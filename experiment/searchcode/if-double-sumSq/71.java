/*CopyrightHere*/
package repast.simphony.data.bsf.util;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import repast.simphony.data.bsf.gui.IgnoreFunction;
import simphony.util.messages.MessageCenter;

/**
 * Some static functions primarily developed to be statically imported and used in scripts. These
 * are generally of the form (scriptExpression, objectToPerformCallsOn). <p/>
 * 
 * Most (if not all) of the statistical computations are computed using the commons-math library
 * from apache (<a
 * href="http://jakarta.apache.org/commons/math">http://jakarta.apache.org/commons/math</a>). For
 * documentation on the statistical functions please see the
 * {@link org.apache.commons.math.stat.descriptive.DescriptiveStatistics} class (<a href="http://jakarta.apache.org/commons/math/apidocs/org/apache/commons/math/stat/descriptive/DescriptiveStatistics.html">
 * http://jakarta.apache.org/commons/math/apidocs/org/apache/commons/math/stat/descriptive/DescriptiveStatistics.html</a>(.
 *
 * @see org.apache.commons.math.stat.descriptive.DescriptiveStatistics
 * 
 * @author Jerry Vos
 */
public class RepastBSFFuncs {
	private static final MessageCenter msgCenter = MessageCenter
			.getMessageCenter(RepastBSFFuncs.class);

	/**
	 * This will create a new {@link DescriptiveStatistics} object and a {@link BSFManager} and will
	 * load each object's properties (using {@link BSFUtils#loadProperties(BSFManager, Object)})
	 * and then store the result of {@link BSFUtils#eval(BSFManager, String)} in the
	 * {@link DescriptiveStatistics} it returns.<p/>
	 * 
	 * When an error occurs a warning will be fed into the {@link MessageCenter} and then it will be
	 * ignored.
	 * 
	 * @param bsfForEach
	 *            the expression to execute for each object
	 * @param objects
	 *            the objects whose properties to load
	 * @return the object with the results of the expressions stored in it
	 */
	@IgnoreFunction
	public static DescriptiveStatistics buildStatsCalculator(String bsfForEach,
			Iterable<Object> objects) {
		// TODO: this is not generic, will work for beanshell and python, but maybe some
		// other language it won't
		if (!bsfForEach.matches(BSFUtils.VALUE_VAR + " *=")) {
			bsfForEach = "value = " + bsfForEach;
		}
		BSFManager bsfManager = BSFUtils.createDefaultManager();

		DescriptiveStatistics stats = DescriptiveStatistics.newInstance();

		for (Object obj : objects) {
			BSFUtils.loadProperties(bsfManager, obj);

			Object result = null;
			try {
				result = BSFUtils.eval(bsfManager, bsfForEach);

				if (result instanceof Number) {
					Number numb = (Number) result;

					stats.addValue(numb.doubleValue());
				} else {
					msgCenter
							.warn("When executing a calculation, received a non number value as a result ("
									+ result + ").  Ignoring and continuing");
				}
			} catch (BSFException e) {
				msgCenter
						.warn("When executing a calculation, received an exception while evaluating '"
								+ bsfForEach + "'.  Ignoring and continuing");
			}
		}

		return stats;
	}

	/**
	 * Performs a sum on a series of objects using the specified scripting expression. This uses
	 * {@link #buildStatsCalculator(String, Iterable)} and then will return the variance.
	 * 
	 * @param bsfForEach
	 *            the scripting expression to execute on each object
	 * @param objects
	 *            the objects to execute the expression on
	 * @return the sum
	 */
	public static double variance(String bsfForEach, Iterable<Object> objects) {
		return buildStatsCalculator(bsfForEach, objects).getVariance();
	}

	/**
	 * Performs a sum on a series of objects using the specified scripting expression. This uses
	 * {@link #buildStatsCalculator(String, Iterable)} and then will return the square of the sums.
	 * 
	 * @param bsfForEach
	 *            the scripting expression to execute on each object
	 * @param objects
	 *            the objects to execute the expression on
	 * @return the sum
	 */
	public static double sumsq(String bsfForEach, Iterable<Object> objects) {
		return buildStatsCalculator(bsfForEach, objects).getSumsq();
	}

	/**
	 * Performs a sum on a series of objects using the specified scripting expression. This uses
	 * {@link #buildStatsCalculator(String, Iterable)} and then will return the standard deviation.
	 * 
	 * @param bsfForEach
	 *            the scripting expression to execute on each object
	 * @param objects
	 *            the objects to execute the expression on
	 * @return the sum
	 */
	public static double stddev(String bsfForEach, Iterable<Object> objects) {
		return buildStatsCalculator(bsfForEach, objects).getStandardDeviation();
	}

	/**
	 * Performs a sum on a series of objects using the specified scripting expression. This uses
	 * {@link #buildStatsCalculator(String, Iterable)} and then will return the skewness.
	 * 
	 * @param bsfForEach
	 *            the scripting expression to execute on each object
	 * @param objects
	 *            the objects to execute the expression on
	 * @return the sum
	 */
	public static double skewness(String bsfForEach, Iterable<Object> objects) {
		return buildStatsCalculator(bsfForEach, objects).getSkewness();
	}

	/**
	 * Performs a sum on a series of objects using the specified scripting expression. This uses
	 * {@link #buildStatsCalculator(String, Iterable)} and then will return the Kurtosis.
	 * 
	 * @param bsfForEach
	 *            the scripting expression to execute on each object
	 * @param objects
	 *            the objects to execute the expression on
	 * @return the sum
	 */
	public static double kurtosis(String bsfForEach, Iterable<Object> objects) {
		return buildStatsCalculator(bsfForEach, objects).getKurtosis();
	}

	/**
	 * Performs a sum on a series of objects using the specified scripting expression. This uses
	 * {@link #buildStatsCalculator(String, Iterable)} and then will return the min.
	 * 
	 * @param bsfForEach
	 *            the scripting expression to execute on each object
	 * @param objects
	 *            the objects to execute the expression on
	 * @return the sum
	 */
	public static double percentile(String bsfForEach, Iterable<Object> objects, double percentile) {
		return buildStatsCalculator(bsfForEach, objects).getMin();
	}

	/**
	 * Performs a sum on a series of objects using the specified scripting expression. This uses
	 * {@link #buildStatsCalculator(String, Iterable)} and then will return the max.
	 * 
	 * @param bsfForEach
	 *            the scripting expression to execute on each object
	 * @param objects
	 *            the objects to execute the expression on
	 * @return the sum
	 */
	public static double max(String bsfForEach, Iterable<Object> objects) {
		return buildStatsCalculator(bsfForEach, objects).getMax();
	}

	/**
	 * Performs a sum on a series of objects using the specified scripting expression. This uses
	 * {@link #buildStatsCalculator(String, Iterable)} and then will return the geometric mean.
	 * 
	 * @param bsfForEach
	 *            the scripting expression to execute on each object
	 * @param objects
	 *            the objects to execute the expression on
	 * @return the sum
	 */
	public static double geometricMean(String bsfForEach, Iterable<Object> objects) {
		return buildStatsCalculator(bsfForEach, objects).getGeometricMean();
	}

	/**
	 * Performs a sum on a series of objects using the specified scripting expression. This uses
	 * {@link #buildStatsCalculator(String, Iterable)} and then will return the mean.
	 * 
	 * @param bsfForEach
	 *            the scripting expression to execute on each object
	 * @param objects
	 *            the objects to execute the expression on
	 * @return the sum
	 */
	public static double mean(String bsfForEach, Iterable<Object> objects) {
		return buildStatsCalculator(bsfForEach, objects).getMean();
	}

	/**
	 * Performs a sum on a series of objects using the specified scripting expression. This uses
	 * {@link #buildStatsCalculator(String, Iterable)} and then will return the sum.
	 * 
	 * @param bsfForEach
	 *            the scripting expression to execute on each object
	 * @param objects
	 *            the objects to execute the expression on
	 * @return the sum
	 */
	public static double sum(String bsfForEach, Iterable<Object> objects) {
		return buildStatsCalculator(bsfForEach, objects).getSum();
	}
}

