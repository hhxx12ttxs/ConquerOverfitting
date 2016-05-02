/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package memoria.utils.coordinate;

import java.util.Hashtable;
import java.util.Map;

/**
 *
 * @author diego
 */
public class CoordinateConversion {

    public CoordinateConversion()
  {

  }

  public static double[] utm2LatLon(String UTM)
  {
    UTM2LatLon c = new UTM2LatLon();
    return c.convertUTMToLatLong(UTM);
  }

  public static String latLon2UTM(double latitude, double longitude)
  {
    LatLon2UTM c = new LatLon2UTM();
    return c.convertLatLonToUTM(latitude, longitude);

  }

  public static void validate(double latitude, double longitude)
  {
    if (latitude < -90.0 || latitude > 90.0 || longitude < -180.0
        || longitude >= 180.0)
    {
      throw new IllegalArgumentException(
          "Legal ranges: latitude [-90,90], longitude [-180,180).");
    }

  }

  public static String latLon2MGRUTM(double latitude, double longitude)
  {
    LatLon2MGRUTM c = new LatLon2MGRUTM();
    return c.convertLatLonToMGRUTM(latitude, longitude);

  }

  public static double[] mgrutm2LatLon(String MGRUTM)
  {
    MGRUTM2LatLon c = new MGRUTM2LatLon();
    return c.convertMGRUTMToLatLong(MGRUTM);
  }

  public static double degreeToRadian(double degree)
  {
    return degree * Math.PI / 180;
  }

  public static double radianToDegree(double radian)
  {
    return radian * 180 / Math.PI;
  }

  public static double POW(double a, double b)
  {
    return Math.pow(a, b);
  }

  public static double SIN(double value)
  {
    return Math.sin(value);
  }

  public static double COS(double value)
  {
    return Math.cos(value);
  }

  public static double TAN(double value)
  {
    return Math.tan(value);
  }

  
}

