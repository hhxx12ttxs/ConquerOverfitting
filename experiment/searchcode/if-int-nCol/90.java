package com.jeasonzhao.report.dataset;

import java.util.Vector;

import com.jeasonzhao.commons.utils.ConvertEx;
import com.jeasonzhao.commons.xml.XMLException;
import com.jeasonzhao.commons.xml.XMLHelper;
import com.jeasonzhao.commons.xml.XMLNode;

public class RowInfoCollection extends Vector<RowInfo>
{
    private static final long serialVersionUID = 1L;
	private boolean m_bIsCellSpaned = false;
    public RowInfoCollection()
    {
    }

    public RowInfoCollection(int nSize)
    {
        for(int n = 0;n < nSize;n++)
        {
            this.addElement(new RowInfo());
        }
    }

    public boolean isCellSpanned()
    {
        return m_bIsCellSpaned;
    }

    public void setCellSpanned(boolean b)
    {
        m_bIsCellSpaned = b;
    }
    public int hashCode()
    {
    	return super.hashCode();
    }
    public boolean equals(Object obj)
    {
    	return super.equals(obj);
    }
    public Object valueOf(int nrow,int ncol)
    {
        if(nrow < 0 || nrow >= this.size())
        {
            return null;
        }
        RowInfo row = this.elementAt(nrow);
        if(row == null || ncol >= row.size() || ncol < 0)
        {
            return null;
        }
        return null == row.get(ncol) ? null : row.get(ncol).getValue();
    }

    public java.util.Vector<Range> getAllRanges()
    {
        java.util.Vector<Range> ret = new java.util.Vector<Range>();
        for(int nRow = 0;nRow < this.size();nRow++)
        {
            RowInfo row = this.elementAt(nRow);
            for(int nCol = 0;nCol < row.size();nCol++)
            {
                boolean bInRange = false;
                for(int nR = 0;nR < ret.size();nR++)
                {
                    Range r = ret.elementAt(nR);
                    if(r.isContains(nRow,nCol))
                    {
                        bInRange = true;
                        break;
                    }
                }
                if(!bInRange)
                {
                    DataCell cell = row.get(nCol);
                    Range range = cell.getRange();
                    if(null != range)
                    {
                        ret.addElement(range);
                    }
                }
            }
        }
        return ret;
    }

    public void resetCellPosition()
    {
        for(int nRow = 0;nRow < this.size();nRow++)
        {
            RowInfo row = this.elementAt(nRow);
            row.setRowIndex(nRow);
            for(int nCol = 0;nCol < row.size();nCol++)
            {
                row.get(nCol).setPosition(nRow,nCol);
            }
        }
    }

    public void hiddenAllRangedCells()
    {
        resetCellPosition();
        if(this.m_bIsCellSpaned)
        {
            return;
        }
        java.util.Vector<Range> rcs = this.getAllRanges();
        for(int nRow = 0;nRow < this.size();nRow++)
        {
            RowInfo row = this.elementAt(nRow);
            row.setRowIndex(nRow);
            for(int nCol = 0;nCol < row.size();nCol++)
            {
                if(isCellInsideRanges(rcs,nRow,nCol))
                {
                    row.get(nCol).setVisiable(false);
                }
            }
        }
    }

    public boolean isCellInsideRanges(java.util.Vector<Range> rcs,int nRow,int nCol)
    {
        for(int nR = 0;nR < rcs.size();nR++)
        {
            Range r = rcs.elementAt(nR);
            if(r.isContains(nRow,nCol) && r.isTopLeft(nRow,nCol) == false)
            {
                return true;
            }
        }
        return false;
    }

    private Range getCellRange(java.util.Vector<Range> rcs,int nRow,int nCol)
    {
        for(int nR = 0;nR < rcs.size();nR++)
        {
            Range r = rcs.elementAt(nR);
            if(r.isContains(nRow,nCol) && r.isTopLeft(nRow,nCol) == false)
            {
                return r;
            }
        }
        return null;
    }

    private DataCell getCellRangeTopLeft(java.util.Vector<Range> rcs,int nRow,int nCol)
    {
        Range r = this.getCellRange(rcs,nRow,nCol);
        if(null == r)
        {
            return this.get(nRow).get(nCol);
        }
        else
        {
            return this.get(r.getBeginRowIndex()).get(r.getBeginColIndex());
        }
    }

    public static RowInfoCollection fromTableHTML(String strHTML,boolean isExpand,boolean isClearSpan)
        throws XMLException
    {
        return fromTableHTML(XMLHelper.fromString(strHTML),isExpand,isClearSpan);
    }

    public static RowInfoCollection fromTableHTML(XMLNode xml,boolean isExpand,boolean isClearSpan)
    {
        RowInfoCollection rows = new RowInfoCollection();
        for(XMLNode nodeTr : xml.selectNodes("tr"))
        {
            RowInfo row = new RowInfo();
            for(XMLNode nodeTd : nodeTr.selectNodes("td","th"))
            {
                DataCell cell = new DataCell();
                cell.setRowSpan(ConvertEx.toInt(nodeTd.valueOf("rowspan"),0));
                cell.setColSpan(ConvertEx.toInt(nodeTd.valueOf("colspan"),0));
                cell.setValue(nodeTd.getInnerText());
                row.add(cell);
            }
        }
        if(isExpand)
        {
            rows.expandSpans(isClearSpan);
        }
//        System.out.println(rows.toHTML(true));
        return rows;
    }

    public String toHTML(boolean bAddTableTag)
    {
        StringBuffer strHTML = bAddTableTag ? new StringBuffer("<table>") : new StringBuffer();
        for(int nrow = 0;nrow < this.size();nrow++)
        {
            strHTML.append("<tr>");
            RowInfo row = this.elementAt(nrow);
            for(int ncol = 0;ncol < row.size();ncol++)
            {
                DataCell cell = row.elementAt(ncol);
                if(cell.isVisiable() == false)
                {
                    continue;
                }
                strHTML.append("<td" +
                               (cell.getRowSpan() < 2 ? "" : " rowspan=\"" + cell.getRowSpan() + "\"") +
                               (cell.getColSpan() < 2 ? "" : " colspan=\"" + cell.getColSpan() + "\"") +
                               " nowrap>");
                strHTML.append(cell.getText() == null ? "&nbsp;" : cell.getText());
                strHTML.append("</td>");
            }
            strHTML.append("</tr>");
        }
        if(bAddTableTag)
        {
            strHTML.append("</table>");
        }
        return strHTML.toString();
    }

    public RowInfo createRow(Object[] objs)
    {
        if(null == objs)
        {
            return null;
        }
        RowInfo row = new RowInfo();
        for(int n = 0;n < objs.length;n++)
        {
            DataCell cell = DataCell.create(objs[n]);
            row.add(cell);
        }
        this.addElement(row);
        return row;
    }

    public void expandSpans(boolean bClearSpan)
    {
        for(int nrow = 0;nrow < this.size();nrow++)
        {
            RowInfo row = this.elementAt(nrow);
            for(int ncurrentcol = 0;ncurrentcol < row.size();ncurrentcol++)
            {
                DataCell cell = row.elementAt(ncurrentcol);
                int rowspan = Math.max(1,cell.getRowSpan());
                int colspan = Math.max(1,cell.getColSpan());
                cell.setRowSpan(0);
                cell.setColSpan(0);
//                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>["+nrow+","+ncol+"]"+"{"+rowspan+","+colspan+"}");
                for(int xrow = 1;xrow < rowspan;xrow++)
                {
//                    System.out.println("AAAA["+nrow+","+ncol+"]rowspan "+rowspan+" colspan "+colspan);
                    RowInfo rowNext = this.elementAt(nrow + xrow);
                    for(int xcol = 0;xcol < colspan;xcol++)
                    {
//                        System.out.println("------------------");
                        DataCell cellNew = DataCell.createFrom(cell).setVisiable(bClearSpan);
                        if(ncurrentcol >= rowNext.size())
                        {
                            rowNext.add(cellNew);
                        }
                        else
                        {
                            rowNext.insertElementAt(cellNew,ncurrentcol);
                        }
                    }
                }
                for(int xcol = 1;xcol < colspan;xcol++)
                {
                    DataCell cellNew = DataCell.createFrom(cell).setVisiable(bClearSpan);
                    if(ncurrentcol + 1 >= row.size())
                    {
                        row.add(cellNew);
                    }
                    else
                    {
                        row.insertElementAt(cellNew,ncurrentcol + 1);
                    }
                }
                if(bClearSpan == false)
                {
                    cell.setRowSpan(rowspan);
                    cell.setColSpan(colspan);
                }
                ncurrentcol += Math.max(colspan - 1,0);
            }
        }

    }

    public void join(RowInfoCollection rows)
    {
        if(rows == null || rows.size() < 1)
        {
            return;
        }
        int nNewRows = Math.max(rows.size(),this.size());
        this.appendNewRow4Join(nNewRows);
        rows.appendNewRow4Join(nNewRows);
        for(int nrow = 0;nrow < rows.size();nrow++)
        {
            this.get(nrow).addAll(rows.get(nrow));
        }
    }

    private void appendNewRow4Join(int nDesignSize)
    {
        if(nDesignSize - this.size() <= 0)
        {
            return;
        }
        //???????rowspan
        this.resetCellPosition();
        //??????
        RowInfo lastrow = this.size() < 1 ? null : this.elementAt(this.size() - 1);
        int nRowIndex = this.size();
        int nLastRowIndex = this.size() - 1;
        java.util.Vector<Range> rc = null;
        while(this.size() < nDesignSize)
        {
            RowInfo newRow = new RowInfo();
            for(int ncol = 0;null != lastrow && ncol < lastrow.size();ncol++)
            {
                if(null == rc)
                {
                    rc = this.getAllRanges();
                }
                DataCell cellLastRow = lastrow.get(ncol);
                DataCell cell = DataCell.createFrom(cellLastRow);
                cell.setPosition(nRowIndex,ncol);
                cell.setVisiable(false);
                newRow.add(cell);
                //??????Cell??????????????????
                if(false == this.isCellInsideRanges(rc,nRowIndex,ncol))
                {
                    DataCell cellTopLeft = this.getCellRangeTopLeft(rc,nLastRowIndex,ncol);
                    cellTopLeft.setRowSpan((cellTopLeft.getRowSpan() < 1 ? 1 : cellTopLeft.getRowSpan()) + 1);
                    rc = this.getAllRanges(); //??????
                }
            }
            this.addElement(newRow);
            nRowIndex++;
        }
    }

    public void sort(RowInfoComparator c)
    {
        if(null == c || this.size() < 2)
        {
            return;
        }
        RowInfo[] ary = new RowInfo[this.size()];
        this.copyInto(ary);
        java.util.Arrays.sort(ary,c);
        this.clear();
        for(int n = 0;n < ary.length;n++)
        {
            this.add(ary[n]);
        }
    }

    public static void main(String[] argvs)
    {
        RowInfoCollection f = new RowInfoCollection();
        for(int n = 0;n < 2;n++)
        {
            f.addElement(RowInfo.fromArrayValues(new Object[]
                                                 {n + "A",n + "B",n + "C"}));
        }
        RowInfoCollection fs = new RowInfoCollection();
        for(int n = 0;n < 3;n++)
        {
            fs.addElement(RowInfo.fromArrayValues(new Object[]
                                                  {n + "A2",n + "B2",n + "C2"}));
        }
        f.join(fs);
        System.out.println(f.toHTML(true));
    }
}

