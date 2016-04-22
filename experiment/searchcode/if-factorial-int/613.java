package com.rmd.personal.yahtzee.core;

public final class Common {

    private Common() {
    }

    /**
     * Deals only with non-negative n and k.
     * @throws IllegalArgumentException if n < 0 or if n < k
     * @param n int
     * @param k int
     * @return int
     */
    public static int binomialCoefficient(int n, int k) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be > 0.");
        }

        if (n < k) {
            throw new IllegalArgumentException("n must be >= k.");
        }

        int numerator = factorial(n);
        int denominator = factorial(k) * factorial(n - k);
        return numerator / denominator;
    }

    private static int factorial(int i) {
        if (i == 0) {
            return 1;
        }

        int factorial = 1;
        while (i-- > 0) {
            factorial += factorial * i;
        }
        return factorial;
    }
}

