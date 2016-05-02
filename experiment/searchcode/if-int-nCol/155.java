package com.jeasonzhao.commons.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import com.jeasonzhao.commons.parser.lex.LexException;
import com.jeasonzhao.commons.parser.lex.LexToken;
import com.jeasonzhao.commons.parser.lex.LexTokenCollection;
import com.jeasonzhao.commons.parser.lex.SqlTokenizer;
import com.jeasonzhao.commons.utils.Algorithms;
import java.sql.ResultSet;

public class SqlExecutor
{
    private String originalSQL = null;
    private String executingSQL = null;
    private boolean selectStatement = false;
    private ArrayList<SqlParameter> parameters = null;
    private ArrayList<ParameterValueArray> parameterMatrix = null;
    private boolean privateSetFlag = false;
    public SqlExecutor(String strSqlStatement)
        throws SQLException
    {
        this.originalSQL = strSqlStatement;
        parse();
    }

    private void parse()
        throws SQLException
    {
        executingSQL = null;
        if(Algorithms.isEmpty(this.originalSQL))
        {
            throw new Sqlca.SqlcaExceptionNoSQL();
        }
        SqlTokenizer tokenizer = new SqlTokenizer(originalSQL);
        LexTokenCollection tokens = null;
        try
        {
            tokens = tokenizer.getTokens(false,false);
        }
        catch(LexException ex)
        {
            throw new SQLException("SQL parse Error: " + ex.getMessage());
        }
        if(tokens == null || tokens.size() < 1)
        {
            throw new Sqlca.SqlcaExceptionNoSQL();
        }
        LexToken tkFirst = tokens.get(0);
        if(tkFirst.isName() == false)
        {
            throw new SQLException("Invalidate SQL:" + originalSQL);
        }
        this.selectStatement = tkFirst.getToken().equalsIgnoreCase("select");
        this.parameters = new ArrayList<SqlParameter>();
        StringBuffer bufSQL = new StringBuffer();
        for(LexToken tk : tokens)
        {
            if(SqlTokenizer.isSQLParameterToken(tk))
            {
                if(tk.isSpecial() && tk.equals("?"))
                {
                    int nIdx = parameters.size();
                    SqlParameter param = new SqlParameter(nIdx);
                    param.setName("<Param_" + nIdx + ">");
                    parameters.add(param);
                    bufSQL.append(tk.getInitString());
                }
                else
                {
                    String strName = tk.getToken();
                    String rep = replaceParameterString(strName);
                    if(null != rep)
                    {
                        bufSQL.append(tk.replaceToken(rep));
                    }
                    else
                    {
                        int nParameterIndex = parameters.size();
                        SqlParameter param = new SqlParameter(nParameterIndex);
                        param.setName(strName);
                        parameters.add(param);
                        bufSQL.append(tk.replaceToken("?"));
                    }
                }
            }
            else
            {
                bufSQL.append(tk.getInitString());
            }
        }
        executingSQL = bufSQL.toString();
    }

    protected String replaceParameterString(String strParameterName)
    {
        return null;
    }

    public SqlExecutor clean()
    {
        selectStatement = false;
        parameters = null;
        parameterMatrix = null;
        return this;
    }

    public SqlExecutor setSql(String str)
        throws SQLException
    {
        clean();
        originalSQL = str;
        parse();
        return this;
    }

    public boolean isSelectStatement()
    {
        return selectStatement;
    }

    public String getExecutableSQL()
    {
        return executingSQL;
    }

    public SqlExecutor setParameterValue(SqlParameter p)
        throws SQLException
    {
        if(p == null || null == parameters || parameters.size() < 1)
        {
            return this;
        }
        int nID = p.getIndexId() - 1;
        if(nID >= 0)
        {
            if(nID >= parameters.size())
            {
                return this;
            }
            else
            {
                privateSetFlag = true;
                parameters.get(nID).copyValue(p);
            }
        }
        else if(p.getName() != null)
        {
            String strName = p.getName().trim();
            for(SqlParameter pv : parameters)
            {
                if(null != pv.getName() && pv.getName().equalsIgnoreCase(strName))
                {
                    privateSetFlag = true;
                    pv.copyValue(p);
//                    System.out.println(pv.getName()+"  =   "+pv.getValue());
                }
            }
        }
        return this;
    }

    public int getBatchSize()
    {
        return null == parameterMatrix ? 0 : parameterMatrix.size();
    }

    public SqlExecutor addBatch()
        throws SQLException
    {
        if(null == this.parameters)
        {
            return this;
        }
        //Check status
        ParameterValueArray ary = null;
        for(SqlParameter p : this.parameters)
        {
            if(privateSetFlag) //Check value
            {
                if(p.isSet() == false)
                {
                    throw new SQLException("The value of parameter " + p.getIndexId() + " \"" + p.getName() + "\" is not set");
                }
                if(null == ary)
                {
                    ary = new ParameterValueArray();
                }
                ParameterValue pv = new ParameterValue();
                pv.isNull = p.isDbNull();
                pv.value = p.getValue();
                ary.add(pv);
            }
            p.cleanValue();
        }
        if(null != ary)
        {
            if(null == parameterMatrix)
            {
                parameterMatrix = new ArrayList<ParameterValueArray>();
            }
            parameterMatrix.add(ary);
        }
        privateSetFlag = false;
        return this;
    }

    public ExecutionResult execute(Connection conn,ResultSetScrollType scrollType,ResultSetCursorType cursorType)
        throws SQLException
    {
        addBatch();
        int nscroll = ResultSet.TYPE_FORWARD_ONLY;
        switch(scrollType)
        {
            case TYPE_SCROLL_INSENSITIVE:
                nscroll = ResultSet.TYPE_SCROLL_INSENSITIVE;
                break;
            case TYPE_SCROLL_SENSITIVE:
                nscroll = ResultSet.TYPE_SCROLL_SENSITIVE;
                break;
            default:
                nscroll = ResultSet.TYPE_FORWARD_ONLY;
                break;
        }
        int ncursor = ResultSetCursorType.CONCUR_UPDATABLE == cursorType ? ResultSet.CONCUR_UPDATABLE : ResultSet.CONCUR_READ_ONLY;
        PreparedStatement p = conn.prepareStatement(this.executingSQL,nscroll,ncursor);
        int batchSize = null == parameterMatrix ? 0 : parameterMatrix.size();
        if(null != this.parameters && this.parameters.size() > 0)
        {
            if(this.parameterMatrix == null || parameterMatrix.size() < 1)
            {
                throw new SQLException("No parameter value set!");
            }
            for(int nRowIndex = 0;nRowIndex < parameterMatrix.size();nRowIndex++)
            {
                ParameterValueArray ary = parameterMatrix.get(nRowIndex);
                for(int nCol = 0;nCol < ary.size();nCol++)
                {
                    ParameterValue pv = ary.get(nCol);
                    int nT = parameters.get(nCol).getSqlType();
                    if(pv.isNull)
                    {
                        p.setNull(nCol + 1,nT);
                    }
                    else
                    {
                        p.setObject(nCol + 1,pv.value,nT);
                    }
                }
                if(this.selectStatement)
                {
                    batchSize = 1;
                    break;
                }
                else if(batchSize > 1)
                {
                    p.addBatch();
                }
            }
        }
        ExecutionResult ret = new ExecutionResult();
        ret.setEffectRows(0);
        ret.setPrepareStatement(p);
        ret.setResultSet(null);
        ret.setOriginalSql(this.originalSQL);
        ret.setExecutedSql(this.executingSQL);
        if(batchSize > 1)
        {
            int[] nar = p.executeBatch();
            int re = 0;
            for(int x : nar)
            {
                re += x;
            }
            ret.setEffectRows(re);
        }
        else
        {
            if(this.selectStatement)
            {
                ret.setResultSet(p.executeQuery());
            }
            else
            {
                ret.setEffectRows(p.executeUpdate());
            }
        }
        clean();
        return ret;
    }

    //Inner class which hold one parameter's value, and identify whether the value is null or not.
    private static class ParameterValue
    {
        public boolean isNull = false; //Identier that the value is null or not.
        public Object value = null; // The actual value which used by the SQL statement.
    }

    private static class ParameterValueArray extends ArrayList<ParameterValue>
    {
        private static final long serialVersionUID = 1L;
    }

}

