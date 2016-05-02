/* ===========================================================
 * $Id: LineStyle.java 518 2009-08-24 05:33:01Z bitorb $
 * This file is part of Micrite
 * ===========================================================
 *
 * (C) Copyright 2009, by Gaixie.org and Contributors.
 * 
 * Project Info:  http://micrite.gaixie.org/
 *
 * Micrite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Micrite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Micrite.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.gaixie.micrite.jfreechart.style;

import java.awt.Color;
import java.awt.Paint;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;

/**
 * ?????
 */
public class LineStyle {

    /**
     * ???????
     * <p>
     * ??micrite??????
     * @param chart JFreeChart??
     */
    public static void styleDefault(JFreeChart chart){
        LineStyle.setBackground(chart);
    }
    public static void styleOne(JFreeChart chart){
        XYPlot xyplot = (XYPlot)chart.getPlot();
        XYDataset xyd = xyplot.getDataset();
        //????
        LineStyle.setBackground(chart);
        XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer)xyplot.getRenderer();
        if(xyd!=null){
            //??????
            xylineandshaperenderer.setBaseShapesVisible(true);
            //????????
            xylineandshaperenderer.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
            xylineandshaperenderer.setBaseItemLabelsVisible(true);
            xyplot.setRenderer(xylineandshaperenderer);
            //tooltip
//            xyplot.getRenderer().setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: ({1}, {2})",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),new DecimalFormat()));
            //?????
            for(int i = 0 ;i<xyd.getSeriesCount();i++){
                int colorIdx = i % colors.length;
                xylineandshaperenderer.setSeriesPaint(i, colors[colorIdx]);
            }
        }
        else{
            xyplot.setNoDataMessage("NO DATA");
        }
        
    }
    /**
     * ?????????
     * <p>
     * ??????????#EEE,??????????  
     * @param chart JFreeChart??
     */    
    public static void setBackground(JFreeChart chart){
        chart.setBackgroundPaint(null);
    }  
    /**
     * ????
     */
    public static Paint colors[] = { 
        Color.decode("#999933"), Color.decode("#88AACC"),
        Color.decode("#666699"), Color.decode("#CC9933"),
        Color.decode("#006666"), Color.decode("#3399FF"),
        Color.decode("#993300"), Color.decode("#AAAA77"),
        Color.decode("#666666"), Color.decode("#FFCC66"),
        Color.decode("#6699CC"), Color.decode("#663366"),
        Color.decode("#9999CC"), Color.decode("#AAAAAA"),
        Color.decode("#669999"), Color.decode("#BBBB55"),
        Color.decode("#CC6600"), Color.decode("#9999FF"),
        Color.decode("#0066CC"), Color.decode("#99CCCC"),
        Color.decode("#999999"), Color.decode("#FFCC00"),
        Color.decode("#009999"), Color.decode("#99CC33"),
        Color.decode("#FF9900"), Color.decode("#999966"),
        Color.decode("#66CCCC"), Color.decode("#339966"),
        Color.decode("#CCCC33") 
    };

}

