//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.server.dal.processor;

import static org.nuclos.server.dal.processor.AbstractDalProcessor.DT_BOOLEAN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nuclos.common.E;
import org.nuclos.common.EntityLafParameterVO;
import org.nuclos.common.EntityMeta;
import org.nuclos.common.FieldMeta;
import org.nuclos.common.IMetaProvider;
import org.nuclos.common.NucletEntityMeta;
import org.nuclos.common.NucletFieldMeta;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.PivotInfo;
import org.nuclos.common.SF;
import org.nuclos.common.UID;
import org.nuclos.common.WorkspaceVO;
import org.nuclos.common.dal.vo.EOGenericObjectVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.IDalVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.MetaProvider;
import org.nuclos.server.common.SessionUtils;
import org.nuclos.server.dal.processor.jdbc.TableAliasSingleton;
import org.nuclos.server.dal.processor.jdbc.impl.ChartEntityObjectProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.DynamicEntityObjectProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.EOGenericObjectProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.EntityLafParameterProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.EntityMetaProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.EntityObjectProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.FieldMetaProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.ImportObjectProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.WorkspaceProcessor;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.database.SpringDataBaseHelper;
import org.nuclos.server.dblayer.DbUtils;
import org.nuclos.server.fileimport.ImportStructure;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessorFactorySingleton {

	private static ProcessorFactorySingleton INSTANCE;
	
	//
	
	private TableAliasSingleton tableAliasSingleton;
	
	private SpringDataBaseHelper dataBaseHelper;
	
	private SessionUtils utils;

	private ProcessorFactorySingleton() {
		INSTANCE = this;
	}

	public static ProcessorFactorySingleton getInstance() {
		return INSTANCE;
	}
	
	@Autowired
	void setTableAliasSingleton(TableAliasSingleton tableAliasSingleton) {
		this.tableAliasSingleton = tableAliasSingleton;
	}
	
	@Autowired
	void setSpringDataBaseHelper(SpringDataBaseHelper dataBaseHelper) {
		this.dataBaseHelper = dataBaseHelper;
	}
	
	@Autowired
	void setSessionUtils(SessionUtils utils) {
		this.utils = utils;
	}
	
	private static <S, PK> IColumnToVOMapping<S, PK> createBeanMapping(String alias, Class<? extends IDalVO<PK>> type, FieldMeta<S> ef, String methodRadical) {
		try {
			return (IColumnToVOMapping<S, PK>) createBeanMapping(alias, type, ef.getDbColumn(), methodRadical, ef.getUID(), Class.forName(ef.getDataType()), false);
		} catch (ClassNotFoundException e) {
			throw new NuclosFatalException(e);
		}
	}
	
	private static <S, PK> IColumnToVOMapping<S, PK> createBeanMapping(String alias, Class<? extends IDalVO<PK>> type, SF<S> sf, UID entityUID) {
		Class<S> javaClass = sf.getJavaClass();
//		if (SF.PK_ID.equals(sf) || SF.PK_UID.equals(sf) ) {
//			javaClass = (Class<S>) Object.class;
//		}
		return createBeanMapping(alias, type, sf.getDbColumn(), sf.getFieldName(), sf.getUID(entityUID), javaClass, false);
	}
	
	private static <S, PK> IColumnToVOMapping<S, PK> createBeanMapping(String alias, Class<? extends IDalVO<PK>> type, FieldMeta<S> fieldMeta) {
		try {
			return (IColumnToVOMapping<S, PK>) createBeanMapping(alias, type, fieldMeta.getDbColumn(), fieldMeta.getFieldName(), fieldMeta.getUID(), Class.forName(fieldMeta.getDataType()), false);
		} catch (ClassNotFoundException e) {
			throw new NuclosFatalException(e);
		}
	}
	
	private static <S, PK> IColumnToVOMapping<S, PK> createBeanMapping(String alias, Class<? extends IDalVO<PK>> type, FieldMeta<?> fieldMeta, Class<S> dataType) {
		return createBeanMapping(alias, type, fieldMeta.getDbColumn(), fieldMeta.getFieldName(), fieldMeta.getUID(), dataType, false);
	}

	private static <S, PK> IColumnToVOMapping<S, PK> createBeanMapping(String alias, Class<? extends IDalVO<PK>> type, String column, String methodRadical, UID fieldUID, Class<S> dataType, boolean isReadonly) {
		final String xetterSuffix = methodRadical.substring(0, 1).toUpperCase() + methodRadical.substring(1);
		// final Class<?> clazz = getDalVOClass();
		Class<?> methodParameterType = dataType;
		if ("primaryKey".equals(methodRadical)) {
			methodParameterType = Object.class;
		}
		try {
			return new ColumnToBeanVOMapping<S, PK>(alias, column, fieldUID, type.getMethod("set" + xetterSuffix, methodParameterType),
					type.getMethod((DT_BOOLEAN.equals(dataType) ? "is" : "get") + xetterSuffix), dataType, isReadonly);
		} catch (Exception e) {
			throw new CommonFatalException("On " + type + ": " + e);
		}
	}

	private static <PK> boolean isIdColumnInList(List<IColumnToVOMapping<?, PK>> list, String columnName) {
		return getColumnFromList(list, columnName) != null;
	}

	private static <PK> IColumnToVOMapping<?, ?> getColumnFromList(List<IColumnToVOMapping<?, PK>> list, String columnName) {
		for (IColumnToVOMapping<?, ?> column : list) {
			if (column.getColumn().equals(columnName)) {
				return column;
			}
		}
		return null;
	}

	protected static <S extends Object, PK> IColumnToVOMapping<S, PK> createFieldMapping(String alias, FieldMeta field) {
		try {
			return new ColumnToFieldVOMapping<S, PK>(alias, field);
		} catch (ClassNotFoundException e) {
			throw new CommonFatalException(e);
		}
	}

	protected <S extends Object, PK> IColumnToVOMapping<S, PK> createRefFieldMapping(FieldMeta field) {
		try {
			final String alias = tableAliasSingleton.getAlias(field);
			return new ColumnToRefFieldVOMapping<S, PK>(alias, field);
		} catch (ClassNotFoundException e) {
			throw new CommonFatalException(e);
		}
	}

	protected static <S extends Object, PK> IColumnToVOMapping<S, PK> createFieldIdMapping(String alias, FieldMeta field, boolean isUidEntity) {
		try {
			return new ColumnToFieldIdVOMapping<S, PK>(alias, field, isUidEntity);
		} catch (ClassNotFoundException e) {
			throw new CommonFatalException(e);
		}
	}

	protected static <S extends Object, PK> IColumnToVOMapping<S, PK> createPivotJoinMapping(String alias,
			FieldMeta mdField, UID joinEntity) {
		try {
			return new PivotJoinEntityFieldVOMapping<S, PK>(alias, mdField, joinEntity);
		} catch (ClassNotFoundException e) {
			throw new CommonFatalException(e);
		}
	}

	public <PK> JdbcEntityObjectProcessor<PK> newEntityObjectProcessor(EntityMeta<?> eMeta, Collection<FieldMeta<?>> colEfMeta, boolean addSystemColumns) {
		final Class<EntityObjectVO<PK>> eov = LangUtils.getGenericClass(EntityObjectVO.class);
		final Class<? extends IDalVO<PK>> type = (Class<? extends IDalVO<PK>>) eov;
		final ProcessorConfiguration<PK> config = newProcessorConfiguration(type, eMeta, colEfMeta, addSystemColumns);
		final EntityObjectProcessor<PK> result = new EntityObjectProcessor<PK>(config);
		
		// HACK: force spring, as @Autowired on EntityObjectProcessor does not work (tp)
		result.setDataBaseHelper(dataBaseHelper);
		result.setTableAliasSingleton(tableAliasSingleton);
		result.setSessionUtils(utils);
		
		return result;
	}
	
	private <PK> ProcessorConfiguration<PK> newProcessorConfiguration(Class<? extends IDalVO<PK>> type, EntityMeta<?> eMeta, Collection<FieldMeta<?>> colEfMeta, boolean addSystemColumns) {
		final List<IColumnToVOMapping<? extends Object, PK>> allColumns = new ArrayList<IColumnToVOMapping<? extends Object, PK>>();

		Set<UID> staticSystemFields = new HashSet<UID>();
		
		IColumnToVOMapping<?, PK> pkColumn;
		if (eMeta.isUidEntity()) {
			pkColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, SF.PK_UID, eMeta.getUID());
		} else {
			pkColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, SF.PK_ID, eMeta.getUID());
		}
		allColumns.add(pkColumn);
		final IColumnToVOMapping<Integer, PK> versionColumn;

		if(addSystemColumns) {
			allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CREATEDAT, eMeta.getUID()));
			allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CREATEDBY, eMeta.getUID()));
			allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CHANGEDAT, eMeta.getUID()));
			allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CHANGEDBY, eMeta.getUID()));
			versionColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, SF.VERSION, eMeta.getUID());
			allColumns.add(versionColumn);
		} else {
			versionColumn = null;
		}
		
		for (IColumnToVOMapping<?, ?> col : allColumns) {
			staticSystemFields.add(col.getUID());
		}

		for (FieldMeta<?> efMeta : colEfMeta) {
			if (staticSystemFields.contains(efMeta.getUID())) {
				// hier nur dynamische Zuweisungen
				continue;
			}

			// normal (non-reference) field
			if (efMeta.getForeignEntity() == null && efMeta.getUnreferencedForeignEntity() == null) {
				allColumns.add(this.<Object, PK>createFieldMapping(SystemFields.BASE_ALIAS, efMeta));
			}
			// column is ref to foreign table
			else {
				boolean isUidEntity = false;
				EntityMeta<?> sysMeta = E.getByUID(LangUtils.defaultIfNull(efMeta.getForeignEntity(), efMeta.getUnreferencedForeignEntity()));
				if (sysMeta != null) {
					isUidEntity = sysMeta.isUidEntity();
				}
				
				// only an primary key ref to foreign table
				if (efMeta.getJavaClass() == Long.class && efMeta.getDbColumn().toUpperCase().startsWith("INTID_")) {
					// kein join n??tig!
					if (!isIdColumnInList(allColumns, efMeta.getDbColumn()))
						allColumns.add(this.<Object, PK>createFieldIdMapping(SystemFields.BASE_ALIAS, efMeta, isUidEntity));
				} else if (efMeta.getJavaClass() == UID.class && efMeta.getUnreferencedForeignEntityField() == null && efMeta.getForeignEntityField() == null) { 
					// kein join n??tig!
					if (!isIdColumnInList(allColumns, efMeta.getDbColumn()))
						allColumns.add(this.<Object, PK>createFieldIdMapping(SystemFields.BASE_ALIAS, efMeta, isUidEntity));
				}
				// normal case: key ref and 'stringified' ref to foreign table
				else {
					// add 'stringified' ref to column mapping

					// The 'if' is temporary HACK. We MUST get rid of it! (tp)
					if (!eMeta.equals(E.DATASOURCE)) {
						allColumns.add(this.<Object, PK>createRefFieldMapping(efMeta));
					}

					// Also add foreign key
					final String dbIdFieldName = DbUtils.getDbIdFieldName(efMeta, isUidEntity);
					if (!isIdColumnInList(allColumns, dbIdFieldName)) {
						allColumns.add(this.<Object, PK>createFieldIdMapping(SystemFields.BASE_ALIAS, efMeta, isUidEntity));
					}
					// id column is already in allColumns:
					// Replace the id column if the one present is read-only and the current is not read-only
					// This in effect only switched the read-only flag to false.
					else {
						final IColumnToVOMapping<?, ?> col = getColumnFromList(allColumns, dbIdFieldName);
						if (col.isReadonly() && !efMeta.isReadonly()) {
							allColumns.remove(col);
							allColumns.add(this.<Object, PK>createFieldIdMapping(SystemFields.BASE_ALIAS, efMeta, isUidEntity));
						}
					}
				}
			}
		}

		return new ProcessorConfiguration(type, eMeta, allColumns, pkColumn, versionColumn, addSystemColumns);
	}

	public FieldMetaProcessor newFieldMetaProcessor() {
		final Class<IDalVO<UID>> nfm = LangUtils.getGenericClass(NucletFieldMeta.class);
		final Class<? extends IDalVO<UID>> type = (Class<? extends IDalVO<UID>>) nfm;
		final List<IColumnToVOMapping<? extends Object, UID>> allColumns = new ArrayList<IColumnToVOMapping<? extends Object, UID>>();

		final IColumnToVOMapping<UID, UID> idColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, SF.PK_UID, E.ENTITYFIELD.getUID());
		allColumns.add(idColumn);
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CREATEDAT, E.ENTITYFIELD.getUID()));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CREATEDBY, E.ENTITYFIELD.getUID()));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CHANGEDAT, E.ENTITYFIELD.getUID()));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CHANGEDBY, E.ENTITYFIELD.getUID()));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.VERSION, E.ENTITYFIELD.getUID()));
		
		final IColumnToVOMapping<UID, UID> entityIdColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.entity);
		allColumns.add(entityIdColumn);
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.entityfieldgroup, "fieldGroup"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.field, "fieldName"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.dbfield, "dbColumn"));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.foreignentity, "foreignEntity"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.foreignentityfield, "foreignEntityField"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.lookupentity, "lookupEntity"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.lookupentityfield, "lookupEntityField"));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.datatype, "dataType"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.defaultcomponenttype, "defaultComponentType"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.datascale, "scale"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.dataprecision, "precision"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.formatinput, "formatInput"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.formatoutput, "formatOutput"));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.foreigndefault, "defaultForeignId"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.valuedefault, "defaultValue"));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.readonly));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.unique));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.nullable));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.indexed));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.searchable));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.modifiable));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.insertable));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.logbooktracking, "logBookTracking"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.showmnemonic, "showMnemonic"));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.calcfunction, "calcFunction"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.sortationasc, "sortorderASC"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.sortationdesc, "sortorderDESC"));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.localeresourcel, "localeResourceIdForLabel"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.localeresourced, "localeResourceIdForDescription"));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.defaultmandatory, "defaultMandatory"));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.ondeletecascade, "onDeleteCascade"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.order));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYFIELD.calculationscript, "calculationScript"));

		return new FieldMetaProcessor(allColumns, entityIdColumn, idColumn);
	}

	public EntityMetaProcessor newEntityMetaProcessor() {
		final Class<? extends IDalVO<UID>> type = NucletEntityMeta.class;
		final List<IColumnToVOMapping<? extends Object, UID>> allColumns = new ArrayList<IColumnToVOMapping<? extends Object, UID>>();
		final IColumnToVOMapping<UID, UID> idColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, SF.PK_UID, E.ENTITY.getUID());

		/*
		 * Attention: The sequence of initialization is important. 
		 * <p>
		 * As a general rule the last 3 items to set are:
		 * <ol>
		 *   <li>virtualEntity</li>
		 *   <li>entity</li>
		 *   <li>dbEntity</li>
		 * </ol>
		 * </p>
		 */
		allColumns.add(idColumn);
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CREATEDAT, E.ENTITY.getUID()));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CREATEDBY, E.ENTITY.getUID()));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CHANGEDAT, E.ENTITY.getUID()));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CHANGEDBY, E.ENTITY.getUID()));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.VERSION, E.ENTITY.getUID()));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.nuclet));
		
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.systemidprefix, "systemIdPrefix"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.menushortcut, "menuShortcut"));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.editable));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.usessatemodel, "stateModel"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.logbooktracking, "logBookTracking"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.cacheable));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.searchable));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.treerelation, "treeRelation"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.treegroup, "treeGroup"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.importexport, "importExport"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.fieldvalueentity, "fieldValueEntity"));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.accelerator));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.acceleratormodifier, "acceleratorModifier"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.fieldsforequality, "fieldsForEquality"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.resource));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.nuclosResource));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.localeresourcel, "localeResourceIdForLabel"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.localeresourcem, "localeResourceIdForMenuPath"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.localeresourced, "localeResourceIdForDescription"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.localeresourcetw, "localeResourceIdForTreeView"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.localeresourcett, "localeResourceIdForTreeViewDescription"));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.documentPath));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.reportFilename));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.idFactory));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.readDelegate));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.rowcolorscript, "rowColorScript"));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.virtualentity, "virtualEntity"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.entity, "entityName"));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITY.dbtable, "dbTable"));

		return new EntityMetaProcessor(allColumns, idColumn);
	}
	
	public EntityLafParameterProcessor newEntityLafParameterProcessor() {
		final Class<? extends IDalVO<UID>> type = EntityLafParameterVO.class;
		final List<IColumnToVOMapping<? extends Object, UID>> allColumns = new ArrayList<IColumnToVOMapping<? extends Object, UID>>();
		final IColumnToVOMapping<UID, UID> idColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, SF.PK_UID, E.ENTITYLAFPARAMETER.getUID());

		allColumns.add(idColumn);
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CREATEDAT, E.ENTITYLAFPARAMETER.getUID()));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CREATEDBY, E.ENTITYLAFPARAMETER.getUID()));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CHANGEDAT, E.ENTITYLAFPARAMETER.getUID()));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CHANGEDBY, E.ENTITYLAFPARAMETER.getUID()));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.VERSION, E.ENTITYLAFPARAMETER.getUID()));

//		allColumns.add(createBeanRefMapping("INTID_T_MD_ENTITY", "T_MD_ENTITY", "ENTITY", JoinType.LEFT, type, "STRENTITY", "STRVALUE_T_MD_ENTITY", "entity", DT_STRING));

		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYLAFPARAMETER.entity));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYLAFPARAMETER.parameter));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.ENTITYLAFPARAMETER.value));
		
		return new EntityLafParameterProcessor(allColumns, idColumn);
	}

	public EOGenericObjectProcessor newEOGenericObjectProcessor() {
		final Class<? extends IDalVO<Long>> type = EOGenericObjectVO.class;
		final List<IColumnToVOMapping<? extends Object, Long>> allColumns = new ArrayList<IColumnToVOMapping<? extends Object, Long>>();
		final IColumnToVOMapping<Long, Long> idColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, SF.PK_ID, E.GENERICOBJECT.getUID());

		allColumns.add(idColumn);
		final IColumnToVOMapping<UID, Long> moduleColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, E.GENERICOBJECT.module, "entityUID");
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CREATEDAT, E.GENERICOBJECT.getUID()));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CREATEDBY, E.GENERICOBJECT.getUID()));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CHANGEDAT, E.GENERICOBJECT.getUID()));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CHANGEDBY, E.GENERICOBJECT.getUID()));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.VERSION, E.GENERICOBJECT.getUID()));
		allColumns.add(moduleColumn);

		return new EOGenericObjectProcessor(allColumns, moduleColumn, idColumn);
	}

	public WorkspaceProcessor newWorkspaceProcessor() {
		final Class<? extends IDalVO<UID>> type = WorkspaceVO.class;
		final List<IColumnToVOMapping<? extends Object, UID>> allColumns = new ArrayList<IColumnToVOMapping<? extends Object, UID>>();
		final IColumnToVOMapping<UID, UID> idColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, SF.PK_UID, E.WORKSPACE.getUID());
		allColumns.add(idColumn);
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CREATEDAT, E.WORKSPACE.getUID()));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CREATEDBY, E.WORKSPACE.getUID()));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CHANGEDAT, E.WORKSPACE.getUID()));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.CHANGEDBY, E.WORKSPACE.getUID()));
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, SF.VERSION, E.WORKSPACE.getUID()));

		final IColumnToVOMapping<String, UID> nameColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, E.WORKSPACE.name);
		allColumns.add(nameColumn);
		allColumns.add(createBeanMapping(SystemFields.BASE_ALIAS, type, E.WORKSPACE.clbworkspace));
		final IColumnToVOMapping<UID, UID> userColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, E.WORKSPACE.user);
		allColumns.add(userColumn);
		final IColumnToVOMapping<UID, UID> assignedColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, E.WORKSPACE.assignedWorkspace);
		allColumns.add(assignedColumn);
		final IColumnToVOMapping<UID, UID> nucletColumn = createBeanMapping(SystemFields.BASE_ALIAS, type, E.WORKSPACE.nuclet);
		allColumns.add(nucletColumn);

		return new WorkspaceProcessor(allColumns, idColumn, userColumn, nameColumn, nucletColumn, assignedColumn);
	}

	public <PK> DynamicEntityObjectProcessor<PK> newDynamicEntityObjectProcessor(EntityMeta<?> eMeta, Collection<FieldMeta<?>> colEfMeta) {
		final Class<IDalVO<PK>> eov = LangUtils.getGenericClass(EntityObjectVO.class);
		final Class<? extends IDalVO<PK>> type = (Class<? extends IDalVO<PK>>) eov;
		final ProcessorConfiguration<PK> config = newProcessorConfiguration(type, eMeta, colEfMeta, false);
		
		final DynamicEntityObjectProcessor<PK> result = new DynamicEntityObjectProcessor<PK>(config);
		
		// HACK: force spring, as @Autowired on EntityObjectProcessor does not work (tp)
		result.setDataBaseHelper(dataBaseHelper);
		result.setTableAliasSingleton(tableAliasSingleton);
		result.setSessionUtils(utils);
		
		return result;
	}

	public <PK> ChartEntityObjectProcessor<PK> newChartEntityObjectProcessor(EntityMeta<?> eMeta, Collection<FieldMeta<?>> colEfMeta) {
		final Class<IDalVO<PK>> eov = LangUtils.getGenericClass(EntityObjectVO.class);
		final Class<? extends IDalVO<PK>> type = (Class<? extends IDalVO<PK>>) eov;
		final ProcessorConfiguration<PK> config = newProcessorConfiguration(type, eMeta, colEfMeta, false);
		
		final ChartEntityObjectProcessor<PK> result = new ChartEntityObjectProcessor<PK>(config);
		
		// HACK: force spring, as @Autowired on EntityObjectProcessor does not work (tp)
		result.setDataBaseHelper(dataBaseHelper);
		result.setTableAliasSingleton(tableAliasSingleton);
		result.setSessionUtils(utils);
		
		return result;
	}

	public <PK> ImportObjectProcessor<PK> newImportObjectProcessor(EntityMeta<?> eMeta, Collection<FieldMeta<?>> colEfMeta, ImportStructure structure) {
		final Class<IDalVO<PK>> eov = LangUtils.getGenericClass(EntityObjectVO.class);
		final Class<? extends IDalVO<PK>> type = (Class<? extends IDalVO<PK>>) eov;
		final ProcessorConfiguration<PK> config = newProcessorConfiguration(type, eMeta, colEfMeta, true);
		return new ImportObjectProcessor<PK>(config, structure);
	}

	public <PK> void addToColumns(JdbcEntityObjectProcessor<PK> processor, FieldMeta<?> field) {
		final IColumnToVOMapping<?, PK> mapping;
		final PivotInfo pinfo = field.getPivotInfo();
		final IMetaProvider mdProv = MetaProvider.getInstance();
		final EntityMeta<?> mdEnitiy = mdProv.getEntity(field.getEntity());

		final String alias;
		if (mdEnitiy.equals(processor.getMetaData())) {
			alias = SystemFields.BASE_ALIAS;
		}
		// The join table alias must be unique in the SQL
		else if (pinfo != null) {
			alias = pinfo.getPivotTableAlias(field.getFieldName());
		}
		else {
			alias = mdEnitiy.getEntityName();
		}

		if (pinfo == null) {
			mapping = createFieldMapping(alias, field);
		}
		else {
			final FieldMeta<?> vField = mdProv.getEntityField(pinfo.getValueField());
			final IColumnToVOMapping<?, PK> mapping2 = createPivotJoinMapping(alias, vField, pinfo.getSubform());
			processor.addToColumns(mapping2);
			// Also add the key field so that the gui result table could find the pivot
			final FieldMeta<?> kField = mdProv.getEntityField(pinfo.getKeyField());
			mapping = createPivotJoinMapping(alias, kField, pinfo.getSubform());
		}
		processor.addToColumns(mapping);
	}

}

