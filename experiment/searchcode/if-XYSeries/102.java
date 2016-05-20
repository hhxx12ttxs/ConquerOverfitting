package pl.edu.pw.fizyka.pojava.a3;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/*
 * PlotPanel:
 * Class plotting data, used 2 times (top and bottom plotPanel).
 * Uses JFreeChart.
 */

public class PlotPanel extends JPanel {

	private static final long       serialVersionUID        = 1L;
	private MainPanel mainPanel;
	long frameCounter;
	public enum QuantityType {NONE, VELOCITY_X, VELOCITY_Y, ENERGY, VOLTAGE, VELOCITY_MODULE, XPOS, YPOS};
	private QuantityType quantityType;
	private JFreeChart chart;
	private ChartPanel chartPanel;
	private XYSeries xyseries;
	private XYSeriesCollection seriesCollection;
	private Color color;

	public PlotPanel(MainPanel parent, QuantityType qt, Color c) {
		quantityType = qt;
		mainPanel = parent;
		color = c;

		this.setLayout(new GridLayout(1,1));
		createChart();
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

	}


	/*
	 * createPlotPanel:
	 * Creates chart and chartPanel, sets some visual attributes.
	 */
	 private void createChart() {
		xyseries = new XYSeries("seria");
		seriesCollection = new XYSeriesCollection(xyseries);
		chart = ChartFactory.createXYLineChart(getQuantityName(quantityType), "x", "y", seriesCollection,
				PlotOrientation.VERTICAL, false,false,false);

		chart.setBackgroundPaint(Color.WHITE);
		chart.getPlot().setBackgroundPaint(Color.WHITE);
		chartPanel = new ChartPanel(chart);
		XYPlot plot = (XYPlot) chart.getPlot();

		plot.getRangeAxis().setLabel("");

		chartPanel.setMinimumSize(new Dimension(5,80));
		chartPanel.setMaximumSize(new Dimension(660,222));

		plot.getDomainAxis().setLabel("");

		plot.getRenderer().setSeriesPaint(0, color);

		chart.setTitle(new TextTitle(getQuantityName(quantityType),
				new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12)));

		this.add(chartPanel);
	 }


	 /*
	  * addPoint:
	  * Adds new point to plot.
	  */
	 public void addPoint(Particle.Result result) {
		 frameCounter++;
		 double value=0;
		 if (quantityType == QuantityType.NONE) {
			 return;
		 } else if (quantityType==QuantityType.ENERGY) {
			 value=result.getEnergy();
		 } else if (quantityType==QuantityType.VELOCITY_MODULE) {
			 value=result.getVelocityValue();
		 } else if (quantityType==QuantityType.VELOCITY_X) {
			 value=result.getVX();
		 } else if (quantityType==QuantityType.VELOCITY_Y) {
			 value=result.getVY();
		 } else if (quantityType==QuantityType.VOLTAGE) {
			 value=result.getVoltage();
		 } else if (quantityType==QuantityType.XPOS) {
			 value=result.getX();
		 } else if (quantityType==QuantityType.YPOS) {
			 value=result.getY();
		 }
		 xyseries.add(result.getTime()*1e6,value);
	 }


	 /*
	  * getQuantityType:
	  * Get type of plotted quantity.
	  */
	 public QuantityType getQuantityType() {
		 return quantityType;
	 }


	 /*
	  * setQuantityType:
	  * Set type of plotted quantity.
	  */
	 public void setQuantityType(QuantityType qt) {
		 quantityType=qt;
		 newSettings();
		 mainPanel.repaint();

	 }


	 /*
	  * newSettings:
	  * Creates new chart series, called when changing quantity type.
	  */
	 public void newSettings() {

		 seriesCollection.removeAllSeries();

		 xyseries=new XYSeries("seria");
		 seriesCollection.addSeries(xyseries);

		 chart.setTitle(new TextTitle(getQuantityName(quantityType),
				 new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12)));
	 }


	 /*
	  * Returns a string which contains quantity name, which is put in chart title.
	  */
	 public String getQuantityName(QuantityType qt) {
		 if (quantityType==QuantityType.ENERGY) {
			 return mainPanel.getBundle().getString("energyPlot");
		 } else if (quantityType==QuantityType.VELOCITY_MODULE) {
			 return mainPanel.getBundle().getString("velValuePlot");
		 } else if (quantityType==QuantityType.VELOCITY_X) {
			 return mainPanel.getBundle().getString("xVelPlot");
		 } else if (quantityType==QuantityType.VELOCITY_Y) {
			 return mainPanel.getBundle().getString("yVelPlot");
		 } else if (quantityType==QuantityType.VOLTAGE) {
			 return mainPanel.getBundle().getString("voltPlot");
		 } else if (quantityType==QuantityType.XPOS) {
			 return mainPanel.getBundle().getString("xPosPlot");
		 } else if (quantityType==QuantityType.YPOS) {
			 return mainPanel.getBundle().getString("yPosPlot");
		 }
		 return "";
	 }

}
