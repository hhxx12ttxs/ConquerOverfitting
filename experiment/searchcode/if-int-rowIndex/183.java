package org.ggp;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class VerticeTableModel implements TableModel{

	
	String[] columnNames = {"Vértice", "X", "Y", "Z"};
	Double[] valuesX;
	Double[] valuesY;
	Double[] valuesZ;
	Integer[] numVertice;
	
	
	
	public VerticeTableModel(int numVertice) {
		criaNumVertice(numVertice);
		valuesX = new Double[numVertice];
		valuesY = new Double[numVertice];
		valuesZ = new Double[numVertice];
	}
	
	public void criaNumVertice(int numVertice) {
		this.numVertice = new Integer[numVertice];
		for (int i = 0; i < numVertice; i++) {
			this.numVertice[i] = new Integer(i+1);
		}
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return Integer.class;
		} else {
			return Double.class;
		}
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	@Override
	public int getRowCount() {
		return numVertice.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return numVertice[rowIndex];
		case 1:
			return valuesX[rowIndex];
		case 2:
			return valuesY[rowIndex];
		case 3:
			return valuesZ[rowIndex];
		default:
			return null;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return false;
		}
		return true;
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		switch (columnIndex) {		
		case 1:
			valuesX[rowIndex] = (Double) aValue;
			break;
		case 2:
			valuesY[rowIndex] = (Double) aValue;
			break;
		case 3:
			valuesZ[rowIndex] = (Double) aValue;
			break;
		default:
			return;
		}
	}

}

