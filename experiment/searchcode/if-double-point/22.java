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
  * $Author: hammond $
  * $Date: 2004/01/10 03:02:25 $   
  * $Revision: 1.10 $
  * $Headers$
  * $Id: Point.java,v 1.10 2004/01/10 03:02:25 hammond Exp $     
  * $Name:  $   
  * $Locker:  $
  * $Source: /projects/drg/CVSROOT/drg/code/src/edu/mit/sketch/geom/Point.java,v $
  *  
  **/

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Comparator;


/**
  *
  * This class represents a Point described by a 
  * java.awt.geom.Line2D.Double
  *
  **/
public
class      Point
extends    java.awt.Point
implements GeometricObject,
           Serializable
{
    /**
    *
    * The original data points
    *
    **/
    private Polygon points;
    

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
	static final long serialVersionUID = -6351515395049627364L;
	
    /**
    *
    * The constructor.
    *
    **/    
  public Point()
    {
        super();
    }


    /**
    *
    * The constructor.
    *
    **/    
  public Point( java.awt.Point point )
    {
        super( point );
        if ( point == null ) {
            System.err.println( "NULL POINTER PASSES TO Point CONSTRUCTOR!!!" );
        }
    }


    /**
    *
    * The constructor.
    *
    **/    
  public Point( int x, int y )
    {
        super( x, y );
    }


    /**
    *
    * The constructor.
    *
    **/    
  public Point( java.awt.Point point, long time_stamp )
    {
        super( point );
        setTimeStamp( time_stamp );
    }


    /**
    *
    * The constructor.
    *
    **/    
  public Point( int x, int y, long time_stamp )
    {
        super( x, y );
        setTimeStamp( time_stamp );
    }

    
    /**
    *
    * Implement GeometricObject
    *
    **/    
    public String
    getType()
    {
        return "point";
    }
    
    
    /**
    *
    * Override toString
    *
    **/    
    public String
    toString()
    {
        return "Point ( " + x + ", " + y + " ) ";
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
    * Draw the object
    *
    **/
    public void
    paint( Graphics g )
    {
        g.fillOval( (int)(x)-2, (int)(y)-2, 8, 8 );
    }


    /**
    *
    * Returns true if the point is within +-radius distance from
    * this point. Returns false o/w.
    *
    **/
    public boolean
    pointIsOn( Point point, int radius )
    {
        return ( distance( x, y ) < radius );
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
   * Returns the angle of the line formed from this point to point p
   **/
   public double getAngle(Point p){
      double ydiff = p.getY() - getY();
      double xdiff = p.getX() - getX();
      return Math.atan2( ydiff, xdiff );
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
    * Supplied for completeness.
    *
    **/
    public Rectangle
    getRectangularBounds()
    {
        return new Rectangle( x-1, y-1, 2, 2 );
    }


    /**
    *
    * This method should return a polygon that corresponds to this
    * object. The polygon is implicity closed and the last 
    * point doesn't necessarily have to be the same as the first 
    * (zeroth) point. The returned polygon is a liberal 
    * approximation to the real shape of the object. 
    *
    * Known eksik: This should be refined to return a more 
    * conservative result.
    *
    **/
    public Polygon 
    getPolygonalBounds()
    {
        Polygon result = new Polygon();
        
        result.addPoint( x-1, y-1 );
        result.addPoint( x+1, y-1 );
        result.addPoint( x+1, y+1 );
        result.addPoint( x-1, y+1 );
        
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
    setDataPoints( new Polygon( pts ) );
    m_vertices = new Vertex[pts.length];
    for ( int i = 0; i < pts.length; i++ ) {
      m_vertices[i] = pts[i];
    }
  }

  public Vertex[] getOriginalVertices()
  {
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
    * Rotate about the origin by radians amount.
    *
    **/
    public void
    rotate( double radians )
    {
        double radius = Math.sqrt( x*x + y*y );
		double angle  = Math.atan2( y, x ) + radians;
		x = (int)(Math.cos( angle ) * radius);
		y = (int)(Math.sin( angle ) * radius);
    }
    
    
    /**
    *
    * Add the arguments to the (x, y) position of the object.
    *
    **/
    public void
    translate( double x, double y )
    {
        this.x += (int)x;
        this.y += (int)y;
        if ( points != null )
            points.translate( x, y );
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
    * Multiply the x and y fields by the scale.
    *
    **/
    public void
    scale( double scale )
    {
        x *= scale;
        y *= scale;
    }
    
    
    /**
    *
    * Return distance from origin.
    *
    **/
    public double
    magnitude()
    {
        return Math.sqrt( x*x + y*y ); 
    }
    
    
    /**
    *
    * Return distance from this point to input.
    *
    **/
    public double
    distance( Point p )
    {
        return Math.sqrt( (x-p.x)*(x-p.x) + (y-p.y)*(y-p.y) ); 
    }
    
    
    /**
    *
    * paint points. 
    *
    **/
    public static void
    paint( Point points[], Graphics g )
    {
        for ( int i=0; i<points.length-1; i++ ) {
            g.drawLine( points[i].x,
                        points[i].y,
                        points[i+1].x,
                        points[i+1].y );
		}
    }
    
    
    /**
    *
    * paint points. 
    *
    **/
    public static Point[]
    arrayListToPoints( ArrayList list )
    {
		Point result[] = new Point[list.size()];
        for ( int i=0; i<result.length; i++ ) {
			result[i] = (Point)list.get( i );
		}
		
		return result;
    }

  public GeometricObject copy()
  {
    Point point = new Point( x, y );
    if( points != null ) {
      point.points = (Polygon)points.copy();
    }
    
    point.time_stamp = time_stamp;
    return point;
  }
  
}
/** 
  * 
  * $Log: Point.java,v $
  * Revision 1.10  2004/01/10 03:02:25  hammond
  * no real edits
  *
  * Revision 1.9  2003/06/26 19:57:14  calvarad
  * Lots of bug fixes
  *
  * Revision 1.8  2003/03/06 01:08:49  moltmans
  * Added copyright to all the files.
  *
  * Revision 1.7  2002/08/14 18:35:56  moltmans
  * Added a copy method to Geometric primitives so that we can make deep
  * copies of objects.  The copy method is careful to not reuse any data.
  *
  * Revision 1.6  2002/08/06 15:37:30  mtsezgin
  * Added static serialVersionUID information to the serializable geometric objects,so that serialization doesn't break unless modifications made to the class are truly incompatible changes.
  *
  * Revision 1.5  2002/05/24 17:33:07  mtsezgin
  * Added utility functions.
  *
  * Revision 1.4  2002/02/26 02:33:37  mtsezgin
  * Started on the spatial query infrastructure. It is not as efficient
  * as I want it to be yet.
  *
  * Revision 1.3  2001/11/23 03:23:31  mtsezgin
  * Major reorganization.
  *
  * Revision 1.2  2001/04/12 19:25:55  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.1.1.1  2001/03/29 16:25:00  moltmans
  * Initial directories for DRG
  *
  * Revision 1.18  2000/09/06 22:40:51  mtsezgin
  * Combinations of curves and polygons are successfully approximated
  * by straight lines and Bezier curves as appropriate. System works
  * quite reliably.
  *
  * Revision 1.12  2000/06/08 03:14:30  mtsezgin
  *
  * Made the class Serializable for supporting saving and loading
  * designs. Both the object attributes, and the original data points
  * are stored and restored.
  *
  * Revision 1.11  2000/06/03 01:52:34  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.10  2000/05/03 23:26:46  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.9  2000/04/28 04:45:04  mtsezgin
  *
  * Now each GeometricObject keeps the mouse input that was previously
  * discarded. User can switch between seeing the recognized mode and
  * the raw mode. setDataPoints( Polygon points ) and getDataPoints()
  * are added to GeometricObject, and all the implementors are modified
  * accordingly.
  *
  * Revision 1.8  2000/04/25 22:18:57  mtsezgin
  *
  * The getBounds changed to getRectangularBounds which returns a Rectangle.
  *
  * Revision 1.7  2000/04/17 07:02:30  mtsezgin
  *
  * Finally made the Rectangle really rotatable.
  *
  * Revision 1.6  2000/04/13 06:24:08  mtsezgin
  *
  * The current version of the program recognized Crosses, and Shades.
  * Implementors of Terminal and their descendants were modified to
  * implement the changes in GeometricObject.
  *
  * Revision 1.5  2000/04/11 01:41:46  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.4  2000/04/11 00:41:47  mtsezgin
  *
  * Now the whole package succesfully parses a motor.
  *
  * Revision 1.3  2000/04/07 04:28:54  mtsezgin
  *
  * Added Rotatable interface. Rectangle and Line are Rotatable for now, but
  * Rectangle should be modified to have an angle field. Also other rotatable
  * classes should also implement Rotatable interface.
  *
  *
  **/

