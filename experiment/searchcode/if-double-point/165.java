<<<<<<< HEAD
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spatial4j.core.shape.impl;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.SpatialRelation;

import static com.spatial4j.core.shape.SpatialRelation.*;

/**
 * A line between two points with a buffer distance extending in every
 * direction. By contrast, an un-buffered line covers no area and as such
 * is extremely unlikely to intersect with a point.
 */
public class BufferedLine implements Shape {

  private final Point pA, pB;
  private final double buf;
  private final Rectangle bbox;
  /**
   * the primary line; passes through pA & pB
   */
  private final InfBufLine linePrimary;
  /**
   * perpendicular to the primary line, centered between pA & pB
   */
  private final InfBufLine linePerp;

  /**
   * Creates a buffered line from pA to pB. The buffer extends on both sides of
   * the line, making the width 2x the buffer. The buffer extends out from
   * pA & pB, making the line in effect 2x the buffer longer than pA to pB.
   *
   * @param pA  start point
   * @param pB  end point
   * @param buf the buffer distance in degrees
   * @param ctx
   */
  public BufferedLine(Point pA, Point pB, double buf, SpatialContext ctx) {
    assert buf >= 0;//TODO support buf=0 via another class ?

    /**
     * If true, buf should bump-out from the pA & pB, in effect
     *                  extending the line a little.
     */
    final boolean bufExtend = true;//TODO support false and make this a
    // parameter

    this.pA = pA;
    this.pB = pB;
    this.buf = buf;

    double deltaY = pB.getY() - pA.getY();
    double deltaX = pB.getX() - pA.getX();

    PointImpl center = new PointImpl(pA.getX() + deltaX / 2,
        pA.getY() + deltaY / 2, null);

    double perpExtent = bufExtend ? buf : 0;

    if (deltaX == 0 && deltaY == 0) {
      linePrimary = new InfBufLine(0, center, buf);
      linePerp = new InfBufLine(Double.POSITIVE_INFINITY, center, buf);
    } else {
      linePrimary = new InfBufLine(deltaY / deltaX, center, buf);
      double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
      linePerp = new InfBufLine(-deltaX / deltaY, center,
          length / 2 + perpExtent);
    }

    double minY, maxY;
    double minX, maxX;
    if (deltaX == 0) { // vertical
      if (pA.getY() <= pB.getY()) {
        minY = pA.getY();
        maxY = pB.getY();
      } else {
        minY = pB.getY();
        maxY = pA.getY();
      }
      minX = pA.getX() - buf;
      maxX = pA.getX() + buf;
      minY = minY - perpExtent;
      maxY = maxY + perpExtent;

    } else {
      if (!bufExtend) {
        throw new UnsupportedOperationException("TODO");
        //solve for B & A (C=buf), one is buf-x, other is buf-y.
      }

      //Given a right triangle of A, B, C sides, C (hypotenuse) ==
      // buf, and A + B == the bounding box offset from pA & pB in x & y.
      double bboxBuf = buf * (1 + Math.abs(linePrimary.getSlope()))
          * linePrimary.getDistDenomInv();
      assert bboxBuf >= buf && bboxBuf <= buf * 1.5;

      if (pA.getX() <= pB.getX()) {
        minX = pA.getX() - bboxBuf;
        maxX = pB.getX() + bboxBuf;
      } else {
        minX = pB.getX() - bboxBuf;
        maxX = pA.getX() + bboxBuf;
      }
      if (pA.getY() <= pB.getY()) {
        minY = pA.getY() - bboxBuf;
        maxY = pB.getY() + bboxBuf;
      } else {
        minY = pB.getY() - bboxBuf;
        maxY = pA.getY() + bboxBuf;
      }

    }
    Rectangle bounds = ctx.getWorldBounds();

    bbox = ctx.makeRectangle(
        Math.max(bounds.getMinX(), minX),
        Math.min(bounds.getMaxX(), maxX),
        Math.max(bounds.getMinY(), minY),
        Math.min(bounds.getMaxY(), maxY));
  }

  /**
   * Calls {@link DistanceUtils#calcLonDegreesAtLat(double,
   * double)} given pA or pB's latitude; whichever is farthest. It's useful to
   * expand a buffer of a line segment when used in a geospatial context to
   * cover the desired area.
   */
  public static double expandBufForLongitudeSkew(Point pA, Point pB,
                                                 double buf) {
    double absA = Math.abs(pA.getY());
    double absB = Math.abs(pB.getY());
    double maxLat = Math.max(absA, absB);
    double newBuf = DistanceUtils.calcLonDegreesAtLat(maxLat, buf);
//    if (newBuf + maxLat >= 90) {
//      //TODO substitute spherical cap ?
//    }
    assert newBuf >= buf;
    return newBuf;
  }

  @Override
  public SpatialRelation relate(Shape other) {
    if (other instanceof Point)
      return contains((Point) other) ? CONTAINS : DISJOINT;
    if (other instanceof Rectangle)
      return relate((Rectangle) other);
    throw new UnsupportedOperationException();
  }

  public SpatialRelation relate(Rectangle r) {
    //Check BBox for disjoint & within.
    SpatialRelation bboxR = bbox.relate(r);
    if (bboxR == DISJOINT || bboxR == WITHIN)
      return bboxR;
    //Either CONTAINS, INTERSECTS, or DISJOINT

    Point scratch = new PointImpl(0, 0, null);
    Point prC = r.getCenter();
    SpatialRelation result = linePrimary.relate(r, prC, scratch);
    if (result == DISJOINT)
      return DISJOINT;
    SpatialRelation resultOpp = linePerp.relate(r, prC, scratch);
    if (resultOpp == DISJOINT)
      return DISJOINT;
    if (result == resultOpp)//either CONTAINS or INTERSECTS
      return result;
    return INTERSECTS;
  }

  public boolean contains(Point p) {
    //TODO check bbox 1st?
    return linePrimary.contains(p) && linePerp.contains(p);
  }

  public Rectangle getBoundingBox() {
    return bbox;
  }

  @Override
  public boolean hasArea() {
    return buf > 0;
  }

  @Override
  public double getArea(SpatialContext ctx) {
    return linePrimary.getBuf() * linePerp.getBuf() * 4;
  }

  @Override
  public Point getCenter() {
    return getBoundingBox().getCenter();
  }

  public Point getA() {
    return pA;
  }

  public Point getB() {
    return pB;
  }

  public double getBuf() {
    return buf;
  }

  /**
   * INTERNAL
   */
  public InfBufLine getLinePrimary() {
    return linePrimary;
  }

  /**
   * INTERNAL
   */
  public InfBufLine getLinePerp() {
    return linePerp;
  }

  @Override
  public String toString() {
    return "BufferedLine(" + pA + ", " + pB + " b=" + buf + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BufferedLine that = (BufferedLine) o;

    if (Double.compare(that.buf, buf) != 0) return false;
    if (!pA.equals(that.pA)) return false;
    if (!pB.equals(that.pB)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = pA.hashCode();
    result = 31 * result + pB.hashCode();
    temp = buf != +0.0d ? Double.doubleToLongBits(buf) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }
}

=======
package algos;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jens Staahl
 */

public class closestpair1 {

	// some local config
	static boolean test = false;
	static String testDataFile = "testdata.txt";
	private static String ENDL = "\n";

	class Point {
		double x, y;

		public Point(double x, double y) {
			super();
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return x + " " + y;
		}

		double dot(Point other) {
			return x * other.x + y * other.y;
		}

		Point sub(Point other) {
			return new Point(x - other.x, y - other.y);
		}

		Point add(Point other) {
			return new Point(x + other.x, y + other.y);
		}

		double norm() {
			return Math.sqrt(x * x + y * y);
		}

		double cross(Point other) {
			return x * other.y - other.x * y;
		}

		double dist(Point other) {
			double xdiff = x - other.x;
			double ydiff = y - other.y;
			return Math.sqrt(xdiff * xdiff + ydiff * ydiff);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			long temp;
			temp = Double.doubleToLongBits(x);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(y);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Point other = (Point) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
				return false;
			if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
				return false;
			return true;
		}

		private closestpair1 getOuterType() {
			return closestpair1.this;
		}

	}

	// Just solves the acutal kattis-problem
	ZKattio io;

	private void solve() throws Throwable {
		io = new ZKattio(stream);
		while (true) {
			int n = io.getInt();
			if(n == 0){
				break;
			}
			int numBuckets = (int) (Math.sqrt(n)/2)+1;
			List[][] buckets = new List[numBuckets][numBuckets];
			for (int i = 0; i < buckets.length; i++) {
				for (int j = 0; j < buckets[i].length; j++) {
					buckets[i][j] = new ArrayList();
				}
			}
			for (int i = 0; i < n; i++) {
				Point p = new Point(io.getDouble(), io.getDouble());
				int xbuck = (int) ((p.x + 100000)/(200000)*numBuckets);
				int ybuck = (int) ((p.y + 100000)/(200000)*numBuckets);
				buckets[ybuck][xbuck].add(p);
			}
			double dmin = Integer.MAX_VALUE;
			Point a=null, b=null;
			for (int ybuck = 0; ybuck < buckets.length; ybuck++) {
				for (int xbuck = 0; xbuck < buckets.length; xbuck++) {
					for (int i = 0; i < buckets[ybuck][xbuck].size(); i++) {
						Point p = (Point) buckets[ybuck][xbuck].get(i);
						for (int xmod = -1; xmod <= 1; xmod++) {
							for (int ymod = -1; ymod <= 1; ymod++) {
								int newx = xbuck + xmod;
								int newy = ybuck + ymod;
								if(newx >= 0 && newx < numBuckets && newy >= 0 && newy < numBuckets){
									for (int other = 0; other < buckets[newy][newx].size(); other++) {
										if(newy == ybuck && newx == xbuck && other == i){
											continue;
										}
										Point p2 = (Point) buckets[newy][newx].get(other);
										if(p.dist(p2) < dmin){
											dmin = p.dist(p2);
											a = p;
											b = p2;
										}
									}
								}
							}
						}
					}
				}
			}
			out.write(a + " " + b + "\n");
		}
		out.flush();
	}

	public static void main(String[] args) throws Throwable {
		new closestpair1().solve();
	}

	public closestpair1() throws Throwable {
		if (test) {
			stream = new FileInputStream(testDataFile);
		}
	}

	InputStream stream = System.in;
	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));// outStream = System.out;

}
>>>>>>> 76aa07461566a5976980e6696204781271955163
