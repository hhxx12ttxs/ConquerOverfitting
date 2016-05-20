/*
Copyright (C) 2013 by Florian SIMON

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package org.jew.swing.dataset.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.jew.swing.dataset.TableCellStyle;
import org.jew.swing.dataset.DataType;
import org.jew.swing.dataset.EditionEvents;
import org.jew.swing.dataset.table.companies.datamodel.Company;
import org.jew.swing.table.JETable;
import org.jew.swing.table.JETableModel;


public class JTableDataSet <T>
{
	protected class Column{
		String name;
		String tooltips;
		double width;
		boolean visible;
		DataType type;
		public Column(
				final String name,
				final String tooltips,
				final double width, 
				final boolean visible,
				final DataType type) {
			
			this.name = name;
			this.tooltips = tooltips;
			this.width = width;
			this.visible = visible;
			this.type = type;
		}
	}
	
	
	public static final double FILL = -1;
	public static final double PREFERED = -2;
	
	protected Map<Integer, Double> columnWidthPercentages = new HashMap<Integer, Double>();
	
	protected List<Column> columns = new ArrayList<Column>();

	protected boolean[] columnVisbilities = null;

	protected Component displayComponent;

	public Component getDisplayComponent () {
		return this.displayComponent;
	}

	protected JETableModel<T> model;

	protected JTable table;

	protected TableCellStyleRenderer<T> tableCellStyleRenderer = new TableCellStyleRenderer<T>() {

		@Override
		public TableCellStyle computeDefaultStyle(int row, int column, T obj, TableCellStyle cellStyle) {
			cellStyle.setBackgroundColor(Color.WHITE);
			return cellStyle;
		}

		@Override
		public TableCellStyle computeSelectedStyle(int row, int column, T obj, TableCellStyle cellStyle) {
			cellStyle.setBackgroundColor(Color.LIGHT_GRAY);
			return cellStyle;
		}
	};

	protected Map<Integer, EditionEvents> tableMouseEventMap = new HashMap<Integer, EditionEvents>();

	public Map<Integer, EditionEvents> getTableMouseEventMap () {
		return this.tableMouseEventMap;
	}

	protected TableRowProcessData<T> tableRowProcessData;

	protected Map<Integer, T> tableValues = new LinkedHashMap<Integer, T>();
	
	protected Map<Integer, T> updatedValues = new LinkedHashMap<Integer, T>();
	
	protected List<Integer> removedValues = new ArrayList<Integer>();
	
	protected Object valuesLock = new Object();
		
	protected int columnIndex = -1;
	
	protected TableColumn tableColumn = new TableColumn() {		
		@Override
		public void setWidth(final double width) {
			columns.get(columnIndex).width = width;
			columnModelToView();
		}
		
		@Override
		public void setVisible(final boolean visible) {
			columns.get(columnIndex).visible = visible;
			columnModelToView();
		}
	};

	public JTableDataSet(){

		this.initComponents();    	
		this.initCellRenderer();

		this.tableRowProcessData = new TableRowProcessData<T>() {
			@Override
			public Object computeValue(T obj, int columnIndex) {
				return obj;
			}    		
		};
	}
	
	boolean tooltipsVisible;
	
	public JTableDataSet(
			final String[] columnNames,
			final DataType[] columnTypes,
			final double[] columnWidths,
			final TableRowProcessData<T> rowProcessData){
		
		this.setTableRowProcessData(rowProcessData);
		
		this.initComponents();    	
		this.initCellRenderer();
		
		this.columns.clear();
		for (int i = 0; i < columnNames.length; i++) {
			this.columns.add(new Column(
					columnNames[i], 
					null, 
					columnWidths[i], 
					true,
					columnTypes[i]));
		}
		
		this.columnModelToView();
	}

	public JTableDataSet(
			final String[] columnNames,
			final String[] columnTooltips,
			final DataType[] columnTypes,
			final double[] columnWidths,
			final TableRowProcessData<T> rowProcessData){

		this(columnNames, columnTypes, columnWidths, rowProcessData);
		this.setColumnTooltips(columnTooltips);
		this.tooltipsVisible = true;
		this.columnModelToView();
	}

	protected void columnModelToView() {
		String[] columnNames = new String[this.columns.size()];
		String[] columnTooltips = new String[this.columns.size()];
		double[] columnWidths = new double[this.columns.size()];
		
		for (int i = 0 ; i < this.columns.size() ; i++) {
			columnNames[i] = this.columns.get(i).name;
			columnTooltips[i] = this.columns.get(i).tooltips;
			columnWidths[i] = this.columns.get(i).width;
		}
				
		model.setColumnNames(columnNames);
		if(this.tooltipsVisible){
			model.setColumnTooltips(columnTooltips);
		}
			
		fireTableStructureChanged();
		
		TableColumnModel columnModel = table.getColumnModel();
		this.columnWidthPercentages.clear();
		for (int i = 0; i < columnWidths.length; i++) {
			if(columns.get(i).visible){
				if(columnWidths[i] > 0 && columnWidths[i] < 1){
					this.columnWidthPercentages.put(i, columnWidths[i]);					
				}	
				else if(columnWidths[i] == PREFERED){
					columnModel.getColumn(i).sizeWidthToFit();
				}
				else if(columnWidths[i] != FILL){					
					int columnWidth = (int) columnWidths[i];
					columnModel.getColumn(i).setMinWidth(columnWidth);
					columnModel.getColumn(i).setMaxWidth(columnWidth);
					columnModel.getColumn(i).setPreferredWidth(columnWidth);
				}
			}else{
				columnModel.getColumn(i).setMinWidth(0);
				columnModel.getColumn(i).setMaxWidth(0);
				columnModel.getColumn(i).setPreferredWidth(0);
			}
		}
		
		this.computePercentageColumnWidths();		
		this.initEditors();
	}

	public void addColumn(
			final String columnName,
			final String columnTooltip,
			final DataType columnType,
			final int columnWidth){

		this.columns.add(new Column(columnName, columnTooltip, columnWidth, true, columnType));
		this.columnModelToView();
	}

	public void removeColumn(final int index){
		this.columns.remove(index);
		this.columnModelToView();
	}
	
	public void addValue(final Integer id, final T value){
		synchronized (this.valuesLock) {
			this.updatedValues.put(id, value);
		}
	}
	
	public void updateValue(final Integer id, final T value){
		synchronized (this.valuesLock) {
			this.updatedValues.put(id, value);
		}
	}
	
	public void removeValue(final Integer id){
		synchronized (this.valuesLock) {
			this.removedValues.add(id);
		}
	}
	
	public void clearValues(){
		synchronized (this.valuesLock) {
			this.updatedValues.clear();
			this.removedValues.clear();
			this.tableValues.clear();
		}
	}
	
	public int getSize(){
		int size = -1;
		synchronized (this.valuesLock) {
			size = this.tableValues.size();
		}
		return size;
	}
	
	public T readValue(final Integer id){
		T retVal = null;
		synchronized (this.valuesLock) {
			retVal= this.tableValues.get(id);
		}
		return retVal;
	}
	
	public void readAllValues(final TableValuesReader<T> reader){
		synchronized (this.valuesLock) {
			for (Entry<Integer, T> entry : this.tableValues.entrySet()) {
				reader.readValue(entry.getKey(), entry.getValue());
			}
		}
	}
	
	public void addMouseListener(
			final MouseListener ml){

		this.table.addMouseListener(ml);

	}

	public void addMouseMotionListener(
			final MouseMotionListener mml){

		this.table.addMouseMotionListener(mml);

	}

	public void setAutoCreateRowSorter(
			final boolean enabled){

		this.table.setAutoCreateRowSorter(enabled);

	}

	public void setAutoResizeMode(
			final int autoResizeMode){

		this.table.setAutoResizeMode(autoResizeMode);

	}

	public void setTableCellStyleRenderer(
			final TableCellStyleRenderer<T> tableCellStyleRenderer){

		this.tableCellStyleRenderer = tableCellStyleRenderer;
	}


	public void setCellSelectionEnabled(
			final boolean enabled){

		this.table.setCellSelectionEnabled(enabled);

	}

	public void setColumnEditionEvent(
			final int columnIndex,
			final EditionEvents tme){

		this.tableMouseEventMap.put(columnIndex, tme);

	}

	public void setColumnNames(
			final String[] names){

		for (int i = 0; i < names.length; i++) {
			this.columns.get(i).name = names[i];
		}
		this.columnModelToView();
	}

	public void setColumnSelectionAllowed(
			final boolean enabled){

		this.table.setColumnSelectionAllowed(enabled);

	}

	public void setColumnTooltips(
			final String[] tooltips){

		for (int i = 0; i < tooltips.length; i++) {
			this.columns.get(i).tooltips = tooltips[i];
		}
		this.columnModelToView();
	}

	public void setColumnWidths(
			final double[] columnWidths){

		for (int i = 0; i < columnWidths.length; i++) {
			this.columns.get(i).width = columnWidths[i];
		}
		this.columnModelToView();
	}

	public void setColumnTypes (DataType[] columnTypes) {
		for (int i = 0; i < columnTypes.length; i++) {
			this.columns.get(i).type = columnTypes[i];
		}
		this.columnModelToView();
	}

	public void setDragEnabled(
			final boolean enabled){

		this.table.setDragEnabled(enabled);

	}

	public void setEnabled(
			final boolean enabled){

		this.table.setEnabled(enabled);

	}

	public void setRowHeight(
			final int rowHeight){

		this.table.setRowHeight(rowHeight);

	}

	public void setSelectionMode(
			final int model){

		this.table.setSelectionMode(model);

	}

	public void setTableRowProcessData(
			final TableRowProcessData<T> processData){

		this.tableRowProcessData = processData;

	}

	public int getSelectedRow()	{

		return this.table.getSelectedRow();
	}

	public int[] getSelectedRows(){

		return this.table.getSelectedRows();
	}

	@SuppressWarnings("unchecked")
	public T getSelectedValue()	{
		synchronized (this.valuesLock) {
			if(this.table.getSelectedRow() == -1){
				return null;
			}
			return (T) tableValues.values().toArray()[this.table.convertRowIndexToModel(this.table.getSelectedRow())];
		}		
	}

	@SuppressWarnings("unchecked")
	public T[] getSelectedValues(){

		int[] selRowsTab = this.table.getSelectedRows();
		Object[] selectedValues = new Object[selRowsTab.length];
		for (int i = 0; i < selRowsTab.length; i++) {
			selectedValues[i] = (T) tableValues.values().toArray()[this.table.convertRowIndexToModel(selRowsTab[i])];
		}

		return (T[])selectedValues;
	}

	public int getSelectedId(){		
		return getSelectedId(this.getSelectedValue());
	}

	public int[] getSelectedIds(){
		T[] values = this.getSelectedValues();
		int[] selectedIds = new int[values.length];
		for (int i = 0; i < values.length; i++) {
			selectedIds[i] = this.getSelectedId(values[i]);
		}
		return selectedIds;
	}

	protected int getSelectedId(T value){
		for (int id : this.tableValues.keySet()) {
			T currentValue = this.tableValues.get(id);
			if(value.equals(currentValue)){
				return id;
			}
		}		
		return -1;
	}

	public JTableHeader getTableHeader(){

		return this.table.getTableHeader();
	}

	public void fireTableDataChanged(){
		synchronized (this.valuesLock) {
			for (Integer id : this.removedValues) {
				this.tableValues.remove(id);
			}
			for (java.util.Map.Entry<Integer, T> entry : this.updatedValues.entrySet()) {
				this.tableValues.put(entry.getKey(), entry.getValue());
			}
			this.removedValues.clear();
			this.updatedValues.clear();
		}
		((AbstractTableModel)this.model).fireTableDataChanged();
	}

	public void fireTableStructureChanged()	{

		((AbstractTableModel)this.model).fireTableStructureChanged();
	}

	public void selectRow(final int row){
		this.table.changeSelection(row, 0, true, false);
	}

	public void selectRows(
			final int[] rows){

		for (int i = 0; i < rows.length; i++) {
			this.table.changeSelection(rows[i], 0, true, false);
		}
	}

	@SuppressWarnings("unchecked")
	public void selectValue(
			final T value){

		T[] valTab = null;
		synchronized (this.valuesLock) {			
			valTab = (T[]) this.tableValues.values().toArray();
		}
		for (int i = 0; i < valTab.length; i++) {
			if(valTab[i].equals(value)){
				this.table.changeSelection(this.table.convertRowIndexToView(i), 0, true, false);
				return;
			}
		}    
		this.table.clearSelection();
	}

	@SuppressWarnings("unchecked")
	public void selectValues(
			final T[] values){

		synchronized (this.valuesLock) {
			boolean selectionChanged = false;
			T[] valTab = (T[]) this.tableValues.values().toArray();
			for (int i = 0; i < valTab.length; i++) {
				for (int j = 0; j < values.length; j++) {			
					if(valTab[i].equals(values[j])){
						selectionChanged = true;
						this.table.changeSelection(this.table.convertRowIndexToView(i), 0, true, false);
					}
				}
			}
			if(! selectionChanged){
				this.table.clearSelection();
			}
		}
	}

	public TableColumn getColumn(final int index) {
		this.columnIndex = table.convertColumnIndexToModel(index);
		return this.tableColumn;
	}


	@SuppressWarnings("serial")
	protected void initCellRenderer(){

		TableCellRenderer renderer = new DefaultTableCellRenderer() {
			protected JCheckBox checkBox = new JCheckBox();
			protected TableCellStyle cellStyle = new TableCellStyle();

			@SuppressWarnings("unchecked")
			@Override
			public Component getTableCellRendererComponent(
					final JTable table, 
					final Object value,
					final boolean isSelected, 
					final boolean hasFocus,
					final int row, 
					final int column) {

				
				if (value instanceof String || value instanceof Enum) {
					this.setHorizontalAlignment(SwingConstants.CENTER);
				} else {
					this.setHorizontalAlignment(SwingConstants.RIGHT);
				}

				int columnIndex = table.convertColumnIndexToModel(column);
				Object data = value;
				if( ! (data instanceof Boolean) ){
					data = columns.get(columnIndex).type.format(data);
				}

				T obj = (T) tableValues.values().toArray()[table.convertRowIndexToModel(row)];
				
				if(isSelected){
					this.cellStyle = tableCellStyleRenderer.computeSelectedStyle(
							row, 
							columnIndex, 
							obj, 
							this.cellStyle);		
				}else{
					this.cellStyle = tableCellStyleRenderer.computeDefaultStyle(
							row, 
							columnIndex, 
							obj, 
							this.cellStyle);		
				}
				
									
				
				if (value !=null) {
					if(value instanceof Boolean) {
						this.checkBox.setOpaque(true);
						this.checkBox.setSelected((Boolean) value);
						this.checkBox.setHorizontalAlignment(SwingConstants.CENTER);
						this.checkBox.setEnabled(this.isEnabled());
						this.checkBox.setBackground(this.cellStyle.getBackgroundColor());						
						return checkBox;
					}
				}												
				this.setFont(this.cellStyle.getFont());
				this.setBackground(this.cellStyle.getBackgroundColor());
				this.setForeground(this.cellStyle.getForegroundColor());
									
				setValue(data);

				return this;
			}
		};
		this.table.setDefaultRenderer(Object.class, renderer);	
		this.table.setDefaultRenderer(Integer.class, renderer);	
		this.table.setDefaultRenderer(Double.class, renderer);	
		this.table.setDefaultRenderer(Float.class, renderer);	
		this.table.setDefaultRenderer(Boolean.class, renderer);	
		this.table.setDefaultRenderer(String.class, renderer);	
		this.table.setDefaultRenderer(List.class, renderer);	
	}

	@SuppressWarnings("serial")
	protected void initComponents()
	{

		this.table = new JETable();

		this.table.addHierarchyBoundsListener(new HierarchyBoundsListener() {			
			@Override
			public void ancestorResized(final HierarchyEvent event) {
				computePercentageColumnWidths();
			}
			
			@Override
			public void ancestorMoved(final HierarchyEvent event) {}
		});
		
		JETableModel<T> model = new JETableModel<T>(){
			@SuppressWarnings("unchecked")
			@Override
			public void setValueAt(
					final Object value, 
					final int row, 
					final int column) {

				if(tableMouseEventMap.containsKey(column)){
					T obj = (T) tableValues.values().toArray()[row];
					tableMouseEventMap.get(column).notifyValueChanged(obj, value);
				}
			}
			@Override
			public boolean isCellEditable(
					final int row, 
					final int column) {

				return tableMouseEventMap.containsKey(column);
			}
			@SuppressWarnings("unchecked")
			@Override
			public Object getValueAt(
					final int row, 
					final int col) {

				T value = (T) tableValues.values().toArray()[row];    			
				Object data = tableRowProcessData.computeValue(value, col);	
				return data;
			}
			@Override
			public Class<?> getColumnClass(final int col) {
				return columns.get(col).type.getCellType();
			}
		};
		model.setTableMap(this.tableValues);
		this.model = model;
	
		this.table.setModel(model);
		this.table.setEnabled(true);
		this.table.setCellSelectionEnabled(true);
		this.table.setDragEnabled(true);
		this.table.setColumnSelectionAllowed(false);
		this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);		
		this.table.setAutoCreateRowSorter(true); 
		this.table.setRowHeight(20);

		this.displayComponent = table;		
	}

	protected void computePercentageColumnWidths() {
		for(java.util.Map.Entry<Integer, Double> entry : columnWidthPercentages.entrySet()){
			double totalWidth = this.table.getWidth();
			int columnWidth = (int) (totalWidth * entry.getValue());
			int col = this.table.convertColumnIndexToView(entry.getKey());
			this.table.getColumnModel().getColumn(col).setMinWidth(columnWidth);
			this.table.getColumnModel().getColumn(col).setMaxWidth(columnWidth);
			this.table.getColumnModel().getColumn(col).setPreferredWidth(columnWidth);
		}
	}
	
	protected void initEditors() {
		for (int i = 0 ; i < this.table.getColumnModel().getColumnCount() ; i++) {
			final Column currentColumn = columns.get(i);
			if(currentColumn.type.getCellType().equals(List.class)){
				this.table.getColumnModel().getColumn(i).setCellEditor(
						this.createComboCellEditor(currentColumn));
			}
		}
	}

	protected TableCellEditor createComboCellEditor(final Column column) {
		JComboBox comboBox = new JComboBox();
		for (Object key : column.type.getDico().keySet()) {
			comboBox.addItem(key);					
		}
		comboBox.setRenderer(new DefaultListCellRenderer() {					
			@Override
			public Component getListCellRendererComponent(
					JList list, 
					Object value,
					int index, 
					boolean isSelected, 
					boolean cellHasFocus) {
				
				return super.getListCellRendererComponent(list, column.type.format(value), index, isSelected, cellHasFocus);
			}
		});
		return new DefaultCellEditor(comboBox);
	}
}

