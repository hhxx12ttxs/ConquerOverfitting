// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) nonlb 

package cn.kuaipan.kss.utils;

import java.security.MessageDigest;

public class Encode {

    public Encode() {
    }

    public static String MD5Encode(byte abyte0[]) {
        String s1 = byteArrayToHexString(MessageDigest.getInstance("MD5").digest(abyte0));
        String s = s1;
_L2:
        return s;
        Exception exception;
        exception;
        exception.printStackTrace();
        s = null;
        if(true) goto _L2; else goto _L1
_L1:
    }

    public static String SHA1Encode(byte abyte0[]) {
        String s1 = byteArrayToHexString(MessageDigest.getInstance("sha1").digest(abyte0));
        String s = s1;
_L2:
        return s;
        Exception exception;
        exception;
        exception.printStackTrace();
        s = null;
        if(true) goto _L2; else goto _L1
_L1:
    }

    public static String byteArrayToHexString(byte abyte0[]) {
        String s;
        if(abyte0 == null) {
            s = null;
        } else {
            StringBuffer stringbuffer = new StringBuffer(2 * abyte0.length);
            for(int i = 0; i < abyte0.length; i++) {
                stringbuffer.append(HEXDIGITS[0xf & abyte0[i] >>> 4]);
                stringbuffer.append(HEXDIGITS[0xf & abyte0[i]]);
            }

            s = stringbuffer.toString();
        }
        return s;
    }

    public static int byteArrayToInt(byte abyte0[], int i) {
        return (0xff & abyte0[i]) << 24 | (0xff & abyte0[i + 1]) << 16 | (0xff & abyte0[i + 2]) << 8 | 0xff & abyte0[i + 3];
    }

    public static long byteArrayToLong(byte abyte0[], int i) {
        int j = i + 8;
        long l = abyte0[i];
        for(int k = i + 1; k < j; k++)
            l = l << 8 | (long)(0xff & abyte0[k]);

        return l;
    }

    public static short byteArrayToShort(byte abyte0[], int i) {
        return (short)(abyte0[i] << 8 | 0xff & abyte0[i + 1]);
    }

    public static String byteToHexString(byte byte0) {
        return (new StringBuilder()).append(HEXDIGITS[0xf & byte0 >>> 4]).append(HEXDIGITS[byte0 & 0xf]).toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        byte abyte0[] = new byte[s.length() / 2];
        for(int i = 0; i < abyte0.length; i++)
            abyte0[i] = (byte)(16 * Character.digit(s.charAt(i * 2), 16) + Character.digit(s.charAt(1 + i * 2), 16));

        return abyte0;
    }

    public static String intToHexString(int i) {
        byte abyte0[] = new byte[4];
        abyte0[0] = (byte)(0xff & i >> 24);
        abyte0[1] = (byte)(0xff & i >> 16);
        abyte0[2] = (byte)(0xff & i >> 8);
        abyte0[3] = (byte)(i & 0xff);
        return byteArrayToHexString(abyte0);
    }

    public static String longToHexString(long l) {
        byte abyte0[] = new byte[8];
        for(int i = 0; i < 8; i++)
            abyte0[i] = (byte)(int)(255L & l >> 8 * (7 - i));

        return byteArrayToHexString(abyte0);
    }

    static final String HEXDIGITS[];

    static  {
        String as[] = new String[16];
        as[0] = "0";
        as[1] = "1";
        as[2] = "2";
        as[3] = "3";
        as[4] = "4";
        as[5] = "5";
        as[6] = "6";
        as[7] = "7";
        as[8] = "8";
        as[9] = "9";
        as[10] = "a";
        as[11] = "b";
        as[12] = "c";
        as[13] = "d";
        as[14] = "e";
        as[15] = "f";
        HEXDIGITS = as;
    }
}

