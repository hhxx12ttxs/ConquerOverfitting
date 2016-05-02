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

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.net.Socket;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.text.*;
import com.netscape.management.client.util.*;
import com.netscape.management.client.components.GenericDialog;
import com.netscape.admin.dirserv.*;
import com.netscape.admin.dirserv.panel.UIFactory;
import netscape.ldap.*;
import netscape.ldap.util.*;

/**
 * Display this dialog to get a NewInstance.
 *
 * @author dt
 * @author rweltman
 *
 */

public class NewInstanceDialog extends AbstractDialog {
	/**
	 * Creates a dialog which asks the user to input the basic parameters
	 * needed to create a new DS instance.  All of the other needed basic
	 * information is derived from other information stored in the topology.
	 * The parameters may be given default values in the constructor.  If
         * the default value given is null, this means to disable that field.
         * If the default value given is "" the empty string, enable the field
         * with an empty value.  If root DN is null the root DN password entry
         * field will be disabled as well.
	 *
	 * @param parent         Parent container.
	 */
	public NewInstanceDialog( JFrame parent, String[] keys, Hashtable values) {
		super(parent, "", true, OK|CANCEL|HELP);
		_parent = parent;
		_keys = keys;
		_values = values;
		setTitle(_resource.getString("NewInstanceDialog", "title"));

		Container p = getContentPane();
		p.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor     = gbc.WEST;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.fill       = gbc.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;

		if (keys.length > INSTANCENAME && keys[INSTANCENAME] != null) {
			_tfInstanceName =
				new LabeledTextField("instancename",
									 (String)values.get(keys[INSTANCENAME]),
									 false, getContentPane(), gbc);
		}

		if (keys.length > PORT && keys[PORT] != null) {
			_tfPort =
				new LabeledTextField("port",
									 (String)values.get(keys[PORT]),
									 false, getContentPane(), gbc);
		}

		if (keys.length > SUFFIX && keys[SUFFIX] != null) {
			_tfSuffix =
				new LabeledTextField("suffix",
									 (String)values.get(keys[SUFFIX]),
									 false, getContentPane(), gbc);
		}

		if (keys.length > ROOTDN && keys[ROOTDN] != null) {
			_tfRootDN =
				new LabeledTextField("rootdn",
									 (String)values.get(keys[ROOTDN]),
									 false, getContentPane(), gbc);
		}

		if (keys.length > ROOTDNPWD) {
			String val;
			if (keys[ROOTDNPWD] != null)
			    val = (String)values.get(keys[ROOTDNPWD]);
			else
				val = "";
			// hack: if we're not asking for rootdn, assume we are asking
			// for the rootpw of the server to migrate
			String fieldName = "rootdnpwd";
			if (keys[ROOTDN] == null)
				fieldName = "rootdnpwdmigrate";
			_tfRootDNPwd1 = new LabeledTextField(fieldName + "1", val,
												 true, getContentPane(), gbc);
			_tfRootDNPwd2 = new LabeledTextField(fieldName + "2", val,
												 true, getContentPane(), gbc);
		}

		if (keys.length > LOCALUSER && keys[LOCALUSER] != null) {
			_tfLocalUser =
				new LabeledTextField("localuser",
									 (String)values.get(keys[LOCALUSER]),
									 false, getContentPane(), gbc);
		}

		Boolean enableshutdown = Boolean.valueOf(
			_resource.getString("NewInstanceDialog", "enableshutdown"));
		if (enableshutdown.booleanValue() && keys.length > SHUTDOWN_OLD &&
			keys[SHUTDOWN_OLD] != null) {
			ButtonGroup shutdownButtonGroup = new ButtonGroup();

			Boolean bl = (Boolean)values.get(keys[SHUTDOWN_OLD]);
			_rbShutdown = new LabeledRadioButton("shutdown",
												 bl.booleanValue(),
												 getContentPane(), gbc);
			shutdownButtonGroup.add(_rbShutdown.getRadioButton());

			_rbNoShutdown = new LabeledRadioButton("noshutdown",
												   !bl.booleanValue(),
												   getContentPane(), gbc);
			shutdownButtonGroup.add(_rbNoShutdown.getRadioButton());
		}

		if (keys.length > SECPWD && keys[SECPWD] != null) {
			String val = "";
			if (keys[SECPWD] != null)
			    val = (String)values.get(keys[SECPWD]);
			_tfSecPwd = new LabeledTextField("secpwd", val,
											 true, getContentPane(), gbc);
		}

		pack();
	}


	/**
	 * OK button action.
	 * Subclasses GenericDialog.
	 */
	protected void okInvoked() {
		String err = null;
		String[] args = null;
		JTextComponent field = null;
		boolean confirm = false; // set to true if you want a confirm dialog

		// first, set the new default values to the values typed in by
		// the user
		if (_tfRootDN != null) {
			_values.put(_keys[ROOTDN], _tfRootDN.getValue());
		}

		if (_tfRootDNPwd1 != null) {
			_values.put(_keys[ROOTDNPWD], _tfRootDNPwd1.getValue());
		}

		if (_tfSuffix != null) {
			_values.put(_keys[SUFFIX], _tfSuffix.getValue());
		}

		if (_tfPort != null) {
			_values.put(_keys[PORT], _tfPort.getValue());
		}

		if (_tfInstanceName != null) {
			_values.put(_keys[INSTANCENAME],
						_tfInstanceName.getValue());
		}

		if (_tfLocalUser != null) {
			_values.put(_keys[LOCALUSER],
						_tfLocalUser.getValue());
		}

		if (_rbShutdown != null && _rbNoShutdown != null) {
			_values.put(_keys[SHUTDOWN_OLD],
						new Boolean(_rbShutdown.getValue()));
			Debug.println("NewInstanceDialog.actionPerformed: shutdown=" +
						  _rbShutdown.getValue());
		}

		if (_tfSecPwd != null) {
			String val = _tfSecPwd.getValue();
			if (val == null)
				val = "";

			_values.put(_keys[SECPWD], val);
		}

		// now, do validation on the user input
		if (_tfRootDN != null) {
			String val = _tfRootDN.getValue();
			if (!DN.isDN(val)) {
				err = "122";
				field = _tfRootDN.getTextField();
				args = new String[1];
				args[0] = _tfRootDN.getLabel();
			} else if (val.length() == 0) {
				err = "116";
				args = new String[1];
				args[0] = _tfRootDN.getLabel();
				field = _tfRootDN.getTextField();
			} else {
				DSUtil.checkForLDAPv2Quoting(val, _parent,
											 _tfRootDN.getLabel());
			}
		}

		if (err == null && _tfRootDNPwd1 != null) {
			String val = _tfRootDNPwd1.getValue();
			if (val == null || val.length() < 8) {
				err = "114";
				field = _tfRootDNPwd1.getTextField();
			} else if (_tfRootDNPwd2.getValue() == null ||
					   !val.equals(_tfRootDNPwd2.getValue())) {
				err = "passwordMismatch";
				field = _tfRootDNPwd2.getTextField();
			} else if (!is7BitClean(val)) {
				err = "not7bitclean";
				args = new String[1];
				args[0] = _tfRootDNPwd1.getLabel();
				field = _tfRootDNPwd1.getTextField();
			}
		}

		if (err == null && _tfSuffix != null) {
			if (_tfSuffix.getValue().length() == 0) {
				err = "116";
				args = new String[1];
				args[0] = _tfSuffix.getLabel();
				field = _tfSuffix.getTextField();
			} else if (!DN.isDN(_tfSuffix.getValue())) {
				err = "122";
				field = _tfSuffix.getTextField();
				args = new String[1];
				args[0] = _tfSuffix.getLabel();
			} else {
				DSUtil.checkForLDAPv2Quoting(_tfSuffix.getValue(), _parent,
											 _tfSuffix.getLabel());
			}
		}

		if (err == null && _tfPort != null) {
			int port = 0;
			try {
				port = Integer.parseInt(_tfPort.getValue());
			} catch (Exception ex) {
				err = "notanumber";
				args = new String[1];
				args[0] = _tfPort.getLabel();
				field = _tfPort.getTextField();
			}

			if (err == null) {
				String host = (String)_values.get(_keys[HOST]);
				if (_tfPort.getValue().length() == 0) {
					err = "116";
					args = new String[1];
					args[0] = _tfPort.getLabel();
					field = _tfPort.getTextField();
				} else if (port < 1) {
					// value is less than recommended minimum
					err = "103";
					args = new String[2];
					args[0] = _tfPort.getValue();
					args[1] = "1";
					field = _tfPort.getTextField();
				} else if (port > 65535) {
					// value is greater than recommended maximum
					err = "104";
					args = new String[2];
					args[0] = _tfPort.getValue();
					args[1] = "65535";
					field = _tfPort.getTextField();
				} else if (portIsInUse(host, port)) {
					err = "portinuse";
					args = new String[1];
					args[0] = host;
					field = _tfPort.getTextField();
					confirm = true; // user may continue to use port in use
				} else { // convert integer back to single byte string
							// in case it was entered as double byte
					_values.put(_keys[PORT], Integer.toString(port));
				}
			}
		}

		if (err == null && _tfInstanceName != null) {
			if (_tfInstanceName.getValue().length() == 0) {
				err = "116";
				args = new String[1];
				args[0] = _tfInstanceName.getLabel();
				field = _tfInstanceName.getTextField();
			} else if (!is7BitClean(_tfInstanceName.getValue())) {
				err = "not7bitclean";
				args = new String[1];
				args[0] = _tfInstanceName.getLabel();
				field = _tfInstanceName.getTextField();
			}
		}

		Debug.println("NewInstanceDialog.actionPerformed(): err=" +
					  err + " confirm=" + confirm + " arg=" +
					  ((args != null) ? args[0] : ""));
		if (err != null) {
			int response = JOptionPane.NO_OPTION;
			if (confirm) {
				response = DSUtil.showConfirmationDialog(
					_parent, err, args, "general");
			} else {
				DSUtil.showErrorDialog(_parent, err, args);
			}

			if (response == JOptionPane.NO_OPTION) {
				if (field != null) {
					field.grabFocus(); // put the keyboard focus in this field
				}
				return;
			}
		}
		Debug.println(9, "NewInstanceDialog.okInvoked(): " +
					  "before super.okInvoked()");
		super.okInvoked();
		Debug.println(9, "NewInstanceDialog.okInvoked(): " +
					  "after super.okInvoked()");
	}
	
	/**
	 * Help button action
	 * Subclasses GenericDialog.
	 */
	protected void helpInvoked() {
		DSUtil.help( _helpToken );
	}
	 	


	public static void main( String[] args ) {
		String[] keys = { "instancename", "port", "suffix",
						  "rootdn", "rootdnpwd", "localuser" };
		Hashtable h = new Hashtable();
		h.put(keys[INSTANCENAME], "foo");
		h.put(keys[PORT], "foo");
		h.put(keys[SUFFIX], "foo");
		h.put(keys[ROOTDN], "foo");
		h.put(keys[ROOTDNPWD], "");
		h.put(keys[LOCALUSER], "root");
		NewInstanceDialog dlg = new NewInstanceDialog( new JFrame(), keys, h );
		dlg.setLocation(new Point(300,320));
		dlg.show();
		keys[SUFFIX] = null;
		keys[ROOTDN] = null;
		dlg = new NewInstanceDialog( new JFrame(), keys, h );
		dlg.setLocation(new Point(300,320));
		dlg.show();
		System.exit(0);
	}

	private class LabeledTextField {
		public LabeledTextField(String name, String value, boolean isPwd,
								Container parent, GridBagConstraints gbc) {
			
			_label = UIFactory.makeJLabel("NewInstanceDialog", name);
			_label.setHorizontalAlignment(SwingConstants.RIGHT);
			gbc.anchor = gbc.EAST;
			gbc.gridx = 0;
			gbc.gridy++;
			gbc.gridwidth = 1;
			gbc.insets = new Insets(0,PAD,3,8);
			parent.add(_label, gbc);

			if (isPwd) {
				_tf = UIFactory.makeJPasswordField( NewInstanceDialog.this,
													textWidth );
			} else {
				_tf = UIFactory.makeJTextField( NewInstanceDialog.this,
												textWidth );
			}
			if (value != null) {
				_tf.setText(value);
			}
			gbc.gridwidth = gbc.REMAINDER;
			gbc.insets = new Insets(0,0,3,PAD);
			gbc.gridx = 1;
			gbc.weightx = 1;
			gbc.anchor = gbc.WEST;
			parent.add(_tf, gbc);
		}

		public String getValue() {
			return _tf.getText();
		}

		public String getLabel() {
			return _label.getText();
		}

		public JTextComponent getTextField() {
			return _tf;
		}

		private JTextComponent _tf = null;
		private JLabel _label = null;
	}

	private class LabeledRadioButton {
		public LabeledRadioButton(String name, boolean value,
								  Container parent, GridBagConstraints gbc) {
			
			_rb = UIFactory.makeJRadioButton(NewInstanceDialog.this,
											 "NewInstanceDialog", name, value);
			_rb.setHorizontalAlignment(SwingConstants.RIGHT);
			gbc.anchor = gbc.EAST;
			gbc.gridx = 0;
			gbc.gridy++;
			gbc.gridwidth = 1;
			gbc.insets = new Insets(0,PAD,3,8);
			parent.add(_rb, gbc);
		}

		public boolean getValue() {
			return _rb.isSelected();
		}

		public String getLabel() {
			return _rb.getText();
		}

		public JRadioButton getRadioButton() {
			return _rb;
		}

		private JRadioButton _rb = null;
	}

	private boolean is7BitClean(String pwd) {
		if (pwd == null) {
			return false;
		}

		for (int ii = 0; ii < pwd.length(); ++ii) {
			int val = (int)pwd.charAt(ii);
			if ((val > 255) || ((val & 0x80) != 0))
				return false;
		}

		return true;
	}

	/**
	 * Return true if the port is in use on the specified host, false otherwise
	 */
	private boolean portIsInUse(String host, int port) {
		boolean status = false;
		if (host == null || port <= 0) {
			Debug.println("NewInstanceDialog.portIsInUse(): host=" + host +
						  " port=" + port);
			return status;
		}

		Socket test = null;
		try {
			test = new Socket(host, port);
			Debug.println("NewInstanceDialog.portIsInUse(): socket=" + test);
			status = true;
			test.close();
		} catch (Exception e) {
			Debug.println("NewInstanceDialog.portIsInUse(): exception=" + e);
		}

		Debug.println("NewInstanceDialog.portIsInUse(): status=" + status);
		return status;
	}
		
	private static final int PAD = 0;
	private static final int textWidth = 15;
	private static final int WIDTH = 400;
	private static final int HEIGHT = 300;

	// instance name textfield
	private LabeledTextField  _tfInstanceName = null;
	private LabeledTextField  _tfPort = null;		  // port textfield
	private LabeledTextField  _tfSuffix = null;		  // port textfield
	private LabeledTextField  _tfRootDN = null;		  // RootDN textfield
	private LabeledTextField  _tfRootDNPwd1 = null;	  // RootDNPwd1 field
	private LabeledTextField  _tfRootDNPwd2 = null;	  // RootDNPwd2 field
	private LabeledTextField  _tfLocalUser = null;	  // localuser field
	private LabeledRadioButton  _rbShutdown = null;	  // toggle to shutdown old server
	private LabeledRadioButton  _rbNoShutdown = null; // toggle to not shutdown old server
	private LabeledTextField  _tfSecPwd = null;	      // cert/key db password field

	private JFrame _parent = null; // parent container

	private String[] _keys; // keys for user specified hashtable
	private Hashtable _values; // values to provide for user

	private ResourceSet _resource = DSUtil._resource;
	private static final String _helpToken = "menubar-newinstance-dbox-help";

	// If the user passes in an array of string keys, the array elements
	// should correspond to these indices e.g.
	//     array[INSTANCENAME] == "serverid"
	//  or array[PORT] = "389"
	public static final int INSTANCENAME = 0;
	public static final int PORT = 1;
	public static final int SUFFIX = 2;
	public static final int ROOTDN = 3;
	public static final int ROOTDNPWD = 4;
	public static final int LOCALUSER = 5;
	public static final int SHUTDOWN_OLD = 6;
	public static final int SECPWD = 7;
	public static final int HOST = 8;
}

