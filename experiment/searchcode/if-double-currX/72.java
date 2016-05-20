package com.objectwave.uiWidget;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.*;
import javax.swing.JLabel;
import javax.swing.JComponent;

/**
 *    This class displays a histogram (bar-graph) of the given data in the
 *  given scale.  Note that there's an boolean autoRange data member.  If
 *  this is "true" then the scale of the graph will be relative to the 
 *  size of the data.  
 *
 *    This implementation is incomplete.  It will not display negative values
 *  in the desired manner: all values will be drawn from the bottom of the
 *  graph, not from 0.  Additionally, there should be indicators on the 
 *  side of the graph to give a scale:  at least minValue, 0, and maxValue
 *  should be displayed, if available real estate is available.  Finally,
 *  it would be useful to provide the option of displaying the graph in any
 *  of the four orientations (left->right, right->left, top->bottom, bottom->top)
 *  instead of just bottom->top.  In left<->rights orientations, the ability
 *  to write the labels associated with the different bars would be useful.
 */
public class Histogram extends JGraph //extends JComponent implements ItemSelectable
{
	public int minValue = 0;
	public int maxValue = 100;
	public boolean autoRange = true; // automagically determine the min, max values.
	public int numTicks = 10;
	public int barBase = 20;

	/**
	*/
	public Histogram()
	{
		clear();
		setLayout(null);
		addMouseListener(getMouseListener());
		addMouseMotionListener(getMouseMotionListener());
		focusedItemValue = new JLabel();
		focusedItemValue.setBounds(new Rectangle(0, 0, 50, 18));
		focusedItemValue.setOpaque(true);
		focusedItemValue.setForeground(Color.black);
		focusedItemValue.setBackground(Color.white);
		focusedItemValue.setToolTipText("Value & label of slice under the pointer");
		focusedItemValue.setText("value");
		focusedItemValue.setHorizontalAlignment(JLabel.CENTER);
		focusedItemValue.setVisible(false);
		focusedItemValue.setBorder(new LineBorder(Color.black));
		this.add(focusedItemValue);
		setRange();
	}
	/** Pass an object instead of a name so that it can be accessed later.
	* the object's name will be object.toString().
	*/
	public void addItem(Object object, int value, Color col)
	{
		graphItems.addElement(new GraphItem(object, value, col==null ? pickColor() : col));
		setRange();
		recalc = true;
		revalidate();
	}
	/**
	*/
	public void addItem(String name, int value)
	{
		addItem(name, value, pickColor());
		setRange();
		recalc = true;
		revalidate();
	}
	/**
	*/
	public void addItem(String name, int value, Color col)
	{
		graphItems.addElement(new GraphItem(name, value, col==null ? pickColor() : col));
		setRange();
		recalc = true;
		revalidate();
	}
	/**
	*/
	public void clear()
	{
		super.clear();
		setRange();
	}
	/**
	*/
	protected MouseMotionListener getMouseMotionListener()
	{
		return new MouseMotionListener()
		{
			 public void mouseDragged(MouseEvent e)
			 { }
			 public void mouseMoved(MouseEvent e)
			 { updateFocusedItemLabel(new Point(e.getX(), e.getY())); }
		};
	}
	/**
	*/
	public GraphItem itemAtPoint(Point p)
	{
		for (int i=0; i<graphItems.size(); ++i)
		{
			GraphItem item = (GraphItem)graphItems.elementAt(i);
			Rectangle bounds = item.getBounds();
			if (bounds!=null && 
				p.x >= bounds.x && p.x <= bounds.x+bounds.width &&
				p.y >= bounds.y && p.y <= bounds.y+bounds.height)
			{
				return (GraphItem)graphItems.elementAt(i);
			}
		}
		return null;
	}
	/**
	*/
	public void paint(Graphics g)
	{
		Color temp = g.getColor();
		//if (recalc)
		//    recalculateBounds();
		int width = right-left;
		int height = bottom-top;
		double scale = (1.0d*(maxValue-minValue))/height;
		if (height == 0 || width == 0)
			return; // we're dimensionless: nothing to draw.
		int spacing = width / (graphItems.size()+1);
		int currX = left + spacing;
		
		float tickSpacing = 1.0f*height / numTicks;
		float tickY = bottom;
		g.setColor(Color.gray);
		for (int i=0; i<numTicks+1; ++i)
		{
			int y = Math.round(tickY);
			g.drawLine(left+2, y, right-2, y);
			tickY -= tickSpacing;
		}
		g.setColor(Color.black);
		g.drawLine(left, bottom, left, top);
		g.drawLine(right, bottom, right, top);
		g.drawLine(left, bottom, right, bottom);
		g.drawLine(left, top, right, top);
		
		for (int i=0; i<graphItems.size(); ++i)
		{
			GraphItem item = (GraphItem)graphItems.elementAt(i);
			double itemH = (item.value - minValue)/scale;
			int itemHeight = (int)Math.round(itemH + (itemH > height ? padding:0));
			if (itemHeight == 0)
				itemHeight = 1;
			g.setColor(item.color);
//            g.fillRect(currX-barBase/2, bottom-itemHeight, barBase, itemHeight);
			g.fill3DRect(currX-barBase/2, bottom-itemHeight, barBase, itemHeight, true);
			item.bounds = new Rectangle(currX-barBase/2, bottom-itemHeight, barBase, itemHeight);
			currX += spacing;
		}
		g.setColor(temp);
		super.paint(g);
		recalc = false;
	}
	/**
	*/
	public void removeItem(String name)
	{
		super.removeItem(name);
		setRange();
	}
	/**
	*/
	public void setRange()
	{
		if (autoRange)
		{
			minValue = 0;
			maxValue = 0;
			for (Enumeration e = graphItems.elements(); e.hasMoreElements();)
			{
				int value = ((GraphItem)e.nextElement()).value;
				if (value < minValue)
					minValue = value;
				if (value > maxValue)
					maxValue = value;
			}
			// add 5% to max, remove 5% from min
			maxValue = (int)Math.round(1.05*(maxValue-minValue));
			if (minValue < 0)
				minValue = (int)Math.round(0.95*(maxValue-minValue)); 
		}
	}
	/**
	*/
	protected void updateFocusedItemLabel(Point p)
	{
		GraphItem item = itemAtPoint(p);
		double value = 0.0;
		if (item == null)
			value = valueAtPoint(p);
		Color bg = item==null ? Color.white : item.color;
		String text = "";
		if (item == null)
		{
			if (value == Double.POSITIVE_INFINITY || value == Double.NEGATIVE_INFINITY)
			{
				focusedItemValue.setVisible(false);
				return;
			}
			text = "Value - " + Math.round(value);
		}
		else
			text = "" + item.title + " - " + item.value;
		updateItemLabel(p, bg, text);
	}
	/**
	 * Return the corresponding value in the range [minValue...maxValue] at
	 * the given point p.  If the value determined is not in the range of
	 * legal values, either positive or negative infinity will be returned,
	 * depending on which side of the graph the value was determined.
	*/
	public double valueAtPoint(Point p)
	{
		double scale = (1.0d*(maxValue-minValue))/(bottom-top);
		double val = (maxValue-minValue) - (p.y-top) * scale;
		if (val > maxValue)
			return Double.POSITIVE_INFINITY;
		if (val < minValue)
			return Double.NEGATIVE_INFINITY;
		return val;
	}
}

