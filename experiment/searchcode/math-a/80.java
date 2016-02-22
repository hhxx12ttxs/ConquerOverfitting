package org.worldbank.transport.tamt.shared;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class RoadDetails implements Serializable {
	
	private String id;
	private String name;
	private String description;
	private StudyRegion region;
	private String tagId;
	private ArrayList<Vertex> vertices;
	private Vertex centroid;
	
	public RoadDetails() {
		
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public StudyRegion getRegion() {
		return region;
	}

	public void setRegion(StudyRegion region) {
		this.region = region;
	}

	public void setVertices(ArrayList<Vertex> vertices) {
		this.vertices = vertices;
	}

	public ArrayList<Vertex> getVertices() {
		return vertices;
	}

	public void setTagId(String tagId) {
		this.tagId = tagId;
	}

	public String getTagId() {
		return tagId;
	}

	public Vertex getCentroid() {
		return centroid;
	}

	/**
	 * Calculated server-side by JTS/PostGIS
	 * Used to help pan the map if a user clicks
	 * on a road that is out of view.
	 * 
	 * @param centroid
	 */
	public void setCentroid(Vertex centroid) {
		this.centroid = centroid;
	}
  
  
}
