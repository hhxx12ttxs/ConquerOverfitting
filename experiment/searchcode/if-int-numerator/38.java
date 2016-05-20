/**
 * 
 */
package p73;

import peutils.Utils;

/**
 * @author Son-Huy TRAN
 * 
 */
public class P73_CountingFractionsInARange {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int count = 0;
		int denominatorMax = 12000;

		for (int denominator = 4; denominator <= denominatorMax; denominator++) {
			int numeratorMax = (int) Math.floor(denominator * 1.0 / 2);
			int numeratorMin = (int) Math.ceil(denominator * 1.0 / 3);

			for (int numerator = numeratorMin; numerator <= numeratorMax; numerator++) {
				if (Utils.largestCommonDivisor(numerator, denominator) == 1) {
					count++;
				}
			}
		}

		System.out.println(count);
	}
}

