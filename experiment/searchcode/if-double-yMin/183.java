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
  * $Date: 2003/09/08 20:52:11 $   
  * $Revision: 1.16 $
  * $Headers$
  * $Id: Polygon.java,v 1.16 2003/09/08 20:52:11 moltmans Exp $     
  * $Name:  $   
  * $Locker:  $
  * $Source: /projects/drg/CVSROOT/drg/code/src/edu/mit/sketch/geom/Polygon.java,v $
  *  
  **/

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import java.util.Enumeration;
import java.util.Vector;

import java.io.Serializable;

import edu.mit.sketch.util.GraphicsUtil;
import edu.mit.sketch.ui.Tablet;
import edu.mit.sketch.toolkit.StatisticsModule;

/**
  *
  * This class represents a polygon described by its vertices.
  * The polygon should be closed explicitly if needed.
  *
  **/
public
class      Polygon
extends    java.awt.Polygon
implements GeometricObject,
           Serializable
{
    /**
    *
    * The original data points
    *
    **/
    private Polygon points;
    

  /**
   *  An alternate representation of the orinigal data points.
   *
   */
  private Vertex[] m_vertices;
  
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
	static final long serialVersionUID = 1842886910037622996L;

    /**
    *
    * The ranges for the segments in the general_path
    *
    **/
    private int indices[];
    private Range ranges[];

    /**
     *
     * Sets the ranges for the segments in the polygon(line)
     * It does this by processing the list of indices based on the original data points 
     *
    **/
    public void setIndices(int[] indices)
    {
      this.indices = indices;
      ranges = new Range[indices.length - 1];
      for(int i = 0; i < ranges.length; i++)
      {
        ranges[i] = getRange(i);
      }
    }

    /**
     *
     * Gets the ranges for the segments in the polygon(line)
     * These are the ranges for the data points of the original data points.
     *
    **/
    public Range[] getRanges()
    {
      //System.out.println("Getting Ranges");
      //for(int i = 0; i < ranges.length; i++)
      //{
      //  System.out.println("range " + i + " : " + ranges[i]);
      //}
      return ranges;
    }

    /**
     *
     * Returns the ith range for the segments in the polygon(line).
     *
    **/
    public Range getRange(int i)
    {
      if(i+1 < indices.length)
        return new Range(indices[i], indices[i+1]);
      else
        return null;
    }

    /**
    *
    * The constructor.
    *
    **/    
  public Polygon()
    {
        super();
    }
    

    /**
    *
    * The constructor.
    *
    **/    
  public Polygon( java.awt.Polygon p )
    {
        super();
        
        npoints = p.npoints;        
        xpoints = new int[p.npoints];
        ypoints = new int[p.npoints];
        
        for ( int i=0; i<npoints; i++ ) {
            xpoints[i] = p.xpoints[i];
            ypoints[i] = p.ypoints[i];
        }
    }
    
    /**
    *
    * The constructor.
    *
    **/    
  public Polygon( Point points[] )
    {
        super();
        
        npoints = points.length;        
        xpoints = new int[points.length];
        ypoints = new int[points.length];
        
        for ( int i=0; i<npoints; i++ ) {
            xpoints[i] = points[i].x;
            ypoints[i] = points[i].y;
        }
    }
    

    /**
    *
    * The constructor.
    *
    **/    
  public Polygon( int xpoints[], int ypoints[], int npoints )
    {
        super( xpoints, ypoints, npoints );
    }
    
    
    /**
    *
    * The constructor.
    *
    **/    
  public Polygon( Polygon p )
    {
        super();
        npoints = p.npoints;        
        xpoints = new int[npoints];
        ypoints = new int[npoints];
        
        for ( int i=0; i<npoints; i++ ) {
            xpoints[i] = p.xpoints[i];
            ypoints[i] = p.ypoints[i];
        }
        
        points = p.getDataPoints();
	m_vertices = p.getOriginalVertices();
        setGraphicsContext( p.graphics );
        setTimeStamp( p.time_stamp );
    }
    
    
    /**
    *
    * Copy the vertices. The input should be the same length as this 
    * polygon.
    *
    **/ 
  public void   
    copyVerticesFrom( Polygon p )
    {
        for ( int i=0; i<npoints; i++ ) {
            xpoints[i] = p.xpoints[i];
            ypoints[i] = p.ypoints[i];
        }
        points = p.getDataPoints();
	m_vertices = p.getOriginalVertices();
    }
    
    
    /**
    *
    * The constructor.
    *
    **/    
    Polygon( Line line )
    {
        super();
        npoints = 2;        
        xpoints = new int[2];
        ypoints = new int[2];
        
        xpoints[0] = (int)line.x1;
        ypoints[0] = (int)line.y1;
        xpoints[1] = (int)line.x2;
        ypoints[1] = (int)line.y2;

        points = line.getDataPoints();
        m_vertices = line.getOriginalVertices();
	setGraphicsContext( line.graphics );
        setTimeStamp( line.time_stamp );
    }
    
    
    /**
    *
    * Implement GeometricObject
    *
    **/    
    public String
    getType()
    {
        return "polygon";
    }
    
    
    /**
    *
    * add point for doubles
    *
    **/    
    public void
    addPointDouble( double x, double y )
    {
        addPoint( (int)x, (int)y );
    }
    
    
    /**
    *
    * Override toString
    *
    **/    
    public String
    toString()
    {
        String description;
        description = "Polygon with " + npoints + " vertices ";
           for ( int i=0; i<npoints; i++ ) {
            description += "( " + xpoints[i] + ", " +
                                  ypoints[i] + " )--";
        }
        
        return description;
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
           for ( int i=0; i<npoints-1; i++ ) {
             GraphicsUtil.drawThickLine( 1,
                                         graphics,
                                         xpoints[i],
                                         ypoints[i],
                                         xpoints[i+1],
                                         ypoints[i+1] );
        }
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
        for ( int i=0; i<npoints-1; i++ ) {
             GraphicsUtil.drawThickLine( 1,
                                         g,
                                         xpoints[i],
                                         ypoints[i],
                                         xpoints[i+1],
                                         ypoints[i+1] );
        }
    }
    
    
    /**
    *
    * Returns distance from point to the polygon
    *
    **/
    public double getDistanceTo( Point p )
    {
      double minDist = Double.MAX_VALUE;
      double dist;
      
      for ( int i=0; i<npoints-1; i++ ) {
        dist =  Line.ptSegDist( xpoints[i],   ypoints[i],
                                xpoints[i+1], ypoints[i+1],
                                p.x, p.y );
        if( dist < minDist ) {
          minDist = dist;
          }
      }
      return minDist;
    }

  /**
    *
    * Returns true if the point is within +-radius distance from
    * the curve defining the object. Returns false o/w.
    *
    **/
    public boolean
    pointIsOn( Point p, int radius )
    {
        for ( int i=0; i<npoints-1; i++ ) {
            if ( Line.ptSegDist( xpoints[i],   ypoints[i],
                                 xpoints[i+1], ypoints[i+1],
                                 p.x,           p.y ) < radius ) {
                return true;
            }
        }
        
        return false;
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
    * Add the arguments to the (x, y) position of the object.
    *
    **/
    public void
    translate( double x, double y )
    {
        for ( int i=0; i<npoints; i++ ) {
            xpoints[i] += (int)x;
            ypoints[i] += (int)y;
        }
        if ( points != null )
            points.translate( x, y );
    }
   

    /**
    *
    * Returns the index of the vertices that is closest to point p.
    *
    **/
    public int
    closestNeighborIndex( Point p )
    {
        int distance;
        int min       = (int)(p.distance( xpoints[0], ypoints[0] ));
        int min_index = 0;
        
        for ( int i=0; i<npoints; i++ ) {
            distance = (int)(p.distance( xpoints[i], ypoints[i] ));
            if ( distance < min ) {
                min       = distance;
                min_index = i;
            }
        }
        
        return min_index;
    }
    
    
    /**
    *
    * Returns the ith point in this Polygon
    *
    **/
    public Point
    pointAt( int i )
    {
        return new Point( xpoints[i], ypoints[i] );
    }
    
    
    /**
    *
    * Draws an arc (angle) on the ith vertex (start from 0)
    * using the current graphics context. Point p is used to 
    * determine which angle to paint ( internal or external ).
    * Cosine theorem is applied here.
    *
    **/
    public void
    drawAngleAt( int i, Point reference_point )
    {
        if ( ( graphics == null      ) || 
             ( i >=(npoints-1) ) ||
             ( i < 1                 ) ) // Draw only if g
            return;                      // and i are valid
        Point current     = pointAt(i);
        Point next        = pointAt(i+1);
        Point previous    = pointAt(i-1);
        
        int   theta_start = (int)(Math.atan2( current.y - previous.y,
                                              current.x - previous.x )*
                                              180/Math.PI );
          if ( theta_start < 0.0 ) {
              theta_start += 360;
         }
          int x[] = new int[3];
          int y[] = new int[3];

          x[0] = previous.x;
          y[0] = previous.y;
          x[1] = current.x;
          y[1] = current.y;
          x[2] = next.x;
          y[2] = next.y;

          Polygon p = new Polygon( x, y, 3 );

          boolean clockwise = true;
          if ( GeometryUtil.relativeCCW( previous, current, next ) == -1 ){
              clockwise = false;
          }
 
          int arc_angle = (int)(GeometryUtil.cosTheoremAngle(
                                       previous,
                                       current,
                                       next )/Math.PI*180);
          if ( clockwise ) {
              arc_angle = -arc_angle;
          } else {
//                System.out.println( "right");
          }

          if ( !p.contains( reference_point.x, reference_point.y) ) {
//                   System.out.println( "Outside..." );
                 arc_angle = arc_angle - 360;
          }
 
//          System.out.println( "Arc angle   =        " + arc_angle   );    
//          System.out.println( "Theta start =        " + theta_start );

        graphics.setColor( Color.red );
        graphics.fillArc( current.x-15, current.y-15, 30, 30,
                          180-theta_start,  arc_angle );
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
        Polygon p  = object.getPolygonalBounds();
//         Graphics g = Tablet.debug_graphics;
//          System.out.println( "Polygon.touches called" );
        int next_i = 0;
        int next_j = 0;
        
        for ( int i=0; i<npoints-1; i++ ) {
            next_i = i+1;
            for ( int j=0; j<p.npoints-1; j++ ) {
                    next_j = j+1;
//                     g.setColor( Color.black );
//                     g.drawLine( xpoints[i],      ypoints[i],
//                                 xpoints[next_i], ypoints[next_i] );
//                     g.setColor( Color.yellow );
//                     g.drawLine( p.xpoints[j],      p.ypoints[j], 
//                                 p.xpoints[next_j], p.ypoints[next_j] );
//                      g.setColor( Color.black );
//                      g.drawLine( xpoints[i],      ypoints[i],
//                                  xpoints[next_i], ypoints[next_i] );
//                      g.setColor( Color.yellow );
//                      g.drawLine( p.xpoints[j],      p.ypoints[j], 
//                                  p.xpoints[next_j], p.ypoints[next_j] );
                if ( Line.linesIntersect( xpoints[i],        ypoints[i],
                                          xpoints[next_i],   ypoints[next_i],
                                          p.xpoints[j],      p.ypoints[j],
                                          p.xpoints[next_j], p.ypoints[next_j] ) ) {
                    
                    
//                      System.out.println( "Polygon.touches returned true" );
                    return true;
                }
            }
        }
        
//          System.out.println( "Polygon.touches returned false" );
        return false;
    }


    /**
    *
    * Supplied for completeness. Returns the bounding box with the
    * smallest perimeter for steps = 9 
    *
    **/
    public java.awt.Rectangle
    getHorizontalBounds()
    {
	return super.getBounds();
//         double x_min = xpoints[0];
//         double y_min = ypoints[0];
//         double x_max = xpoints[0];
//         double y_max = ypoints[0];
//         for ( int i=1; i<npoints; i++ ) {
//             if ( xpoints[i] < x_min )
//                 x_min = xpoints[i];
//             if ( ypoints[i] < y_min )
//                 y_min = ypoints[i];
//             if ( xpoints[i] > x_max )
//                 x_max = xpoints[i];
//             if ( ypoints[i] > y_max )
//                 y_max = ypoints[i];
//         }
//         if ( Tablet.debug ) {
//             System.out.println( "getHorizontalBounds() " +
//                 "( " + ((int)x_min)         + ", "  + ((int)y_min) + " ) " +
//                 "w " + ((int)(x_max-x_min)) + " h " + ((int)(y_max-y_min)) );
//         }
//         return new java.awt.Rectangle( (int)x_min,         (int)y_min, 
//                                        (int)(x_max-x_min), (int)(y_max-y_min) );
    }
    
    
    /**
    *
    * Supplied for completeness. Returns the bounding box with the
    * smallest perimeter for steps = 9 
    *
    **/
    public Rectangle
    getRectangularBounds()
    {
        return getRectangularBounds( 72 );
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
        if ( this.touches( object ) ) {
            return false;
        }
        
        Polygon p = object.getPolygonalBounds();
        
        for ( int i=0; i<p.npoints; i++ ) {
            if ( !this.contains( p.xpoints[i], p.ypoints[i] ) )
                return false;
        }
        
        return true;
    }


    /**
    *
    * Supplied for completeness. Returns the bounding box with the
    * smallest perimeter for step angles of Math.PI/(2*steps)
    * Should be optimized further. Should always return a Rectangle
    * with an angle between 0.0 and Math.PI/2
    *
    **/
    public Rectangle
    getRectangularBounds( int steps )
    {
        Color colors[] = new Color[10];
        
        colors[0] = Color.red;
        colors[1] = Color.green;
        colors[2] = Color.blue;
        colors[3] = Color.black;
        colors[4] = Color.orange;
        colors[5] = Color.cyan;
        colors[6] = Color.gray;
        colors[7] = Color.magenta;
        colors[8] = Color.white;
        colors[9] = Color.pink;
        
        double  step_angle = Math.PI/(2*steps);
        Polygon copy       = new Polygon(this);
                
        java.awt.Rectangle bounds = getHorizontalBounds();
        int min_index = 0;
        int min_sum   = bounds.width + bounds.height;
        
        Rectangle result  = new Rectangle( getHorizontalBounds() );
        for ( int i=1; i<steps; i++ ) {
            copy   = new Polygon( this );
            copy.rotate( -step_angle*i ); 
            bounds = copy.getHorizontalBounds();

            if ( bounds.width + bounds.height < min_sum ) {
                min_sum   = bounds.width + bounds.height;
                min_index = i;
            }
        }
        copy   = new Polygon( this );
        copy.rotate( -step_angle*min_index ); 
        bounds = copy.getHorizontalBounds();
        result = new Rectangle( bounds );
        result.rotateAbout( new Point( xpoints[0], ypoints[0] ), 
                            (min_index)*step_angle );
        return result;
    }


    /**
    *
    * This method returns the nth edge of this Polygon.
    *
    **/
    public Line
    getEdge( int n )
    {
        return new Line( xpoints[n], ypoints[n], xpoints[n+1], ypoints[n+1] );
    }

  /**
   *
   * get the nth angle as a positive value between 0 and 2*PI
   *
   **/
  public double getAngle( int n )
  {
    return GeometryUtil.cosTheoremAngle(new Point(xpoints[n],
                          ypoints[n]),
                    new Point(xpoints[(n+1)%npoints],
                          ypoints[(n+1)%npoints]),
                    new Point(xpoints[(n+2)%npoints],
                          ypoints[(n+2)%npoints]));
    
  }
  
    /**
    *
    * This method should return a polygon that fully contains the 
    * current object. The polygon is open. In order to close it,
    * the last point should be the same as the first one.
    *
    **/
    public Polygon 
    getPolygonalBounds()
    {
        return this;
    }


    /**
    *                            
    * This method rotates the polygon by theta radians CCW.
    *                            
    **/
    public void
    rotateAboutOrigin( double theta )
    {
        Point  p      = new Point();
        double radius = 0.0;
        double angle  = 0.0;
        // Rotate about the origin
        for ( int i=0; i<npoints; i++ ) {
            p.x           = xpoints[i];
            p.y           = ypoints[i];
            radius        = Math.sqrt( p.x*p.x + p.y*p.y );
            if ( !((p.x == 0.0) && (p.y == 0.0)) ) {
                angle      = Math.atan2( p.y, p.x ) + theta;
                xpoints[i] = (int)(Math.cos( angle ) * radius);
                ypoints[i] = (int)(Math.sin( angle ) * radius);
            }
        }
    }


    /**
    *                            
    * This method rotates the polygon by theta radians CCW.
    *                            
    **/
    public void
    rotateAboutCOM( double theta )
    {
        double center_x = 0.0;
        double center_y = 0.0;
        
        for ( int i=0; i<npoints; i++ ) {
            center_x += xpoints[0];
            center_y += xpoints[0];
        }
        center_x /= npoints;
        center_y /= npoints;
        
        double radius   = 0.0;
        double angle    = 0.0;
        Point  p        = new Point();
        
        // Center at the COM
        translate( -center_x, -center_y );
        
        // Rotate about the zeroth point 
        for ( int i=0; i<npoints; i++ ) {
            p.x           = xpoints[i];
            p.y           = ypoints[i];
            radius        = Math.sqrt( p.x*p.x + p.y*p.y );
            if ( !((p.x == 0.0) && (p.y == 0.0)) ) {
                angle    = Math.atan2( p.y, p.x );
                xpoints[i] = (int)(Math.cos( angle + theta ) * radius);
                ypoints[i] = (int)(Math.sin( angle + theta ) * radius);
            }
        }
        
        // Translate to the old coordinate system
        translate( center_x, center_y );
    }


    /**
    *                            
    * This method rotates the polygon by theta radians CCW.
    *                            
    **/
    public void
    rotate( double theta )
    {
	if (npoints < 1) return;
	
	// (Added by Olya Veselova, Mar 1 02')
	// Trying to avoid precision errors, though I am not totally sure this is better

        // Center at the zeroth point 
        int oX = xpoints[0];
        int oY = ypoints[0];
	translate( -(double)oX, -(double)oY);
	
        // Rotate about the zeroth point 
        for ( int i=0; i<npoints; i++ ) {
	    Point p = new Point(xpoints[i], ypoints[i]);
            xpoints[i] = (int)Math.round(p.x * Math.cos(theta) - p.y * Math.sin(theta));
            ypoints[i] = (int)Math.round(p.x * Math.sin(theta) + p.y * Math.cos(theta));	    
        }

	translate( (double)oX, (double)oY);

	// Older version
//         double  radius = 0.0;
//         double  angle  = 0.0;
//         Point   p      = new Point();
        
//         // Center at the zeroth point 
//         int xmin = xpoints[0];
//         int ymin = ypoints[0];
//         translate( -(double)xmin, -(double)ymin );
        
//         // Rotate about the zeroth point 
//         for ( int i=0; i<npoints; i++ ) {
//             p.x           = xpoints[i];
//             p.y           = ypoints[i];
//             radius        = Math.sqrt( p.x*p.x + p.y*p.y );
//             if ( !((p.x == 0) && (p.y ==0)) ) {
//                 angle    = Math.atan2( p.y, p.x );
//                 xpoints[i] = (int)(Math.cos( angle + theta ) * radius);
//                 ypoints[i] = (int)(Math.sin( angle + theta ) * radius);
//             }
//         }
        
//         // Translate to the old coordinate system
//         translate( (double)xmin, (double)ymin );
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
    m_vertices = new Vertex[pts.length];
    for ( int i = 0; i < pts.length; i++ ) {
      m_vertices[i] = pts[i];
    }
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
    * Returns an array containing the angles that each edge makes
    * with the x axis.
    *
    **/
    public double[]
    getAbsoluteAngles()
    {
        double angles[] = new double[npoints-1];
        
        for ( int i=0; i<npoints-1; i++ ) {
            angles[i] = Math.atan2( ypoints[i+1] - ypoints[i],
                                    xpoints[i+1] - xpoints[i] );
        }
        
        return angles;
    }
    
     
    /**
    *
    * Returns the polygon length.
    *
    **/
    public double 
    getPolygonLength()
    {
        double arc_length = 0;
        
        for ( int i=0; i<npoints-1; i++ ) {
	    arc_length += Math.sqrt( 
	        (ypoints[i+1] - ypoints[i])*(ypoints[i+1] - ypoints[i]) +
		(xpoints[i+1] - xpoints[i])*(xpoints[i+1] - xpoints[i]) );
        }
        
        return arc_length;
    }

   
    /**
    *
    * Returns an array containing the angles that each edge makes
    * with the x axis.
    *
    **/
    public double[]
    getAbsolutePositiveAngles()
    {
        double angles[] = getAbsoluteAngles();
        
        for ( int i=0; i<angles.length; i++ ) {
            if ( angles[i] < 0 ) {
                angles[i] += Math.PI;
            }
        }
        
        return angles;
    }


    /**
    *
    * Try combining the input object with this Polygon. Return true
    * is successful, false O/W.
    *
    * The input object may be a Line or a Polygon
    *
    * Two Polygons are combinable if their end points are 
    * sufficiently close to each other (determined by the tolerance).
    *
    **/
    public boolean
    tryCombining( Object o, int tolerance )
    {
        if ( o instanceof Line ) {
            return tryCombining( new Polygon( ((Line)o) ), tolerance );
        }
    
        if ( o instanceof Polygon ) {
            Polygon polygon = (Polygon)o;
            
            Polygon tmp_points = new Polygon();            
            
            if ( Point.distance( xpoints[npoints-1], 
                                 ypoints[npoints-1],
                                 polygon.xpoints[0], 
                                 polygon.ypoints[0] ) <
                 tolerance ) {
//                  System.out.println( "In combine polygons 1");
                int tmp_xpoints[] = new int[ npoints+polygon.npoints-1 ];
                int tmp_ypoints[] = new int[ npoints+polygon.npoints-1 ];

                // Take care of the actual points
                for ( int i=0; i<npoints; i++ ) {
                    tmp_xpoints[i] = xpoints[i];
                    tmp_ypoints[i] = ypoints[i];
                }
                for ( int i=1; i<polygon.npoints; i++ ) {
                    tmp_xpoints[npoints+i-1] = polygon.xpoints[i];
                    tmp_ypoints[npoints+i-1] = polygon.ypoints[i];
                }
                
		if( points != null && polygon.points != null) {
		  // Take care of the original points.
		  for ( int i=0; i<polygon.points.npoints; i++ ) {
                    points.addPoint( polygon.points.xpoints[i], 
                                     polygon.points.ypoints[i] );
		  }
                }
                
		npoints += polygon.npoints - 1;
                this.xpoints = tmp_xpoints;
                this.ypoints = tmp_ypoints;
//                    System.out.println( "Combine polygons 1");
                
                return true;
            }


            if ( Point.distance( xpoints[0],
                                 ypoints[0],
                                 polygon.xpoints[polygon.npoints-1],
                                 polygon.ypoints[polygon.npoints-1] ) <
                 tolerance ) {
//                  System.out.println( "In combine polygons 2");
                int tmp_xpoints[] = new int[ npoints+polygon.npoints-1 ];
                int tmp_ypoints[] = new int[ npoints+polygon.npoints-1 ];
                 
                // Take care of the actual points
                for ( int i=0; i<polygon.npoints; i++ ) {
                    tmp_xpoints[i] = polygon.xpoints[i];
                    tmp_ypoints[i] = polygon.ypoints[i];
                }
                for ( int i=1; i<npoints; i++ ) {
                    tmp_xpoints[polygon.npoints+i-1] = xpoints[i];
                    tmp_ypoints[polygon.npoints+i-1] = ypoints[i];
                }

		if( points != null && polygon.points != null) {
		  // Take care of the original points.
		  for ( int i=0; i<polygon.points.npoints; i++ ) {
                    tmp_points.addPoint( polygon.points.xpoints[i], 
                                         polygon.points.ypoints[i] );
		  }
		  for ( int i=0; i<points.npoints; i++ ) {
                    tmp_points.addPoint( points.xpoints[i], 
                                         points.ypoints[i] );
		  }
		  points = tmp_points;
		}
                
                npoints += polygon.npoints - 1;
                this.xpoints = tmp_xpoints;
                this.ypoints = tmp_ypoints;
//                    System.out.println( "Combine polygons 2");
                
                return true;
            }
            
            
            if ( Point.distance( xpoints[npoints-1],
                                 ypoints[npoints-1],
                                 polygon.xpoints[polygon.npoints-1],
                                 polygon.ypoints[polygon.npoints-1] ) <
                 tolerance ) {
                
//                  System.out.println( "In combine polygons 3");
                int tmp_xpoints[] = new int[ npoints+polygon.npoints-1 ];
                int tmp_ypoints[] = new int[ npoints+polygon.npoints-1 ];
                 
                // Take care of the actual points
                for ( int i=0; i<polygon.npoints-1; i++ ) {
                    tmp_xpoints[i] = polygon.xpoints[i];
                    tmp_ypoints[i] = polygon.ypoints[i];
                }
                for ( int i=0; i<npoints; i++ ) {
                    tmp_xpoints[polygon.npoints+i-1] = xpoints[npoints-i-1];
                    tmp_ypoints[polygon.npoints+i-1] = ypoints[npoints-i-1];
                }
                
		if( points != null && polygon.points != null) {

		  // Take care of the original points.
		  for ( int i=0; i<points.npoints; i++ ) {
                    tmp_points.addPoint( points.xpoints[i],
                                         points.ypoints[i] );
		  }
		  for ( int i=polygon.points.npoints-1; i>=0; i-- ) {
                    tmp_points.addPoint( polygon.points.xpoints[i],
                                         polygon.points.ypoints[i] );
		  }
		  points = tmp_points;
		}
		
                npoints += polygon.npoints - 1;
                this.xpoints = tmp_xpoints;
                this.ypoints = tmp_ypoints;
//                    System.out.println( "Combine polygons 3");
                
                return true;
            }


            if ( Point.distance( xpoints[0],
                                 ypoints[0],
                                 polygon.xpoints[0],
                                 polygon.ypoints[0] ) <
                 tolerance ) {
//                  System.out.println( "In combine polygons 4");
                int tmp_xpoints[] = new int[ npoints+polygon.npoints-1 ];
                int tmp_ypoints[] = new int[ npoints+polygon.npoints-1 ];
                 
                // Take care of the actual points
                for ( int i=0; i<npoints-1; i++ ) {
                    tmp_xpoints[i] = xpoints[npoints-i-1];
                    tmp_ypoints[i] = ypoints[npoints-i-1];
                }
                for ( int i=0; i<polygon.npoints; i++ ) {
                    tmp_xpoints[npoints+i-1] = polygon.xpoints[i];
                    tmp_ypoints[npoints+i-1] = polygon.ypoints[i];
                }

		if( points != null && polygon.points != null) {
		
		  // Take care of the original points.
		  for ( int i=points.npoints-1; i>=0; i-- ) {
                    tmp_points.addPoint( points.xpoints[i],
                                         points.ypoints[i] );
		  }
		  for ( int i=0; i<polygon.points.npoints; i++ ) {
                    tmp_points.addPoint( polygon.points.xpoints[i],
                                         polygon.points.ypoints[i] );
		  }
		  points = tmp_points;
		}
		
                npoints += polygon.npoints - 1;
                this.xpoints = tmp_xpoints;
                this.ypoints = tmp_ypoints;
//                  System.out.println( "Combine polygons 4");

                return true;
            }
        }
        
        return false;
    }
        
    
    /**
    * 
    * Combines the ends of the polygon if they are sufficiently close.
    * 
    **/
    public void
    combineEndPoints( double error )
    {
        if ( npoints < 3 )
            return;
        
        int l = npoints-1;
            
        if ( ( ( xpoints[0]-xpoints[l] ) * ( xpoints[0]-xpoints[l] ) +
               ( ypoints[0]-ypoints[l] ) * ( ypoints[0]-ypoints[l] ) ) < 
               error*error ) {
            xpoints[l] = xpoints[0];
            ypoints[l] = ypoints[0];
        }
    }


    /**
    *
    * Return the major angles in this polygon
    *
    **/
    public double[]
    getMajorAngles()
    {
        double  angles[]        = getAbsolutePositiveAngles(); 
        double  window_size     = Tablet.window_width;
        double  dx              = Math.PI/180;
        double wrapped_angles[] = new double[angles.length*2];

        int j = 0;
        for ( j = 0;             j<angles.length;         j++ )
            wrapped_angles[j] = angles[j];
        for ( j = angles.length; j<wrapped_angles.length; j++ )
            wrapped_angles[j] = angles[j%angles.length] - Math.PI;
        

        int histogram[] = StatisticsModule.getSlidingWindowHistogram( 
                wrapped_angles, 
                -window_size,
                Math.PI+window_size, 
                dx,
                window_size );
        
        double  new_angles[]    = new double[npoints];
        boolean incremented     = true;
        int     index           = 0;
        int     n               = 0;
        int     distinct_angles = 0;
        for ( int i=0; i<histogram.length; i++ ) {
            if ( ( histogram[i] == 0 ) && ( !incremented ) ) {
                new_angles[index] /= n; // Take the average of angles
                
                if ( Tablet.debug ) {
                    System.out.println( "index = "       + index + "\t" +
                                        " final value  " + new_angles[index] );
                }
                index++;
                incremented = true;
                n           = 0;
                distinct_angles++;
            }
            if ( histogram[i] != 0 ) {
                n += histogram[i]; // Increment number of angles in the window
                new_angles[index] += (dx*i - window_size)*histogram[i];
                if ( Tablet.debug ) {
                    System.out.println( "index = " + index + "\t" +
                                        " adding " + dx*i );
                }
                incremented = false;
            }
        }
        
        if ( Tablet.debug )
            System.out.println( "Final angles : " );
        double angle_set[] = new double[distinct_angles];
        for ( int i=0; i<distinct_angles; i++ ) {
            if ( Tablet.debug )
                System.out.println( " i = " + i + " angle = " + new_angles[i] );
            angle_set[i] = new_angles[i];
        }
        
        return angle_set;
    }


    /**
    *
    * Normalize the polygon edges so that the angles they make 
    * with the x axis are chosen from the given angle set.
    *
    **/
    public void
    normalize( double angle_set[] )
    {
        int    new_xpoints[] = new int[npoints];
        int    new_ypoints[] = new int[npoints];
    
        int    last_processed_vertex_index = 0;
        double closest_angle               = 0.0;
        double current_point_x              = xpoints[0];
        double current_point_y             = ypoints[0];
        Line   edge;
        Line   next_edge;
        Line   line1;
        Line   line2;
        Point  intersection;
        for ( int i=0; i<npoints-2; i++ ) {
            edge          = getEdge( i );
            next_edge     = getEdge( i+1 );
            
            closest_angle = edge.chooseApproximateAngle( angle_set );
            
            line1         = new 
                Line( current_point_x-Math.cos( closest_angle )*1000,
                      current_point_y-Math.sin( closest_angle )*1000,
                      current_point_x+Math.cos( closest_angle )*1000,
                      current_point_y+Math.sin( closest_angle )*1000 );
                      
            current_point_x = ( next_edge.x1 + next_edge.x2 )/2; // Midpoint
            current_point_y = ( next_edge.y1 + next_edge.y2 )/2; // of next edge

            closest_angle = next_edge.chooseApproximateAngle( angle_set );
            line2         = new 
                Line( current_point_x-Math.cos( closest_angle )*1000,
                      current_point_y-Math.sin( closest_angle )*1000,
                      current_point_x+Math.cos( closest_angle )*1000,
                      current_point_y+Math.sin( closest_angle )*1000 );

            // Tablet.debug_graphics.setColor( Color.green.darker().darker() );            
            // line1.paint( Tablet.debug_graphics );
            // line2.paint( Tablet.debug_graphics );
            
            try {
                intersection = line1.getIntersection( line2 );
            } catch( Exception e ) {
                
                last_processed_vertex_index++;
                new_xpoints[last_processed_vertex_index] = (int)edge.x2;
                new_ypoints[last_processed_vertex_index] = (int)edge.y2;
                System.err.println( "error in finding intersection " +
                                    "in Polygon.normalize()" );
                continue;
            }
            Tablet.debug_graphics.setColor( Color.red );
            intersection.paint( Tablet.debug_graphics );
            
            last_processed_vertex_index++;
            new_xpoints[last_processed_vertex_index] = intersection.x;
            new_ypoints[last_processed_vertex_index] = intersection.y;
        }
            new_xpoints[0]         = xpoints[0];
            new_ypoints[0]         = ypoints[0];
            new_xpoints[npoints-1] = xpoints[npoints-1];
            new_ypoints[npoints-1] = ypoints[npoints-1];
            xpoints = new_xpoints;
            ypoints = new_ypoints;
    }

  public final GeometricObject copy() 
  {
    Polygon polygon = new Polygon( this );
    polygon.time_stamp = time_stamp;
    if( points != null ) {
      polygon.points = (Polygon)points.copy();
    }
    return polygon;
  }
  
}


/** 
  * 
  * $Log: Polygon.java,v $
  * Revision 1.16  2003/09/08 20:52:11  moltmans
  * Added a distance to point method.
  *
  * Revision 1.15  2003/06/26 19:57:14  calvarad
  * Lots of bug fixes
  *
  * Revision 1.14  2003/05/07 20:25:47  mtsezgin
  *
  * Fixed control M problems, and also added a few utility methods.
  *
  * Revision 1.13  2003/03/06 01:08:49  moltmans
  * Added copyright to all the files.
  *
  * Revision 1.12  2003/01/06 21:36:20  hammond
  * missed a println
  *
  * Revision 1.11  2003/01/06 18:28:38  hammond
  * Polygon can now access ranges for original data points for particular lines
  *
  * Revision 1.10  2002/11/25 02:43:26  hammond
  * made ranges accessible
  *
  * Revision 1.9  2002/10/31 20:11:35  olyav
  * Added a way in Ellipse to ask for the line that is that ellipse's horizontal or vertical axis.
  *
  * Revision 1.8  2002/08/14 18:35:56  moltmans
  * Added a copy method to Geometric primitives so that we can make deep
  * copies of objects.  The copy method is careful to not reuse any data.
  *
  * Revision 1.7  2002/08/06 15:37:30  mtsezgin
  * Added static serialVersionUID information to the serializable geometric objects,so that serialization doesn't break unless modifications made to the class are truly incompatible changes.
  *
  * Revision 1.6  2002/07/23 21:00:06  moltmans
  * Commented out some debug code that was throwing errors.
  *
  * Revision 1.5  2002/04/01 23:51:06  moltmans
  * Updated some small bugs in Polygon,  having to do with accessing null
  * original_points, and added some thresholded polygon parsing to filter
  * out small edges.
  *
  * Revision 1.4  2001/11/23 03:23:31  mtsezgin
  * Major reorganization.
  *
  * Revision 1.3  2001/04/12 19:25:55  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.2  2001/04/03 16:26:54  moltmans
  * Added ability to get the n-th angle from a polygon.
  *
  * Revision 1.1.1.1  2001/03/29 16:25:00  moltmans
  * Initial directories for DRG
  *
  * Revision 1.25  2000/09/06 22:40:52  mtsezgin
  * Combinations of curves and polygons are successfully approximated
  * by straight lines and Bezier curves as appropriate. System works
  * quite reliably.
  *
  * Revision 1.19  2000/06/08 03:14:31  mtsezgin
  *
  * Made the class Serializable for supporting saving and loading
  * designs. Both the object attributes, and the original data points
  * are stored and restored.
  *
  * Revision 1.18  2000/06/03 01:52:34  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.17  2000/06/02 21:11:15  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.16  2000/05/26 20:46:19  mtsezgin
  *
  * Normalization method is modified to use weights in the histogram
  * for determining the angle set.
  *
  * Revision 1.15  2000/05/24 01:53:21  mtsezgin
  *
  * The polygon angle normalization works reliably.
  *
  * Revision 1.14  2000/05/22 02:42:34  mtsezgin
  *
  * The current version enables polygons to be sketched in pieces.
  *
  * Revision 1.13  2000/05/03 23:26:46  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.12  2000/04/28 04:45:04  mtsezgin
  *
  * Now each GeometricObject keeps the mouse input that was previously
  * discarded. User can switch between seeing the recognized mode and
  * the raw mode. setDataPoints( Polygon points ) and getDataPoints()
  * are added to GeometricObject, and all the implementors are modified
  * accordingly.
  *
  * Revision 1.11  2000/04/25 22:19:27  mtsezgin
  *
  * The getBounds changed to getRectangularBounds which returns a Rectangle.
  *
  * Revision 1.10  2000/04/20 04:29:51  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.9  2000/04/17 07:02:31  mtsezgin
  *
  * Finally made the Rectangle really rotatable.
  *
  * Revision 1.8  2000/04/13 06:24:08  mtsezgin
  *
  * The current version of the program recognized Crosses, and Shades.
  * Implementors of Terminal and their descendants were modified to
  * implement the changes in GeometricObject.
  *
  * Revision 1.7  2000/04/11 01:41:46  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.6  2000/04/11 00:41:48  mtsezgin
  *
  * Now the whole package succesfully parses a motor.
  *
  * Revision 1.5  2000/04/07 04:28:54  mtsezgin
  *
  * Added Rotatable interface. Rectangle and Line are Rotatable for now, but
  * Rectangle should be modified to have an angle field. Also other rotatable
  * classes should also implement Rotatable interface.
  *
  * Revision 1.4  2000/04/06 21:33:43  mtsezgin
  *
  * Spatial relation represents 9 basic relations for now. It should be extended.
  *
  * GeometricObject is extended. Implementors extended with empty stubs.
  *
  * Revision 1.3  2000/04/06 19:16:24  mtsezgin
  *
  * Modified all the classes to use my Point class which extends java.awt.Point
  * instead of directly using java.awt.Point
  *
  * Revision 1.2  2000/04/01 22:51:22  mtsezgin
  *
  * Started implementation. Also from now on using jdk 1.2
  *
  * Revision 1.1.1.1  2000/04/01 03:07:07  mtsezgin
  * Imported sources
  *
  * Revision 1.2  2000/03/31 22:41:04  mtsezgin
  *
  * Started Log tracking.
  *
  *  
  **/

