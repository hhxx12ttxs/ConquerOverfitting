/**
 * Copyright (c) 2005, 2006 David Peterson, Tom Davies(Atlassian), Bob Swift
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * 			 notice, this list of conditions and the following disclaimer in the
 *   		 documentation and/or other materials provided with the distribution.
 *     * The names of contributors may not be used to endorse or promote products
 *           derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Created Nov 2005 by David Peterson and Tom Davies
 */

package com.atlassian.confluence.extra.chart;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.DefaultConversionContext;
import com.atlassian.confluence.content.render.xhtml.XhtmlException;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.importexport.resource.DownloadResourceWriter;
import com.atlassian.confluence.importexport.resource.WritableDownloadResourceManager;
import com.atlassian.confluence.languages.Language;
import com.atlassian.confluence.languages.LanguageManager;
import com.atlassian.confluence.languages.LocaleManager;
import com.atlassian.confluence.links.linktypes.AbstractPageLink;
import com.atlassian.confluence.links.linktypes.AttachmentLink;
import com.atlassian.confluence.links.linktypes.BlogPostLink;
import com.atlassian.confluence.links.linktypes.PageLink;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.AttachmentManager;
import com.atlassian.confluence.pages.thumbnail.CannotGenerateThumbnailException;
import com.atlassian.confluence.pages.thumbnail.ThumbnailInfo;
import com.atlassian.confluence.pages.thumbnail.ThumbnailManager;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.util.i18n.I18NBean;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.core.util.LocaleUtils;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.TokenType;
import com.atlassian.renderer.links.Link;
import com.atlassian.renderer.links.LinkResolver;
import com.atlassian.renderer.links.UnpermittedLink;
import com.atlassian.renderer.links.UnresolvedLink;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.RenderUtils;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.user.User;

/**
 * Generates a chart based on table data contained in its body.
 *
 * @author David Peterson
 */
public class ChartMacro extends BaseMacro implements Macro
{

	private final SettingsManager settingsManager;
	private final LanguageManager languageManager;
	private final AttachmentManager attachmentManager;
	private final PermissionManager permissionManager;
	private final ThumbnailManager thumbnailManager;
    private final WritableDownloadResourceManager downloadResourceManager;
    private final XhtmlContent xhtmlContent;
    private final LinkResolver linkResolver;
    private final LocaleManager localeManager;
    private final I18NBeanFactory i18nBeanFactory;

    private static final Logger log = Logger.getLogger(ChartMacro.class);
    
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;
    private static final double DEFAULT_PIE_EXPLODE = .30;

    // parameter values
    private static final String TRUE = "true";
    private static final String FALSE = "false";

    private static final String HORIZONTAL = "horizontal";
    private static final String VERTICAL = "vertical";
    private static final String BEFORE = "before";
    private static final String AFTER = "after";

    private static final String NEW = "new";
    private static final String REPLACE = "replace";
	private static final String KEEP = "keep";

    // supported chart types
    private static final String PIE_TYPE = "pie";
    private static final String BAR_TYPE = "bar";
    private static final String LINE_TYPE = "line";
    private static final String AREA_TYPE = "area";
    private static final String XYLINE_TYPE = "xyline";
    private static final String XYAREA_TYPE = "xyarea";
    private static final String XYBAR_TYPE = "xybar";
    private static final String XYSTEP_TYPE = "xystep";
    private static final String XYSTEPAREA_TYPE = "xysteparea";
    private static final String SCATTER_TYPE = "scatter";
//  private static final String HISTOGRAM_TYPE = "histogram"; // not supported
    private static final String TIMESERIES_TYPE = "timeseries";
    private static final String GANTT_TYPE = "gantt";

    private static final String START = "start";
    private static final String MIDDLE = "middle";
    private static final String END = "end";

    private static final Map<String, Integer> COLOR_MAP = Collections.unmodifiableMap(
            new HashMap<String, Integer>()
            {
                {
                    put("aqua", 0xffff);
                    put("black", 0);
                    put("blue", 0xff);
                    put("cyan", 0xffff);
                    put("fuchsia", 0xff00ff);
                    put("gray", 0x808080);
                    put("green", 0xff00);
                    put("lime", 0xff00);
                    put("maroon", 0x800000);
                    put("navy", 0x80);
                    put("olive", 0x808000);
                    put("purple", 0xffc0ff);
                    put("red", 0xff0000);
                    put("silver", 0xc0c0c0);
                    put("teal", 0x808000);
                    put("violet", 0xee82ee);
                    put("white", 0xffffff);
                    put("yellow", 0xffff00);
                }
            }
    );

    public ChartMacro(SettingsManager settingsManager, LanguageManager languageManager, AttachmentManager attachmentManager, 
    		PermissionManager permissionManager, ThumbnailManager thumbnailManager, WritableDownloadResourceManager downloadResourceManager, 
    		XhtmlContent xhtmlContent, LinkResolver linkResolver, LocaleManager localeManager, I18NBeanFactory i18nBeanFactory)
    {
        this.settingsManager = settingsManager;
        this.languageManager = languageManager;
        this.attachmentManager = attachmentManager;
        this.permissionManager = permissionManager;
        this.thumbnailManager = thumbnailManager;
        this.downloadResourceManager = downloadResourceManager;
        this.xhtmlContent = xhtmlContent;
        this.linkResolver = linkResolver;
        this.localeManager = localeManager;
        this.i18nBeanFactory = i18nBeanFactory;
    }

    @Override
    public TokenType getTokenType(Map map, String s, RenderContext renderContext)
    {
        return TokenType.BLOCK; // This is only block to agree with getOutputType()
    }

    public boolean hasBody()
    {
        return true;
    }

    public RenderMode getBodyRenderMode()
    {
        return RenderMode.NO_RENDER;
    }

    public String execute(Map parameters, String body, RenderContext renderContext) throws MacroException
    {
        try
        {
        	ConversionContext conversionContext =  new DefaultConversionContext(renderContext);
        	List<RuntimeException> errorList = new ArrayList<RuntimeException>();
            body =  xhtmlContent.convertWikiToView(body, conversionContext, errorList);
        	if(!errorList.isEmpty())
        	{
        		for(RuntimeException runtimeException : errorList)
        		{
        			log.error("RuntimeException while parsing wiki markup ", runtimeException);
        		}
        		throw new MacroException(getI18NBean().getText("confluence.extra.chart.chart.error.parseWikiToStorage"));
        	}
    	    return execute(parameters, body, conversionContext);
        }
        catch (MacroExecutionException e)
        {
            throw new MacroException(e);
        } catch (XhtmlException e) {
            throw new MacroException(e);
        } catch (XMLStreamException e) {
            throw new MacroException(e);
        }
    }

    public OutputType getOutputType()
    {
    	return OutputType.BLOCK; // This has to be block, otherwise, the user won't be able to edit the chart macro body in the editor and somehow the body is treated as if it is PLAIN_TEXT.
    }

    public BodyType getBodyType()
    {
    	return BodyType.RICH_TEXT;
    }

    public String execute(Map<String, String> parameters, String chartDataHtml, ConversionContext conversionContext) throws MacroExecutionException
    {
        parameters = toLowerCase(parameters);    // ensure parameters are case insensitive
        
        try 
        {
            StringBuilder chartHtmlBuilder = new StringBuilder();   // for content output
            String xDisplayData = getStringParameter(parameters, "displaydata", FALSE);       // depreciated name, leave for src compatibility
            String dataDisplay = getStringParameter(parameters, "datadisplay", xDisplayData);   // current name
            if (!dataDisplay.equalsIgnoreCase(FALSE))
            {
                // rendered data requested, so we have to render no matter what
        		if (dataDisplay.equalsIgnoreCase(BEFORE))
                {
        		    // put rendered data on top of the image
                    chartHtmlBuilder.append(chartDataHtml);
                }
            }

            try
            {
            	Attachment chartImageAttachment = getAttachment(parameters, conversionContext, chartDataHtml);
            	
            	if (chartImageAttachment != null)
                {
                    chartHtmlBuilder.append(getChartImageHtml(getBooleanParameter(parameters, "thumbnail", false), chartImageAttachment));
            	}
                else
                {
            		String imageFormat = getStringParameter(parameters, "imageformat", "png");
            		if (!isImageFormatSupported(imageFormat))
            		{
            			log.error(String.format("Invalid image format specified: %s", imageFormat));
                        throw new MacroExecutionException(getI18NBean().getText("confluence.extra.chart.chart.error.invalidImageFormat", Arrays.asList(imageFormat)));
            		}
                    DownloadResourceWriter downloadResourceWriter = downloadResourceManager.getResourceWriter(
                            StringUtils.defaultString(AuthenticatedUserThreadLocal.getUsername()),
                            "chart", '.' + imageFormat
                    );

                    OutputStream outputStream = null;
                    try
                    {
                        outputStream = downloadResourceWriter.getStreamForWriting();
                        
                        ImageIO.write(getChartImage(parameters, chartDataHtml), imageFormat, outputStream);

                        int width = getIntegerParameter(parameters, "width", DEFAULT_WIDTH, 0);
                        if (width <= 0)
                            width = DEFAULT_WIDTH;
                        int height = getIntegerParameter(parameters, "height", DEFAULT_HEIGHT, 0);
                        
                        chartHtmlBuilder.append(
                                String.format(
                                        "<img src=\"%s\" width=\"%d\" height=\"%d\">",
                                        downloadResourceWriter.getResourcePath(),
                                        width,
                                        height
                                )
                        );
                    }
                    finally
                    {
                        IOUtils.closeQuietly(outputStream);
                    }
                }
            }
            catch (ParseException parseError)
            {   // error parsing data for the dataset
            	chartHtmlBuilder.append(getErrorPanel(parseError.getMessage()));
            }
            catch (SeriesException seriesError)
            {
            	chartHtmlBuilder.append(getErrorPanel(seriesError.getMessage()));
            }

            if (dataDisplay.equalsIgnoreCase(TRUE) || dataDisplay.equalsIgnoreCase(AFTER))
            {
                // put rendered data on bottom of the image
            	chartHtmlBuilder.append("<br>").append(chartDataHtml);
            }
            
            return chartHtmlBuilder.toString();
        }
        catch (IOException ioError)
        {
            log.error("Unable to generate chart image", ioError);
            throw new MacroExecutionException(ioError);
        }
        catch (XhtmlException xhtmlError)
        {
            log.error("Unable to render macro body to XHTML", xhtmlError);
            throw new MacroExecutionException(xhtmlError);
        }
        catch (XMLStreamException xmlStreamError)
        {
            log.error("Unable to render macro body to XHTML", xmlStreamError);
            throw new MacroExecutionException(xmlStreamError);
        }
        catch (CloneNotSupportedException attachmentCloneError)
        {
            log.error("Unable to process specified attachment", attachmentCloneError);
            throw new MacroExecutionException(attachmentCloneError);
        }
        catch (CannotGenerateThumbnailException thumbnailError)
        {
            log.error("Unable to create thumbnail version of specified attachment", thumbnailError);
            throw new MacroExecutionException(thumbnailError);
        }
    }

    private String getChartImageHtml(boolean thumbnail, Attachment chartImage) throws CannotGenerateThumbnailException
    {
    	StringBuilder chartImageHtml = new StringBuilder("<span class=\"image-wrap\">");

        if (thumbnail && thumbnailManager.isThumbnailable(chartImage))
        {
        	thumbnailManager.getThumbnail(chartImage);
        	ThumbnailInfo thumbnailInfo = thumbnailManager.getThumbnailInfo(chartImage);

            chartImageHtml.append(
                    String.format(
                            "<a class=\"confluence-thumbnail-link\" href=\"%s%s\"><img src=\"%s\" width=\"%d\" height=\"%d\"></a>",
                            settingsManager.getGlobalSettings().getBaseUrl(),
                            chartImage.getDownloadPathWithoutVersion(),
                            thumbnailInfo.getThumbnailUrlPath(),
                            thumbnailInfo.getThumbnailWidth(),
                            thumbnailInfo.getThumbnailHeight()
                    )
            );
        }
        else
        {
        	chartImageHtml.append(String.format("<img src=\"%s%s\">", settingsManager.getGlobalSettings().getBaseUrl(), chartImage.getDownloadPath()));
        }
        
    	return chartImageHtml.append("</span>").toString();
    }

    BufferedImage getChartImage(Map<String, String> parameters, String chartDataHtml)
            throws ParseException, MacroExecutionException, XMLStreamException, XhtmlException
    {
        return getChart(parameters, chartDataHtml).createBufferedImage(
                   getIntegerParameter(parameters, "width", DEFAULT_WIDTH, 0),
                   getIntegerParameter(parameters, "height", DEFAULT_HEIGHT, 0)
        );
    }

    /**
     * Get a chart object that represents the data provided using the parameters specified
     * <p/>
     * Supported parameter keys
     * title       - string
     * subtitle    = string
     * xLabel      - string (also allow xlabel)
     * yLabel      - string (also allow ylabel)
     * type        - pie (default), pie, bar, bar, line, line, area, ...
     * orientation - vertical (default), horizontal
     * legend      - true (default), false
     * stacked     - false (default), true
     * 3d          - false (default), true
     * dataOrientation - horizontal false (default), true (also allow dataorientation)
     * opacity     - how opaque bars are in area and bar charts (0.0 (fully transparent) to 1.0 (fully opaque))
     * colors      - comma separated list of colors for pie and area sections, series, etc...
     * bgColor     - color
     * borderColor - color
     * ...axis...  - axis customization
     * dateFormat  - format for input of date information for a time series
     * timePeriod  - Day, Month, Year, etc...
     * timeSeries  - false (default) or true
     * showShapes  - true (default) or false
     * imageType   - png (default) or jpg or other
     */
    JFreeChart getChart(Map<String, String> parameters, String rendered) throws ParseException, MacroExecutionException
    {

        // String parameters where null is ok
        String title = parameters.get("title");
        String xLabel = parameters.get("xlabel");
        String yLabel = parameters.get("ylabel");
        String opacity = parameters.get("opacity");
        String bgColor = getStringParameter(parameters, "bgcolor", "white"); // default to white background to be Confluence friendly
        String borderColor = parameters.get("bordercolor");

        // String parameters
        String type = getStringParameter(parameters, "type", PIE_TYPE);    // default to PIE chart
        String subTitle = getStringParameter(parameters, "subtitle", "");  // default to no subtitle

        // boolean parameters
        boolean legend = getBooleanParameter(parameters, "legend", true); // default to show legend unless legend=false
        boolean is3d = getBooleanParameter(parameters, "3d", false); // default to NOT 3D unless 3d=true OR 3D=true
        boolean stacked = getBooleanParameter(parameters, "stacked", false); // default to NOT stacked unless stacked=true
        // default to NOT use a time series unless it is a time series chart or timeSeries=true        
        boolean timeSeries = getBooleanParameter(parameters, "timeseries", false);
        boolean showShapes = getBooleanParameter(parameters, "showshapes", true); // default to show shapes in line charts
        boolean tooltips = false;  // not a parameter - probably not possible to do 
        boolean urls = false;      // not a parameter - probably not possible to do
        boolean forgive = getBooleanParameter(parameters, "forgive", true);  // forgive data irregularities        

        // default to vertical orientation unless orientation=horizontal
        PlotOrientation plotOrientation = PlotOrientation.VERTICAL;
        if (StringUtils.equalsIgnoreCase(parameters.get("orientation"), HORIZONTAL))
            plotOrientation = PlotOrientation.HORIZONTAL;

        String xTableNumber = getStringParameter(parameters, "tableNumber", ""); // depreciated, remains for compatibility with 1.6 release
        String tables = getStringParameter(parameters, "tables", xTableNumber);  // comma separated list of table ids or table numbers (1-based)
        String columns = getStringParameter(parameters, "columns", "");  // comma separated list of column names or column numbers (1-based)

        String language = getStringParameter(parameters, "language", ""); // 2 char lower case, see http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes
        String country = getStringParameter(parameters, "country", "");   // 2 char upper case, see http://en.wikipedia.org/wiki/ISO_3166-1_alpha-2

        ChartData chartData = new ChartData(rendered, tables, columns, forgive);

        // make sure this gets set before locales are added so time/date order respects this setting
        chartData.setTimePeriod(getStringParameter(parameters, "timeperiod", "Day"));  // default to Day time period

        // ChartData uses default date format unless dataFormat is provided
        if (!StringUtils.isEmpty(parameters.get("dateformat")))
        {
            chartData.addDateFormat(new SimpleDateFormat(parameters.get("dateformat")));
        }

        // Locales provide additional data and number format parsing capabilities
        setupLocales(chartData, language, country);

        // default to have columns represent the domain or x axis values unless dataOrientation=vertical
        if (!StringUtils.isEmpty(parameters.get("dataorientation")))
        {
            chartData.setVerticalDataOrientation(VERTICAL.equalsIgnoreCase(parameters.get("dataorientation")));
        }

        JFreeChart chart;

        if (PIE_TYPE.equalsIgnoreCase(type))
        {
            DefaultPieDataset dataset = new DefaultPieDataset();
            chartData.processData(dataset);
            chart = ConfluenceChartFactory.createPieChart(title, dataset, legend, tooltips, urls, is3d);
        }
        else if (BAR_TYPE.equalsIgnoreCase(type))
        {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            chartData.processData(dataset);
            chart = ConfluenceChartFactory.createBarChart(title, xLabel, yLabel, dataset, plotOrientation, legend,
                tooltips, urls, is3d, stacked);
            if (is3d && opacity == null)
            {
                opacity = "100";    // default opacity for 3d charts
            }
        }
        else if (AREA_TYPE.equalsIgnoreCase(type))
        {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            chartData.processData(dataset);
            chart = ConfluenceChartFactory.createAreaChart(title, xLabel, yLabel, dataset, plotOrientation, legend,
                tooltips, urls, stacked);

            if (!stacked && opacity == null)
            {
                opacity = "50";    // default opacity for non-stacked area charts
            }
        }
        else if (LINE_TYPE.equalsIgnoreCase(type))
        {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            chartData.processData(dataset);
            chart = ConfluenceChartFactory.createLineChart(title, xLabel, yLabel, dataset, plotOrientation, legend,
                tooltips, urls, is3d, showShapes);
        }
        else if (XYLINE_TYPE.equalsIgnoreCase(type))
        {
            XYDataset dataset;
            if (timeSeries)
            {
                dataset = new TimeSeriesCollection();
            }
            else
            {
                dataset = new XYSeriesCollection();
            }
            chartData.processData(dataset);
            chart = ConfluenceChartFactory.createXYLineChart(title, xLabel, yLabel, dataset, plotOrientation, legend,
                tooltips, urls);
        }
        else if (XYAREA_TYPE.equalsIgnoreCase(type))
        {

            if (stacked)
            {   // not supported yet
                DefaultTableXYDataset dataset = new DefaultTableXYDataset();
                chartData.processData(dataset);
                chart = ConfluenceChartFactory.createStackedXYAreaChart(title, xLabel, yLabel, dataset, plotOrientation,
                    legend, tooltips, urls);
            }
            else
            {
                XYDataset dataset;
                if (timeSeries)
                {
                    dataset = new TimeSeriesCollection();
                }
                else
                {
                    dataset = new XYSeriesCollection();
                }
                chartData.processData(dataset);
                chart = ConfluenceChartFactory.createXYAreaChart(title, xLabel, yLabel, dataset, plotOrientation,
                    legend, tooltips, urls);
            }
        }
        else if (XYBAR_TYPE.equalsIgnoreCase(type))
        {
            XYDataset dataset;
            if (timeSeries)
            {
                dataset = new TimeSeriesCollection();
            }
            else
            {
                dataset = new XYSeriesCollection();
            }
            chartData.processData(dataset);
            chart = ConfluenceChartFactory.createXYBarChart(title, xLabel, timeSeries, yLabel,
                (IntervalXYDataset) dataset, plotOrientation, legend, tooltips, urls);
        }
        else if (XYSTEP_TYPE.equalsIgnoreCase(type))
        {
            XYDataset dataset;
            if (timeSeries)
            {
                dataset = new TimeSeriesCollection();
            }
            else
            {
                dataset = new XYSeriesCollection();
            }
            chartData.processData(dataset);
            chart = ConfluenceChartFactory.createXYStepChart(title, xLabel, yLabel, dataset, plotOrientation, legend,
                tooltips, urls);
        }
        else if (XYSTEPAREA_TYPE.equalsIgnoreCase(type))
        {
            XYDataset dataset;
            if (timeSeries)
            {
                dataset = new TimeSeriesCollection();
            }
            else
            {
                dataset = new XYSeriesCollection();
            }
            chartData.processData(dataset);
            chart = ConfluenceChartFactory.createXYStepAreaChart(title, xLabel, yLabel, dataset, plotOrientation,
                legend, tooltips, urls);
        }
        else if (SCATTER_TYPE.equalsIgnoreCase(type))
        {
            XYDataset dataset;
            if (timeSeries)
            {
                dataset = new TimeSeriesCollection();
            }
            else
            {
                dataset = new XYSeriesCollection();
            }
            chartData.processData(dataset);
            chart = ConfluenceChartFactory.createScatterPlot(title, xLabel, yLabel, dataset, plotOrientation, legend,
                tooltips, urls);
        }
        else if (TIMESERIES_TYPE.equalsIgnoreCase(type))
        {
            TimeSeriesCollection dataset = new TimeSeriesCollection();
            chartData.processData(dataset);
            chart = ConfluenceChartFactory.createTimeSeriesChart(title, xLabel, yLabel, dataset, legend, tooltips,
                urls);
        }
        else if (GANTT_TYPE.equalsIgnoreCase(type) && ChartUtil.isVersion103Capable())
        {
            chartData.setVerticalDataOrientation(true);
            IntervalCategoryDataset dataset = new TaskSeriesCollection();
            chartData.processData(dataset);
            chart = ConfluenceChartFactory.createGanttChart(title, xLabel, yLabel, dataset, legend, tooltips, urls);
        }
        else
            throw new MacroExecutionException("Unsupported chart type: " + type);

        chart.setBackgroundPaint(stringToColor(bgColor));
        chart.addSubtitle(new TextTitle(subTitle));

        if (borderColor != null)
        {
            chart.setBorderPaint(stringToColor(borderColor));
            chart.setBorderVisible(true);
        }

        Plot plot = chart.getPlot();

        if (plot instanceof PiePlot)
        {
            handlePiePlotCustomization(parameters, (PiePlot) plot);
        }
        else if ((plot instanceof XYPlot) && timeSeries)
        {
            ((XYPlot) plot).setDomainAxis(new DateAxis(
                xLabel)); // necessary so that customized axis works properly for time series data
        }

        handleAxisCustomization(parameters, plot, chartData);
        handleOpacityCustomization(opacity, plot);
        handleColorCustomization(parameters, plot);

        return chart;
    }

    /**
     * Handle pie plot customization
     * - get customization parameters from parameter Map and modify plot accordingly
     * @param parameters  macro parameters
     * @param plot  chart plot
     * @throws MacroExecutionException
     */
    void handlePiePlotCustomization(Map parameters, PiePlot plot) throws MacroExecutionException {
        // Pie section label customization
    	String pieSectionLabel = getStringParameter(parameters, "piesectionlabel", "%0%") // default to just show key
                                 .replaceAll("%0%", "\\{0\\}")  // convert to what jfreechart uses - unfortunately {and } cause problems in parameters
                                 .replaceAll("%1%", "\\{1\\}")
                                 .replaceAll("%2%", "\\{2\\}");
    	plot.setLabelGenerator(new StandardPieSectionLabelGenerator(pieSectionLabel));

    	// Pie section explode - just simple to start with, requires jfreechart 1.0.3 or above
    	// - need a comma separated list of key=value pairs to support fully

    	if (ChartUtil.isVersion103Capable()) {
    		String pieSectionExplode = getStringParameter(parameters, "piesectionexplode", ""); // default to no key explode
    		String explodeList[] = pieSectionExplode.split(",");

            for (int i=0; i < explodeList.length; i++) {
                if (StringUtils.isBlank(explodeList[i])) continue; /* Should skip if trimming string is blank */

                try {
                    plot.setExplodePercent(explodeList[i].trim(), DEFAULT_PIE_EXPLODE);
                }
                catch (Exception exception) {
                    log.debug("Ignore errors");
                }
            }
    	}
    }

    /**
     * Handle axis customization requested.
     * - get customization parameters from parameter Map and modify plot accordingly
     * @param parameters  macro parameters
     * @param plot  chart plot
     * @param chartData  for time series data
     * @throws MacroExecutionException

     RegularTimePeriod.createInstance(timePeriodClass, dateFormat.parse(key), TimeZone.getDefault())

     */
    void handleAxisCustomization(Map parameters, Plot plot, ChartData chartData) throws MacroExecutionException {

        if (plot instanceof CategoryPlot) {
            handleCategoryAxisCustomization(parameters, ((CategoryPlot) plot).getDomainAxis(), chartData);
            handleValueAxisCustomization(parameters, ((CategoryPlot) plot).getRangeAxis(), chartData, "range");

        } else if (plot instanceof XYPlot) {
            handleValueAxisCustomization(parameters, ((XYPlot) plot).getDomainAxis(), chartData, "domain");
            handleValueAxisCustomization(parameters, ((XYPlot) plot).getRangeAxis(),  chartData, "range");
        }
    }

    /**
     * Handle category axis customization
     * - get customization parameters from parameter list and modify accordingly
     */
    private void handleCategoryAxisCustomization(Map parameters, CategoryAxis axis, ChartData chartData) throws MacroExecutionException {

        String categoryLabelPosition = "STANDARD";
        if (!StringUtils.isEmpty((String) parameters.get("categorylabelposition"))) {
            categoryLabelPosition = (String) parameters.get("categorylabelposition");
        }
    	if (categoryLabelPosition.equalsIgnoreCase("up45")) {
            axis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        } else if (categoryLabelPosition.equalsIgnoreCase("up90")) {
            axis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        } else if (categoryLabelPosition.equalsIgnoreCase("down45")) {
            axis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        } else if (categoryLabelPosition.equalsIgnoreCase("down90")) {
            axis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);
        } //else {   // more control can be done if necessary          	
          //CategoryLabelPositions.createUpRotationLabelPositions(Math.toRadians(angle));
          //CategoryLabelPositions.createDownRotationLabelPositions(Math.toRadians(angle));
          //}
    }

    /**
     * Handle value axis customization
     * - get customization parameters from parameter list and modify accordingly
     * - qualifier is either "domain" or "range"
     */
    private void handleValueAxisCustomization(Map parameters, ValueAxis axis, ChartData chartData, String qualifier) throws MacroExecutionException {

        Double axisLabelAngle = getDoubleParameter(parameters, qualifier + "axislabelangle", null);
        if (axisLabelAngle != null) {
        	axis.setLabelAngle(Math.toRadians(axisLabelAngle.doubleValue()));
        }
        boolean axisRotateTickLabel = getBooleanParameter(parameters, qualifier + "axisrotateticklabel", false);
        axis.setVerticalTickLabels(axisRotateTickLabel);

        if (axis instanceof DateAxis) {
        	handleDateAxisCustomization(parameters, (DateAxis) axis, chartData, qualifier);
        } else {
            Double axisLowerBound = getDoubleParameter(parameters, qualifier + "axislowerbound", null);
            Double axisUpperBound = getDoubleParameter(parameters, qualifier + "axisupperbound", null);
            Double axisTickUnit = getDoubleParameter(parameters, qualifier + "axistickunit", null);
        	if (axisLowerBound != null) {
        		axis.setLowerBound(axisLowerBound.doubleValue());
        	}
        	if (axisUpperBound != null) {
        		axis.setUpperBound(axisUpperBound.doubleValue());
        	}
        	if ((axisTickUnit != null) && (axis instanceof NumberAxis)) {
        		((NumberAxis) axis).setTickUnit(new NumberTickUnit(axisTickUnit.doubleValue()));
        	}
        }
    }

    /**
     * Handle date axis customization
     * - get customization parameters from parameter list and modify accordingly
     * - qualifier is either "domain" or "range"
     */
    private void handleDateAxisCustomization(Map parameters, DateAxis axis, ChartData chartData, String qualifier) throws MacroExecutionException {

        axis.setDateFormatOverride(chartData.getDateFormat(0));
        String axisLowerBound = getStringParameter(parameters, qualifier + "axislowerbound", null);
        String axisUpperBound = getStringParameter(parameters, qualifier + "axisupperbound", null);
        String axisTickUnit = getStringParameter(parameters, qualifier + "axistickunit", null);
    	String dateTickMarkPosition = getStringParameter(parameters, "datetickmarkposition", null);

        if (axisLowerBound != null) {
            try {
            	axis.setMinimumDate(chartData.toDate(axisLowerBound));
            }
            catch (ParseException exception) {
                throw new MacroExecutionException("Invalid date format for " + qualifier + "AxisLowerBound parameter: " + axisLowerBound);
                //log.debug(qualifier + "AxisLowerBound format error: " + qualifer + "AxisLowerBound");
            }
        }
        if (axisUpperBound != null) {
            try {
            	axis.setMaximumDate(chartData.toDate(axisUpperBound));
            }
            catch (ParseException ignore) {
                throw new MacroExecutionException("Invalid date format for " + qualifier + "AxisUpperBound parameter: " + axisUpperBound);
                //log.debug(qualifier + "AxisUpperBound format error: " + qualifier + "AxisUpperBound");
            }
        }
        if (axisTickUnit != null) {
            setDateTick(parameters, axis, axisTickUnit);
        }
        if (dateTickMarkPosition != null) {
          	if      (START .equalsIgnoreCase(dateTickMarkPosition)) { axis.setTickMarkPosition(DateTickMarkPosition.START);  }
            else if (MIDDLE.equalsIgnoreCase(dateTickMarkPosition)) { axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE); }
            else if (END   .equalsIgnoreCase(dateTickMarkPosition)) { axis.setTickMarkPosition(DateTickMarkPosition.END);    }
        }
    }

    /**
     * Handle opacity customization requested.
     * - get customization parameters from parameter Map and modify plot accordingly
     * @param opacity value to set to the chart
     * @param plot  chart plot
     * @throws MacroExecutionException
     */
    void handleOpacityCustomization(String opacity, Plot plot) throws MacroExecutionException {

        if (opacity != null) {
            try {
                Integer iOpacity = new Integer(opacity);
                if ((iOpacity.intValue() < 0) || (iOpacity.intValue() > 100)) {
                    throw new MacroExecutionException("opacity parameter value '" + opacity + "' not between 0 and 100");
                }
                plot.setForegroundAlpha(iOpacity.floatValue()/100);
            }
            catch (NumberFormatException exception) {     // error out on illegal integer value
                throw new MacroExecutionException("opacity parameter value '" + opacity + "' not a number between 0 and 100");
            }
        }
    }

    /**
     * Handle color customization requested.
     * - get customization parameters from parameter Map and modify plot accordingly
     * @param parameters  macro parameters
     * @param plot  chart plot
     * @throws MacroExecutionException
     */
    void handleColorCustomization(Map parameters, Plot plot) throws MacroExecutionException {

        String colors = (String) parameters.get("colors");
        if (colors != null) {
            String[] color = colors.split(",");
            for (int i = 0; i < color.length; i++) {
                if (plot instanceof CategoryPlot) {
                    ((CategoryPlot) plot).getRenderer().setSeriesPaint(i, stringToColor(color[i]));
                } else if (plot instanceof XYPlot) {
                    ((XYPlot) plot).getRenderer().setSeriesPaint(i, stringToColor(color[i]));
                } else if (plot instanceof PiePlot) {
                    ((PiePlot) plot).setSectionPaint(i, stringToColor(color[i]));
                }
            }
        }
    }

    /* 
     * Use http://www.answers.com/topic/web-colors for values
     *
     * Color 	Hexadecimal	Color 	Hexadecimal Color 	Hexadecimal	Color 	Hexadecimal
     *
     * black 	#000000 	silver 	#c0c0c0 	maroon 	#800000 	red 	#ff0000
     * navy 	#000080 	blue 	#0000ff 	purple 	#800080 	fuchsia #ff00ff
     * green 	#008000 	lime 	#00ff00 	olive 	#808000 	yellow 	#ffff00
     * teal 	#008080 	aqua 	#00ffff 	gray 	#808080 	white 	#ffffff
     */
    Color stringToColor(String colorName) throws MacroExecutionException
    {
        String colorNameTrimmed = StringUtils.trim(StringUtils.lowerCase(colorName));
        if (StringUtils.isBlank(colorNameTrimmed))
            return null;

        int colorValue;
        if (COLOR_MAP.containsKey(colorNameTrimmed))
        {
            colorValue = COLOR_MAP.get(colorNameTrimmed);
        }
        else
        {
            try
            {
                if (!StringUtils.startsWith(colorNameTrimmed, "#") || colorNameTrimmed.length() <= 1)
                {
                    throw new NumberFormatException(String.format("Invalid custom color specified %s", colorNameTrimmed));
                }
                else
                {
                    colorValue = Integer.parseInt(colorNameTrimmed.substring(1), 16);
                }
            }
            catch (NumberFormatException notHexValue)
            {
                throw new MacroExecutionException(String.format("Invalid color %s", colorNameTrimmed), notHexValue);
            }
        }

        return new Color(colorValue);
    }

    /**
     * Set date tick unit from string
     * - format is number or number followed by one of:
     *   - u - microseconds
     *   - s - seconds
     *   - m - minutes
     *   - h - hours
     *   - d - days
     *   - M - months
     *   - y - years
     * - examples
     *      10, 10s, 10m, 10h, 10d,
     * - data after an s, m, h or d is ignored, so the following are also valid
     *      10days, 10hours, etc...
     * - invalid numeric value results in the default being used
     */
     void setDateTick(Map parameters, DateAxis axis, String tick) throws MacroExecutionException {

    	 char[] timeChars = {'u', 's', 'm', 'h', 'd', 'M', 'y'};  // case sensitive!
         String value;
         int findAt = StringUtils.indexOfAny(tick, timeChars); // needs commons lang version 2
//         int findAt = indexOfAny(tick, timeChars);
         if (findAt < 0) {
             value = tick;
         } else {
             value = tick.substring(0, findAt);
         }
         int count = 0;
         try {
             count = Integer.parseInt(value.trim());
         }
         catch (NumberFormatException ignore) {
             throw new MacroExecutionException("Invalid format for date axis tick unit: " + tick);
             //log.debug("Axis tick format error: " + tick);
         }
         int unit = -1;  // default - do not change
         if (findAt >= 0) {
        	 if      (tick.charAt(findAt) == 'y') { unit = DateTickUnit.YEAR; }
        	 else if (tick.charAt(findAt) == 'M') { unit = DateTickUnit.MONTH; }
        	 else if (tick.charAt(findAt) == 'd') { unit = DateTickUnit.DAY; }
        	 else if (tick.charAt(findAt) == 'h') { unit = DateTickUnit.HOUR; }
        	 else if (tick.charAt(findAt) == 'm') { unit = DateTickUnit.MINUTE; }
        	 else if (tick.charAt(findAt) == 's') { unit = DateTickUnit.SECOND; }
        	 else if (tick.charAt(findAt) == 'u') { unit = DateTickUnit.MILLISECOND; }
        	 else count = 0;
         } else {  // just a number, use the timePeriod setting to determine units
             String timePeriod = getStringParameter(parameters, "timeperiod", "Day"); // default to Day time period
             if      (timePeriod.equalsIgnoreCase("year"))        { unit = DateTickUnit.YEAR;        }
        	 else if (timePeriod.equalsIgnoreCase("quarter"))     { unit = DateTickUnit.MONTH; count = count * 3; }
             else if (timePeriod.equalsIgnoreCase("month"))       { unit = DateTickUnit.MONTH;       }
        	 else if (timePeriod.equalsIgnoreCase("day"))         { unit = DateTickUnit.DAY;         }
        	 else if (timePeriod.equalsIgnoreCase("week"))        { unit = DateTickUnit.DAY;   count = count * 7;}
        	 else if (timePeriod.equalsIgnoreCase("hour"))        { unit = DateTickUnit.HOUR;        }
        	 else if (timePeriod.equalsIgnoreCase("minute"))      { unit = DateTickUnit.MINUTE;      }
        	 else if (timePeriod.equalsIgnoreCase("second"))      { unit = DateTickUnit.SECOND;      }
        	 else if (timePeriod.equalsIgnoreCase("millisecond")) { unit = DateTickUnit.MILLISECOND; }
        	 else count = 0;
         }
         if (count > 0) {
             axis.setTickUnit(new DateTickUnit(unit, count));
         }
         //log.debug("Count: " + count + " unit: " + unit);
         return;
     }


     /**
      * Re-implement StringUtils function until Atlassian steps up to 2.0 level of commons lang
      */
     /*protected int indexOfAny(String string, char[] chars) {
         int index = -1;
         for (int i = 0; i < chars.length; i++) {
             int currentIndex = string.indexOf(chars[i]);
             if (index < 0) {
                 index = currentIndex;
             }
             else if (currentIndex >= 0) {
                 index = Math.min(index, currentIndex);
             }
         }
         return index;
     }*/

	/**
	 * Get attachment if exporting to an attachment
	 * @throws XhtmlException 
	 * @throws XMLStreamException 
     */
	Attachment getAttachment(Map<String, String> parameters, ConversionContext conversionContext, String chartDataHtml)
            throws ParseException, MacroExecutionException, XMLStreamException, XhtmlException, IOException, CloneNotSupportedException
	{
        String attachmentLink = getStringParameter(parameters, "attachment", null);
        if (StringUtils.isNotBlank(attachmentLink))
        {
            int indexOfCaret = attachmentLink.indexOf('^');
            if (indexOfCaret == -1)
                attachmentLink = new StringBuilder("^").append(attachmentLink).toString();

            Link aLink = linkResolver.createLink(conversionContext.getEntity().toPageContext(), attachmentLink);
            ContentEntityObject theAttachmentContentEntity;
            String imageFormat = getStringParameter(parameters, "imageformat", "png");
            
            if (aLink instanceof AttachmentLink)
            {
                User currentUser = AuthenticatedUserThreadLocal.getUser();
                Attachment theAttachment = ((AttachmentLink) aLink).getAttachment();
                String theAttachmentFileName = theAttachment.getFileName();
                String attachmentVersion = getStringParameter(parameters, "attachmentversion", NEW);
                
                if (StringUtils.equals(KEEP, attachmentVersion))
                {
                    return theAttachment;
                }
                else
                {
                    theAttachmentContentEntity = theAttachment.getContent();
                    if (StringUtils.equals(NEW, attachmentVersion) && !permissionManager.hasCreatePermission(currentUser, theAttachmentContentEntity, Attachment.class))
                        throw new MacroExecutionException(String.format("Export not valid. Not authorized to add %s from page: %s (%d)", theAttachmentFileName, theAttachmentContentEntity.getTitle(), theAttachmentContentEntity.getId()));

                    if (StringUtils.equals(REPLACE, attachmentVersion)
                            && !(permissionManager.hasPermission(currentUser, Permission.REMOVE, theAttachment) && permissionManager.hasCreatePermission(currentUser, theAttachmentContentEntity, Attachment.class)))
                        throw new MacroExecutionException(String.format("Export not valid. Not authorized to recreate %s from page: %s (%d)", theAttachmentFileName, theAttachmentContentEntity.getTitle(), theAttachmentContentEntity.getId()));

                    if (StringUtils.equals(REPLACE, attachmentVersion))
                    {
                        attachmentManager.removeAttachmentFromServer(theAttachment);
                        theAttachment = null;
                    }

                    return saveChartImageAsAttachment(
                            theAttachmentContentEntity,
                            new StringBuilder("image/").append(imageFormat).toString(),
                            theAttachmentFileName,
                            getChartAsByteArray(getChartImage(parameters, chartDataHtml), imageFormat),
                            getStringParameter(parameters, "attachmentcomment", null),
                            theAttachment
                    );
                }
            }
            else if (aLink instanceof UnpermittedLink)
            {
                throw new MacroExecutionException("Export not valid. Not authorized to view specified attachment");
            }
            else if (aLink instanceof UnresolvedLink)
            {
                indexOfCaret = attachmentLink.indexOf('^');
                if (indexOfCaret >= 0 && indexOfCaret < attachmentLink.length() - 1)
                {
                    theAttachmentContentEntity = conversionContext.getEntity();
                    if (indexOfCaret > 0)
                    {
                        Link contentEntityLink = linkResolver.createLink(conversionContext.getEntity().toPageContext(), attachmentLink.substring(0, indexOfCaret));
                        if (contentEntityLink instanceof UnpermittedLink)
                            throw new MacroExecutionException("Export not valid. Not authorized to view specified attachment");
                        else if ((contentEntityLink instanceof PageLink) || (contentEntityLink instanceof BlogPostLink))
                            theAttachmentContentEntity = ((AbstractPageLink) contentEntityLink).getDestinationContent();
                    }

                    return saveChartImageAsAttachment(
                            theAttachmentContentEntity,
                            new StringBuilder("image/").append(imageFormat).toString(),
                            attachmentLink.substring(indexOfCaret + 1),
                            getChartAsByteArray(getChartImage(parameters, chartDataHtml), imageFormat),
                            null,
                            null
                    );
                }
                else
                {
                    throw new MacroExecutionException(String.format("Invalid attachment link %s", attachmentLink));
                }
            }
        }

        return null;
	}

    private Attachment saveChartImageAsAttachment(ContentEntityObject attachmentContent, String attachmentMimeType, String attachmentFileName, byte[] chartImageBytes, String comment, Attachment currentVersionOfAttachment) throws CloneNotSupportedException, IOException
    {
        Attachment previousVersionOfAttachment = (Attachment) (null != currentVersionOfAttachment && currentVersionOfAttachment.isPersistent() ? currentVersionOfAttachment.clone() : null);
        Attachment chartImageAttachment = null == previousVersionOfAttachment ? new Attachment() : currentVersionOfAttachment;
        chartImageAttachment.setContent(attachmentContent);
        chartImageAttachment.setContentType(attachmentMimeType);
        chartImageAttachment.setComment(comment);
        chartImageAttachment.setFileName(attachmentFileName);
        chartImageAttachment.setFileSize(chartImageBytes.length);

        attachmentManager.saveAttachment(chartImageAttachment, previousVersionOfAttachment, new ByteArrayInputStream(chartImageBytes));
        return chartImageAttachment;
    }


    private byte[] getChartAsByteArray(RenderedImage image, String imageFormat) throws IOException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try
        {
            ImageIO.write(image, imageFormat, outputStream);
            return outputStream.toByteArray();
        }
        finally
        {
            IOUtils.closeQuietly(outputStream);
        }
    }


    /**
     * Setup locals for date and number parsing
     * - first add the specific user input if provided
     * - next add the Confluence global default
     * - next add all the other locales defined to Confluence
     * - Note that duplicates are removed by the chartData function
     */
    void setupLocales(ChartData chartData, String language, String country) {
    	if (!language.trim().equals("") || !country.trim().equals("")) {  // is something specified, use it
    		chartData.addLocale(new Locale(language, country));
    	}
    	LocaleUtils localeUtils = new LocaleUtils();
    	chartData.addLocale(localeUtils.getLocale(settingsManager.getGlobalSettings().getGlobalDefaultLocale()));

    	List list = languageManager.getLanguages();
		//log.debug("installed languages: " + list.size());
		Iterator iterator = list.iterator();
		while (iterator.hasNext()) {
			chartData.addLocale(((Language) iterator.next()).getLocale());
		}
    }

    String getErrorPanel(String message)
    {
        return RenderUtils.blockError(message, "");
    }

    Map<String, String> toLowerCase(Map<String, String> params) {
        Map<String, String> paramsWithLowerCasedKeys = new HashMap<String, String>(params.size());
        for (Map.Entry<String, String> paramValue : params.entrySet())
            paramsWithLowerCasedKeys.put(StringUtils.lowerCase(paramValue.getKey()), paramValue.getValue());

        return paramsWithLowerCasedKeys;
    }

	// force integer parameter to default if less than lower bound
    int getIntegerParameter(Map parameters, String param, int def, int lowerBound) throws MacroExecutionException {
        int result = getIntegerParameter(parameters, param, new Integer(def)).intValue();
        if (result < lowerBound) {
        	result = def;
        }
        return result;
    }

//	private int getIntegerParameter(Map parameters, String param, int def) throws MacroExecutionException {
//      return getIntegerParameter(parameters, param, new Integer(def)).intValue();
//  }

    Integer getIntegerParameter(Map parameters, String param, Integer def) throws MacroExecutionException {
        Integer result = def;
        if (!StringUtils.isEmpty((String) parameters.get(param))) {
            try {
                result = new Integer((String) parameters.get(param));
            }
            catch (NumberFormatException exception) {
                throw new MacroExecutionException("Invalid " + param + " parameter.  It must be an integer.");
            }
        }
        return result;
    }

    Double getDoubleParameter(Map parameters, String param, Double def) throws MacroExecutionException {
        Double result = def;
        if (!StringUtils.isEmpty((String) parameters.get(param))) {
            try {
                result = new Double((String) parameters.get(param));
            }
            catch (NumberFormatException exception) {
                throw new MacroExecutionException("Invalid " + param + " parameter.  It must be an double value.");
            }
        }
        return result;
    }

    String getStringParameter(Map parameters, String param, String def) {
        String result = def;
        if (!StringUtils.isEmpty((String) parameters.get(param))) {
            result = (String) parameters.get(param);
        }
        return result;
    }
    /**
     * Return boolean based on the rule.  Default unless explicitly equal to the none default case
     * (as we documented in the help text!)
     * Ignores any error input - goes to default
     */
    boolean getBooleanParameter(Map parameters, String param, boolean def) {
    	boolean result;
    	String value = (String) parameters.get(param);
        if (   (value != null)
        	&& value.equalsIgnoreCase(def ? FALSE : TRUE)) {
            result = !def;
        } else {
           result = def;
        }
        return result;
    }
    
    private boolean isImageFormatSupported(String imageFormat)
    {
    	String writerNames[] = ImageIO.getWriterFormatNames();
    	for(String writerFormat : writerNames)
    	{
    		if (writerFormat.equalsIgnoreCase(imageFormat))
    		{
    			return true;
    		}
    	}
    	return false;
    }

    private I18NBean getI18NBean()
    {
        return i18nBeanFactory.getI18NBean(localeManager.getLocale(AuthenticatedUserThreadLocal.getUser()));
    }
    
}

