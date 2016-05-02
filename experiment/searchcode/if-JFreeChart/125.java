package org.rainfall.gui.jfreechart;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.*;
import org.rainfall.lang.Output;

/**
 * Creates the default interface for any JFreeChart that may be produced (whether
 * this be a bar chart, pie chart or line graph they're all mapped to this interface
 * design.)
 *
 * @author Kieran
 */
public class Graph extends JFrame implements ActionListener {

    private JButton EXPORT_GRAPH = new JButton("Export Graph");
    private JFreeChart chart;

    public Graph() {
        
    }

    /**
     * Create a Bar Chart of the given data (uses JFreeChart classes and methods).
     */
    public void createFrame(JFreeChart chart) {
        // set chart (so we can access from this class)
        this.chart = chart;

        // set up the GUI to display the chart
        this.setSize(500,270);

        JPanel pnl = new JPanel(new BorderLayout());
        ChartPanel chartPanel = new ChartPanel(chart);
        pnl.add(chartPanel, BorderLayout.NORTH);
        EXPORT_GRAPH.addActionListener(this);
        pnl.add(EXPORT_GRAPH, BorderLayout.CENTER);
        this.setContentPane( pnl );

        this.pack();
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }

    /**
     * Converts an ArrayList of floats to an array of Point2D.Floats, which is the
     * required data set to produced a graph using JFreeChart.
     * @param data ArrayList of floats consisting of rainfall data for a given period.
     * @return Point2D.Float array of the converted data set.
     */
    public Point2D.Float[] convertDataSet(ArrayList<Float> data, int monthPivot) {
        Point2D.Float[] dataPoints = new Point2D.Float[61];

        for (int i = 0; i < data.size(); i++) {
            dataPoints[i] = new Point2D.Float(i+1, data.get(i));
        }

        return dataPoints;
    }

    /**
     * Listens for actions on specific GUI components
     * @param e ActionEvent for a given user interaction
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == EXPORT_GRAPH) {
            saveFileDialogBox();
        }
    }

    private void saveImage(File file) {
        try {
            ChartUtilities.saveChartAsPNG(file, chart, 980, 550);
        } catch (Exception e) {
            Output.errorToGUI("Unable to save file!");
        }
    }

    private void saveFileDialogBox() {
        JFileChooser fc = new JFileChooser();
        int saveDialog = fc.showSaveDialog(fc);

        if (saveDialog == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            saveImage(file);
        }
    }

}


