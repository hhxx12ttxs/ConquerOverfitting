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

package com.liferay.portal.service.impl;

import com.liferay.portal.CompanyMaxUsersException;
import com.liferay.portal.ContactBirthdayException;
import com.liferay.portal.ContactFirstNameException;
import com.liferay.portal.ContactFullNameException;
import com.liferay.portal.ContactLastNameException;
import com.liferay.portal.DuplicateOpenIdException;
import com.liferay.portal.DuplicateUserEmailAddressException;
import com.liferay.portal.DuplicateUserScreenNameException;
import com.liferay.portal.GroupFriendlyURLException;
import com.liferay.portal.ModelListenerException;
import com.liferay.portal.NoSuchContactException;
import com.liferay.portal.NoSuchGroupException;
import com.liferay.portal.NoSuchOrganizationException;
import com.liferay.portal.NoSuchRoleException;
import com.liferay.portal.NoSuchTicketException;
import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.NoSuchUserGroupException;
import com.liferay.portal.PasswordExpiredException;
import com.liferay.portal.RequiredUserException;
import com.liferay.portal.ReservedUserEmailAddressException;
import com.liferay.portal.ReservedUserScreenNameException;
import com.liferay.portal.UserEmailAddressException;
import com.liferay.portal.UserIdException;
import com.liferay.portal.UserLockoutException;
import com.liferay.portal.UserPasswordException;
import com.liferay.portal.UserPortraitSizeException;
import com.liferay.portal.UserPortraitTypeException;
import com.liferay.portal.UserReminderQueryException;
import com.liferay.portal.UserScreenNameException;
import com.liferay.portal.UserSmsException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.image.ImageBag;
import com.liferay.portal.kernel.image.ImageToolUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.spring.aop.Skip;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.TransactionCommitCallbackRegistryUtil;
import com.liferay.portal.kernel.transaction.Transactional;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.Digester;
import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.KeyValuePair;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowHandlerRegistryUtil;
import com.liferay.portal.kernel.workflow.WorkflowThreadLocal;
import com.liferay.portal.model.Account;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.CompanyConstants;
import com.liferay.portal.model.Contact;
import com.liferay.portal.model.ContactConstants;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.GroupConstants;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.Organization;
import com.liferay.portal.model.PasswordPolicy;
import com.liferay.portal.model.ResourceConstants;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.model.Team;
import com.liferay.portal.model.Ticket;
import com.liferay.portal.model.TicketConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserGroup;
import com.liferay.portal.model.UserGroupRole;
import com.liferay.portal.model.impl.LayoutImpl;
import com.liferay.portal.security.auth.AuthPipeline;
import com.liferay.portal.security.auth.Authenticator;
import com.liferay.portal.security.auth.EmailAddressGenerator;
import com.liferay.portal.security.auth.EmailAddressGeneratorFactory;
import com.liferay.portal.security.auth.EmailAddressValidator;
import com.liferay.portal.security.auth.EmailAddressValidatorFactory;
import com.liferay.portal.security.auth.FullNameGenerator;
import com.liferay.portal.security.auth.FullNameGeneratorFactory;
import com.liferay.portal.security.auth.FullNameValidator;
import com.liferay.portal.security.auth.FullNameValidatorFactory;
import com.liferay.portal.security.auth.PrincipalException;
import com.liferay.portal.security.auth.ScreenNameGenerator;
import com.liferay.portal.security.auth.ScreenNameGeneratorFactory;
import com.liferay.portal.security.auth.ScreenNameValidator;
import com.liferay.portal.security.auth.ScreenNameValidatorFactory;
import com.liferay.portal.security.ldap.LDAPSettingsUtil;
import com.liferay.portal.security.permission.PermissionCacheUtil;
import com.liferay.portal.security.pwd.PwdAuthenticator;
import com.liferay.portal.security.pwd.PwdEncryptor;
import com.liferay.portal.security.pwd.PwdToolkitUtil;
import com.liferay.portal.security.pwd.RegExpToolkit;
import com.liferay.portal.service.BaseServiceImpl;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.base.UserLocalServiceBaseImpl;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.PrefsPropsUtil;
import com.liferay.portal.util.PropsValues;
import com.liferay.portal.util.SubscriptionSender;
import com.liferay.portlet.documentlibrary.ImageSizeException;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.usersadmin.util.UsersAdminUtil;
import com.liferay.util.Encryptor;
import com.liferay.util.EncryptorException;

import java.awt.image.RenderedImage;

import java.io.IOException;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The implementation of the user local service.
 *
 * @author Brian Wing Shun Chan
 * @author Scott Lee
 * @author Raymond Aug?
 * @author Jorge Ferrer
 * @author Julio Camarero
 * @author Wesley Gong
 * @author Zsigmond Rab
 */
public class UserLocalServiceImpl extends UserLocalServiceBaseImpl {

	/**
	 * Adds a default admin user for the company.
	 *
	 * @param  companyId the primary key of the user's company
	 * @param  screenName the user's screen name
	 * @param  emailAddress the user's email address
	 * @param  locale the user's locale
	 * @param  firstName the user's first name
	 * @param  middleName the user's middle name
	 * @param  lastName the user's last name
	 * @return the new default admin user
	 * @throws PortalException n if a portal exception occurred
	 * @throws SystemException if a system exception occurred
	 */
	public User addDefaultAdminUser(
			long companyId, String screenName, String emailAddress,
			Locale locale, String firstName, String middleName, String lastName)
		throws PortalException, SystemException {

		long creatorUserId = 0;
		boolean autoPassword = false;
		String password1 = PropsValues.DEFAULT_ADMIN_PASSWORD;
		String password2 = password1;
		boolean autoScreenName = false;

		screenName = getScreenName(screenName);

		for (int i = 1;; i++) {
			User screenNameUser = userPersistence.fetchByC_SN(
				companyId, screenName);

			if (screenNameUser == null) {
				break;
			}

			screenName = screenName + i;
		}

		long facebookId = 0;
		String openId = StringPool.BLANK;
		int prefixId = 0;
		int suffixId = 0;
		boolean male = true;
		int birthdayMonth = Calendar.JANUARY;
		int birthdayDay = 1;
		int birthdayYear = 1970;
		String jobTitle = StringPool.BLANK;

		Group guestGroup = groupLocalService.getGroup(
			companyId, GroupConstants.GUEST);

		long[] groupIds = {guestGroup.getGroupId()};

		long[] organizationIds = null;

		Role adminRole = roleLocalService.getRole(
			companyId, RoleConstants.ADMINISTRATOR);

		Role powerUserRole = roleLocalService.getRole(
			companyId, RoleConstants.POWER_USER);

		long[] roleIds = {adminRole.getRoleId(), powerUserRole.getRoleId()};

		long[] userGroupIds = null;
		boolean sendEmail = false;
		ServiceContext serviceContext = new ServiceContext();

		User defaultAdminUser = addUser(
			creatorUserId, companyId, autoPassword, password1, password2,
			autoScreenName, screenName, emailAddress, facebookId, openId,
			locale, firstName, middleName, lastName, prefixId, suffixId, male,
			birthdayMonth, birthdayDay, birthdayYear, jobTitle, groupIds,
			organizationIds, roleIds, userGroupIds, sendEmail, serviceContext);

		updateEmailAddressVerified(defaultAdminUser.getUserId(), true);

		updateLastLogin(
			defaultAdminUser.getUserId(), defaultAdminUser.getLoginIP());

		updatePasswordReset(defaultAdminUser.getUserId(), false);

		return defaultAdminUser;
	}

	/**
	 * Adds the user to the default groups, unless the user is already in these
	 * groups. The default groups can be specified in
	 * <code>portal.properties</code> with the key
	 * <code>admin.default.group.names</code>.
	 *
	 * @param  userId the primary key of the user
	 * @throws PortalException if a user with the primary key could not be found
	 * @throws SystemException if a system exception occurred
	 */
	public void addDefaultGroups(long userId)
		throws PortalException, SystemException {

		User user = userPersistence.findByPrimaryKey(userId);

		Set<Long> groupIdsSet = new HashSet<Long>();

		String[] defaultGroupNames = PrefsPropsUtil.getStringArray(
			user.getCompanyId(), PropsKeys.ADMIN_DEFAULT_GROUP_NAMES,
			StringPool.NEW_LINE, PropsValues.ADMIN_DEFAULT_GROUP_NAMES);

		for (String defaultGroupName : defaultGroupNames) {
			Company company = companyPersistence.findByPrimaryKey(
				user.getCompanyId());

			Account account = company.getAccount();

			if (defaultGroupName.equalsIgnoreCase(account.getName())) {
				defaultGroupName = GroupConstants.GUEST;
			}

			try {
				Group group = groupPersistence.findByC_N(
					user.getCompanyId(), defaultGroupName);

				if (!userPersistence.containsGroup(
						userId, group.getGroupId())) {

					groupIdsSet.add(group.getGroupId());
				}
			}
			catch (NoSuchGroupException nsge) {
			}
		}

		String[] defaultOrganizationGroupNames = PrefsPropsUtil.getStringArray(
			user.getCompanyId(),
			PropsKeys.ADMIN_DEFAULT_ORGANIZATION_GROUP_NAMES,
			StringPool.NEW_LINE,
			PropsValues.ADMIN_DEFAULT_ORGANIZATION_GROUP_NAMES);

		for (String defaultOrganizationGroupName :
				defaultOrganizationGroupNames) {

			defaultOrganizationGroupName +=
				GroupLocalServiceImpl.ORGANIZATION_NAME_SUFFIX;

			try {
				Group group = groupPersistence.findByC_N(
					user.getCompanyId(), defaultOrganizationGroupName);

				if (!userPersistence.containsGroup(
						userId, group.getGroupId())) {

					groupIdsSet.add(group.getGroupId());
				}
			}
			catch (NoSuchGroupException nsge) {
			}
		}

		long[] groupIds = ArrayUtil.toArray(
			groupIdsSet.toArray(new Long[groupIdsSet.size()]));

		groupLocalService.addUserGroups(userId, groupIds);
	}

	/**
	 * Adds the user to the default roles, unless the user already has these
	 * roles. The default roles can be specified in
	 * <code>portal.properties</code> with the key
	 * <code>admin.default.role.names</code>.
	 *
	 * @param  userId the primary key of the user
	 * @throws PortalException if a user with the primary key could not be found
	 * @throws SystemException if a system exception occurred
	 */
	public void addDefaultRoles(long userId)
		throws PortalException, SystemException {

		User user = userPersistence.findByPrimaryKey(userId);

		Set<Long> roleIdSet = new HashSet<Long>();

		String[] defaultRoleNames = PrefsPropsUtil.getStringArray(
			user.getCompanyId(), PropsKeys.ADMIN_DEFAULT_ROLE_NAMES,
			StringPool.NEW_LINE, PropsValues.ADMIN_DEFAULT_ROLE_NAMES);

		for (String defaultRoleName : defaultRoleNames) {
			try {
				Role role = rolePersistence.findByC_N(
					user.getCompanyId(), defaultRoleName);

				if (!userPersistence.containsRole(userId, role.getRoleId())) {
					roleIdSet.add(role.getRoleId());
				}
			}
			catch (NoSuchRoleException nsre) {
			}
		}

		long[] roleIds = ArrayUtil.toArray(
			roleIdSet.toArray(new Long[roleIdSet.size()]));

		roleIds = UsersAdminUtil.addRequiredRoles(user, roleIds);

		userPersistence.addRoles(userId, roleIds);
	}

	/**
	 * Adds the user to the default user groups, unless the user is already in
	 * these user groups. The default user groups can be specified in
	 * <code>portal.properties</code> with the property
	 * <code>admin.default.user.group.names</code>.
	 *
	 * @param  userId the primary key of the user
	 * @throws PortalException if a user with the primary key could not be found
	 * @throws SystemException if a system exception occurred
	 */
	@SuppressWarnings("deprecation")
	public void addDefaultUserGroups(long userId)
		throws PortalException, SystemException {

		User user = userPersistence.findByPrimaryKey(userId);

		Set<Long> userGroupIdSet = new HashSet<Long>();

		String[] defaultUserGroupNames = PrefsPropsUtil.getStringArray(
			user.getCompanyId(), PropsKeys.ADMIN_DEFAULT_USER_GROUP_NAMES,
			StringPool.NEW_LINE, PropsValues.ADMIN_DEFAULT_USER_GROUP_NAMES);

		for (String defaultUserGroupName : defaultUserGroupNames) {
			try {
				UserGroup userGroup = userGroupPersistence.findByC_N(
					user.getCompanyId(), defaultUserGroupName);

				if (!userPersistence.containsUserGroup(
						userId, userGroup.getUserGroupId())) {

					userGroupIdSet.add(userGroup.getUserGroupId());
				}
			}
			catch (NoSuchUserGroupException nsuge) {
			}
		}

		long[] userGroupIds = ArrayUtil.toArray(
			userGroupIdSet.toArray(new Long[userGroupIdSet.size()]));

		if (PropsValues.USER_GROUPS_COPY_LAYOUTS_TO_USER_PERSONAL_SITE) {
			for (long userGroupId : userGroupIds) {
				userGroupLocalService.copyUserGroupLayouts(userGroupId, userId);
			}
		}

		userPersistence.addUserGroups(userId, userGroupIds);
	}

	/**
	 * Adds the users to the group.
	 *
	 * @param  groupId the primary key of the group
	 * @param  userIds the primary keys of the users
	 * @throws PortalException if a group or user with the primary key could not
	 *         be found
	 * @throws SystemException if a system exception occurred
	 */
	public void addGroupUsers(long groupId, long[] userIds)
		throws PortalException, SystemException {

		groupPersistence.addUsers(groupId, userIds);

		Indexer indexer = IndexerRegistryUtil.nullSafeGetIndexer(User.class);

		indexer.reindex(userIds);

		PermissionCacheUtil.clearCache();

		addDefaultRolesAndTeams(groupId, userIds);
	}

	/**
	 * Adds the users to the organization.
	 *
	 * @param  organizationId the primary key of the organization
	 * @param  userIds the primary keys of the users
	 * @throws PortalException if an organization or user with the primary key
	 *         could not be found
	 * @throws SystemException if a system exception occurred
	 */
	public void addOrganizationUsers(long organizationId, long[] userIds)
		throws PortalException, SystemException {

		organizationPersistence.addUsers(organizationId, userIds);

		Indexer indexer = IndexerRegistryUtil.nullSafeGetIndexer(User.class);

		indexer.reindex(userIds);

		PermissionCacheUtil.clearCache();
	}

	/**
	 * Assigns the password policy to the users, removing any other currently
	 * assigned password policies.
	 *
	 * @param  passwordPolicyId the primary key of the password policy
	 * @param  userIds the primary keys of the users
	 * @throws SystemException if a system exception occurred
	 */
	public void addPasswordPolicyUsers(long passwordPolicyId, long[] userIds)
		throws SystemException {

		passwordPolicyRelLocalService.addPasswordPolicyRels(
			passwordPolicyId, User.class.getName(), userIds);
	}

	/**
	 * Adds the users to the role.
	 *
	 * @param  roleId the primary key of the role
	 * @param  userIds the primary keys of the users
	 * @throws PortalException if a role or user with the primary key could not
	 *         be found
	 * @throws SystemException if a system exception occurred
	 */
	public void addRoleUsers(long roleId, long[] userIds)
		throws PortalException, SystemException {

		rolePersistence.addUsers(roleId, userIds);

		Indexer indexer = IndexerRegistryUtil.nullSafeGetIndexer(User.class);

		indexer.reindex(userIds);

		PermissionCacheUtil.clearCache();
	}

	/**
	 * Adds the users to the team.
	 *
	 * @param  teamId the primary key of the team
	 * @param  userIds the primary keys of the users
	 * @throws PortalException if a team or user with the primary key could not
	 *         be found
	 * @throws SystemException if a system exception occurred
	 */
	public void addTeamUsers(long teamId, long[] userIds)
		throws PortalException, SystemException {

		teamPersistence.addUsers(teamId, userIds);

		Indexer indexer = IndexerRegistryUtil.nullSafeGetIndexer(User.class);

		indexer.reindex(userIds);

		PermissionCacheUtil.clearCache();
	}

	/**
	 * Adds a user.
	 *
	 * <p>
	 * This method handles the creation and bookkeeping of the user including
	 * its resources, metadata, and internal data structures. It is not
	 * necessary to make subsequent calls to any methods to setup default
	 * groups, resources, etc.
	 * </p>
	 *
	 * @param  creatorUserId the primary key of the creator
	 * @param  companyId the primary key of the user's company
	 * @param  autoPassword whether a password should be automatically generated
	 *         for the user
	 * @param  password1 the user's password
	 * @param  password2 the user's password confirmation
	 * @param  autoScreenName whether a screen name should be automatically
	 *         generated for the user
	 * @param  screenName the user's screen name
	 * @param  emailAddress the user's email address
	 * @param  facebookId the user's facebook ID
	 * @param  openId the user's OpenID
	 * @param  locale the user's locale
	 * @param  firstName the user's first name
	 * @param  middleName the user's middle name
	 * @param  lastName the user's last name
	 * @param  prefixId the user's name prefix ID
	 * @param  suffixId the user's name suffix ID
	 * @param  male whether the user is male
	 * @param  birthdayMonth the user's birthday month (0-based, meaning 0 for
	 *         January)
	 * @param  birthdayDay the user's birthday day
	 * @param  birthdayYear the user's birthday year
	 * @param  jobTitle the user's job title
	 * @param  groupIds the primary keys of the user's groups
	 * @param  organizationIds the primary keys of the user's organizations
	 * @param  roleIds the primary keys of the roles this user possesses
	 * @param  userGroupIds the primary keys of the user's user groups
	 * @param  sendEmail whether to send the user an email notification about
	 *         their new account
	 * @param  serviceContext the user's service context (optionally
	 *         <code>null</code>). Can set the universally unique identifier
	 *         (with the <code>uuid</code> attribute), asset category IDs, asset
	 *         tag names, and expando bridge attributes for the user.
	 * @return the new user
	 * @throws PortalException if the user's information was invalid
	 * @throws SystemException if a system exception occurred
	 */
	public User addUser(
			long creatorUserId, long companyId, boolean autoPassword,
			String password1, String password2, boolean autoScreenName,
			String screenName, String emailAddress, long facebookId,
			String openId, Locale locale, String firstName, String middleName,
			String lastName, int prefixId, int suffixId, boolean male,
			int birthdayMonth, int birthdayDay, int birthdayYear,
			String jobTitle, long[] groupIds, long[] organizationIds,
			long[] roleIds, long[] userGroupIds, boolean sendEmail,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		boolean workflowEnabled = WorkflowThreadLocal.isEnabled();

		try {
			WorkflowThreadLocal.setEnabled(false);

			return addUserWithWorkflow(
				creatorUserId, companyId, autoPassword, password1, password2,
				autoScreenName, screenName, emailAddress, facebookId, openId,
				locale, firstName, middleName, lastName, prefixId, suffixId,
				male, birthdayMonth, birthdayDay, birthdayYear, jobTitle,
				groupIds, organizationIds, roleIds, userGroupIds, sendEmail,
				serviceContext);
		}
		finally {
			WorkflowThreadLocal.setEnabled(workflowEnabled);
		}
	}

	/**
	 * Adds the users to the user group.
	 *
	 * @param  userGroupId the primary key of the user group
	 * @param  userIds the primary keys of the users
	 * @throws PortalException if a user group or user with the primary could
	 *         could not be found
	 * @throws SystemException if a system exception occurred
	 */
	@SuppressWarnings("deprecation")
	public void addUserGroupUsers(long userGroupId, long[] userIds)
		throws PortalException, SystemException {

		if (PropsValues.USER_GROUPS_COPY_LAYOUTS_TO_USER_PERSONAL_SITE) {
			userGroupLocalService.copyUserGroupLayouts(userGroupId, userIds);
		}

		userGroupPersistence.addUsers(userGroupId, userIds);

		Indexer indexer = IndexerRegistryUtil.nullSafeGetIndexer(User.class);

		indexer.reindex(userIds);

		PermissionCacheUtil.clearCache();
	}

	/**
	 * Adds a user with workflow.
	 *
	 * <p>
	 * This method handles the creation and bookkeeping of the user including
	 * its resources, metadata, and internal data structures. It is not
	 * necessary to make subsequent calls to any methods to setup default
	 * groups, resources, etc.
	 * </p>
	 *
	 * @param  creatorUserId the primary key of the creator
	 * @param  companyId the primary key of the user's company
	 * @param  autoPassword whether a password should be automatically generated
	 *         for the user
	 * @param  password1 the user's password
	 * @param  password2 the user's password confirmation
	 * @param  autoScreenName whether a screen name should be automatically
	 *         generated for the user
	 * @param  screenName the user's screen name
	 * @param  emailAddress the user's email address
	 * @param  facebookId the user's facebook ID
	 * @param  openId the user's OpenID
	 * @param  locale the user's locale
	 * @param  firstName the user's first name
	 * @param  middleName the user's middle name
	 * @param  lastName the user's last name
	 * @param  prefixId the user's name prefix ID
	 * @param  suffixId the user's name suffix ID
	 * @param  male whether the user is male
	 * @param  birthdayMonth the user's birthday month (0-based, meaning 0 for
	 *         January)
	 * @param  birthdayDay the user's birthday day
	 * @param  birthdayYear the user's birthday year
	 * @param  jobTitle the user's job title
	 * @param  groupIds the primary keys of the user's groups
	 * @param  organizationIds the primary keys of the user's organizations
	 * @param  roleIds the primary keys of the roles this user possesses
	 * @param  userGroupIds the primary keys of the user's user groups
	 * @param  sendEmail whether to send the user an email notification about
	 *         their new account
	 * @param  serviceContext the user's service context (optionally
	 *         <code>null</code>). Can set the universally unique identifier
	 *         (with the <code>uuid</code> attribute), asset category IDs, asset
	 *         tag names, and expando bridge attributes for the user.
	 * @return the new user
	 * @throws PortalException if the user's information was invalid
	 * @throws SystemException if a system exception occurred
	 */
	@SuppressWarnings("deprecation")
	public User addUserWithWorkflow(
			long creatorUserId, long companyId, boolean autoPassword,
			String password1, String password2, boolean autoScreenName,
			String screenName, String emailAddress, long facebookId,
			String openId, Locale locale, String firstName, String middleName,
			String lastName, int prefixId, int suffixId, boolean male,
			int birthdayMonth, int birthdayDay, int birthdayYear,
			String jobTitle, long[] groupIds, long[] organizationIds,
			long[] roleIds, long[] userGroupIds, boolean sendEmail,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		// User

		Company company = companyPersistence.findByPrimaryKey(companyId);
		screenName = getScreenName(screenName);
		emailAddress = emailAddress.trim().toLowerCase();
		openId = openId.trim();
		Date now = new Date();

		if (PrefsPropsUtil.getBoolean(
				companyId, PropsKeys.USERS_SCREEN_NAME_ALWAYS_AUTOGENERATE)) {

			autoScreenName = true;
		}

		// PLACEHOLDER 01

		long userId = counterLocalService.increment();

		EmailAddressGenerator emailAddressGenerator =
			EmailAddressGeneratorFactory.getInstance();

		if (emailAddressGenerator.isGenerated(emailAddress)) {
			emailAddress = StringPool.BLANK;
		}

		if (!PropsValues.USERS_EMAIL_ADDRESS_REQUIRED &&
			Validator.isNull(emailAddress)) {

			emailAddress = emailAddressGenerator.generate(companyId, userId);
		}

		validate(
			companyId, userId, autoPassword, password1, password2,
			autoScreenName, screenName, emailAddress, openId, firstName,
			middleName, lastName, organizationIds);

		if (!autoPassword) {
			if (Validator.isNull(password1) || Validator.isNull(password2)) {
				throw new UserPasswordException(
					UserPasswordException.PASSWORD_INVALID);
			}
		}

		if (autoScreenName) {
			ScreenNameGenerator screenNameGenerator =
				ScreenNameGeneratorFactory.getInstance();

			try {
				screenName = screenNameGenerator.generate(
					companyId, userId, emailAddress);
			}
			catch (Exception e) {
				throw new SystemException(e);
			}
		}

		User defaultUser = getDefaultUser(companyId);

		FullNameGenerator fullNameGenerator =
			FullNameGeneratorFactory.getInstance();

		String fullName = fullNameGenerator.getFullName(
			firstName, middleName, lastName);

		String greeting = LanguageUtil.format(
			locale, "welcome-x", " " + fullName, false);

		User user = userPersistence.create(userId);

		if (serviceContext != null) {
			String uuid = serviceContext.getUuid();

			if (Validator.isNotNull(uuid)) {
				user.setUuid(uuid);
			}
		}

		user.setCompanyId(companyId);
		user.setCreateDate(now);
		user.setModifiedDate(now);
		user.setDefaultUser(false);
		user.setContactId(counterLocalService.increment());

		if (Validator.isNotNull(password1)) {
			user.setPassword(PwdEncryptor.encrypt(password1));
			user.setPasswordUnencrypted(password1);
		}

		user.setPasswordEncrypted(true);

		PasswordPolicy passwordPolicy = defaultUser.getPasswordPolicy();

		if ((passwordPolicy != null) && passwordPolicy.isChangeable() &&
			passwordPolicy.isChangeRequired()) {

			user.setPasswordReset(true);
		}
		else {
			user.setPasswordReset(false);
		}

		user.setDigest(StringPool.BLANK);
		user.setScreenName(screenName);
		user.setEmailAddress(emailAddress);
		user.setFacebookId(facebookId);

		Long ldapServerId = (Long)serviceContext.getAttribute("ldapServerId");

		if (ldapServerId != null) {
			user.setLdapServerId(ldapServerId);
		}
		else {
			user.setLdapServerId(-1);
		}

		user.setOpenId(openId);
		user.setLanguageId(locale.toString());
		user.setTimeZoneId(defaultUser.getTimeZoneId());
		user.setGreeting(greeting);
		user.setFirstName(firstName);
		user.setMiddleName(middleName);
		user.setLastName(lastName);
		user.setJobTitle(jobTitle);
		user.setStatus(WorkflowConstants.STATUS_DRAFT);
		user.setExpandoBridgeAttributes(serviceContext);

		userPersistence.update(user, serviceContext);

		// Resources

		String creatorUserName = StringPool.BLANK;

		if (creatorUserId <= 0) {
			creatorUserId = user.getUserId();

			// Don't grab the full name from the User object because it doesn't
			// have a corresponding Contact object yet

			//creatorUserName = user.getFullName();
		}
		else {
			User creatorUser = userPersistence.findByPrimaryKey(creatorUserId);

			creatorUserName = creatorUser.getFullName();
		}

		resourceLocalService.addResources(
			companyId, 0, creatorUserId, User.class.getName(), user.getUserId(),
			false, false, false);

		// Contact

		Date birthday = getBirthday(birthdayMonth, birthdayDay, birthdayYear);

		Contact contact = contactPersistence.create(user.getContactId());

		contact.setCompanyId(user.getCompanyId());
		contact.setUserId(creatorUserId);
		contact.setUserName(creatorUserName);
		contact.setCreateDate(now);
		contact.setModifiedDate(now);
		contact.setClassName(User.class.getName());
		contact.setClassPK(user.getUserId());
		contact.setAccountId(company.getAccountId());
		contact.setParentContactId(ContactConstants.DEFAULT_PARENT_CONTACT_ID);
		contact.setEmailAddress(user.getEmailAddress());
		contact.setFirstName(firstName);
		contact.setMiddleName(middleName);
		contact.setLastName(lastName);
		contact.setPrefixId(prefixId);
		contact.setSuffixId(suffixId);
		contact.setMale(male);
		contact.setBirthday(birthday);
		contact.setJobTitle(jobTitle);

		contactPersistence.update(contact, serviceContext);

		// Group

		groupLocalService.addGroup(
			user.getUserId(), GroupConstants.DEFAULT_PARENT_GROUP_ID,
			User.class.getName(), user.getUserId(), null, null, 0,
			StringPool.SLASH + screenName, false, true, null);

		// Groups

		if (groupIds != null) {
			groupLocalService.addUserGroups(userId, groupIds);
		}

		addDefaultGroups(userId);

		// Organizations

		updateOrganizations(userId, organizationIds, false);

		// Roles

		if (roleIds != null) {
			roleIds = UsersAdminUtil.addRequiredRoles(user, roleIds);

			userPersistence.setRoles(userId, roleIds);
		}

		addDefaultRoles(userId);

		// User groups

		if (userGroupIds != null) {
			if (PropsValues.USER_GROUPS_COPY_LAYOUTS_TO_USER_PERSONAL_SITE) {
				for (long userGroupId : userGroupIds) {
					userGroupLocalService.copyUserGroupLayouts(
						userGroupId, new long[] {userId});
				}
			}

			userPersistence.setUserGroups(userId, userGroupIds);
		}

		addDefaultUserGroups(userId);

		// Asset

		if (serviceContext != null) {
			updateAsset(
				creatorUserId, user, serviceContext.getAssetCategoryIds(),
				serviceContext.getAssetTagNames());
		}

		// Indexer

		if ((serviceContext == null) || serviceContext.isIndexingEnabled()) {
			reindex(user);
		}

		// Workflow

		long workflowUserId = creatorUserId;

		if (workflowUserId == userId) {
			workflowUserId = defaultUser.getUserId();
		}

		ServiceContext workflowServiceContext = serviceContext;

		if (workflowServiceContext == null) {
			workflowServiceContext = new ServiceContext();
		}

		workflowServiceContext.setAttribute("autoPassword", autoPassword);
		workflowServiceContext.setAttribute("sendEmail", sendEmail);

		WorkflowHandlerRegistryUtil.startWorkflowInstance(
			companyId, workflowUserId, User.class.getName(), userId, user,
			workflowServiceContext);

		if (serviceContext != null) {
			String passwordUnencrypted = (String)serviceContext.getAttribute(
				"passwordUnencrypted");

			if (Validator.isNotNull(passwordUnencrypted)) {
				user.setPasswordUnencrypted(passwordUnencrypted);
			}
		}

		return user;
	}

	/**
	 * Attempts to authenticate the user by their email address and password,
	 * while using the AuthPipeline.
	 *
	 * @param  companyId the primary key of the user's company
	 * @param  emailAddress the user's email address
	 * @param  password the user's password
	 * @param  headerMap the header map from the authentication request
	 * @param  parameterMap the parameter map from the authentication request
	 * @param  resultsMap the map of authentication results (may be nil). After
	 *         a succesful authentication the user's primary key will be placed
	 *         under the key <code>userId</code>.
	 * @return the authentication status. This can be {@link
	 *         com.liferay.portal.security.auth.Authenticator#FAILURE}
	 *         indicating that the user's credentials are invalid, {@link
	 *         com.liferay.portal.security.auth.Authenticator#SUCCESS}
	 *         indicating a successful login, or {@link
	 *         com.liferay.portal.security.auth.Authenticator#DNE} indicating
	 *         that a user with that login does not exist.
	 * @throws PortalException if <code>emailAddress</code> or
	 *         <code>password</code> was <code>null</code>
	 * @throws SystemException if a system exception occurred
	 * @see    com.liferay.portal.security.auth.AuthPipeline
	 */
	public int authenticateByEmailAddress(
			long companyId, String emailAddress, String password,
			Map<String, String[]> headerMap, Map<String, String[]> parameterMap,
			Map<String, Object> resultsMap)
		throws PortalException, SystemException {

		return authenticate(
			companyId, emailAddress, password, CompanyConstants.AUTH_TYPE_EA,
			headerMap, parameterMap, resultsMap);
	}

	/**
	 * Attempts to authenticate the user by their screen name and password,
	 * while using the AuthPipeline.
	 *
	 * @param  companyId the primary key of the user's company
	 * @param  screenName the user's screen name
	 * @param  password the user's password
	 * @param  headerMap the header map from the authentication request
	 * @param  parameterMap the parameter map from the authentication request
	 * @param  resultsMap the map of authentication results (may be nil). After
	 *         a succesful authentication the user's primary key will be placed
	 *         under the key <code>userId</code>.
	 * @return the authentication status. This can be {@link
	 *         com.liferay.portal.security.auth.Authenticator#FAILURE}
	 *         indicating that the user's credentials are invalid, {@link
	 *         com.liferay.portal.security.auth.Authenticator#SUCCESS}
	 *         indicating a successful login, or {@link
	 *         com.liferay.portal.security.auth.Authenticator#DNE} indicating
	 *         that a user with that login does not exist.
	 * @throws PortalException if <code>screenName</code> or
	 *         <code>password</code> was <code>null</code>
	 * @throws SystemException if a system exception occurred
	 * @see    com.liferay.portal.security.auth.AuthPipeline
	 */
	public int authenticateByScreenName(
			long companyId, String screenName, String password,
			Map<String, String[]> headerMap, Map<String, String[]> parameterMap,
			Map<String, Object> resultsMap)
		throws PortalException, SystemException {

		return authenticate(
			companyId, screenName, password, CompanyConstants.AUTH_TYPE_SN,
			headerMap, parameterMap, resultsMap);
	}

	/**
	 * Attempts to authenticate the user by their primary key and password,
	 * while using the AuthPipeline.
	 *
	 * @param  companyId the primary key of the user's company
	 * @param  userId the user's primary key
	 * @param  password the user's password
	 * @param  headerMap the header map from the authentication request
	 * @param  parameterMap the parameter map from the authentication request
	 * @param  resultsMap the map of authentication results (may be nil). After
	 *         a succesful authentication the user's primary key will be placed
	 *         under the key <code>userId</code>.
	 * @return the authentication status. This can be {@link
	 *         com.liferay.portal.security.auth.Authenticator#FAILURE}
	 *         indicating that the user's credentials are invalid, {@link
	 *         com.liferay.portal.security.auth.Authenticator#SUCCESS}
	 *         indicating a successful login, or {@link
	 *         com.liferay.portal.security.auth.Authenticator#DNE} indicating
	 *         that a user with that login does not exist.
	 * @throws PortalException if <code>userId</code> or <code>password</code>
	 *         was <code>null</code>
	 * @throws SystemException if a system exception occurred
	 * @see    com.liferay.portal.security.auth.AuthPipeline
	 */
	public int authenticateByUserId(
			long companyId, long userId, String password,
			Map<String, String[]> headerMap, Map<String, String[]> parameterMap,
			Map<String, Object> resultsMap)
		throws PortalException, SystemException {

		return authenticate(
			companyId, String.valueOf(userId), password,
			CompanyConstants.AUTH_TYPE_ID, headerMap, parameterMap, resultsMap);
	}

	/**
	 * Attempts to authenticate the user using HTTP basic access authentication,
	 * without using the AuthPipeline. Primarily used for authenticating users
	 * of <code>tunnel-web</code>.
	 *
	 * <p>
	 * Authentication type specifies what <code>login</code> contains.The valid
	 * values are:
	 * </p>
	 *
	 * <ul>
	 * <li>
	 * <code>CompanyConstants.AUTH_TYPE_EA</code> - <code>login</code> is the
	 * user's email address
	 * </li>
	 * <li>
	 * <code>CompanyConstants.AUTH_TYPE_SN</code> - <code>login</code> is the
	 * user's screen name
	 * </li>
	 * <li>
	 * <code>CompanyConstants.AUTH_TYPE_ID</code> - <code>login</code> is the
	 * user's primary key
	 * </li>
	 * </ul>
	 *
	 * @param  companyId the primary key of the user's company
	 * @param  authType the type of authentication to perform
	 * @param  login either the user's email address, screen name, or primary
	 *         key depending on the value of <code>authType</code>
	 * @param  password the user's password
	 * @return the authentication status. This can be {@link
	 *         com.liferay.portal.security.auth.Authenticator#FAILURE}
	 *         indicating that the user's credentials are invalid, {@link
	 *         com.liferay.portal.security.auth.Authenticator#SUCCESS}
	 *         indicating a successful login, or {@link
	 *         com.liferay.portal.security.auth.Authenticator#DNE} indicating
	 *         that a user with that login does not exist.
	 * @throws PortalException if a portal exception occurred
	 * @throws SystemException if a system exception occurred
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public long authenticateForBasic(
			long companyId, String authType, String login, String password)
		throws PortalException, SystemException {

		if (PropsValues.AUTH_LOGIN_DISABLED) {
			return 0;
		}

		try {
			User user = null;

			if (authType.equals(CompanyConstants.AUTH_TYPE_EA)) {
				user = getUserByEmailAddress(companyId, login);
			}
			else if (authType.equals(CompanyConstants.AUTH_TYPE_SN)) {
				user = getUserByScreenName(companyId, login);
			}
			else if (authType.equals(CompanyConstants.AUTH_TYPE_ID)) {
				user = getUserById(companyId, GetterUtil.getLong(login));
			}

			if (user.isDefaultUser()) {
				if (_log.isInfoEnabled()) {
					_log.info(
						"Basic authentication is disabled for the default " +
							"user");
				}

				return 0;
			}
			else if (!user.isActive()) {
				if (_log.isInfoEnabled()) {
					_log.info(
						"Basic authentication is disabled for inactive user " +
							user.getUserId());
				}

				return 0;
			}

			if (!PropsValues.BASIC_AUTH_PASSWORD_REQUIRED) {
				return user.getUserId();
			}

			String userPassword = user.getPassword();

			if (!user.isPasswordEncrypted()) {
				userPassword = PwdEncryptor.encrypt(userPassword);
			}

			String encPassword = PwdEncryptor.encrypt(password);

			if (userPassword.equals(password) ||
				userPassword.equals(encPassword)) {

				return user.getUserId();
			}
		}
		catch (NoSuchUserException nsue) {
		}

		return 0;
	}

	/**
	 * Attempts to authenticate the user using HTTP digest access
	 * authentication, without using the AuthPipeline. Primarily used for
	 * authenticating users of <code>tunnel-web</code>.
	 *
	 * @param  companyId the primary key of the user's company
	 * @param  username either the user's email address, screen name, or primary
	 *         key
	 * @param  realm unused
	 * @param  nonce the number used once
	 * @param  method the request method
	 * @param  uri the request URI
	 * @param  response the authentication response hash
	 * @return the user's primary key if authentication is succesful;
	 *         <code>0</code> otherwise
	 * @throws PortalException if a portal exception occurred
	 * @throws SystemException if a system exception occurred
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public long authenticateForDigest(
			long companyId, String username, String realm, String nonce,
			String method, String uri, String response)
		throws PortalException, SystemException {

		if (PropsValues.AUTH_LOGIN_DISABLED) {
			return 0;
		}

		// Get User

		User user = null;

		try {
			user = getUserByEmailAddress(companyId, username);
		}
		catch (NoSuchUserException nsue) {
			try {
				user = getUserByScreenName(companyId, username);
			}
			catch (NoSuchUserException nsue2) {
				try {
					user = getUserById(GetterUtil.getLong(username));
				}
				catch (NoSuchUserException nsue3) {
					return 0;
				}
			}
		}

		if (user.isDefaultUser()) {
			if (_log.isInfoEnabled()) {
				_log.info(
					"Digest authentication is disabled for the default user");
			}

			return 0;
		}
		else if (!user.isActive()) {
			if (_log.isInfoEnabled()) {
				_log.info(
					"Digest authentication is disabled for inactive user " +
						user.getUserId());
			}

			return 0;
		}

		// Verify digest

		String digest = user.getDigest();

		if (Validator.isNull(digest)) {
			_log.error(
				"User must first login through the portal " + user.getUserId());

			return 0;
		}

		String[] digestArray = StringUtil.split(user.getDigest());

		for (String ha1 : digestArray) {
			String ha2 = DigesterUtil.digestHex(Digester.MD5, method, uri);

			String curResponse = DigesterUtil.digestHex(
				Digester.MD5, ha1, nonce, ha2);

			if (response.equals(curResponse)) {
				return user.getUserId();
			}
		}

		return 0;
	}

	/**
	 * Attempts to authenticate the user using JAAS credentials, without using
	 * the AuthPipeline.
	 *
	 * @param  userId the primary key of the user
	 * @param  encPassword the encrypted password
	 * @return <code>true</code> if authentication is successful;
	 *         <code>false</code> otherwise
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public boolean authenticateForJAAS(long userId, String encPassword) {
		if (PropsValues.AUTH_LOGIN_DISABLED) {
			return false;
		}

		try {
			User user = userPersistence.findByPrimaryKey(userId);

			if (user.isDefaultUser()) {
				if (_log.isInfoEnabled()) {
					_log.info(
						"JAAS authentication is disabled for the default user");
				}

				return false;
			}
			else if (!user.isActive()) {
				if (_log.isInfoEnabled()) {
					_log.info(
						"JAAS authentication is disabled for inactive user " +
							userId);
				}

				return false;
			}

			String password = user.getPassword();

			if (user.isPasswordEncrypted()) {
				if (password.equals(encPassword)) {
					return true;
				}

				if (!PropsValues.PORTAL_JAAS_STRICT_PASSWORD) {
					encPassword = PwdEncryptor.encrypt(encPassword, password);

					if (password.equals(encPassword)) {
						return true;
					}
				}
			}
			else {
				if (!PropsValues.PORTAL_JAAS_STRICT_PASSWORD) {
					if (password.equals(encPassword)) {
						return true;
					}
				}

				password = PwdEncryptor.encrypt(password);

				if (password.equals(encPassword)) {
					return true;
				}
			}
		}
		catch (Exception e) {
			_log.error(e);
		}

		return false;
	}

	/**
	 * Checks if the user is currently locked out based on the password policy,
	 * and performs maintenance on the user's lockout and failed login data.
	 *
	 * @param  user the user
	 * @throws PortalException if the user was determined to still be locked out
	 * @throws SystemException if a system exception occurred
	 */
	public void checkLockout(User user)
		throws PortalException, SystemException {

		if (LDAPSettingsUtil.isPasswordPolicyEnabled(user.getCompanyId())) {
			return;
		}

		PasswordPolicy passwordPolicy = user.getPasswordPolicy();

		if (passwordPolicy.isLockout()) {

			// Reset failure count

			Date now = new Date();
			int failedLoginAttempts = user.getFailedLoginAttempts();

			if (failedLoginAttempts > 0) {
				long failedLoginTime = user.getLastFailedLoginDate().getTime();
				long elapsedTime = now.getTime() - failedLoginTime;
				long requiredElapsedTime =
					passwordPolicy.getResetFailureCount() * 1000;

				if ((requiredElapsedTime != 0) &&
					(elapsedTime > requiredElapsedTime)) {

					user.setLastFailedLoginDate(null);
					user.setFailedLoginAttempts(0);

					userPersistence.update(user);
				}
			}

			// Reset lockout

			if (user.isLockout()) {
				long lockoutTime = user.getLockoutDate().getTime();
				long elapsedTime = now.getTime() - lockoutTime;
				long requiredElapsedTime =
					passwordPolicy.getLockoutDuration() * 1000;

				if ((requiredElapsedTime != 0) &&
					(elapsedTime > requiredElapsedTime)) {

					user.setLockout(false);
					user.setLockoutDate(null);

					userPersistence.update(user);
				}
			}

			if (user.isLockout()) {
				throw new UserLockoutException();
			}
		}
	}

	/**
	 * Adds a failed login attempt to the user and updates the user's last
	 * failed login date.
	 *
	 * @param  user the user
	 * @throws SystemException if a system exception occurred
	 */
	public void checkLoginFailure(User user) throws SystemException {
		Date now = new Date();

		int failedLoginAttempts = user.getFailedLoginAttempts();

		user.setLastFailedLoginDate(now);
		user.setFailedLoginAttempts(++failedLoginAttempts);

		userPersistence.update(user);
	}

	/**
	 * Adds a failed login attempt to the user with the email address and
	 * updates the user's last failed login date.
	 *
	 * @param  companyId the primary key of the user's company
	 * @param  emailAddress the user's email address
	 * @throws PortalException if a user with the email address could not be
	 *         found
	 * @throws SystemException if a system exception occurred
	 */
	public void checkLoginFailureByEmailAddress(
			long companyId, String emailAddress)
		throws PortalException, SystemException {

		User user = getUserByEmailAddress(companyId, emailAddress);

		checkLoginFailure(user);
	}

	/**
	 * Adds a failed login attempt to the user and updates the user's last
	 * failed login date.
	 *
	 * @param  userId the primary key of the user
	 * @throws PortalException if a user with the primary key could not be found
	 * @throws SystemException if a system exception occurred
	 */
	public void checkLoginFailureById(long userId)
		throws PortalException, SystemException {

		User user = userPersistence.findByPrimaryKey(userId);

		checkLoginFailure(user);
	}

	/**
	 * Adds a failed login attempt to the user with the screen name and updates
	 * the user's last failed login date.
	 *
	 * @param  companyId the primary key of the user's company
	 * @param  screenName the user's screen name
	 * @throws PortalException if a user with the screen name could not be found
	 * @throws SystemException if a system exception occurred
	 */
	public void checkLoginFailureByScreenName(long companyId, String screenName)
		throws PortalException, SystemException {

		User user = getUserByScreenName(companyId, screenName);

		checkLoginFailure(user);
	}

	/**
	 * Checks if the user's password is expired based on the password policy,
	 * and performs maintenance on the user's grace login and password reset
	 * data.
	 *
	 * @param  user the user
	 * @throws PortalException if the user's password has expired and the grace
	 *         login limit has been exceeded
	 * @throws SystemException if a system exception occurred
	 */
	public void checkPasswordExpired(User user)
		throws PortalException, SystemException {

		if (LDAPSettingsUtil.isPasswordPolicyEnabled(user.getCompanyId())) {
			return;
		}

		PasswordPolicy passwordPolicy = user.getPasswordPolicy();

		// Check if password has expired

		if (isPasswordExpired(user)) {
			int graceLoginCount = user.getGraceLoginCount();

			if (graceLoginCount < passwordPolicy.getGraceLimit()) {
				user.setGraceLoginCount(++graceLoginCount);

				userPersistence.update(user);
			}
			else {
				user.setDigest(StringPool.BLANK);

				userPersistence.update(user);

				throw new PasswordExpiredException();
			}
		}

		// Check if user should be forced to change password on first login

		if (passwordPolicy.isChangeable() &&
			passwordPolicy.isChangeRequired()) {

			if (user.getLastLoginDate() == null) {
				user.setPasswordReset(true);

				userPersistence.update(user);
			}
		}
	}

	/**
	 * Removes all the users from the organization.
	 *
	 * @param  organizationId the primary key of the organization
	 * @throws SystemException if a system exception occurred
	 */
	public void clearOrganizationUsers(long organizationId)
		throws SystemException {

		organizationPersistence.clearUsers(organizationId);

		PermissionCacheUtil.clearCache();
	}

	/**
	 * Removes all the users from the user group.
	 *
	 * @param  userGroupId the primary key of the user group
	 * @throws SystemException if a system exception occurred
	 */
	public void clearUserGroupUsers(long userGroupId) throws SystemException {
		userGroupPersistence.clearUsers(userGroupId);

		PermissionCacheUtil.clearCache();
	}

	/**
	 * Completes the user's registration by generating a password and sending
	 * the confirmation email.
	 *
	 * @param  user the user
	 * @param  serviceContext the user's service context. Can set whether a
	 *         password should be generated (with the <code>autoPassword</code>
	 *         attribute) and whether the confirmation email should be sent
	 *         (with the <code>sendEmail</code> attribute) for the user.
	 * @throws PortalException if a portal exception occurred
	 * @throws SystemException if a system exception occurred
	 */
	public void completeUserRegistration(
			User user, ServiceContext serviceContext)
		throws PortalException, SystemException {

		boolean autoPassword = ParamUtil.getBoolean(
			serviceContext, "autoPassword");

		String password = null;

		if (autoPassword) {
			if (LDAPSettingsUtil.isPasswordPolicyEnabled(user.getCompanyId())) {
				if (_log.isWarnEnabled()) {
					StringBundler sb = new StringBundler(4);

					sb.append("When LDAP password policy is enabled, it is ");
					sb.append("possible that portal generated passwords will ");
					sb.append("not match the LDAP policy. Using ");
					sb.append("RegExpToolkit to generate new password.");

					_log.warn(sb.toString());
				}

				RegExpToolkit regExpToolkit = new RegExpToolkit();

				password = regExpToolkit.generate(null);
			}
			else {
				PasswordPolicy passwordPolicy =
					passwordPolicyLocalService.getPasswordPolicy(
						user.getCompanyId(), user.getOrganizationIds());

				password = PwdToolkitUtil.generate(passwordPolicy);
			}

			user.setPassword(PwdEncryptor.encrypt(password));
			user.setPasswordEncrypted(true);
			user.setPasswordUnencrypted(password);

			userPersistence.update(user);
		}

		if (user.hasCompanyMx()) {
			String mailPassword = password;

			if (Validator.isNull(mailPassword)) {
				mailPassword = user.getPasswordUnencrypted();
			}

			mailService.addUser(
				user.getCompanyId(), user.getUserId(), mailPassword,
				user.getFirstName(), user.getMiddleName(), user.getLastName(),
				user.getEmailAddress());
		}

		boolean sendEmail = ParamUtil.getBoolean(serviceContext, "sendEmail");

		if (sendEmail) {
			sendEmail(user, password, serviceContext);
		}

		Company company = companyPersistence.findByPrimaryKey(
			user.getCompanyId());

		if (company.isStrangersVerify() && (serviceContext.getPlid() > 0)) {
			sendEmailAddressVerification(
				user, user.getEmailAddress(), serviceContext);
		}
	}

	/**
	 * Decrypts the user's primary key and password from their encrypted forms.
	 * Used for decrypting a user's credentials from the values stored in an
	 * automatic login cookie.
	 *
	 * @param  companyId the primary key of the user's company
	 * @param  name the encrypted primary key of the user
	 * @param  password the encrypted password of the user
	 * @return the user's primary key and password
	 * @throws PortalException if a user with the primary key could not be found
	 *         or if the user's password was incorrect
	 * @throws SystemException if a system exception occurred
	 */
	public KeyValuePair decryptUserId(
			long companyId, String name, String password)
		throws PortalException, SystemException {

		Company company = companyPersistence.findByPrimaryKey(companyId);

		try {
			name = Encryptor.decrypt(company.getKeyObj(), name);
		}
		catch (EncryptorException ee) {
			throw new SystemException(ee);
		}

		long userId = GetterUtil.getLong(name);

		User user = userPersistence.findByPrimaryKey(userId);

		try {
			password = Encryptor.decrypt(company.getKeyObj(), password);
		}
		catch (EncryptorException ee) {
			throw new SystemException(ee);
		}

		String encPassword = PwdEncryptor.encrypt(password);

		if (user.getPassword().equals(encPassword)) {
			if (isPasswordExpired(user)) {
				user.setPasswordReset(true);

				userPersistence.update(user);
			}

			return new KeyValuePair(name, password);
		}
		else {
			throw new PrincipalException();
		}
	}

	/**
	 * Deletes the user's portrait image.
	 *
	 * @param  userId the primary key of the user
	 * @throws PortalException if a user with the primary key could not be found
	 *         or if the user's portrait could not be found
	 * @throws SystemException if a system exception occurred
	 */
	public void deletePortrait(long userId)
		throws PortalException, SystemException {

		User user = userPersistence.findByPrimaryKey(userId);

		long portraitId = user.getPortraitId();

		if (portraitId > 0) {
			user.setPortraitId(0);

			userPersistence.update(user);

			imageLocalService.deleteImage(portraitId);
		}
	}

	/**
	 * Removes the user from the role.
	 *
	 * @param  roleId the primary key of the role
	 * @param  userId the primary key of the user
	 * @throws PortalException if a role or user with the primary key could not
	 *         be found
	 * @throws SystemException if a system exception occurred
	 */
	public void deleteRoleUser(long roleId, long userId)
		throws PortalException, SystemException {

		rolePersistence.removeUser(roleId, userId);

		Indexer indexer = IndexerRegistryUtil.nullSafeGetIndexer(User.class);

		indexer.reindex(userId);

		PermissionCacheUtil.clearCache();
	}

	/**
	 * Deletes the user.
	 *
	 * @param  userId the primary key of the user
	 * @return the deleted user
	 * @throws PortalException if a user with the primary key could not be found
	 * @throws SystemException if a system exception occurred
	 */
	@Override
	public User deleteUser(long userId)
		throws PortalException, SystemException {

		User user = userPersistence.findByPrimaryKey(userId);

		return deleteUser(user);
	}

	/**
	 * Deletes the user.
	 *
	 * @param  user the user
	 * @return the deleted user
	 * @throws PortalException if a portal exception occurred
	 * @throws SystemException if a system exception occurred
	 */
	@Override
	public User deleteUser(User user) throws PortalException, SystemException {
		if (!PropsValues.USERS_DELETE) {
			throw new RequiredUserException();
		}

		// Browser tracker

		browserTrackerLocalService.deleteUserBrowserTracker(user.getUserId());

		// Group

		Group group = user.getGroup();

		if (group != null) {
			groupLocalService.deleteGroup(group);
		}

		// Portrait

		imageLocalService.deleteImage(user.getPortraitId());

		// Password policy relation

		passwordPolicyRelLocalService.deletePasswordPolicyRel(
			User.class.getName(), user.getUserId());

		// Old passwords

		passwordTrackerLocalService.deletePasswordTrackers(user.getUserId());

		// Subscriptions

		subscriptionLocalService.deleteSubscriptions(user.getUserId());

		// External user ids

		userIdMapperLocalService.deleteUserIdMappers(user.getUserId());

		// Announcements

		announcementsDeliveryLocalService.deleteDeliveries(user.getUserId());

		// Asset

		assetEntryLocalService.deleteEntry(
			User.class.getName(), user.getUserId());

		// Blogs

		blogsStatsUserLocalService.deleteStatsUserByUserId(user.getUserId());

		// Document library

		dlFileRankLocalService.deleteFileRanksByUserId(user.getUserId());

		// Expando

		expandoValueLocalService.deleteValues(
			User.class.getName(), user.getUserId());

		// Message boards

		mbBanLocalService.deleteBansByBanUserId(user.getUserId());
		mbStatsUserLocalService.deleteStatsUsersByUserId(user.getUserId());
		mbThreadFlagLocalService.deleteThreadFlagsByUserId(user.getUserId());

		// Membership requests

		membershipRequestLocalService.deleteMembershipRequestsByUserId(
			user.getUserId());

		// Shopping cart

		shoppingCartLocalService.deleteUserCarts(user.getUserId());

		// Social

		socialActivityLocalService.deleteUserActivities(user.getUserId());
		socialRequestLocalService.deleteReceiverUserRequests(user.getUserId());
		socialRequestLocalService.deleteUserRequests(user.getUserId());

		// Mail

		mailService.deleteUser(user.getCompanyId(), user.getUserId());

		// Contact

		try {
			contactLocalService.deleteContact(user.getContactId());
		}
		catch (NoSuchContactException nsce) {
		}

		// Resources

		resourceLocalService.deleteResource(
			user.getCompanyId(), User.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL, user.getUserId());

		// Group roles

		userGroupRoleLocalService.deleteUserGroupRolesByUserId(
			user.getUserId());

		// User

		userPersistence.remove(user);

		// Permission cache

		PermissionCacheUtil.clearCache();

		// Workflow

		workflowInstanceLinkLocalService.deleteWorkflowInstanceLinks(
			user.getCompanyId(), 0, User.class.getName(), user.getUserId());

		return user;
	}

	/**
	 * Removes the user from the user group.
	 *
	 * @param  userGroupId the primary key of the user group
	 * @param  userId the primary key of the user
	 * @throws PortalException if a portal exception occurred
	 * @throws SystemException if a system exception occurred
	 */
	public void deleteUserGroupUser(long userGroupId, long userId)
		throws PortalException, SystemException {

		userGroupPersistence.removeUser(userGroupId, userId);

		Indexer indexer = IndexerRegistryUtil.nullSafeGetIndexer(User.class);

		indexer.reindex(userId);

		PermissionCacheUtil.clearCache();
	}

	/**
	 * Encrypts the primary key of the user. Used when encrypting the user's
	 * credentials for storage in an automatic login cookie.
	 *
	 * @param  name the primary key of the user
	 * @return the user's encrypted primary key
	 * @throws PortalException if a user with the primary key could not be found
	 * @throws SystemException if a system exception occurred
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public String encryptUserId(String name)
		throws PortalException, SystemException {

		long userId = GetterUtil.getLong(name);

		User user = userPersistence.findByPrimaryKey(userId);

		Company company = companyPersistence.findByPrimaryKey(
			user.getCompanyId());

		try {
			return Encryptor.encrypt(company.getKeyObj(), name);
		}
		catch (EncryptorException ee) {
			throw new SystemException(ee);
		}
	}

	/**
	 * Returns the user with the email address.
	 *
	 * @param  companyId the primary key of the user's company
	 * @param  emailAddress the user's email address
	 * @return the user with the email address, or <code>null</code> if a user
	 *         with the email address could not be found
	 * @throws SystemException if a system exception occurred
	 */
	public User fetchUserByEmailAddress(long companyId, String emailAddress)
		throws SystemException {

		return userPersistence.fetchByC_EA(companyId, emailAddress);
	}

	/**
	 * Returns the user with the primary key.
	 *
	 * @param  userId the primary key of the user
	 * @return the user with the primary key, or <code>null</code> if a user
	 *         with the primary key could not be found
	 * @throws SystemException if a system exception occurred
	 */
	public User fetchUserById(long userId) throws SystemException {
		return userPersistence.fetchByPrimaryKey(userId);
	}

	/**
	 * Returns the user with the screen name.
	 *
	 * @param  companyId the primary key of the user's company
	 * @param  screenName the user's screen name
	 * @return the user with the screen name, or <code>null</code> if a user
	 *         with the screen name could not be found
	 * @throws SystemException if a system exception occurred
	 */
	public User fetchUserByScreenName(long companyId, String screenName)
		throws SystemException {

		screenName = getScreenName(screenName);

		return userPersistence.fetchByC_SN(companyId, screenName);
	}

	/**
	 * Returns a range of all the users belonging to the company.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link
	 * com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  companyId the primary key of the company
	 * @param  start the lower bound of the range of users
	 * @param  end the upper bound of the range of users (not inclusive)
	 * @return the range of users belonging to the company
	 * @throws SystemException if a system exception occurred
	 */
	public List<User> getCompanyUsers(long companyId, int start, int end)
		throws SystemException {

		return userPersistence.findByCompanyId(companyId, start, end);
	}

	/**
	 * Returns the number of users belonging to the company.
	 *
	 * @param  companyId the primary key of the company
	 * @return the number of users belonging to the company
	 * @throws SystemException if a system exception occurred
	 */
	public int getCompanyUsersCount(long companyId) throws SystemException {
		return userPersistence.countByCompanyId(companyId);
	}

	/**
	 * Returns the default user for the company.
	 *
	 * @param  companyId the primary key of the company
	 * @return the default user for the company
	 * @throws PortalException if a default user for the company could not be
	 *         found
	 * @throws SystemException if a system exception occurred
	 */
	@Skip
	public User getDefaultUser(long companyId)
		throws PortalException, SystemException {

		User userModel = _defaultUsers.get(companyId);

		if (userModel == null) {
			userModel = userLocalService.loadGetDefaultUser(companyId);

			_defaultUsers.put(companyId, userModel);
		}

		return userModel;
	}

	/**
	 * Returns the primary key of the default user for the company.
	 *
	 * @param  companyId the primary key of the company
	 * @return the primary key of the default user for the company
	 * @throws PortalException if a default user for the company could not be
	 *         found
	 * @throws SystemException if a system exception occurred
	 */
	@Skip
	public long getDefaultUserId(long companyId)
		throws PortalException, SystemException {

		User user = getDefaultUser(companyId);

		return user.getUserId();
	}

	/**
	 * Returns the primary keys of all the users belonging to the group.
	 *
	 * @param  groupId the primary key of the group
	 * @return the primary keys of the users belonging to the group
	 * @throws SystemException if a system exception occurred
	 */
	public long[] getGroupUserIds(long groupId) throws SystemException {
		return getUserIds(getGroupUsers(groupId));
	}

	/**
	 * Returns all the users belonging to the group.
	 *
	 * @param  groupId the primary key of the group
	 * @return the users belonging to the group
	 * @throws SystemException if a system exception occurred
	 */
	public List<User> getGroupUsers(long groupId) throws SystemException {
		return groupPersistence.getUsers(groupId);
	}

	/**
	 * Returns the number of users belonging to the group.
	 *
	 * @param  groupId the primary key of the group
	 * @return the number of users belonging to the group
	 * @throws SystemException if a system exception occurred
	 */
	public int getGroupUsersCount(long groupId) throws SystemException {
		return groupPersistence.getUsersSize(groupId);
	}

	/**
	 * Returns the number of users with the status belonging to the group.
	 *
	 * @param  groupId the primary key of the group
	 * @param  status the workflow status
	 * @return the number of users with the status belonging to the group
	 * @throws PortalException if a group with the primary key could not be
	 *         found
	 * @throws SystemException if a system exce
