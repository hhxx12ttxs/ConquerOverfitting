//---------------------------------------------------------------------------------
//
// BlackBerry Extensions
// Copyright (c) 2008-2012 Vincent Simonetti
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
// documentation files (the "Software"), to deal in the Software without restriction, including without limitation 
// the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
// to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
// INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
// PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
// OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.
//
//---------------------------------------------------------------------------------
//
// Created 2009
package rebuild.util.ref;

import rebuild.BBXResource;
import rebuild.Resources;
import rebuild.util.MathUtilities;
import rebuild.util.text.StringUtilities;

/**
 * <p>A reference number, this class contains many of the different operations that a number could do.</p>
 * <p>Anything of type {@link RefNumber} is treated like the number it represents. If a number of type 
 * <code>double</code> must be converted to an <code>int</code> to be processed, the same will occur here and the type 
 * will be automatically converted to the required type. Also the same operation will occur for 
 * {@link RefDouble} and {@link RefInteger} if the same operation were to occur (just like explained above).</p>
 * <p>If the number could not be converted, or there is more then one type it could be converted to, and that could cause a 
 * different result (with the exception of different size types), then the number will be returned as-is and no 
 * processing will occur. If a numeric error will occur with any numbers (such as divide by zero), it will be thrown.</p>
 * <p>{@link RefNumber}s can be "fixed" or constant. They cannot be changed and must be cloned in order to change there
 * value. If any operation is performed on a fixed {@link RefNumber} then the number is cloned automatically and the
 * operation is performed on the clone. The original number is not changed at all.</p>
 * @since BBX 1.1.0
 */
public abstract class RefNumber
{
	private static final RefULong ONE = new RefULong(1L, true);
	private static final double UINT_CAST_DOUBLE_MAX = ((double)0xFFFFFFFFL) + 0.5;
	private static final double INT_CAST_DOUBLE_MAX = ((double)Integer.MAX_VALUE) + 0.5;
	private static final double INT_CAST_DOUBLE_MIN = -(INT_CAST_DOUBLE_MAX + 1.0);
	private static final double INT_HIGH_AS_DOUBLE = Integer.MAX_VALUE + 1.0;
	//private static final double ULONG_AS_DOUBLE = (Long.MAX_VALUE * 2.0) + 1.0;
	
	/**
	 * Arithmetic bit-shift left, <code>&lt;&lt;</code>.
	 */
	public static final int BIT_SHIFT_LEFT = 0;
	/**
	 * Arithmetic bit-shift right, <code>&gt;&gt;</code>.
	 */
	public static final int BIT_SHIFT_RIGHT = BIT_SHIFT_LEFT + 1;
	/**
	 * Logical bit-shift left, <code>&lt;&lt;&lt;</code>. This produces the same result as {@link #BIT_SHIFT_LEFT}.
	 */
	public static final int BIT_SHIFT_LEFT_LOGICAL = BIT_SHIFT_RIGHT + 1;
	/**
	 * Logical bit-shift right, <code>&gt;&gt;&gt;</code>. This shifts the value right but ignores the sign.
	 */
	public static final int BIT_SHIFT_RIGHT_LOGICAL = BIT_SHIFT_LEFT_LOGICAL + 1;
	
	/**
	 * An array of premade bit masks: high bits (1111..., 1110..., 1100..., 1000..., 0000...). Zero is all bits set, as
	 * the index increases, the number gets shifted left until it reaches zero in value.
	 */
	static long[] LONG_HIGH_BIT_MASK;
	/**
	 * An array of premade bit masks: low bits (...1111, ...0111, ...0011, ...0001, ...0000). Zero is all bits set, as
	 * the index increases, the number gets shifted right until it reaches zero in value.
	 */
	static long[] LONG_LOW_BIT_MASK;
	protected boolean _fixed;
	
	static
	{
		LONG_HIGH_BIT_MASK = new long[65];
		long fullBit = ~0L; //Get all bits set to 1
		LONG_HIGH_BIT_MASK[0] = fullBit;
		for(int i = 1; i < 64; i++)
		{
			fullBit <<= 1;
			LONG_HIGH_BIT_MASK[i] = fullBit;
		}
		
		LONG_LOW_BIT_MASK = new long[65];
		fullBit = ~0L; //Get all bits set to 1
		LONG_LOW_BIT_MASK[64] = fullBit;
		for(int i = 63; i >= 0; i--)
		{
			fullBit >>>= 1;
			LONG_LOW_BIT_MASK[i] = fullBit;
		}
	}
	
	RefNumber()
	{
		this(false);
	}
	
	RefNumber(boolean fixed)
	{
		_fixed = fixed;
	}
	
	/**
	 * If this {@link RefNumber} is read only.
	 * @return <code>true</code> if this {@link RefNumber} is read only, <code>false</code> if otherwise.
	 */
	public boolean isReadOnly()
	{
		return _fixed;
	}
	
	/**
	 * A clone of this {@link RefNumber}. The only component that is not copied is if the number is fixed or not.
	 * @return The clone of this {@link RefNumber}.
	 */
	public abstract RefNumber cloneNumber();
	
	/**
	 * Convert a {@link RefNumber} to a fixed value.
	 * @param num The {@link RefNumber} to make fixed.
	 * @return The {@link RefNumber} fixed.
	 */
	public static RefNumber makeFixed(RefNumber num)
	{
		if(num == null)
		{
			return null;
		}
		if(num._fixed)
		{
			return num;
		}
		RefNumber n = num.cloneNumber();
		n._fixed = true;
		return n;
	}
	
	/**
	 * Convert this {@link RefNumber} to a fixed value. Note, this does not set the current {@link RefNumber} to 
	 * fixed, it clones it then makes the clone fixed.
	 * @return The {@link RefNumber} fixed.
	 */
	public RefNumber makeFixed()
	{
		return RefNumber.makeFixed(this);
	}
	
	/*
	 * Helper/conversion functions
	 */
	private static RefNumber convertToRef(Object obj)
	{
		if(obj == null)
		{
			return null;
		}
		
		//Ignore anything that is not a numeric value.
		if(obj instanceof Byte)
		{
			return new RefByte((Byte)obj);
		}
		else if(obj instanceof Short)
		{
			return new RefShort((Short)obj);
		}
		else if(obj instanceof Integer)
		{
			return new RefInteger((Integer)obj);
		}
		else if(obj instanceof Character)
		{
			return new RefInteger(((Character)obj).charValue());
		}
		else if(obj instanceof Long)
		{
			return new RefLong((Long)obj);
		}
		else if(obj instanceof Float)
		{
			return new RefFloat((Float)obj);
		}
		else if(obj instanceof Double)
		{
			return new RefDouble((Double)obj);
		}
		else if(obj instanceof RefNumber)
		{
			return (RefNumber)obj;
		}
		return null;
	}
	
	/* 
	 * ----------------------------------------
	 * Math operations
	 * ----------------------------------------
	 */
	
	/*
	 * General operations
	 */
	
	/**
	 * Increment this number.
	 * @return This number. If it is read only then a copy that has been incremented is returned.
	 */
	public RefNumber increment()
	{
		if(this._fixed)
		{
			return this.cloneNumber().increment();
		}
		if(this instanceof RefByte)
		{
			((RefByte)this)._val++;
		}
		else if(this instanceof RefShort)
		{
			((RefShort)this)._val++;
		}
		else if(this instanceof RefInteger)
		{
			((RefInteger)this)._val++;
		}
		else if(this instanceof RefLong)
		{
			((RefLong)this)._val++;
		}
		else if(this instanceof RefUByte)
		{
			RefUByte rn = ((RefUByte)this);
			if(rn._val == 0xFF)
			{
				rn._val = 0;
			}
			else
			{
				rn._val++;
			}
		}
		else if(this instanceof RefUShort)
		{
			RefUShort rn = ((RefUShort)this);
			if(rn._val == 0xFFFF)
			{
				rn._val = 0;
			}
			else
			{
				rn._val++;
			}
		}
		else if(this instanceof RefUInteger)
		{
			RefUInteger rn = ((RefUInteger)this);
			if(rn._val == 0xFFFFFFFFL)
			{
				rn._val = 0L;
			}
			else
			{
				rn._val++;
			}
		}
		else if(this instanceof RefULong)
		{
			((RefULong)this).ulongAdd(ONE, true);
		}
		else if(this instanceof RefFloat)
		{
			((RefFloat)this)._val++;
		}
		else if(this instanceof RefDouble)
		{
			((RefDouble)this)._val++;
		}
		return this;
	}
	
	/**
	 * Decrement this number.
	 * @return This number. If it is read only then a copy that has been decremented is returned.
	 */
	public RefNumber decrement()
	{
		if(this._fixed)
		{
			return this.cloneNumber().decrement();
		}
		if(this instanceof RefByte)
		{
			((RefByte)this)._val--;
		}
		else if(this instanceof RefShort)
		{
			((RefShort)this)._val--;
		}
		else if(this instanceof RefInteger)
		{
			((RefInteger)this)._val--;
		}
		else if(this instanceof RefLong)
		{
			((RefLong)this)._val--;
		}
		else if(this instanceof RefUByte)
		{
			RefUByte rn = ((RefUByte)this);
			if(rn._val == 0)
			{
				rn._val = 0xFF;
			}
			else
			{
				rn._val--;
			}
		}
		else if(this instanceof RefUShort)
		{
			RefUShort rn = ((RefUShort)this);
			if(rn._val == 0)
			{
				rn._val = 0xFFFF;
			}
			else
			{
				rn._val--;
			}
		}
		else if(this instanceof RefUInteger)
		{
			RefUInteger rn = ((RefUInteger)this);
			if(rn._val == 0)
			{
				rn._val = 0xFFFFFFFFL;
			}
			else
			{
				rn._val--;
			}
		}
		else if(this instanceof RefULong)
		{
			((RefULong)this).ulongSubtract(ONE);
		}
		else if(this instanceof RefFloat)
		{
			((RefFloat)this)._val--;
		}
		else if(this instanceof RefDouble)
		{
			((RefDouble)this)._val--;
		}
		return this;
	}
	
	//Add
	
	/**
	 * Add a number to this number.
	 * @param value The number to add.
	 * @return This number. If it is read only then a copy that has had the number added is returned.
	 */
	public RefNumber add(byte value)
	{
		return add(castToByte(value));
	}
	
	/**
	 * Add a number to this number.
	 * @param value The number to add.
	 * @return This number. If it is read only then a copy that has had the number added is returned.
	 */
	public RefNumber add(short value)
	{
		return add(castToShort(value));
	}
	
	/**
	 * Add a number to this number.
	 * @param value The number to add.
	 * @return This number. If it is read only then a copy that has had the number added is returned.
	 */
	public RefNumber add(int value)
	{
		return add(castToInt(value));
	}
	
	/**
	 * Add a number to this number.
	 * @param value The number to add.
	 * @return This number. If it is read only then a copy that has had the number added is returned.
	 */
	public RefNumber add(long value)
	{
		return add(castToLong(value));
	}
	
	/**
	 * Add a number to this number.
	 * @param value The number to add.
	 * @return This number. If it is read only then a copy that has had the number added is returned.
	 */
	public RefNumber add(float value)
	{
		return add(castToFloat(value));
	}
	
	/**
	 * Add a number to this number.
	 * @param value The number to add.
	 * @return This number. If it is read only then a copy that has had the number added is returned.
	 */
	public RefNumber add(double value)
	{
		return add(castToDouble(value));
	}
	
	/**
	 * Add some value to this number, if the passed object is not of any use then it is ignored.
	 * @param value The Object to add, examples of supported types are {@link RefByte} and {@link Byte}.
	 * @return This number. If it is read only then a copy that has had the number added is returned.
	 */
	public RefNumber add(Object value)
	{
		RefNumber ref = convertToRef(value);
		if(ref != null)
		{
			return add(ref);
		}
		return this;
	}
	
	/**
	 * Add a reference number to this number.
	 * @param number The {@link RefNumber} to add, if this is null than it is ignored.
	 * @return This number. If it is read only then a copy that has had the number added is returned.
	 */
	public RefNumber add(RefNumber number)
	{
		if(number != null)
		{
			if(this._fixed)
			{
				return this.cloneNumber().add(number);
			}
			//TODO: Addition doesn't get affected by "sign" (so optimize it) and overflow and "looping" need to be addressed and implemented
			if(this instanceof RefByte)
			{
				((RefByte)this)._val += RefNumber.castToByte(number)._val;
			}
			else if(this instanceof RefShort)
			{
				((RefShort)this)._val += RefNumber.castToShort(number)._val;
			}
			else if(this instanceof RefInteger)
			{
				((RefInteger)this)._val += RefNumber.castToInt(number)._val;
			}
			else if(this instanceof RefLong)
			{
				((RefLong)this)._val += RefNumber.castToLong(number)._val;
			}
			else if(this instanceof RefUByte)
			{
				RefUByte rb = (RefUByte)this;
				rb._val = (short)((rb._val + RefNumber.castToUByte(number)._val) % 0x100);
			}
			else if(this instanceof RefUShort)
			{
				RefUShort rs = (RefUShort)this;
				rs._val = (rs._val + RefNumber.castToUShort(number)._val) % 0x10000;
			}
			else if(this instanceof RefUInteger)
			{
				RefUInteger ri = (RefUInteger)this;
				ri._val = (ri._val + RefNumber.castToUInt(number)._val) % 0x100000000L;
			}
			else if(this instanceof RefULong)
			{
				((RefULong)this).ulongAdd(number, false);
			}
			else if(this instanceof RefFloat)
			{
				((RefFloat)this)._val += RefNumber.castToFloat(number)._val;
			}
			else if(this instanceof RefDouble)
			{
				((RefDouble)this)._val += RefNumber.castToDouble(number)._val;
			}
			else
			{
				throw new NumberFormatException(StringUtilities.format_java(Resources.getString(BBXResource.UNKNOWN_TYPE_W_MESSAGE), this.getClass(), Resources.getString(BBXResource.UNKNOWN_REFNUMBER)));
			}
		}
		return this;
	}
	
	//Subtract
	
	/**
	 * Subtract a number from this number.
	 * @param value The number to subtract.
	 * @return This number. If it is read only then a copy that has had the number subtracted is returned.
	 */
	public RefNumber subtract(byte value)
	{
		return subtract(castToByte(value));
	}
	
	/**
	 * Subtract a number from this number.
	 * @param value The number to subtract.
	 * @return This number. If it is read only then a copy that has had the number subtracted is returned.
	 */
	public RefNumber subtract(short value)
	{
		return subtract(castToShort(value));
	}
	
	/**
	 * Subtract a number from this number.
	 * @param value The number to subtract.
	 * @return This number. If it is read only then a copy that has had the number subtracted is returned.
	 */
	public RefNumber subtract(int value)
	{
		return subtract(castToInt(value));
	}
	
	/**
	 * Subtract a number from this number.
	 * @param value The number to subtract.
	 * @return This number. If it is read only then a copy that has had the number subtracted is returned.
	 */
	public RefNumber subtract(long value)
	{
		return subtract(castToLong(value));
	}
	
	/**
	 * Subtract a number from this number.
	 * @param value The number to subtract.
	 * @return This number. If it is read only then a copy that has had the number subtracted is returned.
	 */
	public RefNumber subtract(float value)
	{
		return subtract(castToFloat(value));
	}
	
	/**
	 * Subtract a number from this number.
	 * @param value The number to subtract.
	 * @return This number. If it is read only then a copy that has had the number subtracted is returned.
	 */
	public RefNumber subtract(double value)
	{
		return subtract(castToDouble(value));
	}
	
	/**
	 * Subtract some value from this number, if the passed object is not of any use then it is ignored.
	 * @param value The Object to subtract, examples of supported types are {@link RefByte} and {@link Byte}.
	 * @return This number. If it is read only then a copy that has had the number subtracted is returned.
	 */
	public RefNumber subtract(Object value)
	{
		RefNumber ref = convertToRef(value);
		if(ref != null)
		{
			return subtract(ref);
		}
		return this;
	}
	
	/**
	 * Subtract a reference number from this number.
	 * @param number The {@link RefNumber} to subtract, if this is null than it is ignored.
	 * @return This number. If it is read only then a copy that has had the number subtracted is returned.
	 */
	public RefNumber subtract(RefNumber number)
	{
		if(number != null)
		{
			if(this._fixed)
			{
				return this.cloneNumber().subtract(number);
			}
			//TODO: Subtraction doesn't get affected by "sign" (so optimize it) and overflow and "looping" need to be addressed and implemented
			if(this instanceof RefByte)
			{
				((RefByte)this)._val -= RefNumber.castToByte(number)._val;
			}
			else if(this instanceof RefShort)
			{
				((RefShort)this)._val -= RefNumber.castToShort(number)._val;
			}
			else if(this instanceof RefInteger)
			{
				((RefInteger)this)._val -= RefNumber.castToInt(number)._val;
			}
			else if(this instanceof RefLong)
			{
				((RefLong)this)._val -= RefNumber.castToLong(number)._val;
			}
			else if(this instanceof RefUByte)
			{
				RefUByte rb = (RefUByte)this;
				rb._val = (short)((rb._val - RefNumber.castToUByte(number)._val) % 0x100);
			}
			else if(this instanceof RefUShort)
			{
				RefUShort rs = (RefUShort)this;
				rs._val = (rs._val - RefNumber.castToUShort(number)._val) % 0x10000;
			}
			else if(this instanceof RefUInteger)
			{
				RefUInteger ri = (RefUInteger)this;
				ri._val = (ri._val - RefNumber.castToUInt(number)._val) % 0x100000000L;
			}
			else if(this instanceof RefULong)
			{
				((RefULong)this).ulongSubtract(number);
			}
			else if(this instanceof RefFloat)
			{
				((RefFloat)this)._val -= RefNumber.castToFloat(number)._val;
			}
			else if(this instanceof RefDouble)
			{
				((RefDouble)this)._val -= RefNumber.castToDouble(number)._val;
			}
			else
			{
				throw new NumberFormatException(StringUtilities.format_java(Resources.getString(BBXResource.UNKNOWN_TYPE_W_MESSAGE), this.getClass(), Resources.getString(BBXResource.UNKNOWN_REFNUMBER)));
			}
		}
		return this;
	}
	
	//Multiply
	
	/**
	 * Multiply this number by a number.
	 * @param value The number to multiply with.
	 * @return This number. If it is read only then a copy that has had the number multiplied is returned.
	 */
	public RefNumber multiply(byte value)
	{
		return multiply(castToByte(value));
	}
	
	/**
	 * Multiply this number by a number.
	 * @param value The number to multiply with.
	 * @return This number. If it is read only then a copy that has had the number multiplied is returned.
	 */
	public RefNumber multiply(short value)
	{
		return multiply(castToShort(value));
	}
	
	/**
	 * Multiply this number by a number.
	 * @param value The number to multiply with.
	 * @return This number. If it is read only then a copy that has had the number multiplied is returned.
	 */
	public RefNumber multiply(int value)
	{
		return multiply(castToInt(value));
	}
	
	/**
	 * Multiply this number by a number.
	 * @param value The number to multiply with.
	 * @return This number. If it is read only then a copy that has had the number multiplied is returned.
	 */
	public RefNumber multiply(long value)
	{
		return multiply(castToLong(value));
	}
	
	/**
	 * Multiply this number by a number.
	 * @param value The number to multiply with.
	 * @return This number. If it is read only then a copy that has had the number multiplied is returned.
	 */
	public RefNumber multiply(float value)
	{
		return multiply(castToFloat(value));
	}
	
	/**
	 * Multiply this number by a number.
	 * @param value The number to multiply with.
	 * @return This number. If it is read only then a copy that has had the number multiplied is returned.
	 */
	public RefNumber multiply(double value)
	{
		return multiply(castToDouble(value));
	}
	
	/**
	 * Multiply this number by some value, if the passed object is not of any use then it is ignored.
	 * @param value The Object to multiply with, examples of supported types are {@link RefByte} and {@link Byte}.
	 * @return This number. If it is read only then a copy that has had the number multiplied is returned.
	 */
	public RefNumber multiply(Object value)
	{
		RefNumber ref = convertToRef(value);
		if(ref != null)
		{
			return multiply(ref);
		}
		return this;
	}
	
	/**
	 * Multiply this number by a reference number.
	 * @param number The {@link RefNumber} to multiply with, if this is null than it is ignored.
	 * @return This number. If it is read only then a copy that has had the number multiplied is returned.
	 */
	public RefNumber multiply(RefNumber number)
	{
		if(number != null)
		{
			if(this._fixed)
			{
				return this.cloneNumber().multiply(number);
			}
			if(this instanceof RefByte)
			{
				((RefByte)this)._val *= RefNumber.castToByte(number)._val;
			}
			else if(this instanceof RefShort)
			{
				((RefShort)this)._val *= RefNumber.castToShort(number)._val;
			}
			else if(this instanceof RefInteger)
			{
				((RefInteger)this)._val *= RefNumber.castToInt(number)._val;
			}
			else if(this instanceof RefLong)
			{
				((RefLong)this)._val *= RefNumber.castToLong(number)._val;
			}
			else if(this instanceof RefUByte)
			{
				RefUByte rb = (RefUByte)this;
				rb._val = (short)((rb._val * RefNumber.castToUByte(number)._val) % 0x100);
			}
			else if(this instanceof RefUShort)
			{
				RefUShort rs = (RefUShort)this;
				rs._val = (rs._val * RefNumber.castToUShort(number)._val) % 0x10000;
			}
			else if(this instanceof RefUInteger)
			{
				RefUInteger ri = (RefUInteger)this;
				ri._val = (ri._val * RefNumber.castToUInt(number)._val) % 0x100000000L;
			}
			else if(this instanceof RefULong)
			{
				((RefULong)this).ulongMulti(number);
			}
			else if(this instanceof RefFloat)
			{
				((RefFloat)this)._val *= RefNumber.castToFloat(number)._val;
			}
			else if(this instanceof RefDouble)
			{
				((RefDouble)this)._val *= RefNumber.castToDouble(number)._val;
			}
			else
			{
				throw new NumberFormatException(StringUtilities.format_java(Resources.getString(BBXResource.UNKNOWN_TYPE_W_MESSAGE), this.getClass(), Resources.getString(BBXResource.UNKNOWN_REFNUMBER)));
			}
		}
		return this;
	}
	
	//Divide
	
	/**
	 * Divide this number by a number.
	 * @param value The number to divide with.
	 * @return This number. If it is read only then a copy that has had the number divided is returned.
	 */
	public RefNumber divide(byte value)
	{
		return divide(castToByte(value));
	}
	
	/**
	 * Divide this number by a number.
	 * @param value The number to divide with.
	 * @return This number. If it is read only then a copy that has had the number divided is returned.
	 */
	public RefNumber divide(short value)
	{
		return divide(castToShort(value));
	}
	
	/**
	 * Divide this number by a number.
	 * @param value The number to divide with.
	 * @return This number. If it is read only then a copy that has had the number divided is returned.
	 */
	public RefNumber divide(int value)
	{
		return divide(castToInt(value));
	}
	
	/**
	 * Divide this number by a number.
	 * @param value The number to divide with.
	 * @return This number. If it is read only then a copy that has had the number divided is returned.
	 */
	public RefNumber divide(long value)
	{
		return divide(castToLong(value));
	}
	
	/**
	 * Divide this number by a number.
	 * @param value The number to divide with.
	 * @return This number. If it is read only then a copy that has had the number divided is returned.
	 */
	public RefNumber divide(float value)
	{
		return divide(castToFloat(value));
	}
	
	/**
	 * Divide this number by a number.
	 * @param value The number to divide with.
	 * @return This number. If it is read only then a copy that has had the number divided is returned.
	 */
	public RefNumber divide(double value)
	{
		return divide(castToDouble(value));
	}
	
	/**
	 * Divide this number by some value, if the passed object is not of any use then it is ignored.
	 * @param value The Object to divide with, examples of supported types are {@link RefByte} and {@link Byte}.
	 * @return This number. If it is read only then a copy that has had the number divided is returned.
	 */
	public RefNumber divide(Object value)
	{
		RefNumber ref = convertToRef(value);
		if(ref != null)
		{
			return divide(ref);
		}
		return this;
	}
	
	/**
	 * Divide this number by a reference number.
	 * @param number The {@link RefNumber} to divide with, if this is null than it is ignored.
	 * @return This number. If it is read only then a copy that has had the number divided is returned.
	 */
	public RefNumber divide(RefNumber number)
	{
		if(number != null)
		{
			if(this._fixed)
			{
				return this.cloneNumber().divide(number);
			}
			if(this instanceof RefByte)
			{
				((RefByte)this)._val /= RefNumber.castToByte(number)._val;
			}
			else if(this instanceof RefShort)
			{
				((RefShort)this)._val /= RefNumber.castToShort(number)._val;
			}
			else if(this instanceof RefInteger)
			{
				((RefInteger)this)._val /= RefNumber.castToInt(number)._val;
			}
			else if(this instanceof RefLong)
			{
				((RefLong)this)._val /= RefNumber.castToLong(number)._val;
			}
			else if(this instanceof RefUByte)
			{
				RefUByte rb = (RefUByte)this;
				rb._val = (short)((rb._val / RefNumber.castToUByte(number)._val) % 0x100);
			}
			else if(this instanceof RefUShort)
			{
				RefUShort rs = (RefUShort)this;
				rs._val = (rs._val / RefNumber.castToUShort(number)._val) % 0x10000;
			}
			else if(this instanceof RefUInteger)
			{
				RefUInteger ri = (RefUInteger)this;
				ri._val = (ri._val / RefNumber.castToUInt(number)._val) % 0x100000000L;
			}
			else if(this instanceof RefULong)
			{
				((RefULong)this).ulongDivide(number, false);
			}
			else if(this instanceof RefFloat)
			{
				((RefFloat)this)._val /= RefNumber.castToFloat(number)._val;
			}
			else if(this instanceof RefDouble)
			{
				((RefDouble)this)._val /= RefNumber.castToDouble(number)._val;
			}
			else
			{
				throw new NumberFormatException(StringUtilities.format_java(Resources.getString(BBXResource.UNKNOWN_TYPE_W_MESSAGE), this.getClass(), Resources.getString(BBXResource.UNKNOWN_REFNUMBER)));
			}
		}
		return this;
	}
	
	//Modulus
	
	/**
	 * Divide this number by a number and return the remainder. Thus a modulus operation.
	 * @param value The number to divide with.
	 * @return This number. If it is read only then a copy that has had the number divided and the remainder is returned.
	 */
	public RefNumber modulus(byte value)
	{
		return modulus(castToByte(value));
	}
	
	/**
	 * Divide this number by a number and return the remainder. Thus a modulus operation.
	 * @param value The number to divide with.
	 * @return This number. If it is read only then a copy that has had the number divided and the remainder is returned.
	 */
	public RefNumber modulus(short value)
	{
		return modulus(castToShort(value));
	}
	
	/**
	 * Divide this number by a number and return the remainder. Thus a modulus operation.
	 * @param value The number to divide with.
	 * @return This number. If it is read only then a copy that has had the number divided and the remainder is returned.
	 */
	public RefNumber modulus(int value)
	{
		return modulus(castToInt(value));
	}
	
	/**
	 * Divide this number by a number and return the remainder. Thus a modulus operation.
	 * @param value The number to divide with.
	 * @return This number. If it is read only then a copy that has had the number divided and the remainder is returned.
	 */
	public RefNumber modulus(long value)
	{
		return modulus(castToLong(value));
	}
	
	/**
	 * Divide this number by a number and return the remainder. Thus a modulus operation.
	 * @param value The number to divide with.
	 * @return This number. If it is read only then a copy that has had the number divided and the remainder is returned.
	 */
	public RefNumber modulus(float value)
	{
		return modulus(castToFloat(value));
	}
	
	/**
	 * Divide this number by a number and return the remainder. Thus a modulus operation.
	 * @param value The number to divide with.
	 * @return This number. If it is read only then a copy that has had the number divided and the remainder is returned.
	 */
	public RefNumber modulus(double value)
	{
		return modulus(castToDouble(value));
	}
	
	/**
	 * Divide this number by some value, then return the remainder. If the passed object is not of any use then it is ignored.
	 * @param value The Object to divide with, examples of supported types are {@link RefByte} and {@link Byte}.
	 * @return This number. If it is read only then a copy that has had the number divided and the remainder is returned.
	 */
	public RefNumber modulus(Object value)
	{
		RefNumber ref = convertToRef(value);
		if(ref != null)
		{
			return modulus(ref);
		}
		return this;
	}
	
	/**
	 * Divide this number by a reference number and return the remainder.
	 * @param number The {@link RefNumber} to divide with, if this is null than it is ignored.
	 * @return This number. If it is read only then a copy that has had the number divided and the remainder is returned.
	 */
	public RefNumber modulus(RefNumber number)
	{
		if(number != null)
		{
			if(this._fixed)
			{
				return this.cloneNumber().modulus(number);
			}
			if(this instanceof RefByte)
			{
				((RefByte)this)._val %= RefNumber.castToByte(number)._val;
			}
			else if(this instanceof RefShort)
			{
				((RefShort)this)._val %= RefNumber.castToShort(number)._val;
			}
			else if(this instanceof RefInteger)
			{
				((RefInteger)this)._val %= RefNumber.castToInt(number)._val;
			}
			else if(this instanceof RefLong)
			{
				((RefLong)this)._val %= RefNumber.castToLong(number)._val;
			}
			else if(this instanceof RefUByte)
			{
				((RefUByte)this)._val %= RefNumber.castToUByte(number)._val;
			}
			else if(this instanceof RefUShort)
			{
				((RefUShort)this)._val %= RefNumber.castToUShort(number)._val;
			}
			else if(this instanceof RefUInteger)
			{
				((RefUInteger)this)._val %= RefNumber.castToUInt(number)._val;
			}
			else if(this instanceof RefULong)
			{
				((RefULong)this).ulongDivide(number, true);
			}
			else if(this instanceof RefFloat)
			{
				((RefFloat)this)._val %= RefNumber.castToFloat(number)._val;
			}
			else if(this instanceof RefDouble)
			{
				((RefDouble)this)._val %= RefNumber.castToDouble(number)._val;
			}
			else
			{
				throw new NumberFormatException(StringUtilities.format_java(Resources.getString(BBXResource.UNKNOWN_TYPE_W_MESSAGE), this.getClass(), Resources.getString(BBXResource.UNKNOWN_REFNUMBER)));
			}
		}
		return this;
	}
	
	/* 
	 * ----------------------------------------
	 * Bit functions
	 * ----------------------------------------
	 */
	
	//AND
	
	/**
	 * Perform a binary AND (<code>&</code>) operation on this {@link RefNumber}. Can only be done using integer data types.
	 * @param value The number to AND with.
	 * @return This {@link RefNumber} after the AND operation has been performed on it.
	 */
	public RefNumber and(byte value)
	{
		return and(castToByte(value));
	}
	
	/**
	 * Perform a binary AND (<code>&</code>) operation on this {@link RefNumber}. Can only be done using integer data types.
	 * @param value The number to AND with.
	 * @return This {@link RefNumber} after the AND operation has been performed on it.
	 */
	public RefNumber and(short value)
	{
		return and(castToShort(value));
	}
	
	/**
	 * Perform a binary AND (<code>&</code>) operation on this {@link RefNumber}. Can only be done using integer data types.
	 * @param value The number to AND with.
	 * @return This {@link RefNumber} after the AND operation has been performed on it.
	 */
	public RefNumber and(int value)
	{
		return and(castToInt(value));
	}
	
	/**
	 * Perform a binary AND (<code>&</code>) operation on this {@link RefNumber}. Can only be done using integer data types.
	 * @param value The number to AND with.
	 * @return This {@link RefNumber} after the AND operation has been performed on it.
	 */
	public RefNumber and(long value)
	{
		return and(castToLong(value));
	}
	
	/**
	 * Perform a binary AND (<code>&</code>) operation on this {@link RefNumber} using an object. Can only be done 
	 * using integer data types.
	 * @param value The number to use as a mask. This number <b>cannot</b> be a floating-point number such as 
	 * {@link RefFloat} or {@link Double}. If it is a floating-point or any other unsupported type then the 
	 * operation is ignored.
	 * @return This {@link RefNumber} after the AND operation has been performed on it.
	 */
	public RefNumber and(Object value)
	{
		RefNumber ref = convertToRef(value);
		if(ref != null)
		{
			return and(ref);
		}
		return this;
	}
	
	/**
	 * Perform a binary AND (<code>&</code>) operation on this {@link RefNumber} using another {@link RefNumber}. Can 
	 * only be done using integer data types.
	 * @param number The number to use as a mask. This number <b>cannot</b> be a floating-point number such as 
	 * {@link RefFloat} or {@link RefDouble}. If it is then the operation is ignored.
	 * @return This {@link RefNumber} after the AND operation has been performed on it.
	 */
	public RefNumber and(RefNumber number)
	{
		if(number != null && !(number instanceof RefFloat || number instanceof RefDouble || 
				this instanceof RefFloat || this instanceof RefDouble))
		{
			if(this._fixed)
			{
				return this.cloneNumber().and(number);
			}
			RefULong shift = null;
			if(number instanceof RefByte || number instanceof RefShort || number instanceof RefInteger || 
					number instanceof RefLong)
			{
				long temp = 0L;
				if(number instanceof RefByte)
				{
					temp = ((RefByte)number)._val;
				}
				else if(number instanceof RefShort)
				{
					temp = ((RefShort)number)._val;
				}
				else if(number instanceof RefInteger)
				{
					temp = ((RefInteger)number)._val;
				}
				else if(number instanceof RefLong)
				{
					temp = ((RefLong)number)._val;
				}
				
				if(temp < 0L)
				{
					shift = new RefULong();
					shift._highBit = true;
					shift._val = temp;
				}
			}
			if(shift == null)
			{
				shift = castToULong(number);
			}
			
			if(this instanceof RefByte)
			{
				((RefByte)this)._val &= (byte)(shift._val & 0x00000000000000FFL);
			}
			else if(this instanceof RefShort)
			{
				((RefShort)this)._val &= (short)(shift._val & 0x000000000000FFFFL);
			}
			else if(this instanceof RefInteger)
			{
				((RefInteger)this)._val &= (int)(shift._val & 0x00000000FFFFFFFFL);
			}
			else if(this instanceof RefLong)
			{
				//Only place where this is needed
				if(shift._highBit)
				{
					shift._val |= 0x8000000000000000L;
				}
				((RefLong)this)._val &= shift._val;
			}
			else if(this instanceof RefUByte)
			{
				((RefUByte)this)._val &= (short)(shift._val & 0x00000000000000FFL);
			}
			else if(this instanceof RefUShort)
			{
				((RefUShort)this)._val &= (int)(shift._val & 0x000000000000FFFFL);
			}
			else if(this instanceof RefUInteger)
			{
				((RefUInteger)this)._val &= shift._val & 0x00000000FFFFFFFFL;
			}
			else if(this instanceof RefULong)
			{
				RefULong th = (RefULong)this;
				th._highBit = shift._highBit ? th._highBit : false;
				th._val &= shift._val;
			}
		}
		return this;
	}
	
	//OR
	
	/**
	 * Perform a binary OR (<code>|</code>) operation on this {@link RefNumber}. Can only be done using integer data types.
	 * @param value The number to OR with.
	 * @return This {@link RefNumber} after the OR operation has been performed on it.
	 */
	public RefNumber or(byte value)
	{
		return or(castToByte(value));
	}
	
	/**
	 * Perform a binary OR (<code>|</code>) operation on this {@link RefNumber}. Can only be done using integer data types.
	 * @param value The number to OR with.
	 * @return This {@link RefNumber} after the OR operation has been performed on it.
	 */
	public RefNumber or(short value)
	{
		return or(castToShort(value));
	}
	
	/**
	 * Perform a binary OR (<code>|</code>) operation on this {@link RefNumber}. Can only be done using integer data types.
	 * @param value The number to OR with.
	 * @return This {@link RefNumber} after the OR operation has been performed on it.
	 */
	public RefNumber or(int value)
	{
		return or(castToInt(value));
	}
	
	/**
	 * Perform a binary OR (<code>|</code>) operation on this {@link RefNumber}. Can only be done using integer data types.
	 * @param value The number to OR with.
	 * @return This {@link RefNumber} after the OR operation has been performed on it.
	 */
	public RefNumber or(long value)
	{
		return or(castToLong(value));
	}
	
	/**
	 * Perform a binary OR (<code>|</code>) operation on this {@link RefNumber} using an object. Can only be done 
	 * using integer data types.
	 * @param value The number to combine. This number <b>cannot</b> be a floating-point number such as 
	 * {@link RefFloat} or {@link Double}. If it is a floating-point or any other unsupported type then the 
	 * operation is ignored.
	 * @return This {@link RefNumber} after the OR operation has been performed on it.
	 */
	public RefNumber or(Object value)
	{
		RefNumber ref = convertToRef(value);
		if(ref != null)
		{
			return or(ref);
		}
		return this;
	}
	
	/**
	 * Perform a binary OR (<code>|</code>) operation on this {@link RefNumber} using another {@link RefNumber}. Can 
	 * only be done using integer data types.
	 * @param number The number to combine. This number <b>cannot</b> be a floating-point number such as 
	 * {@link RefFloat} or {@link RefDouble}. If it is then the operation is ignored.
	 * @return This {@link RefNumber} after the OR operation has been performed on it.
	 */
	public RefNumber or(RefNumber number)
	{
		if(number != null && !(number instanceof RefFloat || number instanceof RefDouble || 
				this instanceof RefFloat || this instanceof RefDouble))
		{
			if(this._fixed)
			{
				return this.cloneNumber().or(number);
			}
			RefULong shift = null;
			if(number instanceof RefByte || number instanceof RefShort || number instanceof RefInteger || 
					number instanceof RefLong)
			{
				long temp = 0L;
				if(number instanceof RefByte)
				{
					temp = ((RefByte)number)._val;
				}
				else if(number instanceof RefShort)
				{
					temp = ((RefShort)number)._val;
				}
				else if(number instanceof RefInteger)
				{
					temp = ((RefInteger)number)._val;
				}
				else if(number instanceof RefLong)
				{
					temp = ((RefLong)number)._val;
				}
				
				if(temp < 0L)
				{
					shift = new RefULong();
					shift._highBit = true;
					shift._val = temp;
				}
			}
			if(shift == null)
			{
				shift = castToULong(number);
			}
			
			if(this instanceof RefByte)
			{
				((RefByte)this)._val |= (byte)(shift._val & 0x00000000000000FFL);
			}
			else if(this instanceof RefShort)
			{
				((RefShort)this)._val |= (short)(shift._val & 0x000000000000FFFFL);
			}
			else if(this instanceof RefInteger)
			{
				((RefInteger)this)._val |= (int)(shift._val & 0x00000000FFFFFFFFL);
			}
			else if(this instanceof RefLong)
			{
				//Only place where this is needed
				if(shift._highBit)
				{
					shift._val |= 0x8000000000000000L;
				}
				((RefLong)this)._val |= shift._val;
			}
			else if(this instanceof RefUByte)
			{
				((RefUByte)this)._val |= (short)(shift._val & 0x00000000000000FFL);
			}
			else if(this instanceof RefUShort)
			{
				((RefUShort)this)._val |= (int)(shift._val & 0x000000000000FFFFL);
			}
			else if(this instanceof RefUInteger)
			{
				((RefUInteger)this)._val |= shift._val & 0x00000000FFFFFFFFL;
			}
			else if(this instanceof RefULong)
			{
				RefULong th = (RefULong)this;
				th._highBit = shift._highBit ? true : th._highBit;
				th._val |= (shift._val & 0x7FFFFFFFFFFFFFFFL);
			}
		}
		return this;
	}
	
	//XOR
	
	/**
	 * Perform a binary XOR (<code>^</code>) operation on this {@link RefNumber}. Can only be done using integer data types.
	 * @param value The number to OR with.
	 * @return This {@link RefNumber} after the XOR operation has been performed on it.
	 */
	public RefNumber xor(byte value)
	{
		return xor(castToByte(value));
	}
	
	/**
	 * Perform a binary XOR (<code>^</code>) operation on this {@link RefNumber}. Can only be done using integer data types.
	 * @param value The number to OR with.
	 * @return This {@link RefNumber} after the XOR operation has been performed on it.
	 */
	public RefNumber xor(short value)
	{
		return xor(castToShort(value));
	}
	
	/**
	 * Perform a binary XOR (<code>^</code>) operation on this {@link RefNumber}. Can only be done using integer data types.
	 * @param value The number to OR with.
	 * @return This {@link RefNumber} after the XOR operation has been performed on it.
	 */
	public RefNumber xor(int value)
	{
		return xor(castToInt(value));
	}
	
	/**
	 * Perform a binary XOR (<code>^</code>) operation on this {@link RefNumber}. Can only be done using integer data types.
	 * @param value The number to OR with.
	 * @return This {@link RefNumber} after the XOR operation has been performed on it.
	 */
	public RefNumber xor(long value)
	{
		return xor(castToLong(value));
	}
	
	/**
	 * Perform a binary XOR (<code>^</code>) operation on this {@link RefNumber} using an object. Can only be done using integer data types.
	 * @param value The number to exclusively-combine. This number <b>cannot</b> be a floating-point number such as 
	 * {@link RefFloat} or {@link Double}. If it is a floating-point or any other unsupported type then the 
	 * operation is ignored.
	 * @return This {@link RefNumber} after the XOR operation has been performed on it.
	 */
	public RefNumber xor(Object value)
	{
		RefNumber ref = convertToRef(value);
		if(ref != null)
		{
			return xor(ref);
		}
		return this;
	}
	
	/**
	 * Perform a binary XOR (<code>^</code>) operation on this {@link RefNumber} using another {@link RefNumber}. Can only be done using integer data types.
	 * @param number The number to exclusively-combine. This number <b>cannot</b> be a floating-point number such as 
	 * {@link RefFloat} or {@link RefDouble}. If it is then the operation is ignored.
	 * @return This {@link RefNumber} after the XOR operation has been performed on it.
	 */
	public RefNumber xor(RefNumber number)
	{
		if(number != null && !(number instanceof RefFloat || number instanceof RefDouble || 
				this instanceof RefFloat || this instanceof RefDouble))
		{
			if(this._fixed)
			{
				return this.cloneNumber().xor(number);
			}
			RefULong shift = null;
			if(number instanceof RefByte || number instanceof RefShort || number instanceof RefInteger || 
					number instanceof RefLong)
			{
				long temp = 0L;
				if(number instanceof RefByte)
				{
					temp = ((RefByte)number)._val;
				}
				else if(number instanceof RefShort)
				{
					temp = ((RefShort)number)._val;
				}
				else if(number instanceof RefInteger)
				{
					temp = ((RefInteger)number)._val;
				}
				else if(number instanceof RefLong)
				{
					temp = ((RefLong)number)._val;
				}
				
				if(temp < 0L)
				{
					shift = new RefULong();
					shift._highBit = true;
					shift._val = temp;
				}
			}
			if(shift == null)
			{
				shift = castToULong(number);
			}
			
			/*
			if(!shift._highBit)
			{
			*/
				if(this instanceof RefByte)
				{
					((RefByte)this)._val ^= (byte)(shift._val & 0x00000000000000FFL);
				}
				else if(this instanceof RefShort)
				{
					((RefShort)this)._val ^= (short)(shift._val & 0x000000000000FFFFL);
				}
				else if(this instanceof RefInteger)
				{
					((RefInteger)this)._val ^= (int)(shift._val & 0x00000000FFFFFFFFL);
				}
				else if(this instanceof RefLong)
				{
					//Only place where this is needed
					if(shift._highBit)
					{
						shift._val |= 0x8000000000000000L;
					}
					((RefLong)this)._val ^= shift._val;
				}
				else if(this instanceof RefUByte)
				{
					((RefUByte)this)._val ^= (short)(shift._val & 0x00000000000000FFL);
				}
				else if(this instanceof RefUShort)
				{
					((RefUShort)this)._val ^= (int)(shift._val & 0x000000000000FFFFL);
				}
				else if(this instanceof RefUInteger)
				{
					((RefUInteger)this)._val ^= shift._val & 0x00000000FFFFFFFFL;
				}
				else if(this instanceof RefULong)
				{
					RefULong th = (RefULong)this;
					th._highBit = ((!shift._highBit && th._highBit) || (shift._highBit && !th._highBit));
					th._val = (th._val ^ shift._val) & 0x7FFFFFFFFFFFFFFFL;
				}
			/*
			}
			else
			{
				if(this instanceof RefByte)
				{
					((RefByte)this)._val = 0;
				}
				else if(this instanceof RefShort)
				{
					((RefShort)this)._val = 0;
				}
				else if(this instanceof RefInteger)
				{
					((RefInteger)this)._val = 0;
				}
				else if(this instanceof RefLong)
				{
					((RefInteger)this)._val = 0;
				}
				else if(this instanceof RefUByte)
				{
					((RefUByte)this)._val = 0;
				}
				else if(this instanceof RefUShort)
				{
					((RefUShort)this)._val = 0;
				}
				else if(this instanceof RefUInteger)
				{
					((RefUInteger)this)._val = 0L;
				}
				else if(this instanceof RefULong)
				{
					RefULong th = (RefULong)this;
					th._highBit = false;
					th._val = 0L;
				}
			}
			*/
		}
		return this;
	}
	
	/**
	 * Perform a binary compliment (<code>~</code>) operation on this {@link RefNumber}. This cannot be done to 
	 * {@link RefFloat} or {@link RefDouble}.
	 * @return This {@link RefNumber} after the compliment operation has been performed on it.
	 */
	public RefNumber compliment()
	{
		if(!(this instanceof RefFloat || this instanceof RefDouble))
		{
			if(this._fixed)
			{
				return this.cloneNumber().compliment();
			}
			
			if(this instanceof RefByte)
			{
				((RefByte)this)._val = (byte)(~((RefByte)this)._val);
			}
			else if(this instanceof RefShort)
			{
				((RefShort)this)._val = (short)(~((RefShort)this)._val);
			}
			else if(this instanceof RefInteger)
			{
				((RefInteger)this)._val = ~((RefInteger)this)._val;
			}
			else if(this instanceof RefLong)
			{
				((RefLong)this)._val = ~((RefLong)this)._val;
			}
			else if(this instanceof RefUByte)
			{
				((RefUByte)this)._val = (short)((~((RefUByte)this)._val) & 0x000000FF);
			}
			else if(this instanceof RefUShort)
			{
				((RefUShort)this)._val = (~((RefUShort)this)._val) & 0x0000FFFF;
			}
			else if(this instanceof RefUInteger)
			{
				((RefUInteger)this)._val = (~((RefUInteger)this)._val) & 0x00000000FFFFFFFFL;
			}
			else if(this instanceof RefULong)
			{
				RefULong th = (RefULong)this;
				th._highBit = !th._highBit;
				th._val = (~th._val) & 0x7FFFFFFFFFFFFFFFL;
			}
		}
		return this;
	}
	
	//Bit-Shift
	
	/**
	 * Perform a bit-shift operation on this {@link RefNumber}. Can only be done using integer data types.
	 * @param value The number to bit-shift with.
	 * @param op The shift operation to occur.
	 * @return This {@link RefNumber} after the bit-shift operation has been performed on it.
	 */
	public RefNumber bitShift(byte value, int op)
	{
		return bitShift(castToByte(value), op);
	}
	
	/**
	 * Perform a bit-shift operation on this {@link RefNumber}. Can only be done using integer data types.
	 * @param value The number to bit-shift with.
	 * @param op The shift operation to occur.
	 * @return This {@link RefNumber} after the bit-shift operation has been performed on it.
	 */
	public RefNumber bitShift(short value, int op)
	{
		return bitShift(castToShort(value), op);
	}
	
	/**
	 * Perform a bit-shift operation on this {@link RefNumber}. Can only be done using integer data types.
	 * @param value The number to bit-shift with.
	 * @param op The shift operation to occur.
	 * @return This {@link RefNumber} after the bit-shift operation has been performed on it.
	 */
	public RefNumber bitShift(int value, int op)
	{
		return bitShift(castToInt(value), op);
	}
	
	/**
	 * Perform a bit-shift operation on this {@link RefNumber}. Can only be done using integer data types.
	 * @param value The number to bit-shift with.
	 * @param op The shift operation to occur.
	 * @return This {@link RefNumber} after the bit-shift operation has been performed on it.
	 */
	public RefNumber bitShift(long value, int op)
	{
		return bitShift(castToLong(value), op);
	}
	
	/**
	 * Perform a bit-shift operation on this {@link RefNumber} using an object. Can only be done using integer data types.
	 * @param value The number to bit-shift. This number <b>cannot</b> be a floating-point number such as 
	 * {@link RefFloat} or {@link Double}. If it is a floating-point or any other unsupported type then the 
	 * operation is ignored.
	 * @param op The shift operation to occur.
	 * @return This {@link RefNumber} after the bit-shift operation has been performed on it.
	 */
	public RefNumber bitShift(Object value, int op)
	{
		RefNumber ref = convertToRef(value);
		if(ref != null)
		{
			return bitShift(ref, op);
		}
		return this;
	}
	
	/**
	 * Perform a bit-shift operation on this {@link RefNumber} using another {@link RefNumber}. Can only be done using integer data types.
	 * @param number The number to bit-shift. This number <b>cannot</b> be a floating-point number such as 
	 * {@link RefFloat} or {@link RefDouble}. If it is then the operation is ignored.
	 * @param op The shift operation to occur.
	 * @return This {@link RefNumber} after the bit-shift operation has been performed on it.
	 */
	public RefNumber bitShift(RefNumber number, int op)
	{
		if(op < RefNumber.BIT_SHIFT_LEFT || op > RefNumber.BIT_SHIFT_RIGHT_LOGICAL)
		{
			//Invalid op value.
			return this;
		}
		if(op == RefNumber.BIT_SHIFT_LEFT_LOGICAL)
		{
			//Logical shift left doesn't actually do anything that shift left doesn't.
			op = RefNumber.BIT_SHIFT_LEFT;
		}
		
		if(number != null && !(number instanceof RefFloat || number instanceof RefDouble || 
				this instanceof RefFloat || this instanceof RefDouble))
		{
			if(this._fixed)
			{
				return this.cloneNumber().bitShift(number, op);
			}
			RefULong shift = null;
			if(number instanceof RefByte || number instanceof RefShort || number instanceof RefInteger || 
					number instanceof RefLong)
			{
				long temp = 0L;
				if(number instanceof RefByte)
				{
					temp = ((RefByte)number)._val;
				}
				else if(number instanceof RefShort)
				{
					temp = ((RefShort)number)._val;
				}
				else if(number instanceof RefInteger)
				{
					temp = ((RefInteger)number)._val;
				}
				else if(number instanceof RefLong)
				{
					temp = ((RefLong)number)._val;
				}
				
				if(temp < 0L)
				{
					//If the number is negative then it will return zero.
					if(this instanceof RefByte)
					{
						((RefByte)this)._val = 0;
					}
					else if(this instanceof RefShort)
					{
						((RefShort)this)._val = 0;
					}
					else if(this instanceof RefInteger)
					{
						((RefInteger)this)._val = 0;
					}
					else if(this instanceof RefLong)
					{
						((RefLong)this)._val = 0L;
					}
					else if(this instanceof RefUByte)
					{
						((RefUByte)this)._val = 0;
					}
					else if(this instanceof RefUShort)
					{
						((RefUShort)this)._val = 0;
					}
					else if(this instanceof RefUInteger)
					{
						((RefUInteger)this)._val = 0L;
					}
					else if(this instanceof RefULong)
					{
						RefULong ul = (RefULong)this;
						ul._highBit = false;
						ul._val = 0L;
					}
					return this;
				}
			}
			shift = castToULong(number);
			
			if(shift._highBit)
			{
				//A number this big will pretty much erase the number. If the number is negative then the sign will
				//replace all the current bits.
				if(this instanceof RefByte)
				{
					if(op == RefNumber.BIT_SHIFT_RIGHT && ((RefByte)this)._val < 0)
					{
						((RefByte)this)._val = -1;
					}
					else
					{
						((RefByte)this)._val = 0;
					}
				}
				else if(this instanceof RefShort)
				{
					if(op == RefNumber.BIT_SHIFT_RIGHT && ((RefShort)this)._val < 0)
					{
						((RefShort)this)._val = -1;
					}
					else
					{
						((RefShort)this)._val = 0;
					}
				}
				else if(this instanceof RefInteger)
				{
					if(op == RefNumber.BIT_SHIFT_RIGHT && ((RefInteger)this)._val < 0)
					{
						((RefInteger)this)._val = -1;
					}
					else
					{
						((RefInteger)this)._val = 0;
					}
				}
				else if(this instanceof RefLong)
				{
					if(op == RefNumber.BIT_SHIFT_RIGHT && ((RefLong)this)._val < 0L)
					{
						((RefLong)this)._val = -1L;
					}
					else
					{
						((RefLong)this)._val = 0L;
					}
				}
				else if(this instanceof RefUByte)
				{
					((RefUByte)this)._val = 0;
				}
				else if(this instanceof RefUShort)
				{
					((RefUShort)this)._val = 0;
				}
				else if(this instanceof RefUInteger)
				{
					((RefUInteger)this)._val = 0L;
				}
				else if(this instanceof RefULong)
				{
					RefULong ul = (RefULong)this;
					ul._highBit = false;
					ul._val = 0L;
				}
				return this;
			}
			
			if(this instanceof RefByte)
			{
				RefByte rb = (RefByte)this;
				switch(op)
				{
					case RefNumber.BIT_SHIFT_LEFT:
					//case RefNumber.BIT_SHIFT_LEFT_LOGICAL:
						rb._val <<= shift._val;
						break;
					case RefNumber.BIT_SHIFT_RIGHT:
						rb._val >>= shift._val;
						break;
					case RefNumber.BIT_SHIFT_RIGHT_LOGICAL:
						rb._val >>>= shift._val;
						break;
				}
			}
			else if(this instanceof RefShort)
			{
				RefShort rs = (RefShort)this;
				switch(op)
				{
					case RefNumber.BIT_SHIFT_LEFT:
					//case RefNumber.BIT_SHIFT_LEFT_LOGICAL:
						rs._val <<= shift._val;
						break;
					case RefNumber.BIT_SHIFT_RIGHT:
						rs._val >>= shift._val;
						break;
					case RefNumber.BIT_SHIFT_RIGHT_LOGICAL:
						rs._val >>>= shift._val;
						break;
				}
			}
			else if(this instanceof RefInteger)
			{
				RefInteger ri = (RefInteger)this;
				switch(op)
				{
					case RefNumber.BIT_SHIFT_LEFT:
					//case RefNumber.BIT_SHIFT_LEFT_LOGICAL:
						ri._val <<= shift._val;
						break;
					case RefNumber.BIT_SHIFT_RIGHT:
						ri._val >>= shift._val;
						break;
					case RefNumber.BIT_SHIFT_RIGHT_LOGICAL:
						ri._val >>>= shift._val;
						break;
				}
			}
			else if(this instanceof RefLong)
			{
				RefLong rl = (RefLong)this;
				switch(op)
				{
					case RefNumber.BIT_SHIFT_LEFT:
					//case RefNumber.BIT_SHIFT_LEFT_LOGICAL:
						rl._val <<= shift._val;
						break;
					case RefNumber.BIT_SHIFT_RIGHT:
						rl._val >>= shift._val;
						break;
					case RefNumber.BIT_SHIFT_RIGHT_LOGICAL:
						rl._val >>>= shift._val;
						break;
				}
			}
			else if(this instanceof RefUByte)
			{
				RefUByte ub = (RefUByte)this;
				switch(op)
				{
					case RefNumber.BIT_SHIFT_LEFT:
					//case RefNumber.BIT_SHIFT_LEFT_LOGICAL:
						ub._val <<= shift._val;
						ub._val &= 0x00FF;
						break;
					case RefNumber.BIT_SHIFT_RIGHT:
					case RefNumber.BIT_SHIFT_RIGHT_LOGICAL:
						ub._val >>= shift._val;
						break;
				}
			}
			else if(this instanceof RefUShort)
			{
				RefUShort us = (RefUShort)this;
				switch(op)
				{
					case RefNumber.BIT_SHIFT_LEFT:
					//case RefNumber.BIT_SHIFT_LEFT_LOGICAL:
						us._val <<= shift._val;
						us._val &= 0x0000FFFF;
						break;
					case RefNumber.BIT_SHIFT_RIGHT:
					case RefNumber.BIT_SHIFT_RIGHT_LOGICAL:
						us._val >>= shift._val;
						break;
				}
			}
			else if(this instanceof RefUInteger)
			{
				RefUInteger ui = (RefUInteger)this;
				switch(op)
				{
					case RefNumber.BIT_SHIFT_LEFT:
					//case RefNumber.BIT_SHIFT_LEFT_LOGICAL:
						ui._val <<= shift._val;
						ui._val &= 0x00000000FFFFFFFFL;
						break;
					case RefNumber.BIT_SHIFT_RIGHT:
					case RefNumber.BIT_SHIFT_RIGHT_LOGICAL:
						ui._val >>= shift._val;
						break;
				}
			}
			else if(this instanceof RefULong)
			{
				RefULong ul = (RefULong)this;
				switch(op)
				{
					case RefNumber.BIT_SHIFT_LEFT:
					//case RefNumber.BIT_SHIFT_LEFT_LOGICAL:
						long ls = ul._val | (ul._highBit ? 0x8000000000000000L : 0L);
						ls <<= shift._val;
						ul._highBit = (ls & 0x8000000000000000L) == 0x8000000000000000L;
						ul._val = ls & 0x7FFFFFFFFFFFFFFFL;
						break;
					case RefNumber.BIT_SHIFT_RIGHT:
					case RefNumber.BIT_SHIFT_RIGHT_LOGICAL:
						ul._val >>= shift._val;
						if(ul._highBit)
						{
							long mod = 0x8000000000000000L;
							if(op == RefNumber.BIT_SHIFT_RIGHT)
							{
								mod >>= shift._val;
							}
							else
							{
								mod >>>= shift._val;
							}
							ul._val = ul._val | mod;
						}
						//Normalize the value (otherwise if ul._highBit = true, and shift._val == 0, the number will be invalid)
						ul._highBit = (ul._val & 0x8000000000000000L) == 0x8000000000000000L;
						ul._val &= 0x7FFFFFFFFFFFFFFFL;
						break;
				}
			}
		}
		return this;
	}
	
	/* 
	 * ----------------------------------------
	 * Comparison functions
	 * ----------------------------------------
	 */
	
	/**
	 * Returns true if and only if the argument is of the same value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is of the same value as this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean equals(byte value)
	{
		return equals(castToByte(value));
	}
	
	/**
	 * Returns true if and only if the argument is of the same value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is of the same value as this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean equals(short value)
	{
		return equals(castToShort(value));
	}
	
	/**
	 * Returns true if and only if the argument is of the same value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is of the same value as this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean equals(int value)
	{
		return equals(castToInt(value));
	}
	
	/**
	 * Returns true if and only if the argument is of the same value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is of the same value as this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean equals(long value)
	{
		return equals(castToLong(value));
	}
	
	/**
	 * Returns true if and only if the argument is of the same value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is of the same value as this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean equals(float value)
	{
		return equals(castToFloat(value));
	}
	
	/**
	 * Returns true if and only if the argument is of the same value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is of the same value as this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean equals(double value)
	{
		return equals(castToDouble(value));
	}
	
	/**
	 * Returns true if and only if the argument is not null and is of the same value.
	 * @param obj The object to compare.
	 * @return <code>true</code> if the object is of the same value as this {@link RefNumber}, <code>false</code> if 
	 * <i>obj</i> is <code>null</code>, or not a supported number.
	 */
	public boolean equals(Object obj)
	{
		return equals(convertToRef(obj));
	}
	
	/**
	 * Returns true if and only if the argument is not null and is of the same value.
	 * @param obj The {@link RefNumber} to compare.
	 * @return <code>true</code> if the object is of the same value as this {@link RefNumber}, <code>false</code> if 
	 * <i>obj</i> is <code>null</code>, or not a supported number.
	 */
	public boolean equals(RefNumber obj)
	{
		if(obj == null)
		{
			return false;
		}
		
		//Handle floating point numbers a little differently
		if(obj instanceof RefFloat || obj instanceof RefDouble)
		{
			RefDouble dob = castToDouble(obj);
			if(this instanceof RefFloat || this instanceof RefDouble)
			{
				//Easy
				RefDouble dThis = castToDouble(this);
				return dob._val == dThis._val;
			}
			else
			{
				//We cast this value so we don't end up with: 0.6 == 1? cast 0.6 = 1. 1 == 1
				RefULong ulThis = castToULong(this);
				double dThis;
				if(this instanceof RefUByte || this instanceof RefUShort || this instanceof RefUInteger || 
						this instanceof RefULong)
				{
					dThis = ulThis._highBit ? INT_HIGH_AS_DOUBLE : 0.0;
					dThis += ulThis._val;
				}
				else
				{
					dThis = ulThis.longValue();
				}
				return dob._val == dThis;
			}
		}
		else if(this instanceof RefFloat || this instanceof RefDouble)
		{
			RefDouble dob = castToDouble(this);
			RefULong ulObj = castToULong(obj);
			double dObj;
			if(obj instanceof RefUByte || obj instanceof RefUShort || obj instanceof RefUInteger || 
					obj instanceof RefULong)
			{
				dObj = ulObj._highBit ? INT_HIGH_AS_DOUBLE : 0.0;
				dObj += ulObj._val;
			}
			else
			{
				dObj = ulObj.longValue();
			}
			return dob._val == dObj;
		}
		else
		{
			//Convert both to ulongs, this way if the number is negative or positive it will be treated in a similar manner.
			RefULong ulObj = castToULong(obj);
			RefULong ulThis = castToULong(this);
			if(ulObj._highBit == ulThis._highBit)
			{
				return ulObj._val == ulThis._val;
			}
			//return ulObj.longValue() == ulThis.longValue(); //Could use this but it is slower
		}
		return false;
	}
	
	/**
	 * Returns true if and only if the argument is not equal to this {@link RefNumber}.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is not equal to this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean notEquals(byte value)
	{
		return notEquals(castToByte(value));
	}
	
	/**
	 * Returns true if and only if the argument is not equal to this {@link RefNumber}.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is not equal to this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean notEquals(short value)
	{
		return notEquals(castToShort(value));
	}
	
	/**
	 * Returns true if and only if the argument is not equal to this {@link RefNumber}.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is not equal to this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean notEquals(int value)
	{
		return notEquals(castToInt(value));
	}
	
	/**
	 * Returns true if and only if the argument is not equal to this {@link RefNumber}.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is not equal to this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean notEquals(long value)
	{
		return notEquals(castToLong(value));
	}
	
	/**
	 * Returns true if and only if the argument is not equal to this {@link RefNumber}.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is not equal to this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean notEquals(float value)
	{
		return notEquals(castToFloat(value));
	}
	
	/**
	 * Returns true if and only if the argument is not equal to this {@link RefNumber}.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is not equal to this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean notEquals(double value)
	{
		return notEquals(castToDouble(value));
	}
	
	/**
	 * Returns true if and only if the argument is not equal to this {@link RefNumber}.
	 * @param obj The object to compare.
	 * @return <code>true</code> if the object is not equal to this {@link RefNumber}, <code>false</code> if otherwise.
	 */
	public boolean notEquals(Object obj)
	{
		return !equals(obj);
	}
	
	/**
	 * Returns true if and only if the argument is not equal to this {@link RefNumber}.
	 * @param obj The {@link RefNumber} to compare.
	 * @return <code>true</code> if the object is not equal to this {@link RefNumber}, <code>false</code> if otherwise.
	 */
	public boolean notEquals(RefNumber obj)
	{
		return !equals(obj);
	}
	
	/**
	 * Returns true if and only if the argument is less than this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is less than the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean lessThan(byte value)
	{
		return lessThan(castToByte(value));
	}
	
	/**
	 * Returns true if and only if the argument is less than this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is less than the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean lessThan(short value)
	{
		return lessThan(castToShort(value));
	}
	
	/**
	 * Returns true if and only if the argument is less than this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is less than the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean lessThan(int value)
	{
		return lessThan(castToInt(value));
	}
	
	/**
	 * Returns true if and only if the argument is less than this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is less than the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean lessThan(long value)
	{
		return lessThan(castToLong(value));
	}
	
	/**
	 * Returns true if and only if the argument is less than this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is less than the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean lessThan(float value)
	{
		return lessThan(castToFloat(value));
	}
	
	/**
	 * Returns true if and only if the argument is less than this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is less than the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean lessThan(double value)
	{
		return lessThan(castToDouble(value));
	}
	
	/**
	 * Returns true if and only if the argument is not null and is less than this number's value.
	 * @param obj The object to compare.
	 * @return <code>true</code> if the object is less than the value of this {@link RefNumber}, <code>false</code> if 
	 * <i>obj</i> is <code>null</code>, or not a supported number.
	 */
	public boolean lessThan(Object obj)
	{
		return lessThan(convertToRef(obj));
	}
	
	/**
	 * Returns true if and only if the argument is not null and is less than this number's value.
	 * @param obj The {@link RefNumber} to compare.
	 * @return <code>true</code> if the object is less than the value of this {@link RefNumber}, <code>false</code> if 
	 * <i>obj</i> is <code>null</code>, or not a supported number.
	 */
	public boolean lessThan(RefNumber obj)
	{
		if(obj == null)
		{
			return false;
		}
		
		//Handle floating point numbers a little differently
		if(obj instanceof RefFloat || obj instanceof RefDouble)
		{
			RefDouble dob = castToDouble(obj);
			if(this instanceof RefFloat || this instanceof RefDouble)
			{
				//Easy
				RefDouble dThis = castToDouble(this);
				return dThis._val < dob._val;
			}
			else
			{
				//We cast this value so we don't end up with: 0.6 < 1? cast 0.6 = 1. 1 < 1
				RefULong ulThis = castToULong(this);
				double dThis;
				if(this instanceof RefUByte || this instanceof RefUShort || this instanceof RefUInteger || 
						this instanceof RefULong)
				{
					dThis = ulThis._highBit ? INT_HIGH_AS_DOUBLE : 0.0;
					dThis += ulThis._val;
				}
				else
				{
					dThis = ulThis.longValue();
				}
				return dThis < dob._val;
			}
		}
		else if(this instanceof RefFloat || this instanceof RefDouble)
		{
			RefDouble dob = castToDouble(this);
			RefULong ulObj = castToULong(obj);
			double dObj;
			if(obj instanceof RefUByte || obj instanceof RefUShort || obj instanceof RefUInteger || 
					obj instanceof RefULong)
			{
				dObj = ulObj._highBit ? INT_HIGH_AS_DOUBLE : 0.0;
				dObj += ulObj._val;
			}
			else
			{
				dObj = ulObj.longValue();
			}
			return dob._val < dObj;
		}
		else
		{
			if(obj instanceof RefUByte || obj instanceof RefUShort || obj instanceof RefUInteger || 
					obj instanceof RefULong)
			{
				if(this instanceof RefUByte || this instanceof RefUShort || this instanceof RefUInteger || 
						this instanceof RefULong)
				{
					//Both values are unsigned
					RefULong ulObj = castToULong(obj);
					RefULong ulThis = castToULong(this);
					if((ulObj._highBit && ulThis._highBit) || (!ulObj._highBit && !ulThis._highBit))
					{
						return ulThis._val < ulObj._val;
					}
					else if(ulObj._highBit && !ulThis._highBit)
					{
						return true;
					}
				}
				else
				{
					//This is signed, obj is unsigned
					RefLong rl = castToLong(this);
					if(rl._val >= 0L)
					{
						RefULong ul = castToULong(obj);
						if(ul._highBit)
						{
							return true;
						}
						else
						{
							return rl._val < ul._val;
						}
					}
					else
					{
						return true;
					}
				}
			}
			else
			{
				if(this instanceof RefUByte || this instanceof RefUShort || this instanceof RefUInteger || 
						this instanceof RefULong)
				{
					//This is unsigned, obj is signed
					RefLong rl = castToLong(obj);
					if(rl._val >= 0L)
					{
						RefULong ul = castToULong(this);
						if(!ul._highBit)
						{
							return ul._val < rl._val;
						}
					}
				}
				else
				{
					//Both values are signed
					RefLong rlObj = castToLong(obj);
					RefLong rlThis = castToLong(this);
					return rlThis._val < rlObj._val;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns true if and only if the argument is greater than this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is greater than the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean greaterThan(byte value)
	{
		return greaterThan(castToByte(value));
	}
	
	/**
	 * Returns true if and only if the argument is greater than this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is greater than the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean greaterThan(short value)
	{
		return greaterThan(castToShort(value));
	}
	
	/**
	 * Returns true if and only if the argument is greater than this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is greater than the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean greaterThan(int value)
	{
		return greaterThan(castToInt(value));
	}
	
	/**
	 * Returns true if and only if the argument is greater than this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is greater than the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean greaterThan(long value)
	{
		return greaterThan(castToLong(value));
	}
	
	/**
	 * Returns true if and only if the argument is greater than this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is greater than the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean greaterThan(float value)
	{
		return greaterThan(castToFloat(value));
	}
	
	/**
	 * Returns true if and only if the argument is greater than this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is greater than the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean greaterThan(double value)
	{
		return greaterThan(castToDouble(value));
	}
	
	/**
	 * Returns true if and only if the argument is not null and is greater than this number's value.
	 * @param obj The object to compare.
	 * @return <code>true</code> if the object is greater than the value of this {@link RefNumber}, <code>false</code> if 
	 * <i>obj</i> is <code>null</code>, or not a supported number.
	 */
	public boolean greaterThan(Object obj)
	{
		return greaterThan(convertToRef(obj));
	}
	
	/**
	 * Returns true if and only if the argument is not null and is greater than this number's value.
	 * @param obj The {@link RefNumber} to compare.
	 * @return <code>true</code> if the object is greater than the value of this {@link RefNumber}, <code>false</code> if 
	 * <i>obj</i> is <code>null</code>, or not a supported number.
	 */
	public boolean greaterThan(RefNumber obj)
	{
		if(obj == null)
		{
			return false;
		}
		
		//Handle floating point numbers a little differently
		if(obj instanceof RefFloat || obj instanceof RefDouble)
		{
			RefDouble dob = castToDouble(obj);
			if(this instanceof RefFloat || this instanceof RefDouble)
			{
				//Easy
				RefDouble dThis = castToDouble(this);
				return dThis._val > dob._val;
			}
			else
			{
				//We cast this value so we don't end up with: 0.6 > 1? cast 0.6 = 1. 1 > 1
				RefULong ulThis = castToULong(this);
				double dThis;
				if(this instanceof RefUByte || this instanceof RefUShort || this instanceof RefUInteger || 
						this instanceof RefULong)
				{
					dThis = ulThis._highBit ? INT_HIGH_AS_DOUBLE : 0.0;
					dThis += ulThis._val;
				}
				else
				{
					dThis = ulThis.longValue();
				}
				return dThis > dob._val;
			}
		}
		else if(this instanceof RefFloat || this instanceof RefDouble)
		{
			RefDouble dob = castToDouble(this);
			RefULong ulObj = castToULong(obj);
			double dObj;
			if(obj instanceof RefUByte || obj instanceof RefUShort || obj instanceof RefUInteger || 
					obj instanceof RefULong)
			{
				dObj = ulObj._highBit ? INT_HIGH_AS_DOUBLE : 0.0;
				dObj += ulObj._val;
			}
			else
			{
				dObj = ulObj.longValue();
			}
			return dob._val > dObj;
		}
		else
		{
			if(obj instanceof RefUByte || obj instanceof RefUShort || obj instanceof RefUInteger || 
					obj instanceof RefULong)
			{
				if(this instanceof RefUByte || this instanceof RefUShort || this instanceof RefUInteger || 
						this instanceof RefULong)
				{
					//Both values are unsigned
					RefULong ulObj = castToULong(obj);
					RefULong ulThis = castToULong(this);
					if((ulObj._highBit && ulThis._highBit) || (!ulObj._highBit && !ulThis._highBit))
					{
						return ulThis._val > ulObj._val;
					}
					else if(!ulObj._highBit && ulThis._highBit)
					{
						return true;
					}
				}
				else
				{
					//This is signed, obj is unsigned
					RefLong rl = castToLong(this);
					if(rl._val >= 0L)
					{
						RefULong ul = castToULong(obj);
						if(!ul._highBit)
						{
							return rl._val > ul._val;
						}
					}
				}
			}
			else
			{
				if(this instanceof RefUByte || this instanceof RefUShort || this instanceof RefUInteger || 
						this instanceof RefULong)
				{
					//This is unsigned, obj is signed
					RefLong rl = castToLong(obj);
					if(rl._val >= 0L)
					{
						RefULong ul = castToULong(this);
						if(ul._highBit)
						{
							return true;
						}
						else
						{
							return ul._val > rl._val;
						}
					}
					else
					{
						return true;
					}
				}
				else
				{
					//Both values are signed
					RefLong rlObj = castToLong(obj);
					RefLong rlThis = castToLong(this);
					return rlThis._val > rlObj._val;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns true if and only if the argument is less than or equal to this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is less than or equal to the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean lessThanOrEqual(byte value)
	{
		return lessThanOrEqual(castToByte(value));
	}
	
	/**
	 * Returns true if and only if the argument is less than or equal to this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is less than or equal to the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean lessThanOrEqual(short value)
	{
		return lessThanOrEqual(castToShort(value));
	}
	
	/**
	 * Returns true if and only if the argument is less than or equal to this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is less than or equal to the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean lessThanOrEqual(int value)
	{
		return lessThanOrEqual(castToInt(value));
	}
	
	/**
	 * Returns true if and only if the argument is less than or equal to this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is less than or equal to the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean lessThanOrEqual(long value)
	{
		return lessThanOrEqual(castToLong(value));
	}
	
	/**
	 * Returns true if and only if the argument is less than or equal to this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is less than or equal to the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean lessThanOrEqual(float value)
	{
		return lessThanOrEqual(castToFloat(value));
	}
	
	/**
	 * Returns true if and only if the argument is less than or equal to this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is less than or equal to the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean lessThanOrEqual(double value)
	{
		return lessThanOrEqual(castToDouble(value));
	}
	
	/**
	 * Returns true if and only if the argument is not null and is less than or equal to this number's value.
	 * @param obj The object to compare.
	 * @return <code>true</code> if the object is less than or equal to the value of this {@link RefNumber}, <code>false</code> if 
	 * <i>obj</i> is <code>null</code>, or not a supported number.
	 */
	public boolean lessThanOrEqual(Object obj)
	{
		return lessThanOrEqual(convertToRef(obj));
	}
	
	/**
	 * Returns true if and only if the argument is not null and is less than or equal to this number's value.
	 * @param obj The {@link RefNumber} to compare.
	 * @return <code>true</code> if the object is of less than or equal to the value of this {@link RefNumber}, <code>false</code> if 
	 * <i>obj</i> is <code>null</code>, or not a supported number.
	 */
	public boolean lessThanOrEqual(RefNumber obj)
	{
		if(!equals(obj))
		{
			return lessThan(obj);
		}
		return false;
	}
	
	/**
	 * Returns true if and only if the argument is greater than or equal to this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is greater than or equal to the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean greaterThanOrEqual(byte value)
	{
		return greaterThanOrEqual(castToByte(value));
	}
	
	/**
	 * Returns true if and only if the argument is greater than or equal to this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is greater than or equal to the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean greaterThanOrEqual(short value)
	{
		return greaterThanOrEqual(castToShort(value));
	}
	
	/**
	 * Returns true if and only if the argument is greater than or equal to this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is greater than or equal to the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean greaterThanOrEqual(int value)
	{
		return greaterThanOrEqual(castToInt(value));
	}
	
	/**
	 * Returns true if and only if the argument is greater than or equal to this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is greater than or equal to the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean greaterThanOrEqual(long value)
	{
		return greaterThanOrEqual(castToLong(value));
	}
	
	/**
	 * Returns true if and only if the argument is greater than or equal to this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is greater than or equal to the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean greaterThanOrEqual(float value)
	{
		return greaterThanOrEqual(castToFloat(value));
	}
	
	/**
	 * Returns true if and only if the argument is greater than or equal to this number's value.
	 * @param obj The primitive to compare.
	 * @return <code>true</code> if the primitive is greater than or equal to the value of this {@link RefNumber}, <code>false</code> if 
	 * otherwise.
	 */
	public boolean greaterThanOrEqual(double value)
	{
		return greaterThanOrEqual(castToDouble(value));
	}
	
	/**
	 * Returns true if and only if the argument is not null and is greater than or equal to this number's value.
	 * @param obj The object to compare.
	 * @return <code>true</code> if the object is greater than or equal to the value of this {@link RefNumber}, <code>false</code> if 
	 * <i>obj</i> is <code>null</code>, or not a supported number.
	 */
	public boolean greaterThanOrEqual(Object obj)
	{
		return greaterThanOrEqual(convertToRef(obj));
	}
	
	/**
	 * Returns true if and only if the argument is not null and is greater than or equal to this number's value.
	 * @param obj The {@link RefNumber} to compare.
	 * @return <code>true</code> if the object is greater than or equal to the value of this {@link RefNumber}, <code>false</code> if 
	 * <i>obj</i> is <code>null</code>, or not a supported number.
	 */
	public boolean greaterThanOrEqual(RefNumber obj)
	{
		if(!equals(obj))
		{
			return greaterThan(obj);
		}
		return false;
	}
	
	/* 
	 * ----------------------------------------
	 * Cast functions
	 * ----------------------------------------
	 */
	
	//BYTE
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefByte castToByte(byte value)
	{
		return new RefByte(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefByte castToByte(short value)
	{
		return new RefByte((byte)value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefByte castToByte(int value)
	{
		return new RefByte((byte)value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefByte castToByte(long value)
	{
		return new RefByte((byte)value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefByte castToByte(float value)
	{
		return castToByte((double)value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefByte castToByte(double value)
	{
		return castToByte(castToInt(value));
	}
	
	/**
	 * Convert a Object to a {@link RefNumber} or specified type.
	 * @param value The Object to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public static RefByte castToByte(Object value)
	{
		if(value == null)
		{
			return null;
		}
		
		if(value instanceof Byte)
		{
			return castToByte(((Byte)value).byteValue());
		}
		else if(value instanceof Short)
		{
			return castToByte(((Short)value).shortValue());
		}
		else if(value instanceof Integer)
		{
			return castToByte(((Integer)value).intValue());
		}
		else if(value instanceof Long)
		{
			return castToByte(((Long)value).longValue());
		}
		else if(value instanceof Float)
		{
			return castToByte(((Float)value).floatValue());
		}
		else if(value instanceof Double)
		{
			return castToByte(((Double)value).doubleValue());
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Convert this {@link RefNumber} to a {@link RefNumber} of specified type.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public RefByte castToByte()
	{
		return RefNumber.castToByte(this);
	}
	
	/**
	 * Convert a Object to a {@link RefNumber} or specified type.
	 * @param value The Object to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public static RefByte castToByte(RefNumber value)
	{
		if(value == null)
		{
			return null;
		}
		
		if(value instanceof RefByte)
		{
			RefByte rb = (RefByte)value;
			return rb._fixed ? rb.clone() : rb;
		}
		else if(value instanceof RefShort)
		{
			return castToByte(((RefShort)value)._val);
		}
		else if(value instanceof RefInteger)
		{
			return castToByte(((RefInteger)value)._val);
		}
		else if(value instanceof RefLong)
		{
			return castToByte(((RefLong)value)._val);
		}
		else if(value instanceof RefFloat)
		{
			return castToByte(((RefFloat)value)._val);
		}
		else if(value instanceof RefDouble)
		{
			return castToByte(((RefDouble)value)._val);
		}
		else if(value instanceof RefUByte)
		{
			return castToByte(((RefUByte)value)._val);
		}
		else if(value instanceof RefUShort)
		{
			return castToByte(((RefUShort)value)._val);
		}
		else if(value instanceof RefUInteger)
		{
			return castToByte(((RefUInteger)value)._val);
		}
		else if(value instanceof RefULong)
		{
			return castToByte(((RefULong)value).longValue());
		}
		else
		{
			return null;
		}
	}
	
	//SHORT
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefShort castToShort(byte value)
	{
		return new RefShort(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefShort castToShort(short value)
	{
		return new RefShort(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefShort castToShort(int value)
	{
		return new RefShort((short)value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefShort castToShort(long value)
	{
		return new RefShort((short)value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefShort castToShort(float value)
	{
		return castToShort((double)value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefShort castToShort(double value)
	{
		return castToShort(castToInt(value));
	}
	
	/**
	 * Convert a Object to a {@link RefNumber} or specified type.
	 * @param value The Object to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public static RefShort castToShort(Object value)
	{
		if(value == null)
		{
			return null;
		}
		
		if(value instanceof Byte)
		{
			return castToShort(((Byte)value).byteValue());
		}
		else if(value instanceof Short)
		{
			return castToShort(((Short)value).shortValue());
		}
		else if(value instanceof Integer)
		{
			return castToShort(((Integer)value).intValue());
		}
		else if(value instanceof Long)
		{
			return castToShort(((Long)value).longValue());
		}
		else if(value instanceof Float)
		{
			return castToShort(((Float)value).floatValue());
		}
		else if(value instanceof Double)
		{
			return castToShort(((Double)value).doubleValue());
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Convert this {@link RefNumber} to a {@link RefNumber} of specified type.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public RefShort castToShort()
	{
		return RefNumber.castToShort(this);
	}
	
	/**
	 * Convert a Object to a {@link RefNumber} or specified type.
	 * @param value The Object to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public static RefShort castToShort(RefNumber value)
	{
		if(value == null)
		{
			return null;
		}
		
		if(value instanceof RefByte)
		{
			return castToShort(((RefByte)value)._val);
		}
		else if(value instanceof RefShort)
		{
			RefShort rs = (RefShort)value;
			return rs._fixed ? rs.clone() : rs;
		}
		else if(value instanceof RefInteger)
		{
			return castToShort(((RefInteger)value)._val);
		}
		else if(value instanceof RefLong)
		{
			return castToShort(((RefLong)value)._val);
		}
		else if(value instanceof RefFloat)
		{
			return castToShort(((RefFloat)value)._val);
		}
		else if(value instanceof RefDouble)
		{
			return castToShort(((RefDouble)value)._val);
		}
		else if(value instanceof RefUByte)
		{
			return castToShort(((RefUByte)value)._val);
		}
		else if(value instanceof RefUShort)
		{
			return castToShort(((RefUShort)value)._val);
		}
		else if(value instanceof RefUInteger)
		{
			return castToShort(((RefUInteger)value)._val);
		}
		else if(value instanceof RefULong)
		{
			return castToShort(((RefULong)value).longValue());
		}
		else
		{
			return null;
		}
	}
	
	//INT
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefInteger castToInt(byte value)
	{
		return new RefInteger(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefInteger castToInt(short value)
	{
		return new RefInteger(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefInteger castToInt(int value)
	{
		return new RefInteger(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefInteger castToInt(long value)
	{
		return new RefInteger((int)value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefInteger castToInt(float value)
	{
		return castToInt((double)value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefInteger castToInt(double value)
	{
		value = Math.min(Math.max(INT_CAST_DOUBLE_MIN, value), INT_CAST_DOUBLE_MAX);
		int nRet = (int)value;
		double nVal = value - nRet;
		if(value >= 0.0)
		{
            if ((nVal > 0.5) || ((nVal == 0.5) && ((nRet & 1) != 0)))
            {
            	nRet++;
            }
		}
		else
		{
	        if ((nVal < -0.5) || ((nVal == -0.5) && ((nRet & 1) != 0)))
	        {
	        	nRet--;
	        }
		}
		return new RefInteger(nRet);
	}
	
	/**
	 * Convert a Object to a {@link RefNumber} or specified type.
	 * @param value The Object to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public static RefInteger castToInt(Object value)
	{
		if(value == null)
		{
			return null;
		}
		
		if(value instanceof Byte)
		{
			return castToInt(((Byte)value).byteValue());
		}
		else if(value instanceof Short)
		{
			return castToInt(((Short)value).shortValue());
		}
		else if(value instanceof Integer)
		{
			return castToInt(((Integer)value).intValue());
		}
		else if(value instanceof Long)
		{
			return castToInt(((Long)value).longValue());
		}
		else if(value instanceof Float)
		{
			return castToInt(((Float)value).floatValue());
		}
		else if(value instanceof Double)
		{
			return castToInt(((Double)value).doubleValue());
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Convert this {@link RefNumber} to a {@link RefNumber} of specified type.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public RefInteger castToInt()
	{
		return RefNumber.castToInt(this);
	}
	
	/**
	 * Convert a Object to a {@link RefNumber} or specified type.
	 * @param value The Object to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public static RefInteger castToInt(RefNumber value)
	{
		if(value == null)
		{
			return null;
		}
		
		if(value instanceof RefByte)
		{
			return castToInt(((RefByte)value)._val);
		}
		else if(value instanceof RefShort)
		{
			return castToInt(((RefShort)value)._val);
		}
		else if(value instanceof RefInteger)
		{
			RefInteger ri = (RefInteger)value;
			return ri._fixed ? ri.clone() : ri;
		}
		else if(value instanceof RefLong)
		{
			return castToInt(((RefLong)value)._val);
		}
		else if(value instanceof RefFloat)
		{
			return castToInt(((RefFloat)value)._val);
		}
		else if(value instanceof RefDouble)
		{
			return castToInt(((RefDouble)value)._val);
		}
		else if(value instanceof RefUByte)
		{
			return castToInt(((RefUByte)value)._val);
		}
		else if(value instanceof RefUShort)
		{
			return castToInt(((RefUShort)value)._val);
		}
		else if(value instanceof RefUInteger)
		{
			return castToInt(((RefInteger)value)._val);
		}
		else if(value instanceof RefULong)
		{
			return castToInt(((RefULong)value).longValue());
		}
		else
		{
			return null;
		}
	}
	
	//LONG
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefLong castToLong(byte value)
	{
		return new RefLong(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefLong castToLong(short value)
	{
		return new RefLong(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefLong castToLong(int value)
	{
		return new RefLong(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefLong castToLong(long value)
	{
		return new RefLong(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefLong castToLong(float value)
	{
		return castToLong((double)value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefLong castToLong(double value)
	{
		return new RefLong(MathUtilities.round(value));
	}
	
	/**
	 * Convert a Object to a {@link RefNumber} or specified type.
	 * @param value The Object to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public static RefLong castToLong(Object value)
	{
		if(value == null)
		{
			return null;
		}
		
		if(value instanceof Byte)
		{
			return castToLong(((Byte)value).byteValue());
		}
		else if(value instanceof Short)
		{
			return castToLong(((Short)value).shortValue());
		}
		else if(value instanceof Integer)
		{
			return castToLong(((Integer)value).intValue());
		}
		else if(value instanceof Long)
		{
			return castToLong(((Long)value).longValue());
		}
		else if(value instanceof Float)
		{
			return castToLong(((Float)value).floatValue());
		}
		else if(value instanceof Double)
		{
			return castToLong(((Double)value).doubleValue());
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Convert this {@link RefNumber} to a {@link RefNumber} of specified type.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public RefLong castToLong()
	{
		return RefNumber.castToLong(this);
	}
	
	/**
	 * Convert a Object to a {@link RefNumber} or specified type.
	 * @param value The Object to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public static RefLong castToLong(RefNumber value)
	{
		if(value == null)
		{
			return null;
		}
		
		if(value instanceof RefByte)
		{
			return castToLong(((RefByte)value)._val);
		}
		else if(value instanceof RefShort)
		{
			return castToLong(((RefShort)value)._val);
		}
		else if(value instanceof RefInteger)
		{
			return castToLong(((RefInteger)value)._val);
		}
		else if(value instanceof RefLong)
		{
			RefLong rl = (RefLong)value;
			return rl._fixed ? rl.clone() : rl;
		}
		else if(value instanceof RefFloat)
		{
			return castToLong(((RefFloat)value)._val);
		}
		else if(value instanceof RefDouble)
		{
			return castToLong(((RefDouble)value)._val);
		}
		else if(value instanceof RefUByte)
		{
			return castToLong(((RefUByte)value)._val);
		}
		else if(value instanceof RefUShort)
		{
			return castToLong(((RefUShort)value)._val);
		}
		else if(value instanceof RefUInteger)
		{
			return castToLong(((RefUInteger)value)._val);
		}
		else if(value instanceof RefULong)
		{
			return castToLong(((RefULong)value).longValue());
		}
		else
		{
			return null;
		}
	}
	
	//FLOAT
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefFloat castToFloat(byte value)
	{
		return new RefFloat(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefFloat castToFloat(short value)
	{
		return new RefFloat(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefFloat castToFloat(int value)
	{
		return new RefFloat(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefFloat castToFloat(long value)
	{
		return new RefFloat((float)value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefFloat castToFloat(float value)
	{
		return new RefFloat(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefFloat castToFloat(double value)
	{
		return new RefFloat((float)value);
	}
	
	/**
	 * Convert a Object to a {@link RefNumber} or specified type.
	 * @param value The Object to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public static RefFloat castToFloat(Object value)
	{
		if(value == null)
		{
			return null;
		}
		
		if(value instanceof Byte)
		{
			return castToFloat(((Byte)value).byteValue());
		}
		else if(value instanceof Short)
		{
			return castToFloat(((Short)value).shortValue());
		}
		else if(value instanceof Integer)
		{
			return castToFloat(((Integer)value).intValue());
		}
		else if(value instanceof Long)
		{
			return castToFloat(((Long)value).longValue());
		}
		else if(value instanceof Float)
		{
			return castToFloat(((Float)value).floatValue());
		}
		else if(value instanceof Double)
		{
			return castToFloat(((Double)value).doubleValue());
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Convert this {@link RefNumber} to a {@link RefNumber} of specified type.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public RefFloat castToFloat()
	{
		return RefNumber.castToFloat(this);
	}
	
	/**
	 * Convert a Object to a {@link RefNumber} or specified type.
	 * @param value The Object to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public static RefFloat castToFloat(RefNumber value)
	{
		if(value == null)
		{
			return null;
		}
		
		if(value instanceof RefByte)
		{
			return castToFloat(((RefByte)value)._val);
		}
		else if(value instanceof RefShort)
		{
			return castToFloat(((RefShort)value)._val);
		}
		else if(value instanceof RefInteger)
		{
			return castToFloat(((RefInteger)value)._val);
		}
		else if(value instanceof RefLong)
		{
			return castToFloat(((RefLong)value)._val);
		}
		else if(value instanceof RefFloat)
		{
			RefFloat rf = (RefFloat)value;
			return rf._fixed ? rf.clone() : rf;
		}
		else if(value instanceof RefDouble)
		{
			return castToFloat(((RefDouble)value)._val);
		}
		else if(value instanceof RefUByte)
		{
			return castToFloat(((RefUByte)value)._val);
		}
		else if(value instanceof RefUShort)
		{
			return castToFloat(((RefUShort)value)._val);
		}
		else if(value instanceof RefUInteger)
		{
			return castToFloat(((RefFloat)value)._val);
		}
		else if(value instanceof RefULong)
		{
			return castToFloat(((RefULong)value).longValue());
		}
		else
		{
			return null;
		}
	}
	
	//DOUBLE
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefDouble castToDouble(byte value)
	{
		return new RefDouble(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefDouble castToDouble(short value)
	{
		return new RefDouble(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefDouble castToDouble(int value)
	{
		return new RefDouble(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefDouble castToDouble(long value)
	{
		return new RefDouble(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefDouble castToDouble(float value)
	{
		return new RefDouble(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefDouble castToDouble(double value)
	{
		return new RefDouble(value);
	}
	
	/**
	 * Convert a Object to a {@link RefNumber} or specified type.
	 * @param value The Object to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public static RefDouble castToDouble(Object value)
	{
		if(value == null)
		{
			return null;
		}
		
		if(value instanceof Byte)
		{
			return castToDouble(((Byte)value).byteValue());
		}
		else if(value instanceof Short)
		{
			return castToDouble(((Short)value).shortValue());
		}
		else if(value instanceof Integer)
		{
			return castToDouble(((Integer)value).intValue());
		}
		else if(value instanceof Long)
		{
			return castToDouble(((Long)value).longValue());
		}
		else if(value instanceof Float)
		{
			return castToDouble(((Float)value).floatValue());
		}
		else if(value instanceof Double)
		{
			return castToDouble(((Double)value).doubleValue());
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Convert this {@link RefNumber} to a {@link RefNumber} of specified type.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public RefDouble castToDouble()
	{
		return RefNumber.castToDouble(this);
	}
	
	/**
	 * Convert a Object to a {@link RefNumber} or specified type.
	 * @param value The Object to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public static RefDouble castToDouble(RefNumber value)
	{
		if(value == null)
		{
			return null;
		}
		
		if(value instanceof RefByte)
		{
			return castToDouble(((RefByte)value)._val);
		}
		else if(value instanceof RefShort)
		{
			return castToDouble(((RefShort)value)._val);
		}
		else if(value instanceof RefInteger)
		{
			return castToDouble(((RefInteger)value)._val);
		}
		else if(value instanceof RefLong)
		{
			return castToDouble(((RefLong)value)._val);
		}
		else if(value instanceof RefFloat)
		{
			return castToDouble(((RefFloat)value)._val);
		}
		else if(value instanceof RefDouble)
		{
			RefDouble rd = (RefDouble)value;
			return rd._fixed ? rd.clone() : rd;
		}
		else if(value instanceof RefUByte)
		{
			return castToDouble(((RefUByte)value)._val);
		}
		else if(value instanceof RefUShort)
		{
			return castToDouble(((RefUShort)value)._val);
		}
		else if(value instanceof RefUInteger)
		{
			return castToDouble(((RefUInteger)value)._val);
		}
		else if(value instanceof RefULong)
		{
			return castToDouble(((RefULong)value).longValue());
		}
		else
		{
			return null;
		}
	}
	
	//UBYTE
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefUByte castToUByte(byte value)
	{
		return new RefUByte(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefUByte castToUByte(short value)
	{
		return new RefUByte((byte)value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefUByte castToUByte(int value)
	{
		return new RefUByte((byte)value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefUByte castToUByte(long value)
	{
		return new RefUByte((byte)value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefUByte castToUByte(float value)
	{
		return castToUByte((double)value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefUByte castToUByte(double value)
	{
		return castToUByte(castToUInt(value));
	}
	
	/**
	 * Convert a Object to a {@link RefNumber} or specified type.
	 * @param value The Object to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public static RefUByte castToUByte(Object value)
	{
		if(value == null)
		{
			return null;
		}
		
		if(value instanceof Byte)
		{
			return castToUByte(((Byte)value).byteValue());
		}
		else if(value instanceof Short)
		{
			return castToUByte(((Short)value).shortValue());
		}
		else if(value instanceof Integer)
		{
			return castToUByte(((Integer)value).intValue());
		}
		else if(value instanceof Long)
		{
			return castToUByte(((Long)value).longValue());
		}
		else if(value instanceof Float)
		{
			return castToUByte(((Float)value).floatValue());
		}
		else if(value instanceof Double)
		{
			return castToUByte(((Double)value).doubleValue());
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Convert this {@link RefNumber} to a {@link RefNumber} of specified type.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public RefUByte castToUByte()
	{
		return RefNumber.castToUByte(this);
	}
	
	/**
	 * Convert a Object to a {@link RefNumber} or specified type.
	 * @param value The Object to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public static RefUByte castToUByte(RefNumber value)
	{
		if(value == null)
		{
			return null;
		}
		
		if(value instanceof RefByte)
		{
			return castToUByte(((RefByte)value)._val);
		}
		else if(value instanceof RefShort)
		{
			return castToUByte(((RefShort)value)._val);
		}
		else if(value instanceof RefInteger)
		{
			return castToUByte(((RefInteger)value)._val);
		}
		else if(value instanceof RefLong)
		{
			return castToUByte(((RefLong)value)._val);
		}
		else if(value instanceof RefFloat)
		{
			return castToUByte(((RefFloat)value)._val);
		}
		else if(value instanceof RefDouble)
		{
			return castToUByte(((RefDouble)value)._val);
		}
		else if(value instanceof RefUByte)
		{
			RefUByte ub = (RefUByte)value;
			return ub._fixed ? ub.clone() : ub;
		}
		else if(value instanceof RefUShort)
		{
			return castToUByte(((RefUShort)value)._val);
		}
		else if(value instanceof RefUInteger)
		{
			return castToUByte(((RefUInteger)value)._val);
		}
		else if(value instanceof RefULong)
		{
			return castToUByte(((RefULong)value).longValue());
		}
		else
		{
			return null;
		}
	}
	
	//USHORT
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefUShort castToUShort(byte value)
	{
		return new RefUShort(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefUShort castToUShort(short value)
	{
		return new RefUShort(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefUShort castToUShort(int value)
	{
		return new RefUShort((short)value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefUShort castToUShort(long value)
	{
		return new RefUShort((short)value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefUShort castToUShort(float value)
	{
		return castToUShort(castToUInt(value));
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefUShort castToUShort(double value)
	{
		return castToUShort(castToUInt(value));
	}
	
	/**
	 * Convert a Object to a {@link RefNumber} or specified type.
	 * @param value The Object to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public static RefUShort castToUShort(Object value)
	{
		if(value == null)
		{
			return null;
		}
		
		if(value instanceof Byte)
		{
			return castToUShort(((Byte)value).byteValue());
		}
		else if(value instanceof Short)
		{
			return castToUShort(((Short)value).shortValue());
		}
		else if(value instanceof Integer)
		{
			return castToUShort(((Integer)value).intValue());
		}
		else if(value instanceof Long)
		{
			return castToUShort(((Long)value).longValue());
		}
		else if(value instanceof Float)
		{
			return castToUShort(((Float)value).floatValue());
		}
		else if(value instanceof Double)
		{
			return castToUShort(((Double)value).doubleValue());
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Convert this {@link RefNumber} to a {@link RefNumber} of specified type.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public RefUShort castToUShort()
	{
		return RefNumber.castToUShort(this);
	}
	
	/**
	 * Convert a Object to a {@link RefNumber} or specified type.
	 * @param value The Object to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public static RefUShort castToUShort(RefNumber value)
	{
		if(value == null)
		{
			return null;
		}
		
		if(value instanceof RefByte)
		{
			return castToUShort(((RefByte)value)._val);
		}
		else if(value instanceof RefShort)
		{
			return castToUShort(((RefShort)value)._val);
		}
		else if(value instanceof RefInteger)
		{
			return castToUShort(((RefInteger)value)._val);
		}
		else if(value instanceof RefLong)
		{
			return castToUShort(((RefLong)value)._val);
		}
		else if(value instanceof RefFloat)
		{
			return castToUShort(((RefFloat)value)._val);
		}
		else if(value instanceof RefDouble)
		{
			return castToUShort(((RefDouble)value)._val);
		}
		else if(value instanceof RefUByte)
		{
			return castToUShort(((RefUByte)value)._val);
		}
		else if(value instanceof RefUShort)
		{
			RefUShort ui = (RefUShort)value;
			return ui._fixed ? ui.clone() : ui;
		}
		else if(value instanceof RefUInteger)
		{
			return castToUShort(((RefUInteger)value)._val);
		}
		else if(value instanceof RefULong)
		{
			return castToUShort(((RefULong)value).longValue());
		}
		else
		{
			return null;
		}
	}
	
	//UINT
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefUInteger castToUInt(byte value)
	{
		return new RefUInteger(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefUInteger castToUInt(short value)
	{
		return new RefUInteger(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefUInteger castToUInt(int value)
	{
		return new RefUInteger(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefUInteger castToUInt(long value)
	{
		return new RefUInteger((int)value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefUInteger castToUInt(float value)
	{
		return castToUInt((double)value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefUInteger castToUInt(double value)
	{
		if((value < -0.5) || (value > UINT_CAST_DOUBLE_MAX))
		{
			//Could throw an error but if I am doing some automated processing I don't want the developer to get a "mystery" error.
			value = Math.min(Math.max(-0.5, value), UINT_CAST_DOUBLE_MAX);
		}
		long nNum = (long)value;
		double nValue = value - nNum;
		if ((nValue > 0.5) || ((nValue == 0.5) && ((nNum & 1) != 0)))
	    {
			nNum++;
	    }
		nNum = Math.max(Math.min(nNum, 0xFFFFFFFFL), 0L);
		RefUInteger ui = new RefUInteger();
		ui._val = nNum;
		return ui;
	}
	
	/**
	 * Convert a Object to a {@link RefNumber} or specified type.
	 * @param value The Object to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public static RefUInteger castToUInt(Object value)
	{
		if(value == null)
		{
			return null;
		}
		
		if(value instanceof Byte)
		{
			return castToUInt(((Byte)value).byteValue());
		}
		else if(value instanceof Short)
		{
			return castToUInt(((Short)value).shortValue());
		}
		else if(value instanceof Integer)
		{
			return castToUInt(((Integer)value).intValue());
		}
		else if(value instanceof Long)
		{
			return castToUInt(((Long)value).longValue());
		}
		else if(value instanceof Float)
		{
			return castToUInt(((Float)value).floatValue());
		}
		else if(value instanceof Double)
		{
			return castToUInt(((Double)value).doubleValue());
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Convert this {@link RefNumber} to a {@link RefNumber} of specified type.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public RefUInteger castToUInt()
	{
		return RefNumber.castToUInt(this);
	}
	
	/**
	 * Convert a Object to a {@link RefNumber} or specified type.
	 * @param value The Object to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public static RefUInteger castToUInt(RefNumber value)
	{
		if(value == null)
		{
			return null;
		}
		
		if(value instanceof RefByte)
		{
			return castToUInt(((RefByte)value)._val);
		}
		else if(value instanceof RefShort)
		{
			return castToUInt(((RefShort)value)._val);
		}
		else if(value instanceof RefInteger)
		{
			return castToUInt(((RefInteger)value)._val);
		}
		else if(value instanceof RefLong)
		{
			return castToUInt(((RefLong)value)._val);
		}
		else if(value instanceof RefFloat)
		{
			return castToUInt(((RefFloat)value)._val);
		}
		else if(value instanceof RefDouble)
		{
			return castToUInt(((RefDouble)value)._val);
		}
		else if(value instanceof RefUByte)
		{
			return castToUInt(((RefUByte)value)._val);
		}
		else if(value instanceof RefUShort)
		{
			return castToUInt(((RefUShort)value)._val);
		}
		else if(value instanceof RefUInteger)
		{
			RefUInteger ui = (RefUInteger)value;
			return ui._fixed ? ui.clone() : ui;
		}
		else if(value instanceof RefULong)
		{
			return castToUInt(((RefULong)value).longValue());
		}
		else
		{
			return null;
		}
	}
	
	//ULONG
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefULong castToULong(byte value)
	{
		return new RefULong(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefULong castToULong(short value)
	{
		return new RefULong(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefULong castToULong(int value)
	{
		return new RefULong(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefULong castToULong(long value)
	{
		return new RefULong(value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefULong castToULong(float value)
	{
		return castToULong((double)value);
	}
	
	/**
	 * Convert a number to a {@link RefNumber} or specified type.
	 * @param value The value to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}.
	 */
	public static RefULong castToULong(double value)
	{
		return new RefULong(MathUtilities.round(value));
	}
	
	/**
	 * Convert a Object to a {@link RefNumber} or specified type.
	 * @param value The Object to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public static RefULong castToULong(Object value)
	{
		if(value == null)
		{
			return null;
		}
		
		if(value instanceof Byte)
		{
			return castToULong(((Byte)value).byteValue());
		}
		else if(value instanceof Short)
		{
			return castToULong(((Short)value).shortValue());
		}
		else if(value instanceof Integer)
		{
			return castToULong(((Integer)value).intValue());
		}
		else if(value instanceof Long)
		{
			return castToULong(((Long)value).longValue());
		}
		else if(value instanceof Float)
		{
			return castToULong(((Float)value).floatValue());
		}
		else if(value instanceof Double)
		{
			return castToULong(((Double)value).doubleValue());
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Convert this {@link RefNumber} to a {@link RefNumber} of specified type.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public RefULong castToULong()
	{
		return RefNumber.castToULong(this);
	}
	
	/**
	 * Convert a Object to a {@link RefNumber} or specified type.
	 * @param value The Object to convert to a {@link RefNumber}.
	 * @return The specified {@link RefNumber}, or <code>null</code> if <i>value</i> is null or an unsupported type.
	 */
	public static RefULong castToULong(RefNumber value)
	{
		if(value == null)
		{
			return null;
		}
		
		if(value instanceof RefByte)
		{
			return castToULong(((RefByte)value)._val);
		}
		else if(value instanceof RefShort)
		{
			return castToULong(((RefShort)value)._val);
		}
		else if(value instanceof RefInteger)
		{
			return castToULong(((RefInteger)value)._val);
		}
		else if(value instanceof RefLong)
		{
			return castToULong(((RefLong)value)._val);
		}
		else if(value instanceof RefFloat)
		{
			return castToULong(((RefFloat)value)._val);
		}
		else if(value instanceof RefDouble)
		{
			return castToULong(((RefDouble)value)._val);
		}
		else if(value instanceof RefUByte)
		{
			return castToULong(((RefUByte)value)._val);
		}
		else if(value instanceof RefUShort)
		{
			return castToULong(((RefUShort)value)._val);
		}
		else if(value instanceof RefUInteger)
		{
			return castToULong(((RefUInteger)value)._val);
		}
		else if(value instanceof RefULong)
		{
			RefULong ui = (RefULong)value;
			return ui._fixed ? ui.clone() : ui;
		}
		else
		{
			return null;
		}
	}
}

