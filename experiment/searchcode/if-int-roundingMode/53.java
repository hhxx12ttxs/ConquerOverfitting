package java.math;

import java.io.Serializable;

public class BigDecimal extends Number implements Cloneable, Serializable,
		Comparable/*<BigDecimal>*/ {

	/*
	 * MIEI CAMPI
	 */

	private transient InternalBigDecimal internalBigDecimal = null;

	// Rounding Modes

	public final transient static int ROUND_UP = 0;

	public final transient static int ROUND_DOWN = 1;

	public final transient static int ROUND_CEILING = 2;

	public final transient static int ROUND_FLOOR = 3;

	public final transient static int ROUND_HALF_UP = 4;

	public final transient static int ROUND_HALF_DOWN = 5;

	public final transient static int ROUND_HALF_EVEN = 6;

	public final transient static int ROUND_UNNECESSARY = 7;

	// Useful constants
	public final transient static BigDecimal ZERO = new BigDecimal("0");

	public final transient static BigDecimal ONE = new BigDecimal("1");

	public final transient static BigDecimal TEN = new BigDecimal("10");
	// ***********************************************

	/* Appease the serialization gods */
	private static final long serialVersionUID = 1L;

	// Constructors

	public BigDecimal() {
		this("0");
	}

	public BigDecimal(String val) {
		try {
			Double.parseDouble(val);
			internalBigDecimal = new InternalBigDecimal(val);
		} catch (Exception e) {
			throw new NumberFormatException(
					"Value of BigDecimal isn't a right value.");
		}
	}

	// ADD into 2.0.2
	public BigDecimal(int val) {
		this("" + val);
	}

	private BigDecimal(long val) {
		this("" + val);
	}

	public BigDecimal(double val) {
		this("" + val);
	}

	
	 public BigDecimal(BigInteger val) {
		 this("" + val);
	 }
	
	 public BigDecimal(BigInteger val, int scale) {
		 this("" + val);
		 setScale(scale);
	 }

	// Static Factory Methods
	public static BigDecimal valueOf(long unscaledVal, int scale) {
		BigDecimal bd = new BigDecimal(unscaledVal);
		bd.setScale(scale);
		return bd;
	}

	public static BigDecimal valueOf(long unscaledVal) {
		return valueOf(unscaledVal, 0);
	}

	// Arithmetic Operations
	public BigDecimal pow(int n) {
		if (n < 0 || n > 999999999)
			throw new ArithmeticException("Invalid operation");

		if (n == 0) {
			return ONE;
		}
		long newScale = scale() * n;
		if (newScale > Integer.MAX_VALUE)
			throw new ArithmeticException("Underflow");
		if (newScale < Integer.MIN_VALUE)
			throw new ArithmeticException("Overflow");

		BigDecimal res = this;
		for (int i = 0; i < n-1; i++) {
			res = res.multiply(this).setScale((int) newScale,
					BigDecimal.ROUND_HALF_UP);
		}

		return res;
	}


	// ADD into 2.0.3
	public BigDecimal remainder(BigDecimal val) {
		final InternalBigDecimal i = internalBigDecimal
				.remainder(val.internalBigDecimal);
		final BigDecimal b = new BigDecimal(i.toString());
		return b;
	}

	public BigDecimal add(BigDecimal val) {
		final InternalBigDecimal i = internalBigDecimal
				.add(val.internalBigDecimal);
		final BigDecimal b = new BigDecimal(i.toString());
		return b;

	}

	public BigDecimal subtract(BigDecimal val) {
		final InternalBigDecimal i = internalBigDecimal
				.subtract(val.internalBigDecimal);
		final BigDecimal b = new BigDecimal(i.toString());
		return b;

	}

	public BigDecimal multiply(BigDecimal val) {
		final InternalBigDecimal i = internalBigDecimal
				.multiply(val.internalBigDecimal);
		final BigDecimal b = new BigDecimal(i.toString());
		return b;

	}

	public BigDecimal divide(BigDecimal val, int scale, int roundingMode) {
		final InternalBigDecimal i = internalBigDecimal.divide(
				val.internalBigDecimal, scale, roundingMode);
		final BigDecimal b = new BigDecimal(i.toString());
		return b;
	}

	public BigDecimal divide(BigDecimal val, int roundingMode) {
		final InternalBigDecimal i = internalBigDecimal.divide(
				val.internalBigDecimal, roundingMode);
		final BigDecimal b = new BigDecimal(i.toString());
		return b;
	}

	public BigDecimal abs() {
		final InternalBigDecimal i = internalBigDecimal.abs();
		final BigDecimal b = new BigDecimal(i.toString());
		return b;
	}

	public BigDecimal negate() {
		final InternalBigDecimal i = internalBigDecimal.negate();
		final BigDecimal b = new BigDecimal(i.toString());
		return b;
	}

	public int signum() {
		return internalBigDecimal.signum();
	}

	public int scale() {
		return internalBigDecimal.scale();
	}

	public BigInteger unscaledValue() {
		String s = internalBigDecimal.toString();
		String s2 = s.replaceAll("\\.", "");
		return new BigInteger(s2);
	}

	/*
	 * into Shell Ok outside NO public BigDecimal pow(int n) {
	 * 
	 * for (int i = 0; i < Math.abs(n); i++) { InternalBigDecimal multiply =
	 * internalBigDecimal.multiply(internalBigDecimal); internalBigDecimal =
	 * multiply; }
	 * 
	 * InternalBigDecimal divide; if(n<0){ BigDecimal menoUno = new
	 * BigDecimal("1"); divide =
	 * menoUno.internalBigDecimal.divide(internalBigDecimal); } else{ divide =
	 * internalBigDecimal; }
	 * 
	 * 
	 * final BigDecimal b = new BigDecimal(divide.toString()); return b; }
	 */
	// Scaling/Rounding Operations
	public BigDecimal setScale(int scale, int roundingMode) {

		final InternalBigDecimal i = internalBigDecimal.setScale(scale,
				roundingMode);
		final BigDecimal b = new BigDecimal(i.toString());
		return b;
	}

	public BigDecimal setScale(int scale) {
		return setScale(scale, ROUND_UNNECESSARY);
	}

	// Decimal Point Motion Operations

	// public BigDecimal movePointLeft(int n) {
	// throw new
	// IllegalArgumentException("movePointLeft(int n) not implemented");
	// }
	//
	// public BigDecimal movePointRight(int n) {
	// throw new
	// IllegalArgumentException("movePointRight(int n) not implemented");
	// }

	// Comparison Operations

	public int compareTo(Object val) {
		if (!(val instanceof BigDecimal)) {
			throw new IllegalArgumentException(
					"method compareTo only with BigDecimal");
		}
		return internalBigDecimal.compareTo(((BigDecimal)val).internalBigDecimal);
	}

	// public int compareTo(Object o) {
	// if (!(o instanceof BigDecimal)) {
	// throw new
	// IllegalArgumentException("method compareTo only with BigDecimal");
	// }
	//
	// return compareTo((BigDecimal) o);
	// }

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj instanceof BigDecimal) {
			return internalBigDecimal
					.equals(((BigDecimal) obj).internalBigDecimal);
		} else {
			return false;
		}
	}

	public BigDecimal min(BigDecimal val) {
		InternalBigDecimal i = new InternalBigDecimal(val.internalBigDecimal
				.toString());
		final InternalBigDecimal i2 = internalBigDecimal.min(i);
		final BigDecimal b = new BigDecimal(i2.toString());
		return b;

	}

	public BigDecimal max(BigDecimal val) {
		InternalBigDecimal i = new InternalBigDecimal(val.internalBigDecimal
				.toString());
		final InternalBigDecimal i2 = internalBigDecimal.max(i);
		final BigDecimal b = new BigDecimal(i2.toString());
		return b;
	}

//	//	@Override
	public int intValue() {
		return toBigInteger().intValue();
	}

	//	@Override
	public double doubleValue() {
		return new Double(this.toString());
	}

	//	@Override
	public float floatValue() {
		return new Float(this.toString());
	}

	//	@Override
	public long longValue() {
		return new Long(this.toString());
	}

	// Hash Function

	public int hashCode() {
		return internalBigDecimal.hashCode();
	}

	// Format Converters

	public String toString() {
		return internalBigDecimal.toString();
	}

	public BigInteger toBigInteger() {
		InternalBigDecimal i = internalBigDecimal.setScale(0, ROUND_DOWN);
		return new BigInteger(i.toString());
	}

	public InternalBigDecimal getInternalBigDecimal() {
		return internalBigDecimal;
	}

	// Private "Helper" Methods

	// public byte byteValue() {
	// throw new IllegalArgumentException("byteValue not implemented");
	// }
	// public short shortValue() {
	// throw new IllegalArgumentException("shortValue not implemented");
	// }
}

