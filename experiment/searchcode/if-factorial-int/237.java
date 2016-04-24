/**
 * 
 */
package it.unibo.alchemist.utils;

import it.unibo.alchemist.external.cern.jet.random.engine.MersenneTwister;
import it.unibo.alchemist.external.cern.jet.random.engine.RandomEngine;

import java.util.Date;


/**
 * @author Danilo Pianini
 * 
 *         A collection of static methods useful for some mathematical
 *         computation
 * 
 */
public final class MathUtils {

	/**
	 * The Avogadro's number.
	 */
	public static final double AVOGADRO = 6.02214129e23;

	/**
	 * The Boltzmann's constant.
	 */
	public static final double K_BOLTZMANN = 1.3806488e-23;
	/**
	 * Cache for ln(2).
	 */
	private static final double LOG_2 = Math.log(2);
	/**
	 * Maximum allowed factorial (double).
	 */
	private static final int MAXFACTDOUBLE = 170, LANCZOS_G = 7;
	/**
	 * Maximum allowed factorial (long).
	 */
	private static final int MAXFACTLONG = 20;
	/**
	 * Factorial cache (double).
	 */
	private static final double[] FACTD = new double[MAXFACTDOUBLE];
	/**
	 * Factorial cache (long).
	 */
	private static final long[] FACTL = new long[MAXFACTLONG];

	/**
	 * Coefficients for Lanczos.
	 */
	private static final double[] P = { 0.99999999999980993, 676.5203681218851,
			-1259.1392167224028, 771.32342877765313, -176.61502916214059,
			12.507343278686905, -0.13857109526572012, 9.9843695780195716e-6,
			1.5056327351493116e-7 };
	/**
	 * Lanczos internal paramenter.
	 */
	private static final double POINTFIVE = 0.5d;
	/**
	 * Internal RNG.
	 */
	private static final RandomEngine RG = new MersenneTwister(new Date());
	/**
	 * This is a chached version of the factorial. It means it is slow for the
	 * first call, but extremely fast after that. It relies on the idea that
	 * normally inside Alchemist a factorial is called often with the same
	 * numbers (e.g. for the rate contribution computation of chemical reactions
	 * which involve more molecules of the same compound). Due to limited
	 * precision of double, you may experience precision loss with high numbers.
	 * This method returns meaningful results up to 170!. Over this limit, you
	 * get Double.POSITIVE_INFINITY.
	 * 
	 * @param n
	 *            the number to compute
	 * @return n!
	 */
	public static double factDouble(final int n) {
		if (n == 0) {
			return 1;
		}
		if (n > MAXFACTDOUBLE) {
			return Double.POSITIVE_INFINITY;
		}
		if (FACTD[n] != 0d) {
			return FACTD[n];
		}
		final double res = factDouble(n - 1) * n;
		FACTD[n] = res;
		return res;
	}

	/**
	 * This is a chached version of the factorial. It means it is slow for the
	 * first call, but extremely fast after that. It relies on the idea that
	 * normally inside Alchemist a factorial is called often with the same
	 * numbers (e.g. for the rate contribution computation of chemical reactions
	 * which involve more molecules of the same compound). Due to limited length
	 * of long, this method works up to 20!.
	 * 
	 * @param n
	 *            the number to compute
	 * @return n!
	 */
	public static long factLong(final int n) {
		if (n > MAXFACTLONG) {
			throw new IllegalArgumentException(
					"Maximum allowed value for this is 20. You tried with " + n);
		}
		if (n == 0) {
			return 1;
		}
		if (FACTL[n] != 0) {
			return FACTL[n];
		}
		final long res = factLong(n - 1) * n;
		FACTL[n] = res;
		return res;
	}

	/**
	 * Given a long, this functions computes in which position the first zero
	 * appears, counting from the least significant bit.
	 * 
	 * @param k
	 *            the long number you wish to know where the first 1 occurs
	 * @return the first zero in a binary long number
	 */
	public static int firstOnePosition(final long k) {
		return (int) Math.floor(log2(k));
	}

	/**
	 * Just an application of Pythagore's theorem. If two arrays of different
	 * size are passed, the function returns -1d.
	 * 
	 * @param a
	 *            an array containing the coordinates of the first point
	 * @param b
	 *            an array containing the coordinates of the second point
	 * @param <N>
	 *            the type of the coordinates
	 * @return the euclidean distance between the two points.
	 */
	public static <N extends Number> double getEuclideanDistance(final N[] a,
			final N[] b) {
		if (a.length != b.length) {
			return -1d;
		}
		double squaresum = 0d;
		/* Sum the squares of the distances for each dimension */
		for (int i = 0; i < a.length; i++) {
			final double k = a[i].doubleValue() - b[i].doubleValue();
			squaresum += k * k;
		}
		/* Then use the square root */
		return Math.sqrt(squaresum);
	}

	/**
	 * Return whether a point is inside an ellipse or not.
	 * 
	 * @param a2
	 *            Square of major axis
	 * @param b2
	 *            Square of minor axis
	 * @param x
	 *            X position of the point to test
	 * @param y
	 *            Y position of the point to test
	 * @return true if the point is inside the ellipse
	 */
	public static boolean isInEllipse(final double a2, final double b2,
			final double x, final double y) {
		return x * x / a2 + y * y / b2 < 1;
	}

	/**
	 * This method calculates the Gamma function ?(x) using the Lanczos
	 * approximation.
	 * 
	 * @param xp
	 *            the variable for ?(x)
	 * @return the Gamma function value with Lanczos approximation
	 */
	public static double lanczosGamma(final double xp) {
		double x = xp;
		if (x < POINTFIVE) {
			return Math.PI / (Math.sin(Math.PI * x) * lanczosGamma(1 - x));
		}

		x -= 1;
		double a = P[0];
		final double t = x + LANCZOS_G + POINTFIVE;
		for (int i = 1; i < P.length; i++) {
			a += P[i] / (x + i);
		}

		return Math.sqrt(2 * Math.PI) * Math.pow(t, x + POINTFIVE)
				* Math.exp(-t) * a;
	}

	/**
	 * A simple function to compute the base 2 logarithm.
	 * 
	 * @param v
	 *            the number whose logarithm must be computed
	 * @return log_2(v)
	 */
	public static double log2(final double v) {
		return Math.log(v) / LOG_2;
	}

	/**
	 * Fast method to get a new random integer.
	 * 
	 * @return a random integer in the interval [Integer.MIN_VALUE,
	 *         Integer.MAX_VALUE]
	 */
	public static int randomInt() {
		return RG.nextInt();
	}

	/**
	 * This method calculates the Gamma function ?(x) using the Stirling
	 * approximation.
	 * 
	 * @param x
	 *            the variable for ?(x)
	 * @return the Gamma function value with Stirling approximation
	 */
	public static double stirlingGamma(final double x) {
		return Math.sqrt(2d * Math.PI / x) * Math.pow((x / Math.E), x);
	}

	/**
	 * Disable default constructor.
	 */
	private MathUtils() {
	}

}

