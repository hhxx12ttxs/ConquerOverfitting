/*
 * Created on 20-oct-2006
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
* $Id: TurnUtil.java 8766 2006-11-15 04:11:32Z  $
* $Log$
* Revision 1.4  2006-10-26 11:42:42  fjp
* Ya pita, ya.
*
* Revision 1.3  2006/10/25 15:51:20  fjp
* por terminar lo de los giros
*
* Revision 1.2  2006/10/23 18:51:42  azabala
* *** empty log message ***
*
* Revision 1.1  2006/10/20 19:54:01  azabala
* *** empty log message ***
*
*
*/
package com.iver.cit.gvsig.graph.core;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

public class TurnUtil {
	public static final int TURN_RIGHT = 0;
	public static final int TURN_LEFT = 1;
	public static final int GO_STRAIGH_ON = 2;
	public static final int TURN_U = 4;
	
	
	public static boolean checkIsLine(Geometry geometry){
		if(geometry instanceof LineString || geometry instanceof MultiLineString)
			return true;
		else
			return false;
		
	}	
	
	
	
	public static final int getDirection(IFeature feature1, IFeature feature2){
		Geometry geom1 = feature1.getGeometry().toJTSGeometry();
		Geometry geom2 = feature2.getGeometry().toJTSGeometry();
		if(!checkIsLine(geom1))
			return -1;
		if(!checkIsLine(geom2))
			return -1;
	    Coordinate[] coords1 = geom1.getCoordinates();
	    Coordinate p0 = coords1[coords1.length -2];
	    Coordinate p1 = coords1[coords1.length -1];
	    Coordinate[] coords2 = geom2.getCoordinates();
	    Coordinate p2 = coords2[1];
	    
	     double deegreeAngle = angle(p0, p1, p2);
	         
	     if(Math.abs(deegreeAngle) <= 30)
	    	 return GO_STRAIGH_ON;
	     else if(deegreeAngle > 180)
	    	 return TURN_LEFT;
	     else
	    	 return TURN_RIGHT;
	}	
	public static double angle(Coordinate c1, Coordinate c2, Coordinate c3) {
		double resul = 0.0;
		// Normalizamos:
		Coordinate origin = new Coordinate(0.0, 0.0);
		Coordinate cAux1 = new Coordinate(c2.x-c1.x, c2.y-c1.y);
		Coordinate cAux2 = new Coordinate(c3.x-c2.x, c3.y-c2.y);
		LineSegment v1 = new LineSegment(origin, cAux1);
		LineSegment v2 = new LineSegment(origin, cAux2);
		double prodEscalar = cAux1.x*cAux2.x + cAux1.y*cAux2.y;
		double cosAlpha = prodEscalar / (v1.getLength() * v2.getLength()) ; 
		resul = Math.toDegrees(Math.acos(cosAlpha));
		
        if (cAux1.x * cAux2.y > cAux1.y * cAux2.x) {
            resul = 360 - resul;
        }

//		System.out.println("angulo = " + resul);
		return resul;
	}
	
	/**
	 * 
	 * Code extracted from JUMP
	 * 
	   * Returns the angle between two vectors.
	   * @param a1 the angle of one vector, between -Pi and Pi
	   * @param a2 the angle of the other vector, between -Pi and Pi
	   * @return the angle (in radians) between the two vectors, between 0 and Pi
	   */
//	  public static double diff(double a1, double a2) {
//	      double da;
//
//	      if (a1 < a2) {
//	          da = a2 - a1;
//	      } else {
//	          da = a1 - a2;
//	      }
//
//	      if (da > Math.PI) {
//	          da = (2 * Math.PI) - da;
//	      }
//	      return da;
//	  }
}


