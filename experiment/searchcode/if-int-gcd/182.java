package com.gmail.dailyefforts.algrithm.gcd;

public class RecursivelyDcdResolver {
	public static int getGcd(int a, int b) {
		int smaller = a < b ? a : b;
		int bigger = a > b ? a : b;

		int gcd = -1;

		if (smaller == 0) {
			gcd = bigger;
		} else if (bigger == 0) {
			gcd = smaller;
		} else {
			gcd = getGcd(smaller, bigger % smaller);
		}
		return gcd;
	}
}

