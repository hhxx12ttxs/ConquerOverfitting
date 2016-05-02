<<<<<<< HEAD
package jhpservices.servehttp.com;

/*
 *    Copyright 2011 JSON-SMART authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jhpservices.servehttp.com.parser.ContentHandler;
import jhpservices.servehttp.com.parser.ContentHandlerCompressor;
import jhpservices.servehttp.com.parser.FakeContainerFactory;
import jhpservices.servehttp.com.parser.JSONParser;
import jhpservices.servehttp.com.parser.ParseException;
import static jhpservices.servehttp.com.parser.ContainerFactory.FACTORY_ORDERED;
import static jhpservices.servehttp.com.parser.ContainerFactory.FACTORY_SIMPLE;
import static jhpservices.servehttp.com.parser.JSONParser.DEFAULT_PERMISSIVE_MODE;
import static jhpservices.servehttp.com.parser.JSONParser.MODE_RFC4627;

/**
 * JSONValue is the helper class In most of case you should use those static
 * methode to user JSON-smart
 * 
 * 
 * The most commonly use methode are {@link #parse(String)}
 * {@link #toJSONString(Object)}
 * 
 * @author Uriel Chemouni <uchemouni@gmail.com>
 */
public class JSONValue {
	/**
	 * Global default compression type
	 */
	public static JSONStyle COMPRESSION = JSONStyle.NO_COMPRESS;

	/**
	 * Used for validating Json inputs
	 */
	private final static FakeContainerFactory FACTORY_FAKE_COINTAINER = new FakeContainerFactory();

	/**
	 * Parse JSON text into java object from the input source. Please use
	 * parseWithException() if you don't want to ignore the exception. if you
	 * want strict input check use parseStrict()
	 * 
	 * @see JSONParser#parse(Reader)
	 * @see #parseWithException(Reader)
	 * 
	 * @since 1.0.9-2
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 * 
	 */
	public static Object parse(byte[] in) {
		try {
			return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Parse JSON text into java object from the input source. Please use
	 * parseWithException() if you don't want to ignore the exception. if you
	 * want strict input check use parseStrict()
	 * 
	 * @see JSONParser#parse(Reader)
	 * @see #parseWithException(Reader)
	 * 
	 * @since 1.1.2
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 * 
	 */
	public static Object parse(byte[] in, int offset, int length) {
		try {
			return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, offset, length);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Parse JSON text into java object from the input source. Please use
	 * parseWithException() if you don't want to ignore the exception. if you
	 * want strict input check use parseStrict()
	 * 
	 * @see JSONParser#parse(Reader)
	 * @see #parseWithException(Reader)
	 * 
	 * @since 1.0.9-2
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 * 
	 */
	public static Object parse(InputStream in) {
		try {
			return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Parse JSON text into java object from the input source. Please use
	 * parseWithException() if you don't want to ignore the exception. if you
	 * want strict input check use parseStrict()
	 * 
	 * @see JSONParser#parse(Reader)
	 * @see #parseWithException(Reader)
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 * 
	 */
	public static Object parse(Reader in) {
		try {
			return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Parse JSON text into java object from the input source. Please use
	 * parseWithException() if you don't want to ignore the exception. if you
	 * want strict input check use parseStrict()
	 * 
	 * @see JSONParser#parse(String)
	 * @see #parseWithException(String)
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 * 
	 */
	public static Object parse(String s) {
		try {
			return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(s);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Parse Json input to a java Object keeping element order
	 * 
	 * @since 1.0.9-2
	 */
	public static Object parseKeepingOrder(byte[] in) {
		try {
			return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, FACTORY_ORDERED);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Parse Json input to a java Object keeping element order
	 * 
	 * @since 1.1.2
	 */
	public static Object parseKeepingOrder(byte[] in, int offset, int length) {
		try {
			return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, offset, length, FACTORY_ORDERED);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Parse Json input to a java Object keeping element order
	 * 
	 * @since 1.0.9-2
	 */
	public static Object parseKeepingOrder(InputStream in) {
		try {
			return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, FACTORY_ORDERED);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Parse Json input to a java Object keeping element order
	 * 
	 * @since 1.0.6.1
	 */
	public static Object parseKeepingOrder(Reader in) {
		try {
			return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, FACTORY_ORDERED);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Parse Json input to a java Object keeping element order
	 * 
	 * @since 1.0.6.1
	 */
	public static Object parseKeepingOrder(String in) {
		try {
			return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, FACTORY_ORDERED);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Parse Json Using SAX event handler
	 * 
	 * @since 1.0.9-2
	 */
	public static void SAXParse(InputStream input, ContentHandler handler) throws ParseException, IOException {
		JSONParser p = new JSONParser(DEFAULT_PERMISSIVE_MODE);
		p.parse(input, FACTORY_FAKE_COINTAINER, handler);
	}

	/**
	 * Parse Json Using SAX event handler
	 * 
	 * @since 1.0.6.2
	 */
	public static void SAXParse(Reader input, ContentHandler handler) throws ParseException, IOException {
		JSONParser p = new JSONParser(DEFAULT_PERMISSIVE_MODE);
		p.parse(input, FACTORY_FAKE_COINTAINER, handler);
	}

	/**
	 * Parse Json Using SAX event handler
	 * 
	 * @since 1.0.6.2
	 */
	public static void SAXParse(String input, ContentHandler handler) throws ParseException {
		JSONParser p = new JSONParser(DEFAULT_PERMISSIVE_MODE);
		p.parse(input, FACTORY_FAKE_COINTAINER, handler);
	}

	/**
	 * Reformat Json input keeping element order
	 * 
	 * @since 1.0.6.2
	 */
	public static String compress(String input, JSONStyle style) {
		try {
			StringBuilder sb = new StringBuilder();
			ContentHandlerCompressor comp = new ContentHandlerCompressor(sb, style);
			JSONParser p = new JSONParser(DEFAULT_PERMISSIVE_MODE);
			p.parse(input, FACTORY_FAKE_COINTAINER, comp);
			return sb.toString();
		} catch (Exception e) {
			return input;
		}
	}

	/**
	 * Compress Json input keeping element order
	 * 
	 * @since 1.0.6.1
	 */
	public static String compress(String s) {
		return compress(s, JSONStyle.MAX_COMPRESS);
	}

	/**
	 * Compress Json input keeping element order
	 * 
	 * @since 1.0.6.1
	 */
	public static String uncompress(String s) {
		return compress(s, JSONStyle.NO_COMPRESS);
	}

	/**
	 * Parse JSON text into java object from the input source.
	 * 
	 * @see JSONParser
	 * 
	 * @since 1.0.9-2
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 */
	public static Object parseWithException(byte[] in) throws IOException, ParseException {
		return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, FACTORY_SIMPLE);
	}

	/**
	 * Parse JSON text into java object from the input source.
	 * 
	 * @see JSONParser
	 * 
	 * @since 1.1.2
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 */
	public static Object parseWithException(byte[] in, int offset, int length) throws IOException, ParseException {
		return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, offset, length, FACTORY_SIMPLE);
	}

	/**
	 * Parse JSON text into java object from the input source.
	 * 
	 * @see JSONParser
	 * 
	 * @since 1.0.9-2
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 */
	public static Object parseWithException(InputStream in) throws IOException, ParseException {
		return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, FACTORY_SIMPLE);
	}

	/**
	 * Parse JSON text into java object from the input source.
	 * 
	 * @see JSONParser
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 */
	public static Object parseWithException(Reader in) throws IOException, ParseException {
		return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, FACTORY_SIMPLE);
	}

	/**
	 * Parse JSON text into java object from the input source.
	 * 
	 * @see JSONParser
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 */
	public static Object parseWithException(String s) throws ParseException {
		return new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(s, FACTORY_SIMPLE);
	}

	/**
	 * Parse valid RFC4627 JSON text into java object from the input source.
	 * 
	 * @see JSONParser
	 * 
	 * @since 1.0.9-2
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 */
	public static Object parseStrict(InputStream in) throws IOException, ParseException {
		return new JSONParser(MODE_RFC4627).parse(in, FACTORY_SIMPLE);
	}

	/**
	 * Parse valid RFC4627 JSON text into java object from the input source.
	 * 
	 * @see JSONParser
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 */
	public static Object parseStrict(Reader in) throws IOException, ParseException {
		return new JSONParser(MODE_RFC4627).parse(in, FACTORY_SIMPLE);
	}

	/**
	 * Parse valid RFC4627 JSON text into java object from the input source.
	 * 
	 * @see JSONParser
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 */
	public static Object parseStrict(String s) throws ParseException {
		return new JSONParser(MODE_RFC4627).parse(s, FACTORY_SIMPLE);
	}

	/**
	 * Parse valid RFC4627 JSON text into java object from the input source.
	 * 
	 * @see JSONParser
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 */
	public static Object parseStrict(byte[] s) throws ParseException {
		return new JSONParser(MODE_RFC4627).parse(s, FACTORY_SIMPLE);
	}

	/**
	 * Parse valid RFC4627 JSON text into java object from the input source.
	 * 
	 * @see JSONParser
	 * 
	 * @since 1.1.2
	 * 
	 * @return Instance of the following: JSONObject, JSONArray, String,
	 *         java.lang.Number, java.lang.Boolean, null
	 */
	public static Object parseStrict(byte[] s, int offset, int length) throws ParseException {
		return new JSONParser(MODE_RFC4627).parse(s, offset, length, FACTORY_SIMPLE);
	}

	/**
	 * Check RFC4627 Json Syntax from input Reader
	 * 
	 * @return if the input is valid
	 */
	public static boolean isValidJsonStrict(Reader in) throws IOException {
		try {
			new JSONParser(MODE_RFC4627).parse(in, FACTORY_FAKE_COINTAINER);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	/**
	 * check RFC4627 Json Syntax from input String
	 * 
	 * @return if the input is valid
	 */
	public static boolean isValidJsonStrict(String s) {
		try {
			new JSONParser(MODE_RFC4627).parse(s, FACTORY_FAKE_COINTAINER);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	/**
	 * Check Json Syntax from input Reader
	 * 
	 * @return if the input is valid
	 */
	public static boolean isValidJson(Reader in) throws IOException {
		try {
			new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(in, FACTORY_FAKE_COINTAINER);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	/**
	 * Check Json Syntax from input String
	 * 
	 * @return if the input is valid
	 */
	public static boolean isValidJson(String s) {
		try {
			new JSONParser(DEFAULT_PERMISSIVE_MODE).parse(s, FACTORY_FAKE_COINTAINER);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	/**
	 * Encode an object into JSON text and write it to out.
	 * <p>
	 * If this object is a Map or a List, and it's also a JSONStreamAware or a
	 * JSONAware, JSONStreamAware or JSONAware will be considered firstly.
	 * <p>
	 * 
	 * @see JSONObject#writeJSON(Map, Appendable)
	 * @see JSONArray#writeJSONString(List, Appendable)
	 */
	public static void writeJSONString(Object value, Appendable out) throws IOException {
		writeJSONString(value, out, COMPRESSION);
	}

	/**
	 * Encode an object into JSON text and write it to out.
	 * <p>
	 * If this object is a Map or a List, and it's also a JSONStreamAware or a
	 * JSONAware, JSONStreamAware or JSONAware will be considered firstly.
	 * <p>
	 * 
	 * @see JSONObject#writeJSON(Map, Appendable)
	 * @see JSONArray#writeJSONString(List, Appendable)
	 */
	@SuppressWarnings("unchecked")
	public static void writeJSONString(Object value, Appendable out, JSONStyle compression) throws IOException {
		if (value == null) {
			out.append("null");
			return;
		}

		if (value instanceof String) {
			if (!compression.mustProtectValue((String) value))
				out.append((String) value);
			else {
				out.append('"');
				escape((String) value, out, compression);
				out.append('"');
			}
			return;
		}

		if (value instanceof Number) {
			if (value instanceof Double) {
				if (((Double) value).isInfinite())
					out.append("null");
				else
					out.append(value.toString());
			} else if (value instanceof Float) {
				if (((Float) value).isInfinite())
					out.append("null");
				else
					out.append(value.toString());
			} else {
				out.append(value.toString());
			}
			return;
		}

		if (value instanceof Boolean) {
			out.append(value.toString());
		} else if ((value instanceof JSONStreamAware)) {
			if (value instanceof JSONStreamAwareEx)
				((JSONStreamAwareEx) value).writeJSONString(out, compression);
			else
				((JSONStreamAware) value).writeJSONString(out);
		} else if ((value instanceof JSONAware)) {
			if ((value instanceof JSONAwareEx))
				out.append(((JSONAwareEx) value).toJSONString(compression));
			else
				out.append(((JSONAware) value).toJSONString());
		} else if (value instanceof Map<?, ?>) {
			JSONObject.writeJSON((Map<String, Object>) value, out, compression);
		} else if (value instanceof Iterable<?>) { // List
			JSONArray.writeJSONString((Iterable<Object>) value, out, compression);
		} else if (value instanceof Date) {
			JSONValue.writeJSONString(value.toString(), out, compression);
		} else if (value instanceof Enum<?>) {
			@SuppressWarnings("rawtypes")
			String s = ((Enum) value).name();
			if (!compression.mustProtectValue(s))
				out.append(s);
			else {
				out.append('"');
				escape(s, out, compression);
				out.append('"');
			}
			return;
		} else if (value.getClass().isArray()) {
			Class<?> arrayClz = value.getClass();
			Class<?> c = arrayClz.getComponentType();

			out.append('[');
			boolean needSep = false;

			if (c.isPrimitive()) {
				if (c == int.class) {
					for (int b : ((int[]) value)) {
						if (needSep)
							out.append(',');
						else
							needSep = true;
						out.append(Integer.toString(b));
					}
				} else if (c == short.class) {
					for (short b : ((short[]) value)) {
						if (needSep)
							out.append(',');
						else
							needSep = true;
						out.append(Short.toString(b));
					}
				} else if (c == byte.class) {
					for (byte b : ((byte[]) value)) {
						if (needSep)
							out.append(',');
						else
							needSep = true;
						out.append(Byte.toString(b));
					}
				} else if (c == long.class) {
					for (long b : ((long[]) value)) {
						if (needSep)
							out.append(',');
						else
							needSep = true;
						out.append(Long.toString(b));
					}
				} else if (c == float.class) {
					for (float b : ((float[]) value)) {
						if (needSep)
							out.append(',');
						else
							needSep = true;
						out.append(Float.toString((float) b));
					}
				} else if (c == double.class) {
					for (double b : ((double[]) value)) {
						if (needSep)
							out.append(',');
						else
							needSep = true;
						out.append(Double.toString((double) b));
					}
				} else if (c == boolean.class) {
					for (boolean b : ((boolean[]) value)) {
						if (needSep)
							out.append(',');
						else
							needSep = true;
						if (b)
							out.append("true");
						else
							out.append("false");
					}
				}
			} else {
				for (Object o : ((Object[]) value)) {
					if (needSep)
						out.append(',');
					else
						needSep = true;
					writeJSONString(o, out, compression);
				}
			}
			out.append(']');
		} else {
			try {
				Class<?> cls = value.getClass();
				boolean needSep = false;
				Field[] fields = cls.getDeclaredFields();
				out.append('{');
				for (Field field : fields) {
					int m = field.getModifiers();
					if ((m & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) > 0)
						continue;
					Object v = null;
					if ((m & Modifier.PUBLIC) > 0) {
						v = field.get(value);
					} else {
						String g = JSONUtil.getGetterName(field.getName());
						Method mtd = null;

						try {
							mtd = cls.getDeclaredMethod(g);
						} catch (Exception e) {
						}
						if (mtd == null) {
							Class<?> c2 = field.getType();
							if (c2 == Boolean.TYPE || c2 == Boolean.class) {
								g = JSONUtil.getIsName(field.getName());
								mtd = cls.getDeclaredMethod(g);
							}
						}
						if (mtd == null)
							continue;
						v = mtd.invoke(value);
					}
					if (needSep)
						out.append(',');
					else
						needSep = true;
					JSONObject.writeJSONKV(field.getName(), v, out, compression);
				}
				out.append('}');
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Encode an object into JSON text and write it to out.
	 * <p>
	 * If this object is a Map or a List, and it's also a JSONStreamAware or a
	 * JSONAware, JSONStreamAware or JSONAware will be considered firstly.
	 * <p>
	 * 
	 * @see JSONObject#writeJSON(Map, Appendable)
	 * @see JSONArray#writeJSONString(List, Appendable)
	 */
	public static String toJSONString(Object value) {
		return toJSONString(value, COMPRESSION);
	}

	/**
	 * Convert an object to JSON text.
	 * <p>
	 * If this object is a Map or a List, and it's also a JSONAware, JSONAware
	 * will be considered firstly.
	 * <p>
	 * DO NOT call this method from toJSONString() of a class that implements
	 * both JSONAware and Map or List with "this" as the parameter, use
	 * JSONObject.toJSONString(Map) or JSONArray.toJSONString(List) instead.
	 * 
	 * @see JSONObject#toJSONString(Map)
	 * @see JSONArray#toJSONString(List)
	 * 
	 * @return JSON text, or "null" if value is null or it's an NaN or an INF
	 *         number.
	 */
	public static String toJSONString(Object value, JSONStyle compression) {
		StringBuilder sb = new StringBuilder();
		try {
			writeJSONString(value, sb, compression);
		} catch (IOException e) {
			// can not append on a StringBuilder
		}
		return sb.toString();
	}

	public static String escape(String s) {
		return escape(s, COMPRESSION);
	}

	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters
	 * (U+0000 through U+001F).
	 */
	public static String escape(String s, JSONStyle compression) {
		if (s == null)
			return null;
		StringBuilder sb = new StringBuilder();
		compression.escape(s, sb);
		return sb.toString();
	}

	public static void escape(String s, Appendable ap) {
		escape(s, ap, COMPRESSION);
	}

	public static void escape(String s, Appendable ap, JSONStyle compression) {
		if (s == null)
			return;
		compression.escape(s, ap);
	}
=======
/**
 * Tungsten Scale-Out Stack
 * Copyright (C) 2007-2010 Continuent Inc.
 * Contact: tungsten@continuent.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of version 2 of the GNU General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
 *
 * Initial developer(s): Robert Hodges
 * Contributor(s): Gilles Rayrat
 */

package com.continuent.tungsten.commons.config.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.continuent.tungsten.commons.config.Interval;
import com.continuent.tungsten.commons.config.TungstenProperties;

/**
 * Implements a simple unit test for Tungsten properties.
 * 
 * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
 * @version 1.0
 */
public class TungstenPropertiesTest extends TestCase
{
    /**
     * Tests round trip use of accessors for properties.
     */
    public void testAccessors() throws Exception
    {
        TungstenProperties props = new TungstenProperties();

        props.setString("string1", "mystring");
        props.setString("string2", "");
        assertEquals("Checking normal string", "mystring", props
                .getString("string1"));
        assertEquals("Checking empty string", "", props.getString("string2"));

        props.setInt("int1", 13);
        props.setInt("int2", 0);
        assertEquals("Checking normal int", 13, props.getInt("int1"));
        assertEquals("Checking zero", 0, props.getInt("int2"));

        props.setBoolean("boolean1", true);
        props.setBoolean("boolean2", false);
        assertEquals("Checking true", true, props.getBoolean("boolean1"));
        assertEquals("Checking false", false, props.getBoolean("boolean2"));

        props.setFloat("float1", Float.MIN_VALUE);
        props.setFloat("float2", Float.MAX_VALUE);
        props.setFloat("float3", Float.POSITIVE_INFINITY);
        props.setFloat("float4", Float.NEGATIVE_INFINITY);
        props.setFloat("float5", Float.NaN);
        props.setFloat("float6", 0);
        assertEquals("Checking min float", Float.MIN_VALUE, props
                .getFloat("float1"));
        assertEquals("Checking max float", Float.MAX_VALUE, props
                .getFloat("float2"));
        assertEquals("Checking +inf float", Float.POSITIVE_INFINITY, props
                .getFloat("float3"));
        assertEquals("Checking -inf float", Float.NEGATIVE_INFINITY, props
                .getFloat("float4"));
        assertEquals("Checking NaN float", Float.NaN, props.getFloat("float5"));
        assertEquals("Checking zero float", new Float(0), props
                .getFloat("float6"));

        props.setDouble("double1", Double.MIN_VALUE);
        props.setDouble("double2", Double.MAX_VALUE);
        props.setDouble("double3", Double.POSITIVE_INFINITY);
        props.setDouble("double4", Double.NEGATIVE_INFINITY);
        props.setDouble("double5", Double.NaN);
        props.setDouble("double6", 0);
        assertEquals("Checking min double", Double.MIN_VALUE, props
                .getDouble("double1"));
        assertEquals("Checking max double", Double.MAX_VALUE, props
                .getDouble("double2"));
        assertEquals("Checking +inf double", Double.POSITIVE_INFINITY, props
                .getDouble("double3"));
        assertEquals("Checking -inf double", Double.NEGATIVE_INFINITY, props
                .getDouble("double4"));
        assertEquals("Checking NaN double", Double.NaN, props
                .getDouble("double5"));
        assertEquals("Checking zero double", new Double(0), props
                .getDouble("double6"));

        Date now = new Date();
        props.setDate("date1", new Date(0));
        props.setDate("date2", now);
        props.setDate("date3", new Date(Long.MAX_VALUE));
        props.setDate("date4", new Date(Long.MIN_VALUE));
        assertEquals("Checking Epoch", new Date(0), props.getDate("date1"));
        assertEquals("Checking now", now, props.getDate("date2"));
        assertEquals("Checking max date", new Date(Long.MAX_VALUE), props
                .getDate("date3"));
        assertEquals("Checking min date", new Date(Long.MIN_VALUE), props
                .getDate("date4"));

        File file1 = new File("/etc/init.d");
        props.setFile("file1", file1);
        assertEquals("Checking File instance", file1, props.getFile("file1"));

        Interval interval = new Interval(10000);
        props.setInterval("interval1", interval);
        assertEquals("Checking interval with object", interval, props
                .getInterval("interval1"));
        props.setString("interval2", "3m");
        assertEquals("Checking string interval", 180000, props.getInterval(
                "interval2").longValue());
    }

    /**
     * Tests round trip storage and loading of properties.
     */
    public void testSerialization() throws Exception
    {
        // Create a properties object for testing purposes.
        TungstenProperties props = makeProperties();

        // Store properties.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.store(baos);
        baos.close();
        byte[] bytes = baos.toByteArray();

        // Reload and compare.
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        TungstenProperties props2 = new TungstenProperties();
        props2.load(bais);
        bais.close();

        assertEquals("Comparing reloaded properties", props, props2);
    }

    /**
     * Tests ability to make an equivalent properties instance from an existing
     * one.
     */
    public void testCopying1() throws Exception
    {
        // Create a properties object for testing purposes.
        TungstenProperties props = makeProperties();
        TungstenProperties props2 = new TungstenProperties(props.map());
        TungstenProperties props3 = new TungstenProperties(props.hashMap());

        assertEquals("Comparing properties from Map", props, props2);
        assertEquals("Comparing properties from HashMap", props, props3);
    }

    /**
     * Tests ability to copy properties explicitly from one properties instance
     * to another.
     */
    public void testCopying2() throws Exception
    {
        TungstenProperties props = makeProperties();
        TungstenProperties props2 = new TungstenProperties();

        for (String name : props.keyNames())
        {
            props2.setObject(name, props.getString(name));
        }

        assertEquals("Comparing properties from Map", props, props2);
    }

    /**
     * Tests ability to load properties with substitutions from local
     * properties, showing that local substitutions override system property
     * values.
     */
    public void testLoadingWithLocalSubstitutions() throws Exception
    {
        Properties props = new Properties();
        props.setProperty("a", "y");
        props.setProperty("b1", "a${a}");
        props.setProperty("b2", "a$a");

        // Set two System properties.
        System.setProperty("a", "x");

        // Make substitutions.
        int count = TungstenProperties.substituteSystemValues(props);

        // Check results.
        Assert.assertEquals("substitution count", 1, count);
        Assert.assertEquals("y", props.getProperty("a"));
        Assert.assertEquals("ay", props.getProperty("b1"));
        Assert.assertEquals("a$a", props.getProperty("b2"));
    }

    /**
     * Tests ability to load properties with substitutions from System
     * properties.
     */
    public void testLoadingWithSubstitutions() throws Exception
    {
        Properties props = new Properties();
        props.setProperty("a", "a");
        props.setProperty("b1", "a$");
        props.setProperty("b2", "a$a");
        props.setProperty("c1", "a${");
        props.setProperty("c2", "a${a");
        props.setProperty("d1", "a${}");
        props.setProperty("d2", "a${e");
        props.setProperty("e1", "_${e1sys}_");
        props.setProperty("e2", "${test.test}");
        props.setProperty("e3", "${novalue}");
        props.setProperty("e4", "${no value}");

        // Set two System properties.
        System.setProperty("e1sys", "testLoadingWithSubstitutions");
        System.setProperty("test.test", "e2");

        // Make substitutions.
        TungstenProperties.substituteSystemValues(props);

        // Check results.
        Assert.assertEquals("a", props.getProperty("a"));
        Assert.assertEquals("a$", props.getProperty("b1"));
        Assert.assertEquals("a$a", props.getProperty("b2"));
        Assert.assertEquals("a${", props.getProperty("c1"));
        Assert.assertEquals("a${a", props.getProperty("c2"));
        Assert.assertEquals("a${}", props.getProperty("d1"));
        Assert.assertEquals("a${e", props.getProperty("d2"));
        Assert.assertEquals("_testLoadingWithSubstitutions_", props
                .getProperty("e1"));
        Assert.assertEquals("e2", props.getProperty("e2"));
        Assert.assertEquals("${novalue}", props.getProperty("e3"));
        Assert.assertEquals("${no value}", props.getProperty("e4"));
    }

    /**
     * Tests ability to perform multiple substitutions.  
     */
    public void testLoadWithMultipleSubtitutions() throws Exception
    {
        Properties props = new Properties();
        props.setProperty("a", "${b}");
        props.setProperty("b", "${c}");
        props.setProperty("c", "x12");
        props.setProperty("d", "x");

        // Make substitutions.
        int count = TungstenProperties.substituteSystemValues(props, 3);

        // Check results.
        Assert.assertEquals("substitution count", 3, count);
        Assert.assertEquals("x12", props.getProperty("a"));
        Assert.assertEquals("x12", props.getProperty("b"));
        Assert.assertEquals("x12", props.getProperty("c"));
        Assert.assertEquals("x", props.getProperty("d"));
    }

    /**
     * Test subsetting of properties using prefix values.
     * 
     * @throws Exception
     */
    public void testPropertySubsets() throws Exception
    {
        TungstenProperties superSet = new TungstenProperties();
        superSet.setString("a.keep.s1", "string 1");
        superSet.setString("a.remove.s2", "string 2");
        superSet.setString("a.keepString1", "foo");
        superSet.setString("a.string1.keep", "bar");

        TungstenProperties subset0 = superSet.subset("a.keep..", false);
        TungstenProperties subset1 = superSet.subset("a.keep.", false);
        TungstenProperties subset2 = superSet.subset("a.keep.", true);
        TungstenProperties subset3 = superSet.subset("a.", true);

        // First subset should have no entries.
        Assert.assertEquals("subset 0 has no properties", 0, subset0.size());

        // Second subset should have one entry with prefixed name.
        Assert.assertEquals("subset 1 has 1 property", 1, subset1.size());
        Assert.assertEquals("subset 1 key untruncated", "string 1", subset1
                .getString("a.keep.s1"));

        // Third subset should have one entry with unprefixed name.
        Assert.assertEquals("subset 2 has 1 property", 1, subset2.size());
        Assert.assertEquals("subset 2 key truncated", "string 1", subset2
                .getString("s1"));

        // Last subset should have all entries with truncated prefixes.
        Assert.assertEquals("subset 3 has 4 properties", 4, subset3.size());
        Assert.assertEquals("subset 3 key truncated", "string 1", subset3
                .getString("keep.s1"));

        // Test property removal flag
        subset3.subset("remove", true, true);
        assertEquals("subset 3 has 3 properties left", 3, subset3.size());
        // Empty prefix should remove everything
        subset3.subset("", false, true);
        assertTrue("subset 3 should be empty", subset3.isEmpty());
    }

    /**
     * Test parsing and returning comma-separated string lists. Whitespace also
     * works as a separator.
     */
    public void testStringLists() throws Exception
    {
        TungstenProperties tp = new TungstenProperties();
        tp.setString("commasonly", ",");
        tp.setString("nocommas", "b");
        tp.setString("whitespace", "a b");
        tp.setString("nicelist", "a, b,c, ");
        tp.setString("nicelist2", " b,  c,d,e");

        List<String> commasOnly = tp.getStringList("commasonly");
        List<String> noCommas = tp.getStringList("nocommas");
        List<String> whiteSpace = tp.getStringList("whitespace");
        List<String> niceList = tp.getStringList("nicelist");
        List<String> niceList2 = tp.getStringList("nicelist2");

        Assert.assertEquals("Empty list", 0, commasOnly.size());
        Assert.assertEquals("1 item", 1, noCommas.size());
        Assert.assertEquals("2 items", 2, whiteSpace.size());
        Assert.assertEquals("3 items w/ trailing comma", 3, niceList.size());
        Assert.assertEquals("3 items w/ trailing command ends w/ c", "c",
                niceList.get(2));
        Assert.assertEquals("4 items w/ preceding space", 4, niceList2.size());
        Assert.assertEquals("4 items w/ preceding space starts w/ b", "b",
                niceList2.get(0));
    }

    /**
     * Test setting and returning String values from List instances.
     */
    public void testStringLists2() throws Exception
    {
        TungstenProperties tp = new TungstenProperties();

        // Set and retrieve an ordinary list.
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        tp.setStringList("mykey", list);

        List<String> list2 = tp.getStringList("mykey");
        Assert.assertEquals("populated list size", 3, list2.size());
        Assert.assertEquals(list2.get(0), "a");
        Assert.assertEquals(list2.get(1), "b");
        Assert.assertEquals(list2.get(2), "c");

        // Set and retrieve an empty list.
        List<String> empty = new ArrayList<String>();
        tp.setStringList("empty", empty);
        List<String> empty2 = new ArrayList<String>();
        Assert.assertEquals("empty list size", 0, empty2.size());
    }

    public void testDataSource() throws Exception
    {
        TungstenProperties tp = new TungstenProperties();
        Map<String, TungstenProperties> map = new HashMap<String, TungstenProperties>();
        TungstenProperties tp1 = new TungstenProperties();
        tp1.put("id1", "value1");
        tp1.put("id2", "value2");
        TungstenProperties tp2 = new TungstenProperties();
        map.put("mid1", tp1);
        map.put("mid2", tp2);
        tp.setDataSourceMap(map);
        assertEquals("Did not get inserted data source map", map, tp
                .getDataSourceMap());
    }

    public void testClusterMap() throws Exception
    {
        TungstenProperties tp = new TungstenProperties();
        Map<String, Map<String, TungstenProperties>> clusterMap = new HashMap<String, Map<String, TungstenProperties>>();

        Map<String, TungstenProperties> map = new HashMap<String, TungstenProperties>();
        TungstenProperties tp1 = new TungstenProperties();
        tp1.put("id1", "value1");
        tp1.put("id2", "value2");
        TungstenProperties tp2 = new TungstenProperties();
        map.put("m1id1", tp1);
        map.put("m1id2", tp2);
        clusterMap.put("service1", map);

        Map<String, TungstenProperties> map2 = new HashMap<String, TungstenProperties>();
        TungstenProperties tp3 = new TungstenProperties();
        TungstenProperties tp4 = new TungstenProperties();
        map2.put("m2id1", tp3);
        map2.put("m2id2", tp4);
        clusterMap.put("service2", map2);

        tp.setClusterMap(clusterMap);
        assertEquals("Did not get inserted cluster map", clusterMap, tp
                .getClusterMap());
    }

    /**
     * Test that property application works correctly by assigning values to a
     * test instance and ensuring all values are correctly set.
     */
    public void testPropertyApplication() throws Exception
    {
        TungstenProperties tp = new TungstenProperties();
        tp.setString("string", "s1");
        tp.setInt("my_int", -2);
        tp.setInt("MyLong", 25);
        tp.setString("My_float", "1.0"); // No rounding errors
        tp.setString("myDouble", "-1.0");
        tp.setBoolean("My_Boolean", true);
        tp.setString("myChar", "a");
        Date now = new Date();
        tp.setDate("my_Date", now);
        // test with setObject(). Another test with setString() can be found in
        // testPropertyApplicationWithDots
        BigDecimal bd = new BigDecimal("1e+500");
        tp.setObject("myBigDecimal", bd);
        SampleObject.SampleEnum e = SampleObject.SampleEnum.THREE;
        tp.setObject("myEnum", e);
        List<String> strl = Arrays.asList("strle1", "strle2", "strle3");
        tp.setStringList("myStringList", strl);

        SampleObject so = new SampleObject();
        tp.applyProperties(so);

        Assert.assertEquals("String value", "s1", so.getString());
        Assert.assertEquals("Int value", -2, so.getMyInt());
        Assert.assertEquals("Long value", 25, so.getMyLong());
        Assert.assertTrue("Float value", (1.0 == so.getMyFloat()));
        Assert.assertEquals("Double value", -1.0, so.getMyDouble());
        Assert.assertEquals("Boolean value", true, so.isMyBoolean());
        Assert.assertEquals("Char value", 'a', so.getMyChar());
        Assert.assertEquals("Date value", now, so.getMyDate());
        Assert.assertEquals("BigDecimal value", bd, so.getMyBigDecimal());
        Assert.assertEquals("Enum value", e, so.getMyEnum());
        Assert.assertEquals("String list value", strl, so.getMyStringList());
    }

    /**
     * Same as {@link #testPropertyApplication()} using java property-style dots
     * and extreme values
     */
    public void testPropertyApplicationWithDots() throws Exception
    {
        TungstenProperties tp = new TungstenProperties();
        tp.setInt("my.int", Integer.MAX_VALUE);
        tp.setInt("My.Long", Integer.MIN_VALUE);
        tp.setString("My.float", Float.toString(Float.MAX_VALUE));
        tp.setString("my.Double", Double.toString(Double.MIN_VALUE));
        tp.setBoolean("My.Boolean", false);
        tp.setString("my.Char", "a");
        Date now = new Date();
        tp.setDate("my.date", now);
        BigDecimal bd = new BigDecimal("-2e+1000");
        tp.setString("my.BigDecimal", bd.toString());
        SampleObject.SampleEnum e = SampleObject.SampleEnum.TWO;
        tp.setString("my.enum", e.toString());

        SampleObject so = new SampleObject();
        tp.applyProperties(so);

        Assert.assertEquals("Int value", Integer.MAX_VALUE, so.getMyInt());
        Assert.assertEquals("Long value", Integer.MIN_VALUE, so.getMyLong());
        Assert.assertEquals("Float value", Float.MAX_VALUE, so.getMyFloat());
        Assert.assertEquals("Double value", Double.MIN_VALUE, so.getMyDouble());
        Assert.assertEquals("Boolean value", false, so.isMyBoolean());
        Assert.assertEquals("Char value", 'a', so.getMyChar());
        Assert.assertEquals("Date value", now, so.getMyDate());
        Assert.assertEquals("BigDecimal value", bd, so.getMyBigDecimal());
        Assert.assertEquals("Enum value", e, so.getMyEnum());
    }

    /**
     * Ensure clear() and isEmpty() functions work using both empty and
     * non-empty properties instances.
     */
    public void testPropertyClearing()
    {
        TungstenProperties tp1 = new TungstenProperties();
        TungstenProperties tp2 = makeProperties();

        Assert.assertTrue("New properties are empty", tp1.isEmpty());
        tp1.clear();
        Assert.assertTrue("Cleared new properties are empty", tp1.isEmpty());

        Assert.assertFalse("Properties with values are not empty", tp2
                .isEmpty());
        tp2.clear();
        Assert.assertTrue("Cleared new properties are empty", tp1.isEmpty());
    }

    /**
     * Ensure that putAll() allows properties to be merged with added properties
     * taking priority over existing ones.
     */
    public void testPutAll()
    {
        TungstenProperties tp1 = new TungstenProperties();
        TungstenProperties tp2 = new TungstenProperties();
        tp2.setString("a", "a");
        tp2.setString("b", "b");
        TungstenProperties tp3 = new TungstenProperties();
        tp3.setString("b", "B");
        tp3.setString("c", "c");

        tp2.putAll(tp1);
        Assert.assertEquals("No change in size", 2, tp2.size());
        Assert.assertEquals("b is b", "b", tp2.getString("b"));

        tp2.putAll(tp3);
        Assert.assertEquals("Added 1", 3, tp2.size());
        Assert.assertEquals("b is B", "B", tp2.getString("b"));
    }

    public void testPutAllWithPrefix()
    {
        TungstenProperties tp1 = new TungstenProperties();
        tp1.setInt("a", 1);
        tp1.setInt("b.b", 1);
        TungstenProperties tp2 = new TungstenProperties();
        tp2.setString("a", "tp2's a");
        tp2.setInt("b", 2);

        int oldTp1Size = tp1.size();
        tp1.putAllWithPrefix(tp2, "b.");
        Assert.assertEquals("Added 1 property", oldTp1Size + 1, tp1.size());
        Assert.assertEquals("a is 1", 1, tp1.getInt("a"));
        Assert.assertEquals("tp2's a has become a.a", "tp2's a", tp1
                .getString("b.a"));
        Assert.assertEquals("tp1's b.b has become tp2's b.b", tp2.getInt("b"),
                tp1.getInt("b.b"));

        assertEquals("tp1 subset is tp2", tp2, tp1.subset("b.", true));
    }

    /**
     * Ensure that trim() operation trims whitespace without causing exceptions
     * if nulls somehow get in.
     */
    public void testTrim()
    {
        TungstenProperties tp = new TungstenProperties();
        tp.setString("a", " a");
        tp.setString("b", "b ");
        tp.setString("c", " c c ");
        tp.setString("null", null);

        tp.trim();

        assertEquals("Left trim", "a", tp.getString("a"));
        assertEquals("Right trim", "b", tp.getString("b"));
        assertEquals("Center space", "c c", tp.getString("c"));
        assertNull("Null value", tp.getString("null"));
    }

    /**
     * Ensure that removing a key from the properties makes the value
     * unavailable and decreases the size of the properties instance.
     */
    public void testRemove()
    {
        TungstenProperties tp = new TungstenProperties();
        tp.setString("a", "a");
        tp.setString("b", "b");
        assertEquals("Initial size is 2", 2, tp.size());

        tp.remove("a");
        assertEquals("Remove 1, size is now 1", 1, tp.size());
        assertNull("Value for a no longer exists", tp.getString("a"));
        tp.remove("b");

        assertEquals("Remove 0, size is now 0", 0, tp.size());
        assertEquals("", 0, tp.size());
        assertNull("Value for b no longer exists", tp.getString("b"));
    }

    /**
     * Ensure that Java property files are correctly loaded from a property
     * file. This includes Java conventions that leading whitespace before the
     * property value is consumed, but trailing whitespace is not.
     */
    public void testPropertyFileLoading() throws Exception
    {
        // Create a properties file.
        File propFile = File.createTempFile("propertyLoadTest", ".properties");
        PrintWriter pw = new PrintWriter(propFile);
        pw.println("a=");
        pw.println("a1=   ");
        pw.println("b=foo");
        pw.println("b1=  foo  ");
        pw.close();

        // Read into Tungsten properties instance.
        FileInputStream fis = new FileInputStream(propFile);
        TungstenProperties props = new TungstenProperties();
        props.load(fis);
        fis.close();

        // Check values. These conventions are from Java property files.
        Assert.assertEquals("Empty string set", "", props.get("a"));
        Assert.assertEquals("Blank string set", "", props.get("a1"));
        Assert.assertEquals("Value set", "foo", props.get("b"));
        Assert.assertEquals("Padded value set", "foo  ", props.get("b1"));
        Assert.assertNull("No value set, is null", props.get("c"));
    }

    /**
     * Ensure that we can emit and load properties using param lists of the form
     * key1=value1;key2=value2;key3=value3.
     */
    public void testParamListLoading() throws Exception
    {
        TungstenProperties props = new TungstenProperties();
        props.load("key1= value1;key2 =4;key3= true", false);

        Assert.assertEquals("key1 is set", "value1", props.getString("key1"));
        Assert.assertEquals("key2 is set", 4, props.getLong("key2"));
        Assert.assertEquals("key3 is set", true, props.getBoolean("key3"));

        TungstenProperties props2 = new TungstenProperties();
        props2.load(props.toNameValuePairs(), true);
        Assert.assertEquals("Emitted properties load to equivalent instance",
                props, props2);
    }

    // Make a basic Tungsten properties instance.
    public TungstenProperties makeProperties()
    {
        TungstenProperties props = new TungstenProperties();

        props.setString("string", "a string");
        props.setInt("int", 99);
        props.setBoolean("boolean1", true);
        props.setBoolean("boolean2", false);
        props.setFile("file", new File("/etc/init.d"));

        return props;
    }

    public void testSendReceive() throws IOException
    {
        // Test with empty properties
        TungstenProperties props = new TungstenProperties();
        sendRecvAndCompareProps(props);
        // do another run with filled in properties
        props = makeProperties();
        sendRecvAndCompareProps(props);
    }

    private void sendRecvAndCompareProps(TungstenProperties props)
            throws IOException, FileNotFoundException
    {
        File propFile = File.createTempFile("propertySendReceiveTest",
                ".properties");
        PrintWriter pw = new PrintWriter(propFile);
        props.send(pw);
        pw.flush();

        // Read into Tungsten properties instance.
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(propFile)));
        TungstenProperties props2 = TungstenProperties.createFromStream(br);
        // Check values. These conventions are from Java property files.
        Assert.assertEquals("Received properties differ from sent ones", props,
                props2);
        br.close();

        // Negative test: make sure we get an exception if:
        // 1/ nothing is send
        pw = new PrintWriter(propFile);
        pw.flush();
        try
        {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    propFile)));
            TungstenProperties.createFromStream(br);
            fail("Did not get an exception when receiving incomplete properties");
        }
        catch (IOException expected)
        {
            assertEquals(
                    "Cannot create properties from stream: reached end of stream before end of properties tag",
                    expected.getLocalizedMessage());

        }
        // 2/ the end of stream is reached before the properties are fully
        // received (at different steps
        pw.println("testKey");
        pw.flush();
        try
        {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    propFile)));
            TungstenProperties.createFromStream(br);
            fail("Did not get an exception when receiving incomplete properties");
        }
        catch (IOException expected)
        {
            assertEquals(
                    "Cannot create properties from stream: reached end of stream before end of properties tag",
                    expected.getLocalizedMessage());

        }
        pw.println("java.lang.String");
        pw.flush();
        try
        {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    propFile)));
            TungstenProperties.createFromStream(br);
            fail("Did not get an exception when receiving incomplete properties");
        }
        catch (IOException expected)
        {
            assertEquals(
                    "Cannot create properties from stream: reached end of stream before end of properties tag",
                    expected.getLocalizedMessage());
        }
        pw.println("testVal");
        pw.flush();
        try
        {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    propFile)));
            TungstenProperties.createFromStream(br);
            fail("Did not get an exception when receiving incomplete properties");
        }
        catch (IOException expected)
        {
            assertEquals(
                    "Cannot create properties from stream: reached end of stream before end of properties tag",
                    expected.getLocalizedMessage());
        }
        pw.println(TungstenProperties.ENDOFLINE_TAG);
        pw.flush();
        try
        {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    propFile)));
            TungstenProperties.createFromStream(br);
            fail("Did not get an exception when receiving incomplete properties");
        }
        catch (IOException expected)
        {
            assertEquals(
                    "Cannot create properties from stream: reached end of stream before end of properties tag",
                    expected.getLocalizedMessage());
        }
        pw.println(TungstenProperties.ENDOFPROPERTIES_TAG);
        pw.flush();
        br = new BufferedReader(new InputStreamReader(new FileInputStream(
                propFile)));
        TungstenProperties valid = TungstenProperties.createFromStream(br);
        assertEquals("Didn't get expected testval String value in properties",
                "testVal\n", valid.getString("testKey"));

        pw.close();
        br.close();
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

