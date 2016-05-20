/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
 *
 * Copyright (C) 2005 IVER T.I. and Generalitat Valenciana.
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
package org.gvsig.symbology.fmap.labeling.placements;

import java.awt.Rectangle;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.core.v02.FConverter;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.IPlacementConstraints;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.LabelClass;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.LabelLocationMetrics;
import com.iver.utiles.swing.threads.Cancellable;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;


public class PolygonPlacementParallel implements ILabelPlacement{


	public ArrayList<LabelLocationMetrics> guess(LabelClass lc, IGeometry geom,
			IPlacementConstraints placementConstraints,
			double cartographicSymbolSize, Cancellable cancel, ViewPort vp) {

		if (cancel.isCanceled()) return CannotPlaceLabel.NO_PLACES;
		FShape shp = (FShape)geom.getInternalShape();
		Geometry geo = FConverter.java2d_to_jts(shp);
		double theta = 0;
		if (geo == null) {
			return CannotPlaceLabel.NO_PLACES;
		}
		Point pJTS;
		if(placementConstraints.isFitInsidePolygon()){
			pJTS = geo.getInteriorPoint();
			if (pJTS == null) {
				Logger.getAnonymousLogger().log(Level.SEVERE, "no interior point could be found");
				return CannotPlaceLabel.NO_PLACES;
			}
		} else {
			pJTS = geo.getCentroid();

			if (pJTS == null) {
				Logger.getAnonymousLogger().log(Level.SEVERE, "no centroid could be found");
				return CannotPlaceLabel.NO_PLACES;
			}
		}

		Point2D startingPoint = new Point2D.Double(pJTS.getX(), pJTS.getY());

		// calculated with the Linear Regression technique
		PathIterator pi = shp.getPathIterator(null);
		Rectangle geomBounds = shp.getBounds();
		double sumx = 0, sumy = 0, sumxx = 0, sumyy = 0, sumxy = 0;
		double Sxx, Sxy, b, a;
		double[] coords = new double[6];
		int count = 0;

		// add points to the regression process
		Vector<Point2D> v = new Vector<Point2D>();
		while (!pi.isDone()) {
			pi.currentSegment(coords);
			Point2D p;
			if (geomBounds.width > geomBounds.height)
				p = new Point2D.Double(coords[0], coords[1]);
			else
				p = new Point2D.Double(coords[1], coords[0]);
			v.addElement(p);
			count++;
			sumx += p.getX();
			sumy += p.getY();
			sumxx += p.getX()*p.getX();
			sumyy += p.getY()*p.getY();
			sumxy += p.getX()*p.getY();
			pi.next();
		}

		// start regression
		double n = (double) count;
		Sxx = sumxx-sumx*sumx/n;
		Sxy = sumxy-sumx*sumy/n;
		b = Sxy/Sxx;
		a = (sumy-b*sumx)/n;

		boolean isVertical = false;
		if (geomBounds.width < geomBounds.height) {
			if (b == 0) {
				// force vertical (to avoid divide by zero)
				isVertical = true;

			} else {
				// swap axes
				double bAux = 1/b;
				a = - a / b;
				b = bAux;
			}
		}

		if (isVertical){
			theta = AbstractLinePlacement.HALF_PI;
		} else {
			double p1x = 0;
			double  p1y =geomBounds.height-a;
			double  p2x = geomBounds.width;
			double  p2y = geomBounds.height-
			(a+geomBounds.width*b);

			theta = -Math.atan(((p2y - p1y) / (p2x - p1x)) );
		}

		ArrayList<LabelLocationMetrics> guessed = new ArrayList<LabelLocationMetrics>();
		Rectangle labelBounds = lc.getBounds();
		double cosTheta = Math.cos(theta);
		double sinTheta = Math.sin(theta);
		double halfHeight = labelBounds.getHeight()*0.5;
		double halfWidth= labelBounds.getWidth()*0.5;
		double offsetX =  halfHeight * sinTheta + halfWidth*cosTheta;
		double offsetY = -halfHeight * cosTheta + halfWidth*sinTheta;
		double offsetRX=vp.toMapDistance((int)offsetX);
		double offsetRY=vp.toMapDistance((int)offsetY);
		startingPoint.setLocation(startingPoint.getX() - offsetRX,
				startingPoint.getY() - offsetRY);
		FPoint2D p=(FPoint2D)FConverter.transformToInts(ShapeFactory.createPoint2D(startingPoint.getX(),startingPoint.getY()), vp.getAffineTransform());

		guessed.add(new LabelLocationMetrics(new Point2D.Double(p.getX(),p.getY()), -theta, true));
		return guessed;
	}
	public boolean isSuitableFor(IPlacementConstraints placementConstraints,
			int shapeType) {
		if ((shapeType % FShape.Z) == FShape.POLYGON) {
			return placementConstraints.isParallel();
		}
		return false;
	}

}

