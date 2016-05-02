package com.jeasonzhao.report.engine.impl;

import java.io.IOException;

import com.jeasonzhao.commons.logger.Logger;
import com.jeasonzhao.commons.utils.Algorithms;
import com.jeasonzhao.commons.utils.ResourceHelper;
import com.jeasonzhao.report.codes.HorizonAligns;
import com.jeasonzhao.report.dataset.DataCell;
import com.jeasonzhao.report.dataset.DataSet;
import com.jeasonzhao.report.dataset.RowInfo;
import com.jeasonzhao.report.dataset.RowInfoCollection;
import com.jeasonzhao.report.engine.irp.IChartRender;
import com.jeasonzhao.report.engine.irp.IReportRender;
import com.jeasonzhao.report.exceptions.ChartException;
import com.jeasonzhao.report.exceptions.RenderException;
import com.jeasonzhao.report.model.Report;
import com.jeasonzhao.report.model.ReportColumn;
import com.jeasonzhao.report.model.ReportColumnCollection;
import com.jeasonzhao.report.msg.MessageCodes;
import com.jeasonzhao.web.IHtmlWriter;
import com.jeasonzhao.report.exceptions.DictException;

public class BasicHtmlRender implements IReportRender
{
    private static final String CSSPREFIX = "SQLREPORT";

    private static final String CSS_TITLE = "title";
    private static final String CSS_SUBTITLE = CSSPREFIX + "subtitle";

    private static final String CSS_PARAMETRSECTION = CSSPREFIX + "PARAMETERSECTION";
//    private static final String CSS_FORM = CSSPREFIX + "PARAMETERFORM";

    private static final String CSS_DATA_TABLE = "datatable";
    private static final String CSS_DATA_ROWNUM = "rownum";
    private static final String CSS_DATA_SUBTOTAL = "subtotal";
    private static final String CSS_DATA_SUMMARY = "summary";
    private static final String CSS_RIGHTALIGN = "right";
    private static final String CSS_CENTERALIGN = "center";
    private static final String CSS_EXCEPTION = "exception";

//    private static final String CSS_DATA_TABLE=CSSPREFIX+"datatable";




//    private static final String CSS_EXCEPTION = "exception";

    private IHtmlWriter m_writer = null;
    private IChartRender m_chartRender = null;
    private boolean m_isShowTitle = true;
    private String m_strCssFileName = null;
    private boolean m_isWellFormated = true;
    private String m_strCSSString = null;
    private String m_strJavaScriptNamespace = null;

    public BasicHtmlRender(IHtmlWriter writer,IChartRender chartRender)
    {
        m_writer = writer;
        m_chartRender = chartRender;
        m_strCSSString = ResourceHelper.stringFromClassPath(BasicHtmlRender.class,"SqlReport.css");
        if(null != m_strCSSString)
        {
            m_strCSSString = m_strCSSString
                .replaceAll("%CSS_TITLE%",CSS_TITLE)
                .replaceAll("%CSS_DATA_TABLE%",CSS_DATA_TABLE)
                .replaceAll("%CSS_DATA_ROWNUM%",CSS_DATA_ROWNUM)
                .replaceAll("%CSS_DATA_SUBTOTAL%",CSS_DATA_SUBTOTAL)
                .replaceAll("%CSS_DATA_SUMMARY%",CSS_DATA_SUMMARY)
                .replaceAll("%CSS_RIGHTALIGN%",CSS_RIGHTALIGN)
                .replaceAll("%CSS_CENTERALIGN%",CSS_CENTERALIGN)
                ;
            m_strCSSString = "<style>" + m_strCSSString + "</style>";
        }
    }

    public BasicHtmlRender setShowTitle(boolean isShowTitle)
    {
        this.m_isShowTitle = isShowTitle;
        return this;
    }

    public BasicHtmlRender setCssFileName(String strFileName)
    {
        this.m_strCssFileName = strFileName;
        return this;
    }

    public BasicHtmlRender setJavaScriptNamespace(String str)
    {
        this.m_strJavaScriptNamespace = str;
        return this;
    }

    public String getJavaScriptNamespace()
    {
        return m_strJavaScriptNamespace;
    }

    public String scriptFunctionName(String strFunctionName)
    {
        return Algorithms.isEmpty(m_strJavaScriptNamespace) ? strFunctionName : (m_strJavaScriptNamespace + "." + strFunctionName);
    }

    public String getCssFileName()
    {
        return this.m_strCssFileName;
    }

    public boolean isShowTitle()
    {
        return this.m_isShowTitle;
    }

    public void renderDataset(Logger log,Report report,DataSet dataset)
        throws RenderException
    {
        if(null == report)
        {
            this.renderException(log,null,new RenderException.NoReport());
            return;
        }
        write(m_strCSSString);
        renderTitle(report);
        renderCharts(log,report,dataset);
        renderRows(report,dataset);
    }

    private void renderRows(Report report,DataSet dataset)
    {
        if(null == report || report.isHideResultTable() || dataset == null || dataset.isEmpty())
        {
            return;
        }
        if(Algorithms.notEmpty(dataset.getHtmlHeader()))
        {

        }
        writeln("<table cellspacing=\"0\" cellpadding=\"0\" class=\"" + CSS_DATA_TABLE + "\">");
        renderDataSetHeader(report,dataset);
        renderDataSetBody(report,dataset);
        writeln("</table>");
        if(Algorithms.notEmpty(dataset.getHtmlFooter()))
        {

        }
    }

    private void renderDataSetHeader(Report report,DataSet dataset)
    {
        writeln("<thead>");
        renderRows(report,dataset,dataset.getHeader(),false);
        writeln("</thead>");
    }

    private void renderDataSetBody(Report report,DataSet dataset)
    {
        writeln("<tbody>");
        renderRows(report,dataset,dataset.getRows(),true);
        writeln("</tbody>");
    }

    private void renderRows(Report report,DataSet dataset,RowInfoCollection rows,boolean isData)
    {
        ReportColumnCollection allColumns = dataset.getHeaderColumns();
        int nRowIndex = report.getRowNumMode().isRelative() || dataset.getPageInfo() == null ? 0 : dataset.getPageInfo().getAbsoluteBeginRowIndex();
        int nRealIndexRow = report.getRowNumMode().isRelative() || dataset.getPageInfo() == null ? 0 : dataset.getPageInfo().getAbsoluteBeginRowIndex();
        int nRowsCount = null == rows ? 0 : rows.size();
        for(int nrow = 0;null != rows && nrow < nRowsCount;nrow++)
        {
            RowInfo row = rows.elementAt(nrow);
            if(null == row)
            {
                continue;
            }
            write("<tr ");
            if(null != row.getExtraText())
            {
                write(" " + row.getExtraText());
            }
            writeln(">");
            //Write rownum column at the begin of each rows.
            if(report.getRowNumMode().isNone() == false)
            {
                if(isData == false || row.getRowType().isNormal() == false)
                {
                    write("\t<td class=\"" + CSS_DATA_ROWNUM + "\">&nbsp;</td>");
                }
                else
                {
                    write("\t<td class=\"" + CSS_DATA_ROWNUM + "\">" + (++nRowIndex) + "</td>");
                }
            }
            nRealIndexRow++;
            for(int ncol = 0;null != row && ncol < row.size();ncol++)
            {
                DataCell cell = row.get(ncol);
                ReportColumn colFromCell = null == cell ? null : cell.getReportColumn();
                colFromCell = colFromCell == null && null != allColumns && ncol < allColumns.size() ? allColumns.elementAt(ncol) : colFromCell;
                if(null == cell || cell.isVisiable() == false) //||(isData && col.isHide()))
                {
                    continue;
                }
                String strClassName = "";
                write("\t<td");
                if(cell.getRowSpan() > 1)
                {
                    write(" rowspan='" + cell.getRowSpan() + "'");
                }
                if(cell.getColSpan() > 1)
                {
                    write(" colspan='" + cell.getColSpan() + "'");
                }
                if(isData)
                {
                    if(cell.getCellType().isSubTotal())
                    {
                        strClassName += CSS_DATA_SUBTOTAL + " ";
                    }
                    else if(cell.getCellType().isSum())
                    {
                        strClassName += CSS_DATA_SUMMARY + " ";
                    }
                    if(null != colFromCell && colFromCell.getHorizonAlign().equals(HorizonAligns.Left) == false)
                    {
                        strClassName += (colFromCell.getHorizonAlign().equals(HorizonAligns.Center) ? CSS_CENTERALIGN : CSS_RIGHTALIGN) + " ";
                    }
                    write(" class=\"" + strClassName + "\"");
                    String strStyle = "";
                    if(cell.getCellType().isNormal())
                    {
                        if(null != colFromCell && colFromCell.getColor() != null && colFromCell.getColor().trim().length() > 0)
                        {
                            strStyle += "color:" + colFromCell.getColor() + ";";
                        }
                        if(null != colFromCell && colFromCell.getBgColor() != null && colFromCell.getBgColor().trim().length() > 0)
                        {
                            strStyle += "background-color:" + colFromCell.getBgColor() + ";";
                        }
                    }
                    if(strStyle.length() > 0)
                    {
                        write(" style=\"" + strStyle + "\"");
                    }
                }
                if(null != cell.getExtraText())
                {
                    write(" " + cell.getExtraText());
                }
//                if(isData && cell.getValue4Sort() != null)
//                {
//                    write(" title=\"Sort by value:" + cell.getValue4Sort() + "\"");
//                }
                String strText = "";
                if(null != colFromCell)
                {
                    write(" oncontextmenu=\"javascript:{window.external.ReportContextMenu('" + colFromCell.getGuid() + "','');return false;}\"");
                }
//                    if(isData == false && colFromCell.getSortMode().isNone() == false && isLastRow)
//                    {
//                        strText += "<img class=\"reportsys_grid_headerimg\"" +
//                            " src=\"" + Configuration.getInstance().getRoot() + "/images/" +
//                            (colFromCell.getSortMode().isDescending() ? "grid_header_sort_desc" : "grid_header_sort_asc") + ".gif\"/>";
//                    }
//                    if(isData == false && report.filteredBy(colFromCell))
//                    {
//                        strText += "<img class=\"reportsys_grid_headerimg\" src=\"" +
//                            Configuration.getInstance().getRoot() + "/images/grid_header_filted.gif\"/>";
//                    }
//                    write(" colname=\"" + colFromCell.getTitle() + "\"");
//                    write(" colguid=\"" + colFromCell.getGuid() + "\"");
//                    if(isData == false && null != cell.getReportColumnValue()
//                       && null != cell.getReportColumnValue().getValue()
//                       && "???".equals(cell.getText()) == false //?????ő??q????????value???????Text??????????
//                        )
//                    {
//                        write(" colvalue=\"" + defaultFormatData(cell.getReportColumnValue().getValue()) + "\"");
//                        write(" colvalueName=\"" + cell.getText() + "\"");
//                    }
//                    if(row.getRowType().isNormal() && isData)
//                    {
//                        write(" colvalue=\"" + defaultFormatData(cell.getValue()) + "\"");
//                        write(" colvalueName=\"" + cell.getText() + "\"");
//                    }
//                }
                write(">"); //td tag
                strText += (null == cell.getText() && null == cell.getValue()) ? "&nbsp;" :
                    (cell.getText() == null ? cell.getValue().toString() : cell.getText());
                //strText+="["+cell.getCellType()+"]";
                write(strText);
                writeln("</td>");
            } //end of for loop
            writeln("</tr>");
        }
    }

    private BasicHtmlRender beginSection()
    {
        return this.writeln(this.m_strCSSString)
            .writeln("<div class=\"" + CSSPREFIX + "\">")
            ;
    }

    private BasicHtmlRender endSection()
    {
        return this.writeln("<input type=\"submit\"/></form>")
            .writeln("</div>");
    }

    private BasicHtmlRender renderTitle(Report report)
    {
        if(null == report)
        {
            return this;
        }
        this.writeln("<div class=\"" + CSS_TITLE + "\">" + report.getTitle() + "</div>");
        return this;
    }

    private void renderCharts(Logger log,Report report,DataSet dataset)
    {
        if(null == m_chartRender || null == report || null == m_writer)
        {
            return;
        }
//        boolean isSetTop = report.getChartModels() != null
//            && report.getChartModels().size() > 0
//            && report.getChartModels().getDockMode().equals(DockModes.Top);
//        if(report.getChartModels() != null
//           && report.getChartModels().size() > 0 && isTop == isSetTop)
//        {
        try
        {
            m_chartRender.setup(log,report);
            write(m_chartRender.renderChartHtml(log,report,dataset));
        }
        catch(ChartException ex)
        {
        }
//        }

    }

    public void renderParamterSelector(Logger log,Report report,Exception error)
        throws RenderException
    {
        try
        {
            if(null == report || report.getParametersCount() < 1)
            {
                return;
            }
            this.beginSection()
                .renderTitle(report);
            this.writeln(makeToolbar(report));
            if(null != error)
            {
                this.writeln("<div class=\"" + CSS_SUBTITLE + "\">" + error.getMessage() + "</div>");
            }
            String jquery = " id=\"" + CSS_PARAMETRSECTION + "\""
                + " icon=\"icon-search\""
                + " modal=\"true\""
                + " closed=\"true\""
                + " class=\"easyui-dialog\""
                + " title=\"" + MessageCodes.get(MessageCodes.RENDER_INPUTPARAMETR) + "\""
                + " style=\"width:auto;height:auto;padding:5px;background: #fafafa;\"";
            //class=\""+CSS_PARAMETRSECTION+"\"
            this.writeln("<div " + jquery + ">")
                .writeln("<div class=\"title\">" + MessageCodes.get(MessageCodes.RENDER_INPUTPARAMETR) + "</div>");
            this.writeln("<div class=\"toolbar\" style=\"text-align:right;\">"
                         + makeToolbarRootItem(MessageCodes.RENDER_QUERYBUTTON,"javascript:{window.SQLReport.refresh()}")
                         + "</div>");

            this.writeln(ParameterHtmlGenerator.generateSection(report).toHtml());

            this.writeln("</div>");
            this.endSection();
        }
        catch(DictException ex)
        {
            ex.printStackTrace();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    protected String makeToolbar(Report report)
        throws Exception
    {
//        TagReportToolbar a = new TagReportToolbar(report);
//        return a.simulate();
        return null;
    }

    protected String makeButton(String key,String script)
    {
        String icon = "icon-search";
        return Algorithms.replaceStringByPosition(
            "<a href=\"{0}\" class=\"easyui-linkbutton\" iconCls=\"{1}\">{2}</a>"
            ,script,icon,MessageCodes.get(key));
    }

    protected String makeToolbarRootItem(String key,String script)
    {
        String icon = "icon-search";
        return Algorithms.replaceStringByPosition(
            "<a href=\"{0}\" class=\"easyui-linkbutton\"  plain=\"true\" iconCls=\"{1}\">{2}</a>"
            ,script,icon,MessageCodes.get(key));
    }

    public void renderException(Logger log,Report report,Exception exception)
    {
        if(null == report || null == exception)
        {
            return;
        }
        this.writeln("<div class=\"" + CSS_EXCEPTION + "\">")
            .writeln("<div class=\"t\">" + MessageCodes.format(MessageCodes.EXCEPTION_TITLE,report.getTitle(),exception.getMessage()) + "</div>")
            .writeln("<div class=\"c\">");
        for(StackTraceElement ele : exception.getStackTrace())
        {
            this.writeln("<div>" + ele.toString() + "</div>");
        }
        this.writeln("</div>") //end of c
            .writeln("</div>") //end of exception
            ;
    }

    private BasicHtmlRender writeln(String str)
    {
        if(null == str)
        {
            return this;
        }
        if(this.m_isWellFormated)
        {
            write(str + "\r\n");
        }
        else
        {
            write(str);
        }
        return this;
    }

    private BasicHtmlRender write(String str)
    {
        if(null == str)
        {
            return this;
        }
        if(m_isWellFormated == false)
        {
            str = str.trim();
        }
        if(null != this.m_writer)
        {
            try
            {
                m_writer.write(str);
            }
            catch(IOException ex)
            {
            }
        }
        return this;
    }
}

