package mpv5.db.common;

import java.awt.Color;
import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.globals.Constants;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.panels.DataPanel;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.arrays.ArrayUtilities;

import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.text.RandomText;
import mpv5.utils.text.TypeConversion;
import mpv5.utils.ui.TextFieldUtils;

/**
 *
 * Use this class to access the Yabs database.
 *
 * @see QueryHandler#instanceOf()
 * @see QueryHandler#getConnection()
 * @see QueryHandler#clone(mpv5.db.common.Context)
 */
public class QueryHandler implements Cloneable {

    private static QueryHandler instance;
    private static JProgressBar progressbar = new JProgressBar();

    private static class SQLWatch extends Thread {

        private final long start;
        private boolean done;
        private final String watchedQuery;
        private long minTime = 1000l;

        public SQLWatch(String query) {
            start = new Date().getTime();
            watchedQuery = query;
        }

        public void done() {
            done = true;
        }

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            while (!done) {
                try {
                    Thread.sleep(minTime);
                } catch (InterruptedException ex) {
                    Log.Debug(ex);
                }
                Log.Debug(this, "SQLWatch " + this + " ["
                        + (new Date().getTime() - start)
                        + "]ms for " + watchedQuery);

            }
        }
    }
    private DatabaseConnection conn = null;
    private Connection sqlConn = null;
    private String table = "NOTABLE";
    private static JFrame comp = new JFrame();
    private Context context;

    /**
     * !Use "Clone" method before actually do anything!
     *
     * @return The one and only instance of the database connection
     */
    public static synchronized QueryHandler instanceOf() {
        //Explicitely instantiated at first connection attempt
        if (instance == null) {
            instance = new QueryHandler();
        }
        return instance;
    }
    /**
     * Convenience method which calls clone and returns the cloned QH
     * @param c
     * @return 
     */
    public static synchronized QueryHandler instanceOf(Context c) {
        return instanceOf().clone(c);
    }
    
    private DataPanel viewToBeNotified = null;
    private static Integer ROW_LIMIT = null;
    private volatile int limit = 0;
    private boolean runInBackground = false;
    private static PreparedStatement ivpps;
    private static PreparedStatement uvpps;

    private QueryHandler() {
        try {
            conn = DatabaseConnection.instanceOf();
            sqlConn = conn.getConnection();
            versionCheck();
            runFixes();
            createPs();
        } catch (Exception ex) {
            Log.Debug(ex);
            Popup.error(ex);
        }
    }

    private void versionCheck() {
        try {

            Statement versionCheck = sqlConn.createStatement();
            Log.Debug(this, "Checking database version..");
            ResultSet versionData = versionCheck.executeQuery("SELECT value FROM globalsettings WHERE cname = 'yabs_dbversion'");
            if (versionData.next()) {
                double dbversion = Double.valueOf(versionData.getString(1));
                Log.Debug(this, "Database version found: " + dbversion);
                if (dbversion >= Constants.DATABASE_MAX_VERSION.doubleValue()) {
                    throw new UnsupportedOperationException("Database version is too high! Required min version: " + Constants.DATABASE_VERSION + " Required max version: " + Constants.DATABASE_MAX_VERSION);
                } else if (dbversion < Constants.DATABASE_VERSION.doubleValue()) {
                    new DatabaseUpdater().updateFrom(dbversion);
                }
            } else {
                Log.Debug(this, "Database version info can not be found.");
                throw new UnsupportedOperationException("Database version cannot be validated! Required version: " + Constants.DATABASE_VERSION + "\n\n"
                        + "To solve this issue, you maybe want to run Yabs with the parameter -finstall");
            }
        } catch (Exception ex) {
            Log.Debug(ex);
            Popup.error(ex);
        }
    }

    private void runFixes() {
        try {
            Statement runfixes = sqlConn.createStatement();

            //Issue #239////////////////////////////////////////////////////////
            runfixes.setMaxRows(1);
            ResultSet firstgroup = runfixes.executeQuery("SELECT groupsids, ids FROM groups ORDER BY ids ASC");
            if (firstgroup.next()) {
                int gids = firstgroup.getInt(1);
                if (gids != 0) {
                    runfixes.execute("update groups set groupsids = 0 where ids = " + firstgroup.getInt(2));
                    Log.Debug(this, "Corrected group 1 to fix Issue #239");
                }
            }
            ResultSet firstpgroup = runfixes.executeQuery("SELECT productgroupsids, ids  FROM productgroups ORDER BY ids ASC");
            if (firstpgroup.next()) {
                int gids = firstpgroup.getInt(1);
                if (gids != 0) {
                    runfixes.execute("update productgroups set productgroupsids = 0 where ids = " +firstpgroup.getInt(2));
                    Log.Debug(this, "Corrected productgroup 1 to fix Issue #239");
                }
            }
            ResultSet firstaccount = runfixes.executeQuery("SELECT intparentaccount, ids  FROM accounts ORDER BY ids ASC");
            if (firstaccount.next()) {
                int gids = firstaccount.getInt(1);
                if (gids != 0) {
                    runfixes.execute("update accounts set intparentaccount = 0 where ids = " + firstaccount.getInt(2));
                    Log.Debug(this, "Corrected account 1 to fix Issue #239");
                }
            }
            ////////////////////////////////////////////////////////////////////
        } catch (Exception ex) {
            Log.Debug(this,ex.getMessage().toString());
            Popup.error(ex);
        }
    }

    /**
     * Set the global row limit for select queries. 0 is unlimited.
     *
     * @param limit
     */
    public static synchronized void setRowLimit(int limit) {
        if (ROW_LIMIT == null || limit > ROW_LIMIT.intValue() || limit < ROW_LIMIT.intValue()) {
            Log.Debug(QueryHandler.class, "Setting global row limit to: " + limit);
            ROW_LIMIT = limit;
        }
    }

    /**
     * <b>Do not use this during 'normal' program operation.</b>
     *
     * @see QueryHandler#instanceOf() instead
     * @param c
     */
    public QueryHandler(DatabaseConnection c) {
        try {
            conn = c;
            sqlConn = conn.getConnection();
        } catch (Exception ex) {
            Log.Debug(ex);
            Popup.error(ex);
        }
    }

    protected void setLimit(int limit) {
        if (limit > this.limit || limit < this.limit) {
            Log.Debug(QueryHandler.class, "Setting row limit for this connection to: " + limit);
            this.limit = limit;
        }
    }

    /**
     * Builds a select query which selects all fields from all rows where the
     * fields match the given value.
     *
     * @param value
     * @param fields
     * @return A query String select ids, bla
     */
    public String buildQuery(Object value, String... fields) {
        return buildQuery(fields, fields, "cname", value, "OR");
    }

    /**
     * Builds a select query which selects ids from all rows where the fields
     * match the given value.
     *
     * @param value
     * @param fields
     * @return A query String select ids, bla
     */
    public String buildIdQuery(Object value, String... fields) {
        return buildQuery(new String[]{"ids"}, fields, "cname", value, "OR");
    }

    /**
     * selct ids, columns
     *
     * @param columns
     * @param conditionColumns
     * @param order
     * @param value
     * @param command
     * @return
     */
    public String buildQuery(String[] columns, String[] conditionColumns, String order, Object value, String command) {
        String cols = "";
        if (columns != null && columns.length > 0) {
            for (int i = 0; i < columns.length; i++) {
                String column = columns[i];
                cols += "," + column;
            }
            cols = cols.substring(1);
        } else {
            cols = "*";
        }
        String conds = "";
        if (conditionColumns != null && conditionColumns.length > 0) {
            for (int i = 0; i < conditionColumns.length; i++) {
                String string = conditionColumns[i];
                if (value instanceof String) {
                    conds += " UPPER(" + string + ") LIKE '%" + String.valueOf(value).toUpperCase() + "%'  " + command + " ";
                } else if (value instanceof Date) {
                    conds += string + " = '" + DateConverter.getSQLDateString((Date) value) + "'  " + command + " ";
                } else {
                    conds += string + " = " + value + "  " + command + " ";
                }
            }
            conds = " WHERE (" + conds.substring(0, conds.length() - 4) + ")";
            if (context.getGroupRestrictionSQLString() != null) {
                conds += " AND " + context.getGroupRestrictionSQLString();
            }
            if (context.getNoTrashSQLString() != null) {
                conds += " AND " + context.getNoTrashSQLString();
            }
        }
        String query = "SELECT ids," + cols + " FROM " + table + conds;
        if (order != null) {
            query += " ORDER BY " + order;
        }

        return query;
    }

    /**
     * Checks the uniqueness of a unique constraint Works only for columns with
     * equal data type
     *
     * @param constraint {"column1","column2"}
     * @param values {"value1",value2<any/>}
     * @return true if the key constraint is not existing yet
     */
    public boolean checkConstraint(String[] constraint, Object[] values) {
        for (int i = 0; i < values.length; i++) {
            Object object = values[i];

            if (!(object instanceof Number)) {
                values[i] = "'" + object.toString() + "'";
            }

        }

        Object[][] val = select(context.getDbIdentity() + ".ids", constraint, values, false);
        if (val != null && val.length > 0) {
            Log.Debug(this, "Uniqueness check failed!");
            return false;
        } else {
            return true;
        }
    }

    /**
     * This is a convenience bridge between views and unique constraint checks.
     * If the given objects is from type JTextField or LabeledTextField, the
     * TextFields background will flash red<br/> if the uniqueness check fails,
     * nothing will happen otherwise
     *
     * @param uniqueColumns to be separated with a comma
     * @param object An array of textfields
     * @return true if no uniqueness failure has been hidden
     */
    public boolean checkUniqueness(String uniqueColumns, JTextField[] object) {
        boolean returnv = true;
        for (int i = 0; i < object.length; i++) {
            if (!checkUniqueness(uniqueColumns.split(",")[i], (object[i]).getText())) {
                TextFieldUtils.blinkerRed(object[i]);
                returnv = false;
            }
        }

        return returnv;
    }

    /**
     * Returns a full column
     *
     * @param columnName column1
     * @param maximumRowCount If >0 , this is the row count limit
     * @param q
     * @return The column
     * @throws NodataFoundException
     */
    public Object[] getColumn(String columnName, int maximumRowCount, QueryCriteria2 q) throws NodataFoundException {
        return ArrayUtilities.ObjectToSingleColumnArray(getColumns(new String[]{columnName}, maximumRowCount, q));
    }

    /**
     * Select multiple columns
     *
     * @param columnNames column1, column2, column3...
     * @param maximumRowCount
     * @param q
     * @return
     * @throws NodataFoundException <i><b>Omits trashed datasets
     * implicitly</b></i>
     */
    public Object[][] getColumns(String[] columnNames, int maximumRowCount, QueryCriteria2 q) throws NodataFoundException {
        ReturnValue data = null;
        String columnName = "";
        for (int i = 0; i < columnNames.length; i++) {
            String string = columnNames[i];
            if (i < columnNames.length - 1) {
                columnName += string + ",";
            } else {
                columnName += string;
            }
        }
        if (maximumRowCount > 0) {
            data = freeSelectQuery("SELECT TOP(" + maximumRowCount + ") "
                    + columnName + " FROM " + table + " " + context.getConditions(false) + " AND (" + q.getQuery() + ")", mpv5.usermanagement.MPSecurityManager.VIEW, null);
        } else {
            data = freeSelectQuery("SELECT "
                    + columnName + " FROM " + table + " " + context.getConditions(false) + " AND (" + q.getQuery() + ")", mpv5.usermanagement.MPSecurityManager.VIEW, null);
        }
        if (data.getData().length == 0) {
            throw new NodataFoundException();
        } else {
            return data.getData();
        }
    }

    /**
     * Set the context for this connection, usually not used as the Context is
     * set on Clone
     *
     * @param context
     * @return
     */
    public QueryHandler setContext(Context context) {
        table = context.getDbIdentity();
        if (DatabaseConnection.getPrefix() != null && DatabaseConnection.getPrefix().equals("null")) {
            table = DatabaseConnection.getPrefix() + table;
        }
        this.context = context;
        return this;
    }

    /**
     * Select the row with this IDS
     *
     * @param id
     * @return
     * @throws NodataFoundException If no such row exists
     */
    protected ReturnValue select(int id) throws NodataFoundException {
        return select(id, true);
    }

    /**
     * Select the row with this IDS
     *
     * @param id
     * @param noConditions
     * @return
     * @throws NodataFoundException If no such row exists <i><b>Omits trashed
     * datasets implicitly</b></i>
     */
    protected ReturnValue select(int id, boolean noConditions) throws NodataFoundException {
        ReturnValue data;
        if (noConditions) {
            data = freeSelectQuery("SELECT * FROM " + table + " WHERE " + table + ".ids = " + id, mpv5.usermanagement.MPSecurityManager.VIEW, null);
        } else {
            data = freeSelectQuery("SELECT * FROM " + table + " WHERE " + table + ".ids = " + id + " AND " + context.getConditions(false).substring(6, context.getConditions(false).length()), mpv5.usermanagement.MPSecurityManager.VIEW, null);
        }
        if (data.getData().length == 0) {
            throw new NodataFoundException(context, id);
        } else {
            return data;
        }
    }

    /**
     * Selects one or more columns from the current {@link Context}- No
     * condition checking!
     *
     * @param columns column1, column2, column3...
     * @return
     * @throws NodataFoundException
     */
    public ReturnValue freeSelect(String columns) throws NodataFoundException {
        ReturnValue data = freeSelectQuery("SELECT " + columns + " FROM " + table, mpv5.usermanagement.MPSecurityManager.VIEW, null);
        if (data.getData().length == 0) {
            throw new NodataFoundException(context);
        } else {
            return data;
        }
    }

    /**
     * This is a convenience method to retrieve data such as
     * <code>select("*", criterias.getKeys(), criterias.getValues())<code/>
     *
     * @param columns
     * @param criterias
     * @return
     * @throws NodataFoundException
     */
    public Object[][] select(String columns, QueryCriteria criterias) throws NodataFoundException {
        if (criterias.getKeys().length > 0) {
            return select(columns, criterias.getKeys(), criterias.getValues(), criterias.getIncludeInvisible());
        } else {
            return select(columns, criterias.getIncludeInvisible());
        }
    }

    /**
     * This is a convenience method to retrieve data such as
     * <code>select("*", criterias.getKeys(), criterias.getValues())<code/>
     *
     * @param columns
     * @return
     * @throws NodataFoundException
     */
    public Object[][] select(String columns, boolean withDeleted) throws NodataFoundException {
        return select(columns, new String[0], new Object[0], withDeleted);
    }

    /**
     * 0
     * Select data from a timeframe
     *
     * @param columns column1, column2, column3...
     * @param criterias
     * @param time
     * @param timeCol The column containing the date
     * @return
     * @throws NodataFoundException <i><b>Omits trashed datasets
     * implicitly</b></i>
     */
    public Object[][] select(String columns, QueryCriteria criterias, vTimeframe time, String timeCol) throws NodataFoundException {

        String dateCriterium = table + "." + timeCol + " >= '" + DateConverter.getSQLDateString(time.getStart()) + "' AND " + table + "." + timeCol + " <= '" + DateConverter.getSQLDateString(time.getEnd()) + "'";
        String query = "SELECT " + columns + " FROM " + table + " " + context.getReferences() + (criterias.getKeys().length > 0 ? " WHERE " : "");

        for (int i = 0; i < criterias.getKeys().length; i++) {
            Object object = criterias.getValues()[i];
            String column = criterias.getKeys()[i];
            query += table + "." + column + "=" + String.valueOf(object);

            if ((i + 1) != criterias.getValues().length) {
                query += " AND ";
            }
        }
        if (criterias.getKeys().length > 0 && !query.endsWith("AND ")) {
            query += " AND ";
        }
        query += context.getConditions(false).substring(6, context.getConditions(false).length()) + " AND ";
        query += dateCriterium;
        query += criterias.getOrder();
        ReturnValue p = freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null);
        if (p.hasData()) {
            return p.getData();
        } else {
            throw new NodataFoundException(context);
        }
    }

    /**
     *
     * @param columns column1, column2, column3...
     * @param criterias
     * @param time
     * @param timeCol
     * @return
     * @throws NodataFoundException
     */
    public ReturnValue select(String columns, QueryCriteria2 criterias, vTimeframe time, String timeCol) throws NodataFoundException {
        String dateCriterium = table + "." + timeCol + " >= '" + DateConverter.getSQLDateString(time.getStart()) + "' AND " + table + "." + timeCol + " <= '" + DateConverter.getSQLDateString(time.getEnd()) + "'";
        String query = "SELECT " + columns + " FROM " + table + " " + context.getReferences() + " WHERE ";

        if (criterias.getQuery().length() > 6) {
            query += criterias.getQuery() + " AND ";
        }
        query += context.getConditions(criterias.getIncludeInvisible()).substring(6, context.getConditions(criterias.getIncludeInvisible()).length()) + " AND ";
        query += dateCriterium;
        query += criterias.getOrder();
        ReturnValue p = freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null);
        if (p.hasData()) {
            return p;
        } else {
            throw new NodataFoundException(context);
        }
    }

    /**
     *
     * @param columns column1, column2, column3...
     * @param criterias
     * @return
     * @throws NodataFoundException
     */
    public ReturnValue select(String columns, QueryCriteria2 criterias) throws NodataFoundException {
        String query = "SELECT " + columns + " FROM " + table + " " + context.getReferences() + " WHERE ";

        if (criterias.getQuery().length() > 6) {
            query += criterias.getQuery() + " AND ";
        }
        query += context.getConditions(criterias.getIncludeInvisible()).substring(6, context.getConditions(criterias.getIncludeInvisible()).length());
        query += criterias.getOrder();
        ReturnValue p = freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null);
        if (p.hasData()) {
            return p;
        } else {
            throw new NodataFoundException(context);
        }
    }

    /**
     *
     * @param columns column1, column2, column3...
     * @param criterias
     * @return
     * @throws NodataFoundException
     */
    public List<Integer> selectIds(QueryCriteria2 criterias) throws NodataFoundException {
        ReturnValue data = select("ids", criterias);
        if (data.getData().length == 0) {
            throw new NodataFoundException(context);
        } else {
            List<Integer> l = new ArrayList<Integer>();
            List<Object[]> d = data.getDataAsList();
            try {
                for (int i = 0; i < d.size(); i++) {
                    l.add(Integer.valueOf(d.get(i)[0].toString()));
                }
            } catch (Exception exception) {
                Log.Debug(exception);
            } finally {
                return l;
            }
        }
    }

    /**
     *
     * @param columns column1, column2, column3...
     * @param criterias
     * @return
     * @throws NodataFoundException
     */
    public List<Integer> selectIds(QueryCriteria criterias) throws NodataFoundException {
        Object[][] data = select("ids", criterias);
        if (data.length == 0) {
            throw new NodataFoundException(context);
        } else {
            List<Integer> l = new ArrayList<Integer>();
            try {
                for (int i = 0; i < data.length; i++) {
                    l.add(Integer.valueOf(data[i][0].toString()));
                }
            } catch (Exception exception) {
                Log.Debug(exception);
            } finally {
                return l;
            }
        }
    }

    /**
     * Requires 'dateadded' column
     *
     * @param columns column1, column2, column3...
     * @param criterias
     * @param time
     * @return
     * @throws NodataFoundException
     */
    public Object[][] select(String columns, QueryCriteria criterias, vTimeframe time) throws NodataFoundException {
        return select(columns, criterias, time, "dateadded");
    }

    /**
     * Requires 'dateadded' column
     *
     * @param columns
     * @param criterias
     * @param time
     * @return
     * @throws NodataFoundException
     */
    public ReturnValue select(String columns, QueryCriteria2 criterias, vTimeframe time) throws NodataFoundException {
        return select(columns, criterias, time, "dateadded");
    }

    /**
     * Convenience method to retrieve * from where the criterias match
     *
     * @param criterias
     * @return
     * @throws mpv5.db.common.NodataFoundException
     */
    public ReturnValue select(QueryCriteria criterias) throws NodataFoundException {
        String query = "SELECT * FROM " + table + " " + context.getReferences() + " WHERE ";
        for (int i = 0; i < criterias.getValues().length; i++) {

            Object object = criterias.getValues()[i];
            String column = criterias.getKeys()[i];
            query += column + "=" + String.valueOf(object);

            if ((i + 1) != criterias.getValues().length) {
                query += " AND ";
            } else {
                query += " AND " + context.getConditions(criterias.getIncludeInvisible()).substring(6, context.getConditions(criterias.getIncludeInvisible()).length());
            }
        }

        query += criterias.getOrder();
        ReturnValue data = freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null);
        if (!data.hasData()) {
            throw new NodataFoundException(context);
        } else {
            return data;
        }
    }

    /**
     * This is a convenience method to retrieve data such as "SELECT * FROM
     * table"
     *
     * @return All rows in the current context
     * @throws NodataFoundException
     */
    public ReturnValue select(boolean includeDeleted) throws NodataFoundException {
        ReturnValue data = freeSelectQuery("SELECT * FROM " + table + " " + context.getConditions(includeDeleted), mpv5.usermanagement.MPSecurityManager.VIEW, null);
        if (data.getData().length == 0) {
            throw new NodataFoundException(context);
        } else {
            return data;
        }
    }

    /**
     * This is a convenience method to retrieve data such as "SELECT ids FROM
     * table"
     *
     * @return All rows in the current context
     * @throws NodataFoundException
     */
    public List<Integer> selectIds(boolean includeDeleted) throws NodataFoundException {
        ReturnValue data = freeSelectQuery("SELECT ids FROM " + table + " " + context.getConditions(includeDeleted), mpv5.usermanagement.MPSecurityManager.VIEW, null);
        if (data.getData().length == 0) {
            throw new NodataFoundException(context);
        } else {
            List<Integer> l = new ArrayList<Integer>();
            List<Object[]> d = data.getDataAsList();
            try {
                for (int i = 0; i < d.size(); i++) {
                    l.add(Integer.valueOf(d.get(i)[0].toString()));
                }
            } catch (Exception exception) {
                Log.Debug(exception);
            } finally {
                return l;
            }
        }
    }

    /**
     *
     * @param columns If null, the column specified with "needle" is returned
     * @param needle
     * @param value
     * @param exactMatch
     * @return <i><b>Omits trashed datasets implicitly</b></i>
     */
    public Object[] getValuesFor(String[] columns, String needle, String value, boolean exactMatch) {
        String cols = needle;
        if (columns != null) {
            cols = "";
            for (int i = 0; i < columns.length; i++) {
                String string = columns[i];
                cols += string + ",";
            }
            cols = cols.substring(0, cols.length() - 1);
        }
        String f = " = '";
        String g = "'";

        if (!exactMatch) {
            f = " LIKE '%";
            g = "%'";
        }

        if (context != null) {
            if (value == null) {
                return ArrayUtilities.ObjectToSingleColumnArray(freeSelectQuery("SELECT " + cols + " FROM " + table + " " + context.getReferences() + " WHERE " + context.getConditions(false).substring(6, context.getConditions(false).length()), mpv5.usermanagement.MPSecurityManager.VIEW, null).getData());
            } else {
                return ArrayUtilities.ObjectToSingleColumnArray(freeSelectQuery("SELECT " + cols + " FROM " + table + " " + context.getReferences() + " WHERE " + needle + f + value + g + " AND " + context.getConditions(false).substring(6, context.getConditions(false).length()), mpv5.usermanagement.MPSecurityManager.VIEW, null).getData());
            }
        } else if (value == null) {
            return ArrayUtilities.ObjectToSingleColumnArray(freeSelectQuery("SELECT " + cols + " FROM " + table + " " + context.getReferences(), mpv5.usermanagement.MPSecurityManager.VIEW, null).getData());
        } else {
            return ArrayUtilities.ObjectToSingleColumnArray(freeSelectQuery("SELECT " + cols + " FROM " + table + " " + context.getReferences() + "  WHERE " + needle + f + value + g, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData());
        }
    }

    /**
     *
     * @param what column1, column2, column3...
     * @param where
     * @param datecolumn
     * @param zeitraum
     * @return
     */
    public Object[][] select(String what, String[] where, String datecolumn, vTimeframe zeitraum) {
        String dateCriterium = datecolumn + " >= '" + DateConverter.getSQLDateString(zeitraum.getStart()) + "' AND " + datecolumn + " <= '" + DateConverter.getSQLDateString(zeitraum.getEnd()) + "'";
        String query;
        if (where != null) {
            query = "SELECT " + what + " FROM " + table + " WHERE " + where[0] + " = " + where[2] + where[1] + where[2] + " AND " + dateCriterium;
        } else {
            query = "SELECT " + what + " FROM " + table + "  WHERE " + dateCriterium;
        }
        return freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
    }

    /**
     *
     * @param what column1, column2, column3...
     * @param where
     * @param zeitraum
     * @param additionalCondition
     * @return
     */
    public ArrayList<Double> selectYearlySums(String what, String[] where, vTimeframe zeitraum, String additionalCondition) {

        Date temdate = zeitraum.getStart();
        ArrayList<Double> values = new ArrayList<java.lang.Double>();
        String query;
        do {
            String str = "AND datum BETWEEN '" + DateConverter.getSQLDateString(temdate) + "' AND '" + DateConverter.getSQLDateString(DateConverter.addYear(temdate)) + "'";

            if (where != null) {
                query = "SELECT SUM(" + what + ") FROM " + table + " WHERE " + where[0] + " = " + where[2] + where[1] + where[2] + " " + "  " + str + " " + additionalCondition;
            } else {
                query = "SELECT SUM(" + what + ") FROM " + table + "  " + str + " " + additionalCondition;
            }

            Object[][] o = freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
            if (o != null && o[0][0] != null && !o[0][0].equals("null")) {
                values.add(Double.valueOf(String.valueOf(o[0][0])));
            } else {
                values.add(0d);
            }
            temdate = DateConverter.addMonth(temdate);
        } while (temdate.before(zeitraum.getEnd()));

        return values;

    }

    /**
     *
     * @param what column1, column2, column3...
     * @param where
     * @param zeitraum
     * @param additionalCondition
     * @return
     */
    public ArrayList<Double> selectMonthlySums(String what, String[] where, vTimeframe zeitraum, String additionalCondition) {

        Date temdate = zeitraum.getStart();
        ArrayList<Double> values = new ArrayList<java.lang.Double>();
        String query;
        do {
            String str = "AND datum BETWEEN '" + DateConverter.getSQLDateString(temdate) + "' AND '" + DateConverter.getSQLDateString(DateConverter.addMonth(temdate)) + "'";

            if (where != null) {
                query = "SELECT SUM(" + what + ") FROM " + table + " WHERE " + where[0] + " = " + where[2] + where[1] + where[2] + " " + "  " + str + " " + additionalCondition;
            } else {
                query = "SELECT SUM(" + what + ") FROM " + table + "  " + str + " " + additionalCondition;
            }

            Object[][] o = freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
            if (o != null && o[0][0] != null && !o[0][0].equals("null")) {
                Log.Debug(this, "Summe: " + o[0][0]);
                values.add(Double.valueOf(String.valueOf(o[0][0])));
            } else {
                Log.Debug(this, "Summe: " + 0);
                values.add(0d);
            }
            temdate = DateConverter.addMonth(temdate);
        } while (temdate.before(zeitraum.getEnd()));

        return values;

    }

    /**
     *
     * @param what column1, column2, column3...
     * @param where
     * @param leftJoinTable
     * @param leftJoinKey
     * @param order
     * @param like
     * @return
     */
    public Object[][] select(String what, String[] where, String leftJoinTable, String leftJoinKey, String order, Boolean like) {
//        start();
        String query;
        String l1 = "";
        String l2 = "";
        String k = " = ";
        String j = "";

        String wher = "";
        java.util.Date date;

        if (like != null) {
            if (like) {
                if (where != null && where[0].endsWith("datum")) {
                    k = " BETWEEN ";
                    date = DateConverter.getDate(where[1]);
                    where[1] = "'" + DateConverter.getSQLDateString(date) + "'" + " AND " + "'" + DateConverter.getSQLDateString(DateConverter.addMonth(date)) + "'";
                    where[2] = " ";
                } else {
                    l1 = "%";
                    l2 = "%";
                    k = " LIKE ";
                }
            }
        }

        if (where != null) {

            query = "SELECT " + what + " FROM " + table
                    + " LEFT OUTER JOIN " + leftJoinTable + " ON " + table + "." + leftJoinKey + " = " + leftJoinTable + ".ids"
                    + " WHERE " + table + "." + where[0] + " " + k + " " + where[2] + l1 + where[1] + l2 + where[2] + " ORDER BY " + table + "." + order;
        } else {
            query = "SELECT " + what + " FROM " + table
                    + " LEFT OUTER JOIN  " + leftJoinTable + " ON "
                    + table + "." + leftJoinKey + " = " + leftJoinTable + ".ids "
                    + "  ORDER BY " + table + "." + order;
        }

        return freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
    }

    /**
     * Count rows in a time frame
     *
     * @param date1
     * @param date2
     * @return
     */
    public int selectCountBetween(java.util.Date date1, java.util.Date date2) throws SQLException {
        return selectCount("dateadded", "BETWEEN '" + DateConverter.getSQLDateString(date1) + "' AND '" + DateConverter.getSQLDateString(date2) + "'");
    }

    /**
     * Set the table for the current connection. Usually only used inside the
     * <code>Clone</code> method of {@link QueryHandler}
     *
     * @param newTable
     */
    protected void setTable(String newTable) {
        this.table = newTable;
        if (DatabaseConnection.getPrefix() != null && DatabaseConnection.getPrefix().equals("null")) {
            this.table = DatabaseConnection.getPrefix() + table;
        }
    }

    /**
     * Sets the table and the context for the current connection. Don't use
     * this.
     *
     * @param newTable
     */
    public void setTable2(String newTable) {
        this.table = newTable;
        if (DatabaseConnection.getPrefix() != null && DatabaseConnection.getPrefix().equals("null")) {
            this.table = DatabaseConnection.getPrefix() + table;
        }
        this.context = Context.getMatchingContext(newTable);
        if (this.context == null) {
            this.context = Context.DEFAULT;
            this.context.setDbIdentity(newTable);
        }
    }

    /**
     * The current table name as specified by the current {@link Context}, or {@link #setTable(java.lang.String)
     * }, {@link #setTable2(java.lang.String) }
     *
     * @return The current table name
     */
    public String getTable() {
        return table;
    }

    /**
     * Set a wait-cursor during database transactions (if not cloned for silent
     * transactions)
     *
     * @param main A frame window
     */
    public static void setWaitCursorFor(JFrame main) {
        comp = main;
    }

    /**
     * Set a progressbar during database transactions
     *
     * @param progressBar
     */
    public static void setProgressbar(JProgressBar progressBar) {
        progressbar = progressBar;
    }

    /**
     * This will flush the table of the current context, be careful! This should
     * never be triggered by a user, the user right will not be checked!
     *
     * @param dbIdentity
     */
    public void truncate(String dbIdentity) {
        freeQuery(table, MPSecurityManager.SYSTEM_RIGHT, "Truncating table: " + table);
    }

    /**
     * Checks the uniqueness of the data
     *
     * @param vals
     * @param uniquecols
     * @return
     */
    public boolean checkUniqueness(QueryData vals, int[] uniquecols) {
        String[] values = vals.getKeys();

        if (uniquecols != null) {
            for (int i = 0; i < uniquecols.length; i++) {
                int j = uniquecols[i];
                Object[][] val = select(values[j], new String[]{values[j], vals.getValue(values[j]).toString(), vals.getValue(values[j]).getWrapper()});
                if (val != null && val.length > 0) {
                    mpv5.YabsViewProxy.instance().addMessage(Messages.VALUE_ALREADY_EXISTS + vals.getValue(values[j]).toString(), Color.RED);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks the uniqueness of STRING data
     *
     * @param column
     * @param value
     * @return
     */
    public boolean checkUniqueness(String column, String value) {
        QueryData t = new QueryData();
        t.add(column, value);
        return checkUniqueness(t, new int[]{0});
    }
    private static volatile int RUNNING_JOBS = 0;
    private static Thread JOB_WATCHDOG;

    /**
     * Invoked after running a database query (usually done automatically)
     */
    protected synchronized void stop() {
        if (!runInBackground) {
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    try {//Avoid Cursor flickering
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        mpv5.logging.Log.Debug(ex);//Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (RUNNING_JOBS <= 1) {
                        comp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        progressbar.setValue(0);
                        progressbar.setIndeterminate(false);
                    }
                    RUNNING_JOBS--;
                }
            };
            SwingUtilities.invokeLater(runnable);
        }
    }

    private synchronized void start() {
        if (JOB_WATCHDOG == null) {
            JOB_WATCHDOG = new Thread(new Watchdog());
            JOB_WATCHDOG.start();
        }
        if (!runInBackground) {
            RUNNING_JOBS++;
            if (RUNNING_JOBS > 5) {
                comp.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            }
            progressbar.setMaximum(RUNNING_JOBS);
        }
    }

    /**
     * Returns exactly one value from a column
     *
     * @param columnName The column where to take the result from
     * @param compareColumn The column to compare
     * @param compareValue The value to compare to
     * @return A value
     * @throws NodataFoundException
     */
    public Object getValue(String columnName, String compareColumn, Object compareValue) throws NodataFoundException {
        String quote = "";
        if (compareValue instanceof String) {
            quote = "'";
        } else if (compareValue instanceof Date) {
            quote = "'";
            compareValue = DateConverter.getSQLDateString((Date) compareValue);
        }
        return selectLast(columnName, new String[]{compareColumn, compareValue.toString(), quote})[0];
    }

    /**
     * Returns map view of the found {@link DatabaseObject} with the given ID in
     * the current {@link Context}
     *
     * @param id
     * @return A HashMap
     * @throws NodataFoundException If no Object with the given ID was found in
     * the current Context
     */
    public Map<String, String> getValuesFor(int id) throws NodataFoundException {
        ReturnValue rv = select(id);
        String[] cols = rv.getColumnnames();
        Object[][] data = rv.getData();
        HashMap<String, String> map = new HashMap<String, String>(data[0].length);
        for (int i = 0; i < data[0].length; i++) {
            map.put(cols[i], String.valueOf(data[0][i]));
        }
        return map;
    }
    /**
     * This string is used to replace backslashes in sql queries (if escaping is
     * enabled)
     */
    public static String BACKSLASH_REPLACEMENT_STRING = "<removedbackslash>";

    private synchronized String escapeBackslashes(String query) {
        return query.replace("\\", BACKSLASH_REPLACEMENT_STRING);
    }

    private synchronized String rescapeBackslashes(String query) {
        return query.replace(BACKSLASH_REPLACEMENT_STRING, "\\");
    }

    /**
     * Checks if any data which would match the given criteria is existing in
     * the database
     *
     * @param qc
     * @return true if matching data was found
     */
    public boolean checkExistance(QueryCriteria2 qc) {
        try {
            return select("ids", qc, new vTimeframe(new Date(0), new Date())).hasData();
        } catch (NodataFoundException ex) {
            return false;
        }
    }

    private synchronized byte[] blobToByteArray(final Reader characterStream) throws SQLException, IOException {
        //byte[] is for BLOB data (or char[]?)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(baos, "utf-8");
        char[] buffer = new char[4096];
        for (int count = 0; (count = characterStream.read(buffer)) != -1;) {
            writer.write(buffer, 0, count);
        }
        writer.close();
        return baos.toByteArray();
    }
    private static String ivpquery = "INSERT INTO " + Context.getValueProperties().getDbIdentity()
            + "(value, cname, classname, objectids, contextids, intaddedby, dateadded, groupsids )"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static String uvpquery = "UPDATE " + Context.getValueProperties().getDbIdentity() + " SET "
            + "value = ?, "
            + "cname= ?, "
            + "classname= ?, "
            + "objectids= ?, "
            + "contextids= ?, "
            + "intaddedby= ?, "
            + "dateadded= ?, "
            + "groupsids= ? "
            + "WHERE " + Context.getValueProperties().getDbIdentity() + ".ids = ?";

    private void createPs() {
        try {
            ivpps = sqlConn.prepareStatement(ivpquery, PreparedStatement.RETURN_GENERATED_KEYS);
            uvpps = sqlConn.prepareStatement(uvpquery, PreparedStatement.RETURN_GENERATED_KEYS);
        } catch (SQLException ex) {
            Log.Debug(this, ex.getMessage());
        }
    }

    class Watchdog implements Runnable {

        @Override
        @SuppressWarnings("SleepWhileInLoop")
        public void run() {
            int oldValue = 0;
            while (true) {
                if (RUNNING_JOBS != oldValue) {
                    progressbar.setValue(RUNNING_JOBS);
                    oldValue = RUNNING_JOBS;
                }
                try {
                    Thread.sleep(33);
                } catch (InterruptedException ex) {
                    mpv5.logging.Log.Debug(ex);
                }
            }
        }
    }

    /**
     * Count the rows of the current table
     *
     * @return
     * @throws SQLException
     */
    public Integer getCount() throws SQLException {
        int i = selectCount(null, null);
        i = (i < 0) ? -i : i;
        return i;
    }

    /**
     * Insert values to db
     *
     * @param what : {set, value, "'"} this.insert("name,wert", "'Sprache
     * (Waehrung, z.B. Schweiz: de_CH' ,'de_DE'");
     * @param jobmessage The message to be displayed after a successful run
     * @return id of inserted row
     */
    public int insert(QueryData what, String jobmessage) {
        String query = query = "INSERT INTO " + table + " (" + what.getKeysString() + " ) VALUES (" + what.getValuesString() + ") ";
        return freeUpdateQuery(query, mpv5.usermanagement.MPSecurityManager.CREATE_OR_DELETE, jobmessage).getId();
    }

    /**
     * Does an insert
     *
     * @param blobData [columnName, blobData]
     * @param data
     * @param jobmessage
     * @return The id of the inserted row
     */
    public int insertValueProperty(InputStream blobData, QueryData data, String jobmessage) {
        if (ivpps == null) {
            createPs();
        }
        ResultSet keys;
        int id = -1;

        try {
            start();
            ivpps.setBlob(1, blobData);
            ivpps.setString(2, data.getValue("cname").toString());
            ivpps.setString(3, data.getValue("classname").toString());
            ivpps.setInt(4, Integer.valueOf(data.getValue("objectids").toString()));
            ivpps.setInt(5, Integer.valueOf(data.getValue("contextids").toString()));
            ivpps.setInt(6, mpv5.db.objects.User.getCurrentUser().getID());
            ivpps.setDate(7, new java.sql.Date(new Date().getTime()));
            ivpps.setInt(8, Integer.valueOf(data.getValue("groupsids").toString()));
            ivpps.execute();
            if (!sqlConn.getAutoCommit()) {
                sqlConn.commit();
            }
            try {
                keys = ivpps.getGeneratedKeys();
                if (keys != null && keys.next()) {
                    id = keys.getInt(1);
                }
            } catch (SQLException sQLException) {
                Log.Debug(sQLException);
            }

        } catch (Exception ex) {
            Log.Debug(this, "Datenbankfehler: " + ivpquery);
            Log.Debug(this, ex);
            Popup.error(ex);
            jobmessage = Messages.ERROR_OCCURED.toString();
        } finally {
            stop();
        }
        if (viewToBeNotified != null) {
            viewToBeNotified.refresh();
        }
        if (jobmessage != null) {
            mpv5.YabsViewProxy.instance().addMessage(jobmessage);
        }

        return id;
    }

    /**
     * Does an insert
     *
     * @param ids
     * @param blobData [columnName, blobData]
     * @param data
     * @param jobmessage
     */
    public void updateValueProperty(int ids, InputStream blobData, QueryData data, String jobmessage) {

        if (uvpps == null) {
            createPs();
        }

        try {
            start();
            uvpps.setBlob(1, blobData);
            uvpps.setString(2, data.getValue("cname").toString());
            uvpps.setString(3, data.getValue("classname").toString());
            uvpps.setInt(4, Integer.valueOf(data.getValue("objectids").toString()));
            uvpps.setInt(5, Integer.valueOf(data.getValue("contextids").toString()));
            uvpps.setInt(6, mpv5.db.objects.User.getCurrentUser().getID());
            uvpps.setDate(7, new java.sql.Date(new Date().getTime()));
            uvpps.setInt(8, Integer.valueOf(data.getValue("groupsids").toString()));
            uvpps.setInt(9, ids);
            uvpps.execute();
            if (!sqlConn.getAutoCommit()) {
                sqlConn.commit();
            }
        } catch (Exception ex) {
            Log.Debug(this, "Datenbankfehler: " + uvpquery);
            Log.Debug(this, ex);
            Popup.error(ex);
            jobmessage = Messages.ERROR_OCCURED.toString();
        } finally {
            stop();
        }
        if (viewToBeNotified != null) {
            viewToBeNotified.refresh();
        }
        if (jobmessage != null) {
            mpv5.YabsViewProxy.instance().addMessage(jobmessage);
        }
    }

    /**
     * This is a special insert method for the History feature
     *
     * @param message
     * @param username
     * @param dbidentity
     * @param item
     * @param groupid
     */
    public synchronized void insertHistoryItem(String message, String username, String dbidentity, int item, int groupid) {
        try {
            if (psHistory == null) {
                try {
                    String query = "INSERT INTO " + Context.getHistory().getDbIdentity() + " (cname, username, dbidentity, intitem, groupsids, dateadded) VALUES (?, ?, ?, ?, ?, ?)";
                    psHistory = sqlConn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                } catch (SQLException ex) {
                    mpv5.logging.Log.Debug(ex);//Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            psHistory.setString(1, message);
            psHistory.setString(2, username);
            psHistory.setString(3, dbidentity);
            psHistory.setInt(4, item);
            psHistory.setInt(5, groupid);
            psHistory.setDate(6, new java.sql.Date(new java.util.Date().getTime()));
            psHistory.execute();
        } catch (SQLException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    private static PreparedStatement psHistory;

    /**
     * This is a special insert method for the Lock feature
     *
     * @param context
     * @param id
     * @param user
     * @return
     * @throws UnableToLockException
     */
    protected synchronized boolean insertLock(Context context, int id, User user) throws UnableToLockException {
        try {
            if (psLock == null) {
                try {
                    String query = "INSERT INTO " + Context.getLock().getDbIdentity() + " (cname, rowid, usersids) VALUES (?, ?, ?)";
                    psLock = sqlConn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                } catch (SQLException ex) {
                    mpv5.logging.Log.Debug(ex);//Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            psLock.setString(1, context.getDbIdentity());
            psLock.setInt(2, id);
            psLock.setInt(3, user.__getIDS());
            return psLock.execute();
        } catch (SQLException ex) {
            throw new UnableToLockException(context, id, user);
        }
    }
    private static PreparedStatement psLock;

    protected void removeLock(Context context, int id, User user) {
        try {
            if (psUnLock == null) {
                try {
                    String query = "DELETE FROM " + Context.getLock().getDbIdentity() + " WHERE cname = ? AND rowid = ? AND usersids = ?";
                    psUnLock = sqlConn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                } catch (SQLException ex) {
                    mpv5.logging.Log.Debug(ex);//Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            psUnLock.setString(1, context.getDbIdentity());
            psUnLock.setInt(2, id);
            psUnLock.setInt(3, user.__getIDS());
            psUnLock.execute();
        } catch (SQLException ex) {
            Log.Debug(ex);
        }
    }
    private static PreparedStatement psUnLock;

    /**
     * Updates the given column at the row with the given id
     *
     * @param columnName
     * @param id
     * @param value
     */
    public void update(String columnName, Integer id, Object value) {
        QueryData d = new QueryData();
        d.add(columnName, value);
        update(d, new String[]{"ids", id.toString(), ""}, null);
    }

    /**
     *
     * @param what : {set, values}
     * @param where : {value, comparison, "'"}
     * @param jobmessage
     */
    public void update(QueryData what, String[] where, String jobmessage) {

        String query;
        String[] a = what.getKeys();
        String c = "";

        for (int i = 0; i < a.length; i++) {
            c += a[i] + " = " + what.getValue(a[i]).getWrapper() + what.getValue(a[i]).toString() + what.getValue(a[i]).getWrapper() + ", ";
        }

        if (c.length() > 2) {
            c = c.substring(0, c.length() - 2);
        }

        query = "UPDATE " + table + " SET " + c + " WHERE " + table + "." + where[0] + " = " + where[2] + where[1] + where[2];
        freeUpdateQuery(query, mpv5.usermanagement.MPSecurityManager.EDIT, jobmessage);
    }

    /**
     * Will throw an exception if the desired row to update doesnt exist.
     *
     * @param what The data
     * @param criteria
     * @param jobmessage
     * @throws NodataFoundException
     */
    public void update(QueryData what, QueryCriteria2 criteria, String jobmessage) throws NodataFoundException {

        String checkquery = "select ids from " + table + " WHERE " + criteria.getQuery();
        String query = "UPDATE " + table + " SET " + what + " WHERE " + criteria.getQuery();
        if (freeSelectQuery(checkquery, mpv5.usermanagement.MPSecurityManager.VIEW, jobmessage).hasData()) {
            freeUpdateQuery(query, mpv5.usermanagement.MPSecurityManager.EDIT, jobmessage);
        } else {
            Log.Debug(this, "No data for : " + checkquery);
            throw new NodataFoundException(context);
        }
    }

    /**
     * Will create the row if the desired row to update doesnt exist.
     *
     * @param what The data
     * @param criteria
     * @param jobmessage
     */
    public void updateOrCreate(QueryData what, QueryCriteria2 criteria, String jobmessage) {
        String query = "UPDATE " + table + " SET " + what + " WHERE " + criteria.getQuery();
        if (freeUpdateQuery(query, mpv5.usermanagement.MPSecurityManager.EDIT, jobmessage).getUpdateCount() > 0) {
        } else {
            Log.Debug(this, "Need to create " + what + " on " + criteria.getQuery());
            what.add(criteria);
            insert(what, jobmessage);
        }
    }

    /**
     *
     * @param q The data
     * @param criteria Only the "ids" criterium will be used
     * @param jobmessage
     */
    public void update(QueryData q, QueryCriteria criteria, String jobmessage) {
        update(q, Integer.valueOf(criteria.getValue("ids").toString()), jobmessage);
    }

    /**
     *
     * @param q The data
     * @param doId The row id
     * @param jobmessage
     */
    public void update(QueryData q, int doId, String jobmessage) {
        update(q, new String[]{"ids", String.valueOf(doId), ""}, jobmessage);
    }

    /**
     *
     * @param what
     * @param where : {value, comparison, "'"}
     * @return last matching result as string array
     * @throws NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public Object[] selectLast(String what, String[] where) throws NodataFoundException {

        Object[][] data = select(what, where, what, false);

        if (data == null || data.length == 0) {
            throw new NodataFoundException();
        } else {
            return data[0];
        }
    }

    /**
     * if "where" is "null", everything is selected (without "where" -clause)
     *
     * @param what
     * @param where : {value, comparison, "'"}
     * @return first matching result as string array
     * @throws NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public Object[] selectFirst(String what, String[] where) throws NodataFoundException {

        Object[][] data = select(what, where, what, false);

        if (data == null || data.length == 0) {
            throw new NodataFoundException();
        } else {
            return data[data.length - 1];
        }
    }

    /**
     * if "where" is "null", everything is selected (without "where" -clause)
     *
     * @param what
     * @param where : {value, comparison, "'"}
     * @param searchFoLike
     * @return first matching result as string array
     * @throws NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public Object[] selectFirst(String what, String[] where, boolean searchFoLike) throws NodataFoundException {

        Object[][] data = select(what, where, what, searchFoLike);

        if (data == null || data.length == 0) {
            throw new NodataFoundException();
        } else {
            return data[data.length - 1];
        }
    }

    /**
     * if "where" is "null", everything is selected (without "where" -clause)
     *
     * @param what
     * @param where : {value, comparison, "'"}
     * @param searchFoLike
     * @return last matching result as string array
     * @throws NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public Object[] selectLast(String what, String[] where, boolean searchFoLike) throws NodataFoundException {

        Object[][] data = select(what, where, what, searchFoLike);

        if (data == null || data.length == 0) {
            throw new NodataFoundException();
        } else {
            return data[0];
        }
    }

    /**
     *
     * @param what
     * @param where
     * @param leftJoinTable
     * @param leftJoinKey
     * @param order
     * @return results as multidimensional string array
     */
    @SuppressWarnings("unchecked")
    public Object[][] select(String what, String[] where, String leftJoinTable, String leftJoinKey, String order) {
        return select(what, where, leftJoinTable, leftJoinKey, order, null);
    }

    /**
     *
     * @param what
     * @param where
     * @param leftJoinTable
     * @param leftJoinKey
     * @return results as multidimensional string array
     */
    @SuppressWarnings({"unchecked", "unchecked"})
    public Object[][] select(String what, String[] where, String leftJoinTable, String leftJoinKey) {
        return select(what, where, leftJoinTable, leftJoinKey, "id", null);
    }

    /**
     *
     * @param what
     * @param where : {value, comparison, "'"}
     * @return results as multidimensional string array <i><b>Omits trashed
     * datasets implicitly</b></i>
     */
    @SuppressWarnings("unchecked")
    public Object[][] select(String what, String[] where) {
//        start();
        String query;
        if (where != null && where[0] != null && where[1] != null) {
            query = "SELECT " + what + " FROM " + table + " " + context.getReferences() + " WHERE " + table + "." + where[0] + " = " + where[2] + where[1] + where[2] + " AND " + context.getConditions(false).substring(6, context.getConditions(false).length());
        } else {
            query = "SELECT " + what + " FROM " + table + " " + context.getReferences() + " WHERE " + context.getConditions(false).substring(6, context.getConditions(false).length());
        }
        return freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
    }

    /**
     *
     * @param what
     * @param where : {value, comparison, "'"}
     * @param order
     * @param limit
     * @return results as multidimensional string array <i><b>Omits trashed
     * datasets implicitly</b></i>
     */
    @SuppressWarnings("unchecked")
    public Object[][] select(String what, String[] where, String order, int limit) {
        if (limit > 0) {
            setLimit(limit);
        }
        String query;
        if (where != null && where[0] != null && where[1] != null) {
            query = "SELECT " + what + " FROM " + table + " " + context.getReferences() + " WHERE " + table + "." + where[0] + " = " + where[2] + where[1] + where[2] + " AND " + context.getConditions(false).substring(6, context.getConditions(false).length()) + (order != null ? " ORDER BY " + order : "");
        } else {
            query = "SELECT " + what + " FROM " + table + " " + context.getReferences() + " WHERE " + context.getConditions(false).substring(6, context.getConditions(false).length()) + (order != null ? " ORDER BY " + order : "");
        }
        return freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
    }

    /**
     *
     * @param what
     * @param whereColumns {"column1","column2"}
     * @param haveValues {"value1",value2<any/>}
     * @return <i><b>Omits trashed datasets implicitly</b></i>
     */
    public Object[][] select(String what, String[] whereColumns, Object[] haveValues, boolean withDeleted) {
        String query = "SELECT " + what + " FROM " + table + " " + context.getReferences() + (whereColumns.length > 0 ? " WHERE " : "");
        for (int i = 0; i < haveValues.length; i++) {
            Object object = haveValues[i];
            String column = whereColumns[i];
            query += table + "." + column + "=" + String.valueOf(object);

            if ((i + 1) != haveValues.length) {
                query += " AND ";
            } else {
                query += " AND " + context.getConditions(false).substring(6, context.getConditions(withDeleted).length());
            }
        }
        return freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
    }

    /**
     *
     * @param what
     * @param where : {value, comparison, "'"}
     * @param order
     * @param like - datum will be returned between given and given + 1 month
     * @return results as multidimensional string array <i><b>Omits trashed
     * datasets implicitly</b></i>
     */
    @SuppressWarnings("unchecked")
    public Object[][] select(String what, String[] where, String order, boolean like) {
//        start();
        String l1 = "";
        String l2 = "";
        String condition = " = ";
        String j = "";

        if (order == null) {
            order = "ids ";
        }

        String ord = " ORDER BY " + table + "." + order;
        String wher = "";
        java.util.Date date;


        if (like) {
            if (where != null && where[0].endsWith("datum")) {
                condition = " BETWEEN ";
                date = DateConverter.getDate(where[1]);
                where[1] = "'" + DateConverter.getSQLDateString(date) + "'" + " AND " + "'" + DateConverter.getSQLDateString(DateConverter.addMonth(date)) + "'";
                where[2] = " ";
            } else {
                l1 = "%";
                l2 = "%";
                condition = " LIKE ";
            }
        }

        if (where == null) {
            wher = "  " + context.getConditions(false);
        } else {
            if (!like) {
                wher = " WHERE " + table + "." + where[0] + " " + condition + " " + where[2] + l1 + where[1] + l2 + where[2] + " AND " + context.getConditions(false).substring(6, context.getConditions(false).length()) + " ";
            } else {
                wher = " WHERE UPPER(" + table + "." + where[0] + ") " + condition + " " + where[2] + l1 + where[1].toUpperCase() + l2 + where[2] + " AND " + context.getConditions(false).substring(6, context.getConditions(false).length()) + " ";
            }

        }
        String query = "SELECT " + what + " FROM " + table + " " + context.getReferences() + wher + ord;

        return freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
    }

    /**
     * Creates a {@link PreparedStatement}
     *
     * @param columns
     * @param conditionColumns
     * @param order
     * @param like
     * @return A {@link PreparedStatement}
     * @throws SQLException
     */
    public PreparedStatement buildPreparedSelectStatement(String columns[], String[] conditionColumns, String order, boolean like) throws SQLException {

        return sqlConn.prepareStatement(buildQuery(columns, conditionColumns, order, like, "OR"), PreparedStatement.RETURN_GENERATED_KEYS);
    }

    /**
     * Executes the given statement
     *
     * @param statement
     * @param values Length must match the conditionColumns argument of the
     * build call of the statement
     * @return
     * @throws java.sql.SQLException <i><b>Omits trashed datasets
     * implicitly</b></i>possible not returning the desired results yet
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public ReturnValue executeStatement(PreparedStatement statement, Object[] values) throws SQLException {

        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                Object object = values[i];
                statement.setObject(i + 1, object);
            }
        }

        ResultSet set = statement.executeQuery();
        ReturnValue val = new ReturnValue();
        ArrayList spalten = new ArrayList();
        ArrayList zeilen = new ArrayList
