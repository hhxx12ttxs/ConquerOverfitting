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
package com.atlassian.confluence.extra.cal2.display;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormat;

import com.atlassian.confluence.core.TimeZone;
import com.atlassian.confluence.extra.cal2.mgr.CalendarException;
import com.atlassian.confluence.extra.cal2.mgr.CalendarManager;
import com.atlassian.confluence.extra.cal2.mgr.TimeZoneUtils;
import com.atlassian.confluence.extra.cal2.model.ICalendar;
import com.atlassian.confluence.extra.cal2.model.IEvent;

/**
 * Helper class for displaying the calendar.
 */
public class CalendarDisplay {
    private String[] MONTH_NAMES = new String[]{"January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"};

    private List MONTH_LIST = Arrays.asList( MONTH_NAMES );

    private static final String DATE_FORMAT = "dd MMMM YYYY";

    private static final String TIME_FORMAT = "HH:mm";

    private ICalendar calendar;

    private LocalDate date;

    private Month month;

    private Week week;

    private Day day;

    private LocalDate today;

    private int firstDay;

    private List dayNames;

    private DateTimeZone dateTimeZone;

    private int maxEventListCount;

    private Period eventListPeriod;

    private TimeZone timeZone;

    public CalendarDisplay( ICalendar calendar, LocalDate date, int firstDay, TimeZone timeZone,
            int maxEventListCount, Period eventListPeriod ) {
        this.calendar = calendar;
        this.date = date;
        this.today = new LocalDate( dateTimeZone );
        this.firstDay = firstDay;
        this.timeZone = timeZone;
        this.dateTimeZone = TimeZoneUtils.getDateTimeZone( timeZone );
        this.maxEventListCount = maxEventListCount;
        this.eventListPeriod = eventListPeriod;
    }

    public ICalendar getCalendar() {
        return calendar;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate( LocalDate date ) {
        this.date = date;
        month = null;
        week = null;
        day = null;
    }

    public Month getMonth() {
        if ( month == null )
            month = new Month( calendar, date, firstDay, timeZone );

        return month;
    }

    public Week getWeek() {
        if ( week == null )
            week = new Week( calendar, date, firstDay, timeZone );

        return week;
    }

    public Day getDay() {
        if ( day == null )
            day = new Day( calendar, date, firstDay, timeZone );

        return day;
    }

    public List getAllEvents() throws CalendarException {
        return calendar.findEvents( new Interval( new DateMidnight( 1, 1, 1 ), Period.years( 10000 ) ),
                dateTimeZone );
    }

    public Map getListEvents() throws CalendarException {
        // Grab the events :TODO: extend the calendar.findEvents to take the
        // maxEventListCount
        List events = calendar.findEvents( new Interval( date.toDateMidnight(), eventListPeriod ), dateTimeZone );
        // Trim the event list down, if needed
        if ( events.size() > maxEventListCount ) {
            events = events.subList( 0, maxEventListCount );
        }
        // Order the events by date
        Collections.sort( events, new Comparator() {
            public int compare( Object o1, Object o2 ) {
                IEvent e1 = ( IEvent ) o1;
                IEvent e2 = ( IEvent ) o2;
                // Compare start dates
                return e1.getStartDate().compareTo( e2.getStartDate() );
            }
        } );
        // Convert the list into a map, grouping by date
        LinkedHashMap eventMap = new LinkedHashMap();
        for ( Iterator iter = events.iterator(); iter.hasNext(); ) {
            IEvent event = ( IEvent ) iter.next();
            // Clean up the event date
            DateTime eventDate = event.getStartDate().toDateTime().toDateMidnight().toDateTime();
            // If the event map already contains this date
            List dateEvents;
            if ( eventMap.containsKey( eventDate ) ) {
                // Grab the list
                dateEvents = ( List ) eventMap.get( eventDate );
            } else {
                // Create the list
                dateEvents = new ArrayList();
                // Add the list to the map
                eventMap.put( eventDate, dateEvents );
            }
            // Add the event
            dateEvents.add( event );
        }
        // Return the list
        return eventMap;
    }

    public LocalDate getToday() {
        return today;
    }

    public boolean isToday( Day day ) {
        return day != null && today.equals( day.getDate() );
    }

    public boolean isSelected( Day day ) {
        return day != null && getDate().equals( day.getDate() );
    }

    public String format( ReadablePartial date, String pattern ) {
        if ( date != null ) {
            return DateTimeFormat.forPattern( pattern ).print( date );
        }
        return null;
    }

    public String format( ReadableInstant date, String pattern ) {
        if ( date != null ) {
            return DateTimeFormat.forPattern( pattern ).print( date );
        }
        return null;
    }

    public String format( Date date, String pattern ) {
        if ( date != null ) {
            DateFormat df = new SimpleDateFormat( pattern );
            return df.format( date );
        }
        return null;
    }

    public String formatDate( DateTime date ) {
        return format( date, DATE_FORMAT );
    }

    /**
     * Returns the start time for the specifid 'onDay'.
     * 
     * @param startDate
     *            The real start time of the event.
     * @param onDay
     *            The day we're checking for.
     * @return The start time for the specified day.
     */
    public DateTime getStartDate( DateTime startDate, LocalDate onDay ) {
        DateMidnight dateMidnight;
        try {
            dateMidnight = onDay.toDateMidnight( dateTimeZone );
        } catch ( IllegalArgumentException e ) {
            DateTime dt = onDay.plusDays( 1 ).toDateTimeAtMidnight();
            DateTimeZone zone = DateTimeUtils.getZone( null );
            long adjustedMillis = zone.previousTransition( dt.getMillis() ) + 1;
            dateMidnight = new DateMidnight( adjustedMillis, zone );
        }
        // if (startDate.isBefore(onDay.toDateMidnight(dateTimeZone)))
        if ( startDate.isBefore( dateMidnight ) ) {
            return dateMidnight.toDateTime( dateTimeZone );
        }
        return startDate;
    }

    public DateTime getEndDate( DateTime endDate, LocalDate onDay ) {
        DateMidnight nextDay = onDay.plus( Period.days( 1 ) ).toDateMidnight( dateTimeZone );
        if ( endDate.isBefore( nextDay ) )
            return endDate;

        return nextDay.toDateTime().minusMinutes( 1 );
    }

    public String formatStartDate( IEvent event, LocalDate onDay ) {
        DateTime startDate = getStartDate( event.getStartDateTime( dateTimeZone ), onDay );
        return formatDate( startDate );
    }

    public String formatEndDate( IEvent event, LocalDate onDay ) {
        DateTime endDate = getEndDate( event.getEndDateTime( dateTimeZone ), onDay );
        return formatDate( endDate );
    }

    public String formatStartTime( IEvent event, LocalDate onDay ) {
        DateTime startDate = getStartDate( event.getStartDateTime( dateTimeZone ), onDay );
        return format( startDate, TIME_FORMAT );
    }

    public String formatEndTime( IEvent event, LocalDate onDay ) {
        DateTime endDate = getEndDate( event.getEndDateTime( dateTimeZone ), onDay );
        return format( endDate, TIME_FORMAT );
    }

    public List getMonthNames() {
        return MONTH_LIST;
    }

    public String getMonthName( int monthNumber ) {
        return MONTH_NAMES[monthNumber - 1];
    }

    public List getDayNames() {
        if ( dayNames == null ) {
            dayNames = new java.util.ArrayList();
            DateMidnight date = new DateMidnight();
            date = date.withField( DateTimeFieldType.dayOfWeek(), firstDay );
            Period oneDay = Period.days( 1 );

            for ( int i = 0; i < 7; i++ ) {
                dayNames.add( format( date, "EEEE" ) );
                date = date.plus( oneDay );
            }
        }
        return dayNames;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public DateTimeZone getDateTimeZone() {
        return dateTimeZone;
    }

    public String getTimeZoneLabel() {
        return TimeZoneUtils.getTimeZoneLabel( dateTimeZone );
    }

    public int getMaxEventListCount() {
        return maxEventListCount;
    }

    public void setMaxEventListCount( int maxEventListCount ) {
        this.maxEventListCount = maxEventListCount;
    }

    public Period getEventListPeriod() {
        return eventListPeriod;
    }

    public void setEventListPeriod( Period eventListPeriod ) {
        this.eventListPeriod = eventListPeriod;
    }
}

