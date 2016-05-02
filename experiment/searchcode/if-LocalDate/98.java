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

import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import com.atlassian.confluence.core.TimeZone;
import com.atlassian.confluence.extra.calendar.CalendarManager;
import com.atlassian.confluence.extra.calendar.model.CalendarException;
import com.atlassian.confluence.extra.calendar.model.EventComparator;
import com.atlassian.confluence.extra.calendar.model.ICalendar;

/**
 * A single day. Provides access to the calendar and other details of the day.
 */
public class Day {
    // private static final DateTimeFormatter FORMAT =
    // DateTimeFormat.forPattern("dd-MMM-yyyy").withZone(DateTimeZone.UTC).withLocale(Locale.ENGLISH);

    private ICalendar calendar;

    private LocalDate date;

    private List events;

    private Month month;

    private int firstDay;

    private TimeZone timeZone;

    private long dateId;

    private String dateJs;

    private DateTimeZone dateTimeZone;

    public Day( ICalendar calendar, LocalDate date, int firstDay, TimeZone timeZone ) {
        this.calendar = calendar;
        this.date = date;
        this.firstDay = firstDay;
        this.timeZone = timeZone;

        // The date id is the milliseconds between the date and January 01,
        // 1970, UTC.
        dateId = DateTimeUtils.getInstantMillis( date.toDateMidnight( DateTimeZone.UTC ) );
        dateJs = "new Date(" + dateId + ")";
    }

    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns the list of events for this date in the specified time zone.
     * 
     * @return the list of Events that occur on this day.
     * @throws com.atlassian.confluence.extra.calendar.model.CalendarException
     *             if there was a problem retrieving the events.
     */
    public List getEvents() throws CalendarException {
        Interval theDay = null;
        try {
            theDay = date.toInterval( getDateTimeZone() );
        } catch ( IllegalArgumentException e ) {
            DateTime end = date.plusDays( 1 ).toDateTimeAtMidnight( getDateTimeZone() );
            long adjustedMillis = getDateTimeZone().previousTransition( end.getMillis() ) + 1;
            DateTime start = new DateTime( adjustedMillis, getDateTimeZone() );
            theDay = new Interval( start, end );
        }
        if ( events == null ) {
            if ( calendar == null )
                return Collections.EMPTY_LIST;

            events = calendar.findEvents( theDay, getDateTimeZone() );
            Collections.sort( events, new EventComparator() );
        }
        return events;
    }

    private DateTimeZone getDateTimeZone() {
        if ( dateTimeZone == null && timeZone != null )
            dateTimeZone = CalendarManager.getInstance().getDateTimeZone( timeZone );

        return dateTimeZone;
    }

    public String getDayOfWeek() {
        return date.dayOfWeek().getAsText();
    }

    public int getDayOfMonth() {
        return date.getDayOfMonth();
    }

    public int getMaxDayOfMonth() {
        return date.dayOfMonth().getMaximumValue();
    }

    public Month getMonth() {
        if ( month == null )
            month = new Month( calendar, date, firstDay, timeZone );
        return month;
    }

    public long getDateId() {
        return dateId;
    }

    public String getDateJs() {
        return dateJs;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }
}

