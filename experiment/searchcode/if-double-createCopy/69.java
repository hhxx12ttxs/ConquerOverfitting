package uk.ac.lkl.common.util.value;

public class IntegerValue extends NumericValue<IntegerValue> {

    public static final IntegerValue ZERO = new IntegerValue(0);

    public static final IntegerValue ONE = new IntegerValue(1);
    
    public static final IntegerValue NEGATIVE_ONE = new IntegerValue(-1);

    private int value;

    public IntegerValue(IntegerValue value) {
	this(value.intValue());
    }
    
    public IntegerValue() {
	this(0);
    }
    
    public IntegerValue(int value) {
	this.value = value;
    }

    @Override
    public IntegerValue createCopy() {
	return this;
    }
    
    @Override
    public Class<? extends IntegerValue> getValueClass() {
	return this.getClass();
    }
    
    @Override
    public IntegerValue getZero() {
	return ZERO;
    }

    @Override
    public boolean isValid() {
	return true;
    }
    
    @Override
    public IntegerValue negate() {
	return new IntegerValue(-this.value);
    }

    @Override
    public IntegerValue add(IntegerValue other) {
	return new IntegerValue(this.value + other.value);
    }

    @Override
    public IntegerValue subtract(IntegerValue other) {
	return new IntegerValue(this.value - other.value);
    }

    @Override
    public IntegerValue multiply(IntegerValue other) {
	return new IntegerValue(this.value * other.value);
    }

    @Override
    public IntegerValue divide(IntegerValue other) {
	return new IntegerValue(this.value / other.value);
    }

    @Override
    public IntegerValue absoluteValue() {
	return new IntegerValue(Math.abs(value));
    }

    @Override
    public boolean isGreaterThan(IntegerValue other) {
	return this.value > other.value;
    }

    @Override
    public boolean isLessThan(IntegerValue other) {
	return this.value < other.value;
    }

    @Override
    public boolean isEqualTo(IntegerValue other) {
	return this.value == other.value;
    }

    // very important for managing change events
    @Override
    public boolean equals(Object object) {
	if (!(object instanceof IntegerValue))
	    return false;
	IntegerValue other = (IntegerValue) object;
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

//    @Override
//    public byte byteValue() {
//	return (byte) value;
//    }
//
    @Override
    public double doubleValue() {
	return value;
    }
//
//    @Override
//    public float floatValue() {
//	return value;
//    }

    @Override
    public int intValue() {
	return value;
    }

//    @Override
//    public long longValue() {
//	return value;
//    }
//
//    @Override
//    public short shortValue() {
//	return (short) value;
//    }

    @Override
    public String toString() {
	return Integer.toString(value);
    }

    // note: loses accuracy (inevitably)
//    @Override
//    public IntegerValue createFromDouble(double d) {
//	return new IntegerValue((int) d);
//    }

    @Override
    public IntegerValue createFromInt(int i) {
	return new IntegerValue(i);
    }

    @Override
    public int hashCode() {
	return value;
    }

    @Override
    public boolean isInteger() {
	return true;
    }

}

