package trussoptimizater.Gui.GUIModels;

import java.util.ArrayList;
import java.util.Observable;
import javax.swing.table.AbstractTableModel;
import trussoptimizater.Truss.TrussModel;
import trussoptimizater.Truss.ElementModels.NodeModel;
import trussoptimizater.Truss.Elements.Node;

public class NodeTableModel extends ElementTableModel<Node> {

	private final String[] COLUMN_NAMES = { "Node",
			"<HTML><CENTER>X<BR>(m)</CENTER></HTML>",
			"<HTML><CENTER>Z<BR>(m)</CENTER></HTML>", "Loaded", "Supported" };
	private final boolean[] EDITABLE_COLUMNS = { false, true, true, false,
			false };
	private final int COLUMN_COUNT = COLUMN_NAMES.length;
	public static final int NODE_NUMBER_COLOUMN_INDEX = 0;
	public static final int X_CORDINATE_COLUMN_INDEX = 1;
	public static final int Z_CORDINATE_COLUMN_INDEX = 2;
	public static final int LOADED_COLUMN_INDEX = 3;
	public static final int SUPPORTED_COLUMN_INDEX = 4;

	public NodeTableModel(TrussModel truss) {
		super(truss, truss.getNodeModel());
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
		return EDITABLE_COLUMNS[col];

	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (model.size() == 0) {
			return new Object();
		}
		switch (columnIndex) {
		case NodeTableModel.NODE_NUMBER_COLOUMN_INDEX:
			return model.get(rowIndex).getNumber();
		case NodeTableModel.X_CORDINATE_COLUMN_INDEX:
			return model.get(rowIndex).getPoint().getX() / 100;
		case NodeTableModel.Z_CORDINATE_COLUMN_INDEX:
			return model.get(rowIndex).getPoint().getY() / 100;
		case NodeTableModel.LOADED_COLUMN_INDEX:
			return model.get(rowIndex).isLoaded();
		case NodeTableModel.SUPPORTED_COLUMN_INDEX:
			return model.get(rowIndex).isSupported();
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
		case NodeTableModel.X_CORDINATE_COLUMN_INDEX:
			double i = Double.parseDouble(aValue.toString());
			model.get(rowIndex).setX(i * 100);
			break;
		case NodeTableModel.Z_CORDINATE_COLUMN_INDEX:
			i = Double.parseDouble(aValue.toString());
			model.get(rowIndex).setZ(i * 100);
			break;
		}
		this.fireTableCellUpdated(rowIndex, columnIndex);

	}

	@Override
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

}// end of NodeTableModel

