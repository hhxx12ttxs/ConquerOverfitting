package com.jayant.recursion;

/**
 * Created by jayant on 1/19/2016.
 */
public class FibonacciDemo {

    public static void main(String[] args) {
        int i = 4;
        System.out.println(factorial(i));
    }

    private static int factorial(int i) {

        if( i == 1 || i == 2) {
            return 1;
        }

        return factorial(i - 1) + factorial(i - 2);
    }

}

