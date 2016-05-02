package gui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class TableData extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	protected SimpleDateFormat m_frm;
	public Vector<String[]> m_vector;
	protected Date m_date;

	public TableData() {
		m_frm = new SimpleDateFormat("MM/dd/yyyy");
		m_vector = new Vector<String[]>();
		m_vector.removeAllElements();
	}

	public int getRowCount() {
		return 45;
	}

	public int getColumnCount() {
		return 4;
	}

	public Object getValueAt(int nRow, int nCol) {
		if (nRow >= m_vector.size())
			return "";
		String[] data = (String[]) m_vector.get(nRow);

		if (nCol >= data.length - 1)
			return "";

		return data[nCol];
	}

}

