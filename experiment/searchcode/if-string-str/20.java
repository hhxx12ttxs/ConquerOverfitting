package com.jeasonzhao.commons.parser.expression.library;

import java.util.Date;

import com.jeasonzhao.commons.utils.Algorithms;
import com.jeasonzhao.commons.utils.ConvertEx;
import com.jeasonzhao.commons.utils.RegexHelper;

public class CommonOperations
{
    public CommonOperations()
    {
        super();
    }

    public String guid()
    {
        return com.jeasonzhao.commons.utils.Guid.newGuid();
    }

    public String guid(int nRowIndex)
    {
        return Algorithms.padLeft("" + nRowIndex,10,'0') +
            com.jeasonzhao.commons.utils.Guid.newGuid();
    }

    public String guid(String prefix)
    {
        return(null == prefix ? "" : prefix)
            + com.jeasonzhao.commons.utils.Guid.newGuid();
    }

    public int length(Object obj)
    {
        return null == obj ? 0 : Algorithms.toString(obj).length();
    }

    public boolean startsWith(String str1,String str2)
    {
        return null == str1 || null == str2 ? false : str1.startsWith(str2);
    }

    public boolean endsWith(String str1,String str2)
    {
        return null == str1 || null == str2 ? false : str1.endsWith(str2);
    }

    public String substring(String str1,int nIndex)
    {
        return null == str1 ? null : str1.substring(nIndex);
    }

    public String substring(String str1,int nIndex,int nIndex2)
    {
        if(null != str1 && nIndex2 >= str1.length())
        {
            return substring(str1,nIndex);
        }
        else
        {
            return null == str1 ? null : str1.substring(nIndex,nIndex2);
        }
    }

    public String left(String str1,int nIndex)
    {
        return null == str1 ? null : str1.substring(nIndex);
    }

    public int toInt(String str)
    {
        return ConvertEx.toInt(str,0);
    }

    public int cInt(String str)
    {
        return toInt(str);
    }

    public Date toDate(String str)
    {
        return ConvertEx.toDate(str);
    }

    public Date cDate(String str)
    {
        return toDate(str);
    }

    public String toString(Object obj)
    {
        return Algorithms.toString(obj);
    }

    public String toString(Object obj,String format)
    {
        return Algorithms.toString(obj,format);
    }

    public boolean equals(Object o1,Object o2)
    {
        return null == o1 ? null == o2 : o1.equals(o2);
    }

    public String cStr(Object obj)
    {
        return toString(obj);
    }

    public double toDouble(String str)
    {
        return ConvertEx.toDouble(str,0);
    }

    public double cDouble(String str)
    {
        return toDouble(str);
    }

    public String escapeHead(String strInit,String header)
    {
        if(strInit == null || header == null || strInit.startsWith(header) == false)
        {
            return strInit;
        }
        else
        {
            return strInit.substring(header.length());
        }
    }

    public Object nvl(Object objInstance,Object nullVlaue)
    {
        return null == objInstance ? nullVlaue : objInstance;
    }

    public boolean isnull(Object objInstance)
    {
        return null == objInstance;
    }

    public Object iif(boolean bCheck,Object trueObject,Object falseObject)
    {
        return bCheck ? trueObject : falseObject;
    }

    public String upper(String str)
    {
        return str == null ? null : str.toUpperCase();
    }

    public String lower(String str)
    {
        return str == null ? null : str.toLowerCase();
    }

    public String replace(String str,String str2,String str3)
    {
        return str == null ? null : str.replaceAll(str2,str3);
    }

    public boolean test(String regex,String str)
    {
        return RegexHelper.matches(regex,str);
    }

    public boolean matches(String regex,String str)
    {
        return RegexHelper.matches(regex,str);
    }

    public boolean wildcard(String regex,String str)
    {
        return RegexHelper.wildcard(regex,str);
    }

    public boolean wildcardIgnoreCase(String regex,String str)
    {
        return RegexHelper.wildcardIgnoreCase(regex,str);
    }

    public boolean matchesIgnoreCase(String regex,String str)
    {
        return RegexHelper.matchesIgnoreCase(regex,str);
    }

    public String pad(String str,int nLength,String ca)
    {
        return Algorithms.padRight(str,nLength,null == ca || ca.length() < 1 ? ' ' : ca.charAt(0));
    }

    public String pad(String str,int nLength)
    {
        return Algorithms.padRight(str,nLength,' ');
    }

    public String padLeft(String str,int nLength)
    {
        return Algorithms.padLeft(str,nLength,' ');
    }

    public String padLeft(String str,int nLength,String ca)
    {
        return Algorithms.padLeft(str,nLength,null == ca || ca.length() < 1 ? ' ' : ca.charAt(0));
    }
}

