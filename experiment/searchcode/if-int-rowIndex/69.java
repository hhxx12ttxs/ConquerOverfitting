package com.objectwave.persist.gui;

import com.objectwave.persist.*;
import javax.swing.table.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *  The <B>TableModel</B> interface ispecifies the methods the JTable 
 *  will use to interrogate a tabular data model. <p>
 *
 *  The JTable can be set up to display any data model which implements the 
 *  TableModel interface with a couple of lines of code:  <p>
 *  <code>
 *  TableModel myData = new MyTableModel(); 
 *  JTable table = new JTable(myData);
 *  </code><p>
 *
 * @version 1.2
 * @author Dave Hoag
 * @see JTable
 * @see AbstractTableModel
 */
public class PersistenceAttributeTableModel extends AbstractTableModel
{
	Persistence subject;
	
	/**
	 * Returns the lowest common denominator Class in the column.  This is used
	 * by the table to set up a default renderer and editor for the column.
	 *
	 * @return the common ancestor class of the object values in the model.
	 */
	public Class getColumnClass(int columnIndex)
	{
		return java.lang.String.class;
	}
	/**
	 * Returns the number of columns managed by the data source object. A
	 * <B>JTable</B> uses this method to determine how many columns it
	 * should create and display on initialization.
	 *
	 * @return the number or columns in the model
	 * @see #getRowCount()
	 */
	public int getColumnCount()
	{
		return 2;
	}
	/**
	 * Returns the name of the column at <i>columnIndex</i>.  This is used
	 * to initialize the table's column header name.  Note, this name does
	 * not need to be unique.  Two columns on a table can have the same name.
	 *
	 * @param	columnIndex	the index of column
	 * @return  the name of the column
	 */
	public String getColumnName(int columnIndex)
	{
		if(columnIndex == 0) return "Attribute";
		return "Value";
	}
	/**
	 * Returns the number of records managed by the data source object. A
	 * <B>JTable</B> uses this method to determine how many rows it
	 * should create and display.  This method should be quick, as it
	 * is call by <B>JTable</B> quite frequently.
	 *
	 * @return the number or rows in the model
	 * @see #getColumnCount()
	 */
	public int getRowCount()
	{
		if(subject == null) return 0;
		RDBPersistence pObj = null;
		if(subject.usesAdapter())
			pObj = (RDBPersistence)subject.getAdapter();
		else
			pObj = (RDBPersistence)subject;

		AttributeTypeColumn [] colDefs = pObj.getAttributeDescriptions();
		return colDefs.length;
	}
	/**
	 * Returns an attribute value for the cell at <I>columnIndex</I>
	 * and <I>rowIndex</I>.
	 *
	 * @param	rowIndex	the row whose value is to be looked up
	 * @param	columnIndex 	the column whose value is to be looked up
	 * @return	the value Object at the specified cell
	 */
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		RDBPersistence pObj = null;
		if(subject.usesAdapter())
			pObj = (RDBPersistence)subject.getAdapter();
		else
			pObj = (RDBPersistence)subject;

		AttributeTypeColumn [] colDefs = pObj.getAttributeDescriptions();
		if(columnIndex ==0)
			return colDefs[rowIndex].getField().getName();
		else
			return colDefs[rowIndex].getValue(subject);
	}
	/**
	 * Returns true if the cell at <I>rowIndex</I> and <I>columnIndex</I>
	 * is editable.  Otherwise, setValueAt() on the cell will not change
	 * the value of that cell.
	 *
	 * @param	rowIndex	the row whose value is to be looked up
	 * @param	columnIndex	the column whose value is to be looked up
	 * @return	true if the cell is editable.
	 * @see #setValueAt()
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return columnIndex == 1;
	}
	/**
	*/
	public void setSubject(Persistence p)
	{
		subject = p;
		fireTableChanged (new TableModelEvent(this, -1)); 
	}
	/**
	 * Sets an attribute value for the record in the cell at
	 * <I>columnIndex</I> and <I>rowIndex</I>.  <I>aValue</I> is
	 * the new value.
	 *
	 * @param	aValue		 the new value
	 * @param	rowIndex	 the row whose value is to be changed
	 * @param	columnIndex 	 the column whose value is to be changed
	 * @see #getValueAt()
	 * @see #isCellEditable()
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		RDBPersistence pObj = null;
		if(subject.usesAdapter())
			pObj = (RDBPersistence)subject.getAdapter();
		else
			pObj = (RDBPersistence)subject;

		AttributeTypeColumn [] colDefs = pObj.getAttributeDescriptions();

		if(aValue != null)
		{
			aValue = new ObjectFormatter().convertString(colDefs[rowIndex].getField().getType(), aValue.toString());

			AttributeTypeColumn [] cols = new AttributeTypeColumn [1]; 
			cols[0] = colDefs[rowIndex];
			Object [] data = new Object [1];
			data [0] = aValue;
			new ObjectFormatter().convertTypes(data, cols);
			aValue = data[0];
		}
		if(columnIndex !=0)
		{
			colDefs[rowIndex].setValue(subject, aValue);
		}
		try { 
			subject.save();
		} catch (Exception e)
			{
				JOptionPane.showMessageDialog(
					null, "Failed to save update.", 
					e.toString(), JOptionPane.ERROR_MESSAGE);
			}
	}
}
