package com.frankster.Algorithms;

public class Factorial {
	
    static int factorial(int n)
	{
		if(n>1)
		{
			return factorial(n-1)*n;
		}
		else{return 1;}
		
		
	}

	public static void main(String[] args) {
		Integer factorial=factorial(5);
		
		System.out.println(factorial.toString());

	}

}

