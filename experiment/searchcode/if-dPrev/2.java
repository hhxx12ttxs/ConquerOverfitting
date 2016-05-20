package de.winteger.piap.guicontent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.MultipleCategorySeries;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import de.winteger.piap.R;
import de.winteger.piap.core.LocationLog;
import de.winteger.piap.data.DataSource;
import de.winteger.piap.data.DateComperator;
import de.winteger.piap.helper.TimeHelper;

public class ContentLocationChart extends AContentItem {

	private Context ctx;
	private View rootView;

	// Chart variables
	private GraphicalView locationChart;
	private XYMultipleSeriesDataset dataset;
	private XYMultipleSeriesRenderer renderer;
	private double xMin = 0;
	private double xMax = 0;
	private double yMin = 0;
	private double yMax = 0;

	public ContentLocationChart(String id, String title, int iconID) {
		super(id, title, iconID);
		this.ctx = CManager.CTX;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.activity_charts, container, false);
		return rootView;
	}

	public void onResume() {
		LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.llChart);
		if (locationChart == null) {
			setupChart();
			locationChart = ChartFactory.getBarChartView(ctx, dataset, renderer, Type.STACKED);
			layout.addView(locationChart);
		} else {
			layout.removeView(locationChart);
			locationChart = ChartFactory.getBarChartView(ctx, dataset, renderer, Type.STACKED);
			layout.addView(locationChart);
		}
	}

	/**
	 * constructs the data for the chart view
	 */
	private void setupChart() {
		String[] titles = new String[] { "Time spend not at home" };
		List<double[]> values = new ArrayList<double[]>();
		int[] colors = new int[] { ctx.getResources().getColor(R.color.DeepSkyBlue) };
		renderer = buildBarRenderer(colors);

		getData(ctx, values, renderer);

		setChartSettings(renderer, "Time spent away", "Date", "Hours per Day", xMin, xMax, yMin, yMax + 5, Color.GRAY, Color.BLACK);
		renderer.getSeriesRendererAt(0).setDisplayChartValues(true);
		renderer.setXLabels(12);
		renderer.setYLabels(10);
		renderer.setXLabelsAlign(Align.LEFT);
		renderer.setYLabelsAlign(Align.LEFT);
		renderer.setPanEnabled(true, true);
		//renderer.setZoomEnabled(false);
		renderer.setZoomRate(1.1f);
		renderer.setBarSpacing(0.5f);

		renderer.setXLabelsColor(Color.BLACK);
		renderer.setYLabelsColor(0, Color.BLACK);
		renderer.setShowLegend(false);
		renderer.setXLabels(0);
		renderer.setBackgroundColor(Color.WHITE);
		renderer.setMarginsColor(Color.WHITE);

		dataset = buildBarDataset(titles, values);
	}

	/**
	 * Accesses the DB for location logs and computes data for the chart, and
	 * sets up the chart renderer
	 * 
	 * @param context
	 *            The context
	 * @param values
	 *            The location values
	 * @param renderer
	 *            The renderer
	 */
	private void getData(Context context, List<double[]> values, XYMultipleSeriesRenderer renderer) {
		ArrayList<LocationLog> logs = new ArrayList<LocationLog>();

		// get all logs from the DB
		DataSource datasource = new DataSource(context);
		datasource.openWrite();
		datasource.getAllLocationEntries(logs, 0, System.currentTimeMillis());
		datasource.close();

		// lets evaluate it a bit..
		LinkedHashMap<Date, Long> map = new LinkedHashMap<Date, Long>();

		LocationLog prev = null;
		long timeOutside;
		for (LocationLog log : logs) {
			timeOutside = 0;

			Date d = TimeHelper.getDate(log.getTimestamp());
			// Compute time away
			if (prev != null) {
				// time spent away
				Date dPrev = TimeHelper.getDate(prev.getTimestamp());
				if (dPrev.equals(d)) {
					if (prev.isEntering() == false && log.isEntering() == true) {
						long prevTime = prev.getTimestamp();
						long timediff = log.getTimestamp() - prevTime;
						timeOutside = timediff;
						if (map.containsKey(d)) {
							long time = map.get(d);
							// update stats
							time = time + timeOutside;
							// update it
							map.put(d, time);
						} else {
							// add it
							map.put(d, timeOutside);
						}
					}
				} else {
					// split dates ...
					if (prev.isEntering() == false && log.isEntering() == true) {
						long prevTime = prev.getTimestamp();
						long timediff = log.getTimestamp() - prevTime;

						// last day
						long diff = log.getTimestamp() - d.getTime();
						// now add it to the map for date..
						if (map.containsKey(d)) {
							long time = map.get(d);
							// update stats
							time = time + diff;
							// update it
							map.put(d, time);
						} else {
							// add it
							map.put(d, diff);
						}
						timediff = timediff - diff;

						// other dates
						Date date = new Date(d.getTime() - TimeHelper.INTERVAL_DAY);
						for (; timediff > 0;) {
							if (timediff < TimeHelper.INTERVAL_DAY) {
								diff = timediff;
							} else {
								diff = TimeHelper.INTERVAL_DAY;
							}
							// Left over time outside
							timediff = timediff - diff;
							// now add diff to the map for date..
							if (map.containsKey(date)) {
								long time = map.get(date);
								// update stats
								time = time + diff;
								// update it
								map.put(date, time);
							} else {
								// add it
								map.put(date, diff);
							}
							// go one day back
							date = new Date(date.getTime() - TimeHelper.INTERVAL_DAY);
						}
					}
				}
			}
			prev = log;
		}

		// Sort data, add it to the renderer
		double[] hourValues = new double[map.size()];
		ArrayList<Date> keys = new ArrayList<Date>(map.keySet());
		if (keys != null) {
			Collections.sort(keys, new DateComperator());
			for (int i = 0; i < keys.size(); i++) {
				Date key = keys.get(i);
				double hours = (double) map.get(key);
				hours = hours / 1000; // in seconds
				hours = hours / 60; // in minutes
				hours = hours / 60; // in hours
				double value = (Math.round(hours * 100.0) / 100.0); // round to 2 places
				yMax = value > yMax ? value : yMax;
				hourValues[i] = value;
				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM", Locale.US);
				String label = sdf.format(key);
				renderer.addXTextLabel(i + 1, label);
			}
		}
		xMin = Math.max(0.5, map.size() - 20);
		xMax = Math.max(10, map.size());
		values.add(hourValues);

	}

	/**
	 * Builds an XY multiple dataset using the provided values.
	 * 
	 * @param titles
	 *            the series titles
	 * @param xValues
	 *            the values for the X axis
	 * @param yValues
	 *            the values for the Y axis
	 * @return the XY multiple dataset
	 */
	protected XYMultipleSeriesDataset buildDataset(String[] titles, List<double[]> xValues, List<double[]> yValues) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		addXYSeries(dataset, titles, xValues, yValues, 0);
		return dataset;
	}

	public void addXYSeries(XYMultipleSeriesDataset dataset, String[] titles, List<double[]> xValues, List<double[]> yValues, int scale) {
		int length = titles.length;
		for (int i = 0; i < length; i++) {
			XYSeries series = new XYSeries(titles[i], scale);
			double[] xV = xValues.get(i);
			double[] yV = yValues.get(i);
			int seriesLength = xV.length;
			for (int k = 0; k < seriesLength; k++) {
				series.add(xV[k], yV[k]);
			}
			dataset.addSeries(series);
		}
	}

	/**
	 * Builds an XY multiple series renderer.
	 * 
	 * @param colors
	 *            the series rendering colors
	 * @param styles
	 *            the series point styles
	 * @return the XY multiple series renderers
	 */
	protected XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		setRenderer(renderer, colors, styles);
		return renderer;
	}

	protected void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors, PointStyle[] styles) {
		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setPointSize(5f);
		renderer.setMargins(new int[] { 20, 30, 15, 20 });
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(colors[i]);
			r.setPointStyle(styles[i]);
			renderer.addSeriesRenderer(r);
		}
	}

	/**
	 * Sets a few of the series renderer settings.
	 * 
	 * @param renderer
	 *            the renderer to set the properties to
	 * @param title
	 *            the chart title
	 * @param xTitle
	 *            the title for the X axis
	 * @param yTitle
	 *            the title for the Y axis
	 * @param xMin
	 *            the minimum value on the X axis
	 * @param xMax
	 *            the maximum value on the X axis
	 * @param yMin
	 *            the minimum value on the Y axis
	 * @param yMax
	 *            the maximum value on the Y axis
	 * @param axesColor
	 *            the axes color
	 * @param labelsColor
	 *            the labels color
	 */
	protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle, String yTitle, double xMin, double xMax, double yMin,
			double yMax, int axesColor, int labelsColor) {
		renderer.setChartTitle(title);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);
	}

	/**
	 * Builds an XY multiple time dataset using the provided values.
	 * 
	 * @param titles
	 *            the series titles
	 * @param xValues
	 *            the values for the X axis
	 * @param yValues
	 *            the values for the Y axis
	 * @return the XY multiple time dataset
	 */
	protected XYMultipleSeriesDataset buildDateDataset(String[] titles, List<Date[]> xValues, List<double[]> yValues) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		int length = titles.length;
		for (int i = 0; i < length; i++) {
			TimeSeries series = new TimeSeries(titles[i]);
			Date[] xV = xValues.get(i);
			double[] yV = yValues.get(i);
			int seriesLength = xV.length;
			for (int k = 0; k < seriesLength; k++) {
				series.add(xV[k], yV[k]);
			}
			dataset.addSeries(series);
		}
		return dataset;
	}

	/**
	 * Builds a category series using the provided values.
	 * 
	 * @param titles
	 *            the series titles
	 * @param values
	 *            the values
	 * @return the category series
	 */
	protected CategorySeries buildCategoryDataset(String title, double[] values) {
		CategorySeries series = new CategorySeries(title);
		int k = 0;
		for (double value : values) {
			series.add("Project " + ++k, value);
		}

		return series;
	}

	/**
	 * Builds a multiple category series using the provided values.
	 * 
	 * @param titles
	 *            the series titles
	 * @param values
	 *            the values
	 * @return the category series
	 */
	protected MultipleCategorySeries buildMultipleCategoryDataset(String title, List<String[]> titles, List<double[]> values) {
		MultipleCategorySeries series = new MultipleCategorySeries(title);
		int k = 0;
		for (double[] value : values) {
			series.add(2007 + k + "", titles.get(k), value);
			k++;
		}
		return series;
	}

	/**
	 * Builds a category renderer to use the provided colors.
	 * 
	 * @param colors
	 *            the colors
	 * @return the category renderer
	 */
	protected DefaultRenderer buildCategoryRenderer(int[] colors) {
		DefaultRenderer renderer = new DefaultRenderer();
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setMargins(new int[] { 20, 30, 15, 0 });
		for (int color : colors) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(color);
			renderer.addSeriesRenderer(r);
		}
		return renderer;
	}

	/**
	 * Builds a bar multiple series dataset using the provided values.
	 * 
	 * @param titles
	 *            the series titles
	 * @param values
	 *            the values
	 * @return the XY multiple bar dataset
	 */
	protected XYMultipleSeriesDataset buildBarDataset(String[] titles, List<double[]> values) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		int length = titles.length;
		for (int i = 0; i < length; i++) {
			CategorySeries series = new CategorySeries(titles[i]);
			double[] v = values.get(i);
			int seriesLength = v.length;
			for (int k = 0; k < seriesLength; k++) {
				series.add(v[k]);
			}
			dataset.addSeries(series.toXYSeries());
		}
		return dataset;
	}

	/**
	 * Builds a bar multiple series renderer to use the provided colors.
	 * 
	 * @param colors
	 *            the series renderers colors
	 * @return the bar multiple series renderer
	 */
	protected XYMultipleSeriesRenderer buildBarRenderer(int[] colors) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(colors[i]);
			renderer.addSeriesRenderer(r);
		}
		return renderer;
	}

}

