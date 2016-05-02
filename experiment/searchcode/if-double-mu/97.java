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

import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.distribution.AbstractContinuousDistribution;
import org.apache.commons.math.distribution.BetaDistributionImpl;
import org.apache.commons.math.distribution.BinomialDistributionImpl;
import org.apache.commons.math.distribution.CauchyDistributionImpl;
import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;
import org.apache.commons.math.distribution.ContinuousDistribution;
import org.apache.commons.math.distribution.Distribution;
import org.apache.commons.math.distribution.ExponentialDistributionImpl;
import org.apache.commons.math.distribution.FDistributionImpl;
import org.apache.commons.math.distribution.GammaDistributionImpl;
import org.apache.commons.math.distribution.HypergeometricDistributionImpl;
import org.apache.commons.math.distribution.IntegerDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.distribution.PascalDistributionImpl;
import org.apache.commons.math.distribution.PoissonDistributionImpl;
import org.apache.commons.math.distribution.TDistributionImpl;
import org.apache.commons.math.distribution.WeibullDistributionImpl;
import org.renjin.stats.internals.distributions.Beta;
import org.renjin.stats.internals.distributions.Binom;
import org.renjin.stats.internals.distributions.ChiSquare;
import org.renjin.stats.internals.distributions.ChisquareZeroDfDistribution;
import org.renjin.stats.internals.distributions.F;
import org.renjin.stats.internals.distributions.Geometric;
import org.renjin.stats.internals.distributions.LNorm;
import org.renjin.stats.internals.distributions.LogisticDistribution;
import org.renjin.stats.internals.distributions.SignRank;
import org.renjin.stats.internals.distributions.StudentsT;
import org.renjin.stats.internals.distributions.Tukey;
import org.renjin.stats.internals.distributions.UniformDistribution;
import org.renjin.stats.internals.distributions.Wilcox;


/**
 * Density, mass, cumulative and inverse cumulative distribution functions.
 *
 * <p>The methods defined here serve as an adapter between the R function conventions and the
 * Apache Commons Math Library. (See {@link Distribution}
 */
public class Distributions {

  private Distributions() {
  }

  // TODO: there are several distributions for which the inverse is not provided
  // by commons math, and most non-central distributions are not present.
  // these should be implemented in the distributions package but using the Commons Math API
  /**
   * Calculates the value of the density function at {@code x}
   * for the given continuous distribution
   *
   * @param dist the distribution of the random variable
   * @param x the value
   * @param log whether to return the natural logarithm of the function's value
   * @return the (natural logarithm) of the relative likelihood for the random
   * variable to take the value {@code x}
   */
  private static double d(AbstractContinuousDistribution dist, double x, boolean log) {
    double d = dist.density(x);
    if (log) {
      d = Math.log(d);
    }
    return d;
  }

  /**
   * Calculates the value of the probability mass function at {@code x}
   * for the given discrete distribution
   *
   * @param dist the discrete distribution
   * @param x the value
   * @param log whether to return the natural logarithm of the probability
   * @return  the (natural logarithm) of the probability for the  random variable
   *  to take the value {@code x}
   */
  private static double d(IntegerDistribution dist, double x, boolean log) {
    double d = dist.probability(x);
    if (log) {
      d = Math.log(d);
    }
    return d;
  }

  /**
   *
   * Calculates the value of the cumulative distribution function
   *
   * @param dist the distribution
   * @param q the value
   * @param lowerTail if true, return the value P(x < q), otherwise P(x > q)
   * @param logP  if true, return the natural logarithm of the probability
   * @return  the probability that the random variable will take the value less than (greater than)
   * {@code q}
   */
  private static double p(Distribution dist, double q, boolean lowerTail, boolean logP) {
    double p;
    try {
      p = dist.cumulativeProbability(q);
    } catch (MathException e) {
      return Double.NaN;
    } catch (MathRuntimeException e) {
      return Double.NaN;
    }
    if (!lowerTail) {
      p = 1.0 - p;
    }
    if (logP) {
      p = Math.log(p);
    }

    return p;
  }

  /**
   * Calculates the value of the inverse cumulative probability function according to standard R arguments.
   *
   * @param dist the continuous distribution
   * @param p the probability
   * @param lowerTail
   * @param logP if true, interpret {@code p} as the natural logarithm of the probability
   * @return the value fo
   */
  private static double q(ContinuousDistribution dist, double p, boolean lowerTail, boolean logP) {
    if (logP) {
      p = Math.exp(p);
    }
    double q = 0;
    try {
      q = dist.inverseCumulativeProbability(p);
    } catch (IllegalArgumentException e) {
      return Double.NaN;
    } catch (MathException e) {
      return Double.NaN;
    } catch (MathRuntimeException e) {
      return Double.NaN;
    }
    if (!lowerTail) {
      q = -q;
    }
    return q;
  }

  private static double q(IntegerDistribution dist, double p, boolean lowerTail, boolean logP) {
    if (logP) {
      p = Math.exp(p);
    }
    double q = 0;
    try {
      q = dist.inverseCumulativeProbability(p);
    } catch (IllegalArgumentException e) {
      return Double.NaN;
    } catch (MathException e) {
      return Double.NaN;
    } catch (MathRuntimeException e) {
      return Double.NaN;
    }
    if (!lowerTail) {
      q = -q;
    }
    return q;
  }

  //@Internal
 public static double dnorm(final double x, final double mean, final double sd, boolean log) {
    return d(new NormalDistributionImpl(mean, sd), x, log);
  }

   //@Internal
  public static double pnorm(final double q, final double mean, final double sd, boolean lowerTail, boolean logP) {
    return p(new NormalDistributionImpl(mean, sd), q, lowerTail, logP);
  }

   //@Internal
  public static double plnorm(final double q, final double logmean, final double logsd, boolean lowerTail, boolean logP) {
    return p(new NormalDistributionImpl(logmean, logsd), Math.log(q), lowerTail, logP);
  }

   //@Internal
  public static double qnorm(final double p, final double mean, final double sd, boolean lowerTail, boolean logP) {
    return q(new NormalDistributionImpl(mean, sd), p, lowerTail, logP);
  }

   //@Internal
  public static double qlnorm(final double p, final double meanlog, final double sdlog, boolean lowerTail, boolean logP) {
    return Math.exp(q(new NormalDistributionImpl(meanlog, sdlog), p, lowerTail, logP));
  }

   //@Internal
  public static double dlnorm(final double x, final double meanlog, final double sdlog, boolean logP) {
    return LNorm.dlnorm(x, meanlog, sdlog, logP);
  }

   //@Internal
  public static double dbeta(final double x, final double shape1, final double shape2, boolean log) {
    return d(new BetaDistributionImpl(shape1, shape2), x, log);
  }

   //@Internal
  public static double dnbeta(final double x, final double shape1, final double shape2, final double ncp, boolean log) {
    return Beta.dnbeta(x, shape1, shape2, ncp, log);
  }

   //@Internal
  public static double pbeta(final double q, final double shape1, final double shape2, boolean lowerTail, boolean logP) {
    return p(new BetaDistributionImpl(shape1, shape2), q, lowerTail, logP);
  }

   //@Internal
  public static double pnbeta(final double q, final double shape1, final double shape2, final double ncp, boolean lowerTail, boolean logP) {
    return Beta.pnbeta(q, shape1, shape2, ncp, lowerTail, logP);
  }

   //@Internal
  public static double qbeta(final double p, final double shape1, final double shape2, boolean lowerTail, boolean logP) {
    return q(new BetaDistributionImpl(shape1, shape2), p, lowerTail, logP);
  }

   //@Internal
  public static double qnbeta(final double p, final double shape1, final double shape2, final double ncp, boolean lowerTail, boolean logP) {
    return Beta.qnbeta(p, shape1, shape2, ncp, lowerTail, logP);
  }

   //@Internal
  public static double dchisq(final double x, final double df, boolean log) {
    return d(new ChiSquaredDistributionImpl(df), x, log);
  }

   //@Internal
  public static double dnchisq(final double x, final double df, final double ncp, boolean log) {
    return ChiSquare.dnchisq(x, df, ncp, log);
  }

   //@Internal
  public static double pchisq(final double q, final double df, boolean lowerTail, boolean logP) {
    if(df == 0) {
      return p(new ChisquareZeroDfDistribution(), q, lowerTail, logP);
    } else {
      return p(new ChiSquaredDistributionImpl(df), q, lowerTail, logP);
    }
  }

   //@Internal
  public static double pnchisq(final double q, final double df, final double ncp, boolean lowerTail, boolean logP) {
    return ChiSquare.pnchisq(q, df, ncp, lowerTail, logP);
  }

   //@Internal
  public static double qchisq(final double p, final double df, boolean lowerTail, boolean logP) {
    if(df == 0) {
      return q(new ChisquareZeroDfDistribution(), p, lowerTail, logP);
    } else {
      return q(new ChiSquaredDistributionImpl(df), p, lowerTail, logP);
    }
  }

   //@Internal
  public static double qnchisq(final double p, final double df, final double ncp, boolean lowerTail, boolean logP) {
    return ChiSquare.qnchisq(p, df, ncp, lowerTail, logP);
  }

   //@Internal
  public static double dexp(final double x, final double rate, boolean log) {
    return d(new ExponentialDistributionImpl(1.0/rate), x, log);
  }

   //@Internal
  public static double pexp(final double q, final double rate, boolean lowerTail, boolean logP) {
    return p(new ExponentialDistributionImpl(1.0/rate), q, lowerTail, logP);
  }

   //@Internal
  public static double qexp(final double p, final double rate, boolean lowerTail, boolean logP) {
    return q(new ExponentialDistributionImpl(1.0/rate), p, lowerTail, logP);
  }

   //@Internal
  public static double dt(final double x, final double df, boolean log) {
    return d(new TDistributionImpl(df), x, log);
  }

   //@Internal
  public static double dnt(final double x, final double df, final double ncp, boolean log) {
    return StudentsT.dnt(x, df, ncp, log);
  }

   //@Internal
  public static double pt(final double q, final double df, boolean lowerTail, boolean logP) {
    return p(new TDistributionImpl(df), q, lowerTail, logP);
  }

   //@Internal
  public static double pnt(final double q, final double df, final double ncp, boolean lowerTail, boolean logP) {
    return StudentsT.pnt(q, df, ncp, lowerTail, logP);
  }

   //@Internal
  public static double qt(final double p, final double df, boolean lowerTail, boolean logP) {
    return q(new TDistributionImpl(df), p, lowerTail, logP);
  }

   //@Internal
  public static double qnt(final double p, final double df, final double ncp, boolean lowerTail, boolean logP) {
    return StudentsT.qnt(p, df, ncp, lowerTail, logP);
  }

   //@Internal
  public static double dpois(final double x, final double lambda, boolean log) {
    return d(new PoissonDistributionImpl(lambda), x, log);
  }

   //@Internal
  public static double ppois(final double q, final double lambda, boolean lowerTail, boolean logP) {
    return p(new PoissonDistributionImpl(lambda), q, lowerTail, logP);
  }

  public static double qpois(double p, double lambda, boolean lowerTail, boolean logP)  {
    return q(new PoissonDistributionImpl(lambda), p, lowerTail, logP);
  }
  
   //@Internal
  public static double dbinom(final double x, final int size, final double prob, boolean log) {
    return d(new BinomialDistributionImpl(size, prob), x, log);
  }

   //@Internal
  public static double dnbinom(final double x, final int size, final double prob, boolean log) {
    return d(new PascalDistributionImpl(size, prob), x, log);
  }

   //@Internal
  public static double dnbinom_mu(final double x, final int size, final double mu, boolean log) {
    return Binom.dnbinom_mu(x, size, mu, log);
  }

   //@Internal
  public static double pbinom(final double x, final int size, final double prob, boolean lowerTail, boolean logP) {
    return p(new BinomialDistributionImpl(size, prob), x, lowerTail, logP);
  }

   //@Internal
  public static double pnbinom(final double x, final int size, final double prob, boolean lowerTail, boolean logP) {
    return p(new PascalDistributionImpl(size, prob), x, lowerTail, logP);
  }

   //@Internal
  public static double pnbinom_mu(final double x, final int size, final double mu, boolean lowerTail, boolean logP) {
    if (!logP) {
      return Binom.pnbinom_mu(x, size, mu, lowerTail ? false : true, logP);
    } else {
      return Binom.pnbinom_mu(x, size, mu, lowerTail, logP);
    }
  }

   //@Internal
  public static double qbinom(final double p, final int size, final double prob, boolean lowerTail, boolean logP) {
    return q(new BinomialDistributionImpl(size, prob), p, lowerTail, logP) + 1;
  }

   //@Internal
  public static double qnbinom(final double p, final double size, final double prob, boolean lower_tail, boolean log_p) {
    return Binom.qnbinom(p, size, prob, lower_tail, log_p);
  }

   //@Internal
  public static double qnbinom_mu(final double p, final double size, final double mu, boolean lower_tail, boolean log_p) {
    return Binom.qnbinom_mu(p, size, mu, lower_tail, log_p);
  }

   //@Internal
  public static double dcauchy(final double x, final double location, final double scale, boolean log) {
    return d(new CauchyDistributionImpl(location, scale), x, log);
  }

   //@Internal
  public static double pcauchy(final double q, final double location, final double scale, boolean lowerTail, boolean logP) {
    return p(new CauchyDistributionImpl(location, scale), q, lowerTail, logP);
  }

   //@Internal
  public static double qcauchy(final double p, final double location, final double scale, boolean lowerTail, boolean logP) {
    return q(new CauchyDistributionImpl(location, scale), p, lowerTail, logP);
  }

   //@Internal
  public static double df(final double x, final double df1, final double df2, boolean log) {
    return d(new FDistributionImpl(df1, df2), x, log);
  }

   //@Internal
  public static double dnf(final double x, final double df1, final double df2, final double ncp, boolean log) {
    return F.dnf(x, df1, df2, ncp, log);
  }

   //@Internal
  public static double pf(final double q, final double df1, final double df2, boolean lowerTail, boolean logP) {
    return p(new FDistributionImpl(df1, df2), q, lowerTail, logP);
  }

   //@Internal
  public static double pnf(final double q, final double df1, final double df2, final double ncp, boolean lowerTail, boolean logP) {
    return F.pnf(q, df1, df2, ncp, lowerTail, logP);
  }

   //@Internal
  public static double qf(final double p, final double df1, final double df2, boolean lowerTail, boolean logP) {
    return q(new FDistributionImpl(df1, df2), p, lowerTail, logP);
  }

   //@Internal
  public static double qnf(final double p, final double df1, final double df2, final double ncp, boolean lowerTail, boolean logP) {
    return F.qnf(p, df1, df2, ncp, lowerTail, logP);
  }

   //@Internal
  public static double dgamma(final double x, final double shape, final double scale, boolean log) {
    return d(new GammaDistributionImpl(shape, scale), x, log);
  }

   //@Internal
  public static double pgamma(final double q, final double shape, final double scale, boolean lowerTail, boolean logP) {
    return p(new GammaDistributionImpl(shape, scale), q, lowerTail, logP);
  }

   //@Internal
  public static double qgamma(final double p, final double shape, final double scale, boolean lowerTail, boolean logP) {
    return q(new GammaDistributionImpl(shape, scale), p, lowerTail, logP);
  }

   //@Internal
  public static double dunif(final double x, final double min, final double max, boolean log) {
    double d = new UniformDistribution(min, max).density(x);
    if (log) {
      d = Math.log(d);
    }
    return d;
  }

   //@Internal
  public static double punif(final double q, final double min, final double max, boolean lowerTail, boolean logP) {
    return p(new UniformDistribution(min, max), q, lowerTail, logP);
  }

   //@Internal
  public static double qunif(final double p, final double min, final double max, boolean lowerTail, boolean logP) {
    return q(new UniformDistribution(min, max), p, lowerTail, logP);
  }

   //@Internal
  public static double dweibull(final double x, final double shape, final double scale, boolean log) {
    return d(new WeibullDistributionImpl(shape, scale), x, log);
  }

   //@Internal
  public static double pweibull(final double q, final double shape, final double scale, boolean lowerTail, boolean logP) {
    return p(new WeibullDistributionImpl(shape, scale), q, lowerTail, logP);
  }

   //@Internal
  public static double qweibull(final double p, final double shape, final double scale, boolean lowerTail, boolean logP) {
    return q(new WeibullDistributionImpl(shape, scale), p, lowerTail, logP);
  }

   //@Internal
  public static double dhyper(final double x, final double whiteBalls, final double blackBalls, final double sampleSize, boolean log) {
    return d(new HypergeometricDistributionImpl((int) (whiteBalls + blackBalls), (int) whiteBalls, (int) sampleSize), x, log);
  }

   //@Internal
  public static double phyper(final double q, final double x, final double whiteBalls, final double blackBalls, final double sampleSize, boolean lowerTail, boolean logP) {
    return p(new HypergeometricDistributionImpl((int) (whiteBalls + blackBalls), (int) whiteBalls, (int) sampleSize), q, lowerTail, logP);
  }

  public static double qhyper(double p,double m, double n, double k, boolean lowerTail, boolean logP)  {
    return q(new HypergeometricDistributionImpl((int)m, (int)n, (int)k), p, lowerTail, logP);
  }
  /*
  public static double dgeom(final int x, final double p, final boolean log) {
  if (log) {
  return (Math.log(p * Math.pow(1 - p, x)));
  } else {
  return (p * Math.pow(1 - p, x));
  }
  }
   */
   //@Internal
  public static double pgeom(final double q, final double prob, boolean lowerTail, boolean log) {
    return (Geometric.pgeom(q, prob, lowerTail, log));
  }

   //@Internal
  public static double dgeom(final double x, final double prob, boolean log) {
    return Geometric.dgeom(x, prob, log);
  }

   //@Internal
  public static double qgeom(final double p, final double prob, boolean lowerTail, boolean log) {
    return Geometric.qgeom(p, prob, lowerTail, log);
  }

   //@Internal
  public static double plogis(final double p, final double m, final double s, boolean lowerTail, boolean logP) {
    return p(new LogisticDistribution(m, s), p, lowerTail, logP);
  }

   //@Internal
  public static double dlogis(final double x, final double location, final double scale, boolean log) {
    return d(new LogisticDistribution(location, scale), x, log);
  }

   //@Internal
  public static double qlogis(final double p, final double m, final double s, boolean lowerTail, boolean logP) {
    return q(new LogisticDistribution(m, s), p, lowerTail, logP);
  }

   //@Internal
  public static double qsignrank(double p, double n, boolean lowerTail, boolean logP) {
    return SignRank.qsignrank(p, n, lowerTail, logP);
  }

   //@Internal
  public static double psignrank(double p, double n, boolean lowerTail, boolean logP) {
    return SignRank.psignrank(p, n, lowerTail, logP);
  }

   //@Internal
  public static double dsignrank(double x, double n, boolean logP) {
    return SignRank.dsignrank(x, n, logP);
  }

   //@Internal
  public static double dwilcox(double x, double m, double n, boolean logP) {
    return Wilcox.dwilcox(x, m, n, logP);
  }

   //@Internal
  public static double pwilcox(double q, double m, double n, boolean lowerTail, boolean logP) {
    return Wilcox.pwilcox(q, m, n, lowerTail, logP);
  }

   //@Internal
  public static double qwilcox(double p, double m, double n, boolean lowerTail, boolean logP) {
    return Wilcox.qwilcox(p, m, n, lowerTail, logP);
  }

   //@Internal
  public static double ptukey(final double q, final double nranges, final double nmeans, final double df, boolean lowerTail, boolean logP) {
    return (Tukey.ptukey(q, nranges, nmeans, df, lowerTail, logP));
  }

   //@Internal
  public static double qtukey(final double p, final double nranges, final double nmeans, final double df, boolean lowerTail, boolean logP) {
    return (Tukey.qtukey(p, nranges, nmeans, df, lowerTail, logP));
  }
}

