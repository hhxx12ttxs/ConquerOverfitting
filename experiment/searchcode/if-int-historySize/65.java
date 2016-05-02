package nlp.math;

import java.io.Serializable;
import java.util.LinkedList;

public class LBFGSMinimizer implements GradientMinimizer, Serializable {
	private static final long serialVersionUID = 36473897808840226L;
	double EPS = 1e-10;
	int maxIterations = 20;
	int maxHistorySize = 5;
	LinkedList<double[]> inputDifferenceVectorList;
	LinkedList<double[]> derivativeDifferenceVectorList;
	transient IterationCallbackFunction iterCallbackFunction = null;
	int minIterations = -1;
	double initialStepSizeMultiplier = 0.01;
	double stepSizeMultiplier = 0.5;

	public static interface IterationCallbackFunction {
		public void iterationDone(double[] curGuess,int iter);
	}

	public void setMinIteratons(int minIterations) {
		this.minIterations = minIterations;    
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}		

	public void setInitialStepSizeMultiplier(double initialStepSizeMultiplier) {
		this.initialStepSizeMultiplier = initialStepSizeMultiplier;  
	}

	public void setStepSizeMultiplier(double stepSizeMultiplier) {
		this.stepSizeMultiplier = stepSizeMultiplier;
	}
	public double[] minimize(DifferentiableFunction function, double[] initial, double tolerance) {
		return minimize(function, initial, tolerance, true);  
	}

	public double[] minimize(DifferentiableFunction function, double[] initial, double tolerance, boolean printProgress) {
		inputDifferenceVectorList = new LinkedList<double[]>();
		derivativeDifferenceVectorList = new LinkedList<double[]>();
		
		BacktrackingLineSearcher lineSearcher = new BacktrackingLineSearcher();
		double[] guess = DoubleArrays.clone(initial);
		double value = function.valueAt(guess);
		double[] derivative = function.derivativeAt(guess);

		for (int iteration = 0; iteration < maxIterations; iteration++) {
			double[] initialInverseHessianDiagonal = getInitialInverseHessianDiagonal(function);
			double[] direction = implicitMultiply(initialInverseHessianDiagonal, derivative);
			DoubleArrays.scale(direction, -1.0);

			lineSearcher.stepSizeMultiplier = (iteration == 0)?initialStepSizeMultiplier:stepSizeMultiplier;
			Object[] next = lineSearcher.minimize(function, guess, direction, value, derivative);
			double[] nextGuess = (double[])next[0];
			double nextValue = (Double)next[1];
			double[] nextDerivative = function.derivativeAt(nextGuess);

			if (printProgress) {
				System.out.printf("[LBFGSMinimizer.minimize] Iteration %d ended with value %.6f\n",iteration, nextValue);
			}

			if (iteration >= minIterations && converged(value, nextValue, tolerance))
				return nextGuess;

			updateHistories(guess, nextGuess, derivative,  nextDerivative);
			guess = nextGuess;
			value = nextValue;
			derivative = nextDerivative;
			if (iterCallbackFunction != null) {
				iterCallbackFunction.iterationDone(guess,iteration);
			}
		}
		//System.err.println("LBFGSMinimizer.minimize: Exceeded maxIterations without converging.");
		return guess;
	}

	private boolean converged(double value, double nextValue, double tolerance) {
		if (value == nextValue)
			return true;
		double valueChange = SloppyMath.abs(nextValue - value);
		double valueAverage = SloppyMath.abs(nextValue + value + EPS) / 2.0;
		if (valueChange / valueAverage < tolerance)
			return true;
		return false;
	}

	private void updateHistories(double[] guess, double[] nextGuess, double[] derivative, double[] nextDerivative) {
		double[] guessChange = DoubleArrays.subtract(nextGuess, guess);
		double[] derivativeChange = DoubleArrays.subtract(nextDerivative, derivative);
		pushOntoList(guessChange, inputDifferenceVectorList);
		pushOntoList(derivativeChange,  derivativeDifferenceVectorList);
	}

	private void pushOntoList(double[] vector, LinkedList<double[]> vectorList) {
		vectorList.addFirst(vector);
		if (vectorList.size() > maxHistorySize)
			vectorList.removeLast();
	}

	private int historySize() {
		return inputDifferenceVectorList.size();
	}

	public void setMaxHistorySize(int maxHistorySize) {
		this.maxHistorySize = maxHistorySize;
	}

	private double[] getInputDifference(int num) {
		// 0 is previous, 1 is the one before that
		return inputDifferenceVectorList.get(num);
	}

	private double[] getDerivativeDifference(int num) {
		return derivativeDifferenceVectorList.get(num);
	}

	private double[] getLastDerivativeDifference() {
		return derivativeDifferenceVectorList.getFirst();
	}

	private double[] getLastInputDifference() {
		return inputDifferenceVectorList.getFirst();
	}


	private double[] implicitMultiply(double[] initialInverseHessianDiagonal, double[] derivative) {
		double[] rho = new double[historySize()];
		double[] alpha = new double[historySize()];
		double[] right = DoubleArrays.clone(derivative);
		// loop last backward
		for (int i = historySize()-1; i >= 0; i--) {
			double[] inputDifference = getInputDifference(i);
			double[] derivativeDifference = getDerivativeDifference(i);
			rho[i] = DoubleArrays.innerProduct(inputDifference, derivativeDifference);
			if (rho[i] == 0.0)
				throw new RuntimeException("LBFGSMinimizer.implicitMultiply: Curvature problem.");
			alpha[i] = DoubleArrays.innerProduct(inputDifference, right) / rho[i];
			right = DoubleArrays.addMultiples(right, 1.0, derivativeDifference, -1.0*alpha[i]);
		}
		double[] left = DoubleArrays.pointwiseMultiply(initialInverseHessianDiagonal, right);
		for (int i = 0; i < historySize(); i++) {
			double[] inputDifference = getInputDifference(i);
			double[] derivativeDifference = getDerivativeDifference(i);
			double beta = DoubleArrays.innerProduct(derivativeDifference, left) / rho[i];
			left = DoubleArrays.addMultiples(left, 1.0, inputDifference, alpha[i] - beta);
		}
		return left;
	}

	private double[] getInitialInverseHessianDiagonal(DifferentiableFunction function) {
		double scale = 1.0;
		if (derivativeDifferenceVectorList.size() >= 1) {
			double[] lastDerivativeDifference = getLastDerivativeDifference();
			double[] lastInputDifference = getLastInputDifference();
			double num = DoubleArrays.innerProduct(lastDerivativeDifference, lastInputDifference);
			double den = DoubleArrays.innerProduct(lastDerivativeDifference, lastDerivativeDifference);
			scale = num / den;
		}
		return DoubleArrays.constantArray(scale, function.dimension());
	}

	public void setIterationCallbackFunction(IterationCallbackFunction callbackFunction) {
		this.iterCallbackFunction = callbackFunction;
	}

	public LBFGSMinimizer() {
	}

	public LBFGSMinimizer(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public static void main(String ... args) {
		DifferentiableFunction function = new DifferentiableFunction() {
			public int dimension() {
				return 1;
			}

			public double valueAt(double[] x) {
				System.err.println("valueAt : " + DoubleArrays.toString(x));
				return x[0] * (x[0] - 0.01);
			}

			public double[] derivativeAt(double[] x) {
				System.err.println("derivativeAt : " + DoubleArrays.toString(x));
				return new double[] { 2*x[0] - 0.01 };
			}
		};
		LBFGSMinimizer minimizer = new LBFGSMinimizer(20);
		double[] result=minimizer.minimize(function, new double[] { 0 }, 1e-10);
		System.out.println(DoubleArrays.toString(result));
	}

}
