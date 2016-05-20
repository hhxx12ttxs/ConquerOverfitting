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
 * $Id: RobustLengthIndexedLine.java 14154 2007-09-27 19:14:16Z azabala $
 * $Log: RobustLengthIndexedLine.java,v $
 * Revision 1.1  2007/09/09 11:44:09  azabala
 * Improvements to JTS (robustness problems)
 *
 *
 */
package org.gvsig.jts;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.linearref.LinearIterator;

/**
 * Implementation of LengthIndexedLine to avoid robustness problems.
 * 
 * TODO If JTS official implementation of LenghtIndexedLine solve the problem
 * that has been already reported about precission robustness, DELETE THIS CLASS
 * 
 * @author azabala
 * 
 */
public class RobustLengthIndexedLine extends LengthIndexedLine {

	/**
	 * Superclass has no methods to expose linearGeom, and is private, so we
	 * need to mantain a reference here
	 */
	private Geometry linearGeom;

	public RobustLengthIndexedLine(Geometry arg0) {
		super(arg0);
		this.linearGeom = arg0;
		
	}

	public double indexOf(Coordinate pt) {
		return indexOfFromStart(pt, -1);
	}

	public double indexOfAfter(Coordinate pt, double minIndex) {
		if (minIndex < 0.0)
			return indexOf(pt);
		double endIndex = linearGeom.getLength();
		if (endIndex < minIndex)
			return endIndex;
		double closestAfter = indexOfFromStart(pt, minIndex);
		return closestAfter;
	}

	/**
	 * Return the first index ocurrence of the given coordinate grater than
	 * minIndex.
	 */

	private double indexOfFromStart(Coordinate inputPt, double minIndex) {
		double minDistance = Double.MAX_VALUE;
		double ptMeasure = minIndex;
		double segmentStartMeasure = 0.0;
		LineSegment seg = new LineSegment();
		LinearIterator it = new LinearIterator(linearGeom);
		while (it.hasNext()) {
			if (!it.isEndOfLine()) {
				seg.p0 = it.getSegmentStart();
				seg.p1 = it.getSegmentEnd();
				double segDistance = seg.distance(inputPt);
/*
 * these two lines of code are the only novelties respect to LenghtIndexedLine
 */
				PrecisionModel pm = linearGeom.getPrecisionModel();
				segDistance = pm.makePrecise(segDistance);
/*
 * FIXME Remove them if JTS solve the problem
 */
				double segMeasureToPt = segmentNearestMeasure(seg, inputPt,
						segmentStartMeasure);

				if (segDistance < minDistance && segMeasureToPt > minIndex) {
					ptMeasure = segMeasureToPt;
					minDistance = segDistance;
				}
				segmentStartMeasure += seg.getLength();
			}
			it.next();
		}
		return ptMeasure;
	}

	private double segmentNearestMeasure(LineSegment seg, Coordinate inputPt,
			double segmentStartMeasure) {
		// found new minimum, so compute location distance of point
		double projFactor = seg.projectionFactor(inputPt);
		if (projFactor <= 0.0)
			return segmentStartMeasure;
		if (projFactor <= 1.0)
			return segmentStartMeasure + projFactor * seg.getLength();
		// projFactor > 1.0
		return segmentStartMeasure + seg.getLength();
	}

}

