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
package org.nuclos.server.dal.processor.jdbc.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nuclos.common.EntityMeta;
import org.nuclos.common.FieldMeta;
import org.nuclos.common.NucletEntityMeta;
import org.nuclos.common.UID;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.dal.util.DalTransformations;
import org.nuclos.server.dal.specification.IDalReadSpecification;
import org.nuclos.server.database.SpringDataBaseHelper;
import org.nuclos.server.dblayer.structure.DbTableType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class ChartMetaDataProcessor implements IDalReadSpecification<NucletEntityMeta, UID> {

	private static final Logger LOG = Logger.getLogger(ChartMetaDataProcessor.class);
	
	private static ChartMetaDataProcessor INSTANCE;
	
	//
	
	private SpringDataBaseHelper dataBaseHelper;

	ChartMetaDataProcessor() {
		INSTANCE = this;
	}
	
	public static ChartMetaDataProcessor getInstance() {
		if (INSTANCE.dataBaseHelper == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}
	
	@Autowired
	final void setSpringDataBaseHelper(SpringDataBaseHelper dataBaseHelper) {
		this.dataBaseHelper = dataBaseHelper;
	}

	@Override
    public List<NucletEntityMeta> getAll() {
	    return getChartEntities();
    }

	@Override
	public NucletEntityMeta getByPrimaryKey(final UID uid) {
		return CollectionUtils.findFirst(getAll(), new Predicate<EntityMeta>() {
			@Override
            public boolean evaluate(EntityMeta t) {
	            return uid.equals(t.getUID());
            }});
	}

	@Override
	public List<NucletEntityMeta> getByPrimaryKeys(final List<UID> uids) {
		return CollectionUtils.applyFilter(getAll(), new Predicate<NucletEntityMeta>() {
			@Override
            public boolean evaluate(NucletEntityMeta t) {
	            return uids.contains(t.getUID());
            }});
	}

	@Override
	public List<UID> getAllIds() {
		return CollectionUtils.transform(getAll(), DalTransformations.getId(UID.class));
	}

	private Collection<String> getChartEntityViews() {
		return CollectionUtils.applyFilter(dataBaseHelper.getDbAccess().getTableNames(DbTableType.VIEW), new Predicate<String>() {
			@Override
			public boolean evaluate(String t) {
				// TODO MULTINUCLET
				//return t.toUpperCase().startsWith(CHART_ENTITY_VIEW_PREFIX);
				return false;
			}
		});
	}

	public List<NucletEntityMeta> getChartEntities() {
		ArrayList<NucletEntityMeta> res = new ArrayList<NucletEntityMeta>();
		long id = -1000l;
		for(String viewName : getChartEntityViews()) {
			NucletEntityMeta v = new NucletEntityMeta();
			// TODO MULTINUCLET
			v.setPrimaryKey(new UID());
			v.setUidEntity(true);
			String entityName = getEntityNameFromChartViewName(viewName);
			v.setSearchable(true);
			v.setEditable(false);
			v.setCacheable(false);
			v.setImportExport(false);
			v.setStateModel(false);
			v.setLogBookTracking(false);
			v.setTreeGroup(false);
			v.setTreeRelation(false);
			v.setFieldValueEntity(false);
			v.setDynamic(true);
			v.setEntityName(entityName);
			v.setDbTable(viewName);

			res.add(v);
			id = id - 1000l;
		}
		return res;
	}

	private String getEntityNameFromChartViewName(String viewName) {
		// TODO MULTINUCLET
        //return CHART_ENTITY_PREFIX + viewName.substring(CHART_ENTITY_VIEW_PREFIX.length()).toLowerCase();
		return viewName;
    }

	private static final Set<String> stExcludedFieldNamesForDynamicGeneration = new HashSet<String>(
		Arrays.asList("INTID", "DATCREATED", "STRCREATED", "DATCHANGED", "STRCHANGED", "INTVERSION", "BLNDELETED")
	);
	
	public void addChartEntities(Map<UID, Map<UID, FieldMeta<?>>> result, Map<UID, EntityMeta<?>> mapMetaDataByEntity) {
		for(String dyna : getChartEntityViews()) {
			// TODO MULTINUCLET
//			String entity = getEntityNameFromChartViewName(dyna);
//			Long entityId = mapMetaDataByEntity.containsKey(entity) ? mapMetaDataByEntity.get(entity).getPrimaryKey():null;
//			if (entityId != null) {
//				result.put(entity, getChartFieldsForView(dyna, entityId));
//			}
		}
	}

//	public Map<String, FieldMeta> getChartFieldsForView(String viewName, UID entity) {
//		Map<String, FieldMeta> res = new HashMap<String, FieldMeta>();
//		String entityName = getEntityNameFromChartViewName(viewName);
//		DbTable tableMetaData = dataBaseHelper.getDbAccess().getTableMetaData(viewName);
//
//		long columnId = entityId - 1;
//		for(DbColumn column : tableMetaData.getTableArtifacts(DbColumn.class)) {
//			String columnName = column.getColumnName();
//			if(!stExcludedFieldNamesForDynamicGeneration.contains(StringUtils.toUpperCase(columnName))) {
//				FieldMeta meta = newChartFieldVO(column, columnId, entityName, entityId);
//				res.put(meta.getField(), meta);
//			}
//			columnId = columnId - 1;
//		}
//		return res;
//	}

//	private FieldMeta newChartFieldVO(DbColumn dbColumn, long columnId, String entity, long entityId) {
//		FieldMeta result = DalUtils.getFieldMeta(dbColumn);
//		result.setPrimaryKey(columnId);
//		result.setEntityId(entityId);
//		if(CommonDatasourceFacade.REF_ENTITY.equals(result.getField().toUpperCase())) {
//			result.setField("genericObject");
//			result.setDbColumn(CommonDatasourceFacade.REF_ENTITY);
//			result.setDataType("java.lang.String");
//			// NUCLOS-9: field must be reference field (i.e. foreign entity must be set!)
//			result.setForeignEntity(E.GENERICOBJECT.getEntityName());
//			result.setScale(255);
//			result.setPrecision(null);
//			// this is a special column so we do not mark it as dynamic (esp. it's not case-sensitive)
//			// (Note: if you need to distinguish it, use the entity's dynamic flag)
//			result.setDynamic(false);
//		}
//		else {
//			LOG.debug("Create chart field metadata for " + entity + "." + dbColumn.getColumnName() + ": " + dbColumn.toString());
//			result.setDynamic(true);
//		}
//		result.setFallbacklabel(result.getField());
//		result.setNullable(true);
//		result.setSearchable(true);
//		result.setUnique(false);
//		result.setIndexed(false);
//		result.setLogBookTracking(false);
//		result.setInsertable(false);
//		result.setReadonly(true);
//		return result;
//	}
}

