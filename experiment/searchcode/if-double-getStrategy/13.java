/*
 * Created on 10-oct-2006
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
 * $Id: LineCleanVisitor.java 13881 2007-09-19 16:22:04Z jaume $
 * $Log$
 * Revision 1.4  2007-09-19 16:09:14  jaume
 * removed unnecessary imports
 *
 * Revision 1.3  2007/07/12 11:10:24  azabala
 * bug 2617 solved (clean fails with multilinestring geometries)
 *
 * Revision 1.2  2007/03/06 16:48:14  caballero
 * Exceptions
 *
 * Revision 1.1  2006/12/21 17:23:27  azabala
 * *** empty log message ***
 *
 * Revision 1.1  2006/12/04 19:42:23  azabala
 * *** empty log message ***
 *
 * Revision 1.7  2006/11/14 18:34:16  azabala
 * *** empty log message ***
 *
 * Revision 1.6  2006/11/14 18:01:09  azabala
 * removed system.out.println
 *
 * Revision 1.5  2006/11/13 20:41:08  azabala
 * *** empty log message ***
 *
 * Revision 1.4  2006/10/19 16:06:48  azabala
 * *** empty log message ***
 *
 * Revision 1.3  2006/10/17 18:27:24  azabala
 * *** empty log message ***
 *
 * Revision 1.1  2006/10/10 18:50:17  azabala
 * First version in CVS
 *
 *
 */
package com.iver.cit.gvsig.geoprocess.impl.lineclean.fmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.exceptions.visitors.ProcessVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StartVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.VisitorException;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.v02.FConverter;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.ILayerDefinition;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.layerOperations.AlphanumericData;
import com.iver.cit.gvsig.fmap.layers.layerOperations.VectorialData;
import com.iver.cit.gvsig.fmap.operations.strategies.FeatureVisitor;
import com.iver.cit.gvsig.fmap.operations.strategies.Strategy;
import com.iver.cit.gvsig.fmap.operations.strategies.StrategyManager;
import com.iver.cit.gvsig.geoprocess.core.fmap.FeatureFactory;
import com.iver.cit.gvsig.geoprocess.core.fmap.FeaturePersisterProcessor2;
import com.iver.cit.gvsig.geoprocess.core.fmap.FeatureProcessor;
import com.iver.cit.gvsig.geoprocess.core.fmap.XTypes;
import com.iver.cit.gvsig.util.SnappingCoordinateMap;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geomgraph.Node;
import com.vividsolutions.jts.geomgraph.NodeFactory;
import com.vividsolutions.jts.geomgraph.SnappingNodeMap;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;
import com.vividsolutions.jts.operation.overlay.SnappingOverlayOperation;

/**
 * <p>
 * This visitor operates with features whose geometries are lines. <br>
 * For each visited feature, it looks for features in its proximity (spatial
 * query). If these features hasnt been processed, it intersects the visited
 * feature with all of the neighbour features. If intersection points are not
 * nodes (end points), it ignores them. split the
 * </p>
 * 
 * @author azabala
 */
public class LineCleanVisitor implements FeatureVisitor {

	/**
	 * Recordset of the layer we are working with
	 */
	protected SelectableDataSource recordset;

	/**
	 * Layer which we are cleaning
	 */
	protected FLyrVect layerToClean;

	/**
	 * marks if we are going to clean only layer selection
	 */
	protected boolean cleanOnlySelection;

	/*
	 * TODO Meter esto en preferencias
	 */
	public final static double DEFAULT_SNAP = 0.1;

	protected double snapTolerance = DEFAULT_SNAP;

	/**
	 * It marks all processed features (to ignore them in future intersections)
	 */
	protected FBitSet processedFeatures;

	/**
	 * Strategy of the layer we are working with.
	 */
	protected Strategy strategy;

	/**
	 * Saves features resulting of cleaning process
	 */
	protected FeatureProcessor featureProcessor;

	protected ILayerDefinition layerDefinition;

	/**
	 * Saves pseudonodes found
	 */
	protected FeatureProcessor intersectProcessor;

	protected ILayerDefinition intersectDefinition;

	/**
	 * Counter of new features
	 */
	int fid = 0;

	/**
	 * It caches all written pseudonodes, to avoid writing the same pseudonode
	 * twice.
	 * 
	 */
	SnappingCoordinateMap snapCoordMap;

	/**
	 * Constructor.
	 * 
	 * @param processor
	 * @param intersectsProcessor
	 * @param cleanOnlySelection
	 * @param layerDefinition
	 * @param intersectDefinition
	 * @param firstLayer
	 * @param source
	 * @param snapCoordMap
	 */
	public LineCleanVisitor(FeatureProcessor processor,
			FeaturePersisterProcessor2 intersectsProcessor,
			boolean cleanOnlySelection, ILayerDefinition layerDefinition,
			ILayerDefinition intersectDefinition, FLyrVect firstLayer,
			SelectableDataSource source, SnappingCoordinateMap snapCoordMap) {
		this.featureProcessor = processor;
		this.cleanOnlySelection = cleanOnlySelection;
		processedFeatures = new FBitSet();
		this.layerDefinition = layerDefinition;
		this.intersectProcessor = intersectsProcessor;
		this.intersectDefinition = intersectDefinition;
		this.layerToClean = firstLayer;
		this.recordset = source;
		this.strategy = StrategyManager.getStrategy(layerToClean);
		this.snapCoordMap = snapCoordMap;
	}

	public void setLayerDefinition(ILayerDefinition layerDefinition) {
		this.layerDefinition = layerDefinition;
	}

	private boolean checkForLineGeometry(Geometry geometry) {
		if (geometry instanceof LineString)
			return true;
		if (geometry instanceof MultiLineString)
			return true;
		if (geometry instanceof GeometryCollection) {
			GeometryCollection col = (GeometryCollection) geometry;
			for (int i = 0; i < col.getNumGeometries(); i++) {
				if (!checkForLineGeometry(col.getGeometryN(i)))
					return false;
			}
			return true;
		}
		return false;
	}

	public void visit(IGeometry g, final int index) throws VisitorException,
			StopWriterVisitorException, ProcessVisitorException {
		// first, we check it isnt a null geometry and the geometry type
		// is correct
		if (g == null)
			return;
		int geometryType = g.getGeometryType();
		if (geometryType != XTypes.ARC && geometryType != XTypes.LINE
				&& geometryType != XTypes.MULTI)
			return;

		// after that, if we are going to clean only selected features, we
		// check if this feature is selected
		if (cleanOnlySelection) {
			try {
				if (!layerToClean.getRecordset().getSelection().get(index))
					return;
			} catch (ReadDriverException e) {
				throw new ProcessVisitorException(recordset.getName(), e,
						"Error verificando seleccion en CLEAN");
			}
		}// if cleanOnly

		final Geometry jtsGeo = g.toJTSGeometry();

		// we check if jts geometry is a line (or a line collection)
		if (!checkForLineGeometry(jtsGeo))
			return;

		final SnappingNodeMap nodes = new SnappingNodeMap(new NodeFactory(),
				snapTolerance);
		/*
		 * Se nos plantea una problematica. Tenemos dos features: A y B, y he
		 * calculado la interseccion de A y B. Ahora, hay dos alternativas: -a)
		 * nunca mas calcular esta intersecci?n, pero almacenar su resultado en
		 * SnappingNodeMap. As?, en una segunda pasada, con todos los "Nodos"
		 * calculados, podr?amos fragmentar las lineas de forma individual.
		 * PROBLEMA: SnappingNodeMap ir?a creciendo, almacenando todos los nodos
		 * de una capa....(si se apoya sobre un IndexedShpDriver, no tendr?a por
		 * qu?)
		 * 
		 * 
		 * -b) Calcular la intersecci?n en los dos sentidos: A int B, y luego al
		 * procesar B, B int A. En este caso, pasamos ol?mpicamente del bitset
		 * 
		 * De momento, por simplicidad, seguimos la alternativa -b)
		 */

		final boolean onlySelection = cleanOnlySelection;
		try {
			strategy.process(new FeatureVisitor() {

				SnappingOverlayOperation overlayOp = null;

				/**
				 * From a given geometry, it returns its nodes (coordinate for a
				 * point, extreme coordinates for a line, first coordinate for a
				 * polygon)
				 */
				private Coordinate[] getNodesFor(Geometry processedGeometry) {
					Coordinate[] geomNodes = null;
					if (processedGeometry instanceof LineString) {
						LineString line = (LineString) processedGeometry;
						geomNodes = new Coordinate[2];
						geomNodes[0] = line.getCoordinateN(0);
						geomNodes[1] = line
								.getCoordinateN(line.getNumPoints() - 1);
					} else if (processedGeometry instanceof MultiLineString) {
						MultiLineString lines = (MultiLineString) processedGeometry;
						int numLines = lines.getNumGeometries();
						geomNodes = new Coordinate[2 * numLines];
						int index = 0;
						for (int i = 0; i < numLines; i++) {
							LineString line = (LineString) lines
									.getGeometryN(i);
							geomNodes[index] = line.getCoordinateN(0);
							index++;
							geomNodes[index] = line.getCoordinateN(line
									.getNumPoints() - 1);
							index++;
						}
					} else if (processedGeometry instanceof GeometryCollection) {
						GeometryCollection col = (GeometryCollection) processedGeometry;
						ArrayList coordinates = new ArrayList();
						for (int i = 0; i < col.getNumGeometries(); i++) {
							Geometry geom = col.getGeometryN(i);
							Coordinate[] newNodes = getNodesFor(geom);
							coordinates.addAll(Arrays.asList(newNodes));
						}
					}
					// else {
					// System.out
					// .println("Este proceso solo debe trabajar con lineas");
					// System.out.println(processedGeometry.getGeometryType());
					// }
					return geomNodes;
				}

				/**
				 * Checks if a coordinate is on a node of a given set of nodes
				 * 
				 * @param coord
				 * @param nodes
				 * @return
				 */
				private boolean checkIsNode(Coordinate coord, Coordinate[] nodes) {
					for (int i = 0; i < nodes.length; i++) {
						if (coord.distance(nodes[i]) <= snapTolerance)
							return true;
					}
					return false;
				}

				/**
				 * From a given geometry, and the intersection of this geometry
				 * with another geometry, it creates a new node with these
				 * intersections if its points are not coincident with the nodes
				 * of the original goemetry.
				 * 
				 * @throws VisitorException
				 * 
				 */
				private void processIntersections(
						com.vividsolutions.jts.geomgraph.SnappingNodeMap nodes,
						Geometry processedGeometry, Geometry intersections,
						int fid1, int fid2) throws VisitorException {

					Coordinate[] geomNodes = getNodesFor(processedGeometry);
					if (intersections instanceof Point) {
						Point p = (Point) intersections;
						Coordinate coord = p.getCoordinate();
						if (!checkIsNode(coord, geomNodes)) {
							nodes.addNode(coord);

							/*
							 * We are computing intersections twice: A
							 * intersection B and B intersection A. This is
							 * simpler than manage caches. With this logic, we
							 * avoid to write the same pseudonode twice
							 * 
							 */
							if (snapCoordMap.containsKey(coord))
								return;
							else {
								snapCoordMap.put(coord, coord);
								IFeature feature = createIntersectFeature(
										coord, fid1, fid2);
								intersectProcessor.processFeature(feature);
							}
						}
					} else if (intersections instanceof MultiPoint) {
						MultiPoint points = (MultiPoint) intersections;
						for (int i = 0; i < points.getNumGeometries(); i++) {
							Coordinate coord = ((Point) points.getGeometryN(i))
									.getCoordinate();
							if (!checkIsNode(coord, geomNodes)) {
								nodes.addNode(coord);
								if (snapCoordMap.containsKey(coord))
									return;
								else {
									snapCoordMap.put(coord, coord);
									IFeature feature = createIntersectFeature(
											coord, fid1, fid2);
									intersectProcessor.processFeature(feature);
								}
							}
						}
					} else if (intersections instanceof LineString) {
						LineString line = (LineString) intersections;
						int numPoints = line.getCoordinates().length;
						Coordinate coord1 = line.getCoordinateN(0);
						Coordinate coord2 = line.getCoordinateN(numPoints - 1);
						if (!checkIsNode(coord1, geomNodes)) {
							nodes.addNode(coord1);
							if (snapCoordMap.containsKey(coord1))
								return;
							else {
								snapCoordMap.put(coord1, coord1);
								IFeature feature = createIntersectFeature(
										coord1, fid1, fid2);
								intersectProcessor.processFeature(feature);
							}
						}
						if (!checkIsNode(coord2, geomNodes)) {
							nodes.addNode(coord2);
							if (snapCoordMap.containsKey(coord2))
								return;
							else {
								snapCoordMap.put(coord2, coord2);
								IFeature feature = createIntersectFeature(
										coord2, fid1, fid2);
								intersectProcessor.processFeature(feature);
							}
						}
					} else if (intersections instanceof GeometryCollection) {
						GeometryCollection col = (GeometryCollection) intersections;
						for (int i = 0; i < col.getNumGeometries(); i++) {

							// El tema est? en que aqu? el calculo de los nodos
							// de la geometria intersectada se repite cada vez
							// revisar, pues MultiLineString puede ser un
							// resultado habitual
							processIntersections(nodes, processedGeometry, col
									.getGeometryN(i), fid1, fid2);
						}
					}
					// else if (intersections instanceof Polygon) {
					// System.out
					// .println("Un poligono interseccion de 2 lineas???");
					// }// else

				}

				public void visit(IGeometry g2, int indexOverlay)
						throws VisitorException, StopWriterVisitorException,
						ProcessVisitorException {

					if (g2 == null)
						return;

					if (index == indexOverlay)
						return;

					if (onlySelection) {
						try {
							if (!layerToClean.getRecordset().getSelection()
									.get(indexOverlay))
								return;
						} catch (ReadDriverException e) {
							throw new ProcessVisitorException(recordset
									.getName(), e,
									"Error verificando seleccion en clean");
						}// geometry g is not selected
					}// if onlySelection

					int geometryType = g2.getGeometryType();
					if (geometryType != XTypes.ARC
							&& geometryType != XTypes.LINE
							&& geometryType != XTypes.MULTI)
						return;

					/*
					 * TODO De momento no vamos a tener en cuenta que la
					 * interseccion ya ha sido calculada... (Ver comentario al
					 * instanciar SnappingNodeMap) // ya ha sido tratado
					 * if(processedFeatures.get(indexOverlay)) return;
					 */
					Geometry jtsGeo2 = g2.toJTSGeometry();
					if (!checkForLineGeometry(jtsGeo2))
						return;

					if (overlayOp == null)
						overlayOp = new SnappingOverlayOperation(jtsGeo,
								jtsGeo2, snapTolerance);
					else {
						overlayOp.setSecondGeometry(jtsGeo2);
					}

					Geometry intersections = overlayOp
							.getResultGeometry(SnappingOverlayOperation.INTERSECTION);

					processIntersections(nodes, jtsGeo, intersections, index,
							indexOverlay);

					// IFeature cleanedFeature;
					// try {
					// cleanedFeature = createFeature(newGeoJts,
					// index, indexOverlay);
					// } catch (DriverException e) {
					// throw new VisitException(
					// "Error al crear el feature resultante del CLEAN");
					// }
					// featureProcessor.processFeature(cleanedFeature);

				}

				public String getProcessDescription() {
					return "Computing intersections of a polygon with its adjacents";
				}

				public void stop(FLayer layer)
						throws StopWriterVisitorException, VisitorException {
				}

				public boolean start(FLayer layer) throws StartVisitorException {
					return true;
				}
			}, g.getBounds2D());
			// At this point, nodes variable (SnappingNodeMap)
			// has all intersections of the visited feature with the rest of
			// features of the layer

			// It computes linear distance of a point on the given jtsGeo linear
			// geometry
			boolean rightGeometry = true;
			if (nodes.values().size() > 0) {
				
				LengthIndexedLine lengthLine = new LengthIndexedLine(jtsGeo);
				Iterator nodesIt = nodes.iterator();
				ArrayList nodeIntersections = new ArrayList();
				while (nodesIt.hasNext()) {
					Node node = (Node) nodesIt.next();
					Coordinate coord = node.getCoordinate();
					double lengthOfNode = lengthLine.indexOf(coord);
					LineIntersection inters = new LineIntersection();
					inters.coordinate = coord;
					inters.lenght = lengthOfNode;
					nodeIntersections.add(inters);
				}
				
				if (nodeIntersections.size() > 0) {
					// We sort the intersections by distance along the line
					// (dynamic
					// segmentation)
					rightGeometry = false;
					Collections.sort(nodeIntersections, new Comparator() {
						public int compare(Object arg0, Object arg1) {
							LineIntersection l1 = (LineIntersection) arg0;
							LineIntersection l2 = (LineIntersection) arg1;
							if (l1.lenght > l2.lenght)
								return 1;
							else if (l1.lenght < l2.lenght)
								return -1;
							else
								return 0;
						}
					});

					LinearLocation lastLocation = null;
					LineIntersection lastIntersection = null;
					LocationIndexedLine indexedLine = new LocationIndexedLine(
							jtsGeo);
					for (int i = 0; i < nodeIntersections.size(); i++) {
						Geometry solution = null;
						LineIntersection li = (LineIntersection) nodeIntersections
								.get(i);

						LinearLocation location = indexedLine
								.indexOf(li.coordinate);// es posible que esto
														// est? mal por no
														// pasarle una longitud.
														// REVISAR
						if (lastLocation == null) {
							LinearLocation from = new LinearLocation(0, 0d);
							
//							solution = splitLineString(jtsGeo, from, location,
//									null, li);
//							
							
							solution = indexedLine.extractLine(from, location);
							
							
							lastLocation = location;
							lastIntersection = li;
							/*
							 * Construimos una linea desde el primer punto hasta
							 * el punto contenido en LineIntersection, con todos
							 * los puntos intermedios de la linea.
							 * 
							 * 
							 * 
							 */
						} else {
							// Construimos una linea entre lastIntersection y la
							// intersection
							// actual
							LinearLocation locationFrom = lastLocation;
							
//							solution = splitLineString(jtsGeo, locationFrom,
//									location, lastIntersection, li);
							
							solution = indexedLine.extractLine(locationFrom, location);
							lastLocation = location;
							lastIntersection = li;

						}

						IFeature feature = createFeature(solution, index);
						featureProcessor.processFeature(feature);
						// TODO Podriamos guardar los puntos de interseccion
						// para
						// mostrar al usuario que puntos eran pseudonodos
					}// for

					// a?adimos el ultimo segmento
//					Coordinate[] geomCoords = jtsGeo.getCoordinates();
//					ArrayList coordinates = new ArrayList();
//					coordinates.add(lastIntersection.coordinate);
//					int startIndex = lastLocation.getSegmentIndex() + 1;
//					for (int i = startIndex; i < geomCoords.length; i++) {
//						coordinates.add(geomCoords[i]);
//					}
//					Coordinate[] solutionCoords = new Coordinate[coordinates
//							.size()];
//					coordinates.toArray(solutionCoords);
//					IFeature lastFeature = createFeature(new GeometryFactory()
//							.createLineString(solutionCoords), index);
					LinearLocation endLocation = new LinearLocation();
					endLocation.setToEnd(jtsGeo);
					Geometry geo = indexedLine.extractLine(lastLocation, endLocation);
					IFeature lastFeature = createFeature(geo, index);
					featureProcessor.processFeature(lastFeature);

				}
			} 
			if(rightGeometry){
				IFeature feature = createFeature(g, index);
				featureProcessor.processFeature(feature);
			}
			

		} catch (ReadDriverException e) {
			throw new ProcessVisitorException(recordset.getName(), e,
					"Error buscando los overlays que intersectan con un feature");
		} 
	}
	
	
	
	
	
	
	
//Dada una linea, una localizacion de partida (from), una localizaci?n de llegada (to), y dos puntos de interseccion (from) devuelve la linea entre from y to
	private Geometry splitLineString(Geometry linearGeometry,
			LinearLocation from, LinearLocation to, LineIntersection fromInt,
			LineIntersection toInt) {
		Coordinate[] geomCoords = linearGeometry.getCoordinates();
		ArrayList coordinates = new ArrayList();
		if (fromInt != null)
			coordinates.add(fromInt.coordinate);
		int startIndex = from.getSegmentIndex();
		/*
		 * segmentIndex siempre referencia al punto inmediatamente anterior del
		 * lineString. Nos interesa sumar 1, a no ser que sea el primer punto
		 * del linestring
		 */
		if (startIndex != 0)
			startIndex++;

		for (int i = startIndex; i <= to.getSegmentIndex(); i++) {
			coordinates.add(geomCoords[i]);
		}
		coordinates.add(toInt.coordinate);
		Coordinate[] solutionCoords = new Coordinate[coordinates.size()];
		coordinates.toArray(solutionCoords);
		return new GeometryFactory().createLineString(solutionCoords);

	}

	class LineIntersection {
		Coordinate coordinate;

		double lenght;
	}

	public String getProcessDescription() {
		return "Cleaning lines of a vectorial line layer";
	}

	public void stop(FLayer layer) throws StopWriterVisitorException,
			VisitorException {
		this.featureProcessor.finish();
		this.intersectProcessor.finish();
	}

	public boolean start(FLayer layer) throws StartVisitorException {
		if (layer instanceof AlphanumericData && layer instanceof VectorialData) {
			try {
				layerToClean = (FLyrVect) layer;
				recordset = ((AlphanumericData) layer).getRecordset();
				strategy = StrategyManager.getStrategy(layerToClean);
				featureProcessor.start();
				intersectProcessor.start();
			} catch (ReadDriverException e) {
				return false;
			}
			return true;
		}
		return false;
	}

	private IFeature createFeature(Geometry jtsGeometry, int firstLayerIndex)
			throws ReadDriverException {
		IFeature solution = null;
		IGeometry cleanedGeometry = FConverter.jts_to_igeometry(jtsGeometry);
		FieldDescription[] fields = layerDefinition.getFieldsDesc();
		int numFields = fields.length;
		Value[] featureAttr = new Value[fields.length];
		for (int indexField = 0; indexField < numFields; indexField++) {
			// for each field of firstRs
			String fieldName = recordset.getFieldName(indexField);
			for (int j = 0; j < fields.length; j++) {
				if (fieldName.equalsIgnoreCase(fields[j].getFieldName())) {
					featureAttr[j] = recordset.getFieldValue(firstLayerIndex,
							indexField);
					break;
				}// if
			}// for
		}// for
		// now we put null values
		for (int i = 0; i < featureAttr.length; i++) {
			if (featureAttr[i] == null)
				featureAttr[i] = ValueFactory.createNullValue();
		}
		solution = FeatureFactory.createFeature(featureAttr, cleanedGeometry);
		return solution;
	}

	private IFeature createIntersectFeature(Coordinate coord, int fid1, int fid2) {
		IFeature solution = null;
		Point point = FConverter.geomFactory.createPoint(coord);
		IGeometry cleanedGeometry = FConverter.jts_to_igeometry(point);
		Value[] values = new Value[2];
		values[0] = ValueFactory.createValue(fid1);
		values[1] = ValueFactory.createValue(fid2);
		solution = FeatureFactory.createFeature(values, cleanedGeometry);
		return solution;
	}

	private IFeature createFeature(IGeometry g, int firstLayerIndex)
			throws ReadDriverException {
		IFeature solution = null;
		FieldDescription[] fields = layerDefinition.getFieldsDesc();
		int numFields = fields.length;
		Value[] featureAttr = new Value[fields.length];
		for (int indexField = 0; indexField < numFields; indexField++) {
			// for each field of firstRs
			String fieldName = recordset.getFieldName(indexField);
			for (int j = 0; j < fields.length; j++) {
				if (fieldName.equalsIgnoreCase(fields[j].getFieldName())) {
					featureAttr[j] = recordset.getFieldValue(firstLayerIndex,
							indexField);
					break;
				}// if
			}// for
		}// for
		// now we put null values
		for (int i = 0; i < featureAttr.length; i++) {
			if (featureAttr[i] == null)
				featureAttr[i] = ValueFactory.createNullValue();
		}
		solution = FeatureFactory.createFeature(featureAttr, g);
		return solution;
	}

	// public static void main(String[] args) {
	// DriverManager dm = new DriverManager();
	// dm.setValidation(new DriverValidation() {
	// public boolean validate(Driver d) {
	// return ((d instanceof ObjectDriver)
	// || (d instanceof FileDriver) || (d instanceof DBDriver));
	// }
	// });
	// dm.loadDrivers(new File(
	// "../_fwAndami/gvSIG/extensiones/com.iver.cit.gvsig/drivers"));
	// LayerFactory
	// .setDriversPath("../_fwAndami/gvSIG/extensiones/com.iver.cit.gvsig/drivers");
	//
	// // Setup del factory de DataSources
	// DataSourceFactory dsf = LayerFactory.getDataSourceFactory();
	// dsf.setDriverManager(dm);
	//
	// // Setup de las tablas
	// // dsf.addFileDataSource("gdbms dbf driver", "nodes", "c:/nodes.dbf");
	// // dsf.addFileDataSource("gdbms dbf driver", "edges", "c:/edges.dbf");
	//
	// IProjection prj = CRSFactory.getCRS("EPSG:23030");
	// File shpFile = new File("C:/JUMP/datos/cauces_gv.shp");
	// try {
	// FLyrVect lyr = (FLyrVect) LayerFactory.createLayer("Ejes",
	// "gvSIG shp driver", shpFile, prj);
	// System.out.println(lyr.getSource().getShapeCount());
	//
	// LayerDefinition definition = new LayerDefinition();
	// FieldDescription cauNom = new FieldDescription();
	// cauNom.setFieldName("CAUNOM");
	// cauNom.setFieldType(XTypes.CHAR);
	// cauNom.setFieldLength(10);
	// cauNom.setFieldDecimalCount(0);
	// definition.setFieldsDesc(new FieldDescription[]{cauNom});
	// definition.setShapeType(XTypes.LINE);
	//
	//
	//
	//
	// // LineCleanVisitor visitor = new LineCleanVisitor(
	// // new FeatureProcessor() {
	// //
	// // public void processFeature(IRow feature) {
	// // // TODO Auto-generated method stub
	// //
	// // }
	// //
	// // public void finish() {
	// // // TODO Auto-generated method stub
	// //
	// // }
	// //
	// // public void start() throws EditionException {
	// // // TODO Auto-generated method stub
	// //
	// // }
	// // }, false, definition);
	// Strategy str = StrategyManager.getStrategy(lyr);
	// // str.process(visitor);
	//
	// // } catch (com.iver.cit.gvsig.fmap.DriverException e) {
	// // // TODO Auto-generated catch block
	// // e.printStackTrace();
	// // } catch (DriverIOException e) {
	// // // TODO Auto-generated catch block
	// // e.printStackTrace();
	// // } catch (VisitException e) {
	// // // TODO Auto-generated catch block
	// // e.printStackTrace();
	// // }
	// }

}

