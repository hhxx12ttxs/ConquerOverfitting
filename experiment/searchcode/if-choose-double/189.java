/*
 * %W% %E%
 *
 * Copyright (c) 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.java2d;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.geom.Area;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.awt.image.renderable.RenderContext;
import java.awt.image.AffineTransformOp;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.Image;
import java.awt.Composite;
import java.awt.Color;
import java.awt.image.ColorModel;
import java.awt.GraphicsConfiguration;
import java.awt.Paint;
import java.awt.GradientPaint;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.geom.PathIterator;
import java.awt.geom.GeneralPath;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.text.AttributedCharacterIterator;
import java.awt.Font;
import java.awt.image.ImageObserver;
import java.awt.Transparency;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import sun.font.FontDesignMetrics;
import sun.java2d.pipe.PixelDrawPipe;
import sun.java2d.pipe.PixelFillPipe;
import sun.java2d.pipe.ShapeDrawPipe;
import sun.java2d.pipe.ValidatePipe;
import sun.java2d.pipe.ShapeSpanIterator;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.TextPipe;
import sun.java2d.pipe.DrawImagePipe;
import sun.java2d.pipe.LoopPipe;
import sun.java2d.pipe.DuctusRenderer;
import sun.java2d.loops.FontInfo;
import sun.java2d.loops.RenderLoops;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.Blit;
import sun.java2d.loops.MaskFill;
import sun.font.FontManager;
import java.awt.font.FontRenderContext;
import sun.java2d.loops.XORComposite;
import sun.awt.ConstrainableGraphics;
import sun.awt.SunHints;
import java.util.Map;
import java.util.Iterator;
import sun.java2d.pipe.hw.AccelSurface;
import sun.java2d.DestSurfaceProvider;
import sun.misc.PerformanceLogger;

/**
 * This is a the master Graphics2D superclass for all of the Sun
 * Graphics implementations.  This class relies on subclasses to
 * manage the various device information, but provides an overall
 * general framework for performing all of the requests in the
 * Graphics and Graphics2D APIs.
 *
 * @version 1.211 05/07/98
 * @author Jim Graham
 */
public final class SunGraphics2D
    extends Graphics2D
    implements ConstrainableGraphics, Cloneable, DestSurfaceProvider
{
    /*
     * Attribute States
     */
    /* Paint */
    public static final int PAINT_CUSTOM       = 6; /* Any other Paint object */
    public static final int PAINT_TEXTURE      = 5; /* Tiled Image */
    public static final int PAINT_RAD_GRADIENT = 4; /* Color RadialGradient */
    public static final int PAINT_LIN_GRADIENT = 3; /* Color LinearGradient */
    public static final int PAINT_GRADIENT     = 2; /* Color Gradient */
    public static final int PAINT_ALPHACOLOR   = 1; /* Non-opaque Color */
    public static final int PAINT_OPAQUECOLOR  = 0; /* Opaque Color */

    /* Composite*/
    public static final int COMP_CUSTOM = 3;/* Custom Composite */
    public static final int COMP_XOR    = 2;/* XOR Mode Composite */
    public static final int COMP_ALPHA  = 1;/* AlphaComposite */
    public static final int COMP_ISCOPY = 0;/* simple stores into destination,
                                             * i.e. Src, SrcOverNoEa, and other
                                             * alpha modes which replace
                                             * the destination.
                                             */

    /* Stroke */
    public static final int STROKE_CUSTOM = 3; /* custom Stroke */
    public static final int STROKE_WIDE   = 2; /* BasicStroke */
    public static final int STROKE_THINDASHED   = 1; /* BasicStroke */
    public static final int STROKE_THIN   = 0; /* BasicStroke */

    /* Transform */
    public static final int TRANSFORM_GENERIC = 4; /* any 3x2 */
    public static final int TRANSFORM_TRANSLATESCALE = 3; /* scale XY */
    public static final int TRANSFORM_ANY_TRANSLATE = 2; /* non-int translate */
    public static final int TRANSFORM_INT_TRANSLATE = 1; /* int translate */
    public static final int TRANSFORM_ISIDENT = 0; /* Identity */

    /* Clipping */
    public static final int CLIP_SHAPE       = 2; /* arbitrary clip */
    public static final int CLIP_RECTANGULAR = 1; /* rectangular clip */
    public static final int CLIP_DEVICE      = 0; /* no clipping set */

    /* The following fields are used when the current Paint is a Color. */
    public int eargb;  // ARGB value with ExtraAlpha baked in
    public int pixel;  // pixel value for eargb

    public SurfaceData surfaceData;

    public PixelDrawPipe drawpipe;
    public PixelFillPipe fillpipe;
    public DrawImagePipe imagepipe;
    public ShapeDrawPipe shapepipe;
    public TextPipe textpipe;
    public MaskFill alphafill;

    public RenderLoops loops;

    public CompositeType imageComp;     /* Image Transparency checked on fly */

    public int paintState;
    public int compositeState;
    public int strokeState;
    public int transformState;
    public int clipState;

    public Color foregroundColor;
    public Color backgroundColor;

    public AffineTransform transform;
    public int transX;
    public int transY;

    protected static final Stroke defaultStroke = new BasicStroke();
    protected static final Composite defaultComposite = AlphaComposite.SrcOver;
    private static final Font defaultFont =
	new Font(Font.DIALOG, Font.PLAIN, 12);

    public Paint paint;
    public Stroke stroke;
    public Composite composite;
    protected Font font;
    protected FontMetrics fontMetrics;

    public int renderHint;
    public int antialiasHint;
    public int textAntialiasHint;
    private int fractionalMetricsHint;

    /* A gamma adjustment to the colour used in lcd text blitting */
    public int lcdTextContrast;
    private static int lcdTextContrastDefaultValue = 140;

    private int interpolationHint;	// raw value of rendering Hint
    public int strokeHint;

    public int interpolationType;	// algorithm choice based on
					// interpolation and render Hints

    public RenderingHints hints;

    public Region constrainClip;		// lightweight bounds
    public int constrainX;
    public int constrainY;

    public Region clipRegion;
    public Shape usrClip;
    protected Region devClip;		// Actual physical drawable

    // cached state for text rendering
    private boolean validFontInfo;
    private FontInfo fontInfo;
    private FontInfo glyphVectorFontInfo;
    private FontRenderContext glyphVectorFRC;

    private final static int slowTextTransformMask =
                            AffineTransform.TYPE_GENERAL_TRANSFORM
                        |   AffineTransform.TYPE_MASK_ROTATION
                        |   AffineTransform.TYPE_FLIP;

    static {
	if (PerformanceLogger.loggingEnabled()) {
	    PerformanceLogger.setTime("SunGraphics2D static initialization");
	}
    }

    public SunGraphics2D(SurfaceData sd, Color fg, Color bg, Font f) {
	surfaceData = sd;
	foregroundColor = fg;
	backgroundColor = bg;

	transform = new AffineTransform();
	stroke = defaultStroke;
	composite = defaultComposite;
	paint = foregroundColor;

	imageComp = CompositeType.SrcOverNoEa;

	renderHint = SunHints.INTVAL_RENDER_DEFAULT;
	antialiasHint = SunHints.INTVAL_ANTIALIAS_OFF;
	textAntialiasHint = SunHints.INTVAL_TEXT_ANTIALIAS_DEFAULT;
	fractionalMetricsHint = SunHints.INTVAL_FRACTIONALMETRICS_OFF;
	lcdTextContrast = lcdTextContrastDefaultValue;
	interpolationHint = -1;
	strokeHint = SunHints.INTVAL_STROKE_DEFAULT;

	interpolationType = AffineTransformOp.TYPE_NEAREST_NEIGHBOR;

        validateColor();

	font = f;
	if (font == null) {
	    font = defaultFont;
	}

	loops = sd.getRenderLoops(this);
	setDevClip(sd.getBounds());
	invalidatePipe();
    }

    protected Object clone() {
	try {
	    SunGraphics2D g = (SunGraphics2D) super.clone();
	    g.transform = new AffineTransform(this.transform);
	    if (hints != null) {
		g.hints = (RenderingHints) this.hints.clone();
	    }
	    /* FontInfos are re-used, so must be cloned too, if they
	     * are valid, and be nulled out if invalid.
	     * The implied trade-off is that there is more to be gained
	     * from re-using these objects than is lost by having to
	     * clone them when the SG2D is cloned.
	     */
	    if (this.fontInfo != null) {
		if (this.validFontInfo) {
		    g.fontInfo = (FontInfo)this.fontInfo.clone();
		} else {
		    g.fontInfo = null;
		}
	    }
	    if (this.glyphVectorFontInfo != null) {
		g.glyphVectorFontInfo =
		    (FontInfo)this.glyphVectorFontInfo.clone();
		g.glyphVectorFRC = this.glyphVectorFRC;
	    }
	    //g.invalidatePipe();
	    return g;
	} catch (CloneNotSupportedException e) {
	}
	return null;
    }

    /**
     * Create a new SunGraphics2D based on this one.
     */
    public Graphics create() {
	return (Graphics) clone();
    }

    public void setDevClip(int x, int y, int w, int h) {
	Region c = constrainClip;
	if (c == null) {
	    devClip = Region.getInstanceXYWH(x, y, w, h);
	} else {
	    devClip = c.getIntersectionXYWH(x, y, w, h);
	}
	validateCompClip();
    }

    public void setDevClip(Rectangle r) {
	setDevClip(r.x, r.y, r.width, r.height);
    }

    /**
     * Constrain rendering for lightweight objects.
     *
     * REMIND: This method will back off to the "workaround"
     * of using translate and clipRect if the Graphics
     * to be constrained has a complex transform.  The
     * drawback of the workaround is that the resulting
     * clip and device origin cannot be "enforced".
     *
     * @exception IllegalStateException If the Graphics
     * to be constrained has a complex transform.
     */
    public void constrain(int x, int y, int w, int h) {
        if ((x|y) != 0) {
            translate(x, y);
        }
	if (transformState >= TRANSFORM_TRANSLATESCALE) {
	    clipRect(0, 0, w, h);
	    return;
	}
        x = constrainX = transX;
        y = constrainY = transY;
	w = Region.dimAdd(x, w);
	h = Region.dimAdd(y, h);
	Region c = constrainClip;
	if (c == null) {
	    c = Region.getInstanceXYXY(x, y, w, h);
	} else {
	    c = c.getIntersectionXYXY(x, y, w, h);
	    if (c == constrainClip) {
		// Common case to ignore
		return;
	    }
	}
	constrainClip = c;
	if (!devClip.isInsideQuickCheck(c)) {
	    devClip = devClip.getIntersection(c);
	    validateCompClip();
	}
    }

    protected static ValidatePipe invalidpipe = new ValidatePipe();

    /*
     * Invalidate the pipeline
     */
    protected void invalidatePipe() {
	drawpipe = invalidpipe;
	fillpipe = invalidpipe;
	shapepipe = invalidpipe;
	textpipe = invalidpipe;
	imagepipe = invalidpipe;
    }

    public void validatePipe() {
	surfaceData.validatePipe(this);
    }

    /*
     * Intersect two Shapes by the simplest method, attempting to produce
     * a simplified result.
     * The boolean arguments keep1 and keep2 specify whether or not
     * the first or second shapes can be modified during the operation
     * or whether that shape must be "kept" unmodified.
     */
    Shape intersectShapes(Shape s1, Shape s2, boolean keep1, boolean keep2) {
	if (s1 instanceof Rectangle && s2 instanceof Rectangle) {
	    return ((Rectangle) s1).intersection((Rectangle) s2);
	}
	if (s1 instanceof Rectangle2D) {
	    return intersectRectShape((Rectangle2D) s1, s2, keep1, keep2);
	} else if (s2 instanceof Rectangle2D) {
	    return intersectRectShape((Rectangle2D) s2, s1, keep2, keep1);
	}
	return intersectByArea(s1, s2, keep1, keep2);
    }

    /*
     * Intersect a Rectangle with a Shape by the simplest method,
     * attempting to produce a simplified result.
     * The boolean arguments keep1 and keep2 specify whether or not
     * the first or second shapes can be modified during the operation
     * or whether that shape must be "kept" unmodified.
     */
    Shape intersectRectShape(Rectangle2D r, Shape s,
			     boolean keep1, boolean keep2) {
	if (s instanceof Rectangle2D) {
	    Rectangle2D r2 = (Rectangle2D) s;
	    Rectangle2D outrect;
	    if (!keep1) {
		outrect = r;
	    } else if (!keep2) {
		outrect = r2;
	    } else {
		outrect = new Rectangle2D.Float();
	    }
	    double x1 = Math.max(r.getX(), r2.getX());
	    double x2 = Math.min(r.getX()  + r.getWidth(), 
                                 r2.getX() + r2.getWidth());
	    double y1 = Math.max(r.getY(), r2.getY());
	    double y2 = Math.min(r.getY()  + r.getHeight(), 
                                 r2.getY() + r2.getHeight());

            if (((x2 - x1) < 0) || ((y2 - y1) < 0))
                // Width or height is negative. No intersection.
		outrect.setFrameFromDiagonal(0, 0, 0, 0);
            else
		outrect.setFrameFromDiagonal(x1, y1, x2, y2);
	    return outrect;
	}
	if (r.contains(s.getBounds2D())) {
	    if (keep2) {
		s = cloneShape(s);
	    }
	    return s;
	}
	return intersectByArea(r, s, keep1, keep2);
    }

    protected static Shape cloneShape(Shape s) {
	return new GeneralPath(s);
    }
    
    /*
     * Intersect two Shapes using the Area class.  Presumably other
     * attempts at simpler intersection methods proved fruitless.
     * The boolean arguments keep1 and keep2 specify whether or not
     * the first or second shapes can be modified during the operation
     * or whether that shape must be "kept" unmodified.
     * @see #intersectShapes
     * @see #intersectRectShape
     */
    Shape intersectByArea(Shape s1, Shape s2, boolean keep1, boolean keep2) {
	Area a1, a2;

	// First see if we can find an overwriteable source shape
	// to use as our destination area to avoid duplication.
	if (!keep1 && (s1 instanceof Area)) {
	    a1 = (Area) s1;
	} else if (!keep2 && (s2 instanceof Area)) {
	    a1 = (Area) s2;
	    s2 = s1;
	} else {
	    a1 = new Area(s1);
	}

	if (s2 instanceof Area) {
	    a2 = (Area) s2;
	} else {
	    a2 = new Area(s2);
	}

	a1.intersect(a2);
	if (a1.isRectangular()) {
	    return a1.getBounds();
	}

	return a1;
    }

    /*
     * Intersect usrClip bounds and device bounds to determine the composite
     * rendering boundaries.
     */
    public Region getCompClip() {
	if (!surfaceData.isValid()) {
            // revalidateAll() implicitly recalculcates the composite clip
	    revalidateAll();
	}

	return clipRegion;
    }
    
    public Font getFont() {
        if (font == null) {
            font = defaultFont;
        }
        return font;
    }

    private static final double[] IDENT_MATRIX = {1, 0, 0, 1};
    private static final AffineTransform IDENT_ATX =
	new AffineTransform();

    private static final int MINALLOCATED = 8;
    private static final int TEXTARRSIZE = 17;
    private static double[][] textTxArr = new double[TEXTARRSIZE][];
    private static AffineTransform[] textAtArr =
	new AffineTransform[TEXTARRSIZE];

    static {
	for (int i=MINALLOCATED;i<TEXTARRSIZE;i++) {
	  textTxArr[i] = new double [] {i, 0, 0, i};
	  textAtArr[i] = new AffineTransform( textTxArr[i]);
	}
    }

    // cached state for various draw[String,Char,Byte] optimizations
    public FontInfo checkFontInfo(FontInfo info, Font font,
				  FontRenderContext frc) {
	/* Do not create a FontInfo object as part of construction of an
	 * SG2D as its possible it may never be needed - ie if no text
	 * is drawn using this SG2D.
	 */
	if (info == null) {
	    info = new FontInfo();
	}
  
	float ptSize = font.getSize2D();
	int txFontType;
	AffineTransform devAt, textAt=null;
	if (font.isTransformed()) {
	    textAt = font.getTransform();
	    textAt.scale(ptSize, ptSize);
	    txFontType = textAt.getType();
	    info.originX = (float)textAt.getTranslateX();
	    info.originY = (float)textAt.getTranslateY();
	    textAt.translate(-info.originX, -info.originY);
	    if (transformState >= TRANSFORM_TRANSLATESCALE) {
		transform.getMatrix(info.devTx = new double[4]);
		devAt = new AffineTransform(info.devTx);
		textAt.preConcatenate(devAt);
	    } else {
		info.devTx = IDENT_MATRIX;
		devAt = IDENT_ATX;
	    }
	    textAt.getMatrix(info.glyphTx = new double[4]);
	    double shearx = textAt.getShearX();
	    double scaley = textAt.getScaleY();
	    if (shearx != 0) {
		scaley = Math.sqrt(shearx * shearx + scaley * scaley);
	    }
	    info.pixelHeight = (int)(Math.abs(scaley)+0.5);
	} else {
	    txFontType = AffineTransform.TYPE_IDENTITY;
	    info.originX = info.originY = 0;
	    if (transformState >= TRANSFORM_TRANSLATESCALE) {
		transform.getMatrix(info.devTx = new double[4]);
		devAt = new AffineTransform(info.devTx);
		info.glyphTx = new double[4];
		for (int i = 0; i < 4; i++) {
		    info.glyphTx[i] = info.devTx[i] * ptSize;
		}
		textAt = new AffineTransform(info.glyphTx);
		double shearx = transform.getShearX();
		double scaley = transform.getScaleY();
		if (shearx != 0) {
		    scaley = Math.sqrt(shearx * shearx + scaley * scaley);
		}
		info.pixelHeight = (int)(Math.abs(scaley * ptSize)+0.5);
	    } else {
		/* If the double represents a common integral, we
		 * may have pre-allocated objects.
		 * A "sparse" array be seems to be as fast as a switch
		 * even for 3 or 4 pt sizes, and is more flexible.
		 * This should perform comparably in single-threaded
		 * rendering to the old code which synchronized on the
		 * class and scale better on MP systems.
		 */
		int pszInt = (int)ptSize;
		if (ptSize == pszInt &&
		    pszInt >= MINALLOCATED && pszInt < TEXTARRSIZE) {
		    info.glyphTx = textTxArr[pszInt];
		    textAt = textAtArr[pszInt];
		    info.pixelHeight = pszInt;
		} else {
		    info.pixelHeight = (int)(ptSize+0.5);
		}
		if (textAt == null) {
		    info.glyphTx = new double[] {ptSize, 0, 0, ptSize};
		    textAt = new AffineTransform(info.glyphTx);		    
		}

		info.devTx = IDENT_MATRIX;
		devAt = IDENT_ATX;
	    }
	}

	info.font2D = FontManager.getFont2D(font);

	int fmhint = fractionalMetricsHint;
	if (fmhint == SunHints.INTVAL_FRACTIONALMETRICS_DEFAULT) {
	    fmhint = SunHints.INTVAL_FRACTIONALMETRICS_OFF;
	}
	info.lcdSubPixPos = false; // conditionally set true in LCD mode.

	/* The text anti-aliasing hints that are set by the client need
	 * to be interpreted for the current state and stored in the
	 * FontInfo.aahint which is what will actually be used and
	 * will be one of OFF, ON, LCD_HRGB or LCD_VRGB.
	 * This is what pipe selection code should typically refer to, not
	 * textAntialiasHint. This means we are now evaluating the meaning
	 * of "default" here. Any pipe that really cares about that will
	 * also need to consult that variable.
	 * Otherwise these are being used only as args to getStrike,
	 * and are encapsulated in that object which is part of the
	 * FontInfo, so we do not need to store them directly as fields
	 * in the FontInfo object.
	 * That could change if FontInfo's were more selectively
	 * revalidated when graphics state changed. Presently this
	 * method re-evaluates all fields in the fontInfo.
	 * The strike doesn't need to know the RGB subpixel order. Just
	 * if its H or V orientation, so if an LCD option is specified we
	 * always pass in the RGB hint to the strike.
	 * frc is non-null only if this is a GlyphVector. For reasons
	 * which are probably a historical mistake the AA hint in a GV
	 * is honoured when we render, overriding the Graphics setting.
	 */
	int aahint;
	if (frc == null) {
	    aahint = textAntialiasHint;
	} else {
	    aahint = ((SunHints.Value)frc.getAntiAliasingHint()).getIndex();
	}
	if (aahint == SunHints.INTVAL_TEXT_ANTIALIAS_DEFAULT) {
	    if (antialiasHint == SunHints.INTVAL_ANTIALIAS_ON) {
		aahint = SunHints.INTVAL_TEXT_ANTIALIAS_ON;
	    } else {
		aahint = SunHints.INTVAL_TEXT_ANTIALIAS_OFF;
	    }
	} else {
	    /* If we are in checkFontInfo because a rendering hint has been
	     * set then all pipes are revalidated. But we can also
	     * be here because setFont() has been called when the 'gasp'
	     * hint is set, as then the font size determines the text pipe.
	     * See comments in SunGraphics2d.setFont(Font).
	     */
	    if (aahint == SunHints.INTVAL_TEXT_ANTIALIAS_GASP) {
		if (info.font2D.useAAForPtSize(info.pixelHeight)) {
		    aahint = SunHints.INTVAL_TEXT_ANTIALIAS_ON;
		} else {
		    aahint = SunHints.INTVAL_TEXT_ANTIALIAS_OFF;
		}
	    } else if (aahint >= SunHints.INTVAL_TEXT_ANTIALIAS_LCD_HRGB) {
		/* loops for default rendering modes are installed in the SG2D
		 * constructor. If there are none this will be null.
		 * Not all compositing modes update the render loops, so
		 * we also test that this is a mode we know should support
		 * this. One minor issue is that the loops aren't necessarily
		 * installed for a new rendering mode until after this
		 * method is called during pipeline validation. So it is
		 * theoretically possible that it was set to null for a
		 * compositing mode, the composite is then set back to Src,
		 * but the loop is still null when this is called and AA=ON
		 * is installed instead of an LCD mode.
		 * However this is done in the right order in SurfaceData.java
		 * so this is not likely to be a problem - but not
		 * guaranteed.
		 */		
		if (
		    !surfaceData.canRenderLCDText(this)
// 		      loops.drawGlyphListLCDLoop == null ||
// 		      compositeState > COMP_ISCOPY ||
// 		      paintState > PAINT_ALPHACOLOR
		      ) {
		    aahint = SunHints.INTVAL_TEXT_ANTIALIAS_ON;
		} else {
		    info.lcdRGBOrder = true;
		    /* Collapse these into just HRGB or VRGB.
		     * Pipe selection code needs only to test for these two.
		     * Since these both select the same pipe anyway its
		     * tempting to collapse into one value. But they are
		     * different strikes (glyph caches) so the distinction
		     * needs to be made for that purpose.
		     */
		    if (aahint == SunHints.INTVAL_TEXT_ANTIALIAS_LCD_HBGR) {
			aahint = SunHints.INTVAL_TEXT_ANTIALIAS_LCD_HRGB;
			info.lcdRGBOrder = false;
		    } else if
			(aahint == SunHints.INTVAL_TEXT_ANTIALIAS_LCD_VBGR) {
			aahint = SunHints.INTVAL_TEXT_ANTIALIAS_LCD_VRGB;
			info.lcdRGBOrder = false;
		    }
		    /* Support subpixel positioning only for the case in
		     * which the horizontal resolution is increased
		     */
		    info.lcdSubPixPos =
			fmhint == SunHints.INTVAL_FRACTIONALMETRICS_ON &&
			aahint == SunHints.INTVAL_TEXT_ANTIALIAS_LCD_HRGB;
		}
	    }
	}
	info.aaHint = aahint;
	info.fontStrike = info.font2D.getStrike(font, devAt, textAt,
						aahint, fmhint);
	return info;
    }

    public static boolean isRotated(double [] mtx) {
	if ((mtx[0] == mtx[3]) &&
	    (mtx[1] == 0.0) &&
            (mtx[2] == 0.0) &&
	    (mtx[0] > 0.0))
	{
            return false;
        }
 
        return true;
    }

    public void setFont(Font font) {
	/* replacing the reference equality test font != this.font with
	 * !font.equals(this.font) did not yield any measurable difference
	 * in testing, but there may be yet to be identified cases where it
	 * is beneficial.
	 */
        if (font != null && font!=this.font/*!font.equals(this.font)*/) {
	    /* In the GASP AA case the textpipe depends on the glyph size
	     * as determined by graphics and font transforms as well as the
	     * font size, and information in the font. But we may invalidate
	     * the pipe only to find that it made no difference. 
	     * Deferring pipe invalidation to checkFontInfo won't work because
	     * when called we may already be rendering to the wrong pipe.
	     * So, if the font is transformed, or the graphics has more than
	     * a simple scale, we'll take that as enough of a hint to
	     * revalidate everything. But if they aren't we will 
	     * use the font's point size to query the gasp table and see if
	     * what it says matches what's currently being used, in which
	     * case there's no need to invalidate the textpipe.
	     * This should be sufficient for all typical uses cases.
	     */
	    if (textAntialiasHint == SunHints.INTVAL_TEXT_ANTIALIAS_GASP &&
		textpipe != invalidpipe &&
		(transformState > TRANSFORM_ANY_TRANSLATE ||
		 font.isTransformed() ||
		 fontInfo == null || // Precaution, if true shouldn't get here
		 (fontInfo.aaHint == SunHints.INTVAL_TEXT_ANTIALIAS_ON) !=
		 FontManager.getFont2D(font).useAAForPtSize(font.getSize()))) {
		textpipe = invalidpipe;
	    }
            this.font = font;
            this.fontMetrics = null;
	    this.validFontInfo = false;
        }
    }

    public FontInfo getFontInfo() {
	if (!validFontInfo) {
	    this.fontInfo = checkFontInfo(this.fontInfo, font, null);
	    validFontInfo = true;
	}
        return this.fontInfo;
    }

    /* Used by drawGlyphVector which specifies its own font. */
    public FontInfo getGVFontInfo(Font font, FontRenderContext frc) {
	if (glyphVectorFontInfo != null &&
	    glyphVectorFontInfo.font == font &&
	    glyphVectorFRC == frc) {
	    return glyphVectorFontInfo;
	} else {
	    glyphVectorFRC = frc;
	    return glyphVectorFontInfo =
		checkFontInfo(glyphVectorFontInfo, font, frc);
	}
    }

    public FontMetrics getFontMetrics() {
        if (this.fontMetrics != null) {
            return this.fontMetrics;
        }
	/* NB the constructor and the setter disallow "font" being null */
	return this.fontMetrics =
	   FontDesignMetrics.getMetrics(font, getFontRenderContext());
    }

    public FontMetrics getFontMetrics(Font font) {
        if ((this.fontMetrics != null) && (font == this.font)) {
            return this.fontMetrics;
        }
	FontMetrics fm =
	  FontDesignMetrics.getMetrics(font, getFontRenderContext());

        if (this.font == font) {
            this.fontMetrics = fm;
        }
        return fm;
    }

    /**
     * Checks to see if a Path intersects the specified Rectangle in device
     * space.  The rendering attributes taken into account include the
     * clip, transform, and stroke attributes.
     * @param rect The area in device space to check for a hit.
     * @param p The path to check for a hit.
     * @param onStroke Flag to choose between testing the stroked or
     * the filled path.
     * @return True if there is a hit, false otherwise.
     * @see #setStroke
     * @see #fillPath
     * @see #drawPath
     * @see #transform
     * @see #setTransform
     * @see #clip
     * @see #setClip
     */
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        if (onStroke) {
            s = stroke.createStrokedShape(s);
        }

        s = transformShape(s);
	if ((constrainX|constrainY) != 0) {
	    rect = new Rectangle(rect);
	    rect.translate(constrainX, constrainY);
	}

        return s.intersects(rect);
    }

    /**
     * Return the ColorModel associated with this Graphics2D.
     */
    public ColorModel getDeviceColorModel() {
	return surfaceData.getColorModel();
    }

    /**
     * Return the device configuration associated with this Graphics2D.
     */
    public GraphicsConfiguration getDeviceConfiguration() {
	return surfaceData.getDeviceConfiguration();
    }

    /**
     * Return the SurfaceData object assigned to manage the destination
     * drawable surface of this Graphics2D.
     */
    public final SurfaceData getSurfaceData() {
	return surfaceData;
    }

    /**
     * Sets the Composite in the current graphics state. Composite is used
     * in all drawing methods such as drawImage, drawString, drawPath,
     * and fillPath.  It specifies how new pixels are to be combined with
     * the existing pixels on the graphics device in the rendering process.
     * @param comp The Composite object to be used for drawing.
     * @see java.awt.Graphics#setXORMode
     * @see java.awt.Graphics#setPaintMode
     * @see AlphaComposite
     */
    public void setComposite(Composite comp) {
	if (composite == comp) {
	    return;
	}
	int newCompState;
	CompositeType newCompType;
        if (comp instanceof AlphaComposite) {
	    AlphaComposite alphacomp = (AlphaComposite) comp;
	    newCompType = CompositeType.forAlphaComposite(alphacomp);
	    if (newCompType == CompositeType.SrcOverNoEa) {
		if (paintState == PAINT_OPAQUECOLOR ||
                    (paintState > PAINT_ALPHACOLOR &&
                     paint.getTransparency() == Transparency.OPAQUE))
                {
                    newCompState = COMP_ISCOPY;
                } else {
                    newCompState = COMP_ALPHA;
                }
	    } else if (newCompType == CompositeType.SrcNoEa ||
                       newCompType == CompositeType.Src ||
                       newCompType == CompositeType.Clear)
            {
                newCompState = COMP_ISCOPY;
            } else if (surfaceData.getTransparency() == Transparency.OPAQUE &&
                       newCompType == CompositeType.SrcIn)
            {
                newCompState = COMP_ISCOPY;
            } else {
                newCompState = COMP_ALPHA;
	    }
        } else if (comp instanceof XORComposite) {
            newCompState = COMP_XOR;
	    newCompType = CompositeType.Xor;
	} else if (comp == null) {
	    throw new IllegalArgumentException("null Composite");
        } else {
	    surfaceData.checkCustomComposite();
            newCompState = COMP_CUSTOM;
	    newCompType = CompositeType.General;
        }
        if (compositeState != newCompState ||
	    imageComp != newCompType)
	{
	    compositeState = newCompState;
	    imageComp = newCompType;
	    invalidatePipe();
        }
	composite = comp;
        if (paintState <= PAINT_ALPHACOLOR) {
            validateColor();
        }
    }

    /**
     * Sets the Paint in the current graphics state.
     * @param paint The Paint object to be used to generate color in
     * the rendering process.
     * @see java.awt.Graphics#setColor
     * @see GradientPaint
     * @see TexturePaint
     */
    public void setPaint(Paint paint) {
	if (paint instanceof Color) {
	    setColor((Color) paint);
	    return;
	}
	if (paint == null || this.paint == paint) {
	    return;
	}
        this.paint = paint;
	if (imageComp == CompositeType.SrcOverNoEa) {
	    // special case where compState depends on opacity of paint
	    if (paint.getTransparency() == Transparency.OPAQUE) {
		if (compositeState != COMP_ISCOPY) {
		    compositeState = COMP_ISCOPY;
		}
	    } else {
		if (compositeState == COMP_ISCOPY) {
		    compositeState = COMP_ALPHA;
		}
	    }
	}
        Class paintClass = paint.getClass();
        if (paintClass == GradientPaint.class) {
            paintState = PAINT_GRADIENT;
        } else if (paintClass == LinearGradientPaint.class) {
            paintState = PAINT_LIN_GRADIENT;
        } else if (paintClass == RadialGradientPaint.class) {
            paintState = PAINT_RAD_GRADIENT;
        } else if (paintClass == TexturePaint.class) {
            paintState = PAINT_TEXTURE;
        } else {
            paintState = PAINT_CUSTOM;
        }
        invalidatePipe();
    }

    static final int NON_UNIFORM_SCALE_MASK =
	(AffineTransform.TYPE_GENERAL_TRANSFORM |
	 AffineTransform.TYPE_GENERAL_SCALE);
    public static final double MinPenSizeAASquared = 
	(DuctusRenderer.MinPenSizeAA * DuctusRenderer.MinPenSizeAA);
    // Since inaccuracies in the trig package can cause us to
    // calculated a rotated pen width of just slightly greater
    // than 1.0, we add a fudge factor to our comparison value
    // here so that we do not misclassify single width lines as
    // wide lines under certain rotations.
    public static final double MinPenSizeSquared = 1.000000001;

    private void validateBasicStroke(BasicStroke bs) {
	boolean aa = (antialiasHint == SunHints.INTVAL_ANTIALIAS_ON);
	if (transformState < TRANSFORM_TRANSLATESCALE) {
	    if (aa) {
		if (bs.getLineWidth() <= DuctusRenderer.MinPenSizeAA) {
		    if (bs.getDashArray() == null) {
			strokeState = STROKE_THIN;
		    } else {
			strokeState = STROKE_THINDASHED;
		    }
		} else {
		    strokeState = STROKE_WIDE;
		}
	    } else {
		if (bs == defaultStroke) {
		    strokeState = STROKE_THIN;
		} else if (bs.getLineWidth() <= 1.0f) {
		    if (bs.getDashArray() == null) {
			strokeState = STROKE_THIN;
		    } else {
			strokeState = STROKE_THINDASHED;
		    }
		} else {
		    strokeState = STROKE_WIDE;
		}
	    }
	} else {
	    double widthsquared;
	    if ((transform.getType() & NON_UNIFORM_SCALE_MASK) == 0) {
		/* sqrt omitted, compare to squared limits below. */
		widthsquared = Math.abs(transform.getDeterminant());
	    } else {
		/* First calculate the "maximum scale" of this transform. */
		double A = transform.getScaleX();	// m00
		double C = transform.getShearX();	// m01
		double B = transform.getShearY();	// m10
		double D = transform.getScaleY();	// m11

		/*
		 * Given a 2 x 2 affine matrix [ A B ] such that
		 *                             [ C D ]
		 * v' = [x' y'] = [Ax + Cy, Bx + Dy], we want to
		 * find the maximum magnitude (norm) of the vector v'
		 * with the constraint (x^2 + y^2 = 1).
		 * The equation to maximize is
		 *     |v'| = sqrt((Ax+Cy)^2+(Bx+Dy)^2)
		 * or  |v'| = sqrt((AA+BB)x^2 + 2(AC+BD)xy + (CC+DD)y^2).
		 * Since sqrt is monotonic we can maximize |v'|^2
		 * instead and plug in the substitution y = sqrt(1 - x^2).
		 * Trigonometric equalities can then be used to get
		 * rid of most of the sqrt terms.
		 */
		double EA = A*A + B*B;		// x^2 coefficient
		double EB = 2*(A*C + B*D);	// xy coefficient
		double EC = C*C + D*D;		// y^2 coefficient

		/*
		 * There is a lot of calculus omitted here.
		 *
		 * Conceptually, in the interests of understanding the
		 * terms that the calculus produced we can consider
		 * that EA and EC end up providing the lengths along
		 * the major axes and the hypot term ends up being an
		 * adjustment for the additional length along the off-axis
		 * angle of rotated or sheared ellipses as well as an
		 * adjustment for the fact that the equation below
		 * averages the two major axis lengths.  (Notice that
		 * the hypot term contains a part which resolves to the
		 * difference of these two axis lengths in the absence
		 * of rotation.)
		 *
		 * In the calculus, the ratio of the EB and (EA-EC) terms
		 * ends up being the tangent of 2*theta where theta is
		 * the angle that the long axis of the ellipse makes
		 * with the horizontal axis.  Thus, this equation is
		 * calculating the length of the hypotenuse of a triangle
		 * along that axis.
		 */
		double hypot = Math.sqrt(EB*EB + (EA-EC)*(EA-EC));

		/* sqrt omitted, compare to squared limits below. */
		widthsquared = ((EA + EC + hypot)/2.0);
	    }
	    if (bs != defaultStroke) {
		widthsquared *= bs.getLineWidth() * bs.getLineWidth();
	    }
	    if (widthsquared <=
		(aa ? MinPenSizeAASquared : MinPenSizeSquared))
	    {
		if (bs.getDashArray() == null) {
		    strokeState = STROKE_THIN;
		} else {
		    strokeState = STROKE_THINDASHED;
		}
	    } else {
		strokeState = STROKE_WIDE;
	    }
	}
    }

    /*
     * Sets the Stroke in the current graphics state.
     * @param s The Stroke object to be used to stroke a Path in
     * the rendering process.
     * @see BasicStroke
     */
    public void setStroke(Stroke s) {
	if (s == null) {
	    throw new IllegalArgumentException("null Stroke");
	}
        int saveStrokeState = strokeState;
        stroke = s;
        if (s instanceof BasicStroke) {
	    validateBasicStroke((BasicStroke) s);
        } else {
            strokeState = STROKE_CUSTOM;
        }
        if (strokeState != saveStrokeState) {
	    invalidatePipe();
        }
    }

    /**
     * Sets the preferences for the rendering algorithms.
     * Hint categories include controls for rendering quality and
     * overall time/quality trade-off in the rendering process.
     * @param hintKey The key of hint to be set. The strings are
     * defined in the RenderingHints class.  
     * @param hintValue The value indicating preferences for the specified
     * hint category. These strings are defined in the RenderingHints
     * class.  
     * @see RenderingHints
     */
    public void setRenderingHint(Key hintKey, Object hintValue) {
	// If we recognize the key, we must recognize the value
	//     otherwise throw an IllegalArgumentException
	//     and do not change the Hints object
	// If we do not recognize the key, just pass it through
	//     to the Hints object untouched
	if (!hintKey.isCompatibleValue(hintValue)) {
	    throw new IllegalArgumentException
		(hintValue+" is not compatible with "+hintKey);
	}
	if (hintKey instanceof SunHints.Key) {
	    boolean stateChanged;
	    boolean textStateChanged = false;
	    boolean recognized = true;
	    SunHints.Key sunKey = (SunHints.Key) hintKey;
	    int newHint;
	    if (sunKey == SunHints.KEY_TEXT_ANTIALIAS_LCD_CONTRAST) {
		newHint = ((Integer)hintValue).intValue();
	    } else {
		newHint = ((SunHints.Value) hintValue).getIndex();
	    }
	    switch (sunKey.getIndex()) {
	    case SunHints.INTKEY_RENDERING:
		stateChanged = (renderHint != newHint);
		if (stateChanged) {
		    renderHint = newHint;
		    if (interpolationHint == -1) {
			interpolationType =
			    (newHint == SunHints.INTVAL_RENDER_QUALITY
			     ? AffineTransformOp.TYPE_BILINEAR
			     : AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		    }
		}
		break;
	    case SunHints.INTKEY_ANTIALIASING:
		stateChanged = (antialiasHint != newHint);
		antialiasHint = newHint;
		if (stateChanged) {
		    textStateChanged =
			(textAntialiasHint ==
                         SunHints.INTVAL_TEXT_ANTIALIAS_DEFAULT) ||
                        (textAntialiasHint >=
                         SunHints.INTVAL_TEXT_ANTIALIAS_LCD_HRGB);
		    if (strokeState != STROKE_CUSTOM) {
			validateBasicStroke((BasicStroke) stroke);
		    }
		}
		break;
	    case SunHints.INTKEY_TEXT_ANTIALIASING:
		stateChanged = (textAntialiasHint != newHint);
		textStateChanged = stateChanged;
		textAntialiasHint = newHint;
		break;
	    case SunHints.INTKEY_FRACTIONALMETRICS:
		stateChanged = (fractionalMetricsHint != newHint);
		textStateChanged = stateChanged;
		fractionalMetricsHint = newHint;
		break;
	    case SunHints.INTKEY_AATEXT_LCD_CONTRAST:
		stateChanged = false;
		/* Already have validated it is an int 100 <= newHint <= 250 */
		lcdTextContrast = newHint;
		break;
	    case SunHints.INTKEY_INTERPOLATION:
		interpolationHint = newHint;
		switch (newHint) {
		case SunHints.INTVAL_INTERPOLATION_BICUBIC:
		    newHint = AffineTransformOp.TYPE_BICUBIC;
		    break;
		case SunHints.INTVAL_INTERPOLATION_BILINEAR:
		    newHint = AffineTransformOp.TYPE_BILINEAR;
		    break;
		default:
		case SunHints.INTVAL_INTERPOLATION_NEAREST_NEIGHBOR:
		    newHint = AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
		    break;
		}
		stateChanged = (interpolationType != newHint);
		interpolationType = newHint;
		break;
	    case SunHints.INTKEY_STROKE_CONTROL:
		stateChanged = (strokeHint != newHint);
		strokeHint = newHint;
		break;
	    default:
		recognized = false;
		stateChanged = false;
		break;
	    }
	    if (recognized) {
		if (stateChanged) {
		    invalidatePipe();
		    if (textStateChanged) {
			fontMetrics = null;
			this.cachedFRC = null;
			validFontInfo = false;
			this.glyphVectorFontInfo = null;
		    }
		}
		if (hints != null) {
		    hints.put(hintKey, hintValue);
		}
		return;
	    }
	}
	// Nothing we recognize so none of "our state" has changed
	if (hints == null) {
	    hints = makeHints(null);
	}
	hints.put(hintKey, hintValue);
    }


    /**
     * Returns the preferences for the rendering algorithms.
     * @param hintCategory The category of hint to be set. The strings
     * are defined in the RenderingHints class.
     * @return The preferences for rendering algorithms. The strings
     * are defined in the RenderingHints class.
     * @see RenderingHints
     */
    public Object getRenderingHint(Key hintKey) {
	if (hints != null) {
	    return hints.get(hintKey);
	}
	if (!(hintKey instanceof SunHints.Key)) {
	    return null;
	}
	int keyindex = ((SunHints.Key)hintKey).getIndex();
	switch (keyindex) {
	case SunHints.INTKEY_RENDERING:
	    return SunHints.Value.get(SunHints.INTKEY_RENDERING,
				      renderHint);
	case SunHints.INTKEY_ANTIALIASING:
	    return SunHints.Value.get(SunHints.INTKEY_ANTIALIASING,
				      antialiasHint);
	case SunHints.INTKEY_TEXT_ANTIALIASING:
	    return SunHints.Value.get(SunHints.INTKEY_TEXT_ANTIALIASING,
				      textAntialiasHint);
	case SunHints.INTKEY_FRACTIONALMETRICS:
	    return SunHints.Value.get(SunHints.INTKEY_FRACTIONALMETRICS,
				      fractionalMetricsHint);
	case SunHints.INTKEY_AATEXT_LCD_CONTRAST:
	    return new Integer(lcdTextContrast);
	case SunHints.INTKEY_INTERPOLATION:
	    switch (interpolationHint) {
	    case SunHints.INTVAL_INTERPOLATION_NEAREST_NEIGHBOR:
		return SunHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
	    case SunHints.INTVAL_INTERPOLATION_BILINEAR:
		return SunHints.VALUE_INTERPOLATION_BILINEAR;
	    case SunHints.INTVAL_INTERPOLATION_BICUBIC:
		return SunHints.VALUE_INTERPOLATION_BICUBIC;
	    }
	    return null;
	case SunHints.INTKEY_STROKE_CONTROL:
	    return SunHints.Value.get(SunHints.INTKEY_STROKE_CONTROL,
				      strokeHint);
	}
	return null;
    }

    /**
     * Sets the preferences for the rendering algorithms.
     * Hint categories include controls for rendering quality and
     * overall time/quality trade-off in the rendering process.
     * @param hints The rendering hints to be set
     * @see RenderingHints
     */
    public void setRenderingHints(Map<?,?> hints) {
	this.hints = null;
	renderHint = SunHints.INTVAL_RENDER_DEFAULT;
	antialiasHint = SunHints.INTVAL_ANTIALIAS_OFF;
	textAntialiasHint = SunHints.INTVAL_TEXT_ANTIALIAS_DEFAULT;
	fractionalMetricsHint = SunHints.INTVAL_FRACTIONALMETRICS_OFF;
	lcdTextContrast = lcdTextContrastDefaultValue;
	interpolationHint = -1;
	interpolationType = AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
	boolean customHintPresent = false;
	Iterator iter = hints.keySet().iterator();
	while (iter.hasNext()) {
	    Object key = iter.next();
	    if (key == SunHints.KEY_RENDERING ||
		key == SunHints.KEY_ANTIALIASING ||
		key == SunHints.KEY_TEXT_ANTIALIASING ||
		key == SunHints.KEY_FRACTIONALMETRICS ||
		key == SunHints.KEY_TEXT_ANTIALIAS_LCD_CONTRAST ||
		key == SunHints.KEY_STROKE_CONTROL ||
		key == SunHints.KEY_INTERPOLATION)
	    {
		setRenderingHint((Key) key, hints.get(key));
	    } else {
		customHintPresent = true;
	    }
	}
	if (customHintPresent) {
	    this.hints = makeHints(hints);
	}
	invalidatePipe();
    }

    /**
     * Adds a number of preferences for the rendering algorithms.
     * Hint categories include controls for rendering quality and
     * overall time/quality trade-off in the rendering process.
     * @param hints The rendering hints to be set
     * @see RenderingHints
     */
    public void addRenderingHints(Map<?,?> hints) {
	boolean customHintPresent = false;
	Iterator iter = hints.keySet().iterator();
	while (iter.hasNext()) {
	    Object key = iter.next();
	    if (key == SunHints.KEY_RENDERING ||
		key == SunHints.KEY_ANTIALIASING ||
		key == SunHints.KEY_TEXT_ANTIALIASING ||
		key == SunHints.KEY_FRACTIONALMETRICS ||
		key == SunHints.KEY_TEXT_ANTIALIAS_LCD_CONTRAST ||
		key == SunHints.KEY_STROKE_CONTROL ||
		key == SunHints.KEY_INTERPOLATION)
	    {
		setRenderingHint((Key) key, hints.get(key));
	    } else {
		customHintPresent = true;
	    }
	}
	if (customHintPresent) {
	    if (this.hints == null) {
		this.hints = makeHints(hints);
	    } else {
		this.hints.putAll(hints);
	    }
	}
    }

    /**
     * Gets the preferences for the rendering algorithms.
     * Hint categories include controls for rendering quality and
     * overall time/quality trade-off in the rendering process.
     * @see RenderingHints
     */
    public RenderingHints getRenderingHints() {
	if (hints == null) {
	    return makeHints(null);
	} else {
	    return (RenderingHints) hints.clone();
	}
    }

    RenderingHints makeHints(Map hints) {
	RenderingHints model = new RenderingHints(hints);
	model.put(SunHints.KEY_RENDERING,
		  SunHints.Value.get(SunHints.INTKEY_RENDERING,
				     renderHint));
	model.put(SunHints.KEY_ANTIALIASING,
		  SunHints.Value.get(SunHints.INTKEY_ANTIALIASING,
				     antialiasHint));
	model.put(SunHints.KEY_TEXT_ANTIALIASING,
		  SunHints.Value.get(SunHints.INTKEY_TEXT_ANTIALIASING,
				     textAntialiasHint));
	model.put(SunHints.KEY_FRACTIONALMETRICS,
		  SunHints.Value.get(SunHints.INTKEY_FRACTIONALMETRICS,
				     fractionalMetricsHint));
	model.put(SunHints.KEY_TEXT_ANTIALIAS_LCD_CONTRAST,
		  new Integer(lcdTextContrast));
	Object value;
	switch (interpolationHint) {
	case SunHints.INTVAL_INTERPOLATION_NEAREST_NEIGHBOR:
	    value = SunHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
	    break;
	case SunHints.INTVAL_INTERPOLATION_BILINEAR:
	    value = SunHints.VALUE_INTERPOLATION_BILINEAR;
	    break;
	case SunHints.INTVAL_INTERPOLATION_BICUBIC:
	    value = SunHints.VALUE_INTERPOLATION_BICUBIC;
	    break;
	default:
	    value = null;
	    break;
	}
	if (value != null) {
	    model.put(SunHints.KEY_INTERPOLATION, value);
	}
	model.put(SunHints.KEY_STROKE_CONTROL,
		  SunHints.Value.get(SunHints.INTKEY_STROKE_CONTROL,
				     strokeHint));
	return model;
    }
    
    /**
     * Concatenates the current transform of this Graphics2D with a
     * translation transformation.
     * This is equivalent to calling transform(T), where T is an
     * AffineTransform represented by the following matrix:
     * <pre>
     *		[   1    0    tx  ]
     *		[   0    1    ty  ]
     *		[   0    0    1   ]
     * </pre>
     */
    public void translate(double tx, double ty) {
	transform.translate(tx, ty);
	invalidateTransform();
    }

    /**
     * Concatenates the current transform of this Graphics2D with a
     * rotation transformation.
     * This is equivalent to calling transform(R), where R is an
     * AffineTransform represented by the following matrix:
     * <pre>
     *		[   cos(theta)    -sin(theta)    0   ]
     *		[   sin(theta)     cos(theta)    0   ]
     *		[       0              0         1   ]
     * </pre>
     * Rotating with a positive angle theta rotates points on the positive
     * x axis toward the positive y axis.
     * @param theta The angle of rotation in radians.
     */
    public void rotate(double theta) {
	transform.rotate(theta);
	invalidateTransform();
    }

    /**
     * Concatenates the current transform of this Graphics2D with a
     * translated rotation transformation.
     * This is equivalent to the following sequence of calls:
     * <pre>
     *		translate(x, y);
     *		rotate(theta);
     *		translate(-x, -y);
     * </pre>
     * Rotating with a positive angle theta rotates points on the positive
     * x axis toward the positive y axis.
     * @param theta The angle of rotation in radians.
     * @param x The x coordinate of the origin of the rotation
     * @param y The x coordinate of the origin of the rotation
     */
    public void rotate(double theta, double x, double y) {
	transform.rotate(theta, x, y);
	invalidateTransform();
    }

    /**
     * Concatenates the current transform of this Graphics2D with a
     * scaling transformation.
     * This is equivalent to calling transform(S), where S is an
     * AffineTransform represented by the following matrix:
     * <pre>
     *		[   sx   0    0   ]
     *		[   0    sy   0   ]
     *		[   0    0    1   ]
     * </pre>
     */
    public void scale(double sx, double sy) {
	transform.scale(sx, sy);
	invalidateTransform();
    }

    /**
     * Concatenates the current transform of this Graphics2D with a
     * shearing transformation.
     * This is equivalent to calling transform(SH), where SH is an
     * AffineTransform represented by the following matrix:
     * <pre>
     *		[   1   shx   0   ]
     *		[  shy   1    0   ]
     *		[   0    0    1   ]
     * </pre>
     * @param shx The factor by which coordinates are shifted towards the
     * positive X axis direction according to their Y coordinate
     * @param shy The factor by which coordinates are shifted towards the
     * positive Y axis direction according to their X coordinate
     */
    public void shear(double shx, double shy) {
	transform.shear(shx, shy);
	invalidateTransform();
    }

    /**
     * Composes a Transform object with the transform in this
     * Graphics2D according to the rule last-specified-first-applied.
     * If the currrent transform is Cx, the result of composition
     * with Tx is a new transform Cx'.  Cx' becomes the current
     * transform for this Graphics2D.
     * Transforming a point p by the updated transform Cx' is
     * equivalent to first transforming p by Tx and then transforming
     * the result by the original transform Cx.  In other words,
     * Cx'(p) = Cx(Tx(p)).
     * A copy of the Tx is made, if necessary, so further
     * modifications to Tx do not affect rendering.
     * @param Tx The Transform object to be composed with the current
     * transform.
     * @see #setTransform
     * @see AffineTransform
     */
    public void transform(AffineTransform xform) {
        this.transform.concatenate(xform); 
	invalidateTransform();
    }
    
    /**
     * Translate
     */
    public void translate(int x, int y) {
	transform.translate(x, y);
	if (transformState <= TRANSFORM_INT_TRANSLATE) {
	    transX += x;
	    transY += y;
	    transformState = (((transX | transY) == 0) ?
			      TRANSFORM_ISIDENT : TRANSFORM_INT_TRANSLATE);
	} else {
	    invalidateTransform();
	}
    }

    /**
     * Sets the Transform in the current graphics state.
     * @param Tx The Transform object to be used in the rendering process.
     * @see #transform
     * @see TransformChain
     * @see AffineTransform
     */
    public void setTransform(AffineTransform Tx) {
	if ((constrainX|constrainY) == 0) {
	    transform.setTransform(Tx);
	} else {
	    transform.setToTranslation(constrainX, constrainY);
	    transform.concatenate(Tx);
	}
	invalidateTransform();
    }

    protected void invalidateTransform() {
	int type = transform.getType();
	int origTransformState = transformState;
	if (type == AffineTransform.TYPE_IDENTITY) {
            transformState = TRANSFORM_ISIDENT;
	    transX = transY = 0;
	} else if (type == AffineTransform.TYPE_TRANSLATION) {
	    double dtx = transform.getTranslateX();
	    double dty = transform.getTranslateY();
	    transX = (int) Math.floor(dtx + 0.5);
	    transY = (int) Math.floor(dty + 0.5);
	    if (dtx == transX && dty == transY) {
		transformState = TRANSFORM_INT_TRANSLATE;
	    } else {
		transformState = TRANSFORM_ANY_TRANSLATE;
	    }
	} else if ((type & (AffineTransform.TYPE_FLIP |
			    AffineTransform.TYPE_MASK_ROTATION |
			    AffineTransform.TYPE_GENERAL_TRANSFORM)) == 0)
	{
	    transformState = TRANSFORM_TRANSLATESCALE;
	    transX = transY = 0;
	} else {
	    transformState = TRANSFORM_GENERIC;
	    transX = transY = 0;
	}

	if (transformState >= TRANSFORM_TRANSLATESCALE ||
	    origTransformState >= TRANSFORM_TRANSLATESCALE)
	{
	    /* Its only in this case that the previous or current transform
	     * was more than a translate that font info is invalidated
	     */
	    cachedFRC = null;
	    this.validFontInfo = false;
	    this.fontMetrics = null;
	    this.glyphVectorFontInfo = null;

	    if (transformState != origTransformState) {
		invalidatePipe();
	    }
	}
	if (strokeState != STROKE_CUSTOM) {
	    validateBasicStroke((BasicStroke) stroke);
	}
    }

    /**
     * Returns the current Transform in the Graphics2D state.
     * @see #transform
     * @see #setTransform
     */
    public AffineTransform getTransform() {
	if ((constrainX|constrainY) == 0) {
	    return new AffineTransform(transform);
	}
	AffineTransform tx =
	    AffineTransform.getTranslateInstance(-constrainX, -constrainY);
	tx.concatenate(transform);
        return tx;
    }

    /**
     * Returns the current Transform ignoring the "constrain"
     * rectangle.
     */
    public AffineTransform cloneTransform() {
	return new AffineTransform(transform);
    }

    /**
     * Returns the current Paint in the Graphics2D state.
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     */
    public Paint getPaint() {
        return paint;
    }

    /**
     * Returns the current Composite in the Graphics2D state.
     * @see #setComposite
     */
    public Composite getComposite() {
        return composite;
    }

    public Color getColor() {
        return foregroundColor;
    }

    /*
     * Validate the eargb and pixel fields against the current color.
     *
     * The eargb field must take into account the extraAlpha
     * value of an AlphaComposite.  It may also take into account
     * the Fsrc Porter-Duff blending function if such a function is
     * a constant (see handling of Clear mode below).  For instance,
     * by factoring in the (Fsrc == 0) state of the Clear mode we can
     * use a SrcNoEa loop just as easily as a general Alpha loop
     * since the math will be the same in both cases.
     *
     * The pixel field will always be the best pixel data choice for
     * the final result of all calculations applied to the eargb field.
     *
     * Note that this method is only necessary under the following
     * conditions:
     *     (paintState <= PAINT_ALPHA_COLOR &&
     *      compositeState <= COMP_CUSTOM)
     * though nothing bad will happen if it is run in other states.
     */
    final void validateColor() {
        int eargb;
	if (imageComp == CompositeType.Clear) {
	    eargb = 0;
	} else {
            eargb = foregroundColor.getRGB();
            if (compositeState <= COMP_ALPHA &&
                imageComp != CompositeType.SrcNoEa &&
                imageComp != CompositeType.SrcOverNoEa)
            {
                AlphaComposite alphacomp = (AlphaComposite) composite;
                int a = Math.round(alphacomp.getAlpha() * (eargb >>> 24));
                eargb = (eargb & 0x00ffffff) | (a << 24);
            }
	}
        this.eargb = eargb;
	this.pixel = surfaceData.pixelFor(eargb);
    }

    public void setColor(Color color) {
	if (color == null || color == paint) {
	    return;
	}
        this.paint = foregroundColor = color;
        validateColor();
	if ((eargb >> 24) == -1) {
	    if (paintState == PAINT_OPAQUECOLOR) {
		return;
	    }
	    paintState = PAINT_OPAQUECOLOR;
	    if (imageComp == CompositeType.SrcOverNoEa) {
		// special case where compState depends on opacity of paint
		compositeState = COMP_ISCOPY;
	    }
	} else {
	    if (paintState == PAINT_ALPHACOLOR) {
		return;
	    }
	    paintState = PAINT_ALPHACOLOR;
	    if (imageComp == CompositeType.SrcOverNoEa) {
		// special case where compState depends on opacity of paint
		compositeState = COMP_ALPHA;
	    }
	}
	invalidatePipe();
    }

    /**
     * Sets the background color in this context used for clearing a region.
     * When Graphics2D is constructed for a component, the backgroung color is
     * inherited from the component. Setting the background color in the
     * Graphics2D context only affects the subsequent clearRect() calls and
     * not the background color of the component. To change the background
     * of the component, use appropriate methods of the component.
     * @param color The background color that should be used in
     * subsequent calls to clearRect().
     * @see getBackground
     * @see Graphics.clearRect()
     */
    public void setBackground(Color color) {
        backgroundColor = color;
    }

    /**
     * Returns the background color used for clearing a region.
     * @see setBackground
     */
    public Color getBackground() {
        return backgroundColor;
    }

    /**
     * Returns the current Stroke in the Graphics2D state.
     * @see setStroke
     */
    public Stroke getStroke() {
        return stroke;
    }
    
    public Rectangle getClipBounds() {
	Rectangle r;
	if (clipState == CLIP_DEVICE) {
	    r = null;
	} else if (transformState <= TRANSFORM_INT_TRANSLATE) {
	    if (usrClip instanceof Rectangle) {
		r = new Rectangle((Rectangle) usrClip);
	    } else {
		r = usrClip.getBounds();
	    }
	    r.translate(-transX, -transY);
	} else {
	    r = getClip().getBounds();
	}
	return r;
    }

    public Rectangle getClipBounds(Rectangle r) {
	if (clipState != CLIP_DEVICE) {
	    if (transformState <= TRANSFORM_INT_TRANSLATE) {
		if (usrClip instanceof Rectangle) {
		    r.setBounds((Rectangle) usrClip);
		} else {
		    r.setBounds(usrClip.getBounds());
		}
		r.translate(-transX, -transY);
	    } else {
		r.setBounds(getClip().getBounds());
	    }
	} else if (r == null) {
	    throw new NullPointerException("null rectangle parameter");
	}
	return r;
    }

    public boolean hitClip(int x, int y, int width, int height) {
	if (width <= 0 || height <= 0) {
	    return false;
	}
	if (transformState > TRANSFORM_INT_TRANSLATE) {
	    // Note: Technically the most accurate test would be to
	    // raster scan the parallelogram of the transformed rectangle
	    // and do a span for span hit test against the clip, but for
	    // speed we approximate the test with a bounding box of the
	    // transformed rectangle.  The cost of rasterizing the
	    // transformed rectangle is probably high enough that it is
	    // not worth doing so to save the caller from having to call
	    // a rendering method where we will end up discovering the
	    // same answer in about the same amount of time anyway.
	    // This logic breaks down if this hit test is being performed
	    // on the bounds of a group of shapes in which case it might
	    // be beneficial to be a little more accurate to avoid lots
	    // of subsequent rendering calls.  In either case, this relaxed
	    // test should not be significantly less accurate than the
	    // optimal test for most transforms and so the conservative
	    // answer should not cause too much extra work.

	    double d[] = {
		x, y,
		x+width, y,
		x, y+height,
		x+width, y+height
	    };
	    transform.transform(d, 0, d, 0, 4);
	    x = (int) Math.floor(Math.min(Math.min(d[0], d[2]),
					  Math.min(d[4], d[6])));
	    y = (int) Math.floor(Math.min(Math.min(d[1], d[3]),
					  Math.min(d[5], d[7])));
	    width = (int) Math.ceil(Math.max(Math.max(d[0], d[2]),
					     Math.max(d[4], d[6])));
	    height = (int) Math.ceil(Math.max(Math.max(d[1], d[3]),
					      Math.max(d[5], d[7])));
	} else {
	    x += transX;
	    y += transY;
	    width += x;
	    height += y;
	}
	if (!getCompClip().intersectsQuickCheckXYXY(x, y, width, height)) {
	    return false;
	}
	// REMIND: We could go one step further here and examine the
	// non-rectangular clip shape more closely if there is one.
	// Since the clip has already been rasterized, the performance
	// penalty of doing the scan is probably still within the bounds
	// of a good tradeoff between speed and quality of the answer.
	return true;
    }

    protected void validateCompClip() {
	int origClipState = clipState;
	if (usrClip == null) {
	    clipState = CLIP_DEVICE;
            clipRegion = devClip;
	} else if (usrClip instanceof Rectangle2D) {
	    clipState = CLIP_RECTANGULAR;
            if (usrClip instanceof Rectangle) {
                clipRegion = devClip.getIntersection((Rectangle)usrClip);
            } else {
                clipRegion = devClip.getIntersection(usrClip.getBounds());
            }
	} else {
            PathIterator cpi = usrClip.getPathIterator(null);
            int box[] = new int[4];
            ShapeSpanIterator sr = LoopPipe.getFillSSI(this);
            try {
                sr.setOutputArea(devClip);
                sr.appendPath(cpi);
                sr.getPathBox(box);
                Region r = Region.getInstance(box);
                r.appendSpans(sr);
                clipRegion = r;
                clipState = 
                    r.isRectangular() ? CLIP_RECTANGULAR : CLIP_SHAPE;
            } finally {
                sr.dispose();
            }
	}
	if (origClipState != clipState &&
	    (clipState == CLIP_SHAPE || origClipState == CLIP_SHAPE))
	{
	    invalidatePipe();
	}
    }

    static final int NON_RECTILINEAR_TRANSFORM_MASK =
	(AffineTransform.TYPE_GENERAL_TRANSFORM |
	 AffineTransform.TYPE_GENERAL_ROTATION);

    protected Shape transformShape(Shape s) {
	if (s == null) {
	    return null;
	}
	if (transformState > TRANSFORM_INT_TRANSLATE) {
	    return transformShape(transform, s);
	} else {
	    return transformShape(transX, transY, s);
	}
    }

    public Shape untransformShape(Shape s) {
	if (s == null) {
	    return null;
	}
	if (transformState > TRANSFORM_INT_TRANSLATE) {
	    try {
		return transformShape(transform.createInverse(), s);
	    } catch (NoninvertibleTransformException e) {
		return null;
	    }
	} else {
	    return transformShape(-transX, -transY, s);
	}
    }

    protected static Shape transformShape(int tx, int ty, Shape s) {
	if (s == null) {
	    return null;
	}

	if (s instanceof Rectangle) {
	    Rectangle r = s.getBounds();
	    r.translate(tx, ty);
	    return r;
	}
	if (s instanceof Rectangle2D) {
	    Rectangle2D rect = (Rectangle2D) s;
	    return new Rectangle2D.Double(rect.getX() + tx,
					  rect.getY() + ty,
					  rect.getWidth(),
					  rect.getHeight());
	}

	if (tx == 0 && ty == 0) {
	    return cloneShape(s);
	}

	AffineTransform mat = AffineTransform.getTranslateInstance(tx, ty);
	return mat.createTransformedShape(s);
    }

    protected static Shape transformShape(AffineTransform tx, Shape clip) {
        if (clip == null) {
            return null;
        }

	if (clip instanceof Rectangle2D &&
	    (tx.getType() & NON_RECTILINEAR_TRANSFORM_MASK) == 0)
	{
	    Rectangle2D rect = (Rectangle2D) clip;
	    double matrix[] = new double[4];
	    matrix[0] = rect.getX();
	    matrix[1] = rect.getY();
	    matrix[2] = matrix[0] + rect.getWidth();
	    matrix[3] = matrix[1] + rect.getHeight();
	    tx.transform(matrix, 0, matrix, 0, 2);
	    rect = new Rectangle2D.Float();
	    rect.setFrameFromDiagonal(matrix[0], matrix[1],
				      matrix[2], matrix[3]);
	    return rect;
	}

        if (tx.isIdentity()) {
	    return cloneShape(clip);
        }

	return tx.createTransformedShape(clip);
    }

    public void clipRect(int x, int y, int w, int h) {
	clip(new Rectangle(x, y, w, h));
    }

    public void setClip(int x, int y, int w, int h) {
	setClip(new Rectangle(x, y, w, h));
    }

    public Shape getClip() {
	return untransformShape(usrClip);
    }

    public void setClip(Shape sh) {
	usrClip = transformShape(sh);
	validateCompClip();
    }

    /**
     * Intersects the current clip with the specified Path and sets the
     * current clip to the resulting intersection. The clip is transformed
     * with the current transform in the Graphics2D state before being
     * intersected with the current clip. This method is used to make the
     * current clip smaller. To make the clip larger, use any setClip method.
     * @param p The Path to be intersected with the current clip.
     */
    public void clip(Shape s) {
	s = transformShape(s);
	if (usrClip != null) {
	    s = intersectShapes(usrClip, s, true, true);
	}
	usrClip = s;
	validateCompClip();
    }

    public void setPaintMode() {
        setComposite(AlphaComposite.SrcOver);
    }
    
    public void setXORMode(Color c) {
	if (c == null) {
	    throw new IllegalArgumentException("null XORColor");
	}
        setComposite(new XORComposite(c, surfaceData));
    }

    Blit lastCAblit;
    Composite lastCAcomp;

    public void copyArea(int x, int y, int w, int h, int dx, int dy) {
	try {
	    doCopyArea(x, y, w, h, dx, dy);
	} catch (InvalidPipeException e) {
	    revalidateAll();
	    try {
                doCopyArea(x, y, w, h, dx, dy);
	    } catch (InvalidPipeException e2) {
		// Still catching the exception; we are not yet ready to
		// validate the surfaceData correctly.  Fail for now and 
		// try again next time around.
	    }
	}
    }

    private void doCopyArea(int x, int y, int w, int h, int dx, int dy) {
	if (w <= 0 || h <= 0) {
	    return;
	}
	SurfaceData theData = surfaceData;
	if (theData.copyArea(this, x, y, w, h, dx, dy)) {
	    return;
	}
	if (transformState >= TRANSFORM_TRANSLATESCALE) {
	    throw new InternalError("transformed copyArea not implemented yet");
	}
	// REMIND: This method does not deal with missing data from the
	// source object (i.e. it does not send exposure events...)

	Region clip = getCompClip();
	
	Composite comp = composite;
	if (lastCAcomp != comp) {
	    SurfaceType dsttype = theData.getSurfaceType();
	    CompositeType comptype = imageComp;
	    if (CompositeType.SrcOverNoEa.equals(comptype) &&
		theData.getTransparency() == Transparency.OPAQUE)
	    {
		comptype = CompositeType.SrcNoEa;
	    }
	    lastCAblit = Blit.locate(dsttype, comptype, dsttype);
	    lastCAcomp = comp;
	}

	x += transX;
	y += transY;
	
	Blit ob = lastCAblit;
	if (dy == 0 && dx > 0 && dx < w) {
	    while (w > 0) {
		int partW = Math.min(w, dx);
		w -= par
