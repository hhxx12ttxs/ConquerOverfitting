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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.worldbank.transport.tamt.shared.GPSTrace;
import org.worldbank.transport.tamt.shared.StudyRegion;
import org.worldbank.transport.tamt.shared.TAMTPoint;

public class GPSTraceDAO extends DAO {

	static Logger logger = Logger.getLogger(GPSTraceDAO.class);
	protected final String DELIMITER = "\t";
	private RegionDAO regionDao;
	
	private static GPSTraceDAO singleton = null;
	public static GPSTraceDAO get() {
		if (singleton == null) {
			singleton = new GPSTraceDAO();
		}
		return singleton;
	}

	public GPSTraceDAO() {
		regionDao = RegionDAO.get();
	}

	public ArrayList<GPSTrace> getGPSTraces(StudyRegion region)
			throws Exception {
		ArrayList<GPSTrace> gpsTraceList = new ArrayList<GPSTrace>();
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "select id, name, description, region, fileId, "
					+ "uploaddate, processdate, processrecordcount, "
					+ "matchedpoints, recordCount from \"gpstraces\" "
					+ "where region = '" + region.getId() + "' ORDER BY name";
			logger.debug("SQL for getGPSTraces: " + sql);
			ResultSet r = s.executeQuery(sql);
			while (r.next()) {
				/*
				 * Retrieve the geometry as an object then cast it to the
				 * geometry type. Print things out.
				 */
				String id = r.getString(1);
				String name = r.getString(2);
				String description = r.getString(3);
				// String regionName = r.getString(4);
				String fileId = r.getString(5);
				Date uploadDate = r.getTimestamp(6);
				Date processDate = r.getTimestamp(7);
				int processedCount = r.getInt(8);
				int matchedCount = r.getInt(9);
				int recordCount = r.getInt(10);

				GPSTrace gpsTrace = new GPSTrace();
				gpsTrace.setId(id);
				gpsTrace.setName(name);
				gpsTrace.setDescription(description);
				gpsTrace.setRegion(region); // came in as param, no need to
											// reset
				gpsTrace.setFileId(fileId);
				gpsTrace.setUploadDate(uploadDate);
				gpsTrace.setProcessDate(processDate);
				gpsTrace.setProcessedCount(processedCount);
				gpsTrace.setMatchedCount(matchedCount);
				gpsTrace.setRecordCount(recordCount);

				/*
				 * If process date is after upload date, we know this is
				 * processed. (Saving trace in BO sets process date to yesterday
				 * on upload). Default for processed flag is false.
				 */
				if (processDate.after(uploadDate)) {
					gpsTrace.setProcessed(true);
				}

				gpsTraceList.add(gpsTrace);
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

		return gpsTraceList;

	}

	public GPSTrace getGPSTraceById(String id) throws Exception {
		GPSTrace gpsTrace = new GPSTrace();
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "select id, name, description, region, fileId, "
					+ "uploaddate, processdate, processrecordcount, "
					+ "matchedpoints, recordCount from \"gpstraces\" "
					+ "where id = '" + id + "' ORDER BY name";
			ResultSet r = s.executeQuery(sql);
			while (r.next()) {
				/*
				 * Retrieve the geometry as an object then cast it to the
				 * geometry type. Print things out.
				 */
				String name = r.getString(2);
				String description = r.getString(3);
				String regionId = r.getString(4);
				String fileId = r.getString(5);
				Date uploadDate = r.getTimestamp(6);
				Date processDate = r.getTimestamp(7);
				int processedCount = r.getInt(8);
				int matchedCount = r.getInt(9);
				int recordCount = r.getInt(10);

				gpsTrace.setId(id);
				gpsTrace.setName(name);
				gpsTrace.setDescription(description);

				StudyRegion region = new StudyRegion();
				region.setId(regionId);

				gpsTrace.setRegion(region);
				gpsTrace.setFileId(fileId);
				gpsTrace.setUploadDate(uploadDate);
				gpsTrace.setProcessDate(processDate);

				/*
				 * If process date is after upload date, we know this is
				 * processed. (Saving trace in BO sets process date to yesterday
				 * on upload). Default for processed flag is false.
				 */
				if (processDate.after(uploadDate)) {
					gpsTrace.setProcessed(true);
				}

				gpsTrace.setProcessedCount(processedCount);
				gpsTrace.setMatchedCount(matchedCount);
				gpsTrace.setRecordCount(recordCount);

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

		return gpsTrace;

	}

	public void saveFile(File file, String fileId) throws Exception {
		try {

			Timestamp created = new Timestamp((new java.util.Date()).getTime());
			logger.debug("created=" + created.toString());

			FileInputStream fis = new FileInputStream(file);

			/*
			 * setBinaryStream is not yet implemented in JDBC4 with postgres
			 * drivers, so we need to convert the file to a byte[]
			 */
			byte[] b = new byte[(int) file.length()];
			fis.read(b);

			// id, bytes, created
			Connection connection = getConnection();
			PreparedStatement ps = connection
					.prepareStatement("INSERT INTO \"gpsfiles\" VALUES (?, ?, ?)");
			ps.setString(1, fileId);
			ps.setBytes(2, b);
			ps.setTimestamp(3, created);
			ps.executeUpdate();

			connection.close(); // returns connection to pool

		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw e;
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new Exception(
					"Could not convert GPS file to binary database format");
		}
	}

	public GPSTrace saveGPSTrace(GPSTrace gpsTrace) throws SQLException {

		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "INSERT INTO \"gpstraces\" (id, name, description, "
					+ "region, fileId, uploadDate, processDate, processrecordcount,"
					+ "matchedpoints, recordCount) " + "VALUES ('"
					+ gpsTrace.getId()
					+ "', "
					+ "'"
					+ gpsTrace.getName()
					+ "',"
					+ "'"
					+ gpsTrace.getDescription()
					+ "',"
					+ "'"
					+ gpsTrace.getRegion().getId()
					+ "',"
					+ "'"
					+ gpsTrace.getFileId()
					+ "', "
					+ "'"
					+ gpsTrace.getUploadDate()
					+ "', "
					+ "'"
					+ gpsTrace.getProcessDate()
					+ "',"
					+ "'"
					+ gpsTrace.getProcessedCount()
					+ "', "
					+ "'"
					+ gpsTrace.getMatchedCount()
					+ "', "
					+ "'"
					+ gpsTrace.getRecordCount() + "' " + ")";
			logger.debug("sql=" + sql);
			s.executeUpdate(sql);

			connection.close(); // returns connection to pool
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			throw e;

		}

		return gpsTrace;

	}

	public GPSTrace updateGPSTrace(GPSTrace gpsTrace) throws SQLException {
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			// TODO: extend the model to include regionName string or region
			// StudyRegion as property of GPSTrace
			// for now we just use 'default'
			String sql = "UPDATE \"gpstraces\" SET " + " name = '"
					+ gpsTrace.getName() + "'," + " description = '"
					+ gpsTrace.getDescription() + "'," + " region = '"
					+ gpsTrace.getRegion().getId() + "'," + " fileId = '"
					+ gpsTrace.getFileId() + "', " + " uploadDate = '"
					+ gpsTrace.getUploadDate() + "', " + " processDate = '"
					+ gpsTrace.getProcessDate() + "', "
					+ " processrecordcount = '" + gpsTrace.getRecordCount()
					+ "', " + " matchedpoints = '" + gpsTrace.getMatchedCount()
					+ "', " + " recordCount = '" + gpsTrace.getRecordCount()
					+ "' " + "WHERE id = '" + gpsTrace.getId() + "'";
			logger.debug("UPDATE GPSTRACE SQL=" + sql);
			s.executeUpdate(sql);
			connection.close(); // returns connection to pool

		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw e;
		}

		return gpsTrace;
	}

	public void processGPSTraces(ArrayList<String> gpsTraceIds)
			throws Exception {
		for (Iterator<String> iterator = gpsTraceIds.iterator(); iterator
				.hasNext();) {
			String id = (String) iterator.next();
			processGPSTraceById(id);
		}
	}

	public void processGPSTraceById(String id) throws Exception {
		try {
			logger.debug("processing traces from id=" + id);

			// fetch the GPSTrace by id
			GPSTrace gpsTrace = getGPSTraceById(id);
			logger.debug("gpsTrace=" + gpsTrace);

			// use the fileId from the trace to get the archived bytes
			processTraceContents(gpsTrace);

		} catch (Exception e) {

			logger.error(e.getMessage());

			// we need to delete the trace if we can't process it
			deleteGPSTraceById(id);

			// then throw the error
			throw e;
		}
	}

	private void processTraceContents(GPSTrace gpsTrace) throws Exception {

		/*
		 * 1. Query for the byte array of id=zipId in gpsfiles 2. Create a
		 * ZipFile from the byte array 3. Process the entries in the ZipFile -
		 * format the lines for each ZipEntry like processLineSet - write them
		 * out to the filesystem /tmp - use the SQL COPY command to bring them
		 * all back is as TAMTPoints with the associated zipId Throw whatever
		 * exceptions are needed
		 */
		String traceId = gpsTrace.getId();
		String zipId = gpsTrace.getFileId();

		// 0. get the next value for the gpspoints sequence (we need to create
		// our own for the copy)
		int nextSequence = getNextGPSPointSequenceValue();

		// 1. Query for the byte array
		byte[] bytes = null;
		try {

			Connection connection = getConnection();
			Statement s = connection.createStatement();

			String sql = "SELECT gpsfile FROM \"gpsfiles\" WHERE id = '"
					+ zipId + "'";
			ResultSet r = s.executeQuery(sql);
			while (r.next()) {
				bytes = r.getBytes(1);
			}

			connection.close(); // returns connection to pool

		} catch (SQLException e) {
			logger.error("Could not get bytes for zipId: " + e.getMessage());
			throw new Exception(
					"Could not retrieve file contents from database");
		}

		// test the byte array
		if (bytes == null) {
			throw new Exception("File contents were empty");
		} else {
			logger.debug("we have a byte array with length=" + bytes.length);
		}

		// now that we have bytes, write it temporarily to filesystem with a
		// timestamp
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String ts = format.format(new Date());

		String tmpName = "/tmp/tamt-tmp-" + zipId + "-" + ts + ".zip";
		File tmpFile = new File(tmpName);
		FileOutputStream fos = new FileOutputStream(tmpFile);
		fos.write(bytes);
		fos.flush();
		fos.close();

		// and read it again as a zipfile
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(tmpName);
		} catch (IOException e) {
			throw new Exception(
					"The file was blank or could not be opened as a ZIP archive");
		}
		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		// loop on the zip entries and write all the contents in the TAMTPoint
		// schema format to a single file
		String copyFileName = "/tmp/tamt-tmp-" + zipId + "-" + ts + ".copy";
		FileWriter fstream = new FileWriter(copyFileName, true);
		BufferedWriter copy = new BufferedWriter(fstream);

		// keep track of the number of records
		int numRecords = 0;

		/*
		 * TODO: A nice to have... If we ever need to visualize an entire log
		 * file in Google Maps, putting 1000s of points on the map is not
		 * practical. However, we might gain something by storing a PostGIS
		 * LINESTRING, then for viewing convert the linestring into Google Maps
		 * encoded string.
		 */
		
		// this pattern is for making sure we only parse GPRMC-formatted NMEA sentences
		String patternGPRMC = "^\\$GPRMC.*";
		
		// this pattern is for extracting columns of data from the NMEA sentence
		Pattern pattern = Pattern.compile(",\\s*");
		Pattern coordRegex = Pattern.compile("(.*)(\\d{2})(\\d{2})\\.(.*)$");
		
		while (entries.hasMoreElements()) {
			
			/*
			 * In this case, a ZipEntry corresponds to a GPS log file from
			 * a particular vehicle. We need to keep track of a unique
			 * identifier for each GPS log file in the GPSTrace zip file
			 * so we can properly calculate soak times and trip lengths
			 */
			ZipEntry entry = (ZipEntry) entries.nextElement();
			String gpsLogId = entry.getName();
			InputStream is = zipFile.getInputStream(entry);
			if (is != null) {
				String line;

				BufferedReader buff = new BufferedReader(new InputStreamReader(
						is));
				while ((line = buff.readLine()) != null) {
					
					if( line.matches(patternGPRMC))
					{
						try {
							
							TAMTPoint p = new TAMTPoint();
							p.setId(UUID.randomUUID().toString());

							/*
							 * Extract the data from the GPRMC sentence (ie,
							 * line)
							 */
							String[] data = pattern.split(line);

							// timestamp from hhmmss, ddmmyy
							String timestamp = parseDate(data[1], data[9]);
							p.setTimestamp(timestamp);

							// latitude (ddmm.ss), latitude hemisphere (N or S)
							double latitude = parseCoordRegex(data[3], data[4], coordRegex);
							p.setLatitude(latitude);

							// longitude (ddmm.ss), longitude hemisphere (E or
							// W)
							double longitude = parseCoordRegex(data[5], data[6], coordRegex);
							p.setLongitude(longitude);

							// bearing
							double bearing = Double.parseDouble(data[8]);
							p.setBearing(bearing);
							
							// speed (in knots)
							double speed = Double.parseDouble(data[7]);
							// convert to meters per second
							// 1 knot = 0.514444444 meters per second
							speed = (speed * 0.5144);
							p.setSpeed(speed);

							// if we have not failed up to this point
							// (as indicated by fetching the timestamp -- kind
							// of a hack)
							// then we can append the point to the COPY file
							if (p.getTimestamp() != null) {
								// append to a file in /tmp/
								StringBuffer sb = new StringBuffer();

								// id, gpsLogId, gpsTraceId, lat, lng, bearing, speed,
								// altitude, created, geometry (POINT)
								sb.append(nextSequence);
								sb.append(DELIMITER);
								sb.append(p.getId());
								sb.append(DELIMITER);
								sb.append(traceId); // the parent id of the
													// GPSTrace containing the
													// zip from which this point
													// came
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
								sb.append(DELIMITER);
								sb.append("\\N"); // null for geometry;
								sb.append(DELIMITER);
								sb.append("\\N"); // null for tag_id;
								sb.append(DELIMITER);
								sb.append("\\N"); // null for road_id;
								sb.append(DELIMITER);
								sb.append("\\N"); // null for zone_id;
								sb.append(DELIMITER);
								sb.append("\\N"); // null for daytype;
								sb.append(DELIMITER);
								sb.append("\\N"); // null for hour;
								sb.append(DELIMITER);
								sb.append("\\N"); // null for kph;
								sb.append(DELIMITER);
								sb.append("\\N"); // null for speedbinnumber;
								sb.append(DELIMITER);
								sb.append(gpsLogId); // unique per logfile (aka per vehicle)
								
								// sb.append(geometry);// will print WKT
								// sb.append("GeometryFromText('"+geometry.toText()+"', 4326)");
								// // null geometry for now?
								sb.append("\n");

								nextSequence++; // we increment by 1 to mimic
												// postgresql

								try {
									copy.write(sb.toString());
									numRecords++;
								} catch (IOException e) {
									logger.error("Error writing COPY file: "
											+ e.getMessage());
									throw new Exception(
											"Error transfering point data to temporary file");
								}
							}

						} catch (NumberFormatException e) {
							// skip the line entry if there were problems with
							// number conversions
							logger.error("Error converting string to number from GPS record:"
									+ e.getMessage());
							continue;
						} catch (SQLException e) {
							// skip the line entry if there were sql errors
							logger.error("Error adding point to database:"
									+ e.getMessage());
							continue;
						} catch (Exception e) {
							logger.error(e);
							continue;
						}						

					}
				}

			}

		}

		logger.debug("numRecords=" + numRecords);
		if (numRecords == 0) {
			throw new NonGPSArchiveException(
					"No GPS records were found in the ZIP archive");
		}

		// finished reading the zipfile
		zipFile.close();

		// finish writing the COPY file
		copy.close();

		// now, use postgresql COPY to move data into DB
		// with greater speed than individual INSERT statements per record
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "COPY \"gpspoints\" FROM '" + copyFileName + "'";
			logger.debug(sql);
			s.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			throw new Exception(
					"There was an error copying the point data to the database: "
							+ e.getMessage());
		}

		// and update the sequence value since we did a manual COPY
		setNextGPSPointSequenceValue();
		
		// new: convert knots in the speed column to kph, and set
		// the speedbin number based on floor(kph/5)
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "SELECT * FROM TAMT_convertAndBinSpeeds('"+traceId+"');";
			logger.debug(sql);
			ResultSet r = s.executeQuery(sql); // returns a 1, which can be ignored
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			throw new Exception(
					"There was an error converting knots to kph and categorizing speed bins: "
							+ e.getMessage());
		}
		
		
		// fill out the study region a bit more, including providing the offset
		StudyRegion query = gpsTrace.getRegion();
		StudyRegion studyRegion = regionDao.getStudyRegion(query);
		
		/*
		 * Some imported points may be outside the study region boundary.
		 * Delete them.
		 */
		if( studyRegion != null )
		{
			logger.debug("Delete points outside of study region boundary");
			try {
				Connection connection = getConnection();
				Statement s = connection.createStatement();
				String sql = "DELETE FROM gpspoints WHERE id NOT IN " +
						"(SELECT p.id FROM gpspoints p, studyregion r " +
						"WHERE r.id = '"+studyRegion.getId()+"' " +
						"AND (ST_Contains(r.geometry, p.geometry)))";
				logger.debug(sql);
			
				/*
				 * TODO: we aren't executing this SQL yet, because
				 * when we do, the assign algorithm doesn't process
				 * any points -- even if the study area includes the
				 * intersection of Winneba and Dansoman where we 
				 * know there are points contained in the small.zip
				 * test GPS archive.
				 */
				//s.executeUpdate(sql);
			
			} catch (SQLException e) {
				logger.error("Delete points outside of region error:" + e.getMessage());
					throw new Exception(
							"There was an error deleting points outside the study region boundary: "
									+ e.getMessage());	
			}
		}
		
		/*
		 * We want to account for the UTC offset in the 'created' timestamp for each
		 * GPS point. To do this, we lookup the UTC offset the user supplied
		 * for the study region, and use PostGIS intervals to calculate the correct date
		 */
		String regionOffset = studyRegion.getUtcOffset();
		String intervalOperator = "+";
		if( regionOffset == null || regionOffset.isEmpty() )
		{
			// default to 0, as in 'no offset'
			// If the user has not supplied one, their GPS trip data may be WAY off
			regionOffset = "0"; 
		}
		int utcOffset = Integer.parseInt(regionOffset);
		if( utcOffset < 0)
		{
			// change the operator and remove the sign from the offset
			intervalOperator = "-";
			utcOffset = utcOffset * -1;
		}
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "UPDATE gpspoints SET " +
					" created = created "+intervalOperator+" " +
							"interval '"+utcOffset+" hours' " +
							"WHERE gpstraceid = '"+traceId+"'";
			logger.debug("Fix UTC sql=" + sql);
			s.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			throw new Exception(
					"There was an error updating the GPS point timestamp to the UTC offset for the study region: "
							+ e.getMessage());
		}		

		// Now calculate the dayType and hourOfDay columns for use in the speed binning process
		// TODO: find a way to make this faster
		// see: http://code.google.com/p/tamt/wiki/GPSPointDayTypeCalculation
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "UPDATE gpspoints SET " +
					" daytype = (select * from TAMT_calculateGPSPointDayOfWeek( extract(dow from created)::text)), " +
					" hour = extract(hour from created)::text " +
					"WHERE gpstraceid = '"+traceId+"'";
			logger.debug("Calculate dayType sql=" + sql);
			s.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			throw new Exception(
					"There was an error updating the GPS point dayType value: "
							+ e.getMessage());
		}		
		
		
		
		// now, update the postgis point geometry based on the lat / lng that we
		// COPYd in
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "SELECT calculate_gpspoint_geometry('" + traceId
					+ "')";
			logger.debug(sql);
			s.executeQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			throw new Exception(
					"There was an error updating the point geometry: "
							+ e.getMessage());
		}
		
		// Processed date now refers to when the GPS records
		// were assigned to roads

		// gpsTrace.setProcessDate(new Date());
		gpsTrace.setRecordCount(numRecords);
		gpsTrace.setProcessedCount(0);
		gpsTrace.setMatchedCount(0);
		updateGPSTrace(gpsTrace);
		
		
		/*
		 * Calculate engine soak times and trip length for every point
		 * in every log in the GPS trace being uploaded
		 * 
		 * NOTE: WE CAN DO THIS LATER, FROM A USER TRIGGER IN THE UI
		 * 
		 * calculateEngineSoakTimesAndTripLength();
		 */
		

		// clean up the temporary files
		tmpFile.delete(); // we had a handle on this one, so just delete it
		File copyFile = new File(copyFileName); // we didn't have a deletable
												// handle, so recreate it here
		
		//TODO: comment to inspect files; uncomment for production
		copyFile.delete();

	}
	
	private void setNextGPSPointSequenceValue() throws Exception {
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "SELECT setval('gpspoints_pid_seq', (SELECT pid FROM gpspoints ORDER BY pid DESC LIMIT 1))";
			s.executeQuery(sql);
			connection.close(); // returns connection to pool
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			throw new Exception(
					"There was an error resetting the point sequence: "
							+ e.getMessage());
		}

	}

	private int getNextGPSPointSequenceValue() throws Exception {
		int seqVal = 1;
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "SELECT nextval('gpspoints_pid_seq')";
			ResultSet r = s.executeQuery(sql);
			while (r.next()) {
				seqVal = r.getInt(1);
			}
			connection.close(); // returns connection to pool
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			throw new Exception(
					"There was an error fetching the next point sequence: "
							+ e.getMessage());
		}
		return seqVal;
	}

	private double parseCoordRegex(String ddmmss, String hemisphere, Pattern p) throws Exception
	{
		double coord = 0.0;
		//logger.debug("ddmmss=" + ddmmss);
		Matcher m = p.matcher(ddmmss);
		if( m.matches())
		{
			// build degrees and minutes
			double degrees = Double.parseDouble(m.group(2));
			//logger.debug("degrees=" + degrees);
			String min = m.group(3) + "." + m.group(4);
			//logger.debug("min=" + min);
			double minutes = Double.parseDouble(min);
			coord = degrees + ((minutes * 60) / 3600);
			
			// if hemisphere is W or S, then make coord negative
			if (hemisphere.equals("W") || hemisphere.equals("S")) {
				// coord = Math.abs(coord) * -1;
				coord = coord * -1;
				//logger.debug("multiply coord by -1=" + coord);
			}
			
		} else {
			throw new Exception("There was an error parsing the coordinate");
		}
		return coord;
	}

	private String parseDate(String hhmmss, String ddmmyy) {
		
		// first two chars of hhmmss are hours
		int hours = Integer.parseInt(hhmmss.substring(0, 2));

		// next two chars of hhmmss are minutes
		int minutes = Integer.parseInt(hhmmss.substring(2, 4));

		// next two chars of hhmmss are seconds
		int seconds = Integer.parseInt(hhmmss.substring(4, 6));

		// first two chars of ddmmyy are day
		int date = Integer.parseInt(ddmmyy.substring(0, 2));

		// next two chars of ddmmyy are month
		int month = Integer.parseInt(ddmmyy.substring(2, 4));

		// next two chars of ddmmyy are year
		int year = 100 + Integer.parseInt(ddmmyy.substring(4, 6));
		
		// just format as straight UTC
		// this will go in the database
		year = year + 1900;
		String dateStringFormatted = year + "/" +month+ "/" +date+ " " +hours+ ":" +minutes+ ":" + seconds ;
		return dateStringFormatted;
	}

	public void deleteGPSTraces(ArrayList<String> gpsTraceIds)
			throws SQLException {
		for (Iterator<String> iterator = gpsTraceIds.iterator(); iterator
				.hasNext();) {
			String id = (String) iterator.next();
			deleteGPSTraceById(id);
		}
	}

	public void deleteGPSTraceById(String id) throws SQLException {
		try {

			Connection connection = getConnection();
			Statement s = connection.createStatement();

			// first, we need to fetch the gpsTrace (by id) to get the
			// associated fileId
			String sql = "SELECT fileId, name FROM \"gpstraces\" WHERE id = '"
					+ id + "'";
			String fileId = null;
			logger.debug("fetch fileId sql=" + sql);
			ResultSet r = s.executeQuery(sql);
			// should only get 1 in the result set
			while (r.next()) {
				fileId = r.getString(1);
				logger.debug("deleting gps trace name=" + r.getString(2));
			}

			// TODO: Handle error if fileId is still null here --- either
			// something went wrong with the upload or somehow the fileId was
			// deleted
			// from this record

			// next, delete the rows in gpspoints that match the id
			sql = "DELETE FROM \"gpspoints\" WHERE gpstraceid = '" + id + "'";
			logger.debug("delete gps points for trace id=" + sql);
			s.execute(sql);

			// now, delete the trace
			sql = "DELETE FROM \"gpstraces\" WHERE id = '" + id + "'";
			logger.debug("delete gpstrace sql=" + sql);
			s.execute(sql);

			// And delete the associated item from gpsfiles.
			// Note: gpsTrace needs to be deleted first because of the foreign
			// key
			// but we need a nice way to recover if the delete of gpsFile
			// does not delete
			sql = "DELETE FROM \"gpsfiles\" WHERE id = '" + fileId + "'";
			logger.debug("delete gpsfiles sql=" + sql);
			s.execute(sql);

			connection.close(); // returns connection to pool

		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	public void assignPoints(GPSTrace gpsTrace) throws SQLException {

		try {

			Connection connection = getConnection();
			Statement s = connection.createStatement();

			// New stored procedure "tamt_assignpoints(id)" uses gpsTaggingTolerance from studyregion and default bearing of 45 degrees
			String sql = "SELECT * FROM TAMT_assignPoints(\'" + gpsTrace.getId() + "\') ";
			logger.debug("assignPoints sql=" + sql);
			ResultSet r = s.executeQuery(sql);
			// should only get 1 in the result set
			int numRecordsAssigned = 0;
			while (r.next()) {
				numRecordsAssigned = r.getInt(1);
			}
			logger.debug("numRecordsAssigned=" + numRecordsAssigned);

			connection.close(); // returns connection to pool

		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

}

