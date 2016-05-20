package dulw;

import java.awt.LayoutManager;

import javax.swing.JPanel;

import org.apache.commons.math.FunctionEvaluationException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ChartComponent extends JPanel {

	private static final long serialVersionUID = 1L;

	/**
	 * @param layout
	 *            if null layout stay the same
	 * @param series
	 * @throws FunctionEvaluationException
	 */
	public void changeChartForLinear(LayoutManager layout, XYSeriesCollection dataset,
			SignalType type) throws FunctionEvaluationException {
		if (layout != null) {
			this.setLayout(layout);
		}
		JFreeChart chart = null;
		if (type == SignalType.LINEAR || type == SignalType.NOISE) {
			chart = ChartFactory.createXYLineChart("", "", "", dataset,
					PlotOrientation.VERTICAL, false, false, false);
		} else if (type == SignalType.NONLINEAR) {
			chart = ChartFactory.createScatterPlot("", "", "", dataset,
					PlotOrientation.VERTICAL, false, false, false);
		} else {
			throw new IllegalArgumentException();
		}

		ChartPanel panel = new ChartPanel(chart);
		this.removeAll();
		this.add(panel);
		this.validate();
	}
//	/**
//	 * @param layout
//	 *            if null layout stay the same
//	 * @param series
//	 * @throws FunctionEvaluationException
//	 */
//	public void changeChartForPoints(LayoutManager layout, XYSeriesCollection dataset) throws FunctionEvaluationException {
//		if (layout != null) {
//			this.setLayout(layout);
//		}
//		JFreeChart chart = null;
//			chart = ChartFactory.createScatterPlot("", "", "", dataset,
//					PlotOrientation.VERTICAL, false, false, false);
//
//		ChartPanel panel = new ChartPanel(chart);
//		this.removeAll();
//		this.add(panel);
//		this.validate();
//	}

	public void addHistogram(HistogramDataset dataset) {
		JFreeChart chart = ChartFactory.createHistogram("", "", "", dataset,
				PlotOrientation.VERTICAL, false, false, false);
		ChartPanel panel = new ChartPanel(chart);
		this.add(panel);
		this.validate();
	}

//	public XYSeries changeChart(LayoutManager layout, XYSeries xySeries,
//			SignalType signalType) throws FunctionEvaluationException {
//		if (layout != null) {
//			this.setLayout(layout);
//		}
//		XYSeriesCollection dataset = null;
//		JFreeChart chart = null;
//		dataset = new XYSeriesCollection(xySeries);
//		if (signalType == SignalType.IMPULSE) {
//			chart = ChartFactory.createXYLineChart("", "", "", dataset,
//					PlotOrientation.VERTICAL, false, false, false);
//
//		} else {
//			chart = ChartFactory.createScatterPlot("", "", "", dataset,
//					PlotOrientation.VERTICAL, false, false, false);
//		}
//
//		ChartPanel panel = new ChartPanel(chart);
//		this.removeAll();
//		this.add(panel);
//		this.validate();
//		return xySeries;
//	}

}

