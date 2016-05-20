package uk.ac.lkl.common.util.value;

import java.math.BigInteger;

/**
 * This class represents an expression that is a value.
 * 
 * A value is, up to now, a rational number, with units.
 * 
 * Stored in canonical form. Includes any simplification of the fraction. Also
 * if the fraction is negative, this is indicated always by the numerator.
 * 
 * Question: perhaps a Value with Units should be stored elsewhere. We may want
 * integers or doubles with units, etc.
 * 
 */
public class RationalValue extends NumericValue<RationalValue> implements
	Comparable<RationalValue> {

    public static final RationalValue ZERO = new RationalValue(0);

    public static final RationalValue ONE = new RationalValue(1);

    /**
     * The numerator of this value.
     */
    private BigInteger numerator = BigInteger.ONE;

    /**
     * The denominator of this value.
     * 
     * If it is set to zero, an event happens.
     */
    private BigInteger denominator = BigInteger.ONE;

    /**
     * The units of this value.
     */
    private Unit units = new Unit();

    /**
     * Create a new instance representing the specified integer.
     * 
     * Internally, this corresponds to a rational number with numerator=
     * <code>integer</code> and denominator=1.
     * 
     * @param integer
     *            the integer value of this instance
     * 
     */
    public RationalValue(long integer) {
	this(BigInteger.valueOf(integer));
    }

    public RationalValue(BigInteger integer) {
	super();
	this.numerator = integer;
    }

    // copy ctor
    public RationalValue(RationalValue value) {
	this.numerator = value.numerator;
	this.denominator = value.denominator;
	this.units = value.units;
    }

    // convenience
    // edited since GWT can't compile parser and we're not using this
    // functionality anyway
    public RationalValue(double value) {
	// this(DecimalValueStringParser.parseValue(Double.toString(value)));
	// this throws away the fractional part and won't work for values
	// greater than Long.MAX_VALUE
	this((long) value);
    }

    public RationalValue(BigInteger integer,
	    BigInteger fractionalNumerator,
	    BigInteger fractionalDenominator) {
	this(fractionalDenominator.multiply(integer).add(fractionalNumerator),
		fractionalDenominator);
    }

    public RationalValue(long integer, long fractionalNumerator,
	    long fractionalDenominator) {
	this(BigInteger.valueOf(integer), BigInteger
		.valueOf(fractionalNumerator), BigInteger
		.valueOf(fractionalDenominator));
    }

    /**
     * Create a new fractional scalar.
     * 
     * @param numerator
     *            the numerator of this instance
     * @param denominator
     *            the denominator of this instance
     * 
     */
    public RationalValue(long numerator, long denominator) {
	this(BigInteger.valueOf(numerator), BigInteger
		.valueOf(denominator));
    }

    public RationalValue(BigInteger numerator, BigInteger denominator) {
	super();
	int denominatorSign = denominator.compareTo(BigInteger.ZERO);
	if (denominatorSign < 0) {
	    // ensure negative is always in numerator not denominator
	    this.numerator = numerator.negate();
	    this.denominator = denominator.negate();
	} else {
	    this.numerator = numerator;
	    this.denominator = denominator;
	}
	;
	reduceToMinimalForm();
    }

    // assumes immutable
    public RationalValue createCopy() {
	return this;
    }

    public Class<? extends RationalValue> getValueClass() {
	return this.getClass();
    }

    private void reduceToMinimalForm() {
	int numeratorSign = numerator.compareTo(BigInteger.ZERO);
	if (numeratorSign == 0) {
	    // canonicalise zero
	    this.numerator = BigInteger.ZERO;
	    this.denominator = BigInteger.ONE;
	} else {
	    BigInteger gcd = denominator.gcd(numerator);
	    this.denominator = denominator.divide(gcd);
	    this.numerator = numerator.divide(gcd);
	}
    }

    /**
     * Create a new fractional scalar the units of this instance
     * 
     * @param numerator
     *            the numerator of this instance
     * @param denominator
     *            the denominator of this instance
     * @param units
     *            the units of this instance
     * 
     */

    public RationalValue(long numerator, long denominator, Unit units) {
	this(BigInteger.valueOf(numerator), BigInteger
		.valueOf(denominator), units);
    }

    public RationalValue(BigInteger numerator,
	    BigInteger denominator, Unit units) {
	this(numerator, denominator);
	if (numerator.compareTo(BigInteger.ZERO) != 0)
	    // zero is always a scalar
	    this.units = units;
    }

    public RationalValue getZero() {
	return ZERO;
    }

    public byte byteValue() {
	// abstract class Number requires this -- pretty useless
	Double d = doubleValue();
	return d.byteValue();
    }

    @Override
    public double doubleValue() {
	if (!isValid())
	    return Double.NaN;

	BigInteger quotientAndRemainder[] =
		numerator.divideAndRemainder(denominator);
	try {
	    double quotient = quotientAndRemainder[0].doubleValue();
	    if (quotient == Double.NEGATIVE_INFINITY) {
		return Double.MIN_VALUE;
	    } else if (quotient == Double.POSITIVE_INFINITY) {
		return Double.MAX_VALUE;
	    }
	    try {
		double result = quotient;
		double denominatorAsDoubleValue = denominator.doubleValue();
		if (denominatorAsDoubleValue == Double.POSITIVE_INFINITY) {
		    // denominator is greater than Double.MAX_VALUE
		    // so compute denominator / remainder as a BigInteger
		    // and convert that to double and take reciprocal
		    result +=
			    1.0 / denominator.divide(quotientAndRemainder[1])
				    .doubleValue();
		} else {
		    // quotientAndRemainder[1] is less than denominator
		    // so need to check if it is infinity
		    result +=
			    quotientAndRemainder[1].doubleValue()
				    / denominatorAsDoubleValue;
		}
		return result;
	    } catch (Exception e) {
		double result =
			quotient
				+ 1.0
				/ denominator.divide(quotientAndRemainder[1])
					.doubleValue();
		return result;
	    }
	} catch (Exception e) {
	    if (quotientAndRemainder[0].compareTo(BigInteger.ZERO) < 0) {
		return Double.MIN_VALUE;
	    } else {
		return Double.MAX_VALUE;
	    }
	}
    }

    @Override
    public float floatValue() {
	Double doubleValue = doubleValue();
	return doubleValue.floatValue();
    }

    @Override
    public int intValue() {
	Double doubleValue = doubleValue();
	return doubleValue.intValue();

    }

    @Override
    public long longValue() {
	Double doubleValue = doubleValue();
	return doubleValue.longValue();
    }

    @Override
    public short shortValue() {
	Double doubleValue = doubleValue();
	return doubleValue.shortValue();
    }

    public boolean equals(Object object) {
	if (!(object instanceof RationalValue))
	    return false;

	RationalValue other = (RationalValue) object;
	return this.isEqualTo(other);
    }

    public boolean isEqualTo(RationalValue other) {
	return units.equals(other.getUnits()) && subtract(other).isZero();
    }

    // ignores units. Is this reasonable?
    // I don't think this is very useful due to units
    // See for example LessThanExpression
    public int compareTo(RationalValue other) {
	RationalValue difference = this.subtract(other);
	return difference.isNegative() ? -1 : 1;
    }

    @Override
    public boolean isGreaterThan(RationalValue other) {
	return compareTo(other) > 0;
    }

    @Override
    public boolean isLessThan(RationalValue other) {
	return compareTo(other) < 0;
    }

    @Override
    public boolean isNegative() {
	return numerator.compareTo(BigInteger.ZERO) < 0;
    }

    @Override
    public boolean isPositive() {
	return numerator.compareTo(BigInteger.ZERO) > 0;
    }
    
    public boolean isZero() {
	// Is NaN == zero? Should we check that denominator != 0? (SG
	// 2008-04-28)
	// See Issue 166
	return numerator.compareTo(BigInteger.ZERO) == 0;
    }

    public boolean isInteger() {
	reduceToMinimalForm();
	return denominator.compareTo(BigInteger.ONE) == 0;
    }

    /**
     * Subtract the given value from this instance.
     * 
     * This is equivalent to <code>-other + this</code>.
     * 
     * @param other
     *            the other value
     * 
     * @return <code>other</code> subtracted from this instance
     * 
     */
    public RationalValue subtract(RationalValue other) {
	if (!other.isValidArithmetically()) {
	    return other;
	}
	if (!isValidArithmetically()) {
	    return this;
	}
	return add(other.negate());
    }

    /**
     * Divide this instance by the specified value.
     * 
     * This is equivalent to <code>this * 1/other</code>.
     * 
     * @param other
     *            the value to divide this instance by
     * 
     * @return this instance divide by <code>other</code>
     * 
     */
    @Override
    public RationalValue divide(RationalValue other) {
	if (!other.isValidArithmetically()) {
	    return other;
	}
	if (!isValidArithmetically()) {
	    return this;
	}
	return multiply(other.invert());
    }

    public RationalValue add(RationalValue other) {
	if (!other.isValidArithmetically()) {
	    return other;
	}
	if (!isValidArithmetically()) {
	    return this;
	}
	BigInteger denominator1 = this.getDenominator();
	BigInteger denominator2 = other.getDenominator();
	BigInteger gcd = denominator1.gcd(denominator2);
	// following is the least common multiple of the denominators
	BigInteger newDenominator =
		denominator1.multiply(denominator2).divide(gcd);
	BigInteger thisContributionToNumerator =
		this.getNumerator().multiply(
			newDenominator.divide(denominator1));
	BigInteger otherContributionToNumerator =
		other.getNumerator().multiply(
			newDenominator.divide(denominator2));
	BigInteger newNumerator =
		thisContributionToNumerator.add(otherContributionToNumerator);
	Unit newUnits = new Unit(getUnits()).add(other.getUnits());
	return new RationalValue(newNumerator, newDenominator, newUnits);
    }
    
    public RationalValue multiply(RationalValue other) {
	if (!other.isValidArithmetically()) {
	    return other;
	}
	if (!isValidArithmetically()) {
	    return this;
	}
	BigInteger newDenominator =
		this.getDenominator().multiply(other.getDenominator());
	BigInteger newNumerator =
		this.getNumerator().multiply(other.getNumerator());
	BigInteger gcd = newDenominator.gcd(newNumerator);
	Unit newUnits = new Unit(getUnits()).multiply(other.getUnits());
	return new RationalValue(newNumerator.divide(gcd), newDenominator
		.divide(gcd), newUnits);
    }

    public boolean isValidModulus() {
	return isInteger() && isPositive();
    }

    public RationalValue mod(RationalValue other) {
	// following shouldn't be needed since everything else
	// leaves rationals in minimal form
	reduceToMinimalForm();
	return new RationalValue(numerator.mod(other.getNumerator()));
    }
    
    public RationalValue negate() {
	if (!isValidArithmetically()) {
	    return this;
	}
	// copies units since NegationExpression shouldn't return a new value
	// that shares the same units with the original
	return new RationalValue(numerator.negate(), denominator, new Unit(
		units));
    }
    
    public RationalValue absoluteValue() {
	if (isNegative()) {
	    return negate();
	} else {
	    return this;
	}
    }

    public RationalValue invert() {
	if (!isValidArithmetically()) {
	    return this;
	}
	return new RationalValue(denominator, numerator, units.invert());
    }

    public String toString() {
	String result = numerator.toString();

	if (denominator.compareTo(BigInteger.ONE) != 0
		&& numerator.compareTo(BigInteger.ZERO) != 0)
	    result += "/" + denominator;

	if (!units.isEmpty())
	    result += " " + units.toString();

	return result;
    }

    // edited since GWT can't compile parser and we're not using this
    // functionality
    public static RationalValue parseValue(String valueString) {
	// return ValueStringParser.parseValue(valueString);
	return new RationalValue(Integer.parseInt(valueString));
    }

    public BigInteger getNumerator() {
	return numerator;
    }

    public BigInteger getDenominator() {
	return denominator;
    }

    public Unit getUnits() {
	return units;
    }

    public void setUnits(Unit units) {
	this.units = units;
    }

    public boolean isValid() {
	return isValidArithmetically() && isValidUnits();
    }

    public boolean isValidArithmetically() {
	return denominator.compareTo(BigInteger.ZERO) != 0;
    }

    public boolean isValidUnits() {
	return getUnits().isValid();
    }

    public RationalValue createFromDouble(double d) {
	return new RationalValue(d);
    }

    public RationalValue createFromInt(int i) {
	return new RationalValue(i);
    }

}

