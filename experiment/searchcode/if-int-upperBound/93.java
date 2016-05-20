package edu.uprm.cga.ininsim.simpack.utils;

/**
 * Discrete uniform distribution random number generator (RNG).
 *
 * <p>The discrete uniform distribution corresponds to a sequence of
 * integers, [a, a+1, a+2, ... , b], with equally likely probability to be
 * selected.
 *
 * <p>The RNG returns an integer in a given interval, [a,b].
 *
 * <p>Note that the discrete uniform distribution is defined by parameter
 * values [a,b] = [lowerBound, upperBound], or equivalently:
 * <ul>
 * <li>mean = (b + a) / 2</li>
 * <li>variance = (b - a + 1)^2 / 12 - 1</li>
 * </ul>
 *
 * @author Miguel A. Figueroa-Villanueva
 */
public class DiscreteUniformGenerator implements RandomGenerator
{
  private int lowerBound;
  private int upperBound;

  /**
   * Constructor.
   *
   * @param lowerBound Minimum possible number generated.
   * @param upperBound Maximum possible number generated.
   */
  public DiscreteUniformGenerator(final int lowerBound,
                                  final int upperBound)
  {
    DiscreteUniformGenerator.checkBounds(lowerBound, upperBound);

    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  /**
   * Generates a discrete uniform distribution random number.
   *
   * @return The generated integer random number in [a,b].
   */
  @Override
  public final double generate()
  {
    return DiscreteUniformGenerator.generate(this.lowerBound,
                                             this.upperBound);
  }

  /**
   * Generates a discrete uniform distribution random number.
   *
   * <p>Generator using the inverse-transformation method:
   *
   * <pre>x = a + [(b - a + 1) * U]</pre>
   *
   * @param lowerBound Minimum possible number generated.
   * @param upperBound Maximum possible number generated.
   * @return The generated integer random number in [a,b].
   */
  public static int generate(final int lowerBound, final int upperBound)
  {
    DiscreteUniformGenerator.checkBounds(lowerBound, upperBound);

    return
      lowerBound
      + RANDOM_NUMBER_GENERATOR.nextInt(upperBound - lowerBound + 1);
  }

  private static void checkBounds(final int lowerBound,
                                  final int upperBound)
  {
    if (upperBound < lowerBound)
    {
      throw new IllegalArgumentException(
        "Upper bound < Lower Bound: " + upperBound + " < " + lowerBound);
    }
  }
}

