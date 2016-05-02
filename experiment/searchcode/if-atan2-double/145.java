/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.renjin;

import org.apache.commons.math.special.Beta;
import org.apache.commons.math.special.Gamma;
import org.apache.commons.math.util.MathUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;


/**
 * Math functions not found in java.Math or apache commons math
 */
public class MathExt {

  private static final int DBL_MAX_10_EXP = 308;
  
  private static final int MAX_DIGITS = DBL_MAX_10_EXP;

  private MathExt() {
  }

  
  //@Builtin
 public static double gamma(double x) {
    return Math.exp(Gamma.logGamma(x));
  }
  
  //@Builtin
 public static double sign(double x) {
    return Math.signum(x);
  }

  
  //@Builtin
 public static double log(double x, double base) {

    //Method cannot be called directly as R and Apache Commons Math argument order
    // are reversed
    return MathUtils.log(base, x);
  }

  
  //@Builtin
 public static double log(double d) {
    return Math.log(d);
  }

  
  //@Builtin
 public static double log2(double d) {
    return MathUtils.log(2, d);
  }

  
  //@Builtin
 public static double abs(double x) {
    return Math.abs(x);
  }

  
  //@Builtin
 public static double asinh(double val) {
    return (Math.log(val + Math.sqrt(val * val + 1)));
  }

  
  //@Builtin
 public static double acosh(double val) {
    return (Math.log(val + Math.sqrt(val + 1) * Math.sqrt(val - 1)));
  }

  
  //@Builtin
 public static double atanh(double val) {
    return (0.5 * Math.log((1 + val) / (1 - val)));
  }

  
  //@Internal
 public static double atan2(double y, double x) {
    return (Math.atan2(y, x));
  }

  
  //@Builtin
 public static double signif(double x, int digits) {
    return new BigDecimal(x).round(new MathContext(digits, RoundingMode.HALF_UP)).doubleValue();
  }

  
  //@Builtin
 public static double expm1(double x) {
    return Math.expm1(x);
  }

  
  //@Builtin
 public static double log1p(double x) {
    return Math.log1p(x);
  }

  //@Internal
 
  public static double beta(double a, double b) {
    return (Math.exp(Beta.logBeta(a, b)));
  }

  //@Internal
 
  public static double lbeta(double a, double b) {
    return (Beta.logBeta(a, b));
  }

  //@Internal
 public static double choose(double n, int k) {
    /*
     * Because gamma(a+1) = factorial(a)
     * we use gamma(n+1) /(gamma(n-k+1) * gamma(k+1)) instead of
     * Binomial(n,k) = n! / ((n-k)! * k!) for non-integer n values.
     * 
     */
    if (k < 0) {
      return (0);
    } else if (k == 0) {
      return (1);
    } else if ((int) n == n) {
      return (MathUtils.binomialCoefficientDouble((int) n, k));
    } else {
      return (MathExt.gamma(n + 1) / (MathExt.gamma(n - k + 1) * MathExt.gamma(k + 1)));
    }
  }

  //@Internal
 public static double lchoose(double n, int k) {
    return (Math.log(choose(n, k)));
  }
  
  
  // our wrapper generator gets confused by the two double & float overloads
  // of Math.round
  //@Builtin
  public static double round(double x) {
    return Math.rint(x);
  }
  
  //@Builtin
 
  public static double round(double x, int digits) {
    // adapted from the nmath library, fround.c
    /* = 308 (IEEE); was till R 0.99: (DBL_DIG - 1) */
    /* Note that large digits make sense for very small numbers */
    double sgn;
    int dig;

    if (Double.isNaN(x) || Double.isNaN(digits)) {
      return x + digits;
    }
    if(Double.isInfinite(x)) {
      return x;
    }

    if(digits == Double.POSITIVE_INFINITY) {
      return x;
    } else if(digits == Double.NEGATIVE_INFINITY) {
      return 0.0;
    }

    if (digits > MAX_DIGITS) {
      digits = MAX_DIGITS;
    }
    dig = (int)Math.floor(digits + 0.5);
    
    if(x < 0.) {
      sgn = -1.;
      x = -x;
    } else {
      sgn = 1.;
    }
    if (dig == 0) {
      return sgn * Math.rint(x);
    } else if (dig > 0) {
      double pow10 = Math.pow(10., dig);
      double intx = Math.floor(x);
      return sgn * (intx + Math.rint((x-intx) * pow10) / pow10);
    } else {
      double pow10 = Math.pow(10., -dig);
      return sgn * Math.rint(x/pow10) * pow10;
    }
  }
  
  
  
	// @Builtin("trunc")

	public static double truncate(double x) {
		return Math.floor(x);
	}

	// @Builtin
	public static double ceiling(double x) {
		return Math.ceil(x);
	}

	public static double ceil(double x) {
		return Math.ceil(x);
	}

	public static List<Double> ceil(List<Number> x) {
		java.util.List<Double> result = new ArrayList<Double>();
		for (Number i : x) {
			result.add(Math.ceil(i.doubleValue()));
		}
		return result;
	}
	
	public static double lgamma(double x) {
		return Gamma.logGamma(x);
	}
}

