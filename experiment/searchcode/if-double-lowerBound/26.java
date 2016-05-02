package edu.uprm.cga.ininsim.simpack.utils;

/**
 * Continuous uniform distribution random number generator (RNG).
 *
 * <p>The continuous uniform distribution corresponds to a real-valued
 * interval, [a,b].
 *
 * <p>The RNG returns a number in a given interval, [a,b].
 *
 * <p>Note that the discrete uniform distribution is defined by parameter
 * values [a,b] = [lowerBound, upperBound], or equivalently:
 * <ul>
 * <li>mean = (b + a) / 2</li>
 * <li>variance = (b - a)^2 / 12</li>
 * </ul>
 *
 * @author Miguel A. Figueroa-Villanueva
 */
public class ContinuousUniformGenerator implements RandomGenerator
{
  private double lowerBound;
  private double upperBound;

  /**
   * Constructor.
   *
   * @param lowerBound Minimum possible number generated.
   * @param upperBound Maximum possible number generated.
   */
  public ContinuousUniformGenerator(final double lowerBound,
                                    final double upperBound)
  {
    ContinuousUniformGenerator.checkBounds(lowerBound, upperBound);

    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  /**
   * Generates a continuous uniform distribution random number.
   *
   * @return The generated random number in [a,b].
   */
  @Override
  public final double generate()
  {
    return ContinuousUniformGenerator.generate(this.lowerBound,
                                               this.upperBound);
  }

  /**
   * Generates a continuous uniform distribution random number.
   *
   * <p>Generator using the inverse-transformation method:
   *
   * <pre>x = a + [(b - a) * U]</pre>
   *
   * @param lowerBound Minimum possible number generated.
   * @param upperBound Maximum possible number generated.
   * @return The generated random number in [a,b].
   */
  public static double generate(final double lowerBound,
                                final double upperBound)
  {
    ContinuousUniformGenerator.checkBounds(lowerBound, upperBound);

    return
      lowerBound
      + (upperBound - lowerBound) * RANDOM_NUMBER_GENERATOR.nextDouble();
  }

  private static void checkBounds(final double lowerBound,
                                  final double upperBound)
  {
    if (upperBound < lowerBound)
    {
      throw new IllegalArgumentException(
        "Upper bound < Lower Bound: " + upperBound + " < " + lowerBound);
    }
  }
}

