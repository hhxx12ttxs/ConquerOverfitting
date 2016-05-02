package org.worldbank.transport.tamt.gps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.worldbank.transport.tamt.shared.TAMTPoint;


public class ZipToTAMTPointAndDatabase {

	private static final String DELIMITER = "\t";
	private static String zipFilePath = "/home/tamt/eclipse/workspace/tamt/test/org/worldbank/transport/tamt/gps/sample.multiple.zip";
	private static ZipFile zipFile;
	private static Enumeration entries;
	static Pattern pattern = Pattern.compile(",\\s*");
	
	protected static Connection connection;
	static Logger logger = Logger.getLogger(ZipToTAMTPointAndDatabase.class);
	private static File tmpFile;
	private static BufferedWriter out;
	private static String tmpfileName;
	
	public static void main(String[] args) {

		String line2 = null;
		int numRecords = 0;
		
		try {
		
			long start = System.currentTimeMillis();
			
			init();
			
			
			// open zip
			zipFile = new ZipFile(zipFilePath);
			
			entries = zipFile.entries();

		      while(entries.hasMoreElements()) {
		    	
		        ZipEntry entry = (ZipEntry)entries.nextElement();
		        //System.out.println(entry.getName());
		        //System.out.println(entry.getSize());
		        
		        InputStream is = zipFile.getInputStream(entry);
		        
		        // read the inputstream
		        if( is != null )
		        {
		        	String line;
		        	
		        	BufferedReader buff = new BufferedReader(new InputStreamReader(is));
		        	while(( line = buff.readLine()) != null)
		        	{
		        		// operate on line
		        		// sb.append(line).append("\n");
		        		//System.out.println(line);
		        		//count++;
		        		if( line.indexOf("$GPRMC") != -1)
						{
							// this assumes that GPGGA is the only other captured sentence
							// and will not work if another sentence is recorded of if the
							// sentence sequence is NOT in the correct order (RMC, GGA)
							line2 = buff.readLine();
							try {
								processLineSet(line, line2);
							} catch (NumberFormatException e)
							{
								System.out.println("error processing line set:" + e.getMessage());
								continue;
							} catch (SQLException e) {
								System.out.println("error adding point to database:" + e.getMessage());
								continue;
							}
							numRecords++;
						}
		        	}
		        	
		        }
		      }
		      System.out.println("numRecords="+numRecords);

		      zipFile.close();
		      
		      // finish writing...
		      out.close();
		      
		      // now, use postgresql COPY to move data into DB
		      // with lightning speed
		      copyTmpFile();
		       
		      
		      long end = System.currentTimeMillis();
			  long delta = end - start;
			  System.out.println("total time="+delta);
			  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.out.println("SFMSFM");
		} catch (Exception e)
		{
			System.out.println("SFMSFM 2");
		}
		
	}
	
	private static void copyTmpFile() throws SQLException {
		
		// tmpfileName to database
		try {
			Statement s = connection.createStatement();
			String sql = "COPY \"gpsPointsTest\" FROM '"+tmpfileName+"'";
			s.executeUpdate(sql); 
			
		} 
	    catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			throw e;
		} 	
	}

	public static void processLineSet(String gprmc, String gpgga) throws SQLException
	{
		//System.out.println("gprmc=" + gprmc);
		//System.out.println("gpgga" + gpgga);
		
		// set up new point
		TAMTPoint p = new TAMTPoint();
		p.setId( UUID.randomUUID().toString() );
		
		parseGPRMC(gprmc, p);
		parseGPGGA(gpgga, p);
		
		// insert into database;
		//insertPoint(p);
		if( p.getTimestamp() != null)
		{
			appendPoint(p);
		}
		
	}	
	
	public static void appendPoint(TAMTPoint p)
	{
		// append to a file in /tmp/
		StringBuffer sb = new StringBuffer();
		
		// id, lat, lng, bearing, speed, altitude, created
		sb.append(p.getId());
		sb.append(DELIMITER);
		sb.append(p.getLatitude());
		sb.append(DELIMITER);
		sb.append(p.getLongitude());
		sb.append(DELIMITER);
		sb.append(p.getBearing());
		sb.append(DELIMITER);
		sb.append(p.getSpeed());
		sb.append(DELIMITER);
		sb.append(p.getAltitude());
		sb.append(DELIMITER);
		sb.append(p.getTimestamp());
		sb.append("\n");
		
		try {
			out.write(sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void insertPoint(TAMTPoint p) throws SQLException
	{
		try {
			Statement s = connection.createStatement();
			String sql = "INSERT INTO \"gpsPointsTest\" (id, latitude, longitude, bearing, speed, altitude, created) " +
					"VALUES ('"+p.getId()+"', " +
							"'"+p.getLatitude()+"'," +
							"'"+p.getLongitude()+"'," +
							"'"+p.getBearing()+"'," +
							"'"+p.getSpeed()+"'," +
							"'"+p.getAltitude()+"'," +
							"'"+p.getTimestamp()+"')";
			//logger.debug("sql=" + sql);
			s.executeUpdate(sql); 
			
		} 
	    catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			throw e;
			
		} 		
	}
	
	public static void parseGPGGA(String line, TAMTPoint p)
	{
		//String[] data = line.split(",\\s*");
		String[] data = pattern.split(line);
		
		//  $GPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,*47
		
		// altitude
		p.setAltitude(Double.parseDouble(data[9]));
		
		// altitude units
		p.setAltitudeUnits(data[10]);
		
	}
	
	public static void parseGPRMC(String line, TAMTPoint p)
	{
		//String[] data = line.split(",\\s*");
		String[] data = pattern.split(line);
		
		// timestamp from hhmmss, ddmmyy
		Date timestamp = parseDate(data[1], data[9]);
		//p.setTimestamp(timestamp);
		
		// latitude (ddmm.ss), latitude hemisphere (N or S)
		double latitude = parseCoord(data[3], data[4]);
		p.setLatitude(latitude);
		
		// longitude (ddmm.ss), longitude hemisphere (E or W)
		double longitude = parseCoord(data[5], data[6]);
		p.setLongitude(longitude);
		
		// bearing 
		//System.out.println("bearing=" + data[8]);
		double bearing = Double.parseDouble(data[8]);
		p.setBearing(bearing);
		
		// speed (in knots)
		double speed = Double.parseDouble(data[7]);
		p.setSpeed(speed);
		
	}	

	public static double parseCoord(String ddmmss, String hemisphere)
	{
		double coord = 0;
		
		// degrees
		double degrees = Double.parseDouble(ddmmss.substring(0, 2));
		
		// minutes (with any leftover decimal seconds)
		double minutes = Double.parseDouble(ddmmss.substring(2, ddmmss.length()));
		
		// calc coord
		coord = degrees + ((minutes*60)/3600);
		
		// if hemisphere is W or S, then make coord negative
		if( hemisphere.equals("W") || hemisphere.equals("S"))
		{
			// coord = Math.abs(coord) * -1;
			coord = coord * -1;
		}
		
		return coord;
	}
	
	public static Date parseDate(String hhmmss, String ddmmyy)
	{
		// first two chars of hhmmss are hours
		int hours = Integer.parseInt( hhmmss.substring(0, 2) );
		
		// next two chars of hhmmss are minutes
		int minutes = Integer.parseInt( hhmmss.substring(2, 4));
		
		// next two chars of hhmmss are seconds
		int seconds = Integer.parseInt( hhmmss.substring(4, 6) );
		
		// first two chars of ddmmyy are day
		int date = Integer.parseInt( ddmmyy.substring(0, 2) );
		
		// next two chars of ddmmyy are month
		int month = Integer.parseInt( ddmmyy.substring(2, 4) );
		
		// next two chars of ddmmyy are year
		int year = 100 + Integer.parseInt( ddmmyy.substring(4, 6)); // add 100 to 1900 to get into 21st century

		// create date
		Date d = new Date(year, month, date, hours, minutes, seconds);
		
		return d;
	}
	
	protected static void init() throws IOException
	{
		try 
		{
			System.out.println("initializing database...");
			Class.forName("org.postgresql.Driver");
			String url = "jdbc:postgresql://localhost:5432/tamt";
			connection = DriverManager.getConnection(url, "gis", "gis"); 
			// ((org.postgresql.PGConnection)connection).addDataType("geometry","org.postgis.PGgeometry");
			//((org.postgresql.Connection)conn).addDataType("box3d","org.postgis.PGbox3d");
			   
		} catch (Exception e)
		{
			logger.debug(e.getMessage());
		}
		
		// and init file to write to for COPY command
		tmpfileName = "/tmp/" + UUID.randomUUID().toString();
		System.out.println("Saving data to:" + tmpfileName);
		FileWriter fstream = new FileWriter(tmpfileName,true);
        out = new BufferedWriter(fstream);
    
	}	

}

