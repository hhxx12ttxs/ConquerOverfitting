package userInterface;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.axis.PeriodAxisLabelInfo;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

import com.lowagie.text.pdf.events.IndexEvents.Entry;

import Entities.postTested;

/**
 * In this demo, the {@link PeriodAxis} class is used to display both date and
 * day-of-the-week labels on a bar chart.
 */
public class PeriodAxisDemo3 extends JFrame implements ChartMouseListener {

	/**
	 * A demonstration application showing how to create a simple time series
	 * chart. This example uses monthly data.
	 * 
	 * @param title
	 *            the frame title.
	 */
	public PeriodAxisDemo3(String title, ArrayList<postTested> listPost) {
		super(title);
		JPanel chartPanel = createDemoPanel(listPost);
		chartPanel.setPreferredSize(new Dimension(1000, 540));
		((ChartPanel) chartPanel).addChartMouseListener(this);
		setContentPane(chartPanel);
	}

	/**
	 * Creates a chart.
	 * 
	 * @param dataset
	 *            a dataset.
	 * 
	 * @return A chart.
	 */
	private static JFreeChart createChart(IntervalXYDataset dataset) {

		JFreeChart chart = ChartFactory.createXYBarChart("Sentiment Graph",
				"Week", true, "Sentiment", dataset, PlotOrientation.VERTICAL,
				true, true, false);

		chart.setBackgroundPaint(Color.white);

		XYPlot plot = (XYPlot) chart.getPlot();

		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);

		PeriodAxis domainAxis = new PeriodAxis("Week");
		domainAxis.setAutoRangeTimePeriodClass(Week.class);
		PeriodAxisLabelInfo[] info = new PeriodAxisLabelInfo[3];
		info[0] = new PeriodAxisLabelInfo(Week.class, new SimpleDateFormat("w"));
		info[1] = new PeriodAxisLabelInfo(Month.class, new SimpleDateFormat(
				"MMM"), new RectangleInsets(2, 2, 2, 2), new Font("SansSerif",
				Font.BOLD, 10), Color.blue, false, new BasicStroke(0.0f),
				Color.lightGray);
		info[2] = new PeriodAxisLabelInfo(Year.class, new SimpleDateFormat(
				"yyyy"));
		domainAxis.setLabelInfo(info);
		plot.setDomainAxis(domainAxis);
		return chart;

	}

	/**
	 * Creates a dataset, consisting of two series of monthly data.
	 * 
	 * @return the dataset.
	 */
	private static IntervalXYDataset createDataset(
			ArrayList<postTested> listPost) {
		TimeSeries s1 = new TimeSeries("Positive");
		Map<Week, Integer> timeserie1 = new HashMap<Week, Integer>();
		TimeSeries s2 = new TimeSeries("Negative");
		Map<Week, Integer> timeserie2 = new HashMap<Week, Integer>();
		for (postTested post : listPost) {

			Calendar cal = Calendar.getInstance();
			cal.setTime(post.getDatePost());
			int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
			int year = cal.get(Calendar.YEAR);
			Week week = new Week(weekOfYear, year);
			if (post.getClazz().equalsIgnoreCase("Subjective/pos")) {
				if (timeserie1.containsKey(week)) {
					int temp = timeserie1.get(week);
					timeserie1.put(week, temp + 1);
				} else
					timeserie1.put(week, 1);
			}
			if (post.getClazz().equalsIgnoreCase("Subjective/neg")) {
				if (timeserie2.containsKey(week)) {
					int temp = timeserie2.get(week);
					timeserie2.put(week, temp - 1);
				} else
					timeserie2.put(week, -1);
			}

		}

		for (java.util.Map.Entry<Week, Integer> etr : timeserie1.entrySet()) {
			// System.out.println("key: " + etr.getKey() + " Value: " +
			// etr.getValue());
			s1.add(etr.getKey(), etr.getValue());
		}
		for (java.util.Map.Entry<Week, Integer> etr : timeserie2.entrySet()) {
			// System.out.println("key2: " + etr.getKey() + " Value2: " +
			// etr.getValue());
			s2.add(etr.getKey(), etr.getValue());
		}

		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(s1);
		dataset.addSeries(s2);

		return dataset;
	}

	/**
	 * Creates a panel for the demo (used by SuperDemo.java).
	 * 
	 * @return A panel.
	 */
	public static JPanel createDemoPanel(ArrayList<postTested> listPost) {
		JFreeChart chart = createChart(createDataset(listPost));

		return new ChartPanel(chart);
	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *            ignored.
	 */
	public static void main(String[] args) {
		// PeriodAxisDemo3 demo = new PeriodAxisDemo3("Period Axis Demo 3");
		// demo.pack();
		// RefineryUtilities.centerFrameOnScreen(demo);
		// demo.setVisible(true);
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent chartmouseevent) {
		XYItemEntity xyitem = (XYItemEntity) chartmouseevent.getEntity(); // get
																			// clicked
																			// entity
		XYDataset dataset = (XYDataset) xyitem.getDataset(); // get data set
		System.out.println(xyitem.getItem() + " item of "
				+ xyitem.getSeriesIndex() + "series");
		System.out.println(dataset.getXValue(xyitem.getSeriesIndex(),
				xyitem.getItem()));
		System.out.println(dataset.getYValue(xyitem.getSeriesIndex(),
				xyitem.getItem()));
		Comparable comparable = dataset.getSeriesKey(0);
		XYPlot xyplot = (XYPlot) chartmouseevent.getChart().getPlot();
		System.out.println(xyplot.getRangeCrosshairValue());
//		ChartEntity chartentity = chartmouseevent.getEntity();
//		if (chartentity != null) {
//			System.out.println("Mouse clicked: " + chartentity.toString());
//		} else
//			System.out.println("Mouse clicked: null entity.");
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent arg0) {
		// TODO Auto-generated method stub

	}

}
