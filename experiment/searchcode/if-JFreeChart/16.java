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
 * @(#)BasicXYDatasetBasedCharts.java
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
import org.openesb.tools.extchart.property.bar.XYProperties;
import org.openesb.tools.extpropertysheet.IExtProperty;
import org.openesb.tools.extpropertysheet.IExtPropertyGroup;
import org.openesb.tools.extpropertysheet.IExtPropertyGroupsBean;
import org.openesb.tools.extpropertysheet.impl.ExtProperty;
import org.openesb.tools.extpropertysheet.impl.ExtPropertyGroupsBean;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;


/**
 *
 * @author rdwivedi
 */
public class BasicXYDatasetBasedCharts extends Chart {
    
    private IExtPropertyGroupsBean propGrps = null;
    private DataAccess mDA = null;
    private static Logger mLogger = Logger.getLogger(BasicXYDatasetBasedCharts.class.getName());
    
    private static String SCATTER_CHART = "scatterChart";
    private static String POLAR_CHART = "polarChart";
    private static String TIME_SERIES_CHART = "timeSeriesChart";
    private static String AREA_CHART = "areaChart";
    private static String LINE_CHART = "lineChart";
    private static String STEPPED_AREA_CHART = "steppedAreaChart";
    private static String STEPPED_CHART = "steppedChart";
    
     
    
    public static Map getAllAllowedCharts() {
       HashMap map = new HashMap();
       map.put(SCATTER_CHART,"Scatter Chart");
       map.put(POLAR_CHART,"Polar Chart");
       map.put(TIME_SERIES_CHART,"Time Series Chart");
       map.put(AREA_CHART,"Area Chart");
       map.put(LINE_CHART,"Line Chart");
       map.put(STEPPED_CHART,"Stepped Chart");
       map.put(STEPPED_AREA_CHART,"Stepped Area Chart");
    
       return map;
   }
    
    /** Creates a new instance of BasicCategoryDatasetBasedCharts */
    public BasicXYDatasetBasedCharts(IExtPropertyGroupsBean pg, DataAccess da) {
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
    
    
    public JFreeChart createChart() throws ChartException {
        XYDataset dataset = null;
        JFreeChart chart = null;
        dataset  = (XYDataset)mDA.getDataSet();
        chart = generateChart(dataset);
       
        return chart;
    }
    
    
     private JFreeChart generateChart(XYDataset ds) {
        JFreeChart chart = null;
        IExtPropertyGroup p = getChartDefaults();
        String type = (String)p.getProperty(JFChartConstants.CHART_TYPE).getValue();
        if(type.equals(SCATTER_CHART)) {
            chart = createScatterChart(ds);
        } else if(type.equals(POLAR_CHART)) {
            chart = createPolarChart(ds);
        } else if(type.equals(TIME_SERIES_CHART)) {
            chart = createTimeSeriesChart(ds);
        } else if(type.equals(AREA_CHART)) {
            chart = createAreaChart(ds);
        } else if(type.equals(LINE_CHART)) {
            chart = createLineChart(ds);
        } else if(type.equals(STEPPED_CHART)) {
            chart = createSteppedChart(ds);
        } else if(type.equals(STEPPED_AREA_CHART)) {
            chart = createSteppedAreaChart(ds);
        } 
       mLogger.info("Chart type is " + type); 
       chart.setBackgroundPaint(this.getChartDefaults().getBackgroundColor());
       super.setTitle(chart);
       super.setLegend(chart);
       super.setBorder(chart); 
        
       return chart;
    }
     
     
     private JFreeChart   createSteppedAreaChart(XYDataset ds) {
        
       XYProperties props = (XYProperties)this.getSpecificProperties();
       ChartDefaults def = this.getChartDefaults();
       boolean       includeLegend = def.isIncludeLegend() ;
       boolean       is3D          = def.is3D();
       String        title         = def.getTitle();
       boolean       bUseTooltips  = true;
       boolean       bDrilldownEnabled = false;
       String xLabel = props.getDomainLabel(); // domain axis label
       String yLabel = props.getRangeLabel();
       JFreeChart   chart = null;
       PlotOrientation plotOrientation = null;
        if (props.getOrientation() == XYProperties.HORIZONTAL) {
            plotOrientation = PlotOrientation.HORIZONTAL;
        } else {
            plotOrientation = PlotOrientation.VERTICAL;
        }
       
       
       chart = ChartFactory.createXYStepAreaChart(title,xLabel,yLabel,ds,
               plotOrientation,includeLegend,bUseTooltips,bDrilldownEnabled);
       return chart;
   }
     
     
    private JFreeChart   createSteppedChart(XYDataset ds) {
        
       XYProperties props = (XYProperties)this.getSpecificProperties();
       ChartDefaults def = this.getChartDefaults();
       boolean       includeLegend = def.isIncludeLegend() ;
       boolean       is3D          = def.is3D();
       String        title         = def.getTitle();
       boolean       bUseTooltips  = true;
       boolean       bDrilldownEnabled = false;
       String xLabel = props.getDomainLabel(); // domain axis label
       String yLabel = props.getRangeLabel();
       JFreeChart   chart = null;
       PlotOrientation plotOrientation = null;
        if (props.getOrientation() == XYProperties.HORIZONTAL) {
            plotOrientation = PlotOrientation.HORIZONTAL;
        } else {
            plotOrientation = PlotOrientation.VERTICAL;
        }
       
       
       chart = ChartFactory.createXYStepChart(title,xLabel,yLabel,ds,
               plotOrientation,includeLegend,bUseTooltips,bDrilldownEnabled);
       return chart;
   }
     
     
     
     private JFreeChart   createLineChart(XYDataset ds) {
        
       XYProperties props = (XYProperties)this.getSpecificProperties();
       ChartDefaults def = this.getChartDefaults();
       boolean       includeLegend = def.isIncludeLegend() ;
       boolean       is3D          = def.is3D();
       String        title         = def.getTitle();
       boolean       bUseTooltips  = true;
       boolean       bDrilldownEnabled = false;
       String xLabel = props.getDomainLabel(); // domain axis label
       String yLabel = props.getRangeLabel();
       JFreeChart   chart = null;
       PlotOrientation plotOrientation = null;
        if (props.getOrientation() == XYProperties.HORIZONTAL) {
            plotOrientation = PlotOrientation.HORIZONTAL;
        } else {
            plotOrientation = PlotOrientation.VERTICAL;
        }
       
       
       chart = ChartFactory.createXYLineChart(title,xLabel,yLabel,ds,
               plotOrientation,includeLegend,bUseTooltips,bDrilldownEnabled);
       return chart;
   }
     
   private JFreeChart   createAreaChart(XYDataset ds) {
        
       XYProperties props = (XYProperties)this.getSpecificProperties();
       ChartDefaults def = this.getChartDefaults();
       boolean       includeLegend = def.isIncludeLegend() ;
       boolean       is3D          = def.is3D();
       String        title         = def.getTitle();
       boolean       bUseTooltips  = true;
       boolean       bDrilldownEnabled = false;
       String xLabel = props.getDomainLabel(); // domain axis label
       String yLabel = props.getRangeLabel();
       JFreeChart   chart = null;
       PlotOrientation plotOrientation = null;
        if (props.getOrientation() == XYProperties.HORIZONTAL) {
            plotOrientation = PlotOrientation.HORIZONTAL;
        } else {
            plotOrientation = PlotOrientation.VERTICAL;
        }
       
       
       chart = ChartFactory.createXYAreaChart(title,xLabel,yLabel,ds,
               plotOrientation,includeLegend,bUseTooltips,bDrilldownEnabled);
       return chart;
   }
     
     
   private JFreeChart   createScatterChart(XYDataset ds) {
        
       XYProperties props = (XYProperties)this.getSpecificProperties();
       ChartDefaults def = this.getChartDefaults();
       boolean       includeLegend = def.isIncludeLegend() ;
       boolean       is3D          = def.is3D();
       String        title         = def.getTitle();
       boolean       bUseTooltips  = true;
       boolean       bDrilldownEnabled = false;
       String xLabel = props.getDomainLabel(); // domain axis label
       String yLabel = props.getRangeLabel();
       JFreeChart   chart = null;
       PlotOrientation plotOrientation = null;
        if (props.getOrientation() == XYProperties.HORIZONTAL) {
            plotOrientation = PlotOrientation.HORIZONTAL;
        } else {
            plotOrientation = PlotOrientation.VERTICAL;
        }
       
       
       chart = ChartFactory.createScatterPlot(title,xLabel,yLabel,ds,
               plotOrientation,includeLegend,bUseTooltips,bDrilldownEnabled);
       return chart;
   }
    
   
  private JFreeChart   createPolarChart(XYDataset ds) {
        
       XYProperties props = (XYProperties)this.getSpecificProperties();
       ChartDefaults def = this.getChartDefaults();
       boolean       includeLegend = def.isIncludeLegend() ;
       boolean       is3D          = def.is3D();
       String        title         = def.getTitle();
       boolean       bUseTooltips  = true;
       boolean       bDrilldownEnabled = false;
       String xLabel = props.getDomainLabel(); // domain axis label
       String yLabel = props.getRangeLabel();
       JFreeChart   chart = null;
       PlotOrientation plotOrientation = null;
        if (props.getOrientation() == XYProperties.HORIZONTAL) {
            plotOrientation = PlotOrientation.HORIZONTAL;
        } else {
            plotOrientation = PlotOrientation.VERTICAL;
        }
       
       
       chart = ChartFactory.createPolarChart(title,ds,
               includeLegend,bUseTooltips,bDrilldownEnabled);
       return chart;
   } 
    
  
  private JFreeChart   createTimeSeriesChart(XYDataset ds) {
        
       XYProperties props = (XYProperties)this.getSpecificProperties();
       ChartDefaults def = this.getChartDefaults();
       boolean       includeLegend = def.isIncludeLegend() ;
       boolean       is3D          = def.is3D();
       String        title         = def.getTitle();
       boolean       bUseTooltips  = true;
       boolean       bDrilldownEnabled = false;
       String xLabel = props.getDomainLabel(); // domain axis label
       String yLabel = props.getRangeLabel();
       JFreeChart   chart = null;
       PlotOrientation plotOrientation = null;
        if (props.getOrientation() == XYProperties.HORIZONTAL) {
            plotOrientation = PlotOrientation.HORIZONTAL;
        } else {
            plotOrientation = PlotOrientation.VERTICAL;
        }
       chart = ChartFactory.createTimeSeriesChart(title,xLabel,yLabel,ds,
               includeLegend,bUseTooltips,bDrilldownEnabled);
       return chart;
   } 
  
}

