<<<<<<< HEAD
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
=======
package org.rrd4j.core;

import java.io.IOException;

/**
 * Base implementation class for all backend classes. Each Round Robin Database object
 * ({@link RrdDb} object) is backed with a single RrdBackend object which performs
 * actual I/O operations on the underlying storage. Rrd4j supports
 * multiple backends out of the box. E.g.:</p>
 * <ul>
 * <li>{@link RrdRandomAccessFileBackend}: objects of this class are created from the
 * {@link RrdRandomAccessFileBackendFactory} class. This was the default backend used in all
 * Rrd4j releases prior to 1.4.0. It uses java.io.* package and
 * RandomAccessFile class to store RRD data in files on the disk.
 *
 * <li>{@link RrdNioBackend}: objects of this class are created from the
 * {@link RrdNioBackendFactory} class. The backend uses java.io.* and java.nio.*
 * classes (mapped ByteBuffer) to store RRD data in files on the disk. This backend is fast, very fast,
 * but consumes a lot of memory (borrowed not from the JVM but from the underlying operating system
 * directly). <b>This is the default backend used in Rrd4j since 1.4.0 release.</b>
 *
 * <li>{@link RrdMemoryBackend}: objects of this class are created from the
 * {@link RrdMemoryBackendFactory} class. This backend stores all data in memory. Once
 * JVM exits, all data gets lost. The backend is extremely fast and memory hungry.
 * </ul>
 *
 * To create your own backend in order to provide some custom type of RRD storage,
 * you should do the following:</p>
 *
 * <ul>
 * <li>Create your custom RrdBackend class (RrdCustomBackend, for example)
 * by extending RrdBackend class. You have to implement all abstract methods defined
 * in the base class.
 *
 * <li>Create your custom RrdBackendFactory class (RrdCustomBackendFactory,
 * for example) by extending RrdBackendFactory class. You have to implement all
 * abstract methods defined in the base class. Your custom factory class will actually
 * create custom backend objects when necessary.
 *
 * <li>Create instance of your custom RrdBackendFactory and register it as a regular
 * factory available to Rrd4j framework. See javadoc for {@link RrdBackendFactory} to
 * find out how to do this.
 * </ul>
 */
public abstract class RrdBackend {
    private static boolean instanceCreated = false;
    private final String path;

    /**
     * Creates backend for a RRD storage with the given path.
     *
     * @param path String identifying RRD storage. For files on the disk, this
     *             argument should represent file path. Other storage types might interpret
     *             this argument differently.
     */
    protected RrdBackend(String path) {
        this.path = path;
        instanceCreated = true;
    }

    /**
     * Returns path to the storage.
     *
     * @return Storage path
     */
    public String getPath() {
        return path;
    }

    /**
     * Writes an array of bytes to the underlying storage starting from the given
     * storage offset.
     *
     * @param offset Storage offset.
     * @param b      Array of bytes that should be copied to the underlying storage
     * @throws IOException Thrown in case of I/O error
     */
    protected abstract void write(long offset, byte[] b) throws IOException;

    /**
     * Reads an array of bytes from the underlying storage starting from the given
     * storage offset.
     *
     * @param offset Storage offset.
     * @param b      Array which receives bytes from the underlying storage
     * @throws IOException Thrown in case of I/O error
     */
    protected abstract void read(long offset, byte[] b) throws IOException;

    /**
     * Returns the number of RRD bytes in the underlying storage.
     *
     * @return Number of RRD bytes in the storage.
     * @throws IOException Thrown in case of I/O error.
     */
    public abstract long getLength() throws IOException;

    /**
     * Sets the number of bytes in the underlying RRD storage.
     * This method is called only once, immediately after a new RRD storage gets created.
     *
     * @param length Length of the underlying RRD storage in bytes.
     * @throws IOException Thrown in case of I/O error.
     */
    protected abstract void setLength(long length) throws IOException;

    /**
     * Closes the underlying backend.
     *
     * @throws IOException Thrown in case of I/O error
     */
    public void close() throws IOException {
    }

    /**
     * This method suggests the caching policy to the Rrd4j frontend (high-level) classes. If <code>true</code>
     * is returned, frontend classes will cache frequently used parts of a RRD file in memory to improve
     * performance. If </code>false</code> is returned, high level classes will never cache RRD file sections
     * in memory.
     *
     * @return <code>true</code> if file caching is enabled, <code>false</code> otherwise. By default, the
     *         method returns <code>true</code> but it can be overriden in subclasses.
     */
    protected boolean isCachingAllowed() {
        return true;
    }

    /**
     * Reads all RRD bytes from the underlying storage.
     *
     * @return RRD bytes
     * @throws IOException Thrown in case of I/O error
     */
    public final byte[] readAll() throws IOException {
        byte[] b = new byte[(int) getLength()];
        read(0, b);
        return b;
    }

    final void writeInt(long offset, int value) throws IOException {
        write(offset, getIntBytes(value));
    }

    final void writeLong(long offset, long value) throws IOException {
        write(offset, getLongBytes(value));
    }

    final void writeDouble(long offset, double value) throws IOException {
        write(offset, getDoubleBytes(value));
    }

    final void writeDouble(long offset, double value, int count) throws IOException {
        byte[] b = getDoubleBytes(value);
        byte[] image = new byte[8 * count];
        for (int i = 0, k = 0; i < count; i++) {
            image[k++] = b[0];
            image[k++] = b[1];
            image[k++] = b[2];
            image[k++] = b[3];
            image[k++] = b[4];
            image[k++] = b[5];
            image[k++] = b[6];
            image[k++] = b[7];
        }
        write(offset, image);
        image = null;
    }

    final void writeDouble(long offset, double[] values) throws IOException {
        int count = values.length;
        byte[] image = new byte[8 * count];
        for (int i = 0, k = 0; i < count; i++) {
            byte[] b = getDoubleBytes(values[i]);
            image[k++] = b[0];
            image[k++] = b[1];
            image[k++] = b[2];
            image[k++] = b[3];
            image[k++] = b[4];
            image[k++] = b[5];
            image[k++] = b[6];
            image[k++] = b[7];
        }
        write(offset, image);
        image = null;
    }

    final void writeString(long offset, String value) throws IOException {
        value = value.trim();
        byte[] b = new byte[RrdPrimitive.STRING_LENGTH * 2];
        for (int i = 0, k = 0; i < RrdPrimitive.STRING_LENGTH; i++) {
            char c = (i < value.length()) ? value.charAt(i) : ' ';
            byte[] cb = getCharBytes(c);
            b[k++] = cb[0];
            b[k++] = cb[1];
        }
        write(offset, b);
    }

    final int readInt(long offset) throws IOException {
        byte[] b = new byte[4];
        read(offset, b);
        return getInt(b);
    }

    final long readLong(long offset) throws IOException {
        byte[] b = new byte[8];
        read(offset, b);
        return getLong(b);
    }

    final double readDouble(long offset) throws IOException {
        byte[] b = new byte[8];
        read(offset, b);
        return getDouble(b);
    }

    final double[] readDouble(long offset, int count) throws IOException {
        int byteCount = 8 * count;
        byte[] image = new byte[byteCount];
        read(offset, image);
        double[] values = new double[count];
        for (int i = 0, k = -1; i < count; i++) {
            byte[] b = new byte[]{
                    image[++k], image[++k], image[++k], image[++k],
                    image[++k], image[++k], image[++k], image[++k]
            };
            values[i] = getDouble(b);
        }
        image = null;
        return values;
    }

    final String readString(long offset) throws IOException {
        byte[] b = new byte[RrdPrimitive.STRING_LENGTH * 2];
        char[] c = new char[RrdPrimitive.STRING_LENGTH];
        read(offset, b);
        for (int i = 0, k = -1; i < RrdPrimitive.STRING_LENGTH; i++) {
            byte[] cb = new byte[]{b[++k], b[++k]};
            c[i] = getChar(cb);
        }
        return new String(c).trim();
    }

    // static helper methods

    private static byte[] getIntBytes(int value) {
        byte[] b = new byte[4];
        b[0] = (byte) ((value >>> 24) & 0xFF);
        b[1] = (byte) ((value >>> 16) & 0xFF);
        b[2] = (byte) ((value >>> 8) & 0xFF);
        b[3] = (byte) ((value >>> 0) & 0xFF);
        return b;
    }

    private static byte[] getLongBytes(long value) {
        byte[] b = new byte[8];
        b[0] = (byte) ((int) (value >>> 56) & 0xFF);
        b[1] = (byte) ((int) (value >>> 48) & 0xFF);
        b[2] = (byte) ((int) (value >>> 40) & 0xFF);
        b[3] = (byte) ((int) (value >>> 32) & 0xFF);
        b[4] = (byte) ((int) (value >>> 24) & 0xFF);
        b[5] = (byte) ((int) (value >>> 16) & 0xFF);
        b[6] = (byte) ((int) (value >>> 8) & 0xFF);
        b[7] = (byte) ((int) (value >>> 0) & 0xFF);
        return b;
    }

    private static byte[] getCharBytes(char value) {
        byte[] b = new byte[2];
        b[0] = (byte) ((value >>> 8) & 0xFF);
        b[1] = (byte) ((value >>> 0) & 0xFF);
        return b;
    }

    private static byte[] getDoubleBytes(double value) {
        byte[] bytes = getLongBytes(Double.doubleToLongBits(value));
        return bytes;
    }

    private static int getInt(byte[] b) {
        assert b.length == 4 : "Invalid number of bytes for integer conversion";
        return ((b[0] << 24) & 0xFF000000) + ((b[1] << 16) & 0x00FF0000) +
                ((b[2] << 8) & 0x0000FF00) + ((b[3] << 0) & 0x000000FF);
    }

    private static long getLong(byte[] b) {
        assert b.length == 8 : "Invalid number of bytes for long conversion";
        int high = getInt(new byte[]{b[0], b[1], b[2], b[3]});
        int low = getInt(new byte[]{b[4], b[5], b[6], b[7]});
        return ((long) (high) << 32) + (low & 0xFFFFFFFFL);
    }

    private static char getChar(byte[] b) {
        assert b.length == 2 : "Invalid number of bytes for char conversion";
        return (char) (((b[0] << 8) & 0x0000FF00) + ((b[1] << 0) & 0x000000FF));
    }

    private static double getDouble(byte[] b) {
        assert b.length == 8 : "Invalid number of bytes for double conversion";
		return Double.longBitsToDouble(getLong(b));
	}

	static boolean isInstanceCreated() {
		return instanceCreated;
	}
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

