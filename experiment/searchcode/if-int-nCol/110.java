package com.jeasonzhao.report.model.chart;

import com.jeasonzhao.commons.basic.StringCollection;
import com.jeasonzhao.commons.utils.ConvertEx;
import com.jeasonzhao.model.AbstractModelItem;
import com.jeasonzhao.model.Model;
import com.jeasonzhao.model.ModelField;
import com.jeasonzhao.report.dataset.DataCell;
import com.jeasonzhao.report.dataset.DataSet;
import com.jeasonzhao.report.dataset.RowInfo;
import com.jeasonzhao.report.engine.repo.DictRepository;
import com.jeasonzhao.report.exceptions.SQLReportException;
import com.jeasonzhao.report.exceptions.RenderException;
import com.jeasonzhao.report.model.ReportColumn;
import com.jeasonzhao.report.model.ReportColumnValue;

@Model("series")
public class DataSeries extends AbstractModelItem
{
	private static final long serialVersionUID=1L;
     //Which chart type used in this data series
    @ModelField(names = "type,mode,chartType,chart")
    private ChartTypes m_chartType = null;

    //The name shown for this data series
    @ModelField(names = "title,name")
    private String m_legendText = null;

    //Dimensions used in this data series
    @ModelField(names = "dimension,x,dim")
    private String m_dimension = null;

    //Measure to retrieve data
    @ModelField(names = "measure,y,mea")
    private String m_measure = null;

    //
    @ModelField(names = "zmeasure,z,zmea")
    private String m_zMeasure = null;

    @ModelField(names = "shadow,ShadowEnabled")
    private boolean m_ShadowEnabled = false;

    @ModelField(names = "label,LabelEnabled")
    private boolean m_LabelEnabled = false;

    @ModelField(names = "axis,axistype")
    private AxisTypes axisType = AxisTypes.Primary;

    @ModelField()
    private boolean bevel = true;

    @ModelField()
    private boolean enabled = true;

    @ModelField()
    private int labelAngle = 0;

    @ModelField()
    private LineStyles lineStyle = LineStyles.Solid;

    @ModelField()
    private int lineThickness = 4;

    @ModelField()
    private float opacity = 1;

    private DataPointMatrix m_datasMatrix = null;

    public DataSeries()
    {
    }

    public boolean isShadowEnabled()
    {
        return m_ShadowEnabled;
    }

    public boolean isLabelEnabled()
    {
        return m_LabelEnabled;
    }

    public DataPointMatrix getDataPointMatrix()
    {
        return m_datasMatrix;
    }

    public DataSeries(DataSeries ds)
    {
        if(null != ds)
        {
            this.m_chartType = ds.m_chartType;
            this.m_legendText = ds.m_legendText;
            this.m_dimension = ds.m_dimension;
            this.m_zMeasure = ds.m_zMeasure;
            this.m_measure = ds.m_measure;
            this.axisType = ds.axisType;

            this.bevel = ds.bevel;
            this.enabled = ds.enabled;
            this.labelAngle = ds.labelAngle;
            this.lineStyle = ds.lineStyle;
            this.lineThickness = ds.lineThickness;
            this.opacity = ds.opacity;
        }
    }

    public String toString()
    {
        return this.m_datasMatrix == null ? "" : m_datasMatrix.toString();
    }

    public String getZMeasure()
    {
        return m_zMeasure == null || m_zMeasure.trim().length() < 1 ? null : m_zMeasure;
    }

    public void setZMeasure(String str)
    {
        m_zMeasure = str;
    }

    public String getDimension()
    {
        return m_dimension == null || m_dimension.trim().length() < 1 ? null : m_dimension;
    }

    public AxisTypes getAxisType()
    {
        return axisType;
    }

    public boolean isBevel()
    {
        return bevel;
    }

    public int getLabelAngle()
    {
        return labelAngle;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public LineStyles getLineStyle()
    {
        return lineStyle;
    }

    public int getLineThickness()
    {
        return lineThickness;
    }

    public float getOpacity()
    {
        return opacity;
    }

    public StringCollection getMeasures()
    {
        if(null == m_measure)
        {
            return null;
        }
        else
        {
            return StringCollection.splitString(m_measure,",");
        }
    }

    public void setDimension(String str)
    {
        m_dimension = str;
    }

    public void setAxisType(AxisTypes axisType)
    {
        this.axisType = axisType;
    }

    public void setBevel(boolean bevel)
    {
        this.bevel = bevel;
    }

    public void setLabelAngle(int labelAngle)
    {
        this.labelAngle = labelAngle;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void setLineStyle(LineStyles lineStyle)
    {
        this.lineStyle = lineStyle;
    }

    public void setLineThickness(int lineThickness)
    {
        this.lineThickness = lineThickness;
    }

    public void setOpacity(float opacity)
    {
        this.opacity = opacity;
    }

    //    public void setMeasures(StringCollection str)
//    {
//        m_strAllMeasures = str;
//    }

    public void setTitle(String t)
    {
        m_legendText = t;
    }

    public String getTitle()
    {
        return m_legendText;
    }

    public void setChartType(ChartTypes t)
    {
        m_chartType = t;
    }

    public ChartTypes getChartType()
    {
        return m_chartType;
    }

    void initDataSet(DataSet dataset)
        throws SQLReportException
    {
//        log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>initDataSet"+this.getDimension());
        try
        {
            if(null == dataset || dataset.getHeaderColumns() == null || dataset.getRowsCount() < 1)
            {
                return;
            }
            if(null == this.getDimension() || null == this.getMeasures())
            {
                throw new RenderException("?????????????????Dimension??Measure????");
            }
            this.m_datasMatrix = new DataPointMatrix();
            for(int n = 0;n < this.getMeasures().size();n++)
            {
                String strMeasure = this.getMeasures().get(n);
                ReportColumn col = checkMeasure(dataset,strMeasure);
                DataPointAry ary = new DataPointAry(col.getTitle());
                m_datasMatrix.add(ary);
            }
            if(null != this.getZMeasure())
            {
                checkMeasure(dataset,getZMeasure());
            }
            //?????????
            //???Dimension???????????????????
            int nDimensionColumnIndex = -1;
            for(int ncol = 0;ncol < dataset.getHeaderColumns().size();ncol++)
            {
                ReportColumn col = dataset.getHeaderColumns().get(ncol);
                if(null != col && (this.getDimension().equalsIgnoreCase(col.getFieldName()) || this.getDimension().equalsIgnoreCase(col.getGuid())))
                {
                    if(col.isMeasure())
                    {
                        throw new RenderException("????????????????" + col.getTitle() + "/" + this.getDimension() + "??????????");
                    }
                    nDimensionColumnIndex = ncol;
                }
            }
            //??????????
            for(int nrow = 0;nrow < dataset.getRowsCount();nrow++)
            {
                RowInfo row = dataset.getRow(nrow);
                if(null == row || row.getRowType().isNormal() == false)
                {
                    continue;
                }
                if(nDimensionColumnIndex >= 0)
                {
                    DataCell cell = row.get(nDimensionColumnIndex);
                    if(null == cell || cell.getValue() == null)
                    {
                        continue;
                    }
                    String strDimensionName = cell.getText();
                    double zValue = this.getZMeasure() == null ? 0 : getMeasureValueFromNormalRow(dataset,row,cell,this.getZMeasure());
                    for(int nm = 0;nm < this.getMeasures().size();nm++)
                    {
                        double lfVa = getMeasureValueFromNormalRow(dataset,row,cell,this.getMeasures().get(nm));
//                    log.debug(">>>nDimensionColumnIndex >= 0)");
                        this.addElement(m_datasMatrix.get(nm),strDimensionName,lfVa,zValue);
                    }
                }
                else
                {
                    for(int nm = 0;nm < this.getMeasures().size();nm++)
                    {
                        getMeasureValueFromVectColumn(dataset,row,this.getMeasures().get(nm),nm);
                    }
                }
            }
        }
        catch(ArrayIndexOutOfBoundsException ex)
        {
        }
        catch(RenderException ex)
        {
        }
    }

    private void getMeasureValueFromVectColumn(DataSet dataset,RowInfo row,String strMeasure,int nm)
    {
        for(int ncol = 0;ncol < row.size();ncol++)
        {
            ReportColumn col = dataset.getHeaderColumns().get(ncol);
            if(false == (col != null && (strMeasure.equalsIgnoreCase(col.getGuid())
                                         || strMeasure.equalsIgnoreCase(col.getFieldName()))))
            {
                continue;
            }
            if(col.getVerticalKeys() == null || col.getVerticalKeys().size() < 1 || col.getVerticalKeys().isNormal() == false)
            {
                continue;
            }
            DataCell cell = row.get(ncol);
            if(null == cell || cell.getValue() == null)
            {
                continue;
            }
            double lfValue = ConvertEx.toDouble(cell.getValue(),0);
            //??Col??Vertical Key?????????
            String sd = null;
            for(int nv = 0;nv < col.getVerticalKeys().size();nv++)
            {
                ReportColumnValue rv = col.getVerticalKeys().get(nv);
                if(rv.getValue() == null || rv.getReportColumn() == null || rv.isNormalCell() == false)
                {
                    continue;
                }
                ReportColumn cd = rv.getReportColumn();
                if(this.getDimension().equalsIgnoreCase(cd.getGuid()) || this.getDimension().equalsIgnoreCase(cd.getFieldName()))
                {
                    if(cd.getDictId() != null)
                    {
                        sd = DictRepository.getInstance().nameOf(cd.getDictId(),rv.getValue());
                    }
                    else
                    {
                        sd = rv.getValue().toString();
                    }
                    break;
                }
            }
            if(sd != null)
            {
//                log.debug("Not nDimensionColumnIndex >= 0)");
                if(this.getZMeasure() != null && strMeasure.equalsIgnoreCase(this.getZMeasure()))
                {
                    addElement(m_datasMatrix.get(nm),sd,0,lfValue);
                }
                else
                {
                    addElement(m_datasMatrix.get(nm),sd,lfValue,0);
                }
            }
        }
    }

    private ReportColumn checkMeasure(DataSet dataset,String strMeasure)
        throws RenderException
    {
        ReportColumn measure = dataset.getHeaderColumns().getColumnByFieldOrGuid(strMeasure);
        if(null == measure)
        {
            throw new RenderException("????????????????" + strMeasure + "?????");
        }
        if(measure.isDimension())
        {
            throw new RenderException("????????????????" + measure.getTitle() + "/" + strMeasure + "???????????");
        }
        return measure;
    }

    private double getMeasureValueFromNormalRow(DataSet dataset,RowInfo row,DataCell cell,String strMeasure)
    {
        double lfVa = 0;
        for(int ncol = 0;ncol < row.size();ncol++)
        {
            ReportColumn col = dataset.getHeaderColumns().get(ncol);
            if(col != null && (strMeasure.equalsIgnoreCase(col.getGuid()) || strMeasure.equalsIgnoreCase(col.getFieldName())))
            {
                cell = row.get(ncol);
                if(null != cell && null != cell.getValue())
                {
                    lfVa += ConvertEx.toDouble(cell.getValue(),0);
                }
            }
        }
        return lfVa;
    }

    private void addElement(DataPointAry ary,String name,double lfValue,double zValue)
    {
//        log.debug("addddd  "+name+">>>>>>>>>>>>>>>"+lfValue);
        DataPoint ele = null;
        for(int n = 0;null != ary && n < ary.size();n++)
        {
            if(ary.get(n).getAxisLabel().equals(name))
            {
                ele = ary.get(n);
                break;
            }
        }
        if(ele == null)
        {
            ele = new DataPoint(name,0);
            ary.add(ele);
        }
        ele.setValue(ele.getValue() + lfValue);
        ele.setZValue(zValue); //??????ű???Z???????????
    }
}

