package by.htp.krozov.sample.method;

import java.util.Random;

public class SimpleRecursiveMethodMain {

    public static void main(String[] args) {
        int n = new Random().nextInt(16);
        System.out.printf("Factorial of %d = %,d", n, factorialRecursive(n))
                .println();
    }

    /**
     * Compute factorial of non negative value.
     * Recursive implementation.
     */
    public static int factorialRecursive(int n) {
        return (n == 0 || n == 1) ? 1 : n * factorialRecursive(n - 1);
    }

    /**
     * Compute factorial of non negative value.
     * Not recursive implementation.
     */
    public static int factorial(int n) {
        if (n == 0 || n == 1) {
            return 1;
        } else {
            int factorial = 1;
            for (int i = 2; i <= n; i++) {
                factorial *= 2;
            }
            return factorial;
        }
    }
}

