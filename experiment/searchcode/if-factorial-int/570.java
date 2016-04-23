package com.company;


import java.util.Scanner;

public class CalculateFactorial {
    public static int factorial = 1;
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        int n = scan.nextInt();
        getFactorial(n);
        System.out.println(factorial);
    }
    public static void getFactorial(int n){
        if (n < 1){
           return;
        }
        factorial*=n;
        n--;
        getFactorial(n);
    }
}

