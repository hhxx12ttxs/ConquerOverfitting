/*
 * Created on 04-mar-2005
 *
 * gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
 *
 * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ib??ez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
package com.iver.cit.gvsig.fmap.drivers.jdbc.postgis;

import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.postgis.PGbox2d;
import org.postgis.PGbox3d;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

import com.hardcode.gdbms.driver.exceptions.InitializeWriterException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.edition.DataWare;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.ICanReproject;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.ConnectionJDBC;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.fmap.drivers.DBLayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.DefaultJDBCDriver;
import com.iver.cit.gvsig.fmap.drivers.DriverAttributes;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.IConnection;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.drivers.WKBParser2;
import com.iver.cit.gvsig.fmap.drivers.XTypes;
import com.iver.cit.gvsig.fmap.edition.IWriteable;
import com.iver.cit.gvsig.fmap.edition.IWriter;

/**
 * @author FJP
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PostGisDriver extends DefaultJDBCDriver implements ICanReproject,
		IWriteable {
	private static Logger logger = Logger.getLogger(PostGisDriver.class
			.getName());

	private static int FETCH_SIZE = 5000;

	// To avoid problems when using wkb_cursor with same layer.
	// I mean, when you add twice or more the same layer using
	// the same connection

	private static int CURSOR_ID = 0;

	private int myCursorId;

	private PostGISWriter writer = new PostGISWriter();

	private WKBParser2 parser = new WKBParser2();

	private int fetch_min = -1;

	private int fetch_max = -1;

	private String sqlOrig;

	/**
	 * Used by setAbsolutePosition
	 */
	private String sqlTotal;

	private String strEPSG = null;

	private String originalEPSG = null;

	private Rectangle2D fullExtent = null;

	private String strAux;

	private String completeWhere;

	private String provCursorName = null;

	int numProvCursors = 0;

	boolean bShapeTypeRevised = false;

	private int actual_position;

	/**
	 * It stores all schemas privileges, in order to avoid checking it everytime
	 * canRead() is called.
	 */
	private Map<String, Boolean> schemasUsage = new HashMap<String, Boolean>();

	private static final BigInteger _nbase = new BigInteger("10000");

	private static final BigInteger _nbasePow2 = _nbase.pow(2);

	private static final BigInteger _nbasePow4 = _nbase.pow(4);

	private static final long nbaseLong = _nbase.longValue();

	private static final long nbaseLongPow2 = nbaseLong * nbaseLong;

	private static final int nbaseInt = (int) nbaseLong;

	protected static BigInteger getNBase() {
		return _nbase;
	}

	protected static BigInteger getNBasePow2() {
		return _nbasePow2;
	}

	protected static BigInteger getNBasePow4() {
		return _nbasePow4;
	}

	static {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 *
	 */
	public PostGisDriver() {
		// To avoid problems when using wkb_cursor with same layer.
		// I mean, when you add twice or more the same layer using
		// the same connection
		CURSOR_ID++;
		myCursorId = CURSOR_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.iver.cit.gvsig.fmap.drivers.VectorialDriver#getDriverAttributes()
	 */
	public DriverAttributes getDriverAttributes() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hardcode.driverManager.Driver#getName()
	 */
	public String getName() {
		return "PostGIS JDBC Driver";
	}

	/**
	 * @throws ReadDriverException
	 * @see com.iver.cit.gvsig.fmap.layers.ReadableVectorial#getShape(int)
	 */
	public IGeometry getShape(int index) throws ReadDriverException {
		IGeometry geom = null;
		boolean resul;
		try {
			setAbsolutePosition(index);
			// strAux = rs.getString(1);
			// geom = parser.read(strAux);
			if (rs != null) {
				byte[] data = rs.getBytes(1);
				if (data == null) // null geometry.
					return null;
				geom = parser.parse(data);
			}
		} catch (SQLException e) {
			throw new ReadDriverException(this.getName(), e);
		}

		return geom;
	}

	/**
	 * First, the geometry field. After, the rest of fields
	 * 
	 * @return
	 */
	/*
	 * public String getTotalFields() { String strAux = "AsBinary(" +
	 * getLyrDef().getFieldGeometry() + ")"; String[] fieldNames =
	 * getLyrDef().getFieldNames(); for (int i=0; i< fieldNames.length; i++) {
	 * strAux = strAux + ", " + fieldNames[i]; } return strAux; }
	 */

	/**
	 * Antes de llamar a esta funci?n hay que haber fijado el workingArea si se
	 * quiere usar.
	 * 
	 * @param conn
	 * @throws DBException
	 */
	public void setData(IConnection conn, DBLayerDefinition lyrDef)
			throws DBException {
		this.conn = conn;
		// TODO: Deber?amos poder quitar Conneciton de la llamada y meterlo
		// en lyrDef desde el principio.

		lyrDef.setConnection(conn);
		setLyrDef(lyrDef);

		getTableEPSG_and_shapeType(conn, lyrDef);

		getLyrDef().setSRID_EPSG(originalEPSG);

		try {
			((ConnectionJDBC) conn).getConnection().setAutoCommit(false);
			sqlOrig = "SELECT " + getTotalFields() + " FROM "
			+ getLyrDef().getComposedTableName() + " ";
			// + getLyrDef().getWhereClause();
			if (canReproject(strEPSG)) {
				completeWhere = getCompoundWhere(sqlOrig, workingArea, strEPSG);
			} else {
				completeWhere = getCompoundWhere(sqlOrig, workingArea,
						originalEPSG);
			}
			// completeWhere = getLyrDef().getWhereClause() + completeWhere;

			String sqlAux = sqlOrig + completeWhere + " ORDER BY "
					+ getLyrDef().getFieldID();

			sqlTotal = sqlAux;
			logger.info("Cadena SQL:" + sqlAux);
			Statement st = ((ConnectionJDBC) conn).getConnection().createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			// st.setFetchSize(FETCH_SIZE);
			myCursorId++;
			st.execute("declare " + getTableName() + myCursorId
					+ "_wkb_cursor binary scroll cursor with hold for "
					+ sqlAux);
			rs = st.executeQuery("fetch forward " + FETCH_SIZE + " in "
					+ getTableName() + myCursorId + "_wkb_cursor");
			fetch_min = 0;
			fetch_max = FETCH_SIZE - 1;
			actual_position = 0;
			metaData = rs.getMetaData();
			doRelateID_FID();

			writer.setCreateTable(false);
			writer.setWriteAll(false);
			writer.initialize(lyrDef);

		} catch (SQLException e) {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e1) {
				throw new DBException(e);
			}
			throw new DBException(e);
		} catch (InitializeWriterException e) {
			throw new DBException(e);
		}
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.layers.ReadableVectorial#getFullExtent()
	 */
	public Rectangle2D getFullExtent() throws ReadDriverException {
		if (fullExtent == null) {
			try {
				Statement s = ((ConnectionJDBC) conn).getConnection()
						.createStatement();
				String query = "SELECT extent(\""
				    + getLyrDef().getFieldGeometry()
				    + "\") AS FullExtent FROM " + getLyrDef().getComposedTableName()
				    + " " + getCompleteWhere();
				ResultSet r = s.executeQuery(query);
				r.next();
				String strAux = r.getString(1);
				System.out.println("fullExtent = " + strAux);
				if (strAux == null) {
					logger.debug("La capa " + getLyrDef().getName()
							+ " no tiene FULLEXTENT");
					return null;
				}
				if (strAux.startsWith("BOX3D")) {
					PGbox3d regeom = new PGbox3d(strAux);
					double x = regeom.getLLB().x;
					double y = regeom.getLLB().y;
					double w = regeom.getURT().x - x;
					double h = regeom.getURT().y - y;
					fullExtent = new Rectangle2D.Double(x, y, w, h);
				} else {
					PGbox2d regeom = new PGbox2d(strAux);
					double x = regeom.getLLB().x;
					double y = regeom.getLLB().y;
					double w = regeom.getURT().x - x;
					double h = regeom.getURT().y - y;
					fullExtent = new Rectangle2D.Double(x, y, w, h);
				}
			} catch (SQLException e) {
				throw new ReadDriverException(this.getName(), e);
			}

		}
		return fullExtent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.iver.cit.gvsig.fmap.drivers.VectorialDatabaseDriver#getGeometryIterator
	 * (java.lang.String)
	 */
	public IFeatureIterator getFeatureIterator(String sql)
			throws ReadDriverException {
		PostGisFeatureIterator geomIterator = null;
		geomIterator = myGetFeatureIterator(sql);
		geomIterator.setLyrDef(getLyrDef());

		return geomIterator;
	}

	private PostGisFeatureIterator myGetFeatureIterator(String sql)
			throws ReadDriverException {
		PostGisFeatureIterator geomIterator = null;
		try {
			// st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
			// ResultSet.CONCUR_READ_ONLY);

			// st = conn.createStatement
			if (provCursorName != null) {
				/*
				 * st.execute("BEGIN"); st.execute("CLOSE " + provCursorName);
				 * bCursorActivo = false; st.execute("COMMIT");
				 */
				numProvCursors++;
			}
			// st.execute("BEGIN");
			provCursorName = getTableName() + myCursorId + "wkb_cursor_prov_"
					+ System.currentTimeMillis() + "" + numProvCursors;

			// st.execute("BEGIN");
			bCursorActivo = true;
			// ResultSet rs = st.executeQuery(sql);
			geomIterator = new PostGisFeatureIterator(
					((ConnectionJDBC) conn).getConnection(), provCursorName,
					sql);
		} catch (SQLException e) {
			// e.printStackTrace();
			// e.printStackTrace();
			// SqlDriveExceptionType type = new SqlDriveExceptionType();
			// type.setDriverName("PostGIS Driver");
			// type.setSql(sql);
			// type.setLayerName(getTableName());
			// type.setSchema(null);
			throw new ReadDriverException("PostGIS Driver", e);
			// throw new DriverException(e);
			// return null;
		}

		return geomIterator;
	}

	public IFeatureIterator getFeatureIterator(Rectangle2D r, String strEPSG)
	throws ReadDriverException {
		if (workingArea != null)
			r = r.createIntersection(workingArea);

		String sqlAux;
		if (canReproject(strEPSG)) {
			sqlAux = sqlOrig + getCompoundWhere(sqlOrig, r, strEPSG);
		} else {
			sqlAux = sqlOrig + getCompoundWhere(sqlOrig, r, originalEPSG);
		}

		System.out.println("SqlAux getFeatureIterator = " + sqlAux);

		return getFeatureIterator(sqlAux);
	}

	/**
	 * Le pasas el rect?ngulo que quieres pedir. La primera vez es el
	 * workingArea, y las siguientes una interseccion de este rectangulo con el
	 * workingArea
	 * 
	 * @param r
	 * @param strEPSG
	 * @return
	 */
	private String getCompoundWhere(String sql, Rectangle2D r, String strEPSG) {
		if (r == null)
			return getWhereClause();

		double xMin = r.getMinX();
		double yMin = r.getMinY();
		double xMax = r.getMaxX();
		double yMax = r.getMaxY();
		String wktBox = "GeometryFromText('LINESTRING(" + xMin + " " + yMin
		+ ", " + xMax + " " + yMin + ", " + xMax + " " + yMax + ", "
		+ xMin + " " + yMax + ")', " + strEPSG + ")";
		String sqlAux;
		if (getWhereClause().toUpperCase().indexOf("WHERE") != -1)
		    sqlAux = getWhereClause() + " AND \"" + getLyrDef().getFieldGeometry() + "\" && " + wktBox;
		else
		    sqlAux = "WHERE \"" + getLyrDef().getFieldGeometry() + "\" && "
			+ wktBox;
		return sqlAux;
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.drivers.IVectorialDatabaseDriver#getConnectionStringBeginning()
	 */
	public String getConnectionStringBeginning() {
		return "jdbc:postgresql:";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.iver.cit.gvsig.fmap.drivers.DefaultDBDriver#getFieldValue(long,
	 * int)
	 */
	public Value getFieldValue(long rowIndex, int idField)
	throws ReadDriverException {
		boolean resul;
		// EL ABSOLUTE NO HACE QUE SE VUELVAN A LEER LAS
		// FILAS, ASI QUE MONTAMOS ESTA HISTORIA PARA QUE
		// LO HAGA
		// System.out.println("getShape " + index);
		int index = (int) (rowIndex);
		try {
			setAbsolutePosition(index);
			int fieldId = idField + 2;
			return getFieldValue(rs, fieldId);

		} catch (SQLException e) {
			throw new ReadDriverException("PostGIS Driver", e);
		}
	}

	static Value getFieldValue(ResultSet aRs, int fieldId) throws SQLException {
		ResultSetMetaData metaData = aRs.getMetaData();
		byte[] byteBuf = aRs.getBytes(fieldId);
		if (byteBuf == null)
			return ValueFactory.createNullValue();
		else {
			ByteBuffer buf = ByteBuffer.wrap(byteBuf);
			if (metaData.getColumnType(fieldId) == Types.VARCHAR)
				return ValueFactory.createValue(aRs.getString(fieldId));
			if (metaData.getColumnType(fieldId) == Types.CHAR){
				String character = aRs.getString(fieldId);
				if (character != null){
					return ValueFactory.createValue(character.trim());
				}else{
					return ValueFactory.createValue(character);
				}
			}
			if (metaData.getColumnType(fieldId) == Types.FLOAT)
				return ValueFactory.createValue(buf.getFloat());
			if (metaData.getColumnType(fieldId) == Types.DOUBLE)
				return ValueFactory.createValue(buf.getDouble());
			if (metaData.getColumnType(fieldId) == Types.REAL)
				return ValueFactory.createValue(buf.getFloat());
			if (metaData.getColumnType(fieldId) == Types.INTEGER)
				return ValueFactory.createValue(buf.getInt());
			if (metaData.getColumnType(fieldId) == Types.SMALLINT)
				return ValueFactory.createValue(buf.getShort());
			if (metaData.getColumnType(fieldId) == Types.BIGINT)
				return ValueFactory.createValue(buf.getLong());
			if (metaData.getColumnType(fieldId) == Types.BIT)
				return ValueFactory.createValue((byteBuf[0] == 1));
			if (metaData.getColumnType(fieldId) == Types.BOOLEAN)
				return ValueFactory.createValue(aRs.getBoolean(fieldId));
			if (metaData.getColumnType(fieldId) == Types.DATE) {
				long daysAfter2000 = buf.getInt() + 1;
				long msecs = daysAfter2000 * 24 * 60 * 60 * 1000;
				long real_msecs_date1 = (long) (XTypes.NUM_msSecs2000 + msecs);
				Date realDate1 = new Date(real_msecs_date1);
				return ValueFactory.createValue(realDate1);
			}
			if (metaData.getColumnType(fieldId) == Types.TIME) {
				// TODO:
				// throw new RuntimeException("TIME type not implemented yet");
				return ValueFactory.createValue("NOT IMPLEMENTED YET");
			}
			if (metaData.getColumnType(fieldId) == Types.TIMESTAMP) {
				double segsReferredTo2000 = buf.getDouble();
				long real_msecs = (long) (XTypes.NUM_msSecs2000 + segsReferredTo2000 * 1000);
				Timestamp valTimeStamp = new Timestamp(real_msecs);
				return ValueFactory.createValue(valTimeStamp);
			}

			if (metaData.getColumnType(fieldId) == Types.NUMERIC) {
				// System.out.println(metaData.getColumnName(fieldId) + " "
				// + metaData.getColumnClassName(fieldId));
				// short ndigits = buf.getShort();
				// short weight = buf.getShort();
				// short sign = buf.getShort();
				// short dscale = buf.getShort();
				// String strAux;
				// if (sign == 0)
				// strAux = "+";
				// else
				// strAux = "-";
				//
				// for (int iDigit = 0; iDigit < ndigits; iDigit++) {
				// short digit = buf.getShort();
				// strAux = strAux + digit;
				// if (iDigit == weight)
				// strAux = strAux + ".";
				//
				// }
				// strAux = strAux + "0";

				BigDecimal dec;
				dec = getBigDecimal(buf.array());
				// dec = new BigDecimal(strAux);
				// System.out.println(ndigits + "_" + weight + "_" + dscale
				// + "_" + strAux);
				// System.out.println(strAux + " Big= " + dec);
				return ValueFactory.createValue(dec.doubleValue());
			}

		}

		return ValueFactory.createNullValue();

	}

	private static BigDecimal getBigDecimal(byte[] number) throws SQLException {

		short ndigits = (short) (((number[0] & 0xff) << 8) | (number[1] & 0xff));
		short weight = (short) (((number[2] & 0xff) << 8) | (number[3] & 0xff));
		short sign = (short) (((number[4] & 0xff) << 8) | (number[5] & 0xff));
		short dscale = (short) (((number[6] & 0xff) << 8) | (number[7] & 0xff));

		if (sign == (short) 0xC000) {
			// Numeric NaN - BigDecimal doesn't support this
			throw new PSQLException(
					"The numeric value is NaN - can't convert to BigDecimal",
					PSQLState.NUMERIC_VALUE_OUT_OF_RANGE);
		}

		final int bigDecimalSign = sign == 0x4000 ? -1 : 1;

		// System.out.println("ndigits=" + ndigits
		// +",\n wieght=" + weight
		// +",\n sign=" + sign
		// +",\n dscale=" + dscale);
		// // for (int i=8; i < number.length; i++) {
		// System.out.println("numer[i]=" + (int) (number[i] & 0xff));
		// }

		int tail = ndigits % 4;
		int bytesToParse = (ndigits - tail) * 2 + 8;
		// System.out.println("numberParseLength="+numberParseLength);
		int i;
		BigInteger unscaledValue = BigInteger.ZERO;
		final BigInteger nbase = getNBase();
		final BigInteger nbasePow2 = getNBasePow2();
		final BigInteger nbasePow4 = getNBasePow4();


		byte[] buffer = new byte[8];

		// System.out.println("tail = " + tail + " bytesToParse = " +
		// bytesToParse);

		for (i = 8; i < bytesToParse; i += 8) {
			// This Hi and Lo aren't bytes Hi Li, but decimal Hi Lo!! (Big &
			// Small)
			long valHi = (((number[i] & 0xff) << 8) | (number[i + 1] & 0xff))
					* 10000
					+ (((number[i + 2] & 0xff) << 8) | (number[i + 3] & 0xff));
			long valLo = (((number[i + 4] & 0xff) << 8) | (number[i + 5] & 0xff))
					* 10000
					+ (((number[i + 6] & 0xff) << 8) | (number[i + 7] & 0xff));
			long val = valHi * nbaseLongPow2 + valLo;
			buffer[0] = (byte) (val >>> 56);
			buffer[1] = (byte) (val >>> 48);
			buffer[2] = (byte) (val >>> 40);
			buffer[3] = (byte) (val >>> 32);
			buffer[4] = (byte) (val >>> 24);
			buffer[5] = (byte) (val >>> 16);
			buffer[6] = (byte) (val >>> 8);
			buffer[7] = (byte) (val >>> 0);

			BigInteger valBigInteger = new BigInteger(bigDecimalSign, buffer);
			unscaledValue = unscaledValue.multiply(nbasePow4)
					.add(valBigInteger);
		}
		tail = tail % 2;
		bytesToParse = (ndigits - tail) * 2 + 8;
		// System.out.println("tail = " + tail + " bytesToParse = " +
		// bytesToParse);

		buffer = new byte[4];
		for (; i < bytesToParse; i += 4) {
			int val = (((number[i] & 0xff) << 8) | (number[i + 1] & 0xff))
					* nbaseInt
					+ (((number[i + 2] & 0xff) << 8) | (number[i + 3] & 0xff));
			buffer[0] = (byte) (val >>> 24);
			buffer[1] = (byte) (val >>> 16);
			buffer[2] = (byte) (val >>> 8);
			buffer[3] = (byte) val;
			BigInteger valBigInteger = new BigInteger(bigDecimalSign, buffer);
			unscaledValue = unscaledValue.multiply(nbasePow2)
					.add(valBigInteger);
		}

		// Add the rest of number
		if (tail % 2 == 1) {
			buffer = new byte[2];
			buffer[0] = number[number.length - 2];
			buffer[1] = number[number.length - 1];
			BigInteger valBigInteger = new BigInteger(bigDecimalSign, buffer);
			unscaledValue = unscaledValue.multiply(nbase).add(valBigInteger);
			// System.out.println("Value (2)  unscaled =" + unscaledValue
			// +", valBI = "+ valBigInteger);
		}


		// Calculate scale offset
		final int databaseScale = (ndigits - weight - 1) * 4; // Number of
																// digits in
																// nbase
		// TODO This number of digits should be calculeted depending on nbase
		// (getNbase());

		BigDecimal result = new BigDecimal(unscaledValue, databaseScale);
		return result;

	}

	public void open() {
		/*
		 * try { st = conn.createStatement(); st.setFetchSize(2000); if
		 * (bCursorActivo) close(); st.execute("declare wkb_cursor binary cursor
		 * for " + sqlOrig); rs = st.executeQuery("fetch forward all in
		 * wkb_cursor"); // st.execute("BEGIN"); bCursorActivo = true; } catch
		 * (SQLException e) { e.printStackTrace(); throw new
		 * com.iver.cit.gvsig.fmap.DriverException(e); }
		 */

	}

	private void setAbsolutePosition(int index) throws SQLException {
		// TODO: USAR LIMIT Y ORDER BY, Y HACERLO TAMBI?N PARA
		// MYSQL

		// EL ABSOLUTE NO HACE QUE SE VUELVAN A LEER LAS
		// FILAS, ASI QUE MONTAMOS ESTA HISTORIA PARA QUE
		// LO HAGA
		// System.out.println("getShape " + index + " fetchMin=" + fetch_min + "
		// fetchMax=" + fetch_max);

		if ((index >= fetch_min) && (index <= fetch_max)) {
			// Est? en el intervalo, as? que lo podemos posicionar

		} else {
			// calculamos el intervalo correcto
			fetch_min = (index / FETCH_SIZE) * FETCH_SIZE;
			fetch_max = fetch_min + FETCH_SIZE - 1;
			// y cogemos ese cacho
			rs.close();
			
			Statement st = ((ConnectionJDBC)conn).getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
			ResultSet.CONCUR_READ_ONLY);
			
			myCursorId++;
			st.execute("declare "
					+ getTableName()
					+ myCursorId
					+ "_wkb_cursorAbsolutePosition binary scroll cursor with hold for "
					+ sqlTotal);
			st.executeQuery("fetch absolute " + fetch_min + " in "
					+ getTableName() + myCursorId
					+ "_wkb_cursorAbsolutePosition");

			rs = st.executeQuery("fetch forward " + FETCH_SIZE + " in "
					+ getTableName() + myCursorId
					+ "_wkb_cursorAbsolutePosition");

		}
		rs.absolute(index - fetch_min + 1);
		actual_position = index;

	}

	/**
	 * @see com.iver.cit.gvsig.fmap.drivers.IVectorialDatabaseDriver#getGeometryField(java.lang.String)
	 */
	public String getGeometryField(String fieldName) {
	    return "AsEWKB(\"" + fieldName + "\", 'XDR')";
	}

	/**
	 * @see com.hardcode.gdbms.engine.data.driver.ObjectDriver#getPrimaryKeys()
	 */
	public int[] getPrimaryKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.drivers.IVectorialJDBCDriver#getDefaultPort()
	 */
	public int getDefaultPort() {
		return 5432;
	}

	/**
	 * @see com.hardcode.gdbms.engine.data.driver.ObjectDriver#write(com.hardcode.gdbms.engine.data.edition.DataWare)
	 */
	public void write(DataWare arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.iver.cit.gvsig.fmap.core.ICanReproject#getSourceProjection()
	 */
	public String getSourceProjection(IConnection conn, DBLayerDefinition dbld) {
		if (originalEPSG == null)
			getTableEPSG_and_shapeType(conn, dbld);
		return originalEPSG;
	}

	/**
	 * Las tablas con geometr?as est?n en la tabla GEOMETRY_COLUMNS y de ah?
	 * sacamos en qu? proyecci?n est?n. El problema es que si el usuario hace
	 * una vista de esa tabla, no estar? dada de alta aqu? y entonces gvSIG no
	 * se entera en qu? proyecci?n est? trabajando (y le ponemos un -1 como mal
	 * menor). El -1 implica que luego no podremos reproyectar al vuelo desde la
	 * base de datos. OJO: ES SENSIBLE A MAYUSCULAS / MINUSCULAS!!!
	 */
	private void getTableEPSG_and_shapeType(IConnection conn,
			DBLayerDefinition dbld) {
		try {
			Statement stAux = ((ConnectionJDBC) conn).getConnection()
					.createStatement();

			// String sql =
			// "SELECT * FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = '"
			// + getTableName() + "' AND F_GEOMETRY_COLUMN = '" +
			// getLyrDef().getFieldGeometry() + "'";
			String sql;
			if (dbld.getSchema() == null || dbld.getSchema().equals("")) {
				sql = "SELECT * FROM GEOMETRY_COLUMNS WHERE F_TABLE_SCHEMA = current_schema() AND F_TABLE_NAME = '"
						+ dbld.getTableName()
						+ "' AND F_GEOMETRY_COLUMN = '"
						+ dbld.getFieldGeometry() + "'";
			} else {
				sql = "SELECT * FROM GEOMETRY_COLUMNS WHERE F_TABLE_SCHEMA = '"
						+ dbld.getSchema() + "' AND F_TABLE_NAME = '"
						+ dbld.getTableName() + "' AND F_GEOMETRY_COLUMN = '"
						+ dbld.getFieldGeometry() + "'";
			}

			ResultSet rs = stAux.executeQuery(sql);
			if (rs.next()) {
				originalEPSG = "" + rs.getInt("SRID");
				String geometryType = rs.getString("TYPE");
				int shapeType = FShape.MULTI;
				if (geometryType.compareToIgnoreCase("POINT") == 0)
					shapeType = FShape.POINT;
				else if (geometryType.compareToIgnoreCase("LINESTRING") == 0)
					shapeType = FShape.LINE;
				else if (geometryType.compareToIgnoreCase("POLYGON") == 0)
					shapeType = FShape.POLYGON;
				else if (geometryType.compareToIgnoreCase("MULTIPOINT") == 0)
					shapeType = FShape.MULTIPOINT;
				else if (geometryType.compareToIgnoreCase("MULTILINESTRING") == 0)
					shapeType = FShape.LINE;
				else if (geometryType.compareToIgnoreCase("MULTILINESTRINGM") == 0) // MCoord
					shapeType = FShape.LINE | FShape.M;
				else if (geometryType.compareToIgnoreCase("MULTIPOLYGON") == 0)
					shapeType = FShape.POLYGON;
				dbld.setShapeType(shapeType);
			} else {
				originalEPSG = "-1";
			}

			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			originalEPSG = "-1";
			logger.error(e);
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.iver.cit.gvsig.fmap.core.ICanReproject#getDestProjection()
	 */
	public String getDestProjection() {
		return strEPSG;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.iver.cit.gvsig.fmap.core.ICanReproject#setDestProjection(java.lang
	 * .String)
	 */
	public void setDestProjection(String toEPSG) {
		this.strEPSG = toEPSG;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.iver.cit.gvsig.fmap.core.ICanReproject#canReproject(java.lang.String)
	 */
	public boolean canReproject(String toEPSGdestinyProjection) {
		// TODO POR AHORA, REPROYECTA SIEMPRE gvSIG.
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.iver.cit.gvsig.fmap.drivers.DefaultDBDriver#doRelateID_FID()
	 */
	protected void doRelateID_FID() throws DBException {
		hashRelate = new Hashtable();
		try {
			String strSQL = "SELECT " + getLyrDef().getFieldID() + " FROM "
			+ getLyrDef().getComposedTableName() + " ";
			// + getLyrDef().getWhereClause();
			if (canReproject(strEPSG)) {
				strSQL = strSQL
				+ getCompoundWhere(strSQL, workingArea, strEPSG);
			} else {
				strSQL = strSQL
				+ getCompoundWhere(strSQL, workingArea, originalEPSG);
			}
			strSQL = strSQL + " ORDER BY " + getLyrDef().getFieldID();
			Statement s = ((ConnectionJDBC) getConnection()).getConnection()
					.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
			int fetchSize = 5000;
			ResultSet r = s.executeQuery(strSQL);
			int id = 0;
			String gid;
			while (r.next()) {
				gid = r.getString(1);
				Value aux = ValueFactory.createValue(gid);
				hashRelate.put(aux, new Integer(id));
				// System.out.println("ASOCIANDO CLAVE " + aux + " CON VALOR " +
				// id);
				id++;
				// System.out.println("Row " + id + ":" + strAux);
			}
			s.close();
			numReg = id;

			/*
			 * for (int index = 0; index < getShapeCount(); index++) { Value aux
			 * = getFieldValue(index, idFID_FieldName-2); hashRelate.put(aux,
			 * new Integer(index)); // System.out.println("Row " + index +
			 * " clave=" + aux); }
			 */
			/*
			 * int index = 0;
			 * 
			 * while (rs.next()) { Value aux = getFieldValue(index,
			 * idFID_FieldName-2); hashRelate.put(aux, new Integer(index));
			 * index++; System.out.println("Row " + index + " clave=" + aux); }
			 * numReg = index;
			 */
			// rs.beforeFirst();
			/*
			 * } catch (com.hardcode.gdbms.engine.data.driver.DriverException e)
			 * { // TODO Auto-generated catch block e.printStackTrace();
			 */
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}

	public String getSqlTotal() {
		return sqlTotal;
	}

	/**
	 * @return Returns the completeWhere.
	 */
	public String getCompleteWhere() {
		return completeWhere;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.iver.cit.gvsig.fmap.drivers.DefaultDBDriver#close()
	 */
	public void close() {
		super.close();
		/*
		 * if (bCursorActivo) { try { // st =
		 * conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
		 * ResultSet.CONCUR_READ_ONLY); st.execute("CLOSE wkb_cursor_prov"); //
		 * st.close(); } catch (SQLException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); } bCursorActivo = false; }
		 */

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.iver.cit.gvsig.fmap.drivers.VectorialDatabaseDriver#getFeatureIterator
	 * (java.awt.geom.Rectangle2D, java.lang.String, java.lang.String[])
	 */
	public IFeatureIterator getFeatureIterator(Rectangle2D r, String strEPSG,
			String[] alphaNumericFieldsNeeded) throws ReadDriverException {
		String sqlAux = null;
		DBLayerDefinition lyrDef = getLyrDef();
		DBLayerDefinition clonedLyrDef = cloneLyrDef(lyrDef);
		ArrayList<FieldDescription> myFieldsDesc = new ArrayList<FieldDescription>(); // =
																						// new
																						// FieldDescription[alphaNumericFieldsNeeded.length+1];
		try {
			if (workingArea != null)
				r = r.createIntersection(workingArea);
			// if (getLyrDef()==null){
			// load();
			// throw new DriverException("Fallo de la conexi?n");
			// }
			String strAux = getGeometryField(lyrDef.getFieldGeometry());

			boolean found = false;
			int fieldIndex = -1;
			if (alphaNumericFieldsNeeded != null) {
				FieldDescription[] fieldsDesc = lyrDef.getFieldsDesc();

				for (int i = 0; i < alphaNumericFieldsNeeded.length; i++) {
					fieldIndex = lyrDef
							.getFieldIdByName(alphaNumericFieldsNeeded[i]);
					if (fieldIndex < 0) {
						throw new RuntimeException(
								"No se ha encontrado el nombre de campo "
										+ metaData.getColumnName(i));
					}
					strAux = strAux
							+ ", "
							+ PostGIS
									.escapeFieldName(lyrDef.getFieldNames()[fieldIndex]);
					if (alphaNumericFieldsNeeded[i].equalsIgnoreCase(lyrDef
							.getFieldID())) {
						found = true;
						clonedLyrDef.setIdFieldID(i);
					}

					myFieldsDesc.add(fieldsDesc[fieldIndex]);
				}
			}
			// Nos aseguramos de pedir siempre el campo ID
			if (found == false) {
				strAux = strAux + ", " + lyrDef.getFieldID();
				myFieldsDesc.add(lyrDef.getFieldsDesc()[lyrDef
						.getIdField(lyrDef.getFieldID())]);
				clonedLyrDef.setIdFieldID(myFieldsDesc.size() - 1);
			}
			clonedLyrDef.setFieldsDesc((FieldDescription[]) myFieldsDesc
					.toArray(new FieldDescription[] {}));

			String sqlProv = "SELECT " + strAux + " FROM "
			+ lyrDef.getComposedTableName() + " ";
			// + getLyrDef().getWhereClause();

			if (canReproject(strEPSG)) {
				sqlAux = sqlProv + getCompoundWhere(sqlProv, r, strEPSG);
			} else {
				sqlAux = sqlProv + getCompoundWhere(sqlProv, r, originalEPSG);
			}

			System.out.println("SqlAux getFeatureIterator = " + sqlAux);
			PostGisFeatureIterator geomIterator = null;
			geomIterator = myGetFeatureIterator(sqlAux);
			geomIterator.setLyrDef(clonedLyrDef);
			return geomIterator;
		} catch (Exception e) {
			// e.printStackTrace();
			// SqlDriveExceptionType type = new SqlDriveExceptionType();
			// type.setDriverName("PostGIS Driver");
			// type.setSql(sqlAux);
			// type.setLayerName(getTableName());
			// type.setSchema(null);
			throw new ReadDriverException("PostGIS Driver", e);

			// throw new DriverException(e);
		}
	}

	/*
	 * public void preProcess() throws EditionException { writer.preProcess(); }
	 * 
	 * public void process(IRowEdited row) throws EditionException {
	 * writer.process(row); }
	 * 
	 * public void postProcess() throws EditionException { writer.postProcess();
	 * }
	 * 
	 * public String getCapability(String capability) { return
	 * writer.getCapability(capability); }
	 * 
	 * public void setCapabilities(Properties capabilities) {
	 * writer.setCapabilities(capabilities); }
	 * 
	 * public boolean canWriteAttribute(int sqlType) { return
	 * writer.canWriteAttribute(sqlType); }
	 * 
	 * public boolean canWriteGeometry(int gvSIGgeometryType) { return
	 * writer.canWriteGeometry(gvSIGgeometryType); }
	 * 
	 * public void initialize(ITableDefinition layerDef) throws EditionException
	 * { writer.setCreateTable(false); writer.setWriteAll(false); // Obtenemos
	 * el DBLayerDefinition a partir del driver
	 * 
	 * DBLayerDefinition dbLyrDef = getLyrDef();
	 * 
	 * 
	 * writer.initialize(dbLyrDef); }
	 */
	public boolean isWritable() {
		// CHANGE FROM CARTOLAB
		// return true;
		return writer.canSaveEdits();
		// END CHANGE CARTOLAB
	}

	public IWriter getWriter() {
		return writer;
	}

	public String[] getGeometryFieldsCandidates(IConnection conn,
			String table_name) throws DBException {
		ArrayList list = new ArrayList();
		try {
			Statement stAux = ((ConnectionJDBC) conn).getConnection()
					.createStatement();
			String[] tokens = table_name.split("\\u002E", 2);
			String sql;
			if (tokens.length > 1) {
				sql = "select * from GEOMETRY_COLUMNS WHERE F_TABLE_SCHEMA = '"
						+ tokens[0] + "' AND F_TABLE_NAME = '" + tokens[1]
						+ "'";
			} else {
				sql = "select * from GEOMETRY_COLUMNS"
						+ " where F_TABLE_SCHEMA = current_schema() AND F_TABLE_NAME = '"
						+ table_name + "'";

			}

			// String sql =
			// "SELECT * FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = '"
			// + table_name + "'";

			ResultSet rs = stAux.executeQuery(sql);
			while (rs.next()) {
				String geomCol = rs.getString("F_GEOMETRY_COLUMN");
				list.add(geomCol);
			}
			rs.close();
			stAux.close();
		} catch (SQLException e) {
			throw new DBException(e);
		}
		return (String[]) list.toArray(new String[0]);
	}

	// public String[] getTableFields(IConnection conex, String table) throws
	// DBException {
	// try{
	// Statement st = ((ConnectionJDBC)conex).getConnection().createStatement();
	// // ResultSet rs = dbmd.getTables(catalog, null,
	// dbLayerDefinition.getTable(), null);
	// ResultSet rs = st.executeQuery("select * from " + table + " LIMIT 1");
	// ResultSetMetaData rsmd = rs.getMetaData();
	//
	// String[] ret = new String[rsmd.getColumnCount()];
	//
	// for (int i = 0; i < ret.length; i++) {
	// ret[i] = rsmd.getColumnName(i+1);
	// }
	//
	// return ret;
	// }catch (SQLException e) {
	// throw new DBException(e);
	// }
	// }

	private DBLayerDefinition cloneLyrDef(DBLayerDefinition lyrDef) {
		DBLayerDefinition clonedLyrDef = new DBLayerDefinition();

		clonedLyrDef.setName(lyrDef.getName());
		clonedLyrDef.setFieldsDesc(lyrDef.getFieldsDesc());

		clonedLyrDef.setShapeType(lyrDef.getShapeType());
		clonedLyrDef.setProjection(lyrDef.getProjection());

		clonedLyrDef.setConnection(lyrDef.getConnection());
		clonedLyrDef.setCatalogName(lyrDef.getCatalogName());
		clonedLyrDef.setSchema(lyrDef.getSchema());
		clonedLyrDef.setTableName(lyrDef.getTableName());

		clonedLyrDef.setFieldID(lyrDef.getFieldID());
		clonedLyrDef.setFieldGeometry(lyrDef.getFieldGeometry());
		clonedLyrDef.setWhereClause(lyrDef.getWhereClause());
		clonedLyrDef.setWorkingArea(lyrDef.getWorkingArea());
		clonedLyrDef.setSRID_EPSG(lyrDef.getSRID_EPSG());
		clonedLyrDef.setClassToInstantiate(lyrDef.getClassToInstantiate());

		clonedLyrDef.setIdFieldID(lyrDef.getIdFieldID());
		clonedLyrDef.setDimension(lyrDef.getDimension());
		clonedLyrDef.setHost(lyrDef.getHost());
		clonedLyrDef.setPort(lyrDef.getPort());
		clonedLyrDef.setDataBase(lyrDef.getDataBase());
		clonedLyrDef.setUser(lyrDef.getUser());
		clonedLyrDef.setPassword(lyrDef.getPassword());
		clonedLyrDef.setConnectionName(lyrDef.getConnectionName());
		return clonedLyrDef;
	}

	public String getTotalFields() {
		StringBuilder strAux = new StringBuilder();
		strAux.append(getGeometryField(getLyrDef().getFieldGeometry()));
		String[] fieldNames = getLyrDef().getFieldNames();
		for (int i = 0; i < fieldNames.length; i++) {
			strAux.append(", " + PostGIS.escapeFieldName(fieldNames[i]));
		}
		return strAux.toString();
	}


	/**
	 * Gets all field names of a given table.
	 * 
	 * This method comes from DefaultJDBC.java class. Postgis driver has no method to check
	 *  the status of the connection -if it is valid or not. So, as it's not possible to assure 
	 *  its status, close the connection when an exception happens and re-open it on demand 
	 *  on the proper method will solve the problems related to an invalid status.
	 * 
	 * @param conn connection object
	 * @param table_name table name
	 * @return all field names of the given table
	 * @throws SQLException
	 */
	@Override
	public String[] getAllFields(IConnection conn, String table_name) throws DBException {
		Statement st = null;
		ResultSet rs = null;
		table_name = tableNameToComposedTableName(table_name);

		try {
			st = ((ConnectionJDBC)conn).getConnection().createStatement();
			rs = st.executeQuery("SELECT * FROM " + table_name + " LIMIT 1");
			ResultSetMetaData rsmd = rs.getMetaData();
			String[] ret = new String[rsmd.getColumnCount()];

			for (int i = 0; i < ret.length; i++) {
				ret[i] = rsmd.getColumnName(i+1);
			}

			return ret;
		} catch (SQLException e) {
			// Next time  getConnection() method is called it will be re-opened.
			// @see com.iver.cit.gvsig.fmap.drivers.ConnectionJDBC.java;
			closeConnection(conn);
			throw new DBException(e);
		}
		finally {
			closeResultSet(rs);
			closeStatement(st);
		}
	}

	/**
	 * Gets all field type names of a given table.
	 * 
	 * This method comes from DefaultJDBC.java class. Postgis driver has no method to check
	 *  the status of the connection -if it is valid or not. So, as it's not possible to assure 
	 *  its status, close the connection when an exception happens and re-open it on demand 
	 *  on the proper method will solve the problems related to an invalid status.
	 * 
	 * @param conn connection object
	 * @param table_name table name
	 * @return all field type names of the given table
	 * @throws SQLException
	 */
	public String[] getAllFieldTypeNames(IConnection conn, String table_name) throws DBException {
	    Statement st = null;
	    ResultSet rs = null;
	    table_name = tableNameToComposedTableName(table_name);
		try {
			st = ((ConnectionJDBC)conn).getConnection().createStatement();
			rs = st.executeQuery("SELECT * FROM " + table_name + " LIMIT 1");
			ResultSetMetaData rsmd = rs.getMetaData();
			String[] ret = new String[rsmd.getColumnCount()];

			for (int i = 0; i < ret.length; i++) {
				ret[i] = rsmd.getColumnTypeName(i+1);
			}
			return ret;
		} catch (SQLException e) {
			// Next time  getConnection() method is called it will be re-opened.
			// @see com.iver.cit.gvsig.fmap.drivers.ConnectionJDBC.java;
			closeConnection(conn);
			throw new DBException(e);
		}
		finally{
			closeStatement(st);
			closeResultSet(rs);
		}
	}

	/**
	 *
	 * @param tableName
	 * @return a string with the schema and the tableName quoted
	 */
	private String tableNameToComposedTableName(String tableName) {
		String composedTableName = null;
		// \u002E = unicode character for .
		String[] tokens = tableName.trim().replace("\"", "").split("\\u002E");

		if (tokens.length == 1) {
			composedTableName = "\"" + tokens[0] + "\"";

		} else if (tokens.length == 2) {
			composedTableName = "\"" + tokens[0] + "\".\"" + tokens[1] + "\"";
		} else {
			// this is a not predictable case, so we return the same
			composedTableName = tableName;
		}

		return composedTableName;
	}

	/**
	 * Close a ResultSet
	 * @param rs, the resultset to be closed
	 * @return true if the resulset was correctly closed. false in any other case
	 */
	public boolean closeResultSet(ResultSet rs) {
		boolean error = false;

		if (rs != null) {
			try {
				rs.close();
				error = true;
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return error;
	}

	/**
	 * Close a Statement
	 * @param st, the statement to be closed
	 * @return true if the  statement was correctly closed, false in any other case
	 */
	public boolean closeStatement(Statement st) {
		boolean error = false;

		if (st != null) {
			try {
				st.close();
				error = true;
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return error;
	}

	/**
	 * Close a Connection
	 * @param conn, the  connection to be closed
	 * @return true if the connection was correctly closed, false in any other case
	 */
	public boolean closeConnection(IConnection conn) {
		boolean error = false;

		if (conn != null) {
			try {
				conn.close();
				error = true;
			} catch (DBException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return error;
	}

	/**
	 * Tells if user can read contents of the table.
	 * 
	 *  @param iconn connection with the database where the user is connected
	 *  @param tablename to get the permissions over
	 *  @return true if can read, either false
	 *  @throws SQLException
	 */
	public boolean canRead(IConnection iconn, String tablename) throws SQLException {
		String schema = null;
		int dotPos = tablename.indexOf(".");
		if (dotPos > -1) {
			schema = tablename.substring(0, dotPos);
		}
		tablename = tableNameToComposedTableName(tablename);
		Connection conn = ((ConnectionJDBC) iconn).getConnection();
		boolean checkTable = true;
		if (schema != null) {
			if (!schemasUsage.containsKey(schema)) {
				String query = "SELECT has_schema_privilege('" + schema + "', 'USAGE') AS usg";
				Statement st = conn.createStatement();
				ResultSet rs = st.executeQuery(query);
				if (rs.next()) {
					schemasUsage.put(schema, rs.getBoolean("usg"));
				} else {
					//this sentence should never be executed...
					schemasUsage.put(schema, false);
				}
				rs.close();
				st.close();
			}
			checkTable = schemasUsage.get(schema);
		}
		if (checkTable) {
			String query = "SELECT has_table_privilege('" + tablename + "', 'SELECT') as selct";
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			if (rs.next()) {
				return rs.getBoolean("selct");
			} else {
				return false;
			}
		} else return false;
	}

  
	private Integer getGidFieldIndex(String ret[]){
	    for (int i=0; i<ret.length; i++) {
	        if (ret[i].equalsIgnoreCase("gid")) {
	            return new Integer(i);
	        }	    
	    }
	    return null;
	}
	
	private void swapIndexes(String[] ret, int i, int j){
	    if(i!=j && i>=0 && i<ret.length && j>=0 && j<ret.length){
	        String aux = ret[i];
	        ret[i] = ret[j];
	        ret[j] = aux;
	    }
	}

	public String[] getIdFieldsCandidates(IConnection conn, String table_name) throws DBException {

	    String[] ret = getAllFields(conn, table_name);

	    String pk = getPrimaryKey(conn, table_name);

	    if (!pk.equals("")){
	        for (int i = 0; i < ret.length; i++) {
	            if (pk.equals(ret[i])) {
	                    //swap possible gid col with the first element
	                    //in order to make it the default selection on 
	                    //combobox
	                    swapIndexes(ret, i, 0);
	                    break;
	            }
	        }
	    } else {
	        Integer gidFieldIndex = getGidFieldIndex(ret);
	        if (gidFieldIndex!=null){
	            //swap possible gid col with the first element
	            //in order to make it the default selection on 
	            //combobox
	            int index = gidFieldIndex.intValue();
	            swapIndexes(ret, index, 0);
	        } else {
	            for (int i = 0; i < ret.length; i++) {
	                if (isAutoIncrement(conn, table_name, ret[i])) {
	                    //swap possible gid col with the first element
	                    //in order to make it the default selection on 
	                    //combobox
	                    swapIndexes(ret, i, 0);
	                    break;
	                }
	            }
	        }
	    }
	    return ret;
	}

	private boolean isAutoIncrement(IConnection con, String table_name, String colName) {
		
		String query = "SELECT column_default SIMILAR TO 'nextval%regclass%' AS isautoincremental "
			+ "FROM information_schema.columns " 
			+ "WHERE table_name = ? AND table_schema=? " 
			+ "AND column_name=?";
		
		try {
			// get schema and table from the composed tablename
			String[] tokens = table_name.split("\\u002E", 2);
			String schema = "";
			String tableName = "";
			if (tokens.length == 1) {
				tableName = tokens[0];
			} else {
				schema = tokens[0];
				tableName = tokens[1];
			}
			
			
			Connection c = ((ConnectionJDBC)con).getConnection();
			PreparedStatement st = c.prepareStatement(query);
			st.setString(1, tableName);
			st.setString(2, schema);
			st.setString(3, colName);
			
			ResultSet rs = st.executeQuery();
			boolean isAutoincrement = false;
			if (rs.next()) {
				isAutoincrement = rs.getBoolean("isautoincremental");
			}
			
			rs.close();
			st.close();
			
			return isAutoincrement;
		} catch (SQLException e) {
			try {
				con.close();
			} catch (DBException e2) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			return false;

		}
		
		
	}

	   private String getPrimaryKey(IConnection con, String table_name) {

	       String query = "SELECT column_name FROM information_schema.key_column_usage" +
	       		" WHERE table_name=? AND table_schema=? AND constraint_name=?";

	        try {
	            // get schema and table from the composed tablename
	            String[] tokens = table_name.split("\\u002E", 2);
	            String schema = "";
	            String tableName = "";
	            if (tokens.length == 1) {
	                tableName = tokens[0];
	            } else {
	                schema = tokens[0];
	                tableName = tokens[1];
	            }
	            
	            
	            Connection c = ((ConnectionJDBC)con).getConnection();
	            PreparedStatement st = c.prepareStatement(query);
	            st.setString(1, tableName);
	            st.setString(2, schema);
	            st.setString(3, tableName+"_pkey");
	            
	            ResultSet rs = st.executeQuery();
	            
	            String primaryKey = "";
	            if (rs.next()) {
	                primaryKey = rs.getString("column_name");
	            }
	            
	            rs.close();
	            st.close();
	            
	            return primaryKey;
	        } catch (SQLException e) {
	            try {
	                con.close();
	            } catch (DBException e2) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            } 
	            return "";
	        }
	    }
}

