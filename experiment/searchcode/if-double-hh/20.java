package com.jeasonzhao.commons.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;

public class ConvertEx
{
    private ConvertEx()
    {
    }

    public static boolean toBoolN(Object value,boolean bNullValue)
    {
        if(null == value)
        {
            return bNullValue;
        }
        else
        {
            return toBool(value);
        }
    }

    public static boolean toBoolN(String value,boolean bNullValue)
    {
        if(null == value || value.trim().length() < 1)
        {
            return bNullValue;
        }
        else
        {
            return toBool(value);
        }
    }

    public static boolean toBool(Object value)
    {
        if(null == value)
        {
            return false;
        }
        else if(value instanceof Boolean || value.getClass() == Boolean.TYPE)
        {
            Boolean b = (Boolean) value;
            return b.booleanValue();
        }
        else if(value instanceof Integer || value.getClass() == Integer.TYPE)
        {
            Integer i = (Integer) value;
            return i.intValue() > 0;
        }
        else if(value instanceof Long || value.getClass() == Long.TYPE)
        {
            Long i = (Long) value;
            return(i.longValue() > 0);
        }
        else if(value instanceof Double || value.getClass() == Double.TYPE)
        {
            Double i = (Double) value;
            return(i.doubleValue() > 0);
        }
        else
        {
            if(Algorithms.isEmpty(value))
            {
                return false;
            }
            else if(value.toString().toLowerCase().trim().equals("false"))
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }

    public static Boolean toBoolean(Object value)
    {
        return Boolean.valueOf(toBool(value));
    }

    public static Integer toInteger(Object value)
        throws NumberFormatException,
        NullPointerException
    {
        return Integer.valueOf(toInt(value));
    }
    public static byte toByte(Object value)
    {
        return (byte)toInt(value);
    }
    public static int toInt(Object value,int nDefault)
    {
        try
        {
            return toInt(value);
        }
        catch(Exception excep)
        {
            return nDefault;
        }
    }

    public static int toInt(Object value)
        throws NumberFormatException,NullPointerException
    {
        if(null == value)
        {
            throw new NullPointerException();
        }
        else if(value instanceof Boolean || value.getClass() == Boolean.TYPE)
        {
            Boolean b = (Boolean) value;
            return b.booleanValue() ? 1 : 0;
        }
        else if(value instanceof Integer || value.getClass() == Integer.TYPE)
        {
            Integer i = (Integer) value;
            return i.intValue();
        }
        else if(value instanceof Long || value.getClass() == Long.TYPE)
        {
            Long i = (Long) value;
            return(int) i.longValue();
        }
        else if(value instanceof Double || value.getClass() == Double.TYPE)
        {
            Double i = (Double) value;
            return(int) i.doubleValue();
        }
        else if(value instanceof java.util.Date)
        {
            java.util.Date date = (java.util.Date) value;
            return(int) date.getTime();
        }
        else
        {
            String str = Algorithms.toASCIICharSet(value.toString().trim().replaceAll(",",""));
            try
            {
                if(ChineseNumber.isChinesNumber(str))
                {
                    return ChineseNumber.toInt(str);
                }
                else
                {
                    return Integer.parseInt(str);
                }
            }
            catch(Exception ex)
            {
                double dl = Double.parseDouble(str);
                return(int) dl;
            }
        }
    }

    public static Double toDoubleObject(Object value)
    {
        return null == value ? (Double)null : new Double(toDouble(value));
    }

    public static double toDouble(Object value)
    {
        if(null == value)
        {
            throw new NullPointerException("Null pointer can be convert to Double");
        }
        else if(value instanceof Boolean)
        {
            Boolean b = (Boolean) value;
            return b.booleanValue() ? 1 : 0;
        }
        else if(value instanceof Integer)
        {
            Integer i = (Integer) value;
            return i.intValue();
        }
        else if(value instanceof Long)
        {
            Long i = (Long) value;
            return(double) i.longValue();
        }
        else if(value instanceof Double)
        {
            Double i = (Double) value;
            return i.doubleValue();
        }
        else if(value instanceof java.util.Date)
        {
            java.util.Date date = (java.util.Date) value;
            return(double) date.getTime();
        }
        else
        {
            String str = Algorithms.toASCIICharSet(value.toString().trim().replaceAll(",",""));
            if(ChineseNumber.isChinesNumber(str))
            {
                return ChineseNumber.toInt(str);
            }
            else
            {
                return Double.parseDouble(str);
            }
        }
    }

    public static double toDouble(Object object,double defaultValue)
    {
        try
        {
            return toDouble(object);
        }
        catch(Exception ex)
        {
            return defaultValue;
        }
    }

    ////////////////////////////isInt///////////////////////////////////
    public static final boolean isInt(Object value)
    {
        //I don't want to use regular expression to check.
        try
        {
            if(null == value)
            {
                return false;
            }
            else
            {
                toInt(value);
                return true;
            }
        }
        catch(Exception excep)
        {
            return false;
        }
    }

    public static final boolean isDouble(Object value)
    {
        //I don't want to use regular expression to check.
        try
        {
            if(null == value)
            {
                return false;
            }
            else
            {
                toDouble(value);
                return true;
            }
        }
        catch(Exception excep)
        {
            return false;
        }
    }

    public static final boolean isDate(Object value)
    {
        try
        {
            if(null == value)
            {
                return false;
            }
            else
            {
                return null != toDate(value);
            }
        }
        catch(Exception excep)
        {
            return false;
        }
    }

    public static final Date newDate(int y,int m,int d)
    {
        return newDate(y,m,d,0,0,0);
    }

    public static final Date newDate(int y,int m,int d,int h,int mm,int ss)
    {
        java.util.Calendar ca = java.util.Calendar.getInstance();
        ca.set(y,m,d,h,mm,ss);
        return ca.getTime();
    }

    public static final Date newDate(int y,int m)
    {
        return newDate(y,m,1,0,0,0);
    }

    private static final String PATTERN_YEAR =
        "(\\d{4}|[" + ChineseNumber.AllNumberSymbolWithoutComma
        + "]{4}|[" + ChineseNumber.AllNumberSymbolWithoutComma + "]{2})?{0,1}";

    private static final String PATTERN_MONTH = "(0\\d{1}|1[0,1,2]|\\d{1})[?]{0,1}";
    private static final String PATTERN_MONTH_DUAL = "(0\\d{1}|1[0,1,2])[?]{0,1}";
    private static final String PATTERN_DATE =
        "(0\\d{1}|1\\d{1}|2\\d{1}|3[0,1]|\\d{1}"
        + "|?|?|?|?|?|?|?|?|?|?"
        + "|??|??|??|??|??|??|??|??|??|??"
        + "|?|??|??|??|??|??|??|??|??|??"
        + "|???|???|???|???|???|???|???|???|???"
        + "|??|???|?|??"
        + "|?|?|?|?|?|?|?|?|?|?"
        + "|??|??|??|??|??|??|??|??|??|??"
        + "|???|???|???|???|???|???|???|???|???|??|???"
        + ")[??]{0,1}";
    private static final String PATTERN_DATE_DUAL =
        "(0\\d{1}|1\\d{1}|2\\d{1}|3[0,1]"
        + "|?|?|?|?|?|?|?|?|?|?"
        + "|??|??|??|??|??|??|??|??|??|??"
        + "|???|???|???|???|???|???|???|???|???|??|???"
        + "|?|?|?|?|?|?|?|?|?|?"
        + "|??|??|??|??|??|??|??|??|??|??"
        + "|???|???|???|???|???|???|???|???|???|??|???"
        + ")[??]{0,1}";
    private static final String PATTERN_WEEK_NAME = "("
        + "Sun|Mon|Tue|Wed|Thur|Fri|Sat|Thu"
        + "|Sunday|Monday|Tuesday|Wednesday|Thursday|Friday|Saturday"
        + "|???|???|???|???|???|???|???"
        + "|??|??|??|??|??|??|??"
        + ")";
    private static final String PATTERN_TIME_ZONE = "(CST|PST|GMT)";
    private static final String PATTERN_MONTH_NAME = "("
        + "Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec"
        + "|January|February|March|April|May|June|July|August|September|October|November|December"
        + "|?|?|?|?|?|?|?|?|?|?|??|??"
        + "|?|?|?|?|?|?|?|?|?|?|??|??"
        + "|?|?|?|?|?|?|?|?|?|?|??|?"
        + "|?|?|?|?|?|?|?|?|?|?|??|?"
        + ")[?]{0,1}";
    private static final String P_T_TMP = "[" + ChineseNumber.AllNumberSymbolWithoutComma + "??]{1,3}";
    private static final String P_T_TMP2 = "[" + ChineseNumber.AllNumberSymbolWithoutComma + "]{1,2}";
    private static final String PATTERN_TIME = "(\\d{1,2}|" + P_T_TMP + ")[-/\\:??](\\d{1,2}|" + P_T_TMP + ")[-/\\:?](\\d{1,2}|" + P_T_TMP + ")[?]{0,1}";
    private static final String PATTERN_TIME2 = "(\\d{1,2}|" + P_T_TMP + ")[-/\\:??](\\d{1,2}|" + P_T_TMP + ")[?]{0,1}";
    private static final String PATTERN_TIME3 = "(\\d{1,2}|" + P_T_TMP + ")[??]{0,1}";
    private static final String PATTERN_TIME_DUAL = "(\\d{2}|" + P_T_TMP2 + ")(\\d{2}|" + P_T_TMP2 + ")(\\d{2}|" + P_T_TMP2 + ")";
    private static final String PATTERN_TIME_DUAL2 = "(\\d{2}|" + P_T_TMP2 + ")(\\d{2}|" + P_T_TMP2 + ")";
    private static final String PATTERN_TIME_DUAL3 = "(\\d{2}|" + P_T_TMP2 + ")";

    public static final long TickFor197001010000UTC = 621355968000000000L;
    private static final int getMonth(String str)
    {
        if(ConvertEx.isInt(str))
        {
            return Math.max(0,ConvertEx.toInt(str) - 1);
        }
        else
        {
            String[] allNames = PATTERN_MONTH_NAME.substring(1,PATTERN_MONTH_NAME.length() - 1).split("\\|");
            for(int n = 0;n < allNames.length;n++)
            {
                if(allNames[n].equalsIgnoreCase(str))
                {
                    //System.out.println(str+"="+(n%12)+"="+n);
                    return n % 12;
                }
            }
            return 0;
        }
    }

    private static int adjustShortYear(int n)
    {
        if(n <= 0)
        {
            return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        }
        else if(n < 20)
        {
            return 2000 + n;
        }
        else if(n < 100)
        {
            return 1900 + n;
        }
        else if(n < 1000)
        {
            return 1000 + n;
        }
        else
        {
            return n;
        }
    }

    private static int getYear(String s)
    {
        if(Algorithms.isEmpty(s))
        {
            return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        }
        else
        {
            if(ConvertEx.isInt(s))
            {
                return adjustShortYear(ConvertEx.toInt(s));
            }
            else
            {
                String snumber = ChineseNumber.AllNumberSymbolWithoutComma;
                char[] ary = s.trim().toCharArray();
                int nYear = 0;
                for(int n = ary.length - 1;n >= 0;n--)
                {
                    char c = ary[n];
                    int nx = snumber.indexOf(c) % 10;
                    if(nx < 0)
                    {
                        return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                    }
                    else
                    {
                        switch(n)
                        {
                            case 0:
                                nx = ary.length == 2 ? nx * 10 : (ary.length == 3 ? nx * 100 : nx * 1000);
                                break;
                            case 1:
                                nx = ary.length == 2 ? nx : (ary.length == 3 ? nx * 10 : nx * 100);
                                break;
                            case 2:
                                nx = ary.length == 3 ? nx : nx * 10;
                                break;
                            case 3:
                                break;
                        }
                    }
                    nYear += nx;
                }
                return adjustShortYear(nYear);
            }
        }
    }

    public static java.util.Date toDate(Object value,java.util.Date defaultValue)
    {
        java.util.Date d = toDate(value);
        return null == d ? defaultValue : d;
    }

    public static java.util.Date toDate(Object value)
    {
        if(null == value)
        {
            return null;
        }
        else if(value instanceof java.util.Date)
        {
            return(java.util.Date) value;
        }
        else
        {
            String strDateString = Algorithms.toASCIICharSet(value.toString().trim());
            if(Algorithms.isEmpty(strDateString))
            {
                return null;
            }
            Calendar ca = Calendar.getInstance();
            Matcher m = null;
            // Wed Nov 12 10:01:54 CST 2008
            m = matchOne(strDateString
                         ,PATTERN_WEEK_NAME + "\\s+" + PATTERN_MONTH_NAME + "\\s*" + PATTERN_DATE + "\\s+" +
                         PATTERN_TIME + "\\s+" + PATTERN_TIME_ZONE + "\\s*" + PATTERN_YEAR
                         ,PATTERN_WEEK_NAME + "\\s+" + PATTERN_MONTH + "\\s*" + PATTERN_DATE
                         + "\\s+" + PATTERN_TIME + "\\s+" + PATTERN_TIME_ZONE + "\\s*" + PATTERN_YEAR
                         ,PATTERN_WEEK_NAME + "\\s+" + PATTERN_MONTH_NAME + "\\s*" + PATTERN_DATE + "\\s+"
                         + PATTERN_TIME_DUAL + "\\s+" + PATTERN_TIME_ZONE + "\\s*" + PATTERN_YEAR
                         ,PATTERN_WEEK_NAME + "\\s+" + PATTERN_MONTH + "\\s*" + PATTERN_DATE + "\\s+"
                         + PATTERN_TIME_DUAL + "\\s+" + PATTERN_TIME_ZONE + "\\s*" + PATTERN_YEAR
                );
            if(null != m && m.matches())
            {
                int nMonth = m.groupCount() >= 2 ? getMonth(m.group(2)) : 0;
                int nDate = m.groupCount() >= 3 && m.group(3).length() > 0 ? ConvertEx.toInt(m.group(3),0) : 1;
                int nHour = m.groupCount() >= 4 && m.group(4).length() > 0 ? ConvertEx.toInt(m.group(4),0) : 0;
                int nMinutes = m.groupCount() >= 5 && m.group(5).length() > 0 ? ConvertEx.toInt(m.group(5),0) : 0;
                int nSeconds = m.groupCount() >= 6 && m.group(6).length() > 0 ? ConvertEx.toInt(m.group(6),0) : 0;
                int nYear = m.groupCount() >= 8 ? getYear(m.group(8)) : ca.get(Calendar.YEAR);
                ca.set(nYear,nMonth,nDate,nHour,nMinutes,nSeconds);
                return ca.getTime();
            }
            m = matchOne(strDateString
                         ,PATTERN_WEEK_NAME + "\\s+" + PATTERN_MONTH_NAME + "\\s*" + PATTERN_DATE + "\\s+"
                         + PATTERN_TIME_ZONE + "\\s*" + PATTERN_YEAR
                         ,PATTERN_WEEK_NAME + "\\s+" + PATTERN_MONTH + "\\s+" + PATTERN_DATE + "\\s+"
                         + PATTERN_TIME_ZONE + "\\s*" + PATTERN_YEAR
                );
            if(null != m && m.matches())
            {
                int nMonth = m.groupCount() >= 2 ? getMonth(m.group(2)) : 0;
                int nDate = m.groupCount() >= 3 && m.group(3).length() > 0 ? ConvertEx.toInt(m.group(3),0) : 1;
                int nYear = m.groupCount() >= 5 && m.group(5).length() > 0 ? getYear(m.group(5)) : ca.get(Calendar.YEAR);
                ca.set(nYear,nMonth,nDate,0,0,0);
                ca.set(Calendar.MILLISECOND,0);
                return ca.getTime();
            }

            // Wed Nov 12 10:01:54 2008
            m = matchOne(strDateString
                         ,PATTERN_WEEK_NAME + "\\s+" + PATTERN_MONTH_NAME + "\\s*" + PATTERN_DATE
                         + "\\s*" + PATTERN_TIME + "\\s+" + PATTERN_YEAR
                         ,PATTERN_WEEK_NAME + "\\s+" + PATTERN_MONTH_NAME + "\\s*" + PATTERN_DATE
                         + "\\s*" + PATTERN_TIME_DUAL + "\\s+" + PATTERN_YEAR
                         ,PATTERN_WEEK_NAME + "\\s+" + PATTERN_MONTH + "\\s*" + PATTERN_DATE + "\\s*"
                         + PATTERN_TIME + "\\s+" + PATTERN_YEAR
                         ,PATTERN_WEEK_NAME + "\\s+" + PATTERN_MONTH + "\\s*" + PATTERN_DATE + "\\s*"
                         + PATTERN_TIME_DUAL + "\\s+" + PATTERN_YEAR
                );
            if(null != m && m.matches())
            {
                int nMonth = m.groupCount() >= 2 ? getMonth(m.group(2)) : 0;
                int nDate = m.groupCount() >= 3 ? ConvertEx.toInt(m.group(3),0) : 0;
                int nHour = m.groupCount() >= 4 ? ConvertEx.toInt(m.group(4),0) : 0;
                int nMinutes = m.groupCount() >= 5 ? ConvertEx.toInt(m.group(5),0) : 0;
                int nSeconds = m.groupCount() >= 6 ? ConvertEx.toInt(m.group(6),0) : 0;
                int nYear = m.groupCount() >= 7 ? getYear(m.group(7)) : ca.get(Calendar.YEAR);
                ca.set(nYear,nMonth,nDate,nHour,nMinutes,nSeconds);
                ca.set(Calendar.MILLISECOND,0);
                return ca.getTime();
            }
            m = matchOne(strDateString
                         ,PATTERN_WEEK_NAME + "\\s+" + PATTERN_MONTH_NAME + "\\s*" + PATTERN_DATE + "\\s*" +
                         PATTERN_YEAR
                         ,PATTERN_WEEK_NAME + "\\s+" + PATTERN_MONTH + "\\s+" + PATTERN_DATE + "\\s*" + PATTERN_YEAR
                );
            if(null != m && m.matches())
            {
                int nMonth = m.groupCount() >= 2 ? getMonth(m.group(2)) : 0;
                int nDate = m.groupCount() >= 3 ? ConvertEx.toInt(m.group(3),0) : 0;
                int nYear = m.groupCount() >= 4 ? getYear(m.group(4)) : ca.get(Calendar.YEAR);
                ca.set(nYear,nMonth,nDate,0,0,0);
                ca.set(Calendar.MILLISECOND,0);
                return ca.getTime();
            }

            // 07 Oct 2008 12:59:59
            m = matchOne(strDateString
                         ,PATTERN_DATE + "[-/\\s\\\\]*" + PATTERN_MONTH_NAME + "[-/\\s\\\\]*" + PATTERN_YEAR + "\\s*" + PATTERN_TIME
                         ,PATTERN_DATE + "[-/\\s\\\\]*" + PATTERN_MONTH_NAME + "[-/\\s\\\\]*" + PATTERN_YEAR + "\\s*" + PATTERN_TIME2
                         ,PATTERN_DATE + "[-/\\s\\\\]*" + PATTERN_MONTH_NAME + "[-/\\s\\\\]*" + PATTERN_YEAR + "\\s*" + PATTERN_TIME3
                         ,PATTERN_DATE + "[-/\\s\\\\]*" + PATTERN_MONTH_NAME + "[-/\\s\\\\]*" + PATTERN_YEAR + "\\s*" + PATTERN_TIME_DUAL
                         ,PATTERN_DATE + "[-/\\s\\\\]*" + PATTERN_MONTH_NAME + "[-/\\s\\\\]*" + PATTERN_YEAR + "\\s*" + PATTERN_TIME_DUAL2
                         ,PATTERN_DATE + "[-/\\s\\\\]*" + PATTERN_MONTH_NAME + "[-/\\s\\\\]*" + PATTERN_YEAR + "\\s*" + PATTERN_TIME_DUAL3
                         ,PATTERN_DATE + "[-/\\s\\\\]*" + PATTERN_MONTH_NAME + "[-/\\s\\\\]*" + PATTERN_YEAR
                         ,PATTERN_DATE + "[-/\\s\\\\]*" + PATTERN_MONTH_NAME
                );
            if(null != m && m.matches())
            {
                int nDate = m.groupCount() >= 1 ? ConvertEx.toInt(m.group(1),0) : 0;
                int nMonth = m.groupCount() >= 2 ? getMonth(m.group(2)) : 1;
                int nYear = m.groupCount() >= 3 ? getYear(m.group(3)) : ca.get(Calendar.YEAR);
                int nHour = m.groupCount() >= 4 && m.group(4).length() > 0 ? ConvertEx.toInt(m.group(4),0) : 0;
                int nMinutes = m.groupCount() >= 5 && m.group(5).length() > 0 ? ConvertEx.toInt(m.group(5),0) : 0;
                int nSeconds = m.groupCount() >= 6 && m.group(6).length() > 0 ? ConvertEx.toInt(m.group(6),0) : 0;
                ca.set(nYear,nMonth,nDate,nHour,nMinutes,nSeconds);
                ca.set(Calendar.MILLISECOND,0);
                return ca.getTime();
            }

            // 2008-12-31 12:59:59 yyyy MM dd HH mm ss
            m = matchOne(strDateString
                         ,PATTERN_YEAR + "[-/\\\\?]" + PATTERN_MONTH + "[-/\\\\?]" + PATTERN_DATE + "[??\\s]+" + PATTERN_TIME
                         ,PATTERN_YEAR + "[-/\\\\?]" + PATTERN_MONTH + "[-/\\\\?]" + PATTERN_DATE + "[??\\s]+" + PATTERN_TIME2
                         ,PATTERN_YEAR + "[-/\\\\?]" + PATTERN_MONTH + "[-/\\\\?]" + PATTERN_DATE + "[??\\s]+" + PATTERN_TIME3
                         ,PATTERN_YEAR + "[-/\\\\?]" + PATTERN_MONTH + "[-/\\\\?]" + PATTERN_DATE + "[??\\s]+" + PATTERN_TIME_DUAL
                         ,PATTERN_YEAR + "[-/\\\\?]" + PATTERN_MONTH + "[-/\\\\?]" + PATTERN_DATE + "[??\\s]+" + PATTERN_TIME_DUAL2
                         ,PATTERN_YEAR + "[-/\\\\?]" + PATTERN_MONTH + "[-/\\\\?]" + PATTERN_DATE + "[??\\s]+" + PATTERN_TIME_DUAL3
                         ,PATTERN_YEAR + "[-/\\\\?]" + PATTERN_MONTH + "[-/\\\\?]" + PATTERN_DATE
                         ,PATTERN_YEAR + "[-/\\\\?]" + PATTERN_MONTH

                         ,PATTERN_YEAR + "[-/\\\\?]" + PATTERN_MONTH_NAME + "[-/\\\\?]" + PATTERN_DATE + "[??\\s]+" + PATTERN_TIME
                         ,PATTERN_YEAR + "[-/\\\\?]" + PATTERN_MONTH_NAME + "[-/\\\\?]" + PATTERN_DATE + "[??\\s]+" + PATTERN_TIME2
                         ,PATTERN_YEAR + "[-/\\\\?]" + PATTERN_MONTH_NAME + "[-/\\\\?]" + PATTERN_DATE + "[??\\s]+" + PATTERN_TIME3
                         ,PATTERN_YEAR + "[-/\\\\?]" + PATTERN_MONTH_NAME + "[-/\\\\?]" + PATTERN_DATE + "[??\\s]+" + PATTERN_TIME_DUAL
                         ,PATTERN_YEAR + "[-/\\\\?]" + PATTERN_MONTH_NAME + "[-/\\\\?]" + PATTERN_DATE + "[??\\s]+" + PATTERN_TIME_DUAL2
                         ,PATTERN_YEAR + "[-/\\\\?]" + PATTERN_MONTH_NAME + "[-/\\\\?]" + PATTERN_DATE + "[??\\s]+" + PATTERN_TIME_DUAL3
                         ,PATTERN_YEAR + "[-/\\\\?]" + PATTERN_MONTH_NAME + "[-/\\\\?]" + PATTERN_DATE
                         ,PATTERN_YEAR + "[-/\\\\?]" + PATTERN_MONTH_NAME

                         ,PATTERN_YEAR + PATTERN_MONTH_NAME + PATTERN_DATE_DUAL + "\\s*" + PATTERN_TIME
                         ,PATTERN_YEAR + PATTERN_MONTH_NAME + PATTERN_DATE_DUAL + "\\s*" + PATTERN_TIME2
                         ,PATTERN_YEAR + PATTERN_MONTH_NAME + PATTERN_DATE_DUAL + "\\s*" + PATTERN_TIME3
                         ,PATTERN_YEAR + PATTERN_MONTH_NAME + PATTERN_DATE_DUAL + "\\s*" + PATTERN_TIME_DUAL
                         ,PATTERN_YEAR + PATTERN_MONTH_NAME + PATTERN_DATE_DUAL + "\\s*" + PATTERN_TIME_DUAL2
                         ,PATTERN_YEAR + PATTERN_MONTH_NAME + PATTERN_DATE_DUAL + "\\s*" + PATTERN_TIME_DUAL3

                         ,PATTERN_YEAR + PATTERN_MONTH_DUAL + PATTERN_DATE_DUAL + "\\s*" + PATTERN_TIME
                         ,PATTERN_YEAR + PATTERN_MONTH_DUAL + PATTERN_DATE_DUAL + "\\s*" + PATTERN_TIME2
                         ,PATTERN_YEAR + PATTERN_MONTH_DUAL + PATTERN_DATE_DUAL + "\\s*" + PATTERN_TIME3
                         ,PATTERN_YEAR + PATTERN_MONTH_DUAL + PATTERN_DATE_DUAL + "\\s*" + PATTERN_TIME_DUAL
                         ,PATTERN_YEAR + PATTERN_MONTH_DUAL + PATTERN_DATE_DUAL + "\\s*" + PATTERN_TIME_DUAL2
                         ,PATTERN_YEAR + PATTERN_MONTH_DUAL + PATTERN_DATE_DUAL + "\\s*" + PATTERN_TIME_DUAL3
                         ,PATTERN_YEAR + PATTERN_MONTH_DUAL + PATTERN_DATE_DUAL

                         ,PATTERN_YEAR
                );
            if(null != m && m.matches())
            {
                int nYear = m.groupCount() >= 1 ? getYear(m.group(1)) : ca.get(Calendar.YEAR);
                int nMonth = m.groupCount() >= 2 ? getMonth(m.group(2)) : 0;
                int nDate = m.groupCount() >= 3 && m.group(3).trim().length() > 0 ? ConvertEx.toInt(m.group(3),1) : 1;
                int nHour = m.groupCount() >= 4 && m.group(4).trim().length() > 0 ? ConvertEx.toInt(m.group(4),0) : 0;
                int nMinutes = m.groupCount() >= 5 && m.group(5).trim().length() > 0 ? ConvertEx.toInt(m.group(5),0) : 0;
                int nSeconds = m.groupCount() >= 6 && m.group(6).trim().length() > 0 ? ConvertEx.toInt(m.group(6),0) : 0;
                ca.set(nYear,nMonth,nDate,nHour,nMinutes,nSeconds);
                ca.set(Calendar.MILLISECOND,0);
                return ca.getTime();
            }
            // Nov 12 2008 12:24:56
            m = matchOne(strDateString
                         ,PATTERN_MONTH_NAME + "[-/\\s\\\\]*" + PATTERN_DATE + "[-/\\s\\\\]*" + PATTERN_YEAR + "\\s*" + PATTERN_TIME2
                         ,PATTERN_MONTH_NAME + "[-/\\s\\\\]*" + PATTERN_DATE + "[-/\\s\\\\]*" + PATTERN_YEAR + "\\s*" + PATTERN_TIME3
                         ,PATTERN_MONTH_NAME + "[-/\\s\\\\]*" + PATTERN_DATE + "[-/\\s\\\\]*" + PATTERN_YEAR + "\\s*" + PATTERN_TIME
                         ,PATTERN_MONTH_NAME + "[-/\\s\\\\]*" + PATTERN_DATE + "[-/\\s\\\\]*" + PATTERN_YEAR + "\\s*" + PATTERN_TIME_DUAL
                         ,PATTERN_MONTH_NAME + "[-/\\s\\\\]*" + PATTERN_DATE + "[-/\\s\\\\]*" + PATTERN_YEAR + "\\s*" + PATTERN_TIME_DUAL2
                         ,PATTERN_MONTH_NAME + "[-/\\s\\\\]*" + PATTERN_DATE + "[-/\\s\\\\]*" + PATTERN_YEAR + "\\s*" + PATTERN_TIME_DUAL3
                         ,PATTERN_MONTH_NAME + "[-/\\s\\\\]*" + PATTERN_DATE + "[-/\\s\\\\]*" + PATTERN_YEAR
                         ,PATTERN_MONTH_NAME + "[-/\\s\\\\]*" + PATTERN_DATE
                );
            if(null != m && m.matches())
            {
                int nMonth = m.groupCount() >= 1 ? getMonth(m.group(1)) : 0;
                int nDate = m.groupCount() >= 2 && m.group(2).length() > 0 ? ConvertEx.toInt(m.group(2),0) : 0;
                int nYear = m.groupCount() >= 3 && m.group(3).length() > 0 ? ConvertEx.toInt(m.group(3),ca.get(Calendar.YEAR)) : ca.get(Calendar.YEAR);
                int nHour = m.groupCount() >= 4 && m.group(4).length() > 0 ? ConvertEx.toInt(m.group(4),0) : 0;
                int nMinutes = m.groupCount() >= 5 && m.group(5).length() > 0 ? ConvertEx.toInt(m.group(5),0) : 0;
                int nSeconds = m.groupCount() >= 6 && m.group(6).length() > 0 ? ConvertEx.toInt(m.group(6),0) : 0;
                ca.set(nYear,nMonth,nDate,nHour,nMinutes,nSeconds);
                ca.set(Calendar.MILLISECOND,0);
                return ca.getTime();
            }

            return null;
        }
    }

    private final static Matcher matchOne(String strInput,String ...allPattern)
    {
        Matcher m = null;
        for(String singlePattern : allPattern)
        {
            m = RegexHelper.getMatcherIgnoreCase("^" + singlePattern + "$",strInput);
            if(m.matches())
            {
                //System.out.println(">>>>" + strInput + "  " + singlePattern);
                break;
            }
        }
//        if(m == null || m.matches() == false)
//        {
//            for(String singlePattern : allPattern)
//            {
//                m = RegexHelper.getMatcherIgnoreCase("^.*" + singlePattern + ".*$",strInput);
//                if(m.matches())
//                {
//                    System.out.println(">>>>" + strInput + " EEEE " + singlePattern);
//                    break;
//                }
//            }
//        }
        return m == null || m.matches() == false ? null : m;
    }

    //Notes that Java Time is UTC time, but DotNet time is local time,
    //So we must minus the time zone offset before return.
    public static java.util.Date dotNetTimeToJavaTime(long dotnetTimeticks)
    {
        if(TickFor197001010000UTC < dotnetTimeticks)
        {
            dotnetTimeticks = (dotnetTimeticks - TickFor197001010000UTC) / 10000;
            long timezoneoffset = java.util.TimeZone.getDefault().getOffset(dotnetTimeticks);
            dotnetTimeticks -= timezoneoffset;
        }
        return new java.util.Date(dotnetTimeticks);
    }

    //Notes that Java Time is UTC time, but DotNet time is local time,
    //So we must add the time zone offset before return.
    public static long javaTimeToDotNetTime(java.util.Date date)
    {
        long ticks = null == date ? 0 : date.getTime();
        ticks = ticks * 10000L + TickFor197001010000UTC;
        long timezoneoffset = java.util.TimeZone.getDefault().getOffset(ticks);
        ticks += timezoneoffset * 10000L;
        return ticks;
    }

}

