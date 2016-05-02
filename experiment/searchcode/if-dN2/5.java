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

import netscape.ldap.LDAPDN;

/**
 * XXXggood none of this is tested.
 *
 * @author
 * @version %I%, %G%
 * @date	 	11/20/97
 * @see     com.netscape.admin.dirserv.panel.replication
 */
public class ReplLDAPDN extends LDAPDN {

    private String origDN;
    private String normDN;
    private String[] explodedDN;

    public ReplLDAPDN(String odn)
    {
        origDN = odn;
        if (odn == null || odn.length() == 0) {
            explodedDN = null;
            normDN = odn;
        } else {
            explodedDN = explodeDN(odn, false);
            normDN = explodedDN[0].toLowerCase();
            for (int i = 1; i < explodedDN.length; i++) {
                normDN = normDN + "," + explodedDN[i].toLowerCase();
            }
        }
    }




    public static boolean compareDNs(String dn1, String dn2)
    {

        if (dn1 == null ) {
            if (dn2 == null || dn2.length() == 0) {
                return true;
            } else {
                return false;
            }
        } else if (dn2 == null) {
            if (dn1 == null || dn1.length() == 0) {
                return true;
            } else {
                return false;
            }
        }
        if (dn1.length() == 0 && dn1.length() == 0) {
            return true;
        }

        String xdn1[] = explodeDN(dn1, false);
        String xdn2[] = explodeDN(dn2, false);

        if (xdn1.length != xdn2.length) {
            return false;
        }

        for (int i = 0; i < xdn1.length; i++) {
            if (!xdn1[i].equalsIgnoreCase(xdn2[i])) {
                return false;
            }
        }
        return true;
    }



    public boolean equals(String[] xdn)
    {
        if (xdn.length != explodedDN.length) {
            return false;
        }
        for (int i = 0; i < xdn.length; i++) {
            if (!xdn[i].equalsIgnoreCase(explodedDN[i])) {
                return false;
            }
        }
        return true;
    }



    public boolean equals(String dn)
    {
        if (dn == null) {
            if (origDN == null) {
                return true;
            } else {
                return false;
            }
        }
        if (dn.length() == 0) {
            if (origDN.length() == 0) {
                return true;
            } else {
                return false;
            }
        }
        return(equals(explodeDN(dn, false)));
    }


    public String getOrigDN()
    {
        return origDN;
    }



    public String[] getExplodedDN()
    {
        return explodedDN;
    }



    public String getNormalizedDN()
    {
        return normDN;
    }



    public String toString()
    {
        String ostr = "";
        ostr += "Original DN: \"" + origDN + "\", normalized DN: \"" + normDN + "\", exploded DN: ";
        for (int i = 0; i < explodedDN.length; i++) {
            ostr += "[" + i + "]: \"" + explodedDN[i] + "\"";
        }
        return ostr;
    }
}
