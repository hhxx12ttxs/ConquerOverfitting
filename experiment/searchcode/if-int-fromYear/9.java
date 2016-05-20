/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package emdat;

import database.DBUtils;
import domain.emdat.EmdatData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author wb385924
 */
public class EmdatDao {

    private static final String INSERT_EMDAT_AREA_DATA = "insert into emdat_area (emdat_area_area_id, emdat_area_year, emdat_area_value, emdat_area_emdat_id ) values( ?, ?, ?, (select emdat_id from emdat where emdat_name = ?))";
    private static final String SELECT_EMDAT_AREA_DATA = "select emdat_area_year, emdat_area_value from emdat inner join emdat_area on emdat_id = emdat_area_emdat_id where emdat_area_area_id = ? and emdat_area_year >= ? and emdat_area_year <= ? and emdat_name = ? order by emdat_area_year";
    private static final String SELECT_ALL_EMDAT_AREA_DATA = "select emdat_name,emdat_area_year, emdat_area_value from emdat inner join emdat_area on emdat_id = emdat_area_emdat_id where emdat_area_area_id = ? and emdat_area_year >= ? and emdat_area_year <= ?  order by emdat_name, emdat_area_year";
    private static final String emdat_area_year = "emdat_area_year";
    private static final String emdat_area_value = "emdat_area_value";
    private static final String emdat_name = "emdat_name";

    public void insertEmdatData(int countryId, int year, double value, Emdat emdatString) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = DBUtils.getConnection();
            ps = con.prepareStatement(INSERT_EMDAT_AREA_DATA);
            ps.setInt(1, countryId);
            ps.setInt(2, year);
            ps.setDouble(3, value);
            ps.setString(4, emdatString.toString());
            ps.executeUpdate();
        } catch (SQLException ioe) {
            ioe.printStackTrace();
        } finally {
            DBUtils.close(con);
        }
    }

    public List<EmdatData> getEmdatData(int countryId, int fromYear, int toYear, Emdat emdatString) {
        Connection con = null;
        PreparedStatement ps = null;
        List<EmdatData> emdata = new ArrayList<EmdatData>();
        try {
            con = DBUtils.getConnection();
            ps = con.prepareStatement(SELECT_EMDAT_AREA_DATA);
            ps.setInt(1, countryId);
            ps.setInt(2, fromYear);
            ps.setInt(3, toYear);
            ps.setString(4, emdatString.toString());
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                emdata.add(new EmdatData(emdatString,rs.getDouble(emdat_area_value), rs.getInt(emdat_area_year)));
            }

        } catch (SQLException ioe) {
            ioe.printStackTrace();
        } finally {
            DBUtils.close(con);
        }

        return emdata;
    }

    public List<EmdatData> getEmdatData(int countryId, int fromYear, int toYear) {
        Connection con = null;
        PreparedStatement ps = null;
        List<EmdatData> emdata = new ArrayList<EmdatData>();
        try {
            con = DBUtils.getConnection();
            ps = con.prepareStatement(SELECT_ALL_EMDAT_AREA_DATA);
            ps.setInt(1, countryId);
            ps.setInt(2, fromYear);
            ps.setInt(3, toYear);
            
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                
                emdata.add(new EmdatData((Emdat.valueOf(rs.getString(emdat_name))), rs.getDouble(emdat_area_value), rs.getInt(emdat_area_year)));
                
            }

        } catch (SQLException ioe) {
            ioe.printStackTrace();
        } finally {
            DBUtils.close(con);
        }

        return emdata;
    }

    private HashMap<Integer,EmdatData> converToMap(List<EmdatData> emdatas){
        HashMap<Integer,EmdatData> data = new HashMap<Integer,EmdatData>();

        for(EmdatData emdat: emdatas){
            data.put(emdat.getYear(), emdat);
        }
        return data;
    }

    private List<EmdatData> combine(HashMap<Integer,EmdatData> em1, HashMap<Integer,EmdatData> em2){
        Set<Integer> keys = em1.keySet();
        List<EmdatData> combinedData = new ArrayList<EmdatData>();
        for(Integer i: keys){
            if(em2.containsKey(i)){
                em1.get(i).setData( em1.get(i).getData() + em2.get(i).getData() );
                combinedData.add(em1.get(i));
            }
        }
        return combinedData;
    }
    public List<EmdatData> getEmdatDataCombined(int countryId, int fromYear, int toYear, Emdat em1, Emdat em2) {
        List<EmdatData> part1 = getEmdatData(countryId, fromYear, toYear, em1);
        List<EmdatData> part2 = getEmdatData(countryId, fromYear, toYear, em2);
        converToMap(part2);
        return combine(converToMap(part1), converToMap(part2));
       

    }
}

