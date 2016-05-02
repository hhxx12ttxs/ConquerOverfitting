/**
 * 
 */
package p71;

import peutils.Utils;

/**
 * @author Son-Huy TRAN
 * 
 */
public class P71_Ordered_Fractions {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int maxDenominator = 1000000;
		double fractionMax = 2 * 1.0 / 5;
		int numeratorMax = -1, denominatorMax = -1;

		for (int denominator = 9; denominator <= maxDenominator; denominator++) {
			// find the numerator of the greatest fraction f
			// whose denominator is d and f < 3/7
			int numerator = (int) Math.floor(denominator * 3 / 7);

			// find the greatest PROPER fraction
			while (numerator > 0
					&& Utils.largestCommonDivisor(numerator, denominator) > 1) {
				numerator--;
			}

			if (numerator > 0) {
				double fraction = numerator * 1.0 / denominator;

				if (fraction > fractionMax) {
					fractionMax = fraction;
					numeratorMax = numerator;
					denominatorMax = denominator;
				}
			}
		}

		System.out.println(String.format("%d / %d", numeratorMax,
				denominatorMax));
	}
}

