package com.atlassian.jira.ext.charting.jfreechart;

import com.atlassian.jira.charts.jfreechart.ChartHelper;
import junit.framework.TestCase;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

import java.util.*;

public class TestChartUtils extends TestCase
{


    public void testGenerateMultiLineChart()
    {
        final DefaultXYDataset xyDataset = new DefaultXYDataset();
        final List<ValueMarker> domainMarkers = 
            Arrays.asList(new ValueMarker(2d), new ValueMarker(4d), new ValueMarker(6d), new ValueMarker(8d));

        xyDataset.addSeries("fooSeries1",
                new double[][]{
                        {1d, 2d, 3d, 4d},
                        {5d, 6d, 7d, 8d}
                });

        /* Without any domain markers */
        ChartHelper chartHelper = ChartUtils.generateMultiLineChart(
                xyDataset, "fooTitle", "fooYLabel", "fooXLabel", Collections.EMPTY_LIST);
        assertEquals("fooTitle", chartHelper.getChart().getTitle().getText());

        /* With some domain markers */
        chartHelper = ChartUtils.generateMultiLineChart(
                xyDataset, "fooTitle", "fooYLabel", "fooXLabel", domainMarkers);
        assertEquals("fooTitle", chartHelper.getChart().getTitle().getText());
    }

//    private static TimeSeries createTimeSeries(String name)
//    {
//        TimeSeries timeSeries = new TimeSeries(name);
//        for(int i = 0; i < 5; i++)
//        {
//            timeSeries.add(new Day(1 + i, 1, 2008), i*1.0d);
//        }
//
//        return timeSeries;
//    }

    //TODO: After cleanup test reduce dataset
//    public void testReduceDataset() {
//        final Calendar now = Calendar.getInstance();
//        final DefaultCategoryDataset sourceSet = new DefaultCategoryDataset();
//        final CategoryDataset reducedSet;
//        final List allRowKeys = new ArrayList();
//        final List reducedRowKeys;
//
//        final StringBuffer stringBuffer = new StringBuffer();
//        for (int i = 0; i < 3; ++i) {
//            for (int j = 0; j < 3; ++j) {
//
//                now.add(Calendar.DAY_OF_YEAR, 1);
//
//                stringBuffer.setLength(0);
//
//                sourceSet.addValue(
//                        new Integer(stringBuffer.append(i).append(j).toString()),
//                        String.valueOf(i),
//                        now.getTime());
//            }
//
//            allRowKeys.add(String.valueOf(i));
//        }
//
//        /* Check if data is populated accordingly */
//        assertEquals(3, allRowKeys.size());
//        assertEquals("0", allRowKeys.get(0));
//        assertEquals("1", allRowKeys.get(1));
//        assertEquals("2", allRowKeys.get(2));
//
//        (reducedRowKeys = new ArrayList(allRowKeys)).remove(allRowKeys.size() - 1);
//
//        reducedSet = ChartUtils.reduceDataset(sourceSet, reducedRowKeys);
//        assertTrue(sourceSet.getRowCount() > reducedSet.getRowCount());
//        assertTrue(sourceSet.getRowKeys().containsAll(reducedSet.getRowKeys()));
//    }

    public void testGenerateTimeSeriesXYDataset()
    {
        final Map<String, Map<RegularTimePeriod, Integer>> nameSeriesMap = new HashMap<String, Map<RegularTimePeriod, Integer>>();

        /* Generate data for testing */
        for (int i = 0; i < 2; ++i)
        {
            final Calendar now = Calendar.getInstance();
            final Map<RegularTimePeriod, Integer> seriesMap = new HashMap<RegularTimePeriod, Integer>();

            for (int j = 0; j < 5; ++j)
            {
                now.add(Calendar.DAY_OF_YEAR, 1);

                seriesMap.put(
                        RegularTimePeriod.createInstance(Day.class, now.getTime(), TimeZone.getDefault()),
                        new Integer(j));
            }
            nameSeriesMap.put(String.valueOf(i), seriesMap);
        }

        XYDataset xyDataset = ChartUtils.generateTimeSeriesXYDataset(nameSeriesMap, TimeZone.getDefault());

        assertEquals(nameSeriesMap.size(), xyDataset.getSeriesCount());
        validateXYDatasetAgainstMap(xyDataset, nameSeriesMap);
    }

    private static void validateXYDatasetAgainstMap(XYDataset xyDataset, Map nameSeriesMap)
    {
        for (int i = 0, j = xyDataset.getSeriesCount(); i < j; ++i)
        {
            final Map dataMap = (Map) nameSeriesMap.get(String.valueOf(i));

            for (int k = 0, xItemCount = xyDataset.getItemCount(i); k < xItemCount; ++k)
            {
                final Date date = new Date(xyDataset.getX(i, k).longValue());
                final Number value = xyDataset.getY(i, k);

                assertEquals(
                        dataMap.get(RegularTimePeriod.createInstance(Day.class, date, TimeZone.getDefault())),
                        value);
            }
        }
    }
}

