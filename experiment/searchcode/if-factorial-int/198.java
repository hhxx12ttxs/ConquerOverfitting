package com.shanekumpf.lab4;

import csci130.*;
/**
 * Computes the Nth term of a factorial using a recursive and explicit
 * method.
 * @author Shane Kumpf
 * @version 0.1 09/04/12
 * @since 1.6
 *
 */
public class Factorial {
	
	/**
	 * Computes the factorial (recursive)
	 * @param n		the term to find
	 * @return long	the computed value
	 */
	static long factorialRecurse(int n) {
        if (n == 0 || n == 1) {
            return 1;
        }
        return factorialRecurse(n - 1) * n;
    }

	/**
	 * Computes the factorial (explicit)
	 * @param n		the term to find
	 * @return long	the computed value
	 */
    static long factorialLoop(int n) {
        if (n == 0 || n == 1) {
            return 1;
        }
        int accum = 1;
        for(int i=n; i>0; i--) {
            accum *= i;
        }
        return accum;
    }
    
    public static void main(String[] args) {
        System.out.print("Enter an int: ");
        int resp = KeyboardReaderNG.readInt();
        System.out.println("\nYou entered: " + resp);
        System.out.println("Recursive: " + factorialRecurse(resp));
        System.out.println("Explicit: " + factorialLoop(resp));
    }

}

