package com.aamend.dsa.numeric;

import java.math.BigInteger;
import java.util.Random;

public class PrimeNumber {

	public static int numbers = 20;
	public static int maxIterations = 10;
	private final static Random rand = new Random();

	public static void main(String[] args) {

		BigInteger big = BigInteger.valueOf(1L);
		for (int i = 0; i < numbers; i++) {

			big = big.add(BigInteger.ONE);
			System.out.println("Is " + big.longValue() + " prime ? "
					+ isPrimeFermat(big, maxIterations) + " | "
					+ isLongAPrimeNumber(big.longValue())+ " | "+ isPrimeObvious(big.longValue()));
		}

	}

	private static BigInteger getRandomFermatBase(BigInteger n) {
		// ask for a random integer but reject it if it isn't
		// in the acceptable set.
		while (true) {
			final BigInteger a = new BigInteger(n.bitLength(), rand);
			// must have 1 <= a < n
			if (BigInteger.ONE.compareTo(a) <= 0 && a.compareTo(n) < 0) {
				return a;
			}
		}
	}

	public static boolean isPrimeFermat(BigInteger n, int maxIterations) {

		if (n.equals(BigInteger.ONE) || n.equals(BigInteger.ZERO)) {
			// 0 and 1 are not prime
			return false;
		}

		for (int i = 0; i < maxIterations; i++) {

			// Suppose we wish to determine if n = 221 is prime.
			// Randomly pick 1 <= a < 221, say a = 38.
			// We check the above equality and find that it holds:
			// 38^{220} = 1 mod(221)
			BigInteger a = getRandomFermatBase(n);
			a = a.modPow(n.subtract(BigInteger.ONE), n);
			if (!a.equals(BigInteger.ONE)) {
				// We are sure this number is not prime
				return false;
			}
		}

		// This number is a prime or a false positive
		return true;
	}

	public static boolean isLongAPrimeNumber(long n) {

		if (n == 0 || n == 1) {
			return false;
		}

		for (int i = 0; i < maxIterations; i++) {
			long random = getRandomLong(n);
			long fermat = (long) Math.pow(random, n - 1);
			fermat = fermat % n;
//			System.out.println(random + "^(" + n + "-1) % " + n + " = "
//					+ fermat);
			if (fermat != 1) {
				return false;
			}
		}
		return true;
	}

	public static long getRandomLong(long max) {
		long number = 1 + (long) (rand.nextDouble() * (max - 1));
		return number;
	}

	public static boolean isPrimeObvious(long n) {

		// Once we reach 10, which is \sqrt {100}, the divisors just flip around
		// and repeat
		for (int i = 2; i <= Math.sqrt(n); i++) {

			// We can also eliminate all the even numbers greater than 2, since
			// if an even number can divide n, so can 2
			if (i > 2 && i % 2 == 0) {
				continue;
			}

			if (n % i == 0) {
				return false;
			}
		}

		return true;
	}
}

