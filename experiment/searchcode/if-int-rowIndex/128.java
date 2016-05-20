package trussoptimizater.Gui.GUIModels;

import trussoptimizater.Truss.TrussModel;
import trussoptimizater.Truss.Elements.Node;
import trussoptimizater.Truss.Elements.Support;
import trussoptimizater.Truss.Sections.TubularSection;

//private inner class
public class SupportTableModel extends ElementTableModel<Support> {

	private final String[] COLUMN_NAMES = { "Support", "Node", "UX", "UZ", "RY" };
	private final boolean[] editableColumns = { false, true, true, true, true };
	private final int COLUMN_COUNT = COLUMN_NAMES.length;
	public static final int SUPPORT_NUMBER_COLOUMN_INDEX = 0;
	public static final int NODE_COLOUMN_INDEX = 1;
	public static final int UX_COLUMN_INDEX = 2;
	public static final int UZ_COLUMN_INDEX = 3;
	public static final int RY_COLUMN_INDEX = 4;

	public SupportTableModel(TrussModel truss) {
		super(truss, truss.getSupportModel());
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
		case SupportTableModel.SUPPORT_NUMBER_COLOUMN_INDEX:
			return model.get(rowIndex).getNumber();
		case SupportTableModel.NODE_COLOUMN_INDEX:
			return model.get(rowIndex).getNode().getNumber();
		case SupportTableModel.UX_COLUMN_INDEX:
			return model.get(rowIndex).getUx();
		case SupportTableModel.UZ_COLUMN_INDEX:
			return model.get(rowIndex).getUz();
		case SupportTableModel.RY_COLUMN_INDEX:
			return model.get(rowIndex).getRy();
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
		case SupportTableModel.SUPPORT_NUMBER_COLOUMN_INDEX:
			// Not ediablte
			break;
		case SupportTableModel.NODE_COLOUMN_INDEX:
			int nodeNumber = Integer.parseInt(aValue.toString());
			Node node = truss.getNodeModel().get(nodeNumber - 1);
			model.get(rowIndex).setNode(node);
			break;
		case SupportTableModel.UX_COLUMN_INDEX:
			model.get(rowIndex).setUx(aValue.toString());
			break;
		case SupportTableModel.UZ_COLUMN_INDEX:
			model.get(rowIndex).setUz(aValue.toString());
			break;
		case SupportTableModel.RY_COLUMN_INDEX:
			model.get(rowIndex).setRy(aValue.toString());
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


