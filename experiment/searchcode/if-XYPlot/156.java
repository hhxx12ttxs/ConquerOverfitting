/*
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */

package com.assembla.nufco;

import java.awt.Color;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;

/**
 *
 * @author Jerome Robert
 */
public class NChartPanel extends ChartPanel {

	private Chart chart;

	public NChartPanel(Chart chart) {
		super(createJFC(chart));
		this.chart = chart;
		XYPlot plot = getChart().getXYPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setDomainGridlinePaint(Color.BLACK);
		plot.setRangeGridlinePaint(Color.BLACK);
	}

	private static JFreeChart createJFC(Chart sg) {
		return ChartFactory.createXYLineChart(sg.getTitle(),
				sg.getXTitle(), sg.getYTitle(), new DefaultXYDataset(),
				PlotOrientation.VERTICAL, true, true, true);
	}

	public void refresh() {
		XYPlot plot = getChart().getXYPlot();
		plot.getDomainAxis().setAutoRange(chart.isAutoScale());
		plot.getRangeAxis().setAutoRange(chart.isAutoScale());
		XYItemRenderer renderer = plot.getRenderer();
		DefaultXYDataset dataset = (DefaultXYDataset) plot.getDataset();
		int k = 0;
		for (int i = 0; i < chart.getSize(); i++) {
			dataset.removeSeries(chart.getName(i));
		}

		for (int i = 0; i < chart.getSize(); i++) {
			if (chart.isVisible(i)) {
				dataset.addSeries(chart.getName(i), chart.getData(i));
				renderer.setSeriesPaint(k++, chart.getColor(i));
			}
		}
	}
}

