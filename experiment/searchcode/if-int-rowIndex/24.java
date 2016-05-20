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
package org.rimudb.editor;

import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.event.*;

import org.rimudb.configuration.FieldEntry;
import org.rimudb.editor.swing.*;
import org.rimudb.editor.swing.plaf.mac.*;

/**
 * This TableModel class is used to display the table descriptor columns.
 * 
 * @author Simon Ritchie
 *
 */
public class PropertyTableModel extends ATableModel {
	private static final long serialVersionUID = 682967173406069693L;
	private List<FieldEntry> list = null;
	
	
	/**
	 * Construct a PropertyTableModel
	 */
	public PropertyTableModel() {
		list = new ArrayList<FieldEntry>();
	}

	/**
	 * Add an entry to the TableModel
	 * @param entry FieldEntry
	 */
	public void addEntry(FieldEntry entry) {
		list.add(entry);
		fireTableRowsInserted(getRowCount()-1, getRowCount()-1);
	}

	/**
	 * Insert an entry in the TableModel at a given row
	 * @param entry FieldEntry
	 * @param row int
	 */
	public void insertEntry(FieldEntry entry, int row) {
		list.add(row, entry);
		fireTableRowsInserted(row, row);
	}

	/**
	 * Return all the entries
	 * @return FieldEntry[]
	 */
	public FieldEntry[] getAllRows() {
		FieldEntry entries[] = new FieldEntry[list.size()];
		list.toArray(entries);
		return entries;
	}
	
	/**
	 * Returns the lowest common denominator Class in the column.  This is used
	 * by the table to set up a default renderer and editor for the column.
	 *
	 * @return the common ancestor class of the object values in the model.
	 */
	public java.lang.Class getColumnClass(int columnIndex) {
		switch (columnIndex) {
			case 0 :
				return String.class;
			case 1 :
				return String.class;
			case 2 :
				return String.class;
			case 3 :
				return Integer.class;
			case 4 :
				return Integer.class;
			case 5 :
				return Boolean.class;
			case 6 :
				return Boolean.class;
			case 7 :
				return Boolean.class;
			case 8 :
				return Boolean.class;
			case 9 :
				return String.class;
		}
		return null;
	}
	
	/**
	 * Returns the number of columns managed by the data source object. A
	 * <B>JTable</B> uses this method to determine how many columns it
	 * should create and display on initialization.
	 *
	 * @return the number or columns in the model
	 * @see #getRowCount
	 */
	public int getColumnCount() {
		return 10;
	}
	
	/**
	 * Returns the name of the column at <i>columnIndex</i>.  This is used
	 * to initialize the table's column header name.  Note, this name does
	 * not need to be unique.  Two columns on a table can have the same name.
	 *
	 * @param	columnIndex	the index of column
	 * @return  the name of the column
	 */
	public java.lang.String getColumnName(int columnIndex) {
		switch (columnIndex) {
			case 0 :
				return "Property";
			case 1 :
				return "Column name";
			case 2 :
				return "Type";
			case 3 :
				return "Size";
			case 4 :
				return "Dec";
			case 5 :
				return "Nulls";
			case 6 :
				return "RJ";
			case 7 :
				return "Ident";
			case 8 :
				return "Ver";
			case 9 :
				return "Seq";
		}
		return "";
	}
	
	/**
	 * Return an array of FieldEntry instances for key columns. The instances are sorted
	 * by key number.
	 * 
	 * @return FieldEntry[]
	 */
	public FieldEntry[] getFullKeyedRowsByKey() {
		TreeMap<Integer, FieldEntry> map = new TreeMap<Integer, FieldEntry>();
		
		// Get a list of entries with keys
		for (int x = 0; x < getRowCount(); x++) {
			FieldEntry entry = getRow(x);
			if (entry.getPrimaryKeyNbr() > 0) {
				map.put(entry.getPrimaryKeyNbr(), entry);
			}
		}
		
		Collection<FieldEntry> coll = map.values();
		return (FieldEntry[]) coll.toArray(new FieldEntry[coll.size()]);
	}

	/**
	 * Return the FieldEntry for a given clumnName
	 * @param columnName String
	 * @return FieldEntry
	 */
	public FieldEntry getFieldEntry(String columnName) {
		for (int x = 0; x < getRowCount(); x++) {
			FieldEntry entry = getRow(x);
			if (entry.getColumnName().equals(columnName)) {
				return entry;
			}
		}
		return null;
	}
	
	/**
	 * Convert a comma delimited String of column names to an
	 * array of FieldEntry instances.
	 * 
	 * @param columns String
	 * @return FieldEntry[]
	 */
	public FieldEntry[] getFieldEntriesByKey(String columns) {
		List<FieldEntry> list = new ArrayList<FieldEntry>();

		StringTokenizer st = new StringTokenizer(columns, ",");
		while (st.hasMoreTokens()) {
			String token = st.nextToken().trim();
			FieldEntry entry = getFieldEntry(token);
			if (entry != null) {
				list.add(entry);
			}
		}

		return (FieldEntry[]) list.toArray(new FieldEntry[list.size()]);
	}

	/**
	 * Return the FieldEntry for a given row
	 * @param row int
	 * @return FieldEntry
	 */
	public FieldEntry getRow(int row) {
		return (FieldEntry) list.get(row);
	}
	
	/**
	 * Returns the number of records managed by the data source object. A
	 * <B>JTable</B> uses this method to determine how many rows it
	 * should create and display.  This method should be quick, as it
	 * is call by <B>JTable</B> quite frequently.
	 *
	 * @return the number or rows in the model
	 * @see #getColumnCount
	 */
	public int getRowCount() {
		return list.size();
	}
	/**
	 * Returns an attribute value for the cell at <I>columnIndex</I>
	 * and <I>rowIndex</I>.
	 *
	 * @param	rowIndex	the row whose value is to be looked up
	 * @param	columnIndex 	the column whose value is to be looked up
	 * @return	the value Object at the specified cell
	 */
	public java.lang.Object getValueAt(int rowIndex, int columnIndex) {
		FieldEntry entry = getRow(rowIndex);
		switch (columnIndex) {
			case 0 :
				return entry.getPropertyName();
			case 1 :
				return entry.getColumnName();
			case 2 :
				return entry.getSQLColumnType();
			case 3 :
				return new Integer(entry.getColumnSize());
			case 4 :
				return new Integer(entry.getDecimalDigits());
			case 5 :
				return new Boolean(entry.isNullCapable());
			case 6 :
				return new Boolean(entry.isRightJustify());
			case 7 :
				return new Boolean(entry.isAutoIncrement());
			case 8 :
				return new Boolean(entry.isVersion());
			case 9 :
				return entry.getSequenceName();
		}
		return "";
	}
	
	/**
	 * Returns true if the cell at <I>rowIndex</I> and <I>columnIndex</I>
	 * is editable.  Otherwise, setValueAt() on the cell will not change
	 * the value of that cell.
	 *
	 * @param	rowIndex	the row whose value is to be looked up
	 * @param	columnIndex	the column whose value is to be looked up
	 * @return	true if the cell is editable.
	 * @see #setValueAt
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}
	
	/**
	 * Remove a row.
	 */
	public void removeRow(int row) {
		list.remove(row);
		fireTableRowsDeleted(row, row);
	}
	
	/**
	 * Clear the data in the model
	 */
	public void clear() {
		list.clear();
		fireTableDataChanged();
	}
	
	/**
	 * Move the row up in the table
	 */
	public void moveRowUp(int row) {
		// Retrieve the data in the row
		FieldEntry entry = getRow(row);
		
		// Remove the row
		removeRow(row);
		
		// Insert the entry in the prior row
		insertEntry(entry, row-1);
	}
	
	/**
	 * Move the row down in the table
	 */
	public void moveRowDown(int row) {
		// Retrieve the data in the row
		FieldEntry entry = getRow(row);
		
		// Remove the row
		removeRow(row);
		
		// Insert the entry in the prior row
		insertEntry(entry, row+1);
	}
	
	/**
	 * Sets an attribute value for the record in the cell at
	 * <I>columnIndex</I> and <I>rowIndex</I>.  <I>aValue</I> is
	 * the new value.
	 *
	 * @param	aValue		 the new value
	 * @param	rowIndex	 the row whose value is to be changed
	 * @param	columnIndex 	 the column whose value is to be changed
	 * @see #getValueAt
	 * @see #isCellEditable
	 */
	public void setValueAt(java.lang.Object aValue, int rowIndex, int columnIndex) {
		FieldEntry entry = getRow(rowIndex);
		Integer value = null;


		switch (columnIndex) {
			case 0 :
				String beforePropertyName = entry.getPropertyName();
				entry.setPropertyName((String) aValue);
				if (!beforePropertyName.equals(entry.getPropertyName())) {
					fireTableChanged(new PropertyTableModelEvent(this, rowIndex, rowIndex, columnIndex, beforePropertyName, aValue));
				}
				break;


			case 1 :
				String beforeColumnName = entry.getColumnName();
				entry.setColumnName((String) aValue);
				if (!beforeColumnName.equals(entry.getColumnName())) {
					fireTableChanged(new PropertyTableModelEvent(this, rowIndex, rowIndex, columnIndex, beforeColumnName, aValue));
				}
				break;


			case 2 :
				String beforeSQLColumnType = entry.getSQLColumnType();
				entry.setSQLColumnType((String) aValue);
				if (!beforeSQLColumnType.equals(entry.getSQLColumnType())) {
					fireTableChanged(new PropertyTableModelEvent(this, rowIndex, rowIndex, columnIndex, beforeSQLColumnType, aValue));
				}
				break;


			case 3 :
				if (aValue instanceof String)
					value = new Integer((String) aValue);
				if (aValue instanceof Integer)
					value = (Integer) aValue;
				int beforeColumnSize = entry.getColumnSize();
				entry.setColumnSize(value.intValue());
				if (beforeColumnSize != entry.getColumnSize()) {
					fireTableChanged(new PropertyTableModelEvent(this, rowIndex, rowIndex, columnIndex, beforeColumnSize, value));
				}
				break;


			case 4 :
				if (aValue instanceof String)
					value = new Integer((String) aValue);
				if (aValue instanceof Integer)
					value = (Integer) aValue;
				int beforeDecimalDigits = entry.getDecimalDigits();
				entry.setDecimalDigits(value.intValue());
				if (beforeDecimalDigits != entry.getDecimalDigits()) {
					fireTableChanged(new PropertyTableModelEvent(this, rowIndex, rowIndex, columnIndex, beforeDecimalDigits, value));
				}
				break;
				
			case 5:		// Allow nulls
				if (aValue instanceof Boolean) {
					boolean beforeNullable = entry.isNullCapable();
					Boolean booleanValue = (Boolean)aValue;
					entry.setNullCapable(booleanValue.booleanValue());
					if (beforeNullable != entry.isNullCapable()) {
						fireTableChanged(new PropertyTableModelEvent(this, rowIndex, rowIndex, columnIndex, beforeNullable, value));
					}
				}
				break;
				
			case 6:		// Right justify
				if (aValue instanceof Boolean) {
					boolean beforeRightJustify = entry.isRightJustify();
					Boolean booleanValue = (Boolean)aValue;
					entry.setRightJustify(booleanValue.booleanValue());
					if (beforeRightJustify != entry.isRightJustify()) {
						fireTableChanged(new PropertyTableModelEvent(this, rowIndex, rowIndex, columnIndex, beforeRightJustify, value));
					}
				}
				break;
				
			case 7:		// Auto increment
				if (aValue instanceof Boolean) {
					boolean beforeAutoIncrement = entry.isAutoIncrement();
					Boolean booleanValue = (Boolean)aValue;
					entry.setAutoIncrement(booleanValue.booleanValue());
					if (beforeAutoIncrement != entry.isAutoIncrement()) {
						fireTableChanged(new PropertyTableModelEvent(this, rowIndex, rowIndex, columnIndex, beforeAutoIncrement, value));
					}
				}
				break;
				
			case 8:		// Version
				if (aValue instanceof Boolean) {
					boolean beforeVersion = entry.isVersion();
					Boolean booleanValue = (Boolean)aValue;
					entry.setVersion(booleanValue.booleanValue());
					if (beforeVersion != entry.isVersion()) {
						fireTableChanged(new PropertyTableModelEvent(this, rowIndex, rowIndex, columnIndex, beforeVersion, value));
					}
				}
				break;
				
			case 9:		// Sequence
				String beforeSequence = entry.getSequenceName();
				entry.setSequenceName((String) aValue);
				if (!beforeSequence.equals(entry.getSequenceName())) {
					fireTableChanged(new PropertyTableModelEvent(this, rowIndex, rowIndex, columnIndex, beforeSequence, value));
				}
				break;
		}
		
		
	}
	
	/**
	 * @see ATableModel#getColumnWidthPercent(int)
	 */
	public int getColumnWidthPercent(int col) {
		switch (col) {
			case 0 :
				return 15;	// Property
			case 1 :
				return 15;	// Column name
			case 2 :
				return 8;	// Type
			case 3 :
				return 6;	// Size
			case 4 :
				return 6;	// Decimals
			case 5 :
				return 6;	// Allow nulls
			case 6 :
				return 6;	// Right Justify
			case 7 :
				return 6;	// Auto increment
			case 8 :
				return 6;	// Version
			case 9 :
				return 20;	// Sequence
			default :
				return 1;
		}
	}

	/**
	 * Return an array of all the column names
	 */
	public String[] getAllColumnNames() {
		List<String> list = new ArrayList<String>();
		for (int x=0; x < getRowCount(); x++) {
			FieldEntry fieldEntry = getRow(x);
			list.add(fieldEntry.getColumnName());
		}
		return (String[]) list.toArray(new String[list.size()]);
	}
}

