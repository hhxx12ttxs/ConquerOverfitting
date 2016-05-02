package dulw;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.ComposableFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.integration.TrapezoidIntegrator;

public abstract class ParameterCalc {

	// LINEAR SECTION
	/**
	 * Use this method to get relative average for linear functions, in this
	 * case noises and impulses are not linear.
	 * 
	 * @param function
	 * @param min
	 * @param max
	 * @return
	 * @throws IllegalArgumentException
	 * @throws FunctionEvaluationException
	 * @throws MaxIterationsExceededException
	 */
	// TODO sprawdzac czy przedział jest wielokrotnosciš okresu
	public static double relativeAverageForLinearFunction(
			UnivariateRealFunction function, final double min, final double max)
			throws MaxIterationsExceededException, FunctionEvaluationException,
			IllegalArgumentException {
		TrapezoidIntegrator integrator = new TrapezoidIntegrator();
		return integrator.integrate(function, min, max) / (max - min);
	}

	/**
	 * Use this method to get absolute average for linear functions, in this
	 * case noises and impulses are not linear.
	 * 
	 * @param function
	 * @param min
	 * @param max
	 * @return
	 * @throws IllegalArgumentException
	 * @throws FunctionEvaluationException
	 * @throws MaxIterationsExceededException
	 */
	public static double absoluteAverageForLinearFunction(
			UnivariateRealFunction function, final double min, final double max)
			throws MaxIterationsExceededException, FunctionEvaluationException,
			IllegalArgumentException {
		UnivariateRealFunction absFunction = ComposableFunction.ABS
				.of(function);
		TrapezoidIntegrator integrator = new TrapezoidIntegrator();
		return integrator.integrate(absFunction, min, max) / (max - min);
	}

	/**
	 * 
	 * @param function
	 * @param min
	 * @param max
	 * @return
	 * @throws MaxIterationsExceededException
	 * @throws FunctionEvaluationException
	 * @throws IllegalArgumentException
	 */
	public static double averagePowerForLinearFunction(
			UnivariateRealFunction function, final double min, final double max)
			throws MaxIterationsExceededException, FunctionEvaluationException,
			IllegalArgumentException {
		UnivariateRealFunction powFunction = ComposableFunction.ONE.multiply(
				function).multiply(function);
		TrapezoidIntegrator integrator = new TrapezoidIntegrator();
		return integrator.integrate(powFunction, min, max) / (max - min);
	}

	/**
	 * 
	 * @param function
	 * @param min
	 * @param max
	 * @return
	 * @throws MaxIterationsExceededException
	 * @throws FunctionEvaluationException
	 * @throws IllegalArgumentException
	 */
	public static double variationForLinearFunction(
			UnivariateRealFunction function, final double min, final double max)
			throws MaxIterationsExceededException, FunctionEvaluationException,
			IllegalArgumentException {
		// function - srednia
		ComposableFunction tempFunc = ComposableFunction.ONE.multiply(function)
				.subtract(
						ComposableFunction.ONE
								.multiply(absoluteAverageForLinearFunction(
										function, min, max)));
		// do kwadratu
		UnivariateRealFunction powFunction = ComposableFunction.ONE.multiply(
				tempFunc).multiply(tempFunc);
		TrapezoidIntegrator integrator = new TrapezoidIntegrator();
		return integrator.integrate(powFunction, min, max) / (max - min);
	}

	/**
	 * 
	 * @param function
	 * @param min
	 * @param max
	 * @return
	 * @throws MaxIterationsExceededException
	 * @throws FunctionEvaluationException
	 * @throws IllegalArgumentException
	 */
	public static double rmsForLinearFunction(UnivariateRealFunction function,
			final double min, final double max)
			throws MaxIterationsExceededException, FunctionEvaluationException,
			IllegalArgumentException {
		return Math.sqrt(averagePowerForLinearFunction(function, min, max));
	}

	// NON LINEAR SECTION

	/**
	 * Use this method to get relative average for nonlinear functions, note
	 * that in this case we treat noises as not linear
	 * 
	 * @param function
	 * @param min
	 * @param max
	 * @param f
	 * @return
	 * @throws FunctionEvaluationException
	 */
	public static double relativeAverageForDiscretFunction(
			UnivariateRealFunction function, final double min,
			final double max, final double f)
			throws FunctionEvaluationException {
		double sum = 0;
		int n = 0;
		for (double t = min; t <= max; t += 1 / f) {
			sum += function.value(t);
			++n;
		}
		return sum / (n + 1);
	}

	/**
	 * Use this method to get relative average for nonlinear functions.
	 * 
	 * @param function
	 * @param min
	 * @param max
	 * @param f
	 * @return
	 * @throws FunctionEvaluationException
	 */
	public static double absoluteAverageForDiscretFunction(
			UnivariateRealFunction function, final double min,
			final double max, final double f)
			throws FunctionEvaluationException {
		double sum = 0;
		int n = 0;
		UnivariateRealFunction absFunction = ComposableFunction.ABS
				.of(function);
		for (double t = min; t <= max; t += 1 / f) {
			sum += absFunction.value(t);
			++n;
		}
		return sum / (n + 1);
	}

	/**
	 * 
	 * @param function
	 * @param min
	 * @param max
	 * @param f
	 * @return
	 * @throws FunctionEvaluationException
	 */
	public static double averagePowerForDiscretFunction(
			UnivariateRealFunction function, final double min,
			final double max, final double f)
			throws FunctionEvaluationException {
		double sum = 0;
		int n = 0;
		UnivariateRealFunction powFunction = ComposableFunction.ONE.multiply(
				function).multiply(function);
		for (double t = min; t <= max; t += 1 / f) {
			sum += powFunction.value(t);
			++n;
		}
		return sum / (n + 1);
	}

	/**
	 * 
	 * @param function
	 * @param min
	 * @param max
	 * @param f
	 * @return
	 * @throws MaxIterationsExceededException
	 * @throws FunctionEvaluationException
	 * @throws IllegalArgumentException
	 */
	public static double variationForDiscretFunction(
			UnivariateRealFunction function, final double min,
			final double max, final double f)
			throws MaxIterationsExceededException, FunctionEvaluationException,
			IllegalArgumentException {
		// function - srednia
		ComposableFunction tempFunc = ComposableFunction.ONE.multiply(function)
				.subtract(
						ComposableFunction.ONE
								.multiply(absoluteAverageForDiscretFunction(
										function, min, max, f)));
		// do kwadratu
		UnivariateRealFunction powFunction = ComposableFunction.ONE.multiply(
				tempFunc).multiply(tempFunc);
		double sum = 0;
		int n = 0;
		for (double t = min; t <= max; t += 1 / f) {
			sum += powFunction.value(t);
			++n;
		}
		return sum / (n + 1);
	}

	/**
	 * 
	 * @param function
	 * @param min
	 * @param max
	 * @param f
	 * @return
	 * @throws MaxIterationsExceededException
	 * @throws FunctionEvaluationException
	 * @throws IllegalArgumentException
	 */
	public static double rmsForDiscretFunction(UnivariateRealFunction function,
			final double min, final double max, final double f)
			throws MaxIterationsExceededException, FunctionEvaluationException,
			IllegalArgumentException {
		return Math.sqrt(averagePowerForDiscretFunction(function, min, max, f));
	}

	public static List<double[]> addSignals(List<double[]> signal1,
			List<double[]> signal2) {
		if (signal1.size() != signal2.size()) {
			throw new IllegalArgumentException(
					"Sygnały muszš mieć tyle samo próbek");
		}
		List<double[]> signalsSum = new ArrayList<double[]>();
		double table[];
		for (int i = 0; i < signal1.size(); ++i) {
			table = new double[2];
			table[0] = signal1.get(i)[0];
			table[1] = signal1.get(i)[1] + signal2.get(i)[1];
			signalsSum.add(table);
		}
		return signalsSum;
	}

	public static List<double[]> substractSignals(List<double[]> signal1,
			List<double[]> signal2) {
		if (signal1.size() != signal2.size()) {
			throw new IllegalArgumentException(
					"Sygnały muszš mieć tyle samo próbek");
		}
		List<double[]> signalsSum = new ArrayList<double[]>();
		double table[];
		for (int i = 0; i < signal1.size(); ++i) {
			table = new double[2];
			table[0] = signal1.get(i)[0];
			table[1] = signal1.get(i)[1] - signal2.get(i)[1];
			signalsSum.add(table);
		}
		return signalsSum;
	}

	public static List<double[]> multiplySignals(List<double[]> signal1,
			List<double[]> signal2) {
		if (signal1.size() != signal2.size()) {
			throw new IllegalArgumentException(
					"Sygnały muszš mieć tyle samo próbek");
		}
		List<double[]> signalsSum = new ArrayList<double[]>();
		double table[];
		for (int i = 0; i < signal1.size(); ++i) {
			table = new double[2];
			table[0] = signal1.get(i)[0];
			table[1] = signal1.get(i)[1] * signal2.get(i)[1];
			signalsSum.add(table);
		}
		return signalsSum;
	}

	public static List<double[]> divideSignals(List<double[]> signal1,
			List<double[]> signal2) {
		if (signal1.size() != signal2.size()) {
			throw new IllegalArgumentException(
					"Sygnały muszš mieć tyle samo próbek");
		}
		List<double[]> signalsSum = new ArrayList<double[]>();
		double table[];
		for (int i = 0; i < signal1.size(); ++i) {
			table = new double[2];
			table[0] = signal1.get(i)[0];
			table[1] = signal1.get(i)[1] / signal2.get(i)[1];
			signalsSum.add(table);
		}
		return signalsSum;
	}

}

