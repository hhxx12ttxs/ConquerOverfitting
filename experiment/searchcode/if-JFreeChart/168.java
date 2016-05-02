/* ===========================================================
 * $Id: BarStyle.java 506 2009-08-19 06:51:18Z yebo2009@gmail.com $
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

import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;

/**
 * ?????
 * <p>
 * 
 */
public class BarStyle {

    /**
     * ???????
     * <p>
     * ??micrite??????
     * @param chart JFreeChart??
     */
    public static void styleDefault(JFreeChart chart){
        BarStyle.setBackground(chart);
                
    }
    
    /**
     * ????????????????????
     * @param chart JFreeChart??
     */
    public static void styleOne(JFreeChart chart){
        CategoryPlot categoryplot = (CategoryPlot) chart.getPlot();
        CategoryDataset dca = categoryplot.getDataset();
        //????
        chart.setBackgroundPaint(null);
        if(dca!=null){
            BarRenderer categoryitemrenderer = (BarRenderer)categoryplot.getRenderer();
            // ????????????????????
            categoryitemrenderer.setBaseItemLabelGenerator(
                    new StandardCategoryItemLabelGenerator());
//            categoryitemrenderer.setBaseItemLabelFont(
//                    new Font("??", Font.PLAIN, 15));
            categoryitemrenderer.setBaseItemLabelsVisible(true);
            //tooltip
            categoryplot.getRenderer().setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("{0}={2}",new DecimalFormat()));
            //?????
            for(int i = 0 ;i<dca.getRowCount();i++){
                int colorIdx = i % colors.length;
                categoryitemrenderer.setSeriesPaint(i, colors[colorIdx]);
            }
        }
        else{
            //???
            categoryplot.setNoDataMessage("NO DATA");
        }
    }

    /**
     * ???????????????
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
        Color.decode("#88AACC"), Color.decode("#999933"),
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

