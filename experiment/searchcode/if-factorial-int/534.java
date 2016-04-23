/**
 * Created on 3/11/16
 * Written by: greg
 *
 * @version 1.0
 */
package com.example.recursion;

public class Factorial {

    public static int factorial(int n) {
        if (n == 0) return 1;
        return n * factorial(n - 1);
    }
}

