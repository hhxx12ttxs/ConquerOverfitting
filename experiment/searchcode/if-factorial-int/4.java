package jp.ne.voqn.calcurator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import static java.lang.Math.*;

/**
 * Basic calculation of factorial
 * @author VoQn
 */
public class Factorial {

    /**
     * Error message when argument is inacceptable negatve number.
     */
    private static String ERROR_MESSAGE_ARGUMENT_IS_NEGATIVE;

    static {
        ERROR_MESSAGE_ARGUMENT_IS_NEGATIVE = "argument not acceptable negative number";
    }

    /**
     *
     * Cheking argument number is negative number
     * @param n
     * @exception IllegalArgumentException
     * argument is negative number, throw it
     */
    static void checkNegativeIllegalArgument(int n) {
        if (n < 0) {
            String message = ERROR_MESSAGE_ARGUMENT_IS_NEGATIVE;
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * calcuration n! / m!
     * = n * (n - 1) * ... / m * (m - 1) * ...
     * = n * (n - 1) * .. * (n - m + 1)
     * @param n
     * @param m
     * @return n! / m!
     */
    public static BigInteger factInRange(int n, int m) {
        checkNegativeIllegalArgument(n);
        checkNegativeIllegalArgument(m);
        BigInteger result = BigInteger.ONE;
        if (n > m) {
            for (int i = n, l = m; i > l; i--) {
                result = result.multiply(BigInteger.valueOf(i));
            }
        }
        return result;
    }

    /**
     * long integer value
     * factorial(n) == n! = 1 * 2 * 3 * ... * (n-1) * n
     * @param n natural number
     * @return n! (<samp>0! = 1, 1! = 1</samp>)
     * @exception IllegalArgumentException
     * If n &lt 0, throw it.
     */
    public static long factorial(int n) {
        checkNegativeIllegalArgument(n);
        return fact(n).longValue();
    }

    /**
     * BigInteger Object
     * factorial(n) == n! = 1 * 2 * 3 * ... * (n-1) * n
     * @param n natural number
     * @return n! (<samp>0! = 1, 1! = 1</samp>)
     * @exception IllegalArgumentException
     * If n &lt 0, throw it.
     */
    public static BigInteger fact(int n) {
        checkNegativeIllegalArgument(n);
        return factInRange(n, 0);
    }

    /**
     * double float value
     * calculate Stiling's approximation
     * @param n
     * @return
     */
    public static double approximation(int n) {
        checkNegativeIllegalArgument(n);
        return approx(n).doubleValue();
    }

    /**
     * BigDecimal Object
     * calculate Stilling's approximation
     * @param n
     * @return
     */
    public static BigDecimal approx(int n) {
        checkNegativeIllegalArgument(n);
        BigDecimal result = BigDecimal.ONE;
        double[] values = {sqrt(2 * PI * n), pow(n, n), pow(E, -n)};
        if (n > 0) {
            for (double val : values) {
                result = result.multiply(BigDecimal.valueOf(val));
            }
        }
        return result.round(MathContext.DECIMAL32);
    }
}

