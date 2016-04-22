package org.eyal.brainers.java.factorial;

public class RecursiveFactorial implements Factorial {

	public RecursiveFactorial() {
	}

	@Override
	public int factorial(int number) {
		if (number == 0 || number == 1) {
			return 1;
		}
		return number * factorial(number-1);
	}
}

