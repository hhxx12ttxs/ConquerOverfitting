package com.abc.example;

//factorial n! = n*(n-1)!
public class Factorial {
	
	//recursive
	public static int factorial(int n){
		if (n<=1)
			return n;
		else
			return n*factorial(n-1);
	}
	
	//loop
	public static int factorial_forloop(int n){
		if (n<=1)
			return n;
		
		int fact =1;
		for (int i=1; i<=n; i++){
			fact *= i;
		}
		return fact;
	}
	
	public static void main(String args[]){
		for (int i=0; i<10; i++){
			System.out.printf("%d!=%d\n", i, factorial(i) );
			System.out.printf("%d!=%d\n", i, factorial_forloop(i) );
		}
	}
}

