/*
<<<<<<< HEAD
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.spatial4j.core.distance;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.SpatialRelation;
import com.spatial4j.core.shape.impl.PointImpl;
import org.junit.Before;
import org.junit.Test;

import static com.spatial4j.core.distance.DistanceUtils.DEG_TO_KM;
import static com.spatial4j.core.distance.DistanceUtils.KM_TO_DEG;

public class TestDistances extends RandomizedTest {

  //NOTE!  These are sometimes modified by tests.
  private SpatialContext ctx;
  private double EPS;

  @Before
  public void beforeTest() {
    ctx = SpatialContext.GEO;
    EPS = 10e-4;//delta when doing double assertions. Geo eps is not that small.
  }

  private DistanceCalculator dc() {
    return ctx.getDistCalc();
  }

  @Test
  public void testSomeDistances() {
    //See to verify: from http://www.movable-type.co.uk/scripts/latlong.html
    Point ctr = pLL(0,100);
    assertEquals(11100, dc().distance(ctr, pLL(10, 0)) * DEG_TO_KM, 3);
    double deg = dc().distance(ctr, pLL(10, -160));
    assertEquals(11100, deg * DEG_TO_KM, 3);

    assertEquals(314.40338, dc().distance(pLL(1, 2), pLL(3, 4)) * DEG_TO_KM, EPS);
  }

  @Test
  public void testCalcBoxByDistFromPt() {
    //first test regression
    {
      double d = 6894.1 * KM_TO_DEG;
      Point pCtr = pLL(-20, 84);
      Point pTgt = pLL(-42, 15);
      assertTrue(dc().distance(pCtr, pTgt) < d);
      //since the pairwise distance is less than d, a bounding box from ctr with d should contain pTgt.
      Rectangle r = dc().calcBoxByDistFromPt(pCtr, d, ctx, null);
      assertEquals(SpatialRelation.CONTAINS,r.relate(pTgt));
      checkBBox(pCtr,d);
    }

    assertEquals("0 dist, horiz line",
        -45,dc().calcBoxByDistFromPt_yHorizAxisDEG(ctx.makePoint(-180, -45), 0, ctx),0);

    double MAXDIST = (double) 180 * DEG_TO_KM;
    checkBBox(ctx.makePoint(0,0), MAXDIST);
    checkBBox(ctx.makePoint(0,0), MAXDIST *0.999999);
    checkBBox(ctx.makePoint(0,0),0);
    checkBBox(ctx.makePoint(0,0),0.000001);
    checkBBox(ctx.makePoint(0,90),0.000001);
    checkBBox(ctx.makePoint(-32.7,-5.42),9829);
    checkBBox(ctx.makePoint(0,90-20), (double) 20 * DEG_TO_KM);
    {
      double d = 0.010;//10m
      checkBBox(ctx.makePoint(0,90- (d + 0.001) * KM_TO_DEG),d);
    }

    for (int T = 0; T < 100; T++) {
      double lat = -90 + randomDouble()*180;
      double lon = -180 + randomDouble()*360;
      Point ctr = ctx.makePoint(lon, lat);
      double dist = MAXDIST*randomDouble();
      checkBBox(ctr, dist);
    }

  }

  private void checkBBox(Point ctr, double distKm) {
    String msg = "ctr: "+ctr+" distKm: "+distKm;
    double dist = distKm * KM_TO_DEG;

    Rectangle r = dc().calcBoxByDistFromPt(ctr, dist, ctx, null);
    double horizAxisLat = dc().calcBoxByDistFromPt_yHorizAxisDEG(ctr, dist, ctx);
    if (!Double.isNaN(horizAxisLat))
      assertTrue(r.relateYRange(horizAxisLat, horizAxisLat).intersects());

    //horizontal
    if (r.getWidth() >= 180) {
      double deg = dc().distance(ctr, r.getMinX(), r.getMaxY() == 90 ? 90 : -90);
      double calcDistKm = deg * DEG_TO_KM;
      assertTrue(msg, calcDistKm <= distKm + EPS);
      //horizAxisLat is meaningless in this context
    } else {
      Point tPt = findClosestPointOnVertToPoint(r.getMinX(), r.getMinY(), r.getMaxY(), ctr);
      double calcDistKm = dc().distance(ctr, tPt) * DEG_TO_KM;
      assertEquals(msg, distKm, calcDistKm, EPS);
      assertEquals(msg, tPt.getY(), horizAxisLat, EPS);
    }

    //vertical
    double topDistKm = dc().distance(ctr, ctr.getX(), r.getMaxY()) * DEG_TO_KM;
    if (r.getMaxY() == 90)
      assertTrue(msg, topDistKm <= distKm + EPS);
    else
      assertEquals(msg, distKm, topDistKm, EPS);
    double botDistKm = dc().distance(ctr, ctr.getX(), r.getMinY()) * DEG_TO_KM;
    if (r.getMinY() == -90)
      assertTrue(msg, botDistKm <= distKm + EPS);
    else
      assertEquals(msg, distKm, botDistKm, EPS);
  }

  private Point findClosestPointOnVertToPoint(double lon, double lowLat, double highLat, Point ctr) {
    //A binary search algorithm to find the point along the vertical lon between lowLat & highLat that is closest
    // to ctr, and returns the distance.
    double midLat = (highLat - lowLat)/2 + lowLat;
    double midLatDist = ctx.getDistCalc().distance(ctr,lon,midLat);
    for(int L = 0; L < 100 && (highLat - lowLat > 0.001|| L < 20); L++) {
      boolean bottom = (midLat - lowLat > highLat - midLat);
      double newMid = bottom ? (midLat - lowLat)/2 + lowLat : (highLat - midLat)/2 + midLat;
      double newMidDist = ctx.getDistCalc().distance(ctr,lon,newMid);
      if (newMidDist < midLatDist) {
        if (bottom) {
          highLat = midLat;
        } else {
          lowLat = midLat;
        }
        midLat = newMid;
        midLatDist = newMidDist;
      } else {
        if (bottom) {
          lowLat = newMid;
        } else {
          highLat = newMid;
        }
      }
    }
    return ctx.makePoint(lon,midLat);
  }

  @Test
  public void testDistCalcPointOnBearing_cartesian() {
    ctx = new SpatialContext(false);
    EPS = 10e-6;//tighter epsilon (aka delta)
    for(int i = 0; i < 1000; i++) {
      testDistCalcPointOnBearing(randomInt(100));
    }
  }

  @Test
  public void testDistCalcPointOnBearing_geo() {
    //The haversine formula has a higher error if the points are near antipodal. We adjust EPS tolerance for this case.
    //TODO Eventually we should add the Vincenty formula for improved accuracy, or try some other cleverness.

    //test known high delta
//    {
//      Point c = ctx.makePoint(-103,-79);
//      double angRAD = Math.toRadians(236);
//      double dist = 20025;
//      Point p2 = dc().pointOnBearingRAD(c, dist, angRAD, ctx);
//      //Pt(x=76.61200011750923,y=79.04946929870962)
//      double calcDist = dc().distance(c, p2);
//      assertEqualsRatio(dist, calcDist);
//    }
    double maxDistKm = (double) 180 * DEG_TO_KM;
    for(int i = 0; i < 1000; i++) {
      int distKm = randomInt((int) maxDistKm);
      EPS = (distKm < maxDistKm*0.75 ? 10e-6 : 10e-3);
      testDistCalcPointOnBearing(distKm);
    }
  }

  private void testDistCalcPointOnBearing(double distKm) {
    for(int angDEG = 0; angDEG < 360; angDEG += randomIntBetween(1,20)) {
      Point c = ctx.makePoint(
              DistanceUtils.normLonDEG(randomInt(359)),
              randomIntBetween(-90,90));

      //0 distance means same point
      Point p2 = dc().pointOnBearing(c, 0, angDEG, ctx, null);
      assertEquals(c,p2);

      p2 = dc().pointOnBearing(c, distKm * KM_TO_DEG, angDEG, ctx, null);
      double calcDistKm = dc().distance(c, p2) * DEG_TO_KM;
      assertEqualsRatio(distKm, calcDistKm);
    }
  }

  private void assertEqualsRatio(double expected, double actual) {
    double delta = Math.abs(actual - expected);
    double base = Math.min(actual, expected);
    double deltaRatio = base==0 ? delta : Math.min(delta,delta / base);
    assertEquals(0,deltaRatio, EPS);
  }

  @Test
  public void testNormLat() {
    double[][] lats = new double[][] {
        {1.23,1.23},//1.23 might become 1.2299999 after some math and we want to ensure that doesn't happen
        {-90,-90},{90,90},{0,0}, {-100,-80},
        {-90-180,90},{-90-360,-90},{90+180,-90},{90+360,90},
        {-12+180,12}};
    for (double[] pair : lats) {
      assertEquals("input "+pair[0], pair[1], DistanceUtils.normLatDEG(pair[0]), 0);
    }

    for(int i = -1000; i < 1000; i += randomInt(9)*10) {
      double d = DistanceUtils.normLatDEG(i);
      assertTrue(i + " " + d, d >= -90 && d <= 90);
    }
  }

  @Test
  public void testNormLon() {
    double[][] lons = new double[][] {
        {1.23,1.23},//1.23 might become 1.2299999 after some math and we want to ensure that doesn't happen
        {-180,-180},{180,+180},{0,0}, {-190,170},{181,-179},
        {-180-360,-180},{-180-720,-180},
        {180+360,+180},{180+720,+180}};
    for (double[] pair : lons) {
      assertEquals("input " + pair[0], pair[1], DistanceUtils.normLonDEG(pair[0]), 0);
    }

    for(int i = -1000; i < 1000; i += randomInt(9)*10) {
      double d = DistanceUtils.normLonDEG(i);
      assertTrue(i + " " + d, d >= -180 && d <= 180);
    }
  }

  @Test
  public void assertDistanceConversion() {
    assertDistanceConversion(0);
    assertDistanceConversion(500);
    assertDistanceConversion(DistanceUtils.EARTH_MEAN_RADIUS_KM);
  }

  private void assertDistanceConversion(double dist) {
    double radius = DistanceUtils.EARTH_MEAN_RADIUS_KM;
    //test back & forth conversion for both
    double distRAD = DistanceUtils.dist2Radians(dist, radius);
    assertEquals(dist, DistanceUtils.radians2Dist(distRAD, radius), EPS);
    double distDEG = DistanceUtils.dist2Degrees(dist, radius);
    assertEquals(dist, DistanceUtils.degrees2Dist(distDEG, radius), EPS);
    //test across rad & deg
    assertEquals(distDEG,DistanceUtils.toDegrees(distRAD),EPS);
    //test point on bearing
    assertEquals(
        DistanceUtils.pointOnBearingRAD(0, 0, DistanceUtils.dist2Radians(dist, radius), DistanceUtils.DEG_90_AS_RADS, ctx, new PointImpl(0, 0, ctx)).getX(),
        distRAD, 10e-5);
  }

  private Point pLL(double lat, double lon) {
    return ctx.makePoint(lon,lat);
  }

  @Test
  public void testArea() {
    double radius = DistanceUtils.EARTH_MEAN_RADIUS_KM * KM_TO_DEG;
    //surface of a sphere is 4 * pi * r^2
    final double earthArea = 4 * Math.PI * radius * radius;

    Circle c = ctx.makeCircle(randomIntBetween(-180,180), randomIntBetween(-90,90),
            180);//180 means whole earth
    assertEquals(earthArea, c.getArea(ctx), 1.0);
    assertEquals(earthArea, ctx.getWorldBounds().getArea(ctx), 1.0);

    //now check half earth
    Circle cHalf = ctx.makeCircle(c.getCenter(), 90);
    assertEquals(earthArea/2, cHalf.getArea(ctx), 1.0);

    //circle with same radius at +20 lat with one at -20 lat should have same area as well as bbox with same area
    Circle c2 = ctx.makeCircle(c.getCenter(), 30);
    Circle c3 = ctx.makeCircle(c.getCenter().getX(), 20, 30);
    assertEquals(c2.getArea(ctx), c3.getArea(ctx), 0.01);
    Circle c3Opposite = ctx.makeCircle(c.getCenter().getX(), -20, 30);
    assertEquals(c3.getArea(ctx), c3Opposite.getArea(ctx), 0.01);
    assertEquals(c3.getBoundingBox().getArea(ctx), c3Opposite.getBoundingBox().getArea(ctx), 0.01);

    //small shapes near the equator should have similar areas to euclidean rectangle
    Rectangle smallRect = ctx.makeRectangle(0, 1, 0, 1);
    assertEquals(1.0, smallRect.getArea(null), 0.0);
    double smallDelta = smallRect.getArea(null) - smallRect.getArea(ctx);
    assertTrue(smallDelta > 0 && smallDelta < 0.0001);

    Circle smallCircle = ctx.makeCircle(0,0,1);
    smallDelta = smallCircle.getArea(null) - smallCircle.getArea(ctx);
    assertTrue(smallDelta > 0 && smallDelta < 0.0001);

    //bigger, but still fairly similar
    //c2 = ctx.makeCircle(c.getCenter(), 30);
    double areaRatio = c2.getArea(null) / c2.getArea(ctx);
    assertTrue(areaRatio > 1 && areaRatio < 1.1);
  }

=======
 * Copyright 2010, 2011, 2012 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.map.writer.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mapsforge.map.writer.model.GeoCoordinate;
import org.mapsforge.map.writer.model.MercatorProjection;
import org.mapsforge.map.writer.model.TDNode;
import org.mapsforge.map.writer.model.TDWay;
import org.mapsforge.map.writer.model.TileCoordinate;
import org.mapsforge.map.writer.model.WayDataBlock;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

/**
 * Provides utility functions for the maps preprocessing.
 * 
 * @author bross
 */
public final class GeoUtils {

	private GeoUtils() {
	}

	// private static final double DOUGLAS_PEUCKER_SIMPLIFICATION_TOLERANCE = 0.0000188;
	// private static final double DOUGLAS_PEUCKER_SIMPLIFICATION_TOLERANCE = 0.00003;
	/**
	 * The minimum amount of nodes required for a valid closed polygon.
	 */
	public static final int MIN_NODES_POLYGON = 4;

	/**
	 * The minimum amount of coordinates (lat/lon counted separately) required for a valid closed polygon.
	 */
	public static final int MIN_COORDINATES_POLYGON = 8;
	private static final byte SUBTILE_ZOOMLEVEL_DIFFERENCE = 2;
	private static final double[] EPSILON_ZERO = new double[] { 0, 0 };
	private static final Logger LOGGER = Logger.getLogger(GeoUtils.class.getName());

	private static final int[] TILE_BITMASK_VALUES = new int[] { 32768, 16384, 8192, 4096, 2048, 1024, 512, 256, 128,
			64, 32, 16, 8, 4, 2, 1 };

	// JTS
	private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

	// **************** WAY OR POI IN TILE *****************
	/**
	 * Computes which tiles on the given base zoom level need to include the given way (which may be a polygon).
	 * 
	 * @param way
	 *            the way that is mapped to tiles
	 * @param baseZoomLevel
	 *            the base zoom level which is used in the mapping
	 * @param enlargementInMeter
	 *            amount of pixels that is used to enlarge the bounding box of the way and the tiles in the mapping
	 *            process
	 * @return all tiles on the given base zoom level that need to include the given way, an empty set if no tiles are
	 *         matched
	 */
	public static Set<TileCoordinate> mapWayToTiles(final TDWay way, final byte baseZoomLevel,
			final int enlargementInMeter) {
		if (way == null) {
			LOGGER.fine("way is null in mapping to tiles");
			return Collections.emptySet();
		}

		HashSet<TileCoordinate> matchedTiles = new HashSet<TileCoordinate>();
		Geometry wayGeometry = toJTSGeometry(way, !way.isForcePolygonLine());
		if (wayGeometry == null) {
			LOGGER.fine("unable to create geometry from way: " + way.getId());
			return matchedTiles;
		}

		TileCoordinate[] bbox = getWayBoundingBox(way, baseZoomLevel, enlargementInMeter);
		// calculate the tile coordinates and the corresponding bounding boxes
		try {
			for (int k = bbox[0].getX(); k <= bbox[1].getX(); k++) {
				for (int l = bbox[0].getY(); l <= bbox[1].getY(); l++) {
					Geometry bboxGeometry = tileToJTSGeometry(k, l, baseZoomLevel, enlargementInMeter);
					if (bboxGeometry.intersects(wayGeometry)) {
						matchedTiles.add(new TileCoordinate(k, l, baseZoomLevel));
					}
				}
			}
		} catch (TopologyException e) {
			LOGGER.log(Level.FINE,
					"encountered error during mapping of a way to corresponding tiles, way id: " + way.getId());
			return Collections.emptySet();
		}

		return matchedTiles;
	}

	/**
	 * @param point
	 *            the point
	 * @param tile
	 *            the tile
	 * @return true if the point is located in the given tile
	 */
	public static boolean pointInTile(GeoCoordinate point, TileCoordinate tile) {
		if (point == null || tile == null) {
			return false;
		}

		int lon1 = GeoCoordinate.doubleToInt(MercatorProjection.tileXToLongitude(tile.getX(), tile.getZoomlevel()));
		int lon2 = GeoCoordinate.doubleToInt(MercatorProjection.tileXToLongitude(tile.getX() + 1, tile.getZoomlevel()));
		int lat1 = GeoCoordinate.doubleToInt(MercatorProjection.tileYToLatitude(tile.getY(), tile.getZoomlevel()));
		int lat2 = GeoCoordinate.doubleToInt(MercatorProjection.tileYToLatitude(tile.getY() + 1, tile.getZoomlevel()));
		return point.getLatitudeE6() <= lat1 && point.getLatitudeE6() >= lat2 && point.getLongitudeE6() >= lon1
				&& point.getLongitudeE6() <= lon2;
	}

	// *********** PREPROCESSING OF WAYS **************

	/**
	 * Clips a geometry to a tile.
	 * 
	 * @param way
	 *            the way
	 * @param geometry
	 *            the geometry
	 * @param tileCoordinate
	 *            the tile coordinate
	 * @param enlargementInMeters
	 *            the bounding box buffer
	 * @return the clipped geometry
	 */
	public static Geometry clipToTile(TDWay way, Geometry geometry, TileCoordinate tileCoordinate,
			int enlargementInMeters) {
		// clip geometry?
		Geometry tileBBJTS = null;
		Geometry ret = null;

		// create tile bounding box
		tileBBJTS = tileToJTSGeometry(tileCoordinate.getX(), tileCoordinate.getY(), tileCoordinate.getZoomlevel(),
				enlargementInMeters);

		// clip the polygon/ring by intersection with the bounding box of the tile
		// may throw a TopologyException
		try {
			// geometry = OverlayOp.overlayOp(tileBBJTS, geometry, OverlayOp.INTERSECTION);
			ret = tileBBJTS.intersection(geometry);
		} catch (TopologyException e) {
			LOGGER.log(Level.FINE, "JTS cannot clip way, not storing it in data file: " + way.getId(), e);
			way.setInvalid(true);
			return null;
		}
		return ret;
	}

	/**
	 * Simplifies a geometry using the Douglas Peucker algorithm.
	 * 
	 * @param way
	 *            the way
	 * @param geometry
	 *            the geometry
	 * @param zoomlevel
	 *            the zoom level
	 * @param simplificationFactor
	 *            the simplification factor
	 * @return the simplified geometry
	 */
	public static Geometry simplifyGeometry(TDWay way, Geometry geometry, byte zoomlevel, double simplificationFactor) {
		Geometry ret = null;

		Envelope bbox = geometry.getEnvelopeInternal();
		// compute maximal absolute latitude (so that we don't need to care if we
		// are on northern or southern hemisphere)
		double latMax = Math.max(Math.abs(bbox.getMaxY()), Math.abs(bbox.getMinY()));
		double deltaLat = MercatorProjection.deltaLat(simplificationFactor, latMax, zoomlevel);

		try {
			ret = TopologyPreservingSimplifier.simplify(geometry, deltaLat);
		} catch (TopologyException e) {
			LOGGER.log(Level.FINE,
					"JTS cannot simplify way due to an error, not simplifying way with id: " + way.getId(), e);
			way.setInvalid(true);
			return geometry;
		}

		return ret;
	}

	/**
	 * A tile on zoom level <i>z</i> has exactly 16 sub tiles on zoom level <i>z+2</i>. For each of these 16 sub tiles
	 * it is analyzed if the given way needs to be included. The result is represented as a 16 bit short value. Each bit
	 * represents one of the 16 sub tiles. A bit is set to 1 if the sub tile needs to include the way. Representation is
	 * row-wise.
	 * 
	 * @param geometry
	 *            the geometry which is analyzed
	 * @param tile
	 *            the tile which is split into 16 sub tiles
	 * @param enlargementInMeter
	 *            amount of pixels that is used to enlarge the bounding box of the way and the tiles in the mapping
	 *            process
	 * @return a 16 bit short value that represents the information which of the sub tiles needs to include the way
	 */
	public static short computeBitmask(final Geometry geometry, final TileCoordinate tile, // NOPMD by bross on
																							// 25.12.11 13:30
			final int enlargementInMeter) {
		List<TileCoordinate> subtiles = tile
				.translateToZoomLevel((byte) (tile.getZoomlevel() + SUBTILE_ZOOMLEVEL_DIFFERENCE));

		short bitmask = 0; // NOPMD by bross on 25.12.11 13:30
		int tileCounter = 0;
		for (TileCoordinate subtile : subtiles) {
			Geometry bbox = tileToJTSGeometry(subtile.getX(), subtile.getY(), subtile.getZoomlevel(),
					enlargementInMeter);
			if (bbox.intersects(geometry)) {
				bitmask |= TILE_BITMASK_VALUES[tileCounter];
			}
			tileCounter++;
		}
		return bitmask;
	}

	/**
	 * @param geometry
	 *            a JTS {@link Geometry} object representing the OSM entity
	 * @param tile
	 *            the tile
	 * @param enlargementInMeter
	 *            the enlargement of the tile in meters
	 * @return true, if the geometry is covered completely by this tile
	 */
	public static boolean coveredByTile(final Geometry geometry, final TileCoordinate tile, final int enlargementInMeter) {
		Geometry bbox = tileToJTSGeometry(tile.getX(), tile.getY(), tile.getZoomlevel(), enlargementInMeter);
		if (bbox.covers(geometry)) {
			return true;
		}

		return false;
	}

	/**
	 * @param geometry
	 *            the JTS {@link Geometry} object
	 * @return the centroid of the given geometry
	 */
	public static GeoCoordinate computeCentroid(Geometry geometry) {
		Point centroid = geometry.getCentroid();
		if (centroid != null) {
			return new GeoCoordinate(centroid.getCoordinate().y, centroid.getCoordinate().x);
		}

		return null;
	}

	/**
	 * Convert a JTS Geometry to a WayDataBlock list.
	 * 
	 * @param geometry
	 *            a geometry object which should be converted
	 * @return a list of WayBlocks which you can use to save the way.
	 */
	public static List<WayDataBlock> toWayDataBlockList(Geometry geometry) {
		List<WayDataBlock> res = new ArrayList<WayDataBlock>();
		if (geometry instanceof MultiPolygon) {
			MultiPolygon mp = (MultiPolygon) geometry;
			for (int i = 0; i < mp.getNumGeometries(); i++) {
				Polygon p = (Polygon) mp.getGeometryN(i);
				List<Integer> outer = toCoordinateList(p.getExteriorRing());
				List<List<Integer>> inner = new ArrayList<List<Integer>>();
				for (int j = 0; j < p.getNumInteriorRing(); j++) {
					inner.add(toCoordinateList(p.getInteriorRingN(j)));
				}
				res.add(new WayDataBlock(outer, inner));
			}
		} else if (geometry instanceof Polygon) {
			Polygon p = (Polygon) geometry;
			List<Integer> outer = toCoordinateList(p.getExteriorRing());
			List<List<Integer>> inner = new ArrayList<List<Integer>>();
			for (int i = 0; i < p.getNumInteriorRing(); i++) {
				inner.add(toCoordinateList(p.getInteriorRingN(i)));
			}
			res.add(new WayDataBlock(outer, inner));
		} else if (geometry instanceof MultiLineString) {
			MultiLineString ml = (MultiLineString) geometry;
			for (int i = 0; i < ml.getNumGeometries(); i++) {
				LineString l = (LineString) ml.getGeometryN(i);
				res.add(new WayDataBlock(toCoordinateList(l), null));
			}
		} else if (geometry instanceof LinearRing || geometry instanceof LineString) {
			res.add(new WayDataBlock(toCoordinateList(geometry), null));
		} else if (geometry instanceof GeometryCollection) {
			GeometryCollection gc = (GeometryCollection) geometry;
			for (int i = 0; i < gc.getNumGeometries(); i++) {
				List<WayDataBlock> recursiveResult = toWayDataBlockList(gc.getGeometryN(i));
				for (WayDataBlock wayDataBlock : recursiveResult) {
					res.add(wayDataBlock);
				}
			}
		}

		return res;
	}

	// **************** JTS CONVERSIONS *********************

	/**
	 * Converts a way with potential inner ways to a JTS geometry.
	 * 
	 * @param way
	 *            the way
	 * @param innerWays
	 *            the inner ways or null
	 * @return the JTS geometry
	 */
	public static Geometry toJtsGeometry(TDWay way, List<TDWay> innerWays) {

		Geometry wayGeometry = toJTSGeometry(way, !way.isForcePolygonLine());
		if (wayGeometry == null) {
			return null;
		}

		if (innerWays != null) {
			List<LinearRing> innerWayGeometries = new ArrayList<LinearRing>();
			if (!(wayGeometry instanceof Polygon)) {
				LOGGER.warning("outer way of multi polygon is not a polygon, skipping it: " + way.getId());
				return null;
			}
			Polygon outerPolygon = (Polygon) wayGeometry;

			for (TDWay innerWay : innerWays) {
				// in order to build the polygon with holes, we want to create
				// linear rings of the inner ways
				Geometry innerWayGeometry = toJTSGeometry(innerWay, false);
				if (innerWayGeometry == null) {
					continue;
				}

				if (!(innerWayGeometry instanceof LinearRing)) {
					LOGGER.warning("inner way of multi polygon is not a polygon, skipping it, inner id: "
							+ innerWay.getId() + ", outer id: " + way.getId());
					continue;
				}

				LinearRing innerRing = (LinearRing) innerWayGeometry;

				// check if inner way is completely contained in outer way
				if (outerPolygon.covers(innerRing)) {
					innerWayGeometries.add(innerRing);
				} else {
					LOGGER.warning("inner way is not contained in outer way, skipping inner way, inner id: "
							+ innerWay.getId() + ", outer id: " + way.getId());
				}
			}

			if (!innerWayGeometries.isEmpty()) {
				// make wayGeometry a new Polygon that contains inner ways as holes
				LinearRing[] holes = innerWayGeometries.toArray(new LinearRing[innerWayGeometries.size()]);
				LinearRing exterior = GEOMETRY_FACTORY
						.createLinearRing(outerPolygon.getExteriorRing().getCoordinates());
				wayGeometry = new Polygon(exterior, holes, GEOMETRY_FACTORY);
			}

		}

		return wayGeometry;
	}

	/**
	 * Internal conversion method to convert our internal data structure for ways to geometry objects in JTS. It will
	 * care about ways and polygons and will create the right JTS onjects.
	 * 
	 * @param way
	 *            TDway which will be converted. Null if we were not able to convert the way to a Geometry object.
	 * @param area
	 *            true, if the way represents an area, i.e. a polygon instead of a linear ring
	 * @return return Converted way as JTS object.
	 */
	private static Geometry toJTSGeometry(TDWay way, boolean area) {
		if (way.getWayNodes().length < 2) {
			LOGGER.fine("way has fewer than 2 nodes: " + way.getId());
			return null;
		}

		Coordinate[] coordinates = new Coordinate[way.getWayNodes().length];
		for (int i = 0; i < coordinates.length; i++) {
			TDNode currentNode = way.getWayNodes()[i];
			coordinates[i] = new Coordinate(GeoCoordinate.intToDouble(currentNode.getLongitude()),
					GeoCoordinate.intToDouble(currentNode.getLatitude()));
		}

		Geometry res = null;

		try {
			// check for closed polygon
			if (way.isPolygon()) {
				if (area) {
					// polygon
					res = GEOMETRY_FACTORY.createPolygon(GEOMETRY_FACTORY.createLinearRing(coordinates), null);
				} else {
					// linear ring
					res = GEOMETRY_FACTORY.createLinearRing(coordinates);
				}
			} else {
				res = GEOMETRY_FACTORY.createLineString(coordinates);
			}
		} catch (TopologyException e) {
			LOGGER.log(Level.FINE, "error creating JTS geometry from way: " + way.getId(), e);
			return null;
		}
		return res;
	}

	private static List<Integer> toCoordinateList(Geometry jtsGeometry) {

		Coordinate[] jtsCoords = jtsGeometry.getCoordinates();

		ArrayList<Integer> result = new ArrayList<Integer>();

		for (int j = 0; j < jtsCoords.length; j++) {
			GeoCoordinate geoCoord = new GeoCoordinate(jtsCoords[j].y, jtsCoords[j].x);
			result.add(Integer.valueOf(geoCoord.getLatitudeE6()));
			result.add(Integer.valueOf(geoCoord.getLongitudeE6()));
		}

		return result;

	}

	private static double[] computeTileEnlargement(double lat, int enlargementInPixel) {

		if (enlargementInPixel == 0) {
			return EPSILON_ZERO; // NOPMD by bross on 25.12.11 13:32
		}

		double[] epsilons = new double[2];

		epsilons[0] = GeoCoordinate.latitudeDistance(enlargementInPixel);
		epsilons[1] = GeoCoordinate.longitudeDistance(enlargementInPixel, lat);

		return epsilons;
	}

	private static double[] bufferInDegrees(long tileY, byte zoom, int enlargementInMeter) {
		if (enlargementInMeter == 0) {
			return EPSILON_ZERO; // NOPMD by bross on 25.12.11 13:32
		}

		double[] epsilons = new double[2];
		double lat = MercatorProjection.tileYToLatitude(tileY, zoom);
		epsilons[0] = GeoCoordinate.latitudeDistance(enlargementInMeter);
		epsilons[1] = GeoCoordinate.longitudeDistance(enlargementInMeter, lat);

		return epsilons;
	}

	private static Geometry tileToJTSGeometry(long tileX, long tileY, byte zoom, int enlargementInMeter) {
		double minLat = MercatorProjection.tileYToLatitude(tileY + 1, zoom);
		double maxLat = MercatorProjection.tileYToLatitude(tileY, zoom);
		double minLon = MercatorProjection.tileXToLongitude(tileX, zoom);
		double maxLon = MercatorProjection.tileXToLongitude(tileX + 1, zoom);

		double[] epsilons = bufferInDegrees(tileY, zoom, enlargementInMeter);

		minLon -= epsilons[1];
		minLat -= epsilons[0];
		maxLon += epsilons[1];
		maxLat += epsilons[0];

		Coordinate bottomLeft = new Coordinate(minLon, minLat);
		Coordinate topRight = new Coordinate(maxLon, maxLat);

		return GEOMETRY_FACTORY.createLineString(new Coordinate[] { bottomLeft, topRight }).getEnvelope();
	}

	private static TileCoordinate[] getWayBoundingBox(final TDWay way, byte zoomlevel, int enlargementInPixel) {
		double maxx = Double.NEGATIVE_INFINITY, maxy = Double.NEGATIVE_INFINITY, minx = Double.POSITIVE_INFINITY, miny = Double.POSITIVE_INFINITY;
		for (TDNode coordinate : way.getWayNodes()) {
			maxy = Math.max(maxy, GeoCoordinate.intToDouble(coordinate.getLatitude()));
			miny = Math.min(miny, GeoCoordinate.intToDouble(coordinate.getLatitude()));
			maxx = Math.max(maxx, GeoCoordinate.intToDouble(coordinate.getLongitude()));
			minx = Math.min(minx, GeoCoordinate.intToDouble(coordinate.getLongitude()));
		}

		double[] epsilonsTopLeft = computeTileEnlargement(maxy, enlargementInPixel);
		double[] epsilonsBottomRight = computeTileEnlargement(miny, enlargementInPixel);

		TileCoordinate[] bbox = new TileCoordinate[2];
		bbox[0] = new TileCoordinate((int) MercatorProjection.longitudeToTileX(minx - epsilonsTopLeft[1], zoomlevel),
				(int) MercatorProjection.latitudeToTileY(maxy + epsilonsTopLeft[0], zoomlevel), zoomlevel);
		bbox[1] = new TileCoordinate(
				(int) MercatorProjection.longitudeToTileX(maxx + epsilonsBottomRight[1], zoomlevel),
				(int) MercatorProjection.latitudeToTileY(miny - epsilonsBottomRight[0], zoomlevel), zoomlevel);

		return bbox;
	}
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

