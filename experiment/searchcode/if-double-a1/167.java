/**
 * Class from OSMandroidConverter
 * http://code.google.com/p/osm-android/source/browse/trunk/OSMandroidConverter/src/com/google/code/osmandroidconverter/main/Polygon.java
 */

package se.geoproject.atlas.map.data.osm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.geoproject.atlas.map.triangulation.Triangle;
import se.geoproject.atlas.map.triangulation.Triangulate;
import se.geoproject.atlas.map.triangulation.Point;

public class OsmPolygon {

	ArrayList<OsmPoint> points = new ArrayList<OsmPoint>();

	public OsmPolygon(OsmPoint[] pts) {

		if (polygonClockwise(pts)) {

			for (int i = 0; i < pts.length; i++) {
				this.points.add(pts[i]);
			}
		} else {

			for (int i = pts.length - 1; i >= 0; i--) {
				this.points.add(pts[i]);
			}

		}
	}

	public ArrayList<OsmPoint> triangulate() {
		
		List<Point> triPoints = new ArrayList<Point> (points.size());
		
		for(OsmPoint op: points) {
			triPoints.add(new Point(op.x, op.y));
		}
		
		try {
			Triangulate tri = new Triangulate(triPoints);
			tri.triangulate();
			
			ArrayList<OsmPoint> osmPoints = new ArrayList<OsmPoint> ();
			
			for(Triangle t: tri.getTriangles()) {
				osmPoints.add(new OsmPoint(t.GetPoint(0).x, t.GetPoint(0).y));
				osmPoints.add(new OsmPoint(t.GetPoint(1).x, t.GetPoint(1).y));
				osmPoints.add(new OsmPoint(t.GetPoint(2).x, t.GetPoint(2).y));
			}
			
			if(!osmPoints.isEmpty()) {
				return osmPoints;
			}
		} catch (Exception e) {
			
		}
		
		// If the new triangulation didn't work, try the old one

		ArrayList<OsmPoint> triangles = new ArrayList<OsmPoint>();
		boolean finish = false;

		if (this.points.size() == 3) {
			triangles.addAll(this.points);
			finish = true;
		}

		while (!finish) {

			int pointIndex = 0;
			boolean earFound = false;

			// loop through all the vertices and try to find an ear
			this.classifyPoints();
			while (pointIndex < this.points.size()) {

				if (isEar(this.points.get(pointIndex), pointIndex)) {

					earFound = true;
					break;
				}

				pointIndex++;
			}

			if (!earFound) {
				throw new RuntimeException("Cannot triangulate polygon");
			}

			OsmPoint p0 = this.getPreviousPoint(pointIndex);
			OsmPoint p1 = this.points.get(pointIndex);
			OsmPoint p2 = this.getNextPoint(pointIndex);

			triangles.add(p0);
			triangles.add(p1);
			triangles.add(p2);

			this.points.remove(pointIndex);

			if (this.points.size() == 3) {

				triangles.add(this.points.get(0));
				triangles.add(this.points.get(1));
				triangles.add(this.points.get(2));

				finish = true;
			}
		}

		return triangles;
	}

	private boolean isEar(OsmPoint point, int index) {

		if (point.type == OsmPoint.POINT_CONCAVE) {
			return false;
		}

		OsmPoint p0 = this.getPreviousPoint(index);
		OsmPoint p1 = this.points.get(index);
		OsmPoint p2 = this.getNextPoint(index);

		/*
		 * if the above triangle does not contain any concave point, we have a
		 * valid ear
		 */
		int numPoints = this.points.size();
		for (int i = 0; i < numPoints; i++) {

			OsmPoint currPoint = this.points.get(i);

			if (currPoint.equals(p0) || currPoint.equals(p1)
					|| currPoint.equals(p2)) {
				continue;
			}

			if (currPoint.type == OsmPoint.POINT_CONCAVE) {

				/* if triangle contains point, return false */
				if (this.sameSide(currPoint, p0, p1, p2)
						&& this.sameSide(currPoint, p1, p0, p2)
						&& this.sameSide(currPoint, p2, p0, p1)) {

					return false;
				}

			}
		}

		return true;
	}

	public boolean sameSide(OsmPoint p1, OsmPoint p2, OsmPoint a, OsmPoint b) {

		double dotProduct = 0.0;
		double crossProduct1 = 0.0;
		double crossProduct2 = 0.0;
		double a1, a2, b1, b2;

		a1 = (b.x - a.x);
		a2 = (b.y - a.y);
		b1 = (p1.x - a.x);
		b2 = (p1.y - a.y);
		crossProduct1 = a1 * b2 - a2 * b1;

		a1 = (b.x - a.x);
		a2 = (b.y - a.y);
		b1 = (p2.x - a.x);
		b2 = (p2.y - a.y);
		crossProduct2 = a1 * b2 - a2 * b1;

		dotProduct = crossProduct1 * crossProduct2;

		if (dotProduct >= 0) {
			return true;
		} else {
			return false;
		}
	}

	public void classifyPoints() {

		int numPoints = this.points.size();
		for (int i = 0; i < numPoints; i++) {

			OsmPoint p1 = this.getPreviousPoint(i);
			OsmPoint p2 = this.points.get(i);
			OsmPoint p3 = this.getNextPoint(i);

			// compute the area of the triangle and determine the vertex type
			if (!convex(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)) {

				this.points.get(i).type = OsmPoint.POINT_CONVEX;
			} else {
				this.points.get(i).type = OsmPoint.POINT_CONCAVE;
			}

			/*
			 * double area = 0.0; area += (p0.x * p1.y - p1.x * p0.y); area +=
			 * (p1.x * p2.y - p2.x * p1.y); area += (p2.x * p1.y - p1.x * p2.y);
			 * area /= 2;
			 * 
			 * if (area <= 0 ) {
			 * 
			 * this.points.get(i).type = OsmPoint.POINT_CONVEX; } else {
			 * 
			 * this.points.get(i).type = OsmPoint.POINT_CONCAVE; }
			 */
		}
	}

	/*
	 * convex: returns true if point (x2, y2) is convex
	 */
	private boolean convex(double x1, double y1, double x2, double y2,
			double x3, double y3) {

		if (triangleArea(x1, y1, x2, y2, x3, y3) < 0)
			return true;
		else
			return false;
	}

	/*
	 * area: determines area of triangle formed by three points
	 */
	private double triangleArea(double x1, double y1, double x2, double y2,
			double x3, double y3) {

		double areaSum = 0;

		areaSum += x1 * (y3 - y2);
		areaSum += x2 * (y1 - y3);
		areaSum += x3 * (y2 - y1);

		return areaSum / 2;
	}

	private boolean polygonClockwise(OsmPoint[] pts) {

		int numPoints = pts.length;

		if (numPoints < 3)
			throw new RuntimeException("Less than three points");

		OsmPoint minPoint = pts[0];
		int minIndex = 0;

		for (int i = 1; i < numPoints; i++) {

			OsmPoint curr = pts[i];

			if (curr.x > minPoint.x) {

				minPoint = curr;
				minIndex = i;
			} else if (curr.x == minPoint.x && curr.y < minPoint.y) {

				minPoint = curr;
				minIndex = i;
			}
		}

		OsmPoint p1 = null, p2 = null, p3 = null;

		if (minIndex == 0) {
			p1 = pts[numPoints - 1];
			p2 = pts[0];
			p3 = pts[1];
		} else if (minIndex == numPoints - 1) {
			p1 = pts[numPoints - 2];
			p2 = pts[numPoints - 1];
			p3 = pts[0];

		} else if (minIndex > 0 && minIndex < numPoints - 1) {
			p1 = pts[minIndex - 1];
			p2 = pts[minIndex];
			p3 = pts[minIndex + 1];
		}

		double crossProduct = (p2.x - p1.x) * (p3.y - p2.y);
		crossProduct = crossProduct - ((p2.y - p1.y) * (p3.x - p2.x));

		if (crossProduct < 0)
			return true;
		else
			return false;
	}

	private OsmPoint getNextPoint(int index) {

		if (index != this.points.size() - 1) {

			return this.points.get(index + 1);
		} else {
			return this.points.get(0);
		}
	}

	private OsmPoint getPreviousPoint(int index) {

		if (index != 0) {

			return this.points.get(index - 1);
		} else {
			return this.points.get(this.points.size() - 1);
		}

	}

	public OsmPoint findCentroid() {

		double cx = 0.0, cy = 0.0, f = 0;
		Iterator<OsmPoint> it = this.points.iterator();

		while (it.hasNext()) {

			OsmPoint p1 = it.next();
			if (!it.hasNext()) {
				break;
			}

			OsmPoint p2 = it.next();

			f = p1.x * p2.y - p2.x * p1.y;
			cx += (p1.x + p2.y) * f;
			cy += (p1.y + p2.y) * f;
		}

		cx = 1 / (6 * Math.abs(signedArea())) * cx;
		cy = 1 / (6 * Math.abs(signedArea())) * cy;
		return new OsmPoint(cx, cy);
	}

	private double signedArea() {

		double sum = 0.0;
		Iterator<OsmPoint> it = this.points.iterator();

		while (it.hasNext()) {

			OsmPoint p1 = it.next();
			if (!it.hasNext()) {
				break;
			}

			OsmPoint p2 = it.next();

			sum += (p1.x * p2.y - p2.x * p1.y);
		}

		return 0.5 * sum;
	}
}
