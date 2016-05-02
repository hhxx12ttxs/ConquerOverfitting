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
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import java.util.List;

/**
 * This class assists in producing a month view of a calendar. It is immutable.
 * <p>
 * Note that this class works in whole weeks. So, if the month starts on a
 * Wednesday, the first week of the month will still start on Monday, but the
 * the first day of the week will be in the previous month. The same applies at
 * the end of the month.
 */
public class Month {
    private ICalendar calendar;

    private LocalDate monthStart;

    private LocalDate monthEnd;

    private LocalDate viewStart;

    private LocalDate viewEnd;

    private TimeZone timeZone;

    private List weeks;

    private int firstDay;

    public Month( ICalendar calendar, LocalDate date, int firstDay, TimeZone timeZone ) {
        this.calendar = calendar;

        this.firstDay = firstDay;

        this.timeZone = timeZone;

        if ( date == null )
            date = new LocalDate( timeZone );

        // monthStart = new DateMidnight(date.getYear(), date.getMonthOfYear(),
        // 1);
        monthStart = date.withField( DateTimeFieldType.dayOfMonth(), 1 );
        // monthEnd = new DateMidnight(date.getYear(), date.getMonthOfYear(),
        // monthStart.dayOfMonth().getMaximumValue());
        monthEnd = date.withField( DateTimeFieldType.dayOfMonth(), date.dayOfMonth().getMaximumValue() );

        viewStart = CalendarUtils.getFirstDayOfWeek( monthStart, firstDay );
        viewEnd = CalendarUtils.getLastDayOfWeek( monthEnd, firstDay );
    }

    public ICalendar getCalendar() {
        return calendar;
    }

    /**
     * Returns the month number (1-12).
     * 
     * @return the month this object represents.
     */
    public int getMonthOfYear() {
        return monthStart.getMonthOfYear();
    }

    public int getYear() {
        return monthStart.getYear();
    }

    /**
     * @return The List of Week objects for this month.
     */
    public List getWeeks() {
        if ( weeks == null ) {
            // Initialise the weeks list.
            weeks = new java.util.ArrayList();

            LocalDate weekStart = viewStart;
            Period oneWeek = Period.weeks( 1 );
            while ( !weekStart.isAfter( monthEnd ) ) {
                weeks.add( new Week( calendar, weekStart, firstDay, timeZone ) );
                weekStart = weekStart.plus( oneWeek );
            }
        }
        return weeks;
    }

    // public Day getDay(int week, int dayOfWeek)
    // {
    // if (week < 0 || dayOfWeek < 0)
    // return null;
    //
    // DateMidnight date = viewStart.plus(Period.days((week-1) * 7 +
    // (dayOfWeek-1)));
    // return new Day(_group, date);
    // }

    public String getMonthName() {
        return monthStart.monthOfYear().getAsText();
    }

    public LocalDate getMonthStart() {
        return monthStart;
    }

    public LocalDate getMonthEnd() {
        return monthEnd;
    }

    public LocalDate getViewStart() {
        return viewStart;
    }

    public LocalDate getViewEnd() {
        return viewEnd;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }
}

