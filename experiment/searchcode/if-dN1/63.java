/** BEGIN COPYRIGHT BLOCK
 * Copyright (C) 2001 Sun Microsystems, Inc.  Used by permission.
 * Copyright (C) 2005 Red Hat, Inc.
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version
 * 2.1 of the License.
 *                                                                                 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *                                                                                 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * END COPYRIGHT BLOCK **/

package com.netscape.management.client.util;

import java.util.*;
import java.text.*;
import netscape.ldap.*;
import netscape.ldap.util.DN;
import com.netscape.management.client.console.*;
import com.netscape.management.client.util.UtilConsoleGlobals;

/**
 * A set of utility functions relating to LDAP.
 * Used internally by Console.
 */
public class LDAPUtil {
    public static final int LDAP_VERSION = 3; // Need to connect to DS using version 3 to use virtual list.

    static String _isieDN = "ou=Netscape SuiteSpot, o=NetscapeRoot";

    /**
    * @deprecated Replaced by setInstalledSoftwareDN()
    **/
    public static void setGlobalPreferenceLocation(String newLocation) {
        setInstalledSoftwareDN(newLocation);
    }

    public static void setInstalledSoftwareDN(String newLocation) {
        _isieDN = newLocation;
    }

    public static String getInstalledSoftwareDN() {
        return _isieDN;
    }

    public static String getAdminGlobalParameterEntry() {
        return "ou=" + Console.VERSION + ", ou=admin, ou=Global Preferences,"+
                _isieDN;
    }

    public static String getCommonGlobalParameterEntry() {
        return "cn=common, ou=Global Preferences,"+_isieDN;
    }

    /**
      * Get the configuration DN of the netscape control registry
      *
      * @return return "O=NetscapeRoot" for version 4.0
      */
    public static String getConfigurationRoot() {
        return "o=netscapeRoot";
    }

    /**
      * Get global parameter entry for a product (server)
      *
      * @param product RDN for a server (e.g. "ou=admin" for the admin server)
      */
    public static String getGlobalParameterEntry(String product) {
        return "ou=" + Console.VERSION + "," + product + ", ou=Global Preferences,"+
                _isieDN;
    }

    /**
      * Get global parameter entry for a product (server) and the version
      *
      * @param product RDN for a server (e.g. "ou=admin" for the admin server)
      * @param version RDN for the server version (e.g. "ou=4.0" for version 4.0)
      */

    public static String getGlobalParameterEntry(String product,
            String version) {
        return version + "," + product + ", ou=Global Preferences,"+
                _isieDN;
    }

    public static String getUserPreferenceOU() {
        return "UserPreferences";
    }

    /**
      *  Flatting the multi values directory string
      */
    public static String flatting(Enumeration e) {
        String s = "";
        boolean fFirst = true;
        for (;e.hasMoreElements();) {
            //
            // just add the strings together with a space separation
            //
            String next = (String) e.nextElement();
            if (fFirst) {
                s = next;
                fFirst = false;
            } else {
                s += " "+next;
            }
        }
        return s;
    }

    public static String flatting(LDAPAttribute attr) {
        String sReturn = "";
        if (attr != null) {
            sReturn = flatting(attr.getStringValues());
        }
        return sReturn;
    }

    public static String getUniqueAttribute(LDAPConnection ldc,
            String sEntry) {
        String sReturn = "uid";
        try {
            LDAPEntry entry = ldc.read(sEntry);
            if (entry != null) {
                LDAPAttribute attribute = entry.getAttribute("nsuniqueattribute");
                sReturn = flatting(attribute);
            }
        } catch (LDAPException e) {
            Debug.println(0, "cannot read global parameter because error:"+e);
        }
        if ((sReturn == null) || (sReturn == "")) {
            sReturn = "uid";
        }
        return sReturn;
    }


    /*
      *	Map the specified DN to uid
      *
      * @Return uid for the dn if uid exist
      */
    public static String getUIDFromDN(LDAPConnection ldc, String DN) {
        String sReturn = null;
        try {
            
            LDAPEntry entry = ldc.read(DN, new String[] { "uid" });
            if (entry != null) {
                LDAPAttribute attribute = entry.getAttribute("uid");
                sReturn = flatting(attribute);
            }
        } catch (LDAPException e) {
            Debug.println( 0, "LDAPUtil.getUIDFromDN: cannot read " + DN + " - " + e ); 
        }

        return sReturn;
    }

    /**
      *	Map the specified uid to a DN
      *
      * @Return DN for the uid
      * @deprecated, not to be used, it does not suport secure connections
      */
    public static String getDNFromUID(String sHost, int iPort,
            String sBaseDN, String uid) {

         return getDNFromUID(sHost, iPort, false, sBaseDN, uid);
    }

    /**
      * Maps the specified uid to a DN.
      * @param sHost server host name
      * @param iPort server port number
      * @param isSecure true if SSL is used
      * @param sBaseDN base DN for uid search
      * @param uid uid to search for
      * @return DN for the uid
      */    
    public static String getDNFromUID(String sHost, int iPort, boolean isSecure,
            String sBaseDN, String uid) {
        String sReturn = null;
        LDAPConnection ldc = null;
        try {
            if (isSecure) {
                ldc = new KingpinLDAPConnection(
                      UtilConsoleGlobals.getLDAPSSLSocketFactory(),
                      "", "");
            } else {
                ldc = new KingpinLDAPConnection("","");
            }
            ldc.connect(sHost, iPort);
            String sFilter = "("+getUniqueAttribute(ldc,
                    getCommonGlobalParameterEntry()) + "="+uid + ")";
            LDAPSearchResults result =
                    ldc.search(sBaseDN, LDAPConnection.SCOPE_SUB,
                    sFilter, null, false);
            if (result != null) {
                while (result.hasMoreElements()) {
                    LDAPEntry findEntry = (LDAPEntry) result.next();

                    // just use the first entry and ignore the rest
                    sReturn = findEntry.getDN();
                }
            }
        } catch (LDAPException e) {
            Debug.println( 0, "LDAPUtil.getDNFromUID: cannot read " + uid + " - " + e ); 
        }
        finally {
            if (ldc != null) {
                try {
                    ldc.disconnect();
                }
                catch (Exception e) {}
            }
        }

        if (sReturn == null) {
            Debug.println("cannot find uid <"+uid + "> under "+sHost +
                    ":"+iPort + ":"+sBaseDN);
        }
        return sReturn;
    }

    static public String getLDAPAttributeLocale() {
        ResourceSet _resource = new ResourceSet("com.netscape.management.client.console.console");

        String sLangExtension = _resource.getString("DirectoryServer","attribute-extension");
        if ((sLangExtension == null) || (sLangExtension == "")) {
            sLangExtension = "lang-us";
        }
        return sLangExtension;
    }

    /**
      * A debug method to show the contents of an entry
     */
    static public void printEntry(LDAPEntry ldapEntry) {

        if (ldapEntry == null)
            return;

        Debug.println("==== DN: " + ldapEntry.getDN() + " =====");

        LDAPAttributeSet findAttrs = ldapEntry.getAttributeSet();
        Enumeration enumAttrs = findAttrs.getAttributes();

        while (enumAttrs.hasMoreElements()) {
            LDAPAttribute anAttr = (LDAPAttribute) enumAttrs.nextElement();
            String attrName = anAttr.getName();
            Enumeration attrValues = anAttr.getStringValues();
            Debug.println(attrName + "=" + LDAPUtil.flatting(attrValues));
        }
    }

    /**
      * A utility method to convert a LDAP entry into a hashtable. Optionally,
      * each attribute name be renamed by adding a prefix.
      *
      * @param LDAPEntry LDAP Entry to copy from
      * @param renamePrefix Optional prefix to add to each key, or NULL
     */
    static public Hashtable ldapEntryToHashtable(LDAPEntry ldapEntry,
            String renamePrefix) {

        if (ldapEntry == null) {
            return new Hashtable();
        }

        LDAPAttributeSet findAttrs = ldapEntry.getAttributeSet();
        Enumeration enumAttrs = findAttrs.getAttributes();
        Hashtable out = new Hashtable();

        while (enumAttrs.hasMoreElements()) {
            LDAPAttribute anAttr = (LDAPAttribute) enumAttrs.nextElement();
            String attrName = anAttr.getName();
            Enumeration attrValues = anAttr.getStringValues();
            String attrValue = LDAPUtil.flatting(attrValues);

            if (renamePrefix == null) {
                out.put(new String(attrName), attrValue);
            } else {
                out.put(renamePrefix + attrName, attrValue);
            }
        }

        return out;
    }

    /**
      * Conver the date in LDAP format into java.util.Date object
     */
    public static Date getDateTime(String dbDate) {
        String dbDateFormat = "yyyyMMddHHmmss";
        boolean gmt = false;

        // Check if time zone included into date
        if (dbDate.length() > dbDateFormat.length()) {

            // If date fomat ends with 'Z' interpret 'Z' as the GMT
            // time zone (ASN.1 format used by Directory)
            if (dbDate.length() == dbDateFormat.length() + 1 &&
                    dbDate.endsWith("Z")) {
                dbDate = dbDate.substring(0, dbDate.length() - 1);
                gmt = true;
            }
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(dbDateFormat);
            Date date = sdf.parse(dbDate, new ParsePosition(0));
            if (gmt) {
                long offsetMillis = TimeZone.getDefault().getRawOffset();
                Date date2 = new Date(date.getTime() + offsetMillis);
                if (TimeZone.getDefault().inDaylightTime(date2)) {
                    date2 = new Date(date2.getTime() + 60 * 60 * 1000);
                }
                return date2;
            }

            return date;
        } catch (Exception e) {
            Debug.println(0,
                    dbDate + " does not match expected format "+
                    dbDateFormat);
            Debug.println(0, e.getMessage());
            return null;
        }
    }

    /**
     * Conver the date in LDAP format into localized string
     */
    public static String formatDateTime(String dbDate) {
        try {
            Date date = getDateTime(dbDate);
            if (date == null) {
                return dbDate;
            }
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG,
                    DateFormat.LONG);
            return df.format(date);
        } catch (Exception e) {
            Debug.println(0, e.getMessage());
            return dbDate;
        }
    }


    /**
      * Return an array of all language tags on attributes in the entry.
      * A language tag is of the form <CODE>lang-en</CODE> or <CODE>
      * lang-ja-JP-kanji</CODE>.
      * @param entry An entry returned from a search or read operation.
      * @return An array of language tags; may be of zero length.
      */
    static public String[] getAttributeLanguages(LDAPEntry entry) {
        return getAttributeLanguages(entry.getAttributeSet());
    }

    /**
      * Return an array of all language tags on attributes in the set.
      * A language tag is of the form <CODE>lang-en</CODE> or <CODE>
      * lang-ja-JP-kanji</CODE>.
      * @param attrs A collection of attributes.
      * @return An array of language tags; may be of zero length.
      */
    static public String[] getAttributeLanguages(LDAPAttributeSet attrs) {
        Enumeration e = attrs.getAttributes();
        Vector v = new Vector();
        while (e.hasMoreElements()) {
            LDAPAttribute attr = (LDAPAttribute) e.nextElement();
            String lang = attr.getLangSubtype();
            if ((lang != null) && (v.indexOf(lang) < 0)) {
                v.addElement(lang);
            }
        }
        String[] languages = new String[v.size()];
        for (int i = 0; i < languages.length; i++) {
            languages[i] = (String) v.elementAt(i);
        }
        return languages;
    }

    static public String createEntry(LDAPConnection ldc, String ou,
            String dn) {
        return createEntry(ldc, ou, dn, false);
    }

    static public String createEntry(LDAPConnection ldc, String ou,
            String dn, boolean createACI) {
        String newDN = "ou=" + ou + "," + dn;
        LDAPSearchResults searchResults = null;
        try {
            // TODO: minor optimization: set search attrs to "dn", from rweltman
            // FYI: read() calls search()
            searchResults =
                    ldc.search(newDN, LDAPConnection.SCOPE_SUB, "(objectclass=*)",
                    null, false);

            // next sure that it is not an exception
            while (searchResults.hasMoreElements()) {
                LDAPEntry e = (LDAPEntry) searchResults.next();
            }
        } catch (LDAPException e) {
            Debug.println(0, "Cannot find: " + newDN);
            Debug.println(0, "Creating: " + newDN);
            if (searchResults == null)
                try {
                    //LDAPAttribute attr1 = new LDAPAttribute( "ou", ou);
                    LDAPAttribute attr2 =
                            new LDAPAttribute("objectclass", "top");
                    LDAPAttribute attr3 =
                            new LDAPAttribute("objectclass", "organizationalUnit");
                    LDAPAttribute attr4 = null;
                    if (createACI) {
                        String aci = "(targetattr=*)(version 3.0; acl \"UserDNControl\"; allow (all) userdnattr=\"creatorsname\";)";
                        attr4 = new LDAPAttribute("aci", aci);
                    }
                    LDAPAttributeSet attrs = new LDAPAttributeSet();
                    //attrs.add(attr1);
                    attrs.add(attr2);
                    attrs.add(attr3);
                    if (createACI)
                        attrs.add(attr4);
                    ldc.add(new LDAPEntry(newDN, attrs));
                } catch (LDAPException exception) {
                    Debug.println(0, "Cannot create: " + newDN);
                }
        }
        return newDN;
    }


    /**
      * Determines whether the connection is to a DS version 4.x or higher.
      *
      * @param ldc  the LDAPConnection object
      * @deprecated The method always returns true
      * 
      */
    public static boolean isVersion4(LDAPConnection ldc) {
        return true;
    }

    /**
      * Validate LDAP parameters for connecting, binding and baseDN search.
      * Throws an IllegalArgumentException if validation fails. The message inside the
      * exception describes why validation has failed.
      *
      * @param host Ldap host
      * @param port Ldap port
      * @param ssl a flag whether a secure SLL connection should be used
      * @param bindDN DN to use for binding (can be null)
      * @param bindPWD password to use for binding (can be null)
      * @param baseDN baseDN for search operations (can be null)
      * @throws IllegalArgumentException if validation for any of non-null parameters fails
      */
    public static void validateLDAPParams(String host, int port,
            boolean ssl, String bindDN, String bindPWD,
            String baseDN) throws IllegalArgumentException {
        int error = 0;
        KingpinLDAPConnection ldc = null;

        try {

            error = 1; // Can not create Socket factory
            if (ssl) {
                ldc = new KingpinLDAPConnection(
                                 UtilConsoleGlobals.getLDAPSSLSocketFactory(), 
                                 bindDN, 
                                 bindPWD);
            } else {
                ldc = new KingpinLDAPConnection(bindDN, bindPWD);
            }

            error = 2; // can not connect
            ldc.connect(host, port);

            if (bindDN == null || bindPWD == null) {
                error = 0;
                return; // can not verify further
            }

            error = 3; // can not authenticate
            ldc.authenticate(bindDN, bindPWD);

            error = 4; // baseDN not found
            if (baseDN != null) {
                ldc.search(baseDN, LDAPConnection.SCOPE_BASE, "objectclass=*",
                        null, false);
            }
            error = 0; // OK
        } catch (LDAPException e) {
            ResourceSet resource = new ResourceSet("com.netscape.management.client.util.default");
            String msg = "";
            if (error == 1) {
                msg = resource.getString("error", "LdapSSLSocket");
            } else if (error == 2) {
                msg = resource.getString("error",
                        ssl ? "LdapConnectSecure" : "LdapConnect");
            } else if (error == 3) {
                msg = resource.getString("error", "LdapAuthenticate");
            } else if (error == 4) {
                msg = resource.getString("error", "LdapBaseDN");
            }

            Debug.println("validateLDAPParams " + e);
            throw new IllegalArgumentException(msg);
        }
        finally { if (ldc != null) {
                try {
                    ldc.disconnect();
                } catch (Exception e) {}
            }
        }
    }
    
    /**
     * Check if there is a VLV index for the specified search.
     * 
     * @param ldc LDAP connection
     * @param vlvBase Search base
     * @param vlvScope Search scope
     * @param vlvFilter Search filter
     * @param vlvSort A space separated list of attributes to sort the results on
     * or <code>null</code> to match any value
     * @return true if the index exists
     * 
     * @since SDK 6.1.1
     */
    public static boolean hasVLVIndex(LDAPConnection ldc,
                                      String vlvBase, int vlvScope,
                                      String vlvFilter, String vlvSort) {

        String indexVLVSort = getVLVIndex(ldc, vlvBase, vlvScope,
                                          vlvFilter, vlvSort);        
        if (indexVLVSort != null) {
            if (vlvSort == null) {
                return true;
            }
            else {
                return indexVLVSort.equalsIgnoreCase(vlvSort);
            }
        }
        return false;
    }

    /**
     * Check for existence of a VLV index with some flexibility in regard
     * to the sort attribute list.
     * 
     * Check first if there is an exact match for all search parameters. If 
     * not found, then check for a VLV index that differs only for the sort 
     * attribute list.
     * 
     * Return the sort attribute list for the matched index entry or
     * <code>null</code> if no index found.
     * 
     * @param ldc LDAP connection
     * @param vlvBase Search base
     * @param vlvScope Search scope
     * @param vlvFilter Search filter
     * @param vlvSort A space separated list of attributes to sort the results on
     * or <code>null</code> to match any value
     * @return vlvSort for the VLV index entry or <code>null</code> if no index found
     * 
     * @since 6.1.1
     */
    public static String getVLVIndex(LDAPConnection ldc,
                                     String vlvBase, int vlvScope,
                                     String vlvFilter, String vlvSort) {
    
        String backendInstance = getBackendForDN(ldc, vlvBase);        
        if (backendInstance == null) {
            return null;
        }
        
        Debug.println("LDAPUtil.getVLVIndex " + vlvBase + " " +
                      vlvScope + " " + vlvFilter + " " + vlvSort);
        String indexVLVSort = null;
        try {
            String scope = (new Integer(vlvScope)).toString();

            LDAPSearchResults res = 
                ldc.search(backendInstance,
                    ldc.SCOPE_ONE,
                    "(objectclass=vlvSearch)",
                    null, false);

             while (res.hasMoreElements()) {
                LDAPEntry entry = res.next();
                LDAPAttribute attr = entry.getAttribute("vlvBase");
                if (attr == null) {
                    Debug.println("LDAPUtil.getVLVIndex: no vlvBase attr in "
                                  + entry.getDN());
                    continue;
                }
                String val = attr.getStringValueArray()[0];
                DN dn = new DN(val), dn1 = new DN(vlvBase);
                if (!dn.equals(dn1)) {
                   continue;
                }

                attr = entry.getAttribute("vlvScope");
                if (attr == null) {
                    Debug.println("LDAPUtil.getVLVIndex: no vlvScope attr in "
                                  + entry.getDN());
                    continue;
                }
                if (!attr.getStringValueArray()[0].equals(scope)) {
                    continue;
                }
                
                attr = entry.getAttribute("vlvFilter");
                if (attr == null) {
                    Debug.println("LDAPUtil.getVLVIndex: no vlvFilter attr in "
                                  + entry.getDN());
                    continue;
                }
                // Compare filters ignore case and spaces
                val = contractSpaces(attr.getStringValueArray()[0]);
                if (!val.equalsIgnoreCase(contractSpaces(vlvFilter))) {
                   continue;
                }
                
                // Check the sort attributes
                 LDAPSearchResults res1 = 
                    ldc.search(entry.getDN(),
                               ldc.SCOPE_ONE,
                               "(objectclass=vlvIndex)",
                               null, false);
                  if (res1.hasMoreElements()) {
                      LDAPEntry idxEntry = res1.next();
                      attr = idxEntry.getAttribute("vlvSort");
                      if (attr == null) {
                          Debug.println("LDAPUtil.getVLVIndex: no vlvSort attr in "
                                        + idxEntry.getDN());
                          continue;
                      }
                      val = attr.getStringValueArray()[0];
                      if (vlvSort == null) {
                          // caller does not care about the vlvSort 
                          Debug.println("    match="+val);
                          return val;
                      }
                      else if (val.equalsIgnoreCase(vlvSort)) {
                          // exact match found
                          Debug.println("    exact match found");
                          return vlvSort;
                      }
                      else {
                          // keep in case we do not find the exact match
                          indexVLVSort = val;
                      }
                  }
             }
        }
        catch (Exception e) {
              Debug.println("LDAPUtil.getVLVIndex " + e);
        }
        Debug.println("    match=" + indexVLVSort);
        return indexVLVSort;
    }

    /**
     * Return the DS database DN where index configuration is stored
     * 
     */
    static String getBackendForDN(LDAPConnection ldc, String entryDN) {
  
       
        String[] attrs = {"nsBackendSuffix"};
        LDAPEntry rootDSE = null;

	    try {	        
            rootDSE = ldc.read( "", attrs);
        }
        catch (LDAPException e) {
            Debug.println("LDAPUtil.getBackendForDN:" + e);
            return null;
        }
	        
        if (rootDSE == null) {
            Debug.println("LDAPUtil.getBackendForDN: no " + attrs[0] + " in rootDSE");
            return null;
        }
	    
        LDAPAttribute attr = rootDSE.getAttribute(attrs[0]);
        if (attr == null) {
            Debug.println("LDAPUtil.getBackendForDN: no values for " + attrs[0] + " in rootDSE");
            return null;
        }
        
        /**
         * Search for the suffix of entryDN and get the matching backend name.
         * As suffixes might have subsuffixes (e.g. o=abc and o=xyz,o=abc) can
         * not return on the first match, but need to go through all suffixes
         * and return the longest match. For instance, if entryDN
         * is cn=user,o=xyz,o=abc the match should be o=xyz,o=abc, not o=abc.
         *  
         */        
        DN dn = new DN(entryDN), sDN=null;
        String backendName = null;
        String[] values = attr.getStringValueArray();
        for (int i=0; i<values.length; i++) {
	        int index = values[i].indexOf(':');
	        if (index > 0) {
                // value = <backendName>:<suffixDN>
	    	    String backend = values[i].substring(0, index);
                String suffix =  values[i].substring(index+1);
                DN dn1 = new DN(suffix);
                if (dn.equals(dn1)) {
                    //entryDN is a suffix itself, no need to search further
                    backendName = backend;
                    break;
                }
                else if (dn.isDescendantOf(dn1)) {
                    if (sDN == null || dn1.isDescendantOf(sDN)) {
                        backendName = backend;
                        // save the suffix DN to compare with possibly other matches
                        sDN = dn1;
                    }
                }
            }
        }
        
        if (backendName == null) {
            Debug.println("LDAPUtil.getBackendForDN: no matching backend for \"" + entryDN + "\"");
            return null;
        }
        else {
            return "cn=" + backendName + "," + LDBM_PLUGIN_ROOT;
        }
    }
    
    /**
     * A helper method used for filter comparison.
     * Removes all spaces from a string.
     */
    static String contractSpaces(String s) {
        StringBuffer res = new StringBuffer();
        for (int i=0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != ' ') {
                res.append(c);
            }
        }
        return res.toString();
    }

    // DS ldbm database configuration root DN
    final static String LDBM_PLUGIN_ROOT = "cn=ldbm database, cn=plugins, cn=config";
}

