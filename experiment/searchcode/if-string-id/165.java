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
// $Id: Postgres.java 1258 2009-08-07 12:02:50Z vic $
// $Name:  $

package ru.adv.db.adapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.util.Assert;

import ru.adv.db.DBConnection;
import ru.adv.db.DBException;
import ru.adv.db.JdbcConnectionParameters;
import ru.adv.db.base.MCast;
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
import ru.adv.util.AttributeException;
import ru.adv.util.AttributeName;
import ru.adv.util.ClassCreator;
import ru.adv.util.ErrorCodeException;
import ru.adv.util.Match;
import ru.adv.util.REMatcher;
import ru.adv.util.Strings;
import ru.adv.util.UnreachableCodeReachedException;

/**
 * ??????? ??? <a href="http://www.pgsql.org">PostgresSQL</a>, .
 * @version $Revision: 1.66 $
 */
public class Postgres extends DBAdapter implements NativeTriggers {

	private static final String DRIVER_CLASS_NAME = "org.postgresql.Driver";

	private static HashMap<String,String> dbtTypes = new HashMap<String,String>();

	private static final int MAX_IDENTIFIER_LENGTH = 32;
	private static final int MAX_OBJECT_NAME_LENGTH = MAX_IDENTIFIER_LENGTH - 6; // ex.: mz_bi_
	private static final int MAX_ATTRINBUTE_NAME_LENGTH = MAX_IDENTIFIER_LENGTH;
    private static final Set<String> SQL_KEYWORDS = Collections.unmodifiableSet(new TreeSet<String>(Arrays.asList(new String[]{
        "ABORT", "ACL", "ADD", "AGGREGATE", "APPEND", "ARCHIVE", "ARCH_STORE", "BACKWARD", "BINARY", "CHANGE",
        "CLUSTER", "COPY", "DATABASE", "DELIMITER", "DELIMITERS", "DO", "EXTEND", "EXPLAIN", "FORWARD", "HEAVY",
        "INDEX", "INHERITS", "ISNULL", "LIGHT", "LISTEN", "LOAD", "MERGE", "NOTHING", "NOTIFY", "NOTNULL", "OIDS",
        "PURGE", "RENAME", "REPLACE", "RETRIEVE", "RETURNS", "RULE", "RECIPE", "SETOF", "STDIN", "STDOUT", "STORE",
        "VACUUM", "VERBOSE", "VERSION"})));

	// Mapping DBT to SQL
	static {
		dbtTypes.put(DBT_STRING, "varchar");
		dbtTypes.put(DBT_TEXT, "text");
		dbtTypes.put(DBT_SHORTINT, "int2");
		dbtTypes.put(DBT_INT, "int4");
		dbtTypes.put(DBT_LONG, "int8");
		dbtTypes.put(DBT_DATE, "date");
		dbtTypes.put(DBT_TIMESTAMP, "timestamptz");
		dbtTypes.put(DBT_BOOLEAN, "int2");
		dbtTypes.put(DBT_FILE, "int2");
		dbtTypes.put(DBT_FLOAT, "float8");
		dbtTypes.put(DBT_DOUBLE, "float8");
		dbtTypes.put(DBT_NUMERIC, "numeric");
	}

	/** The JDBC driver name. Use in connection url <code>jdbc:JDBC_DRIVER:databasename</code>*/
	static {
		// Load the driver
		try {
			ClassCreator.forName(DRIVER_CLASS_NAME);
		}
		catch (Exception e) {
			TLogger.error(Postgres.class, "DBPostgres: " + e);
		}
	}

	private TLogger logger = new TLogger(Postgres.class);
	
	/**
	 * Constructor.
	 */
	protected Postgres() {
	}
	
	@Override
	public boolean isSupportConstraints() {
		return false;
	}

	
    @Override
	public String getDriverClassName() {
		return DRIVER_CLASS_NAME;
	}


	public String getAdminConnectionPropertiesSQL() {
        return "SET check_function_bodies = false";
    }
	
	@Override
	public boolean isSupportDistinctOn() {
		return true;
	}
	
	@Override
	public boolean isNullsLastOrder() {
		return true;
	}
	
	
	@Override
	public String addLimitInstructionsToSelect(String selectSentence, int offset, int limit) {
		Assert.isTrue(offset>=0, "Wrong value for offset: "+offset);
		Assert.isTrue(limit>=0, "Wrong value for limit: "+limit);
		return selectSentence + String.format(" LIMIT %1$s OFFSET %2$s", (limit==0? "ALL" : limit) , offset );
	}

	/**
	 * ???????? ????????? SQL ? ???????????? ???? SQL,
	 * ???????????? ? Postgres ??? ??????????? ????????? ????? float 1.3::float4 <> 1.3::float8
	 * @param type ???????? {@link Types}
	 * @param sqlValue SQL ????????
	 * @return
	 */
	public String sqlCustomize(int type, String sqlValue) {
		String value = sqlValue;
		try {
			switch (type) {
			case Types.DOUBLE:
				value += "::" + dbtToSQLType(Types.DBT_DOUBLE);
				break;
			case Types.FLOAT:
				value += "::" + dbtToSQLType(Types.DBT_FLOAT);
				break;
			case Types.NUMERIC:
				value += "::" + dbtToSQLType(Types.DBT_NUMERIC);
				break;
			}
		}
		catch (DBAdapterException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		return value;
	}


	public String getURL( JdbcConnectionParameters connectionParameters ) { 
		String database = connectionParameters.getDatabaseName();
		String host = connectionParameters.getHost();
		String port = connectionParameters.getPort();
		// jdbc:postgresql://host:port/database
		StringBuffer sb = new StringBuffer();
		sb.append("jdbc:postgresql:");
		if (host != null) {
			sb.append("//");
			sb.append(host);
			if (port != null) {
				sb.append(":");
				sb.append(port);
			}
			sb.append("/");
		}
		if (database != null)
			sb.append(database);
		return sb.toString();
	}

	public String extractDBName(String configName) {
		return configName;
	}

	/**
	 * SQL ????????????? ??, ???????????? ??? ???????? ????????????? ??
	 * @param schemaName
	 * @param databaseName
	 * @return
	 * @see DBAdapter#getExistentDatabaseSql
	 */
	public String getDatabaseSQLIdentifier(String schemaName, String databaseName) {
		return databaseName;
	}

	public String extractSchemaName(String configName) {
		return null;
	}

	public String getRandomFunctionSql() {
		return "random()";
	}

    /**
	 * ??????? ????????? SQL ??? attrSQLIdent, ?????????????? ???????? ???? DATE
	 * ??? TIMESTAMP ? ??????
	 * @param attrSQLIdent ????????? ?? ????????? SELECT
	 * @param simpleDateFormat ?????? ?? java.text.SimpleDateFormat
	 * @return "to_char("+attrSQLIdent+",'"+postgresFormat+"')"
	 */
	public String subSqlFormatDate(String attrSQLIdent, String simpleDateFormat) {
		String postgresFormat = new DFCPostgres().convert(simpleDateFormat);
		return "to_char(" + attrSQLIdent + ",'" + postgresFormat + "')";
	}

	/**
	 * This method is used to ignore case.
	 *
	 * @param in The string to transform to upper case.
	 * @return The upper case SQL string.
	 */
	public String toUpperCase(String in) {
		return new StringBuffer("UPPER(").append(in).append(")").toString();
	}

	/**
	 * Generate sub SQL string with customize to SQL text,vchar
	 * @param attrSQLIdent
	 * @return  sub SQL string with customize to STRING or text
	 */
	public String getCustToStringSql(String attrSQLIdent) {
		return "text(" + attrSQLIdent + ")";
	}

	/**
	 * This method is used to ignore case.
	 *
	 * @param in The string whose case to ignore.
	 * @return The string in a case that can be ignored.
	 */
	public String ignoreCase(String in) {
		return new StringBuffer("UPPER(").append(in).append(")").toString();
	}

	/**
	 * Returns the last auto-increment key.  Databases like MySQL
	 * which support this feature will return a result, others will
	 * return null.
	 *
	 * @return SQL expression for most recently inserted database key.
	 */
	public String getSequenceCurrentSql(String schemaName, String name) {
		return "SELECT last_value from "+ name;
	}

    /**
     * ??? sequence
     * @param schemaName
     * @param name
     * @return
     */
    public String getSequenceIncrementSql(String schemaName, String name) {
        return "SELECT increment_by FROM " + qi(name);
    }


	/**
	 * Returns the next auto-incremented key.
	 * <BR/>The number returned by the call nextval('seqname')
	 * for the specified sequence in the current session.
	 * @return SQL statment for most recently inserted database key.
	 */
    @Override
	public Long getSequenceNextValue(DBConnection connection, String schemaName, String name) {
		return connection.queryForLong( String.format(getSequenceNextSql(schemaName,name)) );
	}
	
	private String getSequenceNextSql(String schemaName, String name) {
		return "SELECT nextval('" + name + "')";
	}


	/**
	 * ?????????? SQL ??????? ????????????? ??????? ???????? sequence ? ???????? ????????
	 * @param name ??? sequence
	 * @param value ????? ???????? sequence
     * @param increment
	 * @return
	 */
	public List<String> getSetValueSequenceSql(String name, long value, int increment ) {
        LinkedList<String> sql = new LinkedList<String>();
        sql.addAll(getDropSequenceSql(name));
        sql.addAll(getCreateSequenceSql(name,value,increment));
        return sql;
	}

	public List<String> getCreateSequenceSql(String name, long startValue, int increment) {
		return Arrays.asList(new String[] {"CREATE SEQUENCE " + qi(name) + " INCREMENT " + Integer.toString(increment) + " START " + Long.toString(startValue)});
	}

	public List<String> getDropSequenceSql(String name) {
		return Arrays.asList(new String[] {"DROP SEQUENCE " + qi(name)});
	}

	public String getDropViewSql(String name) {
		return "DROP VIEW " + qi(name);
	}

	public boolean isPStatmentToString() {
		return true;
	}

	public String getExistentDatabaseSql() {
		return "SELECT datname FROM pg_database";
	}

	public String getCreateDatabaseSql(String name) {
		return "CREATE DATABASE " + qi(name);
	}

    public String getDropDatabaseSql(String name) {
        return "DROP DATABASE " + qi(name);
    }

    public String getDropStoredProcedureSql(String name) {
		return "DROP FUNCTION " + qi(name);
	}

	public String getDropIndexSql(String tableName, String indexName) {
		return "DROP INDEX " + qi(indexName);
	}

	public String getCreateIndexSql(String tableName, String indexName, List<String> columnNames, boolean unique) {
		String sql = "CREATE ";
		if (unique)
			sql += "UNIQUE ";
		sql += "INDEX " + qi(indexName) + " ON " + qi(tableName) + " (";
		for (Iterator<String> i = columnNames.iterator(); i.hasNext();) {
			sql += qi((String) i.next());
			if (i.hasNext())
				sql += ",";
		}
		sql += ")";
		return sql;
	}

	public String getTableAddColumnsSql(String tableName, String columnName, String columnDefinition) {
		return "ALTER TABLE " + qi(tableName) + " ADD COLUMN " + qi(columnName) + " " + columnDefinition;
	}

	public String getTableDropColumnsSql(String tableName, Collection<String> columnNames, Collection<String> columnNamesToDrop) {
		String sql = "";
		String columns = "";
		// collect columns for stay in table
		for (String columnName : columnNames) {
			if (!columnNamesToDrop.contains(columnName)) {
				columns += qi(columnName) + ",";
			}
		}
		if (columns.endsWith(","))
			columns = columns.substring(0, columns.length() - 1); // delete last ','

		sql += "CREATE TABLE temp0001 AS SELECT " + columns + " FROM " + qi(tableName) + ";\n";
		sql += "DROP TABLE " + qi(tableName) + ";\n";
		sql += "ALTER TABLE temp0001 RENAME TO " + qi(tableName);
		return sql;
	}

	public SqlActionList getTableSyncTypeColumnsActions(DBConnection conn, String schema, ConfigObject o) throws SQLException, DBConfigException, DBAdapterException {
		List<String> result = new ArrayList<String>();
		String tableName = o.getName();
		Map<String,ColumnInfo> attrTypesInTable = getTableColumns(conn, schema, tableName);
		Map<String,String> attrNewTypes = new HashMap<String,String>();
		for (ObjectAttr attr : o.getAttributes()) {
			String attrName = attr.getName();
			ColumnInfo columnInfo = (ColumnInfo) attrTypesInTable.get(attrName);
			String sqlTypeInTable = getSQLType(columnInfo);
			String sqlTypeInConfig = getSQLType(attr);
			if (!sqlTypeInConfig.equals(sqlTypeInTable)) {
				logger.info("Table " + o.getName() + ": column " + attrName + " wrong type " +
				             sqlTypeInConfig + "<>" + sqlTypeInTable + ", will be synchronized...");
				attrNewTypes.put(attrName, sqlTypeInConfig);
			}
		}

		if (attrNewTypes.size() == 0) {
			return new SqlActionList();
		}

		HashMap<String,String> tempColumns = new HashMap<String,String>(); // maping temp names to original
		List<String> existsColumnNames = new ArrayList<String>(o.getAttributeNames());
		String sql = "";

		// add temp columns
		for (Iterator<String> e = attrNewTypes.keySet().iterator(); e.hasNext();) {
			String columnName = e.next();
			String columnType = (String) attrNewTypes.get(columnName);
			String columnTempName = "temp_" + columnName;
			tempColumns.put(columnTempName, columnName);
			existsColumnNames.add(columnTempName);
			sql += getTableAddColumnsSql(tableName, columnTempName, columnType) + ";\n";
		}

		// copy data to temp columns
		String setColumns = "";
		String renameColumnsSql = "";
		for (Iterator<String> e = tempColumns.keySet().iterator(); e.hasNext();) {
			String columnTempName = (String) e.next();
			String columnName = (String) tempColumns.get(columnTempName);
			String columnType = (String) attrNewTypes.get(columnName);
			String columnOldType = ((ColumnInfo) attrTypesInTable.get(columnName)).getTypeName();

			if (columnOldType.startsWith("int") &&
			        (columnType.startsWith("timestamp") || columnType.startsWith("date"))) {
				setColumns += qi(columnTempName) + "=unixtime2timestamp(" + qi(columnName) + ")";
			}
			else {
				setColumns += qi(columnTempName) + "=text(" + qi(columnName) + ")::" + columnType;
			}

			renameColumnsSql += "ALTER TABLE " + qi(tableName) + " RENAME COLUMN " + qi(columnTempName) + " TO " + qi(columnName);
			if (e.hasNext()) {
				setColumns += ", ";
			}
			renameColumnsSql += ";\n";
		}
		sql += "UPDATE " + qi(tableName) + " SET " + setColumns + ";\n";

		// drop bad type columns
		List<String> columnNamesToDrop = new ArrayList<String>(tempColumns.values());
		sql += getTableDropColumnsSql(tableName, existsColumnNames, columnNamesToDrop) + ";\n";

		// rename temp columns to orig names
		sql += renameColumnsSql;
        result.add(sql);
		return new SqlActionList(result);
	}

	public List<String> getOnCreateDatabaseSql(String dbname) {
		String sql3 = "";

		sql3 += "CREATE OR REPLACE FUNCTION mz_unixtime2timestamp(integer) RETURNS timestamp AS '\n";
		sql3 += "  DECLARE\n";
		sql3 += "		unixtime ALIAS FOR $1;\n";
		sql3 += "		stat TEXT;\n";
		sql3 += "		rec RECORD;\n";
		sql3 += "		result timestamp;\n";
		sql3 += "  BEGIN\n";
		sql3 += "  	IF unixtime IS NULL THEN\n";
		sql3 += " 	 	RETURN NULL;\n";
		sql3 += "  	END IF;\n";
		sql3 += "		stat = ''SELECT timestamp with time zone ''''1970-01-01 00:00:00-00'''' + interval ''''''||unixtime||''s'''' as t'';\n";
		sql3 += "		FOR rec IN EXECUTE stat LOOP\n";
		sql3 += "			result:=rec.t;\n";
		sql3 += "		END LOOP;\n";
		sql3 += "	RETURN result;\n";
		sql3 += "	END;\n";
		sql3 += "	' LANGUAGE 'plpgsql';\n";

		List<String> sql = new LinkedList<String>();
		sql.add(sql3);
		return sql;
	}

	public String getDropTriggerSql(String tableName, String triggerName) {
		return "DROP TRIGGER " + qi(triggerName) + " ON " + qi(tableName);
	}

	public String getTriggerName(String triggerStatment) {
		return getViewNameFromCreateSql(triggerStatment); // get third word
	}

	public Trigger getCheckForeignTrigger(ConfigObject co) throws DBAdapterException {
		if (!co.isTable()) {
			return null;
		}
		String sql = "";
		try {
			for (ObjectAttr attr : co.getForeignAttrs() ) {
				ConfigObject foreignTable = attr.getForeignObject();
				sql += "    SELECT id INTO mz_tmp_id FROM " + foreignTable.getSQLName() + " WHERE id=NEW." + attr.getSQLName() + "; \n";
				sql += "    IF NOT FOUND THEN\n";
                sql += "        RAISE EXCEPTION ''Foreign key \"" + co.getSQLName() + "_" + attr.getSQLName() + "\" not found in \"" + foreignTable.getSQLName() + "\"'';\n";
				sql += "    END IF;\n";
			}
			Set<PersistentEvent> events = new HashSet<PersistentEvent>();
			events.add( PersistentEvent.BEFORE_INSERT /*new Event(Action.INSERT, When.BEFORE)*/);
			events.add( PersistentEvent.BEFORE_UPDATE /*new Event(Action.UPDATE, When.BEFORE)*/);
			Set<VariableInfo> variables = new HashSet<VariableInfo>();
			variables.add(new VariableInfo("mz_tmp_id", (co.getDBConfig().isStringId()) ? "VARCHAR" : "BIGINT"));
			return new Trigger("check_foreign", getName(), sql, events, variables, true, true, co);
		}
		catch (DBConfigException e) {
			throw new DBAdapterException(e);
		}
	}


	public Trigger getOnDeleteForeignTrigger(ConfigObject co, Map<String,String> foreignTableNames) {
		if (!co.isTable()) {
			return null;
		}
		String sql = "";
		for (String foreignTableName : foreignTableNames.keySet()) {
			String attr = foreignTableNames.get(foreignTableName);
			sql += "    DELETE FROM " + qi(foreignTableName) + " WHERE " + qi(attr) + "=OLD.id;\n";
		}
		Set<PersistentEvent> events = new HashSet<PersistentEvent>();
		events.add( PersistentEvent.AFTER_DELETE /* new Event(Action.DELETE, When.AFTER) */);
		Set<VariableInfo> variables = new HashSet<VariableInfo>();
		return new Trigger("delete_foreign", getName(), sql, events, variables, true, true, co);
	}

	public Trigger getCheckRequiredsTrigger(ConfigObject co) {
		if (!co.isTable()) {
			return null;
		}
		String sql = "";
		for (ObjectAttr attr : co.getNotNullableAttrs()) {
			String attrName = attr.getName();
			if (attrName.equals("id"))
				continue; // skip id, it set by another trigger allways
			if (attr.isString()) { // if string is not nullable => lenght>0 (required)
				sql += "    IF NEW." + qi(attrName) + " ISNULL OR char_length(NEW." + qi(attrName) + ")=0 THEN\n";
			}
			else if (attr.isBoolean() && attr.isRequired()) {
				// boolean type is integer 0|1; required for boolean is TRUE
				sql += "    IF NEW." + qi(attrName) + " ISNULL OR NEW." + qi(attrName) + "=0 THEN\n";
			}
			else {
				sql += "    IF NEW." + qi(attrName) + " ISNULL THEN\n";
			}
			sql += "        RAISE EXCEPTION ''Attribute " + attrName + " is required for table " + qi(co.getName()) + "'';\n";
			sql += "    END IF;\n";
		}
		if (sql.length() == 0) {
			return null;
		}
		Set<PersistentEvent> events = new HashSet<PersistentEvent>();
		events.add( PersistentEvent.BEFORE_INSERT );
		events.add( PersistentEvent.BEFORE_UPDATE );
		return new Trigger("check_required", getName(), sql, events, new HashSet<VariableInfo>(), true, true, co);
	}

	public Trigger getDefaultValueTrigger(ConfigObject co) {
		if (!co.isTable()) {
			return null;
		}
		String sql = "";
		for (ObjectAttr a : co.getAttributes()) {
			String value = a.getDefaultValue();
			if (value == null) {
				continue;
			}
			else {
				// ??????????? ???????? ? SQL
				if (a.isBoolean()) {
					try {
						value = MCast.toBoolean(value).booleanValue() ? "1" : "0";
					}
					catch (Exception e) {
						logger.fatal("In create trigger for default values: " + e.getMessage());
					}
				}
				else {
					if (a.isString() || a.isDateTime()) {
						value = "''" + value + "''";
					}
				}
				sql += "    IF NEW." + qi(a.getName()) + " ISNULL THEN\n";
				sql += "        NEW." + qi(a.getName()) + " := " + value + ";\n";
				sql += "    END IF;\n";
			}
		}
		if (sql.length() == 0) {
			return null;
		}
		Set<PersistentEvent> events = new HashSet<PersistentEvent>();
		events.add( PersistentEvent.BEFORE_INSERT );
		return new Trigger("set_default", getName(), sql, events, new HashSet<VariableInfo>(), true, true, co);
	}

	public Trigger getReadonlyAttributesTrigger(ConfigObject co) {
		if (!co.isTable()) {
			return null;
		}
		String sql = "";
		for (ObjectAttr a : co.getAttributes()) {
			if (a.isReadonly()) {
				sql += "    NEW." + qi(a.getName()) + " := OLD." + qi(a.getName()) + ";\n";
			}
		}
		if (sql.length() == 0) {
			return null;
		}
		return new Trigger("readonly", getName(), sql, 
				Collections.singleton(PersistentEvent.BEFORE_UPDATE), new HashSet<VariableInfo>(), true, true, co);
	}

	/**
	 * create trigger statment for insert tree object type.
	 * Extended stored procedure defined in <code>etc/pg-sys-objects.xml</code>
	 * @param co
	 * @return SQL statment for create trigger.
	 */
	public Trigger getOnInsertTreeTrigger(ConfigObject co) {
		if (!co.isTable()) {
			return null;
		}
		String sql = "";
		sql += "	IF NEW.tree IS NOT NULL THEN\n";
		sql += "		PERFORM check_tree_id(TG_RELNAME, NEW.id, NEW.tree);\n";
		sql += "		PERFORM ins_to_extree(TG_RELNAME, NEW.id, NEW.tree);\n";
		sql += "	END IF;\n";
		return new Trigger("insert_tree", getName(), sql, 
				Collections.singleton(PersistentEvent.AFTER_INSERT), new HashSet<VariableInfo>(), true, true, co);
	}

	/**
	 * create trigger statment for update tree object type
	 * Extended stored procedure defined in <code>etc/pg-sys-objects.xml</code>
	 * @param co
	 * @return SQL statment for create trigger.
	 */
	public Trigger getOnUpdateTreeTrigger(ConfigObject co) {
		if (!co.isTable()) {
			return null;
		}
		//Extended stored procedure defined in <code>etc/pg-sys-objects.xml</code>
		String sql = "";
		sql += "	IF NEW.tree IS NULL THEN\n";
		sql += "		IF OLD.tree IS NOT NULL THEN\n";
		sql += "			PERFORM del_from_extree(TG_RELNAME, OLD.id);\n";
		sql += "		END IF;	\n";
		sql += "	ELSE\n";
		sql += "		IF OLD.tree IS NOT NULL THEN\n";
		sql += "			/* old.tree!=null && new.tree!=null */\n";
		sql += "			IF OLD.tree<>NEW.tree THEN\n";
		sql += "				PERFORM check_tree_id(TG_RELNAME, OLD.id, NEW.tree);\n";
		sql += "				PERFORM del_from_extree(TG_RELNAME, OLD.id);\n";
		sql += "				PERFORM ins_to_extree(TG_RELNAME, OLD.id, NEW.tree);\n";
		sql += "			END IF;	\n";
		sql += "		ELSE\n";
		sql += "			/* old.tree==null && new.tree!=null */\n";
		sql += "			PERFORM check_tree_id(TG_RELNAME, OLD.id, NEW.tree);\n";
		sql += "			PERFORM ins_to_extree(TG_RELNAME, OLD.id, NEW.tree);\n";
		sql += "		END IF; \n";
		sql += "	END IF;\n";
		Set<PersistentEvent> events = new HashSet<PersistentEvent>();
		events.add(PersistentEvent.BEFORE_UPDATE);
		return new Trigger("update_tree", getName(), sql, events, new HashSet<VariableInfo>(), true, true, co);
	}

	/**
	 * create trigger statment for update tree object type
	 * Extended stored procedure defined in <code>etc/pg-sys-objects.xml</code>
	 * @param co
	 * @return SQL statment for create trigger.
	 */
	public Trigger getOnDeleteTreeTrigger(ConfigObject co) {
		if (!co.isTable()) {
			return null;
		}
		/*
		  ALTER TABLE treeobj ADD FOREIGN KEY (tree) REFERENCES treeobj (id) ON DELETE CASCADE;
		 */
		//Extended stored procedure defined in <code>etc/pg-sys-objects.xml</code>
		String sql = "";
		sql += "	/* delete ancestors from extended table*/\n";
		sql += "	IF OLD.tree IS NOT NULL THEN\n";
		sql += "		PERFORM del_from_extree(TG_RELNAME, OLD.id);\n";
		sql += "	END IF;\n";
		sql += "	/* delete descendants for this node */\n";
		sql += "	mz_stat = ''DELETE FROM ''||TG_RELNAME||'' WHERE tree=''||OLD.id;\n";
		sql += "	EXECUTE mz_stat;\n";
		Set<PersistentEvent> events = new HashSet<PersistentEvent>();
		events.add(PersistentEvent.BEFORE_DELETE);
		Set<VariableInfo> variables = new HashSet<VariableInfo>();
		variables.add(new VariableInfo("mz_stat", "TEXT"));
		return new Trigger("delete_tree", getName(), sql, events, variables, true, true, co);
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
        String sql = "";
        sql += "    NEW."+qi(ConfigParser.VERSION_ATTR_ID)+"=''1:"+repositoryId+":"+co.getDBConfig().getDatabaseName()+"'';\n";
        Set<PersistentEvent> events = new HashSet<PersistentEvent>();
        events.add(PersistentEvent.BEFORE_INSERT);
        return new Trigger("insert_version", getName(), sql, events, null, true, true, co);
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
        Set<VariableInfo> variables = new HashSet<VariableInfo>();
        variables.add(new VariableInfo("mz_version", "INTEGER"));
        String sql = "";
        sql += "    mz_version = substring(OLD."+qi(ConfigParser.VERSION_ATTR_ID)+" from ''^[0-9]+'');\n";
        sql += "    IF mz_version ISNULL THEN\n";
        sql += "        NEW."+qi(ConfigParser.VERSION_ATTR_ID)+"=''1:"+repositoryId+":"+co.getDBConfig().getDatabaseName()+"'';\n";
        sql += "    ELSE\n";
        sql += "        mz_version = mz_version + 1;\n";
        sql += "        NEW."+qi(ConfigParser.VERSION_ATTR_ID)+"=mz_version||'':"+repositoryId+":"+co.getDBConfig().getDatabaseName()+"'';\n";
        sql += "    END IF;\n";

        Set<PersistentEvent> events = new HashSet<PersistentEvent>();
        events.add(PersistentEvent.BEFORE_UPDATE);
        return new Trigger("update_version", getName(), sql, events, variables, true, true, co);
    }


	public List<String> getSystemStoredProcedureSql(String schema) {
		List<String> sqls = new LinkedList<String>();
		String sql;
		// function for convert bool type to text (not realized in 7.1.2)
		sql = "CREATE OR REPLACE FUNCTION text(bool) RETURNS text AS '\n";
		sql += "SELECT CASE WHEN $1 THEN ''1'' ELSE ''0'' END;' LANGUAGE 'sql';\n";
		sqls.add(sql);
		// function for convert text to bol (not realized in 7.1.2)
		sql = "CREATE OR REPLACE FUNCTION bool(text) RETURNS bool AS '\n";
		sql += "DECLARE \n";
		sql += " val text; \n";
		sql += "BEGIN \n";
		sql += " val = lower($1); \n";
		sql += " IF val=''true'' OR val=''1'' OR val=''yes'' OR val=''y'' OR val=''t'' THEN\n";
		sql += "  RETURN true;\n";
		sql += " END IF;\n";
		sql += " RETURN false;\n";
		sql += "END; \n";
		sql += "' LANGUAGE 'plpgsql'; \n";
		sqls.add(sql);
		// 2 functions for convert numeric type to text (not realized in 7.1.2)
//		sql = "CREATE FUNCTION numericout(numeric) RETURNS float8 AS \n";
//		sql += "'numeric_out' LANGUAGE 'internal';\n";
//		sqls.add(sql);
//		sql = "CREATE FUNCTION text(numeric) RETURNS text AS \n";
//		sql += "'SELECT textin(numericout($1));' LANGUAGE 'sql';\n";
//		sqls.add(sql);
		// revoke all privilege on object from all groups
		// boolean - delete from group if true
		// text - user name or group name
		// text - privilege action
//		sql = "CREATE OR REPLACE FUNCTION revoke_from_all(boolean,text,text) RETURNS bool AS '\n";
//		sql += "DECLARE \n";
//		sql += " tablerec RECORD;\n";
//		sql += " obj_name VARCHAR;\n";
//		sql += " ug_name VARCHAR;\n";
//		sql += " priv_str VARCHAR;\n";
//		sql += " quote_chr VARCHAR;\n";
//		sql += "BEGIN \n";
//		sql += " obj_name := ''\"'' || $2 || ''\"''; \n";
//		sql += " priv_str := lower($3); \n";
//		sql += " IF $1 THEN \n";
//		sql += "   FOR tablerec IN SELECT groname FROM pg_group LOOP\n";
//		sql += "    ug_name := ''\"'' || tablerec.groname || ''\"''; \n";
//		sql += "	   EXECUTE ''REVOKE ''||priv_str||'' ON ''||obj_name||'' FROM GROUP ''||ug_name;\n";
//		sql += "   END LOOP;\n";
//		sql += " ELSE \n";
//		sql += "   FOR tablerec IN SELECT usename FROM pg_user LOOP\n";
//		sql += "    ug_name := ''\"'' || tablerec.usename || ''\"''; \n";
//		sql += "	   EXECUTE ''REVOKE ''||priv_str||'' ON ''||obj_name||'' FROM ''||ug_name;\n";
//		sql += "   END LOOP;\n";
//		sql += " END IF; \n";
//		sql += " RETURN true;\n";
//		sql += "END; \n";
//		sql += "' LANGUAGE 'plpgsql'; \n";
//		sqls.add(sql);

		final String idType = dbtTypes.get(DBT_LONG);
		// ???????? ??????? ????????????? ??????? ??? ???? tree
		// check_tree_id(TG_RELNAME, NEW.id, NEW.tree);
		sql = "CREATE OR REPLACE FUNCTION check_tree_id(text,"+idType+","+idType+") RETURNS boolean AS '\n";
		sql += "  DECLARE\n";
		sql += "		rel_name ALIAS FOR $1;\n";
		sql += "		obj_id ALIAS FOR $2;\n";
		sql += "		tree_id ALIAS FOR $3;\n";
		sql += "		ex_tree TEXT;\n";
		sql += "		id_exists int;\n";
		sql += "  BEGIN    \n";
		sql += "		/* check that tree_id != obj_id */\n";
		sql += "	  IF obj_id=tree_id THEN\n";
		sql += "	 		RAISE EXCEPTION ''Parent tree key % for % is equal for id, cycle.'',tree_id, rel_name;\n";
		sql += "	  END IF;\n";
		sql += "		/* check that tree_id not exists in descendants */\n";
		sql += "		ex_tree = ex_tree_name(rel_name);\n";
		sql += "		EXECUTE ''SELECT id FROM ''||ex_tree||'' WHERE id=''||tree_id||'' AND ancestor=''||obj_id;\n";
		sql += "		GET DIAGNOSTICS id_exists = ROW_COUNT;\n";
		sql += "	  IF id_exists>0 THEN\n";
		sql += "	 		RAISE EXCEPTION ''Can not set tree to % in %, found cycle'',tree_id, rel_name;\n";
		sql += "	  END IF;\n";
		sql += "		/* check that tree_id exists */\n";
		sql += "		EXECUTE ''SELECT id FROM ''||rel_name||'' WHERE id=''||tree_id;\n";
		sql += "		GET DIAGNOSTICS id_exists = ROW_COUNT;\n";
		sql += "	  IF id_exists=0 THEN\n";
		sql += "	 		RAISE EXCEPTION ''Parent tree key % for % not found'',tree_id, rel_name;\n";
		sql += "	  END IF;\n";
		sql += "		RETURN true;\n";
		sql += "	END; \n";
		sql += "	' LANGUAGE 'plpgsql';\n";
		sqls.add(sql);

		sql = "CREATE OR REPLACE FUNCTION check_tree_id(text,varchar,varchar) RETURNS boolean AS '\n";
		sql += "  DECLARE\n";
		sql += "		rel_name ALIAS FOR $1;\n";
		sql += "		obj_id ALIAS FOR $2;\n";
		sql += "		tree_id ALIAS FOR $3;\n";
		sql += "		ex_tree TEXT;\n";
		sql += "		id_exists int;\n";
		sql += "  BEGIN    \n";
		sql += "		/* check that tree_id != obj_id */\n";
		sql += "	  IF obj_id=tree_id THEN\n";
		sql += "	 		RAISE EXCEPTION ''Parent tree key % for % is equal for id, cycle.'',tree_id, rel_name;\n";
		sql += "	  END IF;\n";
		sql += "		/* check that tree_id not exists in descendants */\n";
		sql += "		ex_tree = ex_tree_name(rel_name);\n";
		sql += "		EXECUTE ''SELECT id FROM ''||ex_tree||'' WHERE id=''||tree_id||'' AND ancestor=''||obj_id;\n";
		sql += "		GET DIAGNOSTICS id_exists = ROW_COUNT;\n";
		sql += "	  IF id_exists>0 THEN\n";
		sql += "	 		RAISE EXCEPTION ''Can not set tree to % in %, found cycle'',tree_id, rel_name;\n";
		sql += "	  END IF;\n";
		sql += "		/* check that tree_id exists */\n";
		sql += "		EXECUTE ''SELECT id FROM ''||rel_name||'' WHERE id=''||tree_id;\n";
		sql += "		GET DIAGNOSTICS id_exists = ROW_COUNT;\n";
		sql += "	  IF id_exists=0 THEN\n";
		sql += "	 		RAISE EXCEPTION ''Parent tree key % for % not found'',tree_id, rel_name;\n";
		sql += "	  END IF;\n";
		sql += "		RETURN true;\n";
		sql += "	END; \n";
		sql += "	' LANGUAGE 'plpgsql';\n";
		sqls.add(sql);

// ???????? ancestors ?? ??????????????? ??????? ??? ??????? ???? tree
// del_from_extree(TG_RELNAME, OLD.id);
		
		sql = "CREATE OR REPLACE FUNCTION del_from_extree(text,"+idType+") RETURNS boolean AS '\n";
		sql += "  DECLARE\n";
		sql += "		rel_name ALIAS FOR $1;\n";
		sql += "		obj_id ALIAS FOR $2;\n";
		sql += "		ex_tree TEXT;\n";
		sql += "		stat TEXT;\n";
		sql += "  BEGIN    \n";
		sql += "		ex_tree = ex_tree_name(rel_name);\n";
		sql += "		/* delete ancestors for descendant */\n";
		sql += "		stat = ''DELETE FROM ''||ex_tree||'' WHERE'';\n";
		sql += "		stat = stat||'' ancestor IN (SELECT ancestor FROM ''||ex_tree||'' WHERE id=''||obj_id||'')'';\n";
		sql += "		stat = stat||'' AND '';\n";
		sql += "		stat = stat||'' id IN ( SELECT id FROM ''||ex_tree||'' WHERE ancestor=''||obj_id||'')'';\n";
		sql += "		EXECUTE stat;\n";
		sql += "		/* delete ancestors for self */\n";
		sql += "		stat = ''DELETE FROM ''||ex_tree||'' WHERE id=''||obj_id;\n";
		sql += "		EXECUTE stat;\n";
		sql += "		RETURN true;\n";
		sql += "	END; \n";
		sql += "	' LANGUAGE 'plpgsql';\n";
		sqls.add(sql);

		sql = "CREATE OR REPLACE FUNCTION del_from_extree(text,varchar) RETURNS boolean AS '\n";
		sql += "  DECLARE\n";
		sql += "		rel_name ALIAS FOR $1;\n";
		sql += "		obj_id ALIAS FOR $2;\n";
		sql += "		ex_tree TEXT;\n";
		sql += "		stat TEXT;\n";
		sql += "  BEGIN    \n";
		sql += "		ex_tree = ex_tree_name(rel_name);\n";
		sql += "		/* delete ancestors for descendant */\n";
		sql += "		stat = ''DELETE FROM ''||ex_tree||'' WHERE'';\n";
		sql += "		stat = stat||'' ancestor IN (SELECT ancestor FROM ''||ex_tree||'' WHERE id=''||obj_id||'')'';\n";
		sql += "		stat = stat||'' AND '';\n";
		sql += "		stat = stat||'' id IN ( SELECT id FROM ''||ex_tree||'' WHERE ancestor=''||obj_id||'')'';\n";
		sql += "		EXECUTE stat;\n";
		sql += "		/* delete ancestors for self */\n";
		sql += "		stat = ''DELETE FROM ''||ex_tree||'' WHERE id=''||obj_id;\n";
		sql += "		EXECUTE stat;\n";
		sql += "		RETURN true;\n";
		sql += "	END; \n";
		sql += "	' LANGUAGE 'plpgsql';\n";
		sqls.add(sql);

// ???????  ancestors ? ??????????????? ??????? ??? ??????? ???? tree
// ins_to_extree(TG_RELNAME, NEW.id, NEW.tree)
		sql = "CREATE OR REPLACE FUNCTION ins_to_extree(text,"+idType+","+idType+") RETURNS boolean AS '\n";
		sql += "  DECLARE\n";
		sql += "		rel_name ALIAS FOR $1;\n";
		sql += "		obj_id ALIAS FOR $2;\n";
		sql += "		obj_tree ALIAS FOR $3;\n";
		sql += "		stat TEXT;\n";
		sql += "		ex_tree TEXT;\n";
		sql += "  BEGIN    \n";
		sql += "		IF obj_tree IS NOT NULL THEN\n";
		sql += "			ex_tree = ex_tree_name(rel_name);\n";
		sql += "			/* insert ancestors from parent for self */\n";
		sql += "			stat = ''INSERT INTO ''||ex_tree||'' (ancestor,id) '';\n";
		sql += "			stat = stat || ''SELECT ancestor,''||obj_id||'' FROM ''||ex_tree||'' WHERE id=''||obj_tree;\n";
		sql += "			EXECUTE stat; \n";
		sql += "			/* insert ancestor for self */\n";
		sql += "			stat = ''INSERT INTO ''||ex_tree||'' (ancestor,id) VALUES (''||obj_tree||'',''||obj_id||'')'';\n";
		sql += "			EXECUTE stat; \n";
		sql += "			/* insert ancestors for descendant */\n";
		sql += "			stat = ''INSERT INTO ''||ex_tree||'' (ancestor,id)'';\n";
		sql += "			stat = stat||'' SELECT a.ancestor, i.id FROM'';\n";
		sql += "			stat = stat||''  ''||ex_tree||'' a,'';\n";
		sql += "			stat = stat||''  ( SELECT id FROM ''||ex_tree||'' WHERE ancestor=''||obj_id||'' ) i '';\n";
		sql += "			stat = stat||'' WHERE a.id=''||obj_id;\n";
		sql += "			EXECUTE stat; \n";
		sql += "		END IF;\n";
		sql += "		RETURN true;\n";
		sql += "	END; \n";
		sql += "	' LANGUAGE 'plpgsql';\n";
		sqls.add(sql);

		sql = "CREATE OR REPLACE FUNCTION ins_to_extree(text,varchar,varchar) RETURNS boolean AS '\n";
		sql += "  DECLARE\n";
		sql += "		rel_name ALIAS FOR $1;\n";
		sql += "		obj_id ALIAS FOR $2;\n";
		sql += "		obj_tree ALIAS FOR $3;\n";
		sql += "		stat TEXT;\n";
		sql += "		ex_tree TEXT;\n";
		sql += "  BEGIN    \n";
		sql += "		IF obj_tree IS NOT NULL THEN\n";
		sql += "			ex_tree = ex_tree_name(rel_name);\n";
		sql += "			/* insert ancestors from parent for self */\n";
		sql += "			stat = ''INSERT INTO ''||ex_tree||'' (ancestor,id) '';\n";
		sql += "			stat = stat || ''SELECT ancestor,''||obj_id||'' FROM ''||ex_tree||'' WHERE id=''||obj_tree;\n";
		sql += "			EXECUTE stat; \n";
		sql += "			/* insert ancestor for self */\n";
		sql += "			stat = ''INSERT INTO ''||ex_tree||'' (ancestor,id) VALUES (''||obj_tree||'',''||obj_id||'')'';\n";
		sql += "			EXECUTE stat; \n";
		sql += "			/* insert ancestors for descendant */\n";
		sql += "			stat = ''INSERT INTO ''||ex_tree||'' (ancestor,id)'';\n";
		sql += "			stat = stat||'' SELECT a.ancestor, i.id FROM'';\n";
		sql += "			stat = stat||''  ''||ex_tree||'' a,'';\n";
		sql += "			stat = stat||''  ( SELECT id FROM ''||ex_tree||'' WHERE ancestor=''||obj_id||'' ) i '';\n";
		sql += "			stat = stat||'' WHERE a.id=''||obj_id;\n";
		sql += "			EXECUTE stat; \n";
		sql += "		END IF;\n";
		sql += "		RETURN true;\n";
		sql += "	END; \n";
		sql += "	' LANGUAGE 'plpgsql';\n";
		sqls.add(sql);

// ?????? ??? ??????????? ??????? ??? ??????? ???? tree
		sql = "CREATE OR REPLACE FUNCTION ex_tree_name(varchar) RETURNS varchar AS '\n";
		sql += "  DECLARE\n";
		sql += "		tree_name ALIAS FOR $1;\n";
		sql += "  BEGIN    \n";
		sql += "	RETURN tree_name||''_tree'';\n";
		sql += "	END; \n";
		sql += "	' LANGUAGE 'plpgsql';\n";
		sqls.add(sql);

// ???????? ?? id what ????????? ??? id forTreeId
// is_sibling(what,'tablename',forTreeId)
//  ??? 'tablename' - ??? ??????? tree
		sql = "CREATE OR REPLACE FUNCTION is_sibling("+idType+",varchar,"+idType+") RETURNS int AS '\n";
		sql += "  DECLARE\n";
		sql += "		what_id ALIAS FOR $1;\n";
		sql += "		tree_table ALIAS FOR $2;\n";
		sql += "		for_id ALIAS FOR $3;\n";
		sql += "		rows integer;\n";
		sql += "		result boolean;\n";
		sql += "		stat text;\n";
		sql += "  BEGIN    \n";
		sql += "		stat = ''SELECT id FROM ''||tree_table||'' WHERE id=''||what_id;\n";
		sql += "		stat = stat||'' AND COALESCE(tree,-1) IN ( SELECT COALESCE(tree,-1) FROM ''||tree_table||'' WHERE id=''||for_id||'')'';\n";
		sql += "		EXECUTE stat;\n";
		sql += "		GET DIAGNOSTICS rows = ROW_COUNT;\n";
		sql += "		IF rows>0 THEN\n";
		sql += "			RETURN 1;\n";
		sql += "		END IF;\n";
		sql += "	RETURN 0;\n";
		sql += "	END; \n";
		sql += "	' LANGUAGE 'plpgsql';\n";
		sqls.add(sql);
		sql = "CREATE OR REPLACE FUNCTION is_sibling(varchar,varchar,varchar) RETURNS int AS '\n";
		sql += "  DECLARE\n";
		sql += "		what_id ALIAS FOR $1;\n";
		sql += "		tree_table ALIAS FOR $2;\n";
		sql += "		for_id ALIAS FOR $3;\n";
		sql += "		rows integer;\n";
		sql += "		result boolean;\n";
		sql += "		stat text;\n";
		sql += "  BEGIN    \n";
		sql += "		stat = ''SELECT id FROM ''||tree_table||'' WHERE id=''||what_id;\n";
		sql += "		stat = stat||'' AND COALESCE(tree,-1) IN ( SELECT COALESCE(tree,-1) FROM ''||tree_table||'' WHERE id=''||for_id||'')'';\n";
		sql += "		EXECUTE stat;\n";
		sql += "		GET DIAGNOSTICS rows = ROW_COUNT;\n";
		sql += "		IF rows>0 THEN\n";
		sql += "			RETURN 1;\n";
		sql += "		END IF;\n";
		sql += "	RETURN 0;\n";
		sql += "	END; \n";
		sql += "	' LANGUAGE 'plpgsql';\n";
		sqls.add(sql);

// ???????? ?? id what ???????? ??? id forTreeId
// is_parent(what,'tablename',forTreeId)
//  ??? 'tablename' - ??? ??????? tree
		sql = "CREATE OR REPLACE FUNCTION is_parent("+idType+",varchar,"+idType+") RETURNS int AS '\n";
		sql += "  DECLARE\n";
		sql += "		what_id ALIAS FOR $1;\n";
		sql += "		tree_table ALIAS FOR $2;\n";
		sql += "		for_id ALIAS FOR $3;\n";
		sql += "		rows integer;\n";
		sql += "		result boolean;\n";
		sql += "		stat text;\n";
		sql += "  BEGIN    \n";
		sql += "		stat = ''SELECT id FROM ''||tree_table||'' WHERE id=''||what_id;\n";
		sql += "		stat = stat||'' AND id IN ( SELECT COALESCE(tree,-1) FROM ''||tree_table||'' WHERE id=''||for_id||'')'';\n";
		sql += "		EXECUTE stat;\n";
		sql += "		GET DIAGNOSTICS rows = ROW_COUNT;\n";
		sql += "		IF rows>0 THEN\n";
		sql += "			RETURN 1;\n";
		sql += "		END IF;\n";
		sql += "	RETURN 0;\n";
		sql += "	END; \n";
		sql += "	' LANGUAGE 'plpgsql';\n";
		sqls.add(sql);
		sql = "CREATE OR REPLACE FUNCTION is_parent(varchar,varchar,varchar) RETURNS int AS '\n";
		sql += "  DECLARE\n";
		sql += "		what_id ALIAS FOR $1;\n";
		sql += "		tree_table ALIAS FOR $2;\n";
		sql += "		for_id ALIAS FOR $3;\n";
		sql += "		rows integer;\n";
		sql += "		result boolean;\n";
		sql += "		stat text;\n";
		sql += "  BEGIN    \n";
		sql += "		stat = ''SELECT id FROM ''||tree_table||'' WHERE id=''||what_id;\n";
		sql += "		stat = stat||'' AND id IN ( SELECT COALESCE(tree,-1) FROM ''||tree_table||'' WHERE id=''||for_id||'')'';\n";
		sql += "		EXECUTE stat;\n";
		sql += "		GET DIAGNOSTICS rows = ROW_COUNT;\n";
		sql += "		IF rows>0 THEN\n";
		sql += "			RETURN 1;\n";
		sql += "		END IF;\n";
		sql += "	RETURN 0;\n";
		sql += "	END; \n";
		sql += "	' LANGUAGE 'plpgsql';\n";
		sqls.add(sql);

// ???????? ?? id what ????????? ??? id forTreeId
// is_child(what,'tablename',forTreeId)
// ??? 'tablename' - ??? ??????? tree
		sql = "CREATE OR REPLACE FUNCTION is_child("+idType+",varchar,"+idType+") RETURNS int AS '\n";
		sql += "  DECLARE\n";
		sql += "		what_id ALIAS FOR $1;\n";
		sql += "		tree_table ALIAS FOR $2;\n";
		sql += "		for_id ALIAS FOR $3;\n";
		sql += "		rows integer;\n";
		sql += "		result boolean;\n";
		sql += "		stat text;\n";
		sql += "  BEGIN    \n";
		sql += "		stat = ''SELECT id FROM ''||tree_table||'' WHERE id=''||what_id||'' AND tree=''||for_id;\n";
		sql += "		EXECUTE stat;\n";
		sql += "		GET DIAGNOSTICS rows = ROW_COUNT;\n";
		sql += "		IF rows>0 THEN\n";
		sql += "			RETURN 1;\n";
		sql += "		END IF;\n";
		sql += "	RETURN 0;\n";
		sql += "	END; \n";
		sql += "	' LANGUAGE 'plpgsql';\n";
		sqls.add(sql);
		sql = "CREATE OR REPLACE FUNCTION is_child(varchar,varchar,varchar) RETURNS int AS '\n";
		sql += "  DECLARE\n";
		sql += "		what_id ALIAS FOR $1;\n";
		sql += "		tree_table ALIAS FOR $2;\n";
		sql += "		for_id ALIAS FOR $3;\n";
		sql += "		rows integer;\n";
		sql += "		result boolean;\n";
		sql += "		stat text;\n";
		sql += "  BEGIN    \n";
		sql += "		stat = ''SELECT id FROM ''||tree_table||'' WHERE id=''||what_id||'' AND tree=''||for_id;\n";
		sql += "		EXECUTE stat;\n";
		sql += "		GET DIAGNOSTICS rows = ROW_COUNT;\n";
		sql += "		IF rows>0 THEN\n";
		sql += "			RETURN 1;\n";
		sql += "		END IF;\n";
		sql += "	RETURN 0;\n";
		sql += "	END; \n";
		sql += "	' LANGUAGE 'plpgsql';\n";
		sqls.add(sql);

// ???????? ?? id what ??????????? ??? id forTreeId
// is_ancestor(what,'tablename',forTreeId)
//  ??? 'tablename' - ??? ??????? tree
		sql = "CREATE OR REPLACE FUNCTION is_ancestor("+idType+",varchar,"+idType+") RETURNS int AS '\n";
		sql += "  DECLARE\n";
		sql += "		what_id ALIAS FOR $1;\n";
		sql += "		tree_table ALIAS FOR $2;\n";
		sql += "		for_id ALIAS FOR $3;\n";
		sql += "		rows integer;\n";
		sql += "		result boolean;\n";
		sql += "		stat text;\n";
		sql += "		ex_tree TEXT;\n";
		sql += "  BEGIN    \n";
		sql += "		ex_tree = ex_tree_name(tree_table);\n";
		sql += "		stat = ''SELECT id FROM ''||ex_tree||'' WHERE ancestor=''||what_id||'' AND id=''||for_id;\n";
		sql += "		EXECUTE stat;\n";
		sql += "		GET DIAGNOSTICS rows = ROW_COUNT;\n";
		sql += "		IF rows>0 THEN\n";
		sql += "			RETURN 1;\n";
		sql += "		END IF;\n";
		sql += "	RETURN 0;\n";
		sql += "	END; \n";
		sql += "	' LANGUAGE 'plpgsql';\n";
		sqls.add(sql);
		sql = "CREATE OR REPLACE FUNCTION is_ancestor(varchar,varchar,varchar) RETURNS int AS '\n";
		sql += "  DECLARE\n";
		sql += "		what_id ALIAS FOR $1;\n";
		sql += "		tree_table ALIAS FOR $2;\n";
		sql += "		for_id ALIAS FOR $3;\n";
		sql += "		rows integer;\n";
		sql += "		result boolean;\n";
		sql += "		stat text;\n";
		sql += "		ex_tree TEXT;\n";
		sql += "  BEGIN    \n";
		sql += "		ex_tree = ex_tree_name(tree_table);\n";
		sql += "		stat = ''SELECT id FROM ''||ex_tree||'' WHERE ancestor=''||what_id||'' AND id=''||for_id;\n";
		sql += "		EXECUTE stat;\n";
		sql += "		GET DIAGNOSTICS rows = ROW_COUNT;\n";
		sql += "		IF rows>0 THEN\n";
		sql += "			RETURN 1;\n";
		sql += "		END IF;\n";
		sql += "	RETURN 0;\n";
		sql += "	END; \n";
		sql += "	' LANGUAGE 'plpgsql';\n";
		sqls.add(sql);

// ???????? ?? id what ???????????? ??? id forTreeId
// is_descendant(what,'tablename',forTreeId)
//  ??? 'tablename' - ??? ??????? tree
		sql = "CREATE OR REPLACE FUNCTION is_descendant("+idType+",varchar,"+idType+") RETURNS int AS '\n";
		sql += "  DECLARE\n";
		sql += "		what_id ALIAS FOR $1;\n";
		sql += "		tree_table ALIAS FOR $2;\n";
		sql += "		for_id ALIAS FOR $3;\n";
		sql += "		rows integer;\n";
		sql += "		result boolean;\n";
		sql += "		stat text;\n";
		sql += "		ex_tree TEXT;\n";
		sql += "  BEGIN    \n";
		sql += "		ex_tree = ex_tree_name(tree_table);\n";
		sql += "		stat = ''SELECT id FROM ''||ex_tree||'' WHERE id=''||what_id||'' AND ancestor=''||for_id;\n";
		sql += "		EXECUTE stat;\n";
		sql += "		GET DIAGNOSTICS rows = ROW_COUNT;\n";
		sql += "		IF rows>0 THEN\n";
		sql += "			RETURN 1;\n";
		sql += "		END IF;\n";
		sql += "	RETURN 0;\n";
		sql += "	END; \n";
		sql += "	' LANGUAGE 'plpgsql';\n";
		sqls.add(sql);
		sql = "CREATE OR REPLACE FUNCTION is_descendant(varchar,varchar,varchar) RETURNS int AS '\n";
		sql += "  DECLARE\n";
		sql += "		what_id ALIAS FOR $1;\n";
		sql += "		tree_table ALIAS FOR $2;\n";
		sql += "		for_id ALIAS FOR $3;\n";
		sql += "		rows integer;\n";
		sql += "		result boolean;\n";
		sql += "		stat text;\n";
		sql += "		ex_tree TEXT;\n";
		sql += "  BEGIN    \n";
		sql += "		ex_tree = ex_tree_name(tree_table);\n";
		sql += "		stat = ''SELECT id FROM ''||ex_tree||'' WHERE id=''||what_id||'' AND ancestor=''||for_id;\n";
		sql += "		EXECUTE stat;\n";
		sql += "		GET DIAGNOSTICS rows = ROW_COUNT;\n";
		sql += "		IF rows>0 THEN\n";
		sql += "			RETURN 1;\n";
		sql += "		END IF;\n";
		sql += "	RETURN 0;\n";
		sql += "	END; \n";
		sql += "	' LANGUAGE 'plpgsql';\n";
		sqls.add(sql);

		return sqls;
	}

	public Trigger getSetIdTrigger(ConfigObject co) {
		if (!co.isTable()) {
			return null;
		}
		String sql = "";
		sql += "    IF TG_OP=''INSERT'' THEN\n";
		sql += "      IF NEW.id IS NULL THEN\n";
		sql += "        NEW.id := nextval(''" + getCommonSequenceName() + "''); \n";
		sql += "      END IF;\n";
		sql += "    END IF;\n";
		sql += "    IF TG_OP=''UPDATE'' THEN\n";
		sql += "        NEW.id := OLD.id; \n";
		sql += "    END IF;\n";
		Set<PersistentEvent> events = new HashSet<PersistentEvent>();
		events.add(PersistentEvent.BEFORE_INSERT);
		events.add(PersistentEvent.BEFORE_UPDATE);
		return new Trigger("set_id", getName(), sql, events, null, true, true, co);
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
		if (sqlType.indexOf("char") != -1) {
			sqlType += "(" + info.getSize() + ")";
		}
		else if (sqlType.equals("numeric")) {
			sqlType += "(" + info.getNumPrecRadix() + "," + info.getDigits() + ")";
		}
		return sqlType;
	}

	public List<String> getCreateObjectSQL(ConfigObject o) throws DBAdapterException {
		List<String> result = new ArrayList<String>();
		try {
			if (o.isSequence()) {
				result.addAll(getCreateSequenceSql(o.getName(),1,1));
			}
			else if (o.isView()) {
				result.add(o.getViewText());
			}
			else if (o.isTable()) {
				String sql = "CREATE TABLE " + qi(o.getName()) + " (";
				for (Iterator<ObjectAttr> i = o.getAttributes().iterator(); i.hasNext();) {
					ObjectAttr attr = (ObjectAttr) i.next();
					String id = attr.getName();
					sql += qi(id) + " ";
					sql += getSQLType(attr);
					//if (id.equals("id"))
					//	sql += " PRIMARY KEY"; DO NOT USE PRIMARY KEY for postgres !
					if (i.hasNext()) {
						sql += ", ";
					}
				}
				sql += ")";
				result.add(sql);
			}
			else if (o.isAlias()) {
				throw new DBAdapterException("Alias object '" + o.getName() + "' can't have create SQL statment");
			}
			else {
				throw new DBAdapterException("Unknown object type '" + o.getName() + "' can't have create SQL statment");
			}
		}
		catch (Exception e) {
			throw new DBAdapterException(e.getMessage());
		}

		return result;
	}

	public void dropConstraints(DBConnection conn, String schema, String tableName) throws SQLException {
	}

	public String getSQLIdentifier(ConfigObject co) {
		return qi(co.getRealName());
	}

	public String getSQLIdentifier(ObjectAttr oa) {
		return qi(oa.getName());
	}

	public String getSQLIdentifier(String schemaName, String tableName) {
		return qi(tableName);
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
		return sqlSelect + " LIMIT " + limit;
	}

	public Trigger getLogTrigger(ConfigObject co) {
		if (!co.isTable()) {
			return null;
		}
		String sql = "";
		sql += "    IF tg_op = ''INSERT'' THEN\n";
		sql += "        mz_id := new.id;\n";
		sql += "    ELSE\n";
		sql += "        mz_id := old.id;\n";
		sql += "    END IF;\n";
		sql += "    INSERT INTO " + qi(getLogTableName()) + " ( id, \"time\",\"user\", object, action, objectid ) \n";
		sql += "	    VALUES ( nextval(''" + getLogSequenceName() + "''), now(), '''', tg_relname, tg_op, mz_id);\n";
		Set<PersistentEvent> events = new HashSet<PersistentEvent>();
		events.add(PersistentEvent.AFTER_INSERT);
		events.add(PersistentEvent.AFTER_UPDATE);
		events.add(PersistentEvent.AFTER_DELETE);
		Set<VariableInfo> variables = new HashSet<VariableInfo>();
		variables.add(new VariableInfo("mz_id", "VARCHAR"));
		return new Trigger("log", getName(), sql, events, variables, true, true, co);
	}

	public Trigger getLogupdateTrigger(ConfigObject co) {
		if (!co.isTable()) {
			return null;
		}
		String sql = "";
		sql += "    SELECT id INTO mz_id1 FROM logupdate WHERE object::text=tg_relname;\n";
		sql += "    IF FOUND THEN\n";
		sql += "	    UPDATE logupdate SET time=now() WHERE id=mz_id1;\n";
		sql += "    ELSE\n";
		sql += "	    SELECT COALESCE(max(id),0)+1 INTO mz_id1 FROM logupdate;\n";
		sql += "	    INSERT INTO logupdate (id, object, \"time\") \n";
		sql += "	        VALUES (mz_id1, tg_relname, now());\n";
		sql += "    END IF;\n";
		Set<PersistentEvent> events = new HashSet<PersistentEvent>();
		events.add(PersistentEvent.AFTER_INSERT);
		events.add(PersistentEvent.AFTER_UPDATE);
		events.add(PersistentEvent.AFTER_DELETE);
		Set<VariableInfo> variables = new HashSet<VariableInfo>();
		variables.add(new VariableInfo("mz_id1", "INTEGER"));
		return new Trigger("logupdate", getName(), sql, events, variables, true, true, co);
	}

	private static Map<String,List<String>> systemObjectSQL = new HashMap<String,List<String>>();

	static {
		Postgres a = new Postgres();
		List<String> sql = Arrays.asList(new String[] {"CREATE TABLE " + a.getLogTableName() + " (id int not null primary key, \"time\" timestamp, \"user\" varchar(50), object varchar(32), action varchar(6), objectid varchar(25))"});
		systemObjectSQL.put(a.getLogTableName(), sql);
		sql = Arrays.asList(new String[] {"CREATE TABLE " + a.getLogupdateTableName() + " (id int not null primary key, object varchar(32) unique, \"time\" timestamp)"});
		systemObjectSQL.put(a.getLogupdateTableName(), sql);
		sql = new Postgres().getCreateSequenceSql(a.getLogSequenceName(),1,1);
		systemObjectSQL.put(a.getLogSequenceName(), sql);
		systemObjectSQL = Collections.unmodifiableMap(systemObjectSQL);
	};

	public List<String> getSystemObjectCreateSql(String objectName) {
		return systemObjectSQL.get(objectName);
	}

	public boolean isEnablesForEachStatementTriggers() {
		return false;
	}

	public String getName() {
		return DBAdapter.POSTGRES72;
	}

	public TriggerSQLGenerator getTriggerSQLGenerator() {
		return new PostgresTriggerSQLGenerator(this);
	}

	public int getMaxObjectNameLength() {
		return MAX_OBJECT_NAME_LENGTH;
	}

	public int getMaxAttributeNameLength() {
		return MAX_ATTRINBUTE_NAME_LENGTH;
	}

	public String getTriggerReturnType() {
		return "OPAQUE";
	}

	public boolean storedProcedureExists(DBConnection connect, String functionName) throws SQLException {
		return true;
	}

    public boolean indexExists(DBConnection connect, String tableName, String indexName) throws SQLException {
    	return 0 < connect.queryForInt(
        		"SELECT count(*) FROM pg_indexes WHERE tablename=? AND indexname=?", 
        		new Object[]{tableName, indexName}
        );
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
        return true;
    }

	public boolean useSelectedAttributesAliasing() {
		return false;
	}

    public Set<String> getSQLKeywords() {
        return SQL_KEYWORDS;
    }

    protected String registeredName(String name) {
        return name;
    }

    public List<ConfigIndex> getIndexes(DBConnection connection, String table) throws SQLException, DBConfigException {
		final LinkedList<ConfigIndex> result = new LinkedList<ConfigIndex>();
		connection.executeQuery(
				"SELECT indexdef FROM pg_indexes WHERE tablename=?",
				new Object[]{table},
				new RowCallbackHandler(){
					public void processRow(ResultSet rs) throws SQLException {
						result.add(createConfigIndex(rs.getString(1)));
					}
				}
		);
		return result;
	}

	private static final REMatcher NON_UNIQ_INDEX = new REMatcher("CREATE\\s+INDEX\\s+\\\"*(.+?)\\\"*\\s+ON\\s+(.+)\\s+USING\\s+[^\\(]+\\(([^\\)]+)\\)");
	private static final REMatcher UNIQ_INDEX = new REMatcher("CREATE\\s+UNIQUE\\s+INDEX\\s+\\\"*(.+?)\\\"*\\s+ON\\s+(.+)\\s+USING\\s+[^\\(]+\\(([^\\)]+)\\)");

	private ConfigIndex createConfigIndex(String indexDef) throws SQLException, DBConfigException {
		ConfigIndex result = null;
		boolean unique = false;
		Match match = NON_UNIQ_INDEX.getMatch(indexDef);
		if (match == null) {
			match = UNIQ_INDEX.getMatch(indexDef);
			unique = true;
		}
		if (match != null) {
			List<String> fields = new LinkedList<String>();
			StringTokenizer st = new StringTokenizer(match.getSubExpression(3), ",");
			while (st.hasMoreTokens()) {
				String s = st.nextToken();
				fields.add(Strings.trim(s.trim(), '"'));
			}
			result = new ConfigIndex(match.getSubExpression(1), fields, unique);
		}
		else {
			throw new SQLException("Cannot parse index definition: " + indexDef);
		}
		return result;
	}

	public List<String> getDropTableSql(String name) throws SQLException {
		return Arrays.asList(new String[]{"DROP TABLE " + qi(name)});
	}

    public List<String> getDropTableSql(String name, boolean isCascade) {
        return Arrays.asList(new String[]{"DROP TABLE " + qi(name) + (isCascade? " CASCADE" : "")});
    }

	public boolean doesTableHavePrimaryKey(DBConnection connection, String registeredTableName) throws SQLException {
		//TODO
		return true;
	}

	public boolean isSQLKeyword(String tmp) {
		return getSQLKeywords().contains(tmp.toUpperCase());
	}

	public List<String> getSystemAttributes() {
		return new ArrayList<String>();
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

	public Collection<String> getProcedures(DBConnection conn, String schema) throws SQLException {
		return new ArrayList<String>();
	}

	/* (non-Javadoc)
	 * @see ru.adv.db.adapter.DBAdapter#getConnectionParameters()
	 */
	@Override
	public Map<String, String> getConnectionParameters() {
		return Collections.singletonMap("charSet", "UTF-8");
	}

    public Map<String,Object> extractAdditionalInfo(Throwable e, MObject object) {
        Map<String,Object> result = new HashMap<String,Object>();
        if (e instanceof SQLException) {
            String msg = e.getMessage();
            String state = ((SQLException)e).getSQLState();
            if ("23505".equals(state)) {
                // unique constraint violation
                result = parseUniquenessViolationMessage(msg, object);
            }
            else if ("XX000".equals(state)) {
                // trigger exception
                result = parseForeignMessage(msg, object);
            }
        }
        return result;
    }

    public boolean supportsAdditionalInfoExtraction() {
        return true;
    }

    private static final Pattern UNIQUE_PATTERN;
    private static final Pattern FOREIGN_PATTERN;

    static {
        // Foreign key "d_a" not found in "a"
        FOREIGN_PATTERN = Pattern.compile("ERROR: Foreign key \"([^\"]+)\" not found in \"([^\"]+)\"");
        // duplicate key violates unique constraint "a-idx-2"
        UNIQUE_PATTERN = Pattern.compile("ERROR: duplicate key value violates unique constraint \"([^\"]+)\"");
    }

    protected Map<String,Object> parseForeignMessage(String msg, MObject object) {
        Map<String,Object> result = new HashMap<String,Object>();
        Matcher matcher = FOREIGN_PATTERN.matcher(msg);
        if (matcher.find()) {
            AttributeName attr = null;
            try {
                attr = new AttributeName(matcher.group(1));
            } catch (AttributeException e) {
                throw new UnreachableCodeReachedException(e);
            }
            result.put(ErrorCodeException.ATTR,  attr.attribute());
            result.put("foreign",    matcher.group(2));
            try {
                MValue val = object.getAttribute(attr.attribute()).getDBValue();
                if (val!=null) {
                    result.put("foreign-id", val.get());
                }
            } catch (DBException e) {
                throw new UnreachableCodeReachedException(e);
            }
            result.put("error-code", new Integer(ADVExceptionCode.INVALID_FOREIGN));
        }
        return result;
    }

    protected Map<String,Object> parseUniquenessViolationMessage(String msg, MObject object) {
        Map<String,Object> result = new HashMap<String,Object>();
        Matcher matcher = UNIQUE_PATTERN.matcher(msg);
        if (matcher.find()) {
            String indexName = matcher.group(1);
            result.put("error-code", new Integer(ADVExceptionCode.UNIQUENESS_VIOLATION));
            result.put("index", indexName);
            try {
                ConfigIndex index = object.getConfigObject().getIndex(indexName);
                List<String> uniqueAttrs = index.getColumns();
                result.put("unique-attributes", uniqueAttrs);
                setUniqueValues(uniqueAttrs, object, result);
            } catch (DBConfigException e) {
                // unknown index: we cannot set 'unique-attributes' and 'values' attributes
            }
        }
        return result;
    }

}

