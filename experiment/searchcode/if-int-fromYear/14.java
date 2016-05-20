/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shapefileloader.gcm;

import database.DBUtils;
import domain.web.AnnualGcmDatum;
import domain.web.GcmDatum;
import domain.web.MonthlyGcmDatum;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wb385924
 */
public class P_GcmConfigAreaDao implements P_ConfigAreaDao {

    private static P_GcmConfigAreaDao dao = null;
    private static final Logger log = Logger.getLogger(P_GcmConfigAreaDao.class.getName());

    private P_GcmConfigAreaDao() {
    }

    public static P_GcmConfigAreaDao get() {
        if (dao == null) {
            dao = new P_GcmConfigAreaDao();
        }
        return dao;
    }

    public static void main(String[] args) {
        P_Config config = new P_NameParser().parsePathName("C:\\Users\\wb385924\\OLD_CLIMATE_DATA\\gcm_long_anom_monthly\\GCM_long_anom_monthly.shp\\bccr_bcm2_0\\pcmdi_long_anom.bccr_bcm2_0.pr_20c3m.1920-1939.shp");
        config.setMonth(1);
        P_GcmConfigDao.get().getConfigId((P_GcmConfig) config);
        config.setAreaId(544);
        if (config.isCompleteIgnoringAreaValueMonth()) {
            System.out.println(config.toString());
            HashMap<Integer, Double> monthVals = new P_GcmConfigAreaDao().getAreaDataForTime((P_GcmConfig) config, false);
            System.out.println("data is  " + monthVals);
        }
    }
    private String INSERT_GCM_CONFIG_AREA = "insert into p_gcm_config_area (p_gcm_config_area_gcm_config_id, p_gcm_config_area_area_id, p_gcm_config_area_value) values (?, ?, ?)";

    public void insertAreaValue(int configId, int areaId, double value) {


        Connection c = null;
        PreparedStatement ps = null;

        try {
            c = DBUtils.getConnection();
            ps = c.prepareStatement(INSERT_GCM_CONFIG_AREA);
            ps.setInt(1, configId);
            ps.setInt(2, areaId);
            ps.setDouble(3, value);
            ps.executeUpdate();

        } catch (SQLException sqle) {
            try {
                sqle.printStackTrace();
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(P_GcmConfigAreaDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally {
            try {
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(P_GcmConfigAreaDao.class.getName()).log(Level.SEVERE, null, ex);
            }
            DBUtils.close(c, ps, null);
        }
    }
    private String GET_GCM_CONFIG_AREA = "select p_gcm_config_month, p_gcm_config_area_value from  p_gcm_config inner join p_gcm_config_area on p_gcm_config_id = p_gcm_config_area_gcm_config_id and "
            + "  p_gcm_config_area_area_id = ? and p_gcm_config_o_stat_type_id = ? and  p_gcm_config_o_var_id = ?  and p_gcm_config_from_year = ?  and p_gcm_config_to_year = ? and  "
            + "p_gcm_config_gcm_id = ? and p_gcm_config_scenario_id = ? ";
    private final String monthlyConstraint = "and p_gcm_config_month != -1";
    private final String annualConstraint = "and p_gcm_config_month = -1";
    private final String p_gcm_config_month = "p_gcm_config_month";
    private final String p_gcm_config_area_value = "p_gcm_config_area_value";
    private final String p_gcm_config_from_year = "p_gcm_config_from_year";
    private final String p_gcm_config_to_year = "p_gcm_config_to_year";
    private final String p_gcm_config_gcm_id = "p_gcm_config_gcm_id";
    private final String p_gcm_config_scenario_id = "p_gcm_config_scenario_id";
    private final String p_gcm_config_o_var_id = "p_gcm_config_o_var_id";

    private String constructMonthlyOrAnnualQueryString(String basePath, boolean isAnnual) {
        StringBuilder sb = new StringBuilder();
        sb.append(basePath);
        if (isAnnual) {
            sb.append(annualConstraint);
        } else {
            sb.append(monthlyConstraint);
        }
        return sb.toString();
    }

    /**
     * ===========
     *
     * GCM and Scenario
     *
     * ==============
     * @param config
     * @param isAnnual
     * @return
     */
    public HashMap<Integer, Double> getAreaDataForTime(P_Config config, boolean isAnnual) {
        long t0 = new Date().getTime();

        Connection c = DBUtils.getConnection();
        PreparedStatement ps = null;
        HashMap<Integer, Double> monthVals = new HashMap<Integer, Double>();
        String queryString = constructMonthlyOrAnnualQueryString(GET_GCM_CONFIG_AREA, isAnnual);
        ResultSet rs = null;
        try {

            ps = c.prepareStatement(queryString);
            ps.setInt(1, config.getAreaId());
            ps.setInt(2, config.getStatType().getId());
            ps.setInt(3, config.getStat().getId());
            ps.setInt(4, config.getfYear());
            ps.setInt(5, config.gettYear());
            ps.setInt(6, config.getGcm().getGcmId());
            ps.setInt(7, config.getScenario().getId());
//            ps.setInt(8, config.getMonth());
            rs = ps.executeQuery();
            while (rs.next()) {
                monthVals.put(rs.getInt(p_gcm_config_month), rs.getDouble(p_gcm_config_area_value));
            }
        } catch (SQLException ex) {
            Logger.getLogger(P_GcmConfigDao.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DBUtils.close(c, ps, rs);
        }
        long t1 = new Date().getTime();

        log.log(Level.INFO, "{0} seconds for query ", (t1 - t0) / 1000.0);
        return monthVals;
    }




    
    private String GET_All_CONFIG_AREA =
            " select p_gcm_config_gcm_id, p_gcm_config_month, p_gcm_config_area_value from  p_gcm_config inner join p_gcm_config_area on p_gcm_config_id = p_gcm_config_area_gcm_config_id and "
            + "  p_gcm_config_area_area_id = ? and p_gcm_config_o_stat_type_id = ? and  p_gcm_config_o_var_id = ?  "
            + "  and p_gcm_config_scenario_id = ? and p_gcm_config_from_year = ? and p_gcm_config_to_year = ?";

    /**
     * ===========
     *
     * scenario only
     *
     * ===========
     * @param config
     * @param isAnnual
     * @return
     */
    public List<GcmDatum> getAllGcmAreaData(P_Config config, boolean isAnnual) {
        long t0 = new Date().getTime();
        Connection c = DBUtils.getConnection();
        PreparedStatement ps = null;
        HashMap<String, GcmDatum> gcmData = new HashMap<String, GcmDatum>();

        String queryString = constructMonthlyOrAnnualQueryString(GET_All_CONFIG_AREA, isAnnual);
        ResultSet rs = null;
        try {
            ps = c.prepareStatement(queryString);
            ps.setInt(1, config.getAreaId());
            ps.setInt(2, config.getStatType().getId());
            ps.setInt(3, config.getStat().getId());
            ps.setInt(4, config.getScenario().getId());
            ps.setInt(5, config.getfYear());
            ps.setInt(6, config.gettYear());


            rs = ps.executeQuery();
            P_GcmStatsProperties props = P_GcmStatsProperties.getInstance();

            while (rs.next()) {
                P_GcmStatsProperties.gcm gcm = props.getGCMById(rs.getInt(p_gcm_config_gcm_id));

                double value = rs.getDouble(p_gcm_config_area_value);
                int month = rs.getInt(p_gcm_config_month);
                if (isAnnual) {
                    if (!gcmData.containsKey(gcm.toString())) {
                        gcmData.put(gcm.toString(), new AnnualGcmDatum(gcm.toString()));
                    }
                    month = 0;
                } else {
                    if (!gcmData.containsKey(gcm.toString())) {
                        gcmData.put(gcm.toString(), new MonthlyGcmDatum(gcm.toString()));
                    }
                    month -= 1;
                }
                gcmData.get(gcm.toString()).addVal(month, value);
            }
        } catch (SQLException ex) {
            Logger.getLogger(P_GcmConfigDao.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DBUtils.close(c, ps, rs);
        }
        long t1 = new Date().getTime();

        log.log(Level.INFO, "{0} seconds for query ", (t1 - t0) / 1000.0);
        return new ArrayList<GcmDatum>(gcmData.values());
    }




    /**
     * 
     * ============
     * 
     * gcm only
     * 
     * ===========
     */

    private String GET_SINGLE_GCM_ALL_SCENARIO_CONFIG_AREA =
            " select p_gcm_config_o_var_id, p_gcm_config_scenario_id, p_gcm_config_gcm_id, p_gcm_config_month, p_gcm_config_area_value from  p_gcm_config inner join p_gcm_config_area on p_gcm_config_id = p_gcm_config_area_gcm_config_id and "
            + "  p_gcm_config_area_area_id = ? and p_gcm_config_o_stat_type_id = ? and  p_gcm_config_o_var_id = ?  "
            + "  and p_gcm_config_gcm_id = ? and p_gcm_config_from_year = ? and p_gcm_config_to_year = ?";

    public List<GcmDatum> getGcmData(P_Config config, boolean isAnnual) {
        long t0 = new Date().getTime();
        Connection c = DBUtils.getConnection();
        PreparedStatement ps = null;
        HashMap<String, GcmDatum> gcmData = new HashMap<String, GcmDatum>();
        String request = P_GcmConfigAreaDaoRequestBuilder.request((P_GcmConfig)config);
        String queryString = constructMonthlyOrAnnualQueryString(request, isAnnual);
        ResultSet rs = null;
        try {
            ps = c.prepareStatement(queryString);
            rs = ps.executeQuery();
            P_GcmStatsProperties props = P_GcmStatsProperties.getInstance();

            while (rs.next()) {
                P_GcmStatsProperties.gcm gcm = props.getGCMById(rs.getInt(p_gcm_config_gcm_id));
                P_GcmStatsProperties.scenario scenario = props.getScenarioById(rs.getInt(p_gcm_config_scenario_id));
                P_GcmStatsProperties.climatestat climstat = props.getClimateStatById(rs.getInt(p_gcm_config_o_var_id));
                int fromYear = rs.getInt(p_gcm_config_from_year);
                int toYear = rs.getInt(p_gcm_config_to_year);
                String key = gcmHash(gcm, scenario, fromYear, toYear, climstat);

                double value = rs.getDouble(p_gcm_config_area_value);
                int month = rs.getInt(p_gcm_config_month);
                if (isAnnual) {
                    if (!gcmData.containsKey(key)) {
                        gcmData.put(key, new AnnualGcmDatum(gcm.toString()));
                    }
                    month = 0;
                } else {
                    if (!gcmData.containsKey(key)) {
                        gcmData.put(key, new MonthlyGcmDatum(gcm.toString()));
                    }
                    month -= 1;
                }
                if (fromYear > 2000) {
                    gcmData.get(key).setScenario(scenario.toString());
                } /**else {
                    gcmData.get(key).setModel(scenario.toString());
                }*/
                gcmData.get(key).addVal(month, value);
                gcmData.get(key).setFromYear(fromYear);
                gcmData.get(key).setToYear(toYear);
                gcmData.get(key).setVariable(climstat.toString());
            }
        } catch (SQLException ex) {
            Logger.getLogger(P_GcmConfigDao.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DBUtils.close(c, ps, rs);
        }
        long t1 = new Date().getTime();

        log.log(Level.INFO, "{0} seconds for query ", (t1 - t0) / 1000.0);
        List<GcmDatum> datums = new ArrayList<GcmDatum>(gcmData.values());
        Collections.sort(datums);
        return datums;
    }
//    public List<GcmDatum> getSingleGcmAllScenarioData(P_Config config, boolean isAnnual) {
//        long t0 = new Date().getTime();
//        Connection c = DBUtils.getConnection();
//        PreparedStatement ps = null;
//        HashMap<String, GcmDatum> gcmData = new HashMap<String, GcmDatum>();
//
//        String queryString = constructMonthlyOrAnnualQueryString(GET_SINGLE_GCM_ALL_SCENARIO_CONFIG_AREA, isAnnual);
//        ResultSet rs = null;
//        try {
//            ps = c.prepareStatement(queryString);
//            ps.setInt(1, config.getAreaId());
//            ps.setInt(2, config.getStatType().getId());
//            ps.setInt(3, config.getStat().getId());
//            ps.setInt(4, config.getGcm().getGcmId());
//
//            ps.setInt(5, config.getfYear());
//            ps.setInt(6, config.gettYear());
//
//
//            rs = ps.executeQuery();
//            P_GcmStatsProperties props = P_GcmStatsProperties.getInstance();
//
//            while (rs.next()) {
//                P_GcmStatsProperties.gcm gcm = props.getGCMById(rs.getInt(p_gcm_config_gcm_id));
//                P_GcmStatsProperties.scenario scenario = props.getScenarioById(rs.getInt(p_gcm_config_scenario_id));
//                String key = gcmScenarioHash(gcm, scenario);
//
//                double value = rs.getDouble(p_gcm_config_area_value);
//                int month = rs.getInt(p_gcm_config_month);
//                if (isAnnual) {
//                    if (!gcmData.containsKey(key)) {
//                        gcmData.put(key, new AnnualGcmDatum(gcm.toString()));
//                    }
//                    month = 0;
//                } else {
//                    if (!gcmData.containsKey(key)) {
//                        gcmData.put(key, new MonthlyGcmDatum(gcm.toString()));
//                    }
//                    month -= 1;
//                }
//                if (config.getfYear() > 2000) {
//                    gcmData.get(key).setScenario(scenario.toString());
//                } /**else {
//                    gcmData.get(key).setModel(scenario.toString());
//                }*/
//                gcmData.get(key).addVal(month, value);
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(P_GcmConfigDao.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            DBUtils.close(c, ps, rs);
//        }
//        long t1 = new Date().getTime();
//
//        log.log(Level.INFO, "{0} seconds for query ", (t1 - t0) / 1000.0);
//        return new ArrayList<GcmDatum>(gcmData.values());
//    }





    /**
     * =====
     *
     * no scenario no gcm
     *
     * =====
     */
    private String GET_All_SCENARIO_ALL_GCM_CONFIG_AREA =
            " select p_gcm_config_scenario_id, p_gcm_config_gcm_id, p_gcm_config_month, p_gcm_config_area_value from  p_gcm_config inner join p_gcm_config_area on p_gcm_config_id = p_gcm_config_area_gcm_config_id and "
            + "  p_gcm_config_area_area_id = ? and p_gcm_config_o_stat_type_id = ? and  p_gcm_config_o_var_id = ?  "
            + "  and p_gcm_config_from_year = ? and p_gcm_config_to_year = ?";

    public List<GcmDatum> getAllScenarioAllGcmData(P_Config config, boolean isAnnual) {
        long t0 = new Date().getTime();
        Connection c = DBUtils.getConnection();
        PreparedStatement ps = null;
        HashMap<String, GcmDatum> gcmData = new HashMap<String, GcmDatum>();

        String queryString = constructMonthlyOrAnnualQueryString(GET_All_SCENARIO_ALL_GCM_CONFIG_AREA, isAnnual);
        ResultSet rs = null;
        try {
            ps = c.prepareStatement(queryString);
            ps.setInt(1, config.getAreaId());
            ps.setInt(2, config.getStatType().getId());
            ps.setInt(3, config.getStat().getId());

            ps.setInt(4, config.getfYear());
            ps.setInt(5, config.gettYear());


             rs = ps.executeQuery();
            P_GcmStatsProperties props = P_GcmStatsProperties.getInstance();

            while (rs.next()) {
                P_GcmStatsProperties.gcm gcm = props.getGCMById(rs.getInt(p_gcm_config_gcm_id));
                P_GcmStatsProperties.scenario scenario = props.getScenarioById(rs.getInt(p_gcm_config_scenario_id));
                String key = gcmScenarioHash(gcm, scenario);

                double value = rs.getDouble(p_gcm_config_area_value);
                int month = rs.getInt(p_gcm_config_month);
                if (isAnnual) {
                    if (!gcmData.containsKey(key)) {
                        gcmData.put(key, new AnnualGcmDatum(gcm.toString()));
                    }
                    month = 0;
                } else {
                    if (!gcmData.containsKey(key)) {
                        gcmData.put(key, new MonthlyGcmDatum(gcm.toString()));
                    }
                    month -= 1;
                }
                if (config.getfYear() > 2000) {
                    gcmData.get(key).setScenario(scenario.toString());
                } /**else {
                    gcmData.get(key).setModel(scenario.toString());
                }*/
                gcmData.get(key).addVal(month, value);
            }
        } catch (SQLException ex) {
            Logger.getLogger(P_GcmConfigDao.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DBUtils.close(c, ps, rs);
        }
        long t1 = new Date().getTime();

        log.log(Level.INFO, "{0} seconds for query ", (t1 - t0) / 1000.0);
        return new ArrayList<GcmDatum>(gcmData.values());
    }

    private String gcmHash(P_GcmStatsProperties.gcm gcm, P_GcmStatsProperties.scenario scenario, int fromYear, int toYear, P_GcmStatsProperties.climatestat stat) {

        StringBuilder sb = new StringBuilder();
        if (gcm != null) {
            sb.append(gcm.toString());
        }
        if (scenario != null) {
            sb.append(scenario.toString());
        }
        if (stat != null) {
            sb.append(stat.toString());
        }
        sb.append(fromYear);
        sb.append(toYear);

        return sb.toString();

    }
    private String gcmScenarioHash(P_GcmStatsProperties.gcm gcm, P_GcmStatsProperties.scenario scenario) {

        StringBuilder sb = new StringBuilder();
        if (gcm != null) {
            sb.append(gcm.toString());
        }
        if (scenario != null) {
            sb.append(scenario.toString());
        }

        return sb.toString();

    }
//    public List<MonthlyGcmDatum> getAllAreaData(P_Config config, boolean isAnnual) {
//        long t0 = new Date().getTime();
//        Connection c = DBUtils.getConnection();
//        PreparedStatement ps = null;
//        HashMap<String,MonthlyGcmDatum> gcmData = new  HashMap<String,MonthlyGcmDatum>();
//
//        String queryString = constructMonthlyOrAnnualQueryString(GET_All_CONFIG_AREA, isAnnual);
//        try {
//            ps = c.prepareStatement(queryString);
//            ps.setInt(1, config.getAreaId());
//            ps.setInt(2, config.getStatType().getId());
//            ps.setInt(3, config.getStat().getId());
//            ps.setInt(4, config.getScenario().getId());
//            ps.setInt(5, config.getfYear());
//            ps.setInt(6, config.gettYear());
//
//
//            ResultSet rs = ps.executeQuery();
//            P_GcmStatsProperties props = P_GcmStatsProperties.getInstance();
//
//            while (rs.next()) {
//                P_GcmConfig gcmConfig = new P_GcmConfig();
//                P_GcmStatsProperties.gcm gcm = props.getGCMById(rs.getInt(p_gcm_config_gcm_id));
//                if(!gcmData.containsKey(gcm.toString())){
//                    gcmData.put(gcm.toString(),new MonthlyGcmDatum(gcm.toString()));
//                }
//                double value = rs.getDouble(p_gcm_config_area_value);
//                gcmConfig.setValue(value);
//                int month = rs.getInt(p_gcm_config_month);
//                if(isAnnual){
//                    month = 0;
//                }else{
//                    month -= 1;
//                }
//                gcmData.get(gcm.toString()).addMonthVal(month, value);
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(P_GcmConfigDao.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            DBUtils.close(c, ps, null);
//        }
//        long t1 = new Date().getTime();
//
//        log.log( Level.INFO, "{0} seconds for query ", (t1 - t0) / 1000.0);
//        return new ArrayList<MonthlyGcmDatum>(gcmData.values());
//    }
    private String GET_GCM_CONFIG_AREA_START_YEAR_RANGE = "select p_gcm_config_from_year,p_gcm_config_month, p_gcm_config_area_value from  p_gcm_config inner join p_gcm_config_area on p_gcm_config_id = p_gcm_config_area_gcm_config_id and "
            + "  p_gcm_config_area_area_id = ? and p_gcm_config_o_stat_type_id = ? and  p_gcm_config_o_var_id = ?  and p_gcm_config_from_year >= ?  and p_gcm_config_from_year <=? and  "
            + "p_gcm_config_gcm_id = ? and p_gcm_config_scenario_id = ? ";

    public TreeMap<Integer, HashMap<Integer, Double>> getAreaDataForStartYearRange(P_Config config, int fromStartYear, int toStartYear, boolean isAnnual) {
        long t0 = new Date().getTime();
        Connection c = DBUtils.getConnection();
        PreparedStatement ps = null;
        TreeMap<Integer, HashMap<Integer, Double>> monthVals = new TreeMap<Integer, HashMap<Integer, Double>>();
        String queryString = constructMonthlyOrAnnualQueryString(GET_GCM_CONFIG_AREA_START_YEAR_RANGE, isAnnual);
        ResultSet rs = null;
        try {
            ps = c.prepareStatement(queryString);
            ps.setInt(1, config.getAreaId());
            ps.setInt(2, config.getStatType().getId());
            ps.setInt(3, config.getStat().getId());
            ps.setInt(4, fromStartYear);
            ps.setInt(5, toStartYear);
            ps.setInt(6, config.getGcm().getGcmId());
            ps.setInt(7, config.getScenario().getId());
//            ps.setInt(8, config.getMonth());
            rs = ps.executeQuery();
            while (rs.next()) {
                int fyear = rs.getInt(p_gcm_config_from_year);
                if (!monthVals.containsKey(fyear)) {
                    monthVals.put(fyear, new HashMap<Integer, Double>());
                }
                monthVals.get(fyear).put(rs.getInt(p_gcm_config_month), rs.getDouble(p_gcm_config_area_value));
            }
        } catch (SQLException ex) {
            Logger.getLogger(P_GcmConfigDao.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DBUtils.close(c, ps, rs);
        }
        long t1 = new Date().getTime();

        log.log(Level.INFO, "{0} seconds for query ", (t1 - t0) / 1000.0);
        return monthVals;
    }
}

