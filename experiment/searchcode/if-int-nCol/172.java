package com.jeasonzhao.report.decorator;

import java.io.UnsupportedEncodingException;

import com.jeasonzhao.commons.parser.expression.EvalException;
import com.jeasonzhao.commons.parser.expression.SyntaxException;
import com.jeasonzhao.commons.parser.lex.LexException;
import com.jeasonzhao.commons.utils.ConvertEx;
import com.jeasonzhao.report.codes.ReportAlertModels;
import com.jeasonzhao.report.dataset.DataCell;
import com.jeasonzhao.report.dataset.DataSet;
import com.jeasonzhao.report.dataset.DataSetEvalProvider;
import com.jeasonzhao.report.dataset.RowInfo;
import com.jeasonzhao.report.dataset.RowInfoCollection;
import com.jeasonzhao.report.engine.irp.IDataSetDecorator;
import com.jeasonzhao.report.exceptions.DecoratorException;
import com.jeasonzhao.report.model.Report;
import com.jeasonzhao.report.model.ReportAlert;
import com.jeasonzhao.report.model.ReportAlertCollection;
import com.jeasonzhao.report.model.ReportColumn;
import com.jeasonzhao.report.model.ReportColumnCollection;

public class AlertsDecorator implements IDataSetDecorator
{
//    private static final Log log = LogFactory.getLog(CalculateDecorator.class);
    private static volatile AlertsDecorator m_instanceHtml = null;
    private static volatile AlertsDecorator m_instanceNonHtml = null;
    //???????????????HTML?????????????<a href=''>Anchor</a>???
    //private boolean m_isHtml = false;
    public static AlertsDecorator getInstanceHtml()
    {
        if(null == m_instanceHtml)
        {
            synchronized(AlertsDecorator.class)
            {
                m_instanceHtml = new AlertsDecorator(true);
            }
        }
        return m_instanceHtml;
    }

    public static AlertsDecorator getInstanceNonHtml()
    {
        if(null == m_instanceNonHtml)
        {
            synchronized(AlertsDecorator.class)
            {
                m_instanceNonHtml = new AlertsDecorator(false);
            }
        }

        return m_instanceNonHtml;
    }

    private AlertsDecorator(boolean b)
    {
        super();
        //m_isHtml = b;
    }

    public DataSet decorate(Report report,DataSet dataset)
        throws DecoratorException
    {
            try
			{
				DataSetEvalProvider context = null;
				ReportColumnCollection cols = dataset.getHeaderColumns();
				if(null == cols)
				{
				    return dataset;
				}

				RowInfoCollection rows = dataset.getRows();
				for(int ncol = 0;ncol < cols.size();ncol++)
				{
				    ReportColumn column = cols.get(ncol);
				    ReportAlertCollection alerts = column.getAlerts();
				    if(null == alerts || alerts.size() < 1)
				    {
				        continue;
				    }
				    context = new DataSetEvalProvider(dataset);
				    /**
				     * @todo ?????????????
				     */
				    //                if(null == context)
//                {
//                    context = new DataSetEvalProvider(dataset);
//                    for(int np = 0;null != report.getParameters() && np < report.getParameters().size();np++)
//                    {
//                        ReportParam param = report.getParameters().get(np);
//                        context.addVariable(param.getParam(),param.getSingleParamValue());
//                        context.addVariable(HtmlRenderCodes.getParamName(param),param.getSingleParamValue());
//                    }
//                }
				    for(int nrow = 0;nrow < rows.size();nrow++)
				    {
				        context.setPosition(nrow,ncol);
				        RowInfo row = rows.elementAt(nrow);
				        DataCell cell = row.get(ncol);
				        if(cell == null || cell.getValue() == null)
				        {
				            continue;
				        }
				        alertOperation(report,alerts,ReportAlertModels.Render,row,cell,context);

				        alertOperation(report,alerts,ReportAlertModels.Href,row,cell,context);
				        alertOperation(report,alerts,ReportAlertModels.Click,row,cell,context);
				        alertOperation(report,alerts,ReportAlertModels.DoubleClick,row,cell,context);
				        alertOperation(report,alerts,ReportAlertModels.ContextMenu,row,cell,context);
				    }
				}
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
			catch (EvalException e)
			{
				e.printStackTrace();
			}
			catch (SyntaxException e)
			{
				e.printStackTrace();
			}
			catch (LexException e)
			{
				e.printStackTrace();
			}
        return dataset;
    }

    private void alertOperation(Report report,ReportAlertCollection wholeAlerts,ReportAlertModels mode,
                                RowInfo row,
                                DataCell cell,DataSetEvalProvider context)
        throws EvalException,SyntaxException,LexException,UnsupportedEncodingException
    {
        ReportAlertCollection alerts = null == wholeAlerts ? null : wholeAlerts.findByMode(mode,cell.getCellType());
        if(null == alerts || alerts.size() < 1 || null == cell)
        {
            return;
        }
        for(int n = 0;n < alerts.size();n++)
        {
            ReportAlert alert = alerts.get(n);
            boolean isMatch = false;
            String strExpression = alert.getExpression();
            if(null == strExpression || strExpression.trim().length() < 1)
            {
                isMatch = true;
            }
            else
            {
                isMatch = ConvertEx.toBool(context.evalValue(strExpression));
            }
            if(isMatch == false)
            {
                continue;
            }
            renderAlert(report,mode,row,cell,context,alert);
//            return;
        }
    }

    private void renderAlert(Report report,ReportAlertModels mode,RowInfo row,DataCell cell,DataSetEvalProvider context,
                             ReportAlert alert)
        throws EvalException,SyntaxException,LexException,UnsupportedEncodingException
    {
        /**@todo >>>>>>>>
                 String strText = cell.getText();
                 if(null != alert.getTextExpression() && alert.getTextExpression().trim().length() > 0)
                 {
            strText = Convert.toString(context.evalValue(alert.getTextExpression()));
//            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+alert.getTextExpression()+"="+strText);
            cell.setText(strText);
                 }
                 String strCellExtra = null == cell.getExtraText() ? "" : cell.getExtraText();
                 String strRowExtra = null == row.getExtraText() ? "" : row.getExtraText();
                 strCellExtra += null != alert.getRenderCellCss() && alert.getRenderCellCss().trim().length() > 0 ? " style=\"" + alert.getRenderCellCss() + "\"" : "";
                 strRowExtra += null != alert.getRenderRowCss() && alert.getRenderRowCss().trim().length() > 0 ? " style=\"" + alert.getRenderRowCss() + "\"" : "";
                 if(m_isHtml)
                 {
//        log.info(">>>"+alert+alert.getRenderRowCss());
            if(alert.getAction().equals(ReportAlertActions.JavaScript) && alert.getJavaScript() != null && alert.getJavaScript().trim().length() > 0)
            {
                strCellExtra += " ";
                strCellExtra += mode.equals(ReportAlertModels.ContextMenu) ? "oncontextmenu" :
                    (mode.equals(ReportAlertModels.DoubleClick) ? "ondblclick " : "onclick");
                strCellExtra += "=\"" + alert.getJavaScript() + "\"";
            }
            else if(false == alert.getAction().equals(ReportAlertActions.Render))
            {
                String strInitUrl = alert.getUrl();
                if(null == strInitUrl || strInitUrl.trim().length() < 1
                   || null == strText || strText.trim().length() < 1)
                {
                    //do nothing
                }
                else
                {
                    StringPairCollection pairs = alert.getUrlParameters();
                    Report rptNew = null;
                    if(alert.getAction().equals(ReportAlertActions.Report))
                    {
                        if(strInitUrl.equalsIgnoreCase("_self") || strInitUrl.equalsIgnoreCase("#id") || strInitUrl.equalsIgnoreCase("#report")
                           || strInitUrl.equalsIgnoreCase("#reportid") || strInitUrl.equalsIgnoreCase("#self"))
                        {
                            strInitUrl = report.getReportId();
                            rptNew = report;
                        }
                        else
                        {
                            rptNew = ReportManager.getInstance().safeGetReport(strInitUrl,null);
                        }
                        strInitUrl = Configuration.getInstance().getReportNavUrl(strInitUrl);
                    }
                    StringBuffer strUrlParameters = new StringBuffer();
                    for(int nx = 0;null != pairs && nx < pairs.size();nx++)
                    {
                        StringPair p = pairs.get(nx);
                        String name = p.getId();
                        if(null != rptNew)
                        {
                            name = null == rptNew.getParameters() ? null : HtmlRenderCodes.getParamName(rptNew.getParameters().findByParamName(name));
                            if(null == name)
                            {
                                continue;
                            }
                        }
                        Object value = context.eval(p.getName()).getValue();
                        if(strUrlParameters.length() > 0)
                        {
                            strUrlParameters.append("&");
                        }
                        String strT = HtmlRenderCodes.defaultFormatData(value);
                        if(null != Configuration.getInstance().getDefaultCharset())
                        {
                            strT = java.net.URLEncoder.encode(strT,Configuration.getInstance().getDefaultCharset());
                        }
                        strUrlParameters.append(name + "=" + strT);
                    }
                    if(strUrlParameters.length() > 0)
                    {
                        if(strInitUrl.lastIndexOf("?") < 0)
                        {
                            strInitUrl += "?" + strUrlParameters;
                        }
                        else
                        {
                            strInitUrl += "&" + strUrlParameters;
                        }
                    }
                    cell.setText(alert.getUrlTarget() != null && alert.getUrlTarget().trim().length() > 0 ?
                                 "<a href=\"" + strInitUrl + "\" target=\"" + alert.getUrlTarget() + "\">" + strText + "</a>" :
                                 "<a href=\"" + strInitUrl + "\" >" + strText + "</a>");
                }
            }
                 }
                 if(strCellExtra.length() > 0)
                 {
            cell.setExtraText(strCellExtra);
                 }
                 if(strRowExtra.length() > 0)
                 {
            row.setExtraText(strRowExtra);
                 }
         */
    }
}

