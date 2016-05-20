/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2011, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------------
 * AbstractRenderer.java
 * ---------------------
 * (C) Copyright 2002-2011, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Nicolas Brodu;
 *
 * Changes:
 * --------
 * 22-Aug-2002 : Version 1, draws code out of AbstractXYItemRenderer to share
 *               with AbstractCategoryItemRenderer (DG);
 * 01-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 06-Nov-2002 : Moved to the com.jrefinery.chart.renderer package (DG);
 * 21-Nov-2002 : Added a paint table for the renderer to use (DG);
 * 17-Jan-2003 : Moved plot classes into a separate package (DG);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 29-Apr-2003 : Added valueLabelFont and valueLabelPaint attributes, based on
 *               code from Arnaud Lelievre (DG);
 * 29-Jul-2003 : Amended code that doesn't compile with JDK 1.2.2 (DG);
 * 13-Aug-2003 : Implemented Cloneable (DG);
 * 15-Sep-2003 : Fixed serialization (NB);
 * 17-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 07-Oct-2003 : Moved PlotRenderingInfo into RendererState to allow for
 *               multiple threads using a single renderer (DG);
 * 20-Oct-2003 : Added missing setOutlinePaint() method (DG);
 * 23-Oct-2003 : Split item label attributes into 'positive' and 'negative'
 *               values (DG);
 * 26-Nov-2003 : Added methods to get the positive and negative item label
 *               positions (DG);
 * 01-Mar-2004 : Modified readObject() method to prevent null pointer exceptions
 *               after deserialization (DG);
 * 19-Jul-2004 : Fixed bug in getItemLabelFont(int, int) method (DG);
 * 04-Oct-2004 : Updated equals() method, eliminated use of NumberUtils,
 *               renamed BooleanUtils --> BooleanUtilities, ShapeUtils -->
 *               ShapeUtilities (DG);
 * 15-Mar-2005 : Fixed serialization of baseFillPaint (DG);
 * 16-May-2005 : Base outline stroke should never be null (DG);
 * 01-Jun-2005 : Added hasListener() method for unit testing (DG);
 * 08-Jun-2005 : Fixed equals() method to handle GradientPaint (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Minor API doc update (DG);
 * 19-Feb-2007 : Fixes for clone() method (DG);
 * 28-Feb-2007 : Use cached event to signal changes (DG);
 * 19-Apr-2007 : Deprecated seriesVisible and seriesVisibleInLegend flags (DG);
 * 20-Apr-2007 : Deprecated paint, fillPaint, outlinePaint, stroke,
 *               outlineStroke, shape, itemLabelsVisible, itemLabelFont,
 *               itemLabelPaint, positiveItemLabelPosition,
 *               negativeItemLabelPosition and createEntities override
 *               fields (DG);
 * 13-Jun-2007 : Added new autoPopulate flags for core series attributes (DG);
 * 23-Oct-2007 : Updated lookup methods to better handle overridden
 *               methods (DG);
 * 04-Dec-2007 : Modified hashCode() implementation (DG);
 * 29-Apr-2008 : Minor API doc update (DG);
 * 17-Jun-2008 : Added legendShape, legendTextFont and legendTextPaint
 *               attributes (DG);
 * 18-Aug-2008 : Added clearSeriesPaints() and clearSeriesStrokes() (DG);
 * 28-Jan-2009 : Equals method doesn't test Shape equality correctly (DG);
 * 27-Mar-2009 : Added dataBoundsIncludesVisibleSeriesOnly attribute, and
 *               updated renderer events for series visibility changes (DG);
 * 01-Apr-2009 : Factored up the defaultEntityRadius field from the
 *               AbstractXYItemRenderer class (DG);
 * 28-Apr-2009 : Added flag to allow a renderer to treat the legend shape as
 *               a line (DG);
 *
 */

package org.jfree.chart.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;

import javax.swing.event.EventListenerList;

import org.jfree.chart.HashUtilities;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.LegendTitle;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.TextAnchor;
import org.jfree.util.BooleanList;
import org.jfree.util.BooleanUtilities;
import org.jfree.util.ObjectList;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintList;
import org.jfree.util.PaintUtilities;
import org.jfree.util.ShapeList;
import org.jfree.util.ShapeUtilities;
import org.jfree.util.StrokeList;

/**
 * Base class providing common services for renderers.  Most methods that update
 * attributes of the renderer will fire a {@link RendererChangeEvent}, which
 * normally means the plot that owns the renderer will receive notification that
 * the renderer has been changed (the plot will, in turn, notify the chart).
 */
public abstract class AbstractRenderer implements Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -828267569428206075L;

    /** Zero represented as a <code>Double</code>. */
    public static final Double ZERO = new Double(0.0);

    /** The default paint. */
    public static final Paint DEFAULT_PAINT = Color.blue;

    /** The default outline paint. */
    public static final Paint DEFAULT_OUTLINE_PAINT = Color.gray;

    /** The default stroke. */
    public static final Stroke DEFAULT_STROKE = new BasicStroke(1.0f);

    /** The default outline stroke. */
    public static final Stroke DEFAULT_OUTLINE_STROKE = new BasicStroke(1.0f);

    /** The default shape. */
    public static final Shape DEFAULT_SHAPE
            = new Rectangle2D.Double(-3.0, -3.0, 6.0, 6.0);

    /** The default value label font. */
    public static final Font DEFAULT_VALUE_LABEL_FONT
            = new Font("SansSerif", Font.PLAIN, 10);

    /** The default value label paint. */
    public static final Paint DEFAULT_VALUE_LABEL_PAINT = Color.black;

    /** A list of flags that controls whether or not each series is visible. */
    private BooleanList seriesVisibleList;

    /** The default visibility for each series. */
    private boolean baseSeriesVisible;

    /**
     * A list of flags that controls whether or not each series is visible in
     * the legend.
     */
    private BooleanList seriesVisibleInLegendList;

    /** The default visibility for each series in the legend. */
    private boolean baseSeriesVisibleInLegend;

    /** The paint list. */
    private PaintList paintList;

    /**
     * A flag that controls whether or not the paintList is auto-populated
     * in the {@link #lookupSeriesPaint(int)} method.
     *
     * @since 1.0.6
     */
    private boolean autoPopulateSeriesPaint;

    /** The base paint. */
    private transient Paint basePaint;

    /** The fill paint list. */
    private PaintList fillPaintList;

    /**
     * A flag that controls whether or not the fillPaintList is auto-populated
     * in the {@link #lookupSeriesFillPaint(int)} method.
     *
     * @since 1.0.6
     */
    private boolean autoPopulateSeriesFillPaint;

    /** The base fill paint. */
    private transient Paint baseFillPaint;

    /** The outline paint list. */
    private PaintList outlinePaintList;

    /**
     * A flag that controls whether or not the outlinePaintList is
     * auto-populated in the {@link #lookupSeriesOutlinePaint(int)} method.
     *
     * @since 1.0.6
     */
    private boolean autoPopulateSeriesOutlinePaint;

    /** The base outline paint. */
    private transient Paint baseOutlinePaint;

    /** The stroke list. */
    private StrokeList strokeList;

    /**
     * A flag that controls whether or not the strokeList is auto-populated
     * in the {@link #lookupSeriesStroke(int)} method.
     *
     * @since 1.0.6
     */
    private boolean autoPopulateSeriesStroke;

    /** The base stroke. */
    private transient Stroke baseStroke;

    /** The outline stroke list. */
    private StrokeList outlineStrokeList;

    /** The base outline stroke. */
    private transient Stroke baseOutlineStroke;

    /**
     * A flag that controls whether or not the outlineStrokeList is
     * auto-populated in the {@link #lookupSeriesOutlineStroke(int)} method.
     *
     * @since 1.0.6
     */
    private boolean autoPopulateSeriesOutlineStroke;

    /** A shape list. */
    private ShapeList shapeList;

    /**
     * A flag that controls whether or not the shapeList is auto-populated
     * in the {@link #lookupSeriesShape(int)} method.
     *
     * @since 1.0.6
     */
    private boolean autoPopulateSeriesShape;

    /** The base shape. */
    private transient Shape baseShape;

    /** Visibility of the item labels PER series. */
    private BooleanList itemLabelsVisibleList;

    /** The base item labels visible. */
    private Boolean baseItemLabelsVisible;

    /** The item label font list (one font per series). */
    private ObjectList itemLabelFontList;

    /** The base item label font. */
    private Font baseItemLabelFont;

    /** The item label paint list (one paint per series). */
    private PaintList itemLabelPaintList;

    /** The base item label paint. */
    private transient Paint baseItemLabelPaint;

    /** The positive item label position (per series). */
    private ObjectList positiveItemLabelPositionList;

    /** The fallback positive item label position. */
    private ItemLabelPosition basePositiveItemLabelPosition;

    /** The negative item label position (per series). */
    private ObjectList negativeItemLabelPositionList;

    /** The fallback negative item label position. */
    private ItemLabelPosition baseNegativeItemLabelPosition;

    /** The item label anchor offset. */
    private double itemLabelAnchorOffset = 2.0;

    /**
     * Flags that control whether or not entities are generated for each
     * series.  This will be overridden by 'createEntities'.
     */
    private BooleanList createEntitiesList;

    /**
     * The default flag that controls whether or not entities are generated.
     * This flag is used when both the above flags return null.
     */
    private boolean baseCreateEntities;

    /**
     * The per-series legend shape settings.
     *
     * @since 1.0.11
     */
    private ShapeList legendShapeList;

    /**
     * The base shape for legend items.  If this is <code>null</code>, the
     * series shape will be used.
     *
     * @since 1.0.11
     */
    private transient Shape baseLegendShape;

    /**
     * A special flag that, if true, will cause the getLegendItem() method
     * to configure the legend shape as if it were a line.
     *
     * @since 1.0.14
     */
    private boolean treatLegendShapeAsLine;

    /**
     * The per-series legend text font.
     *
     * @since 1.0.11
     */
    private ObjectList legendTextFont;

    /**
     * The base legend font.
     *
     * @since 1.0.11
     */
    private Font baseLegendTextFont;

    /**
     * The per series legend text paint settings.
     *
     * @since 1.0.11
     */
    private PaintList legendTextPaint;

    /**
     * The default paint for the legend text items (if this is
     * <code>null</code>, the {@link LegendTitle} class will determine the
     * text paint to use.
     *
     * @since 1.0.11
     */
    private transient Paint baseLegendTextPaint;

    /**
     * A flag that controls whether or not the renderer will include the
     * non-visible series when calculating the data bounds.
     *
     * @since 1.0.13
     */
    private boolean dataBoundsIncludesVisibleSeriesOnly = true;

    /** The default radius for the entity 'hotspot' */
    private int defaultEntityRadius;

    /** Storage for registered change listeners. */
    private transient EventListenerList listenerList;

    /** An event for re-use. */
    private transient RendererChangeEvent event;

    /**
     * Default constructor.
     */
    public AbstractRenderer() {

        this.seriesVisible = null;
        this.seriesVisibleList = new BooleanList();
        this.baseSeriesVisible = true;

        this.seriesVisibleInLegend = null;
        this.seriesVisibleInLegendList = new BooleanList();
        this.baseSeriesVisibleInLegend = true;

        this.paint = null;
        this.paintList = new PaintList();
        this.basePaint = DEFAULT_PAINT;
        this.autoPopulateSeriesPaint = true;

        this.fillPaint = null;
        this.fillPaintList = new PaintList();
        this.baseFillPaint = Color.white;
        this.autoPopulateSeriesFillPaint = false;

        this.outlinePaint = null;
        this.outlinePaintList = new PaintList();
        this.baseOutlinePaint = DEFAULT_OUTLINE_PAINT;
        this.autoPopulateSeriesOutlinePaint = false;

        this.stroke = null;
        this.strokeList = new StrokeList();
        this.baseStroke = DEFAULT_STROKE;
        this.autoPopulateSeriesStroke = true;

        this.outlineStroke = null;
        this.outlineStrokeList = new StrokeList();
        this.baseOutlineStroke = DEFAULT_OUTLINE_STROKE;
        this.autoPopulateSeriesOutlineStroke = false;

        this.shape = null;
        this.shapeList = new ShapeList();
        this.baseShape = DEFAULT_SHAPE;
        this.autoPopulateSeriesShape = true;

        this.itemLabelsVisible = null;
        this.itemLabelsVisibleList = new BooleanList();
        this.baseItemLabelsVisible = Boolean.FALSE;

        this.itemLabelFont = null;
        this.itemLabelFontList = new ObjectList();
        this.baseItemLabelFont = new Font("SansSerif", Font.PLAIN, 10);

        this.itemLabelPaint = null;
        this.itemLabelPaintList = new PaintList();
        this.baseItemLabelPaint = Color.black;

        this.positiveItemLabelPosition = null;
        this.positiveItemLabelPositionList = new ObjectList();
        this.basePositiveItemLabelPosition = new ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER);

        this.negativeItemLabelPosition = null;
        this.negativeItemLabelPositionList = new ObjectList();
        this.baseNegativeItemLabelPosition = new ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER);

        this.createEntities = null;
        this.createEntitiesList = new BooleanList();
        this.baseCreateEntities = true;

        this.defaultEntityRadius = 3;

        this.legendShapeList = new ShapeList();
        this.baseLegendShape = null;

        this.treatLegendShapeAsLine = false;

        this.legendTextFont = new ObjectList();
        this.baseLegendTextFont = null;

        this.legendTextPaint = new PaintList();
        this.baseLegendTextPaint = null;

        this.listenerList = new EventListenerList();

    }

    /**
     * Returns the drawing supplier from the plot.
     *
     * @return The drawing supplier.
     */
    public abstract DrawingSupplier getDrawingSupplier();

    // SERIES VISIBLE (not yet respected by all renderers)

    /**
     * Returns a boolean that indicates whether or not the specified item
     * should be drawn (this is typically used to hide an entire series).
     *
     * @param series  the series index.
     * @param item  the item index.
     *
     * @return A boolean.
     */
    public boolean getItemVisible(int series, int item) {
        return isSeriesVisible(series);
    }

    /**
     * Returns a boolean that indicates whether or not the specified series
     * should be drawn.
     *
     * @param series  the series index.
     *
     * @return A boolean.
     */
    public boolean isSeriesVisible(int series) {
        boolean result = this.baseSeriesVisible;
        if (this.seriesVisible != null) {
            result = this.seriesVisible.booleanValue();
        }
        else {
            Boolean b = this.seriesVisibleList.getBoolean(series);
            if (b != null) {
                result = b.booleanValue();
            }
        }
        return result;
    }

    /**
     * Returns the flag that controls whether a series is visible.
     *
     * @param series  the series index (zero-based).
     *
     * @return The flag (possibly <code>null</code>).
     *
     * @see #setSeriesVisible(int, Boolean)
     */
    public Boolean getSeriesVisible(int series) {
        return this.seriesVisibleList.getBoolean(series);
    }

    /**
     * Sets the flag that controls whether a series is visible and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param visible  the flag (<code>null</code> permitted).
     *
     * @see #getSeriesVisible(int)
     */
    public void setSeriesVisible(int series, Boolean visible) {
        setSeriesVisible(series, visible, true);
    }

    /**
     * Sets the flag that controls whether a series is visible and, if
     * requested, sends a {@link RendererChangeEvent} to all registered
     * listeners.
     *
     * @param series  the series index.
     * @param visible  the flag (<code>null</code> permitted).
     * @param notify  notify listeners?
     *
     * @see #getSeriesVisible(int)
     */
    public void setSeriesVisible(int series, Boolean visible, boolean notify) {
        this.seriesVisibleList.setBoolean(series, visible);
        if (notify) {
            // we create an event with a special flag set...the purpose of
            // this is to communicate to the plot (the default receiver of
            // the event) that series visibility has changed so the axis
            // ranges might need updating...
            RendererChangeEvent e = new RendererChangeEvent(this, true);
            notifyListeners(e);
        }
    }

    /**
     * Returns the base visibility for all series.
     *
     * @return The base visibility.
     *
     * @see #setBaseSeriesVisible(boolean)
     */
    public boolean getBaseSeriesVisible() {
        return this.baseSeriesVisible;
    }

    /**
     * Sets the base visibility and sends a {@link RendererChangeEvent}
     * to all registered listeners.
     *
     * @param visible  the flag.
     *
     * @see #getBaseSeriesVisible()
     */
    public void setBaseSeriesVisible(boolean visible) {
        // defer argument checking...
        setBaseSeriesVisible(visible, true);
    }

    /**
     * Sets the base visibility and, if requested, sends
     * a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param visible  the visibility.
     * @param notify  notify listeners?
     *
     * @see #getBaseSeriesVisible()
     */
    public void setBaseSeriesVisible(boolean visible, boolean notify) {
        this.baseSeriesVisible = visible;
        if (notify) {
            // we create an event with a special flag set...the purpose of
            // this is to communicate to the plot (the default receiver of
            // the event) that series visibility has changed so the axis
            // ranges might need updating...
            RendererChangeEvent e = new RendererChangeEvent(this, true);
            notifyListeners(e);
        }
    }

    // SERIES VISIBLE IN LEGEND (not yet respected by all renderers)

    /**
     * Returns <code>true</code> if the series should be shown in the legend,
     * and <code>false</code> otherwise.
     *
     * @param series  the series index.
     *
     * @return A boolean.
     */
    public boolean isSeriesVisibleInLegend(int series) {
        boolean result = this.baseSeriesVisibleInLegend;
        if (this.seriesVisibleInLegend != null) {
            result = this.seriesVisibleInLegend.booleanValue();
        }
        else {
            Boolean b = this.seriesVisibleInLegendList.getBoolean(series);
            if (b != null) {
                result = b.booleanValue();
            }
        }
        return result;
    }

    /**
     * Returns the flag that controls whether a series is visible in the
     * legend.  This method returns only the "per series" settings - to
     * incorporate the override and base settings as well, you need to use the
     * {@link #isSeriesVisibleInLegend(int)} method.
     *
     * @param series  the series index (zero-based).
     *
     * @return The flag (possibly <code>null</code>).
     *
     * @see #setSeriesVisibleInLegend(int, Boolean)
     */
    public Boolean getSeriesVisibleInLegend(int series) {
        return this.seriesVisibleInLegendList.getBoolean(series);
    }

    /**
     * Sets the flag that controls whether a series is visible in the legend
     * and sends a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param visible  the flag (<code>null</code> permitted).
     *
     * @see #getSeriesVisibleInLegend(int)
     */
    public void setSeriesVisibleInLegend(int series, Boolean visible) {
        setSeriesVisibleInLegend(series, visible, true);
    }

    /**
     * Sets the flag that controls whether a series is visible in the legend
     * and, if requested, sends a {@link RendererChangeEvent} to all registered
     * listeners.
     *
     * @param series  the series index.
     * @param visible  the flag (<code>null</code> permitted).
     * @param notify  notify listeners?
     *
     * @see #getSeriesVisibleInLegend(int)
     */
    public void setSeriesVisibleInLegend(int series, Boolean visible,
                                         boolean notify) {
        this.seriesVisibleInLegendList.setBoolean(series, visible);
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the base visibility in the legend for all series.
     *
     * @return The base visibility.
     *
     * @see #setBaseSeriesVisibleInLegend(boolean)
     */
    public boolean getBaseSeriesVisibleInLegend() {
        return this.baseSeriesVisibleInLegend;
    }

    /**
     * Sets the base visibility in the legend and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param visible  the flag.
     *
     * @see #getBaseSeriesVisibleInLegend()
     */
    public void setBaseSeriesVisibleInLegend(boolean visible) {
        // defer argument checking...
        setBaseSeriesVisibleInLegend(visible, true);
    }

    /**
     * Sets the base visibility in the legend and, if requested, sends
     * a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param visible  the visibility.
     * @param notify  notify listeners?
     *
     * @see #getBaseSeriesVisibleInLegend()
     */
    public void setBaseSeriesVisibleInLegend(boolean visible, boolean notify) {
        this.baseSeriesVisibleInLegend = visible;
        if (notify) {
            fireChangeEvent();
        }
    }

    // PAINT

    /**
     * Returns the paint used to fill data items as they are drawn.
     * <p>
     * The default implementation passes control to the
     * <code>lookupSeriesPaint()</code> method. You can override this method
     * if you require different behaviour.
     *
     * @param row  the row (or series) index (zero-based).
     * @param column  the column (or category) index (zero-based).
     *
     * @return The paint (never <code>null</code>).
     */
    public Paint getItemPaint(int row, int column) {
        return lookupSeriesPaint(row);
    }

    /**
     * Returns the paint used to fill an item drawn by the renderer.
     *
     * @param series  the series index (zero-based).
     *
     * @return The paint (never <code>null</code>).
     *
     * @since 1.0.6
     */
    public Paint lookupSeriesPaint(int series) {

        // return the override, if there is one...
        if (this.paint != null) {
            return this.paint;
        }

        // otherwise look up the paint list
        Paint seriesPaint = getSeriesPaint(series);
        if (seriesPaint == null && this.autoPopulateSeriesPaint) {
            DrawingSupplier supplier = getDrawingSupplier();
            if (supplier != null) {
                seriesPaint = supplier.getNextPaint();
                setSeriesPaint(series, seriesPaint, false);
            }
        }
        if (seriesPaint == null) {
            seriesPaint = this.basePaint;
        }
        return seriesPaint;

    }

    /**
     * Returns the paint used to fill an item drawn by the renderer.
     *
     * @param series  the series index (zero-based).
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setSeriesPaint(int, Paint)
     */
    public Paint getSeriesPaint(int series) {
        return this.paintList.getPaint(series);
    }

    /**
     * Sets the paint used for a series and sends a {@link RendererChangeEvent}
     * to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getSeriesPaint(int)
     */
    public void setSeriesPaint(int series, Paint paint) {
        setSeriesPaint(series, paint, true);
    }

    /**
     * Sets the paint used for a series and, if requested, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index.
     * @param paint  the paint (<code>null</code> permitted).
     * @param notify  notify listeners?
     *
     * @see #getSeriesPaint(int)
     */
    public void setSeriesPaint(int series, Paint paint, boolean notify) {
        this.paintList.setPaint(series, paint);
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Clears the series paint settings for this renderer and, if requested,
     * sends a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param notify  notify listeners?
     *
     * @since 1.0.11
     */
    public void clearSeriesPaints(boolean notify) {
        this.paintList.clear();
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the base paint.
     *
     * @return The base paint (never <code>null</code>).
     *
     * @see #setBasePaint(Paint)
     */
    public Paint getBasePaint() {
        return this.basePaint;
    }

    /**
     * Sets the base paint and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getBasePaint()
     */
    public void setBasePaint(Paint paint) {
        // defer argument checking...
        setBasePaint(paint, true);
    }

    /**
     * Sets the base paint and, if requested, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     * @param notify  notify listeners?
     *
     * @see #getBasePaint()
     */
    public void setBasePaint(Paint paint, boolean notify) {
        this.basePaint = paint;
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the flag that controls whether or not the series paint list is
     * automatically populated when {@link #lookupSeriesPaint(int)} is called.
     *
     * @return A boolean.
     *
     * @since 1.0.6
     *
     * @see #setAutoPopulateSeriesPaint(boolean)
     */
    public boolean getAutoPopulateSeriesPaint() {
        return this.autoPopulateSeriesPaint;
    }

    /**
     * Sets the flag that controls whether or not the series paint list is
     * automatically populated when {@link #lookupSeriesPaint(int)} is called.
     *
     * @param auto  the new flag value.
     *
     * @since 1.0.6
     *
     * @see #getAutoPopulateSeriesPaint()
     */
    public void setAutoPopulateSeriesPaint(boolean auto) {
        this.autoPopulateSeriesPaint = auto;
    }

    //// FILL PAINT //////////////////////////////////////////////////////////

    /**
     * Returns the paint used to fill data items as they are drawn.  The
     * default implementation passes control to the
     * {@link #lookupSeriesFillPaint(int)} method - you can override this
     * method if you require different behaviour.
     *
     * @param row  the row (or series) index (zero-based).
     * @param column  the column (or category) index (zero-based).
     *
     * @return The paint (never <code>null</code>).
     */
    public Paint getItemFillPaint(int row, int column) {
        return lookupSeriesFillPaint(row);
    }

    /**
     * Returns the paint used to fill an item drawn by the renderer.
     *
     * @param series  the series (zero-based index).
     *
     * @return The paint (never <code>null</code>).
     *
     * @since 1.0.6
     */
    public Paint lookupSeriesFillPaint(int series) {

        // return the override, if there is one...
        if (this.fillPaint != null) {
            return this.fillPaint;
        }

        // otherwise look up the paint table
        Paint seriesFillPaint = getSeriesFillPaint(series);
        if (seriesFillPaint == null && this.autoPopulateSeriesFillPaint) {
            DrawingSupplier supplier = getDrawingSupplier();
            if (supplier != null) {
                seriesFillPaint = supplier.getNextFillPaint();
                setSeriesFillPaint(series, seriesFillPaint, false);
            }
        }
        if (seriesFillPaint == null) {
            seriesFillPaint = this.baseFillPaint;
        }
        return seriesFillPaint;

    }

    /**
     * Returns the paint used to fill an item drawn by the renderer.
     *
     * @param series  the series (zero-based index).
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setSeriesFillPaint(int, Paint)
     */
    public Paint getSeriesFillPaint(int series) {
        return this.fillPaintList.getPaint(series);
    }

    /**
     * Sets the paint used for a series fill and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getSeriesFillPaint(int)
     */
    public void setSeriesFillPaint(int series, Paint paint) {
        setSeriesFillPaint(series, paint, true);
    }

    /**
     * Sets the paint used to fill a series and, if requested,
     * sends a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param paint  the paint (<code>null</code> permitted).
     * @param notify  notify listeners?
     *
     * @see #getSeriesFillPaint(int)
     */
    public void setSeriesFillPaint(int series, Paint paint, boolean notify) {
        this.fillPaintList.setPaint(series, paint);
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the base fill paint.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setBaseFillPaint(Paint)
     */
    public Paint getBaseFillPaint() {
        return this.baseFillPaint;
    }

    /**
     * Sets the base fill paint and sends a {@link RendererChangeEvent} to
     * all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getBaseFillPaint()
     */
    public void setBaseFillPaint(Paint paint) {
        // defer argument checking...
        setBaseFillPaint(paint, true);
    }

    /**
     * Sets the base fill paint and, if requested, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     * @param notify  notify listeners?
     *
     * @see #getBaseFillPaint()
     */
    public void setBaseFillPaint(Paint paint, boolean notify) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.baseFillPaint = paint;
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the flag that controls whether or not the series fill paint list
     * is automatically populated when {@link #lookupSeriesFillPaint(int)} is
     * called.
     *
     * @return A boolean.
     *
     * @since 1.0.6
     *
     * @see #setAutoPopulateSeriesFillPaint(boolean)
     */
    public boolean getAutoPopulateSeriesFillPaint() {
        return this.autoPopulateSeriesFillPaint;
    }

    /**
     * Sets the flag that controls whether or not the series fill paint list is
     * automatically populated when {@link #lookupSeriesFillPaint(int)} is
     * called.
     *
     * @param auto  the new flag value.
     *
     * @since 1.0.6
     *
     * @see #getAutoPopulateSeriesFillPaint()
     */
    public void setAutoPopulateSeriesFillPaint(boolean auto) {
        this.autoPopulateSeriesFillPaint = auto;
    }

    // OUTLINE PAINT //////////////////////////////////////////////////////////

    /**
     * Returns the paint used to outline data items as they are drawn.
     * <p>
     * The default implementation passes control to the
     * {@link #lookupSeriesOutlinePaint} method.  You can override this method
     * if you require different behaviour.
     *
     * @param row  the row (or series) index (zero-based).
     * @param column  the column (or category) index (zero-based).
     *
     * @return The paint (never <code>null</code>).
     */
    public Paint getItemOutlinePaint(int row, int column) {
        return lookupSeriesOutlinePaint(row);
    }

    /**
     * Returns the paint used to outline an item drawn by the renderer.
     *
     * @param series  the series (zero-based index).
     *
     * @return The paint (never <code>null</code>).
     *
     * @since 1.0.6
     */
    public Paint lookupSeriesOutlinePaint(int series) {

        // return the override, if there is one...
        if (this.outlinePaint != null) {
            return this.outlinePaint;
        }

        // otherwise look up the paint table
        Paint seriesOutlinePaint = getSeriesOutlinePaint(series);
        if (seriesOutlinePaint == null && this.autoPopulateSeriesOutlinePaint) {
            DrawingSupplier supplier = getDrawingSupplier();
            if (supplier != null) {
                seriesOutlinePaint = supplier.getNextOutlinePaint();
                setSeriesOutlinePaint(series, seriesOutlinePaint, false);
            }
        }
        if (seriesOutlinePaint == null) {
            seriesOutlinePaint = this.baseOutlinePaint;
        }
        return seriesOutlinePaint;

    }

    /**
     * Returns the paint used to outline an item drawn by the renderer.
     *
     * @param series  the series (zero-based index).
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setSeriesOutlinePaint(int, Paint)
     */
    public Paint getSeriesOutlinePaint(int series) {
        return this.outlinePaintList.getPaint(series);
    }

    /**
     * Sets the paint used for a series outline and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getSeriesOutlinePaint(int)
     */
    public void setSeriesOutlinePaint(int series, Paint paint) {
        setSeriesOutlinePaint(series, paint, true);
    }

    /**
     * Sets the paint used to draw the outline for a series and, if requested,
     * sends a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param paint  the paint (<code>null</code> permitted).
     * @param notify  notify listeners?
     *
     * @see #getSeriesOutlinePaint(int)
     */
    public void setSeriesOutlinePaint(int series, Paint paint, boolean notify) {
        this.outlinePaintList.setPaint(series, paint);
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the base outline paint.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setBaseOutlinePaint(Paint)
     */
    public Paint getBaseOutlinePaint() {
        return this.baseOutlinePaint;
    }

    /**
     * Sets the base outline paint and sends a {@link RendererChangeEvent} to
     * all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getBaseOutlinePaint()
     */
    public void setBaseOutlinePaint(Paint paint) {
        // defer argument checking...
        setBaseOutlinePaint(paint, true);
    }

    /**
     * Sets the base outline paint and, if requested, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     * @param notify  notify listeners?
     *
     * @see #getBaseOutlinePaint()
     */
    public void setBaseOutlinePaint(Paint paint, boolean notify) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.baseOutlinePaint = paint;
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the flag that controls whether or not the series outline paint
     * list is automatically populated when
     * {@link #lookupSeriesOutlinePaint(int)} is called.
     *
     * @return A boolean.
     *
     * @since 1.0.6
     *
     * @see #setAutoPopulateSeriesOutlinePaint(boolean)
     */
    public boolean getAutoPopulateSeriesOutlinePaint() {
        return this.autoPopulateSeriesOutlinePaint;
    }

    /**
     * Sets the flag that controls whether or not the series outline paint list
     * is automatically populated when {@link #lookupSeriesOutlinePaint(int)}
     * is called.
     *
     * @param auto  the new flag value.
     *
     * @since 1.0.6
     *
     * @see #getAutoPopulateSeriesOutlinePaint()
     */
    public void setAutoPopulateSeriesOutlinePaint(boolean auto) {
        this.autoPopulateSeriesOutlinePaint = auto;
    }

    // STROKE

    /**
     * Returns the stroke used to draw data items.
     * <p>
     * The default implementation passes control to the getSeriesStroke method.
     * You can override this method if you require different behaviour.
     *
     * @param row  the row (or series) index (zero-based).
     * @param column  the column (or category) index (zero-based).
     *
     * @return The stroke (never <code>null</code>).
     */
    public Stroke getItemStroke(int row, int column) {
        return lookupSeriesStroke(row);
    }

    /**
     * Returns the stroke used to draw the items in a series.
     *
     * @param series  the series (zero-based index).
     *
     * @return The stroke (never <code>null</code>).
     *
     * @since 1.0.6
     */
    public Stroke lookupSeriesStroke(int series) {

        // return the override, if there is one...
        if (this.stroke != null) {
            return this.stroke;
        }

        // otherwise look up the paint table
        Stroke result = getSeriesStroke(series);
        if (result == null && this.autoPopulateSeriesStroke) {
            DrawingSupplier supplier = getDrawingSupplier();
            if (supplier != null) {
                result = supplier.getNextStroke();
                setSeriesStroke(series, result, false);
            }
        }
        if (result == null) {
            result = this.baseStroke;
        }
        return result;

    }

    /**
     * Returns the stroke used to draw the items in a series.
     *
     * @param series  the series (zero-based index).
     *
     * @return The stroke (possibly <code>null</code>).
     *
     * @see #setSeriesStroke(int, Stroke)
     */
    public Stroke getSeriesStroke(int series) {
        return this.strokeList.getStroke(series);
    }

    /**
     * Sets the stroke used for a series and sends a {@link RendererChangeEvent}
     * to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param stroke  the stroke (<code>null</code> permitted).
     *
     * @see #getSeriesStroke(int)
     */
    public void setSeriesStroke(int series, Stroke stroke) {
        setSeriesStroke(series, stroke, true);
    }

    /**
     * Sets the stroke for a series and, if requested, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param stroke  the stroke (<code>null</code> permitted).
     * @param notify  notify listeners?
     *
     * @see #getSeriesStroke(int)
     */
    public void setSeriesStroke(int series, Stroke stroke, boolean notify) {
        this.strokeList.setStroke(series, stroke);
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Clears the series stroke settings for this renderer and, if requested,
     * sends a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param notify  notify listeners?
     *
     * @since 1.0.11
     */
    public void clearSeriesStrokes(boolean notify) {
        this.strokeList.clear();
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the base stroke.
     *
     * @return The base stroke (never <code>null</code>).
     *
     * @see #setBaseStroke(Stroke)
     */
    public Stroke getBaseStroke() {
        return this.baseStroke;
    }

    /**
     * Sets the base stroke and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @param stroke  the stroke (<code>null</code> not permitted).
     *
     * @see #getBaseStroke()
     */
    public void setBaseStroke(Stroke stroke) {
        // defer argument checking...
        setBaseStroke(stroke, true);
    }

    /**
     * Sets the base stroke and, if requested, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param stroke  the stroke (<code>null</code> not permitted).
     * @param notify  notify listeners?
     *
     * @see #getBaseStroke()
     */
    public void setBaseStroke(Stroke stroke, boolean notify) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        this.baseStroke = stroke;
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the flag that controls whether or not the series stroke list is
     * automatically populated when {@link #lookupSeriesStroke(int)} is called.
     *
     * @return A boolean.
     *
     * @since 1.0.6
     *
     * @see #setAutoPopulateSeriesStroke(boolean)
     */
    public boolean getAutoPopulateSeriesStroke() {
        return this.autoPopulateSeriesStroke;
    }

    /**
     * Sets the flag that controls whether or not the series stroke list is
     * automatically populated when {@link #lookupSeriesStroke(int)} is called.
     *
     * @param auto  the new flag value.
     *
     * @since 1.0.6
     *
     * @see #getAutoPopulateSeriesStroke()
     */
    public void setAutoPopulateSeriesStroke(boolean auto) {
        this.autoPopulateSeriesStroke = auto;
    }

    // OUTLINE STROKE

    /**
     * Returns the stroke used to outline data items.  The default
     * implementation passes control to the
     * {@link #lookupSeriesOutlineStroke(int)} method. You can override this
     * method if you require different behaviour.
     *
     * @param row  the row (or series) index (zero-based).
     * @param column  the column (or category) index (zero-based).
     *
     * @return The stroke (never <code>null</code>).
     */
    public Stroke getItemOutlineStroke(int row, int column) {
        return lookupSeriesOutlineStroke(row);
    }

    /**
     * Returns the stroke used to outline the items in a series.
     *
     * @param series  the series (zero-based index).
     *
     * @return The stroke (never <code>null</code>).
     *
     * @since 1.0.6
     */
    public Stroke lookupSeriesOutlineStroke(int series) {

        // return the override, if there is one...
        if (this.outlineStroke != null) {
            return this.outlineStroke;
        }

        // otherwise look up the stroke table
        Stroke result = getSeriesOutlineStroke(series);
        if (result == null && this.autoPopulateSeriesOutlineStroke) {
            DrawingSupplier supplier = getDrawingSupplier();
            if (supplier != null) {
                result = supplier.getNextOutlineStroke();
                setSeriesOutlineStroke(series, result, false);
            }
        }
        if (result == null) {
            result = this.baseOutlineStroke;
        }
        return result;

    }

    /**
     * Returns the stroke used to outline the items in a series.
     *
     * @param series  the series (zero-based index).
     *
     * @return The stroke (possibly <code>null</code>).
     *
     * @see #setSeriesOutlineStroke(int, Stroke)
     */
    public Stroke getSeriesOutlineStroke(int series) {
        return this.outlineStrokeList.getStroke(series);
    }

    /**
     * Sets the outline stroke used for a series and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param stroke  the stroke (<code>null</code> permitted).
     *
     * @see #getSeriesOutlineStroke(int)
     */
    public void setSeriesOutlineStroke(int series, Stroke stroke) {
        setSeriesOutlineStroke(series, stroke, true);
    }

    /**
     * Sets the outline stroke for a series and, if requested, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index.
     * @param stroke  the stroke (<code>null</code> permitted).
     * @param notify  notify listeners?
     *
     * @see #getSeriesOutlineStroke(int)
     */
    public void setSeriesOutlineStroke(int series, Stroke stroke,
                                       boolean notify) {
        this.outlineStrokeList.setStroke(series, stroke);
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the base outline stroke.
     *
     * @return The stroke (never <code>null</code>).
     *
     * @see #setBaseOutlineStroke(Stroke)
     */
    public Stroke getBaseOutlineStroke() {
        return this.baseOutlineStroke;
    }

    /**
     * Sets the base outline stroke and sends a {@link RendererChangeEvent} to
     * all registered listeners.
     *
     * @param stroke  the stroke (<code>null</code> not permitted).
     *
     * @see #getBaseOutlineStroke()
     */
    public void setBaseOutlineStroke(Stroke stroke) {
        setBaseOutlineStroke(stroke, true);
    }

    /**
     * Sets the base outline stroke and, if requested, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param stroke  the stroke (<code>null</code> not permitted).
     * @param notify  a flag that controls whether or not listeners are
     *                notified.
     *
     * @see #getBaseOutlineStroke()
     */
    public void setBaseOutlineStroke(Stroke stroke, boolean notify) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        this.baseOutlineStroke = stroke;
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the flag that controls whether or not the series outline stroke
     * list is automatically populated when
     * {@link #lookupSeriesOutlineStroke(int)} is called.
     *
     * @return A boolean.
     *
     * @since 1.0.6
     *
     * @see #setAutoPopulateSeriesOutlineStroke(boolean)
     */
    public boolean getAutoPopulateSeriesOutlineStroke() {
        return this.autoPopulateSeriesOutlineStroke;
    }

    /**
     * Sets the flag that controls whether or not the series outline stroke list
     * is automatically populated when {@link #lookupSeriesOutlineStroke(int)}
     * is called.
     *
     * @param auto  the new flag value.
     *
     * @since 1.0.6
     *
     * @see #getAutoPopulateSeriesOutlineStroke()
     */
    public void setAutoPopulateSeriesOutlineStroke(boolean auto) {
        this.autoPopulateSeriesOutlineStroke = auto;
    }

    // SHAPE

    /**
     * Returns a shape used to represent a data item.
     * <p>
     * The default implementation passes control to the getSeriesShape method.
     * You can override this method if you require different behaviour.
     *
     * @param row  the row (or series) index (zero-based).
     * @param column  the column (or category) index (zero-based).
     *
     * @return The shape (never <code>null</code>).
     */
    public Shape getItemShape(int row, int column) {
        return lookupSeriesShape(row);
    }

    /**
     * Returns a shape used to represent the items in a series.
     *
     * @param series  the series (zero-based index).
     *
     * @return The shape (never <code>null</code>).
     *
     * @since 1.0.6
     */
    public Shape lookupSeriesShape(int series) {

        // return the override, if there is one...
        if (this.shape != null) {
            return this.shape;
        }

        // otherwise look up the shape list
        Shape result = getSeriesShape(series);
        if (result == null && this.autoPopulateSeriesShape) {
            DrawingSupplier supplier = getDrawingSupplier();
            if (supplier != null) {
                result = supplier.getNextShape();
                setSeriesShape(series, result, false);
            }
        }
        if (result == null) {
            result = this.baseShape;
        }
        return result;

    }

    /**
     * Returns a shape used to represent the items in a series.
     *
     * @param series  the series (zero-based index).
     *
     * @return The shape (possibly <code>null</code>).
     *
     * @see #setSeriesShape(int, Shape)
     */
    public Shape getSeriesShape(int series) {
        return this.shapeList.getShape(series);
    }

    /**
     * Sets the shape used for a series and sends a {@link RendererChangeEvent}
     * to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param shape  the shape (<code>null</code> permitted).
     *
     * @see #getSeriesShape(int)
     */
    public void setSeriesShape(int series, Shape shape) {
        setSeriesShape(series, shape, true);
    }

    /**
     * Sets the shape for a series and, if requested, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero based).
     * @param shape  the shape (<code>null</code> permitted).
     * @param notify  notify listeners?
     *
     * @see #getSeriesShape(int)
     */
    public void setSeriesShape(int series, Shape shape, boolean notify) {
        this.shapeList.setShape(series, shape);
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the base shape.
     *
     * @return The shape (never <code>null</code>).
     *
     * @see #setBaseShape(Shape)
     */
    public Shape getBaseShape() {
        return this.baseShape;
    }

    /**
     * Sets the base shape and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @param shape  the shape (<code>null</code> not permitted).
     *
     * @see #getBaseShape()
     */
    public void setBaseShape(Shape shape) {
        // defer argument checking...
        setBaseShape(shape, true);
    }

    /**
     * Sets the base shape and, if requested, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param shape  the shape (<code>null</code> not permitted).
     * @param notify  notify listeners?
     *
     * @see #getBaseShape()
     */
    public void setBaseShape(Shape shape, boolean notify) {
        if (shape == null) {
            throw new IllegalArgumentException("Null 'shape' argument.");
        }
        this.baseShape = shape;
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the flag that controls whether or not the series shape list is
     * automatically populated when {@link #lookupSeriesShape(int)} is called.
     *
     * @return A boolean.
     *
     * @since 1.0.6
     *
     * @see #setAutoPopulateSeriesShape(boolean)
     */
    public boolean getAutoPopulateSeriesShape() {
        return this.autoPopulateSeriesShape;
    }

    /**
     * Sets the flag that controls whether or not the series shape list is
     * automatically populated when {@link #lookupSeriesShape(int)} is called.
     *
     * @param auto  the new flag value.
     *
     * @since 1.0.6
     *
     * @see #getAutoPopulateSeriesShape()
     */
    public void setAutoPopulateSeriesShape(boolean auto) {
        this.autoPopulateSeriesShape = auto;
    }

    // ITEM LABEL VISIBILITY...

    /**
     * Returns <code>true</code> if an item label is visible, and
     * <code>false</code> otherwise.
     *
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return A boolean.
     */
    public boolean isItemLabelVisible(int row, int column) {
        return isSeriesItemLabelsVisible(row);
    }

    /**
     * Returns <code>true</code> if the item labels for a series are visible,
     * and <code>false</code> otherwise.
     *
     * @param series  the series index (zero-based).
     *
     * @return A boolean.
     */
    public boolean isSeriesItemLabelsVisible(int series) {

        // return the override, if there is one...
        if (this.itemLabelsVisible != null) {
            return this.itemLabelsVisible.booleanValue();
        }

        // otherwise look up the boolean table
        Boolean b = this.itemLabelsVisibleList.getBoolean(series);
        if (b == null) {
            b = this.baseItemLabelsVisible;
        }
        if (b == null) {
            b = Boolean.FALSE;
        }
        return b.booleanValue();

    }

    /**
     * Sets a flag that controls the visibility of the item labels for a series,
     * and sends a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param visible  the flag.
     */
    public void setSeriesItemLabelsVisible(int series, boolean visible) {
        setSeriesItemLabelsVisible(series, BooleanUtilities.valueOf(visible));
    }

    /**
     * Sets the visibility of the item labels for a series and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param visible  the flag (<code>null</code> permitted).
     */
    public void setSeriesItemLabelsVisible(int series, Boolean visible) {
        setSeriesItemLabelsVisible(series, visible, true);
    }

    /**
     * Sets the visibility of item labels for a series and, if requested, sends
     * a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param visible  the visible flag.
     * @param notify  a flag that controls whether or not listeners are
     *                notified.
     */
    public void setSeriesItemLabelsVisible(int series, Boolean visible,
                                           boolean notify) {
        this.itemLabelsVisibleList.setBoolean(series, visible);
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the base setting for item label visibility.  A <code>null</code>
     * result should be interpreted as equivalent to <code>Boolean.FALSE</code>.
     *
     * @return A flag (possibly <code>null</code>).
     *
     * @see #setBaseItemLabelsVisible(boolean)
     */
    public Boolean getBaseItemLabelsVisible() {
        // this should have been defined as a boolean primitive, because
        // allowing null values is a nuisance...but it is part of the final
        // API now, so we'll have to support it.
        return this.baseItemLabelsVisible;
    }

    /**
     * Sets the base flag that controls whether or not item labels are visible,
     * and sends a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param visible  the flag.
     *
     * @see #getBaseItemLabelsVisible()
     */
    public void setBaseItemLabelsVisible(boolean visible) {
        setBaseItemLabelsVisible(BooleanUtilities.valueOf(visible));
    }

    /**
     * Sets the base setting for item label visibility and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param visible  the flag (<code>null</code> is permitted, and viewed
     *     as equivalent to <code>Boolean.FALSE</code>).
     */
    public void setBaseItemLabelsVisible(Boolean visible) {
        setBaseItemLabelsVisible(visible, true);
    }

    /**
     * Sets the base visibility for item labels and, if requested, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param visible  the flag (<code>null</code> is permitted, and viewed
     *     as equivalent to <code>Boolean.FALSE</code>).
     * @param notify  a flag that controls whether or not listeners are
     *                notified.
     *
     * @see #getBaseItemLabelsVisible()
     */
    public void setBaseItemLabelsVisible(Boolean visible, boolean notify) {
        this.baseItemLabelsVisible = visible;
        if (notify) {
            fireChangeEvent();
        }
    }

    //// ITEM LABEL FONT //////////////////////////////////////////////////////

    /**
     * Returns the font for an item label.
     *
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return The font (never <code>null</code>).
     */
    public Font getItemLabelFont(int row, int column) {
        Font result = this.itemLabelFont;
        if (result == null) {
            result = getSeriesItemLabelFont(row);
            if (result == null) {
                result = this.baseItemLabelFont;
            }
        }
        return result;
    }

    /**
     * Returns the font for all the item labels in a series.
     *
     * @param series  the series index (zero-based).
     *
     * @return The font (possibly <code>null</code>).
     *
     * @see #setSeriesItemLabelFont(int, Font)
     */
    public Font getSeriesItemLabelFont(int series) {
        return (Font) this.itemLabelFontList.get(series);
    }

    /**
     * Sets the item label font for a series and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param font  the font (<code>null</code> permitted).
     *
     * @see #getSeriesItemLabelFont(int)
     */
    public void setSeriesItemLabelFont(int series, Font font) {
        setSeriesItemLabelFont(series, font, true);
    }

    /**
     * Sets the item label font for a series and, if requested, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero based).
     * @param font  the font (<code>null</code> permitted).
     * @param notify  a flag that controls whether or not listeners are
     *                notified.
     *
     * @see #getSeriesItemLabelFont(int)
     */
    public void setSeriesItemLabelFont(int series, Font font, boolean notify) {
        this.itemLabelFontList.set(series, font);
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the base item label font (this is used when no other font
     * setting is available).
     *
     * @return The font (<code>never</code> null).
     *
     * @see #setBaseItemLabelFont(Font)
     */
    public Font getBaseItemLabelFont() {
        return this.baseItemLabelFont;
    }

    /**
     * Sets the base item label font and sends a {@link RendererChangeEvent} to
     * all registered listeners.
     *
     * @param font  the font (<code>null</code> not permitted).
     *
     * @see #getBaseItemLabelFont()
     */
    public void setBaseItemLabelFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("Null 'font' argument.");
        }
        setBaseItemLabelFont(font, true);
    }

    /**
     * Sets the base item label font and, if requested, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param font  the font (<code>null</code> not permitted).
     * @param notify  a flag that controls whether or not listeners are
     *                notified.
     *
     * @see #getBaseItemLabelFont()
     */
    public void setBaseItemLabelFont(Font font, boolean notify) {
        this.baseItemLabelFont = font;
        if (notify) {
            fireChangeEvent();
        }
    }

    //// ITEM LABEL PAINT  ////////////////////////////////////////////////////

    /**
     * Returns the paint used to draw an item label.
     *
     * @param row  the row index (zero based).
     * @param column  the column index (zero based).
     *
     * @return The paint (never <code>null</code>).
     */
    public Paint getItemLabelPaint(int row, int column) {
        Paint result = this.itemLabelPaint;
        if (result == null) {
            result = getSeriesItemLabelPaint(row);
            if (result == null) {
                result = this.baseItemLabelPaint;
            }
        }
        return result;
    }

    /**
     * Returns the paint used to draw the item labels for a series.
     *
     * @param series  the series index (zero based).
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setSeriesItemLabelPaint(int, Paint)
     */
    public Paint getSeriesItemLabelPaint(int series) {
        return this.itemLabelPaintList.getPaint(series);
    }

    /**
     * Sets the item label paint for a series and sends a
     * {@link Rende
