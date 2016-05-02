


package com.custom.library.reports;


import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import com.documentum.web.common.*;
import com.documentum.web.form.*;
import com.documentum.web.form.control.*;
import com.documentum.web.form.control.databound.*;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.component.*;
import com.documentum.fc.client.*;
import com.documentum.fc.common.*;
import com.documentum.web.formext.session.SessionManagerHttpBinding;
import java.util.*;
import java.text.*;
import java.io.*;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import com.custom.library.utility.*;
import com.custom.library.worklog.Assignment;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;

import org.jfree.chart.block.AbstractBlock;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.axis.*;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.*;
import org.jfree.text.TextBlockAnchor ;

import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.FontMapper;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.BaseFont;

public class PrintPDF_iText extends HttpServlet {

  private CategoryDataset createDataset() {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    IDfSessionManager idfsessionmanager = SessionManagerHttpBinding.getSessionManager();
    try {
      m_dfSession = idfsessionmanager.getSession(SessionManagerHttpBinding.getCurrentDocbase());
      IDfCollection col = null;
      IDfQuery q = new DfQuery();
      q.setDQL(GraphDql);
      try {
        col = q.execute(m_dfSession, DfQuery.DF_READ_QUERY);
        while (col.next()) {
          // org_name  overdue_num  overdue_date
          String str_org = col.getString("org_name");
          String date_overdue = col.getTime("overdue_date").asString("mm/dd/yyyy");
          dataset.addValue(new Integer(col.getInt("overdue_num")), str_org.trim(), date_overdue);
//System.out.println("dataset.addValue(new Integer("+col.getInt("overdue_num") + "), " + str_org.trim() + ", " + date_overdue + ");");          
        }
      } catch (DfException e) {
        System.out.println(e.toString());
      } finally {
        if(col != null)
          col.close();
      }
    } catch(DfException dfexception) {
      throw new WrapperRuntimeException("Failed to set connection in createDataset()", dfexception);
    } finally {
      if (m_dfSession != null)
        idfsessionmanager.release(m_dfSession);
    }
    return dataset;
  }
  
  private JFreeChart createChart(CategoryDataset dataset) {
    // create the chart...
    JFreeChart chart = ChartFactory.createBarChart(
        "Overdue Bar Chart",         // chart title
        "Week Begining Date",               // domain axis label
        "Total Documents",                  // range axis label
        dataset,                  // data
        PlotOrientation.VERTICAL, // orientation
        true,                     // include legend
        true,                     // tooltips?
        false                     // URLs?
    );
    LegendTitle sl = (LegendTitle) chart.getLegend();
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

  
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
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



