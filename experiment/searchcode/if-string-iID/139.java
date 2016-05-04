package RaceLibrary;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.sql.*;
import java.io.*;
import java.lang.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Brian
 */
public class RaceDatabase {

    private static final boolean DEBUG = false;

    public Connection dbConn = null;

    String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    String dbName = "undefinedRace";
    String connectionURL;

    public int connStatus;

    public static final int DBCONN_ESTABLISHED                  = 1;
    public static final int DBCONN_NOT_ESTABLISHED              = 0;
    public static final int DBCONN_ERROR                        = -1;

    protected static final PrintStream o = System.out;

    public static final int DB_CFGID_VERSION                    = 1;
    public static final int DB_CFGID_TITLE                      = 50;
    public static final int DB_CFGID_ORG_NAME                   = 51;
    public static final int DB_CFGID_COMPORT                    = 110;
    public static final int DB_CFGID_IPADDRESS                  = 110;
    public static final int DB_CFGID_MAXTIME                    = 170;
    public static final int DB_CFGID_NLANES                     = 210;
    public static final int DB_CFGID_PERLANE                    = 220;
    public static final int DB_CFGID_SCORING_METHOD             = 230;
    public static final int DB_CFGID_NGROUPS                    = 300;
    public static final int DB_CFGID_GROUP1_NAME                = 310;
    public static final int DB_CFGID_GROUP2_NAME                = 320;
    public static final int DB_CFGID_GROUP3_NAME                = 330;
    public static final int DB_CFGID_GROUP4_NAME                = 340;
    public static final int DB_CFGID_GROUP5_NAME                = 350;
    public static final int DB_CFGID_TRACK_LENGTH               = 410;
    public static final int DB_CFGID_SCALE_FACTOR_MPH           = 420;

    public static final int DB_CFGID_CURRENT_RACEID             = 500;

    public static final int DB_CFGID_BEST_IN_SHOW               = 801;
    public static final int DB_CFGID_MOST_UNUSUAL               = 802;
    
    
    public static final int DB_CFGID_ITEM_TREE_STATUS           = 4000;

    public static final int DB_RACETYPE_MAIN_EVENT              = 1;
    public static final int DB_RACETYPE_RUNOFF                  = 2;

    public RaceDatabase() {
        connStatus = DBCONN_NOT_ESTABLISHED;
        dbConn = null;
    }

    @Override public void finalize() {

        closeDatabase();

    }

    public String getName() {
        return dbName;
    }

    public int openDatabase(java.lang.String dbName) {
        return openDatabase(dbName,false);
    }

    public int openDatabase(java.lang.String dbName,boolean flagCreate) {

        connectionURL = "jdbc:derby:" + dbName.toLowerCase() + ";user=admin;password=password";
        if (flagCreate) connectionURL = connectionURL + ";create=true";

        try {
            Class.forName(driver);
        } catch(java.lang.ClassNotFoundException e) {
            System.out.println("Database driver load failed");
            return -1;
        }

    	try {
        	dbConn = DriverManager.getConnection(connectionURL);
            //checkForWarning (dbConn.getWarnings ());
        } catch (SQLException ex) {
        	// handle any errors
        	o.println("************ Database Connect Error ************");
        	o.println("SQLException: " + ex.getMessage());
        	o.println("SQLState: " + ex.getSQLState());
        	o.println("VendorError: " + ex.getErrorCode());
        	connStatus = DBCONN_ERROR;
            return -1;
    	}

        System.out.println("Java DB Started");
        this.dbName = dbName;
        connStatus = DBCONN_ESTABLISHED;

        return 0;
    }

    public int closeDatabase() {

        dbConn = null;

        System.out.println("Java DB Shutdown");
        return 0;
    }

    synchronized public ResultSet execute(String query) {
        Statement		stmtMY = null;
        ResultSet		rsMY   = null;

        try {
            if (dbConn == null) {
                System.err.println("RaceDatabase - execute() - dbConn is null");
                return null;
            }
            stmtMY  = dbConn.createStatement();
            if (DEBUG) System.out.println(query);
            if (stmtMY.execute(query)) {
                rsMY    = stmtMY.getResultSet();
            }
            else {
                return null;
            }
        }
        catch (SQLException ex) {
            if (!DEBUG) if (ex.getErrorCode() == 0) return null;
        	o.print(ex.getErrorCode());
        	o.print(" - ");
        	o.println(ex.getMessage());
                o.println("SQL Query: " + query);
        	return null;
        }

        return rsMY;
    }

    public void createRace(String rName) {

        if (openDatabase(rName,true) != 0) {
            System.out.print("Error - Open Database Failed - ");
            System.out.println(rName);
            return;
        }

        createStructure();

    }

    public void createStructure() {

        StructureReaderXML creator = new StructureReaderXML();
        creator.setDatabase(this);
        try {
            creator.parse();
        } catch (Exception ex) {
            Logger.getLogger(RaceDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public int getRaceConfigInt(int id) {
        int value = 0;
        
        String svalue = getRaceConfig(id);

        if (svalue == null) return value;
        if (svalue.isEmpty()) return value;

        Integer iValue = new Integer(svalue);
        
        return iValue.intValue();
    }

    public String getRaceConfig(int id) {

        Integer iID;
        String sql;
        String value = "";

        iID = new Integer(id);
        sql = "SELECT name,value FROM raceconfig WHERE cfgid=" + iID.toString();

        ResultSet rs = execute(sql);
        if (rs == null) return value;

        try {
            rs.next();
            value = rs.getString(2);
        } catch (SQLException ex) {
            value = "";
        }
        return value;
    }

    public int setRaceConfig(int id,String value) {

        Integer iID;
        String sql;

        iID = new Integer(id);
        sql = "SELECT count(*) FROM raceconfig WHERE cfgid=" + iID.toString();
        ResultSet rs = execute(sql);
        if (rs == null) return -1;

        int count = 0;
        try {
            rs.next();
            count = rs.getInt(1);
        } catch (SQLException ex) {

        }

        if (count > 0)
            sql = "UPDATE raceconfig SET value='" + value + "' WHERE cfgid=" + iID.toString();
        else
            sql = "INSERT INTO raceconfig (cfgid,subid,name,value) VALUES (" + iID.toString() + ",0,'n/a','" + value + "')";
        execute(sql);

        return 0;
    }

    public int getQueryCount(String table,String where) {

        String sql;

        sql = "SELECT count(*) FROM " + table;
        if (where != null) sql += " WHERE " + where;
        ResultSet rs = execute(sql);
        if (rs == null) return -1;

        int count = 0;
        try {
            rs.next();
            count = rs.getInt(1);
        } catch (SQLException ex) {

        }
        return count;
    }

}


