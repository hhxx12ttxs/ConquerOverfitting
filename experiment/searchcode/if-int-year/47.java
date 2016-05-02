/**
 * ExtDate.java   1.00    2004/03/10
 * Sinyee Framework.
 * Copyright 2004-2006 SINYEE I.T. Co., Ltd. All rights reserved.
 * @author SINYEE I.T. Co., Ltd.
 */
package com.rainstars.common.util.tool;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Function	:  拡張日付処理クラス
 *
 * @version 1.00
 * @author  Tayu      2004/03/10  New
 */

public class ExtDate {

   
    private static String[] japaneseWeekdayNameArray = new String[]{"日", "月", "火", "水", "木", "金", "土"};

    /**
     * コンストラクタ
     */
    public ExtDate() {

    }


    /**
     * Function:日付チェック
     * 明治元年(1868/09/08)からの日付を対象に判断します。
     *        ただし、DBMSによっては日付を格納できない場合もありますので
     *        注意してください。<br>
     * @param month	月
     * @param day		日
     * @param year		年
     * @return	true.正しい; false.誤り
     */
    public static boolean isDate(String month, String day, String year) {
        String targetDate = "";
        // 数字チェック
        
        if (ExtNumeric.isNumeric(year) && ExtNumeric.isNumeric(month) && ExtNumeric.isNumeric(day)) {
            // 明治元年(1868/09/08)以前はエラーとします
            targetDate = ExtNumeric.zeroFormat(year + "", 4) + ExtNumeric.zeroFormat(month + "", 2) + ExtNumeric.zeroFormat(day + "", 2);
            if (targetDate.compareTo("18680908") < 0) {
                return false;
            }
        } else {
            return false;
        }

        // 数字チェックもあわせて行います
        //		 if (checkdate(month, day, year)) {
        //			 return true;
        //		 }
        //		 else {
        //			 return false;
        //		 }
        return checkDate(month, day, year);
    }

    /**
     * Function: 日付チェック<br>
     * @param smonth
     * @param sday
     * @param syear
     * @return
     */
    public static boolean checkDate(String smonth, String sday, String syear) {
        int iyear = Integer.parseInt(syear);
        int imonth = Integer.parseInt(smonth);
        int iday = Integer.parseInt(sday);

        return checkDate(imonth, iday, iyear);
    }

    public static boolean checkDate(int imonth, int iday, int iyear) {
//        Date tdate = new Date();
//        tdate.setYear(iyear);
//        tdate.setMonth(imonth - 1);
//        tdate.setDate(iday);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR,iyear);
        cal.set(Calendar.MONTH,imonth-1);
        cal.set(Calendar.DATE,iday);
        //System.out.println(KanamicTool.date("Ymd", tdate));

        //if (tdate.getYear() == iyear && tdate.getMonth() + 1 == imonth && tdate.getDate() == iday) {
        if (cal.get(Calendar.YEAR) == iyear && cal.get(Calendar.MONTH) + 1 == imonth && cal.get(Calendar.DATE) == iday) { 
        	return true;
        } else {
            return false;
        }
    }




    /**
     * Function:時間チェック<br>
     * @param hour			時
     * @param minute		分
     * @param second		秒
     * @return	true.正しい; false.誤り
     */
    public static boolean isTime(String strhour, String strminute) {
        return (isTime(strhour, strminute, "00"));
    }

    /**
     * Function:時間チェック<br>
     * @param hour			時
     * @param minute		分
     * @param second		秒
     * @return	true.正しい; false.誤り
     */
    public static boolean isTime(String strhour, String strminute, String strsecond) {
        int hour = 0;
        int minute = 0;
        int second = 0;
        try {

            hour = Integer.parseInt(strhour);
            minute = Integer.parseInt(strminute);

            if (strsecond == null || "".equals(strsecond)) {
                second = 0;
            } else {
                second = Integer.parseInt(strsecond);
            }
        } catch (Exception e) {
            return false;
        }
        // 時間
        if (ExtNumeric.isNumeric(strhour)) {
            if ((hour < 0) || (hour > 24)) {
                return false;
            }
        } else {
            return false;
        }

        // 分
        if (ExtNumeric.isNumeric(strminute)) {
            if ((minute < 0) || (minute > 59)) {
                return false;
            }
        } else {
            return false;
        }

        // 秒
        if (ExtNumeric.isNumeric(strsecond)) {

            if ((second < 0) || (second > 59)) {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }


    /**
     * make time
     * @param hour
     * @param minute
     * @param second
     * @param month
     * @param day
     * @param year
     * @return
     */
    public static Date mktime(String hour, String minute, String second, String month, String day, String year) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
        if (month.length() < 2) month = "0" + month;
        if (day.length() < 2) day = "0" + day;
        if (hour.length() < 2) hour = "0" + hour;
        if (minute.length() < 2) minute = "0" + minute;
        if (second.length() < 2) second = "0" + second;
        String tempdate = year + month + day + hour + minute + second;
        Date date = null;
        try {
            date = sdf.parse(tempdate);
        } catch (ParseException e) {
        }
        return date;
    }
    /**
     * 	make time
     * @param hour
     * @param minute
     * @param second
     * @param month
     * @param day
     * @param year
     * @return
     */
    public static Date mktime(int hour, int minute, int second, int month, int day, int year) {
        return mktime(String.valueOf(hour), String.valueOf(minute), String.valueOf(second),
                String.valueOf(month), String.valueOf(day), String.valueOf(year));
    }

    /**
     * Function:曜日取得(数字)<br>
     * @param month	月
     * @param day		日
     * @param year		年
     * @return	曜日番号 (0日曜 〜 6土曜)
     */
    public static int getWeekdayNumber(int month, int day, int year) {
        // week = date("w", mktime(0, 0, 0, month, day, year));
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        return (calendar.get(Calendar.DAY_OF_WEEK) - 1);
    }

    public static int getWeekdayNumber(String smonth, String sday, String syear) {
        int month = Integer.parseInt(smonth);
        int day = Integer.parseInt(sday);
        int year = Integer.parseInt(syear);

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        
        //return getWeekdayNumber(month, day, year);
        return (calendar.get(Calendar.DAY_OF_WEEK) - 1);
    }


    /**
     * Function:曜日取得<br>
     * @param month		月
     * @param day			日
     * @param year			年
     * @return	曜日(Sun 〜 Sat)
     */
    public static String getWeekdayName(int month, int day, int year) {
        String[] englishName = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        return englishName[getWeekdayNumber(month, day, year)];
    }



    /**
	 * Function:曜日取得(日本語)<br>
	 * @param month	月
	 * @param day		日
	 * @param year		年
	 * @return	曜日(日 〜 土)
	 */
    public static String getWeekdayNameJ(int month, int day, int year) {
        int week = getWeekdayNumber(month, day, year);
        return (japaneseWeekdayNameArray[week]);
    }

    public static String getWeekdayNameJ(String amonth, String aday, String ayear) {
        int month = Integer.parseInt(amonth);
        int day = Integer.parseInt(aday);
        int year = Integer.parseInt(ayear);
        return getWeekdayNameJ(month, day, year);
    }

    /**
     * Function:何週取得<br>
     * @param month		月
     * @param day			日
     * @param year			年
     * @return				何週 (1 〜 )
     */
    public static int getWeek(int month, int day, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        return (calendar.get(Calendar.WEEK_OF_YEAR));
    }


  
    /**
     * Function:月の何週<br>
     * @param month
     * @param day
     * @param year
     * @return
     */
    public static int getWeekOfMonth(int month, int day, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        return (calendar.get(Calendar.WEEK_OF_MONTH));
    }

    /**
     * Function:年間通算日<br>
     * @param month
     * @param day
     * @param year
     * @return
     */
    public static int getDayOfYear(int month, int day, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        return (calendar.get(Calendar.DAY_OF_YEAR));
    }
    /**
	 * Function:週始め日取得<br>
	 * @param week	何週
	 * @param month	月
	 * @param year		年
	 * @return	週始め日
	 */
    public static int getBeginWeek(int week, int month, int year) {
        int startday;
        if (week == 1) {
            //	1週目は 1日固定
            startday = 1;
        } else {
            // 2週目以降は計算にて取得

            // 月初曜日取得
            int beginweekday = getWeekdayNumber(month, 1, year);

            // 最初の週始め日取得
            if (beginweekday == 0) {
                // 日曜日の場合
                startday = 1;
            } else {
                // 日曜日以外
                startday = (1 + (7 - beginweekday));
            }

            for (int i = 2; i < week; i++) {
                startday = startday + 7;
            }
        }

        return (startday);
    }

    /**
     * Function:月名称取得<br>
     * @param month	月
     * @return			名称 (Jan 〜 Dec)
     */
    public static String getMonthName(int month) {
        String[] englishMonthName = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Agu", "Sep", "Oct", "Nov", "Dec"};
        return englishMonthName[month - 1];
    }

    
}

