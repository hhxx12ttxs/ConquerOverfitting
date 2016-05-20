package com.gmail.dailyefforts.algrithm.gcd;

public class EasyGcdResolver {
	public static int getGcd(int a, int b) throws Exception {

		if (a < 0 || b < 0) {
			throw new Exception("LessThanZeroException");
		}

		int smaller = a < b ? a : b;
		int bigger = a > b ? a : b;

		int gcd = -1;

		if (smaller == 0) {
			gcd = bigger;
		} else if (bigger == 0) {
			gcd = smaller;
		} else {
			for (int i = 1; i <= smaller; i++) {
				if (a % i == 0 && b % i == 0) {
					gcd = i;
				}
			}
		}

		return gcd;
	}
}

