package com.jeasonzhao.report.decorator;

import com.jeasonzhao.commons.utils.ConvertEx;
import com.jeasonzhao.report.dataset.DataCell;
import com.jeasonzhao.report.dataset.DataSet;
import com.jeasonzhao.report.dataset.RowInfo;
import com.jeasonzhao.report.dataset.RowInfoCollection;
import com.jeasonzhao.report.exceptions.ReportException;
import com.jeasonzhao.report.model.ObjectCollection;
import com.jeasonzhao.report.model.Report;
import com.jeasonzhao.report.model.ReportColumn;
import com.jeasonzhao.report.model.ReportColumnCollection;

public class ShrinkDimensionDecorator
{
    private ShrinkDimensionDecorator()
    {
    }

    public static DataSet decoratorTable(Report report,DataSet dataset)
        throws ReportException
    {
        if(null == report || report.isShrinkByDimension() == false || null == dataset || dataset.getRowsCount() < 2)
        {
            return dataset;
        }
        RowInfoCollection newRows = new RowInfoCollection();
        java.util.HashSet<ObjectCollection> set = new java.util.HashSet<ObjectCollection>();
        //?????Dimension?????????
        ReportColumnCollection colls = dataset.getHeaderColumns();
        for(int nrow = 0;nrow < dataset.getRowsCount();nrow++)
        {
            RowInfo row = dataset.getRow(nrow);
            ObjectCollection kp = getRowKey(colls,row);
            if(set.contains(kp))
            {
                continue;
            }
            set.add(kp);
//            log.debug("Process"+kp);
            processNewRow(colls,newRows,dataset,kp);
        }
        dataset.setRows(newRows);
        return dataset;
    }

    private static ObjectCollection getRowKey(ReportColumnCollection colls,RowInfo row)
    {
        ObjectCollection key = new ObjectCollection();
        for(int ncol = 0;ncol < colls.size();ncol++)
        {
            ReportColumn col = colls.get(ncol);
            if(col.isDimension() && col.isHidden() == false)
            {
                key.add(row.get(ncol));
            }
        }
        return key;
    }

    private static void processNewRow(ReportColumnCollection colls,
                                      RowInfoCollection newRows,DataSet dataset
                                      ,ObjectCollection kpCheck)
    {
        RowInfo newRow = new RowInfo(colls.size());
        //???????????
        int nidx = 0;
        for(int ncol = 0;ncol < colls.size();ncol++)
        {
            ReportColumn col = colls.get(ncol);
            if(col.isMeasure() == false && col.isHidden() == false)
            {
                DataCell cell = (DataCell) kpCheck.get(nidx);
                nidx++;
                newRow.setElementAt(cell,ncol);
            }
        }
        for(int nrow = 0;nrow < dataset.getRowsCount();nrow++)
        {
            RowInfo row = dataset.getRow(nrow);
            ObjectCollection kp = getRowKey(colls,row);
            if(kpCheck.equals(kp) == false)
            {
                continue;
            }
            for(int ncol = 0;ncol < colls.size();ncol++)
            {
                ReportColumn col = colls.get(ncol);
                if(col.isMeasure() && null != row.get(ncol) && null != row.get(ncol).getValue())
                {
                    DataCell cellNew = newRow.get(ncol);
                    Double lfValue = null == cellNew.getValue() ? new Double(0) : (Double) cellNew.getValue();
                    lfValue = new Double(lfValue.doubleValue() + ConvertEx.toDouble(row.get(ncol).getValue()));
                    cellNew.setValue(lfValue);
                    cellNew.setVisiable(col.isHidden() == false);
                }
            }
        }
        for(int ncol = 0;ncol < colls.size();ncol++)
        {
            ReportColumn col = colls.get(ncol);
            DataCell cell = newRow.elementAt(ncol);
            cell.setVisiable(col.isHidden() == false);
            cell.setValue(col.getDataType().castValue(cell.getValue()));
        }
        newRows.addElement(newRow);
    }

}

