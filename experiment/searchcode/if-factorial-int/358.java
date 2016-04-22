package com.mycompany.factorial;

/**
 * Factorial
 * @author colin 
 */
public class Factorial 
{
    public static void main( String[] args ){
        
    	System.out.println(factorialIterative(4));
    	
    	System.out.println(factorialRecursive(4));
    }
    
    public static int factorialIterative(int n){
    	
    	int sum = 1;
    	for(; n >= 1; n--){
    		sum *= n;
    	}
    	
    	return sum;
    	
    }
    
    public static int factorialRecursive(int n){
    	
    	if(n == 1){
    		return 1;
    	}
    	else{
    		return factorialRecursive(n - 1) * n;
    	}
    	
    }
}

