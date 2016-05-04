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
package org.nuclos.server.navigation.ejb3;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.AttributeProvider;
import org.nuclos.common.E;
import org.nuclos.common.EntityMeta;
import org.nuclos.common.EntityTreeViewVO;
import org.nuclos.common.FieldMeta;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.SF;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.UID;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.dblayer.JoinType;
import org.nuclos.common.nuclet.RemoveContentsResult;
import org.nuclos.common.security.Permission;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.DatasourceCache;
import org.nuclos.server.common.MetaProvider;
import org.nuclos.server.common.NodeCache;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.dblayer.DbInvalidResultSizeException;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbColumnExpression;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.eventsupport.ejb3.EventSupportFacadeLocal;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeLocal;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.navigation.treenode.DefaultMasterDataTreeNode;
import org.nuclos.server.navigation.treenode.DynamicTreeNode;
import org.nuclos.server.navigation.treenode.EntitySearchResultTreeNode;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.RelationDirection;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.SystemRelationType;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNodeFactory;
import org.nuclos.server.navigation.treenode.GroupSearchResultTreeNode;
import org.nuclos.server.navigation.treenode.GroupTreeNode;
import org.nuclos.server.navigation.treenode.MasterDataSearchResultTreeNode;
import org.nuclos.server.navigation.treenode.MasterDataTreeNode;
import org.nuclos.server.navigation.treenode.RelationTreeNode;
import org.nuclos.server.navigation.treenode.SubFormEntryTreeNode;
import org.nuclos.server.navigation.treenode.SubFormTreeNode;
import org.nuclos.server.navigation.treenode.TreeNode;
import org.nuclos.server.navigation.treenode.nuclet.NucletTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.NuclosInstanceTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.AbstractNucletContentEntryTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.DefaultNucletContentEntryTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.NucletContentCustomComponentTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.NucletContentEntityTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.NucletContentProcessTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.NucletContentRuleTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.NucletContentTreeNode;
import org.nuclos.server.navigation.treenode.nuclet.content.ReportNucletContentTreeNode;
import org.nuclos.server.ruleengine.NuclosCompileException.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for managing explorer tree structures and contents.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @todo restrict
*/
@Transactional(noRollbackFor= {Exception.class})
@RolesAllowed("Login")
public class TreeNodeFacadeBean extends NuclosFacadeBean implements TreeNodeFacadeRemote {

	private static final int DEFAULT_ROWCOUNT_FOR_SEARCHRESULT = 500;
	
	//
	
	private ServerParameterProvider serverParameterProvider;
	
	private GenericObjectFacadeLocal genericObjectFacade;
	
	private MasterDataFacadeLocal masterDataFacade;
	
	private LocaleFacadeLocal localeFacade;
	
	private EventSupportFacadeLocal eventSupportFacadeLocal;
	
	public TreeNodeFacadeBean() {
	}
	
	@Autowired
	void setServerParameterProvider(ServerParameterProvider serverParameterProvider) {
		this.serverParameterProvider = serverParameterProvider;
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
	
	private final MasterDataFacadeLocal getMasterDataFacade() {
		return masterDataFacade;
	}
	
	private final LocaleFacadeLocal getLocaleFacade() {
		return localeFacade;
	}

	@Autowired
	final void setLocaleFacade(LocaleFacadeLocal localeFacade) {
		this.localeFacade = localeFacade;
	}

	final void setEventSupportFacade(EventSupportFacadeLocal eventSupportFacadeLocal) {
		this.eventSupportFacadeLocal = eventSupportFacadeLocal;
	}
	/**
	 * gets a generic object tree node for a specific generic object
	 * @param iGenericObjectId id of generic object to get tree node for
	 * @return generic object tree node for given id, if existing and allowed. null otherwise.
	 * @throws CommonFinderException if the object doesn't exist (anymore).
	 * @postcondition result != null
	 */
	public GenericObjectTreeNode getGenericObjectTreeNode(Long genericObjectId, UID module) throws CommonFinderException {
		final GenericObjectTreeNode result = newGenericObjectTreeNode(genericObjectId, module, null, null, null, null);
		assert result != null;
		return result;
	}

	/**
	 * Note that user rights are ignored here.
	 * @param iGenericObjectId
	 * @param moduleId the module id
	 * @param iRelationId
	 * @param relationtype
	 * @param direction
	 * @return a new tree node for the generic object with the given id.
	 * @throws CommonFinderException if the object doesn't exist (anymore).
	 * @postcondition result != null
	 */
	@Override
	public GenericObjectTreeNode newGenericObjectTreeNode(Long iGenericObjectId,
			UID moduleId, Long iRelationId, SystemRelationType relationtype, RelationDirection direction, String customUsage) throws CommonFinderException {

		// @todo 1. write/use LOFB method that doesn't require the module id
		// @todo 2. Fix BUG: getWithDependants() throws a CommonPermissionException, even if bIgnoreUser == true

		final GenericObjectFacadeLocal gofacade = this.getGenericObjectFacade();

		//final int iModuleId = gofacade.getModuleContainingGenericObject(iGenericObjectId);

		final GenericObjectWithDependantsVO gowdvo = getWithDependants(gofacade, moduleId, iGenericObjectId);

		final GenericObjectTreeNode result = GenericObjectTreeNodeFactory.getInstance().newTreeNode(gowdvo, AttributeCache.getInstance(), 
				serverParameterProvider, iRelationId, relationtype, direction, getCurrentUserName(), customUsage);
		
		assert result != null;
		return result;
	}

	/**
	 * @param lofacade
	 * @param moduleUid
	 * @param iGenericObjectId
	 * @return the generic object with the given id, along with necessary attributes and dependants.
	 */
	private static GenericObjectWithDependantsVO getWithDependants(GenericObjectFacadeLocal lofacade, UID moduleUid, Long iGenericObjectId)
			throws CommonFinderException {
		// WORKAROUND: Load only necessary attributes (and to avoid slow calculated attributes):
		// @todo move this workaround to GenericObjectFacadeBean

		final CollectableSearchExpression clctexpr = new CollectableSearchExpression(
				SearchConditionUtils.getCollectableSearchConditionForIds(Collections.singletonList(iGenericObjectId)));

		final List<GenericObjectWithDependantsVO> lstlowdcvo = lofacade.getGenericObjects(moduleUid, clctexpr,
				getAttributeIdsRequiredForGenericObjectTreeNode(moduleUid), getSubEntityNamesRequiredForGenericObjectTreeNode(moduleUid), ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY), true);

		switch (lstlowdcvo.size()) {
			case 0:
				throw new CommonFinderException();
			case 1:
				return lstlowdcvo.get(0);
			default:
				throw new CommonFatalException(lstlowdcvo.size() + " objects found while one was expected.");
		}
	}

	private static Set<UID> getAttributeIdsRequiredForGenericObjectTreeNode(UID moduleId) {
		// final MetaProvider mprov = MetaProvider.getInstance();
		// final EntityMeta meta = mprov.getEntity(moduleId);
		//final AttributeProvider attrprovider = AttributeCache.getInstance();

		final Set<UID> result = new HashSet<UID>();
		result.add(SF.SYSTEMIDENTIFIER.getUID(moduleId));
		result.add(SF.PROCESS.getUID(moduleId));
		result.add(SF.STATE.getUID(moduleId));

		final List<EntityMeta> lstModules = new ArrayList<EntityMeta>();
		if (moduleId == null) {
			//choose all modules for finding required attributes for tree labels (tree node for generic search)
			lstModules.addAll(Modules.getInstance().getModules());
		} else {
			//choose requested module for finding required attributes for tree labels
			lstModules.add(Modules.getInstance().getModule(moduleId));
		}

		for (final EntityMeta mdvo : lstModules) {
			// String treeView = mdvo.getFieldValue("treeview", String.class);
			String treeView = mdvo.getLocaleResourceIdForTreeView();
			if (treeView != null) {
				result.addAll(
					CollectionUtils.transform(
						StringUtils.getFieldsFromTreeViewPattern(treeView),
						new Transformer<String, UID>() {
							@Override
							public UID transform(String s) {
								return new UID(s);
							}}
						));
			}
		}
		return result;
	}

	@Deprecated
	private static Set<UID> getSubEntityNamesRequiredForGenericObjectTreeNode(UID iModuleId) {
		// we need no dependend subforms here. get them on demand as child subnode.
		/*final Set<String> result = new HashSet<String>();
		final MasterDataVO mdcvoModule = Modules.getInstance().getModuleById(iModuleId);
		
		final String base = (String)mdcvoModule.getField("entity");
		final Collection<EntityTreeViewVO> etvs = Modules.getInstance().getSubnodesETV(base);
		for (EntityTreeViewVO etv : etvs) {
			if (etv.isActive())
				result.add(etv.getEntity());
		}
		return result;*/
		return Collections.emptySet();
	}

	/**
	 * gets the list of sub nodes for a specific generic object tree node.
	 * Note that there is a specific method right on this method.
	 * @param node tree node of type generic object tree node
	 * @return list of sub nodes for given tree node
	 * @throws CommonPermissionException
	 * @postcondition result != null
	 */
	public List<TreeNode> getSubNodesIgnoreUser(GenericObjectTreeNode node) {
		final List<TreeNode> result = new ArrayList<TreeNode>();
		final EntityMeta mdcvoModule = Modules.getInstance().getModule(node.getEntityUID());
		//add relations
		if((Boolean)mdcvoModule.isTreeRelation()) {
		  result.addAll(getRelatedSubNodes(node, RelationDirection.FORWARD));
		  result.addAll(getRelatedSubNodes(node, RelationDirection.REVERSE));
		  Collections.sort(result, new GenericObjectTreeNodeChildrenComparator());
		}

		// add groups
		if((Boolean)mdcvoModule.isTreeGroup()) {
		  result.addAll(getGroupSubNodes(node));
		}

		// final String base = (String)mdcvoModule.getFieldValue("entity");
		final Collection<MasterDataVO<?>> mds = Modules.getInstance().getSubnodesMD(node.getEntityUID());
		final Collection<EntityTreeViewVO> etvs = Modules.getInstance().getSubnodesETV(node.getEntityUID());
		subformSubnodes(result, node, mds, etvs);
		return result;
	}

	private void subformSubnodes(final List<TreeNode> result, final TreeNode node,
		final Collection<MasterDataVO<?>> mds, final Collection<EntityTreeViewVO> etvs)
	{
		final Iterator<MasterDataVO<?>> it1 = mds.iterator();
		final Iterator<EntityTreeViewVO> it2 = etvs.iterator();
		final NavigableMap<Integer,List<? extends TreeNode>> subforms = new TreeMap<Integer,List<? extends TreeNode>>();
		// add subnodes defined in the module meta data
		while (it1.hasNext()) {
			final MasterDataVO<?> mdvoSub = it1.next();
			final EntityTreeViewVO etv = it2.next();
			
			assert etv.getEntity().equals(mdvoSub.getFieldUid(E.ENTITYSUBNODES.entity));
			assert etv.getField().equals(mdvoSub.getFieldUid(E.ENTITYSUBNODES.field));
			
			// This seems to be all right - except for bmw-fdm. (tp)
			/*
			assert IdUtils.equals(etv.getOriginentityid(), mdvoSub.getId()) 
				: "org: " + etv.getOriginentityid() + " sub: " + mdvoSub.getIntId();
			 */

			final UID entity = etv.getEntity();
			final UID field = etv.getField();
			final String foldername = etv.getFoldername();
			final boolean active = etv.isActive();
			final Integer sortOrder = etv.getSortOrder() == null ? Integer.valueOf(0) : etv.getSortOrder();

			if (node instanceof GenericObjectTreeNode) {
				final GenericObjectTreeNode gotn = (GenericObjectTreeNode) node;
				// check whether the user has the right to see the subform data
				Permission permission = SecurityCache.getInstance().getSubForm(
					getCurrentUserName(), entity).get(gotn.getStateUID());
				if (permission == null || !permission.includesReading()) {
					continue;
				}
			}

			if (active) {
				if(!org.apache.commons.lang.StringUtils.isBlank(foldername)) {
					if (node instanceof GenericObjectTreeNode 
							|| node instanceof MasterDataTreeNode) { // only allow Treenodes of those types.
						final SubFormTreeNode<?> treenode = new SubFormTreeNode(null, node, mdvoSub);
						treenode.getSubNodes();
						subforms.put(sortOrder, Collections.singletonList(treenode));
					}
				}
				else {
					if(Modules.getInstance().isModule(entity)) {
						subforms.put(sortOrder, getModuleSubNodes(node, entity, field));
					}
					else {
						subforms.put(sortOrder, getMasterDataSubNodes(node, entity, field));
					}
				}
			}
		}
		for (List<? extends TreeNode> l: subforms.values()) {
			result.addAll(l);
		}
	}

	/**
	 * get subnodes of type entityUid where the valueId of fieldUid corresponds to node.getId()
	 * @param node
	 * @param entityUid
	 * @param fieldUid
	 * @return
	 * @throws NoSuchElementException
	 * @throws CommonFatalException
	 */
	private List<TreeNode> getModuleSubNodes(TreeNode node, final UID entityUid, final UID fieldUid) throws NoSuchElementException {
		final Object nodeId = node.getId();
		if (nodeId == null) {
			return Collections.emptyList();
		}
		CollectableComparison cond = SearchConditionUtils.newKeyComparison(fieldUid, ComparisonOperator.EQUAL, nodeId);

		List<Long> lstIds = getGenericObjectFacade().getGenericObjectIds(entityUid, cond);

		List<TreeNode> result = CollectionUtils.transform(lstIds, new Transformer<Long, TreeNode>() {

			@Override
			public TreeNode transform(Long genericObjectId) {
				try {
					GenericObjectTreeNode node = newGenericObjectTreeNode(genericObjectId, entityUid, null, null, null, null);
					return node;
				}
				catch(CommonFinderException e) {
					throw new NuclosFatalException(e);
				}
			}
		});
		return result;
	}

	/**
	 * Note that user rights are always ignored here.
	 * @param node
	 * @param direction
	 * @return the given node's subnodes related in the given direction (excluding GenericObjectTreeNode.RELATIONTYPE_INVOICE_OF).
	 * @throws CommonPermissionException
	 */
	private List<TreeNode> getRelatedSubNodes(GenericObjectTreeNode node, final RelationDirection direction) {
		final List<TreeNode> result = new ArrayList<TreeNode>();

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		// DbFrom r = query.from("T_UD_GO_RELATION").alias("r");
		DbFrom<Long> r = query.from(E.GENERICOBJECTRELATION, "r");
		// DbFrom l = query.from("T_UD_GENERICOBJECT").alias("l");
		DbFrom<Long> l = query.from(E.GENERICOBJECT, "l");
		// DbColumnExpression<Integer> genericObject1 = r.baseColumn(direction.isForward() ? "INTID_T_UD_GO_1" : "INTID_T_UD_GO_2", Integer.class);
		DbColumnExpression<Long> genericObject1 = r.baseColumn(direction.isForward() ? 
				E.GENERICOBJECTRELATION.source : E.GENERICOBJECTRELATION.destination);
		// DbColumnExpression<Integer> genericObject2 = r.baseColumn(direction.isForward() ? "INTID_T_UD_GO_2" : "INTID_T_UD_GO_1", Integer.class);
		DbColumnExpression<Long> genericObject2 = r.baseColumn(direction.isForward() ? 
				E.GENERICOBJECTRELATION.destination : E.GENERICOBJECTRELATION.source);
		query.multiselect(
			r.baseColumn(E.GENERICOBJECTRELATION),
			l.baseColumn(E.GENERICOBJECT),
			// r.baseColumn("STRRELATIONTYPE", String.class),
			r.baseColumn(E.GENERICOBJECTRELATION.relationType),
			// l.baseColumn("INTID_T_MD_MODULE", Integer.class)
			l.baseColumn(E.GENERICOBJECT.module)
			);
		query.where(builder.and(
			builder.equal(genericObject2, l.baseColumn(E.GENERICOBJECT)),
			// node.getParentId() != null ? builder.equal(genericObject2, node.getParentId()).not() : builder.alwaysTrue(),
			builder.alwaysTrue(),
			builder.equalValue(genericObject1, node.getId())//,
			));
		// order by type ???

		for (DbTuple tuple : dataBaseHelper.getDbAccess().executeQuery(query.distinct(true))) {
			final Long iRelationId = tuple.get(0, Long.class);
			final Long iGenericObjectId = tuple.get(1, Long.class);

			final String relationType = tuple.get(2, String.class);
			final UID moduleId = tuple.get(3, UID.class);

			SystemRelationType systemRelationType = SystemRelationType.findSystemRelationType(relationType);
			if (systemRelationType != null) {
				// predecessor or part-of relation:
				try {
					result.add(newGenericObjectTreeNode(iGenericObjectId, moduleId, iRelationId, systemRelationType, direction, null));
				}
				catch (CommonFinderException ex) {
					// the object doesn't exist anymore - ignore.
				}
			}
			else {
				try {
					final GenericObjectTreeNode nodeRelatedObject = newGenericObjectTreeNode(iGenericObjectId, moduleId, null, null, null, null);
					String label = getRelationTypeLabel(relationType);
					result.add(new RelationTreeNode(iRelationId, label, relationType, direction, nodeRelatedObject));
				}
				catch (CommonFinderException ex) {
					// the object doesn't exist anymore - ignore.
				}
			}
		}

		return result;
	}

	private String getRelationTypeLabel(String relationType){
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<String> query = builder.createQuery(String.class);
		// DbFrom t = query.from("T_MD_RELATIONTYPE").alias(SystemFields.BASE_ALIAS);
		DbFrom<UID> t = query.from(E.RELATIONTYPE);
		// query.select(t.baseColumn("STRLOCALERESOURCEID", String.class));
		query.select(t.baseColumn(E.RELATIONTYPE.labelres));
		// query.where(builder.equal(t.baseColumn("STRRELATIONTYPE", String.class), relationType));
		query.where(builder.equalValue(t.baseColumn(E.RELATIONTYPE.name), relationType));
		String resourceId = CollectionUtils.getFirst(dataBaseHelper.getDbAccess().executeQuery(query));
		if (resourceId != null) {
			SpringLocaleDelegate.getInstance().getMessage(resourceId, relationType);
		}
		return null;
	}

	private List<TreeNode> getGroupSubNodes(GenericObjectTreeNode node) {
		final List<TreeNode> result = new ArrayList<TreeNode>();

		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		// DbFrom l = query.from("T_UD_GO_GROUP").alias("l");
		DbFrom l = query.from(E.GENERICOBJECTGROUP, "l");
		// DbFrom g = l.join("T_UD_GROUP", JoinType.INNER).alias("g").on("INTID_T_UD_GROUP", "INTID", Integer.class);
		DbFrom<UID> g = l.joinOnJoinedPk(E.GROUP, JoinType.INNER, E.GENERICOBJECTGROUP.group, "g");
		// DbFrom t = g.join("T_MD_GROUPTYPE", JoinType.INNER).alias(SystemFields.BASE_ALIAS).on("INTID_T_MD_GROUPTYPE", "INTID", Integer.class);
		DbFrom<UID> t = g.joinOnBasePk(E.GROUPTYPE, JoinType.INNER, E.GROUP.grouptype, SystemFields.BASE_ALIAS);
		query.multiselect(
			// l.baseColumn("INTID_T_UD_GROUP", Integer.class),
			l.baseColumn(E.GENERICOBJECTGROUP.group),
			// g.baseColumn("STRGROUP", String.class),
			g.baseColumn(E.GROUP.name),
			// g.baseColumn("STRDESCRIPTION", String.class),
			g.baseColumn(E.GROUP.description),
			// t.baseColumn("STRNAME", String.class)
			t.baseColumn(E.GROUPTYPE.name),
			// added for getting UID
			t.baseColumn(E.GROUPTYPE)
			);
		// query.where(builder.equal(l.baseColumn("INTID_T_UD_GROUP", Integer.class), node.getId()));
		query.where(builder.equalValue(l.baseColumn(E.GENERICOBJECTGROUP), node.getId()));
		// query.orderBy(builder.asc(g.baseColumn("STRGROUP", String.class)), builder.desc(g.baseColumn("DATVALIDFROM", Date.class)));
		query.orderBy(builder.asc(g.baseColumn(E.GROUP.name)), builder.desc(g.baseColumn(E.GROUP.validFrom)));

		// TODO: Do we need the grouptype name. If not the join is not needed. (tp)
		for (DbTuple tuple : dataBaseHelper.getDbAccess().executeQuery(query)) {
			final GroupTreeNode nodeGroupDefined = new GroupTreeNode(
				tuple.get(0, UID.class), tuple.get(1, String.class),
				tuple.get(4, UID.class), tuple.get(2, String.class));
			nodeGroupDefined.getSubNodes();
			result.add(nodeGroupDefined);
		}
		return result;
	}

	// group:

	/**
	 * method to get a group tree node for a specific group
	 * @param iId id of group to get tree node for
	 * @return group tree node for given id
	 * @postcondition result != null
	 */
	public GroupTreeNode getGroupTreeNode(final UID iId, final boolean bLoadSubNodes) throws CommonFinderException {
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom<UID> t = query.from(E.GROUP);
		DbFrom<UID> fk1 = t.joinOnBasePk(E.GROUPTYPE, JoinType.INNER, E.GROUP.grouptype, "fk1");
		query.multiselect(
			t.baseColumn(E.GROUP.name),
			fk1.baseColumn(E.GROUPTYPE.name),
			// added for getting UID
			fk1.basePk(),
			t.baseColumn(E.GROUP.description)
			);
		query.where(builder.equalValue(t.basePk(), iId));

		DbTuple tuple;
		try {
			tuple = dataBaseHelper.getDbAccess().executeQuerySingleResult(query);
		} catch (DbInvalidResultSizeException e) {
			throw new NuclosFatalException("treenode.error.missing.group");//"Die Gruppe existiert nicht mehr.");
		}

		// TODO: Do we need the grouptype name. If not the join is not needed. (tp)
		final GroupTreeNode result = new GroupTreeNode(iId, tuple.get(0, String.class),
			tuple.get(2, UID.class), tuple.get(3, String.class));

		if (bLoadSubNodes) {
			result.getSubNodes();
		}
		return result;
	}

	public NucletTreeNode getNucletTreeNode(UID iId) throws CommonFinderException {
		try {
			EntityObjectVO<UID> eovo = NucletDalProvider.getInstance().getEntityObjectProcessor(E.NUCLET).getByPrimaryKey(iId);
			return new NucletTreeNode(eovo, false);
		} catch (Exception ex) {
			throw new CommonFinderException();
		}
	}

	/**
	 * method to get a masterdata tree node for a specific masterdata record
	 * @param iId id of masterdata record to get tree node for
	 * @return masterdata tree node for given id
	 * @throws CommonPermissionException
	 * @postcondition result != null
	 */
	@Override
	public <PK> MasterDataTreeNode<PK> getMasterDataTreeNode(final PK iId, final UID entityUid, boolean bLoadSubNodes) throws CommonFinderException, CommonPermissionException {
		final MasterDataVO<PK> mdvo = getMasterDataFacade().get(entityUid, iId);
		final MasterDataTreeNode<PK> result = new DefaultMasterDataTreeNode<PK>(mdvo);

		if (bLoadSubNodes) {
			result.getSubNodes();
		}
		assert result != null;
		return result;
	}

	public <PK> SubFormEntryTreeNode<PK> getSubFormEntryTreeNode(PK iId, UID entityUid, boolean bLoadSubNodes) throws CommonFinderException, CommonPermissionException {
		final MasterDataVO<PK> mdvo = getMasterDataFacade().get(entityUid, iId);
		final SubFormEntryTreeNode<PK> result = new SubFormEntryTreeNode<PK>(mdvo);

		if (bLoadSubNodes) {
			result.getSubNodes();
		}
		assert result != null;
		return result;
	}

	/**
	 * @param node
	 * @return the subnodes for the given node.
	 * @postcondition result != null
	 */
	public List<GenericObjectTreeNode> getSubNodes(GroupTreeNode node) {
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom<UID> grp = query.from(E.GENERICOBJECTGROUP, "grp");
		DbFrom<?> gob = grp.joinOnJoinedPk(E.GENERICOBJECT, JoinType.INNER, E.GENERICOBJECTGROUP.genericObject, "gob");
		query.multiselect(
			// grp.baseColumn(CommonDatasourceFacade.REF_ENTITY, Integer.class),
			grp.baseColumn(E.GENERICOBJECTGROUP.genericObject),
			// gob.baseColumn("INTID_T_MD_MODULE", Integer.class)
			gob.baseColumn(E.GENERICOBJECT.module)
			);
		// query.where(builder.equal(grp.baseColumn("INTID_T_UD_GROUP", Integer.class), node.getId()));
		query.where(builder.equalValue(grp.baseColumn(E.GENERICOBJECTGROUP.group), node.getId()));
		// query.orderBy(builder.asc(grp.baseColumn("DATCREATED", Date.class)));
		query.orderBy(builder.asc(grp.baseColumn(SF.CREATEDAT)));

		final List<GenericObjectTreeNode> result = new ArrayList<GenericObjectTreeNode>();
		for (DbTuple tuple : dataBaseHelper.getDbAccess().executeQuery(query)) {
			try {
				result.add(getGenericObjectTreeNode(tuple.get(0, Long.class), tuple.get(1, UID.class)));
			}
			catch (CommonFinderException ex) {
				// the object doesn't exist anymore - ignore.
			}
		}
		Collections.sort(result);
		return result;
	}

	public List<TreeNode> getSubNodes(NucletTreeNode node) {
		List<TreeNode> result = new ArrayList<TreeNode>();
		if (node.isShowDependeces()) {

			CollectableSearchCondition cond = SearchConditionUtils.newUidComparison(
				// E.NUCLETDEPENDENCE, 
				E.NUCLETDEPENDENCE.nuclet,
				ComparisonOperator.EQUAL,
				(UID) node.getId());

			List<NucletTreeNode> nucletNodes = new ArrayList<NucletTreeNode>();
			for (EntityObjectVO<UID> eoDependence : NucletDalProvider.getInstance().getEntityObjectProcessor(E.NUCLETDEPENDENCE).getBySearchExpression(
					new CollectableSearchExpression(cond))) {
				EntityObjectVO<UID> eoNuclet = NucletDalProvider.getInstance().getEntityObjectProcessor(E.NUCLET).getByPrimaryKey(
						eoDependence.getFieldUid(E.NUCLETDEPENDENCE.nucletDependence));
				nucletNodes.add(new NucletTreeNode(eoNuclet, true));
			}
			result.addAll(CollectionUtils.sorted(nucletNodes, new Comparator<NucletTreeNode>() {
				@Override
				public int compare(NucletTreeNode o1, NucletTreeNode o2) {
					return LangUtils.compare(o1.getLabel(), o2.getLabel());
				}}));
		} else {
			result.addAll(getNucletContentTypes(node.getId()));
		}
		return result;
	}

	private List<NucletContentTreeNode> getNucletContentTypes(UID nucletId) {
		List<NucletContentTreeNode> result = new ArrayList<NucletContentTreeNode>();
		for (EntityMeta<UID> ne : AbstractNucletContentEntryTreeNode.getNucletContentEntities()) {
			if (E.REPORT.equals(ne)) {
				result.add(new ReportNucletContentTreeNode(nucletId));
			}
			else {
				result.add(new NucletContentTreeNode(nucletId, ne));	
			}
		}
		return result;
	}

	/**
	 * @param node
	 * @return the subnodes for the given node.
	 * @postcondition result != null
	 */
	@Override
	public List<TreeNode> getSubnodes(DefaultMasterDataTreeNode<?> node) {
		final MetaProvider mprov = MetaProvider.getInstance();
		final NodeCache nc = NodeCache.getInstance();
		final List<TreeNode> result = new ArrayList<TreeNode>();
		final EntityMeta mdMeta = mprov.getEntity(node.getEntityUID());
		final Collection<MasterDataVO<?>> colSubNodes = nc.getSubnodesMD(
			node.getEntityUID(), mdMeta.getUID());
		final Collection<EntityTreeViewVO> subnodes = nc.getSubnodesETV(
			node.getEntityUID(), mdMeta.getUID());

		subformSubnodes(result, node, colSubNodes, subnodes);
		return result;
	}

	/**
	 * @param node
	 * @return the subnodes for the given node.
	 * @postcondition result != null
	 */
	public List<AbstractNucletContentEntryTreeNode> getSubNodes(NucletContentTreeNode node) {
		final List<AbstractNucletContentEntryTreeNode> result = new ArrayList<AbstractNucletContentEntryTreeNode>();

		final MetaProvider mprov = MetaProvider.getInstance();
		final EntityMeta<?> eMeta = mprov.getEntity(node.getEntityUID());
		// FieldMeta efMetaNuclet = mprov.getEntityField(eMeta.getEntity(), AbstractNucletContentEntryTreeNode.FOREIGN_FIELD_TO_NUCLET);
		final FieldMeta<?> efMetaNuclet = getRefToNuclet(eMeta, true);

		CollectableSearchCondition cond = SearchConditionUtils.newUidComparison(
			// eMeta.getEntity(),
			efMetaNuclet,
			ComparisonOperator.EQUAL,
			node.getNuclet());

		for (EntityObjectVO<?> eo : NucletDalProvider.getInstance().getEntityObjectProcessor(eMeta).getBySearchExpression(
				new CollectableSearchExpression(cond))) {
			result.add(getNucletContentEntryNode(eo));
		}

		return sortAbstractNucletContentEntryTreeNodes(result);
	}
	
	private FieldMeta<?> getRefToNuclet(EntityMeta<?> em, boolean throwIfNotFound) {
		for (FieldMeta<?> fm: em.getFields()) {
			final UID fe = fm.getForeignEntity();
			if (fe != null && E.NUCLET.checkEntityUID(fe)) {
				return fm;
			}
		}
		if (throwIfNotFound) throw new NuclosFatalException();
		return null;
	}

	public List<AbstractNucletContentEntryTreeNode> getNucletContent(NucletTreeNode node) {
		final List<AbstractNucletContentEntryTreeNode> result = new ArrayList<AbstractNucletContentEntryTreeNode>();

		for (NucletContentTreeNode contentTypeNode : getNucletContentTypes(node.getId())) {
			result.addAll(getSubNodes(contentTypeNode));
		}
		return result;
	}

	public List<NucletTreeNode> getSubNodes(NuclosInstanceTreeNode node) {
		final Collection<NucletTreeNode> result = new ArrayList<NucletTreeNode>();
//		final MetaProvider mprov = MetaProvider.getInstance();
		
//		EntityMeta<UID> eMetaDependence = E.NUCLETDEPENDENCE;
		FieldMeta<UID> efMetaDependence = E.NUCLETDEPENDENCE.nucletDependence;

		for (EntityObjectVO<UID> eoNuclet : NucletDalProvider.getInstance().getEntityObjectProcessor(E.NUCLET).getAll()) {

			CollectableSearchCondition cond = org.nuclos.common.SearchConditionUtils.newKeyComparison(
				// eMetaDependence.getEntity(),
				efMetaDependence,
				ComparisonOperator.EQUAL,
				eoNuclet.getPrimaryKey());

			if (NucletDalProvider.getInstance().getEntityObjectProcessor(E.NUCLETDEPENDENCE)
				.count(new CollectableSearchExpression(cond)) == 0) {
				// is root Nuclet
				result.add(new NucletTreeNode(eoNuclet, true));
			}
		}

		return CollectionUtils.sorted(result, new Comparator<NucletTreeNode>() {
			@Override
			public int compare(NucletTreeNode o1, NucletTreeNode o2) {
				return LangUtils.compare(o1.getLabel(), o2.getLabel());
			}});
	}

	public List<AbstractNucletContentEntryTreeNode> getAvailableNucletContents() {
		final MetaProvider mprov = MetaProvider.getInstance();
		final List<AbstractNucletContentEntryTreeNode> result = new ArrayList<AbstractNucletContentEntryTreeNode>();
		for (EntityMeta<UID> ne : AbstractNucletContentEntryTreeNode.getNucletContentEntities()) {
			// FieldMeta efMetaNuclet = MetaProvider.getInstance().getEntityField(eMeta.getEntity(), 
			// 		AbstractNucletContentEntryTreeNode.FOREIGN_FIELD_TO_NUCLET);
			FieldMeta<?> efMetaNuclet = getRefToNuclet(ne, true);

			CollectableSearchCondition cond = SearchConditionUtils.newIsNullCondition(
				// eMeta.getEntity(),
				efMetaNuclet);
			
			if (E.WORKSPACE.equals(ne)) {
				cond = SearchConditionUtils.and(cond, SearchConditionUtils.newIsNullCondition(
						// ne, 
						E.WORKSPACE.user));
			}

			List<AbstractNucletContentEntryTreeNode> nodes = new ArrayList<AbstractNucletContentEntryTreeNode>();
			for (EntityObjectVO<?> eo : NucletDalProvider.getInstance().getEntityObjectProcessor(ne).getBySearchExpression(
					new CollectableSearchExpression(cond))) {
				nodes.add(getNucletContentEntryNode(eo));
			}
			result.addAll(sortAbstractNucletContentEntryTreeNodes(nodes));
		}
		return result;
	}
	
	public List<ErrorMessage> updateNucletContents(UID nucletId, Set<AbstractNucletContentEntryTreeNode> contentsToAdd, Set<AbstractNucletContentEntryTreeNode> contentsToRemove) throws NuclosBusinessException {
		
		if (contentsToAdd != null && !contentsToAdd.isEmpty()) {
			addNucletContents(nucletId, contentsToAdd, false);
		}
		if (contentsToRemove != null && !contentsToRemove.isEmpty()) {
			removeNucletContents(contentsToRemove, false);
		}
		
		return null;
	}
	
	public List<ErrorMessage> addNucletContents(UID nucletId, Set<AbstractNucletContentEntryTreeNode> contents) throws NuclosBusinessException {
		return addNucletContents(nucletId, contents, true);
	}


	private List<ErrorMessage> addNucletContents(UID nucletId, Set<AbstractNucletContentEntryTreeNode> contents, boolean codeCheck) throws NuclosBusinessException {
		final CacheInvalidator ci = new CacheInvalidator();
		final MetaProvider mprov = MetaProvider.getInstance();
		boolean rebuildBOs = false;
		for (AbstractNucletContentEntryTreeNode node : contents) {
			if (E.ENTITY == node.getEntity()) {
				rebuildBOs = true;
			}
			final JdbcEntityObjectProcessor<UID> processor = NucletDalProvider.getInstance().getEntityObjectProcessor(node.getEntity());
			final EntityObjectVO<UID> eo = processor.getByPrimaryKey(node.getId()); //reload the content, no version check here!
			final EntityMeta<?> em = mprov.getEntity(eo.getDalEntity());
			final FieldMeta<?> fMeta = getRefToNuclet(em, true);
			final UID nucletRef = eo.getFieldUid(fMeta.getUID());
			
			if (nucletRef != null) {
				if (LangUtils.equals(nucletId, nucletRef)) {
					continue;
				} else {
					throw new NuclosBusinessException("treenode.facade.businessexception.1");
				}
			}

			eo.setFieldUid(fMeta.getUID(), nucletId);
			eo.flagUpdate();
			DalUtils.updateVersionInformation(eo, getCurrentUserName());
			processor.insertOrUpdate(eo);

			ci.handleNode(node);
		}

		ci.run();

		return null;
	}
	
	public RemoveContentsResult removeNucletContents(Set<AbstractNucletContentEntryTreeNode> contents) {
		return removeNucletContents(contents, true);
	}

	private RemoveContentsResult removeNucletContents(Set<AbstractNucletContentEntryTreeNode> contents, boolean codeCheck) {
		boolean result = false;
		final CacheInvalidator ci = new CacheInvalidator();
		final MetaProvider mprov = MetaProvider.getInstance();

		for (AbstractNucletContentEntryTreeNode node : contents) {

			final JdbcEntityObjectProcessor<UID> processor = NucletDalProvider.getInstance().getEntityObjectProcessor(node.getEntity());
			final EntityObjectVO<UID> eo = processor.getByPrimaryKey(node.getId()); //reload the content, no version check here!
			final EntityMeta<?> em = mprov.getEntity(eo.getDalEntity());
			final FieldMeta<?> fMeta = getRefToNuclet(em, true);
			final UID nucletRef = eo.getFieldUid(fMeta.getUID());


			if (nucletRef != null) {
				result = true;
				eo.setFieldUid(fMeta.getUID(), null);
				eo.flagUpdate();
				DalUtils.updateVersionInformation(eo, getCurrentUserName());
				processor.insertOrUpdate(eo);
			}

			if (result)
				ci.handleNode(node);
		}

		ci.run();

		return new RemoveContentsResult(result, null);
	}

	private class CacheInvalidator {
		boolean invalidateRuleCache = false;
		boolean invalidateDatasourceCache = false;
		public void handleNode(AbstractNucletContentEntryTreeNode node) {
			if (node.getEntity() == E.RULE ||
				node.getEntity() == E.TIMELIMITRULE ||
				node.getEntity() == E.CODE ||
				node.getEntity() == E.SERVERCODE) {
				invalidateRuleCache = true;
			}
			if (node.getEntity() == E.DATASOURCE ||
				node.getEntity() == E.DYNAMICENTITY ||
				node.getEntity() == E.VALUELISTPROVIDER ||
				node.getEntity() == E.RECORDGRANT ||
				node.getEntity() == E.CHART) {
				invalidateDatasourceCache = true;
			}
		}
		public void run() {
			if (invalidateDatasourceCache) DatasourceCache.getInstance().invalidate();
		}
	}

	private List<AbstractNucletContentEntryTreeNode> sortAbstractNucletContentEntryTreeNodes(List<AbstractNucletContentEntryTreeNode> nodes) {
		return CollectionUtils.sorted(nodes, new AbstractNucletContentEntryTreeNode.Comparator());
	}

	@Override
	public AbstractNucletContentEntryTreeNode getNucletContentEntryNode(EntityMeta<UID> entity, UID eoId) {
		return getNucletContentEntryNode(NucletDalProvider.getInstance().getEntityObjectProcessor(entity).getByPrimaryKey(eoId));
	}

	private AbstractNucletContentEntryTreeNode getNucletContentEntryNode(EntityObjectVO<?> eo) {
		if (eo == null) {
			throw new IllegalArgumentException("eo must not be null");
		}
		
		UID entityUID = eo.getDalEntity();
		if (!E.isNuclosEntity(entityUID)) {
			throw new IllegalArgumentException("entity object must be nuclos entity");
		}
		if (E.ENTITY.checkEntityUID(entityUID)) {
				return new NucletContentEntityTreeNode((EntityObjectVO<UID>) eo);
		}
		else if (E.CUSTOMCOMPONENT.checkEntityUID(entityUID)) {
				return new NucletContentCustomComponentTreeNode((EntityObjectVO<UID>)eo);
		}
		else if (E.RULE.checkEntityUID(entityUID)) {
				return new NucletContentRuleTreeNode((EntityObjectVO<UID>) eo);
		}
		else if (E.PROCESS.checkEntityUID(entityUID)) {
				return new NucletContentProcessTreeNode((EntityObjectVO<UID>) eo);
		}
		else {
				return new DefaultNucletContentEntryTreeNode(eo);
		}
	}

	public <PK> DynamicTreeNode<PK> getDynamicTreeNode(TreeNode node, MasterDataVO<PK> mdVO) {
		return new DynamicTreeNode<PK>(null, node, mdVO);
	}

	public <PK> SubFormTreeNode<PK> getSubFormTreeNode(TreeNode node, MasterDataVO<PK> mdVO) {
		return new SubFormTreeNode<PK>(null, node, mdVO);
	}

	public List<TreeNode> getSubNodesForDynamicTreeNode(TreeNode node, FieldMeta<?> mdVO) {
		final UID entityUid = mdVO.getEntity();
		final UID fieldUid = mdVO.getUID();
		final List<TreeNode> result = new ArrayList<TreeNode>();
		if(Modules.getInstance().isModule(mdVO.getEntity())) {
			result.addAll(getModuleSubNodes(node, entityUid, fieldUid));
		}
		else {
			result.addAll(getMasterDataSubNodes(node, entityUid, fieldUid));
		}

		return result;
	}

    public <PK> List<SubFormEntryTreeNode<PK>> getSubNodesForSubFormTreeNode(TreeNode node, final FieldMeta mdVO) {
		final Collection<MasterDataVO<PK>> colmdvo = getMasterDataFacade().getDependantMasterDataPk(
				mdVO.getEntity(), mdVO.getUID(), (PK) node.getId());
		final Collection<SubFormEntryTreeNode<PK>> colResult = CollectionUtils.transform(colmdvo, new Transformer<MasterDataVO<PK>, SubFormEntryTreeNode<PK>>() {
			@Override
			public SubFormEntryTreeNode<PK> transform(MasterDataVO<PK> i) {
				return new SubFormEntryTreeNode<PK>(i);
			}
		});

		final List<SubFormEntryTreeNode<PK>> result = new ArrayList<SubFormEntryTreeNode<PK>>(colResult);
		Collections.sort(result);
		return result;
	}

	/**
	 * get the masterdata subnodes for the given node
	 * @param node
	 * @param entityUid
	 * @param fieldUid
	 * @return
	 */
	private List<DefaultMasterDataTreeNode<UID>> getMasterDataSubNodes(TreeNode node, final UID entityUid, UID fieldUid) {
		final Collection<MasterDataVO<UID>> colmdvo = getMasterDataFacade().getDependantMasterDataPk(entityUid, fieldUid, (UID) node.getId());
		final Collection<DefaultMasterDataTreeNode<UID>> colResult = CollectionUtils.transform(colmdvo, new Transformer<MasterDataVO<UID>, DefaultMasterDataTreeNode<UID>>() {
			@Override
			public DefaultMasterDataTreeNode<UID> transform(MasterDataVO<UID> i) {
				return new DefaultMasterDataTreeNode<UID>(i);
			}
		});

		final List<DefaultMasterDataTreeNode<UID>> result = new ArrayList<DefaultMasterDataTreeNode<UID>>(colResult);
		Collections.sort(result);
		return result;

	}

	public List<GroupTreeNode> getSubNodes(GroupSearchResultTreeNode node) {
		final List<GroupTreeNode> result = new ArrayList<GroupTreeNode>();

		for (MasterDataVO<UID> mdvo : getMasterDataFacade().getMasterData(E.GROUP, 
				appendRecordGrants(node.getSearchCondition(), E.GROUP.getUID()), false)) {
			
			result.add(new GroupTreeNode(mdvo.getId(), mdvo.getFieldValue(E.GROUP.name),
					mdvo.getFieldUid(E.GROUP.grouptype), mdvo.getFieldValue(E.GROUPTYPE.description)));
		}

		Collections.sort(result);
		assert result != null;
		return result;
	}

	/**
	 * get the subnodes for a masterdata search result
	 * @param node
	 * @return the subnodes for the given node.
	 * @postcondition result != null
	 */
	@Override
	public <PK> List<DefaultMasterDataTreeNode<PK>> getSubNodes(MasterDataSearchResultTreeNode<PK> node) {
		final List<DefaultMasterDataTreeNode<PK>> result = new ArrayList<DefaultMasterDataTreeNode<PK>>();

		for (MasterDataVO<?> mdvo : getMasterDataFacade().getMasterData(node.getEntity(), 
				appendRecordGrants(node.getSearchCondition(), node.getEntity()), false)) {
			result.add(new DefaultMasterDataTreeNode<PK>((MasterDataVO<PK>) mdvo));
		}

		Collections.sort(result);
		assert result != null;
		return result;
	}


	/**
	 * method to get the list of sub nodes for a specific generic object search result tree node
	 * @param node tree node of type search result tree node
	 * @return list of sub nodes for given tree node
	 * @postcondition result != null
	 */
	public List<TreeNode> getSubNodes(EntitySearchResultTreeNode node) {
		final UID eId = node.getEntity();
		final int iMaxRowCount = serverParameterProvider.getIntValue(
				ParameterProvider.KEY_MAX_ROWCOUNT_FOR_SEARCHRESULT_IN_TREE, DEFAULT_ROWCOUNT_FOR_SEARCHRESULT);

		if (Modules.getInstance().isModule(eId)) {
			final AttributeProvider attrprovider = AttributeCache.getInstance();

			final List<TreeNode> result = new ArrayList<TreeNode>(Math.min(iMaxRowCount, DEFAULT_ROWCOUNT_FOR_SEARCHRESULT));
			// final EntityMeta iModuleId = Modules.getInstance().getModule(node.getEntity());
			final TruncatableCollection<GenericObjectWithDependantsVO> collgowdvo =
					getGenericObjectFacade().getRestrictedNumberOfGenericObjects(
							eId, appendRecordGrants(node.getSearchExpression(), 
							node.getEntity()), getAttributeIdsRequiredForGenericObjectTreeNode(eId), 
							getSubEntityNamesRequiredForGenericObjectTreeNode(eId), 
							ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY), iMaxRowCount);

			for (GenericObjectWithDependantsVO gowdvo : collgowdvo) {
				result.add(GenericObjectTreeNodeFactory.getInstance().newTreeNode(
						gowdvo, attrprovider, serverParameterProvider, null, null, null, getCurrentUserName(), null));
			}

			String sLabel = MessageFormat.format(getLocaleFacade().getResourceById(getLocaleFacade().getUserLocale(), "treenode.subnode.label"), 
					node.getLabel(), collgowdvo.size(), collgowdvo.totalSize());
			if (collgowdvo.isTruncated()) {
				node.setLabel(sLabel);
			}
			/** @todo OPTIMIZE: sort in the database! */
			//Collections.sort(result);

			assert result != null;
			return result;
		} else {
			final List<TreeNode> result = new ArrayList<TreeNode>(Math.min(iMaxRowCount, DEFAULT_ROWCOUNT_FOR_SEARCHRESULT));

			final TruncatableCollection<MasterDataVO<UID>> collmdvo = getMasterDataFacade().<UID>getMasterData(
					node.getEntity(), appendRecordGrants(node.getSearchExpression(), node.getEntity()).getSearchCondition(), false);
			for (MasterDataVO<UID> mdvo : collmdvo) {
				result.add(new DefaultMasterDataTreeNode<UID>(mdvo));
			}
			String sLabel = MessageFormat.format(getLocaleFacade().getResourceById(getLocaleFacade().getUserLocale(), "treenode.subnode.label"), 
					node.getLabel(), collmdvo.size(), collmdvo.totalSize());
			if (collmdvo.isTruncated()) {
				node.setLabel(sLabel);
					//node.getLabel() + " begrenzt auf " + collmdvo.size() + " von " + collmdvo.totalSize() + " Ergebnissen)");
			}

			//Collections.sort(result);
			assert result != null;
			return result;
		}
	}

	private static class GenericObjectTreeNodeChildrenComparator implements Comparator<TreeNode> {
		@Override
		public int compare(TreeNode tn1, TreeNode tn2) {
			int result = getOrder(tn1) - getOrder(tn2);
			if (result == 0) {
				if (tn1 instanceof GenericObjectTreeNode) {
					result = ((Comparable<GenericObjectTreeNode>) tn1).compareTo((GenericObjectTreeNode) tn2);
				}
				else if (tn1 instanceof RelationTreeNode) {
					result = ((Comparable<TreeNode>) tn1).compareTo(tn2);
				}
				else {
					throw new CommonFatalException("Cannot compare the given TreeNodes.");
				}
			}
			return result;
		}

		private int getOrder(TreeNode treenode) {
			return LangUtils.isInstanceOf(treenode, GenericObjectTreeNode.class) ? 1 : 2;
		}

	}	// inner class GenericObjectTreeNodeChildrenComparator

	@Override
	public <PK> List<TreeNode> getSubNodesForDynamicTreeNode(TreeNode node,	MasterDataVO<PK> mdVO) {
		final UID entityUid = mdVO.getEntityObject().getDalEntity();
		//TODO MULTINUCLET check if it is the right field
		final UID fieldUid = mdVO.getFieldUid(E.ENTITYSUBNODES.field);
		final List<TreeNode> result = new ArrayList<TreeNode>();
		if(Modules.getInstance().isModule(entityUid)) {
			result.addAll(getModuleSubNodes(node, entityUid, fieldUid));
		}
		else {
			result.addAll(getMasterDataSubNodes(node, entityUid, fieldUid));
		}

		return result;
	}

	@Override
	public <PK> List<SubFormEntryTreeNode<PK>> getSubNodesForSubFormTreeNode(TreeNode node, MasterDataVO<PK> mdVO) {
		// TODO Auto-generated method stub
		return null;
	}
}

