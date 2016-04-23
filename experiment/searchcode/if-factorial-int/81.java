package com.prateek.Permutation;

public class Factorial {

	public int calculateFactorial(final int number) {
		int factorial = 1;
		if (number > 1) {
			factorial = number * calculateFactorial(number - 1);
		}
		return factorial;
	}
}

