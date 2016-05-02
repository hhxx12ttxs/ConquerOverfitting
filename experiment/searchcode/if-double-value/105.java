<<<<<<< HEAD
 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/


package org.pentaho.di.compatibility;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueDataUtil;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.w3c.dom.Node;


/**
 * This class is one of the core classes of the Kettle framework.
 * It contains everything you need to manipulate atomic data (Values/Fields/...)
 * and to describe it in the form of meta-data. (name, length, precision, etc.)
 *
 * @author Matt
 * @since Beginning 2003
 */
public class Value implements Cloneable, XMLInterface, Serializable
{
    public static final String XML_TAG = "value";

    private static final long serialVersionUID = -6310073485210258622L;

    /**
	 * Value type indicating that the value has no type set.
	 */
	public static final int VALUE_TYPE_NONE       = 0;
	/**
	 * Value type indicating that the value contains a floating point double precision number.
	 */
	public static final int VALUE_TYPE_NUMBER      = 1;
	/**
	 * Value type indicating that the value contains a text String.
	 */
	public static final int VALUE_TYPE_STRING      = 2;
	/**
	 * Value type indicating that the value contains a Date.
	 */
	public static final int VALUE_TYPE_DATE        = 3;
	/**
	 * Value type indicating that the value contains a boolean.
	 */
	public static final int VALUE_TYPE_BOOLEAN     = 4;
	/**
	 * Value type indicating that the value contains a long integer.
	 */
	public static final int VALUE_TYPE_INTEGER     = 5;
    /**
     * Value type indicating that the value contains a floating point precision number with arbitrary precision.
     */
    public static final int VALUE_TYPE_BIGNUMBER   = 6;
    /**
     * Value type indicating that the value contains an Object.
     */
    public static final int VALUE_TYPE_SERIALIZABLE= 7;
    /**
     * Value type indicating that the value contains binary data:
     * BLOB, CLOB, ...
     */
    public static final int VALUE_TYPE_BINARY      = 8;

	/**
	 * The descriptions of the value types.
	 */
	private static final String valueTypeCode[]=
		{
			"-",                                                          // $NON-NLS-1$
			"Number", "String", "Date", "Boolean", "Integer", "BigNumber", "Serializable", "Binary" // $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$ $NON-NLS-4$ $NON-NLS-5$ $NON-NLS-6$ $NON-NLS-7$
		};


	private ValueInterface value;

	private String      name;
	private String      origin;
	private boolean     NULL;

	/**
	 * Constructs a new Value of type EMPTY
	 *
	 */
	public Value()
	{
		// clearValue();
	}

	/**
	 * Constructs a new Value with a name.
	 * 
	 * @param name Sets the name of the Value
	 */
	public Value(String name)
	{
		// clearValue();
		setName(name);
	}

	/**
	 * Constructs a new Value with a name and a type.
	 * 
	 * @param name Sets the name of the Value
	 * @param val_type Sets the type of the Value (Value.VALUE_TYPE_*)
	 */
	public Value(String name, int val_type)
	{
		// clearValue();
		newValue(val_type);
		setName(name);
	}

	/**
	 * This method allocates a new value of the appropriate type..
	 * @param val_type The new type of value
	 */
	private void newValue(int val_type)
	{
		switch(val_type)
		{
		case VALUE_TYPE_INTEGER  : value = new ValueInteger(); break;
		case VALUE_TYPE_STRING   : value = new ValueString(); break;
		case VALUE_TYPE_DATE     : value = new ValueDate(); break;
		case VALUE_TYPE_NUMBER   : value = new ValueNumber(); break;
		case VALUE_TYPE_BOOLEAN  : value = new ValueBoolean(); break;
        case VALUE_TYPE_BIGNUMBER: value = new ValueBigNumber(); break;
        case VALUE_TYPE_BINARY   : value = new ValueBinary(); break;
		default: value = null;
		}
	}

	/**
	 * Convert the value to another type.  This only works if a value has been set previously.
	 * That is the reason this method is private. Rather, use the public method setType(int type).
	 *
	 * @param valType The type to convert to.
	 */
	private void convertTo(int valType)
	{
		if (value!=null)
		{
			switch(valType)
			{
			case VALUE_TYPE_NUMBER    : value = new ValueNumber(value.getNumber()); break;
			case VALUE_TYPE_STRING    : value = new ValueString(value.getString()); break;
			case VALUE_TYPE_DATE      : value = new ValueDate(value.getDate()); break;
			case VALUE_TYPE_BOOLEAN   : value = new ValueBoolean(value.getBoolean()); break;
			case VALUE_TYPE_INTEGER   : value = new ValueInteger(value.getInteger()); break;
            case VALUE_TYPE_BIGNUMBER : value = new ValueBigNumber(value.getBigNumber()); break;
            case VALUE_TYPE_BINARY    : value = new ValueBinary(value.getBytes()); break;
			default: value = null;
			}
		}
	}

	/**
	 * Constructs a new Value with a name, a type, length and precision.
	 *
	 * @param name Sets the name of the Value
	 * @param valType Sets the type of the Value (Value.VALUE_TYPE_*)
	 * @param length The length of the value
	 * @param precision The precision of the value
	 */
	public Value(String name, int valType, int length, int precision)
	{
		this(name, valType);
		setLength(length, precision);
	}

    /**
     * Constructs a new Value of Type VALUE_TYPE_BIGNUMBER, with a name, containing a BigDecimal number
     *
     * @param name Sets the name of the Value
     * @param bignum The number to store in this Value
     */
    public Value(String name, BigDecimal bignum)
    {
        // clearValue();
        setValue(bignum);
        setName(name);
    }


    /**
	 * Constructs a new Value of Type VALUE_TYPE_NUMBER, with a name, containing a number
	 *
	 * @param name Sets the name of the Value
	 * @param num The number to store in this Value
	 */
	public Value(String name, double num)
	{
		// clearValue();
		setValue(num);
		setName(name);
	}

	/**
	 * Constructs a new Value of Type VALUE_TYPE_STRING, with a name, containing a String
	 * 
	 * @param name Sets the name of the Value
	 * @param str The text to store in this Value
	 */
	public Value(String name, StringBuffer str)
	{
		this(name, str.toString());
	}

	/**
	 * Constructs a new Value of Type VALUE_TYPE_STRING, with a name, containing a String
	 *
	 * @param name Sets the name of the Value
	 * @param str The text to store in this Value
	 */
	public Value(String name, String str)
	{
		// clearValue();
		setValue(str);
		setName(name);
	}

	/**
	 * Constructs a new Value of Type VALUE_TYPE_DATE, with a name, containing a Date
	 *
	 * @param name Sets the name of the Value
	 * @param dat The date to store in this Value
	 */
	public Value(String name, Date dat)
	{
		// clearValue();
		setValue(dat);
		setName(name);
	}

	/**
	 * Constructs a new Value of Type VALUE_TYPE_BOOLEAN, with a name, containing a boolean value
	 *
	 * @param name Sets the name of the Value
	 * @param bool The boolean to store in this Value
	 */
	public Value(String name, boolean bool)
	{
		// clearValue();
		setValue(bool);
		setName(name);
	}

	/**
	 * Constructs a new Value of Type VALUE_TYPE_INTEGER, with a name, containing an integer number
	 *
	 * @param name Sets the name of the Value
	 * @param l The integer to store in this Value
	 */
	public Value(String name, long l)
	{
		// clearValue();
		setValue(l);
		setName(name);
	}

	/**
	 * Constructs a new Value as a copy of another value and renames it...
	 *
	 * @param name The new name of the copied Value
	 * @param v The value to be copied
	 */
	public Value(String name, Value v)
	{
		this(v);
		setName(name);
	}

	/**
	 * Constructs a new Value of Type VALUE_TYPE_BINARY, with a name, containing a bytes value
	 *
	 * @param name Sets the name of the Value
	 * @param b The bytes to store in this Value
	 */
	public Value(String name, byte[] b)
	{
		clearValue();
		setValue(b);
		setName(name);
	}
	
	/**
	 * Constructs a new Value as a copy of another value
	 *
	 * @param v The Value to be copied
	 */
	public Value(Value v)
	{
		if (v!=null)
		{
			// setType(v.getType()); // Is this really needed???
			value = v.getValueCopy();
			setName(v.getName());
			setLength(v.getLength(), v.getPrecision());
			setNull(v.isNull());
			setOrigin(v.origin);
		}
		else
		{
			clearValue();
            setNull(true);
		}
	}

	public Object clone()
	{
		Value retval = null;
		try
		{
			retval = (Value)super.clone();
            if (value!=null) retval.value = (ValueInterface) value.clone();
		}
		catch(CloneNotSupportedException e)
		{
			retval=null;
		}
		return retval;
	}		

	/**
	 * Build a copy of this Value
	 * @return a copy of another value
	 *
	 */
	public Value Clone()
	{
		Value v = new Value(this);
		return v;
	}

	/**
	 * Clears the content and name of a Value 
	 */
	public void clearValue()
	{
		value    = null;
		name     = null;
		NULL     = false;
		origin  = null;
	}

	private ValueInterface getValueCopy()
	{
		if (value==null) return null;
		return (ValueInterface)value.clone();
	}

	/**
	 * Sets the name of a Value
	 *
	 * @param name The new name of the value
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Obtain the name of a Value
	 *
	 * @return The name of the Value
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * This method allows you to set the origin of the Value by means of the name of the originating step.
	 *
	 * @param step_of_origin The step of origin.
	 */
	public void setOrigin(String step_of_origin)
	{
		origin = step_of_origin;
	}

	/**
	 * Obtain the origin of the step.
	 *
	 * @return The name of the originating step
	 */
	public String getOrigin()
	{
		return origin;
	}

    /**
     * Sets the value to a BigDecimal number value.
     * @param num The number value to set the value to
     */
    public void setValue(BigDecimal num)
    {
        if (value==null || value.getType()!=VALUE_TYPE_BIGNUMBER)  value = new ValueBigNumber(num);
        else value.setBigNumber(num);

        setNull(false);
    }

	/**
	 * Sets the value to a double Number value.
	 * @param num The number value to set the value to
	 */
	public void setValue(double num)
	{
		if (value==null || value.getType()!=VALUE_TYPE_NUMBER)  value = new ValueNumber(num);
		else value.setNumber(num);
		setNull(false);
	}

	/**
	 * Sets the Value to a String text
	 * @param str The StringBuffer to get the text from
	 */
	public void setValue(StringBuffer str)
	{
		if (value==null || value.getType()!=VALUE_TYPE_STRING)  value = new ValueString(str.toString());
		else value.setString(str.toString());
		setNull(str==null);
	}

	/**
	 * Sets the Value to a String text
	 * @param str The String to get the text from
	 */
	public void setValue(String str)
	{
		if (value==null || value.getType()!=VALUE_TYPE_STRING)  value = new ValueString(str);
		else value.setString(str);
		setNull(str==null);
	}

    public void setSerializedValue(Serializable ser) {
        if (value==null || value.getType()!=VALUE_TYPE_SERIALIZABLE)  value = new ValueSerializable(ser);
        else value.setSerializable(ser);
        setNull(ser==null);
    }
   
	/**
	 * Sets the Value to a Date
	 * @param dat The Date to set the Value to
	 */
	public void setValue(Date dat)
	{
		if (value==null || value.getType()!=VALUE_TYPE_DATE)  value = new ValueDate(dat);
		else value.setDate(dat);
		setNull(dat==null);
	}

	/**
	 * Sets the Value to a boolean
	 * @param bool The boolean to set the Value to
	 */
	public void setValue(boolean bool)
	{
		if (value==null || value.getType()!=VALUE_TYPE_BOOLEAN)  value = new ValueBoolean(bool);
		else value.setBoolean(bool);
		setNull(false);
	}
    
    public void setValue(Boolean b)
    {
        setValue(b.booleanValue());
    }


	/**
	 * Sets the Value to a long integer
	 * @param b The byte to convert to a long integer to which the Value is set.
	 */
	public void setValue(byte b)
	{
		setValue((long)b);
	}

	/**
	 * Sets the Value to a long integer
	 * @param i The integer to convert to a long integer to which the Value is set.
	 */
	public void setValue(int i)
	{
		setValue((long)i);
	}

	/**
	 * Sets the Value to a long integer
	 * @param l The long integer to which the Value is set.
	 */
	public void setValue(long l)
	{
		if (value==null || value.getType()!=VALUE_TYPE_INTEGER)  value = new ValueInteger(l);
		else value.setInteger(l);
		setNull(false);
	}

	/**
	 * Sets the Value to a byte array
	 * @param b The byte array to which the Value has to be set.
	 */
	public void setValue(byte[] b)
	{
		if (value==null || value.getType()!=VALUE_TYPE_BINARY)  value = new ValueBinary(b);
		else value.setBytes(b);
		
		if ( b == null )
	        setNull(true);
		else
		    setNull(false);
	}
	
	
	/**
	 * Copy the Value from another Value.
	 * It doesn't copy the name.
	 * @param v The Value to copy the settings and value from
	 */
	public void setValue(Value v)
	{
		if (v!=null)
		{
			value = v.getValueCopy();
			setNull(v.isNull());
			setOrigin(v.origin);
		}
		else
		{
			clearValue();
		}
	}

    /**
     * Get the BigDecimal number of this Value.
     * If the value is not of type BIG_NUMBER, a conversion is done first.
     * @return the double precision floating point number of this Value.
     */
    public BigDecimal getBigNumber()
    {
        if (value==null || isNull()) return null;
        return value.getBigNumber();
    }

	/**
	 * Get the double precision floating point number of this Value.
	 * If the value is not of type NUMBER, a conversion is done first. 
	 * @return the double precision floating point number of this Value.
	 */
	public double getNumber()
	{
		if (value==null || isNull()) return 0.0;
		return value.getNumber();
	}

	/**
	 * Get the String text representing this value.
	 * If the value is not of type STRING, a conversion if done first.
	 * @return the String text representing this value.
	 */
	public String getString()
	{
		if (value==null || isNull()) return null;
		return value.getString();
	}

	/**
	 * Get the length of the String representing this value.
	 * @return the length of the String representing this value.
	 */
	public int getStringLength()
	{
		String s = getString();
		if (s==null) return 0;
		return s.length();
	}

	/**
	 * Get the Date of this Value.
	 * If the Value is not of type DATE, a conversion is done first.
	 * @return the Date of this Value.
	 */
	public Date getDate()
	{
		if (value==null || isNull()) return null;
		return value.getDate();
	}
  
    /**
     * Get the Serializable of this Value.
     * If the Value is not of type Serializable, it returns null.
     * @return the Serializable of this Value.
     */
    public Serializable getSerializable()
    {
        if (value==null || isNull() || value.getType() != VALUE_TYPE_SERIALIZABLE) return null;
        return value.getSerializable();
    }

	/**
	 * Get the boolean value of this Value.
	 * If the Value is not of type BOOLEAN, it will be converted.
	 * <p>Strings: "YES", "Y", "TRUE" (case insensitive) to true, the rest false
	 * <p>Number: 0.0 is false, the rest is true.
	 * <p>Integer: 0 is false, the rest is true.
	 * <p>Date: always false.
	 * @return the boolean representation of this Value.
	 */
	public boolean getBoolean()
	{
		if (value==null || isNull()) return false;
		return value.getBoolean();
	}

	
	/**
	 * Get the long integer representation of this value.
	 * If the Value is not of type INTEGER, it will be converted:
	 * <p>String: try to convert to a long value, 0L if it didn't work.
	 * <p>Number: round the double value and return the resulting long integer.
	 * <p>Date: return the number of miliseconds after <code>1970:01:01 00:00:00</code>
	 * <p>Date: always false.
	 *
	 * @return the long integer representation of this value.
	 */
	public long getInteger()
	{
		if (value==null || isNull()) return 0L;
		return value.getInteger();
	}

	public byte[] getBytes()
	{
		if (value==null || isNull()) return null;
		return value.getBytes();
	}
	
	/**
	 * Set the type of this Value
	 * @param val_type The type to which the Value will be set.
	 */
	public void setType(int val_type)
	{
		if (value==null) newValue(val_type);
		else // Convert the value to the appropriate type...
		{
			convertTo(val_type);
		}
	}

	/**
	 * Returns the type of this Value
	 * @return the type of this Value
	 */
	public int getType()
	{
		if (value==null) return VALUE_TYPE_NONE;
		return value.getType();
	}

	/**
	 * Checks whether or not this Value is empty.
	 * A value is empty if it has the type VALUE_TYPE_EMPTY
	 * @return true if the value is empty.
	 */
	public boolean isEmpty()
	{
		if (value==null) return true;
		return false;
	}

	/**
	 * Checks wheter or not the value is a String.
	 * @return true if the value is a String.
	 */
	public boolean isString()
	{
		if (value==null) return false;
		return value.getType()==VALUE_TYPE_STRING;
	}

	/**
	 * Checks whether or not this value is a Date
	 * @return true if the value is a Date
	 */
	public boolean isDate()
	{
		if (value==null) return false;
		return value.getType()==VALUE_TYPE_DATE;
	}

    /**
     * Checks whether or not the value is a Big Number
     * @return true is this value is a big number
     */
    public boolean isBigNumber()
    {
        if (value==null) return false;
        return value.getType()==VALUE_TYPE_BIGNUMBER;
    }

	/**
	 * Checks whether or not the value is a Number
	 * @return true is this value is a number
	 */
	public boolean isNumber()
	{
		if (value==null) return false;
		return value.getType()==VALUE_TYPE_NUMBER;
	}

	/**
	 * Checks whether or not this value is a boolean
	 * @return true if this value has type boolean.
	 */
	public boolean isBoolean()
	{
		if (value==null) return false;
		return value.getType()==VALUE_TYPE_BOOLEAN;
	}

    /**
     * Checks whether or not this value is of type Serializable
     * @return true if this value has type Serializable
     */
    public boolean isSerializableType() {
        if(value == null) {
            return false;
        }
        return value.getType() == VALUE_TYPE_SERIALIZABLE;
    }

    /**
     * Checks whether or not this value is of type Binary
     * @return true if this value has type Binary
     */
    public boolean isBinary() {
    	// Serializable is not included here as it used for
    	// internal purposes only.
        if(value == null) {
            return false;
        }
        return value.getType() == VALUE_TYPE_BINARY;
    }   
    
	/**
	 * Checks whether or not this value is an Integer
	 * @return true if this value is an integer
	 */
	public boolean isInteger()
	{
		if (value==null) return false;
		return value.getType()==VALUE_TYPE_INTEGER;
	}

	/**
	 * Checks whether or not this Value is Numeric
	 * A Value is numeric if it is either of type Number or Integer
	 * @return true if the value is either of type Number or Integer
	 */
	public boolean isNumeric()
	{
		return isInteger() || isNumber() || isBigNumber();
	}
	
	/**
	 * Checks whether or not the specified type is either Integer or Number
	 * @param t the type to check
	 * @return true if the type is Integer or Number
	 */
	public static final boolean isNumeric(int t)
	{
		return t==VALUE_TYPE_INTEGER || t==VALUE_TYPE_NUMBER || t==VALUE_TYPE_BIGNUMBER;
	}

	/**
	 * Returns a padded to length String text representation of this Value
	 * @return a padded to length String text representation of this Value
	 */
	public String toString()
	{
		return toString(true);
	}

	/**
	 * a String text representation of this Value, optionally padded to the specified length
	 * @param pad true if you want to pad the resulting String
	 * @return a String text representation of this Value, optionally padded to the specified length
	 */
	public String toString(boolean pad)
	{
		String retval;

		switch(getType())
		{
		case VALUE_TYPE_STRING :  retval=toStringString(pad);  break;
		case VALUE_TYPE_INTEGER:  retval=toStringInteger(pad); break;
		case VALUE_TYPE_NUMBER :  retval=toStringNumber(pad);  break;
		case VALUE_TYPE_DATE   :  retval=toStringDate();    break;
		case VALUE_TYPE_BOOLEAN:  retval=toStringBoolean(); break;
        case VALUE_TYPE_BIGNUMBER: retval=toStringBigNumber(); break;
        case VALUE_TYPE_BINARY :  retval=toStringBinary(); break;
		default: retval=""; break; 
		}

		return retval;
	}

	/**
	 * a String text representation of this Value, optionally padded to the specified length
	 * @return a String text representation of this Value, optionally padded to the specified length
	 */
	public String toStringMeta()
	{
		// We (Sven Boden) did explicit performance testing for this
		// part. The original version used Strings instead of StringBuffers,
		// performance between the 2 does not differ that much. A few milliseconds
		// on 100000 iterations in the advantage of StringBuffers. The
		// lessened creation of objects may be worth it in the long run.
		StringBuffer retval=new StringBuffer(getTypeDesc());

		switch(getType())
		{
		case VALUE_TYPE_STRING :
			if (getLength()>0) retval.append('(').append(getLength()).append(')');  
			break;
		case VALUE_TYPE_NUMBER :
        case VALUE_TYPE_BIGNUMBER :
			if (getLength()>0)
			{
				retval.append('(').append(getLength());
				if (getPrecision()>0)
				{
					retval.append(", ").append(getPrecision());
				}
				retval.append(')');
			}
			break;
		case VALUE_TYPE_INTEGER:
			if (getLength()>0)
			{
				retval.append('(').append(getLength()).append(')');
			}
			break;
		default: break;
		}

		return retval.toString();
	}


	/**
	 * Converts a String Value to String optionally padded to the specified length.
	 * @param pad true if you want to pad the resulting string to length.
	 * @return a String optionally padded to the specified length.
	 */
	private String toStringString(boolean pad)
	{
		String retval=null;

		if (value==null) return null;

		if (value.getLength()<=0)  // No length specified!
		{
			if (isNull() || value.getString()==null)
				retval = Const.NULL_STRING;
			else
				retval = value.getString();
		}
		else
		{
            if (pad)
            {
                StringBuffer ret;

                if (isNull() || value.getString()==null) ret=new StringBuffer(Const.NULL_STRING);
                else          ret=new StringBuffer(value.getString());

                int length = value.getLength();
                if (length>16384) length=16384; // otherwise we get OUT OF MEMORY errors for CLOBS.
                Const.rightPad(ret, length);

                retval=ret.toString();
            }
            else
            {
                if (isNull() || value.getString()==null)
                {
                    retval=Const.NULL_STRING;
                }
                else
                {
                    retval = value.getString();
                }
            }
		}
		return retval;
	}
		
	/**
	 * Converts a Number value to a String, optionally padding the result to the specified length.
     * @param pad true if you want to pad the resulting string to length.
	 * @return a String optionally padded to the specified length.
	 */
	private String toStringNumber(boolean pad)
	{
		String retval;

		if (value==null) return null;

		if (pad)
		{
			if (value.getLength()<1)
			{
				if (isNull()) retval=Const.NULL_NUMBER;
				else
				{
					DecimalFormat form= new DecimalFormat();
                    form.applyPattern(" ##########0.0########;-#########0.0########");
                    // System.out.println("local.pattern = ["+form.toLocalizedPattern()+"]");
                    retval=form.format(value.getNumber());
				}
			}
			else
			{
				if (isNull())
				{
					StringBuffer ret=new StringBuffer(Const.NULL_NUMBER);
					Const.rightPad(ret, value.getLength());
					retval=ret.toString();
				}
				else
				{
					StringBuffer fmt=new StringBuffer();
					int i;
					DecimalFormat form;

					if (value.getNumber()>=0) fmt.append(' '); // to compensate for minus sign.

					if (value.getPrecision()<0)  // Default: two decimals
					{
						for (i=0;i<value.getLength();i++) fmt.append('0');
						fmt.append(".00"); // for the .00
					}
					else  // Floating point format   00001234,56  --> (12,2)
					{
						for (i=0;i<=value.getLength();i++) fmt.append('0'); // all zeroes.
						int pos = value.getLength()-value.getPrecision()+1-(value.getNumber()<0?1:0);
						if (pos>=0 && pos <fmt.length())
						{
							fmt.setCharAt(value.getLength()-value.getPrecision()+1-(value.getNumber()<0?1:0), '.'); // one 'comma'
						}
					}
					form= new DecimalFormat(fmt.toString());
					retval=form.format(value.getNumber());
				}
			}
		}
		else
		{
			if (isNull()) retval=Const.NULL_NUMBER;
			else retval=Double.toString(value.getNumber());
		}
		return retval;
	}

	/**
	 * Converts a Date value to a String.
	 * The date has format: <code>yyyy/MM/dd HH:mm:ss.SSS</code>
	 * @return a String representing the Date Value.
	 */
	private String toStringDate()
	{
		String retval;
		if (value==null) return null;

		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", Locale.US);

		if (isNull() || value.getDate()==null) retval=Const.NULL_DATE;
		else
		{
			retval=df.format(value.getDate()).toString();
		}

		/*
		   This code was removed as TYPE_VALUE_DATE does not know "length", so this
		   could never be called anyway
		else
		{
			StringBuffer ret;
			if (isNull() || value.getDate()==null)
				 ret=new StringBuffer(Const.NULL_DATE);
			else ret=new StringBuffer(df.format(value.getDate()).toString());
			Const.rightPad(ret, getLength()<=10?10:getLength());
			retval=ret.toString();
		}
		*/
		return retval;
	}

	/**
	 * Returns a String representing the boolean value.
	 * It will be either "true" or "false".
	 * 
	 * @return a String representing the boolean value.
	 */
	private String toStringBoolean()
	{
		// Code was removed from this method as ValueBoolean
		// did not store length, so some parts could never be
		// called.
		String retval;
		if (value==null) return null;

		if (isNull())
		{
			retval=Const.NULL_BOOLEAN;
		}
		else
		{
			retval=value.getBoolean()?"true":"false";
		}

		return retval;
	}

	/**
	 * Converts an Integer value to a String, optionally padding the result to the specified length.
     * @param pad true if you want to pad the resulting string to length.
	 * @return a String optionally padded to the specified length.
	 */
	private String toStringInteger(boolean pad)
	{
		String retval;
		if (value==null) return null;

		if (getLength()<1)
		{
			if (isNull()) retval=Const.NULL_INTEGER;
			else
			{
				DecimalFormat form= new DecimalFormat(" ###############0;-###############0");
				retval=form.format(value.getInteger());
			}
		}
		else
		{
			if (isNull())
			{
				StringBuffer ret=new StringBuffer(Const.NULL_INTEGER);
				Const.rightPad(ret, getLength());
				retval=ret.toString();
			}
			else
			{
                if (pad)
                {
    				StringBuffer fmt=new StringBuffer();
    				int i;
    				DecimalFormat form;
    	
    				if (value.getInteger()>=0) fmt.append(' '); // to compensate for minus sign.
    
    				int len = getLength();
    				for (i=0;i<len;i++) fmt.append('0'); // all zeroes.
    				
    				form= new DecimalFormat(fmt.toString());
    				retval=form.format(value.getInteger());
                }
                else
                {
                    retval = Long.toString(value.getInteger());
                }
			}
		}
		return retval;
	}

    /**
     * Converts a BigNumber value to a String, optionally padding the result to the specified length. 
     * @param pad true if you want to pad the resulting string to length.
     * @return a String optionally padded to the specified length.
     */
    private String toStringBigNumber()
    {
        if (value==null) return null;
        String retval;
        
        if (isNull())
        {
            retval = Const.NULL_BIGNUMBER;
        }
        else
        {
            if (value.getBigNumber()==null)
            {
                retval=null;
            }
            else
            {
                retval = value.getString();
    
                // Localise . to ,
                if (Const.DEFAULT_DECIMAL_SEPARATOR!='.')
                {
                    retval = retval.replace('.', Const.DEFAULT_DECIMAL_SEPARATOR);
                }
            }
        }

        return retval;
    }

	/**
	 * Returns a String representing the binary value.
     *
	 * @return a String representing the binary value.
	 */
	private String toStringBinary()
	{
		String retval;
		if (value==null) return null;

		if (isNull() || value.getBytes() == null)
		{
			retval=Const.NULL_BINARY;
		}
		else
		{
			retval = new String(value.getBytes());
		}

		return retval;
	}
    
    
	/**
	 * Sets the length of the Number, Integer or String to the specified length
	 * Note: no truncation of the value takes place, this is meta-data only!
	 * @param l the length to which you want to set the Value.
	 */
	public void setLength(int l)
	{
		if (value==null) return;
		value.setLength(l);
	}

	/**
	 * Sets the length and the precision of the Number, Integer or String to the specified length & precision
	 * Note: no truncation of the value takes place, this is meta-data only!
	 * @param l the length to which you want to set the Value.
	 * @param p the precision to which you want to set this Value
	 */
	public void setLength(int l, int p)
	{
		if (value==null) return;
		value.setLength(l,p);
	}

	/**
	 * Get the length of this Value.
	 * @return the length of this Value.
	 */
	public int getLength()
	{
		if (value==null) return -1;
		return value.getLength();
	}

	/**
	 * get the precision of this Value
	 * @return the precision of this Value.
	 */
	public int getPrecision()
	{
		if (value==null) return -1;
		return value.getPrecision();
	}

	/**
	 * Sets the precision of this Value
	 * Note: no rounding or truncation takes place, this is meta-data only!
	 * @param p the precision to which you want to set this Value.
	 */
	public void setPrecision(int p)
	{
		if (value==null) return;
		value.setPrecision(p);
	}

	/**
	 * Return the type of a value in a textual form: "String", "Number", "Integer", "Boolean", "Date", ...
	 * @return A String describing the type of value.
	 */
	public String getTypeDesc()
	{
		if (value==null) return "Unknown";
		return value.getTypeDesc();
	}

	/**
	 * Return the type of a value in a textual form: "String", "Number", "Integer", "Boolean", "Date", ... given a certain integer type
	 * @param t the type to convert to text.
	 * @return A String describing the type of a certain value.
	 */
	public static final String getTypeDesc(int t)
	{
		return valueTypeCode[t];
	}

	/**
	 * Convert the String description of a type to an integer type.
	 * @param desc The description of the type to convert
	 * @return The integer type of the given String.  (Value.VALUE_TYPE_...)
	 */
	public static final int getType(String desc)
	{
		int i;

		for (i=1;i<valueTypeCode.length;i++)
		{
			if (valueTypeCode[i].equalsIgnoreCase(desc))
			{
				return i; 
			}
		}

		return VALUE_TYPE_NONE;
	}

	/**
	 * get an array of String describing the possible types a Value can have.
	 * @return an array of String describing the possible types a Value can have.
	 */
	public static final String[] getTypes()
	{
		String retval[] = new String[valueTypeCode.length-1];
		System.arraycopy(valueTypeCode, 1, retval, 0, valueTypeCode.length-1);
		return retval;
	}

	/**
	 * Get an array of String describing the possible types a Value can have.
	 * @return an array of String describing the possible types a Value can have.
	 */
	public static final String[] getAllTypes()
	{
		String retval[] = new String[valueTypeCode.length];
		System.arraycopy(valueTypeCode, 0, retval, 0, valueTypeCode.length);
		return retval;
	}

	/**
	 * Sets the Value to null, no type is being changed.
	 *
	 */
	public void setNull()
	{
		setNull(true);
	}

	/**
	 * Sets or unsets a value to null, no type is being changed.
	 * @param n true if you want the value to be null, false if you don't want this to be the case.
	 */
	public void setNull(boolean n)
	{
		NULL=n;
	}

	/**
	 * Checks wheter or not a value is null.
	 * @return true if the Value is null.
	 */
	public boolean isNull()
	{
		return NULL;
	}

    /**
     * Write the object to an ObjectOutputStream
     * @param out
     * @throws IOException
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
        writeObj(new DataOutputStream(out));
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException
    {
        readObj(new DataInputStream(in));
    }

    public void writeObj(DataOutputStream dos) throws IOException
    {
        int type=getType();

        // Handle type
        dos.writeInt(getType());
        
        // Handle name-length
        dos.writeInt(name.length());  
        
        // Write name
        dos.writeChars(name);

        // length & precision
        dos.writeInt(getLength());
        dos.writeInt(getPrecision());

        // NULL?
        dos.writeBoolean(isNull());

        // Handle Content -- only when not NULL
        if (!isNull())
        {
            switch(type)
            {
            case VALUE_TYPE_STRING :
                if (getString()==null)
                {
                   dos.writeInt(-1); // -1 == null string 
                }
                else
                {
                    String string = getString();
                    byte[] chars = string.getBytes(Const.XML_ENCODING);
                    dos.writeInt(chars.length);
                    dos.write(chars);                
                }
                break;
            case VALUE_TYPE_BIGNUMBER:
                if (getBigNumber()==null)
                {
                    dos.writeInt(-1); // -1 == null string
                }
                else
                {
                    String string = getBigNumber().toString();
                    dos.writeInt(string.length());
                    dos.writeChars(string);
                }
                break;
            case VALUE_TYPE_DATE   :
                dos.writeBoolean(getDate()!=null);
                if (getDate()!=null)
                {
                    dos.writeLong(getDate().getTime());
                }
                break;
            case VALUE_TYPE_NUMBER :
                dos.writeDouble(getNumber());
                break;
            case VALUE_TYPE_BOOLEAN:
                dos.writeBoolean(getBoolean());
                break;
            case VALUE_TYPE_INTEGER:
                dos.writeLong(getInteger());
                break;
            default: break; // nothing
            }
        }
    }

	/**
	 * Write the value, including the meta-data to a DataOutputStream
	 * @param outputStream the OutputStream to write to .
	 * @throws KettleFileException if something goes wrong.
	 */
	public void write(OutputStream outputStream) throws KettleFileException
	{
		try
		{
            writeObj(new DataOutputStream(outputStream));
		}
		catch(Exception e)
		{
			throw new KettleFileException("Unable to write value to output stream", e);
		}
	}

    /**
     * Read the metadata and data for this Value object from the specified data input stream
     * @param dis
     * @throws IOException
     */
    public void readObj(DataInputStream dis) throws IOException
    {
        // type
        int theType = dis.readInt(); 
        newValue(theType);

        // name-length
        int nameLength=dis.readInt();  

        // name
        StringBuffer nameBuffer=new StringBuffer();
        for (int i=0;i<nameLength;i++) nameBuffer.append(dis.readChar());
        setName(new String(nameBuffer));

        // length & precision
        setLength(dis.readInt(), dis.readInt()); 

        // Null?
        setNull(dis.readBoolean());

        // Read the values
        if (!isNull())
        {
            switch(getType())
            {
            case VALUE_TYPE_STRING:
                // read the length
                int stringLength = dis.readInt();
                if (stringLength<0)
                {
                    setValue((String)null);
                }
                else
                {
                    byte chars[] = new byte[stringLength];
                    dis.readFully(chars);
                    setValue(new String(chars, Const.XML_ENCODING));
                }
                break;
            case VALUE_TYPE_BIGNUMBER:
                // read the length
                int bnLength = dis.readInt();
                if (bnLength<0)
                {
                    setValue((BigDecimal)null);
                }
                else
                {
                    StringBuffer buffer = new StringBuffer();
                    for (int i=0;i<bnLength;i++) buffer.append( dis.readChar() );
                    setValue(buffer.toString());
                    try
                    {
                        convertString(VALUE_TYPE_BIGNUMBER);
                    }
                    catch(KettleValueException e)
                    {
                        throw new IOException("Unable to convert String to BigNumber while reading from data input stream ["+getString()+"]");
                    }
                }
                break;
            case VALUE_TYPE_DATE:
                if (dis.readBoolean())
                {
                    setValue(new Date(dis.readLong()));
                }
                break;
            case VALUE_TYPE_NUMBER:
                setValue(dis.readDouble());
                break;
            case VALUE_TYPE_INTEGER:
                setValue(dis.readLong());
                break;
            case VALUE_TYPE_BOOLEAN:
                setValue(dis.readBoolean());
                break;
            default: break;
            }
        }
    }

    /**
     * Read the Value, including meta-data from a DataInputStream
     * @param is The InputStream to read the value from
     * @throws KettleFileException when the Value couldn't be created by reading it from the DataInputStream.
     */
	public Value(InputStream is) throws KettleFileException
	{
		try
		{
            readObj(new DataInputStream(is));
		}
		catch(EOFException e)
		{
			throw new KettleEOFException("End of file reached", e);
		}
		catch(Exception e)
		{
			throw new KettleFileException("Error reading from data input stream", e);
		}
	}

	/**
	 * Write the data of this Value, without the meta-data to a DataOutputStream
	 * @param dos The DataOutputStream to write the data to
	 * @return true if all went well, false if something went wrong.
	 */
	public boolean writeData(DataOutputStream dos) throws KettleFileException
	{
		try
		{
			// Is the value NULL?
			dos.writeBoolean(isNull());

			// Handle Content -- only when not NULL
			if (!isNull())
			{
				switch(getType())
				{
                case VALUE_TYPE_STRING :
                    if (getString()==null)
                    {
                       dos.writeInt(-1); // -1 == null string 
                    }
                    else
                    {
                        String string = getString();
                        byte[] chars = string.getBytes(Const.XML_ENCODING);
                        dos.writeInt(chars.length);
                        dos.write(chars); 
                    }
                    break;
                case VALUE_TYPE_BIGNUMBER:
                    if (getBigNumber()==null)
                    {
                        dos.writeInt(-1); // -1 == null big number
                    }
                    else
                    {
                        String string = getBigNumber().toString();
                        dos.writeInt(string.length());
                        dos.writeChars(string);
                    }
                    break;
				case VALUE_TYPE_DATE   :
					dos.writeBoolean(getDate()!=null);
					if (getDate()!=null)
					{
						dos.writeLong(getDate().getTime());
					}
					break;
				case VALUE_TYPE_NUMBER :
					dos.writeDouble(getNumber());
					break;
				case VALUE_TYPE_BOOLEAN:
					dos.writeBoolean(getBoolean());
					break;
				case VALUE_TYPE_INTEGER:
                    dos.writeLong(getInteger());
					break;
				default: break; // nothing
				}
			}
		}
		catch(IOException e)
		{
			throw new KettleFileException("Unable to write value data to output stream", e);
		}
        

		return true;
	}

	/**
	 * Read the data of a Value from a DataInputStream, the meta-data of the value has to be set before calling this method!
	 * @param dis the DataInputStream to read from
	 * @throws KettleFileException when the value couldn't be read from the DataInputStream
	 */
	public Value(Value metaData, DataInputStream dis) throws KettleFileException
	{
		setValue(metaData);
		setName(metaData.getName());

		try
		{
			// Is the value NULL?
			setNull(dis.readBoolean());

			// Read the values
			if (!isNull())
			{
				switch(getType())
				{
                case VALUE_TYPE_STRING:
                    // read the length
                    int stringLength = dis.readInt();
                    if (stringLength<0)
                    {
                        setValue((String)null);
                    }
                    else
                    {
                        byte chars[] = new byte[stringLength];
                        dis.readFully(chars);
                        setValue(new String(chars, Const.XML_ENCODING));
                    }
                    break;
                case VALUE_TYPE_BIGNUMBER:
                    // read the length
                    int bnLength = dis.readInt();
                    if (bnLength<0)
                    {
                        setValue((BigDecimal)null);
                    }
                    else
                    {
                        StringBuffer buffer = new StringBuffer();
                        for (int i=0;i<bnLength;i++) buffer.append( dis.readChar() );
                        setValue(buffer.toString());
                        try
                        {
                            convertString(VALUE_TYPE_BIGNUMBER);
                        }
                        catch(KettleValueException e)
                        {
                            throw new IOException("Unable to convert String to BigNumber while reading from data input stream ["+getString()+"]");
                        }
                    }
                    break;
                case VALUE_TYPE_DATE:
					if (dis.readBoolean())
					{
						setValue(new Date(dis.readLong()));
					}
					break;
				case VALUE_TYPE_NUMBER:
					setValue(dis.readDouble());
					break;
				case VALUE_TYPE_INTEGER:
                    setValue(dis.readLong());
					break;
				case VALUE_TYPE_BOOLEAN:
					setValue(dis.readBoolean());
					break;
                default: break;
				}
			}
		}
		catch(EOFException e)
		{
			throw new KettleEOFException("End of file reached while reading value", e);
		}
		catch(Exception e)
		{
			throw new KettleEOFException("Error reading value data from stream", e);
		}
	}

    /**
     * Compare 2 values of the same or different type!
     * The comparison of Strings is case insensitive
     * @param v the value to compare with.
     * @return -1 if The value was smaller, 1 bigger and 0 if both values are equal.
     */
    public int compare(Value v)
    {
        return compare(v, true);
    }

	/**
	 * Compare 2 values of the same or different type!
	 * @param v the value to compare with.
     * @param caseInsensitive True if you want the comparison to be case insensitive
	 * @return -1 if The value was smaller, 1 bigger and 0 if both values are equal.
	 */
	public int compare(Value v, boolean caseInsensitive)
	{
		boolean n1 = isNull() || (isString() && (getString()==null || getString().length()==0)) || (isDate() && getDate()==null) || (isBigNumber() && getBigNumber()==null);
		boolean n2 = v.isNull() || (v.isString() && (v.getString()==null || v.getString().length()==0)) || (v.isDate() && v.getDate()==null) || (v.isBigNumber() && v.getBigNumber()==null);

		// null is always smaller!
		if ( n1 && !n2) return -1;
		if (!n1 &&  n2) return  1;
		if ( n1 &&  n2) return  0;

		switch(getType())
		{
		case VALUE_TYPE_STRING:
        {
			String one = Const.rtrim(getString());
			String two = Const.rtrim(v.getString());

			int cmp=0;
            if (caseInsensitive)
            {
                cmp = one.compareToIgnoreCase(two);
            }
            else
            {
                cmp = one.compareTo(two);
            }

            return cmp;
        }

		case VALUE_TYPE_INTEGER:
        {
		    return Double.compare(getNumber(), v.getNumber());
        }

		case VALUE_TYPE_DATE   :
        {
		    return Double.compare(getNumber(), v.getNumber());
        }        

		case VALUE_TYPE_BOOLEAN:
		    {
    			if (getBoolean() &&  v.getBoolean() || 
    			    !getBoolean() && !v.getBoolean()) return  0;  // true == true, false == false
    			if (getBoolean() && !v.getBoolean()) return  1;  // true  > false
    			return -1;  // false < true
            }

		case VALUE_TYPE_NUMBER :
            {
			    return Double.compare(getNumber(), v.getNumber());
            }

        case VALUE_TYPE_BIGNUMBER:
            {
                return getBigNumber().compareTo(v.getBigNumber());
            }
		}

		// Still here?  Not possible!  But hey, give back 0, mkay?
		return 0;
	}

	public boolean equals(Object v)
	{
		if (compare((Value)v)==0)
			return true;
		else
			return false;
	}

	/**
	 * Check whether this value is equal to the String supplied.
	 * @param string The string to check for equality
	 * @return true if the String representation of the value is equal to string. (ignoring case)
	 */
	public boolean isEqualTo(String string)
	{
		return getString().equalsIgnoreCase(string);
	}

    /**
     * Check whether this value is equal to the BigDecimal supplied.
     * @param number The BigDecimal to check for equality
     * @return true if the BigDecimal representation of the value is equal to number.
     */
    public boolean isEqualTo(BigDecimal number)
    {
        return getBigNumber().equals(number);
    }

	/**
	 * Check whether this value is equal to the Number supplied.
	 * @param number The Number to check for equality
	 * @return true if the Number representation of the value is equal to number.
	 */
	public boolean isEqualTo(double number)
	{
		return getNumber() == number;
	}

	/**
	 * Check whether this value is equal to the Integer supplied.
	 * @param number The Integer to check for equality
	 * @return true if the Integer representation of the value is equal to number.
	 */
	public boolean isEqualTo(long number)
	{
		return getInteger() == number;
	}

	/**
	 * Check whether this value is equal to the Integer supplied.
	 * @param number The Integer to check for equality
	 * @return true if the Integer representation of the value is equal to number.
	 */
	public boolean isEqualTo(int number)
	{
		return getInteger() == number;
	}

	/**
	 * Check whether this value is equal to the Integer supplied.
	 * @param number The Integer to check for equality
	 * @return true if the Integer representation of the value is equal to number.
	 */
	public boolean isEqualTo(byte number)
	{
		return getInteger() == number;
	}

	/**
	 * Check whether this value is equal to the Date supplied.
	 * @param date The Date to check for equality
	 * @return true if the Date representation of the value is equal to date.
	 */
	public boolean isEqualTo(Date date)
	{
		return getDate() == date;
	}

	public int hashCode()
	{
		int hash=0; // name.hashCode(); -> Name shouldn't be part of hashCode()!
		
		if (isNull())
		{
			switch(getType())
			{
			case VALUE_TYPE_BOOLEAN   : hash^= 1; break;
			case VALUE_TYPE_DATE      : hash^= 2; break;
			case VALUE_TYPE_NUMBER    : hash^= 4; break;
			case VALUE_TYPE_STRING    : hash^= 8; break;
			case VALUE_TYPE_INTEGER   : hash^=16; break;
            case VALUE_TYPE_BIGNUMBER : hash^=32; break;
			case VALUE_TYPE_NONE      : break;
			default: break;
			}
		}
		else
		{
			switch(getType())
			{
			case VALUE_TYPE_BOOLEAN   :                           hash^=Boolean.valueOf(getBoolean()).hashCode(); break;
			case VALUE_TYPE_DATE      : if (getDate()!=null)      hash^=getDate().hashCode(); break;
			case VALUE_TYPE_INTEGER   :                           hash^=new Long(getInteger()).hashCode(); break;
			case VALUE_TYPE_NUMBER    :                           hash^=(new Double(getNumber())).hashCode(); break;
			case VALUE_TYPE_STRING    : if (getString()!=null)    hash^=getString().hashCode(); break;
			case VALUE_TYPE_BIGNUMBER : if (getBigNumber()!=null) hash^=getBigNumber().hashCode(); break;
			case VALUE_TYPE_NONE      : break;
			default: break;
			}
		}

		return hash;
	}



	// OPERATORS & COMPARATORS

	public Value and(Value v)
	{
		long n1 = getInteger();
		long n2 = v.getInteger();

		long res = n1 & n2;

		setValue(res);

		return this;
	}

	public Value xor(Value v)
	{
		long n1 = getInteger();
		long n2 = v.getInteger();

		long res = n1 ^ n2;

		setValue(res);

		return this;
	}

	public Value or(Value v)
	{
		long n1 = getInteger();
		long n2 = v.getInteger();

		long res = n1 | n2;

		setValue(res);

		return this;
	}

	public Value bool_and(Value v)
	{
		boolean b1 = getBoolean();
		boolean b2 = v.getBoolean();

		boolean res = b1 && b2;

		setValue(res);

		return this;
	}

	public Value bool_or(Value v)
	{
		boolean b1 = getBoolean();
		boolean b2 = v.getBoolean();

		boolean res = b1 || b2;

		setValue(res);

		return this;
	}

	public Value bool_xor(Value v)
	{
		boolean b1 = getBoolean();
		boolean b2 = v.getBoolean();

		boolean res = b1&&b2 ? false : !b1&&!b2 ? false : true;

		setValue(res);

		return this;
	}

	public Value bool_not()
	{
		value.setBoolean(!getBoolean());		
		return this;
	}


	public Value greater_equal(Value v)
	{
		if (compare(v)>=0) setValue(true); else setValue(false);
		return this;
	}

	public Value smaller_equal(Value v)
	{
		if (compare(v)<=0) setValue(true); else setValue(false);
		return this;
	}

	public Value different(Value v)
	{
		if (compare(v)!=0) setValue(true); else setValue(false);
		return this;
	}

	public Value equal(Value v)
	{
		if (compare(v)==0) setValue(true); else setValue(false);
		return this;
	}

	public Value like(Value v)
	{
		String cmp=v.getString();

		// Is cmp part of look?
		int idx=getString().indexOf(cmp);

		if (idx<0) setValue(false); else setValue(true);

		return this;
	}

	public Value greater(Value v)
	{
		if (compare(v)>0) setValue(true); else setValue(false);
		return this;
	}

	public Value smaller(Value v)
	{
		if (compare(v)<0) setValue(true); else setValue(false);
		return this;
	}

	public Value minus(BigDecimal v) throws KettleValueException { return minus(new Value("tmp", v)); }
    public Value minus(double     v) throws KettleValueException { return minus(new Value("tmp", v)); }
	public Value minus(long       v) throws KettleValueException { return minus(new Value("tmp", v)); }
	public Value minus(int        v) throws KettleValueException { return minus(new Value("tmp", (long)v)); }
	public Value minus(byte       v) throws KettleValueException { return minus(new Value("tmp", (long)v)); }
	public Value minus(Value  v) throws KettleValueException
	{
		switch(getType())
		{
            case VALUE_TYPE_BIGNUMBER : value.setBigNumber(getBigNumber().subtract(v.getBigNumber())); break;
			case VALUE_TYPE_NUMBER    : value.setNumber(getNumber()-v.getNumber()); break;
			case VALUE_TYPE_INTEGER   : value.setInteger(getInteger()-v.getInteger()); break;
			case VALUE_TYPE_BOOLEAN   :
			case VALUE_TYPE_STRING    :
			default:
				throw new KettleValueException("Subtraction can only be done with numbers!");
		}
		return this;
	}

    public Value plus(BigDecimal v) { return plus(new Value("tmp", v)); }
	public Value plus(double     v) { return plus(new Value("tmp", v)); }
	public Value plus(long       v) { return plus(new Value("tmp", v)); }
	public Value plus(int        v) { return plus(new Value("tmp", (long)v)); }
	public Value plus(byte       v) { return plus(new Value("tmp", (long)v)); }
	public Value plus(Value v)
	{
		switch(getType())
		{
            case VALUE_TYPE_BIGNUMBER : setValue(getBigNumber().add(v.getBigNumber())); break;
			case VALUE_TYPE_NUMBER    : setValue(getNumber()+v.getNumber()); break;
			case VALUE_TYPE_INTEGER   : setValue(getInteger()+v.getInteger()); break;
			case VALUE_TYPE_BOOLEAN   : setValue(getBoolean()|v.getBoolean()); break;
			case VALUE_TYPE_STRING    : setValue(getString()+v.getString()); break;
			default: break;
		}
		return this;
	}

    public Value divide(BigDecimal v) throws KettleValueException { return divide(new Value("tmp", v)); }
	public Value divide(double     v) throws KettleValueException { return divide(new Value("tmp", v)); }
	public Value divide(long       v) throws KettleValueException { return divide(new Value("tmp", v)); }
	public Value divide(int        v) throws KettleValueException { return divide(new Value("tmp", (long)v)); }
	public Value divide(byte       v) throws KettleValueException { return divide(new Value("tmp", (long)v)); }
	public Value divide(Value v)  throws KettleValueException
	{
		if (isNull() || v.isNull())
		{
			setNull();
		}
		else
		{
			switch(getType())
			{
	            case VALUE_TYPE_BIGNUMBER : setValue(getBigNumber().divide(v.getBigNumber(), BigDecimal.ROUND_HALF_UP)); break;
				case VALUE_TYPE_NUMBER    : setValue(getNumber()/v.getNumber()); break;
				case VALUE_TYPE_INTEGER   : setValue(getInteger()/v.getInteger()); break;
				case VALUE_TYPE_BOOLEAN   :
				case VALUE_TYPE_STRING    :
				default:
					throw new KettleValueException("Division can only be done with numeric data!");
			}
		}
		return this;
	}

	public Value multiply(BigDecimal v) throws KettleValueException { return multiply(new Value("tmp", v)); }
    public Value multiply(double     v) throws KettleValueException { return multiply(new Value("tmp", v)); }
	public Value multiply(long       v) throws KettleValueException { return multiply(new Value("tmp", v)); }
	public Value multiply(int        v) throws KettleValueException { return multiply(new Value("tmp", (long)v)); }
	public Value multiply(byte       v) throws KettleValueException { return multiply(new Value("tmp", (long)v)); }
	public Value multiply(Value      v) throws KettleValueException
	{
		// a number and a string!
		if (isNull() || v.isNull())
		{
			setNull();
			return this;
		}

		if ((v.isString() && isNumeric()) || (v.isNumeric() && isString()))
		{
			StringBuffer s;
			String append="";
			int n;
			if (v.isString())
			{
				s=new StringBuffer(v.getString());
				append=v.getString();
				n=(int)getInteger();
			}
			else
			{
				s=new StringBuffer(getString());
				append=getString();
				n=(int)v.getInteger();
			}

			if (n==0) s.setLength(0);
			else
			for (int i=1;i<n;i++) s.append(append);

			setValue(s);
		}
		else
        // big numbers
        if (isBigNumber() || v.isBigNumber())
        {
            setValue(ValueDataUtil.multiplyBigDecimals(getBigNumber(), v.getBigNumber(), null));
        }
        else
		// numbers
		if (isNumber() || v.isNumber())
		{
			setValue(getNumber()*v.getNumber());
		}
		else
		// integers
		if (isInteger() || v.isInteger())
		{
			setValue(getInteger()*v.getInteger());
		}
		else
		{
			 throw new KettleValueException("Multiplication can only be done with numbers or a number and a string!");
		}
		return this;
	}



	// FUNCTIONS!!

	// implement the ABS function, arguments in args[]
	public Value abs() throws KettleValueException
	{
		if (isNull()) return this;

        if (isBigNumber())
        {
            setValue(getBigNumber().abs());
        }
        else
		if (isNumber())
		{
			setValue(Math.abs(getNumber()));
		}
		else
		if (isInteger())
		{
			setValue(Math.abs(getInteger()));
		}
		else
		{
			throw new KettleValueException("Function ABS only works with a number");
		}
		return this;
	}

	// implement the ACOS function, arguments in args[]
	public Value acos() throws KettleValueException
	{
		if (isNull()) return this;

		if (isNumeric())
		{
			setValue(Math.acos(getNumber()));
		}
		else
		{
			throw new KettleValueException("Function ACOS only works with numeric data");
		}
		return this;
	}

	// implement the ASIN function, arguments in args[]
	public Value asin() throws KettleValueException
	{
		if (isNull()) return this;

		if (isNumeric())
		{
			setValue(Math.asin(getNumber()));
		}
		else
		{
			 throw new KettleValueException("Function ASIN only works with numeric data");
		}
		return this;
	}

	// implement the ATAN function, arguments in args[]
	public Value atan() throws KettleValueException
	{
		if (isNull()) return this;

		if (isNumeric())
		{
			setValue(Math.atan(getNumber()));
		}
		else
		{
			 throw new KettleValueException("Function ATAN only works with numeric data");
		}
		return this;
	}

	// implement the ATAN2 function, arguments in args[]
	public Value atan2(Value  arg0) throws KettleValueException { return atan2(arg0.getNumber()); }
	public Value atan2(double arg0) throws KettleValueException
	{
		if (isNull()) 
		{
			return this;
		}

		if (isNumeric())
		{
			setValue(Math.atan2(getNumber(), arg0));
		}
		else
		{
			 throw new 
			    KettleValueException("Function ATAN2 only works with numbers");
		}
		return this;
	}

	// implement the CEIL function, arguments in args[]
	public Value ceil() throws KettleValueException
	{
		if (isNull()) return this;
		
		if (isNumeric())
		{
			setValue(Math.ceil(getNumber()));
		}
		else
		{
			 throw new KettleValueException("Function CEIL only works with a number");
		}
		return this;
	}

	// implement the COS function, arguments in args[]
	public Value cos() throws KettleValueException
	{
		if (isNull()) return this;
		
		if (isNumeric())
		{
			setValue(Math.cos(getNumber()));
		}
		else
		{
			throw new KettleValueException("Function COS only works with a number");
		}
		return this;
	}

	// implement the EXP function, arguments in args[]
	public Value exp() throws KettleValueException
	{
		if (isNull()) return this;
		
		if (isNumeric())
		{
			setValue(Math.exp(getNumber()));
		}
		else
		{
			throw new KettleValueException("Function EXP only works with a number");
		}
		return this;
	}

	// implement the FLOOR function, arguments in args[]
	public Value floor() throws KettleValueException
	{
		if (isNull()) return this;
		
		if (isNumeric())
		{
			setValue(Math.floor(getNumber()));
		}
		else
		{
			 throw new KettleValueException("Function FLOOR only works with a number");
		}
		return this;
	}

	// implement the INITCAP function, arguments in args[]
	public Value initcap()
	{
		if (isNull()) return this;
		
		if (getString()==null)
		{
			setNull();
		}
		else
		{
			setValue( Const.initCap(getString()) );
		}
		return this;
	}

	// implement the LENGTH function, arguments in args[]
	public Value length() throws KettleValueException
	{
		if (isNull()) 
		{
			setType(VALUE_TYPE_INTEGER);
			setValue(0L);
			return this;
		} 
		
		if (getType()==VALUE_TYPE_STRING)
		{
			setValue((double)getString().length());
		}
		else
		{
			 throw new KettleValueException("Function LENGTH only works with a string");
		}
		return this;
	}

	// implement the LOG function, arguments in args[]
	public Value log() throws KettleValueException
	{
		if (isNull()) return this;
		
		if (isNumeric())
		{
			setValue(Math.log(getNumber()));
		}
		else
		{
			throw new KettleValueException("Function LOG only works with a number");
		}
		
		return this;
	}

	// implement the LOWER function, arguments in args[]
	public Value lower()
	{
		if (isNull())
		{
			setType(VALUE_TYPE_STRING);
		}
		else
		{
			setValue( getString().toLowerCase() );
		}

		return this;
	}


	// implement the LPAD function: left pad strings or numbers...
	public Value lpad(Value len) { return lpad((int)len.getNumber(), " "); }
	public Value lpad(Value len, Value padstr) { return lpad((int)len.getNumber(), padstr.getString());	}
	public Value lpad(int len) { return lpad(len, " "); }
	public Value lpad(int len, String padstr)
	{
		if (isNull())
		{
			setType(VALUE_TYPE_STRING);
		}
		else
		{
			if (getType()!=VALUE_TYPE_STRING) // also lpad other types!
			{
				setValue(getString());
			}
			
			if (getString()!=null)
			{
				StringBuffer result = new StringBuffer(getString());
		
				int pad=len;
				int l= ( pad-result.length() ) / padstr.length() + 1;
				int i;
				
				for (i=0;i<l;i++) result.insert(0, padstr);
							
				// Maybe we added one or two too many!
				i=result.length();
				while (i>pad && pad>0)
				{
					result.deleteCharAt(0);
					i--;
				}
				setValue(result.toString());
			}
			else
			{
				setNull();
			}
		}
		setLength(len);

		return this;
	}

	// implement the LTRIM function
	public Value ltrim()
	{
		if (isNull())
		{
			setType(VALUE_TYPE_STRING);
		}
		else
		{
			if (getString()!=null)
			{
			    String s;
				if (getType()==VALUE_TYPE_STRING)
				{
					s = Const.ltrim(getString());
				}
				else
				{
					s = Const.ltrim(toString());
				}
				
				setValue(s);
			}
			else
			{
				setNull();
			}
		}
		
		return this;
	}

	// implement the MOD function, arguments in args[]
	public Value mod(Value      arg)  throws KettleValueException { return mod(arg.getNumber()); }
    public Value mod(BigDecimal arg)  throws KettleValueException { return mod(arg.doubleValue()); }
    public Value mod(long       arg)  throws KettleValueException { return mod((double)arg); }
    public Value mod(int        arg)  throws KettleValueException { return mod((double)arg); }
    public Value mod(byte       arg)  throws KettleValueException { return mod((double)arg); }
	public Value mod(double arg0) throws KettleValueException
	{
		if (isNull()) return this;
		
		if (isNumeric())
		{
			double n1=getNumber();
			double n2=arg0;
			
			setValue( n1 - (n2 * Math.floor(n1 /n2 )) );
		}
		else
		{
			 throw new KettleValueException("Function MOD only works with numeric data");
		}

		return this;
	}

	// implement the NVL function, arguments in args[]
	public Value nvl(Value alt)
	{
		if (isNull()) setValue(alt);
		return this;
	}

	// implement the POWER function, arguments in args[]
	public Value power(BigDecimal arg) throws KettleValueException{ return power(new Value("tmp", arg)); }
    public Value power(double     arg) throws KettleValueException{ return power(new Value("tmp", arg)); }
	public Value power(Value v) throws KettleValueException
	{
		if (isNull()) return this;
		
        else if (isNumeric())
		{
			setValue( Math.pow(getNumber(), v.getNumber()) );
		}
		else
		{
			throw new KettleValueException("Function POWER only works with numeric data");
		}
		return this;
	}

	// implement the REPLACE function, arguments in args[]
	public Value replace(Value repl, Value with) { return replace(repl.getString(), with.getString()); }
	public Value replace(String repl, String with)
	{
		if (isNull()) return this;
		if (getString()==null) 
		{
			setNull();
		}
		else
		{
			setValue( Const.replace(getString(), repl, with) );
		}
		return this;
	}

	/**
	 * Rounds off to the nearest integer.<p>  
	 * See also: java.lang.Math.round()
	 * 
	 * @return The rounded Number value.
	 */
	public Value round() throws KettleValueException
	{
		if (isNull()) return this;

		if (isNumeric())
		{
			setValue( (double)Math.round(getNumber()) );
		}
		else
		{
			throw new KettleValueException("Function ROUND only works with a number");
		}
		return this;
	}
    
    /**
     * Rounds the Number value to a certain number decimal places.
     * @param decimalPlaces
     * @return The rounded Number Value
     * @throws KettleValueException in case it's not a number (or other problem).
     */
    public Value round(int decimalPlaces) throws KettleValueException
    {
        if (isNull()) return this;
        
        if (isNumeric())
        {
            if (isBigNumber())
            {
                // Multiply by 10^decimalPlaces
                // For example 123.458343938437, Decimalplaces = 2
                // 
                BigDecimal bigDec = getBigNumber();
                // System.out.println("ROUND decimalPlaces : "+decimalPlaces+", bigNumber = "+bigDec);
                bigDec = bigDec.setScale(decimalPlaces, BigDecimal.ROUND_HALF_EVEN); 
                // System.out.println("ROUND finished result         : "+bigDec);
                setValue( bigDec );
            }
            else
            {
                setValue(Const.round(getNumber(), decimalPlaces));
            }
        }
        else
        {
            throw new KettleValueException("Function ROUND only works with a number");
        }
        return this;

    }

	// implement the RPAD function, arguments in args[]
	public Value rpad(Value len) { return rpad((int)len.getNumber(), " "); }
	public Value rpad(Value len, Value padstr) { return rpad((int)len.getNumber(), padstr.getString());	}
	public Value rpad(int len) { return rpad(len, " "); }
	public Value rpad(int len, String padstr)
	{
		if (isNull())
		{
			setType(VALUE_TYPE_STRING);
		}
		else
		{
			if (getType()!=VALUE_TYPE_STRING) // also rpad other types!
			{
				setValue(getString());
			}
			if (getString()!=null)
			{
				StringBuffer result = new StringBuffer(getString());
		
				int pad=len;
				int l= ( pad-result.length() ) / padstr.length() + 1;
				int i;
				
				for (i=0;i<l;i++) result.append(padstr);
				
				// Maybe we added one or two too many!
				i=result.length();
				while (i>pad && pad>0)
				{
					result.deleteCharAt(i-1);
					i--;
				}
				setValue(result.toString());
			}
			else
			{
				setNull();
			}
		}
		setLength(len);

		return this;
	}

	// implement the RTRIM function, arguments in args[]
	public Value rtrim()
	{
		if (isNull())
		{
			setType(VALUE_TYPE_STRING);
		}
		else
		{
			String s;
			if (getType()==VALUE_TYPE_STRING)
			{
				s = Const.rtrim(getString());
			}
			else
			{
				s = Const.rtrim(toString());
			}
			
			setValue(s);
		}
		return this;
	}

	// implement the SIGN function, arguments in args[]
	public Value sign() throws KettleValueException
	{
		if (isNull()) return this;
        if (isNumber())
        {
            int cmp = getBigNumber().compareTo(new BigDecimal(0L)); 
            if (cmp>0) value.setBigNumber(new BigDecimal(1L));
            else if (cmp<0) value.setBigNumber(new BigDecimal(-1L));
            else value.setBigNumber(new BigDecimal(0L));
        }
        else
		if (isNumber())
		{
			if (getNumber()>0) value.setNumber(1.0);
			else if (getNumber()<0) value.setNumber(-1.0);
			else value.setNumber(0.0);
		}
		else
		if (isInteger())
		{
			if (getInteger()>0) value.setInteger(1);
			else if (getInteger()<0) value.setInteger(-1);
			else value.setInteger(0);
		}
		else
		{
			throw new KettleValueException("Function SIGN only works with a number");
		}
		
		return this;
	}

	// implement the SIN function, arguments in args[]
	public Value sin() throws KettleValueException
	{
		if (isNull()) return this;
		if (isNumeric())
		{
			setValue( Math.sin(getNumber()) );
		}
		else
		{
			throw new KettleValueException("Function SIN only works with a number");
		}

		return this;
	}

	// implement the SQRT function, arguments in args[]
	public Value sqrt() throws KettleValueException
	{
		if (isNull()) return this;
		if (isNumeric())
		{
			setValue( Math.sqrt(getNumber()) );
		}
		else
		{
			throw new KettleValueException("Function SQRT only works with a number");
		}

		return this;
	}

	// implement the SUBSTR function, arguments in args[]
	public Value substr(Value from, Value to) { return substr((int)from.getNumber(), (int)to.getNumber()); }
	public Value substr(Value from) { return substr((int)from.getNumber(), -1); }
	public Value substr(int from) { return substr(from, -1);  }
	public Value substr(int from, int to)
	{
		if (isNull()) 
		{
			setType(VALUE_TYPE_STRING);
			return this;
		} 

		setValue( getString() );
		
		if (getString()!=null)
		{
            if (to<0 && from>=0) 
            {
                setValue( getString().substring(from) );
            }
            else if (to>=0 && from>=0)
            {
                setValue( getString().substring(from, to) );
            }
		}
		else
		{
			setNull();
		}
		if (!isString()) setType(VALUE_TYPE_STRING);
		
		return this;
	}

	// implement the RIGHTSTR function, arguments in args[]
	public Value rightstr(Value len) { return rightstr((int)len.getNumber()); }
	public Value rightstr(int len) 
	{
		if (isNull()) 
		{
			setType(VALUE_TYPE_STRING);
			return this;
		} 

		setValue( getString() );
		
		int tot_len = getString()!=null?getString().length():0;
		
		if (tot_len>0)
		{
			int totlen = getString().length();
			
			int f = totlen-len;
			if (f<0) f=0;
				
			setValue( getString().substring(f) );
		}
		else
		{
			setNull();
		}
		if (!isString()) setType(VALUE_TYPE_STRING);
		
		return this;
	}

	// implement the LEFTSTR function, arguments in args[]
	public Value leftstr(Value len) 
	{ 
		return leftstr((int)len.getNumber()); 
	}
	
	public Value leftstr(int len) 
	{
		if (isNull()) 
		{
			setType(VALUE_TYPE_STRING);
			return this;
		} 

		setValue( getString() );
		
		int tot_len = getString()!=null?getString().length():0;
		
		if (tot_len>0)
		{
			int totlen = getString().length();
			
			int f = totlen-len;
			if (f>0)
			{
				setValue( getString().substring(0,len) );
			}
		}
		else
		{
			setNull();
		}
		if (!isString()) setType(VALUE_TYPE_STRING);
		
		return this;
	}

	public Value startsWith(Value string)
	{
		return startsWith(string.getString());
	}

	public Value startsWith(String string)
	{
		if (isNull()) 
		{
			setType(VALUE_TYPE_BOOLEAN);
			return this;
		} 
		
		if (string==null)
		{
			setValue(false);
			setNull();
			return this;
		}

		setValue( getString().startsWith(string) );
		
		return this;
	}

	
	// implement the SYSDATE function, arguments in args[]
	public Value sysdate()
	{
		setValue( Calendar.getInstance().getTime() );
		
		return this;
	}

	// implement the TAN function, arguments in args[]
	public Value tan() throws KettleValueException
	{
		if (isNull()) return this;
		
		if (isNumeric())
		{
			setValue( Math.tan(getNumber()) );
		}
		else
		{
			throw new KettleValueException("Function TAN only works on a number");
		}

		return this;
	}

	// implement the TO_CHAR function, arguments in args[]
	// number: NUM2STR( 123.456 )   : default format
	// number: NUM2STR( 123.456, '###,##0.000') : format 
	// number: NUM2STR( 123.456, '###,##0.000', '.') : grouping
	// number: NUM2STR( 123.456, '###,##0.000', '.', ',') : decimal
	// number: NUM2STR( 123.456, '###,##0.000', '.', ',', '?') : currency

	public Value num2str() throws KettleValueException { return num2str(null, null, null, null); }
	public Value num2str(String format) throws KettleValueException { return num2str(format, null, null, null); }
	public Value num2str(String format, String decimalSymbol) throws KettleValueException { return num2str(format, decimalSymbol, null, null); }
	public Value num2str(String format, String decimalSymbol, String groupingSymbol) throws KettleValueException { return num2str(format, decimalSymbol, groupingSymbol, null); }
	public Value num2str(String format, String decimalSymbol, String groupingSymbol, String currencySymbol) throws KettleValueException
	{
		if (isNull())
		{
			setType(VALUE_TYPE_STRING);
		}
		else
		{
			// Number to String conversion...
			if (getType()==VALUE_TYPE_NUMBER || getType()==VALUE_TYPE_INTEGER)
			{
				NumberFormat         nf  = NumberFormat.getInstance();
				DecimalFormat        df  = (DecimalFormat)nf;
				DecimalFormatSymbols dfs =new DecimalFormatSymbols();
			
				if (currencySymbol!=null && currencySymbol.length()>0) dfs.setCurrencySymbol( currencySymbol );
				if (groupingSymbol!=null && groupingSymbol.length()>0) dfs.setGroupingSeparator( groupingSymbol.charAt(0) );
				if (decimalSymbol!=null && decimalSymbol.length()>0) dfs.setDecimalSeparator( decimalSymbol.charAt(0) );
				df.setDecimalFormatSymbols(dfs); // in case of 4, 3 or 2
				if (format!=null && format.length()>0) df.applyPattern(format);
				try
				{
					setValue( nf.format(getNumber()) );
				}
				catch(Exception e)
				{
					setType(VALUE_TYPE_STRING);
					setNull();
					throw new KettleValueException("Couldn't convert Number to String "+e.toString());
				}
			}
			else
			{
				throw new KettleValueException("Function NUM2STR only works on Numbers and Integers");
			}
		}
		return this;
	}
	
	// date:   TO_CHAR( <date> , 'yyyy/mm/dd HH:mm:ss'
	public Value dat2str() throws KettleValueException { return dat2str(null, null); }
	public Value dat2str(String arg0) throws KettleValueException { return dat2str(arg0, null); }
	public Value dat2str(String arg0, String arg1) throws KettleValueException
	{
		if (isNull())
		{
			setType(VALUE_TYPE_STRING);
		}
		else
		{
			if (getType()==VALUE_TYPE_DATE)
			{
				SimpleDateFormat     df  = new SimpleDateFormat();
			
				DateFormatSymbols dfs = new DateFormatSymbols();
				if (arg1!=null) dfs.setLocalPatternChars(arg1);
				if (arg0!=null) df.applyPattern(arg0);
				try
				{
					setValue( df.format(getDate()) );
				}
				catch(Exception e)
				{
					setType(VALUE_TYPE_STRING);
					setNull();
					throw new KettleValueException("TO_CHAR Couldn't convert Date to String "+e.toString());
				}
			}
			else
			{
				throw new KettleValueException("Function DAT2STR only works on a date");
			}
		}
		
		return this;
	}

	// implement the TO_DATE function, arguments in args[]
	public Value num2dat() throws KettleValueException
	{
		if (isNull())
		{
			setType(VALUE_TYPE_DATE);
		}
		else
		{
			if (isNumeric())
			{
				setValue(new Date(getInteger()));
				setLength(-1,-1);
			}
			else
			{
				throw new KettleValueException("Function NUM2DAT only works on a number");
			}
		}
		return this;
	}
	
	public Value str2dat(String arg0) throws KettleValueException { return str2dat(arg0, null); }
	public Value str2dat(String arg0, String arg1) throws KettleValueException
	{
		if (isNull())
		{
			setType(VALUE_TYPE_DATE);
		}
		else
		{
			// System.out.println("Convert string ["+string+"] to date using pattern '"+arg0+"'");

			SimpleDateFormat     df  = new SimpleDateFormat();
			
			DateFormatSymbols dfs = new DateFormatSymbols();
			if (arg1!=null) dfs.setLocalPatternChars(arg1);
			if (arg0!=null) df.applyPattern(arg0);

			try
			{
				value.setDate( df.parse(getString()) );
				setType(VALUE_TYPE_DATE);
				setLength(-1,-1);
			}
			catch(Exception e)
			{
				setType(VALUE_TYPE_DATE);
				setNull();
				throw new KettleValueException("TO_DATE Couldn't convert String to Date"+e.toString());
			}
		}
		return this;
	}

	// implement the TO_NUMBER function, arguments in args[]
	public Value str2num() throws KettleValueException { return str2num(null, null, null, null); }
	public Value str2num(String pattern) throws KettleValueException { return str2num(pattern, null, null, null); }
	public Value str2num(String pattern, String decimal) throws KettleValueException { return str2num(pattern, decimal, null, null); }
	public Value str2num(String pattern, String decimal, String grouping) throws KettleValueException { return str2num(pattern, decimal, grouping, null); }
	public Value str2num(String pattern, String decimal, String grouping, String currency) throws KettleValueException
	{
		// 0 : pattern
		// 1 : Decimal separator
		// 2 : Grouping separator
		// 3 : Currency symbol

		if (isNull())
		{
			setType(VALUE_TYPE_STRING);
		}
		else
		{
			if (getType()==VALUE_TYPE_STRING)
			{
				if (getString()==null)
				{
					setNull();
					setValue(0.0);
				} 
				else
				{
					NumberFormat         nf  = NumberFormat.getInstance();
					DecimalFormat        df  = (DecimalFormat)nf;
					DecimalFormatSymbols dfs =new DecimalFormatSymbols();
						
                    if ( !Const.isEmpty(pattern ) ) df.applyPattern( pattern );
                    if ( !Const.isEmpty(decimal ) ) dfs.setDecimalSeparator( decimal.charAt(0) );
                    if ( !Const.isEmpty(grouping) ) dfs.setGroupingSeparator( grouping.charAt(0) );
					if ( !Const.isEmpty(currency) ) dfs.setCurrencySymbol( currency );
					try
					{
                        df.setDecimalFormatSymbols(dfs);
						setValue( df.parse(getString()).doubleValue() );
					}
					catch(Exception e)
					{
                        String message = "Couldn't convert string to number "+e.toString();
                        if ( !Const.isEmpty(pattern ) ) message+=" pattern="+pattern;
                        if ( !Const.isEmpty(decimal ) ) message+=" decimal="+decimal;
                        if ( !Const.isEmpty(grouping) ) message+=" grouping="+grouping.charAt(0);
                        if ( !Const.isEmpty(currency) ) message+=" currency="+currency;
						throw new KettleValueException(message);
					}
				}
			}
			else
			{
				throw new KettleValueException("Function STR2NUM works only on strings");
			}
		}
	 	return this;
	}
	
	public Value dat2num() throws KettleValueException
	{
		if (isNull())
		{
			setType(VALUE_TYPE_INTEGER);
			return this;
		}

		if (getType()==VALUE_TYPE_DATE)
		{
			if (getString()==null)
			{
				setNull();
				setValue(0L);
			} 
			else
			{
				setValue(getInteger());
			}
		}
		else
		{
			throw new KettleValueException("Function DAT2NUM works only on dates");
		}
		return this;
	}

	/**
	 * Performs a right and left trim of spaces in the string.
	 * If the value is not a string a conversion to String is performed first.
	 * 
	 * @return The trimmed string value.
	 */
	public Value trim()
	{
		if (isNull())
		{
			setType(VALUE_TYPE_STRING);
			return this;
		}
		
		String str = Const.trim(getString());
		setValue(str);

		return this;
	}

	// implement the UPPER function, arguments in args[]
	public Value upper()
	{
		if (isNull())
		{
			setType(VALUE_TYPE_STRING);
			return this;
		}
		
		setValue( getString().toUpperCase() );
		
		return this;
	}

	// implement the E function, arguments in args[]
	public Value e()
	{
		setValue(Math.E);
		return this;
	}

	// implement the PI function, arguments in args[]
	public Value pi()
	{
		setValue(Math.PI);
		return this;
	}

	// implement the DECODE function, arguments in args[]
	public Value v_decode(Value args[]) throws KettleValueException
	{
		int i;
		boolean found;
		// Decode takes as input the first argument...
		// The next pair
		
		// Limit to 3, 5, 7, 9, ... arguments

		if (args.length>=3 && (args.length%2)==1)
		{
			i=0;
			found=false;
			while (i<args.length-1 && !found)
			{
				if (this.equals(args[i]))
				{
					setValue(args[i+1]);
					found=true;
				}
				i+=2;
			}
			if (!found) setValue(args[args.length-1]);
		}
		else
		{
			// ERROR with nr of arguments
			throw new KettleValueException("Function DECODE can't have "+args.length+" arguments!");
		}
		
		return this;
	}

	// implement the IF function, arguments in args[]
	// IF( <condition>, <then value>, <else value>)
	public Value v_if(Value args[]) throws KettleValueException
	{
		if (getType()==VALUE_TYPE_BOOLEAN)
		{
			if (args.length==1)
			{
				if (getBoolean()) setValue(args[0]); else setNull();
			}
			else
			if (args.length==2)
			{
				if (getBoolean()) setValue(args[0]); else setValue(args[1]);
			}
		}
		else
		{
			throw new KettleValueException("Function DECODE can't have "+args.length+" arguments!");
		}
		return this;
	}
	
	// implement the ADD_MONTHS function, one argument
	public Value add_months(int months) throws KettleValueException
	{
		if (getType()==VALUE_TYPE_DATE)
		{
			if (!isNull() && getDate()!=null)
			{
				Calendar cal = Calendar.getInstance();
				cal.setTime(getDate());
				int year  = cal.get(Calendar.YEAR);
				int month = cal.get(Calendar.MONTH);
				int day   = cal.get(Calendar.DAY_OF_MONTH);
				
				month+=months;
				
				int newyear =  year+(int)Math.floor(month/12);
				int newmonth   = month%12;
				
				cal.set(newyear, newmonth, 1);
				int newday = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				if (newday<day) cal.set(Calendar.DAY_OF_MONTH, newday);
				else            cal.set(Calendar.DAY_OF_MONTH, day);

				setValue( cal.getTime() );
			}
		}
		else
		{
			throw new KettleValueException("Function add_months only works on a date!");
		}
		return this;
	}
    
    /** 
     * Add a number of days to a Date value.
     * 
     * @param days The number of days to add to the current date value
     * @return The resulting value
     * @throws KettleValueException
     */
    public Value add_days(long days) throws KettleValueException
    {
        if (getType()==VALUE_TYPE_DATE)
        {
            if (!isNull() && getDate()!=null)
            {
                Calendar cal = Calendar.getInstance();
                cal.setTime(getDate());
                cal.add(Calendar.DAY_OF_YEAR, (int)days);
                
                setValue( cal.getTime() );
            }
        }
        else
        {
            throw new KettleValueException("Function add_days only works on a date!");
        }
        return this;
    }

	// implement the LAST_DAY function, arguments in args[]
	public Value last_day() throws KettleValueException
	{
		if (getType()==VALUE_TYPE_DATE)
		{
			Calendar cal=Calendar.getInstance();
			cal.setTime(getDate());
			int last_day = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			cal.set(Calendar.DAY_OF_MONTH, last_day);

			setValue( cal.getTime() );
		}
		else
		{
			throw new KettleValueException("Function last_day only works on a date");
		}

		return this;
	}

	public Value first_day() throws KettleValueException
	{
		if (getType()==VALUE_TYPE_DATE)
		{
			Calendar cal=Calendar.getInstance();
			cal.setTime(getDate());
			cal.set(Calendar.DAY_OF_MONTH, 1);
			setValue( cal.getTime() );
		}
		else
		{
			throw new KettleValueException("Function first_day only works on a date");
		}

		return this;
	}


	// implement the TRUNC function, version without arguments
	public Value trunc() throws KettleValueException
	{
		if (isNull()) return this; // don't do anything, leave it at NULL!

        if (isBigNumber())
        {
            getBigNumber().setScale(0, BigDecimal.ROUND_FLOOR);
        }
        else
		if (isNumber())
		{
			setValue( Math.floor(getNumber()) );
		}
		else
		if (isInteger())
		{
			// Nothing
		}
		else
		if (isDate())
		{
			Calendar cal=Calendar.getInstance();
			cal.setTime(getDate());

			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.HOUR_OF_DAY, 0);

			setValue( cal.getTime() );
		}
		else
		{
			throw new KettleValueException("Function TRUNC only works on numbers and dates");
		}
		
		return this;
	}

	// implement the TRUNC function, arguments in args[]
	public Value trunc(double level) throws KettleValueException { return trunc((int)level); }

	public Value trunc(int level) throws KettleValueException
	{
		if (isNull()) return this; // don't do anything, leave it at NULL!

        if (isBigNumber())
        {
            getBigNumber().setScale(level, BigDecimal.ROUND_FLOOR);
        }
        else
		if (isNumber())
		{
			double pow=Math.pow(10, level);
			setValue( Math.floor( getNumber() * pow ) / pow );
		}
		else
		if (isInteger())
		{
            // Nothing!
		}
		else
		if (isDate())
		{
			Calendar cal=Calendar.getInstance();
			cal.setTime(getDate());
			
			switch(level)
			{
			// MONTHS
			case 5: cal.set(Calendar.MONTH, 1);
			// DAYS
			case 4: cal.set(Calendar.DAY_OF_MONTH, 1);
			// HOURS 
			case 3: cal.set(Calendar.HOUR_OF_DAY, 0);
			// MINUTES
			case 2: cal.set(Calendar.MINUTE, 0);
			// SECONDS
			case 1: cal.set(Calendar.SECOND, 0);
            // MILI-SECONDS
            case 0: cal.set(Calendar.MILLISECOND, 0);  break;
			default:
				throw new KettleValueException("Argument of TRUNC of date has to be between 0 and 5");
			}
		}
		else
		{
			throw new KettleValueException("Function TRUNC only works with numbers and dates");
		}
		
		return this;
	}

	/**
	 * Change a string into its hexadecimal representation. E.g. if Value
	 * contains string "a" afterwards it would contain value "61".
	 *
	 * Note that transformations happen in groups of 2 hex characters, so
	 * the value of a characters is always in the range 0-255.
	 *
	 * @return Value itself
	 * @throws KettleValueException
	 */
    public Value byteToHexEncode()
	{
        final char hexDigits[] =
    	{ '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F' };

		setType(VALUE_TYPE_STRING);
		if (isNull()) 
		{
			return this;
		}
		
		String hex = getString();

		// depending on the use case, this code might deliver the wrong values due to extra conversion with toCharArray
        // see Checksum step and PDI-5190 "Add Checksum step gives incorrect results (MD5, CRC32, ADLER32, SHA-1 are affected)"
		char[] s = hex.toCharArray();
		StringBuffer hexString = new StringBuffer(2 * s.length);
		
		for (int i = 0; i < s.length; i++)
		{
			hexString.append(hexDigits[(s[i] & 0x00F0) >> 4]); // hi nibble
			hexString.append(hexDigits[s[i] & 0x000F]);        // lo nibble
		}
		
		setValue( hexString );
	    return this;
	}

	/**
	 * Change a hexadecimal string into normal ASCII representation. E.g. if Value
	 * contains string "61" afterwards it would contain value "a". If the
	 * hexadecimal string is of odd length a leading zero will be used.
	 *
	 * Note that only the low byte of a character will be processed, this
	 * is for binary transformations.
	 *
	 * @return Value itself
	 * @throws KettleValueException  
	 */    
	public Value hexToByteDecode() throws KettleValueException 
	{
		setType(VALUE_TYPE_STRING);
		if (isNull()) 
		{			
			return this;
		}
		
		setValue( getString() );
		
		String hexString = getString();
		
		int len = hexString.length();
		char chArray[] = new char[(len + 1) / 2];
		boolean	evenByte = true;
		int nextByte = 0;
		
		// we assume a leading 0 if the length is not even.
		if ((len % 2) == 1)
			evenByte = false;
		
		int nibble;
		int i, j;
		for (i = 0, j = 0; i < len; i++)
		{
			char	c = hexString.charAt(i);
			
			if ((c >= '0') && (c <= '9'))
				nibble = c - '0';
			else if ((c >= 'A') && (c <= 'F'))
				nibble = c - 'A' + 0x0A;
			else if ((c >= 'a') && (c <= 'f'))
				nibble = c - 'a' + 0x0A;
			else
				throw new KettleValueException("invalid hex digit '" + c + "'.");
			
			if (evenByte)
			{
				nextByte = (nibble << 4);
			}
			else
			{
				nextByte += nibble;
				chArray[j] = (char)nextByte;
				j++;
			}
			
			evenByte = ! evenByte;
		}
		setValue(new String(chArray));
				
		return this;
	}

	/**
	 * Change a string into its hexadecimal representation. E.g. if Value
	 * contains string "a" afterwards it would contain value "0061".
	 * 
	 * Note that transformations happen in groups of 4 hex characters, so
	 * the value of a characters is always in the range 0-65535.
	 *  
	 * @return Value itself
	 * @throws KettleValueException
	 */
    public Value charToHexEncode()
	{
        final char hexDigits[] =
    	{ '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F' };

		setType(VALUE_TYPE_STRING);
		if (isNull()) 
		{
			return this;
		}
		
		String hex = getString();
		
		char[] s = hex.toCharArray();
		StringBuffer hexString = new StringBuffer(2 * s.length);
		
		for (int i = 0; i < s.length; i++)
		{
			hexString.append(hexDigits[(s[i] & 0xF000) >> 12]); // hex 1
			hexString.append(hexDigits[(s[i] & 0x0F00) >> 8]);  // hex 2
			hexString.append(hexDigits[(s[i] & 0x00F0) >> 4]);  // hex 3
			hexString.append(hexDigits[s[i] & 0x000F]);         // hex 4
		}
		
		setValue( hexString );
	    return this;
	}

	/**
	 * Change a hexadecimal string into normal ASCII representation. E.g. if Value
	 * contains string "61" afterwards it would contain value "a". If the
	 * hexadecimal string is of a wrong length leading zeroes will be used.
	 *
	 * Note that transformations happen in groups of 4 hex characters, so
	 * the value of a characters is always in the range 0-65535.
	 *
	 * @return Value itself
	 * @throws KettleValueException  
	 */    
	public Value hexToCharDecode() throws KettleValueException 
	{
		setType(VALUE_TYPE_STRING);
		if (isNull()) 
		{			
			return this;
		}
		
		setValue( getString() );
		
		String hexString = getString();
		
		int len = hexString.length();
		char chArray[] = new char[(len + 3) / 4];
		int charNr;
		int nextChar = 0;
		
		// we assume a leading 0s if the length is not right.
		charNr = (len % 4);
		if ( charNr == 0 ) charNr = 4;
		
		int nibble;
		int i, j;
		for (i = 0, j = 0; i < len; i++)
		{
			char	c = hexString.charAt(i);
			
			if ((c >= '0') && (c <= '9'))
				nibble = c - '0';
			else if ((c >= 'A') && (c <= 'F'))
				nibble = c - 'A' + 0x0A;
			else if ((c >= 'a') && (c <= 'f'))
				nibble = c - 'a' + 0x0A;
			else
				throw new KettleValueException("invalid hex digit '" + c + "'.");

			if (charNr == 4)
			{
				nextChar = (nibble << 12);
				charNr--;
			}			
			else if (charNr == 3)
			{
				nextChar += (nibble << 8);
				charNr--;
			}
			else if (charNr == 2)
			{
				nextChar += (nibble << 4);
				charNr--;
			}
			else // charNr == 1
			{
				nextChar += nibble;
				chArray[j] = (char)nextChar;
				charNr = 4;
				j++;
			}
		}
		setValue(new String(chArray));
				
		return this;
	}

	/* 
	 * Some javascript extensions... 
	 */
	public static final Value getInstance() { return new Value(); }

	public String getClassName() { return "Value"; }

	public void jsConstructor() 
	{ 
	}

	public void jsConstructor(String name) 
	{ 
		setName(name); 
	}

	public void jsConstructor(String name, String value) 
	{ 
		setName(name); 
		setValue(value);
	}

	/**
	 * Produce the XML representation of this value.
	 * @return a String containing the XML to represent this Value.
	 */
	public String getXML()
	{
		StringBuffer retval = new StringBuffer(128);
		retval.append("<"+XML_TAG+">");
		retval.append(XMLHandler.addTagValue("name", getName(), false));
		retval.append(XMLHandler.addTagValue("type", getTypeDesc(), false));
		retval.append(XMLHandler.addTagValue("text", toString(false), false));
		retval.append(XMLHandler.addTagValue("length", getLength(), false));
		retval.append(XMLHandler.addTagValue("precision", getPrecision(), false));
		retval.append(XMLHandler.addTagValue("isnull", isNull(), false));
        retval.append("</"+XML_TAG+">");

		return retval.toString();
	}

	/**
	 * Construct a new Value and read the data from XML
	 * @param valnode The XML Node to read from.
	 */
	public Value(Node valnode)
	{
		this();
		loadXML(valnode);
	}

	/**
	 * Read the data for this Value from an XML Node
	 * @param valnode The XML Node to read from
	 * @return true if all went well, false if something went wrong.
	 */
	public boolean loadXML(Node valnode)
	{
		try
		{
			String valname =  XMLHandler.getTagValue(valnode, "name");
			int valtype    =  getType( XMLHandler.getTagValue(valnode, "type") );
			String text    =  XMLHandler.getTagValue(valnode, "text");
			boolean isnull =  "Y".equalsIgnoreCase(XMLHandler.getTagValue(valnode, "isnull"));
			int len        =  Const.toInt(XMLHandler.getTagValue(valnode, "length"), -1);
			int prec       =  Const.toInt(XMLHandler.getTagValue(valnode, "precision"), -1);

			setName(valname);
			setValue(text);
			setLength(len, prec);

			if (valtype!=VALUE_TYPE_STRING)	
			{
				trim();
				convertString(valtype);
			}

			if (isnull) setNull();
		}
		catch(Exception e)
		{
			setNull();
			return false;
		}

		return true;
	}

	/**
	 * Convert this Value from type String to another type
	 * @param newtype The Value type to convert to.
	 */
	public void convertString(int newtype) throws KettleValueException
	{
		switch(newtype)
		{
		case VALUE_TYPE_STRING    : break;
		case VALUE_TYPE_NUMBER    : setValue( getNumber() ); break;
		case VALUE_TYPE_DATE      : setValue( getDate() ); break;
		case VALUE_TYPE_BOOLEAN   : setValue( getBoolean() ); break;
		case VALUE_TYPE_INTEGER   : setValue( getInteger() ); break;
        case VALUE_TYPE_BIGNUMBER : setValue( getBigNumber() ); break;
		default: 
            throw new KettleValueException("Please specify the type to convert to from String type.");
		}
	}

	public boolean equalValueType(Value v)
	{
		return equalValueType(v, false);
	}

	/**
	 * Returns whether "types" of the values are exactly the same: type, 
	 * name, length, precision.
	 * 
	 * @param v Value to compare type against.
	 * 
	 * @return == true when types are the same
	 *         == false when the types differ
	 */
	public boolean equalValueType(Value v, boolean checkTypeOnly)
	{
	    if (v == null)
	    	return false;
	    if (getType() != v.getType())
	    	return false;
	    if (!checkTypeOnly) {
		    if ((getName() == null && v.getName() != null) || 
		    	(getName() != null && v.getName() == null) ||
		    	!(getName().equals(v.getName())))
		    	return false;	    
		    if (getLength() != v.getLength())
		    	return false;
		    if (getPrecision() != v.getPrecision())
		    	return false;
	    }
	    
	    return true;
	}
    
    public ValueInterface getValueInterface()
    {
        return value;
    }
    
    public void setValueInterface(ValueInterface valueInterface)
    {
        this.value = valueInterface;
    }

    /**
     * Merges another Value. That means, that if the other Value has got the same name and is of the
     * same type as this Value, it's real field value is set as this this' value, if our value is
     * <code>null</code> or empty
     * 
     * @param other The other value
     */
    public void merge(Value other)
    {
        // Prechecks: Not null (of course) and same name and same type
        if (other == null || !getName().equals(other.getName()) || getType() != other.getType())
            return;

        switch (getType())
        {
            case VALUE_TYPE_BIGNUMBER:
                if (getBigNumber() == null)
                    setValue(other.getBigNumber());
                break;

            case VALUE_TYPE_BINARY:
                if (getBytes() == null || getBytes().length == 0)
                    if (other.getBytes() != null && other.getBytes().length > 0)
                        setValue(other.getBytes());
                break;

            case VALUE_TYPE_BOOLEAN:
                // 'false' cannot be said to be 'empty' (could be set on purpose) so we better don't overwrite
                // with 'true'.
                break;

            case VALUE_TYPE_DATE:
                if (getDate() == null)
                    setValue(other.getDate());
                break;
                
            case VALUE_TYPE_INTEGER:
                if (getInteger() == 0l)
                    setValue(other.getInteger());
                break;

            case VALUE_TYPE_NUMBER:
                if (getNumber() == 0.0)
                    setValue(other.getNumber());
                break;

            case VALUE_TYPE_SERIALIZABLE:
                // Cannot transfer serializables
                break;

            case VALUE_TYPE_STRING:
                if (Const.isEmpty(getString()) && !Const.isEmpty(other.getString()))
                    setValue(other.getString());
                break;
        }
    }
}
=======
package redis.clients.jedis;

import static redis.clients.jedis.Protocol.toByteArray;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.JedisByteHashMap;
import redis.clients.util.SafeEncoder;

public class BinaryJedis implements BinaryJedisCommands {
    protected Client client = null;

    public BinaryJedis(final String host) {
	URI uri = URI.create(host);
	if (uri.getScheme() != null && uri.getScheme().equals("redis")) {
	    client = new Client(uri.getHost(), uri.getPort());
	    client.auth(uri.getUserInfo().split(":", 2)[1]);
	    client.getStatusCodeReply();
	    client.select(Integer.parseInt(uri.getPath().split("/", 2)[1]));
	    client.getStatusCodeReply();
	} else {
	    client = new Client(host);
	}
    }

    public BinaryJedis(final String host, final int port) {
	client = new Client(host, port);
    }

    public BinaryJedis(final String host, final int port, final int timeout) {
	client = new Client(host, port);
	client.setTimeout(timeout);
    }

    public BinaryJedis(final JedisShardInfo shardInfo) {
	client = new Client(shardInfo.getHost(), shardInfo.getPort());
	client.setTimeout(shardInfo.getTimeout());
	client.setPassword(shardInfo.getPassword());
    }

    public BinaryJedis(URI uri) {
	client = new Client(uri.getHost(), uri.getPort());
	client.auth(uri.getUserInfo().split(":", 2)[1]);
	client.getStatusCodeReply();
	client.select(Integer.parseInt(uri.getPath().split("/", 2)[1]));
	client.getStatusCodeReply();
    }

    public String ping() {
	checkIsInMulti();
	client.ping();
	return client.getStatusCodeReply();
    }

    /**
     * Set the string value as value of the key. The string can't be longer than
     * 1073741824 bytes (1 GB).
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @param value
     * @return Status code reply
     */
    public String set(final byte[] key, final byte[] value) {
	checkIsInMulti();
	client.set(key, value);
	return client.getStatusCodeReply();
    }

    /**
     * Get the value of the specified key. If the key does not exist the special
     * value 'nil' is returned. If the value stored at key is not a string an
     * error is returned because GET can only handle string values.
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @return Bulk reply
     */
    public byte[] get(final byte[] key) {
	checkIsInMulti();
	client.get(key);
	return client.getBinaryBulkReply();
    }

    /**
     * Ask the server to silently close the connection.
     */
    public String quit() {
	checkIsInMulti();
	client.quit();
	return client.getStatusCodeReply();
    }

    /**
     * Test if the specified key exists. The command returns "1" if the key
     * exists, otherwise "0" is returned. Note that even keys set with an empty
     * string as value will return "1".
     * 
     * Time complexity: O(1)
     * 
     * @param key
     * @return Integer reply, "1" if the key exists, otherwise "0"
     */
    public Boolean exists(final byte[] key) {
	checkIsInMulti();
	client.exists(key);
	return client.getIntegerReply() == 1;
    }

    /**
     * Remove the specified keys. If a given key does not exist no operation is
     * performed for this key. The command returns the number of keys removed.
     * 
     * Time complexity: O(1)
     * 
     * @param keys
     * @return Integer reply, specifically: an integer greater than 0 if one or
     *         more keys were removed 0 if none of the specified key existed
     */
    public Long del(final byte[]... keys) {
	checkIsInMulti();
	client.del(keys);
	return client.getIntegerReply();
    }

    /**
     * Return the type of the value stored at key in form of a string. The type
     * can be one of "none", "string", "list", "set". "none" is returned if the
     * key does not exist.
     * 
     * Time complexity: O(1)
     * 
     * @param key
     * @return Status code reply, specifically: "none" if the key does not exist
     *         "string" if the key contains a String value "list" if the key
     *         contains a List value "set" if the key contains a Set value
     *         "zset" if the key contains a Sorted Set value "hash" if the key
     *         contains a Hash value
     */
    public String type(final byte[] key) {
	checkIsInMulti();
	client.type(key);
	return client.getStatusCodeReply();
    }

    /**
     * Delete all the keys of the currently selected DB. This command never
     * fails.
     * 
     * @return Status code reply
     */
    public String flushDB() {
	checkIsInMulti();
	client.flushDB();
	return client.getStatusCodeReply();
    }

    /**
     * Returns all the keys matching the glob-style pattern as space separated
     * strings. For example if you have in the database the keys "foo" and
     * "foobar" the command "KEYS foo*" will return "foo foobar".
     * <p>
     * Note that while the time complexity for this operation is O(n) the
     * constant times are pretty low. For example Redis running on an entry
     * level laptop can scan a 1 million keys database in 40 milliseconds.
     * <b>Still it's better to consider this one of the slow commands that may
     * ruin the DB performance if not used with care.</b>
     * <p>
     * In other words this command is intended only for debugging and special
     * operations like creating a script to change the DB schema. Don't use it
     * in your normal code. Use Redis Sets in order to group together a subset
     * of objects.
     * <p>
     * Glob style patterns examples:
     * <ul>
     * <li>h?llo will match hello hallo hhllo
     * <li>h*llo will match hllo heeeello
     * <li>h[ae]llo will match hello and hallo, but not hillo
     * </ul>
     * <p>
     * Use \ to escape special chars if you want to match them verbatim.
     * <p>
     * Time complexity: O(n) (with n being the number of keys in the DB, and
     * assuming keys and pattern of limited length)
     * 
     * @param pattern
     * @return Multi bulk reply
     */
    public Set<byte[]> keys(final byte[] pattern) {
	checkIsInMulti();
	client.keys(pattern);
	final HashSet<byte[]> keySet = new HashSet<byte[]>(
		client.getBinaryMultiBulkReply());
	return keySet;
    }

    /**
     * Return a randomly selected key from the currently selected DB.
     * <p>
     * Time complexity: O(1)
     * 
     * @return Singe line reply, specifically the randomly selected key or an
     *         empty string is the database is empty
     */
    public byte[] randomBinaryKey() {
	checkIsInMulti();
	client.randomKey();
	return client.getBinaryBulkReply();
    }

    /**
     * Atomically renames the key oldkey to newkey. If the source and
     * destination name are the same an error is returned. If newkey already
     * exists it is overwritten.
     * <p>
     * Time complexity: O(1)
     * 
     * @param oldkey
     * @param newkey
     * @return Status code repy
     */
    public String rename(final byte[] oldkey, final byte[] newkey) {
	checkIsInMulti();
	client.rename(oldkey, newkey);
	return client.getStatusCodeReply();
    }

    /**
     * Rename oldkey into newkey but fails if the destination key newkey already
     * exists.
     * <p>
     * Time complexity: O(1)
     * 
     * @param oldkey
     * @param newkey
     * @return Integer reply, specifically: 1 if the key was renamed 0 if the
     *         target key already exist
     */
    public Long renamenx(final byte[] oldkey, final byte[] newkey) {
	checkIsInMulti();
	client.renamenx(oldkey, newkey);
	return client.getIntegerReply();
    }

    /**
     * Return the number of keys in the currently selected database.
     * 
     * @return Integer reply
     */
    public Long dbSize() {
	checkIsInMulti();
	client.dbSize();
	return client.getIntegerReply();
    }

    /**
     * Set a timeout on the specified key. After the timeout the key will be
     * automatically deleted by the server. A key with an associated timeout is
     * said to be volatile in Redis terminology.
     * <p>
     * Voltile keys are stored on disk like the other keys, the timeout is
     * persistent too like all the other aspects of the dataset. Saving a
     * dataset containing expires and stopping the server does not stop the flow
     * of time as Redis stores on disk the time when the key will no longer be
     * available as Unix time, and not the remaining seconds.
     * <p>
     * Since Redis 2.1.3 you can update the value of the timeout of a key
     * already having an expire set. It is also possible to undo the expire at
     * all turning the key into a normal key using the {@link #persist(byte[])
     * PERSIST} command.
     * <p>
     * Time complexity: O(1)
     * 
     * @see <ahref="http://code.google.com/p/redis/wiki/ExpireCommand">ExpireCommand</a>
     * 
     * @param key
     * @param seconds
     * @return Integer reply, specifically: 1: the timeout was set. 0: the
     *         timeout was not set since the key already has an associated
     *         timeout (this may happen only in Redis versions < 2.1.3, Redis >=
     *         2.1.3 will happily update the timeout), or the key does not
     *         exist.
     */
    public Long expire(final byte[] key, final int seconds) {
	checkIsInMulti();
	client.expire(key, seconds);
	return client.getIntegerReply();
    }

    /**
     * EXPIREAT works exctly like {@link #expire(byte[], int) EXPIRE} but
     * instead to get the number of seconds representing the Time To Live of the
     * key as a second argument (that is a relative way of specifing the TTL),
     * it takes an absolute one in the form of a UNIX timestamp (Number of
     * seconds elapsed since 1 Gen 1970).
     * <p>
     * EXPIREAT was introduced in order to implement the Append Only File
     * persistence mode so that EXPIRE commands are automatically translated
     * into EXPIREAT commands for the append only file. Of course EXPIREAT can
     * also used by programmers that need a way to simply specify that a given
     * key should expire at a given time in the future.
     * <p>
     * Since Redis 2.1.3 you can update the value of the timeout of a key
     * already having an expire set. It is also possible to undo the expire at
     * all turning the key into a normal key using the {@link #persist(byte[])
     * PERSIST} command.
     * <p>
     * Time complexity: O(1)
     * 
     * @see <ahref="http://code.google.com/p/redis/wiki/ExpireCommand">ExpireCommand</a>
     * 
     * @param key
     * @param unixTime
     * @return Integer reply, specifically: 1: the timeout was set. 0: the
     *         timeout was not set since the key already has an associated
     *         timeout (this may happen only in Redis versions < 2.1.3, Redis >=
     *         2.1.3 will happily update the timeout), or the key does not
     *         exist.
     */
    public Long expireAt(final byte[] key, final long unixTime) {
	checkIsInMulti();
	client.expireAt(key, unixTime);
	return client.getIntegerReply();
    }

    /**
     * The TTL command returns the remaining time to live in seconds of a key
     * that has an {@link #expire(byte[], int) EXPIRE} set. This introspection
     * capability allows a Redis client to check how many seconds a given key
     * will continue to be part of the dataset.
     * 
     * @param key
     * @return Integer reply, returns the remaining time to live in seconds of a
     *         key that has an EXPIRE. If the Key does not exists or does not
     *         have an associated expire, -1 is returned.
     */
    public Long ttl(final byte[] key) {
	checkIsInMulti();
	client.ttl(key);
	return client.getIntegerReply();
    }

    /**
     * Select the DB with having the specified zero-based numeric index. For
     * default every new client connection is automatically selected to DB 0.
     * 
     * @param index
     * @return Status code reply
     */
    public String select(final int index) {
	checkIsInMulti();
	client.select(index);
	return client.getStatusCodeReply();
    }

    /**
     * Move the specified key from the currently selected DB to the specified
     * destination DB. Note that this command returns 1 only if the key was
     * successfully moved, and 0 if the target key was already there or if the
     * source key was not found at all, so it is possible to use MOVE as a
     * locking primitive.
     * 
     * @param key
     * @param dbIndex
     * @return Integer reply, specifically: 1 if the key was moved 0 if the key
     *         was not moved because already present on the target DB or was not
     *         found in the current DB.
     */
    public Long move(final byte[] key, final int dbIndex) {
	checkIsInMulti();
	client.move(key, dbIndex);
	return client.getIntegerReply();
    }

    /**
     * Delete all the keys of all the existing databases, not just the currently
     * selected one. This command never fails.
     * 
     * @return Status code reply
     */
    public String flushAll() {
	checkIsInMulti();
	client.flushAll();
	return client.getStatusCodeReply();
    }

    /**
     * GETSET is an atomic set this value and return the old value command. Set
     * key to the string value and return the old value stored at key. The
     * string can't be longer than 1073741824 bytes (1 GB).
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @param value
     * @return Bulk reply
     */
    public byte[] getSet(final byte[] key, final byte[] value) {
	checkIsInMulti();
	client.getSet(key, value);
	return client.getBinaryBulkReply();
    }

    /**
     * Get the values of all the specified keys. If one or more keys dont exist
     * or is not of type String, a 'nil' value is returned instead of the value
     * of the specified key, but the operation never fails.
     * <p>
     * Time complexity: O(1) for every key
     * 
     * @param keys
     * @return Multi bulk reply
     */
    public List<byte[]> mget(final byte[]... keys) {
	checkIsInMulti();
	client.mget(keys);
	return client.getBinaryMultiBulkReply();
    }

    /**
     * SETNX works exactly like {@link #set(byte[], byte[]) SET} with the only
     * difference that if the key already exists no operation is performed.
     * SETNX actually means "SET if Not eXists".
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @param value
     * @return Integer reply, specifically: 1 if the key was set 0 if the key
     *         was not set
     */
    public Long setnx(final byte[] key, final byte[] value) {
	checkIsInMulti();
	client.setnx(key, value);
	return client.getIntegerReply();
    }

    /**
     * The command is exactly equivalent to the following group of commands:
     * {@link #set(byte[], byte[]) SET} + {@link #expire(byte[], int) EXPIRE}.
     * The operation is atomic.
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @param seconds
     * @param value
     * @return Status code reply
     */
    public String setex(final byte[] key, final int seconds, final byte[] value) {
	checkIsInMulti();
	client.setex(key, seconds, value);
	return client.getStatusCodeReply();
    }

    /**
     * Set the the respective keys to the respective values. MSET will replace
     * old values with new values, while {@link #msetnx(String...) MSETNX} will
     * not perform any operation at all even if just a single key already
     * exists.
     * <p>
     * Because of this semantic MSETNX can be used in order to set different
     * keys representing different fields of an unique logic object in a way
     * that ensures that either all the fields or none at all are set.
     * <p>
     * Both MSET and MSETNX are atomic operations. This means that for instance
     * if the keys A and B are modified, another client talking to Redis can
     * either see the changes to both A and B at once, or no modification at
     * all.
     * 
     * @see #msetnx(String...)
     * 
     * @param keysvalues
     * @return Status code reply Basically +OK as MSET can't fail
     */
    public String mset(final byte[]... keysvalues) {
	checkIsInMulti();
	client.mset(keysvalues);
	return client.getStatusCodeReply();
    }

    /**
     * Set the the respective keys to the respective values.
     * {@link #mset(String...) MSET} will replace old values with new values,
     * while MSETNX will not perform any operation at all even if just a single
     * key already exists.
     * <p>
     * Because of this semantic MSETNX can be used in order to set different
     * keys representing different fields of an unique logic object in a way
     * that ensures that either all the fields or none at all are set.
     * <p>
     * Both MSET and MSETNX are atomic operations. This means that for instance
     * if the keys A and B are modified, another client talking to Redis can
     * either see the changes to both A and B at once, or no modification at
     * all.
     * 
     * @see #mset(String...)
     * 
     * @param keysvalues
     * @return Integer reply, specifically: 1 if the all the keys were set 0 if
     *         no key was set (at least one key already existed)
     */
    public Long msetnx(final byte[]... keysvalues) {
	checkIsInMulti();
	client.msetnx(keysvalues);
	return client.getIntegerReply();
    }

    /**
     * IDECRBY work just like {@link #decr(String) INCR} but instead to
     * decrement by 1 the decrement is integer.
     * <p>
     * INCR commands are limited to 64 bit signed integers.
     * <p>
     * Note: this is actually a string operation, that is, in Redis there are
     * not "integer" types. Simply the string stored at the key is parsed as a
     * base 10 64 bit signed integer, incremented, and then converted back as a
     * string.
     * <p>
     * Time complexity: O(1)
     * 
     * @see #incr(byte[])
     * @see #decr(byte[])
     * @see #incrBy(byte[], long)
     * 
     * @param key
     * @param integer
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */
    public Long decrBy(final byte[] key, final long integer) {
	checkIsInMulti();
	client.decrBy(key, integer);
	return client.getIntegerReply();
    }

    /**
     * Decrement the number stored at key by one. If the key does not exist or
     * contains a value of a wrong type, set the key to the value of "0" before
     * to perform the decrement operation.
     * <p>
     * INCR commands are limited to 64 bit signed integers.
     * <p>
     * Note: this is actually a string operation, that is, in Redis there are
     * not "integer" types. Simply the string stored at the key is parsed as a
     * base 10 64 bit signed integer, incremented, and then converted back as a
     * string.
     * <p>
     * Time complexity: O(1)
     * 
     * @see #incr(byte[])
     * @see #incrBy(byte[], long)
     * @see #decrBy(byte[], long)
     * 
     * @param key
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */
    public Long decr(final byte[] key) {
	checkIsInMulti();
	client.decr(key);
	return client.getIntegerReply();
    }

    /**
     * INCRBY work just like {@link #incr(byte[]) INCR} but instead to increment
     * by 1 the increment is integer.
     * <p>
     * INCR commands are limited to 64 bit signed integers.
     * <p>
     * Note: this is actually a string operation, that is, in Redis there are
     * not "integer" types. Simply the string stored at the key is parsed as a
     * base 10 64 bit signed integer, incremented, and then converted back as a
     * string.
     * <p>
     * Time complexity: O(1)
     * 
     * @see #incr(byte[])
     * @see #decr(byte[])
     * @see #decrBy(byte[], long)
     * 
     * @param key
     * @param integer
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */
    public Long incrBy(final byte[] key, final long integer) {
	checkIsInMulti();
	client.incrBy(key, integer);
	return client.getIntegerReply();
    }

    /**
     * Increment the number stored at key by one. If the key does not exist or
     * contains a value of a wrong type, set the key to the value of "0" before
     * to perform the increment operation.
     * <p>
     * INCR commands are limited to 64 bit signed integers.
     * <p>
     * Note: this is actually a string operation, that is, in Redis there are
     * not "integer" types. Simply the string stored at the key is parsed as a
     * base 10 64 bit signed integer, incremented, and then converted back as a
     * string.
     * <p>
     * Time complexity: O(1)
     * 
     * @see #incrBy(byte[], long)
     * @see #decr(byte[])
     * @see #decrBy(byte[], long)
     * 
     * @param key
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */
    public Long incr(final byte[] key) {
	checkIsInMulti();
	client.incr(key);
	return client.getIntegerReply();
    }

    /**
     * If the key already exists and is a string, this command appends the
     * provided value at the end of the string. If the key does not exist it is
     * created and set as an empty string, so APPEND will be very similar to SET
     * in this special case.
     * <p>
     * Time complexity: O(1). The amortized time complexity is O(1) assuming the
     * appended value is small and the already present value is of any size,
     * since the dynamic string library used by Redis will double the free space
     * available on every reallocation.
     * 
     * @param key
     * @param value
     * @return Integer reply, specifically the total length of the string after
     *         the append operation.
     */
    public Long append(final byte[] key, final byte[] value) {
	checkIsInMulti();
	client.append(key, value);
	return client.getIntegerReply();
    }

    /**
     * Return a subset of the string from offset start to offset end (both
     * offsets are inclusive). Negative offsets can be used in order to provide
     * an offset starting from the end of the string. So -1 means the last char,
     * -2 the penultimate and so forth.
     * <p>
     * The function handles out of range requests without raising an error, but
     * just limiting the resulting range to the actual length of the string.
     * <p>
     * Time complexity: O(start+n) (with start being the start index and n the
     * total length of the requested range). Note that the lookup part of this
     * command is O(1) so for small strings this is actually an O(1) command.
     * 
     * @param key
     * @param start
     * @param end
     * @return Bulk reply
     */
    public byte[] substr(final byte[] key, final int start, final int end) {
	checkIsInMulti();
	client.substr(key, start, end);
	return client.getBinaryBulkReply();
    }

    /**
     * 
     * Set the specified hash field to the specified value.
     * <p>
     * If key does not exist, a new key holding a hash is created.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @param value
     * @return If the field already exists, and the HSET just produced an update
     *         of the value, 0 is returned, otherwise if a new field is created
     *         1 is returned.
     */
    public Long hset(final byte[] key, final byte[] field, final byte[] value) {
	checkIsInMulti();
	client.hset(key, field, value);
	return client.getIntegerReply();
    }

    /**
     * If key holds a hash, retrieve the value associated to the specified
     * field.
     * <p>
     * If the field is not found or the key does not exist, a special 'nil'
     * value is returned.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @return Bulk reply
     */
    public byte[] hget(final byte[] key, final byte[] field) {
	checkIsInMulti();
	client.hget(key, field);
	return client.getBinaryBulkReply();
    }

    /**
     * 
     * Set the specified hash field to the specified value if the field not
     * exists. <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @param value
     * @return If the field already exists, 0 is returned, otherwise if a new
     *         field is created 1 is returned.
     */
    public Long hsetnx(final byte[] key, final byte[] field, final byte[] value) {
	checkIsInMulti();
	client.hsetnx(key, field, value);
	return client.getIntegerReply();
    }

    /**
     * Set the respective fields to the respective values. HMSET replaces old
     * values with new values.
     * <p>
     * If key does not exist, a new key holding a hash is created.
     * <p>
     * <b>Time complexity:</b> O(N) (with N being the number of fields)
     * 
     * @param key
     * @param hash
     * @return Always OK because HMSET can't fail
     */
    public String hmset(final byte[] key, final Map<byte[], byte[]> hash) {
	checkIsInMulti();
	client.hmset(key, hash);
	return client.getStatusCodeReply();
    }

    /**
     * Retrieve the values associated to the specified fields.
     * <p>
     * If some of the specified fields do not exist, nil values are returned.
     * Non existing keys are considered like empty hashes.
     * <p>
     * <b>Time complexity:</b> O(N) (with N being the number of fields)
     * 
     * @param key
     * @param fields
     * @return Multi Bulk Reply specifically a list of all the values associated
     *         with the specified fields, in the same order of the request.
     */
    public List<byte[]> hmget(final byte[] key, final byte[]... fields) {
	checkIsInMulti();
	client.hmget(key, fields);
	return client.getBinaryMultiBulkReply();
    }

    /**
     * Increment the number stored at field in the hash at key by value. If key
     * does not exist, a new key holding a hash is created. If field does not
     * exist or holds a string, the value is set to 0 before applying the
     * operation. Since the value argument is signed you can use this command to
     * perform both increments and decrements.
     * <p>
     * The range of values supported by HINCRBY is limited to 64 bit signed
     * integers.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @param value
     * @return Integer reply The new value at field after the increment
     *         operation.
     */
    public Long hincrBy(final byte[] key, final byte[] field, final long value) {
	checkIsInMulti();
	client.hincrBy(key, field, value);
	return client.getIntegerReply();
    }

    /**
     * Test for existence of a specified field in a hash.
     * 
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @return Return 1 if the hash stored at key contains the specified field.
     *         Return 0 if the key is not found or the field is not present.
     */
    public Boolean hexists(final byte[] key, final byte[] field) {
	checkIsInMulti();
	client.hexists(key, field);
	return client.getIntegerReply() == 1;
    }

    /**
     * Remove the specified field from an hash stored at key.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param fields
     * @return If the field was present in the hash it is deleted and 1 is
     *         returned, otherwise 0 is returned and no operation is performed.
     */
    public Long hdel(final byte[] key, final byte[]... fields) {
	checkIsInMulti();
	client.hdel(key, fields);
	return client.getIntegerReply();
    }

    /**
     * Return the number of items in a hash.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @return The number of entries (fields) contained in the hash stored at
     *         key. If the specified key does not exist, 0 is returned assuming
     *         an empty hash.
     */
    public Long hlen(final byte[] key) {
	checkIsInMulti();
	client.hlen(key);
	return client.getIntegerReply();
    }

    /**
     * Return all the fields in a hash.
     * <p>
     * <b>Time complexity:</b> O(N), where N is the total number of entries
     * 
     * @param key
     * @return All the fields names contained into a hash.
     */
    public Set<byte[]> hkeys(final byte[] key) {
	checkIsInMulti();
	client.hkeys(key);
	final List<byte[]> lresult = client.getBinaryMultiBulkReply();
	return new HashSet<byte[]>(lresult);
    }

    /**
     * Return all the values in a hash.
     * <p>
     * <b>Time complexity:</b> O(N), where N is the total number of entries
     * 
     * @param key
     * @return All the fields values contained into a hash.
     */
    public List<byte[]> hvals(final byte[] key) {
	checkIsInMulti();
	client.hvals(key);
	final List<byte[]> lresult = client.getBinaryMultiBulkReply();
	return lresult;
    }

    /**
     * Return all the fields and associated values in a hash.
     * <p>
     * <b>Time complexity:</b> O(N), where N is the total number of entries
     * 
     * @param key
     * @return All the fields and values contained into a hash.
     */
    public Map<byte[], byte[]> hgetAll(final byte[] key) {
	checkIsInMulti();
	client.hgetAll(key);
	final List<byte[]> flatHash = client.getBinaryMultiBulkReply();
	final Map<byte[], byte[]> hash = new JedisByteHashMap();
	final Iterator<byte[]> iterator = flatHash.iterator();
	while (iterator.hasNext()) {
	    hash.put(iterator.next(), iterator.next());
	}

	return hash;
    }

    /**
     * Add the string value to the head (LPUSH) or tail (RPUSH) of the list
     * stored at key. If the key does not exist an empty list is created just
     * before the append operation. If the key exists but is not a List an error
     * is returned.
     * <p>
     * Time complexity: O(1)
     * 
     * @see BinaryJedis#rpush(byte[], byte[]...)
     * 
     * @param key
     * @param strings
     * @return Integer reply, specifically, the number of elements inside the
     *         list after the push operation.
     */
    public Long rpush(final byte[] key, final byte[]... strings) {
	checkIsInMulti();
	client.rpush(key, strings);
	return client.getIntegerReply();
    }

    /**
     * Add the string value to the head (LPUSH) or tail (RPUSH) of the list
     * stored at key. If the key does not exist an empty list is created just
     * before the append operation. If the key exists but is not a List an error
     * is returned.
     * <p>
     * Time complexity: O(1)
     * 
     * @see BinaryJedis#rpush(byte[], byte[]...)
     * 
     * @param key
     * @param strings
     * @return Integer reply, specifically, the number of elements inside the
     *         list after the push operation.
     */
    public Long lpush(final byte[] key, final byte[]... strings) {
	checkIsInMulti();
	client.lpush(key, strings);
	return client.getIntegerReply();
    }

    /**
     * Return the length of the list stored at the specified key. If the key
     * does not exist zero is returned (the same behaviour as for empty lists).
     * If the value stored at key is not a list an error is returned.
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @return The length of the list.
     */
    public Long llen(final byte[] key) {
	checkIsInMulti();
	client.llen(key);
	return client.getIntegerReply();
    }

    /**
     * Return the specified elements of the list stored at the specified key.
     * Start and end are zero-based indexes. 0 is the first element of the list
     * (the list head), 1 the next element and so on.
     * <p>
     * For example LRANGE foobar 0 2 will return the first three elements of the
     * list.
     * <p>
     * start and end can also be negative numbers indicating offsets from the
     * end of the list. For example -1 is the last element of the list, -2 the
     * penultimate element and so on.
     * <p>
     * <b>Consistency with range functions in various programming languages</b>
     * <p>
     * Note that if you have a list of numbers from 0 to 100, LRANGE 0 10 will
     * return 11 elements, that is, rightmost item is included. This may or may
     * not be consistent with behavior of range-related functions in your
     * programming language of choice (think Ruby's Range.new, Array#slice or
     * Python's range() function).
     * <p>
     * LRANGE behavior is consistent with one of Tcl.
     * <p>
     * <b>Out-of-range indexes</b>
     * <p>
     * Indexes out of range will not produce an error: if start is over the end
     * of the list, or start > end, an empty list is returned. If end is over
     * the end of the list Redis will threat it just like the last element of
     * the list.
     * <p>
     * Time complexity: O(start+n) (with n being the length of the range and
     * start being the start offset)
     * 
     * @param key
     * @param start
     * @param end
     * @return Multi bulk reply, specifically a list of elements in the
     *         specified range.
     */
    public List<byte[]> lrange(final byte[] key, final int start, final int end) {
	checkIsInMulti();
	client.lrange(key, start, end);
	return client.getBinaryMultiBulkReply();
    }

    /**
     * Trim an existing list so that it will contain only the specified range of
     * elements specified. Start and end are zero-based indexes. 0 is the first
     * element of the list (the list head), 1 the next element and so on.
     * <p>
     * For example LTRIM foobar 0 2 will modify the list stored at foobar key so
     * that only the first three elements of the list will remain.
     * <p>
     * start and end can also be negative numbers indicating offsets from the
     * end of the list. For example -1 is the last element of the list, -2 the
     * penultimate element and so on.
     * <p>
     * Indexes out of range will not produce an error: if start is over the end
     * of the list, or start > end, an empty list is left as value. If end over
     * the end of the list Redis will threat it just like the last element of
     * the list.
     * <p>
     * Hint: the obvious use of LTRIM is together with LPUSH/RPUSH. For example:
     * <p>
     * {@code lpush("mylist", "someelement"); ltrim("mylist", 0, 99); * }
     * <p>
     * The above two commands will push elements in the list taking care that
     * the list will not grow without limits. This is very useful when using
     * Redis to store logs for example. It is important to note that when used
     * in this way LTRIM is an O(1) operation because in the average case just
     * one element is removed from the tail of the list.
     * <p>
     * Time complexity: O(n) (with n being len of list - len of range)
     * 
     * @param key
     * @param start
     * @param end
     * @return Status code reply
     */
    public String ltrim(final byte[] key, final int start, final int end) {
	checkIsInMulti();
	client.ltrim(key, start, end);
	return client.getStatusCodeReply();
    }

    /**
     * Return the specified element of the list stored at the specified key. 0
     * is the first element, 1 the second and so on. Negative indexes are
     * supported, for example -1 is the last element, -2 the penultimate and so
     * on.
     * <p>
     * If the value stored at key is not of list type an error is returned. If
     * the index is out of range a 'nil' reply is returned.
     * <p>
     * Note that even if the average time complexity is O(n) asking for the
     * first or the last element of the list is O(1).
     * <p>
     * Time complexity: O(n) (with n being the length of the list)
     * 
     * @param key
     * @param index
     * @return Bulk reply, specifically the requested element
     */
    public byte[] lindex(final byte[] key, final int index) {
	checkIsInMulti();
	client.lindex(key, index);
	return client.getBinaryBulkReply();
    }

    /**
     * Set a new value as the element at index position of the List at key.
     * <p>
     * Out of range indexes will generate an error.
     * <p>
     * Similarly to other list commands accepting indexes, the index can be
     * negative to access elements starting from the end of the list. So -1 is
     * the last element, -2 is the penultimate, and so forth.
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(N) (with N being the length of the list), setting the first or last
     * elements of the list is O(1).
     * 
     * @see #lindex(byte[], int)
     * 
     * @param key
     * @param index
     * @param value
     * @return Status code reply
     */
    public String lset(final byte[] key, final int index, final byte[] value) {
	checkIsInMulti();
	client.lset(key, index, value);
	return client.getStatusCodeReply();
    }

    /**
     * Remove the first count occurrences of the value element from the list. If
     * count is zero all the elements are removed. If count is negative elements
     * are removed from tail to head, instead to go from head to tail that is
     * the normal behaviour. So for example LREM with count -2 and hello as
     * value to remove against the list (a,b,c,hello,x,hello,hello) will have
     * the list (a,b,c,hello,x). The number of removed elements is returned as
     * an integer, see below for more information about the returned value. Note
     * that non existing keys are considered like empty lists by LREM, so LREM
     * against non existing keys will always return 0.
     * <p>
     * Time complexity: O(N) (with N being the length of the list)
     * 
     * @param key
     * @param count
     * @param value
     * @return Integer Reply, specifically: The number of removed elements if
     *         the operation succeeded
     */
    public Long lrem(final byte[] key, final int count, final byte[] value) {
	checkIsInMulti();
	client.lrem(key, count, value);
	return client.getIntegerReply();
    }

    /**
     * Atomically return and remove the first (LPOP) or last (RPOP) element of
     * the list. For example if the list contains the elements "a","b","c" LPOP
     * will return "a" and the list will become "b","c".
     * <p>
     * If the key does not exist or the list is already empty the special value
     * 'nil' is returned.
     * 
     * @see #rpop(byte[])
     * 
     * @param key
     * @return Bulk reply
     */
    public byte[] lpop(final byte[] key) {
	checkIsInMulti();
	client.lpop(key);
	return client.getBinaryBulkReply();
    }

    /**
     * Atomically return and remove the first (LPOP) or last (RPOP) element of
     * the list. For example if the list contains the elements "a","b","c" LPOP
     * will return "a" and the list will become "b","c".
     * <p>
     * If the key does not exist or the list is already empty the special value
     * 'nil' is returned.
     * 
     * @see #lpop(byte[])
     * 
     * @param key
     * @return Bulk reply
     */
    public byte[] rpop(final byte[] key) {
	checkIsInMulti();
	client.rpop(key);
	return client.getBinaryBulkReply();
    }

    /**
     * Atomically return and remove the last (tail) element of the srckey list,
     * and push the element as the first (head) element of the dstkey list. For
     * example if the source list contains the elements "a","b","c" and the
     * destination list contains the elements "foo","bar" after an RPOPLPUSH
     * command the content of the two lists will be "a","b" and "c","foo","bar".
     * <p>
     * If the key does not exist or the list is already empty the special value
     * 'nil' is returned. If the srckey and dstkey are the same the operation is
     * equivalent to removing the last element from the list and pusing it as
     * first element of the list, so it's a "list rotation" command.
     * <p>
     * Time complexity: O(1)
     * 
     * @param srckey
     * @param dstkey
     * @return Bulk reply
     */
    public byte[] rpoplpush(final byte[] srckey, final byte[] dstkey) {
	checkIsInMulti();
	client.rpoplpush(srckey, dstkey);
	return client.getBinaryBulkReply();
    }

    /**
     * Add the specified member to the set value stored at key. If member is
     * already a member of the set no operation is performed. If key does not
     * exist a new set with the specified member as sole member is created. If
     * the key exists but does not hold a set value an error is returned.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @param members
     * @return Integer reply, specifically: 1 if the new element was added 0 if
     *         the element was already a member of the set
     */
    public Long sadd(final byte[] key, final byte[]... members) {
	checkIsInMulti();
	client.sadd(key, members);
	return client.getIntegerReply();
    }

    /**
     * Return all the members (elements) of the set value stored at key. This is
     * just syntax glue for {@link #sinter(String...) SINTER}.
     * <p>
     * Time complexity O(N)
     * 
     * @param key
     * @return Multi bulk reply
     */
    public Set<byte[]> smembers(final byte[] key) {
	checkIsInMulti();
	client.smembers(key);
	final List<byte[]> members = client.getBinaryMultiBulkReply();
	return new HashSet<byte[]>(members);
    }

    /**
     * Remove the specified member from the set value stored at key. If member
     * was not a member of the set no operation is performed. If key does not
     * hold a set value an error is returned.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @param member
     * @return Integer reply, specifically: 1 if the new element was removed 0
     *         if the new element was not a member of the set
     */
    public Long srem(final byte[] key, final byte[]... member) {
	checkIsInMulti();
	client.srem(key, member);
	return client.getIntegerReply();
    }

    /**
     * Remove a random element from a Set returning it as return value. If the
     * Set is empty or the key does not exist, a nil object is returned.
     * <p>
     * The {@link #srandmember(byte[])} command does a similar work but the
     * returned element is not removed from the Set.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @return Bulk reply
     */
    public byte[] spop(final byte[] key) {
	checkIsInMulti();
	client.spop(key);
	return client.getBinaryBulkReply();
    }

    /**
     * Move the specified member from the set at srckey to the set at dstkey.
     * This operation is atomic, in every given moment the element will appear
     * to be in the source or destination set for accessing clients.
     * <p>
     * If the source set does not exist or does not contain the specified
     * element no operation is performed and zero is returned, otherwise the
     * element is removed from the source set and added to the destination set.
     * On success one is returned, even if the element was already present in
     * the destination set.
     * <p>
     * An error is raised if the source or destination keys contain a non Set
     * value.
     * <p>
     * Time complexity O(1)
     * 
     * @param srckey
     * @param dstkey
     * @param member
     * @return Integer reply, specifically: 1 if the element was moved 0 if the
     *         element was not found on the first set and no operation was
     *         performed
     */
    public Long smove(final byte[] srckey, final byte[] dstkey,
	    final byte[] member) {
	checkIsInMulti();
	client.smove(srckey, dstkey, member);
	return client.getIntegerReply();
    }

    /**
     * Return the set cardinality (number of elements). If the key does not
     * exist 0 is returned, like for empty sets.
     * 
     * @param key
     * @return Integer reply, specifically: the cardinality (number of elements)
     *         of the set as an integer.
     */
    public Long scard(final byte[] key) {
	checkIsInMulti();
	client.scard(key);
	return client.getIntegerReply();
    }

    /**
     * Return 1 if member is a member of the set stored at key, otherwise 0 is
     * returned.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @param member
     * @return Integer reply, specifically: 1 if the element is a member of the
     *         set 0 if the element is not a member of the set OR if the key
     *         does not exist
     */
    public Boolean sismember(final byte[] key, final byte[] member) {
	checkIsInMulti();
	client.sismember(key, member);
	return client.getIntegerReply() == 1;
    }

    /**
     * Return the members of a set resulting from the intersection of all the
     * sets hold at the specified keys. Like in
     * {@link #lrange(byte[], int, int) LRANGE} the result is sent to the client
     * as a multi-bulk reply (see the protocol specification for more
     * information). If just a single key is specified, then this command
     * produces the same result as {@link #smembers(byte[]) SMEMBERS}. Actually
     * SMEMBERS is just syntax sugar for SINTER.
     * <p>
     * Non existing keys are considered like empty sets, so if one of the keys
     * is missing an empty set is returned (since the intersection with an empty
     * set always is an empty set).
     * <p>
     * Time complexity O(N*M) worst case where N is the cardinality of the
     * smallest set and M the number of sets
     * 
     * @param keys
     * @return Multi bulk reply, specifically the list of common elements.
     */
    public Set<byte[]> sinter(final byte[]... keys) {
	checkIsInMulti();
	client.sinter(keys);
	final List<byte[]> members = client.getBinaryMultiBulkReply();
	return new HashSet<byte[]>(members);
    }

    /**
     * This commnad works exactly like {@link #sinter(String...) SINTER} but
     * instead of being returned the resulting set is sotred as dstkey.
     * <p>
     * Time complexity O(N*M) worst case where N is the cardinality of the
     * smallest set and M the number of sets
     * 
     * @param dstkey
     * @param keys
     * @return Status code reply
     */
    public Long sinterstore(final byte[] dstkey, final byte[]... keys) {
	checkIsInMulti();
	client.sinterstore(dstkey, keys);
	return client.getIntegerReply();
    }

    /**
     * Return the members of a set resulting from the union of all the sets hold
     * at the specified keys. Like in {@link #lrange(byte[], int, int) LRANGE}
     * the result is sent to the client as a multi-bulk reply (see the protocol
     * specification for more information). If just a single key is specified,
     * then this command produces the same result as {@link #smembers(byte[])
     * SMEMBERS}.
     * <p>
     * Non existing keys are considered like empty sets.
     * <p>
     * Time complexity O(N) where N is the total number of elements in all the
     * provided sets
     * 
     * @param keys
     * @return Multi bulk reply, specifically the list of common elements.
     */
    public Set<byte[]> sunion(final byte[]... keys) {
	checkIsInMulti();
	client.sunion(keys);
	final List<byte[]> members = client.getBinaryMultiBulkReply();
	return new HashSet<byte[]>(members);
    }

    /**
     * This command works exactly like {@link #sunion(String...) SUNION} but
     * instead of being returned the resulting set is stored as dstkey. Any
     * existing value in dstkey will be over-written.
     * <p>
     * Time complexity O(N) where N is the total number of elements in all the
     * provided sets
     * 
     * @param dstkey
     * @param keys
     * @return Status code reply
     */
    public Long sunionstore(final byte[] dstkey, final byte[]... keys) {
	checkIsInMulti();
	client.sunionstore(dstkey, keys);
	return client.getIntegerReply();
    }

    /**
     * Return the difference between the Set stored at key1 and all the Sets
     * key2, ..., keyN
     * <p>
     * <b>Example:</b>
     * 
     * <pre>
     * key1 = [x, a, b, c]
     * key2 = [c]
     * key3 = [a, d]
     * SDIFF key1,key2,key3 => [x, b]
     * </pre>
     * 
     * Non existing keys are considered like empty sets.
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(N) with N being the total number of elements of all the sets
     * 
     * @param keys
     * @return Return the members of a set resulting from the difference between
     *         the first set provided and all the successive sets.
     */
    public Set<byte[]> sdiff(final byte[]... keys) {
	checkIsInMulti();
	client.sdiff(keys);
	final List<byte[]> members = client.getBinaryMultiBulkReply();
	return new HashSet<byte[]>(members);
    }

    /**
     * This command works exactly like {@link #sdiff(String...) SDIFF} but
     * instead of being returned the resulting set is stored in dstkey.
     * 
     * @param dstkey
     * @param keys
     * @return Status code reply
     */
    public Long sdiffstore(final byte[] dstkey, final byte[]... keys) {
	checkIsInMulti();
	client.sdiffstore(dstkey, keys);
	return client.getIntegerReply();
    }

    /**
     * Return a random element from a Set, without removing the element. If the
     * Set is empty or the key does not exist, a nil object is returned.
     * <p>
     * The SPOP command does a similar work but the returned element is popped
     * (removed) from the Set.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @return Bulk reply
     */
    public byte[] srandmember(final byte[] key) {
	checkIsInMulti();
	client.srandmember(key);
	return client.getBinaryBulkReply();
    }

    /**
     * Add the specified member having the specifeid score to the sorted set
     * stored at key. If member is already a member of the sorted set the score
     * is updated, and the element reinserted in the right position to ensure
     * sorting. If key does not exist a new sorted set with the specified member
     * as sole member is crated. If the key exists but does not hold a sorted
     * set value an error is returned.
     * <p>
     * The score value can be the string representation of a double precision
     * floating point number.
     * <p>
     * Time complexity O(log(N)) with N being the number of elements in the
     * sorted set
     * 
     * @param key
     * @param score
     * @param member
     * @return Integer reply, specifically: 1 if the new element was added 0 if
     *         the element was already a member of the sorted set and the score
     *         was updated
     */
    public Long zadd(final byte[] key, final double score, final byte[] member) {
	checkIsInMulti();
	client.zadd(key, score, member);
	return client.getIntegerReply();
    }

    public Long zadd(final byte[] key, final Map<Double, byte[]> scoreMembers) {
	checkIsInMulti();
	client.zaddBinary(key, scoreMembers);
	return client.getIntegerReply();
    }

    public Set<byte[]> zrange(final byte[] key, final int start, final int end) {
	checkIsInMulti();
	client.zrange(key, start, end);
	final List<byte[]> members = client.getBinaryMultiBulkReply();
	return new LinkedHashSet<byte[]>(members);
    }

    /**
     * Remove the specified member from the sorted set value stored at key. If
     * member was not a member of the set no operation is performed. If key does
     * not not hold a set value an error is returned.
     * <p>
     * Time complexity O(log(N)) with N being the number of elements in the
     * sorted set
     * 
     * 
     * 
     * @param key
     * @param members
     * @return Integer reply, specifically: 1 if the new element was removed 0
     *         if the new element was not a member of the set
     */
    public Long zrem(final byte[] key, final byte[]... members) {
	checkIsInMulti();
	client.zrem(key, members);
	return client.getIntegerReply();
    }

    /**
     * If member already exists in the sorted set adds the increment to its
     * score and updates the position of the element in the sorted set
     * accordingly. If member does not already exist in the sorted set it is
     * added with increment as score (that is, like if the previous score was
     * virtually zero). If key does not exist a new sorted set with the
     * specified member as sole member is crated. If the key exists but does not
     * hold a sorted set value an error is returned.
     * <p>
     * The score value can be the string representation of a double precision
     * floating point number. It's possible to provide a negative value to
     * perform a decrement.
     * <p>
     * For an introduction to sorted sets check the Introduction to Redis data
     * types page.
     * <p>
     * Time complexity O(log(N)) with N being the number of elements in the
     * sorted set
     * 
     * @param key
     * @param score
     * @param member
     * @return The new score
     */
    public Double zincrby(final byte[] key, final double score,
	    final byte[] member) {
	checkIsInMulti();
	client.zincrby(key, score, member);
	String newscore = client.getBulkReply();
	return Double.valueOf(newscore);
    }

    /**
     * Return the rank (or index) or member in the sorted set at key, with
     * scores being ordered from low to high.
     * <p>
     * When the given member does not exist in the sorted set, the special value
     * 'nil' is returned. The returned rank (or index) of the member is 0-based
     * for both commands.
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))
     * 
     * @see #zrevrank(byte[], byte[])
     * 
     * @param key
     * @param member
     * @return Integer reply or a nil bulk reply, specifically: the rank of the
     *         element as an integer reply if the element exists. A nil bulk
     *         reply if there is no such element.
     */
    public Long zrank(final byte[] key, final byte[] member) {
	checkIsInMulti();
	client.zrank(key, member);
	return client.getIntegerReply();
    }

    /**
     * Return the rank (or index) or member in the sorted set at key, with
     * scores being ordered from high to low.
     * <p>
     * When the given member does not exist in the sorted set, the special value
     * 'nil' is returned. The returned rank (or index) of the member is 0-based
     * for both commands.
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))
     * 
     * @see #zrank(byte[], byte[])
     * 
     * @param key
     * @param member
     * @return Integer reply or a nil bulk reply, specifically: the rank of the
     *         element as an integer reply if the element exists. A nil bulk
     *         reply if there is no such element.
     */
    public Long zrevrank(final byte[] key, final byte[] member) {
	checkIsInMulti();
	client.zrevrank(key, member);
	return client.getIntegerReply();
    }

    public Set<byte[]> zrevrange(final byte[] key, final int start,
	    final int end) {
	checkIsInMulti();
	client.zrevrange(key, start, end);
	final List<byte[]> members = client.getBinaryMultiBulkReply();
	return new LinkedHashSet<byte[]>(members);
    }

    public Set<Tuple> zrangeWithScores(final byte[] key, final int start,
	    final int end) {
	checkIsInMulti();
	client.zrangeWithScores(key, start, end);
	Set<Tuple> set = getBinaryTupledSet();
	return set;
    }

    public Set<Tuple> zrevrangeWithScores(final byte[] key, final int start,
	    final int end) {
	checkIsInMulti();
	client.zrevrangeWithScores(key, start, end);
	Set<Tuple> set = getBinaryTupledSet();
	return set;
    }

    /**
     * Return the sorted set cardinality (number of elements). If the key does
     * not exist 0 is returned, like for empty sorted sets.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @return the cardinality (number of elements) of the set as an integer.
     */
    public Long zcard(final byte[] key) {
	checkIsInMulti();
	client.zcard(key);
	return client.getIntegerReply();
    }

    /**
     * Return the score of the specified element of the sorted set at key. If
     * the specified element does not exist in the sorted set, or the key does
     * not exist at all, a special 'nil' value is returned.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param member
     * @return the score
     */
    public Double zscore(final byte[] key, final byte[] member) {
	checkIsInMulti();
	client.zscore(key, member);
	final String score = client.getBulkReply();
	return (score != null ? new Double(score) : null);
    }

    public Transaction multi() {
	client.multi();
	return new Transaction(client);
    }

    public List<Object> multi(final TransactionBlock jedisTransaction) {
	List<Object> results = null;
	jedisTransaction.setClient(client);
	try {
	    client.multi();
	    jedisTransaction.execute();
	    results = jedisTransaction.exec();
	} catch (Exception ex) {
	    jedisTransaction.discard();
	}
	return results;
    }

    protected void checkIsInMulti() {
	if (client.isInMulti()) {
	    throw new JedisDataException(
		    "Cannot use Jedis when in Multi. Please use JedisTransaction instead.");
	}
    }

    public void connect() {
	client.connect();
    }

    public void disconnect() {
	client.disconnect();
    }

    public String watch(final byte[]... keys) {
	client.watch(keys);
	return client.getStatusCodeReply();
    }

    public String unwatch() {
	client.unwatch();
	return client.getStatusCodeReply();
    }

    /**
     * Sort a Set or a List.
     * <p>
     * Sort the elements contained in the List, Set, or Sorted Set value at key.
     * By default sorting is numeric with elements being compared as double
     * precision floating point numbers. This is the simplest form of SORT.
     * 
     * @see #sort(byte[], byte[])
     * @see #sort(byte[], SortingParams)
     * @see #sort(byte[], SortingParams, byte[])
     * 
     * 
     * @param key
     * @return Assuming the Set/List at key contains a list of numbers, the
     *         return value will be the list of numbers ordered from the
     *         smallest to the biggest number.
     */
    public List<byte[]> sort(final byte[] key) {
	checkIsInMulti();
	client.sort(key);
	return client.getBinaryMultiBulkReply();
    }

    /**
     * Sort a Set or a List accordingly to the specified parameters.
     * <p>
     * <b>examples:</b>
     * <p>
     * Given are the following sets and key/values:
     * 
     * <pre>
     * x = [1, 2, 3]
     * y = [a, b, c]
     * 
     * k1 = z
     * k2 = y
     * k3 = x
     * 
     * w1 = 9
     * w2 = 8
     * w3 = 7
     * </pre>
     * 
     * Sort Order:
     * 
     * <pre>
     * sort(x) or sort(x, sp.asc())
     * -> [1, 2, 3]
     * 
     * sort(x, sp.desc())
     * -> [3, 2, 1]
     * 
     * sort(y)
     * -> [c, a, b]
     * 
     * sort(y, sp.alpha())
     * -> [a, b, c]
     * 
     * sort(y, sp.alpha().desc())
     * -> [c, a, b]
     * </pre>
     * 
     * Limit (e.g. for Pagination):
     * 
     * <pre>
     * sort(x, sp.limit(0, 2))
     * -> [1, 2]
     * 
     * sort(y, sp.alpha().desc().limit(1, 2))
     * -> [b, a]
     * </pre>
     * 
     * Sorting by external keys:
     * 
     * <pre>
     * sort(x, sb.by(w*))
     * -> [3, 2, 1]
     * 
     * sort(x, sb.by(w*).desc())
     * -> [1, 2, 3]
     * </pre>
     * 
     * Getting external keys:
     * 
     * <pre>
     * sort(x, sp.by(w*).get(k*))
     * -> [x, y, z]
     * 
     * sort(x, sp.by(w*).get(#).get(k*))
     * -> [3, x, 2, y, 1, z]
     * </pre>
     * 
     * @see #sort(byte[])
     * @see #sort(byte[], SortingParams, byte[])
     * 
     * @param key
     * @param sortingParameters
     * @return a list of sorted elements.
     */
    public List<byte[]> sort(final byte[] key,
	    final SortingParams sortingParameters) {
	checkIsInMulti();
	client.sort(key, sortingParameters);
	return client.getBinaryMultiBulkReply();
    }

    /**
     * BLPOP (and BRPOP) is a blocking list pop primitive. You can see this
     * commands as blocking versions of LPOP and RPOP able to block if the
     * specified keys don't exist or contain empty lists.
     * <p>
     * The following is a description of the exact semantic. We describe BLPOP
     * but the two commands are identical, the only difference is that BLPOP
     * pops the element from the left (head) of the list, and BRPOP pops from
     * the right (tail).
     * <p>
     * <b>Non blocking behavior</b>
     * <p>
     * When BLPOP is called, if at least one of the specified keys contain a non
     * empty list, an element is popped from the head of the list and returned
     * to the caller together with the name of the key (BLPOP returns a two
     * elements array, the first element is the key, the second the popped
     * value).
     * <p>
     * Keys are scanned from left to right, so for instance if you issue BLPOP
     * list1 list2 list3 0 against a dataset where list1 does not exist but
     * list2 and list3 contain non empty lists, BLPOP guarantees to return an
     * element from the list stored at list2 (since it is the first non empty
     * list starting from the left).
     * <p>
     * <b>Blocking behavior</b>
     * <p>
     * If none of the specified keys exist or contain non empty lists, BLPOP
     * blocks until some other client performs a LPUSH or an RPUSH operation
     * against one of the lists.
     * <p>
     * Once new data is present on one of the lists, the client finally returns
     * with the name of the key unblocking it and the popped value.
     * <p>
     * When blocking, if a non-zero timeout is specified, the client will
     * unblock returning a nil special value if the specified amount of seconds
     * passed without a push operation against at least one of the specified
     * keys.
     * <p>
     * The timeout argument is interpreted as an integer value. A timeout of
     * zero means instead to block forever.
     * <p>
     * <b>Multiple clients blocking for the same keys</b>
     * <p>
     * Multiple clients can block for the same key. They are put into a queue,
     * so the first to be served will be the one that started to wait earlier,
     * in a first-blpopping first-served fashion.
     * <p>
     * <b>blocking POP inside a MULTI/EXEC transaction</b>
     * <p>
     * BLPOP and BRPOP can be used with pipelining (sending multiple commands
     * and reading the replies in batch), but it does not make sense to use
     * BLPOP or BRPOP inside a MULTI/EXEC block (a Redis transaction).
     * <p>
     * The behavior of BLPOP inside MULTI/EXEC when the list is empty is to
     * return a multi-bulk nil reply, exactly what happens when the timeout is
     * reached. If you like science fiction, think at it like if inside
     * MULTI/EXEC the time will flow at infinite speed :)
     * <p>
     * Time complexity: O(1)
     * 
     * @see #brpop(int, String...)
     * 
     * @param timeout
     * @param keys
     * @return BLPOP returns a two-elements array via a multi bulk reply in
     *         order to return both the unblocking key and the popped value.
     *         <p>
     *         When a non-zero timeout is specified, and the BLPOP operation
     *         timed out, the return value is a nil multi bulk reply. Most
     *         client values will return false or nil accordingly to the
     *         programming language used.
     */
    public List<byte[]> blpop(final int timeout, final byte[]... keys) {
	checkIsInMulti();
	final List<byte[]> args = new ArrayList<byte[]>();
	for (final byte[] arg : keys) {
	    args.add(arg);
	}
	args.add(Protocol.toByteArray(timeout));

	client.blpop(args.toArray(new byte[args.size()][]));
	client.setTimeoutInfinite();
	final List<byte[]> multiBulkReply = client.getBinaryMultiBulkReply();
	client.rollbackTimeout();
	return multiBulkReply;
    }

    /**
     * Sort a Set or a List accordingly to the specified parameters and store
     * the result at dstkey.
     * 
     * @see #sort(byte[], SortingParams)
     * @see #sort(byte[])
     * @see #sort(byte[], byte[])
     * 
     * @param key
     * @param sortingParameters
     * @param dstkey
     * @return The number of elements of the list at dstkey.
     */
    public Long sort(final byte[] key, final SortingParams sortingParameters,
	    final byte[] dstkey) {
	checkIsInMulti();
	client.sort(key, sortingParameters, dstkey);
	return client.getIntegerReply();
    }

    /**
     * Sort a Set or a List and Store the Result at dstkey.
     * <p>
     * Sort the elements contained in the List, Set, or Sorted Set value at key
     * and store the result at dstkey. By default sorting is numeric with
     * elements being compared as double precision floating point numbers. This
     * is the simplest form of SORT.
     * 
     * @see #sort(byte[])
     * @see #sort(byte[], SortingParams)
     * @see #sort(byte[], SortingParams, byte[])
     * 
     * @param key
     * @param dstkey
     * @return The number of elements of the list at dstkey.
     */
    public Long sort(final byte[] key, final byte[] dstkey) {
	checkIsInMulti();
	client.sort(key, dstkey);
	return client.getIntegerReply();
    }

    /**
     * BLPOP (and BRPOP) is a blocking list pop primitive. You can see this
     * commands as blocking versions of LPOP and RPOP able to block if the
     * specified keys don't exist or contain empty lists.
     * <p>
     * The following is a description of the exact semantic. We describe BLPOP
     * but the two commands are identical, the only difference is that BLPOP
     * pops the element from the left (head) of the list, and BRPOP pops from
     * the right (tail).
     * <p>
     * <b>Non blocking behavior</b>
     * <p>
     * When BLPOP is called, if at least one of the specified keys contain a non
     * empty list, an element is popped from the head of the list and returned
     * to the caller together with the name of the key (BLPOP returns a two
     * elements array, the first element is the key, the second the popped
     * value).
     * <p>
     * Keys are scanned from left to right, so for instance if you issue BLPOP
     * list1 list2 list3 0 against a dataset where list1 does not exist but
     * list2 and list3 contain non empty lists, BLPOP guarantees to return an
     * element from the list stored at list2 (since it is the first non empty
     * list starting from the left).
     * <p>
     * <b>Blocking behavior</b>
     * <p>
     * If none of the specified keys exist or contain non empty lists, BLPOP
     * blocks until some other client performs a LPUSH or an RPUSH operation
     * against one of the lists.
     * <p>
     * Once new data is present on one of the lists, the client finally returns
     * with the name of the key unblocking it and the popped value.
     * <p>
     * When blocking, if a non-zero timeout is specified, the client will
     * unblock returning a nil special value if the specified amount of seconds
     * passed without a push operation against at least one of the specified
     * keys.
     * <p>
     * The timeout argument is interpreted as an integer value. A timeout of
     * zero means instead to block forever.
     * <p>
     * <b>Multiple clients blocking for the same keys</b>
     * <p>
     * Multiple clients can block for the same key. They are put into a queue,
     * so the first to be served will be the one that started to wait earlier,
     * in a first-blpopping first-served fashion.
     * <p>
     * <b>blocking POP inside a MULTI/EXEC transaction</b>
     * <p>
     * BLPOP and BRPOP can be used with pipelining (sending multiple commands
     * and reading the replies in batch), but it does not make sense to use
     * BLPOP or BRPOP inside a MULTI/EXEC block (a Redis transaction).
     * <p>
     * The behavior of BLPOP inside MULTI/EXEC when the list is empty is to
     * return a multi-bulk nil reply, exactly what happens when the timeout is
     * reached. If you like science fiction, think at it like if inside
     * MULTI/EXEC the time will flow at infinite speed :)
     * <p>
     * Time complexity: O(1)
     * 
     * @see #blpop(int, String...)
     * 
     * @param timeout
     * @param keys
     * @return BLPOP returns a two-elements array via a multi bulk reply in
     *         order to return both the unblocking key and the popped value.
     *         <p>
     *         When a non-zero timeout is specified, and the BLPOP operation
     *         timed out, the return value is a nil multi bulk reply. Most
     *         client values will return false or nil accordingly to the
     *         programming language used.
     */
    public List<byte[]> brpop(final int timeout, final byte[]... keys) {
	checkIsInMulti();
	final List<byte[]> args = new ArrayList<byte[]>();
	for (final byte[] arg : keys) {
	    args.add(arg);
	}
	args.add(Protocol.toByteArray(timeout));

	client.brpop(args.toArray(new byte[args.size()][]));
	client.setTimeoutInfinite();
	final List<byte[]> multiBulkReply = client.getBinaryMultiBulkReply();
	client.rollbackTimeout();

	return multiBulkReply;
    }

    /**
     * Request for authentication in a password protected Redis server. A Redis
     * server can be instructed to require a password before to allow clients to
     * issue commands. This is done using the requirepass directive in the Redis
     * configuration file. If the password given by the client is correct the
     * server replies with an OK status code reply and starts accepting commands
     * from the client. Otherwise an error is returned and the clients needs to
     * try a new password. Note that for the high performance nature of Redis it
     * is possible to try a lot of passwords in parallel in very short time, so
     * make sure to generate a strong and very long password so that this attack
     * is infeasible.
     * 
     * @param password
     * @return Status code reply
     */
    public String auth(final String password) {
	checkIsInMulti();
	client.auth(password);
	return client.getStatusCodeReply();
    }

    /**
     * Starts a pipeline, which is a very efficient way to send lots of command
     * and read all the responses when you finish sending them. Try to avoid
     * this version and use pipelined() when possible as it will give better
     * performance.
     * 
     * @param jedisPipeline
     * @return The results of the command in the same order you've run them.
     */
    public List<Object> pipelined(final PipelineBlock jedisPipeline) {
	jedisPipeline.setClient(client);
	jedisPipeline.execute();
	return jedisPipeline.syncAndReturnAll();
    }

    public Pipeline pipelined() {
	Pipeline pipeline = new Pipeline();
	pipeline.setClient(client);
	return pipeline;
    }

    public void subscribe(final JedisPubSub jedisPubSub,
	    final String... channels) {
	client.setTimeoutInfinite();
	jedisPubSub.proceed(client, channels);
	client.rollbackTimeout();
    }

    public Long publish(final String channel, final String message) {
	client.publish(channel, message);
	return client.getIntegerReply();
    }

    public void psubscribe(final JedisPubSub jedisPubSub,
	    final String... patterns) {
	client.setTimeoutInfinite();
	jedisPubSub.proceedWithPatterns(client, patterns);
	client.rollbackTimeout();
    }

    public Long zcount(final byte[] key, final double min, final double max) {
    	return zcount(key, toByteArray(min), toByteArray(max));
    }
    
    public Long zcount(final byte[] key, final byte[] min, final byte[] max) {
    	checkIsInMulti();
    	client.zcount(key, min, max);
    	return client.getIntegerReply();
    }
    
    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     * <p>
     * The elements having the same score are returned sorted lexicographically
     * as ASCII strings (this follows from a property of Redis sorted sets and
     * does not involve further computation).
     * <p>
     * Using the optional
     * {@link #zrangeByScore(byte[], double, double, int, int) LIMIT} it's
     * possible to get only a range of the matching elements in an SQL-alike
     * way. Note that if offset is large the commands needs to traverse the list
     * for offset elements and this adds up to the O(M) figure.
     * <p>
     * The {@link #zcount(byte[], double, double) ZCOUNT} command is similar to
     * {@link #zrangeByScore(byte[], double, double) ZRANGEBYSCORE} but instead
     * of returning the actual elements in the specified interval, it just
     * returns the number of matching elements.
     * <p>
     * <b>Exclusive intervals and infinity</b>
     * <p>
     * min and max can be -inf and +inf, so that you are not required to know
     * what's the greatest or smallest element in order to take, for instance,
     * elements "up to a given value".
     * <p>
     * Also while the interval is for default closed (inclusive) it's possible
     * to specify open intervals prefixing the score with a "(" character, so
     * for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (1.3 5}
     * <p>
     * Will return all the values with score > 1.3 and <= 5, while for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (5 (10}
     * <p>
     * Will return all the values with score > 5 and < 10 (5 and 10 excluded).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements returned by the command, so if M is constant
     * (for instance you always ask for the first ten elements with LIMIT) you
     * can consider it O(log(N))
     * 
     * @see #zrangeByScore(byte[], double, double)
     * @see #zrangeByScore(byte[], double, double, int, int)
     * @see #zrangeByScoreWithScores(byte[], double, double)
     * @see #zrangeByScoreWithScores(byte[], double, double, int, int)
     * @see #zcount(byte[], double, double)
     * 
     * @param key
     * @param min
     * @param max
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    public Set<byte[]> zrangeByScore(final byte[] key, final double min,
	    final double max) {
	return zrangeByScore(key, toByteArray(min), toByteArray(max));
	}

    public Set<byte[]> zrangeByScore(final byte[] key, final byte[] min,
	    final byte[] max) {
	checkIsInMulti();
	client.zrangeByScore(key, min, max);
	return new LinkedHashSet<byte[]>(client.getBinaryMultiBulkReply());
    }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     * <p>
     * The elements having the same score are returned sorted lexicographically
     * as ASCII strings (this follows from a property of Redis sorted sets and
     * does not involve further computation).
     * <p>
     * Using the optional
     * {@link #zrangeByScore(byte[], double, double, int, int) LIMIT} it's
     * possible to get only a range of the matching elements in an SQL-alike
     * way. Note that if offset is large the commands needs to traverse the list
     * for offset elements and this adds up to the O(M) figure.
     * <p>
     * The {@link #zcount(byte[], double, double) ZCOUNT} command is similar to
     * {@link #zrangeByScore(byte[], double, double) ZRANGEBYSCORE} but instead
     * of returning the actual elements in the specified interval, it just
     * returns the number of matching elements.
     * <p>
     * <b>Exclusive intervals and infinity</b>
     * <p>
     * min and max can be -inf and +inf, so that you are not required to know
     * what's the greatest or smallest element in order to take, for instance,
     * elements "up to a given value".
     * <p>
     * Also while the interval is for default closed (inclusive) it's possible
     * to specify open intervals prefixing the score with a "(" character, so
     * for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (1.3 5}
     * <p>
     * Will return all the values with score > 1.3 and <= 5, while for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (5 (10}
     * <p>
     * Will return all the values with score > 5 and < 10 (5 and 10 excluded).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements returned by the command, so if M is constant
     * (for instance you always ask for the first ten elements with LIMIT) you
     * can consider it O(log(N))
     * 
     * @see #zrangeByScore(byte[], double, double)
     * @see #zrangeByScore(byte[], double, double, int, int)
     * @see #zrangeByScoreWithScores(byte[], double, double)
     * @see #zrangeByScoreWithScores(byte[], double, double, int, int)
     * @see #zcount(byte[], double, double)
     * 
     * @param key
     * @param min
     * @param max
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    public Set<byte[]> zrangeByScore(final byte[] key, final double min,
	    final double max, final int offset, final int count) {
	return zrangeByScore(key, toByteArray(min),toByteArray(max),offset, count);
    }
    
    public Set<byte[]> zrangeByScore(final byte[] key, final byte[] min,
    	    final byte[] max, final int offset, final int count) {
    	checkIsInMulti();
    	client.zrangeByScore(key, min, max, offset, count);
    	return new LinkedHashSet<byte[]>(client.getBinaryMultiBulkReply());
        }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     * <p>
     * The elements having the same score are returned sorted lexicographically
     * as ASCII strings (this follows from a property of Redis sorted sets and
     * does not involve further computation).
     * <p>
     * Using the optional
     * {@link #zrangeByScore(byte[], double, double, int, int) LIMIT} it's
     * possible to get only a range of the matching elements in an SQL-alike
     * way. Note that if offset is large the commands needs to traverse the list
     * for offset elements and this adds up to the O(M) figure.
     * <p>
     * The {@link #zcount(byte[], double, double) ZCOUNT} command is similar to
     * {@link #zrangeByScore(byte[], double, double) ZRANGEBYSCORE} but instead
     * of returning the actual elements in the specified interval, it just
     * returns the number of matching elements.
     * <p>
     * <b>Exclusive intervals and infinity</b>
     * <p>
     * min and max can be -inf and +inf, so that you are not required to know
     * what's the greatest or smallest element in order to take, for instance,
     * elements "up to a given value".
     * <p>
     * Also while the interval is for default closed (inclusive) it's possible
     * to specify open intervals prefixing the score with a "(" character, so
     * for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (1.3 5}
     * <p>
     * Will return all the values with score > 1.3 and <= 5, while for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (5 (10}
     * <p>
     * Will return all the values with score > 5 and < 10 (5 and 10 excluded).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements returned by the command, so if M is constant
     * (for instance you always ask for the first ten elements with LIMIT) you
     * can consider it O(log(N))
     * 
     * @see #zrangeByScore(byte[], double, double)
     * @see #zrangeByScore(byte[], double, double, int, int)
     * @see #zrangeByScoreWithScores(byte[], double, double)
     * @see #zrangeByScoreWithScores(byte[], double, double, int, int)
     * @see #zcount(byte[], double, double)
     * 
     * @param key
     * @param min
     * @param max
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key,
	    final double min, final double max) {
	return zrangeByScoreWithScores(key, toByteArray(min), toByteArray(max));
    }
    
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key,
    	    final byte[] min, final byte[] max) {
    	checkIsInMulti();
    	client.zrangeByScoreWithScores(key, min, max);
    	Set<Tuple> set = getBinaryTupledSet();
    	return set;
        }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     * <p>
     * The elements having the same score are returned sorted lexicographically
     * as ASCII strings (this follows from a property of Redis sorted sets and
     * does not involve further computation).
     * <p>
     * Using the optional
     * {@link #zrangeByScore(byte[], double, double, int, int) LIMIT} it's
     * possible to get only a range of the matching elements in an SQL-alike
     * way. Note that if offset is large the commands needs to traverse the list
     * for offset elements and this adds up to the O(M) figure.
     * <p>
     * The {@link #zcount(byte[], double, double) ZCOUNT} command is similar to
     * {@link #zrangeByScore(byte[], double, double) ZRANGEBYSCORE} but instead
     * of returning the actual elements in the specified interval, it just
     * returns the number of matching elements.
     * <p>
     * <b>Exclusive intervals and infinity</b>
     * <p>
     * min and max can be -inf and +inf, so that you are not required to know
     * what's the greatest or smallest element in order to take, for instance,
     * elements "up to a given value".
     * <p>
     * Also while the interval is for default closed (inclusive) it's possible
     * to specify open intervals prefixing the score with a "(" character, so
     * for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (1.3 5}
     * <p>
     * Will return all the values with score > 1.3 and <= 5, while for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (5 (10}
     * <p>
     * Will return all the values with score > 5 and < 10 (5 and 10 excluded).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements returned by the command, so if M is constant
     * (for instance you always ask for the first ten elements with LIMIT) you
     * can consider it O(log(N))
     * 
     * @see #zrangeByScore(byte[], double, double)
     * @see #zrangeByScore(byte[], double, double, int, int)
     * @see #zrangeByScoreWithScores(byte[], double, double)
     * @see #zrangeByScoreWithScores(byte[], double, double, int, int)
     * @see #zcount(byte[], double, double)
     * 
     * @param key
     * @param min
     * @param max
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key,
	    final double min, final double max, final int offset,
	    final int count) {
	return zrangeByScoreWithScores(key, toByteArray(min), toByteArray(max), offset, count);
    }
    
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key,
    	    final byte[] min, final byte[] max, final int offset,
    	    final int count) {
    	checkIsInMulti();
    	client.zrangeByScoreWithScores(key, min, max, offset, count);
    	Set<Tuple> set = getBinaryTupledSet();
    	return set;
        }

    private Set<Tuple> getBinaryTupledSet() {
	checkIsInMulti();
	List<byte[]> membersWithScores = client.getBinaryMultiBulkReply();
	Set<Tuple> set = new LinkedHashSet<Tuple>();
	Iterator<byte[]> iterator = membersWithScores.iterator();
	while (iterator.hasNext()) {
	    set.add(new Tuple(iterator.next(), Double.valueOf(SafeEncoder
		    .encode(iterator.next()))));
	}
	return set;
    }

    public Set<byte[]> zrevrangeByScore(final byte[] key, final double max,
	    final double min) {
	return zrevrangeByScore(key, toByteArray(max), toByteArray(min));
    }

    public Set<byte[]> zrevrangeByScore(final byte[] key, final byte[] max,
	    final byte[] min) {
	checkIsInMulti();
	client.zrevrangeByScore(key, max, min);
	return new LinkedHashSet<byte[]>(client.getBinaryMultiBulkReply());
    }

    public Set<byte[]> zrevrangeByScore(final byte[] key, final double max,
	    final double min, final int offset, final int count) {
	return zrevrangeByScore(key, toByteArray(max), toByteArray(min), offset, count);
    }
    
    public Set<byte[]> zrevrangeByScore(final byte[] key, final byte[] max,
    	    final byte[] min, final int offset, final int count) {
    	checkIsInMulti();
    	client.zrevrangeByScore(key, max, min, offset, count);
    	return new LinkedHashSet<byte[]>(client.getBinaryMultiBulkReply());
        }

    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key,
	    final double max, final double min) {
	return zrevrangeByScoreWithScores(key, toByteArray(max), toByteArray(min));
    }

    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key,
	    final double max, final double min, final int offset,
	    final int count) {
    	return zrevrangeByScoreWithScores(key, toByteArray(max), toByteArray(min), offset, count);
    }
    
    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key,
    	    final byte[] max, final byte[] min) {
	checkIsInMulti();
	client.zrevrangeByScoreWithScores(key, max, min);
	Set<Tuple> set = getBinaryTupledSet();
	return set;
    }

    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key,
	    final byte[] max, final byte[] min, final int offset,
	    final int count) {
	checkIsInMulti();
	client.zrevrangeByScoreWithScores(key, max, min, offset, count);
	Set<Tuple> set = getBinaryTupledSet();
	return set;
    }    

    /**
     * Remove all elements in the sorted set at key with rank between start and
     * end. Start and end are 0-based with rank 0 being the element with the
     * lowest score. Both start and end can be negative numbers, where they
     * indicate offsets starting at the element with the highest rank. For
     * example: -1 is the element with the highest score, -2 the element with
     * the second highest score and so forth.
     * <p>
     * <b>Time complexity:</b> O(log(N))+O(M) with N being the number of
     * elements in the sorted set and M the number of elements removed by the
     * operation
     * 
     */
    public Long zremrangeByRank(final byte[] key, final int start, final int end) {
	checkIsInMulti();
	client.zremrangeByRank(key, start, end);
	return client.getIntegerReply();
    }

    /**
     * Remove all the elements in the sorted set at key with a score between min
     * and max (including elements with score equal to min or max).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements removed by the operation
     * 
     * @param key
     * @param start
     * @param end
     * @return Integer reply, specifically the number of elements removed.
     */
    public Long zremrangeByScore(final byte[] key, final double start,
	    final double end) {
	return zremrangeByScore(key, toByteArray(start), toByteArray(end));
    }
    
    public Long zremrangeByScore(final byte[] key, final byte[] start,
    	    final byte[] end) {
    	checkIsInMulti();
    	client.zremrangeByScore(key, start, end);
    	return client.getIntegerReply();
        }

    /**
     * Creates a union or intersection of N sorted sets given by keys k1 through
     * kN, and stores it at dstkey. It is mandatory to provide the number of
     * input keys N, before passing the input keys and the other (optional)
     * arguments.
     * <p>
     * As the terms imply, the {@link #zinterstore(String, String...)
     * ZINTERSTORE} command requires an element to be present in each of the
     * given inputs to be inserted in the result. The
     * {@link #zunionstore(String, String...) ZUNIONSTORE} command inserts all
     * elements across all inputs.
     * <p>
     * Using the WEIGHTS option, it is possible to add weight to each input
     * sorted set. This means that the score of each element in the sorted set
     * is first multiplied by this weight before being passed to the
     * aggregation. When this option is not given, all weights default to 1.
     * <p>
     * With the AGGREGATE option, it's possible to specify how the results of
     * the union or intersection are aggregated. This option defaults to SUM,
     * where the score of an element is summed across the inputs where it
     * exists. When this option is set to be either MIN or MAX, the resulting
     * set will contain the minimum or maximum score of an element across the
     * inputs where it exists.
     * <p>
     * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the
     * sizes of the input sorted sets, and M being the number of elements in the
     * resulting sorted set
     * 
     * @see #zunionstore(String, String...)
     * @see #zunionstore(String, ZParams, String...)
     * @see #zinterstore(String, String...)
     * @see #zinterstore(String, ZParams, String...)
     * 
     * @param dstkey
     * @param sets
     * @return Integer reply, specifically the number of elements in the sorted
     *         set at dstkey
     */
    public Long zunionstore(final byte[] dstkey, final byte[]... sets) {
	checkIsInMulti();
	client.zunionstore(dstkey, sets);
	return client.getIntegerReply();
    }

    /**
     * Creates a union or intersection of N sorted sets given by keys k1 through
     * kN, and stores it at dstkey. It is mandatory to provide the number of
     * input keys N, before passing the input keys and the other (optional)
     * arguments.
     * <p>
     * As the terms imply, the {@link #zinterstore(String, String...)
     * ZINTERSTORE} command requires an element to be present in each of the
     * given inputs to be inserted in the result. The
     * {@link #zunionstore(String, String...) ZUNIONSTORE} command inserts all
     * elements across all inputs.
     * <p>
     * Using the WEIGHTS option, it is possible to add weight to each input
     * sorted set. This means that the score of each element in the sorted set
     * is first multiplied by this weight before being passed to the
     * aggregation. When this option is not given, all weights default to 1.
     * <p>
     * With the AGGREGATE option, it's possible to specify how the results of
     * the union or intersection are aggregated. This option defaults to SUM,
     * where the score of an element is summed across the inputs where it
     * exists. When this option is set to be either MIN or MAX, the resulting
     * set will contain the minimum or maximum score of an element across the
     * inputs where it exists.
     * <p>
     * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the
     * sizes of the input sorted sets, and M being the number of elements in the
     * resulting sorted set
     * 
     * @see #zunionstore(String, String...)
     * @see #zunionstore(String, ZParams, String...)
     * @see #zinterstore(String, String...)
     * @see #zinterstore(String, ZParams, String...)
     * 
     * @param dstkey
     * @param sets
     * @param params
     * @return Integer reply, specifically the number of elements in the sorted
     *         set at dstkey
     */
    public Long zunionstore(final byte[] dstkey, final ZParams params,
	    final byte[]... sets) {
	checkIsInMulti();
	client.zunionstore(dstkey, params, sets);
	return client.getIntegerReply();
    }

    /**
     * Creates a union or intersection of N sorted sets given by keys k1 through
     * kN, and stores it at dstkey. It is mandatory to provide the number of
     * input keys N, before passing the input keys and the other (optional)
     * arguments.
     * <p>
     * As the terms imply, the {@link #zinterstore(String, String...)
     * ZINTERSTORE} command requires an element to be present in each of the
     * given inputs to be inserted in the result. The
     * {@link #zunionstore(String, String...) ZUNIONSTORE} command inserts all
     * elements across all inputs.
     * <p>
     * Using the WEIGHTS option, it is possible to add weight to each input
     * sorted set. This means that the score of each element in the sorted set
     * is first multiplied by this weight before being passed to the
     * aggregation. When this option is not given, all weights default to 1.
     * <p>
     * With the AGGREGATE option, it's possible to specify how the results of
     * the union or intersection are aggregated. This option defaults to SUM,
     * where the score of an element is summed across the inputs where it
     * exists. When this option is set to be either MIN or MAX, the resulting
     * set will contain the minimum or maximum score of an element across the
     * inputs where it exists.
     * <p>
     * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the
     * sizes of the input sorted sets, and M being the number of elements in the
     * resulting sorted set
     * 
     * @see #zunionstore(String, String...)
     * @see #zunionstore(String, ZParams, String...)
     * @see #zinterstore(String, String...)
     * @see #zinterstore(String, ZParams, String...)
     * 
     * @param dstkey
     * @param sets
     * @return Integer reply, specifically the number of elements in the sorted
     *         set at dstkey
     */
    public Long zinterstore(final byte[] dstkey, final byte[]... sets) {
	checkIsInMulti();
	client.zinterstore(dstkey, sets);
	return client.getIntegerReply();
    }

    /**
     * Creates a union or intersection of N sorted sets given by keys k1 through
     * kN, and stores it at dstkey. It is mandatory to provide the number of
     * input keys N, before passing the input keys and the other (optional)
     * arguments.
     * <p>
     * As the terms imply, the {@link #zinterstore(String, String...)
     * ZINTERSTORE} command requires an element to be present in each of the
     * given inputs to be inserted in the result. The
     * {@link #zunionstore(String, String...) ZUNIONSTORE} command inserts all
     * elements across all inputs.
     * <p>
     * Using the WEIGHTS option, it is possible to add weight to each input
     * sorted set. This means that the score of each element in the sorted set
     * is first multiplied by this weight before being passed to the
     * aggregation. When this option is not given, all weights default to 1.
     * <p>
     * With the AGGREGATE option, it's possible to specify how the results of
     * the union or intersection are aggregated. This option defaults to SUM,
     * where the score of an element is summed across the inputs where it
     * exists. When this option is set to be either MIN or MAX, the resulting
     * set will contain the minimum or maximum score of an element across the
     * inputs where it exists.
     * <p>
     * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the
     * sizes of the input sorted sets, and M being the number of elements in the
     * resulting sorted set
     * 
     * @see #zunionstore(String, String...)
     * @see #zunionstore(String, ZParams, String...)
     * @see #zinterstore(String, String...)
     * @see #zinterstore(String, ZParams, String...)
     * 
     * @param dstkey
     * @param sets
     * @param params
     * @return Integer reply, specifically the number of elements in the sorted
     *         set at dstkey
     */
    public Long zinterstore(final byte[] dstkey, final ZParams params,
	    final byte[]... sets) {
	checkIsInMulti();
	client.zinterstore(dstkey, params, sets);
	return client.getIntegerReply();
    }

    /**
     * Synchronously save the DB on disk.
     * <p>
     * Save the whole dataset on disk (this means that all the databases are
     * saved, as well as keys with an EXPIRE set (the expire is preserved). The
     * server hangs while the saving is not completed, no connection is served
     * in the meanwhile. An OK code is returned when the DB was fully stored in
     * disk.
     * <p>
     * The background variant of this command is {@link #bgsave() BGSAVE} that
     * is able to perform the saving in the background while the server
     * continues serving other clients.
     * <p>
     * 
     * @return Status code reply
     */
    public String save() {
	client.save();
	return client.getStatusCodeReply();
    }

    /**
     * Asynchronously save the DB on disk.
     * <p>
     * Save the DB in background. The OK code is immediately returned. Redis
     * forks, the parent continues to server the clients, the child saves the DB
     * on disk then exit. A client my be able to check if the operation
     * succeeded using the LASTSAVE command.
     * 
     * @return Status code reply
     */
    public String bgsave() {
	client.bgsave();
	return client.getStatusCodeReply();
    }

    /**
     * Rewrite the append only file in background when it gets too big. Please
     * for detailed information about the Redis Append Only File check the <a
     * href="http://code.google.com/p/redis/wiki/AppendOnlyFileHowto">Append
     * Only File Howto</a>.
     * <p>
     * BGREWRITEAOF rewrites the Append Only File in background when it gets too
     * big. The Redis Append Only File is a Journal, so every operation
     * modifying the dataset is logged in the Append Only File (and replayed at
     * startup). This means that the Append Only File always grows. In order to
     * rebuild its content the BGREWRITEAOF creates a new version of the append
     * only file starting directly form the dataset in memory in order to
     * guarantee the generation of the minimal number of commands needed to
     * rebuild the database.
     * <p>
     * 
     * @return Status code reply
     */
    public String bgrewriteaof() {
	client.bgrewriteaof();
	return client.getStatusCodeReply();
    }

    /**
     * Return the UNIX time stamp of the last successfully saving of the dataset
     * on disk.
     * <p>
     * Return the UNIX TIME of the last DB save executed with success. A client
     * may check if a {@link #bgsave() BGSAVE} command succeeded reading the
     * LASTSAVE value, then issuing a BGSAVE command and checking at regular
     * intervals every N seconds if LASTSAVE changed.
     * 
     * @return Integer reply, specifically an UNIX time stamp.
     */
    public Long lastsave() {
	client.lastsave();
	return client.getIntegerReply();
    }

    /**
     * Synchronously save the DB on disk, then shutdown the server.
     * <p>
     * Stop all the clients, save the DB, then quit the server. This commands
     * makes sure that the DB is switched off without the lost of any data. This
     * is not guaranteed if the client uses simply {@link #save() SAVE} and then
     * {@link #quit() QUIT} because other clients may alter the DB data between
     * the two commands.
     * 
     * @return Status code reply on error. On success nothing is returned since
     *         the server quits and the connection is closed.
     */
    public String shutdown() {
	client.shutdown();
	String status = null;
	try {
	    status = client.getStatusCodeReply();
	} catch (JedisException ex) {
	    status = null;
	}
	return status;
    }

    /**
     * Provide information and statistics about the server.
     * <p>
     * The info command returns different information and statistics about the
     * server in an format that's simple to parse by computers and easy to read
     * by humans.
     * <p>
     * <b>Format of the returned String:</b>
     * <p>
     * All the fields are in the form field:value
     * 
     * <pre>
     * edis_version:0.07
     * connected_clients:1
     * connected_slaves:0
     * used_memory:3187
     * changes_since_last_save:0
     * last_save_time:1237655729
     * total_connections_received:1
     * total_commands_processed:1
     * uptime_in_seconds:25
     * uptime_in_days:0
     * </pre>
     * 
     * <b>Notes</b>
     * <p>
     * used_memory is returned in bytes, and is the total number of bytes
     * allocated by the program using malloc.
     * <p>
     * uptime_in_days is redundant since the uptime in seconds contains already
     * the full uptime information, this field is only mainly present for
     * humans.
     * <p>
     * changes_since_last_save does not refer to the number of key changes, but
     * to the number of operations that produced some kind of change in the
     * dataset.
     * <p>
     * 
     * @return Bulk reply
     */
    public String info() {
	client.info();
	return client.getBulkReply();
    }

    /**
     * Dump all the received requests in real time.
     * <p>
     * MONITOR is a debugging command that outputs the whole sequence of
     * commands received by the Redis server. is very handy in order to
     * understand what is happening into the database. This command is used
     * directly via telnet.
     * 
     * @param jedisMonitor
     */
    public void monitor(final JedisMonitor jedisMonitor) {
	client.monitor();
	jedisMonitor.proceed(client);
    }

    /**
     * Change the replication settings.
     * <p>
     * The SLAVEOF command can change the replication settings of a slave on the
     * fly. If a Redis server is arleady acting as slave, the command SLAVEOF NO
     * ONE will turn off the replicaiton turning the Redis server into a MASTER.
     * In the proper form SLAVEOF hostname port will make the server a slave of
     * the specific server listening at the specified hostname and port.
     * <p>
     * If a server is already a slave of some master, SLAVEOF hostname port will
     * stop the replication against the old server and start the
     * synchrnonization against the new one discarding the old dataset.
     * <p>
     * The form SLAVEOF no one will stop replication turning the server into a
     * MASTER but will not discard the replication. So if the old master stop
     * working it is possible to turn the slave into a master and set the
     * application to use the new master in read/write. Later when the other
     * Redis server will be fixed it can be configured in order to work as
     * slave.
     * <p>
     * 
     * @param host
     * @param port
     * @return Status code reply
     */
    public String slaveof(final String host, final int port) {
	client.slaveof(host, port);
	return client.getStatusCodeReply();
    }

    public String slaveofNoOne() {
	client.slaveofNoOne();
	return client.getStatusCodeReply();
    }

    /**
     * Retrieve the configuration of a running Redis server. Not all the
     * configuration parameters are supported.
     * <p>
     * CONFIG GET returns the current configuration parameters. This sub command
     * only accepts a single argument, that is glob style pattern. All the
     * configuration parameters matching this parameter are reported as a list
     * of key-value pairs.
     * <p>
     * <b>Example:</b>
     * 
     * <pre>
     * $ redis-cli config get '*'
     * 1. "dbfilename"
     * 2. "dump.rdb"
     * 3. "requirepass"
     * 4. (nil)
     * 5. "masterauth"
     * 6. (nil)
     * 7. "maxmemory"
     * 8. "0\n"
     * 9. "appendfsync"
     * 10. "everysec"
     * 11. "save"
     * 12. "3600 1 300 100 60 10000"
     * 
     * $ redis-cli config get 'm*'
     * 1. "masterauth"
     * 2. (nil)
     * 3. "maxmemory"
     * 4. "0\n"
     * </pre>
     * 
     * @param pattern
     * @return Bulk reply.
     */
    public List<byte[]> configGet(final byte[] pattern) {
	client.configGet(pattern);
	return client.getBinaryMultiBulkReply();
    }

    /**
     * Reset the stats returned by INFO
     * 
     * @return
     */
    public String configResetStat() {
	client.configResetStat();
	return client.getStatusCodeReply();
    }

    /**
     * Alter the configuration of a running Redis server. Not all the
     * configuration parameters are supported.
     * <p>
     * The list of configuration parameters supported by CONFIG SET can be
     * obtained issuing a {@link #configGet(String) CONFIG GET *} command.
     * <p>
     * The configuration set using CONFIG SET is immediately loaded by the Redis
     * server that will start acting as specified starting from the next
     * command.
     * <p>
     * 
     * <b>Parameters value format</b>
     * <p>
     * The value of the configuration parameter is the same as the one of the
     * same parameter in the Redis configuration file, with the following
     * exceptions:
     * <p>
     * <ul>
     * <li>The save paramter is a list of space-separated integers. Every pair
     * of integers specify the time and number of changes limit to trigger a
     * save. For instance the command CONFIG SET save "3600 10 60 10000" will
     * configure the server to issue a background saving of the RDB file every
     * 3600 seconds if there are at least 10 changes in the dataset, and every
     * 60 seconds if there are at least 10000 changes. To completely disable
     * automatic snapshots just set the parameter as an empty string.
     * <li>All the integer parameters representing memory are returned and
     * accepted only using bytes as unit.
     * </ul>
     * 
     * @param parameter
     * @param value
     * @return Status code reply
     */
    public byte[] configSet(final byte[] parameter, final byte[] value) {
	client.configSet(parameter, value);
	return client.getBinaryBulkReply();
    }

    public boolean isConnected() {
	return client.isConnected();
    }

    public Long strlen(final byte[] key) {
	client.strlen(key);
	return client.getIntegerReply();
    }

    public void sync() {
	client.sync();
    }

    public Long lpushx(final byte[] key, final byte[] string) {
	client.lpushx(key, string);
	return client.getIntegerReply();
    }

    /**
     * Undo a {@link #expire(byte[], int) expire} at turning the expire key into
     * a normal key.
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @return Integer reply, specifically: 1: the key is now persist. 0: the
     *         key is not persist (only happens when key not set).
     */
    public Long persist(final byte[] key) {
	client.persist(key);
	return client.getIntegerReply();
    }

    public Long rpushx(final byte[] key, final byte[] string) {
	client.rpushx(key, string);
	return client.getIntegerReply();
    }

    public byte[] echo(final byte[] string) {
	client.echo(string);
	return client.getBinaryBulkReply();
    }

    public Long linsert(final byte[] key, final LIST_POSITION where,
	    final byte[] pivot, final byte[] value) {
	client.linsert(key, where, pivot, value);
	return client.getIntegerReply();
    }

    public String debug(final DebugParams params) {
	client.debug(params);
	return client.getStatusCodeReply();
    }

    public Client getClient() {
	return client;
    }

    /**
     * Pop a value from a list, push it to another list and return it; or block
     * until one is available
     * 
     * @param source
     * @param destination
     * @param timeout
     * @return the element
     */
    public byte[] brpoplpush(byte[] source, byte[] destination, int timeout) {
	client.brpoplpush(source, destination, timeout);
	client.setTimeoutInfinite();
	byte[] reply = client.getBinaryBulkReply();
	client.rollbackTimeout();
	return reply;
    }

    /**
     * Sets or clears the bit at offset in the string value stored at key
     * 
     * @param key
     * @param offset
     * @param value
     * @return
     */
    public Boolean setbit(byte[] key, long offset, byte[] value) {
	client.setbit(key, offset, value);
	return client.getIntegerReply() == 1;
    }

    /**
     * Returns the bit value at offset in the string value stored at key
     * 
     * @param key
     * @param offset
     * @return
     */
    public Boolean getbit(byte[] key, long offset) {
	client.getbit(key, offset);
	return client.getIntegerReply() == 1;
    }

    public Long setrange(byte[] key, long offset, byte[] value) {
	client.setrange(key, offset, value);
	return client.getIntegerReply();
    }

    public String getrange(byte[] key, long startOffset, long endOffset) {
	client.getrange(key, startOffset, endOffset);
	return client.getBulkReply();
    }

    public Long publish(byte[] channel, byte[] message) {
	client.publish(channel, message);
	return client.getIntegerReply();
    }

    public void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels) {
	client.setTimeoutInfinite();
	jedisPubSub.proceed(client, channels);
	client.rollbackTimeout();
    }

    public void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns) {
	client.setTimeoutInfinite();
	jedisPubSub.proceedWithPatterns(client, patterns);
	client.rollbackTimeout();
    }

    public Long getDB() {
	return client.getDB();
    }

    /**
     * Evaluates scripts using the Lua interpreter built into Redis starting
     * from version 2.6.0.
     * <p>
     * 
     * @return Script result
     */
    public Object eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
	client.setTimeoutInfinite();
	client.eval(script, toByteArray(keys.size()), getParams(keys, args));
	return client.getOne();
    }

    private byte[][] getParams(List<byte[]> keys, List<byte[]> args) {
	int keyCount = keys.size();
	byte[][] params = new byte[keyCount + args.size()][];

	for (int i = 0; i < keyCount; i++)
	    params[i] = keys.get(i);

	for (int i = 0; i < keys.size(); i++)
	    params[keyCount + i] = args.get(i);

	return params;
    }

    public Object eval(byte[] script, byte[] keyCount, byte[][] params) {
	client.setTimeoutInfinite();
	client.eval(script, keyCount, params);
	return client.getOne();
    }

    public byte[] scriptFlush() {
	client.scriptFlush();
	return client.getBinaryBulkReply();
    }

    public List<Long> scriptExists(byte[]... sha1) {
	client.scriptExists(sha1);
	return client.getIntegerMultiBulkReply();
    }

    public byte[] scriptLoad(byte[] script) {
	client.scriptLoad(script);
	return client.getBinaryBulkReply();
    }

    public byte[] scriptKill() {
	client.scriptKill();
	return client.getBinaryBulkReply();
    }

    public byte[] slowlogReset() {
	client.slowlogReset();
	return client.getBinaryBulkReply();
    }

    public long slowlogLen() {
	client.slowlogLen();
	return client.getIntegerReply();
    }

    public List<byte[]> slowlogGetBinary() {
	client.slowlogGet();
	return client.getBinaryMultiBulkReply();
    }

    public List<byte[]> slowlogGetBinary(long entries) {
	client.slowlogGet(entries);
	return client.getBinaryMultiBulkReply();
    }
    
    public Long objectRefcount(byte[] key) {
		client.objectRefcount(key);
		return client.getIntegerReply();
	}
	
	public byte[] objectEncoding(byte[] key) {
		client.objectEncoding(key);
		return client.getBinaryBulkReply();
	}

	public Long objectIdletime(byte[] key) {
		client.objectIdletime(key);
		return client.getIntegerReply();
	}
}

>>>>>>> 76aa07461566a5976980e6696204781271955163
