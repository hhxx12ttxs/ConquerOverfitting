<<<<<<< HEAD
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.waveprotocol.wave.model.operation.testing;

import java.math.BigInteger;

public final class Rational {
  private static final BigInteger BI_M1 = BigInteger.valueOf(-1);

  public static final Rational ZERO = new Rational(0, 1);
  public static final Rational ONE = new Rational(1, 1);
  public static final Rational MINUS_ONE = new Rational(-1, 1);
  public static final Rational TWO = new Rational(2, 1);

  final BigInteger numerator;
  final BigInteger denominator;

  public Rational(int numerator, int denominator) {
    this(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
  }

  public Rational(BigInteger numerator, BigInteger denominator) {
    if (denominator.equals(BigInteger.ZERO)) {
      throw new IllegalArgumentException("Denominator must != 0");
    } else if (denominator.compareTo(BigInteger.ZERO) < 0) {
      denominator = denominator.multiply(BI_M1);
      numerator = numerator.multiply(BI_M1);
    }
//    BigInteger negative = numerator.signum() < 0 ? minusOne : BigInteger.ONE;
//    numerator = numerator.multiply(negative);
    BigInteger gcd = numerator.gcd(denominator);
    this.numerator = numerator.divide(gcd);
    this.denominator = denominator.divide(gcd);
  }

  public Rational plus(Rational other) {
    return new Rational(
        numerator.multiply(other.denominator).add(other.numerator.multiply(denominator)),
        denominator.multiply(other.denominator));
  }

  public Rational minus(Rational other) {
    return new Rational(
        numerator.multiply(other.denominator).add(
            BI_M1.multiply(other.numerator.multiply(denominator))),
        denominator.multiply(other.denominator));
  }

  public Rational times(Rational other) {
    return new Rational(
        numerator.multiply(other.numerator),
        denominator.multiply(other.denominator));
  }

  public Rational dividedBy(Rational other) {
    if (other.numerator.equals(BigInteger.ZERO)) {
      throw new IllegalArgumentException("Division by zero");
    }
    return times(other.reciprocal());
  }

  public Rational reciprocal() {
    if (numerator.equals(BigInteger.ZERO)) {
      throw new IllegalArgumentException("Division by zero");
    }
    return new Rational(denominator, numerator);
  }

  @Override
  public String toString() {
    return numerator +
        (denominator.equals(BigInteger.ONE) || denominator.equals(BigInteger.ZERO)
        ? "" : "/" + denominator);
  }

  // eclipse generated

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((denominator == null) ? 0 : denominator.hashCode());
    result = prime * result + ((numerator == null) ? 0 : numerator.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Rational other = (Rational) obj;
    if (denominator == null) {
      if (other.denominator != null)
        return false;
    } else if (!denominator.equals(other.denominator))
      return false;
    if (numerator == null) {
      if (other.numerator != null)
        return false;
    } else if (!numerator.equals(other.numerator))
      return false;
    return true;
  }
}
=======
/*
 *  MathExercisesJavaAppletBase
 * 
 * by Albert Zeyer / developed for Lehrstuhl A f端r Mathematik at the RWTH Aachen University
 * code under GPLv3+
 */
package applets.Termumformungen$in$der$Technik_08_Ethanolloesungen;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class OTNumber {
	private BigDecimal decimal = null;
	private BigInteger nominator = null, denominator = null;
	
	/** @throws NumberFormatException */
	OTNumber(String dec) {
		int p = dec.indexOf('/');
		if(p >= 0) {
			nominator = new BigInteger(dec.substring(0,p));
			denominator = new BigInteger(dec.substring(p+1));
		}
		else
			decimal = new BigDecimal(dec);
	}
	OTNumber(long num) { this(new BigDecimal(num)); }
	OTNumber(BigInteger dec) { this(new BigDecimal(dec)); }
	OTNumber(BigDecimal dec) {
		if(dec == null) throw new IllegalArgumentException("dec == null");
		decimal = dec;
	}
	OTNumber(long nom, long denom) { this(BigInteger.valueOf(nom), BigInteger.valueOf(denom)); }
	OTNumber(BigInteger nom, BigInteger denom) {
		if(nom == null) throw new IllegalArgumentException("nom == null");
		if(denom == null) throw new IllegalArgumentException("denom == null");
		nominator = nom;
		denominator = denom;
	}

	static OTNumber valueOf(long num) { return new OTNumber(num); }
	static final OTNumber ZERO = valueOf(0);
	static final OTNumber ONE = valueOf(1);
	
	OTNumber asNormatedFrac() {
		OTNumber frac = asFrac();
		BigInteger gcd = frac.nominator.gcd(frac.denominator);
		if(frac.denominator.compareTo(BigInteger.ZERO) <= 0) gcd = gcd.negate();
		return new OTNumber(frac.nominator.divide(gcd), frac.denominator.divide(gcd));
	}

	OTNumber asFrac() {
		if(decimal == null) return this; // we are already
		try { return new OTNumber(decimal.toBigIntegerExact(), BigInteger.ONE); }
		catch(ArithmeticException ignored) {}
		return new OTNumber(decimal.unscaledValue(), BigInteger.TEN.pow(decimal.scale()));
	}
	
	OTNumber normated() {
		if(decimal != null) return this;
		OTNumber frac = asNormatedFrac();
		if(frac.denominator.compareTo(BigInteger.ONE) == 0) return new OTNumber(frac.nominator);
		return frac;
	}
	
	/** @throws ArithmeticException */
	static BigInteger divideBigInt(BigInteger int1, BigInteger int2) {
		BigInteger[] div = int1.divideAndRemainder(int2);
		if(div[1].compareTo(BigInteger.ZERO) == 0) return div[0];
		throw new ArithmeticException("cannot divide " + int1 + " by " + int2);
	}
	
	/** @throws ArithmeticException */
	OTNumber asDecimal() {
		if(decimal != null) return this;
		return new OTNumber(divideBigInt(nominator, denominator));
	}
	/** @throws ArithmeticException */
	int intValueExact() { return asDecimal().decimal.intValueExact(); }
	/** @throws ArithmeticException */
	BigInteger toBigIntegerExact() { return asDecimal().decimal.toBigIntegerExact(); }	
	
	@Override public String toString() {
		if(decimal != null) return decimal.toPlainString();
		return nominator.toString() + "/" + denominator.toString();
	}

	boolean isDecimal(BigDecimal dec) {
		try { return asDecimal().decimal.compareTo(dec) == 0; }
		catch(ArithmeticException ignored) { return false; }
	}
	boolean isZero() { return isDecimal(BigDecimal.ZERO); }
	boolean isOne() { return isDecimal(BigDecimal.ONE); }
	/** @throws ArithmeticException */
	int compareToZero() {
		if(decimal != null) return decimal.compareTo(BigDecimal.ZERO);
		int nomC = nominator.compareTo(BigInteger.ZERO);
		int denomC = denominator.compareTo(BigInteger.ZERO);
		if(denomC == 0) throw new ArithmeticException("cannot compare undefined number to zero");
		if(nomC == 0) return 0;
		if(nomC > 0 && denomC > 0) return 1;
		if(nomC < 0 && denomC < 0) return 1;
		return -1;
	}
	/** @throws ArithmeticException */
	boolean isPositive() { return compareToZero() >= 0; }
	
	/** @throws ArithmeticException */
	int compareTo(OTNumber other) {
		if(other.isZero()) return compareToZero();
		OTNumber divided = divide(other).asNormatedFrac();
		if(divided.isOne()) return 0;
		if(divided.compareToZero() <= 0) return -1;
		return divided.nominator.compareTo(divided.denominator);
	}

	boolean isInteger() {
		try { toBigIntegerExact(); return true; }
		catch(ArithmeticException ignored) { return false; }
	}
	
	OTNumber negate() {
		if(decimal != null) return new OTNumber(decimal.negate());
		return new OTNumber(nominator.negate(), denominator);
	}
	
	OTNumber multiplicativeInverse() {
		if(decimal != null && decimal.scale() > 0) {
			try { return new OTNumber(BigDecimal.ONE.divide(decimal));
			} catch(ArithmeticException ignored) {}
		}
		OTNumber frac = asFrac();
		if(frac.nominator.compareTo(BigInteger.ZERO) >= 0)
			return new OTNumber(frac.denominator, frac.nominator);
		else
			return new OTNumber(frac.denominator.negate(), frac.nominator.negate());
	}
	
	OTNumber add(OTNumber other) {
		if(decimal != null && other.decimal != null) return new OTNumber(decimal.add(other.decimal));
		OTNumber thisFrac = asNormatedFrac(), otherFrac = other.asNormatedFrac();
		BigInteger gcd = thisFrac.denominator.gcd(otherFrac.denominator);
		BigInteger nom1 = thisFrac.nominator.multiply(otherFrac.denominator.divide(gcd));
		BigInteger nom2 = otherFrac.nominator.multiply(thisFrac.denominator.divide(gcd));
		BigInteger denom = thisFrac.denominator.divide(gcd).multiply(otherFrac.denominator);
		return new OTNumber(nom1.add(nom2), denom).normated();
	}
	
	OTNumber subtract(OTNumber other) {
		return add(other.negate());
	}
	
	OTNumber multiply(OTNumber other) {
		if(decimal != null && other.decimal != null) return new OTNumber(decimal.multiply(other.decimal));
		OTNumber thisFrac = asFrac(), otherFrac = other.asFrac();
		return new OTNumber(
				thisFrac.nominator.multiply(otherFrac.nominator),
				thisFrac.denominator.multiply(otherFrac.denominator)).normated();
	}

	OTNumber divide(OTNumber other) {
		if(decimal != null && other.decimal != null) {
			try {
				OTNumber divided = new OTNumber(decimal.divide(other.decimal));
				if(divided.isInteger()) return divided;
			} catch(ArithmeticException ignored) {}
		}
		return multiply(other.multiplicativeInverse());
	}
	
	OTNumber pow(int n) {
		if(decimal != null) return new OTNumber(decimal.pow(n));
		return new OTNumber(nominator.pow(n), denominator.pow(n));
	}
	
	OTNumber gcd(OTNumber other) {
		OTNumber thisFrac = asNormatedFrac(), otherFrac = other.asNormatedFrac();
		BigInteger gcd = thisFrac.denominator.gcd(otherFrac.denominator);
		BigInteger nom1 = thisFrac.nominator.multiply(otherFrac.denominator.divide(gcd));
		BigInteger nom2 = otherFrac.nominator.multiply(thisFrac.denominator.divide(gcd));
		return new OTNumber(nom1.gcd(nom2));
	}
	
	static BigInteger gcdInt(Iterable<BigInteger> nums) {
		BigInteger c = null;
		for(BigInteger n : nums) {
			if(c == null) c = n;
			else c = c.gcd(n); 
		}
		if(c == null) return BigInteger.ONE;
		return c;
	}
	
	static BigInteger lcmInt(Iterable<BigInteger> nums) {
		BigInteger c = null;
		for(BigInteger n : nums) {
			if(c == null) c = n;
			else c = c.divide(c.gcd(n)).multiply(n); 
		}
		if(c == null) return BigInteger.ONE;
		return c;
	}

	static OTNumber gcd(Iterable<OTNumber> nums) {
		List<OTNumber> numList = new ArrayList<OTNumber>(Utils.collFromIter(Utils.map(nums, new Utils.Function<OTNumber,OTNumber>() {
			public OTNumber eval(OTNumber obj) {
				return obj.asNormatedFrac();
			}
		})));
		final BigInteger lcm = lcmInt(Utils.map(numList, new Utils.Function<OTNumber,BigInteger>() {
			public BigInteger eval(OTNumber obj) {
				return obj.asNormatedFrac().denominator;
			}
		}) );
		numList = new ArrayList<OTNumber>(Utils.collFromIter(Utils.map(numList, new Utils.Function<OTNumber,OTNumber>() {
			public OTNumber eval(OTNumber obj) {
				return obj.multiply(new OTNumber(lcm));
			}
		})));
		return new OTNumber(
				gcdInt(Utils.map(numList, new Utils.Function<OTNumber,BigInteger>() {
					public BigInteger eval(OTNumber obj) {
						return obj.toBigIntegerExact();
					}
				})),
				lcm).normated();
	}
	
	static void test() {
		if(new OTNumber(3,2).compareTo(ONE) <= 0) throw new AssertionError();
	}
	
}

>>>>>>> 76aa07461566a5976980e6696204781271955163
