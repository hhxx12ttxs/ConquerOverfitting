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
// $Id: Oracle.java 1162 2009-06-26 14:31:43Z vic $
// $Name:  $

package ru.adv.db.adapter;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.util.Assert;

import ru.adv.db.DBConnection;
import ru.adv.db.DBException;
import ru.adv.db.JdbcConnectionParameters;
import ru.adv.db.base.DBValue;
import ru.adv.db.base.MAttribute;
import ru.adv.db.base.MObject;
import ru.adv.db.base.MValue;
import ru.adv.db.config.ConfigIndex;
import ru.adv.db.config.ConfigObject;
import ru.adv.db.config.ConfigParser;
import ru.adv.db.config.DBConfigException;
import ru.adv.db.config.ObjectAttr;
import ru.adv.db.config.PersistentEvent;
import ru.adv.db.config.Trigger;
import ru.adv.db.config.VariableInfo;
import ru.adv.logger.TLogger;
import ru.adv.util.ADVExceptionCode;
import ru.adv.util.BadBooleanException;
import ru.adv.util.ClassCreator;
import ru.adv.util.ErrorCodeException;
import ru.adv.util.StringParser;
import ru.adv.util.Strings;
import ru.adv.util.UnreachableCodeReachedException;

/**
 * ??????? ??? <a href="http://www.oracle.com">Oracle</a>, .
 * @version $Revision: 1.56 $
 */
public class Oracle extends DBAdapter implements NativeTriggers {

	private static final String DRIVER_CLASS_NAME = "oracle.jdbc.OracleDriver";
	private static final String NEXT_OID_FUNCTION = "mz_next_object_id";
	private static final String CURR_OID_FUNCTION = "mz_curr_object_id";

	private static HashMap dbtTypes = new HashMap();

	private static final int MAX_IDENTIFIER_LENGTH = 30;
	private static final int MAX_OBJECT_NAME_LENGTH = MAX_IDENTIFIER_LENGTH - 7; // ex.: mz_bir_
	private static final int MAX_ATTRINBUTE_NAME_LENGTH = MAX_IDENTIFIER_LENGTH;
	private static final Set<String> SQL_KEYWORDS = Collections.unmodifiableSet(new TreeSet<String>(Arrays.asList(new String[]{
		"ALL", "ALTER", "AND", "ANY", "AS", "ASC", "BETWEEN", "BY", "CHAR", "CHECK", "CLUSTER", "COMMENT",
		"COMPRESS", "CONNECT", "CREATE", "CURRENT", "DATE", "DECIMAL", "DEFAULT", "DELETE", "DESC", "DISTINCT",
		"DROP", "ELSE", "EXCLUSIVE", "EXISTS", "FLOAT", "FOR", "FROM", "GROUP", "HAVING", "IMMEDIATE", "IN",
		"INDEX", "INSERT", "INTEGER", "INTERSECT", "INTO", "IS", "LEVEL", "LIKE", "LOCK", "LONG", "MINUS",
		"MLSLABEL", "MODE", "NOT", "NOWAIT", "NULL", "NUMBER", "OF", "ON", "OPTION", "OR", "ORDER", "PCTFREE",
		"PRIOR", "PUBLIC", "RAW", "ROW", "ROWID", "ROWNUM", "SELECT", "SET", "SHARE", "SMALLINT", "START",
		"SUCCESSFUL", "SYNONYM", "SYSDATE", "TABLE", "THEN", "TO", "TRIGGER", "UID", "UNION", "UNIQUE",
		"UPDATE", "USER", "VALIDATE", "VALUES", "VARCHAR", "VARCHAR2", "VIEW", "WHENEVER", "WHERE", "WITH"})));
    private static final String FKC_PREFIX = "MZ_CFK_";
    
    private TLogger logger = new TLogger(Oracle.class);

    // Mapping DBT to SQL
	static {
		dbtTypes.put(DBT_STRING, "VARCHAR2");
		dbtTypes.put(DBT_TEXT, "VARCHAR2(4000)");
		dbtTypes.put(DBT_SHORTINT, "NUMBER(5,0)");
		dbtTypes.put(DBT_INT, "NUMBER(10,0)");
		dbtTypes.put(DBT_LONG, "NUMBER(19,0)");
		dbtTypes.put(DBT_DATE, "DATE");
		dbtTypes.put(DBT_TIMESTAMP, "TIMESTAMP(6)");
		dbtTypes.put(DBT_BOOLEAN, "NUMBER(1,0)");
		dbtTypes.put(DBT_FILE, "NUMBER(1,0)");
		dbtTypes.put(DBT_FLOAT, "NUMBER(20,8)");
		dbtTypes.put(DBT_DOUBLE, "NUMBER(20,18)");
		dbtTypes.put(DBT_NUMERIC, "NUMBER");
	}

	/** The JDBC driver name. Use in connection url <code>jdbc:JDBC_DRIVER:databasename</code>*/
	static {
		// Load the driver
		try {
			ClassCreator.forName(DRIVER_CLASS_NAME);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructor.
	 */
	protected Oracle() {
	}
	
	@Override
	public String getDriverClassName() {
		return DRIVER_CLASS_NAME;
	}
	
	@Override
	public boolean isSupportConstraints() {
		return true;
	}

	
	@Override
	public String addLimitInstructionsToSelect(String selectSentence, int offset, int limit) {
		Assert.isTrue(offset>=0, "Wrong value for offset: "+offset);
		Assert.isTrue(limit>=0, "Wrong value for limit: "+limit);
		String result = "SELECT * FROM ("+selectSentence+") WHERE ROWNUM>"+offset;
		if (limit>0) {
			result += " AND ROWNUM<" + (offset+limit);
		}
		return result;
	}

	/**
	 * ?????????? ???????? {@link DBValue} ? PreparedStatement
	 * @param value
	 * @param ps
	 * @param position
	 */
	protected void setNonNullToStatement(DBValue value, PreparedStatement ps, int position) throws DBAdapterException, SQLException {
		switch (value.type()) {
		case FLOAT:
			ps.setObject(position, new BigDecimal(value.get().toString()), java.sql.Types.NUMERIC);
			break;
		case DOUBLE:
		case NUMERIC:
			ps.setObject(position, new BigDecimal(value.get().toString()), java.sql.Types.NUMERIC);
			break;
		default:
			super.setNonNullToStatement(value, ps, position);
		}
	}

	/**
	 * Generate sub SQL string with customize to SQL text,vchar
	 * @param attrSQLIdent
	 * @return  sub SQL string with customize to STRING or text
	 */
	public String getCustToStringSql(String attrSQLIdent) {
		return "TO_CHAR(" + attrSQLIdent + ")";
	}

	public String getURL(JdbcConnectionParameters connectionParameters) {
		String host = connectionParameters.getHost();
		String port = connectionParameters.getPort();
		StringBuffer sb = new StringBuffer();
		sb.append("jdbc:oracle:thin:@");
		if (host == null) {
			host = "localhost";
		}
		sb.append(host);
		if (port == null) {
			port = "1521";
		}
		sb.append(":");
		sb.append(port);
		sb.append(":");
		sb.append(connectionParameters.getDatabaseName());
		return sb.toString();
	}
	
	@Override
	public boolean isNullsLastOrder() {
		return true;
	}

	public String extractSchemaName(String configName) {
		String result = configName;
		StringTokenizer st = new StringTokenizer(configName, "@");
		if (st.hasMoreTokens())
			result = st.nextToken();
		return result.toUpperCase();
	}

	public String extractDBName(String configName) {
		String result = configName;
		StringTokenizer st = new StringTokenizer(configName, "@");
		if (st.hasMoreTokens())
			st.nextToken();
		if (st.hasMoreTokens())
			result = st.nextToken();
		return result;
	}

	@Override
	public boolean isSupportDistinctOn() {
		return false;
	}

	public String getRandomFunctionSql() {
		return "DBMS_RANDOM.RANDOM";
	}

	public Collection getProcedures(DBConnection conn, String schema) throws SQLException {
		return Collections.EMPTY_LIST;
	}

	/**
	 * This method is used to ignore case.
	 *
	 * @param in The string to transform to upper case.
	 * @return The upper case SQL string.
	 */
	public String toUpperCase(String in) {
		String s = new StringBuffer("UPPER(").append(in).append(")").toString();
		return s;
	}

	/**
	 * This method is used to ignore case.
	 *
	 * @param in The string whose case to ignore.
	 * @return The string in a case that can be ignored.
	 */
	public String ignoreCase(String in) {
		String s = new StringBuffer("UPPER(").append(in).append(")").toString();
		return s;
	}

	/**
	 * Returns the last auto-increment key.  Databases like MySQL
	 * which support this feature will return a result, others will
	 * return null.
	 *
	 * @return SQL expression for most recently inserted database key.
	 */
	public String getSequenceCurrentSql(String schemaName, String name) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ");
		sb.append(getSQLIdentifier(schemaName, name));
		sb.append(".CURRVAL FROM DUAL");
		return sb.toString();
	}


	@Override
	public Long getSequenceNextValue(DBConnection connection, String schemaName, String name) {
		return connection.queryForLong( String.format(getSequenceNextSql(schemaName,name)) );
	}

	private String getSequenceNextSql(String schemaName, String name) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT ");
		sb.append(getSQLIdentifier(schemaName, name));
		sb.append(".NEXTVAL FROM DUAL");
		return sb.toString();
	}

	/**
	 * ??? sequence
	 * @param schemaName
	 * @param name
	 * @return
	 */
	public String getSequenceIncrementSql(String schemaName, String name) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT INCREMENT_BY FROM ALL_SEQUENCES WHERE SEQUENCE_OWNER='");
		sb.append(schemaName.toUpperCase());
		sb.append("' AND SEQUENCE_NAME='");
		sb.append(needQuoting(name) ? name : name.toUpperCase());
		sb.append("'");
		return sb.toString();
	}

	public List getCreateSequenceSql(String name, long startValue, int increment) {
		return Arrays.asList(new Object[]{"CREATE SEQUENCE " + qi(name) + " START WITH " + Long.toString(startValue) + " INCREMENT BY " + Integer.toString(increment)});
	}

	/**
	 * ?????????? SQL ??????? ????????????? ??????? ???????? sequence ? ???????? ????????
	 * @param name ??? sequence
	 * @param value ????? ???????? sequence
	 * @param increment
	 * @return
	 */
	public List getSetValueSequenceSql(String name, long value, int increment) {
		LinkedList sql = new LinkedList();
		sql.addAll(getDropSequenceSql(name));
		sql.addAll(getCreateSequenceSql(name, value, increment));
		return sql;
	}

	public List getDropSequenceSql(String name) {
		return Arrays.asList(new Object[]{"DROP SEQUENCE " + qi(name)});
	}

	public String getDropViewSql(String name) {
		return "DROP VIEW " + qi(name);
	}


	public String getExistentDatabaseSql() {
		return "SELECT username FROM all_users";
	}

	/**
	 * SQL ????????????? ??, ???????????? ??? ???????? ????????????? ??
	 * @param schemaName
	 * @param databaseName
	 * @return
	 * @see DBAdapter#getExistentDatabaseSql
	 */
	public String getDatabaseSQLIdentifier(String schemaName, String databaseName) {
		return schemaName;
	}

	public String getCreateDatabaseSql(String name) {
		if (name != null)
			throw new RuntimeException("You should ask for your database administrator to create database '" + name + "'");
		return null;
	}

    public String getDropDatabaseSql(String name) {
        return null;
    }

    public String getDropStoredProcedureSql(String name) {
		return "DROP FUNCTION " + qi(name);
	}

	public String getDropIndexSql(String tableName, String indexName) {
		return "DROP INDEX " + qi(indexName);
	}

	public String getCreateIndexSql(String tableName, String indexName, List columnNames, boolean unique) {
		StringBuffer sql = new StringBuffer();
		sql.append("CREATE ");
		if (unique)
			sql.append("UNIQUE ");
		sql.append("INDEX ");
		sql.append(qi(indexName));
		sql.append(" ON ");
		sql.append(qi(tableName));
		sql.append(" (");
		for (Iterator i = columnNames.iterator(); i.hasNext();) {
			sql.append(qi(i.next().toString()));
			if (i.hasNext())
				sql.append(',');
		}
		sql.append(')');
		return sql.toString();
	}

	public String getTableAddColumnsSql(String tableName, String columnName, String columnDefinition) {
		StringBuffer sql = new StringBuffer();
		sql.append("ALTER TABLE ");
		sql.append(qi(tableName));
		sql.append(" ADD ");
		sql.append(qi(columnName));
		sql.append(' ');
		sql.append(columnDefinition);
		return sql.toString();
	}

	public String getTableDropColumnsSql(String tableName, Collection columnNames, Collection columnNamesToDrop) {
		StringBuffer sql = new StringBuffer();
		sql.append("ALTER TABLE ");
		sql.append(qi(tableName));
		sql.append(" DROP (");
		for (Iterator i = columnNamesToDrop.iterator(); i.hasNext();) {
			sql.append(qi(i.next().toString()));
			if (i.hasNext()) {
				sql.append(',');
			}
		}
		sql.append(")");
		return sql.toString();
	}

	private boolean eq(Object o1, Object o2) {
		if (o2 != null && o2.toString().equals("NULL"))
			o2 = null;
		if (o1 == null && o2 == null)
			return true;
		if (o1 == null || o2 == null)
			return false;
		return o1.equals(o2);
	}

	private boolean nullable(int i) {
		if (i == COL_NULLABLE)
			return true;
		return false;
	}

	public SqlActionList getTableSyncTypeColumnsActions(DBConnection conn, String schema, ConfigObject o) throws SQLException, DBConfigException, DBAdapterException {
		List result = new LinkedList();
		HashMap columnDefs = new HashMap(); // columnName->DEFINITION
		LinkedList toCopyColumnNames = new LinkedList();
		try {
			Map columns = getTableColumns(conn, schema, o.getName());
			StringBuffer colDef = null;
			for (Iterator i = o.getAttributes().iterator(); i.hasNext();) {
				ObjectAttr col = (ObjectAttr) i.next();
				String name = col.getName();
				ColumnInfo info = (ColumnInfo) columns.get(name);
				if (info == null) {
					continue;
				}

				if (colDef == null) {
					colDef = new StringBuffer();
				}
				colDef.setLength(0);

				String newType = getSQLType(col);
				boolean newNullable = col.isNullable();
				String newDefault = col.getDefaultValue();
				if ((col.getType() == BOOLEAN || col.getType() == FILE) && newDefault != null) {
					newDefault = StringParser.toBoolean(newDefault) ? "1" : "0";
				}

				String oldType = getSQLType(info);
				boolean oldNullable = nullable(info.getNullable());
				String oldDefault = info.getDefault();

				if (name.equals("id")) {
					newNullable = false;
					oldNullable = false;
					newDefault = null;
					oldDefault = null;
				}

				if (!eq(newType, oldType) ||
						!eq(newDefault == null ? newDefault : "'" + newDefault + "'", oldDefault) ||
						oldNullable != newNullable) {

					if (!eq(newType, oldType)) {
						toCopyColumnNames.add(name); // ?????????? ???????? ?????????? ?? ????????? ???????
					}
					colDef.append(newType);
					if (!eq(newDefault, oldDefault)) {
						colDef.append(" DEFAULT ");
						if (!(newDefault == null && col.isRequired())) {
							colDef.append('\'');
							colDef.append(newDefault);
							colDef.append('\'');
						}
						else {
							colDef.append("NULL");
						}
					}
					if (oldNullable != newNullable) {
						if (newNullable) {
							colDef.append(" NULL");
						}
						else {
							colDef.append(" NOT NULL");
						}
					}
					columnDefs.put(name, colDef.toString());
				}
			}
		}
		catch (BadBooleanException e) {
			throw new DBConfigException(e);
		}
		String sql = null;
		if (columnDefs.size() > 0) {
			StringBuffer sb = new StringBuffer();
			sb.append("ALTER TABLE ");
			sb.append(qi(o.getName()));
			sb.append(" MODIFY (");
			for (Iterator i = columnDefs.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				sb.append(qi((String) entry.getKey()));
				sb.append(' ');
				sb.append(entry.getValue());
				if (i.hasNext()) {
					sb.append(",");
				}
			}
			sb.append(")");
			sql = sb.toString();
		}

		// create statements
		// if type changed
		for (Iterator i = toCopyColumnNames.iterator(); i.hasNext();) {
			String colName = (String) i.next();
			String tmpColName = colName + "_tmp";
			// ... ADD TEMP COLUMN
			result.add("ALTER TABLE " + qi(o.getName()) + " ADD " + qi(tmpColName) + " " + getSQLType(o.getAttribute(colName)));
			// ... COPY VALUES TO _tmp_ column
			result.add("UPDATE " + qi(o.getName()) + " SET " + qi(tmpColName) + "=" + qi(colName));
			// ... DROP COLUMN
			result.add("ALTER TABLE " + qi(o.getName()) + " DROP COLUMN " + qi(colName));
			// ... CREATE COLUMN
			result.add("ALTER TABLE " + qi(o.getName()) + " ADD " + qi(colName) + " " + getSQLType(o.getAttribute(colName)));
			// ... UPDATE ORIG COLUMN TO NULL
			//sqlStatements.add("UPDATE "+qi(o.getName())+" SET "+qi(colName)+"=NULL");
			// ... COPY VALUES FROM _tmp_columns
			result.add("UPDATE " + qi(o.getName()) + " SET " + qi(colName) + "=" + qi(tmpColName));
			// ... DROP _TMP_ COLUMNS
			result.add("ALTER TABLE " + qi(o.getName()) + " DROP COLUMN " + qi(tmpColName));
			if (colName.equals("id")) {
				result.add("ALTER TABLE " + qi(o.getName()) + " ADD PRIMARY KEY (" + qi(colName) + ")");
			}
		}
		result.add(sql);
		return new SqlActionList(result);
	}

	public List getOnCreateDatabaseSql(String dbname) {
		return new LinkedList();
	}

	public String getStoredProcedureName(String procedureStatement) {
		if (procedureStatement.length() == 0) {
			return "";
		}
		String name = super.getStoredProcedureName(procedureStatement);
		try {
			StringTokenizer st = new StringTokenizer(name, "(");
			name = st.nextToken(); // name
		}
		catch (NoSuchElementException e) {
		}
		if (name.length() > 2 && name.startsWith("\"") && name.endsWith("\"")) {
			name = name.substring(1, name.length() - 1);
		}
		return name;
	}

	public String getDropTriggerSql(String tableName, String triggerName) {
		return "DROP TRIGGER " + qi(triggerName);
	}

	public String getTriggerName(String triggerStatment) {
		return getStoredProcedureName(triggerStatment);
	}

	public Trigger getCheckForeignTrigger(ConfigObject co) {
		return null;
	}

	public Trigger getOnDeleteForeignTrigger(ConfigObject co, Map foreignTables) {
		return null;
	}

	public Trigger getCheckRequiredsTrigger(ConfigObject co) {
		return null;
	}

	public Trigger getDefaultValueTrigger(ConfigObject co) {
		return null;
	}

	public Trigger getReadonlyAttributesTrigger(ConfigObject co) {
		String sql = "";
		for (Iterator ai = co.getAttributes().iterator(); ai.hasNext();) {
			ObjectAttr a = (ObjectAttr) ai.next();
			if (a.isReadonly()) {
				sql += "    :NEW." + qi(a.getName()) + " := :OLD." + qi(a.getName()) + ";\n";
			}
		}
		if (sql.length() == 0) {
			return null;
		}
		Set<PersistentEvent> events = Collections.singleton(PersistentEvent.BEFORE_UPDATE);
		Set variables = new HashSet();
		return new Trigger("readonly", getName(), sql, events, variables, true, true, co);
	}

	/**
	 * create trigger statment for insert tree object type.
	 * Extended stored procedure defined in <code>etc/pg-sys-objects.xml</code>
	 * @param co
	 * @return SQL statment for create trigger.
	 */
	public Trigger getOnInsertTreeTrigger(ConfigObject co) {
		if (co.isView()) {
			return null;
		}
		//Extended stored procedure defined in <code>etc/pg-sys-objects.xml</code>
		StringBuffer sb = new StringBuffer();
		sb.append("    IF :NEW.tree IS NOT NULL THEN\n");
		sb.append("        mz_rc := check_tree_id('" + qi(co.getName()) + "', :NEW.id, :NEW.tree);\n");
		sb.append("        mz_rc := ins_to_extree('" + qi(co.getName()) + "', :NEW.id, :NEW.tree);\n");
		sb.append("    END IF;\n");
		Set variables = new HashSet();
		variables.add(new VariableInfo("mz_rc", "NUMBER"));
		return new Trigger("insert_tree", getName(), sb.toString(), 
				Collections.singleton(PersistentEvent.AFTER_INSERT), variables, true, true, co);
	}

	/**
	 * create trigger statment for update tree object type
	 * Extended stored procedure defined in <code>etc/pg-sys-objects.xml</code>
	 * @param co
	 * @return SQL statment for create trigger.
	 */
	public Trigger getOnUpdateTreeTrigger(ConfigObject co) {
		if (co.isView()) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("    IF :NEW.tree IS NULL THEN\n");
		sb.append("        IF :OLD.tree IS NOT NULL THEN\n");
		sb.append("            mz_rc := del_from_extree('" + qi(co.getName()) + "', :OLD.id);\n");
		sb.append("        END IF;\n");
		sb.append("    ELSE\n");
		sb.append("        IF :OLD.tree IS NOT NULL THEN\n");
		sb.append("            IF :OLD.tree <> :NEW.tree THEN\n");
		sb.append("                mz_rc := check_tree_id('" + qi(co.getName()) + "', :OLD.id, :NEW.tree);\n");
		sb.append("                mz_rc := del_from_extree('" + qi(co.getName()) + "', :OLD.id);\n");
		sb.append("                mz_rc := ins_to_extree('" + qi(co.getName()) + "', :OLD.id, :NEW.tree);\n");
		sb.append("            END IF;\n");
		sb.append("        ELSE\n");
		sb.append("            mz_rc := check_tree_id('" + qi(co.getName()) + "', :OLD.id, :NEW.tree);\n");
		sb.append("            mz_rc := ins_to_extree('" + qi(co.getName()) + "', :OLD.id, :NEW.tree);\n");
		sb.append("        END IF;\n");
		sb.append("    END IF;\n");
		Set variables = new HashSet();
		variables.add(new VariableInfo("mz_rc", "NUMBER"));
		return new Trigger("update_tree", getName(), sb.toString(), 
				Collections.singleton(PersistentEvent.BEFORE_UPDATE), variables, true, true, co);
	}

	/**
	 * create trigger statment for update tree object type
	 * Extended stored procedure defined in <code>etc/pg-sys-objects.xml</code>
	 * @param co
	 * @return SQL statment for create trigger.
	 */
	public Trigger getOnDeleteTreeTrigger(ConfigObject co) {
		if (co.isView()) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("    /* delete ancestors from extended table*/\n");
		sb.append("    IF :OLD.tree IS NOT NULL THEN\n");
		sb.append("        mz_rc := del_from_extree('" + qi(co.getName()) + "', :OLD.id);\n");
		sb.append("    END IF;\n");
		Set variables = new HashSet();
		variables.add(new VariableInfo("mz_rc", "NUMBER"));
		return new Trigger("delete_tree", getName(), sb.toString(), 
				Collections.singleton(PersistentEvent.BEFORE_DELETE), variables, true, true, co);
	}

	public List<String> getSystemStoredProcedureSql(String schema) {
		List<String> sqls = new LinkedList<String>();
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE OR REPLACE FUNCTION ");
		sb.append(qi(CURR_OID_FUNCTION));
		sb.append(" RETURN NUMBER IS\n");
		sb.append("  cur NUMBER;\n");
		sb.append("BEGIN\n");
		sb.append("  SELECT ");
		sb.append(qi(getCommonSequenceName()));
		sb.append(".currval INTO cur FROM DUAL;\n");
		sb.append("  RETURN cur;\n");
		sb.append("END ");
		sb.append(qi(CURR_OID_FUNCTION));
		sb.append(";\n");
		sqls.add(sb.toString());
		sb.setLength(0);

		sb.append("CREATE OR REPLACE FUNCTION ");
		sb.append(qi(NEXT_OID_FUNCTION));
		sb.append(" RETURN NUMBER IS\n");
		sb.append("  next NUMBER;\n");
		sb.append("BEGIN\n");
		sb.append("  SELECT ");
		sb.append(qi(getCommonSequenceName()));
		sb.append(".nextval INTO next FROM DUAL;\n");
		sb.append("  RETURN next;\n");
		sb.append("END ");
		sb.append(qi(NEXT_OID_FUNCTION));
		sb.append(";\n");
		sqls.add(sb.toString());
		sb.setLength(0);

		sb.append("CREATE OR REPLACE FUNCTION do_dml(select_str IN VARCHAR) RETURN NUMBER IS\n");
		sb.append("    cursor_id NUMBER;\n");
		sb.append("    rc INTEGER;\n");
		sb.append("BEGIN\n");
		sb.append("    cursor_id := DBMS_SQL.OPEN_CURSOR;\n");
		sb.append("    DBMS_SQL.PARSE(cursor_id, select_str, DBMS_SQL.V7);\n");
		sb.append("    BEGIN\n");
		sb.append("        rc := DBMS_SQL.EXECUTE(cursor_id);\n");
		sb.append("    EXCEPTION\n");
		sb.append("        WHEN OTHERS THEN\n");
		sb.append("        BEGIN\n");
		sb.append("            DBMS_SQL.CLOSE_CURSOR(cursor_id);\n");
		sb.append("            RETURN 0;\n");
		sb.append("        END;\n");
		sb.append("    END;\n");
		sb.append("    DBMS_SQL.CLOSE_CURSOR(cursor_id);\n");
		sb.append("    RETURN rc;\n");
		sb.append("END do_dml;\n");
		sqls.add(sb.toString());
		sb.setLength(0);

		sb.append("CREATE OR REPLACE FUNCTION do_select(select_str IN VARCHAR) RETURN NUMBER IS\n");
		sb.append("    cursor_id NUMBER;\n");
		sb.append("    rc INTEGER;\n");
		sb.append("    found NUMBER;\n");
		sb.append("BEGIN\n");
		sb.append("    DBMS_OUTPUT.PUT_LINE(select_str);\n");
		sb.append("    cursor_id := DBMS_SQL.OPEN_CURSOR;\n");
		sb.append("    DBMS_SQL.PARSE(cursor_id, select_str, DBMS_SQL.V7);\n");
		sb.append("    rc := DBMS_SQL.EXECUTE(cursor_id);\n");
		sb.append("    if DBMS_SQL.FETCH_ROWS(cursor_id) = 0 THEN\n");
		sb.append("        found := 0;\n");
		sb.append("    ELSE\n");
		sb.append("        found := 1;\n");
		sb.append("    END IF;\n");
		sb.append("    DBMS_SQL.CLOSE_CURSOR(cursor_id);\n");
		sb.append("    RETURN found;\n");
		sb.append("END do_select;\n");
		sqls.add(sb.toString());
		sb.setLength(0);

/* ?????? ??? ??????????? ??????? ??? ??????? ???? tree*/
		sb.append("CREATE OR REPLACE FUNCTION ex_tree_name(tree_name IN VARCHAR) RETURN VARCHAR IS\n");
		sb.append("BEGIN\n");
		sb.append("    RETURN tree_name||'_tree';\n");
		sb.append("END ex_tree_name;\n");
		sqls.add(sb.toString());
		sb.setLength(0);

/* ???????? ??????? ????????????? ??????? ??? ???? tree*/
/* check_tree_id(TG_RELNAME, NEW.id, NEW.tree); */
		sb.append("CREATE OR REPLACE FUNCTION check_tree_id(rel_name IN VARCHAR, obj_id IN NUMBER, tree_id NUMBER) RETURN NUMBER IS\n");
		sb.append("    ex_tree VARCHAR(4000);\n");
		sb.append("    id_exists NUMBER;\n");
		sb.append("    select_str VARCHAR(4000);\n");
		sb.append("BEGIN\n");
		sb.append("    /* check that tree_id != obj_id */\n");
		sb.append("    IF obj_id = tree_id THEN\n");
		sb.append("        RAISE_APPLICATION_ERROR(-20101, 'Parent tree key ' || tree_id || ' for ' || rel_name || ' is equal for id, cycle.');\n");
		sb.append("    END IF;\n");
		sb.append("    /* check that tree_id not exists in descendants */\n");
		sb.append("    ex_tree := ex_tree_name(rel_name);\n");
		sb.append("    select_str := 'SELECT id FROM ' || ex_tree || ' WHERE id=' || tree_id || ' AND ancestor=' || obj_id;\n");
		sb.append("    id_exists := do_select(select_str);\n");
		sb.append("    IF id_exists>0 THEN\n");
		sb.append("        RAISE_APPLICATION_ERROR(-20102, 'Can not set tree to ' || tree_id || ' in ' || rel_name || ', found cycle');\n");
		sb.append("    END IF;\n");
		sb.append("    RETURN 1;\n");
		sb.append("END check_tree_id;\n");
		sqls.add(sb.toString());
		sb.setLength(0);

/* ???????? ancestors ?? ??????????????? ??????? ??? ??????? ???? tree*/
/* del_from_extree(TG_RELNAME, OLD.id); */
		sb.append("CREATE OR REPLACE FUNCTION del_from_extree(rel_name IN VARCHAR,obj_id IN NUMBER) RETURN NUMBER IS\n");
		sb.append("    ex_tree VARCHAR(4000);\n");
		sb.append("    stat VARCHAR(4000);\n");
		sb.append("    rc NUMBER;\n");
		sb.append("BEGIN\n");
		sb.append("    ex_tree := ex_tree_name(rel_name);\n");
		sb.append("    /* delete ancestors for descendant */\n");
		sb.append("    stat := 'DELETE FROM '||ex_tree||' WHERE';\n");
		sb.append("    stat := stat||' ancestor IN (SELECT ancestor FROM '||ex_tree||' WHERE id='||obj_id||')';\n");
		sb.append("    stat := stat||' AND ';\n");
		sb.append("    stat := stat||' id IN ( SELECT id FROM '||ex_tree||' WHERE ancestor='||obj_id||')';\n");
		sb.append("    rc := do_dml(stat);\n");
		sb.append("    /* delete ancestors for self */\n");
		sb.append("    stat := 'DELETE FROM '||ex_tree||' WHERE id='||obj_id;\n");
		sb.append("    rc := do_dml(stat);\n");
		sb.append("    RETURN 1;\n");
		sb.append("END del_from_extree;\n");
		sqls.add(sb.toString());
		sb.setLength(0);

/* ???????  ancestors ? ??????????????? ??????? ??? ??????? ???? tree*/
/* ins_to_extree(TG_RELNAME, NEW.id, NEW.tree) */
		sb.append("CREATE OR REPLACE FUNCTION ins_to_extree(rel_name IN VARCHAR, obj_id IN NUMBER, obj_tree IN NUMBER) RETURN NUMBER IS\n");
		sb.append("    stat VARCHAR(4000);\n");
		sb.append("    ex_tree VARCHAR(4000);\n");
		sb.append("    rc NUMBER;\n");
		sb.append("BEGIN\n");
		sb.append("    IF obj_tree IS NOT NULL THEN\n");
		sb.append("        ex_tree := ex_tree_name(rel_name);\n");
		sb.append("        /* insert ancestors from parent for self */\n");
		sb.append("        stat := 'INSERT INTO '||ex_tree||' (ancestor,id) ';\n");
		sb.append("        stat := stat || 'SELECT ancestor,'||obj_id||' FROM '||ex_tree||' WHERE id='||obj_tree;\n");
		sb.append("        rc := do_dml(stat);\n");
		sb.append("        /* insert ancestor for self */\n");
		sb.append("        stat := 'INSERT INTO '||ex_tree||' (ancestor,id) VALUES ('||obj_tree||','||obj_id||')';\n");
		sb.append("        rc := do_dml(stat);\n");
		sb.append("        /* insert ancestors for descendant */\n");
		sb.append("        stat := 'INSERT INTO '||ex_tree||' (ancestor,id)';\n");
		sb.append("        stat := stat||' SELECT a.ancestor, i.id FROM';\n");
		sb.append("        stat := stat||'  '||ex_tree||' a,';\n");
		sb.append("        stat := stat||'  ( SELECT id FROM '||ex_tree||' WHERE ancestor='||obj_id||' ) i ';\n");
		sb.append("        stat := stat||' WHERE a.id='||obj_id;\n");
		sb.append("        rc := do_dml(stat);\n");
		sb.append("    END IF;\n");
		sb.append("    RETURN 1;\n");
		sb.append("END ins_to_extree;\n");
		sqls.add(sb.toString());
		sb.setLength(0);


/* ???????? ?? id what ????????? ??? id forTreeId */
/* is_sibling(what,'tablename',forTreeId) */
/*  ??? 'tablename' - ??? ??????? tree  */
		sb.append("CREATE OR REPLACE FUNCTION is_sibling(what_id IN NUMBER,tree_table IN VARCHAR,for_id IN NUMBER) RETURN NUMBER IS\n");
		sb.append("    stat VARCHAR(4000);\n");
		sb.append("BEGIN\n");
		sb.append("    stat := 'SELECT id FROM '||tree_table||' WHERE id='||what_id;\n");
		sb.append("    stat := stat ||' AND NVL(tree,-1) IN ( SELECT NVL(tree,-1) FROM '||tree_table||' WHERE id='||for_id||')';\n");
		sb.append("END is_sibling;\n");
		sqls.add(sb.toString());
		sb.setLength(0);

/* ???????? ?? id what ???????? ??? id forTreeId */
/* is_parent(what,'tablename',forTreeId) */
/*  ??? 'tablename' - ??? ??????? tree  */
		sb.append("CREATE OR REPLACE FUNCTION is_parent(what_id IN NUMBER,tree_table IN VARCHAR,for_id IN NUMBER) RETURN NUMBER IS\n");
		sb.append("    stat varchar(4000);\n");
		sb.append("BEGIN\n");
		sb.append("    stat := 'SELECT id FROM '||tree_table||' WHERE id='||what_id;\n");
		sb.append("    stat := stat||' AND id IN ( SELECT NVL(tree,-1) FROM '||tree_table||' WHERE id='||for_id||')';\n");
		sb.append("    RETURN do_select(stat);\n");
		sb.append("END is_parent;\n");
		sqls.add(sb.toString());
		sb.setLength(0);

/* ???????? ?? id what ???????? ??? id forTreeId */
/* is_child(what,'tablename',forTreeId) */
/*  ??? 'tablename' - ??? ??????? tree  */
		sb.append("CREATE OR REPLACE FUNCTION is_child(what_id IN NUMBER,tree_table IN VARCHAR,for_id IN NUMBER) RETURN NUMBER IS\n");
		sb.append("BEGIN\n");
		sb.append("    return do_select('SELECT id FROM '||tree_table||' WHERE id='||what_id||' AND tree='||for_id);\n");
		sb.append("END is_child;\n");
		sqls.add(sb.toString());
		sb.setLength(0);

/* ???????? ?? id what ??????????? ??? id forTreeId */
/* is_ancestor(what,'tablename',forTreeId) */
/*  ??? 'tablename' - ??? ??????? tree  */
		sb.append("CREATE OR REPLACE FUNCTION is_ancestor(what_id IN NUMBER,tree_table IN VARCHAR,for_id IN NUMBER) RETURN NUMBER IS\n");
		sb.append("    ex_tree VARCHAR(4000);\n");
		sb.append("BEGIN\n");
		sb.append("    ex_tree := ex_tree_name(tree_table);\n");
		sb.append("    RETURN do_select('SELECT id FROM '||ex_tree||' WHERE ancestor='||what_id||' AND id='||for_id);\n");
		sb.append("END is_ancestor;\n");
		sqls.add(sb.toString());
		sb.setLength(0);


/* ???????? ?? id what ???????????? ??? id forTreeId */
/* is_descendant(what,'tablename',forTreeId) */
/*  ??? 'tablename' - ??? ??????? tree  */
		sb.append("CREATE OR REPLACE FUNCTION is_descendant(what_id IN NUMBER,tree_table IN VARCHAR,for_id IN NUMBER) RETURN NUMBER IS\n");
		sb.append("    ex_tree VARCHAR(4000);\n");
		sb.append("BEGIN\n");
		sb.append("    ex_tree := ex_tree_name(tree_table);\n");
		sb.append("    RETURN do_select('SELECT id FROM '||ex_tree||' WHERE id='||what_id||' AND ancestor='||for_id);\n");
		sb.append("END is_descendant;\n");
		sqls.add(sb.toString());
		sb.setLength(0);

		return sqls;
	}

	public Trigger getSetIdTrigger(ConfigObject co) {
		if (co.isView()) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("    IF INSERTING THEN\n");
		sb.append("     IF :new.id IS NULL THEN\n");
		sb.append("        :new.id := ");
		sb.append(qi(NEXT_OID_FUNCTION));
		sb.append("();\n");
		sb.append("     END IF;\n");
		sb.append("    END IF;\n");
		sb.append("    IF UPDATING THEN\n");
		sb.append("        :new.id := :old.id;\n");
		sb.append("    END IF;\n");
		Set<PersistentEvent> events = new HashSet<PersistentEvent>();
		events.add(PersistentEvent.BEFORE_UPDATE);
		events.add(PersistentEvent.BEFORE_INSERT);
		Set variables = new HashSet();
		return new Trigger("set_id", getName(), sb.toString(), events, variables, true, true, co);
	}

	private String privilegeOnObject(boolean isGrant, String object, String to, Collection privilege) {
		String sql = isGrant ? "GRANT " : "REVOKE ";
		for (Iterator i = privilege.iterator(); i.hasNext();) {
			sql += (String) i.next();
			if (i.hasNext())
				sql += ",";
		}
		sql += " ON " + qi(object) + " " + (isGrant ? "TO " : "FROM ");
		sql += to;
		logger.debug(sql);
		return sql;
	}

	public String dbtToSQLType(String dbt) throws DBAdapterException {
		String type = (String) dbtTypes.get(dbt);
		if (type == null)
			throw new DBAdapterException("DBT constant=" + dbt + " is not defined");
		return type;
	}

	public String getSQLType(ColumnInfo info) throws DBAdapterException {
		String sqlType = "";
		sqlType += info.getTypeName();
		if (sqlType.indexOf("VARCHAR2") != -1) {
			sqlType += "(" + info.getSize() + ")";
		}
		else if (sqlType.equals("NUMBER")) {
			Object precision = info.getSize();
			Object scale = info.getDigits();
			if (precision != null) {
				sqlType += "(" + precision;
				if (scale != null) {
					sqlType += "," + scale;
				}
				sqlType += ")";
			}
		}
		return sqlType;
	}

	public List getCreateObjectSQL(ConfigObject o) throws DBAdapterException {
		List result = new LinkedList();
        if (o.isSequence()) {
            result.addAll(getCreateSequenceSql(o.getName(), 1, 1));
        } else if (o.isView()) {
            result.add(o.getViewText());
        } else if (o.isTable()) {
            String create = "CREATE TABLE " + qi(o.getName()) + " (";
            for (Iterator i = o.getAttributes().iterator(); i.hasNext();) {
                ObjectAttr attr = (ObjectAttr) i.next();
                String id = attr.getName();
                create += qi(id) + " ";
                create += getSQLType(attr);
                if (id.equals("id"))
                    create += " PRIMARY KEY";
                if (i.hasNext())
                    create += ", ";
            }
            create += ")";
            result.add(create);
        } else if (o.isAlias()) {
            throw new DBAdapterException("Alias object '" + o.getName() + "' can't have create SQL statment");
        } else {
            throw new DBAdapterException("Unknown object type '" + o.getName() + "' can't have create SQL statment");
        }
		return result;
	}


    /**
     * ????????? ?? ?????? ??? ?????????? ????? ??????
     * @return false ?? ?????????? constraint primary key(id)
     */
    public boolean isCreatePrimaryIndex() {
        return false;
    }


//	@Override
//	public boolean isSupportForeignConstrain() {
//		return true;
//	}

	public void dropConstraints(DBConnection conn, String schema, String tableName) throws SQLException, DataAccessException {
		final List<String> l = new LinkedList<String>();
		
		final String fk = registeredName(FK_PREFIX + tableName + "_");
		final String check = registeredName(CHECK_PREFIX + tableName + "_");
		final String tree = registeredName(TREE_FK_PREFIX + tableName);
		
		conn.executeQuery(
				"SELECT CONSTRAINT_NAME FROM all_constraints WHERE owner=? AND table_name=?", 
				new Object[]{registeredName(schema),registeredName(tableName)}, 
				new RowCallbackHandler(){
					public void processRow(ResultSet rs) throws SQLException {
						String name = rs.getString(1);
						if (name.startsWith(fk) || name.startsWith(check) || name.equals(tree)) {
							l.add(rs.getString(1)); // ???????? ???????? CONSTRAINT_NAME ??????? ???? ? ?????????
						}
					}
				}
		);
		
		for (String name : l) {
			conn.execute("ALTER TABLE " + qi(tableName) + " DROP CONSTRAINT " + qi(name));
		}
	}

	public String getSQLIdentifier(ConfigObject co) {
		return getSQLIdentifier(co.getSchemaName(), co.getRealName());
	}

	public String getSQLIdentifier(String schemaName, String tableName) {
		StringBuffer sb = new StringBuffer();
		sb.append(schemaName);
		sb.append('.');
		sb.append(qi(tableName));
		return sb.toString();
	}

	public String getSQLIdentifier(ObjectAttr oa) {
		return getSQLIdentifier(oa.getName());
	}

	public String getSQLIdentifier(String attrName) {
		return qi(attrName);
	}

    /**
     *
     * @param sqlSelect
     * @param limit
     * @return
     * @deprecated
     */
	public String createLimitedSelect(String sqlSelect, int limit) {
		return "SELECT * FROM (" + sqlSelect + ") WHERE rownum<=" + limit;
	}

	public Trigger getLogTrigger(ConfigObject co) {
		if (co.isView()) {
			return null;
		}
//		String sql = "    if (select count(*) from inserted) > 0";
		String sql = "    IF INSERTING THEN\n";
		sql += "        mz_id := :new.id;\n";
		sql += "    ELSE\n";
		sql += "        mz_id := :old.id;\n";
		sql += "    END IF;\n";
		sql += "    IF INSERTING THEN\n";
		sql += "        mz_op := 'INSERT';\n";
		sql += "    END IF;\n";
		sql += "    IF UPDATING THEN\n";
		sql += "        mz_op := 'UPDATE';\n";
		sql += "    END IF;\n";
		sql += "    IF DELETING THEN\n";
		sql += "        mz_op := 'DELETE';\n";
		sql += "    END IF;\n";
		sql += "    INSERT INTO " + getLogTableName() + " ( "+qi("id")+","+qi("time")+","+qi("user")+","+qi("object")+","+qi("action")+","+qi("objectid")+")\n";
		sql += "	     VALUES (" + getLogSequenceName() + ".NEXTVAL, SYSDATE, UID, '" + co.getName() + "', mz_op, mz_id);\n";
		Set<PersistentEvent> events = new HashSet<PersistentEvent>();
		events.add(PersistentEvent.AFTER_UPDATE);
		events.add(PersistentEvent.AFTER_INSERT);
		events.add(PersistentEvent.AFTER_DELETE);
		Set variables = new HashSet();
		variables.add(new VariableInfo("mz_id", "VARCHAR(25)"));
		variables.add(new VariableInfo("mz_op", "VARCHAR(6)"));
		return new Trigger("log", getName(), sql, events, variables, true, true, co);
	}

	public Trigger getLogupdateTrigger(ConfigObject co) {
		if (co.isView()) {
			return null;
		}
		String sql = "    UPDATE " + getLogupdateTableName() + " SET "+qi("time")+"=SYSDATE WHERE "+qi("object")+"='" + qi(co.getName()) + "';\n";
		sql += "    IF SQL%NOTFOUND THEN\n";
		sql += "        SELECT NVL(MAX(id),0)+1 INTO mz_id1 FROM " + getLogupdateTableName() + ";\n";
		sql += "        INSERT INTO " + getLogupdateTableName() + " ("+qi("id")+","+qi("object")+","+qi("time")+") VALUES (mz_id1, '" + co.getName() + "', SYSDATE);\n";
		sql += "    END IF;\n";
		Set<PersistentEvent> events = new HashSet<PersistentEvent>();
		events.add(PersistentEvent.AFTER_UPDATE);
		events.add(PersistentEvent.AFTER_INSERT);
		events.add(PersistentEvent.AFTER_DELETE);
		Set variables = new HashSet();
		variables.add(new VariableInfo("mz_id1", "VARCHAR(25)"));
		return new Trigger("logupdate", getName(), sql, events, variables, true, false, co);
	}

	/**
	 * ?????????? INSERT ??????? ??? ???? object version
	 * @param repositoryId ????????????? ???????????
	 * @param co
	 * @return
	 * @see ConfigParser#VERSION_ATTR_ID
	 */
	public Trigger getInsertVersionTrigger(String repositoryId, ConfigObject co) {
		if (!co.isTable()) {
			return null;
		}
		String dbName = co.getDBConfig().getId();
		String sql = "";
		sql += "    :NEW." + qi(ConfigParser.VERSION_ATTR_ID) + ":='1:" + repositoryId + ":" + dbName + "';\n";
		Set variables = new HashSet();
		return new Trigger("insert_version", getName(), sql, 
				Collections.singleton(PersistentEvent.BEFORE_INSERT), variables, true, true, co);
	}

	/**
	 * ?????????? UPDATE ??????? ??? ???? object version
	 * @param repositoryId ????????????? ???????????
	 * @param co
	 * @return
	 * @see ConfigParser#VERSION_ATTR_ID
	 */
	public Trigger getUpdateVersionTrigger(String repositoryId, ConfigObject co) {
		if (!co.isTable()) {
			return null;
		}
		Set variables = new HashSet();
		variables.add(new VariableInfo("mz_version", "NUMBER(10,0)"));
		String attrOldID = ":OLD." + qi(ConfigParser.VERSION_ATTR_ID);
        String attrNewID = ":NEW." + qi(ConfigParser.VERSION_ATTR_ID);
		String dbName = co.getDBConfig().getId();
		String sql = "";
		// to_number( substr("objversion",0,instr("objversion",':')+1) )
		sql += "    mz_version := TO_NUMBER( SUBSTR(" + attrOldID + ",0,INSTR(" + attrOldID + ",':')-1) );\n";
		sql += "    IF mz_version IS NULL THEN\n";
		sql += "        " + attrNewID + ":='1:" + repositoryId + ":" + dbName + "';\n";
		sql += "    ELSE\n";
		sql += "        mz_version := mz_version + 1;\n";
		sql += "        " + attrNewID + ":=mz_version||':" + repositoryId + ":" + dbName + "';\n";
		sql += "    END IF;\n";
		return new Trigger("update_version", getName(), sql, 
				Collections.singleton(PersistentEvent.BEFORE_UPDATE), variables, true, true, co);
	}


	private static Collection seqPerms;

	static {
		seqPerms = new LinkedList();
		seqPerms.add("SELECT");
		seqPerms = Collections.unmodifiableCollection(seqPerms);
	}

	private static Map systemObjectSQL = new HashMap();

	static {
        DBAdapter a = new Oracle();
		List sql =  Arrays.asList(new Object[] {"CREATE TABLE " + a.getLogTableName() + " ("+
                a.qi("id")+" number not null primary key, "+
                a.qi("time")+" timestamp, "+
                a.qi("user")+" varchar(50), "+
                a.qi("object")+" varchar(32), "+
                a.qi("action")+" varchar(6), "+
                a.qi("objectid")+" varchar(25))"});
		systemObjectSQL.put(a.getLogTableName(), sql);
		sql = Arrays.asList(new Object[] {"CREATE TABLE " + a.getLogupdateTableName() + " ("+
                a.qi("id")+" number not null primary key, " +
                a.qi("object")+" varchar(32) unique, " +
                a.qi("time")+" timestamp)"});
		systemObjectSQL.put(a.getLogupdateTableName(), sql);

		sql = new Oracle().getCreateSequenceSql(a.getLogSequenceName(), 1, 1);
		systemObjectSQL.put(a.getLogSequenceName(), sql);

		systemObjectSQL = Collections.unmodifiableMap(systemObjectSQL);
	};

	public List getSystemObjectCreateSql(String objectName) {
		return (List) systemObjectSQL.get(objectName);
	}

	public boolean isEnablesForEachStatementTriggers() {
		return true;
	}

	public String getName() {
		return DBAdapter.ORACLE;
	}

	public TriggerSQLGenerator getTriggerSQLGenerator() {
		return new OracleTriggerSQLGenerator(this);
	}

	public int getMaxObjectNameLength() {
		return MAX_OBJECT_NAME_LENGTH;
	}

	public int getMaxAttributeNameLength() {
		return MAX_ATTRINBUTE_NAME_LENGTH;
	}

	public String getTriggerReturnType() {
		return "";
	}

	public boolean storedProcedureExists(DBConnection connect, String functionName) throws SQLException {
		return true;
	}

	public boolean indexExists(DBConnection connect, String tableName, String indexName) throws SQLException {
		boolean result = false;
		String owner = connect.getUserName().toUpperCase();
		return (Boolean)connect.executeQuery(
                "SELECT table_name, index_name FROM all_indexes WHERE table_name=? AND index_name=? AND owner=?",
                new Object[]{registeredName(tableName), registeredName(indexName), registeredName(owner)},
                new ResultSetExtractor() {
					public Object extractData(ResultSet rs)	throws SQLException, DataAccessException {
						return rs.next();
					}
                }
		);
	}

    /**
     * ??????? TEMPORARY TABLE
     * @param tableName ??? ???????
     * @param attrsDefinition attrName -> attrDefinition
     * @return
     */
    public String getCreateTemporaryTableSql(String tableName, Map attrsDefinition) {
        StringBuffer stat = new StringBuffer();
        stat.append("CREATE GLOBAL TEMPORARY TABLE ");
        stat.append(getSQLIdentifier(tableName));
        stat.append(" (");
        for (Iterator i = attrsDefinition.entrySet().iterator(); i.hasNext(); ){
            Map.Entry entry = (Map.Entry) i.next();
            stat.append( getSQLIdentifier(entry.getKey().toString()));
            stat.append( " " );
            stat.append( entry.getValue().toString() );
            if (i.hasNext()) {
                stat.append(",");
            }
        }
        stat.append(" ) ON COMMIT PRESERVE ROWS");
        return stat.toString();
    }


	public boolean tableExists(DBConnection connect, String name) throws SQLException {
		return true;
	}

	public boolean viewExists(DBConnection connect, String name) throws SQLException {
		return true;
	}

	public boolean sequenceExists(DBConnection connect, String name) throws SQLException {
		return true;
	}

	public boolean isTriggerExists(DBConnection connect, String tableName, String triggerName) throws SQLException {
		return true;
	}

	public boolean enablesCreatingDatabases() {
		return false;
	}

	public boolean useSelectedAttributesAliasing() {
		return true;
	}

	public Set getSQLKeywords() {
		return SQL_KEYWORDS;
	}

	public List getDropTableSql(String name) throws SQLException {
		return Arrays.asList(new Object[]{"DROP TABLE " + quotedIdentifier(name)});
	}

    public List getDropTableSql(String name, boolean isCascade){
        return Arrays.asList(new Object[]{"DROP TABLE " + quotedIdentifier(name) + (isCascade? " CASCADE CONSTRAINTS" : "" )});
    }


	protected String registeredName(String name) {
        if (needQuoting(name)) {
            return name;
        }
        return name.toUpperCase();
	}

	/**
     * ?????????? ?????? ???????? {@link ConfigIndex} ???????????? ? ??
     * @param connection
     * @param table
     * @return
     * @throws SQLException
     * @throws DBConfigException
     */
    public List getIndexes(final DBConnection connection, String table) throws SQLException, DBConfigException {
        final LinkedList result = new LinkedList();
        String owner = registeredName(connection.getUserName());
        final String tableName = registeredName(table);
        connection.executeQuery(
                "SELECT index_name, uniqueness FROM all_indexes idx " +
                "WHERE " +
                "NOT EXISTS (" + // Don't get constraint primary index
                " select 'TRUE' from all_constraints ac " +
                "  where ac.TABLE_NAME = idx.TABLE_NAME " +
                "  and   ac.OWNER = idx.OWNER " +
                "  and   ac.INDEX_NAME = idx.INDEX_NAME " +
                "  and ac.CONSTRAINT_TYPE='P'" +
                ") " +
                "AND table_name=? AND owner=?",
                new Object[]{tableName, owner},
                new RowCallbackHandler() {
					public void processRow(ResultSet rs) throws SQLException {
		                result.add(createConfigIndex(
		                		connection,tableName, rs.getString(1), rs.getString(2).equalsIgnoreCase("unique"))
		                );
					}
                	
                }
        );
        return result;
    }

	private ConfigIndex createConfigIndex(DBConnection connection, String table, String indexName, boolean unique) throws SQLException, DBConfigException {
        List columnNames = getIndexColumnNames(connection,table,indexName);
        return new ConfigIndex(indexName, columnNames, unique);
	}

    /**
     * @return index columns name in lower case
     */
    private List getIndexColumnNames(DBConnection connection, String table, String indexName) throws SQLException {
    	final LinkedList columns = new LinkedList();
    	String owner = connection.getUserName().toUpperCase();
    	connection.executeQuery(
    			"SELECT column_name, column_position FROM all_ind_columns WHERE table_name=? AND index_name=? AND index_owner=? ORDER BY column_position", 
    			new Object[]{table, indexName, owner},
    			new RowCallbackHandler() {
    				public void processRow(ResultSet rs) throws SQLException {
    					columns.add(rs.getString(1).toLowerCase());
    				}
    			}
    	);
    	return columns;
    }

    /**
     * @return index columns name in lower case
     */
    public boolean doesTableHavePrimaryKey(DBConnection connection, String registeredTableName) throws SQLException {
    	String owner = connection.getUserName().toUpperCase();
    	String sql = "SELECT 'TRUE' FROM all_constraints WHERE CONSTRAINT_TYPE='P' AND table_name=? AND owner=?";
    	return (Boolean)connection.executeQuery(
    			sql, 
    			new Object[]{registeredTableName, owner},
    			new ResultSetExtractor() {
    				public Object extractData(ResultSet rs)	throws SQLException, DataAccessException {
    					return rs.next();
    				}

    			}
    	);
    }

	public boolean isSQLKeyword(String tmp) {
		return getSQLKeywords().contains(tmp.toUpperCase());
	}

	public List getSystemAttributes() {
		return Collections.EMPTY_LIST;
	}

	public boolean proceduralUpdates() {
		return false;
	}

	public boolean allowsNullsInUniqueIndex() {
		return true;
	}

//	public String getSequencePhysicalName(String name) {
//		return name;
//	}

	/* (non-Javadoc)
	 * @see ru.adv.db.adapter.DBAdapter#getConnectionParameters()
	 */
	@Override
	public Map<String, String> getConnectionParameters() {
		return Collections.singletonMap("charSet", "Cp1251");
	}

    public Map extractAdditionalInfo(Throwable e, MObject object) {
        Map result = Collections.EMPTY_MAP;
        String msg = e.getMessage();
        if (e instanceof SQLException) {
            int state = ((SQLException)e).getErrorCode();
            switch (state) {
                case 1:
                    // ORA-00001 unique constraint violated
                    result = parseUniquenessViolationMessage(msg, object);
                    break;
                case 2291:
                    // ORA-02291 integrity constrain violated - parent key not found
                    result = parseForeignMessage(msg, object);
                    break;
            }
        }
        return result;
    }

    private static final Pattern UNIQUE_PATTERN;
    private static final Pattern FOREIGN_PATTERN;

    static {
        //
        // ORA-02291 integrity constraint (SCHEMA.MZ_CFK_A_1) violated - parent key not found
        FOREIGN_PATTERN = Pattern.compile("ORA-02291[^\\(]+\\([^\\.]+\\.([^\\)]+)\\)");
        // ORA-00001 unique constraint (string.string) violated
        UNIQUE_PATTERN = Pattern.compile("ORA-00001[^\\(]+\\([^\\.]+\\.([^\\)]+)\\)");
    }

    private Map parseForeignMessage(String msg, MObject object) {
        Map result = new HashMap();
        Matcher matcher = FOREIGN_PATTERN.matcher(msg);
        boolean found = matcher.find();
        if (found) {
            MAttribute attr = findAttribute(object, matcher.group(1));
            if (attr != null) {
                result.put(ErrorCodeException.ATTR, attr.getName());
                result.put("foreign",    attr.getForeignObjectName());
                MValue val = attr.getDBValue();
                if (val!=null) {
                    result.put("foreign-id", val.get());
                }
                result.put("error-code", new Integer(ADVExceptionCode.INVALID_FOREIGN));
            }
        }
        return result;
    }

    private MAttribute findAttribute(MObject object, String constraint) {
        MAttribute result = null;
        //MZ_CFK_A_1
        logger.debug("constraint="+constraint);
        if (constraint.startsWith(FKC_PREFIX)) {
            int pos = getAttributePosition(constraint);
            logger.debug("pos="+pos);
            int fk = 1;
            for (Iterator i = object.getConfigObject().getAttributes().iterator(); i.hasNext();) {
                ObjectAttr a = (ObjectAttr) i.next();
                if (a.isForeign()) {
                    try {
                        ConfigObject fo = a.getForeignObject();
                        if (fo.isTable()) {
                            if (fk == pos) {
                                try {
                                    result = object.getAttribute(a.getName());
                                } catch (DBException e) {
                                    throw new UnreachableCodeReachedException(e);
                                }
                            }
                            fk++;
                        }
                    } catch (DBConfigException e) {
                        throw new UnreachableCodeReachedException(e);
                    }
                }
            }
        }
        return result;
    }

    private int getAttributePosition(String constraint) {
        String name = constraint.substring(FKC_PREFIX.length(), constraint.length());
        List pair = Strings.split(name, "_");
        int no = Integer.parseInt((String) pair.get(1));
        return no;
    }

    private Map parseUniquenessViolationMessage(String msg, MObject object) {
        Map result = new HashMap();
        Matcher matcher = UNIQUE_PATTERN.matcher(msg);
        if (matcher.find()) {
            String indexName = matcher.group(1);
            result.put("error-code", new Integer(ADVExceptionCode.UNIQUENESS_VIOLATION));
            result.put("index", indexName);
            try {
                ConfigIndex index = object.getConfigObject().getIndex(indexName);
                List uniqueAttrs = index.getColumns();
                result.put("unique-attributes", uniqueAttrs);
                setUniqueValues(uniqueAttrs, object, result);
            } catch (DBConfigException e) {
                // unknown index: we cannot set 'unique-attributes' and 'values' attributes
            }
        }
        return result;
    }

    public boolean supportsAdditionalInfoExtraction() {
        return true;
    }

    public String subSqlFormatDate(String attrSQLIdent, String simpleDateFormat) {
        String format = new DFCOracle().convert(simpleDateFormat);
        return "to_char(" + attrSQLIdent + ",'" + format + "')";
    }
}

