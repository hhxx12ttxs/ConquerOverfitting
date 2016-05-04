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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.EntityMeta;
import org.nuclos.common.EntityTreeViewVO;
import org.nuclos.common.FieldMeta;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.UID;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.dal.vo.IDependentDataMap;
import org.nuclos.common2.EntityAndField;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.genericobject.ProxyList;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;

// @Local
public interface MasterDataFacadeLocal extends CommonMasterDataFacade {

	/**
	 * @param sEntityName
	 * @param clctexpr
	 * @return a proxy list containing the search result for the given search expression.
	 * @todo restrict permissions by entity name
	 */
	@RolesAllowed("Login")
	<PK> ProxyList<PK,MasterDataVO<PK>> getMasterDataProxyList(
		UID entity, final List<CollectableEntityField> cefs, CollectableSearchExpression clctexpr);

	/**
	 * gets the ids of all masterdata objects that match a given search expression (ordered, when necessary)
	 * @param cond condition that the masterdata objects to be found must satisfy
	 * @return List<Integer> list of masterdata ids
	 */
	@RolesAllowed("Login")
	<PK> List<PK> getMasterDataIds(UID entity, CollectableSearchExpression cse);

	/**
	 * gets the ids of all masterdata objects
	 * @return List<Integer> list of masterdata ids
	 */
	@RolesAllowed("Login")
	<PK> List<PK> getMasterDataIds(UID entity);

	/**
	 * gets the dependant master data records for the given entity, using the given foreign key field and the given id as foreign key.
	 * @param sEntityName name of the entity to get all dependant master data records for
	 * @param sForeignKeyField name of the field relating to the foreign entity
	 * @param oRelatedId id by which sEntityName and sParentEntity are related
	 * @return
	 * @precondition oRelatedId != null
	 * @todo restrict permissions by entity name
	 */
	@RolesAllowed("Login")
	Collection<MasterDataVO<?>> getDependantMasterData(
		UID sEntityName, UID sForeignKeyField, Object oRelatedId);
	
	<PK> Collection<MasterDataVO<PK>> getDependantMasterDataPk(
			UID sEntityName, UID sForeignKeyField, Object oRelatedId);
	
	<PK, F> Collection<MasterDataVO<PK>> getDependantMasterData(EntityMeta<PK> entity, FieldMeta<F> foreignKeyField, F oRelatedId);

	@RolesAllowed("Login")
	Collection<MasterDataVO<?>> getDependantMasterData(FieldMeta field, Object oRelatedId);

	@RolesAllowed("Login")
	<PK> Collection<EntityTreeViewVO> getDependantSubnodes(
		UID sEntityName, UID sForeignKeyField, Object oRelatedId);

	@RolesAllowed("Login")
	<PK> MasterDataVO<PK> get(EntityMeta<PK> entity, PK pk)
		throws CommonFinderException, CommonPermissionException;	

	@RolesAllowed("Login")
	<PK> MasterDataVO<PK> get(UID entity, PK pk)
		throws CommonFinderException, CommonPermissionException;	

	/**
	 * @param sEntityName
	 * @param oId
	 * @return the version of the given masterdata id.
	 * @throws CommonPermissionException
	 * @throws CommonFinderException
	 */
	@RolesAllowed("Login")
	Integer getVersion(UID entity, Object oId)
		throws CommonFinderException, CommonPermissionException;

	/**
	 * create a new master data record
	 * @param mdvo the master data record to be created
	 * @param mpDependants map containing dependant masterdata, if any
	 * @return master data value object containing the newly created record
	 * @precondition sEntityName != null
	 * @precondition mdvo.getId() == null
	 * @precondition (mpDependants != null) --> mpDependants.areAllDependantsNew()
	 * @nucleus.permission checkWriteAllowed(sEntityName)
	 */
	@RolesAllowed("Login")
	@Deprecated
	<PK> MasterDataVO<PK> create(MasterDataVO<PK> mdvo) throws CommonCreateException,
		CommonPermissionException, NuclosBusinessRuleException;

	/**
	 * create a new master data record
	 * @param mdvo the master data record to be created
	 * @param mpDependants map containing dependant masterdata, if any
	 * @return master data value object containing the newly created record
	 * @precondition sEntityName != null
	 * @precondition mdvo.getId() == null
	 * @precondition (mpDependants != null) --> mpDependants.areAllDependantsNew()
	 * @nucleus.permission checkWriteAllowed(sEntityName)
	 */
	@RolesAllowed("Login")
	<PK> MasterDataVO<PK> create(MasterDataVO<PK> mdvo, String customUsage) throws CommonCreateException,
		CommonPermissionException, NuclosBusinessRuleException;

	/**
	 * modifies an existing master data record.
	 * @param mdvo the master data record
	 * @param mpDependants map containing dependant masterdata, if any
	 * @return id of the modified master data record
	 * @precondition sEntityName != null
	 * @nucleus.permission checkWriteAllowed(sEntityName)
	 */
	@RolesAllowed("Login")
	@Deprecated
	<PK> PK modify(MasterDataVO<PK> mdvo) throws CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonValidationException,
		CommonPermissionException, NuclosBusinessRuleException;
	
	/**
	 * modifies an existing master data record.
	 * @param mdvo the master data record
	 * @param mpDependants map containing dependant masterdata, if any
	 * @return id of the modified master data record
	 * @precondition sEntityName != null
	 * @nucleus.permission checkWriteAllowed(sEntityName)
	 */
	@RolesAllowed("Login")
	<PK> PK modify(MasterDataVO<PK> mdvo,String customUsage) throws CommonCreateException,
		CommonFinderException, CommonRemoveException,
		CommonStaleVersionException, CommonValidationException,
		CommonPermissionException, NuclosBusinessRuleException;

	/**
	 * method to delete an existing master data record
	 * @param mdvo containing the master data record
	 * @param bRemoveDependants remove all dependants if true, else remove only given (single) mdvo record
	 * 			this is helpful for entities which have no layout
	 * @precondition sEntityName != null
	 * @nucleus.permission checkDeleteAllowed(sEntityName)
	 */
	@RolesAllowed("Login")
	<PK> void remove(MasterDataVO<PK> mdvo,
		boolean bRemoveDependants) throws CommonFinderException,
		CommonRemoveException, CommonStaleVersionException,
		CommonPermissionException, NuclosBusinessRuleException;

	/**
	 * method to delete an existing master data record
	 * @param mdvo containing the master data record
	 * @param bRemoveDependants remove all dependants if true, else remove only given (single) mdvo record
	 * 			this is helpful for entities which have no layout
	 * @precondition sEntityName != null
	 * @nucleus.permission checkDeleteAllowed(sEntityName)
	 */
	@RolesAllowed("Login")
	<PK> void remove(MasterDataVO<PK> mdvo,
		boolean bRemoveDependants, String customUsage) throws CommonFinderException,
		CommonRemoveException, CommonStaleVersionException,
		CommonPermissionException, NuclosBusinessRuleException;

	/**
	 * read all dependant masterdata recursively
	 * if necessary, mark the read data as removed
	 * @param sEntityName
	 * @param mdvo
	 */
	@RolesAllowed("Login")
	<PK> IDependentDataMap readAllDependants(UID entity,
		PK iId, IDependentDataMap mpDependants, Boolean bRemoved,
		UID sParentEntity,
		Map<EntityAndField, UID> mpEntityAndParentEntityName);

	/**
	 * creates the given dependants (local use only).
	 * @param dependants
	 * @precondition mpDependants != null
	 */
	void createDependants(UID entity, Object id,
		Boolean removed, IDependentDataMap dependants, String customUsage)
		throws CommonCreateException, CommonPermissionException;
	
	/**
	 * modifies the given dependants (local use only).
	 * @param dependants
	 * @precondition mpDependants != null
	 */
	void modifyDependants(UID entity, Object id,
		Boolean removed, IDependentDataMap dependants, String customUsage)
		throws CommonCreateException, CommonFinderException, CommonPermissionException,
		CommonRemoveException, CommonStaleVersionException;
	
	/**
	 * modifies the given dependants (local use only).
	 * @param dependants
	 * @precondition mpDependants != null
	 */
	void modifyDependants(UID entityName, Object id,
		Boolean removed, IDependentDataMap dependants, boolean read, String customUsage)
		throws CommonCreateException, CommonFinderException, CommonPermissionException,
		CommonRemoveException, CommonStaleVersionException;

	/**
	 * notifies clients that the contents of an entity has changed.
	 * @param sCachedEntityName name of the cached entity.
	 * @precondition sCachedEntityName != null
	 */
	void notifyClients(UID entity);

	/**
	 * @param sEntityName
	 * @param iId the object's id (primary key)
	 * @return the masterdata object with the given entity and id.
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 * @Deprecated use with customUsage
	 */
	@Deprecated
	<PK> MasterDataVO<PK> getWithDependants(
		UID entity, PK iId) throws CommonFinderException,
		NuclosBusinessException, CommonPermissionException;
	
	<PK> MasterDataVO<PK> getWithDependants(
			UID entity, PK iId, String customUsage) throws CommonFinderException,
			NuclosBusinessException, CommonPermissionException;

	/**
	 * @param sEntityName
	 * @param cond search condition
	 * @return the masterdata objects for the given entityname and search condition.
	 * @throws CommonFinderException
	 * @throws CommonPermissionException
	 */
	<PK> Collection<MasterDataVO<PK>> getWithDependantsByCondition(
		UID entity, CollectableSearchCondition cond, String customUsage) ;
	
	<PK> Collection<MasterDataVO<PK>> getWithDependantsByCondition(EntityMeta<PK> entity, CollectableSearchCondition cond, String customUsage) ;

	/**
	 * @param sEntityName
	 * @param lstIntIds
	 * @param lstRequiredSubEntities
	 * @return the next chunk of the search result for a proxy list.
	 * @todo restrict permissions by entity name
	 */
	@RolesAllowed("Login")
	<PK> List<MasterDataVO<PK>> getMasterDataMore(
		UID entity, final List<PK> lstIntIds,
		final List<EntityAndField> lstRequiredSubEntities);
	
	@RolesAllowed("Login")
    void insertInto(UID table, UID column1, Object value1, Object[] varargs) 
			throws CommonCreateException;

	@RolesAllowed("Login")
	void deleteFrom(UID table, UID column1, Object value1, Object[] varargs) 
			throws CommonRemoveException;

	/**
	 * @param user - the user for which to get subordinated users
	 * @return List<MasterDataVO> list of masterdata valueobjects
	 */
	Collection<MasterDataVO<UID>> getUserHierarchy(String user);
}

