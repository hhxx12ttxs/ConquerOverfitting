/*
 * Created on 07-sep-2007
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
 *
 */
package org.gvsig.jts;

import java.util.ArrayList;
import java.util.List;

import com.iver.cit.gvsig.util.SnappingCoordinateMap;
import com.vividsolutions.jts.algorithms.SnapCGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Moves coordinates of geometries (in a given tolerance) 
 * to force them to coincide with coordinates of other geometries 
 * in its proximity (the snap tolerance).
 * 
 * Also, it could normalize coordinates of the same geometry. By normalize
 * we mean to force the coherence of isClosed method and coordinates.
 * (for example, LINESTRING(100 100, 200 100, 200 200, 100 200, 100 100.01)
 *  return 'true' to isClosed call. After normalize the last coordinate
 *  will be (100 100))
 * 
 */
public class GeometrySnapper {

	private double snapTolerance;
	
	public GeometrySnapper(double snapTolerance) {
		this.snapTolerance = snapTolerance;
	}
	
	/**
	 * Utility class for compute coordinate average
	 * */
	class SnapCoordinate {
		int weight;
		Coordinate coordinate;
	}

	/**
	 * Snaps the coordinates of many geometries with the same weight
	 * (all coordinates must move)
	 * @param geoms
	 * @return
	 */
	public Geometry[] snap(Geometry[] geoms){
		int weight[] = new int[geoms.length];
		for(int i = 0; i < geoms.length; i++){
			weight[i] = 1;
		}
		return snap(geoms, weight);
	}
	
	/**
	 * Snaps all the coordinates of the given geometries
	 * .
	 * If many coords are in the same distance tolerance radius, it applies a weighted
	 * average. If one geometry's weight is 0, it wont participate in the average.
	 * 
	 * @param geoms geometries whose coordinates we want to snap
	 * @param weights weight of each geometry in the computation of the weighted average
	 * 
	 * */
	public Geometry[] snap(Geometry[] geoms, int[] weights) {
		//FIXME En caso de que ve
		for(int i = 0; i < geoms.length; i++){
			if(geoms[i] instanceof GeometryCollection){
				GeometryCollection geometryCollection = (GeometryCollection)geoms[i];
				if(geometryCollection.getNumGeometries() > 1)
					throw new IllegalArgumentException("El metodo snap no puede recibir como "+
								"argumentos GeometryCollection o subclases con mas de un elemento");
			}//if
		}
		
		SnappingCoordinateMap coordinateMap = new SnappingCoordinateMap(snapTolerance);
		Geometry[] solution = new Geometry[geoms.length];
		for(int i = 0; i < geoms.length; i++){//for each geometry
			Geometry geom = geoms[i];			
			Coordinate[] coords = geom.getCoordinates();
			CoordinateList coordList = new CoordinateList(coords);
			
			for(int j = 0; j < coords.length; j++){
				Coordinate coord = coords[j];
				Coordinate existingSolution = (Coordinate) coordinateMap.get(coord);
				if(coordinateMap.get(coord) != null){//this coordinate has already been snapped
					coordList.set(j, existingSolution);
					continue;
				}
				
				ArrayList<SnapCoordinate> coordsToSnap = new ArrayList<SnapCoordinate>();
				SnapCoordinate snapCoord = new SnapCoordinate();
				snapCoord.weight = weights[i];
				snapCoord.coordinate = coord;
				coordsToSnap.add(snapCoord);
				
				for (int k = 0; k < geoms.length; k++){
					Geometry geom2 = geoms[k];
					if(i == k)
						continue;
					Coordinate[] coords2 = geom2.getCoordinates();
					for( int z = 0; z < coords2.length; z++){
						Coordinate coord2 = coords2[z];
						if(SnapCGAlgorithms.snapEquals2D(coord, coord2, snapTolerance)){
							snapCoord = new SnapCoordinate();
							snapCoord.weight = weights[k];
							snapCoord.coordinate = coord2;
							coordsToSnap.add(snapCoord);
						}//if
					}//for z
				}//for k
				
				Coordinate newCoord = snapAverage(coordsToSnap);
				coordList.set(j, newCoord);
				coordinateMap.put(newCoord, newCoord);
			}//for j
			
			Coordinate[] newPts = coordList.toCoordinateArray();
			
			String geometryType = geom.getGeometryType();
			if(geometryType.equalsIgnoreCase("GeometryCollection") ||
			   geometryType.equalsIgnoreCase("MultiPolygon") ||
			   geometryType.equalsIgnoreCase("MultiLineString") ||
			   geometryType.equalsIgnoreCase("MultiPoint")){
				
				GeometryCollection geomCol = (GeometryCollection) geom;
				
				//we have already checked that it has no more than 1 geometry
				geom = geomCol.getGeometryN(0);
				geometryType = geom.getGeometryType();
			}
			
			
			
			if(geometryType.equalsIgnoreCase("Polygon")){
				int[] indexOfParts = JtsUtil.getIndexOfParts((Polygon)geom);
				Polygon polygon = JtsUtil.createPolygon(newPts, indexOfParts);
				solution[i] = polygon;
			}else{
				solution[i] = JtsUtil.createGeometry(newPts, geometryType);
			}
		}//for i
		return solution;
	}
	
	
	
	public Geometry snapWith(Geometry geom2snap, Geometry[] geoms){
		int[] weights = new int[geoms.length];
		for(int i = 0; i < weights.length; i++){
			weights[i] = 1;
		}
		return snapWith(geom2snap, geoms, 1, weights);
	}
	
	
	/**
	 * Similar to the previous method, but only returns the first geometry snapped with the
	 * rest of geometries
	 * @param geom2snap
	 * @param snappingGeometries
	 * @return
	 */
	public Geometry snapWith(Geometry geom2snap, Geometry[] geoms, int weightGeom, int[] weights){
		if(geom2snap instanceof GeometryCollection){
			GeometryCollection geometryCollection = (GeometryCollection)geom2snap;
			if(geometryCollection.getNumGeometries() > 1)
				throw new IllegalArgumentException("El metodo snap no puede recibir como "+
							"argumentos GeometryCollection o subclases con mas de un elemento");
		
		}
		
		SnappingCoordinateMap coordinateMap = new SnappingCoordinateMap(snapTolerance);
		Geometry solution = null;
			
		Coordinate[] coords = geom2snap.getCoordinates();
		CoordinateList coordList = new CoordinateList(coords);
			
		for(int j = 0; j < coords.length; j++){ //for each coordinate of the geometry to crack
			Coordinate coord = coords[j];
			Coordinate existingSolution = (Coordinate) coordinateMap.get(coord);
		
			if(coordinateMap.get(coord) != null){//this coordinate has already been snapped
				coordList.set(j, existingSolution);//we exchange the coordinate for the already computed
				continue;
			}
			
			//for the crack operation, we are going to compute the weighted average of a coordinate
			//with all the coordinates of the cracking geometries in cluster tolerance radius
			ArrayList<SnapCoordinate> coordsToSnap = new ArrayList<SnapCoordinate>();
			
			//we add the current coordinate
			SnapCoordinate snapCoord = new SnapCoordinate();
			snapCoord.weight = weightGeom;
			snapCoord.coordinate = coord;
			coordsToSnap.add(snapCoord);
			
			//and now we look for coordinates in the snap tolerance radius
			
			for (int k = 0; k < geoms.length; k++){//for each cracking geometry
				Geometry geom2 = geoms[k];
				Coordinate[] coords2 = geom2.getCoordinates();
				for( int z = 0; z < coords2.length; z++){
					Coordinate coord2 = coords2[z];
					if(SnapCGAlgorithms.snapEquals2D(coord, coord2, snapTolerance)){
						snapCoord = new SnapCoordinate();
						snapCoord.weight = weights[k];
						snapCoord.coordinate = coord2;
						coordsToSnap.add(snapCoord);
					}//if
				}//for z
			}//for k
			
			Coordinate newCoord = snapAverage(coordsToSnap);
			coordList.set(j, newCoord);
			coordinateMap.put(newCoord, newCoord);
		}//for j
			
		Coordinate[] newPts = coordList.toCoordinateArray();
		
		String geometryType = geom2snap.getGeometryType();
		
		if(geometryType.equalsIgnoreCase("GeometryCollection") ||
		   geometryType.equalsIgnoreCase("MultiPolygon") ||
		   geometryType.equalsIgnoreCase("MultiLineString") ||
		   geometryType.equalsIgnoreCase("MultiPoint")){
			
			GeometryCollection geomCol = (GeometryCollection) geom2snap;
			
			//we have already checked that it has no more than 1 geometry
			geom2snap = geomCol.getGeometryN(0);
			geometryType = geom2snap.getGeometryType();
		}
		
		
		if(geometryType.equalsIgnoreCase("Polygon")){
			int[] indexOfParts = JtsUtil.getIndexOfParts((Polygon)geom2snap);
			solution = JtsUtil.createPolygon(newPts, indexOfParts);
		}else{
			solution= JtsUtil.createGeometry(newPts, geometryType);
		}
		
		return solution;
	}
	
	/**
	 * Snaps the coordinates of the given geometry to avoid coordinates
	 * at a distance lower than the cluster tolerance.
	 * 
	 * @param geom original geometry
	 * @return geometry whose vertex has been snapped.
	 *    Maybe this solution wont pass the isValid() JTS test
	 *    (we dont check for 
	 * 
	 * 
	 * 
	 * 
	 * @throws IllegalArgumentException if the resulting geometry
	 * doesnt pass the main checks of geometry construction
	 * 
	 *  
	 */
	public Geometry snap(Geometry geom) throws GeometryCollapsedException{
		Geometry solution = null;
		Coordinate[] coords = null;
		if(geom.getGeometryType().equalsIgnoreCase("MultiLineString")){
			MultiLineString multiLine = (MultiLineString) geom;
			int numGeometries = multiLine.getNumGeometries();
			LineString[] lineStrings = new LineString[numGeometries];
			for(int i = 0; i < numGeometries; i++){
				lineStrings[i] = (LineString) snap(multiLine.getGeometryN(i));
			}
			return JtsUtil.GEOMETRY_FACTORY.createMultiLineString(lineStrings);
			
		}else if(geom.getGeometryType().equalsIgnoreCase("MultiPolygon")){
			MultiPolygon multiPolygon = (MultiPolygon) geom;
			int numGeometries = multiPolygon.getNumGeometries();
			Polygon[] polygons = new Polygon[numGeometries];
			for(int i = 0; i < numGeometries; i++){
				polygons[i] = (Polygon) snap(multiPolygon.getGeometryN(i));
			}
			return JtsUtil.GEOMETRY_FACTORY.createMultiPolygon(polygons);
			
			
		}else if(geom.getGeometryType().equalsIgnoreCase("GeometryCollection")){
			GeometryCollection geomCol = (GeometryCollection) geom;
			int numGeometries = geomCol.getNumGeometries();
			Geometry[] geoms = new Geometry[numGeometries];
			for(int i = 0; i < numGeometries; i++){
				geoms[i] = snap(geomCol.getGeometryN(i));
			}
			return JtsUtil.GEOMETRY_FACTORY.createGeometryCollection(geoms);
		}else if(geom.getGeometryType().equalsIgnoreCase("LineString") || 
					geom.getGeometryType().equalsIgnoreCase("LinearRing")){
			coords = ((LineString)geom).getCoordinates();
		}else if(geom.getGeometryType().equalsIgnoreCase("Polygon") ){
			Polygon polygon = (Polygon) geom;
			LineString shell = polygon.getExteriorRing();
			LinearRing snapedShell = (LinearRing) snap(shell);
			int numHoles = polygon.getNumInteriorRing();
			LinearRing[] newHoles = new LinearRing[numHoles];
			for(int i = 0; i < numHoles; i++){
				LineString hole = polygon.getInteriorRingN(i);
				newHoles[i] = (LinearRing) snap(hole);
			}
			return JtsUtil.GEOMETRY_FACTORY.createPolygon(snapedShell, newHoles);
		}
		
		SnapCoordinateList snapCoordList = 
			new SnapCoordinateList(coords, this.snapTolerance);
		Coordinate[] snappedCoords = snapCoordList.toCoordinateArray();
		try{
			solution =  JtsUtil.createGeometry(snappedCoords, 
									geom.getGeometryType());
			if((solution.getLength() < this.snapTolerance))
				throw new GeometryCollapsedException("Geometria lineal colapsada al aplicar snap");
			
			if(solution.getDimension() > 1 && (solution.getArea() < snapTolerance * snapTolerance))
				throw new GeometryCollapsedException("Area colapsada a linea al aplicar snap");
			
			//FIXME The last check if verify if the solution is a JTS valid geometry
			//but to do that we must check this with original geometry
			if(!solution.isValid())
				throw new GeometryCollapsedException("La geometria resultante no es una geometria valida");
		}catch(IllegalArgumentException e){
			throw new GeometryCollapsedException("JTS no es capaz de construir geometria al snapear sus coordenadas", e);
		}
		return solution;
	}
	
	/**
	 * Creates a geometry of the same type as geom with newPts coordinates.
	 * geom mustn be a GeometryCollection (MultiPolygon, etc.)
	 * @param newPts
	 * @param geom
	 * @return
	 */
	
	//TODO See how to manage the snap of polygon holes
	

	public Coordinate snapAverage(List<Coordinate> coordinates, 
												int[] weights){
		List<SnapCoordinate> snapList = new ArrayList<SnapCoordinate>();
		for(int i = 0; i < coordinates.size(); i++){
			Coordinate coord = coordinates.get(i);
			int weight = weights[i];
			SnapCoordinate snap = new SnapCoordinate();
			snap.weight = weight;
			snap.coordinate = coord;
			
			snapList.add(snap);
		}
		
		return snapAverage(snapList);
	}
	
	
	private Coordinate snapAverage(List<SnapCoordinate> coordsToSnap){
		
		if(coordsToSnap.size() == 1)
		{
			return coordsToSnap.get(0).coordinate;
		}
		
		double sumX = 0d;
		double sumY = 0d;
		double sumW = 0d;
		
		for(int i = 0; i < coordsToSnap.size(); i++){
			SnapCoordinate snapCoord = coordsToSnap.get(i);
			Coordinate coord = snapCoord.coordinate;
			int weight = snapCoord.weight;
			if(weight == 0)
				continue;
			
			double weightD = 1d / (double)weight;
			sumX += weightD * coord.x;
			sumY += weightD * coord.y;
			sumW += weightD;
		}
		
		double newX = sumX / sumW;
		double newY = sumY / sumW;
		
		return new Coordinate(newX, newY);
	}
		
	
	/**
	 * If geometry 'a' is not exactly closed, but its extremes are 
	 * in a snap radius, its snap the last point to the first point of the
	 * geometry
	 * @param a
	 * @return
	 */
	public Geometry normalizeExtremes(Geometry a){
		Coordinate[] coords = a.getCoordinates();
		Coordinate firstPoint = coords[0];
		Coordinate lastPoint = coords[coords.length - 1];
		if(!firstPoint.equals2D(lastPoint) && JtsUtil.isClosed(a, snapTolerance)){
		    CoordinateList coordList = new CoordinateList(coords);
		    coordList.set(coords.length - 1, coords[0]);
//		    return createGeometry(coordList.toCoordinateArray(), a);
		    return JtsUtil.createGeometry(coordList.toCoordinateArray(), 
		    		a.getGeometryType());
		}else
			return a;
	}
	
	
	/**
	 * Snaps coordinates in srcPts with coordinates of snapPts.
	 * This method forces the snap: the points of srcPoints are shifted to
	 * snap with points in snapPts. No average is computed for coordinates.
	 * @param srcPts
	 * @param isClosed
	 * @param snapPts
	 * @return
	 */

	private Coordinate[] snapTo(Coordinate[] srcPts, 
							boolean isClosed,
							Coordinate[] snapPts) {

		CoordinateList coordList = new CoordinateList(srcPts);
		int coordinateCount = coordList.size();
		if(isClosed)
			coordinateCount --;
		for (int i = 0; i < coordinateCount; i++) {
			Coordinate srcPt = (Coordinate) coordList.get(i);
			Coordinate snapVert = findSnapForVertex(srcPt, snapPts);
			if (snapVert != null) {
				coordList.set(i, new Coordinate(snapVert));
				if (i == 0 && isClosed)
					coordList.set(coordList.size() - 1,
								new Coordinate(snapVert));
			}//if
		}//for

		Coordinate[] newPts = coordList.toCoordinateArray();
		return newPts;
	}

	

	private Coordinate findSnapForVertex(Coordinate pt, Coordinate[] snapPts) {
		for (int i = 0; i < snapPts.length; i++) {
			if (pt.equals2D(snapPts[i]))
				return null;
			if(SnapCGAlgorithms.snapEquals2D(pt, snapPts[i], snapTolerance)){
				return snapPts[i];
			}
		}
		return null;
	}
}

