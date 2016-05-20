/* 
 * ==================================================
 * [ SYSTEM ]		: Web::Java ??  ( HOBOKEN )
 * [ PROJECT ]		: ????
 * 
 * 
 * $Id: MultiplePieChartServiceImpl.java 1091 2009-07-29 09:38:14Z mezawa_takuji $
 * ==================================================
 */
package example.hoboken.service.chart.impl;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.urls.StandardPieURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.util.TableOrder;

import example.hoboken.Constant;

import prj.hoboken.patrasche.dao.TableTxDao;
import prj.hoboken.patrasche.dao.exception.BaseDaoException;
import prj.hoboken.patrasche.exception.impl.NoDataException;
import prj.hoboken.patrasche.service.chart.ChartHolder;
import prj.hoboken.patrasche.service.chart.exception.ChartViolationException;
import prj.hoboken.patrasche.service.chart.impl.AbstractChartService;

/**
 * [ ???? ]<br>
 * ?????????????????????
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
public class MultiplePieChartServiceImpl extends AbstractChartService {

    private TableTxDao categoryDao;       /* ?????? */
    
    
    /**
     * ??? <code>MultiplePieChartServiceImpl</code> ??????????????
     */
    public MultiplePieChartServiceImpl() {
        this.categoryDao = null;
    }
    
    
    /* (? Javadoc)
     * @see prj.hoboken.patrasche.service.chart.impl.AbstractChartService#addChartDetailMap()
     */
    protected Map addChartDetailMap() {
        Map _map = new HashMap();
        
        _map.put(DETAIL_ROW, getChartDetails().getProperty("rowKey"));
        _map.put(DETAIL_COL, getChartDetails().getProperty("columnKey"));
        
        return _map;
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
        
        double[][] _dataset = (double[][])_map.get(CAT_DS_MAP_KEY);
        
        return
            DatasetUtilities.createCategoryDataset(
                getChartDetails().getProperty(DETAIL_ROW),
                getChartDetails().getProperty(DETAIL_COL),
                _dataset
            );
    }

    /* (? Javadoc)
     * @see prj.hoboken.patrasche.service.chart.ChartService#createChart(org.jfree.data.general.Dataset)
     */
    public JFreeChart createChart(Dataset dataset) {
        boolean _isToolTips = Boolean.valueOf(getChartDetails().getProperty(TOOL_TIPS)).booleanValue();
        boolean _isClickable = Boolean.valueOf(getChartDetails().getProperty(IS_CLICKABLE)).booleanValue();
        
        /* ????????????? */
        JFreeChart _chart = null;
        if (dataset instanceof CategoryDataset) {
            _chart = ChartFactory.createMultiplePieChart(
                getChartDetails().getProperty(DETAIL_TITLE),
                (CategoryDataset)dataset,
                TableOrder.BY_ROW,
                Boolean.valueOf(getChartDetails().getProperty(INC_LEGEND)).booleanValue(),
                _isToolTips,
                _isClickable
            );
        } else {
            throw new ChartViolationException(
                "??????????????????????????????????????????");
        }
        
        MultiplePiePlot _plot = (MultiplePiePlot)_chart.getPlot();
        
        /* ??????????????????????????? */
        JFreeChart _subchart = _plot.getPieChart();
        
        PiePlot _piePlot = (PiePlot)_subchart.getPlot();
        _piePlot.setLabelGenerator(new StandardPieSectionLabelGenerator(getChartDetails().getProperty("labelFormat.0")));
        _piePlot.setLabelFont(new Font("SansSerif", Font.PLAIN, 8));
        _piePlot.setInteriorGap(0.30);

        /*
         * ????????????????????
         */
        
        if (_isToolTips) {
            _piePlot.setToolTipGenerator(new StandardPieToolTipGenerator());
        }
        
        if (_isClickable) {
            StringBuffer _path = new StringBuffer(getChartDetails().getProperty(MAP_BASEURL));
            _path.append(getChartDetails().getProperty("mapURL"));
            _piePlot.setURLGenerator(new StandardPieURLGenerator(_path.toString()));
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
