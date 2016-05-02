package de.winteger.piap.guicontent;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
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
import de.winteger.piap.core.EvalAccess;
import de.winteger.piap.core.GlobalSettings;
import de.winteger.piap.evaluation.Evaluation;
import de.winteger.piap.helper.TimeHelper;

public class ContentInputEvalChart extends AContentItem {

	private final String TAG = "Logger";
	private Context ctx;
	private View rootView;

	private Evaluation evaluation;

	// Chart variables
	private GraphicalView evaluationChart;
	private XYMultipleSeriesDataset dataset;
	private XYMultipleSeriesRenderer renderer;
	// Chart data
	private String[] mTitles; // category titles
	private List<Date[]> mDates; // dates per category (x-value)
	List<double[]> mValues; // values per category (y-value)
	private int[] mColors; // category colors
	private HashMap<Date, int[]> mMap = new HashMap<Date, int[]>(); // pair of x value and all y values
	private ArrayList<Date> mKeys = new ArrayList<Date>(); // x values
	private int mLimitTriggers = 0; // max y-Value

	public ContentInputEvalChart(String id, String title, int iconID) {
		super(id, title, iconID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ctx = CManager.CTX;

		rootView = inflater.inflate(R.layout.activity_charts, container, false);

		// load last evaluation
		if ((evaluation = EvalAccess.loadLastEvaluation(ctx)) == null) {
			// loading evaluation failed
			evaluation = new Evaluation(ctx);
		}

		// set up data for the chart
		if (evaluation != null) {
			// Titles and Colors
			String[] firstTitle = { ctx.getString(R.string.chart_total) };
			mTitles = GlobalSettings.concat(firstTitle, EvalAccess.getCategoryDescriptors(ctx));
			int[] firstColor = { Color.BLACK };
			mColors = GlobalSettings.concat(firstColor, EvalAccess.getCategoryColors(ctx));
			// Data
			evaluation.computeDailyData(mMap, mKeys);
		}

		return rootView;
	}

	public void onResume() {
		LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.llChart);
		if (evaluationChart == null) {
			setupChart();
			evaluationChart = ChartFactory.getTimeChartView(ctx, dataset, renderer, "dd.MM");
			layout.addView(evaluationChart);
		} else {
			layout.removeView(evaluationChart);
			evaluationChart = ChartFactory.getTimeChartView(ctx, dataset, renderer, "dd.MM");
			layout.addView(evaluationChart);
		}
	}

	private void setupChart() {
		mDates = new ArrayList<Date[]>();
		mValues = new ArrayList<double[]>();

		// fill it with the data
		// dates

		for (int i = 0; i < mTitles.length; i++) {
			mDates.add(new Date[mKeys.size()]);
			for (int j = 0; j < mKeys.size(); j++) {
				mDates.get(i)[j] = mKeys.get(j);
			}
		}

		// alarmtriggers
		for (int i = 0; i < mTitles.length; i++) {
			double[] data = new double[mKeys.size()];
			for (int j = 0; j < mKeys.size(); j++) {
				int[] v = mMap.get(mKeys.get(j));
				if (i == 0) {
					mLimitTriggers = (v[i] > mLimitTriggers ? v[i] : mLimitTriggers);
				}
				data[j] = v[i];
			}
			mValues.add(data);
		}

		PointStyle[] styles = new PointStyle[mTitles.length];
		styles[0] = PointStyle.CIRCLE;
		for (int i = 1; i < mTitles.length; i++) {
			styles[i] = PointStyle.CIRCLE;
		}
		//		PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND, PointStyle.TRIANGLE, PointStyle.SQUARE };
		renderer = buildRenderer(mColors, styles);
		int length = renderer.getSeriesRendererCount();
		for (int i = 0; i < length; i++) {
			((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
			SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(i);
			seriesRenderer.setDisplayChartValues(true);
			seriesRenderer.setDisplayChartValuesDistance(0);
			//			renderer.getSeriesRendererAt(i).setDisplayChartValues(true);
		}

		setChartSettings(renderer, "", ctx.getString(R.string.chart_date), ctx.getString(R.string.chart_alarmtriggers), System.currentTimeMillis()
				- TimeHelper.INTERVAL_DAY * 3, System.currentTimeMillis() + TimeHelper.INTERVAL_DAY * 3, 0, mLimitTriggers + 5, Color.BLACK, Color.BLACK);
		renderer.setXLabels(12);
		renderer.setYLabels(10);
		renderer.getSeriesRendererAt(0).setDisplayChartValues(false);
		renderer.setShowGrid(true);
		renderer.setXLabelsAlign(Align.RIGHT);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setZoomButtonsVisible(false);
		renderer.setXLabelsColor(Color.BLACK);
		renderer.setYLabelsColor(0, Color.BLACK);
		renderer.setBackgroundColor(Color.WHITE);
		renderer.setMarginsColor(Color.WHITE);

		//		Log.i(TAG, "renderer.getSeriesRendererCount() " + renderer.getSeriesRendererCount());
		//		for (int i = 0; i < renderer.getSeriesRendererCount(); i++) {
		//			renderer.getSeriesRendererAt(i).setDisplayChartValues(true);
		//		}

		dataset = buildDateDataset(mTitles, mDates, mValues);
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

}

