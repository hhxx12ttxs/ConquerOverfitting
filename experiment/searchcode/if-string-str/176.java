/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2006 All Rights Reserved.
 */
package com.sohugame.sxl.util;

/**
 * ĺ­çŹŚä¸˛ĺ¤ççĺˇĽĺˇçą?
 * @author stone.zhangjl
 * @version $Id: StringUtil.java, v 0.1 2008-8-21 ä¸ĺ10:47:41 stone.zhangjl Exp $
 */
public class StringUtil {

    /** çŠşĺ­çŹŚä¸˛ă?*/
    public static final String EMPTY_STRING = "";

    /**
     * ćŻčžä¸¤ä¸Şĺ­çŹŚä¸˛ďźĺ¤§ĺ°ĺććďźă?
     * <pre>
     * StringUtil.equals(null, null)   = true
     * StringUtil.equals(null, "abc")  = false
     * StringUtil.equals("abc", null)  = false
     * StringUtil.equals("abc", "abc") = true
     * StringUtil.equals("abc", "ABC") = false
     * </pre>
     *
     * @param str1 čŚćŻčžçĺ­çŹŚä¸?
     * @param str2 čŚćŻčžçĺ­çŹŚä¸?
     *
     * @return ĺŚćä¸¤ä¸Şĺ­çŹŚä¸˛ç¸ĺďźćč?é˝ćŻ<code>null</code>ďźĺčżĺ<code>true</code>
     */
    public static boolean equals(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        }

        return str1.equals(str2);
    }

    /**
     * ćŻčžä¸¤ä¸Şĺ­çŹŚä¸˛ďźĺ¤§ĺ°ĺä¸ććďźă?
     * <pre>
     * StringUtil.equalsIgnoreCase(null, null)   = true
     * StringUtil.equalsIgnoreCase(null, "abc")  = false
     * StringUtil.equalsIgnoreCase("abc", null)  = false
     * StringUtil.equalsIgnoreCase("abc", "abc") = true
     * StringUtil.equalsIgnoreCase("abc", "ABC") = true
     * </pre>
     *
     * @param str1 čŚćŻčžçĺ­çŹŚä¸?
     * @param str2 čŚćŻčžçĺ­çŹŚä¸?
     *
     * @return ĺŚćä¸¤ä¸Şĺ­çŹŚä¸˛ç¸ĺďźćč?é˝ćŻ<code>null</code>ďźĺčżĺ<code>true</code>
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        }

        return str1.equalsIgnoreCase(str2);
    }

    /**
     * ćŁ?Ľĺ­çŹŚä¸˛ćŻĺŚćŻçŠşç˝ďź?code>null</code>ăçŠşĺ­çŹŚä¸?code>""</code>ćĺŞćçŠşç˝ĺ­çŹŚă?
     * <pre>
     * StringUtil.isBlank(null)      = true
     * StringUtil.isBlank("")        = true
     * StringUtil.isBlank(" ")       = true
     * StringUtil.isBlank("bob")     = false
     * StringUtil.isBlank("  bob  ") = false
     * </pre>
     *
     * @param str čŚćŁćĽçĺ­çŹŚä¸?
     *
     * @return ĺŚćä¸şçŠşç? ĺčżĺ?code>true</code>
     */
    public static boolean isBlank(String str) {
        int length;

        if ((str == null) || ((length = str.length()) == 0)) {
            return true;
        }

        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * ćŁ?Ľĺ­çŹŚä¸˛ćŻĺŚä¸ćŻçŠşç˝ďź<code>null</code>ăçŠşĺ­çŹŚä¸?code>""</code>ćĺŞćçŠşç˝ĺ­çŹŚă?
     * <pre>
     * StringUtil.isBlank(null)      = false
     * StringUtil.isBlank("")        = false
     * StringUtil.isBlank(" ")       = false
     * StringUtil.isBlank("bob")     = true
     * StringUtil.isBlank("  bob  ") = true
     * </pre>
     *
     * @param str čŚćŁćĽçĺ­çŹŚä¸?
     *
     * @return ĺŚćä¸şçŠşç? ĺčżĺ?code>true</code>
     */
    public static boolean isNotBlank(String str) {
        int length;

        if ((str == null) || ((length = str.length()) == 0)) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }

        return false;
    }

    /**
     * ćŁ?Ľĺ­çŹŚä¸˛ćŻĺŚä¸ş<code>null</code>ćçŠşĺ­çŹŚä¸?code>""</code>ă?
     * <pre>
     * StringUtil.isEmpty(null)      = true
     * StringUtil.isEmpty("")        = true
     * StringUtil.isEmpty(" ")       = false
     * StringUtil.isEmpty("bob")     = false
     * StringUtil.isEmpty("  bob  ") = false
     * </pre>
     *
     * @param str čŚćŁćĽçĺ­çŹŚä¸?
     *
     * @return ĺŚćä¸şçŠş, ĺčżĺ?code>true</code>
     */
    public static boolean isEmpty(String str) {
        return ((str == null) || (str.length() == 0));
    }

    /**
     * ćŁ?Ľĺ­çŹŚä¸˛ćŻĺŚä¸ć?code>null</code>ĺçŠşĺ­çŹŚä¸?code>""</code>ă?
     * <pre>
     * StringUtil.isEmpty(null)      = false
     * StringUtil.isEmpty("")        = false
     * StringUtil.isEmpty(" ")       = true
     * StringUtil.isEmpty("bob")     = true
     * StringUtil.isEmpty("  bob  ") = true
     * </pre>
     *
     * @param str čŚćŁćĽçĺ­çŹŚä¸?
     *
     * @return ĺŚćä¸ä¸şçŠ? ĺčżĺ?code>true</code>
     */
    public static boolean isNotEmpty(String str) {
        return ((str != null) && (str.length() > 0));
    }

    /**
     * ĺ¨ĺ­çŹŚä¸˛ä¸­ćĽćžćĺŽĺ­çŹŚä¸˛ďźĺšśčżĺçŹŹä¸ä¸Şĺšéçç´˘ĺźĺźă?ĺŚćĺ­çŹŚä¸˛ä¸ş<code>null</code>ććŞćžĺ°ďźĺčżĺ<code>-1</code>ă?
     * <pre>
     * StringUtil.indexOf(null, *)          = -1
     * StringUtil.indexOf(*, null)          = -1
     * StringUtil.indexOf("", "")           = 0
     * StringUtil.indexOf("aabaabaa", "a")  = 0
     * StringUtil.indexOf("aabaabaa", "b")  = 2
     * StringUtil.indexOf("aabaabaa", "ab") = 1
     * StringUtil.indexOf("aabaabaa", "")   = 0
     * </pre>
     *
     * @param str čŚćŤćçĺ­çŹŚä¸?
     * @param searchStr čŚćĽćžçĺ­çŹŚä¸?
     *
     * @return çŹŹä¸ä¸Şĺšéçç´˘ĺźĺźă?ĺŚćĺ­çŹŚä¸˛ä¸ş<code>null</code>ććŞćžĺ°ďźĺčżĺ<code>-1</code>
     */
    public static int indexOf(String str, String searchStr) {
        if ((str == null) || (searchStr == null)) {
            return -1;
        }

        return str.indexOf(searchStr);
    }

    /**
     * ĺ¨ĺ­çŹŚä¸˛ä¸­ćĽćžćĺŽĺ­çŹŚä¸˛ďźĺšśčżĺçŹŹä¸ä¸Şĺšéçç´˘ĺźĺźă?ĺŚćĺ­çŹŚä¸˛ä¸ş<code>null</code>ććŞćžĺ°ďźĺčżĺ<code>-1</code>ă?
     * <pre>
     * StringUtil.indexOf(null, *, *)          = -1
     * StringUtil.indexOf(*, null, *)          = -1
     * StringUtil.indexOf("", "", 0)           = 0
     * StringUtil.indexOf("aabaabaa", "a", 0)  = 0
     * StringUtil.indexOf("aabaabaa", "b", 0)  = 2
     * StringUtil.indexOf("aabaabaa", "ab", 0) = 1
     * StringUtil.indexOf("aabaabaa", "b", 3)  = 5
     * StringUtil.indexOf("aabaabaa", "b", 9)  = -1
     * StringUtil.indexOf("aabaabaa", "b", -1) = 2
     * StringUtil.indexOf("aabaabaa", "", 2)   = 2
     * StringUtil.indexOf("abc", "", 9)        = 3
     * </pre>
     *
     * @param str čŚćŤćçĺ­çŹŚä¸?
     * @param searchStr čŚćĽćžçĺ­çŹŚä¸?
     * @param startPos ĺź?§ćç´˘çç´˘ĺźĺ?ďźĺŚćĺ°äş?ďźĺçä˝0
     *
     * @return çŹŹä¸ä¸Şĺšéçç´˘ĺźĺźă?ĺŚćĺ­çŹŚä¸˛ä¸ş<code>null</code>ććŞćžĺ°ďźĺčżĺ<code>-1</code>
     */
    public static int indexOf(String str, String searchStr, int startPos) {
        if ((str == null) || (searchStr == null)) {
            return -1;
        }

        // JDK1.3ĺäťĽä¸çćŹçbugďźä¸č˝ć­ŁçĄŽĺ¤çä¸é˘çćĺľ
        if ((searchStr.length() == 0) && (startPos >= str.length())) {
            return str.length();
        }

        return str.indexOf(searchStr, startPos);
    }

    /**
     * ĺćĺŽĺ­çŹŚä¸˛çĺ­ä¸˛ă?
     * 
     * <p>
     * č´çç´˘ĺźäťŁčĄ¨äťĺ°žé¨ĺźĺ§čŽĄçŽă?ĺŚćĺ­çŹŚä¸˛ä¸ş<code>null</code>ďźĺčżĺ<code>null</code>ă?
     * <pre>
     * StringUtil.substring(null, *, *)    = null
     * StringUtil.substring("", * ,  *)    = "";
     * StringUtil.substring("abc", 0, 2)   = "ab"
     * StringUtil.substring("abc", 2, 0)   = ""
     * StringUtil.substring("abc", 2, 4)   = "c"
     * StringUtil.substring("abc", 4, 6)   = ""
     * StringUtil.substring("abc", 2, 2)   = ""
     * StringUtil.substring("abc", -2, -1) = "b"
     * StringUtil.substring("abc", -4, 2)  = "ab"
     * </pre>
     * </p>
     *
     * @param str ĺ­çŹŚä¸?
     * @param start čľˇĺ§ç´˘ĺźďźĺŚćä¸şč´ć°ďźčĄ¨ç¤şäťĺ°žé¨čŽĄçŽ
     * @param end çťćç´˘ĺźďźä¸ĺŤďźďźĺŚćä¸şč´ć°ďźčĄ¨ç¤şäťĺ°žé¨čŽĄçŽ
     *
     * @return ĺ­ä¸˛ďźĺŚćĺĺ§ä¸˛ä¸?code>null</code>ďźĺčżĺ<code>null</code>
     */
    public static String substring(String str, int start, int end) {
        if (str == null) {
            return null;
        }

        if (end < 0) {
            end = str.length() + end;
        }

        if (start < 0) {
            start = str.length() + start;
        }

        if (end > str.length()) {
            end = str.length();
        }

        if (start > end) {
            return EMPTY_STRING;
        }

        if (start < 0) {
            start = 0;
        }

        if (end < 0) {
            end = 0;
        }

        return str.substring(start, end);
    }

    /**
     * ćŁ?Ľĺ­çŹŚä¸˛ä¸­ćŻĺŚĺĺŤćĺŽçĺ­çŹŚä¸˛ăĺŚćĺ­çŹŚä¸˛ä¸?code>null</code>ďźĺ°čżĺ<code>false</code>ă?
     * <pre>
     * StringUtil.contains(null, *)     = false
     * StringUtil.contains(*, null)     = false
     * StringUtil.contains("", "")      = true
     * StringUtil.contains("abc", "")   = true
     * StringUtil.contains("abc", "a")  = true
     * StringUtil.contains("abc", "z")  = false
     * </pre>
     *
     * @param str čŚćŤćçĺ­çŹŚä¸?
     * @param searchStr čŚćĽćžçĺ­çŹŚä¸?
     *
     * @return ĺŚććžĺ°ďźĺčżĺ<code>true</code>
     */
    public static boolean contains(String str, String searchStr) {
        if ((str == null) || (searchStr == null)) {
            return false;
        }

        return str.indexOf(searchStr) >= 0;
    }

    /**
     * <p>Checks if the String contains only unicode digits.
     * A decimal point is not a unicode digit and returns false.</p>
     *
     * <p><code>null</code> will return <code>false</code>.
     * An empty String ("") will return <code>true</code>.</p>
     *
     * <pre>
     * StringUtils.isNumeric(null)   = false
     * StringUtils.isNumeric("")     = true
     * StringUtils.isNumeric("  ")   = false
     * StringUtils.isNumeric("123")  = true
     * StringUtils.isNumeric("12 3") = false
     * StringUtils.isNumeric("ab2c") = false
     * StringUtils.isNumeric("12-3") = false
     * StringUtils.isNumeric("12.3") = false
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if only contains digits, and is non-null
     */
    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        int sz = str.length();
        for (int i = 0; i < sz; i++) {
            if (Character.isDigit(str.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

}

