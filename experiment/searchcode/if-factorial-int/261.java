package com.aamend.dsa.numeric;

import java.util.Random;

public class Factoriel {

	private final static Random rand = new Random();
	private static int count1 = 0;
	private static int count2 = 0;
	
	public static void main(String[] args) {
		long randLong = getRandomLong(50L);
		System.out.println("Random number is " + randLong
				+ " its factoriel is " + factorielObvious(randLong) + "("+count1+") | "
				+ factorielRecursive(randLong)+" ("+count2+")");

	}

	private static long getRandomLong(long max) {
		long number = 1 + (long) (rand.nextDouble() * (max - 1));
		return number;
	}

	private static long factorielRecursive(long number) {
		// The factorial of 2 is 2* the factorial of 1, similarly the factorial
		// of 3 is 3* the factorial of 2 and so on.
		// factorial of n is n* factorial of n - 1
		if (number == 0 || number == 1) {
			return 1;
		} else {
			count2++;
			return number * factorielRecursive(number - 1);
		}

	}

	private static long factorielObvious(long number) {

		long fact = 1;
		for (long i = number; i >= 1; i--) {
			count1++;
			fact *= i;
		}
		return fact;
	}

}

