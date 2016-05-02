<<<<<<< HEAD
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3.math;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>Provides extra functionality for Java Number classes.</p>
 *
 * @since 2.0
 * @version $Id$
 */
public class NumberUtils {
    
    /** Reusable Long constant for zero. */
    public static final Long LONG_ZERO = Long.valueOf(0L);
    /** Reusable Long constant for one. */
    public static final Long LONG_ONE = Long.valueOf(1L);
    /** Reusable Long constant for minus one. */
    public static final Long LONG_MINUS_ONE = Long.valueOf(-1L);
    /** Reusable Integer constant for zero. */
    public static final Integer INTEGER_ZERO = Integer.valueOf(0);
    /** Reusable Integer constant for one. */
    public static final Integer INTEGER_ONE = Integer.valueOf(1);
    /** Reusable Integer constant for minus one. */
    public static final Integer INTEGER_MINUS_ONE = Integer.valueOf(-1);
    /** Reusable Short constant for zero. */
    public static final Short SHORT_ZERO = Short.valueOf((short) 0);
    /** Reusable Short constant for one. */
    public static final Short SHORT_ONE = Short.valueOf((short) 1);
    /** Reusable Short constant for minus one. */
    public static final Short SHORT_MINUS_ONE = Short.valueOf((short) -1);
    /** Reusable Byte constant for zero. */
    public static final Byte BYTE_ZERO = Byte.valueOf((byte) 0);
    /** Reusable Byte constant for one. */
    public static final Byte BYTE_ONE = Byte.valueOf((byte) 1);
    /** Reusable Byte constant for minus one. */
    public static final Byte BYTE_MINUS_ONE = Byte.valueOf((byte) -1);
    /** Reusable Double constant for zero. */
    public static final Double DOUBLE_ZERO = Double.valueOf(0.0d);
    /** Reusable Double constant for one. */
    public static final Double DOUBLE_ONE = Double.valueOf(1.0d);
    /** Reusable Double constant for minus one. */
    public static final Double DOUBLE_MINUS_ONE = Double.valueOf(-1.0d);
    /** Reusable Float constant for zero. */
    public static final Float FLOAT_ZERO = Float.valueOf(0.0f);
    /** Reusable Float constant for one. */
    public static final Float FLOAT_ONE = Float.valueOf(1.0f);
    /** Reusable Float constant for minus one. */
    public static final Float FLOAT_MINUS_ONE = Float.valueOf(-1.0f);

    /**
     * <p><code>NumberUtils</code> instances should NOT be constructed in standard programming.
     * Instead, the class should be used as <code>NumberUtils.toInt("6");</code>.</p>
     *
     * <p>This constructor is public to permit tools that require a JavaBean instance
     * to operate.</p>
     */
    public NumberUtils() {
        super();
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Convert a <code>String</code> to an <code>int</code>, returning
     * <code>zero</code> if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, <code>zero</code> is returned.</p>
     *
     * <pre>
     *   NumberUtils.toInt(null) = 0
     *   NumberUtils.toInt("")   = 0
     *   NumberUtils.toInt("1")  = 1
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @return the int represented by the string, or <code>zero</code> if
     *  conversion fails
     * @since 2.1
     */
    public static int toInt(String str) {
        return toInt(str, 0);
    }

    /**
     * <p>Convert a <code>String</code> to an <code>int</code>, returning a
     * default value if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, the default value is returned.</p>
     *
     * <pre>
     *   NumberUtils.toInt(null, 1) = 1
     *   NumberUtils.toInt("", 1)   = 1
     *   NumberUtils.toInt("1", 0)  = 1
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @param defaultValue  the default value
     * @return the int represented by the string, or the default if conversion fails
     * @since 2.1
     */
    public static int toInt(String str, int defaultValue) {
        if(str == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * <p>Convert a <code>String</code> to a <code>long</code>, returning
     * <code>zero</code> if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, <code>zero</code> is returned.</p>
     *
     * <pre>
     *   NumberUtils.toLong(null) = 0L
     *   NumberUtils.toLong("")   = 0L
     *   NumberUtils.toLong("1")  = 1L
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @return the long represented by the string, or <code>0</code> if
     *  conversion fails
     * @since 2.1
     */
    public static long toLong(String str) {
        return toLong(str, 0L);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>long</code>, returning a
     * default value if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, the default value is returned.</p>
     *
     * <pre>
     *   NumberUtils.toLong(null, 1L) = 1L
     *   NumberUtils.toLong("", 1L)   = 1L
     *   NumberUtils.toLong("1", 0L)  = 1L
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @param defaultValue  the default value
     * @return the long represented by the string, or the default if conversion fails
     * @since 2.1
     */
    public static long toLong(String str, long defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * <p>Convert a <code>String</code> to a <code>float</code>, returning
     * <code>0.0f</code> if the conversion fails.</p>
     *
     * <p>If the string <code>str</code> is <code>null</code>,
     * <code>0.0f</code> is returned.</p>
     *
     * <pre>
     *   NumberUtils.toFloat(null)   = 0.0f
     *   NumberUtils.toFloat("")     = 0.0f
     *   NumberUtils.toFloat("1.5")  = 1.5f
     * </pre>
     *
     * @param str the string to convert, may be <code>null</code>
     * @return the float represented by the string, or <code>0.0f</code>
     *  if conversion fails
     * @since 2.1
     */
    public static float toFloat(String str) {
        return toFloat(str, 0.0f);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>float</code>, returning a
     * default value if the conversion fails.</p>
     *
     * <p>If the string <code>str</code> is <code>null</code>, the default
     * value is returned.</p>
     *
     * <pre>
     *   NumberUtils.toFloat(null, 1.1f)   = 1.0f
     *   NumberUtils.toFloat("", 1.1f)     = 1.1f
     *   NumberUtils.toFloat("1.5", 0.0f)  = 1.5f
     * </pre>
     *
     * @param str the string to convert, may be <code>null</code>
     * @param defaultValue the default value
     * @return the float represented by the string, or defaultValue
     *  if conversion fails
     * @since 2.1
     */
    public static float toFloat(String str, float defaultValue) {
      if (str == null) {
          return defaultValue;
      }     
      try {
          return Float.parseFloat(str);
      } catch (NumberFormatException nfe) {
          return defaultValue;
      }
    }

    /**
     * <p>Convert a <code>String</code> to a <code>double</code>, returning
     * <code>0.0d</code> if the conversion fails.</p>
     *
     * <p>If the string <code>str</code> is <code>null</code>,
     * <code>0.0d</code> is returned.</p>
     *
     * <pre>
     *   NumberUtils.toDouble(null)   = 0.0d
     *   NumberUtils.toDouble("")     = 0.0d
     *   NumberUtils.toDouble("1.5")  = 1.5d
     * </pre>
     *
     * @param str the string to convert, may be <code>null</code>
     * @return the double represented by the string, or <code>0.0d</code>
     *  if conversion fails
     * @since 2.1
     */
    public static double toDouble(String str) {
        return toDouble(str, 0.0d);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>double</code>, returning a
     * default value if the conversion fails.</p>
     *
     * <p>If the string <code>str</code> is <code>null</code>, the default
     * value is returned.</p>
     *
     * <pre>
     *   NumberUtils.toDouble(null, 1.1d)   = 1.1d
     *   NumberUtils.toDouble("", 1.1d)     = 1.1d
     *   NumberUtils.toDouble("1.5", 0.0d)  = 1.5d
     * </pre>
     *
     * @param str the string to convert, may be <code>null</code>
     * @param defaultValue the default value
     * @return the double represented by the string, or defaultValue
     *  if conversion fails
     * @since 2.1
     */
    public static double toDouble(String str, double defaultValue) {
      if (str == null) {
          return defaultValue;
      }
      try {
          return Double.parseDouble(str);
      } catch (NumberFormatException nfe) {
          return defaultValue;
      }
    }

     //-----------------------------------------------------------------------
     /**
     * <p>Convert a <code>String</code> to a <code>byte</code>, returning
     * <code>zero</code> if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, <code>zero</code> is returned.</p>
     *
     * <pre>
     *   NumberUtils.toByte(null) = 0
     *   NumberUtils.toByte("")   = 0
     *   NumberUtils.toByte("1")  = 1
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @return the byte represented by the string, or <code>zero</code> if
     *  conversion fails
     * @since 2.5
     */
    public static byte toByte(String str) {
        return toByte(str, (byte) 0);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>byte</code>, returning a
     * default value if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, the default value is returned.</p>
     *
     * <pre>
     *   NumberUtils.toByte(null, 1) = 1
     *   NumberUtils.toByte("", 1)   = 1
     *   NumberUtils.toByte("1", 0)  = 1
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @param defaultValue  the default value
     * @return the byte represented by the string, or the default if conversion fails
     * @since 2.5
     */
    public static byte toByte(String str, byte defaultValue) {
        if(str == null) {
            return defaultValue;
        }
        try {
            return Byte.parseByte(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * <p>Convert a <code>String</code> to a <code>short</code>, returning
     * <code>zero</code> if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, <code>zero</code> is returned.</p>
     *
     * <pre>
     *   NumberUtils.toShort(null) = 0
     *   NumberUtils.toShort("")   = 0
     *   NumberUtils.toShort("1")  = 1
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @return the short represented by the string, or <code>zero</code> if
     *  conversion fails
     * @since 2.5
     */
    public static short toShort(String str) {
        return toShort(str, (short) 0);
    }

    /**
     * <p>Convert a <code>String</code> to an <code>short</code>, returning a
     * default value if the conversion fails.</p>
     *
     * <p>If the string is <code>null</code>, the default value is returned.</p>
     *
     * <pre>
     *   NumberUtils.toShort(null, 1) = 1
     *   NumberUtils.toShort("", 1)   = 1
     *   NumberUtils.toShort("1", 0)  = 1
     * </pre>
     *
     * @param str  the string to convert, may be null
     * @param defaultValue  the default value
     * @return the short represented by the string, or the default if conversion fails
     * @since 2.5
     */
    public static short toShort(String str, short defaultValue) {
        if(str == null) {
            return defaultValue;
        }
        try {
            return Short.parseShort(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    //-----------------------------------------------------------------------
    // must handle Long, Float, Integer, Float, Short,
    //                  BigDecimal, BigInteger and Byte
    // useful methods:
    // Byte.decode(String)
    // Byte.valueOf(String,int radix)
    // Byte.valueOf(String)
    // Double.valueOf(String)
    // Float.valueOf(String)
    // Float.valueOf(String)
    // Integer.valueOf(String,int radix)
    // Integer.valueOf(String)
    // Integer.decode(String)
    // Integer.getInteger(String)
    // Integer.getInteger(String,int val)
    // Integer.getInteger(String,Integer val)
    // Integer.valueOf(String)
    // Double.valueOf(String)
    // new Byte(String)
    // Long.valueOf(String)
    // Long.getLong(String)
    // Long.getLong(String,int)
    // Long.getLong(String,Integer)
    // Long.valueOf(String,int)
    // Long.valueOf(String)
    // Short.valueOf(String)
    // Short.decode(String)
    // Short.valueOf(String,int)
    // Short.valueOf(String)
    // new BigDecimal(String)
    // new BigInteger(String)
    // new BigInteger(String,int radix)
    // Possible inputs:
    // 45 45.5 45E7 4.5E7 Hex Oct Binary xxxF xxxD xxxf xxxd
    // plus minus everything. Prolly more. A lot are not separable.

    /**
     * <p>Turns a string value into a java.lang.Number.</p>
     *
     * <p>First, the value is examined for a type qualifier on the end
     * (<code>'f','F','d','D','l','L'</code>).  If it is found, it starts 
     * trying to create successively larger types from the type specified
     * until one is found that can represent the value.</p>
     *
     * <p>If a type specifier is not found, it will check for a decimal point
     * and then try successively larger types from <code>Integer</code> to
     * <code>BigInteger</code> and from <code>Float</code> to
     * <code>BigDecimal</code>.</p>
     *
     * <p>If the string starts with <code>0x</code> or <code>-0x</code> (lower or upper case), it
     * will be interpreted as a hexadecimal integer.  Values with leading
     * <code>0</code>'s will not be interpreted as octal.</p>
     *
     * <p>Returns <code>null</code> if the string is <code>null</code>.</p>
     *
     * <p>This method does not trim the input string, i.e., strings with leading
     * or trailing spaces will generate NumberFormatExceptions.</p>
     *
     * @param str  String containing a number, may be null
     * @return Number created from the string (or null if the input is null)
     * @throws NumberFormatException if the value cannot be converted
     */
    public static Number createNumber(String str) throws NumberFormatException {
        if (str == null) {
            return null;
        }
        if (StringUtils.isBlank(str)) {
            throw new NumberFormatException("A blank string is not a valid number");
        }  
        if (str.startsWith("--")) {
            // this is protection for poorness in java.lang.BigDecimal.
            // it accepts this as a legal value, but it does not appear 
            // to be in specification of class. OS X Java parses it to 
            // a wrong value.
            return null;
        }
        if (str.startsWith("0x") || str.startsWith("-0x") || str.startsWith("0X") || str.startsWith("-0X")) {
            return createInteger(str);
        }   
        char lastChar = str.charAt(str.length() - 1);
        String mant;
        String dec;
        String exp;
        int decPos = str.indexOf('.');
        int expPos = str.indexOf('e') + str.indexOf('E') + 1;

        if (decPos > -1) {

            if (expPos > -1) {
                if (expPos < decPos || expPos > str.length()) {
                    throw new NumberFormatException(str + " is not a valid number.");
                }
                dec = str.substring(decPos + 1, expPos);
            } else {
                dec = str.substring(decPos + 1);
            }
            mant = str.substring(0, decPos);
        } else {
            if (expPos > -1) {
                if (expPos > str.length()) {
                    throw new NumberFormatException(str + " is not a valid number.");
                }
                mant = str.substring(0, expPos);
            } else {
                mant = str;
            }
            dec = null;
        }
        if (!Character.isDigit(lastChar) && lastChar != '.') {
            if (expPos > -1 && expPos < str.length() - 1) {
                exp = str.substring(expPos + 1, str.length() - 1);
            } else {
                exp = null;
            }
            //Requesting a specific type..
            String numeric = str.substring(0, str.length() - 1);
            boolean allZeros = isAllZeros(mant) && isAllZeros(exp);
            switch (lastChar) {
                case 'l' :
                case 'L' :
                    if (dec == null
                        && exp == null
                        && (numeric.charAt(0) == '-' && isDigits(numeric.substring(1)) || isDigits(numeric))) {
                        try {
                            return createLong(numeric);
                        } catch (NumberFormatException nfe) { // NOPMD
                            // Too big for a long
                        }
                        return createBigInteger(numeric);

                    }
                    throw new NumberFormatException(str + " is not a valid number.");
                case 'f' :
                case 'F' :
                    try {
                        Float f = NumberUtils.createFloat(numeric);
                        if (!(f.isInfinite() || (f.floatValue() == 0.0F && !allZeros))) {
                            //If it's too big for a float or the float value = 0 and the string
                            //has non-zeros in it, then float does not have the precision we want
                            return f;
                        }

                    } catch (NumberFormatException nfe) { // NOPMD
                        // ignore the bad number
                    }
                    //$FALL-THROUGH$
                case 'd' :
                case 'D' :
                    try {
                        Double d = NumberUtils.createDouble(numeric);
                        if (!(d.isInfinite() || (d.floatValue() == 0.0D && !allZeros))) {
                            return d;
                        }
                    } catch (NumberFormatException nfe) { // NOPMD
                        // ignore the bad number
                    }
                    try {
                        return createBigDecimal(numeric);
                    } catch (NumberFormatException e) { // NOPMD
                        // ignore the bad number
                    }
                    //$FALL-THROUGH$
                default :
                    throw new NumberFormatException(str + " is not a valid number.");

            }
        } else {
            //User doesn't have a preference on the return type, so let's start
            //small and go from there...
            if (expPos > -1 && expPos < str.length() - 1) {
                exp = str.substring(expPos + 1, str.length());
            } else {
                exp = null;
            }
            if (dec == null && exp == null) {
                //Must be an int,long,bigint
                try {
                    return createInteger(str);
                } catch (NumberFormatException nfe) { // NOPMD
                    // ignore the bad number
                }
                try {
                    return createLong(str);
                } catch (NumberFormatException nfe) { // NOPMD
                    // ignore the bad number
                }
                return createBigInteger(str);

            } else {
                //Must be a float,double,BigDec
                boolean allZeros = isAllZeros(mant) && isAllZeros(exp);
                try {
                    Float f = createFloat(str);
                    if (!(f.isInfinite() || (f.floatValue() == 0.0F && !allZeros))) {
                        return f;
                    }
                } catch (NumberFormatException nfe) { // NOPMD
                    // ignore the bad number
                }
                try {
                    Double d = createDouble(str);
                    if (!(d.isInfinite() || (d.doubleValue() == 0.0D && !allZeros))) {
                        return d;
                    }
                } catch (NumberFormatException nfe) { // NOPMD
                    // ignore the bad number
                }

                return createBigDecimal(str);

            }
        }
    }

    /**
     * <p>Utility method for {@link #createNumber(java.lang.String)}.</p>
     *
     * <p>Returns <code>true</code> if s is <code>null</code>.</p>
     * 
     * @param str  the String to check
     * @return if it is all zeros or <code>null</code>
     */
    private static boolean isAllZeros(String str) {
        if (str == null) {
            return true;
        }
        for (int i = str.length() - 1; i >= 0; i--) {
            if (str.charAt(i) != '0') {
                return false;
            }
        }
        return str.length() > 0;
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Convert a <code>String</code> to a <code>Float</code>.</p>
     *
     * <p>Returns <code>null</code> if the string is <code>null</code>.</p>
     * 
     * @param str  a <code>String</code> to convert, may be null
     * @return converted <code>Float</code>
     * @throws NumberFormatException if the value cannot be converted
     */
    public static Float createFloat(String str) {
        if (str == null) {
            return null;
        }
        return Float.valueOf(str);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>Double</code>.</p>
     * 
     * <p>Returns <code>null</code> if the string is <code>null</code>.</p>
     *
     * @param str  a <code>String</code> to convert, may be null
     * @return converted <code>Double</code>
     * @throws NumberFormatException if the value cannot be converted
     */
    public static Double createDouble(String str) {
        if (str == null) {
            return null;
        }
        return Double.valueOf(str);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>Integer</code>, handling
     * hex and octal notations.</p>
     *
     * <p>Returns <code>null</code> if the string is <code>null</code>.</p>
     * 
     * @param str  a <code>String</code> to convert, may be null
     * @return converted <code>Integer</code>
     * @throws NumberFormatException if the value cannot be converted
     */
    public static Integer createInteger(String str) {
        if (str == null) {
            return null;
        }
        // decode() handles 0xAABD and 0777 (hex and octal) as well.
        return Integer.decode(str);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>Long</code>; 
     * since 3.1 it handles hex and octal notations.</p>
     * 
     * <p>Returns <code>null</code> if the string is <code>null</code>.</p>
     *
     * @param str  a <code>String</code> to convert, may be null
     * @return converted <code>Long</code>
     * @throws NumberFormatException if the value cannot be converted
     */
    public static Long createLong(String str) {
        if (str == null) {
            return null;
        }
        return Long.decode(str);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>BigInteger</code>.</p>
     *
     * <p>Returns <code>null</code> if the string is <code>null</code>.</p>
     * 
     * @param str  a <code>String</code> to convert, may be null
     * @return converted <code>BigInteger</code>
     * @throws NumberFormatException if the value cannot be converted
     */
    public static BigInteger createBigInteger(String str) {
        if (str == null) {
            return null;
        }
        return new BigInteger(str);
    }

    /**
     * <p>Convert a <code>String</code> to a <code>BigDecimal</code>.</p>
     * 
     * <p>Returns <code>null</code> if the string is <code>null</code>.</p>
     *
     * @param str  a <code>String</code> to convert, may be null
     * @return converted <code>BigDecimal</code>
     * @throws NumberFormatException if the value cannot be converted
     */
    public static BigDecimal createBigDecimal(String str) {
        if (str == null) {
            return null;
        }
        // handle JDK1.3.1 bug where "" throws IndexOutOfBoundsException
        if (StringUtils.isBlank(str)) {
            throw new NumberFormatException("A blank string is not a valid number");
        }  
        return new BigDecimal(str);
    }

    // Min in array
    //--------------------------------------------------------------------
    /**
     * <p>Returns the minimum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     */
    public static long min(long[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    
        // Finds and returns min
        long min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
    
        return min;
    }

    /**
     * <p>Returns the minimum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     */
    public static int min(int[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    
        // Finds and returns min
        int min = array[0];
        for (int j = 1; j < array.length; j++) {
            if (array[j] < min) {
                min = array[j];
            }
        }
    
        return min;
    }

    /**
     * <p>Returns the minimum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     */
    public static short min(short[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    
        // Finds and returns min
        short min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
    
        return min;
    }

    /**
     * <p>Returns the minimum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     */
    public static byte min(byte[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    
        // Finds and returns min
        byte min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
    
        return min;
    }

     /**
     * <p>Returns the minimum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     * @see IEEE754rUtils#min(double[]) IEEE754rUtils for a version of this method that handles NaN differently
     */
    public static double min(double[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    
        // Finds and returns min
        double min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (Double.isNaN(array[i])) {
                return Double.NaN;
            }
            if (array[i] < min) {
                min = array[i];
            }
        }
    
        return min;
    }

    /**
     * <p>Returns the minimum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     * @see IEEE754rUtils#min(float[]) IEEE754rUtils for a version of this method that handles NaN differently
     */
    public static float min(float[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    
        // Finds and returns min
        float min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (Float.isNaN(array[i])) {
                return Float.NaN;
            }
            if (array[i] < min) {
                min = array[i];
            }
        }
    
        return min;
    }

    // Max in array
    //--------------------------------------------------------------------
    /**
     * <p>Returns the maximum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     */
    public static long max(long[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }

        // Finds and returns max
        long max = array[0];
        for (int j = 1; j < array.length; j++) {
            if (array[j] > max) {
                max = array[j];
            }
        }

        return max;
    }

    /**
     * <p>Returns the maximum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     */
    public static int max(int[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    
        // Finds and returns max
        int max = array[0];
        for (int j = 1; j < array.length; j++) {
            if (array[j] > max) {
                max = array[j];
            }
        }
    
        return max;
    }

    /**
     * <p>Returns the maximum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     */
    public static short max(short[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    
        // Finds and returns max
        short max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
    
        return max;
    }

    /**
     * <p>Returns the maximum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     */
    public static byte max(byte[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    
        // Finds and returns max
        byte max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
    
        return max;
    }

    /**
     * <p>Returns the maximum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     * @see IEEE754rUtils#max(double[]) IEEE754rUtils for a version of this method that handles NaN differently
     */
    public static double max(double[] array) {
        // Validates input
        if (array== null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }
    
        // Finds and returns max
        double max = array[0];
        for (int j = 1; j < array.length; j++) {
            if (Double.isNaN(array[j])) {
                return Double.NaN;
            }
            if (array[j] > max) {
                max = array[j];
            }
        }
    
        return max;
    }

    /**
     * <p>Returns the maximum value in an array.</p>
     * 
     * @param array  an array, must not be null or empty
     * @return the minimum value in the array
     * @throws IllegalArgumentException if <code>array</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>array</code> is empty
     * @see IEEE754rUtils#max(float[]) IEEE754rUtils for a version of this method that handles NaN differently
     */
    public static float max(float[] array) {
        // Validates input
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty.");
        }

        // Finds and returns max
        float max = array[0];
        for (int j = 1; j < array.length; j++) {
            if (Float.isNaN(array[j])) {
                return Float.NaN;
            }
            if (array[j] > max) {
                max = array[j];
            }
        }

        return max;
    }
     
    // 3 param min
    //-----------------------------------------------------------------------
    /**
     * <p>Gets the minimum of three <code>long</code> values.</p>
     * 
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the smallest of the values
     */
    public static long min(long a, long b, long c) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>Gets the minimum of three <code>int</code> values.</p>
     * 
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the smallest of the values
     */
    public static int min(int a, int b, int c) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>Gets the minimum of three <code>short</code> values.</p>
     * 
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the smallest of the values
     */
    public static short min(short a, short b, short c) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>Gets the minimum of three <code>byte</code> values.</p>
     * 
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the smallest of the values
     */
    public static byte min(byte a, byte b, byte c) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>Gets the minimum of three <code>double</code> values.</p>
     * 
     * <p>If any value is <code>NaN</code>, <code>NaN</code> is
     * returned. Infinity is handled.</p>
     * 
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the smallest of the values
     * @see IEEE754rUtils#min(double, double, double) for a version of this method that handles NaN differently
     */
    public static double min(double a, double b, double c) {
        return Math.min(Math.min(a, b), c);
    }

    /**
     * <p>Gets the minimum of three <code>float</code> values.</p>
     * 
     * <p>If any value is <code>NaN</code>, <code>NaN</code> is
     * returned. Infinity is handled.</p>
     *
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the smallest of the values
     * @see IEEE754rUtils#min(float, float, float) for a version of this method that handles NaN differently
     */
    public static float min(float a, float b, float c) {
        return Math.min(Math.min(a, b), c);
    }

    // 3 param max
    //-----------------------------------------------------------------------
    /**
     * <p>Gets the maximum of three <code>long</code> values.</p>
     * 
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the largest of the values
     */
    public static long max(long a, long b, long c) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>Gets the maximum of three <code>int</code> values.</p>
     * 
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the largest of the values
     */
    public static int max(int a, int b, int c) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>Gets the maximum of three <code>short</code> values.</p>
     * 
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the largest of the values
     */
    public static short max(short a, short b, short c) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>Gets the maximum of three <code>byte</code> values.</p>
     * 
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the largest of the values
     */
    public static byte max(byte a, byte b, byte c) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>Gets the maximum of three <code>double</code> values.</p>
     * 
     * <p>If any value is <code>NaN</code>, <code>NaN</code> is
     * returned. Infinity is handled.</p>
     *
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the largest of the values
     * @see IEEE754rUtils#max(double, double, double) for a version of this method that handles NaN differently
     */
    public static double max(double a, double b, double c) {
        return Math.max(Math.max(a, b), c);
    }

    /**
     * <p>Gets the maximum of three <code>float</code> values.</p>
     * 
     * <p>If any value is <code>NaN</code>, <code>NaN</code> is
     * returned. Infinity is handled.</p>
     *
     * @param a  value 1
     * @param b  value 2
     * @param c  value 3
     * @return  the largest of the values
     * @see IEEE754rUtils#max(float, float, float) for a version of this method that handles NaN differently
     */
    public static float max(float a, float b, float c) {
        return Math.max(Math.max(a, b), c);
    }

    //-----------------------------------------------------------------------
    /**
     * <p>Checks whether the <code>String</code> contains only
     * digit characters.</p>
     *
     * <p><code>Null</code> and empty String will return
     * <code>false</code>.</p>
     *
     * @param str  the <code>String</code> to check
     * @return <code>true</code> if str contains only Unicode numeric
     */
    public static boolean isDigits(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Checks whether the String a valid Java number.</p>
     *
     * <p>Valid numbers include hexadecimal marked with the <code>0x</code>
     * qualifier, scientific notation and numbers marked with a type
     * qualifier (e.g. 123L).</p>
     *
     * <p><code>Null</code> and empty String will return
     * <code>false</code>.</p>
     *
     * @param str  the <code>String</code> to check
     * @return <code>true</code> if the string is a correctly formatted number
     */
    public static boolean isNumber(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        char[] chars = str.toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        // deal with any possible sign up front
        int start = (chars[0] == '-') ? 1 : 0;
        if (sz > start + 1 && chars[start] == '0' && chars[start + 1] == 'x') {
            int i = start + 2;
            if (i == sz) {
                return false; // str == "0x"
            }
            // checking hex (it can't be anything else)
            for (; i < chars.length; i++) {
                if ((chars[i] < '0' || chars[i] > '9')
                    && (chars[i] < 'a' || chars[i] > 'f')
                    && (chars[i] < 'A' || chars[i] > 'F')) {
                    return false;
                }
            }
            return true;
        }
        sz--; // don't want to loop to the last char, check it afterwords
              // for type qualifiers
        int i = start;
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < sz || (i < sz + 1 && allowSigns && !foundDigit)) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                foundDigit = true;
                allowSigns = false;

            } else if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent   
                    return false;
                }
                hasDecPoint = true;
            } else if (chars[i] == 'e' || chars[i] == 'E') {
                // we've already taken care of hex.
                if (hasExp) {
                    // two E's
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (chars[i] == '+' || chars[i] == '-') {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false; // we need a digit after the E
            } else {
                return false;
            }
            i++;
        }
        if (i < chars.length) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                // no type qualifier, OK
                return true;
            }
            if (chars[i] == 'e' || chars[i] == 'E') {
                // can't have an E at the last byte
                return false;
            }
            if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                // single trailing decimal point after non-exponent is ok
                return foundDigit;
            }
            if (!allowSigns
                && (chars[i] == 'd'
                    || chars[i] == 'D'
                    || chars[i] == 'f'
                    || chars[i] == 'F')) {
                return foundDigit;
            }
            if (chars[i] == 'l'
                || chars[i] == 'L') {
                // not allowing L with an exponent or decimal point
                return foundDigit && !hasExp && !hasDecPoint;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return !allowSigns && foundDigit;
    }

=======
package VEW.Scenario2;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import VEW.Common.StringTools;

public class EquationPanel extends JPanel {
  String EQ;
  boolean BRACKETS = true;
  Font normalFont = new Font("Default",Font.PLAIN,16);
  Font smallFont = normalFont.deriveFont(12.0f);
  Font boldFont = new Font("Default",Font.PLAIN,16);

  public void defaultFonts() {
    normalFont = new Font("Default",Font.PLAIN,16);
    boldFont = new Font("Default",Font.PLAIN,16);
    smallFont = normalFont.deriveFont(12.0f);
  }

  public void smallFonts() {
    normalFont = new Font("Default",Font.PLAIN,12);
    boldFont = new Font("Default",Font.PLAIN,12);
    smallFont = normalFont.deriveFont(9.0f);
  }
  
  public EquationPanel(String eq) {
    super();
    EQ = eq;
  }

  public void setEquation(String eq) {
    EQ = eq;
  }
 

  private double getEqWidth(Graphics2D g, String s) {
    if (StringTools.chomp(s,"\\minus{")) return textWidth(g, "-", normalFont)+getEqWidth(g,StringTools.spit(s,"\\minus{"));
    else if (StringTools.chomp(s,"\\abs{")) return textWidth(g, "||", normalFont)+getEqWidth(g,StringTools.spit(s,"\\abs{"));
    else if (StringTools.chomp(s,"\\varietysum{")) return textWidth(g, "varietysum()", normalFont)+getEqWidth(g,StringTools.spit(s,"\\varietysum{"));
    else if (StringTools.chomp(s,"\\varietymul{")) return textWidth(g, "varietymul()", normalFont)+getEqWidth(g,StringTools.spit(s,"\\varietymul{"));    
    else if (StringTools.chomp(s,"\\rnd{")) return textWidth(g, "rnd()", normalFont)+getEqWidth(g,StringTools.spit(s,"\\rnd{"));
    else if (StringTools.chomp(s,"\\ln{")) return textWidth(g, "ln()", normalFont)+getEqWidth(g,StringTools.spit(s,"\\ln{"));  
    else if (StringTools.chomp(s,"\\log10{")) return textWidth(g, "log()", normalFont)+getEqWidth(g,StringTools.spit(s,"\\log10{"));      
    else if (StringTools.chomp(s,"\\asin{")) return textWidth(g, "sin()", normalFont)+getEqWidth(g,StringTools.spit(s,"\\asin{"));  
    else if (StringTools.chomp(s,"\\acos{")) return textWidth(g, "cos()", normalFont)+getEqWidth(g,StringTools.spit(s,"\\acos{"));  
    else if (StringTools.chomp(s,"\\atan{")) return textWidth(g, "tan()", normalFont)+getEqWidth(g,StringTools.spit(s,"\\atan{"));  
    else if (StringTools.chomp(s,"\\sin{")) return textWidth(g, "sin()", normalFont)+getEqWidth(g,StringTools.spit(s,"\\sin{"));      
    else if (StringTools.chomp(s,"\\cos{")) return textWidth(g, "cos()", normalFont)+getEqWidth(g,StringTools.spit(s,"\\cos{"));
    else if (StringTools.chomp(s,"\\tan{")) return textWidth(g, "tan()", normalFont)+getEqWidth(g,StringTools.spit(s,"\\tan{"));  
    else if (StringTools.chomp(s,"\\sqrt{")) return textWidth(g, "V", normalFont)+getEqWidth(g,StringTools.spit(s,"\\sqrt{"));  
    else if (StringTools.chomp(s,"\\integrate{")) return textWidth(g,"integrate",normalFont)+getEqWidth(g,StringTools.spit(s,"\\integrate{"));
    else if (StringTools.chomp(s,"\\exp{")) return textWidth(g, "e", normalFont)+getEqWidth(g,StringTools.spit(s,"\\exp{"));  
    else if (StringTools.chomp(s,"\\pow{")) return getBinEqWidth(g,StringTools.spit(s,"\\pow{"),"^");  
    else if (StringTools.chomp(s,"\\div{")) return getBinEqWidth(g,StringTools.spit(s,"\\div{"),"/");  
    else if (StringTools.chomp(s,"\\sub{")) return textWidth(g, "(-)", normalFont)+getBinEqWidth(g,StringTools.spit(s,"\\sub{"),"-");      
    else if (StringTools.chomp(s,"\\conditional{")) return textWidth(g, "(if then else )", normalFont)+getTriEqWidth(g,StringTools.spit(s,"\\conditional{"));
    else if (StringTools.chomp(s,"\\min{")) return textWidth(g, "min[]", normalFont)+getMultiEqWidth(g,StringTools.spit(s,"\\min{"),"min");  
    else if (StringTools.chomp(s,"\\max{")) return textWidth(g, "max[]", normalFont)+getMultiEqWidth(g,StringTools.spit(s,"\\max{"),"max");  
    else if (StringTools.chomp(s,"\\mul{")) return getMultiEqWidth(g,StringTools.spit(s,"\\mul{"),"*");  
    else if (StringTools.chomp(s,"\\add{")) return getMultiEqWidth(g,StringTools.spit(s,"\\add{"),"+");  
    else if (StringTools.chomp(s,"\\fullIrradAt{")) return textWidth(g, "fullIrradAt()", normalFont)+getEqWidth(g,StringTools.spit(s,"\\fullIrradAt{"));      
    else if (StringTools.chomp(s,"\\salinityAt{")) return textWidth(g, "salinityAt()", normalFont)+getEqWidth(g,StringTools.spit(s,"\\salinityAt{"));      
    else if (StringTools.chomp(s,"\\temperatureAt{")) return textWidth(g, "temperatureAt()", normalFont)+getEqWidth(g,StringTools.spit(s,"\\temperatureAt{"));      
    else if (StringTools.chomp(s,"\\densityAt{")) return textWidth(g, "densityAt()", normalFont)+getEqWidth(g,StringTools.spit(s,"\\densityAt{"));      
    else if (StringTools.chomp(s,"\\visIrradAt{")) return textWidth(g, "visIrradAt()", normalFont)+getEqWidth(g,StringTools.spit(s,"\\visIrradAt{"));      
    else if (StringTools.chomp(s,"\\var{")) return SSWidth(g,StringTools.spit(s,"\\var{"),normalFont);  
    else if (StringTools.chomp(s,"\\stage{")) return SSWidth(g,StringTools.spit(s,"\\stage{"),normalFont);
    else if (StringTools.chomp(s,"\\change{")) return textWidth(g, "change()", normalFont)+getEqWidth(g,StringTools.spit(s,"\\change{"));  
    else if (StringTools.chomp(s,"\\divide{")) return textWidth(g, "divide()", boldFont)+textWidth(g, "[]", normalFont)+getEqWidth(g,StringTools.spit(s,"\\divide{"));
    else if (StringTools.chomp(s,"\\release{")) return textWidth(g, "release()", boldFont)+textWidth(g, "[]", normalFont)+getEqWidth(g,StringTools.spit(s,"\\release{"));
    else if (StringTools.chomp(s,"\\remineralise{")) return textWidth(g, "remineralise()", boldFont)+textWidth(g, "[]", normalFont)+getEqWidth(g,StringTools.spit(s,"\\remineralise{")); // LEGACY
    else if (StringTools.chomp(s,"\\ingest{")) return textWidth(g, "ingest()", boldFont)+textWidth(g, "[]", normalFont)+getEqWidth(g,StringTools.spit(s,"\\ingest{"));
    else if (StringTools.chomp(s,"\\uptake{")) return textWidth(g, "uptake()", boldFont)+textWidth(g, "[]", normalFont)+getEqWidth(g,StringTools.spit(s,"\\uptake{"));
    else if (StringTools.chomp(s,"\\pchange{")) return textWidth(g, "pchange()", boldFont)+textWidth(g, "[]", normalFont)+getEqWidth(g,StringTools.spit(s,"\\pchange{"));
    else if (StringTools.chomp(s,"\\varhist{")) {
      s = StringTools.spit(s,"\\varhist{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      lhs = StringTools.spit(lhs,"\\var{");
      return textWidth(g,lhs+"_",normalFont)+getEqWidth(g,rhs);
    }
    else if (StringTools.chomp(s,"\\val{")) {
      s = StringTools.spit(s,"\\val{");
      s = StringTools.LHS(s,StringTools.getUnNested(s,','));
      s = StringTools.spit(s,"\\sival{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      if (!(rhs.equals("0"))) return SSWidth(g,lhs+" x 10^{"+rhs+"}",normalFont);
      else return SSWidth(g,lhs,normalFont);
    }
    else if (StringTools.chomp(s,"\\assign")) {
      s = StringTools.spit(s,"\\assign{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      return getEqWidth(g,lhs)+SSWidth(g," = ",normalFont)+getEqWidth(g,rhs);
    }
    else if (StringTools.chomp(s,"\\assigndiff")) {
      s = StringTools.spit(s,"\\assigndiff{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      lhs = StringTools.spit(lhs,"\\var{");
      return getEqWidth(g,lhs)+SSWidth(g,"d = ",normalFont)+getEqWidth(g,rhs);

    }
    else if (StringTools.chomp(s,"\\ifthen")) {
      s = StringTools.spit(s,"\\ifthen{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      lhs = StringTools.spit(lhs,"\\var{");
      return getEqWidth(g,lhs)+SSWidth(g,"if then ",normalFont)+getEqWidth(g,rhs);
    }

    else return 0;
  }


  private double SSWidth(Graphics2D g, String s,Font f) {
    String subscript = "";
    String superscript = "";
    s = s.replace('$','_');
    if (s.indexOf("_")>=0) {
      subscript = s.substring(s.indexOf("_")+1);
      s = s.substring(0,s.indexOf("_"));
      if (subscript.indexOf("^")>=0) {
        superscript = subscript.substring(subscript.indexOf("^")+1);
        subscript = subscript.substring(0,subscript.indexOf("^"));
      }
    }
    if (s.indexOf("^")>=0) {
      superscript = s.substring(s.indexOf("^")+1);
      s = s.substring(0,s.indexOf("^"));
    }
    if (subscript.indexOf("{")>=0) subscript = StringTools.spit(subscript,"{");
    if (superscript.indexOf("{")>=0) superscript = StringTools.spit(superscript,"{");
    double width=textWidth(g, s, f);
    if (subscript.length()>0) {
      width+=textWidth(g,subscript,smallFont);
    }
    if (superscript.length()>0) {
      width+=textWidth(g,superscript,smallFont);
    }
    return width;
  }

  private int textHeight(Graphics2D g, String s, Font f) {
    //FontRenderContext frc = g.getFontRenderContext();
    //Rectangle2D r = f.getStringBounds(s,frc);
    return 18; // (int) r.getHeight();
  }

  private int textWidth(Graphics2D g, String s, Font f) {
    FontRenderContext frc = g.getFontRenderContext();
    Rectangle2D r = f.getStringBounds(s,frc);
    return (int) r.getWidth();
  }

  private double SSHeight(Graphics2D g, String s,Font f) {
    String subscript = "";
    String superscript = "";
    s = s.replace('$','_');
    if (s.indexOf("_")>=0) {
      subscript = s.substring(s.indexOf("_")+1);
      s = s.substring(0,s.indexOf("_"));
      if (subscript.indexOf("^")>=0) {
        superscript = subscript.substring(subscript.indexOf("^")+1);
        subscript = subscript.substring(0,subscript.indexOf("^"));
      }
    }
    if (s.indexOf("^")>=0) {
      superscript = s.substring(s.indexOf("^")+1);
      s = s.substring(0,s.indexOf("^"));
    }
    if (subscript.indexOf("{")>=0) subscript = StringTools.spit(subscript,"{");
    if (superscript.indexOf("{")>=0) superscript = StringTools.spit(superscript,"{");
    double height=textHeight(g,s,f);
    if (subscript.length()>0) {
      int newSize = (int) Math.max(6,f.getSize()/1.2);
      height+=(newSize/3);
    }
    if (superscript.length()>0) {
      int newSize = (int) Math.max(6,f.getSize()/1.2);
      height+=(newSize/3);
    }
    return 18; //height;
  }
  
  private int plotSS(Graphics2D g, int x, int y, String s) {
    String subscript = "";
    String superscript = "";
    s = s.replace('$','_');
    if (s.indexOf("_")>=0) {
      subscript = s.substring(s.indexOf("_")+1);
      s = s.substring(0,s.indexOf("_"));
      if (subscript.indexOf("^")>=0) {
        superscript = subscript.substring(subscript.indexOf("^")+1);
        subscript = subscript.substring(0,subscript.indexOf("^"));
      }
    }
    if (s.indexOf("^")>=0) {
      superscript = s.substring(s.indexOf("^")+1);
      s = s.substring(0,s.indexOf("^"));
    }
    if (subscript.indexOf("{")>=0) subscript = StringTools.spit(subscript,"{");
    if (superscript.indexOf("{")>=0) superscript = StringTools.spit(superscript,"{");
    int newSize = 0;
    int dropBy = 0;
    int hoistBy = 0;
    if (subscript.length()>0) {
      newSize = (int) Math.max(6,normalFont.getSize()/1.2);
      dropBy = (newSize/3);
    }
    if (superscript.length()>0) {
      newSize = (int) Math.max(6,normalFont.getSize()/1.2);
      hoistBy = (newSize/3);
    }
    int mainPos = y+(textHeight(g,s,normalFont)/2)+((hoistBy/2)-(dropBy/2));
    g.drawString(s,x,mainPos);
    x+=textWidth(g, s, normalFont);
    if (subscript.length()>0) {
      //Font nf = new Font(normalFont.getName(),normalFont.getStyle(),newSize);
      //g.setFont(nf);
      g.setFont(smallFont);
      g.drawString(subscript,x,mainPos+dropBy);
      //x+=textWidth(g,subscript,nf);
      x+=textWidth(g,subscript,smallFont);
    }
    if (superscript.length()>0) {
      //Font nf = new Font(normalFont.getName(),normalFont.getStyle(),newSize);
      //g.setFont(nf);
      //g.drawString(superscript,x,mainPos-(textHeight(g,superscript,nf)/2));
      //x+=textWidth(g,superscript,nf);
      g.setFont(smallFont);
      g.drawString(superscript,x,mainPos-(textHeight(g,superscript,smallFont)/2));
      x+=textWidth(g,superscript,smallFont);

    }
    g.setFont(normalFont);
    return x+1;
  }

  /*private String GetSival(String s) {
    s = StringTools.spit(StringTools.LHS(s,StringTools.getUnNested(s,',')),"\\sival{");
    double val = 0.0;
    if (!(s.equals(""))) {
      String vTemp = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String eTemp = StringTools.RHS(s,StringTools.getUnNested(s,','));
      try { 
        double expon = Double.parseDouble(eTemp);
        val = Double.parseDouble(vTemp)*Math.pow(10,expon);
      } catch (NumberFormatException n) { val = 0.0; }
    }
    return ""+val;
  }
*/
  private double getBinEqWidth(Graphics2D g, String s, String op) {
    String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
    String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
    if (op.equals("/")) return Math.max(getEqWidth(g,lhs),getEqWidth(g,rhs));
    else if (op.equals("-")) return getEqWidth(g,lhs)+getEqWidth(g,rhs)+textWidth(g, "+", normalFont);
    else if (op.equals("^")) return getEqWidth(g,lhs)+getEqWidth(g,rhs);
    else return 0.0;
  }

  private double getTriEqWidth(Graphics2D g, String s) {
    String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
    String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
    String mid = StringTools.LHS(rhs,StringTools.getUnNested(rhs,','));
    rhs = StringTools.RHS(rhs,StringTools.getUnNested(rhs,','));
    return getBoolEqWidth(g,lhs)+getEqWidth(g,rhs)+getEqWidth(g,mid)+textWidth(g, "(if then else )", normalFont);
  }

  private double getMultiEqWidth(Graphics2D g,String s, String op) {
    String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
    String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
    String preop = "";
    String midop = "";
    if (op.equals("max")) { preop="max[]"; midop=","; }
    else if (op.equals("min")) { preop="min[]"; midop=","; }
    else if (op.equals("*")) { preop=""; midop="."; }
    else if (op.equals("+")) { preop=""; midop="+"; }
    if (rhs.equals("")) midop="";
    double width = getEqWidth(g,lhs)+textWidth(g, midop, normalFont)+textWidth(g, preop, normalFont);
    while (StringTools.getUnNested(rhs,',')>=0) {
      lhs = StringTools.LHS(rhs,StringTools.getUnNested(rhs,','));
      width += getEqWidth(g,lhs)+textWidth(g, midop, normalFont);
      rhs = StringTools.RHS(rhs,StringTools.getUnNested(rhs,','));
    }
    width += getEqWidth(g,rhs)+textWidth(g, midop, normalFont);
    return width;
  }
  
  private double getBoolEqWidth(Graphics2D g, String s) {
    if (StringTools.chomp(s,"\\not{")) return textWidth(g, "!", normalFont) + getBoolEqWidth(g,StringTools.spit(s,"\\not{"));
    else if (StringTools.chomp(s,"\\greater{")) return getBinBoolEqWidth(g,StringTools.spit(s,"\\greater{"),">");
    else if (StringTools.chomp(s,"\\greaterequal{")) return getBinBoolEqWidth(g,StringTools.spit(s,"\\greaterequal{"),"\u2265");
    else if (StringTools.chomp(s,"\\equal{")) return getBinBoolEqWidth(g,StringTools.spit(s,"\\equal{"),"=");
    else if (StringTools.chomp(s,"\\less{")) return getBinBoolEqWidth(g,StringTools.spit(s,"\\less{"),">");
    else if (StringTools.chomp(s,"\\lessequal{")) return getBinBoolEqWidth(g,StringTools.spit(s,"\\lessequal{"),"\u2264");
    else if (StringTools.chomp(s,"\\neq{")) return getBinBoolEqWidth(g,StringTools.spit(s,"\\neq{"),"=");
    else if (StringTools.chomp(s,"\\and{")) return getMultiBoolEqWidth(g,StringTools.spit(s,"\\and{")," and ");
    else if (StringTools.chomp(s,"\\or{")) return getMultiBoolEqWidth(g,StringTools.spit(s,"\\or{")," or ");
    else if (StringTools.chomp(s,"\\someVariety{")) return textWidth(g, "someVariety", normalFont) + getBoolEqWidth(g,StringTools.spit(s,"\\someVariety{"));
    else if (StringTools.chomp(s,"\\allVariety{")) return textWidth(g, "allVariety", normalFont) + getBoolEqWidth(g,StringTools.spit(s,"\\allVariety{"));
    else if (StringTools.chomp(s,"\\noVariety{")) return textWidth(g, "noVariety", normalFont) + getBoolEqWidth(g,StringTools.spit(s,"\\noVariety{"));

    else return 0;
  }

  private double getBinBoolEqWidth(Graphics2D g, String s, String op) {
    String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
    String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
    return textWidth(g, op, normalFont)+getBoolEqWidth(g,lhs)+getBoolEqWidth(g,rhs);
  }

  private double getMultiBoolEqWidth(Graphics2D g, String s, String op) {
    String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
    String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
    double width = getEqWidth(g,lhs);
    if (!rhs.equals("")) width+= textWidth(g, op, normalFont);
    while (StringTools.getUnNested(rhs,',')>=0) {
      lhs = StringTools.LHS(rhs,StringTools.getUnNested(rhs,','));
      width += getEqWidth(g,lhs)+textWidth(g, op, normalFont);
      rhs = StringTools.RHS(rhs,StringTools.getUnNested(rhs,','));
    }
    width += getEqWidth(g,rhs)+textWidth(g, op, normalFont);
    return width;
  }

  private double getEqTop(Graphics2D g, String s) {
    if (StringTools.chomp(s,"\\minus{")) return getEqTop(g,StringTools.spit(s,"\\minus{"));
    else if (StringTools.chomp(s,"\\integrate{")) return getEqTop(g,StringTools.spit(s,"\\integrate{"));
    else if (StringTools.chomp(s,"\\abs{")) return getEqTop(g,StringTools.spit(s,"\\abs{"));
    else if (StringTools.chomp(s,"\\rnd{")) return getEqTop(g,StringTools.spit(s,"\\rnd{"));
    else if (StringTools.chomp(s,"\\ln{")) return getEqTop(g,StringTools.spit(s,"\\ln{"));  
    else if (StringTools.chomp(s,"\\log10{")) return getEqTop(g,StringTools.spit(s,"\\log10{"));  
    else if (StringTools.chomp(s,"\\asin{")) return getEqTop(g,StringTools.spit(s,"\\asin{"));  
    else if (StringTools.chomp(s,"\\acos{")) return getEqTop(g,StringTools.spit(s,"\\acos{"));  
    else if (StringTools.chomp(s,"\\atan{")) return getEqTop(g,StringTools.spit(s,"\\atan{"));  
    else if (StringTools.chomp(s,"\\varietysum{")) return getEqTop(g,StringTools.spit(s,"\\varietysum{"));  
    else if (StringTools.chomp(s,"\\varietymul{")) return getEqTop(g,StringTools.spit(s,"\\varietymul{"));  
    else if (StringTools.chomp(s,"\\visIrradAt{")) return getEqTop(g,StringTools.spit(s,"\\visIrradAt{"));
    else if (StringTools.chomp(s,"\\fullIrradAt{")) return getEqTop(g,StringTools.spit(s,"\\fullIrradAt{"));
    else if (StringTools.chomp(s,"\\salinityAt{")) return getEqTop(g,StringTools.spit(s,"\\salinityAt{"));
    else if (StringTools.chomp(s,"\\temperatureAt{")) return getEqTop(g,StringTools.spit(s,"\\temperatureAt{"));
    else if (StringTools.chomp(s,"\\densityAt{")) return getEqTop(g,StringTools.spit(s,"\\densityAt{"));
    else if (StringTools.chomp(s,"\\sin{")) return getEqTop(g,StringTools.spit(s,"\\sin{"));      
    else if (StringTools.chomp(s,"\\cos{")) return getEqTop(g,StringTools.spit(s,"\\cos{"));
    else if (StringTools.chomp(s,"\\tan{")) return getEqTop(g,StringTools.spit(s,"\\tan{"));  
    else if (StringTools.chomp(s,"\\sqrt{")) return getEqTop(g,StringTools.spit(s,"\\sqrt{"));  
    else if (StringTools.chomp(s,"\\exp{")) return getEqTop(g,StringTools.spit(s,"\\exp{"));
    else if (StringTools.chomp(s,"\\pow{")) return getBinEqTop(g,StringTools.spit(s,"\\pow{"),"^");  
    else if (StringTools.chomp(s,"\\div{")) return getBinEqTop(g,StringTools.spit(s,"\\div{"),"/");  
    else if (StringTools.chomp(s,"\\sub{")) return getBinEqTop(g,StringTools.spit(s,"\\sub{"),"-");      
    else if (StringTools.chomp(s,"\\conditional{")) return getTriEqTop(g,StringTools.spit(s,"\\conditional{"));
    else if (StringTools.chomp(s,"\\min{")) return getMultiEqTop(g,StringTools.spit(s,"\\min{"),"min");  
    else if (StringTools.chomp(s,"\\max{")) return getMultiEqTop(g,StringTools.spit(s,"\\max{"),"max");  
    else if (StringTools.chomp(s,"\\mul{")) return getMultiEqTop(g,StringTools.spit(s,"\\mul{"),"*");  
    else if (StringTools.chomp(s,"\\add{")) return getMultiEqTop(g,StringTools.spit(s,"\\add{"),"+");  
    else if (StringTools.chomp(s,"\\stage{")) return 0;  
    else if (StringTools.chomp(s,"\\var{")) return 0;  
    else if (StringTools.chomp(s,"\\val{")) return 0;
    else if (StringTools.chomp(s,"\\varhist{")) {
      s = StringTools.spit(s,"\\varhist{");
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      return getEqTop(g,rhs);
    }
    else if (StringTools.chomp(s,"\\not{")) return getEqTop(g,StringTools.spit(s,"\\not{"));
    else if (StringTools.chomp(s,"\\greater{")) return getBinBoolEqTop(g,StringTools.spit(s,"\\greater{"),">");
    else if (StringTools.chomp(s,"\\greaterequal{")) return getBinBoolEqTop(g,StringTools.spit(s,"\\greaterequal{"),">");
    else if (StringTools.chomp(s,"\\equal{")) return getBinBoolEqTop(g,StringTools.spit(s,"\\equal{"),"=");
    else if (StringTools.chomp(s,"\\less{")) return getBinBoolEqTop(g,StringTools.spit(s,"\\less{"),">");
    else if (StringTools.chomp(s,"\\lessequal{")) return getBinBoolEqTop(g,StringTools.spit(s,"\\lessequal{"),"<");
    else if (StringTools.chomp(s,"\\neq{")) return getBinBoolEqTop(g,StringTools.spit(s,"\\neq{"),"=");
    else if (StringTools.chomp(s,"\\and{")) return getMultiBoolEqTop(g,StringTools.spit(s,"\\and{")," and ");
    else if (StringTools.chomp(s,"\\or{")) return getMultiBoolEqTop(g,StringTools.spit(s,"\\or{")," or ");
    else if (StringTools.chomp(s,"\\someVariety{")) return getEqTop(g,StringTools.spit(s,"\\someVariety{"));
    else if (StringTools.chomp(s,"\\noVariety{")) return getEqTop(g,StringTools.spit(s,"\\noVariety{"));
    else if (StringTools.chomp(s,"\\allVariety{")) return getEqTop(g,StringTools.spit(s,"\\allVariety{"));
    else if (StringTools.chomp(s,"\\assign{")) {
      s = StringTools.spit(s,"\\assign{");
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      return getEqTop(g,rhs);
    } else if (StringTools.chomp(s,"\\set{")) {
      s = StringTools.spit(s,"\\set{");
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      return getEqTop(g,rhs);
    } else if (StringTools.chomp(s,"\\assigndiff{")) {
      s = StringTools.spit(s,"\\assigndiff{");
      String lhs = StringTools.spit(StringTools.LHS(s,StringTools.getUnNested(s,',')),"\\var{");
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      return Math.min(getEqTop(g,"\\div{\\var{d"+lhs+"},\\var{dt}}"),getEqTop(g,rhs));
    } else if (StringTools.chomp(s,"\\ifthen{")) {
      s = StringTools.spit(s,"\\ifthen{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      return Math.min(getEqTop(g,lhs),getEqTop(g,rhs));
    } else if (StringTools.chomp(s,"\\create{")) { return 0;
    } else if (StringTools.chomp(s,"\\change{")) { return 0;
    } else if (StringTools.chomp(s,"\\pchange{")) {
      s = StringTools.spit(s,"\\pchange{");
      return getEqTop(g,StringTools.RHS(s,StringTools.getUnNested(s,',')));
    } else if (StringTools.chomp(s,"\\divide")) {
      return getEqTop(g,StringTools.spit(s,"\\divide{"));
    } else if (StringTools.chomp(s,"\\release{")) {
      s = StringTools.spit(s,"\\release{");
      return getEqTop(g,StringTools.RHS(s,StringTools.getUnNested(s,',')));
    } else if (StringTools.chomp(s,"\\remineralise{")) {
      s = StringTools.spit(s,"\\remineralise{");
      return getEqTop(g,StringTools.RHS(s,StringTools.getUnNested(s,',')));
   
    } else if (StringTools.chomp(s,"\\uptake{")) {
      s = StringTools.spit(s,"\\uptake{");
      return getEqTop(g,StringTools.RHS(s,StringTools.getUnNested(s,',')));
    } else if (StringTools.chomp(s,"\\ingest{")) {
      s = StringTools.spit(s,"\\ingest{");
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      String mid = StringTools.LHS(rhs,StringTools.getUnNested(rhs,','));
      rhs = StringTools.RHS(rhs,StringTools.getUnNested(rhs,','));
      return Math.min(getEqTop(g,mid),getEqTop(g,rhs));
    }
    else {
      //System.out.println("EqPanel error: "+s+" not found in getEqTop");
      return 0;
    }
  }

  
  private double getBinEqTop(Graphics2D g, String s, String op) {
    String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
    //String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
    if (op.equals("/")) {
      return -(2+getEqHeight(g,lhs));
    } else return 0;
  }

  private double getTriEqTop(Graphics2D g, String s) {
   // String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
    String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
    String mid = StringTools.LHS(rhs,StringTools.getUnNested(rhs,','));
    rhs = StringTools.RHS(rhs,StringTools.getUnNested(rhs,','));
    return Math.min(getEqTop(g,rhs),getEqTop(g,mid));
  }

  private double getMultiEqTop(Graphics2D g, String s, String op) {
    String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
    String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
    double top = getEqTop(g,lhs);
    while (StringTools.getUnNested(rhs,',')>=0) {
      lhs = StringTools.LHS(rhs,StringTools.getUnNested(rhs,','));
      top = Math.min(top,getEqTop(g,lhs));
      rhs = StringTools.RHS(rhs,StringTools.getUnNested(rhs,','));
    }
    top = Math.min(top,getEqTop(g,rhs));
    return top;
  }

  private double getBinBoolEqTop(Graphics2D g, String s, String op) {
    String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
    String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
    return Math.min(getEqTop(g,lhs),getEqTop(g,rhs));
  }

  private double getMultiBoolEqTop(Graphics2D g, String s, String op) {
    String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
    String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
    double top = getEqTop(g,lhs);
    while (StringTools.getUnNested(rhs,',')>=0) {
      lhs = StringTools.LHS(rhs,StringTools.getUnNested(rhs,','));
      top = Math.max(top,getEqTop(g,lhs));
      rhs = StringTools.RHS(rhs,StringTools.getUnNested(rhs,','));
    }
    top = Math.min(top,getEqHeight(g,rhs));
    return top;
  }

  private double getEqHeight(Graphics2D g, String s) {
    if (StringTools.chomp(s,"\\minus{")) return getEqHeight(g,StringTools.spit(s,"\\minus{"));
    else if (StringTools.chomp(s,"\\integrate{")) return getEqHeight(g,StringTools.spit(s,"\\integrate{"));
    else if (StringTools.chomp(s,"\\abs{")) return getEqHeight(g,StringTools.spit(s,"\\abs{"));
    else if (StringTools.chomp(s,"\\rnd{")) return getEqHeight(g,StringTools.spit(s,"\\rnd{"));
    else if (StringTools.chomp(s,"\\ln{")) return getEqHeight(g,StringTools.spit(s,"\\ln{"));  
    else if (StringTools.chomp(s,"\\log10{")) return getEqHeight(g,StringTools.spit(s,"\\log10{"));  
    else if (StringTools.chomp(s,"\\asin{")) return getEqHeight(g,StringTools.spit(s,"\\asin{"));  
    else if (StringTools.chomp(s,"\\acos{")) return getEqHeight(g,StringTools.spit(s,"\\acos{"));  
    else if (StringTools.chomp(s,"\\atan{")) return getEqHeight(g,StringTools.spit(s,"\\atan{"));  
    else if (StringTools.chomp(s,"\\varietysum{")) return getEqHeight(g,StringTools.spit(s,"\\varietysum{"));  
    else if (StringTools.chomp(s,"\\varietymul{")) return getEqHeight(g,StringTools.spit(s,"\\varietymul{"));  
    else if (StringTools.chomp(s,"\\visIrradAt{")) return getEqHeight(g,StringTools.spit(s,"\\visIrradAt{"));
    else if (StringTools.chomp(s,"\\fullIrradAt{")) return getEqHeight(g,StringTools.spit(s,"\\fullIrradAt{"));
    else if (StringTools.chomp(s,"\\salinityAt{")) return getEqHeight(g,StringTools.spit(s,"\\salinityAt{"));
    else if (StringTools.chomp(s,"\\temperatureAt{")) return getEqHeight(g,StringTools.spit(s,"\\temperatureAt{"));
    else if (StringTools.chomp(s,"\\densityAt{")) return getEqHeight(g,StringTools.spit(s,"\\densityAt{"));
    else if (StringTools.chomp(s,"\\sin{")) return getEqHeight(g,StringTools.spit(s,"\\sin{"));      
    else if (StringTools.chomp(s,"\\cos{")) return getEqHeight(g,StringTools.spit(s,"\\cos{"));
    else if (StringTools.chomp(s,"\\tan{")) return getEqHeight(g,StringTools.spit(s,"\\tan{"));  
    else if (StringTools.chomp(s,"\\sqrt{")) return getEqHeight(g,StringTools.spit(s,"\\sqrt{"));  
    else if (StringTools.chomp(s,"\\exp{")) return getEqHeight(g,StringTools.spit(s,"\\exp{"));
    else if (StringTools.chomp(s,"\\pow{")) return getBinEqHeight(g,StringTools.spit(s,"\\pow{"),"^");  
    else if (StringTools.chomp(s,"\\div{")) return getBinEqHeight(g,StringTools.spit(s,"\\div{"),"/");  
    else if (StringTools.chomp(s,"\\sub{")) return getBinEqHeight(g,StringTools.spit(s,"\\sub{"),"-");      
    else if (StringTools.chomp(s,"\\conditional{")) return getTriEqHeight(g,StringTools.spit(s,"\\conditional{"));
    else if (StringTools.chomp(s,"\\min{")) return getMultiEqHeight(g,StringTools.spit(s,"\\min{"),"min");  
    else if (StringTools.chomp(s,"\\max{")) return getMultiEqHeight(g,StringTools.spit(s,"\\max{"),"max");  
    else if (StringTools.chomp(s,"\\mul{")) return getMultiEqHeight(g,StringTools.spit(s,"\\mul{"),"*");  
    else if (StringTools.chomp(s,"\\add{")) return getMultiEqHeight(g,StringTools.spit(s,"\\add{"),"+");  
    else if (StringTools.chomp(s,"\\stage{")) return SSHeight(g,StringTools.spit(s,"\\stage{"),normalFont);  
    else if (StringTools.chomp(s,"\\var{")) return SSHeight(g,StringTools.spit(s,"\\var{"),normalFont);  
    else if (StringTools.chomp(s,"\\varhist{")) {
      s = StringTools.spit(s,"\\varhist{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      if (lhs.equals("\\? Var{}")) lhs = "\\var{?}";
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      lhs = StringTools.spit(lhs,"\\var{");
      return SSHeight(g,lhs+"_",normalFont)+getEqHeight(g,rhs);

    } else if (StringTools.chomp(s,"\\val{")) {
      s = StringTools.spit(s,"\\val{");
      s = StringTools.LHS(s,StringTools.getUnNested(s,','));
      s = StringTools.spit(s,"\\sival{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      if (!(rhs.equals("0"))) return SSHeight(g,lhs+"x10^{"+rhs+"}",normalFont);
      else return SSHeight(g,lhs,normalFont);
    }
    else if (StringTools.chomp(s,"\\not{")) return getEqHeight(g,StringTools.spit(s,"\\not{"));
    else if (StringTools.chomp(s,"\\greater{")) return getBinBoolEqHeight(g,StringTools.spit(s,"\\greater{"),">");
    else if (StringTools.chomp(s,"\\greaterequal{")) return getBinBoolEqHeight(g,StringTools.spit(s,"\\greaterequal{"),">");
    else if (StringTools.chomp(s,"\\equal{")) return getBinBoolEqHeight(g,StringTools.spit(s,"\\equal{"),"=");
    else if (StringTools.chomp(s,"\\less{")) return getBinBoolEqHeight(g,StringTools.spit(s,"\\less{"),">");
    else if (StringTools.chomp(s,"\\lessequal{")) return getBinBoolEqHeight(g,StringTools.spit(s,"\\lessequal{"),"<");
    else if (StringTools.chomp(s,"\\neq{")) return getBinBoolEqHeight(g,StringTools.spit(s,"\\neq{"),"=");
    else if (StringTools.chomp(s,"\\and{")) return getMultiBoolEqHeight(g,StringTools.spit(s,"\\and{")," and ");
    else if (StringTools.chomp(s,"\\or{")) return getMultiBoolEqHeight(g,StringTools.spit(s,"\\or{")," or ");
    else if (StringTools.chomp(s,"\\someVariety{")) return getEqHeight(g,StringTools.spit(s,"\\someVariety{"));
    else if (StringTools.chomp(s,"\\noVariety{")) return getEqHeight(g,StringTools.spit(s,"\\noVariety{"));
    else if (StringTools.chomp(s,"\\allVariety{")) return getEqHeight(g,StringTools.spit(s,"\\allVariety{"));
    else if (StringTools.chomp(s,"\\assign{")) {
      s = StringTools.spit(s,"\\assign{");
      String lhs = StringTools.spit(StringTools.LHS(s,StringTools.getUnNested(s,',')),"\\var{");
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      return Math.max(getEqHeight(g,rhs),SSHeight(g,lhs,normalFont));
    } else if (StringTools.chomp(s,"\\set{")) {
      s = StringTools.spit(s,"\\set{");
      String lhs = StringTools.spit(StringTools.LHS(s,StringTools.getUnNested(s,',')),"\\var{");
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      return Math.max(getEqHeight(g,rhs),SSHeight(g,lhs,normalFont));
    } else if (StringTools.chomp(s,"\\assigndiff{")) {
      s = StringTools.spit(s,"\\assigndiff{");
      String lhs = StringTools.spit(StringTools.LHS(s,StringTools.getUnNested(s,',')),"\\var{");
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      return Math.max(getEqHeight(g,"\\div{\\var{d"+lhs+"},\\var{dt}}"),getEqHeight(g,rhs));
    } else if (StringTools.chomp(s,"\\ifthen{")) {
      s = StringTools.spit(s,"\\ifthen{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      return Math.max(getEqHeight(g,lhs),getEqHeight(g,rhs));
    } else if (StringTools.chomp(s,"\\create{")) {
      String param = StringTools.spit(s,"\\create{");
      double max = getEqHeight(g,StringTools.LHS(param,StringTools.getUnNested(param,',')));
      param = StringTools.RHS(param,StringTools.getUnNested(param,','));

      while (param.length()>2) {
        double hei = getEqHeight(g,StringTools.LHS(param,StringTools.getUnNested(param,',')));
        max = max + hei;
        if (param.indexOf(',')<0) param = "";
        param = StringTools.RHS(param,StringTools.getUnNested(param,','));
      }
      return max;
    } else if (StringTools.chomp(s,"\\change{")) {
      return SSHeight(g,"change()",normalFont);
    } else if (StringTools.chomp(s,"\\pchange{")) {
      s = StringTools.spit(s,"\\pchange{");
    //  String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      return getEqHeight(g,rhs);
    } else if (StringTools.chomp(s,"\\divide")) {
      return getEqHeight(g,StringTools.spit(s,"\\divide{"));
    } else if (StringTools.chomp(s,"\\release{")) {
      s = StringTools.spit(s,"\\release{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      return Math.max(getEqHeight(g,lhs),getEqHeight(g,rhs));
    } else if (StringTools.chomp(s,"\\remineralise{")) {
      s = StringTools.spit(s,"\\remineralise{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      return Math.max(getEqHeight(g,lhs),getEqHeight(g,rhs));
    } else if (StringTools.chomp(s,"\\uptake{")) {
      s = StringTools.spit(s,"\\uptake{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      return Math.max(getEqHeight(g,lhs),getEqHeight(g,rhs));
    } else if (StringTools.chomp(s,"\\ingest{")) {
      s = StringTools.spit(s,"\\ingest{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      String mid = StringTools.LHS(rhs,StringTools.getUnNested(rhs,','));
      rhs = StringTools.RHS(rhs,StringTools.getUnNested(rhs,','));
      return Math.max(Math.max(getEqHeight(g,lhs),getEqHeight(g,mid)),getEqHeight(g,rhs));
    }
    else {
      //System.out.println("EqPanel error: "+s+" not found in getEqHeight");
      return 18;
    }
  }

  private double getBinEqHeight(Graphics2D g, String s, String op) {
    String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
    String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
    if (op.equals("/")) {
      return getEqHeight(g,lhs)+4+getEqHeight(g,rhs);
    } else if (op.equals("-")) return Math.max(getEqHeight(g,lhs),getEqHeight(g,rhs));
    else if (op.equals("^")) return getEqHeight(g,lhs);
    else return 0.0;
  }

  private double getTriEqHeight(Graphics2D g, String s) {
    //String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
    String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
    String mid = StringTools.LHS(rhs,StringTools.getUnNested(rhs,','));
    rhs = StringTools.RHS(rhs,StringTools.getUnNested(rhs,','));
    return Math.max(getEqHeight(g,rhs),getEqHeight(g,mid));
  }

  private double getMultiEqHeight(Graphics2D g, String s, String op) {
    String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
    String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
    double height = getEqHeight(g,lhs);
    while (StringTools.getUnNested(rhs,',')>=0) {
      lhs = StringTools.LHS(rhs,StringTools.getUnNested(rhs,','));
      height = Math.max(height,getEqHeight(g,lhs));
      rhs = StringTools.RHS(rhs,StringTools.getUnNested(rhs,','));
    }
    height = Math.max(height,getEqHeight(g,rhs));
    return height;
  }

  private double getBinBoolEqHeight(Graphics2D g, String s, String op) {
    String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
    String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
    return Math.max(getEqHeight(g,lhs),getEqHeight(g,rhs));
  }

  private double getMultiBoolEqHeight(Graphics2D g, String s, String op) {
    String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
    String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
    double height = getEqHeight(g,lhs);
    while (StringTools.getUnNested(rhs,',')>=0) {
      lhs = StringTools.LHS(rhs,StringTools.getUnNested(rhs,','));
      height = Math.max(height,getEqHeight(g,lhs));
      rhs = StringTools.RHS(rhs,StringTools.getUnNested(rhs,','));
    }
    height = Math.max(height,getEqHeight(g,rhs));
    return height;
  }

  private double getStatWidth(Graphics2D g, String s) {
    if (StringTools.chomp(s,"\\assign{")) {
      s = StringTools.spit(s,"\\assign{");
      double lhs = SSWidth(g,StringTools.spit(StringTools.LHS(s,StringTools.getUnNested(s,',')),"\\var{"),normalFont);
      double rhs = getEqWidth(g,StringTools.RHS(s,StringTools.getUnNested(s,',')));
      return lhs+rhs;
    } if (StringTools.chomp(s,"\\set{")) {
      s = StringTools.spit(s,"\\set{");
      double lhs = SSWidth(g,StringTools.spit(StringTools.LHS(s,StringTools.getUnNested(s,',')),"\\var{"),normalFont);
      double rhs = getEqWidth(g,StringTools.RHS(s,StringTools.getUnNested(s,',')));
      return lhs+rhs;

    } else if (StringTools.chomp(s,"\\assigndiff{")) {
      s = StringTools.spit(s,"\\assigndiff{");
      String lhs = StringTools.spit(StringTools.LHS(s,StringTools.getUnNested(s,',')),"\\var{");
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      return getEqWidth(g,"\\div{\\var{d"+lhs+"},\\var{dt}}")+textWidth(g, " = ", normalFont)+getEqWidth(g,rhs);
    } else if (StringTools.chomp(s,"ifthen{")) {
      s = StringTools.spit(s,"\\ifthen{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      return getBoolEqWidth(g,lhs)+getFuncWidth(g,rhs)+textWidth(g, "if, ", normalFont);
    } else return 0;
  }

  private double getFuncWidth(Graphics2D g, String s) {
    if (StringTools.chomp(s,"\\change{")) return textWidth(g, "change", normalFont);
    else if (StringTools.chomp(s,"\\create{")) {
      String param = StringTools.spit(s,"\\create{");
      double max = textWidth(g, "create", normalFont)+getStatWidth(g,StringTools.LHS(param,StringTools.getUnNested(param,',')));
      param = StringTools.RHS(param,StringTools.getUnNested(param,','));
      while (param.length()>2) {
        double wid = getFuncWidth(g,StringTools.LHS(param,StringTools.getUnNested(param,',')));
        if (max>wid) max = wid;
        if (param.indexOf(',')<=0) param = "";
        param = StringTools.RHS(param,StringTools.getUnNested(param,','));
      }
      return max;
    }
      
    else if (StringTools.chomp(s,"\\pchange{")) return textWidth(g, "pchange", normalFont);
    else if (StringTools.chomp(s,"\\divide{")) return textWidth(g, "divide", normalFont);
    else if (StringTools.chomp(s,"\\ingest{")) return textWidth(g, "ingest", normalFont);
    else if (StringTools.chomp(s,"\\release{")) return textWidth(g, "release", normalFont);
    else if (StringTools.chomp(s,"\\remineralise{")) return textWidth(g, "remineralise", normalFont);    
    else if (StringTools.chomp(s,"\\uptake{")) return textWidth(g, "uptake", normalFont);
    else if (StringTools.chomp(s,"\\assign{")) return getStatWidth(g,s);
    else if (StringTools.chomp(s,"\\assigndiff{")) return getStatWidth(g,s);
    else return 0;
  }

  public int writeEq(Graphics2D g, int x, int y, String s) {
    if (StringTools.chomp(s,"\\assigndiff{")) {
      s = StringTools.spit(s,"\\assigndiff{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      if (StringTools.chomp(s,"\\var{")) lhs = StringTools.spit(s,"\\var{"); else lhs="";
      lhs = "\\div{\\var{d"+lhs+"},\\var{dt}}";
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      int rHeight = (int) getEqHeight(g,rhs);
      int lHeight = (int) getEqHeight(g,lhs);
      int top = (int) Math.min(getEqTop(g,lhs),getEqTop(g,rhs));
      x = writeEq(g,x,y-top,lhs);
      g.setFont(new Font(normalFont.getName(),Font.BOLD,normalFont.getSize()));
      x = plotSS(g,x,y-top," = ");
      g.setFont(normalFont);
      x = writeEq(g,x,y-top,rhs);
      y+=Math.max(rHeight,lHeight)+10;
      x=0;
    } else if (StringTools.chomp(s,"\\ifthen{")) {
      s = StringTools.spit(s,"\\ifthen{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      int rHeight = (int) getEqHeight(g,rhs);
      int lHeight = (int) getEqHeight(g,lhs);
      int top = (int) Math.min(getEqTop(g,lhs),getEqTop(g,rhs));
      x = plotSS(g,x,y-top,"if ");
      x = writeEq(g,x,y-top,lhs);
      g.setFont(boldFont);
      x = plotSS(g,x,y-top,", ");
      g.setFont(normalFont);
      x = writeEq(g,x,y-top,rhs);
      y+=Math.max(rHeight,lHeight)+10;
      x=0;
    } else if (StringTools.chomp(s,"\\assign{")) {
      s = StringTools.spit(s,"\\assign{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      if (StringTools.chomp(s,"\\var{")) lhs = StringTools.spit(s,"\\var{"); else lhs="";
      int top = (int) getEqTop(g,rhs);
      x = plotSS(g,x,y-top,lhs);
      x = plotSS(g,x,y-top,"=");
      x = writeEq(g,x,y-top,rhs);

    } else if (StringTools.chomp(s,"\\set{")) {
      s = StringTools.spit(s,"\\set{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      if (StringTools.chomp(s,"\\var{")) lhs = StringTools.spit(s,"\\var{"); else lhs="";
      x = plotSS(g,x,y,lhs);
      x = plotSS(g,x,y,"=");
      x = writeEq(g,x,y,rhs);
    
    } else if (StringTools.chomp(s,"\\stage{")) {
      s = StringTools.spit(s,"\\stage{");
      x = plotSS(g,x,y,s);    
    } else if (StringTools.chomp(s,"\\minus{")) {
      g.setFont(boldFont);
      x = plotSS(g,x,y,"-");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,StringTools.spit(s,"\\minus{"));
      x = plotSS(g,x,y,")");
    } else if (StringTools.chomp(s,"\\abs{")) {
      g.setFont(boldFont);
      x = plotSS(g,x,y,"|");
      g.setFont(normalFont);
      x = writeEq(g,x,y,StringTools.spit(s,"\\abs{"));
      g.setFont(boldFont);
      x = plotSS(g,x,y,"|");
      g.setFont(normalFont);
    } else if (StringTools.chomp(s,"\\varietymul{")) {
      g.setFont(boldFont);
      x = plotSS(g,x,y,"varietymul)");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,StringTools.spit(s,"\\varietymul{"));
      x = plotSS(g,x,y,")");
    } else if (StringTools.chomp(s,"\\varietysum{")) {
      g.setFont(boldFont);
      x = plotSS(g,x,y,"varietysum");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,StringTools.spit(s,"\\varietysum{"));
      x = plotSS(g,x,y,")");
    } else if (StringTools.chomp(s,"\\rnd{")) {
      g.setFont(boldFont);
      x = plotSS(g,x,y,"rnd");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,StringTools.spit(s,"\\rnd{"));
      x = plotSS(g,x,y,")");
    } else if (StringTools.chomp(s,"\\integrate{")) {
      g.setFont(boldFont);
      x = plotSS(g,x,y,"integrate");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,StringTools.spit(s,"\\integrate{"));
      x = plotSS(g,x,y,")");
    } else if (StringTools.chomp(s,"\\ln{")) {
      g.setFont(boldFont);
      x = plotSS(g,x,y,"ln");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,StringTools.spit(s,"\\ln{"));
      x = plotSS(g,x,y,")");
    } else if (StringTools.chomp(s,"\\log10{")) {
      
      g.setFont(boldFont);
      x = plotSS(g,x,y,"log${10}");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"("); 
      x = writeEq(g,x,y,StringTools.spit(s,"\\log10{"));
      x = plotSS(g,x,y,")");
    } else if (StringTools.chomp(s,"\\asin{")) {
      
      g.setFont(boldFont);
      x = plotSS(g,x,y,"sin^{-1}");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,StringTools.spit(s,"\\asin{"));
      x = plotSS(g,x,y,")");
    } else if (StringTools.chomp(s,"\\acos{")) {
      
      g.setFont(boldFont);
      x = plotSS(g,x,y,"cos^{-1}");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,StringTools.spit(s,"\\acos{"));
      x = plotSS(g,x,y,")");
    } else if (StringTools.chomp(s,"\\atan{")) {
      
      g.setFont(boldFont);
      x = plotSS(g,x,y,"tan^{-1}");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,StringTools.spit(s,"\\atan{"));
      x = plotSS(g,x,y,")");
    } else if (StringTools.chomp(s,"\\sin{")) {
      
      g.setFont(boldFont);
      x = plotSS(g,x,y,"sin");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,StringTools.spit(s,"\\sin{"));
      x = plotSS(g,x,y,")");

    } else if (StringTools.chomp(s,"\\visIrradAt{")) {
      g.setFont(boldFont);
      x = plotSS(g,x,y,"visIrradAt");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,StringTools.spit(s,"\\visIrradAt{"));
      x = plotSS(g,x,y,")");

    } else if (StringTools.chomp(s,"\\fullIrradAt{")) {
      g.setFont(boldFont);
      x = plotSS(g,x,y,"fullIrradAt");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,StringTools.spit(s,"\\fullIrradAt{"));
      x = plotSS(g,x,y,")");

    } else if (StringTools.chomp(s,"\\salinityAt{")) {
      g.setFont(boldFont);
      x = plotSS(g,x,y,"salinityAt");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,StringTools.spit(s,"\\salinityAt{"));
      x = plotSS(g,x,y,")");

    } else if (StringTools.chomp(s,"\\densityAt{")) {
      g.setFont(boldFont);
      x = plotSS(g,x,y,"densityAt");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,StringTools.spit(s,"\\densityAt{"));
      x = plotSS(g,x,y,")");

    } else if (StringTools.chomp(s,"\\temperatureAt{")) {
      g.setFont(boldFont);
      x = plotSS(g,x,y,"temperatureAt");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,StringTools.spit(s,"\\temperatureAt{"));
      x = plotSS(g,x,y,")");

    } else if (StringTools.chomp(s,"\\pchange{")) {
      s = StringTools.spit(s,"\\pchange{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      g.setFont(boldFont);
      x = plotSS(g,x,y,"pchange");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,lhs);
      x = plotSS(g,x,y,",");
      x = writeEq(g,x,y,rhs);
      x = plotSS(g,x,y,")");

    } else if (StringTools.chomp(s,"\\ingest{")) {
      s = StringTools.spit(s,"\\ingest{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      String mid = StringTools.LHS(rhs,StringTools.getUnNested(rhs,','));
      rhs = StringTools.RHS(rhs,StringTools.getUnNested(rhs,','));
      g.setFont(boldFont);
      x = plotSS(g,x,y,"ingest");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,lhs);
      x = plotSS(g,x,y,",");
      x = writeEq(g,x,y,mid);
      x = plotSS(g,x,y,",");
      x = writeEq(g,x,y,rhs);
      x = plotSS(g,x,y,")");

    } else if (StringTools.chomp(s,"\\divide{")) {
      g.setFont(boldFont);
      x = plotSS(g,x,y,"divide");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,StringTools.spit(s,"\\divide{"));
      x = plotSS(g,x,y,")");

    } else if (StringTools.chomp(s,"\\cos{")) {
      
      g.setFont(boldFont);
      x = plotSS(g,x,y,"cos");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,StringTools.spit(s,"\\cos{"));
      x = plotSS(g,x,y,")");
    } else if (StringTools.chomp(s,"\\tan{")) {
      
      g.setFont(boldFont);
      x = plotSS(g,x,y,"tan");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,StringTools.spit(s,"\\tan{"));
      x = plotSS(g,x,y,")");
    } else if (StringTools.chomp(s,"\\sqrt{")) {
      
      g.setFont(boldFont);
      g.drawString("sqrt(",x,y);
      x+=textWidth(g, "sqrt(", normalFont);
      x = writeEq(g,x,y,StringTools.spit(s,"\\sqrt{"));
      g.drawString(")",x,y);
      x+=textWidth(g, ")", normalFont);
    } else if (StringTools.chomp(s,"\\exp{")) {
      g.setFont(boldFont);
      x = plotSS(g,x,y,"e");
      g.setFont(normalFont);
      int newSize = (int) Math.max(6,normalFont.getSize()/1.5);
    //  int raiseBy = newSize/2;
      Font storeFont = normalFont;
      normalFont = new Font(normalFont.getName(),normalFont.getStyle(),newSize);
      g.setFont(normalFont);
      x = writeEq(g,x,y-3,StringTools.spit(s,"\\exp{"));
      normalFont = storeFont;
      g.setFont(normalFont);
    } else if (StringTools.chomp(s,"\\pow{")) {
      s = StringTools.spit(s,"\\pow{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      x = writeEq(g,x,y,lhs);
      int newSize = (int) Math.max(6,normalFont.getSize()/1.2);
      int raiseBy = newSize/3;
      Font storeFont = normalFont;
      normalFont = new Font(normalFont.getName(),normalFont.getStyle(),newSize);
      g.setFont(normalFont);
      x = writeEq(g,x,y-raiseBy,rhs);
      normalFont = storeFont;
      g.setFont(normalFont);
    } else if (StringTools.chomp(s,"\\div{")) {
      s = StringTools.spit(s,"\\div{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      int widTop = (int) getEqWidth(g,lhs);
      int widBot = (int) getEqWidth(g,rhs);
      int heiTop = (int) getEqHeight(g,lhs);
      int heiBot = (int) getEqHeight(g,rhs);
      int wMax= Math.max(widTop,widBot);
      int x2 = writeEq(g,x+((wMax/2)-(widTop/2)),y-((heiTop/2)+2),lhs);
      int x3 = writeEq(g,x+((wMax/2)-(widBot/2)),y+((heiBot/2)+2),rhs);
      g.drawLine(x,y,Math.max(x3,x2),y);
      x = Math.max(x3,x2)+2;
    } else if (StringTools.chomp(s,"\\sub{")) {
      s = StringTools.spit(s,"\\sub{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,lhs);
      
      g.setFont(boldFont);
      x = plotSS(g,x,y,"-");
      g.setFont(normalFont);
      x = writeEq(g,x,y,rhs);
      x = plotSS(g,x,y,")");
    } else if (StringTools.chomp(s,"\\conditional{")) {
      s = StringTools.spit(s,"\\conditional{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      String mid = StringTools.LHS(rhs,StringTools.getUnNested(rhs,','));
      rhs = StringTools.RHS(rhs,StringTools.getUnNested(rhs,','));
      x = plotSS(g,x,y,"(if ");
      x = writeEq(g,x,y,lhs);
      x = plotSS(g,x,y,"then ");
      x = writeEq(g,x,y,mid);
      x = plotSS(g,x,y,"else ");
      x = writeEq(g,x,y,rhs);
      x = plotSS(g,x,y,")");
    } else if (StringTools.chomp(s,"\\varhist{")) {
      s = StringTools.spit(s,"\\varhist{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      if (lhs.equals("\\? Var{}")) lhs = "\\var{?}";
      lhs = StringTools.spit(lhs,"\\var{");
      x = plotSS(g,x,y,lhs+"[");
      x = writeEq(g,x,y,rhs);
      x = plotSS(g,x,y,"]");

    } else if (StringTools.chomp(s,"\\var{")) {
      s = StringTools.spit(s,"\\var{");
      x = plotSS(g,x,y,s);
    } else if (StringTools.chomp(s,"\\delta{")) {
      s = "\\div{\\var{d"+StringTools.spit(StringTools.spit(s,"\\delta{"),"\\var{")+"},\\var{dt}}";
      x = writeEq(g,x,y,s);
    } else if (StringTools.chomp(s,"\\val{")) {
      s = StringTools.spit(s,"\\val{");
      s = StringTools.LHS(s,StringTools.getUnNested(s,','));
      s = StringTools.spit(s,"\\sival{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      if ((rhs.equals("0"))) x = plotSS(g,x,y,lhs);
      else x = plotSS(g,x,y,lhs+" x 10^{"+rhs+"}");
    } else if ((StringTools.chomp(s,"\\max{"))||(StringTools.chomp(s,"\\min{"))||(StringTools.chomp(s,"\\add{"))||(StringTools.chomp(s,"\\mul{"))
              ||(StringTools.chomp(s,"\\and{"))||(StringTools.chomp(s,"\\or{"))||(StringTools.chomp(s,"\\someVariety{"))
              ||(StringTools.chomp(s,"\\allVariety{"))||(StringTools.chomp(s,"\\noVariety{"))) {
      String preop = "";
      String midop = "";
      if (StringTools.chomp(s,"\\max{")) { preop = "max "; midop = ","; s = StringTools.spit(s,"\\max{"); }
      else if (StringTools.chomp(s,"\\min{")) { preop = "min "; midop = ","; s = StringTools.spit(s,"\\min{"); }
      else if (StringTools.chomp(s,"\\add{")) { midop = "+"; s = StringTools.spit(s,"\\add{"); }
      else if (StringTools.chomp(s,"\\someVariety{")) { midop = ""; s = StringTools.spit(s,"\\someVariety{"); }
      else if (StringTools.chomp(s,"\\allVariety{")) { midop = ""; s = StringTools.spit(s,"\\allVariety{"); }
      else if (StringTools.chomp(s,"\\noVariety{")) { midop = ""; s = StringTools.spit(s,"\\noVariety{"); }
      else if (StringTools.chomp(s,"\\mul{")) { midop = ""; s = StringTools.spit(s,"\\mul{"); }
      else if (StringTools.chomp(s,"\\and{")) { midop = " and "; s = StringTools.spit(s,"\\and{"); }
      else if (StringTools.chomp(s,"\\or{")) { midop = " or "; s = StringTools.spit(s,"\\or{"); }

      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      if (rhs.equals("")) midop="";
      
      g.setFont(boldFont);
      x = plotSS(g,x,y,preop);
      g.setFont(normalFont);
      if (BRACKETS) x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,lhs);
      g.setFont(boldFont);
      if (midop.length()>0) x = plotSS(g,x,y,midop);
      g.setFont(normalFont);
      while (StringTools.getUnNested(rhs,',')>=0) {
        lhs = StringTools.LHS(rhs,StringTools.getUnNested(rhs,','));
        rhs = StringTools.RHS(rhs,StringTools.getUnNested(rhs,','));
        x = writeEq(g,x,y,lhs);
        g.setFont(boldFont);
        if (midop.length()>0) x = plotSS(g,x,y,midop);
        g.setFont(normalFont);
      }
      x = writeEq(g,x,y,rhs);
      if (BRACKETS) x = plotSS(g,x,y,")");
    } else if (StringTools.chomp(s,"\\not{")) {
      g.setFont(boldFont);
      s = StringTools.spit(s,"\\not{");
      x = plotSS(g,x,y,"!");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,s);
      x = plotSS(g,x,y,")");
    } else if ((StringTools.chomp(s,"\\greater{"))||(StringTools.chomp(s,"\\greaterequal{"))||(StringTools.chomp(s,"\\equal{"))
            ||(StringTools.chomp(s,"\\less{"))||(StringTools.chomp(s,"\\lessequal{"))||(StringTools.chomp(s,"\\neq{"))) {
      String symb = "";
      if (StringTools.chomp(s,"\\greater{")) { symb = ">"; s = StringTools.spit(s,"\\greater{"); }
      else if (StringTools.chomp(s,"\\greaterequal{")) { symb = "\u2265"; s = StringTools.spit(s,"\\greaterequal{"); }
      else if (StringTools.chomp(s,"\\equal{")) { symb = "="; s = StringTools.spit(s,"\\equal{"); }
      else if (StringTools.chomp(s,"\\less{")) { symb = "<"; s = StringTools.spit(s,"\\less{"); }
      else if (StringTools.chomp(s,"\\lessequal{")) { symb = "\u2264"; s = StringTools.spit(s,"\\lessequal{"); }
      else if (StringTools.chomp(s,"\\neq{")) { symb = "~"; s = StringTools.spit(s,"\\neq{"); }
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,lhs);
      
      g.setFont(boldFont);
      x = plotSS(g,x,y,symb);
      g.setFont(normalFont);
      x = writeEq(g,x,y,rhs);
      x = plotSS(g,x,y,")");
    } else if (StringTools.chomp(s,"\\change{")) {
      g.setFont(boldFont);
      x = plotSS(g,x,y,"change(");
      g.setFont(normalFont);
      x = writeEq(g,x,y,StringTools.spit(s,"\\change{"));
      g.setFont(boldFont);
      x = plotSS(g,x,y,")");
      g.setFont(normalFont);
    } else if (StringTools.chomp(s,"\\create{")) {
      g.setFont(boldFont);
      x = plotSS(g,x,y,"create");      
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      String param= StringTools.spit(s,"\\create{");
      int storeX = x;
    //  int storeY = y;
      x = writeEq(g,x,y,StringTools.LHS(param,StringTools.getUnNested(param,',')));
      x = plotSS(g,x,y,",");
      param = StringTools.RHS(param,StringTools.getUnNested(param,','));
      x = writeEq(g,x,y,StringTools.LHS(param,StringTools.getUnNested(param,',')));
      x = plotSS(g,x,y,")");
      param = StringTools.RHS(param,StringTools.getUnNested(param,','));
      while (param.length()>2) {
        x = storeX;
        String theparam = StringTools.LHS(param,StringTools.getUnNested(param,','));
        y += getEqHeight(g,theparam);
        x = writeEq(g,x,y,theparam);
        if (param.indexOf(',')<0) param = "";
        param = StringTools.RHS(param,StringTools.getUnNested(param,','));
      }

    } else if (StringTools.chomp(s,"\\divide{")) {
      g.setFont(boldFont);
      x = plotSS(g,x,y,"divide");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      x = writeEq(g,x,y,StringTools.spit(s,"\\divide{"));
      x = plotSS(g,x,y,")");
    } else if (StringTools.chomp(s,"\\uptake{")) {
      g.setFont(boldFont);
      x = plotSS(g,x,y,"uptake");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      s = StringTools.spit(s,"\\uptake{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      x = writeEq(g,x,y,lhs);
      x = plotSS(g,x,y,",");
      x = writeEq(g,x,y,rhs);
      x = plotSS(g,x,y,")");
    } else if ((StringTools.chomp(s,"\\release{")) || (StringTools.chomp(s,"\\remineralise{"))) {
      g.setFont(boldFont);
      x = plotSS(g,x,y,"release");
      g.setFont(normalFont);
      x = plotSS(g,x,y,"(");
      if (StringTools.chomp(s,"\\release{")) s = StringTools.spit(s,"\\release{");
      else s = StringTools.spit(s,"\\remineralise{");
      String lhs = StringTools.LHS(s,StringTools.getUnNested(s,','));
      String rhs = StringTools.RHS(s,StringTools.getUnNested(s,','));
      x = writeEq(g,x,y,lhs);
      x = plotSS(g,x,y,",");
      x = writeEq(g,x,y,rhs);
      x = plotSS(g,x,y,")");
    }

    return x;
  }

  public void paintComponent(Graphics g2) {
    super.paintComponent(g2);
    
    Graphics2D g = (Graphics2D) g2;

    g.setFont(normalFont);
    int maxwidth = 0;
    int x = 0;
    int y = 18;
    String s = EQ;
    int linecount = 0;
    while (s.indexOf("\\newline")>=0) {
      s = s.substring(s.indexOf("\\newline")+8);
      linecount++;
    }
    String eqlist = EQ;
    if (linecount==0) { linecount = 1; eqlist += "\\newline"; }
    for (int i=0; i<linecount; i++) {
      s = new String(eqlist.substring(0,eqlist.indexOf("\\newline")));
      if (eqlist.length()-eqlist.indexOf("\\newline")>8) 
        eqlist = eqlist.substring(eqlist.indexOf("\\newline")+8);
      else eqlist = "";
      if (StringTools.chomp(s,"\\txt{")) {
        smallFonts();
        String eq = "\\var{"+StringTools.spit(s,"\\txt{")+"}";
      //  int height = (int) getEqHeight(g,eq);
        x = writeEq(g,x,y,eq);
      } else if (StringTools.chomp(s,"\\unit{")) {
        if (s.equals("\\unit{0,0,0}")) s = "\\unit{}";
        smallFonts();
        BRACKETS = false;
        String eq = new String();
        int count = 0;
        s = StringTools.spit(s,"\\unit{");
        while (s.length()>0) {
          String pre = StringTools.LHS(s,StringTools.getUnNested(s,','));
          s = StringTools.RHS(s,StringTools.getUnNested(s,','));
          String mid = StringTools.LHS(s,StringTools.getUnNested(s,','));
          s = StringTools.RHS(s,StringTools.getUnNested(s,','));
          if (s.indexOf(",")==-1) s+=",";
          String sup = StringTools.LHS(s,StringTools.getUnNested(s,','));
          s = StringTools.RHS(s,StringTools.getUnNested(s,','));
          if (pre.equals("-3")) pre = "m";
          else if (pre.equals("-6")) pre= "u";
          else if (pre.equals("-9")) pre = "n";
          else if (pre.equals("-12")) pre = "p";
          else if (pre.equals("3")) pre = "k";
          else if (pre.equals("6")) pre = "M";
          else if (pre.equals("9")) pre = "G";
          else if (pre.equals("12")) pre = "T";
          else pre = "";
          if (sup.equals("1")) eq += "\\var{"+pre+mid+"}"; else
            eq += "\\pow{\\var{"+pre+mid+"},\\var{"+sup+"}}";
          if (s.length()>0) eq+=",";
          count++;
        }
        if (count>1) eq = "\\mul{"+eq+"}";
        int height = (int) getEqHeight(g,eq);
        x = writeEq(g,x,y,eq);
        y = y + height + 2;
        BRACKETS = true;
        defaultFonts();
      } else if (StringTools.chomp(s,"\\var{")) {
        s = StringTools.spit(s,"\\var{");
        smallFonts();
        x = plotSS(g,x,y,s);
        defaultFonts();
      }
      
      else {
        int height = (int) getEqHeight(g,s);
        maxwidth = (int)Math.max(getEqWidth(g,s),maxwidth);
        writeEq(g, x, y, s);
        y = y + height;
      }
    }
    setPreferredSize(new Dimension(2*Math.max(800,maxwidth), y + 10));
    revalidate();
  }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

