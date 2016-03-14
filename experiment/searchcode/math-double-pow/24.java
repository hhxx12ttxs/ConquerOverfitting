package com.leetcode.math;

/**
 * Implement pow(x, n).
 *
 * Created by Xiaomeng on 7/22/2014.
 */
public class Pow {
    public static double pow(double x, int n) {
        if(n == 0) return 1;
        if(n == 1) return x;
        double tmp = pow(x, Math.abs(n)/2);
        double result = n % 2 == 0 ? tmp * tmp : x * tmp * tmp;
        return n > 0 ? result : 1/result;
    }

    public static void main(String[] args){
        System.out.println(pow(0.00001, 2147483647));
    }
}

