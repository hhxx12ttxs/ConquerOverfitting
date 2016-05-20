package gui;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JDialog;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.IntervalCategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.gantt.GanttCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

public class GanttCharts extends JDialog { //ApplicationFrame {

	private static final long serialVersionUID = 1L;
	private static int counter;
	private static int[] permutation;

	public GanttCharts(String title, int[] permutation, int[][] times,
			int[][] endTimes, Frame frame) {
		super(frame, title);
		GanttCharts.permutation = permutation;
		ChartPanel chartPanel = new ChartPanel(
				createChart(title, createDataset(permutation, times, endTimes),
						permutation.length));
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
		chartPanel.setMouseZoomable(true, false);
		setContentPane(chartPanel);
		pack();
		setVisible(true);
	}

	private static JFreeChart createChart(String title,
			GanttCategoryDataset dataset, final int tasks) {

		JFreeChart chart = ChartFactory.createGanttChart(title, "Maszyny",
				"Czas", dataset, false, true, false);

		chart.setBackgroundPaint(Color.white);

		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.lightGray);

		final List<Color> colors = new ArrayList<Color>();
		for (int i = 0; i < tasks; i++) {
			colors.add(Color.getHSBColor(i / (float) tasks, 1, 1));
		}

		GanttRenderer gr = new GanttRenderer() {
			private static final long serialVersionUID = 1L;

			private int index = 0;

			@Override
			public Paint getItemPaint(int row, int col) {
				Color c = colors.get(index++ % tasks);
				return c;
			}

			@Override
			protected void drawTasks(Graphics2D g2,
					CategoryItemRendererState state, Rectangle2D dataArea,
					CategoryPlot plot, CategoryAxis domainAxis,
					ValueAxis rangeAxis, GanttCategoryDataset dataset, int row,
					int column) {

				for (int i = 0; i < tasks; i++) {
					RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();

					Number start = dataset.getStartValue(row, column, i);
					double translatedStart = rangeAxis.valueToJava2D(
							start.doubleValue(), dataArea, rangeAxisLocation);

					Number end = dataset.getEndValue(row, column, i);
					double translatedEnd = rangeAxis.valueToJava2D(
							end.doubleValue(), dataArea, rangeAxisLocation);

					double width = state.getBarWidth() / 2;
					double rectStart = calculateBarW0(plot,
							plot.getOrientation(), dataArea, domainAxis, state,
							row, column) + width / 2;
					double height = Math.abs(translatedEnd - translatedStart);

					Rectangle2D bar = new Rectangle2D.Double(Math.min(
							translatedStart, translatedEnd), rectStart, height,
							width);

					Paint color = getItemPaint(row, column);
					g2.setPaint(color);
					g2.fill(bar);

					CategoryItemLabelGenerator generator = getItemLabelGenerator(
							row, column);
					if (generator != null && isItemLabelVisible(row, column)) {
						drawItemLabel(g2, dataset, row, column, plot,
								generator, bar, false);
					}
				}
			}
		};

		plot.setRenderer(gr);

		gr.setBaseItemLabelGenerator(new IntervalCategoryItemLabelGenerator() {
			private static final long serialVersionUID = 1L;

			@Override
			public String generateLabel(CategoryDataset dataset, int row,
					int column) {
				return (1 + permutation[(counter++) % tasks]) + "";
			}
		});

		gr.setBaseItemLabelsVisible(true);
		gr.setBasePositiveItemLabelPosition(new ItemLabelPosition(
				ItemLabelAnchor.CENTER, TextAnchor.CENTER));

		((DateAxis) (plot.getRangeAxis()))
				.setDateFormatOverride(new SimpleDateFormat("ssSSS"));

		return chart;

	}

	private static GanttCategoryDataset createDataset(int[] order,
			int[][] times, int[][] endTimes) {
		TaskSeriesCollection dataset = new TaskSeriesCollection();
		TaskSeries tasks = new TaskSeries("Tasks");

		Task machine, task;

		for (int j = 0, m = endTimes.length; j < m; ++j) {
			machine = new Task("" + (j + 1), new SimpleTimePeriod(new Date(
					endTimes[j][0] - times[j][order[0]]), new Date(
					endTimes[j][order.length - 1])));

			for (int i = 0, n = order.length; i < n; ++i) {
				task = new Task("" + (i + 1), new SimpleTimePeriod(new Date(
						endTimes[j][i] - times[j][order[i]]), new Date(
						endTimes[j][i])));

				machine.addSubtask(task);
			}

			tasks.add(machine);
		}
		dataset.add(tasks);

		return dataset;
	}

}
