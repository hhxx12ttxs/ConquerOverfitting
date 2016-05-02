package lipstone.joshua.parser.types;

import java.math.BigDecimal;
import java.math.MathContext;

import lipstone.joshua.parser.exceptions.UndefinedResultException;

/**
 * A wrapper class for java.math.BigDecimal it adds more direct comparison methods for >=, <=, and !=
 * 
 * @author joshualipstone
 */
public final class BigDec implements Comparable<BigDec> {
	public static final BigDec ZERO = new BigDec(BigDecimal.ZERO);
	public static final BigDec ONE = new BigDec(BigDecimal.ONE);
	public static final BigDec TEN = new BigDec(BigDecimal.TEN);
	public static final BigDec MINUSONE = new BigDec(BigDecimal.ZERO.subtract(BigDecimal.ONE));
	public static final BigDec INFINITY = new BigDec(new Integer(1));
	public static final BigDec MINUS_INFINITY = new BigDec(new Integer(1));
	private final BigDecimal num;
	private int infinity = 0;
	
	private BigDec(Integer infinity) {
		num = BigDecimal.ZERO;
		this.infinity = infinity.signum(infinity);
	}
	
	public BigDec(BigDecimal val) {
		num = new BigDecimal(val.toString());
	}
	
	public BigDec(BigDec val) {
		infinity = val.infinity;
		num = new BigDecimal(val.num.toString());
	}
	
	public BigDec(BigInt val) {
		infinity = val.getInfinity();
		num = new BigDecimal(val.getBI());
	}
	
	public BigDec(BigInt unscaledVal, int scale) {
		infinity = unscaledVal.getInfinity();
		num = new BigDecimal(unscaledVal.getBI(), scale);
	}
	
	public BigDec(BigInt unscaledVal, int scale, MathContext mc) {
		infinity = unscaledVal.getInfinity();
		num = new BigDecimal(unscaledVal.getBI(), scale, mc);
	}
	
	public BigDec(BigInt val, MathContext mc) {
		infinity = val.getInfinity();
		num = new BigDecimal(val.getBI(), mc);
	}
	
	public BigDec(double val) {
		if (val == Double.POSITIVE_INFINITY)
			infinity = 1;
		if (val == Double.NEGATIVE_INFINITY)
			infinity = -1;
		num = new BigDecimal(val);
	}
	
	public BigDec(double val, MathContext mc) {
		if (val == Double.POSITIVE_INFINITY)
			infinity = 1;
		else if (val == Double.NEGATIVE_INFINITY)
			infinity = -1;
		num = new BigDecimal(val, mc);
	}
	
	public BigDec(int val) {
		if (new Integer(val).doubleValue() == Double.POSITIVE_INFINITY)
			infinity = 1;
		else if (new Integer(val).doubleValue() == Double.NEGATIVE_INFINITY)
			infinity = -1;
		num = new BigDecimal(val);
	}
	
	public BigDec(int val, MathContext mc) {
		if (new Integer(val).doubleValue() == Double.POSITIVE_INFINITY)
			infinity = 1;
		else if (new Integer(val).doubleValue() == Double.NEGATIVE_INFINITY)
			infinity = -1;
		num = new BigDecimal(val, mc);
	}
	
	public BigDec(long val) {
		if (new Long(val).doubleValue() == Double.POSITIVE_INFINITY)
			infinity = 1;
		else if (new Long(val).doubleValue() == Double.NEGATIVE_INFINITY)
			infinity = -1;
		num = new BigDecimal(val);
	}
	
	public BigDec(long val, MathContext mc) {
		if (new Long(val).doubleValue() == Double.POSITIVE_INFINITY)
			infinity = 1;
		else if (new Long(val).doubleValue() == Double.NEGATIVE_INFINITY)
			infinity = -1;
		num = new BigDecimal(val, mc);
	}
	
	public BigDec(String val) {
		if (val.equalsIgnoreCase("Infinity")) {
			infinity = 1;
			num = BigDecimal.ZERO;
		}
		else if (val.equalsIgnoreCase("-Infinity")) {
			infinity = -1;
			num = BigDecimal.ZERO;
		}
		else
			num = new BigDecimal(val.trim());
	}
	
	public BigDec(String val, MathContext mc) {
		if (val.equalsIgnoreCase("Infinity")) {
			infinity = 1;
			num = BigDecimal.ZERO;
		}
		else if (val.equalsIgnoreCase("-Infinity")) {
			infinity = -1;
			num = BigDecimal.ZERO;
		}
		else
			num = new BigDecimal(val.trim(), mc);
	}
	
	public BigDecimal getBD() {
		return num;
	}
	
	public BigInt toBigInt() {
		if (infinity == 1)
			return new BigInt("Infinity");
		else if (infinity == -1)
			return new BigInt("-Infinity");
		return new BigInt(num.toBigInteger());
	}
	
	/**
	 * Converts this number into an int. It drops any decimal component without rounding.
	 * 
	 * @return this number as an int
	 */
	public int intValue() {
		return num.intValue();
	}
	
	/**
	 * Converts this number into a double
	 * 
	 * @return a double representation of this number.
	 */
	public double doubleValue() {
		if (infinity == 1)
			return Double.POSITIVE_INFINITY;
		if (infinity == -1)
			return Double.NEGATIVE_INFINITY;
		return num.doubleValue();
	}
	
	/**
	 * Gets this objects infinity information
	 * 
	 * @return 1 if this is positive infinity, 0 if it is a finite number, or -1 if it is negative infinity
	 */
	public int getInfinity() {
		return infinity;
	}
	
	public BigDec add(BigDec val) throws UndefinedResultException {
		if ((infinity != 0 ^ val.infinity != 0) || (infinity != 0 && infinity == val.infinity))
			return new BigDec(new Integer(infinity + val.infinity));
		else if (infinity != val.infinity)
			throw new UndefinedResultException(null);
		return new BigDec(num.add(val.num));
	}
	
	public BigDec add(BigInt val) throws UndefinedResultException {
		return add(new BigDec(val));
	}
	
	public BigDec subtract(BigDec val) throws UndefinedResultException {
		if ((infinity != 0 ^ val.infinity != 0) || infinity != val.infinity)
			return new BigDec(new Integer(infinity - val.infinity));
		else if (infinity != 0 && infinity == val.infinity)
			throw new UndefinedResultException(null);
		return new BigDec(num.subtract(val.num));
	}
	
	public BigDec subtract(BigInt val) throws UndefinedResultException {
		return subtract(new BigDec(val));
	}
	
	public BigDec multiply(BigDec val) throws UndefinedResultException {
		if (infinity != 0 && val.infinity != 0)
			return new BigDec(new Integer(infinity * val.infinity));
		if (infinity != 0) {
			if (val.eq(BigDec.ZERO))
				throw new UndefinedResultException(null);
			return new BigDec(new Integer(infinity));
		}
		if (val.infinity != 0) {
			if (this.eq(BigDec.ZERO))
				throw new UndefinedResultException(null);
			return new BigDec(new Integer(val.infinity));
		}
		return new BigDec(num.multiply(val.num));
	}
	
	public BigDec multiply(BigInt val) throws UndefinedResultException {
		return multiply(new BigDec(val));
	}
	
	public BigDec divide(BigDec val) throws UndefinedResultException {
		if (infinity != 0 && val.infinity != 0)
			throw new UndefinedResultException(null);
		else if (infinity != 0)
			return new BigDec(new Integer(infinity));
		else if (val.infinity != 0)
			return BigDec.ZERO;
		else if (val.eq(BigDec.ZERO) && this.eq(BigDec.ZERO))
			throw new UndefinedResultException(null);
		else if (val.eq(BigDec.ZERO))
			return new BigDec(new Integer(signum()));
		return new BigDec(num.divide(val.num));
	}
	
	public BigDec divide(BigInt val) throws UndefinedResultException {
		return divide(new BigDec(val));
	}
	
	public BigDec mod(BigDec val) throws UndefinedResultException {
		if (val.eq(BigDec.ZERO))
			throw new UndefinedResultException(null);
		return new BigDec(num.remainder(val.num).abs());
	}
	
	public BigDec mod(BigInt val) throws UndefinedResultException {
		return mod(new BigDec(val));
	}
	
	public BigDec remainder(BigDec val) throws UndefinedResultException {
		if (val.eq(BigDec.ZERO))
			throw new UndefinedResultException(null);
		return new BigDec(num.remainder(val.num));
	}
	
	public BigDec remainder(BigInt val) throws UndefinedResultException {
		return remainder(new BigDec(val));
	}
	
	/**
	 * Equivalent of Math.pow(base, exp) where base is this object
	 * 
	 * @param exp
	 *            the exponent as a BigDec, unlike the BigDecimal.pow(BigDecimal exp) this can be a decimal value
	 * @param context
	 *            a MathContext object containing the accuracy for this exponent calculation
	 * @return a BigDec object with a value equal to this^exp to the accuracy defined by context
	 * @throws UndefinedResultException
	 *             when both this and exp are infinitives
	 */
	public BigDec pow(BigDec exp, MathContext context) throws UndefinedResultException {
		if (infinity != 0 && exp.infinity != 0)
			throw new UndefinedResultException(null);
		else if (infinity != 0)
			return new BigDec(new Integer(infinity));
		else if (exp.infinity > 0)
			return new BigDec(new Integer(signum()));
		else if (exp.infinity < 0)
			return BigDec.ZERO;
		BigDec result = BigDec.ZERO;
		double multiplier = 1, n2 = exp.doubleValue();
		if (this.num.signum() < 0 && (int) (n2) != n2 && exp.signum() < 1 && ((int) 1 / n2) % 2 != 0) {
			multiplier = -1;
			try {
				result = new BigDec(multiplier * Math.pow(new BigDec(multiplier).multiply(this).doubleValue(), n2));
			}
			catch (NumberFormatException | UndefinedResultException e) {
				result = BigDec.ZERO;
			}
		}
		else {
			try {
				result = new BigDec(Math.pow(this.num.doubleValue(), n2));
			}
			catch (NumberFormatException e) {
				result = BigDec.ZERO;
			}
		}
		if (result.eq(BigDec.ZERO) && this.neq(BigDec.ZERO)) {
			//###############################################################################################################################################//
			//I got this code for exponents from Gene Marin via http://stackoverflow.com/questions/3579779/how-to-do-a-fractional-power-on-bigdecimal-in-java//
			//###############################################################################################################################################//
			int signOf2 = exp.signum();
			
			MathContext mc;
			mc = new MathContext(this.getBD().precision() * exp.getBD().precision());
			try {
				// Perform X^(A+B)=X^A*X^B (B = remainder)
				double dn1 = this.num.doubleValue();
				exp = exp.multiply(new BigDec(signOf2)); // exp is now positive
				BigDec remainderOf2 = exp.remainder(BigDec.ONE);
				BigDec n2IntPart = exp.subtract(remainderOf2);
				// Calculate big part of the power using context -
				// bigger range and performance but lower accuracy
				BigDec intPow = new BigDec(this.num.pow(n2IntPart.getBD().intValueExact(), mc));
				BigDec doublePow = new BigDec(Math.pow(dn1, remainderOf2.doubleValue()));
				result = intPow.multiply(doublePow);
			}
			catch (Exception e) {}
			// Fix negative power
			if (signOf2 == -1)
				result = new BigDec(BigDec.ONE.getBD().divide(result.getBD(), mc));
		}
		if (exp.infinity == 1)
			result.infinity = 1;
		if (exp.infinity == -1)
			result = BigDec.ZERO;
		return result;
	}
	
	/**
	 * Equivalent of Math.pow(base, exp) where base is this object
	 * 
	 * @param exp
	 *            the exponent as a BigDec, unlike the BigDecimal.pow(BigDecimal exp) this can be a decimal value
	 * @return a BigDec object with a value equal to this^exp to the accuracy defined by MathContext.DECIMAL128
	 * @throws UndefinedResultException
	 *             when both this and exp are infinitives
	 */
	public BigDec pow(BigDec exp) throws UndefinedResultException {
		return pow(exp, MathContext.DECIMAL128);
	}
	
	/**
	 * Equivalent of Math.pow(base, exp) where base is this object
	 * 
	 * @param exp
	 *            the exponent as a BigInt
	 * @return a BigDec object with a value equal to this^exp to the accuracy defined by MathContext.DECIMAL128
	 * @throws UndefinedResultException
	 *             when both this and exp are infinitives
	 */
	public BigDec pow(BigInt val) throws UndefinedResultException {
		return pow(new BigDec(val));
	}
	
	/**
	 * equal to
	 * 
	 * @param val
	 *            the value to be compared to
	 * @return returns true if BigDecimal.compareTo(BigDecimal val) = 0, false otherwise
	 */
	public boolean eq(BigDec val) {
		if (infinity != 0 && infinity == val.infinity)
			return true;
		if (num.subtract(val.num).compareTo(num.ZERO) == 0)
			return true;
		return false;
	}
	
	/**
	 * not equal to
	 * 
	 * @param val
	 *            the value to be compared to
	 * @return returns the inverse of eq(val).
	 */
	public boolean neq(BigDec val) {
		return !eq(val);
	}
	
	/**
	 * greater than
	 * 
	 * @param val
	 *            the value to be compared to
	 * @return returns true if BigDecimal.compareTo(BigDecimal val) = 1, false otherwise
	 */
	public boolean gt(BigDec val) {
		if (infinity > val.infinity)
			return true;
		else if (infinity != 0 || val.infinity != 0)
			return false;
		if (val.infinity == 0 && num.subtract(val.num).compareTo(num.ZERO) == 1)
			return true;
		return false;
	}
	
	/**
	 * less than
	 * 
	 * @param val
	 *            the value to be compared to
	 * @return returns true if BigDecimal.compareTo(BigDecimal val) = -1, false otherwise
	 */
	public boolean lt(BigDec val) {
		if (infinity < val.infinity)
			return true;
		else if (infinity != 0 || val.infinity != 0)
			return false;
		if (num.subtract(val.num).compareTo(num.ZERO) == -1)
			return true;
		return false;
	}
	
	/**
	 * greater than or equal to
	 * 
	 * @param val
	 *            the value to be compared to
	 * @return returns true if BigDecimal.compareTo(BigDecimal val) = 1 or 0, false otherwise
	 */
	public boolean gteq(BigDec val) {
		return eq(val) || gt(val);
	}
	
	/**
	 * less than or equal to
	 * 
	 * @param val
	 *            the value to be compared to
	 * @return returns true if BigDecimal.compareTo(BigDecimal val) = -1 or 0, false otherwise
	 */
	public boolean lteq(BigDec val) {
		return eq(val) || lt(val);
	}
	
	/**
	 * Returns a BigDec whose value is the absolute value of this BigDec
	 * 
	 * @return the absolute value of this BigDec
	 */
	public BigDec abs() {
		return new BigDec(num.abs());
	}
	
	/**
	 * Functional equivalent of ++, the stored value by one, but doesn't save to this object. So, the use is num = num.pp(),
	 * not num.pp().
	 * 
	 * @return this BigDec with the stored value incremented by 1
	 */
	public BigDec pp() {
		if (infinity != 0)
			return this;
		return new BigDec(num.add(num.ONE));
	}
	
	/**
	 * Functional equivalent of --, decrements the stored value by one, but doesn't save to this object. So, the use is num =
	 * num.mm(), not num.mm().
	 * 
	 * @return this BigDec with the stored value decremented by 1
	 */
	public BigDec mm() {
		if (infinity != 0)
			return this;
		return new BigDec(num.subtract(num.ONE));
	}
	
	/**
	 * Equivalent to BigDecimal.signum()
	 * 
	 * @return the sign of this number, or 0 if this number is zero.
	 */
	public int signum() {
		if (infinity != 0)
			return infinity;
		return num.signum();
	}
	
	/**
	 * Determines if this number is an integer.
	 * 
	 * @return true if this number is 0, has no decimal component, or has no non-zero decimal component.
	 */
	public boolean isInt() {
		return this.num.signum() == 0 || this.num.scale() <= 0 || this.num.stripTrailingZeros().scale() <= 0;
	}
	
	public String toString() {
		if (infinity == 1)
			return new Double(Double.POSITIVE_INFINITY).toString();
		if (infinity == -1)
			return new Double(Double.NEGATIVE_INFINITY).toString();
		return num.stripTrailingZeros().toPlainString();
	}
	
	public int compareTo(BigDec o) {
		if (this.gt(o))
			return 1;
		if (this.lt(o))
			return -1;
		return 0;
	}
}
