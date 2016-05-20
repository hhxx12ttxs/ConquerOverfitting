/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2005, by Object Refinery Limited and Contributors.
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
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * -----------
 * XYPlot.java
 * -----------
 * (C) Copyright 2000-2005, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Craig MacFarlane;
 *                   Mark Watson (www.markwatson.com);
 *                   Jonathan Nash;
 *                   Gideon Krause;
 *                   Klaus Rheinwald;
 *                   Xavier Poinsard;
 *                   Richard Atkinson;
 *                   Arnaud Lelievre;
 *                   Nicolas Brodu;
 *                   Eduardo Ramalho;
 *
 * $Id: XYPlot.java,v 1.44.2.4 2005/10/25 20:52:07 mungady Exp $
 *
 * Changes (from 21-Jun-2001)
 * --------------------------
 * 21-Jun-2001 : Removed redundant JFreeChart parameter from constructors (DG);
 * 18-Sep-2001 : Updated header and fixed DOS encoding problem (DG);
 * 15-Oct-2001 : Data source classes moved to com.jrefinery.data.* (DG);
 * 19-Oct-2001 : Removed the code for drawing the visual representation of each
 *               data point into a separate class StandardXYItemRenderer.
 *               This will make it easier to add variations to the way the
 *               charts are drawn.  Based on code contributed by Mark
 *               Watson (DG);
 * 22-Oct-2001 : Renamed DataSource.java --> Dataset.java etc. (DG);
 * 20-Nov-2001 : Fixed clipping bug that shows up when chart is displayed
 *               inside JScrollPane (DG);
 * 12-Dec-2001 : Removed unnecessary 'throws' clauses from constructor (DG);
 * 13-Dec-2001 : Added skeleton code for tooltips.  Added new constructor. (DG);
 * 16-Jan-2002 : Renamed the tooltips class (DG);
 * 22-Jan-2002 : Added DrawInfo class, incorporating tooltips and crosshairs.
 *               Crosshairs based on code by Jonathan Nash (DG);
 * 05-Feb-2002 : Added alpha-transparency setting based on code by Sylvain
 *               Vieujot (DG);
 * 26-Feb-2002 : Updated getMinimumXXX() and getMaximumXXX() methods to handle
 *               special case when chart is null (DG);
 * 28-Feb-2002 : Renamed Datasets.java --> DatasetUtilities.java (DG);
 * 28-Mar-2002 : The plot now registers with the renderer as a property change
 *               listener.  Also added a new constructor (DG);
 * 09-Apr-2002 : Removed the transRangeZero from the renderer.drawItem()
 *               method.  Moved the tooltip generator into the renderer (DG);
 * 23-Apr-2002 : Fixed bug in methods for drawing horizontal and vertical
 *               lines (DG);
 * 13-May-2002 : Small change to the draw() method so that it works for
 *               OverlaidXYPlot also (DG);
 * 25-Jun-2002 : Removed redundant import (DG);
 * 20-Aug-2002 : Renamed getItemRenderer() --> getRenderer(), and
 *               setXYItemRenderer() --> setRenderer() (DG);
 * 28-Aug-2002 : Added mechanism for (optional) plot annotations (DG);
 * 02-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 18-Nov-2002 : Added grid settings for both domain and range axis (previously
 *               these were set in the axes) (DG);
 * 09-Jan-2003 : Further additions to the grid settings, plus integrated plot
 *               border bug fix contributed by Gideon Krause (DG);
 * 22-Jan-2003 : Removed monolithic constructor (DG);
 * 04-Mar-2003 : Added 'no data' message, see bug report 691634.  Added
 *               secondary range markers using code contributed by Klaus
 *               Rheinwald (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 03-Apr-2003 : Added setDomainAxisLocation() method (DG);
 * 30-Apr-2003 : Moved annotation drawing into a separate method (DG);
 * 01-May-2003 : Added multi-pass mechanism for renderers (DG);
 * 02-May-2003 : Changed axis locations from int to AxisLocation (DG);
 * 15-May-2003 : Added an orientation attribute (DG);
 * 02-Jun-2003 : Removed range axis compatibility test (DG);
 * 05-Jun-2003 : Added domain and range grid bands (sponsored by Focus Computer
 *               Services Ltd) (DG);
 * 26-Jun-2003 : Fixed bug (757303) in getDataRange() method (DG);
 * 02-Jul-2003 : Added patch from bug report 698646 (secondary axes for
 *               overlaid plots) (DG);
 * 23-Jul-2003 : Added support for multiple secondary datasets, axes and
 *               renderers (DG);
 * 27-Jul-2003 : Added support for stacked XY area charts (RA);
 * 19-Aug-2003 : Implemented Cloneable (DG);
 * 01-Sep-2003 : Fixed bug where change to secondary datasets didn't generate
 *               change event (797466) (DG)
 * 08-Sep-2003 : Added internationalization via use of properties
 *               resourceBundle (RFE 690236) (AL);
 * 08-Sep-2003 : Changed ValueAxis API (DG);
 * 08-Sep-2003 : Fixes for serialization (NB);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 17-Sep-2003 : Fixed zooming to include secondary domain axes (DG);
 * 18-Sep-2003 : Added getSecondaryDomainAxisCount() and
 *               getSecondaryRangeAxisCount() methods suggested by Eduardo
 *               Ramalho (RFE 808548) (DG);
 * 23-Sep-2003 : Split domain and range markers into foreground and
 *               background (DG);
 * 06-Oct-2003 : Fixed bug in clearDomainMarkers() and clearRangeMarkers()
 *               methods.  Fixed bug (815876) in addSecondaryRangeMarker()
 *               method.  Added new addSecondaryDomainMarker methods (see bug
 *               id 815869) (DG);
 * 10-Nov-2003 : Added getSecondaryDomain/RangeAxisMappedToDataset() methods
 *               requested by Eduardo Ramalho (DG);
 * 24-Nov-2003 : Removed unnecessary notification when updating axis anchor
 *               values (DG);
 * 21-Jan-2004 : Update for renamed method in ValueAxis (DG);
 * 25-Feb-2004 : Replaced CrosshairInfo with CrosshairState (DG);
 * 12-Mar-2004 : Fixed bug where primary renderer is always used to determine
 *               range type (DG);
 * 22-Mar-2004 : Fixed cloning bug (DG);
 * 23-Mar-2004 : Fixed more cloning bugs (DG);
 * 07-Apr-2004 : Fixed problem with axis range when the secondary renderer is
 *               stacked, see this post in the forum:
 *               http://www.jfree.org/phpBB2/viewtopic.php?t=8204 (DG);
 * 07-Apr-2004 : Added get/setDatasetRenderingOrder() methods (DG);
 * 26-Apr-2004 : Added option to fill quadrant areas in the background of the
 *               plot (DG);
 * 27-Apr-2004 : Removed major distinction between primary and secondary
 *               datasets, renderers and axes (DG);
 * 30-Apr-2004 : Modified to make use of the new getRangeExtent() method in the
 *               renderer interface (DG);
 * 13-May-2004 : Added optional fixedLegendItems attribute (DG);
 * 19-May-2004 : Added indexOf() method (DG);
 * 03-Jun-2004 : Fixed zooming bug (DG);
 * 18-Aug-2004 : Added removedAnnotation() method (by tkram01) (DG);
 * 05-Oct-2004 : Modified storage type for dataset-to-axis maps (DG);
 * 06-Oct-2004 : Modified getDataRange() method to use renderer to determine
 *               the x-value range (now matches behaviour for y-values).  Added
 *               getDomainAxisIndex() method (DG);
 * 12-Nov-2004 : Implemented new Zoomable interface (DG);
 * 25-Nov-2004 : Small update to clone() implementation (DG);
 * 22-Feb-2005 : Changed axis offsets from Spacer --> RectangleInsets (DG);
 * 24-Feb-2005 : Added indexOf(XYItemRenderer) method (DG);
 * 21-Mar-2005 : Register plot as change listener in setRenderer() method (DG);
 * 21-Apr-2005 : Added get/setSeriesRenderingOrder() methods (ET);
 * 26-Apr-2005 : Removed LOGGER (DG);
 * 04-May-2005 : Fixed serialization of domain and range markers (DG);
 * 05-May-2005 : Removed unused draw() method (DG);
 * 20-May-2005 : Added setDomainAxes() and setRangeAxes() methods, as per
 *               RFE 1183100 (DG);
 * 01-Jun-2005 : Upon deserialization, register plot as a listener with its
 *               axes, dataset(s) and renderer(s) - see patch 1209475 (DG);
 * 01-Jun-2005 : Added clearDomainMarkers(int) method to match 
 *               clearRangeMarkers(int) (DG);
 * 06-Jun-2005 : Fixed equals() method to handle GradientPaint (DG);
 * 09-Jun-2005 : Added setRenderers(), as per RFE 1183100 (DG);
 * 06-Jul-2005 : Fixed crosshair bug (id = 1233336) (DG);
 *
 */

package org.jfree.chart.plot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisCollection;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.ValueTick;
import org.jfree.chart.event.ChartChangeEventType;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.Range;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.ObjectList;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;

/**
 * A general class for plotting data in the form of (x, y) pairs.  This plot can
 * use data from any class that implements the {@link XYDataset} interface.
 * <P>
 * <code>XYPlot</code> makes use of an {@link XYItemRenderer} to draw each point
 * on the plot.  By using different renderers, various chart types can be
 * produced.
 * <p>
 * The {@link org.jfree.chart.ChartFactory} class contains static methods for
 * creating pre-configured charts.
 */
public class XYPlot extends Plot implements ValueAxisPlot,
                                            Zoomable,
                                            RendererChangeListener,
                                            Cloneable, PublicCloneable,
                                            Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 7044148245716569264L;
    
    /** The default grid line stroke. */
    public static final Stroke DEFAULT_GRIDLINE_STROKE = new BasicStroke(
        0.5f,
        BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_BEVEL,
        0.0f,
        new float[] {2.0f, 2.0f},
        0.0f
    );

    /** The default grid line paint. */
    public static final Paint DEFAULT_GRIDLINE_PAINT = Color.lightGray;

    /** The default crosshair visibility. */
    public static final boolean DEFAULT_CROSSHAIR_VISIBLE = false;

    /** The default crosshair stroke. */
    public static final Stroke DEFAULT_CROSSHAIR_STROKE
        = DEFAULT_GRIDLINE_STROKE;

    /** The default crosshair paint. */
    public static final Paint DEFAULT_CROSSHAIR_PAINT = Color.blue;

    /** The resourceBundle for the localization. */
    protected static ResourceBundle localizationResources
        = ResourceBundle.getBundle("org.jfree.chart.plot.LocalizationBundle");

    /** The plot orientation. */
    private PlotOrientation orientation;

    /** The offset between the data area and the axes. */
    private RectangleInsets axisOffset;

    /** The domain axis / axes (used for the x-values). */
    private ObjectList domainAxes;

    /** The domain axis locations. */
    private ObjectList domainAxisLocations;

    /** The range axis (used for the y-values). */
    private ObjectList rangeAxes;

    /** The range axis location. */
    private ObjectList rangeAxisLocations;

    /** Storage for the datasets. */
    private ObjectList datasets;

    /** Storage for the renderers. */
    private ObjectList renderers;

    /**
     * Storage for keys that map datasets/renderers to domain axes.  If the
     * map contains no entry for a dataset, it is assumed to map to the
     * primary domain axis (index = 0).
     */
    private Map datasetToDomainAxisMap;

    /**
     * Storage for keys that map datasets/renderers to range axes. If the
     * map contains no entry for a dataset, it is assumed to map to the
     * primary domain axis (index = 0).
     */
    private Map datasetToRangeAxisMap;

    /** The origin point for the quadrants (if drawn). */
    private transient Point2D quadrantOrigin = new Point2D.Double(0.0, 0.0);

    /** The paint used for each quadrant. */
    private transient Paint[] quadrantPaint
        = new Paint[] {null, null, null, null};

    /** A flag that controls whether the domain grid-lines are visible. */
    private boolean domainGridlinesVisible;

    /** The stroke used to draw the domain grid-lines. */
    private transient Stroke domainGridlineStroke;

    /** The paint used to draw the domain grid-lines. */
    private transient Paint domainGridlinePaint;

    /** A flag that controls whether the range grid-lines are visible. */
    private boolean rangeGridlinesVisible;

    /** The stroke used to draw the range grid-lines. */
    private transient Stroke rangeGridlineStroke;

    /** The paint used to draw the range grid-lines. */
    private transient Paint rangeGridlinePaint;

    /** 
     * A flag that controls whether or not the zero baseline against the range
     * axis is visible.
     */
    private boolean rangeZeroBaselineVisible;

    /** The stroke used for the zero baseline against the range axis. */
    private transient Stroke rangeZeroBaselineStroke;

    /** The paint used for the zero baseline against the range axis. */
    private transient Paint rangeZeroBaselinePaint;

    /** A flag that controls whether or not a domain crosshair is drawn..*/
    private boolean domainCrosshairVisible;

    /** The domain crosshair value. */
    private double domainCrosshairValue;

    /** The pen/brush used to draw the crosshair (if any). */
    private transient Stroke domainCrosshairStroke;

    /** The color used to draw the crosshair (if any). */
    private transient Paint domainCrosshairPaint;

    /**
     * A flag that controls whether or not the crosshair locks onto actual
     * data points.
     */
    private boolean domainCrosshairLockedOnData = true;

    /** A flag that controls whether or not a range crosshair is drawn..*/
    private boolean rangeCrosshairVisible;

    /** The range crosshair value. */
    private double rangeCrosshairValue;

    /** The pen/brush used to draw the crosshair (if any). */
    private transient Stroke rangeCrosshairStroke;

    /** The color used to draw the crosshair (if any). */
    private transient Paint rangeCrosshairPaint;

    /**
     * A flag that controls whether or not the crosshair locks onto actual
     * data points.
     */
    private boolean rangeCrosshairLockedOnData = true;

    /** A map of lists of foreground markers (optional) for the domain axes. */
    private Map foregroundDomainMarkers;

    /** A map of lists of background markers (optional) for the domain axes. */
    private Map backgroundDomainMarkers;

    /** A map of lists of foreground markers (optional) for the range axes. */
    private Map foregroundRangeMarkers;

    /** A map of lists of background markers (optional) for the range axes. */
    private Map backgroundRangeMarkers;

    /** 
     * A (possibly empty) list of annotations for the plot.  The list should
     * be initialised in the constructor and never allowed to be 
     * <code>null</code>.
     */
    private List annotations;

    /** The paint used for the domain tick bands (if any). */
    private transient Paint domainTickBandPaint;

    /** The paint used for the range tick bands (if any). */
    private transient Paint rangeTickBandPaint;

    /** The fixed domain axis space. */
    private AxisSpace fixedDomainAxisSpace;

    /** The fixed range axis space. */
    private AxisSpace fixedRangeAxisSpace;

    /**
     * The order of the dataset rendering (REVERSE draws the primary dataset
     * last so that it appears to be on top).
     */
    private DatasetRenderingOrder datasetRenderingOrder
        = DatasetRenderingOrder.REVERSE;

    /**
     * The order of the series rendering (REVERSE draws the primary series
     * last so that it appears to be on top).
     */
    private SeriesRenderingOrder seriesRenderingOrder
        = SeriesRenderingOrder.REVERSE;

    /**
     * The weight for this plot (only relevant if this is a subplot in a
     * combined plot).
     */
    private int weight;

    /**
     * An optional collection of legend items that can be returned by the
     * getLegendItems() method.
     */
    private LegendItemCollection fixedLegendItems;

    /**
     * Default constructor.
     */
    public XYPlot() {
        this(null, null, null, null);
    }

    /**
     * Creates a new plot.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     * @param domainAxis  the domain axis (<code>null</code> permitted).
     * @param rangeAxis  the range axis (<code>null</code> permitted).
     * @param renderer  the renderer (<code>null</code> permitted).
     */
    public XYPlot(XYDataset dataset,
                  ValueAxis domainAxis,
                  ValueAxis rangeAxis,
                  XYItemRenderer renderer) {

        super();

        this.orientation = PlotOrientation.VERTICAL;
        this.weight = 1;  // only relevant when this is a subplot
        this.axisOffset = RectangleInsets.ZERO_INSETS;

        // allocate storage for datasets, axes and renderers (all optional)
        this.domainAxes = new ObjectList();
        this.domainAxisLocations = new ObjectList();
        this.foregroundDomainMarkers = new HashMap();
        this.backgroundDomainMarkers = new HashMap();

        this.rangeAxes = new ObjectList();
        this.rangeAxisLocations = new ObjectList();
        this.foregroundRangeMarkers = new HashMap();
        this.backgroundRangeMarkers = new HashMap();

        this.datasets = new ObjectList();
        this.renderers = new ObjectList();

        this.datasetToDomainAxisMap = new TreeMap();
        this.datasetToRangeAxisMap = new TreeMap();

        this.datasets.set(0, dataset);
        if (dataset != null) {
            dataset.addChangeListener(this);
        }

        this.renderers.set(0, renderer);
        if (renderer != null) {
            renderer.setPlot(this);
            renderer.addChangeListener(this);
        }

        this.domainAxes.set(0, domainAxis);
        this.mapDatasetToDomainAxis(0, 0);
        if (domainAxis != null) {
            domainAxis.setPlot(this);
            domainAxis.addChangeListener(this);
        }
        this.domainAxisLocations.set(0, AxisLocation.BOTTOM_OR_LEFT);

        this.rangeAxes.set(0, rangeAxis);
        this.mapDatasetToRangeAxis(0, 0);
        if (rangeAxis != null) {
            rangeAxis.setPlot(this);
            rangeAxis.addChangeListener(this);
        }
        this.rangeAxisLocations.set(0, AxisLocation.BOTTOM_OR_LEFT);

        configureDomainAxes();
        configureRangeAxes();

        this.domainGridlinesVisible = true;
        this.domainGridlineStroke = DEFAULT_GRIDLINE_STROKE;
        this.domainGridlinePaint = DEFAULT_GRIDLINE_PAINT;

        this.rangeGridlinesVisible = true;
        this.rangeGridlineStroke = DEFAULT_GRIDLINE_STROKE;
        this.rangeGridlinePaint = DEFAULT_GRIDLINE_PAINT;

        this.rangeZeroBaselineVisible = false;
        this.rangeZeroBaselinePaint = Color.black;
        this.rangeZeroBaselineStroke = new BasicStroke(0.5f);

        this.domainCrosshairVisible = false;
        this.domainCrosshairValue = 0.0;
        this.domainCrosshairStroke = DEFAULT_CROSSHAIR_STROKE;
        this.domainCrosshairPaint = DEFAULT_CROSSHAIR_PAINT;

        this.rangeCrosshairVisible = false;
        this.rangeCrosshairValue = 0.0;
        this.rangeCrosshairStroke = DEFAULT_CROSSHAIR_STROKE;
        this.rangeCrosshairPaint = DEFAULT_CROSSHAIR_PAINT;

        this.annotations = new java.util.ArrayList();

    }

    /**
     * Returns the plot type as a string.
     *
     * @return A short string describing the type of plot.
     */
    public String getPlotType() {
        return localizationResources.getString("XY_Plot");
    }

    /**
     * Returns the orientation of the plot.
     *
     * @return The orientation of the plot.
     */
    public PlotOrientation getOrientation() {
        return this.orientation;
    }

    /**
     * Sets the orientation for the plot.
     *
     * @param orientation  the orientation (<code>null</code> not allowed).
     */
    public void setOrientation(PlotOrientation orientation) {
        if (orientation == null) {
            throw new IllegalArgumentException("Null 'orientation' argument.");
        }
        if (orientation != this.orientation) {
            this.orientation = orientation;
            notifyListeners(new PlotChangeEvent(this));
        }
    }

    /**
     * Returns the axis offset.
     *
     * @return The axis offset (never <code>null</code>).
     */
    public RectangleInsets getAxisOffset() {
        return this.axisOffset;
    }

    /**
     * Sets the axis offsets (gap between the data area and the axes).
     *
     * @param offset  the offset (<code>null</code> not permitted).
     */
    public void setAxisOffset(RectangleInsets offset) {
        if (offset == null) {
            throw new IllegalArgumentException("Null 'offset' argument.");
        }
        this.axisOffset = offset;
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns the domain axis for the plot.  If the domain axis for this plot
     * is null, then the method will return the parent plot's domain axis (if
     * there is a parent plot).
     *
     * @return The domain axis.
     */
    public ValueAxis getDomainAxis() {
        return getDomainAxis(0);
    }

    /**
     * Returns a domain axis.
     *
     * @param index  the axis index.
     *
     * @return The axis (<code>null</code> possible).
     */
    public ValueAxis getDomainAxis(int index) {
        ValueAxis result = null;
        if (index < this.domainAxes.size()) {
            result = (ValueAxis) this.domainAxes.get(index);
        }
        if (result == null) {
            Plot parent = getParent();
            if (parent instanceof XYPlot) {
                XYPlot xy = (XYPlot) parent;
                result = xy.getDomainAxis(index);
            }
        }
        return result;
    }

    /**
     * Sets the domain axis for the plot and sends a {@link PlotChangeEvent}
     * to all registered listeners.
     *
     * @param axis  the new axis (<code>null</code> permitted).
     */
    public void setDomainAxis(ValueAxis axis) {
        setDomainAxis(0, axis);
    }

    /**
     * Sets a domain axis and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param index  the axis index.
     * @param axis  the axis.
     */
    public void setDomainAxis(int index, ValueAxis axis) {
        setDomainAxis(index, axis, true);
    }
    
    /**
     * Sets a domain axis and, if requested, sends a {@link PlotChangeEvent} to
     * all registered listeners.
     *
     * @param index  the axis index.
     * @param axis  the axis.
     * @param notify  notify listeners?
     */
    public void setDomainAxis(int index, ValueAxis axis, boolean notify) {
        ValueAxis existing = getDomainAxis(index);
        if (existing != null) {
            existing.removeChangeListener(this);
        }
        if (axis != null) {
            axis.setPlot(this);
        }
        this.domainAxes.set(index, axis);
        if (axis != null) {
            axis.configure();
            axis.addChangeListener(this);
        }
        if (notify) {
            notifyListeners(new PlotChangeEvent(this));
        }
    }

    /**
     * Sets the domain axes for this plot and sends a {@link PlotChangeEvent}
     * to all registered listeners.
     * 
     * @param axes  the axes.
     */
    public void setDomainAxes(ValueAxis[] axes) {
        for (int i = 0; i < axes.length; i++) {
            setDomainAxis(i, axes[i], false);   
        }
        notifyListeners(new PlotChangeEvent(this));
    }
    
    /**
     * Returns the location of the primary domain axis.
     *
     * @return The location (never <code>null</code>).
     */
    public AxisLocation getDomainAxisLocation() {
        return (AxisLocation) this.domainAxisLocations.get(0);
    }

    /**
     * Sets the location of the domain axis and sends a {@link PlotChangeEvent}
     * to all registered listeners.
     *
     * @param location  the location (<code>null</code> not permitted).
     */
    public void setDomainAxisLocation(AxisLocation location) {
        // defer argument checking...
        setDomainAxisLocation(location, true);
    }

    /**
     * Sets the location of the domain axis and, if requested, sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param location  the location (<code>null</code> not permitted).
     * @param notify  notify listeners?
     */
    public void setDomainAxisLocation(AxisLocation location, boolean notify) {
        if (location == null) {
            throw new IllegalArgumentException("Null 'location' argument.");
        }
        this.domainAxisLocations.set(0, location);
        if (notify) {
            notifyListeners(new PlotChangeEvent(this));
        }
    }

    /**
     * Returns the edge for the primary domain axis (taking into account the
     * plot's orientation.
     *
     * @return The edge.
     */
    public RectangleEdge getDomainAxisEdge() {
        return Plot.resolveDomainAxisLocation(
            getDomainAxisLocation(), this.orientation
        );
    }

    /**
     * Returns the number of domain axes.
     *
     * @return The axis count.
     */
    public int getDomainAxisCount() {
        return this.domainAxes.size();
    }

    /**
     * Clears the domain axes from the plot and sends a {@link PlotChangeEvent}
     * to all registered listeners.
     */
    public void clearDomainAxes() {
        for (int i = 0; i < this.domainAxes.size(); i++) {
            ValueAxis axis = (ValueAxis) this.domainAxes.get(i);
            if (axis != null) {
                axis.removeChangeListener(this);
            }
        }
        this.domainAxes.clear();
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Configures the domain axes.
     */
    public void configureDomainAxes() {
        for (int i = 0; i < this.domainAxes.size(); i++) {
            ValueAxis axis = (ValueAxis) this.domainAxes.get(i);
            if (axis != null) {
                axis.configure();
            }
        }
    }

    /**
     * Returns the location for a domain axis.  If this hasn't been set
     * explicitly, the method returns the location that is opposite to the
     * primary domain axis location.
     *
     * @param index  the axis index.
     *
     * @return The location (never <code>null</code>).
     */
    public AxisLocation getDomainAxisLocation(int index) {
        AxisLocation result = null;
        if (index < this.domainAxisLocations.size()) {
            result = (AxisLocation) this.domainAxisLocations.get(index);
        }
        if (result == null) {
            result = AxisLocation.getOpposite(getDomainAxisLocation());
        }
        return result;
    }

    /**
     * Sets the location for a domain axis and sends a {@link PlotChangeEvent}
     * to all registered listeners.
     *
     * @param index  the axis index.
     * @param location  the location (<code>null</code> permitted).
     */
    public void setDomainAxisLocation(int index, AxisLocation location) {
        this.domainAxisLocations.set(index, location);
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns the edge for a domain axis.
     *
     * @param index  the axis index.
     *
     * @return The edge.
     */
    public RectangleEdge getDomainAxisEdge(int index) {
        AxisLocation location = getDomainAxisLocation(index);
        RectangleEdge result = Plot.resolveDomainAxisLocation(
            location, this.orientation
        );
        if (result == null) {
            result = RectangleEdge.opposite(getDomainAxisEdge());
        }
        return result;
    }

    /**
     * Returns the range axis for the plot.  If the range axis for this plot is
     * null, then the method will return the parent plot's range axis (if
     * there is a parent plot).
     *
     * @return The range axis.
     */
    public ValueAxis getRangeAxis() {
        return getRangeAxis(0);
    }

    /**
     * Sets the range axis for the plot and sends a {@link PlotChangeEvent} to
     * all registered listeners.
     *
     * @param axis  the axis (<code>null</code> permitted).
     *
     */
    public void setRangeAxis(ValueAxis axis)  {

        if (axis != null) {
            axis.setPlot(this);
        }

        // plot is likely registered as a listener with the existing axis...
        ValueAxis existing = getRangeAxis();
        if (existing != null) {
            existing.removeChangeListener(this);
        }

        this.rangeAxes.set(0, axis);
        if (axis != null) {
            axis.configure();
            axis.addChangeListener(this);
        }
        notifyListeners(new PlotChangeEvent(this));

    }

    /**
     * Returns the location of the primary range axis.
     *
     * @return The location (never <code>null</code>).
     */
    public AxisLocation getRangeAxisLocation() {
        return (AxisLocation) this.rangeAxisLocations.get(0);
    }

    /**
     * Sets the location of the primary range axis and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param location  the location (<code>null</code> not permitted).
     */
    public void setRangeAxisLocation(AxisLocation location) {
        // defer argument checking...
        setRangeAxisLocation(location, true);
    }

    /**
     * Sets the location of the primary range axis and, if requested, sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param location  the location (<code>null</code> not permitted).
     * @param notify  notify listeners?
     */
    public void setRangeAxisLocation(AxisLocation location, boolean notify) {
        if (location == null) {
            throw new IllegalArgumentException("Null 'location' argument.");
        }
        this.rangeAxisLocations.set(0, location);
        if (notify) {
            notifyListeners(new PlotChangeEvent(this));
        }

    }

    /**
     * Returns the edge for the primary range axis.
     *
     * @return The range axis edge.
     */
    public RectangleEdge getRangeAxisEdge() {
        return Plot.resolveRangeAxisLocation(
            getRangeAxisLocation(), this.orientation
        );
    }

    /**
     * Returns a range axis.
     *
     * @param index  the axis index.
     *
     * @return The axis (<code>null</code> possible).
     */
    public ValueAxis getRangeAxis(int index) {
        ValueAxis result = null;
        if (index < this.rangeAxes.size()) {
            result = (ValueAxis) this.rangeAxes.get(index);
        }
        if (result == null) {
            Plot parent = getParent();
            if (parent instanceof XYPlot) {
                XYPlot xy = (XYPlot) parent;
                result = xy.getRangeAxis(index);
            }
        }
        return result;
    }

    /**
     * Sets a range axis and sends a {@link PlotChangeEvent} to all registered
     * listeners.
     *
     * @param index  the axis index.
     * @param axis  the axis (<code>null</code> permitted).
     */
    public void setRangeAxis(int index, ValueAxis axis) {
        setRangeAxis(index, axis, true);
    } 
    
    /**
     * Sets a range axis and, if requested, sends a {@link PlotChangeEvent} to 
     * all registered listeners.
     *
     * @param index  the axis index.
     * @param axis  the axis (<code>null</code> permitted).
     */
    public void setRangeAxis(int index, ValueAxis axis, boolean notify) {
        ValueAxis existing = getRangeAxis(index);
        if (existing != null) {
            existing.removeChangeListener(this);
        }
        if (axis != null) {
            axis.setPlot(this);
        }
        this.rangeAxes.set(index, axis);
        if (axis != null) {
            axis.configure();
            axis.addChangeListener(this);
        }
        if (notify) {
            notifyListeners(new PlotChangeEvent(this));
        }
    }

    /**
     * Sets the range axes for this plot and sends a {@link PlotChangeEvent}
     * to all registered listeners.
     * 
     * @param axes  the axes.
     */
    public void setRangeAxes(ValueAxis[] axes) {
        for (int i = 0; i < axes.length; i++) {
            setRangeAxis(i, axes[i], false);   
        }
        notifyListeners(new PlotChangeEvent(this));
    }
    
    /**
     * Returns the number of range axes.
     *
     * @return The axis count.
     */
    public int getRangeAxisCount() {
        return this.rangeAxes.size();
    }

    /**
     * Clears the range axes from the plot and sends a {@link PlotChangeEvent}
     * to all registered listeners.
     */
    public void clearRangeAxes() {
        for (int i = 0; i < this.rangeAxes.size(); i++) {
            ValueAxis axis = (ValueAxis) this.rangeAxes.get(i);
            if (axis != null) {
                axis.removeChangeListener(this);
            }
        }
        this.rangeAxes.clear();
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Configures the range axes.
     */
    public void configureRangeAxes() {
        for (int i = 0; i < this.rangeAxes.size(); i++) {
            ValueAxis axis = (ValueAxis) this.rangeAxes.get(i);
            if (axis != null) {
                axis.configure();
            }
        }
    }

    /**
     * Returns the location for a range axis.  If this hasn't been set
     * explicitly, the method returns the location that is opposite to the
     * primary range axis location.
     *
     * @param index  the axis index.
     *
     * @return The location (never <code>null</code>).
     */
    public AxisLocation getRangeAxisLocation(int index) {
        AxisLocation result = null;
        if (index < this.rangeAxisLocations.size()) {
            result = (AxisLocation) this.rangeAxisLocations.get(index);
        }
        if (result == null) {
            result = AxisLocation.getOpposite(getRangeAxisLocation());
        }
        return result;
    }

    /**
     * Sets the location for a range axis and sends a {@link PlotChangeEvent}
     * to all registered listeners.
     *
     * @param index  the axis index.
     * @param location  the location (<code>null</code> permitted).
     */
    public void setRangeAxisLocation(int index, AxisLocation location) {
        this.rangeAxisLocations.set(index, location);
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns the edge for a range axis.
     *
     * @param index  the axis index.
     *
     * @return The edge.
     */
    public RectangleEdge getRangeAxisEdge(int index) {
        AxisLocation location = getRangeAxisLocation(index);
        RectangleEdge result = Plot.resolveRangeAxisLocation(
            location, this.orientation
        );
        if (result == null) {
            result = RectangleEdge.opposite(getRangeAxisEdge());
        }
        return result;
    }

    /**
     * Returns the primary dataset for the plot.
     *
     * @return The primary dataset (possibly <code>null</code>).
     */
    public XYDataset getDataset() {
        return getDataset(0);
    }

    /**
     * Returns a dataset.
     *
     * @param index  the dataset index.
     *
     * @return The dataset (possibly <code>null</code>).
     */
    public XYDataset getDataset(int index) {
        XYDataset result = null;
        if (this.datasets.size() > index) {
            result = (XYDataset) this.datasets.get(index);
        }
        return result;
    }

    /**
     * Sets the primary dataset for the plot, replacing the existing dataset if
     * there is one.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     */
    public void setDataset(XYDataset dataset) {
        setDataset(0, dataset);
    }

    /**
     * Sets a dataset for the plot.
     *
     * @param index  the dataset index.
     * @param dataset  the dataset (<code>null</code> permitted).
     */
    public void setDataset(int index, XYDataset dataset) {
        XYDataset existing = getDataset(index);
        if (existing != null) {
            existing.removeChangeListener(this);
        }
        this.datasets.set(index, dataset);
        if (dataset != null) {
            dataset.addChangeListener(this);
        }

        // send a dataset change event to self...
        DatasetChangeEvent event = new DatasetChangeEvent(this, dataset);
        datasetChanged(event);
    }

    /**
     * Returns the number of datasets.
     *
     * @return The number of datasets.
     */
    public int getDatasetCount() {
        return this.datasets.size();
    }

    /**
     * Returns the index of the specified dataset, or <code>-1</code> if the
     * dataset does not belong to the plot.
     *
     * @param dataset  the dataset (<code>null</code> not permitted).
     *
     * @return The index.
     */
    public int indexOf(XYDataset dataset) {
        int result = -1;
        for (int i = 0; i < this.datasets.size(); i++) {
            if (dataset == this.datasets.get(i)) {
                result = i;
                break;
            }
        }
        return result;
    }

    /**
     * Maps a dataset to a particular domain axis.  All data will be plotted
     * against axis zero by default, no mapping is required for this case.
     *
     * @param index  the dataset index (zero-based).
     * @param axisIndex  the axis index.
     */
    public void mapDatasetToDomainAxis(int index, int axisIndex) {
        this.datasetToDomainAxisMap.put(
            new Integer(index), new Integer(axisIndex)
        );
        // fake a dataset change event to update axes...
        datasetChanged(new DatasetChangeEvent(this, getDataset(index)));
    }

    /**
     * Maps a dataset to a particular range axis.  All data will be plotted
     * against axis zero by default, no mapping is required for this case.
     *
     * @param index  the dataset index (zero-based).
     * @param axisIndex  the axis index.
     */
    public void mapDatasetToRangeAxis(int index, int axisIndex) {
        this.datasetToRangeAxisMap.put(
            new Integer(index), new Integer(axisIndex)
        );
        // fake a dataset change event to update axes...
        datasetChanged(new DatasetChangeEvent(this, getDataset(index)));
    }

    /**
     * Returns the renderer for the primary dataset.
     *
     * @return The item renderer (possibly <code>null</code>).
     */
    public XYItemRenderer getRenderer() {
        return getRenderer(0);
    }

    /**
     * Returns the renderer for a dataset, or <code>null</code>.
     *
     * @param index  the renderer index.
     *
     * @return The renderer (possibly <code>null</code>).
     */
    public XYItemRenderer getRenderer(int index) {
        XYItemRenderer result = null;
        if (this.renderers.size() > index) {
            result = (XYItemRenderer) this.renderers.get(index);
        }
        return result;

    }

    /**
     * Sets the renderer for the primary dataset and sends a
     * {@link PlotChangeEvent} to all registered listeners.  If the renderer
     * is set to <code>null</code>, no data will be displayed.
     *
     * @param renderer  the renderer (<code>null</code> permitted).
     */
    public void setRenderer(XYItemRenderer renderer) {
        setRenderer(0, renderer);
    }

    /**
     * Sets a renderer and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param index  the index.
     * @param renderer  the renderer.
     */
    public void setRenderer(int index, XYItemRenderer renderer) {
        setRenderer(index, renderer, true);
    }

    /**
     * Sets a renderer and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param index  the index.
     * @param renderer  the renderer.
     * @param notify  notify listeners?
     */
    public void setRenderer(int index, XYItemRenderer renderer, 
                            boolean notify) {
        XYItemRenderer existing = getRenderer(index);
        if (existing != null) {
            existing.removeChangeListener(this);
        }
        this.renderers.set(index, renderer);
        if (renderer != null) {
            renderer.setPlot(this);
            renderer.addChangeListener(this);
        }
        configureDomainAxes();
        configureRangeAxes();
        if (notify) {
            notifyListeners(new PlotChangeEvent(this));
        }
    }

    /**
     * Sets the renderers for this plot and sends a {@link PlotChangeEvent}
     * to all registered listeners.
     * 
     * @param renderers  the renderers.
     */
    public void setRenderers(XYItemRenderer[] renderers) {
        for (int i = 0; i < renderers.length; i++) {
            setRenderer(i, renderers[i], false);   
        }
        notifyListeners(new PlotChangeEvent(this));
    }
    
    /**
     * Returns the dataset rendering order.
     *
     * @return The order (never <code>null</code>).
     */
    public DatasetRenderingOrder getDatasetRenderingOrder() {
        return this.datasetRenderingOrder;
    }

    /**
     * Sets the rendering order and sends a {@link PlotChangeEvent} to all
     * registered listeners.  By default, the plot renders the primary dataset
     * last (so that the primary dataset overlays the secondary datasets).
     * You can reverse this if you want to.
     *
     * @param order  the rendering order (<code>null</code> not permitted).
     */
    public void setDatasetRenderingOrder(DatasetRenderingOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Null 'order' argument.");
        }
        this.datasetRenderingOrder = order;
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns the series rendering order.
     *
     * @return the order (never <code>null</code>).
     */
    public SeriesRenderingOrder getSeriesRenderingOrder() {
        return this.seriesRenderingOrder;
    }

    /**
     * Sets the series order and sends a {@link PlotChangeEvent} to all
     * registered listeners.  By default, the plot renders the primary series
     * last (so that the primary series appears to be on top).
     * You can reverse this if you want to.
     *
     * @param order  the rendering order (<code>null</code> not permitted).
     */
    public void setSeriesRenderingOrder(SeriesRenderingOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Null 'order' argument.");
        }
        this.seriesRenderingOrder = order;
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns the index of the specified renderer, or <code>-1</code> if the
     * renderer is not assigned to this plot.
     *
     * @param renderer  the renderer (<code>null</code> permitted).
     *
     * @return The renderer index.
     */
    public int getIndexOf(XYItemRenderer renderer) {
        return this.renderers.indexOf(renderer);
    }

    /**
     * Returns the renderer for the specified dataset.  The code first
     * determines the index of the dataset, then checks if there is a
     * renderer with the same index (if not, the method returns renderer(0).
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     *
     * @return The renderer (possibly <code>null</code>).
     */
    public XYItemRenderer getRendererForDataset(XYDataset dataset) {
        XYItemRenderer result = null;
        for (int i = 0; i < this.datasets.size(); i++) {
            if (this.datasets.get(i) == dataset) {
                result = (XYItemRenderer) this.renderers.get(i);
                if (result == null) {
                    result = getRenderer();
                }
                break;
            }
        }
        return result;
    }

    /**
     * Returns the weight for this plot when it is used as a subplot within a
     * combined plot.
     *
     * @return The weight.
     */
    public int getWeight() {
        return this.weight;
    }

    /**
     * Sets the weight for the plot.
     *
     * @param weight  the weight.
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }

    /**
     * Returns <code>true</code> if the domain gridlines are visible, and
     * <code>false<code> otherwise.
     *
     * @return <code>true</code> or <code>false</code>.
     */
    public boolean isDomainGridlinesVisible() {
        return this.domainGridlinesVisible;
    }

    /**
     * Sets the flag that controls whether or not the domain grid-lines are
     * visible.
     * <p>
     * If the flag value is changed, a {@link PlotChangeEvent} is sent to all
     * registered listeners.
     *
     * @param visible  the new value of the flag.
     */
    public void setDomainGridlinesVisible(boolean visible) {
        if (this.domainGridlinesVisible != visible) {
            this.domainGridlinesVisible = visible;
            notifyListeners(new PlotChangeEvent(this));
        }
    }

    /**
     * Returns the stroke for the grid-lines (if any) plotted against the
     * domain axis.
     *
     * @return The stroke.
     */
    public Stroke getDomainGridlineStroke() {
        return this.domainGridlineStroke;
    }

    /**
     * Sets the stroke for the grid lines plotted against the domain axis.
     * <p>
     * If you set this to <code>null</code>, no grid lines will be drawn.
     *
     * @param stroke  the stroke (<code>null</code> permitted).
     */
    public void setDomainGridlineStroke(Stroke stroke) {
        this.domainGridlineStroke = stroke;
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns the paint for the grid lines (if any) plotted against the domain
     * axis.
     *
     * @return The paint.
     */
    public Paint getDomainGridlinePaint() {
        return this.domainGridlinePaint;
    }

    /**
     * Sets the paint for the grid lines plotted against the domain axis.
     * <p>
     * If you set this to <code>null</code>, no grid lines will be drawn.
     *
     * @param paint  the paint (<code>null</code> permitted).
     */
    public void setDomainGridlinePaint(Paint paint) {
        this.domainGridlinePaint = paint;
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns <code>true</code> if the range axis grid is visible, and
     * <code>false<code> otherwise.
     *
     * @return A boolean.
     */
    public boolean isRangeGridlinesVisible() {
        return this.rangeGridlinesVisible;
    }

    /**
     * Sets the flag that controls whether or not the range axis grid lines
     * are visible.
     * <p>
     * If the flag value is changed, a {@link PlotChangeEvent} is sent to all
     * registered listeners.
     *
     * @param visible  the new value of the flag.
     */
    public void setRangeGridlinesVisible(boolean visible) {
        if (this.rangeGridlinesVisible != visible) {
            this.rangeGridlinesVisible = visible;
            notifyListeners(new PlotChangeEvent(this));
        }
    }

    /**
     * Returns the stroke for the grid lines (if any) plotted against the
     * range axis.
     *
     * @return The stroke (never <code>null</code>).
     */
    public Stroke getRangeGridlineStroke() {
        return this.rangeGridlineStroke;
    }

    /**
     * Sets the stroke for the grid lines plotted against the range axis,
     * and sends a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param stroke  the stroke (<code>null</code> not permitted).
     */
    public void setRangeGridlineStroke(Stroke stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        this.rangeGridlineStroke = stroke;
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns the paint for the grid lines (if any) plotted against the range
     * axis.
     *
     * @return The paint (never <code>null</code>).
     */
    public Paint getRangeGridlinePaint() {
        return this.rangeGridlinePaint;
    }

    /**
     * Sets the paint for the grid lines plotted against the range axis and
     * sends a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> permitted).
     */
    public void setRangeGridlinePaint(Paint paint) {
        this.rangeGridlinePaint = paint;
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns a flag that controls whether or not a zero baseline is
     * displayed for the range axis.
     *
     * @return A boolean.
     */
    public boolean isRangeZeroBaselineVisible() {
        return this.rangeZeroBaselineVisible;
    }

    /**
     * Sets the flag that controls whether or not the zero baseline is
     * displayed for the range axis, and sends a {@link PlotChangeEvent} to
     * all registered listeners.
     *
     * @param visible  the flag.
     */
    public void setRangeZeroBaselineVisible(boolean visible) {
        this.rangeZeroBaselineVisible = visible;
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns the stroke used for the zero baseline against the range axis.
     *
     * @return The stroke (never <code>null</code>).
     */
    public Stroke getRangeZeroBaselineStroke() {
        return this.rangeZeroBaselineStroke;
    }

    /**
     * Sets the stroke for the zero baseline for the range axis,
     * and sends a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param stroke  the stroke (<code>null</code> not permitted).
     */
    public void setRangeZeroBaselineStroke(Stroke stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        this.rangeZeroBaselineStroke = stroke;
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns the paint for the zero baseline (if any) plotted against the
     * range axis.
     *
     * @return The paint (never <code>null</code>).
     */
    public Paint getRangeZeroBaselinePaint() {
        return this.rangeZeroBaselinePaint;
    }

    /**
     * Sets the paint for the zero baseline plotted against the range axis and
     * sends a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> permitted).
     */
    public void setRangeZeroBaselinePaint(Paint paint) {
        this.rangeZeroBaselinePaint = paint;
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns the paint used for the domain tick bands.  If this is
     * <code>null</code>, no tick bands will be drawn.
     *
     * @return The paint (possibly <code>null</code>).
     */
    public Paint getDomainTickBandPaint() {
        return this.domainTickBandPaint;
    }

    /**
     * Sets the paint for the domain tick bands.
     *
     * @param paint  the paint (<code>null</code> permitted).
     */
    public void setDomainTickBandPaint(Paint paint) {
        this.domainTickBandPaint = paint;
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns the paint used for the range tick bands.  If this is
     * <code>null</code>, no tick bands will be drawn.
     *
     * @return The paint (possibly <code>null</code>).
     */
    public Paint getRangeTickBandPaint() {
        return this.rangeTickBandPaint;
    }

    /**
     * Sets the paint for the range tick bands.
     *
     * @param paint  the paint (<code>null</code> permitted).
     */
    public void setRangeTickBandPaint(Paint paint) {
        this.rangeTickBandPaint = paint;
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns the origin for the quadrants that can be displayed on the plot.
     * This defaults to (0, 0).
     *
     * @return The origin point (never <code>null</code>).
     */
    public Point2D getQuadrantOrigin() {
        return this.quadrantOrigin;
    }

    /**
     * Sets the quadrant origin and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param origin  the origin (<code>null</code> not permitted).
     */
    public void setQuadrantOrigin(Point2D origin) {
        if (origin == null) {
            throw new IllegalArgumentException("Null 'origin' argument.");
        }
        this.quadrantOrigin = origin;
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns the paint used for the specified quadrant.
     *
     * @param index  the quadrant index (0-3).
     *
     * @return The paint (possibly <code>null</code>).
     */
    public Paint getQuadrantPaint(int index) {
        if (index < 0 || index > 3) {
            throw new IllegalArgumentException(
                "The index should be in the range 0 to 3."
            );
        }
        return this.quadrantPaint[index];
    }

    /**
     * Sets the paint used for the specified quadrant and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param index  the quadrant index (0-3).
     * @param paint  the paint (<code>null</code> permitted).
     */
    public void setQuadrantPaint(int index, Paint paint) {
        if (index < 0 || index > 3) {
            throw new IllegalArgumentException(
                "The index should be in the range 0 to 3."
            );
        }
        this.quadrantPaint[index] = paint;
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Adds a marker for the domain axis and sends a {@link PlotChangeEvent}
     * to all registered listeners.
     * <P>
     * Typically a marker will be drawn by the renderer as a line perpendicular
     * to the range axis, however this is entirely up to the renderer.
     *
     * @param marker  the marker (<code>null</code> not permitted).
     */
    public void addDomainMarker(Marker marker) {
        // defer argument checking...
        addDomainMarker(marker, Layer.FOREGROUND);
    }

    /**
     * Adds a marker for the domain axis in the specified layer and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     * <P>
     * Typically a marker will be drawn by the renderer as a line perpendicular
     * to the range axis, however this is entirely up to the renderer.
     *
     * @param marker  the marker (<code>null</code> not permitted).
     * @param layer  the layer (foreground or background).
     */
    public void addDomainMarker(Marker marker, Layer layer) {
        addDomainMarker(0, marker, layer);
    }

    /**
     * Clears all the (foreground and background) domain markers and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     */
    public void clearDomainMarkers() {
        if (this.foregroundDomainMarkers != null) {
            this.foregroundDomainMarkers.clear();
        }
        if (this.backgroundDomainMarkers != null) {
            this.backgroundDomainMarkers.clear();
        }
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Clears the (foreground and background) domain markers for a particular
     * renderer.
     *
     * @param index  the renderer index.
     */
    public void clearDomainMarkers(int index) {
        Integer key = new Integer(index);
        if (this.backgroundDomainMarkers != null) {
            Collection markers
                = (Collection) this.backgroundDomainMarkers.get(key);
            if (markers != null) {
                markers.clear();
            }
        }
        if (this.foregroundRangeMarkers != null) {
            Collection markers
                = (Collection) this.foregroundDomainMarkers.get(key);
            if (markers != null) {
                markers.clear();
            }
        }
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Adds a marker for a renderer and sends a {@link PlotChangeEvent} to
     * all registered listeners.
     * <P>
     * Typically a marker will be drawn by the renderer as a line perpendicular
     * to the domain axis (that the renderer is mapped to), however this is
     * entirely up to the renderer.
     *
     * @param index  the renderer index.
     * @param marker  the marker.
     * @param layer  the layer (foreground or background).
     */
    public void addDomainMarker(int index, Marker marker, Layer layer) {
        Collection markers;
        if (layer == Layer.FOREGROUND) {
            markers = (Collection) this.foregroundDomainMarkers.get(
                new Integer(index)
            );
            if (markers == null) {
                markers = new java.util.ArrayList();
                this.foregroundDomainMarkers.put(new Integer(index), markers);
            }
            markers.add(marker);
        }
        else if (layer == Layer.BACKGROUND) {
            markers = (Collection) this.backgroundDomainMarkers.get(
                new Integer(index)
            );
            if (markers == null) {
                markers = new java.util.ArrayList();
                this.backgroundDomainMarkers.put(new Integer(index), markers);
            }
            markers.add(marker);
        }
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Adds a marker for the range axis and sends a {@link PlotChangeEvent} to
     * all registered listeners.
     * <P>
     * Typically a marker will be drawn by the renderer as a line perpendicular
     * to the range axis, however this is entirely up to the renderer.
     *
     * @param marker  the marker (<code>null</code> not permitted).
     */
    public void addRangeMarker(Marker marker) {
        addRangeMarker(marker, Layer.FOREGROUND);
    }

    /**
     * Adds a marker for the range axis in the specified layer and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     * <P>
     * Typically a marker will be drawn by the renderer as a line perpendicular
     * to the range axis, however this is entirely up to the renderer.
     *
     * @param marker  the marker (<code>null</code> not permitted).
     * @param layer  the layer (foreground or background).
     */
    public void addRangeMarker(Marker marker, Layer layer) {
        addRangeMarker(0, marker, layer);
    }

    /**
     * Clears all the range markers and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     */
    public void clearRangeMarkers() {
        if (this.foregroundRangeMarkers != null) {
            this.foregroundRangeMarkers.clear();
        }
        if (this.backgroundRangeMarkers != null) {
            this.backgroundRangeMarkers.clear();
        }
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Adds a marker for a renderer and sends a {@link PlotChangeEvent} to
     * all registered listeners.
     * <P>
     * Typically a marker will be drawn by the renderer as a line perpendicular
     * to the range axis, however this is entirely up to the renderer.
     *
     * @param index  the renderer index.
     * @param marker  the marker.
     * @param layer  the layer (foreground or background).
     */
    public void addRangeMarker(int index, Marker marker, Layer layer) {
        Collection markers;
        if (layer == Layer.FOREGROUND) {
            markers = (Collection) this.foregroundRangeMarkers.get(
                new Integer(index)
            );
            if (markers == null) {
                markers = new java.util.ArrayList();
                this.foregroundRangeMarkers.put(new Integer(index), markers);
            }
            markers.add(marker);
        }
        else if (layer == Layer.BACKGROUND) {
            markers = (Collection) this.backgroundRangeMarkers.get(
                new Integer(index)
            );
            if (markers == null) {
                markers = new java.util.ArrayList();
                this.backgroundRangeMarkers.put(new Integer(index), markers);
            }
            markers.add(marker);
        }
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Clears the (foreground and background) range markers for a particular
     * renderer.
     *
     * @param index  the renderer index.
     */
    public void clearRangeMarkers(int index) {
        Integer key = new Integer(index);
        if (this.backgroundRangeMarkers != null) {
            Collection markers
                = (Collection) this.backgroundRangeMarkers.get(key);
            if (markers != null) {
                markers.clear();
            }
        }
        if (this.foregroundRangeMarkers != null) {
            Collection markers
                = (Collection) this.foregroundRangeMarkers.get(key);
            if (markers != null) {
                markers.clear();
            }
        }
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Adds an annotation to the plot and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param annotation  the annotation (<code>null</code> not permitted).
     */
    public void addAnnotation(XYAnnotation annotation) {
        if (annotation == null) {
            throw new IllegalArgumentException("Null 'annotation' argument.");
        }
        this.annotations.add(annotation);
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Removes an annotation from the plot and sends a {@link PlotChangeEvent}
     * to all registered listeners.
     *
     * @param annotation  the annotation (<code>null</code> not permitted).
     *
     * @return A boolean (indicates whether or not the annotation was removed).
     */
    public boolean removeAnnotation(XYAnnotation annotation) {
        if (annotation == null) {
            throw new IllegalArgumentException("Null 'annotation' argument.");
        }
        boolean removed = this.annotations.remove(annotation);
        if (removed) {
            notifyListeners(new PlotChangeEvent(this));
        }
        return removed;
    }

    /**
     * Clears all the annotations and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     */
    public void clearAnnotations() {
        this.annotations.clear();
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Calculates the space required for all the axes in the plot.
     *
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     *
     * @return The required space.
     */
    protected AxisSpace calculateAxisSpace(Graphics2D g2,
                                           Rectangle2D plotArea) {
        AxisSpace space = new AxisSpace();
        space = calculateDomainAxisSpace(g2, plotArea, space);
        space = calculateRangeAxisSpace(g2, plotArea, space);
        return space;
    }

    /**
 
