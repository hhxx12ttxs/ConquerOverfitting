package org.mypomodoro.gui;

import javax.swing.table.AbstractTableModel;

import org.mypomodoro.model.AbstractActivities;

public abstract class AbstractActivitiesTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 20110814L;
    protected Object[][] tableData;
    private final String[] columnNames;

    public AbstractActivitiesTableModel(String[] fields,
            AbstractActivities activities) {
        this.columnNames = fields;
        populateData(activities);
    }

    /**
     * Populates the table from the database.
     *
     * @param activities
     */
    protected abstract void populateData(AbstractActivities activities);

    @Override
    public int getRowCount() {
        return tableData.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= 0 && getRowCount() > 0) {
            return tableData[rowIndex][columnIndex];
        } else {
            return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int c) {
        return getValueAt(0, c) != null ? getValueAt(0, c).getClass() : null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        tableData[rowIndex][columnIndex] = aValue;
        fireTableCellUpdated(rowIndex, columnIndex);
    }
}

