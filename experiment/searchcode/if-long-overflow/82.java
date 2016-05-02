/*
 * Copyright (C) 2011-2013
 * EvilTeam
 * http://evildev.su
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package l2p.gameserver.utils;

import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.model.reward.RewardList;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


@Slf4j
public class Util
{
	static final String PATTERN = "0.0000000000E00";
	static final DecimalFormat df;

	/**
	 * ????????? ??? ?????.<br>
	 * Locale.KOREA ?????????? ??? ?????????????? ????? ",".<br>
	 * Locale.FRANCE ??????????? ????? " "<br>
	 * ??? ?????????????? ????? "." ?????? ? ?????????? Locale.FRANCE
	 */
	private static NumberFormat adenaFormatter;

	static
	{
		adenaFormatter = NumberFormat.getIntegerInstance(Locale.FRANCE);
		df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
		df.applyPattern(PATTERN);
		df.setPositivePrefix("+");
	}

	/**
	 * ????????? ?????? ?? ??????????? ??????????? ?????????
	 * @param text ??????-????????
	 * @param template ?????? ??? ??????
	 * @return true ? ?????? ?????????? ?????? ???????
	 */
	public static boolean isMatchingRegexp(String text, String template)
	{
		Pattern pattern = null;
		try
		{
			pattern = Pattern.compile(template);
		}
		catch(PatternSyntaxException e) // invalid template
		{
			log.error("", e);
		}
		if(pattern == null)
			return false;
		Matcher regexp = pattern.matcher(text);
		return regexp.matches();
	}

	public static String formatDouble(double x, String nanString, boolean forceExponents)
	{
		if(Double.isNaN(x))
			return nanString;
		if(forceExponents)
			return df.format(x);
		if((long) x == x)
			return String.valueOf((long) x);
		return String.valueOf(x);
	}

	/**
	 * Return amount of adena formatted with " " delimiter
	 * @param amount
	 * @return String formatted adena amount
	 */
	public static String formatAdena(long amount)
	{
		return adenaFormatter.format(amount);
	}

	/**
	 * ??????????? ????? ? ???????? ? ???/????/??????/???????
	 */
	public static String formatTime(int time)
	{
		if(time == 0)
			return "now";
		time = Math.abs(time);
		String ret = "";
		long numDays = time / 86400;
		time -= numDays * 86400;
		long numHours = time / 3600;
		time -= numHours * 3600;
		long numMins = time / 60;
		time -= numMins * 60;
		long numSeconds = time;
		if(numDays > 0)
			ret += numDays + "d ";
		if(numHours > 0)
			ret += numHours + "h ";
		if(numMins > 0)
			ret += numMins + "m ";
		if(numSeconds > 0)
			ret += numSeconds + "s";
		return ret.trim();
	}

	/**
	 * ?????????? ??? ???????? ???????? ????? ? ?????? ??????.
	 * ?????????? 0 ???? ???? ?? ??????, ???? ?????????? ???? ??????.
	 * ????????? ???????????? ????? ??????????? 100%.
	 * ???? ? 1:1000000 (L2Drop.MAX_CHANCE)
	 */
	public static long rollDrop(long min, long max, double calcChance, boolean rate)
	{
		if(calcChance <= 0 || min <= 0 || max <= 0)
			return 0;
		int dropmult = 1;
		if(rate)
			calcChance *= Config.RATE_DROP_ITEMS;
		if(calcChance > RewardList.MAX_CHANCE)
			if(calcChance % RewardList.MAX_CHANCE == 0) // ???? ?????? 100% ?? ???? ???????? ??????????
				dropmult = (int) (calcChance / RewardList.MAX_CHANCE);
			else
			{
				dropmult = (int) Math.ceil(calcChance / RewardList.MAX_CHANCE); // ????????? ????? ???? / 100% ?????????? ?????
				calcChance = calcChance / dropmult; // ???? ????? ???? / ?????????
			}
		return Rnd.chance(calcChance / 10000.) ? Rnd.get(min * dropmult, max * dropmult) : 0;
	}

	public static int packInt(int[] a, int bits) throws Exception
	{
		int m = 32 / bits;
		if(a.length > m)
			throw new Exception("Overflow");

		int result = 0;
		int next;
		int mval = (int) Math.pow(2, bits);
		for(int i = 0; i < m; i++)
		{
			result <<= bits;
			if(a.length > i)
			{
				next = a[i];
				if(next >= mval || next < 0)
					throw new Exception("Overload, value is out of range");
			}
			else
				next = 0;
			result += next;
		}
		return result;
	}

	public static long packLong(int[] a, int bits) throws Exception
	{
		int m = 64 / bits;
		if(a.length > m)
			throw new Exception("Overflow");

		long result = 0;
		int next;
		int mval = (int) Math.pow(2, bits);
		for(int i = 0; i < m; i++)
		{
			result <<= bits;
			if(a.length > i)
			{
				next = a[i];
				if(next >= mval || next < 0)
					throw new Exception("Overload, value is out of range");
			}
			else
				next = 0;
			result += next;
		}
		return result;
	}

	public static int[] unpackInt(int a, int bits)
	{
		int m = 32 / bits;
		int mval = (int) Math.pow(2, bits);
		int[] result = new int[m];
		int next;
		for(int i = m; i > 0; i--)
		{
			next = a;
			a = a >> bits;
		result[i - 1] = next - a * mval;
		}
		return result;
	}

	public static int[] unpackLong(long a, int bits)
	{
		int m = 64 / bits;
		int mval = (int) Math.pow(2, bits);
		int[] result = new int[m];
		long next;
		for(int i = m; i > 0; i--)
		{
			next = a;
			a = a >> bits;
		result[i - 1] = (int) (next - a * mval);
		}
		return result;
	}

	/** Just alias */
	public static String joinStrings(String glueStr, String[] strings, int startIdx, int maxCount)
	{
		return Strings.joinStrings(glueStr, strings, startIdx, maxCount);
	}

	/** Just alias */
	public static String joinStrings(String glueStr, String[] strings, int startIdx)
	{
		return Strings.joinStrings(glueStr, strings, startIdx, -1);
	}

	public static boolean isNumber(String s)
	{
		try
		{
			Double.parseDouble(s);
		}
		catch(NumberFormatException e)
		{
			return false;
		}
		return true;
	}

	public static String dumpObject(Object o, boolean simpleTypes, boolean parentFields, boolean ignoreStatics)
	{
		Class<?> cls = o.getClass();
		String val, type, result = "[" + (simpleTypes ? cls.getSimpleName() : cls.getName()) + "\n";
		Object fldObj;
		List<Field> fields = new ArrayList<Field>();
		while(cls != null)
		{
			for(Field fld : cls.getDeclaredFields())
				if(!fields.contains(fld))
				{
					if(ignoreStatics && Modifier.isStatic(fld.getModifiers()))
						continue;
					fields.add(fld);
				}
			cls = cls.getSuperclass();
			if(!parentFields)
				break;
		}

		for(Field fld : fields)
		{
			fld.setAccessible(true);
			try
			{
				fldObj = fld.get(o);
				if(fldObj == null)
					val = "NULL";
				else
					val = fldObj.toString();
			}
			catch(Throwable e)
			{
				log.error("", e);
				val = "<ERROR>";
			}
			type = simpleTypes ? fld.getType().getSimpleName() : fld.getType().toString();

			result += String.format("\t%s [%s] = %s;\n", fld.getName(), type, val);
		}

		result += "]\n";
		return result;
	}

	private static Pattern _pattern = Pattern.compile("<!--TEMPLET(\\d+)(.*?)TEMPLET-->", Pattern.DOTALL);

	public static HashMap<Integer, String> parseTemplate(String html)
	{
		Matcher m = _pattern.matcher(html);
		HashMap<Integer, String> tpls = new HashMap<Integer, String>();
		while(m.find())
		{
			tpls.put(Integer.parseInt(m.group(1)), m.group(2));
			html = html.replace(m.group(0), "");
		}

		tpls.put(0, html);
		return tpls;
	}
}
