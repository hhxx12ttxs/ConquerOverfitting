package com.jeasonzhao.commons.excel;

import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.util.CellReference;

public class Sheet
{
    private HSSFSheet m_sheet = null;
    private WorkBook m_workBook = null;
    public Sheet(WorkBook workBook,HSSFSheet s)
    {
        m_sheet = s;
        m_workBook = workBook;
    }

    public HSSFSheet getHSSFSheet()
    {
        return m_sheet;
    }

    public String getName()
    {
        return m_workBook.getHSSFWorkbook().getSheetName(
            m_workBook.getHSSFWorkbook().getSheetIndex(this.m_sheet));
    }

    public Row row(int rownum)
    {
        if(null == m_sheet)
        {
            return null;
        }
        org.apache.poi.hssf.usermodel.HSSFRow hssfrow = m_sheet.getRow(rownum);
        return null == hssfrow
            ? null
            : new Row(this.m_workBook,this,hssfrow);
    }

    public Row rowEx(int rownum)
    {
        if(null == m_sheet)
        {
            return null;
        }
        org.apache.poi.hssf.usermodel.HSSFRow hssfrow = m_sheet.getRow(rownum);
        return null == hssfrow
            ? new Row(this.m_workBook,this,m_sheet.createRow(rownum))
            : new Row(this.m_workBook,this,hssfrow);
    }

    private HSSFPatriarch m_HSSFPatriarch = null;
    public HSSFPatriarch getHSSFPatriarch()
    {
        if(null == m_HSSFPatriarch)
        {
            m_HSSFPatriarch = this.m_sheet.createDrawingPatriarch();
        }
        return m_HSSFPatriarch;
    }

    public Sheet removeRow(int nrow)
    {
        Row row = this.row(nrow);
        if(row != null) //hssf has a row.
        {
            row.remove();
        }
        return this;
    }

    public int getFirstRowNum()
    {
        return m_sheet.getFirstRowNum();
    }

    public int getLastRowNum()
    {
        return m_sheet.getLastRowNum();
    }

    public Sheet setColumnWidth(int column,int width)
    {
        m_sheet.setColumnWidth(column,width);
        return this;
    }

    public Sheet setColumnWidthByCharacter(int column,int width)
    {
        //set column widths, the width is measured in units of 1/256th of a character width
        m_sheet.setColumnWidth(column,(width + 1) * 256);
        return this;
    }

    public Sheet mergeCells(Range range)
    {
        m_sheet.addMergedRegion(range.toHSSFRange());
        return this;
    }

    public Sheet mergeCells(int row,int ncol,int row2,int col2)
    {
        return this.mergeCells(new Range(row,ncol,row2,col2));
    }

    public Sheet mergeCellsBySpan(int row,int ncol,int rowspan,int colspan)
    {
        return this.mergeCells(Range.createBySpan(row,ncol,rowspan,colspan));
    }

    public Range getMergedRegionAt(int index)
    {
        return new Range(m_sheet.getMergedRegion(index));
    }

    public int getMergedRegionCount()
    {
        return m_sheet.getNumMergedRegions();
    }

    public Cell getCell(int nRow,int col)
    {
        Row row = this.row(nRow);
        return null == row ? null : row.cell(col);
    }

    public Cell cell(int nRow,int col)
    {
        Row row = this.row(nRow);
        return null == row ? null : row.cell(col);
    }

    public Cell cell(String cellString)
    {
        CellReference cr = new CellReference(cellString);
        return this.getCell(cr.getRow(),cr.getCol());
    }

    public Cell cellEx(int nRow,int col)
    {
        return this.rowEx(nRow).cellEx(col);
    }

    public Cell cellEx(String cellString)
    {
        CellReference cr = new CellReference(cellString);
        return this.cellEx(cr.getRow(),cr.getCol());
    }

    public ArrayList<Cell> getCells(String ...allCells)
    {
        ArrayList<Cell> ret = new ArrayList<Cell>();
        if(null == allCells)
        {
            return ret;
        }
        for(String s : allCells)
        {
            Cell cell = this.cell(s);
            if(null != cell)
            {
                ret.add(cell);
            }
        }
        return ret;
    }

    public ArrayList<Row> getRows()
    {
        ArrayList<Row> ret = new ArrayList<Row>();
        for(int n = 0;n <= this.getLastRowNum();n++)
        {
            ret.add(this.row(n));
        }
        return ret;
    }

    public Range getTopRegion(int nRow,int nCol)
    {
        for(int n = 0;n < this.getMergedRegionCount();n++)
        {
            Range range = this.getMergedRegionAt(n);
            if(null != range && range.isContains(nRow,nCol))
            {
                return range;
            }
        }
        return null;
    }

    public Range getRegionFromTopLeft(int nRow,short nCol)
    {
        for(int n = 0;n < this.getMergedRegionCount();n++)
        {
            Range range = this.getMergedRegionAt(n);
            if(null != range && range.isTopLeft(nRow,nCol))
            {
                return range;
            }
        }
        return null;
    }

    public Cell getTopLeftCell(int nRow,short nCol)
    {
        Range region = this.getTopRegion(nRow,nCol);
        if(null == region)
        {
            return this.cellEx(nRow,nCol);
        }
        else
        {
            return this.cellEx(region.getFirstRow(),region.getFirstColumn());
        }
    }

    public Sheet autoSizeColumn(int n)
    {
        this.m_sheet.autoSizeColumn(n);
        return this;
    }
}

