package org.gt.prog.maths;

/**
 * Factorial of Integer
 */
public class FactorialInt {
	public int factorial(int x) {
		if (x <= 0)
			return 1;
		else
			return x * factorial(x - 1);
	}
}

