package factorial;

/**
 *
 * @author Dev
 */
public class Factorial {

    /**
     * @param n --- integer non-negative number.
     * @return its factorial.
     */
    public static int factorial(int n) {
        if (n < 0) {
            throw new FactorialInvalidArgument();
        }
        int result = 1;
        for (int i = 2; i <= n; ++i) {
            result *= i;
        }
        return result;
    }
}

