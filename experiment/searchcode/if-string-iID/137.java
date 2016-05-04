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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.api.rule.DeleteFinalRule;
import org.nuclos.api.rule.DeleteRule;
import org.nuclos.api.rule.InsertFinalRule;
import org.nuclos.api.rule.InsertRule;
import org.nuclos.api.rule.UpdateFinalRule;
import org.nuclos.api.rule.UpdateRule;
import org.nuclos.common.E;
import org.nuclos.common.EntityMeta;
import org.nuclos.common.EntityTreeViewVO;
import org.nuclos.common.FieldMeta;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.SF;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.UID;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdListCondition;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.ReferencingCollectableSearchCondition;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.EntityObjectToEntityTreeViewVO;
import org.nuclos.common.collection.EntityObjectToMasterDataTransformer;
import org.nuclos.common.collection.MasterDataToEntityObjectTransformer;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.DependentDataMap;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.IDependentDataMap;
import org.nuclos.common.dblayer.JoinType;
import org.nuclos.common.lucene.ILucenian;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common.metadata.NotifyObject;
import org.nuclos.common2.EntityAndField;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.TruncatableCollectionDecorator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common2.layoutml.LayoutMLParser;
import org.nuclos.common2.layoutml.exception.LayoutMLException;
import org.nuclos.server.attribute.ejb3.LayoutFacadeLocal;
import org.nuclos.server.autosync.XMLEntities;
import org.nuclos.server.common.LocalCachesUtil;
import org.nuclos.server.common.MetaProvider;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.customcode.codegenerator.WsdlCodeGenerator;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbColumnExpression;
import org.nuclos.server.dblayer.query.DbCondition;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dbtransfer.TransferFacadeLocal;
import org.nuclos.server.eventsupport.ejb3.EventSupportFacadeLocal;
import org.nuclos.server.eventsupport.valueobject.EventSupportSourceVO;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.jms.NuclosJMSUtils;
import org.nuclos.server.livesearch.ejb3.Lucenian;
import org.nuclos.server.masterdata.MasterDataProxyList;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.report.valueobject.ReportVO.ReportType;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.nuclos.server.statemodel.valueobject.StateModelUsagesCache;
import org.nuclos.server.validation.ValidationSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for all master data management functions. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Transactional(noRollbackFor= {Exception.class})
public class MasterDataFacadeBean extends NuclosFacadeBean implements MasterDataFacadeRemote {

	private static final Logger LOG = Logger.getLogger(MasterDataFacadeBean.class);

	private MasterDataFacadeHelper masterDataFacadeHelper;

	private boolean bServerValidatesMasterDataValues;
	
	private ServerParameterProvider serverParameterProvider;
	
	private ValidationSupport validationSupport;
	
	private EventSupportFacadeLocal eventSupportFacade;
	
	public MasterDataFacadeBean() {
	}
	
	@Autowired
	final void setMasterDataFacadeHelper(MasterDataFacadeHelper masterDataFacadeHelper) {
		this.masterDataFacadeHelper = masterDataFacadeHelper;
	}
	
	@Autowired
	final void setServerParameterProvider(ServerParameterProvider serverParameterProvider) {
		this.serverParameterProvider = serverParameterProvider;
	}
	
	@Autowired
	public void setValidationSupport(ValidationSupport validationSupport) {
		this.validationSupport = validationSupport;
	}
	
	protected final MasterDataFacadeHelper getMasterDataFacadeHelper() {
		return masterDataFacadeHelper;
	}

	@PostConstruct
	@RolesAllowed("Login")
	public void postConstruct() {
		this.bServerValidatesMasterDataValues = "1".equals(serverParameterProvider.getValue(
			ParameterProvider.KEY_SERVER_VALIDATES_MASTERDATAVALUES));
	}

	/**
	 * @return Is the server supposed to validate master data values before
	 *         storing them?
	 */
	private boolean getServerValidatesMasterDataValues() {
		return this.bServerValidatesMasterDataValues;
	}

	/**
	 * @param entity
	 * @param clctexpr
	 * @return a proxy list containing the search result for the given search
	 *         expression.
	 * @todo restrict permissions by entity name
	 */
    @RolesAllowed("Login")
    @Override
	public <PK> ProxyList<PK,MasterDataVO<PK>> getMasterDataProxyList(
		UID entity, List<CollectableEntityField> cefs, CollectableSearchExpression clctexpr) {
		List<EntityAndField> lstEafn = Collections.emptyList();
		final EntityMeta eMeta = MetaProvider.getInstance().getEntity(entity);
		return new MasterDataProxyList<PK>(entity, cefs, appendRecordGrants(clctexpr, eMeta), lstEafn);
	}

    @RolesAllowed("Login")
    @Override
	public <PK> TruncatableCollection<MasterDataVO<PK>> getMasterData(EntityMeta<PK> entity, 
		CollectableSearchCondition cond, boolean bAll) {
    	return getMasterData(entity.getUID(), cond, bAll);
    }
    
	/**
	 * method to get master data records for a given entity and search condition
	 *
	 * @param entityUid name of the entity to get master data records for
	 * @param cond search condition
	 * @return TruncatableCollection<MasterDataVO> collection of master data
	 *         value objects
	 * @postcondition result != null
	 * @todo restrict permissions by entity name
	 */
    @RolesAllowed("Login")
    @Override
	public <PK> TruncatableCollection<MasterDataVO<PK>> getMasterData(UID entityUid, 
		CollectableSearchCondition cond, boolean bAll) {
		if (E.REPORT.checkEntityUID(entityUid) || E.REPORTEXECUTION.checkEntityUID(entityUid)) {
			bAll = true;
		}

		final TruncatableCollection<MasterDataVO<PK>> result;
		
		// long gone (tp)
		/*
		if (E.MODULE.getDalEntity().equals(sEntityName)) {
			if (cond != null) {
				throw new CommonFatalException("Conditions for entity " + sEntityName + " are not supported.");
			}
			Collection<MasterDataVO> colResult = new ArrayList<MasterDataVO>();
			for (EntityMeta eMeta : NucletDalProvider.getInstance().getEntityMetaDataProcessor().getAll()) {
				if (eMeta.isStateModel()) {
					colResult.add(DalSupportForMD.wrapEntityMetaInModule(eMeta));
				}
			}
			result = new TruncatableCollectionDecorator<MasterDataVO>(colResult, false, colResult.size());
		} else if (E.MASTERDATA.getDalEntity().equals(sEntityName)) {
			if (cond != null) {
				throw new CommonFatalException("Conditions for entity " + sEntityName + " are not supported.");
			}
			Collection<MasterDataVO> colResult = new ArrayList<MasterDataVO>();
			for (EntityMeta eMeta : NucletDalProvider.getInstance().getEntityMetaDataProcessor().getAll()) {
				if (!eMeta.isStateModel()) {
					colResult.add(MasterDataWrapper.wrapMasterDataMetaVO(
							DalSupportForMD.wrapEntityMetaInMasterData(eMeta, 
							NucletDalProvider.getInstance().getEntityFieldMetaDataProcessor().getByParent(eMeta.getEntity()))));
				}
			}
			result = new TruncatableCollectionDecorator<MasterDataVO>(colResult, false, colResult.size());
		} else */
		if (E.ENTITYFIELDGROUP.checkEntityUID(entityUid)) {
			if (cond != null) {
				throw new CommonFatalException("Conditions for entity " + entityUid + " are not supported.");
			}
			Collection<MasterDataVO<PK>> colResult = new ArrayList<MasterDataVO<PK>>();
			for (EntityObjectVO<UID> eo : NucletDalProvider.getInstance().getEntityObjectProcessor(E.ENTITYFIELDGROUP)
					.getBySearchExpression(appendRecordGrants(new CollectableSearchExpression(cond), entityUid))) {
				colResult.add((MasterDataVO<PK>) DalSupportForMD.wrapEntityObjectVO(eo));
			}
			result = new TruncatableCollectionDecorator<MasterDataVO<PK>>(colResult, false, colResult.size());
		} else {
			TruncatableCollection<MasterDataVO<PK>> truncoll = masterDataFacadeHelper.getGenericMasterData(entityUid, cond, bAll);
			// permissions on reports and forms are given explicitly on a record per
			// record basis
			if (E.REPORT.checkEntityUID(entityUid) || E.REPORTEXECUTION.checkEntityUID(entityUid)) {
				result = filterReports(truncoll,
					SecurityCache.getInstance().getReadableReports(getCurrentUserName()));
			} else {
				result = truncoll;
			}
		}

		assert result != null;
		return result;
	}

	/**
	 * gets the ids of all masterdata objects that match a given search
	 * expression (ordered, when necessary)
	 *
	 * @param cond condition that the masterdata objects to be found must satisfy
	 * @return List<Integer> list of masterdata ids
	 */
    @RolesAllowed("Login")
    @Override
	public <PK> List<PK> getMasterDataIds(UID entity, CollectableSearchExpression cse) {
		JdbcEntityObjectProcessor<PK> eoProcessor = (JdbcEntityObjectProcessor<PK>) 
				NucletDalProvider.getInstance().getEntityObjectProcessor(entity);

		List<PK> masterDataIds = eoProcessor.getIdsBySearchExpression(appendRecordGrants(cse, entity));
		/*
		List<Object> masterDataIds = CollectionUtils.transform(eoIds, new Transformer<Long, Object>() {
			@Override public Object transform(Long l) { return l.intValue(); }
		});
		 */

		boolean bAdditionalSorting = false;
		if (cse != null && cse.isIncludingSystemData()) {
			Collection<PK> systemObjects = (Collection<PK>) XMLEntities.getSystemObjectIds(entity, cse.getSearchCondition());
			masterDataIds.addAll(systemObjects);
			bAdditionalSorting = !systemObjects.isEmpty();
		}

		if (E.isNuclosEntity(entity) && cse.getSortingOrder() != null && !cse.getSortingOrder().isEmpty() && bAdditionalSorting) {
			final UID fieldForSorting = cse.getSortingOrder().get(0).getField();
			this.sortUidList((List<UID>) masterDataIds, entity, fieldForSorting, cse.getSortingOrder().get(0).isAscending());
		}

		return masterDataIds;
	}

	/**
	 * WORKAROUND for XML entities and sorting of mixed lists with DB records
	 * Better: Send fields for sorting to DA-Layer. He has already read the DB records!
	 * @param list
	 * @param sEntityName
	 * @param sEntityFieldForSorting
	 * @param bAsc
	 */
	private void sortUidList(List<UID> list, final UID sEntityName, final UID sEntityFieldForSorting, final boolean bAsc) {
		final JdbcEntityObjectProcessor<UID> proc = NucletDalProvider.getInstance().<UID>getEntityObjectProcessor(sEntityName);
		final Collection<MasterDataVO<UID>> systemObjects = XMLEntities.getSystemObjects(sEntityName, null);
		final Collection<UID> systemObjectIds = getIds(systemObjects);
		Collections.sort(list, new Comparator<UID>() {

			@Override
			public int compare(UID o1, UID o2) {
				final boolean o1_isSystem = systemObjectIds.contains(o1);
				final boolean o2_isSystem = systemObjectIds.contains(o2);

				final Object o1_value;
				final Object o2_value;

				if (o1_isSystem) {
					o1_value = getMDVOFromList(systemObjects, o1).getFieldValue(sEntityFieldForSorting);
				} else {
					EntityObjectVO<?> eo = null;
					try {
						eo = proc.getByPrimaryKey(o1);
					} catch (Exception e) {
						// ignore
					}
					o1_value = (eo == null) ? null : eo.getFieldValue(sEntityFieldForSorting);
				}

				if (o2_isSystem) {
					o2_value = getMDVOFromList(systemObjects, o2).getFieldValue(sEntityFieldForSorting);
				} else {
					EntityObjectVO<?> eo = null;
					try {
						eo = proc.getByPrimaryKey(o2);
					} catch (Exception e) {
						// ignore
					}
					o2_value = (eo == null) ? null : eo.getFieldValue(sEntityFieldForSorting);
				}

				if (o1_value != null && o2_value != null && o1_value instanceof String && o2_value instanceof String) {
					return ((String)o1_value).compareToIgnoreCase((String) o2_value) * (bAsc ? 1 : -1);
				} else {
					return LangUtils.compare(o1_value, o2_value) * (bAsc ? 1 : -1);
				}
			}});
	}

	private <PK> Collection<PK> getIds(Collection<MasterDataVO<PK>> list) {
		Collection<PK> result = new ArrayList<PK>();
		if (list != null) {
			for (MasterDataVO<PK> mdvo : list) {
				result.add(mdvo.getPrimaryKey());
			}
		}

		return result;
	}

	private <PK> MasterDataVO<PK> getMDVOFromList(Collection<MasterDataVO<PK>> list, Object id) {
		if (list != null) {
			for (MasterDataVO<PK> mdvo : list) {
				if (mdvo.getPrimaryKey().equals(id)) {
					return mdvo;
				}
			}
		}

		return null;
	}

	/**
	 * gets the ids of all masterdata objects
	 *
	 * @return List<Integer> list of masterdata ids
	 */
    @RolesAllowed("Login")
    @Override
	public <PK> List<PK> getMasterDataIds(UID entityUid) {
		final EntityMeta mdmetacvo = MetaProvider.getInstance().getEntity(entityUid);

		// String dbEntity = mdmetacvo.getDBEntity();

		if (!dataBaseHelper.isObjectAvailable(mdmetacvo)) {
			throw new CommonFatalException(
				StringUtils.getParameterizedExceptionMessage(
					"masterdata.error.missing.table", mdmetacvo.getEntityName(), E.ENTITY.getEntityName()));
			// "Die Basistabelle/-view '"+dbEntity+"' der Entit\u00e4t '"+mdmetacvo.getEntityName()+"' existiert nicht!");
		}

		final JdbcEntityObjectProcessor<PK> eoProcessor = NucletDalProvider.getInstance().getEntityObjectProcessor(entityUid);
		final List<PK> masterDataIds = eoProcessor.getAllIds();
		/*
		List<Object> masterDataIds = CollectionUtils.transform(eoIds, new Transformer<Long, Object>() {
			@Override public Object transform(Long l) { return l.intValue(); }
		});
		 */
		masterDataIds.addAll(XMLEntities.<PK>getSystemObjectIds(entityUid, null));

		return masterDataIds;
	}

	/**
	 * @param sEntityName
	 * @param lstIntIds
	 * @param lstRequiredSubEntities
	 * @return the next chunk of the search result for a proxy list.
	 * @todo restrict permissions by entity name
	 */
    @RolesAllowed("Login")
    @Override
	public <PK> List<MasterDataVO<PK>> getMasterDataMore(
		UID sEntityName, final List<PK> lstIntIds,
		final List<EntityAndField> lstRequiredSubEntities) {

		final EntityMeta mdmetavo = MetaProvider.getInstance().getEntity(sEntityName);

		return CollectionUtils.transform(lstIntIds,
			new Transformer<PK, MasterDataVO<PK>>() {
				@Override
                public MasterDataVO<PK> transform(PK oId) {
					try {
						IDependentDataMap dmdm = null;
						for (EntityAndField eafn : lstRequiredSubEntities) {
							final UID entity = eafn.getEntity();
							Collection<MasterDataVO<PK>> collmdvo = getDependantMasterDataPk(
								entity, eafn.getField(), oId);
							if (!collmdvo.isEmpty()) {
								if (dmdm == null) {
									dmdm = new DependentDataMap();
								}
								dmdm.addAllData(entity, CollectionUtils.transform(collmdvo, 
										new MasterDataToEntityObjectTransformer<PK>()));
							}
						}
						final MasterDataVO<PK> result = masterDataFacadeHelper.getMasterDataCVOById(mdmetavo, oId, 
								false /*No check of recordgrants here*/);
						result.setDependents(dmdm);
						return result;
					}
					catch(CommonFinderException ex) {
						// This may never occur inside of a "repeatable read"
						// transaction:
						throw new CommonFatalException(ex);
					}
				}
			});
	}
    
	@RolesAllowed("Login")
	@Override
	public <PK> List<MasterDataVO<PK>> getMasterDataChunk(UID sEntityName, List<CollectableEntityField> cefs,
			final CollectableSearchExpression clctexpr, Integer istart, Integer iend) {
		List<MasterDataVO<PK>> lmdvo = masterDataFacadeHelper.getMasterDataChunk(sEntityName, cefs, clctexpr, istart, iend);
		List<MasterDataVO<PK>> mdwd = new ArrayList<MasterDataVO<PK>>();
		for (MasterDataVO<PK> mdvo : lmdvo) {
			// MasterDataVO m = new MasterDataVO(mdvo, null);
			mdwd.add(mdvo);
		}
		return mdwd;
	}
	
    @RolesAllowed("Login")
    @Override
	public Integer countMasterDataRows(UID sEntity, final CollectableSearchExpression clctexpr) {
    	return masterDataFacadeHelper.countMasterDataRows(sEntity, clctexpr);
	}
    
	/**
	 * Convenience function to get all reports or forms used in
	 * AllReportsCollectableFieldsProvider.
	 *
	 * @return TruncatableCollection<MasterDataVO> collection of master data
	 *         value objects
	 * @throws CommonFinderException if a row was deleted in the time between
	 *            executing the search and fetching the single rows.
	 * @throws CommonPermissionException
	 */
    @RolesAllowed("Login")
    @Override
	public TruncatableCollection<MasterDataVO<UID>> getAllReports() throws CommonFinderException, CommonPermissionException {
		this.checkReadAllowed(E.ROLE);
		return masterDataFacadeHelper.getGenericMasterData(E.REPORT, null, true);
	}
    
	/**
	 * convinience function to get all generations used in
	 * AllGenerationsCollectableFieldsProvider.
	 *
	 * @return TruncatableCollection<MasterDataVO> collection of master data
	 *         value objects
	 * @throws CommonFinderException if a row was deleted in the time between
	 *            executing the search and fetching the single rows.
	 * @throws CommonPermissionException
	 */
    @RolesAllowed("Login")
    @Override
	public TruncatableCollection<MasterDataVO<UID>> getAllGenerations() throws CommonFinderException, CommonPermissionException {
		this.checkReadAllowed(E.ROLE);
		return masterDataFacadeHelper.getGenericMasterData(E.GENERATION, null, true);
	}
    
	/**
	 * convinience function to get all recordgrants used in
	 * AllRecordgrantsCollectableFieldsProvider.
	 *
	 * @return TruncatableCollection<MasterDataVO> collection of master data
	 *         value objects
	 * @throws CommonFinderException if a row was deleted in the time between
	 *            executing the search and fetching the single rows.
	 * @throws CommonPermissionException
	 */
    @RolesAllowed("Login")
    @Override
	public TruncatableCollection<MasterDataVO<UID>> getAllRecordgrants() throws CommonFinderException, CommonPermissionException {
		this.checkReadAllowed(E.ROLE);
		return masterDataFacadeHelper.getGenericMasterData(E.RECORDGRANT, null, true);
	}
    
    /**
	 * execute a list of rules for the given Object
	 *
	 * @param lstRuleVO
	 * @param mdvo
	 * @param bSaveAfterRuleExecution
	 * @throws CommonBusinessException
	 * @todo restrict permission - check module id!
	 */
    @RolesAllowed("ExecuteRulesManually")
	public void executeBusinessRules(List<EventSupportSourceVO> lstRuleVO,
		MasterDataVO mdvo, boolean bSaveAfterRuleExecution, String customUsage)
		throws CommonBusinessException {
		
    	EventSupportFacadeLocal eventSupportFacade = ServerServiceLocator.getInstance().getFacade(EventSupportFacadeLocal.class);
    	EntityObjectVO fireCustomEventSupport = mdvo.getEntityObject();
    	
    	for (EventSupportSourceVO eseVO : lstRuleVO) {
    		fireCustomEventSupport = eventSupportFacade.fireCustomEventSupport(fireCustomEventSupport, eseVO, false);
    	}
    	
		if(bSaveAfterRuleExecution) {
			this.modifyVO(new MasterDataVO(fireCustomEventSupport), customUsage);
		}
	}
    
	/**
	 * filter MasterDataVO records from collmdvoReports where the id is not in
	 * collIds
	 * <p>
	 * ATTENTION: Generic tweak to use-case. They should be <UID>. (tp)
	 * </p>
	 *
	 * @param collmdvoReports
	 * @param collIds Collection<MasterDataVO>
	 * @return filtered Collection<MasterDataVO>
	 * @postcondition result != null
	 * @postcondition !result.isTruncated()
	 */
	private <PK> TruncatableCollection<MasterDataVO<PK>> filterReports(
		Collection<MasterDataVO<PK>> collmdvoReports,
		final Map<ReportType, Collection<UID>> mpReports) {
		final Collection<MasterDataVO<PK>> collmdvoResult = CollectionUtils.select(
			collmdvoReports, new Predicate<MasterDataVO<PK>>() {
				@Override
                public boolean evaluate(MasterDataVO<PK> mdvo) {
					for (ReportType rt : mpReports.keySet()) {
						if (mpReports.get(rt).contains(mdvo.getPrimaryKey())) {
							return true;
						}
					}
					return false;
				}
			});
		final TruncatableCollection<MasterDataVO<PK>> result = new TruncatableCollectionDecorator<MasterDataVO<PK>>(
			collmdvoResult, false, collmdvoResult.size());
		assert result != null;
		assert !result.isTruncated();
		return result;
	}
	
	@RolesAllowed("Login")
	public
	Collection<MasterDataVO<?>> getDependantMasterData(FieldMeta field,
			Object oRelatedId) {
		List<MasterDataVO<?>> result = CollectionUtils.transform(masterDataFacadeHelper.getDependantMasterData(field.getEntity(),
				field.getUID(), oRelatedId, getCurrentUserName()), new EntityObjectToMasterDataTransformer());
		return result;
	}
	
	@Override
	public <PK> Collection<MasterDataVO<PK>> getDependantMasterDataPk(
			UID sEntityName, UID sForeignKeyField, Object oRelatedId) {
		List<MasterDataVO<PK>> result = CollectionUtils.transform(masterDataFacadeHelper.<PK>getDependantMasterData(sEntityName,
				sForeignKeyField, oRelatedId, getCurrentUserName()), new EntityObjectToMasterDataTransformer<PK>());
		return result;
	}

	/**
	 * gets the dependant master data records for the given entity, using the
	 * given foreign key field and the given id as foreign key.
	 *
	 * @param sEntityName name of the entity to get all dependant master data
	 *           records for
	 * @param sForeignKeyField name of the field relating to the foreign entity
	 * @param oRelatedId id by which sEntityName and sParentEntity are related
	 * @return
	 * @precondition oRelatedId != null
	 * @todo restrict permissions by entity name
	 */
    @RolesAllowed("Login")
    @Override
	public Collection<MasterDataVO<?>> getDependantMasterData(UID sEntityName,
		UID sForeignKeyField, Object oRelatedId) {
		List<MasterDataVO<?>> result = CollectionUtils.transform(masterDataFacadeHelper.getDependantMasterData(sEntityName,
				sForeignKeyField, oRelatedId, getCurrentUserName()), new EntityObjectToMasterDataTransformer());
		return result;
    }
    
	public <PK, F> Collection<MasterDataVO<PK>> getDependantMasterData(
			EntityMeta<PK> entity, FieldMeta<F> foreignKeyField, F oRelatedId) {
    	List<MasterDataVO<PK>> result = CollectionUtils.transform(masterDataFacadeHelper.<PK>getDependantMasterData(entity.getUID(),
    			foreignKeyField.getUID(), oRelatedId, getCurrentUserName()), new EntityObjectToMasterDataTransformer<PK>());
		return result;
	}

    /**
	 * gets the dependant master data records for the given entity, using the
	 * given foreign key field and the given id as foreign key.
	 *
	 * @param sEntityName name of the entity to get all dependant master data
	 *           records for
	 * @param sForeignKeyField name of the field relating to the foreign entity
	 * @param oRelatedId id by which sEntityName and sParentEntity are related
	 * @return
	 * @precondition oRelatedId != null
	 * @todo restrict permissions by entity name
	 */
    @RolesAllowed("Login")
    @Override
	public <PK> Collection<MasterDataVO<PK>> getDependantMasterData(UID entity,
		UID sForeignKeyField, Object oRelatedId, Map<String, Object> mpParams) {
		Collection<MasterDataVO<PK>> result = CollectionUtils.transform(masterDataFacadeHelper.<PK>getDependantMasterData(entity,
			sForeignKeyField, oRelatedId, this.getCurrentUserName(), mpParams), new EntityObjectToMasterDataTransformer<PK>());
		return result;
	}

	@RolesAllowed("Login")
	@Override
	public <PK> Collection<EntityTreeViewVO> getDependantSubnodes(
		UID sEntityName, UID sForeignKeyField, Object oRelatedId) {
		Collection<EntityTreeViewVO> result = CollectionUtils.transform(masterDataFacadeHelper.getDependantMasterData(sEntityName,
			sForeignKeyField, oRelatedId, this.getCurrentUserName()), new EntityObjectToEntityTreeViewVO());
		return result;
	}
	

	@RolesAllowed("Login")
	public
	<PK> MasterDataVO<PK> get(EntityMeta<PK> entity, PK pk)
			throws CommonFinderException, CommonPermissionException {
		return get(entity.getUID(), pk);
	}

	/**
	 * method to get a master data value object for given primary key id
	 *
	 * @param entityUid name of the entity to get record for
	 * @param oId primary key id of master data record
	 * @return master data value object
	 * @throws CommonPermissionException
	 * @throws CommonPermissionException
	 */
    @RolesAllowed("Login")
    @Override
	public <PK> MasterDataVO<PK> get(UID entityUid, PK oId)
		throws CommonFinderException, CommonPermissionException {
    	
		// @todo This doesn't work for entities with composite primary keys
		checkReadAllowed(entityUid);
		
		getRecordGrantUtils().checkInternal(entityUid, oId);

		if ("attributegroup".equals(entityUid) || E.ENTITYFIELDGROUP.getEntityName().equals(entityUid)) {
			/**
			 * @TODO auch NuclosDalProvider? z.B. fuer Grunddaten...
			 */
			EntityObjectVO<PK> eo = NucletDalProvider.getInstance().<PK>getEntityObjectProcessor(entityUid).getByPrimaryKey(oId);
			return DalSupportForMD.wrapEntityObjectVO(eo);
		} else {
			return masterDataFacadeHelper.getMasterDataCVOById(MetaProvider.getInstance().getEntity(entityUid), oId);
		}
	}

	/**
	 * @param sEntityName
	 * @param oId
	 * @return the version of the given masterdata id.
	 * @throws CommonPermissionException
	 * @throws CommonFinderException
	 */
    @RolesAllowed("Login")
    @Override
	public Integer getVersion(UID sEntityName, Object oId)
		throws CommonFinderException, CommonPermissionException {
		return this.get(sEntityName, oId).getVersion();
	}

	/**
	 * create a new master data record
	 *
	 * @param mdvo the master data record to be created
	 * @param mpDependants map containing dependant masterdata, if any
	 * @return master data value object containing the newly created record
	 * @precondition sEntityName != null
	 * @precondition mdvo.getId() == null
	 * @precondition (mpDependants != null) -->
	 *               mpDependants.areAllDependantsNew()
	 * @nucleus.permission checkWriteAllowed(sEntityName)
	 */
    @RolesAllowed("Login")
	@Deprecated
	@Override
	public <PK> MasterDataVO<PK> create(MasterDataVO<PK> mdvo) throws CommonCreateException,
		CommonPermissionException, NuclosBusinessRuleException {
    	return create(mdvo, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
    }
    
	/**
	 * create a new master data record
	 *
	 * @param mdvo the master data record to be created
	 * @param mpDependants map containing dependant masterdata, if any
	 * @return master data value object containing the newly created record
	 * @precondition sEntityName != null
	 * @precondition mdvo.getId() == null
	 * @precondition (mpDependants != null) -->
	 *               mpDependants.areAllDependantsNew()
	 * @nucleus.permission checkWriteAllowed(sEntityName)
	 */
    @RolesAllowed("Login")
    @Override
	public <PK> MasterDataVO<PK> create(MasterDataVO<PK> mdvo, String customUsage) throws CommonCreateException,
			CommonPermissionException, NuclosBusinessRuleException {
    	ILucenian<PK> lucenian = null;
    	EntityObjectVO<PK> eo = mdvo.getEntityObject();
    	if (eo.lucenian() == null) {
    		lucenian = new Lucenian<PK>(eo, eo.getDependents());
    	}
    	MasterDataVO<PK> result = createVO(mdvo, customUsage);
    	if (lucenian != null) lucenian.store();
    	return result;
    }
    
    private EventSupportFacadeLocal getEventSupportFacade() {
    	
    	if (this.eventSupportFacade == null) {
    		this.eventSupportFacade = ServerServiceLocator.getInstance().getFacade(EventSupportFacadeLocal.class);
    	}
    	
    	return this.eventSupportFacade;
    }
    
    /*
     * This is for server-intern use only.
     */
	private <PK> MasterDataVO<PK> createVO(MasterDataVO<PK> mdvo,
		String customUsage) throws CommonCreateException,
		CommonPermissionException, NuclosBusinessRuleException {
		try {
			if(mdvo.getId() != null) {
				throw new IllegalArgumentException("mdvo.getId()");
			}
			final UID entityUid = mdvo.getEntityObject().getDalEntity();
			checkWriteAllowed(entityUid);
			
			final EntityMeta<?> eMeta = MetaProvider.getInstance().getEntity(entityUid);

			final boolean useRuleEngineSave = getEventSupportFacade().getUsesEventSupport(
					entityUid, E.SERVERCODEENTITY.getUID(), E.SERVERCODEENTITY.entity.getUID(),
					E.SERVERCODEENTITY.type.getUID(), InsertRule.NAME);
			
			if (useRuleEngineSave) {
				mdvo = fireSaveEvent(InsertRule.NAME, mdvo, customUsage);
			}

			if (E.RELATIONTYPE.checkEntityUID(entityUid)) {
				LocaleFacadeLocal localeFacade = ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);
				LocaleInfo localeInfo = localeFacade.getUserLocale();
				String sText = mdvo.getFieldValue(E.RELATIONTYPE.name.getUID(), String.class);
				String sResourceId = localeFacade.setResourceForLocale(null, localeInfo, sText);
				mdvo.setFieldValue(E.RELATIONTYPE.labelres.getUID(), sResourceId);
				if (!localeFacade.getUserLocale().equals(localeFacade.getDefaultLocale())) {
					localeFacade.setDefaultResource(sResourceId, sText);
				}
			}
			
			final EntityObjectVO<PK> validation = DalSupportForMD.getEntityObjectVO(mdvo);
			
			if (!entityUid.equals(E.HISTORY.getUID()))
				validationSupport.validate(validation, validation.getDependents());

			// create the row:
			final PK iId = masterDataFacadeHelper.createSingleRow(mdvo,
				this.getCurrentUserName(),
				this.getServerValidatesMasterDataValues(), null);
			MasterDataVO<PK> result;
			try {
				result = masterDataFacadeHelper.getMasterDataCVOById(eMeta, iId);
			}
			catch(CommonFinderException ex) {
				throw new CommonFatalException(ex);
			}

			if (mdvo.getDependents() != null && !mdvo.getDependents().isEmpty()) {
				if (!mdvo.getDependents().areAllDependentsNew()) {
					throw new IllegalArgumentException(
						"Dependants must be new (must have empty ids).");
				}

				final LayoutFacadeLocal layoutFacade = ServerServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);
				final Map<EntityAndField, UID> mpEntityAndParentEntityName = layoutFacade.getSubFormEntityAndParentSubFormEntityNames(entityUid, iId, false, customUsage);

				// create dependant rows:
				// Note that this currently works for intids only, not for composite
				// primary keys:
				final PK iParentId = result.getPrimaryKey();
				masterDataFacadeHelper.createDependants(mdvo.getDependents(), entityUid, iParentId,
					this.getCurrentUserName(),
					this.getServerValidatesMasterDataValues(), null,
					mpEntityAndParentEntityName);
			}
			
			if (E.isNuclosEntity(entityUid) && mdvo.getResources() != null) {
				LocaleFacadeLocal localeFacade = ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);
				localeFacade.setResources(entityUid, (MasterDataVO<UID>) mdvo);
			}

			if (E.WEBSERVICE.getUID().equals(entityUid)) {
				try {
					compiler.check(new WsdlCodeGenerator(mdvo), false);
				}
				catch(NuclosCompileException e) {
					throw new CommonCreateException(e);
				}
			} else if (E.NUCLET.getUID().equals(entityUid)) {
				ServerServiceLocator.getInstance().getFacade(TransferFacadeLocal.class).checkCircularReference(
						(UID) mdvo.getPrimaryKey());
			}

			boolean useRuleEngineSaveAfter = getEventSupportFacade().getUsesEventSupport(
					entityUid, E.SERVERCODEENTITY.getUID(), E.SERVERCODEENTITY.entity.getUID(),
					E.SERVERCODEENTITY.type.getUID(), InsertFinalRule.NAME);
			
			if (useRuleEngineSaveAfter) {
				try {
					LayoutFacadeLocal layoutFacade = ServerServiceLocator.getInstance().getFacade(
							LayoutFacadeLocal.class);
					Map<EntityAndField, UID> mpEntityAndParentEntityName = layoutFacade
							.getSubFormEntityAndParentSubFormEntityNames(
									entityUid, mdvo.getPrimaryKey(), false, customUsage);
					result.setDependents(readAllDependants(entityUid,
							mdvo.getPrimaryKey(), mdvo.getDependents(),
							mdvo.isRemoved(), null,
							mpEntityAndParentEntityName));
					//mpDependants = reloadDependants(sEntityName, result, true, customUsage);
					fireSaveEvent(InsertFinalRule.NAME, result, customUsage);
					result = masterDataFacadeHelper.getMasterDataCVOById(eMeta, iId);
				}
				catch (CommonFinderException ex) {
					throw new CommonFatalException(ex);
				}
			}

			MasterDataFacadeHelper.invalidateCaches(entityUid, mdvo);
			if (E.DYNAMICENTITY.checkEntityUID(entityUid))
				notifyClients(E.DYNAMICENTITY, true);
			if (E.ROLE.checkEntityUID(entityUid))
				notifyClients(E.ROLE, true);

			return result;
		}
		catch(CommonValidationException ex) {
			throw new CommonCreateException(ex.getMessage(), ex);
		}
	}

	/**
	 * modifies an existing master data record.
	 *
	 * @param mdvo the master data record
	 * @param mpDependants map containing dependant masterdata, if any
	 * @return id of the modified master data record
	 * @precondition sEntityName != null
	 * @nucleus.permission checkWriteAllowed(sEntityName)
	 */
    @RolesAllowed("Login")
	@Deprecated
	@Override
	public <PK> PK modify(MasterDataVO<PK> mdvo) throws CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonValidationException,
		CommonPermissionException, NuclosBusinessRuleException {
    	return modify(mdvo, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
    }
    /**
	 * modifies an existing master data record.
	 *
	 * @param mdvo the master data record
	 * @param mpDependants map containing dependant masterdata, if any
	 * @return id of the modified master data record
	 * @precondition sEntityName != null
	 * @nucleus.permission checkWriteAllowed(sEntityName)
	 */
    @RolesAllowed("Login")
    @Override
	public <PK> PK modify(MasterDataVO<PK> mdvo, String customUsage) throws CommonCreateException,
			CommonFinderException, CommonRemoveException,
			CommonStaleVersionException, CommonValidationException,
			CommonPermissionException, NuclosBusinessRuleException {
		EntityObjectVO<PK> eo = mdvo.getEntityObject();
		ILucenian<PK> lucenian = null;
		if (eo.lucenian() == null) {
			lucenian = new Lucenian<PK>(eo, eo.getDependents());
		}
    	PK result = modifyVO(mdvo, customUsage);
    	if (lucenian != null) lucenian.store();
    	return result;
    }
    
	protected <PK> PK modifyVO(MasterDataVO<PK> mdvo, String customUsage) throws CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonValidationException,
		CommonPermissionException, NuclosBusinessRuleException {

		final UID entityUid = mdvo.getEntityObject().getDalEntity();
		checkWriteAllowed(entityUid);
		getRecordGrantUtils().checkWriteInternal(entityUid, mdvo.getPrimaryKey());

		final MasterDataVO<PK> mdvoold = get(entityUid, mdvo.getPrimaryKey());

		final boolean useRuleEngineSave = getEventSupportFacade().getUsesEventSupport(
				entityUid, E.SERVERCODEENTITY.getUID(), E.SERVERCODEENTITY.entity.getUID(),
				E.SERVERCODEENTITY.type.getUID(), UpdateRule.NAME);
		
		if (useRuleEngineSave) {
			debug("Modifying (Start rules)");
			// dependants could be overridden by rules without deleted ones. @see NUCLOS-1329
			Collection<EntityObjectVO<?>> colRemovedDependants = mdvo.getDependents() == null 
					? Collections.EMPTY_LIST 
					: CollectionUtils.selectFlatten(mdvo.getDependents().getRoDataMap().values(), 
					new Predicate<EntityObjectVO<?>>() {
				@Override
				public boolean evaluate(EntityObjectVO<?> t) {
					return t.isFlagRemoved();
				}
			});
			// In the modify case the changes from the rules must be reflected.
			// This is the same as in create. (tp)
			mdvo = fireSaveEvent(UpdateRule.NAME, mdvo, customUsage);
			
			for (EntityObjectVO<?> mdvoDependant : colRemovedDependants) {
				if (!mdvo.getDependents().getRoDataMap().values().contains(mdvoDependant))
					mdvo.getDependents().addData(mdvoDependant.getDalEntity(), mdvoDependant);
			}
		}

		final String user = getCurrentUserName();
		if (E.ROLE.checkEntityUID(entityUid)
			&& SecurityCache.getInstance().isReadAllowedForMasterData(user, E.ROLE.getUID())) {
			if (hasUserRole(user, mdvo.getDependents())) {
				masterDataFacadeHelper.validateRoleDependants(mdvo.getDependents());

				for (EntityObjectVO<UID> mdvo_dep : mdvo.getDependents().getData(E.ROLEMASTERDATA)) {
					if (mdvo_dep.isFlagRemoved() && E.ROLE.checkEntityUID(mdvo_dep.getFieldUid(E.ROLEMASTERDATA.entity))) {
						throw new CommonFatalException("masterdata.error.role.permission");
						// "Sie d\u00fcrfen sich selber keine Rechte entziehen.");
					}
				}
			}
		}
		if (E.RELATIONTYPE.checkEntityUID(entityUid)) {
			LocaleFacadeLocal localeFacade = ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);
			LocaleInfo localeInfo = localeFacade.getUserLocale();
			String sResourceId = mdvo.getFieldValue(E.RELATIONTYPE.labelres);
			String sText = mdvo.getFieldValue(E.RELATIONTYPE.name);
			sResourceId = localeFacade.setResourceForLocale(sResourceId, localeInfo, sText);
			mdvo.setFieldValue(E.RELATIONTYPE.labelres, sResourceId);
		}
		
		EntityObjectVO<PK> validation = mdvo.getEntityObject();
		validationSupport.validate(validation, mdvo.getDependents());

		// modify the row itself:
		final PK result;
//		if (DalConstants.ENTITY_NAME_FIELDGROUP.equals(sEntityName)) {
//			EntityObjectVO eo = DalSupportForMD.getEntityObjectVO(mdvo);
//			DalUtils.handleVersionUpdate(NucletDalProvider.getInstance().getEntityObjectProcessor(sEntityName), eo, getCurrentUserName());
//			NucletDalProvider.getInstance().getEntityObjectProcessor(sEntityName).insertOrUpdate(eo);
//			result = mdvo.getId();
//		} else {
			result = masterDataFacadeHelper.modifySingleRow(entityUid, mdvo, user, this.getServerValidatesMasterDataValues(), customUsage);
//		}
			
		if (E.getByUID(entityUid) != null && mdvo.getResources() != null) {
			LocaleFacadeLocal localeFacade = ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);
			localeFacade.setResources(entityUid, (MasterDataVO<UID>) mdvo);
		}
		if(mdvo.getDependents() != null) {
			modifyDependants(entityUid, mdvo.getPrimaryKey(), mdvo.isRemoved(),
					mdvo.getDependents(), customUsage);
		}
		if (E.WEBSERVICE.getUID().equals(entityUid)) {
			try {
				compiler.check(new WsdlCodeGenerator(mdvo), false);
			}
			catch(NuclosCompileException e) {
				throw new CommonCreateException(e);
			}
		} else if (E.NUCLET.getUID().equals(entityUid)) {
			ServerServiceLocator.getInstance().getFacade(TransferFacadeLocal.class).checkCircularReference(
					(UID) mdvo.getPrimaryKey());
		}

		
		final boolean useRuleEngineSaveAfter = 
				getEventSupportFacade().getUsesEventSupport(
						entityUid, E.SERVERCODEENTITY.getUID(), E.SERVERCODEENTITY.entity.getUID(),
						E.SERVERCODEENTITY.type.getUID(), UpdateFinalRule.NAME);
		
		if(useRuleEngineSaveAfter) {
			try {
				this.debug("Modifying (Start rules after save)");
				MasterDataVO updated = get(entityUid, result);
				LayoutFacadeLocal layoutFacade = ServerServiceLocator.getInstance().getFacade(
						LayoutFacadeLocal.class);
					Map<EntityAndField, UID> mpEntityAndParentEntityName = layoutFacade.getSubFormEntityAndParentSubFormEntityNames(
							entityUid, updated.getPrimaryKey(), false, customUsage);
					updated.setDependents(readAllDependants(entityUid,
						updated.getPrimaryKey(), updated.getDependents(),
						updated.isRemoved(), null,
					mpEntityAndParentEntityName));
				
				this.fireSaveEvent(UpdateFinalRule.NAME, updated,customUsage);
			}
			catch (CommonFinderException ex) {
				throw new CommonFatalException(ex);
			}
		}
		 
		if (entityUid.equals(E.STATE.getUID())
				|| entityUid.equals(E.GENERATION.getUID())
					||entityUid.equals(E.RULE.getUID())) {
			modifyParentLayouts(entityUid, mdvo, mdvoold);
		}

		MasterDataFacadeHelper.invalidateCaches(entityUid, mdvo);
		if (E.DYNAMICENTITY.checkEntityUID(entityUid))
			notifyClients(E.DYNAMICENTITY, true);
		if (E.ROLE.checkEntityUID(entityUid))
			notifyClients(E.ROLE, true);

		return result;
	}
	
	private void modifyParentLayouts(UID sEntity, MasterDataVO<?> mdvo, MasterDataVO<?> mdvoold) {
		final String newArgument;
		final String oldArgument;
		final List<UID> collEntityUsages = new ArrayList<UID>(); 
		if (sEntity.equals(E.RULE.getUID())) {
			newArgument = mdvo.getFieldValue(E.RULE.rule); 
			oldArgument = mdvoold.getFieldValue(E.RULE.rule); 
			for (MasterDataVO<?> mdUsage : getDependantMasterData(E.RULEUSAGE.getUID(), E.RULEUSAGE.rule.getUID(), mdvo.getId())) {
				collEntityUsages.add(mdUsage.getFieldUid(E.RULEUSAGE.entity));
			}
		} else if (sEntity.equals(E.GENERATION.getUID())) {
			newArgument = mdvo.getFieldValue(E.GENERATION.name); 
			oldArgument = mdvoold.getFieldValue(E.GENERATION.name); 
			collEntityUsages.add(mdvo.getFieldUid(E.GENERATION.sourceModule));
		} else if (sEntity.equals(E.STATE.getUID())) {
			for (UID iStateModelId : StateModelUsagesCache.getInstance().getStateUsages().getStateModelUIDsByStatusUID((UID) mdvo.getPrimaryKey())) {
				for (UsageCriteria uc : StateModelUsagesCache.getInstance().getStateUsages().getUsageCriteriaByStateModelUID(iStateModelId)) {
					collEntityUsages.add(uc.getEntityUID());
				}
			}
			newArgument = mdvo.getFieldValue(E.STATE.numeral).toString(); 
			oldArgument = mdvoold.getFieldValue(E.STATE.numeral).toString(); 
		} else
			throw new NuclosFatalException(sEntity.toString());
		
		// check if arguments has changed.
		if (LangUtils.equals(oldArgument, newArgument))
				return; // nothing to do
		
		for (UID sParentEntity : collEntityUsages) {
			Set<UID> lstLayouts = new HashSet<UID>();
			CollectableComparison compare = SearchConditionUtils.newComparison(
					E.LAYOUTUSAGE.entity, ComparisonOperator.EQUAL, sParentEntity);
			for (MasterDataVO<?> layout : getMasterData(E.LAYOUTUSAGE.getUID(), compare, true)) {
				lstLayouts.add(layout.getFieldUid(E.LAYOUTUSAGE.layout));
			}
			for (UID iLayoutId : lstLayouts) {
				try {
					MasterDataVO<UID> voLayout = get(E.LAYOUT.getUID(), iLayoutId);

					String sLayout = (String)voLayout.getFieldValue(E.LAYOUT.layoutML);
					
					try {
						sLayout = new LayoutMLParser(MetaProvider.getInstance()).replaceButtonArguments(sLayout, sEntity, newArgument, oldArgument);
					} catch (LayoutMLException e) {
						throw new NuclosFatalException(e);
					}
					
					voLayout.setFieldValue(E.LAYOUT.layoutML, sLayout);
					
					try {
			            modifyVO(voLayout, null);
		            }
		            catch(CommonBusinessException e) {
		            	throw new NuclosFatalException(e);
		            }
				}
				catch(Exception e) {
					// don't modify layout
					LOG.info("searchParentLayouts failed: " + e);
				}
			}
		}
	}


	/**
	 * notifies clients that the contents of an entity has changed.
	 *
	 * @param sCachedEntityName name of the cached entity.
	 * @precondition sCachedEntityName != null
	 */
	// @Override
	public void notifyClients(UID sCachedEntityName) {
		masterDataFacadeHelper.notifyClients(sCachedEntityName);
	}

	/**
	 * notifies clients that the meta data has changed, so they can invalidate their local caches.
	 * <p>
	 * TODO: Why on hell does this method sends to TOPICNAME_METADATACACHE but the above <code>notifyClients</code>
	 * sends to TOPICNAME_MASTERDATACACHE???
	 * </p>
	 */
	protected void notifyClients(EntityMeta<?> entity, boolean refreshMenus) {
		LOG.info("JMS send: notify clients that entity " + entity.getEntityName() + " changed:" + this);
		LocalCachesUtil.getInstance().updateLocalCacheRevalidation(JMSConstants.TOPICNAME_METADATACACHE);
		NuclosJMSUtils.sendOnceAfterCommitDelayed(new NotifyObject(entity.getUID(), refreshMenus), JMSConstants.TOPICNAME_METADATACACHE);
	}

	private boolean hasUserRole(String sUser, IDependentDataMap mpDependants) {
//		final UserFacadeLocal ufl = SpringApplicationContextHolder.getApplicationContext().getBean(UserFacadeLocal.class);
		final UID userUid = SecurityCache.getInstance().getUserUid(sUser);
		if (mpDependants != null) {
			for (EntityObjectVO<UID> mdvo : mpDependants.getData(E.ROLEUSER)) {
				if (mdvo.getFieldUid(E.ROLEUSER.user).equals(userUid)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * method to delete an existing master data record
	 *
	 * @param mdvo containing the master data record
	 * @param bRemoveDependants remove all dependants if true, else remove only
	 *           given (single) mdvo record this is helpful for entities which
	 *           have no layout
	 * @precondition sEntityName != null
	 * @nucleus.permission checkDeleteAllowed(sEntityName)
	 */
    @RolesAllowed("Login")
    @Override
	public <PK> void remove(MasterDataVO<PK> mdvo,
		boolean bRemoveDependants) throws CommonFinderException,
		CommonRemoveException, CommonStaleVersionException,
		CommonPermissionException, NuclosBusinessRuleException {
    	remove(mdvo, bRemoveDependants, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
    }
    
	@Override
    public void insertInto(UID table, UID column1, Object value1, Object[] varargs) 
			throws CommonCreateException {
		masterDataFacadeHelper.insertInto(table, column1, value1, varargs);
	}

	@Override
	public void deleteFrom(UID table, UID column1, Object value1, Object[] varargs) 
			throws CommonRemoveException {
		masterDataFacadeHelper.deleteFrom(table, column1, value1, varargs);
	}

	/**
	 * method to delete an existing master data record
	 *
	 * @param mdvo containing the master data record
	 * @param bRemoveDependants remove all dependants if true, else remove only
	 *           given (single) mdvo record this is helpful for entities which
	 *           have no layout
	 * @precondition sEntityName != null
	 * @nucleus.permission checkDeleteAllowed(sEntityName)
	 */
    @RolesAllowed("Login")
    @Override
	public <PK> void remove(MasterDataVO<PK> mdvo,
		boolean bRemoveDependants, String customUsage) throws CommonFinderException,
		CommonRemoveException, CommonStaleVersionException,
		CommonPermissionException, NuclosBusinessRuleException {
    	
		EntityObjectVO<?> eo = mdvo.getEntityObject();
		ILucenian<?> lucinian = null;
		if (eo.lucenian() == null) {
			lucinian = new Lucenian(eo);
		}
		final UID entityUid = mdvo.getEntityObject().getDalEntity(); 
		checkDeleteAllowed(entityUid);
		getRecordGrantUtils().checkDeleteInternal(entityUid, mdvo.getPrimaryKey());

		mdvo.remove();
		
		this.fireDeleteEvent(mdvo, false, customUsage);
		
		if (bRemoveDependants) {
			LayoutFacadeLocal layoutFacade = ServerServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);
			Map<EntityAndField, UID> mpEntityAndParentEntityName = layoutFacade.getSubFormEntityAndParentSubFormEntityNames(
				entityUid, mdvo.getPrimaryKey(), false, customUsage);
			IDependentDataMap mdp = readAllDependants(entityUid, mdvo.getPrimaryKey(),
				mdvo.getDependents(), mdvo.isRemoved(), null, mpEntityAndParentEntityName);
			masterDataFacadeHelper.removeDependants(mdp, customUsage);
		}

		if (E.WEBSERVICE.getEntityName().equals(entityUid)) {
			try {
				compiler.check(new WsdlCodeGenerator(mdvo), true);
			}
			catch(NuclosCompileException e) {
				throw new CommonRemoveException(e);
			}
		}

		masterDataFacadeHelper.removeSingleRow(entityUid, mdvo, customUsage);

		// Note that the dependants are removed via cascading delete in the database.
		masterDataFacadeHelper.removeDependantTaskObjects(mdvo.getPrimaryKey()); 
		//explicit delete, because it is not a reference, so no db constraint available

		if (E.RELATIONTYPE.checkEntityUID(entityUid)) {
			LocaleFacadeLocal localeFacade = ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);
			String sResourceId = mdvo.getFieldValue(E.RELATIONTYPE.labelres);
			localeFacade.deleteResource(sResourceId);
		}

		fireDeleteEvent(mdvo, true, customUsage);

		MasterDataFacadeHelper.invalidateCaches(entityUid, mdvo);
		if (E.DYNAMICENTITY.checkEntityUID(entityUid))
			notifyClients(E.DYNAMICENTITY, true);
		if (E.ROLE.checkEntityUID(entityUid))
			notifyClients(E.ROLE, true);

		if (isInfoEnabled()) {
			final String sMessage = "Der Eintrag mit der Id " + mdvo.getId()
				+ " in der Entit\u00e4t " + entityUid + " wurde gel\u00f6scht.";
			info(sMessage);
		}		
		if (lucinian != null) lucinian.store();
	}

	/**
	 * fires a Save event, executing the corresponding business rules.
	 *
	 * @param mdvo
	 * @param mpDependants
	 * @return
	 * @throws CreateException
	 * @throws NuclosBusinessRuleException
	 */
	private <T> MasterDataVO<T> fireSaveEvent(String event, MasterDataVO<T> mdvo, String customUsage)
		throws NuclosBusinessRuleException {
		
		EntityObjectVO<T> eoVO = mdvo.getEntityObject();
		
		eoVO = ServerServiceLocator.getInstance().getFacade(EventSupportFacadeLocal.class).
				fireSaveEventSupport(mdvo.getEntityObject(), event, extractUsageCriteria(eoVO, customUsage));
				
		return new MasterDataVO<T>(eoVO);
	}

	private static <T> UsageCriteria extractUsageCriteria(EntityObjectVO<T> eoVO, String customUsage) {
		
		final EntityMeta<T> eMeta = MetaProvider.getInstance().getEntity(eoVO.getDalEntity());	
		
		UID process = null;
		UID status = null;
		if (eMeta.isStateModel()) {
			process = SF.PROCESS.getUID(eoVO.getDalEntity());
			status = SF.STATE.getUID(eoVO.getDalEntity());
		}
		
		return new UsageCriteria(eoVO.getDalEntity(), process, status, customUsage);
	}
	/**
	 * fires a Delete event, executing the corresponding business rules.
	 *
	 * @param mdvo
	 * @param mpDependants
	 * @return
	 * @throws CreateException
	 * @throws NuclosBusinessRuleException
	 */
	private <PK> void fireDeleteEvent(MasterDataVO<PK> mdvo, boolean after, String customUsage) throws NuclosBusinessRuleException {

		final EntityObjectVO<PK> eoVO = mdvo.getEntityObject();
		final UsageCriteria usage = extractUsageCriteria(eoVO, customUsage);
		
		// EventSupports
		String sSupportType = DeleteRule.NAME;
		if (after) {
			sSupportType = DeleteFinalRule.NAME;
		}
		
		ServerServiceLocator.getInstance().getFacade(EventSupportFacadeLocal.class).fireDeleteEventSupport(
				eoVO, sSupportType, usage, true);	
	}
	
	/**
	 * Get all subform entities of a masterdata entity
	 *
	 * @param entityUid
	 */
    @RolesAllowed("Login")
    @Override
	public Set<EntityAndField> getSubFormEntitiesByMasterDataEntity(UID entityUid, String customUsage) {
		LayoutFacadeLocal layoutFacade = ServerServiceLocator.getInstance().getFacade(
			LayoutFacadeLocal.class);

		if (!StringUtils.isNullOrEmpty(layoutFacade.getMasterDataLayoutForEntity(entityUid))) {
			Map<EntityAndField, UID> mpEntityAndParentEntityName = layoutFacade.getSubFormEntityAndParentSubFormEntityNames(
				entityUid, entityUid, false, customUsage);
			return new HashSet<EntityAndField>(mpEntityAndParentEntityName.keySet());
		}
		return Collections.emptySet();
	}

	/**
	 * read all dependant masterdata recursively if necessary, mark the read data
	 * as removed
	 *
	 * @param sEntityName
	 * @param mdvo
	 */
    @RolesAllowed("Login")
	public <PK> IDependentDataMap readAllDependants(UID sEntityName,
		PK iId, IDependentDataMap mpDependants, Boolean bRemoved,
		UID sParentEntity,
		Map<EntityAndField, UID> mpEntityAndParentEntityName) {
		Collection<EntityObjectVO<?>> collmdvo = Collections.<EntityObjectVO<?>>emptyList();

		// last subform in hierarchie found
		if(mpEntityAndParentEntityName.containsValue(sParentEntity)) {
			for(EntityAndField eafn : mpEntityAndParentEntityName.keySet()) {
				// first subform in hierarchie found or
				// child subfrom found
				final UID entity = eafn.getEntity();
				EntityMeta eMeta = MetaProvider.getInstance().getEntity(entity);
				if (!eMeta.isEditable()) {
					continue;
				}
				if ((mpEntityAndParentEntityName.get(eafn) == null && sParentEntity == null)
					|| (mpEntityAndParentEntityName.get(eafn) != null 
					&& mpEntityAndParentEntityName.get(eafn).equals(sParentEntity))) {
					if (!mpDependants.getData(entity).isEmpty()) {
						collmdvo = CollectionUtils.emptyIfNull(mpDependants.getData(entity));
					}
					else {
						if (iId != null) {
							Collection<EntityObjectVO<?>> col = CollectionUtils.transform(getDependantMasterData(
								entity,
								masterDataFacadeHelper.getForeignKeyFieldUID(sEntityName, entity, mpEntityAndParentEntityName), 
								iId), 
								new MasterDataToEntityObjectTransformer());

							collmdvo = CollectionUtils.emptyIfNull(col);
							// mpDependants.addAllData(entity, collmdvo);
							for (EntityObjectVO<?> dep: collmdvo) {
								mpDependants.addData(entity, dep);
							}
						}
					}

					for (EntityObjectVO dmdvo : collmdvo) {
						if (bRemoved) {
							dmdvo.flagRemove();
						}
						dmdvo.setDependents(readAllDependants(eafn.getEntity(),
							dmdvo.getPrimaryKey(), dmdvo.getDependents(),
							dmdvo.isFlagRemoved(), eafn.getEntity(),
							mpEntityAndParentEntityName));
					}
				}
			}
		}
		return mpDependants;
	}
    
    /**
	 * create the given dependants (local use only).
	 *
	 * @param dependants
	 * @precondition mpDependants != null
	 */
    // @Override
    public void createDependants(UID entityUid, Object id, Boolean removed,
		IDependentDataMap dependants, String customUsage) throws CommonCreateException, CommonPermissionException {
    	try {
    		flagNew(dependants);
    		createOrModifyDependants(entityUid, id, removed, dependants, false, false, customUsage);
    	}
		catch (CommonFinderException ex) {
			// This must never happen when inserting a new object:
			throw new CommonFatalException(ex);
		}
		catch (CommonStaleVersionException ex) {
			// This must never happen when inserting a new object:
			throw new CommonFatalException(ex);
		}
		catch (CommonRemoveException ex) {
			// This must never happen when inserting a new object:
			throw new CommonFatalException(ex);
		}
	}
    
    private void flagNew(IDependentDataMap dependants) {
    	if (dependants == null) {
    		return;
    	}
    	for (List<EntityObjectVO<?>> list : dependants.getRoDataMap().values()) {
    		for (EntityObjectVO<?> eovo: list) {
	    		eovo.flagNew();
	    		eovo.setVersion(1);
	    		eovo.setPrimaryKey(null);
	    		eovo.setCreatedBy(null);
	    		eovo.setCreatedAt(null);
	    		flagNew(eovo.getDependents());
    		}
    	}
    }
    
    /**
	 * modifies the given dependants (local use only).
	 *
	 * @param dependants
	 * @precondition mpDependants != null
	 */
    // @Override
    public void modifyDependants(UID entityName, Object id, Boolean removed,
		IDependentDataMap dependants, String customUsage) throws CommonCreateException,
		CommonFinderException, CommonRemoveException, CommonPermissionException,
		CommonStaleVersionException {
    	createOrModifyDependants(entityName, id, removed, dependants, true, true, customUsage);
    }
    
	/**
	 * modifies the given dependants (local use only).
	 *
	 * @param dependants
	 * @precondition mpDependants != null
	 */
	// @Override
	public void modifyDependants(UID entityName, Object id, Boolean removed,
			IDependentDataMap dependants, boolean read, String customUsage) throws CommonCreateException,
			CommonFinderException, CommonRemoveException, CommonPermissionException,
			CommonStaleVersionException {
		createOrModifyDependants(entityName, id, removed, dependants, read, true, customUsage);
	}

     private void createOrModifyDependants(UID entityName, Object id, Boolean removed,
		IDependentDataMap dependants, boolean read, boolean remove, String customUsage) throws CommonCreateException,
		CommonFinderException, CommonRemoveException, CommonPermissionException,
		CommonStaleVersionException {
		
		if (dependants == null) {
			throw new NullArgumentException("dependants");
		}

		LayoutFacadeLocal layoutFacade = ServerServiceLocator.getInstance().getFacade(
			LayoutFacadeLocal.class);
		Map<EntityAndField, UID> mpEntityAndParentEntityName = layoutFacade.getSubFormEntityAndParentSubFormEntityNames(
			entityName, id, false, customUsage);
		
		if (read) {
			readAllDependants(entityName, id, dependants, removed, null,
				mpEntityAndParentEntityName);
		}
		if (remove) {
			masterDataFacadeHelper.removeDependants(dependants, customUsage);
		}

		try {
			masterDataFacadeHelper.createOrModifyDependants(dependants, entityName,
				this.getCurrentUserName(),
				this.getServerValidatesMasterDataValues(), null,
				mpEntityAndParentEntityName, customUsage);
		}
		catch(CommonValidationException ex) {
			// @todo check this exception handling
			throw new CommonCreateException(ex.getMessage(), ex);
		}
	}

	/**
	 * revalidates the cache. This may be used for development purposes only, in
	 * order to rebuild the cache after metadata entries in the database were
	 * changed.
	 */
    @RolesAllowed("UseManagementConsole")
    @Override
	public void revalidateMasterDataMetaCache(boolean otherCaches, boolean refreshMenus) {
		MetaProvider.getInstance().revalidate(otherCaches, refreshMenus);
	}

	/**
	 * value list provider function (get processes by usage)
	 *
	 * @param iModuleId module id of usage criteria
	 * @param bSearchMode when true, validity dates and/or active sign will not
	 *           be considered in the search.
	 * @return collection of master data value objects
	 */
    @RolesAllowed("Login")
    @Override
	public List<CollectableField> getProcessByUsage(
		UID iModuleId, boolean bSearchMode) {
		// @todo Try to replace with getDependantMasterData

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom process = query.from(E.PROCESS, "process");
		query.multiselect(process.basePk(), process.baseColumn(E.PROCESS.name));
		DbCondition condition = builder.equalValue(process.baseColumn(E.PROCESS.module), iModuleId);
		if (!bSearchMode) {
			DbColumnExpression<Date> datValidFrom = process.baseColumn(E.PROCESS.validFrom);
			DbColumnExpression<Date> datValidUntil = process.baseColumn(E.PROCESS.validUntil);
			condition = builder.and(
				condition,
				builder.or(builder.lessThanOrEqualTo(datValidFrom, builder.currentDate()), datValidFrom.isNull()),
				builder.or(builder.greaterThanOrEqualTo(datValidUntil, builder.currentDate()), datValidUntil.isNull()));
		}
		query.where(condition);

		return dataBaseHelper.getDbAccess().executeQuery(query, new Transformer<DbTuple, CollectableField>() {
			@Override
			public CollectableField transform(DbTuple t) {
				return new CollectableValueIdField(t.get(0, Object.class), t.get(1, String.class));
			}
		});
	}

	/**
	 * @param iModuleId the id of the module whose subentities we are looking for
	 * @return Collection<MasterDataMetaVO> the masterdata meta information for
	 *         all entities having foreign keys to the given module.
	 */
    @Override
    public List<CollectableField> getSubEntities(UID iModuleId) {

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom m = query.from(E.ENTITY, "m");
		DbFrom mf = m.joinOnBasePk(E.ENTITYFIELD, JoinType.INNER, E.ENTITYFIELD.entity, "mf");
		DbFrom p = mf.joinOnJoinedPk(E.ENTITY, JoinType.INNER, E.ENTITYFIELD.foreignentity, "p");
		query.multiselect(m.basePk(),	m.baseColumn(E.ENTITY.entity));
		query.where(builder.equalValue(p.basePk(), iModuleId));
		query.orderBy(builder.asc(m.baseColumn(E.ENTITY.entity)));

		return dataBaseHelper.getDbAccess().executeQuery(query, new Transformer<DbTuple, CollectableField>() {
			@Override
			public CollectableField transform(DbTuple t) {
				return new CollectableValueIdField(t.get(0, Object.class), t.get(1, String.class));
			}
		});
	}

    @Override
    public Map<String, String> getRuleEventsWithLocaleResource() {
		Map<String, String> mp = CollectionUtils.newHashMap();
		// TODO_AUTOSYNC: Re-merge with database, add resourceId to metadata
		for(MasterDataVO<UID> mdvo : XMLEntities.getData(E.EVENT).getAll()) {
			mp.put(mdvo.getFieldValue(E.EVENT.name), mdvo.getFieldValue(E.EVENT.labelres));
		}
		return mp;
	}

	/**
	 * Validate all masterdata entries against their meta information (length,
	 * format, min, max etc.). The transaction type is "not supported" here in
	 * order to avoid a transaction timeout, as the whole operation may take some
	 * time.
	 *
	 * @param sOutputFileName the name of the csv file to which the results are
	 *           written.
	 *           
	 * @deprecated Very old cruft. Validate is not implemented any more. (tp)
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED, noRollbackFor= {Exception.class})
	@RolesAllowed("UseManagementConsole")
	public void checkMasterDataValues(String sOutputFileName) {
		throw new UnsupportedOperationException();
		
		/*
		final PrintStream ps;
		try {
			ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(
				sOutputFileName)), true);
		}
		catch(FileNotFoundException ex) {
			throw new NuclosFatalException(
				StringUtils.getParameterizedExceptionMessage(
					"masterdata.error.missing.file", sOutputFileName), ex);
		}

		ps.println("Entit\u00e4t; ID; Fehlermeldung");
		for (EntityMeta mdmcvo : MetaProvider.getInstance().getAllMetaData()) {
			final String sEntityName = mdmcvo.getEntity();
			try {
				for (MasterDataVO mdvo : masterDataFacadeHelper.getGenericMasterData(sEntityName, null, true)) {
					try {
						// validate each record
						mdvo.validate(mdmcvo);
					}
					catch(CommonValidationException ex) {
						final StringBuilder sbResult = new StringBuilder();
						sbResult.append(sEntityName);
						sbResult.append(";");
						sbResult.append(mdvo.getId());
						sbResult.append(";");
						sbResult.append(ex.getMessage());
						ps.println(sbResult.toString());
					}
				}
			}
			catch(Exception e) {
				LOG.error("checkMasterDataValues failed: " + e, e);
				error("Error while validating entity " + sEntityName);
			}
		}
		if(ps != null) {
			ps.close();
		}
		if(ps != null && ps.checkError()) {
			throw new NuclosFatalException("Failed to close PrintStream.");
		}
		*/
	}

	
	/**
	 * @param sEntityName
	 * @param iId the object's id (primary key)
	 * @return the masterdata object with the given entity and id.
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 * @Deprecated use with customUsage
	 */
    @Deprecated
    // @Override
    public <PK> MasterDataVO<PK> getWithDependants(UID sEntityName,
    		PK iId) throws CommonFinderException, NuclosBusinessException,
    		CommonPermissionException {
    	return getWithDependants(sEntityName, iId, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
    }
    
    // @Override
    public <PK> MasterDataVO<PK> getWithDependants(UID entityUid,
		PK iId, String customUsage) throws CommonFinderException, NuclosBusinessException,
		CommonPermissionException {
		if (iId == null) {
			throw new NullArgumentException("iId");
		}
		List<EntityAndField> lsteafn = new ArrayList<EntityAndField>();

		LayoutFacadeLocal layoutFacade = ServerServiceLocator.getInstance().getFacade(
			LayoutFacadeLocal.class);
		for(EntityAndField eafn : layoutFacade.getSubFormEntityAndParentSubFormEntityNames(
			entityUid, iId, false, customUsage).keySet()) {
			lsteafn.add(eafn);
		}

		final MasterDataVO<PK> result = get(entityUid, iId);
		result.setDependents(getDependants(iId, lsteafn));
		return result;
		// return new MasterDataVO(get(sEntityName, iId), getDependants(iId, lsteafn));
	}
    
	public <PK> Collection<MasterDataVO<PK>> getWithDependantsByCondition(
			EntityMeta<PK> entity, CollectableSearchCondition cond, String customUsage) {
		return getWithDependantsByCondition(entity.getUID(), cond, customUsage);
	}

	/**
	 * @param sEntityName
	 * @param cond search condition
	 * @return the masterdata objects for the given entityname and search
	 *         condition.
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 */
    public <PK> Collection<MasterDataVO<PK>> getWithDependantsByCondition(
		UID sEntityName, CollectableSearchCondition cond, String customUsage) {
		Collection<MasterDataVO<PK>> result = new ArrayList<MasterDataVO<PK>>();

		for(MasterDataVO<PK> mdVO : this.<PK>getMasterData(sEntityName, cond, true)) {
			List<EntityAndField> lsteafn = new ArrayList<EntityAndField>();

			LayoutFacadeLocal layoutFacade = ServerServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);
			for(EntityAndField eafn : layoutFacade.getSubFormEntityAndParentSubFormEntityNames(
				sEntityName, mdVO.getPrimaryKey(), false, customUsage).keySet()) {
				lsteafn.add(eafn);
			}
			mdVO.setDependents(getDependants(mdVO.getPrimaryKey(), lsteafn));
			// result.add(new MasterDataVO(mdVO, getDependants(mdVO.getPrimaryKey(), lsteafn)));
			result.add(mdVO);
		}
		return result;
	}

    // @Override
	public IDependentDataMap reloadDependants(UID entityname, MasterDataVO<?> mdvo, boolean bAll, String customUsage) throws CommonFinderException {
		LayoutFacadeLocal layoutFacade = ServerServiceLocator.getInstance().getFacade(LayoutFacadeLocal.class);

		final Map<EntityAndField, UID> collSubEntities = layoutFacade.getSubFormEntityAndParentSubFormEntityNames(
				entityname, mdvo.getPrimaryKey(), false, customUsage);
		return getDependants(mdvo.getPrimaryKey(), new ArrayList<EntityAndField>(collSubEntities.keySet()));
	}

    // @Override
	public <PK> IDependentDataMap getDependants(Object oId, List<EntityAndField> lsteafn) {
		final DependentDataMap result = new DependentDataMap();
		for(EntityAndField eafn : lsteafn) {
			final UID entity = eafn.getEntity();
			Collection<EntityObjectVO<PK>> col = CollectionUtils.transform(getDependantMasterData(
				entity, eafn.getField(), oId), new MasterDataToEntityObjectTransformer());

			result.addAllData(eafn.getEntity(), col);
		}
		return result;
	}

	/**
	 * gets the file content of a generic object document
	 *
	 * @param iGenericObjectDocumentId generic object document id
	 * @return generic object document file content
	 * @todo restrict permission - check module id!
	 */
    @RolesAllowed("Login")
    @Override
	public byte[] loadContent(Long iGenericObjectDocumentId, String sFileName, String sPath)
		throws CommonFinderException {
		final java.io.File documentDir = new File(NuclosSystemParameters.getString(NuclosSystemParameters.DOCUMENT_PATH) + "/" +
			StringUtils.emptyIfNull(sPath) + "/");
		if (iGenericObjectDocumentId == null) {
			throw new NuclosFatalException("godocumentfile.invalid.id");// "Die Id des Dokumentanhangs darf nicht null sein");
		}
		java.io.File file = new java.io.File(documentDir, iGenericObjectDocumentId + "." + sFileName);

		try {
			return IOUtils.readFromBinaryFile(file);
		}
		catch(IOException e) {
			throw new NuclosFatalException(e);
		}
	}

	/**
	 * @param user - the user for which to get subordinated users
	 * @return List<MasterDataVO> list of masterdata valueobjects
	 */
    @Override
	public Collection<MasterDataVO<UID>> getUserHierarchy(String user) {
		boolean isSuperUser = SecurityCache.getInstance().isSuperUser(user);
		if (!isSuperUser) {
			List<UID> roles = new ArrayList<UID>();
			roles.addAll(getRolesHierarchyForUser(user));
			return getUsersForRoles(roles);
		} else {
			// return new ArrayList<MasterDataVO<UID>>(getMasterData(E.USER.getUID(), null, false));
			return CollectionUtils.unsaveCopyCollection(getMasterData(E.USER.getUID(), null, false));
		}
	}

	private Set<UID> getRolesHierarchyForUser(String user) {
		Set<UID> roles = new HashSet<UID>();
		Collection<MasterDataVO<Object>> userRoles = getMasterData(E.ROLEUSER.getUID(), 
				SearchConditionUtils.newComparison(E.ROLEUSER.user, ComparisonOperator.EQUAL, user), false);
		for (MasterDataVO voRole : userRoles) {
			roles.add(voRole.getFieldUid(E.ROLEUSER.role));
			addSubordinateRoles(voRole.getFieldUid(E.ROLEUSER.role), roles);
		}
		return roles;
	}

	private void addSubordinateRoles(UID role, Set<UID> alreadyCollectedRoles) {
		Set<UID> roles = new HashSet<UID>();
		Collection<MasterDataVO<Object>> subordinateRoles = getMasterData(E.ROLE.getUID(), 
				SearchConditionUtils.newUidComparison(E.ROLE.parentrole, ComparisonOperator.EQUAL, role), false);
		for (MasterDataVO<?> voRole : subordinateRoles) {
			if (!alreadyCollectedRoles.contains(voRole.getPrimaryKey())) {
				roles.add((UID) voRole.getPrimaryKey());
				addSubordinateRoles((UID) voRole.getPrimaryKey(), roles);
			}
		}
		alreadyCollectedRoles.addAll(roles);
	}

	private List<MasterDataVO<UID>> getUsersForRoles(List<UID> roles) {
		final CollectableEntityField entityFieldRole = new CollectableMasterDataEntity(
				E.ROLEUSER).getEntityField(E.ROLEUSER.role.getUID());
		final ReferencingCollectableSearchCondition refCond = new ReferencingCollectableSearchCondition(
				entityFieldRole, new CollectableIdListCondition(roles));
		final Set<MasterDataVO<UID>> users = CollectionUtils.unsaveConvertToSet(
				getMasterData(E.ROLEUSER.getUID(), refCond, false));
		final JdbcEntityObjectProcessor<UID> userProcessor = NucletDalProvider.getInstance().getEntityObjectProcessor(E.USER);
		
		return CollectionUtils.transform(users, new Transformer<MasterDataVO<UID>, MasterDataVO<UID>>() {
			@Override
			public MasterDataVO transform(MasterDataVO roleuser) {
				return DalSupportForMD.wrapEntityObjectVO(userProcessor.getByPrimaryKey(roleuser.getFieldUid(E.ROLEUSER.user)));
			}
		});
	}
	
}

