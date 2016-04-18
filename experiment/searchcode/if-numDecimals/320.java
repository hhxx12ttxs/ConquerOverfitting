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
 
    NumericTickLocator.java 
 */

package com.approximatrix.charting.coordsystem.ticklocator;

/** Class for locating tick marks along axes.  Designed for axes with numeric values.
 * 
 * @author armstrong
 *
 */
public class NumericTickLocator {

	/** The threshhold of specified tic mark counts at which the routines
     * default to simple tick mark calculator
     */ 
    private static final int SIMPLE_TICK_THRESHOLD = 4;
    
    /** Count of recursions into the tick mark auto-placement routine at which to give up calculating placement
     */
    private static final int STOP_TICK_RECURSIONS = 80;
	
	private double max = 0.0;
	private double min = 1.0;
	private int maxtics = 40;
	private int destics = 20;
	private boolean simple = false;
	
	private int tick_recursions = 0;
	
	/** Constructs a numeric tick locator based on maximum and minimum values.
	 * 
	 * @param max the largest value to be plotted
	 * @param min the smallest value to be plotted
	 * @param numTicsDesired the approximate desired number of tick marks
	 * @param maxTics the most allowed ticks
	 */
	public NumericTickLocator(double max, double min, int numTicsDesired, int maxTics) {
		this(max,min,numTicsDesired,maxTics,false);
	}
	
	/** Constructs a numeric tick locator based on maximum and minimum values.  Allows
	 * one to specify using a simple locator, spacing ticks equally between max and min
	 * values.  
	 * 
	 * @param max the largest value to be plotted
	 * @param min the smallest value to be plotted
	 * @param numTicsDesired the approximate desired number of tick marks
	 * @param maxTics the most allowed ticks
	 * @param force_simple true to force the locator to create exactly numTicsDesired starting with min and ending with max
	 */
	public NumericTickLocator(double max, double min, int numTicsDesired, int maxTics, boolean force_simple) {
		this.max = max;
		this.min = min;
		this.maxtics = maxTics;
		this.destics = numTicsDesired;
		this.simple = force_simple;
	}
	
	/** Computes and returns the tick mark locations
	 * 
	 * @return an array of doubles
	 */
	public double[] getTickMarkLocations() {
		return computeTicMarkLocations(max,min,destics,maxtics,simple);
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
    	
    	double[] return_val = new double[Math.min(numTicsActual+1,maxTics)];
    	
    	return_val[0] = minAdjusted;
    	for(int i=1;i<return_val.length;i++) {
    		return_val[i] = return_val[i-1]+ticValueIncrement;
    		//System.out.print(i);
    		//System.out.print(": ");
    		//System.out.println(return_val[i]);
    	}
    	//System.out.println();
    	
    	return return_val;
    	
    } 
}

