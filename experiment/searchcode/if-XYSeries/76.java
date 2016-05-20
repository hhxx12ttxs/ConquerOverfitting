package net.customware.confluence.flotchart.util;

import com.atlassian.confluence.util.i18n.I18NBean;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import static com.atlassian.core.util.DateUtils.DAY_MILLIS;
import static com.atlassian.core.util.DateUtils.MINUTE_MILLIS;
import static com.atlassian.core.util.DateUtils.YEAR_MILLIS;
import net.customware.confluence.flotchart.model.Chart;
import net.customware.confluence.flotchart.model.options.AxisOptions;
import net.customware.confluence.flotchart.model.options.LegendOptions;
import net.customware.confluence.flotchart.model.options.PlotOptions;
import net.customware.confluence.flotchart.model.options.SeriesOptions;
import net.customware.confluence.flotchart.model.options.TimeSeriesAxisOptions;
import net.customware.confluence.flotchart.model.series.DataPoint;
import net.customware.confluence.flotchart.model.series.PieCustomization;
import net.customware.confluence.flotchart.model.series.PointCustomization;
import net.customware.confluence.flotchart.model.series.Series;
import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Year;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * A facade that converts {@link net.customware.confluence.flotchart.model.Chart} to
 * {@link org.jfree.chart.JFreeChart} for use in the macro's static render modes.
 */
public class JFreeChartFactory
{
    /**
     * For i18n support in Confluence.
     */
    private I18NBeanFactory i18NBeanFactory;

    public JFreeChartFactory(I18NBeanFactory i18NBeanFactory)
    {
        this.i18NBeanFactory = i18NBeanFactory;
    }

    private I18NBean geti18NBean()
    {
        return i18NBeanFactory.getI18NBean();
    }

    private String getText(String key)
    {
        return geti18NBean().getText(key);
    }

    private boolean isTimeSeries(Chart theChart)
    {
        return theChart.getOptions().getXaxis() instanceof TimeSeriesAxisOptions;
    }

    private long getAverageDataPointDistance(Chart theChart)
    {
        List<Series> series = theChart.getSeries();
        List<Long> distances = new ArrayList<Long>();

        for (Series aSeries : series)
        {
            Number lastValue = null;
            for (DataPoint dp : aSeries.getData())
            {
                if (null == lastValue)
                    lastValue = (Number) dp.getDomainValue();
                else
                {
                    Number currentValue = (Number) dp.getDomainValue();
                    distances.add(currentValue.longValue() - lastValue.longValue());
                    lastValue = currentValue;
                }
            }
        }

        long total = 0;
        for (long distance : distances)
            total += distance;

        return total / distances.size();
    }

    private String getDateFormat(Chart theChart)
    {
        TimeSeriesAxisOptions timeSeriesAxisOptions = (TimeSeriesAxisOptions) theChart.getOptions().getXaxis();
        String flotFormat = StringUtils.defaultString(
                timeSeriesAxisOptions.getTimeFormat(),
                FlotUtil.calculateSuitableFlotTimeFormat(getAverageDataPointDistance(theChart))
        );

        return FlotUtil.convertFlotTimeSeriesFormatToJavaDateFormat(flotFormat);
    }
    
    private PieDataset getPieDataset(Chart theChart)
    {
        DefaultPieDataset defaultPieDataset = new DefaultPieDataset();

        for (Series series : theChart.getSeries())
        {
            for (DataPoint dataPoint : series.getData())
                defaultPieDataset.setValue(series.getLabel(), (Number) dataPoint.getRangeValue());
        }

        PieCustomization pieCustomization = theChart.getOptions().getSeries().getPie();
        PieCustomization.Combine combine = pieCustomization.getCombine();

        double threshold = 0.03;
        String otherLabel = getText("flotchart.otherLabel.default");
        
        if (null != combine)
        {
            if (null != combine.getThreshold())
                threshold = combine.getThreshold().doubleValue();
            if (null != combine.getLabel())
                otherLabel = combine.getLabel();
        }

        return DatasetUtilities.createConsolidatedPieDataset(defaultPieDataset, otherLabel, threshold);
    }

    private CategoryDataset getCategoryDataset(Chart theChart)
    {
        DefaultCategoryDataset defaultCategoryDataset = new DefaultCategoryDataset();
        AxisOptions xaxis = theChart.getOptions().getXaxis();
        boolean isTimeSeries = isTimeSeries(theChart);
        DateFormat dateFormat = isTimeSeries ? new SimpleDateFormat(getDateFormat(theChart)) : null;

        for (Series series : theChart.getSeries())
        {
            for (DataPoint dataPoint : series.getData())
            {
                defaultCategoryDataset.addValue(
                        (Number) dataPoint.getRangeValue(),
                        series.getLabel(),
                        isTimeSeries
                                ? dateFormat.format(new Date(((Number) dataPoint.getDomainValue()).longValue()))
                                : xaxis.getTickLabel(dataPoint.getDomainValue())
                );
            }
        }

        return defaultCategoryDataset;
    }

    private Class<? extends RegularTimePeriod> getRegularTimePeriod(Chart theChart)
    {
        long averageTimeSeriesTickSizeInMillis = getAverageDataPointDistance(theChart);

        if (averageTimeSeriesTickSizeInMillis < MINUTE_MILLIS)
            return Minute.class;
        if (averageTimeSeriesTickSizeInMillis < DAY_MILLIS)
            return Hour.class;
        if (averageTimeSeriesTickSizeInMillis < DAY_MILLIS * 28)
            return Day.class;
        if (averageTimeSeriesTickSizeInMillis < YEAR_MILLIS)
            return Month.class;
        else
            return Year.class;
    }

    private XYDataset getGenericXyDataset(Chart theChart)
    {
        DefaultTableXYDataset defaultTableXYDataset = new DefaultTableXYDataset();

        for (Series series : theChart.getSeries())
        {
            XYSeries xySeries = new XYSeries(series.getLabel(), false, false);
            for (DataPoint dataPoint : series.getData())
            {
                xySeries.add(
                        (Number) dataPoint.getDomainValue(),
                        (Number) dataPoint.getRangeValue()
                );
            }

            defaultTableXYDataset.addSeries(xySeries);
        }

        return defaultTableXYDataset;
    }


    private XYDataset getTimeSeriesXyDataset(Chart theChart)
    {
        TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();
        Class<? extends RegularTimePeriod> timePeriodClass = getRegularTimePeriod(theChart);
        TimeZone timeZone = TimeZone.getDefault();


        for (Series series : theChart.getSeries())
        {
            TimeSeries timeSeries = new TimeSeries(series.getLabel(), timePeriodClass);
            for (DataPoint dataPoint : series.getData())
            {
                RegularTimePeriod regularTimePeriod = RegularTimePeriod.createInstance(timePeriodClass, new Date(((Number) dataPoint.getDomainValue()).longValue()), timeZone);
                timeSeries.add(
                        regularTimePeriod,
                        (Number) dataPoint.getRangeValue()
                );
            }

            timeSeriesCollection.addSeries(timeSeries);
        }

        return timeSeriesCollection;
    }


    private XYDataset getXyDataset(Chart theChart)
    {
        return isTimeSeries(theChart) ? getTimeSeriesXyDataset(theChart) : getGenericXyDataset(theChart);
    }

    private boolean isShowLegend(Chart theChart)
    {
        PlotOptions plotOptions = theChart.getOptions();
        PieCustomization pieOptions = plotOptions.getSeries().getPie();
        LegendOptions legendOptions = plotOptions.getLegendOptions();

        if (null == pieOptions)
        {
            return null != legendOptions && legendOptions.isShow();
        }
        else
        {
            return null == pieOptions.getLabel() || pieOptions.getLabel().isShow();
        }
    }

    private JFreeChart _createPieChart(Chart theChart)
    {
        PieDataset pieDataset = getPieDataset(theChart);
        return ChartFactory.createPieChart(
                null, pieDataset, false, false, false
        );
    }

    private JFreeChart _createBarChart(Chart theChart)
    {
        CategoryDataset categoryDataset = getCategoryDataset(theChart);
        return ChartFactory.createStackedBarChart(
                null, null, null, categoryDataset, PlotOrientation.VERTICAL, isShowLegend(theChart), false, false
        );
    }

    private boolean isShowShapes(Chart theChart)
    {
        PointCustomization pointCustomization = theChart.getOptions().getSeries().getPoints();
        return null != pointCustomization && pointCustomization.isShow();
    }

    private JFreeChart _createLineChart(Chart theChart)
    {
        JFreeChart jFreeChart;
        if (isTimeSeries(theChart))
        {
            XYDataset xyDataset = getXyDataset(theChart);

            jFreeChart = ChartFactory.createTimeSeriesChart(
                    null, null, null, xyDataset,
                    isShowLegend(theChart),
                    false, false
            );

            XYLineAndShapeRenderer xyLineAndShapeRenderer = (XYLineAndShapeRenderer) ((XYPlot) jFreeChart.getPlot()).getRenderer();

            if (isShowShapes(theChart))
            {
                for (int i = 0, j = theChart.getSeries().size(); i < j; ++i)
                    xyLineAndShapeRenderer.setSeriesShape(i, new Arc2D.Float(-3f, -3f, 6f, 6f, 0f, 360f, Arc2D.OPEN));

                xyLineAndShapeRenderer.setBaseShapesFilled(false);
                xyLineAndShapeRenderer.setBaseShapesVisible(true);
            }
            else
            {
                xyLineAndShapeRenderer.setBaseShapesVisible(false);
            }
        }
        else
        {
            CategoryDataset dataset = getCategoryDataset(theChart);

            jFreeChart = ChartFactory.createLineChart(
                    null, null, null, dataset, PlotOrientation.VERTICAL,
                    isShowLegend(theChart),
                    false, false
            );

            LineAndShapeRenderer lineAndShapeRenderer = (LineAndShapeRenderer) ((CategoryPlot) jFreeChart.getPlot()).getRenderer();

            if (isShowShapes(theChart))
            {
                for (int i = 0, j = theChart.getSeries().size(); i < j; ++i)
                    lineAndShapeRenderer.setSeriesShape(i, new Arc2D.Float(-3f, -3f, 6f, 6f, 0f, 360f, Arc2D.OPEN));

                lineAndShapeRenderer.setBaseShapesFilled(false);
                lineAndShapeRenderer.setBaseShapesVisible(true);
            }
            else
            {
                lineAndShapeRenderer.setBaseShapesVisible(false);
            }
        }

        return jFreeChart;

    }

    private JFreeChart _createAreaChart(Chart theChart)
    {
        if (isTimeSeries(theChart))
        {
            XYDataset xyDataset = getXyDataset(theChart);

            return ChartFactory.createXYAreaChart(
                    null, null, null, xyDataset, PlotOrientation.VERTICAL,
                    isShowLegend(theChart),
                    false, false
            );
        }
        else
        {
            CategoryDataset dataset = getCategoryDataset(theChart);

            return ChartFactory.createAreaChart(
                    null, null, null, dataset, PlotOrientation.VERTICAL,
                    isShowLegend(theChart),
                    false, false
            );
        }
    }

    private JFreeChart _createStepChart(Chart theChart)
    {
        XYDataset xyDataset = getXyDataset(theChart);
        return ChartFactory.createXYStepChart(
                null, null, null, xyDataset, PlotOrientation.VERTICAL, isShowLegend(theChart), false, false
        );
    }

    private void setBackgroundColorToWhite(JFreeChart theJFreeChart)
    {
        theJFreeChart.setBackgroundPaint(Color.WHITE);
    }

    private void setSeriesColors(JFreeChart theJFreeChart, Chart theChart)
    {
        List<Series> series = theChart.getSeries();
        Plot plot = theJFreeChart.getPlot();


        if (plot instanceof CategoryPlot)
        {
            CategoryPlot categoryPlot = (CategoryPlot) plot;
            CategoryDataset categoryDataset = categoryPlot.getDataset();
            CategoryItemRenderer categoryItemRenderer = categoryPlot.getRenderer();

            for (int i = 0, j = Math.min(categoryDataset.getRowCount(), series.size()); i < j; ++i)
                categoryItemRenderer.setSeriesPaint(i, new Color(Integer.parseInt(series.get(i).getColor().substring(1), 16)));
        }
        else if (plot instanceof XYPlot)
        {
            XYPlot xyPlot = (XYPlot) plot;
            XYDataset xyDataset = xyPlot.getDataset();
            XYItemRenderer xyItemRenderer = xyPlot.getRenderer();

            for (int i = 0, j = Math.min(xyDataset.getSeriesCount(), series.size()); i < j; ++i)
                xyItemRenderer.setSeriesPaint(i, new Color(Integer.parseInt(series.get(i).getColor().substring(1), 16)));
        }
        else if (plot instanceof PiePlot)
        {
            PiePlot piePlot = (PiePlot) plot;
            PieDataset pieDataset = piePlot.getDataset();

            for (int i = 0, j = Math.min(pieDataset.getItemCount(), series.size()); i < j; ++i)
                piePlot.setSectionPaint(i, new Color(Integer.parseInt(series.get(i).getColor().substring(1), 16)));
        }
    }

    private void setTimeSeriesDisplayOverride(JFreeChart theJFreeChart, Chart theChart)
    {
        Plot plot = theJFreeChart.getPlot();
        if (plot instanceof XYPlot)
        {
            Axis axis = ((XYPlot) plot).getDomainAxis();
            /* Step charts are time series charts rendered in a different way. Since we allow the user to generate
             * step charts without specifying the timeSeriesFormat parameter, we should check if the chart is really
             * a time series chart (isTimeSeries(theChart)) before we go casting the theChart.getOptions().getXaxis() to a
             * TimeSeriesAxisOptions.
             */
            if (axis instanceof DateAxis && isTimeSeries(theChart))
            {
                DateAxis dateAxis = (DateAxis) axis;
                long averageDataPointDistance = getAverageDataPointDistance(theChart);
                
                dateAxis.setDateFormatOverride(
                        new SimpleDateFormat(
                                FlotUtil.convertFlotTimeSeriesFormatToJavaDateFormat(
                                        StringUtils.defaultString(
                                                ((TimeSeriesAxisOptions) theChart.getOptions().getXaxis()).getTimeFormat(),
                                                FlotUtil.calculateSuitableFlotTimeFormat(
                                                        averageDataPointDistance
                                                )
                                        )
                                )
                        )
                );
            }
        }
    }

    private void setJFreeChartOpacity(JFreeChart theJFreeChart, Chart theChart)
    {
        SeriesOptions seriesOptions = theChart.getOptions().getSeries();
        Number opacity = null;
        if (null != seriesOptions.getLines())
            opacity = seriesOptions.getLines().getFill();
        else if (null != seriesOptions.getBars())
            opacity = seriesOptions.getBars().getFill();
        else if (null != seriesOptions.getPie())
            opacity = seriesOptions.getPie().getFill();
        
        if (null != opacity)
            theJFreeChart.getPlot().setForegroundAlpha(opacity.floatValue());
    }

    private void customizeChartLooks(Chart theChart, JFreeChart theJfreeChart)
    {
        setBackgroundColorToWhite(theJfreeChart);
        setSeriesColors(theJfreeChart, theChart);
        setTimeSeriesDisplayOverride(theJfreeChart, theChart);
        setJFreeChartOpacity(theJfreeChart, theChart);
    }

    public JFreeChart createPieChart(Chart theChart)
    {
        JFreeChart theJfreeChart = _createPieChart(theChart);

        if (!isShowLegend(theChart))
        {
            PiePlot piePlot = (PiePlot) theJfreeChart.getPlot();
            piePlot.setLabelGenerator(null);
        }

        customizeChartLooks(theChart, theJfreeChart);

        return theJfreeChart;
    }

    public JFreeChart createBarChart(Chart theChart)
    {
        JFreeChart theJFreeChart = _createBarChart(theChart);
        customizeChartLooks(theChart, theJFreeChart);
        return theJFreeChart;
    }

    public JFreeChart createLineChart(Chart theChart)
    {
        JFreeChart theJFreeChart = _createLineChart(theChart);
        customizeChartLooks(theChart, theJFreeChart);
        return theJFreeChart;
    }

    public JFreeChart createAreaChart(Chart theChart)
    {
        JFreeChart theJFreeChart = _createAreaChart(theChart);
        customizeChartLooks(theChart, theJFreeChart);
        return theJFreeChart;
    }

    public JFreeChart createStepChart(Chart theChart)
    {
        JFreeChart theJFreeChart = _createStepChart(theChart);
        customizeChartLooks(theChart, theJFreeChart);
        return theJFreeChart;
    }
}

