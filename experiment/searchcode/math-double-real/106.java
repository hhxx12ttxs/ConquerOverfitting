/*
 * Copyright (C) 2011, EADS France
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package xowl.gmi.view.diagram;

import java.util.List;

/**
 *
 * @author Laurent WOUTERS
 */
public class Optional extends Widget {
    private static final Couple minSize = new Couple(25, 25);
    
    public static interface Actions {
        boolean acceptFlavor(java.awt.datatransfer.DataFlavor flavor);
        boolean acceptData(java.awt.datatransfer.DataFlavor flavor, Object data);
        boolean drop(java.awt.datatransfer.DataFlavor flavor, Object data);
    }
    
    private Actions actions;
    private Widget child;
    private xowl.gmi.view.DropHandler dropHandler;
    private boolean isHovered;
    private boolean accepting;
    
    public xowl.gmi.view.DropHandler onMouseDrop() { return dropHandler; }
    
    public Optional(Actions actions) {
        this.actions = actions;
        this.dropHandler = new xowl.gmi.view.DropHandler() {
            @Override public int getSupportedActions() { return javax.swing.TransferHandler.COPY_OR_MOVE; }
            @Override public boolean acceptFlavor(java.awt.datatransfer.DataFlavor flavor) { return Optional.this.actions.acceptFlavor(flavor); }
            @Override public boolean acceptData(java.awt.datatransfer.DataFlavor flavor, Object data) {
                accepting = Optional.this.actions.acceptData(flavor, data);
                return accepting;
            }
            @Override public boolean drop(java.awt.datatransfer.DataFlavor flavor, Object data, java.awt.Point location) { return Optional.this.actions.drop(flavor, data); }
        };
    }
    
    public void setChild(Widget child) { this.child = child; }
    
    @Override protected void buildStackChildren(List<Widget> stack, Couple input) {
        if (child != null)
            child.buildStackAt(stack, input);
    }
    
    @Override public Couple getMinSize() {
        if (child == null)
            return minSize;
        return child.getMinSize();
    }
    @Override public Couple getMaxSize() {
        if (child == null)
            return MAXSIZE;
        return child.getMaxSize();
    }
    @Override public Couple layout() {
        if (child == null)
            dimension = minSize;
        else {
            dimension = child.layout();
            child.setPosition(new Couple(0, 0));
        }
        return dimension;
    }
    @Override public Couple layout(Couple bounds) {
        if (child == null)
            dimension = bounds;
        else {
            dimension = child.layout(bounds);
            child.setPosition(new Couple(0, 0));
        }
        return dimension;
    }
    @Override public void paint(java.awt.Graphics2D g2d, float alpha, DiagramStyle style, Couple offset) {
        Couple real = position.plus(offset);
        if (child == null)
            paintEmpty(g2d, alpha, style, real);
        else
            child.paint(g2d, alpha * transparency, style, real);
    }
    @Override protected void exportSVG(org.w3c.dom.Document doc, org.w3c.dom.Element parent, float alpha, DiagramStyle style, Couple offset) {
        if (child != null)
            child.exportSVG(doc, parent, alpha * transparency, style, position.plus(offset));
    }
    
    @Override public boolean onMouseEnter(UserState state, Couple local) {
        if (state != UserState.Dragging) return false;
        isHovered = true;
        accepting = false;
        return true;
    }
    @Override public boolean onMouseLeave() {
        if (!isHovered) return false; 
        isHovered = false;
        accepting = false;
        return true;
    }
    
    public void paintEmpty(java.awt.Graphics2D g2d, float alpha, DiagramStyle style, Couple real) {
        float ray = (float)Math.min(dimension.x, dimension.y) / 2;
        float[] fractions = new float[] {0f, 0.5f, 1f};
        java.awt.Color[] colors = new java.awt.Color[] {
            java.awt.Color.WHITE,
            java.awt.Color.WHITE,
            new java.awt.Color(255, 255, 255, 0)
        };
        java.awt.Paint gradient = new java.awt.RadialGradientPaint(real.plus(dimension.mult(0.5)).toAWTPoint(), ray, fractions, colors);
        g2d.setPaint(gradient);
        g2d.fillRect((int)real.x, (int)real.y, (int)dimension.x, (int)dimension.y);
        
        if (isHovered && accepting) {
            colors = new java.awt.Color[] {
                style.dropColor,
                style.dropColor,
                new java.awt.Color(style.dropColor.getRed(), style.dropColor.getGreen(), style.dropColor.getBlue(), 0)
            };
        } else {
            colors = new java.awt.Color[] {
                java.awt.Color.GRAY,
                java.awt.Color.GRAY,
                new java.awt.Color(0, 0, 0, 0)
            };
        }
        gradient = new java.awt.RadialGradientPaint(real.plus(dimension.mult(0.5)).toAWTPoint(), ray, fractions, colors);
        g2d.setPaint(gradient);
        g2d.setStroke(new java.awt.BasicStroke(isHovered?2:1));
        for (double i=5; i<(dimension.x+dimension.y); i+=5) {
            double sx = real.x + i;
            double sy = real.y;
            double ex = real.x;
            double ey = real.y + i;
            if (sx > (real.x + dimension.x)) {
                double offset = i - dimension.x;
                sx -= offset;
                sy += offset;
            }
            if (ey > (real.y + dimension.y)) {
                double offset = i - dimension.y;
                ey -= offset;
                ex += offset;
            }
            g2d.drawLine((int)sx, (int)sy, (int)ex, (int)ey);
        }
    }
}

