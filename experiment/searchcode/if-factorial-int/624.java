package com.java.utills.programs;

public class FactorialUtils {

	public static void factorialByIteration(int n) {

		int result = 1;
		for (int i = 2; i <= n; i++)
			result = result * i;
		System.out.println(result);
	}

	public static int factorialByRecursion(int n) {

		if (n == 0) {
			return 1;

		}
		return n * factorialByRecursion(n - 1);

	}

}

