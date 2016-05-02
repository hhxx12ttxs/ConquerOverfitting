package org.rainfall.gui.jfreechart;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.rainfall.util.Date;

/**
 * A helper class to wrap some of the complexity of creating a Bar Chart using JFreeChart.
 * Hides the necessary imports and the conversion of data into the required Point2D.
 *
 * @author Kieran
 */
public class BarChart extends Graph {

    private Point2D.Float[] data;
    private String title;
    private String xLabel;
    private String yLabel;
    private JFreeChart chart;

    /**
     * Create a Bar Chart of the given data (uses JFreeChart classes and methods).
     *
     * @param title The title for the scatter plot
     * @param xLabel The label for the x axis of the scatter plot
     * @param yLabel The label for the x axis of the scatter plot
     * @param data The actual data in ArrayList format
     */
    public BarChart(String title, String xLabel, String yLabel, ArrayList<Float> data, int monthPivot) {
        super();

        this.title = title;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        this.data = convertDataSet(data, monthPivot);

        BarChart();
        super.createFrame(chart);
    }

    /**
     * Private method to actually do the work of creating the bar chart.
     */
    private void BarChart() {
        // convert data into required format for JFreeChart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i=0; i< data.length; i++) {
            if (data[i] != null) {
                dataset.setValue(data[i].y, yLabel, Date.monthToName(Math.round(data[i].x)));
            }
        }

        chart = ChartFactory.createBarChart (
                        title, xLabel, yLabel, dataset,	// main parameters
                        PlotOrientation.VERTICAL,			// orientation
                        true, false, false );				// show legend, but no tooltips or urls
    }

}

