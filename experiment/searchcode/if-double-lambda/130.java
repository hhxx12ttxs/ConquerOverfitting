/*
 *  Copyright (C) 2011-2012 GeoForge Project
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.geoforge.worldwind._tempo.utils;

import org.geoforge.worldwind._tempo.ellipsoids.ElpAbs;
import org.geoforge.worldwind._tempo.maths.BigMath;
import org.geoforge.worldwind._tempo.point.PointDms;
import org.geoforge.worldwind._tempo.point.PointLambert;
import org.geoforge.worldwind._tempo.projection.conical.PrjConAbs;
import java.math.BigDecimal;

/**
 *
 * @author Amadeus.Sowerby
 *
 * email: Amadeus.Sowerby_AT_gmail.com
 * ... please remove "_AT_" from the above string to get the right email address
 */
public class UtilsConicalConvert
{

   public static PointLambert dmsToCon(PointDms dms, PrjConAbs prj)
   {
      prj.dblCalculate();
      double phi = Math.toRadians(dms.getLatitude());
      double sin_phi = Math.sin(phi);
      double lambda = Math.toRadians(dms.getLongitude());

      ElpAbs elp = prj.getEllipsoid();
      double a = elp.getDblRadius();
      double e = elp.getDblExcentricity();
      
      double lambda0 = prj.getDblLambda0();

      double x0 = prj.getDblX0(); 
      double y0 = prj.getDblY0();
      double n = prj.getDblN();
      double g = prj.getDblG();
      double r0 = prj.getDblR0();

      //Direct transfo
      double t = Math.tan(Math.PI / 4D - phi / 2D)
              / Math.pow(
              (1 - e * sin_phi) / (1 + e * sin_phi),
              e / 2D);

      double r = a * g * Math.pow(t, n);

      double theta = n * (lambda - lambda0);


      double x = x0 + r * Math.sin(theta);
      double y = y0 + r0 - r * Math.cos(theta);

      return new PointLambert(x, y);
   }

   public static PointDms conToDms(PointLambert lmb, PrjConAbs prj)
   {
      prj.dblCalculate();

      double x = lmb.getX();
      double y = lmb.getY();

      double a = prj.getEllipsoid().getDblRadius();
      double e = prj.getEllipsoid().getDblExcentricity();

      double lambda0 = prj.getDblLambda0();

      double x0 = prj.getDblX0(); 
      double y0 = prj.getDblY0();
      double n = prj.getDblN();
      double g = prj.getDblG();
      double r0 = prj.getDblR0();
      
      //reverse transfo

      
      double r = Math.sqrt((x - x0) * (x - x0) + (r0 - (y - y0)) * (r0 - (y - y0)));

      double t = Math.pow((r / (a * g)), (1D / n));

      double theta = Math.atan((x - x0) / (r0 - (y - y0)));

      double lambda = (theta / n) + lambda0;

           
      
      double phi_i = Math.PI / 2D - 2D * Math.atan(t);
      double phi_ip1 = 0;
      for (int i = 0; i < 10000; ++i)
      {
         phi_ip1 = Math.PI / 2D - 2D * Math.atan(
                 t * Math.pow(
                 (1 - e * Math.sin(phi_i))
                 / (1 + e * Math.sin(phi_i)),
                 e / 2D));
         phi_i = phi_ip1;

      }
      
      
      double lat = Math.toDegrees(lambda);
      double lon = Math.toDegrees(phi_ip1);
           
      double lon_expanded = lon*10000000000000D;
      double lat_expanded = lat*10000000000000D;

      double lon_rounded = Math.round(lon_expanded);
      double lat_rounded = Math.round(lat_expanded);
      
      return new PointDms(
           lat_rounded/10000000000000D,
           lon_rounded/10000000000000D);
      
   }

   public static PointLambert dmsToConBigPrecision(PointDms dms, PrjConAbs prj)
   {
      int scale = 18;
      BigMath.setScale(scale);
      BigDecimal phi = BigMath.degToRad(new BigDecimal((dms.getLatitude())), scale);
      BigDecimal sin_phi = BigMath.sin(phi, scale);

      BigDecimal lambda = BigMath.degToRad(new BigDecimal(dms.getLongitude()), scale);

      prj.bdlCalculate();
      ElpAbs elp = prj.getEllipsoid();

      BigDecimal a = elp.getBdlRadius();
      BigDecimal e = elp.getBdlExcentricity();

      
      BigDecimal lambda0 = prj.getBdlLambda0();
      BigDecimal x0 = prj.getBdlX0();
      BigDecimal y0 = prj.getBdlY0();
      BigDecimal n = prj.getBdlN();
      BigDecimal g = prj.getBdlG();
      BigDecimal r0 = prj.getBdlR0();
      
      
      BigDecimal e_x_sin_phi = e.multiply(sin_phi);

      
      BigDecimal num = BigMath.tan(
              BigMath.divide(BigMath.pi(scale), BigMath.NMB_4).subtract(BigMath.divide(phi, BigMath.NMB_2)),
              scale);
      
      BigDecimal den = BigMath.pow(
              BigMath.divide(
                  BigMath.NMB_1.subtract(e_x_sin_phi),
                  BigMath.NMB_1.add(e_x_sin_phi)),
              BigMath.divide(e, BigMath.NMB_2),
              scale);

            //Direct transfo
      BigDecimal t =
              BigMath.divide(
              num,
              den);
     
      BigDecimal r = a.multiply(g).multiply(BigMath.pow(t, n, scale));
      BigDecimal theta = n.multiply(lambda.subtract(lambda0));
      BigDecimal x = x0.add(r.multiply(BigMath.sin(theta, scale)));
      BigDecimal y = y0.add(r0).subtract(r.multiply(BigMath.cos(theta, scale)));

      
      /*/
      System.out.println("sin_phi :" + sin_phi);
      
      System.out.println("e_x_sin_phi :" + e_x_sin_phi);
      
      System.out.println("e :" + e);
      System.out.println("num :" + num);
      System.out.println("den :" + den);
      
      System.out.println("r :" + r);
      System.out.println("t :" + t);
      System.out.println("theta :" + theta);
      System.out.println("x :" + x);
      System.out.println("y :" + y);

      //*/
      return new PointLambert(
              Double.parseDouble(x.toString()),
              Double.parseDouble(y.toString()));


   }

   public static PointDms conToDmsBigPrecision(PointLambert lmb, PrjConAbs prj)
   {
      int scale = 18;
      BigMath.setScale(scale);
      BigDecimal x = new BigDecimal((lmb.getX()));
      BigDecimal y = new BigDecimal(lmb.getY());

      prj.bdlCalculate();
      ElpAbs elp = prj.getEllipsoid();
      
      BigDecimal a = elp.getBdlRadius();
      BigDecimal e = elp.getBdlExcentricity();
      
      BigDecimal lambda0 = prj.getBdlLambda0();
      BigDecimal x0 = prj.getBdlX0();
      BigDecimal y0 = prj.getBdlY0();
      BigDecimal n = prj.getBdlN();
      BigDecimal g = prj.getBdlG();
      BigDecimal r0 = prj.getBdlR0();
      
      BigDecimal x_x0 = x.subtract(x0);
      BigDecimal x_x0_2 = x_x0.multiply(x_x0);
      
      BigDecimal r = 
              BigMath.sqrt(
                  (x_x0_2)
                     .add((r0.subtract(y).add(y0)).multiply(r0.subtract(y).add(y0))),
                  scale
              );
      
      BigDecimal t = BigMath.pow(BigMath.divide(r, (a.multiply(g))),
              BigMath.divide(BigMath.NMB_1, n), scale);
      
      BigDecimal theta = BigMath.atan(
              BigMath.divide(
              (x.subtract(x0)), (r0.subtract(y).add(y0))), scale);

      BigDecimal lambda = BigMath.divide(theta, n).add(lambda0);

      double dblT = Double.parseDouble(t.toString());

      double phi_i = Math.PI / 2D - 2D * Math.atan(dblT);
      double phi_ip1 = 0;

      double dblE = Double.parseDouble(e.toString());

      for (int i = 0; i < 100; ++i)
      {
         phi_ip1 = Math.PI / 2D - 2D * Math.atan(
                 dblT * 
                 Math.pow(
                    (1 - dblE * Math.sin(phi_i))
                    /(1 + dblE * Math.sin(phi_i)),
                 dblE / 2D));
         phi_i = phi_ip1;
        
      }
      
      double lat = Double.parseDouble(BigMath._radToDeg_(lambda, scale).toString());
      double lon = Math.toDegrees(phi_ip1);
           
      double lon_expanded = lon*10000000000000D;
      double lat_expanded = lat*10000000000000D;

      double lon_rounded = Math.round(lon_expanded);
      double lat_rounded = Math.round(lat_expanded);
      
      return new PointDms(
           lat_rounded/10000000000000D,
           lon_rounded/10000000000000D);
   }




}

