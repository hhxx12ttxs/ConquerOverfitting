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

import java.sql.Connection;
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

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
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
import ru.adv.util.ADVRuntimeException;
import ru.adv.util.AttributeException;
import ru.adv.util.AttributeName;
import ru.adv.util.ClassCreator;
import ru.adv.util.ErrorCodeException;
import ru.adv.util.UnreachableCodeReachedException;

public class Mysql extends DBAdapter {
	
	public static final String SEQ_NAME_SUFFIX = "_seq";

	private static final String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
	
	private static final Map<String,String> CONNECTION_PARAMS; 
	
    private static final Pattern UNIQUE_PATTERN;
    private static final Pattern FOREIGN_PATTERN;

    static {
        // FIXME 
    	// Foreign key "d_a" not found in "a"
        FOREIGN_PATTERN = Pattern.compile("ERROR: Foreign key \"([^\"]+)\" not found in \"([^\"]+)\"");
        // Violation of unique constraint q-idx-3: duplicate value(s) for column(s) F4,F5,F6 in statement [INSERT INTO q (removed,objversion,f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)]
        UNIQUE_PATTERN = Pattern.compile("Duplicate entry .*for key '([^']+)'");
    }
	
	static {
		Map<String,String> buff = new HashMap<String, String>();
		buff.put("useUnicode","true");
		buff.put("characterEncoding","UTF8");
		CONNECTION_PARAMS = Collections.unmodifiableMap(buff); 
	}
	
	private static final Set<String> SQL_KEYWORDS =
		Collections.unmodifiableSet( new TreeSet<String>( Arrays.asList( StringUtils.tokenizeToStringArray( 
				"BEFORE,BIGINT,BINARY,DATETIME,LIMIT,LONGVARBINARY,OBJECT," +
				"OTHER,SAVEPOINT,TEMP,TEXT,TOP,TRIGGER,TINYINT,VARBINARY,VARCHAR_IGNORECASE," +
				"LONG",
				", ", true, true 
		))));
	
	private static Map<String,List<String>> systemObjectSQL = new HashMap<String,List<String>>();
	static {
		DBAdapter a = new Mysql();
		List<String> sql = Arrays.asList(new String[] {
				"CREATE TABLE " + a.getLogTableName() + 
				" (id bigint not null primary key, `time` timestamp, `user` varchar(50), " +
				"   `object` varchar(32), `action` varchar(6), objectid varchar(25)" +
				"  ) ENGINE=InnoDb"
				
		});
		systemObjectSQL.put(a.getLogTableName(), sql);
		sql = Arrays.asList(new String[] {
				"CREATE TABLE " + a.getLogupdateTableName() + 
				" (id int not null primary key, `object` varchar(32), `time` timestamp) ENGINE=InnoDb",
				"ALTER TABLE " + a.getLogupdateTableName() + " ADD UNIQUE (`object`)"
		});
		systemObjectSQL.put(a.getLogupdateTableName(), sql);
		sql = a.getCreateSequenceSql(a.getLogSequenceName(),1,1);
		systemObjectSQL.put(a.getLogSequenceName(), sql);
		systemObjectSQL = Collections.unmodifiableMap(systemObjectSQL);
	};
	
	private static HashMap<String,String> dbtTypes = new HashMap<String,String>();
	private TLogger logger = new TLogger(Mysql.class);
	
	static {
		// Load the driver
		try {
			ClassCreator.forName(DRIVER_CLASS_NAME);
		} catch (Exception e) {
			TLogger.logErrorStackTrace(Mysql.class, "Mysql JDBC failed", e);
		}
	}
	
	// Mapping DBT to SQL
	static {
		dbtTypes.put(DBT_STRING, "VARCHAR");
		dbtTypes.put(DBT_TEXT, "TEXT");
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
	protected Mysql() {
		super();
	}
	
	public boolean isSupportSqlExceptInstruction() {
		return false;
	}
	
	@Override
	public char getQuoteSign() {
		return '`';
	}
	
	@Override
	public String getCasewhenFunctionName() {
		return "IF";
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
	public boolean doesTableHavePrimaryKey(DBConnection connection,	final String tableName) throws SQLException {
		return (Boolean)connection.execute(new ConnectionCallback() {
			@Override
			public Object doInConnection(Connection con) throws SQLException,	DataAccessException {
				ResultSet rs =	con.getMetaData().getPrimaryKeys(null, null, tableName);
				boolean hasPrimary = rs.next(); 
				rs.close();
				return hasPrimary;
			}
		});
	}
	
	@Override
	public void dropConstraints(DBConnection conn, String schema, String tableName) throws SQLException, DBConfigException {
		// select all constrain names for tabel
		List<Object[]> rows = conn.executeQuery(
				"SELECT CONSTRAINT_NAME,CONSTRAINT_TYPE " +
				" FROM information_schema.TABLE_CONSTRAINTS " +
				" WHERE " +
				"  CONSTRAINT_TYPE!='PRIMARY KEY' " +
				"  AND TABLE_SCHEMA=?" +
				"  AND TABLE_NAME=?",
				new String[]{registeredName(schema), registeredName(tableName)}
		);
		// drop all constrains
		for (Object[] row : rows) {
			String constraintName = row[0].toString();
			String constraintType = row[1].toString();
			Assert.isTrue("UNIQUE".equals(constraintType), "The 'UNIQUE' type constraint is known only ");
			conn.executeUpdate( 
					String.format( "ALTER TABLE %1$s DROP INDEX %2$s", qi(tableName), qi(constraintName) )
			);
			
		}
	}
	
	@Override
	public boolean isSupportDistinctOn() {
		return false;
	}

	@Override
    public Map<String,Object> extractAdditionalInfo(Throwable t, MObject object) {
        Map<String,Object> result = Collections.emptyMap();
       //TODO
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
		return configName; // schema name is equals to DB name for MySql
	}

	@Override
	public Map<String, String> getConnectionParameters() {
		return CONNECTION_PARAMS;
	}

	@Override
	public boolean enablesCreatingDatabases() {
		return true; 
	}
	
	@Override
	public String getCreateDatabaseSql(String name) {
		return "CREATE DATABASE "+qi(name) + " CHARACTER SET 'UTF8'";
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
				String sql = "CREATE TABLE " + qi(o.getName()) + " (";
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
				sql += ") ENGINE=InnoDb";
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
		List<String> sqlList = new ArrayList<String>();
		sqlList.add(
				String.format(
						"CREATE TABLE %1$s ( " +
						"  last_value bigint(20) NOT NULL, " +
						"  increment_by bigint(20) NOT NULL," +
						"  is_called boolean NOT NULL DEFAULT false)",
						correctSqlSequenceName(name)
				)
		);
		sqlList.add(
				String.format(
						"INSERT INTO %1$s ( last_value, increment_by) VALUES (%2$s,%3$s)",
						correctSqlSequenceName(name),startValue, increment
				)
		);
		return sqlList;
	}

	@Override
	public String getCustToStringSql(String attrSQLIdent) {
		return String.format("CONCAT('',%1$s)",attrSQLIdent);
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
		return String.format("DROP INDEX %1$s ON %2$s", qi(indexName), qi(tableName));
	}

	@Override
	public List<String> getDropSequenceSql(String name) {
		return getDropTableSql( correctSqlSequenceName(name) );
	}

	@Override
	public String getDropStoredProcedureSql(String name) {
		throw new DBAdapterException("Stored procedures are not supported by adpater");
	}

	@Override
	public List<String> getDropTableSql(String name) {
		return getDropTableSql(name,false);
	}

	@Override
	public List<String> getDropTableSql(String name, boolean isCascade) {
		return Collections.singletonList( 
				String.format("DROP TABLE IF EXISTS %1$s %2$s", 
						name, (isCascade ? "CASCADE":"")
				) 
		);
	}

	@Override
	public String getDropViewSql(String name) {
		return String.format("DROP VIEW %1$s", name);
	}

	@Override
	public String getExistentDatabaseSql() {
		return "SELECT SCHEMA_NAME FROM information_schema.SCHEMATA";
	}

	@Override
	public List<ConfigIndex> getIndexes(DBConnection connection, String table) throws SQLException, DBConfigException {
		final LinkedList<ConfigIndex> result = new LinkedList<ConfigIndex>();
		List<Object[]> indexNameRows = connection.executeQuery(
				// select non_unique indexes only cause unique indexes are constraint in Mysql
				"SELECT DISTINCT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS " +
				" WHERE NON_UNIQUE=1 and TABLE_SCHEMA = ? and TABLE_NAME=?", 
				new String[]{registeredName(connection.getDBName()), registeredName(table)} 
		);
	
		for (Object[] idxNameRow : indexNameRows) {
			String indexName = idxNameRow[0].toString();
			List<Object[]> idxColumns = connection.executeQuery(
					"SELECT COLUMN_NAME,NON_UNIQUE" +
					" FROM INFORMATION_SCHEMA.STATISTICS" +
					" WHERE TABLE_SCHEMA = ? and TABLE_NAME=? and INDEX_NAME=? order by SEQ_IN_INDEX",
					new String[]{registeredName(connection.getDBName()), registeredName(table), registeredName(indexName) }
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
		return DBAdapter.MYSQL;
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
	
	private String correctSqlSequenceName(String sequenceName) {
		return sequenceName + SEQ_NAME_SUFFIX;
	}
	
	@Override
	public String getSequenceIncrementSql(String schemaName, String name) {
		return String.format(
				"select increment_by from %1$s", registeredName(correctSqlSequenceName(name))
		);
	}

	@Override
	public String getSequenceCurrentSql(String schemaName, String name) {
		return String.format(
				"select last_value from %1$s", registeredName(correctSqlSequenceName(name))
		);
	}

	
	@Override
	public Long getSequenceNextValue(final DBConnection connection, String schemaName, final String name) {
		
		// Emulation sequence request must be executed in own transaction ! 
    	TransactionTemplate ownTransactionTemplate = new TransactionTemplate( 
    			connection.getTransactionTemplate().getTransactionManager() 
    	);
    	ownTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    	return (Long)ownTransactionTemplate.execute(new TransactionCallback() {
			@Override
			public Object doInTransaction(TransactionStatus status) {
				// DO IT in own connection! 
				// DO not increase value at the first request (like Postgres)
				int affectedRows = connection.executeUpdate( String.format(
						"UPDATE %1$s SET last_value = last_insert_id(last_value + if(is_called,increment_by,0)), is_called=true",
						correctSqlSequenceName(name)) 
				);
				if (affectedRows!=1) {
					throw new ADVRuntimeException("Error on update sequence '"+name+"'. Affected rows count="+affectedRows);
				}
				return connection.queryForLong( "select last_insert_id()" );
			}
		});
    	
	}

	@Override
	public List<String> getSetValueSequenceSql(String name, long value,	int increment) {
		List<String> sqlList = new ArrayList<String>();
		sqlList.add( String.format(
				"UPDATE %1$s SET last_value=%2$s, increment_by=%3$s, is_called=false", 
				correctSqlSequenceName(name), value, increment) 
		);
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
			buff.append( String.format("ALTER TABLE %1$s DROP COLUMN %2$s ;", qi(tableName), qi(columnName)) );
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
							"ALTER TABLE %1$s MODIFY COLUMN %2$s %3$s", 
							qi(tableName), qi(attrName), attrType
					)
			);
		}
		
		return new SqlActionList(result);
		
	}
	
	protected void ceateNullabilityConstraint(final DBConnection conn, final ObjectAttr a) {
		conn.execute(getNullabilityConstraintSql(
				a.getConfigObject().getRealName(),a.getSQLName(), getSQLType(a), a.isNullable() 
		));
	}
	
	protected String getNullabilityConstraintSql(String tableName, String attrName, String attrSqlDefinition, boolean isNullable) {
		return String.format(
				"ALTER TABLE %1$s MODIFY COLUMN %2$s %3$s %4$s",
				qi(tableName), attrName, attrSqlDefinition, ( isNullable ? "NULL" : "NOT NULL" )  
		);
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
		String database = connectionParameters.getDatabaseName();
		String host = connectionParameters.getHost();
		String port = connectionParameters.getPort();
		// jdbc:postgresql://host:port/database
		StringBuffer sb = new StringBuffer();
		sb.append("jdbc:mysql:");
		if (host != null) {
			sb.append("//");
			sb.append(host);
			if (port != null) {
				sb.append(":");
				sb.append(port);
			}
			sb.append("/");
		}
		if (database != null) {
			sb.append(database);
		}
		return sb.toString();
	}

	@Override
	public String ignoreCase(String in) {
		return new StringBuffer("UPPER(").append(in).append(")").toString();
	}

	@Override
	public boolean indexExists(DBConnection connect, final String tableName, String indexName) throws SQLException {
		return 0 < connect.queryForInt(
        		"SELECT count(*) FROM INFORMATION_SCHEMA.STATISTICS " +
        		" WHERE TABLE_SCHEMA=? AND TABLE_NAME=? AND INDEX_NAME=?", 
        		new String[]{registeredName(connect.getDBName()), registeredName(tableName), registeredName(indexName)}
        );
	}

	@Override
	public boolean isSQLKeyword(String tmp) {
		return getSQLKeywords().contains(tmp.toUpperCase());
	}

	@Override
	protected String registeredName(String name) {
		return name;
	}

	/**
	 * gets list of sequences names in low case
	 */
	@Override
	public Collection<String> getSequences(DBConnection conn, String schema) throws SQLException {
		final Set<String> list = new HashSet<String>();
		for ( String tableName: getTables(conn, schema) ) {
			if(tableName.endsWith(SEQ_NAME_SUFFIX))	{
				list.add( tableName.substring(0,tableName.lastIndexOf(SEQ_NAME_SUFFIX)) );				
			}
		}
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
		return getTables(connect, null).contains(name.toLowerCase());
	}
	
	@Override
	public boolean viewExists(DBConnection connect, String name) throws SQLException {
		return getViews(connect, null).contains(name.toLowerCase());
	}

	@Override
	public String toUpperCase(String in) {
		return new StringBuffer("UPPER(").append(in).append(")").toString();
	}

	@Override
	public boolean useSelectedAttributesAliasing() {
		return true;
	}

	@Override
	public void deleteFromExTree(DBConnection dbConnect, String quotedTableName, String quotedTreeTableName, Object id){
		
		// delete ancestors for all descendants
		// - (1 step) create temparary tables with records to delete
		dbConnect.executeUpdate( 
				String.format( 
						"CREATE TEMPORARY TABLE tmp_del_from_ex_tree " +
						"SELECT id, ancestor FROM %1$s WHERE" +
						"  ancestor IN (SELECT ancestor FROM %1$s WHERE id=?) " +
						"  AND id IN ( SELECT id FROM %1$s WHERE ancestor=?)", 
						quotedTreeTableName
				),
				new Object[]{id,id}
		);
		// - (2 step) delete from extTree
		dbConnect.executeUpdate( 
				String.format( 
						"DELETE FROM %1$s USING tmp_del_from_ex_tree" +
						" INNER JOIN tmp_del_from_ex_tree ON " +
						" %1$s.ID = tmp_del_from_ex_tree.ID" +
						" %1$s.ancestor = tmp_del_from_ex_tree.ancestor", 
						quotedTreeTableName
				)
		);
		// - (3 step) drop temp table
		dbConnect.execute( "DROP TEMPORARY TABLE tmp_del_from_ex_tree" );
		
		// delete own ancestors
		dbConnect.executeUpdate(
				String.format("DELETE FROM %1$s WHERE id=?",quotedTreeTableName), 
				new Object[]{ id }
		);
	}


}

