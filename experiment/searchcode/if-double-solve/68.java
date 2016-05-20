package PROG8;

/**
 * Class: GaussianElimination
 * 
 * PSP 2.1 Program 8
 * 
 * @author Clippit
 * @version 1.0
 *
 */
public class GaussianElimination {
	private static final double EPSILON = 1e-10;

	/**
	 * solve
	 * 
	 * ????????????
	 * 
	 * @param A ????
	 * @param b ????
	 * @return ??
	 */
	public static double[] solve(double[][] A, double[] b) {
		int N = b.length;

		for (int p = 0; p < N; p++) {

			// find pivot row and swap
			int max = p;
			for (int i = p + 1; i < N; i++) {
				if (Math.abs(A[i][p]) > Math.abs(A[max][p])) {
					max = i;
				}
			}
			double[] temp = A[p];
			A[p] = A[max];
			A[max] = temp;
			double t = b[p];
			b[p] = b[max];
			b[max] = t;

			// singular or nearly singular
			if (Math.abs(A[p][p]) <= EPSILON) {
				throw new RuntimeException("Matrix is singular or nearly singular");
			}

			// pivot within A and b
			for (int i = p + 1; i < N; i++) {
				double alpha = A[i][p] / A[p][p];
				b[i] -= alpha * b[p];
				for (int j = p; j < N; j++) {
					A[i][j] -= alpha * A[p][j];
				}
			}
		}

		// back substitution
		double[] x = new double[N];
		for (int i = N - 1; i >= 0; i--) {
			double sum = 0.0;
			for (int j = i + 1; j < N; j++) {
				sum += A[i][j] * x[j];
			}
			x[i] = (b[i] - sum) / A[i][i];
		}
		return x;
	}

	/**
	 * sample test
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int N = 4;
		double[][] A = { { 6, 4863, 8761, 654 },
				{ 4863, 4521899, 8519938, 620707 },
				{ 8761, 8519938, 21022091, 905925 },
				{ 654, 620707, 905925, 137902 } };
		double[] b = { 714, 667832, 1265493, 100583 };
		double[] x = solve(A, b);

		// print results
		for (int i = 0; i < N; i++) {
			System.out.println(x[i]);
		}
	}

	/** 
	 * The End of Class
	 */
}

