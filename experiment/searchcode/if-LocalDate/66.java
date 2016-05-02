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
package com.atlassian.confluence.extra.calendar.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

import com.atlassian.confluence.core.TimeZone;
import com.atlassian.confluence.extra.calendar.CalendarHandler;
import com.atlassian.confluence.extra.calendar.CalendarManager;
import com.atlassian.confluence.extra.calendar.CalendarUtils;
import com.atlassian.confluence.extra.calendar.display.CalendarDisplay;
import com.atlassian.confluence.extra.calendar.model.CalendarGroup;
import com.atlassian.confluence.extra.calendar.model.ICalendar;
import com.atlassian.core.util.PairType;

/**
 * Displays the calendar with the current settings.
 */
public class DisplayCalendarAction extends AbstractCalendarAction {
    // private static final String EVENT_MODE = "event";
    private static final String MONTH_MODE = "month";

    private static final String WEEK_MODE = "week";

    private static final String DAY_MODE = "day";

    private static final PeriodFormatter periodFormatter = ISOPeriodFormat.standard();

    private String _mode;

    private int day;

    private int month;

    private int year;

    private int weekNum;

    private int offset;

    private CalendarDisplay _calendarDisplay;

    protected String subCalendarId;

    private ICalendar _subCalendar;

    private String targetNamespace;

    private int firstDay;

    private int weekYear;

    private int _maxEventListCount = 20;

    private Period _eventListPeriod = Period.years( 1 );

    public DisplayCalendarAction() {
        super( false );
    }

    public String execute() throws Exception {
        return SUCCESS;
    }

    /**
     * Toggle the visibility of the calendar
     * 
     * @return
     */
    public String toggle() throws Exception {
        CalendarDisplay display = getCalendarDisplay();
        setDate( getDate() );
        display.setDate( getDate() );
        CalendarGroup calendar = getCalendar();
        ICalendar iCal = getSubCalendar();
        if ( calendar.isUndisplayable( iCal ) == true ) {
            calendar.clearUndisplayable( iCal );
        } else {
            calendar.setUndisplayable( iCal );
        }
        return saveCalendar();
    }

    public String next() throws Exception {
        String mode = getMode();
        CalendarDisplay display = getCalendarDisplay();
        LocalDate date;

        if ( DAY_MODE.equals( mode ) )
            date = getDate().plus( Period.days( 1 ) );
        else if ( WEEK_MODE.equals( mode ) )
            date = getDate().plus( Period.weeks( 1 ) );
        else if ( MONTH_MODE.equals( mode ) )
            date = getDate().plus( Period.months( 1 ) );
        else
            // event mode
            date = getDate().plus( Period.days( 1 ) );

        setDate( date );
        display.setDate( date );

        return SUCCESS;
    }

    public String prev() throws Exception {
        String mode = getMode();
        CalendarDisplay display = getCalendarDisplay();
        LocalDate date;

        if ( DAY_MODE.equals( mode ) )
            date = getDate().minus( Period.days( 1 ) );
        else if ( WEEK_MODE.equals( mode ) )
            date = getDate().minus( Period.weeks( 1 ) );
        else if ( MONTH_MODE.equals( mode ) )
            date = getDate().minus( Period.months( 1 ) );
        else
            // event mode
            // This shoul not be year. Should this be days?
            date = getDate().minus( Period.days( 1 ) );

        setDate( date );
        display.setDate( date );

        return SUCCESS;
    }

    private void setDate( LocalDate date ) {
        setYear( date.getYear() );
        setMonth( date.getMonthOfYear() );
        setDay( date.getDayOfMonth() );
    }

    public LocalDate getDate() {
        LocalDate date = null;
        if ( year != 0 && month != 0 && day != 0 ) {
            date = new LocalDate( year, month, day );

            // Add a day if the browser is in a timezone before UTC (JS offset
            // is reversed)
            if ( offset > 0 )
                date = date.plusDays( 1 );

            if ( weekNum != 0 ) {
                LocalDate weekStart = CalendarUtils.getFirstDayOfWeek( date, firstDay );
                LocalDate yearStart = CalendarUtils.getFirstWeekOfYear( weekYear, firstDay );

                // Shift the date
                date = yearStart.plus( Period.weeks( weekNum - 1 ) ).plus( new Period( weekStart, date ) );
            }
        }

        if ( date == null )
            date = new LocalDate( getCalendarDateTimeZone() );

        return date;
    }

    private DateTimeZone getCalendarDateTimeZone() {
        return CalendarManager.getInstance().getDateTimeZone( getCalendarTimeZone() );
    }

    private TimeZone getCalendarTimeZone() {
        return CalendarManager.getInstance().getTimeZone( getRemoteUser() );
    }

    public int getDay() {
        return day;
    }

    public void setDay( int day ) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth( int month ) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear( int year ) {
        this.year = year;
    }

    public int getFirstDay() {
        if ( firstDay <= 0 )
            firstDay = DateTimeConstants.MONDAY;
        return firstDay;
    }

    protected void buildCalendarView() {
    }

    public CalendarDisplay getCalendarDisplay() {
        if ( _calendarDisplay == null )
            _calendarDisplay = new CalendarDisplay( getCalendar(), getDate(), getFirstDay(),
                    getCalendarTimeZone(), getMaxEventListCount(), _eventListPeriod );

        return _calendarDisplay;
    }

    public void setCalendarView( CalendarDisplay calendarDisplay ) {
        this._calendarDisplay = calendarDisplay;
    }

    public String getMode() {
        if ( _mode == null )
            return MONTH_MODE;
        return _mode;
    }

    public void setMode( String mode ) {
        this._mode = mode;
    }

    public String getSubCalendarId() {
        return subCalendarId;
    }

    public void setSubCalendarId( String subCalendarId ) {
        this.subCalendarId = subCalendarId;
    }

    public ICalendar getSubCalendar() {
        if ( _subCalendar == null ) {
            CalendarGroup group = getCalendar();
            if ( group != null )
                _subCalendar = group.getDescendent( subCalendarId );
        }
        return _subCalendar;
    }

    protected String handleAction( int action ) {
        ICalendar calendar = getSubCalendar();

        if ( calendar != null ) {
            CalendarHandler handler = getCalendarManager().findHandler( calendar );

            if ( handler.supportsAction( calendar, action ) ) {
                targetNamespace = handler.getNamespace();
                return SUCCESS;
            }
        }

        return ERROR;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public int getWeekNum() {
        return weekNum;
    }

    public void setWeekNum( int weekNum ) {
        this.weekNum = weekNum;
    }

    public void setFirstDay( int firstDay ) {
        this.firstDay = firstDay;
    }

    public int getWeekYear() {
        return weekYear;
    }

    public void setWeekYear( int weekYear ) {
        this.weekYear = weekYear;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset( int offset ) {
        this.offset = offset;
    }

    public int getMaxEventListCount() {
        return _maxEventListCount;
    }

    public void setMaxEventListCount( int maxEventListCount ) {
        this._maxEventListCount = maxEventListCount;
    }

    public String getEventListPeriod() {
        return _eventListPeriod == null ? null : periodFormatter.print( _eventListPeriod );
    }

    public void setEventListPeriod( String _eventListPeriod ) {
        this._eventListPeriod = periodFormatter.parsePeriod( _eventListPeriod );
    }

    public List getAvailableTimeZones() {
        List result = new ArrayList();
        TimeZone defaultTimeZone = settingsManager.getGlobalSettings().getTimeZone();
        result.add( new PairType( defaultTimeZone.getID(), defaultTimeZoneCaption( defaultTimeZone ) ) );

        List timeZones = TimeZone.getSortedTimeZones();
        for ( Iterator iter = timeZones.iterator(); iter.hasNext(); ) {
            TimeZone tz = ( TimeZone ) iter.next();
            if ( tz.equals( defaultTimeZone ) )
                continue;
            result.add( new PairType( tz.getID(), timeZoneCaption( tz ) ) );
        }

        return result;
    }

    private String defaultTimeZoneCaption( TimeZone defaultTimeZone ) {
        return getText( "time.zone.server.default", new Object[]{timeZoneCaption( defaultTimeZone )} );
    }

    private String timeZoneCaption( TimeZone timeZone ) {
        return "(GMT" + timeZone.getDisplayOffset() + ") " + getText( timeZone.getMessageKey() );
    }
}

