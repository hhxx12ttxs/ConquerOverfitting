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
package org.nuclos.server.masterdata.valueobject;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.nuclos.common.EntityMeta;
import org.nuclos.common.FieldMeta;
import org.nuclos.common.IMetaProvider;
import org.nuclos.common.SF;
import org.nuclos.common.SFValueable;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.TranslationVO;
import org.nuclos.common.UID;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.DependentDataMap;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.IDependentDataMap;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.LangUtils;
import org.nuclos.server.common.valueobject.NuclosValueObject;

/**
 * Generic value object representing a master data record.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @author	<a href="mailto:sekip.topcu@novabit.de">M. Sekip Top\u00e7u</a>
 * @version 00.01.000
 */
public class MasterDataVO<PK> implements IMasterDataVO<PK> 
//TODO MULTINUCLET: Enable for backwards compatibility after refactoring... 
//, IMasterDataVODeprecated<T, PK>
{

	private static final Logger LOG = Logger.getLogger(MasterDataVO.class);
	
	private static final long serialVersionUID = 16392087823428953L;
	
	private EntityObjectVO<PK> wrapped;
	
	/**
	 * If this object represents a system record, i.e. a record which cannot
	 * manipulated by the user.
	 */
	private boolean systemRecord;
	
	/**
	 * If this object contains fields for resource-ids, a list of translations can be supplied.
	 */
	private List<TranslationVO> resources;
	
	/**
	 * Fill only when needed...
	 */
	private static transient IMetaProvider MDP;
	
	public MasterDataVO(EntityObjectVO<PK> wrapped) {
		this(wrapped, false);
	}
	
	public MasterDataVO(EntityObjectVO<PK> wrapped, boolean systemRecord) {
		this.wrapped = wrapped;
		this.systemRecord = systemRecord;
	}
	
	public MasterDataVO(UID entityUID, PK oId, Date dateCreatedAt, String sCreatedBy,
			Date dateChangedAt, String sChangedBy, Integer iVersion) {
		this(entityUID, oId, dateCreatedAt, sCreatedBy, dateChangedAt, sChangedBy, iVersion, null, null, null, false, false);
	}
	
	public MasterDataVO(MasterDataVO<PK> mdWithoutDependents, IDependentDataMap dependents) {
		this(mdWithoutDependents.getEntityObject());
		if (dependents == null || dependents.isEmpty())
			dependents = mdWithoutDependents.getDependents(); // fallback for dependencies of XMLEntities.
		setDependents(dependents);
	}
	
	public MasterDataVO(UID entityUID, PK oId, Date dateCreatedAt, String sCreatedBy,
			Date dateChangedAt, String sChangedBy, Integer iVersion, 
			Map<UID, Object> mpFields, 
			Map<UID, Long> mpFieldIds,
			Map<UID, UID> mpFieldUids, 
			boolean systemRecord, boolean isThin) {
		wrapped = new EntityObjectVO<PK>(entityUID);
		wrapped.setThin(isThin);
		
		if (oId != null)
			wrapped.setPrimaryKey(oId);
		else {
			wrapped.flagNew();
		}
		
		if (dateChangedAt != null) {
			wrapped.setChangedAt(new InternalTimestamp(dateChangedAt.getTime()));
		}
		wrapped.setChangedBy(sChangedBy);
		if (dateCreatedAt != null) {
			wrapped.setCreatedAt(new InternalTimestamp(dateCreatedAt.getTime()));
		}
		wrapped.setCreatedBy(sCreatedBy);
		wrapped.setVersion(iVersion);
		this.systemRecord = systemRecord;
		
		if (mpFields != null) {
			for (UID fieldUID : mpFields.keySet()) {
				wrapped.setFieldValue(fieldUID, mpFields.get(fieldUID));
			}
		}
		if (mpFieldUids != null) {
			for (UID fieldUID : mpFieldUids.keySet()) {
				wrapped.setFieldUid(fieldUID, mpFieldUids.get(fieldUID));
			}
		}
		if (mpFieldIds != null) {
			for (UID fieldUID : mpFieldIds.keySet()) {
				wrapped.setFieldId(fieldUID, mpFieldIds.get(fieldUID));
			}
		}
	}
	
	public void setThin(boolean bThin) {
		if (wrapped != null) wrapped.setThin(bThin);
	}

	public void setKeepVersion(boolean bKeepVersion) {
		if (wrapped != null) wrapped.setKeepVersion(bKeepVersion);
	}

	public void setSystemFieldsOnRequest(boolean b) {
		if (wrapped != null) wrapped.setSystemFieldsOnRequest(b);
	}

	private static IMetaProvider getMetaProvider() {
		if (MDP == null && SpringApplicationContextHolder.isSpringReady()) {
			MDP = (IMetaProvider) SpringApplicationContextHolder.getBean("metaDataProvider");
		}
		return MDP;
	}

	/**
	 * "copy constructor"
	 * @param mdvo
	 */
	protected MasterDataVO(MasterDataVO<PK> mdvo) {
		this.wrapped = mdvo.getEntityObject();
	}

	/**
	 * constructor to be called by client only
	 * @param mdmetavo the meta data of the master data object to create
	 * @param bSetBooleansToFalse Are booleans to be set to <code>false</code> rather than <code>null</code>?
	 * <code>true</code> is for compatibility only and shouldn't be used for new code.
	 * @precondition mdmetavo != null
	 * @precondition mdmetavo.getEntityName() != null
	 * @postcondition this.getId() == null
	 */
	public MasterDataVO(EntityMeta<?> metavo, boolean bSetBooleansToFalse) {
		this(metavo.getUID(), null, null, null, null, null, null, null, null, null, false, false);
		
		// create fields:
		for (FieldMeta<?> fieldMeta : metavo.getFields()) {
			// enter default value:
			// FALSE for Boolean, null otherwise
			final Object oValue = (bSetBooleansToFalse && (fieldMeta.getDataType() == Boolean.class.getName())) ? Boolean.FALSE : null;

			wrapped.setFieldValue(fieldMeta.getUID(), oValue);

			// for id fields, add an id entry as well:
			if (fieldMeta.getForeignEntity() != null) {
				wrapped.setFieldId(fieldMeta.getUID(), null);
			}
		}
		assert this.getId() == null;
	}

	/**
	 * Clone is <em>with</em> dependent objects!
	 * <p>
	 * Clone only works if the contained Objects are immutable! We don't ensure this currently.
	 * </p><p>
	 * If only a copy without dependent objects is needed, use {@link #copy()}.
	 * </p>
	 * @return a clone of <code>this</code>.
	 * @postcondition result.isChanged() == this.isChanged()
	 * @postcondition result.isRemoved() == this.isRemoved()
	 * @postcondition result.getFields().equals(this.getFields())
	 * @postcondition result.getId() == this.getId()
	 * @see #copy()
	 */
	@Override
	public MasterDataVO<PK> clone() {
		/** @todo this only works if the contained Objects are immutable! We don't ensure this currently. */
		/*
		final MasterDataVOImpl result = (MasterDataVOImpl) super.clone();
		result.mpFields = new HashMap<String, Object>(this.mpFields);
		 */
		final MasterDataVO<PK> result = new MasterDataVO<PK>(wrapped.copy(), systemRecord);
		result.setResources(getResources());
		result.setDependents(new DependentDataMap());

		assert result.isChanged() == this.isChanged();
		assert result.isRemoved() == this.isRemoved();
		assert result.getId() == this.getId();
		return result;
	}

	@Override
	public void setChanged(boolean changed) {
		wrapped.flagUpdate();
	}


	@Override
	public boolean equals(Object obj) {

		if(!(obj instanceof MasterDataVO)) {
			return false;
		}
		MasterDataVO<?> that = (MasterDataVO<?>) obj;

		if(ObjectUtils.equals(that.getId(),this.getId()) && LangUtils.equals(this.getFieldValues(), that.getFieldValues())) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Copy is <em>without</em> dependent objects!
	 * 
	 * @return a new copy of <code>this</code>, with <code>null</code> id.
	 * @postcondition !result.isChanged()
	 * @postcondition result.getFields().equals(this.getFields())
	 * @postcondition result.getId() == null
	 * @see #clone()
	 */
	@Override
	public MasterDataVO<PK> copy() {
		/** @todo this only works if the contained Objects are immutable! We don't ensure this currently. */
		final EntityObjectVO<PK> copy = wrapped.copy();
		return new MasterDataVO<PK>(copy, systemRecord);
	}

	/**
	 * @return a new copy of <code>this</code>, with <code>null</code> id.
	 * @postcondition !result.isChanged()
	 * @postcondition result.getFields().equals(this.getFields())
	 * @postcondition result.getId() == null
	 * @see #clone()
	 */
	@Override
	public MasterDataVO<PK> copy(boolean blnWithDependants) {
		final MasterDataVO<PK> copy = copy();
		if (!blnWithDependants) {
			copy.setDependents(new DependentDataMap());
		}
		return copy;
	}

	@Override
	public PK getPrimaryKey() {
		return wrapped.getPrimaryKey();
	}

	/**
	 * @return this object's primary key
	 * 
	 * @deprecated Use {@link #getPrimaryKey()}.
	 */
	@Override
	public PK getId() {
		return wrapped.getPrimaryKey();
	}
	
	@Override
	public void setPrimaryKey(PK pk) {
		wrapped.setPrimaryKey(pk);
	}

	/**
	 * Returns true if this record is a system record.
	 */
	@Override
	public boolean isSystemRecord() {
		return systemRecord;
	}
	
	@Override
	public Long getFieldId(UID field) {
		return wrapped.getFieldId(field);
	}
	
	@Override
	public UID getFieldUid(UID field) {
		return wrapped.getFieldUid(field);
	}

	/**
	 * @param fieldUID
	 * @return the value of the field with the given uid.
	 */
	@Override
	public Object getFieldValue(UID fieldUID) {
		return wrapped.getFieldValue(fieldUID);
	}

	/**
	 * generic (typed) version of getField(UID).
	 * Note that Class<T>.cast() is about 10-15 times slower than a plain old cast.
	 * For optimum performance (where it's necessary) use the non-generic version of getField(String).
	 * @param sFieldName the name of the field.
	 * @param cls the class of the field.
	 * @return the value of the field with the given name, casted to the given class.
	 * @throws ClassCastException if the value of the field doesn't have the given class.
	 * @see #getFieldValue(String)
	 */
	@Override
	public <T> T getFieldValue(UID fieldUID, Class<T> cls) {
		return cls.cast(getFieldValue(fieldUID));
	}
	
	/**
	 * sets the field with the given uid to the given value.
	 * @param sFieldName
	 * @param oValue
	 * @postcondition this.isChanged()
	 * @todo setChanged() only if the given value is different from the old value.
	 */
	@Override
	public void setFieldValue(UID fieldUID, Object oValue) {
		wrapped.setFieldValue(fieldUID, oValue);
		if (!wrapped.isFlagNew() && !wrapped.isFlagRemoved()) {
			wrapped.flagUpdate();
		}
	}
	
	@Override
	public void setFieldId(UID fieldUID, Long id) {
		wrapped.setFieldId(fieldUID, id);
		if (!wrapped.isFlagNew() && !wrapped.isFlagRemoved()) {
			wrapped.flagUpdate();
		}
	}
	
	@Override
	public void setFieldUid(UID fieldUID, UID uid) {
		wrapped.setFieldUid(fieldUID, uid);
		if (!wrapped.isFlagNew() && !wrapped.isFlagRemoved()) {
			wrapped.flagUpdate();
		}
	}
	
	public <T> void setFieldValue(FieldMeta.Valueable<T> entityField, T obj) {
		wrapped.setFieldValue(entityField, obj);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getFieldValue(FieldMeta.Valueable<T> entityField) {
		return (T) wrapped.getFieldValue(entityField);
	}
	
	public <T> void setFieldValue(SFValueable<T> staticField, T obj) {
		wrapped.setFieldValue(staticField, obj);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getFieldValue(SFValueable<T> staticField) {
		return (T) wrapped.getFieldValue(staticField);
	}
	
	public void setFieldUid(FieldMeta<UID> entityField, UID uid) {
		wrapped.setFieldUid(entityField, uid);
	}
	
	public UID getFieldUid(FieldMeta<UID> entityField) {
		return wrapped.getFieldUid(entityField.getUID());
	}
	
	public void setFieldUid(SF<UID> staticField, UID uid) {
		wrapped.setFieldUid(staticField, uid);
	}
	
	public UID getFieldUid(SF<UID> staticField) {
		return wrapped.getFieldUid(staticField);
	}
	
	public void setFieldId(FieldMeta<Long> entityField, Long id) {
		wrapped.setFieldId(entityField, id);
	}
	
	public Long getFieldId(FieldMeta<Long> entityField) {
		return wrapped.getFieldId(entityField);
	}
	
	public void setFieldId(SF<Long> staticField, Long id) {
		wrapped.setFieldId(staticField, id);
	}
	
	public Long getFieldId(SF<Long> staticField) {
		return wrapped.getFieldId(staticField);
	}

	/**
	 * get all fields of master data record
	 * @return map of all fields for master data record
	 * @postcondition result != null
	 */
	@Override
	public Map<UID, Object> getFieldValues() {
		return Collections.unmodifiableMap(wrapped.getFieldValues());
	}
	
	@Override
	public Map<UID, Long> getFieldIds() {
		return Collections.unmodifiableMap(wrapped.getFieldIds());
	}
	
	@Override
	public Map<UID, UID> getFieldUids() {
		return Collections.unmodifiableMap(wrapped.getFieldUids());
	}

	/**
	 * @return Has this object been changed since its creation?
	 */
	@Override
	public boolean isChanged() {
		return wrapped.isFlagUpdated();
	}

	/**
	 * @deprecated use {@link #toDescription()}
	 */
	@Override
	public String getDebugInfo() {
		return toDescription();
	}

	/**
	 * @return the contents of the name field, if any - otherwise this object's id, if any.
	 */
	@Override
	public String toString() {
		return this.getPrimaryKey() != null ? this.getPrimaryKey().toString() : "New MDVO";
	}
	
	@Override
	public String toDescription() {
		final StringBuilder result = new StringBuilder();
		result.append("MdVO[id=").append(wrapped.getPrimaryKey());
		if (wrapped.isFlagUpdated()) {
			result.append(",changed=").append(wrapped.isFlagUpdated());
		}
		if (systemRecord) {
			result.append(",sr=").append(systemRecord);
		}
		result.append(",fields=").append(wrapped.getFieldValues());
		final IDependentDataMap deps = wrapped.getDependents();
		if (deps != null && !deps.isEmpty()) {
			result.append(",deps=").append(deps);
		}
		result.append("]");
		return result.toString();
	}

	/**
	 * @return the common fields of this object. Note that this may only be called for entities which have an Integer id.
	 * @see #getIntId()
	 */
	@Override
	public NuclosValueObject<PK> getNuclosValueObject() {
		return new NuclosValueObject<PK>(this.getId(), this.getCreatedAt(), this.getCreatedBy(),
				this.getChangedAt(), this.getChangedBy(), this.getVersion());
	}

	/**
	 * inner class <code>GetId</code>: transforms a <code>MasterDataVO</code> into its id.
	 */
	public static class GetId<PK> implements Transformer<MasterDataVO<PK>, PK> {
		@Override
        public PK transform(MasterDataVO<PK> mdvo) {
			return mdvo.getId();
		}
	}
//
//	/**
//	 * Transformer: gets the field with the given name
//	 */
//	public static class GetField implements Transformer<MasterDataVO, Object> {
//		private final String sFieldName;
//
//		public GetField(String sFieldName) {
//			this.sFieldName = sFieldName;
//		}
//
//		@Override
//        public Object transform(MasterDataVO mdvo) {
//			return mdvo.getField(this.sFieldName);
//		}
//	}
//
//	/**
//	 * Transformer: gets the field with the given name, casted to the given type.
//	 */
//	public static class GetTypedField<T> implements Transformer<MasterDataVO, T> {
//		private final String sFieldName;
//		private final Class<T> cls;
//
//		public GetTypedField(String sFieldName, Class<T> cls) {
//			this.sFieldName = sFieldName;
//			this.cls = cls;
//		}
//
//		/**
//		 * @param mdvo
//		 * @throws ClassCastException if the value of the field doesn't have the given type.
//		 */
//		@Override
//        public T transform(MasterDataVO mdvo) {
//			return mdvo.getField(this.sFieldName, this.cls);
//		}
//	}

//	/**
//	 * inner class <code>NameComparator</code>. Compares <code>MasterDataVO</code>s by their names.
//	 */
//	public static class NameComparator implements Comparator<MasterDataVO> {
//		private final Collator collator = LangUtils.getDefaultCollator();
//
//		@Override
//        public int compare(MasterDataVO mdvo1, MasterDataVO mdvo2) {
//			return this.collator.compare(mdvo1.getField(FIELDNAME_NAME), mdvo2.getField(FIELDNAME_NAME));
//		}
//	}	// inner class LabelComparator

	@Override
	public void setDependents(IDependentDataMap mpDependents) {
		wrapped.setDependents(mpDependents);
	}

	@Override
	public IDependentDataMap getDependents() {
		return wrapped.getDependents();
	}

	@Override
	public List<TranslationVO> getResources() {
		return resources;
	}

	@Override
	public void setResources(List<TranslationVO> resources) {
		this.resources = resources;
	}

	/**
	 * Return the underlying EntityObjectVO.
	 * @since Nuclos 3.8
	 * @author Thomas Pasch
	 */
	@Override
	public EntityObjectVO<PK> getEntityObject() {
		return wrapped;
	}
	
	// override methods from AbstractNuclosValueObject
	
	/**
	 * mark underlying database record as to be removed from database
	 */
	@Override
	public void remove() {
		wrapped.flagRemove();
	}

	/**
	 * is underlying database record to be removed from database?
	 * @return boolean value
	 */
	@Override
	public boolean isRemoved() {
		return wrapped.isFlagRemoved();
	}

	/**
	 * get creation date (datcreated) of underlying database record
	 * @return created date of underlying database record
	 */
	@Override
	public Date getCreatedAt() {
		return wrapped.getChangedAt();
	}

	/**
	 * get creator (strcreated) of underlying database record
	 * @return creator of underlying database record
	 */
	@Override
	public String getCreatedBy() {
		return wrapped.getCreatedBy();
	}

	/**
	 * get last changed date (datchanged) of underlying database record
	 * @return last changed date of underlying database record
	 */
	@Override
	public Date getChangedAt() {
		return wrapped.getCreatedAt();
	}

	/**
	 * get last changer (strchanged) of underlying database record
	 * @return last changer of underlying database record
	 */
	@Override
	public String getChangedBy() {
		return wrapped.getCreatedBy();
	}

	/**
	 * get version (intversion) of underlying database record
	 * @return version of underlying database record
	 */
	@Override
	public int getVersion() {
		return wrapped.getVersion();
	}
	
	/**
	 * @since Nuclos 3.5
	 * @author Thomas Pasch
	 */
	@Override
	public void setVersion(int version) {
		wrapped.setVersion(version);
	}

	@Override
	public void clearAllFields() {
		wrapped.clearAllFields();
	}
	
	// end of override methods from AbstractNuclosValueObject
	
	
	
	
//	TODO MULTINUCLET: Enable for backwards compatibility after refactoring... 
	
//	/**
//	 * constructor to be called by server and client
//	 * @param oId primary key of underlying database record
//	 * @param dateCreatedAt creation date of underlying database record
//	 * @param sCreatedBy creator of underlying database record
//	 * @param dateChangedAt last changed date of underlying database record
//	 * @param sChangedBy last changer of underlying database record
//	 * @param iVersion version of underlying database record
//	 * @param mpFields May be <code>null</code>.
//	 * @precondition sEntity != null
//	 * @postcondition this.getId() == oId
//	 * 
//	 * @deprecated As we want to migrate away from MasterDataVO to EntityObjectVO, it is *much* saver
//	 * 		to use {@link #MasterDataVO(String, Object, Date, String, Date, String, Integer, Map)}
//	 */
//	public MasterDataVO(PK oId, Date dateCreatedAt, String sCreatedBy,
//				Date dateChangedAt, String sChangedBy, Integer iVersion, Map<String, Object> mpFields) {
//			this(null, oId, dateCreatedAt, sCreatedBy, dateChangedAt, sChangedBy, iVersion, mpFields, false);
//	}
//
//	public MasterDataVO(String entity, PK oId, Date dateCreatedAt, String sCreatedBy,
//			Date dateChangedAt, String sChangedBy, Integer iVersion, Map<String, Object> mpFields) {
//		this(entity, oId, dateCreatedAt, sCreatedBy, dateChangedAt, sChangedBy, iVersion, mpFields, false);
//	}
//	
//	public MasterDataVO(String entity, PK oId, Date dateCreatedAt, String sCreatedBy,
//			Date dateChangedAt, String sChangedBy, Integer iVersion, Map<String, Object> mpFields, 
//			boolean systemRecord) {
//		this(entity, oId, dateCreatedAt, sCreatedBy, dateChangedAt, sChangedBy, iVersion, mpFields, systemRecord, false);		
//	}
//	
//	public MasterDataVO(String entity, PK oId, Date dateCreatedAt, String sCreatedBy,
//			Date dateChangedAt, String sChangedBy, Integer iVersion, Map<String, Object> mpFields, 
//			boolean systemRecord, boolean isThin) {
//		this(getMetaDataProvider().getEntityUnsafe(entity).getPrimaryKey(), oId, dateCreatedAt, sCreatedBy, dateChangedAt, sChangedBy, iVersion, mpFields, systemRecord, isThin);
//	}
//
//	public MasterDataVO(UID entityUID, PK oId, Date dateCreatedAt, String sCreatedBy,
//		Date dateChangedAt, String sChangedBy, Integer iVersion, Map<String, Object> mpFields, 
//		boolean systemRecord, boolean isThin) {
//		// super(dateCreatedAt, sCreatedBy, dateChangedAt, sChangedBy, iVersion);
//		wrapped = new EntityObjectVO(entityUID);
//		wrapped.setThin(isThin);
//		final int size = mpFields == null ? 0 : mpFields.size(); 
//		
//		final IMetaProvider<EntityMeta, FieldMeta> p = getMetaDataProvider();
//		
//		if (oId != null)
//			wrapped.setPrimaryKey(oId);
//		else {
//			wrapped.flagNew();
//			if (p != null) {
//				if (p.getEntity(wrapped.getEntityUID()).isStateModel())
//					wrapped.getFieldValues().put(NuclosStaticField.LOGICALDELETED.getMetaData(wrapped.getEntityUID()).getPrimaryKey(), Boolean.FALSE);
//			}
//		}
//		if (dateChangedAt != null) {
//			wrapped.setChangedAt(new InternalTimestamp(dateChangedAt.getTime()));
//		}
//		wrapped.setChangedBy(sChangedBy);
//		if (dateCreatedAt != null) {
//			wrapped.setCreatedAt(new InternalTimestamp(dateCreatedAt.getTime()));
//		}
//		wrapped.setCreatedBy(sCreatedBy);
//		wrapped.setVersion(iVersion);
//		this.systemRecord = systemRecord;
//
//		final Map<UID,Object> fields = wrapped.getFieldValues();
//		final Map<UID,Long> idFields = wrapped.getFieldIds();
//		if (mpFields != null) {
//			for (String f: mpFields.keySet()) {
//				final Object value = mpFields.get(f);
//				UID fieldUID = fieldIdUnsafe(f);
//				if (f.endsWith("Id")) {
//					final String e = getEntityObject().getEntity();
//					if (e == null) {
//						throw new IllegalStateException("MasterDataVO should be constructed with entity name set");
//					}
//					fieldUID = fieldIdUnsafe(f);
//					final FieldMeta efmd;
//					try {
//						efmd = p.getEntityField(fieldUID);
//						if (efmd.getForeignEntity() != null) {
//							idFields.put(fieldUID, IdUtils.toLongId(value));
//						}
//						else {
//							fields.put(fieldUID, value);
//						}
//					}
//					catch (CommonFatalException e2) {
//						// ignore - there is no field with this name
//					}
//				}
//				else {
//					if (fieldUID != null) {
//						idFields.put(fieldUID, IdUtils.toLongId(value));
//					}
//				}
//			}
//		}
//		
//		assert IdUtils.equals(getId(), oId);
//	}
	
//	/**
//	 * @postcondition this.getId() == null
//	 */
//	private MasterDataVO(String entity, Map<String, Object> mpFields) {
//		this(entity, null, null, null, null, null, null, mpFields);
//		assert this.getId() == null;
//	}
	
//	/**
//	 * @return this object's primary key, which must be an Integer, otherwise a ClassCastException is thrown. Use getId()
//	 * if you're not sure about the primary key's type.
//	 */
//	@Override
//	public Integer getIntId() {
//		return IdUtils.unsafeToId(wrapped.getId());
//	}
	
//	/**
//	 * Set the integer id of this object.
//	 * This is only allowed to keep the mpDependants up to date for newly created masterdata records, which is necessary for the logbook!
//	 * @param iId
//	 */
//	@Override
//	public void setId(Object iId) {
//		wrapped.setId(IdUtils.toLongId(iId));
//	}
	
//	/**
//	 * @param sFieldName field name
//	 * @return the value of the field with the given name.
//	 */
//	@Override
//	public Object getField(String sFieldName) {
//		Object result;
//		if (sFieldName.endsWith("Id")) {
//			result = IdUtils.unsafeToId(wrapped.getFieldId(fieldId(sFieldName)));
//			// just to test if field is not in "normal" fields
//			if (result == null && wrapped.getFields().containsKey(sFieldName)) {
//				if (sFieldName.equals("nuclosSystemId")) {
//					LOG.warn("Trying to access 'nuclosSystemId' on an MasterDataVO: this field is/should be present on GenericObjectVO only");
//				}
//				result = IdUtils.unsafeToIdIfPossible(wrapped.getField(sFieldName));
//			}
//		}
//		else {
//			result = wrapped.getField(sFieldName);
//		}
//		return result;
//	}
	
//	/**
//	 * generic (typed) version of getField(String).
//	 * Note that Class<T>.cast() is about 10-15 times slower than a plain old cast.
//	 * For optimum performance (where it's necessary) use the non-generic version of getField(String).
//	 * @param sFieldName the name of the field.
//	 * @param cls the class of the field.
//	 * @return the value of the field with the given name, casted to the given class.
//	 * @throws ClassCastException if the value of the field doesn't have the given class.
//	 * @see #getField(String)
//	 */
//	@Override
//	public <T> T getField(String sFieldName, Class<T> cls) {
//		return cls.cast(getField(sFieldName));
//	}
	
//	/**
//	 * sets the field with the given name to the given value.
//	 * @param sFieldName
//	 * @param oValue
//	 * @postcondition this.isChanged()
//	 * @todo setChanged() only if the given value is different from the old value.
//	 */
//	@Override
//	public void setField(String sFieldName, Object oValue) {
//		if (sFieldName.endsWith("Id")) {
//			wrapped.getFieldIds().put(fieldId(sFieldName), IdUtils.toLongId(oValue));
//		}
//		else {
//			wrapped.getFields().put(sFieldName, oValue);
//		}
//		if (!wrapped.isFlagNew() && !wrapped.isFlagRemoved()) {
//			wrapped.flagUpdate();
//		}
//	}
	
//	/**
//	 * get all fields of master data record
//	 * @return map of all fields for master data record
//	 * @postcondition result != null
//	 */
//	@Override
//	public Map<String, Object> getFields() {
//		// return Collections.unmodifiableMap(this.mpFields);
//		final Map<String,Object> result = new HashMap<String, Object>(wrapped.getFields());
//		for (String id: wrapped.getFieldIds().keySet()) {
//			final String idWithId = id + "Id";
//			result.put(idWithId, IdUtils.unsafeToId(wrapped.getFieldIds().get(id)));
//		}
//		return Collections.unmodifiableMap(result);
//	}
	
//	/**
//	 * sets the given fields
//	 * @param mpFields Map<String sFieldName, Object oValue>
//	 * @precondition mpFields != null
//	 * @postcondition this.isChanged()
//	 */
//	@Override
//	public void setFields(Map<String, Object> mpFields) {
//		if (mpFields == null) {
//			throw new NullArgumentException("mpFields");
//		}
//		for (String sFieldName : mpFields.keySet()) {
//			this.setField(sFieldName, mpFields.get(sFieldName));
//		}
//		if (!wrapped.isFlagNew() && !wrapped.isFlagRemoved()) {
//			assert this.isChanged();
//		}
//	}
	
//	/**
//	 * checks if all fields in this masterdata cvo are empty (the given foreign key field is ignored)
//	 * @param sForeignKey foreign key field to ignore (always filled anyway)
//	 * @return Are all fields except for the foreign key field empty?
//	 * @todo What is "the" foreign key field? There can be more than one per entity!
//	 */
//	@Override
//	public boolean isEmpty(String field) {
//		if (getMetaDataProvider() == null) {
//			throw new NuclosFatalException("too early");
//		}
//		if (wrapped.getEntity() == null) {
//			throw new NullPointerException("entity must not be null");
//		}
//	    Map<String, FieldMeta> efMetas = getMetaDataProvider().getAllEntityFieldsByEntity(wrapped.getEntity());
//		if (field.endsWith("Id")) {
//			String withoutId = field.substring(0, field.length()-2);
//			if (!efMetas.keySet().contains(field) && efMetas.keySet().contains(withoutId)) {
//				if (efMetas.get(withoutId).getForeignEntity() != null) {
//					if (wrapped.getFieldId(withoutId) != null) {
//						return false;
//					}
//				} else {
//					if (wrapped.getField(field) != null) {
//						return false;
//					}
//				}
//			} else {
//				if (wrapped.getField(field) != null) {
//					return false;
//				}
//			}
//		} else {
//			if (wrapped.getField(field) != null) {
//				return false;
//			}
//		}
//		return true;
//	}
	
//	/**
//	 * Return the entity name. This is a convenience method.
//	 * @since Nuclos 3.8
//	 * @author Thomas Pasch
//	 */
//	@Override
//	public String getEntityName() {
//		return wrapped.getEntity();
//	}
	
//	@Deprecated
//	private UID fieldIdUnsafe(String fieldIdName) {
//		assert fieldIdName.endsWith("Id");
//		String fieldName = fieldIdName.substring(0, fieldIdName.length() - 2);
//		FieldMeta fieldMeta = getMetaDataProvider().getAllEntityFieldsByEntity(wrapped.getEntityUID()).get(fieldName);
//		if (fieldMeta == null) {
//			return null;
//		}
//		return fieldMeta.getPrimaryKey();
//	}

}	// class MasterDataVO


