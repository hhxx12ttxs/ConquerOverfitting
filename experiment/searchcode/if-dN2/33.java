package net.sourceforge.myvd.types;

import java.util.Comparator;

import com.novell.ldap.util.DN;

public class DNComparer implements Comparator<DN> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(DN dn1, DN dn2) {
		int num1 = dn1.countRDNs();
		int num2 = dn2.countRDNs();

		if (num1 < num2) {
			return -1;
		}

		if (num1 > num2) {
			return 1;
		}

		if (dn1.equals(dn2)) {
			return 0;
		}

		return dn1.toString().compareTo(dn2.toString());
	}

}

