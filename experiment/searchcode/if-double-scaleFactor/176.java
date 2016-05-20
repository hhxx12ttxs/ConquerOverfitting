/*
 * Created on 09-nov-2006
 *
 * gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
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
/* CVS MESSAGES:
 *
 * $Id: SnappingCoordinateMap.java 20706 2008-05-14 14:17:23Z vcaballero $
 * $Log$
 * Revision 1.2  2007-09-19 15:28:42  azabala
 * added a fixed hashCode (until we fix the requeriment of compute the same hashCode for all points in the same cluster circle)
 *
 * Revision 1.1  2006/12/04 19:50:43  azabala
 * *** empty log message ***
 *
 * Revision 1.2  2006/11/13 20:41:08  azabala
 * *** empty log message ***
 *
 * Revision 1.1  2006/11/09 21:08:32  azabala
 * *** empty log message ***
 *
 *
 */
package com.iver.cit.gvsig.util;

import java.util.Comparator;
import java.util.Hashtable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geomgraph.Node;

public class SnappingCoordinateMap extends Hashtable {
	class SnapCoordinate extends Coordinate {
		public SnapCoordinate(Coordinate arg0) {
			super(arg0);
		}

		public boolean equals(Object obj) {
			if(! (obj instanceof SnapCoordinate))
				return false;
			SnapCoordinate other = (SnapCoordinate) obj;
			return other.distance(this) <= snapTolerance;
		} 

		public int hashCode() {
//			 int result = 17;
//			 double xs = simplify(x);
//			 double ys = simplify(y);
//			 result = 37 * result + hashCode(xs);
//			 result = 37 * result + hashCode(ys);
//			 return result;
				
			return 1; // this is not efficient. look for a hash algorithm to ensure
			//all points in the same tolerance radius returns the same hash code
		}
		
		public double simplify(double coordinate){
			if(scaleFactor == 0d)
				return coordinate;
			return Math.round(coordinate * scaleFactor) / scaleFactor;
		}
	}
	
	

	private double snapTolerance;
	private double scaleFactor;

	public SnappingCoordinateMap(double snapTolerance) {
		super();
		this.snapTolerance = snapTolerance;
		if(snapTolerance != 0d)
			this.scaleFactor = 1d / snapTolerance;
	}

	class MinDistCoordComparator implements Comparator {
		Coordinate coord;

		MinDistCoordComparator(Coordinate coord) {
			this.coord = coord;
		}

		public int compare(Object arg0, Object arg1) {
			Coordinate c1 = ((Node) arg0).getCoordinate();
			Coordinate c2 = ((Node) arg1).getCoordinate();

			double d1 = c1.distance(coord);
			double d2 = c2.distance(coord);

			if (d1 < d2)
				return 1;
			if (d1 > d2)
				return -1;
			else
				return 0;
		}
	}
	
	
	public Object put(Object key, Object obj){
		if(! (key instanceof Coordinate) )
			return null;
		return super.put(new SnapCoordinate((Coordinate)key),
				obj);
	}
	
	public Object get(Object key){
		if(! (key instanceof Coordinate) )
			return null;
		return super.get(new SnapCoordinate((Coordinate)key));
	}
	
	public boolean containsKey(Object key){
		if(! (key instanceof Coordinate) )
			return false;
		return super.containsKey(new SnapCoordinate((Coordinate)key));
	}
	
	public static void main(String[] args){
		SnappingCoordinateMap map = 
			new SnappingCoordinateMap(0.1);
		Coordinate c0 = new Coordinate(0, 0);
		Coordinate c1 = new Coordinate(0.01, 0.01);
		Coordinate c2 = new Coordinate(0.31, 0.41);
		Coordinate c3 = new Coordinate(0.29, 0.39);
		Coordinate c4 = new Coordinate(0.299, 0.411);
		map.put(c0, c0);
		map.put(c1, c1);
		map.put(c2, c2);
		map.put(c3, c3);
		map.put(c4, c4);
		System.out.println(map.size());
		java.util.Set values = map.entrySet();
		System.out.println(values.size());
		
	}

}

