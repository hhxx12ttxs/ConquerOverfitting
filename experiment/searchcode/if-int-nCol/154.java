package com.jeasonzhao.report.engine;

import com.jeasonzhao.commons.logger.BasicLogger;
import com.jeasonzhao.commons.logger.Logger;
import com.jeasonzhao.report.codes.DataSetProviderTypes;
import com.jeasonzhao.report.dataset.DataCell;
import com.jeasonzhao.report.dataset.DataSet;
import com.jeasonzhao.report.dataset.RowInfo;
import com.jeasonzhao.report.decorator.AlertsDecorator;
import com.jeasonzhao.report.decorator.AppendInfoDecorator;
import com.jeasonzhao.report.decorator.CalculateDecorator;
import com.jeasonzhao.report.decorator.CellFormatDecorator;
import com.jeasonzhao.report.decorator.ChartModelDataSetDecorator;
import com.jeasonzhao.report.decorator.CustomerHeaderDecorator;
import com.jeasonzhao.report.decorator.DataSetFilterDecorator;
import com.jeasonzhao.report.decorator.DictDecorator;
import com.jeasonzhao.report.decorator.HorizonExpandColumnDecorator;
import com.jeasonzhao.report.decorator.RotateDecorator;
import com.jeasonzhao.report.decorator.ShrinkDimensionDecorator;
import com.jeasonzhao.report.decorator.SortDecorator;
import com.jeasonzhao.report.decorator.SubTotalDecorator;
import com.jeasonzhao.report.decorator.SummaryDecorator;
import com.jeasonzhao.report.engine.impl.BasicHtmlRender;
import com.jeasonzhao.report.engine.impl.SqlDataSetProvider;
import com.jeasonzhao.report.engine.impl.VisiFireRender;
import com.jeasonzhao.report.engine.irp.IDataSetProvider;
import com.jeasonzhao.report.engine.irp.IParameterValueCollector;
import com.jeasonzhao.report.engine.irp.IReportRender;
import com.jeasonzhao.report.engine.repo.ReportRepository;
import com.jeasonzhao.report.exceptions.ReportException;
import com.jeasonzhao.report.model.Report;
import com.jeasonzhao.report.model.ReportAgentSummary;
import com.jeasonzhao.report.model.ReportColumn;
import com.jeasonzhao.report.model.ReportParam;
import com.jeasonzhao.report.model.ReportParamCollection;
import com.jeasonzhao.report.msg.MessageCodes;
import com.jeasonzhao.report.msg.ReportRuntimeLogger;
import com.jeasonzhao.report.exceptions.ReportParameterException;
import com.jeasonzhao.report.exceptions.SQLReportException;
import com.jeasonzhao.report.engine.servlet.ServletParameterCollector;
import com.jeasonzhao.web.IHtmlWriter;

public class ReportAgent
{
    private Report m_report = null;
    private IReportRender m_reportRender = null;
//    private ReportUserInfo m_userInfo = null;
    private Logger log = BasicLogger.DUMMY;
//    private IParameterValueCollector m_parameterCollector = null;

    private ReportAgent()
    {
        Configuration.getInstance();
    }

    /**
     * Constructor
     * @param userinfo UserInfo Current user information
     * @param report Report The report which needs to be generated data.
     * @param collector IParameterCollector The agent which collects all parameters for current report.
     * @param render IReportRender The agent which render the result of report
     * @param logAgent LogAgent The agent which record all log information.
     */
    public ReportAgent(ReportUserInfo userinfo
                       ,Report report
                       ,IParameterValueCollector collector
                       ,IReportRender render
                       ,Logger logAgent)
    {
        this();
        m_report = report;
        m_reportRender = render;
//        m_userInfo = userinfo;
//        m_parameterCollector = collector;
        log = logAgent;
        if(null == log)
        {
            log = Configuration.getInstance().getLogger(); //Make sure the logger should not be null,so the source code is easier to read.
        }
    }

    /**
     * Processing the report
     * Steps:
     * 1. Collect all parameters
     * 2. Retrieve datas from datasource
     * 3. Decorat datas
     * 4. Render datas
     * @param nPageSize int The amount of rows in each page, if the value is less than 0, it means do not pagnate the result.
     * @param nPageNo int The page number
     */
    public void processReport(int nPageSize,int nPageNo)
        throws SQLReportException
    {
        if(null == m_report)
        {
            //  throw new NullPointerException(m_report);
        }
//            int nPageSizeBak=nPageSize;
//            int nPageNoBak=nPageNo;
        int nFilteredRowsCount = 0;
        if(m_report.getDataSetFilters() != null && m_report.getDataSetFilters().size() > 0)
        {
            log.info(MessageCodes.get(MessageCodes.RPT_NOPAGENATE_FITER));
            nPageSize = -1;
            nPageNo = 0;
        }
        loadAttrFromParentReport(); //????????????????????????
        checkValidate();
        ReportAgentSummary pdh = new ReportAgentSummary(m_report);
        IDataSetProvider provider = getDataSetProvider();
        provider.setup(log,m_report);
        if(false == isAllParameterReady(m_report))
        {
            if(null != m_reportRender)
            {
                m_reportRender.renderParamterSelector(log,m_report,null);
            }
            return;
        }
        //prepare finished
//            pdh.setPrepare(timerPt.getMilliSecondes());
        DataSet dataset = provider.getDataSet(log,nPageSize,nPageNo);
        provider.teardown(log);
        dataset = generateDefaultDataSetPros(dataset);
//            pdh.setLoadFromSource(timerPt.getMilliSecondes());
        //???????????
        dataset = DictDecorator.getInstance().decorate(m_report,dataset);
        HorizonExpandColumnDecorator.horizonExpandDataSet(m_report,dataset);
        log.info(MessageCodes.get(MessageCodes.RPT_CALC_BEFOREROTATE));
        CalculateDecorator.calcDimensions(log,m_report,dataset);
        nFilteredRowsCount += DataSetFilterDecorator.filterDimensions(m_report,dataset);
        log.info(MessageCodes.get(MessageCodes.RPT_CALC_ROTATE));
        SortDecorator.sortDataSet(m_report,dataset,true);
        dataset = (new RotateDecorator(this.log)).decorate(m_report,dataset);
//            pdh.setRotate(timerPt.getMilliSecondes());
        log.info(MessageCodes.get(MessageCodes.RPT_CALC_SHRINKBYDIMENSIONS));
        dataset = ShrinkDimensionDecorator.decoratorTable(m_report,dataset);
        log.info(MessageCodes.get(MessageCodes.RPT_CALC_AFTERROTATE));
        CalculateDecorator.calcMeasures(log,m_report,dataset);
//            pdh.setCalcColumns(timerPt.getMilliSecondes());
        nFilteredRowsCount += DataSetFilterDecorator.filterMeasures(m_report,dataset);
        SortDecorator.sortDataSet(m_report,dataset,false);
        if(null != dataset)
        {
            log.info(MessageCodes.get(MessageCodes.RPT_CALC_SUMMARY));
            dataset = SubTotalDecorator.decoratorTable(m_report,dataset);
            dataset = SummaryDecorator.decoratorTable(m_report,dataset);
//                pdh.setSubTotal(timerPt.getMilliSecondes());
            log.info(MessageCodes.get(MessageCodes.RPT_CALC_SUMMARY2));
            CalculateDecorator.calcSubtotalRows(log,m_report,dataset);
        }
//            if(null != dataset)
//            {
//                m_logAgent.info(m_report + "???????????...");
//                dataset = VerticalExpandDecorator.getInstance().decoratorTable(m_report,dataset);
//            }
        if(null != dataset)
        {
            log.info(MessageCodes.get(MessageCodes.RPT_CALC_FORMAT));
            dataset = CellFormatDecorator.getInstance().decorate(m_report,dataset);
        }
        if(null != dataset)
        {
            log.info(MessageCodes.get(MessageCodes.RPT_CALC_ALERTS));
//                if(null != m_reportRender && (this.m_reportRender instanceof IHtmlReportRender))
//                {
//                    AlertsDecorator.getInstanceHtml().decorate(m_report,dataset);
//                }
//                else
            {
                AlertsDecorator.getInstanceNonHtml().decorate(m_report,dataset);
            }
        }
        {
            log.info(MessageCodes.get(MessageCodes.RPT_CALC_APPENDDECORATORS));
            dataset = AppendInfoDecorator.getInstance().decorate(m_report,dataset);
            log.info(MessageCodes.get(MessageCodes.RPT_CALC_CUSTOMIZED_HEADER));
            dataset = CustomerHeaderDecorator.getInstance().decorate(m_report,dataset);
//                pdh.setAppendOperation(timerPt.getMilliSecondes());
        }
        log.info(MessageCodes.get(MessageCodes.RPT_CALC_CHARTS));
        ChartModelDataSetDecorator.calcChartsData(m_report,dataset);
        if(null != m_reportRender && null != dataset)
        {
            //???????????????????????
            if(null != dataset.getPageInfo() && nFilteredRowsCount > 0)
            {
                dataset.getPageInfo().setFilteredRowsCount(nFilteredRowsCount);
            }
            dataset.setReportPDH(pdh);
            log.info(MessageCodes.get(MessageCodes.RPT_RENDER));
            m_reportRender.renderDataset(log,m_report,dataset);
        }
        dataset = null;
        log.info(MessageCodes.get(MessageCodes.RPT_CALC_DONE));
    }

    private DataSet generateDefaultDataSetPros(DataSet dataset)
    {
        if(null == dataset)
        {
            dataset = new DataSet();
        }
        if(dataset.getHeaderColumns() == null || dataset.getHeaderColumns().size() < 1)
        {
            dataset.setReportColumns(m_report.getColumns());
        }
        if(dataset.getHeader() == null || dataset.getHeader().size() < 1)
        {
            dataset.generateDefaultHeader();
        }
        boolean isSQL = m_report.getDataSetProviderType().equals(DataSetProviderTypes.SQL);
        //Set data types
        for(int nrow = 0;nrow < dataset.getRowsCount();nrow++)
        {
            RowInfo row = dataset.getRow(nrow);
            if(null == row)
            {
                continue;
            }
            for(int ncol = 0;ncol < row.size();ncol++)
            {
                ReportColumn col = dataset.getHeaderColumn(ncol);
                if(col == null)
                {
                    continue;
                }
                DataCell cell = row.get(ncol);
                if(null == cell)
                {
                    cell = new DataCell();
                    row.setElementAt(cell,ncol);
                    cell.setVisiable(col.isHidden() == false);
                }
                cell.setColIndex(ncol);
                cell.setRowIndex(nrow);
                cell.setDataType(col.getDataType());
                if(isSQL && cell.isVisiable() && col.isHidden()) //No hidden
                {
                    cell.setVisiable(false);
                }
            }
        }
        return dataset;
    }

    private IDataSetProvider getDataSetProvider()
        throws ReportException
    {
        IDataSetProvider provider = null;
        if(m_report.getDataSetProviderType().equals(DataSetProviderTypes.SQL))
        {
            provider = new SqlDataSetProvider();
        }
        else if(m_report.getDataSetProviderType().equals(DataSetProviderTypes.CLASS))
        {
            String strClassName = m_report.getDataSetProvider();
            if(null == strClassName)
            {
                throw new ReportException.NoDataSetProviderClass(m_report);
            }
            try
            {
                Class<?> cls = Class.forName(strClassName);
                if(IDataSetProvider.class.isAssignableFrom(cls) == false)
                {
                    throw new ReportException.CastError(m_report,strClassName,IDataSetProvider.class);
                }
                provider = (IDataSetProvider) cls.newInstance();
            }
            catch(ClassNotFoundException ex)
            {
                throw new ReportException.CastError(m_report,strClassName,IDataSetProvider.class,ex);
            }
            catch(ReportException ex)
            {
                throw ex;
            }
            catch(IllegalAccessException ex)
            {
                throw new ReportException.CastError(m_report,strClassName,IDataSetProvider.class,ex);
            }
            catch(InstantiationException ex)
            {
                throw new ReportException.CastError(m_report,strClassName,IDataSetProvider.class,ex);
            }
        }
        else
        {
            throw new ReportException.UnknownDatasetProvider(m_report);
        }
        return provider;
    }

    private void checkValidate()
        throws ReportException
    {
        java.util.HashSet<String> set = new java.util.HashSet<String>();
        for(int n = 0;null != m_report && null != m_report.getColumns() && n < m_report.getColumns().size();n++)
        {
            ReportColumn col = m_report.getColumns().get(n);
            if(null == col)
            {
                continue;
            }
            if(null == col.getGuid())
            {
                throw new ReportException.ColumnNoGuid(m_report,col);
            }
            String strKey = col.getGuid().trim().toLowerCase();
            if(set.contains(strKey))
            {
                throw new ReportException.ColumnDuplicatedGuid(m_report,col);
            }
            set.add(strKey);
        }
    }

    private void loadAttrFromParentReport()
        throws ReportException
    {
        if(null != m_report && m_report.getParentReportId() != null
           && m_report.getParentReportId().trim().length() > 0)
        {
            Report rptParent = ReportRepository.getInstance().get(m_report.getParentReportId());
            if(null == rptParent)
            {
                throw new ReportException.CouldNotFoundParent(m_report);
            }
            m_report.setSqlStatement(rptParent.getSqlStatement());
            m_report.setDatabaseId(rptParent.getDatabaseId());
            if(null == m_report.getParameters())
            {
                m_report.setParameters(rptParent.getParameters());
            }
        }
    }

    private boolean isAllParameterReady(Report report)
        throws SQLReportException
    {
        boolean bReady = true;
        ReportParamCollection params = report.getParameters();
        String strParametersName = null;
        for(int n = 0;null != params && n < params.size();n++)
        {
            ReportParam param = params.elementAt(n);
            if(false == param.isOptional() && false == param.hasBeenSetValues())
            {
                bReady = false;
                if(null == strParametersName)
                {
                    strParametersName = "";
                }
                else
                {
                    strParametersName += ",";
                }
                strParametersName += param.getTitle() + ":" + param.getParam();
            }
        }
        if(strParametersName != null && strParametersName.length() > 0)
        {
            throw new ReportParameterException.ParameterNotSet(report,strParametersName);
        }
        return bReady;
    }

    public static void main(String[] argvs)
        throws Exception
    {
        Report report = ReportRepository.getInstance().get("demo");
        ReportUserInfo user = null;
        IParameterValueCollector pc = new ServletParameterCollector(null);
        ReportRuntimeLogger log = new ReportRuntimeLogger.ConsoleReportRuntimeLogger(report);

        IReportRender render = new BasicHtmlRender(new IHtmlWriter.FileHtmlWriter("c:\\sqlreport.htm")
            ,new VisiFireRender());
        ReportAgent agent = new ReportAgent(user,report,pc,render,log);
        agent.processReport(0,0);
        //ParameterHtmlGenerator.generateSection(report);
    }
}

