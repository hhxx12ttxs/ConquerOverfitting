/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shapefileloader.oldclimate;

import shapefileloader.gcm.P_GcmStatsProperties;
import cache.GcmCache;
import dao.country.CountryDao;
import database.DBUtils;
import domain.Country;
import domain.DerivativeStats;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wb385924
 */
public class OldMonthlyDao {

    private static OldMonthlyDao dao = null;
    private final String AVG = "avg";
    private final String insert = "insert into o_monthly (o_monthly_o_stat_type_id, o_monthly_o_var_id, o_monthly_from_year, o_monthly_to_year, o_monthly_month, o_monthly_value, o_monthly_cell_id, o_monthly_gcm_id, o_monthly_scenario_id) values(?,?,?,?,?,?,?,?,?)";
    private final String select_contained_points = "select o_cell_id from o_cell where st_intersects((select boundary_shape from boundary where boundary_area_id = ?),o_cell_geom)";
    private final String insert_cell_area = "insert into o_cell_area(o_cell_area_o_cell_id, o_cell_area_area_id) values(?,?)";
    private static final Logger log = Logger.getLogger(OldMonthlyDao.class.getName());

    public static OldMonthlyDao get() {
        if (dao == null) {
            dao = new OldMonthlyDao();
        }

        return dao;
    }

    public void insertCellArea(long cellId, int areaId) {
        Connection c = null;
        PreparedStatement ps = null;
        try {

            c = DBUtils.getConnection();
            ps = c.prepareStatement(insert_cell_area);
            ps.setLong(1, cellId);
            ps.setLong(2, areaId);
            ps.executeUpdate();
        } catch (SQLException sqle) {
            Logger.getLogger(OldMonthlyDao.class.getName()).log(Level.SEVERE, null, sqle);
        } finally {
            DBUtils.close(c, ps, null);
        }
    }

    public static void main(String[] args) {
        P_GcmStatsProperties.getInstance();
        OldMonthlyDao dao = OldMonthlyDao.get();
        dao.loadBaselineCache(1920, 1939, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.precipstat.pr);
        dao.loadBaselineCache(1920, 1939, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.tempstat.tas);

        dao.loadBaselineCache(1940, 1959, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.precipstat.pr);
        dao.loadBaselineCache(1940, 1959, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.tempstat.tas);

        dao.loadBaselineCache(1960, 1979, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.precipstat.pr);
        dao.loadBaselineCache(1960, 1979, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.tempstat.tas);

        dao.loadBaselineCache(1980, 1999, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.precipstat.pr);
        dao.loadBaselineCache(1980, 1999, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.tempstat.tas);
        

        log.info("loading future scen a2");
        dao.loadFutureCache(2020, 2039, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.scenario.a2, P_GcmStatsProperties.tempstat.tas);
        dao.loadFutureCache(2020, 2039, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.scenario.a2, P_GcmStatsProperties.precipstat.pr);

        dao.loadFutureCache(2040, 2059, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.scenario.a2, P_GcmStatsProperties.tempstat.tas);
        dao.loadFutureCache(2040, 2059, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.scenario.a2, P_GcmStatsProperties.precipstat.pr);

        dao.loadFutureCache(2080, 2099, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.scenario.a2, P_GcmStatsProperties.tempstat.tas);
        dao.loadFutureCache(2080, 2099, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.scenario.a2, P_GcmStatsProperties.precipstat.pr);

        log.info("loading future scen b1");
        dao.loadFutureCache(2020, 2039, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.scenario.b1, P_GcmStatsProperties.tempstat.tas);
        dao.loadFutureCache(2020, 2039, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.scenario.b1, P_GcmStatsProperties.precipstat.pr);

        dao.loadFutureCache(2040, 2059, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.scenario.b1, P_GcmStatsProperties.tempstat.tas);
        dao.loadFutureCache(2040, 2059, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.scenario.b1, P_GcmStatsProperties.precipstat.pr);

        dao.loadFutureCache(2080, 2099, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.scenario.b1, P_GcmStatsProperties.tempstat.tas);
        dao.loadFutureCache(2080, 2099, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.scenario.b1, P_GcmStatsProperties.precipstat.pr);

        
        float t0 = new Date().getTime();

        List<Double> values = GcmCache.get().getValues("SDN", new OldMonthlyCellularConfig(P_GcmStatsProperties.stat_type.mean, P_GcmStatsProperties.gcm.bccr_bcm2_0, P_GcmStatsProperties.scenario.s_20c3m, P_GcmStatsProperties.tempstat.tas, 1980, 1999));

        float t1 = new Date().getTime();

        log.log(Level.INFO,"took {0}",Float.toString((t1-t0)/1000.0f));
        log.log(Level.INFO,"size {0}",Integer.toString(values.size()));

        
//        double d = new OldMonthlyDao().getOldMonthlyData(7, 537, new OldMonthlyConfig(OldDerivativeStats.stat_type.mean, OldDerivativeStats.gcm.bccr_bcm2_0, OldDerivativeStats.scenario.s_20c3m, OldDerivativeStats.precipstat.pr, 1920, 1939));
//        System.out.println(d);
    }

    private void loadBaselineCache(int fromYear, int toYear, P_GcmStatsProperties.gcm gcm,P_GcmStatsProperties.climatestat stat) {
        P_GcmStatsProperties.getInstance();
        CountryDao cdao = CountryDao.get();
        List<Country> countries = cdao.getCountries();
        for (Country c : countries) {
            List<Double> vals = new ArrayList<Double>();
            for (int i = 1; i < 13; i++) {
                OldMonthlyCellularConfig config = new OldMonthlyCellularConfig(P_GcmStatsProperties.stat_type.mean, gcm, P_GcmStatsProperties.scenario.s_20c3m, stat, fromYear, toYear);
                Double val = OldMonthlyDao.get().getOldMonthlyData(i, c.getId(), config);
                if (val != null) {
                    vals.add(val);
                }

            }
            log.log(Level.FINE, "adding {0} for {1}", new Object[]{vals.size(), c.getIso3()});
            
            GcmCache.get().addToCache(c.getIso3(), new OldMonthlyCellularConfig(P_GcmStatsProperties.stat_type.mean, gcm, P_GcmStatsProperties.scenario.s_20c3m, stat, fromYear, toYear), vals);
            
        }
        
    }

    private void loadFutureCache(int fromYear, int toYear, P_GcmStatsProperties.gcm gcm, P_GcmStatsProperties.scenario scenario, P_GcmStatsProperties.climatestat stat) {
        P_GcmStatsProperties.getInstance();
        CountryDao cdao = CountryDao.get();
        List<Country> countries = cdao.getCountries();
        for (Country c : countries) {
            List<Double> vals = new ArrayList<Double>();
            for (int i = 1; i < 13; i++) {
                OldMonthlyCellularConfig config = new OldMonthlyCellularConfig(P_GcmStatsProperties.stat_type.mean, gcm, scenario, stat, fromYear, toYear);
                Double val = OldMonthlyDao.get().getOldMonthlyData(i, c.getId(), config);
                if (val != null) {
                    vals.add(val);
                }

            }
            log.log(Level.INFO, "adding {0} for {1}", new Object[]{vals.size(), c.getIso3()});

            GcmCache.get().addToCache(c.getIso3(), new OldMonthlyCellularConfig(P_GcmStatsProperties.stat_type.mean, gcm, scenario, stat, fromYear, toYear), vals);

        }

    }

    public void populateCellAera(int areaId) {
        Connection c = null;
        PreparedStatement ps = null;
        try {

            c = DBUtils.getConnection();
            ps = c.prepareStatement(select_contained_points);
            ps.setInt(1, areaId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                insertCellArea(rs.getLong("o_cell_id"), areaId);
            }
        } catch (SQLException sqle) {
            Logger.getLogger(OldMonthlyDao.class.getName()).log(Level.SEVERE, null, sqle);
        } finally {
            DBUtils.close(c, ps, null);
        }

    }

    public void saveMonthlyData(OldMonthlyCellularConfig config) {

        Connection c = null;
        PreparedStatement ps = null;
        try {

            c = DBUtils.getConnection();
            ps = c.prepareStatement(insert);
            ps.setInt(1, config.getStatType().getId());
            ps.setInt(2, config.getStat().getId());
            ps.setInt(3, config.getfYear());
            ps.setInt(4, config.gettYear());
            ps.setInt(5, config.getMonth());
            ps.setDouble(6, config.getValue());
            ps.setLong(7, config.getCellId());
            ps.setInt(8, config.getGcm().getGcmId());
            ps.setInt(9, config.getScenario().getId());
            ps.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(OldMonthlyDao.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DBUtils.close(c, ps, null);
        }

    }
    private final String select =
            "select avg(o_monthly_value) from o_monthly inner join o_cell on o_monthly_cell_id = o_cell_id inner join o_cell_area on o_cell_area_o_cell_id = o_cell.o_cell_id and "
            + "o_cell_area_area_id = ? where o_monthly_o_stat_type_id = ? and o_monthly_from_year = ? AND o_monthly_to_year  = ? "
            + "and o_monthly_month=? and o_monthly_o_var_id = ? and o_monthly_gcm_id = ? and o_monthly_scenario_id =  ? ";

    public Double getOldMonthlyData(int month, int areaId, OldMonthlyCellularConfig config) {

        Connection c = null;
        PreparedStatement ps = null;
        Double d = null;
        try {

            c = DBUtils.getConnection();
            ps = c.prepareStatement(select);
            ps.setInt(1, areaId);
            ps.setInt(2, config.getStatType().getId());
            ps.setInt(3, config.getfYear());
            ps.setInt(4, config.gettYear());
            ps.setInt(5, month);
            ps.setInt(6, config.getStat().getId());
            ps.setInt(7, config.getGcm().getGcmId());
            ps.setInt(8, config.getScenario().getId());
            ResultSet rs = ps.executeQuery();


            while (rs.next()) {

                d = rs.getDouble(AVG);
//                log.info("avg is " + d);
                return d;
            }
        } catch (SQLException ex) {
            Logger.getLogger(OldMonthlyDao.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DBUtils.close(c, ps, null);
        }
        return d;

    }
}

