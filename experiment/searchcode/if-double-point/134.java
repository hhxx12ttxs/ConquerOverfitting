<<<<<<< HEAD
// Created by plusminus on 23:51:49 - 24.02.2008
package org.andnav2.nav.util;

import org.andnav2.adt.other.GraphicsPoint;
import org.andnav2.osm.adt.GeoPoint;
import org.andnav2.util.constants.GeoConstants;
import org.andnav2.util.constants.MathematicalConstants;

import android.graphics.Point;
import android.location.Location;
import android.util.FloatMath;



public class Util implements MathematicalConstants, GeoConstants{
	// ===========================================================
	// Final Fields
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public static boolean inBounds(final int lower, final int val, final int upper) {
		return lower <= val && upper >= val;
	}

	/**
	 * Calculates the distance from a line spanned between two MapPoints to another MapPoint.
	 * This is only the geometric distance,
	 * when the search-point is located like:
	 * P or Q, but not like X,Y or Z:
	 * <pre>
	 * ^
	 * |   Y |   P |
	 * |     |     |
	 * |     A-----B
	 * |     |     |  Z
	 * |  X  |  Q  |
	 * |
	 * 0-----------------></pre>
	 * In cases of X and Y, the distance to A would be returned.
	 * In the case of Z, the distance to B would be returned.
	 * @param linePointA First (Map)Point on the line
	 * @param linePointB Second (Map)Point on the line
	 * @param p Point to determine the distance to the line
	 * @return distance from a Point to a line
	 */
	public static float getDistanceToLine(final GeoPoint linePointA, final GeoPoint linePointB, final Point p){
		/* a: Point A on the line. */
		final Point a = new Point(linePointA.getLongitudeE6(), linePointA.getLatitudeE6());
		/* b: Point B on the line. */
		final Point b = new Point(linePointB.getLongitudeE6(), linePointB.getLatitudeE6());
		return getDistanceToLine(a, b, p);
	}

	public static float getDistanceToLine(final Point a, final Point b, final Point p){

		/* If a is the same point as b then return the distance from p to a. */
		if(a.x == b.x && a.y ==b.y){
			final int dx = p.x - a.x;
			final int dy = p.y - a.y;
			return FloatMath.sqrt(dx*dx + dy*dy);
		}

		/* s is the vector from a to b. */
		final Point s = org.andnav2.adt.other.GraphicsPoint.difference(b, a);
		final float lenght_s = FloatMath.sqrt(((long)s.x) * s.x + ((long)s.y) * s.y);

		/* r is the vector from a to p. */
		final Point r = org.andnav2.adt.other.GraphicsPoint.difference(p, a);

		/* The case when the angle at a is 'overstretched' */
		/* Determine the angle between s and r. */
		final double angleAtA = Math.acos(org.andnav2.adt.other.GraphicsPoint.dotProduct(r, s) / (lenght_s * FloatMath.sqrt(((long)r.x) * r.x + ((long)r.y) * r.y)));
		/* If it is bigger than |90°| return distance from p to a*/
		if(Math.abs(angleAtA) > PI_HALF){
			final int dx = p.x - a.x;
			final int dy = p.y - a.y;
			return FloatMath.sqrt(dx*dx + dy*dy);
		}


		/* Attention: s now points to the other direction! */
		s.negate();

		/* t is the vector from b to p. */
		final Point t = GraphicsPoint.difference(p, b);

		/* The case when the angle at b is 'overstretched' */
		/* Determine the angle between s and r. */
		final double angleAtB = Math.acos(GraphicsPoint.dotProduct(s, t) / (lenght_s * FloatMath.sqrt(((long)t.x * t.x) + ((long)t.y) * t.y)));
		/* If it is bigger than |90°| return distance from p to a*/
		if(Math.abs(angleAtB) > PI_HALF){
			final int dx = p.x - b.x;
			final int dy = p.y - b.y;
			return FloatMath.sqrt(dx*dx + dy*dy);
		}

		/* Check if Point is exactly on the line. */
		if(Double.isNaN(angleAtA)){
			// || Double.isNaN(angleAtB) // NOTE: Not needed because angleAtA would also be NaN !
			return 0.0f;
		}


		/* Calculate the geometric distance.
		 * |(p-a) x b| / |b| */
		return Math.abs(GraphicsPoint.crossProduct(GraphicsPoint.difference(p, a), s)) / FloatMath.sqrt(((long)s.x * s.x) + ((long)s.y) * s.y);
	}

	/**
	 * @param linePointA
	 * @param linePointB
	 * @param p
	 * @return the projected MapPoint if possible. <br/>
	 * <ul>
	 * <li><code>null</code> if no projection is possible</li>
	 * <li>when project ON the line was not possible.</li>
	 * <li>when a == b</li>
	 * </ul>.
	 */
	public static GeoPoint getProjectedGeoPoint(final GeoPoint linePointA, final GeoPoint linePointB, final Point p){

		/* a: Point A on the line. */
		final Point a = new Point(linePointA.getLongitudeE6(), linePointA.getLatitudeE6()); /* Longitude is X, Latitude is Y. */
		/* b: Point B on the line. */
		final Point b = new Point(linePointB.getLongitudeE6(), linePointB.getLatitudeE6()); /* Longitude is X, Latitude is Y. */

		/* If a is the same point as b then return null. */
		if(a.x == b.x && a.y ==b.y) {
			return null;
		}

		/* s is the vector from a to b. */
		final Point s = GraphicsPoint.difference(b, a);
		final float lenght_s = FloatMath.sqrt(((long)s.x) * s.x + ((long)s.y) * s.y);

		/* r is the vector from a to p. */
		final Point r = GraphicsPoint.difference(p, a);

		/* The case when the angle at a is 'overstretched' */
		/* Determine the angle between s and r. */
		final double angleAtA = Math.acos(GraphicsPoint.dotProduct(r, s) / (lenght_s * FloatMath.sqrt(((long)r.x) * r.x + ((long)r.y) * r.y)));
		/* If it is bigger than |90°| return null. */
		if(Math.abs(angleAtA) > PI_HALF) {
			return null;
		} else if(Double.isNaN(angleAtA)) {
			return new GeoPoint(p.y,p.x); /* MapPoint is defined as Latitude(Y),Longitude(X). */
		}


		/* Attention: s now points to the other direction! */
		s.negate();

		/* t is the vector from b to p. */
		final Point t = GraphicsPoint.difference(p, b);

		/* The case when the angle at b is 'overstretched' */
		/* Determine the angle between s and r. */
		final float angleAtB = (float)Math.acos(GraphicsPoint.dotProduct(s, t) / (lenght_s * FloatMath.sqrt(((long)t.x) * t.x + ((long)t.y) * t.y)));
		/* If it is bigger than |90°| return b*/
		if(Math.abs(angleAtB) > PI_HALF) {
			return null;
			// NOTE: Not needed because angleAtA would also be NaN !
			//		else if(Double.isNaN(angleAtB))
			//			return new MapPoint(p.x,p.y);
		}

		/* Attention: s now points back to the original direction! */
		s.negate();

		/* Do the actual projection */
		/* First: Calculate the geometric distance.
		 * |(p-a) x b| / |b| */
		final float distance = GraphicsPoint.crossProduct(GraphicsPoint.difference(p, a), s) / FloatMath.sqrt(((long)s.x * s.x) + ((long)s.y) * s.y);

		/* Calculate the gradient of the line from a to be (what already is 's'). */
		final float angleOfOrthogonalRad = (float)Math.atan2(- s.x, s.y);

		/* NOTE: MapPoint is defined as Latitude(Y),Longitude(X). */
		return new GeoPoint(p.y - Math.round(distance * FloatMath.sin(angleOfOrthogonalRad)),
				p.x - Math.round(distance * (float)Math.cos(angleOfOrthogonalRad)));
	}

	public static Point geoPoint2Point(final GeoPoint aGP) {
		return new Point(aGP.getLongitudeE6(), aGP.getLatitudeE6());
	}

	/** Converts an {@link Location} to an {@link Point}. */
	public static Point location2Point(final Location aLocation){
		return new Point((int) (aLocation.getLongitude() * 1E6),
				(int) (aLocation.getLatitude() * 1E6));
	}

	/** Converts an {@link Location} to a {@link GeoPoint}. */
	public static GeoPoint location2GeoPoint(final Location aLocation){
		return new GeoPoint((int) (aLocation.getLatitude() * 1E6),
				(int) (aLocation.getLongitude() * 1E6));
	}

	/**
	 * Calculates the bearing of the two Locations supplied and returns the
	 * Angle in the following (GPS-likely) manner: <br />
	 * <code>N:0°, E:90°, S:180°, W:270°</code>
	 */
	public static float calculateBearing(final GeoPoint before, final GeoPoint after) {
		final Point pBefore = geoPoint2Point(before);
		final Point pAfter = geoPoint2Point(after);

		final float res = -(float) (Math.atan2(pAfter.y - pBefore.y, pAfter.x
				- pBefore.x) * 180 / PI) + 90.0f;

		if (res < 0) {
			return res + 360.0f;
		} else {
			return res;
		}
	}

	/**
	 * Calculates the bearing of the two Locations supplied and returns the
	 * Angle in the following (GPS-likely) manner: <br />
	 * <code>N:0°, E:90°, S:180°, W:270°</code>
	 */
	public static float calculateBearing(final Location before, final Location after) {
		final Point pBefore = location2Point(before);
		final Point pAfter = location2Point(after);

		final float res = -(float) (Math.atan2(pAfter.y - pBefore.y, pAfter.x
				- pBefore.x) * 180 / PI) + 90.0f;

		if (res < 0) {
			return res + 360.0f;
		} else {
			return res;
		}
	}

	/**
	 * Calculates the bearing of the two Locations supplied and returns the
	 * Angle in mathematical manner: <br />
	 * <code>Right: 0° ; Up: 90°; Left: 180°, Down: 270°</code>
	 */
	public static float calculateBearing(final Point coordsA, final Point coordsB) {
		// !!!!!! UNTESTED !!!!!!
		final float res = (float) (Math.atan2(coordsA.y - coordsB.y, coordsA.x - coordsB.x) * 180 / PI);

		if (res < 0) {
			return res + 360.0f;
		} else {
			return res;
		}
	}

	/**
	 * @deprecated Method is probably not correct !!!
	 * @param geoPointA
	 * @param geoPointB
	 * @return
	 */
	@Deprecated
	public static int distanceSquared(final GeoPoint geoPointA, final GeoPoint geoPointB) {
		final int dx = geoPointA.getLongitudeE6() - geoPointB.getLongitudeE6();
		final int dy = geoPointA.getLatitudeE6() - geoPointB.getLatitudeE6();
		return dx*dx + dy*dy;
	}
}
=======
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

package com.spatial4j.core.distance;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;


/**
 * Various distance calculations and constants.
 * Originally from Lucene 3x's old spatial module. It has been modified here.
 */
public class DistanceUtils {

  //pre-compute some angles that are commonly used
  public static final double DEG_45_AS_RADS = Math.PI / 4;
  public static final double SIN_45_AS_RADS = Math.sin(DEG_45_AS_RADS);
  public static final double DEG_90_AS_RADS = Math.PI / 2;
  public static final double DEG_180_AS_RADS = Math.PI;
  public static final double DEG_225_AS_RADS = 5 * DEG_45_AS_RADS;
  public static final double DEG_270_AS_RADS = 3 * DEG_90_AS_RADS;

  public static final double DEGREES_TO_RADIANS =  Math.PI / 180;
  public static final double RADIANS_TO_DEGREES =  1 / DEGREES_TO_RADIANS;

  public static final double KM_TO_MILES = 0.621371192;
  public static final double MILES_TO_KM = 1 / KM_TO_MILES;//1.609

  /**
   * The International Union of Geodesy and Geophysics says the Earth's mean radius in KM is:
   *
   * [1] http://en.wikipedia.org/wiki/Earth_radius
   */
  public static final double EARTH_MEAN_RADIUS_KM = 6371.0087714;
  public static final double EARTH_EQUATORIAL_RADIUS_KM = 6378.1370;

  /** Equivalent to degrees2Dist(1, EARTH_MEAN_RADIUS_KM) */
  public static final double DEG_TO_KM = DEGREES_TO_RADIANS * EARTH_MEAN_RADIUS_KM;
  public static final double KM_TO_DEG = 1 / DEG_TO_KM;

  public static final double EARTH_MEAN_RADIUS_MI = EARTH_MEAN_RADIUS_KM * KM_TO_MILES;
  public static final double EARTH_EQUATORIAL_RADIUS_MI = EARTH_EQUATORIAL_RADIUS_KM * KM_TO_MILES;

  private DistanceUtils() {}

  /**
   * Calculate the p-norm (i.e. length) between two vectors
   *
   * @param vec1  The first vector
   * @param vec2  The second vector
   * @param power The power (2 for cartesian distance, 1 for manhattan, etc.)
   * @return The length.
   *         <p/>
   *         See http://en.wikipedia.org/wiki/Lp_space
   * @see #vectorDistance(double[], double[], double, double)
   */
  public static double vectorDistance(double[] vec1, double[] vec2, double power) {
    return vectorDistance(vec1, vec2, power, 1.0 / power);
  }

  /**
   * Calculate the p-norm (i.e. length) between two vectors
   *
   * @param vec1         The first vector
   * @param vec2         The second vector
   * @param power        The power (2 for cartesian distance, 1 for manhattan, etc.)
   * @param oneOverPower If you've precalculated oneOverPower and cached it, use this method to save one division operation over {@link #vectorDistance(double[], double[], double)}.
   * @return The length.
   */
  public static double vectorDistance(double[] vec1, double[] vec2, double power, double oneOverPower) {
    double result = 0;

    if (power == 0) {
      for (int i = 0; i < vec1.length; i++) {
        result += vec1[i] - vec2[i] == 0 ? 0 : 1;
      }

    } else if (power == 1.0) {
      for (int i = 0; i < vec1.length; i++) {
        result += vec1[i] - vec2[i];
      }
    } else if (power == 2.0) {
      result = Math.sqrt(distSquaredCartesian(vec1, vec2));
    } else if (power == Integer.MAX_VALUE || Double.isInfinite(power)) {//infinite norm?
      for (int i = 0; i < vec1.length; i++) {
        result = Math.max(result, Math.max(vec1[i], vec2[i]));
      }
    } else {
      for (int i = 0; i < vec1.length; i++) {
        result += Math.pow(vec1[i] - vec2[i], power);
      }
      result = Math.pow(result, oneOverPower);
    }
    return result;
  }

  /**
   * Return the coordinates of a vector that is the corner of a box (upper right or lower left), assuming a Rectangular
   * coordinate system.  Note, this does not apply for points on a sphere or ellipse (although it could be used as an approximation).
   *
   * @param center     The center point
   * @param result Holds the result, potentially resizing if needed.
   * @param distance   The d from the center to the corner
   * @param upperRight If true, return the coords for the upper right corner, else return the lower left.
   * @return The point, either the upperLeft or the lower right
   */
  public static double[] vectorBoxCorner(double[] center, double[] result, double distance, boolean upperRight) {
    if (result == null || result.length != center.length) {
      result = new double[center.length];
    }
    if (upperRight == false) {
      distance = -distance;
    }
    //We don't care about the power here,
    // b/c we are always in a rectangular coordinate system, so any norm can be used by
    //using the definition of sine
    distance = SIN_45_AS_RADS * distance; // sin(Pi/4) == (2^0.5)/2 == opp/hyp == opp/distance, solve for opp, similarly for cosine
    for (int i = 0; i < center.length; i++) {
      result[i] = center[i] + distance;
    }
    return result;
  }

  /**
   * Given a start point (startLat, startLon) and a bearing on a sphere, return the destination point.
   *
   * @param startLat The starting point latitude, in radians
   * @param startLon The starting point longitude, in radians
   * @param distanceRAD The distance to travel along the bearing in radians.
   * @param bearingRAD The bearing, in radians.  North is a 0, moving clockwise till radians(360).
   * @param ctx
   * @param reuse A preallocated object to hold the results.
   * @return The destination point, IN RADIANS.
   */
  public static Point pointOnBearingRAD(double startLat, double startLon, double distanceRAD, double bearingRAD, SpatialContext ctx, Point reuse) {
    /*
 	  lat2 = asin(sin(lat1)*cos(d/R) + cos(lat1)*sin(d/R)*cos(θ))
  	lon2 = lon1 + atan2(sin(θ)*sin(d/R)*cos(lat1), cos(d/R)−sin(lat1)*sin(lat2))
     */
    double cosAngDist = Math.cos(distanceRAD);
    double cosStartLat = Math.cos(startLat);
    double sinAngDist = Math.sin(distanceRAD);
    double sinStartLat = Math.sin(startLat);
    double sinLat2 = sinStartLat * cosAngDist +
        cosStartLat * sinAngDist * Math.cos(bearingRAD);
    double lat2 = Math.asin(sinLat2);
    double lon2 = startLon + Math.atan2(Math.sin(bearingRAD) * sinAngDist * cosStartLat,
            cosAngDist - sinStartLat * sinLat2);
    
    // normalize lon first
    if (lon2 > DEG_180_AS_RADS) {
      lon2 = -1.0 * (DEG_180_AS_RADS - (lon2 - DEG_180_AS_RADS));
    } else if (lon2 < -DEG_180_AS_RADS) {
      lon2 = (lon2 + DEG_180_AS_RADS) + DEG_180_AS_RADS;
    }

    // normalize lat - could flip poles
    if (lat2 > DEG_90_AS_RADS) {
      lat2 = DEG_90_AS_RADS - (lat2 - DEG_90_AS_RADS);
      if (lon2 < 0) {
        lon2 = lon2 + DEG_180_AS_RADS;
      } else {
        lon2 = lon2 - DEG_180_AS_RADS;
      }
    } else if (lat2 < -DEG_90_AS_RADS) {
      lat2 = -DEG_90_AS_RADS - (lat2 + DEG_90_AS_RADS);
      if (lon2 < 0) {
        lon2 = lon2 + DEG_180_AS_RADS;
      } else {
        lon2 = lon2 - DEG_180_AS_RADS;
      }
    }

    if (reuse == null) {
      return ctx.makePoint(lon2, lat2);
    } else {
      reuse.reset(lon2, lat2);//x y
      return reuse;
    }
  }

  /**
   * Puts in range -180 <= lon_deg <= +180.
   */
  public static double normLonDEG(double lon_deg) {
    if (lon_deg >= -180 && lon_deg <= 180)
      return lon_deg;//common case, and avoids slight double precision shifting
    double off = (lon_deg + 180) % 360;
    if (off < 0)
      return 180 + off;
    else if (off == 0 && lon_deg > 0)
      return 180;
    else
      return -180 + off;
  }

  /**
   * Puts in range -90 <= lat_deg <= 90.
   */
  public static double normLatDEG(double lat_deg) {
    if (lat_deg >= -90 && lat_deg <= 90)
      return lat_deg;//common case, and avoids slight double precision shifting
    double off = Math.abs((lat_deg + 90) % 360);
    return (off <= 180 ? off : 360-off) - 90;
  }

  /**
   * Calculates the bounding box of a circle, as specified by its center point
   * and distance.  <code>reuse</code> is an optional argument to store the
   * results to avoid object creation.
   */
  public static Rectangle calcBoxByDistFromPtDEG(double lat, double lon, double distDEG, SpatialContext ctx, Rectangle reuse) {
    //See http://janmatuschek.de/LatitudeLongitudeBoundingCoordinates Section 3.1, 3.2 and 3.3
    double minX; double maxX; double minY; double maxY;
    if (distDEG == 0) {
      minX = lon; maxX = lon; minY = lat; maxY = lat;
    } else if (distDEG >= 180) {//distance is >= opposite side of the globe
      minX = -180; maxX = 180; minY = -90; maxY = 90;
    } else {

      //--calc latitude bounds
      maxY = lat + distDEG;
      minY = lat - distDEG;

      if (maxY >= 90 || minY <= -90) {//touches either pole
        //we have special logic for longitude
        minX = -180; maxX = 180;//world wrap: 360 deg
        if (maxY <= 90 && minY >= -90) {//doesn't pass either pole: 180 deg
          minX = normLonDEG(lon - 90);
          maxX = normLonDEG(lon + 90);
        }
        if (maxY > 90)
          maxY = 90;
        if (minY < -90)
          minY = -90;
      } else {
        //--calc longitude bounds
        double lon_delta_deg = calcBoxByDistFromPt_deltaLonDEG(lat, lon, distDEG);

        minX = normLonDEG(lon - lon_delta_deg);
        maxX = normLonDEG(lon + lon_delta_deg);
      }
    }
    if (reuse == null) {
      return ctx.makeRectangle(minX, maxX, minY, maxY);
    } else {
      reuse.reset(minX, maxX, minY, maxY);
      return reuse;
    }
  }

  /**
   * The delta longitude of a point-distance. In other words, half the width of
   * the bounding box of a circle.
   */
  public static double calcBoxByDistFromPt_deltaLonDEG(double lat, double lon, double distDEG) {
    //http://gis.stackexchange.com/questions/19221/find-tangent-point-on-circle-furthest-east-or-west
    if (distDEG == 0)
      return 0;
    double lat_rad = toRadians(lat);
    double dist_rad = toRadians(distDEG);
    double result_rad = Math.asin(Math.sin(dist_rad) / Math.cos(lat_rad));

    if (!Double.isNaN(result_rad))
      return toDegrees(result_rad);
    return 90;
  }

  /**
   * The latitude of the horizontal axis (e.g. left-right line)
   * of a circle.  The horizontal axis of a circle passes through its furthest
   * left-most and right-most edges. On a 2D plane, this result is always
   * <code>from.getY()</code> but, perhaps surprisingly, on a sphere it is going
   * to be slightly different.
   */
  public static double calcBoxByDistFromPt_latHorizAxisDEG(double lat, double lon, double distDEG) {
    //http://gis.stackexchange.com/questions/19221/find-tangent-point-on-circle-furthest-east-or-west
    if (distDEG == 0)
      return lat;
    double lat_rad = toRadians(lat);
    double dist_rad = toRadians(distDEG);
    double result_rad = Math.asin( Math.sin(lat_rad) / Math.cos(dist_rad));
    if (!Double.isNaN(result_rad))
      return toDegrees(result_rad);
    if (lat > 0)
      return 90;
    if (lat < 0)
      return -90;
    return lat;
  }

  /**
   * Calculates the degrees longitude distance at latitude {@code lat} to cover
   * a distance {@code dist}.
   * <p/>
   * Used to calculate a new expanded buffer distance to account for skewing
   * effects for shapes that use the lat-lon space as a 2D plane instead of a
   * sphere.  The expanded buffer will be sure to cover the intended area, but
   * the shape is still skewed and so it will cover a larger area.  For latitude
   * 0 (the equator) the result is the same buffer.  At 60 (or -60) degrees, the
   * result is twice the buffer, meaning that a shape at 60 degrees is twice as
   * high as it is wide when projected onto a lat-lon plane even if in the real
   * world it's equal all around.
   * <p/>
   * If the result added to abs({@code lat}) is >= 90 degrees, then skewing is
   * so severe that the caller should consider tossing the shape and
   * substituting a spherical cap instead.
   *
   * @param lat  latitude in degrees
   * @param dist distance in degrees
   * @return longitudinal degrees (x delta) at input latitude that is >= dist
   *         distance. Will be >= dist and <= 90.
   */
  public static double calcLonDegreesAtLat(double lat, double dist) {
    //This code was pulled out of DistanceUtils.pointOnBearingRAD() and
    // optimized
    // for bearing = 90 degrees, and so we can get an intermediate calculation.
    double distanceRAD = DistanceUtils.toRadians(dist);
    double startLat = DistanceUtils.toRadians(lat);

    double cosAngDist = Math.cos(distanceRAD);
    double cosStartLat = Math.cos(startLat);
    double sinAngDist = Math.sin(distanceRAD);
    double sinStartLat = Math.sin(startLat);

    double lonDelta = Math.atan2(sinAngDist * cosStartLat,
        cosAngDist * (1 - sinStartLat * sinStartLat));

    return DistanceUtils.toDegrees(lonDelta);
  }

  /**
   * The square of the cartesian Distance.  Not really a distance, but useful if all that matters is
   * comparing the result to another one.
   *
   * @param vec1 The first point
   * @param vec2 The second point
   * @return The squared cartesian distance
   */
  public static double distSquaredCartesian(double[] vec1, double[] vec2) {
    double result = 0;
    for (int i = 0; i < vec1.length; i++) {
      double v = vec1[i] - vec2[i];
      result += v * v;
    }
    return result;
  }

  /**
   *
   * @param lat1     The y coordinate of the first point, in radians
   * @param lon1     The x coordinate of the first point, in radians
   * @param lat2     The y coordinate of the second point, in radians
   * @param lon2     The x coordinate of the second point, in radians
   * @return The distance between the two points, as determined by the Haversine formula, in radians.
   */
  public static double distHaversineRAD(double lat1, double lon1, double lat2, double lon2) {
    //TODO investigate slightly different formula using asin() and min() http://www.movable-type.co.uk/scripts/gis-faq-5.1.html

    // Check for same position
    if (lat1 == lat2 && lon1 == lon2)
      return 0.0;
    double hsinX = Math.sin((lon1 - lon2) * 0.5);
    double hsinY = Math.sin((lat1 - lat2) * 0.5);
    double h = hsinY * hsinY +
            (Math.cos(lat1) * Math.cos(lat2) * hsinX * hsinX);
    return 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
  }

  /**
   * Calculates the distance between two lat-lon's using the Law of Cosines. Due to numeric conditioning
   * errors, it is not as accurate as the Haversine formula for small distances.  But with
   * double precision, it isn't that bad -- <a href="http://www.movable-type.co.uk/scripts/latlong.html">
   *   allegedly 1 meter</a>.
   * <p/>
   * See <a href="http://gis.stackexchange.com/questions/4906/why-is-law-of-cosines-more-preferable-than-haversine-when-calculating-distance-b">
   *  Why is law of cosines more preferable than haversine when calculating distance between two latitude-longitude points?</a>
   * <p/>
   * The arguments and return value are in radians.
   */
  public static double distLawOfCosinesRAD(double lat1, double lon1, double lat2, double lon2) {
    //TODO validate formula

    //(MIGRATED FROM org.apache.lucene.spatial.geometry.LatLng.arcDistance()) (Lucene 3x)
    // Imported from mq java client.  Variable references changed to match.

    // Check for same position
    if (lat1 == lat2 && lon1 == lon2)
      return 0.0;

    // Get the m_dLongitude difference. Don't need to worry about
    // crossing 180 since cos(x) = cos(-x)
    double dLon = lon2 - lon1;

    double a = DEG_90_AS_RADS - lat1;
    double c = DEG_90_AS_RADS - lat2;
    double cosB = (Math.cos(a) * Math.cos(c))
        + (Math.sin(a) * Math.sin(c) * Math.cos(dLon));

    // Find angle subtended (with some bounds checking) in radians
    if (cosB < -1.0)
      return Math.PI;
    else if (cosB >= 1.0)
      return 0;
    else
      return Math.acos(cosB);
  }

  /**
   * Calculates the great circle distance using the Vincenty Formula, simplified for a spherical model. This formula
   * is accurate for any pair of points. The equation
   * was taken from <a href="http://en.wikipedia.org/wiki/Great-circle_distance">Wikipedia</a>.
   * <p/>
   * The arguments are in radians, and the result is in radians.
   */
  public static double distVincentyRAD(double lat1, double lon1, double lat2, double lon2) {
    // Check for same position
    if (lat1 == lat2 && lon1 == lon2)
      return 0.0;

    double cosLat1 = Math.cos(lat1);
    double cosLat2 = Math.cos(lat2);
    double sinLat1 = Math.sin(lat1);
    double sinLat2 = Math.sin(lat2);
    double dLon = lon2 - lon1;
    double cosDLon = Math.cos(dLon);
    double sinDLon = Math.sin(dLon);

    double a = cosLat2 * sinDLon;
    double b = cosLat1*sinLat2 - sinLat1*cosLat2*cosDLon;
    double c = sinLat1*sinLat2 + cosLat1*cosLat2*cosDLon;
    
    return Math.atan2(Math.sqrt(a*a+b*b),c);
  }

  /**
   * Converts a distance in the units of the radius to degrees (360 degrees are
   * in a circle). A spherical earth model is assumed.
   */
  public static double dist2Degrees(double dist, double radius) {
    return toDegrees(dist2Radians(dist, radius));
  }

  /**
   * Converts <code>degrees</code> (1/360th of circumference of a circle) into a
   * distance as measured by the units of the radius.  A spherical earth model
   * is assumed.
   */
  public static double degrees2Dist(double degrees, double radius) {
    return radians2Dist(toRadians(degrees), radius);
  }

  /**
   * Converts a distance in the units of <code>radius</code> (e.g. kilometers)
   * to radians (multiples of the radius). A spherical earth model is assumed.
   */
  public static double dist2Radians(double dist, double radius) {
    return dist / radius;
  }

  /**
   * Converts <code>radians</code> (multiples of the <code>radius</code>) to
   * distance in the units of the radius (e.g. kilometers).
   */
  public static double radians2Dist(double radians, double radius) {
    return radians * radius;
  }

  /**
   * Same as {@link Math#toRadians(double)} but 3x faster (multiply vs. divide).
   * See CompareRadiansSnippet.java in tests.
   */
  public static double toRadians(double degrees) {
    return degrees * DEGREES_TO_RADIANS;
  }

  /**
   * Same as {@link Math#toDegrees(double)} but 3x faster (multiply vs. divide).
   * See CompareRadiansSnippet.java in tests.
   */
  public static double toDegrees(double radians) {
    return radians * RADIANS_TO_DEGREES;
  }

}
>>>>>>> 76aa07461566a5976980e6696204781271955163

