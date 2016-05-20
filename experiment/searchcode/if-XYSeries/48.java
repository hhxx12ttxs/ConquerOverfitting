package kr.re.etri.lifeinfomatics.promes.util.chart;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import kr.re.etri.lifeinfomatics.promes.data.ChartData;
import kr.re.etri.lifeinfomatics.promes.data.Define;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Demo for {@link XYSeries}, where all the y values are the same.
 * 
 */
public class LineChart {
	private static Paint[] paint = new Paint[] { new GradientPaint(0, 50, new Color(173, 38, 243), 0, 200, Color.white), new GradientPaint(0, 50, new Color(243, 137, 41), 0, 200, Color.white), new GradientPaint(0, 50, new Color(197, 243, 39), 0, 200, Color.white),
			new GradientPaint(0, 50, new Color(243, 41, 163), 0, 200, Color.white), new GradientPaint(0, 50, new Color(41, 92, 243), 0, 200, Color.white), new GradientPaint(0, 50, new Color(200, 33, 119), 0, 200, Color.white), new GradientPaint(0, 50, new Color(200, 168, 33), 0, 200, Color.white),
			new GradientPaint(0, 50, new Color(37, 201, 46), 0, 200, Color.white), new GradientPaint(0, 50, new Color(201, 74, 37), 0, 200, Color.white), new GradientPaint(0, 50, new Color(138, 51, 204), 0, 200, Color.white), new GradientPaint(0, 50, new Color(227, 40, 232), 0, 200, Color.white),
			new GradientPaint(0, 50, new Color(231, 190, 33), 0, 200, Color.white), new GradientPaint(0, 50, new Color(127, 232, 40), 0, 200, Color.white), new GradientPaint(0, 50, new Color(231, 33, 90), 0, 200, Color.white), new GradientPaint(0, 50, new Color(52, 40, 232), 0, 200, Color.white) };

	private XYSeriesCollection dataset = new XYSeriesCollection();
	private JFreeChart chart = ChartFactory.createXYLineChart("", "", "Percent (%)", dataset, PlotOrientation.VERTICAL, true, true, false);

	private Hashtable<String, XYSeries> seriesHash = new Hashtable<String, XYSeries>();

	public LineChart() {
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setNoDataMessage("No data to display");

		NumberAxis axis = (NumberAxis) plot.getRangeAxis();
		axis.setAutoRangeIncludesZero(true);
	}

	public void setChartData(ArrayList<ChartData> dataList, String type) {
		seriesHash.clear();
		dataset.removeAllSeries();

		XYSeries noneSeries = new XYSeries(Define.TAKEN_SATUS_NONE);
		XYSeries takenSeries = new XYSeries(Define.TAKEN_SATUS_PRETAKEN);
		XYSeries unTakenSeries = new XYSeries(Define.TAKEN_SATUS_PREUNTAKEN);
		XYSeries outTakenSeries = new XYSeries(Define.TAKEN_SATUS_PREOUTTAKEN);

		XYSeries keySeries = new XYSeries("");
		ChartData chartData = new ChartData();
		for (int i = 0; i < dataList.size(); i++) {
			if (type.equals("A")) {
				noneSeries.add(i, dataList.get(i).getPercent(Define.TAKEN_SATUS_NONE));
				takenSeries.add(i, dataList.get(i).getPercent(Define.TAKEN_SATUS_PRETAKEN));
				unTakenSeries.add(i, dataList.get(i).getPercent(Define.TAKEN_SATUS_PREUNTAKEN));
				outTakenSeries.add(i, dataList.get(i).getPercent(Define.TAKEN_SATUS_PREOUTTAKEN));
			}
			else if (type.equals("B")) {
				chartData.setTotalNone(chartData.getTotalNone() + dataList.get(i).getNone());
				chartData.setTotalTaken(chartData.getTotalTaken() + dataList.get(i).getTaken());
				chartData.setNone(chartData.getTotalUntaken() + dataList.get(i).getUnTaken());
				chartData.setNone(chartData.getOutTaken() + dataList.get(i).getOutTaken());

				noneSeries.add(i, chartData.getTotalNone());
				takenSeries.add(i, chartData.getTotalTaken());
				unTakenSeries.add(i, chartData.getTotalUntaken());
				outTakenSeries.add(i, chartData.getOutTaken());
			}
		}

		dataset.addSeries(keySeries);
		dataset.addSeries(noneSeries);
		dataset.addSeries(takenSeries);
		dataset.addSeries(unTakenSeries);
		dataset.addSeries(outTakenSeries);

		seriesHash.put("Key", keySeries);
		seriesHash.put("None", noneSeries);
		seriesHash.put("Taken", takenSeries);
		seriesHash.put("UnTaken", unTakenSeries);
		seriesHash.put("OutTaken", outTakenSeries);
	}

	public void saveChart(String fileName, int width, int height) throws Exception {
		File fs = new File(fileName);
		ChartUtilities.saveChartAsJPEG(fs, chart, width, height);
	}

	public void saveChart(File fs, int width, int height) throws Exception {
		ChartUtilities.saveChartAsJPEG(fs, chart, width, height);
	}

}

