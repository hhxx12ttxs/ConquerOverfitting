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
package edu.mit.sketch.geom;


/** 
  * 
  * See the end of the file for the log of changes.
  * 
  * $Author: moltmans $
  * $Date: 2003/03/06 01:08:49 $   
  * $Revision: 1.3 $
  * $Headers$
  * $Id: GeometryUtil.java,v 1.3 2003/03/06 01:08:49 moltmans Exp $     
  * $Name:  $   
  * $Locker:  $
  * $Source: /projects/drg/CVSROOT/drg/code/src/edu/mit/sketch/geom/GeometryUtil.java,v $
  *  
  **/



/**
  *
  * Implements geometry related utility functions
  *
  **/
public class
GeometryUtil
{
    /**
    *
     * return a double array which is the same length as the input
     * array, except the array entries are rounded up or down to 
     * be integral multiples of dx.
     *
     **/
    public static double[]
    roundAngles( double angles[], double dx )
    {
        double result[] = new double[angles.length];
        
        for ( int i=0; i<angles.length; i++ ) {
            if ( Math.abs( angles[i] - ((int) (angles[i]/dx)   )*dx ) <
                 Math.abs( angles[i] - (((int)(angles[i]/dx))+1)*dx ) ) {
                result[i] = ((int)(angles[i]/dx))*dx;
            } else {
                result[i] = (((int)(angles[i]/dx))+1)*dx;
            }
        }
        
        return result;
    }
    
    
    /**
     * Return true if |a-b| < tolerance
     **/
    public static boolean 
    equalDoubles( double a, double b, double tolerance )
    {
        return Math.abs(a-b) < tolerance;
    }
    
    
    /**
     * 
     * Return true if two lines are roughly parallel
     * Tolerance is the tolerance in the difference of slopes.
     * 
     **/
    public static boolean 
    linesParallel( Line a, Line b, double t )
    {
        double angle_a = Math.atan2( a.y2-a.y1, a.x2-a.x1 );
        double angle_b = Math.atan2( b.y2-b.y1, b.x2-b.x1 );
        return parallelAngles( angle_a, angle_b, t );
    }
    
    
    /**
     * 
     * Return true if two lines with angles a and b are roughly
     * parallel within the tolerance t.
     * 
     **/
    public static boolean 
    parallelAngles( double angle_a, double angle_b, double t )
    {
        System.out.println( "GeometryUtil : parallelAngles()" );
        System.out.println( "angle_a : "  + radian2degree( angle_a ) + 
                            " angle_b : " + radian2degree( angle_b ) );
        
        return ( equalDoubles( angle_a, angle_b, t )                     ||
                 equalDoubles( angle_a+Math.PI, angle_b, t )             ||
                 equalDoubles( angle_a+Math.PI*2, angle_b, t )           ||
                 equalDoubles( angle_a, angle_b+Math.PI, t )             ||
                 equalDoubles( angle_a, angle_b+Math.PI*2, t )           ||
                 equalDoubles( angle_a+Math.PI, angle_b+Math.PI, t )     ||
                 equalDoubles( angle_a+Math.PI, angle_b+Math.PI*2, t )   ||
                 equalDoubles( angle_a+Math.PI*2, angle_b+Math.PI, t )   ||
                 equalDoubles( angle_a+Math.PI*2, angle_b+Math.PI*2, t ) ); 
    }
    
    
    /**
     * radian2degree
     **/
    public static double 
    radian2degree( double angle )
    {
        return angle/Math.PI*180;
    }
    
    
    /**
     * degree2radian
     **/
    public static double 
    degree2radian( double angle )
    {
        return angle*Math.PI/180;
    }
    
    
    /**
     * Return distnace |p1-p2|
     **/
    public static double 
    distance( Point p1, Point p2 )
    {
        return p1.distance( p2 );
    }
    
    
    /**
     * Return angle between |AB| and |BC| 
     **/
    public static double 
    cosTheoremAngle( Point a, Point b, Point c )
    {
        double d1 = distance( a, b );
        double d2 = distance( b, c );
        double d3 = distance( a, c );

    
        return Math.acos( (d1*d1 + d2*d2 - d3*d3)/(2*d1*d2) );
    }
    
    /**
     * A different version of ptSegDist
     **/
    public static double 
    ptSegDist( Point p1, Point p2, Point p )
    {
        return Math.sqrt( ptSegDistSq( p1.x, p1.y, p2.x, p2.y, p.x, p.y ) );
    }
    
    
    /**
     * A different version of ptSegDistSq
     **/
    public static double 
    ptSegDistSq( Point p1, Point p2, Point p )
    {
        return ptSegDistSq( p1.x, p1.y, p2.x, p2.y, p.x, p.y );
    }
    
    
    /**
     * From Sun's java.awt.geom.Line2D.java
     *
     * Returns the square of the distance from a point to a line segment.
     * @param X1,&nbsp;Y1 the coordinates of the beginning of the 
     *            specified line segment
     * @param X2,&nbsp;Y2 the coordinates of the end of the specified 
     *        line segment
     * @param PX,&nbsp;PY the coordinates of the specified point being
     *        measured
     * @return a double value that is the square of the distance from the
     *            specified point to the specified line segment.
     */
    public static double 
    ptSegDistSq( double X1, double Y1, 
                 double X2, double Y2, 
                 double PX, double PY )
    {
        // Adjust vectors relative to X1,Y1
        // X2,Y2 becomes relative vector from X1,Y1 to end of segment
        X2 -= X1;
        Y2 -= Y1;
        // PX,PY becomes relative vector from X1,Y1 to test point
        PX -= X1;
        PY -= Y1;
        double dotprod = PX * X2 + PY * Y2;
        double projlenSq;
        if (dotprod <= 0.0) {
            // PX,PY is on the side of X1,Y1 away from X2,Y2
            // distance to segment is length of PX,PY vector
            // "length of its (clipped) projection" is now 0.0
            projlenSq = 0.0;
        } else {
            // switch to backwards vectors relative to X2,Y2
            // X2,Y2 are already the negative of X1,Y1=>X2,Y2
            // to get PX,PY to be the negative of PX,PY=>X2,Y2
            // the dot product of two negated vectors is the same
            // as the dot product of the two normal vectors
            PX = X2 - PX;
            PY = Y2 - PY;
            dotprod = PX * X2 + PY * Y2;
            if (dotprod <= 0.0) {
                // PX,PY is on the side of X2,Y2 away from X1,Y1
                // distance to segment is length of (backwards) PX,PY vector
                // "length of its (clipped) projection" is now 0.0
                projlenSq = 0.0;
            } else {
                // PX,PY is between X1,Y1 and X2,Y2
                // dotprod is the length of the PX,PY vector
                // projected on the X2,Y2=>X1,Y1 vector times the
                // length of the X2,Y2=>X1,Y1 vector
                projlenSq = dotprod * dotprod / (X2 * X2 + Y2 * Y2);
            }
        }
        // Distance to line is now the length of the relative point
        // vector minus the length of its projection onto the line
        // (which is zero if the projection falls outside the range
        //  of the line segment).
        return PX * PX + PY * PY - projlenSq;
    }
    
    
    /**
     * A different version of relativeCCW
     **/
    public static int 
    relativeCCW( Point x1, Point x2, Point p )
    {
        return relativeCCW( x1.x, x1.y, x2.x, x2.y, p.x, p.y );
    } 
    
    
    /**
     * From Sun's java.awt.geom.Line2D.java
     *
     * Returns an indicator of where the specified point
     * (PX,&nbsp;PY) lies with respect to the line segment from
     * (X1,&nbsp;Y1) to (X2,&nbsp;Y2).
     * The value is 1 if the line segment must turn counterclockwise
     * to point at the specified point, -1 if it must turn clockwise,
     * or 0 if the point lies exactly on the line segment.
     * If the point is colinear with the line segment, but not between
     * the endpoints, then the value will be -1 if the point lies
     * "beyond (X1,&nbsp;Y1)" or 1 if the point lies
     * "beyond (X2,&nbsp;Y2)".
     * Note that an indicator value of 0 is rare and not useful for
     * determining colinearity because of floating point rounding
     * issues.
     * @param X1,&nbsp;Y1 the coordinates of the beginning of the
     *          specified line segment
     * @param X2,&nbsp;Y2 the coordinates of the end of the specified
     *          line segment
     * @param PX,&nbsp;PY the coordinates of the specified point to be
     *          compared with the specified line segment
     * @return an integer that indicates the position of the third specified
     *                  coordinates with respect to the line segment formed
     *                  by the first two specified coordinates.
     */
    public static int 
    relativeCCW( double X1, double Y1,
                 double X2, double Y2,
                 double PX, double PY ) 
    {
        X2 -= X1;
        Y2 -= Y1;
        PX -= X1;
        PY -= Y1;
        double ccw = PX * Y2 - PY * X2;
        if (ccw == 0.0) {
            // The point is colinear, classify based on which side of
            // the segment the point falls on.  We can calculate a
            // relative value using the projection of PX,PY onto the
            // segment - a negative value indicates the point projects
            // outside of the segment in the direction of the particular
            // endpoint used as the origin for the projection.   
            ccw = PX * X2 + PY * Y2;
            if (ccw > 0.0) {
                // Reverse the projection to be relative to the original X2,Y2
                // X2 and Y2 are simply negated.
                // PX and PY need to have (X2 - X1) or (Y2 - Y1) subtracted
                //    from them (based on the original values)
                // Since we really want to get a positive answer when the
                //    point is "beyond (X2,Y2)", then we want to calculate
                //    the inverse anyway - thus we leave X2 & Y2 negated.
                PX -= X2;
                PY -= Y2;
                ccw = PX * X2 + PY * Y2;
                if (ccw < 0.0) {
                    ccw = 0.0;
                }
            }
        }
        return (ccw < 0.0) ? -1 : ((ccw > 0.0) ? 1 : 0);
    }


    /**
    * 
    * Find the angles that the intermediate line segments make WRT
    * the x axis using the input_vertices
    *
    **/
    public static double[]
    getIntermediateAngles( Vertex input_vertices[] )
    {
        double dx;
        double dy;
        double intermediate_angles[] = new double[input_vertices.length-1];
                                    
        for ( int i=0; i<intermediate_angles.length; i++ ) {
            dx = input_vertices[i+1].x - input_vertices[i].x;
            dy = input_vertices[i+1].y - input_vertices[i].y;

            intermediate_angles[i] = Math.atan2( dy, dx );
        }
        
        //continualizeDirection( intermediate_angles );
        
        return intermediate_angles;
    }
    
    
    /**
     *
     * This is for making the Math.pi/2, -Math.pi/2 transitions
     * continuous.
     *
     **/
    public static void
    continualizeDirection( double direction[] )
    {
        // The following trick for continuous direction
        for ( int i=1; i<direction.length; i++ ) {
            if ( Math.abs( direction[i] - direction[i-1] ) > Math.PI/2 ) {
                for ( int j=-10; j<11; j++ ) {
                    if ( Math.abs( direction[i] - direction[i-1] + j*Math.PI) < 
                         Math.PI/2 ) {
                        direction[i] += j*Math.PI;
                        break;
                    }
                }
            }
        }
    }
    
    
    /**
     *
     * Return the segment length from index begin_index to 
     * end_index in points (end_index included). begin_index should
     * be less than or equal to end_index.
     *
     **/
    public static double
    segmentLength( Point points[], int begin_index, int end_index )
    {
        double length = 0.0;
        
        // The following trick for continuous direction
        for ( int i=begin_index; (i<end_index) && (i<points.length-1); i++ ) {
            length += points[i].distance( points[i+1] );
        }
        
        return length;
    }
}


/** 
  * 
  * $Log: GeometryUtil.java,v $
  * Revision 1.3  2003/03/06 01:08:49  moltmans
  * Added copyright to all the files.
  *
  * Revision 1.2  2001/11/23 03:23:30  mtsezgin
  * Major reorganization.
  *
  * Revision 1.1.1.1  2001/03/29 16:25:00  moltmans
  * Initial directories for DRG
  *
  * Revision 1.11  2000/09/06 22:40:37  mtsezgin
  * Combinations of curves and polygons are successfully approximated
  * by straight lines and Bezier curves as appropriate. System works
  * quite reliably.
  *
  * Revision 1.8  2000/06/03 01:52:32  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.7  2000/05/26 20:42:03  mtsezgin
  *
  * Completed the radian to degree conversion.
  *
  * Revision 1.6  2000/05/24 01:53:21  mtsezgin
  *
  * The polygon angle normalization works reliably.
  *
  * Revision 1.5  2000/04/25 22:14:25  mtsezgin
  *
  * Added more utility functions.
  *
  * Revision 1.4  2000/04/20 03:59:52  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.3  2000/04/11 00:41:46  mtsezgin
  *
  * Now the whole package succesfully parses a motor.
  *
  * Revision 1.2  2000/04/06 19:16:23  mtsezgin
  *
  * Modified all the classes to use my Point class which extends java.awt.Point
  * instead of directly using java.awt.Point
  *
  * Revision 1.1.1.1  2000/04/01 03:07:07  mtsezgin
  * Imported sources
  *
  * Revision 1.2  2000/03/31 22:41:03  mtsezgin
  *
  * Started Log tracking.
  *
  *  
  **/

