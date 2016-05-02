/*
 * AuthenticationService.java
 * 
 * Copyright (c) 2001-2002 Labfire, Inc. All rights reserved.
 * Visit http://labfire.com/ for more information. 
 * 
 * This software is the confidential and proprietary information of
 * Labfire, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Labfire.
 */


package com.labfire.fe.auth;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.labfire.fe.FireEngineComponent;
import com.labfire.fe.FireEngineConfig;
import com.labfire.fe.cache.CacheException;
import com.labfire.fe.cache.CacheService;
import com.labfire.fe.cache.CachedObject;
import com.labfire.fe.common.InitializeException;
import com.labfire.fe.common.TransientUser;
import com.labfire.fe.common.TransientUserFactory;
import com.labfire.fe.common.User;
import com.labfire.fe.db.ConnectionService;
import com.labfire.fe.log.LogService;
import com.labfire.fe.mail.MailException;
import com.labfire.fe.mail.MailService;
import com.labfire.fe.util.Connections;
import com.labfire.fe.util.Strings;

/**
 * AuthenticationService keeps track of userID/AuthToken pairs
 * for logged in users. More specifically, the AuthenticationService handles logging
 * in users, translating AuthTokens to userIDs, and selecting,
 * inserting, updating and deleting users from the system database.
 *
 * @author <a href="http://labfire.com">Labfire, Inc.</a>
 * @see com.labfire.fe.auth.AuthenticationServlet
 * @see com.labfire.fe.auth.AuthenticationEditServlet
 * @see com.labfire.fe.auth.AuthenticationDeleteServlet
 * @see com.labfire.fe.User
 */
public final class AuthenticationService extends FireEngineComponent {
	public static final int SYSTEM_ID = 0;

	private static AuthToken serviceAuthToken;
	private static User serviceUser;
	private static TransientUser serviceTransientUser;
	private static SecureRandom rand;
	private static int secondsToLive = 1800;
	private static Map tokens = new HashMap();
	private static Map ids = new HashMap();
	private static Map transientUsers = new HashMap();
	private static TransientUserFactory transientUserFactory =
		new TransientUserFactory();
	private static final String SESSION_ATTRIBUTE_NAME = "FireEngine.AuthToken";
	private static final String SALT_ORG = "CLFFEA_AuthServGRO";
	private static final String USER_FIELDS =
		"Users.UserID,Users.UserAccess,"
			+ "Users.UserName,Users.UserEmail,Users.UserFirstName,Users.UserLastName,"
			+ "Users.UserLoginAllowed,Users.UserDateCreated,Users.UserDateModified,Users.UserLastLoggedIn";
	private static ThreadLocal currentToken = new ThreadLocal();
		
	/**
	 * AuthenticationService
	 * 
	 * Initializes the AuthenticationService component. Called at FireEngine startup.
	 * Gets a SecureRandom instance and seeds it for later use. Creates the FireEngine user.
		
	 * @param inConfigFileName String filename of the AuthenticationService properties file.
	 * @throws InitializeException - if the constructor is unable to initialize SecureRandom.
	 */
 	public AuthenticationService(FireEngineConfig config) throws InitializeException {
  		super(config);
		secondsToLive = Integer.parseInt(super.getProperty("secondstolive"));
		try {
			rand = SecureRandom.getInstance("SHA1PRNG");
			rand.setSeed(new java.util.Date().getTime());
		
			// setup the FireEngineComponent user
			serviceAuthToken = config.getServiceAuthToken();
			Integer id = new Integer(SYSTEM_ID);
			tokens.put(id, serviceAuthToken);
			ids.put(serviceAuthToken, id);
			User u = new User();
			List orgAts = Collections.singletonList(OrgAuthenticationService.findToken(OrgAuthenticationService.SYSTEM_OID));
			u.setUserToken(serviceAuthToken);
			u.setUserOrgTokens(orgAts);
			u.setUserAccessToken(AccessAuthenticationService.findToken(AuthLevel.ACCESS_USER));
			u.setUserName("FireEngine");
			u.setUserEmail("FireEngine@labfire.com");
			u.setUserLastLoggedIn(new Timestamp((new java.util.Date()).getTime()));
			u.lock();
			serviceUser = u;
			CacheService.putCache(new CachedObject(u, serviceAuthToken, 0)); //live forever
			TransientUser tu = new TransientUser();
			tu.setUserToken(u.getUserToken());
			tu.setUserOrgTokens(u.getUserOrgTokens());
			tu.setUserAccessToken(u.getUserAccessToken());
			tu.setUserName(u.getUserName());
			tu.setUserEmail(u.getUserEmail());
			tu.setUserID(SYSTEM_ID);
			tu.setUserOIDs(Collections.singletonList(new Integer(OrgAuthenticationService.SYSTEM_OID)));
			tu.setUserAID(AuthLevel.ACCESS_USER);
			tu.lock();
			serviceTransientUser = tu;
			transientUsers.put(new Integer(SYSTEM_ID), tu);
			// loadSessions();
		} catch (NoSuchAlgorithmException nsae) {
			InitializeException ie = new InitializeException("Cannot get SecureRandom instance.", nsae);
			LogService.logError(ie.getMessage(), nsae);
			throw ie;
		} catch (AuthenticationException ae) {
			InitializeException ie = new InitializeException("Unable to create FireEngineComponent User.", ae);
			LogService.logError(ie.getMessage(), ae);
			throw ie;
		}
	}
	
	protected void unload() {
		tokens.clear();
		ids.clear();
		transientUsers.clear();
	}
	
	/**
	 * login
	 * Public function to translate a user/pass combination into a user in the system
	 * database. If the user/pass combination returns a row from the database, a new
	 * User object is created, a reference placed for it in the CacheService, and the
	 * User object is returned.
	 * 
	 * @param user String userName
	 * @param pass String userPassword
	 * @return A fully initialized User object
	 * @throws AuthenticationException - if user/pass are incorrect or other SQLExceptions are caught.
	*/
	public static synchronized User login(String user, String pass) throws AuthenticationException {
		int id = -1;
		User u = null;
		AuthToken at;
		
		try {
			String query = "SELECT * FROM Users NATURAL FULL OUTER JOIN UserOrgs WHERE UserName = '" + 
				Strings.escapeSQL(user) + "' AND UserPass = '" + Strings.escapeSQL(pass) + "' AND UserLoginAllowed=true";
			ResultSet rs = Connections.select(serviceAuthToken, query, false);
			if (rs != null) {
				if (rs.next()) {
					// we got a user/pass match from the db
					id = rs.getInt("UserID");
					at = addToken(id);
					try { // try the cache first
						u = (User)((CachedObject)CacheService.getCache(at)).object;
						u.setUserLastLoggedIn(new Timestamp((new java.util.Date()).getTime()));
						update(u.getUserToken(), u);
						return u;
					} catch (CacheException ce) {
						// oh well, can't find user in cache, keep going and create one.
					}
					u = makeUser(rs, at, true);
					CacheService.putCache(new CachedObject(u, u.getUserToken(), secondsToLive));
					update(u.getUserToken(), u);
				} else {
					Thread.sleep(750);
				}
				rs.close();
			}
		} catch (Exception e) {
			if (id != -1) {
				purgeUser(id);
			}
			AuthenticationException ae = new AuthenticationException("Failed login attempt for user " + user, e);
			LogService.logError("Unknown error while logging in user " + user, e);
			throw ae;
		}
		if (u == null) {
			AuthenticationException ae = new AuthenticationException("Failed login attempt for user " + user);
			throw ae;
		} else {
			return u;
		}
	}
	
	private static synchronized User login(int id, AuthToken at) throws AuthenticationException {
		User u = null;
		
		try {
			String query = "SELECT * FROM Users NATURAL FULL OUTER JOIN UserOrgs WHERE UserID=" + id;
			ResultSet rs = Connections.select(serviceAuthToken, query, false);
			if (rs != null) {
				if (rs.next()) {
					// we got a user/pass match from the db
					Integer i = new Integer(id);
					tokens.put(i, at);
					ids.put(at, i);
					try { // try the cache first
						return (User)((CachedObject)CacheService.getCache(at)).object;
					} catch (CacheException ce) {
						// oh well, can't find user in cache, keep going and create one.
					}
					u = makeUser(rs, at, false);
					CacheService.putCache(new CachedObject(u, u.getUserToken(), secondsToLive));
				} else {
					Thread.sleep(750);
				}
				rs.close();
			}
		} catch (Exception e) {
			if (id != -1) {
				purgeUser(id);
			}
			AuthenticationException ae = new AuthenticationException("Failed login attempt for " + u, e);
			LogService.logError("Unknown error while logging in " + u, e);
			throw ae;
		}
		if (u == null) {
			AuthenticationException ae = new AuthenticationException("Failed login attempt for " + u);
			throw ae;
		} else {
			return u;
		}
	}
	
	static void sendReminder(HttpServletRequest request) throws SQLException, AuthException, MailException {
		String email = request.getParameter("email");
		if (email != null && email.length() > 0) {
			String query = "SELECT users.* FROM users WHERE useremail='" + Strings.escapeSQL(email) + "'";
			ResultSet rs = Connections.select(serviceAuthToken, query, false);
			if (rs != null) {
				if (rs.next()) {
					String from = "FireEngine <noreply@labfire.com>";
					String to = rs.getString("UserEmail");
					String subject = "User account reminder";
					String body = "Hello,\nA login information reminder request was made from " + 
						request.getRemoteAddr() + ".\n\nYour account information is:\nUsername: " + rs.getString("UserName") +
						"\nPassword: " + rs.getString("UserPass") + "\n";
					MailService.sendMessage(serviceAuthToken, to, from, subject, body);
					LogService.logInfo("Emailed login reminder for " + email);
				}
				rs.close();
			}
		} else {
			throw new AuthException("No email address found in request");
		}
	}
	
	private static User makeUser(ResultSet rs, AuthToken at, boolean setTime) throws SQLException, AuthException {
		// populate user object with info from db
		User u = new User();
		u.setUserLoginAllowed(rs.getBoolean("UserLoginAllowed"));
		u.setUserToken(at);
		u.setUserAccessToken(AccessAuthenticationService.findToken(rs.getInt("UserAccess")));
		u.setUserName(rs.getString("UserName"));
		u.setUserPass(rs.getString("UserPass"));
		u.setUserEmail(rs.getString("UserEmail"));
		u.setUserFirstName(rs.getString("UserFirstName"));
		u.setUserLastName(rs.getString("UserLastName"));
		u.setUserDateCreated(rs.getTimestamp("UserDateCreated"));
		u.setUserDateModified(rs.getTimestamp("UserDateModified"));
		List orgAts = new LinkedList();
		do {
			if (rs.getString("OrgID") != null) {
				orgAts.add(OrgAuthenticationService.addToken(rs.getInt("OrgID")));
			}
		} while (rs.next());
		u.setUserOrgTokens(orgAts);
		if (setTime) {
			u.setUserLastLoggedIn(new Timestamp(System.currentTimeMillis()));
		} else {
			u.setUserLastLoggedIn(rs.getTimestamp("UserLastLoggedIn"));
		}
		u.lock();
		return u;
	}
	
	/**
	 * isValidToken
	 * Returns true if the AuthToken represents a user already logged in,
	 * false otherwise.
	 * 
	 * @param eid int representing the userEID to be validated.
	 * @return Whether or not the the eid belongs to a valid user.
	 */
	public static boolean isValidToken(AuthToken at) {
		try {
			findID(at);
			return true;
		} catch (AuthenticationException ae) {
			return false;
		}
	}
	
	/**
	 * getUser
	 * Returns a valid User object only if the User has already logged
	 * in and a reference to it resides in the CacheService. Otherwise,
	 * an AuthenticationException is thrown.
	 * 
	 * @param eid a userEID
	 * @returns A fully initialized User object
	 * @throws AuthenticationException - if User is not cached
	 */
	public static User getUser(AuthToken at) throws AuthenticationException {
		if (at == null) {
			throw new AuthenticationException("User not found");
		} else if (at.equals(serviceAuthToken)) {
			return serviceUser;
		} else {
			try {
				return (User)((CachedObject)CacheService.getCache(at)).object;
			} catch (CacheException ce) {
				throw new AuthenticationException("User not found in cache");
			}
		}
	}
	
	/**
	 * getTransientUser
	 * 
	 * @return TransientUser
	 */
	public static TransientUser getTransientUser(AuthToken at, int id) {
		if (id == SYSTEM_ID) {
			return serviceTransientUser;
		} else {
			Integer key = new Integer(id);
			TransientUser u = (TransientUser)transientUsers.get(key);			
			if (u == null) {
				try {
					u = transientUserFactory.getInstance(at, id);
					transientUsers.put(key, u);
				} catch (InitializeException ie) {
					LogService.logWarn("Unable to initialize TransientUser with ID " + id, ie);
				}
			}
			return u;
		}
	}
	
	public static TransientUser getTransientUser(int id) {
		return getTransientUser(serviceAuthToken, id);
	}
	
	/**
	 * findID
	 * 
	 * Translates a userEID into a userID. If the EID is not found in
	 * the CacheService, the EID/ID is assumed to have expired, the EID/ID
	 * is removed from the AuthenticationService's list of active users,
	 * and an AuthenticationException is thrown. 
	 * 
	 * @param eid a userEID
	 * @return The user's userID
	 * @throws AuthenticationException - if User is not cached
	 */
	public static synchronized int findID(AuthToken at) throws AuthenticationException {
		Integer id = (Integer)ids.get(at);
		CachedObject co = null;
		
		if (id == null) { 
			throw new AuthenticationException("No corresponding ID");
		}
		try {
			if (id.intValue() != SYSTEM_ID) {
				co = ((CachedObject)CacheService.getCache(at));
				co.setExpiration(secondsToLive);
			}
		} catch (CacheException ce) {
			// if user is not in cache, then it has expired, and we should
			// remove it from our list of active users.
			ids.remove(at);
			tokens.remove(id);
			// removeSession(id.intValue(), at);
			throw new AuthenticationException("User has expired from cache");
		}
		if ((co != null) && (co.object != null)) {
			try {
				// update the user's organizations' expiration date as well.
				Object orgAt;
				Iterator i = ((User)co.object).getUserOrgTokens().iterator();
				while (i.hasNext()) {
					orgAt = i.next();
					if (orgAt instanceof AuthToken) {
						OrgAuthenticationService.findOID((AuthToken)orgAt);
					}
				}
			} catch (AuthenticationException ae) {
				purgeUser(id.intValue());
				throw ae;
			}
		}
		return id.intValue();
	}
	
	/**
	 * findToken
	 * 
	 * @return AuthToken
	 */
	private static synchronized AuthToken findToken(int id) throws AuthenticationException {
		AuthToken at = (AuthToken)tokens.get(new Integer(id));
		CachedObject co = null;
		
		if (at == null) {
			throw new AuthenticationException("No corresponding AuthToken");
		} else if (at.equals(serviceAuthToken)) {
			return serviceAuthToken;
		} else {
			try {
				co = ((CachedObject)CacheService.getCache(at));
				if (id != SYSTEM_ID) {
					co.setExpiration(secondsToLive);
				}
			} catch (CacheException ce) {
				// if user is not in cache, then it has expired, and we should
				// remove it from our list of active users.
				ids.remove(at);
				tokens.remove(new Integer(id));
				// removeSession(id, at);
				throw new AuthenticationException("User has expired from cache");
			}
			if ((co != null) && (co.object != null)) {
				try {
					// update the user's organization's expiration dates as well.
					Object orgAt;
					Iterator i = ((User)co.object).getUserOrgTokens().iterator();
					while (i.hasNext()) {
						orgAt = i.next();
						if (orgAt instanceof AuthToken) {
							OrgAuthenticationService.findOID((AuthToken)orgAt);
						}
					}
				} catch (AuthenticationException ae) {
					purgeUser(id);
					throw ae;
				}
			}
			return at;
		}
	}
	
	/**
	 * addToken
	 * 
	 * @return AuthToken
	 */
	private static synchronized AuthToken addToken(int id) throws AuthenticationException {
		AuthToken at;
		Integer i = new Integer(id);
		
		if (id < 0) {
			throw new AuthenticationException("Attempt to add invalid ID");
		}
		
		try {
			// return the eid already in the list
			at = findToken(id);
		} catch (AuthenticationException ae) {
			// eid isn't in the list, add a new one and return it
			at = new LongAuthToken(rand.nextLong());
			tokens.put(i, at);
			ids.put(at, i);
			// if (id != SYSTEM_ID) {
				// saveSession(id, at);
			// }
			// LogService.logDebug("Added AuthToken " + at);
		}
		return at;
	}
	
	/**
	 * getSessionAttributeName
	 *
	 * @return String
	 */
	public static String getSessionAttributeName() {
		return SESSION_ATTRIBUTE_NAME;
	}
	
	/**
	 * getUserPass
	 * 
	 * @return String
	 */
	public static String getUserPass(AuthToken at, int key) throws SQLException,AuthException {
		String query = null;
		String pass = null;
		User u = null;
		
		if (key == SYSTEM_ID) {
			return null;
		}
		
		try {
			u = (User)((CachedObject)CacheService.getCache(at)).object;
		} catch (CacheException ce) {
			AuthenticationException ae = new AuthenticationException("A CacheException has occurred.", ce);
			LogService.logError(ae.getMessage(), ce);
			throw ae;
		}
		
		int accessLevel = AccessAuthenticationService.findAID(u.getUserAccessToken());
		if (accessLevel == AuthLevel.ACCESS_ADMIN) {
			query = "SELECT UserPass FROM Users WHERE UserID=" + key;
		} else {
			query = "SELECT UserPass FROM Users WHERE UserID=" + key + " AND (UserID=" + findID(u.getUserToken()) + 
				" OR UserID IN (SELECT UserOrgs.UserID FROM Users,UserOrgs WHERE UserOrgs.UserID = Users.UserID " +
				"AND UserOrgs.OrgID IN (SELECT OrgID FROM UserOrgs WHERE UserID=" + findID(u.getUserToken()) + 
				") AND UserAccess < " + accessLevel + "))";
		}
		ResultSet rs = Connections.select(serviceAuthToken, query, true);
		if (rs != null && rs.next()) {
			pass = rs.getString("UserPass");
		}
		return pass;
	}
	
	/**
	 * select
	 * Select all rows from the User table, such that this user is allowed to view them.
	 * The userEID supplied is used to check the user's access level and organization,
	 * and only rows that the user is allowed to view are returned.<br />
	 * Admin: all users<br />
	 * User: users u, such that u == your userID || u.userAccess < your userAccess || u.userOrg.orgDefaultAID < your userAccess<br />
	 * 
	 * @param eid a userEID
	 * @return Rows selected from the user table. If no rows were selected, the ResultSet may be null.
	 * @throws AuthenticationException - if unable to determine access level for the given userEID
	 */
	public static ResultSet select(AuthToken at) throws SQLException,AuthException {
		String query = null;
		User u = null;
		
		try {
			u = (User)((CachedObject)CacheService.getCache(at)).object;
		} catch (CacheException ce) {
			AuthenticationException ae = new AuthenticationException("A CacheException has occurred.", ce);
			LogService.logError(ae.getMessage(), ce);
			throw ae;
		}
		
		int accessLevel = AccessAuthenticationService.findAID(u.getUserAccessToken());
		if (accessLevel == AuthLevel.ACCESS_ADMIN) {
			// admins can view all user accounts
			query = "SELECT * FROM Users ORDER BY UserLastName,UserFirstName";
		} else {
			// users can view all accounts
			query = "SELECT " + USER_FIELDS + " FROM Users ORDER BY UserLastName,UserFirstName";
		}
		return Connections.select(serviceAuthToken, query, true);
	}
	
	/**
	 * selectRecent
	 */
	public static ResultSet selectRecent(AuthToken at, int limit) throws SQLException,AuthException {
		String query = null;
		User u = null;
		
		try {
			u = (User)((CachedObject)CacheService.getCache(at)).object;
		} catch (CacheException ce) {
			AuthenticationException ae = new AuthenticationException("A CacheException has occurred.", ce);
			LogService.logError(ae.getMessage(), ce);
			throw ae;
		}
		
		int accessLevel = AccessAuthenticationService.findAID(u.getUserAccessToken());
		if (accessLevel == AuthLevel.ACCESS_ADMIN) {
			// admins can view all user accounts
			query = "SELECT * FROM Users ORDER BY UserDateModified DESC LIMIT " + limit;
		} else {
			// users can view all accounts
			query = "SELECT " + USER_FIELDS + " FROM Users ORDER BY UserDateModified DESC LIMIT " + limit;
		}
		return Connections.select(serviceAuthToken, query, true);
	}
	
	/**
	 * select
	 * Select a row from the User table with a specific primary key, such that this user is allowed to view it.
	 * The userEID supplied is used to check the user's access level and organization,
	 * and only rows that the user is allowed to view are returned. For the user table,
	 * All joins are performed in advance for all foreign keys in the table.
	 * Admin: any user<br />
	 * User: a user u, such that u == your userID || u.userAccess < your userAccess || u.userOrg.orgDefaultAID < your userAccess<br />
	 * 
	 * @param eid a userEID
	 * @return Rows selected from the user table. If no rows were selected,	the ResultSet may be null.
	 * @throws AuthenticationException - if unable to determine access level for the given userEID
	 */	
	public static ResultSet select(AuthToken at, int key) throws SQLException,AuthException {
		String query = null;
		User u = null;
		
		try {
			u = (User)((CachedObject)CacheService.getCache(at)).object;
		} catch (CacheException ce) {
			AuthenticationException ae = new AuthenticationException("A CacheException has occurred.", ce);
			LogService.logError(ae.getMessage(), ce);
			throw ae;
		}
		
		int accessLevel = AccessAuthenticationService.findAID(u.getUserAccessToken());
		if (accessLevel == AuthLevel.ACCESS_ADMIN) {
			// admins can view all user accounts
			query = "SELECT * FROM Users WHERE UserID=" + key + " ORDER BY UserID";
		} else {
			// users can view all accounts
			query = "SELECT " + USER_FIELDS + " FROM Users WHERE UserID=" + key + " ORDER BY UserID";
		}
		return Connections.select(serviceAuthToken, query, true);
	}
	
	/**
	 * select
	 * Select a row from the User table with a specific primary key, such that this user is allowed to view it.
	 * The userEID supplied is used to check the user's access level and organization,
	 * and only rows that the user is allowed to view are returned. For the user table,
	 * All joins are performed in advance for all foreign keys in the table.
	 * Admin: any user<br />
	 * User: a user u, such that u == your userID || u.userAccess < your userAccess || u.userOrg.orgDefaultAID < your userAccess<br />
	 * 
	 * @param at AuthToken for the caller
	 * @param user String userName to be selected
	 * @return Rows selected from the user table. If no rows were selected,	the ResultSet may be null.
	 * @throws AuthenticationException - if unable to determine access level for the given userEID
	 */	
	public static ResultSet select(AuthToken at, String user) throws SQLException,AuthException {
		String query = null;
		User u = null;
		
		try {
			u = (User)((CachedObject)CacheService.getCache(at)).object;
		} catch (CacheException ce) {
			AuthenticationException ae = new AuthenticationException("A CacheException has occurred.", ce);
			LogService.logError(ae.getMessage(), ce);
			throw ae;
		}
		
		int accessLevel = AccessAuthenticationService.findAID(u.getUserAccessToken());
		if (accessLevel == AuthLevel.ACCESS_ADMIN) {
			// admins can view all user accounts
			query = "SELECT * FROM Users WHERE UserName='" + Strings.escapeSQL(user) + "'";
		} else {
			// users can view all accounts
			query = "SELECT " + USER_FIELDS + " FROM Users WHERE UserName='" + Strings.escapeSQL(user) + "'";
		}
		return Connections.select(serviceAuthToken, query, true);
	}
	
	/**
	 * selectByOrganization
	 * Select a row from the User table with a specific primary key, such that this user is allowed to view it.
	 * The userEID supplied is used to check the user's access level and organization,
	 * and only rows that the user is allowed to view are returned. For the user table,
	 * All joins are performed in advance for all foreign keys in the table.
	 * Admin: any user<br />
	 * User: a user u, such that u == your userID || u.userAccess < your userAccess || u.userOrg.orgDefaultAID < your userAccess<br />
	 * 
	 * @param eid a userEID
	 * @return Rows selected from the user table. If no rows were selected,	the ResultSet may be null.
	 * @throws AuthenticationException - if unable to determine access level for the given userEID
	 */	
	public static ResultSet selectByOrganization(AuthToken at, int orgID) throws SQLException,AuthException {
		String query = null;
		User u = null;
		
		try {
			u = (User)((CachedObject)CacheService.getCache(at)).object;
		} catch (CacheException ce) {
			AuthenticationException ae = new AuthenticationException("A CacheException has occurred.", ce);
			LogService.logError(ae.getMessage(), ce);
			throw ae;
		}
		
		int accessLevel = AccessAuthenticationService.findAID(u.getUserAccessToken());
		if (accessLevel == AuthLevel.ACCESS_ADMIN) {
			// admins can view all user accounts
			query = "SELECT Users.* FROM Users,UserOrgs WHERE Users.UserID=UserOrgs.UserID AND UserOrgs.OrgID="
				+ orgID + " ORDER BY UserLastName,UserFirstName";
		} else {
			// users can view all accounts
			query = "SELECT " + USER_FIELDS + " FROM Users,UserOrgs WHERE Users.UserID=UserOrgs.UserID AND UserOrgs.OrgID=" 
				+ orgID + " ORDER BY UserLastName,UserFirstName";
		}
		return Connections.select(serviceAuthToken, query, true);
	}

	/**
	 * update
	 * Update a row from the User table with a specific primary key, such that this user is allowed to update it.
	 * The userEID supplied is used to check the user's access level and organization,
	 * and only rows that the user is allowed to update are modified. For the user table,
	 * All joins are performed in advance for all foreign keys in the table.
	 * Admin: any user<br />
	 * User: a user u, such that u == your userID || u.userOrg.orgDefaultAID < your userAccess<br />
	 * 
	 * @param eid a userEID
	 * @return Number of rows updated. Returns 1 if successful, less than 1 otherwise.
	 * @throws AuthenticationException - if unable to determine access level for the given userEID
	 */		
	public static int update(AuthToken at, User nu) throws SQLException,AuthException {
		boolean setModTime = true;
		int userID;
		int userAID;
		String update = null;
		Timestamp now = new Timestamp((new java.util.Date()).getTime());
		User u = null;
		
		try {
			u = (User)((CachedObject)CacheService.getCache(at)).object;
		} catch (CacheException ce) {
			AuthenticationException ae = new AuthenticationException("A CacheException has occurred.", ce);
			LogService.logError(ae.getMessage(), ce);
			throw ae;
		}
		
		if (nu instanceof TransientUser) {
			userID = ((TransientUser)nu).getUserID();
			userAID = ((TransientUser)nu).getUserAID();
		} else {
			userID = findID(nu.getUserToken());
			userAID = AccessAuthenticationService.findAID(nu.getUserAccessToken());
			setModTime = false;
		}
			
		int accessLevel = AccessAuthenticationService.findAID(u.getUserAccessToken());
		if (accessLevel == AuthLevel.ACCESS_ADMIN) {
			// admins can update any account
			update = "UPDATE Users SET UserAccess=" + userAID + ",UserName='" + nu.getUserName() + 
				"',UserPass='" + nu.getUserPass() + "',UserEmail='" + nu.getUserEmail() + "',UserFirstName='" + 
				Strings.escapeSQL(nu.getUserFirstName()) + "',UserLastName='" + Strings.escapeSQL(nu.getUserLastName()) + "',UserLoginAllowed=" + 
				nu.isUserLoginAllowed();
			if (setModTime) {
				update += ",UserDateModified='" + now + "'";
			}
			update += ",UserLastLoggedIn='" + nu.getUserLastLoggedIn() + "' WHERE UserID=" + userID;
		} else {
			// users are allowed to update their own account, and
			// those of users who belong to groups with defaultAID less than their own
			update = "UPDATE Users SET UserName='" + nu.getUserName() + "',UserPass='" + nu.getUserPass() + 
				"',UserEmail='" + nu.getUserEmail() + "',UserFirstName='" + nu.getUserFirstName() + "',UserLastName='" + 
				nu.getUserLastName() + "',UserLoginAllowed=" + nu.isUserLoginAllowed();
			if (setModTime) {
				update += ",UserDateModified='" + now + "'";
			}
			update += ",UserLastLoggedIn='" + nu.getUserLastLoggedIn() + 
				"' WHERE UserID=" + userID + " AND (UserID=" + findID(u.getUserToken()) + 
				" OR UserID IN (SELECT UserOrgs.UserID FROM Users,UserOrgs WHERE UserOrgs.UserID = Users.UserID " +
				"AND UserOrgs.OrgID IN (SELECT OrgID FROM UserOrgs WHERE UserID=" + findID(u.getUserToken()) + 
				") AND UserAccess < " + accessLevel + "))";
		}
		
		ResultSet rs = Connections.update(serviceAuthToken, update);
		if (rs != null) {
			if (nu instanceof TransientUser) {
				updateUserOrgs(u, (TransientUser)nu);
				// remove references to this user in the AuthenticationService
				purgeUser(userID);
			}
			// and transient users
			purgeTransientUser(userID);
			rs.close();
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * insert
	 * Insert a row from the User table with a specific primary key, such that this user is allowed to insert it.
	 * The userEID supplied is used to check the user's access level and organization. For the user table,
	 * All joins are performed in advance for all foreign keys in the table.
	 * Admin: any user<br />
	 * User: a user u, such that u.userAccess < your userAccess <br />
	 * 
	 * @param eid a userEID
	 * @return Number of rows inserted. Returns 1 if successful, less than 1 otherwise.
	 * @throws AuthenticationException - if unable to determine access level for the given userEID
	 */	
	public static int insert(AuthToken at, User nu) throws SQLException,AuthException {
		int userAID;
		String update = null;
		Timestamp now = new Timestamp((new java.util.Date()).getTime());
		User u = null;
		
		try {
			u = (User)((CachedObject)CacheService.getCache(at)).object;
		} catch (CacheException ce) {
			AuthenticationException ae = new AuthenticationException("A CacheException has occurred.", ce);
			LogService.logError(ae.getMessage(), ce);
			throw ae;
		}

		if (nu instanceof TransientUser) {
			userAID = ((TransientUser)nu).getUserAID();
		} else {
			userAID = AccessAuthenticationService.findAID(nu.getUserAccessToken());
		}

		int accessLevel = AccessAuthenticationService.findAID(u.getUserAccessToken());
		if (accessLevel == AuthLevel.ACCESS_ADMIN) {
			// admins are allowed to insert any users
			update = "INSERT INTO Users (UserAccess,UserName,UserPass,UserEmail," +
				"UserFirstName,UserLastName,UserLoginAllowed,UserDateCreated,UserDateModified,UserLastLoggedIn) " +
				"VALUES (" + userAID + ",'" + nu.getUserName() + "','" + nu.getUserPass() + "','" + 
				nu.getUserEmail() + "','" + Strings.escapeSQL(nu.getUserFirstName()) + "','" + Strings.escapeSQL(nu.getUserLastName()) + 
				"'," + nu.isUserLoginAllowed() + ",'" + now + "','" + now + "','" + now + "')";
		} else {
			// users cannot insert users
			return 0;
		}
		
		ResultSet rs = Connections.update(serviceAuthToken, update);
		if (rs != null) {
			if (rs.next()) {
				if (nu instanceof TransientUser) {
					((TransientUser)nu).setUserID(rs.getInt("UserID"));
					updateUserOrgs(u, (TransientUser)nu);
				}
			}
			rs.close();
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * delete
	 * Delete a row from the User table with a specific primary key, such that this user is allowed to delete it.
	 * The userEID supplied is used to check the user's access level and organization,
	 * and only rows that the user is allowed to delete are modified. For the user table,
	 * Admin: any user<br />
	 * User: a user u, such that u.userAccess < your userAccess && u.userOrg.orgDefaultAID < your userAccess<br />
	 * 
	 * @param eid a userEID
	 * @return Number of rows deleted. Returns 1 if successful, less than 1 otherwise.
	 * @throws AuthenticationException - if unable to determine access level for the given userEID
	 */	
	public static int delete(AuthToken at, int key) throws SQLException,AuthException {
		int records = -1;
		String update = null;
		User u = null;
		
		try {
			u = (User)((CachedObject)CacheService.getCache(at)).object;
		} catch (CacheException ce) {
			AuthenticationException ae = new AuthenticationException("A CacheException has occurred.", ce);
			LogService.logError(ae.getMessage(), ce);
			throw ae;
		}

		int accessLevel = AccessAuthenticationService.findAID(u.getUserAccessToken());
		if (accessLevel == AuthLevel.ACCESS_ADMIN) {
			// admins can delete any user
			Connection cn = ConnectionService.getConnection(serviceAuthToken);
			cn.setAutoCommit(false);
			try {
				// first clear userorgs
				update = "DELETE FROM UserOrgs WHERE UserID=" + key;
				ResultSet rs = Connections.update(update, cn);
				if (rs != null) {
					rs.close();
				}
				// then clear user
				update = "DELETE FROM Users WHERE UserID=" + key;
				rs = Connections.update(update, cn);
				if (rs != null) {
					rs.close();
					cn.commit();
					// remove references to this user in the AuthenticationService
					purgeUser(key);
					// and transient users
					purgeTransientUser(key);
					records = 1;
				} else {
					cn.rollback();
					records = 0;
				}
			} catch (SQLException sqle) {
				LogService.logWarn("Caught SQLException, rolling back transaction.", sqle);
				cn.rollback();
				throw sqle;
			}
			ConnectionService.returnConnection(cn);
		} else {
			// users cannot delete users
			return 0;
		}
		return records;
	}
	
	/** 
	 * updateUserOrgs
	 * 
	 * @return int
	 */
	private static int updateUserOrgs(User u, TransientUser nu) throws SQLException,AuthException {
		int records = 0;
		int accessLevel = AccessAuthenticationService.findAID(u.getUserAccessToken());
		String update = null;
		Iterator i = nu.getUserOIDs().iterator();
		Object oid;
		
		if (accessLevel == AuthLevel.ACCESS_ADMIN) {
			Connection cn = ConnectionService.getConnection(serviceAuthToken);
			cn.setAutoCommit(false);
			try {
				// first remove the existing organization list
				update = "DELETE FROM UserOrgs WHERE UserID=" + nu.getUserID();
				ResultSet rs = Connections.update(update, cn);
				if (rs != null) {
					rs.close();
				}
				// then insert the new one
				while (i.hasNext()) {
					oid = i.next();
					if (oid instanceof Integer) {
						update = "INSERT INTO UserOrgs (UserID,OrgID) VALUES (" + 
							nu.getUserID() + "," + oid + ")";
						try {
							rs = Connections.update(update, cn);
							if (rs != null) {
								rs.close();
								records++;
							}
						} catch (SQLException sqle) {
							// keep inserting even if one fails for some reason.
							LogService.logWarn("Caught SQLException updating UserOrgs table.", sqle);
						}
					}
				}
			} catch (SQLException sqle) {
				cn.rollback();
				ConnectionService.returnConnection(cn);
				LogService.logWarn("Caught SQLException, rolling back transaction.", sqle);
				throw sqle;
			}
			cn.commit();
			ConnectionService.returnConnection(cn);
		}
		return records;
	}
	
	/**
	 * purgeUser
	 */
	static synchronized void purgeUser(int id) {
		Integer i = new Integer(id);
		AuthToken at = (AuthToken)tokens.get(i);
		CachedObject co = null;
		
		try {
			if (at != null && id != SYSTEM_ID) {
				co = ((CachedObject)CacheService.getCache(at));
				co.setExpiration(-1);
			}
		} catch (CacheException ce) {
			// if user is not in cache, we don't care.
		} finally {
			ids.remove(at);
			tokens.remove(i);
			// removeSession(id, at);
		}
	}
	
	/**
	 * purgeTransientUser
	 * if a user has been modified or delted in the authentication service,
	 * see if there is a transient user being used by the authorization service,
	 * and if so, remove it (it will be recreated when needed).
	 */
	private static void purgeTransientUser(int id) {
		if (id != SYSTEM_ID) {
			transientUsers.remove(new Integer(id));
		}
	}
	
	static void setCurrentToken(AuthToken at) {
		currentToken.set(at);
	}
	
	static void unsetCurrentToken() {
		currentToken.set(null);
	}
	
	public static AuthToken getCurrentToken() {
		return (AuthToken) currentToken.get();
	}
	
	private static void saveSession(int id, AuthToken at) {
		try {
			String update = "INSERT INTO Sessions (SessionUser,SessionToken) VALUES (" + id + ",'" + at + "')";
			ResultSet rs = Connections.update(serviceAuthToken, update);
			if (rs != null) {
				rs.close();
			}
		} catch (Exception e) {
			LogService.logError("Unable to save session", e);
		}
	}
	
	private static void removeSession(int id, AuthToken at) {
		try {
			String update = "DELETE FROM Sessions WHERE SessionUser=" + id + " AND SessionToken='" + at + "'";
			ResultSet rs = Connections.update(serviceAuthToken, update);
			if (rs != null) {
				rs.close();
			}
		} catch (Exception e) {
			LogService.logError("Unable to remove session", e);
		}
	}
	
	private static void loadSessions() {
		try {
			int id;
			AuthToken at;
			User u;
			ResultSet rs = selectSessions();
			if (rs != null) {
				while (rs.next()) {
					try {
						id = rs.getInt("SessionUser");
						at = new LongAuthToken(rs.getString("SessionToken"));
						u = login(id, at);
						LogService.logDebug("Loaded " + u + " from previous session");
					} catch (NumberFormatException nfe) {
						LogService.logError("Unable to load user from previous session", nfe);
					}
				}
				rs.close();
			}
		} catch (Exception e) {
			LogService.logError("Unable to load users from previous session", e);
		}
	}
	
	private static ResultSet selectSessions() throws SQLException,AuthException {
		String query = "SELECT * FROM Sessions";
		return Connections.select(serviceAuthToken, query, false);
	}
	
	/**
	 * getStatus
	 * Returns a String describing the current state of the AuthenticationService.
	 * 
	 * @return String
	 */
	public String getStatus() {
		return "AuthenticationService: tokens=" + tokens.size();
	}
}

