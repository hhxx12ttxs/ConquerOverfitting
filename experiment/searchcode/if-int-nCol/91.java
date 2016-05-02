package com.jeasonzhao.report.dataset;

import java.io.Serializable;

import com.jeasonzhao.report.model.ReportAgentSummary;
import com.jeasonzhao.report.model.ReportColumn;
import com.jeasonzhao.report.model.ReportColumnCollection;

public class DataSet implements Serializable
{
    private static final long serialVersionUID = 1L;
    private PageInfo m_pageInfo = null;
    private RowInfoCollection m_allRowInfos = new RowInfoCollection();
    private RowInfoCollection m_allHeaderRows = new RowInfoCollection();
    private ReportColumnCollection m_allColumnsInfo = new ReportColumnCollection();
    private ReportAgentSummary m_ReportPDH = null;
    private String m_strHeader = null;
    private String m_strFooter = null;
    public DataSet()
    {
    }

    public boolean isEmpty()
    {
        return((null == m_allHeaderRows ? 0 : m_allHeaderRows.size())
               + (null == m_allRowInfos ? 0 : m_allRowInfos.size())) <= 0;
    }

    public void join(DataSet set)
    {
        if(null == set)
        {
            return;
        }
        if(null == m_allRowInfos)
        {
            m_allRowInfos = new RowInfoCollection();
        }
        m_allRowInfos.join(set.getRows());
        if(null == m_allHeaderRows)
        {
            m_allHeaderRows = new RowInfoCollection();
        }
        m_allHeaderRows.join(set.getHeader());
        if(null == m_allColumnsInfo)
        {
            m_allColumnsInfo = new ReportColumnCollection();
        }
        m_allColumnsInfo.addAll(set.getHeaderColumns());
    }

    public String getHtmlHeader()
    {
        return m_strHeader == null || m_strHeader.trim().length() < 1 ? null : m_strHeader;
    }

    public String getHtmlFooter()
    {
        return m_strFooter == null || m_strFooter.trim().length() < 1 ? null : m_strFooter;
    }

    public void setHtmlFooter(String str)
    {
        m_strFooter = str;
    }

    public void setHtmlHeader(String str)
    {
        m_strHeader = str;
    }

    public ReportColumnCollection getHeaderColumns()
    {
        return m_allColumnsInfo;
    }

    public RowInfoCollection getRows()
    {
        return m_allRowInfos;
    }

    public int getRowsCount()
    {
        return null == m_allRowInfos ? 0 : m_allRowInfos.size();
    }

    public void setRows(RowInfoCollection Rows)
    {
        this.m_allRowInfos = Rows;
    }

    public void addRow(RowInfo row)
    {
        m_allRowInfos.addElement(row);
    }

    public void insertRowAt(RowInfo row,int nRow)
    {
        m_allRowInfos.insertElementAt(row,nRow);
    }

    public void setReportColumns(ReportColumnCollection cols)
    {
        m_allColumnsInfo = cols;
    }

    public RowInfo getRow(int nRow)
        throws ArrayIndexOutOfBoundsException
    {
        if(nRow >= m_allRowInfos.size() || nRow < 0)
        {
            return null;
        }
        else
        {
            return m_allRowInfos.elementAt(nRow);
        }
    }

    public ReportColumn getHeaderColumn(int nCol)
    {
        if(null == m_allColumnsInfo)
        {
            return null;
        }
        if(nCol >= m_allColumnsInfo.size() || nCol < 0)
        {
            return null;
        }
        else
        {
            return m_allColumnsInfo.elementAt(nCol);
        }
    }

    public void setAt(int nRow,int nCol,DataCell cell)
        throws ArrayIndexOutOfBoundsException
    {
        RowInfo row = this.getRow(nRow);
        if(nCol < 0 || nCol >= row.size())
        {
            return;
        }
        else
        {
            row.setElementAt(cell,nCol);
        }
    }

    public DataCell getAt(int nRow,int nCol)
    {
        RowInfo row = this.getRow(nRow);
        if(nCol < 0 || nCol >= row.size())
        {
            return null;
        }
        else
        {
            return row.get(nCol);
        }
    }

    public void hiddenAllRangedCells()
    {
        this.m_allRowInfos.hiddenAllRangedCells();
        this.m_allHeaderRows.hiddenAllRangedCells();
    }

    public RowInfoCollection getHeader()
    {
        return this.m_allHeaderRows;
    }

    public void setHeader(RowInfoCollection cols)
    {
        this.m_allHeaderRows = cols;
    }

    public void generateDefaultHeader()
    {
        m_allHeaderRows.clear();
        RowInfo row = new RowInfo();
        for(int i = 0;i < this.m_allColumnsInfo.size();i++)
        {
            ReportColumn col = m_allColumnsInfo.elementAt(i);
            DataCell cell = new DataCell();
            cell.setValue(col.getFieldName());
            cell.setReportColumn(col);
            cell.setText(col.getTitle());
            cell.setVisiable(col.isHidden() == false);
            row.addElement(cell);
        }
        m_allHeaderRows.addElement(row);
    }

    public PageInfo getPageInfo()
    {
        return m_pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo)
    {
        this.m_pageInfo = pageInfo;
    }

    public void setReportPDH(ReportAgentSummary pdh)
    {
        m_ReportPDH = pdh;
    }

    public ReportAgentSummary getReportPDH()
    {
        return m_ReportPDH;
    }

    public void cleanHideCells()
    {
        for(int n = 0;n < this.m_allColumnsInfo.size();n++)
        {
            if(this.m_allColumnsInfo.elementAt(n) == null ||
               m_allColumnsInfo.elementAt(n).isHidden())
            {
                m_allColumnsInfo.remove(n);
                n = n == 0 ? n : n - 1;
            }
        }
        for(int n = 0;n < this.m_allHeaderRows.size();n++)
        {
            RowInfo row = m_allHeaderRows.elementAt(n);
            for(int ncol = 0;ncol < row.size();ncol++)
            {
                if(row.elementAt(ncol) == null || row.elementAt(ncol).isVisiable() == false)
                {
                    row.remove(ncol);
                    ncol = ncol == 0 ? 0 : ncol - 1;
                }
            }
        }
        for(int n = 0;n < this.m_allRowInfos.size();n++)
        {
            RowInfo row = m_allRowInfos.elementAt(n);
            for(int ncol = 0;ncol < row.size();ncol++)
            {
                if(row.elementAt(ncol) == null || row.elementAt(ncol).isVisiable() == false)
                {
                    row.remove(ncol);
                    ncol = ncol == 0 ? 0 : ncol - 1;
                }
            }
        }

    }

    /**
     * @deprecated ???????????????????
     * @return String
     */
    public String toHtml()
    {
        String str = "<table border='1' width='1%'>";
        if(this.getHeader() != null)
        {
            str += this.getHeader().toHTML(false);
        }
//        if(this.getHeaderColumns()!=null)
//        {
//            str+="<tr>";
//            for(int n=0;n<this.getHeaderColumns().size();n++)
//            {
//                str+="<td>";
//                str+=this.getHeaderColumn(n).getTitle()+"<br/>"+this.getHeaderColumn(n).getVerticalKeys();
//                str+="</td>";
//            }
//            str+="</tr>";
//        }

        if(this.getRows() != null)
        {
            str += this.getRows().toHTML(false);
        }
        str += "</table>";
        return str;
    }
}

