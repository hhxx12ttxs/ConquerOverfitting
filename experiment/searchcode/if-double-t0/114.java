/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package memoria.utils.coordinate;

/**
 *
 * @author diego
 */
public class UTM2LatLon
  {
    static double easting;

    static double northing;

    static int zone;

    static String southernHemisphere = "ACDEFGHJKLM";

    protected static  String getHemisphere(String latZone)
    {
      String hemisphere = "N";
      if (southernHemisphere.indexOf(latZone) > -1)
      {
        hemisphere = "S";
      }
      return hemisphere;
    }

    public static double[] convertUTMToLatLong(String UTM)
    {
      double[] latlon = { 0.0, 0.0 };
      String[] utm = UTM.split(" ");
      zone = Integer.parseInt(utm[0]);
      String latZone = utm[1];
      easting = Double.parseDouble(utm[2].replace(",", "."));
      northing = Double.parseDouble(utm[3].replace(",", "."));
      String hemisphere = getHemisphere(latZone);
      double latitude = 0.0;
      double longitude = 0.0;

      if (hemisphere.equals("S"))
      {
        northing = 10000000 - northing;
      }
      setVariables();
      latitude = 180 * (phi1 - fact1 * (fact2 + fact3 + fact4)) / Math.PI;

      if (zone > 0)
      {
        zoneCM = 6 * zone - 183.0;
      }
      else
      {
        zoneCM = 3.0;

      }

      longitude = zoneCM - _a3;
      if (hemisphere.equals("S"))
      {
        latitude = -latitude;
      }

      latlon[0] = latitude;
      latlon[1] = longitude;
      return latlon;

    }

    protected static  void setVariables()
    {
      arc = northing / k0;
      mu = arc
          / (a * (1 - CoordinateConversion.POW(e, 2) / 4.0 - 3 *CoordinateConversion.POW(e, 4) / 64.0 - 5 *CoordinateConversion.POW(e, 6) / 256.0));

      ei = (1 -CoordinateConversion.POW((1 - e * e), (1 / 2.0)))
          / (1 +CoordinateConversion.POW((1 - e * e), (1 / 2.0)));

      ca = 3 * ei / 2 - 27 *CoordinateConversion.POW(ei, 3) / 32.0;

      cb = 21 *CoordinateConversion.POW(ei, 2) / 16 - 55 *CoordinateConversion.POW(ei, 4) / 32;
      cc = 151 *CoordinateConversion.POW(ei, 3) / 96;
      cd = 1097 *CoordinateConversion.POW(ei, 4) / 512;
      phi1 = mu + ca *CoordinateConversion.SIN(2 * mu) + cb *CoordinateConversion.SIN(4 * mu) + cc *CoordinateConversion.SIN(6 * mu) + cd
          *CoordinateConversion.SIN(8 * mu);

      n0 = a /CoordinateConversion.POW((1 -CoordinateConversion.POW((e *CoordinateConversion.SIN(phi1)), 2)), (1 / 2.0));

      r0 = a * (1 - e * e) /CoordinateConversion.POW((1 -CoordinateConversion.POW((e *CoordinateConversion.SIN(phi1)), 2)), (3 / 2.0));
      fact1 = n0 * CoordinateConversion.TAN(phi1) / r0;

      _a1 = 500000 - easting;
      dd0 = _a1 / (n0 * k0);
      fact2 = dd0 * dd0 / 2;

      t0 =CoordinateConversion.POW(CoordinateConversion.TAN(phi1), 2);
      Q0 = e1sq *CoordinateConversion.POW(CoordinateConversion.COS(phi1), 2);
      fact3 = (5 + 3 * t0 + 10 * Q0 - 4 * Q0 * Q0 - 9 * e1sq) *CoordinateConversion.POW(dd0, 4)
          / 24;

      fact4 = (61 + 90 * t0 + 298 * Q0 + 45 * t0 * t0 - 252 * e1sq - 3 * Q0
          * Q0)
          *CoordinateConversion.POW(dd0, 6) / 720;

      //
      lof1 = _a1 / (n0 * k0);
      lof2 = (1 + 2 * t0 + Q0) *CoordinateConversion.POW(dd0, 3) / 6.0;
      lof3 = (5 - 2 * Q0 + 28 * t0 - 3 *CoordinateConversion.POW(Q0, 2) + 8 * e1sq + 24 *CoordinateConversion.POW(t0, 2))
          *CoordinateConversion.POW(dd0, 5) / 120;
      _a2 = (lof1 - lof2 + lof3) /CoordinateConversion.COS(phi1);
      _a3 = _a2 * 180 / Math.PI;

    }

    static double arc;

    static double mu;

    static double ei;

    static double ca;

    static double cb;

    static double cc;

    static double cd;

    static double n0;

    static double r0;

    static double _a1;

    static double dd0;

    static double t0;

    static double Q0;

    static double lof1;

    static double lof2;

    static double lof3;

    static double _a2;

    static double phi1;

    static double fact1;

    static double fact2;

    static double fact3;

    static double fact4;

    static double zoneCM;

    static double _a3;

    static double b = 6356752.314;

    static double a = 6378137;

    static double e = 0.081819191;

    static double e1sq = 0.006739497;

    static double k0 = 0.9996;

  }

