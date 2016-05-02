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
package com.iver.cit.gvsig.graphtests;

import org.gvsig.graph.core.TurnUtil;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

import junit.framework.TestCase;

public class TestAngle extends TestCase {
	
	public void testAngle() {
		Coordinate c1, c2, c3;
		double grados;
		
		// 0 grados (no hay giro
		c1 = new Coordinate(-1, 0);
		c2 = new Coordinate(0, 0);
		c3 = new Coordinate(1, 0);
		grados = TurnUtil.angle(c1, c2, c3);
		assertEquals(0.0, grados, 0.0);
		
		
		// 90 grados (giro a la izquierda)
		c1 = new Coordinate(0, -1);
		c2 = new Coordinate(0, 0);
		c3 = new Coordinate(-1, 0);
		grados = TurnUtil.angle(c1, c2, c3);
		assertEquals(90.0, grados, 0.0);

		
		// 180 grados (360 grados) giro completo
		c1 = new Coordinate(-1, 0);
		c2 = new Coordinate(0, 0);
		c3 = new Coordinate(-1, 0);
		grados = TurnUtil.angle(c1, c2, c3);
		assertEquals(180.0, grados, 0.0);
		
		// 270 grados (giro a la derecha)
		c1 = new Coordinate(-1, 0);
		c2 = new Coordinate(0, 0);
		c3 = new Coordinate(0, 1);
		grados = TurnUtil.angle(c1, c2, c3);
		assertEquals(270.0, grados, 0.0);
		
	}
	
	private double angle(Coordinate c1, Coordinate c2, Coordinate c3) {
		double resul = 0.0;
		// Normalizamos:
		Coordinate origin = new Coordinate(0.0, 0.0);
		Coordinate cAux1 = new Coordinate(c2.x-c1.x, c2.y-c1.y);
		Coordinate cAux2 = new Coordinate(c3.x-c2.x, c3.y-c2.y);
		LineSegment v1 = new LineSegment(origin, cAux1);
		LineSegment v2 = new LineSegment(origin, cAux2);
		double jtsResul = CGAlgorithms.orientationIndex(c1, c2, c3);
		double prodEscalar = cAux1.x*cAux2.x + cAux1.y*cAux2.y;
		double cosAlpha = prodEscalar / (v1.getLength() * v2.getLength()) ; 
		resul = Math.toDegrees(Math.acos(cosAlpha));
		
        if (cAux1.x * cAux2.y > cAux1.y * cAux2.x) {
            resul = 360 - resul;
        }

		System.out.println(resul);
		return resul;
	}

}



