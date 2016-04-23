package com.marcus.hello.exercises.exercise4;

/**
 * @author marcus
 * two different methods for caluclating factorial of a number
 *
 */
public class Factorial {


    /**
     *
     * @param n - must be >= 0
     * @return the factorial of n or -1 if invalid
     */
    public static int factorial (int n){

        if ( n < 0) return -1;

        int factorial =1;
        if ( n <= 1 ) return 1;

        for (int i = 2 ; i <= n; i++  ){
            factorial *= i;
        }
        return factorial;

    }

    public static int recursiveFactorial(int n){

        if ( n < 0){
            return -1;
        }

        if (n == 0 || n == 1 ){
            return 1;
        }

        return  n * recursiveFactorial(n-1);

    }


}

