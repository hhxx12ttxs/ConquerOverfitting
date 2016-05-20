/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataPresentation;

import Servlets.DBConn;
import java.awt.Font;
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
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.ApplicationFrame;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author epp2115
 */
public class SectorPie extends ApplicationFrame {

    /**
     * Default constructor.
     *
     * @param title  the frame title.
     */
    Hashtable<String, int[]> numbers;
    int total = 0;
    File filename_png;
    //   private final ChartPanel chartPanel;
    JSONObject tmp;
    //Na koitakso ki auto http://www.java2s.com/Code/Java/Chart/Chart2DLineChartWithFilledStack.htm
    Hashtable<String, int[]> table;
    Vector keys;
    int[] values;
    JSONArray dataArray, dataObject;
    ChartPanel chartPanel;
    JSONParser parser;
    private URL url;

    public SectorPie(String js) {
        super("");

        // create a dataset...
        final PieDataset dataset = createDataset(js);

        // create the chart...
        final JFreeChart chart = createChart(dataset);

        // add the chart to a panel...
        chartPanel = new ChartPanel(chart);
    }

    /**
     * Creates a sample dataset.
     *
     * @return A sample dataset.
     */
    private PieDataset createDataset(String js) {

        DefaultPieDataset dataset = new DefaultPieDataset();
        parser = new JSONParser();
        try {
            dataObject = (JSONArray) parser.parse(js);
            for (int i = 0; i < dataObject.size(); i++) {
                tmp = (JSONObject) dataObject.get(i);

                dataset.setValue(tmp.get("Name") + "\n " + tmp.get("value") + " % ", Double.valueOf(String.valueOf(tmp.get("thesis"))));
            }


        } catch (org.json.simple.parser.ParseException ex) {
            Logger.getLogger(SectorPie.class.getName()).log(Level.SEVERE, null, ex);
        }




        return dataset;

    }

    /**
     * Creates a chart.
     *
     * @param dataset  the dataset.
     *
     * @return A chart.
     */
    private JFreeChart createChart(PieDataset dataset) {

        JFreeChart chart = ChartFactory.createPieChart(
                "", // chart title
                dataset, // data
                true, // include legend
                true,
                false);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.setNoDataMessage("No data available");
        plot.setCircular(false);
        plot.setLabelGap(0.02);


        try {
            url = DBConn.class.getResource("DBConn.class");
            File parent = new File(url.getFile());
            File gparent = new File(parent.getParent());
            parent = new File(gparent.getParent());
            gparent = new File(parent.getParent());
            parent = new File(gparent.getParent());
            gparent = new File(parent.getParent());
            parent = new File(gparent.getParent());


            String path = parent.getPath();
  if (filename_png != null) {
                filename_png.delete();
            }
            filename_png = new File(path + "/img/graphs/simplePie.png");



            ChartUtilities.saveChartAsPNG(filename_png, chart, 800, 600);

        } catch (IOException ex) {
            Logger.getLogger(doubleLineChart.class.getName()).log(Level.SEVERE, null, ex);
        }
        return chart;

    }

    /**
     * Creates a panel for the demo (used by SuperDemo.java).
     *
     * @return A panel.
     */
    public String getIconURL() {

        return filename_png.getName();
    }
    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
}

