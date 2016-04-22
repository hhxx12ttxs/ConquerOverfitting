package com.Litvinenko;

public class Factorial {

    public static void main(String[] args) {

        int result = factorial(4);
        System.out.println(result);
    }

    public static int factorial(int n) {
        if (n <= 1) return 1;
        return factorial(n - 1) * n;
    }
}

