package com.wifislam.sample.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.util.ShapeUtilities;

import com.wifislam.sample.data.Coordinates;
import com.wifislam.sample.data.InputData;
import com.wifislam.sample.data.Rectangle2D;

/**
 * 
 * @author Giovanni Soldi
 * 
 */
@SuppressWarnings("serial")
public class ScatterPlot extends JFrame {
	private final XYSeriesCollection dataset;

	/**
	 * When this constructor is called a scatter plot is created
	 * 
	 * @param data
	 */
	public ScatterPlot(InputData data) {
		dataset = new XYSeriesCollection();
		XYSeries XY = new XYSeries("Guesses");
		final List<Coordinates> guesses = data.getCoordinates();
		for (Coordinates guess : guesses) {
			XY.add(guess.getX(), guess.getY());
		}
		dataset.addSeries(XY);
		showGraph();
	}

	/**
	 * This methods saved the specified chart into a specified image file.
	 * 
	 * @param file
	 *            the output image file
	 * @param chart
	 *            the chart to be saved
	 * @param width
	 *            the width of the image file
	 * @param height
	 *            the height of the image file
	 * @throws IOException
	 */
	public void saveChartToPngFile(File file, JFreeChart chart, int width,
			int height) throws IOException {
		ChartUtilities.saveChartAsPNG(file, chart, width, height);
	}

	/**
	 * 
	 */
	private void showGraph() {
		final JFreeChart chart = createChart(dataset);
		chart.getXYPlot();
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
		final ApplicationFrame frame = new ApplicationFrame(
				"Guesses & Estimated Position");

		final JPanel control = new JPanel();
		final JLabel label = new JLabel();
		control.add(new JButton(new AbstractAction("Estimate Position") {
			@Override
			public void actionPerformed(ActionEvent e) {
				final Coordinates estimatedPosition = getEstimatedPosition();
				label.setText("THE ESTIMATED POSITION IS: x = "
						+ estimatedPosition.getX() + ", " + "y = "
						+ estimatedPosition.getY());
				control.removeAll();
				control.add(label, BorderLayout.EAST);

			}
		}));
		control.setBackground(Color.WHITE);
		frame.add(chartPanel);
		frame.add(label, BorderLayout.SOUTH);
		frame.add(control, BorderLayout.PAGE_END);
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Retrieve the estimated position to plot into the chart
	 * 
	 * @return the estimated position
	 */
	private Coordinates getEstimatedPosition() {
		XYSeries XY2 = new XYSeries("Estimated position");
		XY2.add(Rectangle2D.getIntervalWithBiggerNumberOfPoints().meanX,
				Rectangle2D.getIntervalWithBiggerNumberOfPoints().meanY);
		if (!dataset.getSeries().contains(XY2))
			dataset.addSeries(XY2);
		return new Coordinates(Rectangle2D
				.getIntervalWithBiggerNumberOfPoints().meanX, Rectangle2D
				.getIntervalWithBiggerNumberOfPoints().meanY);
	}

	/**
	 * 
	 * @param dataset
	 * @return
	 */
	private JFreeChart createChart(final XYDataset dataset) {
		final JFreeChart chart = ChartFactory.createScatterPlot(
				"Guesses & Estimated Position", "X", "Y", dataset,
				PlotOrientation.VERTICAL, true, true, false);
		XYPlot plot = (XYPlot) chart.getPlot();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		Shape cross = ShapeUtilities.createDiamond(3);
		renderer.setSeriesLinesVisible(0, false);
		renderer.setSeriesShape(0, cross);
		renderer.setSeriesPaint(0, Color.BLUE);
		plot.setRenderer(renderer);
		return chart;
	}
}

