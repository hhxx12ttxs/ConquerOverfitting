/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
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
 * ------------
 * PiePlot.java
 * ------------
 * (C) Copyright 2000-2008, by Andrzej Porebski and Contributors.
 *
 * Original Author:  Andrzej Porebski;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *                   Martin Cordova (percentages in labels);
 *                   Richard Atkinson (URL support for image maps);
 *                   Christian W. Zuckschwerdt;
 *                   Arnaud Lelievre;
 *                   Martin Hilpert (patch 1891849);
 *                   Andreas Schroeder (very minor);
 *                   Christoph Beck (bug 2121818);
 *
 * Changes
 * -------
 * 21-Jun-2001 : Removed redundant JFreeChart parameter from constructors (DG);
 * 18-Sep-2001 : Updated header (DG);
 * 15-Oct-2001 : Data source classes moved to com.jrefinery.data.* (DG);
 * 19-Oct-2001 : Moved series paint and stroke methods from JFreeChart.java to
 *               Plot.java (DG);
 * 22-Oct-2001 : Renamed DataSource.java --> Dataset.java etc. (DG);
 * 13-Nov-2001 : Modified plot subclasses so that null axes are possible for
 *               pie plot (DG);
 * 17-Nov-2001 : Added PieDataset interface and amended this class accordingly,
 *               and completed removal of BlankAxis class as it is no longer
 *               required (DG);
 * 19-Nov-2001 : Changed 'drawCircle' property to 'circular' property (DG);
 * 21-Nov-2001 : Added options for exploding pie sections and filled out range
 *               of properties (DG);
 *               Added option for percentages in chart labels, based on code
 *               by Martin Cordova (DG);
 * 30-Nov-2001 : Changed default font from "Arial" --> "SansSerif" (DG);
 * 12-Dec-2001 : Removed unnecessary 'throws' clause in constructor (DG);
 * 13-Dec-2001 : Added tooltips (DG);
 * 16-Jan-2002 : Renamed tooltips class (DG);
 * 22-Jan-2002 : Fixed bug correlating legend labels with pie data (DG);
 * 05-Feb-2002 : Added alpha-transparency to plot class, and updated
 *               constructors accordingly (DG);
 * 06-Feb-2002 : Added optional background image and alpha-transparency to Plot
 *               and subclasses.  Clipped drawing within plot area (DG);
 * 26-Mar-2002 : Added an empty zoom method (DG);
 * 18-Apr-2002 : PieDataset is no longer sorted (oldman);
 * 23-Apr-2002 : Moved dataset from JFreeChart to Plot.  Added
 *               getLegendItemLabels() method (DG);
 * 19-Jun-2002 : Added attributes to control starting angle and direction
 *               (default is now clockwise) (DG);
 * 25-Jun-2002 : Removed redundant imports (DG);
 * 02-Jul-2002 : Fixed sign of percentage bug introduced in 0.9.2 (DG);
 * 16-Jul-2002 : Added check for null dataset in getLegendItemLabels() (DG);
 * 30-Jul-2002 : Moved summation code to DatasetUtilities (DG);
 * 05-Aug-2002 : Added URL support for image maps - new member variable for
 *               urlGenerator, modified constructor and minor change to the
 *               draw method (RA);
 * 18-Sep-2002 : Modified the percent label creation and added setters for the
 *               formatters (AS);
 * 24-Sep-2002 : Added getLegendItems() method (DG);
 * 02-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 09-Oct-2002 : Added check for null entity collection (DG);
 * 30-Oct-2002 : Changed PieDataset interface (DG);
 * 18-Nov-2002 : Changed CategoryDataset to TableDataset (DG);
 * 02-Jan-2003 : Fixed "no data" message (DG);
 * 23-Jan-2003 : Modified to extract data from rows OR columns in
 *               CategoryDataset (DG);
 * 14-Feb-2003 : Fixed label drawing so that foreground alpha does not apply
 *               (bug id 685536) (DG);
 * 07-Mar-2003 : Modified to pass pieIndex on to PieSectionEntity and tooltip
 *               and URL generators (DG);
 * 21-Mar-2003 : Added a minimum angle for drawing arcs
 *               (see bug id 620031) (DG);
 * 24-Apr-2003 : Switched around PieDataset and KeyedValuesDataset (DG);
 * 02-Jun-2003 : Fixed bug 721733 (DG);
 * 30-Jul-2003 : Modified entity constructor (CZ);
 * 19-Aug-2003 : Implemented Cloneable (DG);
 * 29-Aug-2003 : Fixed bug 796936 (null pointer on setOutlinePaint()) (DG);
 * 08-Sep-2003 : Added internationalization via use of properties
 *               resourceBundle (RFE 690236) (AL);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 29-Oct-2003 : Added workaround for font alignment in PDF output (DG);
 * 05-Nov-2003 : Fixed missing legend bug (DG);
 * 10-Nov-2003 : Re-added the DatasetChangeListener to constructors (CZ);
 * 29-Jan-2004 : Fixed clipping bug in draw() method (DG);
 * 11-Mar-2004 : Major overhaul to improve labelling (DG);
 * 31-Mar-2004 : Made an adjustment for the plot area when the label generator
 *               is null.  Fixed null pointer exception when the label
 *               generator returns null for a label (DG);
 * 06-Apr-2004 : Added getter, setter, serialization and draw support for
 *               labelBackgroundPaint (AS);
 * 08-Apr-2004 : Added flag to control whether null values are ignored or
 *               not (DG);
 * 15-Apr-2004 : Fixed some minor warnings from Eclipse (DG);
 * 26-Apr-2004 : Added attributes for label outline and shadow (DG);
 * 04-Oct-2004 : Renamed ShapeUtils --> ShapeUtilities (DG);
 * 04-Nov-2004 : Fixed null pointer exception with new LegendTitle class (DG);
 * 09-Nov-2004 : Added user definable legend item shape (DG);
 * 25-Nov-2004 : Added new legend label generator (DG);
 * 20-Apr-2005 : Added a tool tip generator for legend labels (DG);
 * 26-Apr-2005 : Removed LOGGER (DG);
 * 05-May-2005 : Updated draw() method parameters (DG);
 * 10-May-2005 : Added flag to control visibility of label linking lines, plus
 *               another flag to control the handling of zero values (DG);
 * 08-Jun-2005 : Fixed bug in getLegendItems() method (not respecting flags
 *               for ignoring null and zero values), and fixed equals() method
 *               to handle GradientPaint (DG);
 * 15-Jul-2005 : Added sectionOutlinesVisible attribute (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 09-Jan-2006 : Fixed bug 1400442, inconsistent treatment of null and zero
 *               values in dataset (DG);
 * 28-Feb-2006 : Fixed bug 1440415, bad distribution of pie section
 *               labels (DG);
 * 27-Sep-2006 : Initialised baseSectionPaint correctly, added lookup methods
 *               for section paint, outline paint and outline stroke (DG);
 * 27-Sep-2006 : Refactored paint and stroke methods to use keys rather than
 *               section indices (DG);
 * 03-Oct-2006 : Replaced call to JRE 1.5 method (DG);
 * 23-Nov-2006 : Added support for URLs for the legend items (DG);
 * 24-Nov-2006 : Cloning fixes (DG);
 * 17-Apr-2007 : Check for null label in legend items (DG);
 * 19-Apr-2007 : Deprecated override settings (DG);
 * 18-May-2007 : Set dataset for LegendItem (DG);
 * 14-Jun-2007 : Added label distributor attribute (DG);
 * 18-Jul-2007 : Added simple label option (DG);
 * 21-Nov-2007 : Fixed labelling bugs, added debug code, restored default
 *               white background (DG);
 * 19-Mar-2008 : Fixed IllegalArgumentException when drawing with null
 *               dataset (DG);
 * 31-Mar-2008 : Adjust the label area for the interiorGap (DG);
 * 31-Mar-2008 : Added quad and cubic curve label link lines - see patch
 *               1891849 by Martin Hilpert (DG);
 * 02-Jul-2008 : Added autoPopulate flags (DG);
 * 15-Aug-2008 : Added methods to clear section attributes (DG);
 * 15-Aug-2008 : Fixed bug 2051168 - problem with LegendItemEntity
 *               generation (DG);
 * 23-Sep-2008 : Added getLabelLinkDepth() method - see bug 2121818 reported
 *               by Christoph Beck (DG);
 * 18-Dec-2008 : Use ResourceBundleWrapper - see patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 */

package org.jfree.chart.plot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.PaintMap;
import org.jfree.chart.StrokeMap;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.KeyedValues;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.PieDataset;
import org.jfree.io.SerialUtilities;
import org.jfree.text.G2TextMeasurer;
import org.jfree.text.TextBlock;
import org.jfree.text.TextBox;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;
import org.jfree.util.Rotation;
import org.jfree.util.ShapeUtilities;
import org.jfree.util.UnitType;

/**
 * A plot that displays data in the form of a pie chart, using data from any
 * class that implements the {@link PieDataset} interface.
 * The example shown here is generated by the <code>PieChartDemo2.java</code>
 * program included in the JFreeChart Demo Collection:
 * <br><br>
 * <img src="../../../../images/PiePlotSample.png"
 * alt="PiePlotSample.png" />
 * <P>
 * Special notes:
 * <ol>
 * <li>the default starting point is 12 o'clock and the pie sections proceed
 * in a clockwise direction, but these settings can be changed;</li>
 * <li>negative values in the dataset are ignored;</li>
 * <li>there are utility methods for creating a {@link PieDataset} from a
 * {@link org.jfree.data.category.CategoryDataset};</li>
 * </ol>
 *
 * @see Plot
 * @see PieDataset
 */
public class PiePlot extends Plot implements Cloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -795612466005590431L;

    /** The default interior gap. */
    public static final double DEFAULT_INTERIOR_GAP = 0.08;

    /** The maximum interior gap (currently 40%). */
    public static final double MAX_INTERIOR_GAP = 0.40;

    /** The default starting angle for the pie chart. */
    public static final double DEFAULT_START_ANGLE = 90.0;

    /** The default section label font. */
    public static final Font DEFAULT_LABEL_FONT = new Font("SansSerif",
            Font.PLAIN, 10);

    /** The default section label paint. */
    public static final Paint DEFAULT_LABEL_PAINT = Color.black;

    /** The default section label background paint. */
    public static final Paint DEFAULT_LABEL_BACKGROUND_PAINT = new Color(255,
            255, 192);

    /** The default section label outline paint. */
    public static final Paint DEFAULT_LABEL_OUTLINE_PAINT = Color.black;

    /** The default section label outline stroke. */
    public static final Stroke DEFAULT_LABEL_OUTLINE_STROKE = new BasicStroke(
            0.5f);

    /** The default section label shadow paint. */
    public static final Paint DEFAULT_LABEL_SHADOW_PAINT = new Color(151, 151,
            151, 128);

    /** The default minimum arc angle to draw. */
    public static final double DEFAULT_MINIMUM_ARC_ANGLE_TO_DRAW = 0.00001;

    /** The dataset for the pie chart. */
    private PieDataset dataset;

    /** The pie index (used by the {@link MultiplePiePlot} class). */
    private int pieIndex;

    /**
     * The amount of space left around the outside of the pie plot, expressed
     * as a percentage of the plot area width and height.
     */
    private double interiorGap;

    /** Flag determining whether to draw an ellipse or a perfect circle. */
    private boolean circular;

    /** The starting angle. */
    private double startAngle;

    /** The direction for the pie segments. */
    private Rotation direction;

    /** The section paint map. */
    private PaintMap sectionPaintMap;

    /** The base section paint (fallback). */
    private transient Paint baseSectionPaint;

    /**
     * A flag that controls whether or not the section paint is auto-populated
     * from the drawing supplier.
     *
     * @since 1.0.11
     */
    private boolean autoPopulateSectionPaint;

    /**
     * A flag that controls whether or not an outline is drawn for each
     * section in the plot.
     */
    private boolean sectionOutlinesVisible;

    /** The section outline paint map. */
    private PaintMap sectionOutlinePaintMap;

    /** The base section outline paint (fallback). */
    private transient Paint baseSectionOutlinePaint;

    /**
     * A flag that controls whether or not the section outline paint is
     * auto-populated from the drawing supplier.
     *
     * @since 1.0.11
     */
    private boolean autoPopulateSectionOutlinePaint;

    /** The section outline stroke map. */
    private StrokeMap sectionOutlineStrokeMap;

    /** The base section outline stroke (fallback). */
    private transient Stroke baseSectionOutlineStroke;

    /**
     * A flag that controls whether or not the section outline stroke is
     * auto-populated from the drawing supplier.
     *
     * @since 1.0.11
     */
    private boolean autoPopulateSectionOutlineStroke;

    /** The shadow paint. */
    private transient Paint shadowPaint = Color.gray;

    /** The x-offset for the shadow effect. */
    private double shadowXOffset = 4.0f;

    /** The y-offset for the shadow effect. */
    private double shadowYOffset = 4.0f;

    /** The percentage amount to explode each pie section. */
    private Map explodePercentages;

    /** The section label generator. */
    private PieSectionLabelGenerator labelGenerator;

    /** The font used to display the section labels. */
    private Font labelFont;

    /** The color used to draw the section labels. */
    private transient Paint labelPaint;

    /**
     * The color used to draw the background of the section labels.  If this
     * is <code>null</code>, the background is not filled.
     */
    private transient Paint labelBackgroundPaint;

    /**
     * The paint used to draw the outline of the section labels
     * (<code>null</code> permitted).
     */
    private transient Paint labelOutlinePaint;

    /**
     * The stroke used to draw the outline of the section labels
     * (<code>null</code> permitted).
     */
    private transient Stroke labelOutlineStroke;

    /**
     * The paint used to draw the shadow for the section labels
     * (<code>null</code> permitted).
     */
    private transient Paint labelShadowPaint;

    /**
     * A flag that controls whether simple or extended labels are used.
     *
     * @since 1.0.7
     */
    private boolean simpleLabels = true;

    /**
     * The padding between the labels and the label outlines.  This is not
     * allowed to be <code>null</code>.
     *
     * @since 1.0.7
     */
    private RectangleInsets labelPadding;

    /**
     * The simple label offset.
     *
     * @since 1.0.7
     */
    private RectangleInsets simpleLabelOffset;

    /** The maximum label width as a percentage of the plot width. */
    private double maximumLabelWidth = 0.14;

    /**
     * The gap between the labels and the link corner, as a percentage of the
     * plot width.
     */
    private double labelGap = 0.025;

    /** A flag that controls whether or not the label links are drawn. */
    private boolean labelLinksVisible;

    /**
     * The label link style.
     *
     * @since 1.0.10
     */
    private PieLabelLinkStyle labelLinkStyle = PieLabelLinkStyle.STANDARD;

    /** The link margin. */
    private double labelLinkMargin = 0.025;

    /** The paint used for the label linking lines. */
    private transient Paint labelLinkPaint = Color.black;

    /** The stroke used for the label linking lines. */
    private transient Stroke labelLinkStroke = new BasicStroke(0.5f);

    /**
     * The pie section label distributor.
     *
     * @since 1.0.6
     */
    private AbstractPieLabelDistributor labelDistributor;

    /** The tooltip generator. */
    private PieToolTipGenerator toolTipGenerator;

    /** The URL generator. */
    private PieURLGenerator urlGenerator;

    /** The legend label generator. */
    private PieSectionLabelGenerator legendLabelGenerator;

    /** A tool tip generator for the legend. */
    private PieSectionLabelGenerator legendLabelToolTipGenerator;

    /**
     * A URL generator for the legend items (optional).
     *
     * @since 1.0.4.
     */
    private PieURLGenerator legendLabelURLGenerator;

    /**
     * A flag that controls whether <code>null</code> values are ignored.
     */
    private boolean ignoreNullValues;

    /**
     * A flag that controls whether zero values are ignored.
     */
    private boolean ignoreZeroValues;

    /** The legend item shape. */
    private transient Shape legendItemShape;

    /**
     * The smallest arc angle that will get drawn (this is to avoid a bug in
     * various Java implementations that causes the JVM to crash).  See this
     * link for details:
     *
     * http://www.jfree.org/phpBB2/viewtopic.php?t=2707
     *
     * ...and this bug report in the Java Bug Parade:
     *
     * http://developer.java.sun.com/developer/bugParade/bugs/4836495.html
     */
    private double minimumArcAngleToDraw;

    /** The resourceBundle for the localization. */
    protected static ResourceBundle localizationResources
            = ResourceBundleWrapper.getBundle(
                    "org.jfree.chart.plot.LocalizationBundle");

    /**
     * This debug flag controls whether or not an outline is drawn showing the
     * interior of the plot region.  This is drawn as a lightGray rectangle
     * showing the padding provided by the 'interiorGap' setting.
     */
    static final boolean DEBUG_DRAW_INTERIOR = false;

    /**
     * This debug flag controls whether or not an outline is drawn showing the
     * link area (in blue) and link ellipse (in yellow).  This controls where
     * the label links have 'elbow' points.
     */
    static final boolean DEBUG_DRAW_LINK_AREA = false;

    /**
     * This debug flag controls whether or not an outline is drawn showing
     * the pie area (in green).
     */
    static final boolean DEBUG_DRAW_PIE_AREA = false;

    /**
     * Creates a new plot.  The dataset is initially set to <code>null</code>.
     */
    public PiePlot() {
        this(null);
    }

    /**
     * Creates a plot that will draw a pie chart for the specified dataset.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     */
    public PiePlot(PieDataset dataset) {
        super();
        this.dataset = dataset;
        if (dataset != null) {
            dataset.addChangeListener(this);
        }
        this.pieIndex = 0;

        this.interiorGap = DEFAULT_INTERIOR_GAP;
        this.circular = true;
        this.startAngle = DEFAULT_START_ANGLE;
        this.direction = Rotation.CLOCKWISE;
        this.minimumArcAngleToDraw = DEFAULT_MINIMUM_ARC_ANGLE_TO_DRAW;

        this.sectionPaint = null;
        this.sectionPaintMap = new PaintMap();
        this.baseSectionPaint = Color.gray;
        this.autoPopulateSectionPaint = true;

        this.sectionOutlinesVisible = true;
        this.sectionOutlinePaint = null;
        this.sectionOutlinePaintMap = new PaintMap();
        this.baseSectionOutlinePaint = DEFAULT_OUTLINE_PAINT;
        this.autoPopulateSectionOutlinePaint = false;

        this.sectionOutlineStroke = null;
        this.sectionOutlineStrokeMap = new StrokeMap();
        this.baseSectionOutlineStroke = DEFAULT_OUTLINE_STROKE;
        this.autoPopulateSectionOutlineStroke = false;

        this.explodePercentages = new TreeMap();

        this.labelGenerator = new StandardPieSectionLabelGenerator();
        this.labelFont = DEFAULT_LABEL_FONT;
        this.labelPaint = DEFAULT_LABEL_PAINT;
        this.labelBackgroundPaint = DEFAULT_LABEL_BACKGROUND_PAINT;
        this.labelOutlinePaint = DEFAULT_LABEL_OUTLINE_PAINT;
        this.labelOutlineStroke = DEFAULT_LABEL_OUTLINE_STROKE;
        this.labelShadowPaint = DEFAULT_LABEL_SHADOW_PAINT;
        this.labelLinksVisible = true;
        this.labelDistributor = new PieLabelDistributor(0);

        this.simpleLabels = false;
        this.simpleLabelOffset = new RectangleInsets(UnitType.RELATIVE, 0.18,
                0.18, 0.18, 0.18);
        this.labelPadding = new RectangleInsets(2, 2, 2, 2);

        this.toolTipGenerator = null;
        this.urlGenerator = null;
        this.legendLabelGenerator = new StandardPieSectionLabelGenerator();
        this.legendLabelToolTipGenerator = null;
        this.legendLabelURLGenerator = null;
        this.legendItemShape = Plot.DEFAULT_LEGEND_ITEM_CIRCLE;

        this.ignoreNullValues = false;
        this.ignoreZeroValues = false;
    }

    /**
     * Returns the dataset.
     *
     * @return The dataset (possibly <code>null</code>).
     *
     * @see #setDataset(PieDataset)
     */
    public PieDataset getDataset() {
        return this.dataset;
    }

    /**
     * Sets the dataset and sends a {@link DatasetChangeEvent} to 'this'.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     *
     * @see #getDataset()
     */
    public void setDataset(PieDataset dataset) {
        // if there is an existing dataset, remove the plot from the list of
        // change listeners...
        PieDataset existing = this.dataset;
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
     * Returns the pie index (this is used by the {@link MultiplePiePlot} class
     * to track subplots).
     *
     * @return The pie index.
     *
     * @see #setPieIndex(int)
     */
    public int getPieIndex() {
        return this.pieIndex;
    }

    /**
     * Sets the pie index (this is used by the {@link MultiplePiePlot} class to
     * track subplots).
     *
     * @param index  the index.
     *
     * @see #getPieIndex()
     */
    public void setPieIndex(int index) {
        this.pieIndex = index;
    }

    /**
     * Returns the start angle for the first pie section.  This is measured in
     * degrees starting from 3 o'clock and measuring anti-clockwise.
     *
     * @return The start angle.
     *
     * @see #setStartAngle(double)
     */
    public double getStartAngle() {
        return this.startAngle;
    }

    /**
     * Sets the starting angle and sends a {@link PlotChangeEvent} to all
     * registered listeners.  The initial default value is 90 degrees, which
     * corresponds to 12 o'clock.  A value of zero corresponds to 3 o'clock...
     * this is the encoding used by Java's Arc2D class.
     *
     * @param angle  the angle (in degrees).
     *
     * @see #getStartAngle()
     */
    public void setStartAngle(double angle) {
        this.startAngle = angle;
        fireChangeEvent();
    }

    /**
     * Returns the direction in which the pie sections are drawn (clockwise or
     * anti-clockwise).
     *
     * @return The direction (never <code>null</code>).
     *
     * @see #setDirection(Rotation)
     */
    public Rotation getDirection() {
        return this.direction;
    }

    /**
     * Sets the direction in which the pie sections are drawn and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param direction  the direction (<code>null</code> not permitted).
     *
     * @see #getDirection()
     */
    public void setDirection(Rotation direction) {
        if (direction == null) {
            throw new IllegalArgumentException("Null 'direction' argument.");
        }
        this.direction = direction;
        fireChangeEvent();

    }

    /**
     * Returns the interior gap, measured as a percentage of the available
     * drawing space.
     *
     * @return The gap (as a percentage of the available drawing space).
     *
     * @see #setInteriorGap(double)
     */
    public double getInteriorGap() {
        return this.interiorGap;
    }

    /**
     * Sets the interior gap and sends a {@link PlotChangeEvent} to all
     * registered listeners.  This controls the space between the edges of the
     * pie plot and the plot area itself (the region where the section labels
     * appear).
     *
     * @param percent  the gap (as a percentage of the available drawing space).
     *
     * @see #getInteriorGap()
     */
    public void setInteriorGap(double percent) {

        if ((percent < 0.0) || (percent > MAX_INTERIOR_GAP)) {
            throw new IllegalArgumentException(
                "Invalid 'percent' (" + percent + ") argument.");
        }

        if (this.interiorGap != percent) {
            this.interiorGap = percent;
            fireChangeEvent();
        }

    }

    /**
     * Returns a flag indicating whether the pie chart is circular, or
     * stretched into an elliptical shape.
     *
     * @return A flag indicating whether the pie chart is circular.
     *
     * @see #setCircular(boolean)
     */
    public boolean isCircular() {
        return this.circular;
    }

    /**
     * A flag indicating whether the pie chart is circular, or stretched into
     * an elliptical shape.
     *
     * @param flag  the new value.
     *
     * @see #isCircular()
     */
    public void setCircular(boolean flag) {
        setCircular(flag, true);
    }

    /**
     * Sets the circular attribute and, if requested, sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param circular  the new value of the flag.
     * @param notify  notify listeners?
     *
     * @see #isCircular()
     */
    public void setCircular(boolean circular, boolean notify) {
        this.circular = circular;
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the flag that controls whether <code>null</code> values in the
     * dataset are ignored.
     *
     * @return A boolean.
     *
     * @see #setIgnoreNullValues(boolean)
     */
    public boolean getIgnoreNullValues() {
        return this.ignoreNullValues;
    }

    /**
     * Sets a flag that controls whether <code>null</code> values are ignored,
     * and sends a {@link PlotChangeEvent} to all registered listeners.  At
     * present, this only affects whether or not the key is presented in the
     * legend.
     *
     * @param flag  the flag.
     *
     * @see #getIgnoreNullValues()
     * @see #setIgnoreZeroValues(boolean)
     */
    public void setIgnoreNullValues(boolean flag) {
        this.ignoreNullValues = flag;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether zero values in the
     * dataset are ignored.
     *
     * @return A boolean.
     *
     * @see #setIgnoreZeroValues(boolean)
     */
    public boolean getIgnoreZeroValues() {
        return this.ignoreZeroValues;
    }

    /**
     * Sets a flag that controls whether zero values are ignored,
     * and sends a {@link PlotChangeEvent} to all registered listeners.  This
     * only affects whether or not a label appears for the non-visible
     * pie section.
     *
     * @param flag  the flag.
     *
     * @see #getIgnoreZeroValues()
     * @see #setIgnoreNullValues(boolean)
     */
    public void setIgnoreZeroValues(boolean flag) {
        this.ignoreZeroValues = flag;
        fireChangeEvent();
    }

    //// SECTION PAINT ////////////////////////////////////////////////////////

    /**
     * Returns the paint for the specified section.  This is equivalent to
     * <code>lookupSectionPaint(section, getAutoPopulateSectionPaint())</code>.
     *
     * @param key  the section key.
     *
     * @return The paint for the specified section.
     *
     * @since 1.0.3
     *
     * @see #lookupSectionPaint(Comparable, boolean)
     */
    protected Paint lookupSectionPaint(Comparable key) {
        return lookupSectionPaint(key, getAutoPopulateSectionPaint());
    }

    /**
     * Returns the paint for the specified section.  The lookup involves these
     * steps:
     * <ul>
     * <li>if {@link #getSectionPaint()} is non-<code>null</code>, return
     *         it;</li>
     * <li>if {@link #getSectionPaint(int)} is non-<code>null</code> return
     *         it;</li>
     * <li>if {@link #getSectionPaint(int)} is <code>null</code> but
     *         <code>autoPopulate</code> is <code>true</code>, attempt to fetch
     *         a new paint from the drawing supplier
     *         ({@link #getDrawingSupplier()});
     * <li>if all else fails, return {@link #getBaseSectionPaint()}.
     * </ul>
     *
     * @param key  the section key.
     * @param autoPopulate  a flag that controls whether the drawing supplier
     *     is used to auto-populate the section paint settings.
     *
     * @return The paint.
     *
     * @since 1.0.3
     */
    protected Paint lookupSectionPaint(Comparable key, boolean autoPopulate) {

        // is there an override?
        Paint result = getSectionPaint();
        if (result != null) {
            return result;
        }

        // if not, check if there is a paint defined for the specified key
        result = this.sectionPaintMap.getPaint(key);
        if (result != null) {
            return result;
        }

        // nothing defined - do we autoPopulate?
        if (autoPopulate) {
            DrawingSupplier ds = getDrawingSupplier();
            if (ds != null) {
                result = ds.getNextPaint();
                this.sectionPaintMap.put(key, result);
            }
            else {
                result = this.baseSectionPaint;
            }
        }
        else {
            result = this.baseSectionPaint;
        }
        return result;
    }

    /**
     * Returns the paint for ALL sections in the plot.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setSectionPaint(Paint)
     *
     * @deprecated Use {@link #getSectionPaint(Comparable)} and
     *     {@link #getBaseSectionPaint()}.  Deprecated as of version 1.0.6.
     */
    public Paint getSectionPaint() {
        return this.sectionPaint;
    }

    /**
     * Sets the paint for ALL sections in the plot.  If this is set to
     * </code>null</code>, then a list of paints is used instead (to allow
     * different colors to be used for each section).
     *
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getSectionPaint()
     *
     * @deprecated Use {@link #setSectionPaint(Comparable, Paint)} and
     *     {@link #setBaseSectionPaint(Paint)}.  Deprecated as of version 1.0.6.
     */
    public void setSectionPaint(Paint paint) {
        this.sectionPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns a key for the specified section.  If there is no such section
     * in the dataset, we generate a key.  This is to provide some backward
     * compatibility for the (now deprecated) methods that get/set attributes
     * based on section indices.  The preferred way of doing this now is to
     * link the attributes directly to the section key (there are new methods
     * for this, starting from version 1.0.3).
     *
     * @param section  the section index.
     *
     * @return The key.
     *
     * @since 1.0.3
     */
    protected Comparable getSectionKey(int section) {
        Comparable key = null;
        if (this.dataset != null) {
            if (section >= 0 && section < this.dataset.getItemCount()) {
                key = this.dataset.getKey(section);
            }
        }
        if (key == null) {
            key = new Integer(section);
        }
        return key;
    }

    /**
     * Returns the paint associated with the specified key, or
     * <code>null</code> if there is no paint associated with the key.
     *
     * @param key  the key (<code>null</code> not permitted).
     *
     * @return The paint associated with the specified key, or
     *     <code>null</code>.
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     *
     * @see #setSectionPaint(Comparable, Paint)
     *
     * @since 1.0.3
     */
    public Paint getSectionPaint(Comparable key) {
        // null argument check delegated...
        return this.sectionPaintMap.getPaint(key);
    }

    /**
     * Sets the paint associated with the specified key, and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param key  the key (<code>null</code> not permitted).
     * @param paint  the paint.
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     *
     * @see #getSectionPaint(Comparable)
     *
     * @since 1.0.3
     */
    public void setSectionPaint(Comparable key, Paint paint) {
        // null argument check delegated...
        this.sectionPaintMap.put(key, paint);
        fireChangeEvent();
    }

    /**
     * Clears the section paint settings for this plot and, if requested, sends
     * a {@link PlotChangeEvent} to all registered listeners.  Be aware that
     * if the <code>autoPopulateSectionPaint</code> flag is set, the section
     * paints may be repopulated using the same colours as before.
     *
     * @param notify  notify listeners?
     *
     * @since 1.0.11
     *
     * @see #autoPopulateSectionPaint
     */
    public void clearSectionPaints(boolean notify) {
        this.sectionPaintMap.clear();
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the base section paint.  This is used when no other paint is
     * defined, which is rare.  The default value is <code>Color.gray</code>.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setBaseSectionPaint(Paint)
     */
    public Paint getBaseSectionPaint() {
        return this.baseSectionPaint;
    }

    /**
     * Sets the base section paint and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getBaseSectionPaint()
     */
    public void setBaseSectionPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.baseSectionPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether or not the section paint is
     * auto-populated by the {@link #lookupSectionPaint(Comparable)} method.
     *
     * @return A boolean.
     *
     * @since 1.0.11
     */
    public boolean getAutoPopulateSectionPaint() {
        return this.autoPopulateSectionPaint;
    }

    /**
     * Sets the flag that controls whether or not the section paint is
     * auto-populated by the {@link #lookupSectionPaint(Comparable)} method,
     * and sends a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param auto  auto-populate?
     *
     * @since 1.0.11
     */
    public void setAutoPopulateSectionPaint(boolean auto) {
        this.autoPopulateSectionPaint = auto;
        fireChangeEvent();
    }

    //// SECTION OUTLINE PAINT ////////////////////////////////////////////////

    /**
     * Returns the flag that controls whether or not the outline is drawn for
     * each pie section.
     *
     * @return The flag that controls whether or not the outline is drawn for
     *         each pie section.
     *
     * @see #setSectionOutlinesVisible(boolean)
     */
    public boolean getSectionOutlinesVisible() {
        return this.sectionOutlinesVisible;
    }

    /**
     * Sets the flag that controls whether or not the outline is drawn for
     * each pie section, and sends a {@link PlotChangeEvent} to all registered
     * listeners.
     *
     * @param visible  the flag.
     *
     * @see #getSectionOutlinesVisible()
     */
    public void setSectionOutlinesVisible(boolean visible) {
        this.sectionOutlinesVisible = visible;
        fireChangeEvent();
    }

    /**
     * Returns the outline paint for the specified section.  This is equivalent
     * to <code>lookupSectionPaint(section,
     * getAutoPopulateSectionOutlinePaint())</code>.
     *
     * @param key  the section key.
     *
     * @return The paint for the specified section.
     *
     * @since 1.0.3
     *
     * @see #lookupSectionOutlinePaint(Comparable, boolean)
     */
    protected Paint lookupSectionOutlinePaint(Comparable key) {
        return lookupSectionOutlinePaint(key,
                getAutoPopulateSectionOutlinePaint());
    }

    /**
     * Returns the outline paint for the specified section.  The lookup
     * involves these steps:
     * <ul>
     * <li>if {@link #getSectionOutlinePaint()} is non-<code>null</code>,
     *         return it;</li>
     * <li>otherwise, if {@link #getSectionOutlinePaint(int)} is
     *         non-<code>null</code> return it;</li>
     * <li>if {@link #getSectionOutlinePaint(int)} is <code>null</code> but
     *         <code>autoPopulate</code> is <code>true</code>, attempt to fetch
     *         a new outline paint from the drawing supplier
     *         ({@link #getDrawingSupplier()});
     * <li>if all else fails, return {@link #getBaseSectionOutlinePaint()}.
     * </ul>
     *
     * @param key  the section key.
     * @param autoPopulate  a flag that controls whether the drawing supplier
     *     is used to auto-populate the section outline paint settings.
     *
     * @return The paint.
     *
     * @since 1.0.3
     */
    protected Paint lookupSectionOutlinePaint(Comparable key,
            boolean autoPopulate) {

        // is there an override?
        Paint result = getSectionOutlinePaint();
        if (result != null) {
            return result;
        }

        // if not, check if there is a paint defined for the specified key
        result = this.sectionOutlinePaintMap.getPaint(key);
        if (result != null) {
            return result;
        }

        // nothing defined - do we autoPopulate?
        if (autoPopulate) {
            DrawingSupplier ds = getDrawingSupplier();
            if (ds != null) {
                result = ds.getNextOutlinePaint();
                this.sectionOutlinePaintMap.put(key, result);
            }
            else {
                result = this.baseSectionOutlinePaint;
            }
        }
        else {
            result = this.baseSectionOutlinePaint;
        }
        return result;
    }

    /**
     * Returns the outline paint associated with the specified key, or
     * <code>null</code> if there is no paint associated with the key.
     *
     * @param key  the key (<code>null</code> not permitted).
     *
     * @return The paint associated with the specified key, or
     *     <code>null</code>.
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     *
     * @see #setSectionOutlinePaint(Comparable, Paint)
     *
     * @since 1.0.3
     */
    public Paint getSectionOutlinePaint(Comparable key) {
        // null argument check delegated...
        return this.sectionOutlinePaintMap.getPaint(key);
    }

    /**
     * Sets the outline paint associated with the specified key, and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param key  the key (<code>null</code> not permitted).
     * @param paint  the paint.
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     *
     * @see #getSectionOutlinePaint(Comparable)
     *
     * @since 1.0.3
     */
    public void setSectionOutlinePaint(Comparable key, Paint paint) {
        // null argument check delegated...
        this.sectionOutlinePaintMap.put(key, paint);
        fireChangeEvent();
    }

    /**
     * Clears the section outline paint settings for this plot and, if
     * requested, sends a {@link PlotChangeEvent} to all registered listeners.
     * Be aware that if the <code>autoPopulateSectionPaint</code> flag is set,
     * the section paints may be repopulated using the same colours as before.
     *
     * @param notify  notify listeners?
     *
     * @since 1.0.11
     *
     * @see #autoPopulateSectionOutlinePaint
     */
    public void clearSectionOutlinePaints(boolean notify) {
        this.sectionOutlinePaintMap.clear();
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the base section paint.  This is used when no other paint is
     * available.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setBaseSectionOutlinePaint(Paint)
     */
    public Paint getBaseSectionOutlinePaint() {
        return this.baseSectionOutlinePaint;
    }

    /**
     * Sets the base section paint.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getBaseSectionOutlinePaint()
     */
    public void setBaseSectionOutlinePaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.baseSectionOutlinePaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether or not the section outline paint
     * is auto-populated by the {@link #lookupSectionOutlinePaint(Comparable)}
     * method.
     *
     * @return A boolean.
     *
     * @since 1.0.11
     */
    public boolean getAutoPopulateSectionOutlinePaint() {
        return this.autoPopulateSectionOutlinePaint;
    }

    /**
     * Sets the flag that controls whether or not the section outline paint is
     * auto-populated by the {@link #lookupSectionOutlinePaint(Comparable)}
     * method, and sends a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param auto  auto-populate?
     *
     * @since 1.0.11
     */
    public void setAutoPopulateSectionOutlinePaint(boolean auto) {
        this.autoPopulateSectionOutlinePaint = auto;
        fireChangeEvent();
    }

    //// SECTION OUTLINE STROKE ///////////////////////////////////////////////

    /**
     * Returns the outline stroke for the specified section.  This is
     * equivalent to <code>lookupSectionOutlineStroke(section,
     * getAutoPopulateSectionOutlineStroke())</code>.
     *
     * @param key  the section key.
     *
     * @return The stroke for the specified section.
     *
     * @since 1.0.3
     *
     * @see #lookupSectionOutlineStroke(Comparable, boolean)
     */
    protected Stroke lookupSectionOutlineStroke(Comparable key) {
        return lookupSectionOutlineStroke(key,
                getAutoPopulateSectionOutlineStroke());
    }

    /**
     * Returns the outline stroke for the specified section.  The lookup
     * involves these steps:
     * <ul>
     * <li>if {@link #getSectionOutlineStroke()} is non-<code>null</code>,
     *         return it;</li>
     * <li>otherwise, if {@link #getSectionOutlineStroke(int)} is
     *         non-<code>null</code> return it;</li>
     * <li>if {@link #getSectionOutlineStroke(int)} is <code>null</code> but
     *         <code>autoPopulate</code> is <code>true</code>, attempt to fetch
     *         a new outline stroke from the drawing supplier
     *         ({@link #getDrawingSupplier()});
     * <li>if all else fails, return {@link #getBaseSectionOutlineStroke()}.
     * </ul>
     *
     * @param key  the section key.
     * @param autoPopulate  a flag that controls whether the drawing supplier
     *     is used to auto-populate the section outline stroke settings.
     *
     * @return The stroke.
     *
     * @since 1.0.3
     */
    protected Stroke lookupSectionOutlineStroke(Comparable key,
            boolean autoPopulate) {

        // is there an override?
        Stroke result = getSectionOutlineStroke();
        if (result != null) {
            return result;
        }

        // if not, check if there is a stroke defined for the specified key
        result = this.sectionOutlineStrokeMap.getStroke(key);
        if (result != null) {
            return result;
        }

        // nothing defined - do we autoPopulate?
        if (autoPopulate) {
            DrawingSupplier ds = getDrawingSupplier();
            if (ds != null) {
                result = ds.getNextOutlineStroke();
                this.sectionOutlineStrokeMap.put(key, result);
            }
            else {
                result = this.baseSectionOutlineStroke;
            }
        }
        else {
            result = this.baseSectionOutlineStroke;
        }
        return result;
    }

    /**
     * Returns the outline stroke associated with the specified key, or
     * <code>null</code> if there is no stroke associated with the key.
     *
     * @param key  the key (<code>null</code> not permitted).
     *
     * @return The stroke associated with the specified key, or
     *     <code>null</code>.
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     *
     * @see #setSectionOutlineStroke(Comparable, Stroke)
     *
     * @since 1.0.3
     */
    public Stroke getSectionOutlineStroke(Comparable key) {
        // null argument check delegated...
        return this.sectionOutlineStrokeMap.getStroke(key);
    }

    /**
     * Sets the outline stroke associated with the specified key, and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param key  the key (<code>null</code> not permitted).
     * @param stroke  the stroke.
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     *
     * @see #getSectionOutlineStroke(Comparable)
     *
     * @since 1.0.3
     */
    public void setSectionOutlineStroke(Comparable key, Stroke stroke) {
        // null argument check delegated...
        this.sectionOutlineStrokeMap.put(key, stroke);
        fireChangeEvent();
    }

    /**
     * Clears the section outline stroke settings for this plot and, if
     * requested, sends a {@link PlotChangeEvent} to all registered listeners.
     * Be aware that if the <code>autoPopulateSectionPaint</code> flag is set,
     * the section paints may be repopulated using the same colours as before.
     *
     * @param notify  notify listeners?
     *
     * @since 1.0.11
     *
     * @see #autoPopulateSectionOutlineStroke
     */
    public void clearSectionOutlineStrokes(boolean notify) {
        this.sectionOutlineStrokeMap.clear();
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the base section stroke.  This is used when no other stroke is
     * available.
     *
     * @return The stroke (never <code>null</code>).
     *
     * @see #setBaseSectionOutlineStroke(Stroke)
     */
    public Stroke getBaseSectionOutlineStroke() {
        return this.baseSectionOutlineStroke;
    }

    /**
     * Sets the base section stroke.
     *
     * @param stroke  the stroke (<code>null</code> not permitted).
     *
     * @see #getBaseSectionOutlineStroke()
     */
    public void setBaseSectionOutlineStroke(Stroke stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        this.baseSectionOutlineStroke = stroke;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether or not the section outline stroke
     * is auto-populated by the {@link #lookupSectionOutlinePaint(Comparable)}
     * method.
     *
     * @return A boolean.
     *
     * @since 1.0.11
     */
    public boolean getAutoPopulateSectionOutlineStroke() {
        return this.autoPopulateSectionOutlineStroke;
    }

    /**
     * Sets the flag that controls whether or not the section outline stroke is
     * auto-populated by the {@link #lookupSectionOutlineStroke(Comparable)}
     * method, and sends a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param auto  auto-populate?
     *
     * @since 1.0.11
     */
    public void setAutoPopulateSectionOutlineStroke(boolean auto) {
        this.autoPopulateSectionOutlineStroke = auto;
        fireChangeEvent();
    }

    /**
     * Returns the shadow paint.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setShadowPaint(Paint)
     */
    public Paint getShadowPaint() {
        return this.shadowPaint;
    }

    /**
     * Sets the shadow paint and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getShadowPaint()
     */
    public void setShadowPaint(Paint paint) {
        this.shadowPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the x-offset for the shadow effect.
     *
     * @return The offset (in Java2D units).
     *
     * @see #setShadowXOffset(double)
     */
    public double getShadowXOffset() {
        return this.shadowXOffset;
    }

    /**
     * Sets the x-offset for the shadow effect and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param offset  the offset (in Java2D units).
     *
     * @see #getShadowXOffset()
     */
    public void setShadowXOffset(double offset) {
        this.shadowXOffset = offset;
        fireChangeEvent();
    }

    /**
     * Returns the y-offset for the shadow effect.
     *
     * @return The offset (in Java2D units).
     *
     * @see #setShadowYOffset(double)
     */
    public double getShadowYOffset() {
        return this.shadowYOffset;
    }

    /**
     * Sets the y-offset for the shadow effect and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param offset  the offset (in Java2D units).
     *
     * @see #getShadowYOffset()
     */
    public void setShadowYOffset(double offset) {
        this.shadowYOffset = offset;
        fireChangeEvent();
    }

    /**
     * Returns the amount that the section with the specified key should be
     * exploded.
     *
     * @param key  the key (<code>null</code> not permitted).
     *
     * @return The amount that the section with the specified key should be
     *     exploded.
     *
     * @throws IllegalArgumentException if <code>key</code> is
     *     <code>null</code>.
     *
     * @since 1.0.3
     *
     * @see #setExplodePercent(Comparable, double)
     */
    public double getExplodePercent(Comparable key) {
        double result = 0.0;
        if (this.explodePercentages != null) {
            Number percent = (Number) this.explodePercentages.get(key);
            if (percent != null) {
                result = percent.doubleValue();
            }
        }
        return result;
    }

    /**
     * Sets the amount that a pie section should be exploded and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param key  the section key (<code>null</code> not permitted).
     * @param percent  the explode percentage (0.30 = 30 percent).
     *
     * @since 1.0.3
     *
     * @see #getExplodePercent(Comparable)
     */
    public void setExplodePercent(Comparable key, double percent) {
        if (key == null) {
            throw new IllegalArgumentException("Null 'key' argument.");
        }
        if (this.explodePercentages == null) {
            this.explodePercentages = new TreeMap();
        }
        this.explodePercentages.put(key, new Double(percent));
        fireChangeEvent();
    }

    /**
     * Returns the maximum explode percent.
     *
     * @return The percent.
     */
    public double getMaximumExplodePercent() {
        if (this.dataset == null) {
            return 0.0;
        }
        double result = 0.0;
        Iterator iterator = this.dataset.getKeys().iterator();
        while (iterator.hasNext()) {
            Comparable key = (Comparable) iterator.next();
            Number explode = (Number) this.explodePercentages.get(key);
            if (explode != null) {
                result = Math.max(result, explode.doubleValue());
            }
        }
        return result;
    }

    /**
     * Returns the section label generator.
     *
     * @return The generator (possibly <code>null</code>).
     *
     * @see #setLabelGenerator(PieSectionLabelGenerator)
     */
    public PieSectionLabelGenerator getLabelGenerator() {
        return this.labelGenerator;
    }

    /**
     * Sets the section label generator and sends a {@link PlotChangeEvent} to
     * all registered listeners.
     *
     * @param generator  the generator (<code>null</code> permitted).
     *
     * @see #getLabelGenerator()
     */
    public void setLabelGenerator(PieSectionLabelGenerator generator) {
        this.labelGenerator = generator;
        fireChangeEvent();
    }

    /**
     * Returns the gap between the edge of the pie and the labels, expressed as
     * a percentage of the plot width.
     *
     * @return The gap (a percentage, where 0.05 = five percent).
     *
     * @see #setLabelGap(double)
     */
    public double getLabelGap() {
        return this.labelGap;
    }

    /**
     * Sets the gap between the edge of the pie and the labels (expressed as a
     * percentage of the plot width) and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param gap  the gap (a percentage, where 0.05 = five percent).
     *
     * @see #getLabelGap()
     */
    public void setLabelGap(double gap) {
        this.labelGap = gap;
        fireChangeEvent();
    }

    /**
     * Returns the maximum label width as a percentage of the plot width.
     *
     * @return The width (a percentage, where 0.20 = 20 percent).
     *
     * @see #setMaximumLabelWidth(double)
     */
    public double getMaximumLabelWidth() {
        return this.maximumLabelWidth;
    }

    /**
     * Sets the maximum label width as a percentage of the plot width and sends
     * a {@link PlotChangeEvent} to all registered listeners.
     *
     * @param width  the width (a percentage, where 0.20 = 20 percent).
     *
     * @see #getMaximumLabelWidth()
     */
    public void setMaximumLabelWidth(double width) {
        this.maximumLabelWidth = width;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether or not label linking lines are
     * visible.
     *
     * @return A boolean.
     *
     * @see #setLabelLinksVisible(boolean)
     */
    public boolean getLabelLinksVisible() {
        return this.labelLinksVisible;
    }

    /**
     * Sets the flag that controls whether or not label linking lines are
     * visible and sends a {@link PlotChangeEvent} to all registered listeners.
     * Please take care when hiding the linking lines - depending on the data
     * values, the labels can be displayed some distance away from the
     * corresponding pie section.
     *
     * @param visible  the flag.
     *
     * @see #getLabelLinksVisible()
     */
    public void setLabelLinksVisible(boolean visible) {
        this.labelLinksVisible = visible;
        fireChangeEvent();
    }

    /**
     * Returns the label link style.
     *
     * @return The label link style (never <code>null</code>).
     *
     * @see #setLabelLinkStyle(PieLabelLinkStyle)
     *
     * @since 1.0.10
     */
    public PieLabelLinkStyle getLabelLinkStyle() {
        return this.labelLinkStyle;
    }

    /**
     * Sets the label link style and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param style  the new style (<code>null</code> not permitted).
     *
     * @see #getLabelLinkStyle()
     *
     * @since 1.0.10
     */
    public void setLabelLinkStyle(PieLabelLinkStyle style) {
        if (style == null) {
            throw new IllegalArgumentException("Null 'style' argument.");
        }
        this.labelLinkStyle = style;
        fireChangeEvent();
    }

    /**
     * Returns the margin (expressed as a percentage of the width or height)
     * between the edge of the pie and the link point.
     *
     * @return The link margin (as a percentage, where 0.05 is five percent).
     *
     * @see #setLabelLinkMargin(double)
     */
    public double getLabelLinkMargin() {
        return this.labelLinkMargin;
    }

    /**
     * Sets the link margin and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param margin  the margin.
     *
     * @see #getLabelLinkMargin()
     */
    public void setLabelLinkMargin(double margin) {
        this.labelLinkMargin = margin;
        fireChangeEvent();
    }

    /**
     * Returns the paint used for the lines that connect pie sections to their
     * corresponding labels.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setLabelLinkPaint(Paint)
     */
    public Paint getLabelLinkPaint() {
        return this.labelLinkPaint;
    }

    /**
     * Sets the paint used for the lines that connect pie sections to their
     * corresponding labels, and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getLabelLinkPaint()
     */
    public void setLabelLinkPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.labelLinkPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the stroke used for the label linking lines.
     *
     * @return The stroke.
     *
     * @see #setLabelLinkStroke(Stroke)
     */
    public Stroke getLabelLinkStroke() {
        return this.labelLinkStroke;
    }

    /**
     * Sets the link stroke and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param stroke  the stroke.
     *
     * @see #getLabelLinkStroke()
     */
    public void setLabelLinkStroke(Stroke stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("Null 'stroke' argument.");
        }
        this.labelLinkStroke = stroke;
        fireChangeEvent();
    }

    /**
     * Returns the distance that the end of the label link is embedded into
     * the plot, expressed as a percentage of the plot's radius.
     * <br><br>
     * This method is overridden in the {@link RingPlot} class to resolve
     * bug 2121818.
     *
     * @return <code>0.10</code>.
     *
     * @since 1.0.12
     */
    protected double getLabelLinkDepth() {
        return 0.1;
    }

    /**
     * Returns the section label font.
     *
     * @return The font (never <code>null</code>).
     *
     * @see #setLabelFont(Font)
     */
    public Font getLabelFont() {
        return this.labelFont;
    }

    /**
     * Sets the section label font and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param font  the font (<code>null</code> not permitted).
     *
     * @see #getLabelFont()
     */
    public void setLabelFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("Null 'font' argument.");
        }
        this.labelFont = font;
        fireChangeEvent();
    }

    /**
     * Returns the section label paint.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setLabelPaint(Paint)
     */
    public Paint getLabelPaint() {
        return this.labelPaint;
    }

    /**
     * Sets the section label paint and sends a {@link PlotChangeEvent} to all
     * registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getLabelPaint()
     */
    public void setLabelPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.labelPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the section label background paint.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setLabelBackgroundPaint(Paint)
     */
    public Paint getLabelBackgroundPaint() {
        return this.labelBackgroundPaint;
    }

    /**
     * Sets the section label background paint and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getLabelBackgroundPaint()
     */
    public void setLabelBackgroundPaint(Paint paint) {
        this.labelBackgroundPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the section label outline paint.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setLabelOutlinePaint(Paint)
     */
    public Paint getLabelOutlinePaint() {
        return this.labelOutlinePaint;
    }

    /**
     * Sets the section label outline paint and sends a
     * {@link PlotChangeEvent} to all registered listeners.
     *
     * @param paint  
