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
package com.atlassian.confluence.extra.calendar.ical.model;

import com.atlassian.confluence.extra.calendar.model.ICalendar;
import com.atlassian.confluence.extra.calendar.model.AEvent;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import java.net.URI;

/**
 * Represents an event in the calendar.
 */
public class ICalEvent extends AEvent {
    private EventDetails details;

    private LocalDateTime instanceDate;

    private DateTimeZone timeZone;

    private Period startOffset;

    /**
     * Constructs a new occurrence of the specified details. The occurrence date
     * will also be the start date.
     * 
     * @param details
     *            The details which is occurring.
     * @param instanceDate
     *            The date/time it occurs on.
     * @param timeZone
     *            The timezone the date occurs in.
     */
    public ICalEvent( BaseEventDetails details, LocalDateTime instanceDate, DateTimeZone timeZone ) {
        this( details, instanceDate, timeZone, null );
    }

    public ICalEvent( OverrideEventDetails details, LocalDateTime instanceDate, DateTimeZone timeZone ) {
        this( details, instanceDate, timeZone, details.getStartOffset() );
    }

    /**
     * Constructs a new occurrence of the specified details. This constructor
     * allows for an offset to be set for the start time, which is generally
     * used by override events.
     * 
     * @param details
     *            The details which is occurring.
     * @param instanceDate
     *            The date/time the occurrence is representing
     * @param timeZone
     *            The timezone for the event.
     * @param startOffset
     *            The period of time that the actual start date/time is offset
     *            from the instanceDate.
     */
    protected ICalEvent( EventDetails details, LocalDateTime instanceDate, DateTimeZone timeZone,
            Period startOffset ) {
        this.details = details;
        this.instanceDate = instanceDate;
        this.timeZone = timeZone;
        this.startOffset = startOffset;
    }

    public EventDetails getDetails() {
        return details;
    }

    /**
     * The date/time which this occurrence is representing. If the event is an
     * 'override' event, the occurrence date may be different to the official
     * start date.
     * 
     * @return the date the event is occuring on.
     */
    public LocalDateTime getInstanceDate() {
        return instanceDate;
    }

    public LocalDateTime getStartDate() {
        if ( startOffset != null )
            return instanceDate.plus( startOffset );
        return instanceDate;
    }

    public LocalDateTime getEndDate() {
        return getStartDate().plus( getDuration() );
    }

    /**
     * The timezone the event occurs in.
     * 
     * @return the event timezone.
     */
    public DateTimeZone getTimeZone() {
        return timeZone;
    }

    public Period getStartOffset() {
        return startOffset;
    }

    public ICalendar getCalendar() {
        return details.getCalendar();
    }

    /**
     * Returns a unique identifier for this event. It <i>should</i> be unique
     * within the universe, but may not be. This is <b>not</b> guaranteed to be
     * the UID of the event, as defined by RFC 2445.
     * 
     * @return The unique ID of this event.
     */
    public String getId() {
        EventId id = new EventId( details, instanceDate, timeZone );
        return id.toString();
    }

    public String getSummary() {
        return details.getSummary();
    }

    public String getDescription() {
        return details.getDescription();
    }

    public ICalOrganizer getOrganizer() {
        return details.getOrganizer();
    }

    public String getLocation() {
        return details.getLocation();
    }

    public String getRepeat() {
       if (details instanceof BaseEventDetails) {
    	   return ((BaseEventDetails)details).getRepeat();
       } else {
    	   return null;
       }    	   
    }
    
    public boolean isAllDay() {
        return details.isAllDay();
    }

    public Duration getDuration() {
        return details.getDuration();
    }

    public String getColor() {
        return getCalendar().getColor();
    }

    public String getStringProperty( String name, String defaultValue ) {
        return details.getStringProperty( name, defaultValue );
    }

    public boolean isReadOnly() {
        return details.isReadOnly();
    }

    public URI getUrl() {
        return details.getUrl();
    }
}

