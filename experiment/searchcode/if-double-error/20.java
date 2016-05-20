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
 * An abstract error model.
 * 
 * @author Shay Zakov
 *
 */
public abstract class ErrorModel {

	private static int MAX_RATIO = 2; 

	public abstract double error(int realValue, int observedValue);
	public abstract double accumulate (double accumulatedError, double newError);
	public abstract double maxCurrError(double accumulatedError, double maxError);
	public double initialError(){return 0;}
	public int compareErrors(double error1, double error2){
		if (error1 < error2) return -1;
		else if (error1 > error2) return 1;
		else return 0;
	}
	
	/**
	 * Normalize the error to the 0-1 range, 0 means identical vectors and 1
	 * means extremely different vectors.
	 * 	
	 * @param error the unnormalized error.
	 * @return the normalized value of the input error.
	 */
	public abstract double normlizeError(double error);
	/**
	 * The reverse of the normalizeError method.
	 * 
	 * @param normlizedError
	 * @return
	 */
	public abstract double deNormlizeError(double normlizedError);

	public int minRealValue(int observedValue, double maxError){
		int deltaMax=1, deltaMin=0, deltaMid;
		while (deltaMax < observedValue && compareErrors(error(observedValue - deltaMax, observedValue), maxError) <= 0){
			deltaMin = deltaMax;
			if (deltaMax*2 <= observedValue){
				deltaMax *= 2;
			}
			else {
				deltaMax += (observedValue-deltaMax+1)/2;
			}
		}
		while(deltaMax - deltaMin > 1){
			deltaMid = (deltaMax + deltaMin)/2;
			if (compareErrors(error(observedValue - deltaMid, observedValue), maxError) <= 0){
				deltaMin = deltaMid;
			}
			else deltaMax = deltaMid;
		}
		return Math.max(1, observedValue - deltaMin); // not allowing real counts to be 0.
	}

	public int minObservedValue(int realValue, double maxError){
		int deltaMax=1, deltaMin=0, deltaMid;
		while (deltaMax < realValue && compareErrors(error(realValue, realValue - deltaMax), maxError) <= 0){
			deltaMin = deltaMax;
			if (deltaMax*2 <= realValue){
				deltaMax *= 2;
			}
			else {
				deltaMax += (realValue-deltaMax+1)/2;
			}
		}
		while(deltaMax - deltaMin > 1){
			deltaMid = (deltaMax + deltaMin)/2;
			if (compareErrors(error(realValue, realValue - deltaMid), maxError) <= 0){
				deltaMin = deltaMid;
			}
			else deltaMax = deltaMid;
		}

		return realValue - deltaMin;
	}

	public int maxRealValue(int observedValue, double maxError){
		int deltaMax=1, deltaMin=0, deltaMid;
		while (compareErrors(error(observedValue + deltaMax, observedValue), maxError) <= 0){
			deltaMin = deltaMax;
//			if (deltaMax*2 <= observedValue*MAX_RATIO){
				deltaMax *= 2;
//			}
//			else {
//				deltaMax += (observedValue*MAX_RATIO-deltaMax+1)/2;
//			}
		}
		while(deltaMax - deltaMin > 1){
			deltaMid = (deltaMax + deltaMin)/2;
			if (compareErrors(error(observedValue + deltaMid, observedValue), maxError) <= 0){
				deltaMin = deltaMid;
			}
			else deltaMax = deltaMid;
		}

		return observedValue + deltaMin;
	}

	public int maxObservedValue(int realValue, double maxError){
		int deltaMax=1, deltaMin=0, deltaMid;
		while (deltaMax < realValue*MAX_RATIO && compareErrors(error(realValue + deltaMax, realValue), maxError) <= 0){
			deltaMin = deltaMax;
			if (deltaMax*2 <= realValue*MAX_RATIO){
				deltaMax *= 2;
			}
			else {
				deltaMax += (realValue*MAX_RATIO-deltaMax+1)/2;
			}
		}
		while(deltaMax - deltaMin > 1){
			deltaMid = (deltaMax + deltaMin)/2;
			if (compareErrors(error(realValue + deltaMid, realValue), maxError) <= 0){
				deltaMin = deltaMid;
			}
			else deltaMax = deltaMid;
		}

		return realValue + deltaMin;
	}

	public double accumulate (double accumulatedError, int realValue, int observedValue){
		return accumulate(accumulatedError, error(realValue, observedValue));
	}
}

