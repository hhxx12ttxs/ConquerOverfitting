package org.cytoscape.util.swing;

/*
 * #%L
 * Cytoscape Swing Utility API (swing-util-api)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2004 - 2013
 *   Memorial Sloan-Kettering Cancer Center
 *   The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/*
 * User: Vuk Pavlovic
 * Date: Nov 29, 2006
 * Time: 5:34:46 PM
 * Description: The user-triggered collapsible panel containing the component (trigger) in the titled border
 * 
 * Updated by: Gregory Hannum
 * June 7, 2010
 * Added override for setToolTipText(String text), applying the tip to the titleComponent so that the tool-tip is visible on the entire component.
 */

/**
 * A user-triggered collapsible panel containing the component (trigger) in the
 * titled border
 * @CyAPI.InModule swing-util-api
 */
public class BasicCollapsiblePanel extends JPanel {
	
	private static final long serialVersionUID = 2010434345567315524L;
	// Border
	// includes upper left component and line type
	private CollapsableTitledBorder border; 

	// no border
	private Border collapsedBorderLine = BorderFactory.createEmptyBorder(2, 2, 2, 2); 

	// because this is null, default is used,
	// etched lowered border on MAC
	private Border expandedBorderLine = null; 

	// Title displayed in the titled border
	// protected scope for unit testing
	AbstractButton titleComponent; 

	// Expand/Collapse button
	private final static int COLLAPSED = 0, EXPANDED = 1; // image States
	private ImageIcon[] iconArrow = createExpandAndCollapseIcon();
	private JButton arrow = createArrowButton();

	// Content Pane
	private JPanel panel;

	// Container State
	private boolean collapsed; // stores curent state of the collapsable panel

	/**
	 * Constructor for an option button controlled collapsible panel. This is
	 * useful when a group of options each have unique sub contents. The radio
	 * buttons should be created, grouped, and then used to construct their own
	 * collapsible panels. This way choosing a different option in the same
	 * option group will collapse all unselected options. Expanded panels draw a
	 * border around the contents and through the radio button in the fashion of
	 * a titled border.
	 * 
	 * @param component
	 *            Radio button that expands and collapses the panel based on if
	 *            it is selected or not
	 */
	public BasicCollapsiblePanel(JRadioButton component) {
		component.addItemListener(new BasicCollapsiblePanel.ExpandAndCollapseAction());
		titleComponent = component;
		collapsed = !component.isSelected();
		commonConstructor();
	}

	/**
	 * Constructor for a label/button controlled collapsible panel. Displays a
	 * clickable title that resembles a native titled border except for an arrow
	 * on the right side indicating an expandable panel. The actual border only
	 * appears when the panel is expanded.
	 * 
	 * @param text
	 *            Title of the collapsible panel in string format, used to
	 *            create a button with text and an arrow icon
	 */
	public BasicCollapsiblePanel(String text) {
		arrow.setText(text);
		titleComponent = arrow;
		collapsed = true;
		commonConstructor();
	}

	/**
	 * Sets layout, creates the content panel and adds it and the title
	 * component to the container, all constructors have this procedure in
	 * common.
	 */
	private void commonConstructor() {
		setLayout(new BorderLayout());

		panel = new JPanel();
		//panel.setLayout(new BorderLayout());
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		super.add(titleComponent, BorderLayout.CENTER);
		super.add(panel, BorderLayout.CENTER);
		setCollapsed(collapsed);

		placeTitleComponent();
	}

	/**
	 * Sets the bounds of the border title component so that it is properly
	 * positioned.
	 */
	private void placeTitleComponent() {
		Insets insets = this.getInsets();
		Rectangle containerRectangle = this.getBounds();
		Rectangle componentRectangle = border.getComponentRect(containerRectangle, insets);
		titleComponent.setBounds(componentRectangle);
	}

	/**
	 * Sets the title of of the border title component.
	 * 
	 * @param text
	 *            The string title.
	 */
	public void setTitleComponentText(String text) {
		if (titleComponent instanceof JButton) {
			titleComponent.setText(text);
		}
		placeTitleComponent();
	}

	/**
	 * This class requires that all content be placed within a designated panel,
	 * this method returns that panel.
	 * 
	 * @return panel The content panel.
	 */
	public JPanel getContentPane() {
		return panel;
	}

	/**
	 * Overridden to add any new components to the content panel, as might be expected.
	 * @param comp The component to add.
	 */
	@Override
	public Component add(Component comp) {
		return panel.add(comp);
	}

	/**
	 * Overridden to add any new components to the content panel, as might be expected.
	 * @param comp The component to add.
	 * @param index The index at which to add the component. 
	 */
	@Override
	public Component add(Component comp, int index) {
		return panel.add(comp,index);
	}

	/**
	 * Overridden to add any new components to the content panel, as might be expected.
	 * @param comp The component to add.
	 * @param constraints The constraints to adding. 
	 */
	@Override
	public void add(Component comp, Object constraints) {
		panel.add(comp,constraints);
	}

	/**
	 * Overridden to add any new components to the content panel, as might be expected.
	 * @param comp The component to add.
	 * @param constraints The constraints to adding. 
	 * @param index The index at which to add the component. 
	 */
	@Override
	public void add(Component comp, Object constraints, int index) {
		panel.add(comp,constraints,index);
	}

	/**
	 * Overridden to add any new components to the content panel, as might be expected.
	 * @param name The name of the component to add. 
	 * @param comp The component to add.
	 */
	@Override
	public Component add(String name, Component comp) {
		return panel.add(name,comp);
	}

	/**
	 * Collapses or expands the panel. This is done by adding or removing the
	 * content pane, alternating between a frame and empty border, and changing
	 * the title arrow. Also, the current state is stored in the collapsed
	 * boolean.
	 * 
	 * @param collapse
	 *            When set to true, the panel is collapsed, else it is expanded
	 */
	public void setCollapsed(boolean collapse) {
		if (collapse) {
			// collapse the panel, remove content and set border to empty border
			remove(panel);
			arrow.setIcon(iconArrow[COLLAPSED]);
			border = new CollapsableTitledBorder(collapsedBorderLine, titleComponent);
		} else {
			// expand the panel, add content and set border to titled border
			super.add(panel, BorderLayout.NORTH);
			arrow.setIcon(iconArrow[EXPANDED]);
			border = new CollapsableTitledBorder(expandedBorderLine, titleComponent);
		}
		setBorder(border);
		collapsed = collapse;
		updateUI();
	}

	/**
	 * Returns the current state of the panel, collapsed (true) or expanded
	 * (false).
	 * 
	 * @return collapsed Returns true if the panel is collapsed and false if it
	 *         is expanded
	 */
	public boolean isCollapsed() {
		return collapsed;
	}

	/**
	 * Returns an ImageIcon array with arrow images used for the different
	 * states of the panel.
	 * 
	 * @return iconArrow An ImageIcon array holding the collapse and expanded
	 *         versions of the right hand side arrow
	 */
	private ImageIcon[] createExpandAndCollapseIcon() {
		ImageIcon[] iconArrow = new ImageIcon[2];
		URL iconURL;

		iconURL = getClass().getResource("/images/arrow_collapsed.png");

		if (iconURL != null) {
			iconArrow[COLLAPSED] = new ImageIcon(iconURL);
		}
		iconURL = getClass().getResource("/images/arrow_expanded.png");

		if (iconURL != null) {
			iconArrow[EXPANDED] = new ImageIcon(iconURL);
		}
		return iconArrow;
	}

	/**
	 * Returns a button with an arrow icon and a collapse/expand action
	 * listener.
	 * 
	 * @return button Button which is used in the titled border component
	 */
	private JButton createArrowButton() {
		JButton button = new JButton("arrow", iconArrow[COLLAPSED]);
		button.setBorder(BorderFactory.createEmptyBorder(0, 1, 5, 1));
		button.setVerticalTextPosition(AbstractButton.CENTER);
		button.setHorizontalTextPosition(AbstractButton.LEFT);
		button.setMargin(new Insets(0, 0, 3, 0));

		// We want to use the same font as those in the titled border font
		Font font = BorderFactory.createTitledBorder("Sample").getTitleFont();
		Color color = BorderFactory.createTitledBorder("Sample").getTitleColor();
		button.setFont(font);
		button.setForeground(color);
		button.setFocusable(false);
		button.setContentAreaFilled(false);

		button.addActionListener(new BasicCollapsiblePanel.ExpandAndCollapseAction());

		return button;
	}

	/**
	 * Handles expanding and collapsing of extra content on the user's click of
	 * the titledBorder component.
	 */
	private final class ExpandAndCollapseAction extends AbstractAction implements ActionListener, ItemListener {
		private static final long serialVersionUID = 2010434345567315525L;

		public void actionPerformed(ActionEvent e) {
			setCollapsed(!isCollapsed());
		}

		public void itemStateChanged(ItemEvent e) {
			setCollapsed(!isCollapsed());
		}
	}

	/**
	 * Special titled border that includes a component in the title area
	 */
	private final class CollapsableTitledBorder extends TitledBorder {
		private static final long serialVersionUID = 2010434345567315526L;
		JComponent component;


		public CollapsableTitledBorder(Border border, JComponent component) {
			this(border, component, LEFT, TOP);
		}

		public CollapsableTitledBorder(Border border, JComponent component, int titleJustification, int titlePosition) {
			// TitledBorder needs border, title, justification, position, font,
			// and color
			super(border, null, titleJustification, titlePosition, null, null);
			this.component = component;
			if (border == null) {
				this.border = super.getBorder();
			}
		}

		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			Rectangle borderR = new Rectangle(x + EDGE_SPACING, y + EDGE_SPACING, width - (EDGE_SPACING * 2), height
					- (EDGE_SPACING * 2));
			Insets borderInsets;
			if (border != null) {
				borderInsets = border.getBorderInsets(c);
			} else {
				borderInsets = new Insets(0, 0, 0, 0);
			}

			Rectangle rect = new Rectangle(x, y, width, height);
			Insets insets = getBorderInsets(c);
			Rectangle compR = getComponentRect(rect, insets);
			int diff;
			switch (titlePosition) {
			case ABOVE_TOP:
				diff = compR.height + TEXT_SPACING;
				borderR.y += diff;
				borderR.height -= diff;
				break;
			case TOP:
			case DEFAULT_POSITION:
				diff = insets.top / 2 - borderInsets.top - EDGE_SPACING;
				borderR.y += diff;
				borderR.height -= diff;
				break;
			case BELOW_TOP:
			case ABOVE_BOTTOM:
				break;
			case BOTTOM:
				diff = insets.bottom / 2 - borderInsets.bottom - EDGE_SPACING;
				borderR.height -= diff;
				break;
			case BELOW_BOTTOM:
				diff = compR.height + TEXT_SPACING;
				borderR.height -= diff;
				break;
			}
			border.paintBorder(c, g, borderR.x, borderR.y, borderR.width, borderR.height);
			Color col = g.getColor();
			g.setColor(c.getBackground());
			g.fillRect(compR.x, compR.y, compR.width, compR.height);
			g.setColor(col);
		}

		public Insets getBorderInsets(Component c, Insets insets) {
			Insets borderInsets;
			if (border != null) {
				borderInsets = border.getBorderInsets(c);
			} else {
				borderInsets = new Insets(0, 0, 0, 0);
			}
			insets.top = EDGE_SPACING + TEXT_SPACING + borderInsets.top;
			insets.right = EDGE_SPACING + TEXT_SPACING + borderInsets.right;
			insets.bottom = EDGE_SPACING + TEXT_SPACING + borderInsets.bottom;
			insets.left = EDGE_SPACING + TEXT_SPACING + borderInsets.left;

			if (c == null || component == null) {
				return insets;
			}

			int compHeight = component.getPreferredSize().height;

			switch (titlePosition) {
			case ABOVE_TOP:
				insets.top += compHeight + TEXT_SPACING;
				break;
			case TOP:
			case DEFAULT_POSITION:
				insets.top += Math.max(compHeight, borderInsets.top) - borderInsets.top;
				break;
			case BELOW_TOP:
				insets.top += compHeight + TEXT_SPACING;
				break;
			case ABOVE_BOTTOM:
				insets.bottom += compHeight + TEXT_SPACING;
				break;
			case BOTTOM:
				insets.bottom += Math.max(compHeight, borderInsets.bottom) - borderInsets.bottom;
				break;
			case BELOW_BOTTOM:
				insets.bottom += compHeight + TEXT_SPACING;
				break;
			}
			return insets;
		}

		public Rectangle getComponentRect(Rectangle rect, Insets borderInsets) {
			Dimension compD = component.getPreferredSize();
			Rectangle compR = new Rectangle(0, 0, compD.width, compD.height);
			switch (titlePosition) {
			case ABOVE_TOP:
				compR.y = EDGE_SPACING;
				break;
			case TOP:
			case DEFAULT_POSITION:
				if (titleComponent instanceof JButton) {
					compR.y = EDGE_SPACING + (borderInsets.top - EDGE_SPACING - TEXT_SPACING - compD.height) / 2;
				} else if (titleComponent instanceof JRadioButton) {
					compR.y = (borderInsets.top - EDGE_SPACING - TEXT_SPACING - compD.height) / 2;
				}
				break;
			case BELOW_TOP:
				compR.y = borderInsets.top - compD.height - TEXT_SPACING;
				break;
			case ABOVE_BOTTOM:
				compR.y = rect.height - borderInsets.bottom + TEXT_SPACING;
				break;
			case BOTTOM:
				compR.y = rect.height - borderInsets.bottom + TEXT_SPACING
						+ (borderInsets.bottom - EDGE_SPACING - TEXT_SPACING - compD.height) / 2;
				break;
			case BELOW_BOTTOM:
				compR.y = rect.height - compD.height - EDGE_SPACING;
				break;
			}
			switch (titleJustification) {
			case LEFT:
			case DEFAULT_JUSTIFICATION:
				// compR.x = TEXT_INSET_H + borderInsets.left;
				compR.x = TEXT_INSET_H + borderInsets.left - EDGE_SPACING;
				break;
			case RIGHT:
				compR.x = rect.width - borderInsets.right - TEXT_INSET_H - compR.width;
				break;
			case CENTER:
				compR.x = (rect.width - compR.width) / 2;
				break;
			}
			return compR;
		}
	}

	/**
	 * Sets the tooltip text of this BasicCollapsiblePanel.
	 * 
	 * @param text
	 *            The string to set as the tooltip.
	 */
	@Override
	public void setToolTipText(final String text) {
		super.setToolTipText(text);
		titleComponent.setToolTipText(text);
	}
}

