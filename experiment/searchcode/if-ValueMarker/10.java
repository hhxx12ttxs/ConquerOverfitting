package com.atlassian.jira.ext.charting.jfreechart;

import com.atlassian.jira.charts.jfreechart.ChartHelper;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Utility methods used by the basic charts plugin
 */
public class ChartUtils
{
    public static ChartHelper generateMultiLineChart(XYDataset dataset, String chartTitle, String yLabel, String xLabel, List domainMarkers)
    {
        boolean legend = true;
        boolean tooltips = true;
        boolean urls = true;

        JFreeChart chart = ChartFactory.createTimeSeriesChart(chartTitle, yLabel, xLabel, dataset, legend, tooltips, urls);

        XYPlot plot = chart.getXYPlot();

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        TickUnitSource units = NumberAxis.createIntegerTickUnits();
        yAxis.setStandardTickUnits(units);

        if (domainMarkers != null && !domainMarkers.isEmpty())
        {
            for (Iterator iterator = domainMarkers.iterator(); iterator.hasNext();)
            {
                ValueMarker valueMarker = (ValueMarker) iterator.next();
                valueMarker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
                valueMarker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
                plot.addDomainMarker(valueMarker);
            }
        }

        return new ChartHelper(chart);
    }

    /**
     * This will generate an XYDataset for each name/data pair contained in the incomming map.
     *
     * @param nameSeriesMap
     * @param timeZone
     * @return XYDataset containing the passed in data in a series for each entry in the map named by the entries key.
     */
    public static XYDataset generateTimeSeriesXYDataset(Map nameSeriesMap, TimeZone timeZone) // FIXME should call the methods next
    {
        TimeSeriesCollection dataset = new TimeSeriesCollection(timeZone);

        for (Iterator iterator = nameSeriesMap.keySet().iterator(); iterator.hasNext();)
        {
            String name = (String) iterator.next();
            Map seriesMap = (Map) nameSeriesMap.get(name);
            TimeSeries series = null;

            for (Iterator iterator1 = seriesMap.keySet().iterator(); iterator1.hasNext();)
            {
                RegularTimePeriod period = (RegularTimePeriod) iterator1.next();

                if (series == null)
                {
                    series = new TimeSeries(name, period.getClass());
                }
                series.add(period, (Number) seriesMap.get(period));
            }
            if (series != null)
            {
                dataset.addSeries(series);
            }
        }

        return dataset;
    }
}

