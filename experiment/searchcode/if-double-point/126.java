<<<<<<< HEAD
/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.index.search.geo;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.NumericUtils;
import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.lucene.docset.AndDocIdSet;
import org.elasticsearch.common.lucene.docset.DocIdSets;
import org.elasticsearch.common.lucene.docset.MatchDocIdSet;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.fielddata.GeoPointValues;
import org.elasticsearch.index.fielddata.IndexGeoPointFieldData;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper;

import java.io.IOException;

/**
 *
 */
public class GeoDistanceRangeFilter extends Filter {

    private final double lat;
    private final double lon;

    private final double inclusiveLowerPoint; // in miles
    private final double inclusiveUpperPoint; // in miles

    private final GeoDistance geoDistance;
    private final GeoDistance.FixedSourceDistance fixedSourceDistance;
    private GeoDistance.DistanceBoundingCheck distanceBoundingCheck;
    private final Filter boundingBoxFilter;

    private final IndexGeoPointFieldData indexFieldData;

    public GeoDistanceRangeFilter(GeoPoint point, Double lowerVal, Double upperVal, boolean includeLower, boolean includeUpper, GeoDistance geoDistance, GeoPointFieldMapper mapper, IndexGeoPointFieldData indexFieldData,
                                  String optimizeBbox) {
        this.lat = point.lat();
        this.lon = point.lon();
        this.geoDistance = geoDistance;
        this.indexFieldData = indexFieldData;

        this.fixedSourceDistance = geoDistance.fixedSourceDistance(lat, lon, DistanceUnit.MILES);

        if (lowerVal != null) {
            double f = lowerVal.doubleValue();
            long i = NumericUtils.doubleToSortableLong(f);
            inclusiveLowerPoint = NumericUtils.sortableLongToDouble(includeLower ? i : (i + 1L));
        } else {
            inclusiveLowerPoint = Double.NEGATIVE_INFINITY;
        }
        if (upperVal != null) {
            double f = upperVal.doubleValue();
            long i = NumericUtils.doubleToSortableLong(f);
            inclusiveUpperPoint = NumericUtils.sortableLongToDouble(includeUpper ? i : (i - 1L));
        } else {
            inclusiveUpperPoint = Double.POSITIVE_INFINITY;
            // we disable bounding box in this case, since the upper point is all and we create bounding box up to the
            // upper point it will effectively include all
            // TODO we can create a bounding box up to from and "not" it
            optimizeBbox = null;
        }

        if (optimizeBbox != null && !"none".equals(optimizeBbox)) {
            distanceBoundingCheck = GeoDistance.distanceBoundingCheck(lat, lon, inclusiveUpperPoint, DistanceUnit.MILES);
            if ("memory".equals(optimizeBbox)) {
                boundingBoxFilter = null;
            } else if ("indexed".equals(optimizeBbox)) {
                boundingBoxFilter = IndexedGeoBoundingBoxFilter.create(distanceBoundingCheck.topLeft(), distanceBoundingCheck.bottomRight(), mapper);
                distanceBoundingCheck = GeoDistance.ALWAYS_INSTANCE; // fine, we do the bounding box check using the filter
            } else {
                throw new ElasticSearchIllegalArgumentException("type [" + optimizeBbox + "] for bounding box optimization not supported");
            }
        } else {
            distanceBoundingCheck = GeoDistance.ALWAYS_INSTANCE;
            boundingBoxFilter = null;
        }
    }

    public double lat() {
        return lat;
    }

    public double lon() {
        return lon;
    }

    public GeoDistance geoDistance() {
        return geoDistance;
    }

    @Override
    public DocIdSet getDocIdSet(AtomicReaderContext context, Bits acceptedDocs) throws IOException {
        DocIdSet boundingBoxDocSet = null;
        if (boundingBoxFilter != null) {
            boundingBoxDocSet = boundingBoxFilter.getDocIdSet(context, acceptedDocs);
            if (DocIdSets.isEmpty(boundingBoxDocSet)) {
                return null;
            }
        }
        GeoPointValues values = indexFieldData.load(context).getGeoPointValues();
        GeoDistanceRangeDocSet distDocSet = new GeoDistanceRangeDocSet(context.reader().maxDoc(), acceptedDocs, values, fixedSourceDistance, distanceBoundingCheck, inclusiveLowerPoint, inclusiveUpperPoint);
        if (boundingBoxDocSet == null) {
            return distDocSet;
        } else {
            return new AndDocIdSet(new DocIdSet[]{boundingBoxDocSet, distDocSet});
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeoDistanceRangeFilter filter = (GeoDistanceRangeFilter) o;

        if (Double.compare(filter.inclusiveLowerPoint, inclusiveLowerPoint) != 0) return false;
        if (Double.compare(filter.inclusiveUpperPoint, inclusiveUpperPoint) != 0) return false;
        if (Double.compare(filter.lat, lat) != 0) return false;
        if (Double.compare(filter.lon, lon) != 0) return false;
        if (!indexFieldData.getFieldNames().indexName().equals(filter.indexFieldData.getFieldNames().indexName()))
            return false;
        if (geoDistance != filter.geoDistance) return false;

        return true;
    }

    @Override
    public String toString() {
        return "GeoDistanceRangeFilter(" + indexFieldData.getFieldNames().indexName() + ", " + geoDistance + ", [" + inclusiveLowerPoint + " - " + inclusiveUpperPoint + "], " + lat + ", " + lon + ")";
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = lat != +0.0d ? Double.doubleToLongBits(lat) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = lon != +0.0d ? Double.doubleToLongBits(lon) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = inclusiveLowerPoint != +0.0d ? Double.doubleToLongBits(inclusiveLowerPoint) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = inclusiveUpperPoint != +0.0d ? Double.doubleToLongBits(inclusiveUpperPoint) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (geoDistance != null ? geoDistance.hashCode() : 0);
        result = 31 * result + indexFieldData.getFieldNames().indexName().hashCode();
        return result;
    }

    public static class GeoDistanceRangeDocSet extends MatchDocIdSet {

        private final GeoPointValues values;
        private final GeoDistance.FixedSourceDistance fixedSourceDistance;
        private final GeoDistance.DistanceBoundingCheck distanceBoundingCheck;
        private final double inclusiveLowerPoint; // in miles
        private final double inclusiveUpperPoint; // in miles

        public GeoDistanceRangeDocSet(int maxDoc, @Nullable Bits acceptDocs, GeoPointValues values, GeoDistance.FixedSourceDistance fixedSourceDistance, GeoDistance.DistanceBoundingCheck distanceBoundingCheck,
                                      double inclusiveLowerPoint, double inclusiveUpperPoint) {
            super(maxDoc, acceptDocs);
            this.values = values;
            this.fixedSourceDistance = fixedSourceDistance;
            this.distanceBoundingCheck = distanceBoundingCheck;
            this.inclusiveLowerPoint = inclusiveLowerPoint;
            this.inclusiveUpperPoint = inclusiveUpperPoint;
        }

        @Override
        public boolean isCacheable() {
            return true;
        }

        @Override
        protected boolean matchDoc(int doc) {
            if (!values.hasValue(doc)) {
                return false;
            }

            if (values.isMultiValued()) {
                GeoPointValues.Iter iter = values.getIter(doc);
                while (iter.hasNext()) {
                    GeoPoint point = iter.next();
                    if (distanceBoundingCheck.isWithin(point.lat(), point.lon())) {
                        double d = fixedSourceDistance.calculate(point.lat(), point.lon());
                        if (d >= inclusiveLowerPoint && d <= inclusiveUpperPoint) {
                            return true;
                        }
                    }
                }
                return false;
            } else {
                GeoPoint point = values.getValue(doc);
                if (distanceBoundingCheck.isWithin(point.lat(), point.lon())) {
                    double d = fixedSourceDistance.calculate(point.lat(), point.lon());
                    if (d >= inclusiveLowerPoint && d <= inclusiveUpperPoint) {
                        return true;
                    }
                }
                return false;
            }
        }
    }
}
=======
package com.effectiveJava;

public class GeoLocationService {

	private static double EARTH_RADIUS_KM = 6371.009;

	/**
	 * Method used to convert the value form radians to degrees
	 * 
	 * @param rad
	 * @return value in degrees
	 */
	private static double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

	/**
	 * Converts the value from Degrees to radians
	 * 
	 * @param deg
	 * @return value in radians
	 */
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/**
	 * Returns the difference in degrees of longitude corresponding to the
	 * distance from the center point. This distance can be used to find the
	 * extreme points.
	 * 
	 * @param p1
	 * @param distance
	 * @return
	 */
	public static double getExtremeLongitudesDiffForPoint(Point p1,
			double distance) {
		double lat1 = p1.getLatitude();
		lat1 = deg2rad(lat1);
		double longitudeRadius = Math.cos(lat1) * EARTH_RADIUS_KM;
		double diffLong = (distance / longitudeRadius);
		diffLong = rad2deg(diffLong);
		return diffLong;
	}

	/**
	 * Returns the difference in degrees of latitude corresponding to the
	 * distance from the center point. This distance can be used to find the
	 * extreme points.
	 * 
	 * @param p1
	 * @param distance
	 * @return
	 */
	public static double getExtremeLatitudesDiffForPoint(Point p1,
			double distance) {
		double latitudeRadians = distance / EARTH_RADIUS_KM;
		double diffLat = rad2deg(latitudeRadians);
		return diffLat;
	}

	/**
	 * Returns an array of two extreme points corresponding to center point and
	 * the distance from the center point. These extreme points are the points
	 * with max/min latitude and longitude.
	 * 
	 * @param point
	 * @param distance
	 * @return
	 */
	public static Point[] getExtremePointsFrom(Point point, double distance) {
		double longDiff = getExtremeLongitudesDiffForPoint(point, distance);
		double latDiff = getExtremeLatitudesDiffForPoint(point, distance);
		Point p1 = new Point(point.getLatitude() - latDiff, point.getLongitude()
				- longDiff);
		p1 = validatePoint(p1);
		Point p2 = new Point(point.getLatitude() + latDiff, point.getLongitude()
				+ longDiff);
		p2 = validatePoint(p2);

		return new Point[]{p1, p2};
	}

	/**
	 * Validates if the point passed has valid values in degrees i.e. latitude
	 * lies between -90 and +90 and the longitude
	 * 
	 * @param point
	 * @return
	 */
	private static Point validatePoint(Point point) {
		if (point.getLatitude() > 90)
			point.setLatitude(90 - (point.getLatitude() - 90));
		if (point.getLatitude() < -90)
			point.setLatitude(-90 - (point.getLatitude() + 90));
		if (point.getLongitude() > 180)
			point.setLongitude(-180 + (point.getLongitude() - 180));
		if (point.getLongitude() < -180)
			point.setLongitude(180 + (point.getLongitude() + 180));

		return point;
	}

	/**
	 * Returns the distance between tow points
	 * 
	 * @param p1
	 * @param p2
	 * @param unit
	 * @return
	 */
	public static double getDistanceBetweenPoints(Point p1, Point p2,
			String unit) {
		double theta = p1.getLongitude() - p2.getLongitude();
		double dist = Math.sin(deg2rad(p1.getLatitude()))
				* Math.sin(deg2rad(p2.getLatitude()))
				+ Math.cos(deg2rad(p1.getLatitude()))
				* Math.cos(deg2rad(p2.getLatitude())) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit.equals("K")) {
			dist = dist * 1.609344;
		} else if (unit.equals("M")) {
			dist = dist * 0.8684;
		}
		return (dist);
	}
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

