/**
 * A library to interact with Virtual Worlds such as OpenSim
 * Copyright (C) 2012  Jitendra Chauhan, Email: jitendra.chauhan@gmail.com
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package icc.lut;

import icc .tags.ICCCurveType;

/**
 * A Linear 32 bit SRGB to SRGB lut
 * 
 * @version	1.0
 * @author	Bruce A. Kern
 */
public class LookUpTable32LinearSRGBtoSRGB extends LookUpTable32 {     
    
    /**
     * Factory method for creating the lut.
     *   @param wShadowCutoff size of shadow region
     *   @param dfShadowSlope shadow region parameter
     *   @param ksRGBLinearMaxValue size of lut
     *   @param ksRGB8ScaleAfterExp post shadow region parameter
     *   @param ksRGBExponent post shadow region parameter
     *   @param ksRGB8ReduceAfterEx post shadow region parameter
     * @return the lut
     */
    public static LookUpTable32LinearSRGBtoSRGB createInstance (
                                                                int inMax,
                                                                int outMax,
                                                                double shadowCutoff, 
                                                                double shadowSlope,
                                                                double scaleAfterExp, 
                                                                double exponent, 
                                                                double reduceAfterExp) {
        return new LookUpTable32LinearSRGBtoSRGB
            (inMax, outMax,
             shadowCutoff, shadowSlope,
             scaleAfterExp, exponent, reduceAfterExp); 
    }
    
    /**
     * Construct the lut
     *   @param wShadowCutoff size of shadow region
     *   @param dfShadowSlope shadow region parameter
     *   @param ksRGBLinearMaxValue size of lut
     *   @param ksRGB8ScaleAfterExp post shadow region parameter
     *   @param ksRGBExponent post shadow region parameter
     *   @param ksRGB8ReduceAfterExp post shadow region parameter
     */    
    protected LookUpTable32LinearSRGBtoSRGB 
        (
         int inMax,
         int outMax,
         double shadowCutoff, 
         double shadowSlope,
         double scaleAfterExp, 
         double exponent, 
         double reduceAfterExp) {

        super (inMax+1, outMax);

        int i=-1;
        // Normalization factor for i.
        double normalize = 1.0 / (double)inMax;

        // Generate the final linear-sRGB to non-linear sRGB LUT    

        // calculate where shadow portion of lut ends.
        int cutOff = (int)Math.floor(shadowCutoff*inMax);

        // Scale to account for output
        shadowSlope *= outMax;

        // Our output needs to be centered on zero so we shift it down.
        int shift = (outMax+1)/2;

        for (i = 0; i <= cutOff; i++)
            lut[i] = (int)(Math.floor(shadowSlope*(i*normalize) + 0.5) - shift);

        // Scale values for output.
        scaleAfterExp  *= outMax;
        reduceAfterExp *= outMax;

        // Now calculate the rest
        for (; i <= inMax; i++)
            lut[i] = (int)(Math.floor(scaleAfterExp  * 
                                      Math.pow(i*normalize, exponent) 
                                      - reduceAfterExp + 0.5) - shift);  
    }

    public String toString () {
        StringBuffer rep = new StringBuffer("[LookUpTable32LinearSRGBtoSRGB:");
        return rep.append("]").toString();
    }

    /* end class LookUpTable32LinearSRGBtoSRGB */ }










