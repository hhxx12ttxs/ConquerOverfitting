package vjo.java.sun.misc;

/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
//Jad home page: http://www.kpdus.com/jad.html
//Decompiler options: packimports(3) radix(10) lradix(10) 
//Source File Name:   FormattedFloatingDecimal.java

import vjo.java.lang.* ;

import vjo.java.util.regex.Matcher;
import vjo.java.util.regex.Pattern;

//Referenced classes of package sun.misc:
//         FDBigInt, FpUtils

public class FormattedFloatingDecimal {
	public static final class Form extends Enum {

		public static final Form[] values() {
			return (Form[]) $VALUES.clone();
		}

		public static Form valueOf(String s) {
			return (Form) Enum.valueOf(sun / misc
					/ FormattedFloatingDecimal$Form, s);
		}

		public static final Form SCIENTIFIC;

		public static final Form COMPATIBLE;

		public static final Form DECIMAL_FLOAT;

		public static final Form GENERAL;

		private static final Form $VALUES[];

		static {
			SCIENTIFIC = new Form("SCIENTIFIC", 0);
			COMPATIBLE = new Form("COMPATIBLE", 1);
			DECIMAL_FLOAT = new Form("DECIMAL_FLOAT", 2);
			GENERAL = new Form("GENERAL", 3);
			$VALUES = (new Form[] { SCIENTIFIC, COMPATIBLE, DECIMAL_FLOAT,
					GENERAL });
		}

		private Form(String s, int i) {
			super(s, i);
		}
	}

	private FormattedFloatingDecimal(boolean flag, int i, char ac[], int j,
			boolean flag1, int k, Form form1) {
		mustSetRoundDir = false;
		fromHex = false;
		roundDir = 0;
		isNegative = flag;
		isExceptional = flag1;
		decExponent = i;
		digits = ac;
		nDigits = j;
		precision = k;
		form = form1;
	}

	private static int countBits(long l) {
		if (l == 0L)
			return 0;
		for (; (l & -72057594037927936L) == 0L; l <<= 8)
			;
		for (; l > 0L; l <<= 1)
			;
		int i;
		for (i = 0; (l & 72057594037927935L) != 0L; i += 8)
			l <<= 8;

		while (l != 0L) {
			l <<= 1;
			i++;
		}
		return i;
	}

	private static synchronized FDBigInt big5pow(int i) {
		if (!$assertionsDisabled && i < 0)
			throw new AssertionError(i);
		if (b5p == null)
			b5p = new FDBigInt[i + 1];
		else if (b5p.length <= i) {
			FDBigInt afdbigint[] = new FDBigInt[i + 1];
			System.arraycopy(b5p, 0, afdbigint, 0, b5p.length);
			b5p = afdbigint;
		}
		if (b5p[i] != null)
			return b5p[i];
		if (i < small5pow.length)
			return b5p[i] = new FDBigInt(small5pow[i]);
		if (i < long5pow.length)
			return b5p[i] = new FDBigInt(long5pow[i]);
		int j = i >> 1;
		int k = i - j;
		FDBigInt fdbigint = b5p[j];
		if (fdbigint == null)
			fdbigint = big5pow(j);
		if (k < small5pow.length)
			return b5p[i] = fdbigint.mult(small5pow[k]);
		FDBigInt fdbigint1 = b5p[k];
		if (fdbigint1 == null)
			fdbigint1 = big5pow(k);
		return b5p[i] = fdbigint.mult(fdbigint1);
	}

	private static FDBigInt multPow52(FDBigInt fdbigint, int i, int j) {
		if (i != 0)
			if (i < small5pow.length)
				fdbigint = fdbigint.mult(small5pow[i]);
			else
				fdbigint = fdbigint.mult(big5pow(i));
		if (j != 0)
			fdbigint.lshiftMe(j);
		return fdbigint;
	}

	private static FDBigInt constructPow52(int i, int j) {
		FDBigInt fdbigint = new FDBigInt(big5pow(i));
		if (j != 0)
			fdbigint.lshiftMe(j);
		return fdbigint;
	}

	private FDBigInt doubleToBigInt(double d) {
		long l = Double.doubleToLongBits(d) & 9223372036854775807L;
		int i = (int) (l >>> 52);
		l &= 4503599627370495L;
		if (i > 0) {
			l |= 4503599627370496L;
		} else {
			if (!$assertionsDisabled && l == 0L)
				throw new AssertionError(l);
			for (i++; (l & 4503599627370496L) == 0L; i--)
				l <<= 1;

		}
		i -= 1023;
		int j = countBits(l);
		int k = 53 - j;
		l >>>= k;
		bigIntExp = (i + 1) - j;
		bigIntNBits = j;
		return new FDBigInt(l);
	}

	private static double ulp(double d, boolean flag) {
		long l = Double.doubleToLongBits(d) & 9223372036854775807L;
		int i = (int) (l >>> 52);
		if (flag && i >= 52 && (l & 4503599627370495L) == 0L)
			i--;
		double d1;
		if (i > 52)
			d1 = Double.longBitsToDouble((long) (i - 52) << 52);
		else if (i == 0)
			d1 = 4.9406564584124654E-324D;
		else
			d1 = Double.longBitsToDouble(1L << i - 1);
		if (flag)
			d1 = -d1;
		return d1;
	}

	float stickyRound(double d) {
		long l = Double.doubleToLongBits(d);
		long l1 = l & 9218868437227405312L;
		if (l1 == 0L || l1 == 9218868437227405312L) {
			return (float) d;
		} else {
			l += roundDir;
			return (float) Double.longBitsToDouble(l);
		}
	}

	private void developLongDigits(int i, long l, long l1) {
		int k1;
		for (k1 = 0; l1 >= 10L; k1++)
			l1 /= 10L;

		if (k1 != 0) {
			long l2 = long5pow[k1] << k1;
			long l3 = l % l2;
			l /= l2;
			i += k1;
			if (l3 >= l2 >> 1)
				l++;
		}
		char ac[];
		int j;
		int k;
		if (l <= 2147483647L) {
			if (!$assertionsDisabled && l <= 0L)
				throw new AssertionError(l);
			int i2 = (int) l;
			j = 10;
			ac = (char[]) (char[]) perThreadBuffer.get();
			k = j - 1;
			int i1 = i2 % 10;
			for (i2 /= 10; i1 == 0; i2 /= 10) {
				i++;
				i1 = i2 % 10;
			}

			for (; i2 != 0; i2 /= 10) {
				ac[k--] = (char) (i1 + 48);
				i++;
				i1 = i2 % 10;
			}

			ac[k] = (char) (i1 + 48);
		} else {
			j = 20;
			ac = (char[]) (char[]) perThreadBuffer.get();
			k = j - 1;
			int j1 = (int) (l % 10L);
			for (l /= 10L; j1 == 0; l /= 10L) {
				i++;
				j1 = (int) (l % 10L);
			}

			for (; l != 0L; l /= 10L) {
				ac[k--] = (char) (j1 + 48);
				i++;
				j1 = (int) (l % 10L);
			}

			ac[k] = (char) (j1 + 48);
		}
		j -= k;
		char ac1[] = new char[j];
		System.arraycopy(ac, k, ac1, 0, j);
		digits = ac1;
		decExponent = i + 1;
		nDigits = j;
	}

	private void roundup() {
		int i;
		char c = digits[i = nDigits - 1];
		if (c == '9') {
			for (; c == '9' && i > 0; c = digits[--i])
				digits[i] = '0';

			if (c == '9') {
				decExponent++;
				digits[0] = '1';
				return;
			}
		}
		digits[i] = (char) (c + 1);
	}

	private int checkExponent(int i) {
		if (i >= nDigits || i < 0)
			return decExponent;
		for (int j = 0; j < i; j++)
			if (digits[j] != '9')
				return decExponent;

		return decExponent + (digits[i] < '5' ? 0 : 1);
	}

	private char[] applyPrecision(int i) {
		char ac[] = new char[nDigits];
		for (int j = 0; j < ac.length; j++)
			ac[j] = '0';

		if (i >= nDigits || i < 0) {
			System.arraycopy(digits, 0, ac, 0, nDigits);
			return ac;
		}
		if (i == 0) {
			if (digits[0] >= '5')
				ac[0] = '1';
			return ac;
		}
		int k = i;
		char c = digits[k];
		if (c >= '5' && k > 0) {
			char c1 = digits[--k];
			if (c1 == '9') {
				for (; c1 == '9' && k > 0; c1 = digits[--k])
					;
				if (c1 == '9') {
					ac[0] = '1';
					return ac;
				}
			}
			ac[k] = (char) (c1 + 1);
		}
		while (--k >= 0)
			ac[k] = digits[k];
		return ac;
	}

	public FormattedFloatingDecimal(double d) {
		this(d, 2147483647, Form.COMPATIBLE);
	}

	public FormattedFloatingDecimal(double d, int i, Form form1) {
		mustSetRoundDir = false;
		fromHex = false;
		roundDir = 0;
		long l = Double.doubleToLongBits(d);
		precision = i;
		form = form1;
		if ((l & -9223372036854775808L) != 0L) {
			isNegative = true;
			l ^= -9223372036854775808L;
		} else {
			isNegative = false;
		}
		int j = (int) ((l & 9218868437227405312L) >> 52);
		long l1 = l & 4503599627370495L;
		if (j == 2047) {
			isExceptional = true;
			if (l1 == 0L) {
				digits = infinity;
			} else {
				digits = notANumber;
				isNegative = false;
			}
			nDigits = digits.length;
			return;
		}
		isExceptional = false;
		int k;
		if (j == 0) {
			if (l1 == 0L) {
				decExponent = 0;
				digits = zero;
				nDigits = 1;
				return;
			}
			while ((l1 & 4503599627370496L) == 0L) {
				l1 <<= 1;
				j--;
			}
			k = 52 + j + 1;
			j++;
		} else {
			l1 |= 4503599627370496L;
			k = 53;
		}
		j -= 1023;
		dtoa(j, l1, k);
	}

	public FormattedFloatingDecimal(float f) {
		this(f, 2147483647, Form.COMPATIBLE);
	}

	public FormattedFloatingDecimal(float f, int i, Form form1) {
		mustSetRoundDir = false;
		fromHex = false;
		roundDir = 0;
		int j = Float.floatToIntBits(f);
		precision = i;
		form = form1;
		if ((j & -2147483648) != 0) {
			isNegative = true;
			j ^= -2147483648;
		} else {
			isNegative = false;
		}
		int l = (j & 2139095040) >> 23;
		int k = j & 8388607;
		if (l == 255) {
			isExceptional = true;
			if ((long) k == 0L) {
				digits = infinity;
			} else {
				digits = notANumber;
				isNegative = false;
			}
			nDigits = digits.length;
			return;
		}
		isExceptional = false;
		int i1;
		if (l == 0) {
			if (k == 0) {
				decExponent = 0;
				digits = zero;
				nDigits = 1;
				return;
			}
			while ((k & 8388608) == 0) {
				k <<= 1;
				l--;
			}
			i1 = 23 + l + 1;
			l++;
		} else {
			k |= 8388608;
			i1 = 24;
		}
		l -= 127;
		dtoa(l, (long) k << 29, i1);
	}

	private void dtoa(int i, long l, int j) {
		int k = countBits(l);
		int i1 = Math.max(0, k - i - 1);
		if (i <= 62 && i >= -21 && i1 < long5pow.length && k + n5bits[i1] < 64
				&& i1 == 0) {
			long l1;
			if (i > j)
				l1 = 1L << i - j - 1;
			else
				l1 = 0L;
			if (i >= 52)
				l <<= i - 52;
			else
				l >>>= 52 - i;
			developLongDigits(0, l, l1);
			return;
		}
		double d = Double.longBitsToDouble(4607182418800017408L | l
				& -4503599627370497L);
		int j1 = (int) Math.floor((d - 1.5D) * 0.28952965400000003D
				+ 0.176091259D + (double) i * 0.30102999566398098D);
		int i2 = Math.max(0, -j1);
		int k1 = i2 + i1 + i;
		int k2 = Math.max(0, j1);
		int j2 = k2 + i1;
		int i3 = i2;
		int l2 = k1 - j;
		l >>>= 53 - k;
		k1 -= k - 1;
		int l3 = Math.min(k1, j2);
		k1 -= l3;
		j2 -= l3;
		l2 -= l3;
		if (k == 1)
			l2--;
		if (l2 < 0) {
			k1 -= l2;
			j2 -= l2;
			l2 = 0;
		}
		char ac[] = digits = new char[18];
		int i4 = 0;
		int j3 = k + k1 + (i2 >= n5bits.length ? i2 * 3 : n5bits[i2]);
		int k3 = j2 + 1
				+ (k2 + 1 >= n5bits.length ? (k2 + 1) * 3 : n5bits[k2 + 1]);
		boolean flag;
		boolean flag1;
		long l4;
		if (j3 < 64 && k3 < 64) {
			if (j3 < 32 && k3 < 32) {
				int i6 = (int) l * small5pow[i2] << k1;
				int j6 = small5pow[k2] << j2;
				int i7 = small5pow[i3] << l2;
				int j7 = j6 * 10;
				i4 = 0;
				int j4 = i6 / j6;
				i6 = 10 * (i6 % j6);
				i7 *= 10;
				flag = i6 < i7;
				flag1 = i6 + i7 > j7;
				if (!$assertionsDisabled && j4 >= 10)
					throw new AssertionError(j4);
				if (j4 == 0 && !flag1)
					j1--;
				else
					ac[i4++] = (char) (48 + j4);
				if (form != Form.COMPATIBLE || -3 >= j1 || j1 >= 8)
					flag1 = flag = false;
				while (!flag && !flag1) {
					int k4 = i6 / j6;
					i6 = 10 * (i6 % j6);
					i7 *= 10;
					if (!$assertionsDisabled && k4 >= 10)
						throw new AssertionError(k4);
					if ((long) i7 > 0L) {
						flag = i6 < i7;
						flag1 = i6 + i7 > j7;
					} else {
						flag = true;
						flag1 = true;
					}
					ac[i4++] = (char) (48 + k4);
				}
				l4 = (i6 << 1) - j7;
			} else {
				long l6 = l * long5pow[i2] << k1;
				long l7 = long5pow[k2] << j2;
				long l8 = long5pow[i3] << l2;
				long l9 = l7 * 10L;
				i4 = 0;
				int i5 = (int) (l6 / l7);
				l6 = 10L * (l6 % l7);
				l8 *= 10L;
				flag = l6 < l8;
				flag1 = l6 + l8 > l9;
				if (!$assertionsDisabled && i5 >= 10)
					throw new AssertionError(i5);
				if (i5 == 0 && !flag1)
					j1--;
				else
					ac[i4++] = (char) (48 + i5);
				if (form != Form.COMPATIBLE || -3 >= j1 || j1 >= 8)
					flag1 = flag = false;
				while (!flag && !flag1) {
					int j5 = (int) (l6 / l7);
					l6 = 10L * (l6 % l7);
					l8 *= 10L;
					if (!$assertionsDisabled && j5 >= 10)
						throw new AssertionError(j5);
					if (l8 > 0L) {
						flag = l6 < l8;
						flag1 = l6 + l8 > l9;
					} else {
						flag = true;
						flag1 = true;
					}
					ac[i4++] = (char) (48 + j5);
				}
				l4 = (l6 << 1) - l9;
			}
		} else {
			FDBigInt fdbigint1 = multPow52(new FDBigInt(l), i2, k1);
			FDBigInt fdbigint = constructPow52(k2, j2);
			FDBigInt fdbigint2 = constructPow52(i3, l2);
			int k6;
			fdbigint1.lshiftMe(k6 = fdbigint.normalizeMe());
			fdbigint2.lshiftMe(k6);
			FDBigInt fdbigint3 = fdbigint.mult(10);
			i4 = 0;
			int k5 = fdbigint1.quoRemIteration(fdbigint);
			fdbigint2 = fdbigint2.mult(10);
			flag = fdbigint1.cmp(fdbigint2) < 0;
			flag1 = fdbigint1.add(fdbigint2).cmp(fdbigint3) > 0;
			if (!$assertionsDisabled && k5 >= 10)
				throw new AssertionError(k5);
			if (k5 == 0 && !flag1)
				j1--;
			else
				ac[i4++] = (char) (48 + k5);
			if (form != Form.COMPATIBLE || -3 >= j1 || j1 >= 8)
				flag1 = flag = false;
			while (!flag && !flag1) {
				int l5 = fdbigint1.quoRemIteration(fdbigint);
				fdbigint2 = fdbigint2.mult(10);
				if (!$assertionsDisabled && l5 >= 10)
					throw new AssertionError(l5);
				flag = fdbigint1.cmp(fdbigint2) < 0;
				flag1 = fdbigint1.add(fdbigint2).cmp(fdbigint3) > 0;
				ac[i4++] = (char) (48 + l5);
			}
			if (flag1 && flag) {
				fdbigint1.lshiftMe(1);
				l4 = fdbigint1.cmp(fdbigint3);
			} else {
				l4 = 0L;
			}
		}
		decExponent = j1 + 1;
		digits = ac;
		nDigits = i4;
		if (flag1)
			if (flag) {
				if (l4 == 0L) {
					if ((ac[nDigits - 1] & '\001') != 0)
						roundup();
				} else if (l4 > 0L)
					roundup();
			} else {
				roundup();
			}
	}

	public String toString() {
		StringBuffer stringbuffer = new StringBuffer(nDigits + 8);
		if (isNegative)
			stringbuffer.append('-');
		if (isExceptional) {
			stringbuffer.append(digits, 0, nDigits);
		} else {
			stringbuffer.append("0.");
			stringbuffer.append(digits, 0, nDigits);
			stringbuffer.append('e');
			stringbuffer.append(decExponent);
		}
		return new String(stringbuffer);
	}

	public String toJavaFormatString() {
		char ac[] = (char[]) (char[]) perThreadBuffer.get();
		int i = getChars(ac);
		return new String(ac, 0, i);
	}

	public int getExponent() {
		return decExponent - 1;
	}

	public int getExponentRounded() {
		return decExponentRounded - 1;
	}

	public int getChars(char ac[])
 {
     if(!$assertionsDisabled && nDigits > 19)
         throw new AssertionError(nDigits);
     int i = 0;
     if(isNegative)
     {
         ac[0] = '-';
         i = 1;
     }
     if(isExceptional)
     {
         System.arraycopy(digits, 0, ac, i, nDigits);
         i += nDigits;
     } else
     {
         char ac1[] = digits;
         int j = decExponent;
         static class _cls2
         {

             static final int $SwitchMap$sun$misc$FormattedFloatingDecimal$Form[];

             static 
             {
                 $SwitchMap$sun$misc$FormattedFloatingDecimal$Form = new int[Form.values().length];
                 try
                 {
                     $SwitchMap$sun$misc$FormattedFloatingDecimal$Form[Form.COMPATIBLE.ordinal()] = 1;
                 }
                 catch(NoSuchFieldError nosuchfielderror) { }
                 try
                 {
                     $SwitchMap$sun$misc$FormattedFloatingDecimal$Form[Form.DECIMAL_FLOAT.ordinal()] = 2;
                 }
                 catch(NoSuchFieldError nosuchfielderror1) { }
                 try
                 {
                     $SwitchMap$sun$misc$FormattedFloatingDecimal$Form[Form.SCIENTIFIC.ordinal()] = 3;
                 }
                 catch(NoSuchFieldError nosuchfielderror2) { }
                 try
                 {
                     $SwitchMap$sun$misc$FormattedFloatingDecimal$Form[Form.GENERAL.ordinal()] = 4;
                 }
                 catch(NoSuchFieldError nosuchfielderror3) { }
             }
         }

         switch(_cls2..SwitchMap.sun.misc.FormattedFloatingDecimal.Form[form.ordinal()])
         {
         case 1: // '\001'
             break;

         case 2: // '\002'
             j = checkExponent(decExponent + precision);
             ac1 = applyPrecision(decExponent + precision);
             break;

         case 3: // '\003'
             j = checkExponent(precision + 1);
             ac1 = applyPrecision(precision + 1);
             break;

         case 4: // '\004'
             j = checkExponent(precision);
             ac1 = applyPrecision(precision);
             if(j - 1 < -4 || j - 1 >= precision)
             {
                 form = Form.SCIENTIFIC;
                 precision--;
             } else
             {
                 form = Form.DECIMAL_FLOAT;
                 precision = precision - j;
             }
             break;

         default:
             if(!$assertionsDisabled)
                 throw new AssertionError();
             break;
         }
         decExponentRounded = j;
         if(j > 0 && (form == Form.COMPATIBLE && j < 8 || form == Form.DECIMAL_FLOAT))
         {
             int k = Math.min(nDigits, j);
             System.arraycopy(ac1, 0, ac, i, k);
             i += k;
             if(k < j)
             {
                 k = j - k;
                 for(int l1 = 0; l1 < k; l1++)
                     ac[i++] = '0';

                 if(form == Form.COMPATIBLE)
                 {
                     ac[i++] = '.';
                     ac[i++] = '0';
                 }
             } else
             if(form == Form.COMPATIBLE)
             {
                 ac[i++] = '.';
                 if(k < nDigits)
                 {
                     int i2 = Math.min(nDigits - k, precision);
                     System.arraycopy(ac1, k, ac, i, i2);
                     i += i2;
                 } else
                 {
                     ac[i++] = '0';
                 }
             } else
             {
                 int j2 = Math.min(nDigits - k, precision);
                 if(j2 > 0)
                 {
                     ac[i++] = '.';
                     System.arraycopy(ac1, k, ac, i, j2);
                     i += j2;
                 }
             }
         } else
         if(j <= 0 && (form == Form.COMPATIBLE && j > -3 || form == Form.DECIMAL_FLOAT))
         {
             ac[i++] = '0';
             if(j != 0)
             {
                 int l = Math.min(-j, precision);
                 if(l > 0)
                 {
                     ac[i++] = '.';
                     for(int k2 = 0; k2 < l; k2++)
                         ac[i++] = '0';

                 }
             }
             int i1 = Math.min(ac1.length, precision + j);
             if(i1 > 0)
             {
                 if(i == 1)
                     ac[i++] = '.';
                 System.arraycopy(ac1, 0, ac, i, i1);
                 i += i1;
             }
         } else
         {
             ac[i++] = ac1[0];
             if(form == Form.COMPATIBLE)
             {
                 ac[i++] = '.';
                 if(nDigits > 1)
                 {
                     System.arraycopy(ac1, 1, ac, i, nDigits - 1);
                     i += nDigits - 1;
                 } else
                 {
                     ac[i++] = '0';
                 }
                 ac[i++] = 'E';
             } else
             {
                 if(nDigits > 1)
                 {
                     int j1 = Math.min(nDigits - 1, precision);
                     if(j1 > 0)
                     {
                         ac[i++] = '.';
                         System.arraycopy(ac1, 1, ac, i, j1);
                         i += j1;
                     }
                 }
                 ac[i++] = 'e';
             }
             int k1;
             if(j <= 0)
             {
                 ac[i++] = '-';
                 k1 = -j + 1;
             } else
             {
                 if(form != Form.COMPATIBLE)
                     ac[i++] = '+';
                 k1 = j - 1;
             }
             if(k1 <= 9)
             {
                 if(form != Form.COMPATIBLE)
                     ac[i++] = '0';
                 ac[i++] = (char)(k1 + 48);
             } else
             if(k1 <= 99)
             {
                 ac[i++] = (char)(k1 / 10 + 48);
                 ac[i++] = (char)(k1 % 10 + 48);
             } else
             {
                 ac[i++] = (char)(k1 / 100 + 48);
                 k1 %= 100;
                 ac[i++] = (char)(k1 / 10 + 48);
                 ac[i++] = (char)(k1 % 10 + 48);
             }
         }
     }
     return i;
 }

	public void appendTo(Appendable appendable) {
		char ac[] = (char[]) (char[]) perThreadBuffer.get();
		int i = getChars(ac);
		if (appendable instanceof StringBuilder)
			((StringBuilder) appendable).append(ac, 0, i);
		else if (appendable instanceof StringBuffer)
			((StringBuffer) appendable).append(ac, 0, i);
		else if (!$assertionsDisabled)
			throw new AssertionError();
	}

	public static FormattedFloatingDecimal readJavaFormatString(String s)
			throws NumberFormatException {
		boolean flag;
		boolean flag1;
		flag = false;
		flag1 = false;
		char c;
		int j;
		int k;
		boolean flag2;
		char ac1[];
		int i1;
		s = s.trim();
		j = s.length();
		if (j == 0)
			throw new NumberFormatException("empty String");
		k = 0;
		switch (c = s.charAt(k)) {
		case 45: // '-'
			flag = true;
			// fall through

		case 43: // '+'
			k++;
			flag1 = true;
			// fall through

		default:
			c = s.charAt(k);
			break;
		}
		if (c != 'N' && c != 'I')
			break MISSING_BLOCK_LABEL_227;
		flag2 = false;
		ac1 = null;
		if (c == 'N') {
			ac1 = notANumber;
			flag2 = true;
		} else {
			ac1 = infinity;
		}
		for (i1 = 0; k < j && i1 < ac1.length; i1++) {
			if (s.charAt(k) != ac1[i1])
				break MISSING_BLOCK_LABEL_829;
			k++;
		}

		int i;
		char c1;
		char ac[];
		int l;
		boolean flag3;
		int j1;
		int k1;
		int l1;
		byte byte0;
		int i2;
		int j2;
		boolean flag4;
		int k2;
		int l2;
		try {
			if (i1 == ac1.length && k == j)
				return flag2 ? new FormattedFloatingDecimal((0.0D / 0.0D))
						: new FormattedFloatingDecimal(flag ? (-1.0D / 0.0D)
								: (1.0D / 0.0D));
		} catch (StringIndexOutOfBoundsException stringindexoutofboundsexception) {
		}
		break MISSING_BLOCK_LABEL_829;
		if (c == '0' && j > k + 1) {
			c1 = s.charAt(k + 1);
			if (c1 == 'x' || c1 == 'X')
				return parseHexString(s);
		}
		ac = new char[j];
		l = 0;
		flag3 = false;
		j1 = 0;
		k1 = 0;
		l1 = 0;
		label0: for (; k < j; k++)
			switch (c = s.charAt(k)) {
			case 47: // '/'
			default:
				break label0;

			case 48: // '0'
				if (l > 0)
					l1++;
				else
					k1++;
				break;

			case 49: // '1'
			case 50: // '2'
			case 51: // '3'
			case 52: // '4'
			case 53: // '5'
			case 54: // '6'
			case 55: // '7'
			case 56: // '8'
			case 57: // '9'
				for (; l1 > 0; l1--)
					ac[l++] = '0';

				ac[l++] = c;
				break;

			case 46: // '.'
				if (flag3)
					throw new NumberFormatException("multiple points");
				j1 = k;
				if (flag1)
					j1--;
				flag3 = true;
				break;
			}

		if (l == 0) {
			ac = zero;
			l = 1;
			if (k1 == 0)
				break MISSING_BLOCK_LABEL_829;
		}
		if (flag3)
			i = j1 - k1;
		else
			i = l + l1;
		if (k < j && ((c = s.charAt(k)) == 'e' || c == 'E')) {
			byte0 = 1;
			i2 = 0;
			j2 = 214748364;
			flag4 = false;
			switch (s.charAt(++k)) {
			case 45: // '-'
				byte0 = -1;
				// fall through

			case 43: // '+'
				k++;
				// fall through

			default:
				k2 = k;
				break;
			}
			label1: do {
				if (k >= j)
					break;
				if (i2 >= j2)
					flag4 = true;
				switch (c = s.charAt(k++)) {
				case 48: // '0'
				case 49: // '1'
				case 50: // '2'
				case 51: // '3'
				case 52: // '4'
				case 53: // '5'
				case 54: // '6'
				case 55: // '7'
				case 56: // '8'
				case 57: // '9'
					i2 = i2 * 10 + (c - 48);
					break;

				default:
					k--;
					break label1;
				}
			} while (true);
			l2 = 324 + l + l1;
			if (flag4 || i2 > l2)
				i = byte0 * l2;
			else
				i += byte0 * i2;
			if (k == k2)
				break MISSING_BLOCK_LABEL_829;
		}
		if (k < j
				&& (k != j - 1 || s.charAt(k) != 'f' && s.charAt(k) != 'F'
						&& s.charAt(k) != 'd' && s.charAt(k) != 'D'))
			break MISSING_BLOCK_LABEL_829;
		return new FormattedFloatingDecimal(flag, i, ac, l, false, 2147483647,
				Form.COMPATIBLE);
		throw new NumberFormatException((new StringBuilder()).append(
				"For input string: \"").append(s).append("\"").toString());
	}

	public double doubleValue() {
		int i = Math.min(nDigits, 16);
		if (digits == infinity || digits == notANumber)
			if (digits == notANumber)
				return (0.0D / 0.0D);
			else
				return isNegative ? (-1.0D / 0.0D) : (1.0D / 0.0D);
		if (mustSetRoundDir)
			roundDir = 0;
		int j = digits[0] - 48;
		int k = Math.min(i, 9);
		for (int i1 = 1; i1 < k; i1++)
			j = (j * 10 + digits[i1]) - 48;

		long l = j;
		for (int j1 = k; j1 < i; j1++)
			l = l * 10L + (long) (digits[j1] - 48);

		double d = l;
		int k1 = decExponent - i;
		if (nDigits <= 15) {
			if (k1 == 0 || d == 0.0D)
				return isNegative ? -d : d;
			if (k1 >= 0) {
				if (k1 <= maxSmallTen) {
					double d1 = d * small10pow[k1];
					if (mustSetRoundDir) {
						double d4 = d1 / small10pow[k1];
						roundDir = d4 != d ? d4 >= d ? -1 : 1 : 0;
					}
					return isNegative ? -d1 : d1;
				}
				int l1 = 15 - i;
				if (k1 <= maxSmallTen + l1) {
					d *= small10pow[l1];
					double d2 = d * small10pow[k1 - l1];
					if (mustSetRoundDir) {
						double d5 = d2 / small10pow[k1 - l1];
						roundDir = d5 != d ? d5 >= d ? -1 : 1 : 0;
					}
					return isNegative ? -d2 : d2;
				}
			} else if (k1 >= -maxSmallTen) {
				double d3 = d / small10pow[-k1];
				double d6 = d3 * small10pow[-k1];
				if (mustSetRoundDir)
					roundDir = d6 != d ? d6 >= d ? -1 : 1 : 0;
				return isNegative ? -d3 : d3;
			}
		}
		if (k1 > 0) {
			if (decExponent > 309)
				return isNegative ? (-1.0D / 0.0D) : (1.0D / 0.0D);
			if ((k1 & 15) != 0)
				d *= small10pow[k1 & 15];
			if ((k1 >>= 4) != 0) {
				int i2 = 0;
				for (; k1 > 1; k1 >>= 1) {
					if ((k1 & 1) != 0)
						d *= big10pow[i2];
					i2++;
				}

				double d7 = d * big10pow[i2];
				if (Double.isInfinite(d7)) {
					d7 = d / 2D;
					d7 *= big10pow[i2];
					if (Double.isInfinite(d7))
						return isNegative ? (-1.0D / 0.0D) : (1.0D / 0.0D);
					d7 = 1.7976931348623157E+308D;
				}
				d = d7;
			}
		} else if (k1 < 0) {
			k1 = -k1;
			if (decExponent < -325)
				return isNegative ? -0D : 0.0D;
			if ((k1 & 15) != 0)
				d /= small10pow[k1 & 15];
			if ((k1 >>= 4) != 0) {
				int j2 = 0;
				for (; k1 > 1; k1 >>= 1) {
					if ((k1 & 1) != 0)
						d *= tiny10pow[j2];
					j2++;
				}

				double d8 = d * tiny10pow[j2];
				if (d8 == 0.0D) {
					d8 = d * 2D;
					d8 *= tiny10pow[j2];
					if (d8 == 0.0D)
						return isNegative ? -0D : 0.0D;
					d8 = 4.9406564584124654E-324D;
				}
				d = d8;
			}
		}
		FDBigInt fdbigint = new FDBigInt(l, digits, i, nDigits);
		k1 = decExponent - nDigits;
		do {
			FDBigInt fdbigint1 = doubleToBigInt(d);
			int k2;
			int l2;
			int i3;
			int j3;
			if (k1 >= 0) {
				k2 = l2 = 0;
				i3 = j3 = k1;
			} else {
				k2 = l2 = -k1;
				i3 = j3 = 0;
			}
			if (bigIntExp >= 0)
				k2 += bigIntExp;
			else
				i3 -= bigIntExp;
			int k3 = k2;
			int l3;
			if (bigIntExp + bigIntNBits <= -1022)
				l3 = bigIntExp + 1023 + 52;
			else
				l3 = 54 - bigIntNBits;
			k2 += l3;
			i3 += l3;
			int i4 = Math.min(k2, Math.min(i3, k3));
			k2 -= i4;
			i3 -= i4;
			k3 -= i4;
			fdbigint1 = multPow52(fdbigint1, l2, k2);
			FDBigInt fdbigint2 = multPow52(new FDBigInt(fdbigint), j3, i3);
			FDBigInt fdbigint3;
			int j4;
			boolean flag;
			if ((j4 = fdbigint1.cmp(fdbigint2)) > 0) {
				flag = true;
				fdbigint3 = fdbigint1.sub(fdbigint2);
				if (bigIntNBits == 1 && bigIntExp > -1023 && --k3 < 0) {
					k3 = 0;
					fdbigint3.lshiftMe(1);
				}
			} else {
				if (j4 >= 0)
					break;
				flag = false;
				fdbigint3 = fdbigint2.sub(fdbigint1);
			}
			FDBigInt fdbigint4 = constructPow52(l2, k3);
			if ((j4 = fdbigint3.cmp(fdbigint4)) < 0) {
				if (mustSetRoundDir)
					roundDir = flag ? -1 : 1;
				break;
			}
			if (j4 == 0) {
				d += 0.5D * ulp(d, flag);
				if (mustSetRoundDir)
					roundDir = flag ? -1 : 1;
				break;
			}
			d += ulp(d, flag);
		} while (d != 0.0D && d != (1.0D / 0.0D));
		return isNegative ? -d : d;
	}

	public float floatValue() {
		int i = Math.min(nDigits, 8);
		if (digits == infinity || digits == notANumber)
			if (digits == notANumber)
				return (0.0F / 0.0F);
			else
				return isNegative ? (-1.0F / 0.0F) : (1.0F / 0.0F);
		int j = digits[0] - 48;
		for (int k = 1; k < i; k++)
			j = (j * 10 + digits[k]) - 48;

		float f = j;
		int l = decExponent - i;
		if (nDigits <= 7) {
			if (l == 0 || f == 0.0F)
				return isNegative ? -f : f;
			if (l >= 0) {
				if (l <= singleMaxSmallTen) {
					f *= singleSmall10pow[l];
					return isNegative ? -f : f;
				}
				int j1 = 7 - i;
				if (l <= singleMaxSmallTen + j1) {
					f *= singleSmall10pow[j1];
					f *= singleSmall10pow[l - j1];
					return isNegative ? -f : f;
				}
			} else if (l >= -singleMaxSmallTen) {
				f /= singleSmall10pow[-l];
				return isNegative ? -f : f;
			}
		} else if (decExponent >= nDigits && nDigits + decExponent <= 15) {
			long l1 = j;
			for (int k1 = i; k1 < nDigits; k1++)
				l1 = l1 * 10L + (long) (digits[k1] - 48);

			double d1 = l1;
			int i1 = decExponent - nDigits;
			d1 *= small10pow[i1];
			float f1 = (float) d1;
			return isNegative ? -f1 : f1;
		}
		if (decExponent > 39)
			return isNegative ? (-1.0F / 0.0F) : (1.0F / 0.0F);
		if (decExponent < -46) {
			return isNegative ? -0F : 0.0F;
		} else {
			mustSetRoundDir = !fromHex;
			double d = doubleValue();
			return stickyRound(d);
		}
	}

	static FormattedFloatingDecimal parseHexString(String s) {
		Matcher matcher = hexFloatPattern.matcher(s);
		boolean flag = matcher.matches();
		if (!flag)
			throw new NumberFormatException((new StringBuilder()).append(
					"For input string: \"").append(s).append("\"").toString());
		String s1 = matcher.group(1);
		double d = s1 != null && !s1.equals("+") ? -1D : 1.0D;
		String s2 = null;
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;
		String s4;
		if ((s4 = matcher.group(4)) != null) {
			s2 = stripLeadingZeros(s4);
			k = s2.length();
		} else {
			String s5 = stripLeadingZeros(matcher.group(6));
			k = s5.length();
			String s6 = matcher.group(7);
			l = s6.length();
			s2 = (new StringBuilder()).append(s5 != null ? s5 : "").append(s6)
					.toString();
		}
		s2 = stripLeadingZeros(s2);
		i = s2.length();
		if (k >= 1)
			j = 4 * (k - 1);
		else
			j = -4 * ((l - i) + 1);
		if (i == 0)
			return new FormattedFloatingDecimal(d * 0.0D);
		String s3 = matcher.group(8);
		boolean flag1 = s3 == null || s3.equals("+");
		long l1;
		try {
			l1 = Integer.parseInt(matcher.group(9));
		} catch (NumberFormatException numberformatexception) {
			return new FormattedFloatingDecimal(d
					* (flag1 ? (1.0D / 0.0D) : 0.0D));
		}
		long l2 = (flag1 ? 1L : -1L) * l1;
		long l3 = l2 + (long) j;
		boolean flag2 = false;
		boolean flag3 = false;
		boolean flag4 = false;
		int i1 = 0;
		long l4 = 0L;
		long l5 = getHexDigit(s2, 0);
		if (l5 == 1L) {
			l4 |= l5 << 52;
			i1 = 48;
		} else if (l5 <= 3L) {
			l4 |= l5 << 51;
			i1 = 47;
			l3++;
		} else if (l5 <= 7L) {
			l4 |= l5 << 50;
			i1 = 46;
			l3 += 2L;
		} else if (l5 <= 15L) {
			l4 |= l5 << 49;
			i1 = 45;
			l3 += 3L;
		} else {
			throw new AssertionError("Result from digit converstion too large!");
		}
		int j1 = 0;
		for (j1 = 1; j1 < i && i1 >= 0; j1++) {
			long l6 = getHexDigit(s2, j1);
			l4 |= l6 << i1;
			i1 -= 4;
		}

		if (j1 < i) {
			long l7 = getHexDigit(s2, j1);
			switch (i1) {
			case -1:
				l4 |= (l7 & 14L) >> 1;
				flag2 = (l7 & 1L) != 0L;
				break;

			case -2:
				l4 |= (l7 & 12L) >> 2;
				flag2 = (l7 & 2L) != 0L;
				flag3 = (l7 & 1L) != 0L;
				break;

			case -3:
				l4 |= (l7 & 8L) >> 3;
				flag2 = (l7 & 4L) != 0L;
				flag3 = (l7 & 3L) != 0L;
				break;

			case -4:
				flag2 = (l7 & 8L) != 0L;
				flag3 = (l7 & 7L) != 0L;
				break;

			default:
				throw new AssertionError("Unexpected shift distance remainder.");
			}
			for (j1++; j1 < i && !flag3; j1++) {
				long l8 = getHexDigit(s2, j1);
				flag3 = flag3 || l8 != 0L;
			}

		}
		if (l3 > 1023L)
			return new FormattedFloatingDecimal(d * (1.0D / 0.0D));
		if (l3 <= 1023L && l3 >= -1022L) {
			l4 = l3 + 1023L << 52 & 9218868437227405312L | 4503599627370495L
					& l4;
		} else {
			if (l3 < -1075L)
				return new FormattedFloatingDecimal(d * 0.0D);
			flag3 = flag3 || flag2;
			flag2 = false;
			int k1 = 53 - (((int) l3 - -1074) + 1);
			if (!$assertionsDisabled && (k1 < 1 || k1 > 53))
				throw new AssertionError();
			flag2 = (l4 & 1L << k1 - 1) != 0L;
			if (k1 > 1) {
				long l9 = ~(-1L << k1 - 1);
				flag3 = flag3 || (l4 & l9) != 0L;
			}
			l4 >>= k1;
			l4 = 0L | 4503599627370495L & l4;
		}
		boolean flag5 = false;
		boolean flag7 = (l4 & 1L) == 0L;
		if (flag7 && flag2 && flag3 || !flag7 && flag2) {
			boolean flag6 = true;
			l4++;
		}
		FormattedFloatingDecimal formattedfloatingdecimal = new FormattedFloatingDecimal(
				FpUtils.rawCopySign(Double.longBitsToDouble(l4), d));
		if (l3 >= -150L && l3 <= 127L && (l4 & 268435455L) == 0L
				&& (flag2 || flag3))
			if (flag7) {
				if (flag2 ^ flag3)
					formattedfloatingdecimal.roundDir = 1;
			} else if (flag2)
				formattedfloatingdecimal.roundDir = -1;
		formattedfloatingdecimal.fromHex = true;
		return formattedfloatingdecimal;
	}

	static String stripLeadingZeros(String s) {
		return s.replaceFirst("^0+", "");
	}

	static int getHexDigit(String s, int i) {
		int j = Character.digit(s.charAt(i), 16);
		if (j <= -1 || j >= 16)
			throw new AssertionError((new StringBuilder()).append(
					"Unxpected failure of digit converstion of ").append(
					s.charAt(i)).toString());
		else
			return j;
	}

	boolean isExceptional;

	boolean isNegative;

	int decExponent;

	int decExponentRounded;

	char digits[];

	int nDigits;

	int bigIntExp;

	int bigIntNBits;

	boolean mustSetRoundDir;

	boolean fromHex;

	int roundDir;

	int precision;

	private Form form;

	static final long signMask = -9223372036854775808L;

	static final long expMask = 9218868437227405312L;

	static final long fractMask = 4503599627370495L;

	static final int expShift = 52;

	static final int expBias = 1023;

	static final long fractHOB = 4503599627370496L;

	static final long expOne = 4607182418800017408L;

	static final int maxSmallBinExp = 62;

	static final int minSmallBinExp = -21;

	static final int maxDecimalDigits = 15;

	static final int maxDecimalExponent = 308;

	static final int minDecimalExponent = -324;

	static final int bigDecimalExponent = 324;

	static final long highbyte = -72057594037927936L;

	static final long highbit = -9223372036854775808L;

	static final long lowbytes = 72057594037927935L;

	static final int singleSignMask = -2147483648;

	static final int singleExpMask = 2139095040;

	static final int singleFractMask = 8388607;

	static final int singleExpShift = 23;

	static final int singleFractHOB = 8388608;

	static final int singleExpBias = 127;

	static final int singleMaxDecimalDigits = 7;

	static final int singleMaxDecimalExponent = 38;

	static final int singleMinDecimalExponent = -45;

	static final int intDecimalDigits = 9;

	private static FDBigInt b5p[];

	private static ThreadLocal perThreadBuffer = new ThreadLocal() {

		protected synchronized Object initialValue() {
			return new char[26];
		}

	};

	private static final double small10pow[] = { 1.0D, 10D, 100D, 1000D,
			10000D, 100000D, 1000000D, 10000000D, 100000000D, 1000000000D,
			10000000000D, 100000000000D, 1000000000000D, 10000000000000D,
			100000000000000D, 1000000000000000D, 10000000000000000D, 1E+017D,
			1E+018D, 1E+019D, 1E+020D, 1E+021D, 1E+022D };

	private static final float singleSmall10pow[] = { 1.0F, 10F, 100F, 1000F,
			10000F, 100000F, 1000000F, 1E+007F, 1E+008F, 1E+009F, 1E+010F };

	private static final double big10pow[] = { 10000000000000000D,
			1.0000000000000001E+032D, 1E+064D, 1.0000000000000001E+128D,
			1E+256D };

	private static final double tiny10pow[] = { 9.9999999999999998E-017D,
			1.0000000000000001E-032D, 9.9999999999999997E-065D,
			1.0000000000000001E-128D, 9.9999999999999998E-257D };

	private static final int maxSmallTen = small10pow.length - 1;

	private static final int singleMaxSmallTen = singleSmall10pow.length - 1;

	private static final int small5pow[] = { 1, 5, 25, 125, 625, 3125, 15625,
			78125, 390625, 1953125, 9765625, 48828125, 244140625, 1220703125 };

	private static final long long5pow[] = { 1L, 5L, 25L, 125L, 625L, 3125L,
			15625L, 78125L, 390625L, 1953125L, 9765625L, 48828125L, 244140625L,
			1220703125L, 6103515625L, 30517578125L, 152587890625L,
			762939453125L, 3814697265625L, 19073486328125L, 95367431640625L,
			476837158203125L, 2384185791015625L, 11920928955078125L,
			59604644775390625L, 298023223876953125L, 1490116119384765625L };

	private static final int n5bits[] = { 0, 3, 5, 7, 10, 12, 14, 17, 19, 21,
			24, 26, 28, 31, 33, 35, 38, 40, 42, 45, 47, 49, 52, 54, 56, 59, 61 };

	private static final char infinity[] = { 'I', 'n', 'f', 'i', 'n', 'i', 't',
			'y' };

	private static final char notANumber[] = { 'N', 'a', 'N' };

	private static final char zero[] = { '0', '0', '0', '0', '0', '0', '0', '0' };

	private static Pattern hexFloatPattern = Pattern
			.compile("([-+])?0[xX](((\\p{XDigit}+)\\.?)|((\\p{XDigit}*)\\.(\\p{XDigit}+)))[pP]([-+])?(\\p{Digit}+)[fFdD]?");

	static final boolean $assertionsDisabled = true ; //MrP - JAD casuality - !sun.misc.FormattedFloatingDecimal.desiredAssertionStatus();

}


/*
	DECOMPILATION REPORT

	Decompiled from: C:\opt\java5-ibm-2007-12-13\jre\lib\core.jar
	Total time: 125 ms
	Jad reported messages/errors:
Couldn't resolve all exception handlers in method readJavaFormatString
	Exit status: 0
	Caught exceptions:
*/

