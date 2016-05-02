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
package com.netscape.admin.dirserv.account;

import java.util.*;
import java.util.zip.CRC32;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import netscape.ldap.*;
import netscape.ldap.util.*;
import com.netscape.admin.dirserv.DSUtil;
import com.netscape.admin.dirserv.panel.MappingUtils;
import com.netscape.admin.dirserv.DSContentModel;
import com.netscape.admin.dirserv.task.*;
import com.netscape.management.client.util.Debug;
import com.netscape.management.client.ug.ResourcePageObservable;
import com.netscape.admin.dirserv.IDSModel;

public class AccountInactivation extends Object {
	public AccountInactivation(LDAPEntry entry) {
		_entry = entry;
		if (entry != null) {
			_dn = entry.getDN();
		}
	}

	public AccountInactivation(ResourcePageObservable observable) {
		 _observable = observable;
		_entry = getEntry(observable);
		if (_entry != null) {
			_dn = _entry.getDN();
		}
	}

	/**
	 * Tells the state of the entry.
	 *
	 * @param ldc	LDAP connection to access the directory
	 * @return ACTIVATED if the entry is not locked.
	 * @return INACTIVATED if the entry is locked with server mechanics.
	 * @return INACTIVATED_THROUGH_UNKNOWN_MECHANICS if the entry is locked using different methods from the server's.
	 * @exception LDAPException
	 */
	public int getState(LDAPConnection ldc) throws LDAPException {
		if (!isLocked(ldc)) {			
			return ACTIVATED;
		}
		if (_throughRoles == null) {
			updateThroughRoles(ldc);
		}
		/* We found the roles that lock the entry??*/
		if ((_throughRoles!=null) && (_throughRoles.size() > 0)) {
			return INACTIVATED;
		} else {
			return INACTIVATED_THROUGH_UNKNOWN_MECHANICS;
		}
	}

	/**
	 * Tells what can be done with this entry in terms of activation/inactivation operations.
	 *
	 * @param ldc	LDAP connection to access the directory
	 * @return CAN_BE_ACTIVATED if the entry is locked.
	 * @return CAN_BE_INACTIVATED if the entry is activated.
	 * @return CANNOT_BE_ACTIVATED_INACTIVATED entry if it can't be activated/inactivated
	 * @exception LDAPException
	 */
	public int operationAllowed(LDAPConnection ldc) throws LDAPException {
		int operationAllowed = CANNOT_BE_ACTIVATED_INACTIVATED;
		if (!isLocked(ldc)) {			
			if (_suffix == null) {
				updateSuffixAndRoleDNs(ldc);								
			} 	
			if ((_suffix!=null) && (_suffix.length() > 0)) {
				operationAllowed = CAN_BE_INACTIVATED;
			}
		} else {			
			if (_throughRoles == null) {
				updateThroughRoles(ldc);
			}			
			/* We found the roles that lock the entry??*/
			if ((_throughRoles!=null) && (_throughRoles.size() > 0)) {
				operationAllowed =  CAN_BE_ACTIVATED;
			}
		}		
		return operationAllowed;
	}



	/** 
	 * Returns in a Vector the dns of the roles locking a role.
	 *    
	 * @return null If there is no Role locking the role.
	 * @return empty Vector If the role is directly locked (only locked through nsDisabledRole) 
	 * @return Vector with locking roles If the role is locked through roles other than nsDisabledRoles
	 * @param ldc LDAP connection to access the directory
	 * @exception LDAPException
	 */
	
	public Vector getLockingRolesForRole(LDAPConnection ldc) throws LDAPException {
		if (_throughRoles == null) {
			updateThroughRolesForRole(ldc);
		}
		if (_suffix == null) {
			updateSuffixAndRoleDNs(ldc);
		}
		if ((_throughRoles == null) || (_suffix == null)) {
			return null;
		} else {
			Vector vector = new Vector();
			DN dn1 = new DN(_nsDisabledRole);
			for (int i=0; i<_throughRoles.size(); i++) {
				String lockingRole = (String)_throughRoles.elementAt(i);
				DN dn2 = new DN(lockingRole);
				if (!dn1.equals(dn2)) {
					vector.addElement(lockingRole);
				}
			}	
			return vector;
		}
	}

	/** 
	 * Returns in a Vector the dns of the roles locking an entry with userpassword.
	 *
	 * @return null If there is no Role locking the role.
	 * @return empty Vector If the entry is directly locked (only locked through nsManagedDisabledRole and nsDisabledRole).
	 * @return Vector with locking roles If the entry is locked through roles other than nsManagedDisabledRole and nsDisabledRole.
	 * @param ldc LDAP connection to access the directory
	 * @exception LDAPException
	 */
	
	public Vector getLockingRolesForEntry(LDAPConnection ldc) throws LDAPException {
		if (_throughRoles == null) {
			updateThroughRolesForEntry(ldc);
		}
		if (_suffix == null) {
			updateSuffixAndRoleDNs(ldc);
		}
		if ((_throughRoles == null) || (_suffix == null)) {
			return null;
		} else {
			Vector vector = new Vector();
			DN dn1 = new DN(_nsManagedDisabledRole);
			for (int i=0; i<_throughRoles.size(); i++) {
				String lockingRole = (String)_throughRoles.elementAt(i);
				DN dn2 = new DN(lockingRole);
				if (!dn1.equals(dn2)) {
					vector.addElement(lockingRole);
				}
			}
			return vector;
		}
	}

	/** 
	 * Returns in a Vector the dns of the roles locking an entry.
	 *
	 * @return null IIf there is no Role locking the role.
	 * @return empty Vector If the entry is directly locked (only locked through nsManagedDisabledRole and nsDisabledRole).
	 * @return Vector with locking roles If the entry is locked through roles other than nsManagedDisabledRole and nsDisabledRole.
	 * @param ldc LDAP connection to access the directory
	 */
	public Vector getLockingRoles(LDAPConnection ldc) throws LDAPException {	
		if (isRole()) {
			return getLockingRolesForRole(ldc);
		} else if (hasUserPassword()) {
			return getLockingRolesForEntry(ldc);
		}
		return null;
	}
	


	/**
	 * Tells if the entry can be activated (supposing we used server mechanics)
	 *
	 * @param ldc	LDAP connection to access the directory
	 * @return true if the entry can be inactivated.  If it is not a role and has no userpassword it returns false
	 * @exception LDAPException
	 */
	public boolean canBeActivated(LDAPConnection ldc) throws LDAPException {
		if (isRole()) {
			return canRoleBeActivated(ldc);
		}
		if (hasUserPassword()) {
			return canEntryBeActivated(ldc);
		}
		return false;
	}

	/**
	 * Tells if the entry with userPassword can be activated (supposing we used server mechanics)
	 *
	 * @param ldc	LDAP connection to access the directory 
	 * @return true if the entry can be inactivated
	 * @exception LDAPException
	 */

	public boolean canEntryBeActivated(LDAPConnection ldc) throws LDAPException {
		if (!isLocked(ldc)) {
			return true;
		}
		if (_throughRoles == null) {
			updateThroughRolesForEntry(ldc);
		}
		if ((_throughRoles != null) && (_throughRoles.size() == 1)) {
			DN dn1 = new DN((String)_throughRoles.elementAt(0));
			DN dn2 = new DN(_nsManagedDisabledRole);
			if (dn1.equals(dn2)) {
				return true;
			} 
		}
		return false;
	}


	/**
	 * Tells if the role can be activated (supposing we used server mechanics)
	 *
	 * @param ldc	LDAP connection to access the directory 
	 * @return true if the role can be inactivated
	 * @exception LDAPException
	 */

	public boolean canRoleBeActivated(LDAPConnection ldc) throws LDAPException {
		if (!isLocked(ldc)) {
			return true;
		}
		if (_throughRoles == null) {
			updateThroughRolesForRole(ldc);
		}
		if ((_throughRoles != null) && (_throughRoles.size() == 1)) {
			DN dn1 = new DN((String)_throughRoles.elementAt(0));
			DN dn2 = new DN(_nsDisabledRole);
			if (dn1.equals(dn2)) {
				return true;
			} 
		}
		return false;
	}



	/**
	 * Tells if the entry can be inactivated (supposing we use server mechanics) 	 
	 * @return true if the entry can be inactivated
	 */
	public boolean canBeInactivated() {
		if ( !isRole() && !hasUserPassword()) {
			return false;
		}
		return true;
	}



	/**
	 * Check if the entry has been locked
	 * @param ldc	LDAP connection to access the directory
	 * @return true if the entry is locked 
	 * @exception LDAPException
	 */	  
	public boolean isLocked(LDAPConnection ldc) throws LDAPException {
		if (isRole()) {			
			if (_throughRoles == null) {
				updateThroughRolesForRole(ldc);
			}
			if ((_throughRoles!=null) && (_throughRoles.size() > 0)) {
				Debug.println(0, "AccountInactivation.isLocked(): entry " + _dn + " belongs to locking roles " + _throughRoles.toString());				
				return true;
			} else if (_nsDisabledRole != null) {
				DN dn1 = new DN(_entry.getDN());
				DN dn2 = new DN(_nsDisabledRole);
				if (dn1.equals(dn2)) {
					return true;
				}
			}
		} else if (hasUserPassword()) {
			if (_entry != null) {
				LDAPAttribute accountLockAttribute = _entry.getAttribute(ACCOUNT_LOCK);		
				if ( accountLockAttribute != null ) {					
					Enumeration e = accountLockAttribute.getStringValues();
					String lock = (String)e.nextElement();
					if ((lock!=null) && (lock.equalsIgnoreCase(LOCKED))) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
		
		return false;
	}

	/**
	  * Check if the roles and cos for account inactivation for the suffix of the entry are created
	  * @return true if ALL the roles and COS needed are created
	  * @param ldc	LDAP connection to access the directory
	  * @exception LDAPException
	  */

	public boolean isLockingInfrastructureCreated (LDAPConnection ldc ) throws LDAPException {
		if (_suffix == null) {
			updateSuffixAndRoleDNs(ldc);
		}
		if (_suffix == null) {
			return false;
		}
		String[] dnSet = {"cn=nsManagedDisabledRole,"+_suffix,
						  "cn=nsDisabledRole,"+_suffix,
						  "cn=nsAccountInactivationTmp,"+_suffix,
						  "cn=\"cn=nsDisabledRole,"+_suffix+"\",cn=nsAccountInactivationTmp,"+_suffix,
						  "cn=nsAccountInactivation_cos,"+_suffix};
		
		LDAPSearchConstraints cons =
			(LDAPSearchConstraints)ldc.getSearchConstraints().clone();
		LDAPEntry entry;
		for (int i=0; i< dnSet.length; i++) {
			try {
				entry = DSUtil.readEntry(ldc, dnSet[i], null, cons);
				if (entry == null) {
					return false;
				} 
			} catch (LDAPException e) {
				if (e.getLDAPResultCode() != LDAPException.NO_SUCH_OBJECT) {
					Debug.println("AccountInactivation.isLockingInfrastructureCreated() "+e);
					throw e;
				}
				return false;
			}
		}
		return true;
	}


	/**
	  * Create  the roles and cos for account inactivation for the suffix of the entry are created
	  * @return true if ALL the roles and COS needed are created, false otherwise
	  * @param ldc	LDAP connection to access the directory
	  * @exception LDAPException
	  */

	public boolean createLockingInfrastructure (LDAPConnection ldc ) throws LDAPException {
		if (_suffix == null) {
			updateSuffixAndRoleDNs(ldc);
		}
		if (_suffix == null) {
			return false;
		}
		
		String[] dnSet = {"cn=nsManagedDisabledRole,"+_suffix,
						  "cn=nsDisabledRole,"+_suffix,
						  "cn=nsAccountInactivationTmp,"+_suffix,
						  "cn=\"cn=nsDisabledRole,"+_suffix+"\",cn=nsAccountInactivationTmp,"+_suffix,
						  "cn=nsAccountInactivation_cos,"+_suffix};
		
		String[] attrs1 = {"top", "ldapsubentry", "nsroledefinition", "nssimpleroledefinition", "nsmanagedroledefinition"};
		LDAPAttribute[] attributesRole1 = {new LDAPAttribute("objectclass", 
															 attrs1),
										   new LDAPAttribute("cn",
															 "nsManagedDisabledRole")};

		String[] attrs2 = {"ldapsubentry", "nsroledefinition", "nscomplexroledefinition", "nsnestedroledefinition"};
		LDAPAttribute[] attributesRole2 = {new LDAPAttribute("objectclass", 
															 attrs2),
										   new LDAPAttribute("nsroledn", "cn=nsmanageddisabledrole,"+_suffix),
										   new LDAPAttribute("cn",
															 "nsDisabledRole") };
		String[] attrs3 = {"top", "nscontainer"};
		LDAPAttribute[] attributesCos1 = { new LDAPAttribute("objectclass", 
															 attrs3)};
		
		String[] attrs4 = {"top", "ldapsubentry", "extensibleobject", "costemplate" };
		LDAPAttribute[] attributesCos2 = { new LDAPAttribute("objectclass", 
															 attrs4),
										   new LDAPAttribute("nsaccountlock",
															 "true"),
		                                   new LDAPAttribute("cospriority", 
															 "1")};

		String[] attrs5 = {"top", "ldapsubentry", "cossuperdefinition", "cosclassicdefinition"};
		LDAPAttribute[] attributesCos3 = { new LDAPAttribute("objectclass", 
															 attrs5),
		                                   new LDAPAttribute("costemplatedn", 
															 "cn=nsAccountInactivationTmp,"+_suffix),
										   new LDAPAttribute("cosspecifier",
															 "nsrole"),
		                                   new LDAPAttribute("cosattribute",
															 "nsaccountlock operational")};		
						
		LDAPAttributeSet[] attributeSets = {new LDAPAttributeSet(attributesRole1),
											new LDAPAttributeSet(attributesRole2),
											new LDAPAttributeSet(attributesCos1),
											new LDAPAttributeSet(attributesCos2),
											new LDAPAttributeSet(attributesCos3)};
		
		boolean somethingAdded = false;
		_lockingInfrastructureDNs = new Vector();
		for (int i=0; i< dnSet.length; i++) {
			try {
				LDAPEntry entry = new LDAPEntry(dnSet[i], attributeSets[i]);	
				ldc.add(entry);
				somethingAdded = true;
				_lockingInfrastructureDNs.addElement(entry.getDN());
			} catch (LDAPException e) {
				if (e.getLDAPResultCode() == LDAPException.ENTRY_ALREADY_EXISTS) {
					continue;
				} else {					
					Debug.println("AccountInactivation.createLockingInfrastructure(): Error adding "+ dnSet[i]+ "\n with attribute:\n" + attributeSets[i].toString()+"\n\n"+e);
					throw e;					
				}
			}
		}
		/* We do this in order to be sure that the cos cache is updated */
		if (somethingAdded) {
			try {
				Thread.sleep(1500);
			} catch (Exception e) {				
			}
		}
		return true;
	}

	/**
	  * Method that returns the list of DNs of the entries that have been added.
	  * This entries are the roles and the Cos that create the locking structure
	  * The order of the entries in the vector is the same order used to add them.
	  */
	public Vector getLockingInfrastructureDNs() {
		return _lockingInfrastructureDNs;
	}
	

	/** 
	   Function that modifies the entry with userpassword to activate it.  It supposes we can do it and that the roles are created.
	   * @return true if the roles have been modified, false if the suffix needed to modify the entry was not found
	   * @param ldc	LDAP connection to access the directory
	   * @exception LDAPException
	   */
	
	
	public boolean modifyEntryToActivateEntry(LDAPConnection ldc) throws LDAPException {
		if (_suffix == null) {
			updateSuffixAndRoleDNs(ldc);
		}
		if (_suffix == null) {
			return false;
		}
		LDAPModification mod;
		LDAPAttribute attr;
		attr = new LDAPAttribute(NSROLEDN, _nsManagedDisabledRole);
		mod = new LDAPModification(LDAPModification.DELETE, attr);
		try {
			ldc.modify(_dn, mod);
		} catch (LDAPException e) {
			if (e.getLDAPResultCode() != LDAPException.NO_SUCH_ATTRIBUTE) {
				Debug.println("AccountInactivation.modifyEntryToActivateEntry(): "+e);
				throw e;
			}
			Debug.println(0, "AccountInactivation.modifyEntryToActivateEntry(): "+e);
		}
		return true;
	}


	/** 
	   Function that modifies the roles to activate a role.  It supposes we can do it and that the roles are created.
	   * @return true if the roles have been modified, false if the suffix needed to modify the entry was not found
	   * @param ldc	LDAP connection to access the directory
	   * @exception LDAPException
	   */		
	public boolean modifyRolesToActivateRole(LDAPConnection ldc) throws LDAPException {
		if (_suffix == null) {
			updateSuffixAndRoleDNs(ldc);
		}
		if (_suffix == null) {
			return false;
		}
		LDAPModification mod;
		LDAPAttribute attr;
		attr = new LDAPAttribute(NSROLEDN, _dn);
		mod = new LDAPModification(LDAPModification.DELETE, attr);		
		try {
			ldc.modify(_nsDisabledRole, mod);
		} catch (LDAPException e) {
			if (e.getLDAPResultCode() != LDAPException.NO_SUCH_ATTRIBUTE) {
				Debug.println("AccountInactivation.modifyRolesToActivateRole(): "+e);
				throw e;
			}
			Debug.println(0, "AccountInactivation.modifyRolesToActivateRole(): "+e);
		}
		return true;
	}


	/** 
	   Function that modifies the roles to inactivate an entry with userpassword.  It supposes we can do it and that the roles are created.
	   * @return true if the roles have been modified, false if the suffix needed to modify the entry was not found
	   * @param ldc	LDAP connection to access the directory
	   * @exception LDAPException
	   */		
	public boolean modifyEntryToInactivateEntry(LDAPConnection ldc) throws LDAPException {
		if (_suffix == null) {
			updateSuffixAndRoleDNs(ldc);
		}
		if (_suffix == null) {
			return false;
		}
		LDAPModification mod;
		LDAPAttribute attr;
		attr = new LDAPAttribute(NSROLEDN, _nsManagedDisabledRole);
		mod = new LDAPModification(LDAPModification.ADD, attr);
		try {
			ldc.modify(_dn, mod);
		} catch (LDAPException e) {
			if (e.getLDAPResultCode() != LDAPException.ATTRIBUTE_OR_VALUE_EXISTS) {
				Debug.println("AccountInactivation.modifyEntryToInactivateEntry(): "+e);
				throw e;
			}
			Debug.println(0, "AccountInactivation.modifyEntryToInactivateEntry(): "+e);
		}
		return true;
	}


	/** 
	   Function that modifies the roles to inactivate a role.  It supposes we can do it and that the roles are created.
	   * @return true if the roles have been modified, false if the suffix needed to modify the entry was not found
	   * @param ldc	LDAP connection to access the directory
	   * @exception LDAPException
	   */		
	public boolean modifyRolesToInactivateRole(LDAPConnection ldc) throws LDAPException {
		if (_suffix == null) {
			updateSuffixAndRoleDNs(ldc);
		}
		if (_suffix == null) {
			return false;
		}
		LDAPModification mod;
		LDAPAttribute attr;
		attr = new LDAPAttribute(NSROLEDN, _dn);
		mod = new LDAPModification(LDAPModification.ADD, attr);
		try {
			ldc.modify(_nsDisabledRole, mod);
		} catch (LDAPException e) {
			if (e.getLDAPResultCode() != LDAPException.ATTRIBUTE_OR_VALUE_EXISTS) {
				Debug.println("AccountInactivation.modifyRolesToInactivateRole(): "+e);				
				throw e;
			}
			Debug.println(0, "AccountInactivation.modifyRolesToInactivateRole(): "+e);
		}
		return true;
	}

	/**
	 * Function used to inactivate an entry.
	 *
	 * @param ldc	LDAP connection to access the directory
	 * @return SUCCESS if the entry gets inactivated through server mechanics
	 * @return INACTIVATED_THROUGH_UNKNOWN_MECHANICS if the entry is inactivated through unknown mechanics
	 * @return CAN_NOT_INACTIVATE if the entry can't be inactivated
	 * @return ERROR if other type of error ocurred during the inactivation of the entry
	 */
	public int inactivate(LDAPConnection ldc) {
		try {
			int state = getState(ldc);
			if (state == INACTIVATED) {
				Debug.println("AccountInactivation.inactivate(): entry "+_dn+" was already inactivated ");
				return SUCCESS;
			}
			if (state == INACTIVATED_THROUGH_UNKNOWN_MECHANICS) {
				Debug.println("AccountInactivation.inactivate(): entry "+_dn+" was already inactivated through unknown mechanics");
				return INACTIVATED_THROUGH_UNKNOWN_MECHANICS;
			}			
		} catch (LDAPException e) {
			Debug.println("AccountInactivation: inactivate() getting state of entry "+_dn+" "+e);
			return ERROR;
		}
	
		if (!canBeInactivated()) {
			Debug.println("AccountInactivation.inactivate(): entry "+_dn+" can not be inactivated: it is not a role and has no userpassword ");
			return CAN_NOT_INACTIVATE;
		}

		try {
			if (!isLockingInfrastructureCreated(ldc)) {
				createLockingInfrastructure( ldc );				
			}
		} catch (LDAPException e) {
			Debug.println("AccountInactivation: inactivate() creating locking roles for entry "+_dn+" "+e);
			return ERROR;
		}

		try {
			if (isRole()) {
				if (!modifyRolesToInactivateRole( ldc )) {
					if (_suffix == null) {
						return ROOT_OR_CONFIG_ENTRY;
					}
				}
			} else if (hasUserPassword()) {
				if (!modifyEntryToInactivateEntry( ldc )) {
					if (_suffix == null) {
						return ROOT_OR_CONFIG_ENTRY;
					}
				}				
			}
		} catch (LDAPException e) {
			Debug.println("AccountInactivation: inactivate() modifying locking roles for entry "+_dn+" "+e);
			return ERROR;
		}
		return SUCCESS;
							
	}


	/**
	 * Function used to activate an entry.
	 *
	 * @param ldc	LDAP connection to access the directory
	 * @return SUCCESS if the entry gets activated
	 * @return INACTIVATED_THROUGH_UNKNOWN_MECHANICS if the entry is inactivated through unknown mechanics
	 * @return CAN_NOT_ACTIVATE if the entry can't be activated
	 * @return ERROR if other type of error ocurred during the activation of the entry
	 */
	public int activate(LDAPConnection ldc) {
		try {
			int state = getState(ldc);
			if (state == ACTIVATED) {
				Debug.println("AccountInactivation.activate(): entry "+_dn+" was already activated ");
				return SUCCESS;
			}
			if (state == INACTIVATED_THROUGH_UNKNOWN_MECHANICS) {
				Debug.println("AccountInactivation.activate(): entry "+_dn+" is inactivated through unknown mechanics");
				return INACTIVATED_THROUGH_UNKNOWN_MECHANICS;
			}
		} catch (LDAPException e) {
			Debug.println("AccountInactivation: activate() getting state for entry "+_dn+" "+e);
			return ERROR;
		}
		try {
			if (!canBeActivated(ldc)) {
				Debug.println("AccountInactivation.activate(): entry "+_dn+" can't be activated because is locked trough roles "+getLockingRoles(ldc));
				return CAN_NOT_ACTIVATE;
			}
		} catch (LDAPException e) {
			Debug.println("AccountInactivation: activate() looking if we can activate entry "+_dn+" "+e);
			return ERROR;
		}
		
		try {
			if (isRole()) {
				if (!modifyRolesToActivateRole( ldc )) {
					if (_suffix == null) {
						return ROOT_OR_CONFIG_ENTRY;
					}
				}
			} else if (hasUserPassword()) {
				if (!modifyEntryToActivateEntry( ldc )) {
					if (_suffix == null) {
						return ROOT_OR_CONFIG_ENTRY;
					}
				}				
			}			
			return SUCCESS;
		} catch (LDAPException e) {
			Debug.println("AccountInactivation: activate() modifying locking roles for entry "+_dn+" "+e);
			return ERROR;
		}						
	}

	/**
	  * Return the LDAPEntry corresponding to the ResourcePageObservable
	  * @return LDAPEntry corresponding to the observable
	  * @param observable ResourcePageObservable we use to create the LDAPEntry
	  */
	private LDAPEntry getEntry(ResourcePageObservable observable) {
		String attribute;
		String[] values;
		Vector v;
		LDAPAttributeSet set = new LDAPAttributeSet();
		Enumeration e = observable.getAttributesList();
		while (e.hasMoreElements()) {
			attribute = (String)e.nextElement();
			v = observable.get(attribute);
			values = new String[v.size()];
			v.toArray(values);
			set.add(new LDAPAttribute(attribute, values));			
		}
		LDAPEntry entry = new LDAPEntry(observable.getDN(), set);
		return entry;
	}

	/**
	 * Check if the entry is a role
	 * @return true if the entry is a role
	 */

	private boolean isRole() {
		LDAPAttribute objectclassAttribute = _entry.getAttribute(OBJECTCLASS);
		if (objectclassAttribute!=null) {
			Enumeration e = objectclassAttribute.getStringValues();
			String value;
			boolean isRoleDefinition=false;
			boolean isLDAPSubentry=false;
			while (e.hasMoreElements()) {
				value = (String)e.nextElement();
				if (value!=null) {
					if (value.equalsIgnoreCase("nsroledefinition")) {
						isRoleDefinition=true;
					} else if (value.equalsIgnoreCase("ldapsubentry")) {
						isLDAPSubentry = true;
					}
					if (isRoleDefinition && isLDAPSubentry) {
						return true;
					}
				}
			}			
		}	     		  
		return false;				
	}


	/**
	 * Check if the entry is a user account (i.e, has a userpassword) 
	 * @return true if the user has a user password
	 */
	private boolean hasUserPassword() {

//
// elp : 24/08/2000
// I disable hasUserPassword() temporarily to allow
// activating / inactivating on entry which have not
// a user password.
//
/*
		LDAPAttribute userPasswordAttribute = _entry.getAttribute(USERPASSWORD);		
		if ( userPasswordAttribute != null ) {					
			Enumeration e = userPasswordAttribute.getStringValues();
			String userPassword = (String)e.nextElement();
			if ((userPassword!=null) && (!userPassword.equals(""))) {
				return true;
			} else {
				return false;
			}
		}
		return false;
*/
		return true;
	}						   

	/**
	 * Finds the suffix of the partition for the entry.  With it it gets the names of the locking roles.
	 * Updates members _suffix, _nsDisabledRole, _nsManagedDisabledRole
   	 * @param ldc LDAP connection to access the directory
	 */
	private void updateSuffixAndRoleDNs(LDAPConnection ldc) {		
		String suffix = null;
		/* We get the suffix of the parent:  the roles are defined under the backend root where the parent is contained */
        
		if (_observable != null && _observable.isNewUser()) {
			_entry = getEntry(_observable);
			if (_entry != null) {
				_dn = _entry.getDN();
			}
		}
        
		DN entryDN = new DN(_dn);
		DN parentDN = entryDN.getParent();

		if (parentDN != null) {
			String parent = parentDN.toString();
			if (parent != null) {
				if (!parent.equals("")) {
					suffix = MappingUtils.getTopSuffixForEntry(ldc, _dn);
				}
			}
		}

		if ((suffix == null) || ! (entryDN.isDN(suffix))) { 
			Debug.println("AccountInactivation(): could not get the suffix of the entry "+_dn); 
		} else { 
			DN suffixDN = new DN(suffix); 
			
			/* If it is a root entry, we can't inactivate/activate the entry... better to let _suffix be null (we handle this case
			 for the entries of cn=config, etc. */
			if (suffixDN.equals(entryDN)) {
				suffix = null;
			}
		}

		_suffix = suffix;

		_nsDisabledRole = "cn=nsdisabledrole,"+_suffix;
		_nsManagedDisabledRole = "cn=nsmanageddisabledrole,"+_suffix;
	}

	/**
	 * This method updates the member _throughRoles (puts the roles that make the entry to be inactivated if any).
	 *
	 * @param ldc LDAP connection to access the directory
	 * @exception LDAPException
	 */
	private void updateThroughRoles(LDAPConnection ldc) throws LDAPException {
		if (isRole()) {
			updateThroughRolesForRole(ldc);
		} else if (hasUserPassword()) {
			updateThroughRolesForEntry(ldc);
		}
	}

	/**
	 * This method updates the member _throughRoles for an entry (puts the roles that make the entry to be inactivated if any).
	 *
	 * @param ldc LDAP connection to access the directory
	 * @exception LDAPException
	 */
	private void updateThroughRolesForEntry(LDAPConnection ldc) throws LDAPException {
		_throughRoles=null;			
		
		if (_suffix == null) {
			updateSuffixAndRoleDNs(ldc);
		}
		
		LDAPEntry nsDisabledRoleEntry;
		try {			
			if ((_nsDisabledRole !=null) && (ldc != null)) {
				LDAPSearchConstraints cons =
					(LDAPSearchConstraints)ldc.getSearchConstraints().clone();
				nsDisabledRoleEntry	= DSUtil.readEntry(ldc, _nsDisabledRole, ATTRS, cons);
				if (nsDisabledRoleEntry == null) {
					Debug.println("AccountInactivation.updateThroughRolesForEntry(): could not read the nsDisabledRoleEntry "+_nsDisabledRole);
					return;		
				}	
			} else {
				return;
			}		   					
			
			/* We compare the nsrole attribute of the entry with the nsRoleDN attribute of the nsDisabledRole*/
			Vector nsRoleValues = getAttributeValues(_entry, NSROLE);				
			Vector nsRoleDNValues = getAttributeValues(nsDisabledRoleEntry, NSROLEDN);
			
			if ((nsRoleValues == null) || (nsRoleDNValues == null)) {
				Debug.println("AccountInactivation.updateThroughRolesForEntry(): the entry "+_dn+" has no nsrole or the role has no nsroledn");
				return;
			}
			
			for (int i=0; i< nsRoleValues.size(); i++) {
				DN dn1 = new DN((String)nsRoleValues.elementAt(i));
				for (int j=0; j< nsRoleDNValues.size(); j++) {
					DN dn2 = new DN((String)nsRoleDNValues.elementAt(j));
					if (dn1.equals(dn2)) {
						if (_throughRoles==null) {
							_throughRoles = new Vector();
						}
						_throughRoles.addElement(nsRoleDNValues.elementAt(j));
					}
				}
			}
		} catch (LDAPException e) {
			if (e.getLDAPResultCode() != LDAPException.NO_SUCH_OBJECT) {
				Debug.println("AccountInactivation.updateThroughRolesForEntry(): "+e);
				throw e;
			}
		}
	}


	/**
	 * This method updates the member _throughRoles for a role (puts the roles that make the entry to be inactivated if any).
	 *
	 * @param ldc LDAP connection to access the directory
	 * @exception LDAPException
	 */
	private void updateThroughRolesForRole(LDAPConnection ldc) throws LDAPException {
		_throughRoles=null;			
		
		if (_suffix == null) {
			updateSuffixAndRoleDNs(ldc);
		}
		
		LDAPEntry nsDisabledRoleEntry;
		try {			
			if ((_nsDisabledRole !=null) && (ldc != null)) {
				LDAPSearchConstraints cons =
					(LDAPSearchConstraints)ldc.getSearchConstraints().clone();
				nsDisabledRoleEntry	= DSUtil.readEntry(ldc, _nsDisabledRole, ATTRS, cons);
				if (nsDisabledRoleEntry == null) {
					Debug.println("AccountInactivation.updateThroughRolesForRole(): could not read the nsDisabledRoleEntry "+_nsDisabledRole);
					return;		
				}	
			} else {
				return;
			} 		
			lookForRolesLockingRole(_dn, nsDisabledRoleEntry, ldc);
		} catch (LDAPException e) {
			if (e.getLDAPResultCode() != LDAPException.NO_SUCH_OBJECT) {
				Debug.println("AccountInactivation.updateThroughRolesForRole(): "+e);
				throw e;
			}
		}
	}


	/** Function used to update _throughroles for the roles.
	 * This function takes a role entry (roleParent) and sees if the entry (which is supposed to be a role)
	 * is contained in this role (its value is one of the values of the nsroledn attribute of the role parent)
	 *
	 * @param LDAPEntry entry: is the LDAPEntry we are analyzing
	 * @param LDAPEntry roleParent: the LDAPEntry corresponding to the role parent
	 * @param ldc	LDAP connection to access the directory
	 * @exception LDAPException
	 */

	private void lookForRolesLockingRole(String entry, LDAPEntry roleParent, LDAPConnection ldc) throws LDAPException {
		if (roleParent == null) {
			return;
		}

		Vector nsRoleDNValues = getAttributeValues(roleParent, NSROLEDN);	
				
		if (nsRoleDNValues == null) {
			return;
		}
		DN dnRole = new DN(entry);
		
		/* Check if the entry (the role we are dealing with) is in the scope of the roleParent (the role that may be
		   the nested role containing the entry).  To be in the scope of the role, the roleParent has to be defined ABOVE
		   or at the same level of the role we are dealing with */		
		DN dnRoleParent = new DN(roleParent.getDN());
		if (!dnRole.isDescendantOf(dnRoleParent.getParent())) {
			return;
		}
		for (int i=0; i< nsRoleDNValues.size(); i++) {
			DN dnRoleDN = new DN((String)nsRoleDNValues.elementAt(i));
			if (dnRoleDN.equals(dnRole)) {
				if (_throughRoles==null) {
					_throughRoles = new Vector();
				}				
				_throughRoles.addElement(roleParent.getDN());	
			} else {
				try {
					if ((nsRoleDNValues.elementAt(i) !=null) && (ldc != null)) {
						LDAPSearchConstraints cons =
							(LDAPSearchConstraints)ldc.getSearchConstraints().clone();
						LDAPEntry nsRoleDNEntry	= DSUtil.readEntry(ldc, (String)nsRoleDNValues.elementAt(i), ATTRS, cons);
						if (nsRoleDNEntry != null) {						
							lookForRolesLockingRole(entry, nsRoleDNEntry, ldc);
						}
					}										
				} catch (LDAPException e) {
					if (e.getLDAPResultCode() != LDAPException.NO_SUCH_OBJECT) {
						Debug.println("AccountInactivation.lookForRolesLockingRole(): "+e);
						throw e;
					}
				}
			}
		}
	}

	private Vector getAttributeValues(LDAPEntry entry, String attr) {
		Vector values = new Vector();
		LDAPAttribute ldapAttribute = entry.getAttribute(attr);
		if (ldapAttribute != null) {
			Enumeration e = ldapAttribute.getStringValues();			
			while (e.hasMoreElements()) {				
				values.addElement(e.nextElement());
			}
		}
		if (values.isEmpty()) {
			return null;
		}
		return values;
	}


						   

/*
	public int test(DSContentModel model) {
		TestAccountInactivation test = new TestAccountInactivation(model);
		test.run();
		TestAccountInactivationQA testQA = new TestAccountInactivationQA(model);
		return testQA.run();
	}
*/




	String _dn;
	LDAPEntry _entry;
	int _initResult;
    ResourcePageObservable _observable;

	private String _suffix;

	private Vector _throughRoles;
	private String _nsManagedDisabledRole;
	private String _nsDisabledRole;

	Vector _lockingInfrastructureDNs;

	public final static int SUCCESS = 0;
	public final static int ERROR = 1;
	public final static int CAN_NOT_ACTIVATE = 2;
	public final static int CAN_NOT_INACTIVATE = 3;
	public final static int INACTIVATED_THROUGH_UNKNOWN_MECHANICS = 4;
    public final static int ACTIVATED = 5;
    public final static int INACTIVATED = 6;
	public final static int ROOT_OR_CONFIG_ENTRY = 7;

	public final static int CAN_BE_ACTIVATED = 8;
	public final static int CAN_BE_INACTIVATED = 9;
	public final static int CANNOT_BE_ACTIVATED_INACTIVATED = 10;

	private final String DN = "dn";
  	private final String CN = "cn";
	private final String USERPASSWORD = "userpassword";
	private final String NSROLE = "nsrole";
	private final String NSROLEDN = "nsroledn";
	private final String OBJECTCLASS = "objectclass";
	private final String ACCOUNT_LOCK = "nsAccountLock";
	private final String LOCKED = "true";

	
	private final String SUFFIX_ATTR = "nsslapd-suffix";
	private final String BACKEND_ATTR = "cn";	
	String[] ATTRS = {CN, USERPASSWORD, NSROLE, NSROLEDN, OBJECTCLASS, ACCOUNT_LOCK};

}

