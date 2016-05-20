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
package org.nuclos.server.dblayer;

import static org.nuclos.server.dblayer.structure.DbColumnType.DbGenericType.DATETIME;
import static org.nuclos.server.dblayer.structure.DbColumnType.DbGenericType.NUMERIC;
import static org.nuclos.server.dblayer.structure.DbColumnType.DbGenericType.VARCHAR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.nuclos.common.EntityMeta;
import org.nuclos.common.FieldMeta;
import org.nuclos.common.IRigidMetaProvider;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.RigidUtils;
import org.nuclos.common.SF;
import org.nuclos.common.UID;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dblayer.IFieldUIDRef;
import org.nuclos.common2.ForeignEntityFieldUIDParser;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.database.SpringDataBaseHelper;
import org.nuclos.server.dblayer.structure.DbArtifact;
import org.nuclos.server.dblayer.structure.DbColumn;
import org.nuclos.server.dblayer.structure.DbColumnType;
import org.nuclos.server.dblayer.structure.DbConstraint.DbForeignKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbLogicalUniqueConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbPrimaryKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbUniqueConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbUnreferencedForeignKeyConstraint;
import org.nuclos.server.dblayer.structure.DbIndex;
import org.nuclos.server.dblayer.structure.DbNullable;
import org.nuclos.server.dblayer.structure.DbSimpleView;
import org.nuclos.server.dblayer.structure.DbSimpleView.DbSimpleViewColumn;
import org.nuclos.server.dblayer.structure.DbTable;
import org.nuclos.server.dblayer.structure.DbTableArtifact;

public class MetaDbHelper {

	private static Map<String, DbColumnType> SYSTEM_COLUMNS = new LinkedHashMap<String, DbColumnType>();

	static DbColumnType ID_COLUMN_TYPE = new DbColumnType(NUMERIC, 20, 0);
	static DbColumnType UID_COLUMN_TYPE = new DbColumnType(VARCHAR, SF.UID_SCALE);

	static {
		SYSTEM_COLUMNS.put("DATCREATED", new DbColumnType(DATETIME));
		SYSTEM_COLUMNS.put("STRCREATED", new DbColumnType(VARCHAR, 30));
		SYSTEM_COLUMNS.put("DATCHANGED", new DbColumnType(DATETIME));
		SYSTEM_COLUMNS.put("STRCHANGED", new DbColumnType(VARCHAR, 30));
		SYSTEM_COLUMNS.put("INTVERSION", new DbColumnType(NUMERIC, 9, 0));
	}

	private final MetaDbProvider provider;
	private final DbAccess dbAccess;
	private final boolean viewsEnabled;
	
	public MetaDbHelper(IRigidMetaProvider provider) {
		this(new MetaDbProvider(provider));
	}

	public MetaDbHelper(MetaDbProvider provider) {
		this(SpringDataBaseHelper.getInstance().getDbAccess(), provider);
	}
	
	public MetaDbHelper(DbAccess dbAccess, IRigidMetaProvider provider) {
		this(dbAccess, new MetaDbProvider(provider), true);
	}

	public MetaDbHelper(DbAccess dbAccess, MetaDbProvider provider) {
		this(dbAccess, provider, true);
	}
	
	public MetaDbHelper(DbAccess dbAccess, MetaDbProvider provider, boolean viewsEnabled) {
		this.dbAccess = dbAccess;
		this.provider = provider;
		this.viewsEnabled = viewsEnabled;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("access=").append(dbAccess);
		result.append(", mdProv=").append(provider);
		result.append("]");
		return result.toString();
	}
	
	public Map<String, DbTable> getSchema() {
		return getSchema(null);
	}

	public Map<String, DbTable> getSchema(Predicate<DbArtifact> predicate) {
		Map<String, DbTable> tables = new LinkedHashMap<String, DbTable>();
		for (MetaDbEntityWrapper entityMeta : provider.getAllEntities()) {
			if (!entityMeta.isDynamic()) {
				DbTable dbTable = getDbTable(entityMeta);
				if (predicate != null) {
					if (predicate.evaluate(dbTable)) {
						Iterator<DbTableArtifact> it = dbTable.getTableArtifacts().iterator();
						while (it.hasNext()) {
							DbTableArtifact dba = it.next();
							if (!predicate.evaluate(dba)) {
								it.remove();
							}
						}
						
						tables.put(dbTable.getTableName(), dbTable);
					}
				} else {
					tables.put(dbTable.getTableName(), dbTable);
				}
			}
		}
		return tables;
	}
	
	public DbTable getDbTable(EntityMeta<?> entityMeta) {
		return getDbTable(new MetaDbEntityWrapper(entityMeta));
	}

	public DbTable getDbTable(MetaDbEntityWrapper entityMeta) {
		final String tableName = getTableName(entityMeta);
		final String dbTableName = generateDbName(tableName);

		final Map<String, DbColumn> dbColumns = new LinkedHashMap<String, DbColumn>();
		final Map<UID, DbColumn> dbColumnsByField = new HashMap<UID, DbColumn>();
		final Set<DbForeignKeyConstraint> fkConstraints = new LinkedHashSet<DbForeignKeyConstraint>();
		final Set<DbUnreferencedForeignKeyConstraint> ufkConstraints = new LinkedHashSet<DbUnreferencedForeignKeyConstraint>();
		final Map<String, DbIndex> indexes = new LinkedHashMap<String, DbIndex>();
		final List<DbSimpleViewColumn> dbExtraViewColumns = new ArrayList<DbSimpleViewColumn>();
		final SortedMap<String, String> simpleUniqueColumns = new TreeMap<String, String>();

		// Dummy placeholder (LinkedHashMap guarantees that the order)
		dbColumns.put(entityMeta.isUidEntity()?"STRUID":"INTID", null);
 
		final Collection<MetaDbFieldWrapper> fieldMap = provider.getAllEntityFieldsByEntity(entityMeta.getUID());
		for (MetaDbFieldWrapper fieldMeta : fieldMap) {
			DbColumnType columnType = createDbColumnType(fieldMeta.getDataType(), fieldMeta.getScale(), fieldMeta.getPrecision());
			boolean isCalculated = (fieldMeta.getCalcFunction() != null);
			boolean isForeignReference = (fieldMeta.getForeignEntity() != null);

			DbColumn dbColumn = null;
			DbSimpleViewColumn dbExtraViewColumn = null;
			if (viewsEnabled && isCalculated) {
				dbExtraViewColumn = new DbSimpleViewColumn(
					generateDbName(StringUtils.upperCase(fieldMeta.getDbColumn())),
					columnType, fieldMeta.getCalcFunction(), "INTID");
			} else if (isForeignReference) {
				final MetaDbEntityWrapper foreignEntity = provider.getEntity(fieldMeta.getForeignEntity());
				if (foreignEntity == null) {
					throw new IllegalArgumentException("Entity " + entityMeta.getEntityName() + ": Foreign entity " + fieldMeta.getForeignEntity() + " does not exist");
				}
				
				final String dbColumnName = generateDbName(DbUtils.getDbIdFieldName(fieldMeta, foreignEntity.isUidEntity()));
				dbColumn = new DbColumn(dbTableName, dbColumnName,
						foreignEntity.isUidEntity()?UID_COLUMN_TYPE:ID_COLUMN_TYPE,
					DbNullable.of(Boolean.TRUE.equals(fieldMeta.isNullable())), fieldMeta.getDefaultMandatory());				

				final String dbForeignTableName = generateDbName(getTableName(foreignEntity));

				final boolean onDeleteCascade = fieldMeta.isOnDeleteCascade();

				// TODO: was XR_<ID> but this does not work consistently for system entities => ...
				final DbForeignKeyConstraint fkConstraint = new DbForeignKeyConstraint(dbTableName,
					generateDbName("XR_" + tableName, dbColumnName),
					Arrays.asList(dbColumnName), dbForeignTableName, null, Arrays.asList(foreignEntity.isUidEntity()?"STRUID":"INTID"), onDeleteCascade);

				// @TODO GOREF: delete if not needed
				if (foreignEntity.getEntityName().equals("nuclos_generalsearch")) {
//					log.warn("Entity field " + entityMeta.getEntity() + "." + fieldMeta.getField() + " references nuclos_genericobject or nuclos_generalsearch");
					continue;
				}

				if (!fieldMeta.isReadonly() && foreignEntity.getVirtualEntity() == null)
					fkConstraints.add(fkConstraint);

				final boolean isJoin = !StringUtils.upperCase(fieldMeta.getDbColumn()).startsWith("INTID_") &&
						!StringUtils.upperCase(fieldMeta.getDbColumn()).startsWith("STRUID_");
				if (viewsEnabled && isJoin) {
					dbExtraViewColumn = new DbSimpleViewColumn(
						generateDbName(StringUtils.upperCase(fieldMeta.getDbColumn())),
						columnType, fkConstraint, getViewPatternForField(fieldMeta, provider));
				} else if (fieldMeta.getForeignEntityField() != null) {
//					log.info("Join for entity field " + entityMeta.getEntity() + "." + fieldMeta.getField() + " skipped");
				}
			} else {
				final String dbColumnName = StringUtils.upperCase(fieldMeta.getDbColumn());
				dbColumn = new DbColumn(dbTableName, dbColumnName,
					columnType, DbNullable.of(Boolean.TRUE.equals(fieldMeta.isNullable())), fieldMeta.getDefaultMandatory());
				
				final boolean isUnreferencedForeignReference = (fieldMeta.getUnreferencedForeignEntity() != null);
				if (isUnreferencedForeignReference) {
					final MetaDbEntityWrapper unreferencedForeignEntity = provider.getEntity(fieldMeta.getUnreferencedForeignEntity());
					final String dbUnreferencedForeignTableName = generateDbName(getTableName(unreferencedForeignEntity));
					
					final DbUnreferencedForeignKeyConstraint ufkConstraint = new DbUnreferencedForeignKeyConstraint(dbTableName,
							generateDbName("XUR_" + tableName, dbColumnName),
							Arrays.asList(dbColumnName), dbUnreferencedForeignTableName, null, Arrays.asList(unreferencedForeignEntity.isUidEntity()?"STRUID":"INTID"), false);
					ufkConstraints.add(ufkConstraint);
				}
			}

			if (dbColumn != null) {
				if (Boolean.TRUE.equals(fieldMeta.isUnique())) {
					simpleUniqueColumns.put(fieldMeta.getFieldName(), dbColumn.getColumnName());
				}
				if (Boolean.TRUE.equals(fieldMeta.isIndexed())) {
					final DbIndex index = new DbIndex(dbTableName,
						generateDbName("XIE_" + tableName, dbColumn.getColumnName()),
						Arrays.asList(dbColumn.getColumnName()));
					indexes.put(dbColumn.getColumnName(), index);
				}

				dbColumns.put(dbColumn.getColumnName(), dbColumn);
				dbColumnsByField.put(fieldMeta.getUID(), dbColumn);
			}

			if (dbExtraViewColumn != null) {
				dbExtraViewColumns.add(dbExtraViewColumn);
			}
		}

		// Overwrite columns if necessary
		// TODO: check that the types matches if a user mapping exists
		for (Map.Entry<String, DbColumnType> e : SYSTEM_COLUMNS.entrySet()) {
			final String dbColumnName = e.getKey();
			dbColumns.put(dbColumnName, new DbColumn(dbTableName, dbColumnName, e.getValue(), DbNullable.NOT_NULL, null));
		}
		if (entityMeta.isUidEntity()) {
			dbColumns.put("STRUID", new DbColumn(dbTableName, "STRUID", UID_COLUMN_TYPE, DbNullable.NOT_NULL, null));
		} else {
			dbColumns.put("INTID", new DbColumn(dbTableName, "INTID", ID_COLUMN_TYPE, DbNullable.NOT_NULL, null));
		}

		List<DbTableArtifact> tableArtifacts = new ArrayList<DbTableArtifact>();
		if (entityMeta.getVirtualEntity() == null) {
			// Columns
			tableArtifacts.addAll(dbColumns.values());
			// Primary Key
			tableArtifacts.add(new DbPrimaryKeyConstraint(dbTableName, generateDbName("PK_" + tableName), Arrays.asList(entityMeta.isUidEntity()?"STRUID":"INTID")));
			// Foreign Keys
			tableArtifacts.addAll(fkConstraints);
			// Unique Columns
			final List<List<String>> uniqueColumnsList = new ArrayList<List<String>>();
			// - System entities support multiple unique combinations
			final UID[][] uniqueFieldCombinations = entityMeta.getUniqueFieldCombinations();
			if (uniqueFieldCombinations != null) {
				for (UID[] uniqueFields : uniqueFieldCombinations) {
					uniqueColumnsList.add(mapFieldList(uniqueFields, dbColumnsByField));
				}
			}

			// - Legacy behaviour: generate one combined unique key for all flagged columns
			if (uniqueColumnsList.isEmpty() && simpleUniqueColumns.size() > 0) {
				uniqueColumnsList.add(new ArrayList<String>(simpleUniqueColumns.values()));
			}
			// - Generate unique constraints (and remove index for the same columns)
			for (List<String> uniqueColumns : uniqueColumnsList) {
				tableArtifacts.add(new DbUniqueConstraint(dbTableName, generateDbName("XAK_" + tableName, uniqueColumns), uniqueColumns));
				if (uniqueColumns.size() == 1) {
					indexes.remove(uniqueColumns.get(0));
				}
			}

			// Indexes
			tableArtifacts.addAll(indexes.values());
		}
		
		// Unreferenced Foreign Keys are not materialized in db, but we need them for validation and documentation
		tableArtifacts.addAll(ufkConstraints);
		
		// Logical Unique Constraints are not materialized in db, but we need them for validation and documentation
		final List<List<String>> logicalUniqueColumnsList = new ArrayList<List<String>>();
		final UID[][] logicalUniqueFieldCombinations = entityMeta.getLogicalUniqueFieldCombinations();
		if (logicalUniqueFieldCombinations != null) {
			for (UID[] uniqueFields : logicalUniqueFieldCombinations) {
				logicalUniqueColumnsList.add(mapFieldList(uniqueFields, dbColumnsByField));
			}
		}
		for (List<String> uniqueColumns : logicalUniqueColumnsList) {
			tableArtifacts.add(new DbLogicalUniqueConstraint(dbTableName, generateDbName("XLO_" + tableName, uniqueColumns), uniqueColumns));
		}

		// View
		if (viewsEnabled) {
			String viewName = getViewName(entityMeta);
			if (viewName != null) {
	
				boolean createView = false;
				if (!provider.isNuclosEntity(entityMeta.getUID()) && 
						!new DbObjectHelper(dbAccess).hasUserdefinedEntityView(entityMeta, provider)) {
					createView = true;
				}
				
				if (createView) {
					final List<DbSimpleViewColumn> dbViewColumns = RigidUtils.transform(dbColumns.values(), new Transformer<DbColumn, DbSimpleViewColumn>() {
						@Override
						public DbSimpleViewColumn transform(DbColumn c) { return new DbSimpleViewColumn(c.getColumnName()); }
					});
					dbViewColumns.addAll(dbExtraViewColumns);
	
					tableArtifacts.add(new DbSimpleView(dbTableName, generateDbName(viewName), dbViewColumns));
				}
			}
		}

		final DbTable dbTable = new DbTable(dbTableName, tableArtifacts, entityMeta.getVirtualEntity() != null);
		return dbTable;
	}
	
	public static String getDbRefColumn(EntityMeta entity, FieldMeta field) {
		return getDbRefColumn(new MetaDbEntityWrapper(entity), new MetaDbFieldWrapper(field));
	}

	public static String getDbRefColumn(MetaDbEntityWrapper entity, MetaDbFieldWrapper field) {
		if (field.getForeignEntity() == null) {
			throw new IllegalArgumentException();
		}
		return getDbRefColumn(field.getDbColumn(), entity.isUidEntity());
	}
	
	public static String getDbRefColumn(String column, boolean refsToUidEntity) {
		if (column == null) {
			throw new IllegalArgumentException();
		}
		final String dbColumn = column.toUpperCase();
		final String result;
		if (dbColumn.startsWith("STRVALUE_") || dbColumn.startsWith("INTVALUE_") || dbColumn.startsWith("OBJVALUE_")) {
			result = refsToUidEntity?"STRUID_":"INTID_" + dbColumn.substring(9);
		} else if (dbColumn.startsWith("STRUID_")) {
			result = refsToUidEntity?"STRUID_":"INTID_" + dbColumn.substring(7);
		} else if (dbColumn.startsWith("INTID_")) {
			result = refsToUidEntity?"STRUID_":"INTID_" + dbColumn.substring(6);
		}
		else {
			throw new IllegalArgumentException(String.format("getDbRefColumn(\"%s\", %s)", column, refsToUidEntity));
		}
		return result;
	}

	/**
	 * @deprecated Stringified refs are now dereferenced by table joins. Hence the whole method is
	 * 		obsolete. (tp)
	 */
	private static <F extends FieldMeta> List<?> getViewPatternForField(MetaDbFieldWrapper fieldMeta, MetaDbProvider provider2) {
		List<Object> result = new ArrayList<Object>();
		UID refEntityUID = fieldMeta.getForeignEntity() != null ? fieldMeta.getForeignEntity() : fieldMeta.getLookupEntity();

		if (refEntityUID != null) {
			boolean isJoin = !StringUtils.upperCase(fieldMeta.getDbColumn()).startsWith("INTID_") && 
					!StringUtils.upperCase(fieldMeta.getDbColumn()).startsWith("STRUID_");
			if (isJoin) {

				// New:
				UID nameFieldUID = null;
				for (MetaDbFieldWrapper fWrapper : provider2.getAllEntityFieldsByEntity(refEntityUID)) {
					if ("name".equals(fWrapper.getFieldName())) {
						nameFieldUID = fWrapper.getUID();
					}
				}
				for (IFieldUIDRef r: new ForeignEntityFieldUIDParser(fieldMeta.getForeignEntityField(), fieldMeta.getLookupEntityField(), nameFieldUID)) {
					if (r.isUID()) {
						String foreignDbColumn = null;
						try {
							foreignDbColumn = provider2.getEntityField(r.getUID()).getDbColumn();
						} catch (CommonFatalException e) {
							if (DbUtils.isDbIdField(fieldMeta.getDbColumn())) {
								foreignDbColumn = fieldMeta.getDbColumn().toUpperCase().startsWith("INTID")?"INTID":"STRUID)";
							} else {
								throw e;
							}
						}
						result.add(DbIdent.makeIdent(foreignDbColumn));
					}
				}
				
				// Old stuff:
//				EntityMeta<?> foreignEntity = provider2.getEntity(fieldMeta.getForeignEntity() != null ? fieldMeta.getForeignEntity() : fieldMeta.getLookupEntity());
//
//				String foreignFieldName = null;
//				if (fieldMeta.getForeignEntity() != null)
//					foreignFieldName = fieldMeta.getForeignEntityField();
//				else if (fieldMeta.getLookupEntity() != null)
//					foreignFieldName = fieldMeta.getLookupEntityField();
//
//				if (foreignFieldName == null || foreignFieldName.isEmpty()) {
//					for (FieldMeta field : provider2.getAllEntityFieldsByEntity(foreignEntity.getUID()).values()) {
//						if ("name".equalsIgnoreCase(field.getFieldName())) {
//							foreignFieldName = field.getUID().getUID();
//						}
//					}
//					if (foreignFieldName == null || foreignFieldName.isEmpty()) {
//						throw new IllegalArgumentException("Foreign Field not found " + fieldMeta);
//					}
//				}
//				
//				String[] parts = StringUtils.splitWithMatches(PARAM_PATTERN, 1, foreignFieldName);
//				if (parts.length == 1) {
//					// no parameter pattern, so the whole text is interpreted as field name
//					parts = new String[] {"", parts[0]};
//				}
//				for (int i = 0; i < parts.length; i++) {
//					if (i % 2 == 0 && !parts[i].isEmpty()) {
//						// (Non-empty) text fragment
//						result.add(parts[i]);
//					} else if (i % 2 == 1) {
//						// Parameter pattern -> field name
//						String foreignDbColumn = null;
//						try {
//							foreignDbColumn = provider2.getEntityField(new UID(parts[i])).getDbColumn();
//						} catch (CommonFatalException e) {
//							if (DalUtils.isDbIdField(fieldMeta.getDbColumn())) {
//								foreignDbColumn = fieldMeta.getDbColumn().toUpperCase().startsWith("INTID")?"INTID":"STRUID)";
//							} else {
//								throw e;
//							}
//						}
//						result.add(DbIdent.makeIdent(foreignDbColumn));
//					}
//				}
			} else {
				result.add(DbIdent.makeIdent(fieldMeta.getDbColumn()));
			}
		} else {
			result.add(DbIdent.makeIdent(fieldMeta.getDbColumn()));
		}

		return result;
	}

	private static List<String> mapFieldList(UID[] fields, Map<UID, DbColumn> dbColumnsByField) {
		List<String> list = new ArrayList<String>();
		for (UID field : fields) {
			DbColumn dbColumn = dbColumnsByField.get(field);
			if (dbColumn == null)
				throw new IllegalArgumentException("Field " + field + " does not exist");
			list.add(dbColumn.getColumnName());
		}
		return list;
	}

	private String generateDbName(String name, String...affixes) {
		return dbAccess.generateName(name, affixes);
	}

	private String generateDbName(String name, List<String> affixes) {
		return dbAccess.generateName(name, affixes.toArray(new String[affixes.size()]));
	}
	
	public static String getTableName(EntityMeta<?> entityMeta) {
		return getTableName(entityMeta.getVirtualEntity(), entityMeta.getDbTable());
	}

	public static String getTableName(MetaDbEntityWrapper entityMeta) {
		return getTableName(entityMeta.getVirtualEntity(), entityMeta.getDbTable());
	}
	
	public static String getTableName(String virtualEntity, String dbTable) {
		if (virtualEntity != null) {
			return virtualEntity;
		}
		String tableName = StringUtils.upperCase(dbTable);
		if (tableName.startsWith("V_")) {
			tableName = "T_" + tableName.substring(2);
		}
		else if (tableName.startsWith("T_")){
			// do nothing
		}
		// could this really happen? (tp)
		else {
			assert false : tableName;
		}
		return tableName;
	}

	/**
	 * @deprecated Consider that the auto-generated views of nuclos are deprecated.
	 * 		Hence there should be no need to use this method. (tp)
	 */
	public static String getViewName(MetaDbEntityWrapper entityMeta) {
		return getViewName(entityMeta.getDbTable());
	}
	
	/**
	 * @deprecated Consider that the auto-generated views of nuclos are deprecated.
	 * 		Hence there should be no need to use this method. (tp)
	 */
	public static String getViewName(EntityMeta<?> entityMeta) {
		return getViewName(entityMeta.getDbTable());
	}
	
	/**
	 * @deprecated Consider that the auto-generated views of nuclos are deprecated.
	 * 		Hence there should be no need to use this method. (tp)
	 */
	public static String getViewName(String dbTable) {
		String tableName = StringUtils.upperCase(dbTable);
		if (tableName.startsWith("V_")) {
			return tableName;
		}
		else if (tableName.startsWith("T_")) {
			return "V_" + tableName.substring(2);
		}
		return null;
	}

	public static DbColumnType createDbColumnType(FieldMeta fieldMeta) {
		return createDbColumnType(fieldMeta.getDataType(), fieldMeta.getScale(), fieldMeta.getPrecision());
	}

	private static DbColumnType createDbColumnType(String javaClass, Integer oldScale, Integer oldPrecision) {
		try {
			return DbUtils.getDbColumnType(Class.forName(javaClass), oldScale, oldPrecision);
		} catch(ClassNotFoundException e) {
			throw new NuclosFatalException(e);
		}
	}
//
//	/**
//	 * @deprecated Please use {@link org.nuclos.server.dblayer.util.ForeignEntityFieldParser}. (tp)
//	 */
//	private static final Pattern PARAM_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
}

