/*
 * @(#)2012-10-30 V1.0
 * 
 * Copyright 2012 WELL-SOFT, Inc. All rights reserved.
 */
package com.wellsoft.pt.dyform.service.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cache.internal.CacheDataDescriptionImpl;
import org.hibernate.cache.spi.CollectionRegion;
import org.hibernate.cache.spi.EntityRegion;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.CustomConfiguration;
import org.hibernate.cfg.Settings;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.RootClass;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.internal.PersisterFactoryImpl;
import org.hibernate.persister.spi.PersisterFactory;
import org.hibernate.type.AssociationType;
import org.hibernate.type.Type;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.wellsoft.pt.common.component.jqgrid.JqGridQueryInfo;
import com.wellsoft.pt.common.component.jqgrid.JqTreeGridNode;
import com.wellsoft.pt.common.component.tree.TreeNode;
import com.wellsoft.pt.common.enums.Separator;
import com.wellsoft.pt.core.context.ApplicationContextHolder;
import com.wellsoft.pt.core.dao.PropertyFilter;
import com.wellsoft.pt.core.mt.service.TenantService;
import com.wellsoft.pt.core.resource.Config;
import com.wellsoft.pt.core.support.QueryData;
import com.wellsoft.pt.core.support.QueryInfo;
import com.wellsoft.pt.core.support.QueryItem;
import com.wellsoft.pt.dyform.dao.DyFormDefinitionDao;
import com.wellsoft.pt.dyform.entity.BeanCopyUtils;
import com.wellsoft.pt.dyform.entity.DyFormDefinition;
import com.wellsoft.pt.dyform.facade.DyFormApiFacade;
import com.wellsoft.pt.dyform.service.DyFormDefinitionService;
import com.wellsoft.pt.dyform.support.DyFormConfig;
import com.wellsoft.pt.dyform.support.DyFormConfig.DbDataType;
import com.wellsoft.pt.dyform.support.DyFormConfig.EnumFieldPropertyName;
import com.wellsoft.pt.dyform.support.DyFormConfig.EnumFormPropertyName;
import com.wellsoft.pt.dyform.support.DyFormDefinitionJSON;
import com.wellsoft.pt.dyform.support.TableConfig;
import com.wellsoft.pt.dyform.support.cache.DyformCacheUtils;
import com.wellsoft.pt.dyform.support.enums.EnumRelationTblSystemField;
import com.wellsoft.pt.dyform.support.enums.EnumSystemField;
import com.wellsoft.pt.dyform.support.exception.hibernate.HibernateDataExistException;
import com.wellsoft.pt.dytable.bean.TreeNodeBean;
import com.wellsoft.pt.mt.entity.Tenant;
import com.wellsoft.pt.utils.security.SpringSecurityUtils;

/**
 * 
 * @ClassName: FormDefinitionService
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author lilin
 */
@Service
@Transactional
public class DyFormDefinitionServiceImpl implements DyFormDefinitionService {
	private Logger logger = LoggerFactory.getLogger(DyFormDefinitionServiceImpl.class);

	@Autowired
	private DyFormDefinitionDao dyFormDefinitionDao;

	@Autowired
	private DyFormApiFacade dyFormApiFacade;

	public DyFormDefinitionServiceImpl() {
		//初始化表单缓存
		//initDyformCache();
	}

	private void saveFormDefinition(DyFormDefinition formDefinition) {

		//设置版本
		if (StringUtils.isEmpty(formDefinition.getUuid()) || "undefined".equals(formDefinition.getUuid())) {//新增 
			formDefinition.doBindVersionAsMinVersion();
			this.dyFormDefinitionDao.save(formDefinition);
			formDefinition.doBindUuid2Json();
			this.dyFormDefinitionDao.save(formDefinition);
		} else {
			if ("1".equals(formDefinition.getIsUp())) {//当调用方要升级版本时,isUp将会设置成1  
				formDefinition.setUuid(null);
				formDefinition.doBindVersionPlus();
				this.dyFormDefinitionDao.save(formDefinition);
				formDefinition.doBindUuid2Json();
				this.dyFormDefinitionDao.save(formDefinition);
			} else {
				DyFormDefinition obj = dyFormDefinitionDao.getEntitybyUuid(formDefinition.getUuid());
				if (obj == null) {
					throw new RuntimeException("tableName[" + formDefinition.getName() + "]对应的表单不存在 ");
				}
				try {
					BeanCopyUtils copyUtils = new BeanCopyUtils();
					copyUtils.copyProperties(obj, formDefinition);
				} catch (Exception e) {
				}
				obj.doBindUuid2Json();
				this.dyFormDefinitionDao.save(obj);
			}

		}

	}

	/** 
	 * 获取动态表单下拉框的初始值
	 * 
	 * (non-Javadoc)
	 * @see com.wellsoft.pt.dytable.service.FormDefinitionService#getFormKeyValuePair(java.lang.String, java.lang.String)
	 */
	@Override
	public QueryItem getFormKeyValuePair(String s, String formUuidsStr) {
		QueryItem queryitem = new QueryItem();
		if (StringUtils.isNotBlank(formUuidsStr)) {
			String[] formUuids = formUuidsStr.split(Separator.SEMICOLON.getValue());
			int j = formUuids.length;
			for (int i = 0; i < j; i++) {
				String formUuid = formUuids[i];
				DyFormDefinition definition = this.dyFormApiFacade.getFormDefinition(formUuid);

				if (definition != null) {
					String displayName = definition.getDisplayName();
					String moduleName = definition.getModuleName();
					if (queryitem.get("value") == null) {
						queryitem.put("label", moduleName + "/" + displayName);
						queryitem.put("value", formUuid);
					} else {
						queryitem.put(
								"label",
								(new StringBuilder()).append(queryitem.get("label"))
										.append(Separator.SEMICOLON.getValue()).append(moduleName + "/" + displayName)
										.toString());
						queryitem.put(
								"value",
								(new StringBuilder()).append(queryitem.get("value"))
										.append(Separator.SEMICOLON.getValue()).append(formUuid).toString());
					}

				} else {
					queryitem.put("label", formUuid);
					queryitem.put("value",
							(new StringBuilder()).append(queryitem.get("value")).append(Separator.SEMICOLON.getValue())
									.append(formUuid).toString());
				}
			}

		}
		return queryitem;

	}

	private String createRelationTblHbmXML(DyFormDefinition dyFormDefinition) {
		JSONObject formDefinition = new JSONObject();
		JSONObject fieldDefinitions = new JSONObject();
		JSONObject suboformDefinitions = new JSONObject();
		try {
			String relationTblName = dyFormDefinition.getName() + DyFormConfig.DYFORM_RELATIONTBL_POSTFIX;
			dyFormDefinition.getJsonHandler().addFormProperty(EnumFormPropertyName.relationTbl, relationTblName);
			dyFormDefinition.setRelationTbl(relationTblName);
			formDefinition.put(EnumFormPropertyName.name.name(), relationTblName);
			formDefinition.put(EnumFormPropertyName.fields.name(), fieldDefinitions);
			formDefinition.put(EnumFormPropertyName.subforms.name(), suboformDefinitions);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String xml = convertDefinitionJson2HbmCfg(formDefinition.toString(), EnumRelationTblSystemField.class,
				new ArrayList<String>());

		return xml;
	}

	/**
	 *判断是已存在表名为参数formTblName的数据表单表
	 * 
	 * @param formTblName
	 * @return
	 */
	public boolean isFormExistByFormTblName(String formTblName) {
		//查看表是否已存在,若已存在 
		List<DyFormDefinition> dfds = this.findDyFormDefinitionByTblName(formTblName);
		if (dfds == null || dfds.size() == 0) {
			return false;
		} else {
			return true;
		}
	}

	public void initDyformCache() {
		if (DyformCacheUtils.isInitOk()) {//已被初始化完毕
			return;
		}
		List<DyFormDefinition> defs = this.getAllFormDefintions();
		for (DyFormDefinition def : defs) {//底层框架设置bug,导致要加这段代码，否则会导致这些持久化对象中被属性被同步至数据库中
			def.setJsonHandler(null);
		}
		DyformCacheUtils.initDyformDefinitionCache(defs);
	}

	@Override
	@Transactional(readOnly = true)
	public DyFormDefinition findDyFormDefinitionByFormUuid(String formUuid) {
		initDyformCache();
		return DyformCacheUtils.getDyformDefinitionByUuid(formUuid);
	}

	@Override
	public DyFormDefinition findDyFormDefinitionByOuterId(String outerId) {
		initDyformCache();
		return DyformCacheUtils.getDyformDefinitionOfMaxVersionById(outerId);//获取最高版本的定义
	}

	public List<DyFormDefinition> findDyFormDefinitionsByOuterId(String outerId) {
		return this.dyFormDefinitionDao.find(Restrictions.eq("outerId", outerId));
	}

	public DyFormDefinition getFormDefinitionOfMaxVersionByOuterId(String outerId) {
		String hql = QUERY_MAX_VERSION_LIST + " and a.outer_id = :outerId";
		SQLQuery sqlQuery = this.dyFormDefinitionDao.getSession().createSQLQuery(hql);
		sqlQuery.setString("outerId", outerId);
		List<DyFormDefinition> list = sqlQuery.addEntity(DyFormDefinition.class).list();
		if (list == null || list.size() == 0) {
			return null;
		} else {
			return list.get(0);
		}
	}

	@Override
	public List<DyFormDefinition> findDyFormDefinitionByTblName(String tblName) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("name", tblName);

		List<DyFormDefinition> list = this.dyFormDefinitionDao.find(
				"select o from DyFormDefinition o where o.name = :name", params);
		for (DyFormDefinition dydf : list) {
			dydf.setJsonHandler(null);//dydf.getDefinitionJson//由于系统问题，这句不可删除
		}
		return list;

	}

	/**
	 *判断是已存在表名为参数formTblID的数据表单表
	 * 
	 * @param formTblName
	 * @return
	 */
	public boolean isFormExistByFormOuterId(String formTblId) {
		//查看表是否已存在,若已存在
		List<DyFormDefinition> forms = this.dyFormDefinitionDao.find(Restrictions.eq("id", formTblId));
		if (forms == null || forms.size() == 0) {
			return false;
		} else {
			return true;
		}
	}

	private String convertDefinitionJson2HbmCfg(String defintionJson, Class clazz, List<String> delFieldNames) {

		DyFormDefinitionJSON dJson;
		try {
			dJson = new DyFormDefinitionJSON(defintionJson);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return null;
		}
		Document doc = DocumentHelper.createDocument();

		doc.addDocType("hibernate-mapping", "-//Hibernate/Hibernate Mapping DTD 3.0//EN",
				"http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd");

		Element root = doc.addElement("hibernate-mapping");

		String tblName = dJson.getTblNameOfMainform();

		Element classEl = root.addElement("class").addAttribute("entity-name", tblName);
		TenantService tenantService = ApplicationContextHolder.getBean(TenantService.class);
		Tenant tenant = tenantService.getById(SpringSecurityUtils.getCurrentTenantId());
		classEl.addAttribute("schema", tenant.getJdbcUsername());
		if (delFieldNames != null) {
			Element commentEl = classEl.addElement("comment");
			JSONObject commentJSON = new JSONObject();
			try {
				commentJSON.put("delFieldNames", delFieldNames);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			commentEl.setText(commentJSON.toString());
		}

		//创建系统字段
		if (clazz == EnumSystemField.class) {
			for (EnumSystemField field : EnumSystemField.values()) {
				Element sysElement = null;
				sysElement = classEl.addElement(field.getElementType());
				sysElement.addAttribute("name", field.getName());
				sysElement.addAttribute("column", field.getColumn());
				String type = field.getDataType();
				sysElement.addAttribute("type", type);

				if ("id".equals(field.getElementType())) {
					sysElement.addElement("generator").addAttribute("class", "org.hibernate.id.UUIDGenerator");
				}
				if (type.equals("string")) {//字段串类型加长度
					sysElement.addAttribute("length", field.getLength().toString());
				}
			}
		} else if (clazz == EnumRelationTblSystemField.class) {
			for (EnumRelationTblSystemField field : EnumRelationTblSystemField.values()) {
				Element sysElement = null;
				sysElement = classEl.addElement(field.getElementType());
				sysElement.addAttribute("name", field.getName());
				sysElement.addAttribute("column", field.getColumn());
				String type = field.getDataType();
				sysElement.addAttribute("type", type);

				if ("id".equals(field.getElementType())) {
					sysElement.addElement("generator").addAttribute("class", "org.hibernate.id.UUIDGenerator");
				}
				if (type.equals("string")) {//字段串类型加长度
					sysElement.addAttribute("length", field.getLength().toString());
				}
			}
		}

		//设置用户自定义字段

		Iterator<String> it = dJson.getFieldNamesOfMainform().iterator();
		while (it.hasNext()) {

			String fieldName = it.next();

			String oldFieldName = "";//对于已有表单，该值为原来的字段名,用于修改字段名

			try {
				oldFieldName = dJson.getFieldPropertyOfStringType(fieldName, EnumFieldPropertyName.oldName);
			} catch (Exception e) {//如果调用者没传入该值，则直接用现有的name做为旧字段名,当现有的字段和旧字段名一样时，则不执行字段名更新操作
				oldFieldName = fieldName;
			}

			Element proEl = classEl.addElement("property");

			proEl.addAttribute("name", fieldName);//设置原来的字段名

			Element colEl = proEl.addElement("column");

			colEl.addAttribute("name", fieldName);

			//设置字段的类型
			String dataType = dJson.getFieldPropertyOfStringType(fieldName, EnumFieldPropertyName.dbDataType);
			String type = "";
			if (DbDataType._date.equals(dataType)) {//日期类型的字段 
				type = "java.sql.Timestamp";
			} else if (DbDataType._int.equals(dataType)) {
				type = "int";
			} else if (DbDataType._long.equals(dataType)) {
				type = "long";
			} else if (DbDataType._float.equals(dataType)) {
				type = "float";
			} else if (DbDataType._double.equals(dataType)) {
				type = "double";
			} else if (DbDataType._clob.equals(dataType)) {
				type = "clob";
			} else {
				type = "string";
				//设置字段的长度，默认长度为255
				String dataLength = dJson.getFieldPropertyOfStringType(fieldName, EnumFieldPropertyName.length);
				if (null != dataLength && dataLength.trim().length() > 0) {
					colEl.addAttribute("length", dataLength);
				} else {
					colEl.addAttribute("length", "255");
				}
			}

			//设置原字段名
			proEl.addAttribute("type", type);
			Element commentEl = colEl.addElement("comment");
			JSONObject commentJSON = new JSONObject();
			try {
				commentJSON.put("oldName", oldFieldName);
			} catch (JSONException e) {

				e.printStackTrace();
			}
			commentEl.setText(commentJSON.toString());

		}
		String xml = doc.asXML();
		System.out.println(xml);
		return xml;

	}

	/**
	 * 通过单例类configuration获取的sessionfactory
	 */
	private SessionFactory sessionFactory;

	/**
	 * 从configinstance获取的单例
	 */

	public void addNewConfig(Configuration cfg) {
		cfg.buildMappings();
		logger.info("add NewConfig.....");
		Settings settings = ((SessionFactoryImpl) getSessionFactory()).getSettings();

		Field mappingField;
		Mapping mapping;

		Field filterField;
		Map filters;

		Field idenField;
		Map identifierGenerators;

		/*Field allCacheField;
		Map allCacheRegions;*/

		Field entityPerField;
		Map entityPersisters;

		//Field namedQueriesField;
		//Map namedQueries;

		Field namedSqlQueriesField;
		Map namedSqlQueries;

		Field sqlResultSetMappingsField;
		Map sqlResultSetMappings;

		Field importsField;
		Map imports;

		Field classMetadataField;
		Map classMetadata;

		Field collectionPersistersField;
		Map collectionPersisters;
		try {
			mappingField = cfg.getClass().getDeclaredField("mapping");
			mappingField.setAccessible(true);
			mapping = (Mapping) (mappingField.get(cfg));

			filterField = ((SessionFactoryImpl) getSessionFactory()).getClass().getDeclaredField("filters");
			filterField.setAccessible(true);
			filters = (Map) (filterField.get(((SessionFactoryImpl) getSessionFactory())));

			idenField = ((SessionFactoryImpl) getSessionFactory()).getClass().getDeclaredField("identifierGenerators");
			idenField.setAccessible(true);
			identifierGenerators = (Map) (idenField.get(((SessionFactoryImpl) getSessionFactory())));

			/*allCacheField = ((SessionFactoryImpl) getSessionFactory()).getClass().getDeclaredField("allCacheRegions");
			allCacheField.setAccessible(true);
			allCacheRegions = (Map) (allCacheField.get(((SessionFactoryImpl) getSessionFactory())));*/

			entityPerField = ((SessionFactoryImpl) getSessionFactory()).getClass().getDeclaredField("entityPersisters");
			entityPerField.setAccessible(true);
			entityPersisters = (Map) (entityPerField.get(((SessionFactoryImpl) getSessionFactory())));

			//namedQueriesField = ((SessionFactoryImpl) getSessionFactory()).getClass().getDeclaredField("namedQueries");
			//namedQueriesField.setAccessible(true);
			//namedQueries = (Map) (namedQueriesField.get(((SessionFactoryImpl) getSessionFactory())));

			namedSqlQueriesField = ((SessionFactoryImpl) getSessionFactory()).getClass().getDeclaredField(
					"namedSqlQueries");
			namedSqlQueriesField.setAccessible(true);
			namedSqlQueries = (Map) (namedSqlQueriesField.get(((SessionFactoryImpl) getSessionFactory())));

			sqlResultSetMappingsField = ((SessionFactoryImpl) getSessionFactory()).getClass().getDeclaredField(
					"sqlResultSetMappings");
			sqlResultSetMappingsField.setAccessible(true);
			sqlResultSetMappings = (Map) (sqlResultSetMappingsField.get(((SessionFactoryImpl) getSessionFactory())));

			importsField = ((SessionFactoryImpl) getSessionFactory()).getClass().getDeclaredField("imports");
			importsField.setAccessible(true);
			imports = (Map) (importsField.get(((SessionFactoryImpl) getSessionFactory())));

			collectionPersistersField = ((SessionFactoryImpl) getSessionFactory()).getClass().getDeclaredField(
					"collectionPersisters");
			collectionPersistersField.setAccessible(true);
			collectionPersisters = (Map) (collectionPersistersField.get(((SessionFactoryImpl) getSessionFactory())));

			classMetadataField = ((SessionFactoryImpl) getSessionFactory()).getClass()
					.getDeclaredField("classMetadata");
			classMetadataField.setAccessible(true);
			classMetadata = (Map) (collectionPersistersField.get(((SessionFactoryImpl) getSessionFactory())));

			filters.putAll(cfg.getFilterDefinitions());
			//Generators:  
			Iterator classes = cfg.getClassMappings();
			while (classes.hasNext()) {
				PersistentClass model = (PersistentClass) classes.next();

				if (!model.isInherited()) {
					//				IdentifierGenerator generator = model.getIdentifier().createIdentifierGenerator(settings.getDialect(),
					//						settings.getDefaultCatalogName(), settings.getDefaultSchemaName(), (RootClass) model);

					IdentifierGenerator generator = model.getIdentifier().createIdentifierGenerator(
							cfg.getIdentifierGeneratorFactory(),
							((SessionFactoryImpl) getSessionFactory()).getDialect(), settings.getDefaultCatalogName(),
							settings.getDefaultSchemaName(), (RootClass) model);

					identifierGenerators.put(model.getEntityName(), generator);
				}
			}

			///////////////////////////////////////////////////////////////////////  
			// Prepare persisters and link them up with their cache  
			// region/access-strategy  

			String cacheRegionPrefix = settings.getCacheRegionPrefix() == null ? "" : settings.getCacheRegionPrefix()
					+ ".";

			Map entityAccessStrategies = new HashMap();
			Map tmpEntityPersisters = new HashMap();
			Map tmpClassMetadata = new HashMap();

			//		this.configuration.getClassMap().putAll(cfg.getClassMap());
			classes = cfg.getClassMappings();

			while (classes.hasNext()) {

				PersistentClass model = (PersistentClass) classes.next();

				model.prepareTemporaryTables(mapping, ((SessionFactoryImpl) getSessionFactory()).getDialect());
				String cacheRegionName = cacheRegionPrefix + model.getRootClass().getCacheRegionName();
				model.prepareTemporaryTables(mapping, ((SessionFactoryImpl) getSessionFactory()).getDialect());
				// cache region is defined by the root-class in the hierarchy...  
				EntityRegionAccessStrategy accessStrategy = (EntityRegionAccessStrategy) entityAccessStrategies
						.get(cacheRegionName);
				if (accessStrategy == null && settings.isSecondLevelCacheEnabled()) {
					AccessType accessType = AccessType.fromExternalName(model.getCacheConcurrencyStrategy());

					if (accessType != null) {

						logger.trace("Building cache for entity data [" + model.getEntityName() + "]");
						EntityRegion entityRegion = settings.getRegionFactory().buildEntityRegion(cacheRegionName,
								((SessionFactoryImpl) getSessionFactory()).getProperties(),
								CacheDataDescriptionImpl.decode(model));
						accessStrategy = entityRegion.buildAccessStrategy(accessType);
						entityAccessStrategies.put(cacheRegionName, accessStrategy);
						/*allCacheRegions.put(cacheRegionName, entityRegion);*/
					}
				}
				final String naturalIdCacheRegionName = cacheRegionPrefix + model.getNaturalIdCacheRegionName();
				NaturalIdRegionAccessStrategy naturalIdAccessStrategy = (NaturalIdRegionAccessStrategy) entityAccessStrategies
						.get(naturalIdCacheRegionName);

				EntityPersister cp = ((SessionFactoryImpl) getSessionFactory())
						.getServiceRegistry()
						.getService(PersisterFactory.class)
						.createEntityPersister(model, accessStrategy, naturalIdAccessStrategy,
								(SessionFactoryImplementor) getSessionFactory(), mapping);
				tmpEntityPersisters.put(model.getEntityName(), cp);
				tmpClassMetadata.put(model.getEntityName(), cp.getClassMetadata());

			}

			//Named Queries:  
			//namedQueries.putAll(cfg.getNamedQueries());
			namedSqlQueries.putAll(cfg.getNamedSQLQueries());
			sqlResultSetMappings.putAll(cfg.getSqlResultSetMappings());
			imports.putAll(cfg.getImports());

			entityPersisters.putAll(tmpEntityPersisters);
			//这个不能写入，有错误，回头还要再调试看原因
			classMetadata.putAll(Collections.unmodifiableMap(tmpClassMetadata));

			Map tmpEntityToCollectionRoleMap = new HashMap();
			Map tempCollectionPersisters = new HashMap();
			//这个不能写入，有错误，回头还要再调试看原因
			//			cfg.getCollectionMap().putAll(cfg.getCollectionMap());
			Iterator collections = cfg.getCollectionMappings();

			while (collections.hasNext()) {
				Collection model = (Collection) collections.next();
				final String cacheRegionName = cacheRegionPrefix + model.getCacheRegionName();
				final AccessType accessType = AccessType.fromExternalName(model.getCacheConcurrencyStrategy());
				CollectionRegionAccessStrategy accessStrategy = null;
				if (accessType != null && settings.isSecondLevelCacheEnabled()) {
					logger.trace("Building cache for collection data [" + model.getRole() + "]");
					CollectionRegion collectionRegion = settings.getRegionFactory().buildCollectionRegion(
							cacheRegionName, ((SessionFactoryImpl) getSessionFactory()).getProperties(),
							CacheDataDescriptionImpl.decode(model));
					accessStrategy = collectionRegion.buildAccessStrategy(accessType);
					entityAccessStrategies.put(cacheRegionName, accessStrategy);
					/*allCacheRegions.put(cacheRegionName, collectionRegion);*/
				}
				CollectionPersister persister = new PersisterFactoryImpl().createCollectionPersister(cfg, model,
						accessStrategy, (SessionFactoryImpl) getSessionFactory());
				tempCollectionPersisters.put(model.getRole(), persister.getCollectionMetadata());
				Type indexType = persister.getIndexType();
				if (indexType != null && indexType.isAssociationType() && !indexType.isAnyType()) {
					String entityName = ((AssociationType) indexType)
							.getAssociatedEntityName((SessionFactoryImpl) getSessionFactory());
					Set roles = (Set) tmpEntityToCollectionRoleMap.get(entityName);
					if (roles == null) {
						roles = new HashSet();
						tmpEntityToCollectionRoleMap.put(entityName, roles);
					}
					roles.add(persister.getRole());
				}
				Type elementType = persister.getElementType();
				if (elementType.isAssociationType() && !elementType.isAnyType()) {
					String entityName = ((AssociationType) elementType)
							.getAssociatedEntityName((SessionFactoryImpl) getSessionFactory());
					Set roles = (Set) tmpEntityToCollectionRoleMap.get(entityName);
					if (roles == null) {
						roles = new HashSet();
						tmpEntityToCollectionRoleMap.put(entityName, roles);
					}
					roles.add(persister.getRole());
				}

			}
			//加入新的  
			collectionPersisters.putAll(tempCollectionPersisters);

			Iterator itr = tmpEntityToCollectionRoleMap.entrySet().iterator();
			while (itr.hasNext()) {
				final Map.Entry entry = (Map.Entry) itr.next();
				entry.setValue(Collections.unmodifiableSet((Set) entry.getValue()));
			}
			//这个不能写入，有错误，回头还要再调试看原因
			//		collectionRolesByEntityParticipant.putAll(tmpEntityToCollectionRoleMap);

			// after *all* persisters and named queries are registered  
			Iterator iter = tmpEntityPersisters.values().iterator();
			while (iter.hasNext()) {
				((EntityPersister) iter.next()).postInstantiate();
			}
			iter = tempCollectionPersisters.values().iterator();
			while (iter.hasNext()) {
				((CollectionPersister) iter.next()).postInstantiate();
			}
		} catch (Exception e) {
			//e.printStackTrace();
			logger.error("add NewConfig error.....");
		}
	}

	/**
	 * 
	* @Title: getSessionFactory
	* @Description: 获取sessionfactory
	* @param @return    设定文件
	* @return SessionFactory    返回类型
	* @throws
	 */
	public SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			sessionFactory = ApplicationContextHolder.getBean(Config.TENANT_SESSION_FACTORY_BEAN_NAME,
					SessionFactory.class);
		}
		return sessionFactory;
	}

	@Override
	public void createFormDefinitionAndFormTable(DyFormDefinition formDefinition) {

		/*//保存表单数据表定义信息
		this.saveFormDefinition(formDefinition);

		//创建表单数据表结构

		this.createOrUpdateDyForm(formDefinition.getDefinitionJson(), null);*/

		this.updateFormDefinitionAndFormTable(formDefinition, new ArrayList<String>());

	}

	@Override
	public void updateFormDefinitionAndFormTable(DyFormDefinition formDefinition, List<String> deletedFieldNames) {

		//this.removeFieldsAndTheirAssistedFields(formDefinition, deletedFieldNames);//删除辅助性字段
		formDefinition.getJsonHandler().removeFields(deletedFieldNames);

		//对于在新版本中被删除的列，在其他版本的定义也需要同步删除
		try {
			doSynDefinitionWithOtherVersions(formDefinition, deletedFieldNames);
		} catch (JSONException e) {
			throw new HibernateException(e.getMessage());
		}

		//dUtils.removeFieldByFieldName("deletedFieldNames");

		//保存或者更新表单数据表定义信息
		String relationTblHbmXML = this.createRelationTblHbmXML(formDefinition);//生成数据关系表

		String tblHbmXML = this.createTblHbmXML(formDefinition, deletedFieldNames);

		this.saveFormDefinition(formDefinition);

		//创建或者更新表单数据表结构 
		//this.createOrUpdateDyFormTbl(formDefinition, deletedFieldNames);
		this.createOrUpdateTbl(formDefinition, tblHbmXML, relationTblHbmXML);

		DyformCacheUtils.updateOrAdd(formDefinition);

	}

	/**
	 * 删除辅助性字段
	 * 
	 * @param formDefinition
	 * @param deletedFieldNames
	 */
	private void removeFieldsAndTheirAssistedFields(DyFormDefinition formDefinition, List<String> deletedFieldNames) {
		DyFormDefinitionJSON dJson = formDefinition.getJsonHandler();
		List<String> deletedRealDisplayFieldNames = new ArrayList<String>();
		for (String deletedFieldName : deletedFieldNames) {
			if (dJson.isValueAsMap(deletedFieldName)) {
				String realFieldName = dJson.getAssistedFieldNameRealValue(deletedFieldName);
				if (realFieldName == null) {
					continue;
				}
				String displayFieldName = dJson.getAssistedFieldNameDisplayValue(deletedFieldName);

				deletedRealDisplayFieldNames.add(realFieldName);
				deletedRealDisplayFieldNames.add(displayFieldName);
			}
		}

		deletedFieldNames.addAll(deletedRealDisplayFieldNames);
		formDefinition.getJsonHandler().removeFields(deletedFieldNames);

	}

	private void createOrUpdateTbl(DyFormDefinition formDefinition, String tblHbmXML, String relationTblHbmXML) {

		//将定义json转换成hibernate配置文件(也就是hibernate.cfg.xml格式的文件)
		String defintionJson = formDefinition.getDefinitionJson();

		//this.updateSubformRelation(defintionJson);

		logger.debug("用户配置后生成相应的hbm文件内容(新建):" + tblHbmXML);

		//根据hibernate配置文件生成或者更新表结构
		CustomConfiguration config = new CustomConfiguration();
		config.addXML(tblHbmXML);
		this.addNewConfig(config);
		TableConfig tableconfig = new TableConfig(config);
		CustomConfiguration config2 = new CustomConfiguration();
		config2.addXML(relationTblHbmXML);
		this.addNewConfig(config2);
		TableConfig tableconfig2 = new TableConfig(config2);

		if (!this.isFormExistByFormTblName(formDefinition.getName())) {//数据表单表不存在

			//生成数据关系表 
			tableconfig2.addTable();

			//生成数据表
			tableconfig.addTable();
		} else {//更新数据表 
			tableconfig.updateTable();
			tableconfig2.updateTable();
		}
	}

	private String createTblHbmXML(DyFormDefinition formDefinition, List<String> deletedFieldNames) {
		//createAssistedField(formDefinition);//生成辅助性字段
		return convertDefinitionJson2HbmCfg(formDefinition.getDefinitionJson(), EnumSystemField.class,
				deletedFieldNames);
	}

	/**
	 * 添加辅助性字段
	 * 
	 * @param formDefinition
	
	private void createAssistedField(DyFormDefinition formDefinition) {
		DyFormDefinitionJSON dJon = formDefinition.getJsonHandler();
		Iterator<String> it = dJon.getFieldNamesOfMainform().iterator();
		while (it.hasNext()) {
			String fieldName = it.next();

			if (dJon.isSysTypeAsCustom(fieldName) && dJon.isValueAsMap(fieldName)) {

				JSONObject assistedFieldName = dJon.getFieldPropertyOfJSONType(fieldName,
						EnumFieldPropertyName.realDisplay);

				if (assistedFieldName == null || assistedFieldName.length() == 0) {
					if (!(StringUtils.isEmpty(formDefinition.getUuid()) || "undefined".equals(formDefinition.getUuid()))) {
						DyFormDefinition dbFormDef = this.dyFormApiFacade.getFormDefinition(formDefinition.getUuid());
						assistedFieldName = dbFormDef.getJsonHandler().getFieldPropertyOfJSONType(fieldName,
								EnumFieldPropertyName.realDisplay);
					}
				}

				if (assistedFieldName == null || assistedFieldName.length() == 0) {
					String realFieldName = fieldName + DyFormConfig.assistedpofix4realValue;
					try {
						JSONObject realFieldDefinition = new JSONObject();

						realFieldDefinition.put(EnumFieldPropertyName.name.name(), realFieldName);
						realFieldDefinition.put(EnumFieldPropertyName.dbDataType.name(), DbDataType._string);
						realFieldDefinition.put(EnumFieldPropertyName.length.name(), 1000);
						realFieldDefinition.put(EnumFieldPropertyName.sysType.name(), DyFieldSysType.assist);
						dJon.addField(realFieldName, realFieldDefinition);

					} catch (JSONException e) {
						e.printStackTrace();
					}
					String displayFieldName = fieldName + DyFormConfig.assistedpofix4DisplayValue;
					try {
						JSONObject displayFieldDefinition = new JSONObject();

						displayFieldDefinition.put(EnumFieldPropertyName.name.name(), displayFieldName);
						displayFieldDefinition.put(EnumFieldPropertyName.dbDataType.name(), DbDataType._string);
						displayFieldDefinition.put(EnumFieldPropertyName.length.name(), 1000);
						displayFieldDefinition.put(EnumFieldPropertyName.sysType.name(), DyFieldSysType.assist);
						dJon.addField(displayFieldName, displayFieldDefinition);
					} catch (JSONException e) {
						e.printStackTrace();
					}

					try {
						assistedFieldName = new JSONObject();
						assistedFieldName.put(realFieldName, displayFieldName);
						dJon.addFieldProperty(fieldName, EnumFieldPropertyName.realDisplay, assistedFieldName);
					} catch (JSONException e) {
						e.printStackTrace();
					}

				}

			}
		}
	} */

	/**
	 * 将参数中的表单的JSON信息同步到其他版本的表单定义中
	 * @param formDefinition
	 * @throws JSONException 
	 */
	private void doSynDefinitionWithOtherVersions(DyFormDefinition formDefinition, List<String> delFieldNames)
			throws JSONException {
		if (delFieldNames == null || delFieldNames.size() == 0) {
			logger.debug("没有字段需要删除");
			return;
		}

		List<DyFormDefinition> formDefinitions = dyFormApiFacade.getFormDefinitionsByTblName(formDefinition.getName());

		for (DyFormDefinition df : formDefinitions) {

			if (df.getUuid().equalsIgnoreCase(formDefinition.getUuid())) {
				continue;
			}

			for (String delFn : delFieldNames) {
				if (df.getJsonHandler().isFieldInDefinition(delFn)) {
					df.getJsonHandler().removeFieldByFieldName(delFn);
				}
			}
			this.dyFormDefinitionDao.save(df);
		}
	}

	@Override
	public List<TreeNodeBean> getFormOutlineOfAllVersionTreeByFormUuid(String formUuid) {
		List<TreeNodeBean> list = Lists.newArrayList();
		DyFormDefinition pForm = dyFormDefinitionDao.get(formUuid);
		List<DyFormDefinition> forms = dyFormDefinitionDao.findBy("name", pForm.getName());
		for (int i = forms.size() - 1; i >= 0; i--) {
			DyFormDefinition obj = forms.get(i);
			if (!StringUtils.equals(formUuid, obj.getUuid())) {
				TreeNodeBean node = new TreeNodeBean(obj.getUuid(), obj.getDisplayName() + "(" + obj.getVersion() + ")");
				list.add(node);
			}
		}
		return list;
	}

	@Override
	public List<TreeNodeBean> getFormOutlineOfAllVersionTreeByFormName(String formName) {
		List<TreeNodeBean> list = Lists.newArrayList();
		List<DyFormDefinition> forms = dyFormDefinitionDao.findBy("name", formName);
		for (int i = forms.size() - 1; i >= 0; i--) {
			DyFormDefinition obj = forms.get(i);
			TreeNodeBean node = new TreeNodeBean(obj.getUuid(), obj.getDisplayName() + "(" + obj.getVersion() + ")");
			list.add(node);
		}
		return list;
	}

	public List<TreeNodeBean> getFormOutlineOfAllVersionTree(String parentId, String selectedFormUuid) {
		List<TreeNodeBean> tree = Lists.newArrayList();

		List<DyFormDefinition> forms = dyFormDefinitionDao.getMaxVersionList();
		for (DyFormDefinition fDefinition : forms) {
			TreeNodeBean node = new TreeNodeBean(fDefinition.getUuid(), fDefinition.getDisplayName() + "("
					+ fDefinition.getVersion() + ")");
			tree.add(node);

			if (fDefinition.isMinVersion()) {//该表单已是最低版本
				node.setOpen(false);
				node.setIsParent("false");
				setChecked(node, selectedFormUuid);//设置选中节点
			} else {//该表单还有其他低版本
				List<TreeNodeBean> chirldrenTree = this.getFormOutlineOfAllVersionTreeByFormUuid(fDefinition.getUuid());
				setChecked(node, selectedFormUuid);//设置选中节点

				if (chirldrenTree == null || chirldrenTree.size() == 0) {
					node.setOpen(false);
					node.setIsParent("false");
				} else {
					node.setIsParent("true");
					if (setChecked(chirldrenTree, selectedFormUuid)) {//设置选中节点
						node.setOpen(true);
					} else {
						node.setOpen(false);
					}
					node.setChildren(chirldrenTree);

				}
			}
		}
		return tree;
	}

	private boolean setChecked(Object obj, String selectedFormUuid) {
		if (obj == null) {
			return false;
		}

		if (obj instanceof TreeNodeBean) {
			TreeNodeBean node = (TreeNodeBean) obj;
			if (node.getId().equals(selectedFormUuid)) {
				node.setChecked(true);
				return true;
			}
		} else if (obj instanceof List) {
			List<TreeNodeBean> nodes = (List<TreeNodeBean>) obj;
			for (TreeNodeBean node : nodes) {
				return setChecked(node, selectedFormUuid);
			}
		}

		return false;
	}

	/** 
	 * 如何描述该方法
	 * 
	 * (non-Javadoc)
	 * @see com.wellsoft.pt.dytable.service.FormDefinitionService#getForPageAsTree(com.wellsoft.pt.common.component.jqgrid.JqGridQueryInfo, com.wellsoft.pt.core.support.QueryInfo)
	 */
	@Override
	public QueryData getForPageAsTree(JqGridQueryInfo jqGridQueryInfo, QueryInfo queryInfo, String flag) {
		// 设置查询字段条件
		Map<String, Object> values = PropertyFilter.convertToMap(queryInfo.getPropertyFilters());
		// 查询父节点为null的部门
		List<QueryItem> results = null;

		Session session = dyFormDefinitionDao.getSession();
		System.out.println(sessionFactory + "" + session);
		if (StringUtils.isBlank(jqGridQueryInfo.getNodeid())) {
			if ("1".equals(flag)) {
				results = this.dyFormDefinitionDao.namedQuery("topDyFormDefinitionTreeQuery", values, QueryItem.class,
						queryInfo.getPagingInfo());
			} else {
				results = this.dyFormDefinitionDao.namedQuery("topDyFormDefinitionTreeQuery2", values, QueryItem.class,
						queryInfo.getPagingInfo());
			}

		} else {
			String uuid = jqGridQueryInfo.getNodeid();
			DyFormDefinition parent = this.dyFormDefinitionDao.get(uuid);
			values.put("parentUuid", uuid);
			values.put("outerId", parent.getOuterId());
			results = this.dyFormDefinitionDao.namedQuery("dyFormDefinitionTreeQuery", values, QueryItem.class,
					queryInfo.getPagingInfo());
		}
		// results = pageData.getResult();
		List<JqTreeGridNode> retResults = new ArrayList<JqTreeGridNode>();

		int level = jqGridQueryInfo.getN_level() == null ? 0 : jqGridQueryInfo.getN_level() + 1;
		String parentId = jqGridQueryInfo.getNodeid() == null ? "null" : jqGridQueryInfo.getNodeid();
		for (int index = 0; index < results.size(); index++) {
			QueryItem item = results.get(index);
			JqTreeGridNode node = new JqTreeGridNode();
			node.setId(item.get("outerId").toString());// id
			List<Object> cell = node.getCell();
			cell.add(item.get("uuid"));// UUID
			cell.add(item.get("displayName") + " (" + item.get("version") + ")");// name
			cell.add(item.get("name"));// name
			cell.add(item.get("version"));// version
			cell.add(item.get("moduleName"));// enabled
			cell.add(item.get("moduleId"));// formName
			cell.add(item.get("outerId"));// version
			cell.add(item.get("code"));// version
			// level field
			cell.add(level);
			// parent id field
			cell.add(parentId);
			// leaf field
			if (StringUtils.isBlank(jqGridQueryInfo.getNodeid())) {
				if (Double.valueOf(1).toString().equals(item.get("version"))) {
					cell.add(true);
				} else {
					long count = this.dyFormDefinitionDao.countById(item.get("outerId").toString());
					cell.add(count <= 1);
				}
			} else {
				cell.add(true);
			}
			// expanded field	第一个节点展开
			if ("null".equals(parentId)) {
				cell.add(true);
			} else {
				cell.add(false);
			}

			retResults.add(node);
		}
		QueryData queryData = new QueryData();
		queryData.setDataList(retResults);
		queryData.setPagingInfo(queryInfo.getPagingInfo());
		return queryData;
	}

	/** 
	 * 根据formUuid获取字段的树形(控制显示\隐藏字段使用)
	 * 
	 * (non-Javadoc)
	 */
	@Override
	public List<TreeNode> getFieldByFormUuid(String s, String formUuid) {
		JSONObject fieldjson = this.dyFormApiFacade.getFormDefinition(formUuid).getJsonHandler().getFieldDefinitions();
		Iterator<String> it = fieldjson.keys();
		List<TreeNode> treeNodes = new ArrayList<TreeNode>();
		while (it.hasNext()) {
			String fieldName = it.next();
			org.json.JSONObject fieldinfo;
			try {
				fieldinfo = fieldjson.getJSONObject(fieldName);
				TreeNode treeNode = new TreeNode();
				String displayname = fieldinfo.getString("displayName");
				String fieldname = fieldinfo.getString("name");
				if (displayname == null || displayname.equals("")) {
					treeNode.setName(fieldname);
				} else {
					treeNode.setName(displayname);
				}
				treeNode.setId(fieldname);
				//treeNode.setData(fieldinfo);
				treeNodes.add(treeNode);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return treeNodes;
	}

	@Override
	public List<DyFormDefinition> getFormDefinitionsByTblName(String tblName) {

		return dyFormDefinitionDao.getFormDefinitionsByTblName(tblName);
	}

	@Override
	public List<DyFormDefinition> getAllFormDefintions() {
		List<DyFormDefinition> dydefs = new ArrayList<DyFormDefinition>();
		for (DyFormDefinition dydef : this.dyFormDefinitionDao.getAll()) {
			dydef.setJsonHandler(null);
			dydefs.add(dydef);
		}
		return dydefs;
	}

	private final String QUERY_MAX_VERSION_LIST = "select * from DYFORM_FORM_DEFINITION a,"
			+ "(select name, max(version) version from DYFORM_FORM_DEFINITION group by name) b"
			+ " where a.name = b.name and a.version = b.version";

	@Override
	public List<DyFormDefinition> getMaxVersionList() {

		List<DyFormDefinition> list = this.dyFormDefinitionDao.getSession().createSQLQuery(QUERY_MAX_VERSION_LIST)
				.addEntity(DyFormDefinition.class).list();

		for (DyFormDefinition dydf : list) {
			dydf.setJsonHandler(null);//dydf.getDefinitionJson//由于系统问题，这句不可删除
		}
		return list;
	}

	@Override
	public DyFormDefinition getFormDefinitionOfMaxVersionByTblName(String tableName) {
		String hql = QUERY_MAX_VERSION_LIST + " and a.name = :name";
		SQLQuery sqlQuery = this.dyFormDefinitionDao.getSession().createSQLQuery(hql);
		sqlQuery.setString("name", tableName);
		List<DyFormDefinition> list = sqlQuery.addEntity(DyFormDefinition.class).list();
		if (list == null || list.size() == 0) {
			return null;
		} else {
			return list.get(0);
		}
	}

	@Override
	public DyFormDefinition getFormDefinition(String tblName, String version) {
		return this.dyFormDefinitionDao.findUnique(Restrictions.and(Restrictions.eq("name", tblName),
				Restrictions.eq("version", version)));
	}

	@Override
	public void dropForm(String formUuid) {

		DyFormDefinition dy = this.dyFormApiFacade.getFormDefinition(formUuid);
		if (dy == null) {
			return;
		}

		List<DyFormDefinition> dys = this.dyFormApiFacade.getFormDefinitionsByTblName(dy.getName());

		if (dys.size() > 1) {//该表的定义有多个版本
			if (this.existDataInFormByFormUuid(formUuid)) {
				throw new HibernateDataExistException("cann't drop this form definition,  some  datas exist in table["
						+ dy.getName() + "]formUuid[" + formUuid + "]");
			}

			this.dyFormDefinitionDao.delete(formUuid);//只删除定义,因为还有其他版本的定义,所以表结构不得删除

		} else {//定义只有一个版本
			long count = this.dyFormApiFacade.countDataInForm(dy.getName());
			if (count > 0) {//表单中中有数据，不得删除
				throw new HibernateDataExistException("cann't drop this form , there are " + count + " datas in table["
						+ dy.getName() + "] ");
			}

			//this.dyFormDefinitionDao.dropFormTbl(dy.getName());

			String tblHbmXML = this.convertDefinitionJson2HbmCfg(dy.getDefinitionJson(), EnumSystemField.class, null);
			CustomConfiguration config = new CustomConfiguration();
			config.addXML(tblHbmXML);
			this.addNewConfig(config);
			TableConfig tableconfig = new TableConfig(config);

			String tblHbmXML2 = this.createRelationTblHbmXML(dy);
			CustomConfiguration config2 = new CustomConfiguration();
			config2.addXML(tblHbmXML2);
			this.addNewConfig(config2);
			TableConfig tableconfig2 = new TableConfig(config2);

			this.dyFormDefinitionDao.delete(formUuid);
			tableconfig.dropTable();
			tableconfig2.dropTable();
		}

		DyformCacheUtils.delete(formUuid);
	}

	/**
	 * 判断指定的formUuid在数据表中是否有数据
	 * 
	 * @param formUuid
	 * @return
	 */
	public boolean existDataInFormByFormUuid(String formUuid) {
		float i = 10.334132412344f;
		long countOfSpecifiedForm = this.dyFormApiFacade.countByFormUuid(formUuid);//查看该版本是否有数据
		if (countOfSpecifiedForm > 0) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	public List<DyFormDefinition> getFormDefinitionByModelId(String modelId) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("displayFormModelId", modelId);

		List<DyFormDefinition> list = this.dyFormDefinitionDao.find(
				"select o from DyFormDefinition o where o.displayFormModelId = :displayFormModelId", params);
		for (DyFormDefinition dydf : list) {
			dydf.setJsonHandler(null);//dydf.getDefinitionJson//由于系统问题，这句不可删除
		}
		return list;
	}

}

