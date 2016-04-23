package com.general;

public class FactorialTailcall {
  static int factorial(int n){
	  if(n < 1)
		  return 0;
	  else if(n == 1)
		  return 1;
	  else
		  return n*factorial(n-1);
  }
  
  static int tailFactorial(int n, int agg){
	  if(n < 1)
		  return 0;
	  else if (n == 1)
		  return agg;
	  else
		  return tailFactorial(n-1, n*agg);
  }
}

class Runner{
	public static void main(String[] args){
		System.out.println(FactorialTailcall.factorial(10));
		System.out.println(FactorialTailcall.tailFactorial(10, 1));
	}
}

