package com.dthielke.nnet.backprop;

import java.awt.GridLayout;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.dthielke.nnet.io.DataLoader;

public class Driver {
    
    // tree initialization parameters
    private static final double BIAS = 1;
    private static final double ABS_MIN_WEIGHT = 0.4;
    private static final double ABS_MAX_WEIGHT = 0.6;
    private static final int[] HIDDEN_NODES = { 2 }; // each element is the number of nodes in the layer
    
    // input data parameters
    private static final boolean NORMALIZE_INPUTS = false;
    private static final boolean NORMALIZE_TARGETS = false;
    private static final String TRAINING_FILE = "data/xor.data";
    
    // training parameters
    private static final double LEARNING_RATE = 0.25;
    private static final double MOMENTUM = 0.75;
    private static final double DECAY = 0.0;
    private static final int THREAD_SLEEP = 1; // sleep time in ms

    private static BackPropNetwork network; // the actual neural network
    private static BackPropNetworkPanel networkPanel; // the network's display
    private static int errorResolution = 5; // resolution of error calculations
    private static List<Double> errorBuffer = new LinkedList<Double>(); // temporary error storage
    private static XYSeries errors = new XYSeries("RMS Error"); // the actual errors to be displayed

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        // load the data
        DataLoader loader = new DataLoader();
        loader.load(TRAINING_FILE);
        double[][] inputs = NORMALIZE_INPUTS ? loader.getNormalizedInputs() : loader.getInputs();
        double[][] targets = NORMALIZE_TARGETS ? loader.getNormalizedTargets() : loader.getTargets();

        // create the network and the display
        int[] structure = new int[2 + HIDDEN_NODES.length];
        structure[0] = inputs[0].length; // input layer
        structure[structure.length - 1] = targets[0].length; // output layer
        for (int i = 0; i < HIDDEN_NODES.length; ++i) {
            structure[i + 1] = HIDDEN_NODES[i]; // hidden layer
        }
        network = new BackPropNetwork(structure, BIAS, ABS_MIN_WEIGHT, ABS_MAX_WEIGHT);
        createGUI();

        // run training indefinitely
        double error = 0;
        int errorCount = 0;
        while (true) {
            // select a random piece of training data
            int dataset = (int) (Math.random() * inputs.length);
            synchronized (network) {
                // train the network with this data
                network.train(inputs[dataset], targets[dataset], LEARNING_RATE, MOMENTUM, DECAY);
            }
            // calculate and store the error based on the past 'errorResolution' epochs
            if (errorCount == errorResolution) {
                synchronized (errorBuffer) {
                    errorBuffer.add(Math.sqrt(error / errorCount)); // SSE
                }
                error = 0;
                errorCount = 0;
            }
            error += Math.pow(network.getError(), 2);
            errorCount++;
            // sleep momentarily 
            Thread.sleep(THREAD_SLEEP);
        }
    }

    public static void createGUI() {
        // initialize the panel
        networkPanel = new BackPropNetworkPanel(network);

        // create the error chart
        XYSeriesCollection data = new XYSeriesCollection(errors);
        JFreeChart chart = ChartFactory.createXYLineChart("", "Epoch", "Error", data, PlotOrientation.VERTICAL, false, false, false);
        ChartPanel chartPanel = new ChartPanel(chart);
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);

        // set general frame settings and add the network panel and error chart
        JFrame frame = new JFrame("Backpropagation Neural Network");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new GridLayout(1, 2));
        frame.getContentPane().add(networkPanel);
        frame.getContentPane().add(chartPanel);
        frame.setSize(800, 600);
        frame.pack();
        frame.setVisible(true);

        // start GUI update routines in separate threads
        new Thread(new VisualizationUpdater()).start();
        new Thread(new GraphUpdater()).start();
    }

    static class VisualizationUpdater implements Runnable {

        @Override
        public void run() {
            while (true) {
                synchronized (network) {
                    networkPanel.repaint();
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ex) {
                    System.out.println(ex);
                }
            }
        }

    }

    static class GraphUpdater implements Runnable {

        private int epoch = 0;

        @Override
        public void run() {
            while (true) {
                synchronized (errorBuffer) {
                    errors.setNotify(false);
                    for (double error : errorBuffer) {
                        epoch += errorResolution;
                        errors.add(epoch, error);
                    }
                    errorBuffer.clear();
                    errors.setNotify(true);
                }
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ex) {
                    System.out.println(ex);
                }
            }
        }

    }

}

