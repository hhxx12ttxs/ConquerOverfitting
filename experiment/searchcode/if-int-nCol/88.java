package com.jeasonzhao.report.decorator;

import java.util.Vector;

import com.jeasonzhao.commons.logger.Logger;
import com.jeasonzhao.commons.utils.ConvertEx;
import com.jeasonzhao.commons.utils.DataTypes;
import com.jeasonzhao.report.codes.ColumnRotateHeaderModes;
import com.jeasonzhao.report.codes.DataSetCellTypes;
import com.jeasonzhao.report.codes.ReportMergeModes;
import com.jeasonzhao.report.dataset.DataCell;
import com.jeasonzhao.report.dataset.DataSet;
import com.jeasonzhao.report.dataset.RowInfo;
import com.jeasonzhao.report.dataset.RowInfoCollection;
import com.jeasonzhao.report.engine.irp.IDataSetDecorator;
import com.jeasonzhao.report.engine.repo.DictRepository;
import com.jeasonzhao.report.exceptions.DecoratorException;
import com.jeasonzhao.report.model.ObjectCollection;
import com.jeasonzhao.report.model.Report;
import com.jeasonzhao.report.model.ReportColumn;
import com.jeasonzhao.report.model.ReportColumnCollection;
import com.jeasonzhao.report.model.ReportColumnValue;
import com.jeasonzhao.report.model.ReportColumnValueCollection;
import com.jeasonzhao.report.msg.MessageCodes;

public class RotateDecorator implements IDataSetDecorator
{
    private static final String FIXSUBTOTALCELLSTRING = "FAKEVALUE_32764fcb-e5d1-4e55-91d5-c0b6b6db1abd";
    private static class RotateInfo
    {
        private ReportColumnCollection m_newColumns = null;
        private ReportColumnCollection m_measures = null;
        private ReportColumnCollection m_dimensions = null;
        private RowInfoCollection m_header = null;

        public ReportColumnCollection getNewColumns()
        {
            return m_newColumns;
        }

        public void setNewColumns(ReportColumnCollection cols)
        {
            m_newColumns = cols;
        }

        @SuppressWarnings("unused")
        public ReportColumnCollection getMeasures()
        {
            return m_measures;
        }

        public void setHeader(RowInfoCollection cols)
        {
            m_header = cols;
        }

        public RowInfoCollection getHeader()
        {
            return m_header;
        }

        public void setMeasures(ReportColumnCollection v)
        {
            m_measures = v;
        }

        @SuppressWarnings("unused")
        public ReportColumnCollection getDimensions()
        {
            return m_dimensions;
        }

        public void setDimensions(ReportColumnCollection v)
        {
            m_dimensions = v;
        }

    }

    private java.util.Hashtable<Integer,RotateInfo> m_hashRotateGrp2RotateInfo = null;
    private int m_nMaxHeaderRowsCount = 0;
    private RowInfoCollection m_newReportHeader = null;
    private ReportColumnCollection m_newReportColumns = null;
    private java.util.ArrayList<Integer> m_allDimensionIdsInNewHeader = null;
    private java.util.ArrayList<Integer> m_allDimensionIndexInNewHeader = null;
    private ReportColumnCollection m_oldColumnsDefination = null;
    public RotateDecorator(Logger logagent)
    {
    }

    public DataSet decorate(Report reportxx,DataSet dataset)
        throws DecoratorException
    {
        if(null == dataset
           || dataset.getRowsCount() < 1
           || dataset.getHeaderColumns() == null
           || dataset.getHeaderColumns().getRotateGroupNos() == null
           || dataset.getHeaderColumns().getRotateGroupNos().length < 1)
        {
            return dataset;
        }
        m_oldColumnsDefination = dataset.getHeaderColumns();
        m_nMaxHeaderRowsCount = 1;
        m_hashRotateGrp2RotateInfo = new java.util.Hashtable<Integer,RotateInfo>();
        prepareData2Rotate(dataset);
        prepareHeader();
        //???????
        RowInfoCollection newRows = prepareNewRows(dataset.getRows());
        //????

        //log.debug("fillMeasures.....................");
        newRows = fillMeasures(newRows,dataset.getRows());
        //log.debug("setHeader.....................");
        dataset.setHeader(m_newReportHeader);
        dataset.setReportColumns(m_newReportColumns);
        dataset.setRows(newRows);
        for(int ncol = 0;ncol < m_newReportColumns.size();ncol++)
        {
            m_newReportColumns.get(ncol).setColId(ncol);
        }
        return dataset;
    }

    private RowInfoCollection fillMeasures(RowInfoCollection newRows,RowInfoCollection oldRows)
    {
        for(int nnewrow = 0;nnewrow < newRows.size();nnewrow++)
        {
//            log.debug(" fillMeasures " + nnewrow + "/" + newRows.size());
            ObjectCollection newKey = (ObjectCollection) m_hashRowNo2KeyForNew.get(new Integer(nnewrow));
            RowInfo newRow = newRows.elementAt(nnewrow);
            for(int nnewcol = 0;nnewcol < m_newReportColumns.size();nnewcol++)
            {
                ReportColumn newColumn = this.m_newReportColumns.get(nnewcol);
                if(newRow.get(nnewcol) == null)
                {
                    newRow.setElementAt(new DataCell(newColumn.getDataType()),nnewcol);
                }
                DataCell cell = newRow.get(nnewcol);
                cell.setVisiable(newColumn.isHidden() == false);
                cell.setDataType(newColumn.getDataType());
                if(newColumn.isDimension())
                {
                    continue;
                }
                Double lfValue = null;
                ReportColumnValueCollection vkc = newColumn.getVerticalKeys();
                for(int noldrow = 0;noldrow < oldRows.size();noldrow++)
                {
                    ObjectCollection oldKey = (ObjectCollection) m_hashRowNo2KeyForOld.get(Integer.valueOf(noldrow));
                    if(false == (newKey == null || newKey.equals(oldKey)))
                    {
                        continue;
                    }
                    RowInfo oldrow = oldRows.elementAt(noldrow);
                    //???????key?
                    boolean isVkOk = true;
                    for(int nkc = 0;null != vkc && nkc < vkc.size();nkc++)
                    {
                        ReportColumnValue vk = vkc.get(nkc);
                        DataCell cto = oldrow.get(vk.getReportColumn().getColId());
                        Object vcto = null == cto ? null : cto.getValue();
                        isVkOk = (vcto == null && null == vk.getValue())
                            || (vk.getValue() != null && vk.getValue().equals(vcto));
                        if(isVkOk == false)
                        {
                            break;
                        }
                    }
                    if(isVkOk == false)
                    {
                        continue;
                    }
                    DataCell cellold = oldrow.get(newColumn.getColId());
                    if(null != cellold && null != cellold.getValue())
                    {
                        try
                        {
                            if(null == lfValue)
                            {
                                try
                                {
                                    lfValue = new Double(ConvertEx.toDouble(cellold.getValue()));
                                }
                                catch(Exception ex)
                                {
                                    ex.printStackTrace();
                                    lfValue = null;
                                }
                            }
                            else
                            {
                                lfValue = new Double(lfValue.doubleValue() + ConvertEx.toDouble(cellold.getValue(),0));
                            }
                        }
                        catch(Exception excep)
                        {
                            excep.printStackTrace();
                        }
                    }
                }
                cell.setValue(lfValue);
            }
        }
        return newRows;
    }

    private java.util.Hashtable<Integer,ObjectCollection> m_hashRowNo2KeyForOld = null;
    private java.util.Hashtable<Integer,ObjectCollection> m_hashRowNo2KeyForNew = null;

    private RowInfoCollection prepareNewRows(RowInfoCollection oldRows)
    {
        //log.debug("prepareNewRows............");
        m_hashRowNo2KeyForOld = new java.util.Hashtable<Integer,ObjectCollection>();
        m_hashRowNo2KeyForNew = new java.util.Hashtable<Integer,ObjectCollection>();
        RowInfoCollection newRows = new RowInfoCollection();
        if(m_allDimensionIdsInNewHeader.size() < 1)
        {
            newRows.addElement(new RowInfo(m_newReportColumns.size()));
        }
        else
        {
            java.util.HashSet<ObjectCollection> set = new java.util.HashSet<ObjectCollection>();
            for(int nrow = 0;nrow < oldRows.size();nrow++)
            {
                RowInfo rowold = oldRows.elementAt(nrow);
                RowInfo newRow = new RowInfo(m_newReportColumns.size());
                ObjectCollection key = new ObjectCollection();
                for(int n = 0;n < m_allDimensionIdsInNewHeader.size();n++)
                {
                    key.add(null == rowold.get(m_allDimensionIdsInNewHeader.get(n)) ? null
                            : rowold.get(m_allDimensionIdsInNewHeader.get(n)).getValue());
                    newRow.setElementAt(rowold.get(m_allDimensionIdsInNewHeader.get(n)),m_allDimensionIndexInNewHeader.get(n));
                }
                m_hashRowNo2KeyForOld.put(Integer.valueOf(nrow),key);
                if(set.contains(key) == false)
                {
                    set.add(key);
                    m_hashRowNo2KeyForNew.put(Integer.valueOf(newRows.size()),key);
                    newRows.addElement(newRow);
                }
            }
        }
        return newRows;
    }

    private void prepareHeader()
    {
        m_newReportHeader = new RowInfoCollection(m_nMaxHeaderRowsCount);
        m_newReportColumns = new ReportColumnCollection();
        m_allDimensionIdsInNewHeader = new java.util.ArrayList<Integer>();
        m_allDimensionIndexInNewHeader = new java.util.ArrayList<Integer>();

        //???????
        java.util.HashSet<Integer> handledGroupNos = new java.util.HashSet<Integer>();
        for(int ncolOlRp = 0;null != m_oldColumnsDefination && ncolOlRp < m_oldColumnsDefination.size();ncolOlRp++)
        {
            ReportColumn col = m_oldColumnsDefination.get(ncolOlRp);
            Integer opkey = Integer.valueOf(col.getRotateGroupNo());
            RotateInfo info = m_hashRotateGrp2RotateInfo.get(opkey);
            if(info != null)
            {
                if(handledGroupNos.contains(opkey) || info.getHeader().size() < 1)
                {
                    continue;
                }
                handledGroupNos.add(opkey);
                for(int nx = 0;nx < info.getNewColumns().size();nx++)
                {
                    m_newReportColumns.add(new ReportColumn(info.getNewColumns().get(nx)));
                }
                int lastRSpan = m_nMaxHeaderRowsCount - info.getHeader().size() + 1;
                while(info.getHeader().size() < m_nMaxHeaderRowsCount)
                {
                    RowInfo newRow = new RowInfo(info.getHeader().get(0),true,true);
                    info.getHeader().insertElementAt(newRow,1);
                }
                //????
                if(lastRSpan > 1)
                {
                    RowInfo row = info.getHeader().get(0);
                    for(int ncol2 = 0;ncol2 < row.size();ncol2++)
                    {
                        DataCell cell = row.get(ncol2);
                        cell.setRowSpan(Math.max(cell.getRowSpan(),1) - 1 + lastRSpan);
                    }
                }
                int nRColumnCount = 0;
                for(int nrow = 0;nrow < info.getHeader().size();nrow++)
                {
                    RowInfo row = info.getHeader().elementAt(nrow);
                    nRColumnCount = 0;
                    for(int ncol2 = 0;ncol2 < row.size();ncol2++)
                    {
                        DataCell cell = row.get(ncol2);
                        nRColumnCount++;
                        m_newReportHeader.elementAt(nrow).add(cell);
                    }
                }
                for(int nnewrow = info.getHeader().size();nnewrow < m_nMaxHeaderRowsCount;nnewrow++)
                {
                    for(int ncol2 = 0;ncol2 < nRColumnCount;ncol2++)
                    {
                        DataCell cell = DataCell.create(null).setVisiable(false);
                        m_newReportHeader.elementAt(nnewrow).add(cell);
                    }
                }
            }
            else
            {
                if(col.isMeasure() == false && col.isHidden() == false)
                {
                    //log.debug("Dimension " + col.getTitle());
                    m_allDimensionIdsInNewHeader.add(col.getColId());
                    m_allDimensionIndexInNewHeader.add(m_newReportColumns.size());
                }
                m_newReportColumns.add(new ReportColumn(col));
                for(int nrow = 0;nrow < m_nMaxHeaderRowsCount;nrow++)
                {
                    DataCell cell = new DataCell();
                    cell.setReportColumn(col);
                    cell.setValue(col.getTitle());
                    cell.setVisiable(col.isHidden() == false && nrow == 0);
                    //log.debug(">>>>>>>>>>>>>>>>" + col.getTitle() + ">>>" + col.isHidden());
                    if(nrow == 0 && m_nMaxHeaderRowsCount > 1)
                    {
                        cell.setRowSpan(m_nMaxHeaderRowsCount);
                    }
                    m_newReportHeader.elementAt(nrow).add(cell);
                }
            }
        }
        //log.debug("New Columns size =" + m_newReportColumns.size());
    }

    private void prepareData2Rotate(DataSet dataset)
    {
        int[] rgrps = this.m_oldColumnsDefination.getRotateGroupNos();
        for(int n = 0;n < m_oldColumnsDefination.size();n++)
        {
            if(null != m_oldColumnsDefination.get(n))
            {
                m_oldColumnsDefination.get(n).setColId(n);
            }
        }
        for(int ng = 0;null != rgrps && ng < rgrps.length;ng++)
        {
//            log.debug("processsing " + rgrps[ng]);
            RotateInfo info = processGroupHeader(m_oldColumnsDefination,dataset,rgrps[ng]);
            if(info != null)
            {
                m_hashRotateGrp2RotateInfo.put(Integer.valueOf(rgrps[ng]),info);
                m_nMaxHeaderRowsCount = Math.max(info.getHeader().size(),m_nMaxHeaderRowsCount);
            }
//            log.debug("processed " + rgrps[ng]);
        }
    }

    private RotateInfo processGroupHeader(ReportColumnCollection oldColumns,DataSet dataset,int ngroupno)
    {
        ReportColumnCollection measures = new ReportColumnCollection();
        ReportColumnCollection dimensions = new ReportColumnCollection();
        int nShownMeasureCount = 0;
        ColumnRotateHeaderModes mode = ColumnRotateHeaderModes.FixedHeader;
        for(int ncol = 0;null != oldColumns && ncol < oldColumns.size();ncol++)
        {
            ReportColumn col = oldColumns.get(ncol);
            if(null == col || col.getRotateGroupNo() != ngroupno)
            {
                continue;
            }
            if(col.isMeasure()) //?????????????????????
            {
                nShownMeasureCount += col.isHidden() ? 0 : 1;
                measures.add(col);
            }
            else if(col.isHidden() == false) //?????????
            {
                if(dimensions.size() == 0)
                {
                    mode = col.getRotateMode();
                }
                dimensions.add(col);
            }
        }
        if(nShownMeasureCount < 1 || dimensions.size() < 1)
        {
//            log.debug("??>>>" + ngroupno + " ????[" + dimensions.size() + "] ?? ????[" + nShownMeasureCount + "] ???????");
            return null;
        }
        Vector<ReportColumnValueCollection> allHeaderKeyColumns = extractColumnsKey(dataset,measures,dimensions,mode);
        ReportColumnCollection newColumns = getColumnFromValues(allHeaderKeyColumns);
        RowInfoCollection header = merge(getHeaderFromValues(allHeaderKeyColumns,newColumns,mode,nShownMeasureCount));
        RotateInfo rotateInfo = new RotateInfo();
        rotateInfo.setDimensions(dimensions);
        rotateInfo.setNewColumns(newColumns);
        rotateInfo.setMeasures(measures);
        rotateInfo.setHeader(header);
        return rotateInfo;
    }

    private Vector<ReportColumnValueCollection> extractColumnsKey(DataSet dataset,ReportColumnCollection measures,
                                     ReportColumnCollection dimensions,ColumnRotateHeaderModes mode)
    {
        java.util.Vector<ReportColumnValueCollection> allHeaderKeyColumns = mode.isFixedHeader() ?
            extractSimpleColumnsKey(dataset,dimensions) :
            extractDiscartesColumnsKey(dataset,dimensions);
        return addSubtotalColumns(dimensions,measures,allHeaderKeyColumns,mode.isRotateMeasureAtTop());
    }

    private ReportColumnCollection getColumnFromValues(Vector<ReportColumnValueCollection> allHeaderKeyColumns)
    {
        ReportColumnCollection newColumns = new ReportColumnCollection();
        for(int ncol = 0;ncol < allHeaderKeyColumns.size();ncol++)
        {
            ReportColumn newColumn = new ReportColumn();
            ReportColumnValueCollection newVK = new ReportColumnValueCollection();
            ReportColumnValueCollection column = (ReportColumnValueCollection) allHeaderKeyColumns.get(ncol);
            for(int nrow = 0;nrow < column.size();nrow++)
            {
                ReportColumnValue v = column.get(nrow);
                ReportColumn rptcol = v.getReportColumn();
                if(v.isNormalCell())
                {
                    newVK.add(v);
                }
                else if(v.isTotalCell() && null != v.getValue())
                {
                    newVK.add(v);
                }
                else if(v.isMeasure())
                {
                    newColumn.copyFrom(rptcol);
                }
            }
            if(newVK.size() > 0)
            {
                newColumn.setVerticalKeys(newVK);
            }
            newColumns.add(newColumn);
        }
        return newColumns;
    }

    private RowInfoCollection getHeaderFromValues(Vector<ReportColumnValueCollection> allHeaderKeyColumns,ReportColumnCollection newColumns2,ColumnRotateHeaderModes mode,int nShownMeasureCount)
    {
//        log.debug("Mode>>>>>>>>>>>>>>>>>>>" + mode);
        RowInfoCollection header = new RowInfoCollection();
        for(int ncol = 0;ncol < allHeaderKeyColumns.size();ncol++)
        {
            ReportColumnValueCollection column = (ReportColumnValueCollection) allHeaderKeyColumns.get(ncol);
            for(int nrow = 0;nrow < column.size();nrow++)
            {
                ReportColumnValue curKey = column.get(nrow);
                ReportColumn rptcol = curKey.getReportColumn();
                DataCell headerCell = null;
                if(curKey.isNormalCell())
                {
                    headerCell = DataCell.create(curKey.getValue());
                    if(rptcol.getDictId() != null)
                    {
                        Object valux = DictRepository.getInstance().nameOf(rptcol.getDictId(),curKey.getValue());
                        headerCell.setText(null == valux ? null : valux.toString());
                    }
                    CellFormatDecorator.formatText(rptcol,headerCell);
                }
                else if(curKey.isTotalCell())
                {
                    headerCell = DataCell.create(FIXSUBTOTALCELLSTRING);
                    headerCell.setText(rptcol.getMergeTitle() == null ? MessageCodes.get(MessageCodes.RENDER_Subtotal) : rptcol.getMergeTitle());
                    headerCell.setCellType(DataSetCellTypes.SubTotal);
                }
                else // isMeasure
                {
                    if(false == (mode.isHideSingleMeasure() && nShownMeasureCount == 1))
                    {
                        headerCell = DataCell.create(rptcol.getTitle());
                    }
                }
                if(null != headerCell)
                {
//                    cell.setText(v.toString());//todo cccc
                    headerCell.setReportColumnValue(curKey);
                    headerCell.setReportColumnValueCollection(column.left(nrow));
                    RowInfo row = null;
                    if(nrow < header.size())
                    {
                        row = header.elementAt(nrow);
                    }
                    else
                    {
                        row = new RowInfo();
                        header.addElement(row);
                    }
                    headerCell.setReportColumn(rptcol);
                    row.add(headerCell);
                }
            }
        }
        //??????????
        for(int nrow = 0;newColumns2 != null && nrow < header.size();nrow++)
        {
            RowInfo row = header.elementAt(nrow);
            for(int ncol = 0;ncol < row.size();ncol++)
            {
                if(newColumns2.get(ncol) != null && null != row.get(ncol))
                {
                    row.get(ncol).setVisiable(newColumns2.get(ncol).isHidden() == false);
                }
            }
        }
        //??????
        for(int nrow = 0;nrow < header.size();nrow++)
        {
            RowInfo row = header.elementAt(nrow);
            for(int ncol = 0;ncol < row.size();ncol++)
            {
                DataCell cell = row.get(ncol);
                if(null != cell
                   && FIXSUBTOTALCELLSTRING.equals(cell.getValue())
                   && cell.getCellType().isSubTotal()
                   && cell.isVisiable()
                    )
                {
                    //??
                    int nrowspan = 1;
                    for(int nrow2 = nrow + 1;nrow2 < header.size();nrow2++)
                    {
                        DataCell cell2 = header.elementAt(nrow2).get(ncol);
                        if(null != cell2
                           && FIXSUBTOTALCELLSTRING.equals(cell2.getValue())
                           && cell2.getCellType().isSubTotal()
                           && cell2.isVisiable()
                            )
                        {
                            cell2.setVisiable(false);
                            nrowspan++;
                        }
                    }
                    if(nrowspan > 0)
                    {
                        cell.setRowSpan(nrowspan);
                    }
                }

            }
        }
        return header;
    }

    private ObjectCollection getKeys(RowInfoCollection header,int nLastrow,int ncol)
    {
        ObjectCollection key = new ObjectCollection();
        for(int n = 0;n <= nLastrow;n++)
        {
            key.add(header.valueOf(n,ncol));
        }
        return key;
    }

    private RowInfoCollection merge(RowInfoCollection header)
    {
        ObjectCollection prevValues = null;
        int nStartColIndex = 0;
        for(int nrow = 0;nrow < header.size() - 1;nrow++)
        {
            RowInfo row = header.elementAt(nrow);
            int ncol = 0;
            int nskipHider = 0;
            for(ncol = 0;ncol < row.size();ncol++)
            {
                if(null != row.get(ncol) && row.get(ncol).isVisiable() == false)
                {
                    nskipHider++;
                    continue;
                }
                ObjectCollection curKey = this.getKeys(header,nrow,ncol);
//                log.debug("**********************" + curKey);
                if(prevValues == null)
                {
                    nStartColIndex = ncol;
                    prevValues = curKey;
                }
                else
                {
                    if(curKey.equals(prevValues) == false)
                    {
                        int ncolspan = ncol - nStartColIndex - nskipHider;
                        if(ncolspan > 1 && null != prevValues)
                        {
                            prevValues = null;
                            row.get(nStartColIndex).setColSpan(ncolspan);
                            for(int nx = 1;nx < ncolspan + nskipHider;nx++)
                            {
                                if(row.get(nStartColIndex + nx) != null)
                                {
                                    row.get(nStartColIndex + nx).setVisiable(false);
                                }
                            }
                        }
                        nskipHider = 0;
                        prevValues = curKey;
                        nStartColIndex = ncol;
                    }
                }
            }
            int ncolspan = ncol - nStartColIndex - nskipHider;
            if(ncolspan > 1 && null != prevValues)
            {
                prevValues = null;
                row.get(nStartColIndex).setColSpan(ncolspan);
                for(int nx = 1;nx < ncolspan + nskipHider;nx++)
                {
                    if(row.get(nStartColIndex + nx) != null)
                    {
                        row.get(nStartColIndex + nx).setVisiable(false);
                    }
                }
            }
        }
        return header;
    }

    private java.util.Vector<ReportColumnValueCollection> extractSimpleColumnsKey(DataSet dataset,ReportColumnCollection dimensions)
    {
        java.util.Vector<ReportColumnValueCollection> allHeaderKeyColumns = new java.util.Vector<ReportColumnValueCollection>();
        for(int nrow = 0;nrow < dataset.getRowsCount();nrow++)
        {
            RowInfo row = dataset.getRow(nrow);
            ReportColumnValueCollection rowKey = new ReportColumnValueCollection();
            for(int ndimension = 0;ndimension < dimensions.size();ndimension++)
            {
                int ncol = dimensions.get(ndimension).getColId();
                Object value = row.get(ncol) == null ? null : row.get(ncol).getValue();
                ReportColumnValue vk = new ReportColumnValue(dimensions.get(ndimension),value);
                rowKey.add(vk);
            }
            if(allHeaderKeyColumns.contains(rowKey))
            {
                continue;
            }
            else
            {
//                log.debug("add rotate key " + nrow + "/" + dataset.getRowsCount() + rowKey);
                allHeaderKeyColumns.add(rowKey);
            }
        }
        return allHeaderKeyColumns;
    }

    private java.util.Vector<ReportColumnValueCollection> extractDiscartesColumnsKey(DataSet dataset,ReportColumnCollection dimensions)
    {
        java.util.Vector<ReportColumnValueCollection> ary = new Vector<ReportColumnValueCollection>();
        for(int nd = 0;nd < dimensions.size();nd++)
        {
            int ncol = dimensions.get(nd).getColId();
            ReportColumn column = dimensions.get(nd);
            ReportColumnValueCollection ckey = new ReportColumnValueCollection();
            if(column.getMergeMode().equals(ReportMergeModes.TopSubtotal))
            {
                ckey.add(ReportColumnValue.totalValue(column));
            }
            for(int nrow = 0;nrow < dataset.getRowsCount();nrow++)
            {
                RowInfo row = dataset.getRow(nrow);
                Object value = row.get(ncol) == null ? null : row.get(ncol).getValue();
                ReportColumnValue vk = new ReportColumnValue(column,value);
                if(ckey.contains(vk) == false)
                {
                    ckey.add(vk);
                }
            }
            ary.add(ckey);
        }
        java.util.Vector<ReportColumnValueCollection> allHeaderKeyColumns = new java.util.Vector<ReportColumnValueCollection>();
        int nprevGroupCount = 1;
        for(int nd = 0;nd < ary.size();nd++)
        {
            ReportColumnValueCollection ckey = (ReportColumnValueCollection) ary.get(nd);
            //???????????=?????????
            int nrepeatCount = 1;
            for(int nx = nd + 1;nx < ary.size();nx++)
            {
                nrepeatCount *= ((ReportColumnValueCollection) ary.get(nx)).size();
            }
            int nappendColumns = 0;
            int nrowidx = 0;
            for(int ng = 0;ng < nprevGroupCount;ng++)
            {
                for(int nv = 0;nv < ckey.size();nv++)
                {
                    ReportColumnValue kv = ckey.get(nv);
                    for(int nr = 0;nr < nrepeatCount;nr++)
                    {
                        ReportColumnValueCollection kxy = null;
                        if(nrowidx < allHeaderKeyColumns.size())
                        {
                            kxy = (ReportColumnValueCollection) allHeaderKeyColumns.get(nrowidx);
                        }
                        else
                        {
                            kxy = new ReportColumnValueCollection();
                            allHeaderKeyColumns.add(kxy);
                        }
                        kxy.add(kv);
                        nrowidx++;
                    }
                }
            }
            nprevGroupCount *= (ckey.size() + nappendColumns);
        }
        return allHeaderKeyColumns;
    }

    private Vector<ReportColumnValueCollection> addSubtotalColumns(ReportColumnCollection dimensions,ReportColumnCollection measures,
                                      Vector<ReportColumnValueCollection> allHeaderKeyColumnsInit,boolean isRotateTop)
    {
//        for(int n=0;n<allHeaderKeyColumnsInit.size();n++)
//        {
//            log.debug(allHeaderKeyColumnsInit.get(n));
//        }
//        log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>[" + measures.size() + "]>>>>>>>>>>");
        //????
        ReportColumnCollection fakeColumnList = new ReportColumnCollection();
        Vector<ReportColumnValueCollection> allHeaderKeyColumns = new Vector<ReportColumnValueCollection>();
        if(isRotateTop)
        {
            fakeColumnList.add((new ReportColumn("Fake",DataTypes.DOUBLE).setMeasure(true)));
            fakeColumnList.addAll(dimensions);
            for(int nmeasure = 0;nmeasure < measures.size();nmeasure++)
            {
                ReportColumn col = measures.get(nmeasure);
                for(int nkey = 0;nkey < allHeaderKeyColumnsInit.size();nkey++)
                {
                    ReportColumnValueCollection ckv = new ReportColumnValueCollection((ReportColumnValueCollection) allHeaderKeyColumnsInit.get(nkey));
                    ckv.insertElementAt(ReportColumnValue.measureValue(col),0);
                    allHeaderKeyColumns.add(ckv);
                }
            }
        }
        else
        {
            fakeColumnList.addAll(dimensions);
            fakeColumnList.add((new ReportColumn("Fake",DataTypes.DOUBLE).setMeasure(true)));
            for(int nkey = 0;nkey < allHeaderKeyColumnsInit.size();nkey++)
            {
                for(int nmeasure = 0;nmeasure < measures.size();nmeasure++)
                {
                    ReportColumnValueCollection ckv = new ReportColumnValueCollection((ReportColumnValueCollection) allHeaderKeyColumnsInit.get(nkey));
                    ckv.add(ReportColumnValue.measureValue(measures.get(nmeasure)));
                    allHeaderKeyColumns.add(ckv);
                }
            }
        }
//        for(int n = 0;n < allHeaderKeyColumnsInit.size();n++)
//        {
//            log.debug(allHeaderKeyColumnsInit.get(n));
//        }
//        log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        //??????????
        for(int ncolumn = 0;ncolumn < fakeColumnList.size() - 1;ncolumn++)
        {
            ReportColumn col = fakeColumnList.get(ncolumn);
            if(col.getMergeMode().isNone())
            {
                continue;
            }
//            log.debug("???? .....");
            ReportColumnValueCollection prevKey = null;
            int nStartIndex = 0;
            for(int nCurIndex = 0;nCurIndex < allHeaderKeyColumns.size();nCurIndex++)
            {
                ReportColumnValueCollection rowkey = (ReportColumnValueCollection) allHeaderKeyColumns.get(nCurIndex);
                ReportColumnValueCollection curkey = rowkey.left(col.getMergeMode().isSubtotalAll() ? ncolumn + 1 : ncolumn);
                if(null == prevKey)
                {
                    nStartIndex = nCurIndex;
                    prevKey = curkey;
                }
                else if(curkey.equals(prevKey) == false)
                {
//                    log.debug("[" + ncolumn + "]Addd Start" + nStartIndex + "/" + nCurIndex + ">>>>>" + prevKey);
                    nCurIndex += addCalcColumn(allHeaderKeyColumns,prevKey,nStartIndex,nCurIndex,ncolumn,fakeColumnList) - 1;
                    nStartIndex = -1;
                    prevKey = null;
                }
            }
            //??????
            if(null != prevKey)
            {
//                log.debug("[" + ncolumn + "]Addd Start" + nStartIndex + "/" + allHeaderKeyColumns.size() + ">>>>>" + prevKey);
                addCalcColumn(allHeaderKeyColumns,prevKey,nStartIndex,allHeaderKeyColumns.size(),ncolumn,fakeColumnList);
            }
        }
//
//        for(int n=0;n<allHeaderKeyColumns.size();n++)
//        {
//            log.debug(allHeaderKeyColumns.get(n));
//        }
//
        return allHeaderKeyColumns;
    }

    private int addCalcColumn(Vector<ReportColumnValueCollection> allHeaderKeyColumns,ReportColumnValueCollection prevKey,
                              int nStartIndex,int nCurIndex,int ncolumn
                              ,ReportColumnCollection dimensions)
    {
        ReportColumn col = dimensions.get(ncolumn);
//        log.debug("?????" + col.getMergeMode().isSubtotalAll() + "? " + prevKey);
        //?????????
        Vector<ReportColumnValueCollection> rightParts = new Vector<ReportColumnValueCollection>();
        for(int nr = nStartIndex;nr < nCurIndex;nr++)
        {
            ReportColumnValueCollection rowPrev = (ReportColumnValueCollection) allHeaderKeyColumns.get(nr);
            ReportColumnValueCollection right = col.getMergeMode().isSubtotalAll() ? rowPrev.rightTotal(ncolumn) : rowPrev.right(ncolumn);
            if(rightParts.contains(right) == false)
            {
//                log.debug("?????" + col.getMergeMode().isSubtotalAll() + "? " + right);
                rightParts.add(right);
            }
        }
        //??????
        if(col.getMergeMode().isSubtotalAll() && prevKey.size() > 0)
        {
            ReportColumnValue lastNode = prevKey.get(prevKey.size() - 1);
            prevKey.setElementAt(ReportColumnValue.totalValue(lastNode.getReportColumn(),lastNode.getValue()),prevKey.size() - 1);
        }
        int nAdded = 0;
        for(int nr = 0;nr < rightParts.size();nr++)
        {
            ReportColumnValueCollection newR = new ReportColumnValueCollection();
            newR.addCollection(prevKey);
            if(col.getMergeMode().isSubtotalAll() == false)
            {
                newR.add(ReportColumnValue.totalValue(col));
            }
            newR.addCollection((ReportColumnValueCollection) rightParts.get(nr));
            if(allHeaderKeyColumns.contains(newR))
            {
                nAdded++;
//                log.debug("????? " + newR);
                continue;
            }
            if(col.getMergeMode().isSubtotalTop())
            {
                allHeaderKeyColumns.insertElementAt(newR,nStartIndex + nr);
            }
            else
            {
                allHeaderKeyColumns.insertElementAt(newR,nCurIndex + nr);
            }
        }
        return rightParts.size() - nAdded;
    }
}

