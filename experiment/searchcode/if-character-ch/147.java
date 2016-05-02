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

/**
 * Reference Character is the same as {@link Character} but allows you to set the character without creating a new {@link Character}.
 * @since BBX 1.1.0
 */
public final class RefCharacter
{
	/**
	 * The maximum radix available for conversion to and from {@link String}s.
	 */
	public static final int MAX_RADIX  = Character.MAX_RADIX;
	/**
	 * The constant value of this field is the largest value of type char.
	 */
	public static final char MAX_VALUE  = Character.MAX_VALUE;
	/**
	 *  The minimum radix available for conversion to and from {@link String}s.
	 */
	public static final int MIN_RADIX  = Character.MIN_RADIX;
	/**
	 * The constant value of this field is the smallest value of type char.
	 */
	public static final char MIN_VALUE  = Character.MIN_VALUE;
	
	private boolean _fixed;
	char _val;
	
	/**
	 * Create a new {@link RefCharacter} set to the default of '\0'.
	 */
	public RefCharacter()
	{
		_fixed = false;
		_val = '\0';
	}
	
	/**
	 * Create a new {@link RefCharacter} using a char primitive.
	 * @param value The char primitive to set this {@link RefCharacter} with.
	 */
	public RefCharacter(char value)
	{
		this(value, false);
	}
	
	/**
	 * Create a new {@link RefCharacter} using a char primitive.
	 * @param value The char primitive to set this {@link RefCharacter} with.
	 * @param fixed If this item is read only and cannot be modified.
	 */
	public RefCharacter(char value, boolean fixed)
	{
		_fixed = fixed;
		_val = value;
	}
	
	/**
	 * Create a new {@link RefCharacter} using a {@link Character}.
	 * @param value The {@link Character} to set this {@link RefCharacter} with.
	 */
	public RefCharacter(Character value)
	{
		this(value, false);
	}
	
	/**
	 * Create a new {@link RefCharacter} using a {@link Character}.
	 * @param value The {@link Character} to set this {@link RefCharacter} with.
	 * @param fixed If this item is read only and cannot be modified.
	 */
	public RefCharacter(Character value, boolean fixed)
	{
		_fixed = fixed;
		_val = value.charValue();
	}
	
	/**
	 * Returns The value of this {@link RefCharacter} object as a char primitive.
	 * @return The primitive char value of this object.
	 */
	public char charValue()
	{
		return _val;
	}
	
	/**
	 * Set the value of this {@link RefCharacter} object with a char primitive.
	 * @param value The primitive char value to set this object.
	 * @return This object.
	 */
	public RefCharacter setValue(char value)
	{
		if(_fixed)
		{
			return this.clone().setValue(value);
		}
		_val = value;
		return this;
	}
	
	/**
	 * Set the value of this {@link RefCharacter} object with a {@link Character}.
	 * @param value The {@link Character} value to set this object.
	 * @return This object.
	 */
	public RefCharacter setValue(Character value)
	{
		if(_fixed)
		{
			return this.clone().setValue(value);
		}
		_val = value.charValue();
		return this;
	}
	
	/**
	 * Returns true if and only if the argument is not null and is a {@link RefCharacter} or {@link Character} object that represents the same char value as this object.
	 * @param obj The object to compare with.
	 * @return true if the {@link RefCharacter} objects represent the same value; false otherwise.
	 */
	public boolean equals(Object obj)
	{
		if((obj != null) && ((obj instanceof RefCharacter) || (obj instanceof Character)))
		{
			if(obj instanceof Character)
			{
				return ((Character)obj).charValue() == this._val;
			}
			else
			{
				return ((RefCharacter)obj)._val == this._val;
			}
		}
		return false;
	}
	
	/**
	 * Returns a hash code for this {@link RefCharacter} object.
	 * @return A hash code value for this object.
	 */
	public int hashCode()
	{
		return new Character(_val).hashCode();
	}
	
	/**
	 * Returns a {@link String} object representing this {@link RefCharacter}'s value.
	 * @return A string representation of the object.
	 */
	public String toString()
	{
		return new Character(_val).toString();
	}
	
	/**
	 * Returns the numeric value of the character ch in the specified radix.
	 * @param ch The character to be converted.
	 * @param radix The radix.
	 * @return The numeric value represented by the character in the specified radix.
	 */
	public static int digit(char ch, int radix)
	{
		return Character.digit(ch, radix);
	}
	
	/**
	 * Determines if the specified character is a digit.
	 * @param ch The character to be tested.
	 * @return true if the character is a digit; false otherwise.
	 */
	public static boolean isDigit(char ch)
	{
		return Character.isDigit(ch);
	}
	
	/**
	 * Determines if the specified character is a lowercase character.
	 * @param ch The character to be tested.
	 * @return True if the character is lowercase; false otherwise.
	 */
	public static boolean isLowerCase(char ch)
	{
		return Character.isLowerCase(ch);
	}
	
	/**
	 * Determines if the specified character is an uppercase character.
	 * @param ch The character to be tested.
	 * @return true if the character is uppercase; false otherwise.
	 */
	public static boolean isUpperCase(char ch)
	{
		return Character.isUpperCase(ch);
	}
	
	/**
	 * The given character is mapped to its lowercase equivalent; if the character has no lowercase equivalent, the character itself is returned.
	 * @param ch The character to be converted.
	 * @return The lowercase equivalent of the character, if any; otherwise the character itself.
	 */
	public static char toLowerCase(char ch)
	{
		return Character.toLowerCase(ch);
	}
	
	/**
	 * Converts the character argument to uppercase; if the character has no lowercase equivalent, the character itself is returned.
	 * @param ch The character to be converted.
	 * @return The uppercase equivalent of the character, if any; otherwise the character itself.
	 */
	public static char toUpperCase(char ch)
	{
		return Character.toUpperCase(ch);
	}
	
	/**
	 * Create a clone of the current object.
	 * @return A clone of the current {@link RefCharacter}.
	 */
	public RefCharacter clone()
	{
		return new RefCharacter(_val);
	}
}

