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
package rebuild.graphics.tiff;

import java.io.IOException;
import java.io.OutputStream;

import rebuild.BBXResource;
import rebuild.Resources;

/**
 * A data type writer for TIFF.
 * @since BBX 1.0.1
 */
public final class Writer
{
	/**
	 * 8-bit unsigned integer. In Java the only value that can be an unsigned 8-bit integer is an int.
	 */
	public static final short BYTE = 1;
	/**
	 * 8-bit byte that contains a 7-bit ASCII code; the last byte must be NUL (binary zero), the last byte is taken 
	 * care of by the {@link Writer}.
	 */
	public static final short ASCII = 2;
	/**
	 * 16-bit (2-byte) unsigned integer. In Java the only value that can be an unsigned 16-bit integer is an int.
	 */
	public static final short SHORT = 3;
	/**
	 * 32-bit (4-byte) unsigned integer. In Java the only value that can be an unsigned 32-bit integer is an int.
	 */
	public static final short LONG = 4;
	/**
	 * Two {@link LONG}s: the first represents the numerator of a fraction; the second, the denominator.
	 */
	public static final short RATIONAL = 5;
	
	/**
	 * An 8-bit signed (twos-complement) integer.
	 */
	public static final short SBYTE = 6;
	/**
	 * An 8-bit byte that may contain anything, depending on the definition of the field.
	 */
	public static final short UNDEFINED = 7;
	/**
	 * A 16-bit (2-byte) signed (twos-complement) integer.
	 */
	public static final short SSHORT = 8;
	/**
	 * A 32-bit (4-byte) signed (twos-complement) integer.
	 */
	public static final short SLONG = 9;
	/**
	 * Two {@link SLONG}&#x2019;s: the first represents the numerator of a fraction, the second the denominator.
	 */
	public static final short SRATIONAL = 10;
	/**
	 * Single precision (4-byte) IEEE format.
	 */
	public static final short FLOAT = 11;
	/**
	 * Double precision (8-byte) IEEE format.
	 */
	public static final short DOUBLE = 12;
	
	/**
	 * A 64-bit (8-byte) unsigned integer.
	 */
	public static final short LONG8 = 16;
	/**
	 * A 64-bit (8-byte) signed (twos-complement) integer.
	 */
	public static final short SLONG8 = 17;
	
	private static final char NUL = ('\0' & 0x7F);
	
	private boolean littleEndian;
	private OutputStream dat;
	
	/**
	 * Create a new Writer.
	 * @param littleEndian If the stream should be little-endian (true) or big-endian (false).
	 * @param out The stream to write data to.
	 */
	public Writer(boolean littleEndian, OutputStream out)
	{
		this.littleEndian = littleEndian;
		this.dat = out;
	}
	
	/**
	 * Write an array of bytes to the stream.
	 * @param data The array of bytes to write.
	 * @throws IOException If any IO exception occurs.
	 */
	public void write(byte[] data) throws IOException
	{
		this.dat.write(data);
	}
	
	/**
	 * Write an array of bytes to the stream.
	 * @param data The array of bytes to write.
	 * @param off The start offset in the data.
	 * @param len The number of bytes to write.
	 * @throws IOException If any IO exception occurs.
	 */
	public void write(byte[] data, int off, int len) throws IOException
	{
		this.dat.write(data, off, len);
	}
	
	/**
	 * Write a {@link BYTE} or {@link SBYTE}.
	 * @param value The {@link BYTE}/{@link SBYTE} to write.
	 * @throws IOException If any IO exception occurs.
	 */
	public void writeByte(int value) throws IOException
    {
    	dat.write(value);
    }
    
	/**
	 * Write a {@link SHORT} or {@link SSHORT}.
	 * @param value The {@link SHORT}/{@link SSHORT} to write.
	 * @throws IOException If any IO exception occurs.
	 */
    public void writeUShort(int value) throws IOException
    {
    	if(littleEndian)
    	{
    		dat.write(value & 0x00FF);
    		dat.write((value & 0xFF00) >> 8);
    	}
    	else
    	{
    		dat.write((value & 0xFF00) >> 8);
    		dat.write(value & 0x00FF);
    	}
    }
    
    /**
	 * Write a {@link LONG} or {@link SLONG}.
	 * @param value The {@link LONG}/{@link SLONG} to write.
	 * @throws IOException If any IO exception occurs.
	 */
    public void writeUInt(int value) throws IOException
    {
    	if(littleEndian)
    	{
    		dat.write(value & 0x000000FF);
    		dat.write((value & 0x0000FF00) >> 8);
    		dat.write((value & 0x00FF0000) >> 16);
    		dat.write((value & 0xFF000000) >> 24);
    	}
    	else
    	{
    		dat.write((value & 0xFF000000) >> 24);
    		dat.write((value & 0x00FF0000) >> 16);
    		dat.write((value & 0x0000FF00) >> 8);
    		dat.write(value & 0x000000FF);
    	}
    }
    
    /**
	 * Write a {@link FLOAT}.
	 * @param value The {@link FLOAT} to write.
	 * @throws IOException If any IO exception occurs.
	 */
    public void writeFloat(float value) throws IOException
    {
    	writeUInt(Float.floatToIntBits(value));
    }
    
    /**
	 * Write a 8-byte signed/unsigned integer.
	 * @param value The 8-byte signed/unsigned integer to write.
	 * @throws IOException If any IO exception occurs.
	 */
    public void writeULong(long value) throws IOException
    {
    	if(littleEndian)
    	{
    		dat.write((int)(value & 0x00000000000000FFL));
    		dat.write((int)((value & 0x000000000000FF00L) >> 8));
    		dat.write((int)((value & 0x0000000000FF0000L) >> 16));
    		dat.write((int)((value & 0x00000000FF000000L) >> 24));
    		dat.write((int)((value & 0x000000FF00000000L) >> 32));
    		dat.write((int)((value & 0x0000FF0000000000L) >> 40));
    		dat.write((int)((value & 0x00FF000000000000L) >> 48));
    		dat.write((int)((value & 0xFF00000000000000L) >> 56));
    	}
    	else
    	{
    		dat.write((int)((value & 0xFF00000000000000L) >> 56));
    		dat.write((int)((value & 0x00FF000000000000L) >> 48));
    		dat.write((int)((value & 0x0000FF0000000000L) >> 40));
    		dat.write((int)((value & 0x000000FF00000000L) >> 32));
    		dat.write((int)((value & 0x00000000FF000000L) >> 24));
    		dat.write((int)((value & 0x0000000000FF0000L) >> 16));
    		dat.write((int)((value & 0x000000000000FF00L) >> 8));
    		dat.write((int)(value & 0x00000000000000FFL));
    	}
    }
    
    /*
    public void writeLong(long value) throws IOException
    {
    	writeULong(value);
    }
    */
    
    /**
	 * Write a {@link DOUBLE}.
	 * @param value The {@link DOUBLE} to write.
	 * @throws IOException If any IO exception occurs.
	 */
    public void writeDouble(double value) throws IOException
    {
    	writeULong(Double.doubleToLongBits(value));
    }
    
    /*
    public void writeURational(long numerator, long denominator) throws IOException
    {
    	// Numerator first, then denominator
    	writeULong(numerator);
    	writeULong(denominator);
    }
    
    public void writeRational(long numerator, long denominator) throws IOException
    {
    	writeURational(numerator, denominator);
    }
    */
    /**
	 * Write a {@link RATIONAL} or {@link SRATIONAL}.
	 * @param numerator The numerator to write.
	 * @param denominator The denominator to write.
	 * @throws IOException If any IO exception occurs.
	 */
    public void writeURational(int numerator, int denominator) throws IOException
    {
    	// Numerator first, then denominator
    	writeUInt(numerator);
    	writeUInt(denominator);
    }
    
    /**
	 * Write a 7-bit ASCII code.
	 * @param value The 7-bit ASCII code to write.
	 * @throws IOException If any IO exception occurs.
	 */
    public void writeASCII(char value) throws IOException
    {
    	dat.write(value & 0x7F);
    }
    
    /**
     * Write a {@link ASCII} string.
     * @param value The {@link ASCII} string to write.
     * @throws IOException If any IO exception occurs.
     */
    public void writeASCII(String value) throws IOException
    {
    	if(value != null)
    	{
	    	if(value.indexOf(NUL) != -1)
	    	{
	    		throw new IllegalArgumentException(Resources.getString(BBXResource.ARGUMENT_NULL_CHAR));
	    	}
	    	char[] chars = value.toCharArray();
	    	int count = chars.length;
	    	for(int i = 0; i < count; i++)
	    	{
	    		writeASCII(chars[i]);
	    	}
    	}
    	writeASCII(NUL);
    }
    
    /**
     * Write an array of {@link ASCII} strings.
     * @param values The array of {@link ASCII} strings to write.
     * @throws IOException If any IO exception occurs.
     */
    public void writeASCII(String[] values) throws IOException
    {
    	int length = values.length;
    	for(int i = 0; i < length; i++)
    	{
    		writeASCII(values[i]);
    	}
    }
    
    /**
     * Get if the encoder is in little-endian format.
     * @return true if in little-endian format, false if big-endian.
     */
    public boolean getLittleEndian()
    {
    	return littleEndian;
    }
    
    /**
     * Write the {@link Tag}'s value to the stream.
     * @param value The {@link Tag}'s value.
     * @param offset If the value is an offset.
     * @param type The data type the value is.
     */
    public void writeTagValue(int value, boolean offset, short type) throws IOException
    {
    	int val = 0;
    	if(offset)
    	{
    		val = value;
    	}
    	else
    	{
    		switch(type)
    		{
	    		case BYTE:
	    		case SBYTE:
	    		case ASCII:
	    			if (littleEndian)
                    {
	    				int v1 = (value & 0xFF000000) >> 24;
	    		    	int v2 = (value & 0x00FF0000) >> 16;
	    		    	int v3 = (value & 0x0000FF00) >> 8;
	    		    	int v4 = value & 0x000000FF;
	    		    	val =  (v4 << 24) | (v3 << 16) | (v2 << 8) | v1;
                    }
                    else
                    {
                        val = value;
                    }
	    			break;
	    		case SHORT:
	    		case SSHORT:
	    			if(littleEndian)
	    			{
	    				val = ((value & 0xFFFF0000) >> 16) | ((value & 0x0000FFFF) << 16);
	    			}
	    			else
	    			{
	    				val = value;
	    			}
	    			break;
	    		case LONG:
                case SLONG:
                    val = value;
                    break;
    		}
    	}
    	this.writeUInt(val);
    }
    
    /**
     * Write the {@link Tag}'s value to the stream as a 8byte int.
     * @param value The {@link Tag}'s value 8byte value.
     * @param offset If the value is an offset.
     * @param type The data type the value is.
     */
    public void writeTagLongValue(long value, boolean offset, short type) throws IOException
    {
    	long val = 0;
    	if(offset)
    	{
    		val = value;
    	}
    	else
    	{
    		switch(type)
    		{
	    		case BYTE:
	    		case SBYTE:
	    		case ASCII:
	    			value = ((value & 0x00000000FFFFFFFFL) << 32) | ((value & 0xFFFFFFFF00000000L) >> 32);
	    			if (littleEndian)
                    {
	    				long v1 = (value & 0xFF00000000000000L) >> 56;
	    		    	long v2 = (value & 0x00FF000000000000L) >> 48;
	    		    	long v3 = (value & 0x0000FF0000000000L) >> 40;
	    		    	long v4 = (value & 0x000000FF00000000L) >> 32;
	    		    	long v5 = (value & 0x00000000FF000000L) >> 24;
	    		    	long v6 = (value & 0x0000000000FF0000L) >> 16;
	    		    	long v7 = (value & 0x000000000000FF00L) >> 8;
	    		    	long v8 = value & 0x00000000000000FFL;
	    		    	val = (v8 << 56) | (v7 << 48) | (v6 << 40) | (v5 << 32) | (v4 << 24) | (v3 << 16) | (v2 << 8) | v1;
                    }
                    else
                    {
                        val = value;
                    }
	    			break;
	    		case SHORT:
	    		case SSHORT:
	    			if (littleEndian)
                    {
                        val = ((value & 0xFFFF000000000000L) >> 16) | ((value & 0x0000FFFF00000000L) << 16) |
                            ((value & 0x00000000FFFF0000L) >> 16) | ((value & 0x000000000000FFFFL) << 16);
                    }
                    else
                    {
                        val = ((value & 0x00000000FFFFFFFFL) << 32) | ((value & 0xFFFFFFFF00000000L) >> 32);
                    }
	    			break;
	    		case LONG:
	    		case SLONG:
	    			if (littleEndian)
                    {
                        val = value;
                    }
                    else
                    {
                        val = ((value & 0x00000000FFFFFFFFL) << 32) | ((value & 0xFFFFFFFF00000000L) >> 32);
                    }
	    			break;
	    		case LONG8:
                case SLONG8:
                    val = value;
                    break;
    		}
    	}
    	this.writeULong(val);
    }
    
    /**
     * Shift a value so it is in a format that the {@link Writer} can write out.
     * @param value The value to shift.
     * @param pos The position it should be in the value (0-1).
     * @return The shifted value.
     */
    public static long shiftValue(int value, int pos)
    {
    	if(pos > 1 || pos < 0)
    	{
    		throw new IllegalArgumentException("pos > 1, pos < 0");
    	}
    	return pos == 1 ? (value & 0xFFFFFFFF) << 32 : (value & 0xFFFFFFFF);
    }
    
    /**
     * Shift a value so it is in a format that the {@link Writer} can write out.
     * @param value The value to shift.
     * @param pos The position it should be in the value (0-3).
     * @return The shifted value.
     */
    public static long shiftValue(short value, int pos)
    {
    	if(pos > 3 || pos < 0)
    	{
    		throw new IllegalArgumentException("pos > 3, pos < 0");
    	}
    	switch(pos)
    	{
    		case 2:
    			return (value & 0xFFFF) << 48;
    		case 3:
    			return (value & 0xFFFF) << 32;
    		case 0:
    			return (value & 0xFFFF) << 16;
    		case 1:
    			return (value & 0xFFFF);
    	}
    	return 0;
    }
    
    /**
     * Shift a value so it is in a format that the {@link Writer} can write out.
     * @param value The value to shift.
     * @param pos The position it should be in the value (0-7).
     * @return The shifted value.
     */
    public static long shiftValue(byte value, int pos)
    {
    	if(pos > 7 || pos < 0)
    	{
    		throw new IllegalArgumentException("pos > 7, pos < 0");
    	}
    	switch(pos)
    	{
	    	case 4:
				return (value & 0xFF) << 56;
			case 5:
				return (value & 0xFF) << 48;
			case 6:
				return (value & 0xFF) << 40;
			case 7:
				return (value & 0xFF) << 32;
    		case 0:
    			return (value & 0xFF) << 24;
    		case 1:
    			return (value & 0xFF) << 16;
    		case 2:
    			return (value & 0xFF) << 8;
    		case 3:
    			return (value & 0xFF);
    	}
    	return 0;
    }
    
    /**
     * Unshift a value so it is in a format that is useable.
     * @param value The value to "unshift."
     * @param pos The position it is in the value (0-1).
     * @return The unshifted value.
     */
    public static int unshiftInt(long value, int pos)
    {
    	if(pos > 1 || pos < 0)
    	{
    		throw new IllegalArgumentException("pos > 1, pos < 0");
    	}
    	return (int)(pos == 1 ? value >> 32 : value & 0x00000000FFFFFFFF);
    }
    
    /**
     * Unshift a value so it is in a format that is useable.
     * @param value The value to "unshift."
     * @param pos The position it is in the value (0-3).
     * @return The unshifted value.
     */
    public static short unshiftShort(long value, int pos)
    {
    	if(pos > 3 || pos < 0)
    	{
    		throw new IllegalArgumentException("pos > 3, pos < 0");
    	}
    	switch(pos)
    	{
	    	case 2:
	    		return (short)((value & 0xFFFF000000000000L) >> 48);
			case 3:
				return (short)((value & 0x0000FFFF00000000L) >> 32);
			case 0:
				return (short)((value & 0x00000000FFFF0000L) >> 16);
			case 1:
				return (short)(value & 0x000000000000FFFFL);
    	}
    	return 0;
    }
    
    /**
     * Unshift a value so it is in a format that is useable.
     * @param value The value to "unshift."
     * @param pos The position it is in the value (0-7).
     * @return The unshifted value.
     */
    public static byte unshiftByte(long value, int pos)
    {
    	if(pos > 7 || pos < 0)
    	{
    		throw new IllegalArgumentException("pos > 7, pos < 0");
    	}
    	switch(pos)
    	{
	    	case 4:
	    		return (byte)((value & 0xFF00000000000000L) << 56);
			case 5:
				return (byte)((value & 0x00FF000000000000L) << 48);
			case 6:
				return (byte)((value & 0x0000FF0000000000L) << 40);
			case 7:
				return (byte)((value & 0x000000FF00000000L) << 32);
    		case 0:
    			return (byte)((value & 0x00000000FF000000L) >> 24);
    		case 1:
    			return (byte)((value & 0x0000000000FF0000L) >> 16);
    		case 2:
    			return (byte)((value & 0x000000000000FF00L) << 8);
    		case 3:
    			return (byte)(value & 0x00000000000000FFL);
    	}
    	return 0;
    }
}

