/*------------------------------------------------------------------------
* (The MIT License)
* 
* Copyright (c) 2008-2011 Rhomobile, Inc.
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
* 
* http://rhomobile.com
*------------------------------------------------------------------------*/

package j2me.lang;
import j2me.math.HugeInt;
import j2me.lang.ArrayMe;

public class Convert {

    public static final byte[] intToByteArray(int i) {
        if (i == Integer.MIN_VALUE)
            //return (byte[])MIN_INT_BYTE_ARRAY.clone();
                return ArrayMe.clone(MIN_INT_BYTE_ARRAY);
        int size = (i < 0) ? arraySize(-i) + 1 : arraySize(i);
        byte[] buf = new byte[size];
        getCharBytes(i, size, buf);
        return buf;
    }


    public static final byte[] intToByteArray(int i, int radix, boolean upper) {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            radix = 10;
        if (radix == 10)
            return intToByteArray(i); // much faster for base 10
        byte buf[] = new byte[33];
        byte[] digits = upper ? UCDIGITS : DIGITS;
        boolean negative = (i < 0);
        int charPos = 32;
        if (!negative) {
            i = -i;
        }
        while (i <= -radix) {
            buf[charPos--] = digits[-(i % radix)];
            i = i / radix;
        }
        buf[charPos] = digits[-i];
        if (negative) {
            buf[--charPos] = '-';
        }
        int len = 33 - charPos;
        byte[] out = new byte[len];
        System.arraycopy(buf,charPos,out,0,len);
        return out;
    }

    public static final byte[] longToByteArray(long i) {
        if (i == Long.MIN_VALUE)
            //return (byte[])MIN_LONG_BYTE_ARRAY.clone();
                return ArrayMe.clone(MIN_LONG_BYTE_ARRAY);
        // int version is slightly faster, use if possible
        if (i <= Integer.MAX_VALUE && i >= Integer.MIN_VALUE)
            return intToByteArray((int)i);
        int size = (i < 0) ? arraySize(-i) + 1 : arraySize(i);
        byte[] buf = new byte[size];
        getCharBytes(i, size, buf);
        return buf;
    }
   
    public static final byte[] longToByteArray(long i, int radix, boolean upper) {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            radix = 10;
        if (radix == 10)
            return longToByteArray(i); // much faster for base 10
        byte[] buf = new byte[65];
        byte[] digits = upper ? UCDIGITS : DIGITS;
        int charPos = 64;
        boolean negative = (i < 0);
        if (!negative) {
            i = -i;
        }
        while (i <= -radix) {
            buf[charPos--] = digits[(int)(-(i % radix))];
            i = i / radix;
        }
        buf[charPos] = digits[(int)(-i)];
        if (negative) { 
            buf[--charPos] = '-';
        }
        int len = 65 - charPos;
        byte[] out = new byte[len];
        System.arraycopy(buf,charPos,out,0,len);
        return out;
    }

    public static final byte[] intToCharBytes(int i) {
        if (i == Integer.MIN_VALUE)
            //return (byte[])MIN_INT_BYTE_ARRAY.clone();
                return ArrayMe.clone(MIN_INT_BYTE_ARRAY);
        int size = (i < 0) ? arraySize(-i) + 1 : arraySize(i);
        byte[] buf = new byte[size];
        getCharBytes(i, size, buf);
        return buf;
    }

    public static final byte[] longToCharBytes(long i) {
        if (i == Long.MIN_VALUE)
            //return (byte[])MIN_LONG_BYTE_ARRAY.clone();
                return ArrayMe.clone(MIN_LONG_BYTE_ARRAY);
        int size = (i < 0) ? arraySize(-i) + 1 : arraySize(i);
        byte[] buf = new byte[size];
        getCharBytes(i, size, buf);
        return buf;
    }
    public static final char[] longToChars(long i) {
        if (i == Long.MIN_VALUE)
            //return (char[])MIN_LONG_CHAR_ARRAY.clone();
                return ArrayMe.clone(MIN_LONG_CHAR_ARRAY);
        int size = (i < 0) ? arraySize(-i) + 1 : arraySize(i);
        char[] buf = new char[size];
        getChars(i, size, buf);
        return buf;
    }
    
    public static final void getCharBytes(int i, int index, byte[] buf) {
        int q, r;
        int charPos = index;
        byte sign = 0;

        if (i < 0) { 
            sign = '-';
            i = -i;
        }

        // Generate two digits per iteration
        while (i >= 65536) {
            q = i / 100;
        // really: r = i - (q * 100);
            r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            buf [--charPos] = DIGIT_ONES[r];
            buf [--charPos] =DIGIT_TENS[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i <= 65536, i);
        for (;;) { 
            q = (i * 52429) >>> (16+3);
            r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
            buf [--charPos] = DIGITS[r];
            i = q;
            if (i == 0) break;
        }
        if (sign != 0) {
            buf [--charPos] = sign;
        }
    }

    public static final void getCharBytes(long i, int index, byte[] buf) {
        long q;
        int r;
        int charPos = index;
        byte sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i > Integer.MAX_VALUE) { 
            q = i / 100;
            // really: r = i - (q * 100);
            r = (int)(i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            buf[--charPos] = DIGIT_ONES[r];
            buf[--charPos] =DIGIT_TENS[r];
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int)i;
        while (i2 >= 65536) {
            q2 = i2 / 100;
            // really: r = i2 - (q * 100);
            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
            i2 = q2;
            buf[--charPos] = DIGIT_ONES[r];
            buf[--charPos] =DIGIT_TENS[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i2 <= 65536, i2);
        for (;;) {
            q2 = (i2 * 52429) >>> (16+3);
            r = i2 - ((q2 << 3) + (q2 << 1));  // r = i2-(q2*10) ...
            buf[--charPos] = DIGITS[r];
            i2 = q2;
            if (i2 == 0) break;
        }
        if (sign != 0) {
            buf[--charPos] = sign;
        }
    }
    public static final void getChars(long i, int index, char[] buf) {
        long q;
        int r;
        int charPos = index;
        char sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i > Integer.MAX_VALUE) { 
            q = i / 100;
            // really: r = i - (q * 100);
            r = (int)(i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            buf[--charPos] = CDIGIT_ONES[r];
            buf[--charPos] = CDIGIT_TENS[r];
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int)i;
        while (i2 >= 65536) {
            q2 = i2 / 100;
            // really: r = i2 - (q * 100);
            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
            i2 = q2;
            buf[--charPos] = CDIGIT_ONES[r];
            buf[--charPos] = CDIGIT_TENS[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i2 <= 65536, i2);
        for (;;) {
            q2 = (i2 * 52429) >>> (16+3);
            r = i2 - ((q2 << 3) + (q2 << 1));  // r = i2-(q2*10) ...
            buf[--charPos] = CDIGITS[r];
            i2 = q2;
            if (i2 == 0) break;
        }
        if (sign != 0) {
            buf[--charPos] = sign;
        }
    }

    public static final int arraySize(long x) {
        long p = 10;
        for (int i=1; i<19; i++) {
            if (x < p)
                return i;
            p = 10*p;
        }
        return 19;
    }
    
    public static final int arraySize(int x) {
        for (int i=0; ; i++)
            if (x <= SIZE_TABLE[i])
                return i+1;
    }
    // the following group of conversions to binary/octal/hex
    // is mostly for use by the new sprintf code
    public static final byte[] intToBinaryBytes(int i) {
        return intToUnsignedBytes(i, 1, false);
    }
    public static final byte[] intToOctalBytes(int i) {
        return intToUnsignedBytes(i, 3, false);
    }
    public static final byte[] intToHexBytes(int i) {
        return intToUnsignedBytes(i, 4, false);
    }

    public static final byte[] intToHexBytes(int i, boolean upper) {
        return intToUnsignedBytes(i, 4, upper);
    }


    public static final byte[] longToBinaryBytes(long i) {
        return longToUnsignedBytes(i, 1, false);
    }
    public static final byte[] longToOctalBytes(long i) {
        return longToUnsignedBytes(i, 3, false);
    }
    public static final byte[] longToHexBytes(long i) {
        return longToUnsignedBytes(i, 4, false);
    }

    public static final byte[] longToHexBytes(long i, boolean upper) {
        return longToUnsignedBytes(i, 4, upper);
    }

    public static final byte[] intToRawUnsignedBytes(int i, int shift) {
        byte[] buf = new byte[32];
        int charPos = 32;
        int radix = 1 << shift;
        int mask = radix - 1;
        do {
            buf[--charPos] = DIGITS[i & mask];
            i >>>= shift;
        } while (i != 0);
        return buf;
    }
    
    public static final byte[] intToUnsignedBytes(int i, int shift, boolean upper) {
        byte[] buf = new byte[32];
        int charPos = 32;
        int radix = 1 << shift;
        int mask = radix - 1;
        byte[] digits = upper ? UCDIGITS : DIGITS;
        do {
            buf[--charPos] = digits[i & mask];
            i >>>= shift;
        } while (i != 0);
        int length = 32 - charPos;
        byte[] result = new byte[length];
        System.arraycopy(buf,charPos,result,0,length);
        return result;
    }

    public static final byte[] longToRawUnsignedBytes(long i, int shift) {
        byte[] buf = new byte[64];
        int charPos = 64;
        int radix = 1 << shift;
        long mask = radix - 1;
        do {
            buf[--charPos] = DIGITS[(int)(i & mask)];
            i >>>= shift;
        } while (i != 0);
        return buf;
    }
    
    public static final byte[] longToUnsignedBytes(long i, int shift, boolean upper) {
        byte[] buf = new byte[64];
        int charPos = 64;
        int radix = 1 << shift;
        long mask = radix - 1;
        byte[] digits = upper ? UCDIGITS : DIGITS;
        do {
            buf[--charPos] = digits[(int)(i & mask)];
            i >>>= shift;
        } while (i != 0);
        int length = 64 - charPos;
        byte[] result = new byte[length];
        System.arraycopy(buf,charPos,result,0,length);
        return result;
    }

    public static final long byteArrayToLong(byte[] bytes, int begin, int buflen, int base, boolean strict) {
        final int SCOMPLETE         = 0;
        final int SBEGIN            = 1;
        final int SSIGN             = 2;
        final int SZERO             = 3;
        final int SPOST_SIGN        = 4;
        final int SDIGITS           = 5;
        final int SDIGIT            = 6;
        final int SDIGIT_STRICT     = 7;
        final int SDIGIT_USC        = 8;
        final int SEOD_STRICT       = 13;
        final int SEOF              = 14;
        final int SERR_NOT_STRICT   = 17;
        final int SERR_TOO_BIG      = 18;
        final int FLAG_NEGATIVE    = 1 << 0;
        final int FLAG_DIGIT       = 1 << 1;
        final int FLAG_UNDERSCORE  = 1 << 2;
        final int FLAG_WHITESPACE  = 1 << 3;
      
        if (bytes == null) {
            throw new IllegalArgumentException("null bytes");
        }
        if (buflen < 0 || buflen > bytes.length) {
            throw new IllegalArgumentException("invalid buflen specified");
        }
        int radix = base == 0 ? 10 : base;
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
            throw new IllegalArgumentException("illegal radix " + radix);
        }
        if (buflen == 0) {
            throw new RuntimeException("InvalidInteger");//InvalidIntegerException();
        }
        int i = begin;
        buflen += begin;
        byte ival;
        int flags = 0;
        long limit = -Long.MAX_VALUE;
        long result = 0;
        long multmin = 0;
        int digit;
        int state = SBEGIN;
        while (state != SCOMPLETE) {
            states:
            switch(state) {
            case SBEGIN:
                if (strict) {
                    for (; i < buflen && isWhitespace(bytes[i]); i++) ;
                } else {
                    for (; i < buflen && (isWhitespace(ival = bytes[i]) || ival == '_'); i++) ;
                }
                state = i < buflen ? SSIGN : SEOF;
                break;
            case SSIGN:
                switch(bytes[i]) {
                case '-':
                    flags |= FLAG_NEGATIVE;
                    limit = Long.MIN_VALUE;
                case '+':
                    if (++i >= buflen) {
                        state = SEOF;
                    } else if (bytes[i] == '0') {
                        state = SZERO;
                    } else {
                        state = SPOST_SIGN;
                    }
                    break states;
                case '0':
                    state = SZERO;
                    break states;
                default:
                    state = SDIGITS;
                    break states;
                }
            case SZERO:
                if (++i >= buflen) {
                    state = SCOMPLETE;
                    break;
                }
                switch (bytes[i]) {
                case 'x':
                case 'X':
                    if (base == 0 || base == 16) {
                        radix = 16;
                        state = ++i >= buflen ? SEOF : SPOST_SIGN;
                    } else {
                        state = SDIGITS;
                    }
                    break states;
                case 'b':
                case 'B':
                    if (base == 0 || base == 2) {
                        radix = 2;
                        state = ++i >= buflen ? SEOF : SPOST_SIGN;
                    } else {
                        state = SDIGITS;
                    }
                    break states;
                case 'd':
                case 'D':
                    if (base == 0 || base == 10) {
                        radix = 10;
                        state = ++i >= buflen ? SEOF : SPOST_SIGN;
                    } else {
                        state = SDIGITS;
                    }
                    break states;
                case 'o':
                case 'O':
                case '_':
                    if (base == 0 || base == 8) {
                        radix = 8;
                        state = ++i >= buflen ? SEOF : SPOST_SIGN;
                    } else {
                        state = SDIGITS;
                    }
                    break states;
                default:
                    if (base == 0 || base == 8) {
                        radix = 8;
                    }
                    flags |= FLAG_DIGIT;
                    state = SDIGITS;
                    break states;
                }
            case SPOST_SIGN:
                if (strict) {
                    int ibefore = i;
                    for (; i < buflen && isWhitespace(bytes[i]); i++) ;
                    if (ibefore != i) {
                        // set this to enforce no-underscore rule (though I think 
                        // it's an MRI bug)
                        flags |= FLAG_WHITESPACE;
                    }
                } else {
                    for ( ; i < buflen && (isWhitespace(ival = bytes[i]) || ival == '_'); i++) {
                        if (ival == '_') {
                            if ((flags & FLAG_WHITESPACE) != 0) {
                                throw new RuntimeException("InvalidInteger");//InvalidIntegerException();
                            }
                            flags |= FLAG_UNDERSCORE;
                        } else {
                            if ((flags & FLAG_UNDERSCORE) != 0) {
                                throw new RuntimeException("InvalidInteger");//InvalidIntegerException();
                            }
                            flags |= FLAG_WHITESPACE;
                        }
                    }
                }
                state = i < buflen ? SDIGITS : SEOF;
                break;
            case SDIGITS:
                digit = Character.digit((char) bytes[i],radix);
                if (digit < 0) {
                    state = strict ? SEOD_STRICT : SEOF;
                    break;
                }
                result = -digit;
                if (++i >= buflen) {
                    state = SCOMPLETE;
                    break;
                }
                multmin = limit / radix;
                flags = (flags | FLAG_DIGIT) & ~FLAG_UNDERSCORE;
                state = strict ? SDIGIT_STRICT : SDIGIT;
                break;

            case SDIGIT:
                while ((digit = Character.digit((char) bytes[i],radix)) >= 0) {
                    if (result < multmin || ((result *= radix) < limit + digit)) {
                        state = SERR_TOO_BIG;
                        break states;
                    }
                    result -= digit;
                    if (++i >= buflen) {
                        state = SCOMPLETE;
                        break states;
                    }
                }
                state = bytes[i++] == '_' ? SDIGIT_USC : SEOF;
                break;
            case SDIGIT_USC:
                for ( ; i < buflen && bytes[i] == '_'; i++) ;
                state = i < buflen ? SDIGIT : SEOF;
                break;
                
            case SDIGIT_STRICT:
                while ((digit = Character.digit((char) bytes[i],radix)) >= 0) {
                    if (result < multmin || ((result *= radix) < limit + digit)) {
                        state = SERR_TOO_BIG;
                        break states;
                    }
                    result -= digit;
                    if (++i >= buflen) {
                        state = SCOMPLETE;
                        break states;
                    }
                    flags &= ~FLAG_UNDERSCORE;
                }
                if (bytes[i] == '_') {
                    if ((flags & (FLAG_UNDERSCORE | FLAG_WHITESPACE)) != 0) {
                        state = SERR_NOT_STRICT;
                        break;
                    }
                    flags |= FLAG_UNDERSCORE;
                    state = ++i >= buflen ? SEOD_STRICT : SDIGIT_STRICT;
                } else {
                    state = SEOD_STRICT;
                }
                break;

            case SEOD_STRICT:
                if ((flags & FLAG_UNDERSCORE)!= 0) {
                    state = SERR_NOT_STRICT;
                    break;
                }
                for ( ; i < buflen && isWhitespace(bytes[i]); i++ );
                state = i < buflen ? SERR_NOT_STRICT : SCOMPLETE;
                break;
                
            case SEOF:
                if ((flags & FLAG_DIGIT) == 0) {
                    throw new RuntimeException("no digits supplied");
                }
                state = SCOMPLETE;
                break;

            case SERR_TOO_BIG:
                throw new RuntimeException("can't convert to long");

            case SERR_NOT_STRICT:
                throw new RuntimeException("does not meet strict criteria");
                
            } // switch
        } //while
        if ((flags & FLAG_NEGATIVE) == 0) {
            return -result;
        } else {
            return result;
        }
    }
    
    public static final HugeInt byteArrayToBigInteger(byte[] bytes, int begin, int buflen, int base, boolean strict) {
        final int SCOMPLETE         = 0;
        final int SBEGIN            = 1;
        final int SSIGN             = 2;
        final int SZERO             = 3;
        final int SPOST_SIGN        = 4;
        final int SDIGITS           = 5;
        final int SDIGIT            = 6;
        final int SDIGIT_STRICT     = 7;
        final int SDIGIT_USC        = 8;
        final int SEOD_STRICT       = 13;
        final int SEOF              = 14;
        final int SERR_NOT_STRICT   = 17;
        final int FLAG_NEGATIVE    = 1 << 0;
        final int FLAG_DIGIT       = 1 << 1;
        final int FLAG_UNDERSCORE  = 1 << 2;
        final int FLAG_WHITESPACE  = 1 << 3;
      
        if (bytes == null) {
            throw new IllegalArgumentException("null bytes");
        }
        if (buflen < 0 || buflen > bytes.length) {
            throw new IllegalArgumentException("invalid buflen specified");
        }
        int radix = base == 0 ? 10 : base;
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
            throw new IllegalArgumentException("illegal radix " + radix);
        }
        if (buflen == 0) {
            throw new RuntimeException("InvalidInteger");//InvalidIntegerException();
        }
        int i = begin;
        buflen += begin; 
        byte ival;
        int flags = 0;
        int digit;
        int offset = 0;
        char[] chars = null;
        int state = SBEGIN;
        while (state != SCOMPLETE) {
            states:
            switch(state) {
            case SBEGIN:
                if (strict) {
                    for (; i < buflen && isWhitespace(bytes[i]); i++) ;
                } else {
                    for (; i < buflen && (isWhitespace(ival = bytes[i]) || ival == '_'); i++) ;
                }
                state = i < buflen ? SSIGN : SEOF;
                break;
            case SSIGN:
                switch(bytes[i]) {
                case '-':
                    flags |= FLAG_NEGATIVE;
                case '+':
                    if (++i >= buflen) {
                        state = SEOF;
                    } else if (bytes[i] == '0') {
                        state = SZERO;
                    } else {
                        state = SPOST_SIGN;
                    }
                    break states;
                case '0':
                    state = SZERO;
                    break states;
                default:
                    state = SDIGITS;
                    break states;
                }
            case SZERO:
                if (++i >= buflen) {
                    state = SCOMPLETE;
                    break;
                }
                switch (bytes[i]) {
                case 'x':
                case 'X':
                    if (base == 0 || base == 16) {
                        radix = 16;
                        state = ++i >= buflen ? SEOF : SPOST_SIGN;
                    } else {
                        state = SDIGITS;
                    }
                    break states;
                case 'b':
                case 'B':
                    if (base == 0 || base == 2) {
                        radix = 2;
                        state = ++i >= buflen ? SEOF : SPOST_SIGN;
                    } else {
                        state = SDIGITS;
                    }
                    break states;
                default:
                    if (base == 0 || base == 8) {
                        radix = 8;
                    }
                    flags |= FLAG_DIGIT;
                    state = SDIGITS;
                    break states;
                }
            case SPOST_SIGN:
                if (strict) {
                    int ibefore = i;
                    for (; i < buflen && isWhitespace(bytes[i]); i++) ;
                    if (ibefore != i) {
                        // set this to enforce no-underscore rule (though I think 
                        // it's an MRI bug)
                        flags |= FLAG_WHITESPACE;
                    }
                } else {
                    for ( ; i < buflen && (isWhitespace(ival = bytes[i]) || ival == '_'); i++) {
                        if (ival == '_') {
                            if ((flags & FLAG_WHITESPACE) != 0) {
                                throw new RuntimeException("InvalidInteger");//InvalidIntegerException();
                            }
                            flags |= FLAG_UNDERSCORE;
                        } else {
                            if ((flags & FLAG_UNDERSCORE) != 0) {
                                throw new RuntimeException("InvalidInteger");//InvalidIntegerException();
                            }
                            flags |= FLAG_WHITESPACE;
                        }
                    }
                }
                state = i < buflen ? SDIGITS : SEOF;
                break;
            case SDIGITS:
                digit = Character.digit((char) bytes[i],radix);
                if (digit < 0) {
                    state = strict ? SEOD_STRICT : SEOF;
                    break;
                }
                if ((flags & FLAG_NEGATIVE) == 0) {
                    chars = new char[buflen - i];
                    chars[0] = (char)bytes[i];
                    offset = 1;
                } else {
                    chars = new char[buflen - i + 1];
                    chars[0] = '-';
                    chars[1] = (char)bytes[i];
                    offset = 2;
                }
                if (++i >= buflen) {
                    state = SCOMPLETE;
                    break;
                }
                flags = (flags | FLAG_DIGIT) & ~FLAG_UNDERSCORE;
                state = strict ? SDIGIT_STRICT : SDIGIT;
                break;

            case SDIGIT:
                while ((digit = Character.digit((char) bytes[i],radix)) >= 0) {
                    chars[offset++] = (char)bytes[i];
                    if (++i >= buflen) {
                        state = SCOMPLETE;
                        break states;
                    }
                }
                state = bytes[i++] == '_' ? SDIGIT_USC : SEOF;
                break;
            case SDIGIT_USC:
                for ( ; i < buflen && bytes[i] == '_'; i++) ;
                state = i < buflen ? SDIGIT : SEOF;
                break;
                
            case SDIGIT_STRICT:
                while ((digit = Character.digit((char)bytes[i],radix)) >= 0) {
                    chars[offset++] = (char)bytes[i];
                    if (++i >= buflen) {
                        state = SCOMPLETE;
                        break states;
                    }
                    flags &= ~FLAG_UNDERSCORE;
                }
                if (bytes[i] == '_') {
                    if ((flags & (FLAG_UNDERSCORE | FLAG_WHITESPACE)) != 0) {
                        state = SERR_NOT_STRICT;
                        break;
                    }
                    flags |= FLAG_UNDERSCORE;
                    state = ++i >= buflen ? SEOD_STRICT : SDIGIT_STRICT;
                } else {
                    state = SEOD_STRICT;
                }
                break;

            case SEOD_STRICT:
                if ((flags & FLAG_UNDERSCORE)!= 0) {
                    state = SERR_NOT_STRICT;
                    break;
                }
                for ( ; i < buflen && isWhitespace(bytes[i]); i++ );
                state = i < buflen ? SERR_NOT_STRICT : SCOMPLETE;
                break;
                
            case SEOF:
                if ((flags & FLAG_DIGIT) == 0) {
                    throw new RuntimeException("no digits supplied");
                }
                state = SCOMPLETE;
                break;

            case SERR_NOT_STRICT:
                throw new RuntimeException("does not meet strict criteria");
                
            } // switch
        } //while
        if (chars == null) { // 0, won't happen if byteArrayToLong called first
            return BIG_INT_ZERO;
        } else {
            return new HugeInt(new String(chars,0,offset),radix);
        }
    }
    
    public static final double byteArrayToDouble(byte[] bytes, int begin, int buflen, boolean strict) {
        // Simple cases  ( abs(exponent) <= 22 [up to 37 depending on significand length])
        // are converted directly, which is considerably faster than creating a Java
        // String and passing it to Double.parseDouble() (which in turn passes it to
        // sun.misc.FloatingDecimal); the latter approach involves 5 object allocations
        // (3 arrays + String + FloatingDecimal) and 3 array copies, two of them one byte/char
        // at a time (here and in FloatingDecimal).
        // However, the latter approach is employed for more difficult cases (generally
        // speaking, those that require rounding). (The code for the difficult cases is 
        // quite involved; see sun.misc.FloatingDecimal.java if you're interested.)

        // states
        final int SCOMPLETE            =  0;
        final int SBEGIN               =  1; // remove leading whitespace (includes _ for lax)
        final int SSIGN                =  2; // get sign, if any
        
        // optimistic pass - calculate value as digits are processed
        final int SOPTDIGIT            =  3; // digits - lax rules
        final int SOPTDECDIGIT         =  4; // decimal digits - lax rules
        final int SOPTEXP              =  9; // exponent sign/digits - lax rules
        final int SOPTDIGIT_STRICT     =  6; // digits - strict rules
        final int SOPTDECDIGIT_STRICT  =  7; // decimal digits - strict rules
        final int SOPTEXP_STRICT       =  8; // exponent sign/digits - strict rules
        final int SOPTCALC             =  5; // complete calculation if possible

        // fallback pass - gather chars into array and pass to Double.parseDouble()
        final int SDIGIT               = 10; // digits - lax rules
        final int SDECDIGIT            = 11; // decimal digits - lax rules
        final int SEXP                 = 12; // exponent sign/digits - lax rules
        final int SDIGIT_STRICT        = 13; // digits - strict rules
        final int SDECDIGIT_STRICT     = 14; // decimal digits - strict rules
        final int SEXP_STRICT          = 15; // exponent sign/digits - strict rules

        final int SERR_NOT_STRICT      = 16;

        // largest abs(exponent) we can (potentially) handle without
        // calling Double.parseDouble() (aka sun.misc.FloatingDecimal)
        final int MAX_EXP = MAX_DECIMAL_DIGITS + MAX_SMALL_10; // (37)
      
        if (bytes == null) {
            throw new IllegalArgumentException("null bytes");
        }
        if (buflen < 0 || buflen > bytes.length) {
            throw new IllegalArgumentException("invalid buflen specified");
        }
        // TODO: get rid of this (lax returns 0.0, strict will throw)
        if (buflen == 0) {
            throw new NumberFormatException();
        }
        int i = begin;
        buflen += begin;
        byte ival = -1;
        boolean negative = false;

        // fields used for direct (optimistic) calculation
        int nDigits = 0;         // number of significant digits, updated as parsed
        int nTrailingZeroes = 0; // zeroes that may go to significand or exponent
        int decPos = -1;         // offset of decimal pt from start (-1 -> no decimal)
        long significand = 0;    // significand, updated as parsed
        int exponent = 0;        // exponent, updated as parsed
        
        // fields used for fallback (send to Double.parseDouble())
        int startPos = 0;        // start of digits (or . if no leading digits)
        char[] chars = null;
        int offset = 0;
        int lastValidOffset = 0;

        int state = SBEGIN;
        while (state != SCOMPLETE) {
        states:
            switch(state) {
            case SBEGIN:
                if (strict) {
                    for (; i < buflen && isWhitespace(bytes[i]); i++) ;
                } else {
                    for (; i < buflen && (isWhitespace(ival = bytes[i]) || ival == '_'); i++) ;
                }
                if ( i >= buflen) {
                    state = strict ? SERR_NOT_STRICT : SCOMPLETE;
                    break;
                }
                // drop through for sign
            case SSIGN:
                switch(bytes[i]) {
                case '-':
                    negative = true;
                case '+':
                    if (++i >= buflen) {
                        // TODO: turn off the negative? will return -0.0 in lax mode
                        state = strict ? SERR_NOT_STRICT : SCOMPLETE;
                        break states;
                    }
                } // switch
                startPos = i; // will use this if we have to go back the slow way
                if (strict) {
                    state = SOPTDIGIT_STRICT;
                    break;
                }
                // drop through for non-strict digits
            case SOPTDIGIT:
                // first char must be digit or decimal point
                switch(ival = bytes[i++]) {
                case '0':
                    // ignore leading zeroes
                    break; // switch
                case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    significand = (long)((int)ival-(int)'0');
                    nDigits = 1;
                    break; // switch
                case '.':
                    state = SOPTDECDIGIT;
                    break states;
                default:
                    // no digits, go calc (will return +/- 0.0 for lax)
                    state = SOPTCALC;
                    break states;
                } // switch
                for ( ; i < buflen ;  ) {
                    switch(ival = bytes[i++]) {
                    case '0':
                        // ignore leading zeroes
                        if (nDigits > 0) {
                            // just save a count of zeroes for now; if no digit
                            // ends up following them, they'll be applied to the
                            // exponent rather than the significand (and our max
                            // length for optimistic calc).
                            nTrailingZeroes++;
                        }
                        break; // switch
                    case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                         // ok, got a non-zero, have to own up to our horded zeroes
                        if (nTrailingZeroes > 0) {
                            if ((nDigits += nTrailingZeroes) < MAX_DECIMAL_DIGITS) {
                                significand *= LONG_10_POWERS[nTrailingZeroes];
                                nTrailingZeroes = 0;
                            } // else catch oversize below
                        }
                        if (nDigits++ < MAX_DECIMAL_DIGITS) {
                            significand = significand*10L + (long)((int)ival-(int)'0');
                            break; // switch
                        } else {
                            // oh, well, it was worth a try. go let
                            // Double/FloatingDecimal handle it 
                            state = SDIGIT;
                            break states;
                        }
                    case '.':
                        state = SOPTDECDIGIT;
                        break states;
                    case 'e':
                    case 'E':
                        state = SOPTEXP;
                        break states;
                    case '_':
                        // ignore
                        break; // switch
                    default:
                        // end of parseable data, go to calc
                        state = SOPTCALC;
                        break states;
                        
                    } // switch
                } // for
                state = SOPTCALC;
                break;

            case SOPTDECDIGIT:
                decPos = nDigits + nTrailingZeroes;
                for ( ; i < buflen && bytes[i] == '_'; i++ ) ;
                // first non_underscore char must be digit
                if (i < buflen) {
                    switch(ival = bytes[i++]) {
                    case '0':
                        if (nDigits > 0) {
                            nTrailingZeroes++;
                        } else {
                            exponent--;
                        }
                        break; // switch
                    case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        if (nTrailingZeroes > 0) {
                            if ((nDigits += nTrailingZeroes) < MAX_DECIMAL_DIGITS) {
                                significand *= LONG_10_POWERS[nTrailingZeroes];
                                nTrailingZeroes = 0;
                            } // else catch oversize below
                        }
                        if (nDigits++ < MAX_DECIMAL_DIGITS) {
                            significand = significand*10L + (long)((int)ival-(int)'0');
                            break; // switch
                        } else {
                            state = SDIGIT;
                            break states;
                        }
                    default:
                        // no dec digits, end of parseable data, go to calc
                        state = SOPTCALC;
                        break states;
                        
                    } // switch
                } // if
                for ( ; i < buflen ; ) {
                    switch(ival = bytes[i++]) {
                    case '0':
                        if (nDigits > 0) {
                            nTrailingZeroes++;
                        } else {
                            exponent--;
                        }
                        break; // switch
                    case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        if (nTrailingZeroes > 0) {
                            if ((nDigits += nTrailingZeroes) < MAX_DECIMAL_DIGITS) {
                                significand *= LONG_10_POWERS[nTrailingZeroes];
                                nTrailingZeroes = 0;
                            } // else catch oversize below
                        }
                        if (nDigits++ < MAX_DECIMAL_DIGITS) {
                            significand = significand*10L + (long)((int)ival-(int)'0');
                            break; // switch
                        } else {
                            state = SDIGIT;
                            break states;
                        }
                    case 'e':
                    case 'E':
                        state = SOPTEXP;
                        break states;
                    case '_':
                        // ignore
                        break; // switch
                    default:
                        // end of parseable data, go to calc
                        state = SOPTCALC;
                        break states;
                    } // switch
                } // for
                // no exponent, so drop through for calculation
            case SOPTCALC:
                // calculation for simple (and typical) case,
                // adapted from sun.misc.FloatingDecimal
                if (nDigits == 0) {
                    if (i + 1 < buflen) {
                        if ((ival == 'n' || ival == 'N') && 
                                (bytes[i] == 'a' || bytes[i] == 'A') &&
                                (bytes[i+1] == 'n' || bytes[i+1] == 'N')) {
                            return Double.NaN;
                        } else if ((ival == 'i' || ival == 'I') && 
                                (bytes[i] == 'n' || bytes[i] == 'N') &&
                                (bytes[i+1] == 'f' || bytes[i+1] == 'F')) {
                            return negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                        }
                    }
                    return negative ? -0.0d : 0.0d;
                }
                if (decPos < 0) {
                    exponent += nTrailingZeroes;
                } else {
                    exponent += decPos - nDigits;
                }
                double dValue = (double)significand;
                if (exponent == 0 || dValue == 0.0) {
                    return negative ? -dValue : dValue;
                } else if ( exponent >= 0 ){
                    if ( exponent <= MAX_SMALL_10 ){
                        dValue *= SMALL_10_POWERS[exponent];
                        return negative ? -dValue : dValue;
                    }
                    int slop = MAX_DECIMAL_DIGITS - nDigits;
                    if ( exponent <= MAX_SMALL_10+slop ){
                        dValue = (dValue * SMALL_10_POWERS[slop]) * SMALL_10_POWERS[exponent-slop];
                        return negative ? -dValue : dValue;
                    }
                } else {
                    // TODO: it's not clear to me why, in FloatingDecimal, the
                    // "slop" calculation performed above for positive exponents
                    // isn't used for negative exponents as well. Will find out...
                    if ( exponent >= -MAX_SMALL_10 ){
                        dValue = dValue / SMALL_10_POWERS[-exponent];
                        return negative ? -dValue : dValue;
                    }
                }
                // difficult case, send to Double/FloatingDecimal
                state = SDIGIT;
                break;
                
            case SOPTEXP:
            {
                // lax (str.to_f) allows underscores between e/E and sign
                for ( ; i < buflen && bytes[i] == '_' ; i++ ) ;
                if (i >= buflen) {
                    state = SOPTCALC;
                    break;
                }
                int expSign = 1;
                int expSpec = 0;
                switch (bytes[i]) {
                case '-':
                    expSign = -1;
                case '+':
                    if (++i >= buflen) {
                        state = SOPTCALC;
                        break states;
                    }
                }
                for ( ; i < buflen ; ) {
                    switch(ival = bytes[i++]) {
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        if ((expSpec = expSpec * 10 + ((int)ival-(int)'0')) >= MAX_EXP) {
                            // too big for us
                            state = SDIGIT;
                            break states;
                        }
                        break; //switch
                    case '_':
                        break; //switch
                    default:
                        exponent += expSign * expSpec;
                        state = SOPTCALC;
                        break states;
                    }
                }
                exponent += expSign * expSpec;
                state = SOPTCALC;
                break;
            } // block

            case SOPTDIGIT_STRICT:
                // first char must be digit or decimal point
                switch(ival = bytes[i++]) {
                case '0':
                    break; // switch
                case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    significand = (long)((int)ival-(int)'0');
                    nDigits = 1;
                    break; // switch
                case '.':
                    state = SOPTDECDIGIT_STRICT;
                    break states;
                default:
                    // no digits, error
                    state = SERR_NOT_STRICT;
                    break states;
                }
                for ( ; i < buflen ;  ) {
                    switch(ival = bytes[i++]) {
                    case '0':
                        if (nDigits > 0) {
                            nTrailingZeroes++;
                        }
                        break; // switch
                    case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        if (nTrailingZeroes > 0) {
                            if ((nDigits += nTrailingZeroes) < MAX_DECIMAL_DIGITS) {
                                significand *= LONG_10_POWERS[nTrailingZeroes];
                                nTrailingZeroes = 0;
                            } // else catch oversize below
                        }
                        if (nDigits++ < MAX_DECIMAL_DIGITS) {
                            significand = significand*10L + (long)((int)ival-(int)'0');
                            break; // switch
                        } else {
                            state = SDIGIT;
                            break states;
                        }
                    case '.':
                        state = SOPTDECDIGIT_STRICT;
                        break states;
                    case 'e':
                    case 'E':
                        state = SOPTEXP_STRICT;
                        break states;
                    case '_':
                        if (i >= buflen || bytes[i] < '0' || bytes[i] > '9') {
                            state = SERR_NOT_STRICT;
                            break states;
                        }
                        break; // switch
                    default:
                        // only whitespace allowed after value for strict
                        for ( --i; i < buflen && isWhitespace(bytes[i]); i++ );
                        state = i < buflen ? SERR_NOT_STRICT : SOPTCALC; 
                        break states;
                    } // switch
                } // for
                // no more data, OK for strict to go calc
                state = SOPTCALC;
                break;

            case SOPTDECDIGIT_STRICT:
                decPos = nDigits + nTrailingZeroes;
                // first char must be digit
                if (i < buflen) {
                    switch(ival = bytes[i++]) {
                    case '0':
                        if (nDigits > 0) {
                            nTrailingZeroes++;
                        } else {
                            exponent--;
                        }
                        break; // switch
                    case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        if (nTrailingZeroes > 0) {
                            if ((nDigits += nTrailingZeroes) < MAX_DECIMAL_DIGITS) {
                                significand *= LONG_10_POWERS[nTrailingZeroes];
                                nTrailingZeroes = 0;
                            } // else catch oversize below
                        }
                        if (nDigits++ < MAX_DECIMAL_DIGITS) {
                            significand = significand*10L + (long)((int)ival-(int)'0');
                            break; // switch
                        } else {
                            state = SDIGIT;
                            break states;
                        }
                    default:
                        // no dec digits after '.', error for strict
                        state = SERR_NOT_STRICT;
                        break states;
                        
                    } // switch
                } else {
                    state = SERR_NOT_STRICT;
                    break;
                }
                for ( ; i < buflen ; ) {
                    switch(ival = bytes[i++]) {
                    case '0':
                        if (nDigits > 0) {
                            nTrailingZeroes++;
                        } else {
                            exponent--;
                        }
                        break; // switch
                    case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        if (nTrailingZeroes > 0) {
                            if ((nDigits += nTrailingZeroes) < MAX_DECIMAL_DIGITS) {
                                significand *= LONG_10_POWERS[nTrailingZeroes];
                                nTrailingZeroes = 0;
                            } // else catch oversize below
                        }
                        if (nDigits++ < MAX_DECIMAL_DIGITS) {
                            significand = significand*10L + (long)((int)ival-(int)'0');
                            break; // switch
                        } else {
                            state = SDIGIT;
                            break states;
                        }
                    case 'e':
                    case 'E':
                        state = SOPTEXP_STRICT;
                        break states;
                    case '_':
                        if (i >= buflen || bytes[i] < '0' || bytes[i] > '9') {
                            state = SERR_NOT_STRICT;
                            break states;
                        }
                        break; // switch
                    default:
                        // only whitespace allowed after value for strict
                        for ( --i; i < buflen && isWhitespace(bytes[i]); i++);
                        state = i < buflen ? SERR_NOT_STRICT : SOPTCALC; 
                        break states;
                    } // switch
                } // for
                // no more data, OK for strict to go calc
                state = SOPTCALC;
                break;
            
            case SOPTEXP_STRICT:
            {
                int expSign = 1;
                int expSpec = 0;

                if ( i < buflen) {
                    switch (bytes[i]) {
                    case '-':
                        expSign = -1;
                    case '+':
                        if (++i >= buflen) {
                            state = SERR_NOT_STRICT;
                            break states;
                        }
                    }
                } else {
                    state = SERR_NOT_STRICT;
                    break;
                }
                // must be at least one digit for strict
                if ( i < buflen ) {
                    switch(ival = bytes[i++]) {
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        expSpec = (int)ival-(int)'0';
                        break; //switch
                    default:
                        state = SERR_NOT_STRICT;
                        break states;
                    }
                } else {
                    state = SERR_NOT_STRICT;
                    break;
                }
                for ( ; i < buflen ; ) {
                    switch(ival = bytes[i++]) {
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        if ((expSpec = expSpec * 10 + ((int)ival-(int)'0')) >= MAX_EXP) {
                            // too big for us
                            state = SDIGIT;
                            break states;
                        }
                        break; //switch
                    case '_':
                        if (i >= buflen || bytes[i] < '0' || bytes[i] > '9') {
                            state = SERR_NOT_STRICT;
                            break states;
                        }
                        break; //switch
                    default:
                        exponent += expSign * expSpec;
                        // only whitespace allowed after value for strict
                        for ( --i; i < buflen && isWhitespace(bytes[i]);  i++);
                        state = i < buflen ? SERR_NOT_STRICT : SOPTCALC; 
                        break states;
                    } // switch
                } // for
                exponent += expSign * expSpec;
                state = SOPTCALC;
                break;
            } // block
                
            // fallback, copy non-whitespace chars to char buffer and send
            // to Double.parseDouble() (front for sun.misc.FloatingDecimal)
            case SDIGIT:
                i = startPos;
                if (negative) {
                    chars = new char[buflen - i + 1];
                    chars[0] = '-';
                    offset = 1;
                } else {
                    chars = new char[buflen - i];
                }
                if (strict) {
                    state = SDIGIT_STRICT;
                    break;
                }
                // first char must be digit or decimal point
                if (i < buflen) {
                    switch(ival = bytes[i++]) {
                    case '0':
                        // ignore leading zeroes
                        break; // switch
                    case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        chars[offset++] = (char)ival;
                        lastValidOffset = offset;
                        break; // switch
                    case '.':
                        state = SDECDIGIT;
                        break states;
                    default:
                        state = SCOMPLETE;
                        break states;
                    } // switch
                } // if
                for ( ; i < buflen ; ) {
                    switch(ival = bytes[i++]) {
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        chars[offset++] = (char)ival;
                        lastValidOffset = offset;
                        break; // switch
                    case '.':
                        state = SDECDIGIT;
                        break states;
                    case 'e':
                    case 'E':
                        state = SEXP;
                        break states;
                    case '_':
                        break; // switch
                    default:
                        state = SCOMPLETE;
                        break states;
                    } // switch
                } // for
                state = SCOMPLETE;
                break;
                
            case SDECDIGIT:
                chars[offset++] = '.';
                for ( ; i < buflen && bytes[i] == '_'; i++) ;
                if ( i < buflen) {
                    switch(ival = bytes[i++]) {
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        chars[offset++] = (char)ival;
                        lastValidOffset = offset;
                        break; // switch
                    default:
                        state = SCOMPLETE;
                        break states;
                    } // switch
                } // if
                for ( ; i < buflen ; ) {
                    switch(ival = bytes[i++]) {
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        chars[offset++] = (char)ival;
                        lastValidOffset = offset;
                        break; // switch
                    case 'e':
                    case 'E':
                        state = SEXP;
                        break states;
                    case '_':
                        break; // switch
                    default:
                        state = SCOMPLETE;
                        break states;
                    } // switch
                } // for
                state = SCOMPLETE;
                break;

            case SEXP:
                chars[offset++] = 'E';
                for ( ; i < buflen && bytes[i] == '_'; i++) ;
                if (i >= buflen) {
                    state = SCOMPLETE;
                    break;
                }
                switch(bytes[i]) {
                case '-':
                case '+':
                    chars[offset++] = (char)bytes[i];
                    if (++i >= buflen) {
                        state = SCOMPLETE;
                        break states;
                    }
                }
                for ( ; i < buflen; ) {
                    switch(ival = bytes[i++]) {
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        chars[offset++] = (char)ival;
                        lastValidOffset = offset;
                        break;
                    case '_':
                        break;
                    default:
                        state = SCOMPLETE;
                        break states;
                    }
                }
                state = SCOMPLETE;
                break;
            
            case SDIGIT_STRICT:
                // first char must be digit or decimal point
                if (i < buflen) {
                    switch(ival = bytes[i++]) {
                    case '0':
                        // ignore leading zeroes
                        break; // switch
                    case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        chars[offset++] = (char)ival;
                        lastValidOffset = offset;
                        break; // switch
                    case '.':
                        state = SDECDIGIT_STRICT;
                        break states;
                    default:
                        state = SERR_NOT_STRICT;
                        break states;
                    } // switch
                } else {
                    state = SERR_NOT_STRICT;
                    break;
                }
                for ( ; i < buflen ; ) {
                    switch(ival = bytes[i++]) {
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        chars[offset++] = (char)ival;
                        lastValidOffset = offset;
                        break; // switch
                    case '.':
                        state = SDECDIGIT_STRICT;
                        break states;
                    case 'e':
                    case 'E':
                        state = SEXP_STRICT;
                        break states;
                    case '_':
                        if (i >= buflen || bytes[i] < '0' || bytes[i] > '9') {
                            state = SERR_NOT_STRICT;
                            break states;
                        }
                        break; //switch
                    default:
                        // only whitespace allowed after value for strict
                        for ( --i; i < buflen && isWhitespace(bytes[i]);  i++) ;
                        state = i < buflen ? SERR_NOT_STRICT : SCOMPLETE; 
                        break states;
                    } // switch
                } // for
                state = SCOMPLETE;
                break;
                
            case SDECDIGIT_STRICT:
                chars[offset++] = '.';
                if ( i < buflen) {
                    switch(ival = bytes[i++]) {
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        chars[offset++] = (char)ival;
                        lastValidOffset = offset;
                        break; // switch
                    default:
                        state = SERR_NOT_STRICT;
                        break states;
                    } // switch
                } else {
                    state = SERR_NOT_STRICT;
                    break;
                }
                for ( ; i < buflen ; ) {
                    switch(ival = bytes[i++]) {
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        chars[offset++] = (char)ival;
                        lastValidOffset = offset;
                        break; // switch
                    case 'e':
                    case 'E':
                        state = SEXP_STRICT;
                        break states;
                    case '_':
                        if (i >= buflen || bytes[i] < '0' || bytes[i] > '9') {
                            state = SERR_NOT_STRICT;
                            break states;
                        }
                        break; //switch
                    default:
                        for ( --i; i < buflen && isWhitespace(bytes[i]);  i++) ;
                        state = i < buflen ? SERR_NOT_STRICT : SCOMPLETE; 
                        break states;
                    } // switch
                } // for
                state = SCOMPLETE;
                break;

            case SEXP_STRICT:
                chars[offset++] = 'E';
                if ( i < buflen ) {
                    switch (bytes[i]) {
                    case '-':
                    case '+':
                        chars[offset++] = (char)bytes[i];
                        if (++i >= buflen) {
                            state = SERR_NOT_STRICT;
                            break states;
                        }
                    }
                } else {
                    state = SERR_NOT_STRICT;
                    break;
                }
                // must be at least one digit for strict
                if ( i < buflen ) {
                    switch(ival = bytes[i++]) {
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        chars[offset++] = (char)ival;
                        lastValidOffset = offset;
                        break; //switch
                    default:
                        state = SERR_NOT_STRICT;
                        break states;
                    }
                } else {
                    state = SERR_NOT_STRICT;
                    break;
                }
                for ( ; i < buflen ; ) {
                    switch(ival = bytes[i++]) {
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        chars[offset++] = (char)ival;
                        lastValidOffset = offset;
                        break;
                    case '_':
                        if (i >= buflen || bytes[i] < '0' || bytes[i] > '9') {
                            state = SERR_NOT_STRICT;
                            break states;
                        }
                        break; //switch
                    default:
                        for ( --i; i < buflen && isWhitespace(bytes[i]);  i++) ;
                        state = i < buflen ? SERR_NOT_STRICT : SCOMPLETE; 
                        break states;
                    }
                }
                state = SCOMPLETE;
                break;
            
            case SERR_NOT_STRICT:
                throw new NumberFormatException("does not meet strict criteria");
                
            } // switch
        } //while
        if (chars == null || lastValidOffset == 0) {
            return 0.0;
        } else {
            return Double.parseDouble(new String(chars,0,lastValidOffset));
        }
    }

    public static final byte[] twosComplementToBinaryBytes(byte[] in) {
        retur
