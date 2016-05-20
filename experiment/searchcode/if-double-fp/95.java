
package org.geoforge.worldwind._tempo.utils;

import java.util.logging.Logger;
import org.geoforge.lang.util.logging.FileHandlerLogger;
import org.geoforge.worldwind._tempo.point.PointDms;
import org.geoforge.worldwind._tempo.point.PointUtm;
import org.geoforge.worldwind._tempo.projection.PrjAbs;
import org.geoforge.worldwind._tempo.projection.cylindrical.PrjCylAbs;

public class UtilsCylindricalConvert
{

   // ----
   // begin: instantiate logger for this class

   final static private Logger _LOGGER_ = Logger.getLogger(UtilsCylindricalConvert.class.getName());

   static
   {
      UtilsCylindricalConvert._LOGGER_.addHandler(FileHandlerLogger.s_getInstance());
   }
   // end: instantiate logger for this class
   // ----
   
   public UtilsCylindricalConvert()
   {
   }

   public static double getMeridionalArc_usgs_cor(double phi, double e_2, double a)
   {
      double e_4 = e_2 * e_2;
      double e_6 = e_4 * e_2;

      double sin_2phi = Math.sin(2D * phi);
      double sin_4phi = Math.sin(4D * phi);
      double sin_6phi = Math.sin(6D * phi);

      double M = (1D - e_2 / 4D - 3D * e_4 / 64D - 5D * e_6 / 256D) * phi
              + (-3D * e_2 / 8D - 3D * e_4 / 32D - 45D * e_6 / 1024D) * sin_2phi
              + (15D * e_4 / 256D + 45D * e_6 / 1024D) * sin_4phi
              + (-35D * e_6 / 3072D) * sin_6phi;

      return M * a;
   }

   public static double getMeridionalArc_precise(double phi, double e_2, double a)
   {

      double e_4 = e_2 * e_2;
      double e_6 = e_4 * e_2;
      double e_8 = e_6 * e_2;

      double sin_2phi = Math.sin(2D * phi);
      double sin_4phi = Math.sin(4D * phi);
      double sin_6phi = Math.sin(6D * phi);
      double sin_8phi = Math.sin(8D * phi);
      double sin_10phi = Math.sin(10D * phi);
      double sin_12phi = Math.sin(12D * phi);
      double sin_14phi = Math.sin(14D * phi);


      double M = (1D - e_2 / 4D - 3D * e_4 / 64D - 5D * e_6 / 256D - 175D * e_8 / 16384D) * phi
              + (-3D * e_2 / 8D - 3D * e_4 / 32D - 45D * e_6 / 1024D - 105D * e_8 / 4096D) * sin_2phi
              + (15D * e_4 / 256D + 45D * e_6 / 1024D + 525D * e_8 / 16384D) * sin_4phi
              + (-35D * e_6 / 3072D - 175D * e_8 / 12888D) * sin_6phi
              + (315 * e_8 / 131072D) * sin_8phi;

      return M * a;
   }

   public static PointUtm dmsToUtm(
           PointDms pnt,
           PrjCylAbs prj)
   {
      double phiRad = Math.toRadians(pnt.getLatitude());
      double lambdaDeg = pnt.getLongitude();
      double lambda0 = Math.toRadians(getLambda0(lambdaDeg));
      double lambdaRad = Math.toRadians(lambdaDeg);

      double a = prj.getEllipsoid().getDblRadius();
      double e_2 = prj.getEllipsoid().getDblSquareExcentricity();

      double sin_phi = Math.sin(phiRad);
      double tan_phi = Math.tan(phiRad);
      double sin_2_phi = sin_phi * sin_phi;
      double cos_phi = Math.cos(phiRad);
      double cos_2_phi = cos_phi * cos_phi;

      double nu = 1D / (Math.sqrt(1D - e_2 * sin_2_phi));
      double A = (lambdaRad - lambda0) * cos_phi;

      double A_2 = A * A;
      double A_3 = A_2 * A;
      double A_4 = A_3 * A;
      double A_5 = A_4 * A;
      double A_6 = A_5 * A;

      double T = tan_phi * tan_phi;
      double T_2 = T * T;
      double C = (e_2 / (1 - e_2)) * cos_2_phi;
      double k0 = 0.9996;
      double E0 = 500000;
      double N0 = getN0(phiRad);


      double s = getMeridionalArc_precise(phiRad, e_2, a) / a;

      double E = E0 + k0 * a * nu * (A + (1D - T + C) * (A_3 / 6D) + (5D - 18D * T + T_2) * (A_5 / 120D));

      double N = N0 + k0 * a * (s + nu * tan_phi * (A_2 / 2D + (5D - T + 9D * C + 4 * C * C) * (A_4 / 24D) + (61D - 58D * T + T_2) * (A_6 / 720D)));

      double prec = 100;


      E = Math.round(E * prec) / prec;
      N = Math.round(N * prec) / prec;

      String strZoneLetter = getZoneLetter(Math.toDegrees(phiRad));
      int zone = getZoneNumber(lambdaDeg);
      return new PointUtm(N, E, zone, strZoneLetter);
   }

   public static double getN0(PointUtm pnt)
   {
      String strZoneLetter = pnt.getZoneLetter();
      try
      {
         double dblHemisphere = getHemisphereFromLetter(strZoneLetter);
         if (dblHemisphere == -1)
            return 10000000;
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }

      return 0;

   }

   public static double getN0(double phi)
   {
      if (phi < 0)
         return 10000000;
      else
         return 0;
   }

   public static double getLambda0(double lambda)
   {
      if (lambda > 0)
         return (double) ((((int) lambda) / 6) * 6D + 3);

      return (double) ((((int) lambda) / 6) * 6D - 3);


   }

   public static double getLambda0(int intZone)
   {
      double lambda0 = -180 + intZone * 6 - 3;


      return (double) lambda0;
   }

   public static double getHemisphereFromLetter(String str) throws Exception
   {
      int intAscii = str.hashCode();

      if (intAscii >= 67 && intAscii <= 88) //between C and X
      {
         if (intAscii < 78)// (A to M)
            return -1;

         return 1;
      }

      if (intAscii >= 99 && intAscii <= 120) //between c and x
      {
         if (intAscii < 110)// (a to m)
            return -1;

         return 1;
      }

      String strMessage = "Bad UTM zone letter : " + str;

      UtilsCylindricalConvert._LOGGER_.severe(strMessage);
      throw new Exception(str);

   }

   public static int getZoneNumber(double lambda)
   {
      System.out.println("lambda = " + lambda);
      if (lambda > 0)
         return ((int) lambda) / ((int) 6) + 31;
      return ((int) lambda) / ((int) 6) + 30;

   }

   public static String getZoneLetter(double phi)
   {
      if (phi > 90 || phi < -90)
         return null;
      if (phi > 84)
         return "Z";
      if (phi >= 72)
         return "X";
      if (phi >= 64)
         return "W";
      if (phi >= 56)
         return "V";
      if (phi >= 48)
         return "U";
      if (phi >= 40)
         return "T";
      if (phi >= 32)
         return "S";
      if (phi >= 24)
         return "R";
      if (phi >= 16)
         return "Q";
      if (phi >= 8)
         return "P";
      if (phi >= 0)
         return "N";
      if (phi >= -8)
         return "M";
      if (phi >= -16)
         return "L";
      if (phi >= -24)
         return "K";
      if (phi >= -32)
         return "J";
      if (phi >= -40)
         return "H";
      if (phi >= -48)
         return "G";
      if (phi >= -56)
         return "F";
      if (phi >= -64)
         return "E";
      if (phi >= -72)
         return "D";
      if (phi >= -80)
         return "C";
      return "Z";

   }

   public static PointDms utmToDms_impl2(PointUtm pnt, PrjAbs prj) throws Exception
   {
      double x = pnt.getEasting();
      double y = pnt.getNorthing();

      x -= 500000;

      y -= UtilsCylindricalConvert.getN0(pnt);

      double a = prj.getEllipsoid().getDblRadius();
      double e_2 = prj.getEllipsoid().getDblSquareExcentricity();
      double e_4 = e_2 * e_2;
      double e_6 = e_4 * e_2;

      double k0 = 0.9996;

      double M = y / k0;

      double mu = M / (a * (1 - e_2 / 4 - 3 * e_4 / 64 - 5 * e_6 / 256));

      double sin_2mu = Math.sin(2 * mu);
      double sin_4mu = Math.sin(4 * mu);
      double sin_6mu = Math.sin(6 * mu);
      double sin_8mu = Math.sin(8 * mu);

      double e1 = (1 - Math.sqrt(1 - e_2)) / (1 + Math.sqrt(1 - e_2));
      double e1_2 = e1 * e1;
      double e1_3 = e1_2 * e1;
      double e1_4 = e1_3 * e1;



      double J1 = (3 * e1 / 2 - 27 * e1_3 / 32);
      double J2 = (21 * e1_2 / 16 - 55 * e1_4 / 32);
      double J3 = (151 * e1_3 / 96);
      double J4 = (1097 * e1_4 / 512);

      double fp = mu + J1 * sin_2mu + J2 * sin_4mu + J3 * sin_6mu + J4 * sin_8mu;

      double sin_fp = Math.sin(fp);
      double cos_fp = Math.cos(fp);
      double tan_fp = Math.tan(fp);
      double cos_2_fp = cos_fp * cos_fp;
      double sin_2_fp = sin_fp * sin_fp;

      double e_prime_2 = e_2 / (1 - e_2);
      double C1 = e_prime_2 * cos_2_fp;
      double C1_2 = C1 * C1;
      double T1 = tan_fp * tan_fp;
      double T1_2 = T1 * T1;

      double sqrt_exp = Math.sqrt(1 - e_2 * sin_2_fp);
      double sqrt_3_exp = sqrt_exp * sqrt_exp * sqrt_exp;

      double R1 = a * (1 - e_2) / (sqrt_3_exp);

      double N1 = a / sqrt_exp;


      double D = x / (N1 * k0);
      double D_2 = D * D;
      double D_3 = D_2 * D;
      double D_4 = D_2 * D_2;
      double D_5 = D_4 * D;
      double D_6 = D_4 * D_2;

      double Q1 = N1 * tan_fp / R1;
      double Q2 = D_2 / 2;
      double Q3 = (5 + 3 * T1 + 10 * C1 * 4 * C1_2 - 9 * e_prime_2) * D_4 / 24;
      double Q4 = (61 + 90 * T1 + 298 * C1 + 45 * T1_2 - 3 * C1_2 - 252 * e_prime_2) * D_6 / 720;

      double lat = fp - Q1 * (Q2 - Q3 + Q4);

      int intZone = pnt.getZoneNumber();
      double long0 = Math.toRadians(getLambda0(intZone));

      double Q5 = D;
      double Q6 = (1 + 2 * T1 + C1) * D_3 / 6;
      double Q7 = (5 - 2 * C1 + 28 * T1 - 3 * C1_2 + 8 * e_prime_2 + 24 * T1_2) * D_5 / 120;

      double lon = long0 + (Q5 - Q6 + Q7) / cos_fp;


      lat = Math.toDegrees(lat);
      lon = Math.toDegrees(lon);

      double prec = 0;
      if(Math.abs(lat) < 85)
         prec = 1000000D;
      else if(Math.abs(lat) <= 89)
         prec = 100000D;
      else
         prec = 10000D;

      return new PointDms(
              Math.round(lon * prec) / prec,
              Math.round(lat * prec) / prec);

   }

   public static PointDms utmToDms(PointUtm pnt, PrjAbs prj) throws Exception
   {
      return utmToDms_impl2(pnt, prj);
   }
}

