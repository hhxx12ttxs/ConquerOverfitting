package com.vesatile.core.algorithm;

public class Factorial {
	public int factorial(int i) {
		if (i == 0) {
			return 1;
		} else if (i > 0) {
			return i * factorial(i - 1);
		} else {
			throw new ArithmeticException("Nagitive value is not support");
		}
	}
}

