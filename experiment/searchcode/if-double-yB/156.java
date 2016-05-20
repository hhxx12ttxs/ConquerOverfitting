/*
 * Created on Nov 21, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.suijten.bordermaker;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.swing.SwingUtilities;


/**
 * @author Thijs
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SemanticaUtil
{
	private final static String[] units = { "YB", "ZB", "EB", "PB", "TB", "GB", "MB", "KB", "bytes" };
	private final static DecimalFormat formatter = new DecimalFormat("##.#");
	private final static String HEX = "0123456789ABCDEF";

	public static String getLineSeperator()	{
    	return (String) System.getProperty("line.separator");
	}
	
	public static boolean isRightMouse(MouseEvent e) {
		return SwingUtilities.isRightMouseButton(e) || (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0 || (e.getModifiersEx() & MouseEvent.META_DOWN_MASK) != 0;
	}
	
	/**
	 * Gets lines from a String.
	 * 
	 * @param s
	 * @return
	 */
	public static List getLines(String s) {
		List lines = new ArrayList();
		StringReader reader = null;
		LineNumberReader lineReader = null;
		
		if(SemanticaUtil.isEmpty(s)) {
			return null;
		}
		
		try	{
			reader = new StringReader(s);
			lineReader = new LineNumberReader(reader);
			
			String line = null;
			while ((line = lineReader.readLine()) != null) {
				lines.add(line);
			}
		}
		catch (IOException ex)	{
			throw new RuntimeException(ex);
		} finally {
			try{lineReader.close();}catch (Exception e){};
			try{reader.close();}catch (Exception e){};
		}
		return lines;
	}

	public static final Color getBlackWhiteBackgroundColor(Color c) {
        float vals[] = new float[3];
        Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), vals);
        if(vals[2] > 0.75)
            return Color.BLACK;
        else
        	return Color.WHITE;
	}
	
    
	public static final Color invertColor(Color c)
    {
        int r = checkRange(255 - c.getRed());
        int g = checkRange(255 - c.getGreen());
        int b = checkRange(255 - c.getBlue());
        return new Color(r, g, b);
    }

    private static final int checkRange(int i)
    {
        int result = i;
        if(Math.abs(128 - i) < 24)
            result = Math.abs(128 - i);
        return result;
    }

	public static long printNano(double begin) {
		return printNano("%.2f%n", begin);
	}
	
	public static long printNano(String prefix, double begin) {
		double nanos = ((double) System.nanoTime() - begin) / 1000000d;
		System.out.printf(prefix + " %.2f%n", round(nanos, 2));
		return System.nanoTime();
	}
	
	public static double round(double val, int places) {
		double factor = Math.pow(10, places);
		// Shift the decimal the correct number of places to the right.
		val = val * factor;
		// Round to the nearest integer.
		double tmp = Math.round(val);
		// Shift the decimal the correct number of places back to the left.
		return tmp / factor;
	}
	
	public static String postfixLines(String s, String postfix) {
		return prefixPostfixLines(s, "", postfix, false, false);
	}
	public static String postfixLines(String s, String postfix, boolean last) {
		return prefixPostfixLines(s, "", postfix, false, last);
	}
	public static String prefixLines(String s, String prefix) {
		return prefixPostfixLines(s, prefix, "", false, false);
	}
	public static String prefixLines(String s, String prefix, boolean first) {
		return prefixPostfixLines(s, prefix, "", first, false);
	}
	public static String prefixPostfixLines(String s, String prefix, String postfix) {
		return prefixPostfixLines(s, prefix, postfix, false, false);
	}
	public static String prefixPostfixLines(String s, String prefix, String postfix, boolean first, boolean last) {
		if(s == null)
			return null;
		
		StringBuffer buf = new StringBuffer();
		List lines = getLines(s);
		
		for (int i = 0; lines != null && i < lines.size(); i++) {
			if(i != 0 || first) {
				buf.append(prefix);
			}
			buf.append(lines.get(i));
			if(last || i < lines.size() - 1) {
				buf.append(postfix);
			}
		}
		return buf.toString();
	}

    /**
     * Read from inputstream till EOF.
     *
     * @param in the inputstream from which to read.
     *
     * @return the contents read out of the given reader.
     *
     * @throws IOException if the contents could not be read out from the
     *         reader.
     */
    public static final String readFully(InputStream in)
        throws IOException {
    	return readFully(new BufferedReader(new InputStreamReader(in)));
    }
    
    /**
     * Read from reader till EOF.
     *
     * @param rdr the reader from which to read.
     *
     * @return the contents read out of the given reader.
     *
     * @throws IOException if the contents could not be read out from the
     *         reader.
     */
    public static final String readFully(Reader rdr)
        throws IOException {
        final char[] buffer = new char[8192];
        int bufferLength = 0;
        StringBuffer textBuffer = null;
        while (bufferLength != -1) {
            bufferLength = rdr.read(buffer);
            if (bufferLength > 0) {
                textBuffer = (textBuffer == null) ? new StringBuffer() : textBuffer;
                textBuffer.append(new String(buffer, 0, bufferLength));
            }
        }
        return (textBuffer == null) ? null : textBuffer.toString();
    }
    
	public static String getStack(Throwable t) {
		if(t == null) {
			return null;
		}
		String stackTrace = null;
		try	{
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			// print stack trace to printer writer i.e. string writer
			t.printStackTrace(pw);
			// close writers
			pw.close();
			sw.close();
			// get stack trace as string
			stackTrace = sw.toString();
		} catch(Exception e){}
		return stackTrace;
	}

	public static Object getObject(Object value, Object defaultValue) {
		if(value != null) {
			return value;
		} else {
			return defaultValue;
		}
	}
	
	public static String getString(String value, String defaultValue) {
		if(SemanticaUtil.isNotEmpty(value)) {
			return value;
		} else {
			return defaultValue;
		}
	}
	
	/**
	 * Determines if the Object is emtpy.<br>
	 * This method uses the size() method.
	 * @see SemanticaUtil#size(Object)
 	 * @param o The Object to check for emptyness.
 	 * @return If the object is empty. True if the Object is null. 
	 */
 	public static boolean isEmpty(Object o) {
 		return size(o) <= 0;
 	}

	/**
	 * Determines if the Object is not emtpy.<br>
	 * This method uses the size() method.
	 * @see SemanticaUtil#size(Object)
 	 * @param o The Object to check for non-emptyness.
 	 * @return If the object is not empty. False if the Object is null. 
	 */
 	public static boolean isNotEmpty(Object o) {
 		return !isEmpty(o);
 	}
 	
 	/**
 	 * Determines the size of an object. <br>
 	 * The following objects are supported:
 	 * <ul>
 	 * <li>Collections</li>
 	 * <li>Maps</li>
 	 * <li>Arrays</li>
 	 * <li>Strings</li>
 	 * <li>StringBuffers</li>
 	 * <li>Numbers</li>
 	 * </ul>
 	 * If the object is not supported this method recursively
 	 * calls itself with the String representaion of the Object.
 	 * @param o The Object to determine the size of.
 	 * @return The size of the object. 0 if the object is null.
 	 */
 	public static int size(Object o) {
 		if(o == null)
 			return 0;
 		if(o instanceof Collection) {
 			return ((Collection)o).size();
 		}
 		if(o instanceof Map) {
 			return ((Map)o).size();
 		}
 		if(o.getClass().isArray()) {
 			return ((Object[])o).length;
 		}
 		if(o instanceof Number) {
 			return ((Number)o).intValue();
 		}
 		if(o instanceof String) {
 			return ((String)o).trim().length();
 		}
 		if(o instanceof StringBuffer) {
 			return ((StringBuffer)o).toString().trim().length();
 		}
 		return size(o.toString());
 	}

 	/**
 	 * Trims the trailing whitespaces from a String.
 	 * Whitespace is defined by {@link Character#isWhitespace(char)}.
 	 * @param str The String to trim.
 	 * @return The trimmed String.
 	 */
	public static String trimTrailing(String str) {
		if(str != null) {
			int trim = 0;
			for(int i = str.length() - 1; i >= 0; i--) {
				if(Character.isWhitespace(str.charAt(i))) {
					trim++;
				} else {
					return str.substring(0, str.length() - trim);
				}
			}
		}
		return str;
	}
	
	/**
	 * Format bytes.
	 * @see QueryToolUtil#formatBytes(double)
	 * @param s A Number.
	 * @return The formatted byte string. If the parameter object
	 * is not supported or the byte-value could not be formatted
	 * returns "?b".
	 */
	public static String formatBytes(Number s) {
		return formatBytes(s.longValue());
	}

	/**
	 * Format bytes.
	 * @see QueryToolUtil#formatBytes(double)
	 * @param s A Number.
	 * @return The formatted byte string. If the parameter object
	 * is not supported or the byte-value could not be formatted
	 * returns "?b".
	 */
	public static String formatBytes(Object s) {
		if (s instanceof Number) {
			return formatBytes((Number) s);
		}
		return "?b";
	}
	
	/**
	 * Format bytes. <BR>
	 * 1468006,4 = 1,4 MB. <BR>
	 * 21474836480 = 20 GB.
	 * 
	 * @param s
	 *            A double value.
	 * @return The formatted byte string. If the byte-value could not be
	 *         formatted returns "?b".
	 */
	public static String formatBytes(double s) {
		if (s == 0) {
			return "0 bytes";
		}
		for (int i = 0; i < units.length; i++) {
			if (s >= Math.pow(1024, units.length - i - 1)) {
				return formatter.format((s / Math.pow(1024, units.length - i - 1))) + " " + units[i];
			}
		}
		return "?b";
	}

	/**
	 * <B>N </B>umber <B>f </B>rom <B>o </B>bject <BR>
	 * Gets a number representation from a Object. <BR>
	 * When the object is null this method returns 0. When the object cannot be
	 * formattted as a number this method returns 0.
	 * 
	 * @param o
	 *            The Object to get the Number from.
	 * @return The Number representation of the given Object.
	 */
	public static int nfo(Object o) {
		return nfo(o, 0);
	}

	/**
	 * <B>N</B>umber <B>f</B>rom <B>o</B>bject<BR>
	 * Gets a number representation from a Object.<BR>
	 * When the object is null this method returns the defaultValue.
	 * @param o	The Object to get the Number from.
	 * @param defaultValue	The value to return when the object is null or cannot be formatted as a number.
	 * @return	The Number representation of the given Object.
	 */
	public static int nfo(Object o, int defaultValue) {
		return nfo(o, defaultValue, false);
	}

	/**
	 * <B>N</B>umber <B>f</B>rom <B>o</B>bject<BR>
	 * Gets a number representation from a Object.<BR>
	 * When the object is null this method returns 0. 
	 * When the object cannot be
	 * formattted as a number this method returns 0.
	 * @param o	The Object to get the Number from.
	 * @param throwException Specifies if we should throw an exception when parsing the number fails. 
	 * @return	The Number representation of the given Object.
	 */
	public static int nfo(Object o, boolean throwException) {
		return nfo(o, 0, false);
	}

	/**
	 * <B>N</B>umber <B>f</B>rom <B>o</B>bject<BR>
	 * Gets a number representation from a Object.<BR>
	 * When the object is null this method returns the defaultValue. 
	 * When the object cannot be
	 * formattted as a number this method returns the defaultValue.
	 * @param o	The Object to get the Number from.
	 * @param defaultValue	The value to return when the object is null or cannot be formatted as a number.
	 * @param throwException Specifies if we should throw an exception when parsing the number fails. 
	 * @return	The Number representation of the given Object.
	 */
	public static int nfo(Object o, int defaultValue, boolean throwException) {
		int retValue = defaultValue;
		if(o != null) {
			if(o instanceof Number) {
				retValue = ((Number) o).intValue();
			} else {
				try {
					retValue = Integer.parseInt(String.valueOf(o).trim());
				} catch(NumberFormatException e) {
					if(throwException) {
						throw e;
					}
				}
			}
		}
		return retValue;
	}
	
	/**
	 * <B>L</B>ong <B>f </B>rom <B>o </B>bject <BR>
	 * Gets a long representation from a Object. <BR>
	 * When the object is null this method returns 0. When the object cannot be
	 * formattted as a long this method returns 0.
	 * 
	 * @param o
	 *            The Object to get the long from.
	 * @return The long representation of the given Object.
	 */
	public static long lfo(Object o) {
		return lfo(o, 0);
	}
	
	/**
	 * <B>L</B>ong <B>f</B>rom <B>o</B>bject<BR>
	 * Gets a long representation from a Object.<BR>
	 * When the object is null this method returns the defaultValue.
	 * @param o	The Object to get the long from.
	 * @param defaultValue	The value to return when the object is null or cannot be formatted as a long.
	 * @return	The long representation of the given Object.
	 */
	public static long lfo(Object o, long defaultValue) {
		return lfo(o, defaultValue, false);
	}
	
	/**
	 * <B>L</B>ong <B>f</B>rom <B>o</B>bject<BR>
	 * Gets a long representation from a Object.<BR>
	 * When the object is null this method returns 0. 
	 * When the object cannot be
	 * formattted as a number this method returns 0.
	 * @param o	The Object to get the long from.
	 * @param throwException Specifies if we should throw an exception when parsing the long fails. 
	 * @return	The long representation of the given Object.
	 */
	public static long lfo(Object o, boolean throwException) {
		return lfo(o, 0, false);
	}
	
	/**
	 * <B>L</B>ong <B>f</B>rom <B>o</B>bject<BR>
	 * Gets a long representation from a Object.<BR>
	 * When the object is null this method returns the defaultValue. 
	 * When the object cannot be
	 * formattted as a long this method returns the defaultValue.
	 * @param o	The Object to get the long from.
	 * @param defaultValue	The value to return when the object is null or cannot be formatted as a long.
	 * @param throwException Specifies if we should throw an exception when parsing the long fails. 
	 * @return	The long representation of the given Object.
	 */
	public static long lfo(Object o, long defaultValue, boolean throwException) {
		long retValue = defaultValue;
		if(o != null) {
			if(o instanceof Number) {
				retValue = ((Number) o).longValue();
			} else {
				try {
					retValue = Long.parseLong(String.valueOf(o).trim());
				} catch(NumberFormatException e) {
					if(throwException) {
						throw e;
					}
				}
			}
		}
		return retValue;
	}
	
	/**
	 * <B>B</B>oolean <B>f</B>rom <B>o</B>bject<BR>
	 * Gets a boolean representation from a Object.<BR>
	 * When the object is null this method returns false.
	 * When the object is not null the returned represents the
     * value <code>true</code> if the object is equal, 
     * ignoring case, to the string <code>"true"</code>. 
	 * @param o	The Object to get the boolean from.
	 * @param defaultValue	The value to return when the object is null.
	 * @return	The boolean representation of the given Object.
	 */
	public static boolean bfo(Object o) {
		return bfo(o, false);
	}
	
	/**
	 * <B>B</B>oolean <B>f</B>rom <B>o</B>bject<BR>
	 * Gets a boolean representation from a Object.<BR>
	 * When the object is null this method returns the defaultValue.
	 * When the object is not null the returned represents the
     * value <code>true</code> if the object is equal, 
     * ignoring case, to the string <code>"true"</code>. 
	 * @param o	The Object to get the boolean from.
	 * @param defaultValue	The value to return when the object is null.
	 * @return	The boolean representation of the given Object.
	 */
	public static boolean bfo(Object o, boolean defaultValue) {
		boolean retValue = defaultValue;
		if(o != null) {
			if(o instanceof Boolean) {
				retValue = ((Boolean) o).booleanValue();
			} else {
				retValue = Boolean.valueOf(String.valueOf(o).trim()).booleanValue();
			}
		}
		return retValue;
	}
	
	/**
	 * <B>S</B>tring <B>f</B>rom <B>o</B>bject<BR>
	 * Gets a string representation from a Object.<BR>
	 * When the object is null this methode returns "".
	 * @param o	The Object to get the String from.
	 * @return	The String representation of the given Object.
	 */
	public static String sfo(Object o) {
		return o == null?"":o.toString();
	}

    /**
     * <p>Replaces all occurances of a String within another String ignoring case.</p>
     *
     * <p>A <code>null</code> reference passed to this method is a no-op.</p>
     * 
     * <pre>
     * StringUtils.replace(null, *, *)        = null
     * StringUtils.replace("", *, *)          = ""
     * StringUtils.replace("aba", null, null) = "aba"
     * StringUtils.replace("aba", null, null) = "aba"
     * StringUtils.replace("aba", "a", null)  = "aba"
     * StringUtils.replace("aba", "a", "")    = "aba"
     * StringUtils.replace("aba", "a", "z")   = "zbz"
     * </pre>
     * 
     * @see #replace(String text, String repl, String with, int max)
     * @param text  text to search and replace in, may be null
     * @param repl  the String to search for, may be null
     * @param with  the String to replace with, may be null
     * @return the text with any replacements processed,
     *  <code>null</code> if null String input
     */
    public static String replaceIgnoreCase(String text, String repl, String with) {
    	return replaceIgnoreCase(text, repl, with, -1);
    }

    /**
     * <p>Replaces a String with another String inside a larger String,
     * for the first <code>max</code> values of the search String ignoring case.</p>
     *
     * <p>A <code>null</code> reference passed to this method is a no-op.</p>
     *
     * <pre>
     * StringUtils.replace(null, *, *, *)         = null
     * StringUtils.replace("", *, *, *)           = ""
     * StringUtils.replace("abaa", null, null, 1) = "abaa"
     * StringUtils.replace("abaa", null, null, 1) = "abaa"
     * StringUtils.replace("abaa", "a", null, 1)  = "abaa"
     * StringUtils.replace("abaa", "a", "", 1)    = "abaa"
     * StringUtils.replace("abaa", "a", "z", 0)   = "abaa"
     * StringUtils.replace("abaa", "a", "z", 1)   = "zbaa"
     * StringUtils.replace("abaa", "a", "z", 2)   = "zbza"
     * StringUtils.replace("abaa", "a", "z", -1)  = "zbzz"
     * </pre>
     * 
     * @param text  text to search and replace in, may be null
     * @param repl  the String to search for, may be null
     * @param with  the String to replace with, may be null
     * @param max  maximum number of values to replace, or <code>-1</code> if no maximum
     * @return the text with any replacements processed,
     *  <code>null</code> if null String input
     */
    public static String replaceIgnoreCase(String text, String repl, String with, int max) {
        if (text == null || repl == null || with == null || repl.length() == 0 || max == 0) {
            return text;
        }

		String lowerRepl = repl.toLowerCase();
		String lowerText = text.toLowerCase();
        StringBuffer buf = new StringBuffer(text.length());
        int start = 0, end = 0;
        while ((end = lowerText.indexOf(lowerRepl, start)) != -1) {
            buf.append(text.substring(start, end)).append(with);
            start = end + repl.length();
            if (--max == 0) {
                break;
            }
        }
        buf.append(text.substring(start));
        return buf.toString();
    }
    
	/**
	 * Compares the two objects as Strings ignoring case.
	 * @param a Object a
	 * @param b Object b
	 * @return If Object a and b's String representation are equal.
	 */
	public static boolean stringEqualsIgnoreCase(Object a, Object b) {
		return equals(a == null ? null : a.toString().toLowerCase(), b == null ? null : b.toString().toLowerCase());
	}
	
	/**
	 * Compares the two objects as Strings.
	 * @param a Object a
	 * @param b Object b
	 * @return If Object a and b's String representation are equal.
	 */
	public static boolean stringEquals(Object a, Object b) {
		return equals(a == null ? null : a.toString(), b == null ? null : b.toString());
	}
	
	/**
	 * Compares two objects 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean equals(Object a, Object b) {
		if(a == null && b == null) {
			return true;
		}
		
		if((a == null && b != null) || (a != null && b == null)) {
			return false;
		}
		
		if(a == b) {
			return true;
		}
		
		return a.equals(b);
	}
	
	public static int compare(Object lhs, Object rhs) {
		return compare(lhs, rhs, null);
	}
	
	public static int compare(Object lhs, Object rhs, Comparator comparator) {
		if(lhs == rhs) {
			return 0;
		}
		
		if(lhs == null) {
			return -1;
		}
		
		if(rhs == null) {
			return +1;
		}

        if (comparator == null) {
            return ((Comparable) lhs).compareTo(rhs);
        } else {
        	return comparator.compare(lhs, rhs);
        }
	}

	public static int createTwoDimensionalList(List list, int width) {
		return createTwoDimensionalList(list, width, false, null);
	}
	
	public static int createTwoDimensionalList(List list, int width, Object fill) {
		return createTwoDimensionalList(list, width, true, fill);
	}
	
	private static int createTwoDimensionalList(List list, int width, boolean fillRemainder, Object fillObject) {
		// Make a copy of the original list. 
		List oneDimensional = new ArrayList(list);
		
		//Clear the target list.
		list.clear();
		
		List lastRow = null;
		
		for (int i = 0; i < oneDimensional.size(); i++) {
			if(i % width == 0) {
				lastRow = new Vector();
				list.add(lastRow);
			} else {
				lastRow = (List) list.get(list.size() - 1);
			}
			lastRow.add(oneDimensional.get(i));
		}
		
		int remainder = width - lastRow.size();
		
		if(fillRemainder) {
			while(lastRow.size() < width) {
				lastRow.add(fillObject);
			}
		}
		
		return remainder;
	}

	public static void moveBelow(List list, int[] indices, int to) {
		Vector itemsToMove = new Vector();
		
		//Store the items we have to move
		for (int i = 0; i < indices.length; i++) {
			itemsToMove.add(list.get(indices[i]));
		}

		Arrays.sort(indices);
		
		//Remove the items we have to move
		for (int i = indices.length - 1; i >= 0 ; i--) {
			list.remove(indices[i]);

			if(indices[i] < to) {
				to--;
			}
		}
		
		//Add one to the "to" index to achieve move below
		to++;
		
		//Fail safe the bounds
		to = Math.max(0, Math.min(to, list.size()));
		
		//Add all the elements to the "to" index
		list.addAll(to, itemsToMove);
	}
	
	/**
	 * Writes an inputstream to an outputstream and closes the streams when done.
	 * @throws IOException
	 */
	public static long writeStream2Stream(InputStream is, OutputStream os) throws IOException {
		return writeStream2Stream(is, os, true);
	}
	
	/**
	 * Writes an inputstream to an outputstream.
	 * @throws IOException
	 * @return The number of bytes written.
	 */
	public static long writeStream2Stream(InputStream is, OutputStream os, boolean closeStreams) throws IOException {
		long totalLength = 0;
		try {
			if(is != null && os != null) {
				byte[] bBuffer = new byte[8192];
				int length;
				while ((length = is.read(bBuffer)) != -1) {
					totalLength += length;
					os.write(bBuffer, 0, length);
				}
			}
		} finally {
			if(closeStreams) {
				try { is.close(); } catch (Exception e2) {}
				try { os.close(); } catch (Exception e2) {}
			}
		}
		return totalLength;
	}
	
	/**
	 * Gets the extension from a File. The extension is
	 * everything after the last dot.
	 * @param file The File to get the extension from.
	 * @return The extension.
	 */
	public static String getFileExtension(File file) {
		return getFileExtension(file.getName());
	}

	/**
	 * Gets the extension from a String. The extension is
	 * everything after the last dot.
	 * @param file The String to get the extension from.
	 * @return The extension.
	 */
	public static String getFileExtension(String file) {
		return StringUtils.substringAfterLast(file, ".");
	}

	/**
	 * Gets the filename from a String. This works in client-server
	 * programs. 
	 * 
	 * It first tries to get the filename using the System's file seperator.
	 * If that doesn't work it tries to get the filename using / and \.
	 * @param file The String to get the filename from.
	 * @return The filename.
	 */
	public static String getFileName(String file) {
		//First try to get the name using the System's file seperator.
		String retValue = StringUtils.substringAfterLast(file, File.separator);
		
		if(SemanticaUtil.isEmpty(retValue)) {
			if(File.separator.equals("\\")) {
				retValue = StringUtils.substringAfterLast(file, "/");
			} else {
				retValue = StringUtils.substringAfterLast(file, "\\");
			}
		}
		
		if(SemanticaUtil.isEmpty(retValue)) {
			retValue = file;
		}
		
		return retValue;
	}

	/**
	 * Gets the filename from a File. This works in client-server
	 * programs. 
	 * 
	 * It first tries to get the filename using the System's file seperator.
	 * If that doesn't work it tries to get the filename using / and \.
	 * @param file The File to get the filename from.
	 * @return The filename.
	 */
	public static String getFileName(File file) {
		return getFileName(file.getPath());
	}

	/**
	 * Gets a random integer between 0 and Integer.MAX_VALUE.
	 * @return The random integer.
	 */
	public static int getRandom() {
		return getRandom(0, Integer.MAX_VALUE);
	}
	
	/**
	 * Gets a random integer between 0 and the specified max value.
	 * @param max The maximum value.
	 * @return The random integer.
	 */
	public static int getRandom(int max) {
		return getRandom(0, max);
	}
	
	/**
	 * Gets a random integer between the specified min and max value.
	 * @param min The minimum value.
	 * @param max The maximum value.
	 * @return The random integer.
	 */
	public static int getRandom(int min, int max) {
		return (int) (min + Math.round((max - min) * Math.random()));
	}
	
	/**
	 * Copies a file from the specified source File to the 
	 * specified destination File.
	 * @param source The source File.
	 * @param destination The destination File.
	 * @throws IOException If something goed wrong.
	 */
	public static void copyFile(File source, File destination) throws IOException {
		FileChannel sourceChannel = null;
		FileChannel destinationChannel = null;
		try {
			sourceChannel = new FileInputStream(source).getChannel();
			destinationChannel = new FileOutputStream(destination).getChannel();
			sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
		} finally {
			try {
				sourceChannel.close();
			} catch (Exception e) {}
			try {
				destinationChannel.close();
			} catch (Exception e) {}
		}
	}

	/**
	 * Delete a directory and all files and subdirectories.
	 * @param dir The directory to delete.
	 * @param includeSelf Specifies if the directory itself must be
	 * deleted or only the contents of the directory. 
	 * @return True if the directory had been deleted succesfully.
	 */
	public static boolean deleteDirectory(File dir, boolean includeSelf) {
		if (dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					if(!deleteDirectory(files[i])) {
						return false;
					}
				} else {
					if(!files[i].delete()) {
						return false;
					}
				}
			}
		}
		
		if(includeSelf) {
			return dir.delete();
		} else {
			return true;
		}
	}

	/**
	 * Delete a directory and all files and subdirectories.
	 * @param dir The directory to delete.
	 * @return True if the directory had been deleted succesfully.
	 */
	public static boolean deleteDirectory(File dir) {
		if (dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					if(!deleteDirectory(files[i])) {
						return false;
					}
				} else {
					if(!files[i].delete()) {
						return false;
					}
				}
			}
		}
		
		return dir.delete();
	}

	/**
	 * Parses a String into a Locale Object.
	 * If the String cannot be parsed this method returns
	 * Locale.getDefault();
	 * @param parse The String to parse.
	 * @return The Locale.
	 */
	public static Locale parseLocale(String parse) {
		String[] s = StringUtils.split(parse, "_");
		int size = SemanticaUtil.size(s);
		if(size <= 0) {
			return Locale.getDefault();
		}
		if(size <= 1) {
			return new Locale(s[0]);
		}
		if(size <= 2) {
			return new Locale(s[0], s[1]);
		}
		if(size <= 3) {
			return new Locale(s[0], s[1], s[2]);
		}
		return null;
	}
	
	/**
	 * Gets a value from an Object using bean getter.
	 * You can als specify multiple getters like:<br>
	 * <code>
	 * getBeanValue("hello", "class.package.name");
	 * </code>
	 * <br><br>
	 * This will reslove to: <br>
	 * <code>
	 * "hello".getClass().getPackage().getName();
	 * </code>
	 * @param bean The bean to get the value from.
	 * @param expression The expression to use.
	 * @return The resloved value.
	 * @throws Exception
	 */
	public static Object getBeanValue(Object bean, String expression) throws Exception {
		if(SemanticaUtil.isEmpty(expression)) {
			return null;
		}
		
		String[] getters = StringUtils.split(expression, '.');
		
		Method method = null;
		for (int i = 0; i < getters.length; i++) {
			if(bean == null) {
				return null;
			}
			StringBuffer sb = new StringBuffer();
			sb.append("get");
			sb.append(getters[i].substring(0, 1).toUpperCase());
			sb.append(getters[i].substring(1));
			method = bean.getClass().getMethod(sb.toString(), (Class[]) null);
			bean = method.invoke(bean, (Object[]) null);
		}
		
		return bean;
	}
	
	public static void setBeanNull(Object bean, String expression, Class clazz) throws Exception {
		if(clazz == null) {
			return;
		}
		setBeanValue(bean, expression, clazz, null);
	}
	
	public static void setBeanValue(Object bean, String expression, Object value) throws Exception {
		if(value == null) {
			return;
		}
		setBeanValue(bean, expression, value.getClass(), value);
	}
	
	public static void setBeanValue(Object bean, String expression, Class clazz, Object value) throws Exception {
		if(SemanticaUtil.isEmpty(expression)) {
			return;
		}

		if(bean == null) {
			throw new NullPointerException("Bean is null");
		}
		
		int index = expression.indexOf('.');
		if(index != -1) {
			bean = getBeanValue(bean, expression.substring(0, index));
			expression = expression.substring(index + 1);
		}
		
		if(bean == null) {
			throw new NullPointerException("Bean is null");
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("set");
		sb.append(expression.substring(0, 1).toUpperCase());
		sb.append(expression.substring(1));
		
		Method m = bean.getClass().getMethod(sb.toString(), new Class[]{clazz});
		m.invoke(bean, new Object[]{value});
	}
	
	/**
	 * Create a word-wrapped version of a String. Wrap at 80 characters and 
	 * use newlines as the delimiter. If a word is over 80 characters long 
	 * use a - sign to split it.
	 */
	public static String wordWrap(String str) {
		return wordWrap(str, 80, "\n", "-", true);
	}
	/**
	 * Create a word-wrapped version of a String. Wrap at a specified width and 
	 * use newlines as the delimiter. If a word is over the width in lenght 
	 * use a - sign to split it.
	 */
	public static String wordWrap(String str, int width) {
		return wordWrap(str, width, "\n", "-", true);
	}
	/**
	 * Word-wrap a string.
	 *
	 * @param str   String to word-wrap
	 * @param width int to wrap at
	 * @param delim String to use to separate lines
	 * @param split String to use to split a word greater than width long
	 *
	 * @return String that has been word wrapped (with the delim inside width boundaries)
	 */
	public static String wordWrap(String str, int width, String delim, String split ) {
		return wordWrap(str, width, delim, split, true);
	}
	
	/**
	 * Word-wrap a string.
	 *
	 * @param str   String to word-wrap
	 * @param width int to wrap at
	 * @param delim String to use to separate lines
	 * @param split String to use to split a word greater than width long
	 * @param delimInside wheter or not delim should be included in chunk before length reaches width.
	 *
	 * @return String that has been word wrapped
	 */
	public static String wordWrap(String str, int width, String delim,
			String split, boolean delimInside) {
		int sz = str.length();
		
		/// shift width up one. mainly as it makes the logic easier
		width++;
		
		// our best guess as to an initial size
		StringBuffer buffer = new StringBuffer(sz/width*delim.length()+sz);
		
		// every line might include a delim on the end
		//        System.err.println( "width before: "+ width );
		if ( delimInside ) {
			width = width - delim.length();
		} else {
			width --;
		}
		
		int idx = -1;
		String substr = null;
		
		// beware: i is rolled-back inside the loop
		for(int i=0; i<sz; i+=width) {
			
			// on the last line
			if(i > sz - width) {
				buffer.append(str.substring(i));
				break;
			}
			
			// the current line
			substr = str.substring(i, i+width);
			
			// is the delim already on the line
			idx = substr.indexOf(delim);
			if(idx != -1) {
				buffer.append(substr.substring(0,idx));
				buffer.append(delim);
				i -= width-idx-delim.length();
				
				// Erase a space after a delim. Is this too obscure?
				if(substr.length() > idx+1) {
					if(substr.charAt(idx+1) != '\n') {
						if(Character.isWhitespace(substr.charAt(idx+1))) {
							i++;
						}
					}
				}
				continue;
			}
			
			idx = -1;
			
			// figure out where the last space is
			char[] chrs = substr.toCharArray();
			for(int j=width; j>0; j--) {
				if(Character.isWhitespace(chrs[j-1])) {
					idx = j;
					break;
				}
			}
			
			// idx is the last whitespace on the line.
			if(idx == -1) {
				for(int j=width; j>0; j--) {
					if(chrs[j-1] == '-') {
						idx = j;
						break;
					}
				}
				if(idx == -1) {
					buffer.append(substr);
					buffer.append(delim);
				} else {
					if(idx != width) {
						idx++;
					}
					buffer.append(substr.substring(0,idx));
					buffer.append(delim);
					i -= width-idx;
				}
			} else {
				// insert spaces
				buffer.append(substr.substring(0,idx));
				buffer.append(StringUtils.repeat(" ",width-idx));
				buffer.append(delim);
				i -= width-idx;
			}
		}
		return buffer.toString();
	}

	/**
	 * Performs conversion of a long value to a byte array representation.
	 *
	 * @see #bytesToLong(byte[])
	 */
	public static byte[] longToBytes(long value) {

		// A long value is 8 bytes in length.
		byte[] bytes = new byte[8];

		// Convert and copy value to byte array:
		//   -- Cast long to a byte to retrieve least significant byte;
		//   -- Left shift long value by 8 bits to isolate next byte to be converted;
		//   -- Repeat until all 8 bytes are converted (long = 64 bits).
		// Note: In the byte array, the least significant byte of the long is held in
		// the highest indexed array bucket.

		for (int i = 0; i < bytes.length; i++) {
			bytes[(bytes.length - 1) - i] = (byte) value;
			value >>>= 8;
		}

		return bytes;
	}

	/**
	 * Performs conversion of a byte array to a long representation.
	 *
	 * @see #longToBytes(long)
	 */
	public static long bytesToLong(byte[] value) {

		long longValue = 0L;

		// See method convertLongToBytes(long) for algorithm details.	
		for (int i = 0; i < value.length; i++) {
			// Left shift has no effect thru first iteration of loop.
			longValue <<= 8;
			longValue ^= value[i] & 0xFF;
		}

		return longValue;
	}


	public static byte[] hexToBytes(String s) {
		int slen = s.length();
		if ((slen % 2) != 0) {
			s = '0' + s;
		}

		if(slen % 2 != 0) {
			throw new RuntimeException("String must be off even length.");
		}
		
		byte[] out = new byte[slen / 2];
		
		// Safe to assume the string is even length
		byte b1, b2;
		for (int i = 0; i < slen; i += 2) {
			b1 = (byte) Character.digit(s.charAt(i), 16);
			b2 = (byte) Character.digit(s.charAt(i + 1), 16);
			if (b1 < 0 || b2 < 0) {
				throw new NumberFormatException();
			}
			out[i / 2] = (byte) (b1 << 4 | b2);
		}
		
		return out;
	}

	/**
	 * Converts bytes into a HEX String.
	 * 
	 * @param buf
	 *            The bytes to convert.
	 * @return The HEX String of the bytes.
	 */
	public static String hexString(byte[] buf) {
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			str.append(HEX.charAt(buf[i] >>> 4 & 0x0F));
			str.append(HEX.charAt(buf[i] & 0x0F));
		}
		return str.toString();
	}

	/**
	 * Prints a HEX Dump to the specifies PrintStream. <br>
	 * Example: <br>
	 * <pre>
	 * 	 002d9880  D1 4E 33 AF 3D D1 FC 2D DB 0C B7 F5 FA 9E A5 BE   ?N3?=?ü-?.??ú??
	 * 	 002d9890  53 9D 7A D5 55 52 A7 BE 9C 3D BD FE 1F AE 36 32   S?z?UR§?=??.62
	 * 	 002d98a0  79 CA 0C 53 A4 1D 00 96 4C 4A 30 E2 54 8E E9 3A   y?.S¤..?LJ0âT?é:
	 * 	 002d98b0  82 AD B7 0F F8 DB 47 91 EA FC 64 A8 C8 16 0B 93   ?­?.??G??üd¨?..?
	 * </pre>
	 * 
	 * @param bytes The bytes to create a HEX Dump of.
	 * @param out The PrintStream to print the HEX Dump to (System.out).
	 * @throws IOException If something goes wrong.
	 */
	public static void printHexDump(byte[] bytes, PrintStream out) throws IOException {
		ByteArrayInputStream is = null;
		try {
			is = new ByteArrayInputStream(bytes);
			printHexDump(is, out);
		} catch(Exception e) {
			try {is.close();}catch(Exception ex){}
		}
	}
	
	/**
	 * Prints a HEX Dump to the specifies PrintStream. <br>
	 * Example: <br>
	 * <pre>
	 * 	 002d9880  D1 4E 33 AF 3D D1 FC 2D DB 0C B7 F5 FA 9E A5 BE   ?N3?=?ü-?.??ú??
	 * 	 002d9890  53 9D 7A D5 55 52 A7 BE 9C 3D BD FE 1F AE 36 32   S?z?UR§?=??.62
	 * 	 002d98a0  79 CA 0C 53 A4 1D 00 96 4C 4A 30 E2 54 8E E9 3A   y?.S¤..?LJ0âT?é:
	 * 	 002d98b0  82 AD B7 0F F8 DB 47 91 EA FC 64 A8 C8 16 0B 93   ?­?.??G??üd¨?..?
	 * </pre>
	 * 
	 * @param is The InputStream to create a HEX Dump of.
	 * @param out The PrintStream to print the HEX Dump to (System.out).
	 * @throws IOException If something goes wrong.
	 */
	public static void printHexDump(InputStream is, PrintStream out) throws IOException {
		byte[] buf = new byte[16];
		int length;
		int totalLenth = 0;
		while ((length = is.read(buf)) != -1) {
			StringBuffer line = new StringBuffer();
			line.append(StringUtils.leftPad(Integer.toString(totalLenth, 16), 8, '0'));
			line.append("  ");
			
			for (int i = 0; i < length; i++) {
				line.append(HEX.charAt(buf[i] >>> 4 & 0x0F));
				line.append(HEX.charAt(buf[i] & 0x0F));
				if(i < length - 1) {
					line.append(" ");
				}
			}
			
			line.append(StringUtils.repeat(" ", 60 - line.length()));
			
			for (int i = 0; i < length; i++) {
				if (Character.isISOControl((char) (buf[i]))) {
					line.append('.');
				} else {
					line.append((char) (buf[i] & 0xFF));
				}
			}
			
			out.println(line.toString());
			totalLenth += length;
		}
	}

	/**
	 * Abbreviates a filename. <br>
	 * Example using the following file: <br>
	 * c:\Documents and Settings\Thijs\My Documents\Welcome to DocsDB.pps <br>
	 * returns <br>
	 * c:\Documents and Settings\Thijs\...\Welcome to DocsDB.pps <br>
	 * c:\...\Welcome to DocsDB.pps <br>
	 * c:\...\Welcome to... <br><br>
	 * The result of this method depends on the specified maxLength. <br>
	 * The resulting string may exeed maxLength when this is very small. The
	 * smallest value for the above example is: <br>
	 * c:\...\Welc... <br>
	 *
	 * @param fileName The filename to abbreviate.
	 * @param maxLength The maximum length of the abbreviated String.
	 * @return The abbreviate filename.
	 */
	public static String abbreviateFileName(String fileName, int maxLength) {
		return abbreviateFileName(fileName, maxLength, File.separatorChar, true);
	}

	/**
	 * Abbreviates a filename. <br>
	 * Example using the following file: <br>
	 * c:\Documents and Settings\Thijs\My Documents\Welcome to DocsDB.pps <br>
	 * returns <br>
	 * c:\Documents and Settings\Thijs\...\Welcome to DocsDB.pps <br>
	 * c:\...\Welcome to DocsDB.pps <br>
	 * c:\...\Welcome to... <br><br>
	 * The result of this method depends on the specified maxLength. <br>
	 * The resulting string may exeed maxLength when this is very small. The
	 * smallest value for the above example is: <br>
	 * c:\...\Welc... <br>
	 *
	 * @param fileName The filename to abbreviate.
	 * @param maxLength The maximum length of the abbreviated String.
	 * @param separator The String that will be used as a separator.
	 * @return The abbreviate filename.
	 */
	public static String abbreviateFileName(String fileName, int maxLength, char separator, boolean abbreviateMiddle) {
		if(SemanticaUtil.size(fileName) <= maxLength) {
			return fileName;
		}

		String[] s = StringUtils.split(fileName, separator);
		int[] l = new int[s.length];
		for (int i = 0; i < l.length; i++) {
			l[i] = s[i].length();
		}

		int total = 0;
		int middle = l.length / 2;
		
		if(l.length % 2 == 0) {
			middle--;
		}
		
		int process = 0;
		for (int i = 0; i < l.length; i++) {
			total += l[i] + 1;
		}
		
		while(true) {
			int diff = total - maxLength;
			int idx = middle + process;
			
			if(!abbreviateMiddle && idx != l.length - 1) {
				diff = 10000000;
			}
			
			if(idx < 0) {
				break;
			}
			
			if(idx < l.length && idx >= 0) {
				if(idx == 0 || idx == l.length - 1) {
					l[idx] = Math.max(4, l[idx] - diff);
				} else {
					l[idx] = Math.max(0, l[idx] - diff);
				}
	
				total = 0;
				for (int i = 0; i < l.length; i++) {
					total += l[i] + 1;
				}
			}
			
			if(total <= maxLength) {
				break;
			}
			
			if(process == 0) {
				process++;
			} else if(process > 0) {
				process = -process;
			} else if(process < 0) {
				process = -process;
				process++;
			}
		}
		
		StringBuffer sb = new StringBuffer();
		boolean wroteAbbreviation = false;
		for (int i = 0; i < l.length; i++) {
			if(l[i] >= 4) {
				wroteAbbreviation = false;
				sb.append(StringUtils.abbreviate(s[i], l[i]));
				if(i < l.length - 1) {
					sb.append(separator);
				}
			} else if(l[i] > 0 && l[i] <= 3 && s[i].length() > 0 && s[i].length() <= 3) {
				sb.append(s[i]);
				if(i < l.length - 1) {
					sb.append(separator);
				}
			} else if(!wroteAbbreviation) {
				wroteAbbreviation = true;
				sb.append("...");
				if(i < l.length - 1) {
					sb.append(separator);
				}
			}
		}
		
		return sb.toString();
	}

	
	
	/**
	 * Replaces all control characters in a String with the
	 * specified replaceString.
	 * @param s The String to replace the control characters in.
	 * @param replaceString The replaceString.
	 * @return
	 */
	public static String replaceControlCharacters(String s, String replaceString) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(Character.isISOControl(c)) {
				sb.append(replaceString);
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String escapeRegEx(String in) {
		StringBuffer sb = new StringBuffer();
		String escapeChars = "([{\\^$|)?*+.";
		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			if(escapeChars.indexOf(c) != -1) {
				sb.append("\\");
			}
			sb.append(c);
		}
		return sb.toString();
	}
	
	public static String md5(String value) {
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		
		char[] charArray = value.toCharArray();
		byte[] byteArray = new byte[charArray.length];
		
		for (int i=0; i<charArray.length; i++) {
			byteArray[i] = (byte) charArray[i];
		}
		
		byte[] md5Bytes = md5.digest(byteArray);
		
		return hexString(md5Bytes);
	}
	
	public static String log(Object[] params) {
		return log(null, params);
	}
	
	public static String log(String message, Object param1) {
		return log(message, new Object[]{param1});
	}
	
	public static String log(String message, Object param1, Object param2) {
		return log(message, new Object[]{param1, param2});
	}
	
	public static String log(String message, Object param1, Object param2, Object param3) {
		return log(message, new Object[]{param1, param2, param3});
	}
	
	public static String log(String message, Object param1, Object param2, Object param3, Object param4) {
		return log(message, new Object[]{param1, param2, param3, param4});
	}
	
	public static String log(String message, Object param1, Object param2, Object param3, Object param4, Object param5) {
		return log(message, new Object[]{param1, param2, param3, param4, param5});
	}
	
	public static String log(String message, Object[] params) {
		StringBuffer sb = new StringBuffer();
		if(isNotEmpty(message)) {
			sb.append(message);
			sb.append(" ");
		}
		
		sb.append("<");
		if(params != null) {
			for (int i = 0; i < params.length; i++) {
				sb.append(String.valueOf(params[i]));
				if(i < params.length - 1) {
					sb.append(", ");
				}
			}
		}
		sb.append(">");
		return sb.toString();
	}

	
	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T clone(T obj){
		try{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream objOut = new ObjectOutputStream(out);
			objOut.writeObject(obj);
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			ObjectInputStream objIn = new ObjectInputStream(in);
			return (T) objIn.readObject();
		} catch(Exception ex) {
			throw new RuntimeException("CloneNotSupported: " + ex.toString(), ex);
		}
	}
}

