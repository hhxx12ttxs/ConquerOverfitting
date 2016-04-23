package com.jarvis.tutorials.examples;

public class Factorial {

	public static void main(String[] args) {

		int n = 10, fact;

		fact = factorial(n);
		System.out.println(fact);
		fact = factorialRecursion(n);
		System.out.println(fact);
	}

	public static int factorial(int n) {

		int fact = 1;
		for (int i = 1; i <= n; i++) {
			fact = fact * i;
		}
		return fact;
	}

	public static int factorialRecursion(int n) {         

		if (n == 0)
			return 1;
		return n * factorialRecursion(n - 1);
	}
}

