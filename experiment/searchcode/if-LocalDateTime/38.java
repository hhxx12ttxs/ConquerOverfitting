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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Range;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Uid;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.ReadableInstant;

/**
 * Override events replace a specific recurrence of an event with new details.
 */
public class OverrideEventDetails extends EventDetails {
    private BaseEventDetails baseEvent;

   /**
    * used to create a new override event from the baseeventdetails
    * @param calendar
    * @param baseEventDetails
    */ 
   public OverrideEventDetails( ICalCalendar calendar, BaseEventDetails baseEvent, EventId eventId) {
   	   super( calendar, (VEvent) null );
   	   
   	   setBaseEvent( baseEvent) ;
   	   
   	   this.vevent = new VEvent();
       // Generate a UID
       addProperty( new Uid( baseEvent.getUid() ) );
       incSequence();
       
       // Check that the DTSTAMP property is set correctly.
       DtStamp dtStamp = ( DtStamp ) getProperty( Property.DTSTAMP );
       net.fortuna.ical4j.model.DateTime dateTime;

       if ( dtStamp == null ) {
           dateTime = new net.fortuna.ical4j.model.DateTime( true );
           addProperty( new DtStamp( dateTime ) );
       } else {
           dateTime = dtStamp.getDateTime();
       }

       if ( dateTime != null && dateTime.getTimeZone() == null )
           dateTime.setUtc( true );

       setOverrideDate(eventId.getInstanceDate(), eventId.getTimeZone(),
    		           baseEvent.isAllDay() ? Value.DATE : null);

   }

   OverrideEventDetails( ICalCalendar calendar, VEvent event ) {
    	super( calendar, event );

    	// Check that the DTSTAMP property is set correctly.
        DtStamp dtStamp = ( DtStamp ) getProperty( Property.DTSTAMP );
        if ( dtStamp == null )
            addProperty( new DtStamp( new net.fortuna.ical4j.model.DateTime() ) );
     }

    void setBaseEvent( BaseEventDetails parent ) {
        this.baseEvent = parent;
    }

    public BaseEventDetails getBaseEvent() {
        return baseEvent;
    }
 

    /**
     * Returns a list of the times this event occurs within the specified date
     * range. Occurrences where the from or to date/time are <i>inside</i> the
     * event (i.e. the event starts before the 'from' date but ends after it, or
     * vise-versa) are included.
     * 
     * @param interval
     *            The interval to check occurrence for.
     * @return the list of occurrences.
     */
    public List getEvents( Interval interval, DateTimeZone targetTimeZone ) {
    	 return getEvents( interval, targetTimeZone ,false);
    }
    
    /**
     * Returns a list of the times this event occurs within the specified date
     * range. Occurrences where the from or to date/time are <i>inside</i> the
     * event (i.e. the event starts before the 'from' date but ends after it, or
     * vise-versa) are included.
     * 
     * @param interval
     *            The interval to check occurrence for.
     * @param targetTimeZone 
     * @param useOverrideDate  - true use overrideDate ,false use startDate         
     * @return the list of occurrences.
     */
    public List getEvents( Interval interval, DateTimeZone targetTimeZone, boolean useOverrideDate) {
        Interval rinterval = getRecurrenceInterval( interval );

        if ( rinterval != null && baseEvent != null ) {
            Recur recur = baseEvent.getRecur();
            Period offset = getStartOffset();
            Set exclusions = getExcludedDates();

            return processRecurrence( baseEvent.getStartDate(), rinterval, targetTimeZone, recur, exclusions,
                    null, null, offset );
        } else {
        	Interval evtInterval = null;
        	if (useOverrideDate) {//return eventinterval by the overrideDate (recurrence-id)
        		evtInterval = getOverrideInterval( targetTimeZone );
        	} else { //return eventinterval by the startDate
        		evtInterval =  getInterval( targetTimeZone );
        	}
            if ( interval.overlaps( evtInterval ) )
                return Collections.singletonList( new ICalEvent( this, getOverrideDate(), getTimeZone() ) );
        }

        return Collections.EMPTY_LIST;
    }

    public Period getStartOffset() {
        return new Period( getOverrideDate(), getStartDate() );
    }

    protected Interval getRecurrenceInterval( Interval interval ) {
        RecurrenceId recurrenceId = ( RecurrenceId ) getProperty( Property.RECURRENCE_ID );
        // Then, check for recurrences.
        if ( recurrenceId != null ) // it is an explicit recurrence
        {
            Range range = ( Range ) recurrenceId.getParameters().getParameter( Parameter.RANGE );
            if ( range != null ) {
                ReadableInstant recurrenceDate = ICalUtil.toJodaReadableInstant( recurrenceId.getDate() );

                if ( Range.THISANDPRIOR.equals( range.getValue() ) ) {
                    if ( interval.getEnd().isAfter( recurrenceDate ) )
                        return interval.withEnd( recurrenceDate );

                    return interval;
                } else if ( Range.THISANDFUTURE.equals( range.getValue() ) ) {
                    if ( interval.getStart().isBefore( recurrenceDate ) )
                        return interval.withStart( recurrenceDate );

                    return interval;
                }
            }
        }

        return null;
    }

    /**
     * find interval using the override date
     * @param timeZone
     * @return
     */
    protected Interval getOverrideInterval( DateTimeZone timeZone ) {
    	LocalDateTime start = getOverrideDate();
        DateTimeZone eventTimeZone = getTimeZone();
        Duration dur = getDuration();

        if ( start != null && dur != null ) {
            if ( dur.getMillis() < 0 )
                dur = new Duration( 1000 );

            DateTime startDt;
            if ( eventTimeZone != null )
                startDt = start.toDateTime( eventTimeZone ).toDateTime( timeZone );
            else
                startDt = start.toDateTime( timeZone );

            return new Interval( startDt, dur );
        }

        return null;
    }

    public LocalDateTime getOverrideDate() {
        RecurrenceId rid = ( RecurrenceId ) getProperty( Property.RECURRENCE_ID );
        if ( rid != null )
            return ICalUtil.toJodaLocalDateTime( rid.getDate() );

        return null;
    }
   
    public void setOverrideDate(LocalDateTime overrideDate, DateTimeZone dateTimeZone, Value valueType) {
    	checkReadOnly();
     	
    	RecurrenceId recurrenceId = ( RecurrenceId ) getProperty(Property.RECURRENCE_ID );
    	if (recurrenceId != null) {
    		removeProperty(recurrenceId);
    	}

    	if (overrideDate != null) {
    		recurrenceId = new RecurrenceId( ICalUtil.toICalDate(overrideDate,dateTimeZone ));
    		if ( valueType != null )
                recurrenceId.getParameters().add( valueType );
    		addProperty(recurrenceId);
    	}
    }
}
