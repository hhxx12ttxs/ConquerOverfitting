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
// $Id: Handler.java 1272 2009-08-14 15:18:37Z vic $
// $Name:  $

package ru.adv.db.handler;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.util.Assert;

import ru.adv.db.DBConnection;
import ru.adv.db.DBException;
import ru.adv.db.MTransaction;
import ru.adv.db.MTransactionManager;
import ru.adv.db.base.DBCastException;
import ru.adv.db.base.FileValue;
import ru.adv.db.base.Id;
import ru.adv.db.base.MAttribute;
import ru.adv.db.base.MCast;
import ru.adv.db.base.MObject;
import ru.adv.db.base.MValue;
import ru.adv.db.base.SingleAncestor;
import ru.adv.db.base.link.LinkPath;
import ru.adv.db.config.ConfigParser;
import ru.adv.db.config.DBConfig;
import ru.adv.db.config.DBConfigException;
import ru.adv.db.config.PersistentEvent;
import ru.adv.db.config.trigger.AbstractJavaTrigger;
import ru.adv.db.config.trigger.TriggerContext;
import ru.adv.db.config.trigger.TriggerException;
import ru.adv.db.filter.FilterException;
import ru.adv.io.atomic.FileTransaction;
import ru.adv.io.atomic.FileTransactionManager;
import ru.adv.logger.TLogger;
import ru.adv.security.AccessDeniedException;
import ru.adv.security.SecurityOptions;
import ru.adv.util.ADVExceptionCode;
import ru.adv.util.ErrorCodeException;
import ru.adv.util.Files;

/**
 * ???? ????? ???????????? ?????????? ? ????? ? ?? ???????? <code>MObject</code>.
 * 
 */

public class Handler implements HandlerI {

	private static final String TREE_ALIAS = "_tree_";

	private static final String ID = "id";

	private static final String ERROR_CODE_KEY = "error-code";

	/**
	 * ????? ?????????? ???????? ?? ????????? ??? ???????
	 * ????????????? ??????? ???????????, ? ???????????? ???????????.
	 */
	public static final int DEFAULT_SAVE_MODE = 0;

	/**
	 * ????? ?????????? ???????? ??? ??????? ??????? ?????? ???????????.
	 */
	public static final int NEWONLY_SAVE_MODE = 1;

	/**
	 * ????? ?????????? ???????? ??? ??????? ??????? ?????? ???????????.
	 */
	public static final int RENEW_SAVE_MODE = 2;

	/** ????? ??????	?????? {@link #selectTree} ???????? siblings  */
	public static final int TREE_SIBLING = 1;
	/** ????? ??????	?????? {@link #selectTree} ???????? parents  */
	public static final int TREE_PARENT = 2;
	/** ????? ??????	?????? {@link #selectTree} ???????? ancestors  */
	public static final int TREE_ANCESTOR = 3;
	/** ????? ??????	?????? {@link #selectTree} ???????? childrens  */
	public static final int TREE_CHILDREN = 4;
	/** ????? ??????	?????? {@link #selectTree} ???????? descendants  */
	public static final int TREE_DESCENDANT = 5;
	
	private static final String UNQUENESS_VIOLATION_MSG = "Uniqueness violation";
	private static final String INVALID_FOREIGN_MSG = "Invalid foreign";

	private TLogger logger = new TLogger(Handler.class);
	private DBConfig _dbConfig;
	private DBConnection _connection;
	private Vector<ObjectModifyListener> modifyListeners = new Vector<ObjectModifyListener>();
	private long _executionTime=0;
	private MTransaction mTransaction = null;
	private MTransactionManager mTransactionManager;
	private String objectVersionSuffix;
	protected boolean _useSecurityChecks = true;

	/**
	 * ??????????? ? ?????????? ??? ????????? <code>DBConnection</code>
	 * @param dbConfig
	 * @param connect
	 */
	public Handler(DBConfig dbConfig, DBConnection connect, String objectVersionSuffix) throws HandlerException {
		this(dbConfig, connect, createMTransactionManager(dbConfig, connect),objectVersionSuffix);
	}

	/**
	 * creates MTransactionManager from properly configured DBConfig and DBConnection
	 * @param dbConfig
	 * @param connect
	 * @return
	 */
	private static MTransactionManager createMTransactionManager(DBConfig dbConfig, DBConnection connect) {
		Assert.notNull(connect.getTransactionTemplate());
		Assert.notNull(dbConfig.getFileStoragePath());
		return new MTransactionManager(
				connect.getTransactionTemplate().getTransactionManager(),
				new FileTransactionManager(dbConfig.getFileStoragePath())
		);
	}

	/**
	 * ??????????? ? ?????????? ??? ????????? <code>DBConnection</code>
	 * @param dbConfig
	 * @param connect
	 */
	public Handler(DBConfig dbConfig, DBConnection connect, MTransactionManager mTransactionManager, String objectVersionSuffix) throws HandlerException {
		_dbConfig = dbConfig;
		_connection = connect;
		Assert.notNull(mTransactionManager);
		this.mTransactionManager = mTransactionManager;
		this.objectVersionSuffix = objectVersionSuffix;
	}

	/* (non-Javadoc)
	 * @see ru.adv.db.handler.HandlerI#isTransactionStarted()
	 */
	public synchronized boolean isTransactionStarted() {
		return this.mTransaction!=null;
	}

	/* (non-Javadoc)
	 * @see ru.adv.db.handler.HandlerI#startTransaction()
	 */
	public synchronized void startTransaction() {
		if (this.mTransaction==null) {
			this.mTransaction = this.mTransactionManager.startTransaction();
	        logger.debug("startTransaction(): transaction="+this.mTransaction);
		}
	}

	/* (non-Javadoc)
	 * @see ru.adv.db.handler.HandlerI#commitTransaction()
	 */
	public synchronized void commitTransaction() {
		if (this.mTransaction!=null) {
	        logger.debug("commitTransaction(): transaction="+this.mTransaction);
			this.mTransaction.commit();
			this.mTransaction = null;
		}
	}

	/* (non-Javadoc)
	 * @see ru.adv.db.handler.HandlerI#rollbackTransaction()
	 */
	public synchronized void rollbackTransaction() {
		if (this.mTransaction!=null) {
	        logger.debug("rollbackTransaction(): transaction="+this.mTransaction);
			this.mTransaction.rollback();
			this.mTransaction.setRollBackOnly();
			this.mTransaction=null;
		}
	}
	
	/* (non-Javadoc)
	 * @see ru.adv.db.handler.HandlerI#getObjectVersionSuffix()
	 */
	public String getObjectVersionSuffix() {
		return objectVersionSuffix;
	}

	/* (non-Javadoc)
	 * @see ru.adv.db.handler.HandlerI#resetExecutionTime()
	 */
	public void resetExecutionTime() {
		_executionTime = 0;
	}

	/* (non-Javadoc)
	 * @see ru.adv.db.handler.HandlerI#getExecutionTime()
	 */
	public long getExecutionTime() {
		return _executionTime;
	}

	/* (non-Javadoc)
	 * @see ru.adv.db.handler.HandlerI#getDBConfig()
	 */
	public DBConfig getDBConfig() {
		return _dbConfig;
	}


	/* (non-Javadoc)
	 * @see ru.adv.db.handler.HandlerI#getConnection()
	 */
	public DBConnection getConnection() {
		return _connection;
	}

	/* (non-Javadoc)
	 * @see ru.adv.db.handler.HandlerI#destroy()
	 */
	public void destroy() {
		if (isTransactionStarted()) {
			logger.warning("destroy not commited tarnsaction");
			rollbackTransaction();
		}
		_dbConfig = null;
		_connection = null;
	}


	/**
	 * for stupid programmer, that not invoke destory() method
	 */
	protected void finalize() throws Throwable {
		destroy();
		super.finalize();
	}

	/* (non-Javadoc)
	 * @see ru.adv.db.handler.HandlerI#selectTree(ru.adv.db.handler.SelectTreeOptions, ru.adv.db.handler.RowSetCallback)
	 */
	public Object selectTree(final SelectTreeOptions options, RowSetCallback rsc) throws HandlerException, AccessDeniedException {
		if (_useSecurityChecks) {
			options.checkPermissions();
		}
		try {
			if (options.getTreeType() != null) {
				return selectTree1(options,rsc);
			}

			// ???????? ??????? ??????
			SQLSelect sql = new SQLSelect(_dbConfig, options.getFilterMap(), true);
			sql.addFrom(options.getObjectName()); // tree context

			// ???????? ?????????? ?????????
			final SQLSelectTree sqlTree = new SQLSelectTree(_dbConfig, options.getMode(), options.getObjectName(), options.getFilterMap(), false);
			// ??????? ?????????? ?? plain ???????? ? tree
			sqlTree.addFilter(options.getObjectName(), ID, options.getPlainIdCollection());

			sql.addFilter(sqlTree.getSqlWhere());

			if (options.isUseRemoved()) {
				sql.addFilter(options.getObjectName(), ConfigParser.REMOVED_ATTR_ID, new MValue(null,"false"));
			}

			// ??????? ????????????? ???????? ??? tree context
			Set<String> selfAttrNames = new HashSet<String>();
			for (MAttribute mAttr : options.getObject().getDefaultSelectAttributes()) {
				sql.addSelect(options.getObjectName(), mAttr.getName());
				selfAttrNames.add(mAttr.getName());
			}

			// ??????? ? sql ????????? foreign ???????
			if (options.isGetForeignAttrs()) {
				appendForeignObjects(sql, options.getObject(), selfAttrNames);
			}

			try {
				return sql.execute(_connection,rsc);
			} finally {
				_executionTime += sql.getExecutionTime();
			}

		} catch (AccessDeniedException e) {
			throw e;
		} catch (ErrorCodeException e) {
			throw new HandlerException(e);
		} catch (Exception e) {
			throw new HandlerException(HandlerException.DB_CANNOT_SELECT, e, getDBConfig().getId());
		}
	}


	/**
	 * ???????? id ??? ????????? ???????? ?? MValueCollection ???? ??????? ? id ? plainIdCollection,
	 * ? id ????????, ??????? ????????? ? ??????????? ????????? ?????????? tree
	 *  tree ????????? ????? ???????? ?? plainIdCollection
	 */
	private Object selectTree1(SelectTreeOptions options, RowSetCallback rsc) throws HandlerException {
		try {

			/*==========================================
			 SELECT 
			  i.id AS i_id,
			  i.title AS i_title
			 FROM 
			   i i,
			 (
			   (SELECT `_t_`.id FROM i `_t_` WHERE `_t_`.id IN(37)) 
			    UNION 
			   (SELECT `_t_`.tree FROM i `_t_` WHERE `_t_`.id IN(37) AND `_t_`.tree IS NOT NULL) 
			    UNION 
			   (SELECT `_t_`.id FROM i `_t_` WHERE `_t_`.tree IN(37))
			 ) _tree_id_
			 WHERE
			  i.id=_tree_id_.id
			 ORDER BY i.title AS i_title DESC
			 ============================================*/

			// ???????? ??????? ??????
			SQLSelect sql = new SQLSelect(_dbConfig, options.getFilterMap(), true);
			sql.addFrom( 
					new SQLWhereTree(_dbConfig, options.getTreeType(), options.getObjectName(), options.getPlainIdCollection(), options.getFilterMap()),
					TREE_ALIAS
			);
			sql.addSelect(options.getObjectName(), ID);
			sql.addFilter(options.getObjectName(), ID, TREE_ALIAS, ID, "=");
			sql.addOrder(options.getSort());
			try {
				return sql.execute(_connection,rsc);
			} finally {
				_executionTime += sql.getExecutionTime();
			}

		} catch (ErrorCodeException e) {
			throw new HandlerException(e);
		} catch (Exception e) {
			throw new HandlerException(HandlerException.DB_CANNOT_SELECT, e, getDBConfig().getId());
		}
	}
	
	/* (non-Javadoc)
	 * @see ru.adv.db.handler.HandlerI#select(java.lang.String, java.lang.Object, ru.adv.security.SecurityOptions)
	 */
	public MObject select(String objectName, Object id, SecurityOptions securityOptions) {
		
		final MObject obj = getDBConfig().createMObject(objectName, null);
		
		SelectOptions options = new SelectOptions( obj,	securityOptions	);
		options.setGetForeignAttrs(false);
		
		SelectAttributes selectAttributes = new SelectAttributes();
		SelectAttributesItem saItems = new SelectAttributesItem(obj.getRealName(), obj.getName(), true);
		saItems.addAttributes(obj.getConfigObject().getAttributeNames());
		selectAttributes.add(saItems);
		
		options.setSelectAttributes(selectAttributes);
		
		SingleAncestor ancestor = new SingleAncestor();
		ancestor.set(obj.getName(), ID, new MValue(id,id));
		
		options.setAncestor(ancestor);
		
		MObject object = (MObject)select( options, new RowSetCallback(){

			@Override
			public Object doInRowSet(RowSet rs) throws Exception {
				if (rs.next()) {
					return rs.getMObject(obj.getName());
				}
				return null;
			}
			
		});
		
		return object;
	}
	
	/* (non-Javadoc)
	 * @see ru.adv.db.handler.HandlerI#select(ru.adv.db.handler.SelectOptions, ru.adv.db.handler.RowSetCallback)
	 */
	@Override
	public Object select(SelectOptions options, RowSetCallback rsc) throws HandlerException, AccessDeniedException {
		return _select(options, rsc, false);
	}
	
	@Override
	public long selectCount(SelectOptions options) throws HandlerException, AccessDeniedException {
		return (Long)_select(options, null, true);
	}

	/**
	 * 
	 * @param options
	 * @param rsc
	 * @param isCountOnly do "SELECT count(*) FROM ..." instead real request 
	 * @return
	 * @throws HandlerException
	 * @throws AccessDeniedException
	 */
	private Object _select(SelectOptions options, RowSetCallback rsc, boolean isCountOnly) throws HandlerException, AccessDeniedException {
		if (_useSecurityChecks) {
			options.checkPermissions();
		}
		try {
			SQLSelect sql = new SQLSelect(_dbConfig, options.getFilterMap(), true);
			sql.setCountOnly(isCountOnly);
			sql.setDistinct(options.isDistinct());
			sql.setDistinctByOrder(options.isDistinctByOrder());

			// ??????? ????????????? ????????
			constructSelectFrom(sql, options.getObject(), options.getObjectName(), options.getSelectAttributes());

			if (options.getSelectAttributes() != null) {
				for (String objName : options.getSelectAttributes().getObjects()) {
					if (objName.equals(options.getObjectName())) {
						continue;
					}
					SelectAttributesItem item = options.getSelectAttributes().get(objName);
					MObject obj = _dbConfig.createMObject(item.getName(), options.getFilterMap());
					constructSelectFrom(sql, obj, item.getAlias(), options.getSelectAttributes());
				}
			}

			// ?????? SQLSelect ????? ????????????? ???????? foreign ???????
			// ? ?????????? ??? ??????? ??????????
			if (options.isGetForeignAttrs()) {
				appendForeignObjects(sql, options.getObject(), options.getSelfAttrs());
			}

			// ???????? ????? ????? ?????????
			if (options.getPaths() != null) {
				for (LinkPath linkPath : options.getPaths().getLinkPaths()) {
					sql.addLink(linkPath);
				}
			}

			// ???????? ??????????? ?? ancestor, ?????? ?? ???????,
			// ??????? ??? ???? ? select
			if (options.getAncestor() != null) {
				for (String aliasName : sql.getAliases()) {
					if (options.getAncestor().exists(aliasName)) {
						for (String attrName : options.getAncestor().objectAttributes(aliasName)) {
							sql.addFilter(aliasName, attrName, options.getAncestor().get(aliasName, attrName));
						}
					}
				}
			}

			// ??????? ??????????? ?????, ???? ?? ???????
			if (options.getSContextCollection() != null) {
				for (Iterator<SContext> i = options.getSContextCollection().iterator(); i.hasNext();) {
					sql.addSearchContext(i.next());
				}
			}

			// add Extra Filter
			if (options.getExtraSQLWhere()!=null) {
				sql.addFilter(options.getExtraSQLWhere());
			}

			// ???????? ??????????? ?? page
			sql.setLimit(options.getLimit());

			if (options.isUseRemoved()) {
				sql.addFilter(options.getObjectName(), 
						ConfigParser.REMOVED_ATTR_ID, new MValue(null,"false")
				);
			}

			sql.addOrder(options.getSort());

			try {
				return sql.execute(_connection, rsc);
			} finally {
				_executionTime += sql.getExecutionTime();
			}

		} catch (ErrorCodeException e) {
			throw new HandlerException(e);
		} catch (Throwable e) {
			throw new HandlerException(HandlerException.DB_CANNOT_SELECT, e, getDBConfig().getId());
		}
	}

	/* (non-Javadoc)
	 * @see ru.adv.db.handler.HandlerI#save(ru.adv.db.handler.SaveOptions)
	 */
	public synchronized Id save(final SaveOptions options) throws HandlerException, AccessDeniedException {

		final boolean isDoCommitHere = !isTransactionStarted();

		if (isDoCommitHere) {
			startTransaction(); 
		}
		
		try {
			
			Id result = (Id) _connection.getTransactionTemplate().execute(
					new TransactionCallback(){
						@Override
						public Object doInTransaction(TransactionStatus status) {
							return _save(options);
						}
					});
			if (isDoCommitHere) {
				commitTransaction(); 
			}
			return result;
			
		} catch (RuntimeException e) {
			if (isDoCommitHere) {
				rollbackTransaction(); 
			}
			throw e;
		}

	}

	/**
	 * 
	 */
	private Id _save(SaveOptions options) throws ErrorCodeException {

		try {
			Id id = null;
			if (options.isSearchMode()) {
				SearchOptions searchOptions = new SearchOptions(options.getObject(), _useSecurityChecks ? options.getSecurityOptions() : null);
				searchOptions.setTrowException(false);
				id = findUniqueId(searchOptions);
				if (id != null) {
					// update
					options.getObject().setId(id);
					updateSingle(id, options);
				} else if (options.isUpdateMode()) {
					throw new ObjectNotFoundException("Cannot renew object " + options.getObjectName() + ": object not found", options.getObject(), searchOptions.getUniqueAttributes());
				}
			}
			if (id == null && options.isInsertMode()) {
				// insert
				id = insertSingle(options);
				options.getObject().getAttribute(ID).setDBValue(new MValue(id));
			}
			return id;
		} catch (ErrorCodeException e) {
			throw e;
		} catch (Throwable e) {
			throw new HandlerException(0,e);
		}
	}


	/* (non-Javadoc)
	 * @see ru.adv.db.handler.HandlerI#delete(ru.adv.db.handler.DeleteOptions)
	 */
	public void delete(DeleteOptions options) throws HandlerException, AccessDeniedException {
		delete(options, true);
	}

	/**
	 * DO delete in transaction
	 * @param options
	 * @param isUseTriggers
	 * @throws HandlerException
	 * @throws AccessDeniedException
	 */
	protected synchronized void delete(final DeleteOptions options, final boolean isUseTriggers) throws HandlerException, AccessDeniedException {

		final boolean isDoCommitHere = !isTransactionStarted();

		if (isDoCommitHere) {
			startTransaction(); 
		}

		try {

			getConnection().getTransactionTemplate().execute(new TransactionCallback(){
				@Override
				public Object doInTransaction(TransactionStatus status) {
					_delete(options, isUseTriggers);
					return null;
				}
			});

			if (isDoCommitHere) {
				commitTransaction();
			}

		} catch (RuntimeException e) {
			if (isDoCommitHere) {
				rollbackTransaction();
			}
			throw e;
		}

	}


	/**
	 * ???????? ??????? ?? ?? ?? ??? ???????? id
	 */
	private void _delete(DeleteOptions options, boolean isUseTriggers) throws ErrorCodeException {

		if (_useSecurityChecks) {
			options.checkPermissions();
		}

		final MObject object = options.getObject();
		try {
			SQLDelete sql = SQLDelete.create(object, _dbConfig.createId(options.getId()));
			try {
				MObject oldObject = null;
				final boolean fireTriggers = isUseTriggers && isHasJavaTriggers(object);
				if (fireTriggers) {
					oldObject = select( object.getName(), options.getId(), options.getSecurityOptions());
					fireJavaTriggers(PersistentEvent.BEFORE_DELETE, object, oldObject, options.getSecurityOptions());
				}
				sql.execute(_connection);
				if (fireTriggers) {
					fireJavaTriggers(PersistentEvent.AFTER_DELETE, object, oldObject, options.getSecurityOptions());
				}
			} catch (ErrorCodeException e) {
				throw e;
			}
			finally {
				_executionTime += sql.getExecutionTime();
			}
			for (MAttribute ma : object.getAttributes()) {
				if (ma.getType() == ru.adv.db.adapter.Types.FILE)
					removeDir(_dbConfig.getFileStoragePath() +
							File.separator +
							object.getRealName() +
							File.separator +
							ma.getName() +
							File.separator +
							options.getId()
					);
			}
			if (isUseTriggers) {
				modifyObjectPerform(object);
			}

		} catch (Throwable e) {
			HandlerException ex = new HandlerException(HandlerException.DB_CANNOT_DELETE, e, getDBConfig().getId());
			ex.setObject(object.getName());
			ex.setId(options.getId());
			throw ex;
		}
	}

	/* (non-Javadoc)
	 * @see ru.adv.db.handler.HandlerI#findUniqueId(ru.adv.db.handler.SearchOptions)
	 */
	public Id findUniqueId(SearchOptions options) throws HandlerException, AccessDeniedException {
		if (_useSecurityChecks) {
			options.checkPermissions();
		}
		Id id = null;
		HashSet<String> searchAttrNames;
		Set<MAttribute> uniqueAttrs = options.getUniqueAttributes();
		final MObject object = options.getObject();

		try {
			SQLSelect sql = new SQLSelect(_dbConfig, object.getFilterMap(), true);
			sql.addFrom(object.getName());
			sql.addSelect(object.getName(), ID);
			searchAttrNames = new HashSet<String>();
			for (MAttribute mAttr : uniqueAttrs) {
				MValue mValue = new MValue();
				if (mAttr.isSearchNotRegion()) {
					mValue.setSearchValue(mAttr.getValue().getSearchValue());
				}else{
					mValue.setSearchValue(mAttr.getValue().get());
				}
				sql.addFilter(object.getName(), mAttr.getName(), mValue);
				searchAttrNames.add(mAttr.getName());
			}
			try {
				id = (Id)sql.execute(_connection, new RowSetCallback(){
					public Object doInRowSet(RowSet rowSet) throws Exception {
						if (rowSet.next()) {
							return rowSet.getMObject(object.getName()).getId();
						}
						return null;
					}
				});
			} finally {
				_executionTime += sql.getExecutionTime();
			}
			// ????????????? serche attributes
			object.resetSearchAttrs();
			object.setSearchAttrs(searchAttrNames);

		} catch (ErrorCodeException e) {
			e.setObject(object.getName());
			throw e;
		} catch (Throwable e) {
			HandlerException ex = new HandlerException(HandlerException.DB_CANNOT_FIND, e, getDBConfig().getId());
			ex.setObject(object.getName());
			throw ex;
		}

		if (options.isTrowException() && id == null && (uniqueAttrs == null || uniqueAttrs.size() > 0)) {
			throw new ObjectNotFoundException(
					new StringBuffer().append("Object '").append(object.getName()).append("' not found").toString(),
					object,
					uniqueAttrs
			);
		}

		return id;
	}

	/**
	 * ????????? ? SQLSelect ??????? ??? ????????? foreign objects
	 * @param sql SQLSelect
	 * @param object
	 * @param selfAttrNames ????? ???? ????????? object, ??????? ??? ???????
	 *   ??? ??????? ? sql
	 */
	private void appendForeignObjects(SQLSelect sql, MObject object, Set<String> selfAttrNames)
	throws DBException, DBConfigException, SQLStatementException {
		int k = 1;
		for (String selfAttrName : selfAttrNames) {
			MAttribute attr = object.getAttribute(selfAttrName);
			if (attr.isForeign() && attr.isTagLayout()) {
				String foreignObjectName = attr.getForeignObjectName();
				String foreignAlias = "f$" + Integer.toString(k++);
				MObject fObject = _dbConfig.createMObject(foreignObjectName, object.getFilterMap());
				constructSelectFrom(sql, fObject, foreignAlias, null);
				sql.addFilter(object.getName(), attr.getName(), foreignAlias, ID, "=");
				sql.addForeignAlias(foreignAlias);
			}
		}
	}

	/**
	 * ????????? ? <code>sql</code> ????????????? ???????? ?????? ??? object, c ??????? alias
	 */
	private static void constructSelectFrom(SQLSelect sql, MObject object, String alias, SelectAttributes selectAttrs) throws SQLStatementException {
		// add selected attributes
		SelectAttributesItem item = null;
		if (selectAttrs != null)
			item = selectAttrs.get(alias);

		sql.addFrom(object.getName(), alias);

		boolean add = (item != null && item.theseOnly());
		if (item == null || !item.theseOnly()) {
			for (MAttribute mAttr : object.getDefaultSelectAttributes()) {
				sql.addSelect(alias, mAttr.getName());
			}
			if (item != null) {
				add = true;
			}
		}
		if (add) {
			for (String attrName : item.getAttributes()) {
				sql.addSelect(alias, attrName);
			}
		}
	}

	/**
	 * ?????????? ?????? ??????? ? ??
	 * @return id ???????????? ???????
	 */
	private Id insertSingle(SaveOptions options) throws HandlerException, AccessDeniedException {
		if (_useSecurityChecks) {
			options.checkInsertPermissions();
		}
		Id id = null;
		try {

			fireJavaTriggers(PersistentEvent.BEFORE_INSERT, options.getObject(), null, options.getSecurityOptions());

			SQLInsert sql = createInsertSql(options);
			if ( getDBConfig().isNativeSystemTriggers() ) {
				// ??? ???? ?????????? ????? ??????? Id ?? ???????
				// ?????? ??? sequence ????? ???? ????????? ?????????, ???????? ??????????? ?? insert
				id = getNextID();
			} else {
				// it's already set by BISystemTrigger
				id = options.getObject().getId();
			}
			sql.setId(id);
			sql.execute(_connection);
			processFiles(id, options.getObjectName(), sql.getFileAttrMap());

			fireJavaTriggers(PersistentEvent.AFTER_INSERT, options.getObject(), null, options.getSecurityOptions());

			logger.debug("id=" + id);
			_executionTime += sql.getExecutionTime();
			modifyObjectPerform(options.getObject());

		} catch (Throwable e) {
			logger.logStackTrace(e);
			throw newHanlerException(HandlerException.DB_CANNOT_INSERT, e, options.getObject());
		}
		return id;
	}

	private SQLInsert createInsertSql(SaveOptions options) {
		SQLInsert sql;
		sql = SQLInsert.create(options.getObject());
		for (MAttribute mAttr : options.getObject().getAttributes()) {
			if (mAttr.getName().equals(ID)) {// cannot update id
				continue;
			}
			if (!mAttr.isSet() && mAttr.isFile() && !mAttr.isRequired()) {
				try {
					mAttr.setValue(new MValue(new Boolean(false)));
				} catch (Exception e) {
				}
			}
			if (mAttr.isSet()) {
				sql.addAttribute(mAttr);
			}
			checkRequired(mAttr, options, true);
		}
		return sql;
	}

	private void checkRequired(MAttribute mAttr, SaveOptions options, boolean inserting) throws HandlerException {
		boolean required = false;
		if (mAttr.isSet()) {
			try {
				if (mAttr.isString()) {
					required = isNotNullAndRequired(mAttr) && mAttr.getValue().get().toString().length() == 0;
				} else if (mAttr.isFile()) {
					required = isNotNullAndRequired(mAttr) && !mAttr.getValue().isFile();
				} else if (mAttr.isBoolean()) {  // Note: .isBoolean() returns true for boolean and file types
					required = isNotNullAndRequired(mAttr) && !(MCast.toBoolean(mAttr.getValue().get())).booleanValue();
				}
			} catch (ErrorCodeException e) {
				throw new HandlerException(e);
			}
		} else if (inserting) {
			required = mAttr.isRequired() && !mAttr.isHasDefaultValue();
		}
		if (required) {
			HandlerException e = new HandlerException(
					ADVExceptionCode.REQUIRED_ATTRIBUTE,
					String.format("Attribute %1$s.%2$s is required", options.getObjectName(), mAttr.getName()),
					getDBConfig().getId()
			);
			e.setObject(options.getObjectName());
			e.setAttribute(mAttr.getName());
			throw e;
		}
	}

	private boolean isNotNullAndRequired(MAttribute mAttr) throws FilterException, DBCastException {
		return mAttr.isRequired() && mAttr.getValue().get() != null;
	}

	/* (non-Javadoc)
	 * @see ru.adv.db.handler.HandlerI#getNextID()
	 */
	public Id getNextID() {
		long t = System.currentTimeMillis();
		try {
			return _connection.getNextID(
					getDBConfig().getSchemaName(), 
					getDBConfig().getDBAdapter().getCommonSequenceName()
			);
		}
		finally {
			_executionTime += System.currentTimeMillis() - t;
		}
	}


	/**
	 * ????????? ??? ???????????? ??????? ? ??
	 */
	private int updateSingle(Id id, SaveOptions options) throws ErrorCodeException {
		if (_useSecurityChecks) {
			options.checkUpdatePermissions();
		}
		try {
			
			int result = 0;
			
			Object idValue =  getDBConfig().isStringId() ? id.getStringId() : id.getLongId() ;
			MObject oldObject = null;
			final boolean hasJavaTriggers = isHasJavaTriggers(options.getObject());
			
			if (hasJavaTriggers) {
				oldObject = select( options.getObject().getName(), idValue, options.getSecurityOptions());
				fireJavaTriggers(PersistentEvent.BEFORE_UPDATE, options.getObject(), oldObject, options.getSecurityOptions());
			}
			
			SQLUpdate sql = createUpdateSql(id, options);
			try {
				result = sql.execute(_connection);
			} finally {
				_executionTime += sql.getExecutionTime();
			}
			processFiles(id, options.getObjectName(), sql.getFileAttrMap());

			if (hasJavaTriggers) {
				fireJavaTriggers(PersistentEvent.AFTER_UPDATE, options.getObject(), oldObject, options.getSecurityOptions());
			}
			
			modifyObjectPerform(options.getObject());
			
			return result;

		} catch (Throwable e) {
			throw newHanlerException(HandlerException.DB_CANNOT_UPDATE, e, options.getObject());
		}
	}

	private SQLUpdate createUpdateSql(Id id, SaveOptions options) {
		SQLUpdate sql;
		sql = SQLUpdate.create(options.getObject());
		sql.addFilterById(new MValue(id));
		for (MAttribute mAttr : options.getObject().getAttributes()) {
			if (mAttr.isSetAndSearch() || (mAttr.isSet() && !options.getObject().isSearchAttr(mAttr.getName()))) {
				sql.addUpdate(mAttr);
				checkRequired(mAttr, options, false);
			}
		}
		return sql;
	}

	/* (non-Javadoc)
	 * @see ru.adv.db.handler.HandlerI#processFiles(ru.adv.db.base.Id, java.lang.String, java.util.Map)
	 */
	public void processFiles(Id id, String objectName, Map<MAttribute,FileValue> fileValues) throws HandlerException {
		try {
			if (fileValues.size() == 0) {
				return; // nothing to do
			}
			for (MAttribute attr : fileValues.keySet() ) {
				String destDir = _dbConfig.getFileStoragePath();
				if (destDir == null) {
					throw new HandlerException(HandlerException.DB_UNDEFINED_FILE_STORAGE, "File storage directory not defined", getDBConfig().getId());
				}
				destDir += File.separator + attr.getAttrStoragePath() + File.separator + id;
				FileValue fv = fileValues.get(attr);
				if (fv == null) {
					removeDir(destDir);
				}
				else {
					prepareDir(destDir);
					String filename = null;
					String mime = attr.getMIME();
					if (mime != null && mime.length() > 0) {
						filename = constructFuleName(id, mime);
					}
					fv.copyTo(getCurrentFileTransaction(), destDir, filename, true);
				}
			}
		}
		catch (ErrorCodeException e) {
			throw new HandlerException(e);
		}
	}

	private String constructFuleName(Id id, String mime) {
		String filename;
		StringBuffer sb = new StringBuffer();
		sb.append(id);
		sb.append('.');
		sb.append(mime);
		filename = sb.toString();
		return filename;
	}

	private void prepareDir(String destDir) throws HandlerException {
		// make directory if needed
		File d = new File(destDir);
		if (!d.exists() && !d.mkdirs()) {
			HandlerException ex = new HandlerException(HandlerException.IO_CANNOT_CREATE, "Cannot create directory '" + destDir + "'", getDBConfig().getId());
			ex.setDirectory(destDir);
			throw ex;
		}
		// clean up directory content if any
		String[] files = d.list();
		if (files==null) {
			HandlerException ex = new HandlerException(HandlerException.IO_ERROR, "It isn't directory: '" + destDir + "'", getDBConfig().getId());
			ex.setDirectory(destDir);
			throw ex;
		}
		for (int i = 0; i < files.length; i++) {
			File f = new File(destDir + File.separator + files[i]);
			final FileTransaction currentFileTransaction = getCurrentFileTransaction();
			if (currentFileTransaction != null) {
				try {
					currentFileTransaction.delete(f);
				} catch (ErrorCodeException e) {
					throw new HandlerException(e);
				}
			} else {
				if (!f.delete()) {
					HandlerException ex = new HandlerException(HandlerException.IO_CANNOT_REMOVE, "Cannot remove file '" + f.getPath() + "'", getDBConfig().getId());
					ex.setFilename(f.getPath());
					throw ex;
				}
			}
		}
	}

	private void removeDir(String s) throws HandlerException {
		File d = new File(s);
		if (d.exists()) {
			// remove directory
			final FileTransaction currentFileTransaction = getCurrentFileTransaction();
			if (currentFileTransaction != null) {
				try {
					currentFileTransaction.delete(d);
				} catch (ErrorCodeException e) {
					throw new HandlerException(e);
				}
			} else {
				if (!Files.remove(d, true)) {
					HandlerException ex = new HandlerException(HandlerException.IO_CANNOT_REMOVE, "Cannot remove directory '" + d.getPath() + "'", getDBConfig().getId());
					ex.setDirectory(d.getPath());
					throw ex;
				}
			}
		}
	}

	private FileTransaction getCurrentFileTransaction() {
		if (this.mTransaction!=null) {
			return this.mTransaction.getFileTransaction();
		}
		return null; 
	}

	/* (non-Javadoc)
	 * @see ru.adv.db.handler.HandlerI#addObjectModifyListener(ru.adv.db.handler.ObjectModifyListener)
	 */
	public void addObjectModifyListener(ObjectModifyListener listener) {
		modifyListeners.add(listener);
	}

	/**
	 * ??????? ????????? ?? ????????? ???????
	 * @param mObject ?????????? ??????
	 */
	private void modifyObjectPerform(MObject mObject) {
		for (ObjectModifyListener listener : modifyListeners) {
			listener.objectModifyPerformed(mObject.getDatabaseName(), mObject.getRealName());
		}
	}

	private void fireJavaTriggers(PersistentEvent event, MObject object, MObject oldObject, SecurityOptions securityOptions) throws TriggerException {
		final List<AbstractJavaTrigger> triggerList = object.getConfigObject().getJavaTriggers(event);
		if (triggerList!=null && !triggerList.isEmpty()) {
			final TriggerContext context = new TriggerContext( this, event, object, oldObject, securityOptions );
			for( AbstractJavaTrigger trigger : triggerList ) {
				if (logger.isDebugEnabled()) {
					logger.debug("Fire java trigger "+trigger.getId()+" on "+event+ " for "+object.getName() );
				}
				trigger.execute( context );
			}
		}
	}
	
	private boolean isHasJavaTriggers(MObject object) {
		return !object.getConfigObject().getJavaTriggers().isEmpty();
	}

	private ErrorCodeException newHanlerException(int code, Throwable e, MObject object) {
		
		ErrorCodeException result;
		Map<String,Object> extraInfoMap = extractExceptionInfo(e, object);
		
		if (e instanceof ErrorCodeException && code==((ErrorCodeException)e).getCode()) {
			// use previous created exception
			result = (ErrorCodeException)e; 
		} else if (extraInfoMap!=null) {
			result = createExceptionFromExtraInfo(e, extraInfoMap);
		} else {
			result = new HandlerException(code, e, getDBConfig().getId());
		}
		
		result.setObject(object.getName());
		return result;
	}

	private ErrorCodeException createExceptionFromExtraInfo(Throwable e, Map<String, Object> extraInfoMap) {
		ErrorCodeException result;
		int errorCode = ((Number) extraInfoMap.get(ERROR_CODE_KEY)).intValue();
		switch (errorCode) {
		case ADVExceptionCode.UNIQUENESS_VIOLATION:
			result = new HandlerException(errorCode, UNQUENESS_VIOLATION_MSG, e, getDBConfig().getId());
			break;
		case ADVExceptionCode.INVALID_FOREIGN:
			result = new HandlerException(errorCode, INVALID_FOREIGN_MSG, e, getDBConfig().getId());
			break;
		default:
			result = new HandlerException(errorCode, e, getDBConfig().getId());
			break;
		}
		extraInfoMap.remove(ERROR_CODE_KEY);
		result.addAttrs(extraInfoMap);
		return result;
	}
	
	private Map<String,Object> extractExceptionInfo(Throwable e, MObject object) {
		if (getDBConfig().getDBAdapter().supportsAdditionalInfoExtraction()) { 
			Map<String,Object> attrs = getDBConfig().getDBAdapter().extractAdditionalInfo(e, object);
			Integer errorCode = (Integer) attrs.get(ERROR_CODE_KEY);
			if (errorCode!=null) {
				return attrs;	
			}
		}
		return null;
	}

}




