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
import rebuild.util.text.StringUtilities;

/**
 * Reference ULong is the same as {@link Long} but is unsigned and allows you to set the long without creating a new {@link Long}.
 * @since BBX 1.1.0
 */
public final class RefULong extends RefNumber
{
	/**
	 * The maximum value a {@link RefULong} can have. This is stored as a signed number for simplicity.
	 */
	public static final long MAX_VALUE_LONG = 0xFFFFFFFFFFFFFFFFL;
	/**
	 * The minimum value a {@link RefULong} can have. This is stored as a signed number for simplicity.
	 */
	public static final long MIN_VALUE_LONG = 0x0000000000000000L;
	
	/**
	 * The maximum value a {@link RefULong} can have.
	 */
	public static final RefULong MAX_VALUE = new RefULong(MAX_VALUE_LONG, true);
	/**
	 * The minimum value a {@link RefULong} can have.
	 */
	public static final RefULong MIN_VALUE = new RefULong(MIN_VALUE_LONG, true);
	
	boolean _highBit;
	long _val;
	
	/**
	 * Create a new {@link RefULong} set to the default of 0.
	 */
	public RefULong()
	{
		_val = 0;
	}
	
	/**
	 * Create a new {@link RefULong} using a long primitive.
	 * @param value The long primitive to set this {@link RefULong} with.
	 */
	public RefULong(long value)
	{
		this(value, false);
	}
	
	/**
	 * Create a new {@link RefULong} using a long primitive.
	 * @param value The long primitive to set this {@link RefULong} with.
	 * @param fixed If this item is read only and cannot be modified.
	 */
	public RefULong(long value, boolean fixed)
	{
		super(fixed);
		if (value < 0)
        {
			this._highBit = true;
			this._val = value + Long.MIN_VALUE;
        }
        else
        {
        	this._highBit = false;
            this._val = value;
        }
	}
	
	/**
	 * Returns The value of this {@link RefULong} object as a long primitive.
	 * @return The primitive long value of this object.
	 */
	public long longValue()
	{
		if (this._highBit)
        {
            return _val + Long.MAX_VALUE + 1L;
        }
        else
        {
            return this._val;
        }
	}
	
	/**
	 * Returns The value of this {@link RefULong} object as a long primitive. It does not contain the high order bit.
	 * @return The primitive long value of this object.
	 */
	public long raw_ulongValue()
	{
		return _val;
	}
	
	/**
	 * Returns if the high order bit of the ulong is set.
	 * @return <code>true</code> if the high order bit is set, <code>false</code> otherwise.
	 */
	public boolean raw_highBitValue()
	{
		return _highBit;
	}
	
	/**
	 * Set the value of this {@link RefULong} object with a long primitive.
	 * @param value The primitive long value to set this object.
	 * @return This object.
	 */
	public RefULong setValue(long value)
	{
		if(_fixed)
		{
			return this.clone().setValue(value);
		}
		if (value < 0)
        {
            this._highBit = true;
            this._val = Long.MAX_VALUE + value;
        }
        else
        {
            this._highBit = false;
            this._val = value;
        }
		return this;
	}
	
	/**
	 * Set the value of this {@link RefULong} object with a {@link Long}.
	 * @param value The {@link Long} value to set this object.
	 * @return This object.
	 */
	public RefULong setValue(Long value)
	{
		return setValue(value.longValue());
	}
	
	/**
	 * Returns a hash code for this {@link RefULong} object.
	 * @return A hash code value for this object.
	 */
	public int hashCode()
	{
		return new Long(_val).hashCode() + new Boolean(_highBit).hashCode();
	}
	
	/**
	 * Returns a {@link String} object representing this {@link RefULong}'s value.
	 * @return A string representation of the object.
	 */
	public String toString()
	{
		StringBuffer bu = new StringBuffer();
        if (this._highBit)
        {
            if (this._val != 0L)
            {
            	long value = this._val;
                //Get the value as an array
                byte[] result = new byte[] { 0, 9, 2, 2, 3, 3, 7, 2, 0, 3, 6, 8, 5, 4, 7, 7, 5, 8, 0, 8 };
                
                byte[] addValues = new byte[19];
                int i = 19;
                while (value != 0)
                {
                    addValues[--i] = (byte)(value % 10);
                    value /= 10;
                }
                
                //Add "addValues" to result
                int a = 18;
                int r = 19;
                int rem = 0;
                for (int k = 19; k >= 0; k--, r--, a--)
                {
                    byte add = (byte)(a >= i ? addValues[a] : 0);
                    byte val = (byte)(result[r] + add + rem);
                    rem -= rem;
                    if (val >= 10)
                    {
                        rem = 1;
                        val -= 10;
                    }
                    result[r] = val;
                }
                
                //Convert array to string
                boolean writing = false;
                for (int k = 0; k < 20; k++)
                {
                    if (writing)
                    {
                    	bu.append(result[k]);
                    }
                    else if (result[k] != 0)
                    {
                        writing = true;
                        k--;
                    }
                }
            }
            else
            {
                bu.append("9223372036854775808");
            }
        }
        else
        {
            //Simply write the value
            bu.append(this._val);
        }
        return bu.toString();
	}
	
	/**
	 * Assuming the specified {@link String} represents a long, returns that long's value.
	 * @param s The {@link String} containing the long.
	 * @return The parsed value of the long.
	 * @throws NumberFormatException If the string does not contain a parsable long.
	 */
	public static RefULong parseULong(String s)
	{
		return parseULong(s, 10);
	}
	
	/**
	 * Assuming the specified {@link String} represents a long, returns that long's value.
	 * @param s The {@link String} containing the long.
	 * @param radix The radix to be used.
	 * @return The parsed value of the long.
	 * @throws NumberFormatException If the string does not contain a parsable long.
	 */
	public static RefULong parseULong(String s, int radix)
	{
		//Check for invalid data, have the native BlackBerry system throw the error
		if(s == null || s.length() == 0)
		{
			Long.parseLong(s, radix);
		}
		if(radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
		{
			Long.parseLong("10", radix);
		}
		
		//Do some preliminary checks to make sure the number is valid.
		char firstChar = s.charAt(0);
        int i = 0, len = s.length();
        if (firstChar < '0')
        {
            if (firstChar == '-')
            {
            	//Negative number not supported
            	throw new NumberFormatException(StringUtilities.format_java(Resources.getString(BBXResource.UNSIGNED_NUMBER_UNPARSEABLE), "long"));
            }
            else if (firstChar != '+')
            {
            	Long.parseLong(s, radix);
            }
            if (len == 1)
            {
            	Long.parseLong(s, radix);
            }
            i++;
        }
        //Get the multmin (the maximum number before multiplication goes high then the radix's possible value for a ulong).
        RefInteger radRef = new RefInteger(radix, true);
        //Create the result and use it temporarily for the multmin calculation.
        RefULong result = new RefULong(RefULong.MAX_VALUE_LONG);
        long multmin = ((RefULong)result.divide(radRef)).longValue();
        result._val = 0L;
        result._highBit = false;
        
        //Calculate the number
        long digit;
        while (i < len)
        {
        	digit = Character.digit(s.charAt(i++), radix);
        	if (digit < 0)
            {
        		Long.parseLong(s, radix);
            }
        	if (result._val > multmin)
            {
        		//The number is greater then the maximum range of this function.
                throw new NumberFormatException(StringUtilities.format_java(Resources.getString(BBXResource.UNSIGNED_NUMBER_UNPARSEABLE), "long"));
            }
        	result.multiply(radRef);
        	if (result._highBit && Long.MAX_VALUE - result._val < digit)
            {
        		Byte.parseByte(s, radix);
            }
        	result.add(digit);
        }
        return result;
	}
	
	/**
	 * Create a clone of the current object.
	 * @return A clone of the current {@link RefULong}.
	 */
	public RefULong clone()
	{
		RefULong clone = new RefULong();
		clone._val = this._val;
		clone._highBit = this._highBit;
		return clone;
	}
	
	/**
	 * Create a clone of the current object.
	 * @return A clone of the current {@link RefULong}.
	 */
	public RefNumber cloneNumber()
	{
		return clone();
	}
	
	/*
	 * Unsigned long operations, ulong's are treated differently because they have a different internal structure
	 */
	
	void ulongAdd(RefNumber number, boolean rawCalc)
	{
		if(this._fixed)
		{
			//Can't change
			return;
		}
		
		RefULong add = rawCalc ? (RefULong)number : null;
		if(add == null)
		{
			add = new RefULong();
			if(number instanceof RefLong || number instanceof RefInteger || number instanceof RefShort || 
					number instanceof RefByte || number instanceof RefFloat || number instanceof RefDouble)
			{
				//Signed numbers
				
				//Check if it is negative, if so then do subtraction
				if(number instanceof RefLong)
				{
					add._val = ((RefLong)number)._val;
				}
				else if(number instanceof RefInteger)
				{
					add._val = ((RefInteger)number)._val;
				}
				else if(number instanceof RefShort)
				{
					add._val = ((RefShort)number)._val;
				}
				else if(number instanceof RefByte)
				{
					add._val = ((RefByte)number)._val;
				}
				else if(number instanceof RefFloat)
				{
					add._val = ((RefFloat)number).longValue();
				}
				else if(number instanceof RefDouble)
				{
					add._val = ((RefDouble)number).longValue();
				}
				
				if(add._val < 0)
				{
					ulongSubtract(new RefLong(-add._val));
				}
				if(add._val <= 0)
				{
					//Zero, nothing to do
					return;
				}
			}
			else
			{
				//Unsigned numbers
				
				if(number instanceof RefUByte)
				{
					add._val = ((RefUByte)number)._val;
				}
				else if(number instanceof RefUShort)
				{
					add._val = ((RefUShort)number)._val;
				}
				else if(number instanceof RefUInteger)
				{
					add._val = ((RefUInteger)number)._val;
				}
				else if(number instanceof RefULong)
				{
					RefULong t = ((RefULong)number);
					add._val = t._val;
					add._highBit = t._highBit;
				}
				
				if(add._val == 0L && !add._highBit)
				{
					//Zero, nothing to do
					return;
				}
			}
		}
		//Lets get down to business
		
		//Add the values together and see if they "overflow"
        long fs = this._val + add._val;
        if (fs < 0)
        {
            //NOTE: bits go 63->0
            //Since they overflowed, 1. get up to the 61 bit so that the last 2 bits can be manually added
            long fv = this._val & 0x3FFFFFFFFFFFFFFFL;
            long sv = add._val & 0x3FFFFFFFFFFFFFFFL;
            fs = fv + sv;

            //2. Check for overflow of 62bit addition
            boolean overflow = (fs & 0x4000000000000000L) == 0x4000000000000000L;
            //3. Calculate bits 63-62
            if (overflow && (((this._val & 0x4000000000000000L) != 0x4000000000000000L) ||
                    ((add._val & 0x4000000000000000L) != 0x4000000000000000L)))
            {
                /* Options:
                 * 1. Both first and second val have bit 62 set->odd number, the 62 bit should remain a 1
                 * 2. Either first or second val have bit 62 set->even number, the 62 bit needs to go.
                 * 
                 * Option 2 is the only time thing we need to operate on.
                 */
                fs &= 0x3FFFFFFFFFFFFFFFL;
            }

            //The highbit is determined by if overflow is set and is only if both or neither of the values are set.
            if ((this._highBit && add._highBit) || (!this._highBit && !add._highBit))
            {
                this._highBit = overflow;
            }
            else
            {
                this._highBit = !overflow;
            }
        }
        else
        {
        	/*
            if (!this._highBit && add._highBit)
            {
                //Need to pass the high bit
                this._highBit = true;
            }
            else if (this._highBit && add._highBit)
            {
                //High bit should not be set
                this._highBit = false;
            }
            */
        	if (add._highBit)
            {
        		this._highBit = !this._highBit;
            }
        }
        this._val = fs;
	}
	
	void ulongSubtract(RefNumber number)
	{
		if(this._fixed)
		{
			//Can't change
			return;
		}
		
		RefULong sub = new RefULong();
		if(number instanceof RefLong || number instanceof RefInteger || number instanceof RefShort || 
				number instanceof RefByte || number instanceof RefFloat || number instanceof RefDouble)
		{
			//Signed numbers
			
			//Check if it is negative, if so then do addition
			if(number instanceof RefLong)
			{
				sub._val = ((RefLong)number)._val;
			}
			else if(number instanceof RefInteger)
			{
				sub._val = ((RefInteger)number)._val;
			}
			else if(number instanceof RefShort)
			{
				sub._val = ((RefShort)number)._val;
			}
			else if(number instanceof RefByte)
			{
				sub._val = ((RefByte)number)._val;
			}
			else if(number instanceof RefFloat)
			{
				sub._val = ((RefFloat)number).longValue();
			}
			else if(number instanceof RefDouble)
			{
				sub._val = ((RefDouble)number).longValue();
			}
			
			if(sub._val < 0)
			{
				ulongAdd(new RefLong(-sub._val), true);
			}
			if(sub._val <= 0)
			{
				//Zero, nothing to do
				return;
			}
		}
		else
		{
			//Unsigned numbers
			
			if(number instanceof RefUByte)
			{
				sub._val = ((RefUByte)number)._val;
			}
			else if(number instanceof RefUShort)
			{
				sub._val = ((RefUShort)number)._val;
			}
			else if(number instanceof RefUInteger)
			{
				sub._val = ((RefUInteger)number)._val;
			}
			else if(number instanceof RefULong)
			{
				RefULong t = ((RefULong)number);
				sub._val = t._val;
				sub._highBit = t._highBit;
			}
			
			if(sub._val == 0L && !sub._highBit)
			{
				//Zero, nothing to do
				return;
			}
		}
		
		//Avoid doing unnecessary work
		if (this._highBit == sub._highBit && this._val == sub._val)
        {
            this._highBit = false;
            this._val = 0L;
            return;
        }
		//Lets get down to business
		
		// ?|?????->63
        //-?|?????->63
        //------------
        // ?|?????->63
        long fs = this._val - sub._val;
        if (fs < 0L)
        {
            if ((this._highBit && sub._highBit) || (!this._highBit && !sub._highBit))
            {
                // B|?????->63,  B|?????->63
                // 1|?????->63,  0|?????->63
                //-1|?????->63, -0|?????->63
                //--------------------------
                // 1|?????->63,  1|?????->63
                fs = Long.MAX_VALUE + fs + 1L; //This is to prevent "overflow" otherwise fs += Long.MAX_VALUE + 1L; should be fine
                this._highBit = true;
            }
            else
            {
                /*
                if (!this._highBit && sub._highBit)
                {
                    // B|?????->63
                    // 0|?????->63
                    //-1|?????->63
                    //------------
                    // 0|?????->63
                }
                else
                {
                    // B|?????->63
                    // 1|?????->63
                    //-0|?????->63
                    //------------
                    // 0|?????->63
                }
                */
                fs = Long.MAX_VALUE - (sub._val - this._val - 1L);
                this._highBit = false;
            }
        }
        else
        {
            //Just need to process the high bit
        	/*
            if ((this._highBit && sub._highBit) || (!this._highBit && !sub._highBit))
            {
                // 1|?????->63,  0|?????->63
                //-1|?????->63, -0|?????->63
                //--------------------------
                // 0|?????->63,  0|?????->63
                this._highBit = false;
            }
            else
            {
                /*
                if (!this._highBit && sub._highBit)
                {
                    // 0|?????->63
                    //-1|?????->63
                    //------------
                    // 1|?????->63
                    //Need to borrow
                }
                else
                {
                    // 1|?????->63
                    //-0|?????->63
                    //------------
                    // 1|?????->63
                }
                *-/
                this._highBit = true;
            }
            */
        	this._highBit = !((this._highBit && sub._highBit) || (!this._highBit && !sub._highBit));
        }
        this._val = fs;
	}
	
	void ulongMulti(RefNumber number)
	{
		if(this._fixed)
		{
			//Can't change
			return;
		}
		
		RefULong multi = RefNumber.castToULong(number);
		
		//Multiply by 1 doesn't do anything and multiply by zero returns zero
        if (!multi._highBit)
        {
            if (multi._val == 1L)
            {
                return;
            }
            else if (multi._val == 0L)
            {
                this._highBit = false;
                this._val = 0L;
                return;
            }
        }
        
        //Lets get down to business
        
        //Get the count
        int firstCount = this._highBit ? 64 : -1;
        int secondCount = multi._highBit ? 64 : -1;
        long mask;
        if (firstCount == -1)
        {
            firstCount = 0;
            for (int i = 63; i > 0; i--)
            {
                mask = 1L << (i - 1);
                if ((this._val & mask) == mask)
                {
                    firstCount = i;
                    break;
                }
            }
        }
        if (secondCount == -1)
        {
            secondCount = 0;
            for (int i = 63; i > 0; i--)
            {
                mask = 1L << (i - 1);
                if ((multi._val & mask) == mask)
                {
                    secondCount = i;
                    break;
                }
            }
        }
        
        //Duplicate the first number so it can be modified
        long fs_val_back = this._val;
        
        //Even if this bit is set, the manner this function executes will cause this to be false unless a number is 
        //added that makes it bigger.
        this._highBit = false;
        
        //Determine if manual multiplication is needed, number is 63 so that the booleans can be set separately
        boolean manual = firstCount + (secondCount - 1) > 63;
        
        //Mask out certain bits
        int maskLen = Math.max(0, Math.min(63 - firstCount, secondCount));
        
        /*
        mask = 0;
        for (int i = 1; i <= maskLen; i++)
        {
            mask |= 1L << (i - 1);
        }
        */
        mask = RefNumber.LONG_LOW_BIT_MASK[maskLen];
        this._val *= (multi._val & mask); //The real magic
        
        if (!manual)
        {
            //Done, everything could be done outside of manual multiplication
            return;
        }
        
        //maskLen = current "position" (zero based)
        
        RefULong multiAdd = new RefULong();
        for (; maskLen < secondCount; maskLen++)
        {
            mask = 1L << maskLen;
            if ((multi._val & mask) == mask)
            {
            	multiAdd._val = fs_val_back << maskLen;
            	multiAdd._highBit = multiAdd._val < 0L;
                if (multiAdd._highBit)
                {
                	multiAdd._val &= 0x7FFFFFFFFFFFFFFFL;
                }
                ulongAdd(multiAdd, true);
            }
        }
        
        //The top bit for multiplying is set, if the number is correct
        if (secondCount == 64 && multi._highBit)
        {
            if ((fs_val_back & 0x0000000000000001L) == 0x0000000000000001L)
            {
            	multiAdd._val = 0L;
            	multiAdd._highBit = true;
            	ulongAdd(multiAdd, true);
            }
        }
	}
	
	void ulongDivide(RefNumber number, boolean returnRemained)
	{
		if(this._fixed)
		{
			//Can't change
			return;
		}
		
		RefULong divide = RefNumber.castToULong(number);
		
		//Can't divide by zero.
        if (!divide._highBit && divide._val == 0L)
        {
            //Have an error get thrown
        	if (returnRemained)
            {
                int error = 1 % 0;
            }
            else
            {
                int error = 1 / 0;
            }
            return;
        }
        //If the number is the same then the result is 1.
        if (this._highBit == divide._highBit && this._val == divide._val)
        {
        	this._highBit = false;
        	this._val = returnRemained ? 0L : 1L;
            return;
        }
        
        //If the divider is greater then the number being divided then return 0
        if (((divide._val > this._val) && (!this._highBit || (this._highBit && divide._highBit))) || 
                //Sometimes the _val might not be less but the high bit is set instead
                (!this._highBit && divide._highBit))
        {
        	if(!returnRemained)
        	{
	        	this._highBit = false;
	        	this._val = 0L;
        	}
            return;
        }
		
		//Lets get down to business
        if (!this._highBit && !divide._highBit)
        {
            //this._highBit = false; this is already set to false
        	if (returnRemained)
            {
        		this._val %= divide._val;
            }
            else
            {
            	this._val /= divide._val;
            }
            return;
        }
        
        //If the highbit is set of the divider and the value to be divided it can only do one process, thus 1 is returned
        if (this._highBit && divide._highBit)
        {
        	this._highBit = false;
        	this._val = returnRemained ? this._val - divide._val : 1L;
            return;
        }
        
        //Backup the value
        long fs_back_val = this._val;
        
        //Get the mask needed for division
        int maskIndex = 62;
        long shiftedValue = 0L;
        for (; maskIndex >= 0; maskIndex--)
        {
            shiftedValue = fs_back_val & RefNumber.LONG_HIGH_BIT_MASK[maskIndex];
            shiftedValue >>= maskIndex;
            if (this._highBit)
            {
                shiftedValue |= (1L << ((64 - maskIndex) - 1));
            }
            if (shiftedValue >= divide._val || shiftedValue < 0L)
            {
                break;
            }
        }
        
        if (!returnRemained)
        {
        	this._val = 1L << maskIndex;
        }
        this._highBit = false;
        shiftedValue -= divide._val; //This gets the non-highbit set value. This can be divided like normal... after some work.

        //Get the non-processed value and append to the shiftedValue
        shiftedValue <<= maskIndex;
        shiftedValue |= (fs_back_val & RefNumber.LONG_LOW_BIT_MASK[maskIndex]);

        //Divide and take the result and append to _val
        if (returnRemained)
        {
        	this._val = shiftedValue % divide._val;
        }
        else
        {
        	shiftedValue /= divide._val;
        	this._val |= shiftedValue;
        }
	}
}

