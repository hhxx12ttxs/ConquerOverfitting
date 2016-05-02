/** BEGIN COPYRIGHT BLOCK
 * Copyright (C) 2001 Sun Microsystems, Inc. Used by permission.
 * Copyright (C) 2005 Red Hat, Inc.
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * END COPYRIGHT BLOCK **/

package com.netscape.admin.dirserv.task;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import com.netscape.admin.dirserv.panel.UIFactory;
import com.netscape.management.client.util.ResourceSet;
import com.netscape.management.client.console.*;
import com.netscape.management.client.topology.*;
import com.netscape.management.client.util.*;
import com.netscape.management.client.comm.*;
import com.netscape.admin.dirserv.*;
import java.util.*;
import java.net.*;
import java.io.*;
import netscape.ldap.*;
import netscape.ldap.util.*;

public class MigrateCreate extends CGITask implements IProductObject {
	/**
	 * Constructor, just call super()
	 */
	public MigrateCreate() {
		super();
		setAsync(true);
	}

    /**
	 * Initialize server object with system information such as directory
	 * server location, port number, etc. Called by the topology code after
	 * object construction.
	 *
	 * @param  info - global information
	 */
	public void initialize(ConsoleInfo info) {
//		Debug.setTrace(false);
		print(info, "MigrateCreate.initialize():");
		_consoleInfo = info;
	}

	/**
	 * Starts the server specific migration code, providing the server root
	 * from which the specified servers should be migrated. The DN for the
	 * target admin group of the migration as well as whether existing SIE
	 * should be overwritten is also provided. The method returns true or
	 * false depending on whether it was successful.
	 *
	 * @param  serverRoot - directory path for the migration origin.
	 * @param  server - the server to migrate from the serverRoot.
	 * @param  targetDN - the destination admin group DN.
	 * @return  boolean value indicating whether the process succeeded (true)
	 *          or failed (false).
	 */
	public boolean migrate(String serverRoot,
						   String server,
						   String targetDN) {
		return migrate(serverRoot, server, targetDN, true);
	}

	public boolean migrate(String serverRoot,
						   String server,
						   String targetDN,
						   boolean flag) {
//		Debug.setTrace(false);
		Debug.println("MigrateCreate.migrate(): serverRoot = " +
					  serverRoot + " server = " + server +
					  " targetDN = " + targetDN);

		Hashtable configParams = new Hashtable();

		String serverid = server;
		String prefix = "slapd-";
		if (server.startsWith(prefix))
			serverid = server.substring(prefix.length());

		// get the old port and root dn
		Hashtable args = new Hashtable();
		// value is name of config param in slapd.conf; key is also unless it
		// is not unique
		args.put("rootdn", "rootdn");
		args.put("port", "port");
		if (!isNT(targetDN)) {
			args.put("oldlocaluser", "localuser");
			args.put("newlocaluser", "localuser");
		}
		args.put("oldServerRoot", serverRoot);
		args.put("oldServerName", serverid);
		args.put("localhost", "localhost");
		// set the arguments for the CGI call
		_consoleInfo.put("arguments", args);
		// we have to do this because this is a non instance specific task
		_consoleInfo.put(GET_CONFIG_INFO_CGI_NAME, "slapd");

		if (_consoleInfo.get(GlobalConstants.TASKS_AUTH_DN) == null)
			_consoleInfo.put(GlobalConstants.TASKS_AUTH_DN,
							 _consoleInfo.getAuthenticationDN());
		Debug.println("AdminUsername = " +
					  _consoleInfo.get(GlobalConstants.TASKS_AUTH_DN));

		if (_consoleInfo.get(GlobalConstants.TASKS_AUTH_PWD) == null)
			_consoleInfo.put(GlobalConstants.TASKS_AUTH_PWD,
							 _consoleInfo.getAuthenticationPassword());
		Debug.println("AdminUserPassword = " +
					  _consoleInfo.get(GlobalConstants.TASKS_AUTH_PWD));

		// call the CGI program
		boolean status = super.run(null, GET_CONFIG_INFO_CGI_NAME);

		// parse reply
		if (_cgiResponse.containsKey("port"))
			configParams.put("servport", _cgiResponse.get("port"));
		if (_cgiResponse.containsKey("rootdn"))
			configParams.put("rootdn", _cgiResponse.get("rootdn"));
		if (_cgiResponse.containsKey("oldlocaluser"))
			configParams.put("oldlocaluser", _cgiResponse.get("oldlocaluser"));
		if (_cgiResponse.containsKey("newlocaluser"))
			configParams.put("newlocaluser", _cgiResponse.get("newlocaluser"));
		if (_cgiResponse.containsKey("needSecPwd"))
			configParams.put("secpwd", "");
		if (_cgiResponse.containsKey("localhost")) {
			configParams.put("localhost", _cgiResponse.get("localhost"));
		} // otherwise, the servname parameter will be used

		JFrame frame = UtilConsoleGlobals.getActivatedFrame();
		
		// OK now we have the info from the old server.  Ask the user to select
		// the server to migrate into or to create a new one
		// Get a list of installed servers
		String createNewOneString = _resource.getString("MigrateCreate",
														"creation-label");

		String[] serverList = getServerList(targetDN, createNewOneString, null);
		ProductSelectionDialog serverSelection =
			new ProductSelectionDialog(frame,
									   ProductSelectionDialog.FOR_MIGRATION);
		serverSelection.setProductList(serverList);
		serverSelection.show();
		if (serverSelection.isCancel()) {
			Debug.println("MigrateCreate.migrate: server selection " +
						  "dialog was cancelled.");
			return false;
		}

		int[] selectedServers = serverSelection.getSelectedIndices();
		if (selectedServers.length == 0) {
			Debug.println("MigrateCreate.migrate: no destination" +
						  " server selected.");
			return false;
		}

		String[] keys = {null, null, null, null, null, null, null, null, null};
		// just use the first one selected if multiple
		String newServer = serverList[selectedServers[0]];
		boolean migrateUseExistingInstance = false;
		if (newServer.equals(createNewOneString)) {
			// they are creating a new instance.  Use the old name if it is
			// not in use, otherwise, ask the user to specify a new name
			Debug.println("MigrateCreate.migrate(): searching for serverid=" +
						  serverid + " under DN=" + targetDN);
			// if a new server already exists with the old server name, we
			// will need to ask the user to supply a new name, otherwise . . .
			if (attrValueExists(targetDN, "nsServerID", serverid, true) ||
				attrValueExists(targetDN, "nsServerID",
								"slapd-" + serverid, true)) {
				keys[NewInstanceDialog.INSTANCENAME] = "servid";
				configParams.remove("servid"); // no default value
			} else { // . . . just use the old server name
				configParams.put("servid", serverid);
			}

			// However, on NT, the server id is also used for the NT
			// service name.  So, we need to ask for a new server id.
			// We also need to make sure it is not the same as the old
			// one after the dialog . . .
			if (isNT(targetDN)) {
				configParams.put("servid", serverid + "-m");
				configParams.put("oldServerID", serverid);
				keys[NewInstanceDialog.INSTANCENAME] = "servid";
			}
		} else { // merge with existing instance
			if (newServer.startsWith(prefix))
				newServer = newServer.substring(prefix.length());
			// they have selected to migrate into an existing instance, so
			// we don't need to ask for the name
			configParams.put("servid", newServer);
			migrateUseExistingInstance = true;
		}

		keys[NewInstanceDialog.ROOTDNPWD] = "rootpw";
		// the suffix, rootdn and port will be determined from the
		// old installation
		configParams.put("oldServerRoot", serverRoot);
		configParams.put("oldServerName", serverid);
		// by default, always shutdown the old server
		keys[NewInstanceDialog.SHUTDOWN_OLD] = "shutdown_old_server";
		configParams.put("shutdown_old_server", new Boolean(true));

		// if the old and the new user are different, ask the user to
		// choose which one to use, with the default being the new
		// user
		if (!isNT(targetDN)) {
			String olduser = (String)configParams.get("oldlocaluser");
			String newuser = (String)configParams.get("newlocaluser");
			if ((olduser == null) || !olduser.equals(newuser)) {
				keys[NewInstanceDialog.LOCALUSER] = "servuser";
				configParams.put("servuser", newuser);
			}
		}

		// see if we need to as the user for the password to use to upgrade the
		// existing cert and key db to the new format
		if (configParams.containsKey("secpwd")) {
			keys[NewInstanceDialog.SECPWD] = "secpwd";
		}

		_finishedString = MIGRATION_FINISHED_STRING;

		return createAnInstance(targetDN, keys,
								configParams, MIGRATE_CGI_NAME,
								migrateUseExistingInstance);
	}

	/**
	 * Starts the server specific creation code, providing the DN for the
	 * target admin group. The method returns true or false depending
	 * on whether it was successful.
	 *
	 * @param  targetDN - the admin group DN where the new instance is to be
	 *                    created.
	 * @return  boolean value indicating whether the process succeeded (true)
	 *          or failed (false).
	 */
	public boolean createNewInstance(String targetDN) {
		boolean status = false; // return value
//		Debug.setTrace(false);
		Debug.println("MigrateCreate.createNewInstance(): targetDN = " +
					  targetDN);

		// this hashtable will contain the parameters used to configure the
		// new server instance.  the key will be the name of the CGI parameter,
		// and the value will be the value
		Hashtable configParams = new Hashtable();
		String[] keys = null;
		String[] NTkeys = {
			"servid",
			"servport",
			"suffix",
			"rootdn",
			"rootpw",
			null,
			null,
			null,
			"servname"
		};
		String[] Unixkeys = {
			"servid",
			"servport",
			"suffix",
			"rootdn",
			"rootpw",
			"servuser",
			null,
			null,
			"servname"
		};
		if (isNT(targetDN)) {
			keys = NTkeys;
		} else {
			keys = Unixkeys;
		}

		return createAnInstance(targetDN, keys, configParams, CREATE_CGI_NAME,
								false);
	}

	/**
	 * Starts the server specific creation code, providing the DN for the
	 * target admin group. The method returns true or false depending
	 * on whether it was successful.
	 *
	 * @param  targetDN     the admin group DN where the new instance is to be
	 *                      created.
	 * @param  keys         a list of keys in the configParams hashtable that
	 *                      we need to ask the user for the values of
	 * @param  configParams the cgi variable names and values to pass to the cgi
	 * @param  urlTask      the cgi task name
	 * @param  migrateUseExistingInstance true if we're migrating into an
	 *                      existing ds instance
	 * @return  boolean value indicating whether the process succeeded (true)
	 *          or failed (false).
	 */
	private boolean createAnInstance(String targetDN, String[] keys,
									 Hashtable configParams, String cgiTask,
									 boolean migrateUseExistingInstance) {
		JFrame frame = UtilConsoleGlobals.getActivatedFrame();
		boolean status = false; // return value
//		Debug.setTrace(false);
		Debug.println("MigrateCreate.createNewInstance(): targetDN = " +
					  targetDN);

		
		// the full machine name is stored in the serverhostname attribute in
		// the parent entry of targetDN
		String[] entries = LDAPDN.explodeDN(targetDN, false);
		String hostDN = entries[entries.length-3] + ", " +
			entries[entries.length-2] + ", " +
			entries[entries.length-1];
		configParams.put("servname",
						 getValue(hostDN, "serverHostName",
								  LDAPConnection.SCOPE_BASE, null));

		LDAPConnection ldc = _consoleInfo.getLDAPConnection();
		String ssdn = ldc.getAuthenticationDN();
		// ss id must be the full dn so the new instance can register its
		// SIE and ISIE information with the kingpin server
		configParams.put("cfg_sspt_uid", ssdn);
		configParams.put("cfg_sspt_uid_pw",
						 ldc.getAuthenticationPassword());

		// we need to get the uid to use for the !$@#^@# suitespot
		// 3.x support . . . grumble, grumble . . .
		if (!configParams.containsKey("suitespot3x_uid")) {
			String filter = "(cn=Configuration Administrator)";
			String dn = "ou=TopologyManagement, " +
				entries[entries.length-1];
			String ssuid = getValue(dn, "uid",
									LDAPConnection.SCOPE_SUB, filter);
			if (ssuid == null)
				ssuid = "admin";
			configParams.put("suitespot3x_uid", ssuid);
		}

		String baseSuffix = (String)_consoleInfo.get("BaseDN");
		configParams.put("ldap_url", DSUtil.getLdapURL(ldc, baseSuffix));

		// set the default user
		if (!isNT(targetDN) &&
			!configParams.containsKey("servuser")) {
			// find the nssuitespotuser
			String filter = "(&(objectclass=nsConfig)" +
				"(objectclass=nsDirectoryServer))";
			DN dn = new DN(targetDN);
			String user = null;

			// keep searching until we find some entry which has
			// the suitespot user
			while ((user == null) && (dn != null) &&
				   (dn.countRDNs() > 1)) {
				user = getValue(dn.toString(), "nssuitespotuser",
								LDAPConnection.SCOPE_SUB, filter);
				dn = dn.getParent();
			}

			if (user != null)
				configParams.put("servuser", user);
			else
				Debug.println("MigrateCreate.createNewInstance(): " +
							  "did not find nssuitespotuser under DN=" +
							  targetDN);
		}

		if (!configParams.containsKey("admin_domain")) {
			RDN rdn = new RDN(entries[entries.length-2]);
			configParams.put("admin_domain", rdn.getValues()[0]);
		}

		// if we're on a unix system and the admin server is not
		// being run by the superuser, port 389 is not available
		// assign a random port number
		if (!configParams.containsKey("servport")) {
			String port = "389";
			boolean inuse = getValue(hostDN, "nsserverport",
									 LDAPConnection.SCOPE_SUB,
									 "nsserverport=" + port) != null;

			// if the default port is not in use, see if we have
			// permission to use it
			String dsuser = null;
			String asuser = null;
			if (!inuse && !isNT(targetDN)) {
				dsuser = (String)configParams.get("servuser");
				String filter = "(&(objectclass=nsConfig)" +
					"(objectclass=nsAdminConfig))";
				asuser = getValue(targetDN, "nssuitespotuser",
								  LDAPConnection.SCOPE_SUB,
								  filter);
			}

			// either the ds user or the as user must be root
			// to use ports < 1024
			if (inuse ||
				(dsuser != null && !dsuser.equals("root")) ||
				(asuser != null && !asuser.equals("root"))) {
				// get a random port > 1024
				double maxport = (double)32767.0 - (double)1024.0;
				double dport = Math.random()*maxport +
					(double)1024.0;
				port = Integer.toString((int)Math.rint(dport));
			} else {
				Debug.println("inuse=" + inuse + " dsuser=" + dsuser +
							  " asuser=" + asuser);
			}

			configParams.put("servport", port);
		}

		if (!configParams.containsKey("servid")) {
			// derive the serveridentifier from the hostname
			String id;
			String serverHost =
				(String)configParams.get("servname");
			int index = serverHost.indexOf('.');
			if (index > -1)
				id = serverHost.substring(0, index);
			else
				id = serverHost;

			String[] list = getServerList(targetDN, null, id);
			// set default servid to the base hostname, adding a number
			// for each one in use already
			if (list != null && list.length > 0)
				configParams.put("servid", id + (list.length+1));
			else
				configParams.put("servid", id);
		}

		if (!configParams.containsKey("suffix")) {
			configParams.put("suffix", baseSuffix);

			// try to determine what the default user directory suffix is
			// and use that one instead
			String filter = "(objectclass=nsDirectoryInfo)";
			String dn = getValue(targetDN, "nsdirectoryinforef",
								 LDAPConnection.SCOPE_BASE, filter);
			if (dn != null) { // dn of entry with user dir ldap url
				String url = getValue(dn, "nsdirectoryurl",
									  LDAPConnection.SCOPE_BASE, filter);
				if (url != null) { // ldap url of user directory
					try {
						LDAPUrl lurl = new LDAPUrl(url);
						if (lurl != null && lurl.getDN() != null) {
							configParams.put("suffix", lurl.getDN());
						}
					} catch (Exception e) {
					}
				}
			}
		}

		// use the same root dn as the other directory server
		if (!configParams.containsKey("rootdn")) {
			String filter = "(&(objectclass=netscapeServer)" +
				"(objectclass=nsDirectoryServer))";
			DN dn = new DN(targetDN);
			String rootdn = null;

			// keep searching until we find some entry which has
			// the nsbinddn
			while ((rootdn == null) && (dn != null) &&
				   (dn.countRDNs() > 1)) {
				rootdn = getValue(dn.toString(), "nsbinddn",
								  LDAPConnection.SCOPE_SUB, filter);
				dn = dn.getParent();
			}

			if (rootdn == null)
				configParams.put("rootdn", DSUtil.getRootDN());
			else
				configParams.put("rootdn", rootdn);
			
		}
		if (!configParams.containsKey("rootpw"))
			configParams.put("rootpw", "");
		if (!configParams.containsKey("start_server"))
			configParams.put("start_server", "1");

		boolean done = false;
		boolean migrating = (keys[NewInstanceDialog.SHUTDOWN_OLD] != null);
		// attempt to connect to the old server.  If it's already down, no
		// need to shut it down
		if (keys[NewInstanceDialog.SHUTDOWN_OLD] != null) {
			String host = (String)configParams.get("localhost");
			if (host == null)
				host = (String)configParams.get("servname");
			int port = 0;
			try {
				port = Integer.parseInt((String)configParams.get("servport"));
			} catch (Exception e) {}
			if (host != null && port > 0) {
				try {
					LDAPConnection ldctemp = new LDAPConnection();
					ldctemp.connect(host, port);
					ldctemp.disconnect();
				} catch (Exception e2) {
					Debug.println("MigrateCreate.createAnInstance(): failed " +
								  "to connect to old server at " + host +
								  ":" + port + " error [" + e2 + "] " +
								  "assuming it is already down");
					keys[NewInstanceDialog.SHUTDOWN_OLD] = null;
					if (configParams.containsKey("shutdown_old_server"))
						configParams.remove("shutdown_old_server");
				}
			}
		}

		while (!done && keys != null) {
			try {
			NewInstanceDialog nid =
				new NewInstanceDialog(frame,
									  keys, configParams);
//			nid.setLocationRelativeTo(frame);
			Debug.println("MigrateCreate.createAnInstance(): now calling show()");
			nid.show();
			if (nid.isCancel()) {
				return status;
			}
			}
			catch(Exception x) {
				x.printStackTrace();
			}

			// if we are migrating, we may want to migrate data into an
			// existing server, so skip the duplicate search
			String serverid =
				(String)configParams.get("servid");
			if (!migrating) {
				if (attrValueExists(targetDN, "nsServerID", serverid, true) ||
					attrValueExists(targetDN, "nsServerID",
									"slapd-" + serverid, true)) {
					DSUtil.showErrorDialog(frame,
										   "117", serverid);
				} else {
					done = true;
				}
			} else if (isNT(targetDN)) {
				String oldServerID =
					(String)configParams.get("oldServerID");
				Debug.println("MigrateCreate.createAnInstance():" +
							  "serverid=" + serverid + " oldServerID=" +
							  oldServerID);
				if (serverid.equals(oldServerID)) {
					DSUtil.showErrorDialog(frame,
										   "ntserviceinuse", serverid);
				} else {
					done = true;
				}
			} else {
				done = true;
			}
		}

		// The user may have done something brain dead like migrate an old
		// server, then shut it down, then migrate it again using a different
		// name.  Then again, it could be that a power user is trying to do
		// something tricky.  We need to check for this condition and ask the
		// user for confirmation.
		if (attrValueExists(hostDN, "nsServerPort",
							configParams.get("servport").toString(),
							true) &&
			!migrateUseExistingInstance) {
			String[] args = { configParams.get("servport").toString(),
							  configParams.get("servname").toString() };
			int res = DSUtil.showConfirmationDialog(frame,
													"portinuse2", args,
													"general");
			if (res == JOptionPane.NO_OPTION) {
				return false; // bail
			}
		}

		if (configParams.containsKey("shutdown_old_server")) {
			Boolean val = (Boolean)configParams.get("shutdown_old_server");
			Debug.println("MigrateCreate.createNewServer: shutdown=" + val);
			if (!val.booleanValue())
				configParams.remove("shutdown_old_server");
		}

		// set the arguments for the CGI call
		_consoleInfo.put("arguments", configParams);
		// we have to do this because this is a non instance specific task
		_consoleInfo.put(cgiTask, "slapd");

		if (_consoleInfo.get(GlobalConstants.TASKS_AUTH_DN) == null)
			_consoleInfo.put(GlobalConstants.TASKS_AUTH_DN,
							 _consoleInfo.getAuthenticationDN());
		Debug.println("AdminUsername = " +
					  _consoleInfo.get(GlobalConstants.TASKS_AUTH_DN));

		if (_consoleInfo.get(GlobalConstants.TASKS_AUTH_PWD) == null)
			_consoleInfo.put(GlobalConstants.TASKS_AUTH_PWD,
							 _consoleInfo.getAuthenticationPassword());
		Debug.println("AdminUserPassword = " +
					  _consoleInfo.get(GlobalConstants.TASKS_AUTH_PWD));

		// popup a window to display the reply to the user
		_statusDialog = new StatusDialog( frame );
		_statusDialog.setOKButtonEnabled(false);
//		_statusDialog.setLocation( 200, 200 );
		_statusDialog.setTitle(_resource.getString("MigrateCreate-statuswindow",
												  "title"));
		_statusDialog.appendText(_resource.getString("MigrateCreate-statuswindow",
												"creating-text") + '\n');
	    _statusDialog.show();

		// call the CGI program
		Debug.println("MigrateCreate.createAnInstance(): before run task=" +
					  cgiTask);
		_cgiTask = cgiTask;
		status = super.run(null, cgiTask);
		Debug.println("MigrateCreate.createAnInstance(): after run status=" +
					  status + " _success=" + _success);

		// Put the final message in the status window
		// and enable the OK button.
		String msgkey = _success ? "success-label" : "failed-label";
		String msg = _resource.getString("MigrateCreate", msgkey);
		_statusDialog.appendText("\n" + msg);
		_statusDialog.setOKButtonEnabled(true); // operation is complete, so enable button

		return _success;
	}

    /**
     * Searches the given tree branch for the given attribute with the given
	 * value.  Useful to determine if the user specified port or server
	 * identifier is already in use.
     *
     * @param     targetDN the base DN for the individual server entries e.g.
	                       cn=SuiteSpot 4.0, cn=fqdn, o=NetscapeRoot
	 * @param     attr     attribute to search for
	 * @param     value    value of attribute to match
     * @return    true if an attribute exists whose value matches the given
	 *            value
	 *            false if no attribute was found, no match was found, or other
	 *            error
     **/
    protected boolean attrValueExists(String targetDN, String attr,
									  String value, boolean subtree) {
		LDAPSearchResults result = null;
		// just do a simple equality filter
		if (value == null)
			return false; // null values do not exist
		if (attr == null)
			attr = "*"; // null attr searches all attrs
		String filter = "(" + attr + "=" + value + ")";
		int scope = LDAPConnection.SCOPE_ONE;
		if (subtree)
			scope = LDAPConnection.SCOPE_SUB;

		try {
			LDAPConnection ldc = _consoleInfo.getLDAPConnection();
			if (ldc != null)
			{
				result = ldc.search(targetDN, scope, filter, null, false);
			}
		} catch (LDAPException e) {
			Debug.println("error MigrateCreate.serverExists: LDAP search " +
						  "failed: " + filter);
			Debug.println("error MigrateCreate.serverExists: could not " +
						  "find any servers under " + targetDN + " error: " +
						  e);
		}

		boolean exists = false;
		if (result.hasMoreElements())
			exists = true;

/*
		if (Debug.getTrace()) {
			Debug.println("attrValueExists(): results");
			while (result.hasMoreElements())
				Debug.println("\tresult=" + result.nextElement());
		}
*/
		return exists;
    }

    /**
     * Get the values for several specified attributes from the given DN.
	 * If there is more than 1 entry which matches the given criteria, the
	 * first one will be used.
     *
     * @param  DN     DN of the entry with the specified attributes
     * @param  attrs  Array of attributes to get the values of
	 * @param  scope  LDAPConnection SCOPE_BASE SCOPE_ONE SCOPE_SUB
	 * @param  filter LDAP search filter; if null, default is objectclass=*
     * @return        An array of string values for each attribute; multi-valued
	 *                attributes are returned as 1 value, space delimited
	 *                (flattened)
     **/
    protected String[] getValues(String DN, String[] attrs, int scope,
								 String filter) {
		String[] values = null;
		LDAPSearchResults results = null;
		if (filter == null)
			filter = "(objectclass=*)";

		try {
			LDAPConnection ldc = _consoleInfo.getLDAPConnection();
			if (ldc != null)
			{
				results = ldc.search(DN, scope, filter, attrs, false);
			}
		} catch (LDAPException e) {
			Debug.println("error MigrateCreate.getValues: LDAP read failed " +
						  "for DN=" + DN + " attributes " + attrs);
			Debug.println("error MigrateCreate.getValues: LDAP Exception:" +
						  e);
		}

		if (results != null && results.hasMoreElements()) {
			values = new String[attrs.length];
			LDAPEntry entry = (LDAPEntry)results.nextElement();
			for (int ii = 0; entry != null && ii < attrs.length; ++ii) {
				values[ii] = LDAPUtil.flatting(entry.getAttribute(attrs[ii]));
			}
		} else {
			Debug.println("error MigrateCreate.getValues: LDAP read failed " +
						  "for DN=" + DN + " attributes=" + attrs);
		}

		return values;
    }

    /**
     * Get one value for one specified attribute from the given DN.
	 * If there is more than 1 entry which matches the given criteria, the
	 * first one will be used.
     *
     * @param  DN     DN of the entry with the specified attributes
     * @param  attr   Attribute to get the value of
	 * @param  scope  LDAPConnection SCOPE_BASE SCOPE_ONE SCOPE_SUB
	 * @param  filter LDAP search filter; if null, default is objectclass=*
     * @return        The string value of the attribute; multi-valued
	 *                attributes are returned as 1 value, space delimited
	 *                (flattened)
     **/
    protected String getValue(String DN, String attr, int scope,
							  String filter) {
		String[] attrs = { attr };
		String[] values = getValues(DN, attrs, scope, filter);
		if (values != null)
			return values[0];

		return null;
	}

	/**
	 * Get a list of names of directory servers stored under the given
	 * DN.
	 * @param  DN    The DN of the parent entry holding the SIE entries
	 * @param  other Another string to tack on to the start of the list
	 * @param  val   Used as an instance name qualifier
	 * @return       The list of directory server names
	 **/
	protected String[] getServerList(String DN, String other, String val) {
		Vector v = new Vector();
		LDAPSearchResults result = null;
		String crit = (val != null) ? val : "";
		String filter = "&(nsServerID=slapd-" + crit + "*)(cn=slapd-" + crit + "*)";

		int scope = LDAPConnection.SCOPE_SUB;

		try {
			LDAPConnection ldc = _consoleInfo.getLDAPConnection();
			if (ldc != null)
			{
				result = ldc.search(DN, scope, filter, null, false);
			}
		} catch (LDAPException e) {
			Debug.println("error MigrateCreate.getServerList: LDAP search " +
						  "failed: " + filter);
			Debug.println("error MigrateCreate.getServerList: could not " +
						  "find any servers under " + DN + " error: " +
						  e);
		}

		if (other != null)
			v.addElement(other);

		while (result.hasMoreElements()) {
			String val2 = LDAPUtil.flatting(
				((LDAPEntry)result.nextElement()).getAttribute("nsServerID"));
			Debug.println("MigrateCreate.getServerList(): val = " + val2);
			v.addElement(val2);
		}

		String[] retval = new String[v.size()];
		v.copyInto(retval);

		return retval;
	}

	/**
	 * Determines what OS the remote admin server is using e.g. the server which
	 * will be executing the create/migrate CGI.  Uses the AdminOS keyword
	 * in the consoleInfo Hashtable.
	 *
	 * @return true if the remote Admin Server is running on NT, false otherwise
	 **/
	private boolean isNT(String targetDN) {
		boolean retval = false;
		Debug.println("MigrateCreate:isNT(): BEGIN");
		String os = (String)_consoleInfo.get("AdminOS");
		Debug.println("MigrateCreate:isNT(): os string=" + os);
		if (os == null) { // search for it
			DN dn = new DN(targetDN);
			os = getValue(dn.getParent().toString(), "nsosversion",
						  LDAPConnection.SCOPE_BASE, null);
			Debug.println("MigrateCreate:isNT(): search under DN=" + targetDN +
				" for os=" + os);
			if (os != null) {
				_consoleInfo.put("AdminOS", os);
			}
		}
		if (os != null) {
			if (os.indexOf("Windows NT") >= 0) {
				retval = true;
			}
		}

		Debug.println("MigrateCreate:isNT(): return=" + retval);
		return retval;
	}

	/**
	 *	the operation is finished after we receive the http stream
	 */
    public void replyHandler(InputStream response, CommRecord cr) {
		Debug.println("MigrateCreate.replyHandler BEGIN this=" + this +
					  " cr=" + cr + " task=" + _cgiTask);
		_success = false;
		if (_cgiResponse != null)
			_cgiResponse.clear();
		try {
			BufferedReader rspStream =
				new BufferedReader(new InputStreamReader(response, "UTF8"));
			String rspStr;

			Debug.println("MigrateCreate.replyHandler: start");
			boolean done = false;
			while (!done && ((rspStr = rspStream.readLine()) != null))
			{
				Debug.println("MigrateCreate.replyHandler: read [" + rspStr + "]");
				// NMC_ messages are parsed, but not shown to the user
				if (_statusDialog != null && !rspStr.startsWith("NMC_") &&
					(_finishedString == null ||
					 rspStr.indexOf(_finishedString) == -1)) {
					_statusDialog.invokeAppendText(rspStr + "\n");
                    Thread.yield(); // allow graphics repaints
				}
				done = parse(rspStr);
			}
		} catch (Exception e) {
			Debug.println("MigrateCreate.replyHandler: " + e.toString());
		}

		Debug.println("MigrateCreate.replyHandler END this=" + this +
					  " cr=" + cr + " task=" + _cgiTask);
		finish();
	}


	/**
	 * return the value for the given keyword in the reply
	 * returns true if there will be no more output, false otherwise
	 */
	private boolean parse(String s) {
		String sName;
		String sValue;
		int iIndex;
		boolean retval = false;

		Debug.println("Parse input: " + s);

		if ((iIndex=s.indexOf(":")) != (-1))
		{
			sName = s.substring(0, iIndex).trim();
			sValue = s.substring(iIndex+1).trim();
			Debug.println("Parse input: name=" + sName + " value=" + sValue);
			if (_cgiResponse == null)
				_cgiResponse = new Hashtable();
			_cgiResponse.put(sName, sValue);
			if (sName.equalsIgnoreCase("NMC_Status"))
			{
				int code = Integer.parseInt(sValue);
				_success = (code == 0);
				Debug.println("Parse input: code=" + code + " _success=" + _success);
				retval = true;
			}
		} else if ((_finishedString != null) &&
				   (s.indexOf(_finishedString) != -1)) {
			retval = true;
		}

		Debug.println("Parse finished val=" + retval);
		return retval;
    }

	private void print(Hashtable h, String msg) {
		Debug.println(msg);
		for (Enumeration e = h.keys(); e.hasMoreElements();) {
			String key = (String)e.nextElement();
			Debug.println("\t" + key + "=" + h.get(key));
		}
		Debug.println("");
	}

	static private final String CREATE_CGI_NAME =
	    "Tasks/Operation/Create";
	static private final String MIGRATE_CGI_NAME =
	    "Tasks/Operation/Migrate";
	static private final String GET_CONFIG_INFO_CGI_NAME =
	    "Tasks/Operation/GetConfigInfo";

	private boolean _success = false; // status of last executed CGI
	private Hashtable _cgiResponse = null; // holds parsed contents of CGI return
	private String _cgiTask = null; // CGI task to call
	private StatusDialog _statusDialog = null; // to display the CGI output
	private String _finishedString = null; // string sent from client which
								// indicates end of operation

	// the migration CGI writes this string to indicate that the operation is finished
	static private final String MIGRATION_FINISHED_STRING = "###MIGRATION FINISHED###";
	static private ResourceSet _resource = 
		new ResourceSet("com.netscape.admin.dirserv.dirserv");

}


/**
 * An AbstractDialog with a JTextArea used
 * to display the output of the start CGI.
 */
class StatusDialog extends AbstractDialog {

	/**
	 * Make an empty status dialog
	 */
	StatusDialog(JFrame f) {
		super(f, "", false, AbstractDialog.OK);
		_statusText = new JTextArea(10, 40) {
             protected int getRowHeight() { 
                 _statusTextRowHeight = super.getRowHeight(); 
                 return _statusTextRowHeight; 
             } 
        };
		_statusText.setEditable( false );
		_statusText.setBackground( getBackground() );
		_statusScroller = new JScrollPane(_statusText);
		_statusScroller.setVerticalScrollBarPolicy(
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
		_statusScroller.setHorizontalScrollBarPolicy(
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		getContentPane().add( _statusScroller );
		pack();
	}
	
	/**
	 * Append text to _statusText and scroll to make it visible.
	 */
	void appendText(String t) {
		_statusText.append(t);
        int line = _statusText.getLineCount();
        Rectangle scrollTo = new Rectangle( 
            0, (line-1)*_statusTextRowHeight, 1,
            _statusScroller.getHeight()); 
        _statusText.scrollRectToVisible(scrollTo);
		_statusText.revalidate(); // Required to see the changes
	}


	/**
	 * A call to appendToStatusText() embedded in invokeLater.
	 */
	void invokeAppendText(final String t) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				appendText(t);
			}
		});
	}

    private int _statusTextRowHeight = 0; // height of a fow in text window
    private JScrollPane _statusScroller = null; // scroll pane containing text area
	private JTextArea _statusText = null; // window used to display status
}

