package com.amaresh;

public class Factorial {

    public int factorial(int n) {
        if(n == 0 || n == 1) {
            return 1;
        }
        
        return ( n * factorial(n-1) );
    }
}


