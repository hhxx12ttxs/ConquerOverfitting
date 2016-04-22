package recursion;

import static recursion.TailCalls.call;
import static recursion.TailCalls.done;

/**
 * @author Tomasz Lelek
 * @since 2014-06-02
 */
public class Factorial {
    public static TailCall<Integer> factorialTailRec(final int factorial, final int number) {
        if (number == 1)
            return done(factorial);
        else
            return call(() -> factorialTailRec(factorial * number, number - 1));
    }

    public static void main(String[] args) {
        factorialTailRec(1, 2).invoke();
        System.out.println(factorialTailRec(1, 5).invoke());
        System.out.println(factorialTailRec(1, 20000).invoke());

        System.out.println(factorial(5));
        System.out.println(factorial(20000));
    }


    public static int factorial(final int number) {
        return factorialTailRec(1, number).invoke();
    }

}

