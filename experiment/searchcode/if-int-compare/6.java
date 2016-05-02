/* =================================================================
Copyright (C) 2009 ADV/web-engineering All rights reserved.

This file is part of Mozart.

Mozart is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Mozart is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

Mozart
http://www.mozartcms.ru
================================================================= */
// -*- java -*-
// File: Compare.java
//
// Created: Wed Dec 12 14:06:52 2001
//
// $Id: Compare.java 1106 2009-06-03 07:32:17Z vic $
// $Name:  $
//

package ru.adv.util;

/**
 * Comparison utilites.
 */
public class Compare {
	private Compare() {}

	/**
	 * Compares to objects. At first tries to compare objects numerically.
	 * In case some or both objects are not a number preforms
	 * lexicographically comparison.
	 * @return a negative integer, zero, or a positive integer as first
	 * object is less than, equal to, or greater than the second
	 * object.
	 * @see Compare#compareNumerically
	 * @see Compare#compareLexicogaphically
	 */
	public static final int compare(Object o1, Object o2) {
		int retval; 
		try {
			retval = compareNumerically(o1, o2);
		}
		catch (NumberFormatException e) {
			retval = compareLexicogaphically(o1, o2);
		}
		return retval;
	}
	
	/**
	 * Compares to object numerically. It tries to convert specified
	 * objects to numbers and compares they.
	 * @return a negative integer, zero, or a positive integer as first
	 * object is less than, equal to, or greater than the second
	 * object.
	 * @throws NumberFormatException if some object does not contain a
	 * parsable number 
	 */
	public static final int compareNumerically(Object o1, Object o2) throws NumberFormatException {
		Double d1, d2;
		if (o1 instanceof Number) {
			d1 = new Double(((Number)o1).doubleValue());
		}
		else {
			d1 = new Double(o1.toString());
		}
		if (o2 instanceof Number) {
			d2 = new Double(((Number)o2).doubleValue());
		}
		else {
			d2 = new Double(o2.toString());
		}
		return d1.compareTo(d2);
	}
	
	/**
	 * Compares to object lexicographically. First if first object is
	 * <code>Comaprable</code> preforms its own <code>compareTo</code>
	 * method. In case second object has different type both argumens
	 * converts to <code>String</code> and compares.
	 * @return a negative integer, zero, or a positive integer as first
	 * object is less than, equal to, or greater than the second
	 * object.
	 */
	public static final int compareLexicogaphically(Object o1, Object o2) {
		try {
			if (o1 instanceof Comparable) {
				return ((Comparable) o1).compareTo(o2);
			}
		}
		catch (ClassCastException e) {
			// do nothig, assume lexicographically comparison
		}
		return o1.toString().compareTo(o2.toString());
	}

}

