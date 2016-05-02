/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * -----------
 * XYPlot.java
 * -----------
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
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
 * $Id: XYPlot.java,v 1.81 2004/04/15 13:39:22 mungady Exp $
 *
 * Changes (from 21-Jun-2001)
 * --------------------------
 * 21-Jun-2001 : Removed redundant JFreeChart parameter from constructors (DG);
 * 18-Sep-2001 : Updated header and fixed DOS encoding problem (DG);
 * 15-Oct-2001 : Data source classes moved to com.jrefinery.data.* (DG);
 * 19-Oct-2001 : Removed the code for drawing the visual representation of each data point into
 *               a separate class StandardXYItemRenderer.  This will make it easier to add
 *               variations to the way the charts are drawn.  Based on code contributed by
 *               Mark Watson (DG);
 * 22-Oct-2001 : Renamed DataSource.java --> Dataset.java etc. (DG);
 * 20-Nov-2001 : Fixed clipping bug that shows up when chart is displayed inside JScrollPane (DG);
 * 12-Dec-2001 : Removed unnecessary 'throws' clauses from constructor (DG);
 * 13-Dec-2001 : Added skeleton code for tooltips.  Added new constructor. (DG);
 * 16-Jan-2002 : Renamed the tooltips class (DG);
 * 22-Jan-2002 : Added DrawInfo class, incorporating tooltips and crosshairs.  Crosshairs based
 *               on code by Jonathan Nash (DG);
 * 05-Feb-2002 : Added alpha-transparency setting based on code by Sylvain Vieujot (DG);
 * 26-Feb-2002 : Updated getMinimumXXX() and getMaximumXXX() methods to handle special case when
 *               chart is null (DG);
 * 28-Feb-2002 : Renamed Datasets.java --> DatasetUtilities.java (DG);
 * 28-Mar-2002 : The plot now registers with the renderer as a property change listener.  Also
 *               added a new constructor (DG);
 * 09-Apr-2002 : Removed the transRangeZero from the renderer.drawItem(...) method.  Moved the
 *               tooltip generator into the renderer (DG);
 * 23-Apr-2002 : Fixed bug in methods for drawing horizontal and vertical lines (DG);
 * 13-May-2002 : Small change to the draw(...) method so that it works for OverlaidXYPlot also (DG);
 * 25-Jun-2002 : Removed redundant import (DG);
 * 20-Aug-2002 : Renamed getItemRenderer() --> getRenderer(), and
 *               setXYItemRenderer() --> setRenderer() (DG);
 * 28-Aug-2002 : Added mechanism for (optional) plot annotations (DG);
 * 02-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 18-Nov-2002 : Added grid settings for both domain and range axis (previously these were set in
 *               the axes) (DG);
 * 09-Jan-2003 : Further additions to the grid settings, plus integrated plot border bug fix
 *               contributed by Gideon Krause (DG);
 * 22-Jan-2003 : Removed monolithic constructor (DG);
 * 04-Mar-2003 : Added 'no data' message, see bug report 691634.  Added secondary range markers
 *               using code contributed by Klaus Rheinwald (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 03-Apr-2003 : Added setDomainAxisLocation(...) method (DG);
 * 30-Apr-2003 : Moved annotation drawing into a separate method (DG);
 * 01-May-2003 : Added multi-pass mechanism for renderers (DG);
 * 02-May-2003 : Changed axis locations from int to AxisLocation (DG);
 * 15-May-2003 : Added an orientation attribute (DG);
 * 02-Jun-2003 : Removed range axis compatibility test (DG);
 * 05-Jun-2003 : Added domain and range grid bands (sponsored by Focus Computer Services Ltd) (DG);
 * 26-Jun-2003 : Fixed bug (757303) in getDataRange(...) method (DG);
 * 02-Jul-2003 : Added patch from bug report 698646 (secondary axes for overlaid plots) (DG);
 * 23-Jul-2003 : Added support for multiple secondary datasets, axes and renderers (DG);
 * 27-Jul-2003 : Added support for stacked XY area charts (RA);
 * 19-Aug-2003 : Implemented Cloneable (DG);
 * 01-Sep-2003 : Fixed bug where change to secondary datasets didn't generate change 
 * 08-Sep-2003 : Added internationalization via use of properties resourceBundle (RFE 690236) (AL); 
 *               event (797466) (DG)
 * 08-Sep-2003 : Changed ValueAxis API (DG);
 * 08-Sep-2003 : Fixes for serialization (NB);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 17-Sep-2003 : Fixed zooming to include secondary domain axes (DG);
 * 18-Sep-2003 : Added getSecondaryDomainAxisCount() and getSecondaryRangeAxisCount() methods 
 *               suggested by Eduardo Ramalho (RFE 808548) (DG);
 * 23-Sep-2003 : Split domain and range markers into foreground and background (DG);
 * 06-Oct-2003 : Fixed bug in clearDomainMarkers() and clearRangeMarkers() methods.  Fixed
 *               bug (815876) in addSecondaryRangeMarker(...) method.  Added new 
 *               addSecondaryDomainMarker methods (see bug id 815869) (DG);
 * 10-Nov-2003 : Added getSecondaryDomain/RangeAxisMappedToDataset(...) methods requested by 
 *               Eduardo Ramalho (DG);
 * 24-Nov-2003 : Removed unnecessary notification when updating axis anchor values (DG);
 * 21-Jan-2004 : Update for renamed method in ValueAxis (DG);
 * 25-Feb-2004 : Replaced CrosshairInfo with CrosshairState (DG);
 * 12-Mar-2004 : Fixed bug where primary renderer is always used to determine range type (DG);
 * 22-Mar-2004 : Fixed cloning bug (DG);
 * 23-Mar-2004 : Fixed more cloning bugs (DG);
 * 07-Apr-2004 : Fixed problem with axis range when the secondary renderer is stacked, see this
 *               post in the forum: http://www.jfree.org/phpBB2/viewtopic.php?t=8204 (DG);
 * 07-Apr-2004 : Added get/setDatasetRenderingOrder() methods (DG);
 *
 */

package org.jfree.chart.plot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Insets;
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

import org.apache.log4j.Logger;
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
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.renderer.RangeType;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.chart.renderer.XYItemRendererState;
import org.jfree.data.DatasetChangeEvent;
import org.jfree.data.DatasetUtilities;
import org.jfree.data.Range;
import org.jfree.data.TableXYDataset;
import org.jfree.data.XYDataset;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.Spacer;
import org.jfree.util.ObjectList;
import org.jfree.util.ObjectUtils;
import org.jfree.util.PublicCloneable;

/**
 * A general class for plotting data in the form of (x, y) pairs.  This plot can
 * use data from any class that implements the {@link XYDataset} interface.
 * <P>
 * <code>XYPlot</code> makes use of an {@link XYItemRenderer} to draw each point on the plot.
 * By using different renderers, various chart types can be produced.
 * <p>
 * The {@link org.jfree.chart.ChartFactory} class contains static methods for creating
 * pre-configured charts.
 */
public class XYPlot extends Plot implements ValueAxisPlot,
                                            RendererChangeListener,
                                            Cloneable,
                                            Serializable {

    /** The default grid line stroke. */
    public static final Stroke DEFAULT_GRIDLINE_STROKE = new BasicStroke(0.5f,
        BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_BEVEL,
        0.0f,
        new float[] {2.0f, 2.0f},
        0.0f);

    /** The default grid line paint. */
    public static final Paint DEFAULT_GRIDLINE_PAINT = Color.lightGray;

    /** The default crosshair visibility. */
    public static final boolean DEFAULT_CROSSHAIR_VISIBLE = false;

    /** The default crosshair stroke. */
    public static final Stroke DEFAULT_CROSSHAIR_STROKE = DEFAULT_GRIDLINE_STROKE;

    /** The default crosshair paint. */
    public static final Paint DEFAULT_CROSSHAIR_PAINT = Color.blue;

    /** The resourceBundle for the localization. */
    protected static ResourceBundle localizationResources 
        = ResourceBundle.getBundle("org.jfree.chart.plot.LocalizationBundle");

    /** The plot orientation. */
    private PlotOrientation orientation;

    /** The offset between the data area and the axes. */
    private Spacer axisOffset;

    /** The domain axis (used for the x-values). */
    private ValueAxis domainAxis;

    /** The domain axis location. */
    private AxisLocation domainAxisLocation;

    /** Storage for the (optional) secondary domain axes. */
    private ObjectList secondaryDomainAxes;

    /** Storage for the (optional) secondary domain axis locations. */
    private ObjectList secondaryDomainAxisLocations;

    /** The range axis (used for the y-values). */
    private ValueAxis rangeAxis;

    /** The range axis location. */
    private AxisLocation rangeAxisLocation;

    /** Storage for the (optional) secondary range axes. */
    private ObjectList secondaryRangeAxes;

    /** Storage for the (optional) secondary range axis locations. */
    private ObjectList secondaryRangeAxisLocations;

    /** The dataset. */
    private XYDataset dataset;

    /** Storage for the (optional) secondary datasets. */
    private ObjectList secondaryDatasets;

    /** Storage for keys that map secondary datasets to domain axes. */
    private ObjectList secondaryDatasetDomainAxisMap;

    /** Storage for keys that map secondary datasets to range axes. */
    private ObjectList secondaryDatasetRangeAxisMap;

    /** Object responsible for drawing the visual representation of each point on the plot. */
    private XYItemRenderer renderer;

    /** Storage for the (optional) secondary renderers. */
    private ObjectList secondaryRenderers;

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

    /** A flag that controls whether or not a domain crosshair is drawn..*/
    private boolean domainCrosshairVisible;

    /** The domain crosshair value. */
    private double domainCrosshairValue;

    /** The pen/brush used to draw the crosshair (if any). */
    private transient Stroke domainCrosshairStroke;

    /** The color used to draw the crosshair (if any). */
    private transient Paint domainCrosshairPaint;

    /** A flag that controls whether or not the crosshair locks onto actual data points. */
    private boolean domainCrosshairLockedOnData = true;

    /** A flag that controls whether or not a range crosshair is drawn..*/
    private boolean rangeCrosshairVisible;

    /** The range crosshair value. */
    private double rangeCrosshairValue;

    /** The pen/brush used to draw the crosshair (if any). */
    private transient Stroke rangeCrosshairStroke;

    /** The color used to draw the crosshair (if any). */
    private transient Paint rangeCrosshairPaint;

    /** A flag that controls whether or not the crosshair locks onto actual data points. */
    private boolean rangeCrosshairLockedOnData = true;

    /** A list of markers (optional) for the domain axis. */
    private transient List foregroundDomainMarkers;

    /** A list of markers (optional) for the domain axis. */
    private transient List backgroundDomainMarkers;

    /** A list of secondary markers (optional) for the secondary domain axis. */
    private transient Map secondaryForegroundDomainMarkers;

    /** A list of secondary markers (optional) for the secondary domain axis. */
    private transient Map secondaryBackgroundDomainMarkers;

    /** A list of markers (optional) for the range axis. */
    private transient List foregroundRangeMarkers;

    /** A list of markers (optional) for the range axis. */
    private transient List backgroundRangeMarkers;

    /** A list of secondary markers (optional) for the secondary range axis. */
    private transient Map secondaryForegroundRangeMarkers;

    /** A list of secondary markers (optional) for the secondary range axis. */
    private transient Map secondaryBackgroundRangeMarkers;

    /** A list of annotations (optional) for the plot. */
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
     * The order of the dataset rendering (STANDARD draws the secondary datasets first, then the 
     * primary dataset, so the primary data appears to be on top). 
     */
    private DatasetRenderingOrder renderingOrder = DatasetRenderingOrder.STANDARD;
    
    /** The weight for this plot (only relevant if this is a subplot in a combined plot). */
    private int weight;

    /** Log4j logging. */
    static Logger logger = Logger.getLogger(XYPlot.class);

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
        this.axisOffset = new Spacer(Spacer.ABSOLUTE, 0.0, 0.0, 0.0, 0.0);

        // allocate storage for secondary datasets, axes and renderers (all optional)
        this.secondaryDomainAxes = new ObjectList();
        this.secondaryDomainAxisLocations = new ObjectList();

        this.secondaryRangeAxes = new ObjectList();
        this.secondaryRangeAxisLocations = new ObjectList();

        this.secondaryDatasets = new ObjectList();
        this.secondaryDatasetDomainAxisMap = new ObjectList();
        this.secondaryDatasetRangeAxisMap = new ObjectList();

        this.secondaryRenderers = new ObjectList();

        this.dataset = dataset;
        if (dataset != null) {
            dataset.addChangeListener(this);
        }

        this.renderer = renderer;
        if (renderer != null) {
            renderer.setPlot(this);
            renderer.addChangeListener(this);
        }

        this.domainAxis = domainAxis;
        if (domainAxis != null) {
            domainAxis.setPlot(this);
            domainAxis.addChangeListener(this);
        }
        this.domainAxisLocation = AxisLocation.BOTTOM_OR_LEFT;

        this.rangeAxis = rangeAxis;
        if (rangeAxis != null) {
            rangeAxis.setPlot(this);
            rangeAxis.addChangeListener(this);
        }
        this.rangeAxisLocation = AxisLocation.TOP_OR_LEFT;

        this.domainGridlinesVisible = true;
        this.domainGridlineStroke = DEFAULT_GRIDLINE_STROKE;
        this.domainGridlinePaint = DEFAULT_GRIDLINE_PAINT;

        this.foregroundDomainMarkers = new java.util.ArrayList();
        this.backgroundDomainMarkers = new java.util.ArrayList();
        this.secondaryForegroundDomainMarkers = new HashMap();
        this.secondaryBackgroundDomainMarkers = new HashMap();
        
        this.rangeGridlinesVisible = true;
        this.rangeGridlineStroke = DEFAULT_GRIDLINE_STROKE;
        this.rangeGridlinePaint = DEFAULT_GRIDLINE_PAINT;

        this.foregroundRangeMarkers = new java.util.ArrayList();
        this.backgroundRangeMarkers = new java.util.ArrayList();
        this.secondaryForegroundRangeMarkers = new HashMap();
        this.secondaryBackgroundRangeMarkers = new HashMap();
        
        this.domainCrosshairVisible = false;
        this.domainCrosshairValue = 0.0;
        this.domainCrosshairStroke = DEFAULT_CROSSHAIR_STROKE;
        this.domainCrosshairPaint = DEFAULT_CROSSHAIR_PAINT;

        this.rangeCrosshairVisible = false;
        this.rangeCrosshairValue = 0.0;
        this.rangeCrosshairStroke = DEFAULT_CROSSHAIR_STROKE;
        this.rangeCrosshairPaint = DEFAULT_CROSSHAIR_PAINT;

    }

    /**
     * Returns the plot type as a string.
     *
     * @return a short string describing the type of plot.
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
            throw new IllegalArgumentException("XYPlot.setOrientation(...): null not allowed.");
        }
        if (orientation != this.orientation) {
            this.orientation = orientation;
            notifyListeners(new PlotChangeEvent(this));
        }
    }

    /**
     * Returns the axis offset.
     *
     * @return The axis offset.
     */
    public Spacer getAxisOffset() {
        return this.axisOffset;
    }

    /**
     * Sets the axis offsets (gap between the data area and the axes).
     *
     * @param offset  the offset.
     */
    public void setAxisOffset(Spacer offset) {
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

        ValueAxis result = this.domainAxis;

        if (result == null) {
            Plot parent = getParent();
            if (parent instanceof XYPlot) {
                XYPlot xy = (XYPlot) parent;
                result = xy.getDomainAxis();
            }
        }

        return result;

    }

    /**
     * Sets the domain axis for the plot.
     *
     * @param axis  the new axis.
     */
    public void setDomainAxis(ValueAxis axis) {

        if (axis != null) {
            axis.setPlot(this);
            axis.addChangeListener(this);
        }

        // plot is likely registered as a listener with the existing axis...
        if (this.domainAxis != null) {
            this.domainAxis.removeChangeListener(this);
        }

        this.domainAxis = axis;
        if (axis != null) {
            axis.configure();
            axis.addChangeListener(this);
        }
        notifyListeners(new PlotChangeEvent(this));

    }

    /**
     * Returns the location of the domain axis.
     *
     * @return the location.
     */
    public AxisLocation getDomainAxisLocation() {
        return this.domainAxisLocation;
    }

    /**
     * Sets the location of the domain axis and sends a {@link PlotChangeEvent} to all registered
     * listeners.
     *
     * @param location  the location (<code>null</code> not permitted).
     */
    public void setDomainAxisLocation(AxisLocation location) {
        // defer argument checking...
        setDomainAxisLocation(location, true);
    }

    /**
     * Sets the location of the domain axis and, if requested, sends a {@link PlotChangeEvent} to
     * all registered listeners.
     *
     * @param location  the location (<code>null</code> not permitted).
     * @param notify  notify listeners?
     */
    public void setDomainAxisLocation(AxisLocation location, boolean notify) {
        if (location == null) {
            throw new IllegalArgumentException("Null 'location' argument.");   
        }
        if (location != this.domainAxisLocation) {
            this.domainAxisLocation = location;
            if (notify) {
                notifyListeners(new PlotChangeEvent(this));
            }
        }
    }

    /**
     * Returns the edge for the domain axis (taking into account the plot's orientation.
     *
     * @return The edge.
     */
    public RectangleEdge getDomainAxisEdge() {
        return Plot.resolveDomainAxisLocation(this.domainAxisLocation, this.orientation);
    }

    /**
     * Returns a secondary domain axis.
     *
     * @param index  the axis index.
     *
     * @return The axis (<code>null</code> possible).
     */
    public ValueAxis getSecondaryDomainAxis(int index) {
        ValueAxis result = null;
        if (index < this.secondaryDomainAxes.size()) {
            result = (ValueAxis) this.secondaryDomainAxes.get(index);
        }
        if (result == null) {
            Plot parent = getParent();
            if (parent instanceof XYPlot) {
                XYPlot xy = (XYPlot) parent;
                result = xy.getSecondaryDomainAxis(index);
            }
        }
        return result;
    }

    /**
     * Sets a secondary domain axis.
     *
     * @param index  the axis index.
     * @param axis  the axis.
     */
    public void setSecondaryDomainAxis(int index, ValueAxis axis) {

        ValueAxis existing = getSecondaryDomainAxis(index);
        if (existing != null) {
            existing.removeChangeListener(this);
        }

        if (axis != null) {
            axis.setPlot(this);
        }

        this.secondaryDomainAxes.set(index, axis);
        if (axis != null) {
            axis.configure();
            axis.addChangeListener(this);
        }
        notifyListeners(new PlotChangeEvent(this));

    }

    /**
     * Returns the number of secondary domain axes.
     * 
     * @return The axis count.
     */
    public int getSecondaryDomainAxisCount() {
        return this.secondaryDomainAxes.size();
    }
    
    /**
     * Clears the secondary domain axes from the plot.
     */
    public void clearSecondaryDomainAxes() {
        for (int i = 0; i < this.secondaryDomainAxes.size(); i++) {
            ValueAxis axis = (ValueAxis) this.secondaryDomainAxes.get(i);
            if (axis != null) {
                axis.removeChangeListener(this);
            }
        }
        this.secondaryDomainAxes.clear();
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Configures the secondary domain axes.
     */
    public void configureSecondaryDomainAxes() {
        for (int i = 0; i < this.secondaryDomainAxes.size(); i++) {
            ValueAxis axis = (ValueAxis) this.secondaryDomainAxes.get(i);
            if (axis != null) {
                axis.configure();
            }
        }
    }

    /**
     * Returns the location for a secondary domain axis.  If this hasn't been set explicitly,
     * the method returns the location that is opposite to the primary domain axis location.
     *
     * @param index  the axis index.
     *
     * @return The location (never <code>null</code>).
     */
    public AxisLocation getSecondaryDomainAxisLocation(int index) {
        AxisLocation result = null;
        if (index < this.secondaryDomainAxisLocations.size()) {
            result = (AxisLocation) this.secondaryDomainAxisLocations.get(index);
        }
        if (result == null) {
            result = AxisLocation.getOpposite(this.domainAxisLocation);
        }
        return result;
    }

    /**
     * Sets the location for a secondary domain axis and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param index  the axis index.
     * @param location  the location (<code>null</code> permitted).
     */
    public void setSecondaryDomainAxisLocation(int index, AxisLocation location) {
        this.secondaryDomainAxisLocations.set(index, location);
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns the edge for a secondary domain axis.
     *
     * @param index  the axis index.
     *
     * @return The edge.
     */
    public RectangleEdge getSecondaryDomainAxisEdge(int index) {
        AxisLocation location = getSecondaryDomainAxisLocation(index);
        RectangleEdge result = Plot.resolveDomainAxisLocation(location, this.orientation);
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
     * @return the range axis.
     */
    public ValueAxis getRangeAxis() {

        ValueAxis result = this.rangeAxis;

        if (result == null) {
            Plot parent = getParent();
            if (parent instanceof XYPlot) {
                XYPlot xy = (XYPlot) parent;
                result = xy.getRangeAxis();
            }
        }

        return result;

    }

    /**
     * Sets the range axis for the plot.
     * <P>
     * An exception is thrown if the new axis and the plot are not mutually compatible.
     *
     * @param axis the new axis (null permitted).
     *
     */
    public void setRangeAxis(ValueAxis axis)  {

        if (axis != null) {
            axis.setPlot(this);
        }

        // plot is likely registered as a listener with the existing axis...
        if (this.rangeAxis != null) {
            this.rangeAxis.removeChangeListener(this);
        }

        this.rangeAxis = axis;
        if (axis != null) {
            axis.configure();
            axis.addChangeListener(this);
        }
        notifyListeners(new PlotChangeEvent(this));

    }

    /**
     * Returns the location of the range axis.
     *
     * @return the location (never <code>null</code>).
     */
    public AxisLocation getRangeAxisLocation() {
        return this.rangeAxisLocation;
    }

    /**
     * Sets the location of the range axis and sends a {@link PlotChangeEvent} to all registered
     * listeners.
     *
     * @param location  the location (<code>null</code> not permitted).
     */
    public void setRangeAxisLocation(AxisLocation location) {
        // defer argument checking...
        setRangeAxisLocation(location, true);
    }

    /**
     * Sets the location of the range axis and, if requested, sends a {@link PlotChangeEvent}
     * to all registered listeners.
     *
     * @param location  the location (<code>null</code> not permitted).
     * @param notify  notify listeners?
     */
    public void setRangeAxisLocation(AxisLocation location, boolean notify) {
        if (location == null) {
            throw new IllegalArgumentException("Null 'location' argument.");   
        }
        if (location != this.rangeAxisLocation) {
            this.rangeAxisLocation = location;
            if (notify) {
                notifyListeners(new PlotChangeEvent(this));
            }
        }
    }

    /**
     * Returns the edge for the range axis.
     *
     * @return The range axis edge.
     */
    public RectangleEdge getRangeAxisEdge() {
        return Plot.resolveRangeAxisLocation(this.rangeAxisLocation, this.orientation);
    }

    /**
     * Returns a secondary range axis.
     *
     * @param index  the axis index.
     *
     * @return The axis (<code>null</code> possible).
     */
    public ValueAxis getSecondaryRangeAxis(int index) {
        ValueAxis result = null;
        if (index < this.secondaryRangeAxes.size()) {
            result = (ValueAxis) this.secondaryRangeAxes.get(index);
        }
        if (result == null) {
            Plot parent = getParent();
            if (parent instanceof XYPlot) {
                XYPlot xy = (XYPlot) parent;
                result = xy.getSecondaryRangeAxis(index);
            }
        }
        return result;
    }

    /**
     * Sets a secondary range axis.
     *
     * @param index  the axis index.
     * @param axis  the axis.
     */
    public void setSecondaryRangeAxis(int index, ValueAxis axis) {

        ValueAxis existing = getSecondaryRangeAxis(index);
        if (existing != null) {
            existing.removeChangeListener(this);
        }

        if (axis != null) {
            axis.setPlot(this);
        }

        this.secondaryRangeAxes.set(index, axis);
        if (axis != null) {
            axis.configure();
            axis.addChangeListener(this);
        }
        notifyListeners(new PlotChangeEvent(this));

    }

    /**
     * Returns the number of secondary range axes.
     * 
     * @return The axis count.
     */
    public int getSecondaryRangeAxisCount() {
        return this.secondaryRangeAxes.size();
    }
    
    /**
     * Clears the secondary range axes from the plot.
     */
    public void clearSecondaryRangeAxes() {
        for (int i = 0; i < this.secondaryRangeAxes.size(); i++) {
            ValueAxis axis = (ValueAxis) this.secondaryRangeAxes.get(i);
            if (axis != null) {
                axis.removeChangeListener(this);
            }
        }
        this.secondaryRangeAxes.clear();
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Configures the secondary range axes.
     */
    public void configureSecondaryRangeAxes() {
        for (int i = 0; i < this.secondaryRangeAxes.size(); i++) {
            ValueAxis axis = (ValueAxis) this.secondaryRangeAxes.get(i);
            if (axis != null) {
                axis.configure();
            }
        }
    }

    /**
     * Returns the location for a secondary range axis.  If this hasn't been set explicitly,
     * the method returns the location that is opposite to the primary range axis location.
     *
     * @param index  the axis index.
     *
     * @return The location (never <code>null</code>).
     */
    public AxisLocation getSecondaryRangeAxisLocation(int index) {
        AxisLocation result = null;
        if (index < this.secondaryRangeAxisLocations.size()) {
            result = (AxisLocation) this.secondaryRangeAxisLocations.get(index);
        }
        if (result == null) {
            result = AxisLocation.getOpposite(this.rangeAxisLocation);
        }
        return result;
    }

    /**
     * Sets the location for a secondary range axis and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param index  the axis index.
     * @param location  the location (<code>null</code> permitted).
     */
    public void setSecondaryRangeAxisLocation(int index, AxisLocation location) {
        this.secondaryRangeAxisLocations.set(index, location);
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns the edge for a secondary range axis.
     *
     * @param index  the axis index.
     *
     * @return The edge.
     */
    public RectangleEdge getSecondaryRangeAxisEdge(int index) {
        AxisLocation location = getSecondaryRangeAxisLocation(index);
        RectangleEdge result = Plot.resolveRangeAxisLocation(location, this.orientation);
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
        return this.dataset;
    }

    /**
     * Sets the dataset for the plot, replacing the existing dataset if there is one.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     */
    public void setDataset(XYDataset dataset) {

        // if there is an existing dataset, remove the plot from the list of change listeners...
        XYDataset existing = this.dataset;
        if (existing != null) {
            existing.removeChangeListener(this);
        }

        // set the new dataset, and register the chart as a change listener...
        this.dataset = dataset;
        if (dataset != null) {
            setDatasetGroup(dataset.getGroup());
            dataset.addChangeListener(this);
        }

        // send a dataset change event to self...
        DatasetChangeEvent event = new DatasetChangeEvent(this, dataset);
        datasetChanged(event);

    }

    /**
     * Returns one of the secondary datasets.
     *
     * @param index  the dataset index.
     *
     * @return The dataset (possibly <code>null</code>).
     */
    public XYDataset getSecondaryDataset(int index) {
        XYDataset result = null;
        if (this.secondaryDatasets.size() > index) {
            result = (XYDataset) this.secondaryDatasets.get(index);
        }
        return result;
    }

    /**
     * Returns the number of secondary datasets.
     *
     * @return The number of secondary datasets.
     */
    public int getSecondaryDatasetCount() {
        return this.secondaryDatasets.size();
    }

    /**
     * Adds or changes a secondary dataset for the plot.
     *
     * @param index  the dataset index.
     * @param dataset  the dataset (<code>null</code> permitted).
     */
    public void setSecondaryDataset(int index, XYDataset dataset) {
        XYDataset existing = (XYDataset) this.secondaryDatasets.get(index);
        if (existing != null) {
            existing.removeChangeListener(this);
        }
        this.secondaryDatasets.set(index, dataset);
        if (dataset != null) {
            dataset.addChangeListener(this);
        }

        // map dataset to main axis by default
        if (index >= this.secondaryDatasetRangeAxisMap.size()) {
            this.secondaryDatasetRangeAxisMap.set(index, null);
        }
        if (index >= this.secondaryDatasetDomainAxisMap.size()) {
            this.secondaryDatasetDomainAxisMap.set(index, null);
        }

        // send a dataset change event to self...
        DatasetChangeEvent event = new DatasetChangeEvent(this, dataset);
        datasetChanged(event);
    }

    /**
     * Maps a secondary dataset to a particular domain axis.
     *
     * @param index  the dataset index (zero-based).
     * @param key  the key (<code>null</code> for primary axis, or the index of the secondary
     *             axis).
     */
    public void mapSecondaryDatasetToDomainAxis(int index, Integer key) {
        this.secondaryDatasetDomainAxisMap.set(index, key);
        // fake a dataset change event to update axes...
        datasetChanged(new DatasetChangeEvent(this, this.dataset));
    }

    /**
     * Maps a secondary dataset to a particular range axis.
     *
     * @param index  the dataset index (zero-based).
     * @param key  the key (<code>null</code> for primary axis, or the index of the secondary
     *             axis).
     */
    public void mapSecondaryDatasetToRangeAxis(int index, Integer key) {
        this.secondaryDatasetRangeAxisMap.set(index, key);
        // fake a dataset change event to update axes...
        datasetChanged(new DatasetChangeEvent(this, this.dataset));
    }

    /**
     * Returns the item renderer.
     *
     * @return The item renderer (possibly <code>null</code>).
     */
    public XYItemRenderer getRenderer() {
        return this.renderer;
    }

    /**
     * Sets the item renderer, and notifies all listeners of a change to the plot.
     * <P>
     * If the renderer is set to <code>null</code>, no chart will be drawn.
     *
     * @param renderer  the new renderer (<code>null</code> permitted).
     */
    public void setRenderer(XYItemRenderer renderer) {

        if (this.renderer != null) {
            this.renderer.removeChangeListener(this);
        }

        this.renderer = renderer;
        if (renderer != null) {
            renderer.setPlot(this);
        }

        notifyListeners(new PlotChangeEvent(this));

    }

    /**
     * Returns a secondary renderer.
     *
     * @param index  the renderer index.
     *
     * @return The renderer (possibly <code>null</code>).
     */
    public XYItemRenderer getSecondaryRenderer(int index) {
        XYItemRenderer result = null;
        if (this.secondaryRenderers.size() > index) {
            result = (XYItemRenderer) this.secondaryRenderers.get(index);
        }
        return result;

    }

    /**
     * Sets a secondary renderer.  A {@link PlotChangeEvent} is sent to all registered listeners.
     *
     * @param index  the index.
     * @param renderer  the renderer.
     */
    public void setSecondaryRenderer(int index, XYItemRenderer renderer) {
        XYItemRenderer existing = getSecondaryRenderer(index);
        if (existing != null) {
            existing.removeChangeListener(this);
        }
        this.secondaryRenderers.set(index, renderer);
        if (renderer != null) {
            renderer.setPlot(this);
        }
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns the dataset rendering order.
     *
     * @return the order (never <code>null</code>).
     */
    public DatasetRenderingOrder getDatasetRenderingOrder() {
        return this.renderingOrder;
    }

    /**
     * Sets the rendering order and sends a {@link PlotChangeEvent} to all registered listeners.
     * By default, the plot renders the secondary dataset first, then the primary dataset (so that
     * the primary dataset overlays the secondary dataset).  You can reverse this if you want to.
     *
     * @param order  the rendering order (<code>null</code> not permitted).
     */
    public void setDatasetRenderingOrder(DatasetRenderingOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Null 'order' argument.");   
        }
        this.renderingOrder = order;
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns the renderer for the specified dataset.
     * 
     * @param d  the dataset (<code>null</code> permitted).
     * 
     * @return The renderer (possibly <code>null</code>).
     */
    public XYItemRenderer getRendererForDataset(XYDataset d) {
        XYItemRenderer result = null;
        if (this.dataset == d) {
            result = this.renderer;   
        }
        else {
            for (int i = 0; i < this.secondaryDatasets.size(); i++) {
                if (this.secondaryDatasets.get(i) == d) {
                    result = (XYItemRenderer) this.secondaryRenderers.get(i);   
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Returns the weight for this plot when it is used as a subplot within a
     * combined plot.
     *
     * @return the weight.
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
     * Returns <code>true</code> if the domain gridlines are visible, and <code>false<code>
     * otherwise.
     *
     * @return <code>true</code> or <code>false</code>.
     */
    public boolean isDomainGridlinesVisible() {
        return this.domainGridlinesVisible;
    }

    /**
     * Sets the flag that controls whether or not the domain grid-lines are visible.
     * <p>
     * If the flag value is changed, a {@link PlotChangeEvent} is sent to all registered listeners.
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
     * Returns the stroke for the grid-lines (if any) plotted against the domain axis.
     *
     * @return the stroke.
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
     * Returns the paint for the grid lines (if any) plotted against the domain axis.
     *
     * @return the paint.
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
     * Returns <code>true</code> if the range axis grid is visible, and <code>false<code>
     * otherwise.
     *
     * @return <code>true</code> or <code>false</code>.
     */
    public boolean isRangeGridlinesVisible() {
        return this.rangeGridlinesVisible;
    }

    /**
     * Sets the flag that controls whether or not the range axis grid lines are visible.
     * <p>
     * If the flag value is changed, a {@link PlotChangeEvent} is sent to all registered listeners.
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
     * Returns the stroke for the grid lines (if any) plotted against the range axis.
     *
     * @return the stroke.
     */
    public Stroke getRangeGridlineStroke() {
        return this.rangeGridlineStroke;
    }

    /**
     * Sets the stroke for the grid lines plotted against the range axis.
     * <p>
     * If you set this to <code>null</code>, no grid lines will be drawn.
     *
     * @param stroke  the stroke (<code>null</code> permitted).
     */
    public void setRangeGridlineStroke(Stroke stroke) {
        this.rangeGridlineStroke = stroke;
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns the paint for the grid lines (if any) plotted against the range axis.
     *
     * @return the paint.
     */
    public Paint getRangeGridlinePaint() {
        return this.rangeGridlinePaint;
    }

    /**
     * Sets the paint for the grid lines plotted against the range axis.
     * <p>
     * If you set this to <code>null</code>, no grid lines will be drawn.
     *
     * @param paint  the paint (<code>null</code> permitted).
     */
    public void setRangeGridlinePaint(Paint paint) {
        this.rangeGridlinePaint = paint;
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Returns the paint used for the domain tick bands.  If this is <code>null</code>,
     * no tick bands will be drawn.
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
     * Returns the paint used for the range tick bands.  If this is <code>null</code>,
     * no tick bands will be drawn.
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
     * Adds a marker for the domain axis and sends a {@link PlotChangeEvent} to all
     * registered listeners.
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
        if (marker == null) {
            throw new IllegalArgumentException("Null 'marker' argument.");   
        }
        if (layer == Layer.FOREGROUND) {
            if (this.foregroundDomainMarkers == null) {
                this.foregroundDomainMarkers = new java.util.ArrayList();
            }
            this.foregroundDomainMarkers.add(marker);
            notifyListeners(new PlotChangeEvent(this));
        }
        else if (layer == Layer.BACKGROUND) {
            if (this.backgroundDomainMarkers == null) {
                this.backgroundDomainMarkers = new java.util.ArrayList();
            }
            this.backgroundDomainMarkers.add(marker);
            notifyListeners(new PlotChangeEvent(this));            
        }
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
     * Adds a marker for a secondary domain axis and sends a {@link PlotChangeEvent}
     * to all registered listeners.
     * <P>
     * Typically a marker will be drawn by the renderer as a line perpendicular
     * to the domain axis, however this is entirely up to the renderer.
     *
     * @param marker  the marker.
     */
    public void addSecondaryDomainMarker(Marker marker) {
        addSecondaryDomainMarker(0, marker, Layer.FOREGROUND);
    }
    
    /**
     * Adds a marker for a secondary domain axis and sends a {@link PlotChangeEvent} to
     * all registered listeners.
     * <P>
     * Typically a marker will be drawn by the renderer as a line perpendicular
     * to the domain axis, however this is entirely up to the renderer.
     *
     * @param index  the secondary axis index.
     * @param marker  the marker.
     * @param layer  the layer (foreground or background).
     */
    public void addSecondaryDomainMarker(int index, Marker marker, Layer layer) {
        Collection markers;
        if (layer == Layer.FOREGROUND) {
            markers = (Collection) this.secondaryForegroundDomainMarkers.get(new Integer(index));
            if (markers == null) {
                markers = new java.util.ArrayList();
                this.secondaryForegroundDomainMarkers.put(new Integer(index), markers);
            }
            markers.add(marker);
        }
        else if (layer == Layer.BACKGROUND) {
            markers = (Collection) this.secondaryBackgroundDomainMarkers.get(new Integer(index));
            if (markers == null) {
                markers = new java.util.ArrayList();
                this.secondaryBackgroundDomainMarkers.put(new Integer(index), markers);
            }
            markers.add(marker);            
        }
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Adds a marker for the range axis and sends a {@link PlotChangeEvent} to all
     * registered listeners.
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
        if (marker == null) {
            throw new IllegalArgumentException("Null 'marker' argument.");   
        }
        if (layer == Layer.FOREGROUND) {
            if (this.foregroundRangeMarkers == null) {
                this.foregroundRangeMarkers = new java.util.ArrayList();
            }
            this.foregroundRangeMarkers.add(marker);
            notifyListeners(new PlotChangeEvent(this));
        }
        else if (layer == Layer.BACKGROUND) {
            if (this.backgroundRangeMarkers == null) {
                this.backgroundRangeMarkers = new java.util.ArrayList();
            }
            this.backgroundRangeMarkers.add(marker);
            notifyListeners(new PlotChangeEvent(this));            
        }
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
     * Adds a secondary marker for the range axis.
     * <P>
     * Typically a marker will be drawn by the renderer as a line perpendicular
     * to the range axis, however this is entirely up to the renderer.
     *
     * @param marker  the marker.
     */
    public void addSecondaryRangeMarker(Marker marker) {
        addSecondaryRangeMarker(0, marker, Layer.FOREGROUND);
    }
    
    /**
     * Adds a marker for a secondary range axis and sends a {@link PlotChangeEvent} to
     * all registered listeners.
     * <P>
     * Typically a marker will be drawn by the renderer as a line perpendicular
     * to the range axis, however this is entirely up to the renderer.
     *
     * @param index  the secondary axis index.
     * @param marker  the marker.
     * @param layer  the layer (foreground or background).
     */
    public void addSecondaryRangeMarker(int index, Marker marker, Layer layer) {
        Collection markers;
        if (layer == Layer.FOREGROUND) {
            markers = (Collection) this.secondaryForegroundRangeMarkers.get(new Integer(index));
            if (markers == null) {
                markers = new java.util.ArrayList();
                this.secondaryForegroundRangeMarkers.put(new Integer(index), markers);
            }
            markers.add(marker);
        }
        else if (layer == Layer.BACKGROUND) {
            markers = (Collection) this.secondaryBackgroundRangeMarkers.get(new Integer(index));
            if (markers == null) {
                markers = new java.util.ArrayList();
                this.secondaryBackgroundRangeMarkers.put(new Integer(index), markers);
            }
            markers.add(marker);            
        }
        notifyListeners(new PlotChangeEvent(this));
    }

    /**
     * Clears the (foreground and background) range markers for a particular secondary range axis.
     * 
     * @param index  the secondary range axis index.
     */
    public void clearSecondaryRangeMarkers(int index) {
        Integer key = new Integer(index);
        Collection markers = (Collection) this.secondaryBackgroundRangeMarkers.get(key);
        if (markers != null) {
            markers.clear();
        }
        markers = (Collection) this.secondaryForegroundRangeMarkers.get(key);
        if (markers != null) {
            markers.clear();
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
        if (this.annotations == null) {
            this.annotations = new java.util.ArrayList();
        }
        this.annotations.add(annotation);
        notifyListeners(new PlotChangeEvent(this));

    }

    /**
     * Clears all the annotations and sends a {@link PlotChangeEvent} to all registered
     * listeners.
     */
    public void clearAnnotations() {
        if (this.annotations != null) {
            this.annotations.clear();
            notifyListeners(new PlotChangeEvent(this));
        }
    }

    /**
     * Calculates the space required for all the axes in the plot.
     *
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     *
     * @return The required space.
     */
    protected AxisSpace calculateAxisSpace(Graphics2D g2, Rectangle2D plotArea) {
        AxisSpace space = new AxisSpace();
        space = calculateDomainAxisSpace(g2, plotArea, space);
        space = calculateRangeAxisSpace(g2, plotArea, space);
        return space;
    }

    /**
     * Calculates the space required for the domain axis/axes.
     *
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     * @param space  a carrier for the result (<code>null</code> permitted).
     *
     * @return  The required space.
     */
    protected AxisSpace calculateDomainAxisSpace(Graphics2D g2, Rectangle2D plotArea,
                                                 AxisSpace space) {

        if (space == null) {
            space = new AxisSpace();
        }

        // reserve some space for the domain axis...
        if (this.fixedDomainAxisSpace != null) {
            if (this.orientation == PlotOrientation.HORIZONTAL) {
                space.ensureAtLeast(this.fixedDomainAxisSpace.getLeft(), RectangleEdge.LEFT);
                space.ensureAtLeast(this.fixedDomainAxisSpace.getRight(), RectangleEdge.RIGHT);
            }
            else if (this.orientation == PlotOrientation.VERTICAL) {
                space.ensureAtLeast(this.fixedDomainAxisSpace.getTop(), RectangleEdge.TOP);
                space.ensureAtLeast(this.fixedDomainAxisSpace.getBottom(), RectangleEdge.BOTTOM);
            }
        }
        else {
            // reserve space for the primary domain axis...
            if (this.domainAxis != null) {
                space = this.domainAxis.reserveSpace(g2, this, plotArea, getDomainAxisEdge(),
                                                     space);
            }

            // reserve space for any secondary domain axes...
            for (int i = 0; i < this.secondaryDomainAxes.size(); i++) {
                Axis secondaryDomainAxis = getSecondaryDomainAxis(i);
                if (secondaryDomainAxis != null) {
                    RectangleEdge edge = getSecondaryDomainAxisEdge(i);
                    space = secondaryDomainAxis.reserveSpace(g2, this, plotArea, edge, space);
                }
            }
        }

        return space;

    }

    /**
     * Calculates the space required for the range axis/axes.
     *
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     * @param space  a carrier for the result (<code>null</code> permitted).
     *
     * @return  The required space.
     */
    protected AxisSpace calculateRangeAxisSpace(Graphics2D g2, Rectangle2D plotArea,
                                                AxisSpace space) {

        if (space == null) {
            space = new AxisSpace();
        }

        // reserve some space for the range axis...
        if (this.fixedRangeAxisSpace != null) {
            if (this.orientation == PlotOrientation.HORIZONTAL) {
                space.ensureAtLeast(this.fixedRangeAxisSpace.getTop(), RectangleEdge.TOP);
                space.ensureAtLeast(this.fixedRangeAxisSpace.getBottom(), RectangleEdge.BOTTOM);
            }
            else if (this.orientation == PlotOrientation.VERTICAL) {
                space.ensureAtLeast(this.fixedRangeAxisSpace.getLeft(), RectangleEdge.LEFT);
                space.ensureAtLeast(this.fixedRangeAxisSpace.getRight(), RectangleEdge.RIGHT);
            }
        }
        else {
            Axis rangeAxis1 = this.rangeAxis;
            if (rangeAxis1 != null) {
                space = rangeAxis1.reserveSpace(g2, this, plotArea, getRangeAxisEdge(), space);
            }

            // reserve space for the secondary range axes (if any)...
            for (int i = 0; i < this.secondaryRangeAxes.size(); i++) {
                Axis secondaryRangeAxis = getSecondaryRangeAxis(i);
                if (secondaryRangeAxis != null) {
                    RectangleEdge edge = getSecondaryRangeAxisEdge(i);
                    space = secondaryRangeAxis.reserveSpace(g2, this, plotArea, edge, space);
                }
            }
        }
        return space;

    }

    /**
     * Draws the plot on a Java 2D graphics device (such as the screen or a printer).
     * <P>
     * This plot relies on an {@link XYItemRenderer} to draw each item in the plot.  This
     * allows the visual representation of the data to be changed easily.
     * <P>
     * The optional info argument collects information about the rendering of
     * the plot (dimensions, tooltip information etc).  Just pass in <code>null</code> if
     * you do not need this information.
     *
     * @param g2  the graphics device.
     * @param area  the area within which the plot (including axes and labels) should be drawn.
     * @param parentState  the state from the parent plot, if there is one.
     * @param state  collects chart drawing information (<code>null</code> permitted). 
     */
    public void draw(Graphics2D g2, 
                     Rectangle2D area, 
                     PlotState parentState,
                     PlotRenderingInfo state) {
        draw(g2, area, null, parentState, state);                          
    }
    
    /**
     * Draws the plot within the specified area on a graphics device.
     * 
     * @param g2  the graphics device.
     * @param area  the plot area (in Java2D space).
     * @param anchor  an anchor point in Java2D space (<code>null</code> permitted).
     * @param parentState  the state from the parent plot, if there is one (<code>null</code> 
     *                     permitted).
     * @param info  collects chart drawing information (<code>null</code> permitted).
     */
    public void draw(Graphics2D g2,
                     Rectangle2D area,
                     Point2D anchor,
                     PlotState parentState,
                     PlotRenderingInfo info) {

        if (logger.isDebugEnabled()) {
            logger.debug("Entering draw() method, plot area = " + area.toString());   
        }
        
        // if the plot area is too small, just return...
        boolean b1 = (area.getWidth() <= MINIMUM_WIDTH_TO_DRAW);
        boolean b2 = (area.getHeight() <= MINIMUM_HEIGHT_TO_DRAW);
        if (b1 || b2) {
            return;
        }

        // record the plot area...
        if (info != null) {
            info.setPlotArea(area);
        }

        // adjust the drawing area for the plot insets (if any)...
        Insets insets = getInsets();
        if (insets != null) {
            area.setRect(area.getX() + insets.left,
                         area.getY() + insets.top,
                         area.getWidth() - insets.left - insets.right,
                         area.getHeight() - insets.top - insets.bottom);
        }

    
