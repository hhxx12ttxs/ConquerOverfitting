package edu.uprm.cga.ininsim.simpack.utils;

/**
 * Poisson distribution random number generator (RNG).
 *
 * <p>The Poisson distribution corresponds to the number of events that
 * occur in a given interval of time, when the time between events follow
 * an exponential distribution.
 *
 * <p>The RNG returns the number of events in a given interval (i.e., a
 * non-negative integer value).
 *
 * <p>Note that the Poisson distribution is defined by the parameter
 * value lambda (i.e., the average number of events that occur within the
 * specified interval), or equivalently:
 * <ul>
 * <li>mean = lambda</li>
 * <li>variance = lambda</li>
 * </ul>
 *
 * @author Gabriel J. Perez-Irizarry
 */
public class PoissonGenerator implements RandomGenerator
{
  private double lambda;

  /**
   * Constructor.
   *
   * @param lambda
   *        the average number of events that occur within a time interval
   *        (i.e., a non-negative real value).
   * @throws IllegalArgumentException if {@code lambda} is out of range.
   */
  PoissonGenerator(final double lambda)
  {
    PoissonGenerator.checkLambdaRange(lambda);
    this.lambda = lambda;
  }

  /**
   * Generates a Poisson distribution random number.
   *
   * @return The generated random number in {0,1,2,...}.
   */
  @Override
  public final double generate()
  {
    return PoissonGenerator.generate(this.lambda);
  }

  /**
   * Generates a Poisson distribution random number.
   *
   * @param lambda The mean number of events that occur within the
   *               specified interval.
   * @return The generated random number in {0,1,2,...}.
   * @throws IllegalArgumentException if {@code lambda} is out of range.
   */
  public static int generate(final double lambda)
  {
    PoissonGenerator.checkLambdaRange(lambda);

    double sum = 0;
    int count = -1;

    while (sum <= lambda)
    {
      sum += ExponentialGenerator.generate(1.0);
      ++count;
    }

    return count;
  }

  private static void checkLambdaRange(final double lambda)
  {
    if (lambda < 0)
    {
      throw new IllegalArgumentException(
        "Lambda is not in range (>= 0): " + lambda);
    }
  }
}

