package uk.ac.lkl.common.util.value;

public class DoubleValue extends NumericValue<DoubleValue> {

    public static final DoubleValue ZERO = new DoubleValue(0.0);

    public static final DoubleValue ONE = new DoubleValue(1.0);

    private double value;

    public DoubleValue(double value) {
	this.value = value;
    }

    // return 'this' since immutable
    @Override
    public DoubleValue createCopy() {
	return this;
    }
    
    @Override
    public Class<? extends DoubleValue> getValueClass() {
	return this.getClass();
    }

    @Override
    public DoubleValue getZero() {
	return ZERO;
    }

    @Override
    public boolean isValid() {
	return !Double.isNaN(value);
    }

    @Override
    public DoubleValue negate() {
	return new DoubleValue(-this.value);
    }

    @Override
    public DoubleValue add(DoubleValue other) {
	return new DoubleValue(this.value + other.value);
    }

    @Override
    public DoubleValue subtract(DoubleValue other) {
	return new DoubleValue(this.value - other.value);
    }

    @Override
    public DoubleValue multiply(DoubleValue other) {
	return new DoubleValue(this.value * other.value);
    }

    @Override
    public DoubleValue divide(DoubleValue other) {
	return new DoubleValue(this.value / other.value);
    }

    @Override
    public DoubleValue absoluteValue() {
	return new DoubleValue(Math.abs(value));
    }

    @Override
    public boolean isGreaterThan(DoubleValue other) {
	return this.value > other.value;
    }

    @Override
    public boolean isLessThan(DoubleValue other) {
	return this.value < other.value;
    }

    @Override
    public boolean isEqualTo(DoubleValue other) {
	return this.value == other.value;
    }

    // very important for managing change events
    @Override
    public boolean equals(Object object) {
	if (!(object instanceof DoubleValue))
	    return false;
	DoubleValue other = (DoubleValue) object;
	return this.value == other.value;
    }

    @Override
    public boolean isZero() {
	return value == 0;
    }

    @Override
    public boolean isPositive() {
	return value > 0;
    }

    @Override
    public boolean isNegative() {
	return value < 0;
    }

    @Override
    public String toString() {
	return Double.toString(value);
    }

    public byte byteValue() {
	return (byte) value;
    }

    @Override
    public double doubleValue() {
	return value;
    }

    public float floatValue() {
	return (float) value;
    }

    @Override
    public int intValue() {
	return (int) value;
    }

    public long longValue() {
	return (long) value;
    }

    public short shortValue() {
	return (short) value;
    }

    public DoubleValue createFromDouble(double d) {
	return new DoubleValue(d);
    }

    @Override
    public DoubleValue createFromInt(int i) {
	return new DoubleValue(i);
    }

    @Override
    public boolean isInteger() {
	return false;
    }

}

