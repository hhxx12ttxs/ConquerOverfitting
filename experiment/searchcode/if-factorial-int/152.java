package com.jeffhobbsonline;

/**
 * Factorial Using Recursion
 *
 */
public class Factorial 
{
    public static void main( String[] args )
    {
    	int n = 6;
    	int k = factorial(n);
        System.out.println("Factorial of " + n + " equals: " + k);
    }
    
    public static int factorial(int n){
    	if (n == 1)
    		return 1;
    	else
    		return n  * factorial(n-1);
    }
}
