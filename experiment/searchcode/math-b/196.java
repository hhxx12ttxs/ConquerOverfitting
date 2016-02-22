/*
 Copyright (c) 2007-2009 WebAppShowCase DBA Appcloem (http://www.appcloem.com). All rights reserved.
Apache Software License 2.0
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in
     the documentation and/or other materials provided with the distribution.

  3. The names of the authors may not be used to endorse or promote products
     derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL WebAppShowCase
OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.jiql.jdbc;
import java.util.*;
import java.sql.*;
import java.io.*;
import org.jiql.util.*;
import tools.util.NameValuePairs;
import tools.util.EZArrayList;
import org.jiql.db.*;
import java.math.*;
import java.net.*;
import org.jiql.db.objs.*;
import org.jiql.db.select.FunctionBase;
import org.jiql.db.jdbc.stat.ResultObj;


public class ResultSet implements java.sql.ResultSet,java.io.Serializable
{
public static Vector jiqldescols = new Vector();
public static Vector descols = new Vector();
public static Vector<String> tcols = new Vector<String>();
public static Vector<String> pkcols = new Vector<String>();
public static Vector<String> ekcols = new Vector<String>();
public static Vector<String> identity = new Vector<String>();
public static Vector<String> foundrows = new Vector<String>();
public static Vector<String> indexv = new Vector<String>();

ResultObj rmo = null;

Vector results = null;
boolean wasNull = false;
int indx = 0;
int range = 0;
boolean close = false;
SQLParser sqp = null;
java.sql.Statement statement = null;
static {
	descols.add("Field");
	descols.add("Type");
	descols.add("Null");
	descols.add("Key");
	descols.add("Default");
	descols.add("Extra");
	
	jiqldescols.add("prefix");
	jiqldescols.add("prefixName");
	jiqldescols.add("tableleafs");
	jiqldescols.add("leafcount");
//TABLE_CAT  	TABLE_SCHEM  	TABLE_NAME  	COLUMN_NAME  	DATA_TYPE  	TYPE_NAME  	COLUMN_SIZE  	BUFFER_LENGTH  	
//DECIMAL_DIGITS  	NUM_PREC_RADIX  	NULLABLE  	REMARKS  	COLUMN_DEF  	SQL_DATA_TYPE  	
//SQL_DATETIME_SUB  	CHAR_OCTET_LENGTH  	ORDINAL_POSITION  	IS_NULLABLE
	tcols.add("TABLE_CAT");
	tcols.add("TABLE_SCHEM");
	tcols.add("TABLE_NAME");
	tcols.add("COLUMN_NAME");
	tcols.add("DATA_TYPE");
	tcols.add("TYPE_NAME");
	tcols.add("COLUMN_SIZE");
	tcols.add("BUFFER_LENGTH");
	tcols.add("DECIMAL_DIGITS");
	tcols.add("NUM_PREC_RADIX");
	tcols.add("NULLABLE");
	tcols.add("REMARKS");
	tcols.add("COLUMN_DEF");
	tcols.add("SQL_DATA_TYPE");
	tcols.add("SQL_DATETIME_SUB");
	tcols.add("CHAR_OCTET_LENGTH");
	tcols.add("ORDINAL_POSITION");
	tcols.add("IS_NULLABLE");

	pkcols.add("TABLE_CAT");
	pkcols.add("TABLE_SCHEM");
	pkcols.add("TABLE_NAME");
	pkcols.add("COLUMN_NAME");
	pkcols.add("KEY_SEQ");
	pkcols.add("PK_NAME");


//getExportedKeys
//PKTABLE_CAT  	PKTABLE_SCHEM  	PKTABLE_NAME  	PKCOLUMN_NAME  	FKTABLE_CAT  	
//FKTABLE_SCHEM  	FKTABLE_NAME  	FKCOLUMN_NAME  	KEY_SEQ  	UPDATE_RULE  	
//DELETE_RULE  	FK_NAME  	PK_NAME  	DEFERRABILITY
//	WS_PORTALU 	REALM_USERROLE 	REALM_USER 		WS_PORTALU 	REALM_USER 	REALM_USERNAME 	1 	3 	3 	WS_USERID_FK 	SQL090716111225810 	7

	
	ekcols.add("PKTABLE_CAT");
	ekcols.add("PKTABLE_SCHEM");
	ekcols.add("PKTABLE_NAME");
	ekcols.add("PKCOLUMN_NAME");
	ekcols.add("FKTABLE_CAT");
	ekcols.add("FKTABLE_SCHEM");
	ekcols.add("FKTABLE_NAME");
	ekcols.add("FKCOLUMN_NAME");
	ekcols.add("KEY_SEQ");
	ekcols.add("UPDATE_RULE");
	ekcols.add("DELETE_RULE");
	ekcols.add("FK_NAME");
	ekcols.add("PK_NAME");
	ekcols.add("DEFERRABILITY");
	identity.add("@@identity");	
	foundrows.add("FOUND_ROWS()");
	
	indexv.add("TABLE_CAT");
	indexv.add("TABLE_SCHEM");
	indexv.add("TABLE_NAME");
	indexv.add("NON_UNIQUE");
	indexv.add("INDEX_QUALIFIER");
	indexv.add("INDEX_NAME");
	indexv.add("TYPE");
	indexv.add("ORDINAL_POSITION");
	indexv.add("COLUMN_NAME");
	indexv.add("ASC_OR_DESC");
	indexv.add("CARDINALITY");
	indexv.add("PAGES");
	indexv.add("FILTER_CONDITION");
	
}
public ResultSet(Vector r,SQLParser s){
	results = r;
	
	sqp = s;
	if (results != null && sqp.getConnection() != null)
		sqp.getConnection().setFoundRows(size());
	indx = sqp.getSelectParser().getLimit().getBegin();
	range = sqp.getSelectParser().getLimit().getRange();
}

public void reset(){
	indx = 0;
	wasNull = false;
	close = false;
}
public Vector getResults(){
	return results;
}

public SQLParser getSQLParser(){
	return sqp;
}

	public void setResultMetaObj(ResultObj ro){
		rmo = ro;
	}
protected String getColumnName(int  ci) throws SQLException{
	//NameValuePairs nvp = (NameValuePairs)results.elementAt(0);
	//EZArrayList ez = new EZArrayList(nvp.keys());
	if (rmo != null)
		return rmo.getColumnName(ci);
	if (sqp.showTables()){
			if (isSchema())
			{
				rsetLog(" getColumnName TABLE_TYPE ");
				if ( ci == 2)
				return "TABLE_CATALOG"	;
				return "tablename";

			}
	
						if (ci == 1)
				return "TABLE_CAT";
			if (ci == 2)
				return "TABLE_SCHEM";
			if (ci == 4)
				return "TABLE_TYPE";
	
		return "tablename";
	}
	
	if (sqp.isSpecial()){
		if (sqp.getAction().equals("getIdentity"))
		return identity.elementAt(ci -1).toString();
		else if (sqp.getAction().equals("getFoundRows"))
		return foundrows.elementAt(ci -1).toString();
		else if (sqp.getAction().equals("getIndex"))
		return indexv.elementAt(ci -1).toString();
		else if (sqp.getAction().equals("getTypeInfo"))
		return Gateway.get(sqp.getProperties()).getTypeinfoCols().elementAt(ci -1).toString();
		if (sqp.getAction().equals("getGeneratedKeys"))
		return "GENERATED_KEY";

	}
	if (sqp.getAction().equals("describeTable"))
		return descols.elementAt(ci -1).toString();
	if (sqp.getAction().equals("getColumns"))
		return tcols.elementAt(ci -1).toString();
	if (sqp.getAction().equals("getPrimaryKeys"))
		return pkcols.elementAt(ci -1).toString();
	if (sqp.getAction().equals("getExportedKeys")|| sqp.getAction().equals("getImportedKeys"))
		return ekcols.elementAt(ci -1).toString();
	if (sqp.getAction().equals("jiqldescribeTable"))
		return jiqldescols.elementAt(ci -1).toString();
	Vector ez = sqp.getOriginalSelectList();
	if (sqp.getSelectList().size() == 1 && sqp.getSelectList().elementAt(0).equals("*"))
		return sqp.getJiqlTableInfo().getFieldList().elementAt(ci -1).toString();
	return ez.elementAt(ci -1).toString();
}

protected  Row getRowObject(int i) throws SQLException{
	rsetLog(size() + " getRowObject " + i);
	Row nvp = (Row)results.elementAt(i -1);
	nvp.noNulls(true);
	return nvp;

}
protected  Row getRowObject() throws SQLException{
	return getRowObject(indx);
}



public	boolean 	isWrapperFor(Class<?> iface)throws SQLException{
	return false;
}
      //    Returns true if this either implements the interface argument or is directly or indirectly a wrapper for an object that does.
public <T> T unwrap(Class<T> iface)throws SQLException
{
	rsetLog("RESL 20");
	return null;
}


public boolean absolute(int row) throws SQLException{
	rsetLog("RESL 21");

if (row >= size())return false;
indx = row - 1;
return true;
}
 //         Moves the cursor to the given row number in this ResultSet object. 
public  void afterLast() throws SQLException{
	rsetLog("RESL 22");

indx = size();
}
 //           Moves the cursor to the end of this ResultSet object, just after the last row. 
public  void beforeFirst() throws SQLException{
	rsetLog("beforeFirst");

indx = 0;
}
 //           Moves the cursor to the front of this ResultSet object, just before the first row. 
public  void cancelRowUpdates() throws SQLException{
	rsetLog("RMISC 1");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Cancels the updates made to the current row in this ResultSet object. 
public  void clearWarnings() throws SQLException{
	rsetLog("RMISC 2");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Clears all warnings reported on this ResultSet object. 
public  void close()  throws SQLException{
	results = null;
	close = true;
}
 //           Releases this ResultSet object's database and JDBC resources immediately instead of waiting for this to happen when it is automatically closed. 
public  void deleteRow() throws SQLException{
	rsetLog("RMISC 3");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Deletes the current row from this ResultSet object and from the underlying database. 
public  int findColumn(String columnLabel) throws SQLException{
	rsetLog("RESL 24");

		if (sqp.showTables()){
	
					if (columnLabel.equalsIgnoreCase("TABLE_SCHEM"))
					return 2;	
				if (columnLabel.equalsIgnoreCase("TABLE_CAT"))
					return 1;	
				if (columnLabel.equalsIgnoreCase("TABLE_TYPE"))
					return 4;	
			return 3;
		}
	Vector ez = sqp.getOriginalSelectList();
		if (rmo != null)
		return rmo.findColumn(columnLabel);

	
		if (sqp.isSpecial()){
		if (sqp.getAction().equals("getIdentity"))
		ez = identity;
		else if (sqp.getAction().equals("getFoundRows"))
		ez = foundrows;
		else if (sqp.getAction().equals("getIndex"))
		ez = indexv;
		else if (sqp.getAction().equals("getTypeInfo"))
		ez = Gateway.get(sqp.getProperties()).getTypeinfoCols();
	}
	
	if (sqp.getAction().equals("describeTable"))
		ez = descols;
	if (sqp.getAction().equals("getColumns"))
		ez = tcols;
	if (sqp.getAction().equals("getPrimaryKeys"))
		ez = pkcols;
	if (sqp.getAction().equals("getExportedKeys")|| sqp.getAction().equals("getImportedKeys"))
		ez = ekcols;

	if (sqp.getAction().equals("jiqldescribeTable"))
		ez = jiqldescols;
	for (int ct = 0;ct < ez.size();ct++)
		if (columnLabel.equals(ez.elementAt(ct)))
			return ct + 1;
	return -1;
}


 //           Maps the given ResultSet column label to its ResultSet column index. 
public  boolean first()  throws SQLException{
	rsetLog("RESL 25");

indx = 1;
return true;
}
 //           Moves the cursor to the first row in this ResultSet object. 
public  Array getArray(int columnIndex)  throws SQLException{
	rsetLog("RMISC 4");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as an Array object in the Java programming language. 
public  Array getArray(String columnLabel) throws SQLException{
	rsetLog("RMISC 5");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as an Array object in the Java programming language. 
public  InputStream getAsciiStream(int columnIndex)  throws SQLException{
	rsetLog("RESL 26");

	Object r = getObject(columnIndex);
	if (r == null)return null;
	return new ByteArrayInputStream(r.toString().getBytes());

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a stream of ASCII characters. 
public  InputStream getAsciiStream(String columnLabel)  throws SQLException{
	rsetLog("RESL 27");

	return getAsciiStream(findColumn(columnLabel));

}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a stream of ASCII characters. 
public  BigDecimal getBigDecimal(int columnIndex)  throws SQLException{
	rsetLog("RESL 28");

	Object r = getObject(columnIndex);
	if (r == null)return null;
	return new BigDecimal(r.toString());


}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a java.math.BigDecimal with full precision. 
public  BigDecimal getBigDecimal(int columnIndex, int scale)  throws SQLException{
	rsetLog("RMISC 6");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Deprecated.   
public  BigDecimal getBigDecimal(String columnLabel) throws SQLException{
	rsetLog("RESL 29");

return getBigDecimal(findColumn(columnLabel));

}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a java.math.BigDecimal with full precision. 
public  BigDecimal getBigDecimal(String columnLabel, int scale)  throws SQLException{
	rsetLog("RMISC 7");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Deprecated.   
public  InputStream getBinaryStream(int columnIndex)  throws SQLException{
	rsetLog("RESL 30");

		return getBinaryStream(getColumnName(columnIndex));



}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a stream of uninterpreted bytes. 
public  InputStream getBinaryStream(String columnLabel)  throws SQLException{
	rsetLog("RESL 31");

	Object r = getObject(columnLabel);
	if (r == null)return null;
	if(r instanceof jiqlBlob)
		return ((jiqlBlob)r).getBinaryStream();
		
	return new ByteArrayInputStream(r.toString().getBytes());

}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a stream of uninterpreted bytes. 
public Blob getBlob(int columnIndex)  throws SQLException{
	rsetLog("RMISC 8");

	return getBlob(getColumnName(columnIndex));

}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a Blob object in the Java programming language. 
public Blob getBlob(String columnLabel)  throws SQLException{
	rsetLog("RMISC 9");

		Object b = getObject(columnLabel);
	if (b == null)
		wasNull = true;
	else
		wasNull = false;
	if (b == null)return null;
//("CANE YOU SEEa  " + b);
	return (jiqlBlob)b;
		
		
}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a Blob object in the Java programming language. 
public  boolean getBoolean(int columnIndex) throws SQLException{
	rsetLog("RESL 32");

	return getBoolean(getColumnName(columnIndex));
}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a boolean in the Java programming language. 
public  boolean getBoolean(String columnLabel)  throws SQLException{
	rsetLog("RESL 33");
	checkNull(getRowObject().get(columnLabel));

	boolean b= getRowObject().getBoolean(columnLabel);

	return b;
}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a boolean in the Java programming language. 
public  byte getByte(int columnIndex)  throws SQLException{
	rsetLog("RESL 34");

	byte b = getByte(getColumnName(columnIndex));

	return b;
}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a byte in the Java programming language. 
public byte getByte(String columnLabel)  throws SQLException{
	rsetLog("RESL 35");

	checkNull(getRowObject().get(columnLabel));


	return getRowObject().getByte(columnLabel);

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a byte in the Java programming language. 
public  byte[] getBytes(int columnIndex)  throws SQLException{
	rsetLog("getBytes " + columnIndex );

	return getBytes(getColumnName(columnIndex));

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a byte array in the Java programming language. 
public  byte[] getBytes(String columnLabel)  throws SQLException{
	rsetLog("getBytes " + columnLabel);

		if (sqp.showTables()){

				if (columnLabel.equalsIgnoreCase("TABLE_SCHEM"))
					return "jiql".getBytes();	
				if (columnLabel.equalsIgnoreCase("TABLE_CAT"))
					return "".getBytes();	
				if (columnLabel.equalsIgnoreCase("TABLE_TYPE"))
					return "TABLE".getBytes();
						if (columnLabel.equalsIgnoreCase("TABLE_CATALOG"))
					return null;
			columnLabel = "tablename";
		}


	Object b = getObject(columnLabel);
	if (b == null)
		wasNull = true;
	else
		wasNull = false;
	if (b == null)return null;
	return b.toString().getBytes();
}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a byte array in the Java programming language. 
public  Reader getCharacterStream(int columnIndex)  throws SQLException{
	rsetLog("RESL 38");

	return getCharacterStream(getColumnName(columnIndex));

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a java.io.Reader object. 
public  Reader getCharacterStream(String columnLabel)  throws SQLException{
	rsetLog("RESL 39");

	Object r = getRowObject().get(columnLabel);
	if (r == null)
		wasNull = true;
	else
		wasNull = false;

	if (r == null)return null;
	return new StringReader(r.toString());


}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a java.io.Reader object. 
public  Clob getClob(int columnIndex)  throws SQLException{
	rsetLog("RMISC 10");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a Clob object in the Java programming language. 
public  Clob getClob(String columnLabel)  throws SQLException{
	rsetLog("RMISC 11");

	throw  JGException.get("not_supported","NOT SUPPORTED");

}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a Clob object in the Java programming language. 
public  int getConcurrency()  throws SQLException{
	rsetLog("RESL 40");

return CONCUR_READ_ONLY;
}
 //            Retrieves the concurrency mode of this ResultSet object. 
public  String getCursorName()  throws SQLException{
	rsetLog("RMISC 12");

	throw new SQLFeatureNotSupportedException (JGException.get("not_supported","NOT SUPPORTED").toString());

}
 //            Retrieves the name of the SQL cursor used by this ResultSet object. 
public  java.sql.Date getDate(int columnIndex)  throws SQLException{
	rsetLog("RESL 41");

	return getDate(getColumnName(columnIndex));
	//throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a java.sql.Date object in the Java programming language. 
public  java.sql.Date getDate(int columnIndex, Calendar cal)  throws SQLException{
	rsetLog("RESL 42");

	return getDate(getColumnName(columnIndex));
	//throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a java.sql.Date object in the Java programming language. 
public  java.sql.Date getDate(String columnLabel)  throws SQLException{
	rsetLog("RESL 43");

	java.sql.Date b = (java.sql.Date)getRowObject().get(columnLabel);//.getLong(columnLabel));

	if (b == null)
		wasNull = true;
	else
		wasNull = false;
	return b;
	//throw JGException.get("not_supported","NOT SUPPORTED");

}
 //             Retrieves the value of the designated column in the current row of this ResultSet object as a java.sql.Date object in the Java programming language. 
public  java.sql.Date getDate(String columnLabel, Calendar cal)  throws SQLException{
	rsetLog("RMISC 13");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a java.sql.Date object in the Java programming language. 
public  double getDouble(int columnIndex)  throws SQLException{
	rsetLog("RESL 44");

	return getDouble(getColumnName(columnIndex));

}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a double in the Java programming language. 

protected void checkNull(Object b){
		if (b == null)
		wasNull = true;
	else
		wasNull = false;
}
public  double getDouble(String columnLabel) throws SQLException{
	rsetLog("RESL 45");
	checkNull(getRowObject().get(columnLabel));
	return getRowObject().getDouble(columnLabel);

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a double in the Java programming language. 
public  int getFetchDirection()  throws SQLException{
	rsetLog("RESL 46");

return 0;
}
 //            Retrieves the fetch direction for this ResultSet object. 
public  int getFetchSize()  throws SQLException{
	rsetLog("RESL 47");

return 0;
}
 //            Retrieves the fetch size for this ResultSet object. 
public  float getFloat(int columnIndex)  throws SQLException{
	rsetLog("RESL 48");

	return getFloat(getColumnName(columnIndex));

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a float in the Java programming language. 
public  float getFloat(String columnLabel) throws SQLException{
	rsetLog("RESL 49");
	checkNull(getRowObject().get(columnLabel));
	return getRowObject().getFloat(columnLabel);

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a float in the Java programming language. 
public  int getHoldability()  throws SQLException{
	rsetLog("RESL 50");

return CLOSE_CURSORS_AT_COMMIT ;
}
 //            Retrieves the holdability of this ResultSet object 
public  int getInt(int columnIndex)  throws SQLException{
	rsetLog("getInt " + columnIndex);

	return getInt(getColumnName(columnIndex));

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as an int in the Java programming language. 
public  int getInt(String columnLabel)  throws SQLException{
	rsetLog(sqp.isSpecial() + " getInt " + columnLabel);
		if ( sqp.isSpecial())
		{
				if (columnLabel.equalsIgnoreCase("FOUND_ROWS()"))
					return new Integer(sqp.getConnection().getFoundRows()).intValue();
				if (columnLabel.equalsIgnoreCase("@@identity"))
					return new Integer(sqp.getConnection().getIdentity()).intValue();
			
		}
	
		Object o = null;
		if (rmo != null)
		o = rmo.getValue(indx,columnLabel);
		else
		o = getRowObject().get(columnLabel);
	//	checkNull(o);

//		FunctionBase fb = sqp.getSelectParser().getSQLFunctionParser().getFunction(columnLabel);
//if (fb != null)o = fb.process(o);
		if (o == null)
		return 0; 

	if (o instanceof Long)
		return ((Long)o).intValue();
	return new Integer(o.toString());
		

	
	//checkNull(o);
	//return getRowObject().getInt(columnLabel);

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as an int in the Java programming language. 
public  long getLong(int columnIndex)  throws SQLException{
	rsetLog("RESL 53 " + columnIndex);

	return getLong(getColumnName(columnIndex));

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a long in the Java programming language. 
public  long getLong(String columnLabel)  throws SQLException{
	rsetLog("RESL 54 " + columnLabel);
		if ( sqp.isSpecial())
		{
				if (columnLabel.equalsIgnoreCase("FOUND_ROWS()"))
					return new Long(sqp.getConnection().getFoundRows()).longValue();
				if (columnLabel.equalsIgnoreCase("@@identity")){
				long ld = new Long(sqp.getConnection().getIdentity()).longValue();
				rsetLog("getLong.@@identity " + ld);
					return ld;
				}
			
		}
//		checkNull(getRowObject().get(columnLabel));

//	return getRowObject().getLong(columnLabel);
		Object b = getObject(columnLabel);
	if (b == null)
		wasNull = true;
	else
		wasNull = false;
	if (b == null)return 0;
//("CANE YOU SEEa  " + b);
	if (b instanceof Long)
		return (Long)b;
	return new Long(b.toString());


}
 //             Retrieves the value of the designated column in the current row of this ResultSet object as a long in the Java programming language. 

boolean catalog = false;
public boolean isCatalog(){
	return catalog;
}

public void setIsCatalog(boolean tf){
	catalog = tf;
}

boolean schema = false;
public boolean isSchema(){
	return schema;
}

public void setIsSchema(boolean tf){
	schema = tf;
}

jiqlResultSetMetaData jrd = null;
public  ResultSetMetaData getMetaData()  throws SQLException{
	rsetLog("RESL getMetaData");

	if (isClosed())
		throw JGException.get("operation_not_allowed_after_closed","Operation not allowed after close");
	if (jrd != null)return jrd;
	jrd = new jiqlResultSetMetaData(sqp);
	jrd.setIsCatalog(catalog);
	jrd.setIsSchema(schema);
	jrd.setResultMetaObj(rmo);
	return jrd;
}
 //            Retrieves the number, types and  of this ResultSet object's columns. 
public  Reader getNCharacterStream(int columnIndex)  throws SQLException{
	rsetLog("RMISC 15");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //             Retrieves the value of the designated column in the current row of this ResultSet object as a java.io.Reader object. 
public  Reader getNCharacterStream(String columnLabel)  throws SQLException{
	rsetLog("RMISC 16");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a java.io.Reader object. 
public  NClob getNClob(int columnIndex)  throws SQLException{
	rsetLog("RMISC 17");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a NClob object in the Java programming language. 
public  NClob getNClob(String columnLabel)  throws SQLException{
	rsetLog("RMISC 18");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a NClob object in the Java programming language. 
public  String getNString(int columnIndex) throws SQLException{
	rsetLog("RMISC 19");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a String in the Java programming language. 
public  String getNString(String columnLabel)  throws SQLException{
	rsetLog("RMISC 20");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a String in the Java programming language. 
public  Object getObject(int columnIndex)  throws SQLException{
	rsetLog("RESL 56");

	return getObject(getColumnName(columnIndex));

}
 //            Gets the value of the designated column in the current row of this ResultSet object as an Object in the Java programming language. 
public  Object getObject(int columnIndex, Map<String,Class<?>> map)  throws SQLException{
	rsetLog("RMISC 21");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as an Object in the Java programming language. 

/*public String getRName(String col){
	if (col.equals("TABLE_CAT"))
		col = "tablename";
	return col;
}*/
public  Object getObject(String columnLabel)  throws SQLException{
	rsetLog("getObject " + columnLabel);

		if (rmo != null)
		return rmo.getValue(indx,columnLabel);

	Row r = getRowObject();
	
		

		if ( sqp.isSpecial())
		{
				if (columnLabel.equalsIgnoreCase("FOUND_ROWS()")){
					wasNull = false;
					return new Integer(sqp.getConnection().getFoundRows());
				}
				else if (sqp.getAction().equals("getTypeInfo")){
				
					Object o = r.getObject(columnLabel);
					checkNull(o);

					return o;
				}
			
		}
		if (sqp.showTables()){
				if (columnLabel.equalsIgnoreCase("TABLE_SCHEM")){
				wasNull = false;
					return "jiql";	
				}
				if (columnLabel.equalsIgnoreCase("TABLE_CAT")){
				wasNull = false;
					return "";	
				}
				if (columnLabel.equalsIgnoreCase("TABLE_TYPE")){
				wasNull = false;
					return "TABLE";
				}
						if (columnLabel.equalsIgnoreCase("TABLE_CATALOG")){
					wasNull = true;	
					return null;
						}
					

			columnLabel = "tablename";
		}

if (sqp.isCount()){
columnLabel = "count(*)";
}

	Object o = r.get(columnLabel);
	
	if (o == null){
		Hashtable h = sqp.getSelectAS();
		Enumeration en = h.keys();
		String cn = null;
		String cv = null;
		while (en.hasMoreElements())
		{
			cn = en.nextElement().toString();
			cv = h.get(cn).toString();
			if (cv.equals(columnLabel)){
				wasNull = false;
				return r.get(cn);
			}
		}
		//(columnLabel + " sqp.getRealColName(columnLabel) " + sqp.getRealColName(columnLabel) + ":" + r);
	o = r.get(sqp.getRealColName(columnLabel));
	if (o == null){
		String t = sqp.getUnion().findTable(columnLabel);
		if (t != null){
			SQLParser s = sqp.getUnion().getSQLParser(t);
		if (s != null)
			o = r.get(s.getRealColName(columnLabel));
		}
	}

	}
	FunctionBase fb = sqp.getSelectParser().getSQLFunctionParser().getFunction(columnLabel);
if (fb != null)o = fb.process(o,sqp); 
	checkNull(o);
	return o;
}
 //            Gets the value of the designated column in the current row of this ResultSet object as an Object in the Java programming language. 
public  Object getObject(String columnLabel, Map<String,Class<?>> map)  throws SQLException{
	rsetLog("RMISC 22");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as an Object in the Java programming language. 
public  Ref getRef(int columnIndex)  throws SQLException{
	rsetLog("RMISC 23");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a Ref object in the Java programming language. 
public  Ref getRef(String columnLabel)  throws SQLException{
	rsetLog("RMISC 24");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //             Retrieves the value of the designated column in the current row of this ResultSet object as a Ref object in the Java programming language. 
public  int getRow()  throws SQLException{
	rsetLog("getRow " + indx);

return indx;
}
 //            Retrieves the current row number. 
public  RowId getRowId(int columnIndex)  throws SQLException{
	rsetLog("RESL 58");

	return getRowId(getColumnName(columnIndex));

}
 //            Retrieves the value of the designated column in the current row of this ResultSet object as a java.sql.RowId object in the Java programming language. 
public  RowId getRowId(String columnLabel) throws SQLException{
	rsetLog("RESL 59");

	return new org.jiql.jdbc.RowId(getRowObject().getRowId() + ":" + columnLabel);

}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a java.sql.RowId object in the Java programming language. 
public  short getShort(int columnIndex) throws SQLException{
	rsetLog("RESL 60");

	return getShort(getColumnName(columnIndex));
}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a short in the Java programming language. 
public  short getShort(String columnLabel)  throws SQLException{
	rsetLog("RESL 61");
	//checkNull(getRowObject().get(columnLabel));
	//return getRowObject().getShort(columnLabel);
	
	Object b = getObject(columnLabel);
	if (b == null)
		wasNull = true;
	else
		wasNull = false;
	if (b == null)return 0;
//("CANE YOU SEEa  " + b);
	if (b instanceof Long)
		return ((Long)b).shortValue();
	return new Short(b.toString());
	

}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a short in the Java programming language. 
public  SQLXML getSQLXML(int columnIndex) throws SQLException{
	rsetLog("RMISC 25");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Retrieves the value of the designated column in the current row of this ResultSet as a java.sql.SQLXML object in the Java programming language. 
public  SQLXML getSQLXML(String columnLabel)  throws SQLException{
	rsetLog("RMISC 26");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Retrieves the value of the designated column in the current row of this ResultSet as a java.sql.SQLXML object in the Java programming language. 
public  java.sql.Statement getStatement()  throws SQLException{
	rsetLog("RESL 62");

	return statement;
}
 //           Retrieves the Statement object that produced this ResultSet object. 
public  String getString(int columnIndex)  throws SQLException{
	rsetLog("getString " + columnIndex);

		return getString(getColumnName(columnIndex));

}

void rsetLog(String t){
	//tools.util.LogMgr.debug("rsetLog:" + t);
}

 //           Retrieves the value of the designated column in the current row of this ResultSet object as a String in the Java programming language. 
public  String getString(String columnLabel)  throws SQLException{
	//( () + ":" + indx + ":year " + columnLabel);
	rsetLog( " getString a " + columnLabel);
		if (rmo != null){
			Object o = rmo.getValue(indx,columnLabel);
			if (o == null)return null;
			return o.toString();
		}

		if ( sqp.isSpecial())
		{
				if (columnLabel.equalsIgnoreCase("FOUND_ROWS()"))
					return new Integer(sqp.getConnection().getFoundRows()).toString();
			
		}
		if (sqp.showTables()){

				if (columnLabel.equalsIgnoreCase("TABLE_SCHEM"))
					return "jiql";	
				if (columnLabel.equalsIgnoreCase("TABLE_CAT"))
					return "";	
				if (columnLabel.equalsIgnoreCase("TABLE_TYPE"))
					return "TABLE";
						if (columnLabel.equalsIgnoreCase("TABLE_CATALOG"))
					return null;
			columnLabel = "tablename";
		}
	/*if (sqp.getAction().equals("jiql "))
	{
		if (column == 1 || column == 3)
		return Types.BOOLEAN;
		if ( column == 4)
		return  ;
		return Types.VARCHAR;
	}*/

	String s =  getRowObject().getString(columnLabel);
		checkNull(s);

	rsetLog(s + " getString " + columnLabel);

	if (sqp.showTables() || sqp.getAction().equals("jiqldescribeTable")|| sqp.getAction().equals("describeTable")|| sqp.getAction().equals("getColumns")|| sqp.getAction().equals("getPrimaryKeys") || sqp.getAction().equals("getExportedKeys")|| sqp.getAction().equals("getImportedKeys") || sqp.isSpecial())
		return s;

	if (s == null)return null;
	//(s + " RGS " + columnLabel + ":" + sqp.getTableInfo() + ":" + sqp.getTableInfo().getColumnInfo(columnLabel));
	ColumnInfo ci = sqp.getTableInfo().getColumnInfo(columnLabel);
	if (ci == null)return s;
	Object o = jiqlCellValue.getObj(s,ci.getColumnType(),sqp);
	return o.toString();

}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a String in the Java programming language. 
public  Time getTime(int columnIndex) throws SQLException{
	rsetLog("RMISC 27");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a java.sql.Time object in the Java programming language. 
public  Time getTime(int columnIndex, Calendar cal) throws SQLException{
	rsetLog("RMISC 28");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a java.sql.Time object in the Java programming language. 
public  Time getTime(String columnLabel) throws SQLException{
	rsetLog("RMISC 29");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a java.sql.Time object in the Java programming language. 
public  Time getTime(String columnLabel, Calendar cal) throws SQLException{
	rsetLog("RMISC 30");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a java.sql.Time object in the Java programming language. 
public  Timestamp getTimestamp(int columnIndex) throws SQLException{
	rsetLog("RMISC 31");

	return getTimestamp(getColumnName(columnIndex));
}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a java.sql.Timestamp object in the Java programming language. 
public  Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException{
	rsetLog("RMISC 32");

	return getTimestamp(getColumnName(columnIndex));
}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a java.sql.Timestamp object in the Java programming language. 
public  Timestamp getTimestamp(String columnLabel) throws SQLException{
	rsetLog("RMISC 330");

	java.sql.Date b = (java.sql.Date)getRowObject().get(columnLabel);//.getLong(columnLabel));
	Timestamp r = null;
	if (b == null)
		wasNull = true;
	else
	{	
		wasNull = false;
		r = new Timestamp(b.getTime());
	}	
	return r;
}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a java.sql.Timestamp object in the Java programming language. 
public  Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException{
	rsetLog("RMISC 34");

	return getTimestamp(columnLabel);
}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a java.sql.Timestamp object in the Java programming language. 
public  int getType() throws SQLException{
rsetLog("RESL 1");
	return TYPE_FORWARD_ONLY;
}
 //            Retrieves the type of this ResultSet object. 
public  InputStream getUnicodeStream(int columnIndex) throws SQLException{
	rsetLog("RMISC 35");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Deprecated. use getCharacterStream in place of getUnicodeStream 
public  InputStream getUnicodeStream(String columnLabel) throws SQLException{
	rsetLog("RMISC 36");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Deprecated. use getCharacterStream instead 
public  URL getURL(int columnIndex) throws SQLException{
		try{
rsetLog("RESL 2");		
		return getURL(getColumnName(columnIndex));
	}catch (Exception e){
		throw new SQLException(e.toString());
	}
}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a java.net.URL object in the Java programming language. 
public  URL getURL(String columnLabel)  throws SQLException{
	try{
	rsetLog("RESL 3");
		checkNull(getRowObject().get(columnLabel));

	return new URL(getRowObject().get(columnLabel).toString());
	}catch (Exception e){
		throw new SQLException(e.toString());
	}
}
 //           Retrieves the value of the designated column in the current row of this ResultSet object as a java.net.URL object in the Java programming language. 
public  SQLWarning getWarnings() throws SQLException{
rsetLog("RESL 4");
	return null;
}
 //           Retrieves the first warning reported by calls on this ResultSet object. 
public  void insertRow() throws SQLException{
	rsetLog("RMISC 37");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Inserts the contents of the insert row into this ResultSet object and into the database. 
public  boolean isAfterLast() throws SQLException{
rsetLog("RESL 5");
return (indx > size());
}
 //            Retrieves whether the cursor is after the last row in this ResultSet object. 
public  boolean isBeforeFirst()  throws SQLException{
 rsetLog("RESL 6");
 return (indx == 0);
}
 //            Retrieves whether the cursor is before the first row in this ResultSet object. 
public  boolean isClosed() throws SQLException{
 rsetLog("isClosed " + close );
 return close;
}
 //            Retrieves whether this ResultSet object has been closed. 
public  boolean isFirst() throws SQLException{
rsetLog("RESL 8");
return  (indx ==1);

}
 //           Retrieves whether the cursor is on the first row of this ResultSet object. 
public  boolean isLast()  throws SQLException{
rsetLog("RESL 9");
return  (indx >= size());
}
 //            Retrieves whether the cursor is on the last row of this ResultSet object. 
public  boolean last() throws SQLException{
	indx = size();
	
rsetLog("last " + indx);

	return (indx > 0);
}
 //            Moves the cursor to the last row in this ResultSet object. 
public  void moveToCurrentRow() throws SQLException{
	rsetLog("RMISC 38");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Moves the cursor to the remembered cursor position, usually the current row. 
public  void moveToInsertRow() throws SQLException{
	rsetLog("RMISC 39");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Moves the cursor to the insert row. 
 public int size(){
 	
 	if (rmo != null)
 		try{
 		if (range > 0 && range <= rmo.size())
 			return range;
 		return rmo.size();
 		}catch (SQLException e){
 			e.printStackTrace();
 		}
  		if (range > 0 && range <= results.size())
 			return range;

 	return results.size();
 }
public  boolean next() throws SQLException{
rsetLog("next");
if (results == null || indx >= size())return false;
indx = indx + 1;
return true;
}
 //            Moves the cursor froward one row from its current position. 
public  boolean previous() throws SQLException{
rsetLog("RESL 12");
	if (indx <= 1)return false;
	indx = indx - 1;
	return true;
}
 //            Moves the cursor to the previous row in this ResultSet object. 
public  void refreshRow() throws SQLException{
	rsetLog("RMISC 40");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Refreshes the current row with its most recent value in the database. 
public  boolean relative(int rows) throws SQLException{
rsetLog("RESL 13");
indx = indx + rows;
return true;
}
 //           Moves the cursor a relative number of rows, either positive or negative. 
public  boolean rowDeleted() throws SQLException{
	rsetLog("RMISC 41");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Retrieves whether a row has been deleted. 
public  boolean rowInserted() throws SQLException{
	rsetLog("RMISC 42");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Retrieves whether the current row has had an insertion. 
public  boolean rowUpdated() throws SQLException{
	rsetLog("RMISC 43");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Retrieves whether the current row has been updated. 
public  void setFetchDirection(int direction)  throws SQLException{
	rsetLog("RMISC 44");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Gives a hint as to the direction in which the rows in this ResultSet object will be processed. 
public  void setFetchSize(int rows) throws SQLException{
	rsetLog("RMISC 45");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Gives the JDBC driver a hint as to the number of rows that should be fetched from the database when more rows are needed for this ResultSet object. 
public  void updateArray(int columnIndex, Array x) throws SQLException{
	rsetLog("RMISC 46");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a java.sql.Array value. 
public  void updateArray(String columnLabel, Array x) throws SQLException{
	rsetLog("RMISC 47");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Updates the designated column with a java.sql.Array value. 
public  void updateAsciiStream(int columnIndex, InputStream x) throws SQLException{
	rsetLog("RMISC 48");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with an ascii stream value. 
public  void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException{
	rsetLog("RMISC 49");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Updates the designated column with an ascii stream value, which will have the specified number of bytes. 
public  void updateAsciiStream(int columnIndex, InputStream x, long length)  throws SQLException{
	rsetLog("RMISC 50");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Updates the designated column with an ascii stream value, which will have the specified number of bytes. 
public  void updateAsciiStream(String columnLabel, InputStream x) throws SQLException{
	rsetLog("RMISC 51");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Updates the designated column with an ascii stream value. 
public  void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException{
	rsetLog("RMISC 52");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with an ascii stream value, which will have the specified number of bytes. 
public  void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException{
	rsetLog("RMISC 53");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with an ascii stream value, which will have the specified number of bytes. 
public  void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException{
	rsetLog("RMISC 54");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Updates the designated column with a java.math.BigDecimal value. 
public  void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException{
	rsetLog("RMISC 55");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Updates the designated column with a java.sql.BigDecimal value. 
public  void updateBinaryStream(int columnIndex, InputStream x) throws SQLException{
	rsetLog("RMISC 56");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a binary stream value. 
public  void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException{
	rsetLog("RMISC 57");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a binary stream value, which will have the specified number of bytes. 
public  void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException{
	rsetLog("RMISC 58");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a binary stream value, which will have the specified number of bytes. 
public  void updateBinaryStream(String columnLabel, InputStream x) throws SQLException{
	rsetLog("RMISC 59");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Updates the designated column with a binary stream value. 
public  void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException{
	rsetLog("RMISC 60");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Updates the designated column with a binary stream value, which will have the specified number of bytes. 
public  void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException{
	rsetLog("RMISC 61");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a binary stream value, which will have the specified number of bytes. 
public  void updateBlob(int columnIndex, Blob x) throws SQLException{
	rsetLog("RMISC 62");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Updates the designated column with a java.sql.Blob value. 
public  void updateBlob(int columnIndex, InputStream inputStream) throws SQLException{
	rsetLog("RMISC 63");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Updates the designated column using the given input stream. 
public  void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException{
	rsetLog("RMISC 64");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column using the given input stream, which will have the specified number of bytes. 
public void updateBlob(String columnLabel, Blob x) throws SQLException{
	rsetLog("RMISC 65");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Updates the designated column with a java.sql.Blob value. 
public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException{
	rsetLog("RMISC 660");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Updates the designated column using the given input stream. 
public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException{
	rsetLog("RMISC 67");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column using the given input stream, which will have the specified number of bytes. 
public void updateBoolean(int columnIndex, boolean x) throws SQLException{
	rsetLog("RMISC 68");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a boolean value. 
public void updateBoolean(String columnLabel, boolean x) throws SQLException{
	rsetLog("RMISC 70");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Updates the designated column with a boolean value. 
public void updateByte(int columnIndex, byte x) throws SQLException{
	rsetLog("RMISC 71");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Updates the designated column with a byte value. 
public void updateByte(String columnLabel, byte x) throws SQLException{
	rsetLog("RMISC 72");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a byte value. 
public void updateBytes(int columnIndex, byte[] x) throws SQLException{
	rsetLog("RMISC 73");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a byte array value. 
public void updateBytes(String columnLabel, byte[] x) throws SQLException{
	rsetLog("RMISC 74");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a byte array value. 
public void updateCharacterStream(int columnIndex, Reader x) throws SQLException{
	rsetLog("RMISC 75");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a character stream value. 
public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException{
	rsetLog("RMISC 76");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a character stream value, which will have the specified number of bytes. 
public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException{
	rsetLog("RMISC 77");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a character stream value, which will have the specified number of bytes. 
public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException{
	rsetLog("RMISC 78");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a character stream value. 
public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException{
	rsetLog("RMISC 79");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a character stream value, which will have the specified number of bytes. 
public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException{
	rsetLog("RMISC 80");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a character stream value, which will have the specified number of bytes. 
public void updateClob(int columnIndex, Clob x) throws SQLException{
	rsetLog("RMISC 81");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a java.sql.Clob value. 
public void updateClob(int columnIndex, Reader reader) throws SQLException{
	rsetLog("RMISC 82");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //                  Updates the designated column using the given Reader object. 
public void updateClob(int columnIndex, Reader reader, long length) throws SQLException{
	rsetLog("RMISC 83");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column using the given Reader object, which is the given number of characters long. 
public void updateClob(String columnLabel, Clob x) throws SQLException{
	rsetLog("RMISC 84");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a java.sql.Clob value. 
public void updateClob(String columnLabel, Reader reader) throws SQLException{
	rsetLog("RMISC 85");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column using the given Reader object. 
public void updateClob(String columnLabel, Reader reader, long length) throws SQLException{
	rsetLog("RMISC 86");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column using the given Reader object, which is the given number of characters long. 
public void updateDate(int columnIndex, java.sql.Date x) throws SQLException{
	rsetLog("RMISC 87");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a java.sql.Date value. 
public void updateDate(String columnLabel, java.sql.Date x) throws SQLException{
	rsetLog("RMISC 88");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a java.sql.Date value. 
public void updateDouble(int columnIndex, double x) throws SQLException{
	rsetLog("RMISC 89");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Updates the designated column with a double value. 
public void updateDouble(String columnLabel, double x) throws SQLException{
	rsetLog("RMISC 90");

	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a double value. 
public void updateFloat(int columnIndex, float x) throws SQLException{
rsetLog("RMISC 91");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a float value. 
public void updateFloat(String columnLabel, float x) throws SQLException{
rsetLog("RMISC 92");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a float value. 
public void updateInt(int columnIndex, int x) throws SQLException{
rsetLog("RMISC 93");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with an int value. 
public void updateInt(String columnLabel, int x) throws SQLException{
rsetLog("RMISC 94");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with an int value. 
public void updateLong(int columnIndex, long x) throws SQLException{
rsetLog("RMISC 95");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a long value. 
public void updateLong(String columnLabel, long x) throws SQLException{
rsetLog("RMISC 96");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a long value. 
public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException{
rsetLog("RMISC 97");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a character stream value. 
public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException{
rsetLog("RMISC 98");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a character stream value, which will have the specified number of bytes. 
public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException{
rsetLog("RMISC 99");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a character stream value. 
public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException{
rsetLog("RMISC 100");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a character stream value, which will have the specified number of bytes. 
public void updateNClob(int columnIndex, NClob nClob) throws SQLException{
rsetLog("RMISC 101");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a java.sql.NClob value. 
public void updateNClob(int columnIndex, Reader reader) throws SQLException{
rsetLog("RMISC 102");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column using the given Reader The data will be read from the stream as needed until end-of-stream is reached. 
public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException{
rsetLog("RMISC 103");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column using the given Reader object, which is the given number of characters long. 
public void updateNClob(String columnLabel, NClob nClob) throws SQLException{
rsetLog("RMISC 104");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a java.sql.NClob value. 
public void updateNClob(String columnLabel, Reader reader) throws SQLException{
rsetLog("RMISC 105");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column using the given Reader object. 
public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException{
rsetLog("RMISC 106");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column using the given Reader object, which is the given number of characters long. 
public void updateNString(int columnIndex, String nString) throws SQLException{
rsetLog("RMISC 107");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a String value. 
public void updateNString(String columnLabel, String nString) throws SQLException{
rsetLog("RMISC 108");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a String value. 
public void updateNull(int columnIndex) throws SQLException{
rsetLog("RMISC 109");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a null value. 
public void updateNull(String columnLabel) throws SQLException{
rsetLog("RMISC 110");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a null value. 
public void updateObject(int columnIndex, Object x) throws SQLException{
rsetLog("RMISC 111");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with an Object value. 
public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException{
rsetLog("RMISC 112");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with an Object value. 
public void updateObject(String columnLabel, Object x) throws SQLException{
rsetLog("RMISC 113");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with an Object value. 
public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException{
rsetLog("RMISC 114");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with an Object value. 
public void updateRef(int columnIndex, Ref x) throws SQLException{
rsetLog("RMISC 115");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a java.sql.Ref value. 
public void updateRef(String columnLabel, Ref x) throws SQLException{
rsetLog("RMISC 116");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a java.sql.Ref value. 
public void updateRow() throws SQLException{
rsetLog("RMISC 117");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the underlying database with the new contents of the current row of this ResultSet object. 
public void updateRowId(int columnIndex, java.sql.RowId x) throws SQLException {
rsetLog("RMISC 118");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a RowId value. 
public void updateRowId(String columnLabel, java.sql.RowId x) throws SQLException,SQLFeatureNotSupportedException {
rsetLog("RMISC 119");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a RowId value. 
public void updateShort(int columnIndex, short x) throws SQLException{
rsetLog("RMISC 120");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a short value. 
public void updateShort(String columnLabel, short x) throws SQLException{
rsetLog("RMISC 121");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a short value. 
public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException{
rsetLog("RMISC 122");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a java.sql.SQLXML value. 
public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException{
rsetLog("RMISC 123");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a java.sql.SQLXML value. 
public void updateString(int columnIndex, String x) throws SQLException{
rsetLog("RMISC 124");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a String value. 
public void updateString(String columnLabel, String x) throws SQLException{
rsetLog("RMISC 125");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //           Updates the designated column with a String value. 
public void updateTime(int columnIndex, Time x) throws SQLException{
rsetLog("RMISC 126");
	throw JGException.get("not_supported","NOT SUPPORTED");

}
 //            Updates the designated column with a java.sql.Time value. 
public void updateTime(St
