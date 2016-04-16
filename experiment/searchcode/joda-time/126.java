/*
 * @(#)2014-2-7 V1.0
 * 
 * Copyright 2012 WELL-SOFT, Inc. All rights reserved.
 */
package com.wellsoft.pt.basicdata.workhour.support;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.wellsoft.pt.basicdata.workhour.entity.WorkHour;
import com.wellsoft.pt.basicdata.workhour.enums.WorkUnit;
import com.wellsoft.pt.utils.date.DateUtils;

/**
 * Description: 如何描述该类
 *  
 * @author zhulh
 * @date 2014-2-7
 * @version 1.0
 * 
 * <pre>
 * 修改记录:
 * 修改后版本	修改人		修改日期			修改内容
 * 2014-2-7.1	zhulh		2014-2-7		Create
 * </pre>
 *
 */
public class WorkHourUtils {
	public static String[] DAY_OF_WEEKS = new String[] { "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT" };

	/**
	 * 获取指定工作日的信息
	 * 
	 * @param date
	 * @return
	 */
	public static WorkingHour getWorkingHour(Date date) {
		WorkingHour workingHour = new WorkingHour();
		// 判断是否为工作时间
		if (isWorkHour(date)) {
			workingHour.setIsWorkHour(true);
			workingHour.setIsWorkDay(true);
		} else {
			workingHour.setIsWorkHour(false);
			// 判断是否为工作日
			if (isWorkDay(date)) {
				workingHour.setIsWorkDay(true);
			} else {
				workingHour.setIsWorkDay(false);
				return workingHour;
			}
		}

		// 获取周一到周日的某一工作时间
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		String code = DAY_OF_WEEKS[calendar.get(Calendar.DAY_OF_WEEK) - 1];
		WorkHour workHour = WorkHourContext.getWorkDayByCode(code);
		if (workHour == null) {
			return workingHour;
		}

		// 获取上午时间和下午时间
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		// 上午时间
		String fromTime1 = workHour.getFromTime1();
		String toTime1 = workHour.getToTime1();
		String fixedFromTime1 = year + "-" + month + "-" + day + " " + fromTime1 + ":" + "00";
		String fixedToTime1 = year + "-" + month + "-" + day + " " + toTime1 + ":" + "00";
		// 下午时间
		String fromTime2 = workHour.getFromTime2();
		String toTime2 = workHour.getToTime2();
		String fixedFromTime2 = year + "-" + month + "-" + day + " " + fromTime2 + ":" + "00";
		String fixedToTime2 = year + "-" + month + "-" + day + " " + toTime2 + ":" + "00";
		try {
			// 上午时间
			workingHour.setFromTime1(DateUtils.parseDateTime(fixedFromTime1));
			workingHour.setToTime1(DateUtils.parseDateTime(fixedToTime1));

			// 下午时间
			workingHour.setFromTime2(DateUtils.parseDateTime(fixedFromTime2));
			workingHour.setToTime2(DateUtils.parseDateTime(fixedToTime2));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return workingHour;
	}

	/**
	 * 判断是否为工作日
	 * 
	 * @param date
	 * @return
	 */
	public static boolean isWorkDay(Date date) {
		// 判断是否在正常工作日内
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		String code = DAY_OF_WEEKS[calendar.get(Calendar.DAY_OF_WEEK) - 1];

		// 1、在补班日期中，直接返回true
		if (isInMakeup(date)) {
			return true;
		}

		// 2、不是正常工作日，直接返回false
		Boolean result = WorkHourContext.isWorkDayByCode(code);
		if (Boolean.FALSE.equals(result)) {
			return false;
		}

		// 3、判断是否在固定节假日内，如果是返回false
		if (isInFixedHolidays(date).equals(Boolean.TRUE)) {
			return false;
		}

		// 4、判断是否为特殊节假日，如果是返回false
		if (isInSpecialHolidays(date).equals(Boolean.TRUE)) {
			return false;
		}

		return result;
	}

	/**
	 * 判断是否为工作时间
	 * 
	 * @param date
	 * @return
	 */
	public static boolean isWorkHour(Date date) {
		Boolean result = Boolean.FALSE;
		// 如果不是工作日，直接返回
		if (Boolean.FALSE.equals(isWorkDay(date))) {
			return result;
		}
		// 获取周一到周日的某一工作时间
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		String code = DAY_OF_WEEKS[calendar.get(Calendar.DAY_OF_WEEK) - 1];
		WorkHour workHour = WorkHourContext.getWorkDayByCode(code);
		if (workHour == null) {
			return result;
		}

		// 判断是否为工作时间
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		// 上午时间
		String fromTime1 = workHour.getFromTime1();
		String toTime1 = workHour.getToTime1();
		String fixedFromTime1 = year + "-" + month + "-" + day + " " + fromTime1 + ":" + "00";
		String fixedToTime1 = year + "-" + month + "-" + day + " " + toTime1 + ":" + "00";
		// 下午时间
		String fromTime2 = workHour.getFromTime2();
		String toTime2 = workHour.getToTime2();
		String fixedFromTime2 = year + "-" + month + "-" + day + " " + fromTime2 + ":" + "00";
		String fixedToTime2 = year + "-" + month + "-" + day + " " + toTime2 + ":" + "00";
		try {
			// 上午时间比较
			Date from = DateUtils.parseDateTime(fixedFromTime1);
			Date to = DateUtils.parseDateTime(fixedToTime1);
			if (date.compareTo(from) >= 0 && date.compareTo(to) <= 0) {
				return true;
			}

			// 下午时间比较
			from = DateUtils.parseDateTime(fixedFromTime2);
			to = DateUtils.parseDateTime(fixedToTime2);
			if (date.compareTo(from) >= 0 && date.compareTo(to) <= 0) {
				return true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 返回有效工作时间区段(单位秒)以及经历几个工作日
	 * 
	 * @param fromTime
	 * @param toTime
	 * @return
	 */
	public static WorkPeriod getWorkPeriod(Date fromTime, Date toTime) {
		WorkPeriod workPeriod = new WorkPeriod();
		workPeriod.setFromTime(fromTime);
		workPeriod.setToTime(toTime);
		// 1 如果起始时间大于结束时间，直接返回
		if (fromTime.compareTo(toTime) > 0) {
			return workPeriod;
		}

		int days = 0;
		long milliseconds = 0l;

		// 2 如果开始时间与结束时间同一天
		if (DateUtils.isSameDate(fromTime, toTime)) {
			// 获取工作时间信息
			WorkingHour workingHour = getWorkingHour(fromTime);
			// 如果是工作日
			if (workingHour.getIsWorkDay()) {
				// 工作日加一
				days++;
				// 根据同一天的开始时间、结束时间及工作时间信息，获取有效的工作时间
				milliseconds = calculateEffectiveTime(fromTime, toTime, workingHour);
				workPeriod.setWorkDay(calculateWorkDay(milliseconds, workingHour));
			}
			workPeriod.setDays(days);
			workPeriod.setWorkHour(DateUtils.millisecondToHour(milliseconds));
			workPeriod.setWorkMinute(DateUtils.millisecondToMinute(milliseconds));
		} else {// 不同一天的情况
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(fromTime);

			// 1、获取开始工作时间信息
			WorkingHour workingHour = getWorkingHour(calendar.getTime());
			if (workingHour.getIsWorkDay()) {
				// 工作日加一
				days++;
				// 计算工作小时数
				Calendar toCalendar = DateUtils.getMaxTimeCalendar(calendar);
				Date tmpToTime = toCalendar.getTime();
				long effective = calculateEffectiveTime(fromTime, tmpToTime, workingHour);
				milliseconds += effective;
				double workDay = workPeriod.getWorkDay() + calculateWorkDay(effective, workingHour);
				workPeriod.setWorkDay(workDay);
			}

			// 向前推进一天
			calendar.add(Calendar.DAY_OF_MONTH, 1);

			// 2、获取中间工作时间信息
			while (!DateUtils.isSameDate(toTime, calendar.getTime())) {
				// 获取工作时间信息
				workingHour = getWorkingHour(calendar.getTime());
				if (workingHour.getIsWorkDay()) {
					// 工作日加一
					days++;
					// 计算工作小时数
					long effective = calculateEffectiveTime(workingHour);
					milliseconds += effective;
					double workDay = workPeriod.getWorkDay() + calculateWorkDay(effective, workingHour);
					workPeriod.setWorkDay(workDay);
				}
				// 向前推进一天
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			}

			// 3、获取最后工作时间信息
			workingHour = getWorkingHour(calendar.getTime());
			if (workingHour.getIsWorkDay()) {
				// 工作日加一
				days++;
				// 计算工作小时数
				Calendar fromCalendar = DateUtils.getMinTimeCalendar(calendar);
				Date tmpFromTime = fromCalendar.getTime();
				long effective = calculateEffectiveTime(tmpFromTime, toTime, workingHour);
				milliseconds += effective;
				double workDay = workPeriod.getWorkDay() + calculateWorkDay(effective, workingHour);
				workPeriod.setWorkDay(workDay);
			}

			workPeriod.setDays(days);
			workPeriod.setWorkHour(DateUtils.millisecondToHour(milliseconds));
			workPeriod.setWorkMinute(DateUtils.millisecondToMinute(milliseconds));
		}

		return workPeriod;
	}

	/**
	 * 返回有效的工作时间点
	 * 
	 * @param date
	 * @param amount
	 * @param workingUnit
	 * @return
	 */
	public static Date getWorkDate(Date date, Double amount, WorkUnit workingUnit) {
		WorkUnit workUnit = workingUnit;
		// 如果增加或减少的数量为0则按当前工作日处理
		if (Double.valueOf(0).equals(amount)) {
			workUnit = WorkUnit.WorkingDay;
		}

		Date workDate = date;
		switch (workUnit) {
		// 以工作日为单位，计算有效时间
		case WorkingDay:
			Calendar calendar = DateUtils.getCalendar(workDate);
			Integer days = amount.intValue();
			// 获取最后的工作日
			for (int i = 0; i < Math.abs(days); i++) {
				// 工作日加一或减一
				calendar.add(Calendar.DAY_OF_MONTH, days / Math.abs(days));
				// 如果不是工作日，则继续推进
				while (!isWorkDay(calendar.getTime())) {
					calendar.add(Calendar.DAY_OF_MONTH, days / Math.abs(days));
				}
			}
			WorkingHour workingHour = getWorkingHour(calendar.getTime());
			// 以工作日为单位，计算有效的工作时间
			workDate = calculateWorkDateByDay(calendar.getTime(), workingHour, days);
			// 计算小数非整数工作日
			Double decimal = amount - days;
			if (decimal != 0) {
				// 工作日转化为工作小时
				Double hours = workDayToHour(workDate, decimal);
				return getWorkDate(workDate, hours, WorkUnit.WorkingHour);
			}
			break;
		// 以小时为单位，计算有效时间
		case WorkingHour:
			if (amount > 0) {
				// 时间为正数，计算有效时间
				workDate = getWorkDateByHour1(amount, workDate);
			} else if (amount < 0) {
				// 时间为负数，计算有效时间
				workDate = getWorkDateByHour2(amount, workDate);
			} else {
			}
			break;
		// 以分钟为单位，计算有效时间
		case WorkingMinute:
			if (amount > 0) {
				// 时间为正数，计算有效时间
				workDate = getWorkDateByHour1(amount / 60d, workDate);
			} else if (amount < 0) {
				// 时间为负数，计算有效时间
				workDate = getWorkDateByHour2(amount / 60d, workDate);
			} else {
			}
			break;
		default:
			break;
		}
		return workDate;
	}

	/**
	 * 如何描述该方法
	 * 
	 * @param a
	 * @return
	 */
	private static Double workDayToHour(Date workDate, Double workDay) {
		WorkingHour workingHour = getWorkingHour(workDate);
		long ectiveTime = calculateEffectiveTime(workingHour);
		return DateUtils.millisecondToHour(ectiveTime) * workDay;
	}

	/**
	 * 判断是否在补班日期内，如果是返回true，否则返回false
	 * 
	 * @param date
	 * @return
	 */
	private static boolean isInMakeup(Date date) {
		Boolean result = Boolean.FALSE;
		// 查询补班日期
		List<WorkHour> makeups = WorkHourContext.getMakeups();
		for (WorkHour workHour : makeups) {
			String fromDate = workHour.getFromDate();
			String toDate = workHour.getToDate();
			String fixedFromDate = fromDate + " " + WorkHour.TIME_MIN;
			String fixedToDate = toDate + " " + WorkHour.TIME_MAX;
			try {
				Date from = DateUtils.parseDateTime(fixedFromDate);
				Date to = DateUtils.parseDateTime(fixedToDate);
				if (date.compareTo(from) >= 0 && date.compareTo(to) <= 0) {
					return true;
				}
			} catch (ParseException e) {
				e.printStackTrace();
				result = false;
			}
		}
		return result;
	}

	/**
	 * 判断指定日期是否在固定节假日内，如果是返回true，否则返回false
	 * 
	 * @param date
	 * @return
	 */
	private static Boolean isInFixedHolidays(Date date) {
		Boolean result = Boolean.FALSE;
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		// 查询固定节假日
		List<WorkHour> fixeds = WorkHourContext.getFixedHolidays();
		for (WorkHour workHour : fixeds) {
			String fromDate = workHour.getFromDate();
			String toDate = workHour.getToDate();
			String fixedFromDate = year + "-" + fromDate + " " + WorkHour.TIME_MIN;
			String fixedToDate = year + "-" + toDate + " " + WorkHour.TIME_MAX;
			try {
				Date from = DateUtils.parseDateTime(fixedFromDate);
				Date to = DateUtils.parseDateTime(fixedToDate);
				if (date.compareTo(from) >= 0 && date.compareTo(to) <= 0) {
					return true;
				}
			} catch (ParseException e) {
				e.printStackTrace();
				result = false;
			}
		}
		return result;
	}

	/**
	 * 判断指定日期是否在特殊节假日内，如果是返回true，否则返回false
	 * 
	 * @param date
	 * @return
	 */
	private static Boolean isInSpecialHolidays(Date date) {
		Boolean result = Boolean.FALSE;
		// 查询特殊节假日
		List<WorkHour> specials = WorkHourContext.getSpecialHolidays();
		for (WorkHour workHour : specials) {
			String fromDate = workHour.getFromDate();
			String toDate = workHour.getToDate();
			String fixedFromDate = fromDate + " " + WorkHour.TIME_MIN;
			String fixedToDate = toDate + " " + WorkHour.TIME_MAX;
			try {
				Date from = DateUtils.parseDateTime(fixedFromDate);
				Date to = DateUtils.parseDateTime(fixedToDate);
				if (date.compareTo(from) >= 0 && date.compareTo(to) <= 0) {
					return true;
				}
			} catch (ParseException e) {
				e.printStackTrace();
				result = false;
			}
		}
		return result;
	}

	/**
	 * 以小时为单位，计算有效时间，其中时间为正数
	 * 
	 * @param amount
	 * @param workDate
	 * @return
	 */
	private static Date getWorkDateByHour1(Double amount, Date workDate) {
		Date date = workDate;
		Calendar calendar = DateUtils.getCalendar(workDate);
		Calendar toCalendar = DateUtils.getMaxTimeCalendar(calendar);
		Double hours = amount;
		// 计算开始，是否在当前日期内
		WorkPeriod workPeriod = getWorkPeriod(calendar.getTime(), toCalendar.getTime());
		if (workPeriod.getWorkHour() > hours) {
			// 以小时为单位，计算有效的工作时间
			date = calculateWorkDateByHour1(calendar.getTime(), hours);
		} else {
			hours = hours - workPeriod.getWorkHour();
			// 后部计算，获取后一日期
			Calendar fromCalendar = DateUtils.getMinTimeCalendar(calendar);
			toCalendar = DateUtils.getMaxTimeCalendar(calendar);
			// 下一天
			fromCalendar.add(Calendar.DAY_OF_MONTH, 1);
			toCalendar.add(Calendar.DAY_OF_MONTH, 1);
			workPeriod = getWorkPeriod(fromCalendar.getTime(), toCalendar.getTime());
			while (hours - workPeriod.getWorkHour() >= 0) {
				hours = hours - workPeriod.getWorkHour();

				// 下一天
				fromCalendar.add(Calendar.DAY_OF_MONTH, 1);
				toCalendar.add(Calendar.DAY_OF_MONTH, 1);
				workPeriod = getWorkPeriod(fromCalendar.getTime(), toCalendar.getTime());
			}
			// 以小时为单位，计算有效的工作时间
			date = calculateNewWorkDateByHour1(fromCalendar.getTime(), hours);
		}
		return date;
	}

	/**
	 * 以小时为单位，计算有效时间，其中时间为负数
	 * 
	 * @param amount
	 * @param workDate
	 * @return
	 */
	private static Date getWorkDateByHour2(Double amount, Date workDate) {
		Date date = workDate;
		Calendar calendar = DateUtils.getCalendar(workDate);
		Calendar fromCalendar = DateUtils.getMinTimeCalendar(calendar);
		Double hours = amount;
		// 计算开始，是否在当前日期内
		WorkPeriod workPeriod = getWorkPeriod(fromCalendar.getTime(), calendar.getTime());
		if (workPeriod.getWorkHour() + hours >= 0) {
			date = calculateWorkDateByHour2(calendar.getTime(), hours);
		} else {
			hours = hours + workPeriod.getWorkHour();
			// 后部计算，获取后一日期
			fromCalendar = DateUtils.getMinTimeCalendar(calendar);
			Calendar toCalendar = DateUtils.getMaxTimeCalendar(calendar);
			// 下一天
			fromCalendar.add(Calendar.DAY_OF_MONTH, -1);
			toCalendar.add(Calendar.DAY_OF_MONTH, -1);
			workPeriod = getWorkPeriod(fromCalendar.getTime(), toCalendar.getTime());
			while (hours + workPeriod.getWorkHour() < 0) {
				hours = hours + workPeriod.getWorkHour();

				// 下一天
				fromCalendar.add(Calendar.DAY_OF_MONTH, -1);
				toCalendar.add(Calendar.DAY_OF_MONTH, -1);
				workPeriod = getWorkPeriod(fromCalendar.getTime(), toCalendar.getTime());
			}
			// 以小时为单位，计算有效的工作时间
			date = calculateNewWorkDateByHour2(fromCalendar.getTime(), hours);
		}
		return date;
	}

	/**
	 * 计算工作时间的有效工作时间
	 * 
	 * @param workingHour
	 * @return
	 */
	private static long calculateEffectiveTime(WorkingHour workingHour) {
		// 上午上班时间段
		Date fromTime1 = workingHour.getFromTime1();
		Date toTime1 = workingHour.getToTime1();
		// 下午下班时间段
		Date fromTime2 = workingHour.getFromTime2();
		Date toTime2 = workingHour.getToTime2();

		long totalTime1 = toTime1.getTime() - fromTime1.getTime();
		long totalTime2 = toTime2.getTime() - fromTime2.getTime();

		return totalTime1 + totalTime2;
	}

	/**
	 * 根据同一天的开始时间、结束时间及工作时间信息，获取有效的工作时间
	 * 
	 * @param fromTime
	 * @param toTime
	 * @param workingHour
	 * @return effective milliseconds
	 */
	private static long calculateEffectiveTime(Date fromTime, Date toTime, WorkingHour workingHour) {
		// 上午上班时间段
		Date fromTime1 = workingHour.getFromTime1();
		Date toTime1 = workingHour.getToTime1();
		// 计算上午的有效时间
		long totalTime1 = DateUtils.getUnionTime(fromTime, toTime, fromTime1, toTime1);

		// 下午下班时间段
		Date fromTime2 = workingHour.getFromTime2();
		Date toTime2 = workingHour.getToTime2();
		// 计算下午的有效时间
		long totalTime2 = DateUtils.getUnionTime(fromTime, toTime, fromTime2, toTime2);

		// 计算工作小时数
		return totalTime1 + totalTime2;
	}

	/**
	 * 以小时为单位，计算有效的工作时间，其中时间为正数
	 * 
	 * @param workDate
	 * @param hours
	 * @return
	 */
	private static Date calculateWorkDateByHour1(Date workDate, Double hours) {
		WorkingHour workingHour = getWorkingHour(workDate);
		Date date = workDate;
		// 上午上班时间
		Date fromTime1 = workingHour.getFromTime1();
		// 上午下班时间
		Date toTime1 = workingHour.getToTime1();
		// 下午上班时间
		Date fromTime2 = workingHour.getFromTime2();
		// 下午下班时间
		Date toTime2 = workingHour.getToTime2();
		// 在上午上班时间之前
		if (workDate.before(fromTime1)) {
			return calculateNewWorkDateByHour1(workDate, hours);
		} else if (workDate.before(toTime1)) {// 在上午下班时间之前
			// 计算时间差，以小时为单位
			Double tmp = DateUtils.calculateAsHour(toTime1, workDate);
			if (tmp > hours) {
				Calendar calendar = DateUtils.getCalendar(workDate);
				calendar.add(Calendar.SECOND, DateUtils.hourToSecond(hours));
				date = calendar.getTime();
			} else {
				tmp = hours - tmp;
				Calendar calendar = DateUtils.getCalendar(fromTime2);
				calendar.add(Calendar.SECOND, DateUtils.hourToSecond(tmp));
				date = calendar.getTime();
			}
		} else if (workDate.before(fromTime2)) {// 在下午上班时间之前
			Calendar calendar = DateUtils.getCalendar(fromTime2);
			calendar.add(Calendar.SECOND, DateUtils.hourToSecond(hours));
			date = calendar.getTime();
		} else if (workDate.before(toTime2)) {// 下午下班时间之前
			Calendar calendar = DateUtils.getCalendar(workDate);
			calendar.add(Calendar.SECOND, DateUtils.hourToSecond(hours));
			date = calendar.getTime();
		} else {// 下午下班时间之后，设置为上午上班时间
			throw new RuntimeException("Work Date Not Found");
		}

		return date;
	}

	/**
	 * 以小时为单位，在新的一天中计算有效的工作时间，其中时间为正数
	 * 
	 * @param workDate
	 * @param hours
	 * @return
	 */
	private static Date calculateNewWorkDateByHour1(Date workDate, Double hours) {
		WorkingHour workingHour = getWorkingHour(workDate);
		// 上午上班时间
		Date fromTime1 = workingHour.getFromTime1();
		// 下午下班时间
		Date toTime1 = workingHour.getToTime1();
		// 下午上班时间
		Date fromTime2 = workingHour.getFromTime2();

		Calendar calendar = Calendar.getInstance();
		// 计算上午上班时间区段
		Double amHours = DateUtils.calculateAsHour(toTime1, fromTime1);
		if (amHours > hours) {
			calendar.setTime(fromTime1);
			calendar.add(Calendar.SECOND, DateUtils.hourToSecond(hours));
		} else {
			calendar.setTime(fromTime2);
			calendar.add(Calendar.SECOND, DateUtils.hourToSecond(hours - amHours));
		}

		return calendar.getTime();
	}

	/**
	 * 以小时为单位，计算有效的工作时间，其中时间为负数
	 * 
	 * @param workDate
	 * @param hours
	 * @return
	 */
	private static Date calculateWorkDateByHour2(Date workDate, Double hours) {
		WorkingHour workingHour = getWorkingHour(workDate);
		Date date = workDate;
		// 上午上班时间
		Date fromTime1 = workingHour.getFromTime1();
		// 上午下班时间
		Date toTime1 = workingHour.getToTime1();
		// 下午上班时间
		Date fromTime2 = workingHour.getFromTime2();
		// 下午下班时间
		Date toTime2 = workingHour.getToTime2();
		// 在上午上班时间之前
		if (workDate.before(fromTime1)) {
			throw new RuntimeException("Work Date Not Found");
		} else if (workDate.before(toTime1)) {// 在上午下班时间之前
			Calendar calendar = DateUtils.getCalendar(workDate);
			calendar.add(Calendar.SECOND, DateUtils.hourToSecond(hours));
			date = calendar.getTime();
		} else if (workDate.before(fromTime2)) {// 在下午上班时间之前
			Calendar calendar = DateUtils.getCalendar(toTime1);
			calendar.add(Calendar.SECOND, DateUtils.hourToSecond(hours));
			date = calendar.getTime();
		} else if (workDate.before(toTime2)) {// 下午下班时间之前
			// 计算时间差，以小时为单位
			Double tmp = DateUtils.calculateAsHour(workDate, fromTime2);
			if (tmp > Math.abs(hours)) {
				Calendar calendar = DateUtils.getCalendar(workDate);
				calendar.add(Calendar.SECOND, DateUtils.hourToSecond(hours));
				date = calendar.getTime();
			} else {
				tmp = hours + tmp;
				Calendar calendar = DateUtils.getCalendar(toTime1);
				calendar.add(Calendar.SECOND, DateUtils.hourToSecond(tmp));
				date = calendar.getTime();
			}
		} else {// 下午下班时间之后
			date = calculateNewWorkDateByHour2(workDate, hours);
		}

		return date;
	}

	/**
	 * 以小时为单位，在新的一天中计算有效的工作时间，其中时间为负数
	 * 
	 * @param workDate
	 * @param hours
	 * @return
	 */
	private static Date calculateNewWorkDateByHour2(Date workDate, Double hours) {
		WorkingHour workingHour = getWorkingHour(workDate);
		// 上午下班时间
		Date toTime1 = workingHour.getToTime1();
		// 下午上班时间
		Date fromTime2 = workingHour.getFromTime2();
		// 下午下班时间
		Date toTime2 = workingHour.getToTime2();

		Calendar calendar = Calendar.getInstance();
		// 计算下午上班时间区段
		Double pmHours = DateUtils.calculateAsHour(toTime2, fromTime2);
		if ((pmHours + hours) >= 0) {
			calendar.setTime(toTime2);
			calendar.add(Calendar.SECOND, DateUtils.hourToSecond(hours));
		} else {
			calendar.setTime(toTime1);
			calendar.add(Calendar.SECOND, DateUtils.hourToSecond(pmHours + hours));
		}

		return calendar.getTime();
	}

	/**
	 * 以秒为单位，计算有效的工作时间
	 * 
	 * @param milliseconds
	 * @param workingHour
	 * @return
	 */
	private static double calculateWorkDay(long milliseconds, WorkingHour workingHour) {
		long totalTime1 = workingHour.getToTime1().getTime() - workingHour.getFromTime1().getTime();
		long totalTime2 = workingHour.getToTime2().getTime() - workingHour.getFromTime2().getTime();
		return milliseconds / ((totalTime1 + totalTime2) * 1d);
	}

	/**
	 * 以工作日为单位，计算有效的工作时间
	 * 
	 * @param workDate
	 * @param workingHour
	 * @param days
	 * @return
	 */
	private static Date calculateWorkDateByDay(Date workDate, WorkingHour workingHour, Integer days) {
		Date date = workDate;
		// 上午上班时间
		Date fromTime1 = workingHour.getFromTime1();
		// 上午下班时间
		Date toTime1 = workingHour.getToTime1();
		// 下午上班时间
		Date fromTime2 = workingHour.getFromTime2();
		// 下午下班时间
		Date toTime2 = workingHour.getToTime2();

		// 在上午上班时间之前
		if (workDate.before(fromTime1)) {
			date = fromTime1;
		} else if (workDate.before(toTime1)) {// 在上午下班时间之前
			date = workDate;
		} else if (workDate.before(fromTime2)) {// 在下午上班时间之前
			date = fromTime2;
		} else if (workDate.before(toTime2)) {// 下午下班时间之前
			date = workDate;
		} else {// 下午下班时间之后，设置为上午上班时间
			if (days > 0) {
				date = toTime2;
			} else {
				date = fromTime1;
			}
		}

		return date;
	}

}

