/**
 * 
 */
package p183;

import peutils.Utils;

/**
 * @author Son-Huy TRAN http://www.wolframalpha.com/
 */
public class P183_MaximumProductOfParts {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int sum = 0;

		for (int N = 5; N <= 10000; N++) {
			double max = N / Math.E;
			int floorMax = (int) Math.floor(max);
			int ceilMax = (int) Math.ceil(max);
			double fFloorMax = floorMax * Math.log(N * 1.0 / floorMax);
			double fCeilMax = ceilMax * Math.log(N * 1.0 / ceilMax);

			int kmax = fCeilMax > fFloorMax ? ceilMax : floorMax;
			int denominator = kmax / Utils.largestCommonDivisor(kmax, N);

			while (denominator % 2 == 0) {
				denominator /= 2;
			}

			while (denominator % 5 == 0) {
				denominator /= 5;
			}

			if (denominator > 1) {
				sum += N;
			} else {
				sum -= N;
			}
		}

		System.out.println(sum);
	}
}

