package com.jeasonzhao.report.dataset;

import com.jeasonzhao.commons.basic.StringPairCollection;
import com.jeasonzhao.commons.parser.expression.BasicEvalProvider;
import com.jeasonzhao.commons.parser.expression.EvalException;
import com.jeasonzhao.commons.parser.expression.EvalProviderHelper;
import com.jeasonzhao.commons.parser.expression.SyntaxException;
import com.jeasonzhao.commons.parser.expression.ValuePair;
import com.jeasonzhao.commons.parser.lex.LexException;
import com.jeasonzhao.commons.utils.ConvertEx;
import com.jeasonzhao.commons.utils.DataTypes;
import com.jeasonzhao.report.engine.Configuration;
import com.jeasonzhao.report.model.ReportColumn;
import com.jeasonzhao.report.model.ReportColumnValueCollection;

public class DataSetEvalProvider extends BasicEvalProvider
{
    private DataSet m_dataset = null;
    private int m_nRowIndex = -1;
    private int m_nColIndex = -1;
//    private static final Log log = LogFactory.getLog(DataSetEvalProvider.class);
    public void setPosition(int nrow,int ncol)
    {
        this.m_nRowIndex = nrow;
        this.m_nColIndex = ncol;
    }

    public DataSetEvalProvider(DataSet dataser)
    {
        super(true);
        this.addProvider(Configuration.getInstance().getBasicEvalProvider());
        m_nRowIndex = 0;
        m_dataset = dataser;
        EvalProviderHelper.addFunctionByMethod(this,this,"count",null); //????
        EvalProviderHelper.addFunctionByMethod(this,this,"pageNo",null); //??????0??
        EvalProviderHelper.addFunctionByMethod(this,this,"pageSize",null); //???????
        EvalProviderHelper.addFunctionByMethod(this,this,"pagesCount",null); //???
        EvalProviderHelper.addFunctionByMethod(this,this,"rowsCount",null); //????

        EvalProviderHelper.addFunctionByMethod(this,this,"sum",null); //Sum("FieldName");
        EvalProviderHelper.addFunctionByMethod(this,this,"avg",null); //avg("FieldName");

        EvalProviderHelper.addFunctionByMethod(this,this,"linesum",null); //linesum("EvalProviderHelper");

        EvalProviderHelper.addFunctionByMethod(this,this,"caseWhen",null);
//        this.add("linesum");
//        this.add("linecount");
//        this.add("avg");
    }

    @SuppressWarnings("unused")
    private Double avg(String strColumn)
        throws EvalException
    {
        Double lfValue = sum(strColumn);
        return new Double(lfValue.doubleValue() / m_dataset.getRowsCount());
    }

    private int count()
        throws EvalException
    {
        return m_dataset.getRowsCount();
    }

    @SuppressWarnings("unused")
    private int pageNo()
        throws EvalException
    {
        PageInfo page = m_dataset.getPageInfo();
        return null == page ? 0 : page.getPageNo();
    }

    @SuppressWarnings("unused")
    private int pageSize()
        throws EvalException
    {
        PageInfo page = m_dataset.getPageInfo();
        return null == page ? 0 : page.getPageSize();
    }

    @SuppressWarnings("unused")
    private int pagesCount()
        throws EvalException
    {
        PageInfo page = m_dataset.getPageInfo();
        return null == page ? 0 : page.getPageCount();
    }

    @SuppressWarnings("unused")
    private int rowsCount()
        throws EvalException
    {
        PageInfo page = m_dataset.getPageInfo();
        return null == page ? this.count() : page.getRowsCount();
    }

    private Double sum(String strFieldName)
        throws EvalException
    {
        double lfSum = 0;
        for(int nrow = 0;nrow < this.m_dataset.getRowsCount();nrow++)
        {
            if(this.m_dataset.getRow(nrow) == null || this.m_dataset.getRow(nrow).getRowType().isNormal() == false)
            {
                continue;
            }
            ValuePair cp = evalFieldValueByRow(m_dataset,nrow,m_nColIndex,strFieldName);
            if(null == cp || null == cp.getValue())
            {
                continue;
            }
            lfSum += ConvertEx.toDouble(cp.getValue());
        }
        return new Double(lfSum);
    }

    /**
     * ???????????????????????????????
     * @param strFieldName String
     * @return Double
     * @throws EvalException
     */
    @SuppressWarnings("unused")
    private Double linesum(String strFieldName)
        throws EvalException
    {
        ValuePair cp = evalFieldValueByRow(m_dataset,this.m_nRowIndex,m_nColIndex,strFieldName);
        if(null == cp || null == cp.getValue())
        {
            return null;
        }
        else
        {
            return ConvertEx.toDoubleObject(cp.getValue());
        }
        /*
         RowInfo row=m_dataset.getRow(m_nRowIndex);
                 if(null==row||null==strFieldName)
                 {
            return null;
                 }
                 Double d=null;
                 for(int ncol=0;ncol<row.size();ncol++)
                 {
            ReportColumn col=m_dataset.getHeaderColumn(ncol);
            if(col==null)
            {
                continue;
            }
            if(col.getVerticalKeys()!=null && col.getVerticalKeys().isNormal()==false)
            {
                continue;
            }
            if(strFieldName.equalsIgnoreCase(col.getFieldName()) || strFieldName.equalsIgnoreCase(col.getGuid()))
            {
                Cell cell = row.get(ncol);
                if(null == cell || cell.getValue() == null)
                {
                    continue;
                }
                        log.debug("==="+strFieldName+"==="+d+">>"+cell.getValue());
                if(null == d)
                {
                    d = new Double(Convert.toDouble(cell.getValue(),0));
                }
                else
                {
                    d = new Double(Convert.toDouble(cell.getValue(),0) + d.doubleValue());
                }
            }
                 }
                 log.debug("return ======"+d);
                 return d;
         */
    }

    /**
     * Get the value of the cell resided in current line and specified column named with var strFieldName,
     *
     * @param strFieldName String
     * @return ValuePair
     * @throws EvalException
     */
    public ValuePair evalVariable(String strFieldName)
        throws EvalException
    {
        try
        {
            return super.evalVariable(strFieldName);
        }
        catch(Exception ex)
        {
            //do not handle exception here
        }
        DataCell cell = m_dataset.getAt(m_nRowIndex,m_nColIndex);
        if(strFieldName.equalsIgnoreCase("#null#"))
        {
            return new ValuePair("");
        }
        else if(strFieldName.equalsIgnoreCase("#value"))
        {
            return null == cell ? null : new ValuePair(cell.getValue());
        }
        else if(strFieldName.equalsIgnoreCase("#text"))
        {
            return null == cell ? null : new ValuePair(cell.getText());
        }
        return evalFieldValueByRow(m_dataset,m_nRowIndex,m_nColIndex,strFieldName);
    }

    private static ValuePair evalFieldValueByRow(DataSet dataSet,int curRowIdx,int curColIdx,String strFieldName)
        throws EvalException,ArrayIndexOutOfBoundsException
    {
        RowInfo row = dataSet.getRow(curRowIdx);
        if(null == row)
        {
            throw new EvalException("????????????????");
        }
        ReportColumn col = dataSet.getHeaderColumn(curColIdx);
        if(col != null && col.getVerticalKeys() != null && col.getVerticalKeys().size() > 0)
        {
            ReportColumnValueCollection cks = col.getVerticalKeys();
            for(int ncol = 0;ncol < dataSet.getHeaderColumns().size();ncol++)
            {
                col = dataSet.getHeaderColumn(ncol);
                if(col.getVerticalKeys() != null && col.getVerticalKeys().equals(cks)
                   && null != col.getFieldName()
                   && col.getFieldName().equalsIgnoreCase(strFieldName))
                {
                    DataCell cell = row.get(ncol);
                    if(null == cell)
                    {
                        throw new EvalException("???????????" + strFieldName + " ??");
                    }
                    return new ValuePair(cell.getValue());
                }
            }
//            throw new EvalException("???????????" + strFieldName + " ??");
        }
        //??????????
        Double lfRet = null;
        DataTypes type = null;
        for(int ncol = 0;ncol < dataSet.getHeaderColumns().size();ncol++)
        {
            col = dataSet.getHeaderColumn(ncol);
            if(col == null || col.getFieldName() == null ||
               col.getFieldName().equalsIgnoreCase(strFieldName) == false)
            {
                continue;
            }
            DataCell cell = row.get(ncol);
            if(col.isMeasure() == false)
            {
                if(null == cell)
                {
                    throw new EvalException("???????????" + strFieldName + " ??");
                }
                return new ValuePair(cell.getValue());
            }
            else if(cell != null)
            {
                if(null == type)
                {
                    type = col.getDataType();
                }
                if(lfRet == null)
                {
                    lfRet = new Double(ConvertEx.toDouble(cell.getValue(),0));
                }
                else
                {
                    lfRet = new Double(ConvertEx.toDouble(cell.getValue(),0) + lfRet.doubleValue());
                }
            }
        }
//            log.debug("Col "+m_nColIndex+" "+strFieldName+"="+lfRet);
        return new ValuePair(lfRet,type);
    }

    @SuppressWarnings("unused")
    private Object caseWhen(String strFormat)
        throws EvalException,SyntaxException,LexException
    {
        if(null == strFormat || strFormat.trim().length() < 1)
        {
            return "";
        }
        StringPairCollection colls = StringPairCollection.from(strFormat,";","\\|");
        String strRet = "";
        for(int n = 0;null != colls && n < colls.size();n++)
        {
            String strExp = colls.get(n).getId();
            String strValue = colls.get(n).getName();
            if(strExp.trim().equalsIgnoreCase("else"))
            {
                strRet = strValue;
                continue;
            }
            Object obj = this.evalValue(strExp);
            if(ConvertEx.toBool(obj))
            {
                strRet = strValue;
                break;
            }
        }
        return this.evalValue(strRet);
    }

    /**
     * @todo ????????
     * @param strKeyField String
     * @param strValueField String
     * @return Double
     * @throws EvalException
     */
    java.util.Hashtable<String,Double> m_hashV = new java.util.Hashtable<String,Double>();
    @SuppressWarnings("unused")
    private Double sumByField(String strKeyField,String strValueField)
        throws EvalException
    {
        double lfSum = 0;
        ValuePair keyCurrent = evalFieldValueByRow(m_dataset,this.m_nRowIndex,m_nColIndex,strKeyField);
        Object objCurrent = keyCurrent == null ? null : keyCurrent.getRealData();
        String sssss = strKeyField + "@@@@" + strValueField + "@@@@" + objCurrent;
        if(m_hashV.containsKey(sssss))
        {
            return(Double) m_hashV.get(sssss);
        }
        for(int nrow = 0;nrow < this.m_dataset.getRowsCount();nrow++)
        {
            if(this.m_dataset.getRow(nrow) == null ||
               this.m_dataset.getRow(nrow).getRowType().isNormal() == false)
            {
                continue;
            }
            ValuePair key = evalFieldValueByRow(m_dataset,nrow,m_nColIndex,strKeyField);
            Object ok = key == null ? null : key.getRealData();
            if((null == ok && null == objCurrent) || (objCurrent != null && objCurrent.equals(ok)))
            {
                ValuePair value = evalFieldValueByRow(m_dataset,nrow,m_nColIndex,strValueField);
                lfSum += value == null ? 0 : ConvertEx.toDouble(value.getValue());
            }
        }
        m_hashV.put(sssss,new Double(lfSum));
        return new Double(lfSum);
    }

}

