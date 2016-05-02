/*
 * Copyright 2003-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.rowset;

import java.sql.*;
import javax.sql.*;
import java.io.*;
import java.math.*;
import java.util.*;
import java.text.*;

import javax.sql.rowset.*;
import javax.sql.rowset.spi.*;
import javax.sql.rowset.serial.*;
import com.sun.rowset.internal.*;
import com.sun.rowset.providers.*;

/**
 * The standard implementation of the <code>CachedRowSet</code> interface.
 *
 * See interface defintion for full behaviour and implementation requirements.
 * This reference implementation has made provision for a one-to-one write back
 * facility and it is curremtly be possible to change the peristence provider
 * during the life-time of any CachedRowSetImpl.
 *
 * @author Jonathan Bruce, Amit Handa
 */

public class CachedRowSetImpl extends BaseRowSet implements RowSet, RowSetInternal, Serializable, Cloneable, CachedRowSet {

    /**
     * The <code>SyncProvider</code> used by the CachedRowSet
     */
    private SyncProvider provider;

    /**
     * The <code>RowSetReaderImpl</code> object that is the reader
     * for this rowset.  The method <code>execute</code> uses this
     * reader as part of its implementation.
     * @serial
     */
    private RowSetReader rowSetReader;

    /**
     * The <code>RowSetWriterImpl</code> object that is the writer
     * for this rowset.  The method <code>acceptChanges</code> uses
     * this writer as part of its implementation.
     * @serial
     */
    private RowSetWriter rowSetWriter;

    /**
     * The <code>Connection</code> object that connects with this
     * <code>CachedRowSetImpl</code> object's current underlying data source.
     */
    private transient Connection conn;

    /**
     * The <code>ResultSetMetaData</code> object that contains information
     * about the columns in the <code>ResultSet</code> object that is the
     * current source of data for this <code>CachedRowSetImpl</code> object.
     */
    private transient ResultSetMetaData RSMD;

    /**
     * The <code>RowSetMetaData</code> object that contains information about
     * the columns in this <code>CachedRowSetImpl</code> object.
     * @serial
     */
    private RowSetMetaDataImpl RowSetMD;

    // Properties of this RowSet

    /**
     * An array containing the columns in this <code>CachedRowSetImpl</code>
     * object that form a unique identifier for a row. This array
     * is used by the writer.
     * @serial
     */
    private int keyCols[];

    /**
     * The name of the table in the underlying database to which updates
     * should be written.  This name is needed because most drivers
     * do not return this information in a <code>ResultSetMetaData</code>
     * object.
     * @serial
     */
    private String tableName;


    /**
     * A <code>Vector</code> object containing the <code>Row</code>
     * objects that comprise  this <code>CachedRowSetImpl</code> object.
     * @serial
     */
    private Vector rvh;
    /**
     * The current postion of the cursor in this <code>CachedRowSetImpl</code>
     * object.
     * @serial
     */
    private int cursorPos;

    /**
     * The current postion of the cursor in this <code>CachedRowSetImpl</code>
     * object not counting rows that have been deleted, if any.
     * <P>
     * For example, suppose that the cursor is on the last row of a rowset
     * that started with five rows and subsequently had the second and third
     * rows deleted. The <code>absolutePos</code> would be <code>3</code>,
     * whereas the <code>cursorPos</code> would be <code>5</code>.
     * @serial
     */
    private int absolutePos;

    /**
     * The number of deleted rows currently in this <code>CachedRowSetImpl</code>
     * object.
     * @serial
     */
    private int numDeleted;

    /**
     * The total number of rows currently in this <code>CachedRowSetImpl</code>
     * object.
     * @serial
     */
    private int numRows;

    /**
     * A special row used for constructing a new row. A new
     * row is constructed by using <code>ResultSet.updateXXX</code>
     * methods to insert column values into the insert row.
     * @serial
     */
    private InsertRow insertRow;

    /**
     * A <code>boolean</code> indicating whether the cursor is
     * currently on the insert row.
     * @serial
     */
    private boolean onInsertRow;

    /**
     * The field that temporarily holds the last position of the
     * cursor before it moved to the insert row, thus preserving
     * the number of the current row to which the cursor may return.
     * @serial
     */
    private int currentRow;

    /**
     * A <code>boolean</code> indicating whether the last value
     * returned was an SQL <code>NULL</code>.
     * @serial
     */
    private boolean lastValueNull;

    /**
     * A <code>SQLWarning</code> which logs on the warnings
     */
    private SQLWarning sqlwarn;

    /**
     * Used to track match column for JoinRowSet consumption
     */
    private String strMatchColumn ="";

    /**
     * Used to track match column for JoinRowSet consumption
     */
    private int iMatchColumn = -1;

    /**
     * A <code>RowSetWarning</code> which logs on the warnings
     */
    private RowSetWarning rowsetWarning;

    /**
     * The default SyncProvider for the RI CachedRowSetImpl
     */
    private String DEFAULT_SYNC_PROVIDER = "com.sun.rowset.providers.RIOptimisticProvider";

    /**
     * The boolean variable indicating locatorsUpdateValue
     */
    private boolean dbmslocatorsUpdateCopy;

    /**
     * The <code>ResultSet</code> object that is used to maintain the data when
     * a ResultSet and start position are passed as parameters to the populate function
     */
    private transient ResultSet resultSet;

    /**
     * The integer value indicating the end position in the ResultSetwhere the picking
     * up of rows for populating a CachedRowSet object was left off.
     */
    private int endPos;

    /**
     * The integer value indicating the end position in the ResultSetwhere the picking
     * up of rows for populating a CachedRowSet object was left off.
     */
    private int prevEndPos;

    /**
     * The integer value indicating the position in the ResultSet, to populate the
     * CachedRowSet object.
     */
    private int startPos;

    /**
     * The integer value indicating the positon from where the page prior to this
     * was populated.
     */
    private int startPrev;

    /**
     * The integer value indicating size of the page.
     */
    private int pageSize;

    /**
     * The integer value indicating number of rows that have been processed so far.
     * Used for checking whether maxRows has been reached or not.
     */
    private int maxRowsreached;
    /**
     * The boolean value when true signifies that pages are still to follow and a
     * false value indicates that this is the last page.
     */
    private boolean pagenotend = true;

    /**
     * The boolean value indicating whether this is the first page or not.
     */
    private boolean onFirstPage;

    /**
     * The boolean value indicating whether this is the last page or not.
     */
    private boolean onLastPage;

    /**
     * The integer value indicating how many times the populate function has been called.
     */
    private int populatecallcount;

    /**
     * The integer value indicating the total number of rows to be processed in the
     * ResultSet object passed to the populate function.
     */
    private int totalRows;

    /**
     * The boolean value indicating how the CahedRowSet object has been populated for
     * paging purpose. True indicates that connection parameter is passed.
     */
    private boolean callWithCon;

    /**
     * CachedRowSet reader object to read the data from the ResultSet when a connection
     * parameter is passed to populate the CachedRowSet object for paging.
     */
    private CachedRowSetReader crsReader;

    /**
     * The Vector holding the Match Columns
     */
       private Vector iMatchColumns;

    /**
     * The Vector that will hold the Match Column names.
     */
       private Vector strMatchColumns;

    /**
     * Trigger that indicates whether the active SyncProvider is exposes the
     * additional TransactionalWriter method
     */
    private boolean tXWriter = false;

    /**
     * The field object for a transactional RowSet writer
     */
    private TransactionalWriter tWriter = null;

    protected transient JdbcRowSetResourceBundle resBundle;

    private boolean updateOnInsert;



    /**
     * Constructs a new default <code>CachedRowSetImpl</code> object with
     * the capacity to hold 100 rows. This new object has no metadata
     * and has the following default values:
     * <pre>
     *     onInsertRow = false
     *     insertRow = null
     *     cursorPos = 0
     *     numRows = 0
     *     showDeleted = false
     *     queryTimeout = 0
     *     maxRows = 0
     *     maxFieldSize = 0
     *     rowSetType = ResultSet.TYPE_SCROLL_INSENSITIVE
     *     concurrency = ResultSet.CONCUR_UPDATABLE
     *     readOnly = false
     *     isolation = Connection.TRANSACTION_READ_COMMITTED
     *     escapeProcessing = true
     *     onInsertRow = false
     *     insertRow = null
     *     cursorPos = 0
     *     absolutePos = 0
     *     numRows = 0
     * </pre>
     * A <code>CachedRowSetImpl</code> object is configured to use the default
     * <code>RIOptimisticProvider</code> implementation to provide connectivity
     * and synchronization capabilities to the set data source.
     * <P>
     * @throws SQLException if an error occurs
     */
    public CachedRowSetImpl() throws SQLException {

        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }

        // set the Reader, this maybe overridden latter
        provider =
        (SyncProvider)SyncFactory.getInstance(DEFAULT_SYNC_PROVIDER);

        if (!(provider instanceof RIOptimisticProvider)) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidp").toString());
        }

        rowSetReader = (CachedRowSetReader)provider.getRowSetReader();
        rowSetWriter = (CachedRowSetWriter)provider.getRowSetWriter();

        // allocate the parameters collection
        initParams();

        initContainer();

        // set up some default values
        initProperties();

        // insert row setup
        onInsertRow = false;
        insertRow = null;

        // set the warninings
        sqlwarn = new SQLWarning();
        rowsetWarning = new RowSetWarning();

    }

    /**
     * Provides a <code>CachedRowSetImpl</code> instance with the same default properties as
     * as the zero parameter constructor.
     * <pre>
     *     onInsertRow = false
     *     insertRow = null
     *     cursorPos = 0
     *     numRows = 0
     *     showDeleted = false
     *     queryTimeout = 0
     *     maxRows = 0
     *     maxFieldSize = 0
     *     rowSetType = ResultSet.TYPE_SCROLL_INSENSITIVE
     *     concurrency = ResultSet.CONCUR_UPDATABLE
     *     readOnly = false
     *     isolation = Connection.TRANSACTION_READ_COMMITTED
     *     escapeProcessing = true
     *     onInsertRow = false
     *     insertRow = null
     *     cursorPos = 0
     *     absolutePos = 0
     *     numRows = 0
     * </pre>
     *
     * However, applications will have the means to specify at runtime the
     * desired <code>SyncProvider</code> object.
     * <p>
     * For example, creating a <code>CachedRowSetImpl</code> object as follows ensures
     * that a it is established with the <code>com.foo.provider.Impl</code> synchronization
     * implementation providing the synchronization mechanism for this disconnected
     * <code>RowSet</code> object.
     * <pre>
     *     Hashtable env = new Hashtable();
     *     env.put(javax.sql.rowset.spi.SyncFactory.ROWSET_PROVIDER_NAME,
     *         "com.foo.provider.Impl");
     *     CachedRowSetImpl crs = new CachedRowSet(env);
     * </pre>
     * <p>
     * Calling this constructor with a <code>null</code> parameter will
     * cause the <code>SyncFactory</code> to provide the reference
     * optimistic provider <code>com.sun.rowset.providers.RIOptimisticProvider</code>.
     * <p>
     * In addition, the following properties can be associated with the
     * provider to assist in determining the choice of the synchronizaton
     * provider such as:
     * <ul>
     * <li><code>ROWSET_SYNC_PROVIDER</code> - the property specifying the the
     * <code>SyncProvider</code> class name to be instantiated by the
     * <code>SyncFacttory</code>
     * <li><code>ROWSET_SYNC_VENDOR</code> - the property specifying the software
     * vendor associated with a <code>SyncProvider</code> implementation.
     * <li><code>ROWSET_SYNC_PROVIDER_VER</code> - the property specifying the
     * version of the <code>SyncProvider</code> implementation provided by the
     * software vendor.
     * </ul>
     * More specific detailes are available in the <code>SyncFactory</code>
     * and <code>SyncProvider</code> specificiations later in this document.
     * <p>
     * @param env a <code>Hashtable</code> object with a list of desired
     *        synchronization providers
     * @throws SQLException if the requested provider cannot be found by the
     * synchonization factory
     * @see SyncProvider
     */

    public CachedRowSetImpl(Hashtable env) throws SQLException {


        try {
           resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
        } catch(IOException ioe) {
            throw new RuntimeException(ioe);
        }

        if (env == null) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.nullhash").toString());
        }

        String providerName = (String)env.get(
        javax.sql.rowset.spi.SyncFactory.ROWSET_SYNC_PROVIDER);

        // set the Reader, this maybe overridden latter
        provider =
        (SyncProvider)SyncFactory.getInstance(providerName);

        rowSetReader = provider.getRowSetReader();
        rowSetWriter = provider.getRowSetWriter();

        initParams(); // allocate the parameters collection
        initContainer();
        initProperties(); // set up some default values
    }

    /**
     * Sets the <code>rvh</code> field to a new <code>Vector</code>
     * object with a capacity of 100 and sets the
     * <code>cursorPos</code> and <code>numRows</code> fields to zero.
     */
    private void initContainer() {

        rvh = new Vector(100);
        cursorPos = 0;
        absolutePos = 0;
        numRows = 0;
        numDeleted = 0;
    }

    /**
     * Sets the properties for this <code>CachedRowSetImpl</code> object to
     * their default values. This method is called internally by the
     * default constructor.
     */

    private void initProperties() throws SQLException {

        if(resBundle == null) {
            try {
               resBundle = JdbcRowSetResourceBundle.getJdbcRowSetResourceBundle();
            } catch(IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
        setShowDeleted(false);
        setQueryTimeout(0);
        setMaxRows(0);
        setMaxFieldSize(0);
        setType(ResultSet.TYPE_SCROLL_INSENSITIVE);
        setConcurrency(ResultSet.CONCUR_UPDATABLE);
        if((rvh.size() > 0) && (isReadOnly() == false))
            setReadOnly(false);
        else
            setReadOnly(true);
        setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        setEscapeProcessing(true);
        setTypeMap(null);
        checkTransactionalWriter();

        //Instantiating the vector for MatchColumns

        iMatchColumns = new Vector(10);
        for(int i = 0; i < 10 ; i++) {
           iMatchColumns.add(i,new Integer(-1));
        }

        strMatchColumns = new Vector(10);
        for(int j = 0; j < 10; j++) {
           strMatchColumns.add(j,null);
        }
    }

    /**
     * Determine whether the SyncProvider's writer implements the
     * <code>TransactionalWriter<code> interface
     */
    private void checkTransactionalWriter() {
        if (rowSetWriter != null) {
            Class c = rowSetWriter.getClass();
            if (c != null) {
                Class[] theInterfaces = c.getInterfaces();
                for (int i = 0; i < theInterfaces.length; i++) {
                    if ((theInterfaces[i].getName()).indexOf("TransactionalWriter") > 0) {
                        tXWriter = true;
                        establishTransactionalWriter();
                    }
                }
            }
        }
    }

    /**
     * Sets an private field to all transaction bounddaries to be set
     */
    private void establishTransactionalWriter() {
        tWriter = (TransactionalWriter)provider.getRowSetWriter();
    }

    //-----------------------------------------------------------------------
    // Properties
    //-----------------------------------------------------------------------

    /**
     * Sets this <code>CachedRowSetImpl</code> object's command property
     * to the given <code>String</code> object and clears the parameters,
     * if any, that were set for the previous command.
     * <P>
     * The command property may not be needed
     * if the rowset is produced by a data source, such as a spreadsheet,
     * that does not support commands. Thus, this property is optional
     * and may be <code>null</code>.
     *
     * @param cmd a <code>String</code> object containing an SQL query
     *            that will be set as the command; may be <code>null</code>
     * @throws SQLException if an error occurs
     */
    public void setCommand(String cmd) throws SQLException {

        super.setCommand(cmd);

        if(!buildTableName(cmd).equals("")) {
            this.setTableName(buildTableName(cmd));
        }
    }


    //---------------------------------------------------------------------
    // Reading and writing data
    //---------------------------------------------------------------------

    /**
     * Populates this <code>CachedRowSetImpl</code> object with data from
     * the given <code>ResultSet</code> object.  This
     * method is an alternative to the method <code>execute</code>
     * for filling the rowset with data.  The method <code>populate</code>
     * does not require that the properties needed by the method
     * <code>execute</code>, such as the <code>command</code> property,
     * be set. This is true because the method <code>populate</code>
     * is given the <code>ResultSet</code> object from
     * which to get data and thus does not need to use the properties
     * required for setting up a connection and executing this
     * <code>CachedRowSetImpl</code> object's command.
     * <P>
     * After populating this rowset with data, the method
     * <code>populate</code> sets the rowset's metadata and
     * then sends a <code>RowSetChangedEvent</code> object
     * to all registered listeners prior to returning.
     *
     * @param data the <code>ResultSet</code> object containing the data
     *             to be read into this <code>CachedRowSetImpl</code> object
     * @throws SQLException if an error occurs; or the max row setting is
     *          violated while populating the RowSet
     * @see #execute
     */

     public void populate(ResultSet data) throws SQLException {
        int rowsFetched;
        Row currentRow;
        int numCols;
        int i;
        Map map = getTypeMap();
        Object obj;
        int mRows;

        if (data == null) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.populate").toString());
        }
        this.resultSet = data;

        // get the meta data for this ResultSet
        RSMD = data.getMetaData();

        // set up the metadata
        RowSetMD = new RowSetMetaDataImpl();
        initMetaData(RowSetMD, RSMD);

        // release the meta-data so that aren't tempted to use it.
        RSMD = null;
        numCols = RowSetMD.getColumnCount();
        mRows = this.getMaxRows();
        rowsFetched = 0;
        currentRow = null;

        while ( data.next()) {

            currentRow = new Row(numCols);

            if ( rowsFetched > mRows && mRows > 0) {
                rowsetWarning.setNextWarning(new RowSetWarning("Populating rows "
                + "setting has exceeded max row setting"));
            }
            for ( i = 1; i <= numCols; i++) {
                /*
                 * check if the user has set a map. If no map
                 * is set then use plain getObject. This lets
                 * us work with drivers that do not support
                 * getObject with a map in fairly sensible way
                 */
                if (map == null) {
                    obj = data.getObject(i);
                } else {
                    obj = data.getObject(i, map);
                }
                /*
                 * the following block checks for the various
                 * types that we have to serialize in order to
                 * store - right now only structs have been tested
                 */
                if (obj instanceof Struct) {
                    obj = new SerialStruct((Struct)obj, map);
                } else if (obj instanceof SQLData) {
                    obj = new SerialStruct((SQLData)obj, map);
                } else if (obj instanceof Blob) {
                    obj = new SerialBlob((Blob)obj);
                } else if (obj instanceof Clob) {
                    obj = new SerialClob((Clob)obj);
                } else if (obj instanceof java.sql.Array) {
                    obj = new SerialArray((java.sql.Array)obj, map);
                }

                ((Row)currentRow).initColumnObject(i, obj);
            }
            rowsFetched++;
            rvh.add(currentRow);
        }

        numRows = rowsFetched ;
        // Also rowsFetched should be equal to rvh.size()

        // notify any listeners that the rowset has changed
        notifyRowSetChanged();


    }

    /**
     * Initializes the given <code>RowSetMetaData</code> object with the values
     * in the given <code>ResultSetMetaData</code> object.
     *
     * @param md the <code>RowSetMetaData</code> object for this
     *           <code>CachedRowSetImpl</code> object, which will be set with
     *           values from rsmd
     * @param rsmd the <code>ResultSetMetaData</code> object from which new
     *             values for md will be read
     * @throws SQLException if an error occurs
     */
    private void initMetaData(RowSetMetaDataImpl md, ResultSetMetaData rsmd) throws SQLException {
        int numCols = rsmd.getColumnCount();

        md.setColumnCount(numCols);
        for (int col=1; col <= numCols; col++) {
            md.setAutoIncrement(col, rsmd.isAutoIncrement(col));
            if(rsmd.isAutoIncrement(col))
                updateOnInsert = true;
            md.setCaseSensitive(col, rsmd.isCaseSensitive(col));
            md.setCurrency(col, rsmd.isCurrency(col));
            md.setNullable(col, rsmd.isNullable(col));
            md.setSigned(col, rsmd.isSigned(col));
            md.setSearchable(col, rsmd.isSearchable(col));
             /*
             * The PostgreSQL drivers sometimes return negative columnDisplaySize,
             * which causes an exception to be thrown.  Check for it.
             */
            int size = rsmd.getColumnDisplaySize(col);
            if (size < 0) {
                size = 0;
            }
            md.setColumnDisplaySize(col, size);
            md.setColumnLabel(col, rsmd.getColumnLabel(col));
            md.setColumnName(col, rsmd.getColumnName(col));
            md.setSchemaName(col, rsmd.getSchemaName(col));
            /*
             * Drivers return some strange values for precision, for non-numeric data, including reports of
             * non-integer values; maybe we should check type, & set to 0 for non-numeric types.
             */
            int precision = rsmd.getPrecision(col);
            if (precision < 0) {
                precision = 0;
            }
            md.setPrecision(col, precision);

            /*
             * It seems, from a bug report, that a driver can sometimes return a negative
             * value for scale.  javax.sql.rowset.RowSetMetaDataImpl will throw an exception
             * if we attempt to set a negative value.  As such, we'll check for this case.
             */
            int scale = rsmd.getScale(col);
            if (scale < 0) {
                scale = 0;
            }
            md.setScale(col, scale);
            md.setTableName(col, rsmd.getTableName(col));
            md.setCatalogName(col, rsmd.getCatalogName(col));
            md.setColumnType(col, rsmd.getColumnType(col));
            md.setColumnTypeName(col, rsmd.getColumnTypeName(col));
        }

        if( conn != null){
           // JDBC 4.0 mandates as does the Java EE spec that all DataBaseMetaData methods
           // must be implemented, therefore, the previous fix for 5055528 is being backed out
           dbmslocatorsUpdateCopy = conn.getMetaData().locatorsUpdateCopy();
        }
    }

    /**
     * Populates this <code>CachedRowSetImpl</code> object with data,
     * using the given connection to produce the result set from
     * which data will be read.  A second form of this method,
     * which takes no arguments, uses the values from this rowset's
     * user, password, and either url or data source properties to
     * create a new database connection. The form of <code>execute</code>
     * that is given a connection ignores these properties.
     *
     * @param conn A standard JDBC <code>Connection</code> object that this
     * <code>CachedRowSet</code> object can pass to a synchronization provider
     * to establish a connection to the data source
     * @throws SQLException if an invalid <code>Connection</code> is supplied
     *           or an error occurs in establishing the connection to the
     *           data source
     * @see #populate
     * @see java.sql.Connection
     */
    public void execute(Connection conn) throws SQLException {
        // store the connection so the reader can find it.
        setConnection(conn);

        if(getPageSize() != 0){
            crsReader = (CachedRowSetReader)provider.getRowSetReader();
            crsReader.setStartPosition(1);
            callWithCon = true;
            crsReader.readData((RowSetInternal)this);
        }

        // Now call the current reader's readData method
        else {
           rowSetReader.readData((RowSetInternal)this);
        }
        RowSetMD = (RowSetMetaDataImpl)this.getMetaData();

        if(conn != null){
            // JDBC 4.0 mandates as does the Java EE spec that all DataBaseMetaData methods
            // must be implemented, therefore, the previous fix for 5055528 is being backed out
            dbmslocatorsUpdateCopy = conn.getMetaData().locatorsUpdateCopy();
        }

    }

    /**
     * Sets this <code>CachedRowSetImpl</code> object's connection property
     * to the given <code>Connection</code> object.  This method is called
     * internally by the version of the method <code>execute</code> that takes a
     * <code>Connection</code> object as an argument. The reader for this
     * <code>CachedRowSetImpl</code> object can retrieve the connection stored
     * in the rowset's connection property by calling its
     * <code>getConnection</code> method.
     *
     * @param connection the <code>Connection</code> object that was passed in
     *                   to the method <code>execute</code> and is to be stored
     *                   in this <code>CachedRowSetImpl</code> object's connection
     *                   property
     */
    private void setConnection (Connection connection) {
        conn = connection;
    }


    /**
     * Propagates all row update, insert, and delete changes to the
     * underlying data source backing this <code>CachedRowSetImpl</code>
     * object.
     * <P>
     * <b>Note</b>In the reference implementation an optimistic concurrency implementation
     * is provided as a sample implementation of a the <code>SyncProvider</code>
     * abstract class.
     * <P>
     * This method fails if any of the updates cannot be propagated back
     * to the data source.  When it fails, the caller can assume that
     * none of the updates are reflected in the data source.
     * When an exception is thrown, the current row
     * is set to the first "updated" row that resulted in an exception
     * unless the row that caused the exception is a "deleted" row.
     * In that case, when deleted rows are not shown, which is usually true,
     * the current row is not affected.
     * <P>
     * If no <code>SyncProvider</code> is configured, the reference implementation
     * leverages the <code>RIOptimisticProvider</code> available which provides the
     * default and reference synchronization capabilities for disconnected
     * <code>RowSets</code>.
     *
     * @throws SQLException if the cursor is on the insert row or the underlying
     *          reference synchronization provider fails to commit the updates
     *          to the datasource
     * @throws SyncProviderException if an internal error occurs within the
     *          <code>SyncProvider</code> instance during either during the
     *          process or at any time when the <code>SyncProvider</code>
     *          instance touches the data source.
     * @see #acceptChanges(java.sql.Connection)
     * @see javax.sql.RowSetWriter
     * @see javax.sql.rowset.spi.SyncProvider
     */
    public void acceptChanges() throws SyncProviderException {
        if (onInsertRow == true) {
            throw new SyncProviderException(resBundle.handleGetObject("cachedrowsetimpl.invalidop").toString());
        }

        int saveCursorPos = cursorPos;
        boolean success = false;
        boolean conflict = false;

        try {
            if (rowSetWriter != null) {
                saveCursorPos = cursorPos;
                conflict = rowSetWriter.writeData((RowSetInternal)this);
                cursorPos = saveCursorPos;
            }

            if ((tXWriter) && this.COMMIT_ON_ACCEPT_CHANGES) {
                // do commit/rollback's here
                if (!conflict) {
                    tWriter = (TransactionalWriter)rowSetWriter;
                    tWriter.rollback();
                    success = false;
                } else {
                    tWriter = (TransactionalWriter)rowSetWriter;
                    ((CachedRowSetWriter)tWriter).commit(this, updateOnInsert);
                    success = true;
                }
            }

            if (success == true) {
                setOriginal();
            } else if (!(success) && !(this.COMMIT_ON_ACCEPT_CHANGES)) {
                throw new SyncProviderException(resBundle.handleGetObject("cachedrowsetimpl.accfailed").toString());
            }

        } catch (SyncProviderException spe) {
               throw spe;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SyncProviderException(e.getMessage());
        } catch (SecurityException e) {
            throw new SyncProviderException(e.getMessage());
        }
    }

    /**
     * Propagates all row update, insert, and delete changes to the
     * data source backing this <code>CachedRowSetImpl</code> object
     * using the given <code>Connection</code> object.
     * <P>
     * The reference implementation <code>RIOptimisticProvider</code>
     * modifies its synchronization to a write back function given
     * the updated connection
     * The reference implementation modifies its synchronization behaviour
     * via the <code>SyncProvider</code> to ensure the synchronization
     * occurs according to the updated JDBC <code>Connection</code>
     * properties.
     *
     * @param con a standard JDBC <code>Connection</code> object
     * @throws SQLException if the cursor is on the insert row or the underlying
     *                   synchronization provider fails to commit the updates
     *                   back to the data source
     * @see #acceptChanges
     * @see javax.sql.RowSetWriter
     * @see javax.sql.rowset.spi.SyncFactory
     * @see javax.sql.rowset.spi.SyncProvider
     */
    public void acceptChanges(Connection con) throws SyncProviderException{

      try{
         setConnection(con);
         acceptChanges();
      } catch (SyncProviderException spe) {
          throw spe;
      } catch(SQLException sqle){
          throw new SyncProviderException(sqle.getMessage());
      }
    }

    /**
     * Restores this <code>CachedRowSetImpl</code> object to its original state,
     * that is, its state before the last set of changes.
     * <P>
     * Before returning, this method moves the cursor before the first row
     * and sends a <code>rowSetChanged</code> event to all registered
     * listeners.
     * @throws SQLException if an error is occurs rolling back the RowSet
     *           state to the definied original value.
     * @see javax.sql.RowSetListener#rowSetChanged
     */
    public void restoreOriginal() throws SQLException {
        Row currentRow;
        for (Iterator i = rvh.iterator(); i.hasNext();) {
            currentRow = (Row)i.next();
            if (currentRow.getInserted() == true) {
                i.remove();
                --numRows;
            } else {
                if (currentRow.getDeleted() == true) {
                    currentRow.clearDeleted();
                }
                if (currentRow.getUpdated() == true) {
                    currentRow.clearUpdated();
                }
            }
        }
        // move to before the first
        cursorPos = 0;

        // notify any listeners
        notifyRowSetChanged();
    }

    /**
     * Releases the current contents of this <code>CachedRowSetImpl</code>
     * object and sends a <code>rowSetChanged</code> event object to all
     * registered listeners.
     *
     * @throws SQLException if an error occurs flushing the contents of
     *           RowSet.
     * @see javax.sql.RowSetListener#rowSetChanged
     */
    public void release() throws SQLException {
        initContainer();
        notifyRowSetChanged();
    }

    /**
     * Cancels deletion of the current row and notifies listeners that
     * a row has changed.
     * <P>
     * Note:  This method can be ignored if deleted rows are not being shown,
     * which is the normal case.
     *
     * @throws SQLException if the cursor is not on a valid row
     */
    public void undoDelete() throws SQLException {
        if (getShowDeleted() == false) {
            return;
        }
        // make sure we are on a row
        checkCursor();

        // don't want this to happen...
        if (onInsertRow == true) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidcp").toString());
        }

        Row currentRow = (Row)getCurrentRow();
        if (currentRow.getDeleted() == true) {
            currentRow.clearDeleted();
            --numDeleted;
            notifyRowChanged();
        }
    }

    /**
     * Immediately removes the current row from this
     * <code>CachedRowSetImpl</code> object if the row has been inserted, and
     * also notifies listeners the a row has changed.  An exception is thrown
     * if the row is not a row that has been inserted or the cursor is before
     * the first row, after the last row, or on the insert row.
     * <P>
     * This operation cannot be undone.
     *
     * @throws SQLException if an error occurs,
     *                         the cursor is not on a valid row,
     *                         or the row has not been inserted
     */
    public void undoInsert() throws SQLException {
        // make sure we are on a row
        checkCursor();

        // don't want this to happen...
        if (onInsertRow == true) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidcp").toString());
        }

        Row currentRow = (Row)getCurrentRow();
        if (currentRow.getInserted() == true) {
            rvh.remove(cursorPos-1);
            --numRows;
            notifyRowChanged();
        } else {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.illegalop").toString());
        }
    }

    /**
     * Immediately reverses the last update operation if the
     * row has been modified. This method can be
     * called to reverse updates on a all columns until all updates in a row have
     * been rolled back to their originating state since the last synchronization
     * (<code>acceptChanges</code>) or population. This method may also be called
     * while performing updates to the insert row.
     * <P>
     * <code>undoUpdate</code may be called at any time during the life-time of a
     * rowset, however after a synchronization has occurs this method has no
     * affect until further modification to the RowSet data occurs.
     *
     * @throws SQLException if cursor is before the first row, after the last
     *     row in rowset.
     * @see #undoDelete
     * @see #undoInsert
     * @see java.sql.ResultSet#cancelRowUpdates
     */
    public void undoUpdate() throws SQLException {
        // if on insert row, cancel the insert row
        // make the insert row flag,
        // cursorPos back to the current row
        moveToCurrentRow();

        // else if not on insert row
        // call undoUpdate or undoInsert
        undoDelete();

        undoInsert();

    }

    //--------------------------------------------------------------------
    // Views
    //--------------------------------------------------------------------

    /**
     * Returns a new <code>RowSet</code> object backed by the same data as
     * that of this <code>CachedRowSetImpl</code> object and sharing a set of cursors
     * with it. This allows cursors to interate over a shared set of rows, providing
     * multiple views of the underlying data.
     *
     * @return a <code>RowSet</code> object that is a copy of this <code>CachedRowSetImpl</code>
     * object and shares a set of cursors with it
     * @throws SQLException if an error occurs or cloning is
     *                         not supported
     * @see javax.sql.RowSetEvent
     * @see javax.sql.RowSetListener
     */
    public RowSet createShared() throws SQLException {
        RowSet clone;
        try {
            clone = (RowSet)clone();
        } catch (CloneNotSupportedException ex) {
            throw new SQLException(ex.getMessage());
        }
        return clone;
    }

    /**
     * Returns a new <code>RowSet</code> object containing by the same data
     * as this <code>CachedRowSetImpl</code> object.  This method
     * differs from the method <code>createCopy</code> in that it throws a
     * <code>CloneNotSupportedException</code> object instead of an
     * <code>SQLException</code> object, as the method <code>createShared</code>
     * does.  This <code>clone</code>
     * method is called internally by the method <code>createShared</code>,
     * which catches the <code>CloneNotSupportedException</code> object
     * and in turn throws a new <code>SQLException</code> object.
     *
     * @return a copy of this <code>CachedRowSetImpl</code> object
     * @throws CloneNotSupportedException if an error occurs when
     * attempting to clone this <code>CachedRowSetImpl</code> object
     * @see #createShared
     */
    protected Object clone() throws CloneNotSupportedException  {
        return (super.clone());
    }

    /**
     * Creates a <code>RowSet</code> object that is a deep copy of
     * this <code>CachedRowSetImpl</code> object's data, including
     * constraints.  Updates made
     * on a copy are not visible to the original rowset;
     * a copy of a rowset is completely independent from the original.
     * <P>
     * Making a copy saves the cost of creating an identical rowset
     * from first principles, which can be quite expensive.
     * For example, it can eliminate the need to query a
     * remote database server.
     * @return a new <code>CachedRowSet</code> object that is a deep copy
     *           of this <code>CachedRowSet</code> object and is
     *           completely independent from this <code>CachedRowSetImpl</code>
     *           object.
     * @throws SQLException if an error occurs in generating the copy of this
     *           of the <code>CachedRowSetImpl</code>
     * @see #createShared
     * @see javax.sql.RowSetEvent
     * @see javax.sql.RowSetListener
     */
    public CachedRowSet createCopy() throws SQLException {
        ObjectOutputStream out;
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        try {
            out = new ObjectOutputStream(bOut);
            out.writeObject(this);
        } catch (IOException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.clonefail").toString() , ex.getMessage()));
        }

        ObjectInputStream in;

        try {
            ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
            in = new ObjectInputStream(bIn);
        } catch (StreamCorruptedException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.clonefail").toString() , ex.getMessage()));
        } catch (IOException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.clonefail").toString() , ex.getMessage()));
        }

        try {
            //return ((CachedRowSet)(in.readObject()));
            CachedRowSetImpl crsTemp = (CachedRowSetImpl)in.readObject();
            crsTemp.resBundle = this.resBundle;
            return ((CachedRowSet)crsTemp);

        } catch (ClassNotFoundException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.clonefail").toString() , ex.getMessage()));
        } catch (OptionalDataException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.clonefail").toString() , ex.getMessage()));
        } catch (IOException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.clonefail").toString() , ex.getMessage()));
        }
    }

    /**
     * Creates a <code>RowSet</code> object that is a copy of
     * this <code>CachedRowSetImpl</code> object's table structure
     * and the constraints only.
     * There will be no data in the object being returned.
     * Updates made on a copy are not visible to the original rowset.
     * <P>
     * This helps in getting the underlying XML schema which can
     * be used as the basis for populating a <code>WebRowSet</code>.
     *
     * @return a new <code>CachedRowSet</code> object that is a copy
     * of this <code>CachedRowSetImpl</code> object's schema and
     * retains all the constraints on the original rowset but contains
     * no data
     * @throws SQLException if an error occurs in generating the copy
     * of the <code>CachedRowSet</code> object
     * @see #createShared
     * @see #createCopy
     * @see #createCopyNoConstraints
     * @see javax.sql.RowSetEvent
     * @see javax.sql.RowSetListener
     */
    public CachedRowSet createCopySchema() throws SQLException {
        // Copy everything except data i.e all constraints

        // Store the number of rows of "this"
        // and make numRows equals zero.
        // and make data also zero.
        int nRows = numRows;
        numRows = 0;

        CachedRowSet crs = this.createCopy();

        // reset this object back to number of rows.
        numRows = nRows;

        return crs;
    }

    /**
     * Creates a <code>CachedRowSet</code> object that is a copy of
     * this <code>CachedRowSetImpl</code> object's data only.
     * All constraints set in this object will not be there
     * in the returning object.  Updates made
     * on a copy are not visible to the original rowset.
     *
     * @return a new <code>CachedRowSet</code> object that is a deep copy
     * of this <code>CachedRowSetImpl</code> object and is
     * completely independent from this <code>CachedRowSetImpl</code> object
     * @throws SQLException if an error occurs in generating the copy of the
     * of the <code>CachedRowSet</code>
     * @see #createShared
     * @see #createCopy
     * @see #createCopySchema
     * @see javax.sql.RowSetEvent
     * @see javax.sql.RowSetListener
     */
    public CachedRowSet createCopyNoConstraints() throws SQLException {
        // Copy the whole data ONLY without any constraints.
        CachedRowSetImpl crs;
        crs = (CachedRowSetImpl)this.createCopy();

        crs.initProperties();
        try {
            crs.unsetMatchColumn(crs.getMatchColumnIndexes());
        } catch(SQLException sqle) {
            //do nothing, if the setMatchColumn is not set.
        }

        try {
            crs.unsetMatchColumn(crs.getMatchColumnNames());
        } catch(SQLException sqle) {
            //do nothing, if the setMatchColumn is not set.
        }

        return crs;
    }

    /**
     * Converts this <code>CachedRowSetImpl</code> object to a collection
     * of tables. The sample implementation utilitizes the <code>TreeMap</code>
     * collection type.
     * This class guarantees that the map will be in ascending key order,
     * sorted according to the natural order for the key's class.
     *
     * @return a <code>Collection</code> object consisting of tables,
     *         each of which is a copy of a row in this
     *         <code>CachedRowSetImpl</code> object
     * @throws SQLException if an error occurs in generating the collection
     * @see #toCollection(int)
     * @see #toCollection(String)
     * @see java.util.TreeMap
     */
    public Collection<?> toCollection() throws SQLException {

        TreeMap tMap;
        int count = 0;
        Row origRow;
        Vector newRow;

        int colCount = ((RowSetMetaDataImpl)this.getMetaData()).getColumnCount();

        tMap = new TreeMap();

        for (int i = 0; i<numRows; i++) {
            tMap.put(new Integer(i), rvh.get(i));
        }

        return (tMap.values());
    }

    /**
     * Returns the specified column of this <code>CachedRowSetImpl</code> object
     * as a <code>Collection</code> object.  This method makes a copy of the
     * column's data and utilitizes the <code>Vector</code> to establish the
     * collection. The <code>Vector</code> class implements a growable array
     * objects allowing the individual components to be accessed using an
     * an integer index similar to that of an array.
     *
     * @return a <code>Collection</code> object that contains the value(s)
     *         stored in the specified column of this
     *         <code>CachedRowSetImpl</code>
     *         object
     * @throws SQLException if an error occurs generated the collection; or
     *          an invalid column is provided.
     * @see #toCollection()
     * @see #toCollection(String)
     * @see java.util.Vector
     */
    public Collection<?> toCollection(int column) throws SQLException {

        Vector vec;
        Row origRow;
        int nRows = numRows;
        vec = new Vector(nRows);

        // create a copy
        CachedRowSetImpl crsTemp;
        crsTemp = (CachedRowSetImpl) this.createCopy();

        while(nRows!=0) {
            crsTemp.next();
            vec.add(crsTemp.getObject(column));
            nRows--;
        }

        return (Collection)vec;
    }

    /**
     * Returns the specified column of this <code>CachedRowSetImpl</code> object
     * as a <code>Collection</code> object.  This method makes a copy of the
     * column's data and utilitizes the <code>Vector</code> to establish the
     * collection. The <code>Vector</code> class implements a growable array
     * objects allowing the individual components to be accessed using an
     * an integer index similar to that of an array.
     *
     * @return a <code>Collection</code> object that contains the value(s)
     *         stored in the specified column of this
     *         <code>CachedRowSetImpl</code>
     *         object
     * @throws SQLException if an error occurs generated the collection; or
     *          an invalid column is provided.
     * @see #toCollection()
     * @see #toCollection(int)
     * @see java.util.Vector
     */
    public Collection<?> toCollection(String column) throws SQLException {
        return toCollection(getColIdxByName(column));
    }

    //--------------------------------------------------------------------
    // Advanced features
    //--------------------------------------------------------------------


    /**
     * Returns the <code>SyncProvider</code> implementation being used
     * with this <code>CachedRowSetImpl</code> implementation rowset.
     *
     * @return the SyncProvider used by the rowset. If not provider was
     *          set when the rowset was instantiated, the reference
     *          implementation (default) provider is returned.
     * @throws SQLException if error occurs while return the
     *          <code>SyncProvider</code> instance.
     */
    public SyncProvider getSyncProvider() throws SQLException {
        return provider;
    }

    /**
     * Sets the active <code>SyncProvider</code> and attempts to load
     * load the new provider using the <code>SyncFactory</code> SPI.
     *
     * @throws SQLException if an error occurs while resetting the
     *          <code>SyncProvider</code>.
     */
    public void setSyncProvider(String providerStr) throws SQLException {
        provider =
        (SyncProvider)SyncFactory.getInstance(providerStr);

        rowSetReader = provider.getRowSetReader();
        rowSetWriter = provider.getRowSetWriter();
    }


    //-----------------
    // methods inherited from RowSet
    //-----------------






    //---------------------------------------------------------------------
    // Reading and writing data
    //---------------------------------------------------------------------

    /**
     * Populates this <code>CachedRowSetImpl</code> object with data.
     * This form of the method uses the rowset's user, password, and url or
     * data source name properties to create a database
     * connection.  If properties that are needed
     * have not been set, this method will throw an exception.
     * <P>
     * Another form of this method uses an existing JDBC <code>Connection</code>
     * object instead of creating a new one; therefore, it ignores the
     * properties used for establishing a new connection.
     * <P>
     * The query specified by the command property is executed to create a
     * <code>ResultSet</code> object from which to retrieve data.
     * The current contents of the rowset are discarded, and the
     * rowset's metadata is also (re)set.  If there are outstanding updates,
     * they are also ignored.
     * <P>
     * The method <code>execute</code> closes any database connections that it
     * creates.
     *
     * @throws SQLException if an error occurs or the
     *                         necessary properties have not been set
     */
    public void execute() throws SQLException {
        execute(null);
    }



    //-----------------------------------
    // Methods inherited from ResultSet
    //-----------------------------------

    /**
     * Moves the cursor down one row from its current position and
     * returns <code>true</code> if the new cursor position is a
     * valid row.
     * The cursor for a new <code>ResultSet</code> object is initially
     * positioned before the first row. The first call to the method
     * <code>next</code> moves the cursor to the first row, making it
     * the current row; the second call makes the second row the
     * current row, and so on.
     *
     * <P>If an input stream from the previous row is open, it is
     * implicitly closed. The <code>ResultSet</code> object's warning
     * chain is cleared when a new row is read.
     *
     * @return <code>true</code> if the new current row is valid;
     *         <code>false</code> if there are no more rows
     * @throws SQLException if an error occurs or
     *            the cursor is not positioned in the rowset, before
     *            the first row, or after the last row
     */
    public boolean next() throws SQLException {
        /*
         * make sure things look sane. The cursor must be
         * positioned in the rowset or before first (0) or
         * after last (numRows + 1)
         */
        if (cursorPos < 0 || cursorPos >= numRows + 1) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidcp").toString());
        }
        // now move and notify
        boolean ret = this.internalNext();
        notifyCursorMoved();

        return ret;
    }

    /**
     * Moves this <code>CachedRowSetImpl</code> object's cursor to the next
     * row and returns <code>true</code> if the cursor is still in the rowset;
     * returns <code>false</code> if the cursor has moved to the position after
     * the last row.
     * <P>
     * This method handles the cases where the cursor moves to a row that
     * has been deleted.
     * If this rowset shows deleted rows and the cursor moves to a row
     * that has been deleted, this method moves the cursor to the next
     * row until the cursor is on a row that has not been deleted.
     * <P>
     * The method <code>internalNext</code> is called by methods such as
     * <code>next</code>, <code>absolute</code>, and <code>relative</code>,
     * and, as its name implies, is only called internally.
     * <p>
     * This is a implementation only method and is not required as a standard
     * implementation of the <code>CachedRowSet</code> interface.
     *
     * @return <code>true</code> if the cursor is on a valid row in this
     *         rowset; <code>false</code> if it is after the last row
     * @throws SQLException if an error occurs
     */
    protected boolean internalNext() throws SQLException {
        boolean ret = false;

        do {
            if (cursorPos < numRows) {
                ++cursorPos;
                ret = true;
            } else if (cursorPos == numRows) {
                // increment to after last
                ++cursorPos;
                ret = false;
                break;
            }
        } while ((getShowDeleted() == false) && (rowDeleted() == true));

        /* each call to internalNext may increment cursorPos multiple
         * times however, the absolutePos only increments once per call.
         */
        if (ret == true)
            absolutePos++;
        else
            absolutePos = 0;

        return ret;
    }

    /**
     * Closes this <code>CachedRowSetImpl</code> objecy and releases any resources
     * it was using.
     *
     * @throws SQLException if an error occurs when releasing any resources in use
     * by this <code>CachedRowSetImpl</code> object
     */
    public void close() throws SQLException {

        // close all data structures holding
        // the disconnected rowset

        cursorPos = 0;
        absolutePos = 0;
        numRows = 0;
        numDeleted = 0;

        // set all insert(s), update(s) & delete(s),
        // if at all, to their initial values.
        initProperties();

        // clear the vector of it's present contents
        rvh.clear();

        // this will make it eligible for gc
        // rvh = null;
    }

    /**
     * Reports whether the last column read was SQL <code>NULL</code>.
     * Note that you must first call the method <code>getXXX</code>
     * on a column to try to read its value and then call the method
     * <code>wasNull</code> to determine whether the value was
     * SQL <code>NULL</code>.
     *
     * @return <code>true</code> if the value in the last column read
     *         was SQL <code>NULL</code>; <code>false</code> otherwise
     * @throws SQLException if an error occurs
     */
    public boolean wasNull() throws SQLException {
        return lastValueNull;
    }

    /**
     * Sets the field <code>lastValueNull</code> to the given
     * <code>boolean</code> value.
     *
     * @param value <code>true</code> to indicate that the value of
     *        the last column read was SQL <code>NULL</code>;
     *        <code>false</code> to indicate that it was not
     */
    private void setLastValueNull(boolean value) {
        lastValueNull = value;
    }

    // Methods for accessing results by column index

    /**
     * Checks to see whether the given index is a valid column number
     * in this <code>CachedRowSetImpl</code> object and throws
     * an <code>SQLException</code> if it is not. The index is out of bounds
     * if it is less than <code>1</code> or greater than the number of
     * columns in this rowset.
     * <P>
     * This method is called internally by the <code>getXXX</code> and
     * <code>updateXXX</code> methods.
     *
     * @param idx the number of a column in this <code>CachedRowSetImpl</code>
     *            object; must be between <code>1</code> and the number of
     *            rows in this rowset
     * @throws SQLException if the given index is out of bounds
     */
    private void checkIndex(int idx) throws SQLException {
        if (idx < 1 || idx > RowSetMD.getColumnCount()) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidcol").toString());
        }
    }

    /**
     * Checks to see whether the cursor for this <code>CachedRowSetImpl</code>
     * object is on a row in the rowset and throws an
     * <code>SQLException</code> if it is not.
     * <P>
     * This method is called internally by <code>getXXX</code> methods, by
     * <code>updateXXX</code> methods, and by methods that update, insert,
     * or delete a row or that cancel a row update, insert, or delete.
     *
     * @throws SQLException if the cursor for this <code>CachedRowSetImpl</code>
     *         object is not on a valid row
     */
    private void checkCursor() throws SQLException {
        if (isAfterLast() == true || isBeforeFirst() == true) {
            throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalidcp").toString());
        }
    }

    /**
     * Returns the column number of the column with the given name in this
     * <code>CachedRowSetImpl</code> object.  This method throws an
     * <code>SQLException</code> if the given name is not the name of
     * one of the columns in this rowset.
     *
     * @param name a <code>String</code> object that is the name of a column in
     *              this <code>CachedRowSetImpl</code> object
     * @throws SQLException if the given name does not match the name of one of
     *         the columns in this rowset
     */
    private int getColIdxByName(String name) throws SQLException {
        RowSetMD = (RowSetMetaDataImpl)this.getMetaData();
        int cols = RowSetMD.getColumnCount();

        for (int i=1; i <= cols; ++i) {
            String colName = RowSetMD.getColumnName(i);
            if (colName != null)
                if (name.equalsIgnoreCase(colName))
                    return (i);
                else
                    continue;
        }
        throw new SQLException(resBundle.handleGetObject("cachedrowsetimpl.invalcolnm").toString());

    }

    /**
     * Returns the insert row or the current row of this
     * <code>CachedRowSetImpl</code>object.
     *
     * @return the <code>Row</code> object on which this <code>CachedRowSetImpl</code>
     * objects's cursor is positioned
     */
    protected BaseRow getCurrentRow() {
        if (onInsertRow == true) {
            return (BaseRow)insertRow;
        } else {
            return (BaseRow)(rvh.get(cursorPos - 1));
        }
    }

    /**
     * Removes the row on which the cursor is positioned.
     * <p>
     * This is a implementation only method and is not required as a standard
     * implementation of the <code>CachedRowSet</code> interface.
     *
     * @throws SQLException if the cursor is positioned on the insert
     *            row
     */
    protected void removeCurrentRow() {
        ((Row)getCurrentRow()).setDeleted();
        rvh.remove(cursorPos);
        --numRows;
    }


    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>CachedRowSetImpl</code> object as a
     * <code>String</code> object.
     *
     * @param columnIndex the first column is <code>1</code>, the second
     *        is <code>2</code>, and so on; must be <code>1</code> or larger
     *        and equal to or less than the number of columns in the rowset
     * @return the column value; if the value is SQL <code>NULL</code>, the
     *         result is <code>null</code>
     * @throws SQLException if (1) the given column index is out of bounds,
     * (2) the cursor is not on one of this rowset's rows or its
     * insert row, or (3) the designated column does not store an
     * SQL <code>TINYINT, SMALLINT, INTEGER, BIGINT, REAL,
     * FLOAT, DOUBLE, DECIMAL, NUMERIC, BIT, <b>CHAR</b>, <b>VARCHAR</b></code>
     * or <code>LONGVARCHAR</code> value. The bold SQL type designates the
     * recommended return type.
     */
    public String getString(int columnIndex) throws SQLException {
        Object value;

        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();

        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);

        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return null;
        }

        return value.toString();
    }

    /**
     * Retrieves the value of the designated column in the current row
     * of this <code>CachedRowSetImpl</code> object as a
     * <code>boolean</code> value.
     *
     * @param columnIndex the first column is <code>1</code>, the second
     *        is <code>2</code>, and so on; must be <code>1</code> or larger
     *        and equal to or less than the number of columns in the rowset
     * @return the column value as a <code>boolean</code> in the Java progamming language;
     *        if the value is SQL <code>NULL</code>, the result is <code>false</code>
     * @throws SQLException if (1) the given column index is out of bounds,
     *            (2) the cursor is not on one of this rowset's rows or its
     *            insert row, or (3) the designated column does not store an
     *            SQL <code>BOOLEAN</code> value
     * @see #getBoolean(String)
     */
    public boolean getBoolean(int columnIndex) throws SQLException {
        Object value;

        // sanity check.
        checkIndex(columnIndex);
        // make sure the cursor is on a valid row
        checkCursor();

        setLastValueNull(false);
        value = getCurrentRow().getColumnObject(columnIndex);

        // check for SQL NULL
        if (value == null) {
            setLastValueNull(true);
            return false;
        }

        // check for Boolean...
        if (value instanceof Boolean) {
            return ((Boolean)value).booleanValue();
        }

        // convert to a Double and compare to zero
        try {
            Double d = new Double(value.toString());
            if (d.compareTo(new Double((double)0)) == 0) {
                return false;
            } else {
                return true;
            }
        } catch (NumberFormatException ex) {
            throw new SQLException(MessageFormat.format(resBundle.handleGetObject("cachedrowsetimpl.bool
