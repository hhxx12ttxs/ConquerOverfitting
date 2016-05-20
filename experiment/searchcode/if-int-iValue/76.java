/*
 * Copyright (c) 2008-2011 Simon Ritchie.
 * All rights reserved. 
 * 
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published 
 * by the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program.  If not, see http://www.gnu.org/licenses/>.
 */
package org.rimudb;

import java.io.*;
import java.math.*;
import java.sql.*;
import java.util.*;

/**
 * 
 * This class represents a list of KeyColumns.
 * 
 * @deprecated Use WhereList instead.
 */
@Deprecated
public class KeyList implements Serializable {
	private static final long serialVersionUID = -8487546422599367894L;
	
	private List<KeyColumn> klist = new ArrayList<KeyColumn>();
	private StringBuilder uniquePropertyID = new StringBuilder();

	/**
	 * Construct a KeyList
	 */
	public KeyList() {
	}

	/**
	 * Construct a KeyList from another KeyList.
	 * 
	 * @param keylist KeyList
	 */
	public KeyList(KeyList keylist) {
		List<KeyColumn> otherList = keylist.getInternalKeyList();
		for (KeyColumn otherKeyColumn : otherList) {
			add(new KeyColumn(otherKeyColumn));
		}
	}
	
	public KeyList(WhereList whereList) { 
		if (whereList != null) {
			List<WhereEntry> whereEntryList = whereList.getWhereEntryList();
			for (Iterator<WhereEntry> iterator = whereEntryList.iterator(); iterator.hasNext();) {
				WhereEntry whereEntry = (WhereEntry) iterator.next();
				if (whereEntry.getBooleanOperator() == BooleanOperator.OR) {
					throw new IllegalArgumentException("WhereList contains a Boolean OR operator. Cannot convert to KeyList.");
				}
				if (whereEntry.getOperator() != Operator.EQ) {
					throw new IllegalArgumentException("WhereList contains a '"+whereEntry.getOperator().getSQLValue().trim()+"' operator. WhereLists must contain only EQ operators to convert to a KeyList");
				}
				add(whereEntry.getPropertyName(), whereEntry.getValue());
			}
		}
	}

	/**
	 * Return the internal key list.
	 * @return List<KeyColumn>
	 */
	protected List<KeyColumn> getInternalKeyList() {
		return klist;
	}
	
	/**
	 * Return true if the partial keylist contains identical keys
	 * to the partial part of this keylist.
	 * 
	 * The keylists are  compared to find the keylist with the
	 * smallest number of keys. This number of keys are compared
	 * between each keylist.
	 * 
	 * This allows the keylist of a child record to be compared 
	 * with the keylist of parent to see if the primary keys match.
	 * 
	 * @return boolean
	 * @param obj Object
	 */
	public boolean equalsPartial(Object obj) {
		if (this == obj) {
			return true;
		}
		if((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}

		// Cast the object
		KeyList keylist = (KeyList) obj;

		// Count the minimum number of keys
		int min = 0;
		if (keylist.size() < size())
			min = keylist.size();
		else
			min = size();

		// Compare each key	
		for (int x = 0; x < min; x++) {
			if (getValue(x) == null && keylist.getValue(x) != null)
				return false;
			if (getValue(x) != null && keylist.getValue(x) == null)
				return false;
			if (!getValue(x).equals(keylist.getValue(x))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Return the number of keys in the list.
	 * @return int
	 */
	public int size() {
		return getInternalKeyList().size();
	}
	
	private void rangeCheck(int index) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Index "+index+" is less than 0");
		}
		if (index >= size()) {
			if (size() == 0) {
				throw new IndexOutOfBoundsException("Index "+index+" is invalid. The Keylist has no keys.");
			} else {
				throw new IndexOutOfBoundsException("Index "+index+" is greater than the maximum index of "+(size()-1));
			}
		}
	}
	
	/**
	 * Return the key name at 'index'
	 * @param index int
	 * @return String
	 */
	public String getName(int index) {
		rangeCheck(index);
		KeyColumn keyColumn = getInternalKeyList().get(index);
		return keyColumn.getName();
	}
	
	/**
	 * Return the key value at 'index'.
	 * @return Object
	 * @param index int
	 */
	public Object getValue(int index) {
		rangeCheck(index);
		KeyColumn keyColumn = getInternalKeyList().get(index);
		return keyColumn.getValue();
	}
	
	/**
	 * Convenience method to get the value at a specific index as an int.
	 * @return int
	 * @param index int
	 */
	public int getValueAsInt(int index) {
		Object obj = getValue(index);
		if (obj instanceof Integer) {
			Integer iValue = (Integer) obj;
			return iValue.intValue();
		} else
			throw new ClassCastException("Object " + obj + ", index " + index + " is not an Integer");
	}
	
	/**
	 * Convenience method to get the value at a specific index as a long.
	 * @return long
	 * @param index int
	 */
	public long getValueAsLong(int index) {
		Object obj = getValue(index);
		if (obj instanceof Long) {
			Long iValue = (Long) obj;
			return iValue.longValue();
		} else
			throw new ClassCastException("Object " + obj + ", index " + index + " is not a Long");
	}
	
	/**
	 * Convenience method to get the value at a specific index as a String.
	 * @return String
	 * @param index int
	 */
	public String getValueAsString(int index) {
		Object obj = getValue(index);
		if (obj instanceof String)
			return (String) obj;
		else
			throw new ClassCastException("Object " + obj + ", index " + index + " is not a String");
	}

	/**
	 * Convenience method to get the value at a specific index as a Timestamp.
	 * 
	 * @return Timestamp
	 * @param index int
	 */
	public Timestamp getValueAsTimestamp(int index) {
		Object obj = getValue(index);
		if (obj instanceof Timestamp)
			return (Timestamp) obj;
		else
			throw new ClassCastException("Object " + obj + ", index " + index + " is not a Timestamp");
	}

	/**
	 * Convenience method to get the value at a specific index as a java.sql.Date.
	 * @return Date
	 * @param index int
	 */
	public java.sql.Date getValueAsDate(int index) {
		Object obj = getValue(index);
		if (obj instanceof java.sql.Date)
			return (java.sql.Date) obj;
		else
			throw new ClassCastException("Object " + obj + ", index " + index + " is not a java.sql.Date");
	}

	/**
	 * Convenience method to get the value at a specific index as a BigDecimal.
	 * @return BigDecimal
	 * @param index int
	 */
	public BigDecimal getValueAsBigDecimal(int index) {
		Object obj = getValue(index);
		if (obj instanceof BigDecimal)
			return (BigDecimal) obj;
		else
			throw new ClassCastException("Object " + obj + ", index " + index + " is not a BigDecimal");
	}

	/**
	 * Convenience method to get the value at a specific index as a double.
	 * @return double
	 * @param index int
	 */
	public double getValueAsDouble(int index) {
		Object obj = getValue(index);
		if (obj instanceof Double)
			return ((Double)obj).doubleValue();
		else
			throw new ClassCastException("Object " + obj + ", index " + index + " is not a Double");
	}

	/**
	 * Convenience method to get the value at a specific index as a float.
	 * @return float
	 * @param index int
	 */
	public float getValueAsFloat(int index) {
		Object obj = getValue(index);
		if (obj instanceof Float)
			return ((Float)obj).floatValue();
		else
			throw new ClassCastException("Object " + obj + ", index " + index + " is not a Float");
	}

	/**
	 * Convenience method to get the value at a specific index as a short.
	 * @return short
	 * @param index int
	 */
	public short getValueAsShort(int index) {
		Object obj = getValue(index);
		if (obj instanceof Short) {
			Short iValue = (Short) obj;
			return iValue.shortValue();
		} else
			throw new ClassCastException("Object " + obj + ", index " + index + " is not a Short");
	}

	/**
	 * Return a string representing the full key with trimmed values. The results of this method are not suitable for 
	 * display to a user. This is normally used as a unique String representation of the key.
	 * 
	 * @return String
	 */
	public String toDefinitionString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Key[");
		for (int x = 0; x < size(); x++) {
			if (x > 0)
				sb.append(":");
			Object obj = getValue(x);
			if (obj == null) {
				obj = "null";
			}
			sb.append(obj.toString().trim());
		}
		sb.append("]");
		return sb.toString();
	}

	public String toString() {
		return toDefinitionString();
	}

	/**
	 * Return true if the keylist contains a null value
	 * 
	 * @return boolean
	 */
	public boolean containsNull() {
		for (int x = 0; x < size(); x++) {
			if (getValue(x) == null)
				return true;
		}
		return false;
	}

	/**
	 * Add a new KeyColumn to the end of the KeyList
	 * 
	 * @param name
	 * @param value
	 */
	public void add(String name, Object value) {
		KeyColumn keyColumn = new KeyColumn();
		keyColumn.setName(name);
		keyColumn.setValue(value);
		add(keyColumn);
	}

	/**
	 * Add a new KeyColumn to the end of the KeyList
	 * 
	 * @param keyColumn KeyColumn
	 */
	public void add(KeyColumn keyColumn) {
		// Check the column is not in the list already
		Iterator<KeyColumn> iter = klist.iterator();
		while (iter.hasNext()) {
			KeyColumn column = (KeyColumn) iter.next();
			if (column.getName().equals(keyColumn.getName())) {
				throw new IllegalArgumentException("Key column "+keyColumn.getName()+" is already in the KeyList");
			}
		}

		klist.add(keyColumn);
		
		// Build the uniqueID
		uniquePropertyID.append(keyColumn.getName());
		uniquePropertyID.append(";");
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}

		// Cast the object
		KeyList keylist = (KeyList) obj;

		// Compare the number of keys
		if (keylist.size() != size())
			return false;

		// Compare each key	
		for (int x = 0; x < size(); x++) {
			if (getValue(x) == null && keylist.getValue(x) != null)
				return false;
			if (getValue(x) != null && keylist.getValue(x) == null)
				return false;
			if (!getValue(x).equals(keylist.getValue(x))) {
				return false;
			}
		}

		return true;
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + ((klist == null) ? 0 :klist.hashCode());
		return hash;
	}
	
	/**
	 * Return a unique ID. This ID is intended to be used for caching SQL statements. The 
	 * property names are part of the uniqueID, but the values are not.
	 * 
	 * @return String
	 */
	public String getUniquePropertyID() {
		return uniquePropertyID.toString();
	}

	/**
	 * Return true if the propertyName exists in the keyList
	 * 
	 * @param propertyName
	 * @return boolean
	 */
	public boolean contains(String propertyName) {
		Iterator<KeyColumn> iter = getInternalKeyList().iterator();
		while (iter.hasNext()) {
			KeyColumn keyColumn = iter.next();
			if (keyColumn.getName().equals(propertyName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param propertyName
	 * @return Object The value for the propertyName
	 */
	public Object getValueByName(String propertyName) {
		Iterator<KeyColumn> iter = getInternalKeyList().iterator();
		while (iter.hasNext()) {
			KeyColumn keyColumn = iter.next();
			if (keyColumn.getName().equals(propertyName)) {
				return keyColumn.getValue();
			}
		}
		return null;
	}
}

