package multipar;

/* --------------------
* Function2DDemo1.java
* --------------------
* (C) Copyright 2007, by Object Refinery Limited.
*
*/


import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.function.Function2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;

/**
* An example showing how to plot a simple function in JFreeChart.  Because
* JFreeChart displays discrete data rather than plotting functions, you need
* to create a dataset by sampling the function values.
*/
public class Function2DDemo1 extends JFrame {
	
	public static double func;

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Creates a new demo.
     *
     * @param title  the frame title.
     */
    public Function2DDemo1(String title) {
        super(title);
        JPanel chartPanel = createDemoPanel();
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
    }
    
    /**
     * Creates a chart.
     * 
     * @param dataset  the dataset.
     * 
     * @return A chart instance.
     */
    private static JFreeChart createChart(XYDataset dataset) {
        // create the chart...
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Function2DDemo1 ",       // chart title
            "X",                      // x axis label
            "Y",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,  
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.getDomainAxis().setLowerMargin(0.0);
        plot.getDomainAxis().setUpperMargin(0.0);
        return chart;
    }
    
    /**
     * Creates a sample dataset.
     * 
     * @return A sample dataset.
     */
    public static XYDataset createDataset() {
        XYDataset result = DatasetUtilities.sampleFunction2D(new X2(),
                -100, 100, 100, "f(x)");
        return result;
    }

    /**
     * Creates a panel for the demo (used by SuperDemo.java).
     * 
     * @return A panel.
     */
    public static JPanel createDemoPanel() {
        JFreeChart chart = createChart(createDataset());
        return new ChartPanel(chart);
    }
    
    /**
     * A simple function.
     */
    static class X2 implements Function2D {

        /* (non-Javadoc)
         * @see org.jfree.data.function.Function2D#getValue(double)
         */
    	// Build a function or get from the input
        public double getValue(double x) {
            return func;
        }
    }
    
    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    public static void main(String[] args) {
    	
    	System.out.println(args[0]);
    	
    	if(args.length > 0){
    		
    			 try {
					func = Double.parseDouble(args[0]);
				} catch (NumberFormatException e) {
					System.out.println("Could not parse.");
					e.printStackTrace();
				}
           
        }else{
            System.out.println("No input...");
            return;
        }
    	
        Function2DDemo1 demo = new Function2DDemo1(
                "JFreeChart: Function2DDemo1.java");
        demo.pack();
       // RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }

}
