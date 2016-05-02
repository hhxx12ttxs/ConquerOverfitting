package com.marketdata.marvin.midtier.framework.scheduler.model;

import java.text.ParseException;
import java.util.Date;

import org.joda.time.LocalDate;
import org.joda.time.Weeks;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;

import com.marketdata.marvin.midtier.framework.exception.domain.ErrorCode;
import com.marketdata.marvin.midtier.framework.exception.domain.MarvinRuntimeException;

public class WeeklyTrigger extends CronTrigger {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -8633919771283423455L;

	private int repeatIntervalInWeeks;

	/**
	 * @param name
	 * @param group
	 * @param startTime
	 * @param endTime
	 * @param cronExpression
	 * @param repeatIntervalInWeeks
	 *            - integer indicating the repeat interval in weeks. Will throw a
	 *            {@link com.marketdata.marvin.midtier.framework.exception.domain.MarvinRuntimeException} if integer is less than
	 *            0. e.g. '3' - allow execution on every third week.
	 * @throws ParseException
	 */
	public WeeklyTrigger(final String name, final String group, final Date startTime, final Date endTime,
			final CronExpression cronExpression, final int repeatIntervalInWeeks) throws ParseException {
		super(name, group);
		validateRepeatIntervalInWeeks(repeatIntervalInWeeks);
		this.repeatIntervalInWeeks = repeatIntervalInWeeks;
		setCronExpression(cronExpression);
		setStartTime(startTime);
		setEndTime(endTime);
	}

	/**
	 * Determines whether the given fireTimeDate, falls on an 'active' or an 'inactive' week.
	 * @param fireTimeDate
	 * @return
	 */
	public boolean isActiveWeek(final Date fireTimeDate) {
		if (fireTimeDate == null) {
			return false;
		}
		final LocalDate startTime = new LocalDate(getStartTime());
		final LocalDate fireTime = new LocalDate(fireTimeDate);
		final int weeks = Weeks.weeksBetween(startTime, fireTime).getWeeks();
		return weeks % repeatIntervalInWeeks == 0;
	}

	public void setRepeatIntervalInWeeks(final int repeatIntervalInWeeks) {
		validateRepeatIntervalInWeeks(repeatIntervalInWeeks);
		this.repeatIntervalInWeeks = repeatIntervalInWeeks;
	}

	public int getRepeatIntervalInWeeks() {
		return repeatIntervalInWeeks;
	}

	/**
	 * Validates the repeatIntervalInWeeks is a positive number greater than 0.
	 * @param repeatInterval
	 */
	private void validateRepeatIntervalInWeeks(final int repeatInterval) {
		if (repeatInterval < 1) {
			throw new MarvinRuntimeException(ErrorCode.GENERAL_FAILURE,
					"repeatIntervalInWeeks must be > 0 - repeatIntervalInWeeks=" + repeatInterval);
		}
	}

	/*
	 * Overridden methods from CronTrigger - only needed to override methods that performed calculations, quite often this meant
	 * only modifying a couple of underlying methods, that other methods referenced.
	 */

	@Override
	protected Date getTimeAfter(final Date afterTime) {
		// Referenced from:
		// computeFirstFireTime() -> getFireTimeAfter()
		Date timeAfter = super.getTimeAfter(afterTime);
		final Date finalFireTime = getFinalFireTime(); // N.B. Always returns null!
		// while timeAfter isn't null, and is less than the final time, and is not on an active week...
		while (timeAfter != null && (finalFireTime == null || timeAfter.before(finalFireTime)) && !isActiveWeek(timeAfter)) {
			// get next timeAfter...
			timeAfter = super.getTimeAfter(timeAfter);
		}
		return timeAfter;
	}

	/**
	 * As long as the underlying {@link org.quartz.CronExpression} class returns null for the "getTimeBefore(Date endTime)"
	 * method, this method will always return null.
	 */
	@Override
	protected Date getTimeBefore(final Date endTime) {
		// Referenced from:
		// getFinalFireTime()
		Date timeBefore = super.getTimeBefore(endTime); // N.B. Always returns null!
		final Date startTime = getStartTime();
		// while timeBefore isn't null, and is less after the start time, and is not on an active week...
		while (timeBefore != null && startTime != null && timeBefore.after(startTime) && !isActiveWeek(timeBefore)) {
			// get next timeAfter...
			timeBefore = super.getTimeBefore(timeBefore);
		}
		return timeBefore;
	}
}

