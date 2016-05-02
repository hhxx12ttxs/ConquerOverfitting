/*
 * StaticResultSet.java
 * 
 * Copyright (c) 2001-2002 Labfire, Inc. All rights reserved.
 * Visit http://labfire.com/ for more information. 
 * 
 * This software is the confidential and proprietary information of
 * Labfire, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Labfire.
 */


package com.labfire.fe.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StaticResultSet
 *
 * @author <a href="http://labfire.com/">Labfire, Inc.</a>
 */
public class StaticResultSet implements ResultSet, Cloneable {
	public static final StaticResultSet EMPTY_RS = new StaticResultSet();
	private static final String IMPL_MSG = "Unsupported method";
	private int position = -1;
	private int rowCount = -1;
	private int columnCount = -1;
	private List rows = null;
	private Map columns = null;
	private SQLWarning warnings;
	
	/**
	 * StaticResultSet
	 * StaticResultSet create a new StaticResultSet with the data
	 * from a dynamic ResultSet.
	 * 
	 * @param ResultSet used to populate this StaticResultSet
	 * @throws SQLException if the underlying ResultSet throws one
	 */
	public StaticResultSet(ResultSet rs) throws SQLException {
		columns = new HashMap();
		rows = new ArrayList();
		ArrayList row;
		if (rs != null) {
			rs.beforeFirst();
			rowCount = 0;
			ResultSetMetaData rsmd = rs.getMetaData();
			columnCount = rsmd.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				columns.put(rsmd.getColumnName(i).toUpperCase(), new Integer(i));
			}
			while (rs.next()) {
				row = new ArrayList(columnCount);
				for (int i = 1; i <= columnCount; i++) {
					row.add(rs.getObject(i));
				}
				rows.add(row);
			}
			rowCount = rows.size();
			setWarnings(rs.getWarnings());
			rs.close();
		}
	}
	
	StaticResultSet() {
		columns = new HashMap();
		rows = new ArrayList();
	}
	
	/**
	 * StaticResultSet
	 */
	StaticResultSet(StaticResultSet srs) {
		setColumns(srs.getColumns());
		setRows(srs.getRows());
		setWarnings(srs.getWarnings());
	}
	
	void setRows(List rows) {
		this.rows = rows;
		rowCount = rows.size();
	}
	
	void setColumns(Map columns) {
		this.columns = columns;
		columnCount = columns.size();
	}
	
	List getRows() {
		return this.rows;
	}
	
	Map getColumns() {
		return this.columns;
	}
	
	void setWarnings(SQLWarning warnings) {
		this.warnings = warnings;
	}
	
	public Object clone() {
		return new StaticResultSet(this);
	}
	
	public boolean next() { 
		if ((position + 1) < rowCount) {
			position++;
			return true;
		} else
			return false;
	}

	public void close() { 
		beforeFirst(); 
	}
	
	public boolean wasNull() { 
		return false;
	}
	
	public java.lang.String getString(int S1) {
		if (S1 > 0 && S1 <= (columnCount + 1))
			return ((ArrayList)rows.get(position)).get(S1 - 1).toString();
		else
			return null;
	}
	
	public boolean getBoolean(int S1) {
		if (S1 > 0 && S1 <= (columnCount + 1)) {
			Object o = ((ArrayList)rows.get(position)).get(S1 - 1);
			if (o instanceof Boolean)
				return ((Boolean)o).booleanValue();
		}
		return false; 
	}
	
	public byte getByte(int S1) {
		if (S1 > 0 && S1 <= (columnCount + 1)) {
			Object o = ((ArrayList)rows.get(position)).get(S1 - 1);
			if (o instanceof Number)
				return ((Number)o).byteValue();
		}
		return 0; 
	}
	
	public short getShort(int S1) {
		if (S1 > 0 && S1 <= (columnCount + 1)) {
			Object o = ((ArrayList)rows.get(position)).get(S1 - 1);
			if (o instanceof Number)
				return ((Number)o).shortValue();
		}
		return 0; 
	}

	public int getInt(int S1) {
		if (S1 > 0 && S1 <= (columnCount + 1)) {
			Object o = ((ArrayList)rows.get(position)).get(S1 - 1);
			if (o instanceof Number)
				return ((Number)o).intValue();
		}
		return 0;
	}

	public long getLong(int S1) {
		if (S1 > 0 && S1 <= (columnCount + 1)) {
			Object o = ((ArrayList)rows.get(position)).get(S1 - 1);
			if (o instanceof Number)
				return ((Number)o).longValue();
		}
		return 0;
	}
	
	public float getFloat(int S1) {
		if (S1 > 0 && S1 <= (columnCount + 1)) {
			Object o = ((ArrayList)rows.get(position)).get(S1 - 1);
			if (o instanceof Number)
				return ((Number)o).floatValue();
		}
		return 0;
	}
	
	public double getDouble(int S1) {
		if (S1 > 0 && S1 <= (columnCount + 1)) {
			Object o = ((ArrayList)rows.get(position)).get(S1 - 1);
			if (o instanceof Number)
				return ((Number)o).doubleValue();
		}
		return 0;
	}
	
	/**
	 *  
	 * @see java.sql.ResultSet#getBigDecimal(int, int)
	 * @deprecated 
	 */
	public java.math.BigDecimal getBigDecimal(int S1, int S2) { 
		return null; 
	}

	public byte[] getBytes(int S1) { 
		return new byte[0]; 
	}
	
	public java.sql.Date getDate(int S1) {
		if (S1 > 0 && S1 <= (columnCount + 1)) {
			Object o = ((ArrayList)rows.get(position)).get(S1 - 1);
			if (o instanceof java.sql.Date)
				return (java.sql.Date)o;
		}
		return null;
	}
	
	public java.sql.Time getTime(int S1) {
		if (S1 > 0 && S1 <= (columnCount + 1)) {
			Object o = ((ArrayList)rows.get(position)).get(S1 - 1);
			if (o instanceof java.sql.Time)
				return (java.sql.Time)o;
		}
		return null;
	}
	
	public java.sql.Timestamp getTimestamp(int S1) {
		if (S1 > 0 && S1 <= (columnCount + 1)) {
			Object o = ((ArrayList)rows.get(position)).get(S1 - 1);
			if (o instanceof java.sql.Timestamp)
				return (java.sql.Timestamp)o;
		}
		return null;
	}

	public java.sql.Ref getRef(int S1) {
		if (S1 > 0 && S1 <= (columnCount + 1)) {
			Object o = ((ArrayList)rows.get(position)).get(S1 - 1);
			if (o instanceof java.sql.Ref)
				return (java.sql.Ref)o;
		}
		return null;
	}
	
	public java.sql.Blob getBlob(int S1) {
		if (S1 > 0 && S1 <= (columnCount + 1)) {
			Object o = ((ArrayList)rows.get(position)).get(S1 - 1);
			if (o instanceof java.sql.Blob)
				return (java.sql.Blob)o;
		}
		return null;
	}
	
	public java.sql.Clob getClob(int S1) {
		if (S1 > 0 && S1 <= (columnCount + 1)) {
			Object o = ((ArrayList)rows.get(position)).get(S1 - 1);
			if (o instanceof java.sql.Clob)
				return (java.sql.Clob)o;
		}
		return null;
	}

	public java.io.InputStream getAsciiStream(int S1) { 
		return null; 
	}
	
	/**
	 * 
	 * @see java.sql.ResultSet#getUnicodeStream(int)
	 * @deprecated 
	 */
	public java.io.InputStream getUnicodeStream(int S1) { 
		return null; 
	}
		
	public java.io.InputStream getBinaryStream(int S1) { 
		return null; 
	}
	
	public java.lang.String getString(java.lang.String S1) {
		int column;
		Object o = columns.get(S1.toUpperCase());
		if (o != null && o instanceof Integer) {
			column = ((Integer)o).intValue() - 1;
			o = ((ArrayList)rows.get(position)).get(column);
			if (o != null)
				return o.toString();
			else
				return null;
		} else
			return null;
	}
	
	public boolean getBoolean(java.lang.String S1) {
		int column;
		Object o = columns.get(S1.toUpperCase());
		if (o != null && o instanceof Integer) {
			column = ((Integer)o).intValue() - 1;
			o = ((ArrayList)rows.get(position)).get(column);
			if (o != null && o instanceof Boolean)
				return ((Boolean)o).booleanValue();
		}
		return false;
	}
	
	public byte getByte(java.lang.String S1) {
		int column;
		Object o = columns.get(S1.toUpperCase());
		if (o != null && o instanceof Integer) {
			column = ((Integer)o).intValue() - 1;
			o = ((ArrayList)rows.get(position)).get(column);
			if (o != null && o instanceof Number)
				return ((Number)o).byteValue();
		}
		return 0;
	}

	public short getShort(java.lang.String S1) {
		int column;
		Object o = columns.get(S1.toUpperCase());
		if (o != null && o instanceof Integer) {
			column = ((Integer)o).intValue() - 1;
			o = ((ArrayList)rows.get(position)).get(column);
			if (o != null && o instanceof Number)
				return ((Number)o).shortValue();
		}
		return 0;
	}
	
	public int getInt(java.lang.String S1) {
		int column;
		Object o = columns.get(S1.toUpperCase());
		if (o != null && o instanceof Integer) {
			column = ((Integer)o).intValue() - 1;
			o = ((ArrayList)rows.get(position)).get(column);
			if (o != null && o instanceof Number)
				return ((Number)o).intValue();
		}
		return 0;
	}

	public long getLong(java.lang.String S1) {
		int column;
		Object o = columns.get(S1.toUpperCase());
		if (o != null && o instanceof Integer) {
			column = ((Integer)o).intValue() - 1;
			o = ((ArrayList)rows.get(position)).get(column);
			if (o != null && o instanceof Number)
				return ((Number)o).longValue();
		}
		return 0;
	}
	
	public float getFloat(java.lang.String S1) {
		int column;
		Object o = columns.get(S1.toUpperCase());
		if (o != null && o instanceof Integer) {
			column = ((Integer)o).intValue() - 1;
			o = ((ArrayList)rows.get(position)).get(column);
			if (o != null && o instanceof Number)
				return ((Number)o).floatValue();
		}
		return 0;
	}
	
	public double getDouble(java.lang.String S1) {
		int column;
		Object o = columns.get(S1.toUpperCase());
		if (o != null && o instanceof Integer) {
			column = ((Integer)o).intValue() - 1;
			o = ((ArrayList)rows.get(position)).get(column);
			if (o != null && o instanceof Number)
				return ((Number)o).doubleValue();
		}
		return 0;
	}
	
	/**
	 *  
	 * @see java.sql.ResultSet#getBigDecimal(String, int)
	 * @deprecated 
	 */
	public java.math.BigDecimal getBigDecimal(java.lang.String S1, int S2) { 
		return null;
	}
	
	public byte[] getBytes(java.lang.String S1){ 
		return new byte[0]; 
	}
	
	public java.sql.Date getDate(java.lang.String S1) {
		int column;
		Object o = columns.get(S1.toUpperCase());
		if (o != null && o instanceof Integer) {
			column = ((Integer)o).intValue() - 1;
			o = ((ArrayList)rows.get(position)).get(column);
			if (o != null && o instanceof java.sql.Date)
				return (java.sql.Date)o;
		}
		return null;
	}
	
	public java.sql.Time getTime(java.lang.String S1) {
		int column;
		Object o = columns.get(S1.toUpperCase());
		if (o != null && o instanceof Integer) {
			column = ((Integer)o).intValue() - 1;
			o = ((ArrayList)rows.get(position)).get(column);
			if (o != null && o instanceof java.sql.Time)
				return (java.sql.Time)o;
		}
		return null;
	}
	
	public java.sql.Timestamp getTimestamp(java.lang.String S1) {
		int column;
		Object o = columns.get(S1.toUpperCase());
		if (o != null && o instanceof Integer) {
			column = ((Integer)o).intValue() - 1;
			o = ((ArrayList)rows.get(position)).get(column);
			if (o != null && o instanceof java.sql.Timestamp)
				return (java.sql.Timestamp)o;
		}
		return null;
	}
	
	public java.io.InputStream getAsciiStream(java.lang.String S1) { 
		return null; 
	}
	
	/**
	 * 
	 * @see java.sql.ResultSet#getUnicodeStream(String)
	 * @deprecated 
	 */
	public java.io.InputStream getUnicodeStream(java.lang.String S1) { 
		return null; 
	}
	
	public java.io.InputStream getBinaryStream(java.lang.String S1) { 
		return null; 
	}

	public java.sql.SQLWarning getWarnings() { 
		return warnings; 
	}

	public void clearWarnings() { 
		setWarnings(null);
	}
	
	public java.lang.String getCursorName() { 
		return null; 
	}
	
	public java.sql.ResultSetMetaData getMetaData() { 
		return null; 
	}
	
	public java.lang.Object getObject(int S1) { 
		return ((ArrayList)rows.get(position)).get(S1 - 1);  
	}
	
	public java.lang.Object getObject(java.lang.String S1) {
		int column;
		Object o = columns.get(S1.toUpperCase());
		if (o != null) {
			column = ((Integer)o).intValue() - 1;
			return ((ArrayList)rows.get(position)).get(column);
		}
		return null;
	}
	
	public int findColumn(java.lang.String S1) throws SQLException { 
		Object o = columns.get(S1.toUpperCase());
		if (o != null && o instanceof Integer) {
			return ((Integer)o).intValue();
		} else
			throw new SQLException(S1 + " not a valid column identifier.");
	}
	
	public java.io.Reader getCharacterStream(int S1) { 
		return null; 
	}
	
	public java.io.Reader getCharacterStream(java.lang.String S1) { 
		return null; 
	}
	
	public java.math.BigDecimal getBigDecimal(int S1) {
		if (S1 > 0 && S1 <= (columnCount + 1)) {
			Object o = ((ArrayList)rows.get(position)).get(S1 - 1);
			if (o instanceof java.math.BigDecimal)
				return (java.math.BigDecimal)o;
		}
		return null;
	}
	
	public java.math.BigDecimal getBigDecimal(java.lang.String S1) {
		int column;
		Object o = columns.get(S1.toUpperCase());
		if (o != null && o instanceof Integer) {
			column = ((Integer)o).intValue() - 1;
			o = ((ArrayList)rows.get(position)).get(column);
			if (o != null && o instanceof java.math.BigDecimal)
				return (java.math.BigDecimal)o;
		}
		return null;
	}
	
	public boolean isBeforeFirst() { 
		return (position < 0);
	}
	
	public boolean isAfterLast() { 
		return (position > rowCount);
	}
	
	public boolean isFirst() { 
		return (position == 0); 
	}
	
	public boolean isLast() { 
		return (position == (rowCount - 1));
	}
	
	public void beforeFirst() { 
		position = -1;
	}
	
	public void afterLast() { 
		position = rowCount;
	}
	
	public boolean first() { 
		beforeFirst();
		return next();
	}
	
	public boolean last() { 
		afterLast();
		return previous();
	}
	
	public int getRow() { 
		return position + 1;
	}
	
	public boolean absolute(int S1) { 
		beforeFirst();
		if (S1 < rowCount) {
			position = S1 - 1;
			return true;
		} else
			return false;
	}
	
	public boolean relative(int S1) { 
		if ((position + S1) < rowCount) {
			position += S1;
			return true;
		} else
			return false;
	}
		
	public boolean previous() { 
		if (position > 0) {
			position--;
			return true;
		} else
			return false;
	}

	public java.sql.Array getArray(int S1) {
		if (S1 > 0 && S1 <= (columnCount + 1)) {
			Object o = ((ArrayList)rows.get(position)).get(S1 - 1);
			if (o instanceof java.sql.Array)
				return (java.sql.Array)o;
		}
		return null;
	}

	public java.sql.Ref getRef(java.lang.String S1) {
		int column;
		Object o = columns.get(S1.toUpperCase());
		if (o != null && o instanceof Integer) {
			column = ((Integer)o).intValue() - 1;
			o = ((ArrayList)rows.get(position)).get(column);
			if (o != null && o instanceof java.sql.Ref)
				return (java.sql.Ref)o;
		}
		return null;
	}
	
	public java.sql.Blob getBlob(java.lang.String S1) {
		int column;
		Object o = columns.get(S1.toUpperCase());
		if (o != null && o instanceof Integer) {
			column = ((Integer)o).intValue() - 1;
			o = ((ArrayList)rows.get(position)).get(column);
			if (o != null && o instanceof java.sql.Blob)
				return (java.sql.Blob)o;
		}
		return null;
	}
	
	public java.sql.Clob getClob(java.lang.String S1) {
		int column;
		Object o = columns.get(S1.toUpperCase());
		if (o != null && o instanceof Integer) {
			column = ((Integer)o).intValue() - 1;
			o = ((ArrayList)rows.get(position)).get(column);
			if (o != null && o instanceof java.sql.Clob)
				return (java.sql.Clob)o;
		}
		return null;
	}

	public java.sql.Array getArray(java.lang.String S1) {
		int column;
		Object o = columns.get(S1.toUpperCase());
		if (o != null && o instanceof Integer) {
			column = ((Integer)o).intValue() - 1;
			o = ((ArrayList)rows.get(position)).get(column);
			if (o != null && o instanceof java.sql.Array)
				return (java.sql.Array)o;
		}
		return null;
	}

	public java.net.URL getURL(int S1) {
		if (S1 > 0 && S1 <= (columnCount + 1)) {
			Object o = ((ArrayList)rows.get(position)).get(S1 - 1);
			if (o instanceof java.net.URL)
				return (java.net.URL)o;
		}
		return null;
	}
	
	public java.net.URL getURL(java.lang.String S1) {
		int column;
		Object o = columns.get(S1.toUpperCase());
		if (o != null && o instanceof Integer) {
			column = ((Integer)o).intValue() - 1;
			o = ((ArrayList)rows.get(position)).get(column);
			if (o != null && o instanceof java.net.URL)
				return (java.net.URL)o;
		}
		return null;
	}
	
	public String toString() {
		return "StaticResultSet: " + rows;
	}
	
	public java.lang.Object getObject(java.lang.String S1, java.util.Map S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void setFetchDirection(int S1) throws SQLException { throw new SQLException(IMPL_MSG); }
	public int getFetchDirection() throws SQLException { throw new SQLException(IMPL_MSG); }
	public void setFetchSize(int S1) throws SQLException { throw new SQLException(IMPL_MSG); }
	public int getFetchSize() throws SQLException { throw new SQLException(IMPL_MSG); }
	public int getType() throws SQLException { throw new SQLException(IMPL_MSG); }
	public int getConcurrency() throws SQLException { throw new SQLException(IMPL_MSG); }
	public boolean rowUpdated() throws SQLException { throw new SQLException(IMPL_MSG); }
	public boolean rowInserted() throws SQLException { throw new SQLException(IMPL_MSG); }
	public boolean rowDeleted() throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateNull(int S1) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateBoolean(int S1, boolean S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateByte(int S1, byte S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateShort(int S1, short S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateInt(int S1, int S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateLong(int S1, long S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateFloat(int S1, float S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateDouble(int S1, double S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateBigDecimal(int S1, java.math.BigDecimal S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateString(int S1, java.lang.String S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateBytes(int S1, byte[] S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateDate(int S1, java.sql.Date S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateTime(int S1, java.sql.Time S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateTimestamp(int S1, java.sql.Timestamp S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateAsciiStream(int S1, java.io.InputStream S2, int S3) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateBinaryStream(int S1, java.io.InputStream S2, int S3) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateCharacterStream(int S1, java.io.Reader S2, int S3) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateObject(int S1, java.lang.Object S2, int S3) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateObject(int S1, java.lang.Object S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateNull(java.lang.String S1) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateBoolean(java.lang.String S1, boolean S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateByte(java.lang.String S1, byte S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateShort(java.lang.String S1, short S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateInt(java.lang.String S1, int S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateLong(java.lang.String S1, long S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateFloat(java.lang.String S1, float S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateDouble(java.lang.String S1, double S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateBigDecimal(java.lang.String S1, java.math.BigDecimal S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateString(java.lang.String S1, java.lang.String S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateBytes(java.lang.String S1, byte[] S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateDate(java.lang.String S1, java.sql.Date S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateTime(java.lang.String S1, java.sql.Time S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateTimestamp(java.lang.String S1, java.sql.Timestamp S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateAsciiStream(java.lang.String S1, java.io.InputStream S2, int S3) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateBinaryStream(java.lang.String S1, java.io.InputStream S2, int S3) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateCharacterStream(java.lang.String S1, java.io.Reader S2, int S3) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateObject(java.lang.String S1, java.lang.Object S2, int S3) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateObject(java.lang.String S1, java.lang.Object S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void insertRow() throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateRow() throws SQLException { throw new SQLException(IMPL_MSG); }
	public void deleteRow() throws SQLException { throw new SQLException(IMPL_MSG); }
	public void refreshRow() throws SQLException { throw new SQLException(IMPL_MSG); }
	public void cancelRowUpdates() throws SQLException { throw new SQLException(IMPL_MSG); }
	public void moveToInsertRow() throws SQLException { throw new SQLException(IMPL_MSG); }
	public void moveToCurrentRow() throws SQLException { throw new SQLException(IMPL_MSG); }
	public java.sql.Statement getStatement() throws SQLException { throw new SQLException(IMPL_MSG); }
	public java.lang.Object getObject(int S1, java.util.Map S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public java.sql.Date getDate(int S1, java.util.Calendar S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public java.sql.Date getDate(java.lang.String S1, java.util.Calendar S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public java.sql.Time getTime(int S1, java.util.Calendar S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public java.sql.Time getTime(java.lang.String S1, java.util.Calendar S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public java.sql.Timestamp getTimestamp(int S1, java.util.Calendar S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public java.sql.Timestamp getTimestamp(java.lang.String S1, java.util.Calendar S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateRef(int S1, java.sql.Ref S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateRef(java.lang.String S1, java.sql.Ref S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateBlob(int S1, java.sql.Blob S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateBlob(java.lang.String S1, java.sql.Blob S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateClob(int S1, java.sql.Clob S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateClob(java.lang.String S1, java.sql.Clob S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateArray(int S1, java.sql.Array S2) throws SQLException { throw new SQLException(IMPL_MSG); }
	public void updateArray(java.lang.String S1, java.sql.Array S2) throws SQLException { throw new SQLException(IMPL_MSG); }
}

