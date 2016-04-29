/* =================================================================
Copyright (C) 2009 ADV/web-engineering All rights reserved.

This file is part of Mozart.

Mozart is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Mozart is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

Mozart
http://www.mozartcms.ru
================================================================= */
package ru.adv.util;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * ???????? ????????? ??? ?????? ?? ????????.
 * 
 * @version $Revision: 1.29 $
 */
public class Strings {

	private static final String WHITE_SPACES = " \t\n\r\u00a0";

	public static final String WORD_DELIMITERS = WHITE_SPACES
			+ ",./?\\|;:'\"[]{}~`!*+-=()<>@#$%^&";
	
	private Strings() {
	}

	/**
	 * Pads given string with simbol
	 * 
	 * @param s
	 *            source string
	 * @param symbol
	 * @param len
	 *            desired length
	 * @return padded string
	 */
	public static String pad(String s, char symbol, int len) {
		StringBuffer result = new StringBuffer(len);
		result.append(s);
		appendSymbols(result, symbol, len - s.length());
		return result.toString();
	}

	public static void appendSymbols(StringBuffer result, char symbol,
			final int number) {
		for (int p = number; p > 0; --p) {
			result.append(symbol);
		}
	}

	public static String padWithDots(String s) {
		return Strings.pad(s, '.', 50);
	}
    
    /**
     * ???? ????????? ???????? ?? subStrins ? str.
     * ??????? ???????? ????? ?????? ? ???????????? ??????.
     * @param str
     * @param subStrings
     * @return TreeMap, ??????? ???????? ??????? Integer - ??????? ? 
     * ??????? ??????, ???????? - ????????? ?????????, ??????? ?????????? ? ???????
     * ???????.
     */
    public static TreeMap matchSubstrings(String str, Set subStrings) {
        TreeMap result = new TreeMap();
        
        // sort substring by length at first
        TreeSet sortedSubStrings = new TreeSet(
                new Comparator() {
                    public int compare(Object o1, Object o2) {
                        // compare by length at first
                        if (o1.toString().length()==o2.toString().length()) {
                            // compare by alphabet  
                            o1.toString().compareTo(o2.toString());
                        }
                        return o2.toString().length()-o1.toString().length();
                    }
                }
        );
        sortedSubStrings.addAll(subStrings);
        
        LinkedList matchedParts = new LinkedList();
        for (Iterator i = sortedSubStrings.iterator(); i.hasNext();) {
            String subStr = (String)i.next();
            if (subStr.length()==0) {
                continue;
            }
            //find matches for subStr
            int idx = 0;
            int endIdx = 0;
            while ( -1 != ( idx = str.indexOf(subStr,endIdx) ) ) {
                endIdx = idx + subStr.length();
                if (_matchSubstringsNoPartOf(matchedParts,idx)) {
                    result.put(new Integer(idx),subStr);
                    matchedParts.add(new int[]{idx, endIdx-1});
                }
            }
        }
        return result;
    }
    private static boolean _matchSubstringsNoPartOf(List matchedParts,int idx) {
        Iterator i = matchedParts.iterator();
        while (i.hasNext()) {
            int[] part = (int[])i.next();
            if (part[0]<=idx && idx<=part[1]) {
                return false;
            }
        }
        return true;
    }

	/**
	 * Pads given string with spaces
	 * 
	 * @param s
	 *            source string
	 * @param len
	 *            desired length
	 * @return padded string
	 */
	public static String pad(String s, int len) {
		return pad(s, ' ', len);
	}

	public static String leftPad(String s, char symbol, int len) {
		StringBuffer result = new StringBuffer(len);
		appendSymbols(result, symbol, len - s.length());
		result.append(s);
		return result.toString();
	}

	public static String leftPad(String s, int len) {
		return leftPad(s, ' ', len);
	}

	/**
	 * ????????? ?????? ?? ????????? ????? ?? ???????????? <code>delim</code>,
	 * ??????????? ? ????????? ?? ?????????.
	 * 
	 * @param str
	 * @param delim
	 *            ?????? ? ????????
	 * @see #split(String,String,boolean)
	 */
	public static List<String> split(String str, String delim) {
		return split(str, delim, false);
	}

	/**
	 * ????????? ?????? ?? ????????? ????? ?? ???????????? <code>delim</code>
	 * 
	 * @param str
	 * @param delim
	 *            ?????? ? ????????
	 * @param returnDelims
	 *            ????????? ??????????? ? ?????????
	 * @return List of Strings, ??? ?????? ?????? - List.size() == 0
	 */
	public static List<String> split(String str, String delim, boolean returnDelims) {
		List<String> strings = new LinkedList<String>();
		if (str == null || str.length() == 0) {
			return strings;
		}
		StringTokenizer st = new StringTokenizer(str, delim, returnDelims);
		while (st.hasMoreTokens()) {
			strings.add(st.nextToken());
		}
		return strings;
	}

	/**
	 * ????????? ?????? ? ????????? ????????????.
	 * 
	 * @param delim
	 *            ???????????
	 * @param strings
	 *            ????? ?????
	 */
	public static String join(String delim, Collection strings) {
		StringBuffer output = new StringBuffer();
		for (Iterator i = strings.iterator(); i.hasNext();) {
			output.append(i.next().toString());
			if (i.hasNext())
				output.append(delim);
		}
		return output.toString();
	}

	/**
	 * ???????? ????????? ??????? ?? ?????? ? ??????.
	 * 
	 * @param source
	 * @param oldStr
	 * @param newStr
	 */
	public static String replace(String source, String oldStr, String newStr) {
		StringBuffer output = new StringBuffer(source.length());
		int nl = newStr.length();
		int ol = oldStr.length();
		int start;
		if (ol > 0) {
			String input = source;
			do {
				start = input.indexOf(oldStr);
				if (start < 0)
					output.append(input);
				else {
					output.append(input.substring(0, start));
					if (nl > 0)
						output.append(newStr);
					input = input.substring(start + ol);
				}
			} while (start >= 0);
		} else
			output.append(source);
		return output.toString();
	}

	/**
	 * ???????? ?? ?????? <code>name</code> ?????????? ??? ?????
	 */
	public static boolean isName(String name) {
		try {
			checkName(name);
		} catch (BadNameException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * ???????? ?? ?????? <code>name</code> ?????????? ??? ?????
	 */
	public static boolean isLatinName(String name) {
		try {
			checkLatinName(name);
		} catch (BadNameException e) {
			return false;
		}
		return true;
	}

    public static boolean isLatinOrDigit(char ch) {
        return ('a'<=ch && ch<='z') || ('A'<=ch && ch<='Z') || ('0'<=ch && ch<='9');
    }
    
	/**
	 * ???????? ?? ?????? <code>name</code> ?????????? ??? ?????
	 * 
	 * @throws BadNameException
	 *             ? ????????? ?????? ?????? ?????? ?? ????? ???? ??????
	 */
	public static void checkName(String name) throws BadNameException {
		checkNull(name);
		for (int i = 0; i < name.length(); i++) {
			char letter = name.charAt(i);
			
			if (i == 0 ? !Character.isJavaIdentifierStart(letter): 
						 !Character.isJavaIdentifierPart(letter)) {
				throw new BadNameException("Invalid identificator '" + name + "'", name);
			}
		}
	}
	
	/**
	 * ???????? ?? ?????? <code>name</code> ?????????? ??? ?????
	 * 
	 * @throws BadNameException
	 *             ? ????????? ?????? ?????? ?????? ?? ????? ???? ??????
	 */
	public static void checkLatinName(String name) throws BadNameException {
		checkNull(name);
		for (int i = 0; i < name.length(); i++) {
			char letter = name.charAt(i);
			if (i == 0 ? 
					! isLatinJavaIdentifierStart(letter) : 
					! isLatinJavaIdentifierPart(letter)
				) {
				throw new BadNameException("Invalid identificator '" + name + "'", name);
			}
		}
	}
	
	/**
	 * ???????? ?? ?????? <code>name</code> ??????????????,
	 * ?? ???? ?????????? ? ?? ????? ?????? ??????
	 * 
	 * @throws BadNameException
	 *             ? ????????? ?????? ?????? ?????? ?? ????? ???? ??????
	 */
	public static void checkNull(String name) throws BadNameException {
		if (name == null || name.length() == 0) {
			throw new BadNameException("Empty identificator", "");
		}
	}
	
	/**
	 * ???????? ?? ?????? <code>ch</code> ???????????????,
	 * ??? ??????? ??????? Java ??????????????
	 * ? ???????????? ?????????
	 * 
	 */
	public static boolean isLatinJavaIdentifierStart(char ch) {
		return (Character.isJavaIdentifierStart(ch)&&
				(isLatinOrDigit(ch)||ch=='_'||ch=='$'));
	}
	
	/**
	 * ???????? ?? ?????? <code>ch</code> ???????????????,
	 * ??? ?? ??????? ??????? Java ??????????????
	 * ? ???????????? ?????????
	 * 
	 */
	public static boolean isLatinJavaIdentifierPart(char ch) {
		return (Character.isJavaIdentifierPart(ch)&&
				(isLatinOrDigit(ch)||ch=='_'||ch=='$'));
	}

	/**
	 * ?????? ?????????? ????? ? ?????? ??????????? (?.?. ?????? ????? ? ?????
	 * ????? .?!)
	 * 
	 * @param s
	 *            ???????? ?????
	 * @return ?????????? ?????
	 */
	public static String toSentence(String s) {
		StringBuffer result = new StringBuffer(s.length());
		boolean wasEndOfSentence = true;
		for (int i = 0; i < s.length(); ++i) {
			char ch = s.charAt(i);
			if (ch == '.' || ch == '!' || ch == '?') {
				wasEndOfSentence = true;
			} else if (!isWitespace(ch)) {
				if (wasEndOfSentence) {
					ch = Character.toUpperCase(ch);
					wasEndOfSentence = false;
				}
			}
			result.append(ch);
		}
		return result.toString();
	}

	/**
	 * ?????? ?????? ????? ???? ???? ? ?????? ??????????.
	 * 
	 * @param s
	 *            ???????? ?????
	 * @return ?????????? ?????
	 */
	public static String toCapitals(String s) {
		StringBuffer result = new StringBuffer(s.length());
		boolean wasWhitespace = false;
		for (int i = 0; i < s.length(); ++i) {
			char ch = s.charAt(i);
			if (isWitespace(ch)) {
				wasWhitespace = true;
			} else {
				if (wasWhitespace) {
					ch = Character.toUpperCase(ch);
					wasWhitespace = false;
				}
			}
			result.append(ch);
		}
		return result.toString();
	}


	public static String invertCase(String s) {
		StringBuffer result = new StringBuffer(s.length());
		for (int i = 0; i < s.length(); ++i) {
			char ch = s.charAt(i);
			if (Character.isUpperCase(ch)) {
				ch = Character.toLowerCase(ch);
			} else if (Character.isLowerCase(ch)) {
				ch = Character.toUpperCase(ch);
			}
			result.append(ch);
		}
		return result.toString();
	}
	
	public static String replaceAllSpecials(String string, String param) {
		return string.replaceAll("([,./?\\|;:'\"{}~`!*+-=()<>@#$%^&\\[\\]])", param);
	}

	/**
	 * ?????????, ???????? ?? ?????? ??????????.
	 * 
	 * @param ch
	 *            ??????????? ??????
	 * @return true, ???? ?????? ??????????
	 */
	private static boolean isWitespace(char ch) {
		return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r'
				|| ch == '\u00a0';
	}

	public static boolean isWordDelimiter(char ch) {
		return isWitespace(ch) || ch == ',' || ch == '.' || ch == '/'
				|| ch == '?' || ch == '\\' || ch == '|' || ch == ';'
				|| ch == ':' || ch == '\'' || ch == '\"' || ch == '['
				|| ch == ']' || ch == '{' || ch == '}' || ch == '~'
				|| ch == '`' || ch == '!' || ch == '*' || ch == '+'
				|| ch == '-' || ch == '(' || ch == ')';
	}

	public static String trim(String src, char chr) {
		int len = src.length();
		int st = 0;

		while ((st < len) && (src.charAt(st) == chr)) {
			st++;
		}
		while ((st < len) && (src.charAt(len - 1) == chr)) {
			len--;
		}
		return ((st > 0) || (len < src.length())) ? src.substring(st, len)
				: src;
	}

	public static String convertEntities(String s, String encoding)
			throws UnsupportedEncodingException {
		byte[] b = new byte[1];
		StringBuffer result = new StringBuffer(s.length());
		for (int i = 0; i < s.length(); ++i) {
			char ch = s.charAt(i);
			if (ch == '&') {
				if (i < s.length() - 1) {
					char ch1 = s.charAt(i + 1);
					if (ch1 == '#') {
						int pos = s.indexOf(";", i + 1);
						if (pos > i + 2) {
							String value = s.substring(i + 2, pos);
							char ch2 = value.charAt(0);
							if (value.length() > 0) {
								if (ch2 == 'x' || ch2 == 'X') {
									if (value.length() > 1) {
										b[0] = (byte) Integer.parseInt(value
												.substring(1), 16);
										result.append(new String(b, encoding));
										i = pos;
										continue;
									}
								} else {
									b[0] = (byte) Integer.parseInt(value);
									result.append(new String(b, encoding));
									i = pos;
									continue;
								}
							}
						}
					}
				}
			}
			result.append(ch);
		}
		return result.toString();
	}

	public static String unescape(String s) {
		if (s.length() == 0) {
			return s;
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (ch == '\\') {
				if (i + 1 == s.length()) {
					break;
				}
				char ch1 = s.charAt(++i);
				switch (ch1) {
				case 'n':
					ch = '\n';
					break;
				case 'r':
					ch = '\r';
					break;
				case 't':
					ch = '\t';
					break;
				case '\\':
					ch = '\\';
					break;
				case '"':
					ch = '"';
					break;
				default:
					sb.append(ch);
					ch = ch1;
				}
			}
			sb.append(ch);
		}
		return sb.toString();
	}

	private static DecimalFormat threeDigits = new DecimalFormat("000");

	private static DecimalFormat twoDigits = new DecimalFormat("00");

	public static String formatAge(final long t) {
		final long time = t > 0 ? t : -t;
		StringBuffer result = new StringBuffer();
		if (t < 0)
			result.append("-");
		long secs = time / 1000;
		long millis = time % 1000;
		long mins = secs / 60;
		secs = secs % 60;
		long hours = mins / 60;
		mins = mins % 60;
		long days = hours / 24;
		hours = hours % 24;
		if (days > 0) {
			long years = days / 365;
			if (years > 0) {
				result.append(""+years);
				result.append(" years");
			} else {
				long weeks = days / 7;
				if (weeks > 0) {
					days = days % 7;
					result.append(weeks);
					result.append(" weeks ");
				}
				result.append(""+days);
				result.append(" days");
			}
		} else {
			result.append(twoDigits.format(hours));
			result.append(":");
			result.append(twoDigits.format(mins));
			result.append(":");
			result.append(twoDigits.format(secs));
			result.append(".");
			result.append(threeDigits.format(millis));
		}
		return result.toString();
	}
}

