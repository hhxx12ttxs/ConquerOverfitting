package com.galapagos.workbench;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.galapagos.RunnableIsland;
import com.galapagos.StepEvent;
import com.galapagos.StepEventListener;
import com.galapagos.StepInfo;

public class FitnessChart<T> extends JPanel implements StepEventListener<T> {

	private static final long	serialVersionUID	= 1L;
	private JFreeChart			chart;
	private ChartPanel			chartPanel;
	private IntervalXYDataset	dataset;
	private RunnableIsland<T>	island;
	private XYSeries			minSeries, maxSeries, avgSeries;

	public FitnessChart() {
		this.setMinimumSize(new Dimension(500, 500));
		this.setLayout(new BorderLayout());
		this.add(getChartPanel(), BorderLayout.CENTER);
	}

	public RunnableIsland<T> getIsland() {
		return this.island;
	}

	@Override
	public void onStepEvent(final StepEvent<T> evt) {
		if (SwingUtilities.isEventDispatchThread()) {
			addStep(evt.getInfo());
		} else {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					addStep(evt.getInfo());
				}
			});
		}
	}

	public void setIsland(RunnableIsland<T> island) {
		this.island = island;
		island.addStepEventListener(this);
	}

	private void addStep(StepInfo info) {
		long stepNo = info.stepNumber();
		getMinSeries().add(stepNo, info.minFitness());
		getMaxSeries().add(stepNo, info.maxFitness());
		getAvgSeries().add(stepNo, info.meanFitness());
	}

	private XYSeries getAvgSeries() {
		if (avgSeries == null) {
			avgSeries = new XYSeries("Average");
		}
		return avgSeries;
	}

	private JFreeChart getChart() {
		if (chart == null) {
			chart = ChartFactory.createScatterPlot("Fitness/Step", "Step", "Fitness", getDataSet(),
					PlotOrientation.VERTICAL, true, true, false);
		}
		return chart;
	}

	private ChartPanel getChartPanel() {
		if (chartPanel == null) {
			chartPanel = new ChartPanel(getChart());
		}
		return chartPanel;
	}

	private IntervalXYDataset getDataSet() {
		if (dataset == null) {
			dataset = new XYSeriesCollection();
			((XYSeriesCollection) dataset).addSeries(getMinSeries());
			((XYSeriesCollection) dataset).addSeries(getMaxSeries());
			((XYSeriesCollection) dataset).addSeries(getAvgSeries());
		}
		return dataset;
	}

	private XYSeries getMaxSeries() {
		if (maxSeries == null) {
			maxSeries = new XYSeries("Maximum");
		}
		return maxSeries;
	}

	private XYSeries getMinSeries() {
		if (minSeries == null) {
			minSeries = new XYSeries("Minimum");
		}
		return minSeries;
	}
}

