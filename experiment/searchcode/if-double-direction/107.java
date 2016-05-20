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
// Cleaned -Metin

package edu.mit.sketch.toolkit;

  /**
  *
  * See the end of the file for the log of changes.
  *
  * $Author: moltmans $
  * $Date: 2003/11/05 01:42:02 $
  * $Revision: 1.18 $
  * $Headers$
  * $Id: StrokeData.java,v 1.18 2003/11/05 01:42:02 moltmans Exp $
  * $Name:  $
  * $Locker:  $
  * $Source: /projects/drg/CVSROOT/drg/code/src/edu/mit/sketch/toolkit/StrokeData.java,v $
  *
  **/





import edu.mit.sketch.geom.Ellipse;
import edu.mit.sketch.geom.GeometryUtil;
import edu.mit.sketch.geom.Point;
import edu.mit.sketch.geom.PolarPoint;
import edu.mit.sketch.geom.Polygon;
import edu.mit.sketch.geom.Rectangle;
import edu.mit.sketch.geom.Vertex;
import edu.mit.sketch.ui.Tablet;
import edu.mit.sketch.util.AWTUtil;
import edu.mit.sketch.util.Gaussian;
import edu.mit.sketch.util.LinearFit;
import edu.mit.sketch.util.OrthogonalDistanceRegression;
import edu.mit.util.Assert;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.io.Serializable;
import java.util.Vector;
import edu.mit.sketch.util.Util;
import org.apache.log4j.Logger;
import edu.mit.sketch.system.Logging;

/**
  *
  * This class stores all the data from a single stroke.
  * Time array has the same length as points.
  *
  **/

public
class      StrokeData
implements Serializable
{
    private static Logger LOG = Logging.getSubLogger( Logging.TOOLKIT, "StrokeData" );

    /**
     * The vertices in this data set.
     **/
    public Vertex vertices[];


    /**
     * The absolute time stamps for the vertices.
     **/
    public long time[];
    
    /**
     * Time derivative of position
     **/
    public transient double speed[];


    /**
     * Time derivative of speed
     **/
    public transient double acceleration[];
    
    
    /**
     * Direction array. Length of this array is the same as the
     * length of the vertices array. Basically the direction (ie.
     * the angle of the tangent) at the last point in vertices is 
     * just the same as that of the previous point. 
     **/
    public transient double d[];
    
    
    /**
     * Derivative of the direction array (d). The length of this 
     * array is one less than that of d.
     **/
    public transient double dd_dt[];

    
    /**
     * The accumulated length of the curve upto ith vertex.
     **/
    public transient double accumulated_length[];

    
    /**
     * Speed scale space information. The rightmost dimension indexes
     * the speed values at different scales, and the leftmost dimension 
     * indexes the scale parameter. Higher values correspond to coarser
     * scales.
     **/
    public transient double speed_scale_space[][];

    
    /**
     * Direction change scale space information. The rightmost dimension 
     * indexes the data values at different scales, and the leftmost 
     * dimension indexes the scale parameter. Higher values correspond 
     * to coarser scales.
     **/
    public transient double dd_dt_scale_space[][];
    

    /**
     * The gaussian filters.
     **/
    public transient Gaussian speed_gaussians[];
    public transient Gaussian dd_dt_gaussians[];

    /**
     * The vertices mapped to polar coordinates.
     **/
    public PolarPoint polar_points[];


    /**
     * Parameters and constants.
     **/
    public int        direction_window_width = 5;
    public int        fit_method             = LinearFit.ROTATION_METHOD;
    public double     test_line_scale        = 1.2;

    public Point      center;
    public Rectangle  bounding_box;
    public Dimension  radius;

    public boolean  v_is_valid                  = false;
    public boolean  a_is_valid                  = false;
    public boolean  d_is_valid                  = false;
    public boolean  dd_dt_is_valid              = false;
    public boolean  polar_points_is_valid       = false;
    public boolean  accumulated_length_is_valid = false;
    public boolean  dd_dt_scale_space_is_valid  = false;
    public boolean  speed_scale_space_is_valid  = false;


  /**
    *
    * The constructor.
    *
    **/
    public StrokeData( Vertex vertices[] )
    {
      
        int length = vertices.length;
        
        time     = new long[length];
        this.vertices = vertices;

//      Assert.isTrue( vertices[0].getTimeStamp() == 0.0,
//                 "The timestamps of the vertices should always start with zero," +
//                 "or we need to subtract off the inital time from all the timestamps.");
    
        for ( int i=0; i<length; i++ ) {
            time[i]     = vertices[i].getTimeStamp();
        }
    }


    /**
    *
    * The constructor.
    *
    **/
    public StrokeData( Point points[] )
    {
        int length = points.length;
        
        time     = new long[length];
        vertices = new Vertex[length];
        
        for ( int i=0; i<length; i++ ) {
      time[i]     = points[i].time_stamp; // - points[0].time_stamp;
            vertices[i] = new Vertex( points[i] );
            vertices[i].setIndex( i );
            vertices[i].setTimeStamp( time[i] );
        }
    }
    
    
    /**
     *
     * Derive speed, and fill in the speed array.
     * This method should cache data in order to save computation.
     *
     **/
    public void
    deriveProperties()
    {
        if ( Tablet.very_quiet ) {
            invalidateCaches();
            deriveSpeed();
            deriveAcceleration();
            derive_d( fit_method );
            derive_dd_dt();
            derive_accumulated_length();
        } else {
            System.out.println( "Deriving properties : " );
            invalidateCaches();
            System.out.println( "Deriving speed... " );
            deriveSpeed();
            System.out.println( "Deriving acceleration... " );
            deriveAcceleration();
            System.out.println( "Deriving direction... " );
            derive_d( fit_method );
            System.out.println( "Deriving change in direction... " );
            derive_dd_dt();
            System.out.println( "Deriving accumulated length... " );
            derive_accumulated_length();
            System.out.println( "Deriving properties done " );
        }
    }
    
    
    /**
     *
     * Derive speed, and fill in the speed array.
     * This method should cache data in order to save computation.
     *
     **/
    public void
    deriveScaleSpaces()
    {
        System.out.println( "Deriving speed scale space..." );
        derive_speed_scale_space();
        System.out.println( "Deriving dd_dt scale space..." );
        derive_dd_dt_scale_space();
    }
    

    /**
    *
    * Invalidates caches 
    *
    **/
    public void
    invalidateCaches() 
    {
        v_is_valid                  = false;
        a_is_valid                  = false;
        d_is_valid                  = false;
        dd_dt_is_valid              = false;
        polar_points_is_valid       = false;
        accumulated_length_is_valid = false;
        dd_dt_scale_space_is_valid  = false;
        speed_scale_space_is_valid  = false;
    }
    
    
    /**
     *
     * Return the vertices array.
     *
     **/
    public Vertex[]
    getVertices()
    {
        return vertices;
    }
    
    
    /**
     *
     * Derive the polar coordinates of the vertices with respect to
     * the center. Also determine the upper_right and lower 
     * right points (for surrounding rectangle).
     *
     **/
    public void
    derivePolarCoordinates()
    {
        if ( polar_points_is_valid )     // Don't do anything if the 
            return;                        // cached value is valid
        
        int x;
        int y;
        
        deriveBoundingBox();

        Point lower_left  = new Point( (int)bounding_box.x, 
                                       (int)bounding_box.y );
        Point upper_right = new Point( (int)(bounding_box.x + 
                                       bounding_box.getWidth()),
                                       (int)(bounding_box.y + 
                                       bounding_box.getHeight()) );
        
        radius = new Dimension( (upper_right.x - lower_left.x)/2,
                                (upper_right.y - lower_left.y)/2 );
        
        center = new Point( (upper_right.x + lower_left.x)/2,
                            (upper_right.y + lower_left.y)/2 );

        polar_points = new PolarPoint[vertices.length];
        
        

        for ( int i=0; i<polar_points.length; i++ ) {
            x = ( vertices[i].x - center.x );
            y = ( vertices[i].y - center.y );
            polar_points[i] = new PolarPoint( Math.sqrt( x*x + y*y ),
                                              Math.atan2( y, x ) );
        }
        
        for ( int i=1; i<polar_points.length; i++ ) {
            if ( Math.abs( polar_points[i].theta - 
                           polar_points[i-1].theta ) > Math.PI ) {
                for ( int j=-10; j<11; j++ ) {
                    if ( Math.abs( polar_points[i].theta - 
                                   polar_points[i-1].theta   + 
                                   j*2*Math.PI) < Math.PI ) {
                        polar_points[i].theta += j*2*Math.PI;
                        break;
                    }
                }
            }
        }
        
        if ( Tablet.debug ) {
            for ( int i=0; i<polar_points.length; i++ ) {
            System.out.println( i + " " + polar_points[i] );
            }
        }

        polar_points_is_valid = true;
    }
  
    
    /**
     *
     * Derive the bounding box for the vertices.
     *
     **/
    public void
    deriveBoundingBox()
    {
        Point lower_left  = new Point( vertices[0] );
        Point upper_right = new Point( vertices[0] );
        
        for ( int i=0; i<vertices.length; i++ ) {
            if ( vertices[i].x < lower_left.x )
                lower_left.x = vertices[i].x;
                
            if ( vertices[i].y < lower_left.y )
                lower_left.y = vertices[i].y;
                
            if ( vertices[i].x > upper_right.x )
                upper_right.x = vertices[i].x;
                
            if ( vertices[i].y > upper_right.y )
                upper_right.y = vertices[i].y;
        }
        
        bounding_box = new Rectangle( lower_left.x,  
                                      lower_left.y,
                                      upper_right.x - lower_left.x,
                                      upper_right.y - lower_left.y );
    }

  public long getStartTime()
  {
    return time[0];
  }

  public long getEndTime()
  {
    return time[time.length - 1];
  }
  
  

    /**
     *
     * Derive speed, and fill in the speed array.
     * This method should cache data in order to save computation.
     *
     **/
    public void
    deriveSpeed()
    {
        if ( v_is_valid )     // Don't do anything if the 
            return;           // cached value is valid
    
        double  dx;
        double  dy;
        double  dt;
        
        speed = new double[vertices.length-1];
        for ( int i=0; i<vertices.length-1; i++ ) { 
            dx = vertices[i+1].x - vertices[i].x;
            dy = vertices[i+1].y - vertices[i].y;
            dt = time[i+1]       - time[i];
            speed[i] = Math.sqrt( dx*dx + dy*dy )/dt;
        }

        v_is_valid = true;
    }
    
    
    /**
     *
     * Derive acceleration, and fill in the acceleration array.
     *
     **/
    public void
    deriveAcceleration()
    {
        if ( a_is_valid )     // Don't do anything if the 
            return;           // cached value is valid

        deriveSpeed();
        acceleration = new double[speed.length-1];
        
        for ( int i=0; i<acceleration.length; i++ ) {
            acceleration[i] = (speed[i]-speed[i+1])/(( time[i+2] - time[i] )/2);
        }

        a_is_valid = true;
    }
    
    
    /**
     *
     * Derive direction, and fill in the d array.
     *
     **/
    public void
    derive_d( int method )
    {
        if ( d_is_valid )     // Don't do anything if the 
            return;           // cached value is valid
        
        if ( false && !Tablet.very_quiet ) {
            System.out.println( "Using " + 
                                LinearFit.fitMethodToString(fit_method) );
        }
        switch( method ) {
            case LinearFit.SIMPLE_TANGENTS_METHOD :
                double tmp_d[] = GeometryUtil.getIntermediateAngles( vertices );
                d = new double[tmp_d.length+1];
                for ( int i=0; i<tmp_d.length; i++ ) {
                    d[i] = tmp_d[i];
                }
                d[d.length-1] = tmp_d[tmp_d.length-1];
                break;
            
            case LinearFit.SWODR_METHOD :
                if ( Tablet.platform_is_windows9x ) {
                    d = deriveDirectionViaSWODR( vertices,
                                                 direction_window_width );
                } else {
                    System.out.println( "Fit method not available..." );
                    throw( new NullPointerException() );
                }
                break;
                
            case LinearFit.ROTATION_METHOD :
                d = deriveDirectionUsingRotationalSWODR(vertices, 
                                                        direction_window_width);
                break;

            default :
                System.out.println( "Error: unknown fit method " + method );
        }
        
        d_is_valid = true;
        return;
    }
    
    
    /**
     *
     * Find time derivative of direction, and fill in the dd_dt 
     * array.
     *
     **/
    public void
    derive_dd_dt()
    {
        if ( dd_dt_is_valid )     // Don't do anything if the 
            return;               // cached value is valid

        dd_dt = new double[d.length-1];
        for ( int i=0; i<d.length-1; i++ ) {
            dd_dt[i] = Math.abs( (d[i+1]-d[i])/vertices[i].distance( vertices[i+1] ) );
            //dd_dt[i] = (d[i+1]-d[i])/vertices[i].distance( vertices[i+1] );
        }
        
        // dd_dt = differantiateDirection( d );
        
        dd_dt_is_valid = true;
    }
    
    
    /**
     *
     * Compute the derive accumulated length.
     *
     **/
    public void
    derive_accumulated_length()
    {
        if ( accumulated_length_is_valid )  // Don't do anything if the 
            return;                         // cached value is valid
        
        accumulated_length = new double[vertices.length];
        
        accumulated_length[0] = 0.0;
        for ( int i=1; i<accumulated_length.length; i++ ) {
            accumulated_length[i] = accumulated_length[i-1] + 
                                    vertices[i-1].distance( vertices[i] );
        }

        accumulated_length_is_valid = true;
    }
    
    
    /**
     *
     * Compute the scale space data.
     *
     **/
    public void
    derive_dd_dt_scale_space()
    {
        if ( dd_dt_scale_space_is_valid )  // Don't do anything if the 
            return;                        // cached value is valid
        
        int depth = dd_dt.length*2;

        dd_dt_scale_space = new double[depth][dd_dt.length];
        
        dd_dt_gaussians    = new Gaussian[depth];
        dd_dt_gaussians[0] = new Gaussian( (dd_dt.length/6)*2+1, .005 );
        
        double abs_dd_dt[] = new double[dd_dt.length];
        
        for ( int i=0; i<dd_dt.length; i++ ) {
            abs_dd_dt[i] = dd_dt[i]; //Math.abs( dd_dt[i] );
        }


        dd_dt_scale_space[0] = abs_dd_dt;
        for ( int i=1; i<depth; i++ ) {
            dd_dt_gaussians[i]   = new Gaussian( (dd_dt.length/2)*2+1, i*.05 );
            dd_dt_scale_space[i] = dd_dt_gaussians[i].convolve( abs_dd_dt );
        }
        
        /*
        dd_dt_scale_space[0] = abs_dd_dt;
        System.out.println(new Gaussian( 5, .35 ));
        for ( int i=1; i<depth; i++ ) {
            dd_dt_gaussians[i] = new Gaussian( 5, .25 );
            dd_dt_scale_space[i] = dd_dt_gaussians[i].convolve( dd_dt_scale_space[i-1] );
        }
        */
        dd_dt_scale_space_is_valid = true;
    }
    
    
    /**
     *
     * Compute the scale space data.
     *
     **/
    public void
    derive_speed_scale_space()
    {
        if ( speed_scale_space_is_valid )  // Don't do anything if the 
            return;                        // cached value is valid
        
        int depth = speed.length*2;

        speed_scale_space  = new double[depth][speed.length];
        
        speed_gaussians    = new Gaussian[depth];
        speed_gaussians[0] = new Gaussian( (speed.length/6)*2+1, .005 );
        

        speed_scale_space[0] = speed;
        for ( int i=1; i<depth; i++ ) {
            speed_gaussians[i]     = new Gaussian( (dd_dt.length/2)*2+1, i*.05 );
            speed_scale_space[i] = speed_gaussians[i].convolve( speed );
        }
        /*
        int depth = 2000;

        speed_scale_space = new double[depth][speed.length];
        
        speed_gaussians    = new Gaussian[depth];
        speed_gaussians[0] = new Gaussian( (speed.length/6)*2+1, .002 );
        
        speed_scale_space[0] = speed;
        for ( int i=1; i<depth; i++ ) {
            speed_gaussians[i] = new Gaussian( (speed.length/6)*2+1, i*.002 );
            speed_scale_space[i] = speed_gaussians[i].convolve( speed );
        }
        */
        speed_scale_space_is_valid = true;
    }
    

  public Vertex[] filterVerticesDownToN( Vertex verts[], int num )
  {
    int indices[] = AWTUtil.getIndices( verts );
    return AWTUtil.makeVertices( filterVerticesDownToN(indices, num),
				 vertices );
				
  }

  public Vertex[] filterVerticesDownToN( Vertex verts[], int num,
					 double errorBound )
  {
    int inds[] = AWTUtil.getIndices( verts );
    int new_inds[] = filterVerticesDownToN( inds, num );
    Vertex index_points[] = new Vertex[new_inds.length];
      
    for ( int i = 0; i < new_inds.length; i++ ) {
      index_points[i] = vertices[new_inds[i]];
    }
    double err = AWTUtil.leastSquaresForPolygon( index_points, vertices );
    LOG.debug( "Error is " + err + " errorBound is " + errorBound );
    LOG.debug( "Propotional error is " + (err / vertices.length ) );
    if ( err < errorBound ) {
      return AWTUtil.makeVertices( new_inds, vertices ); 
    }
    else return null;
  }
  
  public int[] filterVerticesDownToN( int inds[], int num )
  {
    
    if ( inds.length <= num ) {
      return inds;
    }

    while ( inds.length > num ) { 
      double minchange = Double.MAX_VALUE;
      Vertex[] best = null;
      Vertex index_points[] = new Vertex[inds.length];
      
      for ( int i = 0; i < inds.length; i++ ) {
	index_points[i] = vertices[inds[i]];
      }
      
      double total_err =
	AWTUtil.leastSquaresForPolygon( index_points, vertices );

      for ( int i = 1; i < index_points.length-1; i++ ) {
	Vertex new_index_points[] = new Vertex[inds.length-1];
	for ( int j=0; j<new_index_points.length; j++ ) {
	  int k = 0;
	  if ( j < i ) {
	    k = j;
	  }
	  else {
	    k = j+1;
	  }
	  new_index_points[j] = index_points[k];
	}
	double new_err = AWTUtil.leastSquaresForPolygon( new_index_points,
							  vertices );
	double change = new_err - total_err;
	if ( change < minchange ) {
	  minchange = change;
	  best = new_index_points;
	}
      }
      inds = new int[best.length];
	
      for ( int j=0; j<best.length; j++ ) {
	  inds[j] = best[j].index;
      }
    }
    return inds;

  }
    /**
     *
     * filterCollinearVertices.
     *
     **/
  public Vertex[]
    filterVerticesByLSQE( Vertex vertices[], double percentTolerance )
    {
	return AWTUtil.makeVertices( 
	    filterVerticesByLSQE( 
	       AWTUtil.getIndices( vertices ), percentTolerance ), vertices );
    }
    
    /**
     *
     * filterCollinearVertices.
     *
     **/
    public int[]
    filterVerticesByLSQE( int indices[], double percentTolerance )
    {
        int result[];
        
        Vertex index_points[] = new Vertex[indices.length];
        
//          String indices_string = "";
        for ( int i=0; i<indices.length; i++ ) {
            index_points[i] = vertices[indices[i]];
//              indices_string += " " + vertices[indices[i]].index;
        }
//          System.out.println( "LSQE Filter: with indices " + indices_string );
        
        double total_LSQE = AWTUtil.leastSquaresForPolygon( index_points,
                                                            vertices );
        
        for ( int i=1; i<index_points.length-1; i++ ) {
            Vertex new_index_points[] = new Vertex[indices.length-1];
            for ( int j=0; j<new_index_points.length; j++ ) {
                int k = ( j<i ) ? j : j+1; 
                new_index_points[j] = index_points[k];
            }
            double new_LSQE = AWTUtil.leastSquaresForPolygon( new_index_points,
                                                              vertices );
//              System.out.println( "total_LSQE " +  total_LSQE );
//              System.out.println( "new_LSQE   " +  new_LSQE    );
            if ( new_LSQE < total_LSQE*percentTolerance  ) { 
                int new_indices[] = new int[new_index_points.length];
                
                for ( int j=0; j<new_index_points.length; j++ ) {
                    new_indices[j] = new_index_points[j].index;
                 }
                
//                  System.out.println( "Filtered vertex " + vertices[indices[i]].index );
                
                return filterVerticesByLSQE( new_indices, percentTolerance );
            }
        }
        
        return indices;
      }


    /**
     *
     * filterCollinearVertices.
     *
     **/
    public int[]
    filterCollinearVertices( int indices[] )
    {
        int result[];
        
        Vertex index_points[] = new Vertex[indices.length];

        for ( int i=0; i<indices.length; i++ ) {
            index_points[i] = vertices[indices[i]];
        }
        
        double angles[] = GeometryUtil.getIntermediateAngles( index_points );
        
        Vector filtered_indices = new Vector();
        filtered_indices.addElement( index_points[0] );
        for ( int i=0; i<angles.length-1; i++ ) {
            if ( Math.abs( angles[i] - angles[i+1] ) >
                Math.PI/32 ) {
                filtered_indices.addElement( index_points[i+1] );
            }
        }
        filtered_indices.addElement( index_points[index_points.length-1] );
        
        result = new int[filtered_indices.size()];

        for ( int i=0; i<filtered_indices.size(); i++ ) {
            result[i] = ((Vertex)(filtered_indices.elementAt(i))).index;
        }
        
        if ( result.length == indices.length ) {
            return result;
        } else {
//              System.out.println( "Filtered " + 
//                                ( indices.length - result.length ) + 
//                                " collinear points" );
        
            return filterCollinearVertices( result );
        }
     }
    
    
    /**
     *
     * Derive direction, and return it.
     * This method is different than the previous one in that here
     * we look at a collection of vertices rather than two vertices to
     * come up with the direction. Also use the rotation method.
     *
     **/
    public double[]
    deriveDirectionUsingRotationalSWODR( Point input[],
                                         int window_span )
    {
        double direction[] =
            OrthogonalDistanceRegression.deriveDirectionUsingRotationalSWODR(
                input,
                window_span );

        // GeometryUtil.continualizeDirection( direction );
        
        return direction;
    }
    
    
    /**
     *
     * Derive direction, and fill in the d array.
     * This method is different than the previous one in that here
     * we look at a collection of vertices rather than two vertices to
     * find the direction. Also this is supposed to work faster. 
     * Returns the direction array.
     *
     **/
    public double[]
    deriveDirectionViaSWODR( Point input[], int twice_window_size )
    {
        double x[] = new double[input.length];
        double y[] = new double[input.length];

        double direction[];
        double swodr_result[][];
        
        // Add some noise
        for ( int i=0; i<input.length; i++ ) {
            x[i] = input[i].x + (1-Math.random())*1.0;
            y[i] = input[i].y + (1-Math.random())*1.0;
        }
        swodr_result = OrthogonalDistanceRegression.doSlidingWindowODR(
            "input",
            "output",
             x,
             y,
             twice_window_size );
                           
        // In this scheme each point has a direction[i]
        direction = new double[swodr_result.length];
        for ( int i=0; i<direction.length; i++ ) {
                direction[i] = Math.atan2( swodr_result[i][0], 1 );
        }
        
        // GeometryUtil.continualizeDirection( direction );
        
        return direction;
    }
    
    
    /**
     *
     * Differantiate the direction array using the current timing
     * data.
     *
     **/
    public double[]
    differantiateDirection( double direction[] )
    {
        double derivative[] = new double[direction.length-1];
        
        double dt;
        double delta_theta;
        
        for ( int i=0; i<derivative.length; i++ ) {
            delta_theta   = direction[i+1] - direction[i];
            dt            = time[i+1] - time[i];
            derivative[i] = delta_theta/dt;
        }
        
        return derivative;
    }

    
    /**
     *
     * Average speed
     *
     **/
    public double
    averageSpeed()
    {
        double  dx;
        double  dy;
        double  dt;
        double  average = 0.0;
    
        for ( int i=0; i<vertices.length-1; i++ ) {
            dx = vertices[i+1].x - vertices[i].x;
            dy = vertices[i+1].y - vertices[i].y;
            average += Math.sqrt( dx*dx + dy*dy );
        }
        average /= (time[time.length-1] - time[0]);
    
        return average;
    }

    
    /**
     *
     * Average 
     *
     **/
    public double
    averageFinG( double f[], double g[] )
    {
        double  average = 0.0;
    
        for ( int i=1; ( i<f.length ) && ( i<g.length ); i++ ) {
            average += ( f[i] + f[i-1] )/2.0*( g[i] - g[i-1] );
        }
        average /= ( Math.abs( g[g.length-1] ) - Math.abs( g[0]) );
    
        return average;
    }

    
    /**
     *
     * Average 
     *
     **/
    public double
    averageAbsoluteFinG( double f[], double g[] )
    {
        double  average = 0.0;
    
        for ( int i=1; ( i<f.length ) && ( i<g.length ); i++ ) {
            average += Math.abs( ( f[i] + f[i-1] ) )/2.0*( g[i] - g[i-1] );
        }
        average /= ( Math.abs( g[g.length-1] ) - Math.abs( g[0]) );
    
        return average;
    }

    
    /**
     *
     * Average 
     *
     **/
    public double
    averageFinG( double f[], long g[] )
    {
        double g_copy[] = new double[g.length];
        for ( int i=0; i<g.length; i++ ) {
            g_copy[i] = g[i];
        }
    
        return averageFinG( f, g_copy );
    }

    
    /**
     *
     * Average 
     *
     **/
    public double
    averageAbsoluteFinG( double f[], long g[] )
    {
        double g_copy[] = new double[g.length];
        for ( int i=0; i<g.length; i++ ) {
            g_copy[i] = g[i];
        }
    
        return averageAbsoluteFinG( f, g_copy );
    }
    
    
    /**
     *
     * Put the valid points in the points array in a Polygon and
     * return it.
     *
     **/
    public Polygon
    getDataPoints()
    {
        Polygon result = new Polygon();
                
        for ( int i=0; i<vertices.length; i++ ) {
            result.addPoint( vertices[i].x, vertices[i].y );
        }
        
        return result;
    }


    /**
    *
    * See if the input is a polygon.
    *
    **/ 
    public Vertex[]
    getSpeedFit( double average_scale )
    {
        double average = 0.0;
        double scaled_average;

        Vector output_points = new Vector();
        
        deriveSpeed();
        average = averageSpeed();

        scaled_average = average*average_scale;       

        int     index     = 0;
        double  min_speed = speed[0];
        boolean below     = ( speed[0] < scaled_average );

        output_points.addElement( vertices[0] );
        for ( int i=0; i<speed.length; i++ ) { 
            if ( speed[i] > scaled_average ) {
                below = false;
            }
            if ( ( speed[i] < scaled_average ) && ( below == false ) ) {
                output_points.addElement( vertices[i] );
                min_speed = speed[i];
                below = true;
            }
            if ( below && ( speed[i] < min_speed ) ) {
                output_points.setElementAt( new Vertex( vertices[i] ),
                                            output_points.size() - 1 );
            }
        }
        output_points.addElement( vertices[vertices.length-1] );
        
        if ( !Tablet.very_quiet ) {
            System.out.println( output_points.size() + " edges detected." );
        }
        
        Vertex result[] = new Vertex[output_points.size()];
        
        for ( int i=0; i<output_points.size(); i++ ) {
            result[i] = new Vertex( ( (Vertex)output_points.elementAt( i ) ) );
        }
                    
        double maximum_speed = 0.0;
        
        for ( int i=1; i<result.length-1; i++ ) {
            if ( speed[result[i].index] > maximum_speed ) { 
                maximum_speed = speed[result[i].index];     
            }
        }
        for ( int i=1; i<result.length-1; i++ ) {
            result[i].certainty = 1 - speed[result[i].index]/maximum_speed;
        }
        
        result[0].certainty                 = 1.0;
        result[result.length - 1].certainty = 1.0;
        
        if ( Tablet.debug ) {
            System.out.println( "Final output polygon via speed" ); 
            for ( int i=0; i<result.length; i++ ) {
                System.out.println( "Speed certainty -> " + result[i] );
            }
        }

        if ( !Tablet.very_quiet ) {
            System.out.println( "Computed the speed fit." );         
        }

        return result;
    }


    /**
    * 
    * Find the corners of the polygon using the change in the 
    * direction. This is basically done by finding the peaks of the 
    * dd_dt array.
    *
    **/
    public Vertex[]
    getDirectionFit( double dd_dt_scale )
    {
        double dd_dt_average = averageAbsoluteFinG( dd_dt, time );

        if ( !Tablet.very_quiet ) {
            System.out.println( "Computed average" );
        }
        
        int    index            = 0;
        double max              = 0.0;
        double overall_max      = 0.0;
        double treshold         = dd_dt_average*dd_dt_scale;
        Vector result_vector    = new Vector();
        result_vector.addElement( new Vertex( vertices[0], 1.0 ) );
        
        if ( !Tablet.very_quiet ) {
            System.out.println( "Adding the first vertex to direction fit" );
        }
        
        for ( int i=0; i<dd_dt.length-1; i++ ) {
            if ( ( max < Math.abs( dd_dt[i] ) ) && 
                 ( Math.abs( dd_dt[i] ) >= treshold ) ) {
                max = Math.abs( dd_dt[i] );
                result_vector.set( index, new Vertex( vertices[i], max ) );
                overall_max = ( max > overall_max ) ? max : overall_max;
            }
            if ( ( Math.abs( dd_dt[i]   ) <  treshold ) &&
                 ( Math.abs( dd_dt[i+1] ) >= treshold ) ) {
                index++;
                max = Math.abs(dd_dt[i+1]);
                result_vector.addElement( new Vertex( vertices[i+1], max ) );
                overall_max = ( max > overall_max ) ? max : overall_max;
            }
        }
        if ( !Tablet.very_quiet ) {
            System.out.println( "Adding the endpoints vertex to direction fit" );
        }
        result_vector.addElement( new Vertex(vertices[vertices.length-1], 1.0));
        result_vector.insertElementAt( new Vertex( vertices[0], 1.0 ), 0 );
        
        Vertex result[] = new Vertex[result_vector.size()];

        double max_angle = 0.0;
        for ( int i=0; i<result.length; i++ ) {
            result[i]          = (Vertex)result_vector.elementAt( i );

            int current        = result[i].index;
            int left_neighbor  = ( ( current - 5 ) < 0 ) ? 
                                 0                     : 
                                 ( current - 5 );
            int right_neighbor = ( ( current + 5 ) >= vertices.length ) ? 
                                  vertices.length - 1                  : 
                                  ( current + 5 );
            result[i].certainty   = Math.abs( d[left_neighbor ] - 
                                              d[right_neighbor] );
            if ( max_angle < result[i].certainty )
                max_angle = result[i].certainty;
        }
        
        for ( int i=0; i<result.length; i++ ) {
            result[i].certainty /= max_angle;
        }

        if ( !Tablet.very_quiet ) {
            System.out.println( "Removing redundant points in direction fit");
        }
        result = removeRedundantEndPoints( result );
        if ( !Tablet.very_quiet ) {
            System.out.println("Removed redundant points in direction fit");
        }
        result[0].certainty               = 1.0;
        result[result.length-1].certainty = 1.0;
        
        if ( Tablet.debug ) {
            System.out.println( "Got " + index + " points using direction info" );
            for ( int i=0; i<result.length; i++ ) {
                System.out.println( "dd_dt certainty -> " + result[i] );
            }
        }
            
        return result;
    }
    
    
    /**
    *
    * See if the input is a line.
    *
    **/
    public boolean
    testLine( double test_line_scale )
    {
        double  dx;
        double  dy;
        double  length   = 0.0;
        double  distance = 0.0;
        
        for ( int i=0; i<vertices.length-1; i++ ) {
            dx = vertices[i+1].x - vertices[i].x;
            dy = vertices[i+1].y - vertices[i].y;

            length += Math.sqrt( dx*dx + dy*dy );
        }
        
        distance = vertices[0].distance( vertices[vertices.length-1] );
        
        if ( length < distance*test_line_scale ) {
            if ( !Tablet.very_quiet ) {
                System.out.println("That was definitely a line");
            }
            return true;
        } else {
            Rectangle bounds = (new Polygon( vertices )).getRectangularBounds();
            if ( bounds.getWidth() + bounds.getHeight() < 
                 accumulated_length[accumulated_length.length-1]/5 ) {
                if ( !Tablet.very_quiet ) {
                     System.out.println("Looked like an overtraced line");
                }
                return true;
            } else {
                if ( !Tablet.very_quiet ) {
                    System.out.println("Doesn't look like a line");
                }
                return false;
            }
        }
    }
    
    
    /**
    *
    * Return the ellipse fully containing the data points.
    *
    **/
    public Ellipse
    getEllipse()
    {
        leastSquaresForCircle();
        deriveBoundingBox();
        return new Ellipse( center.x - radius.width,
                            center.y - radius.height,
                            radius.width*2,
                            radius.height*2 );
    }
    
    
    /**
    *
    * calculate the lsq error.
    *
    **/
    public double
    leastSquaresForCircle()
    {
        derivePolarCoordinates();
        
        double rotation_angle = 0.0;
        Point  p = new Point();

        
        double a = radius.width;
        double b = radius.height;
        if ( a < b ) {
            double tmp = a;
            a = b;
            b = tmp;
            rotation_angle = Math.PI/2;
        }
        
        // Now a > b
        double c     = Math.sqrt( a*a-b*b );
        double e     = Math.sqrt( 1 - (b*b)/(a*a) ); // Eccentricity
        double r     = 0;
        double theta = 0;
                       
        double ls_error   = 0.0;                         

        for ( int i=0; i<polar_points.length; i++ ) {
            theta = polar_points[i].theta + rotation_angle;
            r = a*Math.sqrt( (1-e*e) /
                             (1-e*e*Math.cos(theta)*Math.cos(theta)) );
            p.x = center.x + (int)(r*Math.cos(theta - rotation_angle));
            p.y = center.y + (int)(r*Math.sin(theta - rotation_angle));
            
            ls_error += vertices[i].distanceSq( p );
        }
        
        return (ls_error/polar_points.length-1);
    }
    
    
    /**
    *
    * calculate the lsq error.
    *
    **/
    public double
    leastSquaresForCircle2()
    {
        double average_radius = 0.0;
        double error          = 0.0;
        
        Point center_of_mass = new Point( 0, 0 );
        for ( int i=0; i<vertices.length; i++ ) {
            center_of_mass.x += vertices[i].x; 
            center_of_mass.y += vertices[i].y; 
         }
        
        center_of_mass.x = center_of_mass.x/vertices.length;
        center_of_mass.y = center_of_mass.y/vertices.length;
        
        for ( int i=0; i<vertices.length; i++ ) {
             average_radius += center_of_mass.distance( vertices[i] );
         }
        
        average_radius = average_radius/vertices.length;
        
        double difference;
        for ( int i=0; i<vertices.length; i++ ) {
            difference = Math.abs( average_radius - 
                                   center_of_mass.distance( vertices[i] ) );
            error     += difference*difference;
        }
 
        return error/vertices.length;
    }

    
    /**
    * 
    * Remove the redundant at the beginning and the end.
    *
    **/
    public Vertex[]
    removeRedundantEndPoints( Vertex input_points[] )
    {
        boolean removed_redundant_point_at_the_beginning = false;
        boolean removed_redundant_point_at_the_end       = false;

        int     length     = input_points.length;
        int     new_length = input_points.length;
        
        if ( input_points[0].equals( input_points[1] ) ) {
            removed_redundant_point_at_the_beginning = true;
            new_length--;
        }
        if ( input_points[length-1].equals( input_points[length-2] ) ) {
            removed_redundant_point_at_the_end = true;
            new_length--;
        }
                
        Vertex output[] = new Vertex[new_length];
        
        if ( removed_redundant_point_at_the_beginning ) {
            for ( int i=0; i<output.length; i++ ) {
                output[i] = input_points[i+1];
            }
        } else {
            for ( int i=0; i<output.length; i++ ) {
                output[i] = input_points[i];
            }
        }
        
        return output;
    }

    
    /**
    * 
    * Set fit method
    *
    **/
    public void
    setFitMethod( int method )
    {
        fit_method     = method;
        d_is_valid     = false;
        dd_dt_is_valid = false;
        deriveProperties();
    }

    
    /**
    * 
    * Do convolution.
    *
    **/
    public void
    convolveDirection( double filter[] )
    {
        d = Util.convolve( d, filter );
        dd_dt_is_valid = false;
        derive_dd_dt();
    }

    
    /**
    * 
    * Do convolution.
    *
    **/
    public void
    convolveChangeInDirection( double filter[] )
    {
        dd_dt = Util.convolve( dd_dt, filter );
    }

    
    /**
    * 
    * Paint
    *
    **/
    public void
    paint( Graphics g )
    {
        Point.paint( vertices, g );
    }
}

/**
  *
  * $Log: StrokeData.java,v $
  * Revision 1.18  2003/11/05 01:42:02  moltmans
  * Found more ^M's  They should all be gone now... Again...  For good?
  *
  * Revision 1.17  2003/07/15 14:52:19  calvarad
  * Many changes/bug fixes.  Also, added a renderer interface and a basic surface class.  If you want to turn off the gray rectangles around the interpretations, modify the basic renderer class or just write your own renderer (although without a config file for this you will still need to change some code)
  *
  * Revision 1.16  2003/06/26 19:57:15  calvarad
  * Lots of bug fixes
  *
  * Revision 1.15  2003/05/29 19:44:05  calvarad
  * added some more stuff for the data processing
  *
  * Revision 1.14  2003/05/07 20:58:54  mtsezgin
  *
  * Fixed some problems with the arc recognition in general, and recognition
  * on the acer tablet in particular.
  *
  * Revision 1.13  2003/03/06 01:08:52  moltmans
  * Added copyright to all the files.
  *
  * Revision 1.12  2002/07/22 21:03:34  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.10  2002/07/09 16:04:21  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.9  2002/05/08 00:29:46  moltmans
  * Merge in all of the makefile changes (etc...) onto the main trunk.
  *
  * Revision 1.8.2.1  2002/05/07 22:56:42  moltmans
  * Several major classes of changes:
  *
  * 1) Makefiles now look for .dependencies instead of `files'
  * 2) The system now uses the Log4j Package for logging messages
  *    One place the messages can be sent to is the Debug window.
  * 3) Jar files: are now left in their native form, in the Lib
  *    directory.  This makes it easier to add new versions of jars with
  *    less confusion.  The side effect of this was that you know have to
  *    have all of the jars on your classpath.  The default setup scripts
  *    do this automagically.  On windows, to execute however, more jars
  *    need to be added to the classpath...  I haven't sorted that one out
  *    yet.
  *
  * Revision 1.8  2002/04/01 23:51:06  moltmans
  * Updated some small bugs in Polygon,  having to do with accessing null
  * original_points, and added some thresholded polygon parsing to filter
  * out small edges.
  *
  * Revision 1.7  2001/11/26 18:48:32  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.6  2001/11/26 17:33:39  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.5  2001/11/23 03:22:56  mtsezgin
  * Major reorganization
  *
  * Revision 1.4  2001/11/14 01:59:58  moltmans
  * Added get start and end time methods.
  *
  * Revision 1.3  2001/10/12 23:32:29  mtsezgin
  * Turned off printing...
  *
  * Revision 1.2  2001/10/12 22:25:52  mtsezgin
  * This is a commit of all files.
  * Shoapid
  * vi sux:q
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

