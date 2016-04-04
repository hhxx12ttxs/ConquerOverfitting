/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2004-08 Ben Fry and Casey Reas
  Copyright (c) 2001-04 Massachusetts Institute of Technology

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
*/

package processing.core;

import java.awt.Toolkit;
import java.awt.image.*;
import java.util.*;


/**
 * Subclass of PGraphics that handles 3D rendering.
 * It can render 3D inside a browser window and requires no plug-ins.
 * <p/>
 * The renderer is mostly set up based on the structure of the OpenGL API,
 * if you have questions about specifics that aren't covered here,
 * look for reference on the OpenGL implementation of a similar feature.
 * <p/>
 * Lighting and camera implementation by Simon Greenwold.
 */
public class PGraphics3D extends PGraphics {

  /** The depth buffer. */
  public float[] zbuffer;

  // ........................................................

  /** The modelview matrix. */
  public PMatrix3D modelview;

  /** Inverse modelview matrix, used for lighting. */
  public PMatrix3D modelviewInv;

  /** 
   * Marks when changes to the size have occurred, so that the camera 
   * will be reset in beginDraw().
   */
  protected boolean sizeChanged;
  
  /** The camera matrix, the modelview will be set to this on beginDraw. */
  public PMatrix3D camera;

  /** Inverse camera matrix */
  protected PMatrix3D cameraInv;

  /** Camera field of view. */
  public float cameraFOV;

  /** Position of the camera. */
  public float cameraX, cameraY, cameraZ;
  public float cameraNear, cameraFar;
  /** Aspect ratio of camera's view. */
  public float cameraAspect;

  /** Current projection matrix. */
  public PMatrix3D projection;


  //////////////////////////////////////////////////////////////


  /**
   * Maximum lights by default is 8, which is arbitrary for this renderer,
   * but is the minimum defined by OpenGL
   */
  public static final int MAX_LIGHTS = 8;

  public int lightCount = 0;

  /** Light types */
  public int[] lightType;

  /** Light positions */
  //public float[][] lightPosition;
  public PVector[] lightPosition;

  /** Light direction (normalized vector) */
  //public float[][] lightNormal;
  public PVector[] lightNormal;

  /** Light falloff */
  public float[] lightFalloffConstant;
  public float[] lightFalloffLinear;
  public float[] lightFalloffQuadratic;

  /** Light spot angle */
  public float[] lightSpotAngle;

  /** Cosine of light spot angle */
  public float[] lightSpotAngleCos;

  /** Light spot concentration */
  public float[] lightSpotConcentration;

  /** Diffuse colors for lights.
   *  For an ambient light, this will hold the ambient color.
   *  Internally these are stored as numbers between 0 and 1. */
  public float[][] lightDiffuse;

  /** Specular colors for lights.
      Internally these are stored as numbers between 0 and 1. */
  public float[][] lightSpecular;

  /** Current specular color for lighting */
  public float[] currentLightSpecular;

  /** Current light falloff */
  public float currentLightFalloffConstant;
  public float currentLightFalloffLinear;
  public float currentLightFalloffQuadratic;


  //////////////////////////////////////////////////////////////


  static public final int TRI_DIFFUSE_R = 0;
  static public final int TRI_DIFFUSE_G = 1;
  static public final int TRI_DIFFUSE_B = 2;
  static public final int TRI_DIFFUSE_A = 3;
  static public final int TRI_SPECULAR_R = 4;
  static public final int TRI_SPECULAR_G = 5;
  static public final int TRI_SPECULAR_B = 6;
  static public final int TRI_COLOR_COUNT = 7;

  // ........................................................

  // Whether or not we have to worry about vertex position for lighting calcs
  private boolean lightingDependsOnVertexPosition;

  static final int LIGHT_AMBIENT_R = 0;
  static final int LIGHT_AMBIENT_G = 1;
  static final int LIGHT_AMBIENT_B = 2;
  static final int LIGHT_DIFFUSE_R = 3;
  static final int LIGHT_DIFFUSE_G = 4;
  static final int LIGHT_DIFFUSE_B = 5;
  static final int LIGHT_SPECULAR_R = 6;
  static final int LIGHT_SPECULAR_G = 7;
  static final int LIGHT_SPECULAR_B = 8;
  static final int LIGHT_COLOR_COUNT = 9;

  // Used to shuttle lighting calcs around
  // (no need to re-allocate all the time)
  protected float[] tempLightingContribution = new float[LIGHT_COLOR_COUNT];
//  protected float[] worldNormal = new float[4];

  /// Used in lightTriangle(). Allocated here once to avoid re-allocating
  protected PVector lightTriangleNorm = new PVector();

  // ........................................................

  /**
   * This is turned on at beginCamera, and off at endCamera
   * Currently we don't support nested begin/end cameras.
   * If we wanted to, this variable would have to become a stack.
   */
  protected boolean manipulatingCamera;

  float[][] matrixStack = new float[MATRIX_STACK_DEPTH][16];
  float[][] matrixInvStack = new float[MATRIX_STACK_DEPTH][16];
  int matrixStackDepth;

  // These two matrices always point to either the modelview
  // or the modelviewInv, but they are swapped during
  // when in camera manipulation mode. That way camera transforms
  // are automatically accumulated in inverse on the modelview matrix.
  protected PMatrix3D forwardTransform;
  protected PMatrix3D reverseTransform;

  // Added by ewjordan for accurate texturing purposes. Screen plane is
  // not scaled to pixel-size, so these manually keep track of its size
  // from frustum() calls. Sorry to add public vars, is there a way
  // to compute these from something publicly available without matrix ops?
  // (used once per triangle in PTriangle with ENABLE_ACCURATE_TEXTURES)
  protected float leftScreen;
  protected float rightScreen;
  protected float topScreen;
  protected float bottomScreen;
  protected float nearPlane; //depth of near clipping plane

  /** true if frustum has been called to set perspective, false if ortho */
  private boolean frustumMode = false;

  /**
   * Use PSmoothTriangle for rendering instead of PTriangle?
   * Usually set by calling smooth() or noSmooth()
   */
  static protected boolean s_enableAccurateTextures = false; //maybe just use smooth instead?

  /** Used for anti-aliased and perspective corrected rendering. */
  public PSmoothTriangle smoothTriangle;


  // ........................................................

  // pos of first vertex of current shape in vertices array
  protected int shapeFirst;

  // i think vertex_end is actually the last vertex in the current shape
  // and is separate from vertexCount for occasions where drawing happens
  // on endDraw() with all the triangles being depth sorted
  protected int shapeLast;

  // vertices may be added during clipping against the near plane.
  protected int shapeLastPlusClipped;

  // used for sorting points when triangulating a polygon
  // warning - maximum number of vertices for a polygon is DEFAULT_VERTICES
  protected int vertexOrder[] = new int[DEFAULT_VERTICES];

  // ........................................................

  // This is done to keep track of start/stop information for lines in the
  // line array, so that lines can be shown as a single path, rather than just
  // individual segments. Currently only in use inside PGraphicsOpenGL.
  protected int pathCount;
  protected int[] pathOffset = new int[64];
  protected int[] pathLength = new int[64];

  // ........................................................

  // line & triangle fields (note that these overlap)
//  static protected final int INDEX = 0;          // shape index
  static protected final int VERTEX1 = 0;
  static protected final int VERTEX2 = 1;
  static protected final int VERTEX3 = 2;        // (triangles only)
  /** used to store the strokeColor int for efficient drawing. */
  static protected final int STROKE_COLOR = 1;   // (points only)
  static protected final int TEXTURE_INDEX = 3;  // (triangles only)
  //static protected final int STROKE_MODE = 2;    // (lines only)
  //static protected final int STROKE_WEIGHT = 3;  // (lines only)

  static protected final int POINT_FIELD_COUNT = 2;  //4
  static protected final int LINE_FIELD_COUNT = 2;  //4
  static protected final int TRIANGLE_FIELD_COUNT = 4;

  // points
  static final int DEFAULT_POINTS = 512;
  protected int[][] points = new int[DEFAULT_POINTS][POINT_FIELD_COUNT];
  protected int pointCount;

  // lines
  static final int DEFAULT_LINES = 512;
  public PLine line;  // used for drawing
  protected int[][] lines = new int[DEFAULT_LINES][LINE_FIELD_COUNT];
  protected int lineCount;

  // triangles
  static final int DEFAULT_TRIANGLES = 256;
  public PTriangle triangle;
  protected int[][] triangles =
    new int[DEFAULT_TRIANGLES][TRIANGLE_FIELD_COUNT];
  protected float triangleColors[][][] =
    new float[DEFAULT_TRIANGLES][3][TRI_COLOR_COUNT];
  protected int triangleCount;   // total number of triangles

  // cheap picking someday
  //public int shape_index;

  // ........................................................

  static final int DEFAULT_TEXTURES = 3;
  protected PImage[] textures = new PImage[DEFAULT_TEXTURES];
  int textureIndex;

  // ........................................................

  DirectColorModel cm;
  MemoryImageSource mis;


  //////////////////////////////////////////////////////////////


  public PGraphics3D() { }


  //public void setParent(PApplet parent)


  //public void setPrimary(boolean primary)


  //public void setPath(String path)


  /**
   * Called in response to a resize event, handles setting the
   * new width and height internally, as well as re-allocating
   * the pixel buffer for the new size.
   *
   * Note that this will nuke any cameraMode() settings.
   * 
   * No drawing can happen in this function, and no talking to the graphics
   * context. That is, no glXxxx() calls, or other things that change state.
   */
  public void setSize(int iwidth, int iheight) {  // ignore
    width = iwidth;
    height = iheight;
    width1 = width - 1;
    height1 = height - 1;

    allocate();
    reapplySettings();

    // init lights (in resize() instead of allocate() b/c needed by opengl)
    lightType = new int[MAX_LIGHTS];
    lightPosition = new PVector[MAX_LIGHTS];
    lightNormal = new PVector[MAX_LIGHTS];
    for (int i = 0; i < MAX_LIGHTS; i++) {
      lightPosition[i] = new PVector();
      lightNormal[i] = new PVector();
    }
    lightDiffuse = new float[MAX_LIGHTS][3];
    lightSpecular = new float[MAX_LIGHTS][3];
    lightFalloffConstant = new float[MAX_LIGHTS];
    lightFalloffLinear = new float[MAX_LIGHTS];
    lightFalloffQuadratic = new float[MAX_LIGHTS];
    lightSpotAngle = new float[MAX_LIGHTS];
    lightSpotAngleCos = new float[MAX_LIGHTS];
    lightSpotConcentration = new float[MAX_LIGHTS];
    currentLightSpecular = new float[3];

    projection = new PMatrix3D();
    modelview = new PMatrix3D();
    modelviewInv = new PMatrix3D();

//    modelviewStack = new float[MATRIX_STACK_DEPTH][16];
//    modelviewInvStack = new float[MATRIX_STACK_DEPTH][16];
//    modelviewStackPointer = 0;

    forwardTransform = modelview;
    reverseTransform = modelviewInv;

    // init perspective projection based on new dimensions
    cameraFOV = 60 * DEG_TO_RAD; // at least for now
    cameraX = width / 2.0f;
    cameraY = height / 2.0f;
    cameraZ = cameraY / ((float) Math.tan(cameraFOV / 2.0f));
    cameraNear = cameraZ / 10.0f;
    cameraFar = cameraZ * 10.0f;
    cameraAspect = (float)width / (float)height;

    camera = new PMatrix3D();
    cameraInv = new PMatrix3D();

    // set this flag so that beginDraw() will do an update to the camera.
    sizeChanged = true;
  }


  protected void allocate() {
    //System.out.println(this + " allocating for " + width + " " + height);
    //new Exception().printStackTrace();

    pixelCount = width * height;
    pixels = new int[pixelCount];
    zbuffer = new float[pixelCount];

    if (primarySurface) {
      cm = new DirectColorModel(32, 0x00ff0000, 0x0000ff00, 0x000000ff);;
      mis = new MemoryImageSource(width, height, pixels, 0, width);
      mis.setFullBufferUpdates(true);
      mis.setAnimated(true);
      image = Toolkit.getDefaultToolkit().createImage(mis);

    } else {
      // when not the main drawing surface, need to set the zbuffer,
      // because there's a possibility that background() will not be called
      Arrays.fill(zbuffer, Float.MAX_VALUE);
    }

    line = new PLine(this);
    triangle = new PTriangle(this);
    smoothTriangle = new PSmoothTriangle(this);
  }


  //public void dispose()


  ////////////////////////////////////////////////////////////


  //public boolean canDraw()


  public void beginDraw() {
    // need to call defaults(), but can only be done when it's ok
    // to draw (i.e. for opengl, no drawing can be done outside
    // beginDraw/endDraw).
    if (!settingsInited) defaultSettings();

    if (sizeChanged) {
      // set up the default camera
      camera();

      // defaults to perspective, if the user has setup up their
      // own projection, they'll need to fix it after resize anyway.
      // this helps the people who haven't set up their own projection.
      perspective();
      
      // clear the flag
      sizeChanged = false;
    }
    
    resetMatrix(); // reset model matrix

    // reset vertices
    vertexCount = 0;

    modelview.set(camera);
    modelviewInv.set(cameraInv);

    // clear out the lights, they'll have to be turned on again
    lightCount = 0;
    lightingDependsOnVertexPosition = false;
    lightFalloff(1, 0, 0);
    lightSpecular(0, 0, 0);

    /*
    // reset lines
    lineCount = 0;
    if (line != null) line.reset();  // is this necessary?
    pathCount = 0;

    // reset triangles
    triangleCount = 0;
    if (triangle != null) triangle.reset();  // necessary?
    */

    shapeFirst = 0;

    // reset textures
    Arrays.fill(textures, null);
    textureIndex = 0;

    normal(0, 0, 1);
  }


  /**
   * See notes in PGraphics.
   * If z-sorting has been turned on, then the triangles will
   * all be quicksorted here (to make alpha work more properly)
   * and then blit to the screen.
   */
  public void endDraw() {
    // no need to z order and render
    // shapes were already rendered in endShape();
    // (but can't return, since needs to update memimgsrc)
    if (hints[ENABLE_DEPTH_SORT]) {
      flush();
    }
    if (mis != null) {
      mis.newPixels(pixels, cm, 0, width);
    }
    // mark pixels as having been updated, so that they'll work properly
    // when this PGraphics is drawn using image().
    updatePixels();
  }


  ////////////////////////////////////////////////////////////


  //protected void checkSettings()


  protected void defaultSettings() {
    super.defaultSettings();

    manipulatingCamera = false;
    forwardTransform = modelview;
    reverseTransform = modelviewInv;

    // set up the default camera
    camera();

    // defaults to perspective, if the user has setup up their
    // own projection, they'll need to fix it after resize anyway.
    // this helps the people who haven't set up their own projection.
    perspective();

    // easiest for beginners
    textureMode(IMAGE);

    emissive(0.0f);
    specular(0.5f);
    shininess(1.0f);
  }


  //protected void reapplySettings()


  ////////////////////////////////////////////////////////////


  public void hint(int which) {
    if (which == DISABLE_DEPTH_SORT) {
      flush();
    } else if (which == DISABLE_DEPTH_TEST) {
      if (zbuffer != null) {  // will be null in OpenGL and others
        Arrays.fill(zbuffer, Float.MAX_VALUE);
      }
    }
    super.hint(which);
  }


  //////////////////////////////////////////////////////////////


  //public void beginShape()


  public void beginShape(int kind) {
    shape = kind;

//    shape_index = shape_index + 1;
//    if (shape_index == -1) {
//      shape_index = 0;
//    }

    if (hints[ENABLE_DEPTH_SORT]) {
      // continue with previous vertex, line and triangle count
      // all shapes are rendered at endDraw();
      shapeFirst = vertexCount;
      shapeLast = 0;

    } else {
      // reset vertex, line and triangle information
      // every shape is rendered at endShape();
      vertexCount = 0;
      if (line != null) line.reset();  // necessary?
      lineCount = 0;
//      pathCount = 0;
      if (triangle != null) triangle.reset();  // necessary?
      triangleCount = 0;
    }

    textureImage = null;
    curveVertexCount = 0;
    normalMode = NORMAL_MODE_AUTO;
//    normalCount = 0;
  }


  //public void normal(float nx, float ny, float nz)


  //public void textureMode(int mode)


  public void texture(PImage image) {
    textureImage = image;

    if (textureIndex == textures.length - 1) {
      textures = (PImage[]) PApplet.expand(textures);
    }
    if (textures[textureIndex] != null) {  // ???
      textureIndex++;
    }
    textures[textureIndex] = image;
  }


  public void vertex(float x, float y) {
    // override so that the default 3D implementation will be used,
    // which will pick up all 3D settings (e.g. emissive, ambient)
    vertex(x, y, 0);
  }


  //public void vertex(float x, float y, float z)


  public void vertex(float x, float y, float u, float v) {
    // see vertex(x, y) for note
    vertex(x, y, 0, u, v);
  }


  //public void vertex(float x, float y, float z, float u, float v)


  //public void breakShape()


  //public void endShape()


  public void endShape(int mode) {
    shapeLast = vertexCount;
    shapeLastPlusClipped = shapeLast;

    // don't try to draw if there are no vertices
    // (fixes a bug in LINE_LOOP that re-adds a nonexistent vertex)
    if (vertexCount == 0) {
      shape = 0;
      return;
    }

    // convert points from model (X/Y/Z) to camera space (VX/VY/VZ).
    // Do this now because we will be clipping them on add_triangle.
    endShapeModelToCamera(shapeFirst, shapeLast);

    if (stroke) {
      endShapeStroke(mode);
    }

    if (fill || textureImage != null) {
      endShapeFill();
    }

    // transform, light, and clip
    endShapeLighting(lightCount > 0 && fill);

    // convert points from camera space (VX, VY, VZ) to screen space (X, Y, Z)
    // (this appears to be wasted time with the OpenGL renderer)
    endShapeCameraToScreen(shapeFirst, shapeLastPlusClipped);

    // render shape and fill here if not saving the shapes for later
    // if true, the shapes will be rendered on endDraw
    if (!hints[ENABLE_DEPTH_SORT]) {
      if (fill || textureImage != null) {
        if (triangleCount > 0) {
          renderTriangles(0, triangleCount);
          if (raw != null) {
            rawTriangles(0, triangleCount);
          }
          triangleCount = 0;
        }
      }
      if (stroke) {
        if (pointCount > 0) {
          renderPoints(0, pointCount);
          if (raw != null) {
            rawPoints(0, pointCount);
          }
          pointCount = 0;
        }

        if (lineCount > 0) {
          renderLines(0, lineCount);
          if (raw != null) {
            rawLines(0, lineCount);
          }
          lineCount = 0;
        }
      }
      pathCount = 0;
    }

    shape = 0;
  }


  protected void endShapeModelToCamera(int start, int stop) {
    for (int i = start; i < stop; i++) {
      float vertex[] = vertices[i];

      vertex[VX] =
        modelview.m00*vertex[X] + modelview.m01*vertex[Y] +
        modelview.m02*vertex[Z] + modelview.m03;
      vertex[VY] =
        modelview.m10*vertex[X] + modelview.m11*vertex[Y] +
        modelview.m12*vertex[Z] + modelview.m13;
      vertex[VZ] =
        modelview.m20*vertex[X] + modelview.m21*vertex[Y] +
        modelview.m22*vertex[Z] + modelview.m23;
      vertex[VW] =
        modelview.m30*vertex[X] + modelview.m31*vertex[Y] +
        modelview.m32*vertex[Z] + modelview.m33;

      // normalize
      if (vertex[VW] != 0 && vertex[VW] != 1) {
        vertex[VX] /= vertex[VW];
        vertex[VY] /= vertex[VW];
        vertex[VZ] /= vertex[VW];
      }
      vertex[VW] = 1;
    }
  }


  protected void endShapeStroke(int mode) {
    switch (shape) {
    case POINTS:
    {
      int stop = shapeLast;
      for (int i = shapeFirst; i < stop; i++) {
//        if (strokeWeight == 1) {
        addPoint(i);
//        } else {
//          addLineBreak();  // total overkill for points
//          addLine(i, i);
//        }
      }
    }
    break;

    case LINES:
    {
      // store index of first vertex
      int first = lineCount;
      int stop = shapeLast - 1;
      //increment = (shape == LINES) ? 2 : 1;

      // for LINE_STRIP and LINE_LOOP, make this all one path
      if (shape != LINES) addLineBreak();

      for (int i = shapeFirst; i < stop; i += 2) {
        // for LINES, make a new path for each segment
        if (shape == LINES) addLineBreak();
        addLine(i, i+1);
      }

      // for LINE_LOOP, close the loop with a final segment
      //if (shape == LINE_LOOP) {
      if (mode == CLOSE) {
        addLine(stop, lines[first][VERTEX1]);
      }
    }
    break;

    case TRIANGLES:
    {
      for (int i = shapeFirst; i < shapeLast-2; i += 3) {
        addLineBreak();
        //counter = i - vertex_start;
        addLine(i+0, i+1);
        addLine(i+1, i+2);
        addLine(i+2, i+0);
      }
    }
    break;

    case TRIANGLE_STRIP:
    {
      // first draw all vertices as a line strip
      int stop = shapeLast-1;

      addLineBreak();
      for (int i = shapeFirst; i < stop; i++) {
        //counter = i - vertex_start;
        addLine(i, i+1);
      }

      // then draw from vertex (n) to (n+2)
      stop = shapeLast-2;
      for (int i = shapeFirst; i < stop; i++) {
        addLineBreak();
        addLine(i, i+2);
      }
    }
    break;

    case TRIANGLE_FAN:
    {
      // this just draws a series of line segments
      // from the center to each exterior point
      for (int i = shapeFirst + 1; i < shapeLast; i++) {
        addLineBreak();
        addLine(shapeFirst, i);
      }

      // then a single line loop around the outside.
      addLineBreak();
      for (int i = shapeFirst + 1; i < shapeLast-1; i++) {
        addLine(i, i+1);
      }
      // closing the loop
      addLine(shapeLast-1, shapeFirst + 1);
    }
    break;

    case QUADS:
    {
      for (int i = shapeFirst; i < shapeLast; i += 4) {
        addLineBreak();
        //counter = i - vertex_start;
        addLine(i+0, i+1);
        addLine(i+1, i+2);
        addLine(i+2, i+3);
        addLine(i+3, i+0);
      }
    }
    break;

    case QUAD_STRIP:
    {
      for (int i = shapeFirst; i < shapeLast - 3; i += 2) {
        addLineBreak();
        addLine(i+0, i+2);
        addLine(i+2, i+3);
        addLine(i+3, i+1);
        addLine(i+1, i+0);
      }
    }
    break;

    case POLYGON:
    {
      // store index of first vertex
      int stop = shapeLast - 1;

      addLineBreak();
      for (int i = shapeFirst; i < stop; i++) {
        addLine(i, i+1);
      }
      if (mode == CLOSE) {
        // draw the last line connecting back to the first point in poly
        addLine(stop, shapeFirst); //lines[first][VERTEX1]);
      }
    }
    break;
    }
  }


  protected void endShapeFill() {
    switch (shape) {
    case TRIANGLE_FAN:
    {
      int stop = shapeLast - 1;
      for (int i = shapeFirst + 1; i < stop; i++) {
        addTriangle(shapeFirst, i, i+1);
      }
    }
    break;

    case TRIANGLES:
    {
      int stop = shapeLast - 2;
      for (int i = shapeFirst; i < stop; i += 3) {
        // have to switch between clockwise/counter-clockwise
        // otherwise the feller is backwards and renderer won't draw
        if ((i % 2) == 0) {
          addTriangle(i, i+2, i+1);
        } else {
          addTriangle(i, i+1, i+2);
        }
      }
    }
    break;

    case TRIANGLE_STRIP:
    {
      int stop = shapeLast - 2;
      for (int i = shapeFirst; i < stop; i++) {
        // have to switch between clockwise/counter-clockwise
        // otherwise the feller is backwards and renderer won't draw
        if ((i % 2) == 0) {
          addTriangle(i, i+2, i+1);
        } else {
          addTriangle(i, i+1, i+2);
        }
      }
    }
    break;

    case QUADS:
    {
      int stop = vertexCount-3;
      for (int i = shapeFirst; i < stop; i += 4) {
        // first triangle
        addTriangle(i, i+1, i+2);
        // second triangle
        addTriangle(i, i+2, i+3);
      }
    }
    break;

    case QUAD_STRIP:
    {
      int stop = vertexCount-3;
      for (int i = shapeFirst; i < stop; i += 2) {
        // first triangle
        addTriangle(i+0, i+2, i+1);
        // second triangle
        addTriangle(i+2, i+3, i+1);
      }
    }
    break;

    case POLYGON:
    {
      addPolygonTriangles();
    }
    break;
    }
  }


  protected void endShapeLighting(boolean lights) {
    if (lights) {
      // If the lighting does not depend on vertex position and there is a single
      // normal specified for this shape, go ahead and apply the same lighting
      // contribution to every vertex in this shape (one lighting calc!)
      if (!lightingDependsOnVertexPosition && normalMode == NORMAL_MODE_SHAPE) {
        calcLightingContribution(shapeFirst, tempLightingContribution);
        for (int tri = 0; tri < triangleCount; tri++) {
          lightTriangle(tri, tempLightingContribution);
        }
      } else {  // Otherwise light each triangle individually...
        for (int tri = 0; tri < triangleCount; tri++) {
          lightTriangle(tri);
        }
      }
    } else {
      for (int tri = 0; tri < triangleCount; tri++) {
        int index = triangles[tri][VERTEX1];
        copyPrelitVertexColor(tri, index, 0);
        index = triangles[tri][VERTEX2];
        copyPrelitVertexColor(tri, index, 1);
        index = triangles[tri][VERTEX3];
        copyPrelitVertexColor(tri, index, 2);
      }
    }
  }


  protected void endShapeCameraToScreen(int start, int stop) {
    for (int i = start; i < stop; i++) {
      float vx[] = vertices[i];

      float ox =
        projection.m00*vx[VX] + projection.m01*vx[VY] +
        projection.m02*vx[VZ] + projection.m03*vx[VW];
      float oy =
        projection.m10*vx[VX] + projection.m11*vx[VY] +
        projection.m12*vx[VZ] + projection.m13*vx[VW];
      float oz =
        projection.m20*vx[VX] + projection.m21*vx[VY] +
        projection.m22*vx[VZ] + projection.m23*vx[VW];
      float ow =
        projection.m30*vx[VX] + projection.m31*vx[VY] +
        projection.m32*vx[VZ] + projection.m33*vx[VW];

      if (ow != 0 && ow != 1) {
        ox /= ow; oy /= ow; oz /= ow;
      }

      vx[TX] = width * (1 + ox) / 2.0f;
      vx[TY] = height * (1 + oy) / 2.0f;
      vx[TZ] = (oz + 1) / 2.0f;
    }
  }



  /////////////////////////////////////////////////////////////////////////////

  // POINTS


  protected void addPoint(int a) {
    if (pointCount == points.length) {
      int[][] temp = new int[pointCount << 1][LINE_FIELD_COUNT];
      System.arraycopy(points, 0, temp, 0, lineCount);
      points = temp;
    }
    points[pointCount][VERTEX1] = a;
    //points[pointCount][STROKE_MODE] = strokeCap | strokeJoin;
    points[pointCount][STROKE_COLOR] = strokeColor;
    //points[pointCount][STROKE_WEIGHT] = (int) (strokeWeight + 0.5f); // hmm
    pointCount++;
  }


  protected void renderPoints(int start, int stop) {
    if (strokeWeight != 1) {
      for (int i = start; i < stop; i++) {
        float[] a = vertices[points[i][VERTEX1]];
        renderLineVertices(a, a);
      }
    } else {
      for (int i = start; i < stop; i++) {
        float[] a = vertices[points[i][VERTEX1]];
        int sx = (int) (a[TX] + 0.4999f);
        int sy = (int) (a[TY] + 0.4999f);
        if (sx >= 0 && sx < width && sy >= 0 && sy < height) {
          int index = sy*width + sx;
          pixels[index] = points[i][STROKE_COLOR];
          zbuffer[index] = a[TZ];
        }
      }
    }
  }


  // alternative implementations of point rendering code...

  /*
      int sx = (int) (screenX(x, y, z) + 0.5f);
      int sy = (int) (screenY(x, y, z) + 0.5f);

      int index = sy*width + sx;
      pixels[index] = strokeColor;
      zbuffer[index] = screenZ(x, y, z);

   */

  /*
  protected void renderPoints(int start, int stop) {
    for (int i = start; i < stop; i++) {
      float a[] = vertices[points[i][VERTEX1]];

      line.reset();

      line.setIntensities(a[SR], a[SG], a[SB], a[SA],
                          a[SR], a[SG], a[SB], a[SA]);

      line.setVertices(a[TX], a[TY], a[TZ],
                       a[TX] + 0.5f, a[TY] + 0.5f, a[TZ] + 0.5f);

      line.draw();
    }
  }
  */

  /*
  // handle points with an actual stroke weight (or scaled by renderer)
  private void point3(float x, float y, float z, int color) {
    // need to get scaled version of the stroke
    float x1 = screenX(x - 0.5f, y - 0.5f, z);
    float y1 = screenY(x - 0.5f, y - 0.5f, z);
    float x2 = screenX(x + 0.5f, y + 0.5f, z);
    float y2 = screenY(x + 0.5f, y + 0.5f, z);

    float weight = (abs(x2 - x1) + abs(y2 - y1)) / 2f;
    if (weight < 1.5f) {
      int xx = (int) ((x1 + x2) / 2f);
      int yy = (int) ((y1 + y2) / 2f);
      //point0(xx, yy, z, color);
      zbuffer[yy*width + xx] = screenZ(x, y, z);
      //stencil?

    } else {
      // actually has some weight, need to draw shapes instead
      // these will be
    }
  }
  */


  protected void rawPoints(int start, int stop) {
    raw.colorMode(RGB, 1);
    raw.noFill();
    raw.strokeWeight(vertices[lines[start][VERTEX1]][SW]);
    raw.beginShape(POINTS);

    for (int i = start; i < stop; i++) {
      float a[] = vertices[lines[i][VERTEX1]];

      if (raw.is3D()) {
        if (a[VW] != 0) {
          raw.stroke(a[SR], a[SG], a[SB], a[SA]);
          raw.vertex(a[VX] / a[VW], a[VY] / a[VW], a[VZ] / a[VW]);
        }
      } else {  // if is2D()
        raw.stroke(a[SR], a[SG], a[SB], a[SA]);
        raw.vertex(a[TX], a[TY]);
      }
    }
    raw.endShape();
  }



  /////////////////////////////////////////////////////////////////////////////

  // LINES


  /**
   * Begin a new section of stroked geometry.
   */
  protected final void addLineBreak() {
    if (pathCount == pathOffset.length) {
      pathOffset = PApplet.expand(pathOffset);
      pathLength = PApplet.expand(pathLength);
    }
    pathOffset[pathCount] = lineCount;
    pathLength[pathCount] = 0;
    pathCount++;
  }


  protected void addLine(int a, int b) {
    addLineWithClip(a, b);
  }


  protected final void addLineWithClip(int a, int b) {
    float az = vertices[a][VZ];
    float bz = vertices[b][VZ];
    if (az > cameraNear) {
      if (bz > cameraNear) {
        return;
      }
      int cb = interpolateClipVertex(a, b);
      addLineWithoutClip(cb, b);
      return;
    }
    else {
      if (bz <= cameraNear) {
        addLineWithoutClip(a, b);
        return;
      }
      int cb = interpolateClipVertex(a, b);
      addLineWithoutClip(a, cb);
      return;
    }
  }


  protected final void addLineWithoutClip(int a, int b) {
    if (lineCount == lines.length) {
      int temp[][] = new int[lineCount<<1][LINE_FIELD_COUNT];
      System.arraycopy(lines, 0, temp, 0, lineCount);
      lines = temp;
    }
    lines[lineCount][VERTEX1] = a;
    lines[lineCount][VERTEX2] = b;

    //lines[lineCount][STROKE_MODE] = strokeCap | strokeJoin;
    //lines[lineCount][STROKE_WEIGHT] = (int) (strokeWeight + 0.5f); // hmm
    lineCount++;

    // mark this piece as being part of the current path
    pathLength[pathCount-1]++;
  }


  protected void renderLines(int start, int stop) {
    for (int i = start; i < stop; i++) {
      renderLineVertices(vertices[lines[i][VERTEX1]],
                         vertices[lines[i][VERTEX2]]);
    }
  }


  protected void renderLineVertices(float[] a, float[] b) {
    // 2D hack added by ewjordan 6/13/07
    // Offset coordinates by a little bit if drawing 2D graphics.
    // http://dev.processing.org/bugs/show_bug.cgi?id=95

    // This hack fixes a bug caused by numerical precision issues when
    // applying the 3D transformations to coordinates in the screen plane
    // that should actually not be altered under said transformations.
    // It will not be applied if any transformations other than translations
    // are active, nor should it apply in OpenGL mode (PGraphicsOpenGL
    // overrides render_lines(), so this should be fine).
    // This fix exposes a last-pixel bug in the lineClipCode() function
    // of PLine.java, so that fix must remain in place if this one is used.

    // Note: the "true" fix for this bug is to change the pixel coverage
    // model so that the threshold for display does not lie on an integer
    // boundary. Search "diamond exit rule" for info the OpenGL approach.

    /*
      // removing for 0149 with the return of P2D
      if (drawing2D() && a[Z] == 0) {
        a[TX] += 0.01;
        a[TY] += 0.01;
        a[VX] += 0.01*a[VW];
        a[VY] += 0.01*a[VW];
        b[TX] += 0.01;
        b[TY] += 0.01;
        b[VX] += 0.01*b[VW];
        b[VY] += 0.01*b[VW];
      }
     */
    // end 2d-hack

    if (a[SW] > 1.25f || a[SW] < 0.75f) {
      float ox1 = a[TX];
      float oy1 = a[TY];
      float ox2 = b[TX];
      float oy2 = b[TY];

      // TODO strokeWeight should be transformed!
      float weight = a[SW] / 2;

      // when drawing points with stroke weight, need to extend a bit
      if (ox1 == ox2 && oy1 == oy2) {
        oy1 -= weight;
        oy2 += weight;
      }

      float dX = ox2 - ox1 + EPSILON;
      float dY = oy2 - oy1 + EPSILON;
      float len = (float) Math.sqrt(dX*dX + dY*dY);

      float rh = weight / len;

      float dx0 = rh * dY;
      float dy0 = rh * dX;
      float dx1 = rh * dY;
      float dy1 = rh * dX;

      float ax1 = ox1+dx0;
      float ay1 = oy1-dy0;

      float ax2 = ox1-dx0;
      float ay2 = oy1+dy0;

      float bx1 = ox2+dx1;
      float by1 = oy2-dy1;

      float bx2 = ox2-dx1;
      float by2 = oy2+dy1;

      if (smooth) {
        smoothTriangle.reset(3);
        smoothTriangle.smooth = true;
        smoothTriangle.interpARGB = true;  // ?

        // render first triangle for thick line
        smoothTriangle.setVertices(ax1, ay1, a[TZ],
                                   bx2, by2, b[TZ],
                                   ax2, ay2, a[TZ]);
        smoothTriangle.setIntensities(a[SR], a[SG], a[SB], a[SA],
                                      b[SR], b[SG], b[SB], b[SA],
                                      a[SR], a[SG], a[SB], a[SA]);
        smoothTriangle.render();

        // render second triangle for thick line
        smoothTriangle.setVertices(ax1, ay1, a[TZ],
                                   bx2, by2, b[TZ],
                                   bx1, by1, b[TZ]);
        smoothTriangle.setIntensities(a[SR], a[SG], a[SB], a[SA],
                                      b[SR], b[SG], b[SB], b[SA],
                                      b[SR], b[SG], b[SB], b[SA]);
        smoothTriangle.render();

      } else {
        triangle.reset();

        // render first triangle for thick line
        triangle.setVertices(ax1, ay1, a[TZ],
                             bx2, by2, b[TZ],
                             ax2, ay2, a[TZ]);
        triangle.setIntensities(a[SR], a[SG], a[SB], a[SA],
                                b[SR], b[SG], b[SB], b[SA],
                                a[SR], a[SG], a[SB], a[SA]);
        triangle.render();

        // render second triangle for thick line
        triangle.setVertices(ax1, ay1, a[TZ],
                             bx2, by2, b[TZ],
                             bx1, by1, b[TZ]);
        triangle.setIntensities(a[SR], a[SG], a[SB], a[SA],
                                b[SR], b[SG], b[SB], b[SA],
                                b[SR], b[SG], b[SB], b[SA]);
        triangle.render();
      }

    } else {
      line.reset();

      line.setIntensities(a[SR], a[SG], a[SB], a[SA],
                          b[SR], b[SG], b[SB], b[SA]);

      line.setVertices(a[TX], a[TY], a[TZ],
                       b[TX], b[TY], b[TZ]);

        /*
        // Seems okay to remove this because these vertices are not used again,
        // but if problems arise, this needs to be uncommented because the above
        // change is destructive and may need to be undone before proceeding.
        if (drawing2D() && a[MZ] == 0) {
          a[X] -= 0.01;
          a[Y] -= 0.01;
          a[VX] -= 0.01*a[VW];
          a[VY] -= 0.01*a[VW];
          b[X] -= 0.01;
          b[Y] -= 0.01;
          b[VX] -= 0.01*b[VW];
          b[VY] -= 0.01*b[VW];
        }
        */

      line.draw();
    }
  }


  /**
   * Handle echoing line data to a raw shape recording renderer. This has been
   * broken out of the renderLines() procedure so that renderLines() can be
   * optimized per-renderer without having to deal with this code. This code,
   * for instance, will stay the same when OpenGL is in use, but renderLines()
   * can be optimized significantly.
   * <br/> <br/>
   * Values for start and stop are specified, so that in the future, sorted
   * rendering can be implemented, which will require sequences of lines,
   * triangles, or points to be rendered in the neighborhood of one another.
   * That is, if we're gonna depth sort, we can't just draw all the triangles
   * and then draw all the lines, cuz that defeats the purpose.
   */
  protected void rawLines(int start, int stop) {
    raw.colorMode(RGB, 1);
    raw.noFill();
    raw.beginShape(LINES);

    for (int i = start; i < stop; i++) {
      float a[] = vertices[lines[i][VERTEX1]];
      float b[] = vertices[lines[i][VERTEX2]];
      raw.strokeWeight(vertices[lines[i][VERTEX2]][SW]);

      if (raw.is3D()) {
        if ((a[VW] != 0) && (b[VW] != 0)) {
          raw.stroke(a[SR], a[SG], a[SB], a[SA]);
          raw.vertex(a[VX] / a[VW], a[VY] / a[VW], a[VZ] / a[VW]);
          raw.stroke(b[SR], b[SG], b[SB], b[SA]);
          raw.vertex(b[VX] / b[VW], b[VY] / b[VW], b[VZ] / b[VW]);
        }
      } else if (raw.is2D()) {
        raw.stroke(a[SR], a[SG], a[SB], a[SA]);
        raw.vertex(a[TX], a[TY]);
        raw.stroke(b[SR], b[SG], b[SB], b[SA]);
        raw.vertex(b[TX], b[TY]);
      }
    }
    raw.endShape();
  }



  /////////////////////////////////////////////////////////////////////////////

  // TRIANGLES


  protected void addTriangle(int a, int b, int c) {
    addTriangleWithClip(a, b, c);
  }


  protected final void addTriangleWithClip(int a, int b, int c) {
    boolean aClipped = false;
    boolean bClipped = false;
    int clippedCount = 0;

    // This is a hack for temporary clipping. Clipping still needs to
    // be implemented properly, however. Please help!
    // http://dev.processing.org/bugs/show_bug.cgi?id=1393
    cameraNear = -8;
    if (vertices[a][VZ] > cameraNear) {
      aClipped = true;
      clippedCount++;
    }
    if (vertices[b][VZ] > cameraNear) {
      bClipped = true;
      clippedCount++;
    }
    if (vertices[c][VZ] > cameraNear) {
      //cClipped = true;
      clippedCount++;
    }
    if (clippedCount == 0) {
//        if (vertices[a][VZ] < cameraFar &&
//                vertices[b][VZ] < cameraFar &&
//                vertices[c][VZ] < cameraFar) {
      addTriangleWithoutClip(a, b, c);
//        }

//    } else if (true) {
//        return;

    } else if (clippedCount == 3) {
      // In this case there is only one visible point.            |/|
      // So we'll have to make two new points on the clip line   <| |
      // and add that triangle instead.                           |\|

    } else if (clippedCount == 2) {
      //System.out.println("Clipped two");

      int ca, cb, cc, cd, ce;
      if (!aClipped) {
        ca = a;
        cb = b;
        cc = c;
      }
      else if (!bClipped) {
        ca = b;
        cb = a;
        cc = c;
      }
      else { //if (!cClipped) {
        ca = c;
        cb = b;
        cc = a;
      }

      cd = interpolateClipVertex(ca, cb);
      ce = interpolateClipVertex(ca, cc);
      addTriangleWithoutClip(ca, cd, ce);

    } else { // (clippedCount == 1) {
      //                                                          . |
      // In this case there are two visible points.               |\|
      // So we'll have to make two new points on the clip line    | |>
      // and then add two new triangles.                          |/|
      //                                                          . |
      //System.out.println("Clipped one");
      int ca, cb, cc, cd, ce;
      if (aClipped) {
        //System.out.println("aClipped");
        ca = c;
        cb = b;
        cc = a;
      }
      else if (bClipped) {
        //System.out.println("bClipped");
        ca = a;
        cb = c;
        cc = b;
      }
      else { //if (cClipped) {
        //System.out.println("cClipped");
        ca = a;
        cb = b;
        cc = c;
      }

      cd = interpolateClipVertex(ca, cc);
      ce = interpolateClipVertex(cb, cc);
      addTriangleWithoutClip(ca, cd, cb);
      //System.out.println("ca: " + ca + ", " + vertices[ca][VX] + ", " + vertices[ca][VY] + ", " + vertices[ca][VZ]);
      //System.out.println("cd: " + cd + ", " + vertices[cd][VX] + ", " + vertices[cd][VY] + ", " + vertices[cd][VZ]);
      //System.out.println("cb: " + cb + ", " + vertices[cb][VX] + ", " + vertices[cb][VY] + ", " + vertices[cb][VZ]);
      addTriangleWithoutClip(cb, cd, ce);
    }
  }


  protected final int interpolateClipVertex(int a, int b) {
    float[] va;
    float[] vb;
    // Set up va, vb such that va[VZ] >= vb[VZ]
    if (vertices[a][VZ] < vertices[b][VZ]) {
      va = vertices[b];
      vb = vertices[a];
    }
    else {
      va = vertices[a];
      vb = vertices[b];
    }
    float az = va[VZ];
    float bz = vb[VZ];

    float dz = az - bz;
    // If they have the same z, just use pt. a.
    if (dz == 0) {
      return a;
    }
    //float pa = (az - cameraNear) / dz;
    //float pb = (cameraNear - bz) / dz;
    float pa = (cameraNear - bz) / dz;
    float pb = 1 - pa;

    vertex(pa * va[X] + pb * vb[X],
           pa * va[Y] + pb * vb[Y],
           pa * va[Z] + pb * vb[Z]);
    int irv = vertexCount - 1;
    shapeLastPlusClipped++;

    float[] rv = vertices[irv];

    rv[TX] = pa * va[TX] + pb * vb[TX];
    rv[TY] = pa * va[TY] + pb * vb[TY];
    rv[TZ] = pa * va[TZ] + pb * vb[TZ];

    rv[VX] = pa * va[VX] + pb * vb[VX];
    rv[VY] = pa * va[VY] + pb * vb[VY];
    rv[VZ] = pa * va[VZ] + pb * vb[VZ];
    rv[VW] = pa * va[VW] + pb * vb[VW];

    rv[R] = pa * va[R] + pb * vb[R];
    rv[G] = pa * va[G] + pb * vb[G];
    rv[B] = pa * va[B] + pb * vb[B];
    rv[A] = pa * va[A] + pb * vb[A];

    rv[U] = pa * va[U] + pb * vb[U];
    rv[V] = pa * va[V] + pb * vb[V];

    rv[SR] = pa * va[SR] + pb * vb[SR];
    rv[SG] = pa * va[SG] + pb * vb[SG];
    rv[SB] = pa * va[SB] + pb * vb[SB];
    rv[SA] = pa * va[SA] + pb * vb[SA];

    rv[NX] = pa * va[NX] + pb * vb[NX];
    rv[NY] = pa * va[NY] + pb * vb[NY];
    rv[NZ] = pa * va[NZ] + pb * vb[NZ];

//    rv[SW] = pa * va[SW] + pb * vb[SW];

    rv[AR] = pa * va[AR] + pb * vb[AR];
    rv[AG] = pa * va[AG] + pb * vb[AG];
    rv[AB] = pa * va[AB] + pb * vb[AB];

    rv[SPR] = pa * va[SPR] + pb * vb[SPR];
    rv[SPG] = pa * va[SPG] + pb * vb[SPG];
    rv[SPB] = pa * va[SPB] + pb * vb[SPB];
    //rv[SPA] = pa * va[SPA] + pb * vb[SPA];

    rv[ER] = pa * va[ER] + pb * vb[ER];
    rv[EG] = pa * va[EG] + pb * vb[EG];
    rv[EB] = pa * va[EB] + pb * vb[EB];

    rv[SHINE] = pa * va[SHINE] + pb * vb[SHINE];

    rv[BEEN_LIT] = 0;

    return irv;
  }


  protected final void addTriangleWithoutClip(int a, int b, int c) {
    if (triangleCount == triangles.length) {
      int temp[][] = new int[triangleCount<<1][TRIANGLE_FIELD_COUNT];
      System.arraycopy(triangles, 0, temp, 0, triangleCount);
      triangles = temp;
      //message(CHATTER, "allocating more triangles " + triangles.length);
      float ftemp[][][] = new float[triangleCount<<1][3][TRI_COLOR_COUNT];
      System.arraycopy(triangleColors, 0, ftemp, 0, triangleCount);
      triangleColors = ftemp;
    }
    triangles[triangleCount][VERTEX1] = a;
    triangles[triangleCount][VERTEX2] = b;
    triangles[triangleCount][VERTEX3] = c;

    if (textureImage == null) {
      triangles[triangleCount][TEXTURE_INDEX] = -1;
    } else {
      triangles[triangleCount][TEXTURE_INDEX] = textureIndex;
    }

//    triangles[triangleCount][INDEX] = shape_index;
    triangleCount++;
  }


  /**
   * Triangulate the current polygon.
   * <BR> <BR>
   * Simple ear clipping polygon triangulation adapted from code by
   * John W. Ratcliff (jratcliff at verant.com). Presumably
   * <A HREF="http://www.flipcode.org/cgi-bin/fcarticles.cgi?show=63943">this</A>
   * bit of code from the web.
   */
  protected void addPolygonTriangles() {
    if (vertexOrder.length != vertices.length) {
      int[] temp = new int[vertices.length];
      // vertex_start may not be zero, might need to keep old stuff around
      // also, copy vertexOrder.length, not vertexCount because vertexCount
      // may be larger than vertexOrder.length (since this is a post-processing
      // step that happens after the vertex arrays are built).
      PApplet.arrayCopy(vertexOrder, temp, vertexOrder.length);
      vertexOrder = temp;
    }

    // this clipping algorithm only works in 2D, so in cases where a
    // polygon is drawn perpendicular to the z-axis, the area will be zero,
    // and triangulation will fail. as such, when the area calculates to
    // zero, figure out whether x or y is empty, and calculate based on the
    // two dimensions that actually contain information.
    // http://dev.processing.org/bugs/show_bug.cgi?id=111
    int d1 = X;
    int d2 = Y;
    // this brings up the nastier point that there may be cases where
    // a polygon is irregular in space and will throw off the
    // clockwise/counterclockwise calculation. for instance, if clockwise
    // relative to x and z, but counter relative to y and z or something
    // like that.. will wait to see if this is in fact a problem before
    // hurting my head on the math.

    /*
    // trying to track down bug #774
    for (int i = vertex_start; i < vertex_end; i++) {
      if (i > vertex_start) {
        if (vertices[i-1][MX] == vertices[i][MX] &&
            vertices[i-1][MY] == vertices[i][MY]) {
          System.out.print("**** " );
        }
      }
      System.out.println(i + " " + vertices[i][MX] + " " + vertices[i][MY]);
    }
    System.out.println();
    */

    // first we check if the polygon goes clockwise or counterclockwise
    float area = 0;
    for (int p = shapeLast - 1, q = shapeFirst; q < shapeLast; p = q++) {
      area += (vertices[q][d1] * vertices[p][d2] -
               vertices[p][d1] * vertices[q][d2]);
    }
    // rather than checking for the perpendicular case first, only do it
    // when the area calculates to zero. checking for perpendicular would be
    // a needless waste of time for the 99% case.
    if (area == 0) {
      // figure out which dimension is the perpendicular axis
      boolean foundValidX = false;
      boolean foundValidY = false;

      for (int i = shapeFirst; i < shapeLast; i++) {
        for (int j = i; j < shapeLast; j++){
          if ( vertices[i][X] != vertices[j][X] ) foundValidX = true;
          if ( vertices[i][Y] != vertices[j][Y] ) foundValidY = true;
        }
      }

      if (foundValidX) {
        //d1 = MX;  // already the case
        d2 = Z;
      } else if (foundValidY) {
        // ermm.. which is the proper order for cw/ccw here?
        d1 = Y;
        d2 = Z;
      } else {
        // screw it, this polygon is just f-ed up
        return;
      }

      // re-calculate the area, with what should be good values
      for (int p = shapeLast - 1, q = shapeFirst; q < shapeLast; p = q++) {
        area += (vertices[q][d1] * vertices[p][d2] -
                 vertices[p][d1] * vertices[q][d2]);
      }
    }

    // don't allow polygons to come back and meet themselves,
    // otherwise it will anger the triangulator
    // http://dev.processing.org/bugs/show_bug.cgi?id=97
    float vfirst[] = vertices[shapeFirst];
    float vlast[] = vertices[shapeLast-1];
    if ((abs(vfirst[X] - vlast[X]) < EPSILON) &&
        (abs(vfirst[Y] - vlast[Y]) < EPSILON) &&
        (abs(vfirst[Z] - vlast[Z]) < EPSILON)) {
      shapeLast--;
    }

    // then sort the vertices so they are always in a counterclockwise order
    int j = 0;
    if (area > 0) {
      for (int i = shapeFirst; i < shapeLast; i++) {
        j = i - shapeFirst;
        vertexOrder[j] = i;
      }
    } else {
      for (int i = shapeFirst; i < shapeLast; i++) {
        j = i - shapeFirst;
        vertexOrder[j] = (shapeLast - 1) - j;
      }
    }

    // remove vc-2 Vertices, creating 1 triangle every time
    int vc = shapeLast - shapeFirst;
    int count = 2*vc;  // complex polygon detection

    for (int m = 0, v = vc - 1; vc > 2; ) {
      boolean snip = true;

      // if we start over again, is a complex polygon
      if (0 >= (count--)) {
        break; // triangulation failed
      }

      // get 3 consecutive vertices <u,v,w>
      int u = v ; if (vc <= u) u = 0;    // previous
      v = u + 1; if (vc <= v) v = 0;     // current
      int w = v + 1; if (vc <= w) w = 0; // next

      // Upgrade values to doubles, and multiply by 10 so that we can have
      // some better accuracy as we tessellate. This seems to have negligible
      // speed differences on Windows and Intel Macs, but causes a 50% speed
      // drop for PPC Macs with the bug's example code that draws ~200 points
      // in a concave polygon. Apple has abandoned PPC so we may as well too.
      // http://dev.processing.org/bugs/show_bug.cgi?id=774

      // triangle A B C
      double Ax = -10 * vertices[vertexOrder[u]][d1];
      double Ay =  10 * vertices[vertexOrder[u]][d2];
      double Bx = -10 * vertices[vertexOrder[v]][d1];
      double By =  10 * vertices[vertexOrder[v]][d2];
      double Cx = -10 * vertices[vertexOrder[w]][d1];
      double Cy =  10 * vertices[vertexOrder[w]][d2];

      // first we check if <u,v,w> continues going ccw
      if (EPSILON > (((Bx-Ax) * (Cy-Ay)) - ((By-Ay) * (Cx-Ax)))) {
        continue;
      }

      for (int p = 0; p < vc; p++) {
        if ((p == u) || (p == v) || (p == w)) {
          continue;
        }

        double Px = -10 * vertices[vertexOrder[p]][d1];
        double Py =  10 * vertices[vertexOrder[p]][d2];

        double ax  = Cx - Bx;  double ay  = Cy - By;
        double bx  = Ax - Cx;  double by  = Ay - Cy;
        double cx  = Bx - Ax;  double cy  = By - Ay;
        double apx = Px - Ax;  double apy = Py - Ay;
        double bpx = Px - Bx;  double bpy = Py - By;
        double cpx = Px - Cx;  double cpy = Py - Cy;

        double aCROSSbp = ax * bpy - ay * bpx;
        double cCROSSap = cx * apy - cy * apx;
        double bCROSScp = bx * cpy - by * cpx;

        if ((aCROSSbp >= 0.0) && (bCROSScp >= 0.0) && (cCROSSap >= 0.0)) {
          snip = false;
        }
      }

      if (snip) {
        addTriangle(vertexOrder[u], vertexOrder[v], vertexOrder[w]);

        m++;

        // remove v from remaining polygon
        for (int s = v, t = v + 1; t < vc; s++, t++) {
          vertexOrder[s] = vertexOrder[t];
        }
        vc--;

        // reset error detection counter
        count = 2 * vc;
      }
    }
  }


  private void toWorldNormal(float nx, float ny, float nz, float[] out) {
    out[0] =
      modelviewInv.m00*nx + modelviewInv.m10*ny +
      modelviewInv.m20*nz + modelviewInv.m30;
    out[1] =
      modelviewInv.m01*nx + modelviewInv.m11*ny +
      modelviewInv.m21*nz + modelviewInv.m31;
    out[2] =
      modelviewInv.m02*nx + modelviewInv.m12*ny +
      modelviewInv.m22*nz + modelviewInv.m32;
    out[3] =
      modelviewInv.m03*nx + modelviewInv.m13*ny +
      modelviewInv.m23*nz + modelviewInv.m33;

    if (out[3] != 0 && out[3] != 1) {
      // divide by perspective coordinate
      out[0] /= out[3]; out[1] /= out[3]; out[2] /= out[3];
    }
    out[3] = 1;

    float nlen = mag(out[0], out[1], out[2]);  // normalize
    if (nlen != 0 && nlen != 1) {
      out[0] /= nlen; out[1] /= nlen; out[2] /= nlen;
    }
  }


  //private PVector calcLightingNorm = new PVector();
  //private PVector calcLightingWorldNorm = new PVector();
  float[] worldNormal = new float[4];


  private void calcLightingContribution(int vIndex,
                                        float[] contribution) {
    calcLightingContribution(vIndex, contribution, false);
  }


  private void calcLightingContribution(int vIndex,
                                        float[] contribution,
                                        boolean normalIsWorld) {
    float[] v = vertices[vIndex];

    float sr = v[SPR];
    float sg = v[SPG];
    float sb = v[SPB];

    float wx = v[VX];
    float wy = v[VY];
    float wz = v[VZ];
    float shine = v[SHINE];

    float nx = v[NX];
    float ny = v[NY];
    float nz = v[NZ];

    if (!normalIsWorld) {
//      System.out.println("um, hello?");
//      calcLightingNorm.set(nx, ny, nz);
//      //modelviewInv.mult(calcLightingNorm, calcLightingWorldNorm);
//
////      PMatrix3D mvi = modelViewInv;
////      float ox = mvi.m00*nx + mvi.m10*ny + mvi*m20+nz +
//      modelviewInv.cmult(calcLightingNorm, calcLightingWorldNorm);
//
//      calcLightingWorldNorm.normalize();
//      nx = calcLightingWorldNorm.x;
//      ny = calcLightingWorldNorm.y;
//      nz = calcLightingWorldNorm.z;

      toWorldNormal(v[NX], v[NY], v[NZ], worldNormal);
      nx = worldNormal[X];
      ny = worldNormal[Y];
      nz = worldNormal[Z];

//      float wnx = modelviewInv.multX(nx, ny, nz);
//      float wny = modelviewInv.multY(nx, ny, nz);
//      float wnz = modelviewInv.multZ(nx, ny, nz);
//      float wnw = modelviewInv.multW(nx, ny, nz);

//      if (wnw != 0 && wnw != 1) {
//        wnx /= wnw;
//        wny /= wnw;
//        wnz /= wnw;
//      }
//      float nlen = mag(wnx, wny, wnw);
//      if (nlen != 0 && nlen != 1) {
//        nx = wnx / nlen;
//        ny = wny / nlen;
//        nz = wnz / nlen;
//      } else {
//        nx = wnx;
//        ny = wny;
//        nz = wnz;
//      }
//      */
    } else {
      nx = v[NX];
      ny = v[NY];
      nz = v[NZ];
    }

    // Since the camera space == world space,
    // we can test for visibility by the dot product of
    // the normal with the direction from pt. to eye.
    float dir = dot(nx, ny, nz, -wx, -wy, -wz);
    // If normal is away from camera, choose its opposite.
    // If we add backface culling, this will be backfacing
    // (but since this is per vertex, it's more complicated)
    if (dir < 0) {
      nx = -nx;
      ny = -ny;
      nz = -nz;
    }

    // These two terms will sum the contributions from the various lights
    contribution[LIGHT_AMBIENT_R] = 0;
    contribution[LIGHT_AMBIENT_G] = 0;
    contribution[LIGHT_AMBIENT_B] = 0;

    contribution[LIGHT_DIFFUSE_R] = 0;
    contribution[LIGHT_DIFFUSE_G] = 0;
    contribution[LIGHT_DIFFUSE_B] = 0;

    contribution[LIGHT_SPECULAR_R] = 0;
    contribution[LIGHT_SPECULAR_G] = 0;
    contribution[LIGHT_SPECULAR_B] = 0;

    // for (int i = 0; i < MAX_LIGHTS; i++) {
    // if (!light[i]) continue;
    for (int i = 0; i < lightCount; i++) {

      float denom = lightFalloffConstant[i];
      float spotTerm = 1;

      if (lightType[i] == AMBIENT) {
        if (lightFalloffQuadratic[i] != 0 || lightFalloffLinear[i] != 0) {
          // Falloff depends on distance
          float distSq = mag(lightPosition[i].x - wx,
                             lightPosition[i].y - wy,
                             lightPosition[i].z - wz);
          denom +=
            lightFalloffQuadratic[i] * distSq +
            lightFalloffLinear[i] * sqrt(distSq);
        }
        if (denom == 0) denom = 1;

        contribution[LIGHT_AMBIENT_R] += lightDiffuse[i][0] / denom;
        contribution[LIGHT_AMBIENT_G] += lightDiffuse[i][1] / denom;
        contribution[LIGHT_AMBIENT_B] += lightDiffuse[i][2] / denom;

      } else {
        // If not ambient, we must deal with direction

        // li is the vector from the vertex to the light
        float lix, liy, liz;
        float lightDir_dot_li = 0;
        float n_dot_li = 0;

        if (lightType[i] == DIRECTIONAL) {
          lix = -lightNormal[i].x;
          liy = -lightNormal[i].y;
          liz = -lightNormal[i].z;
          denom = 1;
          n_dot_li = (nx * lix + ny * liy + nz * liz);
          // If light is lighting the face away from the camera, ditch
          if (n_dot_li <= 0) {
            continue;
          }
        } else { // Point or spot light (must deal also with light location)
          lix = lightPosition[i].x - wx;
          liy = lightPosition[i].y - wy;
          liz = lightPosition[i].z - wz;
          // normalize
          float distSq = mag(lix, liy, liz);
          if (distSq != 0) {
            lix /= distSq;
            liy /= distSq;
            liz /= distSq;
          }
          n_dot_li = (nx * lix + ny * liy + nz * liz);
          // If light is lighting the face away from the camera, ditch
          if (n_dot_li <= 0) {
            continue;
          }

          if (lightType[i] == SPOT) { // Must deal with spot cone
            lightDir_dot_li =
              -(lightNormal[i].x * lix +
                lightNormal[i].y * liy +
                lightNormal[i].z * liz);
            // Outside of spot cone
            if (lightDir_dot_li <= lightSpotAngleCos[i]) {
              continue;
            }
            spotTerm = (float) Math.pow(lightDir_dot_li, lightSpotConcentration[i]);
          }

          if (lightFalloffQuadratic[i] != 0 || lightFalloffLinear[i] != 0) {
            // Falloff depends on distance
            denom +=
              lightFalloffQuadratic[i] * distSq +
              lightFalloffLinear[i] * (float) sqrt(distSq);
          }
        }
        // Directional, point, or spot light:

        // We know n_dot_li > 0 from above "continues"

        if (denom == 0)
          denom = 1;
        float mul = n_dot_li * spotTerm / denom;
        contribution[LIGHT_DIFFUSE_R] += lightDiffuse[i][0] * mul;
        contribution[LIGHT_DIFFUSE_G] += lightDiffuse[i][1] * mul;
        contribution[LIGHT_DIFFUSE_B] += lightDiffuse[i][2] * mul;

        // SPECULAR

        // If the material and light have a specular component.
        if ((sr > 0 || sg > 0 || sb > 0) &&
            (lightSpecular[i][0] > 0 ||
             lightSpecular[i][1] > 0 ||
             lightSpecular[i][2] > 0)) {

          float vmag = mag(wx, wy, wz);
          if (vmag != 0) {
            wx /= vmag;
            wy /= vmag;
            wz /= vmag;
          }
          float sx = lix - wx;
          float sy = liy - wy;
          float sz = liz - wz;
          vmag = mag(sx, sy, sz);
          if (vmag != 0) {
            sx /= vmag;
            sy /= vmag;
            sz /= vmag;
          }
          float s_dot_n = (sx * nx + sy * ny + sz * nz);

          if (s_dot_n > 0) {
            s_dot_n = (float) Math.pow(s_dot_n, shine);
            mul = s_dot_n * spotTerm / denom;
            contribution[LIGHT_SPECULAR_R] += lightSpecular[i][0] * mul;
            contribution[LIGHT_SPECULAR_G] += lightSpecular[i][1] * mul;
            contribution[LIGHT_SPECULAR_B] += lightSpecular[i][2] * mul;
          }

        }
      }
    }
    return;
  }


  // Multiply the lighting contribution into the vertex's colors.
  // Only do this when there is ONE lighting per vertex
  // (MANUAL_VERTEX_NORMAL or SHAPE_NORMAL mode).
  private void applyLightingContribution(int vIndex, float[] contribution) {
    float[] v = vertices[vIndex];

    v[R] = clamp(v[ER] + v[AR] * contribution[LIGHT_AMBIENT_R] + v[DR] * contribution[LIGHT_DIFFUSE_R]);
    v[G] = clamp(v[EG] + v[AG] * contribution[LIGHT_AMBIENT_G] + v[DG] * contribution[LIGHT_DIFFUSE_G]);
    v[B] = clamp(v[EB] + v[AB] * contribution[LIGHT_AMBIENT_B] + v[DB] * contribution[LIGHT_DIFFUSE_B]);
    v[A] = clamp(v[DA]);

    v[SPR] = clamp(v[SPR] * contribution[LIGHT_SPECULAR_R]);
    v[SPG] = clamp(v[SPG] * contribution[LIGHT_SPECULAR_G]);
    v[SPB] = clamp(v[SPB] * contribution[LIGHT_SPECULAR_B]);
    //v[SPA] = min(1, v[SPA]);

    v[BEEN_LIT] = 1;
  }


  private void lightVertex(int vIndex, float[] contribution) {
    calcLightingContribution(vIndex, contribution);
    applyLightingContribution(vIndex, contribution);
  }


  private void lightUnlitVertex(int vIndex, float[] contribution) {
    if (vertices[vIndex][BEEN_LIT] == 0) {
      lightVertex(vIndex, contribution);
    }
  }


  private void copyPrelitVertexColor(int triIndex, int index, int colorIndex) {
    float[] triColor = triangleColors[triIndex][colorIndex];
    float[] v = vertices[index];

    triColor[TRI_DIFFUSE_R] = v[R];
    triColor[TRI_DIFFUSE_G] = v[G];
    triColor[TRI_DIFFUSE_B] = v[B];
    triColor[TRI_DIFFUSE_A] = v[A];
    triColor[TRI_SPECULAR_R] = v[SPR];
    triColor[TRI_SPECULAR_G] = v[SPG];
    triColor[TRI_SPECULAR_B] = v[SPB];
    //triColor[TRI_SPECULAR_A] = v[SPA];
  }


  private void copyVertexColor(int triIndex, int index, int colorIndex,
                               float[] contrib) {
    float[] triColor = triangleColors[triIndex][colorIndex];
    float[] v = vertices[index];

    triColor[TRI_DIFFUSE_R] =
      clamp(v[ER] + v[AR] * contrib[LIGHT_AMBIENT_R] + v[DR] * contrib[LIGHT_DIFFUSE_R]);
    triColor[TRI_DIFFUSE_G] =
      clamp(v[EG] + v[AG] * contrib[LIGHT_AMBIENT_G] + v[DG] * contrib[LIGHT_DIFFUSE_G]);
    triColor[TRI_DIFFUSE_B] =
      clamp(v[EB] + v[AB] * contrib[LIGHT_AMBIENT_B] + v[DB] * contrib[LIGHT_DIFFUSE_B]);
    triColor[TRI_DIFFUSE_A] = clamp(v[DA]);

    triColor[TRI_SPECULAR_R] = clamp(v[SPR] * contrib[LIGHT_SPECULAR_R]);
    triColor[TRI_SPECULAR_G] = clamp(v[SPG] * contrib[LIGHT_SPECULAR_G]);
    triColor[TRI_SPECULAR_B] = clamp(v[SPB] * contrib[LIGHT_SPECULAR_B]);
  }


  private void lightTriangle(int triIndex, float[] lightContribution) {
    int vIndex = triangles[triIndex][VERTEX1];
    copyVertexColor(triIndex, vIndex, 0, lightContribution);
    vIndex = triangles[triIndex][VERTEX2];
    copyVertexColor(triIndex, vIndex, 1, lightContribution);
    vIndex = triangles[triIndex][VERTEX3];
    copyVertexColor(triIndex, vIndex, 2, lightContribution);
  }


  private void lightTriangle(int triIndex) {
    int vIndex;

    // Handle lighting on, but no lights (in this case, just use emissive)
    // This wont be used currently because lightCount == 0 is don't use
    // lighting at all... So. OK. If that ever changes, use the below:
    /*
    if (lightCount == 0) {
      vIndex = triangles[triIndex][VERTEX1];
      copy_emissive_vertex_color_to_triangle(triIndex, vIndex, 0);
      vIndex = triangles[triIndex][VERTEX2];
      copy_emissive_vertex_color_to_triangle(triIndex, vIndex, 1);
      vIndex = triangles[triIndex][VERTEX3];
      copy_emissive_vertex_color_to_triangle(triIndex, vIndex, 2);
      return;
    }
    */

    // In MANUAL_VERTEX_NORMAL mode, we have a specific normal
    // for each vertex. In that case, we light any verts that
    // haven't already been lit and copy their colors straight
    // into the triangle.
    if (normalMode == NORMAL_MODE_VERTEX) {
      vIndex = triangles[triIndex][VERTEX1];
      lightUnlitVertex(vIndex, tempLightingContribution);
      copyPrelitVertexColor(triIndex, vIndex, 0);

      vIndex = triangles[triIndex][VERTEX2];
      lightUnlitVertex(vIndex, tempLightingContribution);
      copyPrelitVertexColor(triIndex, vIndex, 1);

      vIndex = triangles[triIndex][VERTEX3];
      lightUnlitVertex(vIndex, tempLightingContribution);
      copyPrelitVertexColor(triIndex, vIndex, 2);

    }

    // If the lighting doesn't depend on the vertex position, do the
    // following: We've already dealt with NORMAL_MODE_SHAPE mode before
    // we got into this function, so here we only have to deal with
    // NORMAL_MODE_AUTO. So we calculate the normal for this triangle,
    // and use that for the lighting.
    else if (!lightingDependsOnVertexPosition) {
      vIndex = triangles[triIndex][VERTEX1];
      int vIndex2 = triangles[triIndex][VERTEX2];
      int vIndex3 = triangles[triIndex][VERTEX3];

      /*
      dv1[0] = vertices[vIndex2][VX] - vertices[vIndex][VX];
      dv1[1] = vertices[vIndex2][VY] - vertices[vIndex][VY];
      dv1[2] = vertices[vIndex2][VZ] - vertices[vIndex][VZ];

      dv2[0] = vertices[vIndex3][VX] - vertices[vIndex][VX];
      dv2[1] = vertices[vIndex3][VY] - vertices[vIndex][VY];
      dv2[2] = vertices
