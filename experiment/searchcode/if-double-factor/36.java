/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Rhino code, released
 * May 6, 1999.
 *
 * The Initial Developer of the Original Code is
 * Netscape Communications Corporation.
 * Portions created by the Initial Developer are Copyright (C) 1997-2000
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Patrick Beard
 *   Norris Boyd
 *   Igor Bukanov
 *   Mike Harm
 *   Ethan Hugg
 *   Bob Jervis
 *   Roger Lawrence
 *   Terry Lucas
 *   Frank Mitchell
 *   Milen Nankov
 *   Hannes Wallnoefer
 *   Andrew Wason
 *
 * Alternatively, the contents of this file may be used under the terms of
 * the GNU General Public License Version 2 or later (the "GPL"), in which
 * case the provisions of the GPL are applicable instead of those above. If
 * you wish to allow use of your version of this file only under the terms of
 * the GPL and not to allow others to use your version of this file under the
 * MPL, indicate your decision by deleting the provisions above and replacing
 * them with the notice and other provisions required by the GPL. If you do
 * not delete the provisions above, a recipient may use your version of this
 * file under either the MPL or the GPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.mozilla.javascript;

import java.io.Serializable;
import java.lang.reflect.*;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.mozilla.javascript.xml.XMLObject;
import org.mozilla.javascript.xml.XMLLib;

/**
 * This is the class that implements the runtime.
 *
 * @author Norris Boyd
 */

public class ScriptRuntime {

    /**
     * No instances should be created.
     */
    protected ScriptRuntime() {
    }

    static class NoSuchMethodShim implements Callable {
        String methodName;
        Callable noSuchMethodMethod;

        NoSuchMethodShim(Callable noSuchMethodMethod, String methodName)
        {
            this.noSuchMethodMethod = noSuchMethodMethod;
            this.methodName = methodName;
        }
        /**
         * Perform the call.
         *
         * @param cx the current Context for this thread
         * @param scope the scope to use to resolve properties.
         * @param thisObj the JavaScript <code>this</code> object
         * @param args the array of arguments
         * @return the result of the call
         */
        public Object call(Context cx, Scriptable scope, Scriptable thisObj,
                           Object[] args)
        {
            Object[] nestedArgs = new Object[2];

            nestedArgs[0] = methodName;
            nestedArgs[1] = newArrayLiteral(args, null, cx, scope);
            return noSuchMethodMethod.call(cx, scope, thisObj, nestedArgs);
        }

    }
    /*
     * There's such a huge space (and some time) waste for the Foo.class
     * syntax: the compiler sticks in a test of a static field in the
     * enclosing class for null and the code for creating the class value.
     * It has to do this since the reference has to get pushed off until
     * execution time (i.e. can't force an early load), but for the
     * 'standard' classes - especially those in java.lang, we can trust
     * that they won't cause problems by being loaded early.
     */

    public final static Class<?>
        BooleanClass      = Kit.classOrNull("java.lang.Boolean"),
        ByteClass         = Kit.classOrNull("java.lang.Byte"),
        CharacterClass    = Kit.classOrNull("java.lang.Character"),
        ClassClass        = Kit.classOrNull("java.lang.Class"),
        DoubleClass       = Kit.classOrNull("java.lang.Double"),
        FloatClass        = Kit.classOrNull("java.lang.Float"),
        IntegerClass      = Kit.classOrNull("java.lang.Integer"),
        LongClass         = Kit.classOrNull("java.lang.Long"),
        NumberClass       = Kit.classOrNull("java.lang.Number"),
        ObjectClass       = Kit.classOrNull("java.lang.Object"),
        ShortClass        = Kit.classOrNull("java.lang.Short"),
        StringClass       = Kit.classOrNull("java.lang.String"),
        DateClass         = Kit.classOrNull("java.util.Date");

    public final static Class<?>
        ContextClass
            = Kit.classOrNull("org.mozilla.javascript.Context"),
        ContextFactoryClass
            = Kit.classOrNull("org.mozilla.javascript.ContextFactory"),
        FunctionClass
            = Kit.classOrNull("org.mozilla.javascript.Function"),
        ScriptableObjectClass
            = Kit.classOrNull("org.mozilla.javascript.ScriptableObject");
    public static final Class<Scriptable> ScriptableClass =
        Scriptable.class;


    private static final String[] lazilyNames = {
        "RegExp",        "org.mozilla.javascript.regexp.NativeRegExp",
        "Packages",      "org.mozilla.javascript.NativeJavaTopPackage",
        "java",          "org.mozilla.javascript.NativeJavaTopPackage",
        "javax",         "org.mozilla.javascript.NativeJavaTopPackage",
        "org",           "org.mozilla.javascript.NativeJavaTopPackage",
        "com",           "org.mozilla.javascript.NativeJavaTopPackage",
        "edu",           "org.mozilla.javascript.NativeJavaTopPackage",
        "net",           "org.mozilla.javascript.NativeJavaTopPackage",
        "getClass",      "org.mozilla.javascript.NativeJavaTopPackage",
        "JavaAdapter",   "org.mozilla.javascript.JavaAdapter",
        "JavaImporter",  "org.mozilla.javascript.ImporterTopLevel",
        "Continuation",  "org.mozilla.javascript.NativeContinuation",
        //	TODO	Grotesque hack using literal string (xml) just to minimize
		//			changes for now
        "XML",           "(xml)",
        "XMLList",       "(xml)",
        "Namespace",     "(xml)",
        "QName",         "(xml)",
    };

    private static final Object LIBRARY_SCOPE_KEY = "LIBRARY_SCOPE";

    public static boolean isRhinoRuntimeType(Class<?> cl)
    {
        if (cl.isPrimitive()) {
            return (cl != Character.TYPE);
        } else {
            return (cl == StringClass || cl == BooleanClass
                    || NumberClass.isAssignableFrom(cl)
                    || ScriptableClass.isAssignableFrom(cl));
        }
    }

    public static ScriptableObject initStandardObjects(Context cx,
                                                       ScriptableObject scope,
                                                       boolean sealed)
    {
        if (scope == null) {
            scope = new NativeObject();
        }
        scope.associateValue(LIBRARY_SCOPE_KEY, scope);
        (new ClassCache()).associate(scope);

        BaseFunction.init(scope, sealed);
        NativeObject.init(scope, sealed);

        Scriptable objectProto = ScriptableObject.getObjectPrototype(scope);

        // Function.prototype.__proto__ should be Object.prototype
        Scriptable functionProto = ScriptableObject.getFunctionPrototype(scope);
        functionProto.setPrototype(objectProto);

        // Set the prototype of the object passed in if need be
        if (scope.getPrototype() == null)
            scope.setPrototype(objectProto);

        // must precede NativeGlobal since it's needed therein
        NativeError.init(scope, sealed);
        NativeGlobal.init(cx, scope, sealed);

        NativeArray.init(scope, sealed);
        if (cx.getOptimizationLevel() > 0) {
            // When optimizing, attempt to fulfill all requests for new Array(N)
            // with a higher threshold before switching to a sparse 
            // representation
            NativeArray.setMaximumInitialCapacity(200000);            
        }
        NativeString.init(scope, sealed);
        NativeBoolean.init(scope, sealed);
        NativeNumber.init(scope, sealed);
        NativeDate.init(scope, sealed);
        NativeMath.init(scope, sealed);

        NativeWith.init(scope, sealed);
        NativeCall.init(scope, sealed);
        NativeScript.init(scope, sealed);
        
        NativeIterator.init(scope, sealed); // Also initializes NativeGenerator

        boolean withXml = cx.hasFeature(Context.FEATURE_E4X) && 
                          cx.getE4xImplementationFactory() != null;

        for (int i = 0; i != lazilyNames.length; i += 2) {
            String topProperty = lazilyNames[i];
            String className = lazilyNames[i + 1];
            if (!withXml && className.equals("(xml)")) {
                continue;
            } else if (withXml && className.equals("(xml)")) {
				className = cx.getE4xImplementationFactory().
                               getImplementationClassName();
			}
            new LazilyLoadedCtor(scope, topProperty, className, sealed);
        }

        return scope;
    }

    public static ScriptableObject getLibraryScopeOrNull(Scriptable scope)
    {
        ScriptableObject libScope;
        libScope = (ScriptableObject)ScriptableObject.
                       getTopScopeValue(scope, LIBRARY_SCOPE_KEY);
        return libScope;
    }

    // It is public so NativeRegExp can access it.
    public static boolean isJSLineTerminator(int c)
    {
        // Optimization for faster check for eol character:
        // they do not have 0xDFD0 bits set
        if ((c & 0xDFD0) != 0) {
            return false;
        }
        return c == '\n' || c == '\r' || c == 0x2028 || c == 0x2029;
    }

    public static Boolean wrapBoolean(boolean b)
    {
        return b ? Boolean.TRUE : Boolean.FALSE;
    }

    public static Integer wrapInt(int i)
    {
        return new Integer(i);
    }

    public static Number wrapNumber(double x)
    {
        if (x != x) {
            return ScriptRuntime.NaNobj;
        }
        return new Double(x);
    }

    /**
     * Convert the value to a boolean.
     *
     * See ECMA 9.2.
     */
    public static boolean toBoolean(Object val)
    {
        for (;;) {
            if (val instanceof Boolean)
                return ((Boolean) val).booleanValue();
            if (val == null || val == Undefined.instance)
                return false;
            if (val instanceof String)
                return ((String) val).length() != 0;
            if (val instanceof Number) {
                double d = ((Number) val).doubleValue();
                return (d == d && d != 0.0);
            }
            if (val instanceof Scriptable) {
                if (val instanceof ScriptableObject &&
                    ((ScriptableObject) val).avoidObjectDetection())
                {
                    return false;
                }
                if (Context.getContext().isVersionECMA1()) {
                    // pure ECMA
                    return true;
                }
                // ECMA extension
                val = ((Scriptable) val).getDefaultValue(BooleanClass);
                if (val instanceof Scriptable)
                    throw errorWithClassName("msg.primitive.expected", val);
                continue;
            }
            warnAboutNonJSObject(val);
            return true;
        }
    }

    /**
     * Convert the value to a number.
     *
     * See ECMA 9.3.
     */
    public static double toNumber(Object val)
    {
        for (;;) {
            if (val instanceof Number)
                return ((Number) val).doubleValue();
            if (val == null)
                return +0.0;
            if (val == Undefined.instance)
                return NaN;
            if (val instanceof String)
                return toNumber((String) val);
            if (val instanceof Boolean)
                return ((Boolean) val).booleanValue() ? 1 : +0.0;
            if (val instanceof Scriptable) {
                val = ((Scriptable) val).getDefaultValue(NumberClass);
                if (val instanceof Scriptable)
                    throw errorWithClassName("msg.primitive.expected", val);
                continue;
            }
            warnAboutNonJSObject(val);
            return NaN;
        }
    }

    public static double toNumber(Object[] args, int index) {
        return (index < args.length) ? toNumber(args[index]) : NaN;
    }

    // Can not use Double.NaN defined as 0.0d / 0.0 as under the Microsoft VM,
    // versions 2.01 and 3.0P1, that causes some uses (returns at least) of
    // Double.NaN to be converted to 1.0.
    // So we use ScriptRuntime.NaN instead of Double.NaN.
    public static final double
        NaN = Double.longBitsToDouble(0x7ff8000000000000L);

    // A similar problem exists for negative zero.
    public static final double
        negativeZero = Double.longBitsToDouble(0x8000000000000000L);

    public static final Double NaNobj = new Double(NaN);

    /*
     * Helper function for toNumber, parseInt, and TokenStream.getToken.
     */
    static double stringToNumber(String s, int start, int radix) {
        char digitMax = '9';
        char lowerCaseBound = 'a';
        char upperCaseBound = 'A';
        int len = s.length();
        if (radix < 10) {
            digitMax = (char) ('0' + radix - 1);
        }
        if (radix > 10) {
            lowerCaseBound = (char) ('a' + radix - 10);
            upperCaseBound = (char) ('A' + radix - 10);
        }
        int end;
        double sum = 0.0;
        for (end=start; end < len; end++) {
            char c = s.charAt(end);
            int newDigit;
            if ('0' <= c && c <= digitMax)
                newDigit = c - '0';
            else if ('a' <= c && c < lowerCaseBound)
                newDigit = c - 'a' + 10;
            else if ('A' <= c && c < upperCaseBound)
                newDigit = c - 'A' + 10;
            else
                break;
            sum = sum*radix + newDigit;
        }
        if (start == end) {
            return NaN;
        }
        if (sum >= 9007199254740992.0) {
            if (radix == 10) {
                /* If we're accumulating a decimal number and the number
                 * is >= 2^53, then the result from the repeated multiply-add
                 * above may be inaccurate.  Call Java to get the correct
                 * answer.
                 */
                try {
                    return Double.valueOf(s.substring(start, end)).doubleValue();
                } catch (NumberFormatException nfe) {
                    return NaN;
                }
            } else if (radix == 2 || radix == 4 || radix == 8 ||
                       radix == 16 || radix == 32)
            {
                /* The number may also be inaccurate for one of these bases.
                 * This happens if the addition in value*radix + digit causes
                 * a round-down to an even least significant mantissa bit
                 * when the first dropped bit is a one.  If any of the
                 * following digits in the number (which haven't been added
                 * in yet) are nonzero then the correct action would have
                 * been to round up instead of down.  An example of this
                 * occurs when reading the number 0x1000000000000081, which
                 * rounds to 0x1000000000000000 instead of 0x1000000000000100.
                 */
                int bitShiftInChar = 1;
                int digit = 0;

                final int SKIP_LEADING_ZEROS = 0;
                final int FIRST_EXACT_53_BITS = 1;
                final int AFTER_BIT_53         = 2;
                final int ZEROS_AFTER_54 = 3;
                final int MIXED_AFTER_54 = 4;

                int state = SKIP_LEADING_ZEROS;
                int exactBitsLimit = 53;
                double factor = 0.0;
                boolean bit53 = false;
                // bit54 is the 54th bit (the first dropped from the mantissa)
                boolean bit54 = false;

                for (;;) {
                    if (bitShiftInChar == 1) {
                        if (start == end)
                            break;
                        digit = s.charAt(start++);
                        if ('0' <= digit && digit <= '9')
                            digit -= '0';
                        else if ('a' <= digit && digit <= 'z')
                            digit -= 'a' - 10;
                        else
                            digit -= 'A' - 10;
                        bitShiftInChar = radix;
                    }
                    bitShiftInChar >>= 1;
                    boolean bit = (digit & bitShiftInChar) != 0;

                    switch (state) {
                      case SKIP_LEADING_ZEROS:
                          if (bit) {
                            --exactBitsLimit;
                            sum = 1.0;
                            state = FIRST_EXACT_53_BITS;
                        }
                        break;
                      case FIRST_EXACT_53_BITS:
                           sum *= 2.0;
                        if (bit)
                            sum += 1.0;
                        --exactBitsLimit;
                        if (exactBitsLimit == 0) {
                            bit53 = bit;
                            state = AFTER_BIT_53;
                        }
                        break;
                      case AFTER_BIT_53:
                        bit54 = bit;
                        factor = 2.0;
                        state = ZEROS_AFTER_54;
                        break;
                      case ZEROS_AFTER_54:
                        if (bit) {
                            state = MIXED_AFTER_54;
                        }
                        // fallthrough
                      case MIXED_AFTER_54:
                        factor *= 2;
                        break;
                    }
                }
                switch (state) {
                  case SKIP_LEADING_ZEROS:
                    sum = 0.0;
                    break;
                  case FIRST_EXACT_53_BITS:
                  case AFTER_BIT_53:
                    // do nothing
                    break;
                  case ZEROS_AFTER_54:
                    // x1.1 -> x1 + 1 (round up)
                    // x0.1 -> x0 (round down)
                    if (bit54 & bit53)
                        sum += 1.0;
                    sum *= factor;
                    break;
                  case MIXED_AFTER_54:
                    // x.100...1.. -> x + 1 (round up)
                    // x.0anything -> x (round down)
                    if (bit54)
                        sum += 1.0;
                    sum *= factor;
                    break;
                }
            }
            /* We don't worry about inaccurate numbers for any other base. */
        }
        return sum;
    }


    /**
     * ToNumber applied to the String type
     *
     * See ECMA 9.3.1
     */
    public static double toNumber(String s) {
        int len = s.length();
        int start = 0;
        char startChar;
        for (;;) {
            if (start == len) {
                // Empty or contains only whitespace
                return +0.0;
            }
            startChar = s.charAt(start);
            if (!Character.isWhitespace(startChar))
                break;
            start++;
        }

        if (startChar == '0') {
            if (start + 2 < len) {
                int c1 = s.charAt(start + 1);
                if (c1 == 'x' || c1 == 'X') {
                    // A hexadecimal number
                    return stringToNumber(s, start + 2, 16);
                }
            }
        } else if (startChar == '+' || startChar == '-') {
            if (start + 3 < len && s.charAt(start + 1) == '0') {
                int c2 = s.charAt(start + 2);
                if (c2 == 'x' || c2 == 'X') {
                    // A hexadecimal number with sign
                    double val = stringToNumber(s, start + 3, 16);
                    return startChar == '-' ? -val : val;
                }
            }
        }

        int end = len - 1;
        char endChar;
        while (Character.isWhitespace(endChar = s.charAt(end)))
            end--;
        if (endChar == 'y') {
            // check for "Infinity"
            if (startChar == '+' || startChar == '-')
                start++;
            if (start + 7 == end && s.regionMatches(start, "Infinity", 0, 8))
                return startChar == '-'
                    ? Double.NEGATIVE_INFINITY
                    : Double.POSITIVE_INFINITY;
            return NaN;
        }
        // A non-hexadecimal, non-infinity number:
        // just try a normal floating point conversion
        String sub = s.substring(start, end+1);
        if (MSJVM_BUG_WORKAROUNDS) {
            // The MS JVM will accept non-conformant strings
            // rather than throwing a NumberFormatException
            // as it should.
            for (int i=sub.length()-1; i >= 0; i--) {
                char c = sub.charAt(i);
                if (('0' <= c && c <= '9') || c == '.' ||
                    c == 'e' || c == 'E'  ||
                    c == '+' || c == '-')
                    continue;
                return NaN;
            }
        }
        try {
            return Double.valueOf(sub).doubleValue();
        } catch (NumberFormatException ex) {
            return NaN;
        }
    }

    /**
     * Helper function for builtin objects that use the varargs form.
     * ECMA function formal arguments are undefined if not supplied;
     * this function pads the argument array out to the expected
     * length, if necessary.
     */
    public static Object[] padArguments(Object[] args, int count) {
        if (count < args.length)
            return args;

        int i;
        Object[] result = new Object[count];
        for (i = 0; i < args.length; i++) {
            result[i] = args[i];
        }

        for (; i < count; i++) {
            result[i] = Undefined.instance;
        }

        return result;
    }

    /* Work around Microsoft Java VM bugs. */
    private final static boolean MSJVM_BUG_WORKAROUNDS = true;

    public static String escapeString(String s)
    {
        return escapeString(s, '"');
    }

    /**
     * For escaping strings printed by object and array literals; not quite
     * the same as 'escape.'
     */
    public static String escapeString(String s, char escapeQuote)
    {
        if (!(escapeQuote == '"' || escapeQuote == '\'')) Kit.codeBug();
        StringBuffer sb = null;

        for(int i = 0, L = s.length(); i != L; ++i) {
            int c = s.charAt(i);

            if (' ' <= c && c <= '~' && c != escapeQuote && c != '\\') {
                // an ordinary print character (like C isprint()) and not "
                // or \ .
                if (sb != null) {
                    sb.append((char)c);
                }
                continue;
            }
            if (sb == null) {
                sb = new StringBuffer(L + 3);
                sb.append(s);
                sb.setLength(i);
            }

            int escape = -1;
            switch (c) {
                case '\b':  escape = 'b';  break;
                case '\f':  escape = 'f';  break;
                case '\n':  escape = 'n';  break;
                case '\r':  escape = 'r';  break;
                case '\t':  escape = 't';  break;
                case 0xb:   escape = 'v';  break; // Java lacks \v.
                case ' ':   escape = ' ';  break;
                case '\\':  escape = '\\'; break;
            }
            if (escape >= 0) {
                // an \escaped sort of character
                sb.append('\\');
                sb.append((char)escape);
            } else if (c == escapeQuote) {
                sb.append('\\');
                sb.append(escapeQuote);
            } else {
                int hexSize;
                if (c < 256) {
                    // 2-digit hex
                    sb.append("\\x");
                    hexSize = 2;
                } else {
                    // Unicode.
                    sb.append("\\u");
                    hexSize = 4;
                }
                // append hexadecimal form of c left-padded with 0
                for (int shift = (hexSize - 1) * 4; shift >= 0; shift -= 4) {
                    int digit = 0xf & (c >> shift);
                    int hc = (digit < 10) ? '0' + digit : 'a' - 10 + digit;
                    sb.append((char)hc);
                }
            }
        }
        return (sb == null) ? s : sb.toString();
    }

    static boolean isValidIdentifierName(String s)
    {
        int L = s.length();
        if (L == 0)
            return false;
        if (!Character.isJavaIdentifierStart(s.charAt(0)))
            return false;
        for (int i = 1; i != L; ++i) {
            if (!Character.isJavaIdentifierPart(s.charAt(i)))
                return false;
        }
        return !TokenStream.isKeyword(s);
    }

    /**
     * Convert the value to a string.
     *
     * See ECMA 9.8.
     */
    public static String toString(Object val) {
        for (;;) {
            if (val == null) {
                return "null";
            }
            if (val == Undefined.instance) {
                return "undefined";
            }
            if (val instanceof String) {
                return (String)val;
            }
            if (val instanceof Number) {
                // XXX should we just teach NativeNumber.stringValue()
                // about Numbers?
                return numberToString(((Number)val).doubleValue(), 10);
            }
            if (val instanceof Scriptable) {
                val = ((Scriptable) val).getDefaultValue(StringClass);
                if (val instanceof Scriptable) {
                    throw errorWithClassName("msg.primitive.expected", val);
                }
                continue;
            }
            return val.toString();
        }
    }

    static String defaultObjectToString(Scriptable obj)
    {
        return "[object " + obj.getClassName() + ']';
    }

    public static String toString(Object[] args, int index)
    {
        return (index < args.length) ? toString(args[index]) : "undefined";
    }

    /**
     * Optimized version of toString(Object) for numbers.
     */
    public static String toString(double val) {
        return numberToString(val, 10);
    }

    public static String numberToString(double d, int base) {
        if (d != d)
            return "NaN";
        if (d == Double.POSITIVE_INFINITY)
            return "Infinity";
        if (d == Double.NEGATIVE_INFINITY)
            return "-Infinity";
        if (d == 0.0)
            return "0";

        if ((base < 2) || (base > 36)) {
            throw Context.reportRuntimeError1(
                "msg.bad.radix", Integer.toString(base));
        }

        if (base != 10) {
            return DToA.JS_dtobasestr(base, d);
        } else {
            StringBuffer result = new StringBuffer();
            DToA.JS_dtostr(result, DToA.DTOSTR_STANDARD, 0, d);
            return result.toString();
        }

    }

    static String uneval(Context cx, Scriptable scope, Object value)
    {
        if (value == null) {
            return "null";
        }
        if (value == Undefined.instance) {
            return "undefined";
        }
        if (value instanceof String) {
            String escaped = escapeString((String)value);
            StringBuffer sb = new StringBuffer(escaped.length() + 2);
            sb.append('\"');
            sb.append(escaped);
            sb.append('\"');
            return sb.toString();
        }
        if (value instanceof Number) {
            double d = ((Number)value).doubleValue();
            if (d == 0 && 1 / d < 0) {
                return "-0";
            }
            return toString(d);
        }
        if (value instanceof Boolean) {
            return toString(value);
        }
        if (value instanceof Scriptable) {
            Scriptable obj = (Scriptable)value;
            // Wrapped Java objects won't have "toSource" and will report
            // errors for get()s of nonexistent name, so use has() first
            if (ScriptableObject.hasProperty(obj, "toSource")) {
                Object v = ScriptableObject.getProperty(obj, "toSource");
                if (v instanceof Function) {
                    Function f = (Function)v;
                    return toString(f.call(cx, scope, obj, emptyArgs));
                }
            }
            return toString(value);
        }
        warnAboutNonJSObject(value);
        return value.toString();
    }

    static String defaultObjectToSource(Context cx, Scriptable scope,
                                        Scriptable thisObj, Object[] args)
    {
        boolean toplevel, iterating;
        if (cx.iterating == null) {
            toplevel = true;
            iterating = false;
            cx.iterating = new ObjToIntMap(31);
        } else {
            toplevel = false;
            iterating = cx.iterating.has(thisObj);
        }

        StringBuffer result = new StringBuffer(128);
        if (toplevel) {
            result.append("(");
        }
        result.append('{');

        // Make sure cx.iterating is set to null when done
        // so we don't leak memory
        try {
            if (!iterating) {
                cx.iterating.intern(thisObj); // stop recursion.
                Object[] ids = thisObj.getIds();
                for (int i=0; i < ids.length; i++) {
                    Object id = ids[i];
                    Object value;
                    if (id instanceof Integer) {
                        int intId = ((Integer)id).intValue();
                        value = thisObj.get(intId, thisObj);
                        if (value == Scriptable.NOT_FOUND)
                            continue;   // a property has been removed
                        if (i > 0)
                            result.append(", ");
                        result.append(intId);
                    } else {
                        String strId = (String)id;
                        value = thisObj.get(strId, thisObj);
                        if (value == Scriptable.NOT_FOUND)
                            continue;   // a property has been removed
                        if (i > 0)
                            result.append(", ");
                        if (ScriptRuntime.isValidIdentifierName(strId)) {
                            result.append(strId);
                        } else {
                            result.append('\'');
                            result.append(
                                ScriptRuntime.escapeString(strId, '\''));
                            result.append('\'');
                        }
                    }
                    result.append(':');
                    result.append(ScriptRuntime.uneval(cx, scope, value));
                }
            }
        } finally {
            if (toplevel) {
                cx.iterating = null;
            }
        }

        result.append('}');
        if (toplevel) {
            result.append(')');
        }
        return result.toString();
    }

    public static Scriptable toObject(Scriptable scope, Object val)
    {
        if (val instanceof Scriptable) {
            return (Scriptable)val;
        }
        return toObject(Context.getContext(), scope, val);
    }

    /**
     * Warning: this doesn't allow to resolve primitive prototype properly when many top scopes are involved
     */
    public static Scriptable toObjectOrNull(Context cx, Object obj)
    {
        if (obj instanceof Scriptable) {
            return (Scriptable)obj;
        } else if (obj != null && obj != Undefined.instance) {
            return toObject(cx, getTopCallScope(cx), obj);
        }
        return null;
    }

    /**
     * @param scope the scope that should be used to resolve primitive prototype
     */
    public static Scriptable toObjectOrNull(Context cx, Object obj,
                                            final Scriptable scope)
    {
        if (obj instanceof Scriptable) {
            return (Scriptable)obj;
        } else if (obj != null && obj != Undefined.instance) {
            return toObject(cx, scope, obj);
        }
        return null;
    }

    /**
     * @deprecated Use {@link #toObject(Scriptable, Object)} instead.
     */
    public static Scriptable toObject(Scriptable scope, Object val,
                                      Class<?> staticClass)
    {
        if (val instanceof Scriptable) {
            return (Scriptable)val;
        }
        return toObject(Context.getContext(), scope, val);
    }

    /**
     * Convert the value to an object.
     *
     * See ECMA 9.9.
     */
    public static Scriptable toObject(Context cx, Scriptable scope, Object val)
    {
        if (val instanceof Scriptable) {
            return (Scriptable) val;
        }
        if (val == null) {
            throw typeError0("msg.null.to.object");
        }
        if (val == Undefined.instance) {
            throw typeError0("msg.undef.to.object");
        }
        String className = val instanceof String ? "String" :
                           val instanceof Number ? "Number" :
                           val instanceof Boolean ? "Boolean" :
                           null;
        if (className != null) {
            Object[] args = { val };
            scope = ScriptableObject.getTopLevelScope(scope);
            return newObject(cx, scope, className, args);
        }

        // Extension: Wrap as a LiveConnect object.
        Object wrapped = cx.getWrapFactory().wrap(cx, scope, val, null);
        if (wrapped instanceof Scriptable)
            return (Scriptable) wrapped;
        throw errorWithClassName("msg.invalid.type", val);
    }

    /**
     * @deprecated Use {@link #toObject(Context, Scriptable, Object)} instead.
     */
    public static Scriptable toObject(Context cx, Scriptable scope, Object val,
                                      Class<?> staticClass)
    {
        return toObject(cx, scope, val);
    }

    /**
     * @deprecated The method is only present for compatibility.
     */
    public static Object call(Context cx, Object fun, Object thisArg,
                              Object[] args, Scriptable scope)
    {
        if (!(fun instanceof Function)) {
            throw notFunctionError(toString(fun));
        }
        Function function = (Function)fun;
        Scriptable thisObj = toObjectOrNull(cx, thisArg);
        if (thisObj == null) {
            throw undefCallError(thisObj, "function");
        }
        return function.call(cx, scope, thisObj, args);
    }

    public static Scriptable newObject(Context cx, Scriptable scope,
                                       String constructorName, Object[] args)
    {
        scope = ScriptableObject.getTopLevelScope(scope);
        Function ctor = getExistingCtor(cx, scope, constructorName);
        if (args == null) { args = ScriptRuntime.emptyArgs; }
        return ctor.construct(cx, scope, args);
    }

    /**
     *
     * See ECMA 9.4.
     */
    public static double toInteger(Object val) {
        return toInteger(toNumber(val));
    }

    // convenience method
    public static double toInteger(double d) {
        // if it's NaN
        if (d != d)
            return +0.0;

        if (d == 0.0 ||
            d == Double.POSITIVE_INFINITY ||
            d == Double.NEGATIVE_INFINITY)
            return d;

        if (d > 0.0)
            return Math.floor(d);
        else
            return Math.ceil(d);
    }

    public static double toInteger(Object[] args, int index) {
        return (index < args.length) ? toInteger(args[index]) : +0.0;
    }

    /**
     *
     * See ECMA 9.5.
     */
    public static int toInt32(Object val)
    {
        // short circuit for common integer values
        if (val instanceof Integer)
            return ((Integer)val).intValue();

        return toInt32(toNumber(val));
    }

    public static int toInt32(Object[] args, int index) {
        return (index < args.length) ? toInt32(args[index]) : 0;
    }

    public static int toInt32(double d) {
        int id = (int)d;
        if (id == d) {
            // This covers -0.0 as well
            return id;
        }

        if (d != d
            || d == Double.POSITIVE_INFINITY
            || d == Double.NEGATIVE_INFINITY)
        {
            return 0;
        }

        d = (d >= 0) ? Math.floor(d) : Math.ceil(d);

        double two32 = 4294967296.0;
        d = Math.IEEEremainder(d, two32);
        // (double)(long)d == d should hold here

        long l = (long)d;
        // returning (int)d does not work as d can be outside int range
        // but the result must always be 32 lower bits of l
        return (int)l;
    }

    /**
     * See ECMA 9.6.
     * @return long value representing 32 bits unsigned integer
     */
    public static long toUint32(double d) {
        long l = (long)d;
        if (l == d) {
            // This covers -0.0 as well
            return l & 0xffffffffL;
        }

        if (d != d
            || d == Double.POSITIVE_INFINITY
            || d == Double.NEGATIVE_INFINITY)
        {
            return 0;
        }

        d = (d >= 0) ? Math.floor(d) : Math.ceil(d);

        // 0x100000000 gives me a numeric overflow...
        double two32 = 4294967296.0;
        l = (long)Math.IEEEremainder(d, two32);

        return l & 0xffffffffL;
    }

    public static long toUint32(Object val) {
        return toUint32(toNumber(val));
    }

    /**
     *
     * See ECMA 9.7.
     */
    public static char toUint16(Object val) {
        double d = toNumber(val);

        int i = (int)d;
        if (i == d) {
            return (char)i;
        }

        if (d != d
            || d == Double.POSITIVE_INFINITY
            || d == Double.NEGATIVE_INFINITY)
        {
            return 0;
        }

        d = (d >= 0) ? Math.floor(d) : Math.ceil(d);

        int int16 = 0x10000;
        i = (int)Math.IEEEremainder(d, int16);

        return (char)i;
    }

    // XXX: this is until setDefaultNamespace will learn how to store NS
    // properly and separates namespace form Scriptable.get etc.
    private static final String DEFAULT_NS_TAG = "__default_namespace__";

    public static Object setDefaultNamespace(Object namespace, Context cx)
    {
        Scriptable scope = cx.currentActivationCall;
        if (scope == null) {
            scope = getTopCallScope(cx);
        }

        XMLLib xmlLib = currentXMLLib(cx);
        Object ns = xmlLib.toDefaultXmlNamespace(cx, namespace);

        // XXX : this should be in separated namesapce from Scriptable.get/put
        if (!scope.has(DEFAULT_NS_TAG, scope)) {
            // XXX: this is racy of cause
            ScriptableObject.defineProperty(scope, DEFAULT_NS_TAG, ns,
                                            ScriptableObject.PERMANENT
                                            | ScriptableObject.DONTENUM);
        } else {
            scope.put(DEFAULT_NS_TAG, scope, ns);
        }

        return Undefined.instance;
    }

    public static Object searchDefaultNamespace(Context cx)
    {
        Scriptable scope = cx.currentActivationCall;
        if (scope == null) {
            scope = getTopCallScope(cx);
        }
        Object nsObject;
        for (;;) {
            Scriptable parent = scope.getParentScope();
            if (parent == null) {
                nsObject = ScriptableObject.getProperty(scope, DEFAULT_NS_TAG);
                if (nsObject == Scriptable.NOT_FOUND) {
                    return null;
                }
                break;
            }
            nsObject = scope.get(DEFAULT_NS_TAG, scope);
            if (nsObject != Scriptable.NOT_FOUND) {
                break;
            }
            scope = parent;
        }
        return nsObject;
    }

    public static Object getTopLevelProp(Scriptable scope, String id) {
        scope = ScriptableObject.getTopLevelScope(scope);
        return ScriptableObject.getProperty(scope, id);
    }

    static Function getExistingCtor(Context cx, Scriptable scope,
                                    String constructorName)
    {
        Object ctorVal = ScriptableObject.getProperty(scope, constructorName);
        if (ctorVal instanceof Function) {
            return (Function)ctorVal;
        }
        if (ctorVal == Scriptable.NOT_FOUND) {
            throw Context.reportRuntimeError1(
                "msg.ctor.not.found", constructorName);
        } else {
            throw Context.reportRuntimeError1(
                "msg.not.ctor", constructorName);
        }
    }

    /**
     * Return -1L if str is not an index or the index value as lower 32
     * bits of the result.
     */
    private static long indexFromString(String str)
    {
        // The length of the decimal string representation of
        //  Integer.MAX_VALUE, 2147483647
        final int MAX_VALUE_LENGTH = 10;

        int len = str.length();
        if (len > 0) {
            int i = 0;
            boolean negate = false;
            int c = str.charAt(0);
            if (c == '-') {
                if (len > 1) {
                    c = str.charAt(1);
                    i = 1;
                    negate = true;
                }
            }
            c -= '0';
            if (0 <= c && c <= 9
                && len <= (negate ? MAX_VALUE_LENGTH + 1 : MAX_VALUE_LENGTH))
            {
                // Use negative numbers to accumulate index to handle
                // Integer.MIN_VALUE that is greater by 1 in absolute value
                // then Integer.MAX_VALUE
                int index = -c;
                int oldIndex = 0;
                i++;
                if (index != 0) {
                    // Note that 00, 01, 000 etc. are not indexes
                    while (i != len && 0 <= (c = str.charAt(i) - '0') && c <= 9)
                    {
                        oldIndex = index;
                        index = 10 * index - c;
                        i++;
                    }
                }
                // Make sure all characters were consumed and that it couldn't
                // have overflowed.
                if (i == len &&
                    (oldIndex > (Integer.MIN_VALUE / 10) ||
                     (oldIndex == (Integer.MIN_VALUE / 10) &&
                      c <= (negate ? -(Integer.MIN_VALUE % 10)
                                   : (Integer.MAX_VALUE % 10)))))
                {
                    return 0xFFFFFFFFL & (negate ? index : -index);
                }
            }
        }
        return -1L;
    }

    /**
     * If str is a decimal presentation of Uint32 value, return it as long.
     * Othewise return -1L;
     */
    public static long testUint32String(String str)
    {
        // The length of the decimal string representation of
        //  UINT32_MAX_VALUE, 4294967296
        final int MAX_VALUE_LENGTH = 10;

        int len = str.length();
        if (1 <= len && len <= MAX_VALUE_LENGTH) {
            int c = str.charAt(0);
            c -= '0';
            if (c == 0) {
                // Note that 00,01 etc. are not valid Uint32 presentations
                return (len == 1) ? 0L : -1L;
            }
            if (1 <= c && c <= 9) {
                long v = c;
                for (int i = 1; i != len; ++i) {
                    c = str.charAt(i) - '0';
                    if (!(0 <= c && c <= 9)) {
                        return -1;
                    }
                    v = 10 * v + c;
                }
                // Check for overflow
                if ((v >>> 32) == 0) {
                    return v;
                }
            }
        }
        return -1;
    }

    /**
     * If s represents index, then return index value wrapped as Integer
     * and othewise return s.
     */
    static Object getIndexObject(String s)
    {
        long indexTest = indexFromString(s);
        if (indexTest >= 0) {
            return new Integer((int)indexTest);
        }
        return s;
    }

    /**
     * If d is exact int value, return its value wrapped as Integer
     * and othewise return d converted to String.
     */
    static Object getIndexObject(double d)
    {
        int i = (int)d;
        if (i == d) {
            return new Integer(i);
        }
        return toString(d);
    }

    /**
     * If toString(id) is a decimal presentation of int32 value, then id
     * is index. In this case return null and make the index available
     * as ScriptRuntime.lastIndexResult(cx). Otherwise return toString(id).
     */
    static String toStringIdOrIndex(Context cx, Object id)
    {
        if (id instanceof Number) {
            double d = ((Number)id).doubleValue();
            int index = (int)d;
            if (index == d) {
                storeIndexResult(cx, index);
                return null;
            }
            return toString(id);
        } else {
            String s;
            if (id instanceof String) {
                s = (String)id;
            } else {
                s = toString(id);
            }
            long indexTest = indexFromString(s);
            if (indexTest >= 0) {
                storeIndexResult(cx, (int)indexTest);
                return null;
            }
            return s;
        }
    }

    /**
     * Call obj.[[Get]](id)
     */
    public static Object getObjectElem(Object obj, Object elem, Context cx)
    {
    	return getObjectElem(obj, elem, cx, getTopCallScope(cx));
    }

    /**
     * Call obj.[[Get]](id)
     */
    public static Object getObjectElem(Object obj, Object elem, Context cx, final Scriptable scope)
    {
        Scriptable sobj = toObjectOrNull(cx, obj, scope);
        if (sobj == null) {
            throw undefReadError(obj, elem);
        }
        return getObjectElem(sobj, elem, cx);
    }

    public static Object getObjectElem(Scriptable obj, Object elem,
                                       Context cx)
    {
        if (obj instanceof XMLObject) {
            XMLObject xmlObject = (XMLObject)obj;
            return xmlObject.ecmaGet(cx, elem);
        }

        Object result;

        String s = toStringIdOrIndex(cx, elem);
        if (s == null) {
            int index = lastIndexResult(cx);
            result = ScriptableObject.getProperty(obj, index);
        } else {
            result = ScriptableObject.getProperty(obj, s);
        }

        if (result == Scriptable.NOT_FOUND) {
            result = Undefined.instance;
        }

        return result;
    }

    /**
     * Version of getObjectElem when elem is a valid JS identifier name.
     */
    public static Object getObjectProp(Object obj, String property,
                                       Context cx)
    {
        Scriptable sobj = toObjectOrNull(cx, obj);
        if (sobj == null) {
            throw undefReadError(obj, property);
        }
        return getObjectProp(sobj, property, cx);
    }

    /**
     * @param scope the scope that should be used to resolve primitive prototype
     */
    public static Object getObjectProp(Object obj, String property,
                                       Context cx, final Scriptable scope)
    {
        Scriptable sobj = toObjectOrNull(cx, obj, scope);
        if (sobj == null) {
            throw undefReadError(obj, property);
        }
        return getObjectProp(sobj, property, cx);
    }
    
    public static Object getObjectProp(Scriptable obj, String property,
                                       Context cx)
    {
        if (obj instanceof XMLObject) {
            // TODO: Change XMLObject to just use Scriptable interface
            // to avoid paying cost of instanceof check on *every property
            // lookup* !
            XMLObject xmlObject = (XMLObject)obj;
            return xmlObject.ecmaGet(cx, property);
        }

        Object result = ScriptableObject.getProperty(obj, property);
        if (result == Scriptable.NOT_FOUND) {
            if (cx.hasFeature(Context.FEATURE_STRICT_MODE)) {
                Context.reportWarning(ScriptRuntime.getMessage1(
                    "msg.ref.undefined.prop", property));
            }
            result = Undefined.instance;
        }

        return result;
    }
    
    public static Object getObjectPropNoWarn(Object obj, String property,
                                             Context cx)
    {
        Scriptable sobj = toObjectOrNull(cx, obj);
        if (sobj == null) {
            throw undefReadError(obj, property);
        }
        if (obj instanceof XMLObject) {
            // TODO: fix as mentioned in note in method above
            getObjectProp(sobj, property, cx);
        }
        Object result = ScriptableObject.getProperty(sobj, property);
        if (result == Scriptable.NOT_FOUND) {
          return Undefined.instance;
        }
        return result;
    }
    
    /*
     * A cheaper and less general version of the above for well-known argument
     * types.
     */
    public static Object getObjectIndex(Object obj, double dblIndex,
                                        Context cx)
    {
        Scriptable sobj = toObjectOrNull(cx, obj);
        if (sobj == null) {
            throw undefReadError(obj, toString(dblIndex));
        }

        int index = (int)dblIndex;
        if (index == dblIndex) {
            return getObjectIndex(sobj, index, cx);
        } else {
            String s = toString(dblIndex);
            return getObjectProp(sobj, s, cx);
        }
    }

    public static Object getObjectIndex(Scriptable obj, int index,
                                        Context cx)
    {
        if (obj instanceof XMLObject) {
            XMLObject xmlObject = (XMLObject)obj;
            return xmlObject.ecmaGet(cx, new Integer(index));
        }

        Object result = ScriptableObject.getProperty(obj, index);
        if (result == Scriptable.NOT_FOUND) {
            result = Undefined.instance;
        }

        return result;
    }

    /*
     * Call obj.[[Put]](id, value)
     */
    public static Object setObjectElem(Object obj, Object elem, Object value,
                                       Context cx)
    {
        Scriptable sobj = toObjectOrNull(cx, obj);
        if (sobj == null) {
            throw undefWriteError(obj, elem, value);
        }
        return setObjectElem(sobj, elem, value, cx);
    }

    public static Object setObjectElem(Scriptable obj, Object elem,
                                       Object value, Context cx)
    {
        if (obj instanceof XMLObject) {
            XMLObject xmlObject = (XMLObject)obj;
            xmlObject.ecmaPut(cx, elem, value);
            return value;
        }

        String s = toStringIdOrIndex(cx, elem);
        if (s == null) {
            int index = lastIndexResult(cx);
            ScriptableObject.putProperty(obj, index, value);
        } else {
            ScriptableObject.putProperty(obj, s, value);
        }

        return value;
    }

    /**
     * Version of setObjectElem when elem is a valid JS identifier name.
     */
    public static Object setObjectProp(Object obj, String property,
                                       Object value, Context cx)
    {
        Scriptable sobj = toObjectOrNull(cx, obj);
        if (sobj == null) {
            throw undefWriteError(obj, property, value);
        }
        return setObjectProp(sobj, property, value, cx);
    }

    public static Object setObjectProp(Scriptable obj, String property,
                                       Object value, Context cx)
    {
        if (obj instanceof XMLObject) {
            XMLObject xmlObject = (XMLObject)obj;
            xmlObject.ecmaPut(cx, property, value);
        } else {
            ScriptableObject.putProperty(obj, property, value);
        }
        return value;
    }

    /*
     * A cheaper and less general version of the above for well-known argument
     * types.
     */
    public static Object setObjectIndex(Object obj, double dblIndex,
                                        Object value, Context cx)
    {
        Scriptable sobj = toObjectOrNull(cx, obj);
        if (sobj == null) {
            throw undefWriteError(obj, String.valueOf(dblIndex), value);
        }

        int index = (int)dblIndex;
        if (index == dblIndex) {
            return setObjectIndex(sobj, index, value, cx);
        } else {
            String s = toString(dblIndex);
            return setObjectProp(sobj, s, value, cx);
        }
    }

    public static Object setObjectIndex(Scriptable obj, int index, Object value,
                                        Context cx)
    {
        if (obj instanceof XMLObject) {
            XMLObject xmlObject = (XMLObject)obj;
            xmlObject.ecmaPut(cx, new Integer(index), value);
        } else {
            ScriptableObject.putProperty(obj, index, value);
        }
        return value;
    }

    public static boolean deleteObjectElem(Scriptable target, Object elem,
                                           Context cx)
    {
        boolean result;
        if (target instanceof XMLObject) {
            XMLObject xmlObject = (XMLObject)target;
            result = xmlObject.ecmaDelete(cx, elem);
        } else {
            String s = toStringIdOrIndex(cx, elem);
            if (s == null) {
                int index = lastIndexResult(cx);
                result = ScriptableObject.deleteProperty(target, index);
            } else {
                result = ScriptableObject.deleteProperty(target, s);
            }
        }
        return result;
    }

    public static boolean hasObjectElem(Scriptable target, Object elem,
                                        Context cx)
    {
        boolean result;

        if (target instanceof XMLObject) {
            XMLObject xmlObject = (XMLObject)target;
            result = xmlObject.ecmaHas(cx, elem);
        } else {
            String s = toStringIdOrIndex(cx, elem);
            if (s == null) {
                int index = lastIndexResult(cx);
                result = ScriptableObject.hasProperty(target, index);
            } else {
                result = ScriptableObject.hasProperty(target, s);
            }
        }

        return result;
    }

    public static Object refGet(Ref ref, Context cx)
    {
        return ref.get(cx);
    }

    public static Object refSet(Ref ref, Object value, Context cx)
    {
        return ref.set(cx, value);
    }

    public static Object refDel(Ref ref, Context cx)
    {
        return wrapBoolean(ref.delete(cx));
    }

    static boolean isSpecialProperty(String s)
    {
        return s.equals("__proto__") || s.equals("__parent__");
    }

    public static Ref specialRef(Object obj, String specialProperty,
                                 Context cx)
    {
        return SpecialRef.createSpecial(cx, obj, specialProperty);
    }

    /**
     * The delete operator
     *
     * See ECMA 11.4.1
     *
     * In ECMA 0.19, the description of the delete operator (11.4.1)
     * assumes that the [[Delete]] method returns a value. However,
     * the definition of the [[Delete]] operator (8.6.2.5) does not
     * define a return value. Here we assume that the [[Delete]]
     * method doesn't return a value.
     */
    public static Object delete(Object obj, Object id, Context cx)
    {
        Scriptable sobj = toObjectOrNull(cx, obj);
        if (sobj == null) {
            String idStr = (id == null) ? "null" : id.toString();
            throw typeError2("msg.undef.prop.delete", toString(obj), idStr);
        }
        boolean result = deleteObjectElem(sobj, id, cx);
        return wrapBoolean(result);
    }

    /**
     * Looks up a name in the scope chain and returns its value.
     */
    public static Object name(Context cx, Scriptable scope, String name)
    {
        Scriptable parent = scope.getParentScope();
        if (parent == null) {
            Object result = topScopeName(cx, scope, name);
            if (result == Scriptable.NOT_FOUND) {
                throw notFoundError(scope, name);
            }
            return result;
        }

        return nameOrFunction(cx, scope, parent, name, false);
    }

    private static Object nameOrFunction(Context cx, Scriptable scope,
                                         Scriptable parentScope, String name,
                                         boolean asFunctionCall)
    {
        Object result;
        Scriptable thisObj = scope; // It is used only if asFunctionCall==true.

        XMLObject firstXMLObject = null;
        for (;;) {
            if (scope instanceof NativeWith) {
                Scriptable withObj = scope.getPrototype();
                if (withObj instanceof XMLObject) {
                    XMLObject xmlObj = (XMLObject)withObj;
                    if (xmlObj.ecmaHas(cx, name)) {
                        // function this should be the target object of with
                        thisObj = xmlObj;
                        result = xmlObj.ecmaGet(cx, name);
                        break;
                    }
                    if (firstXMLObject == null) {
                        firstXMLObject = xmlObj;
                    }
                } else {
                    result = ScriptableObject.getProperty(withObj, name);
                    if (result != Scriptable.NOT_FOUND) {
                        // function this should be the target object of with
                        thisObj = withObj;
                        break;
                    }
                }
            } else if (scope instanceof NativeCall) {
                // NativeCall does not prototype chain and Scriptable.get
                // can be called directly.
                result = scope.get(name, scope);
                if (result != Scriptable.NOT_FOUND) {
                    if (asFunctionCall) {
                        // ECMA 262 requires that this for nested funtions
                        // should be top scope
                        thisObj = ScriptableObject.
                                      getTopLevelScope(parentScope);
                    }
                    break;
                }
            } else {
                // Can happen if Rhino embedding decided that nested
                // scopes are useful for what ever reasons.
                result = ScriptableObject.getProperty(scope, name);
                if (result != Scriptable.NOT_FOUND) {
                    thisObj = scope;
                    break;
                }
            }
            scope = parentScope;
            parentScope = parentScope.getParentScope();
            if (parentScope == null) {
                result = topScopeName(cx, scope, name);
                if (result == Scriptable.NOT_FOUND) {
                    if (firstXMLObject == null || asFunctionCall) {
                        throw notFoundError(scope, name);
                    }
                    // The name was not found, but we did find an XML
                    // object in the scope chain and we are looking for name,
                    // not function. The result should be an empty XMLList
                    // in name context.
                    result = firstXMLObject.ecmaGet(cx, name);
                }
                // For top scope thisObj for functions is always scope itself.
                thisObj = scope;
                break;
            }
        }

        if (asFunctionCall) {
            if (!(result instanceof Callable)) {
                throw notFunctionError(result, name);
            }
            storeScriptable(cx, thisObj);
        }

        return result;
    }

    private static Object topScopeName(Context cx, Scriptable scope,
                                       String name)
    {
        if (cx.useDynamicScope) {
            scope = checkDynamicScope(cx.topCallScope, scope);
        }
        return ScriptableObject.getProperty(scope, name);
    }


    /**
     * Returns the object in the scope chain that has a given property.
     *
     * The order of evaluation of an assignment expression involves
     * evaluating the lhs to a reference, evaluating the rhs, and then
     * modifying the reference with the rhs value. This method is used
     * to 'bind' the given name to an object containing that property
     * so that the side effects of evaluating the rhs do not affect
     * which property is modified.
     * Typically used in conjunction with setName.
     *
     * See ECMA 10.1.4
     */
    public static Scriptable bind(Context cx, Scriptable scope, String id)
    {
        Scriptable firstXMLObject = null;
        Scriptable parent = scope.getParentScope();
        childScopesChecks: if (parent != null) {
            // Check for possibly nested "with" scopes first
            while (scope instanceof NativeWith) {
                Scriptable withObj = scope.getPrototype();
                if (withObj instanceof XMLObject) {
                    XMLObject xmlObject = (XMLObject)withObj;
                    if (xmlObject.ecmaHas(cx, id)) {
                        return xmlObject;
                    }
                    if (firstXMLObject == null) {
                        firstXMLObject = xmlObject;
                    }
                } else {
                    if (ScriptableObject.hasProperty(withObj, id)) {
                        return withObj;
                    }
                }
                scope = parent;
                parent = parent.getParentScope();
                if (parent == null) {
                    break childScopesChecks;
                }
            }
            for (;;) {
                if (ScriptableObject.hasProperty(scope, id)) {
                    return scope;
                }
                scope = parent;
                parent = parent.getParentScope();
                if (parent == null) {
                    break childScopesChecks;
                }
            }
        }
        // scope here is top scope
        if (cx.useDynamicScope) {
            scope = checkDynamicScope(cx.topCallScope, scope);
        }
        if (ScriptableObject.hasProperty(scope, id)) {
            return scope;
        }
        // Nothing was found, but since XML objects always bind
        // return one if found
        return firstXMLObject;
    }

    public static Object setName(Scriptable bound, Object value,
                                 Context cx, Scriptable scope, String id)
    {
        if (bound != null) {
            if (bound instanceof XMLObject) {
                XMLObject xmlObject = (XMLObject)bound;
                xmlObject.ecmaPut(cx, id, value);
            } else {
                ScriptableObject.putProperty(bound, id, value);
            }
        } else {
            // "newname = 7;", where 'newname' has not yet
            // been defined, creates a new property in the
            // top scope unless strict mode is specified.
            if (cx.hasFeature(Context.FEATURE_STRICT_MODE) ||
                cx.hasFeature(Context.FEATURE_STRICT_VARS))
            {
                Context.reportWarning(
                    ScriptRuntime.getMessage1("msg.assn.create.strict", id));
            }
            // Find the top scope by walking up the scope chain.
            bound = ScriptableObject.getTopLevelScope(scope);
            if (cx.useDynamicScope) {
                bound = checkDynamicScope(cx.topCallScope, bound);
            }
            bound.put(id, bound, value);
        }
        return value;
    }

    public static Object setConst(Scriptable bound, Object value,
                                 Context cx, String id)
    {
        if (bound instanceof XMLObject) {
            XMLObject xmlObject = (XMLObject)bound;
            xmlObject.ecmaPut(cx, id, value);
        } else {
            ScriptableObject.putConstProperty(bound, id, value);
        }
        return value;
    }

    /**
     * This is the enumeration needed by the for..in statement.
     *
     * See ECMA 12.6.3.
     *
     * IdEnumeration maintains a ObjToIntMap to make sure a given
     * id is enumerated only once across multiple objects in a
     * prototype chain.
     *
     * XXX - ECMA delete doesn't hide properties in the prototype,
     * but js/ref does. This means that the js/ref for..in can
     * avoid maintaining a hash table and instead perform lookups
     * to see if a given property has already been enumerated.
     *
     */
    private static class IdEnumeration implements Serializable
    {
        private static final long serialVersionUID = 1L;
        Scriptable obj;
        Object[] ids;
        int index;
        ObjToIntMap used;
        Object currentId;
        int enumType; /* one of ENUM_INIT_KEYS, ENUM_INIT_VALUES, 
                         ENUM_INIT_ARRAY */
        
        // if true, integer ids will be returned as numbers rather than strings
    
