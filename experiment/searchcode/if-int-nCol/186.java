package com.jeasonzhao.report.decorator;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.jeasonzhao.commons.utils.ConvertEx;
import com.jeasonzhao.report.dataset.DataCell;
import com.jeasonzhao.report.dataset.DataSet;
import com.jeasonzhao.report.dataset.RowInfo;
import com.jeasonzhao.report.dataset.RowInfoCollection;
import com.jeasonzhao.report.engine.irp.IDataSetDecorator;
import com.jeasonzhao.report.exceptions.DecoratorException;
import com.jeasonzhao.report.model.Report;
import com.jeasonzhao.report.model.ReportColumn;
import com.jeasonzhao.report.model.ReportColumnCollection;

public class CellFormatDecorator implements IDataSetDecorator
{
//    private static final Log log = LogFactory.getLog(CellFormatDecorator.class);
    private static volatile CellFormatDecorator m_instance = null;
    public static CellFormatDecorator getInstance()
    {
        if(null == m_instance)
        {
            synchronized(CellFormatDecorator.class)
            {
                m_instance = new CellFormatDecorator();
            }
        }
        return m_instance;
    }

    private CellFormatDecorator()
    {
        super();
    }

    public DataSet decorate(Report report,DataSet dataset)
        throws DecoratorException
    {
        if(null == dataset || null == report)
        {
            return dataset;
        }
        ReportColumnCollection rptCols = dataset.getHeaderColumns();
        if(null == rptCols)
        {
            return dataset;
        }

        RowInfoCollection rows = dataset.getRows();
        if(null == rptCols || null == rows)
        {
            return dataset;
        }
        for(int ncol = 0;ncol < rptCols.size();ncol++)
        {
            ReportColumn column = rptCols.get(ncol);
            if(null == column)
            {
                continue;
            }
            for(int nrow = 0;nrow < rows.size();nrow++)
            {
                RowInfo row = rows.elementAt(nrow);
                if(null == row)
                {
                    continue;
                }
                DataCell cell = row.get(ncol);
                if(cell == null || cell.getValue() == null)
                {
                    continue;
                }
                if(column.getDictId() != null)
                {
                    cell.setDataType(column.getDataType());
                }
                formatText(column,cell);
            }
        }
        return dataset;
    }

    public static final void formatText(ReportColumn col,DataCell cell)
    {
        if(null == col || null == cell
           || col.getDataFormat() == null
           || cell.getValue() == null
           || col.getDataFormat().trim().length() < 1)
        {
            return;
        }
        try
        {
            if(col.getDataType().isNumeric())
            {
                double alertValue = ConvertEx.toDouble(cell.getValue());
                DecimalFormat def = new DecimalFormat(col.getDataFormat());
//                log.debug(">>>>>>>>>>>>SKIP>>>>"+col.getTitle());
                cell.setText(def.format(alertValue));
            }
            else if(col.getDataType().isDate())
            {
                Date date = ConvertEx.toDate(cell.getValue());
                if(null != date)
                {
                    SimpleDateFormat df = new SimpleDateFormat(col.getDataFormat());
                    cell.setText(df.format(date));
                }
            }
        }
        catch(Exception e)
        {
        }
    }

}

