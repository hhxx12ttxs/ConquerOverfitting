package SongGenerator;

import org.joda.time.DateTime;

/**
 * This is a Value class to hold one of the following data types:Date, Integer, Double, String, Boolean.
 * This class is immutable.
 * @author Alicia Bendz
 *
 */
public class Value {

	/**
	 * Enum to indicate type of value.
	 * @author Alicia Bendz
	 *
	 */
	public enum Type { DATE, INTEGER, DOUBLE, STRING, BOOLEAN };
	
	/**
	 * Type of value.
	 */
	private final Type mType;
	
	/**
	 * The possible values.
	 */
	private final DateTime mDateValue;
	private final Integer mIntegerValue;
	private final Double mDoubleValue;
	private final String mStringValue;
	private final Boolean mBooleanValue;
	
	/**
	 * Default constructor.
	 */
	public Value(){
		mDateValue = null;
		mIntegerValue = null;
		mDoubleValue = null;
		mStringValue = null;
		mType = null;
		mBooleanValue = null;
	}
	
	/**
	 * Date constructor setting all but the Date field to null.
	 * @param date The Date to store.
	 */
	public Value(DateTime date){
		mDateValue = date;
		mIntegerValue = null;
		mDoubleValue = null;
		mStringValue = null;
		mBooleanValue = null;
		mType = Type.DATE;
	}
	
	/**
	 * Integer constructor setting all but the Integer field to null.
	 * @param integer The Integer to store.
	 */
	public Value(Integer integer){
		mDateValue = null;
		mIntegerValue = integer;
		mDoubleValue = null;
		mStringValue = null;
		mBooleanValue = null;
		mType = Type.INTEGER;
	}
	
	/**
	 * Double constructor setting all but the Double field to null.
	 * @param doubl The Double to store.
	 */
	public Value(Double doubl){
		mDateValue = null;
		mIntegerValue = null;
		mDoubleValue = doubl;
		mStringValue = null;
		mBooleanValue = null;
		mType = Type.DOUBLE;
	}
	
	/**
	 * String constructor setting all but the String field to null.
	 * @param string The String to store.
	 */
	public Value(String string){
		mDateValue = null;
		mIntegerValue = null;
		mDoubleValue = null;
		mBooleanValue = null;
		mStringValue = string;
		mType = Type.STRING;
	}
	
	/**
	 * Boolean constructor setting all but the Boolean field to null.
	 * @param bool The Boolean to store.
	 */
	public Value(Boolean bool){
		mDateValue = null;
		mIntegerValue = null;
		mDoubleValue = null;
		mBooleanValue = bool;
		mStringValue = null;
		mType = Type.BOOLEAN;
	}
	
	/**
	 * If the value of this Value is greater than the given value, return true. this.value > v.
	 * This does not apply to Boolean values.
	 * @param v The value to compare to.
	 * @return If the given value is less than this value.
	 */
	public boolean greaterThan(Value v){
		
		//determine type of current value and if the type of the given value is the same, 
		//compare them
		if(mDateValue != null && v.isType(Type.DATE)){
			
			//a date is greater than another if it happens after the other
			if(mDateValue.isAfter(v.getDate()))
				return true;
		} else if (mIntegerValue != null && v.isType(Type.INTEGER)){
			if(mIntegerValue.intValue() > v.getInteger().intValue())
				return true;
		} else if (mDoubleValue != null && v.isType(Type.DOUBLE)){
			if(mDoubleValue.doubleValue() > v.getDouble().doubleValue())
				return true;
			
		} else if (mStringValue != null && v.isType(Type.STRING)){
			//Lexicographical comparison
			if(mStringValue.compareTo(v.getString()) > 0)
				return true;
		}
		
		return false;
	}
	
	/**
	 * If the value of this Value is less than the given value, return true. this.value < v.
	 * This does not apply to boolean values.
	 * @param v The value to compare to.
	 * @return If the given value is greater than this value.
	 */
	public boolean lessThan(Value v){

		//determine type of current value and if the type of the given value is the same, 
		//compare them
		if(mDateValue != null && v.isType(Type.DATE)){
			
			//a date is less than another if it happens before the other
			if(mDateValue.isBefore(v.getDate()))
				return true;
		} else if (mIntegerValue != null && v.isType(Type.INTEGER)){
			if(mIntegerValue.intValue() < v.getInteger().intValue())
				return true;
		} else if (mDoubleValue != null && v.isType(Type.DOUBLE)){
			if(mDoubleValue.doubleValue() < v.getDouble().doubleValue())
				return true;
			
		} else if (mStringValue != null && v.isType(Type.STRING)){
			//Lexicographical comparison
			if(mStringValue.compareTo(v.getString()) < 0)
				return true;
		}
		
		return false;
	}
	
	/**
	 * This method checks if two values are equal. Two values are equal if their stored 
	 * values are equal.
	 * This may not be useful for Dates and Doubles.
	 * @param v The value to check against.
	 * @return This returns true if the stored value in both Values is the same.
	 */
	public boolean equal(Value v){
		
		//determine type of current value and if the type of the given value is the same, 
		//compare them
		if(mDateValue != null && v.isType(Type.DATE)){
			if(mDateValue.isEqual((v.getDate())))
					return true;
		} else if (mIntegerValue != null && v.isType(Type.INTEGER)){
				if(mIntegerValue.intValue() == v.getInteger().intValue())
						return true;
		} else if (mDoubleValue != null && v.isType(Type.DOUBLE)){
			if(mDoubleValue.doubleValue() == v.getDouble().doubleValue())
					return true;
					
		} else if (mStringValue != null && v.isType(Type.STRING)){
			//Lexicographical comparison
			return mStringValue.equals(v.getString());
		} else if (mBooleanValue != null && v.isType(Type.BOOLEAN)){
			return (mBooleanValue && v.getBoolean()) || (!mBooleanValue && !v.getBoolean());
		}
				return false;
	}
	
	/**
	 * This method determines if this value contains the given value. This can only be 
	 * applied to Strings.
	 * @param v The value to check against.
	 * @return If this value contains v, then return true.
	 */
	public boolean contains(Value v){
		//this only applies to strings
		if (mStringValue != null && v.isType(Type.STRING)){
			//Lexicographical comparison
			return mStringValue.contains(v.getString());
		}
				
		return false;
	}
	
	/**
	 * Check if the type of the Value matches a given type.
	 * @param t The given type
	 * @return
	 */
	public boolean isType(Type t){
		return t == mType;
	}
	
	/**
	 * Getter for the DateTime value of the Value. Returns null if there isn't one.
	 * @return The DateTime or null.
	 */
	public DateTime getDate(){
		return mDateValue;
	}
	
	/**
	 * Getter for the Integer value of the Value. Returns null if there isn't one.
	 * @return The Integer or null.
	 */
	public Integer getInteger(){
		return mIntegerValue;
	}
	
	/**
	 * Getter for the Double value of the Value. Returns null if there isn't one.
	 * @return The Double or null.
	 */
	public Double getDouble(){
		return mDoubleValue;
	}
	
	/**
	 * Get the String value of the Value. Returns null if there isn't one.
	 * @return The String or null.
	 */
	public String getString(){
		return mStringValue;
	}
	
	/**
	 * Get the Boolean value of this Value. Returns null if there isn't one.
	 * @return The Boolean value or null.
	 */
	public Boolean getBoolean(){
		return mBooleanValue;
	}
	
	/**
	 * A method that will compare two values according to a given operator. Evaluates the 
	 * expression: v [operator] thisValue.
	 * If check(new Value(3), GREATER) is called on a Value(2), it will return true since 3 > 2.
	 * Ignores the maximize and minimize operators.
	 * @param v The value to compare to.
	 * @param o The operator of comparison.
	 * @return True if the expression is true, false if not.
	 */
	public boolean check(Value v, Operator o){
		switch (o){
		case EQUALS:
			return equal(v);
		case GREATER:
			return v.greaterThan(this);
		case LESS:
			return v.lessThan(this);
		case GREATER_EQUAL:
			return equal(v) || v.greaterThan(this);
		case LESS_EQUAL:
			return equal(v)|| v.lessThan(this);
		case NOT_EQUAL:
			return !equal(v);
		case CONTAINS:
			return v.contains(this);
		case NOT_CONTAINS:
			return !v.contains(this);
		default:
			return false;
		}
	}

}

