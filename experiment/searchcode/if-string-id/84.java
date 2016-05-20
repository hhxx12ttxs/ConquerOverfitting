package org.worldbank.transport.tamt.server.dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.worldbank.transport.tamt.shared.TagDetails;
import org.worldbank.transport.tamt.shared.TrafficCountRecord;
import org.worldbank.transport.tamt.shared.StudyRegion;
import org.worldbank.transport.tamt.shared.TAMTPoint;
import org.worldbank.transport.tamt.shared.TrafficCountReport;

public class TrafficCountRecordDAO extends DAO {

	static Logger logger = Logger.getLogger(TrafficCountRecordDAO.class);

	private static TrafficCountRecordDAO singleton = null;
	public static TrafficCountRecordDAO get() {
		if (singleton == null) {
			singleton = new TrafficCountRecordDAO();
		}
		return singleton;
	}

	public TrafficCountRecordDAO() {

	}

	public ArrayList<TrafficCountRecord> getTrafficCountRecords(StudyRegion region)
			throws Exception {
		ArrayList<TrafficCountRecord> trafficCountRecordList = new ArrayList<TrafficCountRecord>();
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "SELECT " +
					"t1.id, " +
					"t2.name as tag, " +
					"t1.countdate, " +
					"t1.daytype, " +
					"t1.starttime," +
					"t1.endtime, " +
					"t1.w2, " +
					"t1.w3, " +
					"t1.pc, " +
					"t1.tx, " +
					"t1.ldv, " +
					"t1.ldc, " +
					"t1.hdc, " +
					"t1.mdb, " +
					"t1.hdb " +
					"FROM " +
					"	trafficcount t1, " +
					"	tagdetails t2 " +
					"WHERE " +
					"	t1.regionid = '" + region.getId()+ "' " +
					"AND " +
					"	t1.tagid = t2.id " +
					"ORDER BY t1.countdate ASC, t1.starttime ASC";
			logger.debug("SQL for getTrafficCountRecords: " + sql);
			ResultSet r = s.executeQuery(sql);
			while (r.next()) {
				
				String id = r.getString(1);
				String tag = r.getString(2);
				Date date = r.getDate(3);
				String dayType = r.getString(4);
				Date startTime = r.getTimestamp(5);
				Date endTime = r.getTimestamp(6);
				int W2 = r.getInt(7);
				int W3 = r.getInt(8);
				int PC = r.getInt(9);
				int TX = r.getInt(10);
				int LDV = r.getInt(11);
				int LDC = r.getInt(12);
				int HDC = r.getInt(13);
				int MDB = r.getInt(14);
				int HDB = r.getInt(15);

				TrafficCountRecord trafficCountRecord = new TrafficCountRecord();
				trafficCountRecord.setId(id);
				trafficCountRecord.setDate(date);
				trafficCountRecord.setDayType(dayType);
				trafficCountRecord.setEndTime(endTime);
				trafficCountRecord.setHDB(HDB);
				trafficCountRecord.setHDC(HDC);
				trafficCountRecord.setLDC(LDC);
				trafficCountRecord.setLDV(LDV);
				trafficCountRecord.setMDB(MDB);
				trafficCountRecord.setPC(PC);
				trafficCountRecord.setStartTime(startTime);
				trafficCountRecord.setTag(tag);
				trafficCountRecord.setTX(TX);
				trafficCountRecord.setW2(W2);
				trafficCountRecord.setW3(W3);
				
				trafficCountRecordList.add(trafficCountRecord);
			}

			connection.close(); // returns connection to the pool

		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new Exception("There was an error executing the SQL: "
					+ e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			throw new Exception("Unknown exception: " + e.getMessage());
		}

		return trafficCountRecordList;

	}
	
	private ArrayList<TrafficCountRecord> getTrafficCountReportLong(String tagName, String regionName, String dayType) throws Exception {
		
		// TODO: we changed regionName to regionId for the short report, so if we want to use
		// the long report we'll need to update the stored procedure
		
		ArrayList<TrafficCountRecord> records = new ArrayList<TrafficCountRecord>();
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			
			// get the WEEKDAY counts for this tag
			String sql = "SELECT * FROM trafficCountReport('"+tagName+"', '"+regionName+"', '"+dayType+"')" +
							" AS foo(hour int, w2 bigint, w3 bigint, pc bigint, tx bigint," + 
							" ldv bigint, ldc bigint, hdc bigint, mdb bigint, hdb bigint)";
			
			logger.debug("SQL for getTrafficCountReportLong: " + sql);
			
			ResultSet r = s.executeQuery(sql);
			
			
			// there will always be 24 results from the trafficCountReport function
			while (r.next()) {
				// Piggy-back on the TrafficCount model
				// realizing it is not a real TrafficCount, 
				// but it has the placeholders we like
				TrafficCountRecord record = new TrafficCountRecord();
				int hour = r.getInt(1);
				//logger.debug("hour="+hour);
				int w2 = r.getInt(2);
				//logger.debug("w2="+w2);
				int w3 = r.getInt(3);
				//logger.debug("w3="+w3);
				int pc = r.getInt(4);
				//logger.debug("pc="+pc);
				int tx = r.getInt(5);
				//logger.debug("tx="+tx);
				int ldv = r.getInt(6);
				//logger.debug("ldv="+ldv);
				int ldc = r.getInt(7);
				//logger.debug("ldc="+ldc);
				int hdc = r.getInt(8);
				//logger.debug("hdc="+hdc);
				int mdb = r.getInt(9);
				//logger.debug("mdb="+mdb);
				int hdb = r.getInt(10);
				//logger.debug("hdb="+hdb);
				
				// nowhere to set the hour as int in TrafficCountRecord
				record.setW2(w2);
				record.setW3(w3);
				record.setPC(pc);
				record.setTX(tx);
				record.setLDV(ldv);
				record.setLDC(ldc);
				record.setHDC(hdc);
				record.setMDB(mdb);
				record.setHDB(hdb);
				
				//logger.debug("hour=("+hour+"), record=" + record);
				
				records.add(record);
				
			}
			
		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new Exception("There was an error executing the SQL: "
					+ e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new Exception("Unknown exception: " + e.getMessage());
		}
		return records;
	}
	
	private ArrayList<Integer> getTrafficCountReportShort(String tagName, String regionId, String dayType) throws Exception {
		ArrayList<Integer> totals = new ArrayList<Integer>();
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			
			// get the WEEKDAY counts for this tag
			String sql = "SELECT * FROM TAMT_trafficCountReportShort('"+tagName+"', '"+regionId+"', '"+dayType+"')" +
							" AS foo(hour int, t bigint)";
			
			logger.debug("SQL for getTrafficCountReportShort: " + sql);
			
			ResultSet r = s.executeQuery(sql);
			
			
			// there will always be 24 results from the trafficCountReport function
			while (r.next()) {
				int total = r.getInt(2);
				totals.add(total);
			}
			
		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new Exception("There was an error executing the SQL: "
					+ e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new Exception("Unknown exception: " + e.getMessage());
		}
		return totals;
	}
	
	public TrafficCountReport getTrafficCountReport(TagDetails tagDetails) throws Exception {
		
		// get the name of the tag
		String tagName = tagDetails.getName();
		
		// get the name of the region
		String regionId = tagDetails.getRegion().getId();
		
		TrafficCountReport trafficCountReport = new TrafficCountReport();
		
		/* The LONG version (ie, broken out by vehicle type
		ArrayList<TrafficCountRecord> weekdayCounts = getTrafficCountReportLong(tagName, regionName, TrafficCountRecord.DAYTYPE_WEEKDAY);
		trafficCountReport.setWeekdayCounts(weekdayCounts);
		
		ArrayList<TrafficCountRecord> saturdayCounts = getTrafficCountReportLong(tagName, regionName, TrafficCountRecord.DAYTYPE_SATURDAY);
		trafficCountReport.setSaturdayCounts(saturdayCounts);
		
		ArrayList<TrafficCountRecord> sundayHolidayCounts = getTrafficCountReportLong(tagName, regionName, TrafficCountRecord.DAYTYPE_SUNDAY_HOLIDAY);
		trafficCountReport.setSundayHolidayCounts(sundayHolidayCounts);
		*/
		
		// The short version, where we have totals just by tag/hour of day
		ArrayList<Integer> weekdayTotals = getTrafficCountReportShort(tagName, regionId, TrafficCountRecord.DAYTYPE_WEEKDAY);
		trafficCountReport.setWeekdayTotals(weekdayTotals);
		
		ArrayList<Integer> saturdayTotals = getTrafficCountReportShort(tagName, regionId, TrafficCountRecord.DAYTYPE_SATURDAY);
		trafficCountReport.setSaturdayTotals(saturdayTotals);
		
		ArrayList<Integer> sundayHolidayTotals = getTrafficCountReportShort(tagName, regionId, TrafficCountRecord.DAYTYPE_SUNDAY_HOLIDAY);
		trafficCountReport.setSundayHolidayTotals(sundayHolidayTotals);
		
		return trafficCountReport;
	}

	public TrafficCountRecord getTrafficCountRecordById(String id) throws Exception {
		TrafficCountRecord trafficCountRecord = new TrafficCountRecord();
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "select id, tag, countdate, daytype, starttime," +
			"endtime, w2, w3, pc, tx, ldv, ldc, hdc, mdb, hdb from trafficcount"
			+ "where id = '" + id + "' ORDER BY countdate";
			ResultSet r = s.executeQuery(sql);
			while (r.next()) {
				
				String tag = r.getString(2);
				Date date = r.getDate(3);
				String dayType = r.getString(4);
				Date startTime = r.getDate(5);
				Date endTime = r.getDate(6);
				int W2 = r.getInt(7);
				int W3 = r.getInt(8);
				int PC = r.getInt(9);
				int TX = r.getInt(10);
				int LDV = r.getInt(11);
				int LDC = r.getInt(12);
				int HDC = r.getInt(13);
				int MDB = r.getInt(14);
				int HDB = r.getInt(15);

				trafficCountRecord.setId(id);
				trafficCountRecord.setDate(date);
				trafficCountRecord.setDayType(dayType);
				trafficCountRecord.setEndTime(endTime);
				trafficCountRecord.setHDB(HDB);
				trafficCountRecord.setHDC(HDC);
				trafficCountRecord.setLDC(LDC);
				trafficCountRecord.setLDV(LDV);
				trafficCountRecord.setMDB(MDB);
				trafficCountRecord.setPC(PC);
				trafficCountRecord.setStartTime(startTime);
				trafficCountRecord.setTag(tag);
				trafficCountRecord.setTX(TX);
				trafficCountRecord.setW2(W2);
				trafficCountRecord.setW3(W3);

			}

			connection.close(); // returns connection to pool

		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new Exception("There was an error executing the SQL: "
					+ e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new Exception("Unknown exception: " + e.getMessage());
		}

		return trafficCountRecord;

	}

	public TrafficCountRecord saveTrafficCountRecord(TrafficCountRecord trafficCountRecord) throws SQLException {

		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "INSERT INTO trafficcount (pid, id, regionid, tagid, countdate, "
					+ "daytype, starttime, endtime, w2, w3, pc, tx, ldv, ldc, hdc, "
					+ "mdb, hdb) " + "VALUES ("
					+ "(SELECT nextval('studyregion_pid_seq')),"
					+ "'" 
					+ trafficCountRecord.getId()
					+ "', '"
					+ trafficCountRecord.getRegion()
					+ "', "
					+ "(SELECT id FROM tagdetails WHERE name='" + trafficCountRecord.getTag() + "' AND region = '"+trafficCountRecord.getRegion()+"')"
					+ ", '"
					+ trafficCountRecord.getDate()
					+ "', '"
					+ trafficCountRecord.getDayType()
					+ "', '"
					+ trafficCountRecord.getStartTime()
					+ "', '"
					+ trafficCountRecord.getEndTime()
					+ "', '"
					+ trafficCountRecord.getW2()
					+ "', '"
					+ trafficCountRecord.getW3()
					+ "', '"
					+ trafficCountRecord.getPC()
					+ "', '"
					+ trafficCountRecord.getTX()
					+ "', '"
					+ trafficCountRecord.getLDV()
					+ "', '"
					+ trafficCountRecord.getLDC()
					+ "', '"					
					+ trafficCountRecord.getHDC()
					+ "', '"
					+ trafficCountRecord.getMDB()
					+ "', '"
					+ trafficCountRecord.getHDB()
					+ "')";
			logger.debug("sql=" + sql);
			s.executeUpdate(sql);

			connection.close(); // returns connection to pool
		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw e;
		}

		return trafficCountRecord;

	}

	public TrafficCountRecord updateTrafficCountRecord(TrafficCountRecord trafficCountRecord) throws SQLException {
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "UPDATE trafficcount SET " +
					" tagid = (SELECT id FROM tagdetails WHERE name='"+trafficCountRecord.getTag()+"'), " +
					" countdate = '"+trafficCountRecord.getDate()+"'," +
					" daytype = '"+trafficCountRecord.getDayType()+"'," +
					" starttime = '"+trafficCountRecord.getStartTime()+"'," +
					" endtime = '"+trafficCountRecord.getEndTime()+"'," +
					" w2 = '"+trafficCountRecord.getW2()+"'," +
					" w3 = '"+trafficCountRecord.getW3()+"'," +
					" pc = '"+trafficCountRecord.getPC()+"'," +
					" tx = '"+trafficCountRecord.getTX()+"'," +
					" ldv = '"+trafficCountRecord.getLDV()+"'," +
					" ldc = '"+trafficCountRecord.getLDC()+"'," +
					" hdc = '"+trafficCountRecord.getHDC()+"', " +
					" mdb = '"+trafficCountRecord.getMDB()+"', " +
					" hdb = '"+trafficCountRecord.getHDB()+"' " +
					"WHERE id = '" + trafficCountRecord.getId() + "'";
			logger.debug("UPDATE TrafficCountRecord SQL=" + sql);
			s.executeUpdate(sql);
			connection.close(); // returns connection to pool

		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw e;
		}

		return trafficCountRecord;
	}

	public void deleteTrafficCountRecords(ArrayList<String> trafficCountRecordIds)
			throws SQLException {
		for (Iterator<String> iterator = trafficCountRecordIds.iterator(); iterator
				.hasNext();) {
			String id = (String) iterator.next();
			deleteTrafficCountRecordById(id);
		}
	}

	public void deleteTrafficCountRecordById(String id) throws SQLException {
		try {

			Connection connection = getConnection();
			Statement s = connection.createStatement();

			String sql = "DELETE FROM trafficcount WHERE id = '" + id + "'";
			logger.debug("delete TrafficCountRecord sql=" + sql);
			s.execute(sql);

			connection.close(); // returns connection to pool

		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	

}

