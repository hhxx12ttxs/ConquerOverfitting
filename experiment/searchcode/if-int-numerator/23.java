/**
 * 
 */
package p57;

import java.io.IOException;
import java.math.BigInteger;

/**
 * @author Son-Huy TRAN
 * 
 */
public class P57_SquareRootConvergents {

	private static Fraction nextGeneration(Fraction generation) {
		// add 2
		generation.numerator = generation.numerator.add(generation.denominator
				.multiply(BigInteger.valueOf(2)));

		// inverse
		BigInteger temp = generation.numerator;
		generation.numerator = generation.denominator;
		generation.denominator = temp;

		return generation;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) {
		BigInteger numerator = BigInteger.valueOf(1);
		BigInteger denominator = BigInteger.valueOf(2);

		Fraction generation = new Fraction(numerator, denominator);
		int count = 0;

		for (int i = 2; i <= 1000; i++) {
			generation = nextGeneration(generation);
			Fraction next = generation.add1();

			if (next.numerator.toString().length() > next.denominator
					.toString().length()) {
				count++;
			}
		}

		System.out.println(count);
	}

}

