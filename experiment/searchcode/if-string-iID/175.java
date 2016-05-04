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
package org.nuclos.client.masterdata;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.nuclos.common.CommonMetaDataServerProvider;
import org.nuclos.common.EntityMeta;
import org.nuclos.common.FieldMeta;
import org.nuclos.common.LafParameterMap;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.PivotInfo;
import org.nuclos.common.UID;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.transport.vo.EntityMetaTransport;
import org.nuclos.common.transport.vo.FieldMetaTransport;
import org.nuclos.common.valueobject.EntityRelationshipModelVO;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeRemote;
import org.nuclos.server.masterdata.ejb3.MetaDataFacadeRemote;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMapImpl;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * An singleton for remotely accessing the meta data information
 * from the client side.
 * <p>
 * This class will directly call to the server (and hence implements
 * {@link CommonMetaDataServerProvider}. You normally
 * want to use {@link org.nuclos.client.common.MetaProvider}.
 * </p>
 */
// @Component
public class MetaDataDelegate implements CommonMetaDataServerProvider {

 	private static MetaDataDelegate INSTANCE;

	public static final String ENTITYNAME_ENTITY = "entity";

	private MetaDataFacadeRemote facade;

	private MasterDataFacadeRemote mdfacade;

	/**
	 * Use getInstance() to create an (the) instance of this class
	 */
	MetaDataDelegate() {
		INSTANCE = this;
	}
	
	public final void setMetaDataFacadeRemote(MetaDataFacadeRemote metaDataFacadeRemote) {
		this.facade = metaDataFacadeRemote;
	}
	
	public final void setMasterDataFacadeRemote(MasterDataFacadeRemote masterDataFacadeRemote) {
		this.mdfacade = masterDataFacadeRemote;
	}

	public static MetaDataDelegate getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}

	public Collection<MasterDataVO<UID>> hasEntityFieldInImportStructure(UID field) {
		return getMetaDataFacade().hasEntityFieldInImportStructure(field);
	}


	public void invalidateServerMetadata() {
		getMetaDataFacade().invalidateServerMetadata();
	}

	public MetaDataFacadeRemote getMetaDataFacade() {
		return this.facade;
	}

	public MasterDataFacadeRemote getMasterDataFacade() {
		return this.mdfacade;
	}

	public boolean hasEntityRows(EntityMeta<?> voEntity) {
		return this.facade.hasEntityRows(voEntity);
	}

	public boolean hasEntityLayout(UID entity) {
		return this.facade.hasEntityLayout(entity);
	}

	public  Object modifyEntityMetaData(EntityMeta<?> metaVO, List<FieldMetaTransport> lstFields) {
		return this.getMetaDataFacade().modifyEntityMetaData(metaVO, lstFields);
	}

	/*
	public String getResourceSIdForEntityFieldLabel(Integer iId) {
		try {
			return this.getMetaDataFacade().getResourceSIdForEntityFieldLabel(iId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public String getResourceSIdForEntityFieldDescription(Integer iId) {
		try {
			return this.getMetaDataFacade().getResourceSIdForEntityFieldDescription(iId);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public void remove(String sEntityName, MasterDataVO<?> mdvo)
				throws CommonBusinessException{
		try {
			this.getMetaDataFacade().remove(sEntityName, mdvo, true);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}
	 */

	/**
	 * Validate all masterdata entries against their meta information (length, format, min, max etc.).
	 * @param sOutputFileName the name of the csv file to which the results are written.
	 */
	public void checkMasterDataValues(String sOutputFileName) {
		try {
			this.getMasterDataFacade().checkMasterDataValues(sOutputFileName);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	/**
	 * checks that all dependants (if any) have a <code>null</code> id.
	 * @param mpDependants may be <code>null</code>.
	 */
	public static void checkDependantsAreNew(DependantMasterDataMapImpl mpDependants) {
		if (mpDependants != null && !mpDependants.areAllDependantsNew()) {
			throw new IllegalArgumentException("mpDependants");
		}
	}

	public String createOrModifyEntity(EntityMeta<?> oldMDEntity, EntityMetaTransport updatedMDEntity, MasterDataVO<?> voEntity, 
			List<FieldMetaTransport> lstFields, boolean blnExecute, String user, String password) throws NuclosBusinessException {
		try {
			return getMetaDataFacade().createOrModifyEntity(oldMDEntity, updatedMDEntity, voEntity, lstFields, blnExecute, user, password);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public List<String> getDBTables() {
		return getMetaDataFacade().getDBTables();
	}

	public Map<String, MasterDataVO<?>> getColumnsFromTable(String sTable) {
		return getMetaDataFacade().getColumnsFromTable(sTable);
	}

	public List<String> getTablesFromSchema(String url, String user, String password, String schema) {
		return getMetaDataFacade().getTablesFromSchema(url, user, password, schema);
	}

	public EntityMeta<?> transferTable(String url, String user, String password, String schema, String table, UID entityUid) {
		return getMetaDataFacade().transferTable(url, user, password, schema, table, entityUid);
	}

	public List<MasterDataVO<UID>> transformTable(String url, String user, String password, String schema, String table) {
		return getMetaDataFacade().transformTable(url, user, password, schema, table);
	}

	public EntityRelationshipModelVO getEntityRelationshipModelVO(MasterDataVO<UID> vo) {
		return getMetaDataFacade().getEntityRelationshipModelVO(vo);
	}

	public boolean isChangeDatabaseColumnToNotNullableAllowed(UID field) {
		return getMetaDataFacade().isChangeDatabaseColumnToNotNullableAllowed(field);
	}

	public boolean isChangeDatabaseColumnToUniqueAllowed(UID field) {
		return getMetaDataFacade().isChangeDatabaseColumnToUniqueAllowed(field);
	}

	/**
	 * uses Server Cache
	 * @return
	 */
	public Collection<EntityMeta<?>> getAllEntities() {
		return getMetaDataFacade().getAllEntities();
	}

	/**
	 * uses Server Cache
	 * @param entity
	 * @return
	 */
	public Map<UID, FieldMeta<?>> getAllEntityFieldsByEntity(UID entity) {
		return getMetaDataFacade().getAllEntityFieldsByEntity(entity);
	}

	@Override
	public Map<UID, FieldMeta<?>> getAllPivotEntityFields(PivotInfo info) {
		return getMetaDataFacade().getAllPivotEntityFields(info);
	}

	public Map<UID, Map<UID, FieldMeta<?>>> getAllEntityFieldsByEntitiesGz(List<UID> entities) {
		return getMetaDataFacade().getAllEntityFieldsByEntitiesGz(entities);
    }

	/**
	 * uses Server Cache
	 * @return
	 */
	public Collection<EntityMeta<?>> getNucletEntities() {
		return getMetaDataFacade().getNucletEntities();
	}

	public void removeEntity(EntityMeta<?> voEntity, boolean dropLayout) throws CommonBusinessException{
		getMetaDataFacade().removeEntity(voEntity, dropLayout);
	}

	public boolean hasEntityImportStructure(UID entity) throws CommonBusinessException {
		return getMetaDataFacade().hasEntityImportStructure(entity);
	}

	public boolean hasEntityWorkflow(UID entity) throws CommonBusinessException {
		return getMetaDataFacade().hasEntityWorkflow(entity);
	}

	public List<String> getVirtualEntities() {
		return getMetaDataFacade().getVirtualEntities();
	}
	
	@Override
	public List<String> getPossibleIdFactories() {
		return getMetaDataFacade().getPossibleIdFactories();
	}

	public List<FieldMeta<?>> getVirtualEntityFields(String virtualentity) {
		return getMetaDataFacade().getVirtualEntityFields(virtualentity);
	}

	public void tryVirtualEntitySelect(EntityMeta<?> virtualentity) throws NuclosBusinessException {
		getMetaDataFacade().tryVirtualEntitySelect(virtualentity);
	}

	public void tryRemoveProcess(EntityObjectVO<?> process) throws NuclosBusinessException {
		getMetaDataFacade().tryRemoveProcess(process);
	}

	public List<EntityObjectVO<UID>> getEntityMenus() {
		return getMetaDataFacade().getEntityMenus();
	}
	
	public Collection<EntityMeta<?>> getSystemMetaData() {
		return getMetaDataFacade().getSystemMetaData();
	}
	
	public Map<UID, LafParameterMap> getLafParameters() {
		return getMetaDataFacade().getLafParameters();
	}

	@Override
	public <PK> EntityMeta<PK> getEntity(UID entityUID) {
		return getMetaDataFacade().getEntity(entityUID);
	}

	@Override
	public FieldMeta<?> getEntityField(UID fieldUID) {
		return getMetaDataFacade().getEntityField(fieldUID);
	}

	@Override
	public EntityMeta<?> getByTablename(String sTableName) {
		return getMetaDataFacade().getByTablename(sTableName);
	}

	@Override
	public boolean isNuclosEntity(UID entityUID) {
		return getMetaDataFacade().isNuclosEntity(entityUID);
	}

}

