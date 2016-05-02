/**	$Id: Util.java,v 1.4 2004/01/06 21:35:20 tecris Exp $
 *
 * Various utility functions that don't belong anywhere else.
 *
 */
package wsl.fw.util;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.net.URL;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.Date;
import pv.jfcx.JPVDate;
import wsl.fw.resource.ResId;

public class Util
{
    // resources
    public static final ResId EXCEPTION_NULL  = new ResId("Util.exception.Null");
    public static final ResId EXCEPTION_FATAL  = new ResId("Util.exception.Fatal");
    public static final ResId EXCEPTION_NULL_OBJECT  = new ResId("Util.exception.NullObject");
    public static final ResId EXCEPTION_ASSERT  = new ResId("Util.exception.Assert");
    public static final ResId WARNING_ICON  = new ResId("Util.warning.Icon");

    // constant return codes, where a function returns a string the first 2
    // characters are one of the following return codes. The remainder of the
    // string is text describing the result
    public final static String RC_FAIL    = "00";
    public final static String RC_SUCCESS = "01";

    // the number of characters for return codes. all return codes for use with
    // getReturnCode etc. defined here or elsewhere should have this length.
    public final static int RC_LENGTH = 2;

    //--------------------------------------------------------------------------
    /**
     * Private constructor to stop instantiation.
     */
    private Util()
    {
    }

    //--------------------------------------------------------------------------
    /**
     * Check for null or empty string. If illegal print a stacktrace and throw
     * an IllegalArgumentException.
     * @param s, the String to check.
     * @throws IllegalArgumentException if the string is null or empty.
     */
    public static void argCheckEmpty(String s)
        throws IllegalArgumentException
    {
        if (s == null || s.length() <= 0)
        {
            IllegalArgumentException e = new IllegalArgumentException(
                EXCEPTION_NULL.getText());
            Log.fatal(EXCEPTION_FATAL.getText(), e);
            throw e;
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Check for null or empty string. If illegal print a stacktrace and throw
     * an IllegalArgumentException.
     * @param s, the String to check.
     * @param text, text to be included in the exception on failure.
     * @throws IllegalArgumentException if the string is null or empty.
     */
    public static void argCheckEmpty(String s, String text)
        throws IllegalArgumentException
    {
        if (s == null || s.length() <= 0)
        {
            IllegalArgumentException e = new IllegalArgumentException(
                EXCEPTION_NULL.getText() + " : " + text);
            Log.fatal(EXCEPTION_FATAL.getText(), e);
            throw e;
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Check for null object. If null print a stacktrace and throw an
     * IllegalArgumentException.
     * @param obj, the Object to check.
     * @throws IllegalArgumentException if the Object is null.
     */
    public static void argCheckNull(Object obj)
        throws IllegalArgumentException
    {
        if (obj == null)
        {
            IllegalArgumentException e = new IllegalArgumentException(
                EXCEPTION_NULL_OBJECT.getText());
            Log.fatal(EXCEPTION_FATAL.getText(), e);
            throw e;
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Check for null object. If null print a stacktrace and throw an
     * IllegalArgumentException.
     * @param obj, the Object to check.
     * @param text, text to be included in the exception on failure.
     * @throws IllegalArgumentException if the Object is null.
     */
    public static void argCheckNull(Object obj, String text)
        throws IllegalArgumentException
    {
        if (obj == null)
        {
            IllegalArgumentException e = new IllegalArgumentException(
                EXCEPTION_NULL_OBJECT.getText() + " : " + text);
            Log.fatal(EXCEPTION_FATAL.getText(), e);
            throw e;
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Log a stack trace if assertval is false, then throw an
     * AssertFailedException.
    public static void assert(boolean assertval)
        throws AssertFailedException
    {
        if (!assertval)
        {
            AssertFailedException e = new AssertFailedException();
            Log.fatal(EXCEPTION_ASSERT.getText(), e);
            throw e;
        }
    }
     */

    //--------------------------------------------------------------------------
    /**
     * Log a stack trace if assertval is false, appending text, then throw
     * an AssertFailedException.
    public static void assert(boolean assertval, String text)
        throws AssertFailedException
    {
        if (!assertval)
        {
            AssertFailedException e = new AssertFailedException(text);
            Log.fatal(EXCEPTION_ASSERT.getText(), e);
            throw e;
        }
    }
     */

    //--------------------------------------------------------------------------
    /**
     * Get the parameter value for the named argument from the supplied
     * argument array. Used to help in parsing command line parameters.
     * @param args, the array of arguments (from main()).
     * @paramName, the name of the argument to get.
     * @param default, the default to return if the param is not found.
     * @return the argument that follows paramName in args, empty if there is
     *   no following arg, set to sDefault if paramName is not present.
     */
    public static String getArg(String args[], String paramName, String sDefault)
    {
        // check parameters
        argCheckNull(args);
        argCheckEmpty(paramName);

        // iterate over the args looking for paramName
        for (int i = 0; i < args.length; i++)
            if (paramName.equals(args[i]))      // does it match
                if ((i + 1) < args.length)      // is there a value following
                    return args[i + 1];         // return the value
                else
                    return "";                  // no value, return empty

        // param not found, return the default
        return sDefault;
    }

    //--------------------------------------------------------------------------
    /**
     *
     */
    public static String getArg(String args[], String paramName)
    {
        // delegate with null param
        return getArg(args, paramName, null);
    }

    //--------------------------------------------------------------------------
    /**
     * Convert an array of bytes to a hex String (2 digits per byte).
     * @param bytes, an array of bytes.
     * @return a string containing the hex.
     */
    public static String bytesToHex(byte bytes[])
    {
        // create a string buffer to hold the output
        StringBuffer buff = new StringBuffer(bytes.length * 2);

        // iterate over the bytes converting and appending them
        for (int i = 0; i < bytes.length; i++)
            buff.append(byteToHex(bytes[i]));

        return buff.toString();
    }

    //--------------------------------------------------------------------------
    /**
     * Convert and a byte to a 2 char hex String.
     * @param aByte, the byte to convert.
     * @return a string containing the 2 hex digits.
     */
    public static String byteToHex(byte aByte)
    {
        String hex = Integer.toHexString(((int) aByte) & 0xFF);
        return (hex.length() == 2) ? hex : "0" + hex;
    }

    //--------------------------------------------------------------------------
    /**
     * Create a new icon from a resource. A resource is a file in a dir or JAR
     * which is specified like a filename relative to the classpath.
     * @param resourceName, the name of the resource relative to the classpath.
     * @return the created Icon or null if not found.
     */
    public static Icon resourceIcon(String resourceName)
    {
        Icon icon = null;
        URL  iconURL = ClassLoader.getSystemResource(resourceName);
        if (iconURL != null)
            icon = new ImageIcon(iconURL);
        else
            Log.warning(WARNING_ICON.getText() + " " + resourceName);
        return icon;
    }

    //--------------------------------------------------------------------------
    /**
     * Convert null strings to empty strings.
     * @param str, the string to convert.
     * @return str, or an empty string if str is null
     */
    public static String noNullStr(String str)
    {
        return (str == null) ? "" : str;
    }

    //--------------------------------------------------------------------------
    /**
     * Check if a string is empty or null.
     * @param str, the string to check.
     * @return true if str is null or zero length.
     */
    public static boolean isEmpty(String str)
    {
        return (str == null) || (str.length() <= 0);
    }

    //--------------------------------------------------------------------------
    /**
     * Get the return code (the first 2 chars) from a String.
     * @param str, the string to get the return code from.
     * @return the 2 char return code, if str is null or less than 2 chars
     *   then RC_FAIL is returned.
     */
    public static String getReturnCode(String str)
    {
        if (str == null || str.length() < RC_LENGTH)
            return RC_FAIL;
        else
            return str.substring(0, RC_LENGTH);
    }

    //--------------------------------------------------------------------------
    /**
     * Get the return (descriptive) text with the 2 char return code removed.
     * @param str, the string to get the return text from.
     * @return str, less the 2 initial chars.
     */
    public static String getReturnText(String str)
    {
        if (str == null || str.length() < RC_LENGTH)
            return "";
        else
            return str.substring(RC_LENGTH);
    }

    //--------------------------------------------------------------------------
    /**
     * Check if the string has the specified return code.
     * @param str, the string to check.
     * @param rc, the return code to check against.
     * @return true if str starts with rc.
     */
    public static boolean hasReturnCode(String str, String rc)
    {
        if (str == null || rc == null || str.length() < RC_LENGTH
            || rc.length() != RC_LENGTH)
            return false;
        else
            return str.startsWith(rc);
    }

    //--------------------------------------------------------------------------
    /**
     * Make a return code.
     * @param code, the code prefix.
     * @param text, the descriptive text.
     */
    public static String makeReturnCode(String code, String text)
    {
        assert(code != null && code.length() == RC_LENGTH);

        return code + noNullStr(text);
    }

    //--------------------------------------------------------------------------
    /**
     * Format a number as currency using the current locale settings.
     * @param val, the number to format.
     * @return a string holding the number formatted as curency.
     */
    public static String formatCurrency(double val)
    {
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        return nf.format(val);
    }

    //--------------------------------------------------------------------------
    /**
     * Format a Date as text.
     * @param val, the Date to format.
     * @return a string holding the Date formatted as text.
     */
    public static String formatDate(Date val)
    {
        JPVDate pvd = new JPVDate(val);
        return pvd.getText();
    }

    //--------------------------------------------------------------------------
    /**
     * Format a double as text, using the format string "#0.##".
     * @param val, the double to format.
     * @return a string holding the double formatted as text.
     */
    public static String formatDouble(double val)
    {
        return formatDouble(val, "#0.##");
    }

    //--------------------------------------------------------------------------
    /**
     * Format a double as text using DecimalFormat and the supplied format string.
     * @param val, the double to format.
     * @param fmt, the format string to be used by deciaml format.
     * @return a string holding the double formatted as text.
     */
    public static String formatDouble(double val, String fmt)
    {
        DecimalFormat df = new DecimalFormat(fmt);
        return df.format(val);
    }

	/**
	 *	Find all occurences of "token" in "orig" and replace with
	 *  "value"
	 */
	public static String
	strReplace (
	 String orig,
	 String token,
	 String replacement)
	{
		if (orig.indexOf (token) < 0)
			return orig;

		int tokenlen = token.length ();
		int posn;
		String result = "";

		while ((posn = orig.indexOf (token)) >= 0)
		{
			result += orig.substring (0, posn) + replacement;
			orig = orig.substring (posn + tokenlen);
		}
		return result + orig;
	}
}

