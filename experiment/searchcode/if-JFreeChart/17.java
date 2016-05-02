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
 * @(#)BasicSingleValueDatasetBasedCharts.java
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
import org.openesb.tools.extchart.property.meter.MeterProperties;
import org.openesb.tools.extpropertysheet.IExtPropertyGroup;
import org.openesb.tools.extpropertysheet.IExtPropertyGroupsBean;
import com.sun.web.ui.component.Property;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.data.Range;
import org.jfree.data.general.ValueDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

/**
 *
 * @author rdwivedi
 */
public class BasicSingleValueDatasetBasedCharts extends Chart {
   
    private IExtPropertyGroupsBean propGrps = null;
    private DataAccess mDA = null;
    private static Logger mLogger = Logger.getLogger(BasicSingleValueDatasetBasedCharts.class.getName());
    private static String METER_CHART = "meterChart";
    private static String THERMO_CHART = "thermoChart";
    
    
     
    
    public static Map getAllAllowedCharts() {
       HashMap map = new HashMap();
       map.put(METER_CHART,"Meter Chart");
       map.put(THERMO_CHART,"Thermo Chart");
       
    
       return map;
   }
    
    /** Creates a new instance of BasicCategoryDatasetBasedCharts */
    public BasicSingleValueDatasetBasedCharts(IExtPropertyGroupsBean pg, DataAccess da) {
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
        ValueDataset dataset = null;
        JFreeChart chart = null;
        dataset  = (ValueDataset)mDA.getDataSet();
        chart = generateChart(dataset);
       
        return chart;
    }
    
    private JFreeChart generateChart(ValueDataset ds) {
        JFreeChart chart = null;
        ChartDefaults p = getChartDefaults();
        String type = (String)p.getProperty(JFChartConstants.CHART_TYPE).getValue();
        if(type.equals(METER_CHART)) {
            chart = createMeterChart(ds);
        } else if(type.equals(THERMO_CHART)) {
            chart = createThermoChart(ds);
        } else {
            mLogger.info("Chart type" + type + "  not supported.") ; 
        }
        
       chart.setBackgroundPaint(this.getChartDefaults().getBackgroundColor());
       super.setTitle(chart);
       super.setLegend(chart);
       super.setBorder(chart); 
        
       return chart;
    }
    
    private JFreeChart createMeterChart(ValueDataset ds){
        JFreeChart chart = null;
        
        MeterPlot plot = null;
        
        MeterProperties meterProp = (MeterProperties)getSpecificProperties();
        ChartDefaults def = this.getChartDefaults();
        plot = new MeterPlot(ds);
            DecimalFormat df = new DecimalFormat(def.getRealNumberFormat());
            plot.setTickLabelFormat(df);
            plot.setUnits(meterProp.getUnits());
            Range range = new Range(meterProp.getMinimumValue(), meterProp.getMaximumValue());
            plot.setRange(range);


            Range normalRange = new Range(meterProp.getMinimumNormalValue(), meterProp.getMaximumNormalValue());
            MeterInterval normal = new MeterInterval("Normal",normalRange,meterProp.getNormalPaint(),new BasicStroke(2.0f),null) ;
            plot.addInterval(normal);

            Range warningRange = new Range(meterProp.getMinimumWarningValue(), meterProp.getMaximumWarningValue());
            MeterInterval warning = new MeterInterval("Warning",warningRange,meterProp.getWarningPaint(),new BasicStroke(2.0f),null) ;
            plot.addInterval(warning);


            Range criticalRange = new Range(meterProp.getMinimumCriticalValue(), meterProp.getMaximumCriticalValue());
            MeterInterval critical = new MeterInterval("Critical",criticalRange,meterProp.getCriticalPaint(),new BasicStroke(2.0f),null) ;

            plot.addInterval(critical);


            double maxValue = Math.max(meterProp.getMaximumCriticalValue(),
                                        meterProp.getMaximumValue());

            

            int dialType = meterProp.getDialType();
            switch (dialType) {
                case MeterProperties.DIALTYPE_PIE:
                    plot.setDialShape(DialShape.PIE);
                    break;
                case MeterProperties.DIALTYPE_CHORD:
                    plot.setDialShape(DialShape.CHORD);
                    break;
                default:
                    plot.setDialShape(DialShape.CIRCLE);
            }

            int tickLabelType = meterProp.getTickLabelType();
            if (tickLabelType == MeterProperties.TICK_NO_LABELS) {
                plot.setTickLabelsVisible(false);
            }

            Font tickLabelFont = meterProp.getTickLabelFont();
            if (tickLabelFont != null) {
                plot.setTickLabelFont(tickLabelFont);
            }

            plot.setDrawBorder(meterProp.isDrawBorder());
            plot.setDialBackgroundPaint(meterProp.getDialBackgroundPaint());
            plot.setDialOutlinePaint((Paint) meterProp.getDialBorderColor());
            plot.setNeedlePaint(meterProp.getNeedlePaint());
            plot.setValuePaint(meterProp.getValuePaint());
            //plot.setMeterAngle(meterProp.getMeterAngle());
            Font valueFont = meterProp.getValueFont();
            if (valueFont != null) {
                plot.setValueFont(valueFont);
            }


        

        plot.setInsets(new RectangleInsets(5, 5, 5, 5));
        String title = null;
        String sNoData = "No Data";
        
        chart = new JFreeChart(title, def.getTitleFont(), plot, def.isIncludeLegend());
       return chart;
    }
     
    private JFreeChart createThermoChart(ValueDataset ds){
       MeterProperties meterProp = (MeterProperties)getSpecificProperties();
        ChartDefaults def = this.getChartDefaults();
       boolean       includeLegend = def.isIncludeLegend() ;
       boolean       is3D          = def.is3D();
       String        title         = def.getTitle();
       boolean       bUseTooltips  = true;
       boolean       bDrilldownEnabled = false;
       ThermometerPlot plot = new ThermometerPlot(ds);
        JFreeChart chart = new JFreeChart(
            "Thermometer Demo 1",  // chart title
            JFreeChart.DEFAULT_TITLE_FONT,
            plot,                  // plot
            false                  // no legend
        );  
        
         

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        plot.setInsets(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setThermometerStroke(new BasicStroke(2.0f));
        plot.setThermometerPaint(Color.lightGray);
        // OPTIONAL CUSTOMISATION COMPLETED.
              
       return chart;
    }
    
    
   
}

