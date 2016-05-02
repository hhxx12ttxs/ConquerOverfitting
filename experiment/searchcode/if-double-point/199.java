<<<<<<< HEAD
package utils;

import shapes.shapes.Point;

/**
 * Created by IntelliJ IDEA.
 * User: ZOR
 * Date: 03.12.11
 * Time: 17:33
 */
public class DelaunayUtils {

    public static boolean InCircle(Point p, Point p1, Point p2, Point p3) {

        //Return TRUE if the point (xp,yp) lies inside the circumcircle
        //made up by points (x1,y1) (x2,y2) (x3,y3)
        //NOTE: A point on the edge is inside the circumcircle

        double Epsilon = 0.01f;

        double p2y = p2.y;
        double p1y = p1.y;
        double p3y = p3.y;
        if (Math.abs(p1y - p2y) < Epsilon && Math.abs(p2y - p3y) < Epsilon) {
            //INCIRCUM - F - Points are coincident !!
            return false;
        }


        double m1;
        double m2;
        double mx1;
        double mx2;
        double my1;
        double my2;
        double xc;
        double yc;

        double p3x = p3.x;
        double p2x = p2.x;
        double p1x = p1.x;
        if (Math.abs(p2y - p1y) < Epsilon) {
            m2 = -(p3x - p2x) / (p3y - p2y);
            mx2 = (p2x + p3x) * 0.5;
            my2 = (p2y + p3y) * 0.5;
            //Calculate CircumCircle center (xc,yc)
            xc = (p2x + p1x) * 0.5;
            yc = m2 * (xc - mx2) + my2;
        } else if (Math.abs(p3y - p2y) < Epsilon) {
            m1 = -(p2x - p1x) / (p2y - p1y);
            mx1 = (p1x + p2x) * 0.5;
            my1 = (p1y + p2y) * 0.5;
            //Calculate CircumCircle center (xc,yc)
            xc = (p3x + p2x) * 0.5;
            yc = m1 * (xc - mx1) + my1;
        } else {
            m1 = -(p2x - p1x) / (p2y - p1y);
            m2 = -(p3x - p2x) / (p3y - p2y);
            mx1 = (p1x + p2x) * 0.5;
            mx2 = (p2x + p3x) * 0.5;
            my1 = (p1y + p2y) * 0.5;
            my2 = (p2y + p3y) * 0.5;
            //Calculate CircumCircle center (xc,yc)
            xc = (m1 * mx1 - m2 * mx2 + my2 - my1) / (m1 - m2);
            yc = m1 * (xc - mx1) + my1;
        }

        double dx = p2x - xc;
        double dy = p2y - yc;
        double rsqr = dx * dx + dy * dy;
        //double r = Math.Sqrt(rsqr); //Circumcircle radius
        dx = p.x - xc;
        dy = p.y - yc;
        double drsqr = dx * dx + dy * dy;

        return (drsqr <= rsqr);


=======
/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.beam.smos.visat.export;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

class GeometryTracker {
    private Rectangle2D.Double area;
    private Point2D.Double firstPoint;

    GeometryTracker() {
        area = null;
        firstPoint = null;
    }


    Rectangle2D getArea() {
        if (area == null) {
            return new Rectangle2D.Double();
        }
        return area.getBounds2D();
    }

    boolean hasValidArea() {
        return area != null && !area.isEmpty();
    }

    void add(Point2D.Double point) {
        if (area == null && firstPoint != null) {
            final double minX = Math.min(firstPoint.getX(), point.getX());
            final double maxX = Math.max(firstPoint.getX(), point.getX());
            final double minY = Math.min(firstPoint.getY(), point.getY());
            final double maxY = Math.max(firstPoint.getY(), point.getY());
            area = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
        } else if (firstPoint == null) {
            firstPoint = point;
        } else {
            area.add(point);
        }
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }
}

