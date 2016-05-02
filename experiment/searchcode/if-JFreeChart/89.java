/**
 ******************************************************************************
 *
 * Project        EPA CMS
 * File           RenderGraphReport.java
 * Created on     June 22, 2005
 *
 ******************************************************************************
 *
 * PVCS Maintained Data
 *
 * Revision       $Revision: 1.1 $
 * Modified on    $Date: 2006/04/06 20:01:57 $
 *
 ******************************************************************************
 */
package com.custom.library.reports;

import com.custom.library.utility.*;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;
import com.documentum.web.common.*;
import com.documentum.web.form.*;
import com.documentum.web.form.control.*;
import com.documentum.web.form.control.databound.*;
import com.documentum.web.formext.component.*;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.session.SessionManagerHttpBinding;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.*;
import com.lowagie.text.Rectangle;

import java.awt.*;
import java.awt.geom.*;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.jfree.chart.axis.*;
import org.jfree.chart.block.AbstractBlock;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.text.TextBlockAnchor ;
import org.jfree.ui.*;

public class GraphReport
{
	private ArrayList result_list = null;

	private String strChartTitle = "";
	private String strDomainLabel = "";
	private String strRangeLabel = "";


	public GraphReport() {
	}

	public void go()
	{
		// create dataset with input data list
		CategoryDataset dataset = createDataset();

		// create chart
		JFreeChart chart = createChart(dataset);
	}

	private CategoryDataset createDataset()
	{
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		int count = result_list.size();
		for(int i = 0; i < count; i++)
		{
			Object [] data = (Object[])result_list.get(i);
			dataset.addValue((Integer)data[0], (String)data[1], (String)data[2]);
		}
		return dataset;
  }

	private JFreeChart createChart(CategoryDataset dataset)
	{
		// create the chart...
		JFreeChart chart=ChartFactory.createBarChart(strChartTitle,    // chart title
			strDomainLabel,   // domain axis label
			strRangeLabel,    // range axis label
			dataset,          // data
			PlotOrientation.VERTICAL, // orientation
			true,                     // include legend
			true,                     // tooltips?
			false                     // URLs?
		);

		LegendTitle sl = (LegendTitle)chart.getLegend();
		sl.setItemFont(new java.awt.Font(null,0,6));
		//sl.setBackgroundPaint(new GradientPaint(0.0f, 0.0f, Color.magenta, 0.0f, 0.0f, Color.magenta));
		chart.setBackgroundPaint(Color.white);

    	// get a reference to the plot for further customisation...
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.white);

		CategoryAxis hAxis = plot.getDomainAxis();
		//hAxis.setVerticalCategoryLabels(true);
		//hAxis.setSkipCategoryLabelsToFit(true);
		hAxis.setLowerMargin(0.10); // two percent
		hAxis.setCategoryMargin(0.10); // ten percent
		hAxis.setUpperMargin(0.10); // two percent
		hAxis.setLabelFont(new java.awt.Font(null, 0, 8));
		hAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
		ValueAxis vAxis = plot.getRangeAxis();
		vAxis.setLabelFont(new java.awt.Font(null, 0, 8));
		plot.setDomainAxis(hAxis);

		//Geshanglakhang
		//Swayambhunath Temple

 	   // set the range axis to display integers only...
  	  final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

    // disable bar outlines...
    BarRenderer renderer = (BarRenderer) plot.getRenderer();
    renderer.setDrawBarOutline(false);
    renderer.setItemLabelGenerator(new StandardCategoryItemLabelGenerator());
    renderer.setItemLabelFont(new java.awt.Font(null, 0, 6));
    renderer.setItemLabelsVisible(true);


    CategoryAxis categoryAxis = new CategoryAxis();
    categoryAxis.setMaximumCategoryLabelLines( 1 );

    CategoryLabelPositions positions = new CategoryLabelPositions(
      new CategoryLabelPosition(RectangleAnchor.BOTTOM, TextBlockAnchor.BOTTOM_CENTER ), // TOP
      new CategoryLabelPosition( RectangleAnchor.TOP, TextBlockAnchor.TOP_CENTER ), // BOTTOM
      new CategoryLabelPosition( RectangleAnchor.LEFT, TextBlockAnchor.CENTER_LEFT, CategoryLabelWidthType.RANGE, 0.4f ), // LEFT
      new CategoryLabelPosition( RectangleAnchor.LEFT, TextBlockAnchor.CENTER_LEFT ) // RIGHT
    );

    categoryAxis.setCategoryLabelPositions( positions );
    categoryAxis.setLabelInsets( new RectangleInsets( 3, 3, 3, 3 ) );


    /*
    for(int i = 0; i < org_list.size(); i ++){
      GradientPaint gp = new GradientPaint(0.0f, 0.0f, DATA_FIELDS[i], 0.0f, 0.0f, DATA_FIELDS[i]);
      renderer.setSeriesPaint(i, gp);
    }
    */

    // OPTIONAL CUSTOMISATION COMPLETED.
    return chart;
  }

  /**
	* Writes a chart to an output stream in PDF format.
	* @param out The output stream.
	* @param chart The chart.
	* @param width The chart width.
	* @param height The chart height.
  */
  public void writeChartAsPDF(ByteArrayOutputStream out, JFreeChart chart, int width, int height,FontMapper mapper) throws IOException {
    Rectangle pagesize = new Rectangle(width,height);
    Document document = new Document(pagesize, 10, 10, 10, 10);
    try{
      PdfWriter writer = PdfWriter.getInstance(document,out);
/*
      HeaderFooter footer;
      if(isDisplayed) {
       footer = new HeaderFooter(new Phrase("", new Font(Font.TIMES_NEW_ROMAN,
       Font.DEFAULTSIZE, Font.N)), true);
      } else {
        footer = new HeaderFooter(new Phrase(""), false);
      }
      footer.setAlignment(HeaderFooter.ALIGN_CENTER);
      footer.setBorder(footer.NO_BORDER);
      document.resetFooter();
      document.setFooter(footer);
*/

      document.addAuthor("CMS");
      document.addSubject("Graph Report");
      document.open();
      PdfContentByte cb = writer.getDirectContent();
      PdfTemplate tp = cb.createTemplate(width,height);
      Graphics2D g2 = tp.createGraphics(width,height,mapper);
      Rectangle2D r2D = new Rectangle2D.Double(0,0,width,height);
      chart.draw(g2,r2D,null);
      g2.dispose();
      cb.addTemplate(tp,0,0);
	  document.close();
    } catch(DocumentException de) {
      de.printStackTrace();
	    System.err.println("A Document error:" +de.getMessage());
    }
    document.close();
  }


	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException
	{
		if (SessionState.getAttribute("GraphDql") != null && !SessionState.getAttribute("GraphDql").toString().trim().equals("")) {
			GraphDql = ((String) SessionState.getAttribute("GraphDql"));
		}
		org.jfree.text.TextUtilities.setUseDrawRotatedStringWorkaround(false);
		org_list = (ArrayList)SessionState.getAttribute("org_list");
		System.out.println("String = *" + GraphDql + "*");
		CategoryDataset dataset = createDataset();
		JFreeChart chart = createChart(dataset);
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		try {
			writeChartAsPDF(ba, chart, 500, 270, new DefaultFontMapper());
		} catch (IOException e) {
		System.out.println(e.toString());
    }
	  response.setContentType("application/pdf");
	  response.setContentLength(ba.size());
	  ServletOutputStream out = response.getOutputStream();
	  ba.writeTo(out);
	  out.flush();
	}












  private final Color [] DATA_FIELDS = {Color.blue, Color.cyan, Color.darkGray, Color.green, Color.lightGray, Color.magenta, Color.orange, Color.pink, Color.red, Color.yellow};
  public final String LINE_BREAK = System.getProperty("line.separator");
  private IDfSession m_dfSession = null;
  private String GraphDql = null;
  private ArrayList org_list = null;

}



