// (c) MIT 2003.  All rights reserved.

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// AUTHOR:      Tevfik Metin Sezgin                                           //
//              Massachusetts Institute of Technology                         //
//              Department of Electrical Engineering and Computer Science     //
//              Artificial Intelligence Laboratory                            //
//                                                                            //
// E-MAIL:        mtsezgin@ai.mit.edu, mtsezgin@mit.edu                       //
//                                                                            //
// COPYRIGHT:   Tevfik Metin Sezgin                                           //
//              All rights reserved. This code can not be copied, modified,   //
//              or distributed in whole or partially without the written      //
//              permission of the author. Also see the COPYRIGHT file.        //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////
package edu.mit.sketch.util;

  /**
  *
  * See the end of the file for the log of changes.
  *
  * $Author: moltmans $
  * $Date: 2003/10/13 19:46:38 $
  * $Revision: 1.3 $
  * $Headers$
  * $Id: Gaussian.java,v 1.3 2003/10/13 19:46:38 moltmans Exp $
  * $Name:  $
  * $Locker:  $
  * $Source: /projects/drg/CVSROOT/drg/code/src/edu/mit/sketch/util/Gaussian.java,v $
  *
  **/


/**
  *
  * This class computes and stores the necessary convolution 
  * filters of different sizes for gaussian convlution.
  *
  **/

public
class      Gaussian
{
    /**
     * Gaussian mask
     **/
    public double g[];


    /**
     * sigma
     **/
    public double sigma;


    /**
     * Gaussian mask
     **/
    public int size;


    /**
    *
    * The constructor.
    * size  should be an odd number
    * This method was written using MATLAB's implementation of fspecial(.,.)
    *
    **/
    public Gaussian( int size, double sigma )
    {
        this.size  = size;
        this.sigma = sigma;
        
        int    mid_index          = (size+1)/2;
        double two_sigma_squared = sigma*sigma*2;
        
        g = new double[size];
        for ( int i=0; i<mid_index; i++ ) {
            g[i]            = Math.pow( Math.E, -((mid_index-i-1)*(mid_index-i-1))/two_sigma_squared );
            g[g.length-i-1] = g[i];
        }
        
        double sum = 0.0;
        for ( int i=0; i<g.length; i++ ) {
            sum += g[i];
        }
        
        for ( int i=0; i<g.length; i++ ) {
            g[i] /= sum;
        }
    }


    /**
    *
    * toString
    *
    **/
    public String 
    toString()
    {
        String s = "[ ";
        String e = "             ";
        
        for ( int i=0; i<g.length; i++ ) {
            
            if ( ( ( g[i] + e ).indexOf( 'e' ) == -1 ) &&
                 ( ( g[i] + e ).indexOf( 'E' ) == -1 ) ) {
                s += ( ( g[i] + e ).substring( 0, 5 ) ) + ", ";
            } else {
                s += g[i] + ", ";
            }
        }
        
        return s + " ]";
    }


    /**
    *
    * Returns the convolved version of the input signal
    * This is non-destructive on the input. The input shold
    * be longer than the mask size.
    *
    **/
    public double[]
    convolve( double input[] )
    {
        double result[]   = new double[input.length];
        int    l          = (size-1)/2;

        for ( int i=l; i<input.length-l; i++ ) {
            result[i] = 0.0;
            for ( int j=0; j<size; j++ ) {
                result[i] += g[j]*input[i-l+j];
            }
        }
        
        double partial_sum = 0.0;
        for ( int i=0; i<l; i++ ) {
            partial_sum = 0.0;
            result[i]   = 0.0;
            for ( int j=0; j<size; j++ ) {
                if ( i-l+j >= 0 ) {
                    result[i]   += g[j]*input[i-l+j];
                    partial_sum += g[j];
                }
            }
            result[i] /= partial_sum;
        }
        for ( int i=input.length-l; i<input.length; i++ ) {
            partial_sum = 0.0;
            result[i]   = 0.0;
            for ( int j=0; j<size; j++ ) {
                if ( i-l+j < input.length ) {
                    result[i]   += g[j]*input[i-l+j];
                    partial_sum += g[j];
                }
            }
            result[i] /= partial_sum;
        }

        return result;
    }
}

/**
  *
  * $Log: Gaussian.java,v $
  * Revision 1.3  2003/10/13 19:46:38  moltmans
  * Removed bad line endings.
  *
  * Revision 1.2  2003/03/06 01:08:54  moltmans
  * Added copyright to all the files.
  *
  * Revision 1.1  2001/11/23 03:24:32  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.1.1.1  2001/03/29 16:25:01  moltmans
  * Initial directories for DRG
  *
  * Revision 1.5  2000/09/20 20:07:30  mtsezgin
  * This is a working version with curve recognition and curve
  * refinement. The GeneralPath approximation is refined if needed
  * to result in a better fit.
  *
  * Revision 1.3  2000/09/06 22:40:58  mtsezgin
  * Combinations of curves and polygons are successfully approximated
  * by straight lines and Bezier curves as appropriate. System works
  * quite reliably.
  *
  * Revision 1.18  2000/06/08 03:20:07  mtsezgin
  *
  * Now the user can save the design, and read back the designs that
  * were saved earlied. This is accomplished through Serialization.
  *
  * Revision 1.16  2000/06/02 21:11:16  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.15  2000/05/26 20:43:35  mtsezgin
  *
  * Modified to make use of the ControlsModule that lets the user play
  * with parameters used in the recognition process.
  *
  * Revision 1.13  2000/05/22 02:42:34  mtsezgin
  *
  * The current version enables polygons to be sketched in pieces.
  *
  * Revision 1.12  2000/05/07 17:27:58  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.11  2000/04/28 04:45:05  mtsezgin
  *
  * Now each GeometricObject keeps the mouse input that was previously
  * discarded. User can switch between seeing the recognized mode and
  * the raw mode. setDataPoints( Polygon points ) and getDataPoints()
  * are added to GeometricObject, and all the implementors are modified
  * accordingly.
  *
  * Revision 1.10  2000/04/20 04:29:51  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.8  2000/04/17 07:02:32  mtsezgin
  *
  * Finally made the Rectangle really rotatable.
  *
  * Revision 1.7  2000/04/13 06:24:09  mtsezgin
  *
  * The current version of the program recognized Crosses, and Shades.
  * Implementors of Terminal and their descendants were modified to
  * implement the changes in GeometricObject.
  *
  * Revision 1.6  2000/04/12 04:00:17  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.5  2000/04/11 00:41:48  mtsezgin
  *
  * Now the whole package succesfully parses a motor.
  *
  * Revision 1.4  2000/04/06 19:16:24  mtsezgin
  *
  * Modified all the classes to use my Point class which extends java.awt.Point
  * instead of directly using java.awt.Point
  *
  * Revision 1.3  2000/04/01 20:34:04  mtsezgin
  *
  * Renamed Oval.java to Ellipse.java
  *
  * Revision 1.2  2000/04/01 04:11:32  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.1.1.1  2000/04/01 03:07:07  mtsezgin
  * Imported sources
  *
  * Revision 1.3  2000/03/31 22:41:05  mtsezgin
  *
  * Started Log tracking.
  *
  * Revision 1.2  2000/03/31 22:30:31  mtsezgin
  *
  *
  * Starting the log management.
  *
  *
  **/

