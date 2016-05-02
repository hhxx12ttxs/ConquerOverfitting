package com.marketdata.marvin.midtier.domain.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import net.objectlab.kit.datecalc.common.DefaultHolidayCalendar;
import net.objectlab.kit.datecalc.common.HolidayHandlerType;
import net.objectlab.kit.datecalc.common.WorkingWeek;
import net.objectlab.kit.datecalc.joda.JodaWorkingWeek;
import net.objectlab.kit.datecalc.joda.LocalDateCalculator;
import net.objectlab.kit.datecalc.joda.LocalDateKitCalculatorsFactory;

import org.joda.time.LocalDate;

import com.marketdata.marvin.midtier.framework.date.DateTimeUtil;

public class CalendarDaysHelper {

	protected BusinessCalendar target;

	public CalendarDaysHelper(final BusinessCalendar target) {
		this.target = target;
	}

	public void setTarget(final BusinessCalendar target) {
		this.target = target;
	}

	public List<CalendarEvent> getEvents(final Date date) {

		if (target.getEvents() == null) {
			return null;
		}

		final ArrayList<CalendarEvent> events = new ArrayList<CalendarEvent>();

		final Date d = DateTimeUtil.zeroTimeComponents(date);

		for (final CalendarEvent event : target.getEvents()) {

			if (d.equals(event.getDate())) {
				events.add(event);
			}
		}

		return events;
	}

	public CalendarEvent findNonBusinessEvent(final Date date) {
		return CalendarEvent.findNonBusinessEvent(this.getEvents(date));
	}

	public WorkingWeek createWorkingWeek() {
		return createWorkingWeek(this);
	}

	public static WorkingWeek createWorkingWeek(final CalendarDaysHelper daysHelper) {

		WorkingWeek ww = new WorkingWeek();

		ww = ww.withWorkingDayFromCalendar(daysHelper.isDayMon(), Calendar.MONDAY);
		ww = ww.withWorkingDayFromCalendar(daysHelper.isDayTue(), Calendar.TUESDAY);
		ww = ww.withWorkingDayFromCalendar(daysHelper.isDayWed(), Calendar.WEDNESDAY);
		ww = ww.withWorkingDayFromCalendar(daysHelper.isDayThu(), Calendar.THURSDAY);
		ww = ww.withWorkingDayFromCalendar(daysHelper.isDayFri(), Calendar.FRIDAY);
		ww = ww.withWorkingDayFromCalendar(daysHelper.isDaySat(), Calendar.SATURDAY);
		ww = ww.withWorkingDayFromCalendar(daysHelper.isDaySun(), Calendar.SUNDAY);

		return ww;
	}

	public int getBusinessDaysBetween(final LocalDate startDate, final LocalDate endDate) {
		final LocalDateCalculator dateCalculator = getLocalDateCalculator();
		dateCalculator.setStartDate(startDate);
		int businessDays = 0;
		while (dateCalculator.getCurrentBusinessDate().isBefore(endDate)) {
			businessDays++;
			dateCalculator.moveByBusinessDays(1);
		}
		return businessDays;
	}

	public LocalDateCalculator getLocalDateCalculator() {

		// get 'events'
		final HashSet<LocalDate> nonBizDays = getNonBusinessDatesSet();

		final DefaultHolidayCalendar<LocalDate> nonBizCal = new DefaultHolidayCalendar<LocalDate>(nonBizDays);

		LocalDateKitCalculatorsFactory.getDefaultInstance().registerHolidays(
				target.getLabel() != null ? target.getLabel() : "default", nonBizCal);

		// setup dateCalculator
		final LocalDateCalculator dateCalc = LocalDateKitCalculatorsFactory.getDefaultInstance().getDateCalculator(
				target.getLabel() != null ? target.getLabel() : "default", HolidayHandlerType.FORWARD);

		dateCalc.setWorkingWeek(new JodaWorkingWeek(createWorkingWeek()));
		return dateCalc;
	}

	private HashSet<LocalDate> getNonBusinessDatesSet() {
		final HashSet<LocalDate> nonBizLocalDatesSet = new HashSet<LocalDate>();
		for (final CalendarEvent ce : target.events) {
			nonBizLocalDatesSet.add(new LocalDate(ce.getDate()));
		}
		return nonBizLocalDatesSet;
	}

	public boolean isTradingDay(final Calendar calendar) {
		return target.usualBusinessDays[calendar.get(Calendar.DAY_OF_WEEK)];
	}

	public boolean isDayMon() {
		return target.usualBusinessDays[java.util.Calendar.MONDAY];
	}

	public boolean isDayTue() {
		return target.usualBusinessDays[java.util.Calendar.TUESDAY];
	}

	public boolean isDayWed() {
		return target.usualBusinessDays[java.util.Calendar.WEDNESDAY];
	}

	public boolean isDayThu() {
		return target.usualBusinessDays[java.util.Calendar.THURSDAY];
	}

	public boolean isDayFri() {
		return target.usualBusinessDays[java.util.Calendar.FRIDAY];
	}

	public boolean isDaySat() {
		return target.usualBusinessDays[java.util.Calendar.SATURDAY];
	}

	public boolean isDaySun() {
		return target.usualBusinessDays[java.util.Calendar.SUNDAY];
	}

	public void setDayMon(final boolean value) {
		target.usualBusinessDays[java.util.Calendar.MONDAY] = value;
	}

	public void setDayTue(final boolean value) {
		target.usualBusinessDays[java.util.Calendar.TUESDAY] = value;
	}

	public void setDayWed(final boolean value) {
		target.usualBusinessDays[java.util.Calendar.WEDNESDAY] = value;
	}

	public void setDayThu(final boolean value) {
		target.usualBusinessDays[java.util.Calendar.THURSDAY] = value;
	}

	public void setDayFri(final boolean value) {
		target.usualBusinessDays[java.util.Calendar.FRIDAY] = value;
	}

	public void setDaySat(final boolean value) {
		target.usualBusinessDays[java.util.Calendar.SATURDAY] = value;
	}

	public void setDaySun(final boolean value) {
		target.usualBusinessDays[java.util.Calendar.SUNDAY] = value;
	}
}

