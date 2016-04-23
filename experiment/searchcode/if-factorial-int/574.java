package com.cainc.jlou.fundamental;

public class Factorial {

	// from n to 1
	public int doFactorial(int n) {

		if (n < 0) {
			return -1;
		}

		if (n == 1) {
			return 1;
		}

		return n * doFactorial(n - 1);
	}

	// from 1 to n
	public int doFactorial(int curr, int n) {

		if (curr > n) {
			return 1;
		}

		return curr * doFactorial(curr + 1, n);
	}

	public int doFactorialIterative(int n) {

		int i = 1;
		int res = 1;
		// or you can just use a for loop
		while (i <= n) {
			res *= i;
			i++;
		}

		return res;

	}

	public static void main(String[] args) {
		System.out.println(new Factorial().doFactorial(10));
		System.out.println(new Factorial().doFactorial(1, 10));
		System.out.println(new Factorial().doFactorialIterative(10));
	}

}

