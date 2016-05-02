package com.fpbm.common.util;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName: StringUtil
 * @Description: 工具类-字符串操作
 *
 */
public class StringUtil {

    /**
     * 根据指定长度 分隔字符串
     *
     * @param str
     *            需要处理的字符串
     * @param length
     *            分隔长度
     *
     * @return 字符串集合
     */
    public static List<String> splitString(String str, int length) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < str.length(); i += length) {
            int endIndex = i + length;
            if (endIndex <= str.length()) {
                list.add(str.substring(i, i + length));
            } else {
                list.add(str.substring(i, str.length() - 1));
            }
        }
        return list;
    }

    /**
     * 将字符串List转化为字符串，以分隔符间隔.
     *
     * @param list
     *            需要处理的List.
     *
     * @param separator
     *            分隔符.
     *
     * @return 转化后的字符串
     */
    public static String toString(List<String> list, String separator) {
        StringBuffer stringBuffer = new StringBuffer();
        for (String str : list) {
            stringBuffer.append(separator + str);
        }
        stringBuffer.deleteCharAt(0);
        return stringBuffer.toString();
    }

    /**
     *
     *
     * @Title: splitStrToIntList
     * @Description: 将字符串转化为List<Integer>
     * @param str
     * @param regix
     * @return List<Integer>
     */
    public static List<Integer> splitStrToIntList(String str, String regix) {
        String ss[] = str.split(regix);
        List<Integer> lst = new ArrayList<Integer>();
        for (int i = 0; i < ss.length; i++) {
            lst.add(Integer.parseInt(ss[i]));
        }
        return lst;
    }

    /**
     *
     *
     * @Title: splitStringLength
     * @Description: 根据指定长度 分隔字符串
     * @param str
     * @param lengths
     * @return List<String>
     */
    public static List<String> splitStringLength(String str, List<Integer> lengths) {
        List<String> list = new ArrayList<String>();
        Integer pos = 0;
        for (Integer len : lengths) {
            if ((pos + len) < str.length()) {
                list.add(str.substring(pos, pos + len));
                pos = pos + len;
            } else {
                list.add(str.substring(pos));
                break;
            }
            if (pos >= str.length()) {
                break;
            }
        }
        return list;
    }

    /**
     *
     *
     * @Title: splitStringLength
     * @Description: 根据指定长度 分隔字符串
     * @param str
     * @param lengths
     * @return List<String>
     */
    public static List<String> splitStringLength(String str, Integer[] lengths) {
        List<String> list = new ArrayList<String>();
        Integer pos = 0;
        for (Integer len : lengths) {
            if ((pos + len) < str.length()) {
                list.add(str.substring(pos, pos + len));
                pos = pos + len;
            } else {
                list.add(str.substring(pos));
                break;
            }
            if (pos >= str.length()) {
                break;
            }
        }
        return list;
    }

    /**
     *
     *
     * @Title: splitStringPattern
     * @Description: 根据正则表达式分割字符串
     * @param str
     * @param sPattern
     * @return List<String>
     */
    public static List<String> splitStringPattern(String str, String sPattern) {
        Pattern pattern = Pattern.compile(sPattern);
        String[] strs = pattern.split(str);
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < strs.length; i++) {
            list.add(strs[i]);
        }
        return list;
    }

    /**
     *
     *
     * @Title: filterStringPattern
     * @Description: 根据正则表达式替換字符串
     * @param str
     * @param sPattern
     * @return String
     */
    public static String filterStringPattern(String str, String sPattern) {
        Pattern pattern = Pattern.compile(sPattern);
        Matcher m = pattern.matcher(str);
        return m.replaceAll("").trim();

    }

    /**
     *
     *
     * @Title: replace
     * @Description: 字符串替換
     * @param strSource
     * @param strFrom
     * @param strTo
     * @return String
     */
    public static String replace(String strSource, String strFrom, String strTo) {
        if (strSource == null) {
            return null;
        }
        int i = 0;
        if ((i = strSource.indexOf(strFrom, i)) >= 0) {
            char[] cSrc = strSource.toCharArray();
            char[] cTo = strTo.toCharArray();
            int len = strFrom.length();
            StringBuffer buf = new StringBuffer(cSrc.length);
            buf.append(cSrc, 0, i).append(cTo);
            i += len;
            int j = i;
            while ((i = strSource.indexOf(strFrom, i)) > 0) {
                buf.append(cSrc, j, i - j).append(cTo);
                i += len;
                j = i;
            }
            buf.append(cSrc, j, cSrc.length - j);
            return buf.toString();
        }
        return strSource;
    }

    /**
     *
     *
     * @Title: getNumsFromStr
     * @Description: 将字符串中的数字，生成List<Integer>
     * @param str
     * @return List<Integer>
     */
    public static List<Integer> getNumsFromStr(String str) {

        List<Integer> list = new ArrayList<Integer>();
        String s = filterStringPattern(str, "[^1-9]");
        for (int i = 0; i < str.length(); i++) {
            list.add(new Integer(s.substring(i, 1)));
        }
        return list;
    }

    /**
     *
     *
     * @Title: deleteString
     * @Description: 删除指定位置、长度的字符串
     * @param str
     * @param index
     * @param len
     * @return String
     */
    public static String deleteString(String str, Integer index, Integer len) {
        String s = str;
        if ((index + len) > s.length()) {
            return s;
        }
        return s.substring(0, index) + s.substring(index + len, s.length() - index - len);

    }

    public static String getCategoryRex(String str) {
        String s = "";
        Integer n = 0;
        for (int i = 0; i < str.length(); i++) {
            n = n + Integer.parseInt(str.substring(i, 1));
            s = s + "\\{" + String.valueOf(n) + "}|";
        }
        if (s.length() > 0) {
            s = deleteString(s, s.length() - 1, 1);
        }
        return s;
    }

    /**
     *
     *
     * @Title: subStr
     * @Description: 按位置、长度截取字符串
     * @param s
     * @param pos
     * @param len
     * @return String
     */
    public static String subStr(String s, int pos, int len) {
        return s.substring(pos, pos + len);
    }

    /**
     *
     *
     * @Title: leftString
     * @Description: 按长度截取左侧字符串
     * @param s
     * @param len
     * @return String
     */
    public static String leftString(String s, int len) {
        return subStr(s, 0, len);
    }

    /**
     *
     *
     * @Title: rightString
     * @Description:按长度截取右侧字符串
     * @param s
     * @param len
     * @return String
     */
    public static String rightString(String s, int len) {
        return subStr(s, s.length() - len, len);
    }

    /**
     *
     *
     * @Title: formatDouble
     * @Description: 格式化浮点数
     * @param fmt
     * @param d
     * @return String
     */
    public static String formatDouble(String fmt, double d) {
        return new DecimalFormat(fmt).format(d);
    }

    /**
     *
     *
     * @Title: repeatString
     * @Description: 重复字符串到超过指定长度
     * @param s
     * @param n
     * @return String
     */
    public static String repeatString(String s, int n) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static String repeatString(char s, int n) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     *
     *
     * @Title: fillLeftString
     * @Description: 左侧补齐
     * @param s
     * @param c
     * @param length
     * @return String
     */
    public static String fillLeftString(String s, char c, int length) {
        if (s.length() > length) {
            return leftString(s, length);
        } else if (s.length() < length) {
            return repeatString(c, length - s.length()) + s;
        } else {
            return s;
        }
    }

    /**
     *
     *
     * @Title: fillRightString
     * @Description: 右侧补齐
     * @param s
     * @param c
     * @param length
     * @return String
     */
    public static String fillRightString(String s, char c, int length) {
        if (s.length() > length) {
            return leftString(s, length);
        } else if (s.length() < length) {
            return s + repeatString(c, length - s.length());
        } else {
            return s;
        }
    }

    /**
     *
     *
     * @Title: strToInt
     * @Description: 字符串转int
     * @param value
     * @return int
     */
    public static int strToInt(String value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     *
     *
     * @Title: strSwap
     * @Description: 反转字符串
     * @param str
     * @return String
     */
    public static String strSwap(String str) {
        StringBuffer stringBuffer = new StringBuffer(str);
        return stringBuffer.reverse().toString();
    }

    /**
     *
     *
     * @Title: addZero
     * @Description: int转字符串，左侧补零
     * @param d
     * @param len
     * @return String
     */
    public static String addZero(int d, int len) {
        return fillLeftString(String.valueOf(d), '0', len);
    }

    /**
     *
     *
     * @Title: evenString
     * @Description: 获得长度为偶数的字符串，左侧补零
     * @param str
     * @return String
     */
    public static String evenString(String str) {
        return str == null ? "" : str.length() % 2 > 0 ? "0" + str : str;
    }

    public static final int search(String b[], String key) {
        for (int i = 0; i < b.length; i++) {
            if (b[i].equals(key)) {
                return i;
            }
        }
        return -1;
    }

    /**
    *
    *
    * @Title: searchHead
    * @Description: 字符串字首包含返回key长度，否则返回0
    * @param str
    * @param key
    * @return int
    */
    public static final int searchHead(String str, String key) {
        if (key.length() > str.length()) {
            return 0;
        }
        for (int i = 0; i < key.length(); i++) {
            if (key.charAt(i) != str.charAt(i)) {
                return 0;
            }
        }
        return key.length();
    }
    
    /**
     * 
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
    	
		if(str == null || str.trim().equals(""))
		{
			return true;
		}
		return false;
	}
    
    /**
     * 去掉字符串的标签
     * @param str
     * @return
     */
    public static String delHTMLTag(String htmlStr){ 
        String regEx_script="<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式 
        String regEx_style="<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式 
        String regEx_html="<[^>]+>"; //定义HTML标签的正则表达式 
         
        Pattern p_script=Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE); 
        Matcher m_script=p_script.matcher(htmlStr); 
        htmlStr=m_script.replaceAll(""); //过滤script标签 
         
        Pattern p_style=Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE); 
        Matcher m_style=p_style.matcher(htmlStr); 
        htmlStr=m_style.replaceAll(""); //过滤style标签 
         
        Pattern p_html=Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE); 
        Matcher m_html=p_html.matcher(htmlStr); 
        htmlStr=m_html.replaceAll(""); //过滤html标签 

        return htmlStr.trim(); //返回文本字符串 
    } 

    
}

