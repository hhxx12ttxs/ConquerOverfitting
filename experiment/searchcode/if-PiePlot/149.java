/* 
 * ==================================================
 * [ SYSTEM ]		: Web::Java ??  ( HOBOKEN )
 * [ PROJECT ]		: ????
 * 
 * 
 * $Id: PieChartServiceImpl.java 1091 2009-07-29 09:38:14Z mezawa_takuji $
 * ==================================================
 */
package example.hoboken.service.chart.impl;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.urls.StandardPieURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.PieDataset;

import example.hoboken.Constant;

import prj.hoboken.patrasche.dao.TableTxDao;
import prj.hoboken.patrasche.dao.exception.BaseDaoException;
import prj.hoboken.patrasche.exception.impl.NoDataException;
import prj.hoboken.patrasche.service.chart.ChartHolder;
import prj.hoboken.patrasche.service.chart.exception.ChartViolationException;
import prj.hoboken.patrasche.service.chart.impl.AbstractChartService;

/**
 * [ ???? ]<br>
 * ?????????????????????????
 * 
 * ????????????????????????????????????????????????
 * ???????
 * 
 * <p>
 * $Revision: 1091 $<br>$Date: 2009-07-29 18:38:14 +0900 (?, 29 7 2009) $
 * </p>
 * @since  JVM : J2SDK 1.4.2 : Servlet 2.3/JSP1.2 : Struts 1.2 : Spring 1.2
 * @since  Patrasche 3.0
 * 
 * @author
 *     Odaka Tetsuya  ( HOBOKEN Project )<br>
 *     Mezawa Takuji  ( HOBOKEN Project )<br>
 */
public class PieChartServiceImpl extends AbstractChartService {

    private TableTxDao categoryDao;       /* ?????? */
    
    
    /**
     * ??? <code>PieChartServiceImpl</code> ??????????????
     */
    public PieChartServiceImpl() {
        this.categoryDao = null;
    }
    
    
    /* (? Javadoc)
     * @see prj.hoboken.patrasche.service.chart.impl.AbstractChartService#addChartDetailMap()
     */
    protected Map addChartDetailMap() {
        // ??????????????????
        return null;
    }

    /* (? Javadoc)
     * @see prj.hoboken.patrasche.service.chart.ChartService#createCategoryDataset()
     */
    public Dataset createCategoryDataset() {
        Map _keys = new HashMap();
        _keys.put(Constant.CATEGORY_DS_KEY, CAT_DS_MAP_KEY);
        
        Map _map = null;
        try {
            _map = (Map)this.categoryDao.getRecordsByUniqueKey(null, _keys);
            
        } catch (BaseDaoException ex) {
            throw new NoDataException(ex.getMessage(), ex);
        }
        
        return (Dataset)_map.get(CAT_DS_MAP_KEY);
    }

    /* (? Javadoc)
     * @see prj.hoboken.patrasche.service.chart.ChartService#createChart(org.jfree.data.general.Dataset)
     */
    public JFreeChart createChart(Dataset dataset) {
        // CategoryDataset??????????????PieDataset?????????
        PieDataset _dataset = null;
        if (dataset instanceof CategoryDataset) {
            _dataset = DatasetUtilities.createPieDatasetForRow((CategoryDataset)dataset, 0);
        } else {
            throw new ChartViolationException(
                "??????????????????????????????????????????");
        }

        boolean _isToolTips = Boolean.valueOf(getChartDetails().getProperty(TOOL_TIPS)).booleanValue();
        boolean _isClickable = Boolean.valueOf(getChartDetails().getProperty(IS_CLICKABLE)).booleanValue();
        
        /* ???????????? */
        JFreeChart _chart = ChartFactory.createPieChart(
            getChartDetails().getProperty(DETAIL_TITLE),
            _dataset,
            Boolean.valueOf(getChartDetails().getProperty(INC_LEGEND)).booleanValue(),
            _isToolTips,
            _isClickable
        );

        PiePlot _plot = (PiePlot)_chart.getPlot();
        // _plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        _plot.setNoDataMessage("No data available");
        _plot.setCircular(Boolean.valueOf(getChartDetails().getProperty("circular")).booleanValue());
        _plot.setLabelLinkPaint(Color.RED);
        _plot.setLabelGap(0.02);
        
        /*
         * ????????????????????
         */
        
        if (_isToolTips) {
            _plot.setToolTipGenerator(new StandardPieToolTipGenerator());
        }
        
        if (_isClickable) {
            StringBuffer _path = new StringBuffer(getChartDetails().getProperty(MAP_BASEURL));
            _path.append(getChartDetails().getProperty("mapURL"));
            _plot.setURLGenerator(new StandardPieURLGenerator(_path.toString()));
        }
                
        return _chart;
    }

// ------------------------------------------------------------------- [ setter : DI ]

    /**
     * ????????????????
     * ???DI?????????????????
     * 
     * @param categoryDao ?????????
     */
    public void setCategoryDao(TableTxDao categoryDao) {
        this.categoryDao = categoryDao;
    }
    
    /**
     * ?????????????????
     * ???DI?????????????????
     * 
     * @param chartHolder ??????????
     */
    public void setChartHolder(ChartHolder chartHolder) {
        super.setHolder(chartHolder);
    }
    
    /**
     * ???????????????????
     * ???DI?????????????????
     * 
     * @param chartDetails ????????????
     */
    public void setDetails(Properties chartDetails) {
        super.setChartDetails(chartDetails);
    }
}


/* Copyright (C) 2005, HOBOKEN Project, All Rights Reserved. */
