package com.bayamp.basic.programs;

public class FactorialRecursion {

	public static void main(String[] args) {
		int a = 10;
		int factorial = fact(a);
		System.out.println("The factorial is: " + factorial);

	}

	public static int fact(int b) {
		if (b <= 1) {
			return 1;
		}

		else {
			return b * fact(b - 1);
		}
	}
}

