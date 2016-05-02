/*
 * Licensed to Marvelution under one or more contributor license 
 * agreements.  See the NOTICE file distributed with this work 
 * for additional information regarding copyright ownership.
 * Marvelution licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.marvelution.jira.plugins.hudson.charts;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.CategoryTableXYDataset;
import org.jfree.data.xy.XYDataset;

import com.atlassian.jira.charts.jfreechart.ChartHelper;
import com.atlassian.jira.charts.jfreechart.util.ChartDefaults;
import com.atlassian.jira.charts.jfreechart.util.ChartUtil;
import com.atlassian.jira.util.I18nHelper;
import com.marvelution.hudson.plugins.apiv2.resources.model.build.Build;
import com.marvelution.hudson.plugins.apiv2.resources.model.build.TestResult;

/**
 * {@link HudsonChartGenerator} implementation for generating a ratio build test results chart
 * 
 * @author <a href="mailto:markrekveld@marvelution.com">Mark Rekveld</a>
 */
public class BuildTestResultsRatioChartGenerator extends AbstractHudsonChartGenerator implements XYToolTipGenerator {

	private Map<Integer, Build> buildMap;
	private final String[] seriesNames;

	/**
	 * Default Constructor
	 */
	public BuildTestResultsRatioChartGenerator() {
		this(null);
	}

	/**
	 * Constructor
	 * 
	 * @param i18nHelper the {@link I18nHelper} implementation
	 */
	public BuildTestResultsRatioChartGenerator(I18nHelper i18nHelper) {
		super(i18nHelper);
		seriesNames = new String[] {
			getI18n().getText("hudson.charts.test.passed"),
			getI18n().getText("hudson.charts.test.failed"),
			getI18n().getText("hudson.charts.test.skipped")
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChartHelper generateChart() {
		buildMap = new HashMap<Integer, Build>();
		final CategoryTableXYDataset dataset = new CategoryTableXYDataset();
		for (Build build : builds) {
			final TestResult results = build.getTestResult();
			double percentagePass = 0.0D, percentageFail = 0.0D, percentageSkipped = 0.0D;
			if (results != null && results.getTotal() > 0) {
				percentagePass = Double.valueOf(results.getPassed()) / Double.valueOf(results.getTotal()) * 100.0D;
				percentageFail = Double.valueOf(results.getFailed()) / Double.valueOf(results.getTotal()) * 100.0D;
				percentageSkipped = Double.valueOf(results.getSkipped()) / Double.valueOf(results.getTotal()) * 100.0D;
			}
			dataset.add(Double.valueOf(build.getBuildNumber()), percentagePass, seriesNames[0]);
			dataset.add(Double.valueOf(build.getBuildNumber()), percentageFail, seriesNames[1]);
			dataset.add(Double.valueOf(build.getBuildNumber()), percentageSkipped, seriesNames[2]);
			buildMap.put(Integer.valueOf(build.getBuildNumber()), build);
		}
		final JFreeChart chart = ChartFactory.createStackedXYAreaChart("", "", getI18n().getText("hudson.charts.tests"),
				dataset, PlotOrientation.VERTICAL, false, false, false);
		chart.setBackgroundPaint(Color.WHITE);
		chart.setBorderVisible(false);
		XYPlot xyPlot = chart.getXYPlot();
		xyPlot.setDataset(1, dataset);
		if (dataset.getItemCount() > 0) {
			XYLineAndShapeRenderer shapeRenderer = new XYLineAndShapeRenderer(false, true);
			shapeRenderer.setSeriesShapesVisible(1, false);
			shapeRenderer.setSeriesLinesVisible(1, false);
			shapeRenderer.setSeriesShapesVisible(2, false);
			shapeRenderer.setSeriesLinesVisible(2, false);
			shapeRenderer.setSeriesShape(0, new Ellipse2D.Double(-3.0D, -3.0D, 6.0D, 6.0D));
			shapeRenderer.setSeriesPaint(0, GREEN_PAINT);
			shapeRenderer.setSeriesShapesFilled(0, true);
			shapeRenderer.setBaseToolTipGenerator(this);
			shapeRenderer.setBaseItemLabelFont(ChartDefaults.defaultFont);
			shapeRenderer.setBaseItemLabelsVisible(false);
			xyPlot.setRenderer(0, shapeRenderer);
			StackedXYAreaRenderer2 renderer = new StackedXYAreaRenderer2();
			renderer.setSeriesPaint(0, GREEN_PAINT);
			renderer.setSeriesPaint(1, RED_PAINT);
			renderer.setSeriesPaint(2, YELLOW_PAINT);
			renderer.setBaseItemLabelFont(ChartDefaults.defaultFont);
			renderer.setBaseItemLabelsVisible(false);
			xyPlot.setRenderer(1, renderer);
			renderer.setBaseToolTipGenerator(this);
		}
		ValueAxis rangeAxis = xyPlot.getRangeAxis();
		rangeAxis.setLowerBound(0.0D);
		rangeAxis.setUpperBound(100.0D);
		final NumberAxis domainAxis = new NumberAxis();
		domainAxis.setLowerBound(Collections.min(buildMap.keySet()));
		domainAxis.setUpperBound(Collections.max(buildMap.keySet()));
		final TickUnitSource ticks = NumberAxis.createIntegerTickUnits();
		domainAxis.setStandardTickUnits(ticks);
		domainAxis.setVerticalTickLabels(true);
		xyPlot.setDomainAxis(domainAxis);
		ChartUtil.setupPlot(xyPlot);
		return new ChartHelper(chart);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String generateToolTip(XYDataset dataset, int series, int item) {
		int passed = 0, failed = 0, skipped = 0;
		final int number = Double.valueOf(dataset.getXValue(0, item)).intValue();
		final Build build = buildMap.get(number);
		if (build.getTestResult() != null) {
			passed = build.getTestResult().getPassed();
			failed = build.getTestResult().getFailed();
			skipped = build.getTestResult().getSkipped();
		}
		return getI18n().getText("hudson.charts.test.tooltip", String.valueOf(build.getBuildNumber()),
				String.valueOf(passed), String.valueOf(failed),
				String.valueOf(skipped));
	}

}

