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
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import ru.adv.db.DBConnection;
import ru.adv.db.DBException;
import ru.adv.db.JdbcConnectionParameters;
import ru.adv.db.base.MObject;
import ru.adv.db.base.MValue;
import ru.adv.db.config.ConfigIndex;
import ru.adv.db.config.ConfigObject;
import ru.adv.db.config.DBConfigException;
import ru.adv.db.config.ObjectAttr;
import ru.adv.logger.TLogger;
import ru.adv.util.ADVExceptionCode;
import ru.adv.util.AttributeException;
import ru.adv.util.AttributeName;
import ru.adv.util.ClassCreator;
import ru.adv.util.ErrorCodeException;
import ru.adv.util.UnreachableCodeReachedException;

public class Hsqldb extends DBAdapter {
	
	private static final String DRIVER_CLASS_NAME = "org.hsqldb.jdbcDriver";
	
	private static final Map<String,String> CONNECTION_PARAMS; 
	
    private static final Pattern UNIQUE_PATTERN;
    private static final Pattern FOREIGN_PATTERN;

    static {
        // FIXME Foreign key "d_a" not found in "a"
        FOREIGN_PATTERN = Pattern.compile("ERROR: Foreign key \"([^\"]+)\" not found in \"([^\"]+)\"");
        // Violation of unique constraint q-idx-3: duplicate value(s) for column(s) F4,F5,F6 in statement [INSERT INTO q (removed,objversion,f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)]
        UNIQUE_PATTERN = Pattern.compile("Violation of unique constraint ([^\\:]+)\\:");
    }
	
	static {
		Map<String,String> buff = new HashMap<String, String>();
		buff.put("shutdown", "true");
		buff.put("hsqldb.default_table_type", "cached");
		CONNECTION_PARAMS = Collections.unmodifiableMap(buff); 
	}
	
	private static final Set<String> SQL_KEYWORDS =
		Collections.unmodifiableSet( new TreeSet<String>( Arrays.asList( StringUtils.tokenizeToStringArray( 
				"BEFORE,BIGINT,BINARY,CACHED,DATETIME,LIMIT,LONGVARBINARY,LONGVARCHAR,OBJECT," +
				"OTHER,SAVEPOINT,TEMP,TEXT,TOP,TRIGGER,TINYINT,VARBINARY,VARCHAR_IGNORECASE",
				", ", true, true 
		))));
	
	private static Map<String,List<String>> systemObjectSQL = new HashMap<String,List<String>>();
	static {
		DBAdapter a = new Hsqldb();
		List<String> sql = Arrays.asList(new String[] {
				"CREATE TABLE " + a.getLogTableName() + " (id int not null primary key, \"time\" timestamp, \"user\" varchar(50), \"object\" varchar(32), \"action\" varchar(6), objectid varchar(25))"
				
		});
		systemObjectSQL.put(a.getLogTableName(), sql);
		sql = Arrays.asList(new String[] {
				"CREATE TABLE " + a.getLogupdateTableName() + " (id int not null primary key, \"object\" varchar(32), \"time\" timestamp)",
				"ALTER TABLE " + a.getLogupdateTableName() + " ADD UNIQUE (\"object\")"
		});
		systemObjectSQL.put(a.getLogupdateTableName(), sql);
		sql = a.getCreateSequenceSql(a.getLogSequenceName(),1,1);
		systemObjectSQL.put(a.getLogSequenceName(), sql);
		systemObjectSQL = Collections.unmodifiableMap(systemObjectSQL);
	};
	
	private static HashMap<String,String> dbtTypes = new HashMap<String,String>();
	private TLogger logger = new TLogger(Hsqldb.class);
	
	static {
		// Load the driver
		try {
			ClassCreator.forName(DRIVER_CLASS_NAME);
		} catch (Exception e) {
			TLogger.error(Hsqldb.class,"Hsqldb: " + e);
		}
	}
	
	// Mapping DBT to SQL
	static {
		dbtTypes.put(DBT_STRING, "VARCHAR");
		dbtTypes.put(DBT_TEXT, "LONGVARCHAR");
		dbtTypes.put(DBT_SHORTINT, "SMALLINT");
		dbtTypes.put(DBT_INT, "INTEGER");
		dbtTypes.put(DBT_LONG, "BIGINT");
		dbtTypes.put(DBT_DATE, "DATE");
		dbtTypes.put(DBT_TIMESTAMP, "TIMESTAMP");
		dbtTypes.put(DBT_BOOLEAN, "SMALLINT");
		dbtTypes.put(DBT_FILE, "SMALLINT");
		dbtTypes.put(DBT_FLOAT, "FLOAT");
		dbtTypes.put(DBT_DOUBLE, "DOUBLE");
		dbtTypes.put(DBT_NUMERIC, "NUMERIC");
	}
	
	/**
	 * protected constructor
	 */
	protected Hsqldb() {
		super();
	}
	
	@Override
	public boolean isSupportNativeSystemTriggers() {
		return false;
	}

	@Override
	public boolean isSupportConstraints() {
		return true;
	}

	@Override
	public boolean allowsNullsInUniqueIndex() {
		return true;
	}
	
	@Override
	public boolean isNullsLastOrder() {
		return false;
	}

	@Override
	public String dbtToSQLType(String dbt) throws DBAdapterException {
		String type = (String) dbtTypes.get(dbt);
		if (type == null)
			throw new DBAdapterException("DBT constant=" + dbt + " is not defined");
		return type;
	}
	
	@Override
	public String addLimitInstructionsToSelect(String selectSentence, int offset, int limit) {
		Assert.isTrue(offset>=0, "Wrong value for offset: "+offset);
		Assert.isTrue(limit>=0, "Wrong value for limit: "+limit);
		return selectSentence + String.format(" LIMIT %1$s OFFSET %2$s",limit,offset);
	}

	/**
	 * create quoted comma delimited string
	 */
	private String qi(List<String> identifierList) {
		List<String> resultList = new ArrayList<String>(identifierList.size());
		for (String identifier : identifierList) {
			resultList.add(qi(identifier));
		}
		return StringUtils.collectionToCommaDelimitedString(resultList);
	}
	
	
	@Override
	public boolean isCreatePrimaryIndex() {
		return false;
	}

	@Override
	public boolean doesTableHavePrimaryKey(DBConnection connection,	String tableName) throws SQLException {
		return 0 < connection.queryForLong(
				"SELECT count(*) FROM information_schema.system_primarykeys WHERE table_name=?",
				new Object[]{registeredName(tableName)}
		);
	}
	
	@Override
	public void dropConstraints(DBConnection conn, String schema, String tableName) throws SQLException, DBConfigException {
		// select all constrain names for tabel
		List<Object[]> rows = conn.executeQuery(
				"SELECT CONSTRAINT_NAME from information_schema.SYSTEM_TABLE_CONSTRAINTS where CONSTRAINT_TYPE!='PRIMARY KEY' and TABLE_NAME=?",
				new Object[]{registeredName(tableName)}
		);
		// drop all constrains
		for (Object[] row : rows) {
			conn.executeUpdate( String.format( "ALTER TABLE %1$s DROP CONSTRAINT %2$s", qi(tableName), qi(row[0].toString()) ));
		}
	}
	
	@Override
	public boolean isSupportDistinctOn() {
		return false;
	}

	@Override
    public Map<String,Object> extractAdditionalInfo(Throwable t, MObject object) {
        Map<String,Object> result = Collections.emptyMap();
        
        if (t instanceof SQLException ) {
            String msg = t.getMessage();
            String state = ((SQLException)t).getSQLState();
            if ("23000".equals(state)) {
                // unique constraint violation
                result = parseUniquenessViolationMessage(msg, object);
            }
            else if ("P0001".equals(state)) {
                // PostgresSQL-8.0 P0001 RAISE EXCEPTION
                result = parseForeignMessage(msg, object);
            }
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
    

	@Override
	public String extractDBName(String configName) {
		return configName;
	}

	@Override
	public String extractSchemaName(String configName) {
		return "PUBLIC";
	}

	@Override
	public Map<String, String> getConnectionParameters() {
		return CONNECTION_PARAMS;
	}

	@Override
	public boolean enablesCreatingDatabases() {
		return false; // in-process mode creates db automatically 
	}
	
	@Override
	public String getCreateDatabaseSql(String name) {
		return null;
	}

	@Override
	public String getCreateIndexSql(String tableName, String indexName,	List<String> columnNames, boolean unique) {
		if (!unique) {
			return "CREATE INDEX "+qi(indexName)+" ON "+qi(tableName)+" ("+qi(columnNames)+")";
		}else{
			// Unique indexes can be defined but this is deprecated. 
			// Use UNIQUE constraints instead. The name of an index must be unique within the whole database.
			return "ALTER TABLE "+qi(tableName)+" ADD CONSTRAINT "+qi(indexName)+" UNIQUE ("+qi(columnNames)+")";
		}
	}

	@Override
	public List<String> getCreateObjectSQL(ConfigObject o) throws DBAdapterException {
		List<String> result = new ArrayList<String>();
		try {
			
			if (o.isSequence()) {
				result.addAll(getCreateSequenceSql(o.getName(),1,1));
			} else if (o.isView()) {
				result.add(o.getViewText());
			} else if (o.isTable()) {
				String sql = "CREATE CACHED TABLE " + qi(o.getName()) + " (";
				for (Iterator<ObjectAttr> i = o.getAttributes().iterator(); i.hasNext();) {
					ObjectAttr attr = (ObjectAttr) i.next();
					String id = attr.getName();
					sql += qi(id) + " ";
					sql += getSQLType(attr);
					if ("id".equals(id)) {
						sql+=" PRIMARY KEY ";
					}
					if (i.hasNext()) {
						sql += ", ";
					}
				}
				sql += ")";
				result.add(sql);
			} else if (o.isAlias()) {
				throw new DBAdapterException("Alias object '" + o.getName() + "' can't have create SQL statment");
			} else {
				throw new DBAdapterException("Unknown object type '" + o.getName() + "' can't have create SQL statment");
			}
			
		} catch (Exception e) {
			throw new DBAdapterException(e.getMessage());
		}
		return result;
	}

	@Override
	public List<String> getCreateSequenceSql(String name, long startValue, int increment) {
		return Collections.singletonList( 
				String.format(
						"CREATE SEQUENCE %1$s AS BIGINT START WITH %2$s INCREMENT BY %3$s",
						name, startValue, increment
				) 
		);
	}

	@Override
	public String getCustToStringSql(String attrSQLIdent) {
		return String.format("CONVERT(%1$s,varchar)",attrSQLIdent);
	}

	@Override
	public String getDatabaseSQLIdentifier(String schemaName,String databaseName) {
		return databaseName;
	}

	@Override
	public String getDriverClassName() {
		return DRIVER_CLASS_NAME;
	}

	@Override
	public String getDropDatabaseSql(String name) {
		return null;
	}

	@Override
	public String getDropIndexSql(String tableName, String indexName) {
		return String.format("DROP INDEX %1$s IF EXISTS", qi(indexName));
	}

	@Override
	public List<String> getDropSequenceSql(String name) {
		return Collections.singletonList( 
				String.format("DROP SEQUENCE %1$s IF EXISTS", qi(name)) 
		);
	}

	@Override
	public String getDropStoredProcedureSql(String name) {
		throw new DBAdapterException("Stored procedures are not supported by adpater");
	}

	@Override
	public List<String> getDropTableSql(String name) throws SQLException {
		return getDropTableSql(name,false);
	}

	@Override
	public List<String> getDropTableSql(String name, boolean isCascade) {
		return Collections.singletonList( 
				String.format("DROP TABLE %1$s IF EXISTS %2$s", 
						name, (isCascade ? "CASCADE":"")
				) 
		);
	}

	@Override
	public String getDropViewSql(String name) {
		return String.format("DROP VIEW %1$s IF EXISTS", name);
	}

	@Override
	public String getExistentDatabaseSql() {
		throw new DBAdapterException("existentDatabaseSql are not supported by adpater");
	}

	@Override
	public List<ConfigIndex> getIndexes(DBConnection connection, String table) throws SQLException, DBConfigException {
		
		final LinkedList<ConfigIndex> result = new LinkedList<ConfigIndex>();
		List<Object[]> indexNameRows = connection.executeQuery(
				// select non_unique indexes only cause unique indexes are constraint in HsqlDB
				"SELECT DISTINCT INDEX_NAME FROM INFORMATION_SCHEMA.SYSTEM_INDEXINFO WHERE NON_UNIQUE and TABLE_NAME=?", 
				new Object[]{registeredName(table)} 
		);
		
		for (Object[] idxNameRow : indexNameRows) {
			String indexName = idxNameRow[0].toString();
			List<Object[]> idxColumns = connection.executeQuery(
					"SELECT COLUMN_NAME,NON_UNIQUE" +
					" FROM INFORMATION_SCHEMA.SYSTEM_INDEXINFO" +
					" WHERE TABLE_NAME=? and INDEX_NAME=? order by ORDINAL_POSITION",
					new Object[]{registeredName(table), registeredName(indexName) }
			);
			Assert.notEmpty(idxColumns,"Wrong number columns in index");
			boolean isUnique = Boolean.parseBoolean( idxColumns.get(0)[1].toString() );
			List<String> columnNames = new ArrayList<String>();
			for (Object[] idxColumn : idxColumns) {
				columnNames.add(idxColumn[0].toString());
			}
			result.add(new ConfigIndex(indexName, columnNames, isUnique));
			
		}
		return result;	
	}

	@Override
	public int getMaxAttributeNameLength() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int getMaxObjectNameLength() {
		return Integer.MAX_VALUE;
	}

	@Override
	public String getName() {
		return DBAdapter.HSQLDB;
	}

	@Override
	public List<String> getOnCreateDatabaseSql(String dbname) {
		return Collections.emptyList();
	}

	@Override
	public Collection<String> getProcedures(DBConnection conn, String schema) throws SQLException {
		return Collections.emptyList();
	}

	@Override
	public String getRandomFunctionSql() {
		return "rand()";
	}

	@Override
	public String getSQLIdentifier(ConfigObject co) {
		return qi(co.getRealName());
	}

	@Override
	public String getSQLIdentifier(ObjectAttr attr) {
		return qi(attr.getName());
	}

	@Override
	public String getSQLIdentifier(String schemaName, String tableName) {
		return qi(tableName);
	}

	@Override
	public String getSQLIdentifier(String attrName) {
		return qi(attrName);
	}

	@Override
	public Set<String> getSQLKeywords() {
		return SQL_KEYWORDS;
	}

	@Override
	public String getSQLType(ColumnInfo info) throws DBAdapterException {
		String sqlType = "";
		sqlType += info.getTypeName();
		if (sqlType.startsWith("VARCHAR")) { 
			sqlType += "(" + info.getSize() + ")";
		} else if (sqlType.equals("NUMERIC")) {
			sqlType += "(" + info.getNumPrecRadix() + "," + info.getDigits() + ")";
		}
		return sqlType;
	}
	
	@Override
	public String getSequenceIncrementSql(String schemaName, String name) {
		return String.format(
				"select INCREMENT from INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_NAME='%1$s'", registeredName(name)
		);
	}

	@Override
	public String getSequenceCurrentSql(String schemaName, String name) {
		return String.format(
				"select START_WITH-INCREMENT " +
				"from INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_NAME='%1$s'",
				registeredName(name)
		);
	}
	
	@Override
	public Long getSequenceNextValue(DBConnection connection, String schemaName, String name) {
		return connection.queryForLong( String.format("call next value for %1$s",name) );
	}

	@Override
	public List<String> getSetValueSequenceSql(String name, long value,	int increment) {
		List<String> sqlList = new ArrayList<String>();
		sqlList.addAll( getDropSequenceSql(name) );
		sqlList.addAll( getCreateSequenceSql(name, value, increment) );
		return sqlList;
	}

	@Override
	public List<String> getSystemAttributes() {
		return Collections.emptyList();
	}

	@Override
	public List<String> getSystemObjectCreateSql(String objectName) {
		return systemObjectSQL.get(objectName);
	}

	@Override
	public List<String> getSystemStoredProcedureSql(String schema) {
		return Collections.emptyList();
	}

	@Override
	public String getTableAddColumnsSql(String tableName, String columnName, String columnDefinition) {
		return String.format(
				"ALTER TABLE %1$s ADD COLUMN %2$s %3$s",
				tableName, columnName, columnDefinition
		);
	}

	@Override
	public String getTableDropColumnsSql(String tableName,	Collection<String> columnNames, Collection<String> columnNamesToDrop) {
		StringBuilder buff = new StringBuilder();
		for (String columnName : columnNamesToDrop) {
			buff.append( String.format("ALTER TABLE %1$s DROP %2$s ;", qi(tableName), qi(columnName)) );
		}
		return buff.toString();
	}

	@Override
	public SqlActionList getTableSyncTypeColumnsActions(DBConnection conn, String schema, ConfigObject co) 
	throws SQLException,DBConfigException, DBAdapterException 
	{

		String tableName = co.getName();
		
		Map<String,String> attrNewTypes = collectNewTypeAttributes(conn, schema, co, tableName);

		if (attrNewTypes.size() == 0) {
			return new SqlActionList(); // nothig to sync
		}

		List<String> result = new ArrayList<String>();
		
		for (Entry<String, String> entry : attrNewTypes.entrySet() ) {
			String attrName = entry.getKey();
			String attrType = entry.getValue();
			result.add(
					String.format(
							"ALTER TABLE %1$s ALTER COLUMN %2$s %3$s", 
							qi(tableName), qi(attrName), attrType
					)
			);
		}
		
		return new SqlActionList(result);
		
	}

	private Map<String, String> collectNewTypeAttributes(DBConnection conn,
			String schema, ConfigObject co, String tableName) throws SQLException
			{
		Map<String, String> attrNewTypes;
		attrNewTypes = new HashMap<String,String>();
		Map<String,ColumnInfo> attrTypesInTable = getTableColumns(conn, schema, tableName);
		for (ObjectAttr attr : co.getAttributes()) {
			String attrName = attr.getName();
			ColumnInfo columnInfo = (ColumnInfo) attrTypesInTable.get(attrName);
			String sqlTypeInTable = getSQLType(columnInfo);
			String sqlTypeInConfig = getSQLType(attr);
			if (!sqlTypeInConfig.equalsIgnoreCase(sqlTypeInTable)) {
				logger.info("Table " + co.getName() + ": column " + attrName + " wrong type " +
				             sqlTypeInConfig + "<>" + sqlTypeInTable + ", will be synchronized...");
				attrNewTypes.put(attrName, sqlTypeInConfig);
			}
		}
		return attrNewTypes;
	}

	/**
	 * host: path to directory db
	 */
	@Override
	public String getURL(JdbcConnectionParameters connectionParameters) {
		return 
			"jdbc:hsqldb:file:"+connectionParameters.getDatabaseFilesPath()+"/hsqldb/"+connectionParameters.getDatabaseName()+
			";shutdown=true"; // define here too
	}

	@Override
	public String ignoreCase(String in) {
		return new StringBuffer("UPPER(").append(in).append(")").toString();
	}

	@Override
	public boolean indexExists(DBConnection connect, String tableName, String indexName) throws SQLException {
	   	return 0 < connect.queryForInt(
        		"SELECT count(*) FROM INFORMATION_SCHEMA.SYSTEM_INDEXINFO WHERE TABLE_NAME=? AND INDEX_NAME=?", 
        		new Object[]{registeredName(tableName), registeredName(indexName)}
        );
	}

	@Override
	public boolean isSQLKeyword(String tmp) {
		return getSQLKeywords().contains(tmp.toUpperCase());
	}

//	@Override
//	public boolean isSupportForeignConstrain() {
//		return true;
//	}

	@Override
	protected String registeredName(String name) {
		if (!needQuoting(name)) {
			return name.toUpperCase();
		}
		return name;
	}

	/**
	 * gets list of sequences names in low case
	 */
	@Override
	public Collection<String> getSequences(DBConnection conn, String schema) throws SQLException {
		
		final Set<String> list = new HashSet<String>();
		conn.executeQuery(
				"SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES",
				null, 
				new RowCallbackHandler() {
					public void processRow(ResultSet rs) throws SQLException {
						list.add(rs.getString(1).toLowerCase());
					}
				}
		);
		return list;
		
	}

	@Override
	public boolean sequenceExists(DBConnection connect, String name) throws SQLException {
		return getSequences(connect, null).contains(name.toLowerCase());
	}

	@Override
	public boolean storedProcedureExists(DBConnection connect,String functionName) throws SQLException {
		return false;
	}

	@Override
	public boolean supportsAdditionalInfoExtraction() {
		return true;
	}

	@Override
	public boolean tableExists(DBConnection connect, String name) throws SQLException {
		return getTables(connect, "public").contains(name.toLowerCase());
	}
	
	@Override
	public boolean viewExists(DBConnection connect, String name) throws SQLException {
		return getViews(connect, "public").contains(name.toLowerCase());
	}

	@Override
	public String toUpperCase(String in) {
		return new StringBuffer("UPPER(").append(in).append(")").toString();
	}

	@Override
	public boolean useSelectedAttributesAliasing() {
		return true;
	}


}

