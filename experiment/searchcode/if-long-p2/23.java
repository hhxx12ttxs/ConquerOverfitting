/*
      Copyright (C) 2002-2004 MySQL AB

      This program is free software; you can redistribute it and/or modify
      it under the terms of version 2 of the GNU General Public License as 
      published by the Free Software Foundation.

      There are special exceptions to the terms and conditions of the GPL 
      as it is applied to this software. View the full text of the 
      exception in file EXCEPTIONS-CONNECTOR-J in the directory of this 
      software distribution.

      This program is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
      GNU General Public License for more details.

      You should have received a copy of the GNU General Public License
      along with this program; if not, write to the Free Software
      Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA



 */
package com.mysql.jdbc;

import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.log.LogFactory;
import com.mysql.jdbc.log.NullLogger;
import com.mysql.jdbc.profiler.ProfileEventSink;
import com.mysql.jdbc.profiler.ProfilerEvent;
import com.mysql.jdbc.util.LRUCache;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;

import java.net.URL;

import java.sql.Clob;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Time;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;


/**
 * A Connection represents a session with a specific database.  Within the
 * context of a Connection, SQL statements are executed and results are
 * returned.
 * 
 * <P>
 * A Connection's database is able to provide information describing its
 * tables, its supported SQL grammar, its stored procedures, the capabilities
 * of this connection, etc.  This information is obtained with the getMetaData
 * method.
 * </p>
 *
 * @author Mark Matthews
 * @version $Id: Connection.java,v 1.31.4.75.2.25 2004/12/23 16:25:42 mmatthew Exp $
 *
 * @see java.sql.Connection
 */
public class Connection extends ConnectionProperties
    implements java.sql.Connection {
    // The command used to "ping" the database.
    // Newer versions of MySQL server have a ping() command,
    // but this works for everything.4
    private static final String PING_COMMAND = "SELECT 1";
    
    /**
     * Map mysql transaction isolation level name to
     * java.sql.Connection.TRANSACTION_XXX
     */
    private static Map mapTransIsolationNameToValue = null;

    private static final Map serverConfigByUrl = new HashMap();
    
    private static final Map serverCollationByUrl = new HashMap();
    
    /**
     * The mapping between MySQL charset names and Java charset names.
     * Initialized by loadCharacterSetMapping()
     */
    public static Map charsetMap;
    
    /**
     * The mapping between MySQL charset names and the max number of chars
     * in them. Lazily instantiated via getMaxBytesPerChar().
     */
    private Map charsetToNumBytesMap;

    /** Table of multi-byte charsets. Initialized by loadCharacterSetMapping() */
    private static Map multibyteCharsetsMap;

    /** Default logger class name */
    protected static final String DEFAULT_LOGGER_CLASS = "com.mysql.jdbc.log.StandardLogger";

    /** Logger instance name */
    private static final String LOGGER_INSTANCE_NAME = "MySQL";

    /** Null logger shared by all connections at startup */
    private static final Log NULL_LOGGER = new NullLogger(LOGGER_INSTANCE_NAME);

    static {
        loadCharacterSetMapping();
        mapTransIsolationNameToValue = new HashMap(8);
        mapTransIsolationNameToValue.put("READ-UNCOMMITED",
            new Integer(TRANSACTION_READ_UNCOMMITTED));
        mapTransIsolationNameToValue.put("READ-UNCOMMITTED",
            new Integer(TRANSACTION_READ_UNCOMMITTED));
        mapTransIsolationNameToValue.put("READ-COMMITTED",
            new Integer(TRANSACTION_READ_COMMITTED));
        mapTransIsolationNameToValue.put("REPEATABLE-READ",
            new Integer(TRANSACTION_REPEATABLE_READ));
        mapTransIsolationNameToValue.put("SERIALIZABLE",
            new Integer(TRANSACTION_SERIALIZABLE));
    }

    /**
     * Marker for character set converter not being available (not written,
     * multibyte, etc)  Used to prevent multiple instantiation requests.
     */
    private static final Object CHARSET_CONVERTER_NOT_AVAILABLE_MARKER = new Object();
    private static Map roundRobinStatsMap;
    private final static int HISTOGRAM_BUCKETS = 20;

    /** Internal DBMD to use for various database-version specific features */
    private DatabaseMetaData dbmd = null;
    private LRUCache parsedCallableStatementCache;

    /** The list of host(s) to try and connect to */
    private List hostList = null;

    /** The logger we're going to use */
    private Log log = NULL_LOGGER;

    /** A map of SQL to parsed prepared statement parameters. */
    private Map cachedPreparedStatementParams;

    /**
     * Holds cached mappings to charset converters to avoid static
     * synchronization and at the same time save memory (each charset
     * converter takes approx 65K of static data).
     */
    private Map charsetConverterMap = new HashMap(CharsetMapping.JAVA_TO_MYSQL_CHARSET_MAP
            .size());

    /** A map of currently open statements */
    private Map openStatements;

    /** The map of server variables that we retrieve at connection init. */
    private Map serverVariables = null;

    /** A map of statements that have had setMaxRows() called on them */
    private Map statementsUsingMaxRows;

    /**
     * The type map for UDTs (not implemented, but used by some third-party
     * vendors, most notably IBM WebSphere)
     */
    private Map typeMap;

    /** The I/O abstraction interface (network conn to MySQL server */
    private MysqlIO io = null;

    /** Mutex */
    private final Object mutex = new Object();

    /** The event sink to use for profiling */
    private ProfileEventSink eventSink;

    /** Properties for this connection specified by user */
    private Properties props = null;

    /** The database we're currently using (called Catalog in JDBC terms). */
    private String database = null;

    /** The hostname we're connected to */
    private String host = null;

    /** The JDBC URL we're using */
    private String myURL = null;

    /** The password we used */
    private String password = null;

    /** The user we're connected as */
    private String user = null;

    /** Why was this connection implicitly closed, if known? (for diagnostics) */
    private Throwable forceClosedReason;

    /** Where was this connection implicitly closed? (for diagnostics) */
    private Throwable forcedClosedLocation;

    /** Point of origin where this Connection was created */
    private Throwable pointOfOrigin;
    private TimeZone defaultTimeZone;

    /** The timezone of the server */
    private TimeZone serverTimezoneTZ = null;

    private boolean isServerTzUTC = false;
    /**
     * We need this 'bootstrapped', because 4.1 and newer will send fields back
     * with this even before we fill this dynamically from the server.
     */
    private String[] indexToCharsetMapping = CharsetMapping.INDEX_TO_CHARSET;
    private long[] perfMetricsHistBreakpoints;
    private int[] perfMetricsHistCounts;

    /** Are we in autoCommit mode? */
    private boolean autoCommit = true;

    /** Are we failed-over to a non-master host */
    private boolean failedOver = false;

    /** Does the server suuport isolation levels? */
    private boolean hasIsolationLevels = false;

    /** Does this version of MySQL support quoted identifiers? */
    private boolean hasQuotedIdentifiers = false;

    /** Has this connection been closed? */
    private boolean isClosed = true;

    /** Is the server configured to use lower-case table names only? */
    private boolean lowerCaseTableNames = false;

    /** Has the max-rows setting been changed from the default? */
    private boolean maxRowsChanged = false;

    /** Does this connection need to be tested? */
    private boolean needsPing = false;
    private boolean parserKnowsUnicode = false;

    /** Should we retrieve 'info' messages from the server? */
    private boolean readInfoMsg = false;

    /** Are we in read-only mode? */
    private boolean readOnly = false;

    /** Are transactions supported by the MySQL server we are connected to? */
    private boolean transactionsSupported = false;

    /** Has ANSI_QUOTES been enabled on the server? */
    private boolean useAnsiQuotes = false;

    /** Can we use the "ping" command rather than a query? */
    private boolean useFastPing = false;

    /**
     * Should we use server-side prepared statements? (auto-detected, but can
     * be disabled by user)
     */
    private boolean useServerPreparedStmts = false;
    private double totalQueryTimeMs = 0;

    /** ID used when profiling */
    private int connectionId;

    /** How many hosts are in the host list? */
    private int hostListSize = 0;

    /** isolation level */
    private int isolationLevel = java.sql.Connection.TRANSACTION_READ_COMMITTED;

    /**
     * The largest packet we can send (changed once we know what the server
     * supports, we get this at connection init).
     */
    private int maxAllowedPacket = 65536;
    private int netBufferLength = 16384;

    /** The port number we're connected to (defaults to 3306) */
    private int port = 3306;

    /** The point in time when this connection was created */
    private long connectionCreationTimeMillis = 0;

    /** When did the last query finish? */
    private long lastQueryFinishedTime = 0;

    /**
     * If gathering metrics, what was the execution time of the  longest query
     * so far ?
     */
    private long longestQueryTimeMs = 0;

    /** When did the master fail? */
    private long masterFailTimeMillis = 0L;

    /** When was the last time we reported metrics? */
    private long metricsLastReportedMs;
    private long numberOfPreparedExecutes = 0;
    private long numberOfPrepares = 0;
    private long numberOfQueriesIssued = 0;
    private long numberOfResultSetsFetched = 0;

    /** Number of queries we've issued since the master failed */
    private long queriesIssuedFailedOver = 0;
    private long shortestQueryTimeMs = Long.MAX_VALUE;

	private Calendar dateTimeBindingCal;

    /**
     * Creates a connection to a MySQL Server.
     *
     * @param hostToConnectTo the hostname of the database server
     * @param portToConnectTo the port number the server is listening on
     * @param info a Properties[] list holding the user and password
     * @param databaseToConnectTo the database to connect to
     * @param url the URL of the connection
     * @param d the Driver instantation of the connection
     *
     * @exception SQLException if a database access error occurs
     */
    Connection(String hostToConnectTo, int portToConnectTo, Properties info,
        String databaseToConnectTo, String url, NonRegisteringDriver d)
        throws SQLException {
        this.connectionCreationTimeMillis = System.currentTimeMillis();
        this.pointOfOrigin = new Throwable();

        //
        // Normally, this code would be in initializeDriverProperties,
        // but we need to do this as early as possible, so we can start 
        // logging to the 'correct' place as early as possible...this.log 
        // points to 'NullLogger' for every connection at startup to avoid 
        // NPEs and the overhead of checking for NULL at every logging call.
        //
        // We will reset this to the configured logger during properties
        // initialization.
        //
        this.log = LogFactory.getLogger(getLogger(), LOGGER_INSTANCE_NAME);

        // We store this per-connection, due to static synchronization
        // issues in Java's built-in TimeZone class...
        this.defaultTimeZone = TimeZone.getDefault();

        this.openStatements = new HashMap();
        this.serverVariables = new HashMap();
        this.hostList = new ArrayList();

        if (hostToConnectTo == null) {
            this.host = "localhost";
            this.hostList.add(this.host);
        } else if (hostToConnectTo.indexOf(",") != -1) {
            // multiple hosts separated by commas (failover)
            StringTokenizer hostTokenizer = new StringTokenizer(hostToConnectTo,
                    ",", false);

            while (hostTokenizer.hasMoreTokens()) {
                this.hostList.add(hostTokenizer.nextToken().trim());
            }
        } else {
            this.host = hostToConnectTo;
            this.hostList.add(this.host);
        }

        this.hostListSize = this.hostList.size();
        this.port = portToConnectTo;

        if (databaseToConnectTo == null) {
            databaseToConnectTo = "";
        }

        this.database = databaseToConnectTo;
        this.myURL = url;
        this.user = info.getProperty(NonRegisteringDriver.USER_PROPERTY_KEY);
        this.password = info.getProperty(NonRegisteringDriver.PASSWORD_PROPERTY_KEY);

        if ((this.user == null) || this.user.equals("")) {
            this.user = "";
        }

        if (this.password == null) {
            this.password = "";
        }

        this.props = info;
        initializeDriverProperties(info);

        try {
            createNewIO(false);
            this.dbmd = new DatabaseMetaData(this, this.database);
        } catch (SQLException ex) {
            cleanup(new Throwable(), ex);

            // don't clobber SQL exceptions
            throw ex;
        } catch (Exception ex) {
            cleanup(new Throwable(), ex);

            StringBuffer mesg = new StringBuffer();

            if (getParanoid()) {
                mesg.append("Cannot connect to MySQL server on ");
                mesg.append(this.host);
                mesg.append(":");
                mesg.append(this.port);
                mesg.append(".\n\n");
                mesg.append("Make sure that there is a MySQL server ");
                mesg.append("running on the machine/port you are trying ");
                mesg.append(
                    "to connect to and that the machine this software is "
                    + "running on ");
                mesg.append("is able to connect to this host/port "
                    + "(i.e. not firewalled). ");
                mesg.append(
                    "Also make sure that the server has not been started "
                    + "with the --skip-networking ");
                mesg.append("flag.\n\n");
            } else {
                mesg.append("Unable to connect to database.");
            }

            mesg.append("Underlying exception: \n\n");
            mesg.append(ex.getClass().getName());

            if (!getParanoid()) {
                mesg.append(Util.stackTraceToString(ex));
            }

            throw new SQLException(mesg.toString(),
                SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE);
        }
    }

    /**
     * If a connection is in auto-commit mode, than all its SQL statements will
     * be executed and committed as individual transactions.  Otherwise, its
     * SQL statements are grouped into transactions that are terminated by
     * either commit() or rollback().  By default, new connections are in
     * auto- commit mode.  The commit occurs when the statement completes or
     * the next execute occurs, whichever comes first.  In the case of
     * statements returning a ResultSet, the statement completes when the last
     * row of the ResultSet has been retrieved or the ResultSet has been
     * closed.  In advanced cases, a single statement may return multiple
     * results as well as output parameter values.  Here the commit occurs
     * when all results and output param values have been retrieved.
     * 
     * <p>
     * <b>Note:</b> MySQL does not support transactions, so this method is a
     * no-op.
     * </p>
     *
     * @param autoCommitFlag - true enables auto-commit; false disables it
     *
     * @exception SQLException if a database access error occurs
     */
    public void setAutoCommit(boolean autoCommitFlag) throws SQLException {
        checkClosed();

        if (getAutoReconnectForPools()) {
            setHighAvailability(true);
        }

        try {
            if (this.transactionsSupported) {
                // this internal value must be set first as failover depends on it
                // being set to true to fail over (which is done by most
                // app servers and connection pools at the end of
                // a transaction), and the driver issues an implicit set
                // based on this value when it (re)-connects to a server
                // so the value holds across connections
                this.autoCommit = autoCommitFlag;

                boolean needsSetOnServer = true;
                
                if (!this.getHighAvailability()) {
                	needsSetOnServer = this.io.isSetNeededForAutoCommitMode(autoCommitFlag);
                }
                
                if (needsSetOnServer) {
                	execSQL(null,
                			autoCommitFlag ? "SET autocommit=1" : "SET autocommit=0",
                				-1, null, java.sql.ResultSet.TYPE_FORWARD_ONLY,
								java.sql.ResultSet.CONCUR_READ_ONLY, false, false,
								this.database, true, Statement.USES_VARIABLES_FALSE);
                }
  
            } else {
                if ((autoCommitFlag == false) && !getRelaxAutoCommit()) {
                    throw new SQLException("MySQL Versions Older than 3.23.15 "
                        + "do not support transactions",
                        SQLError.SQL_STATE_CONNECTION_NOT_OPEN);
                }
                    
                this.autoCommit = autoCommitFlag;
            }
        } finally {
            if (this.getAutoReconnectForPools()) {
                setHighAvailability(false);
            }
        }

        return;
    }

    /**
     * Gets the current auto-commit state
     *
     * @return Current state of auto-commit
     *
     * @exception SQLException if an error occurs
     *
     * @see setAutoCommit
     */
    public boolean getAutoCommit() throws SQLException {
        return this.autoCommit;
    }

    /**
     * A sub-space of this Connection's database may be selected by setting a
     * catalog name.  If the driver does not support catalogs, it will
     * silently ignore this request
     * 
     * <p>
     * <b>Note:</b> MySQL's notion of catalogs are individual databases.
     * </p>
     *
     * @param catalog the database for this connection to use
     *
     * @throws SQLException if a database access error occurs
     */
    public void setCatalog(String catalog) throws SQLException {
        checkClosed();

        String quotedId = this.dbmd.getIdentifierQuoteString();

        if ((quotedId == null) || quotedId.equals(" ")) {
            quotedId = "";
        }

        StringBuffer query = new StringBuffer("USE ");
        query.append(quotedId);
        query.append(catalog);
        query.append(quotedId);

        execSQL(null, query.toString(), -1, null,
            java.sql.ResultSet.TYPE_FORWARD_ONLY,
            java.sql.ResultSet.CONCUR_READ_ONLY, false, false, this.database,
            true, Statement.USES_VARIABLES_FALSE);
        this.database = catalog;
    }

    /**
     * Return the connections current catalog name, or null if no catalog name
     * is set, or we dont support catalogs.
     * 
     * <p>
     * <b>Note:</b> MySQL's notion of catalogs are individual databases.
     * </p>
     *
     * @return the current catalog name or null
     *
     * @exception SQLException if a database access error occurs
     */
    public String getCatalog() throws SQLException {
        return this.database;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isClosed() {
        return this.isClosed;
    }

    /**
     * @see Connection#setHoldability(int)
     */
    public void setHoldability(int arg0) throws SQLException {
        // do nothing
    }

    /**
     * @see Connection#getHoldability()
     */
    public int getHoldability() throws SQLException {
        return java.sql.ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }

    /**
     * NOT JDBC-Compliant, but clients can use this method to determine how
     * long this connection has been idle. This time (reported in
     * milliseconds) is updated once a query has completed.
     *
     * @return number of ms that this connection has been idle, 0 if the driver
     *         is busy retrieving results.
     */
    public long getIdleFor() {
        if (this.lastQueryFinishedTime == 0) {
            return 0;
        }
            
        long now = System.currentTimeMillis();          
        long idleTime = now - this.lastQueryFinishedTime;
  
        return idleTime;
    }

    /**
     * Returns the log mechanism that should be used to log information
     * from/for this Connection.
     *
     * @return the Log instance to use for logging messages.
     *
     * @throws SQLException if an error occurs
     */
    public Log getLog() throws SQLException {
        return this.log;
    }

    /**
     * A connection's database is able to provide information describing its
     * tables, its supported SQL grammar, its stored procedures, the
     * capabilities of this connection, etc.  This information is made
     * available through a DatabaseMetaData object.
     *
     * @return a DatabaseMetaData object for this connection
     *
     * @exception SQLException if a database access error occurs
     */
    public java.sql.DatabaseMetaData getMetaData() throws SQLException {
        checkClosed();

        return new DatabaseMetaData(this, this.database);
    }

    /**
     * You can put a connection in read-only mode as a hint to enable database
     * optimizations <B>Note:</B> setReadOnly cannot be called while in the
     * middle of a transaction
     *
     * @param readOnlyFlag - true enables read-only mode; false disables it
     *
     * @exception SQLException if a database access error occurs
     */
    public void setReadOnly(boolean readOnlyFlag) throws SQLException {
        checkClosed();
        this.readOnly = readOnlyFlag;
    }

    /**
     * Tests to see if the connection is in Read Only Mode.  Note that we
     * cannot really put the database in read only mode, but we pretend we can
     * by returning the value of the readOnly flag
     *
     * @return true if the connection is read only
     *
     * @exception SQLException if a database access error occurs
     */
    public boolean isReadOnly() throws SQLException {
        return this.readOnly;
    }

    /**
     * @see Connection#setSavepoint()
     */
    public java.sql.Savepoint setSavepoint() throws SQLException {
        MysqlSavepoint savepoint = new MysqlSavepoint();

        setSavepoint(savepoint);

        return savepoint;
    }

    /**
     * @see Connection#setSavepoint(String)
     */
    public java.sql.Savepoint setSavepoint(String name)
        throws SQLException {
        MysqlSavepoint savepoint = new MysqlSavepoint(name);

        setSavepoint(savepoint);

        return savepoint;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public TimeZone getServerTimezoneTZ() {
        return this.serverTimezoneTZ;
    }

    /**
     * DOCUMENT ME!
     *
     * @param level DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    public void setTransactionIsolation(int level) throws SQLException {
        checkClosed();

        if (this.hasIsolationLevels) {
            String sql = null;

            switch (level) {
            case java.sql.Connection.TRANSACTION_NONE:
                throw new SQLException("Transaction isolation level "
                    + "NONE not supported by MySQL");

            case java.sql.Connection.TRANSACTION_READ_COMMITTED:
                sql = "SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED";

                break;

            case java.sql.Connection.TRANSACTION_READ_UNCOMMITTED:
                sql = "SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED";

                break;

            case java.sql.Connection.TRANSACTION_REPEATABLE_READ:
                sql = "SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ";

                break;

            case java.sql.Connection.TRANSACTION_SERIALIZABLE:
                sql = "SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE";

                break;

            default:
                throw new SQLException("Unsupported transaction "
                    + "isolation level '" + level + "'",
                    SQLError.SQL_STATE_DRIVER_NOT_CAPABLE);
            }

            execSQL(null, sql, -1, null, java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY, false, false,
                this.database, true, Statement.USES_VARIABLES_FALSE);
            this.isolationLevel = level;
        } else {
            throw new SQLException("Transaction Isolation Levels are "
                + "not supported on MySQL versions older than 3.23.36.",
                SQLError.SQL_STATE_DRIVER_NOT_CAPABLE);
        }
    }

    /**
     * Get this Connection's current transaction isolation mode.
     *
     * @return the current TRANSACTION_ mode value
     *
     * @exception SQLException if a database access error occurs
     */
    public int getTransactionIsolation() throws SQLException {
        if (this.hasIsolationLevels) {
            java.sql.Statement stmt = null;
            java.sql.ResultSet rs = null;

            try {
                stmt = this.createStatement();
                stmt.setEscapeProcessing(false);

                String query = null;

                if (versionMeetsMinimum(4, 0, 3)) {
                    query = "SHOW VARIABLES LIKE 'tx_isolation'";
                } else {
                    query = "SHOW VARIABLES LIKE 'transaction_isolation'";
                }

                rs = stmt.executeQuery(query);

                if (rs.next()) {
                    String s = rs.getString(2);

                    if (s != null) {
                        Integer intTI = (Integer) mapTransIsolationNameToValue
                            .get(s);

                        if (intTI != null) {
                            return intTI.intValue();
                        }
                    }

                    throw new SQLException(
                        "Could not map transaction isolation '" + s
                        + " to a valid JDBC level.",
                        SQLError.SQL_STATE_GENERAL_ERROR);
                }
                    
                throw new SQLException("Could not retrieve transaction isolation level from server",
                		SQLError.SQL_STATE_GENERAL_ERROR);
                
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (Exception ex) {
                        // ignore
                        ;
                    }

                    rs = null;
                }

                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (Exception ex) {
                        // ignore
                        ;
                    }

                    stmt = null;
                }
            }
        }

        return this.isolationLevel;
    }

    /**
     * JDBC 2.0 Install a type-map object as the default type-map for this
     * connection
     *
     * @param map the type mapping
     *
     * @throws SQLException if a database error occurs.
     */
    public synchronized void setTypeMap(java.util.Map map)
        throws SQLException {
        this.typeMap = map;
    }

    /**
     * JDBC 2.0 Get the type-map object associated with this connection. By
     * default, the map returned is empty.
     *
     * @return the type map
     *
     * @throws SQLException if a database error occurs
     */
    public synchronized java.util.Map getTypeMap() throws SQLException {
        if (this.typeMap == null) {
            this.typeMap = new HashMap();
        }

        return this.typeMap;
    }

    /**
     * The first warning reported by calls on this Connection is returned.
     * <B>Note:</B> Sebsequent warnings will be changed to this
     * java.sql.SQLWarning
     *
     * @return the first java.sql.SQLWarning or null
     *
     * @exception SQLException if a database access error occurs
     */
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    /**
     * Changes the user on this connection by performing a re-authentication.
     * If authentication fails, the connection will remain under the context
     * of the current user.
     *
     * @param userName the username to authenticate with
     * @param newPassword the password to authenticate with
     *
     * @throws SQLException if authentication fails, or some other error occurs
     *         while performing the command.
     */
    public void changeUser(String userName, String newPassword)
        throws SQLException {
        if ((userName == null) || userName.equals("")) {
            userName = "";
        }

        if (newPassword == null) {
            newPassword = "";
        }

        this.io.changeUser(userName, newPassword, this.database);
        this.user = userName;
        this.password = newPassword;
        
        if (versionMeetsMinimum(4, 1, 0)) {
        	configureClientCharacterSet();
        }
    }

    /**
     * After this call, getWarnings returns null until a new warning is
     * reported for this connection.
     *
     * @exception SQLException if a database access error occurs
     */
    public void clearWarnings() throws SQLException {
        // firstWarning = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param sql DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    public PreparedStatement clientPrepareStatement(String sql)
        throws SQLException {
        return clientPrepareStatement(sql,
            java.sql.ResultSet.TYPE_SCROLL_SENSITIVE,
            java.sql.ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * DOCUMENT ME!
     *
     * @param sql DOCUMENT ME!
     * @param resultSetType DOCUMENT ME!
     * @param resultSetConcurrency DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    public synchronized PreparedStatement clientPrepareStatement(String sql,
        int resultSetType, int resultSetConcurrency) throws SQLException {
        checkClosed();

        PreparedStatement pStmt = null;

        if (getCachePreparedStatements()) {
            PreparedStatement.ParseInfo pStmtInfo = (PreparedStatement.ParseInfo) this.cachedPreparedStatementParams
                .get(sql);

            if (pStmtInfo == null) {
                pStmt = new com.mysql.jdbc.PreparedStatement(this, sql,
                        this.database);

                PreparedStatement.ParseInfo parseInfo = pStmt.getParseInfo();

                if (parseInfo.statementLength < getPreparedStatementCacheSqlLimit()) {
                    if (this.cachedPreparedStatementParams.size() >= getPreparedStatementCacheSize()) {
                        Iterator oldestIter = this.cachedPreparedStatementParams.keySet()
                                                                                .iterator();
                        long lruTime = Long.MAX_VALUE;
                        String oldestSql = null;

                        while (oldestIter.hasNext()) {
                            String sqlKey = (String) oldestIter.next();
                            PreparedStatement.ParseInfo lruInfo = (PreparedStatement.ParseInfo) this.cachedPreparedStatementParams
                                .get(sqlKey);

                            if (lruInfo.lastUsed < lruTime) {
                                lruTime = lruInfo.lastUsed;
                                oldestSql = sqlKey;
                            }
                        }

                        if (oldestSql != null) {
                            this.cachedPreparedStatementParams.remove(oldestSql);
                        }
                    }

                    this.cachedPreparedStatementParams.put(sql,
                        pStmt.getParseInfo());
                }
            } else {
                pStmtInfo.lastUsed = System.currentTimeMillis();
                pStmt = new com.mysql.jdbc.PreparedStatement(this, sql,
                        this.database, pStmtInfo);
            }
        } else {
            pStmt = new com.mysql.jdbc.PreparedStatement(this, sql,
                    this.database);
        }

        pStmt.setResultSetType(java.sql.ResultSet.TYPE_SCROLL_SENSITIVE);
        pStmt.setResultSetConcurrency(java.sql.ResultSet.CONCUR_READ_ONLY);

        return pStmt;
    }

    /**
     * In some cases, it is desirable to immediately release a Connection's
     * database and JDBC resources instead of waiting for them to be
     * automatically released (cant think why off the top of my head)
     * <B>Note:</B> A Connection is automatically closed when it is garbage
     * collected.  Certain fatal errors also result in a closed connection.
     *
     * @exception SQLException if a database access error occurs
     */
    public void close() throws SQLException {
        realClose(true, true);
    }

    /**
     * The method commit() makes all changes made since the previous
     * commit/rollback permanent and releases any database locks currently
     * held by the Connection.  This method should only be used when
     * auto-commit has been disabled.
     * 
     * <p>
     * <b>Note:</b> MySQL does not support transactions, so this method is a
     * no-op.
     * </p>
     *
     * @exception SQLException if a database access error occurs
     *
     * @see setAutoCommit
     */
    public void commit() throws SQLException {
        checkClosed();

        try {
            // no-op if _relaxAutoCommit == true
            if (this.autoCommit && !getRelaxAutoCommit()) {
                throw new SQLException("Can't call commit when autocommit=true");
            } else if (this.transactionsSupported) {
                execSQL(null, "commit", -1, null,
                    java.sql.ResultSet.TYPE_FORWARD_ONLY,
                    java.sql.ResultSet.CONCUR_READ_ONLY, false, false,
                    this.database, true, Statement.USES_VARIABLES_FALSE);
            }
        } catch (SQLException sqlException) {
        	if (SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE.equals(sqlException.getSQLState())) {
        		throw new SQLException("Communications link failure during commit(). Transaction resolution unknown.", 
        				SQLError.SQL_STATE_TRANSACTION_RESOLUTION_UNKNOWN);
        	}
        	
        	throw sqlException;
        } finally {
            this.needsPing = this.getReconnectAtTxEnd();
        }

        return;
    }

    //--------------------------JDBC 2.0-----------------------------

    /**
     * JDBC 2.0 Same as createStatement() above, but allows the default result
     * set type and result set concurrency type to be overridden.
     *
     * @param resultSetType a result set type, see ResultSet.TYPE_XXX
     * @param resultSetConcurrency a concurrency type, see ResultSet.CONCUR_XXX
     *
     * @return a new Statement object
     *
     * @exception SQLException if a database-access error occurs.
     */
    public java.sql.Statement createStatement(int resultSetType,
        int resultSetConcurrency) throws SQLException {
        checkClosed();

        Statement stmt = new com.mysql.jdbc.Statement(this, this.database);
        stmt.setResultSetType(resultSetType);
        stmt.setResultSetConcurrency(resultSetConcurrency);

        return stmt;
    }

    /**
     * SQL statements without parameters are normally executed using Statement
     * objects.  If the same SQL statement is executed many times, it is more
     * efficient to use a PreparedStatement
     *
     * @return a new Statement object
     *
     * @throws SQLException passed through from the constructor
     */
    public java.sql.Statement createStatement() throws SQLException {
        return createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
            java.sql.ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * @see Connection#createStatement(int, int, int)
     */
    public java.sql.Statement createStatement(int resultSetType,
        int resultSetConcurrency, int resultSetHoldability)
        throws SQLException {
        if (getPedantic()) {
            if (resultSetHoldability != java.sql.ResultSet.HOLD_CURSORS_OVER_COMMIT) {
                throw new SQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        }

        return createStatement(resultSetType, resultSetConcurrency);
    }

    /**
     * Is the server configured to use lower-case table names only?
     *
     * @return true if lower_case_table_names is 'on'
     */
    public boolean lowerCaseTableNames() {
        return this.lowerCaseTableNames;
    }

    /**
     * A driver may convert the JDBC sql grammar into its system's native SQL
     * grammar prior to sending it; nativeSQL returns the native form of the
     * statement that the driver would have sent.
     *
     * @param sql a SQL statement that may contain one or more '?' parameter
     *        placeholders
     *
     * @return the native form of this statement
     *
     * @exception SQLException if a database access error occurs
     */
    public String nativeSQL(String sql) throws SQLException {
        if (sql == null) {
            return null;
        }

        Object escapedSqlResult = EscapeProcessor.escapeSQL(sql);
    	
    	if (escapedSqlResult instanceof String) {
    		return (String)escapedSqlResult;
    	} 
    		
    	return ((EscapeProcessorResult)escapedSqlResult).escapedSql;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean parserKnowsUnicode() {
        return this.parserKnowsUnicode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param sql DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    public java.sql.CallableStatement prepareCall(String sql)
        throws SQLException {
        if (this.getUseUltraDevWorkAround()) {
            return new UltraDevWorkAround(prepareStatement(sql));
        }
            
        return prepareCall(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * JDBC 2.0 Same as prepareCall() above, but allows the default result set
     * type and result set concurrency type to be overridden.
     *
     * @param sql the SQL representing the callable statement
     * @param resultSetType a result set type, see ResultSet.TYPE_XXX
     * @param resultSetConcurrency a concurrency type, see ResultSet.CONCUR_XXX
     *
     * @return a new CallableStatement object containing the pre-compiled SQL
     *         statement
     *
     * @exception SQLException if a database-access error occurs.
     */
    public synchronized java.sql.CallableStatement prepareCall(String sql,
        int resultSetType, int resultSetConcurrency) throws SQLException {
        if (versionMeetsMinimum(5, 0, 0)) {
            CallableStatement cStmt = null;

            if (!getCacheCallableStatements()) {
                cStmt = new CallableStatement(this, sql, this.database);
            } else {
                if (this.parsedCallableStatementCache == null) {
                    this.parsedCallableStatementCache = new LRUCache(getCallableStatementCacheSize());
                }

                CompoundCacheKey key = new CompoundCacheKey(getCatalog(), sql);

                CallableStatement.CallableStatementParamInfo cachedParamInfo = (CallableStatement.CallableStatementParamInfo) this.parsedCallableStatementCache
                    .get(key);

                if (cachedParamInfo != null) {
                    cStmt = new CallableStatement(this, cachedParamInfo);
                } else {
                    cStmt = new CallableStatement(this, sql, this.database);

                    cachedParamInfo = cStmt.paramInfo;

                    this.parsedCallableStatementCache.put(key, cachedParamInfo);
                }
            }

            cStmt.setResultSetType(resultSetType);
            cStmt.setResultSetConcurrency(resultSetConcurrency);

            return cStmt;
        }
            
        throw new SQLException("Callable statements not " + "supported.",
        		SQLError.SQL_STATE_DRIVER_NOT_CAPABLE);
    }

    /**
     * @see Connection#prepareCall(String, int, int, int)
     */
    public java.sql.CallableStatement prepareCall(String sql,
        int resultSetType, int resultSetConcurrency, int resultSetHoldability)
        throws SQLException {
        if (getPedantic()) {
            if (resultSetHoldability != java.sql.ResultSet.HOLD_CURSORS_OVER_COMMIT) {
                throw new SQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        }

        CallableStatement cStmt = (com.mysql.jdbc.CallableStatement) prepareCall(sql,
                resultSetType, resultSetConcurrency);

        return cStmt;
    }

    /**
     * A SQL statement with or without IN parameters can be pre-compiled and
     * stored in a PreparedStatement object.  This object can then be used to
     * efficiently execute this statement multiple times.
     * 
     * <p>
     * <B>Note:</B> This method is optimized for handling parametric SQL
     * statements that benefit from precompilation if the driver supports
     * precompilation. In this case, the statement is not sent to the database
     * until the PreparedStatement is executed.  This has no direct effect on
     * users; however it does affect which method throws certain
     * java.sql.SQLExceptions
     * </p>
     * 
     * <p>
     * MySQL does not support precompilation of statements, so they are handled
     * by the driver.
     * </p>
     *
     * @param sql a SQL statement that may contain one or more '?' IN parameter
     *        placeholders
     *
     * @return a new PreparedStatement object containing the pre-compiled
     *         statement.
     *
     * @exception SQLException if a database access error occurs.
     */
    public java.sql.PreparedStatement prepareStatement(String sql)
        throws SQLException {
        return prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
            java.sql.ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * JDBC 2.0 Same as prepareStatement() above, but allows the default result
     * set type and result set concurrency type to be overridden.
     *
     * @param sql the SQL query containing place holders
     * @param resultSetType a result set type, see ResultSet.TYPE_XXX
     * @param resultSetConcurrency a concurrency type, see ResultSet.CONCUR_XXX
     *
     * @return a new PreparedStatement object containing the pre-compiled SQL
     *         statement
     *
     * @exception SQLException if a database-access error occurs.
     */
    public java.sql.PreparedStatement prepareStatement(String sql,
        int resultSetType, int resultSetConcurrency) throws SQLException {
        checkClosed();

        //
        // FIXME: Create warnings if can't create results of the given
        //        type or concurrency
        //
        PreparedStatement pStmt = null;

        if (this.useServerPreparedStmts) {
            pStmt = serverPrepare(null, sql);
        } else {
            pStmt = new com.mysql.jdbc.PreparedStatement(this, sql,
                    this.database);
        }

        pStmt.setResultSetType(resultSetType);
        pStmt.setResultSetConcurrency(resultSetConcurrency);

        return pStmt;
    }

    /**
     * @see Connection#prepareStatement(String, int, int, int)
     */
    public java.sql.PreparedStatement prepareStatement(String sql,
        int resultSetType, int resultSetConcurrency, int resultSetHoldability)
        throws SQLException {
        if (getPedantic()) {
            if (resultSetHoldability != java.sql.ResultSet.HOLD_CURSORS_OVER_COMMIT) {
                throw new SQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
        }

        return prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    /**
     * @see Connection#prepareStatement(String, int)
     */
    public java.sql.PreparedStatement prepareStatement(String sql,
        int autoGenKeyIndex) throws SQLException {
        java.sql.PreparedStatement pStmt = prepareStatement(sql);

        ((com.mysql.jdbc.PreparedStatement) pStmt).setRetrieveGeneratedKeys(autoGenKeyIndex == java.sql.Statement.RETURN_GENERATED_KEYS);

        return pStmt;
    }

    /**
     * @see Connection#prepareStatement(String, int[])
     */
    public java.sql.PreparedStatement prepareStatement(String sql,
        int[] autoGenKeyIndexes) throws SQLException {
        java.sql.PreparedStatement pStmt = prepareStatement(sql);

        ((com.mysql.jdbc.PreparedStatement) pStmt).setRetrieveGeneratedKeys((autoGenKeyIndexes != null)
            && (autoGenKeyIndexes.length > 0));

        return pStmt;
    }

    /**
     * @see Connection#prepareStatement(String, String[])
     */
    public java.sql.PreparedStatement prepareStatement(String sql,
        String[] autoGenKeyColNames) throws SQLException {
        java.sql.PreparedStatement pStmt = prepareStatement(sql);

        ((com.mysql.jdbc.PreparedStatement) pStmt).setRetrieveGeneratedKeys((autoGenKeyColNames != null)
            && (autoGenKeyColNames.length > 0));

        return pStmt;
    }

    /**
     * @see Connection#releaseSavepoint(Savepoint)
     */
    public void releaseSavepoint(Savepoint arg0) throws SQLException {
       // this is a no-op
    }

    /**
     * Resets the server-side state of this connection. Doesn't work for MySQL
     * versions older than 4.0.6 or if isParanoid() is set (it will become  a
     * no-op in these cases). Usually only used from connection pooling code.
     *
     * @throws SQLException if the operation fails while resetting server
     *         state.
     */
    public void resetServerState() throws SQLException {
        if (!getParanoid()
                && ((this.io != null) & versionMeetsMinimum(4, 0, 6))) {
            changeUser(this.user, this.password);
        }
    }

    /**
     * The method rollback() drops all changes made since the previous
     * commit/rollback and releases any database locks currently held by the
     * Connection.
     *
     * @exception SQLException if a database access error occurs
     *
     * @see commit
     */
    public void rollback() throws SQLException {
        checkClosed();

        try {
            // no-op if _relaxAutoCommit == true
            if (this.autoCommit && !getRelaxAutoCommit()) {
                throw new SQLException("Can't call rollback when autocommit=true",
                    SQLError.SQL_STATE_CONNECTION_NOT_OPEN);
            } else if (this.transactionsSupported) {
                try {
                    rollbackNoChecks();
                } catch (SQLException sqlEx) {
                    // We ignore non-transactional tables if told to do so
                    if (getIgnoreNonTxTables()
                            && (sqlEx.getErrorCode() != SQLError.ER_WARNING_NOT_COMPLETE_ROLLBACK)) {
                        throw sqlEx;
                    }
                }
            }
        } catch (SQLException sqlException) {
        	if (SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE.equals(sqlException.getSQLState())) {
        		throw new SQLException("Communications link failure during rollback(). Transaction resolution unknown.", 
        				SQLError.SQL_STATE_TRANSACTION_RESOLUTION_UNKNOWN);
        	}
        	
        	throw sqlException;
        } finally {
            this.needsPing = this.getReconnectAtTxEnd();
        }
    }

    /**
     * @see Connection#rollback(Savepoint)
     */
    public void rollback(Savepoint savepoint) throws SQLException {
        
        if (versionMeetsMinimum(4, 0, 14)
                || versionMeetsMinimum(4, 1, 1)) {
            checkClosed();

            try {
                StringBuffer rollbackQuery = new StringBuffer(
                        "ROLLBACK TO SAVEPOINT ");
                rollbackQuery.append('`');
                rollbackQuery.append(savepoint.getSavepointName());
                rollbackQuery.append('`');

                java.sql.Statement stmt = null;

                try {
                    stmt = createStatement();

                    stmt.executeUpdate(rollbackQuery.toString());
                } catch (SQLException sqlEx) {
                    int errno = sqlEx.getErrorCode();

                    if (errno == 1181) {
                        String msg = sqlEx.getMessage();

                        if (msg != null) {
                            int indexOfError153 = msg.indexOf("153");

                            if (indexOfError153 != -1) {
                                throw new SQLException("Savepoint '"
                                    + savepoint.getSavepointName()
                                    + "' does not exist",
                                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT, errno);
                            }
                        }
                    }

                    // We ignore non-transactional tables if told to do so
                    if (getIgnoreNonTxTables()
                            && (sqlEx.getErrorCode() != SQLError.ER_WARNING_NOT_COMPLETE_ROLLBACK)) {
                        throw sqlEx;
                    }
                    
                    
                    if (SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE.equals(sqlEx.getSQLState())) {
                    	throw new SQLException("Communications link failure during rollback(). Transaction resolution unknown.", 
                    			SQLError.SQL_STATE_TRANSACTION_RESOLUTION_UNKNOWN);
                    }
                    	
                    throw sqlEx;
                } finally {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (SQLException sqlEx) {
                            ; // ignore
                        }

                        stmt = null;
                    }
                }
            } finally {
                this.needsPing = this.getReconnectAtTxEnd();
            }
        } else {
            throw new NotImplemented();
        }
    }

    /**
     * Used by MiniAdmin to shutdown a MySQL server
     *
     * @throws SQLException if the command can not be issued.
     */
    public void shutdownServer() throws SQLException {
        try {
            this.io.sendCommand(MysqlDefs.SHUTDOWN, null, null, false, null);
        } catch (Exception ex) {
            throw new SQLException("Unhandled exception '" + ex.toString()
                + "'", SQLError.SQL_STATE_GENERAL_ERROR);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean supportsIsolationLevel() {
        return this.hasIsolationLevels;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean supportsQuotedIdentifiers() {
        return this.hasQuotedIdentifiers;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean supportsTransactions() {
        return this.transactionsSupported;
    }

    public boolean versionMeetsMinimum(int major, int minor, int subminor) 
    	throws SQLException {
    	if (this.io == null) {
    		throw new SQLException("Illegal operation on already closed connection", SQLError.SQL_STATE_CONNECTION_NOT_OPEN);
    	}
    	
    	return this.io.versionMeetsMinimum(major, minor, subminor);
    }
    
    /**
     * Returns the Java character encoding name for the given MySQL server
     * charset index
     *
     * @param charsetIndex
     *
     * @return the Java character encoding name for the given MySQL server
     *         charset index
     *
     * @throws SQLException if the character set index isn't known by the
     *         driver
     */
    protected String getCharsetNameForIndex(int charsetIndex)
        throws SQLException {
        String charsetName = null;

        if (charsetIndex != MysqlDefs.NO_CHARSET_INFO) {
            try {
                charsetName = this.indexToCharsetMapping[charsetIndex];
            } catch (ArrayIndexOutOfBoundsException outOfBoundsEx) {
                throw new SQLException(
                    "Unknown character set index for field '" + charsetIndex
                    + "' received from server.",
                    SQLError.SQL_STATE_GENERAL_ERROR);
            }

            // Punt
            if (charsetName == null) {
                charsetName = getEncoding();
            }
        } else {
            charsetName = getEncoding();
        }

        return charsetName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the defaultTimeZone.
     */
    protected TimeZone getDefaultTimeZone() {
        return this.defaultTimeZone;
    }

    
    /**
     * Returns the IO channel to the server
     *
     * @return the IO channel to the server
     *
     * @throws SQLException if the connection is closed.
     */

    /*
    protected MysqlIO getIO() throws SQLException {
=======
    protected MySQLServer getIO() throws SQLException {
>>>>>>> 1.31.4.75.2.22
=======
    protected MysqlIO getIO() throws SQLException {
>>>>>>> 1.31.4.75.2.6
        if ((this.io == null) || this.isClosed) {
            throw new SQLException("Operation not allowed on closed connection",
                SQLError.SQL_STATE_CONNECTION_NOT_OPEN);
        }

        return this.io;
    }
    */
    
    protected synchronized void checkClosedConnection() throws SQLException {
    	if ((this.io == null) || this.isClosed) {
            throw new SQLException("Operation not allowed on closed connection",
                SQLError.SQL_STATE_CONNECTION_NOT_OPEN);
        }
    }

    /**
     * Creates an IO channel to the server
     *
     * @param isForReconnect is this request for a re-connect
     *
     * @return a new MysqlIO instance connected to a server
     *
     * @throws SQLException if a database access error occurs
     * @throws CommunicationsException DOCUMENT ME!
     */
    protected com.mysql.jdbc.MysqlIO createNewIO(boolean isForReconnect)
        throws SQLException {
        MysqlIO newIo = null;

        Properties mergedProps = new Properties();

        mergedProps = exposeAsProperties(this.props);

        if (!getHighAvailability() && !this.failedOver) {
            int hostIndex = 0;

            //
            // TODO: Eventually, when there's enough metadata
            // on the server to support it, we should come up
            // with a smarter way to pick what server to connect
            // to...perhaps even making it 'pluggable'
            //
            if (getRoundRobinLoadBalance()) {
                hostIndex = getNextRoundRobinHostIndex(getURL(), this.hostList);
            }

            for (; hostIndex < this.hostListSize; hostIndex++) {
                try {
                	String newHostPortPair = (String) this.hostList.get(hostIndex);

                    int newPort = 3306;
                    
                    String[] hostPortPair = NonRegisteringDriver.parseHostPortPair(newHostPortPair);
                    String newHost = hostPortPair[NonRegisteringDriver.HOST_NAME_INDEX];
                	
                    if (newHost == null || newHost.trim().length() == 0) {
                    	newHost = "localhost";
                    }
                	
                	if (hostPortPair[NonRegisteringDriver.PORT_NUMBER_INDEX] != null) {
                		try {
                            newPort = Integer.parseInt(hostPortPair[NonRegisteringDriver.PORT_NUMBER_INDEX]);
                        } catch (NumberFormatException nfe) {
                            throw new SQLException(
                                "Illegal connection port value '"
                                + hostPortPair[NonRegisteringDriver.PORT_NUMBER_INDEX] + "'",
                                SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE);
                        }
                	}

                    this.io = new MysqlIO(newHost, newPort, mergedProps,
                            getSocketFactoryClassName(), this,
                            getSocketTimeout());
                    this.io.doHandshake(this.user, this.password, this.database);
                    this.isClosed = false;

                    // save state from old connection
                    boolean oldAutoCommit = getAutoCommit();
                    int oldIsolationLevel = getTransactionIsolation();
                    boolean oldReadOnly = isReadOnly();
                    String oldCatalog = getCatalog();

                    // Server properties might be different
                    // from previous connection, so initialize
                    // again...
                    initializePropsFromServer(this.props);

                    if (isForReconnect) {
                        // Restore state from old connection
                        setAutoCommit(oldAutoCommit);

                        if (this.hasIsolationLevels) {
                            setTransactionIsolation(oldIsolationLevel);
                        }

                        setCatalog(oldCatalog);
                    }

                    if (hostIndex != 0) {
                        setFailedOverState();
                    } else {
                        this.failedOver = false;

                        if (this.hostListSize > 1) {
                            setReadOnly(false);
                        } else {
                            setReadOnly(oldReadOnly);
                        }
                    }

                    break; // low-level connection succeeded
                } catch (SQLException sqlEx) {
                    if (this.io != null) {
                        this.io.forceClose();
                    }

                    String sqlState = sqlEx.getSQLState();

                    if ((sqlState == null)
                            || !sqlState.equals(
                                SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE)) {
                        throw sqlEx;
                    }

                    if ((this.hostListSize - 1) == hostIndex) {
                        throw sqlEx;
                    }
                } catch (Exception unknownException) {
                    if (this.io != null) {
                        this.io.forceClose();
                    }

                    if ((this.hostListSize - 1) == hostIndex) {
                        throw new CommunicationsException(this,
                            (this.io != null)
                            ? this.io.getLastPacketSentTimeMs() : 0,
                            unknownException);
                    }
                }
            }
        } else {
            double timeout = getInitialTimeout();
            boolean connectionGood = false;

            Exception connectionException = null;

            int hostIndex = 0;

            if (getRoundRobinLoadBalance()) {
                hostIndex = getNextRoundRobinHostIndex(getURL(), this.hostList);
            }

            for (; (hostIndex < this.hostListSize) && !connectionGood;
                    hostIndex++) {
                for (int attemptCount = 0;
                        (attemptCount < getMaxReconnects()) && !connectionGood;
                        attemptCount++) {
                    try {
                        if (this.io != null) {
                            this.io.forceClose();
                        }

                        String newHostPortPair = (String) this.hostList.get(hostIndex);

                        int newPort = 3306;
                        
                        String[] hostPortPair = NonRegisteringDriver.parseHostPortPair(newHostPortPair);
                        String newHost = hostPortPair[NonRegisteringDriver.HOST_NAME_INDEX];
                    	
                        if (newHost == null || newHost.trim().length() == 0) {
                        	newHost = "localhost";
                        }
                    	
                    	if (hostPortPair[NonRegisteringDriver.PORT_NUMBER_INDEX] != null) {
                    		try {
                                newPort = Integer.parseInt(hostPortPair[NonRegisteringDriver.PORT_NUMBER_INDEX]);
                            } catch (NumberFormatException nfe) {
                                throw new SQLException(
                                    "Illegal connection port value '"
                                    + hostPortPair[NonRegisteringDriver.PORT_NUMBER_INDEX] + "'",
                                    SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE);
                            }
                    	}

                        this.io = new MysqlIO(newHost, newPort, mergedProps,
                                getSocketFactoryClassName(), this,
                                getSocketTimeout());
                        this.io.doHandshake(this.user, this.password,
                            this.database);

                        ping();
                        this.isClosed = false;

                        // save state from old connection
                        boolean oldAutoCommit = getAutoCommit();
                        int oldIsolationLevel = getTransactionIsolation();
                        boolean oldReadOnly = isReadOnly();
                        String oldCatalog = getCatalog();

                        // Server properties might be different
                        // from previous connection, so initialize
                        // again...
                        initializePropsFromServer(this.props);

                        if (isForReconnect) {
                            // Restore state from old connection
                            setAutoCommit(oldAutoCommit);

                            if (this.hasIsolationLevels) {
                                setTransactionIsolation(oldIsolationLevel);
                            }

                            setCatalog(oldCatalog);
                        }

                        connectionGood = true;

                        if (hostIndex != 0) {
                            setFailedOverState();
                        } else {
                            this.failedOver = false;

                            if (this.hostListSize > 1) {
                                setReadOnly(false);
                            } else {
                                setReadOnly(oldReadOnly);
                            }
                        }

                        break;
                    } catch (Exception EEE) {
                        connectionException = EEE;
                        connectionGood = false;
                    }

                    if (connectionGood) {
                        break;
                    }

                    try {
                        Thread.sleep((long) timeout * 1000);
                        timeout = timeout * 2;
                    } catch (InterruptedException IE) {
                        ;
                    }
                } // end attempts for a single host  
            } // end iterator for list of hosts
            
            if (!connectionGood) {
                // We've really failed!
                throw new SQLException(
                    "Server connection failure during transaction. Due to underlying exception: '"
                    + connectionException + "'."
                    + (getParanoid() ? ""
                                     : Util.stackTraceToString(
                        connectionException)) + "\nAttempted reconnect "
                    + getMaxReconnects() + " times. Giving up.",
                    SQLError.SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE);
            }
        }

        if (getParanoid() && !getHighAvailability() && (this.hostListSize <= 1)) {
            this.password = null;
            this.user = null;
        }

        if (isForReconnect) {
            //
            // Retrieve any 'lost' prepared statements if re-connecting
            //
            Iterator statementIter = this.openStatements.values().iterator();

            //
            // We build a list of these outside the map of open statements, because
            // in the process of re-preparing, we might end up having to close
            // a prepared statement, thus removing it from the map, and generating
            // a ConcurrentModificationException
            //
            Stack serverPreparedStatements = null;

            while (statementIter.hasNext()) {
                Object statementObj = statementIter.next();

                if (statementObj instanceof ServerPreparedStatement) {
                    if (serverPreparedStatements == null) {
                        serverPreparedStatements = new Stack();
                    }

                    serverPreparedStatements.add(statementObj);
                }
            }

            if (serverPreparedStatements != null) {
                while (!serverPreparedStatements.isEmpty()) {
                	ServerPreparedStatement pstmt = ((ServerPreparedStatement) serverPreparedStatements.pop());
                	serverPrepare(pstmt, pstmt.getOriginalSql());
                }
            }
        }

        return newIo;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Throwable DOCUMENT ME!
     */
    protected void finalize() throws Throwable {
        cleanup(new Throwable(), null);
    }

    protected void incrementNumberOfPreparedExecutes() {
        if (getGatherPerformanceMetrics()) {
            this.numberOfPreparedExecutes++;

            // We need to increment this, because
            // server-side prepared statements bypass
            // any execution by the connection itself...
            this.numberOfQueriesIssued++;
        }
    }

    protected void incrementNumberOfPrepares() {
        if (getGatherPerformanceMetrics()) {
            this.numberOfPrepares++;
        }
    }

    protected void incrementNumberOfResultSetsFetched() {
        if (getGatherPerformanceMetrics()) {
            this.numberOfResultSetsFetched++;
        }
    }

    /**
     * Closes connection and frees resources.
     *
     * @param calledExplicitly is this being called from close()
     * @param issueRollback should a rollback() be issued?
     *
     * @throws SQLException if an error occurs
     */
    protected void realClose(boolean calledExplicitly, boolean issueRollback)
        throws SQLException {
        SQLException sqlEx = null;

        if (this.isAborted) {
        	// do local cleanup
        	
        	localCleanupNonBlocking();
        	
        	return;
        }
        
        try {
	        if (!isClosed()) {
	            if (!getAutoCommit() && issueRollback) {
	                try {
	                    rollback();
	                } catch (SQLException ex) {
	                    sqlEx = ex;
	                }
	            }
	
	            reportMetrics();
	
	            if (getUseUsageAdvisor()) {
	                if (!calledExplicitly) {
	                    String message = "Connection implicitly closed by Driver. You should call Connection.close() from your code to free resources more efficiently and avoid resource leaks.";
	
	                    this.eventSink.consumeEvent(new ProfilerEvent(
	                            ProfilerEvent.TYPE_WARN, "", //$NON-NLS-1$
	                            this.getCatalog(), this.getId(), -1, -1,
	                            System.currentTimeMillis(), 0, null,
	                            this.pointOfOrigin, message));
	                }
	
	                long connectionLifeTime = System.currentTimeMillis()
	                    - this.connectionCreationTimeMillis;
	
	                if (connectionLifeTime < 500) {
	                    String message = "Connection lifetime of < .5 seconds. You might be un-necessarily creating short-lived connections and should investigate connection pooling to be more efficient.";
	
	                    this.eventSink.consumeEvent(new ProfilerEvent(
	                            ProfilerEvent.TYPE_WARN, "", //$NON-NLS-1$
	                            this.getCatalog(), this.getId(), -1, -1,
	                            System.currentTimeMillis(), 0, null,
	                            this.pointOfOrigin, message));
	                }
	            }
	        }
	
	        try {
	            closeAllOpenStatements();
	        } catch (SQLException ex) {
	            sqlEx = ex;
	        }
	
	        if (this.io != null) {
	            try {
	                this.io.quit();
	            } catch (Exception e) {
	                ;
	            }
	        }
	        
	        if (sqlEx != null) {
	            throw sqlEx;
	        }
        } finally {
        	this.isClosed = true;
        	localCleanupNonBlocking();
        }
    }
    
    private void localCleanupNonBlocking() {
    	this.openStatements = null;
    	this.io = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param queryTimeMs
     */
    protected void registerQueryExecutionTime(long queryTimeMs) {
        if (queryTimeMs > this.longestQueryTimeMs) {
            this.longestQueryTimeMs = queryTimeMs;

            repartitionHistogram();
        }

        addToHistogram(queryTimeMs, 1);

        if (queryTimeMs < this.shortestQueryTimeMs) {
            this.shortestQueryTimeMs = (queryTimeMs == 0) ? 1 : queryTimeMs;
        }

        this.numberOfQueriesIssued++;

        this.totalQueryTimeMs += queryTimeMs;
    }

    /**
     * Returns the locally mapped instance of a charset converter (to avoid
     * overhead of static synchronization).
     *
     * @param javaEncodingName the encoding name to retrieve
     *
     * @return a character converter, or null if one couldn't be mapped.
     */
    synchronized SingleByteCharsetConverter getCharsetConverter(
        String javaEncodingName) {
        if (javaEncodingName == null) {
            return null;
        }

        SingleByteCharsetConverter converter = (SingleByteCharsetConverter) this.charsetConverterMap
            .get(javaEncodingName);

        if (converter == CHARSET_CONVERTER_NOT_AVAILABLE_MARKER) {
            return null;
        }

        if (converter == null) {
            try {
                converter = SingleByteCharsetConverter.getInstance(javaEncodingName);

                if (converter == null) {
                    this.charsetConverterMap.put(javaEncodingName,
                        CHARSET_CONVERTER_NOT_AVAILABLE_MARKER);
                }

                this.charsetConverterMap.put(javaEncodingName, converter);
            } catch (UnsupportedEncodingException unsupEncEx) {
                this.charsetConverterMap.put(javaEncodingName,
                    CHARSET_CONVERTER_NOT_AVAILABLE_MARKER);

                converter = null;
            }
        }

        return converter;
    }

    int getId() {
        return this.connectionId;
    }

    /**
     * Returns the maximum packet size the MySQL server will accept
     *
     * @return DOCUMENT ME!
     */
    int getMaxAllowedPacket() {
        return this.maxAllowedPacket;
    }

    /**
     * Returns the Mutex all queries are locked against
     *
     * @return DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    Object getMutex() throws SQLException {
        if (this.io == null) {
            throw new SQLException("Connection.close() has already been called. Invalid operation in this state.",
                SQLError.SQL_STATE_CONNECTION_NOT_OPEN);
        }

        reportMetricsIfNeeded();

        return this.mutex;
    }

    /**
     * Returns the packet buffer size the MySQL server reported upon connection
     *
     * @return DOCUMENT ME!
     */
    int getNetBufferLength() {
        return this.netBufferLength;
    }

    void setReadInfoMsgEnabled(boolean flag) {
        this.readInfoMsg = flag;
    }

    boolean isReadInfoMsgEnabled() {
        return this.readInfoMsg;
    }

    int getServerMajorVersion() {
        return this.io.getServerMajorVersion();
    }

    int getServerMinorVersion() {
        return this.io.getServerMinorVersion();
    }

    int getServerSubMinorVersion() {
        return this.io.getServerSubMinorVersion();
    }

    String getServerVariable(String variableName) {
        if (this.serverVariables != null) {
            return (String) this.serverVariables.get(variableName);
        }
            
        return null;
    }

    String getServerVersion() {
        return this.io.getServerVersion();
    }

    String getURL() {
        return this.myURL;
    }

    String getUser() {
        return this.user;
    }

    /**
     * Send a query to the server.  Returns one of the ResultSet objects. This
     * is synchronized, so Statement's queries will be serialized.
     *
     * @param callingStatement DOCUMENT ME!
     * @param sql the SQL statement to be executed
     * @param maxRows DOCUMENT ME!
     * @param packet DOCUMENT ME!
     * @param resultSetType DOCUMENT ME!
     * @param resultSetConcurrency DOCUMENT ME!
     * @param streamResults DOCUMENT ME!
     * @param queryIsSelectOnly DOCUMENT ME!
     * @param catalog DOCUMENT ME!
     * @param unpackFields DOCUMENT ME!
     *
     * @return a ResultSet holding the results
     *
     * @exception SQLException if a database error occurs
     */

    //ResultSet execSQL(Statement callingStatement, String sql,
    //   int maxRowsToRetreive, String catalog) throws SQLException {
    //    return execSQL(callingStatement, sql, maxRowsToRetreive, null,
    //        java.sql.ResultSet.TYPE_FORWARD_ONLY,
    //        java.sql.ResultSet.CONCUR_READ_ONLY, catalog);
    //}
    //ResultSet execSQL(Statement callingStatement, String sql, int maxRows,
    //    int resultSetType, int resultSetConcurrency, boolean streamResults,
    //    boolean queryIsSelectOnly, String catalog, boolean unpackFields) throws SQLException {
    //    return execSQL(callingStatement, sql, maxRows, null, resultSetType,
    //        resultSetConcurrency, streamResults, queryIsSelectOnly, catalog, unpackFields);
    //}
    ResultSet execSQL(Statement callingStatement, String sql, int maxRows,
        Buffer packet, int resultSetType, int resultSetConcurrency,
        boolean streamResults, boolean queryIsSelectOnly, String catalog,
        boolean unpackFields) throws SQLException {
        return execSQL(callingStatement, sql, maxRows, packet, resultSetType,
            resultSetConcurrency, streamResults, queryIsSelectOnly, catalog,
            unpackFields, Statement.USES_VARIABLES_FALSE);
    }

    ResultSet execSQL(Statement callingStatement, String sql, int maxRows,
        Buffer packet, int resultSetType, int resultSetConcurrency,
        boolean streamResults, boolean queryIsSelectOnly, String catalog,
        boolean unpackFields, byte queryUsesVariables)
        throws SQLException {
        //
        // Fall-back if the master is back online if we've
        // issued queriesBeforeRetryMaster queries since
        // we failed over
        //
        synchronized (this.mutex) {
            long queryStartTime = 0;

            int endOfQueryPacketPosition = 0;
            
            if (packet != null) {
            	endOfQueryPacketPosition = packet.getPosition();
            }
            
            if (getGatherPerformanceMetrics()) {
                queryStartTime = System.currentTimeMillis();
            }

            this.lastQueryFinishedTime = 0; // we're busy!

            if (this.failedOver && this.autoCommit) {
                this.queriesIssuedFailedOver++;

                if (shouldFallBack()) {
                    createNewIO(true);

                    String connectedHost = this.io.getHost();

                    if ((connectedHost != null)
                            && this.hostList.get(0).equals(connectedHost)) {
                        this.failedOver = false;
                        this.queriesIssuedFailedOver = 0;
                        setReadOnly(false);
                    }
                }
            }

            if ((getHighAvailability() || this.failedOver)
                    && (this.autoCommit || getAutoReconnectForPools())
                    && this.needsPing) {
                try {
                    ping();

                    this.needsPing = false;
                } catch (Exception Ex) {
                    createNewIO(true);
                }
            }

            try {
                if (packet == null) {
                    String encoding = null;

                    if (getUseUnicode()) {
                        encoding = getEncoding();
                    }

                    return this.io.sqlQueryDirect(callingStatement, sql,
                        encoding, null, maxRows, this, resultSetType,
                        resultSetConcurrency, streamResults, catalog,
                        unpackFields);
                }
                    
                return this.io.sqlQueryDirect(callingStatement, null, null,
                        packet, maxRows, this, resultSetType,
                        resultSetConcurrency, streamResults, catalog,
                        unpackFields);
            } catch (java.sql.SQLException sqlE) {
                // don't clobber SQL exceptions
            	
            	if (getDumpQueriesOnException()) {
            		String extractedSql = extractSqlFromPacket(sql, packet, endOfQueryPacketPosition);
            		StringBuffer messageBuf = new StringBuffer(extractedSql.length() + 32);
            		messageBuf.append("\n\nQuery being executed when exception was thrown:\n\n");
            		messageBuf.append(extractedSql);

            		sqlE = appendMessageToException(sqlE, messageBuf.toString());
            	}
            	
                if ((getHighAvailability() || this.failedOver)) {
                    this.needsPing = true;
                } else {
                    String sqlState = sqlE.getSQLState();

                    if ((sqlState != null)
                            && sqlState.equals(
                                SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE)) {
                        cleanup(new Throwable(), sqlE);
                    }
                }

                throw sqlE;
            } catch (Exception ex) {
                if ((getHighAvailability() || this.failedOver)) {
                    this.needsPing = true;
                } else if (ex instanceof IOException) {
                    cleanup(new Throwable(), ex);
                }

                String exceptionType = ex.getClass().getName();
                String exceptionMessage = ex.getMessage();

                if (!getParanoid()) {
                    exceptionMessage += "\n\nNested Stack Trace:\n";
                    exceptionMessage += Util.stackTraceToString(ex);
                }

                throw new java.sql.SQLException(
                    "Error during query: Unexpected Exception: "
                    + exceptionType + " message given: " + exceptionMessage,
                    SQLError.SQL_STATE_GENERAL_ERROR);
            } finally {
                this.lastQueryFinishedTime = System.currentTimeMillis();

                if (getGatherPerformanceMetrics()) {
                    long queryTime = System.currentTimeMillis()
                        - queryStartTime;

                    registerQueryExecutionTime(queryTime);
                }
            }
        }
    }

    /**
     * Has the maxRows value changed?
     *
     * @param stmt DOCUMENT ME!
     */
    void maxRowsChanged(Statement stmt) {
        synchronized (this.mutex) {
            if (this.statementsUsingMaxRows == null) {
                this.statementsUsingMaxRows = new HashMap();
            }

            this.statementsUsingMaxRows.put(stmt, stmt);

            this.maxRowsChanged = true;
        }
    }

    /**
     * Register a Statement instance as open.
     *
     * @param stmt the Statement instance to remove
     */
    synchronized void registerStatement(Statement stmt) {
        this.openStatements.put(stmt, stmt);
    }

    /**
     * Remove the given statement from the list of open statements
     *
     * @param stmt the Statement instance to remove
     */
    synchronized void unregisterStatement(Statement stmt) {
        this.openStatements.remove(stmt);
    }

    /**
     * Called by statements on their .close() to let the connection know when
     * it is safe to set the connection back to 'default' row limits.
     *
     * @param stmt the statement releasing it's max-rows requirement
     *
     * @throws SQLException if a database error occurs issuing the statement
     *         that sets the limit default.
     */
    void unsetMaxRows(Statement stmt) throws SQLException {
        synchronized (this.mutex) {
            if (this.statementsUsingMaxRows != null) {
                Object found = this.statementsUsingMaxRows.remove(stmt);

                if ((found != null)
                        && (this.statementsUsingMaxRows.size() == 0)) {
                    execSQL(null, "SET OPTION SQL_SELECT_LIMIT=DEFAULT", -1,
                        null, java.sql.ResultSet.TYPE_FORWARD_ONLY,
                        java.sql.ResultSet.CONCUR_READ_ONLY, false, false,
                        this.database, true, Statement.USES_VARIABLES_FALSE);

                    this.maxRowsChanged = false;
                }
            }
        }
    }

    boolean useAnsiQuotedIdentifiers() {
        return this.useAnsiQuotes;
    }

    /**
     * Has maxRows() been set?
     *
     * @return DOCUMENT ME!
     */
    boolean useMaxRows() {
        synchronized (this.mutex) {
            return this.maxRowsChanged;
        }
    }

    /**
     * Sets state for a failed-over connection
     *
     * @throws SQLException DOCUMENT ME!
     */
    private void setFailedOverState() throws SQLException {
        if (getFailOverReadOnly()) {
            setReadOnly(true);
        }

        this.queriesIssuedFailedOver = 0;
        this.failedOver = true;
        this.masterFailTimeMillis = System.currentTimeMillis();
    }

    private static synchronized int getNextRoundRobinHostIndex(String url,
        List hostList) {
        if (roundRobinStatsMap == null) {
            roundRobinStatsMap = new HashMap();
        }

        int[] index = (int[]) roundRobinStatsMap.get(url);

        if (index == null) {
            index = new int[1];
            index[0] = -1;

            roundRobinStatsMap.put(url, index);
        }

        index[0]++;

        if (index[0] > hostList.size()) {
            index[0] = 0;
        }

        return index[0];
    }

    private void setSavepoint(MysqlSavepoint savepoint)
        throws SQLException {
        
        if (versionMeetsMinimum(4, 0, 14)
                || versionMeetsMinimum(4, 1, 1)) {
            checkClosed();

            StringBuffer savePointQuery = new StringBuffer("SAVEPOINT ");
            savePointQuery.append('`');
            savePointQuery.append(savepoint.getSavepointName());
            savePointQuery.append('`');

            java.sql.Statement stmt = null;

            try {
                stmt = createStatement();

                stmt.executeUpdate(savePointQuery.toString());
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException sqlEx) {
                        ; // ignore
                    }

                    stmt = null;
                }
            }
        } else {
            throw new NotImplemented();
        }
    }

    private void addToHistogram(long value, int numberOfTimes) {
        if (this.perfMetricsHistCounts == null) {
            createInitialHistogram();
        }

        for (int i = 0; i < HISTOGRAM_BUCKETS; i++) {
            if (this.perfMetricsHistBreakpoints[i] >= value) {
                this.perfMetricsHistCounts[i] += numberOfTimes;

                break;
            }
        }
    }

    /**
     * Builds the map needed for 4.1.0 and newer servers that maps field-level
     * charset/collation info to a java character encoding name.
     *
     * @throws SQLException DOCUMENT ME!
     */
    private void buildCollationMapping() throws SQLException {
        if (versionMeetsMinimum(4, 1, 0)) {
        	
        	TreeMap sortedCollationMap = null;
        	
        	if (getCacheServerConfiguration()) {
        		synchronized (serverConfigByUrl) {
        			sortedCollationMap = (TreeMap)serverCollationByUrl.get(getURL());
        		}
        	}
        	
            com.mysql.jdbc.Statement stmt = null;
            com.mysql.jdbc.ResultSet results = null;

            

            try {
            	if (sortedCollationMap == null) {
            		sortedCollationMap = new TreeMap();
            		
            		stmt = (com.mysql.jdbc.Statement) createStatement();

            		if (stmt.getMaxRows() != 0) {
            			stmt.setMaxRows(0);
            		}

            		results = (com.mysql.jdbc.ResultSet) stmt.executeQuery(
                        	"SHOW COLLATION");

            		while (results.next()) {
            			String charsetName = results.getString(2);
            			Integer charsetIndex = new Integer(results.getInt(3));

            			sortedCollationMap.put(charsetIndex, charsetName);
            		}
            		
            		if (getCacheServerConfiguration()) {
                		synchronized (serverConfigByUrl) {
                			serverCollationByUrl.put(getURL(), sortedCollationMap);
                		}
                	}

            	}
            	
                // Now, merge with what we already know
                int highestIndex = ((Integer) sortedCollationMap.lastKey())
                    .intValue();

                if (CharsetMapping.INDEX_TO_CHARSET.length > highestIndex) {
                    highestIndex = CharsetMapping.INDEX_TO_CHARSET.length;
                }

                this.indexToCharsetMapping = new String[highestIndex + 1];

                for (int i = 0; i < CharsetMapping.INDEX_TO_CHARSET.length;
                        i++) {
                    this.indexToCharsetMapping[i] = CharsetMapping.INDEX_TO_CHARSET[i];
                }

                for (Iterator indexIter = sortedCollationMap.entrySet()
                                                            .iterator();
                        indexIter.hasNext();) {
                    Map.Entry indexEntry = (Map.Entry) indexIter.next();

                    String mysqlCharsetName = (String) indexEntry.getValue();

                    this.indexToCharsetMapping[((Integer) indexEntry.getKey())
                    .intValue()] = (String) CharsetMapping.MYSQL_TO_JAVA_CHARSET_MAP
                        .get(mysqlCharsetName);
                }
            } catch (java.sql.SQLException e) {
                throw e;
            } finally {
                if (results != null) {
                    try {
                        results.close();
                    } catch (java.sql.SQLException sqlE) {
                        ;
                    }
                }

                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (java.sql.SQLException sqlE) {
                        ;
                    }
                }
            }
        } else {
            // Safety, we already do this as an initializer, but this makes 
            // the intent more clear
            this.indexToCharsetMapping = CharsetMapping.INDEX_TO_CHARSET;
        }
    }

    private void checkClosed() throws SQLException {
        if (this.isClosed) {
            StringBuffer messageBuf = new StringBuffer(
                    "No operations allowed after connection closed.");

            if (this.forcedClosedLocation != null) {
                messageBuf.append("\n\n");
                messageBuf.append(
                    "Connection was implicitly closed at (stack trace):\n");
                messageBuf.append(Util.stackTraceToString(
                        this.forcedClosedLocation));
            }

            if (this.forceClosedReason != null) {
                messageBuf.append("\n\nDue to underlying exception/error:\n");
                messageBuf.append(Util.stackTraceToString(
                        this.forceClosedReason));
            }

            throw new SQLException(messageBuf.toString(),
                SQLError.SQL_STATE_CONNECTION_NOT_OPEN);
        }
    }

    /**
     * If useUnicode flag is set and explicit client character encoding isn't
     * specified then assign encoding from server if any.
     *
     * @throws SQLException DOCUMENT ME!
     */
    private void checkServerEncoding() throws SQLException {
        if (getUseUnicode() && (getEncoding() != null)) {
            // spec'd by client, don't map
            return;
        }

        String serverEncoding = (String) this.serverVariables.get(
                "character_set");

        if (serverEncoding == null) {
            // must be 4.1.1 or newer?	
            serverEncoding = (String) this.serverVariables.get(
                    "character_set_server");
        }

        String mappedServerEncoding = null;

        if (serverEncoding != null) {
            mappedServerEncoding = (String) charsetMap.get(serverEncoding
                    .toUpperCase(Locale.ENGLISH));
        }

        //
        // First check if we can do the encoding ourselves
        //
        if (!getUseUnicode() && (mappedServerEncoding != null)) {
            SingleByteCharsetConverter converter = getCharsetConverter(mappedServerEncoding);

            if (converter != null) { // we know how to convert this ourselves
                setDoUnicode(true); // force the issue
                setEncoding(mappedServerEncoding);

                return;
            }
        }

        //
        // Now, try and find a Java I/O converter that can do
        // the encoding for us
        //
        if (serverEncoding != null) {
            if (mappedServerEncoding == null) {
                // We don't have a mapping for it, so try
                // and canonicalize the name....
                if (Character.isLowerCase(serverEncoding.charAt(0))) {
                    char[] ach = serverEncoding.toCharArray();
                    ach[0] = Character.toUpperCase(serverEncoding.charAt(0));
                    setEncoding(new String(ach));
                }
            }

            //
            // Attempt to use the encoding, and bail out if it
            // can't be used
            //
            try {
                "abc".getBytes(mappedServerEncoding);
                setEncoding(mappedServerEncoding);
                setDoUnicode(true);
            } catch (UnsupportedEncodingException UE) {
                throw new SQLException(
                    "The driver can not map the character encoding '"
                    + getEncoding() + "' that your server is using "
                    + "to a character encoding your JVM understands. You "
                    + "can specify this mapping manually by adding \"useUnicode=true\" "
                    + "as well as \"characterEncoding=[an_encoding_your_jvm_understands]\" "
                    + "to your JDBC URL.", "0S100");
            }
        }
    }

    /**
     * Set transaction isolation level to the value received from server if
     * any. Is called by connectionInit(...)
     *
     * @throws SQLException DOCUMENT ME!
     */
    private void checkTransactionIsolationLevel() throws SQLException {
        String txIsolationName = null;

        if (versionMeetsMinimum(4, 0, 3)) {
            txIsolationName = "tx_isolation";
        } else {
            txIsolationName = "transaction_isolation";
        }

        String s = (String) this.serverVariables.get(txIsolationName);

        if (s != null) {
            Integer intTI = (Integer) mapTransIsolationNameToValue.get(s);

            if (intTI != null) {
                this.isolationLevel = intTI.intValue();
            }
        }
    }

    /**
     * Destroys this connection and any underlying resources
     *
     * @param fromWhere DOCUMENT ME!
     * @param whyCleanedUp DOCUMENT ME!
     */
    private void cleanup(Throwable fromWhere, Throwable whyCleanedUp) {
        try {
            if ((this.io != null) && !isClosed()) {
                realClose(false, false);
            } else if (this.io != null) {
                this.io.forceClose();
            }
        } catch (SQLException sqlEx) {
            // ignore, we're going away.
            ;
        }

        this.isClosed = true;
    }

    /**
     * Closes all currently open statements.
     *
     * @throws SQLException DOCUMENT ME!
     */
    private void closeAllOpenStatements() throws SQLException {
        SQLException postponedException = null;

        if (this.openStatements != null) {
            List currentlyOpenStatements = new ArrayList(); // we need this to avoid
                                                            // ConcurrentModificationEx

            for (Iterator iter = this.openStatements.keySet().iterator();
                    iter.hasNext();) {
                currentlyOpenStatements.add(iter.next());
            }

            int numStmts = currentlyOpenStatements.size();

            for (int i = 0; i < numStmts; i++) {
                Statement stmt = (Statement) currentlyOpenStatements.get(i);

                try {
                    stmt.realClose(false);
                } catch (SQLException sqlEx) {
                    postponedException = sqlEx; // throw it later, cleanup all statements first
                }
            }

            if (postponedException != null) {
                throw postponedException;
            }
        }
    }

    /**
     * Configures client-side properties for character set information.
     *
     * @throws SQLException if unable to configure the specified character set.
     */
    private void configureCharsetProperties() throws SQLException {
        if (getEncoding() != null) {
            // Attempt to use the encoding, and bail out if it
            // can't be used
            try {
                String testString = "abc";
                testString.getBytes(getEncoding());
            } catch (UnsupportedEncodingException UE) {
                // Try the MySQL character encoding, then....
                String oldEncoding = getEncoding();

                setEncoding((String) CharsetMapping.MYSQL_TO_JAVA_CHARSET_MAP
                    .get(oldEncoding));

                if (getEncoding() == null) {
                    throw new SQLException(
                        "Java does not support the MySQL character encoding "
                        + " " + "encoding '" + oldEncoding + "'.",
                        SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE);
                }

                try {
                    String testString = "abc";
                    testString.getBytes(getEncoding());
                } catch (UnsupportedEncodingException encodingEx) {
                    throw new SQLException("Unsupported character "
                        + "encoding '" + getEncoding() + "'.",
                        SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE);
                }
            }
        }
    }

    /**
     * Sets up client character set for MySQL-4.1 and newer if the user  This
     * must be done before any further communication with the server!
     *
     * @return true if this routine actually configured the client character
     *         set, or false if the driver needs to use 'older' methods to
     *         detect the character set, as it is connected to a MySQL server
     *         older than 4.1.0
     *
     * @throws SQLException if an exception happens while sending 'SET NAMES'
     *         to the server, or the server sends character set  information
     *         that the client doesn't know about.
     */
    private boolean configureClientCharacterSet() throws SQLException {
        String realJavaEncoding = getEncoding();
        boolean characterSetAlreadyConfigured = false;

        try {
            if (versionMeetsMinimum(4, 1, 0)) {
                characterSetAlreadyConfigured = true;

                setUseUnicode(true);

                configureCharsetProperties();
                realJavaEncoding = getEncoding(); // we need to do this again to grab this for 
                                                  // versions > 4.1.0

                try {
                    setEncoding(CharsetMapping.INDEX_TO_CHARSET[this.io.serverCharsetIndex]);
                } catch (ArrayIndexOutOfBoundsException outOfBoundsEx) {
                    if (realJavaEncoding != null) {
                        // user knows best, try it
                        setEncoding(realJavaEncoding);
                    } else {
                        throw new SQLException(
                            "Unknown initial character set index '"
                            + this.io.serverCharsetIndex
                            + "' received from server. Initial client character set can be forced via the 'characterEncoding' property.",
                            SQLError.SQL_STATE_GENERAL_ERROR);
                    }
                }

                if (getEncoding() == null) {
                    // punt?
                    setEncoding("ISO8859_1");
                }

                //
                // Has the user has 'forced' the character encoding via
                // driver properties?
                //
                if (getUseUnicode()) {
                	if (realJavaEncoding != null) {
	                
	                    //
	                    // Now, inform the server what character set we
	                    // will be using from now-on...
	                    //
                		if (!getUseOldUTF8Behavior()) { 
                    		execSQL(null, "SET NAMES utf8", -1, null,
                    				java.sql.ResultSet.TYPE_FORWARD_ONLY,
									java.sql.ResultSet.CONCUR_READ_ONLY, false, false,
									this.database, true, Statement.USES_VARIABLES_FALSE);
                    	} else {
                    		String mysqlEncodingName = (String) CharsetMapping.JAVA_UC_TO_MYSQL_CHARSET_MAP
                            .get(realJavaEncoding.toUpperCase(Locale.ENGLISH));

                    		if ("koi8_ru".equals(mysqlEncodingName)) {
                    			// This has a _different_ name in 4.1...
                    			mysqlEncodingName = "ko18r";
                    		}

                    		if (mysqlEncodingName != null) {
                    			execSQL(null, "SET NAMES " + mysqlEncodingName, -1,
                    					null, java.sql.ResultSet.TYPE_FORWARD_ONLY,
										java.sql.ResultSet.CONCUR_READ_ONLY, false,
										false, this.database, true,
										Statement.USES_VARIABLES_FALSE);
                    		}
                    	}
                    	
                        setEncoding(realJavaEncoding);
                	} else if (getEncoding() != null) {
                    	// Tell the server we'll use the server default charset to send our
                    	// queries from now on....
                    	String mysqlEncodingName = (String) CharsetMapping.JAVA_UC_TO_MYSQL_CHARSET_MAP
                            	.get(getEncoding().toUpperCase(Locale.ENGLISH));
                    		
                    	execSQL(null, "SET NAMES " + mysqlEncodingName, -1,
                                null, java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                java.sql.ResultSet.CONCUR_READ_ONLY, false,
                                false, this.database, true,
                                Statement.USES_VARIABLES_FALSE);
                    		
                    	realJavaEncoding = getEncoding();
                    }
                	
                }
                
                //
                // We know how to deal with any charset coming back from
                // the database, so tell the server not to do conversion
                // if the user hasn't 'forced' a result-set character set
                //
                
                if (getCharacterSetResults() == null) {
                	execSQL(null, "SET character_set_results = NULL", -1, null, java.sql.ResultSet.TYPE_FORWARD_ONLY,
                            java.sql.ResultSet.CONCUR_READ_ONLY, false,
                            false, this.database, true,
                            Statement.USES_VARIABLES_FALSE);
                } else {
                	StringBuffer setBuf = new StringBuffer("SET character_set_results = ".length() + getCharacterSetResults().length());
                	setBuf.append("SET character_set_results = ").append(getCharacterSetResults());
                	
                	execSQL(null, setBuf.toString(), -1, null, java.sql.ResultSet.TYPE_FORWARD_ONLY,
                            java.sql.ResultSet.CONCUR_READ_ONLY, false,
                            false, this.database, true,
                            Statement.USES_VARIABLES_FALSE);
                }
                
                if (getConnectionCollation() != null) {
                	StringBuffer setBuf = new StringBuffer("SET collation_connection = ".length() + getConnectionCollation().length());
                	setBuf.append("SET collation_connection = ").append(getConnectionCollation());
                	
                	execSQL(null, setBuf.toString(), -1, null, java.sql.ResultSet.TYPE_FORWARD_ONLY,
                            java.sql.ResultSet.CONCUR_READ_ONLY, false,
                            false, this.database, true,
                            Statement.USES_VARIABLES_FALSE);
                }
            } else {
                // Use what the server has specified
                realJavaEncoding = getEncoding(); // so we don't get 
                                                  // swapped out in the finally
                                                  // block....
            }
        } finally {
            // Failsafe, make sure that the driver's notion of character
            // encoding matches what the user has specified.
            //
            setEncoding(realJavaEncoding);
        }

        
        
        return characterSetAlreadyConfigured;
    }

    /**
     * Configures the client's timezone if required.
     *
     * @throws SQLException if the timezone the server is configured to use
     *         can't be mapped to a Java timezone.
     */
    private void configureTimezone() throws SQLException {
    	String configuredTimeZoneOnServer = (String)this.serverVariables.get("timezone");
    	
    	if (configuredTimeZoneOnServer == null) {
    		configuredTimeZoneOnServer = (String)this.serverVariables.get("time_zone");
    		
    		if ("SYSTEM".equalsIgnoreCase(configuredTimeZoneOnServer)) {
    			configuredTimeZoneOnServer = (String)this.serverVariables.get("system_time_zone");
    		}
    	}
    	
        if (getUseTimezone() && configuredTimeZoneOnServer != null) {
            // user can specify/override as property
            String canoncicalTimezone = getServerTimezone();

            if ((canoncicalTimezone == null)
                    || (canoncicalTimezone.length() == 0)) {
                String serverTimezoneStr = configuredTimeZoneOnServer;

                try {
                    canoncicalTimezone = TimeUtil.getCanoncialTimezone(serverTimezoneStr);

                    if (canoncicalTimezone == null) {
                        throw new SQLException("Can't map timezone '"
                            + serverTimezoneStr + "' to "
                            + " canonical timezone.",
                            SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
                    }
                } catch (IllegalArgumentException iae) {
                    throw new SQLException(iae.getMessage(),
                        SQLError.SQL_STATE_GENERAL_ERROR);
                }
            }

            this.serverTimezoneTZ = TimeZone.getTimeZone(canoncicalTimezone);

            //
            // The Calendar class has the behavior of mapping
            // unknown timezones to 'GMT' instead of throwing an 
            // exception, so we must check for this...
            //
            if (!canoncicalTimezone.equalsIgnoreCase("GMT")
                    && this.serverTimezoneTZ.getID().equals("GMT")) {
                throw new SQLException("No timezone mapping entry for '"
                    + canoncicalTimezone + "'",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }
            
            if ("GMT".equalsIgnoreCase(this.serverTimezoneTZ.getID())) {
            	this.isServerTzUTC = true;
            } else {
            	this.isServerTzUTC = false;
            }
        }
    }

    private void createInitialHistogram() {
        this.perfMetricsHistCounts = new int[HISTOGRAM_BUCKETS];
        this.perfMetricsHistBreakpoints = new long[HISTOGRAM_BUCKETS];

        long lowerBound = this.shortestQueryTimeMs;

        if (lowerBound == Long.MAX_VALUE) {
            lowerBound = 0;
        }

        double bucketSize = (((double) this.longestQueryTimeMs
            - (double) lowerBound) / HISTOGRAM_BUCKETS) * 1.25;

        for (int i = 0; i < HISTOGRAM_BUCKETS; i++) {
            this.perfMetricsHistBreakpoints[i] = lowerBound;
            lowerBound += bucketSize;
        }
    }

    /**
     * Initializes driver properties that come from URL or properties passed to
     * the driver manager.
     *
     * @param info DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    private void initializeDriverProperties(Properties info)
        throws SQLException {
        initializeProperties(info);

        this.log = LogFactory.getLogger(getLogger(), LOGGER_INSTANCE_NAME);

        if (getProfileSql() || getUseUsageAdvisor()) {
            this.eventSink = ProfileEventSink.getInstance(this);
        }

        if (getCachePreparedStatements()) {
            this.cachedPreparedStatementParams = new HashMap(getPreparedStatementCacheSize());
        }
    }

    /**
     * Sets varying properties that depend on server information. Called once
     * we have connected to the server.
     *
     * @param info DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    private void initializePropsFromServer(Properties info)
        throws SQLException {
        // We need to do this before any further data gets
        // sent to the server....
        boolean clientCharsetIsConfigured = configureClientCharacterSet();

        this.useFastPing = versionMeetsMinimum(3, 22, 1);

        this.parserKnowsUnicode = versionMeetsMinimum(4, 1, 0);

        //
        // Users can turn off detection of server-side prepared statements
        //
        if (getUseServerPreparedStmts() && versionMeetsMinimum(4, 1, 0)) {
            this.useServerPreparedStmts = true;
            
            if (versionMeetsMinimum(5, 0, 0) && !versionMeetsMinimum(5, 0, 2)) {
            	this.useServerPreparedStmts = false; // 4.1.2+ style prepared statements
            		                                 // don't work on these versions
            }
        }

        this.serverVariables.clear();

        //
        // If version is greater than 3.21.22 get the server
        // variables.
        if (versionMeetsMinimum(3, 21, 22)) {
            loadServerVariables();

            buildCollationMapping();

            LicenseConfiguration.checkLicenseType(this.serverVariables);

            String lowerCaseTables = (String) this.serverVariables.get(
                    "lower_case_table_names");

            this.lowerCaseTableNames = "on".equalsIgnoreCase(lowerCaseTables)
                || "1".equalsIgnoreCase(lowerCaseTables)
                || "2".equalsIgnoreCase(lowerCaseTables);

            configureTimezone();

            if (this.serverVariables.containsKey("max_allowed_packet")) {
                this.maxAllowedPacket = Integer.parseInt((String) this.serverVariables
                        .get("max_allowed_packet"));
            }

            if (this.serverVariables.containsKey("net_buffer_length")) {
                this.netBufferLength = Integer.parseInt((String) this.serverVariables
                        .get("net_buffer_length"));
            }

            checkTransactionIsolationLevel();

            //
            // We only do this for servers older than 4.1.0, because
            // 4.1.0 and newer actually send the server charset
            // during the handshake, and that's handled at the
            // top of this method...
            //
            if (!clientCharsetIsConfigured) {
                checkServerEncoding();
            }

            this.io.checkForCharsetMismatch();

            if (this.serverVariables.containsKey("sql_mode")) {
                int sqlMode = 0;

                try {
                    sqlMode = Integer.parseInt((String) this.serverVariables
                            .get("sql_mode"));
                } catch (NumberFormatException nfe) {
                    sqlMode = 0;
                }

                if ((sqlMode & 4) > 0) {
                    this.useAnsiQuotes = true;
                } else {
                    this.useAnsiQuotes = false;
                }
            }
        }

        if (versionMeetsMinimum(3, 23, 15)) {
            this.transactionsSupported = true;
            setAutoCommit(true); // to override anything
                                 // the server is set to...reqd
                                 // by JDBC spec.
        } else {
            this.transactionsSupported = false;
        }

        if (versionMeetsMinimum(3, 23, 36)) {
            this.hasIsolationLevels = true;
        } else {
            this.hasIsolationLevels = false;
        }

        this.hasQuotedIdentifiers = versionMeetsMinimum(3, 23, 6);

        this.io.resetMaxBuf();
    }

    /**
     * Loads the mapping between MySQL character sets and Java character sets
     */
    private static void loadCharacterSetMapping() {
        multibyteCharsetsMap = new HashMap();

        Iterator multibyteCharsets = CharsetMapping.MULTIBYTE_CHARSETS.keySet()
                                                                      .iterator();

        while (multibyteCharsets.hasNext()) {
            String charset = ((String) multibyteCharsets.next()).toUpperCase(Locale.ENGLISH);
            multibyteCharsetsMap.put(charset, charset);
        }

        //
        // Now change all server encodings to upper-case to "future-proof"
        // this mapping
        //
        Iterator keys = CharsetMapping.MYSQL_TO_JAVA_CHARSET_MAP.keySet()
                                                                .iterator();
        charsetMap = new HashMap();

        while (keys.hasNext()) {
            String mysqlCharsetName = ((String) keys.next()).trim();
            String javaCharsetName = CharsetMapping.MYSQL_TO_JAVA_CHARSET_MAP.get(mysqlCharsetName)
                                                                             .toString()
                                                                             .trim();
            charsetMap.put(mysqlCharsetName.toUpperCase(Locale.ENGLISH), javaCharsetName);
            charsetMap.put(mysqlCharsetName, javaCharsetName);
        }
    }

    /**
     * Loads the result of 'SHOW VARIABLES' into the serverVariables field so
     * that the driver can configure itself.
     *
     * @throws SQLException if the 'SHOW VARIABLES' query fails for any reason.
     */
    private void loadServerVariables() throws SQLException {
    	
    	if (getCacheServerConfiguration()) {
    		synchronized (serverConfigByUrl) {
    			Map cachedVariableMap = (Map)serverConfigByUrl.get(getURL());
    		
    			if (cachedVariableMap != null) {
    				this.serverVariables = cachedVariableMap;
    				
    				return;
    			}
    		}
    	}
    	
        com.mysql.jdbc.Statement stmt = null;
        com.mysql.jdbc.ResultSet results = null;

        try {
            stmt = (com.mysql.jdbc.Statement) createStatement();
            stmt.setEscapeProcessing(false);

            results = (com.mysql.jdbc.ResultSet) stmt.executeQuery(
                    "SHOW VARIABLES");

            while (results.next()) {
                this.serverVariables.put(results.getString(1),
                    results.getString(2));
            }
            
            if (getCacheServerConfiguration()) {
        		synchronized (serverConfigByUrl) {
        			serverConfigByUrl.put(getURL(), this.serverVariables);
        		}
        	}
        } catch (SQLException e) {
            throw e;
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (SQLException sqlE) {
                    ;
                }
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlE) {
                    ;
                }
            }
        }
    }

    // *********************************************************************
    //
    //                END OF PUBLIC INTERFACE
    //
    // *********************************************************************

    /**
     * Detect if the connection is still good
     *
     * @throws Exception DOCUMENT ME!
     */
    private void ping() throws Exception {
        if (this.useFastPing) {
            this.io.sendCommand(MysqlDefs.PING, null, null, false, null);
        } else {
            this.io.sqlQueryDirect(null, PING_COMMAND, this.getEncoding(),
                null, MysqlDefs.MAX_ROWS, this,
                java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY, false, this.database, true);
        }
    }

    /**
        	 *
        	 */
    private void repartitionHistogram() {
        int[] oldHistCounts = this.perfMetricsHistCounts;
        long[] oldHistBreakpoints = this.perfMetricsHistBreakpoints;

        if (this.perfMetricsHistCounts == null) {
            createInitialHistogram();
        }

        long lowerBound = this.shortestQueryTimeMs;

        if (lowerBound == Long.MAX_VALUE) {
            lowerBound = 0;
        }

        double bucketSize = (((double) this.longestQueryTimeMs
            - (double) lowerBound) / HISTOGRAM_BUCKETS) * 1.25;

        for (int i = 0; i < HISTOGRAM_BUCKETS; i++) {
            this.perfMetricsHistBreakpoints[i] = lowerBound;
            lowerBound += bucketSize;
        }

        if (oldHistCounts != null) {
            for (int i = 0; i < HISTOGRAM_BUCKETS; i++) {
                addToHistogram(oldHistBreakpoints[i], oldHistCounts[i]);
            }
        }
    }

    private void reportMetrics() {
    	if (getGatherPerformanceMetrics()) {
    		StringBuffer logMessage = new StringBuffer(256);
    		
    		logMessage.append("** Performance Metrics Report **\n");
    		logMessage.append("\nLongest reported query: "
    				+ this.longestQueryTimeMs + " ms");
    		logMessage.append("\nShortest reported query: "
    				+ this.shortestQueryTimeMs + " ms");
    		logMessage.append("\nAverage query execution time: "
    				+ ((double)this.totalQueryTimeMs / (double)this.numberOfQueriesIssued)
					+ " ms");
    		logMessage.append("\nNumber of queries executed: "
    				+ this.numberOfQueriesIssued);
    		logMessage.append("\nNumber of queries prepared: "
    				+ this.numberOfPrepares);
    		logMessage.append("\nNumber of prepared statement executions: "
    				+ this.numberOfPreparedExecutes);
    		
    		if (this.perfMetricsHistBreakpoints != null) {
    			logMessage.append("\n\n\tHistogram:\n");
    			
    			for (int i = 0; i < (HISTOGRAM_BUCKETS - 1); i++) {
    				logMessage.append("\n\tQueries taking between "
    						+ this.perfMetricsHistBreakpoints[i] + " and "
							+ this.perfMetricsHistBreakpoints[i + 1] + ": "
							+ this.perfMetricsHistCounts[i]);
    			}
    			
    			logMessage.append("\n\tQueries taking between ");
    			logMessage.append(this.perfMetricsHistBreakpoints[HISTOGRAM_BUCKETS - 2]);
    			logMessage.append(" and ");
				logMessage.append(this.perfMetricsHistBreakpoints[HISTOGRAM_BUCKETS - 1]);
				logMessage.append(" ");																		
				logMessage.append(this.perfMetricsHistCounts[HISTOGRAM_BUCKETS - 1]);
    		}
    		
    		this.log.logInfo(logMessage);
    		
    		this.metricsLastReportedMs = System.currentTimeMillis();
    	}
    }

    /**
     * Reports currently collected metrics if this feature is enabled and the
     * timeout has passed.
     */
    private void reportMetricsIfNeeded() {
        if (getGatherPerformanceMetrics()) {
            if ((System.currentTimeMillis() - this.metricsLastReportedMs) > getReportMetricsIntervalMillis()) {
                reportMetrics();
            }
        }
    }

    private void rollbackNoChecks() throws SQLException {
        execSQL(null, "rollback", -1, null,
            java.sql.ResultSet.TYPE_FORWARD_ONLY,
            java.sql.ResultSet.CONCUR_READ_ONLY, false, false, this.database,
            true, Statement.USES_VARIABLES_FALSE);
    }

    /**
     * Should we try to connect back to the master? We try when we've been
     * failed over >= this.secondsBeforeRetryMaster _or_ we've issued >
     * this.queriesIssuedFailedOver
     *
     * @return DOCUMENT ME!
     */
    private boolean shouldFallBack() {
        long secondsSinceFailedOver = (System.currentTimeMillis()
            - this.masterFailTimeMillis) / 1000;

        return ((secondsSinceFailedOver >= getSecondsBeforeRetryMaster())
        || ((this.queriesIssuedFailedOver % getQueriesBeforeRetryMaster()) == 0));
    }

    protected synchronized int getMaxBytesPerChar(String charset) throws SQLException {
    	// TODO: Check if we can actually run this query at this point in time
    	if (versionMeetsMinimum(4, 1, 0)) {
	    	if (this.charsetToNumBytesMap == null) {
	    		this.charsetToNumBytesMap = new HashMap();
	    		
	    		java.sql.Statement stmt = null;
	    		java.sql.ResultSet rs = null;
	    		
	    		try {
	    			stmt = getMetadataSafeStatement();
	    			
	    			rs = stmt.executeQuery("SHOW CHARACTER SET");
	    			
	    			while (rs.next()) {
	    				this.charsetToNumBytesMap.put(rs.getString("Charset"), new Integer(rs.getInt("Maxlen")));
	    			}
	    			
	    			rs.close();
	    			rs = null;
	    			
	    			stmt.close();
	    			
	    			stmt = null;
	    		} finally {
	    			if (rs != null) {
	    				rs.close();
	    				rs = null;
	    			}
	    			
	    			if (stmt != null) {
	    				stmt.close();
	    				stmt = null;
	    			}
	    		}
	    	}
	    	
	    	Integer mbPerChar = (Integer)this.charsetToNumBytesMap.get(charset);
	    	
	    	if (mbPerChar != null) {
	    		return mbPerChar.intValue();
	    	}
	    	
	    	return 1; // we don't know
    	}
    	
    	return 1; // we don't know	
    }
    
    protected String extractSqlFromPacket(String possibleSqlQuery, Buffer
    		queryPacket, int endOfQueryPacketPosition) throws SQLException {

    	String extractedSql = null;
    	
    	if (possibleSqlQuery != null) {
    		if (possibleSqlQuery.length() > getMaxQuerySizeToLog()) {
    			StringBuffer truncatedQueryBuf = new StringBuffer(
    					possibleSqlQuery.substring(0, getMaxQuerySizeToLog()));
    			truncatedQueryBuf.append(Messages.getString("MysqlIO.25"));
    			extractedSql = truncatedQueryBuf.toString();
    		} else {
    			extractedSql = possibleSqlQuery;
    		}
    	}
    	
    	if (extractedSql == null) {
    		// This is probably from a client-side prepared
    		// statement
    	
    		int extractPosition = endOfQueryPacketPosition;
        
    	 	boolean truncated = false;

    		if (endOfQueryPacketPosition > getMaxQuerySizeToLog()) {
    		    extractPosition = getMaxQuerySizeToLog();
    		    truncated = true;
    		}

    		extractedSql = new String(queryPacket.getByteBuffer(), 5,
                (extractPosition - 5));

    		if (truncated) {
    			extractedSql += Messages.getString("MysqlIO.25"); //$NON-NLS-1$
    		}
    	}
    	
    	return extractedSql;
    	
    }
    
    protected static SQLException appendMessageToException(SQLException sqlEx,
    		String messageToAppend) {
    	String origMessage = sqlEx.getMessage();
		String sqlState = sqlEx.getSQLState();
		int vendorErrorCode = sqlEx.getErrorCode();
		
		StringBuffer messageBuf = new StringBuffer(origMessage.length() + messageToAppend.length());
		messageBuf.append(origMessage);
		messageBuf.append(messageToAppend);
		
		SQLException sqlExceptionWithNewMessage =  new SQLException(messageBuf.toString(), sqlState, vendorErrorCode);
		
		//
		// Try and maintain the original stack trace,
		// only works on JDK-1.4 and newer
		//

        try {
            // Have to do this with reflection, otherwise older JVMs croak
        	Method getStackTraceMethod = null;
            Method setStackTraceMethod = null;
            Object theStackTraceAsObject = null;

            Class stackTraceElementClass = Class.forName("java.lang.StackTraceElement");
            Class stackTraceElementArrayClass = Array.newInstance(stackTraceElementClass, new int[] {0}).getClass();
            
            
            getStackTraceMethod = Throwable.class.getMethod("getStackTrace",
                    new Class[] {});

            setStackTraceMethod = Throwable.class.getMethod("setStackTrace",
            		new Class[] {stackTraceElementArrayClass});

            if (getStackTraceMethod != null && setStackTraceMethod != null) {
            	theStackTraceAsObject = getStackTraceMethod.invoke(sqlEx, new Object[0]);
            	setStackTraceMethod.invoke(sqlExceptionWithNewMessage, new Object[] {theStackTraceAsObject});
            }   
        } catch (NoClassDefFoundError noClassDefFound) {
            
        } catch (NoSuchMethodException noSuchMethodEx) {
            
        } catch (Throwable catchAll) {
            
        }

		return sqlExceptionWithNewMessage;
    }

    /**
     * Used as a key for caching callable statements which (may) depend on
     * current catalog...In 5.0.x, they don't (currently), but stored
     * procedure names soon will,  so current catalog is a (hidden) component
     * of the name.
     */
    class CompoundCacheKey {
        String componentOne;
        String componentTwo;
        int hashCode;

        CompoundCacheKey(String partOne, String partTwo) {
            this.componentOne = partOne;
            this.componentTwo = partTwo;

            // Handle first component (in most cases, currentCatalog)
            // being NULL....
            this.hashCode = (((this.componentOne != null) ? this.componentOne : "")
                + this.componentTwo).hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj) {
            if (obj instanceof CompoundCacheKey) {
                CompoundCacheKey another = (CompoundCacheKey) obj;

                boolean firstPartEqual = false;

                if (this.componentOne == null) {
                    firstPartEqual = (another.componentOne == null);
                } else {
                    firstPartEqual = this.componentOne.equals(another.componentOne);
                }

                return (firstPartEqual
                && this.componentTwo.equals(another.componentTwo));
            }
                
            return false;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return this.hashCode;
        }
    }

    /**
     * Wrapper class for UltraDev CallableStatements that are really
     * PreparedStatments. Nice going, UltraDev developers.
     */
    class UltraDevWorkAround implements java.sql.CallableStatement {
        private java.sql.PreparedStatement delegate = null;

        UltraDevWorkAround(java.sql.PreparedStatement pstmt) {
            this.delegate = pstmt;
        }

        public void setArray(int p1, final java.sql.Array p2)
            throws SQLException {
            this.delegate.setArray(p1, p2);
        }

        public java.sql.Array getArray(int p1) throws SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getArray(String)
         */
        public java.sql.Array getArray(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setAsciiStream(int p1, final java.io.InputStream p2, int p3)
            throws SQLException {
            this.delegate.setAsciiStream(p1, p2, p3);
        }

        /**
         * @see CallableStatement#setAsciiStream(String, InputStream, int)
         */
        public void setAsciiStream(String arg0, InputStream arg1, int arg2)
            throws SQLException {
            throw new NotImplemented();
        }

        public void setBigDecimal(int p1, final java.math.BigDecimal p2)
            throws SQLException {
            this.delegate.setBigDecimal(p1, p2);
        }

        /**
         * @see CallableStatement#setBigDecimal(String, BigDecimal)
         */
        public void setBigDecimal(String arg0, BigDecimal arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        public java.math.BigDecimal getBigDecimal(int p1)
            throws SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * DOCUMENT ME!
         *
         * @param p1 DOCUMENT ME!
         * @param p2 DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         *
         * @throws SQLException DOCUMENT ME!
         *
         * @deprecated
         */
        public java.math.BigDecimal getBigDecimal(int p1, int p2)
            throws SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getBigDecimal(String)
         */
        public BigDecimal getBigDecimal(String arg0) throws SQLException {
            return null;
        }

        public void setBinaryStream(int p1, final java.io.InputStream p2, int p3)
            throws SQLException {
            this.delegate.setBinaryStream(p1, p2, p3);
        }

        /**
         * @see CallableStatement#setBinaryStream(String, InputStream, int)
         */
        public void setBinaryStream(String arg0, InputStream arg1, int arg2)
            throws SQLException {
            throw new NotImplemented();
        }

        public void setBlob(int p1, final java.sql.Blob p2)
            throws SQLException {
            this.delegate.setBlob(p1, p2);
        }

        public java.sql.Blob getBlob(int p1) throws SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getBlob(String)
         */
        public java.sql.Blob getBlob(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setBoolean(int p1, boolean p2) throws SQLException {
            this.delegate.setBoolean(p1, p2);
        }

        /**
         * @see CallableStatement#setBoolean(String, boolean)
         */
        public void setBoolean(String arg0, boolean arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        public boolean getBoolean(int p1) throws SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getBoolean(String)
         */
        public boolean getBoolean(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setByte(int p1, byte p2) throws SQLException {
            this.delegate.setByte(p1, p2);
        }

        /**
         * @see CallableStatement#setByte(String, byte)
         */
        public void setByte(String arg0, byte arg1) throws SQLException {
            throw new NotImplemented();
        }

        public byte getByte(int p1) throws SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getByte(String)
         */
        public byte getByte(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setBytes(int p1, byte[] p2) throws SQLException {
            this.delegate.setBytes(p1, p2);
        }

        /**
         * @see CallableStatement#setBytes(String, byte[])
         */
        public void setBytes(String arg0, byte[] arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        public byte[] getBytes(int p1) throws SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getBytes(String)
         */
        public byte[] getBytes(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setCharacterStream(int p1, final java.io.Reader p2, int p3)
            throws SQLException {
            this.delegate.setCharacterStream(p1, p2, p3);
        }

        /**
         * @see CallableStatement#setCharacterStream(String, Reader, int)
         */
        public void setCharacterStream(String arg0, Reader arg1, int arg2)
            throws SQLException {
            throw new NotImplemented();
        }

        public void setClob(int p1, final java.sql.Clob p2)
            throws SQLException {
            this.delegate.setClob(p1, p2);
        }

        public java.sql.Clob getClob(int p1) throws SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getClob(String)
         */
        public Clob getClob(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public java.sql.Connection getConnection() throws SQLException {
            return this.delegate.getConnection();
        }

        public void setCursorName(java.lang.String p1)
            throws SQLException {
            throw new SQLException("Not supported");
        }

        public void setDate(int p1, final java.sql.Date p2)
            throws SQLException {
            this.delegate.setDate(p1, p2);
        }

        public void setDate(int p1, final java.sql.Date p2,
            final java.util.Calendar p3) throws SQLException {
            this.delegate.setDate(p1, p2, p3);
        }

        /**
         * @see CallableStatement#setDate(String, Date, Calendar)
         */
        public void setDate(String arg0, Date arg1, Calendar arg2)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#setDate(String, Date)
         */
        public void setDate(String arg0, Date arg1) throws SQLException {
            throw new NotImplemented();
        }

        public java.sql.Date getDate(int p1) throws SQLException {
            throw new SQLException("Not supported");
        }

        public java.sql.Date getDate(int p1, final Calendar p2)
            throws SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getDate(String, Calendar)
         */
        public Date getDate(String arg0, Calendar arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#getDate(String)
         */
        public Date getDate(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setDouble(int p1, double p2) throws SQLException {
            this.delegate.setDouble(p1, p2);
        }

        /**
         * @see CallableStatement#setDouble(String, double)
         */
        public void setDouble(String arg0, double arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        public double getDouble(int p1) throws SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getDouble(String)
         */
        public double getDouble(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setEscapeProcessing(boolean p1) throws SQLException {
            this.delegate.setEscapeProcessing(p1);
        }

        public void setFetchDirection(int p1) throws SQLException {
            this.delegate.setFetchDirection(p1);
        }

        public int getFetchDirection() throws SQLException {
            return this.delegate.getFetchDirection();
        }

        public void setFetchSize(int p1) throws SQLException {
            this.delegate.setFetchSize(p1);
        }

        public int getFetchSize() throws java.sql.SQLException {
            return this.delegate.getFetchSize();
        }

        public void setFloat(int p1, float p2) throws SQLException {
            this.delegate.setFloat(p1, p2);
        }

        /**
         * @see CallableStatement#setFloat(String, float)
         */
        public void setFloat(String arg0, float arg1) throws SQLException {
            throw new NotImplemented();
        }

        public float getFloat(int p1) throws SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getFloat(String)
         */
        public float getFloat(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see Statement#getGeneratedKeys()
         */
        public java.sql.ResultSet getGeneratedKeys() throws SQLException {
            return this.delegate.getGeneratedKeys();
        }

        public void setInt(int p1, int p2) throws SQLException {
            this.delegate.setInt(p1, p2);
        }

        /**
         * @see CallableStatement#setInt(String, int)
         */
        public void setInt(String arg0, int arg1) throws SQLException {
            throw new NotImplemented();
        }

        public int getInt(int p1) throws SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getInt(String)
         */
        public int getInt(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setLong(int p1, long p2) throws SQLException {
            this.delegate.setLong(p1, p2);
        }

        /**
         * @see CallableStatement#setLong(String, long)
         */
        public void setLong(String arg0, long arg1) throws SQLException {
            throw new NotImplemented();
        }

        public long getLong(int p1) throws SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getLong(String)
         */
        public long getLong(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setMaxFieldSize(int p1) throws SQLException {
            this.delegate.setMaxFieldSize(p1);
        }

        public int getMaxFieldSize() throws SQLException {
            return this.delegate.getMaxFieldSize();
        }

        public void setMaxRows(int p1) throws SQLException {
            this.delegate.setMaxRows(p1);
        }

        public int getMaxRows() throws SQLException {
            return this.delegate.getMaxRows();
        }

        public java.sql.ResultSetMetaData getMetaData()
            throws SQLException {
            throw new SQLException("Not supported");
        }

        public boolean getMoreResults() throws SQLException {
            return this.delegate.getMoreResults();
        }

        /**
         * @see Statement#getMoreResults(int)
         */
        public boolean getMoreResults(int arg0) throws SQLException {
            return this.delegate.getMoreResults();
        }

        public void setNull(int p1, int p2) throws SQLException {
            this.delegate.setNull(p1, p2);
        }

        public void setNull(int p1, int p2, java.lang.String p3)
            throws SQLException {
            this.delegate.setNull(p1, p2, p3);
        }

        /**
         * @see CallableStatement#setNull(String, int, String)
         */
        public void setNull(String arg0, int arg1, String arg2)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#setNull(String, int)
         */
        public void setNull(String arg0, int arg1) throws SQLException {
            throw new NotImplemented();
        }

        public void setObject(int p1, final java.lang.Object p2)
            throws SQLException {
            this.delegate.setObject(p1, p2);
        }

        public void setObject(int p1, final java.lang.Object p2, int p3)
            throws SQLException {
            this.delegate.setObject(p1, p2, p3);
        }

        public void setObject(int p1, final java.lang.Object p2, int p3, int p4)
            throws SQLException {
            this.delegate.setObject(p1, p2, p3, p4);
        }

        /**
         * @see CallableStatement#setObject(String, Object, int, int)
         */
        public void setObject(String arg0, Object arg1, int arg2, int arg3)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#setObject(String, Object, int)
         */
        public void setObject(String arg0, Object arg1, int arg2)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#setObject(String, Object)
         */
        public void setObject(String arg0, Object arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        public java.lang.Object getObject(int p1) throws SQLException {
            throw new SQLException("Not supported");
        }

        public java.lang.Object getObject(int p1, final java.util.Map p2)
            throws SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getObject(String, Map)
         */
        public Object getObject(String arg0, Map arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#getObject(String)
         */
        public Object getObject(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see PreparedStatement#getParameterMetaData()
         */
        public ParameterMetaData getParameterMetaData()
            throws SQLException {
            return this.delegate.getParameterMetaData();
        }

        public void setQueryTimeout(int p1) throws SQLException {
            throw new SQLException("Not supported");
        }

        public int getQueryTimeout() throws SQLException {
            return this.delegate.getQueryTimeout();
        }

        public void setRef(int p1, final Ref p2) throws SQLException {
            throw new SQLException("Not supported");
        }

        public java.sql.Ref getRef(int p1) throws SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getRef(String)
         */
        public Ref getRef(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public java.sql.ResultSet getResultSet() throws SQLException {
            return this.delegate.getResultSet();
        }

        public int getResultSetConcurrency() throws SQLException {
            return this.delegate.getResultSetConcurrency();
        }

        /**
         * @see Statement#getResultSetHoldability()
         */
        public int getResultSetHoldability() throws SQLException {
            return this.delegate.getResultSetHoldability();
        }

        public int getResultSetType() throws SQLException {
            return this.delegate.getResultSetType();
        }

        public void setShort(int p1, short p2) throws SQLException {
            this.delegate.setShort(p1, p2);
        }

        /**
         * @see CallableStatement#setShort(String, short)
         */
        public void setShort(String arg0, short arg1) throws SQLException {
            throw new NotImplemented();
        }

        public short getShort(int p1) throws SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getShort(String)
         */
        public short getShort(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setString(int p1, java.lang.String p2)
            throws java.sql.SQLException {
            this.delegate.setString(p1, p2);
        }

        /**
         * @see CallableStatement#setString(String, String)
         */
        public void setString(String arg0, String arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        public java.lang.String getString(int p1) throws SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getString(String)
         */
        public String getString(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setTime(int p1, final java.sql.Time p2)
            throws SQLException {
            this.delegate.setTime(p1, p2);
        }

        public void setTime(int p1, final java.sql.Time p2,
            final java.util.Calendar p3) throws SQLException {
            this.delegate.setTime(p1, p2, p3);
        }

        /**
         * @see CallableStatement#setTime(String, Time, Calendar)
         */
        public void setTime(String arg0, Time arg1, Calendar arg2)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#setTime(String, Time)
         */
        public void setTime(String arg0, Time arg1) throws SQLException {
            throw new NotImplemented();
        }

        public java.sql.Time getTime(int p1) throws SQLException {
            throw new SQLException("Not supported");
        }

        public java.sql.Time getTime(int p1, final java.util.Calendar p2)
            throws SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getTime(String, Calendar)
         */
        public Time getTime(String arg0, Calendar arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#getTime(String)
         */
        public Time getTime(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        public void setTimestamp(int p1, final java.sql.Timestamp p2)
            throws SQLException {
            this.delegate.setTimestamp(p1, p2);
        }

        public void setTimestamp(int p1, final java.sql.Timestamp p2,
            final java.util.Calendar p3) throws SQLException {
            this.delegate.setTimestamp(p1, p2, p3);
        }

        /**
         * @see CallableStatement#setTimestamp(String, Timestamp, Calendar)
         */
        public void setTimestamp(String arg0, Timestamp arg1, Calendar arg2)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#setTimestamp(String, Timestamp)
         */
        public void setTimestamp(String arg0, Timestamp arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        public java.sql.Timestamp getTimestamp(int p1)
            throws SQLException {
            throw new SQLException("Not supported");
        }

        public java.sql.Timestamp getTimestamp(int p1,
            final java.util.Calendar p2) throws SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#getTimestamp(String, Calendar)
         */
        public Timestamp getTimestamp(String arg0, Calendar arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#getTimestamp(String)
         */
        public Timestamp getTimestamp(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#setURL(String, URL)
         */
        public void setURL(String arg0, URL arg1) throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see PreparedStatement#setURL(int, URL)
         */
        public void setURL(int arg0, URL arg1) throws SQLException {
            this.delegate.setURL(arg0, arg1);
        }

        /**
         * @see CallableStatement#getURL(int)
         */
        public URL getURL(int arg0) throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#getURL(String)
         */
        public URL getURL(String arg0) throws SQLException {
            throw new NotImplemented();
        }

        /**
         * DOCUMENT ME!
         *
         * @param p1 DOCUMENT ME!
         * @param p2 DOCUMENT ME!
         * @param p3 DOCUMENT ME!
         *
         * @throws SQLException DOCUMENT ME!
         *
         * @deprecated
         */
        public void setUnicodeStream(int p1, final java.io.InputStream p2,
            int p3) throws SQLException {
            this.delegate.setUnicodeStream(p1, p2, p3);
        }

        public int getUpdateCount() throws SQLException {
            return this.delegate.getUpdateCount();
        }

        public java.sql.SQLWarning getWarnings() throws SQLException {
            return this.delegate.getWarnings();
        }

        public void addBatch() throws SQLException {
            this.delegate.addBatch();
        }

        public void addBatch(java.lang.String p1) throws SQLException {
            this.delegate.addBatch(p1);
        }

        public void cancel() throws SQLException {
            this.delegate.cancel();
        }

        public void clearBatch() throws SQLException {
            this.delegate.clearBatch();
        }

        public void clearParameters() throws SQLException {
            this.delegate.clearParameters();
        }

        public void clearWarnings() throws SQLException {
            this.delegate.clearWarnings();
        }

        public void close() throws SQLException {
            this.delegate.close();
        }

        public boolean execute() throws SQLException {
            return this.delegate.execute();
        }

        public boolean execute(java.lang.String p1) throws SQLException {
            return this.delegate.execute(p1);
        }

        /**
         * @see Statement#execute(String, int)
         */
        public boolean execute(String arg0, int arg1) throws SQLException {
            return this.delegate.execute(arg0, arg1);
        }

        /**
         * @see Statement#execute(String, int[])
         */
        public boolean execute(String arg0, int[] arg1)
            throws SQLException {
            return this.delegate.execute(arg0, arg1);
        }

        /**
         * @see Statement#execute(String, String[])
         */
        public boolean execute(String arg0, String[] arg1)
            throws SQLException {
            return this.delegate.execute(arg0, arg1);
        }

        public int[] executeBatch() throws SQLException {
            return this.delegate.executeBatch();
        }

        public java.sql.ResultSet executeQuery() throws SQLException {
            return this.delegate.executeQuery();
        }

        public java.sql.ResultSet executeQuery(java.lang.String p1)
            throws SQLException {
            return this.delegate.executeQuery(p1);
        }

        public int executeUpdate() throws SQLException {
            return this.delegate.executeUpdate();
        }

        public int executeUpdate(java.lang.String p1) throws SQLException {
            return this.delegate.executeUpdate(p1);
        }

        /**
         * @see Statement#executeUpdate(String, int)
         */
        public int executeUpdate(String arg0, int arg1)
            throws SQLException {
            return this.delegate.executeUpdate(arg0, arg1);
        }

        /**
         * @see Statement#executeUpdate(String, int[])
         */
        public int executeUpdate(String arg0, int[] arg1)
            throws SQLException {
            return this.delegate.executeUpdate(arg0, arg1);
        }

        /**
         * @see Statement#executeUpdate(String, String[])
         */
        public int executeUpdate(String arg0, String[] arg1)
            throws SQLException {
            return this.delegate.executeUpdate(arg0, arg1);
        }

        public void registerOutParameter(int p1, int p2)
            throws SQLException {
            throw new SQLException("Not supported");
        }

        public void registerOutParameter(int p1, int p2, int p3)
            throws SQLException {
            throw new SQLException("Not supported");
        }

        public void registerOutParameter(int p1, int p2, java.lang.String p3)
            throws SQLException {
            throw new SQLException("Not supported");
        }

        /**
         * @see CallableStatement#registerOutParameter(String, int, int)
         */
        public void registerOutParameter(String arg0, int arg1, int arg2)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#registerOutParameter(String, int, String)
         */
        public void registerOutParameter(String arg0, int arg1, String arg2)
            throws SQLException {
            throw new NotImplemented();
        }

        /**
         * @see CallableStatement#registerOutParameter(String, int)
         */
        public void registerOutParameter(String arg0, int arg1)
            throws SQLException {
            throw new NotImplemented();
        }

        public boolean wasNull() throws SQLException {
            throw new SQLException("Not supported");
        }
    }

	/**
	 * Returns the server's character set
	 * 
	 * @return the server's character set.
	 */
	protected String getServerCharacterEncoding() {
		return (String)this.serverVariables.get("character_set");
	}
	
	protected java.sql.Statement getMetadataSafeStatement() throws SQLException {
		java.sql.Statement stmt = createStatement();
		
		if (stmt.getMaxRows() != 0) {
			stmt.setMaxRows(0);
		}
		
		stmt.setEscapeProcessing(false);
		
		return stmt;
	}
	
	
	protected boolean isServerTzUTC() {
		return this.isServerTzUTC;
	}
	
	protected Buffer fillSendPacket(PreparedStatement pstmt,
			byte[][] staticSqlStrings,
			byte[][] batchedParameterStrings,
			InputStream[] batchedParameterStreams, 
			boolean[] batchedIsStream,
			int[] batchedStreamLengths) throws SQLException {
		checkClosedConnection();
		
		Buffer sendPacket = this.io.getSharedSendPacket();
		
		sendPacket.clear();
		
		sendPacket.writeByte((byte) MysqlDefs.QUERY);
		
		boolean useStreamLengths = getUseStreamLengthsInPrepStmts();
		
		//
		// Try and get this allocation as close as possible
		// for BLOBs
		//
		int ensurePacketSize = 0;
		
		for (int i = 0; i < batchedParameterStrings.length; i++) {
			if (batchedIsStream[i] && useStreamLengths) {
				ensurePacketSize += batchedStreamLengths[i];
			}
		}
		
		if (ensurePacketSize != 0) {
			sendPacket.ensureCapacity(ensurePacketSize);
		}
		
		for (int i = 0; i < batchedParameterStrings.length; i++) {
			if ((batchedParameterStrings[i] == null)
					&& (batchedParameterStreams[i] == null)) {
				throw new SQLException(Messages.getString("PreparedStatement.40") //$NON-NLS-1$
						+ (i + 1), SQLError.SQL_STATE_WRONG_NO_OF_PARAMETERS);
			}
			
			sendPacket.writeBytesNoNull(staticSqlStrings[i]);
			
			if (batchedIsStream[i]) {
				pstmt.streamToBytes(sendPacket, batchedParameterStreams[i], true,
						batchedStreamLengths[i], useStreamLengths);
			} else {
				sendPacket.writeBytesNoNull(batchedParameterStrings[i]);
			}
		}
		
		sendPacket.writeBytesNoNull(staticSqlStrings[batchedParameterStrings.length]);
		
		return sendPacket;
	}
	
	protected void closeServerPreparedStatement(long serverStatementId)
		throws SQLException {
		checkClosed();
		
		synchronized (getMutex()) {
			Buffer packet = this.io.getSharedSendPacket();
			
			packet.writeByte((byte) MysqlDefs.COM_CLOSE_STATEMENT);
			packet.writeLong(serverStatementId);
			
			this.io.sendCommand(MysqlDefs.COM_CLOSE_STATEMENT, null, packet,
					true, null);
		}
	}

	/**
	 * Tells the server to execute this prepared statement with the current
	 * parameter bindings.
	 * <pre>
	 *
	 *   -   Server gets the command 'COM_EXECUTE' to execute the
	 *       previously         prepared query. If there is any param markers;
	 * then client will send the data in the following format:
	 *
	 * [COM_EXECUTE:1]
	 * [STMT_ID:4]
	 * [NULL_BITS:(param_count+7)/8)]
	 * [TYPES_SUPPLIED_BY_CLIENT(0/1):1]
	 * [[length]data]
	 * [[length]data] .. [[length]data].
	 *
	 * (Note: Except for string/binary types; all other types will not be
	 * supplied with length field)
	 *
	 * </pre>
	 *
	 * @param maxRowsToRetrieve DOCUMENT ME!
	 * @param createStreamingResultSet DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws SQLException
	 */
	protected com.mysql.jdbc.ResultSet serverExecute(ServerPreparedStatement pstmt,
			BindValue[] parameterBindings, 
			int fieldCount,
			int maxRowsToRetrieve,
			boolean sendTypesToServer,
			boolean createStreamingResultSet) throws SQLException {
		
		try {
			synchronized (getMutex()) {
				checkClosed();
				
				Buffer packet = this.io.getSharedSendPacket();
				
				packet.clear();
				packet.writeByte((byte) MysqlDefs.COM_EXECUTE);
				packet.writeLong(pstmt.getServerStatementId());
				
				boolean usingCursor = false;
				
				if (versionMeetsMinimum(4, 1, 2)) {
					// we only create cursor-backed result sets if 
					// a) The server supports it
					// b) We know it is forward-only (note this doesn't
					//    preclude updatable result sets)
					// c) The user has set a fetch size
					if (versionMeetsMinimum(5, 0, 2) && 
							pstmt.getResultSetType() == ResultSet.TYPE_FORWARD_ONLY &&
							pstmt.getFetchSize() > 0) {
						packet.writeByte((byte) MysqlDefs.OPEN_CURSOR_FLAG);
						usingCursor = true;
					} else {
						packet.writeByte((byte) 0); // placeholder for flags
					}
					
					packet.writeLong(1); // placeholder for parameter iterations
				}
				
				int parameterCount = parameterBindings.length;
				
				/* Reserve place for null-marker bytes */
				int nullCount = (parameterCount + 7) / 8;
				
				//if (mysql.versionMeetsMinimum(4, 1, 2)) {
				//	nullCount = (this.parameterCount + 9) / 8;
				//}
				int nullBitsPosition = packet.getPosition();
				
				for (int i = 0; i < nullCount; i++) {
					packet.writeByte((byte) 0);
				}
				
				byte[] nullBitsBuffer = new byte[nullCount];
	
				/* In case if buffers (type) altered, indicate to server */
				packet.writeByte(sendTypesToServer ? (byte) 1 : (byte) 0);
				
				if (sendTypesToServer) {
					/*
					 Store types of parameters in first in first package
					 that is sent to the server.
					 */
					for (int i = 0; i < parameterCount; i++) {
						packet.writeInt(parameterBindings[i].bufferType);
					}
				}
				
				//
				// store the parameter values
				//
				for (int i = 0; i < parameterCount; i++) {
					if (!parameterBindings[i].isLongData) {
						if (!parameterBindings[i].isNull) {
							storeBinding(pstmt, packet, parameterBindings[i]);
						} else {
							nullBitsBuffer[i / 8] |= (1 << (i & 7));
						}
					}
				}
				
				//
				// Go back and write the NULL flags
				// to the beginning of the packet
				//
				int endPosition = packet.getPosition();
				packet.setPosition(nullBitsPosition);
				packet.writeBytesNoNull(nullBitsBuffer);
				packet.setPosition(endPosition);
				
				long begin = 0;
				
				if (getProfileSql() ||
						getLogSlowQueries() ||
						getGatherPerformanceMetrics()) {
					begin = System.currentTimeMillis();
				}
				
				Buffer resultPacket = this.io.sendCommand(MysqlDefs.COM_EXECUTE,
						null, packet, false, null);
				
				if (getLogSlowQueries() ||
						getGatherPerformanceMetrics()) {
					long elapsedTime = System.currentTimeMillis() - begin;
					
					if (getLogSlowQueries() &&
							(elapsedTime > getSlowQueryThresholdMillis())) {
						String originalSql = pstmt.getOriginalSql();
						
						StringBuffer mesgBuf = new StringBuffer(48 +
								originalSql.length());
						mesgBuf.append(Messages.getString(
						"ServerPreparedStatement.15")); //$NON-NLS-1$
						mesgBuf.append(getSlowQueryThresholdMillis());
						mesgBuf.append(Messages.getString(
						"ServerPreparedStatement.16")); //$NON-NLS-1$
						mesgBuf.append(originalSql);
						
						getLog().logWarn(mesgBuf.toString());
						
						if (getExplainSlowQueries()) {
							String queryAsString = pstmt.asSql(true);
							
							this.io.explainSlowQuery(queryAsString.getBytes(),
									queryAsString);
						}
					}
					
					if (getGatherPerformanceMetrics()) {
						registerQueryExecutionTime(elapsedTime);
					}
				}
				
				incrementNumberOfPreparedExecutes();
				
				if (getProfileSql()) {
					this.eventSink = ProfileEventSink.getInstance(this);
					
					this.eventSink.consumeEvent(new ProfilerEvent(
							ProfilerEvent.TYPE_EXECUTE, "", pstmt.getCurrentCatalog(), //$NON-NLS-1$
							getId(), pstmt.getId(), -1,
							System.currentTimeMillis(),
							(int) (System.currentTimeMillis() - begin), null,
							new Throwable(), null));
				}
				
				com.mysql.jdbc.ResultSet rs = null;
				
				
				rs = this.io.readAllResults(pstmt,
						maxRowsToRetrieve, pstmt.getResultSetType(),
						pstmt.getResultSetConcurrency(), createStreamingResultSet,
						pstmt.getCurrentCatalog(), resultPacket, true, fieldCount,
						true);
				
				if (!createStreamingResultSet) {
					serverResetStatement(pstmt); // clear any long data...
				}
				
				return rs;
			}
		}  catch (SQLException sqlEx) {
            // don't wrap SQLExceptions
            if (getEnablePacketDebug()) {
                this.io.dumpPacketRingBuffer();
            }

            if (getDumpQueriesOnException()) {
                String extractedSql = pstmt.asSql();
                StringBuffer messageBuf = new StringBuffer(extractedSql.length() +
                        32);
                messageBuf.append(
                    "\n\nQuery being executed when exception was thrown:\n\n");
                messageBuf.append(extractedSql);

                sqlEx = appendMessageToException(sqlEx,
                        messageBuf.toString());
            }

           
            throw sqlEx;
        } catch (Exception ex) {
            if (getEnablePacketDebug()) {
               	this.io.dumpPacketRingBuffer();
            }

            SQLException sqlEx = new SQLException(ex.toString(),
                    SQLError.SQL_STATE_GENERAL_ERROR);

            if (getDumpQueriesOnException()) {
                String extractedSql = pstmt.asSql();
                StringBuffer messageBuf = new StringBuffer(extractedSql.length() +
                        32);
                messageBuf.append(
                    "\n\nQuery being executed when exception was thrown:\n\n");
                messageBuf.append(extractedSql);

                sqlEx = appendMessageToException(sqlEx,
                        messageBuf.toString());
            }

            throw sqlEx;
        }
	}

	/**
	 * Sends stream-type data parameters to the server.
	 * <pre>
	 * Long data handling:
	 *
	 * - Server gets the long data in pieces with command type 'COM_LONG_DATA'.
	 * - The packet recieved will have the format as:
	 *   [COM_LONG_DATA:     1][STMT_ID:4][parameter_number:2][type:2][data]
	 * - Checks if the type is specified by client, and if yes reads the type,
	 *   and  stores the data in that format.
	 * - It's up to the client to check for read data ended. The server doesn't
	 *   care;  and also server doesn't notify to the client that it got the
	 *   data  or not; if there is any error; then during execute; the error
	 *   will  be returned
	 * </pre>
	 *
	 * @param parameterIndex DOCUMENT ME!
	 * @param longData DOCUMENT ME!
	 *
	 * @throws SQLException if an error occurs.
	 */
	protected void serverLongData(ServerPreparedStatement pstmt, 
			int parameterIndex, 
			BindValue longData)
	    throws SQLException {
	    synchronized (getMutex()) {
	    	checkClosed();
	    	
	        Buffer packet = this.io.getSharedSendPacket();
	
	        packet.clear();
	        packet.writeByte((byte) MysqlDefs.COM_LONG_DATA);
	        packet.writeLong(pstmt.getServerStatementId());
	        packet.writeInt((parameterIndex - 1));
	
	        Object value = longData.value;
	
	        if (value instanceof byte[]) {
	            packet.writeBytesNoNull((byte[]) longData.value);
	        } else if (value instanceof InputStream) {
	            storeStream(packet, (InputStream) value);
	        } else if (value instanceof java.sql.Blob) {
	            storeStream(packet, ((java.sql.Blob) value).getBinaryStream());
	        } else if (value instanceof Reader) {
	            storeReader(packet, (Reader) value);
	        } else {
	            throw new SQLException(Messages.getString(
	                    "ServerPreparedStatement.18") //$NON-NLS-1$
	                 +value.getClass().getName() + "'", //$NON-NLS-1$
	                SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
	        }
	
	        this.io.sendCommand(MysqlDefs.COM_LONG_DATA, null, packet, true, null);
	    }
	}

	protected ServerPreparedStatement serverPrepare(ServerPreparedStatement existingStatement, String sql) throws SQLException {
		
		synchronized (this.getMutex()) {
			try {
				Statement.checkNullOrEmptyQuery(sql);
				
				long begin = 0;
				boolean isLoadDataQuery = false;
				
				if (StringUtils.startsWithIgnoreCaseAndWs(sql, "LOAD DATA")) { //$NON-NLS-1$
					isLoadDataQuery = true;
				} else {
					isLoadDataQuery = false;
				}
				
				if (getProfileSql()) {
					begin = System.currentTimeMillis();
				}
				
				String characterEncoding = null;
				String connectionEncoding = getEncoding();
				
				if (!isLoadDataQuery && getUseUnicode() &&
						(connectionEncoding != null)) {
					characterEncoding = connectionEncoding;
				}
				
				Buffer prepareResultPacket = this.io.sendCommand(MysqlDefs.COM_PREPARE,
						sql, null, false, characterEncoding);
				
				if (versionMeetsMinimum(4, 1, 1)) {
					// 4.1.1 and newer use the first byte
					// as an 'ok' or 'error' flag, so move
					// the buffer pointer past it to
					// start reading the statement id.
					prepareResultPacket.setPosition(1);
				} else {
					// 4.1.0 doesn't use the first byte as an 
					// 'ok' or 'error' flag
					prepareResultPacket.setPosition(0);
				}
				
				long serverStatementId = prepareResultPacket.readLong();
				int fieldCount = prepareResultPacket.readInt();
				int parameterCount = prepareResultPacket.readInt();
				
				
				incrementNumberOfPrepares();
				
				if (getProfileSql()) {
					this.eventSink = ProfileEventSink.getInstance(this);
					
					this.eventSink.consumeEvent(new ProfilerEvent(
							ProfilerEvent.TYPE_PREPARE, "", getCatalog(), //$NON-NLS-1$
							getId(), -1, -1,
							System.currentTimeMillis(),
							(int) (System.currentTimeMillis() - begin), null,
							new Throwable(), sql));
				}
				
				Field[] parameterFields = null, resultFields = null;
				
				if (parameterCount > 0) {
					if (versionMeetsMinimum(4, 1, 2) &&
							!isVersion(5, 0, 0)) {
						parameterFields = new Field[parameterCount];
						
						Buffer metaDataPacket = this.io.readPacket();
						
						int i = 0;
						
						while (!metaDataPacket.isLastDataPacket() &&
								(i < parameterCount)) {
							parameterFields[i++] = this.io.unpackField(metaDataPacket,
									false);
							metaDataPacket = this.io.readPacket();
						}
					}
				}
				
				if (fieldCount > 0) {
					resultFields = new Field[fieldCount];
					
					Buffer fieldPacket = this.io.readPacket();
					
					int i = 0;
					
					// Read in the result set column information
					while (!fieldPacket.isLastDataPacket() &&
							(i < fieldCount)) {
						resultFields[i++] = this.io.unpackField(fieldPacket,
								false);
						fieldPacket = this.io.readPacket();
					}
				}
				
				if (existingStatement == null) {
					return new ServerPreparedStatement(this, 
						this.database,
						sql, 
						serverStatementId,
						parameterCount,
						fieldCount,
						parameterFields, 
						resultFields);
				} 
					
				existingStatement.setServersideState(true, 
					serverStatementId,
					parameterCount,
					fieldCount,
					parameterFields,
					resultFields);
					
				return existingStatement;

			} catch (SQLException sqlEx) {
				if (getDumpQueriesOnException()) {
					StringBuffer messageBuf = new StringBuffer(sql.length() +
							32);
					messageBuf.append(
					"\n\nQuery being prepared when exception was thrown:\n\n");
					messageBuf.append(sql);
					
					sqlEx = Connection.appendMessageToException(sqlEx,
							messageBuf.toString());
				}
				
				throw sqlEx;
			} finally {
				// Leave the I/O channel in a known state...there might be packets out there
				// that we're not interested in
				this.io.clearInputStream();
			}
		}
	}

	protected void serverResetStatement(ServerPreparedStatement pstmt) throws SQLException {
	    synchronized (getMutex()) {
	        
	        Buffer packet = this.io.getSharedSendPacket();
	
	        packet.clear();
	        packet.writeByte((byte) MysqlDefs.COM_RESET_STMT);
	        packet.writeLong(pstmt.getServerStatementId());
	
	        try {
	            this.io.sendCommand(MysqlDefs.COM_RESET_STMT, null, packet,
	                !versionMeetsMinimum(4, 1, 2), null);
	        } catch (SQLException sqlEx) {
	            throw sqlEx;
	        } catch (Exception ex) {
	            throw new SQLException(ex.toString(),
	                SQLError.SQL_STATE_GENERAL_ERROR);
	        } finally {
	            this.io.clearInputStream();
	        }
	    }
	}
	
	protected boolean isVersion(int major, int minor, int subminor) {
		return this.io.isVersion(major, minor, subminor);
	}

	/**
	 * Method storeBinding.
	 *
	 * @param packet
	 * @param bindValue
	 * @param mysql DOCUMENT ME!
	 *
	 * @throws SQLException DOCUMENT ME!
	 */
	protected void storeBinding(ServerPreparedStatement pstmt,
			Buffer packet, 
			BindValue bindValue)
	    throws SQLException {
	    try {
	        Object value = bindValue.value;
	
	        //
	        // Handle primitives first
	        //
	        switch (bindValue.bufferType) {
	
	        case MysqlDefs.FIELD_TYPE_TINY:
	            packet.writeByte(bindValue.byteBinding);
	            return;
	        case MysqlDefs.FIELD_TYPE_SHORT:
	            packet.ensureCapacity(2);
	            packet.writeInt(bindValue.shortBinding);
	            return;
	        case MysqlDefs.FIELD_TYPE_LONG:
	            packet.ensureCapacity(4);
	            packet.writeLong(bindValue.intBinding);
	            return;
	        case MysqlDefs.FIELD_TYPE_LONGLONG:
	            packet.ensureCapacity(8);
	            packet.writeLongLong(bindValue.longBinding);
	            return;
	        case MysqlDefs.FIELD_TYPE_FLOAT:
	            packet.ensureCapacity(4);
	            packet.writeFloat(bindValue.floatBinding);
	            return;
	        case MysqlDefs.FIELD_TYPE_DOUBLE:
	            packet.ensureCapacity(8);
	            packet.writeDouble(bindValue.doubleBinding);
	            return;
	        case MysqlDefs.FIELD_TYPE_TIME: 
	            storeTime(packet, (Time) value);
	        	return;
	        case MysqlDefs.FIELD_TYPE_DATE:
	        case MysqlDefs.FIELD_TYPE_DATETIME:
	        case MysqlDefs.FIELD_TYPE_TIMESTAMP:
	            storeDateTime(packet, (java.util.Date) value);
	        	return;
	        case MysqlDefs.FIELD_TYPE_VAR_STRING:
	        case MysqlDefs.FIELD_TYPE_STRING:
	        	if (!pstmt.isLoadDataQuery()) {
	                packet.writeLenString((String) value, pstmt.getCharEncoding(),
	                    getServerCharacterEncoding(),
	                    pstmt.getCharConverter(), parserKnowsUnicode());
	            } else {
	                packet.writeLenBytes(((String) value).getBytes());
	            }
	        	
	        	return;
	        }
	        
	        if (value instanceof byte[]) {
	            packet.writeLenBytes((byte[]) value);
	        }
	    } catch (UnsupportedEncodingException uEE) {
	        throw new SQLException(Messages.getString(
	                "ServerPreparedStatement.22") //$NON-NLS-1$
	             + getEncoding() + "'", //$NON-NLS-1$
	            SQLError.SQL_STATE_GENERAL_ERROR);
	    }
	}

	private void storeDateTime412AndOlder(Buffer intoBuf, java.util.Date dt)
	    throws SQLException {
	    // This is synchronized on the connection by callers, so it is
	    // safe to lazily-instantiate this...
	    if (this.dateTimeBindingCal == null) {
	    	this.dateTimeBindingCal = Calendar.getInstance();
	    }
	
	    this.dateTimeBindingCal.setTime(dt);
	
	    intoBuf.ensureCapacity(8);
	    intoBuf.writeByte((byte) 7); // length
	
	    int year = this.dateTimeBindingCal.get(Calendar.YEAR);
	    int month = this.dateTimeBindingCal.get(Calendar.MONTH) + 1;
	    int date = this.dateTimeBindingCal.get(Calendar.DATE);
	
	    intoBuf.writeInt(year);
	    intoBuf.writeByte((byte) month);
	    intoBuf.writeByte((byte) date);
	
	    if (dt instanceof java.sql.Date) {
	        intoBuf.writeByte((byte) 0);
	        intoBuf.writeByte((byte) 0);
	        intoBuf.writeByte((byte) 0);
	    } else {
	        intoBuf.writeByte((byte) this.dateTimeBindingCal.get(
	                Calendar.HOUR_OF_DAY));
	        intoBuf.writeByte((byte) this.dateTimeBindingCal.get(Calendar.MINUTE));
	        intoBuf.writeByte((byte) this.dateTimeBindingCal.get(Calendar.SECOND));
	    }
	}

	private void storeDateTime(Buffer intoBuf, java.util.Date dt)
	    throws SQLException {
	    if (versionMeetsMinimum(4, 1, 3)) {
	        storeDateTime413AndNewer(intoBuf, dt);
	    } else {
	        storeDateTime412AndOlder(intoBuf, dt);
	    }
	}

	private void storeDateTime413AndNewer(Buffer intoBuf, java.util.Date dt)
	    throws SQLException {
	    // This is synchronized on the connection by callers, so it is
	    // safe to lazily-instantiate this...
	    if (this.dateTimeBindingCal == null) {
	    	this.dateTimeBindingCal = Calendar.getInstance();
	    }
	
	    this.dateTimeBindingCal.setTime(dt);
	
	    byte length = (byte) 7;
	
	    intoBuf.ensureCapacity(length);
	
	    if (dt instanceof java.sql.Timestamp) {
	        length = (byte) 11;
	    }
	
	    intoBuf.writeByte(length); // length
	
	    int year = this.dateTimeBindingCal.get(Calendar.YEAR);
	    int month = this.dateTimeBindingCal.get(Calendar.MONTH) + 1;
	    int date = this.dateTimeBindingCal.get(Calendar.DATE);
	
	    intoBuf.writeInt(year);
	    intoBuf.writeByte((byte) month);
	    intoBuf.writeByte((byte) date);
	
	    if (dt instanceof java.sql.Date) {
	        intoBuf.writeByte((byte) 0);
	        intoBuf.writeByte((byte) 0);
	        intoBuf.writeByte((byte) 0);
	    } else {
	        intoBuf.writeByte((byte) this.dateTimeBindingCal.get(
	                Calendar.HOUR_OF_DAY));
	        intoBuf.writeByte((byte) this.dateTimeBindingCal.get(Calendar.MINUTE));
	        intoBuf.writeByte((byte) this.dateTimeBindingCal.get(Calendar.SECOND));
	    }
	
	    if (length == 11) {
	        intoBuf.writeLong(((java.sql.Timestamp) dt).getNanos());
	    }
	}

	private void storeTime(Buffer intoBuf, Time tm)
    	throws SQLException {
	    intoBuf.ensureCapacity(9);
	    intoBuf.writeByte((byte) 8); // length
	    intoBuf.writeByte((byte) 0); // neg flag
	    intoBuf.writeLong(0); // tm->day, not used
	
	    //	  This is synchronized on the connection by callers, so it is
	    // safe to lazily-instantiate this...
	    if (this.dateTimeBindingCal == null) {
	    	this.dateTimeBindingCal = Calendar.getInstance();
	    }
	
	    this.dateTimeBindingCal.setTime(tm);

	    intoBuf.writeByte((byte) dateTimeBindingCal.get(Calendar.HOUR_OF_DAY));
	    intoBuf.writeByte((byte) dateTimeBindingCal.get(Calendar.MINUTE));
	    intoBuf.writeByte((byte) dateTimeBindingCal.get(Calendar.SECOND));
	
	    //intoBuf.writeLongInt(0); // tm-second_part
	}
	
	//
	// TO DO: Investigate using NIO to do this faster
	//
	private void storeReader(Buffer packet, Reader inStream)
	    throws SQLException {
	    char[] buf = new char[4096];
	    StringBuffer valueAsString = new StringBuffer();
	
	    int numRead = 0;
	
	    try {
	        while ((numRead = inStream.read(buf)) != -1) {
	            valueAsString.append(buf, 0, numRead);
	        }
	    } catch (IOException ioEx) {
	        throw new SQLException(Messages.getString(
	                "ServerPreparedStatement.24") //$NON-NLS-1$
	             +ioEx.toString(), SQLError.SQL_STATE_GENERAL_ERROR);
	    } finally {
	        if (inStream != null) {
	            try {
	                inStream.close();
	            } catch (IOException ioEx) {
	                ; // ignore
	            }
	        }
	    }
	
	    byte[] valueAsBytes = StringUtils.getBytes(valueAsString.toString(),
	            getEncoding(),
	            getServerCharacterEncoding(),
	            parserKnowsUnicode());
	
	    packet.writeBytesNoNull(valueAsBytes);
	}

	private void storeStream(Buffer packet, InputStream inStream)
	    throws SQLException {
	    byte[] buf = new byte[4096];
	
	    int numRead = 0;
	
	    try {
	        while ((numRead = inStream.read(buf)) != -1) {
	            packet.writeBytesNoNull(buf, 0, numRead);
	        }
	    } catch (IOException ioEx) {
	        throw new SQLException(Messages.getString(
	                "ServerPreparedStatement.25") //$NON-NLS-1$
	             +ioEx.toString(), SQLError.SQL_STATE_GENERAL_ERROR);
	    } finally {
	        if (inStream != null) {
	            try {
	                inStream.close();
	            } catch (IOException ioEx) {
	                ; // ignore
	            }
	        }
	    }
	}

	/**
	 * @return
	 */
	protected boolean hasLongColumnInfo() {
		return this.io.hasLongColumnInfo();
	}

    public String getDatabase() {
        return this.database;
    }
    
    //
    // Note, proposed JDBC-4.0 functionality
    //
    
    private boolean isAborted = false;
    
    public void abort() throws SQLException {
    	SQLException caughtWhileClosing = null;
    	
    	if (!this.isClosed) {
    		try {
    			this.io.forceClose();
    		} catch (Exception ex) {
    			caughtWhileClosing = new SQLException(ex.toString());
    		} finally {
    			this.isAborted = true;
    		}
    	}
    	
    	if (caughtWhileClosing != null) {
    		throw caughtWhileClosing;
    	}
    }
}

