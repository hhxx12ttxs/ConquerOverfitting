package org.worldbank.transport.tamt.server.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.worldbank.transport.tamt.shared.RoadDetails;
import org.worldbank.transport.tamt.shared.StudyRegion;
import org.worldbank.transport.tamt.shared.Vertex;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class RoadDAO extends DAO {
	
	static Logger logger = Logger.getLogger(RoadDAO.class);
	
	private static RoadDAO singleton = null;
	public static RoadDAO get() {
		if (singleton == null) {
			singleton = new RoadDAO();
		}
		return singleton;
	}
	
	public RoadDAO()
	{
		
	}
	
	public ArrayList<RoadDetails> getRoadDetails(StudyRegion region) throws Exception
	{
		/*
		 * Query the roaddetails table where regionName = region.name
		 * 
		 * select * from "roaddetails" where region = 'default'
		 * 
		 */
		ArrayList<RoadDetails> roadDetailsList = new ArrayList<RoadDetails>();
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "select id, name, description, region, tag_id, AsText(geometry) " +
					"from \"roaddetails\" where " +
					"region = '"+region.getId()+"' ORDER BY name";
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
			      String tagId = r.getString(5);
			      String lineString = r.getString(6);
			      
			      // convert a linestring to a JTS geometry
			      WKTReader reader = new WKTReader();
			      Geometry geometry = reader.read(lineString);
			      Point centroidJTS = geometry.getCentroid();
			      
			      RoadDetails roadDetails = new RoadDetails();
			      roadDetails.setId(id);
			      roadDetails.setName(name);
			      roadDetails.setDescription(description);
			      roadDetails.setTagId(tagId);
			      
			      // now convert the geometry to an ArrayList<Vertex> and
			      // set in the roadDetails
			      ArrayList<Vertex> vertices = Utils.geometryToVertexArrayList(geometry);
			      roadDetails.setVertices(vertices);
			      
			      // convert the centroid point into a Vertex
			      Vertex centroid = new Vertex();
			      centroid.setLat(centroidJTS.getY());
			      centroid.setLng(centroidJTS.getX());
			      roadDetails.setCentroid(centroid);
			      
			      //TODO: Do we need to include the study region name, description here?
			      StudyRegion sr = new StudyRegion();
			      sr.setId(regionId);
			      
			      roadDetailsList.add(roadDetails);
			} 
			connection.close(); // returns connection to pool
		} 
	    catch (SQLException e) {
			logger.error(e.getMessage());
			throw new Exception("There was an error executing the SQL: " + e.getMessage());
		} 
	    catch (ParseException e) {
	    	logger.error(e.getMessage());
			throw new Exception("Cannot convert geometry string to geometry object: " + e.getMessage());
		}
		
		return roadDetailsList;
		
	}
	
	public RoadDetails saveRoadDetails(RoadDetails roadDetails, Geometry geometry) throws SQLException {

		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			// TODO: extend the model to include regionName string or region StudyRegion as property of RoadDetails
			// for now we just use 'default'
			// TODO: add geometry (store as jts linestring, BO will convert to coordinate pair list)
			String sql = "INSERT INTO \"roaddetails\" (id, name, description, region, tag_id, geometry) " +
					"VALUES ('"+roadDetails.getId()+"', " +
					"'"+roadDetails.getName()+"'," +
					"'"+roadDetails.getDescription()+"'," +
					"'"+roadDetails.getRegion().getId()+"'," +
					"'"+roadDetails.getTagId()+"'," +
					"GeometryFromText('"+geometry.toText()+"', 4326)" +
					")";
			logger.debug("sql=" + sql);
			s.executeUpdate(sql); 
			connection.close(); // returns connection to pool
		} 
	    catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			throw e;
			
		} 
	    
	    return roadDetails;
	    
	}
	
	public RoadDetails updateRoadDetails(RoadDetails roadDetails, Geometry geometry) throws SQLException {
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			// TODO: extend the model to include regionName string or region StudyRegion as property of RoadDetails
			// for now we just use 'default'
			String sql = "UPDATE \"roaddetails\" SET " +
					" name = '"+roadDetails.getName()+"'," +
					" description = '"+roadDetails.getDescription()+"'," +
					" region = '"+roadDetails.getRegion().getId()+"'," +
					" tag_id = '"+roadDetails.getTagId()+"', " +
					" geometry = GeometryFromText('"+geometry.toText()+"', 4326) " + 
					"WHERE id = '"+roadDetails.getId()+"'";
			logger.debug("sql=" + sql);
			s.executeUpdate(sql); 
			connection.close(); // returns connection to pool
		} 
		catch (SQLException e) {
			logger.error(e.getMessage());
			throw e;
		} 
	    
	    return roadDetails;
	}	
	
	public void deleteRoadDetails(ArrayList<String> roadDetailIds) throws SQLException {
		for (Iterator iterator = roadDetailIds.iterator(); iterator.hasNext();) {
			String id = (String) iterator.next();
			deleteRoadDetailById(id);
		}
	}
	
	public void deleteRoadDetailById(String id) throws SQLException
	{
		try {
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			String sql = "DELETE FROM \"roaddetails\" WHERE id = '"+id+"'";
			logger.debug("sql=" + sql);
			s.execute(sql); 
			connection.close(); // returns connection to pool
		} 
		catch (SQLException e) {
			logger.error(e.getMessage());
			throw e;
		}		
	}
	
	private ArrayList<RoadDetails> stubList()
	{
		ArrayList<RoadDetails> roadDetails = new ArrayList<RoadDetails>();
		int sample = 10;
		for (int i = 0; i < sample; i++) {
			UUID id = UUID.randomUUID();
			RoadDetails t = new RoadDetails();
			t.setName("Name-" + id.toString());
			t.setDescription("Desc-"+id.toString());
			roadDetails.add(t);
		}
		return roadDetails;
	}

}

