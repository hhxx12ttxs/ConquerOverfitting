<<<<<<< HEAD
/*
 * Copyright 2012 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.pdf417.decoder.ec;

import com.google.zxing.ChecksumException;

/**
 * <p>PDF417 error correction implementation.</p>
 *
 * <p>This <a href="http://en.wikipedia.org/wiki/Reed%E2%80%93Solomon_error_correction#Example">example</a>
 * is quite useful in understanding the algorithm.</p>
 *
 * @author Sean Owen
 * @see com.google.zxing.common.reedsolomon.ReedSolomonDecoder
 */
public final class ErrorCorrection {

  private final ModulusGF field;

  public ErrorCorrection() {
    this.field = ModulusGF.PDF417_GF;
  }

  public void decode(int[] received,
                     int numECCodewords,
                     int[] erasures) throws ChecksumException {

    ModulusPoly poly = new ModulusPoly(field, received);
    int[] S = new int[numECCodewords];
    boolean error = false;
    for (int i = numECCodewords; i > 0; i--) {
      int eval = poly.evaluateAt(field.exp(i));
      S[numECCodewords - i] = eval;
      if (eval != 0) {
        error = true;
      }
    }

    if (error) {

      ModulusPoly knownErrors = field.getOne();
      for (int erasure : erasures) {
        int b = field.exp(received.length - 1 - erasure);
        // Add (1 - bx) term:
        ModulusPoly term = new ModulusPoly(field, new int[] { field.subtract(0, b), 1 });
        knownErrors = knownErrors.multiply(term);
      }

      ModulusPoly syndrome = new ModulusPoly(field, S);
      //syndrome = syndrome.multiply(knownErrors);

      ModulusPoly[] sigmaOmega =
          runEuclideanAlgorithm(field.buildMonomial(numECCodewords, 1), syndrome, numECCodewords);
      ModulusPoly sigma = sigmaOmega[0];
      ModulusPoly omega = sigmaOmega[1];

      //sigma = sigma.multiply(knownErrors);

      int[] errorLocations = findErrorLocations(sigma);
      int[] errorMagnitudes = findErrorMagnitudes(omega, sigma, errorLocations);

      for (int i = 0; i < errorLocations.length; i++) {
        int position = received.length - 1 - field.log(errorLocations[i]);
        if (position < 0) {
          throw ChecksumException.getChecksumInstance();
        }
        received[position] = field.subtract(received[position], errorMagnitudes[i]);
      }
    }
  }

  private ModulusPoly[] runEuclideanAlgorithm(ModulusPoly a, ModulusPoly b, int R)
      throws ChecksumException {
    // Assume a's degree is >= b's
    if (a.getDegree() < b.getDegree()) {
      ModulusPoly temp = a;
      a = b;
      b = temp;
    }

    ModulusPoly rLast = a;
    ModulusPoly r = b;
    ModulusPoly tLast = field.getZero();
    ModulusPoly t = field.getOne();

    // Run Euclidean algorithm until r's degree is less than R/2
    while (r.getDegree() >= R / 2) {
      ModulusPoly rLastLast = rLast;
      ModulusPoly tLastLast = tLast;
      rLast = r;
      tLast = t;

      // Divide rLastLast by rLast, with quotient in q and remainder in r
      if (rLast.isZero()) {
        // Oops, Euclidean algorithm already terminated?
        throw ChecksumException.getChecksumInstance();
      }
      r = rLastLast;
      ModulusPoly q = field.getZero();
      int denominatorLeadingTerm = rLast.getCoefficient(rLast.getDegree());
      int dltInverse = field.inverse(denominatorLeadingTerm);
      while (r.getDegree() >= rLast.getDegree() && !r.isZero()) {
        int degreeDiff = r.getDegree() - rLast.getDegree();
        int scale = field.multiply(r.getCoefficient(r.getDegree()), dltInverse);
        q = q.add(field.buildMonomial(degreeDiff, scale));
        r = r.subtract(rLast.multiplyByMonomial(degreeDiff, scale));
      }

      t = q.multiply(tLast).subtract(tLastLast).negative();
    }

    int sigmaTildeAtZero = t.getCoefficient(0);
    if (sigmaTildeAtZero == 0) {
      throw ChecksumException.getChecksumInstance();
    }

    int inverse = field.inverse(sigmaTildeAtZero);
    ModulusPoly sigma = t.multiply(inverse);
    ModulusPoly omega = r.multiply(inverse);
    return new ModulusPoly[]{sigma, omega};
  }

  private int[] findErrorLocations(ModulusPoly errorLocator) throws ChecksumException {
    // This is a direct application of Chien's search
    int numErrors = errorLocator.getDegree();
    int[] result = new int[numErrors];
    int e = 0;
    for (int i = 1; i < field.getSize() && e < numErrors; i++) {
      if (errorLocator.evaluateAt(i) == 0) {
        result[e] = field.inverse(i);
        e++;
      }
    }
    if (e != numErrors) {
      throw ChecksumException.getChecksumInstance();
    }
    return result;
  }

  private int[] findErrorMagnitudes(ModulusPoly errorEvaluator,
                                    ModulusPoly errorLocator,
                                    int[] errorLocations) {
    int errorLocatorDegree = errorLocator.getDegree();
    int[] formalDerivativeCoefficients = new int[errorLocatorDegree];
    for (int i = 1; i <= errorLocatorDegree; i++) {
      formalDerivativeCoefficients[errorLocatorDegree - i] =
          field.multiply(i, errorLocator.getCoefficient(i));
    }
    ModulusPoly formalDerivative = new ModulusPoly(field, formalDerivativeCoefficients);

    // This is directly applying Forney's Formula
    int s = errorLocations.length;
    int[] result = new int[s];
    for (int i = 0; i < s; i++) {
      int xiInverse = field.inverse(errorLocations[i]);
      int numerator = field.subtract(0, errorEvaluator.evaluateAt(xiInverse));
      int denominator = field.inverse(formalDerivative.evaluateAt(xiInverse));
      result[i] = field.multiply(numerator, denominator);
    }
    return result;
  }
=======
package skylight1.opengl.files;

/**
 * Parses strings to numbers quickly.
 * Arguments not checked or trimmed.
 * Limited formats accepted.
 * 
 */
public class QuickParseUtil {

	/**
	 * Parse string containing a float.
	 * Assumes input trimmed, not null, not infinity, not NaN, not using exponent representation.
	 * Handles only: [-+]?\d+(.\d+)?
	 * <p>
	 * Current implementation took 12ms to parse 1000 test strings,
	 * Float.parseFloat took 465ms to parse the same strings.
	 * 
	 * @param aInput
	 *            String representation of a float number
	 * @return parsed float
	 */
	public static float parseFloat(final String aInput) {
		// XXX Might be cleaner to use Float.intBitsToFloat with a researched algorithm to find inputs:
		// http://portal.acm.org/citation.cfm?doid=93542.93557

		// Read sign.
		int index = 0;
		boolean isNegative = false;
		switch (aInput.charAt(0)) {
			case '-':
				isNegative = true;
				// Fall through.
			case '+':
				index++;
				// No default action.
		}

		// Read integer before the decimal.
		final int length = aInput.length();
		int integer = 0;
		for (; index < length; index++) {
			char character = aInput.charAt(index);
			if (character == '.') {
				index++;
				break;
			}

			// Pre-calculated power values in an array doesn't seem to help speed
			// when using integer math. It did previously when using float math.
			// Maybe Java's array bounds checking slows it down.
			integer *= 10;
			integer += character - '0';
		}

		// Read fraction after the decimal.
		int numerator = 0;
		int denominator = 1;
		for (; index < length; index++) {
			char character = aInput.charAt(index);

			denominator *= 10;
			numerator *= 10;
			numerator += character - '0';
		}

		// Calculate and return result.
		float result = integer + numerator / (float) denominator;
		return isNegative ? -result : result;
	}
	

	private final static int[][] INTEGER_DECIMAL_VALUES = new int[4][10];

	static {
		for (int decimalPlace = 0; decimalPlace < INTEGER_DECIMAL_VALUES.length; decimalPlace++) {
			for (int decimalValue = 0; decimalValue < 10; decimalValue++) {
				INTEGER_DECIMAL_VALUES[decimalPlace][decimalValue] = (int) (Math.pow(10d, decimalPlace) * decimalValue);
			}
		}
	}

	public static int parseInteger(final String aStringRepresentationOfAnInteger) {
		final int startOfDigits;
		final int sign;
		if (aStringRepresentationOfAnInteger.charAt(0) == '-') {
			startOfDigits = 1;
			sign = -1;
		} else {
			startOfDigits = 0;
			sign = 1;
		}
		int result = 0;
		int decimalPlace = -1;
		final int stringLength = aStringRepresentationOfAnInteger.length();
		for (int i = stringLength - 1; i >= startOfDigits; i--) {
			decimalPlace++;
			result += INTEGER_DECIMAL_VALUES[decimalPlace][aStringRepresentationOfAnInteger.charAt(i) - '0'];
		}
		return sign * result;
	}
	
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

