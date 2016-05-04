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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.nuclos.common.E;
import org.nuclos.common.EntityMeta;
import org.nuclos.common.EntityTreeViewVO;
import org.nuclos.common.FieldMeta;
import org.nuclos.common.FieldMetaVO;
import org.nuclos.common.IMetaProvider;
import org.nuclos.common.LafParameterMap;
import org.nuclos.common.NucletEntityMeta;
import org.nuclos.common.NucletFieldMeta;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.PivotInfo;
import org.nuclos.common.SF;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.SimpleDbField;
import org.nuclos.common.StaticMetaDataProvider;
import org.nuclos.common.TranslationVO;
import org.nuclos.common.UID;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.collectable.searchcondition.CompositeCollectableSearchCondition;
import org.nuclos.common.dal.DalCallResult;
import org.nuclos.common.dal.vo.Delete;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.IDependentDataMap;
import org.nuclos.common.format.FormattingTransformer;
import org.nuclos.common.transport.vo.EntityMetaTransport;
import org.nuclos.common.transport.vo.FieldMetaTransport;
import org.nuclos.common.valueobject.EntityRelationshipModelVO;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.LocaleUtils;
import org.nuclos.server.common.MetaProvider;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.common.valueobject.DocumentFileBase;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.ProcessorFactorySingleton;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.database.SpringDataBaseHelper;
import org.nuclos.server.dblayer.DbAccess;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.DbUtils;
import org.nuclos.server.dblayer.IBatch;
import org.nuclos.server.dblayer.MetaDbHelper;
import org.nuclos.server.dblayer.expression.DbNull;
import org.nuclos.server.dblayer.impl.SchemaUtils;
import org.nuclos.server.dblayer.query.DbColumnExpression;
import org.nuclos.server.dblayer.query.DbCondition;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.query.DbSelection;
import org.nuclos.server.dblayer.statements.DbDeleteStatement;
import org.nuclos.server.dblayer.statements.DbInsertStatement;
import org.nuclos.server.dblayer.statements.DbMap;
import org.nuclos.server.dblayer.statements.DbStructureChange;
import org.nuclos.server.dblayer.structure.DbColumn;
import org.nuclos.server.dblayer.structure.DbColumnType;
import org.nuclos.server.dblayer.structure.DbTable;
import org.nuclos.server.dblayer.structure.DbTableType;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeLocal;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
* Facade bean for all meta data management functions (server side).
* <p>
* Uses the MetaProvider as implementation.
* </p>
* <br>
* <br>Created by Novabit Informationssysteme GmbH
* <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*/
@Transactional(noRollbackFor= {Exception.class})
public class MetaDataFacadeBean extends NuclosFacadeBean implements MetaDataFacadeRemote {

	private static final Logger LOG = Logger.getLogger(MetaDataFacadeBean.class);

	private MasterDataFacadeHelper helper;

	private ProcessorFactorySingleton processorFactory;
	
	private SpringDataBaseHelper dataBaseHelper;
	
	private DataSource dataSource;
	
	private GenericObjectFacadeLocal genericObjectFacade;
	
	private MasterDataFacadeLocal masterDataFacade;
	
	private LocaleFacadeLocal localeFacade;
	
	public MetaDataFacadeBean() {
	}

	@Autowired
	void setMasterDataFacadeHelper(MasterDataFacadeHelper masterDataFacadeHelper) {
		this.helper = masterDataFacadeHelper;
	}
	
	@Autowired
	void setDataBaseHelper(SpringDataBaseHelper dataBaseHelper) {
		this.dataBaseHelper = dataBaseHelper;
	}
	
	@Autowired
	@Qualifier("nuclos")
	void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public ProcessorFactorySingleton getProcessorFactory() {
		return processorFactory;
	}

	@Autowired
	void setProcessorFactory(ProcessorFactorySingleton processorFactory) {
		this.processorFactory = processorFactory;
	}

	@Autowired
	final void setGenericObjectFacade(GenericObjectFacadeLocal genericObjectFacade) {
		this.genericObjectFacade = genericObjectFacade;
	}
	
	private final GenericObjectFacadeLocal getGenericObjectFacade() {
		return genericObjectFacade;
	}

	@Autowired
	final void setMasterDataFacade(MasterDataFacadeLocal masterDataFacade) {
		this.masterDataFacade = masterDataFacade;
	}
	
	private final LocaleFacadeLocal getLocaleFacade() {
		return localeFacade;
	}

	@Autowired
	final void setLocaleFacade(LocaleFacadeLocal localeFacade) {
		this.localeFacade = localeFacade;
	}
	
	@Override
    @RolesAllowed("Login")
	public Collection<EntityMeta<?>> getAllEntities() {
		return MetaProvider.getInstance().getAllEntities();
	}

	@Override
    @RolesAllowed("Login")
	public Map<UID, FieldMeta<?>> getAllEntityFieldsByEntity(UID entity) {
		return MetaProvider.getInstance().getAllEntityFieldsByEntity(entity);
	}

	@Override
	public Map<UID, FieldMeta<?>> getAllPivotEntityFields(PivotInfo info) {
		return MetaProvider.getInstance().getAllPivotEntityFields(info);
	}

	@Override
    @RolesAllowed("Login")
	public 	Map<UID, Map<UID, FieldMeta<?>>> getAllEntityFieldsByEntitiesGz(List<UID> entities) {
		return MetaProvider.getInstance().getAllEntityFieldsByEntitiesGz(entities);
	}

	@Override
    @RolesAllowed("Login")
	public Collection<EntityMeta<?>> getNucletEntities() {
		return MetaProvider.getInstance().getAllEntities();
	}

	@Override
    public Object modifyEntityMetaData(EntityMeta<?> metaVO, List<FieldMetaTransport> toFields) {

		metaVO = MetaProvider.getInstance().getEntity(metaVO.getUID());

		List<FieldMeta<?>> lstFields = new ArrayList<FieldMeta<?>>();

		for(FieldMetaTransport to : toFields) {
			if(!to.getEntityFieldMeta().isFlagRemoved()) {
				lstFields.add(to.getEntityFieldMeta());
			}
		}
		
		EntityMeta<?> voIst = MetaProvider.getInstance().getEntity(metaVO.getUID());

		MetaDbHelper dbHelperIst = new MetaDbHelper(dataBaseHelper.getDbAccess(), MetaProvider.getInstance());
		DbTable tableIst = dbHelperIst.getDbTable(metaVO);

		StaticMetaDataProvider staticMetaData = new StaticMetaDataProvider(E.getThis());
		EntityMeta<?> originMeta = MetaProvider.getInstance().getEntity(metaVO.getUID());
		staticMetaData.addClone(originMeta, false);

		Collection<FieldMeta<?>> colFields = originMeta.getFields();
		if (colFields != null) {
			for(FieldMeta<?> vo : colFields) {
				boolean addField = true;
				for(FieldMetaTransport to : toFields) {
					if(to.getEntityFieldMeta().getFieldName().equals(vo.getFieldName())) {
						if(to.getEntityFieldMeta().isFlagRemoved()){
							addField = false;
						}
					}
					if(to.getEntityFieldMeta().getPrimaryKey() != null) {
						if(to.getEntityFieldMeta().getPrimaryKey().equals(vo.getUID())) {
							addField = false;
						}
					}
	
				}
				if(addField) {
					FieldMetaVO<?> clone = staticMetaData.addClone(vo);
					if(vo.getForeignEntity() != null) {
						clone.setReadonly(false);
						staticMetaData.addClone(MetaProvider.getInstance().getEntity(vo.getForeignEntity()), true);
					}
				}
			}
		}
		for(FieldMetaTransport to : toFields) {
			NucletFieldMeta<?> vo = to.getEntityFieldMeta();
			if(vo.getForeignEntity() != null) {
				if(!vo.isFlagRemoved()) {
					vo.setReadonly(false);
					staticMetaData.addClone(MetaProvider.getInstance().getEntity(vo.getForeignEntity()), true);
				}
			}
			if(!vo.isFlagRemoved()) {
				staticMetaData.addClone(vo);
			}
		}
		MetaDbHelper dbHelperSoll = new MetaDbHelper(dataBaseHelper.getDbAccess(), staticMetaData);
		DbTable tableSoll = dbHelperSoll.getDbTable(metaVO);

		List<DbStructureChange> lstStructureChanges = null;

		if(voIst.getUID() != null) {
			lstStructureChanges = SchemaUtils.modify(tableIst, tableSoll);
		}
		else {
			lstStructureChanges = SchemaUtils.create(tableSoll);
		}


		for(DbStructureChange ds : lstStructureChanges) {
			dataBaseHelper.getDbAccess().execute(ds);
		}

		for(FieldMetaTransport metaFieldTO : toFields) {
			NucletFieldMeta<?> metaFieldVO = metaFieldTO.getEntityFieldMeta();
			if(metaFieldVO.getUID() == null) {
				metaFieldVO.setPrimaryKey(new UID());
				metaFieldVO.setEntity(voIst.getUID());
				metaFieldVO.flagNew();
			}
			else {
				if(!metaFieldVO.isFlagRemoved()) {
					metaFieldVO.flagUpdate();
					metaFieldVO.setEntity(voIst.getUID());
				}
			}
			if(metaFieldVO.isFlagRemoved()) {
				NucletDalProvider.getInstance().getEntityFieldMetaDataProcessor().delete(new Delete(metaFieldVO.getPrimaryKey()));
			}
			else {
				DalUtils.updateVersionInformation(metaFieldVO, getCurrentUserName());
				NucletDalProvider.getInstance().getEntityFieldMetaDataProcessor().insertOrUpdate(metaFieldVO);
				createResourceIdForEntityField(metaFieldTO, metaFieldTO.getEntityFieldMeta().getPrimaryKey());
			}
		}
		MetaProvider.getInstance().revalidate(true, false);
		return null;
	}

	private void createResourceIdForEntity(EntityMetaTransport mdvo, UID uid) {

		Map<String, FieldMeta<String>> mp = new HashMap<String, FieldMeta<String>>();

		mp.put(TranslationVO.labelsEntity[0], E.ENTITY.localeresourcel);
		mp.put(TranslationVO.labelsEntity[1], E.ENTITY.localeresourcem);
		mp.put(TranslationVO.labelsEntity[2], E.ENTITY.localeresourcetw);
		mp.put(TranslationVO.labelsEntity[3], E.ENTITY.localeresourcett);

		for(String key : mp.keySet()) {
			String sResId = null;
			Collection<LocaleInfo> colLocaleInfo = getLocaleFacade().getAllLocales(false);
			for(LocaleInfo li : colLocaleInfo) {
				for(TranslationVO vo : mdvo.getTranslation()) {
					if(vo.getLanguage().equals(li.language)) {
						if(vo.getLabels().get(key) != null && vo.getLabels().get(key).length() > 0) {
							if(sResId == null)
								sResId = getResourceIdFromMetaDataVO(mdvo.getEntityMetaVO(), mp.get(key));
							sResId = getLocaleFacade().setResourceForLocale(sResId, li, vo.getLabels().get(key));
							LocaleUtils.setResourceIdForField(E.ENTITY, uid, mp.get(key), sResId);
							break;
						}
					}
				}
			}
		}
		getLocaleFacade().flushInternalCaches();

	}

	private void createResourceIdForEntityField(FieldMetaTransport mdvo, UID uid) {

		Map<String, FieldMeta<String>> mp = new HashMap<String, FieldMeta<String>>();

		mp.put(TranslationVO.labelsField[0], E.ENTITYFIELD.localeresourcel);
		mp.put(TranslationVO.labelsField[1], E.ENTITYFIELD.localeresourced);

		for(String key : mp.keySet()) {
			String sResId = null;
			Collection<LocaleInfo> colLocaleInfo = getLocaleFacade().getAllLocales(false);
			for(LocaleInfo li : colLocaleInfo) {
				if(mdvo.getTranslation() == null)
					continue;
				for(TranslationVO vo : mdvo.getTranslation()) {
					if(vo.getLanguage().equals(li.language)) {
						if(vo.getLabels().get(key) != null && vo.getLabels().get(key).length() > 0) {
							if(sResId == null)
								sResId = getResourceIdFromMetaDataVO(mdvo.getEntityFieldMeta(), mp.get(key));
							sResId = getLocaleFacade().setResourceForLocale(sResId, li, vo.getLabels().get(key));
							LocaleUtils.setResourceIdForField(E.ENTITYFIELD, uid, mp.get(key), sResId);
							break;
						}
					}
				}
			}
		}
		getLocaleFacade().flushInternalCaches();

	}


	private static String getResourceIdFromMetaDataVO(FieldMeta<?> metavo, FieldMeta<String> resField) {
		if (resField.equals(E.ENTITYFIELD.localeresourcel)) {
			return metavo.getLocaleResourceIdForLabel();
		} else if (resField.equals(E.ENTITYFIELD.localeresourced)) {
			return metavo.getLocaleResourceIdForDescription();
		}
		return null;
	}

	private static String getResourceIdFromMetaDataVO(EntityMeta<?> metavo, FieldMeta<String> resField) {
		if (resField.equals(E.ENTITY.localeresourcel)) {
			return metavo.getLocaleResourceIdForLabel();
		} else if (resField.equals(E.ENTITY.localeresourcem)) {
			return metavo.getLocaleResourceIdForMenuPath();
		} else if (resField.equals(E.ENTITY.localeresourced)) {
			return metavo.getLocaleResourceIdForDescription();
		} else if (resField.equals(E.ENTITY.localeresourcetw)) {
			return metavo.getLocaleResourceIdForTreeView();
		} else if (resField.equals(E.ENTITY.localeresourcett)) {
			return metavo.getLocaleResourceIdForTreeViewDescription();
		}
		return null;
	}

	@RolesAllowed("Login")
	@Override
	public boolean hasEntityImportStructure(UID entityUID) throws CommonBusinessException {
		DbQuery<DbTuple> query = dataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
		DbFrom from = query.from(E.IMPORT);
		List<DbSelection<?>> columns = new ArrayList<DbSelection<?>>();

		columns.add(from.baseColumn(E.IMPORT.getPk()).alias("INTID"));
		columns.add(from.baseColumn(E.IMPORT.entity).alias("INTID_T_AD_MASTERDATA"));
		query.multiselect(columns);
		query.where(dataBaseHelper.getDbAccess().getQueryBuilder().equalValue(from.baseColumn(E.IMPORT.entity),	entityUID));

		List<DbTuple> count = dataBaseHelper.getDbAccess().executeQuery(query);

		return count.size() > 0;
	}

	@Override
	@RolesAllowed("Login")
	public boolean hasEntityWorkflow(UID entityUID) throws CommonBusinessException {
		DbQuery<DbTuple> query = dataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
		DbFrom from = query.from(E.GENERATION);
		List<DbSelection<?>> columns = new ArrayList<DbSelection<?>>();

		columns.add(from.baseColumn(E.GENERATION.getPk()).alias("INTID"));
		columns.add(from.baseColumn(E.GENERATION.targetModule).alias("INTID_T_MD_MODULE_TARGET"));
		columns.add(from.baseColumn(E.GENERATION.sourceModule).alias("INTID_T_MD_MODULE_SOURCE"));
		query.multiselect(columns);

		DbCondition cond1 = dataBaseHelper.getDbAccess().getQueryBuilder().equalValue(from.baseColumn(E.GENERATION.targetModule), entityUID);
		DbCondition cond2 = dataBaseHelper.getDbAccess().getQueryBuilder().equalValue(from.baseColumn(E.GENERATION.sourceModule), entityUID);

		query.where(dataBaseHelper.getDbAccess().getQueryBuilder().or(cond1, cond2));

		List<DbTuple> count = dataBaseHelper.getDbAccess().executeQuery(query);

		return count.size() > 0;
	}

	@Override
    public void removeEntity(EntityMeta<?> voEntity, boolean dropLayout) throws CommonBusinessException {
		final IMetaProvider mdProvider = MetaProvider.getInstance();
		if (voEntity.getVirtualEntity() == null) {
			if(hasEntityRows(voEntity)) {
				if(voEntity.isStateModel()) {
					GenericObjectFacadeLocal local = ServerServiceLocator.getInstance().getFacade(GenericObjectFacadeLocal.class);
					for(Long iId : local.getGenericObjectIds(voEntity.getUID(), new CollectableSearchExpression())) {
						Set<UID> setNames = new HashSet<UID>();
						try {
							GenericObjectWithDependantsVO vo = local.getWithDependants(iId, setNames, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
							local.remove(vo, true, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
						}
						catch(CommonBusinessException e) {
							throw new NuclosFatalException(e);
						}
					}

				}
				else {
					MasterDataFacadeLocal local = ServerServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);
					for(MasterDataVO<?> vo : local.getMasterData(voEntity.getUID(), null, true)) {
						try {
							local.remove(vo, false, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
						}
						catch(NuclosBusinessRuleException e) {
							throw new NuclosFatalException(e);
						}
						catch(CommonBusinessException e) {
							throw new NuclosFatalException(e);
						}
					}

				}
			}
		}

		final MetaDbHelper helper = new MetaDbHelper(mdProvider);
		final DbTable table = helper.getDbTable(voEntity);

		List<DbStructureChange> lstChanges = SchemaUtils.drop(table);
		for(DbStructureChange db : lstChanges) {
			dataBaseHelper.getDbAccess().execute(db);
		}

		// delete workflow subentity
		CollectableComparison compWorkflowSubEntity1 = SearchConditionUtils.newUidComparison(E.GENERATIONSUBENTITY.entitySource, ComparisonOperator.EQUAL, voEntity.getUID());
		CollectableComparison compWorkflowSubEntity2 = SearchConditionUtils.newUidComparison(E.GENERATIONSUBENTITY.entityTarget, ComparisonOperator.EQUAL, voEntity.getUID());
		CompositeCollectableSearchCondition searchWorkflowSubEntity = SearchConditionUtils.or(compWorkflowSubEntity1, compWorkflowSubEntity2);
		Collection<MasterDataVO<UID>> colWorkflowSubEntity = masterDataFacade.getMasterData(E.GENERATIONSUBENTITY, searchWorkflowSubEntity, true);
		for(MasterDataVO<?> voWorkflowSubEntity : colWorkflowSubEntity) {
			masterDataFacade.remove(voWorkflowSubEntity, true, null);
		}

		// delete workflow
		CollectableComparison compWorkflow1 = SearchConditionUtils.newUidComparison(E.GENERATION.sourceModule, ComparisonOperator.EQUAL, voEntity.getUID());
		CollectableComparison compWorkflow2 = SearchConditionUtils.newUidComparison(E.GENERATION.targetModule, ComparisonOperator.EQUAL, voEntity.getUID());
		CompositeCollectableSearchCondition searchWorkflow = SearchConditionUtils.or(compWorkflow1, compWorkflow2);
		Collection<MasterDataVO<UID>> colWorkflow = masterDataFacade.getMasterData(E.GENERATION, searchWorkflow, true);
		for(MasterDataVO<UID> voWorkflow : colWorkflow) {
			CollectableComparison compWorkflowRule = SearchConditionUtils.newUidComparison(E.RULEGENERATION.generation, ComparisonOperator.EQUAL, voWorkflow.getPrimaryKey());
			for(MasterDataVO<?> voWorkflowRule : masterDataFacade.getMasterData(E.RULEGENERATION, compWorkflowRule, true)) {
				masterDataFacade.remove(voWorkflowRule, true, null);
			}
			masterDataFacade.remove(voWorkflow, true, null);
		}

		// delete import structure
		CollectableComparison comp = SearchConditionUtils.newUidComparison(E.IMPORT.entity, ComparisonOperator.EQUAL, voEntity.getUID());
		Collection<MasterDataVO<UID>> colImportStructure = masterDataFacade.getMasterData(E.IMPORT, comp, true);
		for(MasterDataVO<?> voImportStructure : colImportStructure) {
			masterDataFacade.remove(voImportStructure, true, null);
		}

		// delete statemodel
		DbMap mpDelStatemodel = new DbMap();
		mpDelStatemodel.put(E.STATEMODELUSAGE.nuclos_module, voEntity.getUID());
		DbDeleteStatement<UID> delStatemodel = new DbDeleteStatement<UID>(E.STATEMODELUSAGE, mpDelStatemodel);
		dataBaseHelper.getDbAccess().execute(delStatemodel);

		// delete userrights
		DbMap mpDelRoleMasterdata = new DbMap();
		mpDelRoleMasterdata.put(E.ROLEMASTERDATA.entity, voEntity.getUID());
		DbDeleteStatement<UID> delMasterdata = new DbDeleteStatement<UID>(E.ROLEMASTERDATA, mpDelRoleMasterdata);
		dataBaseHelper.getDbAccess().execute(delMasterdata);

		DbMap mpDelRoleModule = new DbMap();
		mpDelRoleModule.put(E.ROLEMODULE.module, voEntity.getUID());
		DbDeleteStatement<UID> delModule = new DbDeleteStatement<UID>(E.ROLEMODULE, mpDelRoleModule);
		dataBaseHelper.getDbAccess().execute(delModule);

		// delete entity subnodes (NUCLOSINT-1127)
		final EntityMeta<?> subnodesVO = E.ENTITYSUBNODES;
		final String subnodesTable = MetaDbHelper.getTableName(subnodesVO);
		final DbMap snWhere = new DbMap();
		// delete subnodes from entities which are deleted
		snWhere.put(E.ENTITYSUBNODES.originentityid, voEntity.getUID());
		final DbDeleteStatement<UID> snDel1 = new DbDeleteStatement<UID>(subnodesTable, snWhere);
		dataBaseHelper.getDbAccess().execute(snDel1);
		// delete subnodes representation of entity embedded in other entities (as subform)
		snWhere.clear();
		snWhere.put(E.ENTITYSUBNODES.entity, voEntity.getUID());
		final DbDeleteStatement<?> snDel2 = new DbDeleteStatement(subnodesTable, snWhere);
		dataBaseHelper.getDbAccess().execute(snDel2);

		// delete layouts
		DbQuery<DbTuple> query = dataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
		DbFrom<UID> from = query.from(E.LAYOUTUSAGE);
		List<DbSelection<?>> columns = new ArrayList<DbSelection<?>>();

		columns.add(from.baseColumn(E.LAYOUTUSAGE.entity).alias("STRENTITY"));
		columns.add(from.baseColumn(E.LAYOUTUSAGE.getPk()).alias("INTID"));
		columns.add(from.baseColumn(E.LAYOUTUSAGE.layout).alias("INTID_T_MD_LAYOUT"));
		query.multiselect(columns);
		query.where(dataBaseHelper.getDbAccess().getQueryBuilder().equalValue(from.baseColumn(E.LAYOUTUSAGE.entity),	voEntity.getUID()));

		List<UID> lstDeleteIds = new ArrayList<UID>();
		List<DbTuple> usages = dataBaseHelper.getDbAccess().executeQuery(query);

		for(DbTuple tuple : usages) {
		   UID idLayout = tuple.get("INTID_T_MD_LAYOUT", UID.class);
		   UID id = tuple.get("INTID", UID.class);

		   lstDeleteIds.add(idLayout);
		   DbMap mpDelLayout = new DbMap();
			mpDelLayout.put(E.LAYOUTUSAGE.getPk(), id);
			DbDeleteStatement<UID> delLayout = new DbDeleteStatement<UID>(E.LAYOUTUSAGE, mpDelLayout);
			dataBaseHelper.getDbAccess().execute(delLayout);
		}

		if(dropLayout) {
			for(UID idLayout : lstDeleteIds) {
				DbMap mpDelLayout = new DbMap();
				mpDelLayout.put(E.LAYOUT.getPk(), idLayout);
				DbDeleteStatement<UID> delLayout = new DbDeleteStatement<UID>(E.LAYOUT, mpDelLayout);
				dataBaseHelper.getDbAccess().execute(delLayout);
			}
		}

		// delete fields
		for(FieldMeta<?> voField : MetaProvider.getInstance().getAllEntityFieldsByEntity(voEntity.getUID()).values()) {
			NucletDalProvider.getInstance().getEntityFieldMetaDataProcessor().delete(new Delete(voField.getUID()));
	    }
		// delete entity
		NucletDalProvider.getInstance().getEntityMetaDataProcessor().delete(new Delete(voEntity.getUID()));

		MetaProvider.getInstance().revalidate(true, true);
	}

	@Override
    @RolesAllowed("Login")
	public boolean hasEntityRows(EntityMeta<?> voEntity) {

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Long> query = builder.createQuery(Long.class);
		DbFrom t = query.from(voEntity);
		query.select(builder.count(t.baseColumn(voEntity.isUidEntity() ? SF.PK_UID : SF.PK_ID)));

		return dataBaseHelper.getDbAccess().executeQuerySingleResult(query) > 0L;
	}

	@Override
    public String createOrModifyEntity(EntityMeta<?> oldMDEntity, EntityMetaTransport updatedTOEntity,
    		MasterDataVO<?> voEntity, List<FieldMetaTransport> toFields, boolean blnExecute,
    		String user, String password) throws NuclosBusinessException {
		String resultMessage = null;
		NucletEntityMeta updatedMDEntity = updatedTOEntity.getEntityMetaVO();

		String sOldPath = "";
		boolean bHasStateModelChanged = false;
		
		boolean isNew = false;
		try {
			MetaProvider.getInstance().getEntity(updatedMDEntity.getUID());
			
			sOldPath = MetaProvider.getInstance().getEntity(updatedMDEntity.getUID()).getDocumentPath();
			sOldPath = StringUtils.emptyIfNull(sOldPath);
			
			bHasStateModelChanged = 
					!LangUtils.equals(updatedMDEntity.isStateModel(),
							MetaProvider.getInstance().getEntity(updatedMDEntity.getUID()).isStateModel());
		} catch (Exception ex) {
			// entity is new
			isNew = true;
		}

		final StaticMetaDataProvider staticMetaData = new StaticMetaDataProvider(E.getThis());
		staticMetaData.addClone(updatedMDEntity, false);
		
		final List<NucletFieldMeta<?>> lstFields = new ArrayList<NucletFieldMeta<?>>();

		final Collection<FieldMeta<?>> lstSystemFields = new ArrayList<FieldMeta<?>>();
		DalUtils.addStaticFields(lstSystemFields, updatedMDEntity, true);

		for(FieldMetaTransport to : toFields) {
			if(!to.getEntityFieldMeta().isFlagRemoved()) {
				if (to.getEntityFieldMeta().isFlagNew())
					to.getEntityFieldMeta().setUID(new UID());
				
				lstFields.add(to.getEntityFieldMeta());
				staticMetaData.addClone(to.getEntityFieldMeta());
				if(updatedMDEntity.getUID().equals(to.getEntityFieldMeta().getForeignEntity())) {
					continue;
					// NUCLOSINT-697
				}
				if(to.getEntityFieldMeta().getForeignEntity() != null) {
					staticMetaData.addClone(MetaProvider.getInstance().getEntity(to.getEntityFieldMeta().getForeignEntity()), true);
				}
				if(to.getEntityFieldMeta().getLookupEntity() != null) {
					staticMetaData.addClone(MetaProvider.getInstance().getEntity(to.getEntityFieldMeta().getLookupEntity()), true);
				}
			}
		}
		for(FieldMeta<?> voSystemField : lstSystemFields) {
			staticMetaData.addClone(voSystemField);
			if(voSystemField.getForeignEntity() != null) {
				staticMetaData.addClone(MetaProvider.getInstance().getEntity(voSystemField.getForeignEntity()), true);
			}
		}
		
		final MetaDbHelper dbHelperIst = new MetaDbHelper(dataBaseHelper.getDbAccess(), MetaProvider.getInstance());
		final DbTable tableIst = dbHelperIst.getDbTable(updatedMDEntity);

		final MetaDbHelper dbHelperSoll = new MetaDbHelper(dataBaseHelper.getDbAccess(), staticMetaData);
		final DbTable tableSoll = dbHelperSoll.getDbTable(updatedMDEntity);

		List<DbStructureChange> lstStructureChanges = null;

		if(!isNew) {
			lstStructureChanges = SchemaUtils.modify(tableIst, tableSoll);
		}
		else {
			lstStructureChanges = SchemaUtils.create(tableSoll);
		}

		final List<DbStructureChange> lstDbChangesOkay = new ArrayList<DbStructureChange>();
		final List<DbStructureChange> lstDbChangesNotOkay = new ArrayList<DbStructureChange>();
		
		boolean dbchangeOkay = true;
		for(DbStructureChange ds : lstStructureChanges) {
			try {
				dataBaseHelper.getDbAccess().execute(ds);
				lstDbChangesOkay.add(ds);
			}
			catch(DbException e) {
				LOG.info("createOrModifyEntity failed: " + e);
				dbchangeOkay = false;
				lstDbChangesNotOkay.add(ds);
			}
		}

		// Error handling
		if(!dbchangeOkay) {
			final DbAccess dbAccess = dataBaseHelper.getDbAccess();
			if(updatedMDEntity.getUID() != null) {
				rollBackDBChanges(updatedTOEntity, toFields);
				
				final StringBuffer sb = new StringBuffer();
				
				List<String> lstStrings = Collections.emptyList();
				try {
					final IBatch batch = dbAccess.getBatchFor(lstDbChangesNotOkay.get(0));
					lstStrings = dbAccess.getStatementsForLogging(batch);
				}
				catch (SQLException e) {
					sb.append("Failed on getPreparedSqlFor(" + lstDbChangesNotOkay.get(0) + "): " + e);
				}
				sb.append("Entit\u00e4t " + updatedMDEntity.getEntityName() + " konnte nicht ver\u00e4ndert werden.\n");
				sb.append("Grund:\n");
				sb.append(lstStrings.get(0));

				resultMessage = sb.toString();
			}
			else {
				final MetaDbHelper helper = new MetaDbHelper(MetaProvider.getInstance());
				final DbTable table = helper.getDbTable(updatedMDEntity);

				final List<DbStructureChange> lstChanges = SchemaUtils.drop(table);
				for(DbStructureChange db : lstChanges) {
					try {
						dataBaseHelper.getDbAccess().execute(db);
					}
					catch(DbException e)  {
						// ignore
						LOG.info("createOrModifyEntity: " + e);
					}
				}
				
				final StringBuffer sb = new StringBuffer();
				
				List<String> lstStrings = Collections.emptyList();
				try {
					final IBatch batch = dbAccess.getBatchFor(lstDbChangesNotOkay.get(0));
					lstStrings = dbAccess.getStatementsForLogging(batch);
				}
				catch (SQLException e) {
					sb.append("Failed on getPreparedSqlFor(" + lstDbChangesNotOkay.get(0) + "): " + e);
				}
				sb.append("Entit\u00e4t " + updatedMDEntity.getEntityName() + " konnte nicht angelegt werden.\n");
				sb.append("Grund:\n");
				sb.append(lstStrings.get(0));
				resultMessage = sb.toString();
			}

			return resultMessage;
		}

		try {
			final String user2 = getCurrentUserName();
			DalUtils.updateVersionInformation(updatedMDEntity, user2);

			if(!isNew) {
				updatedMDEntity.flagUpdate();

				insertOrUpdateEntityMetaData(updatedMDEntity);
				createResourceIdForEntity(updatedTOEntity, updatedMDEntity.getUID());
				setSystemValuesAndParent(lstFields, updatedMDEntity);
				insertOrUpdateEntityFieldMetaData(toFields);
				for(FieldMetaTransport toField : toFields) {
					if(!toField.getEntityFieldMeta().isFlagRemoved())
						createResourceIdForEntityField(toField, toField.getEntityFieldMeta().getPrimaryKey());
				}
				MetaProvider.getInstance().revalidate(true, false);
				updatedMDEntity = new NucletEntityMeta(MetaProvider.getInstance().<Long>getEntity(updatedMDEntity.getUID()), false);
			}
			else {
				updatedMDEntity.flagNew();
				insertOrUpdateEntityMetaData(updatedMDEntity);
				createResourceIdForEntity(updatedTOEntity, updatedMDEntity.getUID());
				setSystemValuesAndParent(lstFields, updatedMDEntity);
				insertOrUpdateEntityFieldMetaData(toFields);
				for(FieldMetaTransport toField : toFields) {
					createResourceIdForEntityField(toField, toField.getEntityFieldMeta().getPrimaryKey());
				}
				MetaProvider.getInstance().revalidate(true, false);
			}

			if (updatedTOEntity.getProcesses() != null) {
				final JdbcEntityObjectProcessor<UID> processor = NucletDalProvider.getInstance().getEntityObjectProcessor(E.PROCESS);
				for (EntityObjectVO<UID> process : updatedTOEntity.getProcesses()) {
					if (process.isFlagNew() || process.isFlagUpdated()) {
						if (process.getPrimaryKey() == null || process.isFlagNew()) {
							process.flagNew();
							process.setPrimaryKey(new UID());
							DalUtils.updateVersionInformation(process, user2);
						}
						process.setFieldUid(E.PROCESS.module, updatedMDEntity.getUID());
						processor.insertOrUpdate(process);
					}
					else if (process.getPrimaryKey() != null && process.isFlagRemoved()) {
						processor.delete(new Delete<UID>(process.getPrimaryKey()));
					}
				}
			}

			if (updatedTOEntity.getMenus() != null) {
				final JdbcEntityObjectProcessor<UID> menuProcessor = NucletDalProvider.getInstance().getEntityObjectProcessor(E.ENTITYMENU);
				for (EntityObjectVO<UID> menu : updatedTOEntity.getMenus().keySet()) {
					if (menu.isFlagNew() || menu.isFlagUpdated()) {
						if (menu.getPrimaryKey() == null || menu.isFlagNew()) {
							menu.flagNew();
							menu.setPrimaryKey(new UID());
							DalUtils.updateVersionInformation(menu, user2);
						}
						menu.setFieldUid(E.ENTITYMENU.entity, updatedMDEntity.getUID());
						
						
						final Map<String, String> locales = updatedTOEntity.getMenus().get(menu);
						
						String resourceId = menu.getFieldValue(E.ENTITYMENU.menupath);
						for (LocaleInfo li : getLocaleFacade().getAllLocales(false)) {
							resourceId = getLocaleFacade().setResourceForLocale(resourceId, li, locales.get(li.getTag()));
						}
						menu.setFieldValue(E.ENTITYMENU.menupath, resourceId);
						menuProcessor.insertOrUpdate(menu);

					}
					else if (menu.getPrimaryKey() != null && menu.isFlagRemoved()) {
						menuProcessor.delete(new Delete<UID>(menu.getPrimaryKey()));
						getLocaleFacade().deleteResource(menu.getFieldValue(E.ENTITYMENU.menupath));
					}
				}
			}

			for(EntityTreeViewVO voTreeView : updatedTOEntity.getTreeView()) {
				if(voTreeView.getField() == null){
					continue;
				}

				final DbMap conditionMap = new DbMap();
				conditionMap.put(E.ENTITYSUBNODES.originentityid, voTreeView.getOriginEntity());
				conditionMap.put(E.ENTITYSUBNODES.entity, voTreeView.getEntity());
				dataBaseHelper.getDbAccess().execute(new DbDeleteStatement<UID>(E.ENTITYSUBNODES.getDbTable(), conditionMap));

				final DbMap m = new DbMap();
				// EntityTreeViewVO specific fields
				m.put(E.ENTITYSUBNODES.field, voTreeView.getField());
				m.put(E.ENTITYSUBNODES.entity, voTreeView.getEntity());
				m.put(E.ENTITYSUBNODES.originentityid, voTreeView.getOriginEntity());
				// if(voTreeView.getFoldername() != null)
				m.put(E.ENTITYSUBNODES.foldername, voTreeView.getFoldername());
				m.put(E.ENTITYSUBNODES.active, voTreeView.isActive());
				m.put(E.ENTITYSUBNODES.sortOrder, voTreeView.getSortOrder());

				// Standard nuclos fields
				m.put(E.ENTITYSUBNODES.getPk(), new UID());
				m.put(SF.CREATEDAT, new InternalTimestamp(System.currentTimeMillis()));
				m.put(SF.CREATEDBY, user2);
				m.put(SF.CHANGEDAT, new InternalTimestamp(System.currentTimeMillis()));
				m.put(SF.CHANGEDBY, user2);
				m.put(SF.VERSION, 1);

				dataBaseHelper.getDbAccess().execute(new DbInsertStatement<UID>(E.ENTITYSUBNODES, DbNull.escapeNull(m)));
			}

			changeModuleDirectory(sOldPath, updatedMDEntity.getDocumentPath(), updatedMDEntity);
			
			if (bHasStateModelChanged)
				genericObjectFacade.updateGenericObjectEntries(updatedMDEntity.getUID());

		}
		catch (CommonFatalException e) {
			throw e;
		}
		catch (Exception e) {
			throw new CommonFatalException(e);
		}

		return resultMessage;
	}

	private void changeModuleDirectory(String sOldPath, String sNewPath, EntityMeta<?> mdvo) throws CommonPermissionException, CommonFinderException, CommonCreateException, CommonRemoveException, CommonStaleVersionException, CommonValidationException, NuclosBusinessException, IOException {
		if(!mdvo.isStateModel())
			return;

		sOldPath = StringUtils.emptyIfNull(sOldPath);
		sNewPath = StringUtils.emptyIfNull(sNewPath);

		if(org.apache.commons.lang.StringUtils.equals(sOldPath, sNewPath)) {
			return;
		}

		CollectableSearchExpression exp = new CollectableSearchExpression();

		for(Long iId : getGenericObjectFacade().getGenericObjectIds(mdvo.getUID(), exp)) {
			GenericObjectWithDependantsVO vo = getGenericObjectFacade().getWithDependants(iId, Collections.singleton(E.GENERALSEARCHDOCUMENT.getUID()), ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
			if(!vo.getDependents().hasData(E.GENERALSEARCHDOCUMENT))
				continue;
			boolean bModify = false;

			String oldPath = getPath(StringUtils.emptyIfNull(sOldPath), vo);
			String newPath = getPath(StringUtils.emptyIfNull(sNewPath), vo);

			IDependentDataMap mp = vo.getDependents();
			for(EntityObjectVO<Long> voDocument : mp.getData(E.GENERALSEARCHDOCUMENT)) {
				voDocument.setFieldValue(E.GENERALSEARCHDOCUMENT.path, newPath);
				voDocument.flagUpdate();
				bModify = true;
				String sBaseDir = NuclosSystemParameters.getString(NuclosSystemParameters.DOCUMENT_PATH);

				DocumentFileBase docFile = voDocument.getFieldValue(E.GENERALSEARCHDOCUMENT.file);
				String sFilename = voDocument.getPrimaryKey() + "." + docFile.getFilename();

				File file = new File(sBaseDir +"/" + oldPath + "/" + sFilename);
				File newDir = new File(sBaseDir +"/" + newPath);
				newDir.mkdirs();
				IOUtils.copyFile(file, new File(sBaseDir +"/" + newPath + "/" + sFilename));
			}
			if(bModify) {
				getGenericObjectFacade().modify(vo, false, ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
			}
			for(EntityObjectVO<Long> voDocument : mp.getData(E.GENERALSEARCHDOCUMENT)) {
				voDocument.setFieldValue(E.GENERALSEARCHDOCUMENT.path, newPath);
				bModify = true;
				String sBaseDir = NuclosSystemParameters.getString(NuclosSystemParameters.DOCUMENT_PATH);
				DocumentFileBase docFile = voDocument.getFieldValue(E.GENERALSEARCHDOCUMENT.file);
				String sFilename = voDocument.getPrimaryKey() + "." + docFile.getFilename();
				File file = new File(sBaseDir +"/" + oldPath + "/" + sFilename);
				file.delete();
			}

		}

	}

	private String getPath(String path, final GenericObjectWithDependantsVO oParent) {
		String rPath = new String(path);
		if (rPath.contains("${")){
			rPath = StringUtils.replaceParameters(rPath, new FormattingTransformer() {
				@Override
				protected Object getValue(UID field) {
					return oParent.getAttribute(field).getValue();
				}

				@Override
				protected UID getEntity() {
					return oParent.getModule();
				}
			});
		}
		return rPath;
	}


	private void rollBackDBChanges(EntityMetaTransport updatedTOEntity,
		List<FieldMetaTransport> toFields) {
		NucletEntityMeta updatedMDEntity = updatedTOEntity.getEntityMetaVO();

		MetaDbHelper dbHelperIst = new MetaDbHelper(dataBaseHelper.getDbAccess(), MetaProvider.getInstance());
		DbTable tableIst = dbHelperIst.getDbTable(updatedMDEntity);

		StaticMetaDataProvider staticMetaData = new StaticMetaDataProvider(E.getThis());
		staticMetaData.addClone(updatedMDEntity, false);
		List<FieldMeta<?>> lstFields = new ArrayList<FieldMeta<?>>();

//		List<FieldMeta> lst = new ArrayList<FieldMeta>();
//
//		for(FieldMeta field : lst) {
//			staticMetaData.addEntityField(field);
//			if(field.getForeignEntity() != null) {
//				staticMetaData.addEntity(new StaticEntityMeta(MetaProvider.getInstance().getEntity(field.getForeignEntity())));
//				for(FieldMeta voForeignField : MetaProvider.getInstance().getAllEntityFieldsByEntity(field.getForeignEntity()).values()) {
//					staticMetaData.addEntityField(voForeignField);
//				}
//			}
//		}
		Collection<FieldMeta<?>> lstSystemFields = new ArrayList<FieldMeta<?>>();
		DalUtils.addStaticFields(lstSystemFields, updatedMDEntity);
		for(FieldMetaTransport to : toFields) {
			lstFields.add(to.getEntityFieldMeta());
			staticMetaData.addClone(to.getEntityFieldMeta());
			if(to.getEntityFieldMeta().getForeignEntity() != null) {
				staticMetaData.addClone(MetaProvider.getInstance().getEntity(to.getEntityFieldMeta().getForeignEntity()), true);
			}
		}
		for(FieldMeta<?> voSystemField : lstSystemFields) {
			staticMetaData.addClone(voSystemField);
			if(voSystemField.getForeignEntity() != null) {
				staticMetaData.addClone(MetaProvider.getInstance().getEntity(voSystemField.getForeignEntity()), true);
			}
		}
		MetaDbHelper dbHelperSoll = new MetaDbHelper(dataBaseHelper.getDbAccess(), staticMetaData);
		DbTable tableSoll = dbHelperSoll.getDbTable(updatedMDEntity);

		List<DbStructureChange> lstStructureChanges = null;

		if(updatedMDEntity.getUID() != null) {
			lstStructureChanges = SchemaUtils.modify(tableSoll, tableIst);
		}
		else {
			lstStructureChanges = new ArrayList<DbStructureChange>();
		}


		for(DbStructureChange ds : lstStructureChanges) {
			try {
				dataBaseHelper.getDbAccess().execute(ds);
			}
			catch(DbException e) {
				// ignore
				LOG.info("rollBackDBChanges: " + e);
			}
		}
	}

	private void setSystemValuesAndParent(List<NucletFieldMeta<?>> lstFields, EntityMeta<?> voParent) {
		for(NucletFieldMeta<?> voField : lstFields) {
			voField.setEntity(voParent.getUID());
			if(voField.isFlagNew()) {
				DalUtils.updateVersionInformation(voField,getCurrentUserName());
				voField.setPrimaryKey(new UID());
			}
			else if(voField.isFlagUpdated()){
				DalUtils.updateVersionInformation(voField, getCurrentUserName());
			}
		}
	}


	private void insertOrUpdateEntityMetaData(NucletEntityMeta vo) {
		NucletDalProvider.getInstance().getEntityMetaDataProcessor().insertOrUpdate(vo);
	}

	private DalCallResult insertOrUpdateEntityFieldMetaData(List<FieldMetaTransport> lstFields) {

		// first remove fields
		for(FieldMetaTransport vo : lstFields) {
			NucletFieldMeta<?> v = vo.getEntityFieldMeta();
			DalUtils.updateVersionInformation(v, getCurrentUserName());
			if(v.isFlagRemoved() && v.getPrimaryKey() != null) {
				// NUCLOSINT-714: remove dependants generation attributes
				dataBaseHelper.getDbAccess().execute(DbStatementUtils.deleteFrom(E.GENERATIONATTRIBUTE,	E.GENERATIONATTRIBUTE.attributeSource, v.getPrimaryKey()));
				dataBaseHelper.getDbAccess().execute(DbStatementUtils.deleteFrom(E.GENERATIONATTRIBUTE, E.GENERATIONATTRIBUTE.attributeTarget, v.getPrimaryKey()));
				dataBaseHelper.getDbAccess().execute(DbStatementUtils.deleteFrom(E.GENERATIONSUBENTITYATTRIBUTE, E.GENERATIONSUBENTITYATTRIBUTE.subentityAttributeSource, v.getPrimaryKey()));
				dataBaseHelper.getDbAccess().execute(DbStatementUtils.deleteFrom(E.GENERATIONSUBENTITYATTRIBUTE, E.GENERATIONSUBENTITYATTRIBUTE.subentityAttributeTarget, v.getPrimaryKey()));
				dataBaseHelper.getDbAccess().execute(DbStatementUtils.deleteFrom(E.IMPORTATTRIBUTE, E.IMPORTATTRIBUTE.attribute, v.getPrimaryKey()));
				dataBaseHelper.getDbAccess().execute(DbStatementUtils.deleteFrom(E.IMPORTIDENTIFIER, E.IMPORTIDENTIFIER.attribute, v.getPrimaryKey()));
				NucletDalProvider.getInstance().getEntityFieldMetaDataProcessor().delete(new Delete<UID>(v.getPrimaryKey()));
			}
			else
				continue;
		}

		for(FieldMetaTransport vo : lstFields) {
			NucletFieldMeta<?> v = vo.getEntityFieldMeta();
			DalUtils.updateVersionInformation(v, getCurrentUserName());
			if(v.isFlagRemoved()) {
				continue;
			}
			else {
				NucletDalProvider.getInstance().getEntityFieldMetaDataProcessor().insertOrUpdate(v);
			}
		}

		return null;
	}

	@Override
    public List<String> getDBTables() {
		return new ArrayList<String>(dataBaseHelper.getDbAccess().getTableNames(DbTableType.TABLE));
	}

	/**
	 * @return Script (with results if selected)
	 */
	@Override
    @RolesAllowed("Login")
	public Map<String, MasterDataVO<?>> getColumnsFromTable(String sTable) {
		Map<String, MasterDataVO<?>> mp = new HashMap<String, MasterDataVO<?>>();
		DbTable table = dataBaseHelper.getDbAccess().getTableMetaData(sTable);
		for (DbColumn column : table.getTableColumns()) {
			MasterDataVO<UID> vo = new MasterDataVO<UID>(E.DATATYPE, false);
			DbColumnType type = column.getColumnType();
			String javatyp = "java.lang.String";
			String name = "Text";
			int scale = 0, precision = 0;
			boolean blnAddColumn = true;
			if (type.getGenericType() != null) {
				javatyp = type.getGenericType().getPreferredJavaType().getName();
				switch (type.getGenericType()) {
				case NUMERIC:
					name = "Kommazahl";
					scale = (type.getPrecision() != null ? type.getPrecision() : 0);
					precision = (type.getScale() != null ? type.getScale() : 0);
					// Nuclos maps integer to number(x,0), hence we map these back to Integer
					if (precision == 0) {
						name = "Ganzzahl";
						javatyp = "java.lang.Integer";
					}
					break;
				case BOOLEAN:
					name = "Boolean";
					break;
				case VARCHAR:
					name = "Text";
					scale = (type.getLength() != null ? type.getLength() : 0);
					break;
				case DATE:
				case DATETIME:
					name = "Datum";
					break;
				default:
					// Column type nuclos don't supported
					blnAddColumn = false;
					break;
				}
			}
			if(blnAddColumn) {
				vo.setFieldValue(E.DATATYPE.name, name);
				vo.setFieldValue(E.DATATYPE.javatyp, javatyp);
				vo.setFieldValue(E.DATATYPE.scale, scale);
				vo.setFieldValue(E.DATATYPE.precision, precision);

				mp.put(column.getColumnName(), vo);
			}
		}
		return mp;
	}

	/**
	 * @return Script (with results if selected)
	 */
	@Override
    @RolesAllowed("Login")
	public List<String> getTablesFromSchema(String url, String user, String password, String schema) {
		List<String> lstTables = new ArrayList<String>();
		Connection connect = null;
		try {
			connect = DriverManager.getConnection(url, user, password);
			DatabaseMetaData dbmeta = connect.getMetaData();
			ResultSet rsTables = dbmeta.getTables(null, schema.toUpperCase(), "%", new String [] {"TABLE"});
			while(rsTables.next()) {
				lstTables.add(rsTables.getString("TABLE_NAME"));
			}
			rsTables.close();
		}
		catch(SQLException e) {
			throw new CommonFatalException(e);
		}
		finally {
			if(connect != null)
				try {
					connect.close();
				}
				catch(SQLException e) {
					// do noting here
					LOG.info("getTablesFromSchema: " + e);
				}
		}

		return lstTables;
	}

	/**
	 * @return Script (with results if selected)
	 */
	@Override
    @RolesAllowed("Login")
	public List<MasterDataVO<UID>> transformTable(String url, String user, String password, String schema, String table) {

		List<MasterDataVO<UID>> lstFields = new ArrayList<MasterDataVO<UID>>();

		Connection connect = null;
		try {
			connect = DriverManager.getConnection(url, user, password);
			DatabaseMetaData dbmeta = connect.getMetaData();
			ResultSet rsCols = dbmeta.getColumns(null, schema.toUpperCase(), table, "%");
			while(rsCols.next()) {
				String colName = rsCols.getString("COLUMN_NAME");
				int colsize = rsCols.getInt("COLUMN_SIZE");
				int postsize = rsCols.getInt("DECIMAL_DIGITS");
				int columsType = rsCols.getInt("DATA_TYPE");
				String sJavaType = getBestJavaType(columsType);
				if(postsize > 0)
					sJavaType = "java.lang.Double";

				MasterDataVO<UID> mdFieldVO = new MasterDataVO<UID>(E.ENTITYFIELD, true);
				mdFieldVO.setFieldValue(E.ENTITYFIELD.datascale, colsize);
				mdFieldVO.setFieldValue(E.ENTITYFIELD.localeresourcel, org.apache.commons.lang.StringUtils.capitalize(colName.toLowerCase()));
				mdFieldVO.setFieldValue(E.ENTITYFIELD.nullable, Boolean.TRUE);
				mdFieldVO.setFieldValue(E.ENTITYFIELD.dataprecision, postsize);
				mdFieldVO.setFieldValue(E.ENTITYFIELD.dbfield, colName.toLowerCase());
				mdFieldVO.setFieldValue(E.ENTITYFIELD.localeresourced, org.apache.commons.lang.StringUtils.capitalize(colName.toLowerCase()));
				mdFieldVO.setFieldValue(E.ENTITYFIELD.field, colName.toLowerCase());
				mdFieldVO.setFieldValue(E.ENTITYFIELD.datatype, sJavaType);
				lstFields.add(mdFieldVO);
			}
			rsCols.close();
		}
		catch(Exception e) {
			LOG.info("transformTable: " + e, e);
		}
		finally {
			try {
				if (connect != null) {
					connect.close();
				}
			}
			catch(Exception e) {
				LOG.info("transformTable: " + e, e);
			}
		}
		return lstFields;
	}

	/**
	 * @return Script (with results if selected)
	 */
	@Override
    @RolesAllowed("Login")
	public EntityMeta<?> transferTable(String url, String user, String password, String schema, String table, UID entityUID) {

		EntityMeta<?> metaNew = null;

		Connection connect = null;
		try {
			List<MasterDataVO<UID>> tableFields = transformTable(url, user, password, schema, table);
			
			connect = DriverManager.getConnection(url, user, password);

			metaNew = MetaProvider.getInstance().getEntity(entityUID);

			String sqlSelect = "select * from " + schema + "." + table;
			Statement stmt = connect.createStatement();
			ResultSet rsSelect =  stmt.executeQuery(sqlSelect);
			while(rsSelect.next()) {
				List<Object> lstValues = new ArrayList<Object>();
				for(MasterDataVO<UID> field : tableFields) {
					lstValues.add(rsSelect.getObject(field.getFieldValue(E.ENTITYFIELD.dbfield)));
				}

				StringBuffer sb = new StringBuffer();
				sb.append("insert into " + metaNew.getDbTable());
				sb.append(" values(?");
				for(int i = 0; i < lstValues.size(); i++) {
					sb.append(",?");
				}
				sb.append(",?,?,?,?,?)");

				int col = 1;
				PreparedStatement pst = dataSource.getConnection().prepareStatement(sb.toString());
				pst.setLong(col++, dataBaseHelper.getNextIdAsLong(SpringDataBaseHelper.DEFAULT_SEQUENCE));
				for(Object object : lstValues) {
					pst.setObject(col++, object);
				}
				pst.setDate(col++, new java.sql.Date(System.currentTimeMillis()));
				pst.setString(col++, "Wizard");
				pst.setDate(col++, new java.sql.Date(System.currentTimeMillis()));
				pst.setString(col++, "Wizard");
				pst.setInt(col++, 1);

				pst.executeUpdate();
				pst.close();

			}
			rsSelect.close();
			stmt.close();

		}
		catch(SQLException e) {
			LOG.info("transferTable: " + e, e);
		}
		finally {
			if(connect != null)
				try {
					connect.close();
				}
				catch(SQLException e) {
					// do noting here
					LOG.info("transferTable: " + e);
				}
		}
		return metaNew;
	}

	private String getBestJavaType(int colType) {
		String sType = "java.lang.String";
		switch(colType) {
		case Types.VARCHAR:
			return sType;
		case Types.CHAR:
			return sType;
		case Types.NCHAR:
			return sType;
		case Types.NVARCHAR:
			return sType;
		case Types.LONGNVARCHAR:
			return sType;
		case Types.LONGVARCHAR:
			return sType;
		case Types.LONGVARBINARY:
			return sType;
		case Types.NUMERIC:
			return "java.lang.Integer";
		case Types.DECIMAL:
			return "java.lang.Double";
		case Types.BOOLEAN:
			return "java.lang.Integer";
		case Types.DATE:
			return "java.util.Date";
		case Types.TIME:
			return "java.util.Date";
		case Types.TIMESTAMP:
			return "java.util.Date";

		default:

			return sType;
		}
	}

	@Override
    @RolesAllowed("Login")
	public EntityRelationshipModelVO getEntityRelationshipModelVO(MasterDataVO<UID> vo) {
		return MasterDataWrapper.getEntityRelationshipModelVO(vo);
	}

	/**
	 * force to change internal entity name
	 */
	@Override
    @RolesAllowed("Login")
	public boolean isChangeDatabaseColumnToNotNullableAllowed(UID field) {
		FieldMeta<?> fieldMeta = MetaProvider.getInstance().getEntityField(field);
		EntityMeta<?> entityMeta = MetaProvider.getInstance().getEntity(fieldMeta.getEntity());

		try {
			// @TODO GOREF: maybe this should be delegated to the (JDBC)-EntityObjectProcessor ?
			DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<Long> query = builder.createQuery(Long.class);
			DbFrom t = query.from(entityMeta);
			DbColumnExpression<?> c = t.baseColumn(fieldMeta);
			query.select(builder.countRows());
			query.where(c.isNull());

			Long count = dataBaseHelper.getDbAccess().executeQuerySingleResult(query);
			return count == 0L;
		}
		catch(Exception e) {
			LOG.info("isChangeDatabaseColumnToNotNullableAllowed: " + e);
			return false;
		}
	}

	/**
	 * force to change internal entity name
	 */
	@Override
    @RolesAllowed("Login")
	public boolean isChangeDatabaseColumnToUniqueAllowed(UID field) {
		FieldMeta<?> fieldMeta = MetaProvider.getInstance().getEntityField(field);
		EntityMeta<?> entityMeta = MetaProvider.getInstance().getEntity(fieldMeta.getEntity());
		String sColumn = null;

		// We don't need a join to the referenced field here, instead we simply need th
		// (raw) field name in the database. (tp)
		if (fieldMeta.getForeignEntity() != null && fieldMeta.getForeignEntityField() != null) {
			sColumn = MetaDbHelper.getDbRefColumn(entityMeta, fieldMeta);
		}
		else {
			sColumn = fieldMeta.getDbColumn();
		}
		try {
			// @TODO GOREF: maybe this should be delegated to the (JDBC)-EntityObjectProcessor ?
			DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
			DbQuery<Long> query = builder.createQuery(Long.class);
			DbFrom t = query.from(entityMeta);
			DbColumnExpression<?> c = t.baseColumn(new SimpleDbField(sColumn, DbUtils.getDbType(fieldMeta.getDataType())));
			query.select(builder.countRows());
			query.groupBy(c);
			query.having(builder.greaterThan(builder.countRows(), builder.literal(1L)));
			query.maxResults(2);

			List<Long> result = dataBaseHelper.getDbAccess().executeQuery(query);
			return result.isEmpty();
		}
		catch(Exception e) {
			LOG.error("isChangeDatabaseColumnToUniqueAllowed: " + e, e);
			return false;
		}
	}

	@Override
	@RolesAllowed("Login")
	public Collection<MasterDataVO<UID>> hasEntityFieldInImportStructure(UID field) {
		FieldMeta<?> fieldMeta = MetaProvider.getInstance().getEntityField(field);

		CollectableComparison comp = SearchConditionUtils.newUidComparison(E.IMPORT.entity, ComparisonOperator.EQUAL, fieldMeta.getEntity());
		Collection<MasterDataVO<UID>> colImportStructure = masterDataFacade.getMasterData(E.IMPORT, comp, true);
		for(MasterDataVO<UID> voImportStructure : colImportStructure) {

			CollectableComparison compAttribute1 = SearchConditionUtils.newUidComparison(E.IMPORTATTRIBUTE.importfield, ComparisonOperator.EQUAL, voImportStructure.getPrimaryKey());
			CollectableComparison compAttribute2 = SearchConditionUtils.newUidComparison(E.IMPORTATTRIBUTE.attribute, ComparisonOperator.EQUAL, field);
			CompositeCollectableSearchCondition search = SearchConditionUtils.and(compAttribute1, compAttribute2);
			Collection<MasterDataVO<UID>> colImportAttribute = masterDataFacade.getMasterData(E.IMPORTATTRIBUTE, search, true);
			if(colImportAttribute.size() > 0)
				return colImportStructure;
		}

		return new ArrayList<MasterDataVO<UID>>();
	}

	@Override
	@RolesAllowed("Login")
	public boolean hasEntityLayout(UID entity) {
		DbQuery<DbTuple> query = dataBaseHelper.getDbAccess().getQueryBuilder().createTupleQuery();
		DbFrom from = query.from(E.LAYOUTUSAGE);
		List<DbSelection<?>> columns = new ArrayList<DbSelection<?>>();

		columns.add(from.baseColumn(E.LAYOUTUSAGE.getPk()).alias("INTID"));
		columns.add(from.baseColumn(E.LAYOUTUSAGE.entity).alias("STRENTITY"));
		query.multiselect(columns);
		query.where(dataBaseHelper.getDbAccess().getQueryBuilder().equalValue(from.baseColumn(E.LAYOUTUSAGE.entity), entity));

		List<DbTuple> count = dataBaseHelper.getDbAccess().executeQuery(query);

		return count.size() > 0;
	}


    @RolesAllowed("Login")
	public EntityMeta<?> getEntityMeta(UID entityUID) {
		return MetaProvider.getInstance().getEntity(entityUID);
	}

	@Override
	@RolesAllowed("Login")
    public void invalidateServerMetadata() {
	    MetaProvider.getInstance().revalidate(true, true);
    }

	@Override
	@RolesAllowed("Login")
	public List<String> getVirtualEntities() {
		List<String> result = new ArrayList<String>();
		result.addAll(dataBaseHelper.getDbAccess().getTableNames(DbTableType.TABLE));
		result.addAll(dataBaseHelper.getDbAccess().getTableNames(DbTableType.VIEW));
		for (EntityMeta<?> meta : getAllEntities()) {
			result.remove(meta.getDbTable());
			result.remove("T_" + meta.getDbTable().substring(2));
		}
		return result;
	}

	@Override
	@RolesAllowed("Login")
	public List<String> getPossibleIdFactories() {
		return MetaProvider.getInstance().getPossibleIdFactories();
	}

	@Override
	@RolesAllowed("Login")
	public List<FieldMeta<?>> getVirtualEntityFields(String virtualentity) {
		List<FieldMeta<?>> result = new ArrayList<FieldMeta<?>>();
		DbTable tableMetaData = dataBaseHelper.getDbAccess().getTableMetaData(virtualentity);

		for (DbColumn column : tableMetaData.getTableColumns()) {
			NucletFieldMeta<?> field = DalUtils.getFieldMeta(column);
			field.setDbColumn(column.getColumnName().toUpperCase());
			field.setFieldName(field.getFieldName().toLowerCase());
			field.setFallbackLabel(field.getFieldName());
			result.add(field);
		}
		return result;
	}

	@Override
	public void tryVirtualEntitySelect(EntityMeta<?> virtualentity) throws NuclosBusinessException {
		JdbcEntityObjectProcessor<?> processor = getProcessorFactory().newEntityObjectProcessor(virtualentity, new ArrayList<FieldMeta<?>>(), true);
		try {
			processor.getBySearchExpression(new CollectableSearchExpression(new CollectableIdCondition(new Long(0))));
		}
		catch (Exception ex) {
			error(ex);
			throw new NuclosBusinessException(StringUtils.getParameterizedExceptionMessage("MetaDataFacade.tryVirtualEntitySelect.error", ex.getMessage()));
		}
	}

	@Override
	public void tryRemoveProcess(EntityObjectVO<?> process) throws NuclosBusinessException {
		try {
			NucletDalProvider.getInstance().getEntityObjectProcessor(E.PROCESS).delete(
					new Delete(process.getId()));
		}
		catch (DbException e) {
			throw new NuclosBusinessException("tryRemoveProcess failed", e);
		}
		TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
	}

	@Override
	public List<EntityObjectVO<UID>> getEntityMenus() {
		return NucletDalProvider.getInstance().getEntityObjectProcessor(E.ENTITYMENU).getAll();
	}

	@Override
	public Collection<EntityMeta<?>> getSystemMetaData() {
		return E.getAllEntities();
	}

	@Override
	public Map<UID, LafParameterMap> getLafParameters() {
		return MetaProvider.getInstance().getAllLafParameters();
	}

	@Override
	public <PK> EntityMeta<PK> getEntity(UID entityUID) {
		return (EntityMeta<PK>) MetaProvider.getInstance().getEntities(entityUID);
	}

	@Override
	public FieldMeta<?> getEntityField(UID fieldUID) {
		return MetaProvider.getInstance().getEntityField(fieldUID);
	}

	@Override
	public EntityMeta<?> getByTablename(String sTableName) {
		return MetaProvider.getInstance().getByTablename(sTableName);
	}

	@Override
	public boolean isNuclosEntity(UID entityUID) {
		return MetaProvider.getInstance().isNuclosEntity(entityUID);
	}
}

