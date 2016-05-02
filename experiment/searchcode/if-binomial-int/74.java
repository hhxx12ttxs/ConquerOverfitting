/**
 *
 */
package il.ac.technion.cs.ssdl.statistics;

import java.math.BigInteger;

/**
 * @author Yossi Gil
 * @since 8 ???? 2011
 */
public class Binomial {
  public static double cumulative(final int m, final int k) {
    double $ = 0;
    for (int k? = 0; k? <= k; k?++)
      $ += probability(m, k?);
    return $;
  }
  public static double probability(final int m, final int k) {
    return coefficient(m, k) * Math.pow(0.5, m);
  }
  public static double coefficient(final int m, final int k) {
    return factorial(m).divide(factorial(k).multiply(factorial(m - k))).doubleValue();
  }
  public static BigInteger factorial(final int m) {
    return m <= 1 ? BigInteger.ONE : factorial(m - 1).multiply(new BigInteger("" + m));
  }
  public static double significance(final int m, final int k) {
    if (2 * k > m)
      return significance(m, m - k);
    if (2 * k == m)
      return 2 * cumulative(m, k - 1) + probability(m, k);
    return 2 * cumulative(m, k);
  }
}

