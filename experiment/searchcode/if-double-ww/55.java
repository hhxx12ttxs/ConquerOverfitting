package beast.app.draw;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import beast.app.beauti.BeautiDoc;
import beast.core.BEASTInterface;
import beast.core.Input;

public class StrokeInputEditor extends InputEditor.Base {
	private static final long serialVersionUID = 1L;
	
	public StrokeInputEditor(BeautiDoc doc) {
		super(doc);
	}

	@Override
	public Class<?> type() {
		return BasicStroke.class;
	}

	@Override
	public void init(Input<?> input, BEASTInterface plugin, int itemNr, ExpandOption bExpandOption, boolean bAddButtons) {
        m_bAddButtons = bAddButtons;
        m_input = input;
        m_plugin = plugin;
        this.itemNr= itemNr;
        
        addInputLabel();
        
        BasicStroke stroke = (BasicStroke) m_input.get();
        
        StrokeSample current = new StrokeSample(stroke);
        if (stroke != null) {
        	current = new StrokeSample(stroke);
        } else {
        	current = new StrokeSample(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        }
        List<StrokeSample> available = new ArrayList<>();
        final float [] dash1 = {10.0f};
        final float [] dash2 = {2.0f};
        final float [] dash3 = {10.0f, 5f, 2f, 5f};
        available.add(new StrokeSample(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)));
        available.add(new StrokeSample(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL,1.0f, dash1,1.0f)));
        available.add(new StrokeSample(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL,1.0f, dash2,1.0f)));
        available.add(new StrokeSample(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL,1.0f, dash3,1.0f)));
        available.add(new StrokeSample(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL)));
        available.add(new StrokeSample(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL,1.0f, dash1,1.0f)));
        available.add(new StrokeSample(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL,1.0f, dash2,1.0f)));
        available.add(new StrokeSample(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL,1.0f, dash3,1.0f)));
        
        final StrokeChooserPanel strokeChooser = new StrokeChooserPanel(current, available.toArray(new StrokeSample[0]));
        add(strokeChooser);
        strokeChooser.getSelector().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Stroke stroke = strokeChooser.getSelectedStroke();
				m_input.setValue(stroke, m_plugin);
			}
		});
        
        add(Box.createHorizontalGlue());
        addValidationLabel();
    } // init


	/* 
	 * JCommon : a free general purpose class library for the Java(tm) platform
	 * 
	 *
	 * (C) Copyright 2000-2009, by Object Refinery Limited and Contributors.
	 *
	 * Project Info:  http://www.jfree.org/jcommon/index.html
	 *
	 * This library is free software; you can redistribute it and/or modify it
	 * under the terms of the GNU Lesser General Public License as published by
	 * the Free Software Foundation; either version 2.1 of the License, or
	 * (at your option) any later version.
	 *
	 * This library is distributed in the hope that it will be useful, but
	 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
	 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
	 * License for more details.
	 *
	 * You should have received a copy of the GNU Lesser General Public
	 * License along with this library; if not, write to the Free Software
	 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
	 * USA.
	 *
	 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
	 * in the United States and other countries.]
	 *
	 * -----------------
	 * StrokeSample.java
	 * -----------------
	 * (C) Copyright 2000-2009, by Object Refinery Limited.
	 *
	 * Original Author:  David Gilbert (for Object Refinery Limited);
	 * Contributor(s):   -;
	 *
	 * $Id: StrokeSample.java,v 1.5 2009/02/27 13:58:41 mungady Exp $
	 *
	 * Changes (from 26-Oct-2001)
	 * --------------------------
	 * 26-Oct-2001 : Changed package to com.jrefinery.ui.*;
	 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
	 * 21-Mar-2003 : Fixed null pointer exception, bug 705126 (DG);
	 *
	 */

	/**
	 * A component for choosing a stroke from a list of available strokes.
	 *
	 * @author David Gilbert
	 */
	public class StrokeChooserPanel extends JPanel {

	    /** A combo for selecting the stroke. */
	    private JComboBox selector;

	    /**
	     * Creates a panel containing a combo-box that allows the user to select
	     * one stroke from a list of available strokes.
	     *
	     * @param current  the current stroke sample.
	     * @param available  an array of 'available' stroke samples.
	     */
	    public StrokeChooserPanel(StrokeSample current, StrokeSample[] available) {
	        setLayout(new BorderLayout());
	        // we've changed the behaviour here to populate the combo box
	        // with Stroke objects directly - ideally we'd change the signature
	        // of the constructor too...maybe later.
	        DefaultComboBoxModel model = new DefaultComboBoxModel();
	        for (int i = 0; i < available.length; i++) {
	            model.addElement(available[i].getStroke());
	        }
	        this.selector = new JComboBox(model);
	        this.selector.setSelectedItem(current.getStroke());
	        this.selector.setRenderer(new StrokeSample(null));
	        add(this.selector);
	        // Changes due to focus problems!! DZ
	        this.selector.addActionListener(new ActionListener() {
	            public void actionPerformed(final ActionEvent evt) {
	                getSelector().transferFocus();
	            }
	        });
	    }


	    /**
	     * Returns the selector component.
	     *
	     * @return Returns the selector.
	     */
	    protected final JComboBox getSelector() {
	        return this.selector;
	    }

	    /**
	     * Returns the selected stroke.
	     *
	     * @return The selected stroke (possibly <code>null</code>).
	     */
	    public Stroke getSelectedStroke() {
	        return (Stroke) this.selector.getSelectedItem();
	    }
	}

	/**
	 * A panel that displays a stroke sample.
	 *
	 * @author David Gilbert
	 */
	 class StrokeSample extends JComponent implements ListCellRenderer {

	    /** The stroke being displayed (may be null). */
	    private Stroke stroke;

	    /** The preferred size of the component. */
	    private Dimension preferredSize;

	    /**
	     * Creates a StrokeSample for the specified stroke.
	     *
	     * @param stroke  the sample stroke (<code>null</code> permitted).
	     */
	    public StrokeSample(final Stroke stroke) {
	        this.stroke = stroke;
	        this.preferredSize = new Dimension(80, 18);
	        setPreferredSize(this.preferredSize);
	    }

	    /**
	     * Returns the current Stroke object being displayed.
	     *
	     * @return The stroke (possibly <code>null</code>).
	     */
	    public Stroke getStroke() {
	        return this.stroke;
	    }

	    /**
	     * Sets the stroke object being displayed and repaints the component.
	     *
	     * @param stroke  the stroke (<code>null</code> permitted).
	     */
	    public void setStroke(final Stroke stroke) {
	        this.stroke = stroke;
	        repaint();
	    }

	    /**
	     * Returns the preferred size of the component.
	     *
	     * @return the preferred size of the component.
	     */
	    public Dimension getPreferredSize() {
	        return this.preferredSize;
	    }

	    /**
	     * Draws a line using the sample stroke.
	     *
	     * @param g  the graphics device.
	     */
	    public void paintComponent(final Graphics g) {

	        final Graphics2D g2 = (Graphics2D) g;
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                RenderingHints.VALUE_ANTIALIAS_ON);
	        final Dimension size = getSize();
	        final Insets insets = getInsets();
	        final double xx = insets.left;
	        final double yy = insets.top;
	        final double ww = size.getWidth() - insets.left - insets.right;
	        final double hh = size.getHeight() - insets.top - insets.bottom;

	        // calculate point one
	        final Point2D one =  new Point2D.Double(xx + 6, yy + hh / 2);
	        // calculate point two
	        final Point2D two =  new Point2D.Double(xx + ww - 6, yy + hh / 2);
	        // draw a circle at point one
	        final Ellipse2D circle1 = new java.awt.geom.Ellipse2D.Double(one.getX() - 5,
	                one.getY() - 5, 10, 10);
	        final Ellipse2D circle2 = new Ellipse2D.Double(two.getX() - 6,
	                two.getY() - 5, 10, 10);

	        // draw a circle at point two
	        g2.draw(circle1);
	        g2.fill(circle1);
	        g2.draw(circle2);
	        g2.fill(circle2);

	        // draw a line connecting the points
	        final Line2D line = new Line2D.Double(one, two);
	        if (this.stroke != null) {
	            g2.setStroke(this.stroke);
	            g2.draw(line);
	        }

	    }

	    /**
	     * Returns a list cell renderer for the stroke, so the sample can be
	     * displayed in a list or combo.
	     *
	     * @param list  the list.
	     * @param value  the value.
	     * @param index  the index.
	     * @param isSelected  selected?
	     * @param cellHasFocus  focussed?
	     *
	     * @return the component for rendering.
	     */
	    public Component getListCellRendererComponent(JList list, Object value,
	            int index, boolean isSelected, boolean cellHasFocus) {
	        if (value instanceof Stroke) {
	            setStroke((Stroke) value);
	        }
	        else {
	            setStroke(null);
	        }
	        return this;
	    }

	}
}

