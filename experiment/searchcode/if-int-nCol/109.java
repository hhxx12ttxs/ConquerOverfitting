package com.jeasonzhao.report.model;

import com.jeasonzhao.commons.basic.StringCollection;
import com.jeasonzhao.commons.basic.StringPairCollection;
import com.jeasonzhao.commons.json.JSONClass;
import com.jeasonzhao.commons.utils.Guid;
import com.jeasonzhao.commons.xml.XMLNode;
import com.jeasonzhao.model.Model;
import com.jeasonzhao.model.ModelException;
import com.jeasonzhao.model.ModelField;
import com.jeasonzhao.model.ModelHelper;
import com.jeasonzhao.report.codes.DataSetProviderTypes;
import com.jeasonzhao.report.codes.ReportCacheLevels;
import com.jeasonzhao.report.codes.ReportHeaderModes;
import com.jeasonzhao.report.codes.ReportRowNumModes;
import com.jeasonzhao.report.codes.ReportShowModes;
import com.jeasonzhao.report.codes.ReportSummaryModes;
import com.jeasonzhao.report.dataset.RowInfoCollection;
import com.jeasonzhao.report.model.chart.Chart;
import com.jeasonzhao.report.model.chart.ChartCollection;
import com.jeasonzhao.report.dataset.DataSetFilter;

@Model("report")
public class Report extends BaseReportModelItem
{
    private static final long serialVersionUID = 1L;
    private static final String NODENAME_ROTATE = "rotate";
    private static final String NODENAME_ALL_COLUMNS = "columns";
    private static final String NODENAME_COLUMN = "column";

    private static final String NODENAME_GROUP = "group";
    private static final String NODENAME_ALL_PARAMS = "parameters";
    private static final String NODENAME_PARAM = "param";

    @ModelField(names = "id,reportId")
    private String m_strReportId = null;

    @ModelField(names = "parentId,parent,ref,parentReportId,parentReport")
    private String m_strParentReportId = null;

    @ModelField(names = "name,title,reportName")
    private String m_strReportName = null;

    @ModelField(names = "owner,userId,userName,user")
    private String m_strOwnerId = null;

    @ModelField(names = "tag,tags,category")
    private String m_strCategory = null;

    @ModelField(names = "dataSetProviderType,dataproviderType")
    private DataSetProviderTypes m_DatasetProviderType = DataSetProviderTypes.SQL;

    @ModelField(names = "dataSetProvider,dataProvider")
    private String m_strDataSetProvider = null;

    @ModelField(names = "databaseId,database,dbid,className,class")
    private String m_strDatabaseId = null;

    @ModelField(names = "sql,queryString,query,statement",cdata = true)
    private String m_strQueryStatement = null;

    @ModelField(names = "pageSize,page")
    private int m_nPageSize = -1;

    @ModelField(names = "notes,comment,comments,desc,description")
    private String m_strComments = null;

    @ModelField(names = "summaryMode,summary,sum")
    private ReportSummaryModes m_summaryMode = null;

    @ModelField(names = "cacheLevel,cache,cacheType,cacheMode")
    private ReportCacheLevels m_nCacheLevel = null;

    @ModelField(names = "cacheParam,cacheParameter")
    private String m_cacheParam = null;

    @ModelField(names = "showMode")
    private ReportShowModes m_reportShowMode = null;

    @ModelField(names = "isShowQuerySection,querysection,showQuerySection")
    private boolean m_isShowQuerySection = false;

    @ModelField(names = "isCanBeCustomize,isCanBeCustomized,customize,custom,userdefine,redefine,clientdefine")
    private boolean m_isUserCanDefine = true;

    @ModelField(names = "rowNumMode,rowNum,rowMode")
    private ReportRowNumModes m_rowNumberMode = null;

    @ModelField(names = "isPerformanceStatistics,pdh")
    private boolean m_isPerformanceStatistics = false;

    @ModelField(names = "reportTag,tag,tags")
    private String m_strTags = null;

    @ModelField(names = "headerMode")
    private ReportHeaderModes m_reportHeaderMode = null;

    @ModelField(names = "usingFilter,filter")
    private boolean m_isUsingFilter = true;

    @ModelField(names = "showTitle,isShowTitle,showName,isShowTitle")
    private boolean m_isShowReportTitle = true;

    @ModelField(names = "shrinkedByDimension,compact")
    private boolean m_isShrinkByDimension = false;

    @ModelField(names = "appendInfo,append,extra")
    private XMLNode m_xmlAppend = null;

//    @ModelField(names = "tableHeader,myHeader")
//    private XMLNode m_xmlUserDefinedTableHeader = null;

    @ModelField(names = "pageFooter")
    private String m_strPageFooter = null;

    @ModelField(names = "pageHeader")
    private String m_pageHeaderText = null;

    @ModelField(names = "hideTable,hideDataSet,isHideDataSet")
    private boolean m_isHideResultTable = false;

    @ModelField(collection = true,names = NODENAME_ALL_COLUMNS + "." + NODENAME_COLUMN)
    private ReportColumnCollection m_columns = null;

    @ModelField(collection = true,names = NODENAME_ALL_PARAMS + "." + NODENAME_PARAM)
    private ReportParamCollection m_parameters = null;

    @ModelField(collection = true,names = "decorator")
    private StringCollection m_appendDecorators = null;

    @ModelField(collection = true,names = "chart")
    private ChartCollection m_charts = null;

    //Dynamic Parameters: The following parameters are used only in runtime,
    //users can not set values for those.
    private java.util.Vector<DataSetFilter> m_filters = null;
    private StringPairCollection m_sortConfigs = null;
    private RowInfoCollection m_userDefinedTableHeader = null;

    public Report()
    {
        super();
    }

    public static Report createSqlReport(String strDatabaseId,String strSql)
    {
        Report rpt = new Report();
        rpt.setDatabaseId(strDatabaseId);
        rpt.setSqlStatement(strSql);
        return rpt;
    }

    public boolean filteredBy(ReportColumn col)
    {
        if(null == m_filters || null == col)
        {
            return false;
        }
        for(int n = 0;n < m_filters.size();n++)
        {
            if(null != m_filters.elementAt(n)
               && null != m_filters.elementAt(n).getColGuidId()
               && m_filters.elementAt(n).getColGuidId().equalsIgnoreCase(col.getGuid()))
            {
                return true;
            }
        }
        return false;
    }

    public ChartCollection getChartModels()
    {
        return m_charts == null || m_charts.size() < 1 ? null : m_charts;
    }

    public void setChartModels(ChartCollection v)
    {
        m_charts = v;
    }

    public java.util.Vector<DataSetFilter> getDataSetFilters()
    {
        return m_filters == null || m_filters.size() < 1 ? null : m_filters;
    }

    public void setDataSetFilters(java.util.Vector<DataSetFilter> v)
    {
        m_filters = v;
    }

    public StringPairCollection getSortSettings()
    {
        return m_sortConfigs;
    }

    public void setSortSettings(StringPairCollection v)
    {
        m_sortConfigs = v;
    }

    public StringCollection getDecorators()
    {
        return m_appendDecorators == null || m_appendDecorators.size() < 1 ? null : m_appendDecorators;
    }

    public void setAppendDecorators(String ...decorators)
    {
        m_appendDecorators = null;
        for(int n = 0;null != decorators && n < decorators.length;n++)
        {
            if(null != decorators[n] && decorators[n].trim().length() > 0)
            {
                if(m_appendDecorators == null)
                {
                    m_appendDecorators = new StringCollection();
                }
                m_appendDecorators.add(decorators[n]);
            }
        }
    }

    public void setAppendDecorators(Class<?>...classes)
    {
        m_appendDecorators = null;
        for(int n = 0;null != classes && n < classes.length;n++)
        {
            if(null != classes)
            {
                if(m_appendDecorators == null)
                {
                    m_appendDecorators = new StringCollection();
                }
                m_appendDecorators.add(classes[n].getName());
            }
        }

    }

    public void setAppendDecorators(StringCollection strs)
    {
        m_appendDecorators = strs == null || strs.size() < 1 ? null : strs;
    }

    public RowInfoCollection getUserDefinedTableHeader()
    {
        return m_userDefinedTableHeader == null || m_userDefinedTableHeader.size() < 1 ? null : m_userDefinedTableHeader;
    }

    public void setUserDefinedHeader(RowInfoCollection coll)
    {
        m_userDefinedTableHeader = coll == null || coll.size() < 1 ? null : coll;
    }

    public boolean isUsingFilter()
    {
        return m_isUsingFilter;
    }

    public void setUsingFilter(boolean b)
    {
        m_isUsingFilter = b;
    }

    public String getDataSetProvider()
    {
        return m_strDataSetProvider == null || m_strDataSetProvider.trim().length() < 1 ? null : m_strDataSetProvider;
    }

    public void setDataSetProvider(String str)
    {
        m_strDataSetProvider = str;
    }

    public DataSetProviderTypes getDataSetProviderType()
    {
        return m_DatasetProviderType == null ? DataSetProviderTypes.SQL : m_DatasetProviderType;
    }

    public void setDataSetProviderType(DataSetProviderTypes v)
    {
        m_DatasetProviderType = v;
    }

    public void hideResultTable(boolean b)
    {
        m_isHideResultTable = b;
    }

    public boolean isHideResultTable()
    {
        return m_isHideResultTable;
    }

    public Report(Report report)
    {
        super(report);
        if(null == report)
        {
            return;
        }
        this.m_strReportId = report.m_strReportId;
        this.m_strOwnerId = report.m_strOwnerId;
        this.m_strParentReportId = report.m_strParentReportId;
        this.m_strReportName = report.m_strReportName;
        this.m_strDatabaseId = report.m_strDatabaseId;
        this.m_strQueryStatement = report.m_strQueryStatement;
        this.m_nPageSize = report.m_nPageSize;
        this.m_strComments = report.m_strComments;
        this.m_summaryMode = report.m_summaryMode;
        this.m_nCacheLevel = report.m_nCacheLevel;
        this.m_cacheParam = report.m_cacheParam;
        this.m_reportShowMode = report.m_reportShowMode;
        this.m_isShowQuerySection = report.m_isShowQuerySection;
        this.m_isUserCanDefine = report.m_isUserCanDefine;
        this.m_rowNumberMode = report.m_rowNumberMode;
        this.m_isPerformanceStatistics = report.m_isPerformanceStatistics;
        this.m_strTags = report.m_strTags;
        this.m_reportHeaderMode = report.m_reportHeaderMode;
        this.m_isShowReportTitle = report.m_isShowReportTitle;
        this.m_columns = ReportColumnCollection.from(report.getColumns());
        this.m_parameters = ReportParamCollection.from(report.getParameters());
        this.m_isShrinkByDimension = report.m_isShrinkByDimension;
        this.m_DatasetProviderType = report.m_DatasetProviderType;
        this.m_strDataSetProvider = report.m_strDataSetProvider;
        this.m_strPageFooter = report.m_strPageFooter;
        this.m_pageHeaderText = report.m_pageHeaderText;
        this.m_isUsingFilter = report.m_isUsingFilter;
        this.m_userDefinedTableHeader = report.m_userDefinedTableHeader;
        this.m_xmlAppend = report.m_xmlAppend;
        this.m_appendDecorators = report.m_appendDecorators;
        this.m_isHideResultTable = report.m_isHideResultTable;
        if(null != report.m_charts)
        {
            this.m_charts = new ChartCollection();
            for(int n = 0;n < report.m_charts.size();n++)
            {
                this.m_charts.add(new Chart(report.m_charts.get(n)));
            }
        }
    }

    public Report addColumn(ReportColumn ...colls)
    {
        for(int n = 0;null != colls && n < colls.length;n++)
        {
            if(null != colls[n])
            {
                if(null == m_columns)
                {
                    m_columns = new ReportColumnCollection();
                }
                m_columns.add(colls[n]);
            }
        }
        return this;
    }

    public Report addColumnsByMagicName(String ...fieldNames)
    {
        for(int n = 0;null != fieldNames && n < fieldNames.length;n++)
        {
            ReportColumn col = ReportColumn.createByMagicString(fieldNames[n]);
            if(null != col)
            {
                if(null == m_columns)
                {
                    m_columns = new ReportColumnCollection();
                }
                m_columns.add(col);
            }
        }
        return this;
    }

    public Report setColumnsByMagicName(String ...filedNames)
    {
        m_columns = null;
        return addColumnsByMagicName(filedNames);
    }

    public Report setColumns(ReportColumn ...colls)
    {
        m_columns = null;
        return addColumn(colls);
    }

    public Report setColumns(ReportColumnCollection colls)
    {
        m_columns = colls;
        return this;
    }

    public int getColumnsCount()
    {
        return m_columns == null ? 0 : m_columns.size();
    }

    public ReportColumnCollection getColumns()
    {
        return m_columns;
    }

    public void setParameters(ReportParamCollection colls)
    {
        m_parameters = colls;
    }

    public ReportParamCollection getParameters()
    {
        return m_parameters;
    }

    public int noneFixedParameterSize()
    {
        int nSize = 0;
        for(int n = 0;null != m_parameters && n < m_parameters.size();n++)
        {
            if(m_parameters.get(n).isFixedValue() == false)
            {
                nSize++;
            }
        }
        return nSize;
    }

    public ReportParam findParameterByName(String strParameterName)
    {
        return findParameterByName(strParameterName,false);
    }

    public ReportParam findParameterByName(String strParameterName,boolean isCaseSenstive)
    {
        if(null == strParameterName || null == m_parameters)
        {
            return null;
        }
        else
        {
            return m_parameters.findParameterByName(strParameterName,isCaseSenstive);
        }
    }

    public ReportCacheLevels getCacheLevel()
    {
        return m_nCacheLevel;
    }

    public String getCacheParam()
    {
        return m_cacheParam;
    }

    public String getDatabaseId()
    {
        return m_strDatabaseId == null || m_strDatabaseId.trim().length() < 1 ? null : m_strDatabaseId;
    }

    public boolean isPerformanceStatistics()
    {
        return m_isPerformanceStatistics;
    }

    public ReportHeaderModes getHeaderMode()
    {
        return m_reportHeaderMode == null ? ReportHeaderModes.Default : m_reportHeaderMode;
    }

    public boolean isCanBeCustomize()
    {
        return m_isUserCanDefine && this.getColumns() != null && this.getColumns().size() > 0;
    }

    public boolean isShowQuerySection()
    {
        return m_isShowQuerySection;
    }

    public ReportSummaryModes getSummaryMode()
    {
        return this.m_summaryMode == null ? ReportSummaryModes.None : this.m_summaryMode;
    }

    public String getNotes()
    {
        return m_strComments == null || m_strComments.trim().length() < 1 ? null : m_strComments;
    }

    public int getPageSize()
    {
        return m_nPageSize;
    }

    public String getParentReportId()
    {
        return m_strParentReportId;
    }

    public String getReportId()
    {
        return m_strReportId == null || m_strReportId.trim().length() < 1 ? null : m_strReportId;
    }

    public String getReportTagId()
    {
        return m_strTags;
    }

    public ReportShowModes getShowMode()
    {
        return m_reportShowMode == null ? ReportShowModes.FreeMode : m_reportShowMode;
    }

    public String getSqlStatement()
    {
        return m_strQueryStatement == null || m_strQueryStatement.trim().length() < 1 ? null : m_strQueryStatement;
    }

    public String getTitle()
    {
        return m_strReportName == null || m_strReportName.trim().length() < 1 ? null : m_strReportName;
    }

    public String getOwner()
    {
        return m_strOwnerId == null || m_strOwnerId.trim().length() < 1 ? null : m_strOwnerId;
    }

    public boolean isShowTitle()
    {
        return m_isShowReportTitle;
    }

    public ReportRowNumModes getRowNumMode()
    {
        return m_rowNumberMode == null ? ReportRowNumModes.DoNotShown : m_rowNumberMode;
    }

    public void setCacheParam(String cacheParam)
    {
        this.m_cacheParam = cacheParam;
    }

    public void setCacheLevel(ReportCacheLevels lvl)
    {
        this.m_nCacheLevel = lvl;
    }

    public void setDatabaseId(String databaseId)
    {
        this.m_strDatabaseId = databaseId;
    }

    public void setPerformanceStatistics(boolean isPerformanceStatistics)
    {
        this.m_isPerformanceStatistics = isPerformanceStatistics;
    }

    public void setHeaderMode(ReportHeaderModes mode)
    {
        this.m_reportHeaderMode = mode;
    }

    public void setCanBeCustomized(boolean isCanBeCustomize)
    {
        this.m_isUserCanDefine = isCanBeCustomize;
    }

    public void setShowQuerySection(boolean b)
    {
        this.m_isShowQuerySection = b;
    }

    public void setSummaryMode(ReportSummaryModes s)
    {
        this.m_summaryMode = s;
    }

    public void setNotes(String notes)
    {
        this.m_strComments = notes;
    }

    public void setPageSize(int pageSize)
    {
        this.m_nPageSize = pageSize;
    }

    public void setParentReportId(String relReportId)
    {
        this.m_strParentReportId = relReportId;
    }

    public void setReportId(String reportId)
    {
        this.m_strReportId = reportId;
    }

    public int[] getRotateGroupNos()
    {
        return null == m_columns ? null : m_columns.getRotateGroupNos();
    }

    public void setReportTagId(String reportTagId)
    {
        this.m_strTags = reportTagId;
    }

    public void setShowMode(ReportShowModes showMode)
    {
        this.m_reportShowMode = showMode;
    }

    public void setSqlStatement(String sqlStatement)
    {
        this.m_strQueryStatement = sqlStatement;
    }

    public void setTitle(String title)
    {
        this.m_strReportName = title;
    }

    public void setUserId(String userId)
    {
        this.m_strOwnerId = userId;
    }

    public void setShowTitle(boolean showTitle)
    {
        this.m_isShowReportTitle = showTitle;
    }

    public void setRowNumMode(ReportRowNumModes RowNumMode)
    {
        this.m_rowNumberMode = RowNumMode;
    }

    public String toString()
    {
        return(this.getTitle() == null ? "" : this.getTitle()) +
            (this.getReportId() == null ? "" : "[" + this.getReportId() + "]") +
            ("{" + this.getConfigType().getName() + "}");
    }

    @SuppressWarnings("unused")
    private void setPageFooter(String string)
    {
        m_strPageFooter = string;
    }

    @SuppressWarnings("unused")
    private void setPageHeader(String string)
    {
        m_pageHeaderText = string;
    }

    public String getHtmlFooter()
    {
        return m_strPageFooter == null || m_strPageFooter.trim().length() < 1 ? null : m_strPageFooter;
    }

    public String getHtmlHeader()
    {
        return m_pageHeaderText == null || m_pageHeaderText.trim().length() < 1 ? null : m_pageHeaderText;
    }

    public void setShrinkByDimension(boolean shrinkByDimension)
    {
        this.m_isShrinkByDimension = shrinkByDimension;
    }

    public void setAppendInfoXML(XMLNode appendInfo)
    {
        this.m_xmlAppend = appendInfo;
    }

    public boolean isShrinkByDimension()
    {
        return m_isShrinkByDimension;
    }

    public XMLNode getAppendXML()
    {
        return m_xmlAppend.isEmpty() ? null : m_xmlAppend;
    }

    public int getParametersCount()
    {
        return this.m_parameters == null ? 0 : this.m_parameters.size();
    }

    public ReportParamCollection getNonFixedParameters()
    {
        if(null == m_parameters)
        {
            return null;
        }
        ReportParamCollection ret = new ReportParamCollection();
        for(ReportParam p : this.m_parameters)
        {
            if(p.isFixedValue())
            {
                continue;
            }
            else
            {
                ret.add(p);
            }
        }
        return ret.size() < 1 ? null : ret;
    }

    public void afterInitializedFromXML(XMLNode nodeItem)
        throws ModelException
    {
        if(null == this.m_columns)
        {
            m_columns = new ReportColumnCollection();
        }
        int nRotateIndex = 0;
        //get all nodes under "columns"
        for(XMLNode nodeLevel0 : nodeItem.selectNodes(NODENAME_ALL_COLUMNS).selectNodes())
        {
            if(nodeLevel0.matchName(NODENAME_ROTATE))
            {
                nRotateIndex++;
                for(XMLNode nodeLevel1 : nodeLevel0.selectNodes(NODENAME_COLUMN))
                {
                    ReportColumn col = ModelHelper.getInstance().fromXML(ReportColumn.class,nodeLevel1);
                    col.setRotateGroupNo(nRotateIndex);
                    this.m_columns.add(col);
                }
            }
            else
            {
                if(nodeLevel0.matchName(NODENAME_COLUMN))
                {
                    ReportColumn col = ModelHelper.getInstance().fromXML(ReportColumn.class,nodeLevel0);
                    this.m_columns.add(col);
                }
            }
        }
        if(null == this.m_parameters)
        {
            m_parameters = new ReportParamCollection();
        }
        //get all nodes under "parameters"
        for(XMLNode nodeLevel0 : nodeItem.selectNodes(NODENAME_ALL_PARAMS).selectNodes())
        {
            /**
             * @todo Add group parameter
             */
            if(nodeLevel0.matchName(NODENAME_GROUP))
            {
                nRotateIndex++;
                for(XMLNode nodeLevel1 : nodeLevel0.selectNodes(NODENAME_PARAM))
                {
                    ReportParam param = ModelHelper.getInstance().fromXML(ReportParam.class,nodeLevel1);
                    this.m_parameters.add(param);
                }
            }
            else
            {
                if(nodeLevel0.matchName(NODENAME_PARAM))
                {
                    ReportParam param = ModelHelper.getInstance().fromXML(ReportParam.class,nodeLevel0);
                    this.m_parameters.add(param);
                }
            }
        }

    }

    public void afterInitializedFromJSON(JSONClass jsonclass)
        throws ModelException
    {
    }

    public void normalizeModelItem()
        throws ModelException
    {

        if(null == getReportId() || getReportId().trim().length() < 1)
        {
            this.setReportId(Guid.newGuid());
        }
        this.setGuid(this.getReportId());
        for(int ncol = 0;null != m_columns && ncol < this.m_columns.size();ncol++)
        {
            ReportColumn col = m_columns.get(ncol);
            if(null == col)
            {
                continue;
            }
            col.setReportId(this.getReportId());
            col.setColId(ncol);
            if(col.getGuid() == null || col.getGuid().trim().length() < 1)
            {
                col.setGuid("rptcolumn_" + this.getReportId() + "_c" + ncol);
            }
            col.normalizeModelItem();
        }
        int[] rgrps = getRotateGroupNos();
        for(int n = 0;null != rgrps && n < rgrps.length;n++)
        {
            int nno = rgrps[n];
            int nm = 0;
            int nd = 0;
            for(int ncol = 0;null != m_columns && ncol < this.m_columns.size();ncol++)
            {
                ReportColumn col = m_columns.get(ncol);
                if(null == col || col.getRotateGroupNo() != nno)
                {
                    continue;
                }
                nm += col.isMeasure() ? 1 : 0;
                nd += col.isMeasure() ? 0 : 1;
            }
            if(nd < 1 || nm < 1)
            {
                for(int ncol = 0;null != m_columns && ncol < this.m_columns.size();ncol++)
                {
                    ReportColumn col = m_columns.get(ncol);
                    if(null == col || col.getRotateGroupNo() != nno)
                    {
                        continue;
                    }
                    col.setRotateGroupNo( -1);
                    col.setRotateMode(null);
                }
            }
        }
        for(int n = 0;null != this.m_parameters && n < this.m_parameters.size();n++)
        {
            ReportParam param = m_parameters.get(n);
            if(null != param)
            {
                param.setSortNum(n);
            }
        }
        /**
         *@todo Genereate Header XML->HTML
         */
    }

    public boolean isPrivateReport()
    {
        return null != this.m_strParentReportId && this.m_strParentReportId.trim().length() > 0;
    }

    public String getTags()
    {
        return m_strCategory;
    }

    public void setTags(String str)
    {
        m_strCategory = str;
    }
}

