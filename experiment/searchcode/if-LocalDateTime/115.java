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

import com.atlassian.confluence.core.TimeZone;
import com.atlassian.confluence.extra.calendar.CalendarManager;
import com.atlassian.confluence.extra.calendar.model.CalendarException;
import com.atlassian.confluence.extra.calendar.model.ICalendar;
import com.atlassian.confluence.extra.calendar.model.IEvent;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

/**
 * Supports actions which deal with events.
 */
public abstract class AbstractEventAction extends AbstractSubCalendarAction {
    private static final Logger log = Logger.getLogger( AbstractEventAction.class );

    private String _eventId;

    private IEvent _event;
    
    private IEvent _baseEvent;//used when modifying all occurrences of recurring event

    protected String eventSummary;

    protected String eventLocation;

    protected String repeat;//recurring events type

    protected boolean allDay;

    private int startYear;

    private int startMonth;

    private int startDay;

    private int startHour;

    private int startMinute;

    private int endYear;

    private int endMonth;

    private int endDay;

    private int endHour;

    private int endMinute;

    private DateTimeZone eventDateTimeZone;

    private TimeZone eventTimeZone;

    private boolean allOccurrences;//Are we editing/deleting one or all occurrences of recurring event

    private boolean editMode;//Is this in edit mode
    

    protected AbstractEventAction( boolean requireEditPermission ) {
        super( requireEditPermission );
    }

    public LocalDateTime getEndDate() {
        if ( isAllDay() )
            return new LocalDateTime( endYear, endMonth, endDay, 0, 0 );
        else
            return new LocalDateTime( endYear, endMonth, endDay, endHour, endMinute, 0, 0 );
    }

    protected void setEndDate( LocalDateTime endDate ) {
        if ( endDate != null ) {
            setEndYear( endDate.getYear() );
            setEndMonth( endDate.getMonthOfYear() );
            setEndDay( endDate.getDayOfMonth() );
            setEndHour( endDate.getHourOfDay() );
            setEndMinute( endDate.getMinuteOfHour() );
        } else {
            setEndYear( 0 );
            setEndMonth( 0 );
            setEndDay( 0 );
            setEndHour( 0 );
            setEndMinute( 0 );
        }
    }

    protected void setStartDate( LocalDateTime startDate ) {
        if ( startDate != null ) {
            setStartYear( startDate.getYear() );
            setStartMonth( startDate.getMonthOfYear() );
            setStartDay( startDate.getDayOfMonth() );
            setStartHour( startDate.getHourOfDay() );
            setStartMinute( startDate.getMinuteOfHour() );
        } else {
            setStartYear( 0 );
            setStartMonth( 0 );
            setStartDay( 0 );
            setStartHour( 0 );
            setStartMinute( 0 );
        }
    }

    public LocalDateTime getStartDate() {
        if ( startYear != 0 && startMonth > 0 && startMonth <= 12 && startDay > 0 ) {
            if ( isAllDay() )
                return new LocalDateTime( startYear, startMonth, startDay, 0, 0 );
            else
                return new LocalDateTime( startYear, startMonth, startDay, startHour, startMinute );
        }
        return null;
    }

    public String getEventId() {
        return _eventId;
    }

    public void setEventId( String eventId ) {
        this._eventId = eventId;
        this._event = null;
    }

    public IEvent getEvent() {
        if ( _event == null && _eventId != null ) {
            ICalendar cal = getSubCalendar();
            if ( cal == null ) // try the group
                cal = getCalendar();

            if ( cal != null ) {
                try {
                    _event = cal.findEvent( _eventId );
                } catch ( CalendarException e ) {
                    log.error( "Problem while retrieving event '" + _eventId + "' from calendar '" + cal.getId(),
                            e );
                }
            }
       }
        return _event;
    }

    /*
     * Used when we need to modify all occurrences of an recurring event
     */
    public IEvent getBaseEvent() {
        if ( _baseEvent == null && _eventId != null ) {
            ICalendar cal = getSubCalendar();
            if ( cal == null ) // try the group
                cal = getCalendar();

            if ( cal != null ) {
                try {
                    _baseEvent = cal.findBaseEvent( _eventId );
                } catch ( CalendarException e ) {
                    log.error( "Problem while retrieving event '" + _eventId + "' from calendar '" + cal.getId(),
                            e );
                }
            }
        }
        return _baseEvent;
    }

    public String getEventSummary() {
        return eventSummary;
    }

    public void setEventSummary( String eventSummary ) {
        this.eventSummary = eventSummary;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation( String eventLocation ) {
        this.eventLocation = eventLocation;
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear( int startYear ) {
        this.startYear = startYear;
    }

    public int getStartMonth() {
        return startMonth;
    }

    public void setStartMonth( int startMonth ) {
        this.startMonth = startMonth;
    }

    public int getStartDay() {
        return startDay;
    }

    public void setStartDay( int startDay ) {
        this.startDay = startDay;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour( int startHour ) {
        this.startHour = startHour;
    }

    public int getStartHour12() {
        int hour12 = startHour % 12;
        return ( hour12 == 0 ) ? 12 : hour12;
    }

    public void setStartHour12( int startHour12 ) {
        startHour12 = startHour12 % 12;
        startHour = ( startHour < 12 ) ? startHour12 : 12 + startHour12;
    }

    public boolean isStartAM() {
        return startHour < 12;
    }

    public void setStartAM( boolean startAM ) {
        if ( startAM && startHour >= 12 )
            startHour -= 12;
        else if ( !startAM && startHour < 12 )
            startHour += 12;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute( int startMinute ) {
        this.startMinute = startMinute;
    }

    public int getEndYear() {
        return endYear;
    }

    public void setEndYear( int endYear ) {
        this.endYear = endYear;
    }

    public int getEndMonth() {
        return endMonth;
    }

    public void setEndMonth( int endMonth ) {
        this.endMonth = endMonth;
    }

    public int getEndDay() {
        return endDay;
    }

    public void setEndDay( int endDay ) {
        this.endDay = endDay;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour( int endHour ) {
        this.endHour = endHour;
    }

    public int getEndHour12() {
        int hour12 = endHour % 12;
        return ( hour12 == 0 ) ? 12 : hour12;
    }

    public void setEndHour12( int endHour12 ) {
        endHour12 = endHour12 % 12;
        endHour = ( endHour < 12 ) ? endHour12 : 12 + endHour12;
    }

    public boolean isEndAM() {
        return endHour < 12;
    }

    public void setEndAM( boolean endAM ) {
        if ( endAM && endHour >= 12 )
            endHour -= 12;
        else if ( !endAM && endHour < 12 )
            endHour += 12;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public void setEndMinute( int endMinute ) {
        this.endMinute = endMinute;
    }
        
    public String getRepeat() {
    	return repeat;
    }

    public void setRepeat(String repeat) {
    	this.repeat = repeat;
    }

    public boolean isAllOccurrences() {
    	return allOccurrences;
    }
    
    public void setAllOccurrences(boolean allOccurrences) {
    	this.allOccurrences = allOccurrences;
    }

    public boolean isEditMode() {
    	return editMode;
    }
    
    public void setEditMode(boolean editMode) {
    	this.editMode = editMode;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public void setAllDay( boolean allDay ) {
        this.allDay = allDay;
    }

    public String getTimeZoneId() {
        if ( eventDateTimeZone != null )
            return eventDateTimeZone.getID();

        return null;
    }

    public void setTimeZoneId( String eventDateTimeZoneId ) {
        if ( eventDateTimeZoneId != null && eventDateTimeZoneId.trim().length() > 0 ) {
            eventDateTimeZone = DateTimeZone.forID( eventDateTimeZoneId );
        } else {
            eventDateTimeZone = null;
        }
    }

    public TimeZone getEventTimeZone() {
        if ( eventTimeZone == null && eventDateTimeZone != null )
            eventTimeZone = CalendarManager.getInstance().getTimeZone( eventDateTimeZone );

        return eventTimeZone;
    }

    /**
     * Returns the {@link DateTimeZone} for the event.
     * 
     * @return the current event's timezone.
     */
    public DateTimeZone getEventDateTimeZone() {
        return eventDateTimeZone;
    }

    public void setEventDateTimeZone( DateTimeZone eventDateTimeZone ) {
        this.eventDateTimeZone = eventDateTimeZone;
    }
    
}

