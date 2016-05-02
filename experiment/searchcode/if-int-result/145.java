/* 
 * Copyright 2007-2010 John C. Gunther
 * 
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 * 
 */
package com.googlecode.gchart.client;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasDoubleClickHandlers;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.HasMouseMoveHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.HasMouseUpHandlers;
import com.google.gwt.event.dom.client.HasMouseWheelHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.Date;
import com.google.gwt.core.client.GWT;

/**
 * A GChart can represent and display a line chart, a bar chart, a pie chart, an
 * area chart, or a chart that contains arbitrary combinations of line, bar,
 * pie, and/or area based curves.
 * 
 * <p>
 * For detailed examples, with screen shots, visit the <a
 * href="package-summary.html#ChartGallery">Chart Gallery</a>.
 * 
 * <p>
 * For detailed instructions on how to integrate Client-side GChart into your
 * GWT application, see <a href="package-summary.html#InstallingGChart">
 * Installing Client-side GChart</a>.
 * 
 * <p>
 * <b>CSS Style Rule</b>
 * <ul>
 * .gchart-GChart { the GChart's primary top-level styles }
 * </ul>
 * 
 * It is sometimes more natural to consider certain CSS attributes as properties
 * of a GChart Java object. So, GChart supports "CSS convenience methods" that
 * let you (optionally) use Java to specify GChart CSS attributes such as
 * <tt>border-color</tt> and <tt>background-color</tt>. See {@link #USE_CSS
 * USE_CSS} for a detailed description of these CSS convenience methods--which
 * won't interfere with standard CSS-based specifications if you never invoke
 * them.
 * 
 */

public class GChart extends Composite implements HasClickHandlers,
    HasDoubleClickHandlers, HasMouseDownHandlers, HasMouseMoveHandlers,
    HasMouseOutHandlers, HasMouseOverHandlers, HasMouseUpHandlers,
    HasMouseWheelHandlers {

  /**
   * Defines the location of a data point's annotation or hover annotation
   * (which can be defined by either plain text, HTML, or a widget) relative to
   * the location of that point's symbol. The "Field Summary" section below
   * lists all available annotation locations.
   * <p>
   * 
   * The default annotation location is {@link AnnotationLocation#SOUTH SOUTH}
   * for annotations and is symbol-type-dependent for hover annotations. See the
   * <tt>setHoverLocation</tt> method for list of these defaults.
   * 
   * <p>
   * 
   * You can further adjust the position of a point's annotation (or hover
   * annotation) by specifying non-zero positional shifts via the
   * <tt>setAnnotationXShift</tt> and <tt>setAnnotationYShift</tt> (or via the
   * <tt>setHoverXShift</tt>, <tt>setHoverYShift</tt>), and
   * <tt>setHoverAnnotationSymbolType</tt> methods for hover annotations).
   * <p>
   * 
   * @see Curve.Point#setAnnotationLocation Point.setAnnotationLocation
   * @see Curve.Point#setAnnotationXShift Point.setAnnotationXShift
   * @see Curve.Point#setAnnotationYShift Point.setAnnotationYShift
   * @see Symbol#setHoverLocation Symbol.setHoverLocation
   * @see Symbol#setHoverAnnotationSymbolType
   *      Symbol.setHoverAnnotationSymbolType
   * @see Symbol#setHoverXShift Symbol.setHoverXShift
   * @see Symbol#setHoverYShift Symbol.setHoverYShift
   * @see #DEFAULT_HOVER_LOCATION DEFAULT_HOVER_LOCATION
   * 
   */
  public static final class AnnotationLocation {
    // non-public tagging-only locations used by ANCHOR_MOUSE_* symbol types
    static final AnnotationLocation AT_THE_MOUSE =
        new AnnotationLocation(0, 0);
    static final AnnotationLocation AT_THE_MOUSE_SNAP_TO_X =
        new AnnotationLocation(0, 0);
    static final AnnotationLocation AT_THE_MOUSE_SNAP_TO_Y =
        new AnnotationLocation(0, 0);
    /**
     * Specifies that a point's annotation (label) should be positioned so as to
     * be centered on the symbol used to represent the point.
     * 
     * @see Curve.Point#setAnnotationLocation setAnnotationLocation
     */
    public static final AnnotationLocation CENTER =
        new AnnotationLocation(0, 0);
    private static final AnnotationLocation north =
        new AnnotationLocation(0, -1);
    private static final AnnotationLocation west =
        new AnnotationLocation(-1, 0);
    private static final AnnotationLocation south =
        new AnnotationLocation(0, 1);

    /**
     * Specifies that a point's annotation (label) should be placed just above,
     * and centered horizontally on, vertical bars that grow down from a
     * horizontal baseline, and just below, and centered horizontally on,
     * vertical bars that grow up from a horizontal baseline.
     * 
     * <p>
     * 
     * This another name for <tt>AnnotationLocation.NORTH</tt>. Its sole purpose
     * is to clarify/document the behavior of this location type when used in
     * conjunction with curves that employ <tt>VBAR_BASELINE_*</tt> symbol
     * types.
     * 
     * @see Curve.Point#setAnnotationLocation setAnnotationLocation
     * @see SymbolType#VBAR_BASELINE_CENTER SymbolType.VBAR_BASELINE_CENTER
     * 
     */
    public static final AnnotationLocation CLOSEST_TO_HORIZONTAL_BASELINE = north;

    /**
     * Specifies that a point's annotation (label) should be placed just to the
     * right of, and centered vertically on, horizontal bars that grow left from
     * a vertical baseline, and just to the left of, and centered vertically on,
     * horizontal bars that grow right from a vertical baseline.
     * 
     * <p>
     * 
     * This another name for <tt>AnnotationLocation.WEST</tt>. Its sole purpose
     * is to clarify/document the behavior of this location type when used in
     * conjunction with curves that employ the <tt>HBAR_BASELINE_*</tt> symbol
     * types.
     * 
     * @see Curve.Point#setAnnotationLocation setAnnotationLocation
     * @see SymbolType#HBAR_BASELINE_CENTER SymbolType.HBAR_BASELINE_CENTER
     * 
     */

    public static final AnnotationLocation CLOSEST_TO_VERTICAL_BASELINE = west;

    /**
     * Specifies that a point's annotation (label) should be positioned just to
     * the right of, and vertically centered on, the symbol used to represent
     * the point.
     * 
     * @see Curve.Point#setAnnotationLocation
     */
    public static final AnnotationLocation EAST =
        new AnnotationLocation(1, 0);

    /**
     * Specifies that a point's annotation (label) should be placed just below,
     * and centered horizontally on, vertical bars that grow down from a
     * horizontal baseline, and just above, and centered horizontally on,
     * vertical bars that grow up from a horizontal baseline.
     * 
     * <p>
     * 
     * This another name for <tt>AnnotationLocation.SOUTH</tt>. Its sole purpose
     * is to clarify/document the behavior of this location type when used in
     * conjunction with curves that employ <tt>VBAR_BASELINE_*</tt> symbol
     * types.
     * 
     * @see Curve.Point#setAnnotationLocation setAnnotationLocation
     * @see SymbolType#VBAR_BASELINE_CENTER SymbolType.VBAR_BASELINE_CENTER
     * 
     */
    public static final AnnotationLocation FARTHEST_FROM_HORIZONTAL_BASELINE = south;

    /**
     * Specifies that a point's annotation (label) should be placed just to the
     * left of, and centered vertically on, horizontal bars that grow left from
     * a vertical baseline, and just to the right of, and centered vertically
     * on, horizontal bars that grow right from a vertical baseline.
     * 
     * <p>
     * 
     * This another name for <tt>AnnotationLocation.EAST</tt>. Its sole purpose
     * is to clarify/document the behavior of this location type when used in
     * conjunction with curves that employ the <tt>HBAR_BASELINE_*</tt> family
     * of symbol types.
     * 
     * @see Curve.Point#setAnnotationLocation setAnnotationLocation
     * @see SymbolType#HBAR_BASELINE_CENTER SymbolType.HBAR_BASELINE_CENTER
     * 
     */
    public static final AnnotationLocation FARTHEST_FROM_VERTICAL_BASELINE = EAST;

    /**
     * Specifies that a point's annotation (label) should be positioned just
     * inside, and centered on, the arc side of a pie slice.
     * <p>
     * 
     * You can move a pie slice's annotation a specific number of pixels
     * radially away from (or towards) the pie center by passing a positive (or
     * negative) argument to the associated <tt>Point</tt>'s
     * <tt>setAnnotationXShift</tt> method.
     * 
     * <p>
     * This is pie-friendly synonym for, and when used with non-pie symbol types
     * will behave exactly the same as, <tt>AnnotationLocation.NORTH</tt>
     * 
     * @see #OUTSIDE_PIE_ARC OUTSIDE_PIE_ARC
     * @see #ON_PIE_ARC ON_PIE_ARC
     * @see Curve.Point#setAnnotationLocation setAnnotationLocation
     * @see AnnotationLocation#NORTH NORTH
     */
    public static final AnnotationLocation INSIDE_PIE_ARC = north;

    /**
     * Specifies that a point's annotation (label) should be positioned just
     * above, and horizontally centered on, the symbol used to represent the
     * point.
     * 
     * @see Curve.Point#setAnnotationLocation setAnnotationLocation
     */
    public static final AnnotationLocation NORTH = north;

    /**
     * Specifies that a point's annotation (label) should be positioned just to
     * the right of and above, the symbol used to represent the point.
     * 
     * @see Curve.Point#setAnnotationLocation
     */
    public static final AnnotationLocation NORTHEAST =
        new AnnotationLocation(1, -1);

    /**
     * Specifies that a point's annotation (label) should be positioned just to
     * the left of and above, the symbol used to represent the point.
     * 
     * @see Curve.Point#setAnnotationLocation
     */
    public static final AnnotationLocation NORTHWEST =
        new AnnotationLocation(-1, -1);

    /**
     * Specifies that a point's annotation (label) should be centered on the
     * center-point of the arc side of a pie slice.
     * <p>
     * 
     * You can move a pie slice's annotation a specific number of pixels
     * radially away from (or towards) the pie center by passing a positive (or
     * negative) argument to the associated <tt>Point</tt>'s
     * <tt>setAnnotationXShift</tt> method.
     * 
     * <p>
     * This is pie-friendly synonym for, and when used with non-pie symbol types
     * will behave exactly the same as, <tt>AnnotationLocation.CENTER</tt>
     * 
     * @see #OUTSIDE_PIE_ARC OUTSIDE_PIE_ARC
     * @see #INSIDE_PIE_ARC INSIDE_PIE_ARC
     * @see Curve.Point#setAnnotationLocation setAnnotationLocation
     * @see AnnotationLocation#CENTER CENTER
     * 
     */
    public static final AnnotationLocation ON_PIE_ARC = CENTER;

    /**
     * Specifies that a point's annotation (label) should be positioned just
     * outside, and centered on, the arc side of a pie slice.
     * <p>
     * 
     * You can move a pie slice's annotation a specific number of pixels
     * radially away from (or towards) the pie center by passing a positive (or
     * negative) argument to the associated <tt>Point</tt>'s
     * <tt>setAnnotationXShift</tt> method.
     * 
     * <p>
     * This is pie-friendly synonym for, and when used with non-pie symbol types
     * will behave exactly the same as, <tt>AnnotationLocation.SOUTH</tt>
     * 
     * @see #INSIDE_PIE_ARC INSIDE_PIE_ARC
     * @see #ON_PIE_ARC ON_PIE_ARC
     * @see Curve.Point#setAnnotationLocation setAnnotationLocation
     * @see Curve.Point#setAnnotationXShift setAnnotationXShift
     * @see AnnotationLocation#SOUTH SOUTH
     */
    public static final AnnotationLocation OUTSIDE_PIE_ARC = south;

    /**
     * Specifies that a point's annotation (label) should be positioned just
     * below, and horizontally centered on, the symbol used to represent the
     * point.
     * 
     * @see Curve.Point#setAnnotationLocation setAnnotationLocation
     */
    public static final AnnotationLocation SOUTH = south;

    /**
     * Specifies that a point's annotation (label) should be positioned just to
     * the right of and below, the symbol used to represent the point.
     * 
     * @see Curve.Point#setAnnotationLocation setAnnotationLocation
     */
    public static final AnnotationLocation SOUTHEAST =
        new AnnotationLocation(1, 1);
    /**
     * Specifies that a point's annotation (label) should be positioned just to
     * the left of and below, the symbol used to represent the point.
     * 
     * @see Curve.Point#setAnnotationLocation setAnnotationLocation
     */
    public static final AnnotationLocation SOUTHWEST =
        new AnnotationLocation(-1, 1);

    /**
     * Specifies that a point's annotation (label) should be positioned just to
     * the left of, and vertically centered on, the symbol used to represent the
     * point.
     * 
     * @see Curve.Point#setAnnotationLocation setAnnotationLocation
     */
    public static final AnnotationLocation WEST = west;

    /*
     * These multiply the width and height of the annotation and the
     * symbol it is attached to in order to define the center of the
     * annotation (c.f. <tt>getUpperLeftX</tt> and
     * <tt>getUpperLeftY</tt> below), and thus the upper left corner
     * anchoring point.
     * 
     */ 
    private int heightMultiplier;
    private int widthMultiplier;

    private AnnotationLocation(int widthMultiplier, int heightMultiplier) {
      validateMultipliers(widthMultiplier, heightMultiplier);
      this.widthMultiplier = widthMultiplier;
      this.heightMultiplier = heightMultiplier;
    }

    /*
     * Retrieves a static location given its multipliers.
     */ 
    private static AnnotationLocation getAnnotationLocation(
        int widthMultiplier, int heightMultiplier) {
      final AnnotationLocation[][] locationMap = {
          { NORTHWEST, NORTH, NORTHEAST },
          { WEST, CENTER, EAST },
          { SOUTHWEST, SOUTH, SOUTHEAST } };
      // assumes both multiplier are -1, 0, or 1
      AnnotationLocation result = locationMap[heightMultiplier + 1][widthMultiplier + 1];
      return result;
    }

    /*
     * Negative width or height "turn the symbol inside-out", requiring
     * a corresponding "reflection" of annotation location (only needed
     * for baseline-based bar symbols).
     * 
     */ 
    static AnnotationLocation transform(AnnotationLocation a,
        int signWidth, int signHeight) {
      AnnotationLocation result = a;
      if (signWidth < 0 || signHeight < 0)
        result = getAnnotationLocation(
            signWidth * a.widthMultiplier, signHeight * a.heightMultiplier);

      return result;
    }

    /*
     * These define the alignment of the label within it's containing 1
     * x 1 Grid (&lt;table*gt;). For example, if this containing Grid is
     * to the left of the labeled symbol (widthMultiplier==-1) the
     * horizontal alignment will be ALIGN_RIGHT, so as to bring the
     * contained label flush against the left edge of the labeled
     * symbol.
     * 
     */ 
    HasHorizontalAlignment.HorizontalAlignmentConstant getHorizontalAlignment() {
      HasHorizontalAlignment.HorizontalAlignmentConstant result;
      if (widthMultiplier == -1)
        result = HasHorizontalAlignment.ALIGN_RIGHT;
      else if (widthMultiplier == 0)
        result = HasHorizontalAlignment.ALIGN_CENTER;
      else if (widthMultiplier == 1)
        result = HasHorizontalAlignment.ALIGN_LEFT;
      else
        throw new IllegalStateException("Invalid widthMultiplier: "
            + widthMultiplier + " 1, 0, or -1 were expected.");
      return result;
    }

    /* analogous to getHorizontalAlignment, but for vertical alignment */
    HasVerticalAlignment.VerticalAlignmentConstant getVerticalAlignment() {
      HasVerticalAlignment.VerticalAlignmentConstant result;
      if (heightMultiplier == -1)
        result = HasVerticalAlignment.ALIGN_BOTTOM;
      else if (heightMultiplier == 0)
        result = HasVerticalAlignment.ALIGN_MIDDLE;
      else if (heightMultiplier == 1)
        result = HasVerticalAlignment.ALIGN_TOP;
      else
        throw new IllegalStateException("Invalid heightMultiplier: "
                                        + heightMultiplier + " -1, 0, or 1 were expected.");
      return result;
    }

    /*
     * Given the x-coordinate at the center of the symbol that this
     * annotation annotates, the annotation's width, and the symbol's
     * width, this method returns the x-coordinate of the upper left
     * corner (left edge) of this annotation.
     */
    int getUpperLeftX(double x, double w, double symbolW) {
      int result = (int) Math.round(x
          + (widthMultiplier * (w + symbolW) - w) / 2.);
      return result;
    }

    /* analogous to getUpperLeftX, but for the y-coordinate (top edge) */
    int getUpperLeftY(double y, double h, double symbolH) {
      int result = (int) Math.round(y
          + (heightMultiplier * (h + symbolH) - h) / 2.);
      return result;
    }

    /*
     * This method returns the annotation location whose "attachment point"
     * keeps the annotation either completely outside, centered on, or
     * completely inside (depending on if the heightMultiplier of this
     * annotation is 1, 0, or -1) the point on the pie's circumference
     * associated with the given angle. <p>
     * 
     * The use of heightMultiplier rather than widthMultiplier is somewhat
     * arbitrary, but was chosen so that the NORTH, CENTER, and SOUTH annotation
     * locations have the same interpretation for a pie slice whose bisecting
     * radius points due south (due south is the default initial pie slice
     * orientation) and for a 1px x 1px BOX_CENTER type symbol positioned at the
     * due south position on the pie's circumference. As the
     * pie-slice-arc-bisection point moves clockwise around the pie perimeter,
     * the attachment point (except for vertically-centered annotations, which
     * remain centered on the pie arc) also moves clockwise, but in discrete
     * jumps (e.g. from NORTH, to NORTHEAST, to EAST, to SOUTHEAST, to SOUTH,
     * etc. for annotations inside the pie) so the annotation remains
     * appropriately attached to the center of the slice's arc as the angle
     * changes.
     * <p>
     * 
     * thetaMid is the conventional trigonometric angle measured
     * counter-clockwise from +x.
     * 
     */
    AnnotationLocation decodePieLocation(double thetaMid) {
      /*
       * A sin or cos that is small enough so that the associated angle
       * is horizontal (for sines) or vertical (for cosines) enough to
       * warrant use of a "centered" annotation location.
       * 
       */ 
      final double LOOKS_VERTICAL_OR_HORIZONTAL_DELTA = 0.1;
      double sinTheta = Math.sin(thetaMid);
      double cosTheta = Math.cos(thetaMid);
      int pieTransformedWidthMultiplier = heightMultiplier
          * ((cosTheta < -LOOKS_VERTICAL_OR_HORIZONTAL_DELTA) ? -1
          : ((cosTheta > LOOKS_VERTICAL_OR_HORIZONTAL_DELTA) ? 1
          : 0));
      int pieTransformedHeightMultiplier = heightMultiplier
          * ((sinTheta < -LOOKS_VERTICAL_OR_HORIZONTAL_DELTA) ? 1
          : ((sinTheta > LOOKS_VERTICAL_OR_HORIZONTAL_DELTA) ? -1
          : 0));

      return getAnnotationLocation(pieTransformedWidthMultiplier,
          pieTransformedHeightMultiplier);

    }

  } // end of class AnnotationLocation

  /**
   * Defines keywords that specify
   * the location of the legend on the chart.  <p>
   *
   * <i>Tip:</i> You can emulate the missing <tt>OUTSIDE_TOP</tt> and
   * <tt>OUTSIDE_BOTTOM</tt> locations by using <tt>INSIDE_TOP</tt> or
   * <tt>INSIDE_BOTTOM</tt> and shifting the y position appropriately
   * via <tt>setLegendYShift</tt>.
   * <p>
   *
   * <i>Tip:</i> If GChart's internally generated, single column
   * formatted, legend key isn't appropriate, you can use your own
   * legend widget via <tt>setLegend</tt>.
   * 
   * 
   * @see #setLegendLocation setLegendLocation
   * @see #setLegendXShift setLegendYShift
   * @see #setLegendYShift setLegendYShift
   * @see #setLegend setLegend
   * 
   */

  public static class LegendLocation {
    /*
     * To realize this location, the system curve representing the
     * legend will be given this symbol type and annotation location.
     * 
     */ 
    private SymbolType symbolType;
    private AnnotationLocation annotationLocation;

    private LegendLocation(SymbolType symbolType,
        AnnotationLocation annotationLocation) {
      this.symbolType = symbolType;
      this.annotationLocation = annotationLocation;
    }

    SymbolType getSymbolType() {
      return symbolType;
    }

    AnnotationLocation getAnnotationLocation() {
      return annotationLocation;
    }

    /*
     * These initial pixel shifts from the symbol/location defined
     * initial positions are used, for example, to leave space for the
     * x, y or y2 axis ticks, tick labels, and axis labels. Note that
     * legend anchoring positions are always at compass point positions
     * around the plot area (via the ANCHOR_* family of symbol types).
     * 
     */
    int getInitialXShift(GChart g) {
      return 0;
    }
    int getInitialYShift(GChart g) {
      return 0;
    }

    /*
     * Retrieves the space taken up by the legend on the left and the right
     * sides of the chart when it is placed at this location. <p>
     * 
     * The INSIDE_* family of legend locations take up no space on the left or
     * right of the chart, so they just use the default (0). <p>
     * 
     * OUTSIDE_LEFT, OUTSIDE_RIGHT methods override getLeftThickness,
     * getRightThickness so as to account for the space the legend takes up when
     * positioned outside the chart. <p>
     * 
     * GChart uses these methods to adjust the relative positioning of various
     * chart parts so as to leave enough room for the legend, regardless of
     * where it is located.
     * 
     */
    int getLeftThickness(GChart g) {
      return 0;
    }

    int getRightThickness(GChart g) {
      return 0;
    }

    // the legend location to the right of the plot area
    private static final class RightLegendLocation extends LegendLocation {
      RightLegendLocation() {
        super(SymbolType.ANCHOR_EAST, AnnotationLocation.CENTER);
      }

      @Override
      int getInitialXShift(GChart g) {
        int result = g.getY2Axis().getTickLabelThickness(false)
            + g.getY2Axis().getTickSpace()
            + g.getY2Axis().getTickLabelPadding()
            + g.getY2Axis().getAxisLabelThickness()
            + g.getLegendThickness() / 2;
        return result;
      }

      @Override
      int getRightThickness(GChart g) {
        int result = g.getLegendThickness();
        return result;
      }
    }

    // the legend location to the left of the plot area
    private static final class LeftLegendLocation extends LegendLocation {
      LeftLegendLocation() {
        super(SymbolType.ANCHOR_WEST, AnnotationLocation.CENTER);
      }

      @Override
      int getInitialXShift(GChart g) {
        int result = -g.getYAxis().getTickLabelThickness(false)
            - g.getYAxis().getTickSpace()
            - g.getYAxis().getTickLabelPadding()
            - g.getYAxis().getAxisLabelThickness()
            - g.getLegendThickness() / 2;
        return result;
      }

      @Override
      int getLeftThickness(GChart g) {
        int result = g.getLegendThickness();
        return result;
      }
    }

    /**
     * Places the legend inside the plot area, centered along the bottom
     * edge.
     * 
     * @see GChart#setLegendLocation setLegendLocation
     */
    public final static LegendLocation INSIDE_BOTTOM = new LegendLocation(
            SymbolType.ANCHOR_SOUTH, AnnotationLocation.NORTH);
    /**
     * Places the legend in the lower left inside corner of the plot area.
     * 
     * @see GChart#setLegendLocation setLegendLocation
     */
    public final static LegendLocation INSIDE_BOTTOMLEFT = new LegendLocation(
            SymbolType.ANCHOR_SOUTHWEST, AnnotationLocation.NORTHEAST);
    /**
     * Places the legend in the lower right inside corner of the plot area.
     * 
     * @see GChart#setLegendLocation setLegendLocation
     */
    public final static LegendLocation INSIDE_BOTTOMRIGHT = new LegendLocation(
            SymbolType.ANCHOR_SOUTHEAST, AnnotationLocation.NORTHWEST);
    /**
     * Places the legend inside the plot area, centered along the left
     * edge.
     * 
     * @see GChart#setLegendLocation setLegendLocation
     */
    public final static LegendLocation INSIDE_LEFT = new LegendLocation(
            SymbolType.ANCHOR_WEST, AnnotationLocation.EAST);
    /**
     * Places the legend inside the plot area, centered along the right
     * edge.
     * 
     * @see GChart#setLegendLocation setLegendLocation
     */
    public final static LegendLocation INSIDE_RIGHT = new LegendLocation(
            SymbolType.ANCHOR_EAST, AnnotationLocation.WEST);
    /**
     * Places the legend inside the plot area, centered along the top
     * edge.
     * 
     * @see GChart#setLegendLocation setLegendLocation
     */
    public final static LegendLocation INSIDE_TOP = new LegendLocation(
            SymbolType.ANCHOR_NORTH, AnnotationLocation.SOUTH);

    /**
     * Places the legend in the upper left inside corner of the plot area.
     * 
     * @see GChart#setLegendLocation setLegendLocation
     */
    public final static LegendLocation INSIDE_TOPLEFT = new LegendLocation(
        SymbolType.ANCHOR_NORTHWEST, AnnotationLocation.SOUTHEAST);
    /**
     * Places the legend in the upper right inside corner of the plot area.
     * 
     * @see GChart#setLegendLocation setLegendLocation
     */
    public final static LegendLocation INSIDE_TOPRIGHT = new LegendLocation(
        SymbolType.ANCHOR_NORTHEAST, AnnotationLocation.SOUTHWEST);
    /**
     * Places the legend so that it is vertically centered on the left edge of
     * the chart's plot area, and to the left of the y axis label.
     * 
     */
    public final static LegendLocation OUTSIDE_LEFT = new LeftLegendLocation();
    /**
     * Places the legend so that it is vertically centered on the right edge of
     * the chart's plot area, and to the right of the y2 axis label.
     * 
     */
    public final static LegendLocation OUTSIDE_RIGHT = new RightLegendLocation();
  } // class LegendLocation 

  /**
   * Represents an axis of the chart, for example, the x, y, or y2 axis. An axis
   * consists of the axis itself, along with its tick marks, tick labels and
   * gridlines.
   * 
   * @see XAxis XAxis
   * @see YAxis YAxis
   * @see Y2Axis Y2Axis
   * @see #getXAxis getXAxis
   * @see #getYAxis getYAxis
   * @see #getY2Axis getY2Axis
   * 
   */
  public abstract class Axis {
    // points dropped if more than this number of axis lengths off chart
    private double outOfBoundsMultiplier = Double.NaN;
    // true for X, false for Y
    protected boolean isHorizontalAxis;
    // sys curve representing ticks
    protected int ticksId;
    // sys curve representing gridlines
    protected int gridlinesId;
    // sys curve representing axis line
    protected int axisId;
    // +/-1 for right/left axes
    protected int axisPosition;
    protected TickLocation tickLocation = DEFAULT_TICK_LOCATION;
    // number of developer curves on axis. Count does not include
    // system or invisible curves.
    private int nCurvesVisibleOnAxis = 0;

    void incrementCurves() {
      nCurvesVisibleOnAxis++;
    }

    void decrementCurves() {
      nCurvesVisibleOnAxis--;
    }

    // model unit limits associated with an axis
    protected class AxisLimits {
      double min;
      double max; // in user-defined model units

      AxisLimits(double min, double max) {
        this.min = min;
        this.max = max;
      }

      boolean equals(AxisLimits al) {
        boolean result = (al.min == min && al.max == max);
        return result;
      }
    }

    // different initial curr, prev ==> "limits have changed" state
    private AxisLimits currentLimits = new AxisLimits(Double.MAX_VALUE,
        -Double.MAX_VALUE);
    private AxisLimits previousLimits = new AxisLimits(-Double.MAX_VALUE,
        Double.MAX_VALUE);

    private Widget axisLabel;
    protected int axisLabelThickness = GChart.NAI;
    private boolean hasGridlines = false;
    protected int tickCount = DEFAULT_TICK_COUNT;
    // axes auto-scale whenever min or max are NaN.
    protected double axisMax = Double.NaN;
    protected double axisMin = Double.NaN;
    protected String tickLabelFontColor = DEFAULT_TICK_LABEL_FONT_COLOR;
    protected String tickLabelFontFamily = null;
    /*
     * In CSS font-size pixels. These define the height of each
     * character; our code relies on the rule of thumb that character
     * width is approximately 3/5th this height to obtain a reasonably
     * tight upper bound on tick label widths. So, even when Widget
     * based tick labels override these sizes, the specified size can
     * still impact the default amount of space reserved for the labels.
     * 
     */ 
    protected int tickLabelFontSize = DEFAULT_TICK_LABEL_FONTSIZE;
    protected String tickLabelFontStyle = DEFAULT_TICK_LABEL_FONT_STYLE;
    protected String tickLabelFontWeight = DEFAULT_TICK_LABEL_FONT_WEIGHT;
    protected String tickLabelFormat = DEFAULT_TICK_LABEL_FORMAT;
    protected int tickLabelThickness = GChart.NAI;
    protected int tickLabelPadding = 0;
    protected int ticksPerLabel = 1;
    protected int ticksPerGridline = 1;
    protected int tickLength = DEFAULT_TICK_LENGTH;
    protected int tickThickness = DEFAULT_TICK_THICKNESS;

    // is axis itself visible (has no impact ticks or their labels)
    boolean axisVisible = true;

    /**
     * Adds a tick on this axis at the specified position. Note that explicitly
     * adding a single tick via this method will eliminate any implicitly
     * generated ticks associated with the <tt>setTickCount</tt> method.
     * <p>
     * 
     * The label associated with this tick will be generated by applying the
     * format specified via <tt>setTickLabelFormat</tt> to the specified
     * position.
     * <p>
     * 
     * This is a convenience method equivalent to
     * <tt>addTick(tickPosition, thisAxis.formatAsTickLabel(tickPosition), GChart.NAI,
     * GChart.NAI)</tt>. See {@link #addTick(double,String,int,int)
     * addTick(tickPosition,tickLabel,widthUpperBound,heightUpperBound)} for
     * details.
     * 
     * @param tickPosition
     *          the position, in model units, along this axis at which this tick
     *          is displayed. For example, if the axis range goes from 0 to 100,
     *          a tick at position 50 would appear in the middle of the axis.
     * 
     * @see #clearTicks clearTicks
     * @see #addTick(double,String) addTick(double,String)
     * @see #addTick(double,String,int,int) addTick(double,String,int,int)
     * @see #addTick(double,Widget,int,int) addTick(double,Widget,int,int)
     * @see #formatAsTickLabel formatAsTickLabel
     * @see #setTickCount setTickCount
     * @see #setTickLabelFormat setTickLabelFormat
     * @see #setTickLabelFontStyle setTickLabelFontStyle
     * @see #setTickLabelFontColor setTickLabelFontColor
     * @see #setTickLabelFontWeight setTickLabelFontWeight
     * @see #setTickLabelFontSize setTickLabelFontSize
     * 
     */
    public void addTick(double tickPosition) {
      addTick(tickPosition, formatAsTickLabel(tickPosition));
    }

    // adds a labeled tick mark via this Axis' special system tick curve
    private void addTickAsPoint(double tickPosition, String tickLabel,
        Widget tickWidget, int widthUpperBound, int heightUpperBound) {

      Curve c = getSystemCurve(ticksId);
      if (isHorizontalAxis)
        c.addPoint(tickPosition, axisPosition * Double.MAX_VALUE);
      else
        c.addPoint(axisPosition * Double.MAX_VALUE, tickPosition);

      // unlabeled tick--we are done, so return to save time
      if (null == tickLabel && null == tickWidget)
        return;

      // add an annotation representing the tick label
      Curve.Point p = c.getPoint();
      if (isHorizontalAxis) {
        // below tick on X, above it on (the future) X2
        p.setAnnotationLocation((axisPosition < 0) ?
            AnnotationLocation.SOUTH : AnnotationLocation.NORTH);
        if (tickLabelPadding != 0) // padding < 0 is rare but allowed
          p.setAnnotationYShift(axisPosition * tickLabelPadding);
        // else stick with default of 0 y-shift

      } else {
        // to left of tick mark on Y, to right of it on Y2
        p.setAnnotationLocation((axisPosition < 0) ?
            AnnotationLocation.WEST : AnnotationLocation.EAST);
        if (tickLabelPadding != 0)
          p.setAnnotationXShift(axisPosition * tickLabelPadding);
        // else stick with default of 0 x-shift
      }

      if (null != tickLabel)
        p.setAnnotationText(tickLabel, widthUpperBound,
            heightUpperBound);
      else if (null != tickWidget)
        p.setAnnotationWidget(tickWidget, widthUpperBound,
            heightUpperBound);

      p.setAnnotationFontSize(getTickLabelFontSize());
      p.setAnnotationFontFamily(getTickLabelFontFamily());
      p.setAnnotationFontStyle(getTickLabelFontStyle());
      p.setAnnotationFontColor(getTickLabelFontColor());
      p.setAnnotationFontWeight(getTickLabelFontWeight());

    }

    /**
     * Adds a tick at the specified position with the specified label on this
     * axis, whose width and height are within the specified upper-bounds.
     * 
     * <p>
     * Note that explicitly adding a single tick via this method will eliminate
     * any auto-generated ticks associated with the <tt>setTickCount</tt>
     * method.
     * 
     * <p>
     * Use this method to specify unusually spaced tick marks with labels that
     * do not directly reflect the position (for example, for a logarithmic
     * axis, or for a bar chart with special keyword-type labels, or a time axis
     * that places date and time on two separate lines).
     * 
     * @param tickPosition
     *          the position, in model units, along this axis at which the tick
     *          is displayed. For example, if the axis range goes from 0 to 1, a
     *          tick at position 0.5 would appear in the middle of the axis.
     * 
     * @param tickLabel
     *          the label for this tick. HTML is supported in tick labels, but
     *          it must be prefixed by <tt>&lt;html&gt</tt>. See the
     *          {@link Curve.Point#setAnnotationText(String,int,int)
     *          setAnnotationText} method for more information.
     * 
     * @param widthUpperBound
     *          an upper bound on the width of the text or HTML, in pixels. Use
     *          <tt>GChart.NAI</tt> to get GChart to estimate this width for
     *          you. See the <tt>setAnnotationText</tt> method for more
     *          information.
     * 
     * @param heightUpperBound
     *          an upper bound on the height of the text or HTML, in pixels. Use
     *          <tt>GChart.NAI</tt> to get GChart to estimate this height for
     *          you. See the <tt>setAnnotationText</tt> method for more
     *          information.
     * 
     * @see #clearTicks clearTicks
     * @see #addTick(double) addTick(double)
     * @see #addTick(double,String) addTick(double,String)
     * @see #addTick(double,Widget,int,int) addTick(double,Widget,int,int)
     * @see #setTickCount setTickCount
     * @see #setTickLabelFormat setTickLabelFormat
     * @see #setTickLabelFontSize setTickLabelFontSize
     * @see #setTickLabelFontStyle setTickLabelFontStyle
     * @see #setTickLabelFontColor setTickLabelFontColor
     * @see #setTickLabelFontWeight setTickLabelFontWeight
     * @see Curve.Point#setAnnotationText(String,int,int) setAnnotationText
     * @see Curve.Point#setAnnotationWidget setAnnotationWidget
     * 
     */
    public void addTick(double tickPosition, String tickLabel,
        int widthUpperBound, int heightUpperBound) {
      chartDecorationsChanged = true;
      if (GChart.NAI != tickCount) { // clear out any auto-generated ticks
        Curve cTicks = getSystemCurve(ticksId);
        cTicks.clearPoints();
        tickCount = GChart.NAI;
      }
      addTickAsPoint(tickPosition, tickLabel, null, widthUpperBound,
          heightUpperBound);
    }

    /**
     * Adds a tick at the specified position with the specified label on this
     * axis.
     * <p>
     * 
     * This is a convenience method equivalent to
     * <tt>addTick(tickPosition, tickLabel, GChart.NAI,
     * GChart.NAI)</tt>. Most applications can usually just use this convenience
     * method. See {@link #addTick(double,String,int,int)
     * addTick(tickPosition,tickLabel, widthUpperBound,heightUpperBound)} for
     * the fine print.
     * 
     * @param tickPosition
     *          the position, in model units, along this axis at which the tick
     *          is displayed.
     * 
     * @param tickLabel
     *          the plain text or (<tt>&lt;html&gt</tt>-prefixed) HTML defining
     *          the tick's label.
     * 
     * @see #addTick(double,String,int,int) addTick(double,String,int,int)
     * @see #addTick(double,Widget) addTick(double,Widget)
     * 
     */
    public void addTick(double tickPosition, String tickLabel) {
      addTick(tickPosition, tickLabel, GChart.NAI, GChart.NAI);
    }

    /**
     * Adds a widget-defined tick label at the specified position, whose width
     * and height are within the specified upper-bounds.
     * <p>
     * 
     * This method is similar to <tt>addTick(double,String,int,int)</tt> except
     * that it uses a widget, rather than a string, to define the tick's label.
     * Although the string-based method is faster on first chart rendering, and
     * uses less memory, the widget-based method allows you to change the label
     * independently of the chart--potentially bypassing (or speeding up)
     * expensive chart updates later on.
     * <p>
     * 
     * You might use a widget-based tick label to pop up a dialog that allows
     * the user to edit the parameters defining the axis (min, max, etc.)
     * whenever they click on one of the tick labels on that axis, to define
     * hovertext that appears when the user mouses over a tick label, to use
     * images for your tick labels, etc.
     * 
     * @param tickPosition
     *          the position, in model units, along this axis at which the tick
     *          is displayed. For example, if the axis range goes from 0 to 1, a
     *          tick at position 0.5 would appear in the middle of the axis.
     * 
     * @param tickWidget
     *          the label for this tick, as defined by any GWT Widget.
     * 
     * @param widthUpperBound
     *          an upper bound on the width of the widget, in pixels. If this
     *          and the next parameter are omitted, GChart will use
     *          <tt>DEFAULT_WIDGET_WIDTH_UPPERBOUND</tt>.
     * 
     * @param heightUpperBound
     *          an upper bound on the height of the widget, in pixels. If this
     *          and the previous parameter are omitted, GChart will use <tt>
     *  DEFAULT_WIDGET_HEIGHT_UPPERBOUND</tt>
     * 
     * @see #addTick(double,Widget) addTick(double,Widget)
     * @see #addTick(double,String,int,int) addTick(double,String,int,int)
     * @see Curve.Point#setAnnotationWidget setAnnotationWidget
     * @see #DEFAULT_WIDGET_WIDTH_UPPERBOUND DEFAULT_WIDGET_WIDTH_UPPERBOUND
     * @see #DEFAULT_WIDGET_HEIGHT_UPPERBOUND DEFAULT_WIDGET_HEIGHT_UPPERBOUND
     */
    public void addTick(double tickPosition, Widget tickWidget,
        int widthUpperBound, int heightUpperBound) {
      chartDecorationsChanged = true;
      if (GChart.NAI != tickCount) { // clear out any auto-generated ticks
        Curve cTicks = getSystemCurve(ticksId);
        cTicks.clearPoints();
        tickCount = GChart.NAI;
      }
      addTickAsPoint(tickPosition, null, tickWidget, widthUpperBound,
          heightUpperBound);
    }

    /**
     * Adds a Widget-defined tick label at the specified position. Convenience
     * method equivalent to <tt>addTick(tickPosition, tickWidget,
     *  DEFAULT_WIDGET_WIDTH_UPPERBOUND,
     *  DEFAULT_WIDGET_HEIGHT_UPPERBOUND)</tt>.
     * 
     * @param tickPosition
     *          the position, in model units, along this axis at which the tick
     *          is displayed. For example, if the axis range goes from 0 to 1, a
     *          tick at position 0.5 would appear in the middle of the axis.
     * 
     * @param tickWidget
     *          the label for this tick, as defined by any GWT Widget.
     * 
     * @see #addTick(double,Widget,int,int) addTick(double,Widget,int,int)
     * 
     */
    public void addTick(double tickPosition, Widget tickWidget) {
      addTick(tickPosition, tickWidget, DEFAULT_WIDGET_WIDTH_UPPERBOUND,
          DEFAULT_WIDGET_HEIGHT_UPPERBOUND);
    }

    /**
     * 
     * Removes all ticks from this axis. Specifically, erases any ticks that
     * were explicitly specified via <tt>addTick</tt>, and also sets the tick
     * count to 0.
     * <p>
     * 
     * @see #setTickCount setTickCount
     * @see #addTick(double) addTick(double)
     * @see #addTick(double,String) addTick(double,String)
     * @see #addTick(double,String,int,int) addTick(double,String,int,int)
     * @see #addTick(double,Widget) addTick(double,Widget)
     * @see #addTick(double,Widget,int,int) addTick(double,Widget,int,int)
     * 
     */
    public void clearTicks() {
      chartDecorationsChanged = true;
      tickCount = GChart.NAI;
      Curve c = getSystemCurve(ticksId);
      c.clearPoints();
    }

    /**
     * Converts a pixel, client-window coordinate position along this axis into
     * the model units associated with this axis.
     * <p>
     * 
     * For example, if the client coordinate associated with this axis' midpoint
     * were passed to this method, it would return
     * <tt>(getAxisMin() + getAxisMax())/2.0</tt>.
     * <p>
     * 
     * <small> Note that the client/model coordinate mapping used is as of the
     * last <tt>update</tt>. Before the first <tt>update</tt>, this method
     * returns <tt>GChart.NaN</tt>. This method also invokes either
     * <tt>getAbsoluteTop</tt> (for the y or y2 axis) or
     * <tt>getAbsoluteLeft</tt> (for the x axis), and these GWT methods return 0
     * if the chart isn't actually rendered within the browser. So, results
     * likely won't be useful to you until after the page containing your chart
     * becomes visible to the user. Since most applications are expected to
     * invoke this method in response to the user mousing over the page, these
     * requirements should usually be satisfied. </small>
     * <p>
     * 
     * <small> Saurabh Hirani in <a href=
     * "http://groups.google.com/group/Google-Web-Toolkit/msg/80301715acb6f719"
     * > this GWT Forum post</a> and in GChart <a
     * href="http://code.google.com/p/gchart/issues/detail?id=21">issue #22</a>
     * most recently suggested the need for client to model coordinate mapping.
     * Client to model conversion was requested earlier in GChart <a
     * href="http://code.google.com/p/gchart/issues/detail?id=9">issue #9</a>
     * from <a href="http://yoxel.com">yoxel.com</a>. </small>
     * 
     * @param clientCoordinate
     *          a pixel-based coordinate that defines the dimension associated
     *          with this axis in the standard client window coordinates of GWT.
     * 
     * @return the location defined by the client-coordinate argument, but
     *         converted into the model units associated with this axis.
     * 
     * @see #getMouseCoordinate getMouseCoordinate
     * @see #modelToClient modelToClient
     * @see #pixelToModel pixelToModel
     * @see #modelToPixel modelToPixel
     * 
     * 
     */
    public abstract double clientToModel(int clientCoordinate);

    // these are used in formatting tick positions into tick labels:
    private NumberFormat numberFormat =
        NumberFormat.getFormat(DEFAULT_TICK_LABEL_FORMAT);
    private DateTimeFormat dateFormat =
        DateTimeFormat.getShortDateTimeFormat();
    private final int NUMBER_FORMAT_TYPE = 0;
    private final int DATE_FORMAT_TYPE = 1;
    private final int LOG10INVERSE_FORMAT_TYPE = 2;
    private final int LOG2INVERSE_FORMAT_TYPE = 3;
    private int tickLabelFormatType = NUMBER_FORMAT_TYPE;

    /**
     * 
     * Applies this axis' tick label format to format a given value.
     * 
     * @return the value formated as per this axis' currently specified tick
     *         label format.
     * 
     * @see #setTickLabelFormat(String) setTickLabelFormat
     * 
     */
    public String formatAsTickLabel(double value) {
      String result = null;
      switch (tickLabelFormatType) {
      case NUMBER_FORMAT_TYPE:  
        result = numberFormat.format(value);
        break;
      case DATE_FORMAT_TYPE:
        Date transDate = new Date((long) value);
        result = dateFormat.format(transDate);
        break;
      case LOG10INVERSE_FORMAT_TYPE:
        value = Math.pow(10., value);
        result = numberFormat.format(value);
        break;
      case LOG2INVERSE_FORMAT_TYPE:
        value = Math.pow(2., value);
        result = numberFormat.format(value);
        break;
      default:
        throw new IllegalStateException("Invalid tick label format type:" +
          tickLabelFormatType + ". Likely cause: a GChart bug.");
      }
      return result;
    }

    /**
     * @deprecated
     * 
     *             Equivalent to the better-named formatAsTickLabel.
     * 
     *             <p>
     * 
     * @see #formatAsTickLabel formatAsTickLabel
     * 
     */
    public String formatNumberAsTickLabel(double value) {
      return formatAsTickLabel(value);
    }

    /**
     * Returns the previously specified label of this axis.
     * 
     * @return the Widget used as the label of this axis
     * 
     * @see #setAxisLabel setAxisLabel
     * 
     */
    public Widget getAxisLabel() {
      return axisLabel;
    }

    /**
     * Returns the thickness of the axis-label-holding region adjacent to the
     * region allocated for this axis' tick labels.
     * <p>
     *
     * If the axis label thickness is specified explicitly, that value
     * is returned. Otherwise, with an undefined (<tt>GChart.NAI</tt>)
     * axis label thickness, an heuristic based on the text or HTML
     * defining the axis label, and an estimate of character size, is
     * used to estimate axis width.
     * 
     * <p>
     * 
     * @return the thickness of the axis-label-holding region, in pixels.
     * 
     * @see #setAxisLabelThickness setAxisLabelThickness
     * 
     */
    public int getAxisLabelThickness() {
      int result = 0;
      // Base class implementation is for y axes (x-axis will override).
      final int EXTRA_CHARWIDTH = 2; // 1-char padding on each side
      final int DEF_CHARWIDTH = 1; // when widget has no text
      if (GChart.NAI != axisLabelThickness)
        result = axisLabelThickness;
      else if (null == getAxisLabel())
        result = 0;
      else if (getAxisLabel() instanceof HasHTML) {
        int charWidth = htmlWidth(
            ((HasHTML) (getAxisLabel())).getHTML());
        result = (int) Math.round((charWidth + EXTRA_CHARWIDTH)
            * getTickLabelFontSize()
            * TICK_CHARWIDTH_TO_FONTSIZE_LOWERBOUND);
      } else if (getAxisLabel() instanceof HasText) {
        String text = ((HasText) (getAxisLabel())).getText();
        result = (int) Math.round((EXTRA_CHARWIDTH +
            ((null == text) ? 0 : text.length()))
            * getTickLabelFontSize()
            * TICK_CHARWIDTH_TO_FONTSIZE_LOWERBOUND);
      } else {
        // non-text widget. Not a clue, just use def width
        result = (int) Math.round((DEF_CHARWIDTH + EXTRA_CHARWIDTH)
            * getTickLabelFontSize()
            * TICK_CHARWIDTH_TO_FONTSIZE_LOWERBOUND);
      }
      return result;
    }

    /**
     * Returns the maximum value displayed on this axis. If the explicitly
     * specified maximum value is undefined (<tt>Double.NaN</tt>) the maximum
     * value returned by this function is calculated as the maximum of all of
     * the values either displayed on this axis via points on a curve, or
     * explicitly specified via tick positions.
     * 
     * @return maximum value visible on this axis, in "model units" (arbitrary,
     *         application-specific, units)
     * 
     * @see #setAxisMax setAxisMax
     * @see #getDataMin getDataMin
     * @see #getDataMax getDataMax
     */
    public double getAxisMax() {

      if (!(axisMax != axisMax)) { // x!=x is a faster isNaN
        return axisMax;
      } else if (GChart.NAI != tickCount) {
        return getDataMax();
      } else {
        return Math.max(getDataMax(), getTickMax());
      }
    }

    /**
     * 
     * Returns the minimum value displayed on this axis. If the minimum value is
     * undefined (<tt>Double.NaN</tt>) the minimum value returned by this
     * function is the minimum of all of the values either displayed on this
     * axis via points on a curve, or explicitly specified via tick positions.
     * 
     * @return minimum value visible on this axis, in "model units" (arbitrary,
     *         application-specific, units)
     * 
     * @see #setAxisMin setAxisMin
     */
    public double getAxisMin() {
      if (!(axisMin != axisMin)) { // x!=x is a faster isNaN
        return axisMin; // explicitly set
      } else if (GChart.NAI != tickCount) {
        return getDataMin();
      } else {
        return Math.min(getDataMin(), getTickMin());
      }
    }

    /**
     * Is axis line visible on the chart? Note that this property only
     * determines the visibility of the axis line itself. It does not control
     * the visibility of the tick marks or tick labels along this axis.
     * <p>
     * 
     * @return true if the axis line is visible, false otherwise.
     * 
     * @see #setAxisVisible setAxisVisible
     * 
     */
    public boolean getAxisVisible() {
      return axisVisible;
    }

    /**
     * The maximum number of "axis lengths" a point can be off this axis before
     * it is completely dropped from the chart's rendering.
     * 
     * @return the out of bounds multiplier.
     * 
     * @see #setOutOfBoundsMultiplier setOutOfBoundsMultiplier
     * 
     */
    public double getOutOfBoundsMultiplier() {
      return outOfBoundsMultiplier;
    }

    /**
     * Returns the maximum data value associated with values represented on this
     * axis. For example, for the left y-axis, this would be the largest y-value
     * of all points contained in curves that are displayed on the left y-axis.
     * 
     * @return the maximum value associated with values mapped onto this axis.
     * 
     * @see #getDataMin getDataMin
     * @see #getAxisMax getAxisMax
     * @see #getAxisMin getAxisMin
     * 
     */
    public abstract double getDataMax();

    /**
     * Returns the minimum data value associated with values represented on this
     * axis. For example, for the left y-axis, this would be the smallest
     * y-value of all points contained in curves that are displayed on the left
     * y-axis.
     * 
     * @return the minimum value associated with values mapped onto this axis.
     * 
     * @see #getDataMax getDataMax
     * @see #getAxisMax getAxisMax
     * @see #getAxisMin getAxisMax
     * 
     */
    public abstract double getDataMin();

    /**
     * Returns the gridline setting previously made with
     * <tt>setHasGridlines</tt>.
     * 
     * @return true if gridlines have been enabled, false if not.
     * 
     * @see #setHasGridlines setHasGridlines
     * 
     */
    public boolean getHasGridlines() {
      return hasGridlines;
    }

    /**
     * Returns the coordinate along this axis that is associated with the last
     * "GChart-tracked" mouse location.
     * <p>
     * 
     * The coordinate returned is in the "scale" associated with the axis. For
     * example, if the axis mininum is 0 and the maximum is 100, and the mouse
     * is at the axis midpoint, this method would return 50.
     * <p>
     * 
     * The main intended use for this method is to allow you to create points
     * that, if they have x and y coordinates defined by calling this method on
     * appropriate axes, will be positioned on the chart at the last
     * GChart-tracked mouse location.
     * <p>
     * 
     * As the user moves their mouse over the chart, GChart watches those mouse
     * moves and updates it's currently "tracked" mouse location. That
     * internally maintained position is the basis for the value returned by
     * this method. Note that the actual, physical, mouse cursor position could
     * differ from this GChart-tracked position because:
     * 
     * <p>
     * 
     * <ol>
     * 
     * <li>The mouse has moved off the chart, and it's GChart-tracked location
     * has become undefined (this method returns <tt>Double.NaN</tt> in that
     * case)
     * 
     * <li>You have invoked <tt>setHoverTouchingEnabled(false)</tt> which means
     * that mouse moves are no longer tracked, so the last GChart-tracked mouse
     * location will be the last position that the user clicked on.
     * 
     * <li>You have popped up a modal dialog that "eats" mouse moves so GChart
     * no longer sees them. In that case, the GChart-tracked mouse location is
     * the location the mouse was at when the modal dialog popped up.
     * 
     * <li>You are mousing over the opened hover widget (popup). Note that, to
     * prevent the user from accidentally "touching" nearby points while
     * interacting with the opened hover widget, GChart ignores mouse moves over
     * the opened hover widget.
     * 
     * <li>Other, similar, reasons.
     * 
     * </ol>
     * <p>
     * 
     * In other words, this routine tells you where, for hit testing and hover
     * selection feedback purposes, GChart considers the mouse to be, not the
     * actual physical location of the mouse. Despite the potential for
     * differences, in most cases, with the default setting of
     * <tt>setHoverTouchingEnabled(true)</tt>, and when you are not over the
     * opened hover widget, you can use the value returned by this method as if
     * it represented the physical mouse location.
     * <p>
     * 
     * For an example that uses this method to create points at the current
     * mouse location within a very simple line chart editor, see <a
     * href="package-summary.html#GChartExample22a">the Chart Gallery's
     * GChartExample22a</a>.
     * <p>
     * 
     * 
     * @return the coordinate, projected along this axis, in the scale defined
     *         by this axis, representing the position GChart has currently
     *         "tracked" the mouse to, or <tt>Double.NaN</tt> if GChart has
     *         tracked the mouse right off the edge of the chart.
     * 
     * @see #clientToModel clientToModel
     * @see #modelToClient modelToClient
     * @see #pixelToModel pixelToModel
     * @see #modelToPixel modelToPixel
     * @see GChart#setHoverTouchingEnabled setHoverTouchingEnabled
     * 
     */
    public abstract double getMouseCoordinate();

    /**
     * Returns the number of visible curves displayed on this axis.
     * <p>
     * 
     * @return the number of visible curves on this axis, or <tt>0</tt> if there
     *         are no visible curves on this axis.
     * 
     * @see Axis#setVisible setVisible
     * 
     */
    public int getNCurvesVisibleOnAxis() {
      return nCurvesVisibleOnAxis;
    }

    /**
     * Returns the number of ticks on this axis.
     * 
     * @return the number of ticks on this axis.
     * 
     * @see #setTickCount setTickCount
     * @see #addTick(double) addTick(double)
     * @see #addTick(double,String) addTick(double,String)
     * @see #addTick(double,String,int,int) addTick(double,String,int,int)
     * @see #addTick(double,Widget) addTick(double,Widget)
     * @see #addTick(double,Widget,int,int) addTick(double,Widget,int,int)
     * @see #clearTicks clearTicks
     * 
     */
    public int getTickCount() {
      int result = tickCount;
      if (GChart.NAI == tickCount) {
        Curve c = getSystemCurve(ticksId);
        result = c.getNPoints();
      }
      return result;

    }

    /**
     * Returns the CSS font-weight specification to be used by this axis' tick
     * labels.
     * 
     * @return font-weight of this axis' tick labels
     * 
     * @see #setTickLabelFontWeight setTickLabelFontWeight
     */
    public String getTickLabelFontWeight() {
      return tickLabelFontWeight;
    }

    /**
     * Returns the color of the font used to display the text of the tick labels
     * on this axis.
     * 
     * 
     * @return CSS color string defining the color of the text of the tick
     *         labels for this axis.
     * 
     * @see #setTickLabelFontColor setTickLabelFontColor
     * 
     * @see #DEFAULT_TICK_LABEL_FONT_COLOR DEFAULT_TICK_LABEL_FONT_COLOR
     * 
     * 
     * 
     */
    public String getTickLabelFontColor() {
      return tickLabelFontColor;
    }

    /**
     * Returns the font-family of the font used to render tick labels on this
     * axis.
     * <p>
     * 
     * @return the CSS font-family used to render the tick labels of this axis.
     * 
     * @see #setTickLabelFontFamily setTickLabelFontFamily
     */
    public String getTickLabelFontFamily() {
      return tickLabelFontFamily;
    }

    /**
     * Returns the CSS font size, in pixels, used for tick labels on this axis.
     * 
     * @return the tick label font size in pixels
     * 
     * @see #setTickLabelFontSize setTickLabelFontSize
     */
    public int getTickLabelFontSize() {
      return tickLabelFontSize;
    }

    /**
     * Returns the font-style of the font used to render tick labels on this
     * axis (typically either "italic" or "normal")
     * 
     * @return the CSS font-style in which tick labels of this axis are
     *         rendered.
     * 
     * @see #setTickLabelFontStyle setTickLabelFontStyle
     */
    public String getTickLabelFontStyle() {
      return tickLabelFontStyle;
    }

    /**
     * Returns the tick label numeric format string for this axis.
     * 
     * @return numeric format used to generate tick labels.
     * 
     * @see #setTickLabelFormat setTickLabelFormat
     * 
     */
    public String getTickLabelFormat() {
      return tickLabelFormat;
    }

    /**
     * Returns the amount of padding (blank space) between the ticks and their
     * labels.
     * <p>
     * 
     * @return amount of padding between ticks and their labels, in pixels.
     * 
     * @see #setTickLabelPadding setTickLabelPadding
     * 
     */
    public int getTickLabelPadding() {
      return tickLabelPadding;
    }

    /*
     * Does real work of public getTickLabelThickness; flag saves time
     * during repeated calls made in updateChartDecorations.
     * 
     */ 
    int getTickLabelThickness(boolean needsPopulation) {
      int maxLength = 0;
      int result;
      if (tickLabelThickness != GChart.NAI)
        result = tickLabelThickness;
      else { // use an heuristic to estimate thickness
        if (needsPopulation)
          maybePopulateTicks();
        Curve c = getSystemCurve(ticksId);
        int nTicks = c.getNPoints();
        for (int i = 0; i < nTicks; i++) {
          String tt = c.getPoint(i).getAnnotationText();
          if (null != tt)
            maxLength = Math.max(maxLength,
              Annotation.getNumberOfCharsWide(tt));
        }
        result = (int) Math.round(maxLength * tickLabelFontSize
            * TICK_CHARWIDTH_TO_FONTSIZE_LOWERBOUND);
      }
      return result;
    }

    /**
     * Returns the thickness of the band adjacent to this axis that GChart will
     * allocate to hold this axis' tick labels.
     * <p>
     * 
     * @return width of band, in pixels, GChart will reserve for this axis' tick
     *         labels.
     * 
     * @see #setTickLabelThickness setTickLabelThickness
     * 
     */
    public int getTickLabelThickness() {
      int result = getTickLabelThickness(true);
      return result;
    }

    /**
     * Returns the ratio of the number of ticks to the number of ticks that have
     * an associated gridline.
     * 
     * @return number of ticks per gridline for this axis
     * 
     * @see #setTicksPerGridline setTicksPerGridline
     * 
     */
    public int getTicksPerGridline() {
      return ticksPerGridline;
    }

    /**
     * Returns the ratio of the number of ticks to the number of labeled ticks.
     * 
     * @return number of ticks per label.
     * 
     * @see #setTicksPerLabel setTicksPerLabel
     * 
     */
    public int getTicksPerLabel() {
      return ticksPerLabel;
    }

    /**
     * Returns the length of ticks for this axis.
     * 
     * @return the 
