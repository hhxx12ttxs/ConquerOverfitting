package com.example;

public class Recursion {

	public static int Factorial(int n){
		if (n == 0 || n == 1)
			return 1;
		return n * Factorial(n -1);
	}
	public static int FactorialTail(int a,int b){
		if (a == 0)
			return b;
		return FactorialTail(a -1,a * b);
	}
}

