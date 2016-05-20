package smartant.gui;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * 
 * @author pasa
 * 
 */
public class ESRunsView extends JPanel {

	private static final long serialVersionUID = 1L;

	private final XYSeries resultSeries;
	private final XYSeries meanSeries;
	private final XYSeriesCollection dataset;
	private final JFreeChart chart;
	private final ValueAxis domainAxis;
	private int cnt;
	private double sum;

	public ESRunsView() {
		setLayout(new BorderLayout());
		resultSeries = new XYSeries("results");
		meanSeries = new XYSeries("mean");
		dataset = new XYSeriesCollection();
		dataset.addSeries(resultSeries);
		dataset.addSeries(meanSeries);
		chart = ChartFactory.createXYLineChart("Runs statistic", "Run number",
				"Best candidate", dataset, PlotOrientation.VERTICAL, true,
				false, false);
		domainAxis = chart.getXYPlot().getDomainAxis();
		domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		ChartPanel chartPanel = new ChartPanel(chart);
		add(chartPanel, BorderLayout.CENTER);
	}

	public void setMaxRuns(int maxRuns) {
		domainAxis.setRange(0, maxRuns);
	}

	public void addResult(final int runNumber, final double fitness)
			throws InterruptedException {

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					if (runNumber == 0) {
						resultSeries.clear();
						meanSeries.clear();
						cnt = 1;
						sum = fitness;
					}
					resultSeries.add(runNumber, fitness);
					cnt++;
					sum += fitness;
					meanSeries.add(runNumber, sum / cnt);
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}

