/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package explorer.chart;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.Rotation;
import explorer.util.Util;
import java.util.Iterator;
import java.util.Map;

public class PieChart extends JFrame {

    private static final long serialVersionUID = 1L;
    
    public PieChart(String applicationTitle, String chartTitle) {
        super(applicationTitle);
        // This will create the dataset 
        PieDataset dataset = createDataset(applicationTitle);
        // based on the dataset we create the chart
        JFreeChart chart = createChart(dataset, chartTitle);
        // we put the chart into a panel
        ChartPanel chartPanel = new ChartPanel(chart);
        // default size
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        // add it to our application
        setContentPane(chartPanel);
        this.pack();
    }

    /**
     * Creates a sample dataset
     */
    private PieDataset createDataset(String path) {
        DefaultPieDataset result = new DefaultPieDataset();
        Util.scanTree(path);
        Map<String, Integer> ls = Util.compressList();
        for (Map.Entry m : ls.entrySet()) {
            if((Integer) m.getValue() != 0)
                result.setValue((String) m.getKey(), (Integer) m.getValue());
        }
        return result;
    }

    /**
     * Creates a chart
     */
    private JFreeChart createChart(PieDataset dataset, String title) {

        JFreeChart chart = ChartFactory.createPieChart3D(title, // chart title
                dataset, // data
                true, // include legend
                true,
                false);

        PiePlot3D plot = (PiePlot3D) chart.getPlot();
        plot.setStartAngle(290);
        plot.setDirection(Rotation.CLOCKWISE);
        plot.setForegroundAlpha(0.5f);
        return chart;

    }

    public static void main(String[] args) {
        PieChart demo = new PieChart("Comparison", "Which operating system are you using?");
        demo.pack();
        demo.setVisible(true);
    }
}

