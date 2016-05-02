/*
 * Copyright (c) 2006, Atlassian Software Systems Pty Ltd
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of "Atlassian" nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.atlassian.confluence.extra.calendar.display;

import com.atlassian.confluence.core.TimeZone;
import com.atlassian.confluence.extra.calendar.CalendarUtils;
import com.atlassian.confluence.extra.calendar.model.ICalendar;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import java.util.List;

/**
 * This object represents a 7 day timespan.
 */
public class Week {
    private ICalendar calendar;

    private LocalDate weekStart;

    private LocalDate weekEnd;

    private List days;

    private int firstDay;

    private int weekOfYear;

    private int maxWeekOfYear;

    private int year;

    private TimeZone timeZone;

    /**
     * Constructs a new object representing a week of time.
     * 
     * @param calendar
     *            The calendar being viewed.
     * @param date
     *            The date the week contains.
     * @param firstDay
     *            The first day of the week (Sunday, Monday, etc)
     * @param timeZone
     *            The timezone to calculate using.
     */
    public Week( ICalendar calendar, LocalDate date, int firstDay, TimeZone timeZone ) {
        this.calendar = calendar;
        this.timeZone = timeZone;

        if ( date == null )
            date = new LocalDate( timeZone );

        this.firstDay = firstDay;

        weekStart = CalendarUtils.getFirstDayOfWeek( date, firstDay );
        weekEnd = CalendarUtils.getLastDayOfWeek( date, firstDay );

        weekOfYear = CalendarUtils.getWeekOfYear( date, firstDay );
        maxWeekOfYear = CalendarUtils.getMaxWeekOfYear( date, firstDay );

        int daysPerWeek = weekStart.dayOfWeek().getMaximumValue();
        if ( weekStart.getYear() != weekEnd.getYear() && weekEnd.getDayOfMonth() > daysPerWeek / 2 ) {
            year = weekEnd.getYear();
        } else {
            year = weekStart.getYear();
        }
    }

    /**
     * Returns the date at the start of the week.
     * 
     * @return the date at the start of the week.
     */
    public LocalDate getWeekStart() {
        return weekStart;
    }

    /**
     * Gets the list of days in this week.
     * 
     * @return the List of Day objects.
     */
    public List getDays() {
        if ( days == null ) {
            days = new java.util.ArrayList( 7 );
            LocalDate dayStart = weekStart;
            for ( int i = 0; i < 7; i++ ) {
                days.add( new Day( calendar, dayStart, firstDay, timeZone ) );
                dayStart = dayStart.plus( Period.days( 1 ) );
            }
        }
        return days;
    }

    public LocalDate getWeekEnd() {
        return weekEnd;
    }

    public int getWeekOfYear() {
        return weekOfYear;
    }

    public int getMinWeekOfYear() {
        return 1;
    }

    public int getMaxWeekOfYear() {
        return maxWeekOfYear;
    }

    public int getYear() {
        return year;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }
}
