/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)STCTypeConverter.java 
 *
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

package com.sun.stc.eways.util;

import java.math.BigDecimal;

import java.io.UnsupportedEncodingException;

import java.sql.Time;
import java.sql.Date;
import java.sql.Timestamp;

/**
 *
 * This is a utility class that eases data type conversion.<p>
 *
 * These methods are used by the Collaboration Editor when a field of one type is copied to a field of a different type.
 *
 * @author Scott Steadman (ssteadman@seebeyond.com or ss@stdmn.com)
 */
public class STCTypeConverter {

  /**
   * the exception message if the parameter passed to a method is null.
   */
  public static final String EX_NULL_PARAM = "Parameter cannot be null.";

  /**
   * the exception message if the parameter contains an invalid value.
   */
  public static final String EX_INVALID_VALUE = "Parameter contains an invalid value: ";

  /**
   * the exception message if the parameter is an empty String
   */
  public static final String EX_EMPTY_STRING = "Parameter contains an empty string.";

  /**
   * the exception message if the parameter is out of range for a conversion
   */
  public static final String EX_OUT_OF_RANGE = "Parameter is out of range: ";

  /**
   * the character used to represent the boolean TRUE
   */
  public static final char TRUE_CHAR = 't';

  /**
   * the character used to represent the boolean FALSE
   */
  public static final char FALSE_CHAR = 'f';

  /**
   * the string used to represent the boolean TRUE
   */
  public static final String TRUE_STRING = "true";

  /**
   * the string used to represent the boolean FALSE
   */
  public static final String FALSE_STRING = "false";

  /**
   * constants used to check ranges for conversions
   */
  private static final double MAX_BYTE = (double)Byte.MAX_VALUE;
  private static final double MIN_BYTE = (double)Byte.MIN_VALUE;

  private static final double MAX_SHORT = (double)Short.MAX_VALUE;
  private static final double MIN_SHORT = (double)Short.MIN_VALUE;

  private static final double MAX_INT = (double)Integer.MAX_VALUE;
  private static final double MIN_INT = (double)Integer.MIN_VALUE;

  private static final double MAX_LONG = (double)Long.MAX_VALUE;
  private static final double MIN_LONG = (double)Long.MIN_VALUE;

  private static final double MAX_FLOAT = (double)Float.MAX_VALUE;
  private static final double MIN_FLOAT = (double)Float.MIN_VALUE;


  /**
   * hiding the constructor because all methods are static.
   */
  private STCTypeConverter(){}

  /**
  * Converts a Boolean object to a boolean primitive.
  * <p>
  * @param _value The Boolean object. The <code><b>_value</b></code> cannot be null.
  * @return <CODE>boolean</CODE> - The converted boolean primitive.
  * @exception IllegalArgumentException Thrown if the <code>_value</code> is null.
  * @include
  */
  public static final boolean toBooleanPrimitive(Boolean _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.booleanValue();
  }

  /**
  * Converts a byte primitive to a boolean primitive.
  * <p>
  * @param _value The byte primitive.
  * @return <CODE>boolean</CODE> - Returns <code><b>false</code></b> if the <code><b>_value</code></b> is <b>0</b>. Otherwise, returns <code><b>true</code></b>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final boolean toBooleanPrimitive(byte _value) {
    return 0 != _value;
  }

  /**
  * Converts a Byte object to a boolean primitive.
  * <p>
  * @param _value The Byte object.
  * @return <CODE>boolean</CODE> - Returns <code><b>false</code></b> if the <code><b>_value</code></b> is <b>0</b>. Otherwise, returns <code><b>true</code></b>.
  * @exception IllegalArgumentException Thrown if <code><b>_value</code></b> is null.
  * @include
  */
  public static final boolean toBooleanPrimitive(Byte _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBooleanPrimitive(_value.byteValue());
  }

  /**
  * Converts a char primitive to a boolean primitive.
  * <p>
  * @param _value The char primitive. This must be <code><b><a href="#TRUE_CHAR">TRUE_CHAR</a></code></b> or <code><b><a href="#FALSE_CHAR">FALSE_CHAR</a></code></b>.
  *
  * @return <CODE>boolean</CODE> - Returns <code><b>true</code></b> if <code><b>_value</code></b> is <code><b>TRUE_CHAR</code></b> or <code><b>false</code></b> if <code><b>_value</code></b> is <code><b>FALSE_CHAR</code></b>.
  *
  * @exception IllegalArgumentException Thrown if <code><b>_value</code></b> is not <code><b>TRUE_CHAR</code></b> or <code><b>FALSE_CHAR</code></b>.
  *
  * @include
  */
  public static final boolean toBooleanPrimitive(char _value)
    throws IllegalArgumentException
  {
    if(TRUE_CHAR != _value && FALSE_CHAR != _value) {
      throw new IllegalArgumentException(EX_INVALID_VALUE + _value);
    }
    return TRUE_CHAR == _value;
  }

  /**
  * Converts a Character object to a boolean primitive.
  * <p>
  * @param _value The Character object. This must be <code><b><a href="#TRUE_CHAR">TRUE_CHAR</a></code></b> or <code><b><a href="#FALSE_CHAR">FALSE_CHAR</a></code></b>.
  *
  * @return <CODE>boolean</CODE> - Returns <code><b>true</code></b> if <code><b>_value</code></b> is <code><b>TRUE_CHAR</code></b> or <code><b>false</code></b> if <code><b>_value</code></b> is <code><b>FALSE_CHAR</code></b>.
  *
  * @exception IllegalArgumentException Thrown if <code><b>_value</code></b> is not <code><b>TRUE_CHAR</code></b> or <code><b>FALSE_CHAR</code></b>.
  *
  * @include
  */
  public static final boolean toBooleanPrimitive(Character _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBooleanPrimitive(_value.charValue());
  }

  /**
  * Converts a double primitive to a boolean primitive.
  * <p>
  * @param _value The double primitive.
  * @return <CODE>boolean</CODE> - Returns <code><b>false</code></b> if <code><b>_value</code></b> is <code><b>0d</code></b>. Otherwise, returns <code><b>true</code></b>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
    public static final boolean toBooleanPrimitive(double _value) {
    return 0d != _value;
  }

  /**
  * Converts a Double object to a boolean primitive.
  * <p>
  * @param _value The Double object.
  * @return <CODE>boolean</CODE> - Returns <code><b>false</code></b> if <code><b>_value</code></b> is <code><b>0d</code></b>. Otherwise, returns <code><b>true</code></b>.
  * @exception IllegalArgumentException Thrown if <code><b>_value</code></b> is null.
  * @include
  */
  public static final boolean toBooleanPrimitive(Double _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBooleanPrimitive(_value.doubleValue());
  }

  /**
  * Converts a float primitive to a boolean primitive.
  * <p>
  * @param _value The float primitive.
  * @return <CODE>boolean</CODE> - Returns <code><b>false</code></b> if <code><b>_value</code></b> is <code><b>0f</code></b>. Otherwise, returns <code><b>true</code></b>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final boolean toBooleanPrimitive(float _value) {
    return 0f != _value;
  }

  /**
  * Converts a Float object to a boolean primitive.
  * <p>
  * @param _value The Float object.
  * @return <CODE>boolean</CODE> - Returns <code><b>false</code></b> if <code><b>_value</code></b> is <code><b>0f</code></b>. Otherwise, returns <code><b>true</code></b>.
  * @exception IllegalArgumentException Thrown if <code><b>_value</code></b> is null.
  * @include
  */
  public static final boolean toBooleanPrimitive(Float _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBooleanPrimitive(_value.floatValue());
  }

  /**
  * Converts an int primitive to a boolean primitive.
  * <p>
  * @param _value The int primitive.
  * @return <CODE>boolean</CODE> - Returns <code><b>false</code></b> if <code><b>_value</code></b> is <code><b>0</code></b>. Otherwise, returns <code><b>true</code></b>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final boolean toBooleanPrimitive(int _value) {
    return 0 != _value;
  }

  /**
  * Converts an Integer object to a boolean primitive.
  * <p>
  * @param _value The Integer object.
  * @return <CODE>boolean</CODE> - Returns <code><b>false</code></b> if <code><b>_value</code></b> is <code><b>0</code></b>. Otherwise, returns <code><b>true</code></b>.
  * @exception IllegalArgumentException Thrown if <code><b>_value</code></b> is null.
  * @include
  */
  public static final boolean toBooleanPrimitive(Integer _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBooleanPrimitive(_value.intValue());
  }

  /**
  * Converts a long primitive to a boolean primitive.
  * <p>
  * @param _value The long primitive.
  * @return <CODE>boolean</CODE> - Returns <code><b>false</code></b> if <code><b>_value</code></b> is <code><b>0</code></b>. Otherwise, returns <code><b>true</code></b>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final boolean toBooleanPrimitive(long _value) {
    return 0L != _value;
  }

  /**
  * Converts a Long object to a boolean primitive.
  * <p>
  * @param _value The Long object.
  * @return <CODE>boolean</CODE> - Returns <code><b>false</code></b> if <code><b>_value</code></b> is <code><b>0</code></b>. Otherwise, returns <code><b>true</code></b>.
  * @exception IllegalArgumentException Thrown if <code><b>_value</code></b> is null.
  * @include
  */
  public static final boolean toBooleanPrimitive(Long _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBooleanPrimitive(_value.longValue());
  }

  /**
  * Converts a short primitive to a boolean primitive.
  * <p>
  * @param _value The short primitive.
  * @return <CODE>boolean</CODE> - Returns <code><b>false</code></b> if <code><b>_value</code></b> is <code><b>0</code></b>. Otherwise, returns <code><b>true</code></b>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final boolean toBooleanPrimitive(short _value) {
    return 0 != _value;
  }

  /**
  * Converts a Short object to a boolean primitive.
  * <p>
  * @param _value The Short object.
  *
  * @return <CODE>boolean</CODE> - Returns <code><b>false</code></b> if <code><b>_value</code></b> is <code><b>0</code></b>. Otherwise, returns <code><b>true</code></b>.
  *
  * @exception IllegalArgumentException Thrown if <code><b>_value</code></b> is null.
  * @include
  */
  public static final boolean toBooleanPrimitive(Short _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBooleanPrimitive(_value.shortValue());
  }

  /**
  * Converts a String object to a boolean primitive.
  * <p>
  * @param _value The String object.
  *
  * @return <CODE>boolean</CODE> - Returns <code><b>true</code></b> if <code><b>_value</code></b> is <code><b>"true"</code></b>. Otherwise, returns <code><b>false</code></b>.
  *
  * @exception IllegalArgumentException Thrown if <code><b>_value</b></code> is null or if <code><b>_value</b></code> is not <code><b><a href="#TRUE_STRING">TRUE_STRING</a></code></b> or <code><b><a href="#FALSE_STRING">FALSE_STRING</a></code></b>.
  *
  * @include
  */
  public static final boolean toBooleanPrimitive(String _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    if(!TRUE_STRING.equals(_value) && !FALSE_STRING.equals(_value)) {
      throw new IllegalArgumentException(EX_INVALID_VALUE + _value);
    }
    return TRUE_STRING.equals(_value);
  }

  /**
  * Converts a boolean primitive to a Boolean object.
  * <p>
  * @param _value The boolean primitive.
  * @return <CODE>Boolean</CODE> - Returns <code><b>Boolean.TRUE</code></b> if <code><b>_value</code></b> is <b>true</b>. Returns <code><b>Boolean.FALSE</code></b> if <code><b>_value</code></b> is <b>false</b>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Boolean toBoolean(boolean _value) {
    return _value?Boolean.TRUE:Boolean.FALSE;
  }

  /**
  * Converts a byte primitive to a Boolean object.
  * <p>
  * @param _value The byte primitive.
  * @return <CODE>Boolean</CODE> - Returns <code><b>Boolean.FALSE</code></b> if <code><b>_value</code></b> is <b>0</b>. Otherwise, returns <code><b>Boolean.TRUE</code></b>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Boolean toBoolean(byte _value) {
    return (0 != _value)?Boolean.TRUE:Boolean.FALSE;
  }

  /**
  * Converts a Byte object to a Boolean object.
  * <p>
  * @param _value The Byte object.
  * @return <CODE>Boolean</CODE> - Returns <code><b>Boolean.FALSE</b></code> if <code><b>_value</b></code> is <b>0</b>. Otherwise, returns <code><b>Boolean.TRUE</b></code>.
  * @exception IllegalArgumentException Thrown if <code><b>_value</code></b> is null.
  * @include
  */
  public static final Boolean toBoolean(Byte _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBoolean(_value.byteValue());
  }

  /**
  * Converts a char primitive to a Boolean object.
  * <p>
  *
  * @param _value The char primitive. This must be <href a=#TRUE_CHAR><code><b>TRUE_CHAR</code></b></a> or <href a=#FALSE_CHAR><code><b>FALSE_CHAR</b></code></a>.
  *
  * @return <CODE>Boolean</CODE> - Returns <code><b>Boolean.TRUE</code></b> if <code><b>_value</code></b> is <code><b>TRUE_CHAR</code></b> or <code><b>Boolean.FALSE</code></b> if <code><b>_value</code></b> is <code><b>FALSE_CHAR</code></b>.
  *
  * @exception IllegalArgumentException Thrown if <code><b>_value</b></code> is not <code><b>TRUE_CHAR</b></code> or <code><b>FALSE_CHAR</b></code>.
  * @include
  */
  public static final Boolean toBoolean(char _value)
    throws IllegalArgumentException
  {
    if(TRUE_CHAR != _value && FALSE_CHAR != _value) {
      throw new IllegalArgumentException(EX_INVALID_VALUE + _value);
    }
    return (TRUE_CHAR == _value)?Boolean.TRUE:Boolean.FALSE;
  }

  /**
  * Converts a Character object to a Boolean object.
  * <p>
  * @param _value The Character object. This must be <href a=#TRUE_CHAR><code><b>TRUE_CHAR</code></b></a> or <href a=#FALSE_CHAR><code><b>FALSE_CHAR</b></code></a>.
  *
  * @return <CODE>Boolean</CODE> - Returns <code><b>Boolean.TRUE</code></b> if <code><b>_value</code></b> is <code><b>TRUE_CHAR</code></b> or <code><b>Boolean.FALSE</code></b> if <code><b>_value</code></b> is <code><b>FALSE_CHAR</code></b>.
  *
  * @exception IllegalArgumentException Thrown if <code><b>_value</b></code> is not null or is not <code><b>TRUE_CHAR</b></code> or <code><b>FALSE_CHAR</b></code>.
  *
  * @include
  */
  public static final Boolean toBoolean(Character _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBoolean(_value.charValue());
  }

  /**
  * Converts a double primitive to a Boolean object.
  * <p>
  * @param _value The double primitive.
  *
  * @return <CODE>Boolean</CODE> - Returns <code><b>Boolean.FALSE</code></b> if <code><b>_value</code></b> is <code><b>0d</code></b>. Otherwise, returns <code><b>Boolean.TRUE</code></b>.
  *
  * @include
  */
  public static final Boolean toBoolean(double _value) {
    return (0d != _value)?Boolean.TRUE:Boolean.FALSE;
  }

  /**
  * Converts a Double object to a Boolean object.
  * <p>
  * @param _value The Double object.
  *
  * @return <CODE>Boolean</CODE> - Returns <code><b>Boolean.FALSE</code></b> if <code><b>_value</code></b> is <code><b>0d</code></b>. Otherwise, returns <code><b>Boolean.TRUE</code></b>.
  *
  * @exception IllegalArgumentException Thrown if <code><b>_value</b></code> is null.
  * @include
  */
  public static final Boolean toBoolean(Double _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBoolean(_value.doubleValue());
  }

  /**
  * Converts a float primitive to a Boolean object.
  * <p>
  * @param _value The float primitive.
  *
  * @return <CODE>Boolean</CODE> - Returns <code><b>Boolean.FALSE</code></b> if <code><b>_value</code></b> is <code><b>0f</code></b>. Otherwise, returns <code><b>Boolean.TRUE</code></b>.
  *
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Boolean toBoolean(float _value) {
    return (0f != _value)?Boolean.TRUE:Boolean.FALSE;
  }

  /**
  * Converts a Float object to a Boolean object.
  * <p>
  * @param _value The Float object.
  *
  * @return <CODE>Boolean</CODE> - Returns <code><b>Boolean.FALSE</code></b> if <code><b>_value</code></b> is <code><b>0f</code></b>. Otherwise, returns <code><b>Boolean.TRUE</code></b>.
  *
  * @exception IllegalArgumentException Thrown if <code><b>_value</code></b> is null.
  * @include
  */
  public static final Boolean toBoolean(Float _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBoolean(_value.floatValue());
  }

  /**
  * Converts an int primitive to a Boolean object.
  * <p>
  * @param _value The int primitive.
  *
  * @return <CODE>Boolean</CODE> - Returns <code><b>Boolean.FALSE</code></b> if <code><b>_value</code></b> is <code><b>0</code></b>. Otherwise, returns <code><b>Boolean.TRUE</code></b>.
  *
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Boolean toBoolean(int _value) {
    return (0 != _value)?Boolean.TRUE:Boolean.FALSE;
  }

  /**
  * Converts an Integer object to a Boolean object.
  * <p>
  * @param _value The Integer object.
  *
  * @return <CODE>Boolean</CODE> - Returns <code><b>Boolean.FALSE</code></b> if <code><b>_value</code></b> is <code><b>0</code></b>. Otherwise, returns <code><b>Boolean.TRUE</code></b>.
  *
  * @exception IllegalArgumentException Thrown if <code><b>_value</code></b> is null.
  * @include
  */
  public static final Boolean toBoolean(Integer _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBoolean(_value.intValue());
  }

  /**
  * Converts a long primitive to a Boolean object.
  * <p>
  * @param _value The long primitive.
  *
  * @return <CODE>Boolean</CODE> - Returns <code><b>Boolean.FALSE</code></b> if <code><b>_value</code></b> is <code><b>0L</code></b>. Otherwise, returns <code><b>Boolean.TRUE</code></b>.
  *
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Boolean toBoolean(long _value) {
    return (0L != _value)?Boolean.TRUE:Boolean.FALSE;
  }

  /**
  * Converts a Long object to a Boolean object.
  * <p>
  * @param _value The Long object.
  *
  * @return <CODE>Boolean</CODE> - Returns <code><b>Boolean.FALSE</code></b> if <code><b>_value</code></b> is <code><b>0L</code></b>. Otherwise, returns <code><b>Boolean.TRUE</code></b>.
  *
  * @exception IllegalArgumentException Thrown if <code><b>_value</code></b> is null.
  * @include
  */
  public static final Boolean toBoolean(Long _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBoolean(_value.longValue());
  }

  /**
  * Converts a short primitive to a Boolean object.
  * <p>
  * @param _value Description of the parameter.

    or

  * <DL><DT><B>Parameters:</B><DD>None.</DL>

  * @return <CODE>Boolean</CODE> - Returns <code><b>Boolean.FALSE</code></b> if <code><b>_value</code></b> is <code><b>0</code></b>. Otherwise, returns <code><b>Boolean.TRUE</code></b>.
  *
  * @include
  */
  public static final Boolean toBoolean(short _value) {
    return (0 != _value)?Boolean.TRUE:Boolean.FALSE;
  }

  /**
  * Converts a Short object to a Boolean object.
  * <p>
  * @param _value The Short object.
  *
  * @return <CODE>Boolean</CODE> - Returns <code><b>Boolean.FALSE</code></b> if <code><b>_value</code></b> is <code><b>0</code></b>. Otherwise, returns <code><b>Boolean.TRUE</code></b>.
  *
  * @exception IllegalArgumentException Thrown if <code><b>_value</code></b> is null.
  * @include
  */
  public static final Boolean toBoolean(Short _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBoolean(_value.shortValue());
  }

  /**
  * Converts a String object to a Boolean object.
  * <p>
  * @param _value The String object.
  *
  * @return <CODE>Boolean</CODE> - Returns <code><b>Boolean.TRUE</code></b> if <code><b>_value</code></b> is <href a=#TRUE_STRING><code><b>TRUE_STRING</code></b></a>. Otherwise, returns <href a=#FALSE_STRING><code><b>Boolean.TRUE</code></b></a>.
  *
  * @exception IllegalArgumentException Thrown if <code><b>_value</code></b> is null or is not <code><b>TRUE_STRING</code></b> or <code><b>FALSE_STRING</b></code>.
  * @include
  */
  public static final Boolean toBoolean(String _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    if(!TRUE_STRING.equals(_value) && !FALSE_STRING.equals(_value)) {
      throw new IllegalArgumentException(EX_INVALID_VALUE + _value);
    }
    return TRUE_STRING.equals(_value)?Boolean.TRUE:Boolean.FALSE;
  }

  /**
  * Converts a boolean primitive to a byte primitive.
  * <p>
  * @param _value The boolean primitive.
  * @return <CODE>byte</CODE> - Returns <code><b>0</code></b> if the <code><b>_value</b></code> is false or <code><b>1</code></b> if <code><b>_value</b></code> is true.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final byte toBytePrimitive(boolean _value) {
    return (byte)(_value?1:0);
  }

  /**
  * Converts a Boolean object to a byte primitive.
  * <p>
  * @param _value The Boolean object.
  * @return <CODE>byte</CODE> - The byte primitive.
  * @exception IllegalArgumentException Thrown if <code><b>_value</b></code> is null.
  * @include
  */
  public static final byte toBytePrimitive(Boolean _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBytePrimitive(_value.booleanValue());
  }

  /**
  * Converts a byte array to a byte primitive. The array can only have one element in it.
  * <p>
  * @param _value The byte array.
  * @return <CODE>byte</CODE> - The byte primitive.
  * @exception IllegalArgumentException Thrown if <code><b>_value</b></code> is null or contains more than or less than one element.
  * @include
  */
  public static final byte toBytePrimitive(byte[] _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    if(1 != _value.length) {
      throw new IllegalArgumentException("_value must be 1 byte.");
    }
    return _value[0];
  }

  /**
  * Converts a Byte object to a byte primitive.
  * <p>
  * @param _value The Byte object.
  * @return <CODE>byte</CODE> - The byte primitive.
  * <DT><B>Throws:</B><DD>None.
  * @see Byte#byteValue()
  * @include
  */
  public static final byte toBytePrimitive(Byte _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.byteValue();
  }

  /**
  * Converts a char primitive to a byte primitive.
  * <p>
  * @param _value The char primitive.
  * @return <CODE>byte</CODE> - The ASCII code of the character.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final byte toBytePrimitive(char _value) {
    return (byte)(_value & 0xFF);
  }

  /**
  * Converts a Character object to a byte primitive.
  * <p>
  * @param _value The Character object.
  * @return <CODE>byte</CODE> - The ASCII code of the character.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final byte toBytePrimitive(Character _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBytePrimitive(_value.charValue());
  }

  /**
  * Converts a double primitive to a byte primitive.
  * <p>
  * @param _value The double primitive.
  * @return <CODE>byte</CODE> - The byte primitive.
  * @exception IllegalArgumentException Thrown if <code><b>_value</b></code> is too large to fit in a byte.
  * @include
  */
  public static final byte toBytePrimitive(double _value) {
    if(Double.POSITIVE_INFINITY == _value
      || Double.NEGATIVE_INFINITY == _value
      || Double.NaN == _value
      || _value > STCTypeConverter.MAX_BYTE
      || _value < STCTypeConverter.MIN_BYTE
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return (byte)_value;
  }

  /**
  * Converts a Double object to a byte primitive.
  * <p>
  * @param _value The Double object.
  * @return <CODE>byte</CODE> - The byte primitive.
  * @exception IllegalArgumentException Thrown if <code><b>_value</b></code> is null.
  * @include
  */
  public static final byte toBytePrimitive(Double _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBytePrimitive(_value.doubleValue());
  }

  /**
  * Converts a float primitive to a byte primitive.
  * <p>
  * @param _value The float primitive.
  * @return <CODE>byte</CODE> - The byte primitive.
  * @exception IllegalArgumentException Thrown if <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final byte toBytePrimitive(float _value) {
    if(Float.POSITIVE_INFINITY == _value
      || Float.NEGATIVE_INFINITY == _value
      || Float.NaN == _value
      || _value > STCTypeConverter.MAX_BYTE
      || _value < STCTypeConverter.MIN_BYTE
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return (byte)_value;
  }

  /**
  * Converts a Float object to a byte primitive.
  * <p>
  * @param _value The Float object.
  * @return <CODE>byte</CODE> - The byte primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final byte toBytePrimitive(Float _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBytePrimitive(_value.floatValue());
  }

  /**
  * Converts an int primitive to a byte primitive.
  * <p>
  * @param _value The int primitive.
  * @return <CODE>byte</CODE> - The byte primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final byte toBytePrimitive(int _value) {
    if(_value > STCTypeConverter.MAX_BYTE
      || _value < STCTypeConverter.MIN_BYTE
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return (byte)_value;
  }

  /**
  * Converts an Integer object to a byte primitive.
  * <p>
  * @param _value The Integer object.
  * @return <CODE>byte</CODE> - The byte primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final byte toBytePrimitive(Integer _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBytePrimitive(_value.intValue());
  }

  /**
  * Converts a long primitive to a byte primitive.
  * <p>
  * @param _value The long primitive.
  * @return <CODE>byte</CODE> - The byte primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final byte toBytePrimitive(long _value) {
    if(_value > STCTypeConverter.MAX_BYTE
      || _value < STCTypeConverter.MIN_BYTE
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return (byte)_value;
  }

  /**
  * Converts a Long object to a byte primitive.
  * <p>
  * @param _value The Long object.
  * @return <CODE>byte</CODE> - The byte primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final byte toBytePrimitive(Long _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBytePrimitive(_value.longValue());
  }

  /**
  * Converts a short primitive to a byte primitive.
  * <p>
  * @param _value The short primitive.
  * @return <CODE>byte</CODE> - The byte primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final byte toBytePrimitive(short _value) {
    if(_value > STCTypeConverter.MAX_BYTE
      || _value < STCTypeConverter.MIN_BYTE
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return (byte)_value;
  }

  /**
  * Converts a Short object to a byte primitive.
  * <p>
  * @param _value The Short object.
  * @return <CODE>byte</CODE> - The byte primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final byte toBytePrimitive(Short _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBytePrimitive(_value.shortValue());
  }

  /**
  * Converts a BigDecimal object to a byte primitive.
  * <p>
  * @param _value The BigDecimal object.
  * @return <CODE>byte</CODE> - The byte primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final byte toBytePrimitive(BigDecimal _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toBytePrimitive(_value.doubleValue());
  }

  /**
  * Converts a String object to a byte primitive.
  * <p>
  * @param _value The String object.
  * @return <CODE>byte</CODE> - The byte primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @see Byte#parseByte(String)
  * @include
  */
  public static final byte toBytePrimitive(String _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return Byte.parseByte(_value);
  }

  /**
  * Converts a boolean primitive to a Byte object.
  * <p>
  * @param _value The boolean primitive.
  * @return <CODE>Byte</CODE> - The Byte object.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Byte toByte(boolean _value) {
    return new Byte(toBytePrimitive(_value));
  }

  /**
  * Converts a Boolean object to a Byte object.
  * <p>
  * @param _value The Boolean object.
  * @return <CODE>Byte</CODE> - Returns <code><b>1</b></code> if the <code><b>_value</b></code> is true or <code><b>0</b></code> if the <code><b>_value</b></code> is false.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Byte toByte(Boolean _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toByte(_value.booleanValue());
  }

  /**
  * Converts a byte primitive to a Byte object.
  * <p>
  * @param _value The byte primitive.
  * @return <CODE>Byte</CODE> - Returns the Byte object.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Byte toByte(byte _value) {
    return new Byte(_value);
  }

  /**
  * Converts a byte array to a Byte object. The array can only contain one element.
  * <p>
  * @param _value The byte array.
  * @return <CODE>Byte</CODE> - Returns the Byte object.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null or contains more than or less than one element.
  * @include
  */
  public static final Byte toByte(byte[] _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    if(1 != _value.length) {
      throw new IllegalArgumentException("_value must be 1 byte.");
    }
    return new Byte(_value[0]);
  }

  /**
  * Converts a char primitive to a Byte object.
  * <p>
  * @param _value The char primitive.
  * @return <CODE>Byte</CODE> - Returns the Byte object.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Byte toByte(char _value) {
    return new Byte(toBytePrimitive(_value));
  }

  /**
  * Converts a Character object to a Byte object.
  * <p>
  * @param _value The Character object.
  * @return <CODE>Byte</CODE> - Returns the Byte object containing the ASCII code of the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null or is not <href a=#TRUE_CHAR><code><b>TRUE_CHAR</b></code></a> or <href a=#FALSE_CHAR><code><b>FALSE_CHAR</b></code></a>.
  * @include
  */
  public static final Byte toByte(Character _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toByte(_value.charValue());
  }

  /**
  * Converts a double primitive to a Byte object.
  * <p>
  * @param _value The double primitive.
  * @return <CODE>Byte</CODE> - Returns the Byte object containing the <code><b>_value</b></code> cast as a byte.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final Byte toByte(double _value) {
    if(Double.POSITIVE_INFINITY == _value
      || Double.NEGATIVE_INFINITY == _value
      || Double.NaN == _value
      || _value > STCTypeConverter.MAX_BYTE
      || _value < STCTypeConverter.MIN_BYTE
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return new Byte((byte)_value);
  }

  /**
  * Converts a Double object to a Byte object.
  * <p>
  * @param _value The Double object.
  * @return <CODE>Byte</CODE> - Returns the Byte object.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Byte toByte(Double _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toByte(_value.doubleValue());
  }

  /**
  * Converts a float primitive to a Byte object.
  * <p>
  * @param _value The float primitive.
  * @return <CODE>Byte</CODE> - Returns the Byte object containing the <code><b>_value</b></code> cast as a byte.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the return.
  * @include
  */
  public static final Byte toByte(float _value) {
    if(Float.POSITIVE_INFINITY == _value
      || Float.NEGATIVE_INFINITY == _value
      || Float.NaN == _value
      || _value > STCTypeConverter.MAX_BYTE
      || _value < STCTypeConverter.MIN_BYTE
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return new Byte((byte)_value);
  }

  /**
  * Converts a Float object to a Byte object.
  * <p>
  * @param _value The Float object.
  * @return <CODE>Byte</CODE> - Returns the Byte object containing the <code><b>_value</b></code> cast as a byte.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Byte toByte(Float _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toByte(_value.floatValue());
  }

  /**
  * Converts an int primitive to a Byte object.
  * <p>
  * @param _value The int primitive.
  * @return <CODE>Byte</CODE> - Returns the Byte object containing the <code><b>_value</b></code> case as a Byte.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the return.
  * @include
  */
  public static final Byte toByte(int _value) {
    if(_value > STCTypeConverter.MAX_BYTE
      || _value < STCTypeConverter.MIN_BYTE
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return new Byte((byte)_value);
  }

  /**
  * Converts an Integer object to a Byte object.
  * <p>
  * @param _value The Integer object.
  * @return <CODE>Byte</CODE> - Returns the Byte object containing the <code><b>_value</b></code> cast as a byte.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Byte toByte(Integer _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toByte(_value.intValue());
  }

  /**
  * Converts a long primitive to a Byte object.
  * <p>
  * @param _value The long primitive.
  * @return <CODE>Byte</CODE> - Returns the Byte object containing the <code><b>_value</b></code> cast as a byte.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the return.
  * @include
  */
  public static final Byte toByte(long _value) {
    if(_value > STCTypeConverter.MAX_BYTE
      || _value < STCTypeConverter.MIN_BYTE
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return new Byte((byte)_value);
  }

  /**
  * Converts a Long object to a Byte object.
  * <p>
  * @param _value The Long object.
  * @return <CODE>Byte</CODE> - Returns the Byte object containing the <code><b>_value</b></code> cast as a byte.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Byte toByte(Long _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toByte(_value.longValue());
  }

  /**
  * Converts a short primitive to a Byte object.
  * <p>
  * @param _value The short primitive.
  * @return <CODE>Byte</CODE> - Returns the Byte object containing the <code><b>_value</b></code> cast as a byte.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the return.
  * @include
  */
  public static final Byte toByte(short _value) {
    if(_value > STCTypeConverter.MAX_BYTE
      || _value < STCTypeConverter.MIN_BYTE
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return new Byte((byte)_value);
  }

  /**
  * Converts a Short object to a Byte object.
  * <p>
  * @param _value The Short object.
  * @return <CODE>Byte</CODE> - Returns the Byte object containing the <code><b>_value</b></code> cast as a byte.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Byte toByte(Short _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toByte(_value.shortValue());
  }

  /**
  * Converts a BigDecimal object to a Byte object.
  * <p>
  * @param _value The BigDecimal object.
  * @return <CODE>Byte</CODE> - Returns the Byte object containing the <code><b>_value</b></code> cast as a byte.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Byte toByte(BigDecimal _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toByte(_value.doubleValue());
  }

  /**
  * Converts a String object to a Byte object.
  * <p>
  * @param _value The String object.
  * @return <CODE>Byte</CODE> - Returns the Byte object containing the <code><b>_value</b></code> cast as a byte.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Byte toByte(String _value)
    throws IllegalArgumentException
  {
    return new Byte(toBytePrimitive(_value));
  }

  /**
  * Converts a byte to a byte array.
  * <p>
  * @param _value The byte value.
  * @return <CODE>byte[]</CODE> - Returns the byte array containing the byte.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final byte[] toByteArray(byte _value) {
      byte[] ret = new byte[1];
      ret[0] = _value;
      return ret;
  }

  /**
  * Converts a Byte object to a byte array.
  * <p>
  * @param _value The byte value.
  * @return <CODE>byte[]</CODE> - Returns the byte array containing the byte.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final byte[] toByteArray(Byte _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toByteArray(_value.byteValue());
  }

  /**
  * Converts a char to a byte array.
  * <p>
  * @param _value The char value.
  * @return <CODE>byte[]</CODE> - Returns the byte array containing the two Unicode bytes that make up the character.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final byte[] toByteArray(char _value) {
    byte[] ret = new byte[2];
    ret[0] = (byte)((_value >> 8) & 0xff);
    ret[1] = (byte)(_value & 0xff);
    return ret;
  }

  /**
  * Converts a Character object to a byte array.
  * <p>
  * @param _value The Character object.
  * @return <CODE>byte[]</CODE> - Returns the byte array containing the two Unicode bytes that make up the character.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final byte[] toByteArray(Character _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toByteArray(_value.charValue());
  }

  /**
  * Converts a double to a byte array. The most significant bits are in element zero; the least significant are in element seven.
  * <p>
  * @param _value The double value.
  * @return <CODE>byte[]</CODE> - Returns an eight byte array containing the double.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final byte[] toByteArray(double _value) {
    long bits = Double.doubleToLongBits(_value);
    byte[] ret = new byte[8];
    ret[0] = (byte)((bits >> 56) & 0xff);
    ret[1] = (byte)((bits >> 48) & 0xff);
    ret[2] = (byte)((bits >> 40) & 0xff);
    ret[3] = (byte)((bits >> 32) & 0xff);
    ret[4] = (byte)((bits >> 24) & 0xff);
    ret[5] = (byte)((bits >> 16) & 0xff);
    ret[6] = (byte)((bits >> 8) & 0xff);
    ret[7] = (byte)((bits) & 0xff);
    return ret;
  }

  /**
  * Converts a Double object to a byte array. The most significant bits are in element zero; the least significant are in element seven.
  * <p>
  * @param _value The double object.
  * @return <CODE>byte[]</CODE> - Returns an eight byte array containing the double.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final byte[] toByteArray(Double _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toByteArray(_value.doubleValue());
  }

  /**
  * Converts a float to a byte array. The most significant bits are in element zero; the least significant are in element three.
  * <p>
  * @param _value The float value.
  * @return <CODE>byte[]</CODE> - Returns a four byte array containing the float.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final byte[] toByteArray(float _value) {
    int bits = Float.floatToIntBits(_value);
    byte[] ret = new byte[4];
    ret[0] = (byte)((bits >> 24) & 0xff);
    ret[1] = (byte)((bits >> 16) & 0xff);
    ret[2] = (byte)((bits >> 8) & 0xff);
    ret[3] = (byte)((bits) & 0xff);
    return ret;
  }

  /**
  * Converts a Float object to a byte array. The most significant bits are in element zero; the least significant are in element three.
  * <p>
  * @param _value Description of the parameter.
  * @return <CODE>byte[]</CODE> - Returns a four byte array containing the float.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final byte[] toByteArray(Float _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toByteArray(_value.floatValue());
  }

  /**
  * Converts an int to a byte array. The most significant bits are in element zero; the least significant are in element three.
  * <p>
  * @param _value The int value.
  * @return <CODE>byte[]</CODE> - Returns a four byte array containing the int.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final byte[] toByteArray(int _value) {
    byte[] ret = new byte[4];
    ret[0] = (byte)((_value >> 24) & 0xff);
    ret[1] = (byte)((_value >> 16) & 0xff);
    ret[2] = (byte)((_value >> 8) & 0xff);
    ret[3] = (byte)((_value) & 0xff);
    return ret;
  }

  /**
  * Converts an Integer object to a byte array. The most significant bits are in element zero; the least significant are in element three.
  * <p>
  * @param _value The Integer object.
  * @return <CODE>byte[]</CODE> - Returns a four byte array containing the int.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final byte[] toByteArray(Integer _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toByteArray(_value.intValue());
  }

  /**
  * Converts a long to a byte array. The most significant bits are in element zero; the least significant are in element seven.
  * <p>
  * @param _value The long value.
  * @return <CODE>byte[]</CODE> - Returns an eight byte array containing the long.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final byte[] toByteArray(long _value) {
    byte[] ret = new byte[8];
    ret[0] = (byte)((_value >> 56) & 0xff);
    ret[1] = (byte)((_value >> 48) & 0xff);
    ret[2] = (byte)((_value >> 40) & 0xff);
    ret[3] = (byte)((_value >> 32) & 0xff);
    ret[4] = (byte)((_value >> 24) & 0xff);
    ret[5] = (byte)((_value >> 16) & 0xff);
    ret[6] = (byte)((_value >> 8) & 0xff);
    ret[7] = (byte)((_value) & 0xff);
    return ret;
  }

  /**
  * Converts a Long object to a byte array. The most significant bits are in element zero; the least significant are in element seven.
  * <p>
  * @param _value The Long object.
  * @return <CODE>byte[]</CODE> - Returns an eight byte array containing the Long object.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final byte[] toByteArray(Long _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toByteArray(_value.longValue());
  }

  /**
  * Converts a short to a byte array. The most significant bits are in element zero; the least significant are in element one.
  * <p>
  * @param _value The short value.
  * @return <CODE>byte[]</CODE> - Returns a two byte array containing the short.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final byte[] toByteArray(short _value) {
    byte[] ret = new byte[2];
    ret[0] = (byte)((_value >> 8) & 0xff);
    ret[1] = (byte)((_value) & 0xff);
    return ret;
  }

  /**
  * Converts a Short object to a byte array. The most significant bits are in element zero; the least significant are in element one.
  * <p>
  * @param _value The Short object.
  * @return <CODE>byte[]</CODE> - Returns a two byte array containing the Short.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final byte[] toByteArray(Short _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toByteArray(_value.shortValue());
  }

  /**
  * Converts a BigDecimal object to a byte array that contains a UTF-8 string representation of the BigDecimal.
  * <p>
  * @param _value The BigDecimal object.
  * @return <CODE>byte[]</CODE> - Returns the byte array.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final byte[] toByteArray(BigDecimal _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    try {
        return _value.toString().getBytes("UTF-8");
    } catch(UnsupportedEncodingException ex) {
        System.err.println("Something is seriously wrong if you don't have UTF-8 encoding!");
        ex.printStackTrace();
    }
    return null;
  }

  /**
  * Converts a String object to a byte array.
  * <p>
  * @param _value The String object.
  * @return <CODE>byte[]</CODE> - Returns a byte array containing the String.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final byte[] toByteArray(String _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.getBytes();
  }

  /**
  * Converts a String object to a byte array according to the specified encoding.
  * <p>
  * @param _value The String object.
  * @param _encoding The encoding scheme.
  * @return <CODE>byte[]</CODE> - Returns a byte array containing the String.
  * @exception UnsupportedEncodingException Thrown if the <code><b>_encoding</b></code> is unsupported.
  * @include
  */
  public static final byte[] toByteArray(String _value, String _encoding)
    throws java.io.UnsupportedEncodingException
  {
    if(null == _value || null == _encoding) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.getBytes(_encoding);
  }

  /**
  * Converts a boolean primitive to a char primitive.
  * <p>
  * @param _value The char primitive.
  *
  * @return <CODE>char</CODE> - Returns <code><b><href a=#TRUE_CHAR>TRUE_CHAR</a></b></code> if <code><b>_value</b></code> is true or <href a=#FALSE_CHAR><code><b>FALSE_CHAR</b></code></a> if <code><b>_value</b></code> is false.
  *
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final char toCharPrimitive(boolean _value) {
    return _value?TRUE_CHAR:FALSE_CHAR;
  }

  /**
  * Converts a Boolean object to a char primitive.
  * <p>
  * @param _value The Boolean object (cannot be null).
  * @return <CODE>char</CODE> - Returns <href a=#TRUE_CHAR><code><b>TRUE_CHAR</b></code></a> if the <code><b>_value</b></code> is true or <href a=#FALSE_CHAR><code><b>FALSE_CHAR</b></code></a> if it is false.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final char toCharPrimitive(Boolean _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toCharPrimitive(_value.booleanValue());
  }

  /**
  * Converts a byte primitive to a char primitive. The char is the character represented by the byte as an ASCII code.
  * <p>
  * @param _value The byte primitive.
  * @return <CODE>char</CODE> - Returns the character whose ASCII code is contained in the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final char toCharPrimitive(byte _value) {
    return (char)_value;
  }

  /**
  * Converts a Byte object to a char primitive.
  * <p>
  * @param _value The Byte object.
  * @return <CODE>char</CODE> - Returns the character whose ASCII code is contained in the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final char toCharPrimitive(Byte _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toCharPrimitive(_value.byteValue());
  }

  /**
  * Converts a one or two byte array to a char primitive.
  * <p>
  * @param _value The one or two byte array.
  * @return <CODE>char</CODE> - Returns the character whose ASCII or Unicode code is contained in the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null or is not one or two bytes in length.
  * @include
  */
  public static final char toCharPrimitive(byte[] _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    switch(_value.length) {

      case 1:
        return (char)_value[0];

      case 2:
        return (char)((_value[0]<<8) | (_value[1]));

      default:
        throw new IllegalArgumentException("Byte array must be 1 or 2 bytes long.");
    }
  }

  /**
  * Converts a Character object to a char primitive.
  * <p>
  * @param _value The Character object.
  * @return <CODE>char</CODE> - Returns the character primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final char toCharPrimitive(Character _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.charValue();
  }

  /**
  * Converts a double primitive to a char primitive.
  * <p>
  * @param _value The double primitive.
  * @return <CODE>char</CODE> - Returns the character whose Unicode code is contained in the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final char toCharPrimitive(double _value) {
    return (char)_value;
  }

  /**
  * Converts a Double object to a char primitive.
  * <p>
  * @param _value The Double object.
  * @return <CODE>char</CODE> - Returns the character whose Unicode code is contained in the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final char toCharPrimitive(Double _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toCharPrimitive(_value.doubleValue());
  }

  /**
  * Converts a float primitive to a char primitive.
  * <p>
  * @param _value The float primitive.
  * @return <CODE>char</CODE> - Returns the character whose Unicode code is contained in the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final char toCharPrimitive(float _value) {
    return (char)_value;
  }

  /**
  * Converts a Float object to a char primitive.
  * <p>
  * @param _value The Float object.
  * @return <CODE>char</CODE> - Returns the character whose Unicode code is contained in the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final char toCharPrimitive(Float _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toCharPrimitive(_value.floatValue());
  }

  /**
  * Converts an int primitive to a char primitive.
  * <p>
  * @param _value The int primitive.
  * @return <CODE>char</CODE> - Returns the character whose Unicode code is contained in the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final char toCharPrimitive(int _value) {
    return (char)_value;
  }

  /**
  * Converts an Integer object to a char primitive.
  * <p>
  * @param _value The Integer object.
  * @return <CODE>char</CODE> - Returns the character whose Unicode code is contained in the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final char toCharPrimitive(Integer _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toCharPrimitive(_value.intValue());
  }

  /**
  * Converts a long primitive to a char primitive.
  * <p>
  * @param _value The long primitive.
  * @return <CODE>char</CODE> - Returns the character whose Unicode code is contained in <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final char toCharPrimitive(long _value) {
    return (char)_value;
  }

  /**
  * Converts a Long object to a char primitive.
  * <p>
  * @param _value The Long object.
  * @return <CODE>char</CODE> - Returns the character whose Unicode code is contained in <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final char toCharPrimitive(Long _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toCharPrimitive(_value.longValue());
  }

  /**
  * Converts a short primitive to a char primitive.
  * <p>
  * @param _value The short primitive.
  * @return <CODE>char</CODE> - Returns the character whose Unicode code is contained in the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final char toCharPrimitive(short _value) {
    return (char)_value;
  }

  /**
  * Converts a Short object to a char primitive.
  * <p>
  * @param _value The Short object.
  * @return <CODE>char</CODE> - Returns the character whose Unicode code is contained in <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final char toCharPrimitive(Short _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toCharPrimitive(_value.shortValue());
  }

  /**
  * Converts a BigDecimal object to a char primitive.
  * <p>
  * @param _value The BigDecimal object.
  * @return <CODE>char</CODE> - Returns the character whose Unicode code is contained in the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final char toCharPrimitive(BigDecimal _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toCharPrimitive(_value.doubleValue());
  }

  /**
  * Converts a one character String object to a char primitive.
  * <p>
  * @param _value The one character String object.
  * @return <CODE>char</CODE> - Returns the char primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null, empty, or contains more than one character.
  * @include
  */
  public static final char toCharPrimitive(String _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    if(0 == _value.length()) {
      throw new IllegalArgumentException(EX_EMPTY_STRING);
    }
    if(1 < _value.length()) {
      throw new IllegalArgumentException(EX_INVALID_VALUE + _value);
    }
    return _value.charAt(0);
  }

  /**
  * Converts a boolean primitive to a Character object.
  * <p>
  * @param _value The boolean primitive.
  * @return <CODE>Character</CODE> - Returns the Character object containing <href a=#TRUE_CHAR><code><b>TRUE_CHAR</b></code></a> if <code><b>_value</b></code> is true or <href a=#FALSE_CHAR><code><b>FALSE_CHAR</b></code></a> if <code><b>_value</b></code> is false.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Character toCharacter(boolean _value) {
    return new Character(toCharPrimitive(_value));
  }

  /**
  * Converts a Boolean object to a Character object.
  * <p>
  * @param _value The Boolean object.
  * @return <CODE>Character</CODE> - Returns the Character object containing <href a=#TRUE_CHAR><code><b>TRUE_CHAR</b></code></a> if <code><b>_value</b></code> is true or <href a=#FALSE_CHAR><code><b>FALSE_CHAR</b></code></a> if <code><b>_value</b></code> is false.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Character toCharacter(Boolean _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toCharacter(_value.booleanValue());
  }

  /**
  * Converts a byte primitive to a Character object.
  * <p>
  * @param _value The byte primitive.
  * @return <CODE>Character</CODE> - Returns the Character containing the character whose ASCII code is <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Character toCharacter(byte _value) {
    return new Character((char)_value);
  }

  /**
  * Converts a one or two byte array to a Character object.
  * <p>
  * @param _value The one of two byte array.
  * @return <CODE>Character</CODE> - Returns the character whose ASCII or Unicode code is contained in <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null or is not one or two bytes in length.
  * @include
  */
  public static final Character toCharacter(byte[] _value) {
    return new Character(toCharPrimitive(_value));
  }

  /**
  * Converts a Byte object to a Character object.
  * <p>
  * @param _value The Byte object.
  * @return <CODE>Character</CODE> - Returns the Character containing the character whose ASCII code is <code><b>_value</b></code>.
  * @include
  */
  public static final Character toCharacter(Byte _value) {
    return toCharacter(_value.byteValue());
  }

  /**
  * Converts a char primitive to a Character object.
  * <p>
  * @param _value The char primitive.
  * @return <CODE>Character</CODE> - Returns the Character containing the <code><b>_value</b></code>.
  * @include
  */
  public static final Character toCharacter(char _value) {
    return new Character(_value);
  }

  /**
  * Converts a double primitive to a Character object.
  * <p>
  * @param _value The double primitive.
  * @return <CODE>Character</CODE> - Returns the Character containing the Unicode code of <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Character toCharacter(double _value) {
    return new Character((char)_value);
  }

  /**
  * Converts a Double object to a Character object.
  * <p>
  * @param _value The Double object.
  * @return <CODE>Character</CODE> - Returns the Character containing the Unicode code of <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Character toCharacter(Double _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toCharacter(_value.doubleValue());
  }

  /**
  * Converts a float primitive to a Character object.
  * <p>
  * @param _value The float primitive.
  * @return <CODE>Character</CODE> - Returns the Character containing the Unicode code of <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Character toCharacter(float _value) {
    return new Character((char)_value);
  }

  /**
  * Converts a Float object to a Character object.
  * <p>
  * @param _value The Float object.
  * @return <CODE>Character</CODE> - Returns the Character containing the Unicode code of <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Character toCharacter(Float _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toCharacter(_value.floatValue());
  }

  /**
  * Converts an int primitive to a Character object.
  * <p>
  * @param _value The int primitive.
  * @return <CODE>Character</CODE> - Returns the Character containing the Unicode code of <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Character toCharacter(int _value) {
    return new Character((char)_value);
  }

  /**
  * Converts an Integer object to a Character object.
  * <p>
  * @param _value The Integer object.
  * @return <CODE>Character</CODE> - Returns the Character containing the Unicode code of <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Character toCharacter(Integer _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toCharacter(_value.intValue());
  }

  /**
  * Converts a long primitive to a Character object.
  * <p>
  * @param _value The long primitive.
  * @return <CODE>Character</CODE> - Returns the Character containing the Unicode code of <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Character toCharacter(long _value) {
    return new Character((char)_value);
  }

  /**
  * Converts a Long object to a Character object.
  * <p>
  * @param _value The Long object.
  * @return <CODE>Character</CODE> - Returns the Character containing the Unicode code of <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Character toCharacter(Long _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toCharacter(_value.longValue());
  }

  /**
  * Converts a short primitive to a Character object.
  * <p>
  * @param _value The short primitive.
  * @return <CODE>Character</CODE> - Returns the Character containing the Unicode code of <code><b>_value</b></code>.
  * @include
  */
  public static final Character toCharacter(short _value) {
    return new Character((char)_value);
  }

  /**
  * Converts a Short object to a Character object.
  * <p>
  * @param _value The Short object.
  * @return <CODE>Character</CODE> - Returns the Character containing the Unicode code of <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Character toCharacter(Short _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toCharacter(_value.shortValue());
  }

  /**
  * Converts a BigDecimal object to a Character object.
  * <p>
  * @param _value The BigDecimal object.
  * @return <CODE>Character</CODE> - Returns the Character containing the Unicode code of <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Character toCharacter(BigDecimal _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toCharacter(_value.doubleValue());
  }

  /**
  * Converts a one character String object to a Character object.
  * <p>
  * @param _value The one character String object.
  * @return <CODE>Character</CODE> - Returns the Character object.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Character toCharacter(String _value)
    throws IllegalArgumentException
  {
    return new Character(toCharPrimitive(_value));
  }

  /**
  * Converts a boolean primitive to a double primitive.
  * <p>
  * @param _value The boolean primitive.
  * @return <CODE>double</CODE> - Returns <code><b>1</b></code> if <code><b>_value</b></code> is true or <code><b>0</b></code> if <code><b>_value</b></code> is false.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final double toDoublePrimitive(boolean _value) {
    return _value?1d:0d;
  }

  /**
  * Converts a Boolean object to a double primitive.
  * <p>
  * @param _value The Boolean object. The <code><b>_value</b></code> cannot be null.
  * @return <CODE>double</CODE> - Returns <code><b>1</b></code> if <code><b>_value</b></code> is true or <code><b>0</b></code> if <code><b>_value</b></code> is false.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final double toDoublePrimitive(Boolean _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toDoublePrimitive(_value.booleanValue());
  }

  /**
  * Converts a byte primitive to a double primitive.
  * <p>
  * @param _value The byte primitive.
  * @return <CODE>double</CODE> - Returns the double value.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final double toDoublePrimitive(byte _value) {
    return (double)_value;
  }

  /**
  * Converts an eight byte array to a double primitive. The most significant bits are in element zero; the least significant are in element seven.
  * <p>
  * @param _value The eight byte array.
  * @return <CODE>double</CODE> - Returns the double value.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null or not eight bytes in length.
  * @include
  */
  public static final double toDoublePrimitive(byte[] _value) {

    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }

    if(_value.length != 8) {
      throw new IllegalArgumentException("_value must be 8 bytes.");
    }

    long bits = 0L;
    bits |= _value[0] << 56;
    bits |= _value[1] << 48;
    bits |= _value[2] << 40;
    bits |= _value[3] << 32;
    bits |= _value[4] << 24;
    bits |= _value[5] << 16;
    bits |= _value[6] << 8;
    bits |= _value[7];

    return Double.longBitsToDouble(bits);
  }

  /**
  * Converts a Byte object to a double primitive.
  * <p>
  * @param _value The Byte object.
  * @return <CODE>double</CODE> - Returns the double value.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final double toDoublePrimitive(Byte _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toDoublePrimitive(_value.byteValue());
  }

  /**
  * Converts a char primitive to a double primitive.
  * <p>
  * @param _value The char primitive.
  * @return <CODE>double</CODE> - Returns the double whose Unicode code is contained in <code><b>_value</b></code>.
  * @include
  */
  public static final double toDoublePrimitive(char _value) {
    return (double)_value;
  }

  /**
  * Converts a Character object to a double primitive.
  * <p>
  * @param _value The Character object.
  * @return <CODE>double</CODE> - Returns the double whose Unicode code is contained in <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final double toDoublePrimitive(Character _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toDoublePrimitive(_value.charValue());
  }

  /**
  * Converts a Double object to a double primitive.
  * <p>
  * @param _value The Double object.
  * @return <CODE>double</CODE> - Returns the double primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @see Double#doubleValue()
  * @include
  */
  public static final double toDoublePrimitive(Double _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.doubleValue();
  }

  /**
  * Converts a float primitive to a double primitive.
  * <p>
  * @param _value The float primitive.
  * @return <CODE>double</CODE> - Returns the double primitive.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final double toDoublePrimitive(float _value) {
    return (double)_value;
  }

  /**
  * Converts a Float object to a double primitive.
  * <p>
  * @param _value The Float object.
  * @return <CODE>double</CODE> - Returns the double primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final double toDoublePrimitive(Float _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toDoublePrimitive(_value.floatValue());
  }

  /**
  * Converts an int primitive to a double primitive.
  * <p>
  * @param _value The int primitive.
  * @return <CODE>double</CODE> - Returns the double primitive.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final double toDoublePrimitive(int _value) {
    return (double)_value;
  }

  /**
  * Converts an Integer object to a double primitive.
  * <p>
  * @param _value The Integer object.
  * @return <CODE>double</CODE> - Returns the double primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final double toDoublePrimitive(Integer _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toDoublePrimitive(_value.intValue());
  }

  /**
  * Converts a long primitive to a double primitive.
  * <p>
  * @param _value The long primitive.
  * @return <CODE>double</CODE> - Returns the double primitive.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final double toDoublePrimitive(long _value) {
    return (double)_value;
  }

  /**
  * Converts a Long object to a double primitive.
  * <p>
  * @param _value The Long object.
  * @return <CODE>double</CODE> - Returns the double primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final double toDoublePrimitive(Long _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toDoublePrimitive(_value.longValue());
  }

  /**
  * Converts a short primitive to a double primitive.
  * <p>
  * @param _value The short primitive.
  * @return <CODE>double</CODE> - Returns the double primitive.
  * @include
  */
  public static final double toDoublePrimitive(short _value) {
    return (double)_value;
  }

  /**
  * Converts a Short object to a double primitive.
  * <p>
  * @param _value The Short object.
  * @return <CODE>double</CODE> - Returns the double primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final double toDoublePrimitive(Short _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toDoublePrimitive(_value.shortValue());
  }

  /**
  * Converts a BigDecimal object to a double primitive.
  * <p>
  * @param _value The BigDecimal object.
  * @return <CODE>double</CODE> - Returns the double primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final double toDoublePrimitive(BigDecimal _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.doubleValue();
  }

  /**
  * Converts a String object to a double primitive.
  * <p>
  * @param _value The String object.
  * @return <CODE>double</CODE> - Returns the double primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final double toDoublePrimitive(String _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    if(0 == _value.length()) {
      throw new IllegalArgumentException(EX_EMPTY_STRING);
    }
    return Double.parseDouble(_value);
  }

  /**
  * Converts a boolean primitive to a Double object.
  * <p>
  * @param _value The boolean primitive.
  * @return <CODE>Double</CODE> - Returns the Double object containing <code><b>1</b></code> if <code><b>_value</b></code> is true or <code><b>0</b></code> if <code><b>_value</b></code> is false.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Double toDouble(boolean _value) {
    return new Double(toDoublePrimitive(_value));
  }

  /**
  * Converts a Boolean object to a Double object.
  * <p>
  * @param _value The Boolean object.
  * @return <CODE>Double</CODE> - Returns the Double object containing <code><b>1</b></code> if <code><b>_value</b></code> is true or <code><b>0</b></code> if <code><b>_value</b></code> is false.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Double toDouble(Boolean _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toDouble(_value.booleanValue());
  }

  /**
  * Converts a byte primitive to a Double object.
  * <p>
  * @param _value The byte primitive.
  * @return <CODE>Double</CODE> - Returns the Double object containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Double toDouble(byte _value) {
    return new Double((double)_value);
  }

  /**
  * Converts a Byte object to a Double object.
  * <p>
  * @param _value The Byte object.
  * @return <CODE>Double</CODE> - Returns the Double object containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Double toDouble(Byte _value) {
    return toDouble(_value.byteValue());
  }

  /**
  * Converts a byte array to a Double object.
  * <p>
  * @param _value The eight byte array.
  * @return <CODE>Double</CODE> - Returns the Double object.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null or is not eight bytes in length.
  * @include
  */
  public static final Double toDouble(byte[] _value) {
    return new Double(toDoublePrimitive(_value));
  }

  /**
    * Converts a char primitive to a Double object.
    * <p>
    * @param _value The char primitive.
    * @return <CODE>Double</CODE> - Returns the Double containing the Unicode code of <code><b>_value</b></code>.
    * <DT><B>Throws:</B><DD>None.
    * @include
    */
  public static final Double toDouble(char _value) {
    return new Double((double)_value);
  }

  /**
  * Converts a Character object to a Double object.
  * <p>
  * @param _value The Character object.
  * @return <CODE>Double</CODE> - Returns the Double object containing <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null
  * @include
  */
  public static final Double toDouble(Character _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toDouble(_value.charValue());
  }

  /**
  * Converts a double primitive to a Double object.
  * <p>
  * @param _value The double primitive.
  * @return <CODE>Double</CODE> - Returns the Double object containing the <code><b>_value</b></code>
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Double toDouble(double _value) {
    return new Double(_value);
  }

  /**
  * Converts a float primitive to a Double object.
  * <p>
  * @param _value The float primitive.
  * @return <CODE>Double</CODE> - Returns the Double object containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Double toDouble(float _value) {
    return new Double((double)_value);
  }

  /**
  * Converts a Float object to a Double object.
  * <p>
  * @param _value The Float object.
  * @return <CODE>Double</CODE> - Returns the Double object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Double toDouble(Float _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toDouble(_value.floatValue());
  }

  /**
  * Converts an int primitive to a Double object.
  * <p>
  * @param _value The int primitive.
  * @return <CODE>Double</CODE> - Returns the Double object containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Double toDouble(int _value) {
    return new Double((double)_value);
  }

  /**
  * Converts an Integer object to a Double object.
  * <p>
  * @param _value The Integer object.
  * @return <CODE>Double</CODE> - Returns the Double object containing the <code><b>_value</b></code>
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Double toDouble(Integer _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toDouble(_value.intValue());
  }

  /**
  * Converts a long primitive to a Double object.
  * <p>
  * @param _value The long primitive.
  * @return <CODE>Double</CODE> - Returns the Double object containing the <code><b>_value</b></code>.
  * @include
  */
  public static final Double toDouble(long _value) {
    return new Double((double)_value);
  }

  /**
  * Converts a Long object to a Double object.
  * <p>
  * @param _value The Long object.
  * @return <CODE>Double</CODE> - Returns the Double object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null
  * @include
  */
  public static final Double toDouble(Long _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toDouble(_value.longValue());
  }

  /**
  * Converts a short primitive to a Double object.
  * <p>
  * @param _value The short primitive.
  * @return <CODE>Double</CODE> - Returns the Double object containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Double toDouble(short _value) {
    return new Double((double)_value);
  }

  /**
  * Converts a Short object to a Double object.
  * <p>
  * @param _value The Short object.
  * @return <CODE>Double</CODE> - Returns the Double object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Double toDouble(Short _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toDouble(_value.shortValue());
  }

  /**
  * Converts a BigDecimal object to a Double object.
  * <p>
  * @param _value The BigDecimal object.
  * @return <CODE>Double</CODE> - Returns the Double object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Double toDouble(BigDecimal _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toDouble(_value.doubleValue());
  }

  /**
  * Converts a String object to a Double object.
  * <p>
  * @param _value The String object.
  * @return <CODE>Double</CODE> - Returns the Double object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Double toDouble(String _value)
    throws IllegalArgumentException
  {
    return new Double(toDoublePrimitive(_value));
  }

  /**
  * Converts a boolean primitive to a float primitive.
  * <p>
  * @param _value The boolean primitive.
  * @return <CODE>float</CODE> - Returns <code><b>1</b></code> if the <code><b>_value</b></code> is true or <code><b>0</b></code> if the <code><b>_value</b></code> is false.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final float toFloatPrimitive(boolean _value) {
    return _value?1f:0f;
  }

  /**
  * Converts a Boolean object to a float primitive.
  * <p>
  * @param _value The Boolean object.
  * @return <CODE>float</CODE> - Returns the float value.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final float toFloatPrimitive(Boolean _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toFloatPrimitive(_value.booleanValue());
  }

  /**
  * Converts a byte primitive to a float primitive.
  * <p>
  * @param _value The byte primitive.
  * @return <CODE>float</CODE> - Returns the float value.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final float toFloatPrimitive(byte _value) {
    return (float)_value;
  }

  /**
  * Converts a Byte object to a float primitive.
  * <p>
  * @param _value The Byte object.
  * @return <CODE>float</CODE> - Returns the float value.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final float toFloatPrimitive(Byte _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toFloatPrimitive(_value.byteValue());
  }

  /**
  * Converts a four byte array to a float primitive. The most significant bits are in element zero; the least significant are in element three.
  * <p>
  * @param _value The four byte array.
  * @return <CODE>float</CODE> - Returns the float contained in <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null or is not four bytes in length.
  * @include
  */
  public static final float toFloatPrimitive(byte[] _value) {

    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }

    if(_value.length != 4) {
      throw new IllegalArgumentException("_value must be 4 bytes.");
    }

    int bits = 0;
    bits |= _value[0] << 24;
    bits |= _value[1] << 16;
    bits |= _value[2] << 8;
    bits |= _value[3];

    return Float.intBitsToFloat(bits);
  }

  /**
  * Converts a char primitive to a float primitive.
  * <p>
  * @param _value The char primitive.
  * @return <CODE>float</CODE> - Returns the float whose Unicode code is contained in <code><b>_value</b></code>.
  * @include
  */
  public static final float toFloatPrimitive(char _value) {
    return (float)_value;
  }

  /**
  * Converts a Character object to a float primitive.
  * <p>
  * @param _value The Character object.
  * @return <CODE>float</CODE> - Returns the float whose Unicode code is contained in the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final float toFloatPrimitive(Character _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toFloatPrimitive(_value.charValue());
  }

  /**
  * Converts a double primitive to a float primitive.
  * <p>
  * @param _value The double primitive.
  * @return <CODE>float</CODE> - Returns the float primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final float toFloatPrimitive(double _value) {
    if(Double.POSITIVE_INFINITY == _value
      || Double.NEGATIVE_INFINITY == _value
      || Double.NaN == _value
      || (_value > 0 &&
            (_value > STCTypeConverter.MAX_FLOAT
              || _value < STCTypeConverter.MIN_FLOAT
            )
          )
      || (_value < 0 &&
            (-_value > STCTypeConverter.MAX_FLOAT
              || -_value < STCTypeConverter.MIN_FLOAT
            )
          )
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return (float)_value;
  }

  /**
  * Converts a Double object to a float primitive.
  * <p>
  * @param _value The Double object.
  * @return <CODE>float</CODE> - Returns the float primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final float toFloatPrimitive(Double _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toFloatPrimitive(_value.doubleValue());
  }

  /**
  * Converts a Float object to a float primitive.
  * <p>
  * @param _value The Float object.
  * @return <CODE>float</CODE> - Returns the float primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final float toFloatPrimitive(Float _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.floatValue();
  }

  /**
  * Converts an int primitive to a float primitive.
  * <p>
  * @param _value The int primitive.
  * @return <CODE>float</CODE> - Returns the float primitive.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final float toFloatPrimitive(int _value) {
    return (float)_value;
  }

  /**
  * Converts an Integer object to a float primitive.
  * <p>
  * @param _value The Integer object.
  * @return <CODE>float</CODE> - Returns the float primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final float toFloatPrimitive(Integer _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toFloatPrimitive(_value.intValue());
  }

  /**
  * Converts a long primitive to a float primitive.
  * <p>
  * @param _value The long primitive.
  * @return <CODE>float</CODE> - Returns the float primitive.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final float toFloatPrimitive(long _value) {
    return (float)_value;
  }

  /**
  * Converts a Long object to a float primitive.
  * <p>
  * @param _value The Long object.
  * @return <CODE>float</CODE> - Returns the float primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final float toFloatPrimitive(Long _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toFloatPrimitive(_value.longValue());
  }

  /**
  * Converts a short primitive to a float primitive.
  * <p>
  * @param _value The short primitive.
  * @return <CODE>float</CODE> - Returns the float primitive.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final float toFloatPrimitive(short _value) {
    return (float)_value;
  }

  /**
  * Converts a Short object to a float primitive.
  * <p>
  * @param _value The Short object.
  * @return <CODE>float</CODE> - Returns the float primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final float toFloatPrimitive(Short _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toFloatPrimitive(_value.shortValue());
  }

  /**
  * Converts a BigDecimal object to a float primitive.
  * <p>
  * @param _value The BigDecimal object.
  * @return <CODE>float</CODE> - Returns the float primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final float toFloatPrimitive(BigDecimal _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toFloatPrimitive(_value.doubleValue());
  }

  /**
  * Converts a String object to a float primitive.
  * <p>
  * @param _value The String object.
  * @return <CODE>float</CODE> - Returns the float primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @see Float#parseFloat(String)
  * @include
  */
  public static final float toFloatPrimitive(String _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    if(0 == _value.length()) {
      throw new IllegalArgumentException(EX_EMPTY_STRING);
    }
    return Float.parseFloat(_value);
  }

  /**
  * Converts a boolean primitive to a Float object.
  * <p>
  * @param _value The boolean primitive.
  * @return <CODE>Float</CODE> - Returns the Float object containing <code><b>1</b></code> if the <code><b>_value</b></code> is true or <code><b>0</b></code> if the <code><b>_value</b></code> is false.
  * @include
  */
  public static final Float toFloat(boolean _value) {
    return new Float(toFloatPrimitive(_value));
  }

  /**
  * Converts a Boolean object to a Float object.
  * <p>
  * @param _value The Boolean object.
  * @return <CODE>Float</CODE> - Returns the Float object containing <code><b>1</b></code> if the <code><b>_value</b></code> is true or <code><b>0</b></code> if the <code><b>_value</b></code> is false.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Float toFloat(Boolean _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toFloat(_value.booleanValue());
  }

  /**
  * Converts a byte primitive to a Float object.
  * <p>
  * @param _value The byte primitive.
  * @return <CODE>Float</CODE> - Returns the Float object.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Float toFloat(byte _value) {
    return new Float((float)_value);
  }

  /**
  * Converts the Byte object to a Float object.
  * <p>
  * @param _value The Byte object.
  * @return <CODE>Float</CODE> - Returns the Float containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Float toFloat(Byte _value) {
    return toFloat(_value.byteValue());
  }

  /**
  * Converts a four byte array to a Float object. The most significant bits are in element zero; the least significant are in element three.
  * <p>
  * @param _value The four byte array.
  * @return <CODE>Float</CODE> - Returns the ...
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null or if it is not four bytes in length.
  * @include
  */
  public static final Float toFloat(byte[] _value) {
    return new Float(toFloatPrimitive(_value));
  }

  /**
  * Converts a char primitive to a Float object.
  * <p>
  * @param _value The char primitive.
  * @return <CODE>Float</CODE> - Returns the Float object containing the Unicode code of <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Float toFloat(char _value) {
    return new Float((float)_value);
  }

  /**
  * Converts a Character object to a Float object.
  * <p>
  * @param _value The Character object.
  * @return <CODE>Float</CODE> - Returns the Float object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Float toFloat(Character _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toFloat(_value.charValue());
  }

  /**
  * Converts a double primitive to a Float object.
  * <p>
  * @param _value The double primitive.
  * @return <CODE>Float</CODE> - Returns the Float object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final Float toFloat(double _value) {
    if(Double.POSITIVE_INFINITY == _value
      || Double.NEGATIVE_INFINITY == _value
      || Double.NaN == _value
      || (_value > 0 &&
            (_value > STCTypeConverter.MAX_FLOAT
              || _value < STCTypeConverter.MIN_FLOAT
            )
          )
      || (_value < 0 &&
            (-_value > STCTypeConverter.MAX_FLOAT
              || -_value < STCTypeConverter.MIN_FLOAT
            )
          )
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return new Float(_value);
  }

  /**
  * Converts a Double object to a Float object.
  * <p>
  * @param _value The Double object.
  * @return <CODE>Float</CODE> - Returns the Float object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Float toFloat(Double _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toFloat(_value.floatValue());
  }

  /**
  * Converts a float primitive to a Float object.
  * <p>
  * @param _value The float primitive.
  * @return <CODE>Float</CODE> - Returns the Float object containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Float toFloat(float _value) {
    return new Float((float)_value);
  }

  /**
  * Converts an int primitive to a Float object.
  * <p>
  * @param _value The int primitive.
  * @return <CODE>Float</CODE> - Returns the Float object containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Float toFloat(int _value) {
    return new Float((float)_value);
  }

  /**
  * Converts an Integer object to a Float object.
  * <p>
  * @param _value The Integer object.
  * @return <CODE>Float</CODE> - Returns the Float object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Float toFloat(Integer _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toFloat(_value.intValue());
  }

  /**
  * Converts a long primitive to a Float object.
  * <p>
  * @param _value The long primitive.
  * @return <CODE>Float</CODE> - Returns the Float object containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Float toFloat(long _value) {
    return new Float((float)_value);
  }

  /**
  * Converts a Long object to a Float object.
  * <p>
  * @param _value The Long object.
  * @return <CODE>Float</CODE> - Returns the Float object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Float toFloat(Long _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toFloat(_value.longValue());
  }

  /**
  * Converts a short primitive to a Float object.
  * <p>
  * @param _value The short primitive.
  * @return <CODE>Float</CODE> - Returns the Float object containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Float toFloat(short _value) {
    return new Float((float)_value);
  }

  /**
  * Converts a Short object to a Float object.
  * <p>
  * @param _value The Short object.
  * @return <CODE>Float</CODE> - Returns the Float object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Float toFloat(Short _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toFloat(_value.shortValue());
  }

  /**
  * Converts a BigDecimal to a Float object.
  * <p>
  * @param _value The BigDecimal object.
  * @return <CODE>Float</CODE> - Returns the Float object containing the <code><b>_value</b></code>
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Float toFloat(BigDecimal _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toFloat(_value.doubleValue());
  }

  /**
  * Converts a String object to a Float object.
  * <p>
  * @param _value The String object.
  * @return <CODE>Float</CODE> - Returns the Float object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Float toFloat(String _value)
    throws IllegalArgumentException
  {
    return new Float(toFloatPrimitive(_value));
  }

  /**
  * Converts a boolean primitive to an int primitive.
  * <p>
  * @param _value The boolean primitive.
  * @return <CODE>int</CODE> - Returns <code><b>1</b></code> if the <code><b>_value</b></code> is true or <code><b>0</b></code> if the <code><b>_value</b></code> is false.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final int toIntegerPrimitive(boolean _value) {
    return _value?1:0;
  }

  /**
  * Converts a Boolean object to an int primitive.
  * <p>
  * @param _value The Boolean object.
  * @return <CODE>int</CODE> - Returns <code><b>1</b></code> if the <code><b>_value</b></code> is true or <code><b>0</b></code> if the <code><b>_value</b></code> is false.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final int toIntegerPrimitive(Boolean _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toIntegerPrimitive(_value.booleanValue());
  }

  /**
  * Converts a byte primitive to an int primitive.
  * <p>
  * @param _value The byte primitive.
  * @return <CODE>int</CODE> - Returns the int value.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final int toIntegerPrimitive(byte _value) {
    return (int)_value;
  }

  /**
  * Converts a Byte object to an int primitive.
  * <p>
  * @param _value The Byte object.
  * @return <CODE>int</CODE> - Returns the int value.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final int toIntegerPrimitive(Byte _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toIntegerPrimitive(_value.byteValue());
  }

  /**
  * Converts a four byte array into an int primitive. The most significant bits are in element zero; the least significant are in element three.
  * <p>
  * @param _value The four byte array.
  * @return <CODE>int</CODE> - Returns the int contained in the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null or is not four bytes in length.
  * @include
  */
  public static final int toIntegerPrimitive(byte[] _value) {

    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }

    if(_value.length != 4) {
      throw new IllegalArgumentException("_value must be 8 bytes.");
    }

    int bits = 0;
    bits |= _value[0] << 24;
    bits |= _value[1] << 16;
    bits |= _value[2] << 8;
    bits |= _value[3];

    return bits;
  }

  /**
  * Converts a char primitive to an int primitive.
  * <p>
  * @param _value The char primitive.
  * @return <CODE>int</CODE> - Returns the int whose Unicode code is contained in the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final int toIntegerPrimitive(char _value) {
    return (int)_value;
  }

  /**
  * Converts a Character object to an int primitive.
  * <p>
  * @param _value The Character object.
  * @return <CODE>int</CODE> - Returns the int whose Unicode code is contained in the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final int toIntegerPrimitive(Character _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toIntegerPrimitive(_value.charValue());
  }

  /**
  * Converts a double primitive to an int primitive.
  * <p>
  * @param _value The double primitive.
  * @return <CODE>int</CODE> - Returns the int primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final int toIntegerPrimitive(double _value) {
    if(Double.POSITIVE_INFINITY == _value
      || Double.NEGATIVE_INFINITY == _value
      || Double.NaN == _value
      || _value > STCTypeConverter.MAX_INT
      || _value < STCTypeConverter.MIN_INT
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return (int)_value;
  }

  /**
  * Converts a Double object to an int primitive.
  * <p>
  * @param _value The Double object.
  * @return <CODE>int</CODE> - Returns the int primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final int toIntegerPrimitive(Double _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toIntegerPrimitive(_value.doubleValue());
  }

  /**
  * Converts an Integer object to an int primitive.
  * <p>
  * @param _value The Integer object.
  * @return <CODE>int</CODE> - Returns the int primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final int toIntegerPrimitive(Integer _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.intValue();
  }

  /**
  * Converts a float primitive to an int primitive.
  * <p>
  * @param _value The float primitive.
  * @return <CODE>int</CODE> - Returns the int primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final int toIntegerPrimitive(float _value) {
    if(Float.POSITIVE_INFINITY == _value
      || Float.NEGATIVE_INFINITY == _value
      || Float.NaN == _value
      || _value > STCTypeConverter.MAX_INT
      || _value < STCTypeConverter.MIN_INT
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return (int)_value;
  }

  /**
  * Converts a Float object to an int primitive.
  * <p>
  * @param _value The Float object.
  * @return <CODE>int</CODE> - Returns the int primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final int toIntegerPrimitive(Float _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toIntegerPrimitive(_value.floatValue());
  }

  /**
  * Converts a long primitive to an int primitive.
  * <p>
  * @param _value The long primitive.
  * @return <CODE>int</CODE> - Returns the int primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final int toIntegerPrimitive(long _value) {
    if(_value > STCTypeConverter.MAX_INT
      || _value < STCTypeConverter.MIN_INT
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return (int)_value;
  }

  /**
  * Converts a Long object to an int primitive.
  * <p>
  * @param _value The Long object.
  * @return <CODE>int</CODE> - Returns the int primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final int toIntegerPrimitive(Long _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.intValue();
  }

  /**
  * Converts a short primitive to an int primitive.
  * <p>
  * @param _value The short primitive.
  * @return <CODE>int</CODE> - Returns the int primitive.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final int toIntegerPrimitive(short _value) {
    return (int)_value;
  }

  /**
  * Converts a Short object to an int primitive.
  * <p>
  * @param _value The Short object.
  * @return <CODE>int</CODE> - Returns the int primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final int toIntegerPrimitive(Short _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.shortValue();
  }

  /**
  * Converts a BigDecimal object to an int primitive.
  * <p>
  * @param _value The BigDecimal object.
  * @return <CODE>int</CODE> - Returns the int primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final int toIntegerPrimitive(BigDecimal _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toIntegerPrimitive(_value.doubleValue());
  }

  /**
  * Converts a String object to an int primitive.
  * <p>
  * @param _value The String object.
  * @return <CODE>int</CODE> - Returns the int primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final int toIntegerPrimitive(String _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    if(0 == _value.length()) {
      throw new IllegalArgumentException(EX_EMPTY_STRING);
    }
    return Integer.parseInt(_value);
  }

  /**
  * Converts a boolean primitive to an Integer object.
  * <p>
  * @param _value The boolean primitive.
  * @return <CODE>Integer</CODE> - Returns the Integer object containing <code><b>1</b></code> if the <code><b>_value</b></code> is true or <code><b>0</b></code> if the <code><b>_value</b></code> is false.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Integer toInteger(boolean _value) {
    return new Integer(toIntegerPrimitive(_value));
  }

  /**
  * Converts a Boolean object to an Integer object.
  * <p>
  * @param _value The Boolean object.
  * @return <CODE>Integer</CODE> - Returns the Integer object containing <code><b>1</b></code> if <code><b>_value</b></code> is true or <code><b>0</b></code> if <code><b>_value</b></code> is false.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Integer toInteger(Boolean _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toInteger(_value.booleanValue());
  }

  /**
  * Converts a byte primitive to an Integer object.
  * <p>
  * @param _value The byte primitive.
  * @return <CODE>Integer</CODE> - Returns the Integer object containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Integer toInteger(byte _value) {
    return new Integer((int)_value);
  }

  /**
  * Converts a Byte object to an Integer object.
  * <p>
  * @param _value The Byte object.
  * @return <CODE>Integer</CODE> - Returns the Integer containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Integer toInteger(Byte _value) {
    return toInteger(_value.byteValue());
  }

  /**
  * Converts a four byte array to an Integer object. The most significant bits are in element zero; the least significant are in element three.
  * <p>
  * @param _value The four byte array.
  * @return <CODE>Integer</CODE> - Returns the Integer object.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null or is not four bytes in length.
  * @include
  */
  public static final Integer toInteger(byte[] _value) {
    return new Integer(toIntegerPrimitive(_value));
  }

  /**
  * Converts a char primitive to an Integer object.
  * <p>
  * @param _value The char primitive.
  * @return <CODE>Integer</CODE> - Returns the Integer object.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Integer toInteger(char _value) {
    return new Integer((int)_value);
  }

  /**
  * Converts a Character object to an Integer object.
  * <p>
  * @param _value The Character object.
  * @return <CODE>Integer</CODE> - Returns the Integer object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Integer toInteger(Character _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toInteger(_value.charValue());
  }

  /**
  * Converts a double primitive to an Integer object.
  * <p>
  * @param _value The double primitive.
  * @return <CODE>Integer</CODE> - Returns the Integer object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final Integer toInteger(double _value) {
    if(Double.POSITIVE_INFINITY == _value
      || Double.NEGATIVE_INFINITY == _value
      || Double.NaN == _value
      || _value > STCTypeConverter.MAX_INT
      || _value < STCTypeConverter.MIN_INT
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return new Integer((int)_value);
  }

  /**
  * Converts a Double object to an Integer object.
  * <p>
  * @param _value The Double object.
  * @return <CODE>Integer</CODE> - Returns the Integer object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Integer toInteger(Double _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return new Integer(_value.intValue());
  }

  /**
  * Converts a float primitive to an Integer object.
  * <p>
  * @param _value The float primitive.
  * @return <CODE>Integer</CODE> - Returns the Integer object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final Integer toInteger(float _value) {
    if(Float.POSITIVE_INFINITY == _value
      || Float.NEGATIVE_INFINITY == _value
      || Float.NaN == _value
      || _value > STCTypeConverter.MAX_INT
      || _value < STCTypeConverter.MIN_INT
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return new Integer((int)_value);
  }

  /**
  * Converts a Float object to an Integer object.
  * <p>
  * @param _value The Float object.
  * @return <CODE>Integer</CODE> - Returns the Integer object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Integer toInteger(Float _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return new Integer(_value.intValue());
  }

  /**
  * Converts an int primitive to an Integer object.
  * <p>
  * @param _value The int primitive.
  * @return <CODE>Integer</CODE> - Returns the Integer object containing the <code><b>_value</b></code>.
  * @include
  */
  public static final Integer toInteger(int _value) {
    return new Integer(_value);
  }

  /**
  * Converts a long primitive to an Integer object.
  * <p>
  * @param _value The long primitive.
  * @return <CODE>Integer</CODE> - Returns the Integer object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the results.
  * @include
  */
  public static final Integer toInteger(long _value) {
    if(_value > STCTypeConverter.MAX_INT
      || _value < STCTypeConverter.MIN_INT
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return new Integer((int)_value);
  }

  /**
  * Converts a Long object to an Integer object.
  * <p>
  * @param _value The Long object.
  * @return <CODE>Integer</CODE> - Returns the Integer object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Integer toInteger(Long _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return new Integer(_value.intValue());
  }

  /**
  * Converts a short primitive to an Integer object.
  * <p>
  * @param _value The short primitive.
  * @return <CODE>Integer</CODE> - Returns the Integer object containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Integer toInteger(short _value) {
    return new Integer((int)_value);
  }

  /**
  * Converts a Short object to an Integer object.
  * <p>
  * @param _value The Short object.
  * @return <CODE>Integer</CODE> - Returns the Integer object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Integer toInteger(Short _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return new Integer(_value.intValue());
  }

  /**
  * Converts a BigDecimal object to an Integer object.
  * <p>
  * @param _value The BigDecimal object.
  * @return <CODE>Integer</CODE> - Returns the Integer object containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Integer toInteger(BigDecimal _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toInteger(_value.doubleValue());
  }

  /**
  * Converts a String object to an Integer object.
  * <p>
  * @param _value The String object.
  * @return <CODE>Integer</CODE> - Returns the Integer object.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Integer toInteger(String _value)
    throws IllegalArgumentException
  {
    return new Integer(toIntegerPrimitive(_value));
  }

  /**
  * Converts a boolean primitive to a long primitive.
  * <p>
  * @param _value The boolean primitive.
  * @return <CODE>long</CODE> - Returns <code><b>1</b></code> if the <code><b>_value</b></code> is <b>true</b> or <code><b>0</b></code> if the <code><b>_value</b></code> is <b>false</b>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final long toLongPrimitive(boolean _value) {
    return _value?1L:0L;
  }

  /**
  * Converts a Boolean object to a long primitive.
  * <p>
  * @param _value The Boolean object.
  * @return <CODE>long</CODE> - Returns <code><b>1</b></code> if the <code><b>_value</b></code> is <b>true</b> or <code><b>0</b></code> if the <code><b>_value</b></code> is <b>false</b>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final long toLongPrimitive(Boolean _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toLongPrimitive(_value.booleanValue());
  }

  /**
  * Converts a byte primitive to a long primitive.
  * <p>
  * @param _value The byte primitive.
  * @return <CODE>long</CODE> - Returns the long value.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final long toLongPrimitive(byte _value) {
    return (long)_value;
  }

  /**
  * Converts a Byte object to a long primitive.
  * <p>
  * @param _value The Byte object.
  * @return <CODE>long</CODE> - Returns the long object.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final long toLongPrimitive(Byte _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toLongPrimitive(_value.byteValue());
  }

  /**
  * Converts an eight byte array to a long primitive. The significant bits are in element zero; the least significant are in element seven.
  * <p>
  * @param _value The eight byte array.
  * @return <CODE>long</CODE> - Returns the long contained in the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null or is not eight bytes in length.
  * @include
  */
  public static final long toLongPrimitive(byte[] _value) {

    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }

    if(_value.length != 8) {
      throw new IllegalArgumentException("_value must be 8 bytes.");
    }

    long bits = 0L;
    bits |= _value[0] << 56;
    bits |= _value[1] << 48;
    bits |= _value[2] << 40;
    bits |= _value[3] << 32;
    bits |= _value[4] << 24;
    bits |= _value[5] << 16;
    bits |= _value[6] << 8;
    bits |= _value[7];

    return bits;
  }

  /**
  * Converts a char primitive to a long primitive.
  * <p>
  * @param _value The char primitive.
  * @return <CODE>long</CODE> - Returns the long whose Unicode code is contained in the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final long toLongPrimitive(char _value) {
    return (long)_value;
  }

  /**
  * Converts a Character object to a long primitive.
  * <p>
  * @param _value The Character object.
  * @return <CODE>long</CODE> - Returns the long whose Unicode code is contained in the <code><b>_value</b></code>
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final long toLongPrimitive(Character _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toLongPrimitive(_value.charValue());
  }

  /**
  * Converts a double primitive to a long primitive.
  * <p>
  * @param _value The double primitive.
  * @return <CODE>long</CODE> - Returns the long primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final long toLongPrimitive(double _value) {
    if(Double.POSITIVE_INFINITY == _value
      || Double.NEGATIVE_INFINITY == _value
      || Double.NaN == _value
      || _value > STCTypeConverter.MAX_LONG
      || _value < STCTypeConverter.MIN_LONG
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return (long)_value;
  }

  /**
  * Converts a Double object to a long primitive.
  * <p>
  * @param _value The Double object.
  * @return <CODE>long</CODE> - Returns the long primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final long toLongPrimitive(Double _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toLongPrimitive(_value.doubleValue());
  }

  /**
  * Converts a float primitive to a long primitive.
  * <p>
  * @param _value The float primitive.
  * @return <CODE>long</CODE> - Returns the long primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final long toLongPrimitive(float _value) {
    if(Float.POSITIVE_INFINITY == _value
      || Float.NEGATIVE_INFINITY == _value
      || Float.NaN == _value
      || _value > STCTypeConverter.MAX_LONG
      || _value < STCTypeConverter.MIN_LONG
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return (long)_value;
  }

  /**
  * Converts a Float object to a long primitive.
  * <p>
  * @param _value The Float object.
  * @return <CODE>long</CODE> - Returns the long primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final long toLongPrimitive(Float _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toLongPrimitive(_value.floatValue());
  }

  /**
  * Converts an int primitive to a long primitive.
  * <p>
  * @param _value The int primitive.
  * @return <CODE>long</CODE> - Returns the long primitive.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final long toLongPrimitive(int _value) {
    return (long)_value;
  }

  /**
  * Converts an Integer object to a long primitive.
  * <p>
  * @param _value The Integer object.
  * @return <CODE>long</CODE> - Returns the long primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final long toLongPrimitive(Integer _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.longValue();
  }

  /**
  * Converts a Long object to a long primitive.
  * <p>
  * @param _value The Long object.
  * @return <CODE>long</CODE> - Returns the long primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final long toLongPrimitive(Long _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.longValue();
  }

  /**
  * Converts a short primitive to a long primitive.
  * <p>
  * @param _value The short primitive.
  * @return <CODE>long</CODE> - Returns the long primitive.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final long toLongPrimitive(short _value) {
    return (long)_value;
  }

  /**
  * Converts a Short object to a long primitive.
  * <p>
  * @param _value The Short object.
  * @return <CODE>long</CODE> - Returns the long primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final long toLongPrimitive(Short _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.shortValue();
  }

  /**
  * Converts a BigDecimal object to a long primitive.
  * <p>
  * @param _value The BigDecimal object.
  * @return <CODE>long</CODE> - Returns the long primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final long toLongPrimitive(BigDecimal _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toLongPrimitive(_value.doubleValue());
  }

  /**
  * Converts a String object to a long primitive.
  * <p>
  * @param _value The String object.
  * @return <CODE>long</CODE> - Returns the long primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @see Long#parseLong(String)
  * @include
  */
  public static final long toLongPrimitive(String _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    if(0 == _value.length()) {
      throw new IllegalArgumentException(EX_EMPTY_STRING);
    }
    return Long.parseLong(_value);
  }

  /**
  * Converts a boolean primitive to a Long object.
  * <p>
  * @param _value The boolean primitive.
  * @return <CODE>long</CODE> - Returns the Long object containing <code><b>1</b></code> if the <code><b>_value</b></code> is <b>true</b> or <code><b>0</b></code> if the <code><b>_value</b></code> is <b>false</b>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Long toLong(boolean _value) {
    return new Long(toLongPrimitive(_value));
  }

  /**
  * Converts a Boolean object to a Long object.
  * <p>
  * @param _value The Boolean object.
  * @return <CODE>long</CODE> - Returns the Long object containing <code><1>1</b></code> if the <code><b>_value</b></code> is <b>true</b> or <code><b>0</b></code> if the <code><b>_value</b></code> is <b>false</b>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Long toLong(Boolean _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toLong(_value.booleanValue());
  }

  /**
  * Converts a byte primitive to a Long object.
  * <p>
  * @param _value The byte primitive.
  * @return <CODE>long</CODE> - Returns the Long containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Long toLong(byte _value) {
    return new Long((long)_value);
  }

  /**
  * Converts a Byte object to a Long object.
  * <p>
  * @param _value The Byte object.
  * @return <CODE>long</CODE> - Returns the Long containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Long toLong(Byte _value) {
    return toLong(_value.byteValue());
  }

  /**
  * Converts an eight byte array to a Long object. The most significant bits are in element zero; the least significant are in element seven.
  * <p>
  * @param _value The eight byte array.
  * @return <CODE>long</CODE> - Returns the Long object.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null or is not eight bytes in length.
  * @include
  */
  public static final Long toLong(byte[] _value) {
    return new Long(toLongPrimitive(_value));
  }

  /**
  * Converts a char primitive to a Long object.
  * <p>
  * @param _value The char primitive.
  * @return <CODE>long</CODE> - Returns the Long containing the Unicode code of <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Long toLong(char _value) {
    return new Long((long)_value);
  }

  /**
  * Converts a Character object to a Long object.
  * <p>
  * @param _value The Character object.
  * @return <CODE>long</CODE> - Returns the Long containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Long toLong(Character _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toLong(_value.charValue());
  }

  /**
  * Converts a double primitive to a Long object.
  * <p>
  * @param _value The double primitive.
  * @return <CODE>long</CODE> - Returns the Long containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Long toLong(double _value) {
    return new Long((long)_value);
  }

  /**
  * Converts a Double object to a Long object.
  * <p>
  * @param _value The Double object.
  * @return <CODE>long</CODE> - Returns the Long containing the <code><b>_value</b></code>
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Long toLong(Double _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return new Long(_value.longValue());
  }

  /**
  * Converts a float primitive to a Long object.
  * <p>
  * @param _value The float primitive.
  * @return <CODE>long</CODE> - Returns the Long containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Long toLong(float _value) {
    return new Long((long)_value);
  }

  /**
  * Converts a Float object to a Long object.
  * <p>
  * @param _value The Float object.
  * @return <CODE>long</CODE> - Returns the Long containing the <code><b>_value</b></code>
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Long toLong(Float _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return new Long(_value.longValue());
  }

  /**
  * Converts an int primitive to a Long object.
  * <p>
  * @param _value The int primitive.
  * @return <CODE>long</CODE> - Returns the Long containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Long toLong(int _value) {
    return new Long((long)_value);
  }

  /**
  * Converts an Integer object to a Long object.
  * <p>
  * @param _value The Integer object.
  * @return <CODE>long</CODE> - Returns the Long containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Long toLong(Integer _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return new Long(_value.longValue());
  }

  /**
  * Converts a long primitive to a Long object.
  * <p>
  * @param _value The long primitive.
  * @return <CODE>long</CODE> - Returns the Long containing the <code><b>_value</b></code>.
  * @include
  */
  public static final Long toLong(long _value) {
    return new Long(_value);
  }

  /**
  * Converts a short primitive to a Long object.
  * <p>
  * @param _value The short primitive.
  * @return <CODE>long</CODE> - Returns the Long containing the <code><b>_value</b></code>.
  * @include
  */
  public static final Long toLong(short _value) {
    return new Long((long)_value);
  }

  /**
  * Converts a Short object to a Long object.
  * <p>
  * @param _value The Short object.
  * @return <CODE>long</CODE> - Returns the Long containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Long toLong(Short _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return new Long(_value.longValue());
  }

  /**
  * Converts a BigDecimal object to a Long object.
  * <p>
  * @param _value The BigDecimal object.
  * @return <CODE>long</CODE> - Returns the Long containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Long toLong(BigDecimal _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toLong(_value.doubleValue());
  }

  /**
  * Converts String object to a Long object.
  * <p>
  * @param _value The String object.
  * @return <CODE>long</CODE> - Returns the Long containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Long toLong(String _value)
    throws IllegalArgumentException
  {
    return new Long(toLongPrimitive(_value));
  }

  /**
  * Converts a boolean primitive to a short primitive.
  * <p>
  * @param _value The boolean primitive.
  * @return <CODE>short</CODE> - Returns <code><b>1</b></code> if the <code><b>_value</b></code> is <b>true</b> or <code><b>0</b></code> is <b>false</b>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final short toShortPrimitive(boolean _value) {
    return (short)(_value?1:0);
  }

  /**
  * Converts a Boolean object to a short primitive.
  * <p>
  * @param _value The Boolean object.
  * @return <CODE>short</CODE> - Returns <code><b>1</b></code> if the <code><b>_value</b></code> is <b>true</b> or <code><b>0</b></code> if the <code><b>_value</b></code> is <b>false</b>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final short toShortPrimitive(Boolean _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toShortPrimitive(_value.booleanValue());
  }

  /**
  * Converts a byte primitive to a short primitive.
  * <p>
  * @param _value The byte primitive.
  * @return <CODE>short</CODE> - Returns the short value.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final short toShortPrimitive(byte _value) {
    return (short)_value;
  }

  /**
  * Converts a Byte object to a short primitive.
  * <p>
  * @param _value The Byte object.
  * @return <CODE>short</CODE> - Returns the short value.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final short toShortPrimitive(Byte _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toShortPrimitive(_value.byteValue());
  }

  /**
  * Converts a two byte array to a short primitive. The most significant bits are in element zero; the least significant are in element one.
  * <p>
  * @param _value The two byte array.
  * @return <CODE>short</CODE> - Returns the short contained in the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null or is not two bytes in length.
  * @include
  */
  public static final short toShortPrimitive(byte[] _value) {

    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }

    if(_value.length != 2) {
      throw new IllegalArgumentException("_value must be 2 bytes.");
    }

    short bits = 0;
    bits |= _value[0] << 8;
    bits |= _value[1];

    return bits;
  }

  /**
  * Converts a char primitive to a short primitive.
  * <p>
  * @param _value The char primitive.
  * @return <CODE>short</CODE> - Returns the Unicode value of the code contained in the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final short toShortPrimitive(char _value) {
    return (short)_value;
  }

  /**
  * Converts a Character object to a short primitive.
  * <p>
  * @param _value The Character object.
  * @return <CODE>short</CODE> - Returns the Unicode value of the code contained in the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final short toShortPrimitive(Character _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toShortPrimitive(_value.charValue());
  }

  /**
  * Converts a double primitive to a short primitive.
  * <p>
  * @param _value The double primitive.
  * @return <CODE>short</CODE> - Returns the short primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final short toShortPrimitive(double _value) {
    if(Double.POSITIVE_INFINITY == _value
      || Double.NEGATIVE_INFINITY == _value
      || Double.NaN == _value
      || _value > STCTypeConverter.MAX_SHORT
      || _value < STCTypeConverter.MIN_SHORT
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return (short)_value;
  }

  /**
  * Converts a Double object to a short primitive.
  * <p>
  * @param _value The Double object.
  * @return <CODE>short</CODE> - Returns the short primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final short toShortPrimitive(Double _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toShortPrimitive(_value.doubleValue());
  }

  /**
  * Converts a float primitive to a short primitive.
  * <p>
  * @param _value The float primitive.
  * @return <CODE>short</CODE> - Returns the short primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final short toShortPrimitive(float _value) {
    if(Float.POSITIVE_INFINITY == _value
      || Float.NEGATIVE_INFINITY == _value
      || Float.NaN == _value
      || _value > STCTypeConverter.MAX_SHORT
      || _value < STCTypeConverter.MIN_SHORT
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return (short)_value;
  }

  /**
  * Converts a Float object to a short primitive.
  * <p>
  * @param _value The Float object.
  * @return <CODE>short</CODE> - Returns the short primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final short toShortPrimitive(Float _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toShortPrimitive(_value.floatValue());
  }

  /**
  * Converts an int primitive to a short primitive.
  * <p>
  * @param _value The int primitive.
  * @return <CODE>short</CODE> - Returns the short primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final short toShortPrimitive(int _value) {
    if(_value > STCTypeConverter.MAX_SHORT
      || _value < STCTypeConverter.MIN_SHORT
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return (short)_value;
  }

  /**
  * Converts an Integer object to a short primitive.
  * <p>
  * @param _value The Integer object.
  * @return <CODE>short</CODE> - Returns the short primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final short toShortPrimitive(Integer _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.shortValue();
  }

  /**
  * Converts a long primitive to a short primitive.
  * <p>
  * @param _value The long primitive.
  * @return <CODE>short</CODE> - Returns the short primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final short toShortPrimitive(long _value) {
    if(_value > STCTypeConverter.MAX_SHORT
      || _value < STCTypeConverter.MIN_SHORT
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return (short)_value;
  }

  /**
  * Converts a Long object to a short primitive.
  * <p>
  * @param _value The Long object.
  * @return <CODE>short</CODE> - Returns the short primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final short toShortPrimitive(Long _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.shortValue();
  }

  /**
  * Converts a Short object to a short primitive.
  * <p>
  * @param _value The Short object.
  * @return <CODE>short</CODE> - Returns the short primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final short toShortPrimitive(Short _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.shortValue();
  }

  /**
  * Converts a BigDecimal object to a short primitive.
  * <p>
  * @param _value The BigDecimal object.
  * @return <CODE>short</CODE> - Returns the short primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final short toShortPrimitive(BigDecimal _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toShortPrimitive(_value.doubleValue());
  }

  /**
  * Converts a string object to a short primitive.
  * <p>
  * @param _value The String object.
  * @return <CODE>short</CODE> - Returns the short primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @see Short#parseShort(String)
  * @include
  */
  public static final short toShortPrimitive(String _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    if(0 == _value.length()) {
      throw new IllegalArgumentException(EX_EMPTY_STRING);
    }
    return Short.parseShort(_value);
  }

  /**
  * Converts a boolean primitive to a Short object.
  * <p>
  * @param _value The boolean primitive.
  * @return <CODE>Short</CODE> - Returns the Short object containing <code><b>1</b></code> if the <code><b>_value</b></code> is <b>true</b> or <code><b>0</b></code> if the <code><b>_value</b></code> is <b>false</b>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Short toShort(boolean _value) {
    return new Short(toShortPrimitive(_value));
  }

  /**
  * Converts a boolean object to a Short object.
  * <p>
  * @param _value The Boolean object.
  * @return <CODE>Short</CODE> - Returns the Short object containing <code><b>1</b></code> if the <code><b>_value</b></code> is <b>true</b> or <code><b>0</b></code> if the <code><b>_value</b></code> is <b>false</b>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Short toShort(Boolean _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toShort(_value.booleanValue());
  }

  /**
  * Converts a byte primitive to a Short object.
  * <p>
  * @param _value The byte primitive.
  * @return <CODE>Short</CODE> - Returns the Short containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Short toShort(byte _value) {
    return new Short((short)_value);
  }

  /**
  * Converts a Byte object to a Short object.
  * <p>
  * @param _value The Byte object.
  * @return <CODE>Short</CODE> - Returns the Short containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Short toShort(Byte _value) {
    return toShort(_value.byteValue());
  }

  /**
  * Converts a two byte array to a Short object. The most significant bits are in element zero; the least significant are in element one.
  * <p>
  * @param _value The two byte array.
  * @return <CODE>Short</CODE> - Returns the Short object.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null or is not two bytes in length.
  * @include
  */
  public static final Short toShort(byte[] _value) {
    return new Short(toShortPrimitive(_value));
  }

  /**
  * Converts a char primitive to a Short object.
  * <p>
  * @param _value The char primitive.
  * @return <CODE>Short</CODE> - Returns the Short containing the Unicode code of the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Short toShort(char _value) {
    return new Short((short)_value);
  }

  /**
  * Converts a Character object to a Short object.
  * <p>
  * @param _value The Character object.
  * @return <CODE>Short</CODE> - Returns the Short containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Short toShort(Character _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toShort(_value.charValue());
  }

  /**
  * Converts a double primitive to a Short object.
  * <p>
  * @param _value The double primitive.
  * @return <CODE>Short</CODE> - Returns the double primitive.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final Short toShort(double _value) {
    if(Double.POSITIVE_INFINITY == _value
      || Double.NEGATIVE_INFINITY == _value
      || Double.NaN == _value
      || _value > STCTypeConverter.MAX_SHORT
      || _value < STCTypeConverter.MIN_SHORT
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return new Short((short)_value);
  }

  /**
  * Converts a Double object to a Short object.
  * <p>
  * @param _value The Double object.
  * @return <CODE>Short</CODE> - Returns the Short containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Short toShort(Double _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return new Short(_value.shortValue());
  }

  /**
  * Converts a float primitive to a Short object.
  * <p>
  * @param _value The float primitive.
  * @return <CODE>Short</CODE> - Returns the Short containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final Short toShort(float _value) {
    if(Float.POSITIVE_INFINITY == _value
      || Float.NEGATIVE_INFINITY == _value
      || Float.NaN == _value
      || _value > STCTypeConverter.MAX_SHORT
      || _value < STCTypeConverter.MIN_SHORT
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return new Short((short)_value);
  }

  /**
  * Converts a Float object to a Short object.
  * <p>
  * @param _value The Float object.
  * @return <CODE>Short</CODE> - Returns the Short containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Short toShort(Float _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return new Short(_value.shortValue());
  }

  /**
  * Converts an int primitive to a Short object.
  * <p>
  * @param _value The int primitive.
  * @return <CODE>Short</CODE> - Returns the Short containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range fo the result.
  * @include
  */
  public static final Short toShort(int _value) {
    if(_value > STCTypeConverter.MAX_SHORT
      || _value < STCTypeConverter.MIN_SHORT
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return new Short((short)_value);
  }

  /**
  * Converts an Integer primitive to a Short object.
  * <p>
  * @param _value The Integer primitive.
  * @return <CODE>Short</CODE> - Returns the Short containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Short toShort(Integer _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return new Short(_value.shortValue());
  }

  /**
  * Converts a long primitive to a Short object.
  * <p>
  * @param _value The long primitive.
  * @return <CODE>Short</CODE> - Returns the Short containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is outside the range of the result.
  * @include
  */
  public static final Short toShort(long _value) {
    if(_value > STCTypeConverter.MAX_SHORT
      || _value < STCTypeConverter.MIN_SHORT
    ) {
      throw new IllegalArgumentException(EX_OUT_OF_RANGE + _value);
    }
    return new Short((short)_value);
  }

  /**
  * Converts a Long object to a Short object.
  * <p>
  * @param _value The Long object.
  * @return <CODE>Short</CODE> - Returns the Short containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Short toShort(Long _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return new Short(_value.shortValue());
  }

  /**
  * Converts a short primitive to a Short object.
  * <p>
  * @param _value The short primitive.
  * @return <CODE>Short</CODE> - Returns the Short containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final Short toShort(short _value) {
    return new Short(_value);
  }

  /**
  * Converts a BigDecimal object to a Short object.
  * <p>
  * @param _value The BigDecimal.
  * @return <CODE>Short</CODE> - Returns the Short containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Short toShort(BigDecimal _value)
    throws IllegalArgumentException
  {
    return new Short(toShortPrimitive(_value));
  }

  /**
  * Converts a String object to a Short object.
  * <p>
  * @param _value The String object.
  * @return <CODE>Short</CODE> - Returns the Short containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final Short toShort(String _value)
    throws IllegalArgumentException
  {
    return new Short(toShortPrimitive(_value));
  }

  /**
  * Converts a boolean primitive to a String object.
  * <p>
  * @param _value The boolean primitive.
  * @return <CODE>String</CODE> - Returns the String object containing <code><b><a href="#TRUE_STRING">TRUE_STRING</a></b></code> if <b>true</b> or <code><b><a href="#FALSE_STRING">FALSE_STRING</a></b></code> if <b>false</b>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final String toString(boolean _value) {
    return _value?TRUE_STRING:FALSE_STRING;
  }

  /**
  * Converts a Boolean object to a String object.
  * <p>
  * @param _value The Boolean object.
  * @return <CODE>String</CODE> - Returns the String object containing <code><b><a href="#TRUE_STRING">TRUE_STRING</a></b></code> if <b>true</b> or <code><b><a href="#FALSE_STRING">FALSE_STRING</a></b></code> if <b>false</b>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final String toString(Boolean _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return toString(_value.booleanValue());
  }

  /**
  * Converts a byte primitive to a String object.
  * <p>
  * @param _value The byte primitive.
  * @return <CODE>String</CODE> - Returns the String containing the <code><b>_value</b></code>.
  * @include
  */
  public static final String toString(byte _value) {
    return String.valueOf(_value);
  }

  /**
  * Converts a Byte object to a String object.
  * <p>
  * @param _value The Byte object.
  * @return <CODE>String</CODE> - Returns the String containing the <code><b>_value</b></code>.
  * @include
  */
  public static final String toString(Byte _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.toString();
  }

  /**
  * Converts a byte array to a String object.
  * <p>
  * @param _value The byte array.
  * @return <CODE>String</CODE> - Returns the String object.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final String toString(byte[] _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return new String(_value);
  }

  /**
  * Converts a byte array to a String using the specified encoding.
  * <p>
  * @param _value The byte array.
  * @param _encoding The encoding scheme.
  * @return <CODE>String</CODE> - Returns the String object.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @exception UnsupportedEncodingException Thrown if <code><b>_encoding</b></code> is not a supported encoding scheme.
  * @include
  */
  public static final String toString(byte[] _value, String _encoding)
    throws java.io.UnsupportedEncodingException
  {
    if(null == _value || null == _encoding) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return new String(_value, _encoding);
  }

  /**
  * Converts a char primitive to a String object.
  * <p>
  * @param _value The char primitive.
  * @return <CODE>String</CODE> - Returns a one character String containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final String toString(char _value) {
    return String.valueOf(_value);
  }

  /**
  * Converts a Character object to a String object.
  * <p>
  * @param _value The Character object.
  * @return <CODE>String</CODE> - Returns the one character String containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final String toString(Character _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.toString();
  }

  /**
  * Converts a double primitive to a String object.
  * <p>
  * @param _value The double primitive.
  * @return <CODE>String</CODE> - Returns the String containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final String toString(double _value) {
    return String.valueOf(_value);
  }

  /**
  * Converts a Double object to a String object.
  * <p>
  * @param _value The Double object.
  * @return <CODE>String</CODE> - Returns the String containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final String toString(Double _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.toString();
  }

  /**
  * Converts a float primitive to a String object.
  * <p>
  * @param _value The float primitive.
  * @return <CODE>String</CODE> - Returns the String containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final String toString(float _value) {
    return String.valueOf(_value);
  }

  /**
  * Converts a Float object to a String object.
  * <p>
  * @param _value The Float object.
  * @return <CODE>String</CODE> - Returns the String containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final String toString(Float _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.toString();
  }

  /**
  * Converts an int primitive to a String object.
  * <p>
  * @param _value The int primitive.
  * @return <CODE>String</CODE> - Returns the String containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final String toString(int _value) {
    return String.valueOf(_value);
  }

  /**
  * Converts an Integer object to a String object.
  * <p>
  * @param _value The Integer object.
  * @return <CODE>String</CODE> - Returns the String containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final String toString(Integer _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.toString();
  }

  /**
  * Converts a long primitive to a String object.
  * <p>
  * @param _value The long primitive.
  * @return <CODE>String</CODE> - Returns the String containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final String toString(long _value) {
    return String.valueOf(_value);
  }

  /**
  * Converts a Long object to a String object.
  * <p>
  * @param _value The Long object.
  * @return <CODE>String</CODE> - Returns the String containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final String toString(Long _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.toString();
  }

  /**
  * Converts a short primitive to a String object.
  * <p>
  * @param _value The short primitive.
  * @return <CODE>String</CODE> - Returns the String containing the <code><b>_value</b></code>.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final String toString(short _value) {
    return String.valueOf(_value);
  }

  /**
  * Converts a Short object to a String object.
  * <p>
  * @param _value The Short object.
  * @return <CODE>String</CODE> - Returns the String containing the <code><b>_value</b></code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final String toString(Short _value)
    throws IllegalArgumentException
  {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.toString();
  }

  /**
  * Converts a BigDecimal object to a String object.
  * <p>
  * @param _value The BigDecimal object.
  * @return <CODE>String</CODE> - Returns the string representation of the BigDecimal object.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final String toString(BigDecimal _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.toString();
  }

  /**
  * Converts a <code>java.sql.Timestamp</code> to a String object.
  * <p>
  * @param _value The Timestamp object.
  * @return <CODE>String</CODE> - Returns the String containing the timestamp format <code>yyyy-mm-dd hh:mm:ss.fffffffff</code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final String toString(Timestamp _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.toString();
  }

  /**
  * Converts a <code>java.sql.Time</code> to a String object.
  * <p>
  * @param _value The Time object.
  * @return <CODE>String</CODE> - Returns the String containing the time format <code>hh:mm:ss</code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final String toString(Time _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.toString();
  }

  /**
  * Converts a <code>java.sql.Date</code> to a String object.
  * <p>
  * @param _value The Date object.
  * @return <CODE>String</CODE> - Returns the String containing the data format <code>yyy-mm-dd</code>.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final String toString(Date _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return _value.toString();
  }

  /**
  * Converts a byte primitive to a BigDecimal object.
  * <p>
  * @param _value The byte primitive value.
  * @return <CODE>BigDecimal</CODE> - Returns the BigDecimal value.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final BigDecimal toBigDecimal(byte _value) {
    return new BigDecimal((double)_value);
  }

  /**
  * Converts a short primitive to a <code>java.math.BigDecimal</code> object.
  * <p>
  * @param _value The short primitive.
  * @return <CODE>BigDecimal</CODE> - Returns the BigDecimal value.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final BigDecimal toBigDecimal(short _value) {
    return new BigDecimal((double)_value);
  }

  /**
  * Converts an int primitive to a BigDecimal object.
  * <p>
  * @param _value The int primitive.
  * @return <CODE>BigDecimal</CODE> - Returns the BigDecimal value.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final BigDecimal toBigDecimal(int _value) {
    return new BigDecimal((double)_value);
  }

  /**
  * Converts a long primitive to a <code>java.math.BigDecimal</code> object.
  * <p>
  * @param _value The long value.
  * @return <CODE>BigDecimal</CODE> - Returns the BigDecimal value.
  * @include
  */
  public static final BigDecimal toBigDecimal(long _value) {
    return new BigDecimal((double)_value);
  }

  /**
  * Converts a float primitive to a <code>java.math.BigDecimal</code> object.
  * <p>
  * @param _value The float value.
  * @return <CODE>BigDecimal</CODE> - Returns the BigDecimal value.
  * <DT><B>Throws:</B><DD>None.
  * @include
  */
  public static final BigDecimal toBigDecimal(float _value) {
    return new BigDecimal((double)_value);
  }

  /**
  * Converts a double primitive to a <code>java.math.BigDecimal</code> object.
  * <p>
  * @param _value The double value.
  * @return <CODE>BigDecimal</CODE> - Returns the BigDecimal value.
  * @include
  */
  public static final BigDecimal toBigDecimal(double _value) {
    return new BigDecimal(_value);
  }

  /**
  * Converts a byte array containing a UTF-8 string representation to a BigDecimal object.
  * <p>
  * @param _value The byte array.
  * @return <CODE>BigDecimal</CODE> - Returns the BigDecimal value.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final BigDecimal toBigDecimal(byte[] _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    try {
        return toBigDecimal(new String(_value, "UTF-8"));
    } catch(UnsupportedEncodingException ex) {
        System.err.println("Something is seriously wrong if you don't have UTF-8 encoding!");
        ex.printStackTrace();
    }
    return null;
  }

  /**
  * Converts a Number object to a <code>java.math.BigDecimal</code> object.
  * <p>
  * @param _value The Number object.
  * @return <CODE>BigDecimal</CODE> - Returns the BigDecimal value.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final BigDecimal toBigDecimal(Number _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return new BigDecimal(_value.doubleValue());
  }

  /**
  * Converts a String object to a <code>java.math.BigDecimal</code> object.
  * <p>
  * @param _value The String representation of the BigDecimal.
  * @return <CODE>BigDecimal</CODE> - Returns the BigDecimal represented by the String.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null.
  * @include
  */
  public static final BigDecimal toBigDecimal(String _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return new BigDecimal(_value);
  }

  /**
  * Converts a String object to a <code>java.sql.Timestamp</code> object.
  * <p>
  * @param _value The String containing the timestamp in the format <code>yyyy-mm-dd hh:mm:ss.fffffffff</code>.
  * @return <CODE>Timestamp</CODE> - Returns the Timestamp represented by the String.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null or is not in the proper format.
  * @include
  */
  public static final Timestamp toTimestamp(String _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return Timestamp.valueOf(_value);
  }

  /**
  * Converts a String object to a <code>java.sql.Time</code> object.
  * <p>
  * @param _value The String containing the time in the format <code>hh:mm:ss</code>.
  * @return <CODE>Time</CODE> - Returns the Time represented by the String.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null or is not in the proper format.
  * @include
  */
  public static final Time toTime(String _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return Time.valueOf(_value);
  }

  /**
  * Converts a String object to a <code>java.sql.Date</code> object.
  * <p>
  * @param _value The String containing the date in the format <code>yyyy-mm-dd</code>.
  * @return <CODE>Date</CODE> - Returns the Date represented by the String.
  * @exception IllegalArgumentException Thrown if the <code><b>_value</b></code> is null or is not in the proper format.
  * @include
  */
  public static final Date toDate(String _value) {
    if(null == _value) {
      throw new IllegalArgumentException(EX_NULL_PARAM);
    }
    return Date.valueOf(_value);
  }

  /*
   * following methods test the class.
   *
   * They should be commented out for production.
   */
/*
  private static java.lang.reflect.Method[] methodArray =
    STCTypeConverter.class.getMethods();

  private static Class[] classArray = {
    boolean.class
    ,Boolean.class
    ,byte.class
    ,Byte.class
    ,char.class
    ,Character.class
    ,double.class
    ,Double.class
    ,float.class
    ,Float.class
    ,int.class
    ,Integer.class
    ,long.class
    ,Long.class
    ,short.class
    ,Short.class
    ,String.class
  };

  private static Object[][] paramArray = {

    {
      Boolean.class                     // the class
      ,new Boolean(true)                // valid 1
      ,new Boolean(false)               // valid 2
    },{
      Byte.class                        // the class
      ,new Byte("0")                    // valid 1
      ,new Byte("1")                    // valid 1
      ,null                             // valid/invalid separator
      ,new Byte("65")                   // valid 2
    },{
      Character.class                   // the class
      ,new Character('t')               // valid 1
      ,new Character('f')               // valid 2
      ,null                             // valid/invalid separator
      ,new Character('a')               // should cause toBoolean to fail
    },{
      Double.class                      // the class
      ,new Double(0.0)                  // valid 1
      ,new Double(1.0)                  // valid 2
      ,null                             // valid/invalid separator
      ,new Double(65.0)                 // invalid
      ,new Double(MAX_BYTE+9)           // should cause toByte to fail
      ,new Double(MAX_SHORT+9)          // should cause toShort to fail
      ,new Double(MAX_INT+9)            // should cause toInt to fail
      ,new Double(MAX_LONG+9)           // should cause toLong to fail
    },{
      Float.class                       // the class
      ,new Float(0.0)                   // valid 1
      ,new Float(1.0)                   // valid 2
      ,null                             // valid/invalid separator
      ,new Float(65.0)                  // invalid
      ,new Float(MAX_BYTE+9)            // should cause toByte to fail
      ,new Float(MAX_SHORT+9)           // should cause toShort to fail
      ,new Float(MAX_INT+9)             // should cause toInt to fail
      ,new Float(MAX_LONG+9)            // should cause toLong to fail
    },{
      Integer.class                     // the class
      ,new Integer(0)                   // valid 1
      ,new Integer(1)                   // valid 2
      ,null                             // valid/invalid separator
      ,new Integer(65)                  // invalid
      ,new Integer((int)(MAX_BYTE+9))   // should cause toByte to fail
      ,new Integer((int)(MAX_SHORT+9))  // should cause toShort to fail
    },{
      Long.class                        // the class
      ,new Long(0)                      // valid 1
      ,new Long(1)                      // valid 2
      ,null                             // valid/invalid separator
      ,new Long(65)                     // invalid
      ,new Long((long)(MAX_BYTE+9))     // should cause toByte to fail
      ,new Long((long)(MAX_SHORT+9))    // should cause toShort to fail
      ,new Long((long)(MAX_INT+9))      // should cause toInt to fail
    },{
      Short.class                       // the class
      ,new Short("0")                   // valid 1
      ,new Short("1")                   // valid 2
      ,null                             // valid/invalid separator
      ,new Short("65")                  // invalid
      ,new Short((short)(MAX_BYTE+9))   // should cause toByte to fail
    },{
      String.class                      // the class
      ,null                             // valid/invalid separator
      ,new String("true")               // should cause numeric conversions to fail
      ,new String("false")              // should cause numeric conversions to fail
      ,new String("0.0")                // should cause toBoolean to fail
      ,new String("1.0")                // should cause toBoolean to fail
      ,new String("65.0")               // should cause toBoolean to fail
      ,new String("1E999")              // should cause to<numeric> to fail
    }

  };

  public static void main(String[] _args) {

    boolean verbose = false;
    if(_args.length > 0) {
      verbose = _args[0].equals("-v");
    }

    try {

      System.out.println("*** Validating: " + STCTypeConverter.class);

      makeSureAllCastsAreAvailable(verbose);
      System.out.println();

      testConversions(verbose);
      System.out.println();

    } catch(Exception ex) {
      ex.printStackTrace();
    }

  }

  private static void testConversions(boolean _verbose) {

    for(int xx=0; xx<paramArray.length ;xx++) {
      Object[] paramList = paramArray[xx];
      Class from = (Class)paramList[0];

      for(int yy=0; yy<classArray.length ;yy++) {
        Class to = classArray[yy];

        if(from.equals(to)) {
          continue;
        }

        java.lang.reflect.Method forward = findCast(from, to);
        java.lang.reflect.Method reverse = findCast(to, from);

        boolean validFlag = true;

        for(int zz=1; zz<paramList.length ;zz++) {
          Object parameter = paramList[zz];

          if(null == parameter && validFlag) {
            validFlag = false;
            continue;
          }

          try {

            Object forwardResult = forward.invoke(null, new Object[] {parameter});
            Object reverseResult = reverse.invoke(null, new Object[] {forwardResult});

            if(_verbose) {
              System.out.print(parameter
                              + " => "
                              + forwardResult
                              + " => "
                              + reverseResult
                              + "  "
                              + from
                              + " to "
                              + to
                              );
            }

            if( !parameter.equals(reverseResult) ) {
              if(_verbose) {
                System.out.print(" ***");
              } else {
                System.out.println(parameter
                                  + " => "
                                  + forwardResult
                                  + " => "
                                  + reverseResult
                                  + "  casting "
                                  + from
                                  + " to "
                                  + to
                                  );
                }
            }

            if(_verbose) {
              System.out.println();
            }

          } catch(Exception ex) {
            if(validFlag) {
              System.out.println("*** Failed casting "
                                + from
                                + " to "
                                + to
                                );
              ex.printStackTrace();
            }
          }

        }

      }

    }

  }

  private static void makeSureAllCastsAreAvailable(boolean _verbose) {

    for(int to=0; to<classArray.length ;to++) {
      for(int from=0; from<classArray.length ;from++) {

        // skip unnecessary cases
        if(from == to) continue;

        if(null == findCast(classArray[from], classArray[to])) {
          throw new IllegalStateException("No cast available from "
                                          + classArray[from]
                                          + " to "
                                          + classArray[to]
                                          );

        }

      }

    }

    System.out.println("All casts available");
  }

  public static java.lang.reflect.Method findCast(Class _from, Class _to) {

    for(int xx=0; xx<methodArray.length ;xx++) {

      java.lang.reflect.Method method = methodArray[xx];

      String name = method.getName();
      Class[] parameters = method.getParameterTypes();
      Class returnType = method.getReturnType();

      if(parameters.length == 1
        && name.startsWith("to")
        && parameters[0] == _from
        && returnType == _to
      ) {
        return method;
      }

    }

    return null;
  }
*/

}

