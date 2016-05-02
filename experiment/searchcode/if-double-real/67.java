package fft;

public class Roots {

	private int count = 0;

	private double[] real = null;
	private double[] imaginaryForward = null;
	private double[] imaginaryInverse = null;

	public void calculate(int n) {

		if (n == count) {
			return;
		}

		real = new double[n];
		real[0] = 1.0;

		imaginaryForward = new double[n];
		imaginaryForward[0] = 0.0;

		imaginaryInverse = new double[n];
		imaginaryInverse[0] = 0.0;

		double cos = Math.cos(2.0 * Math.PI / n);
		double sin = Math.sin(2.0 * Math.PI / n);

		for (int i = 1; i < n; i++) {
			real[i] = real[i - 1] * cos + imaginaryForward[i - 1] * sin;
			imaginaryForward[i] = imaginaryForward[i - 1] * cos - real[i - 1] * sin;
			imaginaryInverse[i] = -imaginaryForward[i];
		}

		count = n;

	}

	public double getReal(int i) {
		return real[i];
	}

	public double getImaginary(int i) {
		return imaginaryForward[i];
	}
}

