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

package l2p.commons.math;


/**
 * ?????????? ?? ???????????? ??????????.<br>
 * ??? ???? ?? ?????? Apache commons-math.
 * 
 * @see http://commons.apache.org/math/
 */
public class SafeMath
{
	/**
	 * ???????? ???? ????? ? ????????? ?? ????????????.
	 *
	 * @param a ?????????
	 * @param b ?????????
	 * @return ????? <code>a+b</code>
	 * @throws ArithmeticException ???? ????????? ???????? ???????? ??????
	 */
	public static int addAndCheck(int a, int b) throws ArithmeticException{
		return addAndCheck(a, b, "overflow: add", false);
	}

	/**
	 * ???????? ???? ????? ? ????????? ?? ????????????.
	 *
	 * @param a ?????????
	 * @param b ?????????
	 * @return ????? <code>a+b</code>, ???? ????????? ???????? ??????, ? ?????? ????????????
	 */
	public static int addAndLimit(int a, int b){
		return addAndCheck(a, b, null, true);
	}

	private static int addAndCheck(int a, int b, String msg, boolean limit) {
		int ret;
		if (a > b)
			// use symmetry to reduce boundary cases
			ret = addAndCheck(b, a, msg, limit);
		else if (a < 0) {
			if (b < 0) {
				// check for negative overflow
				if (Integer.MIN_VALUE - b <= a)
					ret = a + b;
				else if (limit)
					ret = Integer.MIN_VALUE;
				else
					throw new ArithmeticException(msg);
			}
			else
				// opposite sign addition is always safe
				ret = a + b;
		}
		else // check for positive overflow
			if (a <= Integer.MAX_VALUE - b)
				ret = a + b;
			else if (limit)
				ret = Integer.MAX_VALUE;
			else
				throw new ArithmeticException(msg);
		return ret;
	}

	/**
	 * ???????? ???? ????? ? ????????? ?? ????????????.
	 *
	 * @param a ?????????
	 * @param b ?????????
	 * @return ????? <code>a+b</code>, ???? ????????? ???????? ??????, ? ?????? ????????????
	 */
	public static long addAndLimit(long a, long b) {
		return addAndCheck(a, b, "overflow: add", true);
	}

	/**
	 * ???????? ???? ????? ? ????????? ?? ????????????.
	 *
	 * @param a ?????????
	 * @param b ?????????
	 * @return ????? <code>a+b</code>
	 * @throws ArithmeticException ???? ????????? ???????? ???????? ??????
	 */
	public static long addAndCheck(long a, long b) throws ArithmeticException {
		return addAndCheck(a, b, "overflow: add", false);
	}

	private static long addAndCheck(long a, long b, String msg, boolean limit) {
		long ret;
		if (a > b)
			// use symmetry to reduce boundary cases
			ret = addAndCheck(b, a, msg, limit);
		else if (a < 0) {
			if (b < 0) {
				// check for negative overflow
				if (Long.MIN_VALUE - b <= a)
					ret = a + b;
				else if (limit)
					ret = Long.MIN_VALUE;
				else
					throw new ArithmeticException(msg);
			}
			else
				// opposite sign addition is always safe
				ret = a + b;
		}
		else // check for positive overflow
			if (a <= Long.MAX_VALUE - b)
				ret = a + b;
			else if (limit)
				ret = Long.MAX_VALUE;
			else
				throw new ArithmeticException(msg);
		return ret;
	}

	/**
	 * ????????? ???? ????? ? ????????? ?? ????????????.
	 *
	 * @param a ?????????
	 * @param b ?????????
	 * @return ???????????? <code>a*b</code>
	 * @throws ArithmeticException ???? ????????? ???????? ???????? ??????
	 */
	public static int mulAndCheck(int a, int b)  throws ArithmeticException
	{
		return mulAndCheck(a, b, "overflow: mul", false);
	}

	/**
	 * ????????? ???? ????? ? ????????? ?? ????????????.
	 *
	 * @param a ?????????
	 * @param b ?????????
	 * @return ???????????? <code>a*b</code>, ???? ????????? ???????? ??????, ? ?????? ????????????
	 */
	public static int mulAndLimit(int a, int b)
	{
		return mulAndCheck(a, b, "overflow: mul", true);
	}

	private static int mulAndCheck(int a, int b, String msg, boolean limit) {
		int ret;
		if (a > b)
			// use symmetry to reduce boundary cases
			ret = mulAndCheck(b, a, msg, limit);
		else if (a < 0) {
			if (b < 0) {
				// check for positive overflow with negative a, negative b
				if (a >= Integer.MAX_VALUE / b)
					ret = a * b;
				else if(limit)
					ret = Integer.MAX_VALUE;
				else
					throw new ArithmeticException(msg);
			} else if (b > 0) {
				// check for negative overflow with negative a, positive b
				if (Integer.MIN_VALUE / b <= a)
					ret = a * b;
				else if(limit)
					ret = Integer.MIN_VALUE;
				else
					throw new ArithmeticException(msg);
			}
			else
				ret = 0;
		} else if (a > 0) {
			// check for positive overflow with positive a, positive b
			if (a <= Integer.MAX_VALUE / b)
				ret = a * b;
			else if(limit)
				ret = Integer.MAX_VALUE;
			else
				throw new ArithmeticException(msg);
		}
		else
			ret = 0;
		return ret;
	}

	/**
	 * ????????? ???? ????? ? ????????? ?? ????????????.
	 *
	 * @param a ?????????
	 * @param b ?????????
	 * @return ???????????? <code>a*b</code>
	 * @throws ArithmeticException ???? ????????? ???????? ???????? ??????
	 */
	public static long mulAndCheck(long a, long b)  throws ArithmeticException
	{
		return mulAndCheck(a, b, "overflow: mul", false);
	}

	/**
	 * ????????? ???? ????? ? ????????? ?? ????????????.
	 *
	 * @param a ?????????
	 * @param b ?????????
	 * @return ???????????? <code>a*b</code>, ???? ????????? ???????? ??????, ? ?????? ????????????
	 */
	public static long mulAndLimit(long a, long b)
	{
		return mulAndCheck(a, b, "overflow: mul", true);
	}

	private static long mulAndCheck(long a, long b, String msg, boolean limit) {
		long ret;
		if (a > b)
			// use symmetry to reduce boundary cases
			ret = mulAndCheck(b, a, msg, limit);
		else if (a < 0) {
			if (b < 0) {
				// check for positive overflow with negative a, negative b
				if (a >= Long.MAX_VALUE / b)
					ret = a * b;
				else if(limit)
					ret = Long.MAX_VALUE;
				else
					throw new ArithmeticException(msg);
			} else if (b > 0) {
				// check for negative overflow with negative a, positive b
				if (Long.MIN_VALUE / b <= a)
					ret = a * b;
				else if(limit)
					ret = Long.MIN_VALUE;
				else
					throw new ArithmeticException(msg);
			}
			else
				ret = 0;
		} else if (a > 0) {
			// check for positive overflow with positive a, positive b
			if (a <= Long.MAX_VALUE / b)
				ret = a * b;
			else if(limit)
				ret = Long.MAX_VALUE;
			else
				throw new ArithmeticException(msg);
		}
		else
			ret = 0;
		return ret;
	}

}
