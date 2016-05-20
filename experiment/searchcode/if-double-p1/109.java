/* Amanda Rampersad
 * COT 4500
 * Program 1: Newton Secant
 */

import java.util.*;
import java.lang.*;

public class newton {

	public static void main(String[] args) {

		// Get paramter values from input
		double p0 = Double.valueOf(args[0]);
		double p1 = Double.valueOf(args[1]);
		double tol = Double.valueOf(args[2]);
		int max = Integer.valueOf(args[3]);
		int lim = Integer.valueOf(args[4]);

		// Get f(x) polynomial coefficients from input - store in array
		int end = 5 + lim;
		int i;
		double[] poly = new double[lim + 1];

		for (i = 0; i < poly.length; i++) {
			poly[i] = Double.valueOf(args[end - i]);
		}

		// Echo input parameters, polynomial order, and terms of polynomial
		System.out.println("Input Parameters:");
		System.out.println("    " + "p0 = " + p0);
		System.out.println("    " + "p1 = " + p1);
		System.out.println("    " + "tol = " + tol);
		System.out.println("    " + "max = " + max);
		System.out.println("Polynomial is of order: " + lim);
		System.out.printf("Terms of polynomial: ");
		
		// Prints terms of input polynomial
		printPoly(poly);

		// Compute derivative of input polynomial
		double[] derivative = derivative(poly);

		// Compute root by using the Newton and Secant methods
		Newton(p0, tol, max, poly, derivative);
		Secant(p0, p1, tol, max, poly);

	}

	// Newton method algorithm
	public static void Newton(double p0, double tol, int max, double[] poly,
			double[] derivative) {

		System.out.println("Newton's Method:");

		int i = 1;

		while (i <= max) {
			// Compute f(p0) and f'(p0)
			double fp0 = compute(poly, p0);
			double f1p0 = compute(derivative, p0);

			// If f'(p0) = 0, this causes a divide by zero 0 - exit
			// Otherwise, continue with Newton's method
			if (f1p0 == 0.0) {
				System.out.println("    Divide by zero error detected. Exiting.");
				break;
			} else {
				// Set p = p0 - f(p0) / f'(p0)
				double p = p0 - (fp0 / f1p0);

				// If |p - p0| < tol, output solution and stop
				// Else, increment i by 1 and set p0 = p
				if (Math.abs(p - p0) < tol) {
					System.out.println("    p" + i + " = " + p);
					System.out.println("    Solution found after " + i
							+ " iterations: " + p);
					break;
				} else {
					System.out.println("    p" + i + " = " + p);
					i++;
					p0 = p;
				}
			}
		}

		// If ith iteration is greater than the max, no solution has been found
		if (i > max) {
			System.out.println("    No solution found after " + max
					+ " iterations.");
		}

	}

	// Secant method algorithm
	public static void Secant(double p0, double p1, double tol, int max,
			double[] poly) {

		System.out.println("Secant Method:");

		int i = 2;
		
		// Set q0 = f(p0) and q1 = f(p1)
		double q0 = compute(poly, p0);
		double q1 = compute(poly, p1);


		while (i <= max) {
			// If q1 - q0 = 0, this causes a divide by zero error - exit
			// Otherwise, continue with Secant method
			if (q1 - q0 == 0.0) {
				System.out.println("    Divide by zero error detected. Exiting.");
				break;
			} else {
				// Set p = p1 - q1 * (p1 - p0) / (q1 - q0)
				double p = p1 - q1 * (p1 - p0) / (q1 - q0);
				
				// If |p - p1| < tol, output solution and stop
				// Else, increment i by 1 and update p0, q0, p1, q1
				if (Math.abs(p - p1) < tol) {
					System.out.println("    p" + i + " = " + p);
					System.out.println("    Solution found after " + i
							+ " iterations: " + p);
					break;
				} else {
					System.out.println("    p" + i + " = " + p);
					i++;
					p0 = p1;
					q0 = q1;
					p1 = p;
					q1 = compute(poly, p);
				}
			}
		}

		// If ith iteration is greater than the max, no solution has been found
		if (i > max) {
			System.out.println("    No solution found after " + max
					+ " iterations.");
		}

	}

	// Computes the derivative of the input polynomial and returns coefficients in an array
	public static double[] derivative(double[] coefficients) {

		double[] derivative = new double[coefficients.length - 1];

		int i;

		for (i = 1; i < coefficients.length; i++) {
			derivative[i - 1] = coefficients[i] * i;
		}

		return derivative;

	}

	// Computes f(x) for a function f with an array of coefficients and returns the double
	public static double compute(double[] coefficients, double x) {

		double p = 0;
		int i;

		for (i = 0; i < coefficients.length; i++) {
			p += Math.pow(x, i) * coefficients[i];
		}

		return p;

	}

	// Prints the terms of a polynomial given then coefficients in an array
	public static void printPoly(double[] coefficients) {

		int i;

		for (i = coefficients.length - 1; i >= 0; i--) {
			if (i > 0) {
				System.out.printf("%.1f*x^%d + ", coefficients[i], i);
			} else {
				System.out.printf("%.1f*x^%d", coefficients[i], i);
			}
		}

		System.out.println();

	}

}

