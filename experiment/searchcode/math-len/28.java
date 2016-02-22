package org.nutz.lang;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.meta.Email;

/**
 * ??????????
 * 
 * @author zozoh(zozohtnt@gmail.com)
 * @author wendal(wendal1985@gmail.com)
 * @author mawm(ming300@gmail.com)
 * @author bonyfish(mc02cxj@gmail.com)
 */
public abstract class Strings {

    /**
     * ?????
     * 
     * @param cs
     *            ???
     * @param num
     *            ??
     * @return ????
     */
    public static String dup(CharSequence cs, int num) {
        if (isEmpty(cs) || num <= 0)
            return "";
        StringBuilder sb = new StringBuilder(cs.length() * num);
        for (int i = 0; i < num; i++)
            sb.append(cs);
        return sb.toString();
    }

    /**
     * ????
     * 
     * @param c
     *            ??
     * @param num
     *            ??
     * @return ????
     */
    public static String dup(char c, int num) {
        if (c == 0 || num < 1)
            return "";
        StringBuilder sb = new StringBuilder(num);
        for (int i = 0; i < num; i++)
            sb.append(c);
        return sb.toString();
    }

    /**
     * ?????????
     * 
     * @param s
     *            ???
     * @return ???????????
     */
    public static String capitalize(CharSequence s) {
        if (null == s)
            return null;
        int len = s.length();
        if (len == 0)
            return "";
        char char0 = s.charAt(0);
        if (Character.isUpperCase(char0))
            return s.toString();
        return new StringBuilder(len).append(Character.toUpperCase(char0))
                                     .append(s.subSequence(1, len))
                                     .toString();
    }

    /**
     * ?????????
     * 
     * @param s
     *            ???
     * @return ???????????
     */
    public static String lowerFirst(CharSequence s) {
        if (null == s)
            return null;
        int len = s.length();
        if (len == 0)
            return "";
        char c = s.charAt(0);
        if (Character.isLowerCase(c))
            return s.toString();
        return new StringBuilder(len).append(Character.toLowerCase(c))
                                     .append(s.subSequence(1, len))
                                     .toString();
    }

    /**
     * ??????????????????.
     * 
     * @param s1
     *            ???A
     * @param s2
     *            ???B
     * @return true ???????????????,?????????null
     */
    public static boolean equalsIgnoreCase(String s1, String s2) {
        return s1 == null ? s2 == null : s1.equalsIgnoreCase(s2);
    }

    /**
     * ???????????.
     * 
     * @param s1
     *            ???A
     * @param s2
     *            ???B
     * @return true ?????????,?????????null
     */
    public static boolean equals(String s1, String s2) {
        return s1 == null ? s2 == null : s1.equals(s2);
    }

    /**
     * ??????????????
     * 
     * @param s
     *            ???
     * @param c
     *            ????
     * @return ?????????
     */
    public static boolean startsWithChar(String s, char c) {
        return null != s ? (s.length() == 0 ? false : s.charAt(0) == c) : false;
    }

    /**
     * ??????????????
     * 
     * @param s
     *            ???
     * @param c
     *            ????
     * @return ?????????
     */
    public static boolean endsWithChar(String s, char c) {
        return null != s ? (s.length() == 0 ? false : s.charAt(s.length() - 1) == c) : false;
    }

    /**
     * @param cs
     *            ???
     * @return ????????
     */
    public static boolean isEmpty(CharSequence cs) {
        return null == cs || cs.length() == 0;
    }

    /**
     * @param cs
     *            ???
     * @return ?????????
     */
    public static boolean isBlank(CharSequence cs) {
        if (null == cs)
            return true;
        int length = cs.length();
        for (int i = 0; i < length; i++) {
            if (!(Character.isWhitespace(cs.charAt(i))))
                return false;
        }
        return true;
    }

    /**
     * ?????????
     * 
     * @param cs
     *            ???
     * @return ????
     */
    public static String trim(CharSequence cs) {
        if (null == cs)
            return null;
        if (cs instanceof String)
            return ((String) cs).trim();
        int length = cs.length();
        if (length == 0)
            return cs.toString();
        int l = 0;
        int last = length - 1;
        int r = last;
        for (; l < length; l++) {
            if (!Character.isWhitespace(cs.charAt(l)))
                break;
        }
        for (; r > l; r--) {
            if (!Character.isWhitespace(cs.charAt(r)))
                break;
        }
        if (l > r)
            return "";
        else if (l == 0 && r == last)
            return cs.toString();
        return cs.subSequence(l, r + 1).toString();
    }

    /**
     * ????????? "xxx...xxx" ??????
     * 
     * @param str
     *            ???
     * @param len
     *            ????
     * @return ??????
     */
    public static String brief(String str, int len) {
        if (Strings.isBlank(str) || (str.length() + 3) <= len)
            return str;
        int w = len / 2;
        int l = str.length();
        return str.substring(0, len - w) + " ... " + str.substring(l - w);
    }

    /**
     * ???????????????????????
     * 
     * @param s
     *            ???
     * @return ?????
     */
    public static String[] splitIgnoreBlank(String s) {
        return Strings.splitIgnoreBlank(s, ",");
    }

    /**
     * ?????????????????????????
     * 
     * @param s
     *            ???
     * @param regex
     *            ???
     * @return ?????
     */
    public static String[] splitIgnoreBlank(String s, String regex) {
        if (null == s)
            return null;
        String[] ss = s.split(regex);
        List<String> list = new LinkedList<String>();
        for (String st : ss) {
            if (isBlank(st))
                continue;
            list.add(trim(st));
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * ????????????????????????????
     * 
     * @param d
     *            ??
     * @param width
     *            ??
     * @return ????
     */
    public static String fillDigit(int d, int width) {
        return Strings.alignRight(String.valueOf(d), width, '0');
    }

    /**
     * ?????????????????????????????
     * 
     * @param d
     *            ??
     * @param width
     *            ??
     * @return ????
     */
    public static String fillHex(int d, int width) {
        return Strings.alignRight(Integer.toHexString(d), width, '0');
    }

    /**
     * ????????????????????????????
     * 
     * @param d
     *            ??
     * @param width
     *            ??
     * @return ????
     */
    public static String fillBinary(int d, int width) {
        return Strings.alignRight(Integer.toBinaryString(d), width, '0');
    }

    /**
     * ?????????????????????
     * 
     * @param d
     *            ??
     * @param width
     *            ??
     * @return ????
     */
    public static String toDigit(int d, int width) {
        return Strings.cutRight(String.valueOf(d), width, '0');
    }

    /**
     * ??????????????????????
     * 
     * @param d
     *            ??
     * @param width
     *            ??
     * @return ????
     */
    public static String toHex(int d, int width) {
        return Strings.cutRight(Integer.toHexString(d), width, '0');
    }

    /**
     * ?????????????????????
     * 
     * @param d
     *            ??
     * @param width
     *            ??
     * @return ????
     */
    public static String toBinary(int d, int width) {
        return Strings.cutRight(Integer.toBinaryString(d), width, '0');
    }

    /**
     * ??????????????????????????
     * 
     * @param s
     *            ???
     * @param width
     *            ??
     * @param c
     *            ???
     * @return ???????
     */
    public static String cutRight(String s, int width, char c) {
        if (null == s)
            return null;
        int len = s.length();
        if (len == width)
            return s;
        if (len < width)
            return Strings.dup(c, width - len) + s;
        return s.substring(len - width, len);
    }

    /**
     * ?????????????????
     * 
     * @param o
     *            ?? toString ???
     * @param width
     *            ????
     * @param c
     *            ??
     * @return ????
     */
    public static String alignRight(Object o, int width, char c) {
        if (null == o)
            return null;
        String s = o.toString();
        int len = s.length();
        if (len >= width)
            return s;
        return new StringBuilder().append(dup(c, width - len)).append(s).toString();
    }

    /**
     * ?????????????????
     * 
     * @param o
     *            ?? toString ???
     * @param width
     *            ????
     * @param c
     *            ??
     * @return ????
     */
    public static String alignLeft(Object o, int width, char c) {
        if (null == o)
            return null;
        String s = o.toString();
        int length = s.length();
        if (length >= width)
            return s.toString();
        return new StringBuilder().append(s).append(dup(c, width - length)).toString();
    }

    /**
     * @param cs
     *            ???
     * @param lc
     *            ???
     * @param rc
     *            ???
     * @return ?????????????? -- ????
     */
    public static boolean isQuoteByIgnoreBlank(CharSequence cs, char lc, char rc) {
        if (null == cs)
            return false;
        int len = cs.length();
        if (len < 2)
            return false;
        int l = 0;
        int last = len - 1;
        int r = last;
        for (; l < len; l++) {
            if (!Character.isWhitespace(cs.charAt(l)))
                break;
        }
        if (cs.charAt(l) != lc)
            return false;
        for (; r > l; r--) {
            if (!Character.isWhitespace(cs.charAt(r)))
                break;
        }
        return l < r && cs.charAt(r) == rc;
    }

    /**
     * @param cs
     *            ???
     * @param lc
     *            ???
     * @param rc
     *            ???
     * @return ??????????????
     */
    public static boolean isQuoteBy(CharSequence cs, char lc, char rc) {
        if (null == cs)
            return false;
        int length = cs.length();
        return length > 1 && cs.charAt(0) == lc && cs.charAt(length - 1) == rc;
    }

    /**
     * ?????????????????
     * 
     * @param coll
     *            ?????
     * @return ????
     */
    public static int maxLength(Collection<? extends CharSequence> coll) {
        int re = 0;
        if (null != coll)
            for (CharSequence s : coll)
                if (null != s)
                    re = Math.max(re, s.length());
        return re;
    }

    /**
     * ?????????????????
     * 
     * @param array
     *            ?????
     * @return ????
     */
    public static <T extends CharSequence> int maxLength(T[] array) {
        int re = 0;
        if (null != array)
            for (CharSequence s : array)
                if (null != s)
                    re = Math.max(re, s.length());
        return re;
    }

    /**
     * ?obj??toString()??,???null??""
     * 
     * @param obj
     * @return obj.toString()
     */
    public static String sNull(Object obj) {
        return sNull(obj, "");
    }

    /**
     * ?obj??toString()??,???null??def?????
     * 
     * @param obj
     * @param def
     *            ??obj==null?????
     * @return obj?toString()??
     */
    public static String sNull(Object obj, String def) {
        return obj != null ? obj.toString() : def;
    }

    /**
     * ?obj??toString()??,???????""
     * 
     * @param obj
     * @return obj.toString()
     */
    public static String sBlank(Object obj) {
        return sBlank(obj, "");
    }

    /**
     * ?obj??toString()??,???????def?????
     * 
     * @param obj
     * @param def
     *            ??obj==null?????
     * @return obj?toString()??
     */
    public static String sBlank(Object obj, String def) {
        if (null == obj)
            return def;
        String s = obj.toString();
        return Strings.isBlank(s) ? def : s;
    }

    /**
     * ???????
     * <p>
     * ??:
     * <ul>
     * <li>removeFirst("12345") => 2345
     * <li>removeFirst("A") => ""
     * </ul>
     * 
     * @param str
     *            ???
     * @return ????
     */
    public static String removeFirst(CharSequence str) {
        if (str == null)
            return null;
        if (str.length() > 1)
            return str.subSequence(1, str.length()).toString();
        return "";
    }

    /**
     * ??str??????? c??,???,???? str
     * <p>
     * ??:
     * <ul>
     * <li>removeFirst("12345",1) => "2345"
     * <li>removeFirst("ABC",'B') => "ABC"
     * <li>removeFirst("A",'B') => "A"
     * <li>removeFirst("A",'A') => ""
     * </ul>
     * 
     * @param str
     *            ???
     * @param c
     *            ???????????
     * @return ????
     */
    public static String removeFirst(String str, char c) {
        return (Strings.isEmpty(str) || c != str.charAt(0)) ? str : str.substring(1);
    }

    /**
     * ??????????????????
     * 
     * @param ss
     *            ?????
     * @param s
     *            ???
     * @return ????
     */
    public static boolean isin(String[] ss, String s) {
        if (null == ss || ss.length == 0 || Strings.isBlank(s))
            return false;
        for (String w : ss)
            if (s.equals(w))
                return true;
        return false;
    }

    /**
     * ???????????????????
     * 
     * @param input
     *            ????????
     * @return true ??????????
     */
    public static final boolean isEmail(CharSequence input) {
        if (Strings.isBlank(input))
            return false;
        try {
            new Email(input.toString());
            return true;
        }
        catch (Exception e) {}
        return false;
    }

    /**
     * ?????????????????????
     * 
     * <pre>
     *  lowerWord("helloWorld", '-') => "hello-world"
     * </pre>
     * 
     * @param cs
     *            ???
     * @param c
     *            ???
     * 
     * @return ??????
     */
    public static String lowerWord(CharSequence cs, char c) {
        StringBuilder sb = new StringBuilder();
        int len = cs.length();
        for (int i = 0; i < len; i++) {
            char ch = cs.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (i > 0)
                    sb.append(c);
                sb.append(Character.toLowerCase(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * ???????????????????????
     * 
     * <pre>
     *  upperWord("hello-world", '-') => "helloWorld"
     * </pre>
     * 
     * @param cs
     *            ???
     * @param c
     *            ???
     * 
     * @return ??????
     */
    public static String upperWord(CharSequence cs, char c) {
        StringBuilder sb = new StringBuilder();
        int len = cs.length();
        for (int i = 0; i < len; i++) {
            char ch = cs.charAt(i);
            if (ch == c) {
                do {
                    i++;
                    if (i >= len)
                        return sb.toString();
                    ch = cs.charAt(i);
                } while (ch == c);
                sb.append(Character.toUpperCase(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * ?????????HMTL?????????
     * 
     * <pre>
     *  escapeHtml("&lt;script&gt;alert("hello world");&lt;/script&gt;") => "&amp;lt;script&amp;gt;alert(&amp;quot;hello world&amp;quot;);&amp;lt;/script&amp;gt;"
     * </pre>
     * 
     * ????????
     * <ul>
     * <li>& => &amp;amp;
     * <li>< => &amp;lt;
     * <li>>=> &amp;gt;
     * <li>' => &amp;#x27;
     * <li>" => &amp;quot;
     * </ul>
     * 
     * @param cs
     *            ???
     * 
     * @return ??????
     */
    public static String escapeHtml(CharSequence cs) {
        if (null == cs)
            return null;
        char[] cas = cs.toString().toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : cas) {
            switch (c) {
            case '&':
                sb.append("&amp;");
                break;
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            case '\'':
                sb.append("&#x27;");
                break;
            case '"':
                sb.append("&quot;");
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static byte[] getBytesUTF8(CharSequence cs) {
        try {
            return cs.toString().getBytes(Encoding.UTF8);
        }
        catch (UnsupportedEncodingException e) {
            throw Lang.wrapThrow(e);
        }
    }

}

