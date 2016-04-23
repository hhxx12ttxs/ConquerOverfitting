package com.interviewcake;

/**
 * Find the factorial of a number using recursion
 */
public class Factorial {
	
	
	public static void main(String[] args) {
		int n = 5;
		int factorial = factorial(n);
		System.out.println("factorial(" + n + ") = " + factorial);
	}

	private static final int factorial(int n) {
		if (n < 1) {
			return 1;
		}

		return n * factorial(n - 1);
	}
}

