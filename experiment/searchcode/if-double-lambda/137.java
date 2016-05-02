package org.zrli.geogame.api.util;

import org.zrli.geogame.api.GeoPoint;

/**
 * Utilities for calculating distance between, and otherwise working with,
 * {@link GeoPoint}s.
 * <p>
 * Formulas taken from http://www.movable-type.co.uk/scripts/latlong.html
 */
public class GeoPointUtil {

  private static final int RADIUS_OF_EARTH_METERS = 6371010;

  public static double distanceMeters(GeoPoint a, GeoPoint b) {
    return distanceMetersVincenty(a, b);
  }

  /**
   * Distance computation using the spherical law of cosines, assumes a
   * spherical earth, is computationally cheap.
   */
  private static double distanceMetersSpherical(GeoPoint a, GeoPoint b) {
    // Using spherical law of cosines.
    double lat1R = Math.toRadians(a.getLatitudeE6() / 1E6);
    double lat2R = Math.toRadians(b.getLatitudeE6() / 1E6);
    double lon1R = Math.toRadians(a.getLongitudeE6() / 1E6);
    double lon2R = Math.toRadians(b.getLongitudeE6() / 1E6);
    return Math.acos(Math.sin(lat1R) * Math.sin(lat2R) +
        Math.cos(lat1R) * Math.cos(lat2R) +
        Math.cos(lon2R - lon1R)) * RADIUS_OF_EARTH_METERS;
  }

  /**
   * Distance computation using a computationally-expensive iterative solution,
   * which models the ellipsoidal shape of the globe.
   * <p>
   * Taken from http://www.movable-type.co.uk/scripts/latlong-vincenty.html
   */
  private static double distanceMetersVincenty(GeoPoint p1, GeoPoint p2) {
    /*
     * Vincenty Inverse Solution of Geodesics on the Ellipsoid (c) Chris
     * Veness 2002-2010
     *
     * from: Vincenty inverse formula - T Vincenty, "Direct and Inverse
     *       Solutions of Geodesics on the Ellipsoid with application of nested
     *       equations", Survey Review, vol XXII no 176, 1975
     *       http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
     */

    double a = 6378137, b = 6356752.314245;
    double f = 1/298.257223563;  // WGS-84 ellipsoid params

    double lat1 = Math.toRadians(p1.getLatitudeE6() / 1E6);
    double lat2 = Math.toRadians(p2.getLatitudeE6() / 1E6);
    double lon1 = Math.toRadians(p1.getLongitudeE6() / 1E6);
    double lon2 = Math.toRadians(p2.getLongitudeE6() / 1E6);

    double L = (lon2-lon1);
    double U1 = Math.atan((1-f) * Math.tan(lat1));
    double U2 = Math.atan((1-f) * Math.tan(lat2));
    double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
    double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

    double lambda = L, lambdaP, cosSqAlpha, sinSigma, cos2SigmaM, cosSigma, sigma;
    int iterLimit = 100;
    do {
      double sinLambda = Math.sin(lambda), cosLambda = Math.cos(lambda);
      sinSigma = Math.sqrt((cosU2*sinLambda) * (cosU2*sinLambda) +
          (cosU1*sinU2-sinU1*cosU2*cosLambda) * (cosU1*sinU2-sinU1*cosU2*cosLambda));
      if (sinSigma==0) return 0;  // co-incident points
      cosSigma = sinU1*sinU2 + cosU1*cosU2*cosLambda;
      sigma = Math.atan2(sinSigma, cosSigma);
      double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
      cosSqAlpha = 1 - sinAlpha*sinAlpha;
      cos2SigmaM = 0;
      if (cosSqAlpha != 0) {
        cosSigma = 2*sinU1*sinU2/cosSqAlpha;
      }
      double C = f/16*cosSqAlpha*(4+f*(4-3*cosSqAlpha));
      lambdaP = lambda;
      lambda = L + (1-C) * f * sinAlpha *
        (sigma + C*sinSigma*(cos2SigmaM+C*cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)));
    } while (Math.abs(lambda-lambdaP) > 1e-12 && --iterLimit>0);

    if (iterLimit==0) {
      // Failed to converge.  Fall back on less accurate, faster method.
      return distanceMetersSpherical(p1, p2);
    }

    double uSq = cosSqAlpha * (a*a - b*b) / (b*b);
    double A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
    double B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
    double deltaSigma = B*sinSigma*(cos2SigmaM+B/4*(cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)-
      B/6*cos2SigmaM*(-3+4*sinSigma*sinSigma)*(-3+4*cos2SigmaM*cos2SigmaM)));
    double s = b*A*(sigma-deltaSigma);

    return s;
  }

  /**
   * Get a point displaced from an origin.
   *
   * @param origin the origin {@link GeoPoint}
   * @param brng the bearing, in radians.  0.0 is due north, 1/2*PI is due east.
   * @param distanceMeters the distance of displacement, in meters
   * @return the displaced point
   */
  public static GeoPoint displacedPoint(GeoPoint origin,
      double brng,
      double distanceMeters) {
    // Angular distance
    double ang = distanceMeters / RADIUS_OF_EARTH_METERS;

    double lat1R = Math.toRadians(origin.getLatitudeE6() / 1E6);
    double lat2R = Math.asin(
        Math.sin(lat1R) * Math.cos(ang) +
        Math.cos(lat1R) * Math.sin(ang) * Math.cos(brng));

    double lon1 = origin.getLongitudeE6() / 1E6;
    double lon1R = Math.toRadians(lon1);
    double lon2R = lon1R +
        Math.atan2(Math.sin(brng) * Math.sin(ang) * Math.cos(lat1R),
            Math.cos(ang) - Math.sin(lat1R) * Math.sin(lat2R));

    // Normalize lon2 to [-2pi, 2pi]
    lon2R = (lon2R+3*Math.PI) % (2*Math.PI) - Math.PI;

    int lat2E6 = (int) (Math.toDegrees(lat2R) * 1E6);
    int lon2E6 = (int) (Math.toDegrees(lon2R) * 1E6);
    return new GeoPoint(lat2E6, lon2E6);
  }
}

