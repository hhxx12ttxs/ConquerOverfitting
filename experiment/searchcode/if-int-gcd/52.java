/**
 * 
 */
package p243;

import java.util.ArrayList;

/**
 * @author Son-Huy TRAN
 * 
 */
public class P243_Resilience {

	public static int greatestCommonDivisor(int a, int b) {
		while (b > 0 && a > 0) {
			if (a < b) {
				b %= a;
			} else {
				a %= b;
			}
		}

		return a + b;
	}

	private static Fraction resilience(int number) {
		Fraction result = new Fraction(1, 1);
		int resilient = 1;

		for (int i = 2; i < number; i++) {
			if (greatestCommonDivisor(i, number) == 1) {
				resilient++;
			}
		}

		int gcd = greatestCommonDivisor(resilient, number - 1);
		result.setNumerator(resilient / gcd);
		result.setDenominator((number - 1) / gcd);

		return result;
	}

	private static Integer[] primes = generatePrimes(1000);

	private static Integer[] generatePrimes(int max) {
		ArrayList<Integer> results = new ArrayList<Integer>();
		results.add(2);

		for (int i = 3; i <= max; i++) {
			double sqrt = Math.sqrt(i);
			boolean isPrime = true;

			for (int j = 0; results.get(j) <= sqrt; j++) {
				if (i % results.get(j) == 0) {
					isPrime = false;
					break;
				}
			}

			if (isPrime) {
				results.add(i);
			}
		}

		Integer[] resultsArray = new Integer[results.size()];
		return results.toArray(resultsArray);
	}

	private static Fraction resilience1(int number) {
		int tempNumber = number;
		int[] product = new int[primes.length];

		for (int i = 0; i < product.length; i++) {
			product[i] = 0;
		}

		int i = 0;
		int sqrt = (int)Math.sqrt(number);
		
		while (primes[i] <= sqrt
				&& number > 1) {
			if (number % primes[i] == 0) {
				number /= primes[i];
				product[i]++;
			} else {
				i++;
			}
		}
		
		int phi = 1;
		
		if (number > 1) {
			phi *= number - 1;
		}
		
		for (int j = 0; j < product.length; j++) {
			if (product[j] > 0)	{
				phi *= (primes[j] - 1) * Math.pow(primes[j], product[j] - 1);
			}
		}
		
		return new Fraction(phi, tempNumber - 1);
	}

	public static void main(String[] args) {
		Fraction fraction1 = new Fraction(4, 11);
		Fraction fraction2 = new Fraction(4, 10);
		
		System.out.println(fraction1.lessThan(fraction2));
		
		Fraction fraction = new Fraction(15499, 94744);
		int i = 12;
		Fraction resilienceFraction = resilience1(i);
		
		System.out.println(resilienceFraction);

		while (!resilienceFraction.lessThan(fraction)) {
			i++;
			resilienceFraction = resilience1(i);

			if (i % 1000 == 0) {
				System.out.println(i);
			}
		}

		System.out.println(resilienceFraction);
		System.out.println(resilienceFraction.lessThan(fraction));
	}
}

