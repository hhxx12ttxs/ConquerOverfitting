/*
 * This file is a part of the bfb java package for the analysis
 * of Breakage-Fusion-Bridge count vectors.
 *
 * Copyright (C) 2013 Shay Zakov, Marcus Kinsella, and Vineet Bafna.
 *
 * The bfb package is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The bfb package is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact:
 * Shay Zakov:		zakovs@gmail.com
 */

package bfb;


/**
 * A Poisson-based error model. The Poisson probability of observing q when the
 * observation mean is r is defined by P(X = q | r) = r^q * e^{-r}/ q!. The 
 * distance of q from r is defined by dist(q,r) = P(X = q | r) / P(X = r | r).
 * The distance between two vectors is the product of thier entry-wise distances.
 *  
 * 
 * @author Shay Zakov
 *
 */
public class PoissonErrorModel extends ErrorModel {

	private static final double HALF_LOG_PI = Math.log(Math.PI) / 2.;

	@Override
	public double error(int realValue, int observedValue) {
		return poissonProbabilityApproximation(realValue, observedValue) - poissonProbabilityApproximation(realValue, realValue);
	}

	@Override
	public double accumulate(double accumulatedError, double newError) {
		return accumulatedError + newError;
	}

	@Override
	public double maxCurrError(double accumulatedError, double maxError) {
		return maxError - accumulatedError;
	}


	@Override
	public double normlizeError(double error) {
		return 1 - Math.exp(error);
	}

	@Override
	public double deNormlizeError(double normlizedError) {
		return Math.log(1-normlizedError);
	}
	
	/**Calculates an approximation of the Poisson probability.
	 * @param mean - lambda, the average number of occurrences
	 * @param observed - the actual number of occurrences observed
	 * @return ln(Poisson probability) - the natural log of the Poisson probability.
	 */
	public static double poissonProbabilityApproximation (double mean, int observed) {
	        return observed * Math.log(mean) - mean - factorialApproximation(observed);
	}
	 
	/**Srinivasa Ramanujan ln(n!) factorial estimation.
	 * Good for larger values of n.
	 * @return ln(n!)
	 */
	public static double factorialApproximation(double n) {
	        if (n < 2.) return 0;
	        double a = n * Math.log(n) - n;
	        double b = Math.log(n * (1. + 4. * n * (1. + 2. * n))) / 6.;
	        return a + b + HALF_LOG_PI;
	}

	@Override
	public int compareErrors(double error1, double error2){
		if (error1 > error2) return -1;
		else if (error1 < error2) return 1;
		else return 0;
	}

}

