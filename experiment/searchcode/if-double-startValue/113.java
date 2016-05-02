/* =================================================================
Copyright (C) 2009 ADV/web-engineering All rights reserved.

This file is part of Mozart.

Mozart is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Mozart is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

Mozart
http://www.mozartcms.ru
================================================================= */
// -*- java -*-
// $Id: DBAdapter.java 1293 2009-08-28 08:25:21Z vic $
// $Name:  $

package ru.adv.db.adapter;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import ru.adv.db.DBConnection;
import ru.adv.db.DBException;
import ru.adv.db.JdbcConnectionParameters;
import ru.adv.db.base.BooleanDBValue;
import ru.adv.db.base.DBValue;
import ru.adv.db.base.FileDBValue;
import ru.adv.db.base.FileValue;
import ru.adv.db.base.MObject;
import ru.adv.db.base.MValue;
import ru.adv.db.config.ConfigIndex;
import ru.adv.db.config.ConfigObject;
import ru.adv.db.config.DBConfigException;
import ru.adv.db.config.ObjectAttr;
import ru.adv.logger.TLogger;
import ru.adv.util.BadNumberException;
import ru.adv.util.ErrorCodeException;
import ru.adv.util.StringParser;
import ru.adv.util.Strings;
import ru.adv.util.UnreachableCodeReachedException;

/**
 * ?????????? ??? ??-????????? ???? ? ???? ?????
 * <p>???? ????? ?? <code>org.apache.turbine.util.db.adapter</code>
 * @version $Revision: 1.49 $
 */
public abstract class DBAdapter implements ru.adv.db.adapter.Types {

	private static final char ANSI_QUOTE_SIGN = '"';

	static final String SYS_TBL_PREFIX         = "mz";

	private TLogger logger = new TLogger(DBAdapter.class);
	
	public String getObjectTableName()       { return  SYS_TBL_PREFIX + "_object"; }
	public String getFuctionTableName()      { return  SYS_TBL_PREFIX + "_function"; }
	public String getIndexTableName()      { return  SYS_TBL_PREFIX + "_index"; }
	public String getTriggerTableName()      { return  SYS_TBL_PREFIX + "_trigger"; }
	public String getExtTreeTableName()   { return  SYS_TBL_PREFIX + "_extree"; }
	public String getCommonSequenceName()      { return  SYS_TBL_PREFIX + "_sequence"; }
	public String getLogSequenceName() { return "logsequence"; }
	public String getLogTableName() { return "log"; }
	public String getLogupdateTableName() { return "logupdate"; }

	private static final Set<String> SQL92_KEYWORDS = Collections.unmodifiableSet(new TreeSet<String>(Arrays.asList(new String[]{
		"ABSOLUTE", "CROSS", "GET", "NEXT", "SPACE", "ACTION", "CURRENT", "GLOBAL", "NO", "SQL", "ADD",
		"CURRENT_DATE", "GO", "NOT", "SQLCODE", "ALL", "CURRENT_TIME", "GOTO", "NULL", "SQLERROR", "ALLOCATE",
		"CURRENT_TIMESTAMP", "GRANT", "OCTET_LENGTH", "SQLSTATE", "ALTER", "CURRENT_USER", "GROUP", "OF",
		"SUBSTRING", "AND", "CURSOR", "HAVING", "ON", "SUM", "ANY", "DATE", "HOUR", "ONLY", "SYSTEM_USER", "ARE",
		"DAY", "IDENTITY", "OPEN", "TABLE", "AS", "DEALLOCATE", "IMMEDIATE", "OPTION", "TEMPORARY", "ASC", "DEC",
		"IN", "OR", "THEN", "ASSERTION", "DECIMAL", "INDICATOR", "ORDER", "TIME", "AT", "DECLARE", "INITIALLY",
		"OUTER", "TIMESTAMP", "AUTHORIZATION", "DEFAULT", "INNER", "OUTPUT", "TIMEZONE_HOUR", "AVG", "DEFERRABLE",
		"INPUT", "OVERLAPS", "TIMEZONE_MINUTE", "BEGIN", "DEFERRED", "INSENSITIVE", "PAD", "TO", "BETWEEN",
		"DELETE", "INSERT", "PARTIAL", "TRAILING", "BIT", "DESC", "INT", "POSITION", "TRANSACTION", "BIT_LENGTH",
		"DESCRIBE", "INTEGER", "PRECISION", "TRANSLATE", "BOTH", "DESCRIPTOR", "INTERSECT", "PREPARE",
		"TRANSLATION", "BY", "DIAGNOSTICS", "INTERVAL", "PRESERVE", "TRIM", "CASCADE", "DISCONNECT", "INTO",
		"PRIMARY", "TRUE", "CASCADED", "DISTINCT", "IS", "PRIOR", "UNION", "CASE", "DOMAIN", "ISOLATION",
		"PRIVILEGES", "UNIQUE", "CAST", "DOUBLE", "JOIN", "PROCEDURE", "UNKNOWN", "CATALOG", "DROP", "KEY",
		"PUBLIC", "UPDATE", "CHAR", "ELSE", "LANGUAGE", "READ", "UPPER", "CHARACTER", "END", "LAST", "REAL",
		"USAGE", "CHAR_LENGTH", "END-EXEC", "LEADING", "REFERENCES", "USER", "CHARACTER_LENGTH", "ESCAPE", "LEFT",
		"RELATIVE", "USING", "CHECK", "EXCEPT", "LEVEL", "RESTRICT", "VALUE", "CLOSE", "EXCEPTION", "LIKE",
		"REVOKE", "VALUES", "COALESCE", "EXEC", "LOCAL", "RIGHT", "VARCHAR", "COLLATE", "EXECUTE", "LOWER",
		"ROLLBACK", "VARYING", "COLLATION", "EXISTS", "MATCH", "ROWS", "VIEW", "COLUMN", "EXTERNAL", "MAX",
		"SCHEMA", "WHEN", "COMMIT", "EXTRACT", "MIN", "SCROLL", "WHENEVER", "CONNECT", "FALSE", "MINUTE", "SECOND",
		"WHERE", "CONNECTION", "FETCH", "MODULE", "SECTION", "WITH", "CONSTRAINT", "FIRST", "MONTH", "SELECT",
		"WORK", "CONSTRAINTS", "FLOAT", "NAMES", "SESSION", "WRITE", "CONTINUE", "FOR", "NATIONAL", "SESSION_USER",
		"YEAR", "CONVERT", "FOREIGN", "NATURAL", "SET", "ZONE", "CORRESPONDING", "FOUND", "NCHAR", "SIZE", "COUNT",
		"FROM", "NULLIF", "SMALLINT", "CREATE", "FULL", "NUMERIC", "SOME"})));

	public Set<String> getSystemTables() {
		return new TreeSet<String>(Arrays.asList(new String[]{
			getObjectTableName(),
			getFuctionTableName(),
			getIndexTableName(),
			getTriggerTableName(),
			getExtTreeTableName(),
			getLogTableName(),
			getLogupdateTableName()}));
	}
	
	/**
	 * Is database supports Postgres "DISTINCT ON" feature   
	 * @return
	 */
	abstract public boolean isSupportDistinctOn();
	
	public boolean isSupportSqlExceptInstruction() {
		return true;
	}

	public Set<String> getSystemSequences() {
		return new TreeSet<String>(Arrays.asList(new String[]{
			getCommonSequenceName(),
			getLogSequenceName()}));
	}

	public Set<String> getSystemObjects() {
		TreeSet<String> result = new TreeSet<String>();
		result.addAll(getSystemTables());
		result.addAll(getSystemSequences());
		return result;
	}

	protected static final int DEFAULT_STRING_LENGTH = 200;
	protected static final String DEFAULT_NUMERIC_PRECISION = "15";
	protected static final String DEFAULT_NUMERIC_DECIMAL_PLACES = "2";

	private static HashMap<String, Integer> typeToDBT;
	private static HashMap<Integer, String> dbt2Type;

	static {
		typeToDBT = new HashMap<String, Integer>();
		dbt2Type = new HashMap<Integer, String>();
		typeToDBT.put(DBT_STRING, new Integer(STRING));
		typeToDBT.put(DBT_TEXT, new Integer(TEXT));
		typeToDBT.put(DBT_SHORTINT, new Integer(SHORTINT));
		typeToDBT.put(DBT_INT, new Integer(INT));
		typeToDBT.put(DBT_LONG, new Integer(LONG));
		typeToDBT.put(DBT_DATE, new Integer(DATE));
		typeToDBT.put(DBT_TIMESTAMP, new Integer(TIMESTAMP));
		typeToDBT.put(DBT_BOOLEAN, new Integer(BOOLEAN));
		typeToDBT.put(DBT_FILE, new Integer(FILE));
		typeToDBT.put(DBT_FLOAT, new Integer(FLOAT));
		typeToDBT.put(DBT_DOUBLE, new Integer(DOUBLE));
		typeToDBT.put(DBT_NUMERIC, new Integer(NUMERIC));
		typeToDBT.put(DBT_CALCULATED, new Integer(CALCULATED));
		dbt2Type.put(new Integer(STRING), DBT_STRING);
		dbt2Type.put(new Integer(TEXT), DBT_TEXT);
		dbt2Type.put(new Integer(SHORTINT), DBT_SHORTINT);
		dbt2Type.put(new Integer(INT), DBT_INT);
		dbt2Type.put(new Integer(LONG), DBT_LONG);
		dbt2Type.put(new Integer(DATE), DBT_DATE);
		dbt2Type.put(new Integer(TIMESTAMP), DBT_TIMESTAMP);
		dbt2Type.put(new Integer(BOOLEAN), DBT_BOOLEAN);
		dbt2Type.put(new Integer(FILE), DBT_FILE);
		dbt2Type.put(new Integer(FLOAT), DBT_FLOAT);
		dbt2Type.put(new Integer(DOUBLE), DBT_DOUBLE);
		dbt2Type.put(new Integer(NUMERIC), DBT_NUMERIC);
		dbt2Type.put(new Integer(CALCULATED), DBT_CALCULATED);
	}

	public static DBAdapter create(String adapterName) throws DBAdapterException {
		if (adapterName.equals(POSTGRES)) {
			return new Postgres83();
		}
		if (adapterName.equals(POSTGRES80)) {
			return new Postgres80();
		}
        if (adapterName.equals(POSTGRES73)) {
            return new Postgres73();
        }
        if (adapterName.equals(POSTGRES72)) {
			return new Postgres();
		} 
        if (adapterName.equals(ORACLE)) {
			return new Oracle();
		}
		if (adapterName.equals(HSQLDB)) {
			return new Hsqldb();
		}
		if (adapterName.equals(MYSQL)) {
			return new Mysql();
		}
		throw new DBAdapterException("Invalid database adapter name: '" + adapterName + "'");
	}

	private static final String CREATE_OR_REPLACE = "create or replace";
	private static final String CREATE = "create";

	public static final String POSTGRES72 = "postgres72";
    public static final String POSTGRES73 = "postgres73";
	public static final String POSTGRES80 = "postgres80";
	public static final String POSTGRES = "postgres";
	public static final String ORACLE = "oracle";
	public static final String HSQLDB = "hsqldb";
	public static final String MYSQL = "mysql";
	

	protected DBAdapter() {
	}
	
	public abstract boolean isNullsLastOrder();
	
	public abstract String getDriverClassName();
	
	/**
	 * TODO FIXME
	 *  
	 * It's always false, because there is problem with replication/
	 * For more information see http://jira.adv.ru/browse/MOZART-135
	 * 
	 * @return
	 */
	public final boolean isSupportForeignConstrain() {
		return false;
	}
	
	public String getTreeTableName(String tableName) {
		return tableName+ConfigObject.TREE_EX_SUFFIX;
	}
	
	/**
	 * Add row limit instractions to SQL SELECT sentence 
	 * @param selectSentence
	 * @param offset skip rows to get the first one 
	 * @param limit max rows in response. 0 is mean no limits
	 * @return
	 */
	abstract public String addLimitInstructionsToSelect(String selectSentence, int offset, int limit);
	
	/**
	 * @return the supportNativeSystemTriggers
	 */
	public boolean isSupportNativeSystemTriggers() {
		return true;
	}
	
	/**
	 * SQL expression with additional properties for admin connection
	 * @return
	 */
	public String getAdminConnectionPropertiesSQL(){
		return "";
	}
	
	/**
	 * SQL ????????????? ??, ???????????? ??? ???????? ????????????? ??
	 * @param schemaName
	 * @param databaseName
	 * @return
	 * @see DBAdapter#getExistentDatabaseSql
	 */
	public abstract String getDatabaseSQLIdentifier(String schemaName, String databaseName);

	/**
	 * Makes URL for JDBC connection.
	 */
	public abstract String getURL(JdbcConnectionParameters parameters);

	/**
	 * The registered driver name.
	 *
	 * @return name.
	 */
	public abstract String getName();

	/**
	 * ?????????? ??? ???????, ??????? ??????? ????????? ?????
	 */
	public abstract String getRandomFunctionSql();


	/**
	 * ??????? TEMPORARY TABLE
	 * @param tableName ??? ???????
	 * @param attrsDefinition attrName -> attrDefinition
	 * @return
	 */
	public String getCreateTemporaryTableSql(String tableName, Map<String,String> attrsDefinition) {
	    StringBuffer stat = new StringBuffer();
	    stat.append("CREATE TEMPORARY TABLE ");
	    stat.append(getSQLIdentifier(tableName));
	    stat.append(" (");
	    for (Iterator<Entry<String,String>> i = attrsDefinition.entrySet().iterator(); i.hasNext(); ){
	        Entry<String, String> entry = i.next();
	        stat.append( getSQLIdentifier(entry.getKey()));
	        stat.append( " " );
	        stat.append( entry.getValue() );
	        if (i.hasNext()) {
	            stat.append(",");
	        }
	    }
	    stat.append(" )");
	    return stat.toString();
	}
	
	/**
	 * ????????? ?? ?????? ??? ?????????? ????? ??????
	 */
	public boolean isCreatePrimaryIndex() {
		return true;
	}


	/**
	 * get constant form {@link java.sql.Types} by mozart types {@link Types}
	 * @return
	 */
	protected final int getJavaSQLType(DBValue value) throws DBAdapterException {
		int type;
		switch (value.type()) {
			case STRING:
			case TEXT:
				type = java.sql.Types.VARCHAR;
				break;
			case SHORTINT:
			case INT:
			case LONG:
				type = java.sql.Types.NUMERIC;
				break;
			case DATE:
				//type = java.sql.Types.DATE;
				type = java.sql.Types.TIMESTAMP;
				break;
			case TIMESTAMP:
				type = java.sql.Types.TIMESTAMP;
				break;
			case BOOLEAN:
			case FILE:
				type = java.sql.Types.NUMERIC;
				break;
			case FLOAT:
				type = java.sql.Types.FLOAT;
				break;
			case DOUBLE:
				type = java.sql.Types.DOUBLE;
				break;
			case NUMERIC:
				type = java.sql.Types.NUMERIC;
				break;
			case CALCULATED:
				throw new DBAdapterException("CalculatedDBValue can't be stored");
			default:
				throw new DBAdapterException("Unknown DBValue type");
		}
		return type;
	}

	/**
	 * ???????? ???????? ? <code>PreparedStatement</code>.
	 * ??? ??????? ????????? ?????? <code>java.sql.PreparedStatement</code>
	 * @param dbValue
	 * @param ps
	 * @param position ? ??? ??????? ????? ??????????? ????????
	 */
	public final void setTo(DBValue dbValue, PreparedStatement ps, int position) throws SQLException, DBAdapterException {
		if (dbValue == null || dbValue.isNull() || dbValue.get()==null) {
			ps.setNull(position, getJavaSQLType(dbValue));
		} else {
			setNonNullToStatement(dbValue, ps, position);
		}
	}

	/**
	 * ?????????? ???????? {@link DBValue} ? PreparedStatement
	 * @param value
	 * @param ps
	 * @param position
	 */
	protected void setNonNullToStatement(DBValue value, PreparedStatement ps, int position) throws DBAdapterException, SQLException {
		
		switch (value.type()) {
			case STRING:
			case TEXT:
				ps.setString(position, (String)custToJdbcValue(value));
				break;
			case SHORTINT:
				ps.setShort(position, (Short)custToJdbcValue(value));
				break;
			case INT:
				ps.setInt(position, (Integer)custToJdbcValue(value));
				break;
			case LONG:
				ps.setLong(position, (Long)custToJdbcValue(value));
				break;
			case DATE:
				ps.setDate(position, (java.sql.Date)custToJdbcValue(value));
				break;
			case TIMESTAMP:
				ps.setTimestamp(position, (java.sql.Timestamp)custToJdbcValue(value));
				break;
			case BOOLEAN:
			case FILE:
				ps.setInt(position, (Integer)custToJdbcValue(value));
				break;
			case FLOAT:
				ps.setFloat(position, (Float)custToJdbcValue(value));
				break;
			case DOUBLE:
				ps.setDouble(position, (Double)custToJdbcValue(value));
				break;
			case NUMERIC:
				ps.setObject(position, custToJdbcValue(value), getJavaSQLType(value));
				break;
			case CALCULATED:
				throw new DBAdapterException("CalculatedDBValue can't be stored");
			default:
				throw new DBAdapterException("Unknown DBValue type");
		}
	}
	
	/**
	 * Customize {@link DBValue} to SQL value in string presentation  
	 * @param value
	 * @return
	 * @throws DBAdapterException
	 */
	public String custToSqlValue(DBValue value) throws DBAdapterException {
		Object v = custToJdbcValue(value);
		switch (value.type()) {
		case STRING:
		case TEXT:
			return v!=null ? v.toString().replaceAll("'", "''") : null;
		}
		return v.toString();
	}
	
	/**
	 * Customize {@link DBValue} to JDBC value for prepared statement 
	 * @param value
	 * @return
	 * @throws DBAdapterException
	 */
	public Object custToJdbcValue(DBValue value) throws DBAdapterException {
		switch (value.type()) {
		case STRING:
		case TEXT:
			return value.get().toString();
		case SHORTINT:
			return ((Short)value.get()).shortValue();
		case INT:
			return ((Integer) value.get()).intValue();
		case LONG:
			return ((Long) value.get()).longValue();
		case DATE:
			return (java.sql.Date) value.get();
		case TIMESTAMP:
			return (java.sql.Timestamp) value.get();
		case BOOLEAN:
			return ((Boolean) value.get()).booleanValue() ? 1 : 0;
		case FILE:
			int i;
			if (value.get() instanceof FileValue) {
				i = ((FileValue) value.get()).canRead() ? 1 : 0;
			}
			else if (value.get() instanceof Boolean) {
				i = ((Boolean) value.get()).booleanValue() ? 1 : 0;
			}
			else {
				throw new UnreachableCodeReachedException("Invalid file value: " + value);
			}
			return i;
		case FLOAT:
			return ((Float) value.get()).floatValue();
		case DOUBLE:
		case NUMERIC:
			return ((Double) value.get()).doubleValue();
		case CALCULATED:
			throw new DBAdapterException("CalculatedDBValue can't be stored");
		default:
			throw new DBAdapterException("Unknown DBValue type");
		}
	}
	

	/**
	 * ??????? ????????? SQL ??? attrSQLIdent, ?????????????? ???????? ???? DATE
	 * ??? TIMESTAMP ? ??????
	 * ?? ???????? ?????? ?????????? ??? attrSQLIdent
	 * @param attrSQLIdent ????????? ?? ????????? SELECT
	 * @param simpleDateFormat ?????? ?? java.text.SimpleDateFormat
	 */
	public String subSqlFormatDate(String attrSQLIdent, String simpleDateFormat) throws ErrorCodeException {
		return attrSQLIdent;
	}

	/**
	 * ???????? ????????? SQL ? ???????????? ???? SQL,
	 * ???????????? ? Postgres ??? ??????????? ????????? ????? float 1.3::float4 <> 1.3::float8
	 * @param type ???????? {@link Types}
	 * @param sqlValue SQL ????????
	 * @return
	 */
	public String sqlCustomize(int type, String sqlValue) {
		return sqlValue;
	}

	/**
	 * Create quoted identifier. It is formed by enclosing an
	 *  arbitrary sequence of characters in double-quotes (")
	 */
	public String quotedIdentifier(String identifier) {
		if (!needQuoting(identifier)) {
			return identifier;
		}
		StringBuffer result = new StringBuffer(identifier.length() + 2);
		result.append(getQuoteSign());
		result.append(identifier);
		result.append(getQuoteSign());
		return result.toString();
	}
	
	public char getQuoteSign() {
		return ANSI_QUOTE_SIGN;
	}

	/**
	 * Extracts name of database from database config identificator.
	 */
	public abstract String extractDBName(String configName);

	/**
	 * Extracts name of schema from database config identificator.
	 */
	public abstract String extractSchemaName(String configName);

	/**
	 * This method is used to ignore case.
	 *
	 * @param in The string to transform to upper case.
	 * @return The upper case SQL string.
	 */
	public abstract String toUpperCase(String in);

	/**
	 * Generate sub SQL string with customize to SQL text,vchar
	 * @param attrSQLIdent
	 * @return  sub SQL string with customize to STRING or text
	 */
	public abstract String getCustToStringSql(String attrSQLIdent);

	/**
	 * Returns the last auto-incremented key.
	 * ?????? ???????? ?? ?????? getSequenceNextSql
	 * <BR/>The number returned by the last call to nextval('seqname')
	 * for the specified sequence in the current session.
	 * @return SQL statment for most recently inserted database key.
	 */
	public abstract String getSequenceCurrentSql(String schemaName, String name);

	/**
	 * ??? sequence
	 * @param schemaName
	 * @param name
	 * @return
	 */
	public abstract String getSequenceIncrementSql(String schemaName, String name);

	/**
	 * Returns the next auto-incremented key.
	 * <BR/>The number returned by the call nextval('seqname')
	 * for the specified sequence in the current session.
	 * @return SQL statment for most recently inserted database key.
	 */
	public abstract Long getSequenceNextValue( DBConnection connection,  String schemaName, String name);

	/**
	 * Returns the SQL statment for create sequence.
	 * @param startValue
	 * @param increment
	 * @return SQL statment
	 */
	public abstract List<String> getCreateSequenceSql(String name, long startValue, int increment);

	/**
	 * ?????????? SQL ??????? ????????????? ??????? ???????? sequence ? ???????? ????????
	 * @param name ??? sequence
	 * @param value ????? ???????? sequence
	 * @param increment
	 * @return
	 */
	public abstract List<String> getSetValueSequenceSql(String name, long value, int increment);

	/**
	 * Returns the SQL statment for drop sequence.
	 * @return SQL statment
	 */
	public abstract List<String> getDropTableSql(String name) throws SQLException;

	/**
	 * Returns the SQL statment for drop sequence.
	 * @param isCascade use CASCADE
	 * @return SQL statment
	 */
	public abstract List<String> getDropTableSql(String name, boolean isCascade);

	/**
	 * Returns the SQL statment for drop sequence.
	 * @return SQL statment
	 */
	public abstract List<String> getDropSequenceSql(String name);

	/**
	 * Returns the SQL statment for drop view.
	 * @return SQL statment
	 */
	public abstract String getDropViewSql(String name);

	/**
	 * Returns the SQL statment for exists databases.
	 * First column is name of database.
	 * @return SQL statment.
	 */
	public abstract String getExistentDatabaseSql();

	public boolean doesDatabaseExist(Connection conn, final String dbName) throws SQLException {
		Statement statement = conn.createStatement();
		try {
			ResultSet rs = statement.executeQuery(getExistentDatabaseSql());
			while (rs.next()) {
				if (dbName.equals( rs.getString(1) )) {
					return true;
				}
			}
		} finally {
			statement.close();
		}
		return false;
	}

	/**
	 * Returns the SQL statment for create database.
	 * @param name database name
	 * @return SQL statment.
	 */
	public abstract String getCreateDatabaseSql(String name);

    /**
     * Returns the SQL statment for create database.
     * @param name database name
     * @return SQL statment.
     */
    public abstract String getDropDatabaseSql(String name);

	/**
	 * Returns the List SQL statments, that must once execute after creating database.
	 * @return List with SQL statment.
	 */
	public abstract List<String> getOnCreateDatabaseSql(String dbname);

	/**
	 * Returns the SQL create statment for system stored procedures.
	 * @return List of SQL statment.
	 */
	public abstract List<String> getSystemStoredProcedureSql(String schema);

	/**
	 * Returns the SQL statment for drop stored procedure (function).
	 * @param name function name
	 * @return SQL statment.
	 */
	public abstract String getDropStoredProcedureSql(String name);

	public String getStoredProcedureName(String procedureStatment) {
		String tmp = procedureStatment.trim().toLowerCase();
		if (tmp.startsWith(DBAdapter.CREATE_OR_REPLACE)) {
			tmp = tmp.substring(DBAdapter.CREATE_OR_REPLACE.length()).trim();
		} else if (tmp.startsWith(DBAdapter.CREATE)) {
			tmp = tmp.substring(DBAdapter.CREATE.length()).trim();
		} else {
			throw new IllegalArgumentException("Invalid SQL stored procedure statement: " + procedureStatment);
		}
		StringTokenizer tokenizer = new StringTokenizer(tmp, " \t\n\r");
		try {
			tokenizer.nextToken();
			String name = tokenizer.nextToken();
			if (name.length() > 2 && name.startsWith("\"") && name.endsWith("\"")) {
				name = name.substring(1, name.length() - 1);
			}
			return name;
		}
		catch (NoSuchElementException e) {
			throw new IllegalArgumentException("Invalid SQL stored procedure statement: " + procedureStatment);
		}
	}

	/**
	 * Returns the SQL statment for drop column in table.
	 * @param tableName
	 * @param columnNames exists columns in table
	 * @param columnNamesToDrop columns in table to drop
	 * @return SQL statment.
	 */
	public abstract String getTableDropColumnsSql(String tableName, Collection<String> columnNames, Collection<String> columnNamesToDrop);

	/**
	 * Returns the SQL statment for drop index for table.
	 * @param tableName
	 * @param indexName
	 * @return SQL statment.
	 */
	public abstract String getDropIndexSql(String tableName, String indexName);

	/**
	 * Returns the SQL statment for create index for table.
	 * @param tableName
	 * @param indexName
	 * @param columnNames
	 * @param unique
	 * @return SQL statment.
	 */
	public abstract String getCreateIndexSql(String tableName, String indexName, List<String> columnNames, boolean unique);

	/**
	 * Returns the SQL statment for drop column in table.
	 * @param tableName
	 * @param columnName
	 * @param columnDefinition
	 * @return SQL statment.
	 */
	public abstract String getTableAddColumnsSql(String tableName, String columnName, String columnDefinition);

	/**
	 * Returns the SQL statment for synchronize types of columns in table.
	 * @param conn
	 * @param schema
	 * @param object
	 * @return List of SQL statment to execute or emty List
	 */
	public abstract SqlActionList getTableSyncTypeColumnsActions(DBConnection conn, String schema, ConfigObject object) throws SQLException, DBConfigException, DBAdapterException;

	/**
	 * This method is used to ignore case.
	 *
	 * @param in The string whose case to ignore.
	 * @return The string in a case that can be ignored.
	 */
	public abstract String ignoreCase(String in);

	/**
	 * ?????????? ???????? JDBC ???????????? ????? toString() ??? PreparedStatment
	 * ??????
	 */
	public boolean isPStatmentToString() {
		return false;
	}

	/**
	 * This mehod returns collection of names of all table in database.
	 */
	public Collection<String> getTables(DBConnection conn, String schema) throws SQLException {
		return getObjects(conn, schema, "TABLE");
	}

	/**
	 * This mehod returns collection of names of all views in database.
	 */
	public Collection<String> getViews(DBConnection conn, String schema) throws SQLException {
		return getObjects(conn, schema, "VIEW");
	}

	/**
	 * This mehod returns collection of names of all sequences in database.
	 */
	public Collection<String> getSequences(DBConnection conn, String schema) throws SQLException {
		return getObjects(conn, schema, "SEQUENCE");
	}
	

	public abstract Collection<String> getProcedures(DBConnection conn, String schema) throws SQLException;

	public static final int COL_NULLABLE = 0;
	public static final int COL_NO_NULLS = 1;
	public static final int COL_NULLABLE_UNKNOWN = 2;
	public static final String FK_PREFIX = "mz_cfk_";
	public static final String CHECK_PREFIX = "mz_cck_";
	public static final String TREE_FK_PREFIX = "mz_ctr_";

	/**
	 *
	 */
	private int nullable(int nullable) {
		if (nullable == DatabaseMetaData.columnNullable)
			return COL_NULLABLE;
		if (nullable == DatabaseMetaData.columnNoNulls)
			return COL_NO_NULLS;
		return COL_NULLABLE_UNKNOWN;
	}
	
	
	@SuppressWarnings("unchecked")
	public Map<String, ColumnInfo> getTableColumns(DBConnection conn, final String schema, final String tableName) throws SQLException {
		return (Map<String, ColumnInfo>)conn.execute(new ConnectionCallback(){

			@Override
			public Map<String, ColumnInfo> doInConnection(Connection con) throws SQLException, DataAccessException {
				return _getTableColumns(con.getMetaData(),schema, registeredName(tableName));
			}
			
		});
		
	}
	

	/**
	 * Returns map of [table name => related {@link ColumnInfo} object] pairs.
	 * All keys in lowcase.
	 */
	private Map<String, ColumnInfo> _getTableColumns(DatabaseMetaData md, String schema, String tableName) throws SQLException {
		Map<String, ColumnInfo> attrTypesInTable = new HashMap<String, ColumnInfo>();
		ResultSet rs = md.getColumns(null, registeredName(schema), registeredName(tableName), "%");
		if (rs != null) {
			try {
				while (rs.next()) {
					ColumnInfo ci = new ColumnInfo(rs.getString("TABLE_CAT"),
												   rs.getString("TABLE_SCHEM"),
												   rs.getString("TABLE_NAME"),
												   rs.getString("COLUMN_NAME"),
												   rs.getString("DATA_TYPE"),
												   rs.getString("TYPE_NAME"),
												   rs.getString("COLUMN_SIZE"),
												   rs.getString("DECIMAL_DIGITS"),
												   rs.getString("NUM_PREC_RADIX"),
												   nullable(rs.getInt("NULLABLE")),
												   rs.getString("COLUMN_DEF"),
												   rs.getString("CHAR_OCTET_LENGTH"),
												   rs.getString("ORDINAL_POSITION"));
					attrTypesInTable.put(ci.getName().toLowerCase(), ci);
				}
			}
			finally {
				if (rs.getStatement() != null)
					rs.getStatement().close();
				rs.close();
			}
		}
		return attrTypesInTable;
	}
	
	
	public abstract boolean isSupportConstraints();

	/**
	 * This method drops all relevant constraints
	 */
	public abstract void dropConstraints(DBConnection conn, String schema, String tableName) throws SQLException, DBConfigException;

	
	/**
	 * This method creates all relevant constraints
	 */
	final public void createConstraints(final DBConnection conn, final ConfigObject co) throws SQLException, DBConfigException {
		int fk = 1;
		int chk = 1;
		if (!doesTableHavePrimaryKey(conn, registeredName(co.getName()))) {
			conn.execute("ALTER TABLE " + qi(co.getName()) + " ADD CONSTRAINT "+qi("pk_"+co.getName())+" PRIMARY KEY (" + qi("id") + ")");
		}
		for (final ObjectAttr a : co.getAttributes()) {
			if ( isSupportForeignConstrain() && a.isForeign()) {
				ConfigObject fo = a.getForeignObject();
				if (fo.isTable()) {
					// ????????? ??????????? ??? foreign'??
					String cname = qi(FK_PREFIX + co.getName() + "_" + fk);
					conn.execute("ALTER TABLE " + qi(co.getName()) + " ADD CONSTRAINT " + cname + " FOREIGN KEY (" + a.getSQLName() + ") REFERENCES " + fo.getSQLName() + " (" + qi("id") + ")");
					fk++;
				}
			}
			// set column nullability
			ceateNullabilityConstraint(conn, a);
			if (a.getType() == Types.BOOLEAN && a.isRequired()) {
				// ?????? ???????? ?? ????? ????????? false
				// ???? ??? required
				String cname = qi(CHECK_PREFIX + co.getName() + "_" + chk);
				conn.execute("ALTER TABLE " + qi(co.getName()) + " ADD CONSTRAINT " + cname + " CHECK (" + a.getSQLName() + "=1)");
				chk++;
			}
			// set default value
			if (a.getDefaultJdbcValue()!=null) {
				createDefaultValueConstraint(conn,  a);
			}
		}
		// ??? ???????? ???????? ??????? ???? ?? ?????? ????
		createTreeConstraints(conn, co);		

	}


	/**
	 * This method returns collection of names of objects specified by type argument.
	 */
	@SuppressWarnings("unchecked")
	protected Collection<String> getObjects(DBConnection dbConection, final String schema, final String type) throws SQLException {
		
		return (Collection<String>)dbConection.execute(new ConnectionCallback(){

			@Override
			public Object doInConnection(Connection con) throws SQLException, DataAccessException {
				TreeSet<String> out = new TreeSet<String>();
				String options[] = {type};
				ResultSet rs = con.getMetaData().getTables(null, schema, null, options);
				if (rs != null) {
					try {
						while (rs.next()) {
							String name = rs.getString("TABLE_NAME");
							out.add(name.toLowerCase()); // ????? ???????? ???????????? ?????? ? low-case
						}
					} finally {
						if (rs.getStatement() != null)
							rs.getStatement().close();
						rs.close();
					}
				}
				return out;
			}
			
		});
	}


	/**
	 * This method converts DBT type name to SQL type name.
	 */
	public abstract String dbtToSQLType(String dbt) throws DBAdapterException;

	public String getSQLType(ObjectAttr attr) throws DBAdapterException {
		String result;
		String dbt = attr.getDBT();
		result = dbtToSQLType(dbt);
		if (dbt.equals(DBT_STRING)) {
			int length = attr.getLength();
			length = length == 0 ? DEFAULT_STRING_LENGTH : length;
			result += "(" + length + ")";
		} else if (dbt.equals(DBT_NUMERIC)) {
			String precision = attr.getPrecision();
			String decimalPlaces = attr.getDecimalPlaces();
			precision = precision == null ? DEFAULT_NUMERIC_PRECISION : precision;
			decimalPlaces = decimalPlaces == null ? DEFAULT_NUMERIC_DECIMAL_PLACES : decimalPlaces;
			try {
				result += "(" + StringParser.toInt(precision) + "," + StringParser.toInt(decimalPlaces) + ")";
			} catch (BadNumberException e) {
				throw new DBAdapterException("Ivalid DBT numeric precision=(" + precision + "," + decimalPlaces + ")");
			}
		}
		return result;
	}

	public String getSQLDefinition(ObjectAttr attr) throws DBAdapterException {
		return getSQLType(attr);
	}

	/**
	 * Create SQL type by result of java.sql.DatabaseMetaData.getColumns().
	 * @return SQL type.
	 */
	public abstract String getSQLType(ColumnInfo info) throws DBAdapterException;

	/**
	 * check for string type.
	 * @return true if dbt=DB.DBT_STRING or DB.DBT_TEXT
	 */
	public static boolean isStringType(String dbt) {
		return dbt.equals(DBAdapter.DBT_STRING) || dbt.equals(DBAdapter.DBT_TEXT);
	}


	/**
	 *	Get name of view from create view SQL statment.
	 * This method get third word from <code>createViewSql</code> and
	 * strip resalt from &quote;
	 * @param createViewSql statment for create <code>VIEW</code>
	 * @return name of <code>VIEW</code>, or emty string if third not present.
	 */
	public String getViewNameFromCreateSql(String createViewSql) {
		String result;
		StringTokenizer st = new StringTokenizer(createViewSql, " \t\n\r", false);
		try {
			st.nextToken(); // first word
			st.nextToken(); // second word
			result = st.nextToken(); // name, third word
		}
		catch (NoSuchElementException e) {
            result = "";
		}
		// strip quotes
		if (result.length() > 2 && result.startsWith("\"") && result.endsWith("\"")) {
			result = result.substring(1, result.length() - 1);
		}
		return result;
	}

	/**
	 * Return Types int constatnt by DBT string
	 */
	public static int getTypeByDBT(String dbt) throws DBAdapterException {
		if (!typeToDBT.containsKey(dbt)) {
			throw new DBAdapterException("Invalid DBT constant: " + dbt);
		}
		return typeToDBT.get(dbt).intValue();
	}

	public static String getDBTByType(int type) throws DBAdapterException {
		Integer tmp = new Integer(type);
		if (!dbt2Type.containsKey(tmp)) {
			throw new DBAdapterException("Invalid type constant: " + type);
		}
		return dbt2Type.get(tmp);
	}

	/**
	 * This method returns &apos;CREATE TABLE ...&apos; statement.
	 */
	public abstract List<String> getCreateObjectSQL(ConfigObject o) throws DBAdapterException;

	/**
	 * This method return SQL specific object name.
	 */
	public abstract String getSQLIdentifier(ConfigObject obj);

	/**
	 * This method return SQL specific attribute name.
	 */
	public abstract String getSQLIdentifier(ObjectAttr attr);

	/**
	 * This method return SQL specific object name.
	 */
	public abstract String getSQLIdentifier(String schemaName, String tableName);

	/**
	 * This method return SQL specific attribute name.
	 */
	public abstract String getSQLIdentifier(String attrName);


	public abstract List<String> getSystemObjectCreateSql(String objectName);

	public List<String> getSequenceZeroingSql(String name) {
		ArrayList<String> l = new ArrayList<String>();
		l.addAll(getDropSequenceSql(name));
		l.addAll(getCreateSequenceSql(name, 1, 1));
		return l;
	}

	public abstract int getMaxObjectNameLength();

	public abstract int getMaxAttributeNameLength();

	public abstract boolean storedProcedureExists(DBConnection connect, String functionName) throws SQLException;

	public abstract boolean indexExists(DBConnection connect, String tableName, String indexName) throws SQLException;

	public abstract boolean tableExists(DBConnection connect, String name) throws SQLException;

	public abstract boolean viewExists(DBConnection connect, String name) throws SQLException;

	public abstract boolean sequenceExists(DBConnection connect, String name) throws SQLException;

	public abstract boolean enablesCreatingDatabases();

	public abstract boolean useSelectedAttributesAliasing();

	public String fillPlaceholers(String sql, List<?> values) {
		StringBuffer result = new StringBuffer();
		List<String> tokens = Strings.split(sql, "?", true);
		int n = 0;
		for (String token : tokens) {
			if ("?".equals(token)) {
				Object value = values.get(n);
				if (value instanceof DBValue) {
					DBValue dbValue = ((DBValue) value);
					if (dbValue.isNull()) {
						token = "null";
					}
					else if (dbValue instanceof BooleanDBValue || dbValue instanceof FileDBValue) {
						if (getBooleanValue(dbValue)) {
							token = "1";
						}
						else {
							token = "0";
						}
					}
					else {
						value = dbValue.get();
						token = escapeValue(value);
					}
				}
				else {
					token = escapeValue(value);
				}
				n++;
			}
			result.append(token);
		}
		return result.toString();
	}

	private boolean getBooleanValue(DBValue dbValue) {
		boolean result = false;
		Object value = dbValue.get();
		if (value instanceof Boolean && ((Boolean)value).booleanValue()) {
			result = true;
		}
		return result;
	}

	private String escapeValue(Object value) {
		String result = value.toString();
		try {
			StringParser.toDouble(result);
		}
		catch (BadNumberException e) {
			result = "'" + Strings.replace(result, "'", "''") + "'";
		}
		return result;
	}

	public boolean needQuoting(String identificator) {
		String tmp = identificator.toUpperCase();
		if (SQL92_KEYWORDS.contains(tmp)) {
			return true;
		}
		if (isSQLKeyword(tmp)) {
			return true;
		}
		if (containsExtraCharacters(identificator)) {
			return true;
		}
		return false;
	}

	public abstract boolean isSQLKeyword(String tmp);

	public boolean containsExtraCharacters(String identificator) {
		for (int i = 0; i < identificator.length(); ++i) {
			char ch = identificator.charAt(i);
			if (i == 0) {
				if (!(isAlpha(ch) || ch == '$')) {
					return true;
				}
			}
			if (!isSQLChar(ch)) {
				return true;
			}
		}
		return false;
	}

	protected boolean isSQLChar(char ch) {
		return isAlpha(ch) || isDigit(ch) || ch == '_';
	}

	private boolean isDigit(char ch) {
		return (ch >= '0' && ch <= '9');
	}

	private boolean isAlpha(char ch) {
		return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
	}

	public abstract Set<String> getSQLKeywords();

	/**
	 * ???????????? ????????? ??? ?????? ???????? ? ????????? ???????? ??
	 * @param name
	 * @return
	 */
	protected abstract String registeredName(String name);

	/**
	 * ?????????? ?????? ???????? {@link ru.adv.db.config.ConfigIndex} ???????????? ? ??
	 * @param connection
	 * @param table
	 * @return
	 * @throws SQLException
	 * @throws DBConfigException
	 */
	public abstract List<ConfigIndex> getIndexes(DBConnection connection, String table) throws SQLException, DBConfigException;


	public abstract boolean doesTableHavePrimaryKey(DBConnection connection, String registeredTableName) throws SQLException;

	/**
     * ??????????? ??? ????????? ??????? ??? ????????????
     * @param tableName
     * @return
     */
    public String getTemporaryName(String tableName) {
		return tableName;
	}

	public abstract List<String> getSystemAttributes();

	public abstract boolean allowsNullsInUniqueIndex();

//	public boolean canDropSystemIndexes() {
//		return true;
//	}

//	public abstract String getSequencePhysicalName(String name);

	public abstract Map<String,String> getConnectionParameters();

    public abstract Map<String,Object> extractAdditionalInfo(Throwable e, MObject object);

    public abstract boolean supportsAdditionalInfoExtraction();

    protected void setUniqueValues(List<String> uniqueAttrs, MObject object, Map<String,Object> result) {
        if (uniqueAttrs != null) {
            TreeMap<String, Object> values = new TreeMap<String, Object>();
            for (String attrName : uniqueAttrs) {
                try {
                    MValue val = object.getAttribute(attrName).getDBValue();
                    if (val != null) {
                        values.put(attrName, val.get());
                    }
                } catch (DBException e) {
                    throw new UnreachableCodeReachedException(e);
                }
            }
            result.put("values", values);
        }
    }
	/**
	 * Short name of quotedIdentifier
	 */
	protected String qi(String identifier) {
		return quotedIdentifier(identifier);
	}
	/**
	 * Creates constarints for tree objects
	 * @param conn
	 * @param co
	 */
	protected void createTreeConstraints(DBConnection conn, ConfigObject co) {
		
		if (!co.isTree()) {
			return;
		}
		
		conn.execute(
				getNullabilityConstraintSql(co.getExtendedTreeTableSQLIdentifier(), "id" , co.getIdSQLType(), false )		
		);

		conn.execute(
				getNullabilityConstraintSql(co.getExtendedTreeTableSQLIdentifier(), "ancestor", co.getIdSQLType(), false )		
		);

		if (isSupportForeignConstrain()) {
			conn.execute(
					String.format(
							"ALTER TABLE %1$s ADD CONSTRAINT %2$s FOREIGN KEY (id) REFERENCES %1$s (id)", 
							qi(co.getName()),
							qi(qi(TREE_FK_PREFIX + co.getName()))
					)
			);
			conn.execute(
					String.format(
							"ALTER TABLE %1$s ADD CONSTRAINT %2$s FOREIGN KEY (id) REFERENCES %3$s (id)", 
							co.getExtendedTreeTableSQLIdentifier(), 
							qi(TREE_FK_PREFIX + co.getExtendedTreeTableName()+"_id"),
							qi(co.getName())
					)
			);
			conn.execute(
					String.format(
							"ALTER TABLE %1$s ADD CONSTRAINT %2$s FOREIGN KEY (ancestor) REFERENCES %3$s (id)", 
							co.getExtendedTreeTableSQLIdentifier(), 
							qi(TREE_FK_PREFIX + co.getExtendedTreeTableName()+"_anc"),
							qi(co.getName())
					)
			);

		}
	}
	
	public String getCasewhenFunctionName() {
		return "CASEWHEN";
	}
	
	/**
	 * set column nullability
	 * @param conn
	 * @param a
	 */
	protected void ceateNullabilityConstraint(final DBConnection conn, final ObjectAttr a) {
		conn.execute(getNullabilityConstraintSql(
				a.getConfigObject().getRealName(),a.getSQLName(), getSQLType(a), a.isNullable() 
		));
	}

	protected void __ceateNullabilityConstraint(final DBConnection conn, final ObjectAttr a) {
		conn.execute(String.format(
				"", 
				qi(a.getConfigObject().getRealName()), a.getSQLName(), ( a.isNullable() ? "" : "NOT" )  
		));
	}
	
	protected String getNullabilityConstraintSql(String tableName, String attrName, String attrSqlDefinition, boolean isNullable) {
		return String.format(
				"ALTER TABLE %1$s ALTER COLUMN %2$s SET %3$s NULL",
				qi(tableName), attrName, ( isNullable ? "" : "NOT" )  
		);
	}

	
	/**
	 * update table to default value and create NOT NULL constraint 
	 * @param conn
	 * @param a
	 */
	protected void createDefaultValueConstraint(final DBConnection conn, final ObjectAttr a) {
		
		logger.debug("set "+a+" default value="+a.getDefaultJdbcValue());
	
		conn.getTransactionTemplate().execute(new TransactionCallback(){
	
			@Override
			public Object doInTransaction(TransactionStatus status) {
				conn.executeUpdate( String.format( 
						"UPDATE %1$s SET %2$s=? WHERE %2$s IS NULL", qi(a.getConfigObject().getRealName()), a.getSQLName() ), 
						new Object[]{a.getDefaultJdbcValue()}  
				);
				
				final Object value = a.isString()||a.isDateTime() ? 
						"'"+a.getDefaultJdbcValue().toString().replaceAll("'", "''")+"'"
						: a.getDefaultJdbcValue();
				conn.execute( String.format(
						"ALTER TABLE %1$s ALTER COLUMN %2$s SET DEFAULT %3$s", 
						qi(a.getConfigObject().getRealName()), 
						a.getSQLName(),	value
				));
				
				return null;
			}
			
		});
	}
	
	public void deleteFromExTree(DBConnection dbConnect, String quotedTableName, String quotedTreeTableName, Object id){
		// delete ancestors for all descendants
		dbConnect.executeUpdate( 
				String.format( 
						"DELETE FROM %1$s WHERE" +
						"  ancestor IN (SELECT ancestor FROM %1$s WHERE id=?) " +
						"  AND id IN ( SELECT id FROM %1$s WHERE ancestor=?)", 
						quotedTreeTableName
				),
				new Object[]{id,id}
		);
		// delete own ancestors
		dbConnect.executeUpdate(
				String.format("DELETE FROM %1$s WHERE id=?",quotedTreeTableName), 
				new Object[]{ id }
		);
	}

}

