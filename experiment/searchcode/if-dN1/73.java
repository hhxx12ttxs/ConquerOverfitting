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

package com.netscape.admin.dirserv.panel;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import com.netscape.management.client.util.*;
import com.netscape.admin.dirserv.*;
import com.netscape.admin.dirserv.task.ListDB;

import netscape.ldap.*;
import netscape.ldap.util.*;


public class MappingUtils {

    private MappingUtils() {}

    /**
     * The mapping node name is the suffix DN.  In the mapping tree entry,
     * the values of the cn attribute hold the suffix DN.  There are usually two
     * values - the quoted or escaped value, and the unquoted value.  We look
     * for the first unquoted value and use that for the node name.  If for some
     * reason we could not find the cn attribute or a suitable value, assume the
     * suffix is the value of the leftmost RDN in the entry DN.
     * @param mnent - the mapping tree node entry
     * @return the suffix to use as the name of the mapping tree node
     */
    static public String getMappingNodeName(LDAPEntry mnent) {
	String cnvals[] = DSUtil.getAttrValues(mnent, "cn");
	String suffixval = null;
	for (int ii = 0; (cnvals != null) && (ii < cnvals.length); ++ii) {
	    if (!DSUtil.DNUsesLDAPv2Quoting(cnvals[ii])) {
	        suffixval = DSUtil.unEscapeDN(cnvals[ii]);
	        break;
	    }
	}
	if (suffixval == null) {
	    suffixval = DSUtil.unEscapeRDNVal(LDAPDN.explodeDN(mnent.getDN(),true)[0]);
	}
	return suffixval;
    }
    
    /**
     * Given a mapping tree node entry, return the suffix that this represents.
     * @param mnent - the mapping tree node entry
     * @return the suffix represented by this mapping tree node
     */
    static public String getMappingNodeSuffix(LDAPEntry mnent) {
	return getMappingNodeName(mnent);
    }
    
    static public String getSuffixSearchFilter(String suffix, String attrname) {
	String normsuffix = LDAPDN.normalize(suffix);
	return "(|(" + attrname + "=" + suffix + ")(" + attrname + "=" + Quote(suffix) + ")(" +
		attrname + "=" + normsuffix + ")(" + attrname + "=" + Quote(normsuffix) + "))";
    }

    static public String getSuffixSearchFilter(String suffix) {
	return getSuffixSearchFilter(suffix, "cn");
    }
    static public String getParentSearchFilter(String suffix) {
	return getSuffixSearchFilter(suffix, "nsslapd-parent-suffix");
    }

    static public String[] getMappingNode(LDAPConnection ldc ) {
	Vector v = new Vector(1);

	// Search Mapping node  
	try {			
	    LDAPSearchResults res =
		ldc.search( CONFIG_MAPPING,
			    ldc.SCOPE_ONE,
			    "objectclass=nsMappingTree",
			    null,
			    false );
	    while( res.hasMoreElements() ) {
		LDAPEntry bentry = (LDAPEntry)res.nextElement();
		String name = getMappingNodeSuffix(bentry);
		v.addElement(name);
	    }
	} catch ( LDAPException e ) {
	    Debug.println(0, "NewMappingNodePanel.getMappingNode() " + e );
	}
	String[] mapList = new String[v.size()];
	v.toArray(mapList);
	return mapList;
    }

    static public String Quote( String line ) {
	if ((line == null) || (line.length() < 1)) {
	    return "\"\"";
	}
	StringBuffer quoted_line = new StringBuffer();

	Character deb = new Character( line.charAt(0) );
	Character tag = new Character( '"' );

	if ( deb.compareTo(tag) ==  0) {
	    quoted_line.append( line );
	} else {
	    quoted_line.append('"');
	    quoted_line.append( line );
	    quoted_line.append('"');
	}
	return(quoted_line.toString());
    }

    static public String[] getBackendList(LDAPConnection ldc) {
	if (ldc == null) {
	    return null;
	}
	return getBackendList(ldc, ALL);		
    }

    static public String[] getBackendList(LDAPConnection ldc, int backendType) {
	if (ldc == null) {
	    return null;
	}
	Vector v = new Vector(1);
	String baseDN = "";
	int scope = ldc.SCOPE_SUB;;
	String filter = "";
	if (backendType==CHAINING) {
	    baseDN = CHAINING_CONFIG_BASEDN;
	    filter = "objectclass=nsBackendInstance";
	} else if (backendType==LDBM) {
	    baseDN = LDBM_CONFIG_BASEDN;			
	    filter = "objectclass=nsBackendInstance";
	} else if (backendType==ALL) {
	    baseDN = "";
	    scope = ldc.SCOPE_BASE;
	    filter = "objectclass=*";
	}
  
	try {
	    String[] attrs = {SUFFIX_ATTR, BACKEND_ATTR};
	    LDAPSearchResults res =
		ldc.search( baseDN,
			    scope,
			    filter,
			    attrs,
			    false );
	    while( res.hasMoreElements() ) {
		LDAPEntry bentry = (LDAPEntry)res.nextElement();
		if (backendType == ALL) {
		    String[] values = DSUtil.getAttrValues(bentry, BACKEND_ATTR);
		    if (values != null) {
			for (int i=0; i<values.length; i++) {
			    int index = values[i].indexOf(':');
			    if (index > 0) {
				String backend = values[i].substring(0, index);
				v.addElement(backend);
			    }
			}
		    }					
		} else {
		    String name = bentry.getDN();
		    v.addElement(LDAPDN.explodeDN(bentry.getDN(),true)[0]);
		}				
	    }
	} catch ( LDAPException e ) {
	    Debug.println(0, "NewMappingNodePanel.getBackendList() " + e );
	} 
	Debug.println("MappingUtils.getBackendList() " +v);
	String[] bckList = new String[v.size()];
	v.toArray(bckList);
	return bckList;
    }


    static public String[] getOrderedSuffixList(LDAPConnection ldc) {
	return getOrderedSuffixList(ldc, ALL);
    }

    static public String[] getOrderedSuffixList(LDAPConnection ldc, int suffixType) {
	if (ldc == null) {
	    return null;
	}

	String[] suffixes = getSuffixList(ldc, suffixType);
	if (suffixes == null) {
	    return null;
	}
	orderSuffixes(suffixes);
	return suffixes;
    }

    /* Uses the bubble method to order the suffixes (the longest goes first)*/

    static public void orderSuffixes(String[] suffixes) {
	if (suffixes==null) {
	    return;
	}

	for (int i = 0; i < suffixes.length-1; i++) {
	    DN dn1 = new DN(suffixes[i]);
	    for (int j = i+1; j < suffixes.length; j++) {
		DN dn2 = new DN(suffixes[j]);
                if( dn2.isDescendantOf(dn1)) {
		    String t = suffixes[i];
		    suffixes[i] = suffixes[j];
		    suffixes[j] = t;
		    dn1 = new DN(suffixes[i]);
		}
	    }
	}
    }


    static public boolean isEntryInSuffixes(String dn, String[] orderedSuffixes, LDAPConnection ldc) {
	return isEntryInSuffixes(dn, orderedSuffixes, getOrderedSuffixList(ldc, ALL));
    }

    static public boolean isEntryInSuffixes(String dn, String[] orderedSuffixes, String[] allOrderedSuffixes) {
	if ((orderedSuffixes == null) || (allOrderedSuffixes == null) || (dn==null)) {
	    return false;
	}

	DN entryDN = new DN(dn);
	DN suffixDN = null;
	DN currentSuffix = null;		
				

	for (int i=0; i<orderedSuffixes.length; i++) {
	    currentSuffix = new DN(orderedSuffixes[i]);
	    if (entryDN.equals(currentSuffix) || entryDN.isDescendantOf(currentSuffix)) {
		suffixDN = currentSuffix;				
		break;
	    }
	}
		
	if (suffixDN==null) {
	    return false;
	}

	for (int i=0; i<allOrderedSuffixes.length; i++) {			
	    currentSuffix = new DN(allOrderedSuffixes[i]);
	    if (entryDN.equals(currentSuffix) || entryDN.isDescendantOf(currentSuffix)) {				
		if (currentSuffix.equals(suffixDN)) {
		    return true;
		}
		break;
	    }
	}	
	return false;			
    }

    static public String[] getSuffixList(LDAPConnection ldc) {
	return getSuffixList(ldc, ALL);
    }

    static public String[] getSuffixList(LDAPConnection ldc, int suffixType) {
	if (ldc == null) {
	    return null;
	}

	Vector v = new Vector(1);
	String baseDN = "";
	int scope = ldc.SCOPE_SUB;
	String filter = "";
	if (suffixType==CHAINING) {
	    baseDN = CHAINING_CONFIG_BASEDN;			
	    filter = "objectclass=nsBackendInstance";
	} else if (suffixType==LDBM) {
	    baseDN = LDBM_CONFIG_BASEDN;			
	    filter = "objectclass=nsBackendInstance";
	} else if (suffixType==ALL) {
	    /* We are looking for all the backends: we look in the root entry */
	    baseDN = "";
	    scope = ldc.SCOPE_BASE;
	    filter = "objectclass=*";
	}
	try {
	    String[] attrs = {SUFFIX_ATTR, BACKEND_ATTR};
	    LDAPSearchResults res =
		ldc.search( baseDN,
			    scope,
			    filter,
			    attrs,
			    false );
	    while( res.hasMoreElements() ) {
		LDAPEntry entry = (LDAPEntry)res.nextElement();
		if (suffixType == ALL) {
		    String[] values = DSUtil.getAttrValues(entry, BACKEND_ATTR);
		    if (values != null) {					
			for (int i=0; i< values.length; i++) {
			    int index = values[i].indexOf(':');
			    if (index > 0) {
				String suffix = values[i].substring(index+1);
				v.addElement(suffix);
			    }
			}			
		    }		
		} else {
		    String suffix = DSUtil.getAttrValue(entry, SUFFIX_ATTR);
		    v.addElement(suffix);
		}
	    }
	} catch ( LDAPException e ) {
	    Debug.println(0, "MappingUtils.getSuffixList() " + e );
	} 
	
	String[] suffixList = new String[v.size()];
	v.toArray(suffixList);
	return suffixList;
    }

    public static String getSuffixForBackend(LDAPConnection ldc, String backend) {
		
	if ((ldc == null) || (backend == null)) {
	    return null;
	}
	String[] attrs = {BACKEND_ATTR};		
	try {
	    LDAPSearchResults res =
		ldc.search( "",
			    ldc.SCOPE_BASE,
			    "objectclass=*",
			    attrs,
			    false );
	    while( res.hasMoreElements() ) {
		LDAPEntry entry = (LDAPEntry)res.nextElement();
		String[] values = DSUtil.getAttrValues(entry, BACKEND_ATTR);
		if (values != null) {					
		    for (int i=0; i< values.length; i++) {
			int index = values[i].indexOf(':');
			if (index > 0) {
			    String currentBackend = values[i].substring(0, index);
			    if (currentBackend.equalsIgnoreCase(backend)) {
				String suffix = values[i].substring(index+1);
				return suffix;
			    }
			}
		    }
		}
	    }			
	} catch (LDAPException e) {
	    Debug.println("MappingUtils.getSuffixForBackend "+e);
	}
	return null;
    }

	/**
	  * Gives the corresponding suffixes for a given list of backends.  The order of the list correspond
	  * to the order of the backends.
	  *
	  * @param ldc the LDAPConnection with the server
	  * @param backends, the array with the list of the backends.
	  * @return the list of suffixes corresponding to the given list of backends
	  */
    public static String[] getSuffixesForBackends(LDAPConnection ldc, String[] backends) {		
		if ((ldc == null) || (backends == null)) {
			return null;
		}
		String[] attrs = {BACKEND_ATTR};
		String[] suffixes = null;
		try {
			LDAPSearchResults res =
				ldc.search( "",
							ldc.SCOPE_BASE,
							"objectclass=*",
							attrs,
							false );
			Vector vSuffixes = new Vector();
			while( res.hasMoreElements() ) {
				LDAPEntry entry = (LDAPEntry)res.nextElement();
				String[] values = DSUtil.getAttrValues(entry, BACKEND_ATTR);				
				if (values != null) {
					for (int j=0; j<backends.length; j++) {
						for (int i=0; i< values.length; i++) {
							int index = values[i].indexOf(':');
							if (index > 0) {
								String currentBackend = values[i].substring(0, index);
								if (currentBackend.equalsIgnoreCase(backends[j])) {
									String suffix = values[i].substring(index+1);
									vSuffixes.addElement(suffix);
								}
							}
						}
					}
				}
			}
			suffixes = new String[vSuffixes.size()];
			vSuffixes.copyInto(suffixes);
		} catch (LDAPException e) {
			Debug.println("MappingUtils.getSuffixForBackend "+e);
		}
		return suffixes;
    }
	
    public static String[] getBackendsForSuffix(LDAPConnection ldc, String suffix) {
	if ((ldc == null) || (suffix == null)) {
	    return null;
	}

	Vector vBackends = new Vector();

	DN suffixDN = new DN(suffix);

	String[] attrs = {BACKEND_ATTR};		
	try {
	    LDAPSearchResults res =
		ldc.search( "",
			    ldc.SCOPE_BASE,
			    "objectclass=*",
			    attrs,
			    false );
	    while( res.hasMoreElements() ) {
		LDAPEntry entry = (LDAPEntry)res.nextElement();
		String[] values = DSUtil.getAttrValues(entry, BACKEND_ATTR);
		if (values != null) {
		    for (int i=0; i< values.length; i++) {
			int index = values[i].indexOf(':');
			if (index > 0) {
			    String currentSuffix = values[i].substring(index+1);
			    DN currentSuffixDN = new DN(currentSuffix);
			    if (currentSuffixDN.equals(suffixDN)) {
				String currentBackend = values[i].substring(0, index);
				vBackends.addElement(currentBackend);
			    }
			}						
		    }
		}
	    }			
	} catch (LDAPException e) {
	    Debug.println("MappingUtils.getBackendsForSuffix "+e);
	}

	if (vBackends.size() <= 0) {
	    return null;
	}
	String[] backendsForSuffix = new String[vBackends.size()];
	vBackends.toArray(backendsForSuffix);

	return backendsForSuffix;
    }

    public static String getBackendForSuffix(LDAPConnection ldc, String suffix) {
	if ((ldc == null) || (suffix == null)) {
	    return null;
	}

	String[] backendsForSuffix = getBackendsForSuffix(ldc, suffix);
	if (backendsForSuffix != null ) {
	    return backendsForSuffix[0];
	}			
				
	return null;
    }


    /**
     * Returns the suffix of the backend that is naming context and parent of the entry.

     * For example if we have 2 backends with suffixes 'o=example.com' and 'ou=people, o=example.com'
     * For 'cn=Michael, o=example.com' it will return 'o=example.com'
     * For 'cn=Michael, ou=people, o=example.com' it will return 'o=example.com' ('ou=people, o=example.com' is not a naming context)
     * For 'cn=plugins, cn=config' (not in a regular backend) it will return null
     */
    public static String getTopSuffixForEntry(LDAPConnection ldc, String dn) {
	if ((ldc == null) || (dn==null)) {
	    return null;
	}

	DN entryDN = new DN(dn);
	DN suffixDN = null;
	DN currentSuffix = null;
				
	String[] orderedSuffixes = getOrderedSuffixList(ldc, ALL);
	
	for (int i=orderedSuffixes.length-1; i>= 0; i--) {
	    currentSuffix = new DN(orderedSuffixes[i]);
	    if (entryDN.equals(currentSuffix) || entryDN.isDescendantOf(currentSuffix)) {
		suffixDN = currentSuffix;				
		return suffixDN.toString();
	    }
	}
	return null;
    }

    /**
     * This method gets the shortest suffix which is superior of the given dn from the given suffix list .

     * For example if we have a list with 2 suffixes 'o=example.com' and 'ou=people, o=example.com'
     * For 'cn=Michael, o=example.com' it will return 'o=example.com'
     * For 'cn=Michael, ou=people, o=example.com' it will return 'o=example.com'
     * For 'cn=plugins, cn=config' (not in the list of suffixes) it will return null
	 *
	 * @param dn of the entry we want to handle
	 * @param suffixes the String[] with the suffixes
	 * @returns the shortest suffix corresponding to the given dn.  Null if no suffix was found.
     */
	
    public static String getTopSuffixForEntry(String dn, String[] suffixes) {
		if ((dn == null) ||
			(suffixes == null)) {
			return null;
		}
		DN entryDN = new DN(dn);
		DN suffixDN = null;
		DN currentSuffix = null;

		orderSuffixes(suffixes);		
		for (int i=suffixes.length-1; i>= 0; i--) {
			currentSuffix = new DN(suffixes[i]);
			if (entryDN.equals(currentSuffix) || entryDN.isDescendantOf(currentSuffix)) {
				suffixDN = currentSuffix;				
				return suffixDN.toString();
			}
		}
		return null;
	}

    /**
     * Returns the suffix of the backend to which this entry belongs.

     * For example if we have 2 backends with suffixes 'o=example.com' and 'ou=people, o=example.com'
     * For 'cn=Michael, o=example.com' it will return 'o=example.com'
     * For 'cn=Michael, ou=people, o=example.com' it will return 'ou=people, o=example.com'
     * For 'cn=plugins, cn=config' (not in a regular backend) it will return null
     */
	
    public static String getSuffixForEntry(LDAPConnection ldc, String dn) {
	if ((ldc == null) || (dn==null)) {
	    return null;
	}

	DN entryDN = new DN(dn);
	DN suffixDN = null;
	DN currentSuffix = null;
				
	String[] orderedSuffixes = getOrderedSuffixList(ldc, ALL);

	for (int i=0; i<orderedSuffixes.length; i++) {
	    currentSuffix = new DN(orderedSuffixes[i]);
	    if (entryDN.equals(currentSuffix) || entryDN.isDescendantOf(currentSuffix)) {
		suffixDN = currentSuffix;				
		return suffixDN.toString();
	    }
	}
	return null;
    }


    public static String getBackendForEntry(LDAPConnection ldc, String dn) {
	return getBackendForSuffix(ldc, getSuffixForEntry(ldc, dn));
    }

    public static String[] getBackendsForEntry(LDAPConnection ldc, String dn) {
	return getBackendsForSuffix(ldc, getSuffixForEntry(ldc, dn));
    }
		
    static public boolean addMappingNode(LDAPConnection ldc,
					 String section,
					 String newNodeName,
					 String upperNode,
					 String NStatus,
					 String backends[],
					 String referrals[]
					 ) {
	int resDiag;

	// Check parms
	if( (newNodeName == null) || (newNodeName.trim().length() == 0) ) {
	    return( false );
	}

	StringBuffer cn2add = new StringBuffer(newNodeName);
	// Add the upperNode to the new node
	if ( upperNode.compareTo( ROOT_MAPPING_NODE )!=0 ) {
	    cn2add.append( "," );
	    cn2add.append( upperNode );
	}

	// Building dn to add in mapping tree 
	// NOTE: we only need the escaped value in the DN - we use the 
	// "raw" value everywhere else
	String dn2add  = "cn=" + DSUtil.escapeDNVal(cn2add.toString()) + "," + CONFIG_MAPPING ;

	// Start LDAP job
	LDAPAttributeSet attrs = new LDAPAttributeSet();

	String objectclass[] = { "top", "extensibleObject", "nsMappingTree" };
	attrs.add( new LDAPAttribute( "objectclass", objectclass ));

	String state[] = { NStatus };
	attrs.add( new LDAPAttribute( "nsslapd-state", state ));

	// cn value is the "raw" suffix
	// the server may also add the value from the RDN value from the DN
	String mycn[] = { cn2add.toString() };
	attrs.add( new LDAPAttribute( "cn", mycn ));

	if ( upperNode.compareTo( ROOT_MAPPING_NODE ) != 0) {
	    // parent suffix is also the "raw" value, not escaped or quoted
	    String dady[] = { upperNode.trim() };
	    attrs.add( new LDAPAttribute( "nsslapd-parent-suffix", dady ));
	}

	if ( backends != null ) {
	    attrs.add( new LDAPAttribute( "nsslapd-backend", backends ));
	}

	if ( referrals != null ) {
	    attrs.add( new LDAPAttribute( "nsslapd-referral", referrals ));
	}

	LDAPEntry nodeEntry = new LDAPEntry( dn2add, attrs );
  
        try {
            ldc.add( nodeEntry );
        } catch (LDAPException e) {
	    	String ldapError;
			String ldapMessage;
			
			ldapError =  e.errorCodeToString();
			ldapMessage = e.getLDAPErrorMessage();
			if ((ldapMessage != null) &&
				(ldapMessage.length() > 0)) {
				ldapError = ldapError + ". "+ldapMessage;
			}

			String[] args = {ldapError};
            	DSUtil.showErrorDialog( null,
                                    	"error-new-node-title",
										"error-new-node-msg",
										args,
										section,
										DSUtil._resource);
 		return ( false );
        } 
	return ( true );
    }
  
    static public String[] whatsMoreInThisList ( String[] originalList,
                                                 String[] list2compare ) {
	return whatsMoreInThisList(originalList, list2compare, false);
    }

    static public String[] whatsMoreInThisList ( String[] originalList,
						 String[] list2compare,
						 boolean case_sens) {
	if(( originalList == null ) || (originalList.length == 0 )) {
	    return list2compare;
	}
	if(( list2compare == null ) || (list2compare.length == 0)) {
	    return null;
	}

	Vector v = new Vector(1);
	boolean found = false;

	for(int i=0;i < list2compare.length;i++) {
	    if ( case_sens == false ) {
		//				if ( Arrays.binarySearch( originalList, list2compare[i]) < 0 ){
		if (!( isStringInStringArray( list2compare[i], originalList, case_sens ))) {
		    v.addElement( list2compare[i] );
		}
	    } else {
		for(int j=0; (!found) && (j < originalList.length); j++) {
					
		}
	    }
	}
	String[] moreInList = new String[v.size()];
	v.toArray( moreInList );
	return moreInList;	
    }

    /**
     * @return true if s in is sArray
     * @parameters		s				:string to search for
     *					sArray			:string to search in
     *					case_sens		:apply case sensitivity
     */
    static public boolean isStringInStringArray( String s, String[] sArray, boolean case_sens ) {
	boolean isIn = false;
	if(( s == null) || (s.length() == 0)) {
	    return( false );
	}
	if(( sArray == null) || ( sArray.length == 0 )) {
	    return( false );
	}
	for(int i=0;
	    ((i < sArray.length) &&
	     !( isIn ));
	    i++) {
	    if ( case_sens ) {
		isIn = (s.compareTo( sArray[i] ) == 0 );
	    } else {
		isIn = ( s.compareToIgnoreCase ( sArray[i] ) == 0 );
	    }
	}
        return( isIn );
    }
  
    /**
     * get the default location of ldbm db
     * @parameters: model     : IDSModel
     *                    section     : Panel section (if null won't pop up 
     *                                dialog in case of error )
     * @return db path or null if not found
     */
    static public String getDefaultDBLoc( IDSModel model, String section ) {
	String DBLoc = "";
	String pluginDN;
	try{        
	    model.setWaitCursor( true );
	    LDAPConnection ldc = model.getServerInfo().getLDAPConnection();
	    LDAPSearchResults res =
		ldc.search( CONFIG_BASEDN,
			    ldc.SCOPE_ONE,
			    "nsslapd-pluginid=ldbm-backend",
			    null,
			    false );
	    if(res.hasMoreElements() ) {
		LDAPEntry bentry = (LDAPEntry)res.nextElement();
		pluginDN = bentry.getDN();
		Debug.println( "DSUtil.getDatabaseLoc() {");
		Debug.println( "*** plugin db: " + pluginDN );
		// Now retreive the config
		LDAPSearchResults res_conf = 
		    ldc.search( bentry.getDN(),
				ldc.SCOPE_ONE,
				"cn=config",
				null,
				false );
		if( res_conf.hasMoreElements() ) {
		    LDAPEntry centry = (LDAPEntry)res_conf.nextElement();
		    LDAPAttribute attr = centry.getAttribute( "nsslapd-directory" );
		    Debug.println( "*** nsslapd-directory =" + attr);
		    Enumeration en = attr.getStringValues();
		    if ( en.hasMoreElements() ) {
			DBLoc =  (String)en.nextElement() + "/";
		    }
		    return( DBLoc );
		}
	    }
           
	} catch( LDAPException e) {
	    if ( section != null ) {
			   
	    }
	} finally {
	    model.setWaitCursor( false );
	}
	return( null );
    }
	
    /**
     * Check if db name already exist in conf
     *
     * @param nodel   - model of the application
     * @param dbName  - new database name
     *
     * @return false if a db (local or chaining) has the same name.
     */
	
    static public boolean checkUnique( IDSModel       model,
				       String   dbName) {
	model.setWaitCursor( true );
	try {
	    LDAPConnection ldc = model.getServerInfo().getLDAPConnection();
	    if ( dbName != null) {
		LDAPSearchResults res =
		    ldc.search( CONFIG_BASEDN,
				ldc.SCOPE_SUB ,
				"(&(cn=" + dbName + ")(objectclass=nsBackendInstance))",
				null,
				false );
		model.setWaitCursor( false );
		if (res.hasMoreElements()) {
		    DSUtil.showErrorDialog( model.getFrame(),
					    "backendname-exist",
					    dbName );
		    return (false);
		} //if (res.hasMoreElements())
	    } // if(dbName != null)
	} catch (LDAPException e) { 
	    model.setWaitCursor( false );
	    DSUtil.showErrorDialog( model.getFrame(),
				    "backendname-can-create",
				    e.toString() );
	    return( false );
	} finally {
	    model.setWaitCursor( false );
	} 
	return( true );
    }

    /**
     * Add a backend in a suffix
     *
     * @param model   - model of the application
     * @param suffix  - Suffix name, where backend name should be added
     * @param dbName  - database name
     *
     * @return true if no error occured
     */
	
    static public boolean addBackendInSuffix(IDSModel model,
					     LDAPEntry suffixEntry,
					     String dbName,
					     String section) {

	String backends[] = { dbName.trim() };
	String dn_instMapping = suffixEntry.getDN();
	LDAPConnection ldc = model.getServerInfo().getLDAPConnection();
	String eStatus = 
	    DSUtil.getAttrValue(suffixEntry, "nsslapd-state");
	Debug.println("MappingUtils.addBackendInSuffix suffix is :" + eStatus);
	LDAPModificationSet mods = new LDAPModificationSet();
	
	LDAPAttribute backend_instMapping = 
	    new LDAPAttribute( "nsslapd-backend",
			       backends );
	mods.add( LDAPModification.ADD, backend_instMapping );
	if( (eStatus != null) &&
	    (eStatus.compareToIgnoreCase(MappingUtils.DISABLE) == 0 )) {
	    String[] args = {LDAPDN.explodeDN(suffixEntry.getDN(),true)[0]};
	    int resDiag = DSUtil.showConfirmationDialog(model.getFrame(),
							"enable-suffix",
							args,
							"general");
	    if( resDiag == JOptionPane.OK_OPTION ) {
		String[] state = { BACKEND };
		LDAPAttribute backend_status =
		    new LDAPAttribute( "nsslapd-state",
				       state );
		mods.add( LDAPModification.REPLACE,
			  backend_status );
	    }
	}
	model.setWaitCursor( true );
	try {
	    ldc.modify(dn_instMapping,  mods );
	} catch (LDAPException e) {
	    String[] args_m = { dn_instMapping, e.toString()} ;
	    DSUtil.showErrorDialog( model.getFrame(),
				    "error-mod-mapping",
				    args_m,
				    section);
	    return( false );
	} finally {
	    model.setWaitCursor( false );
	}
	return( true );
	
    }

    public static boolean isLeafMappingNode(LDAPConnection ldc, LDAPEntry _entry) {
	String suffix = MappingUtils.getMappingNodeSuffix(_entry);
	String filter = MappingUtils.getParentSearchFilter(suffix);
	// this node is a leaf if it is not the parent of another node
	try {
	    LDAPSearchResults res = ldc.search( CONFIG_MAPPING, LDAPv3.SCOPE_ONE,
		    filter, null, false);
	    if ((res == null) || !res.hasMoreElements()) {
		return( true );
	    } else {
		return( false );
	    }
	} catch (LDAPException e) {
	    Debug.println( "MappingNodeObject.isLeafMappingNode() :" + e );
	    return( false );
	}
    }

    /**
     * Given a suffix DN, return the corresponding mapping tree node DN
     * @param ldc the LDAP connection
     * @param suffix the suffix DN
     * @return the DN of the mapping tree node corresponding to this suffix
     */
    public static String getMappingNodeForSuffix(LDAPConnection ldc, String suffix) {
	String filter = getSuffixSearchFilter(suffix);
	// this node is a leaf if it is not the parent of another node
	String mndn = null;
	try {
	    LDAPSearchResults res = ldc.search( CONFIG_MAPPING, LDAPv3.SCOPE_ONE,
		    filter, null, false);
	    if ((res == null) || !res.hasMoreElements()) {
		Debug.println( "MappingNodeObject.getMappingNodeForSuffix() : " +
			"no entries returned for filter " + filter );
	    } else {
		LDAPEntry entry = (LDAPEntry)res.nextElement();
		mndn = entry.getDN();
	    }
	} catch (LDAPException e) {
	    Debug.println( "MappingNodeObject.getMappingNodeForSuffix() :" + e );
	}
	return mndn;
    }

    private final static String _section = "mappingtree";
    public static final String CONFIG_BASEDN = "cn=plugins, cn=config" ;
    public 	static final String CHAINING_CONFIG_BASEDN = "cn=chaining database, cn=plugins, cn=config" ;
    public 	static final String LDBM_CONFIG_BASEDN = "cn=ldbm database, cn=plugins, cn=config" ;
    public 	static final String CONFIG_MAPPING = "cn=mapping tree, cn=config" ;
    public 	static final String ROOT_MAPPING_NODE = "is root suffix";
    public 	static final String DISABLE = "Disabled";
    public 	static final String BACKEND = "Backend";
    public 	static final String REFERRAL = "Referral";
    public 	static final String ADD_REF = "addReferral";
    public 	static final String SUFFIX_ATTR = "nsslapd-suffix";
    public 	static final String BACKEND_ATTR = "nsbackendsuffix";
    private final String DELETE_REF = "deleteReferral";
    public 	static final String REFERRAL_UPDATE = "Referral on Update";

    static public final int ALL = 0;
    static public final int CHAINING = 1;
    static public final int LDBM = 2;

    private LDAPEntry _entry = null;
}

