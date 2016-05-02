/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2004-09 Ben Fry and Casey Reas
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

import java.awt.*;
import java.util.HashMap;


/**
 * Main graphics and rendering context, as well as the base API implementation for processing "core".
 * Use this class if you need to draw into an off-screen graphics buffer.
 * A PGraphics object can be constructed with the <b>createGraphics()</b> function.
 * The <b>beginDraw()</b> and <b>endDraw()</b> methods (see above example) are necessary to set up the buffer and to finalize it.
 * The fields and methods for this class are extensive;
 * for a complete list visit the developer's reference: http://dev.processing.org/reference/core/
 * =advanced
 * Main graphics and rendering context, as well as the base API implementation.
 *
 * <h2>Subclassing and initializing PGraphics objects</h2>
 * Starting in release 0149, subclasses of PGraphics are handled differently.
 * The constructor for subclasses takes no parameters, instead a series of
 * functions are called by the hosting PApplet to specify its attributes.
 * <ul>
 * <li>setParent(PApplet) - is called to specify the parent PApplet.
 * <li>setPrimary(boolean) - called with true if this PGraphics will be the
 * primary drawing surface used by the sketch, or false if not.
 * <li>setPath(String) - called when the renderer needs a filename or output
 * path, such as with the PDF or DXF renderers.
 * <li>setSize(int, int) - this is called last, at which point it's safe for
 * the renderer to complete its initialization routine.
 * </ul>
 * The functions were broken out because of the growing number of parameters
 * such as these that might be used by a renderer, yet with the exception of
 * setSize(), it's not clear which will be necessary. So while the size could
 * be passed in to the constructor instead of a setSize() function, a function
 * would still be needed that would notify the renderer that it was time to
 * finish its initialization. Thus, setSize() simply does both.
 *
 * <h2>Know your rights: public vs. private methods</h2>
 * Methods that are protected are often subclassed by other renderers, however
 * they are not set 'public' because they shouldn't be part of the user-facing
 * public API accessible from PApplet. That is, we don't want sketches calling
 * textModeCheck() or vertexTexture() directly.
 *
 * <h2>Handling warnings and exceptions</h2>
 * Methods that are unavailable generally show a warning, unless their lack of
 * availability will soon cause another exception. For instance, if a method
 * like getMatrix() returns null because it is unavailable, an exception will
 * be thrown stating that the method is unavailable, rather than waiting for
 * the NullPointerException that will occur when the sketch tries to use that
 * method. As of release 0149, warnings will only be shown once, and exceptions
 * have been changed to warnings where possible.
 *
 * <h2>Using xxxxImpl() for subclassing smoothness</h2>
 * The xxxImpl() methods are generally renderer-specific handling for some
 * subset if tasks for a particular function (vague enough for you?) For
 * instance, imageImpl() handles drawing an image whose x/y/w/h and u/v coords
 * have been specified, and screen placement (independent of imageMode) has
 * been determined. There's no point in all renderers implementing the
 * <tt>if (imageMode == BLAH)</tt> placement/sizing logic, so that's handled
 * by PGraphics, which then calls imageImpl() once all that is figured out.
 *
 * <h2>His brother PImage</h2>
 * PGraphics subclasses PImage so that it can be drawn and manipulated in a
 * similar fashion. As such, many methods are inherited from PGraphics,
 * though many are unavailable: for instance, resize() is not likely to be
 * implemented; the same goes for mask(), depending on the situation.
 *
 * <h2>What's in PGraphics, what ain't</h2>
 * For the benefit of subclasses, as much as possible has been placed inside
 * PGraphics. For instance, bezier interpolation code and implementations of
 * the strokeCap() method (that simply sets the strokeCap variable) are
 * handled here. Features that will vary widely between renderers are located
 * inside the subclasses themselves. For instance, all matrix handling code
 * is per-renderer: Java 2D uses its own AffineTransform, P2D uses a PMatrix2D,
 * and PGraphics3D needs to keep continually update forward and reverse
 * transformations. A proper (future) OpenGL implementation will have all its
 * matrix madness handled by the card. Lighting also falls under this
 * category, however the base material property settings (emissive, specular,
 * et al.) are handled in PGraphics because they use the standard colorMode()
 * logic. Subclasses should override methods like emissiveFromCalc(), which
 * is a point where a valid color has been defined internally, and can be
 * applied in some manner based on the calcXxxx values.
 *
 * <h2>What's in the PGraphics documentation, what ain't</h2>
 * Some things are noted here, some things are not. For public API, always
 * refer to the <a href="http://processing.org/reference">reference</A>
 * on Processing.org for proper explanations. <b>No attempt has been made to
 * keep the javadoc up to date or complete.</b> It's an enormous task for
 * which we simply do not have the time. That is, it's not something that
 * to be done once&mdash;it's a matter of keeping the multiple references
 * synchronized (to say nothing of the translation issues), while targeting
 * them for their separate audiences. Ouch.
 * 
 * We're working right now on synchronizing the two references, so the website reference
 * is generated from the javadoc comments. Yay.
 * 
 * @webref rendering
 * @instanceName graphics any object of the type PGraphics
 * @usage Web &amp; Application
 * @see processing.core.PApplet#createGraphics(int, int, String)
 */
public class PGraphics extends PImage implements PConstants {

  // ........................................................

  // width and height are already inherited from PImage


  /// width minus one (useful for many calculations)
  protected int width1;

  /// height minus one (useful for many calculations)
  protected int height1;

  /// width * height (useful for many calculations)
  public int pixelCount;

  /// true if smoothing is enabled (read-only)
  public boolean smooth = false;

  // ........................................................

  /// true if defaults() has been called a first time
  protected boolean settingsInited;

  /// set to a PGraphics object being used inside a beginRaw/endRaw() block
  protected PGraphics raw;

  // ........................................................

  /** path to the file being saved for this renderer (if any) */
  protected String path;

  /**
   * true if this is the main drawing surface for a particular sketch.
   * This would be set to false for an offscreen buffer or if it were
   * created any other way than size(). When this is set, the listeners
   * are also added to the sketch.
   */
  protected boolean primarySurface;

  // ........................................................

  /**
   * Array of hint[] items. These are hacks to get around various
   * temporary workarounds inside the environment.
   * <p/>
   * Note that this array cannot be static, as a hint() may result in a
   * runtime change specific to a renderer. For instance, calling
   * hint(DISABLE_DEPTH_TEST) has to call glDisable() right away on an
   * instance of PGraphicsOpenGL.
   * <p/>
   * The hints[] array is allocated early on because it might
   * be used inside beginDraw(), allocate(), etc.
   */
  protected boolean[] hints = new boolean[HINT_COUNT];


  ////////////////////////////////////////////////////////////

  // STYLE PROPERTIES

  // Also inherits imageMode() and smooth() (among others) from PImage.

  /** The current colorMode */
  public int colorMode; // = RGB;

  /** Max value for red (or hue) set by colorMode */
  public float colorModeX; // = 255;

  /** Max value for green (or saturation) set by colorMode */
  public float colorModeY; // = 255;

  /** Max value for blue (or value) set by colorMode */
  public float colorModeZ; // = 255;

  /** Max value for alpha set by colorMode */
  public float colorModeA; // = 255;

  /** True if colors are not in the range 0..1 */
  boolean colorModeScale; // = true;

  /** True if colorMode(RGB, 255) */
  boolean colorModeDefault; // = true;

  // ........................................................

  // Tint color for images

  /**
   * True if tint() is enabled (read-only).
   *
   * Using tint/tintColor seems a better option for naming than
   * tintEnabled/tint because the latter seems ugly, even though
   * g.tint as the actual color seems a little more intuitive,
   * it's just that g.tintEnabled is even more unintuitive.
   * Same goes for fill and stroke, et al.
   */
  public boolean tint;

  /** tint that was last set (read-only) */
  public int tintColor;

  protected boolean tintAlpha;
  protected float tintR, tintG, tintB, tintA;
  protected int tintRi, tintGi, tintBi, tintAi;

  // ........................................................

  // Fill color

  /** true if fill() is enabled, (read-only) */
  public boolean fill;

  /** fill that was last set (read-only) */
  public int fillColor = 0xffFFFFFF;

  protected boolean fillAlpha;
  protected float fillR, fillG, fillB, fillA;
  protected int fillRi, fillGi, fillBi, fillAi;

  // ........................................................

  // Stroke color

  /** true if stroke() is enabled, (read-only) */
  public boolean stroke;

  /** stroke that was last set (read-only) */
  public int strokeColor = 0xff000000;

  protected boolean strokeAlpha;
  protected float strokeR, strokeG, strokeB, strokeA;
  protected int strokeRi, strokeGi, strokeBi, strokeAi;

  // ........................................................

  // Additional stroke properties

  static protected final float DEFAULT_STROKE_WEIGHT = 1;
  static protected final int DEFAULT_STROKE_JOIN = MITER;
  static protected final int DEFAULT_STROKE_CAP = ROUND;

  /**
   * Last value set by strokeWeight() (read-only). This has a default
   * setting, rather than fighting with renderers about whether that
   * renderer supports thick lines.
   */
  public float strokeWeight = DEFAULT_STROKE_WEIGHT;

  /**
   * Set by strokeJoin() (read-only). This has a default setting
   * so that strokeJoin() need not be called by defaults,
   * because subclasses may not implement it (i.e. PGraphicsGL)
   */
  public int strokeJoin = DEFAULT_STROKE_JOIN;

  /**
   * Set by strokeCap() (read-only). This has a default setting
   * so that strokeCap() need not be called by defaults,
   * because subclasses may not implement it (i.e. PGraphicsGL)
   */
  public int strokeCap = DEFAULT_STROKE_CAP;

  // ........................................................

  // Shape placement properties

  // imageMode() is inherited from PImage

  /** The current rect mode (read-only) */
  public int rectMode;

  /** The current ellipse mode (read-only) */
  public int ellipseMode;

  /** The current shape alignment mode (read-only) */
  public int shapeMode;

  /** The current image alignment (read-only) */
  public int imageMode = CORNER;

  // ........................................................

  // Text and font properties

  /** The current text font (read-only) */
  public PFont textFont;

  /** The current text align (read-only) */
  public int textAlign = LEFT;

  /** The current vertical text alignment (read-only) */
  public int textAlignY = BASELINE;

  /** The current text mode (read-only) */
  public int textMode = MODEL;

  /** The current text size (read-only) */
  public float textSize;

  /** The current text leading (read-only) */
  public float textLeading;

  // ........................................................

  // Material properties

//  PMaterial material;
//  PMaterial[] materialStack;
//  int materialStackPointer;

  public float ambientR, ambientG, ambientB;
  public float specularR, specularG, specularB;
  public float emissiveR, emissiveG, emissiveB;
  public float shininess;


  // Style stack

  static final int STYLE_STACK_DEPTH = 64;
  PStyle[] styleStack = new PStyle[STYLE_STACK_DEPTH];
  int styleStackDepth;


  ////////////////////////////////////////////////////////////


  /** Last background color that was set, zero if an image */
  public int backgroundColor = 0xffCCCCCC;

  protected boolean backgroundAlpha;
  protected float backgroundR, backgroundG, backgroundB, backgroundA;
  protected int backgroundRi, backgroundGi, backgroundBi, backgroundAi;

  // ........................................................

  /**
   * Current model-view matrix transformation of the form m[row][column],
   * which is a "column vector" (as opposed to "row vector") matrix.
   */
//  PMatrix matrix;
//  public float m00, m01, m02, m03;
//  public float m10, m11, m12, m13;
//  public float m20, m21, m22, m23;
//  public float m30, m31, m32, m33;

//  static final int MATRIX_STACK_DEPTH = 32;
//  float[][] matrixStack = new float[MATRIX_STACK_DEPTH][16];
//  float[][] matrixInvStack = new float[MATRIX_STACK_DEPTH][16];
//  int matrixStackDepth;

  static final int MATRIX_STACK_DEPTH = 32;

  // ........................................................

  /**
   * Java AWT Image object associated with this renderer. For P2D and P3D,
   * this will be associated with their MemoryImageSource. For PGraphicsJava2D,
   * it will be the offscreen drawing buffer.
   */
  public Image image;

  // ........................................................

  // internal color for setting/calculating
  protected float calcR, calcG, calcB, calcA;
  protected int calcRi, calcGi, calcBi, calcAi;
  protected int calcColor;
  protected boolean calcAlpha;

  /** The last RGB value converted to HSB */
  int cacheHsbKey;
  /** Result of the last conversion to HSB */
  float[] cacheHsbValue = new float[3];

  // ........................................................

  /**
   * Type of shape passed to beginShape(),
   * zero if no shape is currently being drawn.
   */
  protected int shape;

  // vertices
  static final int DEFAULT_VERTICES = 512;
  protected float vertices[][] =
    new float[DEFAULT_VERTICES][VERTEX_FIELD_COUNT];
  protected int vertexCount; // total number of vertices

  // ........................................................

  protected boolean bezierInited = false;
  public int bezierDetail = 20;

  // used by both curve and bezier, so just init here
  protected PMatrix3D bezierBasisMatrix =
    new PMatrix3D(-1,  3, -3,  1,
                   3, -6,  3,  0,
                  -3,  3,  0,  0,
                   1,  0,  0,  0);

  //protected PMatrix3D bezierForwardMatrix;
  protected PMatrix3D bezierDrawMatrix;

  // ........................................................

  protected boolean curveInited = false;
  protected int curveDetail = 20;
  public float curveTightness = 0;
  // catmull-rom basis matrix, perhaps with optional s parameter
  protected PMatrix3D curveBasisMatrix;
  protected PMatrix3D curveDrawMatrix;

  protected PMatrix3D bezierBasisInverse;
  protected PMatrix3D curveToBezierMatrix;

  // ........................................................

  // spline vertices

  protected float curveVertices[][];
  protected int curveVertexCount;

  // ........................................................

  // precalculate sin/cos lookup tables [toxi]
  // circle resolution is determined from the actual used radii
  // passed to ellipse() method. this will automatically take any
  // scale transformations into account too

  // [toxi 031031]
  // changed table's precision to 0.5 degree steps
  // introduced new vars for more flexible code
  static final protected float sinLUT[];
  static final protected float cosLUT[];
  static final protected float SINCOS_PRECISION = 0.5f;
  static final protected int SINCOS_LENGTH = (int) (360f / SINCOS_PRECISION);
  static {
    sinLUT = new float[SINCOS_LENGTH];
    cosLUT = new float[SINCOS_LENGTH];
    for (int i = 0; i < SINCOS_LENGTH; i++) {
      sinLUT[i] = (float) Math.sin(i * DEG_TO_RAD * SINCOS_PRECISION);
      cosLUT[i] = (float) Math.cos(i * DEG_TO_RAD * SINCOS_PRECISION);
    }
  }

  // ........................................................

  /** The current font if a Java version of it is installed */
  //protected Font textFontNative;

  /** Metrics for the current native Java font */
  //protected FontMetrics textFontNativeMetrics;

  /** Last text position, because text often mixed on lines together */
  protected float textX, textY, textZ;

  /**
   * Internal buffer used by the text() functions
   * because the String object is slow
   */
  protected char[] textBuffer = new char[8 * 1024];
  protected char[] textWidthBuffer = new char[8 * 1024];

  protected int textBreakCount;
  protected int[] textBreakStart;
  protected int[] textBreakStop;

  // ........................................................

  public boolean edge = true;

  // ........................................................

  /// normal calculated per triangle
  static protected final int NORMAL_MODE_AUTO = 0;
  /// one normal manually specified per shape
  static protected final int NORMAL_MODE_SHAPE = 1;
  /// normals specified for each shape vertex
  static protected final int NORMAL_MODE_VERTEX = 2;

  /// Current mode for normals, one of AUTO, SHAPE, or VERTEX
  protected int normalMode;

  /// Keep track of how many calls to normal, to determine the mode.
  //protected int normalCount;

  /** Current normal vector. */
  public float normalX, normalY, normalZ;

  // ........................................................

  /**
   * Sets whether texture coordinates passed to
   * vertex() calls will be based on coordinates that are
   * based on the IMAGE or NORMALIZED.
   */
  public int textureMode;

  /**
   * Current horizontal coordinate for texture, will always
   * be between 0 and 1, even if using textureMode(IMAGE).
   */
  public float textureU;

  /** Current vertical coordinate for texture, see above. */
  public float textureV;

  /** Current image being used as a texture */
  public PImage textureImage;

  // ........................................................

  // [toxi031031] new & faster sphere code w/ support flexibile resolutions
  // will be set by sphereDetail() or 1st call to sphere()
  float sphereX[], sphereY[], sphereZ[];

  /// Number of U steps (aka "theta") around longitudinally spanning 2*pi
  public int sphereDetailU = 0;
  /// Number of V steps (aka "phi") along latitudinally top-to-bottom spanning pi
  public int sphereDetailV = 0;


  //////////////////////////////////////////////////////////////

  // INTERNAL


  /**
   * Constructor for the PGraphics object. Use this to ensure that
   * the defaults get set properly. In a subclass, use this(w, h)
   * as the first line of a subclass' constructor to properly set
   * the internal fields and defaults.
   * 
   */
  public PGraphics() {
  }


  public void setParent(PApplet parent) {  // ignore
    this.parent = parent;
  }


  /**
   * Set (or unset) this as the main drawing surface. Meaning that it can
   * safely be set to opaque (and given a default gray background), or anything
   * else that goes along with that.
   */
  public void setPrimary(boolean primary) {  // ignore
    this.primarySurface = primary;

    // base images must be opaque (for performance and general
    // headache reasons.. argh, a semi-transparent opengl surface?)
    // use createGraphics() if you want a transparent surface.
    if (primarySurface) {
      format = RGB;
    }
  }


  public void setPath(String path) {  // ignore
    this.path = path;
  }


  /**
   * The final step in setting up a renderer, set its size of this renderer.
   * This was formerly handled by the constructor, but instead it's been broken
   * out so that setParent/setPrimary/setPath can be handled differently.
   *
   * Important that this is ignored by preproc.pl because otherwise it will
   * override setSize() in PApplet/Applet/Component, which will 1) not call
   * super.setSize(), and 2) will cause the renderer to be resized from the
   * event thread (EDT), causing a nasty crash as it collides with the
   * animation thread.
   */
  public void setSize(int w, int h) {  // ignore
    width = w;
    height = h;
    width1 = width - 1;
    height1 = height - 1;

    allocate();
    reapplySettings();
  }


  /**
   * Allocate memory for this renderer. Generally will need to be implemented
   * for all renderers.
   */
  protected void allocate() { }


  /**
   * Handle any takedown for this graphics context.
   * <p>
   * This is called when a sketch is shut down and this renderer was
   * specified using the size() command, or inside endRecord() and
   * endRaw(), in order to shut things off.
   */
  public void dispose() {  // ignore
  }



  //////////////////////////////////////////////////////////////

  // FRAME


  /**
   * Some renderers have requirements re: when they are ready to draw.
   */
  public boolean canDraw() {  // ignore
    return true;
  }


  /**
   * Sets the default properties for a PGraphics object. It should be called before anything is drawn into the object.
   * =advanced
   * <p/>
   * When creating your own PGraphics, you should call this before
   * drawing anything.
   * 
   * @webref
   * @brief Sets up the rendering context
   */
  public void beginDraw() {  // ignore
  }


  /**
   * Finalizes the rendering of a PGraphics object so that it can be shown on screen.
   * =advanced
   * <p/>
   * When creating your own PGraphics, you should call this when
   * you're finished drawing.
   * 
   * @webref
   * @brief Finalizes the renderering context
   */
  public void endDraw() {  // ignore
  }


  public void flush() {
    // no-op, mostly for P3D to write sorted stuff
  }


  protected void checkSettings() {
    if (!settingsInited) defaultSettings();
  }


  /**
   * Set engine's default values. This has to be called by PApplet,
   * somewhere inside setup() or draw() because it talks to the
   * graphics buffer, meaning that for subclasses like OpenGL, there
   * needs to be a valid graphics context to mess with otherwise
   * you'll get some good crashing action.
   *
   * This is currently called by checkSettings(), during beginDraw().
   */
  protected void defaultSettings() {  // ignore
//    System.out.println("PGraphics.defaultSettings() " + width + " " + height);

    noSmooth();  // 0149

    colorMode(RGB, 255);
    fill(255);
    stroke(0);
    // other stroke attributes are set in the initializers
    // inside the class (see above, strokeWeight = 1 et al)

    // init shape stuff
    shape = 0;

    // init matrices (must do before lights)
    //matrixStackDepth = 0;

    rectMode(CORNER);
    ellipseMode(DIAMETER);

    // no current font
    textFont = null;
    textSize = 12;
    textLeading = 14;
    textAlign = LEFT;
    textMode = MODEL;

    // if this fella is associated with an applet, then clear its background.
    // if it's been created by someone else through createGraphics,
    // they have to call background() themselves, otherwise everything gets
    // a gray background (when just a transparent surface or an empty pdf
    // is what's desired).
    // this background() call is for the Java 2D and OpenGL renderers.
    if (primarySurface) {
      //System.out.println("main drawing surface bg " + getClass().getName());
      background(backgroundColor);
    }

    settingsInited = true;
    // defaultSettings() overlaps reapplySettings(), don't do both
    //reapplySettings = false;
  }


  /**
   * Re-apply current settings. Some methods, such as textFont(), require that
   * their methods be called (rather than simply setting the textFont variable)
   * because they affect the graphics context, or they require parameters from
   * the context (e.g. getting native fonts for text).
   *
   * This will only be called from an allocate(), which is only called from
   * size(), which is safely called from inside beginDraw(). And it cannot be
   * called before defaultSettings(), so we should be safe.
   */
  protected void reapplySettings() {
//    System.out.println("attempting reapplySettings()");
    if (!settingsInited) return;  // if this is the initial setup, no need to reapply

//    System.out.println("  doing reapplySettings");
//    new Exception().printStackTrace(System.out);

    colorMode(colorMode, colorModeX, colorModeY, colorModeZ);
    if (fill) {
//      PApplet.println("  fill " + PApplet.hex(fillColor));
      fill(fillColor);
    } else {
      noFill();
    }
    if (stroke) {
      stroke(strokeColor);

      // The if() statements should be handled inside the functions,
      // otherwise an actual reset/revert won't work properly.
      //if (strokeWeight != DEFAULT_STROKE_WEIGHT) {
      strokeWeight(strokeWeight);
      //}
//      if (strokeCap != DEFAULT_STROKE_CAP) {
      strokeCap(strokeCap);
//      }
//      if (strokeJoin != DEFAULT_STROKE_JOIN) {
      strokeJoin(strokeJoin);
//      }
    } else {
      noStroke();
    }
    if (tint) {
      tint(tintColor);
    } else {
      noTint();
    }
    if (smooth) {
      smooth();
    } else {
      // Don't bother setting this, cuz it'll anger P3D.
      noSmooth();
    }
    if (textFont != null) {
//      System.out.println("  textFont in reapply is " + textFont);
      // textFont() resets the leading, so save it in case it's changed
      float saveLeading = textLeading;
      textFont(textFont, textSize);
      textLeading(saveLeading);
    }
    textMode(textMode);
    textAlign(textAlign, textAlignY);
    background(backgroundColor);

    //reapplySettings = false;
  }


  //////////////////////////////////////////////////////////////

  // HINTS

  /**
   * Enable a hint option.
   * <P>
   * For the most part, hints are temporary api quirks,
   * for which a proper api hasn't been properly worked out.
   * for instance SMOOTH_IMAGES existed because smooth()
   * wasn't yet implemented, but it will soon go away.
   * <P>
   * They also exist for obscure features in the graphics
   * engine, like enabling/disabling single pixel lines
   * that ignore the zbuffer, the way they do in alphabot.
   * <P>
   * Current hint options:
   * <UL>
   * <LI><TT>DISABLE_DEPTH_TEST</TT> -
   * turns off the z-buffer in the P3D or OPENGL renderers.
   * </UL>
   */
  public void hint(int which) {
    if (which > 0) {
      hints[which] = true;
    } else {
      hints[-which] = false;
    }
  }



  //////////////////////////////////////////////////////////////

  // VERTEX SHAPES

  /**
   * Start a new shape of type POLYGON
   */
  public void beginShape() {
    beginShape(POLYGON);
  }


  /**
   * Start a new shape.
   * <P>
   * <B>Differences between beginShape() and line() and point() methods.</B>
   * <P>
   * beginShape() is intended to be more flexible at the expense of being
   * a little more complicated to use. it handles more complicated shapes
   * that can consist of many connected lines (so you get joins) or lines
   * mixed with curves.
   * <P>
   * The line() and point() command are for the far more common cases
   * (particularly for our audience) that simply need to draw a line
   * or a point on the screen.
   * <P>
   * From the code side of things, line() may or may not call beginShape()
   * to do the drawing. In the beta code, they do, but in the alpha code,
   * they did not. they might be implemented one way or the other depending
   * on tradeoffs of runtime efficiency vs. implementation efficiency &mdash
   * meaning the speed that things run at vs. the speed it takes me to write
   * the code and maintain it. for beta, the latter is most important so
   * that's how things are implemented.
   */
  public void beginShape(int kind) {
    shape = kind;
  }


  /**
   * Sets whether the upcoming vertex is part of an edge.
   * Equivalent to glEdgeFlag(), for people familiar with OpenGL.
   */
  public void edge(boolean edge) {
   this.edge = edge;
  }


  /**
   * Sets the current normal vector. Only applies with 3D rendering
   * and inside a beginShape/endShape block.
   * <P/>
   * This is for drawing three dimensional shapes and surfaces,
   * allowing you to specify a vector perpendicular to the surface
   * of the shape, which determines how lighting affects it.
   * <P/>
   * For the most part, PGraphics3D will attempt to automatically
   * assign normals to shapes, but since that's imperfect,
   * this is a better option when you want more control.
   * <P/>
   * For people familiar with OpenGL, this function is basically
   * identical to glNormal3f().
   */
  public void normal(float nx, float ny, float nz) {
    normalX = nx;
    normalY = ny;
    normalZ = nz;

    // if drawing a shape and the normal hasn't been set yet,
    // then we need to set the normals for each vertex so far
    if (shape != 0) {
      if (normalMode == NORMAL_MODE_AUTO) {
        // either they set the normals, or they don't [0149]
//        for (int i = vertex_start; i < vertexCount; i++) {
//          vertices[i][NX] = normalX;
//          vertices[i][NY] = normalY;
//          vertices[i][NZ] = normalZ;
//        }
        // One normal per begin/end shape
        normalMode = NORMAL_MODE_SHAPE;

      } else if (normalMode == NORMAL_MODE_SHAPE) {
        // a separate normal for each vertex
        normalMode = NORMAL_MODE_VERTEX;
      }
    }
  }


  /**
   * Set texture mode to either to use coordinates based on the IMAGE
   * (more intuitive for new users) or NORMALIZED (better for advanced chaps)
   */
  public void textureMode(int mode) {
    this.textureMode = mode;
  }


  /**
   * Set texture image for current shape.
   * Needs to be called between @see beginShape and @see endShape
   *
   * @param image reference to a PImage object
   */
  public void texture(PImage image) {
    textureImage = image;
  }


  protected void vertexCheck() {
    if (vertexCount == vertices.length) {
      float temp[][] = new float[vertexCount << 1][VERTEX_FIELD_COUNT];
      System.arraycopy(vertices, 0, temp, 0, vertexCount);
      vertices = temp;
    }
  }


  public void vertex(float x, float y) {
    vertexCheck();
    float[] vertex = vertices[vertexCount];

    curveVertexCount = 0;

    vertex[X] = x;
    vertex[Y] = y;

    vertex[EDGE] = edge ? 1 : 0;

//    if (fill) {
//      vertex[R] = fillR;
//      vertex[G] = fillG;
//      vertex[B] = fillB;
//      vertex[A] = fillA;
//    }
    if (fill || textureImage != null) {
      if (textureImage == null) {
        vertex[R] = fillR;
        vertex[G] = fillG;
        vertex[B] = fillB;
        vertex[A] = fillA;
      } else {
        if (tint) {
          vertex[R] = tintR;
          vertex[G] = tintG;
          vertex[B] = tintB;
          vertex[A] = tintA;
        } else {
          vertex[R] = 1;
          vertex[G] = 1;
          vertex[B] = 1;
          vertex[A] = 1;
        }
      }
    }

    if (stroke) {
      vertex[SR] = strokeR;
      vertex[SG] = strokeG;
      vertex[SB] = strokeB;
      vertex[SA] = strokeA;
      vertex[SW] = strokeWeight;
    }

    if (textureImage != null) {
      vertex[U] = textureU;
      vertex[V] = textureV;
    }

    vertexCount++;
  }


  public void vertex(float x, float y, float z) {
    vertexCheck();
    float[] vertex = vertices[vertexCount];

    // only do this if we're using an irregular (POLYGON) shape that
    // will go through the triangulator. otherwise it'll do thinks like
    // disappear in mathematically odd ways
    // http://dev.processing.org/bugs/show_bug.cgi?id=444
    if (shape == POLYGON) {
      if (vertexCount > 0) {
        float pvertex[] = vertices[vertexCount-1];
        if ((Math.abs(pvertex[X] - x) < EPSILON) &&
            (Math.abs(pvertex[Y] - y) < EPSILON) &&
            (Math.abs(pvertex[Z] - z) < EPSILON)) {
          // this vertex is identical, don't add it,
          // because it will anger the triangulator
          return;
        }
      }
    }

    // User called vertex(), so that invalidates anything queued up for curve
    // vertices. If this is internally called by curveVertexSegment,
    // then curveVertexCount will be saved and restored.
    curveVertexCount = 0;

    vertex[X] = x;
    vertex[Y] = y;
    vertex[Z] = z;

    vertex[EDGE] = edge ? 1 : 0;

    if (fill || textureImage != null) {
      if (textureImage == null) {
        vertex[R] = fillR;
        vertex[G] = fillG;
        vertex[B] = fillB;
        vertex[A] = fillA;
      } else {
        if (tint) {
          vertex[R] = tintR;
          vertex[G] = tintG;
          vertex[B] = tintB;
          vertex[A] = tintA;
        } else {
          vertex[R] = 1;
          vertex[G] = 1;
          vertex[B] = 1;
          vertex[A] = 1;
        }
      }

      vertex[AR] = ambientR;
      vertex[AG] = ambientG;
      vertex[AB] = ambientB;

      vertex[SPR] = specularR;
      vertex[SPG] = specularG;
      vertex[SPB] = specularB;
      //vertex[SPA] = specularA;

      vertex[SHINE] = shininess;

      vertex[ER] = emissiveR;
      vertex[EG] = emissiveG;
      vertex[EB] = emissiveB;
    }

    if (stroke) {
      vertex[SR] = strokeR;
      vertex[SG] = strokeG;
      vertex[SB] = strokeB;
      vertex[SA] = strokeA;
      vertex[SW] = strokeWeight;
    }

    if (textureImage != null) {
      vertex[U] = textureU;
      vertex[V] = textureV;
    }

    vertex[NX] = normalX;
    vertex[NY] = normalY;
    vertex[NZ] = normalZ;

    vertex[BEEN_LIT] = 0;

    vertexCount++;
  }


  /**
   * Used by renderer subclasses or PShape to efficiently pass in already
   * formatted vertex information.
   * @param v vertex parameters, as a float array of length VERTEX_FIELD_COUNT
   */
  public void vertex(float[] v) {
    vertexCheck();
    curveVertexCount = 0;
    float[] vertex = vertices[vertexCount];
    System.arraycopy(v, 0, vertex, 0, VERTEX_FIELD_COUNT);
    vertexCount++;
  }


  public void vertex(float x, float y, float u, float v) {
    vertexTexture(u, v);
    vertex(x, y);
  }


  public void vertex(float x, float y, float z, float u, float v) {
    vertexTexture(u, v);
    vertex(x, y, z);
  }


  /**
   * Internal method to copy all style information for the given vertex.
   * Can be overridden by subclasses to handle only properties pertinent to
   * that renderer. (e.g. no need to copy the emissive color in P2D)
   */
//  protected void vertexStyle() {
//  }


  /**
   * Set (U, V) coords for the next vertex in the current shape.
   * This is ugly as its own function, and will (almost?) always be
   * coincident with a call to vertex. As of beta, this was moved to
   * the protected method you see here, and called from an optional
   * param of and overloaded vertex().
   * <p/>
   * The parameters depend on the current textureMode. When using
   * textureMode(IMAGE), the coordinates will be relative to the size
   * of the image texture, when used with textureMode(NORMAL),
   * they'll be in the range 0..1.
   * <p/>
   * Used by both PGraphics2D (for images) and PGraphics3D.
   */
  protected void vertexTexture(float u, float v) {
    if (textureImage == null) {
      throw new RuntimeException("You must first call texture() before " +
                                 "using u and v coordinates with vertex()");
    }
    if (textureMode == IMAGE) {
      u /= (float) textureImage.width;
      v /= (float) textureImage.height;
    }

    textureU = u;
    textureV = v;

    if (textureU < 0) textureU = 0;
    else if (textureU > 1) textureU = 1;

    if (textureV < 0) textureV = 0;
    else if (textureV > 1) textureV = 1;
  }


  /** This feature is in testing, do not use or rely upon its implementation */
  public void breakShape() {
    showWarning("This renderer cannot currently handle concave shapes, " +
                "or shapes with holes.");
  }


  public void endShape() {
    endShape(OPEN);
  }


  public void endShape(int mode) {
  }



  //////////////////////////////////////////////////////////////

  // CURVE/BEZIER VERTEX HANDLING


  protected void bezierVertexCheck() {
    if (shape == 0 || shape != POLYGON) {
      throw new RuntimeException("beginShape() or beginShape(POLYGON) " +
                                 "must be used before bezierVertex()");
    }
    if (vertexCount == 0) {
      throw new RuntimeException("vertex() must be used at least once" +
                                 "before bezierVertex()");
    }
  }


  public void bezierVertex(float x2, float y2,
                           float x3, float y3,
                           float x4, float y4) {
    bezierInitCheck();
    bezierVertexCheck();
    PMatrix3D draw = bezierDrawMatrix;

    float[] prev = vertices[vertexCount-1];
    float x1 = prev[X];
    float y1 = prev[Y];

    float xplot1 = draw.m10*x1 + draw.m11*x2 + draw.m12*x3 + draw.m13*x4;
    float xplot2 = draw.m20*x1 + draw.m21*x2 + draw.m22*x3 + draw.m23*x4;
    float xplot3 = draw.m30*x1 + draw.m31*x2 + draw.m32*x3 + draw.m33*x4;

    float yplot1 = draw.m10*y1 + draw.m11*y2 + draw.m12*y3 + draw.m13*y4;
    float yplot2 = draw.m20*y1 + draw.m21*y2 + draw.m22*y3 + draw.m23*y4;
    float yplot3 = draw.m30*y1 + draw.m31*y2 + draw.m32*y3 + draw.m33*y4;

    for (int j = 0; j < bezierDetail; j++) {
      x1 += xplot1; xplot1 += xplot2; xplot2 += xplot3;
      y1 += yplot1; yplot1 += yplot2; yplot2 += yplot3;
      vertex(x1, y1);
    }
  }


  public void bezierVertex(float x2, float y2, float z2,
                           float x3, float y3, float z3,
                           float x4, float y4, float z4) {
    bezierInitCheck();
    bezierVertexCheck();
    PMatrix3D draw = bezierDrawMatrix;

    float[] prev = vertices[vertexCount-1];
    float x1 = prev[X];
    float y1 = prev[Y];
    float z1 = prev[Z];

    float xplot1 = draw.m10*x1 + draw.m11*x2 + draw.m12*x3 + draw.m13*x4;
    float xplot2 = draw.m20*x1 + draw.m21*x2 + draw.m22*x3 + draw.m23*x4;
    float xplot3 = draw.m30*x1 + draw.m31*x2 + draw.m32*x3 + draw.m33*x4;

    float yplot1 = draw.m10*y1 + draw.m11*y2 + draw.m12*y3 + draw.m13*y4;
    float yplot2 = draw.m20*y1 + draw.m21*y2 + draw.m22*y3 + draw.m23*y4;
    float yplot3 = draw.m30*y1 + draw.m31*y2 + draw.m32*y3 + draw.m33*y4;

    float zplot1 = draw.m10*z1 + draw.m11*z2 + draw.m12*z3 + draw.m13*z4;
    float zplot2 = draw.m20*z1 + draw.m21*z2 + draw.m22*z3 + draw.m23*z4;
    float zplot3 = draw.m30*z1 + draw.m31*z2 + draw.m32*z3 + draw.m33*z4;

    for (int j = 0; j < bezierDetail; j++) {
      x1 += xplot1; xplot1 += xplot2; xplot2 += xplot3;
      y1 += yplot1; yplot1 += yplot2; yplot2 += yplot3;
      z1 += zplot1; zplot1 += zplot2; zplot2 += zplot3;
      vertex(x1, y1, z1);
    }
  }


  /**
   * Perform initialization specific to curveVertex(), and handle standard
   * error modes. Can be overridden by subclasses that need the flexibility.
   */
  protected void curveVertexCheck() {
    if (shape != POLYGON) {
      throw new RuntimeException("You must use beginShape() or " +
                                 "beginShape(POLYGON) before curveVertex()");
    }
    // to improve code init time, allocate on first use.
    if (curveVertices == null) {
      curveVertices = new float[128][3];
    }

    if (curveVertexCount == curveVertices.length) {
      // Can't use PApplet.expand() cuz it doesn't do the copy properly
      float[][] temp = new float[curveVertexCount << 1][3];
      System.arraycopy(curveVertices, 0, temp, 0, curveVertexCount);
      curveVertices = temp;
    }
    curveInitCheck();
  }


  public void curveVertex(float x, float y) {
    curveVertexCheck();
    float[] vertex = curveVertices[curveVertexCount];
    vertex[X] = x;
    vertex[Y] = y;
    curveVertexCount++;

    // draw a segment if there are enough points
    if (curveVertexCount > 3) {
      curveVertexSegment(curveVertices[curveVertexCount-4][X],
                         curveVertices[curveVertexCount-4][Y],
                         curveVertices[curveVertexCount-3][X],
                         curveVertices[curveVertexCount-3][Y],
                         curveVertices[curveVertexCount-2][X],
                         curveVertices[curveVertexCount-2][Y],
                         curveVertices[curveVertexCount-1][X],
                         curveVertices[curveVertexCount-1][Y]);
    }
  }


  public void curveVertex(float x, float y, float z) {
    curveVertexCheck();
    float[] vertex = curveVertices[curveVertexCount];
    vertex[X] = x;
    vertex[Y] = y;
    vertex[Z] = z;
    curveVertexCount++;

    // draw a segment if there are enough points
    if (curveVertexCount > 3) {
      curveVertexSegment(curveVertices[curveVertexCount-4][X],
                         curveVertices[curveVertexCount-4][Y],
                         curveVertices[curveVertexCount-4][Z],
                         curveVertices[curveVertexCount-3][X],
                         curveVertices[curveVertexCount-3][Y],
                         curveVertices[curveVertexCount-3][Z],
                         curveVertices[curveVertexCount-2][X],
                         curveVertices[curveVertexCount-2][Y],
                         curveVertices[curveVertexCount-2][Z],
                         curveVertices[curveVertexCount-1][X],
                         curveVertices[curveVertexCount-1][Y],
                         curveVertices[curveVertexCount-1][Z]);
    }
  }


  /**
   * Handle emitting a specific segment of Catmull-Rom curve. This can be
   * overridden by subclasses that need more efficient rendering options.
   */
  protected void curveVertexSegment(float x1, float y1,
                                    float x2, float y2,
                                    float x3, float y3,
                                    float x4, float y4) {
    float x0 = x2;
    float y0 = y2;

    PMatrix3D draw = curveDrawMatrix;

    float xplot1 = draw.m10*x1 + draw.m11*x2 + draw.m12*x3 + draw.m13*x4;
    float xplot2 = draw.m20*x1 + draw.m21*x2 + draw.m22*x3 + draw.m23*x4;
    float xplot3 = draw.m30*x1 + draw.m31*x2 + draw.m32*x3 + draw.m33*x4;

    float yplot1 = draw.m10*y1 + draw.m11*y2 + draw.m12*y3 + draw.m13*y4;
    float yplot2 = draw.m20*y1 + draw.m21*y2 + draw.m22*y3 + draw.m23*y4;
    float yplot3 = draw.m30*y1 + draw.m31*y2 + draw.m32*y3 + draw.m33*y4;

    // vertex() will reset splineVertexCount, so save it
    int savedCount = curveVertexCount;

    vertex(x0, y0);
    for (int j = 0; j < curveDetail; j++) {
      x0 += xplot1; xplot1 += xplot2; xplot2 += xplot3;
      y0 += yplot1; yplot1 += yplot2; yplot2 += yplot3;
      vertex(x0, y0);
    }
    curveVertexCount = savedCount;
  }


  /**
   * Handle emitting a specific segment of Catmull-Rom curve. This can be
   * overridden by subclasses that need more efficient rendering options.
   */
  protected void curveVertexSegment(float x1, float y1, float z1,
                                    float x2, float y2, float z2,
                                    float x3, float y3, float z3,
                                    float x4, float y4, float z4) {
    float x0 = x2;
    float y0 = y2;
    float z0 = z2;

    PMatrix3D draw = curveDrawMatrix;

    float xplot1 = draw.m10*x1 + draw.m11*x2 + draw.m12*x3 + draw.m13*x4;
    float xplot2 = draw.m20*x1 + draw.m21*x2 + draw.m22*x3 + draw.m23*x4;
    float xplot3 = draw.m30*x1 + draw.m31*x2 + draw.m32*x3 + draw.m33*x4;

    float yplot1 = draw.m10*y1 + draw.m11*y2 + draw.m12*y3 + draw.m13*y4;
    float yplot2 = draw.m20*y1 + draw.m21*y2 + draw.m22*y3 + draw.m23*y4;
    float yplot3 = draw.m30*y1 + draw.m31*y2 + draw.m32*y3 + draw.m33*y4;

    // vertex() will reset splineVertexCount, so save it
    int savedCount = curveVertexCount;

    float zplot1 = draw.m10*z1 + draw.m11*z2 + draw.m12*z3 + draw.m13*z4;
    float zplot2 = draw.m20*z1 + draw.m21*z2 + draw.m22*z3 + draw.m23*z4;
    float zplot3 = draw.m30*z1 + draw.m31*z2 + draw.m32*z3 + draw.m33*z4;

    vertex(x0, y0, z0);
    for (int j = 0; j < curveDetail; j++) {
      x0 += xplot1; xplot1 += xplot2; xplot2 += xplot3;
      y0 += yplot1; yplot1 += yplot2; yplot2 += yplot3;
      z0 += zplot1; zplot1 += zplot2; zplot2 += zplot3;
      vertex(x0, y0, z0);
    }
    curveVertexCount = savedCount;
  }



  //////////////////////////////////////////////////////////////

  // SIMPLE SHAPES WITH ANALOGUES IN beginShape()


  public void point(float x, float y) {
    beginShape(POINTS);
    vertex(x, y);
    endShape();
  }


  public void point(float x, float y, float z) {
    beginShape(POINTS);
    vertex(x, y, z);
    endShape();
  }


  public void line(float x1, float y1, float x2, float y2) {
    beginShape(LINES);
    vertex(x1, y1);
    vertex(x2, y2);
    endShape();
  }


  public void line(float x1, float y1, float z1,
                   float x2, float y2, float z2) {
    beginShape(LINES);
    vertex(x1, y1, z1);
    vertex(x2, y2, z2);
    endShape();
  }


  public void triangle(float x1, float y1, float x2, float y2,
                       float x3, float y3) {
    beginShape(TRIANGLES);
    vertex(x1, y1);
    vertex(x2, y2);
    vertex(x3, y3);
    endShape();
  }


  public void quad(float x1, float y1, float x2, float y2,
                   float x3, float y3, float x4, float y4) {
    beginShape(QUADS);
    vertex(x1, y1);
    vertex(x2, y2);
    vertex(x3, y3);
    vertex(x4, y4);
    endShape();
  }



  //////////////////////////////////////////////////////////////

  // RECT


  public void rectMode(int mode) {
    rectMode = mode;
  }


  public void rect(float a, float b, float c, float d) {
    float hradius, vradius;
    switch (rectMode) {
    case CORNERS:
      break;
    case CORNER:
      c += a; d += b;
      break;
    case RADIUS:
      hradius = c;
      vradius = d;
      c = a + hradius;
      d = b + vradius;
      a -= hradius;
      b -= vradius;
      break;
    case CENTER:
      hradius = c / 2.0f;
      vradius = d / 2.0f;
      c = a + hradius;
      d = b + vradius;
      a -= hradius;
      b -= vradius;
    }

    if (a > c) {
      float temp = a; a = c; c = temp;
    }

    if (b > d) {
      float temp = b; b = d; d = temp;
    }

    rectImpl(a, b, c, d);
  }


  protected void rectImpl(float x1, float y1, float x2, float y2) {
    quad(x1, y1,  x2, y1,  x2, y2,  x1, y2);
  }



  //////////////////////////////////////////////////////////////

  // ELLIPSE AND ARC


  public void ellipseMode(int mode) {
    ellipseMode = mode;
  }


  public void ellipse(float a, float b, float c, float d) {
    float x = a;
    float y = b;
    float w = c;
    float h = d;

    if (ellipseMode == CORNERS) {
      w = c - a;
      h = d - b;

    } else if (ellipseMode == RADIUS) {
      x = a - c;
      y = b - d;
      w = c * 2;
      h = d * 2;

    } else if (ellipseMode == DIAMETER) {
      x = a - c/2f;
      y = b - d/2f;
    }

    if (w < 0) {  // undo negative width
      x += w;
      w = -w;
    }

    if (h < 0) {  // undo negative height
      y += h;
      h = -h;
    }

    ellipseImpl(x, y, w, h);
  }


  protected void ellipseImpl(float x, float y, float w, float h) {
  }


  /**
   * Identical parameters and placement to ellipse,
   * but draws only an arc of that ellipse.
   * <p/>
   * start and stop are always radians because angleMode() was goofy.
   * ellipseMode() sets the placement.
   * <p/>
   * also tries to be smart about start < stop.
   */
  public void arc(float a, float b, float c, float d,
                  float start, float stop) {
    float x = a;
    float y = b;
    float w = c;
    float h = d;

    if (ellipseMode == CORNERS) {
      w = c - a;
      h = d - b;

    } else if (ellipseMode == RADIUS) {
      x = a - c;
      y = b - d;
      w = c * 2;
      h = d * 2;

    } else if (ellipseMode == CENTER) {
      x = a - c/2f;
      y = b - d/2f;
    }

    // make sure this loop will exit before starting while
    if (Float.isInfinite(start) || Float.isInfinite(stop)) return;
//    while (stop < start) stop += TWO_PI;
    if (stop < start) return;  // why bother

    // make sure that we're starting at a useful point
    while (start < 0) {
      start += TWO_PI;
      stop += TWO_PI;
    }

    if (stop - start > TWO_PI) {
      start = 0;
      stop = TWO_PI;
    }

    arcImpl(x, y, w, h, start, stop);
  }


  /**
   * Start and stop are in radians, converted by the parent function.
   * Note that the radians can be greater (or less) than TWO_PI.
   * This is so that an arc can be drawn that crosses zero mark,
   * and the user will still collect $200.
   */
  protected void arcImpl(float x, float y, float w, float h,
                         float start, float stop) {
  }



  //////////////////////////////////////////////////////////////

  // BOX


  public void box(float size) {
    box(size, size, size);
  }


  // TODO not the least bit efficient, it even redraws lines
  // along the vertices. ugly ugly ugly!
  public void box(float w, float h, float d) {
    float x1 = -w/2f; float x2 = w/2f;
    float y1 = -h/2f; float y2 = h/2f;
    float z1 = -d/2f; float z2 = d/2f;

    beginShape(QUADS);

    // front
    normal(0, 0, 1);
    vertex(x1, y1, z1);
    vertex(x2, y1, z1);
    vertex(x2, y2, z1);
    vertex(x1, y2, z1);

    // right
    normal(1, 0, 0);
    vertex(x2, y1, z1);
    vertex(x2, y1, z2);
    vertex(x2, y2, z2);
    vertex(x2, y2, z1);

    // back
    normal(0, 0, -1);
    vertex(x2, y1, z2);
    vertex(x1, y1, z2);
    vertex(x1, y2, z2);
    vertex(x2, y2, z2);

    // left
    normal(-1, 0, 0);
    vertex(x1, y1, z2);
    vertex(x1, y1, z1);
    vertex(x1, y2, z1);
    vertex(x1, y2, z2);

    // top
    normal(0, 1, 0);
    vertex(x1, y1, z2);
    vertex(x2, y1, z2);
    vertex(x2, y1, z1);
    vertex(x1, y1, z1);

    // bottom
    normal(0, -1, 0);
    vertex(x1, y2, z1);
    vertex(x2, y2, z1);
    vertex(x2, y2, z2);
    vertex(x1, y2, z2);

    endShape();
  }



  //////////////////////////////////////////////////////////////

  // SPHERE


  public void sphereDetail(int res) {
    sphereDetail(res, res);
  }


  /**
   * Set the detail level for approximating a sphere. The ures and vres params
   * control the horizontal and vertical resolution.
   *
   * Code for sphereDetail() submitted by toxi [031031].
   * Code for enhanced u/v version from davbol [080801].
   */
  public void sphereDetail(int ures, int vres) {
    if (ures < 3) ures = 3; // force a minimum res
    if (vres < 2) vres = 2; // force a minimum res
    if ((ures == sphereDetailU) && (vres == sphereDetailV)) return;

    float delta = (float)SINCOS_LENGTH/ures;
    float[] cx = new float[ures];
    float[] cz = new float[ures];
    // calc unit circle in XZ plane
    for (int i = 0; i < ures; i++) {
      cx[i] = cosLUT[(int) (i*delta) % SINCOS_LENGTH];
      cz[i] = sinLUT[(int) (i*delta) % SINCOS_LENGTH];
    }
    // computing vertexlist
    // vertexlist starts at south pole
    int vertCount = ures * (vres-1) + 2;
    int currVert = 0;

    // re-init arrays to store vertices
    sphereX = new float[vertCount];
    sphereY = new float[vertCount];
    sphereZ = new float[vertCount];

    float angle_step = (SINCOS_LENGTH*0.5f)/vres;
    float angle = angle_step;

    // step along Y axis
    for (int i = 1; i < vres; i++) {
      float curradius = sinLUT[(int) angle % SINCOS_LENGTH];
      float currY = -cosLUT[(int) angle % SINCOS_LENGTH];
      for (int j = 0; j < ures; j++) {
        sphereX[currVert] = cx[j] * curradius;
        sphereY[currVert] = currY;
        sphereZ[currVert++] = cz[j] * curradius;
      }
      angle += angle_step;
    }
    sphereDetailU = ures;
    sphereDetailV = vres;
  }


  /**
   * Draw a sphere with radius r centered at coordinate 0, 0, 0.
   * <P>
   * Implementation notes:
   * <P>
   * cache all the points of the sphere in a static array
   * top and bottom are just a bunch of triangles that land
   * in the center point
   * <P>
   * sphere is a series of concentric circles who radii vary
   * along the shape, based on, er.. cos or something
   * <PRE>
   * [toxi 031031] new sphere code. removed all multiplies with
   * radius, as scale() will take care of that anyway
   *
   * [toxi 031223] updated sphere code (removed modulos)
   * and introduced sphereAt(x,y,z,r)
   * to avoid additional translate()'s on the user/sketch side
   *
   * [davbol 080801] now using separate sphereDetailU/V
   * </PRE>
   */
  public void sphere(float r) {
    if ((sphereDetailU < 3) || (sphereDetailV < 2)) {
      sphereDetail(30);
    }

    pushMatrix();
    scale(r);
    edge(false);

    // 1st ring from south pole
    beginShape(TRIANGLE_STRIP);
    for (int i = 0; i < sphereDetailU; i++) {
      normal(0, -1, 0);
      vertex(0, -1, 0);
      normal(sphereX[i], sphereY[i], sphereZ[i]);
      vertex(sphereX[i], sphereY[i], sphereZ[i]);
    }
    //normal(0, -1, 0);
    vertex(0, -1, 0);
    normal(sphereX[0], sphereY[0], sphereZ[0]);
    vertex(sphereX[0], sphereY[0], sphereZ[0]);
    endShape();

    int v1,v11,v2;

    // middle rings
    int voff = 0;
    for (int i = 2; i < sphereDetailV; i++) {
      v1 = v11 = voff;
      voff += sphereDetailU;
      v2 = voff;
      beginShape(TRIANGLE_STRIP);
      for (int j = 0; j < sphereDetailU; j++) {
        normal(sphereX[v1], sphereY[v1], sphereZ[v1]);
        vertex(sphereX[v1], sphereY[v1], sphereZ[v1++]);
        normal(sphereX[v2], sphereY[v2], sphereZ[v2]);
        vertex(sphereX[v2], sphereY[v2], sphereZ[v2++]);
      }
      // close each ring
      v1 = v11;
      v2 = voff;
      normal(sphereX[v1], sphereY[v1], sphereZ[v1]);
      vertex(sphereX[v1], sphereY[v1], sphereZ[v1]);
      normal(sphereX[v2], sphereY[v2], sphereZ[v2]);
      vertex(sphereX[v2], sphereY[v2], sphereZ[v2]);
      endShape();
    }

    // add the northern cap
    beginShape(TRIANGLE_STRIP);
    for (int i = 0; i < sphereDetailU; i++) {
      v2 = voff + i;
      normal(sphereX[v2], sphereY[v2], sphereZ[v2]);
      vertex(sphereX[v2], sphereY[v2], sphereZ[v2]);
      normal(0, 1, 0);
      vertex(0, 1, 0);
    }
    normal(sphereX[voff], sphereY[voff], sphereZ[voff]);
    vertex(sphereX[voff], sphereY[voff], sphereZ[voff]);
    normal(0, 1, 0);
    vertex(0, 1, 0);
    endShape();

    edge(true);
    popMatrix();
  }



  //////////////////////////////////////////////////////////////

  // BEZIER


  /**
   * Evalutes quadratic bezier at point t for points a, b, c, d.
   * t varies between 0 and 1, and a and d are the on curve points,
   * b and c are the control points. this can be done once with the
   * x coordinates and a second time with the y coordinates to get
   * the location of a bezier curve at t.
   * <P>
   * For instance, to convert the following example:<PRE>
   * stroke(255, 102, 0);
   * line(85, 20, 10, 10);
   * line(90, 90, 15, 80);
   * stroke(0, 0, 0);
   * bezier(85, 20, 10, 10, 90, 90, 15, 80);
   *
   * // draw it in gray, using 10 steps instead of the default 20
   * // this is a slower way to do it, but useful if you need
   * // to do things with the coordinates at each step
   * stroke(128);
   * beginShape(LINE_STRIP);
   * for (int i = 0; i <= 10; i++) {
   *   float t = i / 10.0f;
   *   float x = bezierPoint(85, 10, 90, 15, t);
   *   float y = bezierPoint(20, 10, 90, 80, t);
   *   vertex(x, y);
   * }
   * endShape();</PRE>
   */
  public float bezierPoint(float a, float b, float c, float d, float t) {
    float t1 = 1.0f - t;
    return a*t1*t1*t1 + 3*b*t*t1*t1 + 3*c*t*t*t1 + d*t*t*t;
  }


  /**
   * Provide the tangent at the given point on the bezier curve.
   * Fix from davbol for 0136.
   */
  public float bezierTangent(float a, float b, float c, float d, float t) {
    return (3*t*t * (-a+3*b-3*c+d) +
            6*t * (a-2*b+c) +
            3 * (-a+b));
  }


  protected void bezierInitCheck() {
    if (!bezierInited) {
      bezierInit();
    }
  }


  protected void bezierInit() {
    // overkill to be broken out, but better parity with the curve stuff below
    bezierDetail(bezierDetail);
    bezierInited = true;
  }


  public void bezierDetail(int detail) {
    bezierDetail = detail;

    if (bezierDrawMatrix == null) {
      bezierDrawMatrix = new PMatrix3D();
    }

    // setup matrix for forward differencing to speed up drawing
    splineForward(detail, bezierDrawMatrix);

    // multiply the basis and forward diff matrices together
    // saves much time since this needn't be done for each curve
    //mult_spline_matrix(bezierForwardMatrix, bezier_basis, bezierDrawMatrix, 4);
    //bezierDrawMatrix.set(bezierForwardMatrix);
    bezierDrawMatrix.apply(bezierBasisMatrix);
  }


  /**
   * Draw a cubic bezier curve. The first and last points are
   * the on-curve points. The middle two are the 'control' points,
   * or 'handles' in an application like Illustrator.
   * <P>
   * Identical to typing:
   * <PRE>beginShape();
   * vertex(x1, y1);
   * bezierVertex(x2, y2, x3, y3, x4, y4);
   * endShape();
   * </PRE>
   * In Postscript-speak, this would be:
   * <PRE>moveto(x1, y1);
   * curveto(x2, y2, x3, y3, x4, y4);</PRE>
   * If you were to try and continue that curve like so:
   * <PRE>curveto(x5, y5, x6, y6, x7, y7);</PRE>
   * This would be done in processing by adding these statements:
   * <PRE>bezierVertex(x5, y5, x6, y6, x7, y7)
   * </PRE>
   * To draw a quadratic (instead of cubic) curve,
   * use the control point twice by doubling it:
   * <PRE>bezier(x1, y1, cx, cy, cx, cy, x2, y2);</PRE>
   */
  public void bezier(float x1, float y1,
                     float x2, float y2,
                     float x3, float y3,
                     float x4, float y4) {
    beginShape();
    vertex(x1, y1);
    bezierVertex(x2, y2, x3, y3, x4, y4);
    endShape();
  }


  public void bezier(float x1, float y1, float z1,
                     float x2, float y2, float z2,
                     float x3, float y3, float z3,
                     float x4, float y4, float z4) {
    beginShape();
    vertex(x1, y1, z1);
    bezierVertex(x2, y2, z2,
                 x3, y3, z3,
                 x4, y4, z4);
    endShape();
  }



  //////////////////////////////////////////////////////////////

  // CATMULL-ROM CURVE


  /**
   * Get a location along a catmull-rom curve segment.
   *
   * @param t Value between zero and one for how far along the segment
   */
  public float curvePoint(float a, float b, float c, float d, float t) {
    curveInitCheck();

    float tt = t * t;
    float ttt = t * tt;
    PMatrix3D cb = curveBasisMatrix;

    // not optimized (and probably need not be)
    return (a * (ttt*cb.m00 + tt*cb.m10 + t*cb.m20 + cb.m30) +
            b * (ttt*cb.m01 + tt*cb.m11 + t*cb.m21 + cb.m31) +
            c * (ttt*cb.m02 + tt*cb.m12 + t*cb.m22 + cb.m32) +
            d * (ttt*cb.m03 + tt*cb.m13 + t*cb.m23 + cb.m33));
  }


  /**
   * Calculate the tangent at a t value (0..1) on a Catmull-Rom curve.
   * Code thanks to Dave Bollinger (Bug #715)
   */
  public float curveTangent(float a, float b, float c, float d, float t) {
    curveInitCheck();

    float tt3 = t * t * 3;
    float t2 = t * 2;
    PMatrix3D cb = curveBasisMatrix;

    // not optimized (and probably need not be)
    return (a * (tt3*cb.m00 + t2*cb.m10 + cb.m20) +
            b * (tt3*cb.m01 + t2*cb.m11 + cb.m21) +
            c * (tt3*cb.m02 + t2*cb.m12 + cb.m22) +
            d * (tt3*cb.m03 + t2*cb.m13 + cb.m23) );
  }


  public void curveDetail(int detail) {
    curveDetail = detail;
    curveInit();
  }


  public void curveTightness(float tightness) {
    curveTightness = tightness;
    curveInit();
  }


  protected void curveInitCheck() {
    if (!curveInited) {
      curveInit();
    }
  }


  /**
   * Set the number of segments to use when drawing a Catmull-Rom
   * curve, and setting the s parameter, which defines how tightly
   * the curve fits to each vertex. Catmull-Rom curves are actually
   * a subset of this curve type where the s is set to zero.
   * <P>
   * (This function is not optimized, since it's not expected to
   * be called all that often. there are many juicy and obvious
   * opimizations in here, but it's probably better to keep the
   * code more readable)
   */
  protected void curveInit() {
    // allocate only if/when used to save startup time
    if (curveDrawMatrix == null) {
      curveBasisMatrix = new PMatrix3D();
      curveDrawMatrix = new PMatrix3D();
      curveInited = true;
    }

    float s = curveTightness;
    curveBasisMatrix.set((s-1)/2f, (s+3)/2f,  (-3-s)/2f, (1-s)/2f,
                         (1-s),    (-5-s)/2f, (s+2),     (s-1)/2f,
                         (s-1)/2f, 0,         (1-s)/2f,  0,
                         0,        1,         0,         0);

    //setup_spline_forward(segments, curveForwardMatrix);
    splineForward(curveDetail, curveDrawMatrix);

    if (bezierBasisInverse == null) {
      bezierBasisInverse = bezierBasisMatrix.get();
      bezierBasisInverse.invert();
      curveToBezierMatrix = new PMatrix3D();
    }

    // TODO only needed for PGraphicsJava2D? if so, move it there
    // actually, it's generally useful for other renderers, so keep it
    // or hide the implementation elsewhere.
    curveToBezierMatrix.set(curveBasisMatrix);
    curveToBezierMatrix.preApply(bezierBasisInverse);

    // multiply the basis and forward diff matrices together
    // saves much time since this needn't be done for each curve
    curveDrawMatrix.apply(curveBasisMatrix);
  }


  /**
   * Draws a segment of Catmull-Rom curve.
   * <P>
   * As of 0070, this function no longer doubles the first and
   * last points. The curves are a bit more boring, but it's more
   * mathematically correct, and properly mirrored in curvePoint().
   * <P>
   * Identical to typing out:<PRE>
   * beginShape();
   * curveVertex(x1, y1);
   * curveVertex(x2, y2);
   * curveVertex(x3, y3);
   * curveVertex(x4, y4);
   * endShape();
   * </PRE>
   */
  public void curve(float x1, float y1,
                    float x2, float y2,
                    float x3, float y3,
                    float x4, float y4) {
    beginShape();
    curveVertex(x1, y1);
    curveVertex(x2, y2);
    curveVertex(x3, y3);
    curveVertex(x4, y4);
    endShape();
  }


  public void curve(float x1, float y1, float z1,
                    float x2, float y2, float z2,
                    float x3, float y3, float z3,
                    float x4, float y4, float z4) {
    beginShape();
    curveVertex(x1, y1, z1);
    curveVertex(x2, y2, z2);
    curveVertex(x3, y3, z3);
    curveVertex(x4, y4, z4);
    endShape();
  }



  //////////////////////////////////////////////////////////////

  // SPLINE UTILITY FUNCTIONS (used by both Bezier and Catmull-Rom)


  /**
   * Setup forward-differencing matrix to be used for speedy
   * curve rendering. It's based on using a specific number
   * of curve segments and just doing incremental adds for each
   * vertex of the segment, rather than running the mathematically
   * expensive cubic equation.
   * @param segments number of curve segments to use when drawing
   * @param matrix target object for the new matrix
   */
  protected void splineForward(int segments, PMatrix3D matrix) {
    float f  = 1.0f / segments;
    float ff = f * f;
    float fff = ff * f;

    matrix.set(0,     0,    0, 1,
               fff,   ff,   f, 0,
               6*fff, 2*ff, 0, 0,
               6*fff, 0,    0, 0);
  }



  //////////////////////////////////////////////////////////////

  // SMOOTHING


  /**
   * If true in PImage, use bilinear interpolation for copy()
   * operations. When inherited by PGraphics, also controls shapes.
   */
  public void smooth() {
    smooth = true;
  }


  /**
   * Disable smoothing. See smooth().
   */
  public void noSmooth() {
    smooth = false;
  }



  //////////////////////////////////////////////////////////////

  // IMAGE


  /**
   * The mode can only be set to CORNERS, CORNER, and CENTER.
   * <p/>
   * Support for CENTER was added in release 0146.
   */
  public void imageMode(int mode) {
    if ((mode == CORNER) || (mode == CORNERS) || (mode == CENTER)) {
      imageMode = mode;
    } else {
      String msg =
        "imageMode() only works with CORNER, CORNERS, or CENTER";
      throw new RuntimeException(msg);
    }
  }


  public void image(PImage imag
