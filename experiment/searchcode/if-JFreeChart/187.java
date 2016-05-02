package com.atlassian.jira.ext.charting.gadgets.charts;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.jfreechart.ChartHelper;
import com.atlassian.jira.charts.jfreechart.util.ChartUtil;
import com.atlassian.jira.charts.util.DataUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.ext.charting.data.DateRangeObjectHitCollector;
import com.atlassian.jira.ext.charting.field.DateOfFirstResponseCFType;
import com.atlassian.jira.ext.charting.field.util.CustomFieldLocator;
import com.atlassian.jira.ext.charting.statistics.CustomFieldDatePeriodStatisticsMapper;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.DatePeriodStatisticsMapper;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.bean.I18nBean;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimePeriod;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class TimeToFirstResponseChart implements ChartParamKeys
{
    private final CustomFieldManager customFieldManager;

    private final IssueIndexManager issueIndexManager;

    private final SearchProvider searchProvider;

    private final SearchService searchService;

    private final ApplicationProperties applicationProperties;

    private final TimeZoneManager timeZoneManager;

    public TimeToFirstResponseChart(CustomFieldManager customFieldManager, IssueIndexManager issueIndexManager, SearchProvider searchProvider, SearchService searchService, ApplicationProperties applicationProperties, TimeZoneManager timeZoneManager)
    {
        this.customFieldManager = customFieldManager;
        this.issueIndexManager = issueIndexManager;
        this.searchProvider = searchProvider;
        this.searchService = searchService;
        this.applicationProperties = applicationProperties;
        this.timeZoneManager = timeZoneManager;
    }

    public Chart generate(
            final JiraAuthenticationContext jiraAuthenticationContext,
            final SearchRequest searchRequest,
            final ChartFactory.PeriodName periodName,
            final int daysPrevious,
            final int width,
            final int height) throws IOException, SearchException
    {
        final CustomField firstResponseTimeCf = CustomFieldLocator.getCustomField(customFieldManager, DateOfFirstResponseCFType.class);
        final SearchRequest searchRequestCopy = new SearchRequest(searchRequest);
        final I18nHelper i18nBean = jiraAuthenticationContext.getI18nHelper();
        int normalizedDaysPrevious = DataUtils.normalizeDaysValue(daysPrevious, periodName);

        final Class<? extends TimePeriod> timePeriodClass = ChartUtil.getTimePeriodClass(periodName);
        CategoryDataset dataset = getAverageOpenTimes(jiraAuthenticationContext, searchRequestCopy, timePeriodClass, normalizedDaysPrevious, firstResponseTimeCf);
        CategoryDataset chartDataset = reduceDataset(dataset, Arrays.asList(i18nBean.getText("datacollector.averageresolution")));

        ChartHelper helper = generateBarChart(chartDataset, null, null, i18nBean.getText("datacollector.hours"));
        ChartUtil.setDefaults(helper.getChart(), new I18nBean(jiraAuthenticationContext.getLoggedInUser()));

        JFreeChart chart = helper.getChart();
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setToolTipGenerator(new StandardCategoryToolTipGenerator("{1}: {2} " + i18nBean.getText("datacollector.hourstorespond"), NumberFormat.getInstance()));

        final VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext();
        CategoryURLGenerator urlGenerator = new CategoryURLGenerator()
        {
            public String generateURL(CategoryDataset categoryDataset, int row, int col)
            {
                if (row == 0) // only generate urls for the first row in the dataset
                {
                    RegularTimePeriod period = (RegularTimePeriod) categoryDataset.getColumnKey(col);
                    StatisticsMapper createdMapper = new CustomFieldDatePeriodStatisticsMapper(timePeriodClass, firstResponseTimeCf.getId());
                    SearchRequest searchUrlSuffix = createdMapper.getSearchUrlSuffix(period, searchRequestCopy);
                    return velocityRequestContext.getCanonicalBaseUrl() + "/secure/IssueNavigator.jspa?reset=true" + searchService.getQueryString(jiraAuthenticationContext.getLoggedInUser(), searchUrlSuffix.getQuery());
                }
                else
                    return null;
            }
        };
        renderer.setItemURLGenerator(urlGenerator);
        plot.setRenderer(renderer);

        helper.generate(width, height);

        Map<String, Object> chartParams = new HashMap<String, Object>();

        chartParams.put("chart", helper.getLocation());
        chartParams.put("chartDataset", chartDataset);
        chartParams.put(KEY_COMPLETE_DATASET, dataset);
        chartParams.put(KEY_COMPLETE_DATASET_URL_GENERATOR, urlGenerator);
        chartParams.put("normalizedDaysPrevious", normalizedDaysPrevious);
        chartParams.put("daysPrevious", daysPrevious);
        chartParams.put("imagemap", helper.getImageMap());
        chartParams.put("imagemapName", helper.getImageMapName());

        // For resolutiontime.vm
        chartParams.put("i18nPrefix", "portlet.firstresponsetime");
        chartParams.put("numIssues", 10);

        Chart theChart = new Chart(helper.getLocation(), helper.getImageMap(), helper.getImageMapName(), chartParams);
        return theChart;
    }

    private CategoryDataset getAverageOpenTimes(JiraAuthenticationContext jiraAuthenticationContext, SearchRequest sr, Class<? extends TimePeriod> timePeriodClass, int normalizedDaysPrevious, CustomField firstResponseTimeCf) throws IOException, SearchException
    {
        StatisticsMapper createdMapper = new DatePeriodStatisticsMapper(timePeriodClass, DocumentConstants.ISSUE_CREATED, timeZoneManager.getLoggedInUserTimeZone());
        StatisticsMapper dateMapper = new DatePeriodStatisticsMapper(timePeriodClass, firstResponseTimeCf.getId(), timeZoneManager.getLoggedInUserTimeZone());

        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder(sr.getQuery());
        builder.where().defaultAnd().customField(firstResponseTimeCf.getIdAsLong()).gtEq("-" + normalizedDaysPrevious + "d");
        final Map<RegularTimePeriod, List<Long>> allOpenTimes = new TreeMap<RegularTimePeriod, List<Long>>();
        searchProvider.search(
                builder.buildQuery(),
                jiraAuthenticationContext.getLoggedInUser(),
                new DateRangeObjectHitCollector(
                        createdMapper.getDocumentConstant(),
                        dateMapper.getDocumentConstant(),
                        allOpenTimes,
                        issueIndexManager.getIssueSearcher(),
                        timePeriodClass,
                        timeZoneManager.getLoggedInUserTimeZone())
        );

        DataUtils.normaliseDateRange(allOpenTimes, normalizedDaysPrevious - 1, timePeriodClass, timeZoneManager.getLoggedInUserTimeZone()); // only need to do one map as normalising keys will fix second

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Iterator iterator = allOpenTimes.keySet().iterator(); iterator.hasNext();)
        {
            RegularTimePeriod period = (RegularTimePeriod) iterator.next();
            final Object o = allOpenTimes.get(period);
            Collection times = Collections.<Long>emptyList();
            if (o instanceof Collection) // normalising the date range puts integers in, but we neatly work around that here.
                times = (Collection) o;
            long total = 0;
            long average = 0;

            if (times != null)
            {
                for (Iterator iterator1 = times.iterator(); iterator1.hasNext();)
                {
                    Long time = (Long) iterator1.next();
                    total += time;
                }
                if (times.size() > 0)
                    average = total / times.size();
            }

            I18nHelper i18nHelper = jiraAuthenticationContext.getI18nHelper();
            dataset.addValue(times.size(), i18nHelper.getText("datacollector.issuesresolvedcapital"), period);
            dataset.addValue(total / DateUtils.HOUR_MILLIS, i18nHelper.getText("datacollector.totalresolvetime"), period);
            dataset.addValue(average / DateUtils.HOUR_MILLIS, i18nHelper.getText("datacollector.averageresolvetime"), period);
        }

        return dataset;
    }

    private CategoryDataset reduceDataset(CategoryDataset dataset, List<? extends Comparable<?>> rowKeysToKeep)
    {
        DefaultCategoryDataset newDataset = new DefaultCategoryDataset();
        for (Comparable<?> rowKey : rowKeysToKeep)
        {
            for (Iterator iterator1 = dataset.getColumnKeys().iterator(); iterator1.hasNext();)
            {
                Comparable colKey = (Comparable) iterator1.next();
                newDataset.addValue(dataset.getValue(rowKey, colKey), rowKey, colKey);
            }
        }

        return newDataset;
    }

    private ChartHelper generateBarChart(CategoryDataset dataset, String chartTitle, String yLabel, String xLabel)
    {
        JFreeChart chart = org.jfree.chart.ChartFactory.createBarChart(chartTitle, yLabel, xLabel, dataset, PlotOrientation.VERTICAL, false, false, false);
        BarRenderer renderer = (BarRenderer) ((CategoryPlot) chart.getPlot()).getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);

        return new ChartHelper(chart);
    }
}

