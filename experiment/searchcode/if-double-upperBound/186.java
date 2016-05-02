package org.jeffkubina.utils;

/**
 * The class BoundedRandomDouble generates random doubles greater than or equal
 * to MinValue but less than the UpperBoundValue, that is, the random value
 * satisfies MinValue <= random value < UpperBoundValue.
 */
public class BoundedRandomDouble
{
  /**
   * Instantiates a new bounded random double to generate random doubles such
   * that MinValue <= random-value < UpperBoundValue.
   * 
   * @param MinValue
   *          The minimum value of the double that can be generated.
   * @param UpperBoundValue
   *          The upper bound of the value of the double that can be generated.
   */
  public BoundedRandomDouble(final double MinValue, final double UpperBoundValue)
  {
    double upperBound;

    /*
     * make sure the smaller value is used as the offset.
     */
    if (MinValue < UpperBoundValue)
    {
      offset = MinValue;
      upperBound = UpperBoundValue;
    } else
    {
      upperBound = MinValue;
      offset = UpperBoundValue;
    }

    /*
     * compute how to scale the values.
     */
    scale = upperBound - offset;
  }

  /**
   * Instantiates a new bounded random double to generate a random double such
   * that 0 <= random-value < 1.
   */
  public BoundedRandomDouble()
  {
    scale = 1;
    offset = 0;
  }

  /**
   * Next returns a random double in the specified range.
   * 
   * @return Returns a random double in the specified range.
   */
  public double next()
  {
    return scale * Math.random() + offset;
  }

  /**
   * The two values needed to calculate the random value in the specified range.
   */
  private double scale, offset;
}

