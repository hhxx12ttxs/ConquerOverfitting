/**
 * Copyright (c) 2011, yoomai.net. All rights reserved.
 * yoomai.cn. Use is subject to license terms.
 */
package net.yoomai.util;

/**
 * @(#)StringUtils.java 1.0 14/09/2012
 */

import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * @author Ray
 * @version 1.0, 13/07/2011
 * @since 1.5
 */
public class StringUtils {
    private static char[] HEXCHAR = {'0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private final static SecureRandom sr;

    static {
        sr = new SecureRandom();
    }

    private StringUtils() {

    }

    /**
     * 通用MD5加密
     *
     * @param s
     * @return
     */
    public static String MD5(String s) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            byte[] strTemp = s.getBytes();
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(strTemp);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将字节数组转换成16进制字符串
     *
     * @param b
     * @return
     */
    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEXCHAR[(b[i] & 0xf0) >>> 4]);
            sb.append(HEXCHAR[b[i] & 0x0f]);
        }
        return sb.toString();
    }

    /**
     * 将字符串转换成16进制字节
     *
     * @param s
     * @return
     */
    public static byte[] toBytes(String s) {
        byte[] bytes;
        bytes = new byte[s.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2),
                    16);
        }
        return bytes;
    }

    /**
     * 字符串编码转换
     *
     * @param str  待转的字符串
     * @param from 字符串转码之前的编码
     * @param to   字符串转码之后的编码
     * @return 转码后的字符串
     */
    public static String iconv(String str, String from, String to) {
        String value;
        if (str == null || str.length() == 0) {
            return "";
        }

        try {
            value = new String(str.getBytes(from), to);
        } catch (Exception e) {
            return null;
        }
        return value;
    }

    /**
     * 产生若干位随机数字符串
     *
     * @param len 随机数位数
     * @return 随机数
     */
    public static synchronized String getUniqueID(int len) {
        if (len < 1) return null;
        final char[] alphabet = ("12345abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ67890").toCharArray();
        byte[] b = new byte[len];
        sr.nextBytes(b);
        char[] out = new char[len];
        for (int i = 0; i < len; i++) {
            int index = b[i] % alphabet.length;
            if (index < 0) index += alphabet.length;
            out[i] = alphabet[index];
        }
        return new String(out);
    }

    /**
     * 提供截取字符串部分信息
     *
     * @param original   待截取的字符串
     * @param split      截取时的分隔符
     * @param index      需要信息所对应的索引
     * @param nullString 提供默认值
     * @return 截取后的信息
     */
    public static String split(String original, String split, int index, String nullString) {
        String ret = nullString;

        String[] splited = original.split(split);
        if (index < splited.length) {
            ret = splited[index];
        }
        return ret;
    }

    /**
     * 给字符串不足相应位数时左边补位
     */
    public static String leftPadding(String orgStr, String addStr, int strLength) {
        if (orgStr.length() < strLength) {
            String padding = new String();
            int len = strLength - orgStr.length();
            for (int loop = 0; loop < len; loop++) {
                padding = padding + addStr;
            }
            padding = padding + orgStr;
            return padding;
        } else {
            return orgStr;
        }
    }
}

