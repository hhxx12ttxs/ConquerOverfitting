/*
 * Copyright (c) CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.bp.pensionline.webstats;

import com.bp.pensionline.constants.Environment;
import com.bp.pensionline.database.DBConnector;
import com.bp.pensionline.test.XmlReader;
import com.bp.pensionline.util.Mapper;
import com.bp.pensionline.util.Timer;

import org.apache.commons.logging.Log;

import org.opencms.main.CmsLog;

import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Please enter a short description for this class.
 *
 * <p>Optionally, enter a longer description.</p>
 *
 * @author   Tu Nguyen
 * @author   Binh Nguyen
 * @version  1.1
 * @since    25/06/2007
 */
/**
 * Please enter a short description for this class.
 *
 * <p>Optionally, enter a longer description.</p>
 *
 * @author   SonNT
 * @author   Tu Nguyen
 * @version  2.0
 * @version  3.0
 * @since    27/06/2007
 */
public class WebstatsSQLHandler {

    //~ Static fields/initializers ---------------------------------------------

    /** DOCUMENT ME! */
    public static final Log LOG = CmsLog.getLog(
            org.opencms.jsp.CmsJspLoginBean.class);

    /** Declare sql query insert into BP_STATS_AUTH table. */
    private static String sqlInsertStatsAuth = "Insert into BP_STATS_AUTH "
        + "(session_id, event_time, event_time_ms, event_year, event_month, event_day, "
        + "event_hour, event_minute, event_second, browser_string, javascript_on, browser_type, "
        + "browser_name, browser_version, browser_os, outcome, usertype, unauth_seqno, duration) values( "
        + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /** Declare sql query insert into BP_STATS_UNAUTH table. */
    private static String sqlInsertStatsUnauth = "Insert into BP_STATS_UNAUTH "
        + "(session_id, event_time, event_time_ms, event_year, event_month, event_day, "
        + "event_hour, event_minute, event_second, browser_string, javascript_on, browser_type, "
        + "browser_name, browser_version, browser_os, duration) values( "
        + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /** Declare sql query insert into BP_STATS_POS table. */
    private static String sqlInsertStatsPos = "Insert into BP_STATS_POS "
        + "(session_id, event_time, event_time_ms, event_year, event_month, event_day, "
        + "event_hour, event_minute, event_second, auth_seqno, company, member_scheme, member_status, "
        + "gender, marital_status, age_range, salary_range, location, overseas, duration) values( "
        + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /** Declare sql query insert into BP_STATS_LOGOUT table. */
    private static String sqlInsertStatsLogout = "Insert into BP_STATS_LOGOUT "
        + "(session_id, event_time, event_time_ms, event_year, event_month, event_day, "
        + "event_hour, event_minute, event_second, auth_seqno, pos_seqno) values( "
        + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /** Declare sql query insert into BP_STATS_SEARCH table. */
    private static String sqlInsertStatsSearch = "Insert into BP_STATS_SEARCH "
        + "(session_id, event_time, event_time_ms, event_year, event_month, event_day, "
        + "event_hour, event_minute, event_second, auth_seqno, pos_seqno, search_string, result_count) values( "
        + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /** Declare sql query insert into BP_STATS_ACCESS table. */
    private static String sqlInsertStatsAccess = "Insert into BP_STATS_ACCESS "
        + "(session_id, event_time, event_time_ms, event_year, event_month, event_day, "
        + "event_hour, event_minute, event_second, auth_seqno, unauth_seqno, pos_seqno, search_seqno, "
        + "content_group, uri, referrer, page_size, duration) values( "
        + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    /** Insert calc statistic */
    private static String sqlInsertCalcLog = "Insert into BP_STATS_CALCS "
        + "(calc_date, calc_source, refno, bgroup, calc_type, DoR, accrual, cash, " +
        		"pension, precap_pension, unreduced_pension, reduced_pension, spouses_pension, " +
        		"cash_lump_sum, max_cash_lump_sum, pension_with_chosen_cash, pension_with_max_cash, vera_indicator, overfund_indicator, erf, com_factor) "  +
        		" values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static String MEMBER_ADDRESS_FILE = Environment.MEMBERMAPPERFILE_DIR + "ALL/address.sql";

    //~ Methods ----------------------------------------------------------------

    /**
     * Insert data to MySql Database - BP_STATS_AUTH.
     *
     * @param   map
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception
     */
    public static void insertCalcStats(Date calcDate, String calcSource, String refno, String bGroup, String calcType, Date DoR, int accrual, double cash, 
    		double pension, double preCapPostReductionPension, double unreducedPension, double reducedPension, 
    		double spousesPension, double cashLumpSum, double maxCashLumpSum, double pensionWithChosenCash,
    		double pensionWithMaxCash, boolean veraIndicator, boolean overfundIndicator, double erf, double comFactor) throws Exception {
        Connection con = null;
        
//        LOG.info("calcDate: " + calcDate);
//        LOG.info("refno: " + refno);
//        LOG.info("bGroup: " + bGroup);
//        LOG.info("calcType: " + calcType);
//        LOG.info("DoR: " + DoR);
        if (calcDate == null || refno == null || bGroup == null || calcType == null || DoR == null)
        {
        	LOG.error("Insert Calcs statistic return because of NULL input(s)!");
        	return;
        }

        try {
            // set up the connection to Mysql database
            DBConnector connector = DBConnector.getInstance();
            con = connector.getDBConnFactory(Environment.WEBSTATS);

            // con = connector.getDBConnFactory(Environment.WEBSTATS);
            con.setAutoCommit(false);
            PreparedStatement pstm = con.prepareStatement(sqlInsertCalcLog);

            // set all the parameters into the insert query
            /** TODO complete the rest of setting parameters. Be aware of the
             * type */
            pstm.setTimestamp(1, new java.sql.Timestamp(calcDate.getTime()));
            pstm.setString(2, calcSource);
            pstm.setString(3, refno);
            pstm.setString(4, bGroup);
            pstm.setString(5, calcType);
            pstm.setDate(6, DoR);
            pstm.setInt(7, accrual);
            pstm.setDouble(8, cash);
            pstm.setDouble(9, pension);
            pstm.setDouble(10, preCapPostReductionPension);
            pstm.setDouble(11, unreducedPension);
            pstm.setDouble(12, reducedPension);
            pstm.setDouble(13, spousesPension);
            pstm.setDouble(14, cashLumpSum);
            pstm.setDouble(15, maxCashLumpSum);
            pstm.setDouble(16, pensionWithChosenCash);
            pstm.setDouble(17, pensionWithMaxCash);
            pstm.setInt(18, veraIndicator ? 1 : 0);
            pstm.setInt(19, overfundIndicator ? 1 : 0);
            pstm.setDouble(20, erf);
            pstm.setDouble(21, comFactor);
            
            pstm.execute();
            con.commit();
            con.setAutoCommit(true);
        } catch (Exception e) {
            LOG.error("Error while insert CALC log for: " + refno + "-" + bGroup + ". Exception: " + e.toString());
            try {
                con.rollback();
            } catch (Exception ex) {
                // TODO: handle exception
                throw ex;
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    DBConnector connector = DBConnector.getInstance();
                    connector.close(con);
                } catch (Exception e) {
                    // TODO: handle exception
                    LOG.error(e.getMessage() + e.getCause());
                }
            }
        }
    }    
    
    /**
     * Insert data to MySql Database - BP_STATS_AUTH.
     *
     * @param   map
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception
     */
    public static long insertStatsAuth(Mapper map) throws Exception {
        Connection con = null;
        long seqno = 0;

        /** get the String from the HashMap to insert into the database
         * TODO complete pulling out information from HashMap
         */
        String sessionId = (String) map.get(WebstatsConstants.SESSIONID);
        String browserString = (String) map.get(
                WebstatsConstants.BROWSER_STRING);
        Boolean jsOnCheck = (Boolean) (map.get(WebstatsConstants.JAVASCRIPT_ON));
        boolean javascriptOn = false;
        if (jsOnCheck != null) {
            javascriptOn = jsOnCheck.booleanValue();
        }
        String browserType = (String) map.get(WebstatsConstants.BROWSER_TYPE);
        String browserName = (String) map.get(WebstatsConstants.BROWSER_NAME);
        String browserVer = (String) map.get(WebstatsConstants.BROWSER_VERSION);
        String browserOs = (String) map.get(WebstatsConstants.BROWSER_OS);
        String outcome = (String) map.get(WebstatsConstants.OUT_COME);
        String userType = (String) map.get(WebstatsConstants.USER_TYPE);
        long unauthNo = -1;
        Long unauthNoLong = (Long) map.get(WebstatsConstants.UNAUTH_SEQNO);
        if (unauthNoLong != null) {
            unauthNo = unauthNoLong.longValue();
        }
        try {
            // set up the connection to Mysql database
            DBConnector connector = DBConnector.getInstance();
            con = connector.getDBConnFactory(Environment.WEBSTATS);

            // con = connector.getDBConnFactory(Environment.WEBSTATS);
            con.setAutoCommit(false);
            PreparedStatement pstm = con.prepareStatement(sqlInsertStatsAuth);

            // set all the parameters into the insert query
            /** TODO complete the rest of setting parameters. Be aware of the
             * type */
            pstm.setString(1, sessionId);
            pstm.setBigDecimal(2, Timer.getTime());
            pstm.setBigDecimal(3, Timer.getTimeMs());
            pstm.setInt(4, Timer.getYear());
            pstm.setInt(5, Timer.getMonth());
            pstm.setInt(6, Timer.getDay());
            pstm.setInt(7, Timer.getHour());
            pstm.setInt(8, Timer.getMin());
            pstm.setInt(9, Timer.getSec());
            if (browserString != null && browserString.length() > 255)
            {
            	browserString = browserString.substring(0, 255);
            }
            pstm.setString(10, browserString);
            
            pstm.setBoolean(11, javascriptOn);
            if (browserType != null && browserType.length() > 10)
            {
            	browserType = browserType.substring(0, 10);
            }            
            pstm.setString(12, browserType);
            if (browserName != null && browserName.length() > 45)
            {
            	browserType = browserType.substring(0, 45);
            }
            pstm.setString(13, browserName);
            if (browserVer != null && browserVer.length() > 45)
            {
            	browserVer = browserVer.substring(0, 45);
            }            
            pstm.setString(14, browserVer);
            if (browserOs != null && browserOs.length() > 45)
            {
            	browserOs = browserOs.substring(0, 45);
            }               
            pstm.setString(15, browserOs);
            pstm.setString(16, outcome);
            pstm.setString(17, userType);
            pstm.setLong(18, unauthNo);
            pstm.setLong(19, 0);
            pstm.execute();
            con.commit();
            con.setAutoCommit(true);
        } catch (Exception e) {
            LOG.error(e.getMessage() + e.getCause());
            try {
                con.rollback();
            } catch (Exception ex) {
                // TODO: handle exception
                throw ex;
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    DBConnector connector = DBConnector.getInstance();
                    connector.close(con);
                } catch (Exception e) {
                    // TODO: handle exception
                    LOG.error(e.getMessage() + e.getCause());
                }
            }
        }
        seqno = getMaxSeqno(WebstatsConstants.BP_STATS_AUTH);

        return seqno;
    }

    /**
     * Insert data to MySql Database - BP_STATS_UNAUTH.
     *
     * @param   map
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception
     */
    public static long insertStatsUnauth(Mapper map) throws Exception {
        Connection con = null;
        long seqno = 0;

        /** get the String from the HashMap to insert into the database
         * TODO complete pulling out information from HashMap
         */
        String sessionId = (String) map.get(WebstatsConstants.SESSIONID);
        String browserString = (String) map.get(
                WebstatsConstants.BROWSER_STRING);
        Boolean jsOnCheck = (Boolean) (map.get(WebstatsConstants.JAVASCRIPT_ON));
        boolean javascriptOn = false;
        if (jsOnCheck != null) {
            javascriptOn = jsOnCheck.booleanValue();
        }
        String browserType = (String) map.get(WebstatsConstants.BROWSER_TYPE);
        String browserName = (String) map.get(WebstatsConstants.BROWSER_NAME);
        String browserVer = (String) map.get(WebstatsConstants.BROWSER_VERSION);
        String browserOs = (String) map.get(WebstatsConstants.BROWSER_OS);
        try {
            // set up the connection to Mysql database
            DBConnector connector = DBConnector.getInstance();
            con = connector.getDBConnFactory(Environment.WEBSTATS);

            // con = connector.getDBConnFactory(Environment.WEBSTATS);
            con.setAutoCommit(false);
            PreparedStatement pstm = con.prepareStatement(sqlInsertStatsUnauth);

            // set all the parameters into the insert query
            /** TODO complete the rest of setting parameters. Be aware of the
             * type */
            pstm.setString(1, sessionId);
            pstm.setBigDecimal(2, Timer.getTime());
            pstm.setBigDecimal(3, Timer.getTimeMs());
            pstm.setInt(4, Timer.getYear());
            pstm.setInt(5, Timer.getMonth());
            pstm.setInt(6, Timer.getDay());
            pstm.setInt(7, Timer.getHour());
            pstm.setInt(8, Timer.getMin());
            pstm.setInt(9, Timer.getSec());
            pstm.setString(10, browserString);
            pstm.setBoolean(11, javascriptOn);
            pstm.setString(12, browserType);
            pstm.setString(13, browserName);
            pstm.setString(14, browserVer);
            pstm.setString(15, browserOs);
            pstm.setLong(16, 0);

            // pstm.execute();
            // Change to executeUpdate()
            pstm.executeUpdate();
            con.commit();
            con.setAutoCommit(true);
        } catch (Exception e) {
            LOG.error("Error: " + e.getMessage() + "; Browser type: "
                + browserType);
            try {
                con.rollback();
            } catch (Exception ex) {
                // TODO: handle exception
                LOG.error("Error: " + ex.toString());
                throw ex;
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    DBConnector connector = DBConnector.getInstance();
                    connector.close(con);
                } catch (Exception e) {
                    // TODO: handle exception
                    LOG.error("Error: " + e.getMessage() + e.getCause());
                }
            }
        }
        seqno = getMaxSeqno(WebstatsConstants.BP_STATS_UNAUTH);

        return seqno;
    }

    /**
     * Insert data to MySql Database - BP_STATS_POS.
     *
     * @param   map
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception
     */
    public static long insertStatsPos(Mapper map) throws Exception {
        Connection con = null;
        long seqno = 0;

        /** get the String from the HashMap to insert into the database
         * TODO complete pulling out information from HashMap
         */
        String sessionId = (String) map.get(WebstatsConstants.SESSIONID);
        long authNo = -1;
        Long authNoLong = (Long) map.get(WebstatsConstants.AUTH_SEQNO);
        if (authNoLong != null) {
            authNo = authNoLong.longValue();
        }
        String company = (String) map.get(WebstatsConstants.COMPANY);
        String memScheme = (String) map.get(WebstatsConstants.MEMBER_SCHEME);
        String memStatus = (String) map.get(WebstatsConstants.MEMBER_STATUS);
        String gender = (String) map.get(WebstatsConstants.GENDER);

        // checked by Huy. Make sure data truncation warning does not occured
        if ((gender == null) || (gender.trim().length() == 0)) {
            gender = "Unknown";
        }
        String marital = (String) map.get(WebstatsConstants.MARITAL_STATUS);

        // checked by Huy. Make sure data truncation warning does not occured
        if ((marital == null) || (marital.trim().length() == 0)) {
            marital = "Other";
        }
        String age = (String) map.get(WebstatsConstants.AGE_RANGE);
        String salary = (String) map.get(WebstatsConstants.SALARY);
        String location = (String) map.get(WebstatsConstants.LOCATION);
        boolean overseas = false;
        Boolean overseasBoolean = (Boolean) map.get(WebstatsConstants.OVERSEAS);
        if (overseasBoolean != null) {
            overseas = overseasBoolean.booleanValue();
        }
        try {
            // set up the connection to Mysql database
            DBConnector connector = DBConnector.getInstance();
            con = connector.getDBConnFactory(Environment.WEBSTATS);

            // con = connector.getDBConnFactory(Environment.WEBSTATS);
            con.setAutoCommit(false);
            PreparedStatement pstm = con.prepareStatement(sqlInsertStatsPos);

            // set all the parameters into the insert query
            /** TODO complete the rest of setting parameters. Be aware of the
             * type */
            pstm.setString(1, sessionId);
            pstm.setBigDecimal(2, Timer.getTime());
            pstm.setBigDecimal(3, Timer.getTimeMs());
            pstm.setInt(4, Timer.getYear());
            pstm.setInt(5, Timer.getMonth());
            pstm.setInt(6, Timer.getDay());
            pstm.setInt(7, Timer.getHour());
            pstm.setInt(8, Timer.getMin());
            pstm.setInt(9, Timer.getSec());
            pstm.setLong(10, authNo);
            pstm.setString(11, company);
            pstm.setString(12, memScheme);
            pstm.setString(13, memStatus);
            pstm.setString(14, gender);
            pstm.setString(15, marital);
            pstm.setString(16, age);
            pstm.setString(17, salary);
            pstm.setString(18, location);
            pstm.setBoolean(19, overseas);
            pstm.setLong(20, 0);
            pstm.execute();
            con.commit();
            con.setAutoCommit(true);
        } catch (Exception e) {
            LOG.error(e.getMessage() + e.getCause());
            try {
                con.rollback();
            } catch (Exception ex) {
                // TODO: handle exception
                throw ex;
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    DBConnector connector = DBConnector.getInstance();
                    connector.close(con);
                } catch (Exception e) {
                    // TODO: handle exception
                    LOG.error(e.getMessage() + e.getCause());
                }
            }
        }
        seqno = getMaxSeqno(WebstatsConstants.BP_STATS_POS);

        return seqno;
    }

    /**
     * Insert data to MySql Database - BP_STATS_LOGOUT.
     *
     * @param   map
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception
     */
    public static long insertStatsLogout(Mapper map) throws Exception {
        Connection con = null;
        long seqno = 0;

        /** get the String from the HashMap to insert into the database
         * TODO complete pulling out information from HashMap
         */
        String sessionId = (String) map.get(WebstatsConstants.SESSIONID);
        long authNo = -1;
        Long authNoLong = (Long) map.get(WebstatsConstants.AUTH_SEQNO);
        if (authNoLong != null) {
            authNo = authNoLong.longValue();
        }
        long posNo = -1;
        Long posNoLong = (Long) map.get(WebstatsConstants.POS_SEQNO);
        if (posNoLong != null) {
            posNo = posNoLong.longValue();
        }
        try {
            // set up the connection to Mysql database
            DBConnector connector = DBConnector.getInstance();
            con = connector.getDBConnFactory(Environment.WEBSTATS);

            // con = connector.getDBConnFactory(Environment.WEBSTATS);
            con.setAutoCommit(false);
            PreparedStatement pstm = con.prepareStatement(sqlInsertStatsLogout);

            // set all the parameters into the insert query
            /** TODO complete the rest of setting parameters. Be aware of the
             * type */
            pstm.setString(1, sessionId);
            pstm.setBigDecimal(2, Timer.getTime());
            pstm.setBigDecimal(3, Timer.getTimeMs());
            pstm.setInt(4, Timer.getYear());
            pstm.setInt(5, Timer.getMonth());
            pstm.setInt(6, Timer.getDay());
            pstm.setInt(7, Timer.getHour());
            pstm.setInt(8, Timer.getMin());
            pstm.setInt(9, Timer.getSec());
            pstm.setLong(10, authNo);
            pstm.setLong(11, posNo);
            pstm.execute();
            con.commit();
            con.setAutoCommit(true);
        } catch (Exception e) {
            LOG.error(e.getMessage() + e.getCause());
            try {
                con.rollback();
            } catch (Exception ex) {
                // TODO: handle exception
                throw ex;
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    DBConnector connector = DBConnector.getInstance();
                    connector.close(con);
                } catch (Exception ex) {
                    ;
                }
            }
        }
        seqno = getMaxSeqno(WebstatsConstants.BP_STATS_LOGOUT);

        return seqno;
    }

    /**
     * Insert data to MySql Database - BP_STATS_SEARCH.
     *
     * @param   map
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception
     */
    public static long insertStatsSearch(Mapper map) throws Exception {
        Connection con = null;
        long seqno = 0;

        /** get the String from the HashMap to insert into the database
         * TODO complete pulling out information from HashMap
         */
        String sessionId = (String) map.get(WebstatsConstants.SESSIONID);
        long authNo = -1;
        Long authNoLong = (Long) map.get(WebstatsConstants.AUTH_SEQNO);
        if (authNoLong != null) {
            authNo = authNoLong.longValue();
        }
        long posNo = -1;
        Long posNoLong = (Long) map.get(WebstatsConstants.POS_SEQNO);
        if (posNoLong != null) {
            posNo = posNoLong.longValue();
        }
        String searchString = (String) map.get(WebstatsConstants.SEARCH_STRING);
        int resultCount = -1;
        Integer resultCountInteger = (Integer) map.get(
                WebstatsConstants.RESULT_COUNT);
        if (resultCountInteger != null) {
            resultCount = resultCountInteger.intValue();
        }
        try {
            // set up the connection to Mysql database
            DBConnector connector = DBConnector.getInstance();
            con = connector.getDBConnFactory(Environment.WEBSTATS);

            // con = connector.getDBConnFactory(Environment.WEBSTATS);
            con.setAutoCommit(false);
            PreparedStatement pstm = con.prepareStatement(sqlInsertStatsSearch);

            // set all the parameters into the insert query
            /** TODO complete the rest of setting parameters. Be aware of the
             * type */
            pstm.setString(1, sessionId);
            pstm.setBigDecimal(2, Timer.getTime());
            pstm.setBigDecimal(3, Timer.getTimeMs());
            pstm.setInt(4, Timer.getYear());
            pstm.setInt(5, Timer.getMonth());
            pstm.setInt(6, Timer.getDay());
            pstm.setInt(7, Timer.getHour());
            pstm.setInt(8, Timer.getMin());
            pstm.setInt(9, Timer.getSec());
            pstm.setLong(10, authNo);
            pstm.setLong(11, posNo);
            pstm.setString(12, searchString);
            pstm.setInt(13, resultCount);
            pstm.execute();
            con.commit();
            con.setAutoCommit(true);
        } catch (Exception e) {
            LOG.error(e.getMessage() + e.getCause());
            try {
                con.rollback();
            } catch (Exception ex) {
                // TODO: handle exception
                throw ex;
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    DBConnector connector = DBConnector.getInstance();
                    connector.close(con);
                } catch (Exception ex) {
                    ;
                }
            }
        }
        seqno = getMaxSeqno(WebstatsConstants.BP_STATS_SEARCH);

        return seqno;
    }

    /**
     * Insert data to MySql Database - BP_STATS_ACCESS.
     *
     * @param   map
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception
     */
    public static long insertStatsAccess(Mapper map) throws Exception {
        Connection con = null;
        long seqno = 0;

        /** get the String from the HashMap to insert into the database
         * TODO complete pulling out information from HashMap
         */
        String sessionId = (String) map.get(WebstatsConstants.SESSIONID);
        long authNo = -1;
        Long authNoLong = (Long) map.get(WebstatsConstants.AUTH_SEQNO);
        if (authNoLong != null) {
            authNo = authNoLong.longValue();
        }
        long posNo = -1;
        Long posNoLong = (Long) map.get(WebstatsConstants.POS_SEQNO);
        if (posNoLong != null) {
            posNo = posNoLong.longValue();
        }
        long unauthNo = -1;
        Long unauthNoLong = (Long) map.get(WebstatsConstants.UNAUTH_SEQNO);
        if (unauthNoLong != null) {
            unauthNo = unauthNoLong.longValue();
        }
        long searchNo = -1;
        Long searchNoLong = (Long) map.get(WebstatsConstants.SEARCH_SEQNO);
        if (unauthNoLong != null) {
            searchNo = searchNoLong.longValue();
        }
        String contentGroup = (String) map.get(WebstatsConstants.CONTENT_GROUP);
        String uri = (String) map.get(WebstatsConstants.URI);
        String referrer = (String) map.get(WebstatsConstants.REFERRER);

        // added by Huy Data truncation: Data too long for column 'referrer'
        // at row 1: Max length 225
        if ((referrer != null) && (referrer.length() > 225)) {
            referrer = referrer.substring(0, 225);
        }
        long pageSize = -1;
        Long pageSizeLong = (Long) map.get(WebstatsConstants.PAGE_SIZE);
        if (pageSizeLong != null) {
            pageSize = pageSizeLong.longValue();
        }
        try {
            // set up the connection to Mysql database
            DBConnector connector = DBConnector.getInstance();
            con = connector.getDBConnFactory(Environment.WEBSTATS);

            // con = connector.getDBConnFactory(Environment.WEBSTATS);
            con.setAutoCommit(false);
            PreparedStatement pstm = con.prepareStatement(sqlInsertStatsAccess);

            // set all the parameters into the insert query
            /** TODO complete the rest of setting parameters. Be aware of the
             * type */
            pstm.setString(1, sessionId);
            pstm.setBigDecimal(2, Timer.getTime());
            pstm.setBigDecimal(3, Timer.getTimeMs());
            pstm.setInt(4, Timer.getYear());
            pstm.setInt(5, Timer.getMonth());
            pstm.setInt(6, Timer.getDay());
            pstm.setInt(7, Timer.getHour());
            pstm.setInt(8, Timer.getMin());
            pstm.setInt(9, Timer.getSec());
            pstm.setLong(10, authNo);
            pstm.setLong(11, unauthNo);
            pstm.setLong(12, posNo);
            pstm.setLong(13, searchNo);
            pstm.setString(14, contentGroup);
            pstm.setString(15, uri);
            pstm.setString(16, referrer);
            pstm.setLong(17, pageSize);
            pstm.setLong(18, 0);
            pstm.execute();
            con.commit();
            con.setAutoCommit(true);
        } catch (Exception e) {
            LOG.error(e.getMessage() + e.getCause());
            try {
                con.rollback();
            } catch (Exception ex) {
                // TODO: handle exception
                throw ex;
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    DBConnector connector = DBConnector.getInstance();
                    connector.close(con);
                } catch (Exception ex) {
                    ;
                }
            }
        }
        seqno = getMaxSeqno(WebstatsConstants.BP_STATS_ACCESS);

        // System.out.println("Finished insertStatsAccess");
        return seqno;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   tableName
     * @param   pageNo
     *
     * @throws  Exception
     */
    public static void updateDuration(String tableName, long pageNo)
        throws Exception {
        Connection con = null;

        /** get the String from the HashMap to insert into the database
         * TODO complete pulling out information from HashMap
         */
        //String selectQuery = "select EVENT_TIME_MS from " + tableName
        String selectQuery = "select event_time_ms from " + tableName
            + " where seqno = ?";
        String updateQuery = "update " + tableName
            + " set duration = ? where seqno = ?";
        try {
            // set up the connection to Mysql database
            DBConnector connector = DBConnector.getInstance();
            con = connector.getDBConnFactory(Environment.WEBSTATS);

            // con = connector.getDBConnFactory(Environment.WEBSTATS);
            con.setAutoCommit(false);
            PreparedStatement pstm = con.prepareStatement(selectQuery);

            // set all the parameters into the insert query
            /** TODO complete the rest of setting parameters. Be aware of the
             * type */
            pstm.setLong(1, pageNo);
            BigDecimal startTime = BigDecimal.valueOf(0);
            BigDecimal duration = BigDecimal.valueOf(0);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                startTime = rs.getBigDecimal(1);
            }
            duration = Timer.getTimeMs().subtract(startTime);
            pstm = con.prepareStatement(updateQuery);
            pstm.setBigDecimal(1, duration);
            pstm.setLong(2, pageNo);
            pstm.execute();
            con.commit();
            con.setAutoCommit(true);
        } catch (Exception e) {
            LOG.error(e.getMessage() + e.getCause());
            try {
                con.rollback();
            } catch (Exception ex) {
                // TODO: handle exception
                throw ex;
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    DBConnector connector = DBConnector.getInstance();
                    connector.close(con);
                } catch (Exception ex) {
                    ;
                }
            }
        }
    }

    /**
     * @param   tableName
     *
     * @return
     */
    private static long getMaxSeqno(String tableName) {
        Connection con = null;
        //String query = "select max(SEQNO) AS CASENO FROM " + tableName
        String query = "select max(seqno) AS CASENO FROM " + tableName
            			+ " where session_id is not null";
        try {
            long caseCode = 0;
            DBConnector connector = DBConnector.getInstance();
            con = connector.getDBConnFactory(Environment.WEBSTATS);

            // con = connector.getDBConnFactory(Environment.WEBSTATS);
            Statement stm = con.createStatement();
            ResultSet rs;
            rs = stm.executeQuery(query);
            while (rs.next()) {
                caseCode = rs.getLong("CASENO");
            }

            return caseCode;
        } catch (Exception e) {
            // TODO: handle exception
            LOG.info("Error: " + e.getMessage() + e.getCause());

            return 0;
        } finally {
            if (con != null) {
                try {
                    DBConnector connector = DBConnector.getInstance();
                    connector.close(con);
                } catch (Exception ex) {
                    LOG.info("Error: " + ex.getMessage() + ex.getCause());
                }
            }
        }
    }
    
    public static Map<String, String> getMemberAddress (String bgroup, String refno)
    {
    	Map<String, String> valueMap = new HashMap<String, String>();
    	
    	Connection conn = getAquilaConnection();
    	if (conn != null)
    	{
	    	try {    			
	    		XmlReader xReader=new XmlReader();
				String SQLQuery=new String(xReader.readFile(MEMBER_ADDRESS_FILE));
				
				//PreparedStatement pstm = conn.prepareStatement(SQLQuery);	
		    	
		    	/**Get the array contain the location in which preparedStatement 
		    	 * must replace with the value
		    	 * if result[i]=bgroup, must set query at location i, value of Bgroup
		    	 * if result[i]=refno, must set query at location i, value of Refno
		    	 */
				
				SQLQuery = SQLQuery.trim();
				// get the last char of the sql query and compare with ";"
				char lastChar = SQLQuery.charAt(SQLQuery.length()-1);
				if (lastChar == ';'){
					SQLQuery = SQLQuery.substring(0, SQLQuery.length()-1);
				}
				//SQLQuery.substring(0, endIndex)''
				
				SQLQuery = SQLQuery.replaceAll(":bgroup", "'"+bgroup+"'");
				SQLQuery = SQLQuery.replaceAll(":refno", "'"+refno+"'");
				SQLQuery = SQLQuery.replaceAll(":BGROUP", "'"+bgroup+"'");
				SQLQuery = SQLQuery.replaceAll(":REFNO", "'"+refno+"'");
				SQLQuery = SQLQuery.replaceAll(":Bgroup", "'"+bgroup+"'");
				SQLQuery = SQLQuery.replaceAll(":Refno", "'"+refno+"'");
				SQLQuery = SQLQuery.replaceAll(":BGroup", "'"+bgroup+"'");
				SQLQuery = SQLQuery.replaceAll(":RefNo", "'"+refno+"'");
	
				ResultSet rs = conn.createStatement().executeQuery(SQLQuery);
				/*
				PreparedStatement pstm = conn.prepareStatement(SQLQuery);
		    	pstm.execute();				
		    	rs = pstm.executeQuery();
		    	*/
		    	ResultSetMetaData rsmd=rs.getMetaData();																		
		    	//Get total number of column
		    	int colsCount=rsmd.getColumnCount();								
		    	
		    	//Assign the value to valueMap
		    	while(rs.next()){
		    		for(int j=1;j<=colsCount;j++)
		    			valueMap.put(rsmd.getColumnName(j), rs.getString(j));
		    	}
			} 							
			catch (Exception e)
			{
		    			// TODO Auto-generated catch block
				LOG.error("Error while getting member address for webstatistic: " + e.toString());		    			
			}
			finally
			{
				try 
				{
					if (conn != null) conn.close();//con.close();
				} 
				catch (Exception ce) 
				{
					LOG.error("AATAX - Error while closing aquila connection in loading member: " + ce.toString());
				}
			}			
    	}
    	
    	return valueMap;
    	
    }
    
	/**
	 * get Connection
	 * 
	 * @return conn
	 * 
	 */
	private static Connection getAquilaConnection() {
		/*
		 * Create new conn
		 */
		Connection conn = null;
		try {

			DBConnector dbConn = DBConnector.getInstance();//new DBConnector();
			conn = dbConn.getDBConnFactory(Environment.AQUILA);

		} catch (Exception ex) {
			LOG.error("DBMemberConnector", ex);
		}
		return conn;
	}    
}

