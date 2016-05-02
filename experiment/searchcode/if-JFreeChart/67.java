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
 * @(#)Chart.java
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
import org.openesb.tools.extpropertysheet.IExtPropertyGroup;
import org.openesb.tools.extpropertysheet.IExtPropertyGroupsBean;
import java.awt.Color;
import java.util.Map;

import java.util.logging.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

/**
 *
 * @author rdwivedi
 */
public abstract class Chart {
    
    
    
    private static Logger mLogger = Logger.getLogger(Chart.class.getName());
    
    
    abstract public JFreeChart createChart() throws ChartException ;
    abstract public ChartDefaults getChartDefaults() ;
    
    
    // Chart specific methods common to all
    
    protected void setTitle(JFreeChart chart) {
        
        ChartDefaults chartProp = getChartDefaults();
        String title = chartProp.getTitle();
        if (!chartProp.isDisplayTitle()) {
            chart.setTitle((String) null);
        } else {
            int nHorizonalAlignment = chartProp.getTitleAlignment();
            HorizontalAlignment horizontalAlignment = null;
            switch (nHorizonalAlignment) {
                case ChartDefaults.LEFT:
                    horizontalAlignment = HorizontalAlignment.LEFT; //TextTitle.LEFT;
                    break;
                case ChartDefaults.RIGHT:
                    horizontalAlignment = HorizontalAlignment.RIGHT; //TextTitle.RIGHT;
                    break;
                default:
                    horizontalAlignment = HorizontalAlignment.CENTER; //TextTitle.CENTER;
            }
            TextTitle textTitle = new TextTitle(title, chartProp.getTitleFont());
            Color titleColor = chartProp.getTitleColor();
            if (titleColor != null) {
                textTitle.setPaint(titleColor);
            }
            Color titleBackground = chartProp.getTitleBackground();
            if (titleBackground != null) {
                textTitle.setBackgroundPaint(titleBackground);
            }
            textTitle.setHorizontalAlignment(horizontalAlignment);
            chart.setTitle(textTitle);
        }
    }
    /**
     * Sets legend of the chart basing on chart properties
     * @param chart - chart
     */
    protected void setLegend(JFreeChart chart) {
        ChartDefaults chartProp = getChartDefaults();
        if (!chartProp.isIncludeLegend()) {
            return;
        }
        LegendTitle legend = chart.getLegend();
        if (legend == null) {
            return;
        }
        int anchor = chartProp.getLegendAnchor();
        switch (anchor) {
            case ChartDefaults.ANCHOR_NORTH:
                legend.setPosition(RectangleEdge.TOP);
                break;
            case ChartDefaults.ANCHOR_EAST:
                legend.setPosition(RectangleEdge.RIGHT);
                break;
            case ChartDefaults.ANCHOR_WEST:
                legend.setPosition(RectangleEdge.LEFT);
                break;
            default:
                legend.setPosition(RectangleEdge.BOTTOM);
        }
    }
    
    /**
     * Sets border for the chart
     * @param chart - chart
     */
    protected void setBorder(JFreeChart chart) {
        ChartDefaults chartProp = getChartDefaults();
        boolean borderVisible = chartProp.isBorderVisible();
        if (borderVisible) {
            chart.setBorderVisible(true);
            Color borderPaint = chartProp.getBorderPaint();
            if (borderPaint != null) {
                chart.setBorderPaint(borderPaint);
            }
        }
    }
    
    
    public static Map getAllowedChartsForDatasetType(String dsType){
            Map map = null;
         if(dsType.equals(JFChartConstants.CATEGORY_DATASET)){
             map = BasicCategoryDatasetBasedCharts.getAllAllowedCharts();
            
        } else if(dsType.equals(JFChartConstants.XY_DATASET)){
             map = BasicXYDatasetBasedCharts.getAllAllowedCharts();
            
        } else if(dsType.equals(JFChartConstants.VALUE_DATASET)){
             map = BasicSingleValueDatasetBasedCharts.getAllAllowedCharts();
            
        } else if(dsType.equals(JFChartConstants.PIE_DATASET)){
             map = BasicPieDatasetBasedCharts.getAllAllowedCharts();
            
        }
             return map;
    }
            
        
    public static Chart createChartObject(IExtPropertyGroupsBean pg, DataAccess da) throws ChartException {
        Chart chart = null;
        String datasetType = da.getDatasetType();
            if(datasetType.equals(JFChartConstants.CATEGORY_DATASET)){
                chart = new BasicCategoryDatasetBasedCharts(pg,da);
            } else if (datasetType.equals(JFChartConstants.XY_DATASET)) {
                chart = new BasicXYDatasetBasedCharts(pg,da);
            } else if(datasetType.equalsIgnoreCase(JFChartConstants.VALUE_DATASET)) {
                chart =  new BasicSingleValueDatasetBasedCharts( pg,da);
            } else if(datasetType.equalsIgnoreCase(JFChartConstants.PIE_DATASET)) {
                chart =  new BasicPieDatasetBasedCharts( pg,da);
            }  else {
                throw new ChartException("Chart Properties are not set.");
            }
        
        return chart;
    }
             
    
}

