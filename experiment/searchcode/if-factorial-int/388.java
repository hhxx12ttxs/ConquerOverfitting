package com.interview.algorithm;


public class Factorial {
	public static void main(String[] args) {
		System.out.println("Factorial: " + findFactorial(4));
	}
	
	private static int findFactorial(int n) {
		if(n <= 0) {
			return 1;
		} else {
			return n * findFactorial(n-1);
		}
	}
}

