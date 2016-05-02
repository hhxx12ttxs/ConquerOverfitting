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
* $Id: SnapEdgeIntersectionList.java 8766 2006-11-15 04:11:32Z  $
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

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geomgraph.DirectedEdgeStar;
import com.vividsolutions.jts.geomgraph.Edge;
import com.vividsolutions.jts.geomgraph.EdgeEndStar;
import com.vividsolutions.jts.geomgraph.EdgeIntersection;
import com.vividsolutions.jts.geomgraph.EdgeIntersectionList;
import com.vividsolutions.jts.geomgraph.Label;
import com.vividsolutions.jts.geomgraph.Node;
import com.vividsolutions.jts.geomgraph.NodeFactory;

public class SnapEdgeIntersectionList extends EdgeIntersectionList {
		
	   class SnapEdgeIntersectNode extends Node implements Comparable{
		    EdgeIntersection edgeIntersection;
			public SnapEdgeIntersectNode(Coordinate arg0, 
									EdgeEndStar arg1,
									EdgeIntersection edgeIntersection) {
				super(arg0, arg1);
				this.edgeIntersection = edgeIntersection;
			}
			
			//Esto me vale para Nodos a secas, pero para el caso de intersecciones no....
			//porque tienen mas informacion (segmento asociado, etc.)
			//Por ejemplo, primer y ultimo punto de un poligono cerrado no deben ser el 
			//mismo nodo, sino nodos distintos
			
			public boolean equals(Object obj){
				SnapEdgeIntersectNode other = (SnapEdgeIntersectNode) obj;
				return (other.coord.distance(this.coord) 
									<= snapTolerance) &&
					   (other.edgeIntersection.segmentIndex == this.edgeIntersection.segmentIndex) &&
					   ( Math.abs(other.edgeIntersection.dist - this.edgeIntersection.dist) <= snapTolerance);
			}
			public int hashCode() {
			  return 1;   //esto no es eficiente 
			}
			
			//Esto es necesario porque a la hora de recorrer
			//las intersecciones de un Edge me interesa
			//que est?n ordenadas en el sentido de los vertices (0,1,2..etc)
			public int compareTo(Object arg0) {
				SnapEdgeIntersectNode other = (SnapEdgeIntersectNode)arg0;
				return this.edgeIntersection.compareTo(other.edgeIntersection);
			}
		}//EdgeIntersectionNode
		
	    SnappingNodeMap nodeMap = null;
	
	 // a Map <EdgeIntersection, EdgeIntersection>
	//TODO Ver si sustituimos por SnappingNodeMap
//	  private TreeMap nodeMap = new TreeMap();
	  SnappingEdge edge;  // the parent edge
	  
	  /*
	   * El codigo de verificacion de snap est? por todas partes.
	   * Llevar a una clase auxiliar si procede
	   * */
	  private double snapTolerance;

	  public SnapEdgeIntersectionList(SnappingEdge edge)
	  {
		  super(edge);
	      this.edge = edge;
	      this.snapTolerance = edge.getSnapTolerance();
	      nodeMap = new SnappingNodeMap(new NodeFactory(), snapTolerance){
	    	  public Node addNode(Node n)
	    	  {
	    		SnapEdgeIntersectNode newNode = (SnapEdgeIntersectNode)n;
	    	    SnapEdgeIntersectNode eNode = (SnapEdgeIntersectNode) 
	    	    	super.nodeMap.get(newNode);
	    	    if(eNode != null){
	    	    	eNode.mergeLabel(newNode);
	    	    	return eNode;
	    	    }else
	    	    	super.nodeMap.put(newNode, newNode);
	    	    return newNode;
	    	  }  
	    	  //TODO Esto hay que refinarlo. 
	    	  /*
	    	   * Por un lado quiero la rapidez del HashMap, y la posibilidad
	    	   * de resolver conflictos a partir de equals...
	    	   * 
	    	   * Pero cuando me pidan las intersecciones ordenadas, es 
	    	   * necesario hacerlo seg?n el sentido de los vertices
	    	   * (de mas cercana a mas lejana al vertice 0)
	    	   * 
	    	   * */
	    	  
	    	  public Iterator iterator()
	    	  {
	    		 long t0 = System.currentTimeMillis();
	    		 TreeMap ordered = new TreeMap(nodeMap);
	    		 long t1 = System.currentTimeMillis();
//	    		 System.out.println((t1-t0)+" en crear el Map");
	    		  return ordered.values().iterator();
	    	  }
	     
	      };
	  }

	  /**
	   * Adds an intersection into the list, if it isn't already there.
	   * The input segmentIndex and dist are expected to be normalized.
	   * @return the EdgeIntersection found or added
	   */
	  public EdgeIntersection add(Coordinate intPt,
			  	int segmentIndex, double dist)
	  {
		  EdgeIntersection eiNew = new EdgeIntersection(intPt, 
									    		segmentIndex, 
									    		dist);
		  
		  SnapEdgeIntersectNode newNode = new SnapEdgeIntersectNode(intPt,
				 new  DirectedEdgeStar(), eiNew);
		  SnapEdgeIntersectNode solution = 
			  (SnapEdgeIntersectNode) nodeMap.addNode(newNode);
	      return solution.edgeIntersection;
	  }

	  /**
	   * Returns an iterator of {@link EdgeIntersection}s
	   *
	   * @return an Iterator of EdgeIntersections
	   */
	  public Iterator iterator() { 
//		  return nodeMap.values().iterator();
		  return nodeMap.iterator();
	  }

	  /**
	   * Tests if the given point is an edge intersection
	   *
	   * @param pt the point to test
	   * @return true if the point is an intersection
	   */
	  public boolean isIntersection(Coordinate pt)
	  {
		  //TODO No seria mejor acudir al NodeMap???
	    for (Iterator it = iterator(); it.hasNext(); ) {
	      EdgeIntersection ei = ((SnapEdgeIntersectNode)it.next()).edgeIntersection;
	      if (ei.coord.distance(pt) <= snapTolerance)
	       return true;
	    }
	    return false;
	  }
	  
	  /**
	   * Creates new edges for all the edges that the intersections in this
	   * list split the parent edge into.
	   * Adds the edges to the input list (this is so a single list
	   * can be used to accumulate all split edges for a Geometry).
	   *
	   * @param edgeList a list of EdgeIntersections
	   */
	  public void addSplitEdges(List edgeList)
	  {
	    // ensure that the list has entries for the first and last point of the edge
	    addEndpoints();

	    Iterator it = iterator();
	    // there should always be at least two entries in the list
	    //es decir, todo Edge tendr? al menos 2 EdgeIntersection...sus nodos
	    EdgeIntersection eiPrev = ((SnapEdgeIntersectNode) it.next()).edgeIntersection;
	    
	    
//	    UNA DE LAS CLAVES EST? AQUI
//	    SI TENEMOS COORDENADA-EDGEINTERSECTION-COORDENADA Y EDGEINTERSECTION
//	    ES UN SNAP DE COORDENADA FINAL, APARECERAN COSAS SPUREAS.
	    
	    
	    
	    while (it.hasNext()) {
	      EdgeIntersection ei = ((SnapEdgeIntersectNode)it.next()).edgeIntersection;
	      Edge newEdge = this.createSplitEdge(eiPrev, ei);
	      edgeList.add(newEdge);

	      eiPrev = ei;
	    }
	  }
	  
	  public void addEndpoints()
	  {
	    int maxSegIndex = edge.getCoordinates().length - 1;
	    add(edge.getCoordinates()[0], 0, 0.0);
	    add(edge.getCoordinates()[maxSegIndex], maxSegIndex, 0.0);
	  }

	  /**
	   * Create a new "split edge" with the section of points between
	   * (and including) the two intersections.
	   * The label for the new edge is the same as the label for the parent edge.
	   */
	  Edge createSplitEdge(EdgeIntersection ei0, EdgeIntersection ei1)
	  {
//	Debug.print("\ncreateSplitEdge"); Debug.print(ei0); Debug.print(ei1);
	    int npts = ei1.segmentIndex - ei0.segmentIndex + 2;

	    Coordinate lastSegStartPt = this.edge.getCoordinate(ei1.segmentIndex);
	    // if the last intersection point is not equal to the its segment start pt,
	    // add it to the points list as well.
	    
	    
	    // (This check is needed because the distance metric is not totally reliable!)
	    // The check for point equality is 2D only - Z values are ignored
	   // boolean useIntPt1 = ei1.dist > 0.0 || ! ei1.coord.equals2D(lastSegStartPt);
	    boolean useIntPt1 = ei1.dist > snapTolerance || ! 
	    	(ei1.coord.distance(lastSegStartPt) <= snapTolerance);//Con esto se dejaria de usar el segundo punto...pero y el primero??? en el 1er segmento hay que considerarlo
	    if (! useIntPt1) {
	      npts--;
	    }
	    Coordinate[] pts = new Coordinate[npts];
	    int ipt = 0;
	    pts[ipt++] = new Coordinate(ei0.coord);
	    for (int i = ei0.segmentIndex + 1; i <= ei1.segmentIndex; i++) {
	      pts[ipt++] = this.edge.getCoordinate(i);
	    }
	    if (useIntPt1) pts[ipt] = ei1.coord;
	    return new SnappingEdge(pts, new Label(this.edge.getLabel()), snapTolerance);
	  }

}


