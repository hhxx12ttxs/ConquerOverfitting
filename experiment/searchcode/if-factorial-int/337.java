package org.sample;

public class TailRecursion {

    public static void main(String[] args) {
	System.out.println(new TailRecursion().factorial(3));
    }
    
    private int factorial(int n, int result){
	if(n==0){
	    return result;
	}
	return factorial(n-1, n*result);
    }
    
    private int factorial(int n){
   	return factorial(n, 1);
       }

}

