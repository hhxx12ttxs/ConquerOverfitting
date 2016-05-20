package com.loveplusplus.demo.chart;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

/**
 * 显示 饼状图 柱状图 折线图
 * 
 * @author Administrator
 * 
 */
public class ChartActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {

	public static final String TAG = "ChartActivity";
	private LinearLayout rootView;
	private ArrayList<ChartBean> chartData;
	private int type = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.achartengine);
		rootView = (LinearLayout) findViewById(R.id.chart_parent);
		ActionBar mActionBar = getSupportActionBar();
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.chart_type, R.layout.spinner_item);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		mActionBar.setListNavigationCallbacks(adapter, this);

		new ChartAsyncTask().execute();
	}

	class ChartAsyncTask extends AsyncTask<Void, Void, StaticsMessage> {

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(ChartActivity.this);
			dialog.setMessage("正在请求数据，请稍候……");
			dialog.show();
		}

		@Override
		protected StaticsMessage doInBackground(Void... params) {
			try {
				//请求服务器，获取统计数据,此处使用的是模拟数据
				StaticsMessage msg=new StaticsMessage();
				ArrayList<ChartBean> list=new ArrayList<ChartBean>();
				list.add(new ChartBean("8月份", 167649,"#04FE7C"));
				list.add(new ChartBean("9月份", 227347,"#AFD8F8"));
				list.add(new ChartBean("10月份", 232565,"#8BBA00"));
				list.add(new ChartBean("11月份", 285350,"#FF8E46"));
				msg.list=list;
				return msg;
			} catch (Exception e) {
				Log.e(TAG, "请求服务器异常", e);
				return null;
			}
		}

		@Override
		protected void onPostExecute(StaticsMessage result) {
			if (dialog != null) {
				dialog.dismiss();
			}
			if (result != null) {
				chartData = result.list;
				showChart();
			}

		}

	}

	void showChart() {
		if (chartData == null) {
			return;
		}
		switch (type) {
		case 0:
			rootView.removeAllViews();
			rootView.addView(getPieView(this, chartData));
			break;
		case 1:
			rootView.removeAllViews();
			rootView.addView(getBarGraphView(this, chartData));
			break;
		case 2:
			rootView.removeAllViews();
			rootView.addView(getLineGraphView(this, chartData));
			break;
		}

	}

	@Override
	public boolean onNavigationItemSelected(int position, long arg1) {
		type = position;
		showChart();
		return true;
	}

	private GraphicalView getBarGraphView(Context context, List<ChartBean> data) {
		XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
		XYSeries series = new XYSeries("社会公共管理办公室");
		for (int i = 0; i < data.size(); i++) {
			series.add(i + 1, data.get(i).value);
		}
		mDataset.addSeries(series);

		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		mRenderer.setXTitle("\n\n\n\n日期");// 设置为X轴的标题
		mRenderer.setYTitle("数量");// 设置y轴的标题
		mRenderer.setAxisTitleTextSize(20);// 设置xy轴标题文本大小
		// mRenderer.setChartTitleTextSize(35);// 设置图表标题文字的大小
		mRenderer.setLabelsTextSize(15);// 设置标签的文字大小
		mRenderer.setLegendTextSize(20);// 设置图例文本大小
		mRenderer.setPointSize(10f);// 设置点的大小
		mRenderer.setYAxisMin(0);// 设置y轴最小值是0
		double maxValue = getMaxValue(data);
		mRenderer.setYAxisMax(11 * maxValue / 10);
		mRenderer.setXAxisMin(0);
		mRenderer.setXAxisMax(data.size() + 1);
		mRenderer.setShowGrid(true);// 显示网格

		mRenderer.setBarSpacing(0.5);

		mRenderer.setPanEnabled(false, false);// 设置x方向可以滑动，y方向不可以滑动
		mRenderer.setZoomEnabled(false, false);// 设置x，y方向都不可以放大或缩小

		for (int i = 0; i < data.size(); i++) {
			mRenderer.addXTextLabel(i + 1, data.get(i).label);
		}

		mRenderer.setXLabels(0);// 设置只显示如1月，2月等替换后的东西，不显示1,2,3等
		mRenderer.setMargins(new int[] { 20, 50, 50, 20 });// 设置视图位置 上 左 下 右

		// 设置颜色
		mRenderer.setXLabelsColor(Color.BLACK);
		mRenderer.setYLabelsColor(0, Color.BLACK);
		mRenderer.setMarginsColor(Color.WHITE);
		mRenderer.setLabelsColor(Color.BLACK);

		// 折线
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(Color.BLUE);// 设置颜色
		r.setPointStyle(PointStyle.CIRCLE);// 设置点的样式
		r.setFillPoints(true);// 填充点（显示的点是空心还是实心）
		r.setDisplayChartValues(true);// 将点的值显示出来
		r.setChartValuesSpacing(10);// 显示的点的值与图的距离
		r.setChartValuesTextSize(25);// 点的值的文字大小
		r.setLineWidth(3);// 设置线宽
		mRenderer.addSeriesRenderer(r);

		GraphicalView view = ChartFactory.getBarChartView(context, mDataset, mRenderer, Type.DEFAULT);
		return view;
	}

	private GraphicalView getLineGraphView(Context context, List<ChartBean> data) {
		XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
		XYSeries series = new XYSeries("社会公共管理办公室");
		for (int i = 0; i < data.size(); i++) {
			series.add(i + 1, data.get(i).value);
		}
		mDataset.addSeries(series);

		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		mRenderer.setXTitle("\n\n\n\n日期");// 设置为X轴的标题
		mRenderer.setYTitle("数量");// 设置y轴的标题
		mRenderer.setAxisTitleTextSize(20);// 设置xy轴标题文本大小
		// mRenderer.setChartTitleTextSize(35);// 设置图表标题文字的大小
		mRenderer.setLabelsTextSize(15);// 设置标签的文字大小
		mRenderer.setLegendTextSize(20);// 设置图例文本大小
		mRenderer.setPointSize(10f);// 设置点的大小
		mRenderer.setYAxisMin(0);// 设置y轴最小值是0
		double maxValue = getMaxValue(data);
		mRenderer.setYAxisMax(11 * maxValue / 10);
		mRenderer.setXAxisMin(0);
		mRenderer.setXAxisMax(data.size() + 1);
		mRenderer.setShowGrid(true);// 显示网格

		mRenderer.setPanEnabled(false, false);// 设置x方向可以滑动，y方向不可以滑动
		mRenderer.setZoomEnabled(false, false);// 设置x，y方向都不可以放大或缩小

		for (int i = 0; i < data.size(); i++) {
			mRenderer.addXTextLabel(i + 1, data.get(i).label);
		}

		mRenderer.setXLabels(0);// 设置只显示如1月，2月等替换后的东西，不显示1,2,3等
		mRenderer.setMargins(new int[] { 20, 50, 50, 20 });// 设置视图位置 上 左 下 右

		// 设置颜色
		mRenderer.setXLabelsColor(Color.BLACK);
		mRenderer.setYLabelsColor(0, Color.BLACK);
		mRenderer.setMarginsColor(Color.WHITE);
		mRenderer.setLabelsColor(Color.BLACK);

		// 折线
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(Color.BLUE);// 设置颜色
		r.setPointStyle(PointStyle.CIRCLE);// 设置点的样式
		r.setFillPoints(true);// 填充点（显示的点是空心还是实心）
		r.setDisplayChartValues(true);// 将点的值显示出来
		r.setChartValuesSpacing(10);// 显示的点的值与图的距离
		r.setChartValuesTextSize(25);// 点的值的文字大小
		r.setLineWidth(3);// 设置线宽
		mRenderer.addSeriesRenderer(r);

		GraphicalView view = ChartFactory.getLineChartView(context, mDataset, mRenderer);
		return view;
	}

	private double getMaxValue(List<ChartBean> data) {
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).value > max) {
				max = data.get(i).value;
			}
		}
		return max;
	}

	private GraphicalView getPieView(Context context, List<ChartBean> list) {

		final CategorySeries series = new CategorySeries("pie");
		final DefaultRenderer renderer = new DefaultRenderer();

		for (ChartBean b : list) {
			series.add(b.label, b.value);

			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(Color.parseColor(b.color));
			r.setDisplayBoundingPoints(true);
			r.setDisplayChartValuesDistance(5);
			r.setDisplayChartValues(true);
			r.setChartValuesTextSize(20);
			renderer.addSeriesRenderer(r);
		}

		renderer.setLabelsColor(Color.BLACK);
		renderer.setShowLabels(true);
		renderer.setLabelsTextSize(25);
		renderer.setLegendTextSize(30);

		renderer.setDisplayValues(true);

		renderer.setPanEnabled(false);// 移动
		renderer.setZoomEnabled(false);// 缩放

		renderer.setAntialiasing(true);
		renderer.setClickEnabled(true);

		//饼状图点击分离
		final GraphicalView view = ChartFactory.getPieChartView(context, series, renderer);
		view.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				SeriesSelection seriesSelection = view.getCurrentSeriesAndPoint();
				if (seriesSelection != null) {
					for (int i = 0; i < series.getItemCount(); i++) {
						renderer.getSeriesRendererAt(i).setHighlighted(i == seriesSelection.getPointIndex());
					}
					view.repaint();
				}
			}
		});
		return view;
	}
}

