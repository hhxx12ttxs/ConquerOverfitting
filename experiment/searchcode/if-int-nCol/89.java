package com.jeasonzhao.report.decorator;

import com.jeasonzhao.commons.utils.ConvertEx;
import com.jeasonzhao.report.codes.DataSetCellTypes;
import com.jeasonzhao.report.dataset.DataCell;
import com.jeasonzhao.report.dataset.DataSet;
import com.jeasonzhao.report.dataset.RowInfo;
import com.jeasonzhao.report.dataset.RowInfoCollection;
import com.jeasonzhao.report.exceptions.DecoratorException;
import com.jeasonzhao.report.model.ObjectCollection;
import com.jeasonzhao.report.model.Report;
import com.jeasonzhao.report.model.ReportColumn;
import com.jeasonzhao.report.model.ReportColumnCollection;

public class SubTotalDecorator
{
    private SubTotalDecorator()
    {
        super();
    }

    public static DataSet decoratorTable(Report reportx,DataSet dataset)
        throws DecoratorException
    {
        if(null == dataset)
        {
            return null;
        }
        ReportColumnCollection cols = dataset.getHeaderColumns();
        if(null == cols)
        {
            return dataset;
        }
        for(int nCol = 0;nCol < cols.size();nCol++)
        {
            ReportColumn column = cols.get(nCol);
            if(column.getMergeMode().isSubtotal() && column.isHidden() == false)
            {
                calculateRows(column,dataset,nCol);
            }
        }
        //merge all cells
        java.util.ArrayList<Integer> collx = new java.util.ArrayList<Integer>();
        for(int nCol = 0;nCol < cols.size();nCol++)
        {
            ReportColumn column = cols.get(nCol);
            if(column.getMergeMode().isMerge() && column.isHidden() == false)
            {
                megerRow(dataset,nCol,collx);
            }
        }
        return dataset;
    }

    private static ObjectCollection getValuesKey(DataSet table,int nRow,int nCurrentCol)
    {
        ObjectCollection ret = new ObjectCollection();
        for(int ncol = 0;ncol <= nCurrentCol;ncol++)
        {
            if(table.getHeaderColumn(ncol) == null ||
               table.getHeaderColumn(ncol).isHidden() ||
               table.getHeaderColumn(ncol).getMergeMode().isNone())
            {
                continue;
            }
            DataCell cellInit = table.getAt(nRow,ncol);
            Object value = cellInit == null ? new Object() : cellInit.getValue();
            ret.add(value);
        }
        return ret;
    }

    private static void calculateRows(ReportColumn column,DataSet table,int nColumn2Group)
    {
        int nMinLevel = column.getMergeMode().isSubtotalAll() ? 0 : 1;
        boolean isShowTop = column.getMergeMode().isSubtotalTop();
        int nStartRowIndex = 0;
        int nRowsCount = table.getRows().size();
        ObjectCollection prevValues = null;
        int nrow = 0;
        for(;nrow < table.getRows().size();nrow++)
        {
            RowInfo currentRow = table.getRow(nrow);
            ObjectCollection curKey = getValuesKey(table,nrow,nColumn2Group);
            if(prevValues == null)
            {
                if(currentRow.getRowType().isNormal())
                {
                    nStartRowIndex = nrow;
                    prevValues = curKey;
                }
                else
                {
                    prevValues = null;
                    nStartRowIndex = nrow;
                }
            }
            else
            {
                if(currentRow.getRowType().isNormal() == false || curKey.equals(prevValues) == false)
                {
                    int nRowSpan = nrow - nStartRowIndex;
                    int nBStart = nStartRowIndex;
                    prevValues = curKey;
                    nStartRowIndex = nrow;
                    if(nRowSpan > nMinLevel && null != prevValues)
                    {
                        table.insertRowAt(getSubSumRow(column.getMergeTitle(),table,nColumn2Group,nBStart,nrow - 1),isShowTop ? nBStart : nrow);
                        prevValues = null;
                    }
                }
            }
        }
        nRowsCount = table.getRows().size();
        int nRowSpan = nRowsCount - nStartRowIndex;
        if(nRowSpan > nMinLevel && null != prevValues)
        {
            table.insertRowAt(getSubSumRow(column.getMergeTitle(),table,nColumn2Group,nStartRowIndex,nrow - 1),isShowTop ? nStartRowIndex : nrow);
        }
    }

    private static java.util.ArrayList<Integer> megerRow(DataSet table,int nColumn,java.util.ArrayList<Integer> colles)
    {
        ObjectCollection prevValues = null;
        int nPrevRow = 0;
        int nrow = 0;
        for(;nrow < table.getRows().size();nrow++)
        {
            RowInfo currentRow = table.getRow(nrow);
            if(currentRow.getRowType().isNormal() == false)
            {
                colles.clear();
            }
            DataCell cellInit = currentRow.get(nColumn);
            ObjectCollection curKey = getValuesKey(table,nrow,nColumn);
            if(prevValues == null)
            {
                prevValues = cellInit.getCellType().isNormal() ? curKey : null;
                nPrevRow = nrow;
                continue;
            }
            else
            {
                if(cellInit.getCellType().isNormal() == false
                   || curKey.equals(prevValues) == false
                   || colles.indexOf(nrow) >= 0)
                {
                    int nRowSpan = nrow - nPrevRow;
                    prevValues = curKey;
                    if(nRowSpan > 1)
                    {
                        colles.add(nrow);
                        RowInfo rowPrev = table.getRow(nPrevRow);
                        DataCell cellPrev = rowPrev.get(nColumn);
                        cellPrev.setRowSpan(nRowSpan);
                        for(int n = nPrevRow + 1;n < nrow;n++)
                        {
                            table.getAt(n,nColumn).setVisiable(false);
                        }
                        if(cellInit.getCellType().isNormal() == false)
                        {
                            prevValues = null;
                        }
                    }
                    nPrevRow = nrow;
                }
                if(cellInit.getCellType().isNormal() == false)
                {
                    prevValues = null;
                }
            }
        }
        int nRowSpan = nrow - nPrevRow;
        if(null != prevValues && nRowSpan > 1)
        {
            RowInfo rowPrev = table.getRow(nPrevRow);
            DataCell cellPrev = rowPrev.get(nColumn);
            cellPrev.setRowSpan(nRowSpan);
            for(int n = nPrevRow + 1;n < nrow;n++)
            {
                table.getAt(n,nColumn).setVisiable(false);
            }
        }
        return colles;
    }

    private static RowInfo getSubSumRow(String strText,DataSet table,int nColumn,int nStartRow,int nEndRow)
    {
        RowInfoCollection rows = table.getRows();
        ReportColumnCollection cols = table.getHeaderColumns();
        RowInfo rowRet = new RowInfo(rows.elementAt(nStartRow));
        rowRet.setRowType(DataSetCellTypes.SubTotal);
        DataCell cellSubTotal = rowRet.get(nColumn);
        cellSubTotal.setSubtotalRowsCount(nEndRow - nStartRow + 1);
        for(int n = 0;n < nColumn;n++)
        {
            ReportColumn cx = cols.get(n);
            if(null != cx && cx.getMergeMode().isNone() == false)
            {
                rowRet.get(n).setCellType(DataSetCellTypes.Normal);
            }
        }
        if(null != strText && strText.trim().length() > 0)
        {
            cellSubTotal.setText(strText);
        }
        for(int nCol = 0;nCol < cols.size() && nCol < rowRet.size();nCol++)
        {
            if(nCol == nColumn)
            {
                continue;
            }
            ReportColumn column = cols.get(nCol);
            boolean b = false;
            if(nCol > nColumn && (column.isDimension() || column.isSummaryable() == false || column.getMergeMode().isNone()))
            {
                b = true;
            }
            if(nCol < nColumn && column.getMergeMode().isNone() && (column.isDimension() || column.isSummaryable() == false))
            {
                b = true;
            }
            if(b)
            {
                rowRet.get(nCol).setValue(null);
                rowRet.get(nCol).setText(null);
                rowRet.get(nCol).setVisiable(!column.isHidden());
            }
        }
        for(int ncol = 0;ncol < cols.size() && ncol < rowRet.size();ncol++)
        {
            ReportColumn column = cols.get(ncol);
            DataCell cell = rowRet.get(ncol);
            cell.setVisiable(!column.isHidden());
            if(column.isSummaryable() == false || column.isDimension())
            {
                continue;
            }
            Double lfSum = cell.getValue() == null ? null : new Double(ConvertEx.toDouble(cell.getValue(),0));
            for(int nrow = nStartRow;nrow <= nEndRow && nrow < rows.size();nrow++)
            {
                RowInfo row = rows.elementAt(nrow);
                DataCell cx = row.get(ncol);
                if(cx == null || cx.getValue() == null)
                {
                    continue;
                }
                if(null == lfSum)
                {
                    lfSum = new Double(0);
                }
                lfSum = new Double(lfSum.doubleValue() + ConvertEx.toDouble(cx.getValue(),0));
            }
            cell.setValue(lfSum);
            cell.setText(null);
        }
        return rowRet;
    }
}

