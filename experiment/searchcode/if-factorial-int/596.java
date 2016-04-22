package com.recursion;


public class Factorial {


    public static int calculateFactorial(int value){
        if(value<=1){
            return 1;
        }
        else{
            return calculateFactorial(value-1)*value;
        }
    }
}

