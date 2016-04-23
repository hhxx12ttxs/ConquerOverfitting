package com.blogspot.zelinskyi.algorithms;

/**
 * Created by IntelliJ IDEA.
 * User: Dmytro Zelinskyi
 * Date: Jan 18, 2012
 * Time: 2:51:53 PM
 */
public class Factorial {

    public static int factorial(int i) {

        if(i == 0 || i == 1) {
            return 1;
        }
        return i*factorial(i-1);
    }
}

