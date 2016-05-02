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
package com.atlassian.confluence.extra.cal2.model;

import java.net.URI;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;

/**
 * Represents a single event in a calendar. Each event occurs once at a specific
 * date, and possibly time.
 */
public interface IEvent {
    ICalendar getCalendar();

    /**
     * An identifier that is unique to this event within its _group.
     * 
     * @return the unique id.
     */
    String getId();

    /**
     * A one-line summary of the event.
     * 
     * @return the event summary.
     */
    String getSummary();

    /**
     * The location of the event.
     * 
     * @return the event location.
     */
    String getLocation();

    /**
     * The lnk
     */
    URI getUrl();
   
    /**
     * The string that determines what type of recurring event this is
     * @return
     */
    String getRepeat();
    
    /**
     * The last date for repeating event
     * 
     * @return the event last date.
     */
    LocalDateTime getLastDate();

    /**
     * If <code>true</code>, the event is valid for the whole day rather than
     * a specific time. The start and end date will reflect this by having their
     * time set to midnight, and the timezone should be <code>null</code>.
     * 
     * @return <code>true</code> if the event is valid for the whole day.
     */
    boolean isAllDay();

    /**
     * The start date and time of the event.
     * 
     * @return the event start date.
     */
    LocalDateTime getStartDate();

    /**
     * Returns the start date/time of this event, aligned with the specified
     * timezone.
     * 
     * @param targetTimeZone
     *            The timezone the date/time should be aligned with.
     * @return The exact start date/time in the specified timezone.
     * @throws NullPointerException
     *             if the targetTimeZone is <code>null</code>.
     */
    DateTime getStartDateTime( DateTimeZone targetTimeZone );

    /**
     * The end date and time of the event.
     * 
     * @return the event end date.
     */
    LocalDateTime getEndDate();

    /**
     * Returns the end date/time of this event, aligned with the specified
     * timezone.
     * 
     * @param targetTimeZone
     *            The timezone the date/time should be aligned with.
     * @return The exact start date/time in the specified timezone.
     * @throws NullPointerException
     *             if the targetTimeZone is <code>null</code>.
     */
    DateTime getEndDateTime( DateTimeZone targetTimeZone );

    /**
     * The timezone the event occurs in. If this event {@link #isAllDay()},
     * this should return null, since all-day events apply to the date, not an
     * exact 24-hour period.
     * 
     * @return the event timezone.
     */
    DateTimeZone getTimeZone();

    /**
     * The duration of the event.
     * 
     * @return the event duration.
     */
    Duration getDuration();

    /**
     * Returns the CSS colour the event should be.
     * 
     * @return The event colour as a CSS-friendly value
     */
    String getColor();
}

