package com.recursion;

public class Factorial {
	public static void main(final String[] args) {
		System.out.println("10!::"+printFactorial(10));
		System.out.println("4!::"+printFactorial(4));
	}

	private static int printFactorial(final int i) {
		if(i==0){
			return 1;
		}

		return i*printFactorial(i-1);
	}
}

