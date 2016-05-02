/*
 * Created on 02-oct-2006
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
* $Id: SnapDirectedEdge.java 8766 2006-11-15 04:11:32Z  $
* $Log$
* Revision 1.1  2006-10-17 18:25:53  azabala
* *** empty log message ***
*
* Revision 1.2  2006/10/09 19:10:56  azabala
* First version in CVS
*
* Revision 1.1  2006/10/05 19:20:57  azabala
* first version in cvs
*
* Revision 1.1  2006/10/02 19:06:39  azabala
* *** empty log message ***
*
*
*/
package com.vividsolutions.jts.geomgraph;

import com.vividsolutions.jts.algorithms.SnapCGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geomgraph.DirectedEdge;
import com.vividsolutions.jts.geomgraph.Edge;
import com.vividsolutions.jts.geomgraph.Quadrant;
import com.vividsolutions.jts.util.Assert;

public class SnapDirectedEdge extends DirectedEdge {

	private Coordinate p0;
	private Coordinate p1;
	private double dx, dy;
	int quadrant;
	
	public SnapDirectedEdge(Edge arg0, boolean arg1) {
		super(arg0, arg1);
	}
	 
	public Coordinate getCoordinate() { return p0; }
	  public Coordinate getDirectedCoordinate() { return p1; }
	  public int getQuadrant() { return quadrant; }
	  public double getDx() { return dx; }
	  public double getDy() { return dy; }

 protected void init(Coordinate p0, Coordinate p1)
  {
    this.p0 = p0;
    this.p1 = p1;
    dx = p1.x - p0.x;
    dy = p1.y - p0.y;
    if(dx == 0 && dy == 0)
    	System.out.println(p0.toString()+";"+p1.toString());
    quadrant = Quadrant.quadrant(dx, dy);
   
    Assert.isTrue(! (dx == 0 && dy == 0), "EdgeEnd with identical endpoints found");
  }
 
 public String toString(){
	 return this.p0.toString() + "," + p1.toString();
 }
 
 public int compareTo(Object obj)
 {
     SnapDirectedEdge de = (SnapDirectedEdge) obj;
     return compareDirection(de);
 }

 /**
  * Returns 1 if this DirectedEdge has a greater angle with the
  * positive x-axis than b", 0 if the DirectedEdges are collinear, and -1 otherwise.
  * <p>
  * Using the obvious algorithm of simply computing the angle is not robust,
  * since the angle calculation is susceptible to roundoff. A robust algorithm
  * is:
  * <ul>
  * <li>first compare the quadrants. If the quadrants are different, it it
  * trivial to determine which vector is "greater".
  * <li>if the vectors lie in the same quadrant, the robust
  * {@link RobustCGAlgorithms#computeOrientation(Coordinate, Coordinate, Coordinate)}
  * function can be used to decide the relative orientation of the vectors.
  * </ul>
  */
 public int compareDirection(SnapDirectedEdge e)
 {
   // if the rays are in different quadrants, determining the ordering is trivial
   if (quadrant > e.quadrant) return 1;
   if (quadrant < e.quadrant) return -1;
   // vectors are in the same quadrant - check relative orientation of direction vectors
   // this is > e if it is CCW of e
   return SnapCGAlgorithms.computeOrientation(e.p0, e.p1, p1);
 }
 
   
}


