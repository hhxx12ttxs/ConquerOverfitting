/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)Statistics.java 
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

package com.sun.jbi.cam.manager.framework.generic.statistics.charts.analysis;

import java.text.NumberFormat;
import java.util.Arrays;

/**
 * A general-purpose statistics class
 *
 * @author graj
 */
public class Statistics {
    
    /** Creates a new instance of Statistics */
    public Statistics() {
    }
    
    // Return the average set of values
    public static double mean(double[] values) {
        double average = 0.0D;
        for(int index = 0; index < values.length; index++) {
            average += values[index];
        }
        average /= values.length;
        return average;
    }
    
    /**
     * Return the median of the set of values
     */
    public static double median(double[] values) {
        double temp[] = new double[values.length];
        System.arraycopy(values, 0, temp, 0, values.length);
        // sort the data
        Arrays.sort(temp);
        
        // Return the middle value
        if(0 == (values.length % 2)) {
            // if even number of values, find average
            return (temp[(temp.length/2)-1])/2;
        }
        return temp[temp.length/2];
    }
    
    /**
     * Returns the mode of a set of values
     * A NoModeException is thrown if no value
     * occurs more freqiently than any other.
     * If two or more values occur with the same
     * frequency, the first value is returned.
     */
    public static double mode(double[] values) throws NoModeException {
        double modeTemp = 0.0, modeValue = 0.0;
        int count = 0, oldCount = 0;
        
        for(int index = 0; index < values.length; index++) {
            modeTemp = values[index];
            count = 0;
            // Count how many times each value occurs
            for(int inner=index+1; inner < values.length; inner++) {
                if(modeTemp == values[inner]) {
                    count++;
                }
            }
            // If this value occurs more frequently than the
            // previous candidate, save it.
            if(count > oldCount) {
                modeValue = modeTemp;
                oldCount = count;
            }
        }
        if(oldCount == 0) {
            throw new NoModeException();
        }
        return modeValue;
    }
    
    /**
     * Return the standard deviation of a set of values
     */
    public static double standardDeviation(double[] values) {
        double deviation = 0.0D;
        double average = mean(values);
        
        for(int index = 0; index < values.length; index++) {
            deviation += ((values[index] - average) * (values[index] - average));
        }
        deviation /= values.length;
        deviation = Math.sqrt(deviation);
        return deviation;
    }
    
    /**
     * Compute the regression equation and the coefficient
     * of correlation for a set of values. The values
     * represent the Y co-ordinate. The X co-ordinate
     * is time (i.e., ascending increments of 1).
     */
    public static RegressionData regress(double[] values) {
        double aValue = 0.0D, bValue = 0.0D, yAverage = 0.0D,
               xAverage = 0.0D, temp = 0.0D, otherTemp = 0.0D,
               coefficient = 0.0D;
        double[] otherValues = new double[values.length];
        
        // Create number format with two decimal digits
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(2);
        
        // Find mean of Y values
        yAverage = mean(values);
        
        // Find mean of the X component
        xAverage = 0.0D;
        for(int index = 0; index < values.length; index++) {
            xAverage += index;
        }
        xAverage /= values.length;
        
        // Find b value.
        temp = otherTemp = 0.0D;
        for(int index = 0; index < values.length; index++) {
            temp += ((values[index] - yAverage) * (index - xAverage));
            otherTemp += ((index - xAverage) * (index - xAverage));
        }
        
        bValue = temp/otherTemp;
        
        // Find a value.
        aValue = yAverage - (bValue * xAverage);
        
        // Compute the co-efficient of correlation.
        for(int index = 0; index < values.length; index++) {
            otherValues[index] = index + 1;
        }
        coefficient = temp/values.length;
        coefficient /= (standardDeviation(values) * standardDeviation(otherValues));
        
        String equation = "Y = "+format.format(aValue)+ " + "+format.format(bValue)+" * X";
        return new RegressionData(aValue, bValue, coefficient, equation);
    }
}

