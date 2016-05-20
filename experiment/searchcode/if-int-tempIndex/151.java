package com.jrails.commons.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 *
 * @author <a href="arden.emily@gmail.com">arden</a>
 */
public class StringUtils {
    protected static final Logger logger = LoggerFactory.getLogger(StringUtils.class);

    /**
     * @param str String
     * @return String
     */
    public static String isoToGBK(String str) {
        if (str == null) {
            return "";
        }
        try {
            byte[] bytes = str.getBytes("iso-8859-1");
            String destStr = new String(bytes, "GBK");
            return destStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 转换指定字符串的编码
     *
     * @param str
     * @param fromEncoding
     * @param toEncoding
     * @return
     */
    public static String convert(String str, String fromEncoding,
                                 String toEncoding) {
        if (str == null) {
            return "";
        }
        try {
            byte[] bytes = str.getBytes(fromEncoding);
            String destStr = new String(bytes, toEncoding);
            return destStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String toUnicode(java.lang.String text) {
        if (text == null)
            return "";
        char chars[] = text.toCharArray();
        java.lang.StringBuffer sb = new StringBuffer();
        int length = chars.length;
        for (int i = 0; i < length; i++) {
            int s = chars[i];
            sb.append("&#");
            sb.append(s);
            sb.append(";");
        }

        return sb.toString();
    }

    /**
     * 检测字符串里是否有中文字符
     *
     * @param str
     * @return
     */
    public static boolean chinese(String str) {
        if (str == null) {
            return false;
        }
        String regex = "[\u0391-\uFFE5]+";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        boolean validate = m.matches();
        return validate;
    }

    /**
     * 检测输入的邮政编码是否合法
     *
     * @param code
     * @return
     */
    public static boolean isPostCode(String code) {
        if (code == null) {
            return false;
        }
        String regex = "[1-9]\\d{5}";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(code);
        boolean validate = m.matches();
        return validate;
    }

    /**
     * 检测字符串是否为空，或者空字符串
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        str = StringUtils.nullStringToEmptyString(str);
        String regex = "\\s*";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        boolean validate = m.matches();
        return validate;
    }

    /**
     * 字符串是否是"nul"字符串
     *
     * @param str
     * @return
     */
    public static boolean isNull(String str) {
        if (str == null && str.equals("null")) {
            return true;
        }
        return false;
    }

    /**
     * 将"null"字符串或者null值转换成""
     *
     * @param str
     * @return
     */
    public static String nullStringToEmptyString(String str) {
        if (str == null) {
            str = "";
        }
        if (str.equals("null")) {
            str = "";
        }
        return str;
    }

    /**
     * 将"null"字符串或者null值转换成""
     *
     * @param str
     * @return
     */
    public static String nullStringToSetString(String str) {
        if (StringUtils.isEmpty(str)) {
            str = "设置";
        }
        if (str == null) {
            str = "设置";
        }
        if (str.equals("null")) {
            str = "设置";
        }
        return str;
    }

    /**
     * 将"null"字符串或者null值转换成""
     *
     * @param str
     * @return
     */
    public static String nullStringToUnknowString(String str) {
        if (str == null) {
            str = "未知";
        }
        if (str.equals("null")) {
            str = "未知";
        }
        return str;
    }

    /**
     * 屏掉WML不支持的代码
     *
     * @param str
     * @return
     */
    public static String wmlEncode(String str) {
        if (str == null)
            return "";
        str = str.replaceAll("&", "&amp;");
        str = str.replaceAll("<", "&lt;");
        str = str.replaceAll(">", "&gt;");
        str = str.replaceAll("'", "&apos;");
        str = str.replaceAll("\"", "&quot;");
        str = str.replaceAll("\n", "<br/>");
        str = str.replaceAll("<br>", "<br/>");
        return str;
    }

    /**
     * 将字节转换成16进制
     *
     * @param b byte[]
     * @return String
     */
    public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }

    /**
     * 是否是数字
     *
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        str = StringUtils.nullStringToEmptyString(str);
        String regex = "\\d+";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        boolean validate = m.matches();
        return validate;
    }

    /**
     * 检查书的ISBN号是否合法
     *
     * @param isbn
     * @return
     */
    public static boolean isISBN(String isbn) {
        if (StringUtils.isEmpty(isbn)) {
            return false;
        }
        int len = isbn.length();
        if (len != 13) {
            return false;
        }
        String[] splits = isbn.split("-");
        len = splits.length;
        if (len != 4) {
            return false;
        }
        len = splits[0].length();
        if (len < 1 || len > 5) {
            return false;
        }
        len = splits[1].length();
        if (len < 2 || len > 5) {
            return false;
        }
        len = splits[2].length();
        if (len < 1 || len > 6) {
            return false;
        }
        len = splits[3].length();
        if (len != 1) {
            return false;
        }
        String realISBN = isbn.replaceAll("-", "");
        char[] numbers = realISBN.toCharArray();
        int sum = 0;
        for (int i = 10; i > 1; i--) {
            int index = 10 - i;
            int number = Integer.parseInt(String.valueOf(numbers[index]));
            sum = sum + number * i;
        }
        int code = 11 - (sum % 11);
        String codeStr = String.valueOf(code);
        if (code == 10) {
            codeStr = "X";
        }
        if (!splits[3].equals(codeStr)) {
            return false;
        }
        return true;
    }

    public static String getUUID() {
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.replaceAll("-", "");
        return uuid;
    }

    public static String substring(String str, int start, int length) {
        int len = str.length();
        if (len > 15) {
            str = str.substring(start, length);
        }
        str = str + "......";
        return str;
    }

    /**
     * Encode a string using algorithm specified in web.xml and return the
     * resulting encrypted password. If exception, the plain credentials string is
     * returned
     *
     * @param password  Password or other credentials to use in authenticating this
     *                  username
     * @param algorithm Algorithm used to do the digest
     * @return encypted password based on the algorithm.
     */
    public static String encodePassword(String password, String algorithm) {
        byte[] unencodedPassword = password.getBytes();

        MessageDigest md = null;

        try {
            // first create an instance, given the provider
            md = MessageDigest.getInstance(algorithm);
        } catch (Exception e) {
            logger.error("Exception: " + e);

            return password;
        }

        md.reset();

        // call the update method one or more times
        // (useful when you don't know the size of your data, eg. stream)
        md.update(unencodedPassword);

        // now calculate the hash
        byte[] encodedPassword = md.digest();

        StringBuffer buf = new StringBuffer();

        for (byte anEncodedPassword : encodedPassword) {
            if ((anEncodedPassword & 0xff) < 0x10) {
                buf.append("0");
            }

            buf.append(Long.toString(anEncodedPassword & 0xff, 16));
        }

        return buf.toString();
    }

    /**
     * Encode a string using Base64 encoding. Used when storing passwords as
     * cookies.
     * <p/>
     * This is weak encoding in that anyone can use the decodeString routine to
     * reverse the encoding.
     *
     * @param str
     * @return String
     */
    public static String encodeString(String str) {
        Base64 encoder = new Base64();
        return String.valueOf(encoder.encode(str.getBytes())).trim();
    }

    /**
     * Decode a string using Base64 encoding.
     *
     * @param str
     * @return String
     */
    public static String decodeString(String str) {
        Base64 dec = new Base64();
        try {
            return String.valueOf(dec.decode(str));
        } catch (DecoderException de) {
            throw new RuntimeException(de.getMessage(), de.getCause());
        }
    }

    /**
     * 字符串替换
     * @param text
     * @param start
     * @param end
     * @param replacement
     * @return
     */
    public static String replace(String text, int start, int end, String replacement) {
        int len = text.length();
        if (start < len && end <= len && start > 0 && end > 0) {
            String part1 = text.substring(0, start);
            String part2 = text.substring(end);
            return part1 + replacement + part2;
        } else {
            return text;    
        }
    }

    /**
     * 字符串替换
     * @param text
     * @param start
     * @param end
     * @param replacement
     * @return
     */
    public static String replace(String text, int start, int end, char replacement) {
        char[] chars = text.toCharArray();
        int len = text.length();
        String tempText = "";
        if (start < len && end <= len && start > 0 && end > 0) {
            for (int i = start; i <= end; i++) {
                int theIndex = Integer.valueOf(i);
                if (theIndex > 0 && theIndex < text.length()) {
                    chars[theIndex] = replacement;
                }
            }
        }

        for (char c : chars) {
            tempText += c;
        }
        return tempText;
    }

    /**
     * 字符串替换
     * @param text
     * @param index
     * @param replacement
     * @return
     */
    public static String replace(String text, String index, char replacement) {
        char[] chars = text.toCharArray();
        String[] tempIndex = index.split(",");
        String tempText = "";
        for (String i : tempIndex) {
            int theIndex = Integer.valueOf(i);
            if (theIndex > 0 && theIndex < text.length()) {
                chars[theIndex] = replacement;
            }
        }

        for (char c : chars) {
            tempText += c;
        }
        return tempText;
    }

    /**
     * 把键值对的字符串写入HashMap（如：{ip=211.136.20.44, softid=, cid=ad.ucweb, date=2009-04-14:11:59:56, mid=, userAgent=-, page=/soft/system/sort})
     * @param str
     * @return
     */
    public static Map<String,String> loadStrToMap(String str) {
        if (!StringUtils.isEmpty(str)) {
            Map<String, String> valueMap = new WeakHashMap<String, String>();
            if (str.startsWith("{") && str.endsWith("}")) {
                str = str.substring(1, str.length() - 1);
            }
            String[] splits = str.split(",");
            for (String s : splits) {
                String[] tempSplits = s.split("=");
                if (tempSplits != null && tempSplits.length == 2) {
                    String key = tempSplits[0].trim();
                    String value = tempSplits[1];
                    valueMap.put(key, value);
                }
            }
            return valueMap;
        }
       return null; 
    }

    /**
     * 字符串替换
     * @param text
     * @param replacement
     * @return
     */
    public static String replace(String text, String[] replacement) {
        return text;
    }

    public static void main(String... args) {
        System.out.println(StringUtils.isNumber("22ss"));
	}
}
