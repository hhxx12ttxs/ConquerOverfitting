package org.worldbank.transport.tamt.server.dao;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.worldbank.transport.tamt.shared.DefaultFlow;
import org.worldbank.transport.tamt.shared.TagDetails;
import org.worldbank.transport.tamt.shared.TrafficCountRecord;
import org.worldbank.transport.tamt.shared.TrafficFlowReport;

public class TrafficFlowReportDAO extends DAO {

	static Logger logger = Logger.getLogger(TrafficFlowReportDAO.class);
	
	private static TrafficFlowReportDAO singleton = null;
	public static TrafficFlowReportDAO get() {
		if (singleton == null) {
			singleton = new TrafficFlowReportDAO();
		}
		return singleton;
	}

	private String DELIMITER = ",";
	private NumberFormat formatter = new DecimalFormat("#0.00");
	
	public TrafficFlowReportDAO()
	{
	
	}

	public ArrayList<ArrayList> getTrafficFlowReportData(TagDetails tagDetails, String dayType) throws Exception
	{

		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			
			String tagId = tagDetails.getId();
			
			String sql = "SELECT * FROM TAMT_trafficFlowReport(" +
					"'"+tagId+"', '"+dayType+"')" +
					"AS " +
					"foo (" +
					"\"hour\" integer," +
					"w2avg numeric," +
					"w3avg numeric," +
					"pcavg numeric," +
					"txavg numeric," +
					"ldvavg numeric," +
					"ldcavg numeric," +
					"hdcavg numeric," +
					"mdbavg numeric," +
					"hdbavg numeric)";
			
			logger.debug("SQL for getTrafficFlowReport: " + sql);
			
			ResultSet r = s.executeQuery(sql);
			
			ArrayList<ArrayList> rows = new ArrayList<ArrayList>();
			
			while (r.next()) {
				
				// get the rows (there should always be 24, even if some are empty)
				// just keep them as lists for now so we can operate on them more easily
				// to determine "before-or-after" and "in-between" blanks
				// http://code.google.com/p/tamt/wiki/TrafficFlowReports#Before_and_After_Blanks
				ArrayList<String> row = new ArrayList<String>();
				row.add(r.getString(1)); // hour
				row.add(r.getString(2)); // w2avg
				row.add(r.getString(3)); // w3avg
				row.add(r.getString(4)); // pcavg
				row.add(r.getString(5)); // txavg
				row.add(r.getString(6)); // ldvavg
				row.add(r.getString(7)); // ldcavg
				row.add(r.getString(8)); // hdcavg
				row.add(r.getString(9)); // mdbavg
				row.add(r.getString(10)); // hdbavg
				
				rows.add(row);
				
			}

			return rows;
		
		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new Exception("There was an error executing the SQL: "
					+ e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new Exception("Unknown exception: " + e.getMessage());
		}
	}

	public void deleteTrafficFlowReports(TagDetails tagDetails, String dayType) throws Exception
	{
		/*
		 * We should delete any previous data for this tagid so
		 * that we don't end up with duplicate records per tag
		 */
		try {
			
			try {
				Connection connection = getConnection();
				Statement s = connection.createStatement();
				String sql = "DELETE FROM trafficflowreport " +
						"WHERE " +
						"	tagid = '" + tagDetails.getId() + "' " +
						"AND" +
						"	daytype = '"+dayType+"'";
				logger.debug(sql);
				s.executeUpdate(sql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				throw new Exception(
						"There was an error deleting previous traffic flow reports for tag("+tagDetails.getName()+"): "
								+ e.getMessage());
			}

		} 
	    catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			throw e;
			
		}
	}
	
	public void saveTraffiFlowReportRawData(ArrayList<ArrayList> rawData,
			TagDetails tagDetails, String dayType) throws Exception {
		
			
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		Date created = new Date();
		String ts = format.format(created);
		String tmpName = "/tmp/tamt-trafficflowreport-" + tagDetails.getId() + "-" + dayType + "-" + ts + ".txt";
		FileWriter fstream = new FileWriter(tmpName, true);
		BufferedWriter copy = new BufferedWriter(fstream);

		/*
		 * Now loop over the raw data and write each line, making sure
		 * to fetch a new sequence for each pid;
		 * 
		 */
		StringBuffer sb = new StringBuffer();
		String tagId = tagDetails.getId();
		String regionId = tagDetails.getRegion().getId();
		for (Iterator iterator = rawData.iterator(); iterator.hasNext();) {
			ArrayList rowData = (ArrayList) iterator.next();
			// Line format should be:
			// pid,regionid,tagid,created,hour_bin,w2,w3,pc,tx,ldv,ldc,hdc,mdb,hdb,totalflow\n
			
			int pid = getNextTrafficFlowReportSequenceValue();
			sb.append(pid);
			sb.append(DELIMITER);
			sb.append(regionId);
			sb.append(DELIMITER);
			sb.append(tagId);
			sb.append(DELIMITER);
			sb.append(dayType);
			sb.append(DELIMITER);
			sb.append(created);
			sb.append(DELIMITER);

			/*
			 * We need to query by hour_bin, but if it is just text, the sort
			 * does not work. So, convert it to a date here and store it as
			 * such, but always return it as just the date_part('hour') as a string
			 */
			Date hourDate = new Date(); // from above; just a throwaway
			String hourAsString = (String) rowData.get(0);
			hourDate.setHours( Integer.parseInt(hourAsString));
			sb.append(hourDate);
			
			sb.append(DELIMITER);
			sb.append(rowData.get(1));
			sb.append(DELIMITER);
			sb.append(rowData.get(2));
			sb.append(DELIMITER);
			sb.append(rowData.get(3));
			sb.append(DELIMITER);
			sb.append(rowData.get(4));
			sb.append(DELIMITER);
			sb.append(rowData.get(5));
			sb.append(DELIMITER);
			sb.append(rowData.get(6));
			sb.append(DELIMITER);
			sb.append(rowData.get(7));
			sb.append(DELIMITER);
			sb.append(rowData.get(8));
			sb.append(DELIMITER);
			sb.append(rowData.get(9));
			sb.append(DELIMITER);
			
			/*
			// add up all the counts for vehicle type and insert as total flow
			double totalFlow = 0.0;
			Double flow = new Double(0.0);
			for (int i = 0; i <= 9; i++) {
				flow = Double.parseDouble( (String)rowData.get(i) );
				totalFlow = totalFlow + flow;
			}
			*/
			sb.append("\\N"); // placeholder for total flow
			sb.append("\n");
		}
		copy.write(sb.toString());
		copy.close();
		
		// now COPY the data into the table en masse
		try {
			
			// COPY the data from the text file into the table
			try {
				
				Connection connection = getConnection();
				Statement s = connection.createStatement();
				String sql = "COPY trafficflowreport FROM '" + tmpName + "' WITH DELIMITER ','";
				logger.debug(sql);
				s.executeUpdate(sql);
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				throw new Exception(
						"There was an error copying the traffic flow report to the database: "
								+ e.getMessage());
			}
			
			// update the totalflow for every record
			try {
				
				Connection connection = getConnection();
				Statement s = connection.createStatement();
				String sql = "UPDATE trafficflowreport SET totalflow = (w2 + w3 + pc + tx + ldv + ldc + hdc + mdb + hdb)";
				logger.debug(sql);
				s.executeUpdate(sql);
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				throw new Exception(
						"There was an error updating the totalflow for the traffic flow report: "
								+ e.getMessage());
			}
			
		} 
	    catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			throw e;
			
		}
	    
	    // Not deleting right now because the delete is happening before
	    // the copy is completed...these will disappear when the VM is rebooted anyway
	    //File copyFile = new File(tmpName);
	    //copyFile.delete();
	    
	}
	
	private int getNextTrafficFlowReportSequenceValue() throws Exception {
		int seqVal = 1;
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "SELECT nextval('trafficflowreport_pid_seq')";
			ResultSet r = s.executeQuery(sql);
			while (r.next()) {
				seqVal = r.getInt(1);
			}
			connection.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new Exception(
					"There was an error fetching the next traffic flow report sequence: "
							+ e.getMessage());
		}
		return seqVal;
	}

	public TrafficFlowReport getTrafficFlowReport(TagDetails tagDetails, String dayType) throws Exception {
		
		TrafficFlowReport report = null;
		String tagId = tagDetails.getId();
		
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "SELECT  " +
					"created, " +
					"date_part('hour', hour_bin) as hour_bin, " +
					"w2, " + 
					"w3, " + 
					"pc, " + 
					"tx, " + 
					"ldv, " + 
					"ldc, " + 
					"hdc, " + 
					"mdb, " + 
					"hdb " + 
					"FROM trafficflowreport " +
					"WHERE tagid = '"+tagId+"' " +
					"AND daytype = '"+dayType+"' " +
					"ORDER BY hour_bin";
			logger.debug("getTrafficFlowReport sql=" + sql);
			ResultSet r = s.executeQuery(sql); 
			
			report = new TrafficFlowReport();
			report.setTagId(tagId);
			report.setDayType(dayType);
			
			ArrayList<ArrayList> dayTypeValues = new ArrayList<ArrayList>();
			
			while( r.next() ) { 
				
				ArrayList<String> values = new ArrayList<String>();
				report.setCreated(r.getTimestamp(1)); // set multiple times, oh well.
				
				values.add(r.getString(2)); // hour_bin
				
				values.add(formatter.format(r.getDouble(3)));
				values.add(formatter.format(r.getDouble(4)));
				values.add(formatter.format(r.getDouble(5)));
				values.add(formatter.format(r.getDouble(6)));
				values.add(formatter.format(r.getDouble(7)));
				values.add(formatter.format(r.getDouble(8)));
				values.add(formatter.format(r.getDouble(9)));
				values.add(formatter.format(r.getDouble(10)));
				values.add(formatter.format(r.getDouble(11)));
			
				dayTypeValues.add(values);
			}
			
			report.setDayTypeValues(dayTypeValues);
			
			connection.close(); // returns connection to the pool

		} 
	    catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			throw e;
			
		} 		
	    //logger.debug("report=" + report);
		return report;
		
	}

	public String getTrafficFlowReportForRegion() throws Exception {
		
		String output = "";
		
		StringBuffer sb = new StringBuffer();
		sb.append("REGION,TAG,DAYTYPE,REPORTDATE,HOUR,W2,W3,PC,TX,LDV,LDC,HDC,MDB,HDB,TOTALFLOW\n");
		
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "select "+
				"s.name as region,"+
				"t.name as tag,r.daytype,r.created,date_part('hour', r.hour_bin) as hour,"+
				"w2,w3,pc,tx,ldv,ldc,hdc,mdb,hdb,totalflow "+
				"from trafficflowreport r, studyregion s, tagdetails t "+
				"where r.regionid = s.id and r.tagid = t.id " +
				"AND s.iscurrentregion=true "+
				"order by s.name,t.name,r.daytype,r.created,r.hour_bin";
			logger.debug("getTrafficFlowReport sql=" + sql);
			ResultSet r = s.executeQuery(sql); 
			
			// change the formatter just for this method
			//formatter = new DecimalFormat("#0.0000");
			
			while( r.next() ) { 
				
				sb.append(r.getString(1) + ","); // region
				sb.append(r.getString(2) + ","); // tag
				sb.append(r.getString(3) + ","); // daytype
				sb.append(r.getTimestamp(4) + ","); // created
				sb.append(r.getString(5) + ","); // hour
				sb.append(formatter.format(r.getDouble(6)) + ","); // w2
				sb.append(formatter.format(r.getDouble(7)) + ","); // w3
				sb.append(formatter.format(r.getDouble(8)) + ","); // pc
				sb.append(formatter.format(r.getDouble(9)) + ","); // tx
				sb.append(formatter.format(r.getDouble(10)) + ","); // ldv
				sb.append(formatter.format(r.getDouble(11)) + ","); // ldc
				sb.append(formatter.format(r.getDouble(12)) + ","); // hdc
				sb.append(formatter.format(r.getDouble(13)) + ","); // mdb
				sb.append(formatter.format(r.getDouble(14)) + ","); // hdb
				sb.append(formatter.format(r.getDouble(15)) + "\n"); // totalflow
				
			}
			
			output = sb.toString();
			
			//formatter = new DecimalFormat("#0.00");
			
			connection.close(); // returns connection to the pool

		} 
	    catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
			
		} 	
	    return output;
	}	
	
}

