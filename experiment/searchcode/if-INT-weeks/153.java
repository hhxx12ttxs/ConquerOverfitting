/*
 * @(#)2013-4-3 V1.0
 * 
 * Copyright 2013 WELL-SOFT, Inc. All rights reserved.
 */
package com.wellsoft.pt.basicdata.dyview.support;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Description: 如何描述该类
 *  
 * @author wubin
 * @date 2013-4-3
 * @version 1.0
 * 
 * <pre>
 * 修改记录:
 * 修改后版本	修改人		修改日期			修改内容
 * 2013-4-3.1	wubin		2013-4-3		Create
 * </pre>
 *
 */
public class DateUtil implements Serializable {

	/** 如何描述serialVersionUID */
	private static final long serialVersionUID = 9164730971745973114L;

	private Calendar calendar = Calendar.getInstance();

	/**  
	 * 得到当前的时间，时间格式yyyy-MM-dd  
	 * @return  
	 */
	public String getCurrentDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(new Date());
	}

	/**  
	 * 得到当前的时间,自定义时间格式  
	 * y 年 M 月 d 日 H 时 m 分 s 秒  
	 * @param dateFormat 输出显示的时间格式  
	 * @return  
	 */
	public String getCurrentDate(String dateFormat) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(new Date());
	}

	/**  
	 * 日期格式化，默认日期格式yyyy-MM-dd  
	 * @param date  
	 * @return  
	 */
	public String getFormatDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(date);
	}

	/**  
	 * 日期格式化，自定义输出日期格式  
	 * @param date  
	 * @return  
	 */
	public String getFormatDate(Date date, String dateFormat) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(date);
	}

	/**  
	 * 返回当前日期的前一个时间日期，amount为正数 当前时间后的时间 为负数 当前时间前的时间  
	 * 默认日期格式yyyy-MM-dd  
	 * @param field 日历字段  
	 * y 年 M 月 d 日 H 时 m 分 s 秒  
	 * @param amount 数量  
	 * @return 一个日期  
	 */
	public String getPreDate(int amount) {
		calendar.setTime(new Date());
		calendar.add(calendar.DATE, amount);//把日期往后增加一天.整数往后推,负数往前移动
		return getFormatDate(calendar.getTime());
	}

	/**
	 * 
	 * 获取上个月的第一天
	 * 
	 * @return
	 */
	public String getLastMonthFirstDay() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(calendar.YEAR);
		int month = calendar.get(calendar.MONTH) + 1;
		calendar.set(calendar.DAY_OF_MONTH, 1);
		calendar.add(calendar.DAY_OF_MONTH, -1);
		int day = calendar.get(calendar.DAY_OF_MONTH);
		String months = "";
		String days = "";
		if (month > 1) {
			month--;
		} else {
			year--;
			month = 12;
		}
		if (!(String.valueOf(month).length() > 1)) {
			months = "0" + month;
		} else {
			months = String.valueOf(month);
		}
		if (!(String.valueOf(day).length() > 1)) {
			days = "0" + day;
		} else {
			days = String.valueOf(day);
		}
		String firstDay = "" + year + "-" + months + "-01";
		String[] lastMonth = new String[2];
		lastMonth[0] = firstDay;
		return firstDay;

	}

	/**
	 * 
	 * 获取上个月的最后一天
	 * 
	 * @param amount
	 * @return
	 */
	public String getLastMonthLastDay() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(calendar.YEAR);
		int month = calendar.get(calendar.MONTH) + 1;
		calendar.set(calendar.DAY_OF_MONTH, 1);
		calendar.add(calendar.DAY_OF_MONTH, -1);
		int day = calendar.get(calendar.DAY_OF_MONTH);
		String months = "";
		String days = "";
		if (month > 1) {
			month--;
		} else {
			year--;
			month = 12;
		}
		if (!(String.valueOf(month).length() > 1)) {
			months = "0" + month;
		} else {
			months = String.valueOf(month);
		}
		if (!(String.valueOf(day).length() > 1)) {
			days = "0" + day;
		} else {
			days = String.valueOf(day);
		}
		String lastDay = "" + year + "-" + months + "-" + days;
		String[] lastMonth = new String[2];
		lastMonth[1] = lastDay;
		return lastDay;

	}

	/**  
	 * 某一个日期的前一个日期  
	 * @param d,某一个日期  
	 * @param field 日历字段  
	 * y 年 M 月 d 日 H 时 m 分 s 秒  
	 * @param amount 数量  
	 * @return 一个日期  
	 */
	public String getPreDate(Date d, String field, int amount) {
		calendar.setTime(d);
		if (field != null && !field.equals("")) {
			if (field.equals("y")) {
				calendar.add(calendar.YEAR, amount);
			} else if (field.equals("M")) {
				calendar.add(calendar.MONTH, amount);
			} else if (field.equals("d")) {
				calendar.add(calendar.DAY_OF_MONTH, amount);
			} else if (field.equals("H")) {
				calendar.add(calendar.HOUR, amount);
			}
		} else {
			return null;
		}
		return getFormatDate(calendar.getTime());
	}

	/**  
	 * 某一个时间的前一个时间  
	 * @param date  
	 * @return  
	 * @throws ParseException   
	 */
	public String getPreDate(String date) throws ParseException {
		Date d = new SimpleDateFormat().parse(date);
		String preD = getPreDate(d, "d", 1);
		Date preDate = new SimpleDateFormat().parse(preD);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(preDate);
	}

	// 用来全局控制 上一周，本周，下一周的周数变化
	private static int weeks = 0;

	// 获得当前日期与本周一相差的天数
	private int getMondayPlus() {
		Calendar cd = Calendar.getInstance();
		// 获得今天是一周的第几天，星期日是第一天，星期二是第二天......
		int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == 1) {
			return -6;
		} else {
			return 2 - dayOfWeek;
		}
	}

	// 获得上周星期一的日期
	public String getPreviousMonday() {
		weeks--;
		int mondayPlus = this.getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 * weeks);
		Date monday = currentDate.getTime();
		return getFormatDate(monday);
	}

	// 获得上周星期一的日期
	public String getCurrentMonday() {
		weeks = 0;
		int mondayPlus = this.getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus);
		Date monday = currentDate.getTime();
		return getFormatDate(monday);
	}

	// 获得上周星期一的日期
	public String getNextMonday() {
		weeks++;
		int mondayPlus = this.getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 * weeks);
		Date monday = currentDate.getTime();
		return getFormatDate(monday);
	}

	// 获得相应周的周日的日期
	public String getSunday() {
		int mondayPlus = this.getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 * weeks + 6);
		Date monday = currentDate.getTime();
		return getFormatDate(monday);
	}

	/**
	 * 
	 * 获得前一天或后一天
	 * 
	 * @param type （pre前一天，next下一天）
	 * @return
	 */
	public Date getDateByType(String type) {
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		if (type != null && type.equals("pre")) {
			calendar.add(Calendar.DAY_OF_MONTH, -1);
		} else {
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		date = calendar.getTime();
		return date;
	}
}

