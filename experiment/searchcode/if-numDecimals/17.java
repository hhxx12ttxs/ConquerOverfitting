package fi.utu.ville.standardutils.ui;

import fi.utu.ville.standardutils.PreciseDecimal;
import fi.utu.ville.standardutils.ui.MinMaxFieldValidator.ModifyPreference;

public class DecimalField extends RegexField implements ControlWithNumericValue {
	
	private static final long serialVersionUID = -802077291284418987L;
	
	private static String REGEX_ANY_DIGIT = "[0-9]";
	private static String REGEX_NONZERO_DIGIT = "[1-9]";
	private static String REGEX_DEC_SEPARATOR = "(\\.|,)?";
	private static String REGEX_DASH_PREFIX = "\\-";
	public static String REGEX_DEFAULT_DECIMAL = "\\-?[0-9]*(\\.|,)?[0-9]*";
	
	private MinMaxFieldValidator minMaxValidator = null;
	
	public DecimalField(String caption, Integer numIntegers, Integer numDecimals, FieldParameter... parameters) {
		super(caption, getRegexFromParameters(numIntegers, numDecimals, parameters));
		setMaxLength(18);
	}
	
	public DecimalField(String caption, FieldParameter... parameters) {
		this(caption, null, null, parameters);
	}
	
	public DecimalField(FieldParameter... parameters) {
		this(null, null, null, parameters);
	}
	
	public DecimalField(Integer numDecimals, FieldParameter... parameters) {
		this(null, null, numDecimals, parameters);
	}
	
	// TODO: numIntegers does not work
	public static String getRegexFromParameters(Integer numIntegers, Integer numDecimals, FieldParameter... parameters) {
		if (numIntegers == null && numDecimals == null && parameters.length == 0) {
			return REGEX_DEFAULT_DECIMAL;
		}
		String prefix = "";
		String integers = "";
		String decimals = "";
		boolean signRestriction = false;
		for (FieldParameter param : parameters) {
			switch (param) {
			case NEGATIVE_ONLY:
				prefix = REGEX_DASH_PREFIX;
				signRestriction = true;
				break;
			case NONNEGATIVE_ONLY:
				signRestriction = true;
				break;
			case POSITIVE_ONLY:
				signRestriction = true;
				integers += REGEX_NONZERO_DIGIT;
				if (numIntegers != null) {
					numIntegers -= 1;
				}
				break;
			case NONZERO_FIRST_DIGIT:
				integers += REGEX_NONZERO_DIGIT + "?";
				if (numIntegers != null) {
					numIntegers -= 1;
				}
				break;
			}
			
		}
		if (!signRestriction) {
			prefix = REGEX_DASH_PREFIX + "?";
		}
		String regex = prefix;
		
		if (numIntegers == null) {
			integers += REGEX_ANY_DIGIT + "*";
		} else if (numIntegers > 0) {
			integers += REGEX_ANY_DIGIT + "{0," + numIntegers + "}";
		} else if (numIntegers.equals(0)) {
			if (integers.equals("")) {
				integers = "0?";
			}
		}
		regex += integers;
		if (numDecimals == null) {
			decimals = REGEX_ANY_DIGIT + "*";
		} else {
			decimals = REGEX_ANY_DIGIT + "{0," + numDecimals + "}";
		}
		if (numDecimals == null || !numDecimals.equals(0)) {
			regex += REGEX_DEC_SEPARATOR;
		}
		
		regex += decimals;
		return regex;
	}
	
	public void setRange(NumericValueProvider minValue, NumericValueProvider maxValue) {
		setRange(minValue, maxValue, ModifyPreference.MODIFY_THIS);
	}
	
	public void setRange(NumericValueProvider minValue, NumericValueProvider maxValue, ModifyPreference modifyPreference) {
		minMaxValidator = new MinMaxFieldValidator(minValue, this, maxValue, modifyPreference);
	}
	
	public void setRange(long minValue, long maxValue) {
		setRange(new PreciseDecimal(minValue), new PreciseDecimal(maxValue));
	}
	
	public void setRange(double minValue, double maxValue) {
		setRange(new PreciseDecimal(minValue), new PreciseDecimal(maxValue));
	}
	
	@Override
	public boolean isValid() {
		return super.isValid(); /*&& (minMaxValidator == null || minMaxValidator.isValid()); */
	}
	
	@Override
	public PreciseDecimal getPreciseDecimal() {
		return getPreciseDecimalOrDefault(PreciseDecimal.ZERO);
	}
	
	public PreciseDecimal getPreciseDecimalOrDefault(final PreciseDecimal defaultValue) {
		if (isValid()) {
			return new PreciseDecimal(getValue());
		} else {
			return defaultValue;
		}
	}
	
	public void setValue(double value) {
		super.setValue("" + value);
	}
	
	@Override
	public double getDouble() {
		return getDoubleOrDefault(0.0);
	}
	
	public double getDoubleOrDefault(double defaultValue) {
		if (!super.getValue().equals("") && isValid()) {
			return PreciseDecimal.parseDoubleFromString(super.getValue());
		}
		return defaultValue;
	}
	
	@Override
	public void setValue(NumericValueProvider preciseDecimal) {
		this.setValue(preciseDecimal.toString());
	}
	
	@Override
	public boolean isMutable() {
		return true;
	}
	
	@Override
	public boolean canChangeValueTo(double newValue) {
		boolean isValid = super.getRegexFieldExtender().isValid(new PreciseDecimal(newValue).toString());
		isValid &= minMaxValidator.isValid();
		return isValid;
	}
}

