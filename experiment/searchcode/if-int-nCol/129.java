package com.jeasonzhao.report.decorator;

import com.jeasonzhao.report.dataset.DataCell;
import com.jeasonzhao.report.dataset.DataSet;
import com.jeasonzhao.report.dataset.RowInfo;
import com.jeasonzhao.report.engine.irp.IDataSetDecorator;
import com.jeasonzhao.report.engine.repo.DictRepository;
import com.jeasonzhao.report.exceptions.DecoratorException;
import com.jeasonzhao.report.model.Report;
import com.jeasonzhao.report.model.ReportColumn;
import com.jeasonzhao.report.model.ReportColumnCollection;

public class DictDecorator implements IDataSetDecorator
{
    private static volatile DictDecorator m_instance = null;
    public static DictDecorator getInstance()
    {
        if(null == m_instance)
        {
            synchronized(DictDecorator.class)
            {
                m_instance = new DictDecorator();
            }
        }
        return m_instance;
    }

    private DictDecorator()
    {
        super();
    }

    public DataSet decorate(Report report,DataSet dataset)
        throws DecoratorException
    {
        ReportColumnCollection columns = dataset.getHeaderColumns();
        if(null == columns)
        {
            return dataset;
        }
        String[] dictIds = new String[columns.size()];
        java.util.Arrays.fill(dictIds,null);
        for(int nCol = 0;nCol < columns.size();nCol++)
        {
            ReportColumn column = columns.get(nCol);
            String strDictId = column.getDictId();
            if(strDictId != null && strDictId.trim().length() > 0)
            {
                dictIds[nCol] = strDictId;
            }
        }
        for(int nrow = 0;nrow < dataset.getRows().size();nrow++)
        {
            RowInfo row = dataset.getRow(nrow);
            for(int ncol = 0;ncol < dictIds.length && ncol < row.size();ncol++)
            {
                String strDictId = dictIds[ncol];
                if(null == strDictId)
                {
                    continue;
                }
                DataCell cell = row.get(ncol);
                Object value = cell.getValue();
                if(null == value)
                {
                    continue;
                }
                cell.setText(DictRepository.getInstance().nameOf(strDictId,value).toString());
            }
        }
        return dataset;
    }

}

