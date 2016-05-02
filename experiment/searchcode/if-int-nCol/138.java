package com.jeasonzhao.report.decorator;

import com.jeasonzhao.commons.logger.Logger;
import com.jeasonzhao.commons.parser.expression.ExpressionParser;
import com.jeasonzhao.commons.parser.expression.SyntaxNode;
import com.jeasonzhao.commons.parser.expression.ValuePair;
import com.jeasonzhao.report.dataset.DataCell;
import com.jeasonzhao.report.dataset.DataSet;
import com.jeasonzhao.report.dataset.DataSetEvalProvider;
import com.jeasonzhao.report.dataset.RowInfo;
import com.jeasonzhao.report.dataset.RowInfoCollection;
import com.jeasonzhao.report.exceptions.ReportException;
import com.jeasonzhao.report.model.Report;
import com.jeasonzhao.report.model.ReportColumn;
import com.jeasonzhao.report.model.ReportColumnCollection;

public class CalculateDecorator
{
    private CalculateDecorator()
    {
        super();
    }

    public static void calcDimensions(Logger log,Report report,DataSet dataset)
        throws ReportException
    {
        decoratorTable(log,report,dataset,true,false);
    }

    public static void calcSubtotalRows(Logger log,Report report,DataSet dataset)
        throws ReportException
    {
        decoratorTable(log,report,dataset,false,true);
    }

    public static void calcMeasures(Logger log,Report report,DataSet dataset)
        throws ReportException
    {
        decoratorTable(log,report,dataset,false,false);
    }

    private static DataSet decoratorTable(Logger log,Report report,DataSet dataset,boolean isCalcDimensionOnly,boolean isCalcTotalRowOnly)
        throws ReportException
    {
        ReportColumnCollection cols = dataset.getHeaderColumns();
        if(null == cols)
        {
            return dataset;
        }
        DataSetEvalProvider provider = new DataSetEvalProvider(dataset);
        for(int ncol = 0;ncol < cols.size();ncol++)
        {
            ReportColumn column = cols.get(ncol);
            if(null == column || column.getColumnType().isCalcColumn() == false)
            {
                continue;
            }
            boolean dimension = column.isMeasure() == false;
            if(isCalcDimensionOnly != dimension)
            {
                continue;
            }
            String strScript = column.getCalcScript();
            if(null == strScript || strScript.trim().length() < 1)
            {
                continue;
            }
            strScript = strScript.trim();
            if(strScript.startsWith("="))
            {
                strScript = strScript.substring(1);
            }
            if(null == strScript || strScript.trim().length() < 1)
            {
                continue;
            }
            log.debug("???  " + column.getTitle() + ">>>??" + column.isMeasure() + ">??" + strScript);
            calcColumn(dataset,provider,ncol,strScript,isCalcTotalRowOnly);
        }
        if(isCalcTotalRowOnly)
        {
            //??dataset?footer?header
            if(dataset.getHtmlFooter() == null || dataset.getHtmlFooter().trim().length() < 1)
            {
                dataset.setHtmlFooter(evalDataSetExpression(provider,report.getHtmlFooter()));
            }
            if(dataset.getHtmlHeader() == null || dataset.getHtmlHeader().trim().length() < 1)
            {
                dataset.setHtmlHeader(evalDataSetExpression(provider,report.getHtmlHeader()));
            }
        }
        return dataset;
    }

    private static String evalDataSetExpression(DataSetEvalProvider provider,String strScript)
    {
        String strHtml = null;
        try
        {
            if(null != strScript)
            {
                if(strScript.trim().startsWith("="))
                {
                    strScript = strScript.substring(1);
                    ExpressionParser parser = new ExpressionParser();
                    parser.setExpression(strScript);
                    SyntaxNode node = parser.parseNode();
                    ValuePair returnValue = node.eval(provider);
                    //log.debug(returnValue.getValue());
                    if(null != returnValue && null != returnValue.getValue())
                    {
                        strHtml = returnValue.getValue().toString();
                    }
                }
                else
                {
                    strHtml = strScript;
                }
            }
            else
            {
                strHtml = strScript;
            }
        }
        catch(Exception ex)
        {
//            log.error("?????" + strScript,ex);
            ex.printStackTrace();
        }
        return strHtml;
    }

    private static void calcColumn(DataSet dataset,DataSetEvalProvider provider,int ncol,String strScript,boolean isCalcTotalRowOnly)
    {
        RowInfoCollection rows = dataset.getRows();
        ExpressionParser parser = new ExpressionParser();
        try
        {
            parser.setExpression(strScript);
            SyntaxNode node = parser.parseNode();
            for(int nrow = 0;nrow < rows.size();nrow++)
            {
                provider.setPosition(nrow,ncol);
                RowInfo row = rows.elementAt(nrow);
                if(isCalcTotalRowOnly && row.getRowType().isNormal())
                {
                    continue;
                }
                DataCell cell = row.get(ncol);
                if(null != cell)
                {
                    ValuePair returnValue = node.eval(provider);
                    //log.debug(returnValue.getValue());
                    if(null != returnValue)
                    {
                        cell.setValue(returnValue.getValue());
                    }
                }
            }
        }
        catch(Exception ex)
        {
//            log.error("?????" + strScript,ex);
            ex.printStackTrace();
        }
    }
}

