package kasatkin.alex.ifmo.ru;

import java.awt.Color;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;

public class Chart extends ApplicationFrame {
	private static final long serialVersionUID = 1L;
	private int koeff;
	private int intr_size = 1;
	private XYSeriesCollection dataset;
	private ArrayList<Double>[] data;
	private String title;
	public static final int x = 1800;
	public static final int y = 800;

	public JFreeChart getChart() {
		return chart;
	}

	@SuppressWarnings("unchecked")
	public Chart(String title, String[] titles, ArrayList<Double>[] variables,
			int k) {
		super(title);
		this.title = title;
		koeff = k;
		// intr_size = Math.min((int)Math.round(intresting_size * 1.2),
		intr_size = 0;
		for (int i = 0; i < variables.length; i++) {
			intr_size = Math.min(intr_size, variables[i].size());
		}
		data = new ArrayList[variables.length];
		for (int i = 0; i < variables.length; i++) {
			data[i] = new ArrayList<Double>();
			for (int j = 0; j < variables[i].size(); j++) {
				if (variables[i].get(j) > 0) {
					data[i].add(variables[i].get(j));
				}
			}
		}
		createDataset(titles);
		XYDataset d = dataset;
		JFreeChart chart = createChart(d);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(x, y));
		setContentPane(chartPanel);
	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @return a sample dataset.
	 */
	private void createDataset(String[] titles) {
		XYSeries[] series = new XYSeries[data.length];
		for (int i = 0; i < data.length; i++) {
			series[i] = new XYSeries(titles[i]);
			for (int j = 0; j < data[i].size(); j++) {
				series[i].add(j * koeff, data[i].get(j));
			}
		}
		dataset = new XYSeriesCollection();
		for (int i = 0; i < data.length; i++) {
			dataset.addSeries(series[i]);
		}
	}

	private JFreeChart chart;

	private JFreeChart createChart(XYDataset dataset) {
		chart = ChartFactory.createXYLineChart(title, "Number of generations",
				"Fitness function", dataset, PlotOrientation.VERTICAL, true,
				true, false);
		chart.setBackgroundPaint(Color.white);

		XYPlot plot = (XYPlot) chart.getPlot();

		plot.setBackgroundPaint(Color.lightGray);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		// change the auto tick unit selection to integer units only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		// OPTIONAL CUSTOMISATION COMPLETED.
		return chart;
	}

}

