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
package org.nuclos.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.exception.CommonFatalException;

/**
 * @see org.nuclos.common.dal.vo.SystemFields
 */
public abstract class SF<T> implements DbField<T>, SFConstants{
	
	private static final UID UID_PROCESS = new UID("ui9l");
	private static final UID UID_STATE = new UID("U89N");
	
	public static final Integer UID_SCALE = 64;
	
	public static final SF<UID> PK_UID = new SF<UID>(0, "primaryKey", "STRUID", false, UID.class, UID_SCALE) {@Override FieldMeta<?> createStaticMeta() {return createPrimaryKey();}};
	public static final SF<Long> PK_ID = new SF<Long>(0, "primaryKey", "INTID", false, Long.class, 20) {@Override FieldMeta<?> createStaticMeta() {return createPrimaryKey();}};
	
	public static final SFValueable<InternalTimestamp> CREATEDAT = new SFValueable<InternalTimestamp>(1, "createdAt", "DATCREATED", false, InternalTimestamp.class, null) {@Override FieldMeta<?> createStaticMeta() {return createCreatedAt();}};
	public static final SFValueable<String> CREATEDBY = new SFValueable<String>(2, "createdBy", "STRCREATED", false, String.class, 255) {@Override FieldMeta<?> createStaticMeta() {return createCreatedBy();}};
	public static final SFValueable<InternalTimestamp> CHANGEDAT = new SFValueable<InternalTimestamp>(3, "changedAt", "DATCHANGED", false, InternalTimestamp.class, null) {@Override FieldMeta<?> createStaticMeta() {return createChangedAt();}};
	public static final SFValueable<String> CHANGEDBY = new SFValueable<String>(4, "changedBy", "STRCHANGED", false, String.class, 255) {@Override FieldMeta<?> createStaticMeta() {return createChangedBy();}};
	public static final SFValueable<Integer> VERSION = new SFValueable<Integer>(5, "version", "INTVERSION", false, Integer.class, 9) {@Override FieldMeta<?> createStaticMeta() {return createVersion();}};
	public static final SFValueable<String> STATE = new SFValueable<String>(6, "nuclosState", "STRVALUE_NUCLOSSTATE", true, String.class, 255) {@Override FieldMeta<?> createStaticMeta() {return createState();}};
	public static final SFValueable<Integer> STATENUMBER = new SFValueable<Integer>(7, "nuclosStateNumber", "INTVALUE_NUCLOSSTATE", true, Integer.class, 3) {@Override FieldMeta<?> createStaticMeta() {return createStateNumber();}};
	public static final SFValueable<NuclosImage> STATEICON = new SFValueable<NuclosImage>(8, "nuclosStateIcon", "OBJVALUE_NUCLOSSTATE", false, NuclosImage.class, 255) {@Override FieldMeta<?> createStaticMeta() {return createStateIcon();}};
	public static final SFValueable<String> SYSTEMIDENTIFIER = new SFValueable<String>(9, "nuclosSystemId", "STRNUCLOSSYSTEMID", false, String.class, 255) {@Override FieldMeta<?> createStaticMeta() {return createSystemIdentifier();}};
	public static final SFValueable<String> PROCESS = new SFValueable<String>(10, "nuclosProcess", "STRVALUE_NUCLOSPROCESS", false, String.class, 255) {@Override FieldMeta<?> createStaticMeta() {return createProcess();}};
	public static final SFValueable<String> ORIGIN = new SFValueable<String>(11, "nuclosOrigin", "STRNUCLOSORIGIN", false, String.class, 255) {@Override FieldMeta<?> createStaticMeta() {return createOrigin();}};
	public static final SFValueable<Boolean> LOGICALDELETED = new SFValueable<Boolean>(12, "nuclosDeleted", "BLNNUCLOSDELETED", false, Boolean.class, null) {@Override FieldMeta<?> createStaticMeta() {return createLogicalDeleted();}};
	
	public static final SF<UID> STATE_UID = new SF<UID>(6, "nuclosState", "STRUID_NUCLOSSTATE", true, UID.class, UID_SCALE) {@Override FieldMeta<?> createStaticMeta() {return createState();}};
	public static final SF<UID> PROCESS_UID = new SF<UID>(10, "nuclosProcess", "STRUID_NUCLOSPROCESS", false, UID.class, UID_SCALE) {@Override FieldMeta<?> createStaticMeta() {return createProcess();}};
	
	private static Collection<SF<?>> allFields = null;
	
	private final Integer postfix;

	private final String field;
	
	private final String dbField;
	
	private final MetaDataCache metaCache;

	private final boolean forceValueSearch;
	
	private final Class<T> cls;
	
	private final Integer scale;

	SF(Integer postfix, String field, String dbField, boolean forceValueSearch, Class<T> cls, Integer scale) {
		this.postfix = postfix;
		this.field = field;
		this.dbField = dbField;
		this.forceValueSearch = forceValueSearch;
		this.cls = cls;
		this.scale = scale;
		this.metaCache = new MetaDataCache(this);
	}

	public FieldMeta<T> getMetaData(EntityMeta<?> entityMeta) {
		return (FieldMeta<T>) getMetaData(entityMeta.getUID());
	}
	
	public FieldMeta<?> getMetaData(UID entityUID) {
		return metaCache.getMetaData(entityUID);
	}
	
	public UID getUID(EntityMeta<?> entityMeta) {
		return getUID(entityMeta.getUID());
	}
	
	public UID getUID(UID entityUID) {
		return metaCache.getMetaData(entityUID).getUID();
	}

	public String getFieldName() {
		return field;
	}
	
	public String getDbColumn() {
		return dbField;
	}

	public boolean isForceValueSearch() {
		return forceValueSearch;
	}
	
	public Class<T> getJavaClass() {
		return cls;
	}

	public static Collection<SF<?>> getAllFields() {
		if (allFields == null) {
			allFields = new ArrayList<SF<?>>();
			Field[] fl = SF.class.getDeclaredFields();
			for (Field f : fl) {
				try {
					f.setAccessible(true);
					Object o = f.get(null);
					if (o instanceof SF<?>) {
						allFields.add((SF<?>) f.get(null));
					}
				} catch (NullPointerException e) {
					// ignore
				} catch (Exception e) {
					throw new CommonFatalException(e);
				} 
			}
		}
		return allFields;
	}
	
	public static boolean isEOFieldWithForceValueSearch(UID entity, UID field) {
		if (entity == null) {
			throw new IllegalArgumentException("entity must not be null");
		}
		for (SF<?> eoField: getAllFields()) {
			if (eoField.getUID(entity).getString().equals(field)) {
				return eoField.isForceValueSearch();
			}
		}
		return false;
	}

	public static boolean isEOFieldWithForceValueSearch(String field) {
		SF<?> eoField = getByField(field);
		if (eoField != null) {
			return eoField.isForceValueSearch();
		}
		return false;
	}
	
	public static boolean isEOField(UID entity, UID field) {
		if (entity == null) {
			throw new IllegalArgumentException("entity must not be null");
		}
		for (SF<?> eoField: getAllFields()) {
			if (eoField.checkField(entity, field)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isEOField(String field) {
		SF<?> eoField = getByField(field);
		if (eoField != null) {
			return true;
		}
		return false;
	}

	public static SF<?> getByField(String field) {
		for (SF<?> eoField : getAllFields()) {
			if (eoField.checkField(field))
				return eoField;
		}
		return null;
	}

	public boolean checkField(String field) {
		return (field != null) && field.equals(getFieldName());
	}
	
	public boolean checkField(UID entity, UID field) {
		return (field != null) && field.equals(getUID(entity));
	}
	
	abstract FieldMeta<?> createStaticMeta();
	
	@Override
	public int hashCode() {
		return field.hashCode();
	}

	@Override
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		if (that instanceof SF<?>) {
			return false;
		} else {
			throw new IllegalArgumentException("Something went wrong: SF.equal(" + that.getClass().getName() + ")! SF.this[" + this + "] that[" + that + "]");
		}
	}

	@Override
	public String toString() {
		return "SF["+field+"]";
	}

	FieldMeta<?> createMeta(UID entityUID) {
		FieldMetaVO<?> meta = (FieldMetaVO<?>) createStaticMeta();
		
		meta.setUID(new UID(entityUID.getString() + postfix, getFieldName()));
		meta.setEntity(entityUID);
		meta.setFieldName(getFieldName());
		meta.setDbColumn(getDbColumn());
		meta.setDataType(getJavaClass().getName());
		meta.setScale(scale);
		return meta;
	}

	private static class MetaDataCache {
		
		private final Map<UID, FieldMeta<?>> entityFields = new HashMap<UID, FieldMeta<?>>();
		
		private final SF<?> sf;
		
		public MetaDataCache(SF<?> sf) {
			this.sf = sf;
		}
		
		FieldMeta<?> getMetaData(UID entityUID) {
			FieldMeta<?> result = entityFields.get(entityUID);
			if (result != null) {
				return result;
			}
			result = sf.createMeta(entityUID);
			entityFields.put(entityUID, result);
			return result;
		}
		
	}
	
	static FieldMeta<?> createPrimaryKey() {
		FieldMetaVO<?> result = new FieldMetaVO<Object>();
		result.setFieldGroup(GROUP_UID_READ);

		result.setReadonly(false);
		result.setNullable(false);
		result.setModifiable(true);
		result.setSearchable(false);
		result.setLogBookTracking(false);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.systemidentifier.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.systemidentifier.description");
		
		result.setOrder(65530);

		return result;
	}
	
	private static FieldMeta<?> createCreatedAt() {
		FieldMetaVO<?> result = new FieldMetaVO<Object>();
		result.setFieldGroup(GROUP_UID_READ);

		result.setReadonly(false);
		result.setNullable(true);
		result.setModifiable(true);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.createdat.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.createdat.description");
		
		result.setOrder(65531);

		return result;
	}

	private static FieldMeta<?> createCreatedBy() {
		FieldMetaVO<?> result = new FieldMetaVO<Object>();
		result.setFieldGroup(GROUP_UID_READ);

		result.setReadonly(false);
		result.setNullable(true);
		result.setModifiable(true);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.createdby.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.createdby.description");
		
		result.setOrder(65532);

		return result;
	}

	private static FieldMeta<?> createChangedAt() {
		FieldMetaVO<?> result = new FieldMetaVO<Object>();
		result.setFieldGroup(GROUP_UID_READ);

		result.setReadonly(false);
		result.setNullable(true);
		result.setModifiable(true);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.changedat.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.changedat.description");
		
		result.setOrder(65533);

		return result;
	}

	private static FieldMeta<?> createChangedBy() {
		FieldMetaVO<?> result = new FieldMetaVO<Object>();
		result.setScale(255);
		result.setFieldGroup(GROUP_UID_READ);

		result.setReadonly(false);
		result.setNullable(true);
		result.setModifiable(true);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.changedby.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.changedby.description");
		
		result.setOrder(65534);

		return result;
	}

	private static FieldMeta<?> createVersion() {
		FieldMetaVO<?> result = new FieldMetaVO<Object>();
		result.setFieldGroup(GROUP_UID_READ);

		result.setReadonly(false);
		result.setNullable(true);
		result.setModifiable(true);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.version.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.version.description");
		
		result.setOrder(65534);

		return result;
	}
	
	private static FieldMeta<?> createState() {
		FieldMetaVO<?> result = new FieldMetaVO<Object>();
		result.setFieldGroup(GROUP_UID_READ);

		result.setReadonly(false);
		result.setNullable(true);
		result.setModifiable(false);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);

		result.setForeignEntity(UID_STATE);
		result.setForeignEntityField("uid{U89Na}");

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.state.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.state.description");
		
		result.setOrder(65528);

		return result;
	}

	private static FieldMeta<?> createStateNumber() {
		FieldMetaVO<?> result = new FieldMetaVO<Object>();
		result.setFieldGroup(GROUP_UID_READ);

		result.setReadonly(true);
		result.setNullable(true);
		result.setModifiable(false);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);

		result.setForeignEntity(UID_STATE);
		result.setForeignEntityField("uid{U89Nd}");

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.statenumeral.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.statenumeral.description");
		
		result.setOrder(65527);

		return result;
	}

	private static FieldMeta<?> createStateIcon() {
		FieldMetaVO<?> result = new FieldMetaVO<Object>();
		result.setFieldGroup(GROUP_UID_READ);

		result.setReadonly(true);
		result.setNullable(true);
		result.setModifiable(true);
		result.setSearchable(false);
		result.setLogBookTracking(false);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);

		result.setForeignEntity(UID_STATE);
		result.setForeignEntityField("uid{U89Nh}");

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.stateicon.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.stateicon.description");
		
		result.setOrder(65526);

		return result;
	}

	private static FieldMeta<?> createSystemIdentifier() {
		FieldMetaVO<?> result = new FieldMetaVO<Object>();
		result.setFieldGroup(GROUP_UID_READ);

		result.setReadonly(false);
		result.setNullable(true);
		result.setModifiable(true);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.systemidentifier.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.systemidentifier.description");
		
		result.setOrder(65530);

		return result;
	}

	private static FieldMeta<?> createProcess() {
		FieldMetaVO<?> result = new FieldMetaVO<Object>();
		result.setFieldGroup(GROUP_UID_READ);

		result.setReadonly(false);
		result.setNullable(true);
		result.setModifiable(false);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);

		result.setForeignEntity(UID_PROCESS);

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.process.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.process.description");
		
		result.setOrder(65529);

		return result;
	}

	private static FieldMeta<?> createOrigin() {
		FieldMetaVO<?> result = new FieldMetaVO<Object>();
		result.setFieldGroup(GROUP_UID_READ);

		result.setReadonly(false);
		result.setNullable(true);
		result.setModifiable(true);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.origin.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.origin.description");
		
		result.setOrder(65535);

		return result;
	}

	private static FieldMeta<?> createLogicalDeleted() {
		FieldMetaVO<?> result = new FieldMetaVO<Object>();
		result.setFieldGroup(GROUP_UID_READ);

		result.setReadonly(false);
		result.setNullable(false);
		result.setModifiable(true);
		result.setSearchable(false);
		result.setLogBookTracking(true);
		result.setShowMnemonic(false);
		result.setUnique(false);
		result.setInsertable(false);
		result.setDefaultMandatory("false");

		result.setLocaleResourceIdForLabel("nuclos.entityfield.eo.logicaldeleted.label");
		result.setLocaleResourceIdForDescription("nuclos.entityfield.eo.logicaldeleted.description");
		
		result.setOrder(65536);

		return result;
	}
}

