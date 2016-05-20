/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cru.precip;

import database.DBUtils;
import domain.Cru;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wb385924
 */
public class CruDao {

    private static final String GET_CRU = "select cru_@_year, cru_@_month, cru_@_value from cru_@ where cru_@_area_id = ? and cru_@_year >= ? and cru_@_year <= ?";
    private static final String GET_CRU_BY_YEAR = "select cru_@_year, avg(cru_@_value) as cru_@_value from cru_@ where cru_@_area_id = ? group by cru_@_year";
    private static final String GET_CRU_BY_MONTH = "select cru_@_month, avg(cru_@_value) as cru_@_value from cru_@ where cru_@_area_id = ? group by cru_@_month";
    private static final String GET_CRU_BY_MONTH_RANGED = "select cru_@_month, avg(cru_@_value) as cru_@_value from cru_@ where cru_@_area_id = ? and cru_@_year >= ? and cru_@_year <= ? group by cru_@_month";
    private static final String GET_CRU_BY_DECADE = "select (cru_@_year - ( cru_@_year/10.0  - floor(cru_@_year/10.0))*10 ) as cru_@_year ,avg(cru_@_value) as cru_@_value from cru_@ where cru_@_area_id = ? group by (cru_@_year - ( cru_@_year/10.0  - floor(cru_@_year/10.0))*10 )";
    private static final String INSERT_CRU = "insert into cru_@ (cru_@_area_id, cru_@_year, cru_@_month, cru_@_value) values(?, ?, ?, ?)";
    private static final String CRU_VAL = "cru_@_value";
    private static final String CRU_YEAR = "cru_@_year";
    private static final String CRU_MONTH = "cru_@_month";

    public enum VAR {

        pr, temp
    }

    public enum Aggregation {

        YEAR(GET_CRU_BY_YEAR, CRU_YEAR), DECADE(GET_CRU_BY_DECADE, CRU_YEAR), MONTH(GET_CRU_BY_MONTH, CRU_MONTH);
        private String query = null;
        private String fieldName = null;

        Aggregation(String query, String fieldName) {
            this.query = query;
            this.fieldName = fieldName;
        }
    }
    private static CruDao dao = null;

    public static CruDao get() {
        if (dao == null) {
            dao = new CruDao();
        }
        return dao;
    }

    public List<Cru> getCruAggregation(int areaId, VAR var, Aggregation agg) {

        List<Cru> cruPr = new ArrayList<Cru>();
        Connection c = DBUtils.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = c.prepareStatement(agg.query.replaceAll("\\@", var.toString()));
            ps.setInt(1, areaId);

            rs = ps.executeQuery();
            while (rs.next()) {
                cruPr.add(new Cru(new Double(rs.getDouble(CRU_VAL.replaceAll("\\@", var.toString()))).floatValue(), rs.getInt(agg.fieldName.replaceAll("\\@", var.toString()))));
            }
        } catch (SQLException ex) {
            Logger.getLogger(CruDao.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            DBUtils.close(c, ps, rs);
        }
        Collections.sort(cruPr);
        return cruPr;

    }

    public List<Cru> getCru(int areaId, VAR var, int fromYear, int toYear) {
        List<Cru> cruPr = new ArrayList<Cru>();
        Connection c = DBUtils.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = c.prepareStatement(GET_CRU.replaceAll("\\@", var.toString()));
            ps.setInt(1, areaId);
            ps.setInt(2, fromYear);
            ps.setInt(3, toYear);
            rs = ps.executeQuery();
            while (rs.next()) {
                cruPr.add(new Cru(new Double(rs.getDouble(CRU_VAL.replaceAll("\\@", var.toString()))).floatValue(), rs.getInt(CRU_YEAR.replaceAll("\\@", var.toString())), rs.getInt(CRU_MONTH.replaceAll("\\@", var.toString()))));
            }
        } catch (SQLException ex) {
            Logger.getLogger(CruDao.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            DBUtils.close(c, ps, rs);
        }
        Collections.sort(cruPr);
        return cruPr;

    }

    public List<Cru> getCruMonthAveragesWithYearRange(int areaId, VAR var, int fromYear, int toYear) {
        List<Cru> cruPr = new ArrayList<Cru>();
        Connection c = DBUtils.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = c.prepareStatement(GET_CRU_BY_MONTH_RANGED.replaceAll("\\@", var.toString()));
            ps.setInt(1, areaId);
            ps.setInt(2, fromYear);
            ps.setInt(3, toYear);
            rs = ps.executeQuery();
            while (rs.next()) {

                cruPr.add(new Cru(new Double(rs.getDouble(CRU_VAL.replaceAll("\\@", var.toString()))).floatValue(), rs.getInt(CRU_MONTH.replaceAll("\\@", var.toString()))));
            }
        } catch (SQLException ex) {
            Logger.getLogger(CruDao.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            DBUtils.close(c, ps, rs);
        }
        Collections.sort(cruPr);
        return cruPr;

    }

    public List<Cru> insertCru(int areaId, int year, int month, double value, VAR var) {
        List<Cru> cruPr = new ArrayList<Cru>();
        Connection c = DBUtils.getConnection();
        PreparedStatement ps = null;
        
        try {
            ps = c.prepareStatement(INSERT_CRU.replaceAll("\\@", var.toString()));
            ps.setInt(1, areaId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            ps.setDouble(4, value);

            ps.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(CruDao.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            DBUtils.close(c, ps, null);
        }
        return cruPr;

    }
}

