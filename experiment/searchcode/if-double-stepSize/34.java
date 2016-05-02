package nlp.math;

/**
 */
public class BacktrackingLineSearcher implements GradientLineSearcher {
	private double EPS = 1e-10;
	double stepSizeMultiplier = 0.9;
	private double sufficientDecreaseConstant = 1e-4;//0.9;

	public double[] minimize(DifferentiableFunction function, double[] initial, double[] direction) {
		return (double[])minimize(function, initial, direction, 0.0, null)[0];
	}

	public Object[] minimize(DifferentiableFunction function, double[] initial, double[] direction, double initialValue, double[] initialDerivative) {
		double stepSize = 1.0;
		double initialFunctionValue = (initialDerivative == null)?function.valueAt(initial):initialValue;
		double initialDirectionalDerivative = DoubleArrays.innerProduct( ((initialDerivative == null)?function.derivativeAt(initial):initialDerivative) , direction);
		double[] guess = null;
		double guessValue = 0.0;
		boolean sufficientDecreaseObtained = false;

		while (! sufficientDecreaseObtained) {
			guess = DoubleArrays.addMultiples(initial, 1.0, direction, stepSize);
			guessValue = function.valueAt(guess);
			double sufficientDecreaseValue = initialFunctionValue + sufficientDecreaseConstant * initialDirectionalDerivative * stepSize;

			sufficientDecreaseObtained = (guessValue <= sufficientDecreaseValue);
			if (! sufficientDecreaseObtained) {
				stepSize *= stepSizeMultiplier;
				if (stepSize < EPS) {
					//throw new RuntimeException("BacktrackingSearcher.minimize: stepSize underflow.");
					System.err.println("BacktrackingSearcher.minimize: stepSize underflow.");
					return new Object[]{initial, initialValue};
				}
			}
		}

		return new Object[]{guess, guessValue};
	}

	public static void main(String[] args) {
		DifferentiableFunction function = new DifferentiableFunction() {
			public int dimension() {
				return 1;
			}

			public double valueAt(double[] x) {
				return x[0] * (x[0] - 0.01);
			}

			public double[] derivativeAt(double[] x) {
				return new double[] { 2*x[0] - 0.01 };
			}
		};
		BacktrackingLineSearcher lineSearcher = new BacktrackingLineSearcher();
		lineSearcher.minimize(function, new double[] { 0 }, new double[] { 1 });
	}
}
