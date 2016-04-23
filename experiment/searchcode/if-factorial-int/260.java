package com.aman.training;

public class Factorial {

  public static void main(String[] args) {
		int n =5;
		Factorial fact = new Factorial();
		int total= fact.factorial(n);
		System.out.println(total);
	}
	
	public int factorial(int n)
	{
		int total =1;
		if (n==0)
			return 1;
		else 
			total = n * factorial(n-1);
		return total;
		
	}
}

