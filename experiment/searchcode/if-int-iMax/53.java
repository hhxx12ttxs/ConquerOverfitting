/**
 * 
 */
package p69;

import peutils.Primes;

/**
 * @author LG
 * 
 */
public class P69_TotientMaximum {

	private static int[] primeNumbers = null;

	private static int phi(int number) {
		int i = 1;
		int phi = 1;

		while (i < primeNumbers.length && number > 1) {
			int prime = primeNumbers[i];
			int count = 0;

			while (number % prime == 0) {
				count++;
				number /= prime;
			}

			if (count > 0) {
				phi = phi * (prime - 1) * (int) Math.pow(prime, count - 1);
			}

			i++;
		}

		if (number > 1) {
			phi = phi * (number - 1);
		}

		return phi;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Primes primes = new Primes(10000);
		primeNumbers = primes.getPrimes();

		double max = -1;
		int imax = -1;

		for (int i = 2; i <= 1000000; i++) {
			double current = i * 1.0 / phi(i);

			if (current > max) {
				max = current;
				imax = i;
			}
		}

		System.out.println(imax + " = " + max);
		System.out.println(phi(imax));
	}

}

