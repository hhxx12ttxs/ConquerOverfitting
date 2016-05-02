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

package org.elasticsearch.test.integration.search.geo;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.exception.InvalidShapeException;
import com.spatial4j.core.shape.Shape;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.spatial.query.UnsupportedSpatialOperation;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.geo.GeoHashUtils;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.GeoUtils;
import org.elasticsearch.common.geo.builders.MultiPolygonBuilder;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.test.integration.AbstractSharedClusterTest;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.FilterBuilders.geoBoundingBoxFilter;
import static org.elasticsearch.index.query.FilterBuilders.geoDistanceFilter;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.*;
import static org.hamcrest.Matchers.*;

/**
 *
 */
public class GeoFilterTests extends AbstractSharedClusterTest {

    private static boolean intersectSupport;
    private static boolean disjointSupport;
    private static boolean withinSupport;

    @BeforeClass
    public static void createNodes() throws Exception {
        intersectSupport = testRelationSupport(SpatialOperation.Intersects);
        disjointSupport = testRelationSupport(SpatialOperation.IsDisjointTo);
        withinSupport = testRelationSupport(SpatialOperation.IsWithin);
    }
    
    @Override
    protected int numberOfNodes() {
        return 2;
    }

    private static byte[] unZipData(String path) throws IOException {
        InputStream is = Streams.class.getResourceAsStream(path);
        if (is == null) {
            throw new FileNotFoundException("Resource [" + path + "] not found in classpath");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPInputStream in = new GZIPInputStream(is);
        Streams.copy(in, out);

        is.close();
        out.close();

        return out.toByteArray();
    }

    @Test
    public void testShapeBuilders() {

        try {
            // self intersection polygon
            ShapeBuilder.newPolygon()
                .point(-10, -10)
                .point(10, 10)
                .point(-10, 10)
                .point(10, -10)
                .close().build();
            assert false : "Self intersection not detected";
        } catch (InvalidShapeException e) {
        }

        // polygon with hole
        ShapeBuilder.newPolygon()
            .point(-10, -10).point(-10, 10).point(10, 10).point(10, -10)
            .hole()
                .point(-5, -5).point(-5, 5).point(5, 5).point(5, -5)
                .close().close().build();

        try {
            // polygon with overlapping hole
            ShapeBuilder.newPolygon()
                .point(-10, -10).point(-10, 10).point(10, 10).point(10, -10)
                .hole()
                    .point(-5, -5).point(-5, 11).point(5, 11).point(5, -5)
                    .close().close().build();

            assert false : "Self intersection not detected";
        } catch (InvalidShapeException e) {
        }

        try {
            // polygon with intersection holes
            ShapeBuilder.newPolygon()
                .point(-10, -10).point(-10, 10).point(10, 10).point(10, -10)
                .hole()
                    .point(-5, -5).point(-5, 5).point(5, 5).point(5, -5)
                    .close()
                    .hole()
                    .point(-5, -6).point(5, -6).point(5, -4).point(-5, -4)
                    .close()
                    .close().build();
            assert false : "Intersection of holes not detected";
        } catch (InvalidShapeException e) {
        }

        try {
            // Common line in polygon
            ShapeBuilder.newPolygon()
                .point(-10, -10)
                .point(-10, 10)
                .point(-5, 10)
                .point(-5, -5)
                .point(-5, 20)
                .point(10, 20)
                .point(10, -10)
                .close().build();
            assert false : "Self intersection not detected";
        } catch (InvalidShapeException e) {
        }

// Not specified
//        try {
//            // two overlapping polygons within a multipolygon
//            ShapeBuilder.newMultiPolygon()
//                .polygon()
//                    .point(-10, -10)
//                    .point(-10, 10)
//                    .point(10, 10)
//                    .point(10, -10)
//                .close()
//                .polygon()
//                    .point(-5, -5).point(-5, 5).point(5, 5).point(5, -5)
//                .close().build();
//            assert false : "Polygon intersection not detected";
//        } catch (InvalidShapeException e) {}

        // Multipolygon: polygon with hole and polygon within the whole
        ShapeBuilder.newMultiPolygon()
            .polygon()
                .point(-10, -10).point(-10, 10).point(10, 10).point(10, -10)
                .hole()
                .point(-5, -5).point(-5, 5).point(5, 5).point(5, -5)
                .close()
                .close()
                .polygon()
                .point(-4, -4).point(-4, 4).point(4, 4).point(4, -4)
                .close()
                .build();

// Not supported
//        try {
//            // Multipolygon: polygon with hole and polygon within the hole but overlapping
//            ShapeBuilder.newMultiPolygon()
//                .polygon()
//                    .point(-10, -10).point(-10, 10).point(10, 10).point(10, -10)
//                    .hole()
//                        .point(-5, -5).point(-5, 5).point(5, 5).point(5, -5)
//                    .close()
//                .close()
//                .polygon()
//                    .point(-4, -4).point(-4, 6).point(4, 6).point(4, -4)
//                .close()
//                .build();
//            assert false : "Polygon intersection not detected";
//        } catch (InvalidShapeException e) {}

    }

    @Test
    public void testShapeRelations() throws Exception {

        assert intersectSupport : "Intersect relation is not supported";
        assert disjointSupport : "Disjoint relation is not supported";
        assert withinSupport : "within relation is not supported";


        String mapping = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("polygon")
                .startObject("properties")
                .startObject("area")
                .field("type", "geo_shape")
                .field("tree", "geohash")
                .field("store", true)
                .endObject()
                .endObject()
                .endObject()
                .endObject().string();

        CreateIndexRequestBuilder mappingRequest = client().admin().indices().prepareCreate("shapes").addMapping("polygon", mapping);
        mappingRequest.execute().actionGet();
        client().admin().cluster().prepareHealth().setWaitForEvents(Priority.LANGUID).setWaitForGreenStatus().execute().actionGet();

        // Create a multipolygon with two polygons. The first is an rectangle of size 10x10
        // with a hole of size 5x5 equidistant from all sides. This hole in turn contains
        // the second polygon of size 4x4 equidistant from all sites
        MultiPolygonBuilder polygon = ShapeBuilder.newMultiPolygon()
        .polygon()
            .point(-10, -10).point(-10, 10).point(10, 10).point(10, -10)
            .hole()
                .point(-5, -5).point(-5, 5).point(5, 5).point(5, -5)
                .close()
                .close()
                .polygon()
                .point(-4, -4).point(-4, 4).point(4, 4).point(4, -4)
                .close();

        BytesReference data = jsonBuilder().startObject().field("area", polygon).endObject().bytes();
        
        client().prepareIndex("shapes", "polygon", "1").setSource(data).execute().actionGet();
        client().admin().indices().prepareRefresh().execute().actionGet();

        // Point in polygon
        SearchResponse result = client().prepareSearch()
                .setQuery(matchAllQuery())
                .setFilter(FilterBuilders.geoIntersectionFilter("area", ShapeBuilder.newPoint(3, 3)))
                .execute().actionGet();
        assertHitCount(result, 1);
        assertFirstHit(result, hasId("1"));

        // Point in polygon hole
        result = client().prepareSearch()
                .setQuery(matchAllQuery())
                .setFilter(FilterBuilders.geoIntersectionFilter("area", ShapeBuilder.newPoint(4.5, 4.5)))
                .execute().actionGet();
        assertHitCount(result, 0);

        // by definition the border of a polygon belongs to the inner
        // so the border of a polygons hole also belongs to the inner
        // of the polygon NOT the hole

        // Point on polygon border
        result = client().prepareSearch()
                .setQuery(matchAllQuery())
                .setFilter(FilterBuilders.geoIntersectionFilter("area", ShapeBuilder.newPoint(10.0, 5.0)))
                .execute().actionGet();
        assertHitCount(result, 1);
        assertFirstHit(result, hasId("1"));

        // Point on hole border
        result = client().prepareSearch()
                .setQuery(matchAllQuery())
                .setFilter(FilterBuilders.geoIntersectionFilter("area", ShapeBuilder.newPoint(5.0, 2.0)))
                .execute().actionGet();
        assertHitCount(result, 1);
        assertFirstHit(result, hasId("1"));

        if (disjointSupport) {
            // Point not in polygon
            result = client().prepareSearch()
                    .setQuery(matchAllQuery())
                    .setFilter(FilterBuilders.geoDisjointFilter("area", ShapeBuilder.newPoint(3, 3)))
                    .execute().actionGet();
            assertHitCount(result, 0);

            // Point in polygon hole
            result = client().prepareSearch()
                    .setQuery(matchAllQuery())
                    .setFilter(FilterBuilders.geoDisjointFilter("area", ShapeBuilder.newPoint(4.5, 4.5)))
                    .execute().actionGet();
            assertHitCount(result, 1);
            assertFirstHit(result, hasId("1"));
        }

        // Create a polygon that fills the empty area of the polygon defined above
        PolygonBuilder inverse = ShapeBuilder.newPolygon()
            .point(-5, -5).point(-5, 5).point(5, 5).point(5, -5)
            .hole()
                .point(-4, -4).point(-4, 4).point(4, 4).point(4, -4)
                .close()
                .close();

        data = jsonBuilder().startObject().field("area", inverse).endObject().bytes();
        client().prepareIndex("shapes", "polygon", "2").setSource(data).execute().actionGet();
        client().admin().indices().prepareRefresh().execute().actionGet();

        // re-check point on polygon hole
        result = client().prepareSearch()
                .setQuery(matchAllQuery())
                .setFilter(FilterBuilders.geoIntersectionFilter("area", ShapeBuilder.newPoint(4.5, 4.5)))
                .execute().actionGet();
        assertHitCount(result, 1);
        assertFirstHit(result, hasId("2"));

        // Create Polygon with hole and common edge
        PolygonBuilder builder = ShapeBuilder.newPolygon()
                .point(-10, -10).point(-10, 10).point(10, 10).point(10, -10)
                .hole()
                .point(-5, -5).point(-5, 5).point(10, 5).point(10, -5)
                .close()
                .close();

        if (withinSupport) {
            // Polygon WithIn Polygon
            builder = ShapeBuilder.newPolygon()
                    .point(-30, -30).point(-30, 30).point(30, 30).point(30, -30).close();

            result = client().prepareSearch()
                    .setQuery(matchAllQuery())
                    .setFilter(FilterBuilders.geoWithinFilter("area", builder))
                    .execute().actionGet();
            assertHitCount(result, 2);
        }

        // Create a polygon crossing longitude 180.
        builder = ShapeBuilder.newPolygon()
            .point(170, -10).point(190, -10).point(190, 10).point(170, 10)
            .close();

        data = jsonBuilder().startObject().field("area", builder).endObject().bytes();
        client().prepareIndex("shapes", "polygon", "1").setSource(data).execute().actionGet();
        client().admin().indices().prepareRefresh().execute().actionGet();

        // Create a polygon crossing longitude 180 with hole.
        builder = ShapeBuilder.newPolygon()
                .point(170, -10).point(190, -10).point(190, 10).point(170, 10)
                .hole().point(175, -5).point(185,-5).point(185,5).point(175,5).close()
                .close();

        data = jsonBuilder().startObject().field("area", builder).endObject().bytes();
        client().prepareIndex("shapes", "polygon", "1").setSource(data).execute().actionGet();
        client().admin().indices().prepareRefresh().execute().actionGet();

        result = client().prepareSearch()
                .setQuery(matchAllQuery())
                .setFilter(FilterBuilders.geoIntersectionFilter("area", ShapeBuilder.newPoint(174, -4)))
                .execute().actionGet();
        assertHitCount(result, 1);

        result = client().prepareSearch()
                .setQuery(matchAllQuery())
                .setFilter(FilterBuilders.geoIntersectionFilter("area", ShapeBuilder.newPoint(-174, -4)))
                .execute().actionGet();
        assertHitCount(result, 1);

        result = client().prepareSearch()
                .setQuery(matchAllQuery())
                .setFilter(FilterBuilders.geoIntersectionFilter("area", ShapeBuilder.newPoint(180, -4)))
                .execute().actionGet();
        assertHitCount(result, 0);

        result = client().prepareSearch()
                .setQuery(matchAllQuery())
                .setFilter(FilterBuilders.geoIntersectionFilter("area", ShapeBuilder.newPoint(180, -6)))
                .execute().actionGet();
        assertHitCount(result, 1);
    }

    @Test
    public void bulktest() throws Exception {
        byte[] bulkAction = unZipData("/org/elasticsearch/test/integration/search/geo/gzippedmap.json");

        String mapping = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("country")
                .startObject("properties")
                .startObject("pin")
                .field("type", "geo_point")
                .field("lat_lon", true)
                .field("store", true)
                .endObject()
                .startObject("location")
                .field("type", "geo_shape")
                .field("lat_lon", true)
                .field("store", true)
                .endObject()
                .endObject()
                .endObject()
                .endObject()
                .string();

        client().admin().indices().prepareCreate("countries").addMapping("country", mapping).execute().actionGet();
        BulkResponse bulk = client().prepareBulk().add(bulkAction, 0, bulkAction.length, false, null, null).execute().actionGet();

        for (BulkItemResponse item : bulk.getItems()) {
            assert !item.isFailed() : "unable to index data";
        }

        client().admin().indices().prepareRefresh().execute().actionGet();
        String key = "DE";

        SearchResponse searchResponse = client().prepareSearch()
                .setQuery(fieldQuery("_id", key))
                .execute().actionGet();

        assertHitCount(searchResponse, 1);

        for (SearchHit hit : searchResponse.getHits()) {
            assertThat(hit.getId(), equalTo(key));
        }

        SearchResponse world = client().prepareSearch().addField("pin").setQuery(
                filteredQuery(
                        matchAllQuery(),
                        geoBoundingBoxFilter("pin")
                                .topLeft(90, -179.99999)
                                .bottomRight(-90, 179.99999))
        ).execute().actionGet();

        assertHitCount(world, 53);

        SearchResponse distance = client().prepareSearch().addField("pin").setQuery(
                filteredQuery(
                        matchAllQuery(),
                        geoDistanceFilter("pin").distance("425km").point(51.11, 9.851)
                )).execute().actionGet();

        assertHitCount(distance, 5);
        GeoPoint point = new GeoPoint();
        for (SearchHit hit : distance.getHits()) {
            String name = hit.getId();
            point.resetFromString(hit.fields().get("pin").getValue().toString());
            double dist = distance(point.getLat(), point.getLon(), 51.11, 9.851);

            assertThat("distance to '" + name + "'", dist, lessThanOrEqualTo(425000d));
            assertThat(name, anyOf(equalTo("CZ"), equalTo("DE"), equalTo("BE"), equalTo("NL"), equalTo("LU")));
            if (key.equals(name)) {
                assertThat(dist, equalTo(0d));
            }
        }
    }

    @Test
    public void testGeoHashFilter() throws IOException {
        String geohash = randomhash(10);
        logger.info("Testing geohash boundingbox filter for [{}]", geohash);

        List<String> neighbors = GeoHashUtils.neighbors(geohash);
        List<String> parentNeighbors = GeoHashUtils.neighbors(geohash.substring(0, geohash.length() - 1));
       
        logger.info("Neighbors {}", neighbors);
        logger.info("Parent Neighbors {}", parentNeighbors);

        String mapping = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("location")
                .startObject("properties")
                .startObject("pin")
                .field("type", "geo_point")
                .field("geohash_prefix", true)
                .field("latlon", false)
                .endObject()
                .endObject()
                .endObject()
                .endObject()
                .string();

        ensureYellow();

        client().admin().indices().prepareCreate("locations").addMapping("location", mapping).execute().actionGet();

        // Index a pin
        client().prepareIndex("locations", "location", "1").setCreate(true).setSource("{\"pin\":\"" + geohash + "\"}").execute().actionGet();

        // index neighbors
        for (int i = 0; i < neighbors.size(); i++) {
            client().prepareIndex("locations", "location", "N" + i).setCreate(true).setSource("{\"pin\":\"" + neighbors.get(i) + "\"}").execute().actionGet();
        }

        // Index parent cell
        client().prepareIndex("locations", "location", "p").setCreate(true).setSource("{\"pin\":\"" + geohash.substring(0, geohash.length() - 1) + "\"}").execute().actionGet();

        // index neighbors
        for (int i = 0; i < parentNeighbors.size(); i++) {
            client().prepareIndex("locations", "location", "p" + i).setCreate(true).setSource("{\"pin\":\"" + parentNeighbors.get(i) + "\"}").execute().actionGet();
        }

        client().admin().indices().prepareRefresh("locations").execute().actionGet();

        // Result of this geohash search should contain the geohash only
        SearchResponse results1 = client().prepareSearch("locations").setQuery(QueryBuilders.matchAllQuery()).setFilter("{\"geohash_cell\": {\"pin\": \"" + geohash + "\", \"neighbors\": false}}").execute().actionGet();
        assertHitCount(results1, 1);

        // Result of the parent query should contain the parent it self, its neighbors, the child and all its neighbors
        SearchResponse results2 = client().prepareSearch("locations").setQuery(QueryBuilders.matchAllQuery()).setFilter("{\"geohash_cell\": {\"pin\": \"" + geohash.substring(0, geohash.length() - 1) + "\", \"neighbors\": true}}").execute().actionGet();
        assertHitCount(results2, 2 + neighbors.size() + parentNeighbors.size());

        // Testing point formats and precision
        GeoPoint point = GeoHashUtils.decode(geohash);
        int precision = geohash.length();

        logger.info("Testing lat/lon format");
        String pointTest1 = "{\"geohash_cell\": {\"pin\": {\"lat\": " + point.lat() + ",\"lon\": " + point.lon() + "},\"precision\": " + precision + ",\"neighbors\": true}}";
        SearchResponse results3 = client().prepareSearch("locations").setQuery(QueryBuilders.matchAllQuery()).setFilter(pointTest1).execute().actionGet();
        assertHitCount(results3, neighbors.size() + 1);

        logger.info("Testing String format");
        String pointTest2 = "{\"geohash_cell\": {\"pin\": \"" + point.lat() + "," + point.lon() + "\",\"precision\": " + precision + ",\"neighbors\": true}}";
        SearchResponse results4 = client().prepareSearch("locations").setQuery(QueryBuilders.matchAllQuery()).setFilter(pointTest2).execute().actionGet();
        assertHitCount(results4, neighbors.size() + 1);

        logger.info("Testing Array format");
        String pointTest3 = "{\"geohash_cell\": {\"pin\": [" + point.lon() + "," + point.lat() + "],\"precision\": " + precision + ",\"neighbors\": true}}";
        SearchResponse results5 = client().prepareSearch("locations").setQuery(QueryBuilders.matchAllQuery()).setFilter(pointTest3).execute().actionGet();
        assertHitCount(results5, neighbors.size() + 1);
    }

    @Test
    public void testNeighbors() {
        // Simple root case
        assertThat(GeoHashUtils.neighbors("7"), containsInAnyOrder("4", "5", "6", "d", "e", "h", "k", "s"));

        // Root cases (Outer cells)
        assertThat(GeoHashUtils.neighbors("0"), containsInAnyOrder("1", "2", "3", "p", "r"));
        assertThat(GeoHashUtils.neighbors("b"), containsInAnyOrder("8", "9", "c", "x", "z"));
        assertThat(GeoHashUtils.neighbors("p"), containsInAnyOrder("n", "q", "r", "0", "2"));
        assertThat(GeoHashUtils.neighbors("z"), containsInAnyOrder("8", "b", "w", "x", "y"));

        // Root crossing dateline
        assertThat(GeoHashUtils.neighbors("2"), containsInAnyOrder("0", "1", "3", "8", "9", "p", "r", "x"));
        assertThat(GeoHashUtils.neighbors("r"), containsInAnyOrder("0", "2", "8", "n", "p", "q", "w", "x"));

        // level1: simple case
        assertThat(GeoHashUtils.neighbors("dk"), containsInAnyOrder("d5", "d7", "de", "dh", "dj", "dm", "ds", "dt"));

        // Level1: crossing cells
        assertThat(GeoHashUtils.neighbors("d5"), containsInAnyOrder("d4", "d6", "d7", "dh", "dk", "9f", "9g", "9u"));
        assertThat(GeoHashUtils.neighbors("d0"), containsInAnyOrder("d1", "d2", "d3", "9b", "9c", "6p", "6r", "3z"));
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        return GeoUtils.EARTH_SEMI_MAJOR_AXIS * DistanceUtils.distHaversineRAD(
                DistanceUtils.toRadians(lat1),
                DistanceUtils.toRadians(lon1),
                DistanceUtils.toRadians(lat2),
                DistanceUtils.toRadians(lon2)
        );
    }

    protected static boolean testRelationSupport(SpatialOperation relation) {
        try {
            GeohashPrefixTree tree = new GeohashPrefixTree(SpatialContext.GEO, 3);
            RecursivePrefixTreeStrategy strategy = new RecursivePrefixTreeStrategy(tree, "area");
            Shape shape = SpatialContext.GEO.makePoint(0, 0);
            SpatialArgs args = new SpatialArgs(relation, shape);
            strategy.makeFilter(args);
            return true;
        } catch (UnsupportedSpatialOperation e) {
            return false;
        }
    }

    protected static String randomhash(int length) {
        return randomhash(getRandom(), length);
    }

    protected static String randomhash(Random random) {
        return randomhash(random, 2 + random.nextInt(10));
    }

    protected static String randomhash() {
        return randomhash(getRandom());
    }

    protected static String randomhash(Random random, int length) {
        final char[] BASE_32 = {
                '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'b', 'c', 'd', 'e', 'f', 'g',
                'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r',
                's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(BASE_32[random.nextInt(BASE_32.length)]);
        }

        return sb.toString();
    }
}

=======
/**
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

package org.apache.mahout.clustering.meanshift;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.mahout.matrix.CardinalityException;
import org.apache.mahout.matrix.DenseVector;
import org.apache.mahout.matrix.PlusFunction;
import org.apache.mahout.matrix.Vector;
import org.apache.mahout.utils.DistanceMeasure;
import org.apache.mahout.utils.EuclideanDistanceMeasure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class models a canopy as a center point, the number of points that are
 * contained within it according to the application of some distance metric, and
 * a point total which is the sum of all the points and is used to compute the
 * centroid when needed.
 */
public class MeanShiftCanopy {

  // keys used by Driver, Mapper, Combiner & Reducer
  public static final String DISTANCE_MEASURE_KEY = "org.apache.mahout.clustering.canopy.measure";

  public static final String T1_KEY = "org.apache.mahout.clustering.canopy.t1";

  public static final String T2_KEY = "org.apache.mahout.clustering.canopy.t2";

  public static final String CANOPY_PATH_KEY = "org.apache.mahout.clustering.canopy.path";

  public static final String CLUSTER_CONVERGENCE_KEY = "org.apache.mahout.clustering.canopy.convergence";

  private static double convergenceDelta = 0;

  // the next canopyId to be allocated
  private static int nextCanopyId = 0;

  // the T1 distance threshold
  private static double t1;

  // the T2 distance threshold
  private static double t2;

  // the distance measure
  private static DistanceMeasure measure;

  // this canopy's canopyId
  private int canopyId;

  // the current center
  private Vector center = null;

  // the number of points in the canopy
  private int numPoints = 0;

  // the total of all points added to the canopy
  private Vector pointTotal = null;

  private List<Vector> boundPoints = new ArrayList<Vector>();

  private boolean converged = false;

  /**
   * Configure the Canopy and its distance measure
   * 
   * @param job the JobConf for this job
   */
  public static void configure(JobConf job) {
    try {
      measure = Class.forName(job.get(DISTANCE_MEASURE_KEY)).asSubclass(DistanceMeasure.class).newInstance();
      measure.configure(job);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    }
    nextCanopyId = 0;
    t1 = Double.parseDouble(job.get(T1_KEY));
    t2 = Double.parseDouble(job.get(T2_KEY));
    convergenceDelta = Double.parseDouble(job.get(CLUSTER_CONVERGENCE_KEY));
  }

  /**
   * Configure the Canopy for unit tests
   * 
   * @param aMeasure
   * @param aT1
   * @param aT2
   * @param aDelta the convergence criteria
   */
  public static void config(DistanceMeasure aMeasure, double aT1, double aT2,
      double aDelta) {
    nextCanopyId = 100; // so canopyIds will sort properly
    measure = aMeasure;
    t1 = aT1;
    t2 = aT2;
    convergenceDelta = aDelta;
  }

  /**
   * Merge the given canopy into the canopies list. If it touches any existing
   * canopy (norm<T1) then add the center of each to the other. If it covers
   * any other canopies (norm<T2), then merge the given canopy with the closest
   * covering canopy. If the given canopy does not cover any other canopies, add
   * it to the canopies list.
   * 
   * @param aCanopy a MeanShiftCanopy to be merged
   * @param canopies the List<Canopy> to be appended
   */
  public static void mergeCanopy(MeanShiftCanopy aCanopy, List<MeanShiftCanopy> canopies) {
    MeanShiftCanopy closestCoveringCanopy = null;
    double closestNorm = Double.MAX_VALUE;
    for (MeanShiftCanopy canopy : canopies) {
      double norm = measure.distance(canopy.getCenter(), aCanopy.getCenter());
      if (norm < t1)
        aCanopy.touch(canopy);
      if (norm < t2)
        if (closestCoveringCanopy == null || norm < closestNorm) {
          closestNorm = norm;
          closestCoveringCanopy = canopy;
        }
    }
    if (closestCoveringCanopy == null)
      canopies.add(aCanopy);
    else
      closestCoveringCanopy.merge(aCanopy);
  }

  /**
   * This method is used by the CanopyMapper to perform canopy inclusion tests
   * and to emit the point and its covering canopies to the output. The
   * CanopyCombiner will then sum the canopy points and produce the centroids.
   * 
   * @param aCanopy a MeanShiftCanopy to be merged
   * @param canopies the List<Canopy> to be appended
   * @param collector an OutputCollector in which to emit the point
   */
  public static void mergeCanopy(MeanShiftCanopy aCanopy,
      List<MeanShiftCanopy> canopies,
      OutputCollector<Text, WritableComparable<?>> collector) throws IOException {
    MeanShiftCanopy closestCoveringCanopy = null;
    double closestNorm = 0;
    for (MeanShiftCanopy canopy : canopies) {
      double dist = measure.distance(canopy.getCenter(), aCanopy.getCenter());
      if (dist < t1)
        aCanopy.touch(collector, canopy);
      if (dist < t2)
        if (closestCoveringCanopy == null || dist < closestNorm) {
          closestCoveringCanopy = canopy;
          closestNorm = dist;
        }
    }
    if (closestCoveringCanopy == null) {
      canopies.add(aCanopy);
      aCanopy.emitCanopy(aCanopy, collector);
    } else
      closestCoveringCanopy.merge(aCanopy, collector);
  }

  /**
   * Format the canopy for output
   * 
   * @param canopy
   */
  public static String formatCanopy(MeanShiftCanopy canopy) {
    StringBuilder builder = new StringBuilder();
    builder.append(canopy.getIdentifier()).append(" - ").append(
        canopy.getCenter().asWritableComparable().toString()).append(": ");
    for (Vector bound : canopy.boundPoints)
      builder.append(bound.asWritableComparable().toString());
    return builder.toString();
  }

  /**
   * Decodes and returns a Canopy from the formattedString
   * 
   * @param formattedString a String produced by formatCanopy
   * @return a new Canopy
   */
  public static MeanShiftCanopy decodeCanopy(String formattedString) {
    int beginIndex = formattedString.indexOf('[');
    int endIndex = formattedString.indexOf(':', beginIndex);
    String id = formattedString.substring(0, beginIndex);
    String centroid = formattedString.substring(beginIndex, endIndex);
    String boundPoints = formattedString.substring(endIndex + 1).trim();
    char firstChar = id.charAt(0);
    boolean startsWithV = firstChar == 'V';
    if (firstChar == 'C' || startsWithV) {
      int canopyId = Integer.parseInt(formattedString.substring(1, beginIndex - 3));
      Vector canopyCentroid = DenseVector.decodeFormat(new Text(centroid));
      List<Vector> canopyBoundPoints = new ArrayList<Vector>();
      while (boundPoints.length() > 0) {
        int ix = boundPoints.indexOf(']');
        Vector v = DenseVector.decodeFormat(new Text(boundPoints.substring(0,
            ix + 1)));
        canopyBoundPoints.add(v);
        boundPoints = boundPoints.substring(ix + 1);
      }
      return new MeanShiftCanopy(canopyCentroid, canopyId, canopyBoundPoints,
          startsWithV);
    }
    return null;
  }

  /**
   * Create a new Canopy with the given canopyId
   * 
   * @param id
   */
  public MeanShiftCanopy(String id) {
    this.canopyId = Integer.parseInt(id.substring(1));
    this.center = null;
    this.pointTotal = null;
    this.numPoints = 0;
  }

  /**
   * Create a new Canopy containing the given point
   * 
   * @param point a Vector
   */
  public MeanShiftCanopy(Vector point) {
    this.canopyId = nextCanopyId++;
    this.center = point;
    this.pointTotal = point.copy();
    this.numPoints = 1;
    this.boundPoints.add(point);
  }

  /**
   * Create a new Canopy containing the given point, canopyId and bound points
   * 
   * @param point a Vector
   * @param canopyId an int identifying the canopy local to this process only
   * @param boundPoints a List<Vector> containing points bound to the canopy
   * @param converged true if the canopy has converged
   */
  MeanShiftCanopy(Vector point, int canopyId, List<Vector> boundPoints,
      boolean converged) {
    this.canopyId = canopyId;
    this.center = point;
    this.pointTotal = point.copy();
    this.numPoints = 1;
    this.boundPoints = boundPoints;
    this.converged = converged;
  }

  /**
   * Add a point to the canopy some number of times
   * 
   * @param point a Vector to add
   * @param nPoints the number of times to add the point
   * @throws CardinalityException if the cardinalities disagree
   */
  void addPoints(Vector point, int nPoints) {
    numPoints += nPoints;
    Vector subTotal = (nPoints == 1) ? point.copy() : point.times(nPoints);
    pointTotal = (pointTotal == null) ? subTotal : pointTotal.plus(subTotal);
  }

  /**
   * Return if the point is closely covered by this canopy
   * 
   * @param point a Vector point
   * @return if the point is covered
   */
  public boolean closelyBound(Vector point) {
    return measure.distance(center, point) < t2;
  }

  /**
   * Compute the bound centroid by averaging the bound points
   * 
   * @return a Vector which is the new bound centroid
   */
  public Vector computeBoundCentroid() {
    Vector result = new DenseVector(center.cardinality());
    for (Vector v : boundPoints)
      result.assign(v, new PlusFunction());
    return result.divide(boundPoints.size());
  }

  /**
   * Compute the centroid by normalizing the pointTotal
   * 
   * @return a Vector which is the new centroid
   */
  public Vector computeCentroid() {
    if (numPoints == 0)
      return center;
    else
      return pointTotal.divide(numPoints);
  }

  /**
   * Return if the point is covered by this canopy
   * 
   * @param point a Vector point
   * @return if the point is covered
   */
  boolean covers(Vector point) {
    return measure.distance(center, point) < t1;
  }

  /**
   * Emit the new canopy to the collector, keyed by the canopy's Id
   */
  void emitCanopy(MeanShiftCanopy canopy,
      OutputCollector<Text, WritableComparable<?>> collector) throws IOException {
    String identifier = this.getIdentifier();
    collector.collect(new Text(identifier),
        new Text("new " + canopy.toString()));
  }

  /**
   * Emit the canopy centroid to the collector, keyed by the canopy's Id, once
   * per bound point.
   * 
   * @param canopy a MeanShiftCanopy
   * @param collector the OutputCollector
   * @throws IOException if there is an IO problem with the collector
   */
  void emitCanopyCentroid(MeanShiftCanopy canopy,
      OutputCollector<Text, WritableComparable<?>> collector) throws IOException {
    collector.collect(new Text(this.getIdentifier()), new Text(canopy
        .computeCentroid().asWritableComparable().toString()
        + boundPoints.size()));
  }

  public List<Vector> getBoundPoints() {
    return boundPoints;
  }

  public int getCanopyId() {
    return canopyId;
  }

  /**
   * Return the center point
   * 
   * @return a Vector
   */
  public Vector getCenter() {
    return center;
  }

  public String getIdentifier() {
    return converged ? "V" + canopyId : "C" + canopyId;
  }

  /**
   * @return the number of points under the Canopy
   */
  public int getNumPoints() {
    return numPoints;
  }

  void init(MeanShiftCanopy canopy) {
    canopyId = canopy.canopyId;
    center = canopy.center;
    addPoints(center, 1);
    boundPoints.addAll(canopy.getBoundPoints());
  }

  public boolean isConverged() {
    return converged;
  }

  /**
   * The receiver overlaps the given canopy. Touch it and add my bound points to
   * it.
   * 
   * @param canopy an existing MeanShiftCanopy
   */
  void merge(MeanShiftCanopy canopy) {
    boundPoints.addAll(canopy.boundPoints);
  }

  /**
   * The receiver overlaps the given canopy. Touch it and add my bound points to
   * it.
   * 
   * @param canopy an existing MeanShiftCanopy
   */
  void merge(MeanShiftCanopy canopy,
      OutputCollector<Text, WritableComparable<?>> collector) throws IOException {
    collector.collect(new Text(getIdentifier()), new Text("merge "
        + canopy.toString()));
  }

  public boolean shiftToMean() {
    Vector centroid = computeCentroid();
    converged = new EuclideanDistanceMeasure().distance(centroid, center) < convergenceDelta;
    center = centroid;
    numPoints = 1;
    pointTotal = centroid.copy();
    return converged;
  }

  @Override
  public String toString() {
    return formatCanopy(this);
  }

  /**
   * The receiver touches the given canopy. Add respective centers.
   * 
   * @param canopy an existing MeanShiftCanopy
   */
  void touch(MeanShiftCanopy canopy) {
    canopy.addPoints(getCenter(), boundPoints.size());
    addPoints(canopy.center, canopy.boundPoints.size());
  }

  /**
   * The receiver touches the given canopy. Emit the respective centers.
   * 
   * @param collector
   * @param canopy
   * @throws IOException
   */
  void touch(OutputCollector<Text, WritableComparable<?>> collector,
      MeanShiftCanopy canopy) throws IOException {
    canopy.emitCanopyCentroid(this, collector);
    emitCanopyCentroid(canopy, collector);
  }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

