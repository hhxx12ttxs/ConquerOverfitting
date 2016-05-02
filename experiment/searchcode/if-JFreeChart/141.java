package com.custom.library.reports.overdue;

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

import com.custom.library.reports.closed.ClosedStatisticalPDFReport;
import com.custom.library.utility.*;
import com.custom.library.worklog.Assignment;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.FontMapper;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.BaseFont;

public class GraphOverdueReport extends Component {

  public void onInit(ArgumentList arg) {
		super.onInit(arg);
		

		  
		  
    if (SessionState.getAttribute("GraphDql") != null && !SessionState.getAttribute("GraphDql").toString().trim().equals("")) {
      GraphDql = ((String) SessionState.getAttribute("GraphDql"));
    }
    org_list = (ArrayList)SessionState.getAttribute("org_list");
    System.out.println("String = *" + GraphDql + "*");
    CategoryDataset dataset = createDataset();
    JFreeChart chart = createChart(dataset);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
    // OutputStream out = new OutputStream();
    try {
      writeChartAsPDF(out, chart, 500, 270, new DefaultFontMapper());
    } catch (IOException e) {
      System.out.println(e.toString());
    }
  }


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
          Date date_overdue = col.getTime("overdue_date").getDate();
          Integer int_num = new Integer(col.getInt("overdue_num"));
          dataset.addValue(int_num, str_org, date_overdue);
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
    chart.setBackgroundPaint(Color.white);
    // get a reference to the plot for further customisation...
    CategoryPlot plot = chart.getCategoryPlot();
    plot.setBackgroundPaint(Color.lightGray);
    plot.setDomainGridlinePaint(Color.white);
    plot.setDomainGridlinesVisible(true);
    plot.setRangeGridlinePaint(Color.white);
    // set the range axis to display integers only...
    final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    // disable bar outlines...
    BarRenderer renderer = (BarRenderer) plot.getRenderer();
    renderer.setDrawBarOutline(false);
    for(int i = 0; i < org_list.size(); i ++){
      GradientPaint gp = new GradientPaint(0.0f, 0.0f, DATA_FIELDS[i], 0.0f, 0.0f, DATA_FIELDS[i]);
      renderer.setSeriesPaint(0, gp);
    }
    CategoryAxis domainAxis = plot.getDomainAxis();
    domainAxis.setCategoryLabelPositions(
        CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0)
    );
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
  public static void writeChartAsPDF(ByteArrayOutputStream out, JFreeChart chart, int width, int height,FontMapper mapper) throws IOException {
    Rectangle pagesize = new Rectangle(width,height);
    Document document = new Document(pagesize, 50, 50, 50, 50);
    try{ 
      PdfWriter writer = PdfWriter.getInstance(document,out);
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
    } catch(DocumentException de) {
      System.err.println(de.getMessage());
    }
    document.close(); 
  } 

  private static final Color [] DATA_FIELDS = {Color.blue, Color.cyan, Color.darkGray, Color.green, Color.lightGray, Color.magenta, Color.orange, Color.pink, Color.red, Color.yellow};
  public final String LINE_BREAK = System.getProperty("line.separator");
  private IDfSession m_dfSession = null;
  private String GraphDql = null;
  private ArrayList org_list = null;

}
