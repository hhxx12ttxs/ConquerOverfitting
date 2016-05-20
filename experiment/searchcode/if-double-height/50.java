/*
 * Copyright 2010 Brendan Kenny
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gwt.ns.graphics.canvas.client;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;

/**
 * Default wrapper for HTML5's CanvasRenderingContext2D object. Overridden 
 * (via deferred binding) only to support older, pre-standard implementations
 * of methods (e.g. mozDrawText before Firefox 3.5). IE support should be
 * (relatively) transparent due to excanvas. See that project for current
 * issues, bug workarounds, etc.<br><br>
 * 
 * Not directly instantiable. Create a Canvas Widget and retrieve from
 * {@link Canvas#getContext2d()}.
 * 
 *  @see <a href='http://www.whatwg.org/specs/web-apps/current-work/multipage/the-canvas-element.html#canvasrenderingcontext2d'>HTML5 Draft Standard for CanvasRenderingContext2D</a>
 *  @see <a href='https://developer.mozilla.org/en/HTML/Canvas'>Mozilla Developer Center Topic Page</a>
 *  @see <a href='http://code.google.com/p/explorercanvas/'>Explorercanvas project</a>
 */
public class CanvasContext2d {
	// TODO: someday do this with a JSO when generators can generate a JSO per
	//   UA. Overhead should be just one level of indirection
	//     (ctx.ctx.method() vs ctx.method()).
	// TODO: shadow support
	// TODO: canvasPattern
	// TODO: investigate drawFocusRing()...unclear right now as to purpose
	//   doesn't appear to be implemented in webkit or found in spec test
	// TODO: draw images
	// TODO: pixel manipulation
	protected JavaScriptObject ctx;
	protected Canvas parentCanvas;
	
	// TODO: enums done in this way based on Style. assuming the decision there
	// was thought through =) check out overhead, strings in output, etc
	/**
	 * Global Composite Operations.
	 */
	public enum Composite {
		SOURCE_ATOP {
			@Override
			public String getValue() {
				return COMPOSITE_SOURCE_ATOP;
			}
		}, SOURCE_IN {
			@Override
			public String getValue() {
				return COMPOSITE_SOURCE_IN;
			}
		}, SOURCE_OUT {
			@Override
			public String getValue() {
				return COMPOSITE_SOURCE_OUT;
			}
		}, SOURCE_OVER {
			@Override
			public String getValue() {
				return COMPOSITE_SOURCE_OVER;
			}
		}, DESTINATION_ATOP {
			@Override
			public String getValue() {
				return COMPOSITE_DESTINATION_ATOP;
			}
		}, DESTINATION_IN {
			@Override
			public String getValue() {
				return COMPOSITE_DESTINATION_IN;
			}
		}, DESTINATION_OUT {
			@Override
			public String getValue() {
				return COMPOSITE_DESTINATION_OUT;
			}
		}, DESTINATION_OVER {
			@Override
			public String getValue() {
				return COMPOSITE_DESTINATION_OVER;
			}
		}, LIGHTER {
			@Override
			public String getValue() {
				return COMPOSITE_LIGHTER;
			}
		}, COPY {
			@Override
			public String getValue() {
				return COMPOSITE_COPY;
			}
		}, XOR {
			@Override
			public String getValue() {
				return COMPOSITE_XOR;
			}
		};

		public abstract String getValue();
	}
	
	/**
	 * Line Cap Types
	 */
	public enum LineCap {
		BUTT {
			@Override
			public String getType() {
				return CAP_BUTT;
			}
		}, ROUND {
			@Override
			public String getType() {
				return CAP_ROUND;
			}
		}, SQUARE {
			@Override
			public String getType() {
				return CAP_SQUARE;
			}
		};

		public abstract String getType();
	}
	
	/**
	 * Line Join Types
	 */
	public enum LineJoin {
		ROUND {
			@Override
			public String getType() {
				return JOIN_ROUND;
			}
		}, BEVEL {
			@Override
			public String getType() {
				return JOIN_BEVEL;
			}
		}, MITER {
			@Override
			public String getType() {
				return JOIN_MITER;
			}
		};

		public abstract String getType();
	}
	
	/**
	 * Text Alignment Options
	 */
	public enum TextAlign {
		/**
		 * The text is aligned at the normal start of the line (left-aligned
		 * for left-to-right locales, right-aligned for right-to-left locales).
		 */
		START {
			@Override
			public String getValue() {
				return TEXT_ALIGN_START;
			}
		},
		/**
		 * The text is aligned at the normal end of the line (right-aligned for
		 * left-to-right locales, left-aligned for right-to-left locales).
		 */
		END {
			@Override
			public String getValue() {
				return TEXT_ALIGN_END;
			}
		},
		/**
		 * The text is left-aligned.
		 */
		LEFT {
			@Override
			public String getValue() {
				return TEXT_ALIGN_LEFT;
			}
		},
		/**
		 * The text is right-aligned.
		 */
		RIGHT {
			@Override
			public String getValue() {
				return TEXT_ALIGN_RIGHT;
			}
		},
		/**
		 * The text is centered.
		 */
		CENTER {
			@Override
			public String getValue() {
				return TEXT_ALIGN_CENTER;
			}
		};

		public abstract String getValue();
	}
	
	/**
	 * Text Baseline Options
	 */
	public enum TextBaseline {
		/**
		 * The text baseline is the top of the em square.
		 */
		TOP {
			@Override
			public String getValue() {
				return TEXT_BASELINE_TOP;
			}
		},
		/**
		 * The text baseline is the hanging baseline. 
		 */
		HANGING {
			@Override
			public String getValue() {
				return TEXT_BASELINE_HANGING;
			}
		},
		/**
		 * The text baseline is the middle of the em square.
		 */
		MIDDLE {
			@Override
			public String getValue() {
				return TEXT_BASELINE_MIDDLE;
			}
		},
		/**
		 * The text baseline is the normal alphabetic baseline.
		 */
		ALPHABETIC {
			@Override
			public String getValue() {
				return TEXT_BASELINE_ALPHABETIC;
			}
		},
		/**
		 * The text baseline is the ideographic baseline; this is the bottom of
		 * the body of the characters, if the main body of characters protrudes
		 * beneath the alphabetic baseline.
		 */
		IDEOGRAPHIC {
			@Override
			public String getValue() {
				return TEXT_BASELINE_IDEOGRAPHIC;
			}
		},
		/**
		 * The text baseline is the bottom of the bounding box. This differs
		 * from the ideographic baseline in that the ideographic baseline
		 * doesn't consider descenders.
		 */
		BOTTOM {
			@Override
			public String getValue() {
				return TEXT_BASELINE_BOTTOM;
			}
		};

		public abstract String getValue();
	}
	
	// default composite operations
	private static final String COMPOSITE_SOURCE_ATOP = "source-atop";
	private static final String COMPOSITE_SOURCE_IN = "source-in";
	private static final String COMPOSITE_SOURCE_OUT = "source-out";
	private static final String COMPOSITE_SOURCE_OVER = "source-over";
	private static final String COMPOSITE_DESTINATION_ATOP = "destination-atop";
	private static final String COMPOSITE_DESTINATION_IN = "destination-in";
	private static final String COMPOSITE_DESTINATION_OUT = "destination-out";
	private static final String COMPOSITE_DESTINATION_OVER = "destination-over";
	private static final String COMPOSITE_LIGHTER = "lighter";
	private static final String COMPOSITE_COPY = "copy";
	private static final String COMPOSITE_XOR = "xor";
	
	// default line caps
	private static final String CAP_BUTT = "butt";
	private static final String CAP_ROUND = "round";
	private static final String CAP_SQUARE = "square";
	
	// default line joins
	private static final String JOIN_ROUND = "round";
	private static final String JOIN_BEVEL = "bevel";
	private static final String JOIN_MITER = "miter";

	// default text align options
	private static final String TEXT_ALIGN_START = "start";
	private static final String TEXT_ALIGN_END = "end";
	private static final String TEXT_ALIGN_LEFT = "left";
	private static final String TEXT_ALIGN_RIGHT = "right";
	private static final String TEXT_ALIGN_CENTER = "center";
	
	// text baseline options
	private static final String TEXT_BASELINE_TOP = "top";
	private static final String TEXT_BASELINE_HANGING = "hanging";
	private static final String TEXT_BASELINE_MIDDLE = "middle";
	private static final String TEXT_BASELINE_ALPHABETIC = "alphabetic";
	private static final String TEXT_BASELINE_IDEOGRAPHIC = "ideographic";
	private static final String TEXT_BASELINE_BOTTOM = "bottom";
	
	/**
	 * Called by parent Canvas upon creation.
	 * 
	 * @param parent Canvas element owner of context.
	 * @param jsContext JavaScriptObject reference to context
	 */
	protected void init(Canvas parent, JavaScriptObject jsContext) {
		parentCanvas = parent;
		ctx = jsContext;
	}
	
	/**
	 * Returns a back-reference to the canvas that the context paints on.
	 * 
	 * @return reference to parent Canvas Widget.
	 */
	public Canvas getCanvas() {
		return parentCanvas;
	}
	
	/**
	 * Pushes the current Drawing state onto the Drawing state stack.
	 * 
	 * <p>Drawing states consist of:<br>
	 * The current transformation matrix.<br>
	 * The current clipping region.<br>
	 * The current values of the following attributes: strokeStyle, fillStyle,
	 * globalAlpha, lineWidth, lineCap, lineJoin, miterLimit, shadowOffsetX,
	 * shadowOffsetY, shadowBlur, shadowColor, globalCompositeOperation, font,
	 * textAlign, textBaseline.
	 * 
	 * @see <a href='http://www.whatwg.org/specs/web-apps/current-work/multipage/the-canvas-element.html#the-canvas-state'>The Canvas State</a>
	 */
	public native void save() /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).save();
	}-*/;
	
	/**
	 * Pops the top entry in the drawing state stack, and resets the drawing
	 * state it describes. If there is no saved state, the method does nothing.
	 */
	public native void restore() /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).restore();
	}-*/;
	

	/**
	 * Changes the transformation matrix to apply a scaling transformation with
	 * the given characteristics. The factors are multiples.
	 * 
	 * @param x The horizontal scale factor
	 * @param y The vertical scale factor
	 */
	public native void scale(double x, double y) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).scale(x,y);
	}-*/;
	
	/**
	 * Changes the transformation matrix to apply a rotation transformation
	 * with the given characteristics. The angle is in radians.
	 * 
	 * <p><strong>Note:</strong> due to definition of canvas coordinates
	 * (with positive y pointing down), positive values of angle rotate
	 * <em>clockwise</em>.</p>
	 * 
	 * @param angle A clockwise rotation angle expressed in radians.
	 */
	public native void rotate(double angle) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).rotate(angle);
	}-*/;
	
	/**
	 * Changes the transformation matrix to apply a translation transformation
	 * with the given characteristics. The arguments are in coordinate space
	 * units.
	 * 
	 * @param x The horizontal translation distance
	 * @param y The vertical translation distance
	 */
	public native void translate(double x, double y) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).translate(x, y);
	}-*/;
	
	/**
	 * Changes the transformation matrix by applying the supplied
	 * transformation to the current <em>local</em> coordinate system.
	 * Note that the indices are [column, row] not [row, column]...
	 * <pre> 
	 * m11   m21   dx
	 * m12   m22   dy
	 * 0      0     1 
	 * </pre>
	 */
	public native void transform(double m11, double m12, double m21, double m22, double dx, double dy) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).transform(m11, m12, m21, m22, dx, dy);
	}-*/;
	
	/**
	 * Resets the current transform to the identity matrix, then invokes
	 * transform() with the same arguments.
	 */
	public native void setTransform(double m11, double m12, double m21, double m22, double dx, double dy) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).setTransform(m11, m12, m21, m22, dx, dy);
	}-*/;
	
	/**
	 * Sets the globalAlpha, an alpha value that is applied to shapes and
	 * images before they are composited onto the canvas. The value must be in
	 * the range from 0.0 (fully transparent) to 1.0 (no additional
	 * transparency). If an attempt is made to set the attribute to a value
	 * outside this range, including Infinity and Not-a-Number (NaN) values,
	 * the attribute will retain its previous value. When the context is
	 * created, the globalAlpha attribute has the default value 1.0.
	 * 
	 * @param alpha The new globalAlpha. Must be in the range of [0,1]
	 */
	public native void setGlobalAlpha(double alpha) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).globalAlpha = alpha;
	}-*/;
	
	/**
	 * Returns the globalAlpha, an alpha value that is applied to shapes and
	 * images before they are composited onto the canvas.
	 * 
	 * @return The current globalAlpha
	 */
	public native double getGlobalAlpha() /*-{
		return (this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).globalAlpha;
	}-*/;
	
	/**
	 * Set the composite operation. Operations not enumerated (e.g.
	 * vendor-specific operations) may be specified directly with a String.
	 * Default operation is source-over.
	 * 
	 * @param globalCompositeOperation New compositing operation
	 * 
	 * @see <a href='http://www.whatwg.org/specs/web-apps/current-work/multipage/the-canvas-element.html#compositing'>Compositing Operations</a>
	 */
	public void setGlobalCompositeOperation(Composite globalCompositeOperation) {
		setGlobalCompositeOperation(globalCompositeOperation.getValue());
	}
	
	/**
	 * Set the composite operation. Operations not enumerated (e.g.
	 * vendor-specific operations) may be specified directly with a String
	 * with this method. Default operation is "source-over". Unrecognized
	 * Strings will leave this option unaffected.
	 * 
	 * @param globalCompositeOperation The new compositing operation
	 * 
	 * @see <a href='http://www.whatwg.org/specs/web-apps/current-work/multipage/the-canvas-element.html#compositing'>Compositing Operations</a>
	 */
	public native void setGlobalCompositeOperation(String globalCompositeOperation) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).globalCompositeOperation = globalCompositeOperation;
	}-*/;

	/**
	 * Returns the globalAlpha, an alpha value that is applied to shapes and
	 * images before they are composited onto the canvas.
	 * 
	 * @return The current globalCompositeOperation
	 */
	public native String getGlobalCompositeOperation() /*-{
		return (this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).globalCompositeOperation;
	}-*/;
	
	/**
	 * Sets the current style used for stroking shapes. Unrecognized Strings
	 * will leave the strokeStyle unaffected.
	 * 
	 * @param cssColor A valid CSS Color
	 */
	public native void setStrokeStyle(String cssColor) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).strokeStyle = cssColor;
	}-*/;
	
	/**
	 * Sets the current style used for stroking shapes to strokeColor.
	 * 
	 * @param strokeColor The new strokeStyle color
	 */
	public void setStrokeStyle(Color strokeColor) {
		setStrokeStyle(strokeColor.toString());
	}
	
	/**
	 * Sets the current style used for stroking shapes to a gradient.
	 * 
	 * @param gradient The CanvasGradient to set as the strokeStyle
	 */
	public native void setStrokeStyle(CanvasGradient gradient) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).strokeStyle = gradient;
	}-*/;
	
	// TODO: getStrokeStyle() has multiple possible return types
	//public native String getStrokeStyle() /*-{
	//	return (this.@gwt.ns.graphics.canvas.client.context2d.CanvasContext2d::ctx).strokeStyle;
	//}-*/;
	
	/**
	 * Sets the current style used for filling shapes. Unrecognized Strings
	 * will leave the fillStyle unaffected.
	 * 
	 * @param cssColor A valid CSS Color
	 */
	public native void setFillStyle(String cssColor) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).fillStyle = cssColor;
	}-*/;
	
	/**
	 * Sets the current style used for filling shapes to fillColor.
	 * 
	 * @param fillColor The new fillStyle color
	 */
	public void setFillStyle(Color fillColor) {
		setFillStyle(fillColor.toString());
	}
	
	/**
	 * Sets the current style used for filling shapes to a gradient.
	 * 
	 * @param gradient The CanvasGradient to set as the fillStyle
	 */
	public native void setFillStyle(CanvasGradient gradient) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).fillStyle = gradient;
	}-*/;
	
	// TODO: getFillStyle() has multiple possible return types
	//public native String getFillStyle() /*-{
	//	return (this.@gwt.ns.graphics.canvas.client.context2d.CanvasContext2d::ctx).fillStyle;
	//}-*/;
	
	/**
	 * Sets the width of stroked lines, in the units defined by the intrinsic
	 * size of the Canvas. Non-positive and non-numeric values will be ignored
	 * and lineWidth will remain unchanged. Default width is 1.0.
	 * 
	 * @param width The new line width
	 */
	public native void setLineWidth(double width) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).lineWidth = width;
	}-*/;
	
	/**
	 * Returns the lineWidth, the width of stroked lines.
	 * 
	 * @return The current line width
	 */
	public native double getLineWidth() /*-{
		return (this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).lineWidth;
	}-*/;
	
	/**
	 * If the current lineJoin is set to "miter", the miter limit is the
	 * maximum allowed ratio of the miter length to half the line width. If the
	 * ratio exceeds this limit the join will not be rendered. Non-positive and
	 * non-numeric values will be ignored and the miter limit will remain
	 * unchanged. The default miter limit is 10.0.
	 * 
	 * @param limit The new miter limit
	 * 
	 * @see <a href='http://www.whatwg.org/specs/web-apps/current-work/multipage/the-canvas-element.html#line-styles'>Canvas Line Styles</a>
	 */
	public native void setMiterLimit(double limit) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).miterLimit = limit;
	}-*/;
	
	/**
	 * @return The current miter limit
	 */
	public native double getMiterLimit() /*-{
		return (this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).miterLimit;
	}-*/;
	
	/**
	 * Set the type of ending added to rendered lines. All recognized types
	 * are enumerated in {@link LineCap}.
	 * The initial value is {@link LineCap#BUTT} or "butt".
	 * 
	 * @param cap New line cap type
	 */
	public void setLineCap(LineCap cap) {
		setLineCap(cap.getType());
	}
	
	/**
	 * Set the type of ending added to rendered lines. Unrecognized types will
	 * have no effect on the current line cap type. The three valid values are
	 * "butt", "round", and "square". The default is "butt".
	 * 
	 * @param cap New line cap type
	 */
	public native void setLineCap(String cap) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).lineCap = cap;
	}-*/;

	/**
	 * Returns a String representation of the current line cap.
	 * 
	 * @return The current line cap
	 */
	public native String getLineCap() /*-{
		return (this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).lineCap;
	}-*/;
	
	/**
	 * Set the type of corners that will be rendered where two lines meet. All
	 * recognized types are enumerated in {@link LineJoin}.
	 * The initial value is {@link LineJoin#MITER} or "miter".
	 * 
	 * @param join New line join type
	 */
	public void setLineJoin(LineJoin join) {
		setLineJoin(join.getType());
	}
	
	/**
	 * Set the type of corners that will be rendered where two lines meet.
	 * Unrecognized types will have no effect on the current line join type.
	 * The three valid values are "bevel", "round", and "miter". The default is
	 * "miter".
	 * 
	 * @param join New line join type
	 */
	public native void setLineJoin(String join) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).lineJoin = join;
	}-*/;

	/**
	 * Returns a String representation of the current line join.
	 * 
	 * @return The current line join
	 */
	public native String getLineJoin() /*-{
		return (this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).lineJoin;
	}-*/;
	
	/**
	 * Clears the pixels in the specified rectangle that also intersect the
	 * current clipping region to fully transparent and colorless, erasing any
	 * previous image. If either height or width are zero, this method has no
	 * effect.
	 * 
	 * @param x The x coordinate of the top left corner of the rectangle
	 * @param y The y coordinate of the top left corner of the rectangle
	 * @param width The width of the rectangle
	 * @param height The height of the rectangle
	 */
	public native void clearRect(double x, double y, double width, double height) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).clearRect(x, y, width, height);
	}-*/;
	
	/**
	 * Paints the specified rectangular area using the current fillStyle. If
	 * either height or width are zero, this method has no effect.
	 * 
	 * @param x The x coordinate of the top left corner of the rectangle
	 * @param y The y coordinate of the top left corner of the rectangle
	 * @param width The width of the rectangle
	 * @param height The height of the rectangle
	 */
	public native void fillRect(double x, double y, double width, double height) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).fillRect(x, y, width, height);
	}-*/;
	
	/**
	 * Strokes the specified rectangle's path using the current strokeStyle,
	 * lineWidth, lineJoin, and (if appropriate) miterLimit attributes. If both
	 * height and width are zero this method has no effect. If only one of the
	 * two is zero, then the method will draw a line instead .
	 * 
	 * @param x The x coordinate of the top left corner of the rectangle
	 * @param y The y coordinate of the top left corner of the rectangle
	 * @param width The width of the rectangle
	 * @param height The height of the rectangle
	 */
	public native void strokeRect(double x, double y, double width, double height) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).strokeRect(x, y, width, height);
	}-*/;
	
	/**
	 * Resets the current path and empties the list of subpaths so that the
	 * context once again has zero subpaths.
	 */
	public native void beginPath() /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).beginPath();
	}-*/;
	
	/**
	 * Marks the current subpath as closed (thus adding a line from the last
	 * point the first point of that subpat, "closing" the shape), and starts a
	 * new subpath with a point the same as the start and end of the newly
	 * closed subpath. If the shape has already been closed or there's only one
	 * point in the list, this function does nothing.
	 */
	public native void closePath() /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).closePath();
	}-*/;
	
	/**
	 * Creates a new subpath with the specified point as its first (and only)
	 * point. The point will be transformed according to the current
	 * transformation matrix.
	 * 
	 * @param x The x coordinate of the new point
	 * @param y The y coordinate of the new point
	 */
	public native void moveTo(double x, double y) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).moveTo(x, y);
	}-*/;
	
	/**
	 * Adds the given point to the current subpath, connected to the previous
	 * one by a straight line. The point will be transformed according to the
	 * current transformation matrix.
	 * 
	 * @param x The x coordinate of the new point
	 * @param y The y coordinate of the new point
	 */
	public native void lineTo(double x, double y) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).lineTo(x, y);
	}-*/;
	
	/**
	 * Adds the given point (x,y) to the current path, connected to the
	 * previous one by a quadratic Bézier curve with the given control point
	 * (cpx, cpy). The points will be transformed according to the
	 * current transformation matrix.
	 * 
	 * @param cpx The x coordinate of the control point
	 * @param cpy The y coordinate of the control point
	 * @param x The x coordinate of the new point
	 * @param y The y coordinate of the new point
	 */
	public native void quadraticCurveTo(double cpx, double cpy, double x, double y) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).quadraticCurveTo(cpx, cpy, x, y);
	}-*/;
	
	/**
	 * Adds the given point (x, y) to the current path, connected to the
	 * previous one by a cubic Bézier curve with the given control points
	 * (cp1x, cp1y) and (cp2x, cp2y). The points will be transformed according
	 * to the current transformation matrix.
	 * 
	 * @param cp1x The x coordinate of the first control point
	 * @param cp1y The y coordinate of the first control point
	 * @param cp2x The x coordinate of the second control point
	 * @param cp2y The y coordinate of the second control point
	 * @param x The x coordinate of the new point
	 * @param y The y coordinate of the new point
	 */
	public native void bezierCurveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y);
	}-*/;
	
	/**
	 * Adds the first given point (x1, y1) to the current path, connected to
	 * the previous one by a straight line, then adds the second point (x2, y2)
	 * to the current path, connected to the previous one by the arc
	 * defined by the points and the radius. The points will be transformed
	 * according to the current transformation matrix. A negative radius will
	 * throw an exception.
	 * 
	 * @param x1 The x coordinate of the first point
	 * @param y1 The y coordinate of the first point
	 * @param x2 The x coordinate of the second point
	 * @param y2 The y coordinate of the second point
	 * @param radius The radius of the arc
	 * 
	 * @see <a href='http://www.whatwg.org/specs/web-apps/current-work/multipage/the-canvas-element.html#dom-context-2d-arcto'>Definition of the drawn arc</a>
	 */
	public native void arcTo(double x1, double y1, double x2, double y2, double radius) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).arcTo(x1, y1, x2, y2, radius);
	}-*/;
	
	/**
	 * Creates a new subpath containing just the four points (x, y),
	 * (x+width, y), (x+width, y+height), (x, y+height), with those four points
	 * connected by straight lines, and marks the subpath as closed. Then
	 * creates a new subpath with the point (x, y) as the only point in the
	 * subpath. The four points will be transformed according to the current
	 * transformation matrix.
	 * 
	 * @param x The x coordinate of the top left corner of the rectangle
	 * @param y The y coordinate of the top left corner of the rectangle
	 * @param width The width of the rectangle
	 * @param height The height of the rectangle
	 */
	public native void rect(double x, double y, double width, double height) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).rect(x, y, width, height);
	}-*/;
	
	/**
	 * Draws an arc. If the context has any subpaths, a straight line is drawn
	 * from the last point in the subpath to the start point of the arc. An arc
	 * is drawn between the start point of the arc and the end point of the
	 * arc, and the start and end points of the arc are added to the subpath.
	 * 
	 * <p>The arc has its origin at (x, y) and has radius radius. The points at
	 * startAngle and endAngle along this circle's circumference, measured in
	 * radians clockwise from the positive x-axis, are the start and end points
	 * respectively. If the anticlockwise argument is false and
	 * endAngle-startAngle is equal to or greater than 2*Pi, or, if the
	 * anticlockwise argument is true and startAngle-endAngle is equal to or
	 * greater than 2*Pi, then the arc is the whole circumference of this circle.
	 * </p>
	 * 
	 * The points will be transformed according to the current transformation
	 * matrix. A negative radius will throw an exception.
	 * 
	 * @param x The x coordinate of the center of the arc
	 * @param y The y coordinate of the center of the arc
	 * @param radius The radius of the arc
	 * @param startAngle The angle defining the start point
	 * @param endAngle The angle defining the end point
	 * @param anticlockwise designates the direction of the arc
	 */
	public native void arc(double x, double y, double radius, double startAngle, double endAngle, boolean anticlockwise) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).arc(x, y, radius, startAngle, endAngle, anticlockwise);
	}-*/;
	
	/**
	 *  Fills all the subpaths of the current path, using the current
	 *  fillStyle. Open subpaths are implicitly closed when being filled
	 *  (without affecting the actual subpaths). Paths are filled without
	 *  affecting the current path, and are subject to shadow effects, global
	 *  alpha, the clipping region, and global composition operators. 
	 */
	public native void fill() /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).fill();
	}-*/;
	
	/**
	 *  Calculates the strokes of all the subpaths of the current path, using
	 *  the lineWidth, lineCap, lineJoin, and (if appropriate) miterLimit
	 *  attributes, and then fills the combined stroke area using the current
	 *  strokeStyle. Overlapping parts of the paths in one stroke operation are
	 *  treated as if their union was what was painted. Paths are stroked
	 *  without affecting the current path, and are subject to shadow effects,
	 *  global alpha, the clipping region, and global composition operators.
	 */
	public native void stroke() /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).stroke();
	}-*/;
	
	/**
	 * Creates a new clipping region by calculating the intersection of the
	 * current clipping region and the area described by the current path. Open
	 * subpaths are implicitly closed when computing the clipping region,
	 * without affecting the actual subpaths. The new clipping region replaces
	 * the current clipping region.
	 * 
	 * <p>When the context is first initialized, the clipping region is the
	 * entire Canvas, i.e. the rectangle with the top left corner at (0,0) and
	 * the width and height of the Canvas.<p>
	 */
	public native void clip() /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).clip();
	}-*/;
	

	/**
	 * Returns true if the given point (x, y), <em>unaffected by the current
	 * transformation</em>, is within the current path.
	 * 
	 * @param x The x coordinate of the point
	 * @param y The y coordinate of the point
	 * @return True if point is within path.
	 */
	public native boolean isPointInPath(double x, double y) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).isPointInPath(x, y);
	}-*/;
	
	/**
	 * Sets the font used for drawn text. The syntax is the same as for the CSS
	 * 'font' property; values that cannot be parsed as CSS font values are
	 * ignored. Relative keywords and lengths are computed relative to the font
	 * of the canvas element. This includes fonts embedded using
	 * .@font-face, if they are loaded. If a font is referenced
	 * before it is fully loaded, then it must be treated as if it was an
	 * unknown font, falling back to another as described by the relevant CSS
	 * specifications.
	 * 
	 * <p>If the new value is syntactically incorrect then it will
	 * be ignored and the font setting will be unchanged. The default value is
	 * "10px sans-serif".</p>
	 * 
	 * @param font String specifying new font
	 * 
	 * @see <a href='http://www.whatwg.org/specs/web-apps/current-work/multipage/the-canvas-element.html#dom-context-2d-font'>Font Spec</a>
	 * @see <a href='http://www.w3.org/TR/css3-fonts/#shorthand-font-property-the-font-propert'>CSS font property</a>
	 */
	public native void setFont(String font) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).font = font;
	}-*/;
	
	/**
	 * Convenience method to set font piecemeal. Default is
	 * setFont(FontStyle.NORMAL, FontWeight.NORMAL, 10, Unit.PX, "sans-serif");
	 * 
	 * @param style Font style
	 * @param weight Font weight
	 * @param size Font size, in sizeUnit units
	 * @param sizeUnit CSS unit of size
	 * @param family CSS font-family stack, comma separated
	 * 
	 * @see {@link CanvasContext2d#setFont(String) setFont(String)}
	 */
	public void setFont(FontStyle style, FontWeight weight, double size, Unit sizeUnit, String family) {
		// keep stateless, for now.
		String font = style.getCssName() + " " + weight.getCssName() + " "
				+ size + sizeUnit.getType() + " " + family;
		setFont(font);
	}
	
	/**
	 * Returns the serialized form of the context's current font.
	 * 
	 * @return The current font
	 */
	public native String getFont() /*-{
		return (this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).font;
	}-*/;
	
	/**
	 * Set the type of text alignment. All
	 * recognized types are enumerated in {@link TextAlign}.
	 * The default value is {@link TextAlign#START} or "start".
	 * 
	 * @param alignment The new text alignment setting
	 */
	public void setTextAlign(TextAlign alignment) {
		setTextAlign(alignment.getValue());
	}
	
	/**
	 * Set the type of text alignment. Unrecognized types will have no effect
	 * on the current text alignment setting. The five valid values are
	 * "start", "end", "left", "right", and "center". The default is
	 * "start".
	 * 
	 * @param alignment The new text alignment setting
	 */
	public native void setTextAlign(String alignment) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).textAlign = alignment;
	}-*/;

	/**
	 * Returns a String representation of the current text alignment setting.
	 * 
	 * @return The current text alignment setting
	 */
	public native String getTextAlign() /*-{
		return (this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).textAlign;
	}-*/;
	
	/**
	 * Set the type of text baseline alignment. All
	 * recognized types are enumerated in {@link TextBaseline}.
	 * The default value is {@link TextBaseline#ALPHABETIC} or "alphabetic".
	 * 
	 * @param baseline The new text baseline alignment setting
	 * 
	 * @see <a href='http://www.whatwg.org/specs/web-apps/current-work/multipage/the-canvas-element.html#dom-context-2d-textbaseline'>Baseline Types</a>
	 */
	public void setTextBaseline(TextAlign baseline) {
		setTextBaseline(baseline.getValue());
	}
	
	/**
	 * Set the type of text baseline alignment. Unrecognized types will have no
	 * effect on the current text baseline alignment setting. The six valid
	 * values are "top", "hanging", "middle", "alphabetic", "ideographic", or
	 * "bottom". The default is "alphabetic".
	 * 
	 * @param baseline The new text baseline alignment setting
	 * 
	 * @see <a href='http://www.whatwg.org/specs/web-apps/current-work/multipage/the-canvas-element.html#dom-context-2d-textbaseline'>Baseline Types</a>
	 */
	public native void setTextBaseline(String baseline) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).textBaseline = baseline;
	}-*/;

	/**
	 * Returns a String representation of the current text baseline alignment
	 * setting.
	 * 
	 * @return The current text baseline alignment setting
	 */
	public native String getTextBaseline() /*-{
		return (this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).textBaseline;
	}-*/;
	
	/**
	 * Fills the given text at the given position using the current font,
	 * textAlign, and textBaseline values. The text will be <em>scaled</em> to
	 * fit maxWidth if necessary (either by substituting a more condensed font,
	 * scaling horizontally, or using a smaller font).
	 * 
	 * @param text The text to fill
	 * @param x The x coordinate at which to write the text
	 * @param y The y coordinate at which to write the text
	 * @param maxWidth The maximum width to draw, in CSS pixels
	 * 
	 * @see <a href='http://www.whatwg.org/specs/web-apps/current-work/multipage/the-canvas-element.html#dom-context-2d-filltext'>Fill text algorithm</a>
	 */
	public native void fillText(String text, double x, double y, double maxWidth) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).fillText(text, x, y, maxWidth);
	}-*/;
	
	/**
	 * Fills the given text at the given position using the current font,
	 * textAlign, and textBaseline values.
	 * 
	 * @param text The text to fill
	 * @param x The x coordinate at which to write the text
	 * @param y The y coordinate at which to write the text
	 * 
	 * @see <a href='http://www.whatwg.org/specs/web-apps/current-work/multipage/the-canvas-element.html#dom-context-2d-filltext'>Fill text algorithm</a>
	 */
	public native void fillText(String text, double x, double y) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).fillText(text, x, y);
	}-*/;
	
	/**
	 * Strokes the given text at the given position using the current font,
	 * textAlign, and textBaseline values. The text will be <em>scaled</em> to
	 * fit maxWidth if necessary (either by substituting a more condensed font,
	 * scaling horizontally, or using a smaller font).
	 * 
	 * @param text The text to stroke
	 * @param x The x coordinate at which to write the text
	 * @param y The y coordinate at which to write the text
	 * @param maxWidth The maximum width to draw, in CSS pixels
	 * 
	 * @see <a href='http://www.whatwg.org/specs/web-apps/current-work/multipage/the-canvas-element.html#dom-context-2d-stroketext'>Stroke text algorithm</a>
	 */
	public native void strokeText(String text, double x, double y, double maxWidth) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).strokeText(text, x, y, maxWidth);
	}-*/;
	
	/**
	 * Strokes the given text at the given position using the current font,
	 * textAlign, and textBaseline values.
	 * 
	 * @param text The text to stroke
	 * @param x The x coordinate at which to write the text
	 * @param y The y coordinate at which to write the text
	 * 
	 * @see <a href='http://www.whatwg.org/specs/web-apps/current-work/multipage/the-canvas-element.html#dom-context-2d-stroketext'>Stroke text algorithm</a>
	 */
	public native void strokeText(String text, double x, double y) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).strokeText(text, x, y);
	}-*/;
	
	/**
	 * Returns the width, in pixels, that the specified text will be when drawn
	 * in the current text style.
	 * 
	 * @param text The text to measure
	 */
	public native double measureTextWidth(String text) /*-{
		// avoid the intermediate object for now as no other aspect is measured
		// but function call is kept specific for future changes
		var metrics = (this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).measureText(text);
		return metrics.width;
	}-*/;
	
	/**
	 * Creates and returns a linear gradient that paints along the line given
	 * by the coordinates represented by the arguments. The returned gradient
	 * is transparent and colorless until color stops are added. The points
	 * will be transformed according to the current transformation matrix when
	 * rendering.
	 * 
	 * @param x0 The x coordinate of the start point of the gradient
	 * @param y0 The y coordinate of the start point of the gradient
	 * @param x1 The x coordinate of the end point of the gradient
	 * @param y1 The y coordinate of the end point of the gradient
	 * 
	 * @return A linear gradient
	 */
	public native CanvasGradient createLinearGradient(double x0, double y0, double x1, double y1) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).createLinearGradient(x0, y0, x1, y1);
	}-*/;
	
	/**
	 * Creates and returns a radial gradient that paints along the cone given
	 * by the circles represented by the arguments. The first three arguments
	 * represent the start circle with origin (x0, y0) and radius r0, and the
	 * last three represent the end circle with origin (x1, y1) and radius r1.
	 * The returned gradient is transparent and colorless until color stops are
	 * added. Negative radii will throw a Javascript exception. The points will
	 * be transformed according to the current transformation matrix when
	 * rendering.
	 * 
	 * @param x0 The origin x coordinate of the gradient's start circle
	 * @param y0 The origin y coordinate of the gradient's start circle
	 * @param r0 The radius of the start circle of the gradient
	 * @param x1 The origin x coordinate of the gradient's end circle
	 * @param y1 The origin y coordinate of the gradient's end circle
	 * @param r1 The radius of the end circle of the gradient
	 * 
	 * @return A radial gradient
	 */
	public native CanvasGradient createRadialGradient(double x0, double y0, double r0, double x1, double y1, double r1) /*-{
		(this.@gwt.ns.graphics.canvas.client.CanvasContext2d::ctx).createRadialGradient(x0, y0, r0, x1, y1, r1);
	}-*/;
}

