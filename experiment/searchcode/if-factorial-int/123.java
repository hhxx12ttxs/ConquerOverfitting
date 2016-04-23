package eu.kijo.util.math;

import java.util.HashMap;
import java.util.Map;

/**
 * This class consists exclusively of static math utility methods.
 *
 * @author Johannes Kissel
 */
public class MathUtils {

	// Suppresses default constructor, ensuring non-instantiability.
	private MathUtils() { }

	/**
	 * Tests the given number for being odd
	 *
	 * @param x The number to be tested
	 * @return <code>true</code> if <code>x</code> is odd
	 */
	public static boolean odd(int x) {
		return (x % 2 != 0);
	}

	/**
	 * Tests the given number for being odd
	 *
	 * @param x The number to be tested
	 * @return <code>true</code> if <code>x</code> is odd
	 */
	public static boolean odd(long x) {
		return (x % 2 != 0);
	}

	/**
	 * Tests the given number for being even
	 *
	 * @param x The number to be tested
	 * @return <code>true</code> if <code>x</code> is even
	 */
	public static boolean even(int x) {
		return (x % 2 == 0);
	}

	/**
	 * Tests the given number for being even
	 *
	 * @param x The number to be tested
	 * @return <code>true</code> if <code>x</code> is even
	 */
	public static boolean even(long x) {
		return (x % 2 == 0);
	}

	/**
	 * Rounds the given number to the next integer which is greater or equal to it.
	 *
	 * @param x The number to be rounded
	 * @return The rounded result
	 */
	public static long ceil(double x) {
		return (long) Math.ceil(x);
	}

	/**
	 * Rounds the given number to the next integer which is greater or equal to it.
	 *
	 * @param x The number to be rounded
	 * @return The rounded result
	 */
	public static int ceil(float x) {
		return (int) Math.ceil(x);
	}

	/**
	 * Rounds the given number to the next integer which is less or equal to it.
	 *
	 * @param x The number to be rounded
	 * @return The rounded result
	 */
	public static long floor(double x) {
		return (long) Math.floor(x);
	}

	/**
	 * Rounds the given number to the next integer which is less or equal to it.
	 *
	 * @param x The number to be rounded
	 * @return The rounded result
	 */
	public static int floor(float x) {
		return (int) Math.floor(x);
	}

	/**
	 * Computes the sum of the integers in the given array.
	 *
	 * @param array The integer array
	 * @return The sum of the values in <code>array</code>
	 */
	public static int sum(int[] array) {
		int sum = 0;

		for (int x : array) {
			sum += x;
		}

		return sum;
	}

	/**
	 * Computes the sum of the doubles in the given array.
	 *
	 * @param array The double array
	 * @return The sum of the values in <code>array</code>
	 */
	public static double sum(double[] array) {
		double sum = 0;

		for (double x : array) {
			sum += x;
		}

		return sum;
	}

	/**
	 * Computes the factorial of the given integer.
	 *
	 * @param n The integer
	 * @return The factorial of <code>n</code>
	 */
	public static int factorial(int n) {
		return (n <= 1) ? 1 : (n * factorial(n - 1));
	}

	/**
     * Computes the parity of the given byte array.
     *
	 * @param bytes The byte array
     * @return The parity of <code>bytes</code>
     */
    public static byte parity(byte[] bytes) {
        byte parity = 0;

        for (byte b : bytes) {
            parity ^= b;
        }

        return parity;
    }

	/* ========== *
	 * Statistics *
	 * ========== */

	/**
	 * Determines the maximum value of the given integer array.
	 *
	 * @param array The integer array
	 * @return The maximum value in <code>array</code>
	 */
	public static int max(int[] array) {
		int max = array[0];

		for (int i = 1; i < array.length; i++) {
			if (array[i] > max) {
				max = array[i];
			}
		}

		return max;
	}
	
	/**
	 * Determines the maximum value of the given double array.
	 *
	 * @param array The double array
	 * @return The maximum value in <code>array</code>
	 */
	public static double max(double[] array) {
		double max = array[0];

		for (int i = 1; i < array.length; i++) {
			if (array[i] > max) {
				max = array[i];
			}
		}

		return max;
	}

	/**
	 * Determines the minimum value of the given integer array.
	 *
	 * @param array The integer array
	 * @return The minimum value in <code>array</code>
	 */
	public static int min(int[] array) {
		int min = array[0];

		for (int i = 1; i < array.length; i++) {
			if (array[i] < min) {
				min = array[i];
			}
		}

		return min;
	}

	/**
	 * Determines the minimum value of the given double array.
	 *
	 * @param array The double array
	 * @return The minimum value in <code>array</code>
	 */
	public static double min(double[] array) {
		double min = array[0];

		for (int i = 1; i < array.length; i++) {
			if (array[i] < min) {
				min = array[i];
			}
		}

		return min;
	}

	/**
	 * Computes the mean value of the given integer array.
	 * 
	 * @param array The integer array
	 * @return The mean value of <code>array</code>
	 */
	public static double mean(int[] array) {
		return (sum(array) / (double) array.length);
	}

	/**
	 * Computes the mean value of the given double array.
	 *
	 * @param array The double array
	 * @return The mean value of <code>array</code>
	 */
	public static double mean(double[] array) {
		return (sum(array) / array.length);
	}

	/**
	 * Computes the median of the given integer array.
	 *
	 * @param array The integer array
	 * @return The median of <code>array</code>
	 */
	public static double median(int[] array) {
		int c = array.length / 2; // index of center element
		
		return (odd(array.length)) ? array[c] : ((array[c - 1] + array[c]) / 2.0);
	}

	/**
	 * Computes the median of the given double array.
	 *
	 * @param array The double array
	 * @return The median of <code>array</code>
	 */
	public static double median(double[] array) {
		int c = array.length / 2; // index of center element

		return (odd(array.length)) ? array[c] : ((array[c - 1] + array[c]) / 2.0);
	}

	/**
	 * Computes the mode of the given integer array.
	 *
	 * @param array The integer array
	 * @return The mode of <code>array</code>
	 */
	public static int mode(int[] array) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();

		for (int x : array) {
			map.put(x, (map.containsKey(x) ? (map.get(x) + 1) : 1));
		}

		int mode = 0, max = 0;

		for (int x : map.keySet()) {
			int c = map.get(x);
			
			if (c > max) {
				mode = x;
				max  = c;
			}
		}
		
		return mode;
	}

	/* ============= *
	 * Normalization *
	 * ============= */

	/**
	 * Normalizes the given integer value.
	 *
	 * @param v The value to be normalized
	 * @param min The minimum of the old range
	 * @param max The maximum of the old range
	 * @param nmin The nomalization minimum
	 * @param nmax The nomalization maximum
	 * @return <code>v</code> normalized
	 */
	public static double normalize(int v, int min, int max, int nmin, int nmax) {
		if (Double.isNaN(v)) { return v; }
		if (min == max) { return nmin; }
		
		return (v - min) * ((nmax - nmin) / (double) (max - min)) + nmin;
	}

	/**
	 * Normalizes the given integer value to fit between 0 and 1.
	 *
	 * @param v The value to be normalized
	 * @param min The minimum of the old range
	 * @param max The maximum of the old range
	 * @return <code>v</code> normalized
	 */
	public static double normalize(int v, int min, int max) {
		if (Double.isNaN(v)) { return v; }
		if (min == max) { return 0; }

		return (v - min) / (double) (max - min);
	}

	/**
	 * Normalizes the given double value.
	 *
	 * @param v The value to be normalized
	 * @param min The minimum of the old range
	 * @param max The maximum of the old range
	 * @param nmin The nomalization minimum
	 * @param nmax The nomalization maximum
	 * @return <code>v</code> normalized
	 */
	public static double normalize(double v, double min, double max, int nmin, int nmax) {
		if (Double.isNaN(v)) { return v; }
		if (min == max) { return nmin; }

		return (v - min) * ((nmax - nmin) / (max - min)) + nmin;
	}

	/**
	 * Normalizes the given double value to fit between 0 and 1.
	 *
	 * @param v The value to be normalized
	 * @param min The minimum of the old range
	 * @param max The maximum of the old range
	 * @return <code>v</code> normalized
	 */
	public static double normalize(double v, double min, double max) {
		if (Double.isNaN(v)) { return v; }
		if (min == max) { return 0; }
		
		return (v - min) / (max - min);
	}

	/**
	 * Normalizes the given integer array.
	 *
	 * @param array The array to be normalized
	 * @param min The artificial minimum
	 * @param max The artificial maximum
	 * @param nmin The nomalization minimum
	 * @param nmax The nomalization maximum
	 * @return <code>array</code> normalized
	 */
	public static double[] normalize(int[] array, int min, int max, int nmin, int nmax) {
		double[] result = new double[array.length];

		if (nmin == 0 && nmax == 1) {
			for (int i = 0; i < array.length;  i++) {
				result[i] = normalize(array[i], min, max);
			}
		} else {
			for (int i = 0; i < array.length;  i++) {
				result[i] = normalize(array[i], min, max, nmin, nmax);
			}
		}

		return result;
	}

	/**
	 * Normalizes the given integer array.
	 *
	 * @param array The array to be normalized
	 * @param nmin The nomalization minimum
	 * @param nmax The nomalization maximum
	 * @return <code>array</code> normalized
	 */
	public static double[] normalize(int[] array, int nmin, int nmax) {
		int min = min(array);
		int max = max(array);

		return normalize(array, min, max, nmin, nmax);
	}

	/**
	 * Normalizes the given integer array to fit between 0 and 1.
	 *
	 * @param array The array to be normalized
	 * @return <code>array</code> normalized
	 */
	public static double[] normalize(int[] array) {
		return normalize(array, 0, 1);
	}

	/**
	 * Normalizes the given double array.
	 *
	 * @param array The array to be normalized
	 * @param min The artificial minimum
	 * @param max The artificial maximum
	 * @param nmin The nomalization minimum
	 * @param nmax The nomalization maximum
	 * @return <code>array</code> normalized
	 */
	public static double[] normalize(double[] array, double min, double max, int nmin, int nmax) {
		double[] result = new double[array.length];

		if (nmin == 0 && nmax == 1) {
			for (int i = 0; i < array.length;  i++) {
				result[i] = normalize(array[i], min, max);
			}
		} else {
			for (int i = 0; i < array.length;  i++) {
				result[i] = normalize(array[i], min, max, nmin, nmax);
			}
		}

		return result;
	}

	/**
	 * Normalizes the given double array.
	 *
	 * @param array The array to be normalized
	 * @param nmin The nomalization minimum
	 * @param nmax The nomalization maximum
	 * @return <code>array</code> normalized
	 */
	public static double[] normalize(double[] array, int nmin, int nmax) {
		double min = min(array);
		double max = max(array);

		return normalize(array, min, max, nmin, nmax);
	}

	/**
	 * Normalizes the given double array to fit between 0 and 1.
	 *
	 * @param array The array to be normalized
	 * @return <code>array</code> normalized
	 */
	public static double[] normalize(double[] array) {
		return normalize(array, 0, 1);
	}

}

