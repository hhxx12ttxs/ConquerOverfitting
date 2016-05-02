package com.objectwave.utility;
import com.objectwave.exception.NotFoundException;
import com.objectwave.logging.MessageLog;
import java.io.*;
import java.lang.reflect.*;
import java.sql.Timestamp;

import java.util.Date;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
/**
 *  This class converts data from one type to another. Used extensively for
 *  persistence manipulation.
 *
 * @author  Dave Hoag
 * @version  $Date: 2005/03/29 04:10:01 $ $Revision: 2.7 $
 */
public class ObjectFormatter
{
	//Used for compressing serialized objects. We know mininum size is 21k bytes.
	/**
	 */
	public final static int zipBufferSize = 8192;
	/**
	 */
	public final static String Native_Array_Delimiter = "\\\\ \n";
	protected static ThreadLocal outputStreams = new ThreadLocal();
	/**
	 * @param  fromString
	 * @return
	 */
	public static String replace1QuoteWith2(String fromString)
	{
		int fromSize = fromString.length();
		char[] newArray = new char[fromSize * 2];
		char[] fromArray = fromString.toCharArray();
		int count = 0;
		int i;
		for(i = 0; i < fromSize; i++)
		{
			newArray[count++] = fromArray[i];
			if(fromArray[i] == '\'')
			{
				newArray[count++] = '\'';
			}
		}

		return new String(newArray, 0, count);
	}
	/**
	 *  When converting to an array, we need every element to be inserted into the
	 *  array to be of the correct object type. Since the original Object type is a
	 *  String this wouldn't work for an Array of ints. We convert the string to an
	 *  Integer and the reflection array processing takes care of the rest. This
	 *  method is different than convertType in that we know we are starting from a
	 *  String object.
	 *
	 * @param  c Class to which the string is going to be converted.
	 * @param  str A String representation of the class.
	 * @return
	 */
	public Object convertString(final Class c, final String str)
	{
		if(c.isArray())
		{
			return createArray(c, str);
		}
		else
				if(ScalarType.class.isAssignableFrom(c))
		{
			return createScalarType(c, str);
		}
		else
				if(c.isPrimitive())
		{
			return convertPrimitive(c, str);
		}
		else
				if(c == String.class)
		{
			return str;
		}
		else
		{
			if(java.lang.Number.class.isAssignableFrom(c))
			{
				return createNumber(c, new Double(str));
			}
			else
					if(java.lang.Boolean.class.isAssignableFrom(c))
			{
				if(str.trim().length() < 1)
				{
					return new Boolean(false);
				}
				char ch = str.trim().charAt(0);
				return new Boolean("tT1\001".indexOf(ch) > -1);
			}
			else
					if(java.lang.Character.class.isAssignableFrom(c))
			{
				char ch = str.trim().charAt(0);
				return new Character(ch);
			}
			else
					if(java.util.Date.class.isAssignableFrom(c))
			{
				return (Date) java.sql.Timestamp.valueOf(str.trim());
			}
			else
					if(java.io.Serializable.class.isAssignableFrom(c))
			{
				//LDA: delete previous "0x" from the str
				return readSerializable(stringToBytes(str.substring(2)));
			}
			return str;
		}
	}
	/**
	 *  Convert the object from its database representation to its actual value in
	 *  the business object. This is the most likely method to be used in other
	 *  processes. If the 'c' is a primtitive type, we force the value to zero.
	 *
	 * @param  c Class to which the object is going to be converted.
	 * @param  obj An unkown object type that may need to be converted to be a type
	 *      of the class 'c'.
	 * @return  An instance of the passed in type.
	 */
	public Object convertType(final Class c, Object obj)
	{
		if(obj == null)
		{
			if(c.isPrimitive())
			{
				return convertPrimitive(c, "0");
			}
			return null;
		}
		if( obj instanceof Number && c.isPrimitive()){
			return createNumber( c, (Number)obj);
		}
		if(obj.getClass() == c)
		{
			//No conversion necessary - The object is in the correct format

			return obj;
		}
		//If the data is a string and the Variable is of a type that is not a string, convert this
		if(obj instanceof String && (!java.lang.String.class.isAssignableFrom(c)))
		{
			obj = convertString(c, (String) obj);
		}
		else
		//If the target class is Serializable and the source data is a byte[], try to read the bytes.
		if(Serializable.class.isAssignableFrom(c) && obj.getClass().isArray() && obj.getClass().getComponentType() == byte.class)
		{
			obj = readSerializable((byte[]) obj);
		}
		else
		//If the Variable contains a ScalarType, convert the database data to a string, and convert the type.
		if(ScalarType.class.isAssignableFrom(c))
		{
			obj = convertString(c, obj.toString());
		}
		else
		//Sometimes it comes out of the database as a float or double, but is actually an int in an object.
		if(obj instanceof Double || obj instanceof Float)
		{
			obj = createNumber(c, (Number) obj);
		}
		if(boolean.class == c || Boolean.class == c)
		{
			// The boolean type may be encoded as a 'char' in the database.
			// Char values 't','T', '1', and 1 map to true, all other values map to false.

			//
			if(obj instanceof Character)
			{
				obj = new Boolean("tT1\001".indexOf(((Character) obj).charValue()) > -1);
			}
		}
		else
		if(String.class == c)
		{
			try
			{
				obj = formatValue(obj);
			}
			catch(IOException ex)
			{
				MessageLog.warn(this, "Tried a smart format '" + obj + "', just going with toString ", ex);
				obj = String.valueOf(obj);
			}
		}
		return obj;
	}
	/**
	 *  Create an object from the bytes read from the database. It is assumed that
	 *  the bytes were written via JGrinder. JGrinder compresses the bytes during
	 *  the insert/update and we decompress the bytes here.
	 *
	 * @param  bytes byte[] Zipped byte array that contains a serialized object
	 * @return
	 * @see  #serializeObject
	 */
	public Object readSerializable(byte[] bytes)
	{
		try
		{
			ByteArrayInputStream binaryInputStream = new ByteArrayInputStream(bytes);
			GZIPInputStream gzipStream = new GZIPInputStream(binaryInputStream, bytes.length);
			ObjectInputStream os = new ObjectInputStream(gzipStream);
			Object result = os.readObject();

			return result;
		}
		catch(Throwable t)
		{
			System.err.println("Failed to convert bytes to serializable. ");
			t.printStackTrace(System.err);
			return bytes;
		}
	}
	/**
	 *  Convert a native array into a string value.
	 *
	 * @param  dataArray an object representing an array. The value is assumed to
	 *      be non-null.
	 * @return
	 */
	public String convertArray(final Object dataArray)
	{
		String newVal = null;
		try
		{
			StringBuffer concat = new StringBuffer();
			int length = java.lang.reflect.Array.getLength(dataArray);
			for(int j = 0; j < length; j++)
			{
				Object val = java.lang.reflect.Array.get(dataArray, j);
				if(val instanceof ScalarType)
				{
					concat.append(((ScalarType) val).toDatabaseString());
				}
				else
				{
					concat.append(val.toString());
				}

				if(j < length - 1)
				{
					concat.append(Native_Array_Delimiter);
				}
			}
			newVal = concat.toString();
		}
		catch(Exception e)
		{
			System.out.println("Array update error \n" + e);
		}
		return newVal;
	}
	/**
	 *  Convert the Object to a string.
	 *
	 * @param  value
	 * @return
	 * @exception  IOException
	 */
	public String formatValue(final Object value) throws IOException
	{
		final StringBuffer buf = new StringBuffer();
		formatValue(value, buf);
		return buf.toString();
	}
	/**
	 *  Common formatting routine used by most of the sqlAssembler objects.
	 *
	 * @param  value
	 * @param  buf
	 * @exception  IOException
	 */
	public void formatValue(final Object value, final StringBuffer buf) throws IOException
	{
		if(value == null)
		{
			buf.append("NULL");
			return;
		}
		if(value instanceof String)
		{
			String str = (String) value;
			buf.append('\'').append(replace1QuoteWith2((String) value)).append('\'');
		}
		else
				if(value instanceof byte[])
		{
			buf.append('\'');
			bytesToString((byte[]) value, buf);
			buf.append('\'');
		}
		else
				if(value instanceof Character)
		{
			buf.append('\'').append(value).append('\'');
		}
		else
				if(value instanceof Date)
		{
			formatDate((Date) value, buf);
		}
		else
				if(value instanceof Boolean)
		{
			if(((Boolean) value).booleanValue())
			{
				buf.append("1");
			}
			else
			{
				buf.append("0");
			}
		}
		else
				if(value instanceof Number)
		{
			buf.append(value);
		}
		else
				if(value instanceof ScalarType)
		{
			buf.append('\'').append(((ScalarType) value).toDatabaseString()).append('\'');
		}
		else
				if(value instanceof java.io.Serializable)
		{
			//Assume we are storing a byte []

			formatSerializable((java.io.Serializable) value, buf);
		}

		else
		{
			buf.append(value);
		}
	}
	/**
	 *  Serialize the object into a byte []
	 *
	 * @param  value
	 * @return
	 * @exception  IOException
	 */
	public byte[] serializeObject(final java.io.Serializable value) throws IOException
	{
		ByteArrayOutputStream binaryOutputStream = (ByteArrayOutputStream) outputStreams.get();
		if(binaryOutputStream == null)
		{
			binaryOutputStream = new ByteArrayOutputStream();
			outputStreams.set(binaryOutputStream);
		}
		final GZIPOutputStream gzipStream = new GZIPOutputStream(binaryOutputStream, zipBufferSize);
		final ObjectOutputStream os = new ObjectOutputStream(gzipStream);
		os.writeObject(value);
		os.flush();
		os.close();
		gzipStream.close();

		final byte[] theObjectSerialized = binaryOutputStream.toByteArray();
		binaryOutputStream.reset();
		return theObjectSerialized;
	}
	/**
	 *  Take a string of bytes in Hex format and create a byte array from the
	 *  string. The string must have 2 digits for each byte, therefore bytes with a
	 *  value less than 0x10 must begin with a zero. Example: 0xaff would be 0aff
	 *  in the byte string
	 *
	 * @param  string
	 * @return  The array of bytes from the string
	 */
	public byte[] stringToBytes(String string)
	{
		final int arraySize = string.length() / 2;
		byte[] data = new byte[arraySize];
		for(int i = 0; i < arraySize; i++)
		{
			//if an exception occurs here, the string is not properly formed
			String byteString = String.valueOf(string.charAt(i * 2));
			byteString += string.charAt((i * 2) + 1);
			try
			{
				//Use the Integer.parseInt to avoid problems with negative byte values
				final byte b = (byte) Integer.parseInt(byteString, 16);
				data[i] = b;
			}
			catch(NumberFormatException ex)
			{
				data[i] = (byte) 0;
				//BrokerFactory.println("Exception converting a byte string value," + byteString+ " to byte" + ex);
			}
		}
		return data;
	}
	/**
	 *  If the SQL type was "NUMERIC", then we need to check to see if the Double
	 *  that NUMBERIC values are converted to should really be an Integer or a
	 *  Long.
	 *
	 * @param  c
	 * @param  value
	 * @return
	 */
	public Number createNumber(final Class c, final Number value)
	{
		if(short.class == c || Short.class == c)
		{
			return new Short(value.shortValue());
		}
		else
				if(byte.class == c || Byte.class == c)
		{
			return new Byte(value.byteValue());
		}
		else
				if(int.class == c || Integer.class == c)
		{
			return new Integer(value.intValue());
		}
		else
				if(long.class == c || Long.class == c)
		{
			return new Long(value.longValue());
		}
		else
				if(float.class == c || Float.class == c)
		{
			return new Float(value.floatValue());
		}
		return value;
	}
	/**
	 *  To facilitate overriding of this method, the formatting is broken out.
	 *
	 * @param  buf StringBuffer upon which to write the data.
	 * @param  value Serializable object to convert to a string value.
	 * @exception  IOException
	 */
	public void formatSerializable(final java.io.Serializable value, final StringBuffer buf) throws IOException
	{
		final byte[] theObjectSerialized = serializeObject(value);
		buf.append("0x");
		bytesToString(theObjectSerialized, buf);
	}
	/**
	 *  Convert the byte array into a string buffer representing the hexidecimal
	 *  equivalent of the data. Two hexidecimal characters will be created for each
	 *  byte.
	 *
	 * @param  data
	 * @param  buffer
	 */
	protected void bytesToString(final byte[] data, final StringBuffer buffer)
	{
		int length = data.length;
		for(int j = 0; j < length; j++)
		{
			//				if(j == 0) buf.append("0x");

			byte val = data[j];
			int res = val & 0xFF;

			if(res > 0xf)
			{
				buffer.append(Integer.toHexString(res));
			}
			else
			{
				buffer.append("0" + Integer.toHexString(res));
			}
		}
	}
	/**
	 *  Convert the string value to the correct primitive type.
	 *
	 * @param  c Class to which the string is going to be converted.
	 * @param  aStringValue A String representation of the class.
	 * @return
	 */
	protected Object convertPrimitive(final Class c, final String aStringValue)
	{
        //Since it's a primitive, we know we can trim
        String str = aStringValue.trim();
		if(byte.class == c)
		{
			return new Byte(str);
		}
		if(short.class == c)
		{
			return new Short(str);
		}
		if(int.class == c)
		{
			return new Integer(str);
		}
		if(long.class == c)
		{
			return new Long(str);
		}
		if(float.class == c)
		{
			return new Float(str);
		}
		if(double.class == c)
		{
			return new Double(str);
		}
		if(char.class == c)
		{
			return new Character(str.length() == 0 ? '\0' : str.charAt(0));
		}

		if(str.trim().length() < 1)
		{
			if(boolean.class == c)
			{
				return new Boolean(false);
			}
			else
			{
				return new Character(' ');
			}
		}
		char ch = str.trim().charAt(0);

		if(boolean.class == c)
		{
			return new Boolean("tT1\001".indexOf(ch) > -1);
		}
		return new Character(ch);
	}
	/**
	 *  Convert a string value into a Native Array. The data came out of the
	 *  database as a string, but the class itself is an array. We attempt to
	 *  convert every string value in the found in the data value to be an element
	 *  in the correct scalar array. I've used '\n' the new line character to
	 *  sperate entries. This could cause problems but many of the other characters
	 *  were having troubles getting in an out of the datatbase. If an exception
	 *  occurs while building array, return null for the array value.
	 *
	 * @param  clazz java.lang.Class The class of the native array being built.
	 * @param  vals
	 * @return
	 */
	protected Object createArray(final Class clazz, final String vals)
	{
		final Class arrayType = clazz.getComponentType();
		if(arrayType == byte.class)
		{
			//bytes get converted in the formatValue method

			return stringToBytes(vals);
		}

		Object newVal = null;
		if(vals != null && (!vals.trim().equals("")))
		{
			Vector v = com.objectwave.utility.StringManipulator.extractStringsDelimiter(vals, Native_Array_Delimiter);
			final int size = v.size();
			newVal = Array.newInstance(arrayType, size);
			for(int j = 0; j < size; j++)
			{
				String source = (String) v.elementAt(j);
				Object dataVal = convertString(clazz.getComponentType(), source);
				Array.set(newVal, j, dataVal);
			}
		}
		return newVal;
	}
	/**
	 *  To support custom 'scalar' types (types that will be stored in the database
	 *  as a string, but will in fact be an object in the object model) a
	 *  ScalarFactory was introduced. This factory should convert the String found
	 *  in the database to the correct custom ScalarType.
	 *
	 * @param  c
	 * @param  value
	 * @return
	 */
	protected Object createScalarType(final Class c, final String value)
	{
		try
		{
			return ScalarTypeFactory.create(c, value.toString());
		}
		catch(NotFoundException ex)
		{
			MessageLog.error(this, "Error creating " + c + ". Returning a null for the value.", ex);
			return null;
		}
		catch(java.text.ParseException ex)
		{
			MessageLog.error(this, "Error creating " + c + ". Returning a null for the value.", ex);
			return null;
		}
	}
	/**
	 *  To facilitate overriding of this method, the formatting is broken out.
	 *
	 * @param  value Date object to format.
	 * @param  buf StringBuffer upon which to write the data.
	 */
	protected void formatDate(final Date value, final StringBuffer buf)
	{
		//buf.append('\'').append(new Timestamp(value.getTime())).append('\'');
		buf.append(new Timestamp(value.getTime()));
	}
}


