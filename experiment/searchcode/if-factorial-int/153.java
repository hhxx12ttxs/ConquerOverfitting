package hw4.recursion;

/**
 * Given n of 1 or more, return the factorial of n, which is n * (n-1) * (n-2) ...
 * 1. Compute the result recursively (without loops).
 factorial(1) → 1
 factorial(2) → 2
 factorial(3) → 6
 * */

public class Recursion01 {

    public static void main(String[] args) {

        System.out.println(factorial(1));
        System.out.println(factorial(2));
        System.out.println(factorial(3));
        System.out.println(factorial(4));
        System.out.println(factorial(5));
        System.out.println(factorial(6));
        System.out.println(factorial(7));

    }

    public static int factorial(int n) {

        int result;

        if (n == 1) {
            return 1;
        }

        result = factorial(n - 1) * n;

        return result;
    }

}

