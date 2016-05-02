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
import java.sql.*;
import java.util.Properties;
import java.util.Map;
import java.util.Calendar;
import javax.sql.DataSource;
import java.util.Vector;
import java.util.Hashtable;
import org.jiql.util.*;
import java.util.Enumeration;
import org.jiql.db.*;
import tools.util.EZArrayList;
import tools.util.NameValuePairs;
import tools.util.StringUtil;
import tools.util.StreamUtil;
import java.io.*;
import java.math.*;
import java.net.*;

public class jiqlPreparedStatement extends org.jiql.jdbc.Statement implements java.sql.PreparedStatement
{

	String ostatement = null;
	//String statement = null;
	NameValuePairs params = new NameValuePairs();
	public jiqlPreparedStatement(jiqlConnection c,String s){
	super(c);
	setStatement(s);
	}
	protected String processStatement(){
		String pstatement = ostatement;
		pstatement = StringUtil.replaceSubstring(pstatement,"'?'","rjiql_question");
		pstatement = StringUtil.replaceSubstring(pstatement,"?","rjiql_question");

		Object val = null;

		for (int ct = 1; ct <= params.size();ct++)
		{
			val = params.get(String.valueOf(ct));
			//pstatement = StringUtil.replaceFirstSubstring(pstatement,"rjiql_question",val.toString());
			if (pstatement.toLowerCase().startsWith("create "))
						pstatement = StringUtil.replaceFirstSubstring(pstatement,"rjiql_question",StringUtil.getTrimmedValue(val.toString()));
			
			else
						pstatement = StringUtil.replaceFirstSubstring(pstatement,"rjiql_question",val.toString());


		}
		return pstatement;
	}
	
	public void setStatement(String s)
	{
		ostatement = s;
	}

public void addBatch()throws SQLException{
	super.addBatch(processStatement());
	clearParameters();
} 
          //Adds a set of parameters to this PreparedStatement object's batch of commands. 
public void clearParameters()throws SQLException{
//statement = ostatement;
params.clear();
} 
          //Clears the current parameter values immediately. 
public boolean execute()throws SQLException{
return execute(processStatement());
} 
          //Executes the SQL statement in this PreparedStatement object, which may be any kind of SQL statement. 
public ResultSet executeQuery()throws SQLException{
	return executeQuery(processStatement());
} 
          //Executes the SQL query in this PreparedStatement object and returns the ResultSet object generated by the query. 
public int executeUpdate()throws SQLException{
	return executeUpdate(processStatement());

} 
          //Executes the SQL statement in this PreparedStatement object, which must be an SQL Data Manipulation Language (DML) statement, such as INSERT, UPDATE or DELETE; or an SQL statement that returns nothing, such as a DDL statement. 
public ResultSetMetaData getMetaData()throws SQLException{
        throw JGException.get("not_supported","Not Supported");

} 
          //Retrieves a ResultSetMetaData object that contains information about the columns of the ResultSet object that will be returned when this PreparedStatement object is executed. 
public ParameterMetaData getParameterMetaData()throws SQLException{
        throw JGException.get("not_supported","Not Supported");

} 
          //Retrieves the number, types and properties of this PreparedStatement object's parameters. 
public void setArray(int parameterIndex, Array x)throws SQLException{
params.put(parameterIndex,x);
} 
          //Sets the designated parameter to the given java.sql.Array object. 
public void setAsciiStream(int parameterIndex, InputStream x)throws SQLException{
params.put(parameterIndex,x);
} 
          //Sets the designated parameter to the given input stream. 
public void setAsciiStream(int parameterIndex, InputStream x, int length)throws SQLException{
params.put(parameterIndex,x);
} 
          //Sets the designated parameter to the given input stream, which will have the specified number of bytes. 
public void setAsciiStream(int parameterIndex, InputStream x, long length)throws SQLException{
params.put(parameterIndex,x);
} 
          //Sets the designated parameter to the given input stream, which will have the specified number of bytes. 
public void setBigDecimal(int parameterIndex, BigDecimal x)throws SQLException{
params.put(parameterIndex,x);
} 
          //Sets the designated parameter to the given java.math.BigDecimal value. 
public void setBinaryStream(int parameterIndex, InputStream x)throws SQLException{
params.put(parameterIndex,x);
} 
          //Sets the designated parameter to the given input stream. 
public void setBinaryStream(int parameterIndex, InputStream x, int length)throws SQLException{
params.put(parameterIndex,x);
} 
          //Sets the designated parameter to the given input stream, which will have the specified number of bytes. 
public void setBinaryStream(int parameterIndex, InputStream x, long length)throws SQLException{
params.put(parameterIndex,x);
} 
          //Sets the designated parameter to the given input stream, which will have the specified number of bytes. 
public void setBlob(int parameterIndex, Blob x)throws SQLException{
params.put(parameterIndex,"jiqlDirectValue_" + parameterIndex);
byte[] b = x.getBytes(0,new Long(x.length()).intValue());
com.google.appengine.api.datastore.Blob gb = new com.google.appengine.api.datastore.Blob(b);
setDirectValue("jiqlDirectValue_" + parameterIndex,gb);
} 
          //Sets the designated parameter to the given java.sql.Blob object. 
public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException{
params.put(parameterIndex,"jiqlDirectValue_" + parameterIndex);

byte[] b = null;
try{

b = StreamUtil.readStream(inputStream,inputStream.available());
}catch (IOException e){
	throw new SQLException(e.toString());
}
com.google.appengine.api.datastore.Blob gb = new com.google.appengine.api.datastore.Blob(b);
setDirectValue("jiqlDirectValue_" + parameterIndex,gb);


} 
          //Sets the designated parameter to a InputStream object. 
public void setBlob(int parameterIndex, InputStream inputStream, long length)throws SQLException{
params.put(parameterIndex,"jiqlDirectValue_" + parameterIndex);

byte[] b = null;
//try{

b = StreamUtil.readStream(inputStream,new Long(length).intValue());
//}catch (IOException e){
//	throw new SQLException(e.toString());
//}
com.google.appengine.api.datastore.Blob gb = new com.google.appengine.api.datastore.Blob(b);
setDirectValue("jiqlDirectValue_" + parameterIndex,gb);
} 
          // Sets the designated parameter to a InputStream object. 
public void setBoolean(int parameterIndex, boolean x) throws SQLException{
params.put(String.valueOf(parameterIndex),x);
} 
          // Sets the designated parameter to the given Java boolean value. 
public void setByte(int parameterIndex, byte x) throws SQLException{
params.put(parameterIndex,x);
} 
          // Sets the designated parameter to the given Java byte value. 
public void setBytes(int parameterIndex, byte[] x) throws SQLException{

params.put(parameterIndex,"jiqlDirectValue_" + parameterIndex);
com.google.appengine.api.datastore.Blob gb = new com.google.appengine.api.datastore.Blob(x);
setDirectValue("jiqlDirectValue_" + parameterIndex,gb);
//("jiqlDirectValue_" + parameterIndex + " SET 1 " + gb);

} 
          //Sets the designated parameter to the given Java array of bytes. 
public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException{
} 
          // Sets the designated parameter to the given Reader object. 
public void setCharacterStream(int parameterIndex, Reader reader, int length)throws SQLException{
} 
          //Sets the designated parameter to the given Reader object, which is the given number of characters long. 
public void setCharacterStream(int parameterIndex, Reader reader, long length)throws SQLException{
} 
          // Sets the designated parameter to the given Reader object, which is the given number of characters long. 
public void setClob(int parameterIndex, Clob x) throws SQLException{
} 
          // Sets the designated parameter to the given java.sql.Clob object. 
public void setClob(int parameterIndex, Reader reader) throws SQLException{
} 
          //  Sets the designated parameter to a Reader object. 
public void setClob(int parameterIndex, Reader reader, long length)throws SQLException{
} 
          // Sets the designated parameter to a Reader object. 
public void setDate(int parameterIndex, Date x)throws SQLException{
params.put(parameterIndex,x.getTime());
} 
          // Sets the designated parameter to the given java.sql.Date value using the default time zone of the virtual machine that is running the application. 
public void setDate(int parameterIndex, Date x, Calendar cal)throws SQLException{
params.put(parameterIndex,x.getTime());
} 
          //  Sets the designated parameter to the given java.sql.Date value, using the given Calendar object. 
public void setDouble(int parameterIndex, double x) throws SQLException{
params.put(parameterIndex,x);
} 
          // Sets the designated parameter to the given Java double value. 
public void setFloat(int parameterIndex, float x) throws SQLException{
params.put(parameterIndex,x);
} 
          // Sets the designated parameter to the given Java float value. 
public void setInt(int parameterIndex, int x) throws SQLException{
params.put(parameterIndex,x);
} 
          // Sets the designated parameter to the given Java int value. 
public void setLong(int parameterIndex, long x)throws SQLException{
params.put(parameterIndex,x);
} 
          // Sets the designated parameter to the given Java long value. 
public void setNCharacterStream(int parameterIndex, Reader value)throws SQLException{

} 
          //Sets the designated parameter to a Reader object. 
public void setNCharacterStream(int parameterIndex, Reader value, long length)throws SQLException{
} 
          //Sets the designated parameter to a Reader object. 
public void setNClob(int parameterIndex, NClob value)throws SQLException{
} 
          // Sets the designated parameter to a java.sql.NClob object. 
public void setNClob(int parameterIndex, Reader reader)throws SQLException{
} 
          // Sets the designated parameter to a Reader object. 
public void setNClob(int parameterIndex, Reader reader, long length)throws SQLException{
} 
          // Sets the designated parameter to a Reader object. 
public void setNString(int parameterIndex, String value)throws SQLException{
} 
          // Sets the designated paramter to the given String object. 
public void setNull(int parameterIndex, int sqlType)throws SQLException{

params.put(parameterIndex,"null");
} 
          //  Sets the designated parameter to SQL NULL. 
public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException{
params.put(parameterIndex,"null");
} 
          // Sets the designated parameter to SQL NULL. 
public void setObject(int parameterIndex, Object x)throws SQLException{
params.put(parameterIndex,x);
} 
          // Sets the value of the designated parameter using the given object. 
public void setObject(int parameterIndex, Object x, int targetSqlType)throws SQLException{
params.put(parameterIndex,x);
} 
          // Sets the value of the designated parameter with the given object. 
public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength)throws SQLException{
params.put(parameterIndex,x);
} 
          // Sets the value of the designated parameter with the given object. 
public void setRef(int parameterIndex, Ref x) throws SQLException{
} 
          //Sets the designated parameter to the given REF(<structured-type>) value. 
public void setRowId(int parameterIndex, java.sql.RowId x)throws SQLException{
} 
          // Sets the designated parameter to the given java.sql.RowId object. 
public void setShort(int parameterIndex, short x) throws SQLException{
params.put(parameterIndex,x);
} 
          //Sets the designated parameter to the given Java short value. 
public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException{
} 
          // Sets the designated parameter to the given java.sql.SQLXML object. 
public void setString(int parameterIndex, String x)throws SQLException{
//x = StringUtil.replaceSubstring(pstatement,"?",val.toString())
//if (sqp.getAction() == null || !"createUser".equals(sqp.getAction()))
params.put(parameterIndex,"'" + x.replaceAll("'","\\\\'") + "'");
//else
//params.put(parameterIndex, x);
} 
          // Sets the designated parameter to the given Java String value. 
public void setTime(int parameterIndex, Time x)throws SQLException{
params.put(parameterIndex,x);
} 
          // Sets the designated parameter to the given java.sql.Time value. 
public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException{
params.put(parameterIndex,x);
} 
          // Sets the designated parameter to the given java.sql.Time value, using the given Calendar object. 
public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException{
params.put(parameterIndex,x.getTime());
} 
          //Sets the designated parameter to the given java.sql.Timestamp value. 
public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)throws SQLException{
params.put(parameterIndex,x.getTime());
} 
          // Sets the designated parameter to the given java.sql.Timestamp value, using the given Calendar object. 
public void setUnicodeStream(int parameterIndex, InputStream x, int length)throws SQLException{
} 
          // Deprecated.   
public void setURL(int parameterIndex, URL x)throws SQLException{
params.put(parameterIndex,x);
} 
          // Sets the designated parameter to the given java.net.URL value. 











}



