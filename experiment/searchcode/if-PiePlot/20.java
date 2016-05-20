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
 * @(#)BasicPieDatasetBasedCharts.java
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 *
 * END_HEADER - DO NOT EDIT
 */
package org.openesb.tools.extchart.jfchart;


import org.openesb.tools.extchart.exception.ChartException;
import org.openesb.tools.extchart.jfchart.data.DataAccess;
import org.openesb.tools.extchart.property.ChartDefaults;
import org.openesb.tools.extchart.property.JFChartConstants;
import org.openesb.tools.extchart.property.pie.PieConstants;
import org.openesb.tools.extchart.property.pie.PieProperties;
import org.openesb.tools.extpropertysheet.IExtPropertyGroup;
import org.openesb.tools.extpropertysheet.IExtPropertyGroupsBean;
import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.util.Rotation;

/**
 *
 * @author rdwivedi
 */
public class BasicPieDatasetBasedCharts extends Chart {
    private IExtPropertyGroupsBean propGrps = null;
    private DataAccess mDA = null;
    private static Logger mLogger = Logger.getLogger(BasicPieDatasetBasedCharts.class.getName());
    
    private static String PIE_CHART = "pieChart";
    private static String RING_CHART = "ringChart";
   
    
    
   public static Map getAllAllowedCharts() {
       HashMap map = new HashMap();
       map.put(PIE_CHART,"Pie Chart");
       map.put(RING_CHART,"Ring Chart");
       
    
       return map;
   }
    
    /** Creates a new instance of BasicCategoryDatasetBasedCharts */
    public BasicPieDatasetBasedCharts(IExtPropertyGroupsBean pg, DataAccess da) {
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
        PieDataset dataset = null;
        JFreeChart chart = null;
        dataset  = (PieDataset)mDA.getDataSet();
        chart = generateChart(dataset);
       
        return chart;
    }
     
     private JFreeChart generateChart(PieDataset ds) {
        JFreeChart chart = null;
        ChartDefaults p = getChartDefaults();
        String type = (String)p.getProperty(JFChartConstants.CHART_TYPE).getValue();
        if(type.equals(PIE_CHART)) {
            chart = createPieChart(ds);
        } else if(type.equals(RING_CHART)) {
            chart = createRingChart(ds);
        } else {
            mLogger.info("Chart type" + type + "  not supported.") ; 
        }
        
       chart.setBackgroundPaint(this.getChartDefaults().getBackgroundColor());
       super.setTitle(chart);
       super.setLegend(chart);
       super.setBorder(chart); 
        
       return chart;
    }
     private JFreeChart createRingChart(PieDataset dataset)   {
         
         PieProperties pieProp       = (PieProperties)this.getSpecificProperties();
         ChartDefaults def = this.getChartDefaults();
         JFreeChart    chart         = null;
         String        title         = def.getTitle();
         boolean       includeLegend = def.isIncludeLegend();
         boolean       bUseTooltips  = true;
        boolean       bDrilldownEnabled = false;
         
         chart = ChartFactory.createRingChart(title,dataset,includeLegend,bUseTooltips,bDrilldownEnabled);
         
            return chart;
     }
     
     private JFreeChart createPieChart(PieDataset dataset)   {
         
         PieProperties pieProp       = (PieProperties)this.getSpecificProperties();
         ChartDefaults def = this.getChartDefaults();
         
        JFreeChart    chart         = null;
        String        sNoData       = "No Data";
        Object        value         = null;
        boolean       bHasData      = dataset != null;
        
        boolean       includeLegend = def.isIncludeLegend() && bHasData;
        boolean       is3D          = def.is3D();
        String        title         = def.getTitle();
        PiePlot       p             = null;
        DefaultPieDataset singlePieDataset = null;
        boolean       bUseTooltips  = true;
        boolean       bDrilldownEnabled = false;
        
        
        String realNoFormat  = def.getRealNumberFormat();
        DecimalFormat formatter = new DecimalFormat(realNoFormat);
	
           
            if (is3D) {
                chart = ChartFactory.createPieChart3D(
                    title, dataset, includeLegend, bUseTooltips, bDrilldownEnabled);
                p = (PiePlot) chart.getPlot();
                if (p instanceof PiePlot3D) {
                    PiePlot3D PiePlot3D = (PiePlot3D) p;
                    PiePlot3D.setDepthFactor(def.getDepthFactor());
                }
                //setURLGenerator(p, singlePieDataset);
            } else {
                chart = ChartFactory.createPieChart(
                    title, dataset, includeLegend, bUseTooltips, bDrilldownEnabled);
                p = (PiePlot) chart.getPlot();
                //setURLGenerator(p, singlePieDataset);
            }
      

        

        p.setCircular(def.isCircular());

        StandardPieSectionLabelGenerator standardPieItemLabelGenerator = null;
        if (bHasData && pieProp.isShowSectionLabels()) {
            String sLabelFormat = getLabelFormat(pieProp.getSectionLabel());
            standardPieItemLabelGenerator = new StandardPieSectionLabelGenerator(sLabelFormat,formatter,NumberFormat.getPercentInstance());
            Font sectionLabelFont = pieProp.getSectionLabelFont();
            if (sectionLabelFont != null) {
                p.setLabelFont(sectionLabelFont);
            }
            Color sectionLabelPaint = pieProp.getSectionLabelPaint();
            if (sectionLabelPaint != null) {
                p.setLabelPaint(sectionLabelPaint);
            }
            double sectionLabelGap = pieProp.getSectionLabelGap();
            sectionLabelGap = Math.min(sectionLabelGap, 1.0d);
            p.setLabelGap(sectionLabelGap);
        }
        p.setLabelGenerator(standardPieItemLabelGenerator);

        String sTooltipLabelFormat = getLabelFormat(pieProp.getSectionLabel());
        StandardPieSectionLabelGenerator tooltipGenerator = null;
        if (bHasData && sTooltipLabelFormat != null) {//tooltips) {
            tooltipGenerator = new StandardPieSectionLabelGenerator(sTooltipLabelFormat,formatter,NumberFormat.getPercentInstance());
        }

        p.setLabelGenerator(tooltipGenerator);
 
        double interiorGap = def.getInteriorGap();
        interiorGap = Math.min(interiorGap, PiePlot.MAX_INTERIOR_GAP);
        p.setInteriorGap(interiorGap);


        switch (def.getDirection()) {
            case ChartDefaults.CLOCKWISE:
                p.setDirection(Rotation.CLOCKWISE);
                break;
            default:
                p.setDirection(Rotation.ANTICLOCKWISE);
        }



        return chart;
		
    }
    
    
    String getLabelFormat(int sectionLabel) {
        String sLabelFormat = null;
        switch (sectionLabel) {
            case PieConstants.SECTION_NAME_LABELS:
                sLabelFormat = "{0}";
                break;
            case PieConstants.SECTION_VALUE_LABELS:
                sLabelFormat = "{1}";
                break;
            case PieConstants.SECTION_PERCENT_LABELS:
                sLabelFormat = "{2}";
                break;
            case PieConstants.SECTION_NAME_AND_VALUE_LABELS:
                sLabelFormat = "{0} = {1}";
                break;
            case PieConstants.SECTION_NAME_AND_PERCENT_LABELS:
                sLabelFormat = "{0} = {2}";
                break;
            case PieConstants.SECTION_VALUE_AND_PERCENT_LABELS:
                sLabelFormat = "{1},{2}";
                break;
            case PieConstants.SECTION_NAME_VALUE_AND_PERCENT_LABELS:
                sLabelFormat = "{0} = {1},{2}";
                break;
            case PieConstants.SECTION_NO_LABELS:
            default:
               break;
         }
        return sLabelFormat;
    }
    
   
}

