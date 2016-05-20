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
package com.iver.cit.gvsig.fmap.core;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;


/**
 * Spline2D.
 *
 * @author Vicente Caballero Navarro
 */
public class FSpline2D extends FPolyline2D {

	protected Point2D[] points;
	/**
	 * Crea un nuevo FPolyline2D.
	 *
	 * @param gpx GeneralPathX.
	 */
	public FSpline2D(Point2D[] ps) {
		super(getGeneralPathX(ps));
		points=ps;
	}

	private static GeneralPathX getGeneralPathX(Point2D[] ps) {
		GeneralPathX gpx=new GeneralPathX();
		int num=ps.length;
		double[] px=new double[num];
    	double[] py=new double[num];
    	for (int i=0;i<num;i++) {
    		Point2D p=ps[i];
    		px[i]=p.getX();
    		py[i]=p.getY();

    	}
    	Spline splineX = new Spline(px);
        Spline splineY = new Spline(py);
        gpx.moveTo(px[0],py[0]);
        for (int i = 0; i < px.length - 1; i++) {
            for (int t = 1; t < 31; t++) {
                double x1 = splineX.fn(i, ((double) t) / 30.0);
                double y1 = splineY.fn(i, ((double) t) / 30.0);
                gpx.lineTo(x1,y1);
            }
        }
        if (ps[0].getX()==ps[ps.length-1].getX() && ps[0].getY()==ps[ps.length-1].getY())
        	gpx.closePath();
		return gpx;
	}


	/**
	 * @see com.iver.cit.gvsig.fmap.core.FShape#getShapeType()
	 */
	public int getShapeType() {
		return FShape.LINE;
	}

	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.core.FShape#cloneFShape()
	 */
	public FShape cloneFShape() {
		return new FSpline2D(points.clone());
	}

	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.core.FShape#getStretchingHandlers()
	 */
	public Handler[] getStretchingHandlers() {
		ArrayList handlers = new ArrayList();
		for (int i=0;i<points.length;i++) {
			handlers.add(new PointHandler(i, points[i].getX(), points[i].getY()));
		}
		return (Handler[]) handlers.toArray(new Handler[0]);
	}
	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.core.FShape#getSelectHandlers()
	 */
	public Handler[] getSelectHandlers() {
		ArrayList handlers = new ArrayList();
		for (int i=0;i<points.length;i++) {
			Point2D p=points[i];
			handlers.add(new PointSelHandler(i, p.getX(), p.getY()));
		}
		return (Handler[]) handlers.toArray(new Handler[0]);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @author Vicente Caballero Navarro
	 */
	class PointHandler extends AbstractHandler implements IFinalHandler{
		/**
		 * Crea un nuevo PointHandler.
		 *
		 * @param x DOCUMENT ME!
		 * @param y DOCUMENT ME!
		 */
		public PointHandler(int i,double x, double y) {
			point = new Point2D.Double(x,y);
			index=i;
		}

		/**
		 * DOCUMENT ME!
		 *
		 * @param x DOCUMENT ME!
		 * @param y DOCUMENT ME!
		 *
		 * @return DOCUMENT ME!
		 */
		public void move(double x, double y) {
			point.setLocation(point.getX()+x,point.getY()+y);
			//TODO falta actualizar el GeneralPathX
		}

		/**
		 * @see com.iver.cit.gvsig.fmap.core.Handler#set(double, double)
		 */
		public void set(double x, double y) {
			point.setLocation(x,y);
			//TODO falta actualizar el GeneralPathX
		}
	}
	/**
	 * DOCUMENT ME!
	 *
	 * @author Vicente Caballero Navarro
	 */
	class PointSelHandler extends AbstractHandler implements IFinalHandler{
		/**
		 * Crea un nuevo PointHandler.
		 *
		 * @param x DOCUMENT ME!
		 * @param y DOCUMENT ME!
		 */
		public PointSelHandler(int i,double x, double y) {
			point = new Point2D.Double(x,y);
			index=i;
		}

		/**
		 * DOCUMENT ME!
		 *
		 * @param x DOCUMENT ME!
		 * @param y DOCUMENT ME!
		 *
		 * @return DOCUMENT ME!
		 */
		public void move(double x, double y) {
			point.setLocation(point.getX()+x,point.getY()+y);
			points[index]=point;
			gp=getGeneralPathX(points);
		}

		/**
		 * @see com.iver.cit.gvsig.fmap.core.Handler#set(double, double)
		 */
		public void set(double x, double y) {
			point.setLocation(x,y);
			points[index]=point;
			gp=getGeneralPathX(points);
		}
	}
	 static class Spline {
	    	private double y[];
	    	private double y2[];

	    	/**
	    	 * The constructor calculates the second derivatives of the interpolating function
	    	 * at the tabulated points xi, with xi = (i, y[i]).
	    	 * Based on numerical recipes in C, http://www.library.cornell.edu/nr/bookcpdf/c3-3.pdf .
	    	 * @param y Array of y coordinates for cubic-spline interpolation.
	    	 */
	    	public Spline(double y[]) {
	    		this.y = y;
	    		int n = y.length;
	    		y2 = new double[n];
	    		double u[] = new double[n];
	    		for (int i = 1; i < n - 1; i++) {
	    			y2[i] = -1.0 / (4.0 + y2[i - 1]);
	    			u[i] = (6.0 * (y[i + 1] - 2.0 * y[i] + y[i - 1]) - u[i - 1]) / (4.0 + y2[i - 1]);
	    		}
	    		for (int i = n - 2; i >= 0; i--) {
	    			y2[i] = y2[i] * y2[i + 1] + u[i];
	    		}
	    	}

	    	/**
	    	 * Returns a cubic-spline interpolated value y for the point between
	    	 * point (n, y[n]) and (n+1, y[n+1), with t ranging from 0 for (n, y[n])
	    	 * to 1 for (n+1, y[n+1]).
	    	 * @param n The start point.
	    	 * @param t The distance to the next point (0..1).
	    	 * @return A cubic-spline interpolated value.
	    	 */
	    	public double fn(int n, double t) {
	    		return t * y[n + 1] - ((t - 1.0) * t * ((t - 2.0) * y2[n] - (t + 1.0) * y2[n + 1])) / 6.0 + y[n] - t * y[n];
	    	}

	    }
	 public void transform(AffineTransform at) {
			for (int i=0;i<points.length;i++) {
				Point2D p=points[i];
				at.transform(p, p);
			}
		}
}

