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

package com.netscape.admin.dirserv;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.net.*;
import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.Collator;
import java.text.ParsePosition;
import java.text.DateFormat;
import java.text.StringCharacterIterator;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.util.zip.CRC32;
import com.netscape.management.client.Framework;
import com.netscape.management.client.console.ConsoleInfo;
import com.netscape.management.client.util.RemoteImage;
import com.netscape.management.client.util.ResourceSet;
import com.netscape.management.client.util.Debug;
import com.netscape.management.client.util.Help;
import com.netscape.management.client.util.UtilConsoleGlobals;
import com.netscape.management.client.util.LDAPUtil;
import com.netscape.management.client.preferences.Preferences;
import com.netscape.management.client.preferences.PreferenceManager;
import com.netscape.management.client.topology.IServerObject;
import netscape.ldap.*;
import netscape.ldap.util.*;

public class DSUtil {

    private DSUtil() {}

    /**
     * Initialize global data
     *
     * @param ldc LDAPConnection, connected.
     */
    static public void initialize( LDAPConnection ldc, String dn ) {
	//		Debug.println( "DSUtil.initialize: " + ldc );
	if ( _cRequiresRestart == null )
	    _cRequiresRestart = getRequiresRestartTable( ldc );
	_authName = dn;
    }

    static public void setConfigConnection(LDAPConnection configLdc) {
	// connection to the Config directory
	_configLdc = configLdc;
    }

    /**
     * Reauthenticate the console as a specified user.  The authentication
     * is done against the directory server whose directory console
     * is currently being viewed.  Instead of the listeners being notified
     * inline, a DSUtil.DeferAuthListeners object is returned.  The user
     * will call the notify() method when they wish to notify the listeners
     * of the authentication change.  This is useful primarily when the
     * reauthentication is done in the middle of some operation which needs
     * more access but the user wants the operation to finish before the
     * listeners are notified e.g. subtree deletion.
     *     
     * @param ldc           - directory console connection
     * @param frame         - frame window for the application
     * @param listeners     - list of listeners to notify when auth is done
     * @param authDN        - dn to authenticate with
     * @param authPassword  - password to authenticate with
     * 
     * @return true if the connection was successfully reauthenticated
     */

    static public DeferAuthListeners reauthenticateDefer(LDAPConnection ldc, JFrame frame,
							 Vector listeners, String authDN, String authPassword) {
	String oldDN = ldc.getAuthenticationDN();
	String oldPwd = ldc.getAuthenticationPassword();
	boolean status = reauthenticate(ldc, frame, null,
					authDN, authPassword);
	if (status) {
	    return new DeferAuthListeners(listeners, oldDN,
					  ldc.getAuthenticationDN(),
					  oldPwd,
					  ldc.getAuthenticationPassword());
	}
	return null; // reauth failed
    }
														   
    /**
     * Reauthenticate the console as a specified user.  The authentication
     * is done against the connection to the directory server whose directory console
     * is currently being viewed.
     *     
     * @param ldc           - directory console connection
     * @param frame         - frame window for the application
     * @param listeners     - list of listeners to notify when auth is done
     * @param authDN        - dn to authenticate with
     * @param authPassword  - password to authenticate with
     * 
     * @return true if the connection was successfully reauthenticated
     */

    static public boolean reauthenticate(LDAPConnection ldc,
					 JFrame frame,
					 Vector listeners,
					 String authDN,
					 String authPassword) {
	boolean status = true;
	String oldDN = ldc.getAuthenticationDN();
	String oldPwd = ldc.getAuthenticationPassword();	
	if (ldc != null) {
	    status = doReauthenticate(ldc, frame, null,
				    authDN, authPassword);
	}	

	// if the connection was successfully
	// reauthenticated, notify the listeners
	if (status) {
	    if (ldc != null) {
		notifyListeners(listeners, oldDN, ldc.getAuthenticationDN(),
				oldPwd, ldc.getAuthenticationPassword());
	    } 
	}
	return status;
    }

    /**
     * Reauthenticate the console as a specified user.
     *
     * @param ldc           - directory console connection
     * @param frame         - frame window for the application
     * @param listeners     - list of listeners to notify when auth is done
     * @param authDN        - dn to authenticate with
     * @param authPassword  - password to authenticate with
     * 
     * @return true if the connection was successfully reauthenticated
     */

    static private boolean doReauthenticate(LDAPConnection ldc, JFrame frame,
					  Vector listeners,
					  String authDN,
					  String authPassword ) {
	String oldDN = null, oldPwd = null;
	boolean status = true;

	Debug.println("DSUtil.reauthenticate: begin: ldc=" + format(ldc));
	if (authPassword == null) {
	    status = reauthenticate(ldc, frame, listeners, authDN);
	} else {
	    try {
		oldDN = ldc.getAuthenticationDN();
		oldPwd = ldc.getAuthenticationPassword();
		ldc.authenticate( 3, authDN, authPassword );
	    } catch ( LDAPException e ) {
		Debug.println("DSUtil.reauthenticate: " + authDN + " " +
			      authPassword + " " + e);
		if ( e.getLDAPResultCode() == e.INVALID_CREDENTIALS ||
		     e.getLDAPResultCode() == e.NO_SUCH_OBJECT ) {
		    status = reauthenticate (ldc, frame, listeners, authDN);
		} else {
		    status = false;
		}
	    }
	}

	if (status) { // connected and have correct credentials
            setDefaultReferralCredentials( ldc );
            Debug.println(9, "DSUtil.reauthenticate: ldc=" + format(ldc));

            /* notify listeners about the change */
            notifyListeners (listeners, oldDN, ldc.getAuthenticationDN(),
			     oldPwd, ldc.getAuthenticationPassword());
        }

	return status;
    }

    /**
     * Reauthenticase the console as a new user.  Keep trying until
     * the user either successfully reauthenticates or presses Cancel.
     *
     * @param ldc           - LDAPConnection already connected
     * @param authDN        - dn to suggest
     * 
     * @return true if the connection was successfully reauthenticated
     */
    static private boolean reauthenticate(LDAPConnection ldc, JFrame frame,
					  Vector listeners, String authDN) {
	int result = AUTH_FAILURE;
	boolean useSSL = ((ldc.getSocketFactory() != null) &&
			  (ldc.getSocketFactory() instanceof LDAPSSLSocketFactoryExt));
	do {
	    result = getNewAuthentication(frame, ldc,
					  ldc.getHost(),
					  ldc.getPort(),
					  authDN,
					  listeners, useSSL);
	    if (result == AUTH_FAILURE) {
		Debug.println( "DSUtil.reauthenticate: " +
			       "DSUtil.getNewAuthentication" +
			       " failed ldc=" + format(ldc) +
			       " isConnected=" +
			       ldc.isConnected());
	    } else if (result == AUTH_CANCEL){
		Debug.println(9,  "DSUtil.reauthenticate: " +
			      "DSUtil.getNewAuthentication" +
			      " canceled ldc=" + format(ldc) +
			      " isConnected=" +
			      ldc.isConnected());
	    } else {
		Debug.println( "DSUtil.reauthenticate: " +
			       "DSUtil.getNewAuthentication" +
			       " okay ldc=" + format(ldc) +
			       " isConnected=" +
			       ldc.isConnected());
	    }
	} while (result == AUTH_FAILURE);

	return (result == AUTH_SUCCESS);
    }


    /**
     * Returns the name of the server instance corresponding to the server connection.
     *
     * @param consoleInfo, information of the config server     
     * @param info serverInfo, information for the current server
     *
     * @return the name of the instance
     */
	static public String getInstanceName(ConsoleInfo serverInfo) {		
		String instanceName = (String)serverInfo.get("ServerInstance");		

		/* If we can't get the name of the instance we use the pair <server_name>:<port_number> */
		if ((instanceName == null) ||
			(instanceName.length() <= 0)) {
			LDAPConnection ldc = serverInfo.getLDAPConnection();
			String[] args = {ldc.getHost(), Integer.toString(ldc.getPort())};
			instanceName = _resource.getString("general",
											   "serveratport-label",
											   args);
		} else {
			/* The instanceName variable contains now 'slapd-<instance Name>' */
			int index = instanceName.lastIndexOf("slapd");
			if (index >= 0) {
				/* The 6 comes from the length of "slapd-" */
				instanceName = instanceName.substring(index+6);
			} else {
				LDAPConnection ldc = serverInfo.getLDAPConnection();
				String[] args = {ldc.getHost(), Integer.toString(ldc.getPort())};
				instanceName = DSUtil._resource.getString("general",
														  "serveratport-label",
														  args);
			}
		}
		return instanceName;
	}

    /**
     * Formats ldap connection for printing
     *
     * @params ldc - LDAPConnection to format
     *
     * @return string containing formated connection
     */
    static public String format(LDAPConnection ldc) {
	return "{host=" + ldc.getHost() + "} {port=" + ldc.getPort() +
	    "} {authdn=" + ldc.getAuthenticationDN() + "}";
    }

    /**
     * Get new authentication credentials from a dialog, reauthenticate
     * the connection in _serverInfo.
     *
     * @param frame Frame to display dialog on; may NOT be null.
     * @param ldc LDAPConnection, not necessarily connected.
     * @param host Host name
     * @param port Port number
     * @param authName DN to suggest for authentication
     * @param listeners Objects wishing notification; may be null.
     * @param useSSL If true, establish an SSL connection
     * @return <CODE>true</CODE> if the connection was reauthenticated.
     */
    static private int getNewAuthentication(
					    JFrame frame,
					    LDAPConnection ldc,
					    String host,
					    int port,
					    String authName,
					    Vector listeners,
					    boolean useSSL ) {
	if ( (authName == null) || authName.equals("") ) {
	    authName = _authName;
	}
	PasswordDialog dlg = new PasswordDialog( frame, authName );
	Debug.println(9, "DSUtil.getNewAuthentication(): before show ldc=" +
		      ldc + " current Thread=" + Thread.currentThread());
	dlg.setLocationRelativeTo(frame);
	dlg.show();
	Debug.println(9, "DSUtil.getNewAuthentication(): before " +
		      "dialogCleanup() ldc=" + ldc);
	dialogCleanup();
	Debug.println(9, "DSUtil.getNewAuthentication(): after " +
		      "dialogCleanup() ldc=" + ldc);
	if ( ldc == null ) {
	    ldc = makeLDAPConnection( useSSL );
	    if ( ldc == null ) {
		Debug.println("DSUtil.getNewAuthentication(): " +
			      "makeLDAPConnection failed");
		return AUTH_FAILURE;
	    }
	}

	String oldUid = ldc.getAuthenticationDN();
	String oldPwd = ldc.getAuthenticationPassword();

	Debug.println(9, "DSUtil.getNewAuthentication(): 1: newname=" +
		      dlg.getUsername() + " oldname=" + oldUid);

	if ( !dlg.isCancel() ) {
	    _authName = dlg.getUsername();
	    String password = dlg.getPassword();
	    boolean done = false;
	    int tries = 0;
	    while (!done && (tries < 2)) {
		try {
		    tries++;
		    ldc.authenticate( 3, _authName, password );
		    setDefaultReferralCredentials( ldc );
		    Debug.println(9,  "DSUtil.getNewAuthentication: new " +
				  "credentials are <" + _authName + "> <" +
				  password + ">" );
				
		    /* Notify any listeners */
		    notifyListeners (listeners, oldUid,
				     ldc.getAuthenticationDN(),
				     oldPwd, ldc.getAuthenticationPassword());
		    done = true;
		    return AUTH_SUCCESS;
		} catch ( LDAPException e ) {
		    // display a message
		    if ( e.getLDAPResultCode() == e.CONSTRAINT_VIOLATION ) {
			showErrorDialog( frame, "account-lockout", _authName );
			done = true;
		    } else if ( e.getLDAPResultCode() == e.NO_SUCH_OBJECT &&
				_configLdc != null ) {
			// perhaps the user specified the uid of the bind
			// entity e.g. admin, so try to find the DN
			_authName = LDAPUtil.getDNFromUID(
							  _configLdc.getHost(), _configLdc.getPort(),
							  LDAPUtil.getConfigurationRoot(),
							  _authName);
			if (_authName == null) {
			    showLDAPErrorDialog( frame, e, "109-title" );
			    done = true;
			}
		    } else {
			showLDAPErrorDialog( frame, e, "109-title" );
			done = true;
		    }
		    // reset the old uid/password, if necessary
		    try {
			if ( ldc.isConnected() )
			    ldc.authenticate( 3, oldUid, oldPwd );
		    } catch (LDAPException lde) {
			// do nothing
		    }
		}
	    }
	} else {
	    Debug.println(9, "DSUtil.getNewAuthentication: the " +
			  "user pressed the cancel button, authName=" +
			  authName);
	}

        if (dlg.isCancel())
	    return AUTH_CANCEL;

	return AUTH_FAILURE;
    }

    private static void notifyListeners (Vector listeners, String oldDN,
					 String newDN, String oldPwd,
					 String newPwd){
        if ( listeners != null ) {
	    for( int i = 0; i < listeners.size(); i++ ) {
		IAuthenticationChangeListener l = 
		    (IAuthenticationChangeListener)listeners.elementAt(
								       i );
		l.authenticationChanged( oldDN, newDN, oldPwd, newPwd );
	    }
	}
    }

    /**
     * Set up the LDAPConnection to automatically follow referrals,
     * using the same credentials as for the current connection
     *
     * @param ldc An open connection to a Directory Server
     */
    public static void setDefaultReferralCredentials( LDAPConnection ldc ) {
	if ( ldc != null ) {
	    try {
		ldc.setOption( ldc.REFERRALS, new Boolean(true) );
		ldc.setOption(
			      ldc.REFERRALS_REBIND_PROC,
			      new SimpleReferral( ldc.getAuthenticationDN(),
						  ldc.getAuthenticationPassword() ) );
	    } catch ( LDAPException e ) {
		Debug.println( "DSUtil.setDefaultReferralCredentials: " +
			       e );
	    }
	}
    }

    /**
     * Create an unconnected LDAPConnection object, with or without an
     * SSL factory
     *
     * @param useSSL If true, use an SSL socket factory
     * @return An LDAPConnection
     */
    public static LDAPConnection makeLDAPConnection( boolean useSSL ) {
	LDAPConnection ldc = null;
	if ( useSSL ) {
	    LDAPSocketFactory sfactory;
	    try {
	    	sfactory = UtilConsoleGlobals.getLDAPSSLSocketFactory();
	    }
	    catch(Throwable x) {
	    	sfactory = null;
	    	Debug.println(0, "DSUtil.makeLDAPConnection: " + x);
	    }
	    if (sfactory == null)
	    	Debug.println(0, "DSUtil.makeLDAPConnection: failed to get an SSL socket factory");
	    else
	    	ldc = new LDAPConnection( sfactory );
	} else {
	    ldc = new LDAPConnection();
	}

	if (Debug.isTraceTypeEnabled(Debug.TYPE_LDAP)) {
		try {
			ldc.setProperty(ldc.TRACE_PROPERTY, System.err);
		}
		catch (Exception ignore) {}
	}

	return ldc;
    }

    /**
     * Establish an LDAPConnection with default automatic referrals
     *
     * @param host Host to connect to
     * @param port Port on host to connect to
     * @param authDN Distinguished Name for authentication
     * @param authPassword Password for authentication
     * @param useSSL If true, establish an SSL connection
     * @return An LDAPConnection
     * @throws LDAPException on any failure
     */
    public static LDAPConnection getLDAPConnection( String host, int port,
						    String authDN,
						    String authPassword,
						    boolean useSSL )
	throws LDAPException {
	try {
	    LDAPConnection ldc = makeLDAPConnection( useSSL );
	    if ( ldc == null ) {
		return null;
	    }
            Debug.println ("DSUtil: made valid conn object");
	    ldc.connect(host, port);
            Debug.println ("DSUtil: connection established");

            if (authDN != null && !authDN.equals ("")){
                ldc.authenticate (3, authDN, authPassword );
                Debug.println ("DSUtil: auth done");
            }

            Debug.println ("DSUtil: passed connect and auth");
        
	    setDefaultReferralCredentials( ldc );

            Debug.println ("DSUtil: passed default referal");

	    return ldc;
	} catch ( LDAPException e ) {
	    Debug.println( "DSUtil.getLDAPConnection(" + host + ',' +
			   port + ',' + authDN + ',' + authPassword + "): " +
			   e );
	    throw e;
	}
    }

    /**
     * Establish an LDAPConnection with default automatic referrals
     *
     * @param host Host to connect to
     * @param port Port on host to connect to
     * @param authDN Distinguished Name for authentication
     * @param authPassword Password for authentication
     * @return An LDAPConnection
     * @throws LDAPException on any failure
     */
    public static LDAPConnection getLDAPConnection( String host, int port,
						    String authDN,
						    String authPassword )
	throws LDAPException {
	return getLDAPConnection( host, port, authDN, authPassword, false );
    }

    /**
     * Reconnects an LDAPConnection, if it is disconnected but was
     * previously connected.
     *
     * @return true if the connection is now connected
     */
    public static boolean reconnect( LDAPConnection ldc ) {
	boolean state = ((ldc != null) && (ldc.isConnected()));
	if ( (ldc != null) && !state ) {
	    String host = ldc.getHost();
	    int port = ldc.getPort();
	    String authDN = ldc.getAuthenticationDN();
	    String authPassword = ldc.getAuthenticationPassword();
	    if ( host != null ) {
		try {
		    ldc.connect( 3, host, port, authDN, authPassword );					
		    state = true;
		} catch ( LDAPException e ) {
		    Debug.println( "DSUtil.reconnect: (" + host + "," +
				   port + "," + authDN + "," +
				   authPassword + "), " + e );
		}
	    }
	}
	return state;
    }


    /**
     * Get latest activated frame from kingpin
     *
     */
    static JFrame getDefaultFrame() {
	return UtilConsoleGlobals.getActivatedFrame();
    }

    /**
     * Put up dialog showing LDAP error.
     *
     * @param frame Frame to display dialog on; may NOT be null.
     * @param e The LDAP exception containing the error info.
     * @param item Key to dialog title (from "general" section).
     */
    static public void showLDAPErrorDialog( Component frame,
					    LDAPException e, String item ) {
	// display a message
	String extendedMessage = e.getLDAPErrorMessage();
	String matchedDN = e.getMatchedDN();
	String msg;
	if ( (extendedMessage != null) && (extendedMessage.length() > 0) ) {
	    if ( (matchedDN != null) && (matchedDN.length() > 0) ) {
			String[] args = { e.errorCodeToString(), extendedMessage,
							  matchedDN };
			msg = _resource.getString( "general",
									   "ldap-error-dn-msg", args );
	    } else {
			String[] args = { e.errorCodeToString(), extendedMessage };
			msg = _resource.getString( "general",
									   "ldap-error-msg", args );
	    }
	} else {
	    msg = e.errorCodeToString();
	}
	showErrorDialog( frame, msg, e, item );
    }

    /**
     * Put up dialog showing unknown error.
     *
     * @param frame Frame to display dialog on; may NOT be null.
     * @param e The exception containing the error info.
     * @param item Key to dialog title (from "general" section).
     */
    static public void showUnknownErrorDialog( Component frame,
					       Exception e, String item ) {
	showErrorDialog( frame, e.toString(), e, item );
    }

    /**
     * Put up dialog showing error.
     *
     * @param frame Frame to display dialog on; may NOT be null.
     * @param msg Error message; may be null.
     * @param e The exception containing the error info.
     * @param item Key to dialog title (from "general" section).
     */
    static private void showErrorDialog( Component frame, String msg,
					 Exception e, String item,
					 ResourceSet resource ) {
	frame = (frame != null) ? frame : getDefaultFrame();
	if ( (msg == null) || (msg.length() < 1) )
	    msg = e.toString();
	if ( resource == null ) {
	    resource = _resource;
	}
	String title = resource.getString( "general", item );
	if ( title == null )
	    title = item;
	JOptionPane.showMessageDialog( frame,
				       msg,
				       title,
				       JOptionPane.ERROR_MESSAGE,
				       getErrorIcon() );
	dialogCleanup();
    }

    /**
     * Put up dialog showing error.
     *
     * @param frame Frame to display dialog on; may NOT be null.
     * @param msg Error message; may be null.
     * @param e The exception containing the error info.
     * @param item Key to dialog title (from "general" section).
     */
    static private void showErrorDialog( Component frame, String msg,
					 Exception e, String item ) {
	showErrorDialog( frame, msg, e, item, null );
    }

    /**
     * Put up dialog showing error.
     *
     * @param frame   Frame to display dialog on; may NOT be null.
     * @param titleKey Title from resource file
     * @param msgKey  Message from resource file
     * @param args    arguments for error
     * @param section section in resource file containing err
     */
    static public void showErrorDialog( Component frame, String titleKey,
					String msgKey,
					String[] args, String section,
					ResourceSet resource ) {
	frame = (frame != null) ? frame : getDefaultFrame();
	if ( resource == null ) {
	    resource = _resource;
	}
	String msg = resource.getString(section, msgKey, args);
	if ( (msg == null) || (msg.length() < 1) ) {
	    msg = "Undefined resource: " + section + '-' + msgKey;
	}
	String title = resource.getString(section, titleKey);
	if ( (title == null) || (title.length() < 1) ) {
	    title = "Undefined resource: " + section + '-' + titleKey;
	}
	JOptionPane.showMessageDialog(frame,
				      msg,
				      title,
				      JOptionPane.ERROR_MESSAGE,
				      getErrorIcon() );
	dialogCleanup();
    }

    /**
     * Put up dialog showing error.
     *
     * @param frame   Frame to display dialog on; may NOT be null.
     * @param titleKey Title from resource file
     * @param msgKey  Message from resource file
     * @param args    arguments for error
     * @param section section in resource file containing err
     */
    static public void showErrorDialog( Component frame, String titleKey,
					String msgKey,
					String[] args, String section ) {
	showErrorDialog( frame, titleKey, msgKey, args, section, null );
    }

    /**
     * Put up dialog showing error.
     *
     * @param frame   Frame to display dialog on; may NOT be null.
     * @param titleKey Title from resource file
     * @param msgKey  Message from resource file
     * @param arg     argument for error
     * @param section section in resource file containing err
     */
    static public void showErrorDialog( Component frame, String titleKey,
					String msgKey,
					String arg, String section ) {
	String[] args = { arg };
	showErrorDialog( frame, titleKey, msgKey, args, section, null );
    }

    /**
     * Put up dialog showing error.
     *
     * @param frame   Frame to display dialog on; may NOT be null.
     * @param err     error number from resource file
     * @param args    arguments for error
     * @param section section in resource file containing err
     */
    static public void showErrorDialog( Component frame, String err,
					String[] args, String section,
					ResourceSet resource ) {
	showErrorDialog( frame, err + "-title", err + "-msg", args,
			 section, resource );
    }

    /**
     * Put up dialog showing error.
     *
     * @param frame   Frame to display dialog on; may NOT be null.
     * @param err     error number from resource file
     * @param args    arguments for error
     * @param section section in resource file containing err
     */
    static public void showErrorDialog( Component frame, String err,
					String[] args, String section ) {
	showErrorDialog( frame, err, args, section, null );
    }

    /**
     * Put up dialog showing error.  Error numbers will be used from
     *  the general section.
     *
     * @param frame   Frame to display dialog on; may NOT be null.
     * @param err     error number from resource file
     * @param args    arguments for error
     */
    static public void showErrorDialog( Component frame, String err,
					String[] args,
					ResourceSet resource ) {
	showErrorDialog( frame, err, args, "general", resource );
    }

    /**
     * Put up dialog showing error.  Error numbers will be used from
     *  the general section.
     *
     * @param frame   Frame to display dialog on; may NOT be null.
     * @param err     error number from resource file
     * @param args    arguments for error
     */
    static public void showErrorDialog( Component frame, String err,
					String[] args ) {
	showErrorDialog( frame, err, args, (ResourceSet)null );
    }

    /**
     * Put up dialog showing error.  Error numbers will be used from
     * the general section.
     *
     * @param frame   Frame to display dialog on; may NOT be null.
     * @param err     error number from resource file
     * @param arg     argument for error
     */
    static public void showErrorDialog( Component frame, String err,
					String arg, ResourceSet resource ) {
	String[] args = { arg };
	showErrorDialog( frame, err, args, "general", resource );
    }

    /**
     * Put up dialog showing error.  Error numbers will be used from
     * the general section.
     *
     * @param frame   Frame to display dialog on; may NOT be null.
     * @param err     error number from resource file
     * @param arg     argument for error
     */
    static public void showErrorDialog( Component frame, String err,
					String arg ) {
	showErrorDialog( frame, err, arg, (ResourceSet)null );
    }

    /**
     * Put up dialog showing error.  Errors will be used from
     * the specified section.
     *
     * @param frame   Frame to display dialog on; may NOT be null.
     * @param err     error number from resource file
     * @param arg     argument for error
     * @param section section in resource file containing err
     */
    static public void showErrorDialog( Component frame, String err,
					String arg, String section,
					ResourceSet resource) {
	String[] args = { arg };
	showErrorDialog(frame, err, args, section, resource);
    }

    /**
     * Put up dialog showing error.  Errors will be used from
     * the specified section.
     *
     * @param frame   Frame to display dialog on; may NOT be null.
     * @param err     error number from resource file
     * @param arg     argument for error
     * @param section section in resource file containing err
     */
    static public void showErrorDialog( Component frame, String err,
					String arg, String section ) {
	showErrorDialog( frame, err, arg, section, (ResourceSet)null );
    }

    /**
     * Put up dialog showing YES/NO and asking for confirmation.
     *
     * @param frame   Frame to display dialog on; may NOT be null.
     * @param err     information number from resource file
     * @param args    arguments for information
     * @param section section in resource file containing err
     */
    static public int showConfirmationDialog( Component frame, String err,
					      String[] args,
					      String section ) {
	return showConfirmationDialog( frame, err, args, section, null );
    }

    /**
     * Put up dialog showing YES/NO and asking for confirmation.
     *
     * @param frame   Frame to display dialog on; may NOT be null.
     * @param err     information number from resource file
     * @param arg     argument for information
     * @param section section in resource file containing err
     */
    static public int showConfirmationDialog( Component frame, String err,
					      String arg,
					      String section ) {
	String[] args = { arg };
	return showConfirmationDialog( frame, err, args, section, null );
    }

    /**
     * Put up dialog showing YES/NO and asking for confirmation.
     *
     * @param frame   Frame to display dialog on; may NOT be null.
     * @param err     information number from resource file
     * @param arg     argument for information
     * @param section section in resource file containing err
     * @param resource Resource bundle
     */
    static public int showConfirmationDialog( Component frame, String err,
					      String[] args,
					      String section,
					      ResourceSet resource ) {
	frame = (frame != null) ? frame : getDefaultFrame();
	ResourceSet res = (resource != null) ? resource : _resource;
	String msg = res.getString(section, err + "-msg", args);
	if ( (msg == null) || (msg.length() < 1) ) {
	    msg = "Undefined resource: " + section + '-' + err;
	}
	String title = res.getString(section, err + "-title");
	if ( (title == null) || (title.length() < 1) ) {
	    title = "Undefined resource: " + section + '-' + err + "-title";
	}
	// We show the warning icon instead of the Information Icon for this.  See bug 470710.
	int answer =
	    JOptionPane.showConfirmDialog(frame,
					  msg,
					  title,
					  JOptionPane.YES_NO_OPTION,
					  0,
					  getWarningIcon() );  
	dialogCleanup();
	return answer;
    }

    /**
     * Put up dialog showing YES/NO and asking for confirmation.
     *
     * @param frame   Frame to display dialog on; may NOT be null.
     * @param err     information number from resource file
     * @param arg     argument for information
     * @param section section in resource file containing err
     * @param resource Resource bundle
     */
    static public int showConfirmationDialog( Component frame, String err,
					      String arg,
					      String section,
					      ResourceSet resource ) {
	String[] args = { arg };
	return showConfirmationDialog( frame, err, args, section, resource );
    }

    /**
     * Put up dialog showing information.
     *
     * @param frame   Frame to display dialog on; may NOT be null.
     * @param err     information number from resource file
     * @param args    arguments for information
     * @param section section in resource file containing err
     */
    static public void showInformationDialog( Component frame, String err,
					      String[] args,
					      String section ) {
		showInformationDialog(frame,
							  err,
							  args,
							  section,
							  _resource);
    }

    /**
     * Put up dialog showing information.
     *
     * @param frame   Frame to display dialog on; may NOT be null.
     * @param err     information number from resource file
     * @param args    arguments for information
     * @param section section in resource file containing err
	 * @param resource the ResourceSet used to get the message and title to display
     */
    static public void showInformationDialog( Component frame,
											  String err,
											  String[] args,
											  String section,
											  ResourceSet resource
											  ) {
		frame = (frame != null) ? frame : getDefaultFrame();
		if ( resource == null ) {
			resource = _resource;
		}
		String msg = resource.getString(section, err + "-msg", args);
		if ( (msg == null) || (msg.length() < 1) ) {
			msg = "Undefined resource: " + section + '-' + err;
		}
		String title = resource.getString(section, err + "-title");
		if ( (title == null) || (title.length() < 1) ) {
			title = "Undefined resource: " + section + '-' + err + "-title";
		}
		JOptionPane.showMessageDialog( frame,
									   msg,
									   title,
									   JOptionPane.INFORMATION_MESSAGE,
									   getInformationIcon() );
		dialogCleanup();
    }
	
    /**
     * Put up dialog showing information.
     *
     * @param frame   Frame to display dialog on; may NOT be null.
     * @param err     information number from resource file
     * @param arg     argument for information
     * @param section section in resource file containing err
     */
    static public void showInformationDialog( Component frame, String err,
					      String arg, String section ) {
	String[] args = { arg };
	showInformationDialog(frame, err, args, section);
    }

    /**
     * Put up dialog showing information.  Information numbers will be used
     * from the general section.
     *
     * @param frame   Frame to display dialog on; may NOT be null.
     * @param err     information number from resource file
     * @param args    arguments for information
     */
    static public void showInformationDialog( Component frame, String err,
					      String[] args ) {
	showInformationDialog(frame, err, args, "general");
    }

    /**
     * Put up dialog showing information.  Information numbers will be used
     * from the general section.
     *
     * @param frame   Frame to display dialog on; may NOT be null.
     * @param err     information number from resource file
     * @param arg     argument for information
     */
    static public void showInformationDialog( Component frame, String err,
					      String arg ) {
	String[] args = { arg };
	showInformationDialog(frame, err, args, "general");
    }

    /**
     * Put up dialog indicating insufficient permission.
     *
     * @param frame Frame to display dialog on; may NOT be null.
     * @param dn Authentication DN
     */
    static public void showPermissionDialog( Component frame,
					     String dn ) {
	frame = (frame != null) ? frame : getDefaultFrame();
	JOptionPane.showMessageDialog( frame,
				       _resource.getString(
							   "authenticate",
							   "101-msg", dn ),
				       _resource.getString(
							   "authenticate",
							   "101-title"),
				       JOptionPane.ERROR_MESSAGE,
				       getErrorIcon() );
	dialogCleanup();
    }

    /**
     * Put up dialog indicating insufficient permission.
     *
     * @param frame Frame to display dialog on; may NOT be null.
     * @param ldc Connection that suffered the problem.
     */
    static public void showPermissionDialog( Component frame,
					     LDAPConnection ldc ) {
	showPermissionDialog( frame, ldc.getAuthenticationDN() );
    }

    static private Icon getInformationIcon() {
	if ( _infoIcon == null )
	    _infoIcon = DSUtil.getPackageImage( "messagel.gif" );
	return _infoIcon;
    }

    static private Icon getWarningIcon() {
	if ( _warningIcon == null )
	    _warningIcon = DSUtil.getPackageImage( "alertl.gif" );
	return _warningIcon;
    }

    static private Icon getErrorIcon() {
	if ( _errorIcon == null )
	    _errorIcon = DSUtil.getPackageImage( "error.gif" );
	return _errorIcon;
    }

    /**
     * Temporary workaround for jdk bug. On closing a dialog, often other
     * windows are incorrectly repainted, or not at all. Call this method
     * after "dispose()". For now, it does a sleep(200); when the jdk bug
     * is fixed, it will do nothing.
     */
    static public void dialogCleanup() {
	try {
	    Thread.sleep(200);
	    Thread.yield();
	} catch ( Exception e ) {
	}
    }

    /**
     * Get the schema of the Directory instance
     *
     * @param serverInfo Hashtable containing server information.
     * @return A reference to a schema object.
     */
    static public LDAPSchema getSchema( ConsoleInfo serverInfo ) {	
	LDAPSchema schema = null;
	Object o;
	synchronized( serverInfo ) {
	    o = serverInfo.get( "Schema" );
	}
	if ( o != null ) {
	    schema = (LDAPSchema)o;
	} else {
	    LDAPConnection ldc = serverInfo.getLDAPConnection();
	    if ( reconnect( ldc ) ) {
		try {
		    /* Get the schema from the Directory */		
		    schema = new LDAPSchema();
		    schema.fetchSchema( ldc );
		} catch ( LDAPException e ) {		
		    schema = null;
		}
	    }
	    if ( schema != null ) {
		synchronized( serverInfo ) {
		    serverInfo.put( "Schema", schema );
		}
	    }
	}
	return schema;
    }

    /**
     * Sets a reference to the schema of the Directory instance.
     *
     * @param serverInfo Hashtable containing server information.
     * @param schema A reference to a schema object.
     */
    static public void setSchema( ConsoleInfo serverInfo,
				  LDAPSchema schema ) {
	synchronized( serverInfo ) {
	    if ( schema != null ) {
		Debug.println(8, "DSUtil.setSchema: schema was set");
		serverInfo.put( "Schema", schema );
	    } else {
		Debug.println(8, "DSUtil.setSchema: schema was removed");
		serverInfo.remove( "Schema" );
	    }
	}
    }

    static String getImageDir() {
	return _sImageDir;
    }

    static String getSharedImageDir() {
	return _sGeneralImageDir;
    }

    private String getPackagePath() {
        String packageName = getClass().getName();
        int lastDot = packageName.lastIndexOf( '.' );
        packageName = packageName.substring( 0, lastDot );
        packageName = packageName.replace( '.', '/' );
	return packageName;
    }

    public static RemoteImage getPackageImage( String name ) {
	RemoteImage i = (RemoteImage)_cPackageImages.get( name );
	if ( i != null )
	    return i;
        i = getSystemImage( getImageDir() + "/" + name );
	if ( i != null )
	    _cPackageImages.put( name, i );
	return i;
    }

    static RemoteImage getSharedImage( String name ) {
	RemoteImage i = (RemoteImage)_cSharedImages.get( name );
	if ( i != null )
	    return i;
        i = getSystemImage( getSharedImageDir() + "/" + name );
	if ( i != null )
	    _cSharedImages.put( name, i );
	return i;
    }

	/* Returns the inactivated icon version of the icon identified by name */
	public static RemoteImage getInactivatedPackageImage( String name ) {
		String inactivatedName = name+"INACTIVATED";
		RemoteImage inactivatedImage = (RemoteImage)_cPackageImages.get( inactivatedName );
		if ( inactivatedImage != null ) {
			return inactivatedImage;
		}
        RemoteImage image = getSystemImage( getImageDir() + "/" + name );
		if ( image != null ) {
			inactivatedImage = inactivatedIcon(image);
			if (inactivatedImage != null) {
				_cPackageImages.put(inactivatedName, inactivatedImage);
				return inactivatedImage;
			}
		}
		return null;
    }


	/* Returns the inactivated icon version of the icon identified by name.  Used for images representing
	 an inactivated role */
	public static RemoteImage getInactivatedRolePackageImage( String name ) {
		String inactivatedName = name+"INACTIVATED-ROLE";
		RemoteImage inactivatedImage = (RemoteImage)_cPackageImages.get( inactivatedName );
		if ( inactivatedImage != null ) {
			return inactivatedImage;
		}
        RemoteImage image = getSystemImage( getImageDir() + "/" + name );
		if ( image != null ) {
			inactivatedImage = inactivatedRoleIcon(image);
			if (inactivatedImage != null) {
				_cPackageImages.put(inactivatedName, inactivatedImage);
				return inactivatedImage;
			}
		}
		return null;
    }

    /**
     * This is not necessary any more, now that RemoteImage implements
     * the code we used to have inside this method.
     */
    static RemoteImage getSystemImage( String imagePath ) {
	return new RemoteImage( imagePath );
    }

    /**
     * Debugging utility method: dump the contents of a ConsoleInfo.
     *
     * @param info Any ConsoleInfo instance.
     */
    public static void dumpConsoleInfo( ConsoleInfo info ) {
	Enumeration e = info.keys();
	while( e.hasMoreElements() ) {
	    String s = (String)e.nextElement();
	    Debug.println( "  " + s + ": " + info.get( s ) );
	}
    }

    /**
     * Add the required and optional attributes for an objectclass to
     * a Hashtable
     *
     * @param schema Schema defining the objectclasses and attributes
     * @param oclass Schema definition for this objectclass
     * @param attributes Hashtable of attributes
     */
    private static void addAttributesToTable( LDAPSchema schema,
					      LDAPObjectClassSchema oclass,
					      Hashtable attributes ) {
	if (( oclass != null ) && (schema != null) && (attributes != null)) {				
	    DSSchemaHelper.allAttributes(oclass, schema, attributes);			
	}
    }

    /**
     * Add the required and optional attributes for an objectclass to
     * a Hashtable. Superiors are recursed, so the table will contain
     * all aggregated attributes.
     *
     * @param schema Schema defining the objectclasses and attributes
     * @param oName Objectclass name
     * @param attributes Hashtable of attributes
     */
    private static void addAttributesToTable( LDAPSchema schema,
					      String oName,
					      Hashtable attributes ) {
	if ( oName == null ) {
	    return;
	}
	LDAPObjectClassSchema oclass;
	oclass = schema.getObjectClass( oName );
	addAttributesToTable( schema, oclass, attributes );		
    }

    /**
     * Get the required and optional attributes for the objectclasses
     * of a valid entry
     *
     * @param schema Schema defining the objectclasses and attributes
     * @param entry A valid LDAP entry, containing objectclasses
     * @return Hashtable of attributes
     */
    public static Hashtable getAllAttributeList( LDAPSchema schema,
                                                 LDAPEntry entry ) {
	/* Get the object classes */
	Hashtable attributes = new Hashtable();
	LDAPAttribute a = entry.getAttribute( "objectclass" );
	if ( a == null ) {
	    /* a should never be null, but there is a bug in
	       "cn=monitor,cn=ldbm" */
	    return attributes;
	}
	Enumeration en = a.getStringValues();
	while ( en.hasMoreElements() ) {
	    String o = (String)en.nextElement();
	    addAttributesToTable( schema, o, attributes );
	}
	return attributes;
    }

    /**
     * Get the required and optional attributes for the objectclasses
     * passed in
     *
     * @param schema Schema defining the objectclasses and attributes
     * @param classes A Vector of objectclass names
     * @return Hashtable of attributes
     */
    public static Hashtable getAllAttributeList( LDAPSchema schema,
                                                 Vector classes ) {
	/* Get the object classes */
	Hashtable attributes = new Hashtable();
	if ( classes != null ) {
	    Enumeration en = classes.elements();
	    while ( en.hasMoreElements() ) {
		String o = (String)en.nextElement();
		addAttributesToTable( schema, o, attributes );
	    }
	}
	return attributes;
    }

    /* this function is useful to obtain the value of a single value 
     * attribute. Returns first value of a given attribute or "" if
     * the attribute is not found
     * @param entry The LDAP entry DN
     * @param attr The attribute name
     */

    public static String getAttrValue( LDAPEntry entry, String attrName ) {
	if ( entry != null ) {
	    LDAPAttribute attr = entry.getAttribute( attrName );
	    if ( attr != null ) {
		Enumeration e = attr.getStringValues();
		if ( e.hasMoreElements() ) {
		    return (String)e.nextElement();
		}
	    }
	}
	return "";
    }


    /* this function is useful to obtain all values of a multivalued
     * attribute. Returns value array of a given attribute or null if
     * the attribute is not found
     * @param entry The LDAP entry DN
     * @param attr The attribute name
     */
    public static String[] getAttrValues( LDAPEntry entry, String attrName ) {
	if ( entry != null ) {
	    LDAPAttribute attr = entry.getAttribute( attrName );
	    if ( attr != null ) {
		return attr.getStringValueArray();
	    }
	}
	return ( null );
    }


    public static String[] getAttrStringValueArray(
						   LDAPConnection ldc,
						   String dnBase,
						   String AttrName ) {
	return( getAttrStringValueArray( ldc, 
					 dnBase,
					 "objectclass=*", 
					 ldc.SCOPE_BASE, 
					 AttrName)
		);
    }

    /**
     * Usefull to obtain all values of a given attribut present in
     * the server. Returns array of string or null
     * default scope = sub
     */	
    public static String[] getAttrStringValueArray(
						   LDAPConnection ldc,
						   String dnBase,
						   String Filter,
						   String AttrName ) {
	return( getAttrStringValueArray( ldc,
					 dnBase,
					 Filter,
					 ldc.SCOPE_SUB,
					 AttrName)
		);
    }

    /**
     * Usefull to obtain all values of a given attribut present in
     * the server. Returns array of string or null
     * default scope = sub + filter  = "objectclass=*"
     */	
    public static String[] getAttrStringValueArray(
						   LDAPConnection ldc,
						   String dnBase,
						   String Filter,
						   int Scope,
						   String AttrName ) {
	Vector v = new Vector();
	String[] attrs = { AttrName };
	//		Debug.println("DSUtil.getAttrStringValueArray() ###############");
	//		Debug.println("        base = " + dnBase );
	//		Debug.println("        Filter = " + Filter );
	//		Debug.println("        Scope = " + Scope );
	//		Debug.println("        AttrName = " + AttrName );

	try {
	    LDAPSearchResults res =
		ldc.search( dnBase,
			    Scope,
			    Filter,
			    attrs,
			    false );
	    while ( res.hasMoreElements() ) {
		LDAPEntry entry = (LDAPEntry)res.nextElement();
		//Debug.println("               " + entry.toString());
		if( AttrName.compareToIgnoreCase("dn") == 0 ) {
		    v.addElement( entry.getDN());
		} else {
		    LDAPAttribute attr = entry.getAttribute( AttrName );
		    if ( attr != null ) {
			Enumeration en = attr.getStringValues();
			while ( en.hasMoreElements() ) {
			    v.addElement( (String) en.nextElement() );
			}
		    } 
		}
	    }
	    if( v.size() == 0) {
		return( null );
	    } else {
		String[] resL = new String[ v.size() ];
		v.toArray( resL );
		return( resL );
	    }
	} catch( LDAPException e) {
	    return( null );
	}
    }
													


    /**
     * Reports if an attribute, when changed on the server,
     * requires a server restart to take effect.
     *
     * @param entry The LDAP entry DN
     & @param attr The attribute name
    */
    static public boolean requiresRestart( String entry, String attr ) {
	String key = (entry+":"+attr).toLowerCase();
	if ( _cRequiresRestart == null ) {
	    Debug.println( "DSUtil.requiresRestart: null hashtable" );
	    return false;
	}
	return( _cRequiresRestart.get( key ) != null );
    }

    /**
     * Reports if any attribute in a LDAPModificationSet 
     * requires a server restart to take effect.
     *
     * @param entry The LDAP entry DN
     & @param mods The LDAPModificationSet
    */
    static public boolean requiresRestart( String entry, LDAPModificationSet mods ) {
		LDAPModification mod;
		String key;
		int op;
		int j;
		boolean requiresRestart = false;

		if ( _cRequiresRestart == null ) {
		    Debug.println( "DSUtil.requiresRestart: null hashtable" );
		    return false;
		}
		for ( j = 0; j < mods.size() && !requiresRestart; j++ ) {
			mod = mods.elementAt (j);
			op = mod.getOp ();
			if (op == LDAPModification.ADD || op == LDAPModification.REPLACE) {
				key = (entry+":"+mod.getAttribute().getName()).toLowerCase();
				requiresRestart = ( _cRequiresRestart.get( key ) != null );
			}
		}
		return requiresRestart;
    }

    /**
     * Notes that an attribute, when changed on the server,
     * requires a server restart to take effect.
     *
     * @param entry The LDAP entry DN
     & @param attr The attribute name
    */
    static public void addRequiresRestart( String entry, String attr ) {
	String key = (entry+":"+attr).toLowerCase();
	if ( _cRequiresRestart == null ) {
	    Debug.println( "DSUtil.addRequiresRestart: null hashtable" );
	    return;
	}
	_cRequiresRestart.put( key, key );
    }

    /**
     * Get a table of all attributes which, when changed on the server,
     * require a server restart to take effect.
     *
     * @param ldc An open connection to the Directory server in question.
     */
    static private Hashtable getRequiresRestartTable( LDAPConnection ldc ) {
	Hashtable cRequiresRestart = new Hashtable();
	if ( reconnect( ldc ) ) {
	    try {
		String[] attrs = {"nsslapd-requiresrestart"};
		LDAPEntry entry = ldc.read( "cn=config", attrs );
		LDAPAttribute attr = null;
		if ( entry != null )
		    attr = entry.getAttribute( attrs[0] );
		if ( attr != null ) {
		    Enumeration en = attr.getStringValues();
		    while( en.hasMoreElements() ) {
			String val =
			    ((String)en.nextElement()).toLowerCase();
			cRequiresRestart.put( val, val );
			//						Debug.println( "DSUtil.getRequiresRestartTable: " +
			//									   "adding " + val );
		    }
		}
	    } catch ( LDAPException e ) {
		Debug.println( "DSUtil.getRequiresRestartTable: " + e );
	    }
	}
	return cRequiresRestart;
    }
	
    /**
     * Create a 32 bits CRC from the given string.
     * @param s String to calculate hash code for.
     */
    public static int getCRC32( String s ) {
	return getCRC32( s.getBytes() );
    }

    public static int getCRC32( byte[] barray ) {
	CRC32 crcVal = new CRC32();
	crcVal.update(barray);
	return (int)crcVal.getValue();
    }

    public static int getCRC32( LDAPAttribute attr ) {
	Enumeration e = attr.getByteValues();
	byte[] b = null;
	int total = 0;
	while( e.hasMoreElements() ) {
	    b = (byte[])e.nextElement();
	    if ( attr.size() == 1 )
		return getCRC32( b );
	    total += b.length;
	}
	byte[] all = new byte[total];
	e = attr.getByteValues();
	int pos = 0;
	while( e.hasMoreElements() ) {
	    b = (byte[])e.nextElement();
	    System.arraycopy( b, 0, all, pos, b.length );
	    pos += b.length;
	}
	return getCRC32( all );
    }


    /**
     * Trim and sort an array of strings
     */
    public static void trimAndBubbleSort(String[] str, boolean isAscii) {
	for (int i = 0; i < str.length; i++)
	    str[i] = str[i].trim();
	bubbleSort( str, isAscii );
    }
	
	
    /**
     * Sorts the array of strings using bubble sort.
     * @param str The array of string being sorted. The str parameter contains
     * the sorted result.
     * @param isAscii If false, true Unicode collation will be used.
     */
    public static void bubbleSort(String[] str, boolean isAscii) {
	if ( isAscii ) {
	    bubbleSortAscii( str );
	} else {
	    bubbleSortCollated( str );
	}
    }

    /**
     * Sorts the array of strings using bubble sort.
     * @param str The array of string being sorted. The str parameter contains
     * the sorted result. Comparison will be done using true Unicode collation.
     */
    public static void bubbleSort(String[] str) {
	bubbleSortCollated( str );
    }

    /**
     * Sorts the array of strings using bubble sort.
     * @param str The array of string being sorted. The str parameter contains
     * the sorted result.
     */
    private static void bubbleSortCollated(String[] str) {
	Collator collator = Collator.getInstance(); 
	for (int i = 0; i < str.length-1; i++) {
	    for (int j = i+1; j < str.length; j++) {
                if( collator.compare(str[i], str[j]) > 0 ) {
		    String t = str[i];
		    str[i] = str[j];
		    str[j] = t;
		}
	    }
	}
    }

    /**
     * Sorts the array of strings using bubble sort.
     * @param str The array of string being sorted. The str parameter contains
     * the sorted result.
     */
    private static void bubbleSortAscii(String[] str) {
	for (int i = 0; i < str.length-1; i++) {
	    for (int j = i+1; j < str.length; j++) {
                if( str[i].compareTo(str[j]) > 0 ) {
		    String t = str[i];
		    str[i] = str[j];
		    str[j] = t;
		}
	    }
	}
    }

    public static String getRootDN() {
	return _authName;
    }

    public static void help( String token ) {
        Debug.println(9,  "DSUtil.help: " + token );
	Help.showContextHelp(HELP_DIRECTORY,token);
    }

	/**
	* Adjust value so sum of value, current time and 24 hours does
	* not exceed 2,147,483,647 in seconds
	* If proposed time is not in days, provide scalefactor
	*/
	public static int epochConstraint(int proposed, int scaleFactor) {
		proposed /= (scaleFactor > 0 ? scaleFactor : 1);
		int curDay =  (int)(new Date().getTime() / 86400000); // Days since 1-1-1970
		curDay += 1; // Add 1 day for safety
		int epochEnd = 24855; // Days between 1-1-1970 and 1-19-2038
		if ( ( proposed + curDay ) > epochEnd ) {
			return (epochEnd - curDay) * (scaleFactor > 0 ? scaleFactor : 1);
		}
		return proposed;
	}

    /**
     * Conver the date in LDAP format into java.util.Date object
     */
    public static Date getDateTime(String dbDate) {     
	String dbDateFormat = "yyyyMMddHHmmss";

	// Check if time zone included into date
	if ( (dbDate.length() == dbDateFormat.length() + 1) &&
	     dbDate.endsWith("Z") ) {
	    // 'z' symbol is used for time zone
	    dbDateFormat = dbDateFormat + "zzz";
	    dbDate = dbDate.substring(0,dbDate.length()-1) + "GMT.";
	}
		
	try {
	    SimpleDateFormat sdf = new SimpleDateFormat(dbDateFormat);
	    Date date = sdf.parse(dbDate,new ParsePosition(0));
	    return date;
	} catch (Exception e) {
	    Debug.println( "DSUtil.getDateTime: " + dbDate +
			   " does not match expected format " + dbDateFormat);
	    Debug.println(e.getMessage());
	    return null;
	}
    } 

    /**
     * Convert the date in LDAP format into localized string
     */
    public static String formatDateTime(String dbDate) {      
	try
	    {
		Date date = getDateTime(dbDate);
		if (date == null)
		    {
			return dbDate;
		    }
		DateFormat df =
		    DateFormat.getDateTimeInstance(DateFormat.SHORT,
						   DateFormat.SHORT);
		return df.format(date);
	    } catch (Exception e)
		{
		    Debug.println(e.getMessage());
		    return dbDate;
		}
    } 

    // Canonicalize a host name.  Throws an UnknownHostException
    // if the canonical host name cannot be determined.
    static public String canonicalHost(String host)
	throws UnknownHostException {
        if ((host == null) ||(host.equals("")))
            return "";
        String canonHost = null;
        String ipAddr;
	Debug.println(9, "canonicalHost: enter with "+host);
        canonHost = (String)_addressCache.get(host);
	if ( canonHost == null ) {
	    InetAddress addr = InetAddress.getByName(host);
	    if (addr != null) {
		InetAddress canonAddr =
		    InetAddress.getByName(addr.getHostAddress());
		if (canonAddr != null) {
		    canonHost = canonAddr.getHostName();
		}
	    }
	    if ( canonHost != null ) {
		_addressCache.put( host, canonHost );
		Debug.println(9, "canonicalHost: returning " + canonHost +
			      ", hostAddress = " + addr.getHostAddress());
	    }
        }
        return canonHost;
    }

    static public boolean requiresConfirmation( String item ) {
	/* Default to always requiring confirmation */
	boolean defaultValue = true;

	if ( _confirmationPreferences == null ) {
	    /* See if there is a personal preference set */
	    PreferenceManager pm =
		PreferenceManager.getPreferenceManager(Framework.IDENTIFIER,
						       Framework.VERSION);
	    _confirmationPreferences =
		pm.getPreferences(GlobalConstants.PREFERENCES_CONFIRM);
	}
	if ( _confirmationPreferences != null ) {
	    return _confirmationPreferences.getBoolean( item, defaultValue );
	}
	return defaultValue;
    }

    /**
     * Returns SSL port of the server to which console is attached
     * this is a temporary fix until console provides
     *  function to get secure port
     */
    static public int getSSLPort (ConsoleInfo serverInfo){
        LDAPConnection ldc = serverInfo.getLDAPConnection ();
        String dn= "cn=config";
        String attrs[] = {"nsslapd-secureport"};
        LDAPEntry entry;
        String value;
        int port;

        if ( ldc == null )
	    return -1;

        try {
            entry = ldc.read (dn, attrs);
        } catch (LDAPException e){
            return -1;
        }
        
        if (entry == null)
            return -1;

        value = DSUtil.getAttrValue (entry, attrs[0]);

        if (value.equals(""))
            return -1;

        try {
	    port = Integer.parseInt(value);
	} catch (NumberFormatException nfe) {
	    port = -1;
	}

        return port;
    }

    /**
     * Returns the server status: IServerObject.STATUS_STARTED or
     * IServerObject.STATUS_STOPPED.
     *
     * This method checks if the ldap connection in console info
     * is connected or not. If not, it tries to connect it. If
     * it fails, the method returns stopped.
     */
    static public int checkServerStatus (ConsoleInfo info){
	LDAPConnection ldc = info.getLDAPConnection();
	boolean state = false;
	Debug.println(7, "DSUtil.checkServerStatus: begin");
	synchronized (ldc) {
	    state = ( (ldc != null) && ldc.isConnected() );
	    Debug.println(9,  "DSUtil.checkServerStatus: ldc=" +
			  DSUtil.format(ldc) + " state=" + state);
	    if ( !state ) {
		/* See if we can connect */
		String host = info.getHost();
		int port = info.getPort();
		String authDN = info.getAuthenticationDN();
		String authPassword = info.getAuthenticationPassword();
		try {
		    ldc.connect( 3, host, port, authDN, authPassword );
		    DSUtil.setDefaultReferralCredentials( ldc );
		} catch ( LDAPException e ) {
		}
		state = ( (ldc != null) && ldc.isConnected() );
	    }
	}

	Debug.println(7, "DSUtil.checkServerStatus: end state = " + state);
	return state ? IServerObject.STATUS_STARTED : IServerObject.STATUS_STOPPED;
    }

    // returns true if dn1 completely contains dn2 or false otherwise
    // contains is defined as dn2 is a parent of or is equal to dn1
    // NOTE that this method is required until the ldapjdk
    // DN.contains method is fixed
    static public boolean isSubtreeOf(DN dn1, DN dn2) {
	boolean ret = false;
	Vector rdns1 = dn1.getRDNs();
	Vector rdns2 = dn2.getRDNs();
	// if the size is not greater than or equal to, the
	// dn cannot be a parent or the same
	Debug.println(9, "DSUtil.isSubtreeOf: dn1: " + dn1 +
		      " dn2: " + dn2 + " size1=" + rdns1.size() +
		      " size2=" + rdns2.size());
	if (rdns1.size() >= rdns2.size()) {
	    // compare each rdn in turn
	    ret = true;
	    for (int i1 = rdns1.size() - 1, i2 = rdns2.size() - 1;
		 ret && i1 >= 0 && i2 >= 0; --i1, --i2) {
		RDN rdn1 = (RDN)rdns1.elementAt(i1);
		RDN rdn2 = (RDN)rdns2.elementAt(i2);
		ret = rdn1.equals(rdn2);
		Debug.println(9, "DSUtil.isSubtreeOf: " + rdn1 + " " +
			      (ret ? "equals" : "does not equal") +
			      " " + rdn2);
	    }
	}
	return ret;
    }

    static public boolean deleteTree(String entryDN, LDAPConnection ldc) {
	return deleteTree(entryDN, ldc, true);
    }

    /**
     * This is a very simple-minded implementation of a recursive tree
     * deletion.  It does not scale well at all for large numbers of entries
     * or very deep trees.  It is mostly useful for the cn=config entries
     * where there are usually small trees.
     */
    static public boolean deleteTree(String entryDN, LDAPConnection ldc,
				     boolean followReferrals) {
	return deleteTree(entryDN, ldc, followReferrals, null);
    }
	
	static public boolean deleteTree(String entryDN, 
									 LDAPConnection ldc,
									 boolean followReferrals,
									 GenericProgressDialog dlg) {
	Debug.println(8, "DSUtil.deleteTree:  " + entryDN);		
	boolean ret = true;
	if (ldc == null || entryDN == null)
	    return false;

	LDAPSearchConstraints cons =
	    (LDAPSearchConstraints)ldc.getSearchConstraints().clone();
	if ( !followReferrals ) {
	    cons.setServerControls(
				   new LDAPControl(LDAPControl.MANAGEDSAIT, true, null));
	}		

	// delete all of the children of this entry, if any
	LDAPSearchResults search_results = null;
	String[] attrs = { "dn" };
	cons.setMaxResults( 0 );

	try {
	    search_results = ldc.search(entryDN,
					LDAPConnection.SCOPE_ONE,
					"(|(objectClass=*)(objectclass=ldapsubentry))",
					attrs,
					false,
					cons );
	} catch (LDAPException ldex) {
	    Debug.println("DSUtil.deleteTree: could not search entry " +
			  entryDN + ": " + ldex);
	    if (search_results != null) {
		try {ldc.abandon(search_results);}
		catch (LDAPException ldex3) {} // do nothing
	    }
	    return false;
	}

	// copy the search results to a vector to conserve resources
	Stack resstack = new Stack();
	while ( search_results != null &&
		search_results.hasMoreElements() ) {
	    resstack.push(search_results.nextElement());
	}
	if (search_results != null) {
	    try {ldc.abandon(search_results);}
	    catch (LDAPException ldex3) {} // do nothing
	}

	/* Delete each child found, recursively */
	while (ret && resstack != null && !resstack.empty()) {
	    /* Get the next child */
	    String dn = ((LDAPEntry)resstack.pop()).getDN();
	    ret = deleteTree(dn, ldc, followReferrals, dlg);
	}

	// all child nodes were not deleted, more detailed error messages
	// will have come from the recursive calls
	if (!ret) {
	    Debug.println("DSUtil.deleteTree: could not delete children of " +
			  entryDN);
	    return ret;
	}

	// And so this container node
	Debug.println("Deleting entry " + entryDN);
	if (dlg != null) {
	    dlg.setTextInLabel(_resource.getString("general",
						   "delete-entry-title", 
						   abreviateString(entryDN, 30)));
	}

	try {
	    ldc.delete(entryDN);
	}  catch (LDAPException ldex2) {
	    Debug.println("DSUtil.deleteTree: could not delete entry " +
			  entryDN + ": " + ldex2);
	    return false;
	}

	ret = true; // if we got here, we completed successfully
	return ret;
    }

    /* For amusement */
    static private Vector _ttStrings = null;
    static private int _ttIndex = 0;

    public static String getTTString() {
        String tt =  "";
        if ( _ttIndex < _ttStrings.size() ) {
            tt =  (String)_ttStrings.elementAt( _ttIndex );
            _ttIndex++;
            if ( _ttIndex >= _ttStrings.size() ) {
                _ttIndex = 0;
	    }
	}
        return tt;
    }

    /* Read strings for Easter Egg */
    static {
	_ttStrings = new Vector();
        try {
	    String file = "ds.tt";
            InputStream is = DSUtil.class.getResourceAsStream( file );
	    if ( is == null ) {
		Debug.println( "DSUtil static: could not open " + file );
	    } else {
		_ttStrings = TT.read( is );
	    }
	} catch ( Exception e ) {
	    Debug.println( "DSUtil static: " + e );
            e.printStackTrace();
	}
    }

    private static Method isValidDN_meth = null;
    private static Method equalDNs_str_meth = null;
    private static Method equalDNs_dn_meth = null;
    private static Method unEscapeRDNVal_meth = null;
    private static Method unEscapeRDN_meth = null;
    private static Method unEscapeDN_meth = null;
    private static Method escapeDNVal_meth = null;
    private static Method DNUsesLDAPv2Quoting_meth = null;
    static {
    	try {
			isValidDN_meth = LDAPUtil.class.getMethod("isValidDN", String.class);
			equalDNs_str_meth = LDAPUtil.class.getMethod("equalDNs", String.class, String.class);
			equalDNs_dn_meth = LDAPUtil.class.getMethod("equalDNs", DN.class, DN.class);
			unEscapeRDNVal_meth = LDAPUtil.class.getMethod("unEscapeRDNVal", String.class);
			unEscapeRDN_meth = LDAPUtil.class.getMethod("unEscapeRDN", String.class);
			unEscapeDN_meth = LDAPUtil.class.getMethod("unEscapeDN", String.class);
			escapeDNVal_meth = LDAPUtil.class.getMethod("escapeDNVal", String.class);
			DNUsesLDAPv2Quoting_meth = LDAPUtil.class.getMethod("DNUsesLDAPv2Quoting", String.class);
		} catch (SecurityException e) {
			e.printStackTrace();
			throw e;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
    }

    /**
     * Check if the string is a valid dn 
     *
     */
    static public boolean isValidDN (String dn){
    	// attempt to invoke the method defined in LDAPUtil
    	// if not found, or some problem occurred, just fall through
    	// and use the default implementation of this method
    	if (isValidDN_meth != null) {
    		try {
				return ((Boolean)isValidDN_meth.invoke(null, dn)).booleanValue();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
    	}
        if (dn.equals (""))
            return true;

        if (!netscape.ldap.util.DN.isDN(dn))
            return false;

        int eq = dn.indexOf('=');

        return (eq > 0 && eq < dn.length () -1 );
    }

    static public boolean equalDNs(DN dn1, DN dn2) {
    	// attempt to invoke the method defined in LDAPUtil
    	// if not found, or some problem occurred, just fall through
    	// and use the default implementation of this method
    	if (equalDNs_dn_meth != null) {
    		try {
    			return ((Boolean)equalDNs_dn_meth.invoke(null, dn1, dn2)).booleanValue();
    		} catch (IllegalArgumentException e) {
    			e.printStackTrace();
    		} catch (IllegalAccessException e) {
    			e.printStackTrace();
    		} catch (InvocationTargetException e) {
    			e.printStackTrace();
    		}
    	}
    	boolean status = (dn1 == null || dn2 == null);
    	if (status) { // if at least one of the arguments is null
    		status = (dn1 == dn2); // true if both are null, false otherwise
    		return status; // short circuit
    	}

    	Vector thisRDNs = dn1.getRDNs();
    	Vector thatRDNs = dn2.getRDNs();
    	if (thisRDNs != null && thatRDNs != null &&
    			thisRDNs.size() == thatRDNs.size()) {
    		int ii;
    		for (ii = 0; ii < thisRDNs.size(); ++ii) {
    			RDN thisRDN = (RDN)thisRDNs.elementAt(ii);
    			RDN thatRDN = (RDN)thatRDNs.elementAt(ii);
    			if (!thisRDN.equals(thatRDN))
    				break;
    		}

    		// all RDNs were equal
    		if (ii == thisRDNs.size())
    			status = true;
    	}

    	return status;
    }

    static public boolean equalDNs(String dn1, String dn2) {
    	// attempt to invoke the method defined in LDAPUtil
    	// if not found, or some problem occurred, just fall through
    	// and use the default implementation of this method
    	if (equalDNs_str_meth != null) {
    		try {
    			return ((Boolean)equalDNs_str_meth.invoke(null, dn1, dn2)).booleanValue();
    		} catch (IllegalArgumentException e) {
    			e.printStackTrace();
    		} catch (IllegalAccessException e) {
    			e.printStackTrace();
    		} catch (InvocationTargetException e) {
    			e.printStackTrace();
    		}
    	}
    	boolean retVal = false;
    	if (isValidDN(dn1) && isValidDN(dn2)) {
    		retVal = equalDNs(new DN(dn1), new DN(dn2));
    	}
    	return retVal;
    }

    /**
     * Returns the RDN value after unescaping any escaped characters.
     * Can be a simple escape - a \ followed by any character -
     * this will just remove the \ and leave the character in the
     * result unescaped.
     * Can be a hex escape - a \ followed by two hex digits - the
     * \ will be removed and the two hex digits converted to a single
     * char in the string.
     * Note that this is different than netscape.ldap.LDAPDN#unEscapeRDN(java.lang.String
     * in that this function will handle hex escapes.
     * If the rdn value is bogus or otherwise cannot be parsed correctly, the original
     * rdn value will be returned, with escapes if it had them.
     * <P>
     *
     * @param rdnval the RDN value to unescape
     * @return the unescaped RDN value or the original RDN value if there were errors
     * @see netscape.ldap.LDAPDN#escapeRDN(java.lang.String)
     */
    public static String unEscapeRDNVal(String rdnval) {
    	// attempt to invoke the method defined in LDAPUtil
    	// if not found, or some problem occurred, just fall through
    	// and use the default implementation of this method
    	if (unEscapeRDNVal_meth != null) {
    		try {
				return (String)unEscapeRDNVal_meth.invoke(null, rdnval);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
    	}
        StringBuffer copy = new StringBuffer();
        CharacterIterator it = new StringCharacterIterator(rdnval); 
        for (char ch = it.first(); ch != CharacterIterator.DONE; ch = it.next()) {
            if (ch == '\\') {
                ch = it.next();
                if (ch == CharacterIterator.DONE) {
                    // bogus - escape at end of string
                    return rdnval;
                }
                int val1 = Character.digit(ch, 16);
                if ((val1 >= 0) && (val1 < 16)) {
                    val1 = val1 * 16;
                    ch = it.next();
                    if (ch == CharacterIterator.DONE) {
                        // bogus - escape followed by only 1 hex digit
                        return rdnval;
                    }
                    int val2 = Character.digit(ch, 16);
                    if ((val2 < 0) || (val2 > 15)) {
                        return rdnval;
                    }
                    // must be a two digit hex code if we got here
                    ch = (char)(val1 + val2);
                }
            }
            copy.append(ch);
        }
        return copy.toString();
    }
    
    /**
     * Returns the RDN after unescaping any escaped characters.
     * Can be a simple escape - a \ followed by any character -
     * this will just remove the \ and leave the character in the
     * result unescaped.
     * Can be a hex escape - a \ followed by two hex digits - the
     * \ will be removed and the two hex digits converted to a single
     * char in the string.
     * Note that this is different than netscape.ldap.LDAPDN#unEscapeRDN(java.lang.String
     * in that this function will handle hex escapes.
     * If the rdn is bogus or otherwise cannot be parsed correctly, the original
     * rdn value will be returned, with escapes if it had them.
     * <P>
     *
     * @param rdn the RDN to unescape
     * @return the unescaped RDN or the original RDN if there were errors
     * @see netscape.ldap.LDAPDN#escapeRDN(java.lang.String)
     */
    public static String unEscapeRDN(String rdn) {
    	// attempt to invoke the method defined in LDAPUtil
    	// if not found, or some problem occurred, just fall through
    	// and use the default implementation of this method
    	if (unEscapeRDN_meth != null) {
    		try {
				return (String)unEscapeRDN_meth.invoke(null, rdn);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
    	}
        RDN name = new RDN(rdn);
        String[] vals = name.getValues();
        if ( (vals == null) || (vals.length < 1) ) {
            return rdn;
        }
        String[] types = name.getTypes();

        StringBuffer rdnbuf = new StringBuffer();
        for (int ii = 0; ii < vals.length; ++ii) {
            if (rdnbuf.length() > 0) {
                rdnbuf.append("+");
            }
            rdnbuf.append(types[ii] + "=" + unEscapeRDNVal(vals[ii]));
        }

        return rdnbuf.toString();
    }

    /**
     * Returns the DN after unescaping any escaped characters.
     * Can be a simple escape - a \ followed by any character -
     * this will just remove the \ and leave the character in the
     * result unescaped.
     * Can be a hex escape - a \ followed by two hex digits - the
     * \ will be removed and the two hex digits converted to a single
     * char in the string.
     * If the dn is bogus or otherwise cannot be parsed correctly, the original
     * dn value will be returned, with escapes if it had them.
     * <P>
     *
     * @param dn the DN to unescape
     * @return the unescaped DN or the original DN if there were errors
     */
    public static String unEscapeDN(String dn) {
    	// attempt to invoke the method defined in LDAPUtil
    	// if not found, or some problem occurred, just fall through
    	// and use the default implementation of this method
    	if (unEscapeDN_meth != null) {
    		try {
				return (String)unEscapeDN_meth.invoke(null, dn);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
    	}
        if ((dn == null) || (dn.equals(""))) {
            return dn;
        }
        String[] rdns = LDAPDN.explodeDN(dn, false);
        if ((rdns == null) || (rdns.length < 1)) {
            return dn;
        }
        StringBuffer retdn = new StringBuffer();
        for (int ii = 0; ii < rdns.length; ++ii) {
            if (retdn.length() > 0) {
                retdn.append(",");
            }
            retdn.append(unEscapeRDN(rdns[ii]));
        }
        
        return retdn.toString();
    }

    public static boolean[] DN_ESCAPE_CHARS = null;
    static {
	// get max val of DN.ESCAPED_CHAR
	char maxval = 0;
	for (char ii = 0; ii < DN.ESCAPED_CHAR.length; ++ii) {
	    if (maxval < DN.ESCAPED_CHAR[ii]) {
		maxval = DN.ESCAPED_CHAR[ii];
	    }
	}
	// add the '='
	if (maxval < '=') {
	    maxval = '=';
	}
	// create an array large enough to hold spaces
	// for all values up to maxval
	DN_ESCAPE_CHARS = new boolean[maxval+1];
	// set default value to false
	for (char ii = 0; ii < (int)maxval; ++ii) {
	    DN_ESCAPE_CHARS[ii] = false;
	}
	// set escape char vals to true
	for (char ii = 0; ii < DN.ESCAPED_CHAR.length; ++ii) {
	    DN_ESCAPE_CHARS[DN.ESCAPED_CHAR[ii]] = true;
	}
	// add the equals sign
	DN_ESCAPE_CHARS['='] = true;
    }
    /**
     * Escape the given DN string value for use as an RDN value.  Uses
     * the \XX hex escapes.
     * @param dnval value to escape for use as an RDN value
     * @return the escaped string
     */
    public static String escapeDNVal(String dnval) {
    	// attempt to invoke the method defined in LDAPUtil
    	// if not found, or some problem occurred, just fall through
    	// and use the default implementation of this method
    	if (escapeDNVal_meth != null) {
    		try {
				return (String)escapeDNVal_meth.invoke(null, dnval);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
    	}
        StringBuffer copy = new StringBuffer();
        CharacterIterator it = new StringCharacterIterator(dnval); 
        for (char ch = it.first(); ch != CharacterIterator.DONE; ch = it.next()) {
            if ((ch > 0) && (ch < DN_ESCAPE_CHARS.length) && DN_ESCAPE_CHARS[ch]) {
        	copy.append('\\');
        	copy.append(Integer.toHexString((int)ch).toUpperCase());
            } else {
        	copy.append(ch);
            }
        }
        return copy.toString();
    }
    /**
     * This function was stolen from dsalib_dn.c.  It checks the string
     * for LDAPv2 style quoting e.g. o="foo, bar", c=US, a format which
     * is now deprecated.
     *
     * @param  dn  The DN to scan
     * @return true if the given string contains LDAPv2 style quoting
     */
    static public boolean DNUsesLDAPv2Quoting(String dn) {
    	// attempt to invoke the method defined in LDAPUtil
    	// if not found, or some problem occurred, just fall through
    	// and use the default implementation of this method
    	if (DNUsesLDAPv2Quoting_meth != null) {
    		try {
    			return ((Boolean)DNUsesLDAPv2Quoting_meth.invoke(null, dn)).booleanValue();
    		} catch (IllegalArgumentException e) {
    			e.printStackTrace();
    		} catch (IllegalAccessException e) {
    			e.printStackTrace();
    		} catch (InvocationTargetException e) {
    			e.printStackTrace();
    		}
    	}
    	char ESC = '\\';
    	char Q = '"';
    	boolean ret = false;

    	// check dn for a even number (incl. 0) of ESC followed by Q
    	if (dn == null)
    		return ret;

    	int p = dn.indexOf(Q);
    	if (p >= 0)
    	{
    		int nESC = 0;
    		for (--p; (p >= 0) && (dn.charAt(p) == ESC); --p)
    			++nESC;
    		// the quote is unescaped if it is preceeded by an even
    		// number of escape characters, including 0
    		ret = ((nESC % 2) == 0);
    	}

    	return ret;
    }
    
    static public void checkForLDAPv2Quoting(String dn, JFrame frame,
					     String label) {
	if (DNUsesLDAPv2Quoting(dn)) {
	    String[] args = { label, dn };
	    showInformationDialog(frame, "ldapv2quoting", args);
	}
    }

    static public String URLEncode(String s) {
	if (s == null || s.length() == 0) {
	    Debug.println("Error: DSUtil.URLEncode: invalid string: " +
			  s);
	    return s;
	}

	String enc = URLEncoder.encode(s);
	StringBuffer ret = new StringBuffer();
		
	for (int ii = 0; ii < enc.length(); ++ii) {
	    char c = enc.charAt(ii);
	    if (c == '+') {
		ret.append("%20");
	    } else {
		ret.append(c);
	    }
	}

	return ret.toString();
    }

    /**
     * ldc is the connection to the directory server you're logged in
     * as directory manager (or not) to.
     */
    static public boolean isLocalDirectoryManager(LDAPConnection ldc) {
	// read the nsslapd-rootdn attribute from cn=config
	// if this fails, we are probably not bound as the root dn
	Debug.println(9, "DSUtil.isLocalDirectoryManager: begin");
	boolean status = false;
	try {
	    Debug.println(9, "DSUtil.isLocalDirectoryManager: ldc=" +
			  DSUtil.format(ldc));
	    String[] attrs = { "nsslapd-rootdn" };
	    LDAPEntry lde = ldc.read("cn=config", attrs);
	    String val = getAttrValue(lde, "nsslapd-rootdn");
	    if (val == null || val.length() == 0) {
		throw new LDAPException("No value for rootdn attribute",
					LDAPException.NO_SUCH_OBJECT);
	    }
	    status = DSUtil.equalDNs(ldc.getAuthenticationDN(), val);
	} catch (Exception e) {
	    Debug.println("DSUtil.isLocalDirectoryManager(): could not " +
			  "read cn=config from " +
			  DSUtil.format(ldc) +
			  " bound as " + ldc.getAuthenticationDN() +
			  ":" + e);
	}

	Debug.println(9, "DSUtil.isLocalDirectoryManager: end status = " +
		      status);
	return status;
    }

    /**
     * ldc is the connection to the Configuration Directory.  The host
     * and port are for the directory to look up
     */
    static public String getSIE(LDAPConnection ldc, String host, int port) {
	Debug.println(9, "DSUtil.getSIE: begin");
	String sie = null;
	try {
	    Debug.println(9, "DSUtil.getSIE: ldc=" +
			  DSUtil.format(ldc));
	    String base = LDAPUtil.getConfigurationRoot();
	    int scope = ldc.SCOPE_SUB;
	    String filter = "(&(serverHostName=" + host + ")" +
		"(nsServerPort=" + port + "))";
	    String[] attrs = { "serverHostName", "nsServerPort" };
	    LDAPSearchResults lsr = ldc.search(base, scope, filter, attrs,
					       false);
	    if (lsr != null && lsr.hasMoreElements()) {
		sie = ((LDAPEntry)lsr.nextElement()).getDN();
		Debug.println(9, "DSUtil.getSIE: SIE DN=" + sie);
		if (lsr.hasMoreElements()) {
		    Debug.println("DSUtil.getSIE: more than 1 SIE returned " +
				  base + " scope=" + scope + " using filter=" +
				  filter + " other SIE=" + lsr.nextElement());
		}
	    } else {
		Debug.println("DSUtil.getSIE: the search for " +
			      base + " scope=" + scope + " using filter=" +
			      filter + " was empty");
	    }
	} catch (Exception e) {
	    Debug.println("DSUtil.getSIE(): could not " +
			  "read the SIE DN from " +
			  DSUtil.format(ldc) +
			  " bound as " + ldc.getAuthenticationDN() +
			  ":" + e);
	}

	Debug.println(9, "DSUtil.getSIE: end SIE DN = " + sie);
	return sie;
    }

	
    /**
     * Reads the entry for the specified DN.
     * This method emulates LDAPConnection.read() in such a way
     * it works with LDAP subentries.
     *
     * @param ldc	LDAP connection to access the directory
     * @param dn	distinguished name of the entry to retreive
     * @param attrs	names of attributes to retrieve (null means all attributes)
     * @param cons  the constraints set for the read operation (null means no constraint)
     *
     * @return null if the method failed to retreive the entry
     */
    static public LDAPEntry readEntry(LDAPConnection ldc, String dn, 
				      String[] attrs, LDAPSearchConstraints cons) 
	throws LDAPException {
	LDAPEntry entry = null;
	LDAPSearchResults results = ldc.search(dn, LDAPv2.SCOPE_BASE,
					       "(|(objectclass=*)(objectclass=ldapsubentry))", attrs, false, cons);
	if (results != null) {
	    if (results.hasMoreElements()) {
		entry = results.next();
	    }
	}
			
	return entry;
    }


    static public boolean same(LDAPConnection ldc1, LDAPConnection ldc2) {
	String host1 = ldc1.getHost();
	String host2 = ldc2.getHost();
	try {
	    host1 = canonicalHost(host1);
	} catch (Exception e1) {
	}
	try {
	    host2 = canonicalHost(host2);
	} catch (Exception e2) {
	}
	return (host1.equalsIgnoreCase(host2) &&
		(ldc1.getPort() == ldc2.getPort()));
    }

    static public String getLdapURL(LDAPConnection ldc, String suffix) {
	boolean ldapSSL = (ldc.getSocketFactory() != null);
	return "ldap" + (ldapSSL ? "s" : "") + "://" + ldc.getHost() +
	    ":" + ldc.getPort() + "/" + suffix;
    }
		

    static public class DeferAuthListeners {
	public DeferAuthListeners(Vector listeners, String oldDN,
				  String newDN, String oldPwd,
				  String newPwd) {
	    _listeners = listeners;
	    _oldDN = oldDN;
	    _newDN = newDN;
	    _oldPwd = oldPwd;
	    _newPwd = newPwd;
	}

	public void notifyListeners() {
	    DSUtil.notifyListeners(_listeners, _oldDN, _newDN,
				   _oldPwd, _newPwd);
	}
	private Vector _listeners;
	private String _oldDN;
	private String _newDN;
	private String _oldPwd;
	private String _newPwd;
    }

    /**
     * Similar to Vector.indexOf() but the comparison is 
     * done using equalsIgnoreCase(). 
     * v must contain String instances.
     * Useful for testing vector representing object classes.
     */
    static public int indexOfIgnoreCase(Vector v, String s) {
	int result = -1;
	int count = v.size();
	int i = 0 ;
	while ((i < count) && (result == -1)) {
	    if (s.equalsIgnoreCase((String)v.elementAt(i))) {
		result = i;
	    }
	    i++;
	}
	return result;
    }
	
    /**
     * This function is used anywhere a list of LDBM backend instances
     * is needed.  It looks under the plugin config base dn for all
     * entries which match the ldbm backend instance search filter.
     * A custom search filter may also be used.  If the getMappingTree
     * flag is true, the mapping tree entry corresponding to the the
     * ldbm backend instance entry will be returned instead.
     * @param ldc            The LDAPConnection to the server
     * @param getMappingTree If true, the mapping tree entry corresponding
     *                       to the instance will be returned in addition to
     *                       the actual instance entry; in this case, the Vector
     *                       returned will be a Vector of Vectors.  The inner
     *                       Vector will contain two elements.  The first element
     *                       will be the backend instance entry, and the second
     *                       element will be the mapping tree entry
     * @param filter         If non null, an LDAP search filter to use instead of
     *                       the default
     * @return a Vector of LDAPEntry objects
     */
    static public Vector getLDBMInstanceList(LDAPConnection ldc,
					     boolean getMappingTree,
					     String filter) 
	throws LDAPException {
	Vector results = null;
	if (filter == null) {
	    filter = DEFAULT_DB_INSTANCE_FILTER;
	}
	LDAPSearchResults res = null;
	try {
	    res = ldc.search(LDBM_BASE_DN, ldc.SCOPE_SUB,
			     filter, null, false);
	    while (res.hasMoreElements()) {
		LDAPEntry entry = (LDAPEntry)res.nextElement();
		if (results == null) {
		    results = new Vector();
		}
		results.addElement(entry);
		Debug.println(8, "DSUtil.getLDBMInstanceList: found backend instance " +
			      entry.getDN());
	    }
	} catch (LDAPException e) {
	    Debug.println("DSUtil.getLDBMInstanceList: could not search under " +
			  "entry " + PLUGIN_CONFIG_BASE_DN + " in server " +
			  format(ldc) + ": " + e);
	    try {
		if (res != null) {
		    ldc.abandon(res);
		}
	    } catch (LDAPException lde2) {} // ignore abandon errors
	    throw e;
	}

	if (getMappingTree && (results != null)) {
	    Enumeration e = results.elements();
	    Vector newResults = new Vector(); // will replace the existing results
	    while (e.hasMoreElements()) {
		LDAPEntry instEntry = (LDAPEntry)e.nextElement();
		String cn = getAttrValue(instEntry, "cn");
		res = null;
		try {
		    String mtfilter = "nsslapd-backend=" + cn;
		    res = ldc.search(MAPPING_TREE_BASE_DN, ldc.SCOPE_ONE,
				     mtfilter, null, false);
		    if (res == null || !res.hasMoreElements()) {
			Debug.println("DSUtil.getLDBMInstanceList: instance entry " +
				      instEntry.getDN() + " has no corresponding " +
				      "mapping tree entry");
		    }
		    while (res != null && res.hasMoreElements()) {
			Vector v = new Vector(2);
			v.addElement(instEntry);
			LDAPEntry entry = (LDAPEntry)res.nextElement();
			v.addElement(entry);
			newResults.addElement(v);
			Debug.println(8, "DSUtil.getLDBMInstanceList: found mapping tree " +
				      "entry " + entry.getDN() + " corresponding to " +
				      "instance entry " + instEntry.getDN());
		    }						
		} catch (LDAPException lde) {
		    Debug.println("DSUtil.getLDBMInstanceList: could not search under " +
				  "entry " + MAPPING_TREE_BASE_DN +
				  " in server " + format(ldc) + ": " + lde);
		    try {
			if (res != null) {
			    ldc.abandon(res);
			}
		    } catch (LDAPException lde2) {} // ignore abandon errors
		    throw lde;
		}
	    }
	    results = newResults;
	}
	return results;
    }

    public static boolean isStandardSchema(LDAPSchemaElement lse) {
	/*
	 * Return 'true' if this is a standard schema element that the
	 * console should not allow to be edited.  If the X-ORIGIN
	 * element is present AND it contains a value of "user defined",
	 * then this function returns 'false'.  Otherwise it returns 'true'.
	 */
	boolean ret = true;
	if (lse != null) {
	    String[] ss = lse.getQualifier("X-ORIGIN");
	    if (ss != null ) {
			Debug.println(8, "DSUtil.isStandardSchema: schema " +
						  lse.getName() + " qual = " + ss);
			for ( int i = 0; i < ss.length; ++i ) {
				if ( ss[i].equalsIgnoreCase("user defined")) {
					ret = false;	/* not standard schema */
					break;
				}
			}
		}
	}

	if (ret) {
		Debug.println(8, "DSUtil.isStandardSchema: schema " +
					  lse.getName() + " isStandardSchema=true" );
	}

	return ret;
    }


	/**
	  * This function tells wether the given String is a valid database name or not.
	  * We don't allow spaces o accentuated character.
	  *
	  * @ param bckName the name of the database
	  * @ returns true if the name is valid false otherwise.
	  */
    public static boolean isValidBckName ( String bckName ) {		
		boolean isValidBckName = true;
		if ((bckName != null) && (bckName.length() > 0)) {
			StringCharacterIterator iter = new StringCharacterIterator(bckName, 0);
			for (char c = iter.first(); 
				 (c != StringCharacterIterator.DONE) && isValidBckName; 
				 c = iter.next()) {
				if (_bckValidSyntax.indexOf(Character.toLowerCase(c)) == -1) {
					isValidBckName = false;
				}
			}
		}		
		return isValidBckName;
    }
	protected static final String _bckValidSyntax = "abcdefghijklmnopqrstuvwxyz0123456789-_";


    public static boolean isValidLDAPUrl(String url) {
	boolean good = false;
	try {
	    LDAPUrl lurl = new LDAPUrl(url);
	    good = true;
	} catch (Exception e) {
	}
	return good;
    }


    /**
     *  Returns a RemoteImage corresponding to the superposition of the icon Image and the mask Image
     *
     * @param icon  the RemoteImage that we want to bar
     */
	public static RemoteImage maskedIcon(ImageIcon icon, ImageIcon mask) {
		RemoteImage fReturn;
				
		int h = icon.getIconHeight();
		int w = icon.getIconWidth();
			
		if (mask.getImageLoadStatus() != MediaTracker.COMPLETE) {
			Debug.println("DSUtil.maskedIcon(): Error while loading maskImage");
			return null;
		}
		Image maskImage = mask.getImage();
		
		Image scaledMaskImage = maskImage.getScaledInstance(w, h , maskImage.SCALE_SMOOTH);

		RemoteImage scaledMask = new RemoteImage(scaledMaskImage);
		if (scaledMask.getImageLoadStatus() != MediaTracker.COMPLETE) {
			Debug.println("DSUtil.maskedIcon(): Error while loading scaledMaskImage");
			return null;
		}

		int[] iconPixels = new int[w * h];
		try {			
			PixelGrabber pg = new PixelGrabber( icon.getImage(), 0, 0, w, h, iconPixels, 0, w);
			pg.grabPixels();
			
			if ((pg.status() & ImageObserver.ABORT) !=0) {
				Debug.println("DSUtil.maskedIcon(): Error while fetching icon");
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		int[] filterPixels = new int[w * h];
		try {			
			PixelGrabber pgf = new PixelGrabber( scaledMask.getImage(), 0, 0, w, h, filterPixels, 0, w);
			pgf.grabPixels();
			
			if ((pgf.status() & ImageObserver.ABORT) !=0) {
				Debug.println("DSUtil.maskedIcon(): Error while fetching mask");
				fReturn = null;
				return fReturn;
			}
		} catch (Exception e) {
			e.printStackTrace();
			fReturn = null;
			return fReturn;
		}


		int[] newPixels = new int[w * h];

		for( int i = 0; i < h; i++) 
			for (int j = 0; j < w; j++)
				if (filterPixels[j + i*w] != TRANSPARENT) {
					newPixels[j + i*w] = filterPixels[j + i*w];
				} else {
					newPixels[j + i*w] = iconPixels[j + i*w];
				}
		Canvas component = new Canvas();
		
		Image newImage = component.getToolkit().createImage(new MemoryImageSource(w, h, ColorModel.getRGBdefault(), newPixels, 0, w));
		fReturn = new RemoteImage(newImage);

		return fReturn;
	}


    /**
     *  Returns a RemoteImage corresponding to the barred image of the RemoteImage received as parameter
     *
     * @param icon  the RemoteImage that we want to bar
     */    
    public static RemoteImage inactivatedIcon(ImageIcon icon) {
		RemoteImage inactivatedMask = getPackageImage("inactivated.gif");
		return maskedIcon(icon, inactivatedMask);
    }

    /**
     *  Returns a RemoteImage corresponding to the barred image of the RemoteImage received as parameter.
	 * This is used to represent inactivated roles.
     *
     * @param icon  the RemoteImage that we want to bar
     */    
    public static RemoteImage inactivatedRoleIcon(ImageIcon icon) {
		RemoteImage inactivatedMask = getPackageImage("roleinactivated.gif");
		return maskedIcon(icon, inactivatedMask);
    }

    /**
     * Returns a truncated String of size the parameter size, followed by a String 
     * symbolizing that the String is not over (three points in the english version '...').
     * For example: if we do abreviateString("one", 1)   we get "o..."
     *
     * @param string: the String to treate
     * @param size: the desired size for the truncated String
     * @return: the truncated String
     */

    public static String abreviateString(String string, int size) {
		if (string == null) {
			return null;
		}
		if (string.length() > size) {
			String returnString = string.substring(0, size) + _resource.getString( "general", "abreviate-ending");
			return returnString;
		} else {
			String returnString = new String(string);
			return returnString;
		}
    }


    /**
     * Returns a truncated String of size the parameter size, preceded by a String 
     * symbolizing that the String is not over (three points in the english version '...').
     * For example: if we do abreviateString("one", 1)   we get "...e"
     *
     * @param string: the String to treate
     * @param size: the desired size for the truncated String
     * @return: the truncated String
     */

    public static String inverseAbreviateString(String string, int size) {
		if (string == null) {
			return null;
		}
		int totalSize = string.length();
		if (totalSize > size) {
			String returnString = _resource.getString( "general", "abreviate-ending") + string.substring(totalSize - size, totalSize) ;
			return returnString;
		} else {
			String returnString = new String(string);
			return returnString;
		}
    }	
	
	/**
	 * Search the children components of c and returns
	 * the first one which is instance of compClass.
	 * Return null if nothing is found.
	 */
	public static Component searchChildComponent(Component c, Class compClass) {
		Component result = null;
		
		if (compClass.isInstance(c)) {
			result = c ;
		}
		else if (c instanceof Container) {
			Component[] children = ((Container)c).getComponents();
			int i = 0;
			while ((result == null) && (i < children.length)) {
				result = searchChildComponent(children[i], compClass);
				i++;
				
			}
		}
		
		return result;
	}
	

    /**
     * DbType
     * @param ldc           - directory console connection
     * @param sDNEntryDB	- database instance's DN
     *
     * @return 0 = ldbm, 1 = chaining, -1 = unknown
     */
    public static int DbType( 	LDAPConnection ldc, String sDNEntryDB ) {

	int resType = -1;
		
	DN dnDB = new DN( sDNEntryDB );
	if( dnDB != null ) {
	    try {
		netscape.ldap.util.DN dnParent = dnDB.getParent();
		if ( dnParent != null ){
		    LDAPEntry entry = ldc.read( dnParent.toString() );
		    if ( entry != null ) {
			LDAPAttribute attr = 
			    entry.getAttribute( "nsslapd-pluginid" );
			Enumeration en = attr.getStringValues();
			String sType = (String)en.nextElement();
			if( sType.compareTo( LDBM_PLUGIN_ID ) == 0 ) {
			    resType = LDBM_TYPE;
			} else if ( sType.compareTo( CHAINING_PLUGIN_NAME ) == 0 ) {
			    resType = CHAINING_TYPE;
			}
		    }
		}
	    } catch( LDAPException e ) {
		Debug.println( "DSUtil.DbType: error" +
			       sDNEntryDB  +
			       "\n\t LDAPException : " +
			       e.toString());
	    }
	}
	return( resType );
    }

    public static boolean  addLDBMBackend(IDSModel model,
				     String instName,
				     String pluginDN,
				     String dbLoc,
				     String MappingNode,
				     String section ) {
	
	LDAPConnection ldc = model.getServerInfo().getLDAPConnection();
	Debug.println("NewLDBMInstancePanel.addLDBMBackend()");
	// Retreive the ldbm location in config
	
	// Add Instance Entry
	String dn_dbInst = "cn=" + instName + "," + pluginDN;

	LDAPAttributeSet attrs = new LDAPAttributeSet();
	
	String objectclass_dbInst[] = { "top", 
					"extensibleObject", 
					"nsBackendInstance" };
	LDAPAttribute attr = new LDAPAttribute( "objectclass", 
						objectclass_dbInst );
	attrs.add( attr );

	String cn_dbInst[] = { instName };
	attrs.add( new LDAPAttribute( "cn", cn_dbInst ) );
	
	
	String suffix_dbInstConfig[] = { MappingNode };
	attrs.add( new LDAPAttribute ( "nsslapd-suffix", suffix_dbInstConfig ));

	String cachesize_dbInstConfig[] = { "-1" };
	attrs.add( new LDAPAttribute ( "nsslapd-cachesize",
				       cachesize_dbInstConfig ));
	
	String cachememsize_dbInstConfig[] = { "10485760" };
	attrs.add( new LDAPAttribute ( "nsslapd-cachememsize",
				       cachememsize_dbInstConfig ));

	
	if (dbLoc.length() > 0 ) {
	    String ldbm_directory[] = { dbLoc };
	    attrs.add( new LDAPAttribute ( "nsslapd-directory",
					   ldbm_directory ));
	}
 

	LDAPEntry dbInst = new LDAPEntry( dn_dbInst, attrs );
	model.setWaitCursor( true );
	try {
	    ldc.add( dbInst );
	    Debug.println("****** add:" + dn_dbInst);
	} catch (LDAPException e) {
	    String[] args_m = { dn_dbInst, e.toString()} ;
	    DSUtil.showErrorDialog( model.getFrame(),
				    "error-add-mapping",
				    args_m,
				    section);
	    Debug.println("****** error adding " +
			  dn_dbInst +
			  ". Error is : " +
			  e.toString() );
	    return ( false );
	} finally {
	    model.setWaitCursor( false );
	}
	return ( true );
		
    }


	/**
	 * Return true if ConsoleInfo identifies a Windows machine.
	 * This methods relies on the getAdminOS() of ConsoleInfo.
	 */
	public static boolean isNT(ConsoleInfo ci) {
		return ci.getAdminOS().startsWith("Windows");
	}

	/**
	 * Return true if the console is running on the same machine as the server
	 */
	public static boolean isLocal(String host) {
		if ( _local == -1 ) {
			// Now try to determine if the MCC is running on the server.
			// We verify this by comparing the IP of the client to
			// the IP of the server.        
			try {
				_local = 0;
				InetAddress hostAddress = InetAddress.getByName(host);
				InetAddress clientAddress = InetAddress.getLocalHost();
				byte[] hostIP = hostAddress.getAddress();
				byte[] clientIP = clientAddress.getAddress();
				if (hostAddress.equals(clientAddress) ||
					host.equals("localhost"))
					_local = 1;
			} catch (UnknownHostException uhe) {
				Debug.println("Unkown host for: " + host +
							  " uhe: " + uhe.toString());
			}
		}
		return (_local == 1);	
	}

	/**
	 * This method returns wether a file exists or not.
	 * Method used to bypass a problem of the JDK with large files (it returns false systematically
	 * even if the file exists).
	 *
	 * @param file the FILE we are handling
	 * @returns true if the file exists, false otherwise.
	 */
	public static boolean fileExists(File file) {
		if (file == null) {
			return false;
		}
		if (file.exists()) {
			return true;
		}
		FileSystemView view = null;
		view = FileSystemView.getFileSystemView();
		File parent = null;
		parent = view.getParentDirectory(file);
		if (parent == null) {
			return false;
		}
		File[] filesUnderParent = view.getFiles(parent, false);
		if (filesUnderParent == null) {
			return false;
		}
		int i=0;
		for (i=0; i<filesUnderParent.length; i++) {
			if (filesUnderParent[i].equals(file)) {
				return true;
			}
		}
		return false;
	}

    /**
     * Returns the default path used for backups.
     *
     * @param info serverInfo, information for the current server
     *
     * @return the default backup path
     */
    public static String getDefaultBackupPath(ConsoleInfo serverInfo) {
        return getDefaultDSPath( serverInfo, CONFIG_BASE_DN, BAKDIR_ATTR );
    }

    /**
     * Returns the default path used to store the changelog database.
     *
     * @param info serverInfo, information for the current server
     *
     * @return the default changelog database path
     */
    public static String getDefaultChangelogPath(ConsoleInfo serverInfo) {
        String path = getDefaultDBPath( serverInfo );

        // We want the changelog path to be at the same level as the db directory.
        Matcher matcher = Pattern.compile("/db$").matcher( path );
        return matcher.replaceAll("/" + CHANGELOG_DIR);
    }

    /**
     * Returns the default path used to store database files.
     *
     * @param info serverInfo, information for the current server
     *
     * @return the default database path
     */
    public static String getDefaultDBPath(ConsoleInfo serverInfo) {
        return getDefaultDSPath( serverInfo, LDBM_CONFIG_BASE_DN, DBDIR_ATTR );
    }

    /**
     * Returns the default path used to store the LDIF files.
     *
     * @param info serverInfo, information for the current server
     *
     * @return the default LDIF path
     */
    public static String getDefaultLDIFPath(ConsoleInfo serverInfo) {
        return getDefaultDSPath( serverInfo, CONFIG_BASE_DN, LDIFDIR_ATTR );
    }

    /**
     * Returns the default path used to store the server log files.
     *
     * @param info serverInfo, information for the current server
     *
     * @return the default server logfile path
     */
    public static String getDefaultLogPath(ConsoleInfo serverInfo) {
        String path = getDefaultDSPath( serverInfo, CONFIG_BASE_DN, LOGDIR_ATTR );

        // There is no logdir attribute, so we get the errorlog location and
        // trim off the logfile name to get the default path.
        Matcher matcher = Pattern.compile("/errors$").matcher( path );
        return matcher.replaceAll("");
    }

    private static String getDefaultDSPath(ConsoleInfo serverInfo,
                                          String entry,
                                          String pathAttr) {
        String defaultPath = "";

        if (serverInfo != null) {
            LDAPConnection ldc = serverInfo.getLDAPConnection();
            if ( reconnect( ldc ) ) {
                try {
                    /* Lookup the path from the server config */
                    String[] attrs = { pathAttr };
                    LDAPEntry lde = ldc.read(entry, attrs);
                    defaultPath = getAttrValue(lde, pathAttr);
                } catch ( LDAPException e ) {
                    Debug.println("DSUtil.getDefaultDSPath() : Caught ldap exception + e");
                }
            }
        } else {
            Debug.println("DSUtil.getDefaultDSPath() : serverInfo is null");
        }

        File defaultPathFile = new File(defaultPath);
        if (!defaultPathFile.exists()  &&
            isLocal(serverInfo.getHost())) {
            Debug.println("DSUtil.getDefaultDSPath() : " + defaultPath + " does not exist");
        }

        return defaultPath;
    }

	/**
	 * Gets a boolean valued attribute from a properties file.  Values meaning
	 * true are the following: "true", "yes", "on", "1".  Values meaning false
	 * are the following: "false", "no", "off", "0".  Case
	 * is ignored and any unique beginning substring is allowed e.g.
	 * t == tr == tru == true
	 */
	public static boolean getBoolean(String section, String name) {
		boolean ret = false;
		String sval = _resource.getString(section, name);
		if ((sval == null) || (sval.length() == 0)) { /* none or empty == false */
			ret = false;
		} else if ("true".startsWith(sval.toLowerCase())) {
			ret = true;
		} else if ("false".startsWith(sval.toLowerCase())) {
			ret = false;
		} else if ("on".startsWith(sval.toLowerCase())) {
			ret = true;
		} else if ("off".startsWith(sval.toLowerCase())) {
			ret = false;
		} else if ("yes".startsWith(sval.toLowerCase())) {
			ret = true;
		} else if ("no".startsWith(sval.toLowerCase())) {
			ret = false;
		} else if ("0".equals(sval)) {
			ret = false;
		} else { // any other value is true
			ret = true;
		}

		return ret;
	}	

    // This the subdir where the DS online help is stored
    public static final String HELP_DIRECTORY = "slapd";
	
    public static final int AUTH_SUCCESS = 0;
    public static final int AUTH_CANCEL  = 1;    
    public static final int AUTH_FAILURE = 2;

    private static Preferences _confirmationPreferences = null;

    // a cache of previously looked up addresses used by canonicalHost()
    private static Hashtable _addressCache = new Hashtable();

    /* ??? Get the manager DN from the Directory, when it's there */
    /* Default for reauth */
    static private String _authName = "cn=Directory Manager";
    private static final String  _sImageDir	=
    	"com/netscape/admin/dirserv/images"; // DS icons live here
    private static final String  _sGeneralImageDir	=
	"com/netscape/management/client/icons"; // Shared icons
    // Keep track of loaded images, so they only need to be loaded once
    private static Hashtable _cPackageImages = new Hashtable();
    private static Hashtable _cSharedImages = new Hashtable();
    private static Hashtable _cRequiresRestart = null;
    private static Icon _infoIcon = null;
    private static Icon _warningIcon = null;
    private static Icon _errorIcon = null;
    public static final String AUTH_CHANGE_LISTENERS = "AuthChangeListeners";
    private static final String SECURITY_ATTR = "nsslapd-security";
    private static final String BAKDIR_ATTR = "nsslapd-bakdir";
    private static final String DBDIR_ATTR = "nsslapd-directory";
    private static final String LDIFDIR_ATTR = "nsslapd-ldifdir";
    private static final String LOGDIR_ATTR = "nsslapd-errorlog";
    private static final String CHANGELOG_DIR = "changelogdb";
    private static final ResourceSet _helpResource =
	new ResourceSet("com.netscape.admin.dirserv.dirserv-help");
    static public ResourceSet _resource = 
	new ResourceSet("com.netscape.admin.dirserv.dirserv");
    static public ResourceSet _skinResource = 
    	new ResourceSet("com.netscape.admin.dirserv.dirserv-skin");
    static public ResourceSet _langResource =
	new ResourceSet(
			"com.netscape.management.client.ug.PickerEditorResource");

	private static int _local = -1;

    static public final String CONFIG_BASE_DN = "cn=config";
    static public final String PLUGIN_CONFIG_BASE_DN = "cn=plugins,cn=config";
    static public final String LDBM_BASE_DN = "cn=ldbm database,cn=plugins,cn=config";
    static public final String LDBM_CONFIG_BASE_DN = "cn=config,cn=ldbm database,cn=plugins,cn=config";
    static public final String CHAINING_CONFIG_BASE_DN = "cn=chaining database,cn=plugins,cn=config";
    static public final String DEFAULT_DB_INSTANCE_FILTER = "objectclass=nsBackendInstance";
    static public final String MAPPING_TREE_BASE_DN = "cn=mapping tree,cn=config";
    static public final String DEFAULT_LDBM_INDEX_PREFIX= "cn=default indexes, cn=config";
    static public final String LDBM_PLUGIN_ID = "ldbm-backend";
    static public final String LDBM_PLUGIN_NAME = "ldbm database";
    static public final String CHAINING_PLUGIN_NAME = "chaining database";
    static public final String DEFAULT_LDBM_INSTANCE_FILTER ="(&("+
	DEFAULT_DB_INSTANCE_FILTER + ")(nsslapd-pluginid=" +
	LDBM_PLUGIN_ID + "))";
    // this is the LDAPConnection to the Config Directory
    static private LDAPConnection _configLdc = null;
    static public final int LDBM_TYPE = 0;
    static public final int CHAINING_TYPE = 1;
	public static int TRANSPARENT = 16711165;  // The value of a transparent pixel	
}

