package dulw;

import static java.lang.Math.PI;
import static java.lang.Math.floor;

import java.util.Random;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.ComposableFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;

public abstract class SignalsFactory {

	
	public static UnivariateRealFunction createSensorSignal(final double tp, final double T) {
		ComposableFunction tpFunc1 = ComposableFunction.ONE.multiply(tp);
		ComposableFunction innerFunc1 = ComposableFunction.IDENTITY.add(
				tpFunc1).multiply((2 * PI) / T);
		ComposableFunction sinFunc1 = ComposableFunction.SIN.of(innerFunc1)
				.multiply(10.0);
		
		ComposableFunction tpFunc2 = ComposableFunction.ONE.multiply(tp+1);
		ComposableFunction innerFunc2 = ComposableFunction.IDENTITY.add(
				tpFunc2).multiply((4 * PI) / T);
		ComposableFunction sinFunc2 = ComposableFunction.SIN.of(innerFunc2)
		.multiply(1.0);
		
		return sinFunc1.multiply(sinFunc2);
	}
	/**
	 * 
	 * @param t
	 * @param A
	 * @param tp
	 *            - przesuniecie
	 * @param T
	 * @return
	 */
	public static UnivariateRealFunction createSinusSignal(final double A,
			final double tp, final double T) {
		ComposableFunction tpFunc = ComposableFunction.ONE.multiply(tp);
		ComposableFunction innerFunc = ComposableFunction.IDENTITY.subtract(
				tpFunc).multiply((2 * PI) / T);
		ComposableFunction sinFunc = ComposableFunction.SIN.of(innerFunc)
				.multiply(A);
		return sinFunc;
	}

	public static UnivariateRealFunction createSinusHalfSignal(final double A,
			final double tp, final double T) {
		ComposableFunction tpFunc = ComposableFunction.ONE.multiply(tp);
		ComposableFunction innerFunc = ComposableFunction.IDENTITY.subtract(
				tpFunc).multiply((2 * PI) / T);
		ComposableFunction sinFunc = ComposableFunction.SIN.of(innerFunc);
		ComposableFunction absSinFunc = ComposableFunction.ABS.of(sinFunc);
		ComposableFunction outerFunc = sinFunc.add(absSinFunc);
		return outerFunc.multiply(A / 2);
	}

	public static UnivariateRealFunction createSinusBothSignal(final double A,
			final double tp, final double T) {
		ComposableFunction tpFunc = ComposableFunction.ONE.multiply(tp);
		ComposableFunction innerFunc = ComposableFunction.IDENTITY.subtract(
				tpFunc).multiply((2 * PI) / T);
		ComposableFunction absSinFunc = ComposableFunction.ABS
				.of(ComposableFunction.SIN.of(innerFunc));
		return absSinFunc.multiply(A);
	}

	public static UnivariateRealFunction createRectangleSignal(final double A,
			final double T, final double kw, final double t0) {
		return new RectangleSignal(A, T, kw, t0);
	}

	public static UnivariateRealFunction createRectangleSimmetricSignal(
			final double A, final double T, final double kw, final double t0) {
		return new RectangleSimmetricSignal(A, T, kw, t0);
	}

	public static UnivariateRealFunction createTriangleSignal(final double A,
			final double T, final double kw, final double t0) {
		return new TriangleSignal(A, T, kw, t0);
	}

	public static UnivariateRealFunction createJumpSignal(final double A,
			final double ts) {
		return new JumpSignal(A, ts);
	}

	public static UnivariateRealFunction createConstNoiseSignal(final double A) {
		return new ConstNoiseSignal(A);
	}

	public static UnivariateRealFunction createGausseNoiseSignal(final double A) {
		return new GaussNoiseSignal(A);
	}

	public static UnivariateRealFunction createImpulseSignal(final double A,
			final int ns) {
		return new ImpulseSignal(A, ns);
	}

	public static UnivariateRealFunction createImpulseNoise(final double A,
			final double probability) {
		return new ImpulseNoise(A, probability);
	}

}

// Base class for all signals
class Signal {
	public double A;
	public double T;
	public double kw;
	public double t0;

	public Signal(double A, double T, double kw, double t0) {
		this.A = A;
		this.T = T;
		this.kw = kw;
		this.t0 = t0;
	}
}

// Rectangle
class RectangleSignal extends Signal implements UnivariateRealFunction {

	public RectangleSignal(double A, double T, double kw, double t0) {
		super(A, T, kw, t0);
	}

	@Override
	public double value(double t) throws FunctionEvaluationException {
		double floorValue = floor(t / T);
		for (double k = floorValue - 1; k <= floorValue + 1; ++k) {
			if (t >= k * T + t0 && t < kw * T + k * T + t0) {
				return A;
			}
		}
		return 0.0;
	}
}

// RectangleSimmetric
class RectangleSimmetricSignal extends Signal implements UnivariateRealFunction {

	public RectangleSimmetricSignal(double A, double T, double kw, double t0) {
		super(A, T, kw, t0);
	}

	private double tempValue(double t) {
		double floorValue = floor(t / T);
		for (double k = floorValue - 1; k <= floorValue + 1; ++k) {
			if (t >= k * T + t0 && t < kw * T + k * T + t0) {
				return A;
			}
		}
		return 0.0;
	}

	@Override
	public double value(double t) throws FunctionEvaluationException {
		if (tempValue(t) == 0.0) {
			return -A;
		}
		return A;
	}
}

// Triangle
class TriangleSignal extends Signal implements UnivariateRealFunction {

	public TriangleSignal(double A, double T, double kw, double t0) {
		super(A, T, kw, t0);
	}

	@Override
	public double value(double t) throws FunctionEvaluationException {
		double floorValue = floor(t / T);
		for (double k = floorValue - 1; k <= floorValue + 1; ++k) {
			if (t >= k * T + t0 && t < kw * T + k * T + t0) {
				return (A / (kw * T)) * (t - k * T - t0);
			}
		}
		for (double k = floorValue - 1; k <= floorValue + 1; ++k) {
			if (t >= kw * T + t0 + k * T && t < T + k * T + t0) {
				return (-A / (T * (1 - kw))) * (t - k * T - t0)
						+ (A / (1 - kw));
			}
		}
		return 0;
	}
}

// JumpSignal
class JumpSignal extends Signal implements UnivariateRealFunction {
	public double ts;

	public JumpSignal(double A, double ts) {
		super(A, 0, 0, 0);
		this.ts = ts;
	}

	@Override
	public double value(double t) throws FunctionEvaluationException {
		if (t > ts) {
			return A;
		} else if (t == ts) {
			return A / 2;
		} else {
			return 0;
		}
	}

}

// ConstNoiseSignal
class ConstNoiseSignal extends Signal implements UnivariateRealFunction {

	public ConstNoiseSignal(double A) {
		super(A, 0, 0, 0);
	}

	@Override
	public double value(double t) throws FunctionEvaluationException {
		double sign;
		Random rand = new Random();
		if (rand.nextBoolean()) {
			sign = 1;
		} else {
			sign = -1;
		}
		return A * rand.nextDouble() * sign;
	}
}

// GaussNoiseSignal
class GaussNoiseSignal extends Signal implements UnivariateRealFunction {

	public GaussNoiseSignal(double A) {
		super(A, 0, 0, 0);
	}

	@Override
	public double value(double t) throws FunctionEvaluationException {
		double sign;
		Random rand = new Random();
		if (rand.nextBoolean()) {
			sign = 1;
		} else {
			sign = -1;
		}
		return A * rand.nextGaussian() * sign;
	}

}

// ImpulseSignal
class ImpulseSignal extends Signal implements UnivariateRealFunction {
	public int ns;

	public ImpulseSignal(double A, int ns) {
		super(A, 0, 0, 0);
	}

	@Override
	public double value(double t) throws FunctionEvaluationException {
		if (ns == t) {
			return A;
		} else {
			return 0;
		}
	}
}

// ImpulseNoise
class ImpulseNoise extends Signal implements UnivariateRealFunction {
	public double probability;

	public ImpulseNoise(double A, double probability) {
		super(A, 0, 0, 0);
		this.probability = probability;
	}

	@Override
	public double value(double t) throws FunctionEvaluationException {
		Random rand = new Random();
		if (rand.nextDouble() >= (1.0 - probability)) {
			return A;
		} else {
			return 0;
		}
	}

}

