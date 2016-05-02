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

package db.hris.service.persistence;

import com.liferay.portal.service.persistence.BasePersistence;

import db.hris.model.Employee;

/**
 * The persistence interface for the employee service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author danny
 * @see EmployeePersistenceImpl
 * @see EmployeeUtil
 * @generated
 */
public interface EmployeePersistence extends BasePersistence<Employee> {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. Always use {@link EmployeeUtil} to access the employee persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this interface.
	 */

	/**
	* Caches the employee in the entity cache if it is enabled.
	*
	* @param employee the employee
	*/
	public void cacheResult(db.hris.model.Employee employee);

	/**
	* Caches the employees in the entity cache if it is enabled.
	*
	* @param employees the employees
	*/
	public void cacheResult(java.util.List<db.hris.model.Employee> employees);

	/**
	* Creates a new employee with the primary key. Does not add the employee to the database.
	*
	* @param employeeId the primary key for the new employee
	* @return the new employee
	*/
	public db.hris.model.Employee create(long employeeId);

	/**
	* Removes the employee with the primary key from the database. Also notifies the appropriate model listeners.
	*
	* @param employeeId the primary key of the employee
	* @return the employee that was removed
	* @throws db.hris.NoSuchEmployeeException if a employee with the primary key could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee remove(long employeeId)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	public db.hris.model.Employee updateImpl(db.hris.model.Employee employee,
		boolean merge)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the employee with the primary key or throws a {@link db.hris.NoSuchEmployeeException} if it could not be found.
	*
	* @param employeeId the primary key of the employee
	* @return the employee
	* @throws db.hris.NoSuchEmployeeException if a employee with the primary key could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee findByPrimaryKey(long employeeId)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns the employee with the primary key or returns <code>null</code> if it could not be found.
	*
	* @param employeeId the primary key of the employee
	* @return the employee, or <code>null</code> if a employee with the primary key could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee fetchByPrimaryKey(long employeeId)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns all the employees where groupId = &#63;.
	*
	* @param groupId the group ID
	* @return the matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByGroupId(long groupId)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns a range of all the employees where groupId = &#63;.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param groupId the group ID
	* @param start the lower bound of the range of employees
	* @param end the upper bound of the range of employees (not inclusive)
	* @return the range of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByGroupId(long groupId,
		int start, int end)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns an ordered range of all the employees where groupId = &#63;.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param groupId the group ID
	* @param start the lower bound of the range of employees
	* @param end the upper bound of the range of employees (not inclusive)
	* @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	* @return the ordered range of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByGroupId(long groupId,
		int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the first employee in the ordered set where groupId = &#63;.
	*
	* @param groupId the group ID
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the first matching employee
	* @throws db.hris.NoSuchEmployeeException if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee findByGroupId_First(long groupId,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns the first employee in the ordered set where groupId = &#63;.
	*
	* @param groupId the group ID
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the first matching employee, or <code>null</code> if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee fetchByGroupId_First(long groupId,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the last employee in the ordered set where groupId = &#63;.
	*
	* @param groupId the group ID
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the last matching employee
	* @throws db.hris.NoSuchEmployeeException if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee findByGroupId_Last(long groupId,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns the last employee in the ordered set where groupId = &#63;.
	*
	* @param groupId the group ID
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the last matching employee, or <code>null</code> if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee fetchByGroupId_Last(long groupId,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the employees before and after the current employee in the ordered set where groupId = &#63;.
	*
	* @param employeeId the primary key of the current employee
	* @param groupId the group ID
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the previous, current, and next employee
	* @throws db.hris.NoSuchEmployeeException if a employee with the primary key could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee[] findByGroupId_PrevAndNext(long employeeId,
		long groupId,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns all the employees that the user has permission to view where groupId = &#63;.
	*
	* @param groupId the group ID
	* @return the matching employees that the user has permission to view
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> filterFindByGroupId(
		long groupId)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns a range of all the employees that the user has permission to view where groupId = &#63;.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param groupId the group ID
	* @param start the lower bound of the range of employees
	* @param end the upper bound of the range of employees (not inclusive)
	* @return the range of matching employees that the user has permission to view
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> filterFindByGroupId(
		long groupId, int start, int end)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns an ordered range of all the employees that the user has permissions to view where groupId = &#63;.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param groupId the group ID
	* @param start the lower bound of the range of employees
	* @param end the upper bound of the range of employees (not inclusive)
	* @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	* @return the ordered range of matching employees that the user has permission to view
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> filterFindByGroupId(
		long groupId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the employees before and after the current employee in the ordered set of employees that the user has permission to view where groupId = &#63;.
	*
	* @param employeeId the primary key of the current employee
	* @param groupId the group ID
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the previous, current, and next employee
	* @throws db.hris.NoSuchEmployeeException if a employee with the primary key could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee[] filterFindByGroupId_PrevAndNext(
		long employeeId, long groupId,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns all the employees where portalUserId = &#63;.
	*
	* @param portalUserId the portal user ID
	* @return the matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByPortalUserId(
		long portalUserId)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns a range of all the employees where portalUserId = &#63;.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param portalUserId the portal user ID
	* @param start the lower bound of the range of employees
	* @param end the upper bound of the range of employees (not inclusive)
	* @return the range of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByPortalUserId(
		long portalUserId, int start, int end)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns an ordered range of all the employees where portalUserId = &#63;.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param portalUserId the portal user ID
	* @param start the lower bound of the range of employees
	* @param end the upper bound of the range of employees (not inclusive)
	* @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	* @return the ordered range of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByPortalUserId(
		long portalUserId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the first employee in the ordered set where portalUserId = &#63;.
	*
	* @param portalUserId the portal user ID
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the first matching employee
	* @throws db.hris.NoSuchEmployeeException if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee findByPortalUserId_First(long portalUserId,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns the first employee in the ordered set where portalUserId = &#63;.
	*
	* @param portalUserId the portal user ID
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the first matching employee, or <code>null</code> if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee fetchByPortalUserId_First(long portalUserId,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the last employee in the ordered set where portalUserId = &#63;.
	*
	* @param portalUserId the portal user ID
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the last matching employee
	* @throws db.hris.NoSuchEmployeeException if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee findByPortalUserId_Last(long portalUserId,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns the last employee in the ordered set where portalUserId = &#63;.
	*
	* @param portalUserId the portal user ID
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the last matching employee, or <code>null</code> if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee fetchByPortalUserId_Last(long portalUserId,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the employees before and after the current employee in the ordered set where portalUserId = &#63;.
	*
	* @param employeeId the primary key of the current employee
	* @param portalUserId the portal user ID
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the previous, current, and next employee
	* @throws db.hris.NoSuchEmployeeException if a employee with the primary key could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee[] findByPortalUserId_PrevAndNext(
		long employeeId, long portalUserId,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns all the employees where companyId = &#63;.
	*
	* @param companyId the company ID
	* @return the matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByCompanyId(
		long companyId)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns a range of all the employees where companyId = &#63;.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param companyId the company ID
	* @param start the lower bound of the range of employees
	* @param end the upper bound of the range of employees (not inclusive)
	* @return the range of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByCompanyId(
		long companyId, int start, int end)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns an ordered range of all the employees where companyId = &#63;.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param companyId the company ID
	* @param start the lower bound of the range of employees
	* @param end the upper bound of the range of employees (not inclusive)
	* @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	* @return the ordered range of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByCompanyId(
		long companyId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the first employee in the ordered set where companyId = &#63;.
	*
	* @param companyId the company ID
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the first matching employee
	* @throws db.hris.NoSuchEmployeeException if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee findByCompanyId_First(long companyId,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns the first employee in the ordered set where companyId = &#63;.
	*
	* @param companyId the company ID
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the first matching employee, or <code>null</code> if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee fetchByCompanyId_First(long companyId,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the last employee in the ordered set where companyId = &#63;.
	*
	* @param companyId the company ID
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the last matching employee
	* @throws db.hris.NoSuchEmployeeException if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee findByCompanyId_Last(long companyId,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns the last employee in the ordered set where companyId = &#63;.
	*
	* @param companyId the company ID
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the last matching employee, or <code>null</code> if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee fetchByCompanyId_Last(long companyId,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the employees before and after the current employee in the ordered set where companyId = &#63;.
	*
	* @param employeeId the primary key of the current employee
	* @param companyId the company ID
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the previous, current, and next employee
	* @throws db.hris.NoSuchEmployeeException if a employee with the primary key could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee[] findByCompanyId_PrevAndNext(
		long employeeId, long companyId,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns all the employees where lastName = &#63;.
	*
	* @param lastName the last name
	* @return the matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByLastName(
		java.lang.String lastName)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns a range of all the employees where lastName = &#63;.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param lastName the last name
	* @param start the lower bound of the range of employees
	* @param end the upper bound of the range of employees (not inclusive)
	* @return the range of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByLastName(
		java.lang.String lastName, int start, int end)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns an ordered range of all the employees where lastName = &#63;.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param lastName the last name
	* @param start the lower bound of the range of employees
	* @param end the upper bound of the range of employees (not inclusive)
	* @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	* @return the ordered range of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByLastName(
		java.lang.String lastName, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the first employee in the ordered set where lastName = &#63;.
	*
	* @param lastName the last name
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the first matching employee
	* @throws db.hris.NoSuchEmployeeException if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee findByLastName_First(
		java.lang.String lastName,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns the first employee in the ordered set where lastName = &#63;.
	*
	* @param lastName the last name
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the first matching employee, or <code>null</code> if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee fetchByLastName_First(
		java.lang.String lastName,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the last employee in the ordered set where lastName = &#63;.
	*
	* @param lastName the last name
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the last matching employee
	* @throws db.hris.NoSuchEmployeeException if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee findByLastName_Last(
		java.lang.String lastName,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns the last employee in the ordered set where lastName = &#63;.
	*
	* @param lastName the last name
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the last matching employee, or <code>null</code> if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee fetchByLastName_Last(
		java.lang.String lastName,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the employees before and after the current employee in the ordered set where lastName = &#63;.
	*
	* @param employeeId the primary key of the current employee
	* @param lastName the last name
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the previous, current, and next employee
	* @throws db.hris.NoSuchEmployeeException if a employee with the primary key could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee[] findByLastName_PrevAndNext(
		long employeeId, java.lang.String lastName,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns all the employees where firstName = &#63;.
	*
	* @param firstName the first name
	* @return the matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByFirstName(
		java.lang.String firstName)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns a range of all the employees where firstName = &#63;.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param firstName the first name
	* @param start the lower bound of the range of employees
	* @param end the upper bound of the range of employees (not inclusive)
	* @return the range of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByFirstName(
		java.lang.String firstName, int start, int end)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns an ordered range of all the employees where firstName = &#63;.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param firstName the first name
	* @param start the lower bound of the range of employees
	* @param end the upper bound of the range of employees (not inclusive)
	* @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	* @return the ordered range of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByFirstName(
		java.lang.String firstName, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the first employee in the ordered set where firstName = &#63;.
	*
	* @param firstName the first name
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the first matching employee
	* @throws db.hris.NoSuchEmployeeException if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee findByFirstName_First(
		java.lang.String firstName,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns the first employee in the ordered set where firstName = &#63;.
	*
	* @param firstName the first name
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the first matching employee, or <code>null</code> if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee fetchByFirstName_First(
		java.lang.String firstName,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the last employee in the ordered set where firstName = &#63;.
	*
	* @param firstName the first name
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the last matching employee
	* @throws db.hris.NoSuchEmployeeException if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee findByFirstName_Last(
		java.lang.String firstName,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns the last employee in the ordered set where firstName = &#63;.
	*
	* @param firstName the first name
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the last matching employee, or <code>null</code> if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee fetchByFirstName_Last(
		java.lang.String firstName,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the employees before and after the current employee in the ordered set where firstName = &#63;.
	*
	* @param employeeId the primary key of the current employee
	* @param firstName the first name
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the previous, current, and next employee
	* @throws db.hris.NoSuchEmployeeException if a employee with the primary key could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee[] findByFirstName_PrevAndNext(
		long employeeId, java.lang.String firstName,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns all the employees where middleName = &#63;.
	*
	* @param middleName the middle name
	* @return the matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByMiddleName(
		java.lang.String middleName)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns a range of all the employees where middleName = &#63;.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param middleName the middle name
	* @param start the lower bound of the range of employees
	* @param end the upper bound of the range of employees (not inclusive)
	* @return the range of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByMiddleName(
		java.lang.String middleName, int start, int end)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns an ordered range of all the employees where middleName = &#63;.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param middleName the middle name
	* @param start the lower bound of the range of employees
	* @param end the upper bound of the range of employees (not inclusive)
	* @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	* @return the ordered range of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByMiddleName(
		java.lang.String middleName, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the first employee in the ordered set where middleName = &#63;.
	*
	* @param middleName the middle name
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the first matching employee
	* @throws db.hris.NoSuchEmployeeException if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee findByMiddleName_First(
		java.lang.String middleName,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns the first employee in the ordered set where middleName = &#63;.
	*
	* @param middleName the middle name
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the first matching employee, or <code>null</code> if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee fetchByMiddleName_First(
		java.lang.String middleName,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the last employee in the ordered set where middleName = &#63;.
	*
	* @param middleName the middle name
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the last matching employee
	* @throws db.hris.NoSuchEmployeeException if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee findByMiddleName_Last(
		java.lang.String middleName,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns the last employee in the ordered set where middleName = &#63;.
	*
	* @param middleName the middle name
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the last matching employee, or <code>null</code> if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee fetchByMiddleName_Last(
		java.lang.String middleName,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the employees before and after the current employee in the ordered set where middleName = &#63;.
	*
	* @param employeeId the primary key of the current employee
	* @param middleName the middle name
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the previous, current, and next employee
	* @throws db.hris.NoSuchEmployeeException if a employee with the primary key could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee[] findByMiddleName_PrevAndNext(
		long employeeId, java.lang.String middleName,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns all the employees where taxNumber = &#63;.
	*
	* @param taxNumber the tax number
	* @return the matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByTaxNumberSearch(
		java.lang.String taxNumber)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns a range of all the employees where taxNumber = &#63;.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param taxNumber the tax number
	* @param start the lower bound of the range of employees
	* @param end the upper bound of the range of employees (not inclusive)
	* @return the range of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByTaxNumberSearch(
		java.lang.String taxNumber, int start, int end)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns an ordered range of all the employees where taxNumber = &#63;.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param taxNumber the tax number
	* @param start the lower bound of the range of employees
	* @param end the upper bound of the range of employees (not inclusive)
	* @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	* @return the ordered range of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findByTaxNumberSearch(
		java.lang.String taxNumber, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the first employee in the ordered set where taxNumber = &#63;.
	*
	* @param taxNumber the tax number
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the first matching employee
	* @throws db.hris.NoSuchEmployeeException if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee findByTaxNumberSearch_First(
		java.lang.String taxNumber,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns the first employee in the ordered set where taxNumber = &#63;.
	*
	* @param taxNumber the tax number
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the first matching employee, or <code>null</code> if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee fetchByTaxNumberSearch_First(
		java.lang.String taxNumber,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the last employee in the ordered set where taxNumber = &#63;.
	*
	* @param taxNumber the tax number
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the last matching employee
	* @throws db.hris.NoSuchEmployeeException if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee findByTaxNumberSearch_Last(
		java.lang.String taxNumber,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns the last employee in the ordered set where taxNumber = &#63;.
	*
	* @param taxNumber the tax number
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the last matching employee, or <code>null</code> if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee fetchByTaxNumberSearch_Last(
		java.lang.String taxNumber,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the employees before and after the current employee in the ordered set where taxNumber = &#63;.
	*
	* @param employeeId the primary key of the current employee
	* @param taxNumber the tax number
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the previous, current, and next employee
	* @throws db.hris.NoSuchEmployeeException if a employee with the primary key could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee[] findByTaxNumberSearch_PrevAndNext(
		long employeeId, java.lang.String taxNumber,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns all the employees where sssNumber = &#63;.
	*
	* @param sssNumber the sss number
	* @return the matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findBySssNumberSearch(
		java.lang.String sssNumber)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns a range of all the employees where sssNumber = &#63;.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param sssNumber the sss number
	* @param start the lower bound of the range of employees
	* @param end the upper bound of the range of employees (not inclusive)
	* @return the range of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findBySssNumberSearch(
		java.lang.String sssNumber, int start, int end)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns an ordered range of all the employees where sssNumber = &#63;.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param sssNumber the sss number
	* @param start the lower bound of the range of employees
	* @param end the upper bound of the range of employees (not inclusive)
	* @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	* @return the ordered range of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findBySssNumberSearch(
		java.lang.String sssNumber, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the first employee in the ordered set where sssNumber = &#63;.
	*
	* @param sssNumber the sss number
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the first matching employee
	* @throws db.hris.NoSuchEmployeeException if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee findBySssNumberSearch_First(
		java.lang.String sssNumber,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns the first employee in the ordered set where sssNumber = &#63;.
	*
	* @param sssNumber the sss number
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the first matching employee, or <code>null</code> if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee fetchBySssNumberSearch_First(
		java.lang.String sssNumber,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the last employee in the ordered set where sssNumber = &#63;.
	*
	* @param sssNumber the sss number
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the last matching employee
	* @throws db.hris.NoSuchEmployeeException if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee findBySssNumberSearch_Last(
		java.lang.String sssNumber,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns the last employee in the ordered set where sssNumber = &#63;.
	*
	* @param sssNumber the sss number
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the last matching employee, or <code>null</code> if a matching employee could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee fetchBySssNumberSearch_Last(
		java.lang.String sssNumber,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the employees before and after the current employee in the ordered set where sssNumber = &#63;.
	*
	* @param employeeId the primary key of the current employee
	* @param sssNumber the sss number
	* @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	* @return the previous, current, and next employee
	* @throws db.hris.NoSuchEmployeeException if a employee with the primary key could not be found
	* @throws SystemException if a system exception occurred
	*/
	public db.hris.model.Employee[] findBySssNumberSearch_PrevAndNext(
		long employeeId, java.lang.String sssNumber,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException,
			db.hris.NoSuchEmployeeException;

	/**
	* Returns all the employees.
	*
	* @return the employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findAll()
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns a range of all the employees.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param start the lower bound of the range of employees
	* @param end the upper bound of the range of employees (not inclusive)
	* @return the range of employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findAll(int start, int end)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns an ordered range of all the employees.
	*
	* <p>
	* Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set.
	* </p>
	*
	* @param start the lower bound of the range of employees
	* @param end the upper bound of the range of employees (not inclusive)
	* @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	* @return the ordered range of employees
	* @throws SystemException if a system exception occurred
	*/
	public java.util.List<db.hris.model.Employee> findAll(int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator orderByComparator)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Removes all the employees where groupId = &#63; from the database.
	*
	* @param groupId the group ID
	* @throws SystemException if a system exception occurred
	*/
	public void removeByGroupId(long groupId)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Removes all the employees where portalUserId = &#63; from the database.
	*
	* @param portalUserId the portal user ID
	* @throws SystemException if a system exception occurred
	*/
	public void removeByPortalUserId(long portalUserId)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Removes all the employees where companyId = &#63; from the database.
	*
	* @param companyId the company ID
	* @throws SystemException if a system exception occurred
	*/
	public void removeByCompanyId(long companyId)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Removes all the employees where lastName = &#63; from the database.
	*
	* @param lastName the last name
	* @throws SystemException if a system exception occurred
	*/
	public void removeByLastName(java.lang.String lastName)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Removes all the employees where firstName = &#63; from the database.
	*
	* @param firstName the first name
	* @throws SystemException if a system exception occurred
	*/
	public void removeByFirstName(java.lang.String firstName)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Removes all the employees where middleName = &#63; from the database.
	*
	* @param middleName the middle name
	* @throws SystemException if a system exception occurred
	*/
	public void removeByMiddleName(java.lang.String middleName)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Removes all the employees where taxNumber = &#63; from the database.
	*
	* @param taxNumber the tax number
	* @throws SystemException if a system exception occurred
	*/
	public void removeByTaxNumberSearch(java.lang.String taxNumber)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Removes all the employees where sssNumber = &#63; from the database.
	*
	* @param sssNumber the sss number
	* @throws SystemException if a system exception occurred
	*/
	public void removeBySssNumberSearch(java.lang.String sssNumber)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Removes all the employees from the database.
	*
	* @throws SystemException if a system exception occurred
	*/
	public void removeAll()
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the number of employees where groupId = &#63;.
	*
	* @param groupId the group ID
	* @return the number of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public int countByGroupId(long groupId)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the number of employees that the user has permission to view where groupId = &#63;.
	*
	* @param groupId the group ID
	* @return the number of matching employees that the user has permission to view
	* @throws SystemException if a system exception occurred
	*/
	public int filterCountByGroupId(long groupId)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the number of employees where portalUserId = &#63;.
	*
	* @param portalUserId the portal user ID
	* @return the number of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public int countByPortalUserId(long portalUserId)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the number of employees where companyId = &#63;.
	*
	* @param companyId the company ID
	* @return the number of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public int countByCompanyId(long companyId)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the number of employees where lastName = &#63;.
	*
	* @param lastName the last name
	* @return the number of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public int countByLastName(java.lang.String lastName)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the number of employees where firstName = &#63;.
	*
	* @param firstName the first name
	* @return the number of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public int countByFirstName(java.lang.String firstName)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the number of employees where middleName = &#63;.
	*
	* @param middleName the middle name
	* @return the number of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public int countByMiddleName(java.lang.String middleName)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the number of employees where taxNumber = &#63;.
	*
	* @param taxNumber the tax number
	* @return the number of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public int countByTaxNumberSearch(java.lang.String taxNumber)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the number of employees where sssNumber = &#63;.
	*
	* @param sssNumber the sss number
	* @return the number of matching employees
	* @throws SystemException if a system exception occurred
	*/
	public int countBySssNumberSearch(java.lang.String sssNumber)
		throws com.liferay.portal.kernel.exception.SystemException;

	/**
	* Returns the number of employees.
	*
	* @return the number of employees
	* @throws SystemException if a system exception occurred
	*/
	public int countAll()
		throws com.liferay.portal.kernel.exception.SystemException;
}
