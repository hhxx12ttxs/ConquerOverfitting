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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.gvsig.jts.JtsUtil;

import com.iver.cit.gvsig.fmap.core.FArc2D;
import com.iver.cit.gvsig.fmap.core.FCircle2D;
import com.iver.cit.gvsig.fmap.core.FCurve;
import com.iver.cit.gvsig.fmap.core.FEllipse2D;
import com.iver.cit.gvsig.fmap.core.FGeometry;
import com.iver.cit.gvsig.fmap.core.FGeometryCollection;
import com.iver.cit.gvsig.fmap.core.FMultiPoint2D;
import com.iver.cit.gvsig.fmap.core.FMultipoint3D;
import com.iver.cit.gvsig.fmap.core.FNullGeometry;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FPoint3D;
import com.iver.cit.gvsig.fmap.core.FPolygon2D;
import com.iver.cit.gvsig.fmap.core.FPolygon3D;
import com.iver.cit.gvsig.fmap.core.FPolyline2D;
import com.iver.cit.gvsig.fmap.core.FPolyline3D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.FSpline2D;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.gt2.FLiteShape;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.GeometricShapeFactory;

/**
 * Utility methods to work with FMap geometries.
 * 
 * @author Alvaro Zabala
 * 
 */
public class FGeometryUtil {

	public static boolean isClosed(Shape shape, double snapTolerance) {
		List<Point2D[]> partsCoords = ShapePointExtractor.extractPoints(shape);
		return isClosed(partsCoords, snapTolerance);

	}

	public static boolean isClosed(List<Point2D[]> partsCoords,
			double snapTolerance) {
		// each member of the list is a part
		for (int i = 0; i < partsCoords.size(); i++) {
			Point2D[] part = partsCoords.get(i);
			if (!isClosed(part, snapTolerance)) {
				return false;
			}
		}// for
		return true;
	}

	public static boolean isClosed(Point2D[] coords, double snapTolerance) {
		Point2D start = coords[0];
		Point2D end = coords[coords.length - 1];
		return snapEquals2D(start, end, snapTolerance);
	}

	public static boolean isClosed(Shape shape) {
		return isClosed(shape, 0d);
	}

	public static boolean snapEquals2D(Point2D a, Point2D b,
			double snapTolerance) {
		return a.distance(b) <= snapTolerance;
	}

	public static boolean isClosed(IGeometry geometry, double snapTolerance) {

		// TODO Remove all of this if-then and create a method isClosed
		// for IGeometry
		if (geometry instanceof FGeometry) {
			FGeometry fgeo = (FGeometry) geometry;
			FShape fshape = (FShape) fgeo.getInternalShape();
			return isClosed(fshape, snapTolerance);

		} else if (geometry instanceof FGeometryCollection) {
			FGeometryCollection fgeo = (FGeometryCollection) geometry;
			IGeometry[] geoms = fgeo.getGeometries();
			for (int i = 0; i < geoms.length; i++) {
				IGeometry igeo = geoms[i];
				if (!isClosed(igeo, snapTolerance))
					return false;
			}
			return true;

		} else if (geometry instanceof FMultiPoint2D) {
			return false;
		} else if (geometry instanceof FMultipoint3D) {
			return false;
		} else if (geometry instanceof FNullGeometry) {
			return false;
		} else if (geometry instanceof FLiteShape) {
			FLiteShape fgeo = (FLiteShape) geometry;
			Geometry jtsGeo = fgeo.getGeometry();
			return JtsUtil.isClosed(jtsGeo, snapTolerance);
		}
		return false;
	}

	
	public static IGeometry[] extractGeometries(FGeometryCollection geomCol){
		IGeometry[] solution;
		ArrayList<IGeometry> geometries = new ArrayList<IGeometry>();
		Stack<IGeometry> stack = new Stack<IGeometry>();
		stack.add(geomCol);
		while(stack.size() > 0){
			IGeometry geometry = (IGeometry) stack.pop();
			if(geometry instanceof FGeometryCollection){
				FGeometryCollection collection = (FGeometryCollection) geometry;
				IGeometry[] geoms = collection.getGeometries();
				stack.addAll(Arrays.asList(geoms));
			}else{
				geometries.add(geometry);
			}
		}//while
		solution = new IGeometry[geometries.size()];
		return geometries.toArray(solution);
	}
	
	
	public static boolean isClosed(IGeometry geometry) {
		return isClosed(geometry, 0d);
	}
	
	
	/**
	 * This function closes an unclosed geometry, by inserting a new segment
	 * in the gap between the first and the last point.
	 * 
	 * TODO Insted inserting a new segment, MOVE one of the two points.
	 * (those which is owned by the shortest segment?)
	 * 
	 * @param unclosedGeometry unclosed geometry
	 * 
	 * @param snapTolerance cluster tolerance
	 * @return
	 */
	public static IGeometry closeGeometry(IGeometry unclosedGeometry, double snapTolerance){
		int shapeType = unclosedGeometry.getGeometryType();
		
		if(shapeType == FShape.POINT || 
			shapeType == FShape.TEXT || 
			shapeType == FShape.NULL || shapeType == FShape.MULTIPOINT){
			return unclosedGeometry;
		}
		IGeometry geomSolution = null;
		
		GeneralPathX gpx = new GeneralPathX();
		List<Point2D[]> partsCoords = ShapePointExtractor.extractPoints(unclosedGeometry);
		for (int i = 0; i < partsCoords.size(); i++) {
			Point2D[] part = partsCoords.get(i);
			Point2D start = part[0];
			Point2D end = part[part.length - 1];
			if (!snapEquals2D(start, end, snapTolerance)) {
				Point2D[] newPart = new Point2D[part.length + 1];
				System.arraycopy(part, 0, newPart, 0, part.length);
//				newPart[newPart.length] = start;
				newPart[part.length] = start;
				part = newPart;
 			}
			for(int j = 0; j < part.length; j++){
				Point2D point = part[j];
				if(j == 0)
					gpx.moveTo(point.getX(), point.getY());
				else
					gpx.lineTo(point.getX(), point.getY());
			}
		}// for
		if(unclosedGeometry instanceof FGeometry)
		{ 
			FGeometry fgeometry = (FGeometry) unclosedGeometry;
			FShape fshape = (FShape) fgeometry.getInternalShape();
			if(getXyDimensions(fshape) == 1)
				geomSolution = ShapeFactory.createGeometry(new FPolyline2D(gpx));
			else
				geomSolution = ShapeFactory.createGeometry(new FPolygon2D(gpx));
		}else if(unclosedGeometry instanceof FGeometryCollection){
				//FIXME If its a geometry collection, we need to think what we must to return
				IGeometry[] geoms = new IGeometry[]{ShapeFactory.createGeometry(new FPolygon2D(gpx))};
				geomSolution = new FGeometryCollection(geoms);
		}
		
		return geomSolution;
	}
	
	
//	public static IGeometry createGeometryOfType(GeneralPathX gpx, 
//												Class<IGeometry> geometryClass){
//		IGeometry solution = null;
//		if(geometryClass.isAssignableFrom(FGeometry.class)){
//			
//		}else if(geometryClass.isAssignableFrom(FGeometryCollection.class)){
////			solution = new FGeometryCollection()
//		}else if(geometryClass.isAssignableFrom(FMultiPoint2D.class)){
////			solution = new FMultiPoint2D()
//		}else if(geometryClass.isAssignableFrom(FMultipoint3D.class)){
//			
//		}else if(geometryClass.isAssignableFrom(FNullGeometry.class)){
//			solution = new FNullGeometry();
//		}else if(geometryClass.isAssignableFrom(FLiteShape.class)){
//			
//		}
//		return solution;
//	}

	public static IGeometry getGeometryToClose(IGeometry unclosedGeometry,
			double snapTolerance) {
		if (unclosedGeometry instanceof FGeometry) {
			FGeometry fgeo = (FGeometry) unclosedGeometry;
			FShape fshape = (FShape) fgeo.getInternalShape();
			return ShapeFactory
					.createPolyline2D((GeneralPathX) getShapeToClose(fshape,
							snapTolerance));

		} else if (unclosedGeometry instanceof FGeometryCollection) {
			List<IGeometry> geometries = new ArrayList<IGeometry>();
			FGeometryCollection fgeo = (FGeometryCollection) unclosedGeometry;
			IGeometry[] geoms = fgeo.getGeometries();
			for (int i = 0; i < geoms.length; i++) {
				IGeometry igeo = geoms[i];
				IGeometry geomToClose = getGeometryToClose(igeo, snapTolerance);
				geometries.add(geomToClose);
			}
			IGeometry[] igeoms = new IGeometry[geometries.size()];
			geometries.toArray(igeoms);
			FGeometryCollection solution = new FGeometryCollection(igeoms);
			return solution;

		} else if ((unclosedGeometry instanceof FMultiPoint2D)
				|| (unclosedGeometry instanceof FMultipoint3D)
				|| (unclosedGeometry instanceof FNullGeometry)) {
			return null;
		} else if (unclosedGeometry instanceof FLiteShape) {
			FLiteShape fgeo = (FLiteShape) unclosedGeometry;
			Geometry jtsGeo = fgeo.getGeometry();
			Geometry jtsGeo2close = JtsUtil.getGeometryToClose(jtsGeo,
					snapTolerance);
			return ShapeFactory.createGeometry(new FLiteShape(jtsGeo2close));
		}
		return null;
	}

	/**
	 * Receives an unclosed shape (or at least a multipart shape which has an
	 * unclosed part) and returns the missed geometry to get a closed shape
	 * 
	 * @param unclosedShape
	 * @param snapTolerance
	 * @return
	 */
	public static Shape getShapeToClose(Shape unclosedShape,
			double snapTolerance) {
		GeneralPathX solution = new GeneralPathX();

		List<Point2D[]> partsCoords = ShapePointExtractor
				.extractPoints(unclosedShape);
		for (int i = 0; i < partsCoords.size(); i++) {
			Point2D[] part = partsCoords.get(i);
			Point2D start = part[0];
			Point2D end = part[part.length - 1];
			if (!snapEquals2D(start, end, snapTolerance)) {
				solution.moveTo(start.getX(), start.getY());
				solution.lineTo(end.getX(), end.getY());
			}
		}// for
		return solution;
	}
	
	/**
	 * 
	 * @param geometry a non closed geometry which have an overshoot
	 * 
	 * @return
	 */
	public static IGeometry removeOvershoot(IGeometry geometry, double clusterTolerance){
		List<IGeometry> geoms2process = new ArrayList<IGeometry>();
		List<IGeometry> processedGeometries = new ArrayList<IGeometry>();
		if(geometry instanceof FGeometry){
			geoms2process.add(geometry);
		}else if(geometry instanceof FGeometryCollection){
			IGeometry[] primitives = extractGeometries((FGeometryCollection) geometry);
			geoms2process.addAll(Arrays.asList(primitives));
		}else
			return geometry;
		
		for(int i = 0; i < geoms2process.size(); i++){
			IGeometry nextGeometry = geoms2process.get(i);
			int shapeType = nextGeometry.getGeometryType();
			if(shapeType == FShape.POLYGON || shapeType == FShape.ELLIPSE || shapeType == FShape.POLYGON + FShape.Z){
				//When we convert from FMap to JTS, if the resulting geometry is a polygon and
				//its rings are unclosed, JTS will launch a runtime exception
				
				GeneralPathX shape = new GeneralPathX();
				
				List<Point2D[]> points = ShapePointExtractor.extractPoints(nextGeometry);
				List<LinearRing> rings = new ArrayList<LinearRing>();
				for(int j = 0; j < points.size(); j++){
					Point2D[] pts = points.get(j);
					Coordinate[] coords = JtsUtil.getPoint2DAsCoordinates(pts);
					LineString lineString = JtsUtil.GEOMETRY_FACTORY.createLineString(coords);
					LineString correctedLineString = (LineString) JtsUtil.removeOverShoot(lineString, clusterTolerance);
					LinearRing correctedRing = JtsUtil.toLinearRing(correctedLineString);
					rings.add(correctedRing);
				}//for j
				
				for (int j = 0; j < rings.size(); j++){
					LinearRing ring = rings.get(j);
					for(int k = 0; k < ring.getNumPoints(); k++){
						Coordinate coord = ring.getCoordinateN(k);
						if(k == 0)
							shape.moveTo(coord.x, coord.y);
						else
							shape.lineTo(coord.x, coord.y);
					}//points
				}//rings
				
				processedGeometries.add(ShapeFactory.createPolygon2D(shape));
			}else{
				Geometry jtsGeom = NewFConverter.toJtsGeometry(nextGeometry);
				Geometry correctedGeom = JtsUtil.removeOverShoot(jtsGeom, clusterTolerance);
				IGeometry correctedFGeo = NewFConverter.toFMap(correctedGeom);
				processedGeometries.add(correctedFGeo);
			}
		}//for i
		if(processedGeometries.size() == 0)
			return null;
		else if(processedGeometries.size() == 1)
			return processedGeometries.get(0);
		else{
			IGeometry[] solution = new IGeometry[processedGeometries.size()];
			processedGeometries.toArray(solution);
			return new FGeometryCollection(solution);
		}
	}

	/**
	 * Returns the dimension (0, 1, 2) of the specified geometry type.
	 * 
	 * @param shapeType
	 * @return
	 */
	public static int getDimensions(int shapeType) {
		switch (shapeType) {
		case FShape.ARC:
		case FShape.LINE:
			return 1;

		case FShape.CIRCLE:
		case FShape.ELLIPSE:
		case FShape.POLYGON:
		case FShape.MULTI:
			return 2;

		case FShape.MULTIPOINT:
		case FShape.POINT:
		case FShape.TEXT:
			return 0;
		default:
			return -1;
		}
	}

	/**
	 * Returns the dimension (0, 1, 2, -1 for NULL geometries) of the given
	 * geometry.
	 * 
	 * @param shape
	 * @return
	 */

	public static int getDimensions(IGeometry geometry) {
		if (geometry instanceof FGeometry) {
			FGeometry fgeo = (FGeometry) geometry;
			FShape fshape = (FShape) fgeo.getInternalShape();
			return getXyDimensions(fshape);

		} else if (geometry instanceof FGeometryCollection) {
			FGeometryCollection fgeo = (FGeometryCollection) geometry;
			IGeometry[] geoms = fgeo.getGeometries();
			int dimension = -1;
			for (int i = 0; i < geoms.length; i++) {
				IGeometry igeo = geoms[i];
				int igeoDimension = getDimensions(igeo);
				if (igeoDimension > dimension)
					dimension = igeoDimension;

			}
			return dimension;

		} else if (geometry instanceof FMultiPoint2D) {
			return 0;
		} else if (geometry instanceof FMultipoint3D) {
			return 0;
		} else if (geometry instanceof FNullGeometry) {
			return -1;
		} else {
			return -1;
		}
	}

	/**
	 * Returns the dimension (0, 1, 2, -1 for NULL geometries) of the given
	 * shape.
	 * 
	 * @param shape
	 * @return
	 */
	public static int getXyDimensions(FShape shape) {
		if (shape instanceof FArc2D) {
			return 1;
		} else if (shape instanceof FCircle2D) {
			return 2;
		} else if (shape instanceof FEllipse2D) {
			return 2;
		} else if (shape instanceof FPoint2D) {
			return 0;
		} else if (shape instanceof FPoint3D) {
			return 0;
		} else if (shape instanceof FPolygon2D) {
			return 2;
		} else if (shape instanceof FPolygon3D) {
			return 2;
		} else if (shape instanceof FPolyline2D) {
			return 1;
		} else if (shape instanceof FPolyline3D) {
			return 1;
		} else if (shape instanceof FSpline2D) {
			return 1;
		} else if (shape instanceof FLiteShape) {
			FLiteShape liteShape = (FLiteShape) shape;
			Geometry jtsGeo = liteShape.getGeometry();
			return jtsGeo.getDimension();
		} else {
			return -1;
		}
	}

	// TODO Solicitar que este metodo se ponga en FConverter sustituyendo
	// al viejo
	// TODO Movemos esto a JtsUtil en vez de en esta clase????

	/*
	 * Justificacion: libTopology constituye un sistema de control de calidad
	 * geometrica de los datos. FConverter deber?a asumir que los datos le
	 * llegan adecuadamente, y si no es as?, lanzar excepciones.
	 * 
	 * La responsabilidad de detectar que un dato (FShape) no es apropiado para
	 * pasar a poligono deberia tenerla libTopology (TopologyRule), y en caso de
	 * que as? se detecte, tratar el dato de forma previa a llamar a FConverter.
	 * 
	 * Por este motivo en este metodo se eliminan las correcciones que si hace
	 * FConverter
	 * 
	 * 
	 */

	public static Point2D[] getCoordinatesAsPoint2D(Coordinate[] coords) {
		Point2D[] solution = new Point2D[coords.length];
		for (int i = 0; i < coords.length; i++) {
			solution[i] = new Point2D.Double(coords[i].x, coords[i].y);
		}
		return solution;
	}

	public static IGeometry createFPolygon(Point2D[] points) {
		GeneralPathX gpx = new GeneralPathX();
		gpx.moveTo(points[0].getX(), points[0].getY());
		for (int i = 1; i < points.length; i++) {
			gpx.lineTo(points[i].getX(), points[i].getY());
		}
		if (!isClosed(gpx)) {
			gpx.closePath();
		}
		return ShapeFactory.createPolygon2D(gpx);
	}
	

	public static Rectangle2D getSnapRectangle(double x, double y,
			double snapTolerance) {
		Rectangle2D solution = null;
		double xmin = x - snapTolerance;
		double ymin = y - snapTolerance;
		double width = snapTolerance + snapTolerance;
		double height = width;
		solution = new Rectangle2D.Double(xmin, ymin, width, height);
		return solution;
	}
	
	
	public static Rectangle2D envelopeToRectangle2D(Envelope envelope){
		return new Rectangle2D.Double(envelope.getMinX(),
									  envelope.getMinY(),
									  envelope.getWidth(),
									  envelope.getHeight());
	}
	
	/**
	 * Tells if a vertex is in the boundary of a geometry (perimeter or hole perimeer)
	 * @param geometry
	 * @param point2d
	 * @return
	 */
	public static boolean isInBoundary(IGeometry geometry, Point2D point2d, double tolerance){
		Geometry jtsGeom = NewFConverter.toJtsGeometry(geometry);
		Coordinate coord = new Coordinate(point2d.getX(), point2d.getY());
		Coordinate base = new Coordinate(coord.x - tolerance, coord.y - tolerance);
		GeometricShapeFactory shpFactory = new GeometricShapeFactory(JtsUtil.GEOMETRY_FACTORY);
		shpFactory.setBase(base);
		shpFactory.setSize(tolerance * 2);
		shpFactory.setNumPoints(4);
		Polygon rect = shpFactory.createRectangle();
		if(jtsGeom.getDimension() > 1)
			jtsGeom = jtsGeom.getBoundary();
		return jtsGeom.intersects(rect);
	}

	/**
	 * Inserts a new vertex in a segment of the geometry boundary.
	 * It doesnt moves nothing, it only inserts a new collineaer vertex inside a segment.
	 * 
	 * It is useful to make topological edition, ensuring two geometries shares a vertex.
	 * 
	 * @param geometry
	 * @param newVertex
	 * @param snapTolerance
	 * @return
	 */
	public static IGeometry insertVertex(IGeometry geometry, Point2D newVertex,
			double snapTolerance) {

		IGeometry geometryCloned = geometry.cloneGeometry();
		double width = snapTolerance * 2;
		Rectangle2D rect = new Rectangle2D.Double(newVertex.getX()
				- snapTolerance, newVertex.getY() - snapTolerance, width, width);

		if (geometryCloned instanceof FGeometryCollection) {
			FGeometryCollection geomCol = (FGeometryCollection) geometryCloned;
			IGeometry[] geometries = geomCol.getGeometries();
			int length = geometries.length;
			IGeometry[] solution = new IGeometry[length];
			for (int i = 0; i < length; i++) {
				IGeometry geom = geometries[i];
				if (geom.intersects(rect)) {
					geom = insertVertex(geom, newVertex, snapTolerance);
				}
				solution[i] = geom;
			}// for
			return new FGeometryCollection(solution);
		}// if

		GeneralPathX gpx = new GeneralPathX();
		MultipartShapeIterator it = new MultipartShapeIterator(geometry);
		Iterator<Shape> shapeIt = it.getShapeIterator();
		while (shapeIt.hasNext()) {
			Shape shape = shapeIt.next();
			List<Point2D[]> partsCoords = ShapePointExtractor
					.extractPoints(shape);
			for (int i = 0; i < partsCoords.size(); i++) {
				Point2D[] part = partsCoords.get(i);
//				boolean mustBreak = false;
				for (int j = 0; j < part.length - 1; j++) {
					Point2D firstPoint = part[j];
					Point2D secondPoint = part[j + 1];
					//the first point is the start of a part
					if(j == 0){
						gpx.moveTo(firstPoint.getX(), firstPoint.getY());
					}
					else{
						gpx.lineTo(firstPoint.getX(), firstPoint.getY());
					}
					
					GeneralPathX gpxAux = new GeneralPathX();
					gpxAux.moveTo(firstPoint.getX(), firstPoint.getY());
					gpxAux.lineTo(secondPoint.getX(), secondPoint.getY());
					IGeometry lineSegment = ShapeFactory.createPolyline2D(gpxAux);
					if (lineSegment.intersects(rect)) {
						gpx.lineTo(newVertex.getX(), newVertex.getY());
//						mustBreak = true;
					}
					
					//if the last point is almost equal to the first point
					//close path
					if( j + 1 == part.length - 1){
						if(part[0].distance(secondPoint) <= snapTolerance){
							gpx.closePath();
						}else{
							gpx.lineTo(secondPoint.getX(), secondPoint.getY());
						}	
					}
				}// for j
			}// for i
		}// while

		FShape shp = null;
		switch (geometryCloned.getGeometryType()) {
		case FShape.LINE:
		case FShape.LINE + FShape.Z:
			shp = new FPolyline2D(gpx);
			break;
		case FShape.POLYGON:
		case FShape.POLYGON + FShape.Z:
		case FShape.CIRCLE:
		case FShape.ELLIPSE:
			shp = new FPolygon2D(gpx);
			break;
		}
		return ShapeFactory.createGeometry(shp);
	}
	
	/**
	 * From a given geometry with dimension 1 or 2, it computes a parametric curve
	 * and return a smoothed version of the original geometry.
	 * 
	 * To draw the returned geometry it uses a usual GeneralPath, being the FLATNESS
	 * factor of the returned curve FConverter.FLATNESS.
	 * @param originalGeometry
	 * @param shapeType 
	 * @return
	 */
	public static IGeometry smoothGeometry(IGeometry originalGeometry, int curveOption, int shapeType){
		List<Point2D[]> pointsParts =
			ShapePointExtractor.extractPoints(originalGeometry);
		IGeometry[] smoothedCurves = new IGeometry[pointsParts.size()];
		for(int i = 0; i < pointsParts.size(); i++){
			FCurve curve = new FCurve(pointsParts.get(i), curveOption, shapeType);
			smoothedCurves[i] = ShapeFactory.createGeometry(curve);
		}
		
		if(smoothedCurves.length == 1)
			return smoothedCurves[0];
		else
			return new FGeometryCollection(smoothedCurves);
	}
	
	/**
	 * 
	 * @param g
	 * @param affineTransform
	 * @param shp
	 * @param arrowColor
	 * @param stroke
	 * @param lenghtArrow
	 * @param widthArrow
	 * 
	 * TODO Move to fmap util
	 */
	public static void drawLineWithArrow(Graphics2D g, 
										AffineTransform affineTransform, 
										FShape shp,
										Color arrowColor,
										Stroke stroke,
										double lenghtArrow,
										double widthArrow){
		//1? dibujamos la linea
		
        g.setColor(arrowColor);
		if (stroke != null) {
			g.setStroke(stroke);
		}
		g.draw(shp);	
		
		
		PathIterator theIterator;
		theIterator = shp.getPathIterator(null, 0.8);
		int theType;
		double[] theData = new double[2];
		ArrayList<double[]> arrayCoords = new ArrayList<double[]>();
		while (!theIterator.isDone()) {
			theType = theIterator.currentSegment(theData);
		    if(theType == PathIterator.SEG_LINETO || theType == PathIterator.SEG_MOVETO)
		    	arrayCoords.add(theData);
		    theData = new double[2];
			theIterator.next();
		} //end while loop
		
		double length = 0d;
		double[] previous = null;
		for(int i = 0; i < arrayCoords.size(); i++ ){
			double[] coords = (double[]) arrayCoords.get(i);
			if(previous == null)
				previous = coords;
			else{
				double dx = coords[0] - previous[0];
				double dy = coords[1] - previous[1];
				double dist = Math.sqrt( ( dx * dx ) + (dy * dy));
				length += dist;
			}//else
		}//for
		
		if(lenghtArrow > (0.5 * length))//to avoid arrows collisions
			return;
		
		double[] last = (double[]) arrayCoords.get(arrayCoords.size() -1);
		double[] prevLast = (double[]) arrayCoords.get(arrayCoords.size() -2);
		
		
		double mx = last[0];
		double my = last[1];
		double Mx = prevLast[0];
		double My = prevLast[1];
//		

		// tama?o de la flecha
		double tipLength = lenghtArrow;
		double tipWidth = widthArrow;

		double	tip1x = mx + (((Mx - mx) * tipLength + ( tipWidth / 2)*(my - My))/
						Math.sqrt(( my - My) * (my - My)+(mx-Mx)*(mx-Mx)));
		
		double  tip2x = mx + (((Mx-mx) * tipLength-(tipWidth/2)*(my-My))/
						Math.sqrt((my-My)*(my-My)+(mx-Mx)*(mx-Mx)));
		
		double  tip1y = my + (((My-my)*tipLength-(tipWidth/2)*(mx-Mx))/
				 		Math.sqrt((my-My)*(my-My)+(mx-Mx)*(mx-Mx)));
		
		double tip2y = my + (((My-my)*tipLength+(tipWidth/2)*(mx-Mx))/
			            Math.sqrt((my-My)*(my-My)+(mx-Mx)*(mx-Mx)));
		
		GeneralPathX path = new GeneralPathX();
		path.moveTo(mx, my);
		path.lineTo(tip1x, tip1y);
		path.lineTo(tip2x, tip2y);
		path.closePath();
		FPolygon2D arrow = new FPolygon2D(path);
		g.fill(arrow);

	}
	
}

