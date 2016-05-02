package uk.ac.cam.ch.ami.gui.tabs;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import uk.ac.cam.ch.ami.IconFactory;
import uk.ac.cam.ch.ami.events.UpdateStatusBarEvent;
import uk.ac.cam.ch.ami.gui.NumericTextField;
import uk.ac.cam.ch.ami.listeners.UpdateStatusBarListener;
import uk.ac.cam.ch.ami.tablelayout.TableLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

import static org.jfree.chart.ChartFactory.createXYLineChart;

/**
 * Creates a plot of data from a SensorInput
 * <p>
 * Created by IntelliJ IDEA.
 * User: Matthew
 * Date: 01-Sep-2010
 * Time: 13:17:19
 */
public class SensorPlotTab extends JPanel implements ActionListener, KeyListener {

    XYPlot sensorXYPlot;

    public void init(){

        this.setLayout(new GridLayout());
        this.add(mainPanel);

        // set a default value
        txtSetNSecsToView.setText("60");

        //add individual series to a collection of dataseries
        fullSensorSeriesCollection.addSeries(fullSensorLogSeries);
        fullSensorSeriesCollection.addSeries(fullMaxSensorValueSeries);
        fullSensorSeriesCollection.addSeries(fullMinSensorValueSeries);

        addKeyListener(this);
        setupButtonListeners();

        //create the chart object
        sensorChart = createXYLineChart("Graph of " + graphTitle, //title
                "Time",                                         //x-axis label
                graphTitle,                                     //y-axis label
                fullSensorSeriesCollection,                     //colleciton of data series to plot
                PlotOrientation.VERTICAL,                       //orientation
                true,                                           //include legend?
                true,                                           //tooltips?
                false);                                         //urls?

        chartPanel.setChart(sensorChart);
        chartPanel.setMouseZoomable(true);
        sensorXYPlot = sensorChart.getXYPlot();

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.black);    //set the dataseries line to black
        renderer.setSeriesPaint(1, Color.red);      //max line: red
        renderer.setSeriesPaint(2, Color.blue);     //min line: blue
        for(int i = 0; i < fullSensorSeriesCollection.getSeriesCount(); i++){
            renderer.setSeriesShapesVisible(i, false);      //makes the points invisible
        }

        sensorXYPlot.setRenderer(renderer);

        //create toggle buttons and add to a panel
        JPanel warningPanel = new JPanel();

        mainPanel.setLayout(new BorderLayout());

        warningPanel.add(warningLabel);
        viewFullSeriesToggleButton.setIcon(IconFactory.FULL_DATASERIES_ICON) ;
        viewLastNSecondsToggleButton.setIcon(IconFactory.TIME_DATASERIES_ICON);

        //grouping the radio buttons
        ButtonGroup group = new ButtonGroup();
        group.add(viewFullSeriesToggleButton);
        group.add(viewLastNSecondsToggleButton);


        //add the chart panel and the radio button panel to a parent panel
        mainPanel.add(warningPanel, BorderLayout.NORTH);
        mainPanel.add(chartPanel);
        mainPanel.add(buildToggleButtonPanel(), BorderLayout.SOUTH);
        //this panel is then added to the appropriate tag by the experiment screen

    }

    private void setupButtonListeners() {
        viewFullSeriesToggleButton.addActionListener(this);
        viewLastNSecondsToggleButton.addActionListener(this);
    }

    private JPanel buildToggleButtonPanel(){
        //mess around with this to move the toggle buttons around on the screen
        JPanel toggleButtonPanel = new JPanel();
        // 4 equally-sized columns; first and last are blank, middle two will have
        // the buttons added later in this method
        double layout[][] = {{.25, .25, .25, .25},{TableLayout.FILL}};
        toggleButtonPanel.setLayout(new TableLayout(layout));
        JPanel timeP = new JPanel();

        Font font = new Font("Serif", Font.BOLD, 14);

        txtSetNSecsToView.setHorizontalAlignment(JTextField.CENTER);
        txtSetNSecsToView.setFont(font);

        timeP.setLayout(new BorderLayout());
        timeP.add(viewLastNSecondsToggleButton, BorderLayout.NORTH);
        timeP.add(txtSetNSecsToView, BorderLayout.SOUTH);

        toggleButtonPanel.add(timeP, "1, 0");
        toggleButtonPanel.add(viewFullSeriesToggleButton, "2,0");

        viewLastNSecondsToggleButton.setMnemonic(KeyEvent.VK_N);
        viewFullSeriesToggleButton.setMnemonic(KeyEvent.VK_F);

        return toggleButtonPanel;
    }

    public void rescaleAxis(double t){

        ValueAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time (seconds)");

        if(timeViewMode){
            Double userSetLength = txtSetNSecsToView.getNumber();
            //set the lower bound of the graph to the time since the graph started/1000 - number in the text box
            Double lowerBound;
            lowerBound = (t - userSetLength);
            if(lowerBound <= 0){
                lowerBound = 0.0;
            }
            else{
                lowerBound = (t - userSetLength);
            }
            xAxis.setLowerBound(lowerBound);
            xAxis.setUpperBound(t);
        }
        else{
            xAxis.setLowerBound(0);
            xAxis.setUpperBound(t);
        }

        sensorXYPlot.setDomainAxis(xAxis);
    }

    public SensorPlotTab(String name) {
        this.graphTitle = name;
        init();
    }

    public void update(Long sensorLogStartTime, String sensorReading, int maxTemp, int minTemp){
        double timeInSecs = (System.currentTimeMillis() - sensorLogStartTime)/1000;
        fullSensorLogSeries.add(timeInSecs, Double.parseDouble(sensorReading));
        fullMaxSensorValueSeries.add(timeInSecs, maxTemp);
        fullMinSensorValueSeries.add(timeInSecs, minTemp);
        rescaleAxis(timeInSecs);
    }

    public void update(Long sensorLogStartTime, String sensorReading){
        double timeInSecs = (System.currentTimeMillis() - sensorLogStartTime)/1000;
        fullSensorLogSeries.add(timeInSecs, Double.parseDouble(sensorReading));
        rescaleAxis(timeInSecs);
    }

    /**
     * Remove the data series which indicate the min/max values from the plot
     */
    public void removeMaxMinSeries(){
        fullSensorSeriesCollection.removeSeries(fullMaxSensorValueSeries);
        fullSensorSeriesCollection.removeSeries(fullMinSensorValueSeries);
    }

    private JPanel mainPanel = new JPanel();

    private XYSeries fullSensorLogSeries = new XYSeries("Sensor reading VS Time");
    private XYSeries fullMaxSensorValueSeries = new XYSeries("Maximum Limit");
    private XYSeries fullMinSensorValueSeries = new XYSeries("Minimum Limit");
    private XYSeriesCollection fullSensorSeriesCollection = new XYSeriesCollection();

    private JFreeChart sensorChart;
    private ChartPanel chartPanel = new ChartPanel(sensorChart);

    private JToggleButton viewFullSeriesToggleButton = new JToggleButton("View full plot");
    private JToggleButton viewLastNSecondsToggleButton = new JToggleButton("<html><p>View last <i>n</i> seconds</p></html>");

    private NumericTextField txtSetNSecsToView = new NumericTextField();
    private JLabel warningLabel = new JLabel("");

    private boolean timeViewMode = false;

    private String graphTitle;

    public String getGraphTitle() {
        return graphTitle;
    }

    /**
     * Display a warning message in the panel if the sensor reading goes outside the set min/max values
     * @param text The warning message to display
     */
    public void setWarning(String text){
        this.warningLabel.setText(text);
    }

    /**
     * Set whether the entire timeseries should be displayed, or the last N seconds (with N set by the text input box on
     * the form)
     * @param timeViewMode True if the last N seconds is to be displayed, false if the entire series is to be displayed.
     */
    public void setTimeViewMode(boolean timeViewMode) {
        this.timeViewMode = timeViewMode;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if(this.viewFullSeriesToggleButton == source) {
            this.setTimeViewMode(false);
            fireUpdateStatusBarEvent(new UpdateStatusBarEvent(this, "Showing full plot"));
        } else if (this.viewLastNSecondsToggleButton == source) {
            Double nSeconds = txtSetNSecsToView.getNumber();
            fireUpdateStatusBarEvent(new UpdateStatusBarEvent(this, "Showing last "
                    + nSeconds + " seconds"));
            this.setTimeViewMode(true);
        }

    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int id = e.getKeyCode();
        if(e.isAltDown()) {
            if(id == KeyEvent.VK_T) {
                txtSetNSecsToView.requestFocus();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public void saveChartAsPng(File filename) {
        try {
            ChartUtilities.saveChartAsPNG(filename, this.sensorChart, 1024, 600);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
        public void fireUpdateStatusBarEvent(UpdateStatusBarEvent event) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == UpdateStatusBarListener.class) {
                ((UpdateStatusBarListener) listeners[i + 1]).processUpdateStatusBar(event);
            }
        }
    }

    public void addUpdateStatusBarListener(UpdateStatusBarListener listener) {
        listenerList.add(UpdateStatusBarListener.class, listener);
    }

    public void removeUpdateStatusBarListener(UpdateStatusBarListener listener) {
        listenerList.remove(UpdateStatusBarListener.class, listener);
    }

}

