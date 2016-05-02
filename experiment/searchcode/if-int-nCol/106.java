package com.jeasonzhao.report.decorator;

import com.jeasonzhao.commons.basic.StringPair;
import com.jeasonzhao.commons.basic.StringPairCollection;
import com.jeasonzhao.commons.parser.expression.EvalException;
import com.jeasonzhao.commons.parser.expression.ExpressionParser;
import com.jeasonzhao.commons.parser.expression.SyntaxException;
import com.jeasonzhao.commons.parser.expression.SyntaxNode;
import com.jeasonzhao.commons.parser.expression.ValuePair;
import com.jeasonzhao.commons.parser.lex.LexException;
import com.jeasonzhao.report.codes.ReportSortModes;
import com.jeasonzhao.report.dataset.DataCell;
import com.jeasonzhao.report.dataset.DataSet;
import com.jeasonzhao.report.dataset.DataSetEvalProvider;
import com.jeasonzhao.report.dataset.RowInfoComparator;
import com.jeasonzhao.report.exceptions.DecoratorException;
import com.jeasonzhao.report.model.Report;
import com.jeasonzhao.report.model.ReportColumn;
import com.jeasonzhao.report.model.ReportColumnCollection;

public class SortDecorator
{
    private SortDecorator()
    {
    }

    public static void sortDataSet(Report report,DataSet dataset,boolean isRotatedDimensionOnly)
        throws DecoratorException
    {
        ReportColumnCollection cols = dataset.getHeaderColumns();
        if(null == cols || dataset.getRowsCount() <= 1)
        {
            return;
        }
        RowInfoComparator sortSetting = new RowInfoComparator();
        if(null != report)
        {
            StringPairCollection spsorts = report.getSortSettings();
            for(int n = 0;null != spsorts && n < spsorts.size();n++)
            {
                ReportColumn column = cols.getColumnByGuid(spsorts.get(n).getId());
                if(null != column)
                {
                    column.setSortMode(ReportSortModes.fromName(spsorts.get(n).getName()));
                }
                ReportColumn reportColumn = report.getColumns().getColumnByGuid(spsorts.get(n).getId());
                if(null != reportColumn)
                {
                    reportColumn.setSortMode(ReportSortModes.fromName(spsorts.get(n).getName()));
                }

            }
        }
        for(int ncol = 0;ncol < cols.size();ncol++)
        {
            ReportColumn column = cols.get(ncol);
            if(null == column || column.isHidden())
            {
                continue;
            }
            if(isRotatedDimensionOnly)
            {
                if(column.isDimension() == false || column.getRotateMode().isRotate() == false)
                {
                    continue;
                }
            }
            if(column.getSortMode().isNone() == false)
            {
                //log.info(report + " ?????[" + ncol + "] " + column.getTitle() + " " + column.getSortMode() + "...." + column.getSortScript());
                addReportSortSetting(report,column);
                sortSetting.add(ncol,column.getSortMode().isAscending());
            }
            else if(column.getMergeMode().isNone() == false
                    || (column.isDimension() && column.getRotateMode().isRotate()))
            {
                ReportSortModes newS = column.getSortMode().isAscending() ? ReportSortModes.Descending : ReportSortModes.Ascending;
                column.setSortMode(newS);
                ReportColumn reportColumn = report.getColumns().getColumnByGuid(column.getGuid());
                if(null != reportColumn)
                {
                    reportColumn.setSortMode(newS);
                }
//                log.info(report + " ?????[" + ncol + "] " + column.getTitle() + " " + column.getSortMode() + "...." + column.getSortScript());
                addReportSortSetting(report,column);
                sortSetting.add(ncol,newS.isAscending());
            }
        }
        //        log.debug("Sort setting :"+report.getSortSettings());
        //??
        if(sortSetting.size() > 0)
        {
            try
            {
                calcSortValues(report,dataset,cols,sortSetting);
            }
            catch(DecoratorException ex)
            {
                throw ex;
            }
//            log.info(report + " ????....");
            dataset.getRows().sort(sortSetting);
        }
    }

    private static void calcSortValues(Report report,DataSet dataset,ReportColumnCollection cols,RowInfoComparator sortSetting)
        throws DecoratorException
    {
        String strScript = null;
        ReportColumn column = null;
        try
        {
            //??????
            java.util.ArrayList<Integer> col2SortCalc = new java.util.ArrayList<Integer>();
            for(int n = 0;n < cols.size();n++)
            {
                if(cols.get(n) != null && cols.get(n).getSortScript() != null && sortSetting.isContains(n))
                {
                    col2SortCalc.add(n);
                }
            }
            if(col2SortCalc.size() > 0)
            {
                DataSetEvalProvider provider = new DataSetEvalProvider(dataset);
                for(int nrow = 0;nrow < dataset.getRows().size();nrow++)
                {
                    for(int n = 0;n < col2SortCalc.size();n++)
                    {
                        int ncol = col2SortCalc.get(n);
                        provider.setPosition(nrow,ncol);
                        DataCell cell = dataset.getAt(nrow,ncol);
                        if(null == cell)
                        {
                            continue;
                        }
                        column = cols.get(ncol);
                        strScript = column.getSortScript();
                        if(strScript.startsWith("=") == false)
                        {
                            cell.setValue4Sort(strScript);
                        }
                        else
                        {
                            strScript = strScript.substring(1);
                            ExpressionParser parser = new ExpressionParser();
                            parser.setExpression(strScript);
                            SyntaxNode node = parser.parseNode();
                            ValuePair returnValue = node.eval(provider);
                            if(null != returnValue && null != returnValue.getValue())
                            {
                                cell.setValue4Sort(returnValue.getValue());
                            }
                        }
                    }
                }
            }
        }
        catch(EvalException ex)
        {
            throw new DecoratorException.SortEvalError(report,column,strScript,ex);
        }
        catch(SyntaxException ex)
        {
            throw new DecoratorException.SortEvalError(report,column,strScript,ex);
        }
        catch(LexException ex)
        {
            throw new DecoratorException.SortLexError(report,column,strScript,ex);
        }
    }

    private static void addReportSortSetting(Report report,ReportColumn column)
    {
        if(null != report)
        {
            StringPair p = report.getSortSettings() == null ? null : report.getSortSettings().find(column.getGuid());
            if(p == null)
            {
                p = new StringPair(column.getGuid(),column.getSortMode().getConstantFieldName());
                if(report.getSortSettings() == null)
                {
                    report.setSortSettings(new StringPairCollection());
                }
                report.getSortSettings().add(p);
            }
            else
            {
                p.setName(column.getSortMode().getConstantFieldName());
            }
        }
    }
}

