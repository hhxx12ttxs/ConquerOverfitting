/*
 * Created on 25-abr-2006
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
* $Id: NearestHeuristicSpatialJoinVisitor.java 13874 2007-09-19 16:12:18Z jaume $
* $Log$
* Revision 1.3  2007-09-19 16:08:13  jaume
* ReadExpansionFileException removed from this context
*
* Revision 1.2  2007/03/06 16:47:58  caballero
* Exceptions
*
* Revision 1.1  2006/06/20 18:20:45  azabala
* first version in cvs
*
* Revision 1.2  2006/06/02 18:21:28  azabala
* *** empty log message ***
*
* Revision 1.1  2006/05/24 21:09:47  azabala
* primera version en cvs despues de refactoring orientado a crear un framework extensible de geoprocessing
*
* Revision 1.1  2006/05/01 19:09:09  azabala
* Intento de optimizar el spatial join por vecino mas proximo (no funciona)
*
*
*/
package com.iver.cit.gvsig.geoprocess.impl.spatialjoin.fmap;

import java.awt.geom.Rectangle2D;
import java.util.Stack;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.exceptions.visitors.ProcessVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StartVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.VisitorException;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.operations.strategies.FeatureVisitor;
import com.iver.cit.gvsig.geoprocess.core.fmap.FeatureProcessor;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.operation.distance.DistanceOp;
/**
 * This visitor does nearest feature spatial join by applying an heuristic
 * strategy (in constract with NearestSpatialJoinVisitor, that does a
 * secuential scanning).
 * <br>
 * Which heuristic does this visitor apply?
 * It obtains second layer (target layer in spatial join) full extent,
 * and subdivide it in 4 envelopes. After that, it computes the distance
 * of the geometry we want to join with second layer, and computes
 * 4 distances with each one of the envelopes. Then, it recursively subdivide
 * the Envelope at the shortest distance.
 * <br>
 * This process is repeated recursively until we obtain a nearest envelope
 * of a parametrized dimension. After that, it makes a spatial query with
 * this envelope on the layer B. If this query doesnt return, repeat the
 * spatial query with the envelope that originated this envelope (we take
 * the parent node in the quad-tree structure).
 * <br>
 * A critical aspect is optimization of the number of levels of quad-tree.
 *
 * If we take very few levels, the spatial query will return a lot of
 * candidates to nearest, so we wont get advantage of this stuff.
 *
 * If we take a lot of levels, we wont get result in the spatial queries,
 * and we'll have to do a lot of querys.
 *
 * @author azabala
 *
 * FIXME EL ALGORITMO FALLA!!!!!!!! EL QUADTREE ES UNA ESTRUCTURA
 * BUENA PARA RECTANGULOS, PERO PARA PUNTOS CREO QUE NO FUNCIONA.
 * NO TIENE EN CUENTA LOS EXTREMOS DE LOS RECTANGULOS
 *
 */
public class NearestHeuristicSpatialJoinVisitor extends NearestSpatialJoinVisitor {

	private QuadTreeUtil quadTree = new QuadTreeUtil();
	/**
	 * Full extent of the layer where we are looking for
	 * features to join by spatial criteria
	 */
	private Envelope targetLayerEnv = null;
	/**
	 *
	 * @param sourceLayer
	 * @param targetLayer
	 * @param processor
	 * @throws DriverException
	 */
	public NearestHeuristicSpatialJoinVisitor(FLyrVect sourceLayer,
			FLyrVect targetLayer,
			FeatureProcessor processor) throws ReadDriverException {
		super(sourceLayer, targetLayer, processor);

		Rectangle2D rect;
		try {
			rect = targetLayer.getFullExtent();
		} catch (ExpansionFileReadException e) {
			throw new ReadDriverException(sourceLayer.getName(),e);
		}
		targetLayerEnv = new Envelope(rect.getMinX(),
				rect.getMaxX(),
				rect.getMinY(),
				rect.getMaxY());

	}
	//	TODO If we need a class to look for nearest feature to a given
	//feature, move to a public class
	class LookForNearest implements FeatureVisitor{
		/**
		 * Index of the nearest processed feature to the given geometry
		 */
		int nearestFeatureIndex = -1;
		/**
		 * min distance of the features processed in the search of
		 * nearest feature
		 */
		double minDistance = Double.MAX_VALUE;
		/**
		 * Geometry whose nearest feature we want to locate
		 */
		Geometry firstG;
		/**
		 * It this selectin is != null, in our search we will only
		 * consideer features selected.
		 */
		FBitSet selection;

		public boolean hasFoundShortest(){
			return nearestFeatureIndex != -1;
		}

		public int getNearestFeatureIndex(){
			return nearestFeatureIndex;
		}

		public void setSelection(FBitSet bitSet){
			this.selection = bitSet;
		}

		public void setGeometry(Geometry firstG){
			this.firstG = firstG;
		}

		public void visit(IGeometry g, int index) throws VisitorException, ProcessVisitorException {
			if(selection != null){
				if(! selection.get(index)){
					return;
				}
			}
			double dist = firstG.distance(g.toJTSGeometry());
			if(dist < minDistance){
				minDistance = dist;
				nearestFeatureIndex = index;
			}//if
		}
		public String getProcessDescription() {
			return "";
		}
		public void stop(FLayer layer) throws VisitorException {
		}
		public boolean start(FLayer layer) throws StartVisitorException {
			return true;
		}
	};
	/**
	 * Processes a Feature of source layer, looking for its nearest feature of
	 * target layer and taking attributes from it
	 */
	public void visit(IGeometry g, int sourceIndex) throws VisitorException, ProcessVisitorException {
		if(g == null)
			return;
		final Geometry geometry = g.toJTSGeometry();
		Stack stackOfEnvelopes = quadTree.getNearestEnvelopeOfIdealDimension(geometry,
				targetLayerEnv);
		LookForNearest visitor = new LookForNearest();
		visitor.setGeometry(geometry);
		while((stackOfEnvelopes.size() > 0) ) {
			Envelope envelope = (Envelope) stackOfEnvelopes.pop();
			Rectangle2D.Double rect = new Rectangle2D.Double(envelope.getMinX(),
					envelope.getMinY(),
					envelope.getWidth(),
					envelope.getHeight());
			try {
				if(onlySecondLayerSelection){
					visitor.setSelection(targetRecordset.getSelection());
				}
				strategy.process(visitor, rect);
				if(visitor.hasFoundShortest()){
					int targetIndex = visitor.getNearestFeatureIndex();
					IFeature joinedFeature = createFeature(g,
														sourceIndex,
														targetIndex, -1);
					this.featureProcessor.processFeature(joinedFeature);
					return;
				}
			} catch (ReadDriverException e) {
				throw new ProcessVisitorException(targetRecordset.getName(),e,"Error accediendo a los datos buscando el feature mas proximo");
			} 
		}//while

	}

	/**
	 * FIXME Refinar muy mucho, pero de momento me vale para hacer la busqueda
	 * de la geometria mas proxima a una dada mediante subdivisi?n del espacio.
	 *
	 * @author azabala
	 *
	 */
	class QuadTreeUtil{
		double DEFAULT_IDEAL_DIMENSION = 500d;

		double idealDimension = DEFAULT_IDEAL_DIMENSION;

		public void setIdealDimension(double idealDimension){
			this.idealDimension = idealDimension;
		}

		public double distance(Geometry geo, Envelope rect){
			GeometryFactory geoFact = new GeometryFactory();
			Geometry poly = geoFact.toGeometry(rect);
			return DistanceOp.distance(geo, poly);
		}

		public double getMaxDimension(Envelope env){
			double w = env.getWidth();
			double h = env.getHeight();
			return (w > h ? w : h);
		}


		public Stack getNearestEnvelopeOfIdealDimension(Geometry nearest,
				Envelope originalEnvelope){
			//stack with all the hierarchical envelopes of the solution quad
			Stack solution = new Stack();
			Envelope firstsolution = originalEnvelope;
			//the last try will be the full extent
			solution.push(firstsolution);
			double maxDimension = getMaxDimension(originalEnvelope);
			while(maxDimension > idealDimension){
				Envelope[] quads = getNextQtreeLevel(firstsolution);
				double d0 = distance(nearest, quads[0]);
				double d1 = distance(nearest, quads[1]);
				double d2 = distance(nearest, quads[2]);
				double d3 = distance(nearest, quads[3]);
				if(d0 <= d1 && d0 <= d2 && d0 <= d3 )
					firstsolution = quads[0];
				else if(d1 <= d0 && d1 <= d2 && d1 <= d3)
					firstsolution = quads[1];
				else if(d2 <= d0 && d2 <= d1 && d2 <= d3)
					firstsolution = quads[2];
				else
					firstsolution = quads[3];
				solution.push(firstsolution);
				maxDimension = getMaxDimension(firstsolution);
			}
			return solution;

		}

		public  Envelope[] getNextQtreeLevel(Envelope rect){
			Envelope[] solution = new Envelope[4];
			int SW = 0;
			int SE = 1;
			int NW = 2;
			int NE = 3;
			double xMin = rect.getMinX();
			double xMax = rect.getMaxX();
			double yMin = rect.getMinY();
			double yMax = rect.getMaxY();
			double xCenter = (xMin + xMax) / 2d;
			double yCenter = (yMin + yMax) / 2d;
			Envelope r1 = new Envelope(xMin, xCenter, yMin, yCenter);
			Envelope r2 = new Envelope(xCenter, xMax, yMin, yCenter);
			Envelope r3 = new Envelope(xMin, xCenter, yCenter, yMax);
			Envelope r4 = new Envelope(xCenter, xMax, yCenter, yMax);
			solution[SW] = r1;
			solution[SE] = r2;
			solution[NW] = r3;
			solution[NE] = r4;
			return solution;
		}
	}

}


