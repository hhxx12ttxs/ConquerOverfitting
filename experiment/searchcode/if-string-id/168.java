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
package ru.adv.repository.dump;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import ru.adv.db.DBConnection;
import ru.adv.db.adapter.DBAdapter;
import ru.adv.db.adapter.Types;
import ru.adv.db.base.DBCastException;
import ru.adv.db.base.DBValue;
import ru.adv.db.base.Id;
import ru.adv.db.config.ConfigObject;
import ru.adv.db.config.ConfigParser;
import ru.adv.db.config.DBConfig;
import ru.adv.db.config.DBConfigException;
import ru.adv.db.config.Diff;
import ru.adv.db.config.ObjectAttr;
import ru.adv.db.handler.Handler;
import ru.adv.db.handler.SQLWhereAbstract;
import ru.adv.logger.TLogger;
import ru.adv.repository.RepositoryImpl;
import ru.adv.util.ADVRuntimeException;
import ru.adv.util.UnreachableCodeReachedException;

/**
 * Date: 26.01.2004 Time: 11:44:11
 */
public class HashDumpAttrCollection implements AttributeCollection {
	static int UNIQ_ID = 0;
	static final String TMP_TABLE_NAME_PREFIX = "_attr_cllctn_";
	static final String ID = "id";
	static final String OBJECT = "object";
	static final String OBJ_VERSOIN = ConfigParser.VERSION_ATTR_ID;
	private String _tmpTableName;
	private PreparedStatement _prepStatementForGetVersion = null;
	private Set<String> _objectNames = new HashSet<String>();
	private Handler _handler;
	private Diff _diff;
	private Set _newObjects;
	private Set _changedObjects;
	private Set _deletedObjects;
	private Collection _excludeObjects;
	private String _objectVersionNotMatch = null;
	private boolean _injectMode = false;

	private TLogger logger = new TLogger(HashDumpAttrCollection.class);

	/**
	 * @param hashFile
	 * @param openedHandler
	 * @throws Exception
	 */
	public HashDumpAttrCollection(DumpFile hashFile, Handler openedHandler,
			Collection excludeObjects) throws ADVRuntimeException {
		this(false, hashFile, openedHandler, excludeObjects);
	}

	public HashDumpAttrCollection(boolean isInjectMode, DumpFile hashFile,
			Handler openedHandler, Collection excludeObjects)
			throws ADVRuntimeException {
		this._handler = openedHandler;
		this._excludeObjects = excludeObjects;
		setInjectMode(isInjectMode);
		this._objectVersionNotMatch = null;
		incUniqId();
		this._tmpTableName = TMP_TABLE_NAME_PREFIX + Integer.toString(UNIQ_ID);
		try {
			createAndFillTempTable(hashFile);
		} catch (Exception e) {
			throw new ADVRuntimeException(e);
		}
	}

	public Set getObjectNames() {
		return Collections.unmodifiableSet(_objectNames);
	}

	public boolean isInjectMode() {
		return _injectMode;
	}

	private void setInjectMode(boolean isInjectMode) {
		this._injectMode = isInjectMode;
	}

	public void destroy() {
		if (_handler!=null) {
			if (_prepStatementForGetVersion != null) {
				try {
					_prepStatementForGetVersion.close();
				} catch (SQLException e) {
					logger.warning(e);
				}
			}
			try {
				getDBConnection().execute(
						getAdapter().getDropTableSql(
								getAdapter().getTemporaryName(getTmpTableName()),
								true
						));
			} catch (Exception e) {
				logger.warning(e);
			}
		}
		_handler = null;
	}

	public Set getNewObjects() {
		return _newObjects;
	}

	public Set getChangedObjects() {
		return _changedObjects;
	}

	public Set getDeletedObjects() {
		return _deletedObjects;
	}

	public void handleNonExistsObjectId(String objectName,
			final IdCallbackHandler ich) throws Exception {
		try {
			getDBConnection().executeQuery(
					NON_EXISTS_STATEMENT(getAdapter(), objectName),
					new Object[] { objectName }, new RowCallbackHandler() {
						public void processRow(ResultSet rs)
								throws SQLException {
							try {
								ich.handleNextId(new Id(rs.getString(ID)));
							} catch (Exception e) {
								throw new RuntimeException(e); // hide into RTE
							}
						}
					});
		} catch (RuntimeException e) {
			if (e.getCause() instanceof Exception) {
				throw (Exception) e.getCause(); // unhide from RTE
			}
			throw e;
		}
	}

	/**
	 * return SQL WHERE statement for find object id for insert to remote
	 * replica
	 * 
	 * @param objectName
	 * @return
	 */
	public SQLWhereAbstract getSQLWhereForInsertObjects(final String objectName) {
		// id in (select id from tail except (select id from _attr_cllctn_6
		// where object='tail'));
		if  (getAdapter().isSupportSqlExceptInstruction()) {
			// optimized sql request for Postgres and other advanced DB
			return new Where(objectName, 
					ID() + " IN " + "( SELECT " + ID()
					+ " FROM " + SQL(objectName) + " EXCEPT " + "( SELECT " + ID()
					+ " FROM " + TMP_TABLE() + " WHERE " + OBJECT() + "=?" + ")"
					+ ")");
		}
		return new Where(objectName, 
				ID()+" NOT IN ( SELECT " + ID() +" FROM " + TMP_TABLE() + " WHERE " + OBJECT() + "=?" + ")"
		);
		
	}

	/**
	 * @param objectName
	 * @return
	 */
	public SQLWhereAbstract getSQLWhereForUpdateObjects(final String objectName) {
		// id in ( select t.id from tail t, _attr_cllctn_6 a where
		// a.object='tail' and t.id=a.id and ( t.objversion<>a.objver ) );
		final String OBJECT_NAME = SQL(objectName);
		String injectCondition = "";
		if (isInjectMode()) {
			injectCondition = " AND " + TMP_TABLE() + "." + OBJECT_VERSION()
					+ " NOT LIKE '%" + _objectVersionNotMatch + "'";
		}
		if (isSelectAllObjects(objectName)) {
			return new Where(objectName, ID() + " IN " + "( SELECT "
					+ OBJECT_NAME + "." + ID() + " FROM " + OBJECT_NAME + ","
					+ TMP_TABLE() + " WHERE " + TMP_TABLE() + "." + OBJECT()
					+ "=?" + injectCondition + " AND " + TMP_TABLE() + "."
					+ ID() + "=" + OBJECT_NAME + "." + ID() + ")");
		} else {
			return new Where(objectName, ID() + " IN " + "( SELECT "
					+ OBJECT_NAME + "." + ID() + " FROM " + OBJECT_NAME + ","
					+ TMP_TABLE() + " WHERE " + TMP_TABLE() + "." + OBJECT()
					+ "=?" + injectCondition + " AND " + TMP_TABLE() + "."
					+ ID() + "=" + OBJECT_NAME + "." + ID() + " AND ("
					+ TMP_TABLE() + "." + OBJECT_VERSION() + "<>" + OBJECT_NAME
					+ "." + OBJECT_VERSION() + ")" + ")");
		}
	}

	protected void finalize() throws Throwable {
		destroy();
		super.finalize();
	}

	private void initModifiedObjects(Collection excludeObjects) {
		_newObjects = new HashSet(_diff.getNewObjects());
		_changedObjects = new HashSet(_diff.getChangedObjects());
		_deletedObjects = new HashSet(_diff.getDeletedObjects());
		for (Iterator i = excludeObjects.iterator(); i.hasNext();) {
			String objectName = (String) i.next();
			removeConfigObject(_newObjects, objectName);
			removeConfigObject(_changedObjects, objectName);
			removeConfigObject(_deletedObjects, objectName);
		}
		_newObjects = Collections.unmodifiableSet(_newObjects);
		_changedObjects = Collections.unmodifiableSet(_changedObjects);
		_deletedObjects = Collections.unmodifiableSet(_deletedObjects);
	}

	private String getTmpTableName() {
		return _tmpTableName;
	}

	private static synchronized void incUniqId() {
		UNIQ_ID++;
	}

	private DBConnection getDBConnection() {
		return _handler.getConnection();
	}

	private DBAdapter getAdapter() {
		return getDBConnection().getAdapter();
	}

	private String CREATE_STATEMENT(DBAdapter adapter) throws Exception {
		Map attrs = new HashMap();
		if (_handler.getDBConfig().isStringId()) {
			attrs.put(ID, "varchar(100)");
		} else {
			attrs.put(ID, getAdapter().dbtToSQLType(Types.DBT_LONG));
		}
		attrs.put(OBJECT, "varchar(200)");
		attrs.put(OBJ_VERSOIN, "varchar(200)");
		return getAdapter()
				.getCreateTemporaryTableSql(getTmpTableName(), attrs);
	}

	private String CREATE_INDEX_1(DBAdapter adapter) {
		return getAdapter().getCreateIndexSql(getTmpTableName(), "id_obj",
				Arrays.asList(new String[] { ID, OBJECT }), true);
	}

	private String CREATE_INDEX_2(DBAdapter adapter) {
		return getAdapter().getCreateIndexSql(getTmpTableName(), "obj",
				Arrays.asList(new String[] { OBJECT }), false);
	}

	private String INSERT_STATEMENT(DBAdapter adapter) {
		return "INSERT INTO " + TMP_TABLE() + " (" + ID() + "," + OBJECT()
				+ "," + OBJECT_VERSION() + ") VALUES (?,?,?)";
	}

	private String NON_EXISTS_STATEMENT(DBAdapter adapter, String compareTableName) {
		String injectCondition = "";
		if (isInjectMode()) {
			injectCondition = " AND " + OBJECT_VERSION() + " NOT LIKE '%"
					+ _objectVersionNotMatch + "'";
		}
		if  (adapter.isSupportSqlExceptInstruction()) {
			// optimized sql request for Postgres and other advanced DB
			return "SELECT DISTINCT " + ID() + " FROM " + TMP_TABLE() + " WHERE "
			+ OBJECT() + "=? " + injectCondition + " EXCEPT SELECT " + ID()
			+ " FROM " + SQL(compareTableName);
		}
		return "SELECT DISTINCT " + ID() + " FROM " + TMP_TABLE() + " WHERE "
				+ OBJECT() + "=? " + injectCondition + " AND " + ID() +
				" NOT IN ( SELECT " + ID() + " FROM " + SQL(compareTableName) +")";
	}

	/**
	 * ??????? ????????? ??????? ??? ?????? ????????? ? ????????
	 * 
	 * @param hashFile
	 */
	private synchronized void createAndFillTempTable(DumpFile hashFile)
			throws Exception {
		getDBConnection().execute(CREATE_STATEMENT(getAdapter())); // create tmp
		// table
		final DumpReader dumpReader = new DumpReader();
		dumpReader.open(new FileInputStream(hashFile.getFullName()));
		final DumpHeader dumpHeader = dumpReader.getDumpHeader();
		if (!dumpHeader.isHashDump()) {
			throw new Exception("file is not hash-dump");
		}
		if (isInjectMode()) {
			this._objectVersionNotMatch = RepositoryImpl
					.createObjectVersionSuffix(dumpHeader.getRepositoryName(),
							dumpHeader.getDbName());
		}
		_diff = createConfigDiff(dumpHeader);
		initModifiedObjects(_excludeObjects);
		while (dumpReader.nextIsObjectHeader()) {
			final DumpObjectHeader objectHeader = dumpReader.readObjectHeader();
			if (!objectHeader.isSequence()
					&& (objectHeader.indexOfTypeAttr("id") >= 0)) {
				// ?.?. "??????????" ??????
				// insert all rows in transaction
				getDBConnection().getTransactionTemplate().execute(
						new TransactionCallbackWithoutResult() {
							protected void doInTransactionWithoutResult(
									TransactionStatus status) {
								_insertRows(dumpReader, objectHeader);
							}
						});
				_objectNames.add(objectHeader.getObjectName());
			}
		}
		dumpReader.close();
		// create indexes for temporary table, after filling, that's more
		// quickly
		getDBConnection().execute(CREATE_INDEX_1(getAdapter()));
		getDBConnection().execute(CREATE_INDEX_2(getAdapter()));
	}

	private void _insertRows(final DumpReader dumpReader,
			final DumpObjectHeader objectHeader) {
		getDBConnection().execute(INSERT_STATEMENT(getAdapter()),
				new PreparedStatementCallback() {
					public Object doInPreparedStatement(PreparedStatement ps)
							throws SQLException, DataAccessException {
						while (dumpReader.nextIsRow()) {
							try {
								insertObjectData(ps, objectHeader, dumpReader
										.readRow());
							} catch (IOException e) {
								throw new SQLException(e);
							}
						}
						return null;
					}
				});
	}

	private Diff createConfigDiff(DumpHeader dumpHeader) {
		DBConfig config = dumpHeader.getDBConfig();
		Diff diff = new Diff(config, _handler.getDBConfig());
		return diff;
	}

	private void insertObjectData(PreparedStatement statement,
			DumpObjectHeader objectHeader, DumpRow row) throws SQLException {
		statement.clearParameters();
		// id can be String or Integer
		statement.setObject(1, row.getDbValue(
				objectHeader.indexOfTypeAttr("id")).get());
		// object,
		statement.setString(2, objectHeader.getObjectName());
		// objversion
		statement.setString(3, row.getDbValue(
				objectHeader.indexOfTypeAttr(OBJ_VERSOIN)).get().toString());
		statement.execute();
	}

	private void removeConfigObject(Collection objects, String objectName) {
		ConfigObject object = getConfigObject(objects, objectName);
		objects.remove(object);
	}

	private class Where extends SQLWhereAbstract {

		private String objectName;

		private String sql;

		public Where(String objectName, String sql) {
			this.objectName = objectName;
			this.sql = sql;
		}

		public String getSQL() {
			return sql;
		}

		public List<DBValue> getValues() {
			try {
				return Arrays.asList(
						new DBValue[] { DBValue.createInstance(DBValue.STRING, objectName) }
				);
			} catch (DBCastException e) {
				throw new ADVRuntimeException("Cant sql create filter for "
						+ HashDumpAttrCollection.class, e);
			}
		}

		public String getSearchContextSQL() {
			return "";
		}

		public List<DBValue> getSearchContextValues() {
			return Collections.emptyList();
		}

		protected DBConfig getDBConfig() {
			return _handler.getDBConfig();
		}

	}

	private boolean isSelectAllObjects(final String objectName) {
		boolean selectAllObjects;
		selectAllObjects = false;
		if (getConfigObject(_diff.getChangedObjects(), objectName) != null) {
			List newAttrs = _diff.getNewAttributes(objectName);
			for (Iterator i = newAttrs.iterator(); i.hasNext();) {
				ObjectAttr newAttr = (ObjectAttr) i.next();
				if (isRequiredDefaultValue(newAttr)) {
					selectAllObjects = true;
					break;
				}
			}
			List changedAttrs = _diff.getChangedAttributes(objectName);
			for (Iterator i = changedAttrs.iterator(); i.hasNext();) {
				ObjectAttr oldAttr = (ObjectAttr) i.next();
				ObjectAttr newAttr = getNewAttr(objectName, oldAttr);
				if (isRequiredDefaultValue(newAttr)
						&& !isRequiredDefaultValue(oldAttr)) {
					selectAllObjects = true;
					break;
				}
			}
		}
		return selectAllObjects;
	}

	private ConfigObject getConfigObject(final Collection objects,
			final String objectName) {
		ConfigObject result = null;
		for (Iterator i = objects.iterator(); i.hasNext();) {
			ConfigObject object = (ConfigObject) i.next();
			if (object.getName().equals(objectName)) {
				result = object;
				break;
			}
		}
		return result;
	}

	private ObjectAttr getNewAttr(final String objectName, ObjectAttr oldAttr) {
		ObjectAttr newAttr;
		try {
			newAttr = _handler.getDBConfig().getConfigObject(objectName)
					.getAttribute(oldAttr.getName());
		} catch (DBConfigException e) {
			throw new UnreachableCodeReachedException(e);
		}
		return newAttr;
	}

	private boolean isRequiredDefaultValue(ObjectAttr attr) {
		return !attr.isNullable() || attr.isRequired();
	}

	private String SQL(final String ident) {
		return getAdapter().getSQLIdentifier(ident);
	}

	private String OBJECT() {
		return SQL(OBJECT);
	}

	private String ID() {
		return SQL(ID);
	}

	private String OBJECT_VERSION() {
		return SQL(OBJ_VERSOIN);
	}

	private String TMP_TABLE() {
		return SQL(getAdapter().getTemporaryName(getTmpTableName()));
	}

}

