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
import edu.mit.sketch.geom.Polygon;
import edu.mit.sketch.geom.Point;


/**
  *
  * See the end of the file for the log of changes.
  *
  * $Author: moltmans $
  * $Date: 2003/10/13 19:46:38 $
  * $Revision: 1.3 $
  * $Headers$
  * $Id: LinearFit.java,v 1.3 2003/10/13 19:46:38 moltmans Exp $
  * $Name:  $
  * $Locker:  $
  * $Source: /projects/drg/CVSROOT/drg/code/src/edu/mit/sketch/util/LinearFit.java,v $
  *
  **/


/**
  *
  * This class has useful functions for linear line matching.
  * Some adapted from numerical recipes book.
  *
  **/


public
class LinearFit
{
    /**
    * Constant corresponding to deriving direction using simple
    * tangents.
    **/
    public static final int SIMPLE_TANGENTS_METHOD = 0;
    

    /**
    * Constant corresponding to deriving direction using 
    * sliding window and the ODR package.
    **/
    public static final int SWODR_METHOD = 1;
    

    /**
    * Constant corresponding to deriving direction using 
    * sliding window and rotation mehod.
    **/
    public static final int ROTATION_METHOD = 2;
    

    /**
    *
    * Do a linear fit. Return a and b in an array. (y = ax+b)
    *
    **/
    public static double[] 
    fit( double x[], double y[] )
    {
        int    i;
        double t;
        double sxoss;
        double ss;
        
        int    ndata = x.length;
        double a     = 0.0;
        double b     = 0.0;
        double sx    = 0.0;
        double sy    = 0.0;
        double st2   = 0.0;
        
        for ( i=0; i<ndata; i++ ) {
            sx += x[i];
            sy += y[i];
        }

        ss    = ndata;
        sxoss = sx/ss;
        
        for ( i=0; i<ndata; i++ ) {
            t    = x[i] - sxoss;
            st2 += t*t;
            a   += t*y[i];
        }

        a /= st2;
        b  = (sy-sx*a)/ss;
        
        double result[] = new double[2];
        
        result[0] = a;
        result[1] = b;
        return result;
    }


    /**
    *
    * Return the angle -- in radians -- that best approximates
    * the slope of the line that best fits x, y data points.
    * Do this for points [start_index, end_index] (including 
    * start and end points.
    *
    **/
    public static double
    findAngle( double x[], 
               double y[], 
               int    start_index, 
               int    end_index, 
               int    partitions )
    {
        int center_x = 0;
        int center_y = 0;
        int int_x[]  = new int[end_index-start_index + 1];
        int int_y[]  = new int[end_index-start_index + 1];
        for ( int i=start_index; i<=end_index; i++ ) {
            center_x += x[i];
            center_y += y[i];
        }
        center_x /= ( end_index-start_index + 1 );
        center_y /= ( end_index-start_index + 1 );
        for ( int i=0; i<int_x.length; i++ ) {
            int_x[i] = (int)(x[start_index+i]-center_x);
            int_y[i] = (int)(y[start_index+i]-center_y);
        }
        
        Polygon polygon = new Polygon( int_x, int_y, int_x.length );
        
        
        double min_angle  = -Math.PI/2;
        double error      = 0.0;
        double min_error  = Double.MAX_VALUE;
        double distance   = 0.0;
        double y_average  = 0.0;

        Polygon rotated_polygon;
        for ( int i = -partitions; i<partitions; i++ ) {
            rotated_polygon = new Polygon( polygon );
            // Go from -PI/2 to  +PI/2
            rotated_polygon.rotate( Math.PI/((double)partitions*2.0)*i );
                            
            error     = 0.0;                                                 
            y_average = 0.0;                                                             
            for ( int j=0; j<rotated_polygon.npoints; j++ ) {
                y_average += rotated_polygon.ypoints[j];
            }
            y_average /= rotated_polygon.npoints;
            for ( int j=0; j<rotated_polygon.npoints; j++ ) {
                distance = y_average - rotated_polygon.ypoints[j];
                error += distance*distance;
            }
            if ( error < min_error ) {
                min_error = error;
                min_angle = -Math.PI/((double)partitions*2.0)*i;
            }
        }

        return min_angle;
    }


    /**
    *
    * Return the angle -- in radians -- that best approximates
    * the slope of the line that best fits x, y data points.
    * Do this for points [start_index, end_index] (including 
    * start and end points.
    *
    **/
    public static double
    findAngle2( double x[], 
               double y[], 
               int    start_index, 
               int    end_index, 
               int    depth )
    {
        int center_x = 0;
        int center_y = 0;
        int int_x[]  = new int[end_index - start_index + 1];
        int int_y[]  = new int[end_index - start_index + 1];
        for ( int i=start_index; i<=end_index; i++ ) {
            center_x += x[i];
            center_y += y[i];
        }
        center_x /= ( int_x.length );
        center_y /= ( int_y.length );
        for ( int i=0; i<int_x.length; i++ ) {
            int_x[i] = (int)(x[start_index+i]-center_x);
            int_y[i] = (int)(y[start_index+i]-center_y);
        }
        
        Polygon polygon   = new Polygon( int_x, int_y, int_x.length );
        
        double positive_turn_y_average = 0.0;
        double negative_turn_y_average = 0.0;
        double y_average               = 0.0;
        double angle                   = 0.0;
        double span                    = Math.PI/2;
  
        Polygon positive_turn = new Polygon( polygon );
        Polygon negative_turn = new Polygon( polygon );
        
        for ( int j=0; j<polygon.npoints; j++ ) {
            y_average += Math.abs( polygon.ypoints[j] );
        }
        for ( int i =0; i<depth; i++ ) {
            positive_turn.copyVerticesFrom( polygon );
            negative_turn.copyVerticesFrom( polygon );
            
            positive_turn.rotateAboutOrigin( span  );
            negative_turn.rotateAboutOrigin( -span );

            positive_turn_y_average = 0.0;
            negative_turn_y_average = 0.0;
            for ( int j=0; j<polygon.npoints; j++ ) {
                positive_turn_y_average += Math.abs( positive_turn.ypoints[j] );
                negative_turn_y_average += Math.abs( negative_turn.ypoints[j] );
            }
            positive_turn_y_average /= polygon.npoints;
            negative_turn_y_average /= polygon.npoints;
            
            if ( positive_turn_y_average < negative_turn_y_average ) {
                if ( y_average < positive_turn_y_average ) {
                } else {
                    y_average = positive_turn_y_average;
                    angle     = angle + span;
                    polygon.copyVerticesFrom( positive_turn );
                }
            } else {
                if ( y_average < negative_turn_y_average ) {
                } else {
                    y_average = negative_turn_y_average;
                    angle     = angle - span;
                    polygon.copyVerticesFrom( negative_turn );
                }
            }
            span /= 2;
        }

        return -angle;
    }


    /**
    *
    * Return the angle -- in radians -- that best approximates
    * the slope of the line that best fits x, y data points.
    * Do this for points [start_index, end_index] (including 
    * start and end points.
    *
    **/
    public static double
    findAngle( double x[], double y[], int start_index, int end_index )
    {
        int center_x = 0;
        int center_y = 0;
        int int_x[]  = new int[end_index-start_index + 1];
        int int_y[]  = new int[end_index-start_index + 1];
        for ( int i=start_index; i<=end_index; i++ ) {
            center_x += x[i];
            center_y += y[i];
        }
        center_x /= ( end_index-start_index + 1 );
        center_y /= ( end_index-start_index + 1 );
        for ( int i=0; i<int_x.length; i++ ) {
            int_x[i] = (int)(x[start_index+i]-center_x);
            int_y[i] = (int)(y[start_index+i]-center_y);
        }
        
        Polygon polygon = new Polygon( int_x, int_y, int_x.length );
        
        
        int    partitions = 180; // # of partitions we divide a quadrant
        double min_angle  = -Math.PI/2;
        double error      = 0.0;
        double min_error  = Double.MAX_VALUE;
        double distance   = 0.0;
        double y_average  = 0.0;

        Polygon rotated_polygon;
        for ( int i = -partitions; i<partitions; i++ ) {
            rotated_polygon = new Polygon( polygon );
            // Go from -PI/2 to  +PI/2
            rotated_polygon.rotate( Math.PI/((double)partitions*2.0)*i );
                            
            error     = 0.0;                                                 
            y_average = 0.0;                                                             
            for ( int j=0; j<rotated_polygon.npoints; j++ ) {
                y_average += rotated_polygon.ypoints[j];
            }
            y_average /= rotated_polygon.npoints;
            for ( int j=0; j<rotated_polygon.npoints; j++ ) {
                distance = y_average - rotated_polygon.ypoints[j];
                error += distance*distance;
            }
            if ( error < min_error ) {
                min_error = error;
                min_angle = -Math.PI/((double)partitions*2.0)*i;
            }
        }

        return min_angle;
    }


    /**
    *
    * Return the angle -- in radians -- that best approximates
    * the slope of the line that best fits the data points.
    *
    **/
    public static double
    findAngle( Point points[] )
    {
        int center_x = 0;
        int center_y = 0;
        int int_x[]  = new int[points.length];
        int int_y[]  = new int[points.length];
        for ( int i=0; i<points.length; i++ ) {
            center_x += points[i].x;
            center_y += points[i].y;
        }
        center_x /= points.length;
        center_y /= points.length;
        for ( int i=0; i<points.length; i++ ) {
            int_x[i] = (int)(points[i].x-center_x);
            int_y[i] = (int)(points[i].y-center_y);
        }
        
        Polygon polygon = new Polygon( int_x, int_y, points.length );
        
        
        int    partitions = 180; // # of partitions we divide a quadrant
        double min_angle  = -Math.PI/2;
        double error      = 0.0;
        double min_error  = Double.MAX_VALUE;
        double distance   = 0.0;
        double y_average  = 0.0;

        Polygon rotated_polygon;
        for ( int i = -partitions; i<partitions; i++ ) {
            rotated_polygon = new Polygon( polygon );
            // Go from -PI/2 to  +PI/2
            rotated_polygon.rotate( Math.PI/((double)partitions*2.0)*i );
                            
            error     = 0.0;                                                 
            y_average = 0.0;                                                             
            for ( int j=0; j<rotated_polygon.npoints; j++ ) {
                y_average += rotated_polygon.ypoints[j];
            }
            y_average /= rotated_polygon.npoints;
            for ( int j=0; j<rotated_polygon.npoints; j++ ) {
                distance = y_average - rotated_polygon.ypoints[j];
                error += distance*distance;
            }
            if ( error < min_error ) {
                min_error = error;
                min_angle = -Math.PI/((double)partitions*2.0)*i;
            }
        }

        return min_angle;
    }


    /**
    *
    * Return the angle -- in radians -- that best approximates
    * the slope of the line that best fits x, y data points.
    *
    **/
    public static double
    findAngle( double x[], double y[] )
    {
        int center_x = 0;
        int center_y = 0;
        int int_x[]  = new int[x.length];
        int int_y[]  = new int[y.length];
        for ( int i=0; i<x.length; i++ ) {
            center_x += x[i];
            center_y += y[i];
        }
        center_x /= x.length;
        center_y /= y.length;
        for ( int i=0; i<x.length; i++ ) {
            int_x[i] = (int)(x[i]-center_x);
            int_y[i] = (int)(y[i]-center_y);
        }
        
        Polygon polygon = new Polygon( int_x, int_y, x.length );
        
        
        int    partitions = 180; // # of partitions we divide a quadrant
        double min_angle  = -Math.PI/2;
        double error      = 0.0;
        double min_error  = Double.MAX_VALUE;
        double distance   = 0.0;
        double y_average  = 0.0;

        Polygon rotated_polygon;
        for ( int i = -partitions; i<partitions; i++ ) {
            rotated_polygon = new Polygon( polygon );
            // Go from -PI/2 to  +PI/2
            rotated_polygon.rotate( Math.PI/((double)partitions*2.0)*i );
                            
            error     = 0.0;                                                 
            y_average = 0.0;                                                             
            for ( int j=0; j<rotated_polygon.npoints; j++ ) {
                y_average += rotated_polygon.ypoints[j];
            }
            y_average /= rotated_polygon.npoints;
            for ( int j=0; j<rotated_polygon.npoints; j++ ) {
                distance = y_average - rotated_polygon.ypoints[j];
                error += distance*distance;
            }
            if ( error < min_error ) {
                min_error = error;
                min_angle = -Math.PI/((double)partitions*2.0)*i;
            }
        }

        return min_angle;
    }
    

    /**
    *
    * Do a linear fit. Return a and b in an array. (y = ax+b)
    *
    **/
    public static double[] 
    fit2( double x[], double y[] )
    {
        double sxx = 0.0;
        double syy = 0.0;
        double sxy = 0.0;
        double xa  = 0.0;
        double ya  = 0.0;
        
        for ( int i=0; i<x.length; i++ ) {
            xa += x[i];
            ya += y[i];
        }
        xa /= x.length;
        ya /= y.length;
        
        for ( int i=0; i<x.length; i++ ) {
            sxx += (x[i]-xa)*(x[i]-xa);
            syy += (y[i]-ya)*(y[i]-ya);
            sxy += (x[i]-xa)*(y[i]-ya);
        }

        double result[] = new double[2];
        
        result[0] = sxy/sxx;
        result[1] = ya-result[0]*xa;
        return result;
    }
    

    /**
    *
    * Do a linear fit. Return then angle that the fitted line makes
    * with the x axis. 
    *
    **/
    public static double
    findAngleViaODR( double x[], double y[] )
    {
        return Math.atan( 
                    (OrthogonalDistanceRegression.doODR( "parameters",
                                                         "result",
                                                         x,
                                                         y ))[0] );
    }
    

    /**
    *
    * Return a String giving info about the fit method.
    *
    **/
    public static String
    fitMethodToString( int fit_method )
    {
        switch( fit_method ) {
            case LinearFit.SIMPLE_TANGENTS_METHOD :
                return "simple tangents method";
                
            case LinearFit.SWODR_METHOD :
                return "sliding window ODR method";
                
            case LinearFit.ROTATION_METHOD :
                return "rotational ODR method";
                
            default :
                return "unknown fit method";
        }
    }
}

