package org.loon.framework.game.simple.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * Copyright 2008 - 2009
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @project loonframework
 * @author chenpeng
 * @email?ceponline@yahoo.com.cn
 * @version 0.1
 */
final public class NumberUtils {

	private static final int DEF_DIV_SCALE = 10;

	/**
	 * ???????unsigned int
	 * 
	 * @param maxInt
	 * @param doNotInclude1
	 * @param doNotInclude2
	 * @return
	 */
	public static int getRandomUnsignedInt(int maxInt, int doNotInclude1,
			int doNotInclude2) {
		int n = 2;
		if (doNotInclude1 == doNotInclude2) {
			doNotInclude2 = maxInt + 1;
		}
		if (doNotInclude1 > doNotInclude2) {
			n = doNotInclude2;
			doNotInclude2 = doNotInclude1;
			doNotInclude1 = n;
			n = 2;
		}
		if (doNotInclude1 < 0) {
			doNotInclude1 = maxInt + 1;
		}
		if (doNotInclude2 < 0) {
			doNotInclude2 = maxInt + 1;
		}
		if (doNotInclude1 > maxInt) {
			n--;
		}
		if (doNotInclude2 > maxInt) {
			n--;
		}
		int val = (int) Math.floor(Math.random()
				* ((double) maxInt - (double) n));
		if (val >= doNotInclude1) {
			val++;
		}
		if (val >= doNotInclude2) {
			val++;
		}
		return val;
	}

	/**
	 * ???????unsigned int
	 * 
	 * @param maxInt
	 * @param doNotInclude
	 * @return
	 */
	public static int getRandomUnsignedInt(int maxInt, int doNotInclude) {
		int val = 0;
		if (doNotInclude > -1 && doNotInclude <= maxInt) {
			val = (int) Math.floor(Math.random() * ((double) maxInt - 1.0D));
			if (val >= doNotInclude) {
				val++;
			}
		} else {
			val = (int) Math.floor(Math.random() * (double) maxInt);
		}
		return val;
	}

	/**
	 * ???????unsigned int
	 * 
	 * @param maxInt
	 * @return
	 */
	public static int getRandomUnsignedInt(int maxInt) {
		return getRandomUnsignedInt(maxInt, -1);
	}

	/**
	 * ???????
	 * 
	 * @param num1
	 * @param num2
	 * @return
	 */
	public static int getRandomInt(int num1, int num2) {
		int result = 0;
		if (num2 > -1 && num2 <= num1) {
			result = (int) Math.floor(Math.random() * ((double) num1 - 1.0D));
			if (result >= num2) {
				result++;
			}
		} else {
			result = (int) Math.floor(Math.random() * (double) num1);
		}
		return result;
	}

	/**
	 * ???
	 * 
	 * @param i
	 * @param min
	 * @param max
	 * @return
	 */
	public static int mid(int i, int min, int max) {
		return Math.max(i, Math.min(min, max));
	}

	final static private String[] zeros = { "", "0", "00", "000", "0000",
			"00000", "000000", "0000000", "00000000", "000000000", "0000000000" };

	/**
	 * ?????????
	 * 
	 * @param number
	 * @param numDigits
	 * @return
	 */
	public static String addZeros(long number, int numDigits) {
		return addZeros(String.valueOf(number), numDigits);
	}

	/**
	 * ?????????
	 * 
	 * @param number
	 * @param numDigits
	 * @return
	 */
	public static String addZeros(String number, int numDigits) {
		int length = numDigits - number.length();
		if (length != 0) {
			number = zeros[length] + number;
		}
		return number;
	}

	/**
	 * ???????
	 * 
	 * @param param
	 * @return
	 */
	public static boolean isNan(String param) {
		boolean result = false;
		if (param == null || "".equals(param)) {
			return result;
		}
		param = param.replace('d', '_').replace('f', '_');
		try {
			Double test = new Double(param);
			test.intValue();
			result = true;
		} catch (NumberFormatException ex) {
			return result;
		}
		return result;
	}

	/**
	 * ??????????
	 * 
	 * @param val
	 * @return
	 */
	public static boolean isEmpty(int val) {
		return (val == Integer.MIN_VALUE) ? true : 0 == val;
	}

	/**
	 * ?????????????
	 * 
	 * @param val
	 * @return
	 */
	public static boolean isEmpty(String val) {
		return (val == null | "".equals(val) | val.equals(Integer
				.toString(Integer.MAX_VALUE)));
	}

	/**
	 * ?????????
	 * 
	 * @param value
	 * @return
	 */
	public static long doNanRev(Long value) {
		String values = String.valueOf(value);
		if (values.length() == 1) {
			return Integer.parseInt(values);
		}
		String result = values.substring(values.length() - 1, values.length());
		result += doNanRev(Long.valueOf(values
				.substring(0, values.length() - 1)));
		return Integer.parseInt(result);
	}

	/**
	 * ?????????
	 * 
	 * @param pf
	 * @return
	 */
	public static int doFactorial(int pf) {
		if (pf == 1) {
			return 1;
		}
		return (doFactorial(pf - 1) * pf);
	}

	/**
	 * ???????,?????????long,?token????
	 * 
	 * @param input
	 * @param token
	 * @return long[]
	 */
	public static long[] toLongDivide(String input, String token) {
		String str[] = new String[0];

		StringTokenizer val = new StringTokenizer(input, token);
		int i = 0;
		int count = val.countTokens();
		str = new String[count];
		long valLong[] = new long[count];
		while (val.hasMoreTokens()) {
			str[i] = val.nextToken().trim();
			valLong[i] = Long.parseLong(str[i]);
			i++;
		}
		if (valLong == null)
			valLong = new long[0];
		return valLong;
	}

	/**
	 * ??double??????????????
	 * 
	 * @param d
	 * @return
	 */
	public static String toDoubOriginOutPut(double d, int digitally) {
		DecimalFormat df = null;
		String dig = null;
		digitally = (digitally == 0) ? digitally++ : digitally;
		try {
			df = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		} catch (ClassCastException e) {
			System.err.println(e);
		}
		switch (digitally) {
		case 1:
			dig = "####0.0";
			break;
		case 2:
			dig = "####0.00";
			break;
		case 3:
			dig = "###,##0.000";
			break;
		case 4:
			dig = "####0.0000";
			break;
		default:
			dig = "####0.00";
		}
		df.applyPattern(dig);

		return df.format(d);
	}

	/**
	 * ?????????????????int
	 * 
	 * @param input
	 *            ??????????
	 * @param token
	 *            ???
	 * @return ????
	 */
	public static int[] toIntegerDivide(String input, String token) {
		if (input == null && token == null)
			return new int[0];
		String str[] = new String[0];
		StringTokenizer val = new StringTokenizer(input, token);
		int count = val.countTokens();
		str = new String[count];
		int valLong[] = new int[count];
		int i = 0;
		while (val.hasMoreTokens()) {
			str[i] = val.nextToken();
			valLong[i] = Integer.parseInt(str[i]);
			i++;
		}
		if (valLong == null)
			valLong = new int[0];
		return valLong;
	}

	/**
	 * ????????????
	 * 
	 * @param divisor
	 * @param dividend
	 * @return
	 */
	public static double toPercent(long divisor, long dividend) {
		if (divisor == 0 || dividend == 0) {
			return 0d;
		}
		double cd = divisor * 1d;
		double pd = dividend * 1d;

		return (Math.round(cd / pd * 10000) * 1d) / 100;
	}

	/**
	 * ????????????
	 * 
	 * @param size
	 * @return
	 */
	public static int toRandom(int size) {
		Random rad = new Random();
		rad.setSeed(System.currentTimeMillis());
		return Math.abs(rad.nextInt()) % size;
	}

	/**
	 * ?????????
	 * 
	 * @param length
	 *            ????????????????????
	 * @param size
	 *            ??????????
	 * @return
	 */
	public static int[] toRamdom(int length, int size) {
		if (length > size)
			length = size;
		ArrayList arraylist = new ArrayList();
		boolean flag = true;
		int[] result = new int[0];
		while (flag) {
			Integer temp = new Integer(toRandom(size));
			if (!arraylist.contains(temp)) {
				arraylist.add(temp);
			}
			if (arraylist.size() == length) {
				flag = false;
			}
		}
		Integer[] temp = (Integer[]) arraylist.toArray(new Integer[0]);
		result = new int[temp.length];
		for (int i = 0; i < temp.length; i++) {
			result[i] = temp[i].intValue();
		}
		return result;
	}

	/**
	 * ??100%??????????
	 * 
	 * @param maxValue
	 * @param minusValue
	 * @return
	 */
	public static float minusPercent(float maxValue, float minusValue) {
		return 100 - ((minusValue / maxValue) * 100);
	}

	/**
	 * ??100%????????
	 * 
	 * @param maxValue
	 * @param minusValue
	 * @return
	 */
	public static float percent(float maxValue, float minValue) {
		return (minValue / maxValue) * 100;
	}

	/**
	 * ?value???????????
	 * 
	 * @param value
	 * @param type
	 *            1:???? 2?????
	 * 
	 * @return
	 */
	public static String toConvertCnNumber(long value, int type) {
		String[] chNumber = { "?", "?", "?", "?", "?", "?", "?", "?", "?", "?" };
		String[] digit = { "", "?", "?", "?", "?", "?", "?", "?" };
		switch (type) {
		case 1:
			String[] capsCNumber = { "?", "?", "?", "?", "?", "?", "?", "?",
					"?", "?" };
			chNumber = capsCNumber;
		case 2:
			String[] minCNumber = { "?", "?", "?", "?", "?", "?", "?", "?",
					"?", "?" };
			chNumber = minCNumber;
		}
		String retStr = "";

		String inputStr = Long.toString(value);
		for (int i = inputStr.length(); i > 0; i--) {
			char ch = inputStr.charAt(i - 1);
			if (ch != '0') {

				retStr = chNumber[ch - '0'] + digit[inputStr.length() - i]
						+ retStr;
			} else {
				if (inputStr.length() - i == 4)
					retStr = "??" + retStr;
				else
					retStr = "?" + retStr;
			}
		}

		int pos = retStr.indexOf("??");
		while (pos >= 0) {
			retStr = retStr.replaceAll("??", "?");
			pos = retStr.indexOf("??");
		}

		retStr = retStr.replaceAll("??", "?");

		return retStr;
	}

	/**
	 * ?????????????????????[0]????????????
	 * 
	 * @param curValue
	 * @param length
	 * @return
	 */
	public static String toFormatNumber(int curValue, int length) {
		String tmpValue = Integer.toString(curValue);
		if (tmpValue.length() > length) {
			tmpValue = tmpValue.substring(tmpValue.length() - length);
		} else {
			int loop = length - tmpValue.length();
			for (int i = 0; i < loop; i++) {
				tmpValue = "0" + tmpValue;
			}
		}
		return tmpValue;
	}

	/**
	 * ??????????
	 * 
	 * @param v1
	 *            ???
	 * @param v2
	 *            ??
	 * @return ??????
	 */

	public static double add(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.add(b2).doubleValue();
	}

	/**
	 * ??????????
	 * 
	 * @param v1
	 *            ???
	 * @param v2
	 *            ??
	 * @return ??????
	 */

	public static double sub(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.subtract(b2).doubleValue();
	}

	/**
	 * ??????????
	 * 
	 * @param v1
	 *            ???
	 * @param v2
	 *            ??
	 * @return ??????
	 */

	public static double mul(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.multiply(b2).doubleValue();
	}

	/**
	 * ???????????????????????????? ?????10????????????
	 * 
	 * @param v1
	 *            ???
	 * @param v2
	 *            ??
	 * @return ??????
	 */

	public static double div(double v1, double v2) {
		return div(v1, v2, DEF_DIV_SCALE);
	}

	/**
	 * ??????????????????????????scale??? ??????????????
	 * 
	 * @param v1
	 *            ???
	 * @param v2
	 *            ??
	 * @param scale
	 *            ?????????????????
	 * @return ??????
	 */

	public static double div(double v1, double v2, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException(
					"The scale must be a positive integer or zero");
		}

		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * ???????????????
	 * 
	 * @param v
	 *            ?????????
	 * @param scale
	 *            ????????
	 * @return ????????
	 */

	public static double round(double v, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException(
					"The scale must be a positive integer or zero");
		}

		BigDecimal b = new BigDecimal(Double.toString(v));
		BigDecimal one = new BigDecimal("1");
		return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static boolean isIntegralNumber(String value) {
		if (isNull(value))
			return false;
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException ne) {
			return false;
		}
	}

	public static boolean isNumber(String value) {
		if (isNull(value))
			return false;
		try {
			Double.parseDouble(value);
			return true;
		} catch (NumberFormatException ne) {
			return false;
		}
	}

	public static boolean isZero(String value) {
		try {
			double zero = Double.parseDouble(value);
			return zero == 0.0D;
		} catch (NumberFormatException ne) {
			return false;
		}
	}

	public static boolean isNumber(String value1, String value2) {
		return isNumber(value1) && isNumber(value2);
	}

	public static boolean isValues(int value, int values[]) {
		for (int i = 0; i < values.length; i++)
			if (value == values[i])
				return true;

		return false;
	}

	public static boolean isNull(String value) {
		return value == null;
	}

	public static boolean isNull(String value1, String value2) {
		if (value1 == null)
			return true;
		return value2 == null;
	}

	public static int parseInt(String value) {
		return parseInt(value, 0);
	}

	public static int parseInt(String value, int defaultValue) {
		if (isNull(value))
			return defaultValue;
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

}

