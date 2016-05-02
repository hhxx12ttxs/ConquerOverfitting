package com.agh.is.android.logdroid.telephony.views;

import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import com.agh.is.android.logdroid.R;
import com.agh.is.android.logdroid.calendar.CalendarUtilities;
import com.agh.is.android.logdroid.telephony.data.Call;
import com.agh.is.android.logdroid.telephony.data.MMS;
import com.agh.is.android.logdroid.telephony.data.SMS;
import com.agh.is.android.logdroid.telephony.database.CallDatabase;
import com.agh.is.android.logdroid.telephony.database.MMSDatabase;
import com.agh.is.android.logdroid.telephony.database.SMSDatabase;

public class StatisticsHandler {
	
	public enum DataRange {
		MMS_TYPE, SMS_TYPE, CALL_TYPE;
	}
	 
	public enum ChartType {
		BAR_CHART, LINE_CHART, SCATTER_CHART;
	}
	
	private MMSDatabase mmsDatabase = null;
	private SMSDatabase smsDatabase = null;
	private CallDatabase callDatabase = null;

	private List<MMS> mmsList = new LinkedList<MMS>();
	private List<SMS> smsList = new LinkedList<SMS>();
	private List<Call> callList = new LinkedList<Call>();
	
	private Calendar calendar = Calendar.getInstance();
	
	private List<TimeSeries> allTimeSeries = new LinkedList<TimeSeries>();
	private List<XYSeriesRenderer> allRenderers = new LinkedList<XYSeriesRenderer>();
			
	private Context context = null;
	
	private Set<DataRange> dataRanges = new HashSet<DataRange>();
			
	private ChartType chartType = null;
	
	public StatisticsHandler(Context context) {
		this.context = context;
		mmsDatabase = MMSDatabase.getInstance(context);
		smsDatabase = SMSDatabase.getInstance(context);
		callDatabase = CallDatabase.getInstance(context);
	}
	
	public void addDataRange(DataRange dR) {
		dataRanges.add(dR);
	}
	
	public void removeDataRange(DataRange dR) {
		dataRanges.remove(dR);
	}
	
	public Intent getIntentForMonthStatistics(boolean refreshData, int year, int month, String activityTitle, String xAxisTitle, ChartType chartType ) {
		if (refreshData) {
			prepareDataToShow(year, month);
		}
		this.chartType = chartType;
		prepareCalendar(year, month);
		prepareDataSeries(year, month);
		prepareRenderers();
		return generateIntent(activityTitle, xAxisTitle);
	}
	
	private void prepareDataToShow(int year, int month) {
		fillMMSList(year, month);
		fillSMSList(year, month);
		fillCallList(year, month);
	}
	
	private void fillMMSList(int year, int month) {
		mmsList.clear();
		synchronized(mmsDatabase){
			mmsDatabase.open();
			mmsList.addAll(mmsDatabase.getDataFromGivenMonth(year, month));
			mmsDatabase.close();
		}
	}
	
	private void fillSMSList(int year, int month) {
		smsList.clear();
		synchronized(smsDatabase){
			smsDatabase.open();
			smsList.addAll(smsDatabase.getDataFromGivenMonth(year, month));
			smsDatabase.close();
		}
	}
	
	private void fillCallList(int year, int month) {
		callList.clear();
		synchronized(callDatabase){
			callDatabase.open();
			callList.addAll(callDatabase.getDataFromGivenMonth(year, month));
			callDatabase.close();
		}
	}
	
	private void prepareCalendar(int year, int month) {
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
	}
	
	private void prepareDataSeries(int year, int month) {
		int size = CalendarUtilities.getAllDaysFromSpecifiedMonth(calendar).size();
		
		allTimeSeries.clear();
		
		if (dataRanges.contains(DataRange.MMS_TYPE)) {
			allTimeSeries.add(getMMSTimeSeries(size));
		}
		if (dataRanges.contains(DataRange.SMS_TYPE)) {
			allTimeSeries.add(getSMSTimeSeries(size));
		}
		if (dataRanges.contains(DataRange.CALL_TYPE)) {
			allTimeSeries.add(getCallTimeSeries(size));
		}
	}
	
	private TimeSeries getMMSTimeSeries(int size) {
		int [] xAxis = getXAxis(size, mmsList);
		int [] yAxis = getYAxis(size);
		return getSeriesFromGivenAxises(xAxis, yAxis, 
				context.getResources().getString(R.string.settings_mms_data_type));
	}
	
	private TimeSeries getSMSTimeSeries(int size) {
		int [] xAxis = getXAxis(size, smsList);
		int [] yAxis = getYAxis(size);
		return getSeriesFromGivenAxises(xAxis, yAxis, 
				context.getResources().getString(R.string.settings_sms_data_type));
	}
	
	private TimeSeries getCallTimeSeries(int size) {
		int [] xAxis = getXAxis(size, callList);
		int [] yAxis = getYAxis(size);
		return getSeriesFromGivenAxises(xAxis, yAxis, 
				context.getResources().getString(R.string.settings_call_data_type));
	}
	
	private int[] getXAxis(int size, List<? extends DisplayableData> dataToShow) {
		int xAxis[] = new int[size];
		for (DisplayableData dd : dataToShow) {
			int index = CalendarUtilities.getDayFromDate(dd.getDate());
			xAxis[index-1]++;
		}
		return xAxis;
	}
	
	private int[] getYAxis(int size) {
		int yAxis[] = new int[size];
		for (int y = 0; y < yAxis.length; ++y) {
			yAxis[y] = y+1;
		}
		return yAxis;
	}

	private TimeSeries getSeriesFromGivenAxises(int xAxis[], int yAxis[], String title) {
		TimeSeries series = new TimeSeries(title);
		for (int i = 0; i < yAxis.length; i++) {
			series.add(yAxis[i], xAxis[i]);
		}
		return series;
	}
	
	private void prepareRenderers() {
		allRenderers.clear();
		
		if (dataRanges.contains(DataRange.MMS_TYPE)) {
			allRenderers.add(getSeriesRenderer(Color.RED, false, PointStyle.SQUARE));
		}
		if (dataRanges.contains(DataRange.SMS_TYPE)) {
			allRenderers.add(getSeriesRenderer(Color.YELLOW, false, PointStyle.TRIANGLE));
		}
		if (dataRanges.contains(DataRange.CALL_TYPE)) {
			allRenderers.add(getSeriesRenderer(Color.BLUE, false, PointStyle.CIRCLE));
		}
	}
	
	private XYSeriesRenderer getSeriesRenderer(int color, boolean fillBelowLine, PointStyle pointStyle) {
		XYSeriesRenderer renderer = new XYSeriesRenderer();
		renderer.setFillBelowLine(fillBelowLine);
		renderer.setFillBelowLineColor(color);
		renderer.setFillPoints(true);
		renderer.setColor(color);
		renderer.setPointStyle(pointStyle);	
		return renderer;
	}
	
	private Intent generateIntent(String activityTitle, String xAxisTitle) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		for(TimeSeries tS : allTimeSeries) {
			dataset.addSeries(tS);
		}
		
		XYMultipleSeriesRenderer mRenderer = getMultipleSeriesRenderer(xAxisTitle);
		for(XYSeriesRenderer r : allRenderers) {
			mRenderer.addSeriesRenderer(r);
		}
		
		if (chartType == ChartType.BAR_CHART) {
			return ChartFactory.getBarChartIntent(context, dataset, mRenderer, BarChart.Type.STACKED, activityTitle);
		}
		else if (chartType == ChartType.SCATTER_CHART) {
			return ChartFactory.getScatterChartIntent(context, dataset, mRenderer, activityTitle);
		}
		else {
			return ChartFactory.getLineChartIntent(context, dataset, mRenderer, activityTitle);
		}
	}
	
	private XYMultipleSeriesRenderer getMultipleSeriesRenderer(String xAxisTitle) {
		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		mRenderer.setXTitle(xAxisTitle);
		mRenderer.setZoomEnabled(false);
		mRenderer.setExternalZoomEnabled(false);	
		mRenderer.setGridColor(Color.parseColor("#D9D9D9"));
		mRenderer.setShowGrid(true);
		return mRenderer;
	}
	
}


