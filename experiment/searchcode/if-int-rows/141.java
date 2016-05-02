/*
 * OrgAuthenticationService.java
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.labfire.fe.FireEngineComponent;
import com.labfire.fe.FireEngineConfig;
import com.labfire.fe.cache.CacheException;
import com.labfire.fe.cache.CacheService;
import com.labfire.fe.cache.CachedObject;
import com.labfire.fe.common.InitializeException;
import com.labfire.fe.common.Organization;
import com.labfire.fe.common.OrganizationFactory;
import com.labfire.fe.common.User;
import com.labfire.fe.log.LogService;
import com.labfire.fe.util.Connections;
import com.labfire.fe.util.Strings;

/**
 * OrgAuthenticationService
 * OrgAuthenticationService keeps track of OID/EOID pairs
 * for organizations. More specifically, the OrgAuthenticationService handles
 * on-demand creation of organizations, translating EOIDS to OIDS, and selecting,
 * inserting, updating and deleting organizations from the system database.
 * 
 * @author <a href="http://labfire.com/">Labfire, Inc.</a>
 * @see com.labfire.fe.Organization
 */
public final class OrgAuthenticationService extends FireEngineComponent {
	public static final int SYSTEM_OID = 0;
	
	private static Map tokens = new HashMap();
	private static Map oids = new HashMap();
	private static Map organizations = new HashMap();
	private static SecureRandom rand;
	private static AuthToken serviceAuthToken;
	private static Organization serviceOrganization;
	private static OrganizationFactory orgFactory = new OrganizationFactory();
	
	/**
	 * OrgAuthenticationService
	 * Initializes the OrgAuthenticationService component. Called at FireEngine startup.
	 * Gets a SecureRandom instance and seeds it for later use.
	 * 
	 * @param inConfigFileName String filename of the OrgAuthenticationService properties file.
	 * @throws InitializeException - if the constructor is unable to initialize SecureRandom.
	 */
 	public OrgAuthenticationService(FireEngineConfig config) throws InitializeException {
  		super(config);
		serviceAuthToken = config.getServiceAuthToken();
		try {
			rand = SecureRandom.getInstance("SHA1PRNG");
			rand.setSeed(new java.util.Date().getTime());
		} catch (NoSuchAlgorithmException nsae) {
			InitializeException ie = new InitializeException("Cannot get SecureRandom instance.", nsae);
			LogService.logError(ie.getMessage(), nsae);
			throw ie;
		}
		AuthToken orgAt = new LongAuthToken(rand.nextLong());
		Integer oid = new Integer(SYSTEM_OID);
		Organization o = new Organization();
		o.setOrgID(SYSTEM_OID);
		o.setOrgName("System");
		o.setOrgShortName("System");
		o.setOrgDefaultAID(AuthLevel.ACCESS_USER);
		o.lock();
		serviceOrganization = o;
		tokens.put(oid, orgAt);
		oids.put(orgAt, oid);
		organizations.put(oid, o);
	}

	protected void unload() {
		tokens.clear();
		oids.clear();
		organizations.clear();
	}
	
	/**
	 * getOrganization
	 * 
	 * @return Organization
	 */
	public static synchronized Organization getOrganization(int oid) throws AuthException {
		if (oid == SYSTEM_OID) {
			return serviceOrganization;
		} else {
			Integer key = new Integer(oid);
			AuthToken at = addToken(oid);
			Organization o = (Organization)organizations.get(key);
			if (o == null) {
				o = createOrganization(at, oid); 
			}
			return o;
		}
	}
	
	/**
	 * getOrganization
	 * 
	 * @return Organization
	 */
	public static synchronized Organization getOrganization(String shortName) throws AuthException {
		if (shortName.equals(serviceOrganization.getOrgShortName())) {
			return serviceOrganization;
		} else {
			try {
				Organization o = null;
				ResultSet rs = select(serviceAuthToken, shortName);
				if (rs != null) {
					if (rs.next()) {
						o = getOrganization(rs.getInt("orgID"));
					}
					rs.close();
				}
				return o;
			} catch (SQLException sqle) {
				throw new AuthException("No such organization");
			}
		}
	}

	/**
	 * isValidToken
	 * 
	 * @return boolean
	 */
	public static boolean isValidToken(AuthToken at) {
		try {
			findOID(at);
			return true;
		} catch (AuthenticationException ae) {
			return false;
		}
	}
	
	public static boolean isUserInOrg(User u, Organization org) throws AuthException {
		List orgTokens = u.getUserOrgTokens();
		AuthToken orgToken;
		AuthToken targetToken = findToken(org.getOrgID());
		for (Iterator i = orgTokens.iterator(); i.hasNext(); ) {
			orgToken = (AuthToken)i.next();
			if (orgToken.equals(targetToken)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * createOrganization
	 * Returns a valid Organization object. If there is not already a reference
	 * to the needed Organization, a new one is created, inserted into the cache,
	 * and returned.
	 * 
	 * @param eid int representing a userEID
	 * @returns A fully initialized User object
	 * @throws AuthenticationException - if User is not cached
	 */	
	private static Organization createOrganization(AuthToken at, int oid) throws AuthenticationException {
		Organization o = null;
		try {
			o = (Organization)((CachedObject)CacheService.getCache(at)).object;
		} catch (CacheException ce) {
			// creating groups isn't really a priveleged operation,
			// so go ahead and create one. if there aren't any users
			// doing lookups on it, it will expire soon enough anyway.
			try {
				o = orgFactory.getInstance(serviceAuthToken, oid);
				organizations.put(new Integer(oid), o);
			} catch (InitializeException ie) {
				AuthenticationException ae = new AuthenticationException("Caught InitializeException creating while Organization.", ie);
				LogService.logError(ae.getMessage(), ie);
				throw ae;
			}
		}
		return o;
	}
	
	/**
	 * findOID
	 * Translates an EOID into a OID. If the EOID is not found in
	 * the CacheService, the EOID/OID is assumed to have expired, the EOID/OID
	 * is removed from the OrgAuthenticationService's active list,
	 * and an AuthenticationException is thrown. 
	 * 
	 * @param eid int representing a EOID
	 * @return int
	 * @throws AuthenticationException - if Organization is not cached
	 */
	public static synchronized int findOID(AuthToken at) throws AuthenticationException {
		Integer oid = (Integer)oids.get(at);
		if (oid == null) {
			throw new AuthenticationException("No corresponding OID");
		} else {
			return oid.intValue();
		}
	}
	
	/**
	 * findToken
	 * 
	 * @return AuthToken
	 */
	static AuthToken findToken(int oid) throws AuthenticationException {
		Integer i = new Integer(oid);
		AuthToken at = (AuthToken)tokens.get(i);
		if (at == null) {
			throw new AuthenticationException("No corresponding AuthToken");
		} else {
			return at;
		}
	}
	
	/**
	 * addToken
	 * 
	 * @return AuthToken
	 */
	static synchronized AuthToken addToken(int oid) throws AuthenticationException {
		Integer i = new Integer(oid);
		AuthToken at;
		
		if (oid < 0) {
			throw new AuthenticationException("Attempt to add invalid OID");
		}
		
		try {
			// return the eoid already in the list
			at = findToken(oid);
		} catch (AuthenticationException ae) {
			// eoid isn't in the list, add a new one and return it
			at = new LongAuthToken(rand.nextLong());
			tokens.put(i, at);
			oids.put(at, i);
			LogService.logDebug("Added AuthToken " + at);
			try {
				// make sure organization is created and in cache
				createOrganization(at, oid);
			} catch (AuthenticationException ae2) {
				LogService.logError("Unable to create Organization", ae2);
			}
		}
		return at;
	}
	
	/**
	 * select
	 * Select all rows from the Organization table, such that this user is allowed to view them.
	 * The userEID supplied is used to check the user's access level and organization,
	 * and only rows that the user is allowed to view are returned. For the org table,
	 * admins can view all organizations, trusted users can view their organization, and all those with 
	 * orgDefaultAID strictly less than their own access level,
	 * and users can only view their own organization.
	 * 
	 * @param eid int representing a userEID
	 * @return Rows selected from the organization table. If no rows were selected, the ResultSet may be null.
	 * @throws AuthenticationException - if unable to determine access level for the given userEID
	 */	
	public static ResultSet select(AuthToken at) throws SQLException,AuthException {
		String query = null;
		
		try {
			CacheService.getCache(at);
		} catch (CacheException ce) {
			AuthenticationException ae = new AuthenticationException("A CacheException has occurred.", ce);
			LogService.logError(ae.getMessage(), ce);
			throw ae;
		}
		query = "SELECT * FROM Organizations ORDER BY OrgName";
		return Connections.select(serviceAuthToken, query, true);
	}
	
	/**
	 * selectRecent
	 */	
	public static ResultSet selectRecent(AuthToken at, int limit) throws SQLException,AuthException {
		String query;
		try {
			CacheService.getCache(at);
		} catch (CacheException ce) {
			AuthenticationException ae = new AuthenticationException("A CacheException has occurred.", ce);
			LogService.logError(ae.getMessage(), ce);
			throw ae;
		}
		query = "SELECT * FROM Organizations ORDER BY OrgDateModified DESC LIMIT " + limit;
		return Connections.select(serviceAuthToken, query, true);
	}

	/**
	 * select
	 * Select a row from the Organization table, such that this user is allowed to view it.
	 * The userEID supplied is used to check the user's access level and organization,
	 * and only rows that the user is allowed to view are returned. For the org table,
	 * admins can view any organization, trusted users can view their organization, and all those with 
	 * orgDefaultAID strictly less than their own access level,
	 * and users can only view their own organization.
	 *
	 * @param eid int representing a userEID
	 * @return Rows selected from the organization table. If no rows were selected, the ResultSet may be null.
	 * @throws AuthenticationException - if unable to determine access level for the given userEID
	 */	
	public static ResultSet select(AuthToken at, int key) throws SQLException,AuthException {
		String query = null;
		
		try {
			CacheService.getCache(at);
		} catch (CacheException ce) {
			AuthenticationException ae = new AuthenticationException("A CacheException has occurred.", ce);
			LogService.logError(ae.getMessage(), ce);
			throw ae;
		}
		query = "SELECT * FROM Organizations WHERE OrgID = " + key + " ORDER BY OrgID";
		return Connections.select(serviceAuthToken, query, true);
	}
	
	/**
	 * select
	 *
	 * @param eid int representing a userEID
	 * @param shortName orgShortName
	 * @return Rows selected from the organization table. If no rows were selected, the ResultSet may be null.
	 * @throws AuthenticationException - if unable to determine access level for the given userEID
	 */	
	public static ResultSet select(AuthToken at, String shortName) throws SQLException,AuthException {
		String query = null;
		
		try {
			CacheService.getCache(at);
		} catch (CacheException ce) {
			AuthenticationException ae = new AuthenticationException("A CacheException has occurred.", ce);
			LogService.logError(ae.getMessage(), ce);
			throw ae;
		}
		query = "SELECT * FROM Organizations WHERE OrgShortName='" + Strings.escapeSQL(shortName) + '\'';
		return Connections.select(serviceAuthToken, query, true);
	}
	
	/**
	 * selectByUser
	 * Select a row from the Organization table, such that this user is allowed to view it.
	 * The userEID supplied is used to check the user's access level and organization,
	 * and only rows that the user is allowed to view are returned. For the org table,
	 * admins can view any organization, trusted users can view their organization, and all those with 
	 * orgDefaultAID strictly less than their own access level,
	 * and users can only view their own organization.
	 *
	 * @param eid int representing a userEID
	 * @return Rows selected from the organization table. If no rows were selected, the ResultSet may be null.
	 * @throws AuthenticationException - if unable to determine access level for the given userEID
	 */	
	public static ResultSet selectByUser(AuthToken at, int id) throws SQLException,AuthException {
		String query = null;
		
		try {
			CacheService.getCache(at);
		} catch (CacheException ce) {
			AuthenticationException ae = new AuthenticationException("A CacheException has occurred.", ce);
			LogService.logError(ae.getMessage(), ce);
			throw ae;
		}
		query = "SELECT Organizations.* FROM Organizations,UserOrgs WHERE "
			+ "UserOrgs.OrgID=Organizations.OrgID AND UserOrgs.UserID=" 
			+ id + " ORDER BY OrgID";	
		return Connections.select(serviceAuthToken, query, true);
	}

	/**
	 * update
	 * Update a row from the Organization table, such that this user is allowed to update it.
	 * The userEID supplied is used to check the user's access level and organization,
	 * and only rows that the user is allowed to update are updated. For the org table,
	 * admins can update any organization, trusted users can update their organization, and all those with 
	 * orgDefaultAID strictly less than their own access level,
	 * and users can only update their own organization.
	 *
	 * @param eid int representing a userEID
	 * @return Number of rows updated from the organization table.s
	 * @throws AuthenticationException - if unable to determine access level for the given userEID
	 */	
	public static int update(AuthToken at, Organization no) throws SQLException,AuthException {
		String update = null;
		User u = null;
		Timestamp now = new Timestamp((new java.util.Date()).getTime());
		
		try {
			u = (User)((CachedObject)CacheService.getCache(at)).object;
		} catch (CacheException ce) {
			AuthenticationException ae = new AuthenticationException("A CacheException has occurred.", ce);
			LogService.logError(ae.getMessage(), ce);
			throw ae;
		}
		
		int accessLevel = AccessAuthenticationService.findAID(u.getUserAccessToken());
		if (accessLevel == AuthLevel.ACCESS_ADMIN) {
			// admins can update any organization
			update = "UPDATE Organizations SET OrgName='" + no.getOrgName() + "',OrgShortName='" + 
				no.getOrgShortName() + "',OrgURL='" + no.getOrgURL() + "',OrgAddress1='" + no.getOrgAddress1() + "',OrgAddress2='" +
				no.getOrgAddress2() + "',OrgCity='" + no.getOrgCity() + "',OrgState='" +
				no.getOrgState() + "',OrgZip='" + no.getOrgZip() + "',OrgCountry='" + no.getOrgCountry() +
				"',OrgPhone='" + no.getOrgPhone() + "',OrgDefaultAID=" + no.getOrgDefaultAID() + 
				",OrgDateModified='" + now + "' WHERE OrgID=" + no.getOrgID();
		} else {
			// users can update their own organizations with a 
			// defaultaid less than their own aid
			update = "UPDATE Organizations SET OrgName='" + no.getOrgName() + "',OrgShortName='" + no.getOrgShortName() + "',OrgURL='" + 
				no.getOrgURL() + "',OrgAddress1='" + no.getOrgAddress1() + "',OrgAddress2='" +
				no.getOrgAddress2() + "',OrgCity='" + no.getOrgCity() + "',OrgState='" +
				no.getOrgState() + "',OrgZip='" + no.getOrgZip() + "',OrgCountry='" + no.getOrgCountry() +
				"',OrgPhone='" + no.getOrgPhone() + "',OrgDateModified='" + now + 
				"' WHERE OrgID=" + no.getOrgID() +" AND (";
			Iterator i = u.getUserOrgTokens().iterator();
			Object orgAt;
			while(i.hasNext()) {
				orgAt = i.next();
				if (orgAt instanceof AuthToken) {
					update += "(OrgID=" + OrgAuthenticationService.findOID((AuthToken)orgAt) + " AND OrgDefaultAID < " + accessLevel + ")";
				}
				if (i.hasNext()) {
					update += " OR ";
				}
			}
			update += ")";
		}
		purgeOID(no.getOrgID());
		ResultSet rs = Connections.update(serviceAuthToken, update);
		if (rs != null) {
			rs.close();
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * insert
	 * Insert a row into the Organization table.
	 * The userEID supplied is used to check the user's access level and organization,
	 * and only rows that the user is allowed to insert are inserted. For the org table,
	 * admins can insert any organization, trusted users can insert organizations with 
	 * orgDefaultAID strictly less than their own access level,
	 * and users cannot insert organizations
	 *
	 * @param eid int representing a userEID
	 * @return Number of rows insert from the organization table.s
	 * @throws AuthenticationException - if unable to determine access level for the given userEID
	 */	
	public static int insert(AuthToken at, Organization no) throws SQLException,AuthException {
		String update = null;
		User u = null;
		Timestamp now = new Timestamp((new java.util.Date()).getTime());
		
		try {
			u = (User)((CachedObject)CacheService.getCache(at)).object;
		} catch (CacheException ce) {
			AuthenticationException ae = new AuthenticationException("A CacheException has occurred.", ce);
			LogService.logError(ae.getMessage(), ce);
			throw ae;
		}
		
		int accessLevel = AccessAuthenticationService.findAID(u.getUserAccessToken());
		if (accessLevel == AuthLevel.ACCESS_ADMIN) {
			// admins can insert any organization
			update = "INSERT INTO Organizations (OrgName,OrgShortName,OrgURL,OrgAddress1,OrgAddress2,OrgCity,OrgState," +
				"OrgZip,OrgCountry,OrgPhone,OrgDefaultAID,OrgDateCreated,OrgDateModified) VALUES ('" + no.getOrgName() + "','" + 
				no.getOrgShortName() + "','" + no.getOrgURL() + "','" + no.getOrgAddress1() + "','" + no.getOrgAddress2() + "','" +
				no.getOrgCity() + "','" + no.getOrgState() + "','" + no.getOrgZip() + "','" + no.getOrgCountry() + "','" + 
				no.getOrgPhone() + "'," + no.getOrgDefaultAID() + ",'" + now + "','" + now + "')";
		} else {
			// users cannot insert organizations
			return 0;
		}
		ResultSet rs = Connections.update(serviceAuthToken, update);
		if (rs != null) {
			rs.close();
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * delete
	 * Delete a row from the Organization table, such that this user is allowed to delete it.
	 * The userEID supplied is used to check the user's access level and organization,
	 * and only rows that the user is allowed to delete are deleted. For the org table,
	 * admins can delete any organization, trusted users can delete organizations with 
	 * orgDefaultAID strictly less than their own access level,
	 * and users cannot delete organizationss
	 *
	 * @param eid int representing a userEID
	 * @return Number of rows deleted from the organization table.s
	 * @throws AuthenticationException - if unable to determine access level for the given userEID
	 */	
	public static int delete(AuthToken at, int key) throws SQLException,AuthException {
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
			// admins can delete any organization
			update = "DELETE FROM Organizations WHERE OrgID=" + key;
		} else {
			// users cannot delete organizations
			return 0;
		}
		ResultSet rs = Connections.update(serviceAuthToken, update);
		if (rs != null) {
			rs.close();
			return 1;
		} else {
			return 0;
		}
	}
	
	/**
	 * purgeOID
	 */
	private static synchronized void purgeOID(int oid) {
		Integer i = new Integer(oid);
		organizations.remove(i);
		AuthToken at = (AuthToken)tokens.get(i);
		oids.remove(at);
		tokens.remove(i);
	}

	/**
	 * getStatus
	 * 
	 * @return String
	 */
	public String getStatus() {
		return "OrgAuthenticationService: tokens=" + tokens.size();
	}
}

