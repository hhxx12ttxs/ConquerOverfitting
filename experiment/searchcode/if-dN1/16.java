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
package com.netscape.admin.dirserv.panel.replication;

import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.event.*;
import com.netscape.management.client.console.ConsoleInfo;
import netscape.ldap.*;
import netscape.ldap.util.DN;
import com.netscape.admin.dirserv.DSUtil;
import com.netscape.management.client.util.Debug;
import com.netscape.admin.dirserv.IDSModel;
import com.netscape.admin.dirserv.panel.UIFactory;
import com.netscape.admin.dirserv.panel.BackendPanel;


public class ReplicaPanel extends BackendPanel {
	public ReplicaPanel(LDAPConnection ldc, LDAPEntry replicaEntry) {
		super(ldc);
		setSelectedReplica(replicaEntry);
	}

	public LDAPEntry getReplicaEntry() {
		String key = String.valueOf(_theChoice.getSelectedIndex());
		Hashtable ht = (Hashtable)_htSuffixAndBackends.get(key);
		return (LDAPEntry)ht.get(ENTRY_KEYWORD);
	}

	public boolean setSelectedReplica(LDAPEntry replicaEntry) {
		if (replicaEntry == null)
			return false;

		for (int i=0 ; i<_htSuffixAndBackends.size(); i++) {
			String key = String.valueOf(i);
			LDAPEntry currentEntry = (LDAPEntry)(((Hashtable)(
				_htSuffixAndBackends.get(key))).get(ENTRY_KEYWORD));
			DN dn1 = new DN(replicaEntry.getDN());
			DN dn2 = new DN(currentEntry.getDN());
			if (dn1.equals(dn2)) {
				_theChoice.setSelectedIndex(i);
				return true;
			}
		}
		return false;
	}

	protected void init() {
		setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = getGBC();
		
		gbc.anchor = gbc.EAST;
		gbc.gridwidth = gbc.RELATIVE;
		add(_label, gbc);
		
		gbc.weightx = 1;
		_theChoice = UIFactory.makeJComboBox(null, null);
		_theChoice.setEditable(false);
		add(_theChoice, gbc);

		try {
			// returns either a vector of vectors of LDAPEntry objects or
			// a vector of LDAPEntry objects
			boolean getMappingTreeNode = true;
			String LDAPFilter = null;
			Vector v = DSUtil.getLDBMInstanceList(_ldc, getMappingTreeNode,
												  LDAPFilter);
			Debug.println(8, "ReplicaPanel.init: ldbm instance list size = " +
						  ((v == null) ? 0 : v.size()));
			if (v != null && v.size() > 0) {
				for (Enumeration e = v.elements(); e.hasMoreElements();) {
					Object o = e.nextElement();
					LDAPEntry lde = null;
					String replicaDN = null;
					if (o instanceof Vector) {
						Vector vv = (Vector)o;
						lde = (LDAPEntry)vv.elementAt(0);
						replicaDN = ReplicationTool.REPLICA_RDN + "," +
							((LDAPEntry)vv.elementAt(1)).getDN();
					} else { // must be an LDAPEntry
						lde = (LDAPEntry)o;
						replicaDN = ReplicationTool.REPLICA_RDN + "," +
							lde.getDN();
					}
					LDAPEntry replicaEntry = null;
					try {
						replicaEntry = _ldc.read(replicaDN);
					} catch (LDAPException ignore) {
						Debug.println(6, "ReplicaPanel.init: could not read dn = " +
									  replicaDN + ": " + ignore);
						replicaEntry = null;
					}
					if (replicaEntry != null) {
						String suffix = DSUtil.getAttrValue(lde, SUFFIX_ATTR);
						String backend = DSUtil.getAttrValue(lde, BACKEND_ATTR);
						/* Here we make the choice of what we show: the instance name, the
						   suffix, a combination of both...*/
						//_theChoice.addItem(backendString+SEPARATOR+suffixString);
						/* In this implementation we show just the suffix*/
						_theChoice.addItem(suffix);
						if (_htSuffixAndBackends == null) {
							_htSuffixAndBackends = new Hashtable();
						}
						Hashtable ht = new Hashtable();
						ht.put(SUFFIX_ATTR, suffix);
						ht.put(BACKEND_ATTR, backend);
						ht.put(ENTRY_KEYWORD, replicaEntry);
						String key = ""+(_theChoice.getItemCount()-1);
						_htSuffixAndBackends.put(key, ht);
					} else {
						Debug.println(8, "ReplicaPanel.init: there is no replica entry " +
									  "for " + replicaDN);
					}
				}
			}
		} catch (LDAPException e) {
			Debug.println( "ReplicaPanel.init: could not load backend instances " +
						   "from server " + DSUtil.format(_ldc) + " exception: " +
						   e);
			e.printStackTrace();
		}
	}

	// the hashtable key for the mapping tree entry dn
	static private String ENTRY_KEYWORD = "entry";
}

