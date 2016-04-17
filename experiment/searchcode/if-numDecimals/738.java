/*
    OpenChart2 Java Charting Library and Toolkit
    Copyright (C) 2005-2008 Approximatrix, LLC
    Copyright (C) 2001  Sebastian M�ller
    http://www.approximatrix.com

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 
    ClassicCoordSystemUtilities.java 
    Created on 4. April 2002, 22:28
 */

package com.approximatrix.charting.coordsystem;

import java.awt.font.FontRenderContext;
import java.text.DecimalFormat;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.BasicStroke;
import java.awt.Stroke;

import com.approximatrix.charting.ChartUtilities;
import com.approximatrix.charting.coordsystem.ticklocator.NumericTickLocator;
import com.approximatrix.charting.coordsystem.ticklocator.ObjectTickLocator;
import com.approximatrix.charting.model.ChartDataModel;
import com.approximatrix.charting.model.ChartDataModelConstraints;

/**
 * This class provides some utility functions for a ClassicCoordSystem. They were
 * externalized to make the ClassicCoordSystem class clearer.
 * @author  mueller, armstrong
 * @version 1.0
 */
public class ClassicCoordSystemUtilities {

	/** Internal constant refering to the max value index of a 2-element constraint array */ 
	private static final int MAX = 0;
	
	/** Internal constant refering to the min value index of a 2-element constraint array */
	private static final int MIN = 1;
	
	/** Color used for drawing gridlines */
	private static final Color GRID_COLOR = Color.lightGray;
	
	/** Constant array of floats used to create the default grid
	 * line style.
	 */
	private final static float[] DOT1 = {1.0f};
	
	/** Default and fixed grid line style to use */
    private final static BasicStroke GRID_LINE_STYLE = new BasicStroke(1.0f, 
    												   				   BasicStroke.CAP_BUTT, 
    												   				   BasicStroke.JOIN_MITER,
    												   				   10.0f, DOT1, 0.0f);
	
	
	/** used for the offset on the y axis for the size of a "tick"*/
    protected final int marginOffset = 6;
    
    /** Buffer to move the y axis label off the absolute bounds */
    protected final int leftUnitBuffer = 10;
    
    /** The coordinate system associated with these utilities */
    protected ClassicCoordSystem c;
    
    /** The primary axis constraints */
    protected ChartDataModelConstraints constraints;
    
    /** The secondary axis contraints */
    protected ChartDataModelConstraints constraints2;
    
    /** The model associated with these utilities */
    protected ChartDataModel model;
    
    /** Estimate of the number of x ticks to draw.  Used for
     * evaluating the automatically-generated tick locations.
     */
    private int estimated_x_ticks = 20;
    
    /** Estimates of the number of y ticks to draw.  Used for
     * evaluating the automatically-generated tick locations.
     */
    private int estimated_y_ticks = 20;
    
    /** The default maximum number of ticks to allow the tick position
     * calculator to generate on a single axis.
     */
    private static final int DEFAULT_MAX_ACTUAL_TICKS = 40;
    
    /** The maximum number of x ticks to allow in the x direction
     */
    private int maximum_x_ticks = DEFAULT_MAX_ACTUAL_TICKS;
    
    /** The maximum number of y ticks to allow in the x direction
     */
    private int maximum_y_ticks = DEFAULT_MAX_ACTUAL_TICKS;
    
    /** The threshhold of specified tic mark counts at which the routines
     * default to simple tick mark calculator
     */ 
    private static final int SIMPLE_TICK_THRESHOLD = 4;
    
    /** Used to limit the number of recursions into the tick mark auto-placement routine
     */ 
    private int tick_recursions = 0;
    
    /** Count of recursions into the tick mark auto-placement routine at which to give up calculating placement
     */
    private static final int STOP_TICK_RECURSIONS = 80;
    
    /** Creates a new instance of ClassicCoordSystemUtilities */
    public ClassicCoordSystemUtilities(ClassicCoordSystem coord, 
                                ChartDataModelConstraints constraints, 
                                ChartDataModelConstraints constraints2, 
                                ChartDataModel model) {
        c = coord;
        this.constraints = constraints;
        this.constraints2 = constraints2;
        this.model = model;
    }
    
    /** Computes the left margin. */
    public int computeLeftMargin() {

       double[] xm = this.safeMaxMin(constraints.getMaximumX(), constraints.getMinimumX());
        
       if(xm[MIN]<xm[MAX]) {
        	
        	// The first estimate considers only the width of the data labels (numbers) for
        	// each tick mark
        	int maxlmargin = computeYAxisLabelWidth() + marginOffset + leftUnitBuffer;
        	
        	// Now compute the width of the Y-axis label and see if this is now the largest
        	// necessary margin
        	TextLayout layout = new TextLayout(c.getYAxisUnit(), c.getUnitFont(), 
        			c.getFontRenderContext());
        	maxlmargin = Math.max((int)layout.getBounds().getWidth()+leftUnitBuffer,maxlmargin);
        	
        	// Finally, check if the y-axis is displaced from the left in the first place
        	int another_measure = (int)( xm[MIN]*(c.getBounds().getWidth() - c.getRightMargin())/(xm[MAX]-xm[MIN]) );
        	
        	if(Math.abs(another_measure) > maxlmargin && xm[MIN] < 0) {
        		maxlmargin = marginOffset;
        	}

        	int margin = maxlmargin;
        	
        	margin += 5; // just for good looking

        	// Check if the margin is less than the minimum allowable
        	if(margin < c.MINIMALMARGIN)
        		margin = c.MINIMALMARGIN;
        	
        	return margin;
        	
        } else {
        	return c.MINIMALMARGIN;
        }
        
        
    }
    
    /** Computes the right margin. */
    public int computeRightMargin() {
    	TextLayout layout = new TextLayout(c.getXAxisUnit(), c.getUnitFont(), 
    									   c.getFontRenderContext());
		return Math.max((int)(layout.getBounds().getWidth() + (double)c.ARROWLENGTH / 3) , c.ARROWLENGTH)+5;
    }
    
    /** Computes the top margin. */
    public int computeTopMargin() {
    	TextLayout layout = new TextLayout(c.getYAxisUnit(), c.getUnitFont(), 
    									   c.getFontRenderContext());
    	return Math.max((int)(layout.getBounds().getHeight() + (double)c.ARROWLENGTH / 3 + layout.getDescent()), c.ARROWLENGTH);
    }
    
    /** Computes the bottom margin. */
    public int computeBottomMargin() {
    	//double ymin = constraints.getMinimumValue().doubleValue();
        //double ymax = constraints.getMaximumValue().doubleValue();
	
        double[] ym = this.safeMaxMin(constraints.getMaximumY(), constraints.getMinimumY());
        
        if (ym[MIN] < 0 && ym[MAX] > 0) { // In this case, the X axis will not be at the bottom.
        	return c.MINIMALMARGIN;
        } else {
        	ym[MIN] = Math.abs(ym[MIN]);
        	ym[MAX] = Math.abs(ym[MAX]);

        	TextLayout layout = new TextLayout(c.getXAxisUnit(), c.getUnitFont(), 
        			c.getFontRenderContext());

        	// xaxis label height
        	int maxbmargin = computeXAxisLabelHeight() + marginOffset; // + xaxis title height

        	// unit height
        	maxbmargin = Math.max(maxbmargin, (int) layout.getBounds().getHeight() + marginOffset);

        	int margin = maxbmargin;
        	margin += 10; // just for good looking

        	if(margin < c.MINIMALMARGIN)
        		margin = c.MINIMALMARGIN;

        	return margin;
        } 
    }
    
    /** Computes the maximum height of all x-axis labels. */
    public int computeXAxisLabelHeight() {        
        //double min = constraints.getMinimumColumnValue();
        //double max = constraints.getMaximumColumnValue();
    	double[] xm = this.safeMaxMin(constraints.getMaximumX(), constraints.getMinimumX());
        
        double tick = ChartUtilities.calculateTickSpacing(xm[MIN], xm[MAX]);
        
        int height = 0;
         
        boolean paint = false;
        
        DecimalFormat df = c.getXDecimalFormat();
		FontRenderContext frc = c.getFontRenderContext();
        Font f = c.getTickFont();
        
        for(double d = xm[MIN]; d <= xm[MAX]; d += tick) {
            if(paint) {
                String sb = df.format(d);
                Rectangle2D r = f.getStringBounds(sb, frc);
                
                height = Math.max(height, (int)r.getHeight());                
            }
            paint = !paint;
        }
        
        return height;
    }
    
    /** Computes the maximum width of all y-axis labels. */
    public int computeYAxisLabelWidth() {
        //double min = constraints.getMinimumValue().doubleValue();
        //double max = constraints.getMaximumValue().doubleValue();
    	double[] ym = this.safeMaxMin(constraints.getMaximumY(), constraints.getMinimumY());
        //double tick = ChartUtilities.calculateTickSpacing(ym[MIN], ym[MAX]);
       
    	double[] positions = new NumericTickLocator(ym[MAX],ym[MIN],estimated_y_ticks, maximum_y_ticks).getTickMarkLocations(); 
    		//this.computeTicMarkLocations(ym[MAX],ym[MIN],estimated_y_ticks, maximum_y_ticks);
    	
    	if(positions == null) return 0;
    	if(positions.length == 0) return 0;
    	
        int width = 0;
        
        boolean paint = false;
        
        DecimalFormat df = c.getYDecimalFormat();
		FontRenderContext frc = c.getFontRenderContext();
        Font f = c.getTickFont();
		
        for(int i=0;i<positions.length;i++) {
            if(paint) {
                String sb = df.format(positions[i]);
                Rectangle2D r = f.getStringBounds(sb, frc);
                
                width = Math.max((int)Math.ceil(r.getWidth()), width);
            }
            paint = !paint;
        }
        
        return width;
    }
    
    /** This method is called by paintDefault to paint the ticks on the
     * x-axis for numerical x-axis values.
     * @param g the Graphics2D context to paint in
     */
    public void drawNumericalXAxisTicks(Graphics2D g) {
        AffineTransform at = c.getTransform(CoordSystem.FIRST_YAXIS);
        
        //double min = constraints.getMinimumColumnValue();
        //double max = constraints.getMaximumColumnValue();
        double ypt = 0;
        double[] xm = new double[2];
        double[] ym = new double[2];
        try {
        	xm = this.safeMaxMin(constraints.getMaximumX().doubleValue(), constraints.getMinimumX().doubleValue());
        } catch(NullPointerException npe) {
        	xm[0] = 0.0; xm[1] = 1.0;
        }
        try {
        	ym = this.safeMaxMin(constraints.getMaximumY().doubleValue(), constraints.getMinimumY().doubleValue());
        } catch(NullPointerException npe) {
        	ym[0] = 0.0; ym[1] = 1.0;
        }
        	
        if(ym[MIN] > 0)
            ypt = ym[MIN];
        else if(ym[MAX] < 0)
            ypt = ym[MAX];
        
        Point2D p = new Point2D.Double(0.0, 0.0);
        Point2D v;
        Line2D ticks = new Line2D.Double(0.0, 0.0, 0.0, 0.0);
        
		DecimalFormat df = c.getXDecimalFormat();
		FontRenderContext frc = c.getFontRenderContext();
        Font f = c.getTickFont();
        
		//boolean paint = false;
		boolean paint = true;
		
        g.setFont(f);
        boolean paintLabels = c.isPaintLabels();

        double[] positions = new NumericTickLocator(xm[MAX],xm[MIN],estimated_x_ticks, maximum_x_ticks).getTickMarkLocations();
        	//this.computeTicMarkLocations(xm[MAX],xm[MIN],estimated_x_ticks, maximum_x_ticks);
        if(positions == null) return;
        for(int i=0;i<positions.length;i++) {
        	
        	if(positions[i] < xm[MIN] || positions[i] > xm[MAX]) continue;
        	
        	p.setLocation(positions[i], ypt);
        	v = at.transform(p, null);
        	ticks.setLine(v.getX(), v.getY() - marginOffset/2, v.getX(), v.getY() + marginOffset/2);
        	
        	g.draw(ticks);
        	if(paint && paintLabels) {
        		String sb = df.format(positions[i]);
        		Rectangle2D r = f.getStringBounds(sb, frc);
              
        		g.drawString(sb, (float)(v.getX() - r.getWidth() / 2),
        					(float)(v.getY() + r.getHeight() + marginOffset));
        	}
        	
        }
    }
    
    /** This method is called by paintDefault to paint the ticks on the
     * x-axis for non-numerical x-axis values..
     * @param g the Graphics2D context to paint in
     */
    public void drawXAxisTicks(Graphics2D g) {
        AffineTransform at = c.getTransform(CoordSystem.FIRST_YAXIS);
        
        //int min = (int)constraints.getMinimumColumnValue();
        //int max = (int)constraints.getMaximumColumnValue();
        double[] xmd = this.safeMaxMin(constraints.getMaximumX().doubleValue(), constraints.getMinimumX().doubleValue());
        int[] xm = new int[2];
        xm[MIN] = (int)Math.floor(xmd[MIN]);
        xm[MAX] = (int)Math.ceil(xmd[MAX]);

        double ypt = 0;
        
        double[] ym = this.safeMaxMin(constraints.getMaximumY().doubleValue(), constraints.getMinimumY().doubleValue());
        if(ym[MIN] > 0)
            ypt = ym[MIN];
        else if(ym[MAX] < 0)
            ypt = ym[MAX];
        
        Point2D p = new Point2D.Double(0.0, 0.0);
        Point2D v = null;
        Point2D oldv = null;
        
        Line2D ticks = new Line2D.Double(0.0, 0.0, 0.0, 0.0);
        
		FontRenderContext frc = c.getFontRenderContext();
        Font f = c.getTickFont();
        
        boolean paintLabels = c.isPaintLabels();
        g.setFont(f);
        
        int[] ticpoints = new ObjectTickLocator(xm[MIN],xm[MAX],getMaximumXTicks()).getTickMarkLocations();
        if(ticpoints == null)
        	return;
        
        //for(int i = xm[MIN] - 1; i < xm[MAX]; i++) {
        for(int li = 0; li < ticpoints.length; li++) {
        	int i = ticpoints[li] - 1;
            p.setLocation(i + 1, ypt);
            
            v = at.transform(p, null);
            
            ticks.setLine(v.getX(), v.getY() - marginOffset/2, v.getX(), v.getY() + marginOffset/2);
            
            if(i + 1 < xm[MAX])
                g.draw(ticks);
            
            if(!c.isCenterLabelsBetweenTicks() && paintLabels && model.getColumnValueAt(i) != null) {
            	String sb = model.getColumnValueAt(i).toString();
            	Rectangle2D r = f.getStringBounds(sb, frc);

            	g.drawString(sb, (float)(v.getX() - r.getWidth() / 2),
            			(float)(v.getY() + r.getHeight() + marginOffset));
            }
            
            // Draw Strings between ticks
            if(oldv != null && paintLabels && c.isCenterLabelsBetweenTicks()) {
            	if(model.getColumnValueAt(i) != null) {
            		String sb = model.getColumnValueAt(i).toString();
            		Rectangle2D r = f.getStringBounds(sb, frc);
            		
            		g.drawString(sb, (float)(oldv.getX()+(v.getX() - oldv.getX()) / 2 - r.getWidth() / 2),
            					(float)(v.getY() + r.getHeight() + marginOffset));
            	}
            }
            
            oldv = v;
        }
    }
    
    /** This method is called by paintDefault to paint the ticks on the
     * y-axis.
     * @param g the Graphics2D context in which to draw
     */
    public void drawYAxisTicks(Graphics2D g) {
        AffineTransform at = c.getTransform(CoordSystem.FIRST_YAXIS);
        
        //double min = constraints.getMinimumValue().doubleValue();
        //double max = constraints.getMaximumValue().doubleValue();
        double[] ym = this.safeMaxMin(constraints.getMaximumY(), constraints.getMinimumY());
        
        //double tick = ChartUtilities.calculateTickSpacing(min, max);
        double xpt = 0;
       
        // shift the y-axis according to the max and min x-values
        double[] xm = this.safeMaxMin(constraints.getMaximumX(), constraints.getMinimumX());
        if(xm[MIN] > 0)
            xpt = xm[MIN];
        else if(xm[MAX] < 0 && c.getSecondYAxis() != null)
            xpt = xm[MAX];
        
        Point2D p = new Point2D.Double(0.0, 0.0);
        Point2D v;
        Line2D ticks = new Line2D.Double(0.0, 0.0, 0.0, 0.0);
        //boolean paint = false;
        boolean paint = true;
        
        DecimalFormat df = c.getYDecimalFormat();
		FontRenderContext frc = c.getFontRenderContext();
        Font f = c.getTickFont();
		
		Color backupColor = g.getColor();
        g.setFont(f);
        boolean paintLabels = c.isPaintLabels();
        //System.out.println("Yaxis");
        double[] positions = new NumericTickLocator(ym[MAX],ym[MIN],estimated_y_ticks, maximum_y_ticks).getTickMarkLocations();
        	//this.computeTicMarkLocations(ym[MAX],ym[MIN],estimated_y_ticks, maximum_y_ticks);
        if(positions == null) return;
        for(int i=0;i<positions.length;i++) {
        	
        	if(positions[i] < ym[MIN] || positions[i] > ym[MAX]) continue;
        	
        	p.setLocation(xpt,positions[i]);
        	v = at.transform(p, null);
        	
        	ticks.setLine(v.getX() - marginOffset/2, v.getY(), v.getX() + marginOffset/2, v.getY());
        	g.draw(ticks);
        	
        	if (positions[i] != ym[MIN] && !c.isPaintOnlyTick()) {
        		Line2D xax = getXAxisLine2D();
        		ticks.setLine(v.getX() + marginOffset/2, v.getY(), xax.getX2(), v.getY());
        		g.setColor(Color.lightGray);
        		g.draw(ticks);
              	g.setColor(backupColor);
        	}
        	if(paintLabels && (paint || !c.isPaintAltTick())) {
        		String sb = df.format(positions[i]);
        		Rectangle2D r = f.getStringBounds(sb, frc);
        		
        		g.drawString(sb, (float)(v.getX() - r.getWidth() - marginOffset),
        				(float)(v.getY() + r.getHeight() / 2));
        	}
        	//paint = !paint;
        }
    }
    
    
    /** Draws a grid based on values in the y-axis */
    public void drawYAxisGrid(Graphics2D g) {
    	Point2D[] points = this.getYTickLocations();
    	AffineTransform at = c.getTransform(CoordSystem.FIRST_YAXIS);
    	
    	double xpt_last = 0;
    	double xpt_first = 0;
    	
    	if(points == null) return;
    	
    	Color backupColor = g.getColor();
    	Stroke backupStroke = g.getStroke();
    	g.setStroke(GRID_LINE_STYLE);
    	g.setColor(GRID_COLOR);
    	
    	// Get the far side coordinates 
    	double[] xm = this.safeMaxMin(constraints.getMaximumX(), constraints.getMinimumX());
    	xpt_last = xm[MAX];
    	xpt_first = xm[MIN];
    	
        //if(constraints.getMinimumColumnValue() > 0)
        //    xpt = constraints.getMinimumColumnValue();
        //else if(constraints.getMaximumColumnValue() < 0 && c.getSecondYAxis() != null)
        double[] ym = this.safeMaxMin(constraints.getMaximumY(), constraints.getMinimumY());
            
        for(int i=0;i<points.length;i++) {
        	
        	if(points[i].getY() < ym[MIN] || 
        	   points[i].getY() > ym[MAX]) continue;
        	
        	Point2D first_point = new Point2D.Double(xpt_first,points[i].getY());
        	Point2D last_point = new Point2D.Double(xpt_last,points[i].getY());
        	
        	first_point = at.transform(first_point, null);
        	last_point = at.transform(last_point, null);
        	
        	Line2D grid_line = new Line2D.Double(first_point,last_point);

            g.draw(grid_line);
        }
        
        g.setStroke(backupStroke);
        g.setColor(backupColor);    	
    }
    
    /** Draws a grid based on values in the x-axis */
    public void drawNumericalXAxisGrid(Graphics2D g) {
    	Point2D[] points = this.getXTickLocations();
    	AffineTransform at = c.getTransform(CoordSystem.FIRST_YAXIS);
    	
    	double ypt_last = 0;
    	double ypt_first = 0;
    	
    	if(points == null) return;
    	
    	Color backupColor = g.getColor();
    	Stroke backupStroke = g.getStroke();
    	g.setStroke(GRID_LINE_STYLE);
    	g.setColor(GRID_COLOR);
    	
    	// Get the far side coordinates 
        double[] ym = this.safeMaxMin(constraints.getMaximumY(), constraints.getMinimumY());
        ypt_last = ym[MAX];
    	ypt_first = ym[MIN];
    	
        //if(constraints.getMinimumColumnValue() > 0)
        //    xpt = constraints.getMinimumColumnValue();
        //else if(constraints.getMaximumColumnValue() < 0 && c.getSecondYAxis() != null)
    	double[] xm = this.safeMaxMin(constraints.getMaximumX(), constraints.getMinimumX());
    	    
        for(int i=0;i<points.length;i++) {
        	
        	if(points[i].getX() < xm[MIN] || 
               points[i].getX() > xm[MAX]) continue;
        	
        	Point2D first_point = new Point2D.Double(points[i].getX(),ypt_first);
        	Point2D last_point = new Point2D.Double(points[i].getX(),ypt_last);
        	
        	first_point = at.transform(first_point, null);
        	last_point = at.transform(last_point, null);
        	
        	Line2D grid_line = new Line2D.Double(first_point,last_point);
        	
        	g.draw(grid_line);
        }
        
        g.setStroke(backupStroke);
        g.setColor(backupColor);      	
    }
    
    /** Returns the location of tick marks on the y-axis without any transform for plotting */
    private Point2D[] getYTickLocations() {
    	
    	Point2D[] return_val = null;
        
        //double min = constraints.getMinimumValue().doubleValue();
        //double max = constraints.getMaximumValue().doubleValue();
    	double[] ym = this.safeMaxMin(constraints.getMaximumY(), constraints.getMinimumY());
        
        double xpt = 0;
        
        // Gets the axis coordinates
        double[] xm = this.safeMaxMin(constraints.getMaximumX(), constraints.getMinimumX());
        if(xm[MIN] > 0)
            xpt = xm[MIN];
        else if(xm[MAX] < 0 && c.getSecondYAxis() != null)
            xpt = xm[MAX];
        
        // Size the return value
        //System.out.println("Yaxis");
        double[] positions = new NumericTickLocator(ym[MAX],ym[MIN],estimated_y_ticks, maximum_y_ticks).getTickMarkLocations(); 
        	//computeTicMarkLocations(ym[MAX],ym[MIN],estimated_y_ticks, maximum_y_ticks);
        if(positions == null) return null;
        return_val = new Point2D[positions.length];
        for(int i=0;i<positions.length;i++) return_val[i] = new Point2D.Double(xpt,positions[i]);
        
        return return_val;
    }
    
    /** Returns the location of tick marks on the x-axis without any transform for plotting */
    private Point2D[] getXTickLocations() {
    	
    	Point2D[] return_val = null;
        
        //double min = constraints.getMinimumColumnValue();
        //double max = constraints.getMaximumColumnValue();
    	double[] xm = this.safeMaxMin(constraints.getMaximumX(), constraints.getMinimumX());
        
        double ypt = 0;
        
        double[] ym = this.safeMaxMin(constraints.getMaximumY(), constraints.getMinimumY());
        if(ym[MIN] > 0)
            ypt = ym[MIN];
        else if(ym[MAX] < 0)
            ypt = ym[MAX];
        
        // Size the return value
        //System.out.println("Xaxis");
        double[] positions = new NumericTickLocator(xm[MAX],xm[MIN],estimated_x_ticks,maximum_x_ticks).getTickMarkLocations(); 
        	// computeTicMarkLocations(xm[MAX],xm[MIN],estimated_x_ticks,maximum_x_ticks);
        if(positions == null) return null;
        return_val = new Point2D[positions.length];
        for(int i=0;i<positions.length;i++) return_val[i] = new Point2D.Double(positions[i],ypt);
        
        return return_val;
    }
    
    
    /** Computes the Line2D object of the x-axis using the DataConstraints.*/
    public Line2D getXAxisLine2D() {
        double ypt = 0.0;
        // shift the x-axis according to the max and min y-values
        double[] ym = this.safeMaxMin(constraints.getMaximumY(), constraints.getMinimumY());
        if(ym[MIN] > 0)
            ypt = ym[MIN];
        else if(ym[MAX] < 0)
            ypt = ym[MAX];
        
        AffineTransform at = c.getTransform(CoordSystem.FIRST_YAXIS);
        double[] xm = this.safeMaxMin(constraints.getMaximumX(), constraints.getMinimumX());
        
        Point2D l = at.transform(new Point2D.Double(xm[MIN], ypt), null);
        Point2D r = at.transform(new Point2D.Double(xm[MAX], ypt), null);
        
        return new Line2D.Double(l, r);
    }
    
    /** Computes the Line2D object of the y-axis using the DataConstraints.*/
    public Line2D getYAxisLine2D() {
        double xpt = 0.0;
        
        // shift the y-axis according to the max and min x-values
        double[] xm = this.safeMaxMin(constraints.getMaximumX(), constraints.getMinimumX());
        if(xm[MIN] > 0)
            xpt = xm[MIN];
        else if(xm[MAX] < 0 && c.getSecondYAxis() != null)
            xpt = xm[MAX];
        
        AffineTransform at = c.getTransform(CoordSystem.FIRST_YAXIS);
        double[] ym = this.safeMaxMin(constraints.getMaximumY(), constraints.getMinimumY());
        
        Point2D o = at.transform(new Point2D.Double(xpt, ym[MAX]), null);
        Point2D u = at.transform(new Point2D.Double(xpt, ym[MIN]), null);
        //System.out.println("** Y-Axis ("+o+", "+u+")");
        return new Line2D.Double(o, u);
    }
    
    /** Computes the Line2D object of the second y-axis using the DataConstraints.*/
    public Line2D getSecondYAxisLine2D() {
    	double[] xm = this.safeMaxMin(constraints.getMaximumX(), constraints.getMinimumX());
    	double xpt = xm[MAX];
        
        AffineTransform at = c.getTransform(CoordSystem.SECOND_YAXIS);
        double[] ym = this.safeMaxMin(constraints.getMaximumY(), constraints.getMinimumY());
        
        Point2D o = at.transform(new Point2D.Double(xpt, ym[MAX]), null);
        Point2D u = at.transform(new Point2D.Double(xpt, ym[MIN]), null);
        
        return new Line2D.Double(o, u);
    }

    /** Simple wrapper that calls the other computeTicMarkLocations routine.  This wrapper does not require
     * the specification of the force_simple flag.  This routine may become deprecated in the future if
     * the option to always force simple tic mark calculation is added.
     * 
     * @param max the largest value to be plotted
     * @param min the smallest value to be plotted
     * @param numTicsDesired the approximate desired number of tics (the result may not be this number of tics)
     * @param maxTics the absolute maximum number of tics to draw
     * @return positions of tick marks along the axis
     * 
     * @deprecated Deprecated in version 1.4 - should use Tick Locator classes
     */
    private double[] computeTicMarkLocations(double max, double min, int numTicsDesired, int maxTics) {
    	return computeTicMarkLocations(max,min,numTicsDesired,maxTics,false);
    }
    
    /** Simple wrapper routine that calls the appropriate tic mark locator routine.  The algorithm used
     * depends on the desired number of tick marks to draw on the chart.  If the requested number of
     * tics than 5, the library will default to drawing using the simpleTic routine (which does not
     * attempt to generate rounded numerical values).  Any other values calls the elaborate tic
     * calculator performAutoScale.
     *  
     * @param max the largest value to be plotted
     * @param min the smallest value to be plotted
     * @param numTicsDesired the approximate desired number of tics (the result may not be this number of tics)
     * @param maxTics the absolute maximum number of tics to draw
     * @param force_simple force the use of the simple algorithm for drawing tic marks
     * @return positions of tick marks along the axis
     * 
     * @deprecated Deprecated in version 1.4 - should use Tick Locator classes
     */
    private double[] computeTicMarkLocations(double max, double min, int numTicsDesired, int maxTics, boolean force_simple) {	
    	
    	// Reset the recursion counter
    	tick_recursions = 0;
    	
    	if(numTicsDesired == 0)
    		return computeTicMarkLocations(max,min,2,maxTics,true);
    	else if(numTicsDesired <= SIMPLE_TICK_THRESHOLD || force_simple)
    		return simpleTics(max,min,numTicsDesired);
    	else
    		return performAutoScale(max,min,numTicsDesired,maxTics);

    }
    
    /** Foolproof and simple method for determining Tic placement in cases where the tic count is
     * low theoretically.  Simply creates equidistant tic marks based on the max, min and the number
     * of tics desired inputs.  No consideration is given to how nice the tic marks will look; the
     * routine simply partitions the tic marks equally based on the span.
     *  
     * @param max the largest value to be plotted
     * @param min the smallest value to be plotted
     * @param numTicsDesired the exact number of tics desired
     * @return unrounded positions of tick marks along the axis
     * 
     * * @deprecated Deprecated in version 1.4 - should use Tick Locator classes
     */
    private double[] simpleTics(double max, double min, int numTicsDesired) {
    	if(numTicsDesired == 0) return null;
    	double increment = (max-min)/((double)(numTicsDesired-1));
    	double[] return_val = new double[numTicsDesired];
    	
    	return_val[0] = min;
    	for(int i=1;i<numTicsDesired;i++)
    		return_val[i] = return_val[i-1] + increment;
    	
    	return return_val;
    }
    
    /** Computes the tick mark locations on an axis
     * 
     * @param max the largest value in a data set
     * @param min the smallest value in a data set
     * @param numTicsDesired the first guess at the number of desired tick marks
     * @return positions of tick marks along the axis
     * 
     * * @deprecated Deprecated in version 1.4 - should use Tick Locator classes
     */
    private double[] performAutoScale(double max, double min, int numTicsDesired, int maxTics) {
    	
    	// For some wiggle room on the first pass, make sure the desired number
    	// of tics is at least 10
    	int internalDesired = numTicsDesired;
    	if(numTicsDesired < 10 && tick_recursions == 0) {
    		internalDesired = 10;
    	}
    	
    	double d = (max - min) / internalDesired;
    	double ld = Math.log(d) / Math.log(10.0);
    	
    	// Original code
    	//int ild = (int) Math.floor(ld);
    	
    	int ild = (int) Math.round(ld);
    	
    	// Axis debug output
    	//System.out.println(max);
    	//System.out.println(min);
    	//System.out.println(numTicsDesired);
    	
    	//System.out.println(d);
    	//System.out.println(ld);
    	//System.out.println(ild);

    	// Inrement our recursion count
    	tick_recursions++;
    	
    	// Determine the number of decimal places to show 
    	int numDecimals = 0;
    	if (ild < 0)
    		numDecimals = -ild;
    	
    	double fld = Math.pow(10.0, ld - (double) ild);
    	double ticValueIncrement = Math.pow(10.0, (double) ild);
    	
    	//System.out.println(ticValueIncrement);
    	
    	if (fld > 5.0) {
    		ticValueIncrement *= 10.0;
    		numDecimals--;
    		if (numDecimals < 0)
    			numDecimals = 0;
    	} else
    		if (fld > 2.0)
    			ticValueIncrement = 5.0;
    		else
    			if (fld > 1.0)
    				ticValueIncrement = 2.0;
    	
    	double minAdjusted = Math.floor(min / ticValueIncrement) * ticValueIncrement;
    	double maxAdjusted = Math.floor(max / ticValueIncrement + 0.99999) * ticValueIncrement;
    	int numTicsActual = (int) Math.floor((maxAdjusted - minAdjusted) / ticValueIncrement + 1.0e-5);
    	
    	// If simply dividing the increment by two fixes things, do it.
    	if(numTicsActual > maxTics && numTicsActual/2 <= maxTics) {
     			numTicsActual = numTicsActual/2;
     			ticValueIncrement = ticValueIncrement*2.0;
     	}
    	
    	// Check for the unsolvable case here...
    	if((numTicsActual > maxTics && maxTics < 5) || 
    	   (tick_recursions > Math.min(STOP_TICK_RECURSIONS,numTicsDesired-2))) {
    		return simpleTics(max,min,numTicsDesired);
    	} else {
    	
    		// Try again if we've exceeded the maximum number of tics
    		if(numTicsActual > maxTics) {
    			return performAutoScale(max,min,numTicsDesired-1,maxTics);
    		}

    		if(numTicsActual < numTicsDesired/2) {
    			numTicsActual = numTicsActual*2;
    			ticValueIncrement = ticValueIncrement/2.0;
    		}

    		if(ticValueIncrement == 0) {
    			numTicsActual = 3;
    			minAdjusted = min;
    			ticValueIncrement = (max-min)/2.0;
    		}
    		
    	}

    	//if(numTicsActual == 1)
    		//return performAutoScale(max,min,2*numTicsDesired);
    	
    	double[] return_val = new double[numTicsActual+1];
    	
    	return_val[0] = minAdjusted;
    	for(int i=1;i<=numTicsActual;i++) {
    		return_val[i] = return_val[i-1]+ticValueIncrement;
    		//System.out.print(i);
    		//System.out.print(": ");
    		//System.out.println(return_val[i]);
    	}
    	//System.out.println();
    	
    	return return_val;
    	
    } 
    
    /** Sets the estimated number of X ticks (not the actual - handled by autoscaler)
     * 
     * @param number the number of xticks to draw (estimated)
     */
    public void setEstimatedXTicks(int number) {
    	//if(number > 0) 
    	estimated_x_ticks = number;
    	checkMaxTickConsistency();
    }
    
    /** Sets the estimated number of Y ticks (not the actual - handled by autoscaler)
     * 
     * @param number the number of yticks to draw (estimated)
     */
    public void setEstimatedYTicks(int number) {
    	//if(number > 0) 
    	estimated_y_ticks = number;
    	checkMaxTickConsistency();
    }
    
    /** Returns the number of estimated X ticks to draw on the graph (not the actual)
     * 
     * @return the estimated number of ticks to be drawn
     */
    public int getEstimatedXTicks() {
    	return estimated_x_ticks;
    }
    
    /** Returns the number of estimated Y ticks to draw on the graph (not the actual)
     * 
     * @return the estimated number of ticks to be drawn
     */
    public int getEstimatedYTicks() {
    	return estimated_x_ticks;
    }
    
    /** Sets the maximum number of ticks along the X axis that will be allowed. 
     * 
     * @param value the maximum number of X ticks, or -1 to reset to default
     */
    /**
     * @param value
     */
    public void setMaximumXTicks(int value) {
    	if(value >= 0)
    		maximum_x_ticks = value;
    	else
    		maximum_x_ticks = DEFAULT_MAX_ACTUAL_TICKS;
    	
    	checkMaxTickConsistency();
    }
    
    /** Sets the maximum number of ticks along the Y axis that will be allowed. 
     * 
     * @param value the maximum number of Y ticks, or -1 to reset to default
     */
    public void setMaximumYTicks(int value) {
    	if(value >= 0)
    		maximum_y_ticks = value;
    	else
    		maximum_y_ticks = DEFAULT_MAX_ACTUAL_TICKS;
    	
    	checkMaxTickConsistency();
    }
    
    /** Returns the maximum number of X ticks to draw on the X axis
     * 
     * @return the maximum number of X ticks to draw
     */ 
    public int getMaximumXTicks() {
    	return maximum_x_ticks;
    }
 
    /** Returns the maximum number of Y ticks to draw on the Y axis
     * 
     * @return the maximum number of Y ticks to draw
     */ 
    public int getMaximumYTicks() {
    	return maximum_y_ticks;
    }
    
    /** Makes sure that the estimated tic mark count is always limited by
     * the maximum tic mark count limit.  Should be called every time the
     * setEstimated?Ticks or setMaximum?Ticks methods are run.
     *
     */
    private void checkMaxTickConsistency() {
    	if(maximum_x_ticks < estimated_x_ticks)
    		this.setEstimatedXTicks(maximum_x_ticks);
    	if(maximum_y_ticks < estimated_y_ticks)
    		this.setEstimatedYTicks(maximum_y_ticks);
    }
    
    /** Returns a safe estimate of min and max drawing values for
     * an axis.  Used to defend against special cases where max and
     * min might be equal or reversed.
     * 
     * @param max the original max value
     * @param min the originial min value
     * @return the new max and min values in an array, which can be indexed using the MAX and MIN constants in this class
     */
    private double[] safeMaxMin(Number max, Number min) {
    	try {
    		return safeMaxMin(max.doubleValue(),min.doubleValue()); 
    	} catch(NullPointerException npe) {
    		double[] ret = new double[2];
    		ret[0] = 0.0; ret[1] = 1.0;
    		return ret;
    	}
    }
    
    /** Returns a safe estimate of min and max drawing values for
     * an axis.  Used to defend against special cases where max and
     * min might be equal or reversed.
     * 
     * @param max the original max value
     * @param min the originial min value
     * @return the new max and min values in an array, which can be indexed using the MAX and MIN constants in this class
     */
    private double[] safeMaxMin(double max, double min) {
    	double[] maxmin = new double[2];
    	if(max-min == 0.0) {
    		if(max == 0.0) {
    			max = 1.0;
    			min = -1.0;
    		} else {
    			max = 1.1*max;
    			min = 0.9*min;
    		}
    	}
    	if(max < min) {
    		double hold = max;
    		max = min;
    		min = hold;
    	}
    	maxmin[MAX] = max;
    	maxmin[MIN] = min;
    	return maxmin;
    }
    
}

