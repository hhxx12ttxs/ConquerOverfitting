package com.etnos.algorithms;

/**
 * This java program calculates a factorial for a given value.
 * <p>
 * 5! = 5 * 4 * 3 * 2 * 1 = 120
 */
public class Factorial {
    public static void main(String[] args) {
        Factorial factorial = new Factorial();
        int value = 5;
        int result = factorial.factorialIterative(value);
        System.out.println("Factorial iterative for a value " + value + " is " + result);

        result = factorial.factorialRecursive(value);
        System.out.println("Factorial recursive for a value " + value + " is " + result);
    }

    /**
     * Calculate factorial iterative.
     * <p>
     * Complexity: O(n)
     *
     * @param value given value
     * @return factorial of a given value
     */
    protected int factorialIterative(int value) {
        int result = 1;
        for (int i = 1; i <= value; i++) {
            result = result * i;
        }

        return result;
    }

    /**
     * Calculate factorial recursive.
     * <p>
     * Complexity: O(n)
     *
     * @param value given value
     * @return factorial of a given value
     */
    protected int factorialRecursive(int value) {
        if (value <= 1) {
            return 1;
        }
        return value * factorialRecursive(value - 1);
    }
}

