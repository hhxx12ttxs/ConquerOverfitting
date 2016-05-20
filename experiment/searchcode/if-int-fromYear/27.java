/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain;

import tnccsv.DataFileHandler;
import dao.GeoDao;

import database.DBUtils;

import java.sql.Connection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wb385924
 */
public class DerivativeStats {

    private static final Logger log = Logger.getLogger(DerivativeStats.class.getName());
    public static final int RUN = 1;

    public enum gcm {

        cccma_cgcm3_1, cnrm_cm3, bccr_bcm2_0, gfdl_cm2_0, gfdl_cm2_1, ipsl_cm4, miroc3_2_medres, miub_echo_g, mpi_echam5, mri_cgcm2_3_2a;
        private int gcmId;

        public int getGcmId() {
            return gcmId;
        }

        public void setId(int id) {
            this.gcmId = id;
        }
    }

    public enum scenario {

        b1("b1"), a1b("a1b"), a2("a2"), s_20c3m("20c3m");
        private int scenarioId;
        private String realName;

        scenario(String realName) {
            this.realName = realName;
        }

        @Override
        public String toString() {
            return realName;
        }

        public void setId(int id) {
            this.scenarioId = id;
        }

        public int getId() {
            return scenarioId;
        }
    }

    public interface climatestat {

        public int getId();

        public boolean isMonthly();
    }

    public enum tempstat implements climatestat {

        tasmin(true), tasmax(true), txx(true), tnn(true), tx10p(true), tx90(false), fd(true), tx90p(true), tn90p(true), tn10p(true), hwdi(false), gd10(true), hd18(true), cd18(true)/*gsl*/;
        private int id;
        private boolean isMonthly = false;

        tempstat(boolean isMonthly) {
            this.isMonthly = isMonthly;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public boolean isMonthly() {
            return isMonthly;
        }
    }

    public enum precipstat implements climatestat {

        pr(true), cdd(false), cdd5(false), r02(true), r90p(true), r90ptot(true), r5d(false), sdii(true);
        private int id;
        private boolean isMonthly;

        precipstat(boolean isMonthly) {
            this.isMonthly = isMonthly;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public boolean isMonthly() {
            return isMonthly;
        }
    }

    public enum temporal_aggregation {

        yearly, monthly;
        private int id;

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum time_period {

        mid_century(2046, 2065), end_century(2081, 2100), baseline(1961, 1990);
        private int id;
        private int fromYear;

        public int getFromYear() {
            return fromYear;
        }

        public int getToYear() {
            return toYear;
        }
        private int toYear;

        time_period(int fromYear, int toYear) {
            this.fromYear = fromYear;
            this.toYear = toYear;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }
    }

    public enum stat_type {

        mean;
        private int id;

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum file_type {

        table, map
    }

    public interface file_name {
    };

    public enum table_file_name implements file_name {

        table_yearly_AR4, table_yearly_baseline_AR4, table_yearly_depart_AR4
    }

    public enum map_file_name implements file_name {

        map_mean_AR4, map_mean_baseline_AR4, map_mean_depart_AR4, map_mean_ensemble_100_, map_mean_ensemble_50_, map_mean_ensemble_20_, map_mean_ensemble_80_, map_mean_ensemble_0_, map_mean_baseline_ensemble_100_, map_mean_baseline_ensemble_20_, map_mean_baseline_ensemble_50_, map_mean_baseline_ensemble_80_, map_mean_baseline_ensemble_0_
    }
    private EnumMap<gcm, EnumMap<scenario, Integer>> runMap = new EnumMap<gcm, EnumMap<scenario, Integer>>(gcm.class);
    public HashMap<String, gcm> gcmMap = new HashMap<String, gcm>();
    public HashMap<String, scenario> scenarioMap = new HashMap<String, scenario>();
    public HashMap<String, climatestat> statMap = new HashMap<String, climatestat>();
    private static DerivativeStats derivativeStats = null;

    private DerivativeStats() {
        init();
    }

    public String getFileExtension(DerivativeStats.file_type file) {
        if (file.name().equals(DerivativeStats.file_type.table.name())) {
            return DataFileHandler.CSV_FILE;
        } else if (file.name().equals(DerivativeStats.file_type.map.name())) {
            return DataFileHandler.ASCII_FILE;
        }
        log.log(Level.WARNING, "could not discern file type for{0}", file.name());
        return null;
    }

    public static DerivativeStats getInstance() {
        if (derivativeStats == null) {

            derivativeStats = new DerivativeStats();

        }
        return derivativeStats;
    }

    private void initializeGcms() {
        gcm[] gcms = DerivativeStats.gcm.values();
        Connection c = DBUtils.getConnection();

        for (gcm g : gcms) {
            gcmMap.put(g.name(), g);
            int id = GeoDao.getEntityId(c, "gcm", "code", g.toString());
            if (id == -1) {
                log.log(Level.WARNING, "got -1 id for gcm {0}", g.toString());
            } else {
                log.log(Level.FINE, "successfully set id for {0}", g.toString());
                g.setId(id);
            }
        }

        DBUtils.close(c);

    }

    private void initializeTemporalAggregation() {
        temporal_aggregation[] temporal_aggregations = DerivativeStats.temporal_aggregation.values();
        Connection c = DBUtils.getConnection();

        for (temporal_aggregation t : temporal_aggregations) {
            int id = GeoDao.getEntityId(c, "temporal_aggregation", "name", t.toString());
            if (id == -1) {
                log.log(Level.WARNING, "got -1 id for temporal_aggregation {0}", t.toString());
            } else {
                log.log(Level.FINE, "successfully set id for {0}", t.toString());
                t.setId(id);
            }
        }

        DBUtils.close(c);

    }

    private void initializeStatType() {
        stat_type[] stat_types = DerivativeStats.stat_type.values();
        Connection c = DBUtils.getConnection();

        for (stat_type s : stat_types) {
            int id = GeoDao.getEntityId(c, "statistic_type", "name", s.toString());
            if (id == -1) {
                log.log(Level.WARNING, "got -1 id for stat_type {0}", s.toString());

            } else {
                log.log(Level.FINE, "successfully set id for {0}", s.toString());
                s.setId(id);
            }
        }

        DBUtils.close(c);

    }

    private void initializeTimePeriods() {
        time_period[] time_periods = DerivativeStats.time_period.values();
        Connection c = DBUtils.getConnection();

        for (time_period s : time_periods) {
            int id = GeoDao.getEntityId(c, "time_period", "name", s.toString());
            if (id == -1) {
                log.log(Level.WARNING, "got -1 id for time_period {0}", s.toString());
            } else {
                log.log(Level.FINE, "successfully set id for {0}", s.toString());
                s.setId(id);
            }
        }

        DBUtils.close(c);
    }

    private void initializePrecipStats() {
        precipstat[] precipstats = DerivativeStats.precipstat.values();
        Connection c = DBUtils.getConnection();

        for (precipstat s : precipstats) {
            statMap.put(s.name(), s);

            int id = GeoDao.getEntityId(c, "climate_Statistic_type", "code", s.toString());
            if (id == -1) {
                log.log(Level.WARNING, "got -1 id for precipstat {0}", s.toString());
            } else {
                log.log(Level.INFO, "successfully set id for {0}", s.toString());
                s.setId(id);
            }
        }

        DBUtils.close(c);

    }

    private void initializeTempStats() {
        tempstat[] tempstats = DerivativeStats.tempstat.values();
        Connection c = DBUtils.getConnection();

        for (tempstat t : tempstats) {
            statMap.put(t.name(), t);

            int id = GeoDao.getEntityId(c, "climate_Statistic_type", "code", t.toString());
            if (id == -1) {
                log.log(Level.WARNING, "got -1 id for climatestat{0}", t.toString());
            } else {
                log.log(Level.INFO, "successfully set id for {0}", t.toString());
                t.setId(id);
            }
        }

        DBUtils.close(c);

    }

    private void initializeScenarios() {
        scenario[] scenarios = DerivativeStats.scenario.values();
        Connection c = DBUtils.getConnection();

        for (scenario s : scenarios) {
            scenarioMap.put(s.toString(), s);

            int id = GeoDao.getEntityId(c, "scenario", "code", s.toString());
            if (id == -1) {
                log.log(Level.WARNING, "got -1 id for scenario {0}", s.toString());
            } else {
                log.log(Level.FINE, "successfully set id for {0}", s.toString());
                s.setId(id);

            }
        }

        DBUtils.close(c);

    }

    private synchronized void init() {
        if (runMap.size() > 0) {
            log.log(Level.WARNING, "trying to initizialize more than once for some reason");
            return;
        }

        initializeRuns();

        initializeGcms();
        initializeTemporalAggregation();
        initializeStatType();
        initializeScenarios();
        initializePrecipStats();
        initializeTempStats();
        initializeTimePeriods();
    }

    public scenario getScenario(String scenarioName) {
        return scenarioMap.get(scenarioName);
    }

    public gcm getGcm(String gcmName) {
        return gcmMap.get(gcmName);
    }

    public climatestat getClimateStat(String climateStat) {
        return statMap.get(climateStat);
    }

    public boolean hasRun(gcm g, scenario s, int run) {
        if (!runMap.containsKey(g)) {
            log.log(Level.WARNING, "dont know of this gcm {0}", g.toString());
            return false;
        }

        if (!runMap.get(g).containsKey(s)) {
            log.log(Level.WARNING, "dont know of this scenario {0}", s.toString());
        }
        return runMap.get(g).get(s) >= run;
    }

    private void initializeRuns() {

//        EnumMap<scenario, Integer> bccr = new EnumMap<scenario,Integer>(scenario.class);
//        bccr.put(scenario.b1, 1);
//        bccr.put(scenario.a1b, 0);
//        bccr.put(scenario.a2, 0);
//        runMap.put(gcm.bccr_bcm2_0, bccr);

        EnumMap<scenario, Integer> ccm = new EnumMap<scenario, Integer>(scenario.class);
        ccm.put(scenario.b1, 3);
        ccm.put(scenario.a1b, 3);
        ccm.put(scenario.a2, 3);
        runMap.put(gcm.cccma_cgcm3_1, ccm);

        EnumMap<scenario, Integer> cnrm = new EnumMap<scenario, Integer>(scenario.class);
        cnrm.put(scenario.b1, 1);
        cnrm.put(scenario.a1b, 1);
        cnrm.put(scenario.a2, 1);
        runMap.put(gcm.cnrm_cm3, cnrm);

        EnumMap<scenario, Integer> gfdl_cm2_0 = new EnumMap<scenario, Integer>(scenario.class);
        gfdl_cm2_0.put(scenario.b1, 1);
        gfdl_cm2_0.put(scenario.a1b, 1);
        gfdl_cm2_0.put(scenario.a2, 1);
        runMap.put(gcm.gfdl_cm2_0, gfdl_cm2_0);

        EnumMap<scenario, Integer> gfdl_cm2_1 = new EnumMap<scenario, Integer>(scenario.class);
        gfdl_cm2_1.put(scenario.b1, 1);
        gfdl_cm2_1.put(scenario.a1b, 1);
        gfdl_cm2_1.put(scenario.a2, 1);
        runMap.put(gcm.gfdl_cm2_1, gfdl_cm2_1);

        EnumMap<scenario, Integer> ipsl_cm4 = new EnumMap<scenario, Integer>(scenario.class);
        ipsl_cm4.put(scenario.b1, 1);
        ipsl_cm4.put(scenario.a1b, 1);
        ipsl_cm4.put(scenario.a2, 1);
        runMap.put(gcm.ipsl_cm4, ipsl_cm4);

        EnumMap<scenario, Integer> miroc3_2_medres = new EnumMap<scenario, Integer>(scenario.class);
        miroc3_2_medres.put(scenario.b1, 2);
        miroc3_2_medres.put(scenario.a1b, 2);
        miroc3_2_medres.put(scenario.a2, 2);
        runMap.put(gcm.miroc3_2_medres, miroc3_2_medres);

        EnumMap<scenario, Integer> miub_echo_g = new EnumMap<scenario, Integer>(scenario.class);
        miub_echo_g.put(scenario.b1, 3);
        miub_echo_g.put(scenario.a1b, 3);
        miub_echo_g.put(scenario.a2, 3);
        runMap.put(gcm.miub_echo_g, miub_echo_g);

        EnumMap<scenario, Integer> mpi_echam5 = new EnumMap<scenario, Integer>(scenario.class);
        mpi_echam5.put(scenario.b1, 1);
        mpi_echam5.put(scenario.a1b, 0);
        mpi_echam5.put(scenario.a2, 1);
        runMap.put(gcm.mpi_echam5, mpi_echam5);

        EnumMap<scenario, Integer> mri_cgcm2_3_2a = new EnumMap<scenario, Integer>(scenario.class);
        mri_cgcm2_3_2a.put(scenario.b1, 5);
        mri_cgcm2_3_2a.put(scenario.a1b, 5);
        mri_cgcm2_3_2a.put(scenario.a2, 5);
        runMap.put(gcm.mri_cgcm2_3_2a, mri_cgcm2_3_2a);

    }
}

