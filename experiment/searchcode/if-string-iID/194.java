//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.server.masterdata.ejb3;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.nuclos.api.service.MessageContextService;
import org.nuclos.common.E;
import org.nuclos.common.EntityMeta;
import org.nuclos.common.FieldMeta;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SF;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.UID;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.LogicalOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.MasterDataToEntityObjectTransformer;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.Delete;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.IDependentDataMap;
import org.nuclos.common.dblayer.DbObjectMessage;
import org.nuclos.common.entityobject.CollectableEOEntityField;
import org.nuclos.common.lucene.ILucenian;
import org.nuclos.common.transport.GzipList;
import org.nuclos.common2.EntityAndField;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.TruncatableCollectionDecorator;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.attribute.ejb3.AttributeFacadeLocal;
import org.nuclos.server.attribute.ejb3.LayoutFacadeBean;
import org.nuclos.server.autosync.XMLEntities;
import org.nuclos.server.common.LocalCachesUtil;
import org.nuclos.server.common.MetaProvider;
import org.nuclos.server.common.NuclosPerformanceLogger;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.common.RecordGrantUtils;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.dal.DalSupportForGO;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.database.SpringDataBaseHelper;
import org.nuclos.server.dblayer.DbAccess;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbObjectHelper;
import org.nuclos.server.dblayer.DbObjectHelper.DbObjectType;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.DbType;
import org.nuclos.server.dblayer.MetaDbHelper;
import org.nuclos.server.dblayer.statements.DbStatement;
import org.nuclos.server.dblayer.statements.DbStructureChange;
import org.nuclos.server.dblayer.structure.DbTable;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeLocal;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.history.ejb3.HistoryFacadeLocal;
import org.nuclos.server.jms.NuclosJMSUtils;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.report.ejb3.DatasourceFacadeLocal;
import org.nuclos.server.resource.ResourceCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper class for the MasterDataFacade.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
@Component
public class MasterDataFacadeHelper {
	
	private static final Logger LOG = Logger.getLogger(MasterDataFacadeHelper.class);

	/**
	 * @deprecated Does not work with multi nuclet. (tp)
	 */
	private static final List<String> lstSystemDbFieldNames =
			Arrays.asList("intid", "datcreated", "strcreated", "datchanged", "strchanged", "intversion");

	static final int MAXROWS = 100;

	public static enum RoleDependant {

		ROLE_ACTION(E.ROLEACTION, "22292", E.ACTION.action, null),
		ROLE_MODULE(E.ROLEMODULE, "22293", E.ROLEMODULE.module, E.ROLEMODULE.group),
		ROLE_MASTERDATA(E.ROLEMASTERDATA, "22294", E.ROLEMASTERDATA.entity, null),
		ROLE_USER(E.ROLEUSER, "22290", E.ROLEUSER.user, null),
		ROLE_REPORT(E.ROLEREPORT, "22295", E.ROLEREPORT.report, null),
		ROLE_GENERATION(E.ROLEGENERATION, "22296", E.ROLEGENERATION.generation, null),
		ROLE_RECORDGRANT(E.ROLERECORDGRANT, "22297", E.RECORDGRANTUSAGE.recordGrant, null);

		private final EntityMeta<UID> entity;
		private final String resourceId;
		private final FieldMeta<?> entityFieldName;
		private final FieldMeta<?> subFieldName;

		private RoleDependant(EntityMeta<UID> entity, String resourceId, FieldMeta<?> entityFieldName, FieldMeta<?> subFieldName) {
			this.entity = entity;
			this.resourceId = resourceId;
			this.entityFieldName = entityFieldName;
			this.subFieldName = subFieldName;
		}

		public EntityMeta<UID> getEntity() {
			return entity;
		}

		public String getResourceId() {
			return resourceId;
		}

		public FieldMeta<?> getEntityFieldName() {
			return entityFieldName;
		}

		public FieldMeta<?> getSubFieldName() {
			return subFieldName;
		}

		public static RoleDependant getByEntityName(UID entityUid) {
			for (RoleDependant u : RoleDependant.class.getEnumConstants()) {
				if (u.getEntity().checkEntityUID(entityUid)) {
					return u;
				}
			}
			return null;
		}
	}
	
	//
	
	private HistoryFacadeLocal historyFacade;
		
	private AttributeFacadeLocal attributeFacade;

	private RecordGrantUtils grantUtils;
	
	private MetaProvider metaProvider;
	
	private SpringDataBaseHelper dataBaseHelper;
	
	private NucletDalProvider nucletDalProvider;
	
	private DatasourceFacadeLocal datasourceFacade;
	
	private MessageContextService messageService;
	
	public MasterDataFacadeHelper() {
	}
	
	private DatasourceFacadeLocal getDatasourceFacade() {
		if (this.datasourceFacade == null)
			this.datasourceFacade = ServerServiceLocator.getInstance().getFacade(DatasourceFacadeLocal.class);
		return this.datasourceFacade;
	}
	
	private HistoryFacadeLocal getHistoryFacade() {
		if (this.historyFacade == null)
			this.historyFacade = ServerServiceLocator.getInstance().getFacade(HistoryFacadeLocal.class);
		return this.historyFacade;
	}
	
	@Autowired
	void setRecordGrantUtils(RecordGrantUtils grantUtils) {
		this.grantUtils = grantUtils;
	}
	
	@Autowired
	void setMasterDataMetaCache(MetaProvider masterDataMetaCache) {
		this.metaProvider = masterDataMetaCache;
	}
	
	@Autowired
	void setDataBaseHelper(SpringDataBaseHelper dataBaseHelper) {
		this.dataBaseHelper = dataBaseHelper;
	}
	
	@Autowired
	void setNucletDalProvider(NucletDalProvider nucletDalProvider) {
		this.nucletDalProvider = nucletDalProvider;
	}
	
	@Autowired
	void setMessageContextService(MessageContextService messageService) {
		this.messageService = messageService;
	}

	public void notifyClients(UID cachedEntityUid) {
		if (cachedEntityUid == null) {
			throw new NullArgumentException("sCachedEntityName");
		}
		LOG.info("JMS send: notify clients that master data changed:" + this);
		LocalCachesUtil.getInstance().updateLocalCacheRevalidation(JMSConstants.TOPICNAME_MASTERDATACACHE);
		NuclosJMSUtils.sendOnceAfterCommitDelayed(cachedEntityUid, JMSConstants.TOPICNAME_MASTERDATACACHE);
	}
	
	public <PK> MasterDataVO<PK> getMasterDataCVOById(final EntityMeta<?> mdmetavo, final PK oId) throws CommonFinderException {
		return getMasterDataCVOById(mdmetavo, oId, true);
	}

	public <PK> MasterDataVO<PK> getMasterDataCVOById(final EntityMeta<?> mdmetavo, final PK oId, boolean checkRecordGrant) throws CommonFinderException {
		MasterDataVO<?> mdVO = XMLEntities.getSystemObjectById(mdmetavo.getUID(), oId);
		if (mdVO != null) {
			return (MasterDataVO<PK>) mdVO;
		}

		JdbcEntityObjectProcessor<PK> eoProcessor = (JdbcEntityObjectProcessor<PK>) 
				nucletDalProvider.getEntityObjectProcessor(mdmetavo);
		EntityObjectVO<PK> eoResult = eoProcessor.getByPrimaryKey(oId);
		if (checkRecordGrant) {
			try {
				grantUtils.checkInternal(mdmetavo.getUID(), oId);
	        }
	        catch(CommonPermissionException e) {
	        	throw new CommonFinderException(e);
	        }
		}
		if (eoResult != null) {
			mdVO = DalSupportForMD.wrapEntityObjectVO(eoResult);
		}

		if (mdVO == null) {
			throw new CommonFinderException("Can't find " + mdmetavo.getEntityName() + " with id " + oId);
		}

		return (MasterDataVO<PK>) mdVO;
	}

	/**
	 * gets the dependant master data records for the given entity, using the given foreign key field and the given id as foreign key.
	 * @param entity name of the entity to get all dependant master data records for
	 * @param sForeignKeyField name of the field relating to the foreign entity
	 * @param oRelatedId id by which sEntityName and sParentEntity are related
	 * @return
	 * @precondition oRelatedId != null
	 * @todo restrict permissions by entity name
	 */
	public <PK> Collection<EntityObjectVO<PK>> getDependantMasterData(UID entity, UID foreignKeyField, Object oRelated, String username) {
		return getDependantMasterData(entity, foreignKeyField, oRelated, username, new HashMap<String, Object>());
	}
	
	public <PK> Collection<EntityObjectVO<PK>> getDependantMasterData(EntityMeta<PK> entity, FieldMeta<PK> foreignKeyField, Object oRelated, String username) {
		return getDependantMasterData(entity.getUID(), foreignKeyField.getUID(), oRelated, username, new HashMap<String, Object>());
	}
	
	/**
	 * gets the dependant master data records for the given entity, using the given foreign key field and the given id as foreign key.
	 * @param sEntityName name of the entity to get all dependant master data records for
	 * @param sForeignKeyField name of the field relating to the foreign entity
	 * @param oRelatedId id by which sEntityName and sParentEntity are related
	 * @return
	 * @precondition oRelatedId != null
	 * @todo restrict permissions by entity name
	 */
	public <PK> Collection<EntityObjectVO<PK>> getDependantMasterData(UID entityUid, UID sForeignKeyField, Object oRelatedId, String username, Map<String, Object> mpParams) {
		if (oRelatedId == null) {
			throw new NullArgumentException("oRelatedId");
		}
		final EntityMeta<?> mdmetavo = metaProvider.getEntity(entityUid);
		final String sEntityName = mdmetavo.getEntityName();
		LOG.debug("Getting dependant masterdata for entity " + sEntityName + " with foreign key field " 
				+ sForeignKeyField + " and related id " + oRelatedId);
		final Date startDate = new Date();

		Collection<MasterDataVO<PK>> result = new ArrayList<MasterDataVO<PK>>();
		if (mdmetavo.isDynamic()) {
			// TODO MULTINUCLET: dynamic entity handling (tp)
			throw new UnsupportedOperationException();
//			if (sEntityName.startsWith(EntityMeta.DYNAMIC_ENTITY_PREFIX)) {
//				String sDataSource = mdmetavo.getDbEntity().substring(EntityMeta.DYNAMIC_ENTITY_VIEW_PREFIX.length()).toLowerCase();
//				
//				try {
//					DatasourceVO datasourceVO = getDatasourceFacade().getDynamicEntity(sDataSource);
//					// @see NUCLOS-654
//					boolean bIntidCaseInsensitive = true;
//					boolean bIntidGenericObjectCaseInsensitive = true;
//					String sql = getDatasourceFacade().createSQL(datasourceVO.getSource(), new HashMap<String, Object>());
//					if (sql.toUpperCase().indexOf(" \"" + CommonDatasourceFacade.REF_ENTITY + "\"") != -1)
//						bIntidCaseInsensitive = false;
//					if (sql.toUpperCase().indexOf(" \"" + CommonDatasourceFacade.REF_ENTITY + "\"") != -1)
//						bIntidGenericObjectCaseInsensitive = false;
//				
//					result = getDependantMasterDataForDatasource(oRelatedId, mdmetavo, bIntidCaseInsensitive, bIntidGenericObjectCaseInsensitive);
//				} catch (Exception e) {
//					LOG.warn("getDependantMasterDataForDatasource failed for datasource " + sDataSource, e);
//				}
//			} else if (sEntityName.startsWith(EntityMeta.CHART_ENTITY_PREFIX)) {
//				String sDataSource = mdmetavo.getDbEntity().substring(EntityMeta.CHART_ENTITY_VIEW_PREFIX.length()).toLowerCase();
//				
//				try {
//					DatasourceVO datasourceVO = getDatasourceFacade().getChart(sDataSource);
//					Map<String, Object> mpTempParams = new HashMap<String, Object>(mpParams);
//					mpTempParams.put("genericObject", oRelatedId);
//					result = getDependantMasterDataForDatasource(oRelatedId, mdmetavo,datasourceVO, mpTempParams);
//				} catch (Exception e) {
//					LOG.warn("getDependantMasterDataForDatasource failed for datasource " + sDataSource, e);
//				}
//			}
		} else {
			result = getDependantMasterDataByBean(entityUid, sForeignKeyField, oRelatedId);
		}

		Collection<EntityObjectVO<PK>> colEntityObject = CollectionUtils.transform(result, 
				new MasterDataToEntityObjectTransformer<PK>());

		Date endate = new Date();
		NuclosPerformanceLogger.performanceLog(
				startDate.getTime(),
				endate.getTime(),
				username,
				oRelatedId,
				sEntityName,
				"Reading the master data entity for an objekt if type "+sEntityName+" ("
						+ (mdmetavo.isDynamic() ? " dynamic " : " static ") +")",
				"",
				"");

		return colEntityObject;
	}

	<PK> Collection<MasterDataVO<PK>> getDependantMasterDataByBean(UID entityUid, UID sForeignKeyFieldName, Object oRelatedId) {
		final EntityMeta<PK> meta = (EntityMeta<PK>) metaProvider.getEntity(entityUid);
		final FieldMeta<?> fieldMeta = metaProvider.getEntityField(sForeignKeyFieldName);

		CollectableSearchCondition cond = null;
		if (oRelatedId instanceof Long) {
			cond = SearchConditionUtils.newIdComparison(fieldMeta, ComparisonOperator.EQUAL, (Long) oRelatedId);
		} else {
			cond = SearchConditionUtils.newUidComparison(fieldMeta, ComparisonOperator.EQUAL, (UID) oRelatedId);
		}

		final FieldMeta<?> efDeleted = SF.LOGICALDELETED.getMetaData(entityUid);
		if (efDeleted != null && metaProvider.getEntity(entityUid).getFields().contains(efDeleted)) {
			final CollectableEntityField clctEOEFdeleted = new CollectableEOEntityField(
					SF.LOGICALDELETED.getMetaData(entityUid));
			final CollectableSearchCondition condSearchDeleted = new CollectableComparison(
					clctEOEFdeleted, ComparisonOperator.EQUAL, new CollectableValueField(false));
			cond = SearchConditionUtils.and(cond, condSearchDeleted);
		}
		return getGenericMasterData(meta, cond, true);
	}

	// TODO MULTINUCLET: dynamic entity handling (tp)
//	<PK> Collection<MasterDataVO<PK>> getDependantMasterDataForDatasource(
//			PK oRelatedId, final EntityMeta mdmetavo, boolean bIntidCaseInsensitive, boolean bIntidGenericObjectCaseInsensitive) {
//		
//		final Collection<FieldMeta> collFields = mdmetavo.getFields().values();
//		final int fieldCount = collFields.size();
//
//		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
//		DbQuery<DbTuple> query = builder.createTupleQuery();
//		DbFrom<PK> t = (DbFrom<PK>) query.from(mdmetavo.getE()).alias(SystemFields.BASE_ALIAS);
//		
//		DbColumnExpression<Integer> goColumn = bIntidGenericObjectCaseInsensitive 
//				? t.baseColumn(CommonDatasourceFacade.REF_ENTITY, Integer.class)
//				: t.baseColumnCaseSensitive(CommonDatasourceFacade.REF_ENTITY, Integer.class, false);
//		List<DbSelection<?>> selection = new ArrayList<DbSelection<?>>();
//		for (FieldMeta field : collFields) {
//			UID fieldName = field.getEF().getUID();
//			if (fieldName.equals(ModuleConstants.DEFAULT_FOREIGNKEYFIELDNAME)) {
//				selection.add(goColumn);
//			} else {
//				Class<?> javaType = DalUtils.getDbType(field.getDataType());
//				selection.add(t.baseColumnCaseSensitive(fieldName, javaType, false));
//			}
//		}
//		selection.add(bIntidCaseInsensitive 
//				? t.baseColumn(CommonDatasourceFacade.PRIMARY_KEY, Integer.class)
//				: t.baseColumnCaseSensitive(CommonDatasourceFacade.PRIMARY_KEY, Integer.class, false));
//		query.multiselect(selection);
//		query.where(builder.equal(goColumn, oRelatedId));
//
//		return dataBaseHelper.getDbAccess().executeQuery(query, new Transformer<DbTuple, MasterDataVO<PK>>() {
//			@Override
//            public MasterDataVO<PK> transform(DbTuple tuple) {
//				MasterDataVO<PK> result = new MasterDataVO<PK>(mdmetavo, false);
//				result.setPrimaryKey(tuple.get(fieldCount, Integer.class));
//				int i = 0;
//				for (FieldMeta field: collFields) {
//					String fieldName = field.getFieldUID();
//					Object value = tuple.get(i);
//					if (fieldName.equals(ModuleConstants.DEFAULT_FOREIGNKEYFIELDNAME)) {
//						result.setFieldValue(fieldName + "Id", value);
//					} else {
//						result.setFieldValue(fieldName, value);
//					}
//					
//					++i;
//				}
//				return result;
//			}
//		});
//	}
//	
//	<PK> Collection<MasterDataVO<PK>> getDependantMasterDataForDatasource(
//			PK oRelatedId, final EntityMeta mdmetavo, final DatasourceVO datasourceVO, final Map<String, Object> mpParams) 
//					throws NuclosDatasourceException {
//		
//		final String sql = getDatasourceFacade().createSQL(datasourceVO.getSource(), mpParams);
//		return dataBaseHelper.getDbAccess().executePlainQuery(sql, -1, new ResultSetRunner<Collection<MasterDataVO<PK>>>() {
//			@Override
//			public Collection<MasterDataVO<PK>> perform(ResultSet result) throws SQLException {
//		            Collection<MasterDataVO<PK>> values = new ArrayList<MasterDataVO<PK>>();
//		
//		            MasterDataVO<PK> mdvo = null;
//		            HashMap<UID, Object> mpFields = null;
//		            final HashMap<Integer, UID> columnames = new HashMap<Integer, UID>();
//		            final ResultSetMetaData metadata = result.getMetaData();
//		
//		            for (int i = 1; i <= metadata.getColumnCount(); i++) {
//		            	String columName = metadata.getColumnName(i);
//		            	// TODO MULTINUCLET: Could we really use the DB column name as UID??? (tp)
//		            	// columnames.put(i, columName);
//		            	columnames.put(i, new UID(columName));
//		            }
//		            
//		            while (result.next()) {
//		                mpFields = new HashMap<UID, Object>();
//		                for (Integer columnNumber : columnames.keySet()) {
//		               	Object value = result.getObject(columnNumber);
//		               	mpFields.put(columnames.get(columnNumber), value);
//		             }
//		
//		             mdvo = new MasterDataVO<PK>(mdmetavo, false);
//		             // mdvo.setFields(mpFields);
//		             for (UID fuid: mpFields.keySet()) {
//		            	 final Object value = mpFields.get(fuid);
//		            	 mdvo.setFieldValue(fuid, value);
//		             }
//		             values.add(mdvo);
//		          }
//		
//            	return values;
//			}
//		});
//	}

	static void invalidateCaches(UID nuclosEntity, MasterDataVO<?> mdvo) {
		LOG.debug("invalidateCaches(" + nuclosEntity  + ", " + mdvo + ")");
		if (E.isNuclosEntity(nuclosEntity)) {
			if (E.ROLE.checkEntityUID(nuclosEntity) || E.ACTION.checkEntityUID(nuclosEntity) || E.REPORT.checkEntityUID(nuclosEntity)
					|| E.TASKLIST.checkEntityUID(nuclosEntity)) {
				SecurityCache.getInstance().invalidate();
			} else if (E.USER.checkEntityUID(nuclosEntity)) {
				SecurityCache.getInstance().invalidate(mdvo.getFieldValue(E.USER.name), true);
			} else if (E.LAYOUT.checkEntityUID(nuclosEntity)) {
				MetaProvider.getInstance().revalidate(true, true);
				SpringApplicationContextHolder.getBean(LayoutFacadeBean.class).evictCaches();
			} else if (E.LAYOUTUSAGE.checkEntityUID(nuclosEntity)) {
				MetaProvider.getInstance().revalidate(true, true);
			} else if (E.RESOURCE.checkEntityUID(nuclosEntity)) {
				ResourceCache.getInstance().invalidate();
			} else if (E.PARAMETER.checkEntityUID(nuclosEntity)) {
				ServerParameterProvider.getInstance().revalidate();
			} else if (E.DYNAMICENTITY.checkEntityUID(nuclosEntity) || E.DYNAMICENTITYUSAGE.checkEntityUID(nuclosEntity)
					|| E.CHART.checkEntityUID(nuclosEntity) || E.CHARTUSAGE.checkEntityUID(nuclosEntity)) {
				MetaProvider.getInstance().revalidate(true, false);
			} else if (E.ENTITYLAFPARAMETER.checkEntityUID(nuclosEntity)) {
				MetaProvider.getInstance().revalidate(false, false);
			} else { 
				LOG.debug("invalidateCaches: Nothing to do for " + nuclosEntity);
			}
		}
	}

	/**
	 * Called after an entity was changed - that is, a row was inserted, updated or deleted.
	 * @param mdmetavo the entity that was changed.
	 * @param mdvoChanged the object that was inserted, updated or deleted.
	 */
	private void entityChanged(EntityMeta<?> mdmetavo, MasterDataVO<?> mdvoChanged) {
		UID entityUID = mdmetavo.getUID();

		if (E.ROLEUSER.checkEntityUID(entityUID)) {
			// Rights are reloaded the next time the user logs in - we don't need to do anything here
			// @todo To make these changes visible immediately, however, we could notify the client and change the roles dynamically in the server.
			// But can we do that in a J2EE conformant way?
//			NucleusSecurityProxy.invalidateMethodRightsForUser(mdvoChanged.getField("user", String.class));
		}
		else if (E.ROLEACTION.checkEntityUID(entityUID)) {
//			NucleusSecurityProxy.invalidateMethodRightsForAllUsers();
		}

		if (mdmetavo.isCacheable()) {
			this.notifyClients(mdmetavo.getUID());
		} else {
			if (E.RULETRANSITION.checkEntityUID(entityUID)
					|| E.RULEGENERATION.checkEntityUID(entityUID)
					|| E.RULEUSAGE.checkEntityUID(entityUID)
					|| E.RULE.checkEntityUID(entityUID)
					|| E.CODE.checkEntityUID(entityUID)
					|| E.SERVERCODE.checkEntityUID(entityUID)
					|| E.TIMELIMITRULE.checkEntityUID(entityUID))
				this.notifyClients(mdmetavo.getUID());	

		}
	}

	/*
	 * Returns the name of the user-writable
	 * @param mdmetavo
	 * @return
	 * 
	 * @deprecated
	 */
	/*
	public static String getUserWritableDbEntityName(FieldMeta mdmetavo) {
		String table = mdmetavo.getDBEntity();
		return table.startsWith("V_") ? "T_" + table.substring(2) : table;
	}
	 */

	/*
	 * @param mdmetavo
	 * @return the names of all user writable database fields.
	 * 
	 * @deprecated
	 */
	/*
	public static List<UID> getUserWritableDbFieldUids(EntityMeta mdmetavo) {
		final List<UID> result = CollectionUtils.transform(mdmetavo.getFields().values(), new Transformer<FieldMeta, UID>() {
			@Override
            public UID transform(FieldMeta mdmetafieldvo) {
				return (mdmetafieldvo.getForeignEntity() == null) ? mdmetafieldvo.getDbColumn() : mdmetafieldvo.getDBIdFieldName();
			}
		});
		// remove all system fields from the result:
		for (Iterator<UID> iter = result.iterator(); iter.hasNext();) {
			if (lstSystemDbFieldNames.contains(iter.next().toLowerCase())) {
				iter.remove();
			}
		}
		return result;
	}
	 */

	/**
	 * performs a stale version check.
	 * @param mdvo
	 * @throws CommonStaleVersionException
	 */
	<PK> MasterDataVO<PK> checkForStaleVersion(EntityMeta<PK> mdMetaVO, MasterDataVO<PK> mdvo) 
			throws CommonStaleVersionException, CommonPermissionException, CommonFinderException {
		
		final MasterDataVO<PK> mdvoInDataBase = getMasterDataCVOById(mdMetaVO, mdvo.getPrimaryKey());
		if (mdvo.getVersion() != mdvoInDataBase.getVersion()) {
			throw new CommonStaleVersionException("master data", mdvo.toDescription(), mdvoInDataBase.toDescription());
		}
		if (mdvo.isSystemRecord()) {
			throw new CommonPermissionException();
		}

		return mdvoInDataBase;
	}

	static <PK> void checkInvariantFields(EntityMeta<?> mdMetaVO, MasterDataVO<PK> mdvo, MasterDataVO<PK> mdvoInDataBase) throws CommonValidationException {
		for (FieldMeta<?> mdMetaFieldVO : mdMetaVO.getFields()) {
			if (!mdMetaFieldVO.isInvariant()) {
				continue;
			}
			final UID fieldName = mdMetaFieldVO.getUID();
			if (!ObjectUtils.equals(mdvo.getFieldValue(fieldName), mdvoInDataBase.getFieldValue(fieldName))) {
				// TODO_AUTOSYNC: translation
				throw new CommonValidationException(MessageFormat.format(
						"Field \"{0}\" cannot be changed because it is declared as invariant", fieldName));
			}
		}
	}
	
	<PK,F> void insertInto(UID table, UID column1, F value1, Object[] varargs) 
			throws CommonCreateException {
		try {
			EntityMeta<PK> entity = (EntityMeta<PK>) metaProvider.getEntity(table);
			FieldMeta<F> field = (FieldMeta<F>) metaProvider.getEntityField(column1);
			dataBaseHelper.execute(DbStatementUtils.insertInto(entity, field, value1, varargs));
		} catch (CommonFatalException ex) {
			throw new CommonCreateException(ex);
		}
	}

	<PK,F> void deleteFrom(UID table, UID column1, F value1, Object[] varargs) 
			throws CommonRemoveException {
		try {
			EntityMeta<PK> entity = (EntityMeta<PK>) metaProvider.getEntity(table);
			FieldMeta<F> field = (FieldMeta<F>) metaProvider.getEntityField(column1);
			dataBaseHelper.execute(DbStatementUtils.deleteFrom(entity, field, value1, varargs));
		} catch (CommonFatalException ex) {
			throw new CommonRemoveException(ex);
		}
	}
	
	/**
	 * removes a single masterdata row.
	 * @param entityUid
	 * @param mdvo
	 * @throws CommonFinderException
	 * @throws CommonRemoveException
	 * @throws CommonStaleVersionException
	 * @precondition sEntityName != null
	 */
	<PK> void removeSingleRow(UID entityUid, final MasterDataVO<PK> mdvo, String customUsage)
			throws CommonFinderException, CommonRemoveException, CommonStaleVersionException, CommonPermissionException {

		if (entityUid == null) {
			throw new NullArgumentException("sEntityName");
		}

		final EntityMeta<PK> mdmetavo = metaProvider.getEntity(entityUid);

		// prevent removal if dependant dynamic attributes exist:
		final Object oExternalId = mdvo.getId();
		if (oExternalId == null) {
			throw new NuclosFatalException("mdhelper.error.invalid.id");//"Der Datensatz hat eine leere Id.");
		}

		final MasterDataVO<?> mdvoInDB = checkForStaleVersion(mdmetavo, mdvo);
		
		boolean removed = false;
		if (E.isNuclosEntity(entityUid)) {
			if (E.DBSOURCE.checkEntityUID(entityUid)) {
				try {
		            updateDbObject((EntityObjectVO<UID>) mdvo.getEntityObject(), null, -1);
		            removed = true;
	            }
	            catch(NuclosBusinessException e) {
		            throw new CommonRemoveException(e.getMessage(), e);
	            }
			} else if (E.DBOBJECT.checkEntityUID(entityUid)) {
				for (EntityObjectVO<?> source : nucletDalProvider.getEntityObjectProcessor(E.DBSOURCE).getBySearchExpression(
					appendRecordGrants(new CollectableSearchExpression(
						SearchConditionUtils.newUidComparison(E.DBSOURCE.dbobject, 
								ComparisonOperator.EQUAL, (UID) mdvo.getPrimaryKey())
						), entityUid)
					)) {
						removeSingleRow(E.DBSOURCE.getUID(), DalSupportForMD.wrapEntityObjectVO(source), customUsage);
				}
			}
		}

		// @todo refactor: make this easier to write:
		try {
			/*
			String sTable; // = getUserWritableDbEntityName(mdmetavo);
			JdbcEntityObjectProcessor<?> eoProcessor = nucletDalProvider.getEntityObjectProcessor(mdmetavo.getDalEntity());
			sTable = eoProcessor.getDbSourceForSQL();
			 */
			if (!removed) { 
				dataBaseHelper.execute(DbStatementUtils.deleteFromUnsafe(mdmetavo, mdmetavo.isUidEntity() ? SF.PK_UID : SF.PK_ID, mdvo.getPrimaryKey()));
			}
			final ILucenian<PK> lucinian = mdvo.getEntityObject().lucenian();
			if (lucinian != null) {
				final Delete<PK> delete = new Delete<PK>(mdvo.getPrimaryKey(), mdvo.getEntityObject().getDalEntity());
				lucinian.del(delete);
			}
		}
		catch (CommonFatalException ex) {
			throw new CommonRemoveException(ex);
		}
		
		//remove documents
		for (FieldMeta<?> field : metaProvider.getEntity(entityUid).getFields()) {
			if (field.getDataType().equals(GenericObjectDocumentFile.class.getName())) {
				String sExtendedPath = "";
				if (entityUid.equals(E.GENERALSEARCHDOCUMENT.getUID())) {
					GenericObjectDocumentFile<?> docFile = (GenericObjectDocumentFile<?>) mdvo.getFieldValue(E.GENERALSEARCHDOCUMENT.file);
					sExtendedPath = StringUtils.emptyIfNull(docFile.getDirectoryPath());
				}
				// File file = new File(NuclosSystemParameters.getString(NuclosSystemParameters.DOCUMENT_PATH) + "/" + sExtendedPath);
				remove((Long) mdvo.getPrimaryKey(), null, NuclosSystemParameters.getDirectory(NuclosSystemParameters.DOCUMENT_PATH));
			}
		}
		getHistoryFacade().trackRemoveToLogbookIfPossible(mdvoInDB.getEntityObject(), customUsage);
		entityChanged(mdmetavo, mdvo);
	}

	/**
	 * modifies a single masterdata row.
	 * @param entityUid
	 * @param mdvo
	 * @param sUserName
	 * @param bValidate
	 * @return
	 * @throws CommonStaleVersionException
	 * @throws CommonValidationException
	 */
	<PK> PK modifySingleRow(UID entityUid, MasterDataVO<PK> mdvo, String sUserName, boolean bValidate, String customUsage)
			throws CommonCreateException, CommonFinderException, CommonStaleVersionException, CommonValidationException, CommonPermissionException {

		final EntityMeta<PK> mdmetavo = metaProvider.getEntity(entityUid);
		final MasterDataVO<PK> mdvoInDB = checkForStaleVersion(mdmetavo, mdvo);
		checkInvariantFields(mdmetavo, mdvo, mdvoInDB);
		validateUniqueConstraintWithJson(mdmetavo, mdvo);

		if(E.USER.getUID().equals(entityUid)
			&& sUserName.equalsIgnoreCase(mdvoInDB.getFieldValue(E.USER.name))
			&& !mdvoInDB.getFieldValue(E.USER.name).equalsIgnoreCase(mdvo.getFieldValue(E.USER.name))) {
			throw new CommonPermissionException("masterdata.error.change.own.user.name");
		}

		JdbcEntityObjectProcessor<PK> eoProcessor = nucletDalProvider.getEntityObjectProcessor(mdmetavo);
		EntityObjectVO<PK> eoVO = DalSupportForMD.getEntityObjectVO(mdvo);
		DalUtils.updateVersionInformation(eoVO, sUserName);
		eoVO.flagUpdate();
		
		boolean updated = false;
		
		if (E.DBSOURCE.checkEntityUID(entityUid)) {
			try {
	            updateDbObject((EntityObjectVO<UID>) mdvoInDB.getEntityObject(), (EntityObjectVO<UID>) eoVO, 0);
	            updated = true;
            }
            catch(NuclosBusinessException e) {
	            throw new CommonCreateException(e.getMessage(), e);
            }
		}	
		
		try {
			if (!updated) eoProcessor.insertOrUpdate(eoVO);
		} catch (DbException e) {
			throw new CommonCreateException(e.getMessage(), e);
		}

		storeFiles(entityUid, eoVO);
		getHistoryFacade().trackChangesToLogbookIfPossible(mdvoInDB.getEntityObject(), eoVO, customUsage);
		entityChanged(mdmetavo, mdvo);
		return mdvo.getPrimaryKey();
	}

	/**
	 * creates a single masterdata row.
	 * @param sEntityName
	 * @param mdvoToCreate
	 * @param sUserName
	 * @param bValidate
	 * @return the new id of the created row
	 * @precondition mdvo.getId() == null
	 */
	<PK> PK createSingleRow(MasterDataVO<PK> mdvoToCreate, String sUserName, 
			boolean bValidate, PK intid) throws
		CommonCreateException, CommonValidationException {
		if (mdvoToCreate.getPrimaryKey() != null) {
			throw new IllegalArgumentException("mdvoToCreate.getId()");
		}
		final UID entityUid = mdvoToCreate.getEntityObject().getDalEntity();
		final EntityMeta<?> mdmetavo = metaProvider.getEntity(entityUid);

		validateUniqueConstraintWithJson(mdmetavo, mdvoToCreate);

		// @todo optimize: use idfactory.nextval for insert

		final PK result;
		if (intid != null) {
			result = intid;
		} else {
			if (mdvoToCreate.getPrimaryKey() != null) {
				result = mdvoToCreate.getPrimaryKey();
			} else {
				if (mdmetavo.getPkClass() == UID.class) {
					result = (PK) new UID();
				} else {
					final String idFactory = mdmetavo.getIdFactory();
					if (idFactory == null) {
						result = (PK) dataBaseHelper.getNextIdAsLong(SpringDataBaseHelper.DEFAULT_SEQUENCE);
					} else {
						result = (PK) dataBaseHelper.getDbAccess().executeFunction(idFactory, Long.class);
					}
				}
			}
		}
		mdvoToCreate.setPrimaryKey(result);

		JdbcEntityObjectProcessor<PK> eoProcessor = (JdbcEntityObjectProcessor<PK>) 
				nucletDalProvider.getEntityObjectProcessor(mdmetavo);
		EntityObjectVO<PK> eoVO = DalSupportForMD.getEntityObjectVO(mdvoToCreate);
		if (!eoVO.keepsVersion()) eoVO.setVersion(null);
		eoVO.setCreatedBy(null);
		eoVO.setCreatedAt(null);
		DalUtils.updateVersionInformation(eoVO, sUserName);
		eoVO.flagNew();			
		
		boolean created = false;
		
		if (E.DBSOURCE.checkEntityUID(entityUid)) {
			try {
				updateDbObject(null, (EntityObjectVO<UID>) eoVO, 1);
				created = true;
			}
			catch (NuclosBusinessException e) {
				throw new CommonCreateException(e.getMessage(), e);
			}
		}
		
		try {
			if (!created) eoProcessor.insertOrUpdate(eoVO);
		} catch (DbException e) {
			throw new CommonCreateException(e.toString());
		}

		storeFiles(entityUid, eoVO);
		entityChanged(mdmetavo, mdvoToCreate);
		return result;
	}

	private void validateUniqueConstraintWithJson(EntityMeta<?> mdmetavo, MasterDataVO<?> mdvoToCreate) throws CommonValidationException {
		if (!XMLEntities.hasSystemData(mdmetavo.getUID())) {
			return;
		}
		CompositeCollectableSearchCondition cond = new CompositeCollectableSearchCondition(LogicalOperator.AND);
		for (FieldMeta<?> field : mdmetavo.getFields()) {
			if (field.isUnique()) {
				final UID fieldUid = field.getUID();
				if (mdvoToCreate.getFieldValue(fieldUid) != null) {
					if (field.getForeignEntity() != null) {
						/*
						cond.addOperand(SearchConditionUtils.newMDReferenceComparison(
								mdmetavo, fieldUid, mdvoToCreate.getFieldValue(fieldUid + "Id", Integer.class)));
						 */
						Object id = mdvoToCreate.getFieldId(fieldUid);
						if (id == null) {
							id = mdvoToCreate.getFieldUid(fieldUid);
						}
						cond.addOperand(SearchConditionUtils.newPkComparison(
								field, ComparisonOperator.EQUAL, id));
					}
					else {
						/*
						cond.addOperand(SearchConditionUtils.newMDComparison(
								mdmetavo, fieldUid, ComparisonOperator.EQUAL, 
								mdvoToCreate.getFieldValue(field.getEF().getUID())));
						 */
						cond.addOperand(SearchConditionUtils.newComparison(
								field, ComparisonOperator.EQUAL, 
								mdvoToCreate.getFieldValue(field.getUID())));
					}
				}
				else {
					/*
					cond.addOperand(SearchConditionUtils.newMDIsNullCondition(mdmetavo, fieldUid));
					 */
					cond.addOperand(SearchConditionUtils.newIsNullCondition(field));
				}
			}
		}

		if (cond.getOperandCount() > 0) {
			final Collection<MasterDataVO<UID>> systemObjects = XMLEntities.getSystemObjects(mdmetavo.getUID(), cond);
			if (!systemObjects.isEmpty()) {
				throw new CommonValidationException("nuclos.validation.systementity.unique");
			}
		}
	}

	/**
	 *
	 * @param eoVO
	 */
	public static void storeFiles(UID entityUid, EntityObjectVO<?> eoVO) {
		for (FieldMeta<?> efMeta : MetaProvider.getInstance().getAllEntityFieldsByEntity(entityUid).values()) {
			final Object oValue = eoVO.getFieldValue(efMeta.getUID());
			final String sClzz = efMeta.getDataType();

			if (GenericObjectDocumentFile.class.getName().equals(sClzz)) {
				GenericObjectDocumentFile<?> documentFile = (GenericObjectDocumentFile<?>) oValue;
				if (documentFile != null) {
					if (documentFile.getContents() != null && documentFile.getContentsChanged()) {
						storeFile(new GenericObjectDocumentFile(
								documentFile.getFilename(),
								(Long) eoVO.getPrimaryKey(),
								documentFile.getContents(),
								documentFile.getDirectoryPath()),
								NuclosSystemParameters.getDirectory(NuclosSystemParameters.DOCUMENT_PATH));
					}
				}
			}
		}
	}

	/**
	 * removes the given dependants.
	 * @param mpDependants
	 * @throws CommonFinderException
	 * @throws CommonRemoveException
	 * @throws CommonStaleVersionException
	 */
	public void removeDependants(IDependentDataMap mpDependants, String customUsage)
			throws CommonFinderException, CommonRemoveException, CommonStaleVersionException, CommonPermissionException {
		
		for (UID sDependantEntityUid : mpDependants.getEntityUids()) {
			EntityMeta<?> eMeta = MetaProvider.getInstance().getEntity(sDependantEntityUid);
			if (!eMeta.isEditable()) {
				continue;
			}
			if (!metaProvider.getEntity(sDependantEntityUid).isDynamic()
				&& !eMeta.isStateModel()) {
				for (EntityObjectVO<?> mdvoDependant : mpDependants.getData(sDependantEntityUid)) {

					removeDependants(mdvoDependant.getDependents(), customUsage);
					if (MetaProvider.getInstance().getEntity(sDependantEntityUid).isStateModel()) {
						try {
							// mdvoDependant.setEntity(sDependantEntityUid);
							GenericObjectVO govo = DalSupportForGO.getGenericObjectVO((EntityObjectVO<Long>) mdvoDependant);
							GenericObjectFacadeLocal goLocal = ServerServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class);
							goLocal.remove(new GenericObjectWithDependantsVO(govo, mdvoDependant.getDependents()), true, customUsage);
						}
						catch(CommonCreateException ex) {
							throw new NuclosFatalException(ex);
						}
						catch(NuclosBusinessException ex) {
							throw new NuclosFatalException(ex);
						}
					}
					else {
						if (metaProvider.getEntity(sDependantEntityUid).isEditable()
							&& mdvoDependant.isFlagRemoved() && mdvoDependant.getPrimaryKey() != null) {
							// remove the row:
							MasterDataVO<?> voDependant = DalSupportForMD.wrapEntityObjectVO(mdvoDependant);
							removeSingleRow(sDependantEntityUid, voDependant, customUsage);
						}
					}
				}
			}
		}
	}

	/**
	 * creates/modifies the given dependants.
	 * @param mpDependants
	 * @param entityUid
	 * @param sUserName
	 * @param bValidate
	 * @throws CommonCreateException
	 * @throws CommonValidationException
	 * @throws CommonFinderException
	 * @throws CommonStaleVersionException
	 */
	<PK> void createOrModifyDependants(IDependentDataMap mpDependants, UID entityUid, String sUserName, 
			boolean bValidate, Map<MasterDataVO<PK>, PK> mpDependantsWithId, 
			Map<EntityAndField, UID> mpEntityAndParentEntityUid, String customUsage)
			throws CommonCreateException, CommonValidationException, CommonFinderException, 
			CommonStaleVersionException, CommonPermissionException {

		for (UID sDependantEntityName : mpDependants.getEntityUids()) {
			for (EntityObjectVO<?> mdvoDependant : mpDependants.getData(sDependantEntityName)) {
				EntityMeta eMeta = MetaProvider.getInstance().getEntity(sDependantEntityName);
				if (!eMeta.isEditable()) {
					continue;
				}
				// create/modify the row:
				PK intid = null;
				if (mpDependantsWithId != null && !mpDependantsWithId.isEmpty()) {
					intid = mpDependantsWithId.get(mdvoDependant);
				}
				if (eMeta.isStateModel()) {
					try {
						final EntityObjectVO<Long> dep = (EntityObjectVO<Long>) mdvoDependant;
						// mdvoDependant.setEntity(sDependantEntityName);
						final GenericObjectVO govo = DalSupportForGO.getGenericObjectVO(dep);
						final GenericObjectFacadeLocal goLocal = ServerServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class);
						final IDependentDataMap deps = mdvoDependant.getDependents();
						if(mdvoDependant.isFlagNew()) {
							goLocal.create(new GenericObjectWithDependantsVO(govo, deps), customUsage);
						}
						else if(mdvoDependant.isFlagRemoved()) {
							goLocal.remove(new GenericObjectWithDependantsVO(govo, deps), true, customUsage);
						}
						else if (mdvoDependant.isFlagUpdated() || deps.getPendingChanges()) {
							goLocal.modify(new GenericObjectWithDependantsVO(govo, deps), false, customUsage);
						}
					}
					catch(NuclosBusinessException ex) {
						throw new NuclosFatalException(ex);
					}
					catch(CommonRemoveException ex) {
						throw new NuclosFatalException(ex);
					}
				}
				else {
					final EntityObjectVO<PK> dep = (EntityObjectVO<PK>) mdvoDependant;
					final MasterDataVO<PK> voDependant = (MasterDataVO<PK>) DalSupportForMD.wrapEntityObjectVO(mdvoDependant);
					PK id = createOrModify(sDependantEntityName, voDependant, entityUid, sUserName, bValidate, 
							intid, mpEntityAndParentEntityUid, customUsage);
					dep.setPrimaryKey(id);
				}
			}
		}
	}

	/**
	 * creates the given dependant row, if it is new or updates it, if it has changed.
	 * @param sDependantEntityUid
	 * @param mdvoDependant
	 * @param entityUid
	 * @param sUserName
	 * @param bValidate
	 * @throws CommonCreateException
	 * @throws CommonValidationException
	 * @throws CommonStaleVersionException
	 */
	private <PK> PK createOrModify(UID sDependantEntityUid, MasterDataVO<PK> mdvoDependant, UID entityUid, 
			String sUserName, boolean bValidate, PK intid, 
			Map<EntityAndField, UID> mpEntityAndParentEntityUid, String customUsage)
			throws CommonCreateException, CommonValidationException, CommonFinderException, 
			CommonStaleVersionException, CommonPermissionException {

		final EntityMeta meta = metaProvider.getEntity(sDependantEntityUid);
		final UID fieldUid = getForeignKeyFieldUID(entityUid, sDependantEntityUid, mpEntityAndParentEntityUid);
		
		PK id = (PK) mdvoDependant.getFieldId(fieldUid);
		if (id == null) {
			id = (PK) mdvoDependant.getFieldUid(fieldUid);
		}
		if (!mdvoDependant.isRemoved() && id != null && !meta.isDynamic()) {
			/*
			// validate the row
			if (ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_SERVER_VALIDATES_MASTERDATAVALUES).equals("1")) {
				mdvoDependant.validate(metaProvider.getEntity(sDependantEntityUid));
			}
			 */

			PK iReferenceId;

			if (mdvoDependant.getPrimaryKey() == null) {
				// work on sDependantEntityUid
				iReferenceId = createSingleRow(mdvoDependant, sUserName, bValidate, intid);
			}
			else {
				iReferenceId = mdvoDependant.getPrimaryKey();
				if (metaProvider.getEntity(sDependantEntityUid).isEditable() && mdvoDependant.isChanged()) {
					this.modifySingleRow(sDependantEntityUid, mdvoDependant, sUserName, bValidate, customUsage);
				}
				else {
					LOG.debug("Dependant row " + mdvoDependant.getId() + " has not changed. Will not be updated.");
				}
			}

			for (UID sDependantMasterDataEntityUid : mdvoDependant.getDependents().getEntityUids()) {
				UID sForeignKeyFieldUid = getForeignKeyFieldUID(sDependantEntityUid, sDependantMasterDataEntityUid, mpEntityAndParentEntityUid);

				if (sForeignKeyFieldUid != null) {
					for (EntityObjectVO<?> mdvo : mdvoDependant.getDependents().getData(sDependantMasterDataEntityUid)) {
						//if(mdvo.getFieldIds().get(sForeignKeyFieldName) == null) //@see NUCLOS-1113
							mdvo.setFieldId(sForeignKeyFieldUid, (Long) iReferenceId);
					}
				}
				else {
					final String sMessage = StringUtils.getParameterizedExceptionMessage(
							"mdhelper.error.missing.foreignkey.field", sDependantMasterDataEntityUid, sDependantEntityUid);
					//"Es existiert kein Fremdschl\u00fcsselfeld der Entit\u00e4t "+sDependantMasterDataEntityName+", das auf die \u00fcbergeordnete Entit\u00e4t "+sDependantEntityName+" referenziert";
					throw new NuclosFatalException(sMessage);
				}
			}
			createOrModifyDependants(mdvoDependant.getDependents(), sDependantEntityUid, sUserName, 
					bValidate, null, mpEntityAndParentEntityUid, customUsage);
			return iReferenceId;
		}
		else {
			return null;
		}
	}


	public UID getForeignKeyFieldUID(UID entityUid, UID sDependantEntityUid, Map<EntityAndField, UID> mpEntityAndParentEntityUid) {
		UID result = null;

		/**
		 * search in layout...
		 */
		for (EntityAndField eafn : mpEntityAndParentEntityUid.keySet()) {
			if (eafn.getEntity().equals(sDependantEntityUid)) {
				final UID sFieldName = eafn.getField();
				if (result == null) {
					// this is the foreign key field:
					result = sFieldName;
				}
				else {
					final String sMessage = StringUtils.getParameterizedExceptionMessage(
							"mdhelper.error.more.foreignkey.field", result, sFieldName);
						//"Es gibt mehr als ein Fremdschl\u00fcsselfeld, das die \u00fcbergeordnete Entit\u00e4t referenziert:\n" + "\t" + result + "\n" + "\t" + sFieldName;
					throw new NuclosFatalException(sMessage);
				}
			}
		}

		/**
		 * if no information from layout is accessible try to get it from meta data...
		 */
		if (result == null) {
			final EntityMeta<?> entityMeta = metaProvider.getEntity(sDependantEntityUid);

			// Old Nucleus instance namend the foreign key field "genericObject"
			// and it could be that more than column refers on the parent entity, so
			// the underlying search could not find the referencing column.
			for (FieldMeta<?> field : entityMeta.getFields()) {
				if ("genericObject".equalsIgnoreCase(field.getFieldName())) {
					return field.getUID();
				}
			}

			// Default: field referencing the parent entity has the same name as the parent entity
			for (FieldMeta<?> field : entityMeta.getFields()) {
				if (LangUtils.equals(entityMeta.getEntityName(), field.getFieldName())) {
					return field.getUID();
				}
			}

			// If no such field is present, it must be a (the) field referencing the parent entity
			for (FieldMeta<?> field : entityMeta.getFields()) {
				if (entityUid.equals(field.getForeignEntity())) {
					final UID sFieldName = field.getUID();
					if (result == null) {
						// this is the foreign key field:
						result = sFieldName;
					}
					else {
						final String sMessage = StringUtils.getParameterizedExceptionMessage(
								"mdhelper.error.more.foreignkey.field", result, sFieldName);
						//"Es gibt mehr als ein Fremdschl\u00fcsselfeld, das die \u00fcbergeordnete Entit\u00e4t referenziert:\n" + "\t" + result + "\n" + "\t" + sFieldName;
						throw new NuclosFatalException(sMessage);
					}
				}
			}
		}

		return result;
	}

	/**
	 * creates the given dependants.
	 * @param mpDependants
	 * @param entityUid
	 * @param iParentId
	 * @param sUserName
	 * @param bValidate
	 * @throws CommonCreateException
	 * @throws CommonValidationException
	 * @precondition mpDependants != null
	 * @precondition sForeignIdFieldName != null
	 * @precondition iParentId != null
	 */
	public <PK> void createDependants(IDependentDataMap mpDependants, UID entityUid, Object iParentId, 
			String sUserName, boolean bValidate, Map<MasterDataVO<PK>,PK> mpDependantsWithId, 
			Map<EntityAndField, UID> mpEntityAndParentEntityUid)
			throws CommonCreateException, CommonValidationException {

		for (UID sDependantEntityUid : mpDependants.getEntityUids()) {
			for (EntityObjectVO<?> mdvoDependant : mpDependants.getData(sDependantEntityUid)) {
				final UID sForeignIdFieldUid = getForeignKeyFieldUID(entityUid, sDependantEntityUid, mpEntityAndParentEntityUid);
				if (!mdvoDependant.isFlagRemoved()/* && !mdvoDependant.isEmpty(sForeignIdFieldName)*/) {
					// TODO MULTINUCLET: The workaround does not work any more. Fix all places that depend on it. (tp) 
					// eliminate this workaround:
					// set the id of the foreign key field to the id of the parent:
					// mdvoDependant.getFieldValues().put(sForeignIdFieldUid, iParentId);
					// mdvoDependant.getFieldIds().put(sForeignIdFieldUid.substring(0, sForeignIdFieldUid.length()-2), iParentId);
					if (iParentId instanceof Long) {
						mdvoDependant.setFieldId(sForeignIdFieldUid, (Long) iParentId);						
					} else if (iParentId instanceof UID) {
						mdvoDependant.setFieldUid(sForeignIdFieldUid, (UID) iParentId);
					} else {
						throw new IllegalArgumentException("parent id " + iParentId);
					}
					
					// create dependant row:
					PK iId;
					MasterDataVO<PK> voDependant = (MasterDataVO<PK>) DalSupportForMD.wrapEntityObjectVO(mdvoDependant);
					// work on sDependantEntityUid
					if (mpDependantsWithId != null && !mpDependantsWithId.isEmpty()) {
						iId = createSingleRow(voDependant, sUserName, bValidate, mpDependantsWithId.get(mdvoDependant));
					} else {
						iId = createSingleRow(voDependant, sUserName, bValidate, null);
					}
					createDependants(mdvoDependant.getDependents(), sDependantEntityUid, iId, sUserName, 
							bValidate, null, mpEntityAndParentEntityUid);
				}
			}
		}
	}

	public <PK> TruncatableCollection<MasterDataVO<PK>> getGenericMasterData(EntityMeta<PK> entity, final CollectableSearchCondition cond, final boolean bAll) {
		return (TruncatableCollection<MasterDataVO<PK>>) _getGenericMasterData(entity.getUID(), cond, bAll);
	}
	
	/**
	 * gets master data records for a given entity and search condition (generic mechanism)
	 * @param entityUid name of the entity to get master data records for
	 * @param cond search condition value object
	 * @return TruncatableCollection<MasterDataVO> collection of master data value objects
	 * @postcondition result != null
	 * 
	 * @deprecated Use {@link #getGenericMasterData(E, CollectableSearchCondition, boolean)}
	 */
	public <PK> TruncatableCollection<MasterDataVO<PK>> getGenericMasterData(UID entityUid, final CollectableSearchCondition cond, final boolean bAll) {
		return _getGenericMasterData(entityUid, cond, bAll);
	}
	
	private TruncatableCollection _getGenericMasterData(UID entityUid, final CollectableSearchCondition cond, final boolean bAll) {
		JdbcEntityObjectProcessor eoProcessor = nucletDalProvider.getEntityObjectProcessor(entityUid);

		CollectableSearchExpression clctexpr = new CollectableSearchExpression(cond);
		List<EntityObjectVO<?>> eoResult = eoProcessor.getBySearchExpression(appendRecordGrants(clctexpr, entityUid), 
				bAll ? null : MAXROWS + 1, false);
		if (entityUid.equals(E.GENERALSEARCHDOCUMENT.getUID())){
			for (EntityObjectVO<?> voEntity : eoResult) {
				String sPath = (String) voEntity.getFieldValue(E.GENERALSEARCHDOCUMENT.path);
				((GenericObjectDocumentFile<?>) voEntity.getFieldValue(E.GENERALSEARCHDOCUMENT.file)).setDirectoryPath(sPath);
			}
		}

		boolean truncated = false;
		int recordCount = eoResult.size();
		if (!bAll && recordCount >= MAXROWS) {
//			recordCount = eoProcessor.count(clctexpr);
			eoResult.subList(MAXROWS, recordCount).clear();
			truncated = true;
		}

		List<MasterDataVO<?>> result = CollectionUtils.transform(eoResult, new Transformer<EntityObjectVO<?>, MasterDataVO<?>>() {
			@Override
			public MasterDataVO<?> transform(EntityObjectVO<?> eo) {
					MasterDataVO<?> mdvo = DalSupportForMD.wrapEntityObjectVO(eo);
					mdvo.getEntityObject().reset();
					return mdvo;
				}
		});
		result = new GzipList<MasterDataVO<?>>(result);

		final Collection<MasterDataVO<UID>> systemObjects = XMLEntities.getSystemObjects(entityUid, cond);
		if (!systemObjects.isEmpty()) {
			recordCount += systemObjects.size();
			result.addAll(systemObjects);
		}

		return new TruncatableCollectionDecorator<MasterDataVO<?>>(result, truncated, recordCount);
	}

	public <PK> List<MasterDataVO<PK>> getMasterDataChunk(UID entityUid, final List<CollectableEntityField> cefs, 
			final CollectableSearchExpression clctexpr, int istart, int iend) {
		List<MasterDataVO<PK>> systemObjects = null;
		if (clctexpr.isIncludingSystemData()) {
			systemObjects = new ArrayList<MasterDataVO<PK>>();
			List<MasterDataVO<UID>> allSystemObjects = XMLEntities.getSystemObjects(entityUid, clctexpr.getSearchCondition());
			for (int i = istart; i <= iend; i++) {
				if (allSystemObjects.size() > i) {
					systemObjects.add((MasterDataVO<PK>) allSystemObjects.get(i));
				}
			}
			int eoOffset;
			if (!systemObjects.isEmpty()) {
				eoOffset = systemObjects.size();
			} else {
				eoOffset = allSystemObjects.size();
			}
			istart = Math.max(0, istart - eoOffset);
			iend = Math.max(0, iend - eoOffset);
		}
		List<MasterDataVO<PK>> result = null;
		if (iend - istart >= 0) {
			JdbcEntityObjectProcessor eoProcessor = nucletDalProvider.getEntityObjectProcessor(entityUid);
			List<EntityObjectVO<PK>> eoResult = eoProcessor.getChunkBySearchExpression(cefs, clctexpr, istart, iend);
			result = CollectionUtils.transform(eoResult, new Transformer<EntityObjectVO<PK>, MasterDataVO<PK>>() {
				@Override
				public MasterDataVO<PK> transform(EntityObjectVO<PK> eo) { return DalSupportForMD.wrapEntityObjectVO(eo); }
			});
		} else {
			result = new ArrayList<MasterDataVO<PK>>();
		}
		if (systemObjects != null) {
			result.addAll(0, systemObjects);
		}
		return result;	
	}
	
	public Integer countMasterDataRows(UID entityUid, final CollectableSearchExpression clctexpr) {
		JdbcEntityObjectProcessor eoProcessor = nucletDalProvider.getEntityObjectProcessor(entityUid);
		int countSystemObjects = 0;
		if (clctexpr.isIncludingSystemData()) {
			Collection<? extends Object> systemObjects = XMLEntities.getSystemObjectIds(entityUid, clctexpr.getSearchCondition());
			countSystemObjects = systemObjects.size();
		}
		return eoProcessor.count(clctexpr)+countSystemObjects;
	}

	/**
	 * create or replace a file attachement in the file system
	 * @param documentFile
	 */
	private static void storeFile(GenericObjectDocumentFile<?> documentFile, File dir) {
		File directory = dir;
		if(documentFile.getDirectoryPath() != null && documentFile.getDirectoryPath().length() > 0) {

			directory = new File(dir.getAbsolutePath() + "/" + documentFile.getDirectoryPath());
			directory.mkdirs();
		}

		remove((Long) documentFile.getDocumentFilePk(), documentFile.getFilename(), directory);

		try {
			String sPath = getPathName(documentFile);
			//sPath += "/" + StringUtils.emptyIfNull(documentFile.getDirectoryPath()) + "/";

			IOUtils.writeToBinaryFile(new java.io.File(sPath), documentFile.getContents());
		}
		catch (IOException e) {
			// logger.error("File content cannot be updated for new file", e);
			throw new NuclosFatalException("File content cannot be updated for new file (" + e.getMessage() + ").");
		}
	}

	/**
	 * deletes the file with the given id
	 * @param iFileId
	 */
	public static void remove(Long iFileId, String sFilename, File dir) {
		if (dir.isDirectory()) {
			for (String sFileName : dir.list()) {
				if (sFileName.startsWith(iFileId + "." + (sFilename != null ? sFilename : ""))) {
					new File(dir.getAbsolutePath() + File.separator + sFileName).delete();
				}
			}
		}
	}

	private static String getPathName(GenericObjectDocumentFile<?> documentFile) {
		try {
			if (documentFile == null) {
				throw new CommonFatalException("godocumentfile.invalid.file");
				//"Der Parameter documentFile darf nicht null sein.");
			}
			// @todo introduce symbolic constant
			if (documentFile.getDocumentFilePk() == null) {
				throw new NuclosFatalException("godocumentfile.invalid.id");
				//"Die Id des Dokumentanhangs darf nicht null sein");
			}
			File documentDir;
			if (documentFile.getDirectoryPath() != null && documentFile.getDirectoryPath().length() > 0) {
				documentDir = new File(NuclosSystemParameters.getString(NuclosSystemParameters.DOCUMENT_PATH)+ "/" + documentFile.getDirectoryPath());
			}
			else {
				documentDir = NuclosSystemParameters.getDirectory(NuclosSystemParameters.DOCUMENT_PATH);
			}
			File file = new File(documentDir, documentFile.getDocumentFilePk() + "." + documentFile.getFilename());
			LOG.debug("Calculated path for document attachment: " + file.getCanonicalPath());
			return file.getCanonicalPath();
		}
		catch (IOException e) {
			throw new NuclosFatalException(e);
		}
	}

	public static void validateRoleDependants(IDependentDataMap mpDependants) throws CommonValidationException {

		for (UID entityUid : mpDependants.getEntityUids()) {
			RoleDependant dependant = RoleDependant.getByEntityName(entityUid);
			if (dependant != null) {
				List<String> names = CollectionUtils.transform(
					mpDependants.getData(dependant.getEntity().getUID()), 
					new EntityObjectVO.GetTypedField<String>(dependant.getEntityFieldName().getUID(),String.class));

				for (String name : names) {
					if (Collections.frequency(names, name) > 1) {
						if (dependant.getSubFieldName() != null) {
							List<String> subFieldNames = new ArrayList<String>();

							for (EntityObjectVO<?> mdVO : mpDependants.getData(dependant.getEntity().getUID()))
								if (mdVO.getFieldValue(dependant.getEntityFieldName().getUID(), String.class).equals(name))
									subFieldNames.add(mdVO.getFieldValue(dependant.getSubFieldName().getUID(), String.class));

							for (String subName : subFieldNames) {
								if (Collections.frequency(subFieldNames, subName) > 1) {
									throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage(
										"role.error.validation.dependant.sub", name, subName, dependant.getResourceId()));
								}
							}
						}
						else {
							throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage(
								"role.error.validation.dependant", name, dependant.getResourceId()));
						}
					}
				}
			}
		}
	}

	private AttributeFacadeLocal getAttributeFacade() {
		if (attributeFacade == null) {
			attributeFacade = ServerServiceLocator.getInstance().getFacade(AttributeFacadeLocal.class);
		}
		return attributeFacade;
	}

	/**
	 * 
	 * @param oldSource old E.DBSOURCE object
	 * @param newSource new E.DBSOURCE object
	 * @param createModifyDelete
	 * @throws NuclosBusinessException
	 */
	private void updateDbObject(EntityObjectVO<UID> oldSource, EntityObjectVO<UID> newSource, int createModifyDelete) throws NuclosBusinessException {
		if (oldSource == null && newSource == null) {
			throw new NuclosFatalException("oldSource and newSource must not be null.");
		} else if (oldSource != null && newSource != null 
				&& !oldSource.getFieldUid(E.DBSOURCE.dbobject).equals(newSource.getFieldUid(E.DBSOURCE.dbobject))) {
			throw new NuclosFatalException("oldSource and newSource not from same object.");
		} else if (oldSource != null && newSource != null 
				&& !oldSource.getFieldValue(E.DBSOURCE.dbtype).equals(newSource.getFieldValue(E.DBSOURCE.dbtype))) {
			throw new NuclosFatalException("Dbtype of oldSource and dbtype of newSource have to be equal.");
		}

		final String dbtype = (oldSource != null) 
				? oldSource.getFieldValue(E.DBSOURCE.dbtype) 
				: newSource.getFieldValue(E.DBSOURCE.dbtype);

		final DbAccess dbAccess = dataBaseHelper.getDbAccess();

		if (!dbAccess.getDbType().equals(DbType.getFromName(dbtype))) {
			switch (createModifyDelete) {
				case -1:
					nucletDalProvider.getEntityObjectProcessor(E.DBSOURCE).delete(new Delete<UID>(oldSource.getPrimaryKey()));
					break;
				case 0:
				case 1:
					nucletDalProvider.getEntityObjectProcessor(E.DBSOURCE).insertOrUpdate(newSource);
					final StringBuffer warnings = new StringBuffer("Update of DB Object from type '" + dbtype + 
							"' has no effect to database. Current connected database type is '" + dbAccess.getDbType().name() + "'.");
					messageService.sendMessage(new DbObjectMessage("Mit Fehlern", "Datenbank Aktualisierung", true, Collections.EMPTY_LIST, warnings));
			}
			return;
		}

		final DbObjectHelper dboHelper = new DbObjectHelper(dbAccess);

		boolean isUsedAsCalculatedAttribute = false;
		EntityMeta<?> eMetaUsingThisView = null;

		final UID objectUid = oldSource != null
				? oldSource.getFieldUid(E.DBSOURCE.dbobject) 
				: newSource.getFieldUid(E.DBSOURCE.dbobject);
		final EntityObjectVO<UID> dbObject = nucletDalProvider.getEntityObjectProcessor(E.DBOBJECT).getByPrimaryKey(objectUid);
		if (dbObject == null)
			throw new NuclosFatalException("Database object with uid \"" + objectUid + "\" does not exist");

		final String dbObjectName = dbObject.getFieldValue(E.DBOBJECT.name);
		DbObjectType type = DbObjectType.getByName(dbObject.getFieldValue(E.DBOBJECT.dbobjecttype));
		switch (type) {
		case FUNCTION:
			/**
			 * look if function is used as calculated attribute
			 */
			isUsedAsCalculatedAttribute = DbObjectHelper.isUsedAsCalculatedAttribute(dbObjectName, MetaProvider.getInstance());
			break;
		case VIEW:
			/**
			 * look if view is replacing an entity object view
			 */
			eMetaUsingThisView = dboHelper.getEntityMetaForView(dbObjectName, MetaProvider.getInstance());
		}

		/**
		 * check before any DML is executed,
		 * otherwise oracle commits the transaction and it doesn't matter if throw an exception ot not.
		 */
		if (newSource == null || !newSource.getFieldValue(E.DBSOURCE.active)) {
			if (isUsedAsCalculatedAttribute) {
				/**
				 * if in use no deactivation/delete allowed
				 */
				throw new NuclosBusinessException("masterdata.error.dbobject.isinuse.calcattr");
			}
		}

		List<String> script = new ArrayList<String>();
		StringBuffer warnings = new StringBuffer();
		MetaDbHelper schemaHelper = new MetaDbHelper(dbAccess, MetaProvider.getInstance());
		Map<String, DbTable> schema = schemaHelper.getSchema();
		//** drop entity views
		DbObjectHelper.updateViews(dbAccess, schema.values(), MetaProvider.getInstance(),
				DbStructureChange.Type.DROP, true, script, warnings);
		//** drop dbobjects
		DbObjectHelper.updateDbObjects(dbAccess, dboHelper.getAllDbObjects(null),  
				DbStructureChange.Type.DROP, true, script, warnings);
		switch (createModifyDelete) {
			case -1:
				nucletDalProvider.getEntityObjectProcessor(E.DBSOURCE).delete(new Delete(oldSource.getId()));
				break;
			case 0:
			case 1:
				nucletDalProvider.getEntityObjectProcessor(E.DBSOURCE).insertOrUpdate(newSource);
				break;
		}
		schemaHelper = new MetaDbHelper(dbAccess, MetaProvider.getInstance());
		schema = schemaHelper.getSchema();
		//** create dbobjects
		DbObjectHelper.updateDbObjects(dbAccess, dboHelper.getAllDbObjects(null), 
				DbStructureChange.Type.CREATE, true, script, warnings);
		//** create entity views
		DbObjectHelper.updateViews(dbAccess, schema.values(), MetaProvider.getInstance(),
				DbStructureChange.Type.CREATE, true, script,  warnings);
		
		messageService.sendMessage(new DbObjectMessage(
				warnings.length() == 0 ? "Erfolgreich" : "Mit Fehlern", 
				"Datenbank Aktualisierung", true, script, warnings));
	}

	/**
	 * append record grant(s) to expr for given entity.
	 * @param expr
	 * @param entity
	 * @return new AND 'condition' if any record grant(s) found, otherwise expr is returned.
	 * 
	 * @deprecated Use Spring injection instead.
	 */
	protected CollectableSearchExpression appendRecordGrants(CollectableSearchExpression expr, UID entity) {
		return grantUtils.append(expr, entity);
	}

	/**
	 * @deprecated Use Spring injection instead.
	 */
	protected CollectableSearchExpression getRecordGrantExpression(Long id, UID entity) {
		return appendRecordGrants(new CollectableSearchExpression(new CollectableIdCondition(id)), entity);
	}

	public void removeDependantTaskObjects(Object entityId) {
		if (entityId instanceof Long) {
			final Long id = (Long) entityId;
			DbStatement stmt = DbStatementUtils.deleteFrom(E.TODOOBJECT, E.TODOOBJECT.objectId, id);
			dataBaseHelper.getDbAccess().execute(stmt);
		}
	}
	
}	// class MasterDataFacadeHelper

