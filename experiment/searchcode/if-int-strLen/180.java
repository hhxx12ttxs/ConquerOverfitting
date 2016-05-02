package au.com.adcowebsolutions.formatting;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for performing common string manipulations.
 * 
 * @author Russell Adcock
 */
public class StringUtils {

	public static final String BLANK = " ";
	public static final String EMPTY_STRING = "";

    // The value returned from indexof when the match is not found.
    public final static int INDEXOF_NOT_FOUND = -1;

	// Do not allow the an instance Creation. All methods are static
	private StringUtils() {}

	public static Integer[] toIntegerArray(String[] strings) {
        List<Integer> integers = new ArrayList<Integer>();
        for (int i = 0; i < strings.length; i++) {
            integers.add(new Integer(strings[i]));
        }
        return integers.toArray(new Integer[integers.size()]);
    }

	public static String merge(String[] lines, String delimiter) {
		String merged = EMPTY_STRING;
		for (int i=0; lines != null && i < lines.length; i++) {
			if (!StringUtils.isEmpty(lines[i])) {
				merged += delimiter + lines[i].trim();
			}
		}
		if (StringUtils.isEmpty(merged)) {
			return null;
		}

		return merged.substring(delimiter.length());
	}

	/**
	 * Checks if the string is not a number
	 *
	 * @param number Number to be checked
	 * @return true is value is not a number
	 */
	public static boolean isNaN(String number) {
		// Validate argument
		if (isEmpty(number)) {
			return true; //empty strings are not numbers
		}

		try {
			NumberFormat nf = NumberFormat.getInstance();
			nf.parse(number);
		}
		catch (ParseException e) {
			return true; //string could not be parsed to a number
		}

		String trimmedNumber = number.trim();

		for (int i = 0; i < trimmedNumber.length(); i++) {
			char c = trimmedNumber.charAt(i);
			if (!Character.isDigit(c) && c != '.' && c != ',') {
				return true; //string contains invalid character
			}
		}

		return false; //string is a number
	}

	/**
	 * A positive version of the isNaN (isNotANumber) method
	 *
	 * @param number
	 * @return
	 */
	public static boolean isNumber(String number) {
		return !isNaN(number);
	}

	/**
	 * Return true if only numbers, '-' and ' ' in the string
	 * This is mainly for Bug 5171, GDS accept '-' and ' ' in phone numbers
	 *
	 * @param s
	 * @return
	 */
	public static boolean isGdsNumber(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isDigit(s.charAt(i)) && s.charAt(i) != ' ' && s.charAt(i) != '-') {
				return false;
			}
		}

		return true;
	}

	/**
	 * A positive version of the isGdsNumber method
	 *
	 * @param s
	 * @return
	 */
	public static boolean isNaGdsNumber(String s) {
		return !isGdsNumber(s);
	}

	/**
	 * Returns false if any non-alphanumeric (non-letter and number) characters are contained in the string
	 * Any characters passed in the exceptionList (optional) will be treated as if they are alpha-numeric
	 */
	public static boolean isAlphaNumericOnly(String s) {
		return isAlphaNumericOnly(s, null);
	}


	public static boolean isAlphaNumericOnly(String s, String exceptionList) {

		// Remove any of the exceptionList characters from the string
		// So that they don't cause the isLetter() check to fail
		if (exceptionList != null) {
			// for each character in exceptionList, remove it from the string
			for (int i = 0; i < exceptionList.length(); i++) {
				s = removeChar(s, exceptionList.charAt(i));
			}
		}

		for (int i = 0; i < s.length(); i++) {
			if (!Character.isLetterOrDigit(s.charAt(i))) {
				return false;
			}
		}

		// No non-numeric or digit characters found - return Ok
		return true;
	}

    public static boolean isNumericOnly(String s) {
        return isNumericOnly(s, null);
    }

    public static boolean isNumericOnly(String s, String exceptionList) {

        // Remove any of the exceptionList characters from the string
        // So that they don't cause the isLetter() check to fail
        if (exceptionList != null) {
            // for each character in exceptionList, remove it from the string
            for (int i = 0; i < exceptionList.length(); i++) {
                s = removeChar(s, exceptionList.charAt(i));
            }
        }

        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }

        // No non-numeric or digit characters found - return Ok
        return true;
    }



	/**
	 * Returns false if any non-alpha (non-letter) characters are contained in the string
	 * Any characters passed in the exceptionList (optional) will be treated as if they are alpha
	 */
	public static boolean isAlphaOnly(String s) {
		return isAlphaOnly(s, null);
	}


	public static boolean isAlphaOnly(String s, String exceptionList) {
		boolean isAlphaOnly = true;

		// Remove any of the exceptionList characters from the string
		// So that they don't cause the isLetter() check to fail
		if (exceptionList != null) {
			// for each character in exceptionList, remove it from the string
			for (int i = 0; i < exceptionList.length(); i++) {
				s = removeChar(s, exceptionList.charAt(i));
			}
		}

		// Search for any non alpha (letter) characters in the string
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isLetter(s.charAt(i))) {
				isAlphaOnly = false;
				break;   // non-letter found, exit the loop
			}
		}

		// No non-numeric or digit characters found - return Ok
		return isAlphaOnly;
	}


	/**
	 * Remove any non-numeric characters from the String
	 */
	public static String getNumericOnly(String s) {
		StringBuffer sBuffer = new StringBuffer();

		// Check each character in the string.
		// If it is numeric, append it to the StringBuffer object
		if (s != null) {
			for (int i = 0; i < s.length(); i++) {
				char ch = s.charAt(i);
				if (Character.isDigit(ch)) {
					sBuffer.append(ch);
				}
			}
		}

		return sBuffer.toString();
	}


	/**
	 * Remove any non alpha-numeric characters from the String
	 * Any characters passed in exceptionList (optional) will be included as if they were aplha-numeric.
	 */
	public static String getAlphaNumericOnly(String s) {
		return getAlphaNumericOnly(s, null);
	}


	public static String getAlphaNumericOnly(String s, String exceptionList) {
		StringBuffer sBuffer = new StringBuffer();

		// Check each character in the string.
		// If it is alpha-numeric, append it to the StringBuffer object
		if (s != null) {
			for (int i = 0; i < s.length(); i++) {
				char ch = s.charAt(i);
				if (Character.isLetterOrDigit(ch) || (exceptionList != null && exceptionList.indexOf(ch) >= 0)) {
					sBuffer.append(ch);
				}
			}
		}

		return sBuffer.toString();
	}


	/**
	 * Remove any non alpha characters from the String.
	 * Any characters passed in exceptionList (optional) will be included as if they were aplha.
	 */
	public static String getAlphaOnly(String s) {
		return getAlphaOnly(s, null);
	}


	public static String getAlphaOnly(String s, String exceptionList) {
		StringBuffer sBuffer = new StringBuffer();

		// Check each character in the string.
		// If it is alpha-numeric or in the exception list, append it to the StringBuffer object
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (Character.isLetter(ch) || (exceptionList != null && exceptionList.indexOf(ch) >= 0)) {
				sBuffer.append(ch);
			}
		}

		return sBuffer.toString();
	}


	/**
	 * Removes any embedded, leading and trailing spaces from the phone number passed as an argument.
	 * If no hyphens exist in the number then insert them as follows
	 * length   format
	 * ---------------
	 * 6      xxx-xxx
	 * 7      xxx-xxxx
	 * 8      xxxx-xxxx
	 * 9      xxx-xxx-xxx
	 * 10      xxxx-xxx-xxx
	 */
	public static String formatNumber(String number) {

		StringBuffer phoneNumber = new StringBuffer(number.trim());
		boolean hyphenFound = false;

		// firstly strip any embedded spaces and determine if hyphens exist
		for (int i = 0; i < phoneNumber.length(); i++) {
			if (phoneNumber.charAt(i) == '-') {
				hyphenFound = true;
			}
			if (phoneNumber.charAt(i) == ' ') {
				phoneNumber.deleteCharAt(i);
			}
		}

		// If no hyphens exist then insert them
		if (!(hyphenFound)) {
			if (phoneNumber.length() == 6 || phoneNumber.length() == 7) {
				phoneNumber.insert(3, '-');
			}
			else {
				if (phoneNumber.length() == 8) {
					phoneNumber.insert(4, '-');
				}
				else {
					if (phoneNumber.length() == 9) {
						phoneNumber.insert(6, '-');
						phoneNumber.insert(3, '-');
					}
					else {
						if (phoneNumber.length() == 10) {
							phoneNumber.insert(7, '-');
							phoneNumber.insert(4, '-');
						}
					}
				}
			}
		}

		return phoneNumber.toString();
	}


	/**
	 * Helper method pads a string with blank chars to the right
	 * <p>
     * <b>Important!</b> This will truncate if the length of the string is longer than the specified pad-to length.
	 */
	public static String padRight(String value, int length) {
		return padRight(value, length, ' ');
	}


	/**
	 * Helper method, that pads a string out to the required length
	 * <p>
     * <b>Important!</b> This will truncate if the length of the string is longer than the specified pad-to length.
	 */
	public static String padRight(String value, int length, char with) {
		if (value == null) {
			return null;
		}
		if (length < 0) {
			throw new IllegalArgumentException("length must be at least zero");
		}
		if (value.length() > length) {
			return value.substring(0, length);
		}

		StringBuffer buff = new StringBuffer(value);

		for (int i = value.length(); i < length; i++) {
			buff.append(with);
		}

		return buff.toString();
	}
	/**
	 * Helper method, that pads a string out to the required length. method will
	 * pad a null to the correct value as well.
	 * <p>
	 * padRightIncludeNull(null, 3, '_') will return '___'
	 * as opposed to padRight which will return null.
	 * <p>
	 * <b>Important!</b> This will truncate if the length of the string is longer than the specified pad-to length.
	 */
	public static String padRightIncludeNull(String value, int length) {
		return padRightIncludeNull(value, length, ' ');
	}
	/**
	 * Helper method, that pads a string out to the required length. method will
	 * pad a null to the correct value as well.
	 * <p>
	 * padRightIncludeNull(null, 3, '_') will return '___'
	 * as opposed to padRight which will return null.
	 * <p>
	 * <b>Important!</b> This will truncate if the length of the string is longer than the specified pad-to length.
	 */
	public static String padRightIncludeNull(String value, int length, char with) {
		if (value == null) {
			value = EMPTY_STRING;
		}
		if (length < 0) {
			throw new IllegalArgumentException("length must be at least zero");
		}

		if (value.length() > length) {
			return value.substring(0, length);
		}

		StringBuffer buff = new StringBuffer(value);

		for (int i = value.length(); i < length; i++) {
			buff.append(with);
		}

		return buff.toString();
	}

	/**
	 * Helper method pads a string with blank chars to the left.
	 * <p>
     * <b>Important!</b> This will truncate if the length of the string is longer than the specified pad-to length.
     * @deprecated use {@link #padLeft(String, int, boolean)} to specify whether to truncate or not
	 */
	@Deprecated
	public static String padLeft(String value, int length) {
		return padLeft(value, length, ' ', true);
	}

	/**
     * Helper method that pads a string out to the required length with spaces.
     * @param value the initial string
     * @param length the desired length of the string
     * @param truncate true if the string length should be truncated at that length; false if longer strings should be
     *        preserved.
     * @return the String defined in 'value', with space characters prepended.
     */
	public static String padLeft(String value, int length, boolean truncate) {
	    return padLeft(value, length, ' ', truncate);
	}

	/**
     * Helper method, that pads a string out to the required length.
     * <p>
     * <b>Important!</b> This will truncate if the length of the string is longer than the specified pad-to length.
     * @deprecated use {@link #padLeft(String, int, char, boolean)} to specify whether to truncate or not
     */
	@Deprecated
	public static String padLeft(String value, int length, char with) {
	    return padLeft(value, length, with, true);
	}

	/**
     * Helper method that pads a string out to the required length.
     * @param value the initial string
     * @param length the desired length of the string
     * @param with the character to pad smaller strings to make the desired length
     * @param truncate true if the string length should be truncated at that length; false if longer strings should be
     *        preserved.
     * @return the String defined in 'value', with characters prepended.
     */
	public static String padLeft(String value, int length, char with, boolean truncate) {
		if (length < 0) throw new IllegalArgumentException("Cannot pad left " + length + " characters");
        if (value == null) {
            return null;
        }
        if (value.length() > length) {
            return truncate ? value.substring(0, length) : value;
        }

        StringBuffer buff = new StringBuffer(value);

        for (int i = value.length(); i < length; i++) {
            buff.insert(0, with);
        }

        return buff.toString();
	}


	/**
	 * Determines whether the string is empty.
	 * ie null or zero in length
	 */
	public static boolean isEmpty(String value) {
		return value == null || value.trim().length() == 0;
	}

	/**
	 * Determines if the given string has a value.
	 * Ie is not null and not length of zero.
	 *
	 * @param value
	 * @return
	 */
	public static boolean hasValue(String value) {
		return !isEmpty(value);
	}


	/**
	 * Strips occurances of the specified character from the start of a string.
	 */
	public static String stripLeadingChars(String originalString, char charToStrip) {
		// Return a null string if that's what was passed.
		if (originalString == null) {
			return originalString;
		}

		int pos;
		for (pos = 0; pos < originalString.length(); pos++) {
			if (originalString.charAt(pos) != charToStrip) {
				break;
			}
		}

		return originalString.substring(pos);
	}

	/**
	 * Removes the prefix from the given String if it actually starts with it.
	 * If the text or the prefix are null, empty or blank, the original text
	 * will be returned. It will not remove anything if the prefix does not
	 * match the original text case, in other words, this method is case sensitive.
	 *
	 * @param prefix, The text to be removed
	 * @param text, The text where prefix will be removed from
	 * @return The text given without the prefix.
	 */
	public static String removePrefix(String prefix, String text) {
	    if (isEmpty(prefix) || isEmpty(text)) {
	        return text;
	    }
        return text.startsWith(prefix) ? text.substring(prefix.length()) : text;
    }

	/**
	 * Removes all occurrences of the given char from the given String
	 */
	public static String removeChar(String s, char ch) {
		StringBuffer ret = new StringBuffer();

		if (s != null) {
			// Loop through all the characters in the string
			// If they don't match the character to be removed, add them to the return string
			for (int i = 0; i < s.length(); i++) {
				if (ch != s.charAt(i)) {
					ret.append(s.charAt(i));
				}
			}
		}

		return ret.toString();
	}

	public static String removeChars(String s, char[] cs) {
		String ret = s;
		if (s != null && cs != null) {
			for(int i=0; i<cs.length; i++) {
				ret = removeChar(ret, cs[i]);
			}
		}
		return ret;
	}


	/**
	 * Trims a string of spaces. It handles nulls unlike java.lang.String.trim().
	 */
	public static String trim(String s) {
		return (s == null ? null : s.trim());
	}

	/**
	 * Replaces the occurrenceToReplace occurrence of searchStr in origStr with replaceStr.
	 * Note:
	 *  -occurenceToReplace = 0 means 1st occurrence and so on.
	 *  - Returns original string if given null or zero length searchStr or null replaceStr
	 *
	 * @param origStr
	 * @param searchStr
	 * @param replaceStr
	 * @param occurrenceToReplace
	 * @return the new String.
	 *
	 */
	public static String replace(String origStr, String searchStr, String replaceStr, int occurrenceToReplace) {

		if (origStr == null || origStr.length() == 0 || searchStr == null ||
				replaceStr == null || searchStr == EMPTY_STRING) {
			return origStr;
		}

		int occurence = -1;
		int len = origStr.length();
		StringBuffer returnStr = new StringBuffer(len);
		int pos = 0;
		int i = 0;

		while (pos < len && (i = origStr.indexOf(searchStr, pos)) != -1) {
			if (i > 0) {
				returnStr.append(origStr.substring(pos, i));
			}

			occurence++;

			if (occurence == occurrenceToReplace) {
				returnStr.append(replaceStr);
				pos = i + searchStr.length();
			}
			else {
				returnStr.append(searchStr);
				pos = i + searchStr.length();
			}
		}

		if (pos < len) {
			returnStr.append(origStr.substring(pos));
		}

		return returnStr.toString();
	}


	/**
	 * Replaces all occurrences of searchStr in origStr with replaceStr.
	 * Note: Returns original string if given null or zero length searchStr or null replaceStr
	 * 
	 * @param origStr
	 * @param searchStr
	 * @param replaceStr
	 * @return the new String.
	 */
	public static String replaceAll(String origStr, String searchStr, String replaceStr) {
		if (origStr == null || origStr.length() == 0 || searchStr == null ||
				replaceStr == null || searchStr == EMPTY_STRING) {
			return origStr;
		}


		int len = origStr.length();
		StringBuffer returnStr = new StringBuffer(len);
		int pos = 0;
		int i = 0;

		while (pos < len && (i = origStr.indexOf(searchStr, pos)) != -1) {
			if (i > 0) {
				returnStr.append(origStr.substring(pos, i));
			}
			returnStr.append(replaceStr);
			pos = i + searchStr.length();
		}

		if (pos < len) {
			returnStr.append(origStr.substring(pos));
		}

		return returnStr.toString();
	}

	/**
	 * Converts the contents of the collection to a string with the items in list separated by the separator argument
	 *
	 * @param collection
	 * @param separator
	 * @return
	 */
	public static String toString(Collection<String> collection, String separator) {
		if (collection == null || collection.size() == 0) {
			return null;
		}

		Iterator<String> i = collection.iterator();
		StringBuffer result = new StringBuffer();

		while (i.hasNext()) {
			if (result.length() > 0) {
				result.append(separator);
			}
			result.append(i.next());
		}

		return result.toString();
	}

	/**
	 * Converts the string array to a string with the items in list separated by the separator argument
	 *
	 * @param list
	 * @param separator
	 * @return
	 */
	public static String toString(String[] list, String separator) {
		if (list == null || list.length == 0) {
			return null;
		}

		StringBuffer result = new StringBuffer();

		for (int i = 0; i < list.length; i++) {
			if (!isEmpty(list[i])) {
				if (result.length() > 0) {
					result.append(separator);
				}
				result.append(list[i]);
			}
		}

		return result.toString();
	}

	/**
	 * Returns the value, or in the case this is empty, returns the default value
	 * @param value the value
	 * @param defaultValue the default value
	 * @return the value, or in the case this is empty, returns the default value
	 */
	public static String toString(Object value, Object defaultValue) {
		if (value == null || isEmpty(String.valueOf(value))) {
			return String.valueOf(defaultValue);
		}
		return String.valueOf(value);
	}

	public static String toUpperCase(String str) {
		if (str == null) {
			return null;
		} else {
			return str.toUpperCase();
		}
	}

	/**
	 * Trims and converts the string provided to uppercase.
	 * Passing a null argument will be ignored. It will not cause a NullPointerException.
	 *
	 * @param str the string to be trimmed and converted.
	 * @return a trimmed and converted string.
	 */
	public static String trimToUpperCase(String str) {
		if (str == null) {
			return null;
		}
		return str.trim().toUpperCase();
	}

	/**
	 * Removes special characters that confuse the Powerbuilder RTF document generator.
	 * Ascii codes of special characters are 0 <= Char <= 9, 11 <= Char <= 12, 14 <= Char <= 31 and 127 <= Char <= 255.
	 */
	public static String stripSpecialChars(String str) {
		if (isEmpty(str)) {
			return null;
		}

		StringBuffer retStr = new StringBuffer(str.length());
		char[] strChars = str.toCharArray();
		for (int i = 0; i < strChars.length; i++) {
			if (strChars[i] <= 9 || (strChars[i] >= 11 && strChars[i] <= 12) || (strChars[i] >= 14 && strChars[i] <= 31) || (strChars[i] >= 127 && strChars[i] <= 255)) {
				// It's a special character, so ignore it.
			}
			else {
				retStr.append(strChars[i]);
			}
		}

		return retStr.toString();
	}

	/**
	 * Removes non Alpha characters but does include spaces.
	 *
	 * @param str
	 * @return
	 */
	public static String stripNonAlpha(String str) {
		if (isEmpty(str)) {
			return null;
		}

		StringBuffer retStr = new StringBuffer(str.length());
		char[] strChars = str.toCharArray();
		for (int i = 0; i < strChars.length; i++) {
			if ((strChars[i] >= 65 && strChars[i] <= 90) || (strChars[i] >= 97 && strChars[i] <= 122) || (strChars[i] == 32)) {
				retStr.append(strChars[i]);
			}
		}

		return retStr.toString();
	}

	/**
	 * Tests two string values for equality. This function also checks
	 * if both strings are null.
	 *
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static boolean isEqual(String str1, String str2) {
		if (str1 == null) {
			return str2 == null;
		}
		else {
			return str1.equals(str2);
		}
	}

	/**
	 * Tests two string values for equality. This function also checks
	 * if both strings are null.
	 *
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static boolean isEqualIgnoreCase(String str1, String str2) {
		if (str1 == null) {
			return str2 == null;
		}
		else {
			return str1.equalsIgnoreCase(str2);
		}
	}

	/**
	 * Parses a fixed field length string into an array of items matching those given in the int array
	 * Note: Any fields that run beyond the length of the string will just be returned as null
	 *
	 * @param str          - the string to parse
	 * @param fieldLengths - an array of field lengths
	 * @return
	 * @deprecated This method is faulty.  Use StringUtils.parse2(String str, int[] fieldLenthds) instead
	 */
	@Deprecated
	public static String[] parse(String str, int[] fieldLengths) {
		String[] ret = null;

		if (hasValue(str) && fieldLengths != null) {
			// Size the target array
			ret = new String[fieldLengths.length];

			// Remove the next field length from the current position in the string
			// and set it into the target array
			int pos = 0;
			for (int i = 0; i < fieldLengths.length; i++) {
				// Check this field is not longer than the current string from the current position
				// Or, can be longer than remainder of string if is last field
				if (fieldLengths[i] > 0 && (((fieldLengths[i] + pos) <= (str.length())) || (i == fieldLengths.length - 1))) {
					if (pos + fieldLengths[i] <= str.length()) {
						ret[i] = str.substring(pos, pos + fieldLengths[i]);
					}
					else if (pos <= str.length()) {
						// This must be the last fieldLength which extends beyond the length of the string
						// Just get from the current position to the end
						// The current position should always be less than the total length of the string
						ret[i] = str.substring(pos);
					}

				}
				// Always advance the current position
				// - if one field goes beyond the end of the string, all the following field will too
				pos += fieldLengths[i];
			}
		}

		return ret;
	}

	/**
	 * Parses a fixed field length string into an array of items matching those given in the int array
	 * Note: Any fields that run beyond the length of the string will just be returned as null
	 * unless it is the last field and there are characters remaining in the string
	 * Any fields with a negative value are ignored and their corresponding positions
	 * in the string array are left as null
	 *
	 * @param str          - the string to parse
	 * @param fieldLengths - an array of field lengths
	 * @return
	 */
	public static String[] parse2(String str, int[] fieldLengths) {
		String[] ret = null;

		if (hasValue(str) && fieldLengths != null) {
			// Size the target array
			ret = new String[fieldLengths.length];

			// Remove the next field length from the current position in the string
			// and set it into the target array
			int pos = 0;
			for (int i = 0; i < fieldLengths.length; i++) {
				// Check this field is not longer than the current string from the current position
				// Or, can be longer than remainder of string if is last field
				if (fieldLengths[i] > 0 && (((fieldLengths[i] + pos) <= (str.length())) || (i == fieldLengths.length - 1))) {
					if (pos + fieldLengths[i] <= str.length()) {
						ret[i] = str.substring(pos, pos + fieldLengths[i]);
						pos += fieldLengths[i];
					}
					else if (pos < str.length()) {
						// This must be the last fieldLength which extends beyond the length of the string
						// If there is more in the string just get from the current position to the end
						// The current position should always be less than the total length of the string
						ret[i] = str.substring(pos);
					} else if (pos == str.length()) {
						// This must be the last fieldLength which extends beyond the length of the string
						//Leave last array value null
					}



				}
			}
		}

		return ret;
	}

	/**
	 * Converts all occurences of \r or \r\n in a string to HTML <br> characters.
	 *
	 * @param rawString
	 * @return
	 */
	public static String convertNewLinesToHtml(String rawString) {
		StringBuffer convertedString = new StringBuffer(rawString.length());
		String newLine = "<br>";
		char currChar;
		char nextChar;

		for (int i = 0; i < rawString.length(); i++) {
			currChar = rawString.charAt(i);

			if ((i + 1) < rawString.length()) {
				nextChar = rawString.charAt(i + 1);
			}
			else {
				nextChar = rawString.charAt(i);
			}

			if (currChar == 13) {
				convertedString.append(newLine);
				if (nextChar == 10) {
					i++;
				}
			}
			else {
				convertedString.append(currChar);
			}
		}

		return convertedString.toString();
	}

	/**
	 * Gets the boolean equivaluent of a string.
	 * Returns true for the string "1" or any string that starts with y Y t or T
	 *
	 *
	 * For example:
	 * if the lowercase string is t, true, y, yes or 1, then return true.
	 *
	 * @param val
	 * @return boolean
	 */
	public static boolean toBoolean(String val) {
		if (val == null) {
			return false;
		}

		String lcVal = val.toLowerCase();

		if (lcVal.startsWith("t") || lcVal.startsWith("y") || "1".equals(lcVal)) {
			return true;
		}

		return false;
	}


	public static String capitalizeAllWords(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return str;
		}
		StringBuffer buffer = new StringBuffer(strLen);
		boolean whitespace = true;
		for (int i = 0; i < strLen; i++) {
			char ch = str.charAt(i);
			if (Character.isWhitespace(ch)) {
				buffer.append(ch);
				whitespace = true;
			}
			else if (whitespace) {
				buffer.append(Character.toTitleCase(ch));
				whitespace = false;
			}
			else {
				buffer.append(ch);
			}
		}
		return buffer.toString();
	}

	/**
     * Returns true if the list contains the value exactly
     * including whitespace.
     * Will accept null in param "value".
	 *
	 * @param list
	 * @param value
	 * @return boolean
	 */
	public static boolean contains(String[] list, String value) {
		return contains(list, value, false);
	}

	/**
	 * Returns true if the list contains the value including whitespace.
	 * Depending on the boolean ignoreCase matches strings with the some content
	 * but differing case on letters will return true.
	 * Will accept null in param "value".
	 *
	 * @param list
	 * @param value
	 * @param ignoreCase
	 * @return boolean
	 */
	public static boolean contains(String[] list, String value, boolean ignoreCase) {
		if (list == null || list.length == 0) {
			return false;
		}

		if (value != null) {
			for (int i=0; i < list.length; i++) {
				if ( (ignoreCase && value.equalsIgnoreCase(list[i])) ||
				      (!ignoreCase && value.equals(list[i])) ) {
					return true;
				}
			}
		} else {
			for (int i=0; i < list.length; i++) {
				if ( list[i] == null) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * If the value is null, then returns an empty string, otherwise returns the value
	 * @param value the value
	 * @return a string
	 */
	public static String deNull(String value) {
		return (value == null ? EMPTY_STRING : value);
	}

	/**
     * Applies a decimal point to the correct place in the numeric string (example: Base Fare)
     *
     * Will throw IllegalArgumentException if input string contains any
     * non numeric characters.  This includes the '-' character and so this
     * method does not support negative number strings.
     *
     * @param numericString
     * @param decimalPlaces
     * @return String with decimal point
     */
    public static String applyDecimalPoint(String numericString, int decimalPlaces) {
        if (decimalPlaces < 0) {
            throw new IllegalArgumentException("decimalPlaces can not be negative but got: " + decimalPlaces);
        }
        if (StringUtils.isEmpty(numericString)) {
            throw new IllegalArgumentException("numericString can not be null or empty!");
        }
        String trimmedNumericString = numericString.trim();
        if (!isNumericOnly(trimmedNumericString)) {
            throw new IllegalArgumentException("numericString contains non numeric characters: '" + numericString + "'");
        }

        int digitsEntered = trimmedNumericString.length();
        int decimalPointIndex = Math.max(0, digitsEntered - decimalPlaces);

        if (decimalPlaces == 0) {
        	return trimmedNumericString;
        } else if (decimalPlaces < digitsEntered) {
        	return trimmedNumericString.substring(0, decimalPointIndex) + "." + trimmedNumericString.substring(decimalPointIndex, digitsEntered);
        } else {
        	//decimalPlaces >= digitsEntered
        	return "0." + padLeft(trimmedNumericString, decimalPlaces, '0', true);
        }
    }

    /**
     * Checks for characters in the value string that are not defined in the pattern of legal characters
     * @param value String representing the value that needs to be checked for illegal characters
     * @return boolean representing whether the value contains Illegal characters or not
     */
    public static boolean containsInvalidCharacters(String value, String validationExpression) {
        if(isEmpty(value)) return false;
        Pattern pattern = Pattern.compile(validationExpression);
        Matcher matcher = pattern.matcher(value);
        // if it finds a character that's not in the pattern - then it contains invalid characters
        if(matcher.find()) {
            return true;
        }
        return false;
    }

    public static String[] split(String splitString, String delimiter) {
        ArrayList<String> arraylist = new ArrayList<String>();
        int i = 0;
        for(boolean flag = true; flag;) {
            int j = splitString.indexOf(delimiter, i);
            if(j == -1) {
                j = splitString.length();
                flag = false;
            }
            arraylist.add(splitString.substring(i, j));
            i = j + delimiter.length();
        }

        return arraylist.toArray(new String[arraylist.size()]);
    }


	public static String nullIfEmpty(String s) {
		return (hasValue(s) ? s : null);
	}

    public static String removeDelimiter(String token, String delimiter) {
    	if (token == null || token.length() == 0) {
			return token;
		}
    	if (delimiter == null) {
    		return token;
    	}
    	return replaceAll(token, delimiter, EMPTY_STRING);
    }
    
    public static String convertUnderscoreToCamelCase(String s) {
        StringBuilder sb = new StringBuilder();
        boolean uppercaseNextChar = false;
        for (char c : s.toCharArray()) {
            if (c == '_') {
                uppercaseNextChar = true;
            } else {
                if (uppercaseNextChar) {
                    sb.append(Character.toString(c).toUpperCase());
                    uppercaseNextChar = false;
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}

