package edu.cmu.cs.ark.yc.utils;


import org.apache.commons.math3.util.FastMath;

/**
 * This class contains useful Math-related utilities that are not found in Apache Commons Math 3.0.
 * 
 * @author Yanchuan Sim
 * @version 0.1, 04/02/2012
 */
public abstract class MathMethods
{
  /**
   * Computes the log sum of two log-ed numbers.
   * 
   * @param log_a
   *          log a
   * @param log_b
   *          log b
   * @return log(a + b)
   */
  public static double logSum(double log_a, double log_b)
  {
    double diff = log_a - log_b;

    if (FastMath.abs(diff) > 100)
      return FastMath.max(log_a, log_b);

    return log_b + FastMath.log1p(FastMath.exp(diff));
  }

  /**
   * Computes the square of a number.
   * 
   * @param x
   *          Value.
   * @return x*x
   */
  public static double square(double x)
  {
    return x * x;
  }

  /**
   * Computes the cube of a number.
   * 
   * @param x
   *          Value.
   * @return x*x*x
   */
  public static double cube(double x)
  {
    return x * x * x;
  }

  /**
   * Multiply two numbers safely, i.e when one of them is 0, the result is always 0.
   * 
   * @param a
   *          Multiplier.
   * @param b
   *          Multiplicator.
   * @return <code>a * b</code>
   */
  public static double safeMultiply(double a, double b)
  {
    if (a == 0 || b == 0)
      return 0.0;

    return a * b;
  }
}

