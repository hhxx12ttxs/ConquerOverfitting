package com.objectwave.viewUtility;

import com.objectwave.uiWidget.*;
import com.objectwave.utility.*;
import javax.swing.*;
import java.awt.*;

/**
 * This is a new layout manager which targets the problem of laying out
 * columns of components.  This is currently accomplished by using a
 * GridBagLayout, which is cumbersome to use if the components are to be
 * laid out in a similar fashion on a per-column basis.  The FlexGridLayout
 * manager uses an array of column constraints (FlexGridColumn) to determine
 * both how many columns there are and how each component is to presented in
 * each of them.  See the main() method of FlexGridLayout for a simple example.
 */
public class FlexGridLayout implements java.awt.LayoutManager
{
	private int xgap = 0;
	private int ygap = 0;
	private FlexGridColumn[] columns = new FlexGridColumn[0];
	private Component components[] = null;
	private Dimension minimumLayoutSize = new java.awt.Dimension(0, 0);
	private Dimension preferredLayoutSize = new java.awt.Dimension(0, 0);

	private DebugOutput debug = new DebugOutput(10, "FlexGrid: ");
	/**
	 * Empty constructor for FlexGridLayout.  No columns are defined.
	 */
	public FlexGridLayout() {
		super();
	}
	/**
	 * Constructor for FlexGridLayout which defines the column constraints.
	 * @param columns com.objectwave.viewUtility.FlexGridColumn[]
	 */
	public FlexGridLayout(FlexGridColumn[] columns)
	{
		super();
		this.columns = columns;
	}
	/**
	 * This constructor will initialize numColumns column constraints, using
	 * FlexGridColumn's default values.
	 * @param numColumns int
	 */
	public FlexGridLayout(int numColumns)
	{
		setNumberOfColumns(numColumns);
	}
	/**
	 * Add a column constraint to the layout manager.  Please note that this
	 * method is not all that efficient.  A better approach would be to build the
	 * array of FlexGridColumns  and then call addColumns().
	 * @param column com.objectwave.viewUtility.FlexGridColumn
	 */
	public void addColumn(FlexGridColumn column)
	{
		debug.println(9, "addColumn");
		int len = (columns == null) ? 0 : columns.length;
		FlexGridColumn[] newColumns = new FlexGridColumn[len+1];
		for (int i=0; i < len; ++i)
			newColumns[i] = columns[i];
		newColumns[len] = column.duplicate();
		columns = newColumns;
	}
	/**
	 * Add a componenth to the layout.  This method is part of the LayoutManager
	 * interface, but is never called by the framework.
	 */
	public void addLayoutComponent(String name, java.awt.Component comp)
	{
		debug.println(7, "addLayoutComponent");
		int len = getComponents()==null ? 0 : (getComponents().length);
		Component[] newArray = new Component[len+1];
		for (int i=0; i < len; ++i)
			newArray[i] = getComponents()[i];
		newArray[len] = comp;
		setComponents(newArray);
	}
	/**
	 * Determine the bounds of "component" within a given cell (x,y,width,height) using it's
	 * current bounds and "columnLayout".
	 *
	 * @param component java.awt.Component
	 * @param columnLayout com.objectwave.viewUtility.FlexGridColumn
	 * @param x int
	 * @param y int
	 * @param width int
	 * @param height int
	 */
	protected java.awt.Rectangle adjustPosition(java.awt.Component component, FlexGridColumn columnLayout,
		                          				int x, int y, int width, int height)
	{
	  try
	  {
		debug.println(9, "adjustPosition, given cell dim(" + x + "," + y + "," + width + "," + height + ")");
		java.awt.Rectangle newBounds = new java.awt.Rectangle(-1, -1, -1, -1);
		switch (columnLayout.fill)
		{
			case FlexGridColumn.HORIZONTAL:	newBounds.x = x; newBounds.width  = width;  break;
			case FlexGridColumn.VERTICAL:   newBounds.y = y; newBounds.height = height; break;
			case FlexGridColumn.NONE: break;
			case FlexGridColumn.BOTH:
				newBounds.x = x; newBounds.width  = width;
				newBounds.y = y; newBounds.height = height;
				return newBounds; // anchors are meaningless now.
			default: throw new IllegalArgumentException("FlexGridColumn.fill has invalid value " + columnLayout.fill);
		}

		java.awt.Dimension size = component.getPreferredSize();
		if (columnLayout.anchor == FlexGridColumn.CENTER)
		{
			if (newBounds.x < 0)
			{
				int hSpace = Math.max(0, width-size.width);
				newBounds.x = x + hSpace/2;
				newBounds.width = width-hSpace;
			}
			if (newBounds.y < 0)
			{
				int vSpace = Math.max(0, height-size.height);
				newBounds.y = y + vSpace/2;
				newBounds.height = height-vSpace;
			}
			return newBounds; // no need to check the other six bounds.
		}
		if (newBounds.x < 0)
		{
			newBounds.width = Math.min(width, size.width);
			if (columnLayout.anchor == FlexGridColumn.EAST ||
			    columnLayout.anchor == FlexGridColumn.NORTHEAST ||
			    columnLayout.anchor == FlexGridColumn.SOUTHEAST)
			{
				newBounds.x = x + width - newBounds.width;
			}
			else if (columnLayout.anchor == FlexGridColumn.WEST ||
					 columnLayout.anchor == FlexGridColumn.NORTHWEST ||
					 columnLayout.anchor == FlexGridColumn.SOUTHWEST)
			{
				newBounds.x = x;
			}
			else if (columnLayout.anchor == FlexGridColumn.NORTH ||
					 columnLayout.anchor == FlexGridColumn.SOUTH)
			{
				int space = Math.max(0, width-size.width);
				newBounds.x = x + space/2;
				newBounds.width = width-space;
			}
			else
				throw new IllegalArgumentException("FlexGridColumn.anchor has invalid value " + columnLayout.anchor);
		}
		if (newBounds.y < 0)
		{
			newBounds.height = Math.min(height, size.height);
			if (columnLayout.anchor == FlexGridColumn.SOUTH ||
				columnLayout.anchor == FlexGridColumn.SOUTHEAST ||
				columnLayout.anchor == FlexGridColumn.SOUTHWEST)
			{
				newBounds.y = y + height - newBounds.height;
			}
			else if (columnLayout.anchor == FlexGridColumn.NORTH ||
					 columnLayout.anchor == FlexGridColumn.NORTHEAST ||
					 columnLayout.anchor == FlexGridColumn.NORTHWEST)
			{
				newBounds.y = y;
			}
			else if (columnLayout.anchor == FlexGridColumn.EAST ||
					 columnLayout.anchor == FlexGridColumn.WEST)
			{
				int space = Math.max(0, height-size.height);
				newBounds.y = y + space/2;
				newBounds.height = height-space;
			}
			else
				throw new IllegalArgumentException("FlexGridColumn.anchor has invalid value " + columnLayout.anchor);
		}
		return newBounds;
	  } catch (Exception e) { debug.println(0, "EXCEPTION: " + e); return null; }
	}
	/**
	 * Access the index'th column's constraints.  index is from 0 ... #columns
	 * @return com.objectwave.viewUtility.FlexGridColumn
	 * @param index int
	 */
	public FlexGridColumn getColumn(int index)
	{
		debug.println(9, "getColumn");
		return (columns==null || columns.length <= index) ? null : columns[index];
	}
	/**
	 * Return a reference to the column constraints.
	 * @return FlexGridColumn[]
	 */
	public FlexGridColumn[] getColumns()
	{
		debug.println(7, "getColumns");
		if (columns == null)
			columns = new FlexGridColumn[0];
		return columns;
	}
	/**
	 * Get the components.
	 * @return java.awt.Component[]
	 */
	protected Component[] getComponents()
	{
		debug.println(9, "getComponents");
		return components;
	}
	/**
	 * Get the debuggin out put object.
	 * @return com.objectwave.utility.DebugOutput
	 */
	public com.objectwave.utility.DebugOutput getDebug() {
		return debug;
	}
	/**
	 * Get the inter-column spacing value.
	 * @return int
	 */
	public int getXgap() {
		return xgap;
	}
	/**
	 * Get the vertical spacing between components.
	 * @return int
	 */
	public int getYgap() {
		return ygap;
	}
	/**
	 * Layout the given parent's components according to the array of column
	 * constraints.
	 */
	public void layoutContainer(java.awt.Container parent)
	{
		debug.println(7, "layoutContainer");

		FlexGridColumn columns[] = getColumns();
		if (columns.length == 0)
			return; // cannot layout components without column config information.
		java.awt.Dimension dim = parent.getSize();
		setComponents(parent.getComponents());
		Component[] components = getComponents();

		if (debug.getLevel() >= 5)
		{
			debug.println(5, "There are " + components.length + " components to lay out.");
			debug.println(5, "There are " + columns.length + " columns defined.");
			debug.println(6, "FlexGrid columns: ");
			for (int i=0; i < columns.length; ++i)
				debug.println(6, "\tcolumn[" + i + "]: " + columns[i]);
		}


		// Calculate the column widths.
		//
		int columnWidths[] = new int[columns.length];
		int extraWidth = dim.width;
		double totalWeight = 0.0;
		for (int i=0; i < columns.length; ++i)
		{
			debug.println(8, "Examine column #" + i + " for width & weight.");
			if (columns[i].width < 0)
			{
				int width = 0;
				for (int j = i; j < components.length; j += columns.length)
					width = Math.max(width, components[j].getPreferredSize().width);
				extraWidth -= width;
				columnWidths[i] = width;
			}
			else
			{
				columnWidths[i] = columns[i].width;
				extraWidth -= columns[i].width;
			}
			extraWidth -= getXgap();
			totalWeight += columns[i].weight;
		}

		debug.println(5, "totalWeight = " + totalWeight);
		debug.println(5, "extraWidth  = " + extraWidth + ", container.width = " + dim.width);

		// Distribute extraWidth according to weights.
		//
		for (int i=0; i < columns.length; ++i)
		{
			columnWidths[i] += (int)Math.floor(Math.max(0, columns[i].weight + (extraWidth*columns[i].weight/totalWeight)));
		}

		// Collect the row heights.
		// [ Remember, the array elements are initialized to 0 by default. ]
		int numRows = (int)Math.ceil(components.length / (double)columns.length);
		debug.println(6, "Number of rows: " + numRows);
		int rowHeights[] = new int[numRows];
		for (int i=0; i < components.length; ++i)
		{
			int h = components[i].getPreferredSize().height;
			rowHeights[i / columns.length] = Math.max(h, rowHeights[i / columns.length]);
		}

		if (debug.getLevel() >= 5)
		{
			debug.println(5, "Column widths:");
			for (int i=0; i < columnWidths.length; ++i)
				debug.println(5, "\t" + columnWidths[i]);
			debug.println(5, "Row heights:");
			for (int i=0; i < rowHeights.length; ++i)
				debug.println(5, "\t" + rowHeights[i]);
		}

		// Calculate the component positions.
		//
		int col = 0;
		int currX = 0; // assuming 0 == leftmost position
		int currY = 0; // assuming 0 == topmost position
		for (int i = 0; i < components.length; ++i)
		{
			// Position the component, given the component, column layout, and cell bounds.
			//
			java.awt.Rectangle rect = adjustPosition(components[i], columns[col], currX, currY,
						  					   		 columnWidths[col], rowHeights[i / columns.length]);
			components[i].setBounds(rect);
			debug.println(6, "Adjusted rectangle = " + rect);

			// Increment col, possibly moving to the next row.
			//
			currX += columnWidths[col];
			col = (col+1) % getColumns().length;
			if (col == 0)	// adjust local variables to account for being at the next row.
			{
				currX = 0;
				currY += rowHeights[i / columns.length] + getYgap();
			}
			currX += getXgap();
		}

	}
	/**
	 * Test method.
	 * @param args java.lang.String[]
	 */
	public static void main(String args[])
	{
		JPanel panel = new JPanel();
		FlexGridLayout layout = new FlexGridLayout();
		panel.setLayout(layout);

		// Set up the layout like we want it
		//
		layout.debug.setLevel(0);
		layout.setNumberOfColumns(3);
	//	layout.getColumn(1).weight = 1.0;
	//	layout.getColumn(1).fill = FlexGridColumn.BOTH;
		layout.getColumn(1).anchor = FlexGridColumn.EAST;

		// Add components to the panel (and hence, presumably, the layout)
		//
		for (int i=1; i<=10; ++i)
			panel.add(new JButton("button #" + i));
		panel.add(new JButton("wider than the rest"));

		// Build a dialog to contain the panel and display it.
		//
        JFrame af = null;
		JDialog dialog = new JDialog(af, "Test FlexGrid", true);
		dialog.setBounds(100, 100, 300, 400);
		dialog.getContentPane().setLayout(new java.awt.BorderLayout());
		dialog.getContentPane().add("Center", panel);
		dialog.setVisible(true);
		System.exit(0);
	}
	/**
	 * get the minimumLayoutSize
	 */
	public java.awt.Dimension minimumLayoutSize(java.awt.Container parent)
	{
		debug.println(7, "minimumLayoutSize");
		return minimumLayoutSize;
	}
	/**
	 * get the preferredLayoutSize
	 */
	public java.awt.Dimension preferredLayoutSize(java.awt.Container parent)
	{
		debug.println(7, "preferredLayoutSize");
		return preferredLayoutSize;
	}
	/**
	 * Remove a layout component.  This method is required by the LayoutManager
	 * interface, but is never called by the framework.
	 */
	public void removeLayoutComponent(java.awt.Component comp)
	{
		debug.println(7, "removeLayoutComponent");
		if (getComponents() == null || getComponents().length == 0)
			return;
		Component[] newArray = new Component[getComponents().length-1];
		int adjust = 0;
		for (int i=0; i < getComponents().length; ++i)
		{
			if (adjust == 0 && getComponents()[i] == comp)
				adjust = 1;
			else
				newArray[i-adjust] = getComponents()[i];
		}
		setComponents(newArray);
	}
	/**
	 * Replace the index'th column with "column".
	 * @param column com.objectwave.viewUtility.FlexGridColumn
	 * @param index int
	 */
	public void setColumnAt(FlexGridColumn column, int index)
	{
		debug.println(7, "setColumnAt(" + column + ", " + index + ")");
		if (columns == null || columns.length <= index || index < 0)
			throw new ArrayIndexOutOfBoundsException();
		columns[index] = column;
	}
	/**
	 * Set the columns array.
	 * @param newValue FlexGridColumn[]
	 */
	protected void setColumns(FlexGridColumn[] newValue) {
		this.columns = newValue;
	}
	/**
	 * Set the components array.
	 * @param array java.awt.Component[]
	 */
	public void setComponents(java.awt.Component[] array)
	{
		this.components = array;
	}
	/**
	 * Set the debugging object.
	 * @param newValue com.objectwave.utility.DebugOutput
	 */
	protected void setDebug(com.objectwave.utility.DebugOutput newValue) {
		this.debug = newValue;
	}
	/**
	 * Set the minimum layout size.
	 * @param newValue java.awt.Dimension
	 */
	public void setMinimumLayoutSize(java.awt.Dimension newValue)
	{
		debug.println(7, "setMinimumLayoutSize");
		this.minimumLayoutSize = newValue;
	}
	/**
	 * Set the number of columns, creating default-value columns if "num" is
	 * greater that the old number of columns.
	 * @param num int
	 */
	public void setNumberOfColumns(int num)
	{
		debug.println(7, "setNumberOfColumns");
		FlexGridColumn newColumns[] = new FlexGridColumn[num];
		int numToCopy = Math.min(getColumns().length, num);
		for (int i = 0; i < numToCopy; ++i)
			newColumns[i] = getColumns()[i];
		for (int i = numToCopy; i < num; ++i)
			newColumns[i] = new FlexGridColumn();
		setColumns(newColumns);
	}
	/**
	 * Set the prederred layout size
	 * @param newValue java.awt.Dimension
	 */
	public void setPreferredLayoutSize(java.awt.Dimension newValue)
	{
		debug.println(7, "setPreferredLayoutSize");
		this.preferredLayoutSize = newValue;
	}
	/**
	 * Set the intercolumn spacing value.
	 * @param newValue int
	 */
	public void setXgap(int newValue) {
		this.xgap = newValue;
	}
	/**
	 * Set the vertical spacing between components.
	 * @param newValue int
	 */
	public void setYgap(int newValue) {
		this.ygap = newValue;
	}
}
