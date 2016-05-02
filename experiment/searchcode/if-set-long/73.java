/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.service.persistence;

import com.liferay.portal.NoSuchModelException;
import com.liferay.portal.NoSuchPermissionException;
import com.liferay.portal.kernel.bean.BeanReference;
import com.liferay.portal.kernel.cache.CacheRegistryUtil;
import com.liferay.portal.kernel.dao.jdbc.MappingSqlQuery;
import com.liferay.portal.kernel.dao.jdbc.MappingSqlQueryFactoryUtil;
import com.liferay.portal.kernel.dao.jdbc.RowMapper;
import com.liferay.portal.kernel.dao.jdbc.SqlUpdate;
import com.liferay.portal.kernel.dao.jdbc.SqlUpdateFactoryUtil;
import com.liferay.portal.kernel.dao.orm.EntityCacheUtil;
import com.liferay.portal.kernel.dao.orm.FinderCacheUtil;
import com.liferay.portal.kernel.dao.orm.FinderPath;
import com.liferay.portal.kernel.dao.orm.Query;
import com.liferay.portal.kernel.dao.orm.QueryPos;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.SQLQuery;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.InstanceFactory;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.CacheModel;
import com.liferay.portal.model.ModelListener;
import com.liferay.portal.model.Permission;
import com.liferay.portal.model.impl.PermissionImpl;
import com.liferay.portal.model.impl.PermissionModelImpl;
import com.liferay.portal.service.persistence.impl.BasePersistenceImpl;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The persistence implementation for the permission service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see PermissionPersistence
 * @see PermissionUtil
 * @generated
 */
public class PermissionPersistenceImpl extends BasePersistenceImpl<Permission>
	implements PermissionPersistence {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this class directly. Always use {@link PermissionUtil} to access the permission persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */
	public static final String FINDER_CLASS_NAME_ENTITY = PermissionImpl.class.getName();
	public static final String FINDER_CLASS_NAME_LIST_WITH_PAGINATION = FINDER_CLASS_NAME_ENTITY +
		".List1";
	public static final String FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION = FINDER_CLASS_NAME_ENTITY +
		".List2";
	public static final FinderPath FINDER_PATH_WITH_PAGINATION_FIND_BY_RESOURCEID =
		new FinderPath(PermissionModelImpl.ENTITY_CACHE_ENABLED,
			PermissionModelImpl.FINDER_CACHE_ENABLED, PermissionImpl.class,
			FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByResourceId",
			new String[] {
				Long.class.getName(),
				
			"java.lang.Integer", "java.lang.Integer",
				"com.liferay.portal.kernel.util.OrderByComparator"
			});
	public static final FinderPath FINDER_PATH_WITHOUT_PAGINATION_FIND_BY_RESOURCEID =
		new FinderPath(PermissionModelImpl.ENTITY_CACHE_ENABLED,
			PermissionModelImpl.FINDER_CACHE_ENABLED, PermissionImpl.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findByResourceId",
			new String[] { Long.class.getName() },
			PermissionModelImpl.RESOURCEID_COLUMN_BITMASK);
	public static final FinderPath FINDER_PATH_COUNT_BY_RESOURCEID = new FinderPath(PermissionModelImpl.ENTITY_CACHE_ENABLED,
			PermissionModelImpl.FINDER_CACHE_ENABLED, Long.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByResourceId",
			new String[] { Long.class.getName() });
	public static final FinderPath FINDER_PATH_FETCH_BY_A_R = new FinderPath(PermissionModelImpl.ENTITY_CACHE_ENABLED,
			PermissionModelImpl.FINDER_CACHE_ENABLED, PermissionImpl.class,
			FINDER_CLASS_NAME_ENTITY, "fetchByA_R",
			new String[] { String.class.getName(), Long.class.getName() },
			PermissionModelImpl.ACTIONID_COLUMN_BITMASK |
			PermissionModelImpl.RESOURCEID_COLUMN_BITMASK);
	public static final FinderPath FINDER_PATH_COUNT_BY_A_R = new FinderPath(PermissionModelImpl.ENTITY_CACHE_ENABLED,
			PermissionModelImpl.FINDER_CACHE_ENABLED, Long.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByA_R",
			new String[] { String.class.getName(), Long.class.getName() });
	public static final FinderPath FINDER_PATH_WITH_PAGINATION_FIND_ALL = new FinderPath(PermissionModelImpl.ENTITY_CACHE_ENABLED,
			PermissionModelImpl.FINDER_CACHE_ENABLED, PermissionImpl.class,
			FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findAll", new String[0]);
	public static final FinderPath FINDER_PATH_WITHOUT_PAGINATION_FIND_ALL = new FinderPath(PermissionModelImpl.ENTITY_CACHE_ENABLED,
			PermissionModelImpl.FINDER_CACHE_ENABLED, PermissionImpl.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findAll", new String[0]);
	public static final FinderPath FINDER_PATH_COUNT_ALL = new FinderPath(PermissionModelImpl.ENTITY_CACHE_ENABLED,
			PermissionModelImpl.FINDER_CACHE_ENABLED, Long.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countAll", new String[0]);

	/**
	 * Caches the permission in the entity cache if it is enabled.
	 *
	 * @param permission the permission
	 */
	public void cacheResult(Permission permission) {
		EntityCacheUtil.putResult(PermissionModelImpl.ENTITY_CACHE_ENABLED,
			PermissionImpl.class, permission.getPrimaryKey(), permission);

		FinderCacheUtil.putResult(FINDER_PATH_FETCH_BY_A_R,
			new Object[] {
				permission.getActionId(),
				Long.valueOf(permission.getResourceId())
			}, permission);

		permission.resetOriginalValues();
	}

	/**
	 * Caches the permissions in the entity cache if it is enabled.
	 *
	 * @param permissions the permissions
	 */
	public void cacheResult(List<Permission> permissions) {
		for (Permission permission : permissions) {
			if (EntityCacheUtil.getResult(
						PermissionModelImpl.ENTITY_CACHE_ENABLED,
						PermissionImpl.class, permission.getPrimaryKey()) == null) {
				cacheResult(permission);
			}
			else {
				permission.resetOriginalValues();
			}
		}
	}

	/**
	 * Clears the cache for all permissions.
	 *
	 * <p>
	 * The {@link com.liferay.portal.kernel.dao.orm.EntityCache} and {@link com.liferay.portal.kernel.dao.orm.FinderCache} are both cleared by this method.
	 * </p>
	 */
	@Override
	public void clearCache() {
		if (_HIBERNATE_CACHE_USE_SECOND_LEVEL_CACHE) {
			CacheRegistryUtil.clear(PermissionImpl.class.getName());
		}

		EntityCacheUtil.clearCache(PermissionImpl.class.getName());

		FinderCacheUtil.clearCache(FINDER_CLASS_NAME_ENTITY);
		FinderCacheUtil.clearCache(FINDER_CLASS_NAME_LIST_WITH_PAGINATION);
		FinderCacheUtil.clearCache(FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION);
	}

	/**
	 * Clears the cache for the permission.
	 *
	 * <p>
	 * The {@link com.liferay.portal.kernel.dao.orm.EntityCache} and {@link com.liferay.portal.kernel.dao.orm.FinderCache} are both cleared by this method.
	 * </p>
	 */
	@Override
	public void clearCache(Permission permission) {
		EntityCacheUtil.removeResult(PermissionModelImpl.ENTITY_CACHE_ENABLED,
			PermissionImpl.class, permission.getPrimaryKey());

		FinderCacheUtil.clearCache(FINDER_CLASS_NAME_LIST_WITH_PAGINATION);
		FinderCacheUtil.clearCache(FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION);

		clearUniqueFindersCache(permission);
	}

	@Override
	public void clearCache(List<Permission> permissions) {
		FinderCacheUtil.clearCache(FINDER_CLASS_NAME_LIST_WITH_PAGINATION);
		FinderCacheUtil.clearCache(FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION);

		for (Permission permission : permissions) {
			EntityCacheUtil.removeResult(PermissionModelImpl.ENTITY_CACHE_ENABLED,
				PermissionImpl.class, permission.getPrimaryKey());

			clearUniqueFindersCache(permission);
		}
	}

	protected void clearUniqueFindersCache(Permission permission) {
		FinderCacheUtil.removeResult(FINDER_PATH_FETCH_BY_A_R,
			new Object[] {
				permission.getActionId(),
				Long.valueOf(permission.getResourceId())
			});
	}

	/**
	 * Creates a new permission with the primary key. Does not add the permission to the database.
	 *
	 * @param permissionId the primary key for the new permission
	 * @return the new permission
	 */
	public Permission create(long permissionId) {
		Permission permission = new PermissionImpl();

		permission.setNew(true);
		permission.setPrimaryKey(permissionId);

		return permission;
	}

	/**
	 * Removes the permission with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param permissionId the primary key of the permission
	 * @return the permission that was removed
	 * @throws com.liferay.portal.NoSuchPermissionException if a permission with the primary key could not be found
	 * @throws SystemException if a system exception occurred
	 */
	public Permission remove(long permissionId)
		throws NoSuchPermissionException, SystemException {
		return remove(Long.valueOf(permissionId));
	}

	/**
	 * Removes the permission with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param primaryKey the primary key of the permission
	 * @return the permission that was removed
	 * @throws com.liferay.portal.NoSuchPermissionException if a permission with the primary key could not be found
	 * @throws SystemException if a system exception occurred
	 */
	@Override
	public Permission remove(Serializable primaryKey)
		throws NoSuchPermissionException, SystemException {
		Session session = null;

		try {
			session = openSession();

			Permission permission = (Permission)session.get(PermissionImpl.class,
					primaryKey);

			if (permission == null) {
				if (_log.isWarnEnabled()) {
					_log.warn(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
				}

				throw new NoSuchPermissionException(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY +
					primaryKey);
			}

			return remove(permission);
		}
		catch (NoSuchPermissionException nsee) {
			throw nsee;
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	@Override
	protected Permission removeImpl(Permission permission)
		throws SystemException {
		permission = toUnwrappedModel(permission);

		try {
			clearGroups.clear(permission.getPrimaryKey());
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			FinderCacheUtil.clearCache(PermissionModelImpl.MAPPING_TABLE_GROUPS_PERMISSIONS_NAME);
		}

		try {
			clearRoles.clear(permission.getPrimaryKey());
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			FinderCacheUtil.clearCache(PermissionModelImpl.MAPPING_TABLE_ROLES_PERMISSIONS_NAME);
		}

		try {
			clearUsers.clear(permission.getPrimaryKey());
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			FinderCacheUtil.clearCache(PermissionModelImpl.MAPPING_TABLE_USERS_PERMISSIONS_NAME);
		}

		Session session = null;

		try {
			session = openSession();

			BatchSessionUtil.delete(session, permission);
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}

		clearCache(permission);

		return permission;
	}

	@Override
	public Permission updateImpl(
		com.liferay.portal.model.Permission permission, boolean merge)
		throws SystemException {
		permission = toUnwrappedModel(permission);

		boolean isNew = permission.isNew();

		PermissionModelImpl permissionModelImpl = (PermissionModelImpl)permission;

		Session session = null;

		try {
			session = openSession();

			BatchSessionUtil.update(session, permission, merge);

			permission.setNew(false);
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}

		FinderCacheUtil.clearCache(FINDER_CLASS_NAME_LIST_WITH_PAGINATION);

		if (isNew || !PermissionModelImpl.COLUMN_BITMASK_ENABLED) {
			FinderCacheUtil.clearCache(FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION);
		}
		else {
			if ((permissionModelImpl.getColumnBitmask() &
					FINDER_PATH_WITHOUT_PAGINATION_FIND_BY_RESOURCEID.getColumnBitmask()) != 0) {
				Object[] args = new Object[] {
						Long.valueOf(permissionModelImpl.getOriginalResourceId())
					};

				FinderCacheUtil.removeResult(FINDER_PATH_COUNT_BY_RESOURCEID,
					args);
				FinderCacheUtil.removeResult(FINDER_PATH_WITHOUT_PAGINATION_FIND_BY_RESOURCEID,
					args);

				args = new Object[] {
						Long.valueOf(permissionModelImpl.getResourceId())
					};

				FinderCacheUtil.removeResult(FINDER_PATH_COUNT_BY_RESOURCEID,
					args);
				FinderCacheUtil.removeResult(FINDER_PATH_WITHOUT_PAGINATION_FIND_BY_RESOURCEID,
					args);
			}
		}

		EntityCacheUtil.putResult(PermissionModelImpl.ENTITY_CACHE_ENABLED,
			PermissionImpl.class, permission.getPrimaryKey(), permission);

		if (isNew) {
			FinderCacheUtil.putResult(FINDER_PATH_FETCH_BY_A_R,
				new Object[] {
					permission.getActionId(),
					Long.valueOf(permission.getResourceId())
				}, permission);
		}
		else {
			if ((permissionModelImpl.getColumnBitmask() &
					FINDER_PATH_FETCH_BY_A_R.getColumnBitmask()) != 0) {
				Object[] args = new Object[] {
						permissionModelImpl.getOriginalActionId(),
						Long.valueOf(permissionModelImpl.getOriginalResourceId())
					};

				FinderCacheUtil.removeResult(FINDER_PATH_COUNT_BY_A_R, args);
				FinderCacheUtil.removeResult(FINDER_PATH_FETCH_BY_A_R, args);

				FinderCacheUtil.putResult(FINDER_PATH_FETCH_BY_A_R,
					new Object[] {
						permission.getActionId(),
						Long.valueOf(permission.getResourceId())
					}, permission);
			}
		}

		return permission;
	}

	protected Permission toUnwrappedModel(Permission permission) {
		if (permission instanceof PermissionImpl) {
			return permission;
		}

		PermissionImpl permissionImpl = new PermissionImpl();

		permissionImpl.setNew(permission.isNew());
		permissionImpl.setPrimaryKey(permission.getPrimaryKey());

		permissionImpl.setPermissionId(permission.getPermissionId());
		permissionImpl.setCompanyId(permission.getCompanyId());
		permissionImpl.setActionId(permission.getActionId());
		permissionImpl.setResourceId(permission.getResourceId());

		return permissionImpl;
	}

	/**
	 * Returns the permission with the primary key or throws a {@link com.liferay.portal.NoSuchModelException} if it could not be found.
	 *
	 * @param primaryKey the primary key of the permission
	 * @return the permission
	 * @throws com.liferay.portal.NoSuchModelException if a permission with the primary key could not be found
	 * @throws SystemException if a system exception occurred
	 */
	@Override
	public Permission findByPrimaryKey(Serializable primaryKey)
		throws NoSuchModelException, SystemException {
		return findByPrimaryKey(((Long)primaryKey).longValue());
	}

	/**
	 * Returns the permission with the primary key or throws a {@link com.liferay.portal.NoSuchPermissionException} if it could not be found.
	 *
	 * @param permissionId the primary key of the permission
	 * @return the permission
	 * @throws com.liferay.portal.NoSuchPermissionException if a permission with the primary key could not be found
	 * @throws SystemException if a system exception occurred
	 */
	public Permission findByPrimaryKey(long permissionId)
		throws NoSuchPermissionException, SystemException {
		Permission permission = fetchByPrimaryKey(permissionId);

		if (permission == null) {
			if (_log.isWarnEnabled()) {
				_log.warn(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + permissionId);
			}

			throw new NoSuchPermissionException(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY +
				permissionId);
		}

		return permission;
	}

	/**
	 * Returns the permission with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param primaryKey the primary key of the permission
	 * @return the permission, or <code>null</code> if a permission with the primary key could not be found
	 * @throws SystemException if a system exception occurred
	 */
	@Override
	public Permission fetchByPrimaryKey(Serializable primaryKey)
		throws SystemException {
		return fetchByPrimaryKey(((Long)primaryKey).longValue());
	}

	/**
	 * Returns the permission with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param permissionId the primary key of the permission
	 * @return the permission, or <code>null</code> if a permission with the primary key could not be found
	 * @throws SystemException if a system exception occurred
	 */
	public Permission fetchByPrimaryKey(long permissionId)
		throws SystemException {
		Permission permission = (Permission)EntityCacheUtil.getResult(PermissionModelImpl.ENTITY_CACHE_ENABLED,
				PermissionImpl.class, permissionId);

		if (permission == _nullPermission) {
			return null;
		}

		if (permission == null) {
			Session session = null;

			boolean hasException = false;

			try {
				session = openSession();

				permission = (Permission)session.get(PermissionImpl.class,
						Long.valueOf(permissionId));
			}
			catch (Exception e) {
				hasException = true;

				throw processException(e);
			}
			finally {
				if (permission != null) {
					cacheResult(permission);
				}
				else if (!hasException) {
					EntityCacheUtil.putResult(PermissionModelImpl.ENTITY_CACHE_ENABLED,
						PermissionImpl.class, permissionId, _nullPermission);
				}

				closeSession(session);
			}
		}

		return permission;
	}

	/**
	 * Returns all the permissions where resourceId = &#63;.
	 *
	 * @param resourceId the resource ID
	 * @return the matching permissions
	 * @throws SystemException if a system exception occurred
	 */
	public List<Permission> findByResourceId(long resourceId)
		throws SystemException {
		return findByResourceId(resourceId, QueryUtil.ALL_POS,
			QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the permissions where resourceId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	 * </p>
	 *
	 * @param resourceId the resource ID
	 * @param start the lower bound of the range of permissions
	 * @param end the upper bound of the range of permissions (not inclusive)
	 * @return the range of matching permissions
	 * @throws SystemException if a system exception occurred
	 */
	public List<Permission> findByResourceId(long resourceId, int start, int end)
		throws SystemException {
		return findByResourceId(resourceId, start, end, null);
	}

	/**
	 * Returns an ordered range of all the permissions where resourceId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	 * </p>
	 *
	 * @param resourceId the resource ID
	 * @param start the lower bound of the range of permissions
	 * @param end the upper bound of the range of permissions (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching permissions
	 * @throws SystemException if a system exception occurred
	 */
	public List<Permission> findByResourceId(long resourceId, int start,
		int end, OrderByComparator orderByComparator) throws SystemException {
		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
				(orderByComparator == null)) {
			finderPath = FINDER_PATH_WITHOUT_PAGINATION_FIND_BY_RESOURCEID;
			finderArgs = new Object[] { resourceId };
		}
		else {
			finderPath = FINDER_PATH_WITH_PAGINATION_FIND_BY_RESOURCEID;
			finderArgs = new Object[] { resourceId, start, end, orderByComparator };
		}

		List<Permission> list = (List<Permission>)FinderCacheUtil.getResult(finderPath,
				finderArgs, this);

		if ((list != null) && !list.isEmpty()) {
			for (Permission permission : list) {
				if ((resourceId != permission.getResourceId())) {
					list = null;

					break;
				}
			}
		}

		if (list == null) {
			StringBundler query = null;

			if (orderByComparator != null) {
				query = new StringBundler(3 +
						(orderByComparator.getOrderByFields().length * 3));
			}
			else {
				query = new StringBundler(2);
			}

			query.append(_SQL_SELECT_PERMISSION_WHERE);

			query.append(_FINDER_COLUMN_RESOURCEID_RESOURCEID_2);

			if (orderByComparator != null) {
				appendOrderByComparator(query, _ORDER_BY_ENTITY_ALIAS,
					orderByComparator);
			}

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(resourceId);

				list = (List<Permission>)QueryUtil.list(q, getDialect(), start,
						end);
			}
			catch (Exception e) {
				throw processException(e);
			}
			finally {
				if (list == null) {
					FinderCacheUtil.removeResult(finderPath, finderArgs);
				}
				else {
					cacheResult(list);

					FinderCacheUtil.putResult(finderPath, finderArgs, list);
				}

				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Returns the first permission in the ordered set where resourceId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	 * </p>
	 *
	 * @param resourceId the resource ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching permission
	 * @throws com.liferay.portal.NoSuchPermissionException if a matching permission could not be found
	 * @throws SystemException if a system exception occurred
	 */
	public Permission findByResourceId_First(long resourceId,
		OrderByComparator orderByComparator)
		throws NoSuchPermissionException, SystemException {
		List<Permission> list = findByResourceId(resourceId, 0, 1,
				orderByComparator);

		if (list.isEmpty()) {
			StringBundler msg = new StringBundler(4);

			msg.append(_NO_SUCH_ENTITY_WITH_KEY);

			msg.append("resourceId=");
			msg.append(resourceId);

			msg.append(StringPool.CLOSE_CURLY_BRACE);

			throw new NoSuchPermissionException(msg.toString());
		}
		else {
			return list.get(0);
		}
	}

	/**
	 * Returns the last permission in the ordered set where resourceId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	 * </p>
	 *
	 * @param resourceId the resource ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching permission
	 * @throws com.liferay.portal.NoSuchPermissionException if a matching permission could not be found
	 * @throws SystemException if a system exception occurred
	 */
	public Permission findByResourceId_Last(long resourceId,
		OrderByComparator orderByComparator)
		throws NoSuchPermissionException, SystemException {
		int count = countByResourceId(resourceId);

		List<Permission> list = findByResourceId(resourceId, count - 1, count,
				orderByComparator);

		if (list.isEmpty()) {
			StringBundler msg = new StringBundler(4);

			msg.append(_NO_SUCH_ENTITY_WITH_KEY);

			msg.append("resourceId=");
			msg.append(resourceId);

			msg.append(StringPool.CLOSE_CURLY_BRACE);

			throw new NoSuchPermissionException(msg.toString());
		}
		else {
			return list.get(0);
		}
	}

	/**
	 * Returns the permissions before and after the current permission in the ordered set where resourceId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	 * </p>
	 *
	 * @param permissionId the primary key of the current permission
	 * @param resourceId the resource ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next permission
	 * @throws com.liferay.portal.NoSuchPermissionException if a permission with the primary key could not be found
	 * @throws SystemException if a system exception occurred
	 */
	public Permission[] findByResourceId_PrevAndNext(long permissionId,
		long resourceId, OrderByComparator orderByComparator)
		throws NoSuchPermissionException, SystemException {
		Permission permission = findByPrimaryKey(permissionId);

		Session session = null;

		try {
			session = openSession();

			Permission[] array = new PermissionImpl[3];

			array[0] = getByResourceId_PrevAndNext(session, permission,
					resourceId, orderByComparator, true);

			array[1] = permission;

			array[2] = getByResourceId_PrevAndNext(session, permission,
					resourceId, orderByComparator, false);

			return array;
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	protected Permission getByResourceId_PrevAndNext(Session session,
		Permission permission, long resourceId,
		OrderByComparator orderByComparator, boolean previous) {
		StringBundler query = null;

		if (orderByComparator != null) {
			query = new StringBundler(6 +
					(orderByComparator.getOrderByFields().length * 6));
		}
		else {
			query = new StringBundler(3);
		}

		query.append(_SQL_SELECT_PERMISSION_WHERE);

		query.append(_FINDER_COLUMN_RESOURCEID_RESOURCEID_2);

		if (orderByComparator != null) {
			String[] orderByConditionFields = orderByComparator.getOrderByConditionFields();

			if (orderByConditionFields.length > 0) {
				query.append(WHERE_AND);
			}

			for (int i = 0; i < orderByConditionFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByConditionFields[i]);

				if ((i + 1) < orderByConditionFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN_HAS_NEXT);
					}
					else {
						query.append(WHERE_LESSER_THAN_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN);
					}
					else {
						query.append(WHERE_LESSER_THAN);
					}
				}
			}

			query.append(ORDER_BY_CLAUSE);

			String[] orderByFields = orderByComparator.getOrderByFields();

			for (int i = 0; i < orderByFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByFields[i]);

				if ((i + 1) < orderByFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC_HAS_NEXT);
					}
					else {
						query.append(ORDER_BY_DESC_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC);
					}
					else {
						query.append(ORDER_BY_DESC);
					}
				}
			}
		}

		String sql = query.toString();

		Query q = session.createQuery(sql);

		q.setFirstResult(0);
		q.setMaxResults(2);

		QueryPos qPos = QueryPos.getInstance(q);

		qPos.add(resourceId);

		if (orderByComparator != null) {
			Object[] values = orderByComparator.getOrderByConditionValues(permission);

			for (Object value : values) {
				qPos.add(value);
			}
		}

		List<Permission> list = q.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Returns the permission where actionId = &#63; and resourceId = &#63; or throws a {@link com.liferay.portal.NoSuchPermissionException} if it could not be found.
	 *
	 * @param actionId the action ID
	 * @param resourceId the resource ID
	 * @return the matching permission
	 * @throws com.liferay.portal.NoSuchPermissionException if a matching permission could not be found
	 * @throws SystemException if a system exception occurred
	 */
	public Permission findByA_R(String actionId, long resourceId)
		throws NoSuchPermissionException, SystemException {
		Permission permission = fetchByA_R(actionId, resourceId);

		if (permission == null) {
			StringBundler msg = new StringBundler(6);

			msg.append(_NO_SUCH_ENTITY_WITH_KEY);

			msg.append("actionId=");
			msg.append(actionId);

			msg.append(", resourceId=");
			msg.append(resourceId);

			msg.append(StringPool.CLOSE_CURLY_BRACE);

			if (_log.isWarnEnabled()) {
				_log.warn(msg.toString());
			}

			throw new NoSuchPermissionException(msg.toString());
		}

		return permission;
	}

	/**
	 * Returns the permission where actionId = &#63; and resourceId = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param actionId the action ID
	 * @param resourceId the resource ID
	 * @return the matching permission, or <code>null</code> if a matching permission could not be found
	 * @throws SystemException if a system exception occurred
	 */
	public Permission fetchByA_R(String actionId, long resourceId)
		throws SystemException {
		return fetchByA_R(actionId, resourceId, true);
	}

	/**
	 * Returns the permission where actionId = &#63; and resourceId = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param actionId the action ID
	 * @param resourceId the resource ID
	 * @param retrieveFromCache whether to use the finder cache
	 * @return the matching permission, or <code>null</code> if a matching permission could not be found
	 * @throws SystemException if a system exception occurred
	 */
	public Permission fetchByA_R(String actionId, long resourceId,
		boolean retrieveFromCache) throws SystemException {
		Object[] finderArgs = new Object[] { actionId, resourceId };

		Object result = null;

		if (retrieveFromCache) {
			result = FinderCacheUtil.getResult(FINDER_PATH_FETCH_BY_A_R,
					finderArgs, this);
		}

		if (result instanceof Permission) {
			Permission permission = (Permission)result;

			if (!Validator.equals(actionId, permission.getActionId()) ||
					(resourceId != permission.getResourceId())) {
				result = null;
			}
		}

		if (result == null) {
			StringBundler query = new StringBundler(3);

			query.append(_SQL_SELECT_PERMISSION_WHERE);

			if (actionId == null) {
				query.append(_FINDER_COLUMN_A_R_ACTIONID_1);
			}
			else {
				if (actionId.equals(StringPool.BLANK)) {
					query.append(_FINDER_COLUMN_A_R_ACTIONID_3);
				}
				else {
					query.append(_FINDER_COLUMN_A_R_ACTIONID_2);
				}
			}

			query.append(_FINDER_COLUMN_A_R_RESOURCEID_2);

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				if (actionId != null) {
					qPos.add(actionId);
				}

				qPos.add(resourceId);

				List<Permission> list = q.list();

				result = list;

				Permission permission = null;

				if (list.isEmpty()) {
					FinderCacheUtil.putResult(FINDER_PATH_FETCH_BY_A_R,
						finderArgs, list);
				}
				else {
					permission = list.get(0);

					cacheResult(permission);

					if ((permission.getActionId() == null) ||
							!permission.getActionId().equals(actionId) ||
							(permission.getResourceId() != resourceId)) {
						FinderCacheUtil.putResult(FINDER_PATH_FETCH_BY_A_R,
							finderArgs, permission);
					}
				}

				return permission;
			}
			catch (Exception e) {
				throw processException(e);
			}
			finally {
				if (result == null) {
					FinderCacheUtil.removeResult(FINDER_PATH_FETCH_BY_A_R,
						finderArgs);
				}

				closeSession(session);
			}
		}
		else {
			if (result instanceof List<?>) {
				return null;
			}
			else {
				return (Permission)result;
			}
		}
	}

	/**
	 * Returns all the permissions.
	 *
	 * @return the permissions
	 * @throws SystemException if a system exception occurred
	 */
	public List<Permission> findAll() throws SystemException {
		return findAll(QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the permissions.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	 * </p>
	 *
	 * @param start the lower bound of the range of permissions
	 * @param end the upper bound of the range of permissions (not inclusive)
	 * @return the range of permissions
	 * @throws SystemException if a system exception occurred
	 */
	public List<Permission> findAll(int start, int end)
		throws SystemException {
		return findAll(start, end, null);
	}

	/**
	 * Returns an ordered range of all the permissions.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	 * </p>
	 *
	 * @param start the lower bound of the range of permissions
	 * @param end the upper bound of the range of permissions (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of permissions
	 * @throws SystemException if a system exception occurred
	 */
	public List<Permission> findAll(int start, int end,
		OrderByComparator orderByComparator) throws SystemException {
		FinderPath finderPath = null;
		Object[] finderArgs = new Object[] { start, end, orderByComparator };

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
				(orderByComparator == null)) {
			finderPath = FINDER_PATH_WITHOUT_PAGINATION_FIND_ALL;
			finderArgs = FINDER_ARGS_EMPTY;
		}
		else {
			finderPath = FINDER_PATH_WITH_PAGINATION_FIND_ALL;
			finderArgs = new Object[] { start, end, orderByComparator };
		}

		List<Permission> list = (List<Permission>)FinderCacheUtil.getResult(finderPath,
				finderArgs, this);

		if (list == null) {
			StringBundler query = null;
			String sql = null;

			if (orderByComparator != null) {
				query = new StringBundler(2 +
						(orderByComparator.getOrderByFields().length * 3));

				query.append(_SQL_SELECT_PERMISSION);

				appendOrderByComparator(query, _ORDER_BY_ENTITY_ALIAS,
					orderByComparator);

				sql = query.toString();
			}
			else {
				sql = _SQL_SELECT_PERMISSION;
			}

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				if (orderByComparator == null) {
					list = (List<Permission>)QueryUtil.list(q, getDialect(),
							start, end, false);

					Collections.sort(list);
				}
				else {
					list = (List<Permission>)QueryUtil.list(q, getDialect(),
							start, end);
				}
			}
			catch (Exception e) {
				throw processException(e);
			}
			finally {
				if (list == null) {
					FinderCacheUtil.removeResult(finderPath, finderArgs);
				}
				else {
					cacheResult(list);

					FinderCacheUtil.putResult(finderPath, finderArgs, list);
				}

				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Removes all the permissions where resourceId = &#63; from the database.
	 *
	 * @param resourceId the resource ID
	 * @throws SystemException if a system exception occurred
	 */
	public void removeByResourceId(long resourceId) throws SystemException {
		for (Permission permission : findByResourceId(resourceId)) {
			remove(permission);
		}
	}

	/**
	 * Removes the permission where actionId = &#63; and resourceId = &#63; from the database.
	 *
	 * @param actionId the action ID
	 * @param resourceId the resource ID
	 * @throws SystemException if a system exception occurred
	 */
	public void removeByA_R(String actionId, long resourceId)
		throws NoSuchPermissionException, SystemException {
		Permission permission = findByA_R(actionId, resourceId);

		remove(permission);
	}

	/**
	 * Removes all the permissions from the database.
	 *
	 * @throws SystemException if a system exception occurred
	 */
	public void removeAll() throws SystemException {
		for (Permission permission : findAll()) {
			remove(permission);
		}
	}

	/**
	 * Returns the number of permissions where resourceId = &#63;.
	 *
	 * @param resourceId the resource ID
	 * @return the number of matching permissions
	 * @throws SystemException if a system exception occurred
	 */
	public int countByResourceId(long resourceId) throws SystemException {
		Object[] finderArgs = new Object[] { resourceId };

		Long count = (Long)FinderCacheUtil.getResult(FINDER_PATH_COUNT_BY_RESOURCEID,
				finderArgs, this);

		if (count == null) {
			StringBundler query = new StringBundler(2);

			query.append(_SQL_COUNT_PERMISSION_WHERE);

			query.append(_FINDER_COLUMN_RESOURCEID_RESOURCEID_2);

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(resourceId);

				count = (Long)q.uniqueResult();
			}
			catch (Exception e) {
				throw processException(e);
			}
			finally {
				if (count == null) {
					count = Long.valueOf(0);
				}

				FinderCacheUtil.putResult(FINDER_PATH_COUNT_BY_RESOURCEID,
					finderArgs, count);

				closeSession(session);
			}
		}

		return count.intValue();
	}

	/**
	 * Returns the number of permissions where actionId = &#63; and resourceId = &#63;.
	 *
	 * @param actionId the action ID
	 * @param resourceId the resource ID
	 * @return the number of matching permissions
	 * @throws SystemException if a system exception occurred
	 */
	public int countByA_R(String actionId, long resourceId)
		throws SystemException {
		Object[] finderArgs = new Object[] { actionId, resourceId };

		Long count = (Long)FinderCacheUtil.getResult(FINDER_PATH_COUNT_BY_A_R,
				finderArgs, this);

		if (count == null) {
			StringBundler query = new StringBundler(3);

			query.append(_SQL_COUNT_PERMISSION_WHERE);

			if (actionId == null) {
				query.append(_FINDER_COLUMN_A_R_ACTIONID_1);
			}
			else {
				if (actionId.equals(StringPool.BLANK)) {
					query.append(_FINDER_COLUMN_A_R_ACTIONID_3);
				}
				else {
					query.append(_FINDER_COLUMN_A_R_ACTIONID_2);
				}
			}

			query.append(_FINDER_COLUMN_A_R_RESOURCEID_2);

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				if (actionId != null) {
					qPos.add(actionId);
				}

				qPos.add(resourceId);

				count = (Long)q.uniqueResult();
			}
			catch (Exception e) {
				throw processException(e);
			}
			finally {
				if (count == null) {
					count = Long.valueOf(0);
				}

				FinderCacheUtil.putResult(FINDER_PATH_COUNT_BY_A_R, finderArgs,
					count);

				closeSession(session);
			}
		}

		return count.intValue();
	}

	/**
	 * Returns the number of permissions.
	 *
	 * @return the number of permissions
	 * @throws SystemException if a system exception occurred
	 */
	public int countAll() throws SystemException {
		Long count = (Long)FinderCacheUtil.getResult(FINDER_PATH_COUNT_ALL,
				FINDER_ARGS_EMPTY, this);

		if (count == null) {
			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(_SQL_COUNT_PERMISSION);

				count = (Long)q.uniqueResult();
			}
			catch (Exception e) {
				throw processException(e);
			}
			finally {
				if (count == null) {
					count = Long.valueOf(0);
				}

				FinderCacheUtil.putResult(FINDER_PATH_COUNT_ALL,
					FINDER_ARGS_EMPTY, count);

				closeSession(session);
			}
		}

		return count.intValue();
	}

	/**
	 * Returns all the groups associated with the permission.
	 *
	 * @param pk the primary key of the permission
	 * @return the groups associated with the permission
	 * @throws SystemException if a system exception occurred
	 */
	public List<com.liferay.portal.model.Group> getGroups(long pk)
		throws SystemException {
		return getGroups(pk, QueryUtil.ALL_POS, QueryUtil.ALL_POS);
	}

	/**
	 * Returns a range of all the groups associated with the permission.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	 * </p>
	 *
	 * @param pk the primary key of the permission
	 * @param start the lower bound of the range of permissions
	 * @param end the upper bound of the range of permissions (not inclusive)
	 * @return the range of groups associated with the permission
	 * @throws SystemException if a system exception occurred
	 */
	public List<com.liferay.portal.model.Group> getGroups(long pk, int start,
		int end) throws SystemException {
		return getGroups(pk, start, end, null);
	}

	public static final FinderPath FINDER_PATH_GET_GROUPS = new FinderPath(com.liferay.portal.model.impl.GroupModelImpl.ENTITY_CACHE_ENABLED,
			PermissionModelImpl.FINDER_CACHE_ENABLED_GROUPS_PERMISSIONS,
			com.liferay.portal.model.impl.GroupImpl.class,
			PermissionModelImpl.MAPPING_TABLE_GROUPS_PERMISSIONS_NAME,
			"getGroups",
			new String[] {
				Long.class.getName(), "java.lang.Integer", "java.lang.Integer",
				"com.liferay.portal.kernel.util.OrderByComparator"
			});

	static {
		FINDER_PATH_GET_GROUPS.setCacheKeyGeneratorCacheName(null);
	}

	/**
	 * Returns an ordered range of all the groups associated with the permission.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	 * </p>
	 *
	 * @param pk the primary key of the permission
	 * @param start the lower bound of the range of permissions
	 * @param end the upper bound of the range of permissions (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of groups associated with the permission
	 * @throws SystemException if a system exception occurred
	 */
	public List<com.liferay.portal.model.Group> getGroups(long pk, int start,
		int end, OrderByComparator orderByComparator) throws SystemException {
		Object[] finderArgs = new Object[] { pk, start, end, orderByComparator };

		List<com.liferay.portal.model.Group> list = (List<com.liferay.portal.model.Group>)FinderCacheUtil.getResult(FINDER_PATH_GET_GROUPS,
				finderArgs, this);

		if (list == null) {
			Session session = null;

			try {
				session = openSession();

				String sql = null;

				if (orderByComparator != null) {
					sql = _SQL_GETGROUPS.concat(ORDER_BY_CLAUSE)
										.concat(orderByComparator.getOrderBy());
				}
				else {
					sql = _SQL_GETGROUPS.concat(com.liferay.portal.model.impl.GroupModelImpl.ORDER_BY_SQL);
				}

				SQLQuery q = session.createSQLQuery(sql);

				q.addEntity("Group_",
					com.liferay.portal.model.impl.GroupImpl.class);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(pk);

				list = (List<com.liferay.portal.model.Group>)QueryUtil.list(q,
						getDialect(), start, end);
			}
			catch (Exception e) {
				throw processException(e);
			}
			finally {
				if (list == null) {
					FinderCacheUtil.removeResult(FINDER_PATH_GET_GROUPS,
						finderArgs);
				}
				else {
					groupPersistence.cacheResult(list);

					FinderCacheUtil.putResult(FINDER_PATH_GET_GROUPS,
						finderArgs, list);
				}

				closeSession(session);
			}
		}

		return list;
	}

	public static final FinderPath FINDER_PATH_GET_GROUPS_SIZE = new FinderPath(com.liferay.portal.model.impl.GroupModelImpl.ENTITY_CACHE_ENABLED,
			PermissionModelImpl.FINDER_CACHE_ENABLED_GROUPS_PERMISSIONS,
			Long.class,
			PermissionModelImpl.MAPPING_TABLE_GROUPS_PERMISSIONS_NAME,
			"getGroupsSize", new String[] { Long.class.getName() });

	static {
		FINDER_PATH_GET_GROUPS_SIZE.setCacheKeyGeneratorCacheName(null);
	}

	/**
	 * Returns the number of groups associated with the permission.
	 *
	 * @param pk the primary key of the permission
	 * @return the number of groups associated with the permission
	 * @throws SystemException if a system exception occurred
	 */
	public int getGroupsSize(long pk) throws SystemException {
		Object[] finderArgs = new Object[] { pk };

		Long count = (Long)FinderCacheUtil.getResult(FINDER_PATH_GET_GROUPS_SIZE,
				finderArgs, this);

		if (count == null) {
			Session session = null;

			try {
				session = openSession();

				SQLQuery q = session.createSQLQuery(_SQL_GETGROUPSSIZE);

				q.addScalar(COUNT_COLUMN_NAME,
					com.liferay.portal.kernel.dao.orm.Type.LONG);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(pk);

				count = (Long)q.uniqueResult();
			}
			catch (Exception e) {
				throw processException(e);
			}
			finally {
				if (count == null) {
					count = Long.valueOf(0);
				}

				FinderCacheUtil.putResult(FINDER_PATH_GET_GROUPS_SIZE,
					finderArgs, count);

				closeSession(session);
			}
		}

		return count.intValue();
	}

	public static final FinderPath FINDER_PATH_CONTAINS_GROUP = new FinderPath(com.liferay.portal.model.impl.GroupModelImpl.ENTITY_CACHE_ENABLED,
			PermissionModelImpl.FINDER_CACHE_ENABLED_GROUPS_PERMISSIONS,
			Boolean.class,
			PermissionModelImpl.MAPPING_TABLE_GROUPS_PERMISSIONS_NAME,
			"containsGroup",
			new String[] { Long.class.getName(), Long.class.getName() });

	/**
	 * Returns <code>true</code> if the group is associated with the permission.
	 *
	 * @param pk the primary key of the permission
	 * @param groupPK the primary key of the group
	 * @return <code>true</code> if the group is associated with the permission; <code>false</code> otherwise
	 * @throws SystemException if a system exception occurred
	 */
	public boolean containsGroup(long pk, long groupPK)
		throws SystemException {
		Object[] finderArgs = new Object[] { pk, groupPK };

		Boolean value = (Boolean)FinderCacheUtil.getResult(FINDER_PATH_CONTAINS_GROUP,
				finderArgs, this);

		if (value == null) {
			try {
				value = Boolean.valueOf(containsGroup.contains(pk, groupPK));
			}
			catch (Exception e) {
				throw processException(e);
			}
			finally {
				if (value == null) {
					value = Boolean.FALSE;
				}

				FinderCacheUtil.putResult(FINDER_PATH_CONTAINS_GROUP,
					finderArgs, value);
			}
		}

		return value.booleanValue();
	}

	/**
	 * Returns <code>true</code> if the permission has any groups associated with it.
	 *
	 * @param pk the primary key of the permission to check for associations with groups
	 * @return <code>true</code> if the permission has any groups associated with it; <code>false</code> otherwise
	 * @throws SystemException if a system exception occurred
	 */
	public boolean containsGroups(long pk) throws SystemException {
		if (getGroupsSize(pk) > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Adds an association between the permission and the group. Also notifies the appropriate model listeners and clears the mapping table finder cache.
	 *
	 * @param pk the primary key of the permission
	 * @param groupPK the primary key of the group
	 * @throws SystemException if a system exception occurred
	 */
	public void addGroup(long pk, long groupPK) throws SystemException {
		try {
			addGroup.add(pk, groupPK);
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			FinderCacheUtil.clearCache(PermissionModelImpl.MAPPING_TABLE_GROUPS_PERMISSIONS_NAME);
		}
	}

	/**
	 * Adds an association between the permission and the group. Also notifies the appropriate model listeners and clears the mapping table finder cache.
	 *
	 * @param pk the primary key of the permission
	 * @param group the group
	 * @throws SystemException if a system exception occurred
	 */
	public void addGroup(long pk, com.liferay.portal.model.Group group)
		throws SystemException {
		try {
			addGroup.add(pk, group.getPrimaryKey());
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			FinderCacheUtil.clearCache(PermissionModelImpl.MAPPING_TABLE_GROUPS_PERMISSIONS_NAME);
		}
	}

	/**
	 * Adds an association between the permission and the groups. Also notifies the appropriate model listeners and clears the mapping table finder cache.
	 *
	 * @param pk the primary key of the permission
	 * @param groupPKs the primary keys of the groups
	 * @throws SystemException if a system exception occurred
	 */
	public void addGroups(long pk, long[] groupPKs) throws SystemException {
		try {
			for (long groupPK : groupPKs) {
				addGroup.add(pk, groupPK);
			}
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			FinderCacheUtil.clearCache(PermissionModelImpl.MAPPING_TABLE_GROUPS_PERMISSIONS_NAME);
		}
	}

	/**
	 * Adds an association between the permission and the groups. Also notifies the appropriate model listeners and clears the mapping table finder cache.
	 *
	 * @param pk the primary key of the permission
	 * @param groups the groups
	 * @throws SystemException if a system exception occurred
	 */
	public void addGroups(long pk, List<com.liferay.portal.model.Group> groups)
		throws SystemException {
		try {
			for (com.liferay.portal.model.Group group : groups) {
				addGroup.add(pk, group.getPrimaryKey());
			}
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			FinderCacheUtil.clearCache(PermissionModelImpl.MAPPING_TABLE_GROUPS_PERMISSIONS_NAME);
		}
	}

	/**
	 * Clears all associations between the permission and its groups. Also notifies the appropriate model listeners and clears the mapping table finder cache.
	 *
	 * @param pk the primary key of the permission to clear the associated groups from
	 * @throws SystemException if a system exception occurred
	 */
	public void clearGroups(long pk) throws SystemException {
		try {
			clearGroups.clear(pk);
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			FinderCacheUtil.clearCache(PermissionModelImpl.MAPPING_TABLE_GROUPS_PERMISSIONS_NAME);
		}
	}

	/**
	 * Removes the association between the permission and the group. Also notifies the appropriate model listeners and clears the mapping table finder cache.
	 *
	 * @param pk the primary key of the permission
	 * @param groupPK the primary key of the group
	 * @throws SystemException if a system exception occurred
	 */
	public void removeGroup(long pk, long groupPK) throws SystemException {
		try {
			removeGroup.remove(pk, groupPK);
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			FinderCacheUtil.clearCache(PermissionModelImpl.MAPPING_TABLE_GROUPS_PERMISSIONS_NAME);
		}
	}

	/**
	 * Removes the association between the permission and the group. Also notifies the appropriate model listeners and clears the mapping table finder cache.
	 *
	 * @param pk the primary key of the permission
	 * @param group the group
	 * @throws SystemException if a system exception occurred
	 */
	public void removeGroup(long pk, com.liferay.portal.model.Group group)
		throws SystemException {
		try {
			removeGroup.remove(pk, group.getPrimaryKey());
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			FinderCacheUtil.clearCache(PermissionModelImpl.MAPPING_TABLE_GROUPS_PERMISSIONS_NAME);
		}
	}

	/**
	 * Removes the association between the permission and the groups. Also notifies the appropriate model listeners and clears the mapping table finder cache.
	 *
	 * @param pk the primary key of the permission
	 * @param groupPKs the primary keys of the groups
	 * @throws SystemException if a system exception occurred
	 */
	public void removeGroups(long pk, long[] groupPKs)
		throws SystemException {
		try {
			for (long groupPK : groupPKs) {
				removeGroup.remove(pk, groupPK);
			}
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			FinderCacheUtil.clearCache(PermissionModelImpl.MAPPING_TABLE_GROUPS_PERMISSIONS_NAME);
		}
	}

	/**
	 * Removes the association between the permission and the groups. Also notifies the appropriate model listeners and clears the mapping table finder cache.
	 *
	 * @param pk the primary key of the permission
	 * @param groups the groups
	 * @throws SystemException if a system exception occurred
	 */
	public void removeGroups(long pk,
		List<com.liferay.portal.model.Group> groups) throws SystemException {
		try {
			for (com.liferay.portal.model.Group group : groups) {
				removeGroup.remove(pk, group.getPrimaryKey());
			}
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			FinderCacheUtil.clearCache(PermissionModelImpl.MAPPING_TABLE_GROUPS_PERMISSIONS_NAME);
		}
	}

	/**
	 * Sets the groups associated with the permission, removing and adding associations as necessary. Also notifies the appropriate model listeners and clears the mapping table finder cache.
	 *
	 * @param pk the primary key of the permission
	 * @param groupPKs the primary keys of the groups to be associated with the permission
	 * @throws SystemException if a system exception occurred
	 */
	public void setGroups(long pk, long[] groupPKs) throws SystemException {
		try {
			Set<Long> groupPKSet = SetUtil.fromArray(groupPKs);

			List<com.liferay.portal.model.Group> groups = getGroups(pk);

			for (com.liferay.portal.model.Group group : groups) {
				if (!groupPKSet.remove(group.getPrimaryKey())) {
					removeGroup.remove(pk, group.getPrimaryKey());
				}
			}

			for (Long groupPK : groupPKSet) {
				addGroup.add(pk, groupPK);
			}
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			FinderCacheUtil.clearCache(PermissionModelImpl.MAPPING_TABLE_GROUPS_PERMISSIONS_NAME);
		}
	}

	/**
	 * Sets the groups associated with the permission, removing and adding associations as necessary. Also notifies the appropriate model listeners and clears the mapping table finder cache.
	 *
	 * @param pk the primary key of the permission
	 * @param groups the groups to be associated with the permission
	 * @throws SystemException if a system exception occurred
	 */
	public void setGroups(long pk, List<com.liferay.portal.model.Group> groups)
		throws SystemException {
		try {
			long[] groupPKs = new long[groups.size()];

			for (int i = 0; i < groups.size(); i++) {
				com.liferay.portal.model.Group group = groups.get(i);

				groupPKs[i] = group.getPrimaryKey();
			}

			setGroups(pk, groupPKs);
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			FinderCacheUtil.clearCache(PermissionModelImpl.MAPPING_TABLE_GROUPS_PERMISSIONS_NAME);
		}
	}

	/**
	 * Returns all the roles associated with the permission.
	 *
	 * @param pk the primary key of the permission
	 * @return the roles associated with the permission
	 * @throws SystemException if a system exception occurred
	 */
	public List<com.liferay.portal.model.Role> getRoles(long pk)
		throws SystemException {
		return getRoles(pk, QueryUtil.ALL_POS, QueryUtil.ALL_POS);
	}

	/**
	 * Returns a range of all the roles associated with the permission.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	 * </p>
	 *
	 * @param pk the primary key of the permission
	 * @param start the lower bound of the range of permissions
	 * @param end the upper bound of the range of permissions (not inclusive)
	 * @return the range of roles associated with the permission
	 * @throws SystemException if a system exception occurred
	 */
	public List<com.liferay.portal.model.Role> getRoles(long pk, int start,
		int end) throws SystemException {
		return getRoles(pk, start, end, null);
	}

	public static final FinderPath FINDER_PATH_GET_ROLES = new FinderPath(com.liferay.portal.model.impl.RoleModelImpl.ENTITY_CACHE_ENABLED,
			PermissionModelImpl.FINDER_CACHE_ENABLED_ROLES_PERMISSIONS,
			com.liferay.portal.model.impl.RoleImpl.class,
			PermissionModelImpl.MAPPING_TABLE_ROLES_PERMISSIONS_NAME,
			"getRoles",
			new String[] {
				Long.class.getName(), "java.lang.Integer", "java.lang.Integer",
				"com.liferay.portal.kernel.util.OrderByComparator"
			});

	static {
		FINDER_PATH_GET_ROLES.setCacheKeyGeneratorCacheName(null);
	}

	/**
	 * Returns an ordered range of all the roles associated with the permission.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	 * </p>
	 *
	 * @param pk the primary key of the permission
	 * @param start the lower bound of the range of permissions
	 * @param end the upper bound of the range of permissions (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of roles associated with the permission
	 * @throws SystemException if a system exception occurred
	 */
	public List<com.liferay.portal.model.Role> getRoles(long pk, int start,
		int end, OrderByComparator orderByComparator) throws SystemException {
		Object[] finderArgs = new Object[] { pk, start, end, orderByComparator };

		List<com.liferay.portal.model.Role> list = (List<com.liferay.portal.model.Role>)FinderCacheUtil.getResult(FINDER_PATH_GET_ROLES,
				finderArgs, this);

		if (list == null) {
			Session session = null;

			try {
				session = openSession();

				String sql = null;

				if (orderByComparator != null) {
					sql = _SQL_GETROLES.concat(ORDER_BY_CLAUSE)
									   .concat(orderByComparator.getOrderBy());
				}
				else {
					sql = _SQL_GETROLES.concat(com.liferay.portal.model.impl.RoleModelImpl.ORDER_BY_SQL);
				}

				SQLQuery q = session.createSQLQuery(sql);

				q.addEntity("Role_",
					com.liferay.portal.model.impl.RoleImpl.class);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(pk);

				list = (List<com.liferay.portal.model.Role>)QueryUtil.list(q,
						getDialect(), start, end);
			}
			catch (Exception e) {
				throw processException(e);
			}
			finally {
				if (list == null) {
					FinderCacheUtil.removeResult(FINDER_PATH_GET_ROLES,
						finderArgs);
				}
				else {
					rolePersistence.cacheResult(list);

					FinderCacheUtil.putResult(FINDER_PATH_GET_ROLES,
						finderArgs, list);
				}

				closeSession(session);
			}
		}

		return list;
	}

	public static final FinderPath FINDER_PATH_GET_ROLES_SIZE = new FinderPath(com.liferay.portal.model.impl.RoleModelImpl.ENTITY_CACHE_ENABLED,
			PermissionModelImpl.FINDER_CACHE_ENABLED_ROLES_PERMISSIONS,
			Long.class,
			PermissionModelImpl.MAPPING_TABLE_ROLES_PERMISSIONS_NAME,
			"getRolesSize", new String[] { Long.class.getName() });

	static {
		FINDER_PATH_GET_ROLES_SIZE.setCacheKeyGeneratorCacheName(null);
	}

	/**
	 * Returns the number of roles associated with the permission.
	 *
	 * @param pk the primary key of the permission
	 * @return the number of roles associated with the permission
	 * @throws SystemException if a system exception occurred
	 */
	public int getRolesSize(long pk) throws SystemException {
		Object[] finderArgs = new Object[] { pk };

		Long count = (Long)FinderCacheUtil.getResult(FINDER_PATH_GET_ROLES_SIZE,
				finderArgs, this);

		if (count == null) {
			Session session = null;

			try {
				session = openSession();

				SQLQuery q = session.createSQLQuery(_SQL_GETROLESSIZE);

				q.addScalar(COUNT_COLUMN_NAME,
					com.liferay.portal.kernel.dao.orm.Type.LONG);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(pk);

				count = (Long)q.uniqueResult();
			}
			catch (Exception e) {
				throw processException(e);
			}
			finally {
				if (count == null) {
					count = Long.valueOf(0);
				}

				FinderCacheUtil.putResult(FINDER_PATH_GET_ROLES_SIZE,
					finderArgs, count);

				closeSession(session);
			}
		}

		return count.intValue();
	}

	public static final FinderPath FINDER_PATH_CONTAINS_ROLE = new FinderPath(com.liferay.portal.model.impl.RoleModelImpl.ENTITY_CACHE_ENABLED,
			PermissionModelImpl.FINDER_CACHE_ENABLED_ROLES_PERMISSIONS,
			Boolean.class,
			PermissionModelImpl.MAPPING_TABLE_ROLES_PERMISSIONS_NAME,
			"containsRole",
			new String[] { Long.class.getName(), Long.class.getName() });

	/**
	 * Returns <code>true</code> if the role is associated with the permission.
	 *
	 * @param pk the primary key of the permission
	 * @param rolePK the primary key of the role
	 * @return <code>true</code> if the role is associated with the permission; <code>false</code> otherwise
	 * @throws SystemException if a system exception occurred
	 */
	public boolean containsRole(long pk, long rolePK) throws SystemException {
		Object[] finderArgs = new Object[] { pk, rolePK };

		Boolean value = (Boolean)FinderCacheUtil.getResult(FINDER_PATH_CONTAINS_ROLE,
				finderArgs, this);

		if (value == null) {
			try {
				value = Boolean.valueOf(containsRole.contains(pk, rolePK));
			}
			catch (Exception e) {
				throw processException(e);
			}
			finally {
				if (value == null) {
					value = Boolean.FALSE;
				}

				FinderCacheUtil.putResult(FINDER_PATH_CONTAINS_ROLE,
					finderArgs, value);
			}
		}

		return value.booleanValue();
	}

	/**
	 * Returns <code>true</code> if the permission has any roles associated with it.
	 *
	 * @param pk the primary key of the permission to check for associations with roles
	 * @return <code>true</code> if the permission has any roles associated with it; <code>false</code> otherwise
	 * @throws SystemException if a system exception occurred
	 */
	public boolean containsRoles(long pk) throws SystemException {
		if (getRolesSize(pk) > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Adds an association between the permission and the role. Also notifies the appropriate model listeners and clears the mapping table finder cache.
	 *
	 * @param pk the primary key of the permission
	 * @param rolePK the primary key of the role
	 * @throws SystemException if a system exception occurred
	 */
	public void addRole(long pk, long rolePK) throws SystemException {
		try {
			addRole.add(pk, rolePK);
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			FinderCacheUtil.clearCache(PermissionModelImpl.MAPPING_TABLE_ROLES_PERMISSIONS_NAME);
		}
	}

	/**
	 * Adds an association between the permission and the role. Also notifies the appropriate model listeners and clears the mapping table finder cache.
	 *
	 * @param pk the primary key of the permission
	 * @param role the role
	 * @throws SystemException if a s
