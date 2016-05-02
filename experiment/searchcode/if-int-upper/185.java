package com.ntscorp.jenkins.plugin;

import hudson.XmlFile;
import hudson.model.Build;
import hudson.util.ColorPalette;
import hudson.util.ShiftedCategoryAxis;
import hudson.util.XStream2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleInsets;

import com.ntscorp.jenkins.plugin.model.Constant;

public class CountingUtil {
	/**
	 * 라인 차트를 리턴한다.
	 */
	@SuppressWarnings("deprecation")
	public static JFreeChart createLineChart(CategoryDataset dataset, String yAxis, int lower, int upper) {
		final JFreeChart chart = ChartFactory.createLineChart(null, null, yAxis, dataset, PlotOrientation.VERTICAL, false, true, false);

		chart.setBackgroundPaint(Color.white);

		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlinePaint(null);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.black);

		CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
		plot.setDomainAxis(domainAxis);
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
		domainAxis.setLowerMargin(0.0);
		domainAxis.setUpperMargin(0.0);
		domainAxis.setCategoryMargin(0.0);

		if (lower == Integer.MAX_VALUE) {
			lower = 0;
		}

		if (upper == Integer.MIN_VALUE) {
			upper = 0;
		}

		double lowerBound = lower * 0.95;
		double upperBound = upper * 1.05;

		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setLowerBound(lowerBound);
		rangeAxis.setUpperBound(upperBound);

		final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
		renderer.setStroke(new BasicStroke(2.0f));
		ColorPalette.apply(renderer);
		renderer.setSeriesPaint(0, ColorPalette.RED);
		plot.setInsets(new RectangleInsets(5.0, 0, 0, 5.0));

		return chart;
	}
	
	public static XmlFile getDataFile(Build<?, ?> build) {
		File dir = build == null ? new File(System.getProperty("java.io.tmpdir")) : build.getRootDir();
		return new XmlFile(new XStream2(), new File(dir, Constant.RESULT_FILENAME));
	}
}

