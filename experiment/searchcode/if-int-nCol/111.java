package com.jeasonzhao.commons.excel;

import org.apache.poi.ss.util.CellRangeAddress;

public final class Range
{
    private int m_nFirstRow = 0
        ,m_nFirstCol = 0
        ,m_nLastRow = 0
        ,m_nLastCol = 0;
    public Range(int s_row,int s_col,int e_row,int e_col)
    {
        this.m_nFirstRow = s_row;
        this.m_nLastRow = e_row;
        this.m_nFirstCol = s_col;
        this.m_nLastCol = e_col;
    }

    public Range(CellRangeAddress range)
    {
        if(null != range)
        {
            this.m_nFirstRow = range.getFirstRow();
            this.m_nLastRow = range.getLastRow();
            this.m_nFirstCol = range.getFirstColumn();
            this.m_nLastCol = range.getLastColumn();
        }
    }

    public static Range createBySpan(int nrow,int ncol,int rowspan,int colspan)
    {
        return new Range(nrow,ncol
                         ,nrow + Math.max(1,rowspan) - 1
                         ,ncol + Math.max(1,colspan) - 1);
    }

    public CellRangeAddress toHSSFRange()
    {
        return new CellRangeAddress(m_nFirstRow,m_nLastRow,m_nFirstCol,m_nLastCol);
    }

    public int getFirstRow()
    {
        return m_nFirstRow;
    }

    public int getLastRow()
    {
        return m_nLastRow;
    }

    public int getFirstColumn()
    {
        return m_nFirstCol;
    }

    public int getLastColumn()
    {
        return m_nLastCol;
    }

    public int hashCode()
    {
        return super.hashCode();
    }

    public void setFirstRow(int x1)
    {
        this.m_nFirstRow = x1;
    }

    public void setLastRow(int x2)
    {
        this.m_nLastRow = x2;
    }

    public void setFirstColumn(int y1)
    {
        this.m_nFirstCol = y1;
    }

    public void setLastColumn(int y2)
    {
        this.m_nLastCol = y2;
    }

    public static Range empty()
    {
        return new Range(0,0,0,0);
    }

    public boolean isTopLeft(int nRow,int nCol)
    {
        return this.m_nFirstRow == nRow && this.m_nFirstCol == nCol;
    }

    public boolean isContains(int nRow,int nCol)
    {
        return nRow >= m_nFirstRow && nRow <= m_nLastRow
            && nCol >= m_nFirstCol && nCol <= m_nLastCol;
    }

    public int getCellsCount()
    {
        return Math.max(0,(m_nLastRow - m_nFirstRow + 1) * (m_nLastCol - m_nFirstCol + 1));
    }

    public boolean equals(Object obj)
    {
        if(null != obj && obj instanceof Range)
        {
            Range left = (Range) obj;
            return left.m_nFirstRow == this.m_nFirstRow && left.m_nFirstCol == this.m_nFirstCol &&
                left.m_nLastRow == this.m_nLastRow && left.m_nLastCol == this.m_nLastCol;
        }
        return false;
    }

    public String toString()
    {
        return "{(" + m_nFirstRow + "," + m_nFirstCol + ");(" + m_nLastRow + "," + m_nLastCol + ")}";
    }
}

