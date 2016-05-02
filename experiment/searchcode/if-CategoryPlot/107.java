package dataPresentation;

import Servlets.DBConn;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * A simple demonstration application showing how to create a line chart using data from a
 * {@link CategoryDataset}.
 */
public class doubleLineChart extends ApplicationFrame {

    File filename_png;
    //   private final ChartPanel chartPanel;
    JSONObject tmp;
    //Na koitakso ki auto http://www.java2s.com/Code/Java/Chart/Chart2DLineChartWithFilledStack.htm
    Hashtable<String, int[]> table;
    Vector keys;
    int[] values;
    JSONArray dataArray, dataObject;
    ChartPanel chartPanel;

    /**
     * Creates a new demo.
     *
     * @param title  the frame title.
     */
    public doubleLineChart(String obj) {
        super("");

        table = new Hashtable<String, int[]>();
        keys = new Vector();
        values = new int[2];
        tmp = new JSONObject();
        JSONParser parser = new JSONParser();

        try {
            dataObject = (JSONArray) parser.parse(obj);

            for (int i = 0; i < dataObject.size(); i++) {
                tmp = (JSONObject) dataObject.get(i);
                values[0] = Integer.valueOf(tmp.get("Multimedia").toString());
                values[1] = Integer.valueOf(tmp.get("Software").toString());
                keys.add(tmp.get("Semester"));
                System.out.println("added" + keys.elementAt(i) + " " + values[0] + " " + values[1]);
                table.put(keys.elementAt(i).toString(), values);
            }
        } catch (ParseException ex) {
            Logger.getLogger(doubleLineChart.class.getName()).log(Level.SEVERE, null, ex);
        }

        CategoryDataset dataset = createDataset(obj);
        JFreeChart chart = createChart(dataset);
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        setContentPane(chartPanel);

    }

    /**
     * Creates a sample dataset.
     *
     * @return The dataset.
     */
    private CategoryDataset createDataset(String js) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();


        System.out.println("-------------------------");

        JSONParser parser = new JSONParser();
        try {
            dataObject = (JSONArray) parser.parse(js);

            for (int i = 0; i < dataObject.size(); i++) {
                tmp = (JSONObject) dataObject.get(i);
                values[0] = Integer.valueOf(tmp.get("Multimedia").toString());
                values[1] = Integer.valueOf(tmp.get("Software").toString());
                keys.add(tmp.get("Semester"));

                dataset.addValue(Double.parseDouble(String.valueOf(values[0])), "Multimedia", keys.elementAt(i).toString());
                dataset.addValue(Double.parseDouble(String.valueOf(values[1])), "Software", keys.elementAt(i).toString());
                table.put(keys.elementAt(i).toString(), values);

            }
        } catch (ParseException ex) {
            Logger.getLogger(doubleLineChart.class.getName()).log(Level.SEVERE, null, ex);
        }


        return dataset;

    }

    /**
     * Creates a sample chart.
     *
     * @param dataset  a dataset.
     *
     * @return The chart.
     */
    private JFreeChart createChart(final CategoryDataset dataset) {

        // create the chart...
        final JFreeChart chart = ChartFactory.createLineChart(
                "", // chart title
                "", // domain axis label
                "", // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips
                false // urls
                );


        chart.setBackgroundPaint(Color.white);

        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.white);

        final CategoryAxis axis = plot.getDomainAxis();
        axis.setCategoryLabelPositions(
                CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 8.0));
       try {
            URL url = DBConn.class.getResource("DBConn.class");
            File parent = new File(url.getFile());
            File gparent = new File(parent.getParent());
            parent = new File(gparent.getParent());
            gparent = new File(parent.getParent());
            parent = new File(gparent.getParent());
            gparent = new File(parent.getParent());
            parent = new File(gparent.getParent());


            String path = parent.getPath();
            //Diavaste edw!
            System.out.println(path.toString());
            
            filename_png = new File(path + "\\web\\img\\graphs\\doubleLineChart.png");
            System.out.println(filename_png.toURI().toString());
            System.out.println(path);
              if (filename_png != null) {
                filename_png.delete();
            }
            System.out.println(filename_png.toString());


          
            ChartUtilities.saveChartAsPNG(filename_png, chart, 800, 600);
            
            System.out.println("Img saved!");

        } catch (IOException ex) {
            Logger.getLogger(doubleLineChart.class.getName()).log(Level.SEVERE, null, ex);
        }
        return chart;
    }

    public String getIconURL() {


         return filename_png.getName();
    }
}

