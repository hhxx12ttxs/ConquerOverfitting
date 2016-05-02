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

package org.elasticsearch.common.geo;

import java.io.IOException;

import org.elasticsearch.ElasticSearchParseException;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentParser.Token;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper;

/**
 *
 */
public class GeoPoint {

    public static final String LATITUDE = GeoPointFieldMapper.Names.LAT;
    public static final String LONGITUDE = GeoPointFieldMapper.Names.LON;
    public static final String GEOHASH = GeoPointFieldMapper.Names.GEOHASH;

    private double lat;
    private double lon;

    public GeoPoint() {
    }

    public GeoPoint(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public GeoPoint reset(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        return this;
    }

    public GeoPoint resetLat(double lat) {
        this.lat = lat;
        return this;
    }

    public GeoPoint resetLon(double lon) {
        this.lon = lon;
        return this;
    }

    public GeoPoint resetFromString(String value) {
        int comma = value.indexOf(',');
        if (comma != -1) {
            lat = Double.parseDouble(value.substring(0, comma).trim());
            lon = Double.parseDouble(value.substring(comma + 1).trim());
        } else {
            resetFromGeoHash(value);
        }
        return this;
    }

    public GeoPoint resetFromGeoHash(String hash) {
        GeoHashUtils.decode(hash, this);
        return this;
    }

    void latlon(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public final double lat() {
        return this.lat;
    }

    public final double getLat() {
        return this.lat;
    }

    public final double lon() {
        return this.lon;
    }

    public final double getLon() {
        return this.lon;
    }

    public final String geohash() {
        return GeoHashUtils.encode(lat, lon);
    }

    public final String getGeohash() {
        return GeoHashUtils.encode(lat, lon);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeoPoint geoPoint = (GeoPoint) o;

        if (Double.compare(geoPoint.lat, lat) != 0) return false;
        if (Double.compare(geoPoint.lon, lon) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = lat != +0.0d ? Double.doubleToLongBits(lat) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = lon != +0.0d ? Double.doubleToLongBits(lon) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public String toString() {
        return "[" + lat + ", " + lon + "]";
    }

    /**
     * Parse a {@link GeoPoint} with a {@link XContentParser}:
     * 
     * @param parser {@link XContentParser} to parse the value from
     * @return new {@link GeoPoint} parsed from the parse
     * 
     * @throws IOException
     * @throws ElasticSearchParseException
     */
    public static GeoPoint parse(XContentParser parser) throws IOException, ElasticSearchParseException {
        return parse(parser, new GeoPoint());
    }

    /**
     * Parse a {@link GeoPoint} with a {@link XContentParser}. A geopoint has one of the following forms:
     * 
     * <ul>
     *     <li>Object: <pre>{&quot;lat&quot;: <i>&lt;latitude&gt;</i>, &quot;lon&quot;: <i>&lt;longitude&gt;</i>}</pre></li>
     *     <li>String: <pre>&quot;<i>&lt;latitude&gt;</i>,<i>&lt;longitude&gt;</i>&quot;</pre></li>
     *     <li>Geohash: <pre>&quot;<i>&lt;geohash&gt;</i>&quot;</pre></li>
     *     <li>Array: <pre>[<i>&lt;longitude&gt;</i>,<i>&lt;latitude&gt;</i>]</pre></li>
     * </ul>
     * 
     * @param parser {@link XContentParser} to parse the value from
     * @param point A {@link GeoPoint} that will be reset by the values parsed
     * @return new {@link GeoPoint} parsed from the parse
     * 
     * @throws IOException
     * @throws ElasticSearchParseException
     */
    public static GeoPoint parse(XContentParser parser, GeoPoint point) throws IOException, ElasticSearchParseException {
        if(parser.currentToken() == Token.START_OBJECT) {
            while(parser.nextToken() != Token.END_OBJECT) {
                if(parser.currentToken() == Token.FIELD_NAME) {
                    String field = parser.text();
                    if(LATITUDE.equals(field)) {
                        if(parser.nextToken() == Token.VALUE_NUMBER) {
                            point.resetLat(parser.doubleValue());
                        } else {
                            throw new ElasticSearchParseException("latitude must be a number");
                        }
                    } else if (LONGITUDE.equals(field)) {
                        if(parser.nextToken() == Token.VALUE_NUMBER) {
                            point.resetLon(parser.doubleValue());
                        } else {
                            throw new ElasticSearchParseException("latitude must be a number");
                        }
                    } else if (GEOHASH.equals(field)) {
                        if(parser.nextToken() == Token.VALUE_STRING) {
                            point.resetFromGeoHash(parser.text());
                        } else {
                            throw new ElasticSearchParseException("geohash must be a string");
                        }
                    } else {
                        throw new ElasticSearchParseException("field must be either '" + LATITUDE + "', '" + LONGITUDE + "' or '" + GEOHASH + "'");
                    }
                } else {
                    throw new ElasticSearchParseException("Token '"+parser.currentToken()+"' not allowed");
                }
            }
            return point;
        } else if(parser.currentToken() == Token.START_ARRAY) {
            int element = 0;
            while(parser.nextToken() != Token.END_ARRAY) {
                if(parser.currentToken() == Token.VALUE_NUMBER) {
                    element++;
                    if(element == 1) {
                        point.resetLon(parser.doubleValue());
                    } else if(element == 2) {
                        point.resetLat(parser.doubleValue());
                    } else {
                        throw new ElasticSearchParseException("only two values allowed");
                    }
                } else {
                    throw new ElasticSearchParseException("Numeric value expected");
                }
            }
            return point;
        } else if(parser.currentToken() == Token.VALUE_STRING) {
            String data = parser.text();
            int comma = data.indexOf(',');
            if(comma > 0) {
                double lat = Double.parseDouble(data.substring(0, comma).trim());
                double lon = Double.parseDouble(data.substring(comma + 1).trim());
                return point.reset(lat, lon);
            } else {
                point.resetFromGeoHash(data);
                return point;
            }
        } else {
            throw new ElasticSearchParseException("geo_point expected");
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

package org.apache.mahout.clustering.canopy;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.mahout.matrix.AbstractVector;
import org.apache.mahout.matrix.SparseVector;
import org.apache.mahout.matrix.Vector;
import org.apache.mahout.utils.DistanceMeasure;

import java.io.IOException;
import java.util.List;

/**
 * This class models a canopy as a center point, the number of points that are
 * contained within it according to the application of some distance metric, and
 * a point total which is the sum of all the points and is used to compute the
 * centroid when needed.
 */
public class Canopy {

  // keys used by Driver, Mapper, Combiner & Reducer
  public static final String DISTANCE_MEASURE_KEY = "org.apache.mahout.clustering.canopy.measure";

  public static final String T1_KEY = "org.apache.mahout.clustering.canopy.t1";

  public static final String T2_KEY = "org.apache.mahout.clustering.canopy.t2";

  public static final String CANOPY_PATH_KEY = "org.apache.mahout.clustering.canopy.path";

  // the next canopyId to be allocated
  private static int nextCanopyId = 0;

  // the T1 distance threshold
  private static double t1;

  // the T2 distance threshold
  private static double t2;

  // the distance measure
  private static DistanceMeasure measure;

  // this canopy's canopyId
  private final int canopyId;

  // the current center
  private Vector center = new SparseVector(0);

  // the number of points in the canopy
  private int numPoints = 0;

  // the total of all points added to the canopy
  private Vector pointTotal = null;

  /**
   * Create a new Canopy containing the given point
   * 
   * @param point
   *            a point in vector space
   */
  public Canopy(Vector point) {
    this.canopyId = nextCanopyId++;
    this.center = point;
    this.pointTotal = point.copy();
    this.numPoints = 1;
  }

  /**
   * Create a new Canopy containing the given point and canopyId
   * 
   * @param point
   *            a point in vector space
   * @param canopyId
   *            an int identifying the canopy local to this process only
   */
  public Canopy(Vector point, int canopyId) {
    this.canopyId = canopyId;
    this.center = point;
    this.pointTotal = point.copy();
    this.numPoints = 1;
  }

  /**
   * Configure the Canopy and its distance measure
   * 
   * @param job
   *            the JobConf for this job
   */
  public static void configure(JobConf job) {
    try {
      ClassLoader ccl = Thread.currentThread().getContextClassLoader();
      Class<?> cl = ccl.loadClass(job.get(DISTANCE_MEASURE_KEY));
      measure = (DistanceMeasure) cl.newInstance();
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
  }

  /**
   * Configure the Canopy for unit tests
   * 
   * @param aMeasure
   * @param aT1
   * @param aT2
   */
  public static void config(DistanceMeasure aMeasure, double aT1, double aT2) {
    nextCanopyId = 0;
    measure = aMeasure;
    t1 = aT1;
    t2 = aT2;
  }

  /**
   * This is the same algorithm as the reference but inverted to iterate over
   * existing canopies instead of the points. Because of this it does not need
   * to actually store the points, instead storing a total points vector and the
   * number of points. From this a centroid can be computed. <p/> This method is
   * used by the CanopyReducer.
   * 
   * @param point
   *            the point to be added
   * @param canopies
   *            the List<Canopy> to be appended
   */
  public static void addPointToCanopies(Vector point, List<Canopy> canopies) {
    boolean pointStronglyBound = false;
    for (Canopy canopy : canopies) {
      double dist = measure.distance(canopy.getCenter(), point);
      if (dist < t1)
        canopy.addPoint(point);
      pointStronglyBound = pointStronglyBound || (dist < t2);
    }
    if (!pointStronglyBound)
      canopies.add(new Canopy(point));
  }

  /**
   * This method is used by the CanopyMapper to perform canopy inclusion tests
   * and to emit the point and its covering canopies to the output. The
   * CanopyCombiner will then sum the canopy points and produce the centroids.
   * 
   * @param point
   *            the point to be added
   * @param canopies
   *            the List<Canopy> to be appended
   * @param collector
   *            an OutputCollector in which to emit the point
   */
  public static void emitPointToNewCanopies(Vector point,
      List<Canopy> canopies, OutputCollector<Text, Text> collector)
      throws IOException {
    boolean pointStronglyBound = false;
    for (Canopy canopy : canopies) {
      double dist = measure.distance(canopy.getCenter(), point);
      if (dist < t1)
        canopy.emitPoint(point, collector);
      pointStronglyBound = pointStronglyBound || (dist < t2);
    }
    if (!pointStronglyBound) {
      Canopy canopy = new Canopy(point);
      canopies.add(canopy);
      canopy.emitPoint(point, collector);
    }
  }

  /**
   * This method is used by the CanopyMapper to perform canopy inclusion tests
   * and to emit the point keyed by its covering canopies to the output. if the
   * point is not covered by any canopies (due to canopy centroid clustering),
   * emit the point to the closest covering canopy.
   * 
   * @param point
   *            the point to be added
   * @param canopies
   *            the List<Canopy> to be appended
   * @param writable
   *            the original Writable from the input, may include arbitrary
   *            payload information after the point [...]<payload>
   * @param collector
   *            an OutputCollector in which to emit the point
   */
  public static void emitPointToExistingCanopies(Vector point,
      List<Canopy> canopies, Text writable,
      OutputCollector<Text, Text> collector) throws IOException {
    double minDist = Double.MAX_VALUE;
    Canopy closest = null;
    boolean isCovered = false;
    for (Canopy canopy : canopies) {
      double dist = measure.distance(canopy.getCenter(), point);
      if (dist < t1) {
        isCovered = true;
        collector.collect(new Text(formatCanopy(canopy)), writable);
      } else if (dist < minDist) {
        minDist = dist;
        closest = canopy;
      }
    }
    // if the point is not contained in any canopies (due to canopy centroid
    // clustering), emit the point to the closest covering canopy.
    if (!isCovered)
      collector.collect(new Text(formatCanopy(closest)), writable);
  }

  /**
   * Format the canopy for output
   * 
   * @param canopy
   */
  public static String formatCanopy(Canopy canopy) {
    return "C" + canopy.canopyId + ": "
        + canopy.computeCentroid().asFormatString();
  }

  /**
   * Decodes and returns a Canopy from the formattedString
   * 
   * @param formattedString
   *            a String prouced by formatCanopy
   * @return a new Canopy
   */
  public static Canopy decodeCanopy(String formattedString) {
    int beginIndex = formattedString.indexOf('[');
    String id = formattedString.substring(0, beginIndex);
    String centroid = formattedString.substring(beginIndex);
    if (id.charAt(0) == 'C') {
      int canopyId = Integer.parseInt(formattedString.substring(1, beginIndex - 2));
      Vector canopyCentroid = AbstractVector.decodeVector(centroid);
      return new Canopy(canopyCentroid, canopyId);
    }
    return null;
  }

  /**
   * Add a point to the canopy
   * 
   * @param point
   *            some point to add
   */
  public void addPoint(Vector point) {
    numPoints++;
    for (int i = 0; i < point.cardinality(); i++)
      pointTotal.set(i, point.get(i) + pointTotal.get(i));
  }

  /**
   * Emit the point to the collector, keyed by the canopy's formatted
   * representation
   * 
   * @param point
   *            a point to emit.
   */
  public void emitPoint(Vector point, OutputCollector<Text, Text> collector)
      throws IOException {
    collector.collect(new Text(this.getIdentifier()), new Text(point
        .asFormatString()));
  }

  @Override
  public String toString() {
    return getIdentifier() + " - " + getCenter().asFormatString();
  }

  public String getIdentifier() {
    return "C" + canopyId;
  }

  public int getCanopyId() {
    return canopyId;
  }

  /**
   * Return the center point
   * 
   * @return the center of the Canopy
   */
  public Vector getCenter() {
    return center;
  }

  /**
   * Return the number of points in the Canopy
   * 
   * @return the number of points in the canopy.
   */
  public int getNumPoints() {
    return numPoints;
  }

  /**
   * Compute the centroid by averaging the pointTotals
   * 
   * @return a point which is the new centroid
   */
  public Vector computeCentroid() {
    Vector result = new SparseVector(pointTotal.cardinality());
    for (int i = 0; i < pointTotal.cardinality(); i++)
      result.set(i, pointTotal.get(i) / numPoints);
    return result;
  }

  /**
   * Return if the point is covered by this canopy
   * 
   * @param point
   *            a point
   * @return if the point is covered
   */
  public boolean covers(Vector point) {
    return measure.distance(center, point) < t1;
  }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

