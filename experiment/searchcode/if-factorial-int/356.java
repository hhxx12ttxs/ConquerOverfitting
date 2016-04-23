package euler;

import java.math.BigInteger;

public class P015LatticePaths {

	public static BigInteger factorial(int n) {
		if (n > 1)
			return BigInteger.valueOf(n).multiply(factorial(n - 1));
		return BigInteger.ONE;
	}

	public static BigInteger combination(int n, int r) {
		// System.out.println(factorial(n) + " " + factorial(r) + " " + factorial(n - r));
		return factorial(n).divide(factorial(r).multiply(factorial(n - r)));

	}

	public static void main(String args[]) {
		final int N = 40, R = 20;
		System.out.println(combination(N, R));
	}
}

