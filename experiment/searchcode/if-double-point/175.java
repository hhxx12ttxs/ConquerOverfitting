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
 */
public class GeoDistanceFilter extends Filter {

    private final double lat;

    private final double lon;

    private final double distance; // in miles

    private final GeoDistance geoDistance;

    private final IndexGeoPointFieldData indexFieldData;

    private final GeoDistance.FixedSourceDistance fixedSourceDistance;
    private GeoDistance.DistanceBoundingCheck distanceBoundingCheck;
    private final Filter boundingBoxFilter;

    public GeoDistanceFilter(double lat, double lon, double distance, GeoDistance geoDistance, IndexGeoPointFieldData indexFieldData, GeoPointFieldMapper mapper,
                             String optimizeBbox) {
        this.lat = lat;
        this.lon = lon;
        this.distance = distance;
        this.geoDistance = geoDistance;
        this.indexFieldData = indexFieldData;

        this.fixedSourceDistance = geoDistance.fixedSourceDistance(lat, lon, DistanceUnit.MILES);
        if (optimizeBbox != null && !"none".equals(optimizeBbox)) {
            distanceBoundingCheck = GeoDistance.distanceBoundingCheck(lat, lon, distance, DistanceUnit.MILES);
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

    public double distance() {
        return distance;
    }

    public GeoDistance geoDistance() {
        return geoDistance;
    }

    public String fieldName() {
        return indexFieldData.getFieldNames().indexName();
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
        final GeoPointValues values = indexFieldData.load(context).getGeoPointValues();
        GeoDistanceDocSet distDocSet = new GeoDistanceDocSet(context.reader().maxDoc(), acceptedDocs, values, fixedSourceDistance, distanceBoundingCheck, distance);
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

        GeoDistanceFilter filter = (GeoDistanceFilter) o;

        if (Double.compare(filter.distance, distance) != 0) return false;
        if (Double.compare(filter.lat, lat) != 0) return false;
        if (Double.compare(filter.lon, lon) != 0) return false;
        if (!indexFieldData.getFieldNames().indexName().equals(filter.indexFieldData.getFieldNames().indexName()))
            return false;
        if (geoDistance != filter.geoDistance) return false;

        return true;
    }

    @Override
    public String toString() {
        return "GeoDistanceFilter(" + indexFieldData.getFieldNames().indexName() + ", " + geoDistance + ", " + distance + ", " + lat + ", " + lon + ")";
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = lat != +0.0d ? Double.doubleToLongBits(lat) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = lon != +0.0d ? Double.doubleToLongBits(lon) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = distance != +0.0d ? Double.doubleToLongBits(distance) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (geoDistance != null ? geoDistance.hashCode() : 0);
        result = 31 * result + indexFieldData.getFieldNames().indexName().hashCode();
        return result;
    }

    public static class GeoDistanceDocSet extends MatchDocIdSet {
        private final double distance; // in miles
        private final GeoPointValues values;
        private final GeoDistance.FixedSourceDistance fixedSourceDistance;
        private final GeoDistance.DistanceBoundingCheck distanceBoundingCheck;

        public GeoDistanceDocSet(int maxDoc, @Nullable Bits acceptDocs, GeoPointValues values, GeoDistance.FixedSourceDistance fixedSourceDistance, GeoDistance.DistanceBoundingCheck distanceBoundingCheck,
                                 double distance) {
            super(maxDoc, acceptDocs);
            this.values = values;
            this.fixedSourceDistance = fixedSourceDistance;
            this.distanceBoundingCheck = distanceBoundingCheck;
            this.distance = distance;
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
                        if (d < distance) {
                            return true;
                        }
                    }
                }
                return false;
            } else {
                GeoPoint point = values.getValue(doc);
                if (distanceBoundingCheck.isWithin(point.lat(), point.lon())) {
                    double d = fixedSourceDistance.calculate(point.lat(), point.lon());
                    return d < distance;
                }
            }
            return false;
        }
    }
=======
package com.wifislam.sample.data;

/**
 * 
 * 
 * @author Giovanni Soldi
 * 
 */
public class Rectangle2D {

	private final double startPointX;
	private final double endPointX;
	private final double startPointY;
	private final double endPointY;
	public int countPointsInside;
	public double meanX;
	public double meanY;
	public double sumX;
	public double sumY;

	private static Rectangle2D intervalWithBiggerNumberOfPoints;

	/**
	 * Constructor
	 * 
	 * @param startPointX
	 *            the start point of the rectangle on the x-axis
	 * @param endPointX
	 *            the end point of the rectangle on the x-axis
	 * @param startPointY
	 *            the start point of the rectangle on the y-axis
	 * @param endPointY
	 *            the end point of the rectangle on the y-axis
	 */
	public Rectangle2D(double startPointX, double endPointX,
			double startPointY, double endPointY) {
		super();
		this.startPointX = startPointX;
		this.endPointX = endPointX;
		this.startPointY = startPointY;
		this.endPointY = endPointY;
	}

	/**
	 * 
	 * @param point
	 * @return true if the point is inside the rectangle, false if the point is
	 *         outside
	 */
	public boolean isPointInTheInterval(Coordinates point) {
		return (point.getX() <= endPointX && point.getX() >= startPointX
				&& point.getY() <= endPointY && point.getY() >= startPointY);
	}

	/**
	 * 
	 * @return the estimated mean point in the rectangle
	 */
	public Coordinates calculateMeanPoint() {
		meanX = sumX / countPointsInside;
		meanY = sumY / countPointsInside;
		return new Coordinates(meanX, meanY);
	}

	/**
	 * 
	 * @return the interval that contains the biggest number of points
	 */
	public static Rectangle2D getIntervalWithBiggerNumberOfPoints() {
		return intervalWithBiggerNumberOfPoints;
	}

	/**
	 * 
	 * @param intervalWithBiggerNumberOfPoints
	 */
	public static void setIntervalWithBiggerNumberOfPoints(
			Rectangle2D intervalWithBiggerNumberOfPoints) {
		Rectangle2D.intervalWithBiggerNumberOfPoints = intervalWithBiggerNumberOfPoints;
	}

	@Override
	public String toString() {
		return "Interval2D [startPointX=" + startPointX + ", endPointX="
				+ endPointX + ", startPointY=" + startPointY + ", endPointY="
				+ endPointY + ", countPointsInside=" + countPointsInside
				+ ", meanX=" + meanX + ", meanY=" + meanY + ", sumX=" + sumX
				+ ", sumY=" + sumY + "]";
	}

>>>>>>> 76aa07461566a5976980e6696204781271955163
}

