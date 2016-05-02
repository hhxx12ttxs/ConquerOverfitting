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

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Sequence;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Url;
import net.fortuna.ical4j.model.property.XProperty;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import com.atlassian.confluence.extra.calendar.model.ICalendar;

/**
 * This is the abstract base class for event details. There are two subclasses:
 * {@link BaseEventDetails} and {@link OverrideEventDetails}.
 */
public abstract class EventDetails extends ICalObject {
    protected ICalCalendar calendar;

    protected VEvent vevent;

    protected ICalOrganizer organizer;

    private Set excluded;

    protected EventDetails( ICalCalendar calendar, VEvent event ) {
        this.calendar = calendar;
        this.vevent = event;
    }

    public String getUid() {
        Uid uid = ( Uid ) getProperty( Property.UID );
        if ( uid != null )
            return uid.getValue();
        return null;
    }

    public ICalOrganizer getOrganizer() {
        return organizer;
    }

    public String getStringProperty( String name, String defaultValue ) {
        Property prop = getProperty( Property.EXPERIMENTAL_PREFIX + name );
        if ( prop != null )
            return prop.getValue();

        return defaultValue;
    }

    public URI getUrl() {
        Url url = ( Url ) getProperty( Property.URL );
        if ( url != null )
            return url.getUri();

        return null;
    }

    /**
     * Increments the sequence number when major properties are changed.
     */
    protected void incSequence() {
        Sequence seq = ( Sequence ) getProperty( Property.SEQUENCE );
        if ( seq == null ) {
            seq = new Sequence( 0 );
            addProperty( seq );
        } else {
            seq.setValue( String.valueOf( seq.getSequenceNo() + 1 ) );
        }
    }

    public VEvent getEventComponent() {
        return vevent;
    }

    public ICalendar getCalendar() {
        return calendar;
    }

    void setCalendar( ICalCalendar calendar ) {
        this.calendar = calendar;
    }

    public String getSummary() {
        Summary summary = ( Summary ) getProperty( Property.SUMMARY );
        if ( summary != null )
            return summary.getValue();

        return null;
    }

    public void setSummary( String summary ) {
        checkReadOnly();

        Summary prop = ( Summary ) getProperty( Property.SUMMARY );
        if ( prop != null )
            removeProperty( prop );

        if ( summary != null ) {
            prop = new Summary( summary );
            addProperty( prop );
        }
    }

    public String getDescription() {
        Description description = ( Description ) getProperty( Property.DESCRIPTION );
        if ( description != null )
            return description.getValue();

        return null;
    }

    public void setDescription( String description ) {
        checkReadOnly();

        Description prop = ( Description ) getProperty( Property.DESCRIPTION );
        if ( prop != null )
            removeProperty( prop );

        if ( description != null ) {
            prop = new Description( description );
            addProperty( prop );
        }
    }

    public void setOrganizer( ICalOrganizer newOrganizer ) {
        checkReadOnly();

        if ( organizer != null ) {
            // Disassociate the old organizer with this event.
            vevent.getProperties().remove( organizer.getOrganizerProperty() );
            organizer.setDetails( null );
            organizer = null;
        }

        if ( newOrganizer != null ) {
            newOrganizer.setDetails( this );
            addProperty( newOrganizer.getOrganizerProperty() );
        }
    }

    public String getLocation() {
        Location location = ( Location ) getProperty( Property.LOCATION );
        if ( location != null )
            return location.getValue();

        return null;
    }

    public void setLocation( String location ) {
        checkReadOnly();

        Location prop = ( Location ) getProperty( Property.LOCATION );
        if ( prop != null )
            removeProperty( prop );

        if ( location != null ) {
            prop = new Location( location );
            addProperty( prop );
            incSequence();
        }
    }

    /**
     * Returns the timezone for the event. This will be the timezone of the
     * start date/time in this case, which may be different from the end
     * date/time. It will be <code>null</code> if the current event is an all
     * day event.
     * 
     * @return The timezone for this event.
     */
    public DateTimeZone getTimeZone() {
        DtStart dtStart = ( DtStart ) getProperty( Property.DTSTART );

        if ( dtStart != null ) {
            Date date = dtStart.getDate();
            if ( date != null ) {
                return ICalUtil.getJodaDateTimeZone( date );
            }
        }

        return null;
    }

    public LocalDateTime getStartDate() {
        DtStart dtStart = ( DtStart ) getProperty( Property.DTSTART );

        if ( dtStart != null ) {
            Date date = dtStart.getDate();
            if ( date != null ) {
                return ICalUtil.toJodaLocalDateTime( date );
            }
        }

        return null;
    }

    public boolean isAllDay() {
        DtStart dtStart = ( DtStart ) getProperty( Property.DTSTART );
        if ( dtStart != null ) {
            Date date = dtStart.getDate();
            if ( date != null )
                return !ICalUtil.isICalDateTime( date );
        }
        return false;
    }

    /**
     * Returns the effective end date of the event. This may be from a
     * directly-set date/time, or it may be a combination of the start date and
     * the event duration.
     * 
     * @return The end date/time of the event.
     */
    public LocalDateTime getEndDate() {
        LocalDateTime endDate = getEndDateValue();

        if ( endDate == null ) {
            LocalDateTime startDate = getStartDate();
            Duration duration = getDurationValue();
            if ( startDate != null && duration != null ) {
                endDate = startDate.plus( duration );
            } else
                // The end date is the start date.
                endDate = startDate;
        }

        return endDate;
    }

    /**
     * Returns the end date in the local time of this event's timezone.
     * 
     * @return The local end date/time
     * @see #getTimeZone()
     */
    public LocalDateTime getEndDateValue() {
        DtEnd endDate = ( DtEnd ) getProperty( Property.DTEND );

        if ( endDate != null ) {
            Date date = endDate.getDate();
            if ( date != null )
                return ICalUtil.toJodaLocalDateTime( date, getTimeZone() );
        }

        return null;
    }

    public Duration getDuration() {
        Duration duration = getDurationValue();

        if ( duration == null ) {
            LocalDateTime startDate = getStartDate();
            LocalDateTime endDate = getEndDateValue();
            DateTimeZone timeZone = getTimeZone();

            if ( startDate != null && endDate != null )
                duration = new Duration( startDate.toDateTime( timeZone ), endDate.toDateTime( timeZone ) );
        }

        return duration;
    }

    private Duration getDurationValue() {
        net.fortuna.ical4j.model.property.Duration duration = ( net.fortuna.ical4j.model.property.Duration ) getProperty( Property.DURATION );
        if ( duration != null )
            return ICalUtil.toJodaDuration( duration.getDuration() );

        return null;
    }


    protected Interval getInterval( DateTimeZone timeZone ) {
        LocalDateTime start = getStartDate();
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

    public void setUrl( URI uri ) {
        Url url = ( Url ) getProperty( Property.URL );
        if ( uri == null && url != null )
            removeProperty( url );

        if ( uri != null ) {
            if ( url != null )
                url.setUri( uri );
            else {
                url = new Url( uri );
                url.getParameters().add( Value.URI );
                addProperty( url );
            }
        }
    }

    /**
     * Returns a list of the times this event occurs within the specified date
     * range. Occurrences where the from or to date/time are <i>inside</i> the
     * event (i.e. the event starts before the 'from' date but ends after it, or
     * vise-versa) are included.
     * 
     * @param interval
     *            The interval to check for occurrences.
     * @param targetTimeZone
     *            The time zone to targe. This is important for all-day events.
     * 
     * @return the list of {@link ICalEvent} instances of this event
     *         description.
     */
    public abstract List getEvents( Interval interval, DateTimeZone targetTimeZone );

    /**
     * 
     * @param start
     * @param interval
     * @param targetTimeZone
     * @param recur
     * @param exclusions
     * @param overrides -- list of overrides using startDate
     * @param baseOverrides -- list of overrides using the overrideDate
     * @param offset
     * @return
     */
    protected List processRecurrence( LocalDateTime start, Interval interval, DateTimeZone targetTimeZone,
            Recur recur, Set exclusions, Map overrides, Map baseOverrides, Period offset ) {
        List events = new java.util.ArrayList();

        Date from = ICalUtil.toICalDate( interval.getStart() );
        Date to = ICalUtil.toICalDate( interval.getEnd() );

        DateTimeZone eventTimeZone = getTimeZone();

        Value value;
        Date startDate;

        if ( isAllDay() ) {
            // NOTE: There is a bug with iCal4j which converts Dates with no
            // timezone to use
            // the server's timezone for calculating recurrences. To get around
            // this, we
            // have to fake Dates as DateTimes with the targetTimeZone as their
            // zone.
            // value = Value.DATE;
            // startDate =
            // ICalUtil.toICalDate(start.toLocalDate().toDateMidnight(targetTimeZone));
            value = Value.DATE_TIME;
            startDate = ICalUtil.toICalDateTime( start, targetTimeZone );
        } else {
            value = Value.DATE_TIME;
            startDate = ICalUtil
                    .toICalDateTime( start, ( eventTimeZone != null ? eventTimeZone : targetTimeZone ) );
        }

        DateList dates = recur.getDates( startDate, from, to, value );
        Iterator i = dates.iterator();

        while ( i.hasNext() ) {
            Date nextDate = ( Date ) i.next();
            LocalDateTime nextInstant = ICalUtil.toJodaLocalDateTime( nextDate, eventTimeZone );

            if ( !exclusions.contains( nextInstant ) ) {
                ICalEvent event = null;

                if ( overrides != null )
                    event = ( ICalEvent ) baseOverrides.get( nextInstant );
                 
                if ( event == null ) {//Don't add events with overrides, will add overrides below 
            
                    event = new ICalEvent( this, nextInstant, eventTimeZone, offset );

                    events.add( event );
                }
            }
        }
        
        //Currently recur.getDates does not return the override events within interval
        //So lets loop through the override dates and add those to events
        //this needs to be removed and above code adjusted when the recur.getDates bug is fixed
        if (overrides != null && overrides.size()>0) {
        	Iterator keys = overrides.keySet().iterator();
        	while (keys.hasNext()) {
        		LocalDateTime nextInstant = (LocalDateTime) keys.next();
        		  if ( !exclusions.contains( nextInstant ) ) {
                      ICalEvent event = ( ICalEvent ) overrides.get( nextInstant );

                      if ( event != null ) //should never be null
                    	  events.add( event );
                  }
        	}
        }
        return events;
    }

    public void addExcludedDate(LocalDateTime excludedDate,DateTimeZone dateTimeZone) {
        checkReadOnly();

        DateList dateList = null;
        if (excludedDate != null) {
        	ExDate exDate = (ExDate) getProperty(Property.EXDATE);
        	
        	if (exDate != null) {
        		dateList = exDate.getDates();
        		removeProperty(exDate);
        	}
    		if (dateList == null) {
    			dateList = new DateList();
    		}
    		dateList.add(ICalUtil.toICalDate(excludedDate, dateTimeZone));
    		exDate = new ExDate(dateList);
        	addProperty(exDate);
            incSequence();
            excluded = null;
        }   	
    }
    
    public Set getExcludedDates() {
       if ( excluded == null ) {
            excluded = new HashSet();

            PropertyList props = vevent.getProperties().getProperties( Property.EXDATE );
            Iterator i = props.iterator();
            DateTimeZone timeZone = getTimeZone();

            while ( i.hasNext() ) {
                ExDate exDate = ( ExDate ) i.next();
                // exDate.setUtc(true);
                DateList dates = exDate.getDates();
                Iterator j = dates.iterator();
                while ( j.hasNext() ) {
                    excluded.add( ICalUtil.toJodaLocalDateTime( ( Date ) j.next(), timeZone ) );
                }
            }
        }

        return excluded;
    }

 
    public void setStringProperty( String name, String value ) {
        Property prop = getProperty( Property.EXPERIMENTAL_PREFIX + name );
        if ( prop != null )
            removeProperty( prop );

        prop = new XProperty( Property.EXPERIMENTAL_PREFIX + name, value );
        prop.getParameters().add( Value.TEXT );

        addProperty( prop );
    }

    protected Property getProperty( String name ) {
        return vevent.getProperties().getProperty( name );
    }

    protected void removeProperty( Property property ) {
        if ( vevent != null )
            vevent.getProperties().remove( property );
    }

    void addProperty( Property dtStart ) {
        vevent.getProperties().add( dtStart );
    }

    public boolean isReadOnly() {
        return calendar != null && calendar.isReadOnly();
    }

    /**
     * Checks if the current object is read-only and throws an
     * UnsupportedOperationException if it is. <p/> The intention of this method
     * is to make it easy for public 'setXXX' methods on subclasses to test
     * read-only status.
     * 
     * @throws UnsupportedOperationException
     *             if the object is read-only.
     * @see #isReadOnly()
     */
    protected void checkReadOnly() {
        if ( isReadOnly() )
            throw new UnsupportedOperationException( "This value is read-only." );
    }

    public void setDates( LocalDate startDate, LocalDate endDate ) {
        Date start;
        try {
            start = ICalUtil.toICalDate( startDate.toDateMidnight() );
        } catch ( IllegalArgumentException e ) {
            start = processIllegalArgumentException( startDate );
        }
        setStartDate( start, Value.DATE );

        Date end;
        try {
            end = ICalUtil.toICalDate( endDate.toDateMidnight() );
            ;
        } catch ( IllegalArgumentException e ) {
            end = processIllegalArgumentException( endDate );
        }
        setEndDate( end, Value.DATE );
        // setStartDate(ICalUtil.toICalDate(startDate.toDateMidnight()),
        // Value.DATE);
        // setEndDate(ICalUtil.toICalDate(endDate.toDateMidnight()),
        // Value.DATE);
        setDuration( null );
        incSequence();
    }

    private Date processIllegalArgumentException( LocalDate date ) {
        DateTime dt = date.plusDays( 1 ).toDateTimeAtMidnight();
        DateTimeZone zone = DateTimeUtils.getZone( null );
        long adjustedMillis = zone.previousTransition( dt.getMillis() ) + 1;
        DateTime datetime = new DateTime( adjustedMillis, zone );
        return new Date( datetime.toDate() );
    }

    public void setDates( LocalDateTime startDate, LocalDateTime endDate, DateTimeZone timeZone ) {
        // TODO: Add before/after checking on dates.
        setStartDate( ICalUtil.toICalDateTime( startDate, timeZone ), null );
        setEndDate( ICalUtil.toICalDateTime( endDate, timeZone ), null );
        setDuration( null );
        incSequence();
    }

    /**
     * Sets the dates to the specified start with the specified duration
     * 
     * @param startDate
     *            The start date
     * @param duration
     *            The duration.
     */
    public void setDates( LocalDate startDate, Duration duration ) {
        setStartDate( ICalUtil.toICalDate( startDate.toDateMidnight( DateTimeZone.UTC ) ), Value.DATE );
        setEndDate( null, null );
        setDuration( ICalUtil.toICalDur( duration ) );
        incSequence();
    }

    public void setDates( LocalDateTime startDate, Duration duration, DateTimeZone timeZone ) {
        setStartDate( ICalUtil.toICalDateTime( startDate, timeZone ), null );
        setEndDate( null, null );
        setDuration( ICalUtil.toICalDur( duration ) );
        incSequence();
    }

    private void setStartDate( Date startDate, Value valueType ) {
        checkReadOnly();

        DtStart dtStart = ( DtStart ) getProperty( Property.DTSTART );

        // delete it to prevent incompatibilities when switching between all-day
        // and part-day events.
        if ( dtStart != null )
            removeProperty( dtStart );

        if ( startDate != null ) {
            dtStart = new DtStart( startDate );
            if ( valueType != null )
                dtStart.getParameters().add( valueType );

            addProperty( dtStart );
        }
    }

    private void setEndDate( Date endDate, Value valueType ) {
        checkReadOnly();

        DtEnd dtEnd = ( DtEnd ) getProperty( Property.DTEND );

        // delete it to prevent incompatibilities when switching between all-day
        // and part-day events.
        if ( dtEnd != null )
            removeProperty( dtEnd );

        if ( endDate != null ) {
            dtEnd = new DtEnd( endDate );
            if ( valueType != null )
                dtEnd.getParameters().add( valueType );

            addProperty( dtEnd );
        }
    }

    private void setDuration( Dur duration ) {
        checkReadOnly();

        net.fortuna.ical4j.model.property.Duration dp;
        dp = ( net.fortuna.ical4j.model.property.Duration ) getProperty( Property.DURATION );

        if ( duration == null ) {
            if ( dp != null )
                vevent.getProperties().remove( dp );
        } else {
            if ( dp == null ) {
                dp = new net.fortuna.ical4j.model.property.Duration();
                addProperty( dp );
            }

            dp.setDuration( duration );
        }
    }

}
