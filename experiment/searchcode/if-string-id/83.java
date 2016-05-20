package org.worldbank.transport.tamt.server.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.worldbank.transport.tamt.shared.RoadLengthReport;
import org.worldbank.transport.tamt.shared.StudyRegion;
import org.worldbank.transport.tamt.shared.TagDetails;

public class TagDAO extends DAO {

	static Logger logger = Logger.getLogger(TagDAO.class);
	
	private static TagDAO singleton = null;
	public static TagDAO get() {
		if (singleton == null) {
			singleton = new TagDAO();
		}
		return singleton;
	}
	
	public TagDAO()
	{
		
	}
	
	public ArrayList<TagDetails> getTagDetails(StudyRegion region)
	{
		/*
		 * Query the tagdetails table where regionName = region.name
		 * 
		 * select * from "tagdetails" where region = 'default'
		 * 
		 */
		ArrayList<TagDetails> tagDetailsList = new ArrayList<TagDetails>();
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "select * from \"tagdetails\" where region = '"+region.getId()+"' ORDER BY name";
			ResultSet r = s.executeQuery(sql); 
			while( r.next() ) { 
			      /* 
			      * Retrieve the geometry as an object then cast it to the geometry type. 
			      * Print things out. 
			      */ 
				  String id = r.getString(1);
			      String name = r.getString(2);
			      String description = r.getString(3);
			      String regionId = r.getString(4);
			      
			      TagDetails tagDetails = new TagDetails();
			      tagDetails.setId(id);
			      tagDetails.setName(name);
			      tagDetails.setDescription(description);
			      
			      tagDetails.setRegion(region);
			      
			      tagDetailsList.add(tagDetails);
			} 
			connection.close(); // returns connection to the pool
		} 
	    catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return tagDetailsList;
		
	}
	
	public TagDetails saveTagDetails(TagDetails tagDetails) throws SQLException {

		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "INSERT INTO \"tagdetails\" (id, name, description, region) VALUES (" +
					"'"+tagDetails.getId()+"', " +
					"'"+tagDetails.getName()+"'," +
					"'"+tagDetails.getDescription()+"'," +
					"'"+tagDetails.getRegion().getId()+"')";
			logger.debug("sql=" + sql);
			logger.debug("native sql=" + connection.nativeSQL(sql));
			s.executeUpdate(sql); 
			connection.close();
		} 
	    catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			throw e;
			
		} 
	    
	    return tagDetails;
	    
	}
	
	public TagDetails updateTagDetails(TagDetails tagDetails) throws SQLException {
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			// TODO: extend the model to include regionName string or region StudyRegion as property of TagDetails
			// for now we just use 'default'
			String sql = "UPDATE \"tagdetails\" SET " +
					" name = '"+tagDetails.getName()+"'," +
					" description = '"+tagDetails.getDescription()+"'," +
					" region = '"+tagDetails.getRegion().getId()+"'" +
					"WHERE id = '"+tagDetails.getId()+"'";
			logger.debug("sql=" + sql);
			s.executeUpdate(sql); 
			connection.close(); // returns connection to the pool
		} 
		catch (SQLException e) {
			logger.error(e.getMessage());
			throw e;
		} 
	    
	    return tagDetails;
	}	
	
	public void deleteTagDetails(ArrayList<String> tagDetailIds) throws SQLException {
		for (Iterator iterator = tagDetailIds.iterator(); iterator.hasNext();) {
			String id = (String) iterator.next();
			deleteTagDetailById(id);
		}
	}
	
	public void deleteTagDetailById(String id) throws SQLException
	{
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "DELETE FROM \"tagdetails\" WHERE id = '"+id+"'";
			logger.debug("sql=" + sql);
			s.execute(sql); 
			connection.close(); // returns connection to the pool
		} 
		catch (SQLException e) {
			logger.error(e.getMessage());
			throw e;
		}		
	}

	public void createRoadLengthReport() throws Exception {
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "SELECT * FROM TAMT_populateTotalDistanceByTag();";
			logger.debug(sql);
			ResultSet r = s.executeQuery(sql); // returns a 1, which can be ignored
		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new Exception(
					"There was an error creating the road length report: "
							+ e.getMessage());
		}	
	}

	public RoadLengthReport getRoadLengthReport() {
		/**
		 * TODO: query the roadlength table
		 * - for the current study region
		 * - return a report with an array of values in the report
		 * - [0] = tagname (based on tagid and regionid)
		 * - [1] = distance (km)
		 */
		RoadLengthReport report = new RoadLengthReport();
		ArrayList<ArrayList> reportValues = new ArrayList<ArrayList>();

		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "select r.name as region, " +
					"t.name as tag, " +
					"round(d.vkt, 3) " +
					"from " +
					"totaldistancebytag d, " +
					"tagdetails t, " +
					"studyregion r " +
					"where d.tag_id = t.id " +
					"and " +
					"r.id = t.region " +
					"and " +
					"r.id = (SELECT id FROM studyregion WHERE iscurrentregion IS TRUE)";
			logger.debug("SQL=" + sql);
			ResultSet r = s.executeQuery(sql); 
			while( r.next() ) { 
			      /* 
			      * Retrieve the geometry as an object then cast it to the geometry type. 
			      * Print things out. 
			      */ 
				  String region = r.getString(1);
			      String tag = r.getString(2);
			      String vkt = r.getString(3);
			      
			      ArrayList<String> thisRow = new ArrayList<String>();
			      thisRow.add(region);
			      thisRow.add(tag);
			      thisRow.add(vkt);
			      
			      reportValues.add(thisRow);
			      
			} 
			connection.close(); // returns connection to the pool
		} 
	    catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		report.setReportValues(reportValues);
		return report;
	}
	 
}

