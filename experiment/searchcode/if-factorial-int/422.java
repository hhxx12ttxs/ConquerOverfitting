package general;

/**
 * User: Oleg_Kariakin
 * Date: 3/23/16
 */
public class Factorial {

    /**
     * @throws IllegalArgumentException in case factorial is below zero
     * @throws IllegalStateException    in case of overflow
     */
    public static long factorialLoop(int factorial) throws IllegalArgumentException, IllegalStateException {
        if (factorial < 0) {
            throw new IllegalArgumentException("general.Factorial can't be negative");
        } else if (factorial == 1 || factorial == 0) {
            return 1L;
        } else {
            long result = 1L;
            for (int i = 1; i <= factorial; i++) {
                result = result * i;
                if (Long.MAX_VALUE / i < result) {
                    throw new IllegalStateException("general.Factorial overflows long on iteration: " + i);
                }
            }
            return result;
        }
    }

    /**
     * @throws IllegalArgumentException in case factorial is below zero
     * @throws IllegalStateException    in case of overflow
     */
    public static long factorialRecursion(int factorial) throws IllegalArgumentException, IllegalStateException {
        if (factorial < 0) {
            throw new IllegalArgumentException("general.Factorial can't be negative");
        } else if (factorial == 1 || factorial == 0) {
            return 1L;
        } else {
            long result = factorialRecursion(factorial - 1) * factorial;
            return result;
        }
    }

}

