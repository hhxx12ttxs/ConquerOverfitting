package com.jeasonzhao.report.decorator;

import com.jeasonzhao.commons.utils.ConvertEx;
import com.jeasonzhao.report.codes.DataSetCellTypes;
import com.jeasonzhao.report.codes.ReportSummaryModes;
import com.jeasonzhao.report.dataset.DataSet;
import com.jeasonzhao.report.dataset.RowInfo;
import com.jeasonzhao.report.dataset.RowInfoCollection;
import com.jeasonzhao.report.exceptions.DecoratorException;
import com.jeasonzhao.report.model.Report;
import com.jeasonzhao.report.model.ReportColumn;
import com.jeasonzhao.report.model.ReportColumnCollection;
import com.jeasonzhao.report.msg.MessageCodes;

public class SummaryDecorator
{
    private SummaryDecorator()
    {
        super();
    }

    public static DataSet decoratorTable(Report report,DataSet dataset)
        throws DecoratorException
    {
        if(report == null
           || dataset == null
           || dataset.getRowsCount() <= 1
           || ReportSummaryModes.None.equals(report.getSummaryMode()))
        {
            return dataset;
        }
        ReportColumnCollection cols = dataset.getHeaderColumns();
        RowInfo returnrow = new RowInfo(cols == null ? dataset.getRow(0).size() : cols.size());
        int ncolscount = returnrow.size();
        if(ncolscount < 1)
        {
            return dataset;
        }
        RowInfoCollection rows = dataset.getRows();
        int nr = 0;
        for(int nRow = 0;nRow < rows.size();nRow++)
        {
            RowInfo row = rows.elementAt(nRow);
            if(row.getRowType().isNormal() == false)
            {
                continue;
            }
            nr++;
            for(int ncol = 0;ncol < ncolscount;ncol++)
            {
                ReportColumn col = null == cols ? null : cols.elementAt(ncol);
                if(null != col)
                {
                    returnrow.get(ncol).setVisiable(col.isHidden() == false);
                }
                if(row.get(ncol) == null || row.get(ncol).isVisiable() == false)
                {
                    continue;
                }
                if(col.isMeasure() && col.isSummaryable() && row.get(ncol) != null && row.get(ncol).getValue() != null)
                {
                    Double lfSum = (Double) returnrow.get(ncol).getValue();
                    if(null == lfSum)
                    {
                        lfSum = new Double(0);
                    }
                    lfSum = new Double(lfSum.doubleValue() + ConvertEx.toDouble(row.get(ncol).getValue(),0));
                    returnrow.get(ncol).setValue(lfSum);
                }
            }
        }
        if(nr > 0)
        {
            int nColumn = 0;
            if(cols.get(nColumn).isSummaryable() == false)
            {
                returnrow.get(nColumn).setText(MessageCodes.get(MessageCodes.RENDER_Summary));
            }
            returnrow.setRowType(DataSetCellTypes.Sum);
            if(report.getSummaryMode().equals(ReportSummaryModes.Default))
            {
                dataset.addRow(returnrow);
            }
            else
            {
                dataset.insertRowAt(returnrow,0);
            }
        }
        return dataset;
    }

}

