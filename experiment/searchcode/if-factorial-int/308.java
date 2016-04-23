package org.bitbucket.athena.arithmetical;

import java.math.BigInteger;

/**
 * Factorial
 * http://en.wikipedia.org/wiki/Factorial
 */
public class Factorial {
    public static BigInteger factorial(int n) {
        if(isNegativeNumber(n)) throw new FactorialOfNegativeNumberException();
        return calcFactorial(n);
    }

    private static BigInteger calcFactorial(int n) {
        BigInteger factorial = BigInteger.ONE;
        for (int i = 1; i <= n; i++)
            factorial = factorial.multiply(BigInteger.valueOf(i));
        return factorial;
    }

    private static boolean isNegativeNumber(int n) {
        return n < 0;
    }

    public static class FactorialOfNegativeNumberException extends RuntimeException {}
}

