<<<<<<< HEAD
package cgeo.geocaching.geopoint;

import cgeo.geocaching.ICoordinates;

import java.util.Locale;
import java.util.Set;



public class Viewport {

    public final Geopoint center;
    public final Geopoint bottomLeft;
    public final Geopoint topRight;

    public Viewport(final ICoordinates point1, final ICoordinates point2) {
        final Geopoint gp1 = point1.getCoords();
        final Geopoint gp2 = point2.getCoords();
        this.bottomLeft = new Geopoint(Math.min(gp1.getLatitude(), gp2.getLatitude()),
                Math.min(gp1.getLongitude(), gp2.getLongitude()));
        this.topRight = new Geopoint(Math.max(gp1.getLatitude(), gp2.getLatitude()),
                Math.max(gp1.getLongitude(), gp2.getLongitude()));
        this.center = new Geopoint((gp1.getLatitude() + gp2.getLatitude()) / 2,
                (gp1.getLongitude() + gp2.getLongitude()) / 2);
    }

    public Viewport(final ICoordinates center, final double latSpan, final double lonSpan) {
        this.center = center.getCoords();
        final double centerLat = this.center.getLatitude();
        final double centerLon = this.center.getLongitude();
        final double latHalfSpan = Math.abs(latSpan) / 2;
        final double lonHalfSpan = Math.abs(lonSpan) / 2;
        bottomLeft = new Geopoint(centerLat - latHalfSpan, centerLon - lonHalfSpan);
        topRight = new Geopoint(centerLat + latHalfSpan, centerLon + lonHalfSpan);
    }

    public double getLatitudeMin() {
        return bottomLeft.getLatitude();
    }

    public double getLatitudeMax() {
        return topRight.getLatitude();
    }

    public double getLongitudeMin() {
        return bottomLeft.getLongitude();
    }

    public double getLongitudeMax() {
        return topRight.getLongitude();
    }

    public Geopoint getCenter() {
        return center;
    }

    public double getLatitudeSpan() {
        return getLatitudeMax() - getLatitudeMin();
    }

    public double getLongitudeSpan() {
        return getLongitudeMax() - getLongitudeMin();
    }

    /**
     * Check whether a point is contained in this viewport.
     *
     * @param point
     *            the coordinates to check
     * @return true if the point is contained in this viewport, false otherwise or if the point contains no coordinates
     */
    public boolean contains(final ICoordinates point) {
        final Geopoint coords = point.getCoords();
        return coords != null
                && coords.getLongitudeE6() >= bottomLeft.getLongitudeE6()
                && coords.getLongitudeE6() <= topRight.getLongitudeE6()
                && coords.getLatitudeE6() >= bottomLeft.getLatitudeE6()
                && coords.getLatitudeE6() <= topRight.getLatitudeE6();
    }

    @Override
    public String toString() {
        return "(" + bottomLeft.toString() + "," + topRight.toString() + ")";
    }

    /**
     * Check whether another viewport is fully included into the current one.
     *
     * @param vp
     *            the other viewport
     * @return true if the vp is fully included into this one, false otherwise
     */
    public boolean includes(final Viewport vp) {
        return contains(vp.bottomLeft) && contains(vp.topRight);
    }

    /**
     * Return the "where" part of the string appropriate for a SQL query.
     *
     * @param dbTable
     *            the database table to use as prefix, or null if no prefix is required
     * @return the string without the "where" keyword
     */
    public String sqlWhere(final String dbTable) {
        final String prefix = dbTable == null ? "" : (dbTable + ".");
        return String.format((Locale) null,
                "%slatitude >= %s and %slatitude <= %s and %slongitude >= %s and %slongitude <= %s",
                prefix, getLatitudeMin(), prefix, getLatitudeMax(), prefix, getLongitudeMin(), prefix, getLongitudeMax());
    }

    /**
     * Return a widened or shrunk viewport.
     *
     * @param factor
     *            multiplicative factor for the latitude and longitude span (> 1 to widen, < 1 to shrink)
     * @return a widened or shrunk viewport
     */
    public Viewport resize(final double factor) {
        return new Viewport(getCenter(), getLatitudeSpan() * factor, getLongitudeSpan() * factor);
    }

    /**
     * Return a viewport that contains the current viewport as well as another point.
     *
     * @param point
     *            the point we want in the viewport
     * @return either the same or an expanded viewport
     */
    public Viewport expand(final ICoordinates point) {
        if (contains(point)) {
            return this;
        }

        final Geopoint coords = point.getCoords();
        final double latitude = coords.getLatitude();
        final double longitude = coords.getLongitude();
        final double latMin = Math.min(getLatitudeMin(), latitude);
        final double latMax = Math.max(getLatitudeMax(), latitude);
        final double lonMin = Math.min(getLongitudeMin(), longitude);
        final double lonMax = Math.max(getLongitudeMax(), longitude);
        return new Viewport(new Geopoint(latMin, lonMin), new Geopoint(latMax, lonMax));
    }

    /**
     * Return the smallest viewport containing all the given points.
     *
     * @param points
     *            a set of points. Point with null coordinates (or null themselves) will be ignored
     * @return the smallest viewport containing the non-null coordinates, or null if no coordinates are non-null
     */
    static public Viewport containing(final Set<? extends ICoordinates> points) {
        Viewport viewport = null;
        for (final ICoordinates point : points) {
            if (point != null && point.getCoords() != null) {
                if (viewport == null) {
                    viewport = new Viewport(point, point);
                } else {
                    viewport = viewport.expand(point);
                }
            }
        }
        return viewport;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Viewport)) {
            return false;
        }
        final Viewport vp = (Viewport) other;
        return bottomLeft.equals(vp.bottomLeft) && topRight.equals(vp.topRight);
    }

    @Override
    public int hashCode() {
        return bottomLeft.hashCode() ^ topRight.hashCode();
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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.google.common.collect.Lists;
import org.apache.hadoop.conf.Configuration;
import org.apache.mahout.common.distance.DistanceMeasure;
import org.apache.mahout.common.kernel.IKernelProfile;
import org.apache.mahout.math.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeanShiftCanopyClusterer {
  
  private static final Logger log = LoggerFactory
      .getLogger(MeanShiftCanopyClusterer.class);
  
  private final double convergenceDelta;
  
  // the T1 distance threshold
  private final double t1;
  
  // the T2 distance threshold
  private final double t2;
  
  // the distance measure
  private final DistanceMeasure measure;
  
  private final IKernelProfile kernelProfile;
  
  public MeanShiftCanopyClusterer(Configuration configuration) {
    try {
      measure = Class
          .forName(
              configuration.get(MeanShiftCanopyConfigKeys.DISTANCE_MEASURE_KEY))
          .asSubclass(DistanceMeasure.class).newInstance();
      measure.configure(configuration);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(e);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    } catch (InstantiationException e) {
      throw new IllegalStateException(e);
    }
    try {
      kernelProfile = Class
          .forName(
              configuration.get(MeanShiftCanopyConfigKeys.KERNEL_PROFILE_KEY))
          .asSubclass(IKernelProfile.class).newInstance();
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(e);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    } catch (InstantiationException e) {
      throw new IllegalStateException(e);
    }
    // nextCanopyId = 0; // never read?
    t1 = Double
        .parseDouble(configuration.get(MeanShiftCanopyConfigKeys.T1_KEY));
    t2 = Double
        .parseDouble(configuration.get(MeanShiftCanopyConfigKeys.T2_KEY));
    convergenceDelta = Double.parseDouble(configuration
        .get(MeanShiftCanopyConfigKeys.CLUSTER_CONVERGENCE_KEY));
  }
  
  public MeanShiftCanopyClusterer(DistanceMeasure aMeasure,
      IKernelProfile aKernelProfileDerivative, double aT1, double aT2,
      double aDelta) {
    // nextCanopyId = 100; // so canopyIds will sort properly // never read?
    measure = aMeasure;
    t1 = aT1;
    t2 = aT2;
    convergenceDelta = aDelta;
    kernelProfile = aKernelProfileDerivative;
  }
  
  public double getT1() {
    return t1;
  }
  
  public double getT2() {
    return t2;
  }
  
  /**
   * Merge the given canopy into the canopies list. If it touches any existing
   * canopy (norm<T1) then add the center of each to the other. If it covers any
   * other canopies (norm<T2), then merge the given canopy with the closest
   * covering canopy. If the given canopy does not cover any other canopies, add
   * it to the canopies list.
   * 
   * @param aCanopy
   *          a MeanShiftCanopy to be merged
   * @param canopies
   *          the List<Canopy> to be appended
   */
  public void mergeCanopy(MeanShiftCanopy aCanopy,
      Collection<MeanShiftCanopy> canopies) {
    MeanShiftCanopy closestCoveringCanopy = null;
    double closestNorm = Double.MAX_VALUE;
    for (MeanShiftCanopy canopy : canopies) {
      double norm = measure.distance(canopy.getCenter(), aCanopy.getCenter());
      double weight = kernelProfile.calculateDerivativeValue(norm, t1);
      if (weight > 0.0) {
        aCanopy.touch(canopy, weight);
      }
      if (norm < t2 && (closestCoveringCanopy == null || norm < closestNorm)) {
        closestNorm = norm;
        closestCoveringCanopy = canopy;
      }
    }
    if (closestCoveringCanopy == null) {
      canopies.add(aCanopy);
    } else {
      closestCoveringCanopy.merge(aCanopy);
    }
  }
  
  /**
   * Shift the center to the new centroid of the cluster
   * 
   * @param canopy
   *          the canopy to shift.
   * @return if the cluster is converged
   */
  public boolean shiftToMean(MeanShiftCanopy canopy) {
    canopy.observe(canopy.getCenter(), canopy.getBoundPoints().size());
    canopy.computeConvergence(measure, convergenceDelta);
    canopy.computeParameters();
    return canopy.isConverged();
  }
  
  /**
   * Return if the point is covered by this canopy
   * 
   * @param canopy
   *          a canopy.
   * @param point
   *          a Vector point
   * @return if the point is covered
   */
  boolean covers(MeanShiftCanopy canopy, Vector point) {
    return measure.distance(canopy.getCenter(), point) < t1;
  }
  
  /**
   * Return if the point is closely covered by the canopy
   * 
   * @param canopy
   *          a canopy.
   * @param point
   *          a Vector point
   * @return if the point is covered
   */
  public boolean closelyBound(MeanShiftCanopy canopy, Vector point) {
    return measure.distance(canopy.getCenter(), point) < t2;
  }
  
  /**
   * This is the reference mean-shift implementation. Given its inputs it
   * iterates over the points and clusters until their centers converge or until
   * the maximum number of iterations is exceeded.
   * 
   * @param points
   *          the input List<Vector> of points
   * @param measure
   *          the DistanceMeasure to use
   * @param numIter
   *          the maximum number of iterations
   */
  public static List<MeanShiftCanopy> clusterPoints(Iterable<Vector> points,
      DistanceMeasure measure, IKernelProfile aKernelProfileDerivative,
      double convergenceThreshold, double t1, double t2, int numIter) {
    MeanShiftCanopyClusterer clusterer = new MeanShiftCanopyClusterer(measure,
        aKernelProfileDerivative, t1, t2, convergenceThreshold);
    int nextCanopyId = 0;
    
    List<MeanShiftCanopy> canopies = Lists.newArrayList();
    for (Vector point : points) {
      clusterer.mergeCanopy(
          new MeanShiftCanopy(point, nextCanopyId++, measure), canopies);
    }
    List<MeanShiftCanopy> newCanopies = canopies;
    boolean[] converged = {false};
    for (int iter = 0; !converged[0] && iter < numIter; iter++) {
      newCanopies = clusterer.iterate(newCanopies, converged);
    }
    return newCanopies;
  }
  
  protected List<MeanShiftCanopy> iterate(Iterable<MeanShiftCanopy> canopies,
      boolean[] converged) {
    converged[0] = true;
    List<MeanShiftCanopy> migratedCanopies = Lists.newArrayList();
    for (MeanShiftCanopy canopy : canopies) {
      converged[0] = shiftToMean(canopy) && converged[0];
      mergeCanopy(canopy, migratedCanopies);
    }
    return migratedCanopies;
  }
  
  protected static void verifyNonOverlap(Iterable<MeanShiftCanopy> canopies) {
    Collection<Integer> coveredPoints = new HashSet<Integer>();
    // verify no overlap
    for (MeanShiftCanopy canopy : canopies) {
      for (int v : canopy.getBoundPoints().toList()) {
        if (coveredPoints.contains(v)) {
          log.info("Duplicate bound point: {} in Canopy: {}", v,
              canopy.asFormatString(null));
        } else {
          coveredPoints.add(v);
        }
      }
    }
  }
  
  protected static MeanShiftCanopy findCoveringCanopy(MeanShiftCanopy canopy,
      Iterable<MeanShiftCanopy> clusters) {
    // canopies use canopyIds assigned when input vectors are processed as
    // vectorIds too
    int vectorId = canopy.getId();
    for (MeanShiftCanopy msc : clusters) {
      for (int containedId : msc.getBoundPoints().toList()) {
        if (vectorId == containedId) {
          return msc;
        }
      }
    }
    return null;
  }
  
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

