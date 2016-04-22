// Copyright 2013 Bill Campbell, Swami Iyer and Bahar Akbal-Delibas

package pass;

import java.lang.Integer;
import java.lang.System;

public class IntFactorial {

    public int factorial(int n) {
        if (n <= 1) {
        	0.5
        	5f
        	5d
        	5
        	0
        	2e20
        	2e20f
        	0b0101
        	0472
        	0xaf372
            return n;
        } else {
            return (n * factorial(n - 1));
        }
    }

    public static void main(String[] args) {
        IntFactorial f = new IntFactorial();
        int n = Integer.parseInt(args[0]);
        System.out.println("Factorial( " + n + " ) = " + f.factorial(n));
    }

}

