/*
 * Created on 08-jun-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
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
package com.iver.cit.gvsig.fmap.core.v02;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Array;
import java.util.ArrayList;

import com.iver.cit.gvsig.fmap.core.FNullGeometry;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FPolygon2D;
import com.iver.cit.gvsig.fmap.core.FPolyline2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.RobustCGAlgorithms;
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
 * Clase con varios m?todos est?ticos utilizados para pasar de java2d a jts y
 * viceversa.
 *
 * @author fjp
 */
public class FConverter {
	/**
	 * ?QU? PODEMOS HACER CON LOS MULTIPOINT??? => DEBER?AMOS TRABAJAR CON UN
	 * ARRAY DE PUNTOS EN FShape.....Pensarlo bien.
	 */
	public final static GeometryFactory geomFactory = new GeometryFactory();
	public static CGAlgorithms cga = new RobustCGAlgorithms();
	// private final static AffineTransform at = new AffineTransform();
	private static double POINT_MARKER_SIZE = 3.0;

	/**
	 * Es la m?xima distancia que permitimos que el trazo aproximado
	 * difiera del trazo real.
	 */
	public static double FLATNESS =0.8;// Por ejemplo. Cuanto m?s peque?o, m?s segmentos necesitar? la curva


	//returns true if testPoint is a point in the pointList list.
	static boolean pointInList(Coordinate testPoint, Coordinate[] pointList) {
		int t;
		int numpoints;
		Coordinate p;

		numpoints = Array.getLength(pointList);

		for (t = 0; t < numpoints; t++) {
			p = pointList[t];

			if ((testPoint.x == p.x) && (testPoint.y == p.y) &&
					((testPoint.z == p.z) || (!(testPoint.z == testPoint.z))) //nan test; x!=x iff x is nan
			) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Receives a JTS Geometry and returns a fmap IGeometry
	 * @param jtsGeometry jts Geometry
	 * @return IGeometry of FMap
	 * @author azabala
	 */
	public static IGeometry jts_to_igeometry(Geometry jtsGeometry){
		FShape shape = FConverter.jts_to_java2d(jtsGeometry);
		if (shape == null)
			return new FNullGeometry();
		return ShapeFactory.createGeometry(shape);
	}

	/**
	 * Convierte un FShape a una Geometry del JTS. Para ello, utilizamos un
	 * "flattened PathIterator". El flattened indica que las curvas las pasa a
	 * segmentos de l?nea recta AUTOMATICAMENTE!!!.
	 *
	 * @param shp FShape que se quiere convertir.
	 *
	 * @return Geometry de JTS.
	 */
	public static Geometry java2d_to_jts(FShape shp) {


		Geometry geoJTS = null;
		Coordinate coord;
		Coordinate[] coords;
		ArrayList arrayCoords = null;
		ArrayList arrayLines;
		LineString lin;
		LinearRing linRing;
		LinearRing linRingExt = null;
		int theType;
		int numParts = 0;

		//	 	Use this array to store segment coordinate data
		double[] theData = new double[6];
		PathIterator theIterator;

		switch (shp.getShapeType()) {
			case FShape.POINT:
            case FShape.POINT + FShape.Z:
				FPoint2D p = (FPoint2D) shp;
				coord = new Coordinate(p.getX(), p.getY());
				geoJTS = geomFactory.createPoint(coord);

				break;

			case FShape.LINE:
			case FShape.ARC:
            case FShape.LINE + FShape.Z:
            case FShape.LINE | FShape.M:
				arrayLines = new ArrayList();
				theIterator = shp.getPathIterator(null, FLATNESS);

				while (!theIterator.isDone()) {
					//while not done
					theType = theIterator.currentSegment(theData);

					//Populate a segment of the new
					// GeneralPathX object.
					//Process the current segment to populate a new
					// segment of the new GeneralPathX object.
					switch (theType) {
						case PathIterator.SEG_MOVETO:

							// System.out.println("SEG_MOVETO");
							if (arrayCoords == null) {
								arrayCoords = new ArrayList();
							} else {
								lin = geomFactory.createLineString(CoordinateArrays.toCoordinateArray(
											arrayCoords));
								arrayLines.add(lin);
								arrayCoords = new ArrayList();
							}

							numParts++;
							arrayCoords.add(new Coordinate(theData[0],
									theData[1]));

							break;

						case PathIterator.SEG_LINETO:

							// System.out.println("SEG_LINETO");
							arrayCoords.add(new Coordinate(theData[0],
									theData[1]));

							break;

						case PathIterator.SEG_QUADTO:
							System.out.println("Not supported here");

							break;

						case PathIterator.SEG_CUBICTO:
							System.out.println("Not supported here");

							break;

						case PathIterator.SEG_CLOSE:
							// A?adimos el primer punto para cerrar.
							Coordinate firstCoord = (Coordinate) arrayCoords.get(0);
							arrayCoords.add(new Coordinate(firstCoord.x,
									firstCoord.y));

							break;
					} //end switch

					theIterator.next();
				} //end while loop

				lin = new GeometryFactory().createLineString(CoordinateArrays.toCoordinateArray(
							arrayCoords));

				// CAMBIO: ENTREGAMOS SIEMPRE MULTILINESTRING, QUE ES
				// LO QUE HACE TODO EL MUNDO CUANDO ESCRIBE EN POSTGIS
				// O CON GEOTOOLS
				// if (numParts > 1) // Generamos una MultiLineString
				//  {
					arrayLines.add(lin);
					geoJTS = geomFactory.createMultiLineString(GeometryFactory.toLineStringArray(
								arrayLines));
				/* } else {
					geoJTS = lin;
				} */

				break;

			case FShape.POLYGON:
			case FShape.CIRCLE:
			case FShape.ELLIPSE:
			case FShape.POLYGON | FShape.M:
            case FShape.POLYGON + FShape.Z:
            	
				arrayLines = new ArrayList();

				ArrayList shells = new ArrayList();
				ArrayList holes = new ArrayList();
				Coordinate[] points = null;

				theIterator = shp.getPathIterator(null, FLATNESS);

				while (!theIterator.isDone()) {
					//while not done
					theType = theIterator.currentSegment(theData);

					//Populate a segment of the new
					// GeneralPathX object.
					//Process the current segment to populate a new
					// segment of the new GeneralPathX object.
					switch (theType) {
						case PathIterator.SEG_MOVETO:

							// System.out.println("SEG_MOVETO");
							if (arrayCoords == null) {
								arrayCoords = new ArrayList();
							} else {
								points = CoordinateArrays.toCoordinateArray(arrayCoords);

								try {
									LinearRing ring = geomFactory.createLinearRing(points);

									if (CGAlgorithms.isCCW(points)) {
										holes.add(ring);
									} else {
										shells.add(ring);
									}
								} catch (Exception e) {
									/* (jaume) caso cuando todos los puntos son iguales
									 * devuelvo el propio punto
									 */
									boolean same = true;
									for (int i = 0; i < points.length-1 && same; i++) {
										if (points[i].x != points[i+1].x ||
												points[i].y != points[i+1].y /*||
												points[i].z != points[i+1].z*/
												) same = false;
									}
									if (same)
										return geomFactory.createPoint(points[0]);
									/*
									 * caso cuando es una l?nea de 3 puntos, no creo un LinearRing, sino
									 * una linea
									 */
									if (points.length>1 && points.length<=3)
										// return geomFactory.createLineString(points);
										return geomFactory.createMultiLineString(new LineString[] {geomFactory.createLineString(points)});

									System.err.println(
										"Caught Topology exception in GMLLinearRingHandler");

									return null;
								}

								/* if (numParts == 1)
								   {
								           linRingExt = new GeometryFactory().createLinearRing(
								                  CoordinateArrays.toCoordinateArray(arrayCoords));
								   }
								   else
								   {
								           linRing = new GeometryFactory().createLinearRing(
								                          CoordinateArrays.toCoordinateArray(arrayCoords));
								           arrayLines.add(linRing);
								   } */
								arrayCoords = new ArrayList();
							}

							numParts++;
							arrayCoords.add(new Coordinate(theData[0],
									theData[1]));

							break;

						case PathIterator.SEG_LINETO:

							// System.out.println("SEG_LINETO");
							arrayCoords.add(new Coordinate(theData[0],
									theData[1]));

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
							arrayCoords.add(new Coordinate(firstCoord.x,
									firstCoord.y));

							break;
					} //end switch

					// System.out.println("theData[0] = " + theData[0] + " theData[1]=" + theData[1]);
					theIterator.next();
				} //end while loop

				arrayCoords.add(arrayCoords.get(0));
				points = CoordinateArrays.toCoordinateArray(arrayCoords);

				try {
					LinearRing ring = geomFactory.createLinearRing(points);

					if (CGAlgorithms.isCCW(points)) {
						holes.add(ring);
					} else {
						shells.add(ring);
					}
				} catch (Exception e) {
					/* (jaume) caso cuando todos los puntos son iguales
					 * devuelvo el propio punto
					 */
					boolean same = true;
					for (int i = 0; i < points.length-1 && same; i++) {
						if (points[i].x != points[i+1].x ||
								points[i].y != points[i+1].y /*||
								points[i].z != points[i+1].z*/
								) same = false;
					}
					if (same)
						return geomFactory.createPoint(points[0]);
					/*
					 * caso cuando es una l?nea de 3 puntos, no creo un LinearRing, sino
					 * una linea
					 */
					if (points.length>1 && points.length<=3)
						// return geomFactory.createLineString(points);
						return geomFactory.createMultiLineString(new LineString[] {geomFactory.createLineString(points)});
					System.err.println(
						"Caught Topology exception in GMLLinearRingHandler");

					return null;
				}

				/* linRing = new GeometryFactory().createLinearRing(
				   CoordinateArrays.toCoordinateArray(arrayCoords)); */

				// System.out.println("NumParts = " + numParts);
				//now we have a list of all shells and all holes
				ArrayList holesForShells = new ArrayList(shells.size());

				for (int i = 0; i < shells.size(); i++) {
					holesForShells.add(new ArrayList());
				}

				//find homes
				for (int i = 0; i < holes.size(); i++) {
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

						if (tryEnv.contains(testEnv) &&
								(CGAlgorithms.isPointInRing(testPt, coordList) ||
								(pointInList(testPt, coordList)))) {
							isContained = true;
						}

						// check if this new containing ring is smaller than the current minimum ring
						if (isContained) {
							if ((minShell == null) || minEnv.contains(tryEnv)) {
								minShell = tryRing;
							}
						}
					}

					if (minShell == null) {
//						System.out.println(
//							"polygon found with a hole thats not inside a shell");
//azabala: we do the assumption that this hole is really a shell (polygon)
//whose point werent digitized in the right order
Coordinate[] cs = testRing.getCoordinates();
Coordinate[] reversed = new Coordinate[cs.length];
int pointIndex = 0;
for(int z = cs.length-1; z >= 0; z--){
	reversed[pointIndex] = cs[z];
	pointIndex++;
}
LinearRing newRing = geomFactory.createLinearRing(reversed);
shells.add(newRing);
holesForShells.add(new ArrayList());
					} else {
						((ArrayList) holesForShells.get(shells.indexOf(minShell))).add(testRing);
					}
				}

				Polygon[] polygons = new Polygon[shells.size()];

				for (int i = 0; i < shells.size(); i++) {
					polygons[i] = geomFactory.createPolygon((LinearRing) shells.get(
								i),
							(LinearRing[]) ((ArrayList) holesForShells.get(i)).toArray(
								new LinearRing[0]));
				}
				// CAMBIO: ENTREGAMOS SIEMPRE MULTILINESTRING, QUE ES
				// LO QUE HACE TODO EL MUNDO CUANDO ESCRIBE EN POSTGIS
				// O CON GEOTOOLS
				// if (numParts > 1) // Generamos una MultiLineString

				/* if (polygons.length == 1) {
					return polygons[0];
				} */

				// FIN CAMBIO

				holesForShells = null;
				shells = null;
				holes = null;

				//its a multi part
				geoJTS = geomFactory.createMultiPolygon(polygons);

				/* if (numParts > 1) // Generamos un Polygon con agujeros
				   {
				    arrayLines.add(linRing);
				           // geoJTS = new GeometryFactory().createPolygon(linRingExt,
				                           // GeometryFactory.toLinearRingArray(arrayLines));
				    geoJTS = new GeometryFactory().buildGeometry(arrayLines);

				    // geoJTS = Polygonizer.class.
				   }
				   else
				   {
				           geoJTS = new GeometryFactory().createPolygon(linRing,null);
				   } */
				break;
		}

		return geoJTS;
	}

	/**
	 * Converts JTS Geometry objects into Java 2D Shape objects
	 *
	 * @param geo Geometry de JTS.
	 *
	 * @return FShape.
	 */
	public static FShape jts_to_java2d(Geometry geo) {
		FShape shpNew = null;

		try {
			if (geo instanceof Point) {
				shpNew = new FPoint2D(((Point) geo).getX(), ((Point) geo).getY());
			}

			if (geo.isEmpty()) {
				shpNew = null;
			}

			if (geo instanceof Polygon) {
				shpNew = new FPolygon2D(toShape((Polygon) geo));
			}

			if (geo instanceof MultiPolygon) {
				shpNew = new FPolygon2D(toShape((MultiPolygon) geo));
			}

			if (geo instanceof LineString) {
				shpNew = new FPolyline2D(toShape((LineString) geo));
			}

			if (geo instanceof MultiLineString) {
				shpNew = new FPolyline2D(toShape((MultiLineString) geo));
			}

			/* OJO: CON ALGO COMO FSHAPE NO S? C?MO PODEMOS IMPLEMENTAR UN GeometryCollection
			 * No sabremos si queremos una l?nea o un pol?gono.....
			 *  if (geometry instanceof GeometryCollection) {
			          return toShape((GeometryCollection) geometry);
			   } */
			return shpNew;
		} catch (NoninvertibleTransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param p DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private static GeneralPathX toShape(Polygon p) {
		GeneralPathX resul = new GeneralPathX();
		Coordinate coord;

		for (int i = 0; i < p.getExteriorRing().getNumPoints(); i++) {
			coord = p.getExteriorRing().getCoordinateN(i);

			if (i == 0) {
				resul.moveTo(coord.x,coord.y);
			} else {
				resul.lineTo(coord.x,coord.y);
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

	/**
	 * DOCUMENT ME!
	 *
	 * @param modelCoordinates DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws NoninvertibleTransformException DOCUMENT ME!
	 *
	private Coordinate[] toViewCoordinates(Coordinate[] modelCoordinates)
		throws NoninvertibleTransformException {
		Coordinate[] viewCoordinates = new Coordinate[modelCoordinates.length];

		for (int i = 0; i < modelCoordinates.length; i++) {
			FPoint2D point2D = coordinate2FPoint2D(modelCoordinates[i]);
			viewCoordinates[i] = new Coordinate(point2D.getX(), point2D.getY());
		}

		return viewCoordinates;
	} */

	/* private Shape toShape(GeometryCollection gc)
	   throws NoninvertibleTransformException {
	   GeometryCollectionShape shape = new GeometryCollectionShape();
	   for (int i = 0; i < gc.getNumGeometries(); i++) {
	           Geometry g = (Geometry) gc.getGeometryN(i);
	           shape.add(toShape(g));
	   }
	   return shape;
	   } */
	private static GeneralPathX toShape(MultiLineString mls)
		throws NoninvertibleTransformException {
		GeneralPathX path = new GeneralPathX();

		for (int i = 0; i < mls.getNumGeometries(); i++) {
			LineString lineString = (LineString) mls.getGeometryN(i);
			path.append(toShape(lineString), false);
		}

		//BasicFeatureRenderer expects LineStrings and MultiLineStrings to be
		//converted to GeneralPathXs. [Jon Aquino]
		return path;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param lineString DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws NoninvertibleTransformException DOCUMENT ME!
	 */
	private static GeneralPathX toShape(LineString lineString)
		throws NoninvertibleTransformException {
		GeneralPathX shape = new GeneralPathX();
		FPoint2D viewPoint = coordinate2FPoint2D(lineString.getCoordinateN(0));
		shape.moveTo(viewPoint.getX(), viewPoint.getY());

		for (int i = 1; i < lineString.getNumPoints(); i++) {
			viewPoint = coordinate2FPoint2D(lineString.getCoordinateN(i));
			shape.lineTo(viewPoint.getX(), viewPoint.getY());
		}

		//BasicFeatureRenderer expects LineStrings and MultiLineStrings to be
		//converted to GeneralPathXs. [Jon Aquino]
		return shape;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param point DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws NoninvertibleTransformException DOCUMENT ME!
	 */
	private static FPoint2D toShape(Point point)
		throws NoninvertibleTransformException {
		FPoint2D viewPoint = coordinate2FPoint2D(point.getCoordinate());

		return viewPoint;
	}

	private static GeneralPathX toShape(MultiPolygon mp)
	throws NoninvertibleTransformException {
	GeneralPathX path = new GeneralPathX();

	for (int i = 0; i < mp.getNumGeometries(); i++) {
		Polygon polygon = (Polygon) mp.getGeometryN(i);
		path.append(toShape(polygon), false);
	}

	//BasicFeatureRenderer expects LineStrings and MultiLineStrings to be
	//converted to GeneralPathXs. [Jon Aquino]
	return path;
}
	/**
	 * DOCUMENT ME!
	 *
	 * @param coord DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static FPoint2D coordinate2FPoint2D(Coordinate coord) {
		return new FPoint2D(coord.x, coord.y); //,coord.z);
	}

	/**
	 * Convierte una Geometry de JTS a GeneralPathX.
	 *
	 * @param geometry Geometry a convertir.
	 *
	 * @return GeneralPathX.
	 *
	 * @throws NoninvertibleTransformException
	 * @throws IllegalArgumentException
	 */
	public static GeneralPathX toShape(Geometry geometry)
		throws NoninvertibleTransformException {
		if (geometry.isEmpty()) {
			return new GeneralPathX();
		}

		if (geometry instanceof Polygon) {
			return toShape((Polygon) geometry);
		}

		if (geometry instanceof MultiPolygon) {
			return toShape((MultiPolygon) geometry);
		}

		if (geometry instanceof LineString) {
			return toShape((LineString) geometry);
		}

		if (geometry instanceof MultiLineString) {
			return toShape((MultiLineString) geometry);
		}

		if (geometry instanceof GeometryCollection) {
			return toShape((GeometryCollection) geometry);
		}

		throw new IllegalArgumentException("Unrecognized Geometry class: " +
			geometry.getClass());
	}


    public static GeneralPathX transformToInts(GeneralPathX gp, AffineTransform at) {
        GeneralPathX newGp = new GeneralPathX();
        PathIterator theIterator;
        int theType;
        int numParts = 0;
        double[] theData = new double[6];
        Point2D ptDst = new Point2D.Double();
        Point2D ptSrc = new Point2D.Double();
        boolean bFirst = true;
        int xInt, yInt, antX = -1, antY = -1;

        theIterator = gp.getPathIterator(null); //, flatness);

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
                    if ((bFirst) || ((xInt != antX) || (yInt != antY)))
                    {
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
            } //end switch

            theIterator.next();
        } //end while loop

        return newGp;
    }
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


        theIterator = gp.getPathIterator(null); //, flatness);
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
                    if ((bFirst) || ((xInt != antX) || (yInt != antY)))
                    {
                        newGp.lineTo(xInt, yInt);
                        antX = xInt;
                        antY = yInt;
                        bFirst = false;
                        numSegmentsAdded++;
                    }
                    break;

                case PathIterator.SEG_QUADTO:
                    at.transform(theData,0,aux,0,2);
                    newGp.quadTo(aux[0], aux[1], aux[2], aux[3]);
                    numSegmentsAdded++;
                    break;

                case PathIterator.SEG_CUBICTO:
                    at.transform(theData,0,aux,0,3);
                    newGp.curveTo(aux[0], aux[1], aux[2], aux[3], aux[4], aux[5]);
                    numSegmentsAdded++;
                    break;

                case PathIterator.SEG_CLOSE:
                    if (numSegmentsAdded < 3)
                        newGp.lineTo(antX, antY);
                    newGp.closePath();

                    break;
            } //end switch

            theIterator.next();
        } //end while loop

        FShape shp = null;
        switch (gp.getGeometryType())
        {
            case FShape.POINT: //Tipo punto
            case FShape.POINT + FShape.Z:
                shp = new FPoint2D(ptDst.getX(), ptDst.getY());
                break;

            case FShape.LINE:
            case FShape.LINE + FShape.Z:
            case FShape.LINE | FShape.M:
            case FShape.ARC:
            	shp = new FPolyline2D(newGp);
                break;

            case FShape.POLYGON:
            case FShape.POLYGON + FShape.Z:
            case FShape.POLYGON | FShape.M:
            case FShape.CIRCLE:
            case FShape.ELLIPSE:

                shp = new FPolygon2D(newGp);
                break;
        }
        return shp;
    }

    public static Rectangle2D convertEnvelopeToRectangle2D(Envelope jtsR)
    {
        Rectangle2D.Double r = new Rectangle2D.Double(jtsR.getMinX(),
                jtsR.getMinY(), jtsR.getWidth(), jtsR.getHeight());
        return r;
    }

    public static Envelope convertRectangle2DtoEnvelope(Rectangle2D r)
    {
    	Envelope e = new Envelope(r.getX(), r.getX() + r.getWidth(), r.getY(),
			r.getY() + r.getHeight());
    	return e;
    }

    /**
     * Return a correct polygon (no hole)
     * @param coordinates
     * @return
     */
    public static IGeometry getExteriorPolygon(Coordinate[] coordinates)
    {
    	// isCCW = true => it's a hole
    	Coordinate[] vs=new Coordinate[coordinates.length];
        if (CGAlgorithms.isCCW(coordinates)){
        	for (int i=vs.length-1;i>=0;i--){
        		vs[i]=coordinates[i];
        	}
        }else{
        	vs=coordinates;
        }
        LinearRing ring = geomFactory.createLinearRing(vs);

        try {
			return ShapeFactory.createPolygon2D(toShape(ring));
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		return null;
    }

    public static boolean isCCW(Point2D[] points)
    {
    	int length = points.length;
    	Coordinate[] vs;
    	// CGAlgorithms.isCCW asume que la lista de puntos tienen el primer
    	// y el ultimo puntos iguales.... y que este algoritmo est? solo
    	// garantizado con anillos v?lidos.
    	if (points[0].getX() != points[length-1].getX() || points[0].getY() != points[length-1].getY()) {
        	vs=new Coordinate[length+1];
        	vs[points.length] = new Coordinate(points[0].getX(), points[0].getY());
    	} else {
        	vs=new Coordinate[length];
    	}
       	for (int i=0; i<length; i++){
    		vs[i] = new Coordinate(points[i].getX(), points[i].getY());
    	}
        return CGAlgorithms.isCCW(vs);
    }

    public static boolean isCCW(FPolygon2D pol)
    {
    	Geometry jtsGeom = FConverter.java2d_to_jts(pol);
    	if (jtsGeom.getNumGeometries() == 1)
    	{
    		Coordinate[] coords = jtsGeom.getCoordinates();
    		return CGAlgorithms.isCCW(coords);
    	}
    	return false;

    }


    /**
     * Return a hole (CCW ordered points)
     * @param coordinates
     * @return
     */
    public static IGeometry getHole(Coordinate[] coordinates)
    {
    	// isCCW = true => it's a hole
    	Coordinate[] vs=new Coordinate[coordinates.length];
        if (CGAlgorithms.isCCW(coordinates)){
        	vs=coordinates;

        }else{
        	for (int i=vs.length-1;i>=0;i--){
        		vs[i]=coordinates[i];
        	}
        }
        LinearRing ring = geomFactory.createLinearRing(vs);

        try {
			return ShapeFactory.createPolygon2D(toShape(ring));
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		return null;
    }

	public static Shape getExteriorPolygon(GeneralPathX gp) {
		Area area = new Area(gp);
		area.isSingular();
		return area;



	}
	/**
	 * Use it ONLY for NOT multipart polygons.
	 * @param pol
	 * @return
	 */
	public static IGeometry getNotHolePolygon(FPolygon2D pol) {
		// isCCW == true => hole
		Coordinate[] coords;
		ArrayList arrayCoords = null;
		int theType;
		int numParts = 0;

		//	 	Use this array to store segment coordinate data
		double[] theData = new double[6];
		PathIterator theIterator;

				ArrayList shells = new ArrayList();
				ArrayList holes = new ArrayList();
				Coordinate[] points = null;

				theIterator = pol.getPathIterator(null, FLATNESS);

				while (!theIterator.isDone()) {
					//while not done
					theType = theIterator.currentSegment(theData);

					//Populate a segment of the new
					// GeneralPathX object.
					//Process the current segment to populate a new
					// segment of the new GeneralPathX object.
					switch (theType) {
						case PathIterator.SEG_MOVETO:

							// System.out.println("SEG_MOVETO");
							if (arrayCoords == null) {
								arrayCoords = new ArrayList();
							} else {
								points = CoordinateArrays.toCoordinateArray(arrayCoords);

								try {
									LinearRing ring = geomFactory.createLinearRing(points);

									if (CGAlgorithms.isCCW(points)) {
										holes.add(ring);
									} else {
										shells.add(ring);
									}
								} catch (Exception e) {
									System.err.println(
										"Caught Topology exception in GMLLinearRingHandler");

									return null;
								}

								/* if (numParts == 1)
								   {
								           linRingExt = new GeometryFactory().createLinearRing(
								                  CoordinateArrays.toCoordinateArray(arrayCoords));
								   }
								   else
								   {
								           linRing = new GeometryFactory().createLinearRing(
								                          CoordinateArrays.toCoordinateArray(arrayCoords));
								           arrayLines.add(linRing);
								   } */
								arrayCoords = new ArrayList();
							}

							numParts++;
							arrayCoords.add(new Coordinate(theData[0],
									theData[1]));

							break;

						case PathIterator.SEG_LINETO:

							// System.out.println("SEG_LINETO");
							arrayCoords.add(new Coordinate(theData[0],
									theData[1]));

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
							arrayCoords.add(new Coordinate(firstCoord.x,
									firstCoord.y));

							break;
					} //end switch

					// System.out.println("theData[0] = " + theData[0] + " theData[1]=" + theData[1]);
					theIterator.next();
				} //end while loop

				arrayCoords.add(arrayCoords.get(0));
				coords = CoordinateArrays.toCoordinateArray(arrayCoords);


		if (numParts == 1)
		{
			return getExteriorPolygon(coords);
		}
		return ShapeFactory.createGeometry(pol);

	}


    /* public static GeometryCollection convertFGeometryCollection(FGeometryCollection fGeomC)
    {

        geomFactory.createGeometryCollection(theGeoms);
    } */
}

