/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package com.suijten.bordermaker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>Operations on {@link java.lang.String} that are 
 * <code>null</code> safe.</p>
 * 
 * <ul>
 *  <li><b>IsEmpty/IsBlank</b>
 *      - checks if a String contains text</li>
 *  <li><b>Trim/Strip</b>
 *      - removes leading and trailing whitespace</li>
 *  <li><b>Equals</b>
 *      - compares two strings null-safe</li>
 *  <li><b>IndexOf/LastIndexOf/Contains</b>
 *      - null-safe index-of checks
 *  <li><b>IndexOfAny/LastIndexOfAny/IndexOfAnyBut/LastIndexOfAnyBut</b>
 *      - index-of any of a set of Strings</li>
 *  <li><b>ContainsOnly/ContainsNone</b>
 *      - does String contains only/none of these characters</li>
 *  <li><b>Substring/Left/Right/Mid</b>
 *      - null-safe substring extractions</li>
 *  <li><b>SubstringBefore/SubstringAfter/SubstringBetween</b>
 *      - substring extraction relative to other strings</li>
 *  <li><b>Split/Join</b>
 *      - splits a String into an array of substrings and vice versa</li>
 *  <li><b>Replace/Delete/Overlay</b>
 *      - Searches a String and replaces one String with another</li>
 *  <li><b>Chomp/Chop</b>
 *      - removes the last part of a String</li>
 *  <li><b>LeftPad/RightPad/Center/Repeat</b>
 *      - pads a String</li>
 *  <li><b>UpperCase/LowerCase/SwapCase/Capitalize/Uncapitalize</b>
 *      - changes the case of a String</li>
 *  <li><b>CountMatches</b>
 *      - counts the number of occurrances of one String in another</li>
 *  <li><b>IsAlpha/IsNumeric/IsWhitespace</b>
 *      - checks the characters in a String</li>
 *  <li><b>DefaultString</b>
 *      - protects against a null input String</li>
 *  <li><b>Reverse/ReverseDelimited</b>
 *      - reverses a String</li>
 *  <li><b>Abbreviate</b>
 *      - abbreviates a string using ellipsis</li>
 *  <li><b>Difference</b>
 *      - compares two Strings and reports on their differences</li>
 *  <li><b>LevensteinDistance</b>
 *      - the number of changes needed to change one String into another</li>
 * </ul>
 *
 * <p>The <code>StringUtils</code> class defines certain words related to
 * String handling.</p>
 * 
 * <ul>
 *  <li>null - <code>null</code></li>
 *  <li>empty - a zero-length string (<code>""</code>)</li>
 *  <li>space - the space character (<code>' '</code>, char 32)</li>
 *  <li>whitespace - the characters defined by {@link Character#isWhitespace(char)}</li>
 *  <li>trim - the characters &lt;= 32 as in {@link String#trim()}</li>
 * </ul>
 * 
 * <p><code>StringUtils</code> handles <code>null</code> input Strings quietly.
 * That is to say that a <code>null</code> input will return <code>null</code>.
 * Where a <code>boolean</code> or <code>int</code> is being returned
 * details vary by method.</p>
 * 
 * <p>A side effect of the <code>null</code> handling is that a 
 * <code>NullPointerException</code> should be considered a bug in
 * <code>StringUtils</code> (except for deprecated methods).</p>
 * 
 * <p>Methods in this class give sample code to explain their operation.
 * The symbol <code>*</code> is used to indicate any input including <code>null</code>.</p>
 *
 * @see java.lang.String
 * @author <a href="http://jakarta.apache.org/turbine/">Apache Jakarta Turbine</a>
 * @author GenerationJavaCore
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:gcoladonato@yahoo.com">Greg Coladonato</a>
 * @author <a href="mailto:bayard@generationjava.com">Henri Yandell</a>
 * @author <a href="mailto:ed@apache.org">Ed Korthof</a>
 * @author <a href="mailto:rand_mcneely@yahoo.com">Rand McNeely</a>
 * @author Stephen Colebourne
 * @author <a href="mailto:fredrik@westermarck.com">Fredrik Westermarck</a>
 * @author Holger Krauth
 * @author <a href="mailto:alex@purpletech.com">Alexander Day Chaffee</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @author Arun Mammen Thomas
 * @author Gary Gregory
 * @author Phil Steitz
 * @since 1.0
 * @version $Id: StringUtils.java,v 1.1 2005/05/12 11:43:23 ts2 Exp $
 */
public class StringUtils {
    // Performance testing notes (JDK 1.4, Jul03, scolebourne)
    // Whitespace:
    // Character.isWhitespace() is faster than WHITESPACE.indexOf()
    // where WHITESPACE is a string of all whitespace characters
    // 
    // Character access:
    // String.charAt(n) versus toCharArray(), then array[n]
    // String.charAt(n) is about 15% worse for a 10K string
    // They are about equal for a length 50 string
    // String.charAt(n) is about 4 times better for a length 3 string
    // String.charAt(n) is best bet overall
    //
    // Append:
    // String.concat about twice as fast as StringBuffer.append
    // (not sure who tested this)
    
    /**
     * The empty String <code>""</code>.
     * @since 2.0
     */
    public static final String EMPTY = "";
    
    /**
     * <p>The maximum size to which the padding constant(s) can expand.</p>
     */
    private static final int PAD_LIMIT = 8192;

    /**
     * <p>An array of <code>String</code>s used for padding.</p>
     *
     * <p>Used for efficient space padding. The length of each String expands as needed.</p>
     */
    private static final String[] PADDING = new String[Character.MAX_VALUE];
    
    static {
        // space padding is most common, start with 64 chars
        PADDING[32] = "                                                                ";
    }

    /**
     * <p><code>StringUtils</code> instances should NOT be constructed in
     * standard programming. Instead, the class should be used as
     * <code>StringUtils.trim(" foo ");</code>.</p>
     *
     * <p>This constructor is public to permit tools that require a JavaBean
     * instance to operate.</p>
     */
    public StringUtils() {
    }

    // Empty checks
    //-----------------------------------------------------------------------
    /**
     * <p>Checks if a String is empty ("") or null.</p>
     * 
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     *
     * <p>NOTE: This method changed in Lang version 2.0.
     * It no longer trims the String.
     * That functionality is available in isBlank().</p>
     * 
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isEmpty(String str) {
        return (str == null || str.length() == 0);
    }

    /**
     * <p>Checks if a String is not empty ("") and not null.</p>
     * 
     * <pre>
     * StringUtils.isNotEmpty(null)      = false
     * StringUtils.isNotEmpty("")        = false
     * StringUtils.isNotEmpty(" ")       = true
     * StringUtils.isNotEmpty("bob")     = true
     * StringUtils.isNotEmpty("  bob  ") = true
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is not empty and not null
     */
    public static boolean isNotEmpty(String str) {
        return (str != null && str.length() > 0);
    }

    /**
     * <p>Checks if a String is whitespace, empty ("") or null.</p>
     * 
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is null, empty or whitespace
     * @since 2.0
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false) ) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Checks if a String is not empty (""), not null and not whitespace only.</p>
     * 
     * <pre>
     * StringUtils.isNotBlank(null)      = false
     * StringUtils.isNotBlank("")        = false
     * StringUtils.isNotBlank(" ")       = false
     * StringUtils.isNotBlank("bob")     = true
     * StringUtils.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is 
     *  not empty and not null and not whitespace
     * @since 2.0
     */
    public static boolean isNotBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return false;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false) ) {
                return true;
            }
        }
        return false;
    }

    // Trim
    //-----------------------------------------------------------------------
    /**
     * <p>Removes control characters (char &lt;= 32) from both
     * ends of this String, handling <code>null</code> by returning
     * an empty String ("").</p>
     * 
     * <pre>
     * StringUtils.clean(null)          = ""
     * StringUtils.clean("")            = ""
     * StringUtils.clean("abc")         = "abc"
     * StringUtils.clean("    abc    ") = "abc"
     * StringUtils.clean("     ")       = ""
     * </pre>
     *
     * @see java.lang.String#trim()
     * @param str  the String to clean, may be null
     * @return the trimmed text, never <code>null</code>
     * @deprecated Use the clearer named {@link #trimToEmpty(String)}.
     *             Method will be removed in Commons Lang 3.0.
     */
    public static String clean(String str) {
        return (str == null ? EMPTY : str.trim());
    }

    /**
     * <p>Removes control characters (char &lt;= 32) from both
     * ends of this String, handling <code>null</code> by returning
     * <code>null</code>.</p>
     * 
     * <p>The String is trimmed using {@link String#trim()}.
     * Trim removes start and end characters &lt;= 32.
     * To strip whitespace use {@link #strip(String)}.</p>
     * 
     * <p>To trim your choice of characters, use the
     * {@link #strip(String, String)} methods.</p>
     * 
     * <pre>
     * StringUtils.trim(null)          = null
     * StringUtils.trim("")            = ""
     * StringUtils.trim("     ")       = ""
     * StringUtils.trim("abc")         = "abc"
     * StringUtils.trim("    abc    ") = "abc"
     * </pre>
     *
     * @param str  the String to be trimmed, may be null
     * @return the trimmed string, <code>null</code> if null String input
     */
    public static String trim(String str) {
        return (str == null ? null : str.trim());
    }

    /** 
     * <p>Removes control characters (char &lt;= 32) from both  
     * ends of this String returning <code>null</code> if the String is 
     * empty ("") after the trim or if it is <code>null</code>.
     * 
     * <p>The String is trimmed using {@link String#trim()}.
     * Trim removes start and end characters &lt;= 32.
     * To strip whitespace use {@link #stripToNull(String)}.</p>
     * 
     * <pre>
     * StringUtils.trimToNull(null)          = null
     * StringUtils.trimToNull("")            = null
     * StringUtils.trimToNull("     ")       = null
     * StringUtils.trimToNull("abc")         = "abc"
     * StringUtils.trimToNull("    abc    ") = "abc"
     * </pre>
     *  
     * @param str  the String to be trimmed, may be null
     * @return the trimmed String, 
     *  <code>null</code> if only chars &lt;= 32, empty or null String input
     * @since 2.0
     */
    public static String trimToNull(String str) {
        String ts = trim(str);
        return (ts == null || ts.length() == 0 ? null : ts);
    }

    /** 
     * <p>Removes control characters (char &lt;= 32) from both 
     * ends of this String returning an empty String ("") if the String
     * is empty ("") after the trim or if it is <code>null</code>.
     * 
     * <p>The String is trimmed using {@link String#trim()}.
     * Trim removes start and end characters &lt;= 32.
     * To strip whitespace use {@link #stripToEmpty(String)}.</p>
     * 
     * <pre>
     * StringUtils.trimToEmpty(null)          = ""
     * StringUtils.trimToEmpty("")            = ""
     * StringUtils.trimToEmpty("     ")       = ""
     * StringUtils.trimToEmpty("abc")         = "abc"
     * StringUtils.trimToEmpty("    abc    ") = "abc"
     * </pre>
     *  
     * @param str  the String to be trimmed, may be null
     * @return the trimmed String, or an empty String if <code>null</code> input
     * @since 2.0
     */
    public static String trimToEmpty(String str) {
        return (str == null ? EMPTY : str.trim());
    }
    
    // Stripping
    //-----------------------------------------------------------------------
    /**
     * <p>Strips whitespace from the start and end of a String.</p>
     * 
     * <p>This is similar to {@link #trim(String)} but removes whitespace.
     * Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     * 
     * <p>A <code>null</code> input String returns <code>null</code>.</p>
     * 
     * <pre>
     * StringUtils.strip(null)     = null
     * StringUtils.strip("")       = ""
     * StringUtils.strip("   ")    = ""
     * StringUtils.strip("abc")    = "abc"
     * StringUtils.strip("  abc")  = "abc"
     * StringUtils.strip("abc  ")  = "abc"
     * StringUtils.strip(" abc ")  = "abc"
     * StringUtils.strip(" ab c ") = "ab c"
     * </pre>
     * 
     * @param str  the String to remove whitespace from, may be null
     * @return the stripped String, <code>null</code> if null String input
     */
    public static String strip(String str) {
        return strip(str, null);
    }
    
    /** 
     * <p>Strips whitespace from the start and end of a String  returning
     * <code>null</code> if the String is empty ("") after the strip.</p>
     * 
     * <p>This is similar to {@link #trimToNull(String)} but removes whitespace.
     * Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     * 
     * <pre>
     * StringUtils.strip(null)     = null
     * StringUtils.strip("")       = null
     * StringUtils.strip("   ")    = null
     * StringUtils.strip("abc")    = "abc"
     * StringUtils.strip("  abc")  = "abc"
     * StringUtils.strip("abc  ")  = "abc"
     * StringUtils.strip(" abc ")  = "abc"
     * StringUtils.strip(" ab c ") = "ab c"
     * </pre>
     *  
     * @param str  the String to be stripped, may be null
     * @return the stripped String, 
     *  <code>null</code> if whitespace, empty or null String input
     * @since 2.0
     */
    public static String stripToNull(String str) {
        if (str == null) {
            return null;
        }
        str = strip(str, null);
        return (str.length() == 0 ? null : str);
    }

    /** 
     * <p>Strips whitespace from the start and end of a String  returning
     * an empty String if <code>null</code> input.</p>
     * 
     * <p>This is similar to {@link #trimToEmpty(String)} but removes whitespace.
     * Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     * 
     * <pre>
     * StringUtils.strip(null)     = ""
     * StringUtils.strip("")       = ""
     * StringUtils.strip("   ")    = ""
     * StringUtils.strip("abc")    = "abc"
     * StringUtils.strip("  abc")  = "abc"
     * StringUtils.strip("abc  ")  = "abc"
     * StringUtils.strip(" abc ")  = "abc"
     * StringUtils.strip(" ab c ") = "ab c"
     * </pre>
     *  
     * @param str  the String to be stripped, may be null
     * @return the trimmed String, or an empty String if <code>null</code> input
     * @since 2.0
     */
    public static String stripToEmpty(String str) {
        return (str == null ? EMPTY : strip(str, null));
    }
    
    /**
     * <p>Strips any of a set of characters from the start and end of a String.
     * This is similar to {@link String#trim()} but allows the characters
     * to be stripped to be controlled.</p>
     *
     * <p>A <code>null</code> input String returns <code>null</code>.
     * An empty string ("") input returns the empty string.</p>
     * 
     * <p>If the stripChars String is <code>null</code>, whitespace is
     * stripped as defined by {@link Character#isWhitespace(char)}.
     * Alternatively use {@link #strip(String)}.</p>
     * 
     * <pre>
     * StringUtils.strip(null, *)          = null
     * StringUtils.strip("", *)            = ""
     * StringUtils.strip("abc", null)      = "abc"
     * StringUtils.strip("  abc", null)    = "abc"
     * StringUtils.strip("abc  ", null)    = "abc"
     * StringUtils.strip(" abc ", null)    = "abc"
     * StringUtils.strip("  abcyx", "xyz") = "  abc"
     * </pre>
     * 
     * @param str  the String to remove characters from, may be null
     * @param stripChars  the characters to remove, null treated as whitespace
     * @return the stripped String, <code>null</code> if null String input
     */
    public static String strip(String str, String stripChars) {
        if (str == null || str.length() == 0) {
            return str;
        }
        str = stripStart(str, stripChars);
        return stripEnd(str, stripChars);
    }

    /**
     * <p>Strips any of a set of characters from the start of a String.</p>
     *
     * <p>A <code>null</code> input String returns <code>null</code>.
     * An empty string ("") input returns the empty string.</p>
     * 
     * <p>If the stripChars String is <code>null</code>, whitespace is
     * stripped as defined by {@link Character#isWhitespace(char)}.</p>
     * 
     * <pre>
     * StringUtils.stripStart(null, *)          = null
     * StringUtils.stripStart("", *)            = ""
     * StringUtils.stripStart("abc", "")        = "abc"
     * StringUtils.stripStart("abc", null)      = "abc"
     * StringUtils.stripStart("  abc", null)    = "abc"
     * StringUtils.stripStart("abc  ", null)    = "abc  "
     * StringUtils.stripStart(" abc ", null)    = "abc "
     * StringUtils.stripStart("yxabc  ", "xyz") = "abc  "
     * </pre>
     * 
     * @param str  the String to remove characters from, may be null
     * @param stripChars  the characters to remove, null treated as whitespace
     * @return the stripped String, <code>null</code> if null String input
     */
    public static String stripStart(String str, String stripChars) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        int start = 0;
        if (stripChars == null) {
            while ((start != strLen) && Character.isWhitespace(str.charAt(start))) {
                start++;
            }
        } else if (stripChars.length() == 0) {
            return str;
        } else {
            while ((start != strLen) && (stripChars.indexOf(str.charAt(start)) != -1)) {
                start++;
            }
        }
        return str.substring(start);
    }

    /**
     * <p>Strips any of a set of characters from the end of a String.</p>
     *
     * <p>A <code>null</code> input String returns <code>null</code>.
     * An empty string ("") input returns the empty string.</p>
     * 
     * <p>If the stripChars String is <code>null</code>, whitespace is
     * stripped as defined by {@link Character#isWhitespace(char)}.</p>
     * 
     * <pre>
     * StringUtils.stripEnd(null, *)          = null
     * StringUtils.stripEnd("", *)            = ""
     * StringUtils.stripEnd("abc", "")        = "abc"
     * StringUtils.stripEnd("abc", null)      = "abc"
     * StringUtils.stripEnd("  abc", null)    = "  abc"
     * StringUtils.stripEnd("abc  ", null)    = "abc"
     * StringUtils.stripEnd(" abc ", null)    = " abc"
     * StringUtils.stripEnd("  abcyx", "xyz") = "  abc"
     * </pre>
     * 
     * @param str  the String to remove characters from, may be null
     * @param stripChars  the characters to remove, null treated as whitespace
     * @return the stripped String, <code>null</code> if null String input
     */
    public static String stripEnd(String str, String stripChars) {
        int end;
        if (str == null || (end = str.length()) == 0) {
            return str;
        }
 
        if (stripChars == null) {
            while ((end != 0) && Character.isWhitespace(str.charAt(end - 1))) {
                end--;
            }
        } else if (stripChars.length() == 0) {
            return str;
        } else {
            while ((end != 0) && (stripChars.indexOf(str.charAt(end - 1)) != -1)) {
                end--;
            }
        }
        return str.substring(0, end);
    }

    // StripAll
    //-----------------------------------------------------------------------
    /**
     * <p>Strips whitespace from the start and end of every String in an array.
     * Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <p>A new array is returned each time, except for length zero.
     * A <code>null</code> array will return <code>null</code>.
     * An empty array will return itself.
     * A <code>null</code> array entry will be ignored.</p>
     * 
     * <pre>
     * StringUtils.stripAll(null)             = null
     * StringUtils.stripAll([])               = []
     * StringUtils.stripAll(["abc", "  abc"]) = ["abc", "abc"]
     * StringUtils.stripAll(["abc  ", null])  = ["abc", null]
     * </pre>
     * 
     * @param strs  the array to remove whitespace from, may be null
     * @return the stripped Strings, <code>null</code> if null array input
     */
    public static String[] stripAll(String[] strs) {
        return stripAll(strs, null);
    }
 
    /**
     * <p>Strips any of a set of characters from the start and end of every
     * String in an array.</p>
     * Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <p>A new array is returned each time, except for length zero.
     * A <code>null</code> array will return <code>null</code>.
     * An empty array will return itself.
     * A <code>null</code> array entry will be ignored.
     * A <code>null</code> stripChars will strip whitespace as defined by
     * {@link Character#isWhitespace(char)}.</p>
     * 
     * <pre>
     * StringUtils.stripAll(null, *)                = null
     * StringUtils.stripAll([], *)                  = []
     * StringUtils.stripAll(["abc", "  abc"], null) = ["abc", "abc"]
     * StringUtils.stripAll(["abc  ", null], null)  = ["abc", null]
     * StringUtils.stripAll(["abc  ", null], "yz")  = ["abc  ", null]
     * StringUtils.stripAll(["yabcz", null], "yz")  = ["abc", null]
     * </pre>
     * 
     * @param strs  the array to remove characters from, may be null
     * @param stripChars  the characters to remove, null treated as whitespace
     * @return the stripped Strings, <code>null</code> if null array input
     */
    public static String[] stripAll(String[] strs, String stripChars) {
        int strsLen;
        if (strs == null || (strsLen = strs.length) == 0) {
            return strs;
        }
        String[] newArr = new String[strsLen];
        for (int i = 0; i < strsLen; i++) {
            newArr[i] = strip(strs[i], stripChars);
        }
        return newArr;
    }   

    // Equals
    //-----------------------------------------------------------------------
    /**
     * <p>Compares two Strings, returning <code>true</code> if they are equal.</p>
     *
     * <p><code>null</code>s are handled without exceptions. Two <code>null</code>
     * references are considered to be equal. The comparison is case sensitive.</p>
     *
     * <pre>
     * StringUtils.equals(null, null)   = true
     * StringUtils.equals(null, "abc")  = false
     * StringUtils.equals("abc", null)  = false
     * StringUtils.equals("abc", "abc") = true
     * StringUtils.equals("abc", "ABC") = false
     * </pre>
     *  
     * @see java.lang.String#equals(Object)
     * @param str1  the first String, may be null
     * @param str2  the second String, may be null
     * @return <code>true</code> if the Strings are equal, case sensitive, or
     *  both <code>null</code>
     */
    public static boolean equals(String str1, String str2) {
        return (str1 == null ? str2 == null : str1.equals(str2));
    }

    /**
     * <p>Compares two Strings, returning <code>true</code> if they are equal ignoring
     * the case.</p>
     *
     * <p><code>null</code>s are handled without exceptions. Two <code>null</code>
     * references are considered equal. Comparison is case insensitive.</p>
     *
     * <pre>
     * StringUtils.equalsIgnoreCase(null, null)   = true
     * StringUtils.equalsIgnoreCase(null, "abc")  = false
     * StringUtils.equalsIgnoreCase("abc", null)  = false
     * StringUtils.equalsIgnoreCase("abc", "abc") = true
     * StringUtils.equalsIgnoreCase("abc", "ABC") = true
     * </pre>
     * 
     * @see java.lang.String#equalsIgnoreCase(String)
     * @param str1  the first String, may be null
     * @param str2  the second String, may be null
     * @return <code>true</code> if the Strings are equal, case insensitive, or
     *  both <code>null</code>
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        return (str1 == null ? str2 == null : str1.equalsIgnoreCase(str2));
    }

    // IndexOf
    //-----------------------------------------------------------------------
    /**
     * <p>Finds the first index within a String, handling <code>null</code>.
     * This method uses {@link String#indexOf(int)}.</p>
     *
     * <p>A <code>null</code> or empty ("") String will return <code>-1</code>.</p>
     * 
     * <pre>
     * StringUtils.indexOf(null, *)         = -1
     * StringUtils.indexOf("", *)           = -1
     * StringUtils.indexOf("aabaabaa", 'a') = 0
     * StringUtils.indexOf("aabaabaa", 'b') = 2
     * </pre>
     * 
     * @param str  the String to check, may be null
     * @param searchChar  the character to find
     * @return the first index of the search character, 
     *  -1 if no match or <code>null</code> string input
     * @since 2.0
     */
    public static int indexOf(String str, char searchChar) {
        if (str == null || str.length() == 0) {
            return -1;
        }
        return str.indexOf(searchChar);
    }
    
    /**
     * <p>Finds the first index within a String from a start position,
     * handling <code>null</code>.
     * This method uses {@link String#indexOf(int, int)}.</p>
     *
     * <p>A <code>null</code> or empty ("") String will return <code>-1</code>.
     * A negative start position is treated as zero.
     * A start position greater than the string length returns <code>-1</code>.</p>
     * 
     * <pre>
     * StringUtils.indexOf(null, *, *)          = -1
     * StringUtils.indexOf("", *, *)            = -1
     * StringUtils.indexOf("aabaabaa", 'b', 0)  = 2
     * StringUtils.indexOf("aabaabaa", 'b', 3)  = 5
     * StringUtils.indexOf("aabaabaa", 'b', 9)  = -1
     * StringUtils.indexOf("aabaabaa", 'b', -1) = 2
     * </pre>
     * 
     * @param str  the String to check, may be null
     * @param searchChar  the character to find
     * @param startPos  the start position, negative treated as zero
     * @return the first index of the search character, 
     *  -1 if no match or <code>null</code> string input
     * @since 2.0
     */
    public static int indexOf(String str, char searchChar, int startPos) {
        if (str == null || str.length() == 0) {
            return -1;
        }
        return str.indexOf(searchChar, startPos);
    }
    
    /**
     * <p>Finds the first index within a String, handling <code>null</code>.
     * This method uses {@link String#indexOf(String)}.</p>
     *
     * <p>A <code>null</code> String will return <code>-1</code>.</p>
     * 
     * <pre>
     * StringUtils.indexOf(null, *)          = -1
     * StringUtils.indexOf(*, null)          = -1
     * StringUtils.indexOf("", "")           = 0
     * StringUtils.indexOf("aabaabaa", "a")  = 0
     * StringUtils.indexOf("aabaabaa", "b")  = 2
     * StringUtils.indexOf("aabaabaa", "ab") = 1
     * StringUtils.indexOf("aabaabaa", "")   = 0
     * </pre>
     * 
     * @param str  the String to check, may be null
     * @param searchStr  the String to find, may be null
     * @return the first index of the search String,
     *  -1 if no match or <code>null</code> string input
     * @since 2.0
     */
    public static int indexOf(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return -1;
        }
        return str.indexOf(searchStr);
    }
    
    /**
     * <p>Finds the first index within a String, handling <code>null</code>.
     * This method uses {@link String#indexOf(String, int)}.</p>
     *
     * <p>A <code>null</code> String will return <code>-1</code>.
     * A negative start position is treated as zero.
     * An empty ("") search String always matches.
     * A start position greater than the string length only matches
     * an empty search String.</p>
     * 
     * <pre>
     * StringUtils.indexOf(null, *, *)          = -1
     * StringUtils.indexOf(*, null, *)          = -1
     * StringUtils.indexOf("", "", 0)           = 0
     * StringUtils.indexOf("aabaabaa", "a", 0)  = 0
     * StringUtils.indexOf("aabaabaa", "b", 0)  = 2
     * StringUtils.indexOf("aabaabaa", "ab", 0) = 1
     * StringUtils.indexOf("aabaabaa", "b", 3)  = 5
     * StringUtils.indexOf("aabaabaa", "b", 9)  = -1
     * StringUtils.indexOf("aabaabaa", "b", -1) = 2
     * StringUtils.indexOf("aabaabaa", "", 2)   = 2
     * StringUtils.indexOf("abc", "", 9)        = 3
     * </pre>
     * 
     * @param str  the String to check, may be null
     * @param searchStr  the String to find, may be null
     * @param startPos  the start position, negative treated as zero
     * @return the first index of the search String,
     *  -1 if no match or <code>null</code> string input
     * @since 2.0
     */
    public static int indexOf(String str, String searchStr, int startPos) {
        if (str == null || searchStr == null) {
            return -1;
        }
        // JDK1.2/JDK1.3 have a bug, when startPos > str.length for "", hence
        if (searchStr.length() == 0 && startPos >= str.length()) {
            return str.length();
        }
        return str.indexOf(searchStr, startPos);
    }
    
    // LastIndexOf
    //-----------------------------------------------------------------------
    /**
     * <p>Finds the last index within a String, handling <code>null</code>.
     * This method uses {@link String#lastIndexOf(int)}.</p>
     *
     * <p>A <code>null</code> or empty ("") String will return <code>-1</code>.</p>
     * 
     * <pre>
     * StringUtils.lastIndexOf(null, *)         = -1
     * StringUtils.lastIndexOf("", *)           = -1
     * StringUtils.lastIndexOf("aabaabaa", 'a') = 7
     * StringUtils.lastIndexOf("aabaabaa", 'b') = 5
     * </pre>
     * 
     * @param str  the String to check, may be null
     * @param searchChar  the character to find
     * @return the last index of the search character, 
     *  -1 if no match or <code>null</code> string input
     * @since 2.0
     */
    public static int lastIndexOf(String str, char searchChar) {
        if (str == null || str.length() == 0) {
            return -1;
        }
        return str.lastIndexOf(searchChar);
    }
    
    /**
     * <p>Finds the last index within a String from a start position,
     * handling <code>null</code>.
     * This method uses {@link String#lastIndexOf(int, int)}.</p>
     *
     * <p>A <code>null</code> or empty ("") String will return <code>-1</code>.
     * A negative start position returns <code>-1</code>.
     * A start position greater than the string length searches the whole string.</p>
     * 
     * <pre>
     * StringUtils.lastIndexOf(null, *, *)          = -1
     * StringUtils.lastIndexOf("", *,  *)           = -1
     * StringUtils.lastIndexOf("aabaabaa", 'b', 8)  = 5
     * StringUtils.lastIndexOf("aabaabaa", 'b', 4)  = 2
     * StringUtils.lastIndexOf("aabaabaa", 'b', 0)  = -1
     * StringUtils.lastIndexOf("aabaabaa", 'b', 9)  = 5
     * StringUtils.lastIndexOf("aabaabaa", 'b', -1) = -1
     * StringUtils.lastIndexOf("aabaabaa", 'a', 0)  = 0
     * </pre>
     * 
     * @param str  the String to check, may be null
     * @param searchChar  the character to find
     * @param startPos  the start position
     * @return the last index of the search character, 
     *  -1 if no match or <code>null</code> string input
     * @since 2.0
     */
    public static int lastIndexOf(String str, char searchChar, int startPos) {
        if (str == null || str.length() == 0) {
            return -1;
        }
        return str.lastIndexOf(searchChar, startPos);
    }
    
    /**
     * <p>Finds the last index within a String, handling <code>null</code>.
     * This method uses {@link String#lastIndexOf(String)}.</p>
     *
     * <p>A <code>null</code> String will return <code>-1</code>.</p>
     * 
     * <pre>
     * StringUtils.lastIndexOf(null, *)          = -1
     * StringUtils.lastIndexOf(*, null)          = -1
     * StringUtils.lastIndexOf("", "")           = 0
     * StringUtils.lastIndexOf("aabaabaa", "a")  = 0
     * StringUtils.lastIndexOf("aabaabaa", "b")  = 2
     * StringUtils.lastIndexOf("aabaabaa", "ab") = 1
     * StringUtils.lastIndexOf("aabaabaa", "")   = 8
     * </pre>
     * 
     * @param str  the String to check, may be null
     * @param searchStr  the String to find, may be null
     * @return the last index of the search String,
     *  -1 if no match or <code>null</code> string input
     * @since 2.0
     */
    public static int lastIndexOf(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return -1;
        }
        return str.lastIndexOf(searchStr);
    }
    
    /**
     * <p>Finds the first index within a String, handling <code>null</code>.
     * This method uses {@link String#lastIndexOf(String, int)}.</p>
     *
     * <p>A <code>null</code> String will return <code>-1</code>.
     * A negative start position returns <code>-1</code>.
     * An empty ("") search String always matches unless the start position is negative.
     * A start position greater than the string length searches the whole string.</p>
     * 
     * <pre>
     * StringUtils.lastIndexOf(null, *, *)          = -1
     * StringUtils.lastIndexOf(*, null, *)          = -1
     * StringUtils.lastIndexOf("aabaabaa", "a", 8)  = 7
     * StringUtils.lastIndexOf("aabaabaa", "b", 8)  = 5
     * StringUtils.lastIndexOf("aabaabaa", "ab", 8) = 4
     * StringUtils.lastIndexOf("aabaabaa", "b", 9)  = 5
     * StringUtils.lastIndexOf("aabaabaa", "b", -1) = -1
     * StringUtils.lastIndexOf("aabaabaa", "a", 0)  = 0
     * StringUtils.lastIndexOf("aabaabaa", "b", 0)  = -1
     * </pre>
     * 
     * @param str  the String to check, may be null
     * @param searchStr  the String to find, may be null
     * @param startPos  the start position, negative treated as zero
     * @return the first index of the search String,
     *  -1 if no match or <code>null</code> string input
     * @since 2.0
     */
    public static int lastIndexOf(String str, String searchStr, int startPos) {
        if (str == null || searchStr == null) {
            return -1;
        }
        return str.lastIndexOf(searchStr, startPos);
    }
    
    // Contains
    //-----------------------------------------------------------------------
    /**
     * <p>Checks if String contains a search character, handling <code>null</code>.
     * This method uses {@link String#indexOf(int)}.</p>
     *
     * <p>A <code>null</code> or empty ("") String will return <code>false</code>.</p>
     * 
     * <pre>
     * StringUtils.contains(null, *)    = false
     * StringUtils.contains("", *)      = false
     * StringUtils.contains("abc", 'a') = true
     * StringUtils.contains("abc", 'z') = false
     * </pre>
     * 
     * @param str  the String to check, may be null
     * @param searchChar  the character to find
     * @return true if the String contains the search character, 
     *  false if not or <code>null</code> string input
     * @since 2.0
     */
    public static boolean contains(String str, char searchChar) {
        if (str == null || str.length() == 0) {
            return false;
        }
        return (str.indexOf(searchChar) >= 0);
    }
    
    /**
     * <p>Find the first index within a String, handling <code>null</code>.
     * This method uses {@link String#indexOf(int)}.</p>
     *
     * <p>A <code>null</code> String will return <code>false</code>.</p>
     * 
     * <pre>
     * StringUtils.contains(null, *)     = false
     * StringUtils.contains(*, null)     = false
     * StringUtils.contains("", "")      = true
     * StringUtils.contains("abc", "")   = true
     * StringUtils.contains("abc", "a")  = true
     * StringUtils.contains("abc", "z")  = false
     * </pre>
     * 
     * @param str  the String to check, may be null
     * @param searchStr  the String to find, may be null
     * @return true if the String contains the search character, 
     *  false if not or <code>null</code> string input
     * @since 2.0
     */
    public static boolean contains(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        return (str.indexOf(searchStr) >= 0);
    }
    
    // IndexOfAny chars
    //-----------------------------------------------------------------------
    /**
     * <p>Search a String to find the first index of any
     * character in the given set of characters.</p>
     *
     * <p>A <code>null</code> String will return <code>-1</code>.
     * A <code>null</code> or zero length search array will return <code>-1</code>.</p>
     * 
     * <pre>
     * StringUtils.indexOfAny(null, *)                = -1
     * StringUtils.indexOfAny("", *)                  = -1
     * StringUtils.indexOfAny(*, null)                = -1
     * StringUtils.indexOfAny(*, [])                  = -1
     * StringUtils.indexOfAny("zzabyycdxx",['z','a']) = 0
     * StringUtils.indexOfAny("zzabyycdxx",['b','y']) = 3
     * StringUtils.indexOfAny("aba", ['z'])           = -1
     * </pre>
     * 
     * @param str  the String to check, may be null
     * @param searchChars  the chars to search for, may be null
     * @return the index of any of the chars, -1 if no match or null input
     * @since 2.0
     */
     public static int indexOfAny(String str, char[] searchChars) {
         if (str == null || str.length() == 0 || searchChars == null || searchChars.length == 0) {
             return -1;
         }
         for (int i = 0; i < str.length(); i ++) {
             char ch = str.charAt(i);
             for (int j = 0; j < searchChars.length; j++) {
                 if (searchChars[j] == ch) {
                     return i;
                 }
             }
         }
         return -1;
     }

    /**
     * <p>Search a String to find the first index of any
     * character in the given set of characters.</p>
     *
     * <p>A <code>null</code> String will return <code>-1</code>.
     * A <code>null</code> search string will return <code>-1</code>.</p>
     * 
     * <pre>
     * StringUtils.indexOfAny(null, *)            = -1
     * StringUtils.indexOfAny("", *)              = -1
     * StringUtils.indexOfAny(*, null)            = -1
     * StringUtils.indexOfAny(*, "")              = -1
     * StringUtils.indexOfAny("zzabyycdxx", "za") = 0
     * StringUtils.indexOfAny("zzabyycdxx", "by") = 3
     * StringUtils.indexOfAny("aba","z")          = -1
     * </pre>
     *  
     * @param str  the String to check, may be null
     * @param searchChars  the chars to search for, may be null
     * @return the index of any of the chars, -1 if no match or null input
     * @since 2.0
     */
    public static int indexOfAny(String str, String searchChars) {
        if (str == null || str.length() == 0 || searchChars == null || searchChars.length() == 0) {
            return -1;
        }
        return indexOfAny(str, searchChars.toCharArray());
    }

    // IndexOfAnyBut chars
    //-----------------------------------------------------------------------
    /**
     * <p>Search a String to find the first index of any
     * character not in the given set of characters.</p>
     *
     * <p>A <code>null</code> String will return <code>-1</code>.
     * A <code>null</code> or zero length search array will return <code>-1</code>.</p>
     * 
     * <pre>
     * StringUtils.indexOfAnyBut(null, *)           = -1
     * StringUtils.indexOfAnyBut("", *)             = -1
     * StringUtils.indexOfAnyBut(*, null)           = -1
     * StringUtils.indexOfAnyBut(*, [])             = -1
     * StringUtils.indexOfAnyBut("zzabyycdxx",'za') = 3
     * StringUtils.indexOfAnyBut("zzabyycdxx", '')  = 0
     * StringUtils.indexOfAnyBut("aba", 'ab')       = -1
     * </pre>
     * 
     * @param str  the String to check, may be null
     * @param searchChars  the chars to search for, may be null
     * @return the index of any of the chars, -1 if no match or null input
     * @since 2.0
     */
     public static int indexOfAnyBut(String str, char[] searchChars) {
         if (str == null || str.length() == 0 || searchChars == null || searchChars.length == 0) {
             return -1;
         }
         outer: for (int i = 0; i < str.length(); i ++) {
             char ch = str.charAt(i);
             for (int j = 0; j < searchChars.length; j++) {
                 if (searchChars[j] == ch) {
                     continue outer;
                 }
             }
             return i;
         }
         return -1;
     }

    /**
     * <p>Search a String to find the first index of any
     * character not in the given set of characters.</p>
     *
     * <p>A <code>null</code> String will return <code>-1</code>.
     * A <code>null</code> search string will return <code>-1</code>.</p>
     * 
     * <pre>
     * StringUtils.indexOfAnyBut(null, *)            = -1
     * StringUtils.indexOfAnyBut("", *)              = -1
     * StringUtils.indexOfAnyBut(*, null)            = -1
     * StringUtils.indexOfAnyBut(*, "")              = -1
     * StringUtils.indexOfAnyBut("zzabyycdxx", "za") = 3
     * StringUtils.indexOfAnyBut("zzabyycdxx", "")   = 0
     * StringUtils.indexOfAnyBut("aba","ab")         = -1
     * </pre>
     *  
     * @param str  the String to check, may be null
     * @param searchChars  the chars to search for, may be null
     * @return the index of any of the chars, -1 if no match or null input
     * @since 2.0
     */
    public static int indexOfAnyBut(String str, String searchChars) {
        if (str == null || str.length() == 0 || searchChars == null || searchChars.length() == 0) {
            return -1;
        }
        for (int i = 0; i < str.length(); i++) {
            if (searchChars.indexOf(str.charAt(i)) < 0) {
                return i;
            }
        }
        return -1;
    }

    // ContainsOnly
    //-----------------------------------------------------------------------
    /**
     * <p>Checks if the String contains only certain characters.</p>
     *
     * <p>A <code>null</code> String will return <code>false</code>.
     * A <code>null</code> valid character array will return <code>false</code>.
     * An empty String ("") always returns <code>true</code>.</p>
     *
     * <pre>
     * StringUtils.containsOnly(null, *)       = false
     * StringUtils.containsOnly(*, null)       = false
     * StringUtils.containsOnly("", *)         = true
     * StringUtils.containsOnly("ab", '')      = false
     * StringUtils.containsOnly("abab", 'abc') = true
     * StringUtils.containsOnly("ab1", 'abc')  = false
     * StringUtils.containsOnly("abz", 'abc')  = false
     * </pre>
     * 
     * @param str  the String to check, may be null
     * @param valid  an array of valid chars, may be null
     * @return true if it only contains valid chars and is non-null
     */
    public static boolean containsOnly(String str, char[] valid) {
        // All these pre-checks are to maintain API with an older version
        if ( (valid == null) || (str == null) ) {
            return false;
        }
        if (str.length() == 0) {
            return true;
        }
        if (valid.length == 0) {
            return false;
        }
        return indexOfAnyBut(str, valid) == -1;
    }

    /**
     * <p>Checks if the String contains only certain characters.</p>
     *
     * <p>A <code>null</code> String will return <code>false</code>.
     * A <code>null</code> valid character String will return <code>false</code>.
     * An empty String ("") always returns <code>true</code>.</p>
     *
     * <pre>
     * StringUtils.containsOnly(null, *)       = false
     * StringUtils.containsOnly(*, null)       = false
     * StringUtils.containsOnly("", *)         = true
     * StringUtils.containsOnly("ab", "")      = false
     * StringUtils.containsOnly("abab", "abc") = true
     * StringUtils.containsOnly("ab1", "abc")  = false
     * StringUtils.containsOnly("abz", "abc")  = false
     * </pre>
     * 
     * @param str  the String to check, may be null
     * @param validChars  a String of valid chars, may be null
     * @return true if it only contains valid chars and is non-null
     * @since 2.0
     */
    public static boolean containsOnly(String str, String validChars) {
        if (str == null || validChars == null) {
            return false;
        }
        return containsOnly(str, validChars.toCharArray());
    }
    
    // ContainsNone
    //-----------------------------------------------------------------------
    /**
     * <p>Checks that the String does not contain certain characters.</p>
     *
     * <p>A <code>null</code> String will return <code>true</code>.
     * A <code>null</code> invalid character array will return <code>true</code>.
     * An empty String ("") always returns true.</p>
     *
     * <pre>
     * StringUtils.containsNone(null, *)       = true
     * StringUtils.containsNone(*, null)       = true
     * StringUtils.containsNone("", *)         = true
     * StringUtils.containsNone("ab", '')      = true
     * StringUtils.containsNone("abab", 'xyz') = true
     * StringUtils.containsNone("ab1", 'xyz')  = true
     * StringUtils.containsNone("abz", 'xyz')  = false
     * </pre>
     * 
     * @param str  the String to check, may be null
     * @param invalidChars  an array of invalid chars, may be null
     * @return true if it contains none of the invalid chars, or is null
     * @since 2.0
     */
    public static boolean containsNone(String str, char[] invalidChars) {
        if (str == null || invalidChars == null) {
            return true;
        }
        int strSize = str.length();
        int validSize = invalidChars.length;
        for (int i = 0; i < strSize; i++) {
            char ch = str.charAt(i);
            for (int j = 0; j < validSize; j++) {
                if (invalidChars[j] == ch) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * <p>Checks that the String does not contain certain characters.</p>
     *
     * <p>A <code>null</code> String will return <code>true</code>.
     * A <code>null</code> invalid character array will return <code>true</code>.
     * An empty String ("") always returns true.</p>
     *
     * <pre>
     * StringUtils.containsNone(null, *)       = true
     * StringUtils.containsNone(*, null)       = true
     * StringUtils.containsNone("", *)         = true
     * StringUtils.containsNone("ab", "")      = true
     * StringUtils.containsNone("abab", "xyz") = true
     * StringUtils.containsNone("ab1", "xyz")  = true
     * StringUtils.containsNone("abz", "xyz")  = false
     * </pre>
     * 
     * @param str  the String to check, may be null
     * @param invalidChars  a String of invalid chars, may be null
     * @return true if it contains none of the invalid chars, or is null
     * @since 2.0
     */
    public static boolean containsNone(String str, String invalidChars) {
        if (str == null || invalidChars == null) {
            return true;
        }
        return containsNone(str, invalidChars.toCharArray());
    }
    
    // IndexOfAny strings
    //-----------------------------------------------------------------------
    /**
     * <p>Find the first index of any of a set of potential substrings.</p>
     *
     * <p>A <code>null</code> String will return <code>-1</code>.
     * A <code>null</code> or zero length search array will return <code>-1</code>.
     * A <code>null</code> search array entry will be ignored, but a search
     * array containing "" will return <code>0</code> if <code>str</code> is not
     * null. This method uses {@link String#indexOf(String)}.</p>
     * 
     * <pre>
     * StringUtils.indexOfAny(null, *)                     = -1
     * StringUtils.indexOfAny(*, null)                     = -1
     * StringUtils.indexOfAny(*, [])                       = -1
     * StringUtils.indexOfAny("zzabyycdxx", ["ab","cd"])   = 2
     * StringUtils.indexOfAny("zzabyycdxx", ["cd","ab"])   = 2
     * StringUtils.indexOfAny("zzabyycdxx", ["mn","op"])   = -1
     * StringUtils.indexOfAny("zzabyycdxx", ["zab","aby"]) = 1
     * StringUtils.indexOfAny("zzabyycdxx", [""])          = 0
     * StringUtils.indexOfAny("", [""])                    = 0
     * StringUtils.indexOfAny("", ["a"])                   = -1
     * </pre>
     * 
     * @param str  the String to check, may be null
     * @param searchStrs  the Strings to search for, may be null
     * @return the first index of any of the searchStrs in str, -1 if no match
     */
    public static int indexOfAny(String str, String[] searchStrs) {
        if ((str == null) || (searchStrs == null)) {
            return -1;
        }
        int sz = searchStrs.length;

        // String's can't have a MAX_VALUEth index.
        int ret = Integer.MAX_VALUE;

        int tmp = 0;
        for (int i = 0; i < sz; i++) {
            String search = searchStrs[i];
            if (search == null) {
                continue;
            }
            tmp = str.indexOf(search);
            if (tmp == -1) {
                continue;
            }

            if (tmp < ret) {
                ret = tmp;
            }
        }

        return (ret == Integer.MAX_VALUE) ? -1 : ret;
    }

    /**
     * <p>Find the latest index of any of a set of potential substrings.</p>
     *
     * <p>A <code>null</code> String will return <code>-1</code>.
     * A <code>null</code> search array will return <code>-1</code>.
     * A <code>null</code> or zero length search array entry will be ignored,
     * but a search array containing "" will return the length of <code>str</code> 
     * if <code>str</code> is not null. This method uses {@link String#indexOf(String)}</p>
     * 
     * <pre>
     * StringUtils.lastIndexOfAny(null, *)                   = -1
     * StringUtils.lastIndexOfAny(*, null)                   = -1
     * StringUtils.lastIndexOfAny(*, [])                     = -1
     * StringUtils.lastIndexOfAny(*, [null])                 = -1
     * StringUtils.lastIndexOfAny("zzabyycdxx", ["ab","cd"]) = 6
     * StringUtils.lastIndexOfAny("zzabyycdxx", ["cd","ab"]) = 6
     * StringUtils.lastIndexOfAny("zzabyycdxx", ["mn","op"]) = -1
     * StringUtils.lastIndexOfAny("zzabyycdxx", ["mn","op"]) = -1
     * StringUtils.lastIndexOfAny("zzabyycdxx", ["mn",""])   = 10
     * </pre>
     * 
     * @param str  the String to check, may be null
     * @param searchStrs  the Strings to search for, may be null
     * @return the last index of any of the Strings, -1 if no match
     */
    public static int lastIndexOfAny(String str, String[] searchStrs) {
        if ((str == null) || (searchStrs == null)) {
            return -1;
        }
        int sz = searchStrs.length;
        int ret = -1;
        int tmp = 0;
        for (int i = 0; i < sz; i++) {
            String search = searchStrs[i];
            if (search == null) {
                continue;
            }
            tmp = str.lastIndexOf(search);
            if (tmp > ret) {
                ret = tmp;
            }
        }
        return ret;
    }

    // Substring
    //-----------------------------------------------------------------------
    /**
     * <p>Gets a substring from the specified String avoiding exceptions.</p>
     *
     * <p>A negative start position can be used to start <code>n</code>
     * characters from the end of the String.</p>
     *
     * <p>A <code>null</code> String will return <code>null</code>.
     * An empty ("") String will return "".</p>
     * 
     * <pre>
     * StringUtils.substring(null, *)   = null
     * StringUtils.substring("", *)     = ""
     * StringUtils.substring("abc", 0)  = "abc"
     * StringUtils.substring("abc", 2)  = "c"
     * StringUtils.substring("abc", 4)  = ""
     * StringUtils.substring("abc", -2) = "bc"
     * StringUtils.substring("abc", -4) = "abc"
     * </pre>
     * 
     * @param str  the String to get the substring from, may be null
     * @param start  the position to start from, negative means
     *  count back from the end of the String by this many characters
     * @return substring from start position, <code>null</code> if null String input
     */
    public static String substring(String str, int start) {
        if (str == null) {
            return null;
        }

        // handle negatives, which means last n characters
        if (start < 0) {
            start = str.length() + start; // remember start is negative
        }

        if (start < 0) {
            start = 0;
        }
        if (start > str.length()) {
            return EMPTY;
        }

        return str.substring(start);
    }
    
    /**
     * <p>Gets a substring from the specified String avoiding exceptions.</p>
     *
     * <p>A negative start position can be used to start/end <code>n</code>
     * characters from the end of the String.</p>
     *
     * <p>The returned substring starts with the character in the <code>start</code>
     * position and ends before the <code>end</code> position. All postion counting is 
     * zero-based -- i.e., to start at the beginning of the string use 
     * <code>start = 0</code>. Negative start and end positions can be used to 
     * specify offsets relative to the end of the String.</p>
     *
     * <p>If <code>start</code> is not strictly to the left of <code>end</code>, ""
     * is returned.</p>
     * 
     * <pre>
     * StringUtils.substring(null, *, *)    = null
     * StringUtils.substring("", * ,  *)    = "";
     * StringUtils.substring("abc", 0, 2)   = "ab"
     * StringUtils.substring("abc", 2, 0)   = ""
     * StringUtils.substring("abc", 2, 4)   = "c"
     * StringUtils.substring("abc", 4, 6)   = ""
     * StringUtils.substring("abc", 2, 2)   = ""
     * StringUtils.substring("abc", -2, -1) = "b"
     * StringUtils.substring("abc", -4, 2)  = "ab"
     * </pre>
     * 
     * @param str  the String to get the substring from, may be null
     * @param start  the position to start from, negative means
     *  count back from the end of the String by this many characters
     * @param end  the position to end at (exclusive), negative means
     *  count back from the end of the String by this many characters
     * @return substring from start position to end positon,
     *  <code>null</code> if null String input
     */
    public static String substring(String str, int start, int end) {
        if (str == null) {
            return null;
        }

        // handle negatives
        if (end < 0) {
            end = str.length() + end; // remember end is negative
        }
        if (start < 0) {
            start = str.length() + start; // remember start is negative
        }

        // check length next
        if (end > str.length()) {
            end = str.length();
        }

        // if start is greater than end, return ""
        if (start > end) {
            return EMPTY;
        }

        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }

        return str.substring(start, end);
    }

    // Left/Right/Mid
    //-----------------------------------------------------------------------
    /**
     * <p>Gets the leftmost <code>len</code> characters of a String.</p>
     *
     * <p>If <code>len</code> characters are not available, or the
     * String is <code>null</code>, the String will be returned without
     * an exception. An exception is thrown if len is negative.</p>
     *
     * <pre>
     * StringUtils.left(null, *)    = null
     * StringUtils.left(*, -ve)     = ""
     * StringUtils.left("", *)      = ""
     * StringUtils.left("abc", 0)   = ""
     * StringUtils.left("abc", 2)   = "ab"
     * StringUtils.left("abc", 4)   = "abc"
     * </pre>
     * 
     * @param str  the String to get the leftmost characters from, may be null
     * @param len  the length of the required String, must be zero or positive
     * @return the leftmost characters, <code>null</code> if null String input
     */
    public static String left(String str, int len) {
        if (str == null) {
            return null;
        }
        if (len < 0) {
            return EMPTY;
        }
        if (str.length() <= len) {
            return str;
        } else {
            return str.substring(0, len);
        }
    }

    /**
     * <p>Gets the rightmost <code>len</code> characters of a String.</p>
     *
     * <p>If <code>len</code> characters are not available, or the String
     * is <code>null</code>, the String will be returned without an
     * an exception. An exception is thrown if len is negative.</p>
     *
     * <pre>
     * StringUtils.right(null, *)    = null
     * StringUtils.right(*, -ve)     = ""
     * StringUtils.right("", *)      = ""
     * StringUtils.right("abc", 0)   = ""
     * StringUtils.right("abc", 2)   = "bc"
     * StringUtils.right("abc", 4)   = "abc"
     * </pre>
     * 
     * @param str  the String to get the rightmost characters from, may be null
     * @param len  the length of the required String, must be zero or positive
     * @return the rightmost characters, <code>null</code> if null String input
     */
    public static String right(String str, int len) {
        if (str == null) {
            return null;
        }
        if (len < 0) {
            return EMPTY;
        }
        if (str.length() <= len) {
            return str;
        } else {
            return str.substring(str.length() - len);
        }
    }

    /**
     * <p>Gets <code>len</code> characters from the middle of a String.</p>
     *
     * <p>If <code>len</code> characters are not available, the remainder
     * of the String will be returned without an exception. If the
     * String is <code>null</code>, <code>null</code> will be returned.
     * An exception is thrown if len is negative.</p>
     *
     * <pre>
     * StringUtils.mid(null, *, *)    = null
     * StringUtils.mid(*, *, -ve)     = ""
     * StringUtils.mid("", 0, *)      = ""
     * StringUtils.mid("abc", 0, 2)   = "ab"
     * StringUtils.mid("abc", 0, 4)   = "abc"
     * StringUtils.mid("abc", 2, 4)   = "c"
     * StringUtils.mid("abc", 4, 2)   = ""
     * StringUtils.mid("abc", -2, 2)  = "ab"
     * </pre>
     * 
     * @param str  the String to get the characters from, may be null
     * @param pos  the position to start from, negative treated as zero
     * @param len  the length of the required String, must be zero or positive
     * @return the middle characters, <code>null</code> if null String input
     */
    public static String mid(String str, int pos, int len) {
        if (str == null) {
            return null;
        }
        if (len < 0 || pos > str.length()) {
            return EMPTY;
        }
        if (pos < 0) {
            pos = 0;
        }
        if (str.length() <= (pos + len)) {
            return str.substring(pos);
        } else {
            return str.substring(pos, pos + len);
        }
    }

    // SubStringAfter/SubStringBefore
    //-----------------------------------------------------------------------
    /**
     * <p>Gets the substring before the first occurance of a separator.
     * The separator is not returned.</p>
     *
     * <p>A <code>null</code> string input will return <code>null</code>.
     * An empty ("") string input will return the empty string.
     * A <code>null</code> separator will return the input string.</p>
     *
     * <pre>
     * StringUtils.substringBefore(null, *)      = null
     * StringUtils.substri
