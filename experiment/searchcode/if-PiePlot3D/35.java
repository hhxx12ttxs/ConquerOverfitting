/*
* Copyright (C)   Enrico Liboni  - enrico@computer.org
*
*   This program is free software; you can redistribute it and/or modify
*   it under the terms of the LGPL License as published by
*   the Free Software Foundation;
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. *  
*/


package art.graph;

import java.util.*;
import java.sql.ResultSet;
import de.laures.cewolf.*;
import de.laures.cewolf.links.*;

import org.jfree.data.*;
import org.jfree.data.time.*;
import org.jfree.data.general.*;
import org.jfree.data.xy.*;
import org.jfree.data.category.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;

import java.text.*; //to format numbers in tooltips
import java.io.*; //to save chart to png file + enable serialization
import de.laures.cewolf.tooltips.PieToolTipGenerator;


/**
<b>artPie</b> is used to chart a pie.
<br>
See <i>artGraph</i> interface API for description and usage example.
<br>
The <i> resultSet</i> is expected to have the following layout:
<ol>
	<li>the first column must be a character datatype (String). The value
		represent the category name.
	</li>
	<li>the second colums must be numeric.
	</li>
	<li>(optional) if setUseHyperLinks(true) is used, 
		the 3rd column must be a String (an hyperlink).
	</li>
</ol>
	<i>ResultSet  Example:</i><br>
	<tt>select CATEGORY , NUMBER1 [, HyperLink]  from ...</tt>
	<br>
	<i>Note:</i><br>
	<ul>
	<li>the <i>X/Y labels</i> make no sense here.
	</li>
	</ul>
	
*/
public class artPie implements artGraph, DatasetProducer, PieToolTipGenerator,ChartPostProcessor, PieSectionLinkGenerator, Serializable{
	//classes implementing chartpostprocessor need to be serializable to use cewolf-ulf and ResultSet isn't serializable
	//ResultSet rs;

	String title ="Title";
	String xlabel ="x Label";
	String ylabel ="y Label";
	String seriesName ="Series Name";
	
	java.util.HashMap hyperLinks; //Vector hyperLinks;

	int height = 300;
	int width  = 500;

	String bgColor ="#FFFFFF";

	boolean useHyperLinks = false;

	DefaultPieDataset dataset = new DefaultPieDataset();

	public artPie() {

	}

	//classes implementing chartpostprocessor need to be serializable to use cewolf-ulf and ResultSet isn't serializable
	/*
	public void setResultSet(ResultSet rs) {
		// 1st column = category name (string)
		// 2nd column = Y axis (value, double)
		// 3rd column = hyperlink (optional)
		this.rs = rs;   
	}
	*/

	public void setTitle(String title) {
		this.title = title;   
	}

	public String getTitle() {
		return title;   
	}

	public void setXlabel(String xlabel) {
		this.xlabel = xlabel;   
	}

	public String getXlabel() {
		return xlabel;   
	}

	public void setYlabel(String ylabel) {
		this.ylabel = ylabel;   
	}

	public String getYlabel() {
		return ylabel;   
	}

	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;   
	}

	public void setWidth(int width) {
		this.width = width;   
	}

	public int getWidth() {
		return width;   
	}

	public void setHeight(int height) {
		this.height = height; 
	}

	public int getHeight() {
		return height; 
	}

	public void setBgColor(String bgColor) {
		this.bgColor = bgColor;   
	}

	public String getBgColor() {
		return bgColor;   
	}

	public void setUseHyperLinks(boolean b) {
		this.useHyperLinks = b;   
	}

	public boolean getUseHyperLinks() {
		return useHyperLinks;
	}

	//pass resultset rather than have it as a class variable
	public void prepareDataset(ResultSet rs) throws java.sql.SQLException{
		//String[] categories  =   new String[itemsNumber]; // {"mon", "tue", "wen", "thu", "fri", "sat", "sun"};
		String[] seriesNames =   new String[1];  //{ "tutorial.jsp", "testpage.jsp", "performancetest.jsp"};
		//Integer[] [] values = new Integer[seriesNames.length] [categories.length];
		seriesNames[0] = seriesName;

		java.sql.ResultSetMetaData rsmd = rs.getMetaData();
		useHyperLinks = (rsmd.getColumnCount() == 3);
		
		if (useHyperLinks) {
			hyperLinks = new java.util.HashMap();//hyperLinks = new Vector(10);
		}

		//there is only one series, this is for reference
		for (int series = 0; series < seriesNames.length; series ++) {
			while(rs.next()) {
				//  Pie data set: setValue(category, value)
				dataset.setValue(rs.getString(1), rs.getDouble(2));
				if (useHyperLinks) {
					hyperLinks.put(rs.getString(1), rs.getString(3));//hyperLinks.add( rs.getString(3));
				}
			}
		}
	}

	public Object produceDataset(Map params) {
		return dataset;
	}

	public String getProducerId() {
		return "CatDataProducer";
	}

	public boolean hasExpired(Map params, Date since) {
		return true;
	}

	public String generateToolTip(PieDataset dataset, Comparable section, int index) {
		//return String.valueOf(dataset.getValue( index)); // String.valueOf(index);
		
		double dataValue;	   
		DecimalFormat valueFormatter;
		String formattedValue;
		
		//get data value to be used as tooltip
		dataValue=dataset.getValue(index).doubleValue();
		
		//format value. use numberformat factory method to set formatting according to the default locale	   		
		NumberFormat nf=NumberFormat.getInstance();
		valueFormatter=(DecimalFormat)nf;		
		
		formattedValue=valueFormatter.format(dataValue);		
		
		//return final tooltip text	   
		//return String.valueOf(section) + "=" + formattedValue; //category name and value
		return formattedValue;
	}

	public String generateLink(Object data, Object category) {
		return (String) hyperLinks.get(category);
	}

	// get the plot object and set the label / default one is too big...
	public void processChart(Object chart, Map params) {
		PiePlot3D plot = (PiePlot3D)((JFreeChart) chart).getPlot();  

		// switch off labels
		String labelFormat = (String)params.get("labelFormat");
		if (labelFormat.equals("off")) {
			plot.setLabelGenerator(null); 
		} else {
			plot.setLabelGenerator(new StandardPieSectionLabelGenerator(labelFormat));  
		}

		
		// Output to file if required     	  
		String outputToFile= (String)params.get("outputToFile");
		
		if (outputToFile.equals("pdf")){
			String pdfFile = (String)params.get("pdfFile");
			if (pdfFile  != null && !pdfFile.equals("nofile")) {
				PdfGraph.createPdf(chart, pdfFile, (String)params.get("title"));
			}
		}		
		
		if (outputToFile.equals("png")){						
			//save chart as png file						
			int chartWidth=((Integer)params.get("width")).intValue();
			int chartHeight=((Integer)params.get("height")).intValue();
			String pngFile = (String)params.get("pngFile");
			
			try{				
				ChartUtilities.saveChartAsPNG(new File(pngFile),(JFreeChart)chart,chartWidth,chartHeight);				
			}
			catch(Exception e){
				System.out.println(e);
			}
		}
	}
}


