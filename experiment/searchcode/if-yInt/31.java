/*
 * Created on 10-abr-2006
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
 * $Id: 
 * $Log: 
 */
package org.gvsig.fmap.core;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.gvsig.jts.JtsUtil;

import com.iver.cit.gvsig.fmap.core.FGeometry;
import com.iver.cit.gvsig.fmap.core.FGeometryCollection;
import com.iver.cit.gvsig.fmap.core.FMultiPoint2D;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FPolygon2D;
import com.iver.cit.gvsig.fmap.core.FPolyline2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.v02.FConverter;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.RobustCGAlgorithms;
import com.vividsolutions.jts.algorithms.SnapCGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * Offers the same functionality than
 * com.iver.cit.gvsig.fmap.core.v02.FConverter, but doesnt do some checks that
 * FConverter does (for example, it doesnt close unclosed Polygons).
 * 
 * Also, it works with Jts.GEOMETRY_FACTORY, a global geometry factory which
 * works with a precision model selected by user. Precision Model is very important
 * to determine the number of significant digits of geometries, and to avoid precision problems.
 * 
 * @author Alvaro Zabala
 * 
 */
public class NewFConverter extends FConverter {

	public static CGAlgorithms cga = new RobustCGAlgorithms();

	/**
	 * Es la m?xima distancia que permitimos que el trazo aproximado difiera del
	 * trazo real.
	 */
	public static double FLATNESS = 0.8;// Por ejemplo. Cuanto m?s peque?o, m?s

	// segmentos necesitar? la curva

	// returns true if testPoint is a point in the pointList list.
	static boolean pointInList(Coordinate testPoint, Coordinate[] pointList) {
		int t;
		int numpoints;
		Coordinate p;

		numpoints = Array.getLength(pointList);

		for (t = 0; t < numpoints; t++) {
			p = pointList[t];

			if ((testPoint.x == p.x)
					&& (testPoint.y == p.y)
					&& ((testPoint.z == p.z) || (!(testPoint.z == testPoint.z)))) 
			{
				return true;
			}
		}

		return false;
	}
	
	private static Point createPoint(FShape shp){
		FPoint2D p = (FPoint2D) shp;
		Coordinate coord = new Coordinate(p.getX(), p.getY());
		return JtsUtil.GEOMETRY_FACTORY.createPoint(coord);
	}
	
	
	private static MultiLineString createLineString(FShape shp){
		LineString lin = null;
		ArrayList<LineString> arrayLines = new ArrayList<LineString>();
		ArrayList<Coordinate> arrayCoords = null;
		double[] theData = new double[6];
		int numParts = 0;
		
		PathIterator theIterator = shp.getPathIterator(null, FLATNESS);
		while (!theIterator.isDone()) {
			int theType = theIterator.currentSegment(theData);
			switch (theType) {
			case PathIterator.SEG_MOVETO:
				if (arrayCoords == null) {
					arrayCoords = new ArrayList<Coordinate>();
				} else {
					lin = JtsUtil.GEOMETRY_FACTORY.createLineString(
							CoordinateArrays.toCoordinateArray(arrayCoords));
					arrayLines.add(lin);
					arrayCoords.clear();
				}
				numParts++;
				arrayCoords.add(new Coordinate(theData[0], theData[1]));
				break;

			case PathIterator.SEG_LINETO:
				arrayCoords.add(new Coordinate(theData[0], theData[1]));
				break;

			case PathIterator.SEG_CLOSE:
				Coordinate firstCoord = (Coordinate) arrayCoords.get(0);
				arrayCoords.add(new Coordinate(firstCoord.x, firstCoord.y));
				break;
			} // end switch
			theIterator.next();
		} // end while loop
		lin = JtsUtil.GEOMETRY_FACTORY.createLineString(CoordinateArrays
				.toCoordinateArray(arrayCoords));
		arrayLines.add(lin);
		return JtsUtil.GEOMETRY_FACTORY.createMultiLineString(GeometryFactory
				.toLineStringArray(arrayLines));
	}
	
	
	private static MultiPolygon createMultiPolygonFromRings(List<LinearRing> holes, List<LinearRing> shells){
		
		ArrayList<ArrayList<LinearRing>> holesForShells = 
			new ArrayList<ArrayList<LinearRing>>(shells.size());
		
		for (int i = 0; i < shells.size(); i++) {
			holesForShells.add(new ArrayList<LinearRing>());
		}

		for (int i = 0; i < holes.size(); i++) {//for each hole
			LinearRing testRing = (LinearRing) holes.get(i);
			LinearRing minShell = null;
			Envelope minEnv = null;
			
			
			Envelope testEnv = testRing.getEnvelopeInternal();
			Coordinate testPt = testRing.getCoordinateN(0);
			
			LinearRing tryRing = null;
			for (int j = 0; j < shells.size(); j++) {
				tryRing = (LinearRing) shells.get(j);

				Envelope tryEnv = tryRing.getEnvelopeInternal();

				if (minShell != null) {
					minEnv = minShell.getEnvelopeInternal();
				}

				boolean isContained = false;
				Coordinate[] coordList = tryRing.getCoordinates();

				if (tryEnv.contains(testEnv)
						&& (CGAlgorithms.isPointInRing(testPt, coordList) || (pointInList(
								testPt, coordList)))) {
					isContained = true;
				}

				// check if this new containing ring is smaller than the
				// current minimum ring
				if (isContained) {
					if ((minShell == null) || minEnv.contains(tryEnv)) {
						minShell = tryRing;
					}
				}
			}

			if (minShell == null) {
				// System.out.println(
				// "polygon found with a hole thats not inside a shell");
				// azabala: we do the assumption that this hole is really a
				// shell (polygon)
				// whose point werent digitized in the right order
				Coordinate[] cs = testRing.getCoordinates();
				Coordinate[] reversed = new Coordinate[cs.length];
				int pointIndex = 0;
				for (int z = cs.length - 1; z >= 0; z--) {
					reversed[pointIndex] = cs[z];
					pointIndex++;
				}
				LinearRing newRing = JtsUtil.GEOMETRY_FACTORY.createLinearRing(reversed);
				shells.add(newRing);
				holesForShells.add(new ArrayList<LinearRing>());
			} else {
				((ArrayList) holesForShells.get(shells.indexOf(minShell)))
						.add(testRing);
			}
		}//holes
		
		

		Polygon[] polygons = new Polygon[shells.size()];
		for (int i = 0; i < shells.size(); i++) {
			polygons[i] = JtsUtil.GEOMETRY_FACTORY.createPolygon((LinearRing) shells
					.get(i), (LinearRing[]) ((ArrayList) holesForShells
					.get(i)).toArray(new LinearRing[0]));
		}
		
		return JtsUtil.GEOMETRY_FACTORY.createMultiPolygon(polygons);
	}
	
	private static MultiPolygon createPolygon(FShape shp) throws IncorrectJtsGeometryException{
		ArrayList<LinearRing> shells = new ArrayList<LinearRing>();
		ArrayList<LinearRing> holes = new ArrayList<LinearRing>();
		ArrayList<Coordinate> arrayCoords = null;
		Coordinate[] points = null;
		double[] theData = new double[6];
	
		PathIterator theIterator = shp.getPathIterator(null, FLATNESS);
		while (!theIterator.isDone()) {
			int theType = theIterator.currentSegment(theData);
			switch (theType) {
			case PathIterator.SEG_MOVETO:

				// System.out.println("SEG_MOVETO");
				if (arrayCoords == null) {
					arrayCoords = new ArrayList<Coordinate>();
				} else {
					points = CoordinateArrays.toCoordinateArray(arrayCoords);
					try {
						LinearRing ring = JtsUtil.GEOMETRY_FACTORY.createLinearRing(points);
						//TODO VER SI SE PUEDE MODIFICAR EL CRITERIO DE ASIGNACION DE HOLES
						if (CGAlgorithms.isCCW(points)) {
							holes.add(ring);
						} else {
							shells.add(ring);
						}
					} catch (Exception e) {
						throw new IncorrectJtsGeometryException(e, "No se pudo construir la geometria JTS");
					}//catch exception
					arrayCoords.clear();
				}
				arrayCoords.add(new Coordinate(theData[0], theData[1]));
				break;

			case PathIterator.SEG_LINETO:
				arrayCoords.add(new Coordinate(theData[0], theData[1]));
				break;

			case PathIterator.SEG_CLOSE:
			    //if the iterator has SEG_CLOSE, we close the ring
				Coordinate firstCoord = (Coordinate) arrayCoords.get(0);
				arrayCoords.add(new Coordinate(firstCoord.x, firstCoord.y));
				break;
			} // end switch
			theIterator.next();
		} // end while loop

//we dont close rings without SEG_CLOSE flag		
//		arrayCoords.add(arrayCoords.get(0));
		
		points = CoordinateArrays.toCoordinateArray(arrayCoords);
		try {
			LinearRing ring = JtsUtil.GEOMETRY_FACTORY.createLinearRing(points);
			//TODO VER SI SE PUEDE MODIFICAR EL CRITERIO DE ASIGNACION DE HOLES
			if (CGAlgorithms.isCCW(points)) {
				holes.add(ring);
			} else {
				shells.add(ring);
			}
		} catch (Exception e) {
			throw new IncorrectJtsGeometryException(e, "No se pudo construir la geometria JTS");
		}

		return createMultiPolygonFromRings(holes, shells);
		
		
	}
	
	public static Geometry createLinStr(FShape shp){
		return createLineString(shp);
	}

	/**
	 * Convierte un FShape a una Geometry del JTS. Para ello, utilizamos un
	 * "flattened PathIterator". El flattened indica que las curvas las pasa a
	 * segmentos de l?nea recta AUTOMATICAMENTE!!!.
	 * 
	 * @param shp
	 *            FShape que se quiere convertir.
	 * 
	 * @return Geometry de JTS.
	 */
	public static Geometry java2d_to_jts(FShape shp) throws IncorrectJtsGeometryException{

		Geometry geoJTS = null;
		
		
		ArrayList<Coordinate> arrayCoords = null;
	
		int theType;
		int numParts = 0;
		double[] theData = new double[6];
		PathIterator theIterator;

		
		switch (shp.getShapeType()) {
		case FShape.POINT:
		case FShape.POINT + FShape.Z:
			geoJTS = createPoint(shp);
			break;
		case FShape.LINE:
		case FShape.ARC:
		case FShape.LINE + FShape.Z:
			geoJTS = createLineString(shp);
			break;

		case FShape.POLYGON:
		case FShape.CIRCLE:
		case FShape.ELLIPSE:
		case FShape.POLYGON + FShape.Z:
			geoJTS = createPolygon(shp);
			break;
		}

		return geoJTS;
	}

	// private Geometry getJtsPolygon(FShape shape) {
	// Geometry geoJTS = null;
	// Coordinate coord;
	// Coordinate[] coords;
	// ArrayList arrayCoords = null;
	// ArrayList arrayLines = new ArrayList();
	// LineString lin;
	// LinearRing linRing;
	// LinearRing linRingExt = null;
	// int theType;
	// int numParts = 0;
	//
	// // Use this array to store segment coordinate data
	// double[] theData = new double[6];
	// PathIterator theIterator;
	// ArrayList shells = new ArrayList();
	// ArrayList holes = new ArrayList();
	// Coordinate[] points = null;
	//
	// theIterator = shape.getPathIterator(null, FLATNESS);
	//
	// while (!theIterator.isDone()) {
	// theType = theIterator.currentSegment(theData);
	// switch (theType) {
	// case PathIterator.SEG_MOVETO:// Se trata de la primera parte de
	// // un posible poligono multiparte
	//
	// if (arrayCoords == null) {
	// arrayCoords = new ArrayList();
	// } else {
	// points = CoordinateArrays.toCoordinateArray(arrayCoords);
	// try {// Se trata de la segunda parte de un pol?gono
	// // multiparte
	//
	// LinearRing ring = geomFactory.createLinearRing(points);
	// // AQUI LA DETERMINACION DE HUECO O BORDE SE HACE EN
	// // FUNCION DEL SENTIDO
	// // DE LOS PUNTOS, Y ESTE PUEDE VARIAR CON EL FORMATO
	// // (AZABALA)
	// if (CGAlgorithms.isCCW(points)) {
	// holes.add(ring);// Si el shp viene de un formato que
	// // no sea SHP, como WKT, estamos
	// // a?adiendo
	// // un borde a la colecci?n de huecos
	// } else {
	// shells.add(ring);
	// }
	//
	// } catch (Exception e) {
	// /*
	// * (jaume) caso cuando todos los puntos son iguales
	// * devuelvo el propio punto
	// */
	// boolean same = true;
	// for (int i = 0; i < points.length - 1 && same; i++) {
	// if (points[i].x != points[i + 1].x
	// || points[i].y != points[i + 1].y /*
	// * ||
	// * points[i].z !=
	// * points[i+1].z
	// */
	// )
	// same = false;
	// }
	// if (same)
	// return geomFactory.createPoint(points[0]);
	// /*
	// * caso cuando es una l?nea de 3 puntos, no creo un
	// * LinearRing, sino una linea
	// */
	// if (points.length > 1 && points.length <= 3)
	// // return geomFactory.createLineString(points);
	// return geomFactory
	// .createMultiLineString(new LineString[] { geomFactory
	// .createLineString(points) });
	//
	// System.err
	// .println("Caught Topology exception in GMLLinearRingHandler");
	//
	// return null;
	// }// catch
	//
	// arrayCoords = new ArrayList();
	// }// else
	//
	// numParts++;
	// arrayCoords.add(new Coordinate(theData[0], theData[1]));
	// break;
	//
	// case PathIterator.SEG_LINETO:
	// arrayCoords.add(new Coordinate(theData[0], theData[1]));
	// break;
	//
	// case PathIterator.SEG_QUADTO:
	// break;
	//
	// case PathIterator.SEG_CUBICTO:
	// break;
	//
	// case PathIterator.SEG_CLOSE:
	// // A?adimos el primer punto para cerrar.
	// Coordinate firstCoord = (Coordinate) arrayCoords.get(0);
	// arrayCoords.add(new Coordinate(firstCoord.x, firstCoord.y));
	// break;
	// } // end switch
	// theIterator.next();
	// } // end while loop
	//
	// // DUDA: Esto solo habr?a que hacerlo en el caso de que no se invoque a
	// // PathIterator.SEG_CLOSE
	// arrayCoords.add(arrayCoords.get(0));
	//
	// points = CoordinateArrays.toCoordinateArray(arrayCoords);
	// try {
	// LinearRing ring = geomFactory.createLinearRing(points);
	// if (CGAlgorithms.isCCW(points)) {
	// holes.add(ring);
	// } else {
	// shells.add(ring);
	// }
	// } catch (Exception e) {
	// /*
	// * (jaume) caso cuando todos los puntos son iguales devuelvo el
	// * propio punto
	// */
	// boolean same = true;
	// for (int i = 0; i < points.length - 1 && same; i++) {
	// if (points[i].x != points[i + 1].x
	// || points[i].y != points[i + 1].y /*
	// * || points[i].z !=
	// * points[i+1].z
	// */
	// )
	// same = false;
	// }
	// if (same)
	// return geomFactory.createPoint(points[0]);
	// /*
	// * caso cuando es una l?nea de 3 puntos, no creo un LinearRing, sino
	// * una linea
	// */
	// if (points.length > 1 && points.length <= 3)
	// // return geomFactory.createLineString(points);
	// return geomFactory
	// .createMultiLineString(new LineString[] { geomFactory
	// .createLineString(points) });
	// System.err
	// .println("Caught Topology exception in GMLLinearRingHandler");
	//
	// return null;
	// }
	//
	// /*
	// * a partir de aqu? todo est? mal, porque no se ha tenido en cuenta el
	// * criterio para determinar lo que es un HOLE y lo que es un SHELL (el
	// * orden de los puntos CCW y CW var?a en funci?n del formato).
	// *
	// */
	// // now we have a list of all shells and all holes
	// ArrayList holesForShells = new ArrayList(shells.size());
	// for (int i = 0; i < shells.size(); i++) {
	// holesForShells.add(new ArrayList());
	// }
	//
	// // find homes
	// for (int i = 0; i < holes.size(); i++) {
	// LinearRing hole = (LinearRing) holes.get(i);
	//
	// LinearRing minimalShell = null;
	// Envelope minimalShellEnvelope = null;
	//
	// Envelope currentHoleEnvelope = hole.getEnvelopeInternal();
	// Coordinate currentHoleCoordinate = hole.getCoordinateN(0);
	//
	// LinearRing shellCandidate = null;
	//
	// for (int j = 0; j < shells.size(); j++) {
	// shellCandidate = (LinearRing) shells.get(j);
	//
	// Envelope shellCandidateEnvelope = shellCandidate
	// .getEnvelopeInternal();
	// if (minimalShell != null) {
	// minimalShellEnvelope = minimalShell.getEnvelopeInternal();
	// }
	//
	// boolean isContained = false;
	// Coordinate[] coordList = shellCandidate.getCoordinates();
	//
	// if (shellCandidateEnvelope.contains(currentHoleEnvelope)
	// && (CGAlgorithms.isPointInRing(currentHoleCoordinate,
	// coordList) || (pointInList(
	// currentHoleCoordinate, coordList)))) {
	// isContained = true;
	// }
	//
	// // check if this new containing ring is smaller than the current
	// // minimum ring
	// if (isContained) {
	// if ((minimalShell == null)
	// || minimalShellEnvelope
	// .contains(shellCandidateEnvelope)) {
	// minimalShell = shellCandidate;
	// }// if minimalShell
	// }// if isContained
	// }// for j
	//
	// if (minimalShell == null) {
	// /*
	// * Llegados a este punto, tenemos un HOLE para el que no se ha
	// * encontrado SHELL que lo contenga.
	// *
	// *
	// *
	// */
	// // azabala: we do the assumption that this hole is really a
	// // shell (polygon)
	// // whose point werent digitized in the right order
	// Coordinate[] cs = hole.getCoordinates();
	// Coordinate[] reversed = new Coordinate[cs.length];
	// int pointIndex = 0;
	// for (int z = cs.length - 1; z >= 0; z--) {
	// reversed[pointIndex] = cs[z];
	// pointIndex++;
	// }
	// LinearRing newRing = geomFactory.createLinearRing(reversed);
	// shells.add(newRing);
	// holesForShells.add(new ArrayList());
	// } else {
	// ((ArrayList) holesForShells.get(shells.indexOf(minimalShell)))
	// .add(hole);
	// }// if minShell
	//
	// }// for i
	//
	// Polygon[] polygons = new Polygon[shells.size()];
	//
	// for (int i = 0; i < shells.size(); i++) {
	// polygons[i] = geomFactory.createPolygon((LinearRing) shells.get(i),
	// (LinearRing[]) ((ArrayList) holesForShells.get(i))
	// .toArray(new LinearRing[0]));
	// }
	// // CAMBIO: ENTREGAMOS SIEMPRE MULTILINESTRING, QUE ES
	// // LO QUE HACE TODO EL MUNDO CUANDO ESCRIBE EN POSTGIS
	// // O CON GEOTOOLS
	// // if (numParts > 1) // Generamos una MultiLineString
	//
	// /*
	// * if (polygons.length == 1) { return polygons[0]; }
	// */
	//
	// // FIN CAMBIO
	// holesForShells = null;
	// shells = null;
	// holes = null;
	//
	// // its a multi part
	// geoJTS = geomFactory.createMultiPolygon(polygons);
	// return geoJTS;
	// }

	private static GeneralPathX toShape(Polygon p) {
		GeneralPathX resul = new GeneralPathX();
		Coordinate coord;

		for (int i = 0; i < p.getExteriorRing().getNumPoints(); i++) {
			coord = p.getExteriorRing().getCoordinateN(i);

			if (i == 0) {
				resul.moveTo(coord.x, coord.y);
			} else {
				resul.lineTo(coord.x, coord.y);
			}
		}

		for (int j = 0; j < p.getNumInteriorRing(); j++) {
			LineString hole = p.getInteriorRingN(j);

			for (int k = 0; k < hole.getNumPoints(); k++) {
				coord = hole.getCoordinateN(k);

				if (k == 0) {
					resul.moveTo(coord.x, coord.y);
				} else {
					resul.lineTo(coord.x, coord.y);
				}
			}
		}

		return resul;
	}

	// AZABALA: Revisar si el closePath nos afecta
	public static GeneralPathX transformToInts(GeneralPathX gp,
			AffineTransform at) {
		GeneralPathX newGp = new GeneralPathX();
		PathIterator theIterator;
		int theType;
		int numParts = 0;
		double[] theData = new double[6];
		Point2D ptDst = new Point2D.Double();
		Point2D ptSrc = new Point2D.Double();
		boolean bFirst = true;
		int xInt, yInt, antX = -1, antY = -1;

		theIterator = gp.getPathIterator(null); // , flatness);

		while (!theIterator.isDone()) {
			theType = theIterator.currentSegment(theData);
			switch (theType) {
			case PathIterator.SEG_MOVETO:
				numParts++;
				ptSrc.setLocation(theData[0], theData[1]);
				at.transform(ptSrc, ptDst);
				antX = (int) ptDst.getX();
				antY = (int) ptDst.getY();
				newGp.moveTo(antX, antY);
				bFirst = true;
				break;

			case PathIterator.SEG_LINETO:
				ptSrc.setLocation(theData[0], theData[1]);
				at.transform(ptSrc, ptDst);
				xInt = (int) ptDst.getX();
				yInt = (int) ptDst.getY();
				if ((bFirst) || ((xInt != antX) || (yInt != antY))) {
					newGp.lineTo(xInt, yInt);
					antX = xInt;
					antY = yInt;
					bFirst = false;
				}
				break;

			case PathIterator.SEG_QUADTO:
				System.out.println("Not supported here");

				break;

			case PathIterator.SEG_CUBICTO:
				System.out.println("Not supported here");

				break;

			case PathIterator.SEG_CLOSE:
				newGp.closePath();

				break;
			} // end switch

			theIterator.next();
		} // end while loop

		return newGp;
	}

	// Revisar si el SEG_CLOSE puede afectar negativamente a las reglas
	// topologicas
	public static FShape transformToInts(IGeometry gp, AffineTransform at) {
		GeneralPathX newGp = new GeneralPathX();
		double[] theData = new double[6];
		double[] aux = new double[6];

		// newGp.reset();
		PathIterator theIterator;
		int theType;
		int numParts = 0;

		Point2D ptDst = new Point2D.Double();
		Point2D ptSrc = new Point2D.Double();
		boolean bFirst = true;
		int xInt, yInt, antX = -1, antY = -1;

		theIterator = gp.getPathIterator(null); // , flatness);
		int numSegmentsAdded = 0;
		while (!theIterator.isDone()) {
			theType = theIterator.currentSegment(theData);

			switch (theType) {
			case PathIterator.SEG_MOVETO:
				numParts++;
				ptSrc.setLocation(theData[0], theData[1]);
				at.transform(ptSrc, ptDst);
				antX = (int) ptDst.getX();
				antY = (int) ptDst.getY();
				newGp.moveTo(antX, antY);
				numSegmentsAdded++;
				bFirst = true;
				break;

			case PathIterator.SEG_LINETO:
				ptSrc.setLocation(theData[0], theData[1]);
				at.transform(ptSrc, ptDst);
				xInt = (int) ptDst.getX();
				yInt = (int) ptDst.getY();
				if ((bFirst) || ((xInt != antX) || (yInt != antY))) {
					newGp.lineTo(xInt, yInt);
					antX = xInt;
					antY = yInt;
					bFirst = false;
					numSegmentsAdded++;
				}
				break;

			case PathIterator.SEG_QUADTO:
				at.transform(theData, 0, aux, 0, 2);
				newGp.quadTo(aux[0], aux[1], aux[2], aux[3]);
				numSegmentsAdded++;
				break;

			case PathIterator.SEG_CUBICTO:
				at.transform(theData, 0, aux, 0, 3);
				newGp.curveTo(aux[0], aux[1], aux[2], aux[3], aux[4], aux[5]);
				numSegmentsAdded++;
				break;

			case PathIterator.SEG_CLOSE:
				if (numSegmentsAdded < 3)
					newGp.lineTo(antX, antY);
				newGp.closePath();

				break;
			} // end switch

			theIterator.next();
		} // end while loop

		FShape shp = null;
		switch (gp.getGeometryType()) {
		case FShape.POINT: // Tipo punto
		case FShape.POINT + FShape.Z:
			shp = new FPoint2D(ptDst.getX(), ptDst.getY());
			break;

		case FShape.LINE:
		case FShape.LINE + FShape.Z:
		case FShape.ARC:
			shp = new FPolyline2D(newGp);
			break;

		case FShape.POLYGON:
		case FShape.POLYGON + FShape.Z:
		case FShape.CIRCLE:
		case FShape.ELLIPSE:

			shp = new FPolygon2D(newGp);
			break;
		}
		return shp;
	}

	/**
	 * Return a correct polygon (no hole)
	 * 
	 * @param coordinates
	 * @return
	 */
	// FIXME -> Si introducimos un criterio de sentido (CW, CCW) esto
	// tb debe cambiar
	public static IGeometry getExteriorPolygon(Coordinate[] coordinates) {
		// isCCW = true => it's a hole
		Coordinate[] vs = new Coordinate[coordinates.length];
		if (CGAlgorithms.isCCW(coordinates)) {
			for (int i = vs.length - 1; i >= 0; i--) {
				vs[i] = coordinates[i];
			}
		} else {
			vs = coordinates;
		}
		LinearRing ring = JtsUtil.GEOMETRY_FACTORY.createLinearRing(vs);

		try {
			return ShapeFactory.createPolygon2D(toShape(ring));
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Return a hole (CCW ordered points)
	 * 
	 * @param coordinates
	 * @return
	 */

	// FIXME -> Si introducimos un criterio de sentido (CW, CCW) esto
	// tb debe cambiar
	public static IGeometry getHole(Coordinate[] coordinates) {
		// isCCW = true => it's a hole
		Coordinate[] vs = new Coordinate[coordinates.length];
		if (CGAlgorithms.isCCW(coordinates)) {
			vs = coordinates;
		} else {
			for (int i = vs.length - 1; i >= 0; i--) {
				vs[i] = coordinates[i];
			}
		}
		LinearRing ring = JtsUtil.GEOMETRY_FACTORY.createLinearRing(vs);
		try {
			return ShapeFactory.createPolygon2D(toShape(ring));
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Use it ONLY for NOT multipart polygons.
	 * 
	 * @param pol
	 * @return
	 */

	// FIXME Este hay que sobreescribirlo
	public static IGeometry getNotHolePolygon(FPolygon2D pol) {
		// isCCW == true => hole
		Coordinate[] coords;
		ArrayList arrayCoords = null;
		int theType;
		int numParts = 0;

		// Use this array to store segment coordinate data
		double[] theData = new double[6];
		PathIterator theIterator;

		ArrayList shells = new ArrayList();
		ArrayList holes = new ArrayList();
		Coordinate[] points = null;

		theIterator = pol.getPathIterator(null, FLATNESS);

		while (!theIterator.isDone()) {
			// while not done
			theType = theIterator.currentSegment(theData);

			// Populate a segment of the new
			// GeneralPathX object.
			// Process the current segment to populate a new
			// segment of the new GeneralPathX object.
			switch (theType) {
			case PathIterator.SEG_MOVETO:

				// System.out.println("SEG_MOVETO");
				if (arrayCoords == null) {
					arrayCoords = new ArrayList();
				} else {
					points = CoordinateArrays.toCoordinateArray(arrayCoords);

					try {
						LinearRing ring = JtsUtil.GEOMETRY_FACTORY.createLinearRing(points);

						if (CGAlgorithms.isCCW(points)) {
							holes.add(ring);
						} else {
							shells.add(ring);
						}
					} catch (Exception e) {
						System.err
								.println("Caught Topology exception in GMLLinearRingHandler");

						return null;
					}

					/*
					 * if (numParts == 1) { linRingExt = new
					 * GeometryFactory().createLinearRing(
					 * CoordinateArrays.toCoordinateArray(arrayCoords)); } else {
					 * linRing = new GeometryFactory().createLinearRing(
					 * CoordinateArrays.toCoordinateArray(arrayCoords));
					 * arrayLines.add(linRing); }
					 */
					arrayCoords = new ArrayList();
				}

				numParts++;
				arrayCoords.add(new Coordinate(theData[0], theData[1]));

				break;

			case PathIterator.SEG_LINETO:

				// System.out.println("SEG_LINETO");
				arrayCoords.add(new Coordinate(theData[0], theData[1]));

				break;

			case PathIterator.SEG_QUADTO:
				System.out.println("SEG_QUADTO Not supported here");

				break;

			case PathIterator.SEG_CUBICTO:
				System.out.println("SEG_CUBICTO Not supported here");

				break;

			case PathIterator.SEG_CLOSE:

				// A?adimos el primer punto para cerrar.
				Coordinate firstCoord = (Coordinate) arrayCoords.get(0);
				arrayCoords.add(new Coordinate(firstCoord.x, firstCoord.y));

				break;
			} // end switch

			// System.out.println("theData[0] = " + theData[0] + " theData[1]="
			// + theData[1]);
			theIterator.next();
		} // end while loop

		arrayCoords.add(arrayCoords.get(0));
		coords = CoordinateArrays.toCoordinateArray(arrayCoords);

		if (numParts == 1) {
			return getExteriorPolygon(coords);
		}
		return ShapeFactory.createGeometry(pol);

	}
	
	/**
	 * Completes FMap's FConverter consideering GeometryCollection instances
	 * (ignored in FConverter)
	 * @param geometry
	 * @return
	 */
	public static IGeometry toFMap(Geometry geometry){
		IGeometry solution = null;
		if(geometry.getClass().equals(GeometryCollection.class)){
			solution = convert((GeometryCollection) geometry);
		}else{
			solution = FConverter.jts_to_igeometry(geometry);
		}
		return solution;
	}

	
	/**
	 * Original FConverter doestn convert GeometryCollection raw type
	 * 
	 * @param geometryCollection
	 * @return
	 */
	public static IGeometry convert(GeometryCollection geometryCollection){
		Geometry[] geomsArray = JtsUtil.extractGeometries(geometryCollection);
		int numGeoms = geomsArray.length;
		IGeometry[] fmapGeoms = new IGeometry[numGeoms];
		for(int i = 0; i < numGeoms; i++){
			fmapGeoms[i] = FConverter.jts_to_igeometry(geomsArray[i]);
		}
		if(numGeoms == 1)
			return fmapGeoms[0];
		else
			return new FGeometryCollection(fmapGeoms);
	}

	public static MultiPolygon toJtsPolygon(FShape shp) {
	
		ArrayList<LinearRing> shells = new ArrayList<LinearRing>();
		ArrayList<LinearRing> holes = new ArrayList<LinearRing>();
	
		List<Point2D[]> shpPoints = ShapePointExtractor.extractPoints(shp);
		for (int i = 0; i < shpPoints.size(); i++) {
			Point2D[] partPoints = shpPoints.get(i);
			Coordinate[] coords = JtsUtil.getPoint2DAsCoordinates(partPoints);
			try {
				LinearRing ring = JtsUtil.GEOMETRY_FACTORY
						.createLinearRing(coords);
	
				// TODO REPORTAR ESTO EN FMAP, CREO QUE EST? MAL PORQUE LO HACE
				// AL REV?S.
				// EL TEMA EST? EN QUE JTS HACE LAS COSAS AL REVES QUE EL
				// FORMATO SHP
				if (CGAlgorithms.isCCW(coords)) {
					shells.add(ring);
				} else {
					holes.add(ring);
				}
			} catch (Exception e) {
				/*
				 * Leer la cabecera del metodo: FConverter no deber?a hacer
				 * estos tratamientos boolean same = true; for (int j = 0; i <
				 * coords.length - 1 && same; j++) { if (coords[i].x !=
				 * coords[i+1].x || coords[i].y != coords[i+1].y) same = false; }
				 * if (same) return JtsUtil.geomFactory.createPoint(coords[0]);
				 */
	
				/*
				 * caso cuando es una l?nea de 3 puntos, no creo un LinearRing,
				 * sino una linea
				 */
				/*
				 * if (coords.length > 1 && coords.length <= 3) // return
				 * geomFactory.createLineString(points); return
				 * JtsUtil.geomFactory.createMultiLineString(new LineString[]
				 * {JtsUtil.geomFactory.createLineString(coords)});
				 */
				System.err
						.println("Caught Topology exception in GMLLinearRingHandler");
				return null;
			}
		}// for
	
		// At this point, we have a collection of shells and a collection of
		// holes
		// Now we have to find for each shell its holes
		ArrayList<List<LinearRing>> holesForShells = new ArrayList<List<LinearRing>>(
				shells.size());
		for (int i = 0; i < shells.size(); i++) {
			holesForShells.add(new ArrayList<LinearRing>());
		}
	
		/*
		 * Now, for each hole we look for the minimal shell that contains it. We
		 * look for minimal because many shells could contain the same hole.
		 */
		for (int i = 0; i < holes.size(); i++) {// for each hole
			LinearRing testHole = holes.get(i);
			LinearRing minShell = null;
			Envelope minEnv = null;
			Envelope testEnv = testHole.getEnvelopeInternal();
			Coordinate testPt = testHole.getCoordinateN(0);
			LinearRing tryRing = null;
	
			for (int j = 0; j < shells.size(); j++) {// for each shell
				tryRing = (LinearRing) shells.get(j);
				Envelope tryEnv = tryRing.getEnvelopeInternal();
				boolean isContained = false;
				Coordinate[] coordList = tryRing.getCoordinates();
	
				// if testpoint is in ring, or test point is a shell point, is
				// contained
				if (tryEnv.contains(testEnv)
						&& (SnapCGAlgorithms.isPointInRing(testPt, coordList) || JtsUtil
								.pointInList(testPt, coordList))) {
					isContained = true;
				}
	
				// check if this new containing ring is smaller than the current
				// minimum ring
				if (isContained) {
					if ((minShell == null) || minEnv.contains(tryEnv)) {
						minShell = tryRing;
						minEnv = minShell.getEnvelopeInternal();
					}
				}
			}// for shells
	
			// At this point, minShell is the shell that contains a testHole
			// if minShell is null, we have a SHELL which points were digitized
			// in the
			// wrong order
	
			if (minShell == null) {
	
				/*
				 * TODO Si las clases de FMap incluyesen en su semantica la
				 * diferencia entre un shell y un hole, no deberiamos hacer esta
				 * suposicion.
				 * 
				 * Pero como java.awt.geom.Shape no hace esta distincion,
				 * tenemos que hacerla.
				 * 
				 * 
				 */
				LinearRing reversed = (LinearRing) JtsUtil.reverse(testHole);
				shells.add(reversed);
				holesForShells.add(new ArrayList<LinearRing>());
			} else {
				((ArrayList<LinearRing>) holesForShells.get(shells
						.indexOf(minShell))).add(testHole);
			}
		}// for each hole
	
		Polygon[] polygons = new Polygon[shells.size()];
		for (int i = 0; i < shells.size(); i++) {
			polygons[i] = JtsUtil.GEOMETRY_FACTORY.createPolygon(
					(LinearRing) shells.get(i),
					(LinearRing[]) ((ArrayList<LinearRing>) holesForShells
							.get(i)).toArray(new LinearRing[0]));
		}
		return JtsUtil.GEOMETRY_FACTORY.createMultiPolygon(polygons);
	
	}

	public static Geometry toJtsGeometry(IGeometry fmapGeometry) {
		Geometry solution = null;
		if (fmapGeometry instanceof FGeometry) {
			FShape shp = (FShape) ((FGeometry) fmapGeometry).getInternalShape();
			solution = java2d_to_jts(shp);
		} else if (fmapGeometry instanceof FGeometryCollection) {
			IGeometry[] geometries = ((FGeometryCollection) fmapGeometry)
					.getGeometries();
			Geometry[] theGeoms = new Geometry[geometries.length];
			for (int i = 0; i < geometries.length; i++) {
				theGeoms[i] = NewFConverter.toJtsGeometry(((IGeometry) geometries[i]));
			}
			solution = JtsUtil.GEOMETRY_FACTORY.createGeometryCollection(theGeoms);
	
		} else if (fmapGeometry instanceof FMultiPoint2D) {
			solution = ((FMultiPoint2D) fmapGeometry).toJTSGeometry();
		}
		return solution;
	}

}

