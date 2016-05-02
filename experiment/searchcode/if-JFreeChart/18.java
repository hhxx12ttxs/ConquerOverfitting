/*
 * BEGIN_HEADER - DO NOT EDIT
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-esb.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-esb.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)BasicCategoryDatasetBasedCharts.java
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 *
 * END_HEADER - DO NOT EDIT
 */
package org.openesb.tools.extchart.jfchart;
import org.openesb.tools.extchart.exception.ChartException;
import org.openesb.tools.extchart.jfchart.data.DataAccess;
import org.openesb.tools.extchart.property.ChartConstants;
import org.openesb.tools.extchart.property.ChartDefaults;
import org.openesb.tools.extchart.property.JFChartConstants;
import org.openesb.tools.extchart.property.bar.BarProperties;
import org.openesb.tools.extchart.property.bar.XYProperties;
import org.openesb.tools.extpropertysheet.IExtPropertyGroup;
import org.openesb.tools.extpropertysheet.IExtPropertyGroupsBean;
import com.sun.web.ui.component.Property;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;

/**
 *
 * @author rdwivedi
 */
public class BasicCategoryDatasetBasedCharts extends Chart {
    private IExtPropertyGroupsBean propGrps = null;
    private DataAccess mDA = null;
    private static Logger mLogger = Logger.getLogger(BasicCategoryDatasetBasedCharts.class.getName());
    
    private static String AREA_CHART = "areaChart";
    private static String BAR_CHART = "barChart";
    private static String LINE_CHART = "lineChart";
    private static String MULTIPLE_PIE_CHART = "multiplePieChart";
    private static String WATERFALL_CHART = "waterfallChart";
    
    
    public static Map getAllAllowedCharts() {
        HashMap map = new HashMap();
        map.put(AREA_CHART,"Area Chart");
        map.put(BAR_CHART,"Bar Chart");
        map.put(LINE_CHART,"Line Chart");
        map.put(MULTIPLE_PIE_CHART,"Multiple Pie Chart");
        map.put(WATERFALL_CHART,"Waterfall Chart");
        
        return map;
    }
    
    /** Creates a new instance of BasicCategoryDatasetBasedCharts */
    public BasicCategoryDatasetBasedCharts(IExtPropertyGroupsBean pg, DataAccess da) {
        propGrps = pg;
        mDA = da;
    }
    
    public ChartDefaults getChartDefaults() {
        IExtPropertyGroup p = propGrps.getGroupByName(JFChartConstants.CHART_COMMON_PROPERTIES);
        return (ChartDefaults)p;
    }
    
    private IExtPropertyGroupsBean getChartPropertiesGroup() {
        return propGrps;
    } 
    private IExtPropertyGroup getSpecificProperties() {
        return getChartPropertiesGroup().getGroupByName(JFChartConstants.CHART_SPECIFIC_PROPERTIES);
    }
     private IExtPropertyGroup getBARSpecificProperties() {
        return getChartPropertiesGroup().getGroupByName(JFChartConstants.CHART_BAR_PROPERTIES);
    }
    
    public JFreeChart createChart() throws ChartException {
        CategoryDataset dataset = null;
        JFreeChart chart = null;
        dataset  = (CategoryDataset)mDA.getDataSet();
        chart = generateChart(dataset);
       
        return chart;
    }
   
    
    
    private JFreeChart generateChart(CategoryDataset ds) {
        JFreeChart chart = null;
        ChartDefaults p = getChartDefaults();
        String type = (String)p.getProperty(JFChartConstants.CHART_TYPE).getValue();
        if(type.equals(BAR_CHART)) {
            chart = createBarChart(ds);
        } else if(type.equals(LINE_CHART)) {
            chart = createLineChart(ds);
        } else if(type.equals(AREA_CHART)) {
            chart = createAreaChart(ds);
        } else if(type.equals(WATERFALL_CHART)) {
            chart = createWaterfallChart(ds);
        }
        
        chart.setBackgroundPaint(this.getChartDefaults().getBackgroundColor());
        super.setTitle(chart);
        super.setLegend(chart);
        super.setBorder(chart);
        String realNoFormat = this.getChartDefaults().getRealNumberFormat();
        mLogger.info("The real number format is " + realNoFormat);
        DecimalFormat formatter = new DecimalFormat(realNoFormat);
        String formtat = "[{0}, {1}] = {2}";
        StandardCategoryToolTipGenerator  itl = new StandardCategoryToolTipGenerator(formtat,formatter);
        //StandardCategoryItemLabelGenerator itl = new StandardCategoryItemLabelGenerator(realNoFormat, formatter);
        
        chart.getCategoryPlot().getRenderer().setToolTipGenerator(itl);
        StandardCategoryURLGenerator urlG = new StandardCategoryURLGenerator();
        chart.getCategoryPlot().getRenderer().setBaseItemURLGenerator(urlG);
        chart.getPlot().setNoDataMessage("No Data Found.");
        
        
        return chart;
    }
    
    
    private JFreeChart createWaterfallChart(CategoryDataset  ds) {
        XYProperties props = (XYProperties)this.getSpecificProperties();
        ChartDefaults def = this.getChartDefaults();
        boolean       includeLegend = def.isIncludeLegend() ;
        boolean       is3D          = def.is3D();
        String        title         = def.getTitle();
        boolean       bUseTooltips  = true;
        boolean       bDrilldownEnabled = false;
        JFreeChart   chart = null;
        PlotOrientation plotOrientation = null;
        if (props.getOrientation() == XYProperties.HORIZONTAL) {
            plotOrientation = PlotOrientation.HORIZONTAL;
        } else {
            plotOrientation = PlotOrientation.VERTICAL;
        }
        //3d not ...
        chart = ChartFactory.createWaterfallChart(title,
                props.getDomainLabel(), // domain axis label
                props.getRangeLabel(),
                ds,
                plotOrientation,
                includeLegend,
                bUseTooltips,
                bDrilldownEnabled
                );
        
        
        return chart;
        
    }
    
    
    
    
    private JFreeChart createLineChart(CategoryDataset  ds) {
        XYProperties def = (XYProperties)this.getSpecificProperties();
        ChartDefaults props = this.getChartDefaults();
        boolean       includeLegend = props.isIncludeLegend() ;
        boolean       is3D          = props.is3D();
        String        title         = props.getTitle();
        boolean       bUseTooltips  = true;
        boolean       bDrilldownEnabled = false;
        JFreeChart   chart = null;
        PlotOrientation plotOrientation = null;
        if (def.getOrientation() == XYProperties.HORIZONTAL) {
            plotOrientation = PlotOrientation.HORIZONTAL;
        } else {
            plotOrientation = PlotOrientation.VERTICAL;
        }
        if (is3D) {
            chart = ChartFactory.createLineChart3D(props.getTitle(),
                    def.getDomainLabel(), // domain axis label
                    def.getRangeLabel(),
                    ds,
                    plotOrientation,
                    includeLegend,
                    bUseTooltips,
                    bDrilldownEnabled
                    );
        } else {
            chart = ChartFactory.createLineChart(title,
                    def.getDomainLabel(), // domain axis label
                    def.getRangeLabel(),
                    ds,
                    plotOrientation,
                    includeLegend,
                    bUseTooltips,
                    bDrilldownEnabled
                    );
        }
        
        return chart;
        
    }
    
    
    
    
    
    private JFreeChart createAreaChart(CategoryDataset  ds) {
        XYProperties def = (XYProperties)this.getSpecificProperties();
        ChartDefaults props = this.getChartDefaults();
        boolean       includeLegend = props.isIncludeLegend() ;
        boolean       is3D          = props.is3D();
        String        title         = props.getTitle();
        boolean       bUseTooltips  = true;
        boolean       bDrilldownEnabled = false;
        JFreeChart   chart = null;
        PlotOrientation plotOrientation = null;
        if (def.getOrientation() == XYProperties.HORIZONTAL) {
            plotOrientation = PlotOrientation.HORIZONTAL;
        } else {
            plotOrientation = PlotOrientation.VERTICAL;
        }
        //3d not supported ....
        chart = ChartFactory.createAreaChart(props.getTitle(),
                def.getDomainLabel(), // domain axis label
                def.getRangeLabel(),
                ds,
                plotOrientation,
                includeLegend,
                bUseTooltips,
                bDrilldownEnabled
                );
        
        
        return chart;
        
    }
    
    // Bar Chart
    
    private JFreeChart createBarChart(CategoryDataset  mDataSet) {
        
        XYProperties xy = (XYProperties)this.getSpecificProperties();
        BarProperties def = (BarProperties)this.getBARSpecificProperties();
        ChartDefaults props = this.getChartDefaults();
        PlotOrientation plotOrientation = null;
        
        
        if (xy.getOrientation() == XYProperties.HORIZONTAL) {
            plotOrientation = PlotOrientation.HORIZONTAL;
        } else {
            plotOrientation = PlotOrientation.VERTICAL;
        }
        
        
        //boolean bCategorySeriesMutuallyExclusive = isMutuallyExclusiveCategorySeries(mDataSet);
        boolean bUseTooltips      = true;
        boolean bDrilldownEnabled = true;
        //boolean bDrilldownEnabled = barProp.isDrilldownEnabled();
        
        if (props.is3D()) {
            if (def.isStacked() /*||*/ /*mSummaryDataset ||*//* bCategorySeriesMutuallyExclusive*/) {
                return ChartFactory.createStackedBarChart3D(
                        props.getTitle(), // chart title
                        xy.getDomainLabel(), // domain axis label
                        xy.getRangeLabel(), // range axis label
                        mDataSet, // data
                        plotOrientation,
                        props.isIncludeLegend(), // include legend
                        bUseTooltips,
                        bDrilldownEnabled);
            } else {
                return ChartFactory.createBarChart3D(
                        props.getTitle(), // chart title
                        xy.getDomainLabel(), // domain axis label
                        xy.getRangeLabel(), // range axis label
                        mDataSet, // data
                        plotOrientation,
                        props.isIncludeLegend(), // include legend
                        bUseTooltips,
                        bDrilldownEnabled);
            }
        } else {
            if (def.isStacked() /*|| /*mSummaryDataset ||bCategorySeriesMutuallyExclusive*/) {
                return ChartFactory.createStackedBarChart(
                        props.getTitle(), // chart title
                        xy.getDomainLabel(), // domain axis label
                        xy.getRangeLabel(), // range axis label
                        mDataSet, // data
                        plotOrientation,
                        props.isIncludeLegend(), // include legend
                        bUseTooltips,
                        bDrilldownEnabled);
            } else {
                return ChartFactory.createBarChart(
                        props.getTitle(), // chart title
                        xy.getDomainLabel(), // domain axis label
                        xy.getRangeLabel(), // range axis label
                        mDataSet, // data
                        plotOrientation,
                        props.isIncludeLegend(), // include legend
                        bUseTooltips,
                        bDrilldownEnabled);
            }
        }
    }
    
    
    
}

