<<<<<<< HEAD
/*
 * Copyright (c) 2012, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.worldwind.tile;

import static com.metsci.glimpse.util.logging.LoggerUtils.*;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.PreRenderable;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.context.TargetStackUtil;
import com.metsci.glimpse.gl.GLSimpleFrameBufferObject;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.units.Azimuth;
import com.metsci.glimpse.util.units.Length;
import com.metsci.glimpse.util.vector.Vector2d;
import com.metsci.glimpse.worldwind.canvas.SimpleOffscreenCanvas;

/**
 * Displays the content of a GlimpseLayout onto the surface of the Worldwind globe
 * and dynamically adjusts the surface area of the tile to just fill the screen (and no more)
 * to ensure that the visible areas receive maximum texture resolution.
 * 
 * @author ulman
 */
public class GlimpseDynamicSurfaceTile extends AbstractLayer implements GlimpseSurfaceTile, Renderable, PreRenderable
{
    private static final Logger logger = Logger.getLogger( GlimpseDynamicSurfaceTile.class.getSimpleName( ) );

    protected static final int HEURISTIC_ALTITUDE_CUTOFF = 800;

    protected GlimpseLayout background;
    protected GlimpseLayout mask;
    protected GlimpseLayout layout;
    protected Axis2D axes;
    protected GeoProjection projection;
    protected int width, height;

    protected LatLonBounds maxBounds;
    protected List<LatLon> maxCorners;

    protected LatLonBounds bounds;
    protected List<LatLon> corners;

    protected SimpleOffscreenCanvas offscreenCanvas;
    protected TextureSurfaceTile tile;
    protected GLContext context;

    public GlimpseDynamicSurfaceTile( GlimpseLayout layout, Axis2D axes, GeoProjection projection, int width, int height, double minLat, double maxLat, double minLon, double maxLon )
    {
        this( layout, axes, projection, width, height, getCorners( new LatLonBounds( minLat, maxLat, minLon, maxLon ) ) );
    }

    public GlimpseDynamicSurfaceTile( GlimpseLayout layout, Axis2D axes, GeoProjection projection, int width, int height, List<LatLon> corners )
    {
        this.axes = axes;
        this.projection = projection;
        this.layout = layout;

        this.width = width;
        this.height = height;

        updateMaxCorners( corners );

        this.mask = new GlimpseLayout( );
        this.mask.setLayoutData( String.format( "pos 0 0 %d %d", width, height ) );
        this.mask.addLayout( layout );

        this.background = new GlimpseLayout( );
        this.background.addPainter( new BackgroundPainter( ).setColor( 0f, 0f, 0f, 0f ) );
        this.background.addLayout( mask );

        this.offscreenCanvas = new SimpleOffscreenCanvas( width, height, false, false, context );
        this.offscreenCanvas.addLayout( this.background );
    }

    public void updateMaxCorners( List<LatLon> corners )
    {
        this.maxBounds = getCorners( corners );
        this.maxCorners = getCorners( this.maxBounds );
    }

    @Override
    public GlimpseLayout getGlimpseLayout( )
    {
        return this.layout;
    }

    @Override
    public GlimpseCanvas getGlimpseCanvas( )
    {
        return this.offscreenCanvas;
    }

    @Override
    public GlimpseTargetStack getTargetStack( )
    {
        return TargetStackUtil.newTargetStack( this.offscreenCanvas, this.layout );
    }

    @Override
    public void preRender( DrawContext dc )
    {
        if ( tile == null )
        {

            if ( context == null )
            {
                GLContext oldcontext = dc.getGLContext( );
                context = dc.getGLDrawable( ).createContext( oldcontext );
            }

            offscreenCanvas.initialize( context );
        }

        updateGeometry( dc );

        drawOffscreen( dc );

        if ( tile == null )
        {
            int textureHandle = getTextureHandle( );
            tile = newTextureSurfaceTile( textureHandle, corners );
        }
    }

    protected int getTextureHandle( )
    {
        return offscreenCanvas.getFrameBuffer( ).getTextureId( );
    }

    protected TextureSurfaceTile newTextureSurfaceTile( int textureHandle, Iterable<? extends LatLon> corners )
    {
        return new TextureSurfaceTile( textureHandle, corners );
    }

    protected void updateGeometry( DrawContext dc )
    {
        // two heuristic methods of calculating the screen corners
        // heuristic 1 is basically never actually used to updateGeometry(),
        // but it is a good indicator of whether heuristic 2 will provide
        // good results (if heuristic 1 is not valid, heuristic 2 is likely
        // to provide bad results, so updateGeometryDefault() is used)
        List<LatLon> screenCorners1 = getCornersHeuristic1( dc );
        List<LatLon> screenCorners2 = getCornersHeuristic2( dc );

        if ( !isValid( screenCorners1 ) )
        {
            updateGeometryDefault( );
        }
        else if ( isValid( screenCorners2 ) )
        {
            updateGeometry( screenCorners2 );
        }
        else
        {
            updateGeometryDefault( );
        }
    }

    protected void updateGeometryDefault( )
    {
        corners = maxCorners;
        bounds = maxBounds;

        updateTile( );
    }

    protected void updateGeometry( List<LatLon> screenCorners )
    {
        LatLonBounds screenBounds = bufferCorners( getCorners( screenCorners ), 0.5 );
        bounds = getIntersectedCorners( maxBounds, screenBounds );
        corners = getCorners( bounds );

        updateTile( );
    }

    protected void updateTile( )
    {
        if ( tile != null )
        {
            setAxes( axes, bounds, projection );
            tile.setCorners( corners );
        }
    }

    protected void setAxes( Axis2D axes, LatLonBounds bounds, GeoProjection projection )
    {
        Vector2d c1 = projection.project( LatLonGeo.fromDeg( bounds.minLat, bounds.minLon ) );
        Vector2d c2 = projection.project( LatLonGeo.fromDeg( bounds.maxLat, bounds.minLon ) );
        Vector2d c3 = projection.project( LatLonGeo.fromDeg( bounds.maxLat, bounds.maxLon ) );
        Vector2d c4 = projection.project( LatLonGeo.fromDeg( bounds.minLat, bounds.maxLon ) );

        double minX = minX( c1, c2, c3, c4 );
        double maxX = maxX( c1, c2, c3, c4 );
        double minY = minY( c1, c2, c3, c4 );
        double maxY = maxY( c1, c2, c3, c4 );

        axes.set( minX, maxX, minY, maxY );
        axes.getAxisX( ).validate( );
        axes.getAxisY( ).validate( );
    }

    public static double minX( Vector2d... corners )
    {
        double min = Double.POSITIVE_INFINITY;
        for ( Vector2d corner : corners )
        {
            if ( corner.getX( ) < min ) min = corner.getX( );
        }
        return min;
    }

    public static double minY( Vector2d... corners )
    {
        double min = Double.POSITIVE_INFINITY;
        for ( Vector2d corner : corners )
        {
            if ( corner.getY( ) < min ) min = corner.getY( );
        }
        return min;
    }

    public static double maxX( Vector2d... corners )
    {
        double max = Double.NEGATIVE_INFINITY;
        for ( Vector2d corner : corners )
        {
            if ( corner.getX( ) > max ) max = corner.getX( );
        }
        return max;
    }

    public static double maxY( Vector2d... corners )
    {
        double max = Double.NEGATIVE_INFINITY;
        for ( Vector2d corner : corners )
        {
            if ( corner.getY( ) > max ) max = corner.getY( );
        }
        return max;
    }

    public static boolean isValid( List<LatLon> screenCorners )
    {
        if ( screenCorners == null ) return false;

        for ( LatLon latlon : screenCorners )
        {
            if ( latlon == null ) return false;
        }

        return true;
    }

    public static LatLonBounds bufferCorners( LatLonBounds corners, double bufferFraction )
    {
        double diffLat = corners.maxLat - corners.minLat;
        double diffLon = corners.maxLon - corners.minLon;

        double buffMinLat = corners.minLat - diffLat * bufferFraction;
        double buffMaxLat = corners.maxLat + diffLat * bufferFraction;
        double buffMinLon = corners.minLon - diffLon * bufferFraction;
        double buffMaxLon = corners.maxLon + diffLon * bufferFraction;

        return new LatLonBounds( buffMinLat, buffMaxLat, buffMinLon, buffMaxLon );
    }

    public static LatLonBounds getCorners( List<LatLon> screenCorners )
    {
        double minLat = Double.POSITIVE_INFINITY;
        double minLon = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;
        for ( LatLon latlon : screenCorners )
        {
            double lat = latlon.getLatitude( ).getDegrees( );
            double lon = latlon.getLongitude( ).getDegrees( );

            if ( lat < minLat ) minLat = lat;
            if ( lat > maxLat ) maxLat = lat;
            if ( lon < minLon ) minLon = lon;
            if ( lon > maxLon ) maxLon = lon;
        }

        return new LatLonBounds( minLat, maxLat, minLon, maxLon );
    }

    public static LatLonBounds getUnionedCorners( LatLonBounds corners1, LatLonBounds corners2 )
    {
        double minLat = Math.min( corners1.minLat, corners2.minLat );
        double minLon = Math.min( corners1.minLon, corners2.minLon );
        double maxLat = Math.max( corners1.maxLat, corners2.maxLat );
        double maxLon = Math.max( corners1.maxLon, corners2.maxLon );

        return new LatLonBounds( minLat, maxLat, minLon, maxLon );
    }

    public static LatLonBounds getIntersectedCorners( LatLonBounds corners1, LatLonBounds corners2 )
    {
        double minLat = Math.max( corners1.minLat, corners2.minLat );
        double minLon = Math.max( corners1.minLon, corners2.minLon );
        double maxLat = Math.min( corners1.maxLat, corners2.maxLat );
        double maxLon = Math.min( corners1.maxLon, corners2.maxLon );

        return new LatLonBounds( minLat, maxLat, minLon, maxLon );
    }

    public static List<LatLon> getCorners( LatLonBounds bounds )
    {
        List<LatLon> corners = new ArrayList<LatLon>( );

        corners.add( LatLon.fromDegrees( bounds.minLat, bounds.minLon ) );
        corners.add( LatLon.fromDegrees( bounds.minLat, bounds.maxLon ) );
        corners.add( LatLon.fromDegrees( bounds.maxLat, bounds.maxLon ) );
        corners.add( LatLon.fromDegrees( bounds.maxLat, bounds.minLon ) );

        return corners;
    }

    // a heuristic for calculating the corners of the visible region
    public static List<LatLon> getCornersHeuristic1( DrawContext dc )
    {
        View view = dc.getView( );
        Rectangle viewport = view.getViewport( );

        List<LatLon> corners = new ArrayList<LatLon>( 4 );
        corners.add( view.computePositionFromScreenPoint( viewport.getMinX( ), viewport.getMinY( ) ) );
        corners.add( view.computePositionFromScreenPoint( viewport.getMinX( ), viewport.getMaxY( ) ) );
        corners.add( view.computePositionFromScreenPoint( viewport.getMaxX( ), viewport.getMaxY( ) ) );
        corners.add( view.computePositionFromScreenPoint( viewport.getMaxX( ), viewport.getMinY( ) ) );

        return corners;
    }

    // another possible heuristic for calculating the corners of the visible region
    // inspired by: gov.nasa.worldwind.layers.ScalebarLayer
    public static List<LatLon> getCornersHeuristic2( DrawContext dc )
    {
        // Compute scale size in real world
        Position referencePosition = dc.getViewportCenterPosition( );
        if ( referencePosition == null ) return null;

        Vec4 groundTarget = dc.getGlobe( ).computePointFromPosition( referencePosition );
        Double distance = dc.getView( ).getEyePoint( ).distanceTo3( groundTarget );
        double metersPerPixel = dc.getView( ).computePixelSizeAtDistance( distance );

        // now assume this size roughly holds across the whole screen
        // (which is an ok assumption when we're zoomed in)
        View view = dc.getView( );
        Rectangle viewport = view.getViewport( );
        double viewportHeightMeters = viewport.getHeight( ) * metersPerPixel;
        double viewportWidthMeters = viewport.getWidth( ) * metersPerPixel;

        // in order to not worry about how the viewport is rotated
        // (which direction is north) just take the largest dimension
        double viewportSizeMeters = Math.max( viewportHeightMeters, viewportWidthMeters );

        LatLonGeo centerLatLon = LatLonGeo.fromDeg( referencePosition.latitude.getDegrees( ), referencePosition.longitude.getDegrees( ) );
        LatLonGeo swLatLon = centerLatLon.displacedBy( Length.fromMeters( viewportSizeMeters ), Azimuth.southwest );
        LatLonGeo seLatLon = centerLatLon.displacedBy( Length.fromMeters( viewportSizeMeters ), Azimuth.southeast );
        LatLonGeo nwLatLon = centerLatLon.displacedBy( Length.fromMeters( viewportSizeMeters ), Azimuth.northwest );
        LatLonGeo neLatLon = centerLatLon.displacedBy( Length.fromMeters( viewportSizeMeters ), Azimuth.northeast );

        Position swPos = Position.fromDegrees( swLatLon.getLatDeg( ), swLatLon.getLonDeg( ) );
        Position sePos = Position.fromDegrees( seLatLon.getLatDeg( ), seLatLon.getLonDeg( ) );
        Position nwPos = Position.fromDegrees( nwLatLon.getLatDeg( ), nwLatLon.getLonDeg( ) );
        Position nePos = Position.fromDegrees( neLatLon.getLatDeg( ), neLatLon.getLonDeg( ) );

        List<LatLon> corners = new ArrayList<LatLon>( 4 );
        corners.add( swPos );
        corners.add( sePos );
        corners.add( nwPos );
        corners.add( nePos );

        return corners;
    }

    @Override
    protected void doRender( DrawContext dc )
    {
        tile.render( dc );

    }

    protected void drawOffscreen( DrawContext dc )
    {
        context.makeCurrent( );
        try
        {
            drawOffscreen( dc.getGLContext( ) );
        }
        finally
        {
            dc.getGLContext( ).makeCurrent( );
        }
    }

    protected void drawOffscreen( GLContext glContext )
    {
        GLSimpleFrameBufferObject fbo = offscreenCanvas.getFrameBuffer( );
        OGLStackHandler stack = new OGLStackHandler( );
        GL2 gl = glContext.getGL( ).getGL2( );

        stack.pushAttrib( gl, GL2.GL_ALL_ATTRIB_BITS );
        stack.pushClientAttrib( gl, ( int ) GL2.GL_ALL_CLIENT_ATTRIB_BITS );
        stack.pushTexture( gl );
        stack.pushModelview( gl );
        stack.pushProjection( gl );

        fbo.bind( glContext );
        try
        {
            background.paintTo( offscreenCanvas.getGlimpseContext( ) );
        }
        catch ( Exception e )
        {
            logWarning( logger, "Trouble drawing to offscreen buffer", e );
        }
        finally
        {
            fbo.unbind( glContext );
            stack.pop( gl );
        }
    }

    public static class LatLonBounds
    {
        public double minLat, maxLat, minLon, maxLon;

        public LatLonBounds( double minLat, double maxLat, double minLon, double maxLon )
        {
            this.minLat = minLat;
            this.maxLat = maxLat;
            this.minLon = minLon;
            this.maxLon = maxLon;
        }

        @Override
        public String toString( )
        {
            return String.format( "%f %f %f %f", minLat, maxLat, minLon, maxLon );
        }
    }
=======
package fig.basic;

import java.util.*;

public class NumUtils {
  // This random stuff should be deprecated.  DON'T USE IT!
  @Deprecated
  private static Random random = new Random();
  @Deprecated
  public static Random getRandom() { return random; }
  @Deprecated
  public static void setRandom(long seed) { setRandom(new Random(seed)); }
  @Deprecated
  public static void setRandom(Random random) { NumUtils.random = random; }
  @Deprecated
  public static double randDouble() { return random.nextDouble(); }
  @Deprecated
  public static int randInt(int n) { return random.nextInt(n); }
  @Deprecated
  public static boolean randBernoulli(double p) { return random.nextDouble() < p; }
  @Deprecated
  public static int randMultinomial(double[] probs, Random random) {
    double v = random.nextDouble();
    double sum = 0;
    for(int i = 0; i < probs.length; i++) {
      sum += probs[i];
      if(v < sum) return i;
    }
    throw new RuntimeException(sum + " < " + v);
  }
  @Deprecated
  public static int randMultinomial(double[] probs) {
    return randMultinomial(probs, random);
  }

  public static boolean isFinite(double x) { return !Double.isNaN(x) && !Double.isInfinite(x); }
  public static boolean isFinite(double[] xs) { for (double x : xs) if (!isFinite(x)) return false; return true; }
  public static void assertIsFinite(double x) { assert isFinite(x): "Not finite: " + x; }
  public static void assertIsFinite(double[] xs) { for(double x : xs) assert isFinite(x): "Not finite: " + Fmt.D(xs); }
  public static void assertIsFinite(double[][] xss) { for(double[] xs : xss) for(double x : xs) assert isFinite(x): "Not finite: " + Fmt.D(xss); }
  public static boolean isProb(double x) { return x >= 0 && x <= 1 && !Double.isNaN(x); }
  public static void assertIsProb(double x) { assert isProb(x): "Not a probability [0, 1]: " + x; }
  public static void assertEquals(double x, double y) { assertEquals(x, y, 1e-10); }
  public static void assertEquals(double x, double y, double tol) { assert Math.abs(x-y)<tol: x + " != " + y; }
  public static void assertNormalized(double[] p) { assertEquals(ListUtils.sum(p), 1); }
  public static void assertNormalized(double[] p, double tol) { assertEquals(ListUtils.sum(p), 1, tol); }

  // Vector, matrix operations {
  public static boolean normalize(float[] data) {
    float sum = 0;
    for(float x : data) sum += x;
    if(sum == 0) return false;
    for(int i = 0; i < data.length; i++) data[i] /= sum;
    return true;
  }
  public static boolean normalize(double[] data) {
    double sum = 0;
    for(double x : data) sum += x;
    if(sum == 0) return false;
    for(int i = 0; i < data.length; i++) data[i] /= sum;
    return true;
  }
  public static boolean normalize(double[][] data) {
    double sum = 0;
    for(double[] v : data)
      for(double x : v) sum += x;
    if(sum == 0) return false;
    for(double[] v : data)
      for(int i = 0; i < v.length; i++) v[i] /= sum;
    return true;
  }
  public static boolean normalizeEachRow(double[][] data) {
    boolean allRowsOkay = true;
    for (double[] row: data) {
      if(!NumUtils.normalize(row)) allRowsOkay = false;
    }
    return allRowsOkay;
  }
  public static boolean normalize(double[][][] data) {
    double sum = 0;
    for(double[][] m : data)
      for(double[] v : m)
        for(double x : v) sum += x;
    if(sum == 0) return false;
    for(double[][] m : data)
      for(double[] v : m)
        for(int i = 0; i < v.length; i++) v[i] /= sum;
    return true;
  }

  public static boolean expNormalize(double[] probs) {
    // Input: log probabilities (unnormalized too)
    // Output: normalized probabilities
    // probs actually contains log probabilities; so we can add an arbitrary constant to make
    // the largest log prob 0 to prevent overflow problems
    double max = Double.NEGATIVE_INFINITY;
    for(int i = 0; i < probs.length; i++)
      max = Math.max(max, probs[i]);
    if (Double.isInfinite(max)) return false;
    for(int i = 0; i < probs.length; i++)
      probs[i] = Math.exp(probs[i] - max);
    return normalize(probs);
  }

  public static double expNormalizeLogZ(double[] probs) {
    double max = Double.NEGATIVE_INFINITY;
    for(int i = 0; i < probs.length; i++)
      max = Math.max(max, probs[i]);
    if (Double.isInfinite(max)) return max;
    double sum = 0;
    for(int i = 0; i < probs.length; i++) {
      probs[i] = Math.exp(probs[i] - max);
      sum += probs[i];
    }
    assert normalize(probs);
    return max + Math.log(sum);
  }

  public static boolean expNormalize(double[][] probs) {
    double max = Double.NEGATIVE_INFINITY;
    for(double[] v : probs)
      for(int i = 0; i < v.length; i++)
        max = Math.max(max, v[i]);
    for(double[] v : probs)
      for(int i = 0; i < v.length; i++)
        v[i] = Math.exp(v[i]-max);
    return normalize(probs);
  }

  public static boolean expNormalize(double[][][] probs) {
    double max = Double.NEGATIVE_INFINITY;
    for(double[][] m : probs)
      for(double[] v : m)
        for(int i = 0; i < v.length; i++)
          max = Math.max(max, v[i]);
    for(double[][] m : probs)
      for(double[] v : m)
        for(int i = 0; i < v.length; i++)
          v[i] = Math.exp(v[i]-max);
    return normalize(probs);
  }
  
  public static int[][] toInt(double[][] data) {
    int[][] newdata = new int[data.length][];
    for(int r = 0; r < data.length; r++) {
      newdata[r] = new int[data[r].length];
      for(int c = 0; c < data[r].length; c++)
        newdata[r][c] = (int)data[r][c];
    }
    return newdata;
  }

  public static double l1Dist(double[] x, double[] y) {
    double sum = 0;
    for(int i = 0; i < x.length; i++)
      sum += Math.abs(x[i]-y[i]);
    return sum;
  }

  public static double lInfDist(double[] x, double[] y) {
    double max = 0;
    for(int i = 0; i < x.length; i++)
      max = Math.max(max, Math.abs(x[i]-y[i]));
    return max;
  }

  public static double l2Dist(double[] x, double[] y) {
    return Math.sqrt(l2DistSquared(x, y));
  }
  public static double l2DistSquared(double[] x, double[] y) {
    double sum = 0;
    for(int i = 0; i < x.length; i++)
      sum += (x[i]-y[i])*(x[i]-y[i]);
    return sum;
  }

  public static double l2Norm(double[] x) { return Math.sqrt(l2NormSquared(x)); }
  public static double l2NormSquared(double[] x) {
    double sum = 0;
    for(int i = 0; i < x.length; i++)
      sum += x[i]*x[i];
    return sum;
  }

  public static double[] l2NormalizedMut(double[] x) {
    double norm = l2Norm(x);
    if(norm > 0) ListUtils.multMut(x, 1.0/norm);
    return x;
  }

  // If sum is 0, set to uniform
  // Return false if we had to set to uniform
  public static boolean normalizeForce(double[] data) {
    double sum = 0;
    for(double x : data) sum += x;
    if(sum == 0) {
      for(int i = 0; i < data.length; i++) data[i] = 1.0/data.length;
      return false;
    }
    else {
      for(int i = 0; i < data.length; i++) data[i] /= sum;
      return true;
    }
  }

  public static double[][] transpose(double[][] mat) {
    int m = mat.length, n = mat[0].length;
    double[][] newMat = new double[n][m];
    for(int r = 0; r < m; r++)
      for(int c = 0; c < n; c++)
        newMat[c][r] = mat[r][c];
    return newMat;
  }

  public static double[][] elementWiseMult(double[][] mat1, double[][] mat2) {
    int m = mat1.length, n = mat1[0].length;
    double[][] newMat = new double[m][n];
    for(int r = 0; r < m; r++)
      for(int c = 0; c < n; c++)
        newMat[r][c] = mat1[r][c] * mat2[r][c];
    return newMat;
  }

  public static void scalarMult(double[][] mat, double x) {
    int m = mat.length, n = mat[0].length;
    for(int r = 0; r < m; r++)
      for(int c = 0; c < n; c++)
        mat[r][c] *= x;
  }

  public static double[][] copy(double[][] mat) {
    int m = mat.length;
    double[][] newMat = new double[m][];
    for (int r = 0; r < m; r++) {
      int n = mat[r].length;
      newMat[r] = new double[n];
      for (int c = 0; c < n; c++)
        newMat[r][c] = mat[r][c];
    }
    return newMat;
  }

  public static boolean equals(double x, double y) {
    return Math.abs(x-y) < 1e-10;
  }
  public static boolean equals(double x, double y, double tol) {
    return Math.abs(x-y) < tol;
  }

  public static double round(double x, int numPlaces) {
    double scale = Math.pow(10, numPlaces);
    return Math.round(x * scale)/scale;
  }

  public static double[] round(double[] vec, int numPlaces) {
    double[] newVec = new double[vec.length];
    double scale = Math.pow(10, numPlaces);
    for(int i = 0; i < vec.length; i++)
      newVec[i] = Math.round(vec[i] * scale)/scale;
    return newVec;
  }

  public static double bound(double x, double lower, double upper) {
    if(x < lower) return lower;
    if(x > upper) return upper;
    return x;
  }
  public static int bound(int x, int lower, int upper) {
    if(x < lower) return lower;
    if(x > upper) return upper;
    return x;
  }
  // }

  public static double entropy(double[] probs) {
    double e = 0;
    for(double p : probs) {
      if(p > 0) e += -p * Math.log(p);
    }
    return e;
  }

  // Suppose we have a joint probability distribution p(X, Y)
  // and we want to calculate the conditional entropy
  // H(Y | X) = -\sum_x \sum_y p(x, y) \log p(y | x)
  // For a fixed x, we input y -> p(X=x, Y=y)
  // and get out the contribution to H(Y|X).
  // If we sum over all values of X, we get the desired result.
  public static double condEntropy(double[] probs) {
    double sum = ListUtils.sum(probs); // This is p(X) = \sum_y P(X, Y=y)
    double e = 0;
    for(double p : probs) {
      if(p > 0) e += -p * Math.log(p/sum);
    }
    assertIsFinite(sum);
    return e;
  }
  public static double condEntropy(double[][] probs) {
    double sum = 0;
    for(int i = 0; i < probs.length; i++)
      sum += condEntropy(probs[i]);
    return sum;
  }

  // Return log(gamma(xx))
  private static double[] logGammaCoeff =
  {
    76.18009172947146, -86.50532032941677,
    24.01409824083091, -1.231739572450155,
    0.1208650973866179e-2, -0.5395239384953e-5
  };
  public static double logGamma(double xx)
  {
    double x, y, tmp, ser;
    y = x = xx;
    tmp = x + 5.5;
    tmp -= (x+0.5) * Math.log(tmp);
    ser = 1.000000000190015;
    for(int j = 0; j <= 5; j++) ser += logGammaCoeff[j]/++y;
    return -tmp + Math.log(2.5066282746310005*ser/x);
  }

  // Return log factorial(n) = logGamma(n+1)
  // Cache small values
  private static double[] cachedLogFactorial = null;
  private static int numCachedLogFactorial = 1024;
  public static double logFactorial(int n) {
    if(n < numCachedLogFactorial) {
      if(cachedLogFactorial == null) {
        cachedLogFactorial = new double[numCachedLogFactorial];
        for(int i = 1; i < numCachedLogFactorial; i++) {
          cachedLogFactorial[i] = cachedLogFactorial[i-1] + Math.log(i);
        }
      }
      return cachedLogFactorial[n];
    }
    return logGamma(n+1);
  }
  public static double logChoose(int n, int k) {
    return logFactorial(n) - logFactorial(k) - logFactorial(n-k);
  }

  /**
  * Stolen from Radford Neal's fbm package.
  * digamma(x) is defined as (d/dx) log Gamma(x).  It is computed here
  * using an asymptotic expansion when x>5.  For x<=5, the recurrence
  * relation digamma(x) = digamma(x+1) - 1/x is used repeatedly.  See
  * Venables & Ripley, Modern Applied Statistics with S-Plus, pp. 151-152.
  * COMPUTE THE DIGAMMA FUNCTION.  Returns -inf if the argument is an integer
  * less than or equal to zero.
  */
  public static double digamma(double x) {
    assert x > 0 : x;
    double r, f, t;
    r = 0;
    while (x<=5) {
      r -= 1/x;
      x += 1;
    }
    f = 1/(x*x);
    t = f*(-1/12.0 + f*(1/120.0 + f*(-1/252.0 + f*(1/240.0 + f*(-1/132.0
        + f*(691/32760.0 + f*(-1/12.0 + f*3617/8160.0)))))));
    return r + Math.log(x) - 0.5/x + t;
  }

  // Return log(exp(a)+exp(b))
  private static double logMaxValue = Math.log(Double.MAX_VALUE);
  public static double logAdd(double a, double b)
  {
    if(a > b) {
      if(Double.isInfinite(b) || a-b > logMaxValue) return a;
      return b+Math.log(1+Math.exp(a-b));
    }
    else {
      if(Double.isInfinite(a) || b-a > logMaxValue) return b;
      return a+Math.log(1+Math.exp(b-a));
    }
  }

  // Fast exponential
  // http://martin.ankerl.com/2007/10/04/optimized-pow-approximation-for-java-and-c-c/
  public static double fastExp(double val) {
    final long tmp = (long) (1512775 * val + (1072693248 - 60801));
    return Double.longBitsToDouble(tmp << 32);
  }
  public static double fastLog(double val) {
    final double x = (Double.doubleToLongBits(val) >> 32);
    return (x - 1072632447) / 1512775;
  }

  public static double logistic(double z) {
    return 1.0 / (1 + Math.exp(-z));
  }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

