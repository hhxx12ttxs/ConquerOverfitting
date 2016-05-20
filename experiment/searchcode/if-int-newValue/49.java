package com.skp.shaphan.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;

import javax.swing.JTextArea;

import com.skp.shaphan.SQLEditorsPane;
import com.skp.shaphan.toolbars.SqlEditorToolbar;
import com.skp.shaphan.ui.SQLEditor;

public class SQLEditorLayout implements LayoutManager2 {
	private boolean layoutComplete;
	private boolean newLayoutComplete;

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {

	}

	@Override
	public float getLayoutAlignmentX(Container target) {
		return 0;
	}

	@Override
	public float getLayoutAlignmentY(Container target) {
		return 0;
	}

	@Override
	public void invalidateLayout(Container target) {
	}

	@Override
	public Dimension maximumLayoutSize(Container target) {
		return null;
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void layoutContainer(Container parent) {
		SQLEditor sqlEditor = (SQLEditor) parent;
		
		int x;
		int y;
		int actualWidth;
		int actualHeight;
		int prefWidth;
		int prefHeight;
		
		newLayoutComplete = true;
		
		SqlEditorToolbar toolbar = null;
		JTextArea sqlStatement = null;
		
		for(Component c : sqlEditor.getComponents()) {
			if(c instanceof SqlEditorToolbar) {
				toolbar = (SqlEditorToolbar)c;
			}
			if(c instanceof JTextArea) {
				sqlStatement = (JTextArea)c;
			}
		}
		
		/* Toolbar */
		x = 0;
		y = 0;
		actualHeight = toolbar.getHeight();
		actualWidth = toolbar.getWidth();
		if(sqlEditor.getToolbarVisible()) {
			prefHeight = 27;
		} else {
			prefHeight = 0;
		}
		prefWidth = sqlEditor.getWidth();
		actualHeight = adjustDimension(actualHeight, prefHeight);
		actualWidth = adjustDimension(actualWidth, prefWidth);
		toolbar.setBounds(x, y, actualWidth, actualHeight);
		
		/* sql statement */
		x = 0;
		y = toolbar.getHeight();
		actualHeight = sqlStatement.getHeight();
		actualWidth = sqlStatement.getWidth();
		sqlEditor.resizeToFit();
		prefHeight = sqlStatement.getPreferredSize().height;
		prefWidth = sqlEditor.getWidth();
		actualHeight = adjustDimension(actualHeight, prefHeight);
		actualWidth = adjustDimension(actualWidth, prefWidth);
		sqlStatement.setBounds(x, y, actualWidth, actualHeight);

		Dimension d = new Dimension(x + actualWidth, y + actualHeight);
		if(d.equals(sqlEditor.getPreferredSize())) {
			// don't need to adjust the parent
		} else {
			//System.out.println("Changing editor size d = " + d.width + "x" + d.height + " --- y = " + y + " actualHeight = " + actualHeight);
			sqlEditor.setPreferredSize(d);
			parent = sqlEditor.getParentEditorsPane();
			if(parent != null) {
				if(parent instanceof SQLEditorsPane) {
					((SQLEditorsPane)parent).getUIView().doLayout();
				} else {
					parent.doLayout();
				}
			}
		}
		layoutComplete = newLayoutComplete;
		if(!layoutComplete) {
			sqlEditor.startLayoutAnimation();
		}
		//System.out.println("SQLStatement: Actual = " + actualWidth + ", " + actualHeight + " Preferred = " + prefWidth + ", " + prefHeight + " Complete? " + layoutComplete);
	}
	
	private int adjustDimension(int actual, int pref) {
		int newValue = 0;
		int adjustAmount = 0;
		if(actual == pref) {
			newValue = actual;
		} else {
			adjustAmount = (int) Math.ceil(((float)Math.abs(actual - pref)) * 0.25);
			if(adjustAmount <= 1) {
				newValue = pref;
			} else {
				if(actual < pref) {
					newValue = actual + adjustAmount;
				} else {
					newValue = actual - adjustAmount;
				}
			}
			newLayoutComplete = false;
		}
		//System.out.println("adjustDimension: actual = " + actual + " pref = " + pref + " adjustAmount = " + adjustAmount + " newValue = " + newValue);
		return newValue;
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return null;
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return null;
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	public boolean isLayoutComplete() {
		return layoutComplete;
	}

}

