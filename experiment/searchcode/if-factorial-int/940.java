package com.aromero.exception;

/**
 *
 * @author aromero
 */
public class NonTerminatingRecursion {
    static int factorial(int n){
        int result = 0;
        // if(n == 0) return 1;
        result = factorial(n-1) * n;
        return result;
    }
    public static void main(String[] args) {
        System.out.println("Factorial of 4 is: " + factorial(4));
    }
}

