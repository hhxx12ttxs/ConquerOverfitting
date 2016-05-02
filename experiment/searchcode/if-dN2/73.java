package net.sourceforge.myvd.types;

import java.util.Vector;

import com.novell.ldap.util.DN;
import com.novell.ldap.util.RDN;

public class DistinguishedName {
	DN dn;

	public DistinguishedName(String dn) {
		if (dn != null) {
			this.dn = new DN(dn);
		} else {
			this.dn = new DN("");
		}
	}

	public DistinguishedName(DN dn) {
		this.dn = new DN(dn.toString());
	}

	public DN getDN() {
		if (this.dn == null) {
			return new DN("");
		} else {
			return this.dn;
		}
	}

	public void setDN(DN dn2) {
		this.dn = dn2;

	}

	public String toString() {
		String str = this.getDN().toString();
		if (str != null) {
			return str;
		} else {
			return "";
		}
	}
	
	@SuppressWarnings("unchecked")
	public Vector<RDN> getRDNs()
	{
		return getDN().getRDNs();
	}
}

