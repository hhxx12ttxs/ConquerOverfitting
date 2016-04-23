package com.project;

public class FactorialUsesRecursion {
	public  int factorial(int n) {
		if (n == 0)
			return 1;
		if (n <= 1)
			return n;
		else
			return (n * factorial(n - 1));
	}
}

