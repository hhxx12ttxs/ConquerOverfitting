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

package org.geoforge.worldwind._tempo.projection.conical;

import org.geoforge.worldwind._tempo.ellipsoids.ElpAbs;
import org.geoforge.worldwind._tempo.maths.BigMath;
import org.geoforge.worldwind._tempo.projection.PrjAbs;
import java.math.BigDecimal;

/**
 *
 * @author Amadeus.Sowerby
 *
 * email: Amadeus.Sowerby_AT_gmail.com
 * ... please remove "_AT_" from the above string to get the right email address
 */
public class PrjConAbs extends PrjAbs
{
   final protected static int SCALE = 18;
   
   public PrjConAbs
   (
     int epsg,
     ElpAbs ellipsoid,//
     BigDecimal phi0,
     BigDecimal phi1,
     BigDecimal phi2,
     BigDecimal lambda0,
     BigDecimal x0,//
     BigDecimal y0,//
     int scale
   )
   {
      super(epsg, ellipsoid);
      
      this._scale_ = scale;
      this._lambda0_ = lambda0;
      this._x0_ = x0;
      this._y0_ = y0;
      
      this._phi0_ = phi0;
      this._phi1_ = phi1;
      this._phi2_ = phi2;
      

   }
   
   
   public void dblCalculate()
   {

      double e_2 = super.getEllipsoid().getDblSquareExcentricity();
      double e   = super.getEllipsoid().getDblExcentricity();
      double a   = super.getEllipsoid().getDblRadius();
      

      double phi1 = this._phi1_.doubleValue();
      double phi2 = this._phi2_.doubleValue();
      double phi0 = this._phi0_.doubleValue();

      double sin_phi0 = Math.sin(phi0);

      double cos_phi1 = Math.cos(phi1);
      double sin_phi1 = Math.sin(phi1);
      double sin_2_phi1 = sin_phi1 * sin_phi1;

      double cos_phi2 = Math.cos(phi2);
      double sin_phi2 = Math.sin(phi2);
      double sin_2_phi2 = sin_phi2 * sin_phi2;
      

      
            //calculs
      double m1 = cos_phi1 / Math.sqrt(1 - e_2 * sin_2_phi1);
      double m2 = cos_phi2 / Math.sqrt(1 - e_2 * sin_2_phi2);
      

      double t1 =
              Math.tan(Math.PI / 4D - phi1 / 2D)
              / Math.pow(
              (1 - e * sin_phi1) / (1 + e * sin_phi1),
              e / 2D);
      
      
      double t2 =
              Math.tan(Math.PI / 4D - phi2 / 2D)
              / Math.pow(
              (1 - e * sin_phi2) / (1 + e * sin_phi2),
              e / 2D);
      double t0 =
              Math.tan(Math.PI / 4D - phi0 / 2D)
              / Math.pow(
              (1 - e * sin_phi0) / (1 + e * sin_phi0),
              e / 2D);

      this._dblN_ = (Math.log(m1) - Math.log(m2))
              / (Math.log(t1) - Math.log(t2));
      
      this._dblG_ = m1 / (this._dblN_ * Math.pow(t1, this._dblN_));

      this._dblR0_ = a * this._dblG_ * Math.pow(t0, this._dblN_);

      /*/
      System.out.println("a : " + a);
      System.out.println("dblE : " + e);
      
      System.out.println("phi0 : " + phi0);
      System.out.println("phi1 : " + phi1);
      System.out.println("phi2 : " + phi2);
      
      
      System.out.println("sin_phi0 : " + sin_phi0);
      System.out.println("cos_phi1 : " + cos_phi1);
      System.out.println("sin_phi1 : " + sin_phi1);
      System.out.println("sin_2_phi1 : " + sin_2_phi1);
      System.out.println("cos_phi2 : " + cos_phi2);
      System.out.println("sin_phi2 : " + sin_phi2);
      System.out.println("sin_2_phi2 : " + sin_2_phi2);
      
      System.out.println("m1 : " + m1);
      System.out.println("m2 : " + m2);
      System.out.println("t1 : " + t1);
      System.out.println("t2 : " + t2);
      System.out.println("t0 : " + t0);

      
      System.out.println("r0 : " + _dblR0_);
      System.out.println("g : " + _dblG_);
      System.out.println("n : " + _dblN_);
      
      
      //*/
   }
   
   public void bdlCalculate()
   {
      BigDecimal sin_phi0 = BigMath.sin(this._phi0_, this._scale_);
      BigDecimal sin_phi1 = BigMath.sin(this._phi1_, this._scale_);
      BigDecimal cos_phi1 = BigMath.cos(this._phi1_, this._scale_);
      BigDecimal sin_phi2 = BigMath.sin(this._phi2_, this._scale_);
      BigDecimal cos_phi2 = BigMath.cos(this._phi2_, this._scale_);    
      
      BigDecimal sin_2_phi1 = sin_phi1.multiply(sin_phi1);
      BigDecimal sin_2_phi2 = sin_phi2.multiply(sin_phi2);
      
      BigDecimal e_2 = super.getEllipsoid().getBdlSquareExcentricity();
      BigDecimal e = super.getEllipsoid().getBdlExcentricity();
      BigDecimal a = super.getEllipsoid().getBdlRadius();
      
      BigDecimal m1 = cos_phi1.divide(
              BigMath.sqrt(
              BigMath.NMB_1.subtract(e_2.multiply(sin_2_phi1)), this._scale_), this._scale_, BigDecimal.ROUND_HALF_UP);
      
      BigDecimal m2 = cos_phi2.divide(
              BigMath.sqrt(
              BigMath.NMB_1.subtract(e_2.multiply(sin_2_phi2)), this._scale_), this._scale_, BigDecimal.ROUND_HALF_UP);

      BigDecimal t0 =
              BigMath.divide(
              BigMath.tan(
              BigMath.divide(BigMath.pi(this._scale_), BigMath.NMB_4).subtract(BigMath.divide(this._phi0_, BigMath.NMB_2)),
              this._scale_),
              BigMath.pow(
              BigMath.divide(
                  BigMath.NMB_1.subtract(e.multiply(sin_phi0)),
                  BigMath.NMB_1.add(e.multiply(sin_phi0))),
              BigMath.divide(e, BigMath.NMB_2),
              this._scale_));
      
      BigDecimal t1 = 
              BigMath.divide(
              BigMath.tan(
                  BigMath.divide(BigMath.pi(this._scale_), BigMath.NMB_4).subtract(BigMath.divide(this._phi1_, BigMath.NMB_2)),
              this._scale_),
              BigMath.pow(
              BigMath.divide(
                  BigMath.NMB_1.subtract(e.multiply(sin_phi1)),
                  BigMath.NMB_1.add(e.multiply(sin_phi1))
              ),
              BigMath.divide(e, BigMath.NMB_2),
              this._scale_));
      
      BigDecimal t2 =
              BigMath.divide(
              BigMath.tan(
              BigMath.divide(BigMath.pi(this._scale_), BigMath.NMB_4).subtract(BigMath.divide(this._phi2_, BigMath.NMB_2)),
              this._scale_),
              BigMath.pow(
              BigMath.divide(
                  BigMath.NMB_1.subtract(e.multiply(sin_phi2)),
                  BigMath.NMB_1.add(e.multiply(sin_phi2))),
              BigMath.divide(e, BigMath.NMB_2),
              this._scale_));
      
      this._bdmN_ = BigMath.divide((BigMath.log(m1, this._scale_).subtract(BigMath.log(m2, this._scale_))),
              (BigMath.log(t1, this._scale_).subtract(BigMath.log(t2, this._scale_))));
      
      this._bdmG_ = BigMath.divide(
              m1,
              (_bdmN_.multiply(BigMath.pow(t1, _bdmN_, this._scale_))));
      
      this._bdmR0_ = a.multiply(_bdmG_).multiply(BigMath.pow(t0, _bdmN_, this._scale_));

      /*/
      System.out.println("e_2 :" + e_2);
      System.out.println("e :" + e);
      System.out.println("a :" + a);
      

      
      System.out.println("_sin_phi1_ :" + sin_phi1);
      System.out.println("_sin_phi2_ :" + sin_phi2);
      System.out.println("_sin_phi0_ :" + sin_phi0);
      System.out.println("_cos_phi1_ :" + cos_phi1);
      System.out.println("_cos_phi2_ :" + cos_phi2);

      System.out.println("_m1_ :" + m1);
      System.out.println("_m2_ :" + m2);
      System.out.println("_t0_ :" + t0);
      System.out.println("_t1_ :" + t1);
      System.out.println("_t2_ :" + t2);
      System.out.println("_n_ :" + this._bdmN_);
      System.out.println("_g_ :" + this._bdmG_);
      System.out.println("_r0_ :" + this._bdmR0_);
      System.out.println("_t0_pow_n :" + BigMath.pow(t0, this._bdmN_, this._scale_));
      
      //*/
   }
   
   public BigDecimal getBdlLambda0()
   {
      return this._lambda0_;
   }

   public Double getDblLambda0()
   {
      return Double.valueOf(_lambda0_.toString());
   }
   
   public BigDecimal getBdlX0()
   {
      return this._x0_;
   }

   public Double getDblX0()
   {
      return Double.valueOf(_x0_.toString());
   }
   
   public BigDecimal getBdlY0()
   {
      return this._y0_;
   }

   public Double getDblY0()
   {
      return Double.valueOf(_y0_.toString());
   }
   
  
   //- n
   public BigDecimal getBdlN()
   {
      return this._bdmN_;
   }

   public Double getDblN()
   {
      return this._dblN_;
   }
      
   //- g
   public BigDecimal getBdlG()
   {
      return this._bdmG_;
   }

   public Double getDblG()
   {
      return this._dblG_;
   }
   
   //- r0
   public BigDecimal getBdlR0()
   {
      return this._bdmR0_;
   }

   public Double getDblR0()
   {
      return this._dblR0_;
   }


   

   
   
   private BigDecimal _lambda0_;
   
   private BigDecimal _x0_;
   private BigDecimal _y0_;
   
   private BigDecimal _phi0_;
   private BigDecimal _phi1_;
   private BigDecimal _phi2_;
   
   
   private BigDecimal _bdmN_;
   private BigDecimal _bdmG_;
   private BigDecimal _bdmR0_;
   
   private double _dblN_;
   private double _dblG_;
   private double _dblR0_;
   
   private int _scale_;


           
}

