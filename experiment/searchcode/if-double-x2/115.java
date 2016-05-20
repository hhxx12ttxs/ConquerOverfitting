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
  * $Author: calvarad $
  * $Date: 2003/06/26 19:57:14 $   
  * $Revision: 1.11 $
  * $Headers$
  * $Id: Line.java,v 1.11 2003/06/26 19:57:14 calvarad Exp $     
  * $Name:  $   
  * $Locker:  $
  * $Source: /projects/drg/CVSROOT/drg/code/src/edu/mit/sketch/geom/Line.java,v $
  *  
  **/

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import edu.mit.sketch.toolkit.StrokeData;
import edu.mit.sketch.util.GraphicsUtil;

/**
  *
  * This class represents a line described by its end-points p, q.
  *
  **/
public
class      Line
extends    java.awt.geom.Line2D.Double
implements GeometricObject,
           Rotatable,
           Serializable
           
{
    /**
    *
    * The original data points
    *
    **/
    private Polygon points;
    

  /**
   *  The original vertices
   */
  private Vertex m_vertices[];
  
    /**
    *
    * Time stamp of this object.
    *
    **/
    public long time_stamp;


    /**
    *
    * Graphics context for this Geometric object.
    *
    **/
    public transient Graphics graphics;


    /**
    * Serialversion UID added to keep serialization working
	* when modifications are result in a compatible class.
    **/    
	static final long serialVersionUID = 4047294002555214055L;


    /**
    *
    * The constructor.
    *
    **/    
  public Line( Line line )
    {
        this( line.x1,
              line.y1,
              line.x2,
              line.y2 );
	setOriginalVertices( line.getOriginalVertices() );
    }


    /**
    *
    * The constructor.
    *
    **/    
  public Line()
    {
        this( 0, 0, 0, 0 );
    }


    /**
    *
    * The constructor.
    *
    **/    
  public Line( double x1, double y1, double x2, double y2 )
    {
        super( x1, y1, x2, y2 );
    }

    
    /**
    *
    * The constructor.
    *
    **/    
  public Line( Point p, Point q )
    {
        this( p.x, p.y, q.x, q.y );
    }


    /**
    *
    * For serialization of superclass' fields.
    *
    **/    
     private void 
    writeObject( ObjectOutputStream out )
    throws IOException
    {
        out.defaultWriteObject();
        
        out.writeDouble(x1);
        out.writeDouble(y1);
        out.writeDouble(x2);
        out.writeDouble(y2);
    }
    
    /**
    *
    * For deserialization of superclass' fields.
    *
    **/    
    private void 
    readObject( ObjectInputStream in )
    throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        
        x1 = in.readDouble();
        y1 = in.readDouble();
        x2 = in.readDouble();
        y2 = in.readDouble();
    }
     
         
    /**
    *
    * Implement GeometricObject
    *
    **/    
    public String
    getType()
    {
        return "line";
    }
    
    
    /**
    *
    * Override toString
    *
    **/    
    public String
    toString()
    {
        return "Line ( " + x1 + ", " + y1 + " )-( " + x2 + ", " + y2 + " )";
    }


    /**
    *
    * Draw the object
    *
    **/
    public void
    paint()
    {
        if ( graphics == null ) // Draw only if g
            return;             // is valid
        graphics.setColor( Color.black );
        paint( graphics );
    }


    /**
    *
    * Draw the object
    *
    **/
    public void
    paint( Graphics g )
    {
        GraphicsUtil.drawThickLine( 1,
                                    g,
                                    (int)x1,
                                    (int)y1,
                                    (int)x2,
                                    (int)y2 );
    }
    
    
    /**
    *
    * This method is used to paint the original data points that
    * forms this GeometricObject
    *
    **/
    public void
    paintOriginal( Graphics g )
    {
        points.paint( g );
    }
    
    
    /**
    *
    * Returns true if the point is within +-radius distance from
    * the curve defining the object. Returns false o/w.
    *
    **/
    public boolean
    pointIsOn( Point point, int radius )
    {
        return ( ptSegDist( point ) < radius );
    }


    /**
    *
    * Returns true if the point is within +-radius distance from
    * the original curve defining the object. Returns false o/w.
    *
    **/
    public boolean
    pointIsOnOriginal( Point p, int radius )
    {
        return points.pointIsOn( p, radius );
    }


   /**
    *
    * Set graphics context for this Geometric object.
    * Must be set at least once before doing any drawing.
    *
    **/
    public void
    setGraphicsContext( Graphics g )
    {
        graphics = g;
    }


    /**
    *
    * This method should return true if the input objects touch.
    * It should be optimized making use of the object type 
    * information.
    *
    **/
    public boolean
    touches( GeometricObject object )
    {
        return getPolygonalBounds().touches( object.getPolygonalBounds() );
    }


    /**
    *
    * Supplied for completeness.
    *
    **/
    public Rectangle
    getRectangularBounds()
    {
	java.awt.geom.RectangularShape bounds = super.getBounds2D();
	return new Rectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), 0.0, bounds.getHeight());

	// Older version
        // return new Rectangle( super.getBounds() );
    }


    /**
    *
    * Returns false if the argument is not completely inside 
    * this object. Return true O/W.
    *
    **/
    public boolean
    containsGeometricObject( GeometricObject object )
    {
        return getPolygonalBounds().containsGeometricObject( object );
    }


    /**
    *
    * This method should return a polygon that fully contains the 
    * current object. The polygon is implicity closed and the last 
    * point doesn't necessarily have to be the same as the first 
    * (zeroth) point.
    *
    **/
    public Polygon 
    getPolygonalBounds()
    {
        Polygon result = new Polygon();
        int x_1 = (int)x1;
        int x_2 = (int)x2;
        int y_1 = (int)y1;
        int y_2 = (int)y2;
        
        int left_x;
        int right_x;
        int left_y;
        int right_y;
        
        if ( x_1 == x_2 ) { // if the line is vertical
            left_x  = x_1;
            right_x = x_2;
            
            if ( y1 < y2 ) {  // Find the upper point
                left_y  = y_1; // make sure left_y is less than right_y.
                right_y = y_2;
            } else {
                left_y  = y_2;
                right_y = y_1;                       
            }                                            
            
              result.addPoint( left_x-1,  left_y-1  );
              result.addPoint( right_x-1, right_y+1 );
              result.addPoint( right_x+1, right_y+1 );
              result.addPoint( left_x+1,  left_y-1 );
            
            return result;
        }

        if ( x_1 < x_2 ) {
            left_x  = x_1;
            left_y  = y_1;
            right_x = x_2;
            right_y = y_2;
        } else {
            left_x  = x_2;
            left_y  = y_2;
            right_x = x_1;
            right_y = y_1;
        }
                
        
        result.addPoint( left_x-1,  left_y+1  );
        result.addPoint( right_x+1, right_y+1 );
        result.addPoint( right_x+1, right_y-1 );
        result.addPoint( left_x-1,  left_y-1 );
        
        return result;
    }


    /**
    *
    * This method should return the spatial relation of the input
    * parameter with respect to this object. see the SpatialRelation
    * class for a detailed list of possible spatial relations.
    * Another version of this method should be implemented for 
    * handling spatial relations where a rotated coordinate
    * system is to be used.
    *
    **/
    public int
    spatialRelation( GeometricObject object )
    {
        return getRectangularBounds().spatialRelation( object );
    }


    /**
    *
    * Returns the angle of the object WRT the x axis in radians.
    *
    * Known eksik: Probably confused between this and getAngle()
    *
    **/
    public double
    getCartesianAngle()
    {
        return Math.atan2( (y2-y1), x2-x1 );
    }


    /**
    *
    * Returns the angle of the object WRT the x axis in radians.
    *
    * Known eksik: Probably confused between this and getCartesianAngl()
    *
    **/
    public double
    getAngle()
    {
        return Math.atan2( y2-y1, x2-x1 );
    }


    /**
    *
    * Returns true if the input Polygon is a line.
    *
    **/
    public static boolean
    isLine( Polygon p )
    {
        return (p.npoints == 1);
    }
    
    
    /**
    *
    * Sets the time stamp of the current Terminal
    *
    **/
    public void
    setTimeStamp( long time_stamp )
    {
        this.time_stamp = time_stamp;
    }
    
    
    /**
    *
    * Returns the time stamp of the current Terminal
    *
    **/
    public long
    getTimeStamp()
    {
        return time_stamp;
    }
    
    
    /**
    *
    * Swaps the first and the secons points.
    *
    **/
    public void
    swapPoints()
    {
            double tmp;
            
            tmp = x1;
            x1  = x2;
            x2  = tmp;
            
            tmp = y1;
            y1  = y2;
            y2  = tmp;
    }
    
    
    /**
    *
    * Swaps the first and the second points if needed so that 
    * the second point is to the right of the first one.
    * The overall line structure is preserved.
    *
    **/
    public void
    pointRight()
    {
        if ( x1 > x2 ) {
            swapPoints();
        }
    }
    
    
    /**
    *
    * Swaps the first and the second points if needed so that 
    * the second point is to the left of the first one.
    * The overall line structure is preserved.
    *
    **/
    public void
    pointLeft()
    {
        if ( x1 < x2 ) {
            swapPoints();
        }
    }
    
    
    /**
    *
    * Swaps the first and the second points if needed so that 
    * the second point is above the first one.
    * The overall line structure is preserved.
    *
    **/
    public void
    pointUp()
    {
        if ( y1 > y2 ) {
            swapPoints();
        }
    }
    
    
    /**
    *
    * getP1
    *
    **/
    public java.awt.geom.Point2D
    getP1()
    { 
		return (java.awt.geom.Point2D)(new Point( (int)x1, (int)y1 ) );
    }
    
    
    /**
    *
    * getP1
    *
    **/
    public java.awt.geom.Point2D
    getP2()
    { 
		return (java.awt.geom.Point2D)(new Point( (int)x2, (int)y2 ) );
    }
    
    
    /**
    *
    * Swaps the first and the second points if needed so that 
    * the second point is below the first one.
    * The overall line structure is preserved.
    *
    **/
    public void
    pointDown()
    {
        if ( y1 < y2 ) {
            swapPoints();
        }
    }
    
    
    /**
    *
    * This method is used to set the original data points that
    * forms this GeometricObject
    *
    **/
    public void
    setDataPoints( Polygon points )
    {
        this.points = points;
    }

  public void setOriginalVertices( Vertex pts[] )
  {
    points = new Polygon( pts );
    m_vertices = new Vertex[pts.length];
    for ( int i = 0; i < pts.length; i++ ) {
      m_vertices[i] = pts[i];
    }
  }

  public Vertex[] getOriginalVertices()
  {
    if ( m_vertices == null ) {
      return null;
    }
    Vertex ret[] = new Vertex[m_vertices.length];
    for ( int i = 0; i < m_vertices.length; i++ ) {
      ret[i] = m_vertices[i];
    }
    return ret;

  }
  
    /**
    *
    * This method is used to get the original data points that
    * forms this GeometricObject
    *
    **/
    public Polygon
    getDataPoints()
    {
        return points;
    }
    
    
    /**
    *
    * This method is used to get the original data points that
    * forms this GeometricObject
    *
    **/
    public Polygon
    toPolygon()
    {
        Polygon result = new Polygon();
        result.addPoint( (int)x1, (int)y1 );
        result.addPoint( (int)x2, (int)y2 );
        result.setDataPoints( new Polygon( points ) );
        return result;
    }
    
    
    /**
    *
    * Returns the angle that is closest to the angle that this 
    * line makes with the x axis from the input argument. In this
    * context, Math.PI is not the same as 0. The method uses the 
    * absolute value of difference as the metric for distance.
    *
    *
    **/
    public double
    chooseApproximateAngle( double angle_set[] )
    {
        int    smallest_index  = 0;
        double smallest_error = Math.abs( getAngle() - angle_set[0] );
        
        
        for ( int i=0; i<angle_set.length; i++ ) {
            if ( smallest_error >
                 Math.abs( getAngle() - angle_set[i] ) ) {
                smallest_error = Math.abs( getAngle() - angle_set[i] );
                smallest_index = i;
            }
        }
                                    
        return angle_set[smallest_index];
    }
    
    
    /**
    *
    * Returns false if the objects in the input array are
    * completely inside this object. Return true O/W.
    *
    **/
    public boolean
    containsGeometricObjects( GeometricObject objects[] )
    {
        for ( int i=0; i<objects.length; i++ ) {
            if ( !containsGeometricObject( objects[i] ) )
                return false;
        }
        
        return true;
    }
    
    
    /**
    *
    * Add the arguments to the position of the object.
    *
    **/
    public void
    translate( double x, double y )
    {
        x1 += x;
        y1 += y;
        x2 += x;
        y2 += y;
        if ( points != null )
            points.translate( x, y );
    }
    
    
    /**
    *
    * Returns the length of this line.
    *
    **/
    public double
    length()
    {
        return Point.distance( x1, y1, x2, y2 );
    }

    /**
    *
    * Returns the center this line.
    *
    **/
    public Point
    center()
    {
        return new Point( (int)((x1 + x2) / 2), (int)((y1 + y2) / 2) );
    }
  
    
    /**
    *
    * Returns the lsq error.
    *
    **/
    public double
    getLSQError( Point points[], Range range )
    {
        double lsq_error  = 0.0;

        for ( int i=range.min; i<range.max; i++ ) {
            lsq_error +=  ptLineDistSq( points[i] );
        }
        
        return lsq_error/(range.max-range.min);
    }
    
    
    /**
    *
    * Returns the lsq error.
    *
    **/
    public double
    getLSQError( StrokeData data, Range range )
    {
        double lsq_error  = 0.0;
        Vertex vertices[] = data.vertices;
        
        for ( int i=range.min; i<range.max; i++ ) {
            lsq_error +=  ptLineDistSq( vertices[i] );
        }
        
        return lsq_error/(range.max-range.min);
    }
    
    
    /**
    *
    * Returns true if the lines are parallel must be replaced
	* 
    **/
    public boolean
    isParallel( Line line )
    {
        int dx1 = ((int)(this.x2-this.x1));
        int dx2 = ((int)(line.x2-line.x1));
        
        Point result;
        
        // Two vertical lines
        if ( (dx1 == 0) && (dx2 == 0) ) {
			return true;
        }

        // One vertical line
        if ( (dx1 == 0) || (dx2 == 0) ) {
			return false;
        }
        
        // Parallel li	nes
        if ( GeometryUtil.equalDoubles( 
                 (this.y2-this.y1)/(this.x2-this.x1),
                 (line.y2-line.y1)/(line.x2-line.x1),
                 0.1 ) ) {  // This is the difference allowed between slopes
			return true;
        }

        return false;
    }
    
  public Point getMidpoint()
  {
    return new Point( (int)((x1+x2)/2), (int)((y1+y2)/2) );
  }
  
    /**
    *
    * Returns the intersection of the two infinite length lines
    * described by this line and the argument. This method converts
    * the end-points of the lines to ints rather than working with 
    * doubles. This is reasonably good approximation. After all even
    * if we use doubles, we won't get a "perfect" intersection point
    * and the simplification makes handling of special cases easier.
    * Return null if the lines are parallel (even if they are the
    * same).
    *
    **/
    public Point
    getIntersection( Line line )
    throws GeometricComputationException
    {
        int dx1 = ((int)(this.x2-this.x1));
        int dx2 = ((int)(line.x2-line.x1));
        
        Point result;
        
        // Two vertical lines
        if ( (dx1 == 0) && (dx2 == 0) ) {
	  //            System.out.println( " Case 0 " );
            throw new GeometricComputationException( "Lines parallel" );
        }

        // One vertical line
        if ( (dx1 == 0) || (dx2 == 0) ) {
            Line vertical_line;
            Line other_line;

            if ( dx1 == 0 ) {
                result = new Point( (int)this.x1, 
                                    (int)( line.y1 -
                                           (line.y2-line.y1)/
                                           (line.x2-line.x1)* // Line's slope
                                           (line.x1-this.x1) ) );
                // System.out.println( " Case 1 " + result );
                return result;
            } else {
                result = new Point( (int)line.x1, 
                                    (int)( this.y1 -
                                           (this.y2-this.y1)/
                                           (this.x2-this.x1)* // Line's slope
                                           (this.x1-line.x1) ) );
                // System.out.println( " Case 2 " + result );
                return result;
            }
        }
        
        // Parallel lines
        if ( GeometryUtil.equalDoubles( 
                 (this.y2-this.y1)/(this.x2-this.x1),
                 (line.y2-line.y1)/(line.x2-line.x1),
                 0.001 ) ) {  // This is the difference allowed between slopes
            // System.out.println( " Case 3 " );
            throw new GeometricComputationException( "Lines parallel" );
        }
        
        // use line equations ax+b and cx+d for this and the line
        // here, a==dx1/(this.y2-this.y1), and 
        // b==dx2/(line.y2-line.y1)
        
        double a = (this.y2-this.y1)/(this.x2-this.x1);
        double b = this.y1 - this.x1*a;
        double c = (line.y2-line.y1)/(line.x2-line.x1);
        double d = line.y1 - line.x1*c;

        // From now on the invariant is that a != c
        // System.out.println( " a = " + a );
        // System.out.println( " b = " + b );
        // System.out.println( " c = " + c );
        // System.out.println( " d = " + d );
        
        double x = (d-b)/(a-c);
        result   = new Point( (int)x, (int)(a*x+b) );
        // System.out.println( " Case 4 " + result );
        return result;
    }

  public final GeometricObject copy() 
  {
    Line line = new Line( x1, y1, x2, y2 );
    
    if( points != null ) {
      line.points = (Polygon)points.copy();
    }
    
    line.time_stamp = time_stamp;
    return line;
  }
  
}

/** 
  * 
  * $Log: Line.java,v $
  * Revision 1.11  2003/06/26 19:57:14  calvarad
  * Lots of bug fixes
  *
  * Revision 1.10  2003/05/12 17:21:51  olyav
  * Made PolarPoint serializable. Added a methods to Ellipse and Line to be able to retrieve their center.
  *
  * Revision 1.9  2003/03/06 01:08:49  moltmans
  * Added copyright to all the files.
  *
  * Revision 1.8  2002/10/31 20:11:35  olyav
  * Added a way in Ellipse to ask for the line that is that ellipse's horizontal or vertical axis.
  *
  * Revision 1.7  2002/08/15 20:54:05  moltmans
  * Removed a println.
  *
  * Revision 1.6  2002/08/14 18:35:56  moltmans
  * Added a copy method to Geometric primitives so that we can make deep
  * copies of objects.  The copy method is careful to not reuse any data.
  *
  * Revision 1.5  2002/08/06 15:37:30  mtsezgin
  * Added static serialVersionUID information to the serializable geometric objects,so that serialization doesn't break unless modifications made to the class are truly incompatible changes.
  *
  * Revision 1.4  2002/06/09 23:56:47  mtsezgin
  * added getP1, getP2
  *
  * Revision 1.3  2002/05/24 17:33:07  mtsezgin
  * Added utility functions.
  *
  * Revision 1.2  2001/11/23 03:23:30  mtsezgin
  * Major reorganization.
  *
  * Revision 1.1.1.1  2001/03/29 16:25:00  moltmans
  * Initial directories for DRG
  *
  * Revision 1.23  2000/09/06 22:40:38  mtsezgin
  * Combinations of curves and polygons are successfully approximated
  * by straight lines and Bezier curves as appropriate. System works
  * quite reliably.
  *
  * Revision 1.17  2000/06/08 03:14:30  mtsezgin
  *
  * Made the class Serializable for supporting saving and loading
  * designs. Both the object attributes, and the original data points
  * are stored and restored.
  *
  * Revision 1.16  2000/06/03 01:52:33  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.15  2000/06/02 21:11:14  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.14  2000/05/24 02:48:51  mtsezgin
  *
  * Fixed a bug in getIntersection( Line line )
  *
  * Revision 1.12  2000/05/03 23:26:45  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.11  2000/04/28 04:45:03  mtsezgin
  *
  * Now each GeometricObject keeps the mouse input that was previously
  * discarded. User can switch between seeing the recognized mode and
  * the raw mode. setDataPoints( Polygon points ) and getDataPoints()
  * are added to GeometricObject, and all the implementors are modified
  * accordingly.
  *
  * Revision 1.10  2000/04/25 22:16:36  mtsezgin
  *
  * Fixed some bugs and added the getAngle and other angle related
  * funtions. Also getBounds changed to getRectangularBounds which returns
  * a Rectangle.
  *
  * Revision 1.9  2000/04/13 06:24:08  mtsezgin
  *
  * The current version of the program recognized Crosses, and Shades.
  * Implementors of Terminal and their descendants were modified to
  * implement the changes in GeometricObject.
  *
  * Revision 1.8  2000/04/12 04:00:16  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.7  2000/04/11 01:41:45  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.6  2000/04/11 00:41:46  mtsezgin
  *
  * Now the whole package succesfully parses a motor.
  *
  * Revision 1.5  2000/04/07 04:28:54  mtsezgin
  *
  * Added Rotatable interface. Rectangle and Line are Rotatable for now, but
  * Rectangle should be modified to have an angle field. Also other rotatable
  * classes should also implement Rotatable interface.
  *
  * Revision 1.4  2000/04/06 21:33:42  mtsezgin
  *
  * Spatial relation represents 9 basic relations for now. It should be extended.
  *
  * GeometricObject is extended. Implementors extended with empty stubs.
  *
  * Revision 1.3  2000/04/06 19:16:23  mtsezgin
  *
  * Modified all the classes to use my Point class which extends java.awt.Point
  * instead of directly using java.awt.Point
  *
  * Revision 1.2  2000/04/01 22:51:21  mtsezgin
  *
  * Started implementation. Also from now on using jdk 1.2
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

