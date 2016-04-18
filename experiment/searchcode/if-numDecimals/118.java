package fi.utu.ville.standardutils;

import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import com.vaadin.data.Property.ReadOnlyException;

import fi.utu.ville.standardutils.ui.NumericValueProvider;

// The value of this class is immutable.
@SuppressWarnings("unused")
public class PreciseDecimal extends Number implements NumericValueProvider,
		Comparable<Number>, Serializable {
	
	private static final long serialVersionUID = 5481020391938646615L;
	
	private static final String[] DECIMAL_FORMATS;
	private static final int MAX_DECIMALS = 20;
	public static final PreciseDecimal ZERO = new PreciseDecimal(0L);
	public static final PreciseDecimal ONE = new PreciseDecimal(1L);
	public static final PreciseDecimal MINUS_ONE = new PreciseDecimal(-1L);
	public static final PreciseDecimal MIN_VALUE = new PreciseDecimal(
			Long.MIN_VALUE);
	public static final PreciseDecimal MAX_VALUE = new PreciseDecimal(
			Long.MAX_VALUE);
	
	private final long value;
	private final int decPoint; // offset from the right
	
	public PreciseDecimal(PreciseDecimal toClone) {
		value = toClone.value;
		decPoint = toClone.decPoint;
	}
	
	public PreciseDecimal(long value) {
		this.value = value;
		decPoint = 0;
	}
	
	/**
	 * Very imprecise way of initializing PreciseDecimal
	 * 
	 * @param value
	 *            initial value
	 */
	public PreciseDecimal(double value) {
		this(value + "");
	}
	
	/**
	 * Slightly more precise way of initializing PreciseDecimal than passing only double value
	 * 
	 * @param value
	 *            initial value
	 * @param numDecimals
	 *            the number of decimals used
	 */
	public PreciseDecimal(double value, int numDecimals) {
		this(getDecimalFormatter(numDecimals).format(value));
	}
	
	public PreciseDecimal(double value, int numDecimals, boolean optimizePresentation) {
		this(getDecimalFormatter(numDecimals).format(value), optimizePresentation);
	}
	
	public PreciseDecimal(long value, int decimalPoint) {
		long[] optimized = optimizePresentation(value, decimalPoint);
		this.value = optimized[0];
		decPoint = (int) optimized[1];
	}
	
	public PreciseDecimal(String str) {
		this(str, true);
	}
	
	public PreciseDecimal(String str, boolean optimizePresentation) {
		long[] parts = parseDecimalFromString(str);
		if (optimizePresentation) {
			parts = optimizePresentation(parts[0], (int) parts[1]);
		}
		value = parts[0];
		decPoint = (int) parts[1];
	}
	
	/**
	 * Creates a PreciseDecimal from an int-array. <br>
	 * If the array has 3 indexes, the fraction is considered mixed for and index 0 stands for the units, index 1 for the numerator and index 2 for the
	 * denominator.<br>
	 * If the array has 2 indexes, the fraction is considered a pure fraction and index 0 stands for the numerator and index 1 for the denominator.<br>
	 * Notice! This method modifies the given array by transforming it to the pure form as a sideffect.
	 * 
	 * @param array
	 *            the int array
	 * @param maxDecimals
	 *            how many decimals can be displayed
	 * @return a PreciseDecimal with the same value as the given fraction
	 */
	public static PreciseDecimal createFromFractionArray(int[] array,
			int maxDecimals) {
		int numIndex = 0;
		if (array.length == 3) {
			array[1] = array[1] + array[0] * array[2];
			numIndex = 1;
		}
		return new PreciseDecimal(array[numIndex]
				/ (double) array[numIndex + 1], maxDecimals);
	}
	
	public long getIntegerPart() {
		return (long) (value / Math.pow(10, decPoint));
	}
	
	/**
	 * Get the value of the decimal part Warning: type long can't handle leading zeros
	 * 
	 * @return the absolute numeric value of the decimal part
	 */
	public long getDecimalPartValue() {
		return Math.abs(value
				- (long) (getIntegerPart() * Math.pow(10, decPoint)));
	}
	
	/**
	 * Get the value of the decimal part. E.g. 0,032. Returns 0.0 (unlocalized, with dot) if there are no decimals in this PreciseDecimal.
	 * 
	 * @return The decimal part of this PreciseDecimal
	 */
	public String getDecimalPart() {
		if (getNumDecimals() == 0) {
			return "0.0";
		}
		return new PreciseDecimal(getDouble() % 1).toString();
	}
	
	public int getNumDecimals() {
		return decPoint;
	}
	
	public double toDouble() {
		return value / Math.pow(10, decPoint);
	}
	
	public int toInteger() {
		return (int) getIntegerPart();
	}
	
	/**
	 * Returns this decimal as a long-array
	 * 
	 * @deprecated This function is provided to use with old Ville code which uses arrays extensively
	 */
	@Deprecated
	public long[] toFractionWithUnits() {
		long[] fraction = new long[3];
		fraction[0] = getIntegerPart();
		fraction[1] = getDecimalPartValue();
		fraction[2] = (long) Math.pow(10, getNumDecimals());
		return fraction;
	}
	
	/**
	 * @return this decimal as an int-array
	 * @deprecated This function is provided to use with old Ville code which uses arrays extensively
	 */
	@Deprecated
	public int[] toFractionWithUnitsInt() {
		long[] longFrac = toFractionWithUnits();
		return convertArray(longFrac);
	}
	
	/**
	 * Parse a decimal from a string
	 * 
	 * @param str
	 *            the numeric value
	 * @return A decimal number as an int-array where [0] = value, [1] = decimal point from the right.
	 */
	public static long[] parseDecimalFromString(String str) {
		long value = 0;
		long decPoint = 0;
		if (str.length() == 0
				|| (str.length() == 1 && Character.DASH_PUNCTUATION == Character
						.getType(str.charAt(0)))) {
			
		}
		boolean decSeparator = false;
		boolean negative = false;
		int numDigits = 0;
		for (int i = 0; i < str.length() && numDigits < 18; i++) {
			char c = str.charAt(i);
			if (Character.isWhitespace(c)) {
				continue;
			}
			if (Character.DASH_PUNCTUATION == Character.getType(c)) {
				negative = true;
				if (value != 0) {
					throw new NumberFormatException(
							"Dash symbol can only appear at the beginning of string, input: "
									+ str);
				}
				continue;
			}
			if (c == '.' || c == ',') {
				if (decSeparator) {
					throw new NumberFormatException("The input string \"" + str
							+ "\" has two decimal separators");
				}
				decSeparator = true;
				continue;
			}
			if (c == 'E' || c == 'e') { // trying to parse exponential notation
				try {
					int exp = Integer.parseInt(str.substring(i + 1));
					if (exp <= 0) {
						decPoint += -exp;
					} else {
						value *= (long) Math.pow(10, exp);
					}
				} catch (Exception ex) {
					throw new NumberFormatException("Error while parsing exponent in input string: " + str);
				}
				break;
			}
			if ('0' > c || '9' < c) {
				throw new NumberFormatException("Invalid character \'" + c
						+ "\' in string \"" + str + "\"");
			}
			int digit = c - '0';
			value *= 10;
			value += digit;
			numDigits++;
			if (decSeparator) {
				decPoint++;
			}
		}
		if (negative) {
			value *= -1;
		}
		return new long[] {
				value, decPoint };
	}
	
	public static double parseDoubleFromString(String str, int allowedDecimals) {
		long[] parts = parseDecimalFromString(str);
		for (long i = parts[1]; i > allowedDecimals; i--) {
			parts[0] /= 10;
			parts[1]--;
		}
		return parts[0] / Math.pow(10, parts[1]);
	}
	
	public static double parseDoubleFromString(String str) {
		long[] parts = parseDecimalFromString(str);
		return parts[0] / Math.pow(10, parts[1]);
	}
	
	// Operators
	
	public static PreciseDecimal add(PreciseDecimal a, PreciseDecimal b) {
		long value = 0;
		if (a.decPoint > b.decPoint) { // ensure a.decPoint <= b.decPoint
			PreciseDecimal t = a;
			a = b;
			b = t;
		}
		value = (long) (a.value * Math.pow(10, b.decPoint - a.decPoint))
				+ b.value;
		int decPoint = b.decPoint;
		return new PreciseDecimal(value, decPoint);
	}
	
	public static PreciseDecimal subtract(PreciseDecimal a, PreciseDecimal b) {
		return add(a, b.getInverse());
	}
	
	public static PreciseDecimal multiply(PreciseDecimal a, PreciseDecimal b) {
		long value = a.value * b.value;
		int decPoint = a.getNumDecimals() + b.getNumDecimals();
		return new PreciseDecimal(value, decPoint);
	}
	
	/**
	 * Divides two PreciseDecimals and returns the result.
	 * 
	 * @param a
	 *            The dividend
	 * @param b
	 *            The divisor
	 * @return The quotient
	 * @throws ArithmeticException
	 *             on division by zero.
	 */
	public static PreciseDecimal divide(PreciseDecimal a, PreciseDecimal b) {
		long value = a.value / b.value;
		long remainder = a.value % b.value;
		int decPoint = a.decPoint - b.decPoint;
		while (remainder != 0 && Math.log10(value) < 18) {
			decPoint += 1;
			remainder *= 10;
			value *= 10;
			value += remainder / b.value;
			remainder = remainder % b.value;
		}
		return new PreciseDecimal(value, decPoint);
	}
	
	public static PreciseDecimal divide(long a, long b) {
		return PreciseDecimal.divide(new PreciseDecimal(a), new PreciseDecimal(b));
	}
	
	/**
	 * Get the absolute value of this PreciseDecimal.
	 * 
	 * @return a new PD that contains the absolute value of this PD.
	 */
	public PreciseDecimal abs() {
		return new PreciseDecimal(Math.abs(value), decPoint);
	}
	
	/**
	 * Get a random decimal between the given values.
	 * 
	 * @param minValue
	 *            the minimum value
	 * @param maxValue
	 *            the maximum value
	 * @param decimals
	 *            the number of decimals in the generated decimal
	 * @return a random decimal
	 */
	public static PreciseDecimal getRandomDecimal(Number minValue, Number maxValue, int decimals) {
		double random = RandomProvider.getRandom().nextDouble() * (maxValue.doubleValue() - minValue.doubleValue()) + minValue.doubleValue();
		return new PreciseDecimal(random, decimals);
	}
	
	public PreciseDecimal getInverse() {
		return new PreciseDecimal(-value, decPoint);
	}
	
	@Override
	public String toString() {
		return getDecimalFormatter().format(toDouble());
	}
	
	public String toString(int numDecimals) {
		return getDecimalFormatter(numDecimals, null).format(
				toDouble());
	}
	
	public String toString(int numDecimals, char decimalSeparator) {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator(decimalSeparator);
		return getDecimalFormatter(numDecimals, symbols).format(toDouble());
	}
	
	public DecimalFormat getDecimalFormatter() {
		int numDecimals = getNumDecimals();
		return getDecimalFormatter(numDecimals, null);
	}
	
	public static DecimalFormat getDecimalFormatter(int numDecimals) {
		return getDecimalFormatter(numDecimals, null);
	}
	
	public static DecimalFormat getDecimalFormatter(int numDecimals,
			DecimalFormatSymbols formatSymbols) {
		if (numDecimals < 0) {
			return new DecimalFormat("0");
		}
		
		String format = "0";
		if (numDecimals > 0) {
			if (numDecimals >= MAX_DECIMALS) {
				format += ".##E00";
			} else {
				format += "." + DECIMAL_FORMATS[numDecimals];
			}
		}
		DecimalFormat df;
		if (formatSymbols != null) {
			df = new DecimalFormat(format, formatSymbols);
		} else {
			df = new DecimalFormat(format);
		}
		df.setRoundingMode(RoundingMode.HALF_UP);
		return df;
	}
	
	public boolean isApproximately(double other) {
		// TODO: Check this implementation
		DecimalFormat df = getDecimalFormatter();
		String curValStr = df.format(toDouble());
		String otherValStr = df.format(other);
		return curValStr.equals(otherValStr);
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other instanceof Double) {
			return new PreciseDecimal((Double) other).equals(this);
		}
		if (other instanceof Integer) {
			return (decPoint == 0 && ((Integer) other).equals((int) getIntegerPart()));
		}
		if (other instanceof String) {
			try {
				return equals(new PreciseDecimal((String) other));
			} catch (NumberFormatException ex) {
				return false;
			}
		}
		if (other instanceof PreciseDecimal) {
			PreciseDecimal dec = (PreciseDecimal) other;
			return (dec.value == value && dec.decPoint == decPoint);
		}
		return false;
	}
	
	@Deprecated
	public void setGroupingSeparator(char value) {
		
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + decPoint;
		result = prime * result + (int) (value ^ (value >>> 32));
		return result;
	}
	
	private static long[] optimizePresentation(long value, int decPoint) {
		if (decPoint > 5) {
			//double precision stuff; no student given answer should ever contain 6 decimals of 9s or 0s
			if ((value + "").matches(".*9999.")) {
				value /= 10;
				if (value < 0) {
					value--;
				} else {
					value++;
				}
				decPoint--;
			} else if ((value + "").matches(".*0000.")) {
				value /= 10;
				decPoint--;
			}
		}
		while (decPoint > 0) {
			if (value % 10 == 0) {
				decPoint--;
				value /= 10;
			} else {
				break;
			}
		}
		return new long[] {
				value, decPoint };
	}
	
	private PreciseDecimal copy() {
		return new PreciseDecimal(value, decPoint);
	}
	
	private static int[] convertArray(long[] array) {
		int[] result = new int[array.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = (int) array[i];
		}
		return result;
	}
	
	static {
		DECIMAL_FORMATS = new String[MAX_DECIMALS];
		String f = "";
		for (int i = 0; i < MAX_DECIMALS; i++) {
			DECIMAL_FORMATS[i] = f;
			f += "0";
		}
	}
	
	@Override
	public PreciseDecimal getPreciseDecimal() {
		return new PreciseDecimal(this);
	}
	
	@Override
	public double getDouble() {
		return toDouble();
	}
	
	@Override
	public void setValue(NumericValueProvider preciseDecimal)
			throws ReadOnlyException {
		throw new ReadOnlyException("PreciseDecimal is immutable");
	}
	
	@Override
	public boolean isMutable() {
		return false;
	}
	
	@Override
	public int compareTo(Number other) {
		return (int) Math.signum(doubleValue() - other.doubleValue());
		// silly conversion ensures long overflow doesn't occur
	}
	
	@Override
	public boolean canChangeValueTo(double newValue) {
		return true;
	}
	
	// Methods from Number
	@Override
	public double doubleValue() {
		return toDouble();
	}
	
	@Override
	public float floatValue() {
		return (float) toDouble();
	}
	
	@Override
	public int intValue() {
		return (int) getIntegerPart();
	}
	
	@Override
	public long longValue() {
		return getIntegerPart();
	}
	
	/**
	 * Returns the number of significand digits in this preciseDecimal. i.e. 1200 will return 4, whereas 0,004 will return 1 and 0,0400 will return 3
	 */
	public int getNumberOfSignificandDigits() {
		return (Math.abs(value) + "").length();
	}
	
	/**
	 * Returns this PreciseDecimal rounded to the given number of significand digits as a String. The PD-object itself remains unmodified
	 * 
	 * @param significandDigits
	 *            the number of significand figures
	 * @return A string representation of this preciseDecimal rounded to the given number of significand figures. i.e 2 figures: 3456 ~ 3500, 3 figures: 0,0034
	 *         ~ 0,00340
	 */
	public String toSignificandDigits(int significandDigits) {
		if (significandDigits <= 0 || value == 0) {
			return "0";
		}
		
		long tempvalue = value;
		int tempDecPoint = decPoint;
		
		while ((Math.abs(tempvalue) + "").length() > significandDigits) {
			
			if ((Math.abs(tempvalue) + "").length() == significandDigits + 1) {
				tempvalue += 5;
			}
			
			tempDecPoint--;
			tempvalue /= 10;
		}
		
		while ((Math.abs(tempvalue) + "").length() < significandDigits) {
			tempDecPoint++;
			tempvalue *= 10;
		}
		
		return new PreciseDecimal(tempvalue, tempDecPoint).toString(tempDecPoint);
	}
	
	/**
	 * Find the repeating part of the decimal. e.g. 1.6161616 will return 61 whereas 3.65457457 will return 457. If no repeating part is found, an empty string
	 * is returned.
	 * 
	 * @return The repeating part of the decimal portion of the given PD
	 */
	public String findRepeatingDecimal() {
		String number = getDecimalPartValue() + "";
		int originalLength = number.length();
		int nextIndex = 0;
		
		while (number.startsWith("0")) {
			number = number.substring(1);
		}
		
		//check we have at least two same digits; it's impossible to find any repeats with different digits
		int[] numbers = new int[10];
		
		for (int i = 0; i < number.length(); i++) {
			try {
				int digit = Integer.parseInt(number.charAt(i) + "");
				numbers[digit]++;
			} catch (NumberFormatException e) {
				//NOOP
			}
		}
		
		//set the first digit to be one that we found only one of
		for (int i = 0; i < numbers.length; i++) {
			char c = (i + "").charAt(0);
			int index = number.indexOf(c);
			if (numbers[i] == 1 && index > 0) {
				number = number.substring(index);
			}
		}
		
		if (number.length() <= originalLength / 2) {
			return "";
		}
		
		//cannot go on infinitely; search for nestIndex never starts from index 0
		while (number.length() > 0 && nextIndex <= 0) {
			nextIndex = number.indexOf(number.charAt(0), 1);
			if (nextIndex < 0 || number.length() <= nextIndex * 2) {
				number = number.substring(1);
			} else {
				for (int i = 0; i < nextIndex && i + 2 * nextIndex < number.length(); i++) {
					if (number.charAt(i) != number.charAt(i + nextIndex) || number.charAt(i) != number.charAt(i + 2 * nextIndex)) {
						number = number.substring(1);
						nextIndex = -1;
						break;
					}
				}
			}
		}
		if (nextIndex <= 0) {
			return "";
		} else {
			return number.substring(0, nextIndex);
		}
	}
	
	public PreciseDecimal roundToDecimals(int decimals) {
		return new PreciseDecimal(toString(decimals));
	}
}

