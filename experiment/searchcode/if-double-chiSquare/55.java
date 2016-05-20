package org.renjin.stats.internals.distributions;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.ContinuousDistribution;

/**
 * The commons math implementation of the Chisquare distributions
 * throws when df = 0
 *
 */
public class ChisquareZeroDfDistribution implements ContinuousDistribution {

  @Override
  public double cumulativeProbability(double x) throws MathException {
    return 1.0;
  }

  @Override
  public double cumulativeProbability(double x0, double x1)
      throws MathException {
    throw new UnsupportedOperationException();
  }

  @Override
  public double inverseCumulativeProbability(double p) throws MathException {
    if(p == 1) {
      return Double.POSITIVE_INFINITY;
    } else {
      return 0;
    }
  }

@Override
public double density(double x) {
	throw new RuntimeException("not implemented yet");
}

@Override
public double logDensity(double x) {
	throw new RuntimeException("not implemented yet");
}

}

