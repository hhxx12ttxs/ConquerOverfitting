package trussoptimizater.Gui.GUIModels;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import trussoptimizater.Truss.TrussModel;
import trussoptimizater.Truss.Elements.Load;
import trussoptimizater.Truss.Elements.Node;
import trussoptimizater.Truss.Sections.TubularSection;

//private inner class
public class LoadTableModel extends ElementTableModel<Load> {

	private final String[] COLUMN_NAMES = { "Load", "Node",
			"<HTML><CENTER>Load<BR>(KN)</CENTER></HTML>", "Orientation" };
	private final boolean[] editableColumns = { false, true, true, true };
	private final int COLUMN_COUNT = COLUMN_NAMES.length;
	public static final int LOAD_NUMBER_COLOUMN_INDEX = 0;
	public static final int NODE_COLOUMN_INDEX = 1;
	public static final int LOAD_COLUMN_INDEX = 2;
	public static final int DIRECTION_COLUMN_INDEX = 3;

	public LoadTableModel(TrussModel truss) {
		super(truss, truss.getLoadModel());
	}

	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return editableColumns[col];

	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (model.size() == 0) {
			return new Object();
		}

		switch (columnIndex) {
		case LoadTableModel.LOAD_NUMBER_COLOUMN_INDEX:
			return model.get(rowIndex).getNumber();
		case LoadTableModel.NODE_COLOUMN_INDEX:
			return model.get(rowIndex).getNode().getNumber();
		case LoadTableModel.LOAD_COLUMN_INDEX:
			return model.get(rowIndex).getLoad();
		case LoadTableModel.DIRECTION_COLUMN_INDEX:
			return model.get(rowIndex).getOrientation();
		default:
			return null;
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

		if (rowIndex > getRowCount() || columnIndex > COLUMN_COUNT) {
			throw new IndexOutOfBoundsException();
		}

		switch (columnIndex) {
		case LoadTableModel.LOAD_NUMBER_COLOUMN_INDEX:
			// Not ediablte
			break;
		case LoadTableModel.NODE_COLOUMN_INDEX:
			int nodeNumber = Integer.parseInt(aValue.toString());
			Node node = truss.getNodeModel().get(nodeNumber - 1);
			model.get(rowIndex).setNode(node);
			break;
		case LoadTableModel.LOAD_COLUMN_INDEX:
			double load = Double.parseDouble(aValue.toString());
			model.get(rowIndex).setLoad(load);
			break;
		case LoadTableModel.DIRECTION_COLUMN_INDEX:
			model.get(rowIndex).setOrientation(aValue.toString());
			break;
		default:
			break;
		}

		this.fireTableDataChanged();
	}

	@Override
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	/*
	 * public TubularSection getSection(String sectionName) { for (int i = 0; i
	 * < truss.getSections().size(); i++) { if
	 * (sectionName.equals(truss.getSections().get(i).getName())) { return
	 * truss.getSections().get(i); } } return null; }
	 */
}// end of NodeTableModel


