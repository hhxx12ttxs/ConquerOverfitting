/**
 * 
 */
package com.primesc.eu.statistic;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.primesc.eu.db.mysql.MySQLUtils;
import com.primesc.eu.exceptions.StatisticCriticalException;
import com.primesc.eu.object.ObjectInitProperties;
import com.primesc.eu.values.PrimescEu;


/**
 * @author Igor Afteni
 *
 */
public class Statistic extends ObjectInitProperties {
	private static Logger log = LoggerFactory.getLogger(Statistic.class);
	
	private String _anc_statistic_folder = null;	
	private String _anc_statistic_file_name = null;
	
	public Statistic() throws StatisticCriticalException 
	{
		try {
			super.init();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			this.initOk = false;
			log.error("IOException", e);
			throw new StatisticCriticalException("ERROR to initialise SendMail object", e);
		}
		
		if( PrimescEu._forTest )
			this._anc_statistic_folder = properties.getProperty("folder.anc.statistic.path_test");
		else
			this._anc_statistic_folder = properties.getProperty("folder.anc.statistic.path");
			
		this._anc_statistic_file_name = properties.getProperty("file.name.anc.statistic");
		
		this.initOk = true;
	}
	
	@SuppressWarnings("deprecation")
	public void genPieChart3D(int year) throws Throwable {
		try {
			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	
	        Map<String, Integer> stat = ( new MySQLUtils() ).getFoldersStatistic(year);
	         
	        Set<Entry<String, Integer>> set = stat.entrySet();
	        Iterator<Entry<String, Integer>> iterator = set.iterator();
	         
	        while( iterator.hasNext() ){
	        	Entry<String, Integer> entry = iterator.next();
	        	dataset.setValue(entry.getValue().intValue(), "Numarul de dosare", entry.getKey());	        	
	        }


	         JFreeChart chart =
	               ChartFactory.createBarChart3D (
	                     "Datele despre dosarele din " + year + " publicate in ordinele ANC", "Numarul Dosarului", "Total Publicate",
	                     dataset,
	                     PlotOrientation.VERTICAL,
	                     false,
	                     true,
	                     false);
	         
	         
	         
	      // Set the background colour of the chart
	         chart.setBackgroundPaint(Color.WHITE);	         
	         
	         // Set the background colour of the chart
	         chart.getTitle().setPaint(Color.blue);
	         
	         CategoryPlot plot = chart.getCategoryPlot(); 
	         plot.setBackgroundPaint(Color.white);      
	         plot.setRangeGridlinePaint(Color.red);

	         CategoryItemRenderer renderer = plot.getRenderer();
	         renderer.setSeriesPaint(0, Color.green);
	         renderer.setSeriesItemLabelsVisible(0, Boolean.TRUE);
	         
	         CategoryAxis domainAxis = plot.getDomainAxis();
	         domainAxis.setCategoryLabelPositions(
	             CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 3.4)
	         );
	         
	         BarRenderer bRenderer = (BarRenderer) plot.getRenderer();
	         DecimalFormat decimalformat1 = new DecimalFormat("##");
	         bRenderer.setItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", decimalformat1));
	         bRenderer.setItemLabelsVisible(true);
	         chart.getCategoryPlot().setRenderer(bRenderer);	         
	         
	         //response.setContentType("image/png");
	         
	         log.info("Generate file: " + _anc_statistic_folder + _anc_statistic_file_name + year + ".png");
	         
	         File statFile = new File(_anc_statistic_folder + _anc_statistic_file_name + year + ".png");
	         ChartUtilities.saveChartAsPNG(statFile, chart, 625, 500);
	         //ChartUtilities.writeChartAsPNG(out, chart, 625, 500);
	         
	         log.info("image generated");
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
		}
	 }
}

