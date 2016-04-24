package queuing.common;/*
 *
 *  * INESC-ID, Instituto de Engenharia de Sistemas e Computadores InvestigaĂ§ĂŁo e Desevolvimento em Lisboa
 *  * Copyright 2013 INESC-ID and/or its affiliates and other
 *  * contributors as indicated by the @author tags. All rights reserved.
 *  * See the copyright.txt in the distribution for a full listing of
 *  * individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 3.0 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt Date: 01/10/12
 */
public class QueuingMathTools {

   private static double[] facCache;
   private static int max = 200;
   private static final Log log = LogFactory.getLog(QueuingMathTools.class);

   private static AtomicBoolean ready = new AtomicBoolean(false);

   static {
      System.out.println("Initing Factorial up to " + max);
      init(max);
      ready.set(true);
      System.out.println("Factorial initialized");
   }

   private static void init(int newMax) {
      log.trace("QueuingMathTools: initializing the factorial cache with max value = " + newMax);
      double[] newFacCache = new double[newMax + 1];
      int toCopy = facCache == null ? 0 : facCache.length;
      if (facCache != null)
         System.arraycopy(facCache, 0, newFacCache, 0, toCopy);
      for (int i = toCopy; i <= newMax; i++) {
         newFacCache[i] = realFac(i);
      }
      max = newMax;
      facCache = newFacCache;
   }

   private static double realFac(int n) {
      if (n < 0)
         throw new RuntimeException("Factorial invoked on a negative number");
      if (n == 0 || n == 1)
         return 1;
      return n * fac(n - 1);
   }


   public static double fac(int n) {
      if (n > max || !ready.get())
         return realFac(n);
      return facCache[n];
   }

   public static double pow(double a, double b) {
      return Math.pow(a, b);
   }
}

