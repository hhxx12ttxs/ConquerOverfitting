package com.jrg.factorial;

/**
 * Write a java program to print the factorial of the given number.
 *
 * @author "Jigar Gosalia"
 *
 */
public class Factorial {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		int number = 3;

		System.out.println("Recursively Factorial of " + "(" + number + ")     : "
				+ factorialRecursive(number));

		System.out.println("Non-Recursively Factorial of " + "(" + number + ") : "
				+ factorialNonRecursive(number));
	}

	/**
	 * @param n
	 * @return
	 */
	public static int factorialNonRecursive(int n) {

		int factorial = 1;

		if (n == 0) {
			return factorial;
		}

		while (n > 0) {
			factorial = n * factorial;
			n--;
		}
		return factorial;
	}

	/**
	 * @param n
	 * @return
	 */
	public static int factorialRecursive(final int n) {
		if (n == 0) {
			return 1;
		} else {
			return (n * factorialRecursive(n - 1));
		}
	}
}
