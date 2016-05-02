/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
 *
 * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ib??ez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
package org.gvsig.graph.core;

import java.awt.geom.Point2D;
import java.util.Properties;

public class GvFlag {
	/*
	 * Indexes of flag features' fields.
	 * 
	 * */
	public static final int ID_FLAG_INDEX = 0;
	public static final int ID_ARC_INDEX = 1;
	public static final int PCT_INDEX = 2;
	public static final int COST_INDEX = 3;
	public static final int DIREC_INDEX = 4;
	public static final int DESCRIPTION_INDEX = 5;
	
	
	
	
	
	public final static int DIGIT_DIRECTION = 1;
	public final static int INVERSE_DIGIT_DIRECTION = -1;
	public final static int BOTH_DIRECTIONS = 0;


	private int idArc;
	private int direc;
	private double pct;
	private int idFlag;
	private Point2D originalPoint;
	private String description;
	private boolean enabled = true;
	private double cost = -1;
	private double accumulatedLength;
	private static int flagNumber = 0;
	
	/**
	 * Useful for general purpose. For example, we can use it to label flags (with something different
	 * from getDescription. In odMatrix, we use properties to distinguish between origins and 
	 * destinations.
	 */
	private Properties properties = new Properties();

	public GvFlag(double x, double y) {
		this.originalPoint = new Point2D.Double(x, y);
//		this.description = PluginServices.getText(this, "new_flag")+flagNumber++;
		this.description = "flag"+flagNumber++;
	}

	public GvFlag(double x, double y, double cost) {
		this.originalPoint = new Point2D.Double(x, y);
//		this.description = PluginServices.getText(this, "new_flag")+flagNumber++;
		this.description = "flag"+flagNumber++;
		this.cost = cost;
	}


	public GvFlag(double x, double y, String description) {
		this.originalPoint = new Point2D.Double(x, y);
		this.description = description;
	}


	public GvFlag(double x, double y, String description, double cost) {
		this.originalPoint = new Point2D.Double(x, y);
		this.description = description;
		this.cost = cost;
	}

	/**
	 * Returns the cost for this flag. The cost represents the amount of resource of
	 * the user's cost unit required to reach this flag from the previous one in
	 * the route.
	 * @return
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * Sets the cost of this flag. The cost represents the amount of resource of
	 * the user's cost unit required to reach this flag from the previous one in
	 * the route.
	 * @param cost
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}

	/**
	 * Returns a human-readable description of this flag. This is typically a name
	 * for this marker.
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of this flag. The description should be a human readable
	 * description for this flag such is street name or something like that. But
	 * it can be whatever.
	 *
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Tells if this flag is being taken in account when calculating the route.
	 * @return
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Enables or disables this flag. A disabled flag is not taken in account to
	 * compute the route.
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getIdArc() {
		return idArc;
	}

	public void setIdArc(int idArc) {
		this.idArc = idArc;
	}

	public int getDirec() {
		return direc;
	}

	public void setDirec(int sense) {
		this.direc = sense;
	}

	public double getPct() {
		return pct;
	}

	public void setPct(double pct) {
		this.pct = pct;
	}

	public void setIdFlag(int i) {
		idFlag = i;
	}

	public int getIdFlag() {
		return idFlag;
	}

	public Point2D getOriginalPoint() {
		return originalPoint;
	}

	public String toString() {
		return description;
	}

	public void setOriginalPoint(double x, double y) {
		originalPoint.setLocation(x, y);
		
	}

	public void setAccumulatedLength(double accumulatedLength) {
		this.accumulatedLength = accumulatedLength;
		
	}

	public double getAccumulatedLength() {
		return accumulatedLength;
	}

	public Properties getProperties() {
		return properties;
	}

}



