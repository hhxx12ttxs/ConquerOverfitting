package com.github.crazcalm.Recursive;

/**
 * Created by marcuswillock on 5/15/15.
 */
public class Factorial {
    public static void main(String[] args){
        int test = Factorial.factorial(10);
        System.out.println("Answer: " + test);
    }

    public Factorial(int num){

    }

    public Factorial(long num){

    }
    public static int factorial(int num){

        System.out.println("num: " + num);
        if(num < 1){
            return 1;
        }
        else{
            return num * factorial(num - 1);
        }
    }

    public  static long factorial (long num){

        if(num < 1){
            return 1;
        }
        else{
            return num * factorial(num - 1);
        }
    }
}

