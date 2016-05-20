package com.bp.pensionline.reporting.chart;

import java.awt.Color;
import java.awt.GradientPaint;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.opencms.main.CmsLog;


public class PLWebStatsChart {
	public static final Log LOG = CmsLog.getLog(PLWebStatsChart.class);
	
	public static final String CHART_TYPE_BAR = "CHART_TYPE_BAR";
	public static final String CHART_TYPE_PIE = "CHART_TYPE_PIE";
	
	public static final Color[] CHART_COLOR_SET = new Color[] {
		new Color(0x00, 0x90, 0x00),
		new Color(0x00, 0x80, 0x00),
		new Color(0x10, 0x70, 0x00),
		new Color(0x20, 0x60, 0x00),
		new Color(0x30, 0x50, 0x00),
		new Color(0x40, 0x40, 0x00),
		new Color(0x50, 0x30, 0x00),
		new Color(0x60, 0x20, 0x00),
		new Color(0x70, 0x90, 0x00),
		new Color(0x80, 0x90, 0x00),
		new Color(0x90, 0x90, 0x00),
		new Color(0xa0, 0x90, 0x00),
		new Color(0xb0, 0x90, 0x00),
		new Color(0xc0, 0x90, 0x00),
		new Color(0xd0, 0x90, 0x10),
		new Color(0xe0, 0x90, 0x20),
		new Color(0xe0, 0x90, 0x30),
		new Color(0xd0, 0x90, 0x40),
		new Color(0xe0, 0x90, 0x50),
		new Color(0xe0, 0x90, 0x60),
		new Color(0xe0, 0x90, 0x70),
		new Color(0xe0, 0x90, 0x80),
		new Color(0xe0, 0x90, 0x90)
	};
	
	// size in pixel of image per record
	public static final int CHART_IMAGE_WIDTH_PER_RECORD = 34;	
	public static final int BAR_CHART_IMAGE_WIDTH  = 760;
	public static final int BAR_CHART_IMAGE_HEIGHT = 400;
	public static final int PIE_CHART_IMAGE_WIDTH  = 760;
	public static final int PIE_CHART_IMAGE_HEIGHT = 760;
	
	// name representing the chart
	private String chartName = null;
	private String chartType = null;
	
	// data used to draw chart
	private  Vector<String> xValues = new Vector<String>();
	private  Vector<String> yValues = new Vector<String>();
	
	private String xLabel = null;
	private String yLabel = null;
	
	public PLWebStatsChart(String chartName, String chartType, Vector<String> xValues, Vector<String> yValues, String xLabel, String yLabel) {
		this.chartName = "";
		this.chartType = chartType;
		this.xValues = xValues;
		this.yValues = yValues;
		
		this.xLabel = xLabel;
		this.yLabel = yLabel;
		if (this.xLabel == null)
		{
			this.xLabel = "";
		}
		if (this.yLabel == null)
		{
			this.yLabel = "";
		}		
	}
	
	public boolean createChartImage(String fileImage) {
		if (chartType != null && chartType.equals(CHART_TYPE_BAR))
		{
			//LOG.info("createBarChart begin...");
			return createBarChartImage(fileImage);
		}
		else if (chartType != null && chartType.equals(CHART_TYPE_PIE))
		{
			//LOG.info("createPieChartImage begin...");
			return createPieChartImage(fileImage);
		}
		
		return false;
	}
	
	/**
	 * Create chart PNG image and store to disk
	 * @param fileImage
	 * @return: true: If file created successfully
	 */
	public boolean createBarChartImage(String fileImage) {
		// create dataset for chart from user's dataset
		DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
		
		if (xValues == null || xValues.size() == 0 || yValues == null || yValues.size() == 0 || 
				xValues.size() != yValues.size())
		{
			LOG.info("Create WebStat chart aborted due to data not valid!");
			return false;
		}
		
		for (int i = 0; i < xValues.size(); i++)
		{
			String key = (String) xValues.elementAt(i);
			String value = (String) yValues.elementAt(i);
			
			if (key != null && value != null)
			{
				if (value.indexOf(".") >= 0)
				{
					try
					{
						categoryDataset.addValue(Float.parseFloat(value), "", key);
					}
					catch (NumberFormatException nfe)
					{						
						return false;
					}					
				}
				else
				{
					try
					{
						categoryDataset.addValue(Integer.parseInt(value), "", key);
					}
					catch (NumberFormatException nfe)
					{						
						return false;
					}
					
				}
			}			
		}

		LOG.info("create Bar Chart Image: " + chartName);
		JFreeChart chart = ChartFactory.createBarChart(chartName, xLabel, yLabel, categoryDataset, PlotOrientation.VERTICAL, false, true, false);
		
//		Remove title from char image
//		Font titleFont = new Font("Arial", Font.BOLD, 18);
//		TextTitle chartTitle = new TextTitle (chartName, titleFont);
//		chartTitle.setPaint(new Color(0x90, 0xc0, 0x00));
//		
//		chart.setTitle(chartTitle);

		//		 NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customisation...
		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(new Color(0xd0, 0xd0, 0xd0));
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		// set the range axis to display integers only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// disable bar outlines...
		final BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);

		// set up gradient paints for series...
		final GradientPaint gp0 = new GradientPaint(0.0f, 0.0f, new Color(0x00, 0x90, 0x00),
				0.0f, 0.0f, new Color(0xd0, 0xd0, 0xd0));
		final GradientPaint gp1 = new GradientPaint(0.0f, 0.0f, Color.pink,
				0.0f, 0.0f, new Color(0xd0, 0xd0, 0xd0));
		final GradientPaint gp2 = new GradientPaint(0.0f, 0.0f, Color.red,
				0.0f, 0.0f, new Color(0xd0, 0xd0, 0xd0));
		renderer.setSeriesPaint(0, gp0);
		renderer.setSeriesPaint(1, gp1);
		renderer.setSeriesPaint(2, gp2);

		final CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
		// OPTIONAL CUSTOMISATION COMPLETED.
//		domainAxis.setLabelPaint(new Color(0x06, 0x06, 0x06));
//		domainAxis.setTickLabelPaint(new Color(0x06, 0x06, 0x06));
		
//		int chartImageWidth = xValues.size() * CHART_IMAGE_WIDTH_PER_RECORD;
//		if (chartImageWidth < 400)
//		{
//			chartImageWidth = 400;
//		}
		
		int chartImageWidth = BAR_CHART_IMAGE_WIDTH;
		// Save chart as a PNG
		try
		{
			ChartUtilities.saveChartAsPNG (new File(fileImage), chart, chartImageWidth, BAR_CHART_IMAGE_HEIGHT);
		}
		catch (IOException ioe)
		{
			LOG.error("Error in saving bar chart image: " + ioe);
			return false;
		}
		
		return true;

	}	
	
	
	/**
	 * Create chart PNG image and store to disk
	 * @param fileImage
	 * @return: true: If file created successfully
	 */
	public boolean createPieChartImage(String fileImage) {
		// create dataset for chart from user's dataset
		DefaultPieDataset categoryDataset = new DefaultPieDataset();
		
		if (xValues == null || xValues.size() == 0 || yValues == null || yValues.size() == 0 || 
				xValues.size() != yValues.size())
		{
			LOG.info("Create WebStat chart aborted due to data not valid!");
			return false;
		}
		
		for (int i = 0; i < xValues.size(); i++)
		{
			String key = (String) xValues.elementAt(i);
			String value = (String) yValues.elementAt(i);
			
			if (key != null && value != null)
			{
				if (value.indexOf(".") >= 0)
				{
					try
					{
						categoryDataset.setValue(key, Float.parseFloat(value));
					}
					catch (NumberFormatException nfe)
					{
						LOG.error("Value is not in valid format for float: " + nfe);
						return false;
					}
					
				}
				else
				{
					try
					{
						categoryDataset.setValue(key, Integer.parseInt(value));
					}
					catch (NumberFormatException nfe)
					{
						LOG.error("Value is not in valid format for integer: " + nfe);
						return false;
					}
					
				}
			}			
		}

		LOG.info("create Pie Chart Image: " + chartName);
		JFreeChart chart = ChartFactory.createPieChart(chartName, categoryDataset, false, false,
				false);
//		Remove title from chart	image	
//		Font titleFont = new Font("Arial", Font.BOLD, 18);
//		TextTitle chartTitle = new TextTitle (chartName, titleFont);
//		chartTitle.setPaint(new Color(0x90, 0xc0, 0x00));
//		
//		chart.setTitle(chartTitle);
		

		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setStartAngle(45);

		// Specify the colors here		
		for (int i = 0; i < xValues.size(); i++)
		{
			int colorIndex = (i % CHART_COLOR_SET.length);
			plot.setSectionPaint(xValues.elementAt(i), CHART_COLOR_SET[colorIndex]);
		}

		//		 NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);
		
//		int chartImageWidth = (CHART_IMAGE_WIDTH_PER_RECORD * xValues.size());
//		if (chartImageWidth < 400)
//		{
//			chartImageWidth = 400;
//		}
//		int chartImageHeight = (chartImageWidth * 80) / 100;
		
		int chartImageWidth = PIE_CHART_IMAGE_WIDTH;
		int chartImageHeight = PIE_CHART_IMAGE_HEIGHT;
		// Save chart as a PNG
		try
		{
			ChartUtilities.saveChartAsPNG (new File(fileImage), chart, chartImageWidth, chartImageHeight);
		}
		catch (IOException ioe)
		{
			LOG.error("Error in saving pie chart image: " + ioe);
			return false;
		}
		
		return true;

	}	
}

