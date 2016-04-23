package com.company;

public class Factorial {

    public static void main(String[] args) {

        int i = 5;

        System.out.println(factorial(i));
    }

    public static int factorial(int a) {
        if (a == 1) {
            return 1;
        }
        return a * factorial(a-1);
    }



}

