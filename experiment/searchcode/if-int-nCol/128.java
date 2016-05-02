package com.jeasonzhao.report.decorator;

import com.jeasonzhao.report.codes.ReportColumnTypes;
import com.jeasonzhao.report.dataset.DataCell;
import com.jeasonzhao.report.dataset.DataSet;
import com.jeasonzhao.report.dataset.RowInfo;
import com.jeasonzhao.report.dataset.RowInfoCollection;
import com.jeasonzhao.report.engine.repo.DictRepository;
import com.jeasonzhao.report.exceptions.DecoratorException;
import com.jeasonzhao.report.exceptions.DictException;
import com.jeasonzhao.report.model.DictItem;
import com.jeasonzhao.report.model.Report;
import com.jeasonzhao.report.model.ReportColumn;
import com.jeasonzhao.report.model.ReportColumnCollection;
import com.jeasonzhao.report.model.ReportDict;

public class HorizonExpandColumnDecorator
{
    private HorizonExpandColumnDecorator()
    {
        super();
    }

    public static void horizonExpandDataSet(Report report,DataSet dataset)
        throws DecoratorException
    {
        if(null == dataset)
        {
            return;
        }
        ReportColumn column = null;
        try
        {
            ReportColumnCollection cols = dataset.getHeaderColumns();
            RowInfoCollection rows = dataset.getRows();
            if(null == cols || null == rows)
            {
                return;
            }
            RowInfoCollection header = dataset.getHeader();
            for(int ncol = 0;ncol < cols.size();ncol++)
            {
                column = cols.elementAt(ncol);
                if(column.isHidden() || column.getDictId() == null || column.getColumnType().equals(ReportColumnTypes.ExpandDictTree) == false)
                {
                    continue;
                }
                ReportDict[] dicts = DictRepository.getInstance().getParentDictDefines(column.getDictId());
                if(null == dicts || dicts.length < 2)
                {
                    continue;
                }
                for(int nrow = 0;nrow < rows.size();nrow++)
                {
                    RowInfo row = rows.elementAt(nrow);
                    //?????Cell
                    DataCell[] cells = new DataCell[dicts.length];
                    cells[0] = row.get(ncol);
                    for(int nd = 1;nd < dicts.length;nd++)
                    {
                        cells[nd] = new DataCell();
                        row.insertElementAt(cells[nd],ncol);
                    }
                    DictItem item = DictRepository.getInstance().itemOf(column.getDictId(),cells[0].getValue());
                    if(null == item)
                    {
                        continue;
                    }
                    int nLevel = Math.max(0,item.getCurrentLevel());
                    cells[0].setValue(null);
                    cells[0].setText(null);
                    cells[nLevel].setValue(item.getKey());
                    cells[nLevel].setText(item.getName());
                    for(int nd = Math.max(nLevel + 1,1);nd < dicts.length;nd++)
                    {
                        if(null != item)
                        {
                            item = DictRepository.getInstance().itemOf(dicts[nd].getId(),item.getParentKey());
                            if(null != item)
                            {
                                cells[nd].setValue(item.getKey());
                                cells[nd].setText(item.getName());
                            }
                        }
                        else
                        {
                            break;
                        }
                    }
                }
                for(int nd = 1;nd < dicts.length;nd++)
                {
                    ReportColumn expandColumn = new ReportColumn(column);
                    expandColumn.setColumnType(ReportColumnTypes.Normal);
                    expandColumn.setTitle(dicts[nd].getName());
                    expandColumn.setFieldName(column.getFieldName() + nd);
                    expandColumn.setDictId(dicts[nd].getId());
                    expandColumn.setGuid(column.getGuid() + ".@ExtendCol." + nd);
                    expandColumn.setSortMode(column.getSortMode());
                    //Table Header Information
                    DataCell cellHeader = DataCell.create(dicts[nd].getName());
                    cellHeader.setReportColumn(expandColumn);
                    header.elementAt(0).insertElementAt(cellHeader,ncol - nd + 1);
                    cols.insertElementAt(expandColumn,ncol - nd + 1);
                    ncol++;
                }
            }
        }
        catch(DictException ex)
        {
            throw new DecoratorException.HorizonExpand(report,column,ex);
        }
    }

}

