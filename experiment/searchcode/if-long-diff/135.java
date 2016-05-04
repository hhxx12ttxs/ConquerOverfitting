package com.hs.core.utils;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
*
* ????????????????????? ?????????????? ??????????????????????????????
* SimpleDateFormat?????.
*
*/
public final class TimeUtil implements Serializable {
private static final long serialVersionUID = -3098985139095632110L;

private TimeUtil() {
}


public static void main(String[] args){
	System.out.println(getCurrentstr());
}
public static String getCurrentstr(){
	return getFormatDateTime(new Date(), "yyyyMMdd");
}
public static String getCurrentstr1(){
	return getFormatDateTime(new Date(), "yyyy-MM-dd");
}
/**
*
* ?????????
*
* @param sdate
*            ?? yyyy-MM-dd
*
* @param format
*            ????????
*
* @return ?????????
*
*/
public static String dateFormat(String sdate, String format) {
SimpleDateFormat formatter = new SimpleDateFormat(format);
java.sql.Date date = java.sql.Date.valueOf(sdate);
String dateString = formatter.format(date);
return dateString;
}

/**
*
* ?????????
*
* @param sd
*            ???????yyyy-MM-dd
*
* @param ed
*            ???????yyyy-MM-dd
*
* @return ????????
*
*/
public static long getIntervalDays(String sd, String ed) {
return ((java.sql.Date.valueOf(ed)).getTime() - (java.sql.Date
.valueOf(sd)).getTime())
/ (3600 * 24 * 1000);
}

/**
*
* ????yyyy-MM????yyyy-MM????????
*
* @param beginMonth
*            ???yyyy-MM
*
* @param endMonth
*            ???yyyy-MM
*
* @return ??
*
*/
public static int getInterval(String beginMonth, String endMonth) {
int intBeginYear = Integer.parseInt(beginMonth.substring(0, 4));
int intBeginMonth = Integer.parseInt(beginMonth.substring(beginMonth
.indexOf("-") + 1));
int intEndYear = Integer.parseInt(endMonth.substring(0, 4));
int intEndMonth = Integer.parseInt(endMonth.substring(endMonth
.indexOf("-") + 1));
return ((intEndYear - intBeginYear) * 12)
+ (intEndMonth - intBeginMonth) + 1;
}

/**
*
* ??????????(Date???)??????????????
*
* @param date
*            ?????
*
* @param format
*            ???????
*
* @return String ??????????.
*
*/
public static String getFormatDateTime(Date date, String format) {
SimpleDateFormat sdf = new SimpleDateFormat(format);
return sdf.format(date);
}

/**
*
* ????????????yyyy????.
*
* @return ???? yyyy
*
*/
public static String getCurrentYear() {
return getFormatDateTime(new Date(), "yyyy");
}

/**
* ???????????????2009?????????2008
*
* @return ???????? yyyy
*
*/
public static String getLastYear() {
String currentYear = getCurrentYear();
int beforeYear = Integer.parseInt(currentYear) - 1;
return "" + beforeYear;
}

/**
* ???????????MM????.
*
* @return ???? MM
*
*/
public static String getCurrentMonth() {
return getFormatDateTime(new Date(), "MM");
}

/**
*
* ?????????????"dd"??.
*
* @return ???????dd
*
*/
public static String getCurrentDay() {
return getFormatDateTime(new Date(), "dd");
}

/**
*
* ?????????? ???yyyy-MM-dd
*
* @return String ??????????.
*
*/
public static String getCurrentDate() {
return getFormatDateTime(new Date(), "yyyy-MM-dd");
}

/**
*
* ??????????????yyyy-MM-dd HH:mm:ss
*
* @return ???yyyy-MM-dd HH:mm:ss???19??
*
*/
public static String getCurrentDateTime() {
return getFormatDateTime(new Date(), "yyyy-MM-dd HH:mm:ss");
}

/**
*
* ?????????????yyyy-MM-dd
*
* @param date
*            ??
*
* @return String ??????????.
*
*/
public static String getFormatDate(Date date) {
return getFormatDateTime(date, "yyyy-MM-dd");
}

/**
*
* ?????????? ???HH:mm:ss
*
* @return String ??????????.
*
*/
public static String getCurrentTime() {
return getFormatDateTime(new Date(), "HH:mm:ss");
}

/**
*
* ?????????? ???yyyy-MM-dd HH:mm:ss
*
* @param date
*            ??
*
* @return String ??????????.
*
*/
public static String getFormatTime(Date date) {
return getFormatDateTime(date, "yyyy-MM-dd HH:mm:ss");
}

/**
*
* ????????????.
*
* @param year
*            ?
*
* @param month
*            ?????1?12
*
* @param day
*            ?
*
* @return ??java.util.Date()?????
*
*/
public static Date getDateObj(int year, int month, int day) {
Calendar c = new GregorianCalendar();
c.set(year, month - 1, day);
return c.getTime();
}

/**
*
* ???????????
*
* @param date
*            yyyy/MM/dd ?? yyyy-MM-dd
*
* @return yyyy/MM/dd
*
*/
public static String getDateTomorrow(String date) {
Date tempDate = null;
if (date.indexOf("/") > 0)
tempDate = getDateObj(date, "[/]");
if (date.indexOf("-") > 0)
tempDate = getDateObj(date, "[-]");
tempDate = getDateAdd(tempDate, 1);
return getFormatDateTime(tempDate, "yyyy/MM/dd");
}

/**
*
* ?????????????????
*
* @param date
*            yyyy/MM/dd
*
* @param offset
*            ???
*
* @return yyyy/MM/dd
*/
public static String getDateOffset(String date, int offset) {
Date tempDate = null;
if (date.indexOf("/") > 0)
tempDate = getDateObj(date, "[/]");
if (date.indexOf("-") > 0)
tempDate = getDateObj(date, "[-]");
tempDate = getDateAdd(tempDate, offset);
return getFormatDateTime(tempDate, "yyyy/MM/dd");
}

/**
*
* ??????????????????.
*
* @param argsDate
*            ???"yyyy-MM-dd"
*
* @param split
*
* ???????????"-"?"/"??????????
*
* @return ??java.util.Date()?????
*
*/
public static Date getDateObj(String argsDate, String split) {
String[] temp = argsDate.split(split);
int year = new Integer(temp[0]).intValue();
int month = new Integer(temp[1]).intValue();
int day = new Integer(temp[2]).intValue();
return getDateObj(year, month, day);
}

/**
*
* ?????????????????????pattern?????.
*
* @param dateStr
*
* ???? ??????????????????????????????????????? ???????????????
* parse(String, ParsePosition) ??? ?? String???????????
*
* @param pattern
*            ????
*
* @return ?????????????
*
*/
public static Date getDateFromString(String dateStr, String pattern) {
SimpleDateFormat sdf = new SimpleDateFormat(pattern);
Date resDate = null;
try {
resDate = sdf.parse(dateStr);
} catch (Exception e) {
e.printStackTrace();
}
return resDate;
}

/**
* ????Date??.
*
* @return Date ??Date??.
*/
public static Date getDateObj() {
Calendar c = new GregorianCalendar();
return c.getTime();
}

/**
* @return ?????????
*
*/
public static int getDaysOfCurMonth() {
int curyear = new Integer(getCurrentYear()).intValue(); // ????
int curMonth = new Integer(getCurrentMonth()).intValue();// ????
int mArray[] = new int[] { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30,
31 };
// ??????? ?2???29??
if ((curyear % 400 == 0)
|| ((curyear % 100 != 0) && (curyear % 4 == 0))) {
mArray[1] = 29;
}
return mArray[curMonth - 1];
// ??????????????????12???????????
// ??????????????????1???????????
}

/**
* ??????? ???????yyyy-MM??????
*
* @param time
*            yyyy-MM
*
* @return ???????????
*/
public static int getDaysOfCurMonth(final String time) {
if (time.length() != 7) {
throw new NullPointerException("????????yyyy-MM");
}
String[] timeArray = time.split("-");
int curyear = new Integer(timeArray[0]).intValue(); // ????
int curMonth = new Integer(timeArray[1]).intValue();// ????
if (curMonth > 12) {
throw new NullPointerException("????????yyyy-MM???????????12?");
}
int mArray[] = new int[] { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30,
31 };
// ??????? ?2???29??
if ((curyear % 400 == 0)
|| ((curyear % 100 != 0) && (curyear % 4 == 0))) {
mArray[1] = 29;
}
if (curMonth == 12) {
return mArray[0];
}
return mArray[curMonth - 1];
// ??????????????????12???????????
// ??????????????????1???????????
}

/**
*
* ????????year??month??????weekOfMonth?????dayOfWeek????????
*
* @param year
*            ???yyyy
*
* @param month
*            ???MM,????[1-12]
*
* @param weekOfMonth
*            ?[1-6],????????6??
*
* @param dayOfWeek
*
* ???1?7?????1?7?1??????7?????
*
* @return int
*/
public static int getDayofWeekInMonth(String year, String month,
String weekOfMonth, String dayOfWeek) {
Calendar cal = new GregorianCalendar();
// ???????????????????????????? GregorianCalendar?
int y = new Integer(year).intValue();
int m = new Integer(month).intValue();
cal.clear();// ????????
cal.set(y, m - 1, 1);// ?????????????
cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, new Integer(weekOfMonth)
.intValue());
cal.set(Calendar.DAY_OF_WEEK, new Integer(dayOfWeek).intValue());
return cal.get(Calendar.DAY_OF_MONTH);
}

/**
*
* ?????????????????java.Util.Date???
*
* @param year
*            ?
*
* @param month?
*            0-11
*
* @param date
*            ?
*
* @param hourOfDay
*            ?? 0-23
*
* @param minute?
*            0-59
*
* @param second
*            ? 0-59
*
* @return ??Date???
*
*/
public static Date getDate(int year, int month, int date, int hourOfDay,
int minute, int second) {
Calendar cal = new GregorianCalendar();
cal.set(year, month, date, hourOfDay, minute, second);
return cal.getTime();
}

/**
*
* ???????????????????1??????2??????7??????
*
*
*
* @param year
*
* @param month
*
* month??1???12??
*
* @param day
*
* @return ??????????????????1??????2??????7??????
*
*/
public static int getDayOfWeek(String year, String month, String day) {
Calendar cal = new GregorianCalendar(new Integer(year).intValue(),
new Integer(month).intValue() - 1, new Integer(day).intValue());
return cal.get(Calendar.DAY_OF_WEEK);
}

/**
*
* ???????????????????1??????2??????7??????
*
* @param date
*            "yyyy/MM/dd",??"yyyy-MM-dd"
*
* @return ??????????????????1??????2??????7??????
*
*/
public static int getDayOfWeek(String date) {
String[] temp = null;
if (date.indexOf("/") > 0) {
temp = date.split("/");
}
if (date.indexOf("-") > 0) {
temp = date.split("-");
}
return getDayOfWeek(temp[0], temp[1], temp[2]);
}

/**
*
* ????????????????????????????
*
* @param date
*            ??? yyyy/MM/dd ?? yyyy-MM-dd
*
* @return ??????????
*
*/
public static String getChinaDayOfWeek(String date) {
String[] weeks = new String[] { "???", "???", "???", "???", "???",
"???", "???" };
int week = getDayOfWeek(date);
return weeks[week - 1];
}

/**
*
* ???????????????????1??????2??????7??????
*
* @param date
*
* @return ??????????????????1??????2??????7??????
*
*/
public static int getDayOfWeek(Date date) {
Calendar cal = new GregorianCalendar();
cal.setTime(date);
return cal.get(Calendar.DAY_OF_WEEK);
}

/**
*
* ????????????????????
*
* @param year
*
* @param month
*            ??1-12<br>
*
* @param day
*
* @return int
*
*/
public static int getWeekOfYear(String year, String month, String day) {
Calendar cal = new GregorianCalendar();
cal.clear();
cal.set(new Integer(year).intValue(),
new Integer(month).intValue() - 1, new Integer(day).intValue());
return cal.get(Calendar.WEEK_OF_YEAR);
}

/**
*
* ??????????????????.
*
* @param date
*            ???????
*
* @param amount
*            ????????????????????????.
*
* @return Date ?????????Date??.
*
*/
public static Date getDateAdd(Date date, int amount) {
Calendar cal = new GregorianCalendar();
cal.setTime(date);
cal.add(GregorianCalendar.DATE, amount);
return cal.getTime();
}

/**
*
* ??????????????????.
*
* @param date
*            ???????
*
* @param amount
*            ????????????????????????.
*
* @param format
*            ????.
*
* @return Date ?????????Date??.
*
*/
public static String getFormatDateAdd(Date date, int amount, String format) {
Calendar cal = new GregorianCalendar();
cal.setTime(date);
cal.add(GregorianCalendar.DATE, amount);
return getFormatDateTime(cal.getTime(), format);
}

/**
*
* ??????????????????60?dateAdd(-60)
*
* @param amount
*            ????????????????????
*
* @param format
*            ???????.
*
* @return java.lang.String ???????????????.
*
*/
public static String getFormatCurrentAdd(int amount, String format) {
Date d = getDateAdd(new Date(), amount);
return getFormatDateTime(d, format);
}

/**
*
* ??????????????
*
* @param format
*            ???????
*
* @return String ??????????.
*
*/
public static String getFormatYestoday(String format) {
return getFormatCurrentAdd(-1, format);
}

/**
*
* ???????????<br>
*
* @param sourceDate
*
* @param format
*            yyyy MM dd hh mm ss
*
* @return ???????????formcat???
*
*/
public static String getYestoday(String sourceDate, String format) {
return getFormatDateAdd(getDateFromString(sourceDate, format), -1,
format);
}

/**
*
* ????????<br>
*
* @param format
*
* @return ???????????formcat???
*
*/
public static String getFormatTomorrow(String format) {
return getFormatCurrentAdd(1, format);
}

/**
*
* ???????????<br>
*
* @param sourceDate
*
* @param format
*
* @return ???????????formcat???
*
*/
public static String getFormatDateTommorrow(String sourceDate, String format) {
return getFormatDateAdd(getDateFromString(sourceDate, format), 1,
format);
}

/**
*
* ??????? TimeZone???????????????
*
* @param dateFormat
*
* @return ???????????formcat???
*
*/
public static String getCurrentDateString(String dateFormat) {
Calendar cal = Calendar.getInstance(TimeZone.getDefault());
SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
sdf.setTimeZone(TimeZone.getDefault());
return sdf.format(cal.getTime());
}

/**
*
* ???????????????? ?getFormatDate(String format)???
*
* @param format
*            yyyy MM dd hh mm ss
*
* @return ?????????
*
*/
public static String getCurTimeByFormat(String format) {
Date newdate = new Date(System.currentTimeMillis());
SimpleDateFormat sdf = new SimpleDateFormat(format);
return sdf.format(newdate);
}

/**
*
* ?????????????????
*
* @param startTime
*            ???? yyyy-MM-dd HH:mm:ss
*
* @param endTime
*            ???? yyyy-MM-dd HH:mm:ss
*
* @return ???????(?)
*
*/
public static long getDiff(String startTime, String endTime) {
long diff = 0;
SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
try {
Date startDate = ft.parse(startTime);
Date endDate = ft.parse(endTime);
diff = startDate.getTime() - endDate.getTime();
diff = diff / 1000;
} catch (ParseException e) {
e.printStackTrace();
}
return diff;
}

/**
*
* ????/??/?
*
* @param second
*            ?
*
* @return ??????????????????3??23??13??
*
*/
public static String getHour(long second) {
long hour = second / 60 / 60;
long minute = (second - hour * 60 * 60) / 60;
long sec = (second - hour * 60 * 60) - minute * 60;
return hour + "??" + minute + "??" + sec + "?";
}

/**
*
* ??????????
*
* ???yyyy-MM-dd HH:mm:ss
*
* @return String ??????????.
*
*/
public static String getDateTime(long microsecond) {
return getFormatDateTime(new Date(microsecond), "yyyy-MM-dd HH:mm:ss");
}

/**
*
* ??????????????????
*
* ???yyyy-MM-dd HH:mm:ss
*
* @return Float ??????.
*
*/
public static String getDateByAddFltHour(float flt) {
int addMinute = (int) (flt * 60);
Calendar cal = new GregorianCalendar();
cal.setTime(new Date());
cal.add(GregorianCalendar.MINUTE, addMinute);
return getFormatDateTime(cal.getTime(), "yyyy-MM-dd HH:mm:ss");
}

/**
*
* ???????????????????
*
* ???yyyy-MM-dd HH:mm:ss
*
* @return ??.
*
*/
public static String getDateByAddHour(String datetime, int minute) {
String returnTime = null;
Calendar cal = new GregorianCalendar();
SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
Date date;
try {
date = ft.parse(datetime);
cal.setTime(date);
cal.add(GregorianCalendar.MINUTE, minute);
returnTime = getFormatDateTime(cal.getTime(), "yyyy-MM-dd HH:mm:ss");
} catch (ParseException e) {
e.printStackTrace();
}
return returnTime;
}

/**
*
* ??????????????????
*
* @param startTime
*            ???? yyyy-MM-dd HH:mm:ss
*
* @param endTime
*            ???? yyyy-MM-dd HH:mm:ss
*
* @return ???????(?)
*
*/
public static int getDiffHour(String startTime, String endTime) {
long diff = 0;
SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
try {
Date startDate = ft.parse(startTime);
Date endDate = ft.parse(endTime);
diff = startDate.getTime() - endDate.getTime();
diff = diff / (1000 * 60 * 60);
} catch (ParseException e) {
e.printStackTrace();
}
return new Long(diff).intValue();
}

/**
*
* ?????????
*
* @param selectName
*            ?????
*
* @param value
*            ???????
*
* @param startYear????
*
* @param endYear
*            ????
*
* @return ??????html
*
*/
public static String getYearSelect(String selectName, String value,
int startYear, int endYear) {
int start = startYear;
int end = endYear;
if (startYear > endYear) {
start = endYear;
end = startYear;
}
StringBuffer sb = new StringBuffer("");
sb.append("<select name=\"" + selectName + "\">");
for (int i = start; i <= end; i++) {
if (!value.trim().equals("") && i == Integer.parseInt(value)) {
sb.append("<option value=\"" + i + "\" selected>" + i
+ "</option>");
} else {
sb.append("<option value=\"" + i + "\">" + i + "</option>");
}
}
sb.append("</select>");
return sb.toString();
}

/**
*
* ?????????
*
* @param selectName?????
*
* @param value???????
*
* @param startYear????
*
* @param endYear????
*
* ???????2001?????2005????????????2001?2002?2003?2004?2005??
*
* @return ?????????html?
*
*/
public static String getYearSelect(String selectName, String value,
int startYear, int endYear, boolean hasBlank) {
int start = startYear;
int end = endYear;
if (startYear > endYear) {
start = endYear;
end = startYear;
}
StringBuffer sb = new StringBuffer("");
sb.append("<select name=\"" + selectName + "\">");
if (hasBlank) {
sb.append("<option value=\"\"></option>");
}
for (int i = start; i <= end; i++) {

if (!value.trim().equals("") && i == Integer.parseInt(value)) {
sb.append("<option value=\"" + i + "\" selected>" + i
+ "</option>");
} else {
sb.append("<option value=\"" + i + "\">" + i + "</option>");
}
}
sb.append("</select>");
return sb.toString();
}

/**
*
* ?????????
*
* @param selectName
*            ?????
*
* @param value???????
*
* @param startYear????
*
* @param endYear
*            ????
*
* @param js
*
* ???js?js?????? " onchange=\"changeYear()\" "
*
* ,????js???????jsp???????????
*
* @return ?????????
*
*/
public static String getYearSelect(String selectName, String value,
int startYear, int endYear, boolean hasBlank, String js) {
int start = startYear;
int end = endYear;
if (startYear > endYear) {
start = endYear;
end = startYear;
}
StringBuffer sb = new StringBuffer("");
sb.append("<select name=\"" + selectName + "\" " + js + ">");
if (hasBlank) {
sb.append("<option value=\"\"></option>");
}
for (int i = start; i <= end; i++) {
if (!value.trim().equals("") && i == Integer.parseInt(value)) {
sb.append("<option value=\"" + i + "\" selected>" + i
+ "</option>");
} else {
sb.append("<option value=\"" + i + "\">" + i + "</option>");
}
}
sb.append("</select>");
return sb.toString();
}

/**
*
* ?????????
*
* @param selectName
*            ?????
*
* @param value
*            ???????
*
* @param startYear
*            ????
*
* @param endYear
*            ????
*
* @param js
*
* ???js?js?????? " onchange=\"changeYear()\" "
*
* ,????js???????jsp???????????
*
* @return ?????????
*
*/
public static String getYearSelect(String selectName, String value,
int startYear, int endYear, String js) {
int start = startYear;
int end = endYear;
if (startYear > endYear) {
start = endYear;
end = startYear;
}
StringBuffer sb = new StringBuffer("");
sb.append("<select name=\"" + selectName + "\" " + js + ">");
for (int i = start; i <= end; i++) {
if (!value.trim().equals("") && i == Integer.parseInt(value)) {
sb.append("<option value=\"" + i + "\" selected>" + i
+ "</option>");
} else {
sb.append("<option value=\"" + i + "\">" + i + "</option>");
}
}
sb.append("</select>");
return sb.toString();
}

/**
*
* ????????
*
* @param selectName
*
* @param value
*
* @param hasBlank
*
* @return ?????????
*
*/
public static String getMonthSelect(String selectName, String value,
boolean hasBlank) {
StringBuffer sb = new StringBuffer("");
sb.append("<select name=\"" + selectName + "\">");
if (hasBlank) {
sb.append("<option value=\"\"></option>");
}
for (int i = 1; i <= 12; i++) {
if (!value.trim().equals("") && i == Integer.parseInt(value)) {
sb.append("<option value=\"" + i + "\" selected>" + i
+ "</option>");
} else {
sb.append("<option value=\"" + i + "\">" + i + "</option>");
}
}
sb.append("</select>");
return sb.toString();
}

/**
*
* ????????
*
* @param selectName
*
* @param value
*
* @param hasBlank
*
* @param js
*
* @return ?????????
*
*/
public static String getMonthSelect(String selectName, String value,
boolean hasBlank, String js) {
StringBuffer sb = new StringBuffer("");
sb.append("<select name=\"" + selectName + "\" " + js + ">");
if (hasBlank) {
sb.append("<option value=\"\"></option>");
}
for (int i = 1; i <= 12; i++) {
if (!value.trim().equals("") && i == Integer.parseInt(value)) {
sb.append("<option value=\"" + i + "\" selected>" + i
+ "</option>");
} else {
sb.append("<option value=\"" + i + "\">" + i + "</option>");
}
}
sb.append("</select>");
return sb.toString();
}

/**
*
* ????????????1-31? ????????????????????
*
* @param selectName
*
* @param value
*
* @param hasBlank
*
* @return ???????
*
*/
public static String getDaySelect(String selectName, String value,
boolean hasBlank) {
StringBuffer sb = new StringBuffer("");
sb.append("<select name=\"" + selectName + "\">");
if (hasBlank) {
sb.append("<option value=\"\"></option>");
}
for (int i = 1; i <= 31; i++) {
if (!value.trim().equals("") && i == Integer.parseInt(value)) {
sb.append("<option value=\"" + i + "\" selected>" + i
+ "</option>");
} else {
sb.append("<option value=\"" + i + "\">" + i + "</option>");
}
}
sb.append("</select>");
return sb.toString();
}

/**
*
* ????????????1-31
*
* @param selectName
*
* @param value
*
* @param hasBlank
*
* @param js
*
* @return ???????
*
*/
public static String getDaySelect(String selectName, String value,
boolean hasBlank, String js) {
StringBuffer sb = new StringBuffer("");
sb.append("<select name=\"" + selectName + "\" " + js + ">");
if (hasBlank) {
sb.append("<option value=\"\"></option>");
}
for (int i = 1; i <= 31; i++) {
if (!value.trim().equals("") && i == Integer.parseInt(value)) {
sb.append("<option value=\"" + i + "\" selected>" + i
+ "</option>");
} else {
sb.append("<option value=\"" + i + "\">" + i + "</option>");
}
}
sb.append("</select>");
return sb.toString();
}

/**
*
* ????????????????????????????????????2????4??????? ??????????????????
*
* ?????????????????
*
* @param startDate
*
* ???? ???"yyyy/MM/dd" ??"yyyy-MM-dd"
*
* @param endDate
*
* ???? ???"yyyy/MM/dd"??"yyyy-MM-dd"
*
* @return int
*
*/
public static int countWeekend(String startDate, String endDate) {
int result = 0;
Date sdate = null;
Date edate = null;
if (startDate.indexOf("/") > 0 && endDate.indexOf("/") > 0) {
sdate = getDateObj(startDate, "/"); // ????
edate = getDateObj(endDate, "/");// ????
}
if (startDate.indexOf("-") > 0 && endDate.indexOf("-") > 0) {
sdate = getDateObj(startDate, "-"); // ????
edate = getDateObj(endDate, "-");// ????
}
// ?????????????????????????
int sumDays = Math.abs(getDiffDays(startDate, endDate));
int dayOfWeek = 0;
for (int i = 0; i <= sumDays; i++) {
dayOfWeek = getDayOfWeek(getDateAdd(sdate, i)); // ?????????
if (dayOfWeek == 1 || dayOfWeek == 7) { // 1 ??? 7???
result++;
}
}
return result;
}

/**
*
* ??????????????
*
* ?????????????????
*
* @param startDate
*
* ??"yyyy/MM/dd" ??"yyyy-MM-dd"
*
* @param endDate
*
* ??"yyyy/MM/dd" ??"yyyy-MM-dd"
*
* @return ???
*
*/
public static int getDiffDays(String startDate, String endDate) {
long diff = 0;
SimpleDateFormat ft = null;
if (startDate.indexOf("/") > 0 && endDate.indexOf("/") > 0) {
ft = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
}
if (startDate.indexOf("-") > 0 && endDate.indexOf("-") > 0) {
ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}
try {
Date sDate = ft.parse(startDate + " 00:00:00");
Date eDate = ft.parse(endDate + " 00:00:00");
diff = eDate.getTime() - sDate.getTime();
diff = diff / 86400000;// 1000*60*60*24;
} catch (ParseException e) {
e.printStackTrace();
}
return (int) diff;
}

/**
*
* ???????????????(???????????)? ???2007/07/01 ?2007/07/03 ,??????
*
* {"2007/07/01?,"2007/07/02?,"2007/07/03?}
*
* ?????????????????
*
* @param startDate
*
* ??"yyyy/MM/dd"?? yyyy-MM-dd
*
* @param endDate
*
* ??"yyyy/MM/dd"?? yyyy-MM-dd
*
* @return ???????????
*
*/
public static String[] getArrayDiffDays(String startDate, String endDate) {
int LEN = 0; // ??????????????
// ?????????????
if (startDate.equals(endDate)) {
return new String[] { startDate };
}
Date sdate = null;
if (startDate.indexOf("/") > 0 && endDate.indexOf("/") > 0) {
sdate = getDateObj(startDate, "/"); // ????
}
if (startDate.indexOf("-") > 0 && endDate.indexOf("-") > 0) {
sdate = getDateObj(startDate, "-"); // ????
}
LEN = getDiffDays(startDate, endDate);
String[] dateResult = new String[LEN + 1];
dateResult[0] = startDate;
for (int i = 1; i < LEN + 1; i++) {
if (startDate.indexOf("/") > 0 && endDate.indexOf("/") > 0) {
dateResult[i] = getFormatDateTime(getDateAdd(sdate, i),
"yyyy/MM/dd");
}
if (startDate.indexOf("-") > 0 && endDate.indexOf("-") > 0) {
dateResult[i] = getFormatDateTime(getDateAdd(sdate, i),
"yyyy-MM-dd");
}
}
return dateResult;
}

/**
*
* ?????????????????????
*
* @param srcDate
*
* ???? yyyy/MM/dd ?? yyyy-MM-dd
*
* @param startDate
*
* ???? yyyy/MM/dd ?? yyyy-MM-dd
*
* @param endDate
*
* ???? yyyy/MM/dd ?? yyyy-MM-dd
*
* @return ?????????????????????true?????false
*
*/
public static boolean isInStartEnd(String srcDate, String startDate,
String endDate) {
if (startDate.compareTo(srcDate) <= 0
&& endDate.compareTo(srcDate) >= 0) {
return true;
} else {
return false;
}
}

/**
*
* ????????????1-4? ????????????????????
*
* @param selectName
*
* @param value
*
* @param hasBlank
*
* @return ????????
*
*/
public static String getQuarterSelect(String selectName, String value,
boolean hasBlank) {
StringBuffer sb = new StringBuffer("");
sb.append("<select name=\"" + selectName + "\">");
if (hasBlank) {
sb.append("<option value=\"\"></option>");
}
for (int i = 1; i <= 4; i++) {
if (!value.trim().equals("") && i == Integer.parseInt(value)) {
sb.append("<option value=\"" + i + "\" selected>" + i
+ "</option>");
} else {
sb.append("<option value=\"" + i + "\">" + i + "</option>");
}
}
sb.append("</select>");
return sb.toString();
}

/**
*
* ?????????????1-4
*
* @param selectName
*
* @param value
*
* @param hasBlank
*
* @param js
*
* @return ????????
*
*/
public static String getQuarterSelect(String selectName, String value,
boolean hasBlank, String js) {
StringBuffer sb = new StringBuffer("");
sb.append("<select name=\"" + selectName + "\" " + js + ">");
if (hasBlank) {
sb.append("<option value=\"\"></option>");
}
for (int i = 1; i <= 4; i++) {
if (!value.trim().equals("") && i == Integer.parseInt(value)) {
sb.append("<option value=\"" + i + "\" selected>" + i
+ "</option>");
} else {
sb.append("<option value=\"" + i + "\">" + i + "</option>");
}
}
sb.append("</select>");
return sb.toString();
}

/**
*
* ????yyyy??yyyy.MM??yyyy.MM.dd??????yyyy/MM/dd????????????01?<br>
*
* ??.1999 = 1999/01/01 ???1989.02=1989/02/01
*
* @param argDate
*
* ???????????????yyyy??yyyy.MM??yyyy.MM.dd
*
* @return ?????yyyy/MM/dd????
*
*/
public static String changeDate(String argDate) {
if (argDate == null || argDate.trim().equals("")) {
return "";
}
String result = "";
// ??????yyyy/MM/dd??????
if (argDate.length() == 10 && argDate.indexOf("/") > 0) {
return argDate;
}
String[] str = argDate.split("[.]"); // .????
int LEN = str.length;
for (int i = 0; i < LEN; i++) {
if (str[i].length() == 1) {
if (str[i].equals("0")) {
str[i] = "01";
} else {
str[i] = "0" + str[i];
}
}
}
if (LEN == 1) {
result = argDate + "/01/01";
}
if (LEN == 2) {
result = str[0] + "/" + str[1] + "/01";
}
if (LEN == 3) {
result = str[0] + "/" + str[1] + "/" + str[2];
}
return result;
}

/**
*
* ????yyyy??yyyy.MM??yyyy.MM.dd??????yyyy/MM/dd????????????01?<br>
*
* ??.1999 = 1999/01/01 ???1989.02=1989/02/01
*
* @param argDate
*
* ???????????????yyyy??yyyy.MM??yyyy.MM.dd
*
* @return ?????yyyy/MM/dd????
*
*/
public static String changeDateWithSplit(String argDate, String split) {
if (argDate == null || argDate.trim().equals("")) {
return "";
}
if (split == null || split.trim().equals("")) {
split = "-";
}
String result = "";
// ??????yyyy/MM/dd??????
if (argDate.length() == 10 && argDate.indexOf("/") > 0) {
return argDate;
}
// ??????yyyy-MM-dd??????
if (argDate.length() == 10 && argDate.indexOf("-") > 0) {
return argDate;
}
String[] str = argDate.split("[.]"); // .????
int LEN = str.length;
for (int i = 0; i < LEN; i++) {
if (str[i].length() == 1) {
if (str[i].equals("0")) {
str[i] = "01";
} else {
str[i] = "0" + str[i];
}
}
}
if (LEN == 1) {
result = argDate + split + "01" + split + "01";
}
if (LEN == 2) {
result = str[0] + split + str[1] + split + "01";
}
if (LEN == 3) {
result = str[0] + split + str[1] + split + str[2];
}
return result;
}

/**
*
* ????????????????
*
* @param argDate
*            ???yyyy-MM-dd??yyyy/MM/dd
*
* @return ????????
*
*/
public static int getNextMonthDays(String argDate) {
String[] temp = null;
if (argDate.indexOf("/") > 0) {
temp = argDate.split("/");
}
if (argDate.indexOf("-") > 0) {
temp = argDate.split("-");
}
Calendar cal = new GregorianCalendar(new Integer(temp[0]).intValue(),
new Integer(temp[1]).intValue() - 1, new Integer(temp[2])
.intValue());
int curMonth = cal.get(Calendar.MONTH);
cal.set(Calendar.MONTH, curMonth + 1);
int curyear = cal.get(Calendar.YEAR);// ????
curMonth = cal.get(Calendar.MONTH);// ????,0-11
int mArray[] = new int[] { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30,
31 };
// ??????? ?2???29??
if ((curyear % 400 == 0)
|| ((curyear % 100 != 0) && (curyear % 4 == 0))) {
mArray[1] = 29;
}
return mArray[curMonth];
}
}

