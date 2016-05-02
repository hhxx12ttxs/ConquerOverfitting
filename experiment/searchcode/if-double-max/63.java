<<<<<<< HEAD
/*
 * This file is part of the prefuse visualization
 * toolkit which you can find at: http://prefuse.org/
 *
 * Modified by Markus Echterhoff <evopaint@markusechterhoff.com>
 * Modified lines marked with "// MODIFIED"
 * Any modifications are licensed as the rest of EvoPaint under GPLv3+
 *
 * all of prefuse was released under the following license:
 */

/*
  Copyright (c) 2004-2007 Regents of the University of California.
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions
  are met:

  1. Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.

  3.  Neither the name of the University nor the names of its contributors
  may be used to endorse or promote products derived from this software
  without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  SUCH DAMAGE.
 */

package evopaint.gui.rulesetmanager.util;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * <p>Implements a Swing-based Range slider, which allows the user to enter a
 * range (minimum and maximum) value.</p>
 *
 * @author Ben Bederson
 * @author Jesse Grosjean
 * @author Jon Meyer
 * @author Lance Good
 * @author jeffrey heer
 * @author Colin Combe
 */
public class JRangeSlider extends JComponent
    implements MouseListener, MouseMotionListener, KeyListener
{
    /*
     * NOTE: This is a modified version of the original class distributed by
     * Ben Bederson, Jesse Grosjean, and Jon Meyer as part of an HCIL Tech
     * Report.  It is modified to allow both vertical and horitonal modes.
     * It also fixes a bug with offset on the buttons. Also fixed a bug with
     * rendering using (x,y) instead of (0,0) as origin.  Also modified to
     * render arrows as a series of lines rather than as a GeneralPath.
     * Also modified to fix rounding errors on toLocal and toScreen.
     *
     * With inclusion in prefuse, this class has been further modified to use a
     * bounded range model, support keyboard commands and more extensize
     * parameterization of rendering/appearance options. Furthermore, a stub
     * method has been introduced to allow subclasses to perform custom
     * rendering within the slider through.
     */

    final public static int VERTICAL = 0;
    final public static int HORIZONTAL = 1;
    final public static int LEFTRIGHT_TOPBOTTOM = 0;
    final public static int RIGHTLEFT_BOTTOMTOP = 1;

    final public static int PREFERRED_BREADTH = 16;
    final public static int PREFERRED_LENGTH = 100;
    final protected static int ARROW_SZ = 16;
    final protected static int ARROW_WIDTH = 8;
    final protected static int ARROW_HEIGHT = 4;

    protected BoundedRangeModel model;
    protected int orientation;
    protected int direction;
    protected boolean empty;
    protected int increment = 1;
    protected int minExtent = 0; // min extent, in pixels

    protected ArrayList listeners = new ArrayList();
    protected ChangeEvent changeEvent = null;
    protected ChangeListener lstnr;

    protected Color thumbColor = new Color(150,180,220);

    // ------------------------------------------------------------------------

    /**
     * Create a new range slider.
     *
     * @param minimum - the minimum value of the range.
     * @param maximum - the maximum value of the range.
     * @param lowValue - the current low value shown by the range slider's bar.
     * @param highValue - the current high value shown by the range slider's bar.
     * @param orientation - construct a horizontal or vertical slider?
     */
    public JRangeSlider(int minimum, int maximum, int lowValue, int highValue, int orientation) {
        this(new DefaultBoundedRangeModel(lowValue, highValue - lowValue, minimum, maximum),
                orientation,LEFTRIGHT_TOPBOTTOM);
    }

    /**
     * Create a new range slider.
     *
     * @param minimum - the minimum value of the range.
     * @param maximum - the maximum value of the range.
     * @param lowValue - the current low value shown by the range slider's bar.
     * @param highValue - the current high value shown by the range slider's bar.
     * @param orientation - construct a horizontal or vertical slider?
     * @param direction - Is the slider left-to-right/top-to-bottom or right-to-left/bottom-to-top
     */
    public JRangeSlider(int minimum, int maximum, int lowValue, int highValue, int orientation, int direction) {
        this(new DefaultBoundedRangeModel(lowValue, highValue - lowValue, minimum, maximum),
                orientation, direction);
    }

    /**
     * Create a new range slider.
     *
     * @param model - a BoundedRangeModel specifying the slider's range
     * @param orientation - construct a horizontal or vertical slider?
     * @param direction - Is the slider left-to-right/top-to-bottom or right-to-left/bottom-to-top
     */
    public JRangeSlider(BoundedRangeModel model, int orientation, int direction) {
        super.setFocusable(true);
        this.model = model;
        this.orientation = orientation;
        this.direction = direction;

        setForeground(Color.LIGHT_GRAY);

        this.lstnr = createListener();
        model.addChangeListener(lstnr);

        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
    }

    /**
     * Create a listener to relay change events from the bounded range model.
     * @return a ChangeListener to relay events from the range model
     */
    protected ChangeListener createListener() {
        return new RangeSliderChangeListener();
    }

    /**
     * Listener that fires a change event when it receives  change event from
     * the slider list model.
     */
    protected class RangeSliderChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the current "low" value shown by the range slider's bar. The low
     * value meets the constraint minimum <= lowValue <= highValue <= maximum.
     */
    public int getLowValue() {
        return model.getValue();
    }

    /**
     * Sets the low value shown by this range slider. This causes the range slider to be
     * repainted and a ChangeEvent to be fired.
     * @param lowValue the low value to use
     */
    public void setLowValue(int lowValue) {
        int e = (model.getValue()-lowValue)+model.getExtent();
        model.setRangeProperties(lowValue, e,
            model.getMinimum(), model.getMaximum(), false);
        model.setValue(lowValue);
    }

    /**
     * Returns the current "high" value shown by the range slider's bar. The high
     * value meets the constraint minimum <= lowValue <= highValue <= maximum.
     */
    public int getHighValue() {
        return model.getValue()+model.getExtent();
    }

    /**
     * Sets the high value shown by this range slider. This causes the range slider to be
     * repainted and a ChangeEvent to be fired.
     * @param highValue the high value to use
     */
    public void setHighValue(int highValue) {
        model.setExtent(highValue-model.getValue());
    }

    /**
     * Set the slider range span.
     * @param lowValue the low value of the slider range
     * @param highValue the high value of the slider range
     */
    public void setRange(int lowValue, int highValue) {
        model.setRangeProperties(lowValue, highValue-lowValue,
                model.getMinimum(), model.getMaximum(), false);
    }

    /**
     * Gets the minimum possible value for either the low value or the high value.
     * @return the minimum possible range value
     */
    public int getMinimum() {
        return model.getMinimum();
    }

    /**
     * Sets the minimum possible value for either the low value or the high value.
     * @param minimum the minimum possible range value
     */
    public void setMinimum(int minimum) {
        model.setMinimum(minimum);
    }

    /**
     * Gets the maximum possible value for either the low value or the high value.
     * @return the maximum possible range value
     */
    public int getMaximum() {
        return model.getMaximum();
    }

    /**
     * Sets the maximum possible value for either the low value or the high value.
     * @param maximum the maximum possible range value
     */
    public void setMaximum(int maximum) {
        model.setMaximum(maximum);
    }

    /**
     * Sets the minimum extent (difference between low and high values).
     * This method <strong>does not</strong> change the current state of the
     * model, but can affect all subsequent interaction.
     * @param minExtent the minimum extent allowed in subsequent interaction
     */
    public void setMinExtent(int minExtent) {
        this.minExtent = minExtent;
    }

    /**
     * Sets whether this slider is empty.
     * @param empty true if set to empty, false otherwise
     */
    public void setEmpty(boolean empty) {
        this.empty = empty;
        repaint();
    }

    /**
     * Get the slider thumb color. This is the part of the slider between
     * the range resize buttons.
     * @return the slider thumb color
     */
    public Color getThumbColor() {
        return thumbColor;
    }

    /**
     * Set the slider thumb color. This is the part of the slider between
     * the range resize buttons.
     * @param thumbColor the slider thumb color
     */
    public void setThumbColor(Color thumbColor) {
        this.thumbColor = thumbColor;
    }

    /**
     * Get the BoundedRangeModel backing this slider.
     * @return the slider's range model
     */
    public BoundedRangeModel getModel() {
        return model;
    }

    /**
     * Set the BoundedRangeModel backing this slider.
     * @param brm the slider range model to use
     */
    public void setModel(BoundedRangeModel brm) {
        model.removeChangeListener(lstnr);
        model = brm;
        model.addChangeListener(lstnr);
        repaint();
    }

    /**
     * Registers a listener for ChangeEvents.
     * @param cl the ChangeListener to add
     */
    public void addChangeListener(ChangeListener cl) {
        if ( !listeners.contains(cl) )
            listeners.add(cl);
    }

    /**
     * Removes a listener for ChangeEvents.
     * @param cl the ChangeListener to remove
     */
    public void removeChangeListener(ChangeListener cl) {
        listeners.remove(cl);
    }

    /**
     * Fire a change event to all listeners.
     */
    protected void fireChangeEvent() {
        repaint();
        if ( changeEvent == null )
            changeEvent = new ChangeEvent(this);
        Iterator iter = listeners.iterator();
        while ( iter.hasNext() )
            ((ChangeListener)iter.next()).stateChanged(changeEvent);
    }

    /**
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
        if (orientation == VERTICAL) {
            return new Dimension(PREFERRED_BREADTH, PREFERRED_LENGTH);
        }
        else {
            return new Dimension(PREFERRED_LENGTH, PREFERRED_BREADTH);
        }
    }

    // ------------------------------------------------------------------------
    // Rendering

    /**
     * Override this method to perform custom painting of the slider trough.
     * @param g a Graphics2D context for rendering
     * @param width the width of the slider trough
     * @param height the height of the slider trough
     */
    protected void customPaint(Graphics2D g, int width, int height) {
        // does nothing in this class
        // subclasses can override to perform custom painting
    }

    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paintComponent(Graphics g) {
        Rectangle bounds = getBounds();
        int width = (int)bounds.getWidth() - 1;
        int height = (int)bounds.getHeight() - 1;

        int min = toScreen(getLowValue());
        int max = toScreen(getHighValue());

        // Paint the full slider if the slider is marked as empty
        if (empty) {
            if (direction == LEFTRIGHT_TOPBOTTOM) {
                min = ARROW_SZ;
                max = (orientation == VERTICAL) ? height-ARROW_SZ : width-ARROW_SZ;
            }
            else {
                min = (orientation == VERTICAL) ? height-ARROW_SZ : width-ARROW_SZ;
                max = ARROW_SZ;
            }
            ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f)); // MODIFIED
        }

        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(getBackground());
        g2.fillRect(0, 0, width, height);
        g2.setColor(getForeground());
        g2.drawRect(0, 0, width, height);

        customPaint(g2, width, height);

        // Draw arrow and thumb backgrounds
        g2.setStroke(new BasicStroke(1));
        if (orientation == VERTICAL) {
            if (direction == LEFTRIGHT_TOPBOTTOM) {
                g2.setColor(getForeground());
                g2.fillRect(0, min - ARROW_SZ, width, ARROW_SZ-1);
                paint3DRectLighting(g2,0,min-ARROW_SZ,width,ARROW_SZ-1);

                if ( thumbColor != null ) {
                    g2.setColor(thumbColor);
                    g2.fillRect(0, min, width, max - min-1);
                    paint3DRectLighting(g2,0,min,width,max-min-1);
                }

                g2.setColor(getForeground());
                g2.fillRect(0, max, width, ARROW_SZ-1);
                paint3DRectLighting(g2,0,max,width,ARROW_SZ-1);

                // Draw arrows
                g2.setColor(Color.black);
                paintArrow(g2, (width-ARROW_WIDTH) / 2.0, min - ARROW_SZ + (ARROW_SZ-ARROW_HEIGHT) / 2.0, ARROW_WIDTH, ARROW_HEIGHT, true);
                paintArrow(g2, (width-ARROW_WIDTH) / 2.0, max + (ARROW_SZ-ARROW_HEIGHT) / 2.0, ARROW_WIDTH, ARROW_HEIGHT, false);
            }
            else {
                g2.setColor(getForeground());
                g2.fillRect(0, min, width, ARROW_SZ-1);
                paint3DRectLighting(g2,0,min,width,ARROW_SZ-1);

                if ( thumbColor != null ) {
                    g2.setColor(thumbColor);
                    g2.fillRect(0, max, width, min-max-1);
                    paint3DRectLighting(g2,0,max,width,min-max-1);
                }

                g2.setColor(getForeground());
                g2.fillRect(0, max-ARROW_SZ, width, ARROW_SZ-1);
                paint3DRectLighting(g2,0,max-ARROW_SZ,width,ARROW_SZ-1);

                // Draw arrows
                g2.setColor(Color.black);
                paintArrow(g2, (width-ARROW_WIDTH) / 2.0, min + (ARROW_SZ-ARROW_HEIGHT) / 2.0, ARROW_WIDTH, ARROW_HEIGHT, false);
                paintArrow(g2, (width-ARROW_WIDTH) / 2.0, max - ARROW_SZ + (ARROW_SZ-ARROW_HEIGHT) / 2.0, ARROW_WIDTH, ARROW_HEIGHT, true);
            }
        }
        else {
            if (direction == LEFTRIGHT_TOPBOTTOM) {
                g2.setColor(getForeground());
                g2.fillRect(min - ARROW_SZ, 0, ARROW_SZ-1, height);
                paint3DRectLighting(g2,min-ARROW_SZ,0,ARROW_SZ-1,height);

                if ( thumbColor != null ) {
                    g2.setColor(thumbColor);
                    g2.fillRect(min, 0, max - min - 1, height);
                    paint3DRectLighting(g2,min,0,max-min-1,height);
                }

                g2.setColor(getForeground());
                g2.fillRect(max, 0, ARROW_SZ-1, height);
                paint3DRectLighting(g2,max,0,ARROW_SZ-1,height);

                // Draw arrows
                g2.setColor(Color.black);
                paintArrow(g2, min - ARROW_SZ + (ARROW_SZ-ARROW_HEIGHT) / 2.0, (height-ARROW_WIDTH) / 2.0, ARROW_HEIGHT, ARROW_WIDTH, true);
                paintArrow(g2, max + (ARROW_SZ-ARROW_HEIGHT) / 2.0, (height-ARROW_WIDTH) / 2.0, ARROW_HEIGHT, ARROW_WIDTH, false);
            }
            else {
                g2.setColor(getForeground());
                g2.fillRect(min, 0, ARROW_SZ - 1, height);
                paint3DRectLighting(g2,min,0,ARROW_SZ-1,height);

                if ( thumbColor != null ) {
                    g2.setColor(thumbColor);
                    g2.fillRect(max, 0, min - max - 1, height);
                    paint3DRectLighting(g2,max,0,min-max-1,height);
                }

                g2.setColor(getForeground());
                g2.fillRect(max-ARROW_SZ, 0, ARROW_SZ-1, height);
                paint3DRectLighting(g2,max-ARROW_SZ,0,ARROW_SZ-1,height);

                // Draw arrows
                g2.setColor(Color.black);
                paintArrow(g2, min + (ARROW_SZ-ARROW_HEIGHT) / 2.0, (height-ARROW_WIDTH) / 2.0, ARROW_HEIGHT, ARROW_WIDTH, true);
                paintArrow(g2, max - ARROW_SZ + (ARROW_SZ-ARROW_HEIGHT) / 2.0, (height-ARROW_WIDTH) / 2.0, ARROW_HEIGHT, ARROW_WIDTH, false);
            }
        }
    }

    /**
     * This draws an arrow as a series of lines within the specified box.
     * The last boolean specifies whether the point should be at the
     * right/bottom or left/top.
     */
    protected void paintArrow(Graphics2D g2, double x, double y, int w, int h,
                              boolean topDown)
    {
        int intX = (int)(x+0.5);
        int intY = (int)(y+0.5);

        if (orientation == VERTICAL) {
            if (w % 2 == 0) {
                w = w - 1;
            }

            if (topDown) {
                for(int i=0; i<(w/2+1); i++) {
                    g2.drawLine(intX+i,intY+i,intX+w-i-1,intY+i);
                }
            }
            else {
                for(int i=0; i<(w/2+1); i++) {
                    g2.drawLine(intX+w/2-i,intY+i,intX+w-w/2+i-1,intY+i);
                }
            }
        }
        else {
            if (h % 2 == 0) {
                h = h - 1;
            }

            if (topDown) {
                for(int i=0; i<(h/2+1); i++) {
                    g2.drawLine(intX+i,intY+i,intX+i,intY+h-i-1);
                }
            }
            else {
                for(int i=0; i<(h/2+1); i++) {
                    g2.drawLine(intX+i,intY+h/2-i,intX+i,intY+h-h/2+i-1);
                }
            }
        }
    }

    /**
     * Adds Windows2K type 3D lighting effects
     */
    protected void paint3DRectLighting(Graphics2D g2, int x, int y,
                                       int width, int height)
    {
        g2.setColor(Color.white);
        g2.drawLine(x+1,y+1,x+1,y+height-1);
        g2.drawLine(x+1,y+1,x+width-1,y+1);
        g2.setColor(Color.gray);
        g2.drawLine(x+1,y+height-1,x+width-1,y+height-1);
        g2.drawLine(x+width-1,y+1,x+width-1,y+height-1);
        g2.setColor(Color.darkGray);
        g2.drawLine(x,y+height,x+width,y+height);
        g2.drawLine(x+width,y,x+width,y+height);
    }

    /**
     * Converts from screen coordinates to a range value.
     */
    protected int toLocal(int xOrY) {
        Dimension sz = getSize();
        int min = getMinimum();
        double scale;
        if (orientation == VERTICAL) {
            scale = (sz.height - (2 * ARROW_SZ)) / (double) (getMaximum() - min);
        }
        else {
            scale = (sz.width - (2 * ARROW_SZ)) / (double) (getMaximum() - min);
        }

        if (direction == LEFTRIGHT_TOPBOTTOM) {
            return (int) (((xOrY - ARROW_SZ) / scale) + min + 0.5);
        }
        else {
            if (orientation == VERTICAL) {
                return (int) ((sz.height - xOrY - ARROW_SZ) / scale + min + 0.5);
            }
            else {
                return (int) ((sz.width - xOrY - ARROW_SZ) / scale + min + 0.5);
            }
        }
    }

    /**
     * Converts from a range value to screen coordinates.
     */
    protected int toScreen(int xOrY) {
        Dimension sz = getSize();
        int min = getMinimum();
        double scale;
        if (orientation == VERTICAL) {
            scale = (sz.height - (2 * ARROW_SZ)) / (double) (getMaximum() - min);
        }
        else {
            scale = (sz.width - (2 * ARROW_SZ)) / (double) (getMaximum() - min);
        }

        // If the direction is left/right_top/bottom then we subtract the min and multiply times scale
        // Otherwise, we have to invert the number by subtracting the value from the height
        if (direction == LEFTRIGHT_TOPBOTTOM) {
            return (int)(ARROW_SZ + ((xOrY - min) * scale) + 0.5);
        }
        else {
            if (orientation == VERTICAL) {
                return (int)(sz.height-(xOrY - min) * scale - ARROW_SZ + 0.5);
            }
            else {
                return (int)(sz.width-(xOrY - min) * scale - ARROW_SZ + 0.5);
            }
        }
    }

    /**
     * Converts from a range value to screen coordinates.
     */
    protected double toScreenDouble(int xOrY) {
        Dimension sz = getSize();
        int min = getMinimum();
        double scale;
        if (orientation == VERTICAL) {
            scale = (sz.height - (2 * ARROW_SZ)) / (double) (getMaximum()+1 - min);
        }
        else {
            scale = (sz.width - (2 * ARROW_SZ)) / (double) (getMaximum()+1 - min);
        }

        // If the direction is left/right_top/bottom then we subtract the min and multiply times scale
        // Otherwise, we have to invert the number by subtracting the value from the height
        if (direction == LEFTRIGHT_TOPBOTTOM) {
            return ARROW_SZ + ((xOrY - min) * scale);
        }
        else {
            if (orientation == VERTICAL) {
                return sz.height-(xOrY - min) * scale - ARROW_SZ;
            }
            else {
                return sz.width-(xOrY - min) * scale - ARROW_SZ;
            }
        }
    }


    // ------------------------------------------------------------------------
    // Event Handling

    static final int PICK_NONE = 0;
    static final int PICK_LEFT_OR_TOP = 1;
    static final int PICK_THUMB = 2;
    static final int PICK_RIGHT_OR_BOTTOM = 3;
    int pick;
    int pickOffsetLow;
    int pickOffsetHigh;
    int mouse;

    private int pickHandle(int xOrY) {
        int min = toScreen(getLowValue());
        int max = toScreen(getHighValue());
        int pick = PICK_NONE;

        if (direction == LEFTRIGHT_TOPBOTTOM) {
            if ((xOrY > (min - ARROW_SZ)) && (xOrY < min)) {
                pick = PICK_LEFT_OR_TOP;
            } else if ((xOrY >= min) && (xOrY <= max)) {
                pick = PICK_THUMB;
            } else if ((xOrY > max) && (xOrY < (max + ARROW_SZ))) {
                pick = PICK_RIGHT_OR_BOTTOM;
            }
        }
        else {
            if ((xOrY > min) && (xOrY < (min + ARROW_SZ))) {
                pick = PICK_LEFT_OR_TOP;
            } else if ((xOrY <= min) && (xOrY >= max)) {
                pick = PICK_THUMB;
            } else if ((xOrY > (max - ARROW_SZ) && (xOrY < max))) {
                pick = PICK_RIGHT_OR_BOTTOM;
            }
        }

        return pick;
    }

    private void offset(int dxOrDy) {
        model.setValue(model.getValue()+dxOrDy);
    }

    /**
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
        if (orientation == VERTICAL) {
            pick = pickHandle(e.getY());
            pickOffsetLow = e.getY() - toScreen(getLowValue());
            pickOffsetHigh = e.getY() - toScreen(getHighValue());
            mouse = e.getY();
        }
        else {
            pick = pickHandle(e.getX());
            pickOffsetLow = e.getX() - toScreen(getLowValue());
            pickOffsetHigh = e.getX() - toScreen(getHighValue());
            mouse = e.getX();
        }
        repaint();
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent e) {
        requestFocus();
        int value = (orientation == VERTICAL) ? e.getY() : e.getX();

        int minimum = getMinimum();
        int maximum = getMaximum();
        int lowValue = getLowValue();
        int highValue = getHighValue();

        switch (pick) {
            case PICK_LEFT_OR_TOP:
                int low = toLocal(value-pickOffsetLow);

                if (low < minimum) {
                    low = minimum;
                }
                if (low > maximum - minExtent) {
                    low = maximum - minExtent;
                }
                if (low > highValue-minExtent) {
                    setRange(low, low + minExtent);
                }
                else
                    setLowValue(low);
                break;

            case PICK_RIGHT_OR_BOTTOM:
                int high = toLocal(value-pickOffsetHigh);

                if (high < minimum + minExtent) {
                    high = minimum + minExtent;
                }
                if (high > maximum) {
                    high = maximum;
                }
                if (high < lowValue+minExtent) {
                    setRange(high - minExtent, high);
                }
                else
                    setHighValue(high);
                break;

            case PICK_THUMB:
                int dxOrDy = toLocal(value - pickOffsetLow) - lowValue;
                if ((dxOrDy < 0) && ((lowValue + dxOrDy) < minimum)) {
                    dxOrDy = minimum - lowValue;
                }
                if ((dxOrDy > 0) && ((highValue + dxOrDy) > maximum)) {
                    dxOrDy = maximum - highValue;
                }
                if (dxOrDy != 0) {
                    offset(dxOrDy);
                }
                break;
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
        pick = PICK_NONE;
        repaint();
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    public void mouseMoved(MouseEvent e) {
        if (orientation == VERTICAL) {
            switch (pickHandle(e.getY())) {
                case PICK_LEFT_OR_TOP:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case PICK_RIGHT_OR_BOTTOM:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case PICK_THUMB:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case PICK_NONE :
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
            }
        }
        else {
            switch (pickHandle(e.getX())) {
                case PICK_LEFT_OR_TOP:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case PICK_RIGHT_OR_BOTTOM:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case PICK_THUMB:
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case PICK_NONE :
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
            }
        }
    }

    /**
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
    }
    /**
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
    }
    /**
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
    }

    private void grow(int increment) {
        model.setRangeProperties(model.getValue()-increment,
            model.getExtent()+2*increment,
            model.getMinimum(), model.getMaximum(), false);
    }

    /**
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed(KeyEvent e) {
        int kc = e.getKeyCode();
        boolean v = (orientation == VERTICAL);
        boolean d = (kc == KeyEvent.VK_DOWN);
        boolean u = (kc == KeyEvent.VK_UP);
        boolean l = (kc == KeyEvent.VK_LEFT);
        boolean r = (kc == KeyEvent.VK_RIGHT);

        int minimum = getMinimum();
        int maximum = getMaximum();
        int lowValue = getLowValue();
        int highValue = getHighValue();

        if ( v&&r || !v&&u ) {
            if ( lowValue-increment >= minimum &&
                 highValue+increment <= maximum ) {
                grow(increment);
            }
        } else if ( v&&l || !v&&d ) {
            if ( highValue-lowValue >= 2*increment ) {
                grow(-1*increment);
            }
        } else if ( v&&d || !v&&l ) {
            if ( lowValue-increment >= minimum ) {
                offset(-increment);
            }
        } else if ( v&&u || !v&&r ) {
            if ( highValue+increment <= maximum ) {
                offset(increment);
            }
        }
    }

    /**
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased(KeyEvent e) {
    }
    /**
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped(KeyEvent e) {
    }

} // end of class JRangeSlider
=======
package GallowsEngine;
/*************************************************************************
 *  Compilation:  javac StdDraw.java
 *  Execution:    java StdDraw
 *
 *  Standard drawing library. This class provides a basic capability for
 *  creating drawings with your programs. It uses a simple graphics model that
 *  allows you to create drawings consisting of points, lines, and curves
 *  in a window on your computer and to save the drawings to a file.
 *
 *  Todo
 *  ----
 *    -  Add support for gradient fill, etc.
 *
 *  Remarks
 *  -------
 *    -  don't use AffineTransform for rescaling since it inverts
 *       images and strings
 *    -  careful using setFont in inner loop within an animation -
 *       it can cause flicker
 *
 *************************************************************************/

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * <i>Standard draw</i>. This class provides a basic capability for creating
 * drawings with your programs. It uses a simple graphics model that allows you
 * to create drawings consisting of points, lines, and curves in a window on
 * your computer and to save the drawings to a file.
 * <p>
 * For additional documentation, see <a
 * href="http://introcs.cs.princeton.edu/15inout">Section 1.5</a> of
 * <i>Introduction to Programming in Java: An Interdisciplinary Approach</i> by
 * Robert Sedgewick and Kevin Wayne.
 */
public final class StdDraw implements ActionListener, MouseListener,
		MouseMotionListener, KeyListener {

	// pre-defined colors
	public static final Color BLACK = Color.BLACK;
	public static final Color BLUE = Color.BLUE;
	public static final Color CYAN = Color.CYAN;
	public static final Color DARK_GRAY = Color.DARK_GRAY;
	public static final Color GRAY = Color.GRAY;
	public static final Color GREEN = Color.GREEN;
	public static final Color LIGHT_GRAY = Color.LIGHT_GRAY;
	public static final Color MAGENTA = Color.MAGENTA;
	public static final Color ORANGE = Color.ORANGE;
	public static final Color PINK = Color.PINK;
	public static final Color RED = Color.RED;
	public static final Color WHITE = Color.WHITE;
	public static final Color YELLOW = Color.YELLOW;

	public static Color starColor(float hue) {
		return Color.getHSBColor(hue, .2f, 1f);
	}
	public static Color planetColor(float hue) {
		return Color.getHSBColor(hue, .5f, 5f);
	}

	/**
	 * Shade of blue used in Introduction to Programming in Java. It is Pantone
	 * 300U. The RGB values are approximately (9, 90, 266).
	 */
	public static final Color BOOK_BLUE = new Color(9, 90, 166);
	public static final Color BOOK_LIGHT_BLUE = new Color(103, 198, 243);

	/**
	 * Shade of red used in Algorithms 4th edition. It is Pantone 1805U. The RGB
	 * values are approximately (150, 35, 31).
	 */
	public static final Color BOOK_RED = new Color(150, 35, 31);

	// default colors
	private static final Color DEFAULT_PEN_COLOR = BLACK;
	private static final Color DEFAULT_CLEAR_COLOR = WHITE;

	// current pen color
	private static Color penColor;

	// default canvas size is DEFAULT_SIZE-by-DEFAULT_SIZE
	private static final int DEFAULT_SIZE = 512;
	private static int width = DEFAULT_SIZE;
	private static int height = DEFAULT_SIZE;

	// default pen radius
	private static final double DEFAULT_PEN_RADIUS = 0.002;

	// current pen radius
	private static double penRadius;

	// show we draw immediately or wait until next show?
	private static boolean defer = false;

	// boundary of drawing canvas, 5% border
	private static final double BORDER = 0.05;
	private static final double DEFAULT_XMIN = 0.0;
	private static final double DEFAULT_XMAX = 1.0;
	private static final double DEFAULT_YMIN = 0.0;
	private static final double DEFAULT_YMAX = 1.0;
	private static double xmin, ymin, xmax, ymax;

	// for synchronization
	private static Object mouseLock = new Object();
	private static Object keyLock = new Object();

	// default font
	private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN,
			16);

	// current font
	private static Font font;

	// double buffered graphics
	private static BufferedImage offscreenImage, onscreenImage;
	private static Graphics2D offscreen, onscreen;

	// singleton for callbacks: avoids generation of extra .class files
	private static StdDraw std = new StdDraw();

	// the frame for drawing to the screen
	private static JFrame frame;

	// mouse state
	private static boolean mousePressed = false;
	private static double mouseX = 0;
	private static double mouseY = 0;

	// queue of typed key characters
	private static LinkedList<Character> keysTyped = new LinkedList<Character>();

	// set of key codes currently pressed down
	private static TreeSet<Integer> keysDown = new TreeSet<Integer>();

	// not instantiable
	private StdDraw() {
	}

	// static initializer
	static {
		init();
	}

	/**
	 * Set the window size to the default size 512-by-512 pixels.
	 */
	public static void setCanvasSize() {
		setCanvasSize(DEFAULT_SIZE, DEFAULT_SIZE);
	}

	/**
	 * Set the window size to w-by-h pixels.
	 * 
	 * @param w
	 *            the width as a number of pixels
	 * @param h
	 *            the height as a number of pixels
	 * @throws a
	 *             RunTimeException if the width or height is 0 or negative
	 */
	public static void setCanvasSize(int w, int h) {
		if (w < 1 || h < 1)
			throw new RuntimeException("width and height must be positive");
		width = w;
		height = h;
		init();
	}

	// init
	private static void init() {
		if (frame != null)
			frame.setVisible(false);
		frame = new JFrame();
		offscreenImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		onscreenImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		offscreen = offscreenImage.createGraphics();
		onscreen = onscreenImage.createGraphics();
		setXscale();
		setYscale();
		offscreen.setColor(DEFAULT_CLEAR_COLOR);
		offscreen.fillRect(0, 0, width, height);
		setPenColor();
		setPenRadius();
		setFont();
		clear();

		// add antialiasing
		RenderingHints hints = new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		hints.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		offscreen.addRenderingHints(hints);

		// frame stuff
		ImageIcon icon = new ImageIcon(onscreenImage);
		JLabel draw = new JLabel(icon);

		draw.addMouseListener(std);
		draw.addMouseMotionListener(std);

		frame.setContentPane(draw);
		frame.addKeyListener(std); // JLabel cannot get keyboard focus
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // closes all
																// windows
		// frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // closes
		// only current window
		frame.setTitle("GallowsEngine");
		frame.setJMenuBar(createMenuBar());
		frame.pack();
		frame.requestFocusInWindow();
		frame.setVisible(true);
	}

	// create the menu bar (changed to private)
	private static JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menuBar.add(menu);
		JMenuItem menuItem1 = new JMenuItem(" Save...   ");
		menuItem1.addActionListener(std);
		menuItem1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()));
		menu.add(menuItem1);
		return menuBar;
	}

	/*************************************************************************
	 * User and screen coordinate systems
	 *************************************************************************/

	/**
	 * Set the x-scale to be the default (between 0.0 and 1.0).
	 */
	public static void setXscale() {
		setXscale(DEFAULT_XMIN, DEFAULT_XMAX);
	}

	/**
	 * Set the y-scale to be the default (between 0.0 and 1.0).
	 */
	public static void setYscale() {
		setYscale(DEFAULT_YMIN, DEFAULT_YMAX);
	}

	/**
	 * Set the x-scale (a 10% border is added to the values)
	 * 
	 * @param min
	 *            the minimum value of the x-scale
	 * @param max
	 *            the maximum value of the x-scale
	 */
	public static void setXscale(double min, double max) {
		double size = max - min;
		xmin = min - BORDER * size;
		xmax = max + BORDER * size;
	}

	/**
	 * Set the y-scale (a 10% border is added to the values).
	 * 
	 * @param min
	 *            the minimum value of the y-scale
	 * @param max
	 *            the maximum value of the y-scale
	 */
	public static void setYscale(double min, double max) {
		double size = max - min;
		ymin = min - BORDER * size;
		ymax = max + BORDER * size;
	}

	/**
	 * Set the x-scale and y-scale (a 10% border is added to the values)
	 * 
	 * @param min
	 *            the minimum value of the x- and y-scales
	 * @param max
	 *            the maximum value of the x- and y-scales
	 */
	public static void setScale(double min, double max) {
		setXscale(min, max);
		setYscale(min, max);
	}

	// helper functions that scale from user coordinates to screen coordinates
	// and back
	private static double scaleX(double x) {
		return width * (x - xmin) / (xmax - xmin);
	}

	private static double scaleY(double y) {
		return height * (ymax - y) / (ymax - ymin);
	}

	private static double factorX(double w) {
		return w * width / Math.abs(xmax - xmin);
	}

	private static double factorY(double h) {
		return h * height / Math.abs(ymax - ymin);
	}

	private static double userX(double x) {
		return xmin + x * (xmax - xmin) / width;
	}

	private static double userY(double y) {
		return ymax - y * (ymax - ymin) / height;
	}

	/**
	 * Clear the screen to the default color (white).
	 */
	public static void clear() {
		clear(DEFAULT_CLEAR_COLOR);
	}

	/**
	 * Clear the screen to the given color.
	 * 
	 * @param color
	 *            the Color to make the background
	 */
	public static void clear(Color color) {
		offscreen.setColor(color);
		offscreen.fillRect(0, 0, width, height);
		offscreen.setColor(penColor);
		draw();
	}

	/**
	 * Get the current pen radius.
	 */
	public static double getPenRadius() {
		return penRadius;
	}

	/**
	 * Set the pen size to the default (.002).
	 */
	public static void setPenRadius() {
		setPenRadius(DEFAULT_PEN_RADIUS);
	}

	/**
	 * Set the radius of the pen to the given size.
	 * 
	 * @param r
	 *            the radius of the pen
	 * @throws RuntimeException
	 *             if r is negative
	 */
	public static void setPenRadius(double r) {
		if (r < 0)
			throw new RuntimeException("pen radius must be positive");
		penRadius = r * DEFAULT_SIZE;
		BasicStroke stroke = new BasicStroke((float) penRadius,
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		// BasicStroke stroke = new BasicStroke((float) penRadius);
		offscreen.setStroke(stroke);
	}

	/**
	 * Get the current pen color.
	 */
	public static Color getPenColor() {
		return penColor;
	}

	/**
	 * Set the pen color to the default color (black).
	 */
	public static void setPenColor() {
		setPenColor(DEFAULT_PEN_COLOR);
	}

	/**
	 * Set the pen color to the given color. The available pen colors are BLACK,
	 * BLUE, CYAN, DARK_GRAY, GRAY, GREEN, LIGHT_GRAY, MAGENTA, ORANGE, PINK,
	 * RED, WHITE, and YELLOW.
	 * 
	 * @param color
	 *            the Color to make the pen
	 */
	public static void setPenColor(Color color) {
		penColor = color;
		offscreen.setColor(penColor);
	}

	/**
	 * Get the current font.
	 */
	public static Font getFont() {
		return font;
	}

	/**
	 * Set the font to the default font (sans serif, 16 point).
	 */
	public static void setFont() {
		setFont(DEFAULT_FONT);
	}

	/**
	 * Set the font to the given value.
	 * 
	 * @param f
	 *            the font to make text
	 */
	public static void setFont(Font f) {
		font = f;
	}

	/*************************************************************************
	 * Drawing geometric shapes.
	 *************************************************************************/

	/**
	 * Draw a line from (x0, y0) to (x1, y1).
	 * 
	 * @param x0
	 *            the x-coordinate of the starting point
	 * @param y0
	 *            the y-coordinate of the starting point
	 * @param x1
	 *            the x-coordinate of the destination point
	 * @param y1
	 *            the y-coordinate of the destination point
	 */
	public static void line(double x0, double y0, double x1, double y1) {
		offscreen.draw(new Line2D.Double(scaleX(x0), scaleY(y0), scaleX(x1),
				scaleY(y1)));
		draw();
	}

	/**
	 * Draw one pixel at (x, y).
	 * 
	 * @param x
	 *            the x-coordinate of the pixel
	 * @param y
	 *            the y-coordinate of the pixel
	 */
	private static void pixel(double x, double y) {
		offscreen.fillRect((int) Math.round(scaleX(x)),
				(int) Math.round(scaleY(y)), 1, 1);
	}

	/**
	 * Draw a point at (x, y).
	 * 
	 * @param x
	 *            the x-coordinate of the point
	 * @param y
	 *            the y-coordinate of the point
	 */
	public static void point(double x, double y) {
		double xs = scaleX(x);
		double ys = scaleY(y);
		double r = penRadius;
		// double ws = factorX(2*r);
		// double hs = factorY(2*r);
		// if (ws <= 1 && hs <= 1) pixel(x, y);
		if (r <= 1)
			pixel(x, y);
		else
			offscreen.fill(new Ellipse2D.Double(xs - r / 2, ys - r / 2, r, r));
		draw();
	}

	/**
	 * Draw a circle of radius r, centered on (x, y).
	 * 
	 * @param x
	 *            the x-coordinate of the center of the circle
	 * @param y
	 *            the y-coordinate of the center of the circle
	 * @param r
	 *            the radius of the circle
	 * @throws RuntimeException
	 *             if the radius of the circle is negative
	 */
	public static void circle(double x, double y, double r) {
		if (r < 0)
			throw new RuntimeException("circle radius can't be negative");
		double xs = scaleX(x);
		double ys = scaleY(y);
		double ws = factorX(2 * r);
		double hs = factorY(2 * r);
		if (ws <= 1 && hs <= 1)
			pixel(x, y);
		else
			offscreen.draw(new Ellipse2D.Double(xs - ws / 2, ys - hs / 2, ws,
					hs));
		draw();
	}

	/**
	 * Draw filled circle of radius r, centered on (x, y).
	 * 
	 * @param x
	 *            the x-coordinate of the center of the circle
	 * @param y
	 *            the y-coordinate of the center of the circle
	 * @param r
	 *            the radius of the circle
	 * @throws RuntimeException
	 *             if the radius of the circle is negative
	 */
	public static void filledCircle(double x, double y, double r) {
		if (r < 0)
			throw new RuntimeException("circle radius can't be negative");
		double xs = scaleX(x);
		double ys = scaleY(y);
		double ws = factorX(2 * r);
		double hs = factorY(2 * r);
		if (ws <= 1 && hs <= 1)
			pixel(x, y);
		else
			offscreen.fill(new Ellipse2D.Double(xs - ws / 2, ys - hs / 2, ws,
					hs));
		draw();
	}

	/**
	 * Draw an ellipse with given semimajor and semiminor axes, centered on (x,
	 * y).
	 * 
	 * @param x
	 *            the x-coordinate of the center of the ellipse
	 * @param y
	 *            the y-coordinate of the center of the ellipse
	 * @param semiMajorAxis
	 *            is the semimajor axis of the ellipse
	 * @param semiMinorAxis
	 *            is the semiminor axis of the ellipse
	 * @throws RuntimeException
	 *             if either of the axes are negative
	 */
	public static void ellipse(double x, double y, double semiMajorAxis,
			double semiMinorAxis) {
		if (semiMajorAxis < 0)
			throw new RuntimeException(
					"ellipse semimajor axis can't be negative");
		if (semiMinorAxis < 0)
			throw new RuntimeException(
					"ellipse semiminor axis can't be negative");
		double xs = scaleX(x);
		double ys = scaleY(y);
		double ws = factorX(2 * semiMajorAxis);
		double hs = factorY(2 * semiMinorAxis);
		if (ws <= 1 && hs <= 1)
			pixel(x, y);
		else
			offscreen.draw(new Ellipse2D.Double(xs - ws / 2, ys - hs / 2, ws,
					hs));
		draw();
	}

	/**
	 * Draw an ellipse with given semimajor and semiminor axes, centered on (x,
	 * y).
	 * 
	 * @param x
	 *            the x-coordinate of the center of the ellipse
	 * @param y
	 *            the y-coordinate of the center of the ellipse
	 * @param semiMajorAxis
	 *            is the semimajor axis of the ellipse
	 * @param semiMinorAxis
	 *            is the semiminor axis of the ellipse
	 * @throws RuntimeException
	 *             if either of the axes are negative
	 */
	public static void filledEllipse(double x, double y, double semiMajorAxis,
			double semiMinorAxis) {
		if (semiMajorAxis < 0)
			throw new RuntimeException(
					"ellipse semimajor axis can't be negative");
		if (semiMinorAxis < 0)
			throw new RuntimeException(
					"ellipse semiminor axis can't be negative");
		double xs = scaleX(x);
		double ys = scaleY(y);
		double ws = factorX(2 * semiMajorAxis);
		double hs = factorY(2 * semiMinorAxis);
		if (ws <= 1 && hs <= 1)
			pixel(x, y);
		else
			offscreen.fill(new Ellipse2D.Double(xs - ws / 2, ys - hs / 2, ws,
					hs));
		draw();
	}

	/**
	 * Draw an arc of radius r, centered on (x, y), from angle1 to angle2 (in
	 * degrees).
	 * 
	 * @param x
	 *            the x-coordinate of the center of the circle
	 * @param y
	 *            the y-coordinate of the center of the circle
	 * @param r
	 *            the radius of the circle
	 * @param angle1
	 *            the starting angle. 0 would mean an arc beginning at 3
	 *            o'clock.
	 * @param angle2
	 *            the angle at the end of the arc. For example, if you want a 90
	 *            degree arc, then angle2 should be angle1 + 90.
	 * @throws RuntimeException
	 *             if the radius of the circle is negative
	 */
	public static void arc(double x, double y, double r, double angle1,
			double angle2) {
		if (r < 0)
			throw new RuntimeException("arc radius can't be negative");
		while (angle2 < angle1)
			angle2 += 360;
		double xs = scaleX(x);
		double ys = scaleY(y);
		double ws = factorX(2 * r);
		double hs = factorY(2 * r);
		if (ws <= 1 && hs <= 1)
			pixel(x, y);
		else
			offscreen.draw(new Arc2D.Double(xs - ws / 2, ys - hs / 2, ws, hs,
					angle1, angle2 - angle1, Arc2D.OPEN));
		draw();
	}

	/**
	 * Draw a square of side length 2r, centered on (x, y).
	 * 
	 * @param x
	 *            the x-coordinate of the center of the square
	 * @param y
	 *            the y-coordinate of the center of the square
	 * @param r
	 *            radius is half the length of any side of the square
	 * @throws RuntimeException
	 *             if r is negative
	 */
	public static void square(double x, double y, double r) {
		if (r < 0)
			throw new RuntimeException("square side length can't be negative");
		double xs = scaleX(x);
		double ys = scaleY(y);
		double ws = factorX(2 * r);
		double hs = factorY(2 * r);
		if (ws <= 1 && hs <= 1)
			pixel(x, y);
		else
			offscreen.draw(new Rectangle2D.Double(xs - ws / 2, ys - hs / 2, ws,
					hs));
		draw();
	}

	/**
	 * Draw a filled square of side length 2r, centered on (x, y).
	 * 
	 * @param x
	 *            the x-coordinate of the center of the square
	 * @param y
	 *            the y-coordinate of the center of the square
	 * @param r
	 *            radius is half the length of any side of the square
	 * @throws RuntimeException
	 *             if r is negative
	 */
	public static void filledSquare(double x, double y, double r) {
		if (r < 0)
			throw new RuntimeException("square side length can't be negative");
		double xs = scaleX(x);
		double ys = scaleY(y);
		double ws = factorX(2 * r);
		double hs = factorY(2 * r);
		if (ws <= 1 && hs <= 1)
			pixel(x, y);
		else
			offscreen.fill(new Rectangle2D.Double(xs - ws / 2, ys - hs / 2, ws,
					hs));
		draw();
	}

	/**
	 * Draw a rectangle of given half width and half height, centered on (x, y).
	 * 
	 * @param x
	 *            the x-coordinate of the center of the rectangle
	 * @param y
	 *            the y-coordinate of the center of the rectangle
	 * @param halfWidth
	 *            is half the width of the rectangle
	 * @param halfHeight
	 *            is half the height of the rectangle
	 * @throws RuntimeException
	 *             if halfWidth or halfHeight is negative
	 */
	public static void rectangle(double x, double y, double halfWidth,
			double halfHeight) {
		if (halfWidth < 0)
			throw new RuntimeException("half width can't be negative");
		if (halfHeight < 0)
			throw new RuntimeException("half height can't be negative");
		double xs = scaleX(x);
		double ys = scaleY(y);
		double ws = factorX(2 * halfWidth);
		double hs = factorY(2 * halfHeight);
		if (ws <= 1 && hs <= 1)
			pixel(x, y);
		else
			offscreen.draw(new Rectangle2D.Double(xs - ws / 2, ys - hs / 2, ws,
					hs));
		draw();
	}

	/**
	 * Draw a filled rectangle of given half width and half height, centered on
	 * (x, y).
	 * 
	 * @param x
	 *            the x-coordinate of the center of the rectangle
	 * @param y
	 *            the y-coordinate of the center of the rectangle
	 * @param halfWidth
	 *            is half the width of the rectangle
	 * @param halfHeight
	 *            is half the height of the rectangle
	 * @throws RuntimeException
	 *             if halfWidth or halfHeight is negative
	 */
	public static void filledRectangle(double x, double y, double halfWidth,
			double halfHeight) {
		if (halfWidth < 0)
			throw new RuntimeException("half width can't be negative");
		if (halfHeight < 0)
			throw new RuntimeException("half height can't be negative");
		double xs = scaleX(x);
		double ys = scaleY(y);
		double ws = factorX(2 * halfWidth);
		double hs = factorY(2 * halfHeight);
		if (ws <= 1 && hs <= 1)
			pixel(x, y);
		else
			offscreen.fill(new Rectangle2D.Double(xs - ws / 2, ys - hs / 2, ws,
					hs));
		draw();
	}

	/**
	 * Draw a polygon with the given (x[i], y[i]) coordinates.
	 * 
	 * @param x
	 *            an array of all the x-coordindates of the polygon
	 * @param y
	 *            an array of all the y-coordindates of the polygon
	 */
	public static void polygon(double[] x, double[] y) {
		int N = x.length;
		GeneralPath path = new GeneralPath();
		path.moveTo((float) scaleX(x[0]), (float) scaleY(y[0]));
		for (int i = 0; i < N; i++)
			path.lineTo((float) scaleX(x[i]), (float) scaleY(y[i]));
		path.closePath();
		offscreen.draw(path);
		draw();
	}

	/**
	 * Draw a filled polygon with the given (x[i], y[i]) coordinates.
	 * 
	 * @param x
	 *            an array of all the x-coordindates of the polygon
	 * @param y
	 *            an array of all the y-coordindates of the polygon
	 */
	public static void filledPolygon(double[] x, double[] y) {
		int N = x.length;
		GeneralPath path = new GeneralPath();
		path.moveTo((float) scaleX(x[0]), (float) scaleY(y[0]));
		for (int i = 0; i < N; i++)
			path.lineTo((float) scaleX(x[i]), (float) scaleY(y[i]));
		path.closePath();
		offscreen.fill(path);
		draw();
	}

	/*************************************************************************
	 * Drawing images.
	 *************************************************************************/

	// get an image from the given filename
	private static Image getImage(String filename) {

		// to read from file
		ImageIcon icon = new ImageIcon(filename);

		// try to read from URL
		if ((icon == null)
				|| (icon.getImageLoadStatus() != MediaTracker.COMPLETE)) {
			try {
				URL url = new URL(filename);
				icon = new ImageIcon(url);
			} catch (Exception e) { /* not a url */
			}
		}

		// in case file is inside a .jar
		if ((icon == null)
				|| (icon.getImageLoadStatus() != MediaTracker.COMPLETE)) {
			URL url = StdDraw.class.getResource(filename);
			if (url == null)
				throw new RuntimeException("image " + filename + " not found");
			icon = new ImageIcon(url);
		}

		return icon.getImage();
	}

	/**
	 * Draw picture (gif, jpg, or png) centered on (x, y).
	 * 
	 * @param x
	 *            the center x-coordinate of the image
	 * @param y
	 *            the center y-coordinate of the image
	 * @param s
	 *            the name of the image/picture, e.g., "ball.gif"
	 * @throws RuntimeException
	 *             if the image is corrupt
	 */
	public static void picture(double x, double y, String s) {
		Image image = getImage(s);
		double xs = scaleX(x);
		double ys = scaleY(y);
		int ws = image.getWidth(null);
		int hs = image.getHeight(null);
		if (ws < 0 || hs < 0)
			throw new RuntimeException("image " + s + " is corrupt");

		offscreen.drawImage(image, (int) Math.round(xs - ws / 2.0),
				(int) Math.round(ys - hs / 2.0), null);
		draw();
	}

	/**
	 * Draw picture (gif, jpg, or png) centered on (x, y), rotated given number
	 * of degrees
	 * 
	 * @param x
	 *            the center x-coordinate of the image
	 * @param y
	 *            the center y-coordinate of the image
	 * @param s
	 *            the name of the image/picture, e.g., "ball.gif"
	 * @param degrees
	 *            is the number of degrees to rotate counterclockwise
	 * @throws RuntimeException
	 *             if the image is corrupt
	 */
	public static void picture(double x, double y, String s, double degrees) {
		Image image = getImage(s);
		double xs = scaleX(x);
		double ys = scaleY(y);
		int ws = image.getWidth(null);
		int hs = image.getHeight(null);
		if (ws < 0 || hs < 0)
			throw new RuntimeException("image " + s + " is corrupt");

		offscreen.rotate(Math.toRadians(-degrees), xs, ys);
		offscreen.drawImage(image, (int) Math.round(xs - ws / 2.0),
				(int) Math.round(ys - hs / 2.0), null);
		offscreen.rotate(Math.toRadians(+degrees), xs, ys);

		draw();
	}

	/**
	 * Draw picture (gif, jpg, or png) centered on (x, y), rescaled to w-by-h.
	 * 
	 * @param x
	 *            the center x coordinate of the image
	 * @param y
	 *            the center y coordinate of the image
	 * @param s
	 *            the name of the image/picture, e.g., "ball.gif"
	 * @param w
	 *            the width of the image
	 * @param h
	 *            the height of the image
	 * @throws RuntimeException
	 *             if the width height are negative
	 * @throws RuntimeException
	 *             if the image is corrupt
	 */
	public static void picture(double x, double y, String s, double w, double h) {
		Image image = getImage(s);
		double xs = scaleX(x);
		double ys = scaleY(y);
		if (w < 0)
			throw new RuntimeException("width is negative: " + w);
		if (h < 0)
			throw new RuntimeException("height is negative: " + h);
		double ws = factorX(w);
		double hs = factorY(h);
		if (ws < 0 || hs < 0)
			throw new RuntimeException("image " + s + " is corrupt");
		if (ws <= 1 && hs <= 1)
			pixel(x, y);
		else {
			offscreen.drawImage(image, (int) Math.round(xs - ws / 2.0),
					(int) Math.round(ys - hs / 2.0), (int) Math.round(ws),
					(int) Math.round(hs), null);
		}
		draw();
	}

	/**
	 * Draw picture (gif, jpg, or png) centered on (x, y), rotated given number
	 * of degrees, rescaled to w-by-h.
	 * 
	 * @param x
	 *            the center x-coordinate of the image
	 * @param y
	 *            the center y-coordinate of the image
	 * @param s
	 *            the name of the image/picture, e.g., "ball.gif"
	 * @param w
	 *            the width of the image
	 * @param h
	 *            the height of the image
	 * @param degrees
	 *            is the number of degrees to rotate counterclockwise
	 * @throws RuntimeException
	 *             if the image is corrupt
	 */
	public static void picture(double x, double y, String s, double w,
			double h, double degrees) {
		Image image = getImage(s);
		double xs = scaleX(x);
		double ys = scaleY(y);
		double ws = factorX(w);
		double hs = factorY(h);
		if (ws < 0 || hs < 0)
			throw new RuntimeException("image " + s + " is corrupt");
		if (ws <= 1 && hs <= 1)
			pixel(x, y);

		offscreen.rotate(Math.toRadians(-degrees), xs, ys);
		offscreen.drawImage(image, (int) Math.round(xs - ws / 2.0),
				(int) Math.round(ys - hs / 2.0), (int) Math.round(ws),
				(int) Math.round(hs), null);
		offscreen.rotate(Math.toRadians(+degrees), xs, ys);

		draw();
	}

	/*************************************************************************
	 * Drawing text.
	 *************************************************************************/

	/**
	 * Write the given text string in the current font, centered on (x, y).
	 * 
	 * @param x
	 *            the center x-coordinate of the text
	 * @param y
	 *            the center y-coordinate of the text
	 * @param s
	 *            the text
	 */
	public static void text(double x, double y, String s) {
		offscreen.setFont(font);
		FontMetrics metrics = offscreen.getFontMetrics();
		double xs = scaleX(x);
		double ys = scaleY(y);
		int ws = metrics.stringWidth(s);
		int hs = metrics.getDescent();
		offscreen.drawString(s, (float) (xs - ws / 2.0), (float) (ys + hs));
		draw();
	}

	/**
	 * Write the given text string in the current font, centered on (x, y) and
	 * rotated by the specified number of degrees
	 * 
	 * @param x
	 *            the center x-coordinate of the text
	 * @param y
	 *            the center y-coordinate of the text
	 * @param s
	 *            the text
	 * @param degrees
	 *            is the number of degrees to rotate counterclockwise
	 */
	public static void text(double x, double y, String s, double degrees) {
		double xs = scaleX(x);
		double ys = scaleY(y);
		offscreen.rotate(Math.toRadians(-degrees), xs, ys);
		text(x, y, s);
		offscreen.rotate(Math.toRadians(+degrees), xs, ys);
	}

	/**
	 * Write the given text string in the current font, left-aligned at (x, y).
	 * 
	 * @param x
	 *            the x-coordinate of the text
	 * @param y
	 *            the y-coordinate of the text
	 * @param s
	 *            the text
	 */
	public static void textLeft(double x, double y, String s) {
		offscreen.setFont(font);
		FontMetrics metrics = offscreen.getFontMetrics();
		double xs = scaleX(x);
		double ys = scaleY(y);
		int hs = metrics.getDescent();
		offscreen.drawString(s, (float) (xs), (float) (ys + hs));
		draw();
	}

	/**
	 * Write the given text string in the current font, right-aligned at (x, y).
	 * 
	 * @param x
	 *            the x-coordinate of the text
	 * @param y
	 *            the y-coordinate of the text
	 * @param s
	 *            the text
	 */
	public static void textRight(double x, double y, String s) {
		offscreen.setFont(font);
		FontMetrics metrics = offscreen.getFontMetrics();
		double xs = scaleX(x);
		double ys = scaleY(y);
		int ws = metrics.stringWidth(s);
		int hs = metrics.getDescent();
		offscreen.drawString(s, (float) (xs - ws), (float) (ys + hs));
		draw();
	}

	/**
	 * Display on screen, pause for t milliseconds, and turn on
	 * <em>animation mode</em>: subsequent calls to drawing methods such as
	 * <tt>line()</tt>, <tt>circle()</tt>, and <tt>square()</tt> will not be
	 * displayed on screen until the next call to <tt>show()</tt>. This is
	 * useful for producing animations (clear the screen, draw a bunch of
	 * shapes, display on screen for a fixed amount of time, and repeat). It
	 * also speeds up drawing a huge number of shapes (call <tt>show(0)</tt> to
	 * defer drawing on screen, draw the shapes, and call <tt>show(0)</tt> to
	 * display them all on screen at once).
	 * 
	 * @param t
	 *            number of milliseconds
	 */
	@SuppressWarnings("static-access")
	public static void show(int t) {
		defer = false;
		draw();
		try {
			Thread.currentThread().sleep(t);
		} catch (InterruptedException e) {
			System.out.println("Error sleeping");
		}
		defer = true;
	}

	/**
	 * Display on-screen and turn off animation mode: subsequent calls to
	 * drawing methods such as <tt>line()</tt>, <tt>circle()</tt>, and
	 * <tt>square()</tt> will be displayed on screen when called. This is the
	 * default.
	 */
	public static void show() {
		defer = false;
		draw();
	}

	// draw onscreen if defer is false
	private static void draw() {
		if (defer)
			return;
		onscreen.drawImage(offscreenImage, 0, 0, null);
		frame.repaint();
	}

	/*************************************************************************
	 * Save drawing to a file.
	 *************************************************************************/

	/**
	 * Save onscreen image to file - suffix must be png, jpg, or gif.
	 * 
	 * @param filename
	 *            the name of the file with one of the required suffixes
	 */
	public static void save(String filename) {
		File file = new File(filename);
		String suffix = filename.substring(filename.lastIndexOf('.') + 1);

		// png files
		if (suffix.toLowerCase().equals("png")) {
			try {
				ImageIO.write(onscreenImage, suffix, file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// need to change from ARGB to RGB for jpeg
		// reference:
		// http://archives.java.sun.com/cgi-bin/wa?A2=ind0404&L=java2d-interest&D=0&P=2727
		else if (suffix.toLowerCase().equals("jpg")) {
			WritableRaster raster = onscreenImage.getRaster();
			WritableRaster newRaster;
			newRaster = raster.createWritableChild(0, 0, width, height, 0, 0,
					new int[] { 0, 1, 2 });
			DirectColorModel cm = (DirectColorModel) onscreenImage
					.getColorModel();
			DirectColorModel newCM = new DirectColorModel(cm.getPixelSize(),
					cm.getRedMask(), cm.getGreenMask(), cm.getBlueMask());
			BufferedImage rgbBuffer = new BufferedImage(newCM, newRaster,
					false, null);
			try {
				ImageIO.write(rgbBuffer, suffix, file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		else {
			System.out.println("Invalid image file type: " + suffix);
		}
	}

	/**
	 * This method cannot be called directly.
	 */
	public void actionPerformed(ActionEvent e) {
		FileDialog chooser = new FileDialog(StdDraw.frame,
				"Use a .png or .jpg extension", FileDialog.SAVE);
		chooser.setVisible(true);
		String filename = chooser.getFile();
		if (filename != null) {
			StdDraw.save(chooser.getDirectory() + File.separator
					+ chooser.getFile());
		}
	}

	/*************************************************************************
	 * Mouse interactions.
	 *************************************************************************/

	/**
	 * Is the mouse being pressed?
	 * 
	 * @return true or false
	 */
	public static boolean mousePressed() {
		synchronized (mouseLock) {
			return mousePressed;
		}
	}

	/**
	 * What is the x-coordinate of the mouse?
	 * 
	 * @return the value of the x-coordinate of the mouse
	 */
	public static double mouseX() {
		synchronized (mouseLock) {
			return mouseX;
		}
	}

	/**
	 * What is the y-coordinate of the mouse?
	 * 
	 * @return the value of the y-coordinate of the mouse
	 */
	public static double mouseY() {
		synchronized (mouseLock) {
			return mouseY;
		}
	}

	/**
	 * This method cannot be called directly.
	 */
	public void mouseClicked(MouseEvent e) {
	}

	/**
	 * This method cannot be called directly.
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * This method cannot be called directly.
	 */
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * This method cannot be called directly.
	 */
	public void mousePressed(MouseEvent e) {
		synchronized (mouseLock) {
			mouseX = StdDraw.userX(e.getX());
			mouseY = StdDraw.userY(e.getY());
			mousePressed = true;
		}
	}

	/**
	 * This method cannot be called directly.
	 */
	public void mouseReleased(MouseEvent e) {
		synchronized (mouseLock) {
			mousePressed = false;
		}
	}

	/**
	 * This method cannot be called directly.
	 */
	public void mouseDragged(MouseEvent e) {
		synchronized (mouseLock) {
			mouseX = StdDraw.userX(e.getX());
			mouseY = StdDraw.userY(e.getY());
		}
	}

	/**
	 * This method cannot be called directly.
	 */
	public void mouseMoved(MouseEvent e) {
		synchronized (mouseLock) {
			mouseX = StdDraw.userX(e.getX());
			mouseY = StdDraw.userY(e.getY());
		}
	}

	/*************************************************************************
	 * Keyboard interactions.
	 *************************************************************************/

	/**
	 * Has the user typed a key?
	 * 
	 * @return true if the user has typed a key, false otherwise
	 */
	public static boolean hasNextKeyTyped() {
		synchronized (keyLock) {
			return !keysTyped.isEmpty();
		}
	}

	/**
	 * What is the next key that was typed by the user? This method returns a
	 * Unicode character corresponding to the key typed (such as 'a' or 'A'). It
	 * cannot identify action keys (such as F1 and arrow keys) or modifier keys
	 * (such as control).
	 * 
	 * @return the next Unicode key typed
	 */
	public static char nextKeyTyped() {
		synchronized (keyLock) {
			return keysTyped.removeLast();
		}
	}

	/**
	 * Is the keycode currently being pressed? This method takes as an argument
	 * the keycode (corresponding to a physical key). It can handle action keys
	 * (such as F1 and arrow keys) and modifier keys (such as shift and
	 * control). See <a href =
	 * "http://download.oracle.com/javase/6/docs/api/java/awt/event/KeyEvent.html"
	 * >KeyEvent.java</a> for a description of key codes.
	 * 
	 * @return true if keycode is currently being pressed, false otherwise
	 */
	public static boolean isKeyPressed(int keycode) {
		return keysDown.contains(keycode);
	}

	/**
	 * This method cannot be called directly.
	 */
	public void keyTyped(KeyEvent e) {
		synchronized (keyLock) {
			keysTyped.addFirst(e.getKeyChar());
		}
	}

	/**
	 * This method cannot be called directly.
	 */
	public void keyPressed(KeyEvent e) {
		keysDown.add(e.getKeyCode());
	}

	/**
	 * This method cannot be called directly.
	 */
	public void keyReleased(KeyEvent e) {
		keysDown.remove(e.getKeyCode());
	}

	/**
	 * Test client.
	 */
	public static void main(String[] args) {
		StdDraw.square(.2, .8, .1);
		StdDraw.filledSquare(.8, .8, .2);
		StdDraw.circle(.8, .2, .2);

		StdDraw.setPenColor(StdDraw.BOOK_RED);
		StdDraw.setPenRadius(.02);
		StdDraw.arc(.8, .2, .1, 200, 45);

		// draw a blue diamond
		StdDraw.setPenRadius();
		StdDraw.setPenColor(StdDraw.BOOK_BLUE);
		double[] x = { .1, .2, .3, .2 };
		double[] y = { .2, .3, .2, .1 };
		StdDraw.filledPolygon(x, y);

		// text
		StdDraw.setPenColor(StdDraw.BLACK);
		StdDraw.text(0.2, 0.5, "black text");
		StdDraw.setPenColor(StdDraw.WHITE);
		StdDraw.text(0.8, 0.8, "white text");
	}

}

>>>>>>> 76aa07461566a5976980e6696204781271955163
