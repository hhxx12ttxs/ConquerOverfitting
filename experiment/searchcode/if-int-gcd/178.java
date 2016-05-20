/**
 *
 */
package com.wateray.ipassbook.ui.model;

import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.wateray.ipassbook.domain.Entity;

/**
 * @author c-bryu
 * 
 */
public abstract class AbstractPassbookTableModel extends AbstractTableModel {

	/**
         *
         */
	private static final long serialVersionUID = 1L;

	protected Vector<String> columnNames;
	protected Vector<Entity> dataVector;

	public AbstractPassbookTableModel(Vector<String> columnNames) {
		this.dataVector = new Vector<Entity>();
		this.columnNames = columnNames;

	}

	public AbstractPassbookTableModel(Vector<Entity> dataVector,
			Vector<String> columnNames) {
		this.dataVector = dataVector;
		this.columnNames = columnNames;
	}

	public Vector<Entity> getData() {
		return dataVector;
	}

	public void setData(Vector<Entity> entityVector) {
		dataVector = entityVector;
		this.fireTableDataChanged();

	}

	public Vector<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(Vector<String> columnNames) {
		this.columnNames = columnNames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return columnNames.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return dataVector.size();
	}

	public String getColumnName(int col) {
		return columnNames.get(col);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public abstract Object getValueAt(int rowIndex, int columnIndex);

	/**
	 * @param rowIndex
	 * @return Object the row's Object.
	 * */
	public Entity getRowObject(int rowIndex) {
		return dataVector.get(rowIndex);
	}

	public void addRow(Entity rowData) {
		insertRow(getRowCount(), rowData);
	}

	public void addRow(Entity[] rowData) {
		insertRow(getRowCount(), rowData);
	}

	public void insertRow(int row, Entity rowData) {
		dataVector.insertElementAt(rowData, row);
		fireTableRowsInserted(row, row);
	}

	public void insertRow(int row, Entity[] rowData) {
		for (int i = 0; i < rowData.length; i++) {
			dataVector.insertElementAt(rowData[i], row + i);
		}
		fireTableRowsInserted(row, row + rowData.length - 1);
	}

	public void removeRow(int row) {
		dataVector.removeElementAt(row);
		fireTableRowsDeleted(row, row);
	}

	public void rowsRemoved(TableModelEvent event) {
		fireTableChanged(event);
	}

	public void moveRow(int start, int end, int to) {
		int shift = to - start;
		int first, last;
		if (shift < 0) {
			first = to;
			last = end;
		} else {
			first = start;
			last = to + end - start;
		}
		rotate(dataVector, first, last + 1, shift);

		fireTableRowsUpdated(first, last);
	}

	public void updateRow(int row, Entity newRowData) {
		// remove old rowDate.
		dataVector.removeElementAt(row);
		
		// insert new rowData.
		dataVector.insertElementAt(newRowData, row);//ADD 2009/05/28
//		insertRow(row, newRowData); //DEL 2009/05/28

		fireTableRowsUpdated(row, row);
	}

	private static int gcd(int i, int j) {
		return (j == 0) ? i : gcd(j, i % j);
	}

	@SuppressWarnings("unchecked")
	private static void rotate(Vector v, int a, int b, int shift) {
		int size = b - a;
		int r = size - shift;
		int g = gcd(size, r);
		for (int i = 0; i < g; i++) {
			int to = i;
			Object tmp = v.elementAt(a + to);
			for (int from = (to + r) % size; from != i; from = (to + r) % size) {
				v.setElementAt(v.elementAt(a + from), a + to);
				to = from;
			}
			v.setElementAt(tmp, a + to);
		}
	}
}

