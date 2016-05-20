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
 * @(#)FormatterFactory.java 
 *
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

package com.sun.stc.jcsre;

import java.util.Map;
import java.util.HashMap;

import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import java.io.UnsupportedEncodingException;


import com.sun.stc.eways.util.MonkUtils;
import com.sun.stc.jcsre.IStringCoder;
import com.sun.stc.jcsre.JCSProperties;
import com.sun.stc.jcsre.StringCoderFactory;

import java.util.Date;

/**
 * This class is used to create and cache formatters.
 *
 * The formatters are used to parse/render java primitive
 * types (boolean, short, int, long, float, double) and certain
 * objects (java.util.Date).
 *
 * This class is thread safe.
 */
public class FormatterFactory {

  private static final boolean unitTest = false;

  /**
   * for now we'll just use one map for all formatters.
   */
  private static Map cache = new HashMap(101);

  /**
   * default numeric formatter
   */
  private static final NumberFormat DefaultNumberFormat =
    getNumberFormat("#.##;-#.##");

  /**
   * default boolean formatter
   */
  private static final BooleanFormat DefaultBooleanFormat =
    getBooleanFormat("t|true|y|yes|1;f|false|n|no|0;true;false");

  /**
   * default date formatter
   */
  private static final DateFormat DefaultDateFormat =
    getDateFormat("yyyy-MM-dd HH:mm:ss z");

  /**
   * default string formatter
   */
  private static final StringFormat DefaultStringFormat =
    getStringFormat("%s");

  /**
   * inner class to wrap numbers and does range checking.
   */
  public static class RangeCheckedNumber extends Number {

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
     * the encapsulated Number
     */
    private Number number = null;

    /**
     * construct an instance that encapsulates the specified
     * Number
     *
     * @param _number the encapsulated number
     */
    public RangeCheckedNumber (Number _number) {
      this.number = _number;
    }

    /**
     * get the double value of the number.
     *
     * @return the double value of the number
     *
     * @throws NumberFormatException if the number is Double.POSITIVE_INFINITY,
     * Double.NEGATIVE_INFINITY, or Double.NaN
     */
    public double doubleValue () throws NumberFormatException {

      double value = this.number.doubleValue();

      if(Double.POSITIVE_INFINITY == value
        || Double.NEGATIVE_INFINITY == value
        || Double.NaN == value
      ) {
        throw new NumberFormatException("Value is out of range.");
      }

      return value;
    }

    /**
     * get the float value of the number.
     *
     * @return the float value of the number
     *
     * @throws NumberFormatException if the number is Float.POSITIVE_INFINITY,
     * Float.NEGATIVE_INFINITY, Float.NaN, greater than Float.MAX_VALUE,
     * or less than Float.MIN_VALUE.
     */
    public float floatValue () throws NumberFormatException {

      double value = this.number.doubleValue();

      if(Double.POSITIVE_INFINITY == value
        || Double.NEGATIVE_INFINITY == value
        || Double.NaN == value
        || (value > 0 &&
              (value > RangeCheckedNumber.MAX_FLOAT
                || value < RangeCheckedNumber.MIN_FLOAT
              )
            )
        || (value < 0 &&
              (-value > RangeCheckedNumber.MAX_FLOAT
                || -value < RangeCheckedNumber.MIN_FLOAT
              )
            )
      ) {
        throw new NumberFormatException("Value is out of range.");
      }



      return this.number.floatValue();
    }

    /**
     * get the long value of the number.
     *
     * @return the long value of the number
     *
     * @throws NumberFormatException if the number is Long.POSITIVE_INFINITY,
     * Long.NEGATIVE_INFINITY, Long.NaN, greater than Long.MAX_VALUE,
     * or less than Long.MIN_VALUE.
     */
    public long longValue () throws NumberFormatException {

      double value = this.number.doubleValue();

      if(Double.POSITIVE_INFINITY == value
        || Double.NEGATIVE_INFINITY == value
        || Double.NaN == value
        || value > RangeCheckedNumber.MAX_LONG
        || value < RangeCheckedNumber.MIN_LONG
      ) {
        throw new NumberFormatException("Value is out of range.");
      }

      return this.number.longValue();
    }

    /**
     * get the int value of the number.
     *
     * @return the int value of the number
     *
     * @throws NumberFormatException if the number is Integer.POSITIVE_INFINITY,
     * Integer.NEGATIVE_INFINITY, Integer.NaN, greater than Integer.MAX_VALUE,
     * or less than Integer.MIN_VALUE.
     */
    public int intValue () throws NumberFormatException {

      double value = this.number.doubleValue();

      if(Double.POSITIVE_INFINITY == value
        || Double.NEGATIVE_INFINITY == value
        || Double.NaN == value
        || value > RangeCheckedNumber.MAX_INT
        || value < RangeCheckedNumber.MIN_INT
      ) {
        throw new NumberFormatException("Value is out of range.");
      }

      return this.number.intValue();
    }

    /**
     * get the short value of the number.
     *
     * @return the short value of the number
     *
     * @throws NumberFormatException if the number is Short.POSITIVE_INFINITY,
     * Short.NEGATIVE_INFINITY, Short.NaN, greater than Short.MAX_VALUE,
     * or less than Short.MIN_VALUE.
     */
    public short shortValue () throws NumberFormatException {

      double value = this.number.doubleValue();

      if(Double.POSITIVE_INFINITY == value
        || Double.NEGATIVE_INFINITY == value
        || Double.NaN == value
        || value > RangeCheckedNumber.MAX_SHORT
        || value < RangeCheckedNumber.MIN_SHORT
      ) {
        throw new NumberFormatException("Value is out of range.");
      }

      return this.number.shortValue();
    }

    /**
     * get the byte value of the number.
     *
     * @return the byte value of the number
     *
     * @throws NumberFormatException if the number is Byte.POSITIVE_INFINITY,
     * Byte.NEGATIVE_INFINITY, Byte.NaN, greater than Byte.MAX_VALUE,
     * or less than Byte.MIN_VALUE.
     */
    public byte byteValue () throws NumberFormatException {

      double value = this.number.doubleValue();

      if(Double.POSITIVE_INFINITY == value
        || Double.NEGATIVE_INFINITY == value
        || Double.NaN == value
        || value > RangeCheckedNumber.MAX_BYTE
        || value < RangeCheckedNumber.MIN_BYTE
      ) {
        throw new NumberFormatException("Value is out of range.");
      }

      return this.number.byteValue();
    }

  }

  /**
   * this inner class wraps NumberFormat's parse method and
   * returns a RangeCheckedNumber
   *
   * @see java.text.NumberFormat
   */
  public static class RangeCheckedDecimalFormat extends DecimalFormat {

    /**
     * create an empty instance
     */
    public RangeCheckedDecimalFormat() {super();}

    /**
     * construct an instance from a string.
     *
     * @param _pattern the parse/render pattern
     */
    public RangeCheckedDecimalFormat (String _pattern) {
      super(_pattern);
    }


    /**
     * wrap the parsed java.lang.Number in a RangeCheckedNumber
     *
     * @see java.text.NumberFormat#parse(String)
     */
    public Number parse (String _value) throws ParseException {
      return new RangeCheckedNumber(super.parse(_value));
    }

    /**
     * parse a byte array in the specified encoding
     *
     * @param _bytes the byte array
     * @param _enc the encoding
     *
     * @return the number
     *
     * @throws ParseException unable to parse
     * @throws UnsupportedEncodingException _enc bad
     */
    public Number parse (byte[] _bytes, String _enc)
      throws ParseException, UnsupportedEncodingException
    {
      return parse(StringCoderFactory.getStringCoder(_enc).decode(_bytes));
    }

    /**
     * Formats the specified long into a byte array with specified coder.
     *
     * @param _value  the value to format
     * @param _coder  string coder to use
     *
     * @return byte array containing formatted number
     *
     * @throws unsupported encoding exception
     */
    public byte[] format (long _value, IStringCoder _coder)
      throws UnsupportedEncodingException
    {
      return _coder.encode(format(_value));
    }

    /**
     * Formats the specified long into a byte array with specified encoding.
     * This is for backward compatibility with e*Gate 4.5.1.
     *
     * @param _value the value to format
     * @param _enc encoding to use
     *
     * @return byte array containing formatted number
     *
     * @throws unsupported encoding exception
     */
    public byte[] format (long _value, String _enc)
      throws UnsupportedEncodingException
    {
      return format(_value, StringCoderFactory.getStringCoder(_enc));
    }

    /**
     * Formats the specified double into a byte array with specified coder.
     *
     * @param _value  the value to format
     * @param _coder  string coder to use
     *
     * @return byte array containing formatted number
     *
     * @throws unsupported encoding exception
     */
    public byte[] format (double _value, IStringCoder _coder)
      throws UnsupportedEncodingException
    {
      return _coder.encode(format(_value));
    }

    /**
     * Formats the specified double into a byte array with specified encoding.
     * This is for backward compatibility with e*Gate 4.5.1.
     *
     * @param _value the value to format
     * @param _enc encoding to use
     *
     * @return byte array containing formatted number
     *
     * @throws unsupported encoding exception
     */
    public byte[] format (double _value, String _enc)
      throws UnsupportedEncodingException
    {
      return format(_value, StringCoderFactory.getStringCoder(_enc));
    }

  }

  /**
   * This method returns a number formatter that will parse
   * and render numeric primitives according to the specified
   * pattern.
   *
   * This method is thread safe.
   *
   * @param _pattern the parsing/rendering pattern.
   *
   * @return the number formatter
   */
  public static NumberFormat getNumberFormat (String _pattern) {

    // return default if parameter is null
    if(null == _pattern) {
      return DefaultNumberFormat;
    }

    NumberFormat formatter = null;

    synchronized(FormatterFactory.cache) {

      // try to get formatter from cache
      formatter = (NumberFormat)FormatterFactory.cache.get(_pattern);

      // create formatter if not in cache
      if(null == formatter) {

/*
        // COBOL style
        if(null == formatter) {
            try {
                formatter = new COBOLRangeCheckedDecimalFormat(_pattern);
            } catch(Exception ex) {
                formatter = null;
            }
        }
*/

        // C style
        if(null == formatter) {
            try {
                formatter = new CRangeCheckedDecimalFormat(_pattern);
            } catch(Exception ex) {
                formatter = null;
            }
        }

        // default
        if(null == formatter) {
            formatter = new RangeCheckedDecimalFormat(_pattern);
        }

        FormatterFactory.cache.put(_pattern, formatter);
      }

    }

    return formatter;
  }

  /**
   * This method returns a boolean formatter that will parse
   * and render boolean primitives according to the specified
   * pattern.
   *
   * This method is thread safe.
   *
   * @param _pattern the parsing/rendering pattern.
   *
   * @return the boolean formatter
   */
  public static BooleanFormat getBooleanFormat (String _pattern) {

    // return default if parameter is null
    if(null == _pattern) {
      return DefaultBooleanFormat;
    }

    BooleanFormat formatter = null;

    synchronized(FormatterFactory.cache) {

      // try to get formatter from cache
      formatter = (BooleanFormat)FormatterFactory.cache.get(_pattern);

      // create formatter if not in cache
      if(null == formatter) {
        formatter = new BooleanFormat(_pattern);
        FormatterFactory.cache.put(_pattern, formatter);
      }

    }

    return formatter;
  }

  /**
   * This method returns a string formatter that will parse
   * and render boolean primitives according to the specified
   * pattern.
   *
   * This method is thread safe.
   *
   * @param _pattern the parsing/rendering pattern.
   *
   * @return the string formatter
   */
  public static StringFormat getStringFormat (String _pattern) {

    // return default if parameter is null
    if(null == _pattern) {
      return DefaultStringFormat;
    }

    StringFormat formatter = null;

    synchronized(FormatterFactory.cache) {

      // try to get formatter from cache
      formatter = (StringFormat)FormatterFactory.cache.get(_pattern);

      // create formatter if not in cache
      if(null == formatter) {
        formatter = new CStringFormat(_pattern);
        FormatterFactory.cache.put(_pattern, formatter);
      }

    }

    return formatter;
  }

  /**
   * This method returns a date formatter that will parse
   * and render dates according to the specified pattern.
   *
   * This method is thread safe.
   *
   * @param _pattern the parsing/rendering pattern.
   *
   * @return the date formatter
   *
   * @java.text.SimpleDateFormat
   */
  public static DateFormat getDateFormat (String _pattern) {

    // return default if parameter is null
    if(null == _pattern) {
      return DefaultDateFormat;
    }

    DateFormat formatter = null;
    String javaPattern = null;

    // convert from Monk if necessary
    if(MonkUtils.isMonkDatePattern(_pattern)) {
        try {
            javaPattern = MonkUtils.toJavaDatePattern(_pattern);
        } catch(ParseException ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException("invalid pattern.");
        }
    }

    synchronized(FormatterFactory.cache) {

      // try to get formatter from cache
      formatter = (DateFormat)FormatterFactory.cache.get(_pattern);

      // not found based on user pattern
      if(null == formatter) {

        // we have a java pattern
        if(null != javaPattern) {
          formatter = (DateFormat)FormatterFactory.cache.get(javaPattern);

          // found using java pattern
          if(null != formatter) {

            // add under user pattern
            FormatterFactory.cache.put(_pattern, formatter);

          }
            
        // _pattern is the java pattern
        } else {
          javaPattern = _pattern;
        }

      }

      // create formatter if not in cache
      if(null == formatter) {
        formatter = new SimpleDateFormat(javaPattern);
	boolean strict = JCSProperties.getFlag("DateFormat.Lenient", true);
	formatter.setLenient(strict);
        FormatterFactory.cache.put(_pattern, formatter);
        if(!_pattern.equals(javaPattern)) {
            FormatterFactory.cache.put(javaPattern, formatter);
        }
      }

    }

    return formatter;
  }

  /**
   * main class for testing purposes
   */
  public static void main (String[] _args) {
    if(!FormatterFactory.unitTest) return;

    final String numberPattern = "#.##;-#.##";
    final String booleanPattern = "t|true|1;f|false|0;true;false";

    final String[] defaultArgs = {

      // numbers
      "1.01"
      ,"-1.01"
      ,"10.01"
      ,"-10.01"
      ,"100.01"
      ,"-100.01"
      ,"1000.01"
      ,"-1000.01"
      ,"9E9"
      ,"-9E9"
      ,"9E-9"
      ,"-9E-9"
      ,"9E99"
      ,"-9E99"
      ,"9E-99"
      ,"-9E-99"
      ,"9E999"
      ,"-9E999"
      ,"9E-999"
      ,"-9E-999"

      // booleans
      ,"true"
      ,"t"
      ,"false"
      ,"f"

      // failures
      ,"blah"
    };

    java.text.NumberFormat numberFormatter = FormatterFactory.getNumberFormat(numberPattern);
    BooleanFormat booleanFormatter = FormatterFactory.getBooleanFormat(booleanPattern);

    // do straight pointer comparisons to determine if caching is working
    if(FormatterFactory.getNumberFormat(numberPattern) == numberFormatter) {
      System.out.println("**** NumberFormats are being cached.");
    } else {
      System.out.println("**** Not caching NumberFormat.");
      System.exit(1);
    }

    if(FormatterFactory.getBooleanFormat(booleanPattern) == booleanFormatter) {
      System.out.println("**** BooleanFormats are being cached.");
    } else {
      System.out.println("**** Not caching BooleanFormat.");
      System.exit(1);
    }

    // set defaults if none provided on command line
    if(0 == _args.length) {
      _args = defaultArgs;
    }

    for(int ii=0; ii<_args.length ;ii++) {
      String arg = _args[ii];

      // first try to format as number
      try {
        System.out.println("\nParsing: " + arg);
        Number number = numberFormatter.parse(arg);

        //
        // test each conversion for range exceptions
        //
        try {
          byte num = number.byteValue();
          System.out.println("fits in a byte as " + num);
        } catch(NumberFormatException ex) {
          System.out.println("too big for a byte.");
        }

        try {
          short num = number.shortValue();
          System.out.println("fits in a short as " + num);
        } catch(NumberFormatException ex) {
          System.out.println("too big for a short.");
        }

        try {
          int num = number.intValue();
          System.out.println("fits in a int as " + num);
        } catch(NumberFormatException ex) {
          System.out.println("too big for a int.");
        }

        try {
          long num = number.longValue();
          System.out.println("fits in a long as " + num);
        } catch(NumberFormatException ex) {
          System.out.println("too big for a long.");
        }

        try {
          float num = number.floatValue();
          System.out.println("fits in a float as " + num);
        } catch(NumberFormatException ex) {
          System.out.println("too big for a float.");
        }

        try {
          double num = number.doubleValue();
          System.out.println("fits in a double as " + num);
          System.out.println(arg
                            + " rendered as number "
                            + numberFormatter.format(num)
                            );
        } catch(NumberFormatException ex) {
          System.out.println("too big for a double.");
        }

      // if that fails try as boolean
      } catch(java.text.ParseException ex) {
        try {

          boolean boolVal = booleanFormatter.parse(arg);
          System.out.println(arg
                            + " parsed as boolean "
                            + boolVal
                            );
          System.out.println(arg
                            + " rendered as boolean "
                            + booleanFormatter.format(boolVal)
                            );
        } catch(java.text.ParseException ex2) {
          System.out.println("Unable to parse " + arg);
          ex.printStackTrace();
        }

      } catch(Exception ex) {
        ex.printStackTrace();
      }
    }
    
    // Testing DateFormat
    // set defaults if none provided on command line
    String _date;
    String _to;
    if(2 != _args.length) 
    {
       // This should fail for a Strict Lenient setting, which can be  
       // done by an entry in the .jcsrc file "DateFormat.Lenient=false"
      _date = "20032003"; 
      _to   = "yyyyMMdd"; 
    }
    else
    {
      _date = _args[0];
      _to   = _args[1];
    }
    try 
    {
        Date DateF = FormatterFactory.getDateFormat(_to).parse(_date);
        System.out.println("\nDate data: " + DateF);
    } 
    catch(ParseException ex) 
    {
        System.out.println("Invalid Date.");
    }
  }
}

