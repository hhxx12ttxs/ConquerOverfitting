package solutions;

/**
 * Factorial computation class.
 */
public class Factorial {
    /**
     * Computes the {@code n}! (n factorial).  0! is defined as 1.  n! is
     * defined as the product of all the integers in {@code [1,n]}.
     *
     * Requires: {@code n >= 0}
     *
     * @param n Factorial to compute.
     * @return {@code n}!
     * @throws IllegalArgumentException if {@code n} is negative.
     */
    public static int fac(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be positive, was:" + n);
        } else if (n == 0 || n == 1) {
            return 1;
        } else {
            return n * fac(n - 1);
        }
    }
}

