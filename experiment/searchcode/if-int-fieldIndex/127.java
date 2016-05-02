package com.hardcode.gdbms.driver.postgis;

import java.awt.geom.Rectangle2D;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;

import org.postgis.Geometry;
import org.postgis.LineString;
import org.postgis.LinearRing;
import org.postgis.MultiLineString;
import org.postgis.MultiPolygon;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.Polygon;
import org.postgresql.PGConnection;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSourceFactory;
import com.hardcode.gdbms.engine.data.db.JDBCSupport;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.hardcode.gdbms.engine.data.driver.SpatialDBDriver;
import com.hardcode.gdbms.engine.spatial.GeneralPath;
import com.hardcode.gdbms.engine.spatial.GeometryImpl;
import com.hardcode.gdbms.engine.values.Value;


/**
 *
 */
public class PostGISDriver implements SpatialDBDriver {
    private static Exception driverException;

    static {
        try {
            Class.forName("org.postgresql.Driver").newInstance();
        } catch (Exception ex) {
            driverException = ex;
        }
    }

    private JDBCSupport jdbcSupport;
    private String[] fieldNames;

    /**
     * DOCUMENT ME!
     *
     * @param p DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private GeneralPath polygon2GP(Polygon p) {
        GeneralPath gp = new GeneralPath();

        for (int r = 0; r < p.numRings(); r++) {
            gp.append(linearRing2GP(p.getRing(r)), false);
        }

        return gp;
    }

    /**
     * DOCUMENT ME!
     *
     * @param lr DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private GeneralPath linearRing2GP(LinearRing lr) {
        Point p;
        GeneralPath gp = new GeneralPath();

        if ((p = lr.getPoint(0)) != null) {
            gp.moveTo(p.x, p.y);

            for (int i = 1; i < lr.numPoints(); i++) {
                p = lr.getPoint(i);
                gp.lineTo(p.x, p.y);
            }
        }

        gp.closePath();

        return gp;
    }

    /**
     * DOCUMENT ME!
     *
     * @param ls DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private GeneralPath lineString2GP(LineString ls) {
        Point p;
        GeneralPath gp = new GeneralPath();

        if ((p = ls.getPoint(0)) != null) {
            gp.moveTo(p.x, p.y);

            for (int i = 1; i < ls.numPoints(); i++) {
                p = ls.getPoint(i);
                gp.lineTo(p.x, p.y);
            }
        }

        return gp;
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getConnection(java.lang.String,
     *      int, java.lang.String, java.lang.String, java.lang.String)
     */
    public Connection getConnection(String host, int port, String dbName,
        String user, String password) throws SQLException {
        if (driverException != null) {
            throw new RuntimeException(driverException);
        }

        String connectionString = "jdbc:postgresql://" + host;

        if (port != -1) {
            connectionString += (":" + port);
        }

        connectionString += ("/" + dbName);

        if (user != null) {
            connectionString += ("?user=" + user + "&password=" + password);
        }

        Connection c = DriverManager.getConnection(connectionString);
        ((PGConnection) c).addDataType("geometry", "org.postgis.PGgeometry");
        ((PGConnection) c).addDataType("box3d", "org.postgis.PGbox3d");

        return c;
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#open(java.sql.Connection,
     *      java.lang.String, com.hardcode.gdbms.engine.data.HasProperties)
     */
    public void open(Connection con, String sql, String tableName, String geomFieldName)
        throws SQLException {
        String newsql = "select asText(" + geomFieldName + ") as " +
        	geomFieldName + " ";
        String[] fields = getFieldNames(con, sql);

        for (int i = 0; i < fields.length; i++) {
            if (!fields[i].equals(geomFieldName)) {
                newsql += (", " + fields[i]);
            }
        }

        newsql += (" from " + tableName);

        jdbcSupport = JDBCSupport.newJDBCSupport(con, newsql);
    }

    /**
     * DOCUMENT ME!
     *
     * @param con DOCUMENT ME!
     * @param sql DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws SQLException DOCUMENT ME!
     */
    private String[] getFieldNames(Connection con, String sql)
        throws SQLException {
        if (fieldNames == null) {
            Statement st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet r = st.executeQuery(sql);
            ResultSetMetaData rmd = r.getMetaData();
            fieldNames = new String[rmd.getColumnCount()];

            for (int i = 0; i < rmd.getColumnCount(); i++) {
                fieldNames[i] = rmd.getColumnName(i+1);
            }

            ;
        }

        return fieldNames;
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#execute(java.sql.Connection,
     *      java.lang.String, com.hardcode.gdbms.engine.data.HasProperties)
     */
    public void execute(Connection con, String sql)
        throws SQLException {
        JDBCSupport.execute(con, sql);
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#close()
     */
    public void close() throws SQLException {
        jdbcSupport.close();
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getInternalTableName(java.lang.String)
     */
    public String getInternalTableName(String tablename) {
        return tablename;
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.ReadAccess#getFieldValue(long,
     *      int)
     */
    public Value getFieldValue(long rowIndex, int fieldId)
        throws ReadDriverException {
        return jdbcSupport.getFieldValue(rowIndex, fieldId+1);
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.ReadAccess#getFieldCount()
     */
    public int getFieldCount() throws ReadDriverException {
        return jdbcSupport.getFieldCount()-1;
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.ReadAccess#getFieldName(int)
     */
    public String getFieldName(int fieldId) throws ReadDriverException {
        //In the open method, the first field we get is the Geometry field
        return jdbcSupport.getFieldName(fieldId+1);
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.ReadAccess#getRowCount()
     */
    public long getRowCount() throws ReadDriverException {
        return jdbcSupport.getRowCount();
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.ReadAccess#getFieldType(int)
     */
    public int getFieldType(int i) throws ReadDriverException {
        //In the open method, the first field we get is the Geometry field
        return jdbcSupport.getFieldType(i+1);
    }

    /**
     * @see com.hardcode.driverManager.Driver#getName()
     */
    public String getName() {
        return "GDBMS PostGIS driver";
    }

    private GeneralPath getGeneralPath(long rowIndex, int fieldIndex) throws SQLException{
        ResultSet res = jdbcSupport.getResultSet();

        res.absolute((int) rowIndex + 1);
        fieldIndex += 1;

        if (res.getString(fieldIndex) == null) {
            return null;
        }

        PGgeometry geom = new PGgeometry(res.getString(fieldIndex));

        GeneralPath gp = new GeneralPath();

        if (geom.getGeoType() == Geometry.POINT) {
            Point p = (Point) geom.getGeometry();
            gp.moveTo(p.x, p.y);
        } else if (geom.getGeoType() == Geometry.LINESTRING) {
            gp = lineString2GP((LineString) geom.getGeometry());
        } else if (geom.getGeoType() == Geometry.MULTILINESTRING) {
            MultiLineString mls = (MultiLineString) geom.getGeometry();

            for (int j = 0; j < mls.numLines(); j++) {
                gp.append(lineString2GP(mls.getLine(j)), false);
            }
        } else if (geom.getGeoType() == Geometry.POLYGON) {
            gp = polygon2GP((Polygon) geom.getGeometry());
        } else if (geom.getGeoType() == Geometry.MULTIPOLYGON) {
            MultiPolygon mp = (MultiPolygon) geom.getGeometry();

            for (int i = 0; i < mp.numPolygons(); i++) {
                gp.append(polygon2GP(mp.getPolygon(i)), false);
            }
        } else if (geom.getGeoType() == Geometry.GEOMETRYCOLLECTION) {
            throw new RuntimeException(
                "geometryCollections not supported by this driver");
        } else {
            throw new RuntimeException("Unknown datatype");
        }

        return gp;
    }

    /**
     * DOCUMENT ME!
     *
     * @param rowIndex DOCUMENT ME!
     * @param fieldIndex DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DriverException DOCUMENT ME!
     * @throws SQLException DOCUMENT ME!
     * @throws RuntimeException DOCUMENT ME!
     */
    private GeometryImpl getCustomValue(long rowIndex, int fieldIndex)
        throws ReadDriverException, SQLException {

        return new GeometryImpl(getGeneralPath(rowIndex, fieldIndex));
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.SpatialDBDriver#getFullExtent()
     */
    public Rectangle2D getFullExtent() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.SpatialDriver#getGeometry(long)
     */
    public com.hardcode.gdbms.engine.spatial.Geometry getGeometry(long rowIndex) throws ReadDriverException {
        try {
            return getCustomValue(rowIndex, 0);
        } catch (SQLException e) {
            throw new ReadDriverException(getName(),e);
        }
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getStatementString(long)
     */
    public String getStatementString(long i) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getStatementString(int, int)
     */
    public String getStatementString(int i, int sqlType) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getStatementString(double, int)
     */
    public String getStatementString(double d, int sqlType) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getStatementString(java.lang.String, int)
     */
    public String getStatementString(String str, int sqlType) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getStatementString(java.sql.Date)
     */
    public String getStatementString(Date d) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getStatementString(java.sql.Time)
     */
    public String getStatementString(Time t) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getStatementString(java.sql.Timestamp)
     */
    public String getStatementString(Timestamp ts) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getStatementString(byte[])
     */
    public String getStatementString(byte[] binary) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.DBDriver#getStatementString(boolean)
     */
    public String getStatementString(boolean b) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see com.hardcode.gdbms.engine.values.ValueWriter#getNullStatementString()
     */
    public String getNullStatementString() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see com.hardcode.gdbms.engine.data.driver.SpatialDriver#getJTSGeometry(long)
     */
    public com.vividsolutions.jts.geom.Geometry getJTSGeometry(long rowIndex) throws ReadDriverException {
        try {
            return getGeneralPath(rowIndex, 0).getJTSGeometry();
        } catch (SQLException e) {
            throw new ReadDriverException(getName(),e);
        }
    }

    public void setDataSourceFactory(DataSourceFactory dsf) {
    }
	public int getFieldWidth(int i) throws ReadDriverException {
		return jdbcSupport.getFieldWidth(i);
	}
}

