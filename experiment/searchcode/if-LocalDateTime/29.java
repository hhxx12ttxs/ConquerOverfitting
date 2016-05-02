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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.NumberList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Uid;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;

import com.opensymphony.util.GUID;

/**
 * Source events are the original event created, before any recurrence or other
 * trickiness is applied.
 */
public class BaseEventDetails extends EventDetails {
	
	//Types of recurrence events
    public static final String NO_REPEAT = "none";
    public static final String NOT_IMPLEMENTED = "notimplemented";
    public static final String DAILY = "daily";
    public static final String WEEKDAYS = "weekdays";
    public static final String WEEKLY = "weekly";
    public static final String BIWEEKLY = "biweekly";
    public static final String MONTHLY = "monthly";
    public static final String YEARLY = "yearly";
	
    private Map _overrides;

    public BaseEventDetails() {
        this( null, null );
    }

    BaseEventDetails( ICalCalendar calendar, VEvent event ) {
        super( calendar, event );

        if ( this.vevent == null ) // it's a new event.
        {
            this.vevent = new VEvent();

            // Generate a UID
            addProperty( new Uid( GUID.generateGUID() ) );
            incSequence();
        } else // it's an existing event
        {
            Organizer org = ( Organizer ) getProperty( Property.ORGANIZER );
            if ( org != null )
                this.organizer = new ICalOrganizer( this, org );
        }

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
    }

    /**
     * Returns a map of ICalEvents which override the base event's recurrence
     * pattern.
     * 
     * @param interval
     *            The time interval the events are occurring in.
     * @param targetTimeZone
     *            The target timezone.
     * @param 
     * @return The map of ICalEvents which override the base recurrences.
     */
    protected Map getOverrideEvents( Interval interval, DateTimeZone targetTimeZone, boolean useOverrideDate ) {
        Map events = new java.util.HashMap();

        if ( _overrides != null ) {
            Iterator i = _overrides.values().iterator();
            while ( i.hasNext() ) {
                OverrideEventDetails details = ( OverrideEventDetails ) i.next();
                List occurrences = details.getEvents( interval, targetTimeZone, useOverrideDate );

                Iterator j = occurrences.iterator();
                while ( j.hasNext() ) {
                    ICalEvent override = ( ICalEvent ) j.next();
                    events.put( details.getOverrideDate(), override );
                }
            }
        }

        return events;

    }

    /**
     * Returns a list of ICalEvents which override the base event's recurrence
     * pattern.
     * 
     * @param interval
     *            The time interval the events are occurring in.
     * @param targetTimeZone
     *            The target timezone.
     * @return The map of ICalEvents which override the base recurrences.
     */
    protected List getOverrideEvents() {
    	List events = new java.util.ArrayList();
        if ( _overrides != null ) {
            Iterator i = _overrides.values().iterator();
            while ( i.hasNext() ) {
                OverrideEventDetails details = ( OverrideEventDetails ) i.next();
                events.add(details);
            }
        }
        return events;
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
        Recur recur = getRecur();

        if ( recur != null ) {
            Set exclusions = getExcludedDates();
            Map overrides = getOverrideEvents( interval, targetTimeZone, false );
            Map baseOverrides = getOverrideEvents( interval, targetTimeZone, true);

            return processRecurrence( getStartDate(), interval, targetTimeZone, recur, exclusions, 
            		                                               overrides, baseOverrides, null );
        } else {
            Interval evtInt = getInterval( targetTimeZone );
            if ( evtInt != null && interval.overlaps( evtInt ) ) {
                return Collections.singletonList( new ICalEvent( this, getStartDate(), getTimeZone() ) );
            }

            return Collections.EMPTY_LIST;
        }
    }

    public void addOverride( OverrideEventDetails override ) {
        if ( _overrides == null )
            _overrides = new java.util.HashMap();

        _overrides.put( override.getOverrideDate(), override );
        override.setBaseEvent( this );
    }

    public void removeOverride( OverrideEventDetails override ) {
        if ( _overrides != null && 
        	 override !=  null &&
        	 _overrides.containsKey(override.getOverrideDate())) {
            _overrides.remove( override.getOverrideDate());
        }
    }

    public OverrideEventDetails getOverrideDetails( LocalDateTime overrideDate ) {
        if ( _overrides != null )
            return ( OverrideEventDetails ) _overrides.get( overrideDate );
        return null;
    }

    protected Recur getRecur() {
        RRule rrule = ( RRule ) getProperty( Property.RRULE );
        if ( rrule != null )
            return rrule.getRecur();
        return null;
    }
    
    public LocalDateTime getLastDate() {
    	Recur recur = getRecur();
    	if (recur != null) {
    	   Date untilDate = recur.getUntil();
    	   return untilDate == null ? null : ICalUtil.toJodaLocalDateTime(untilDate);
    	}
        return null;
    }
    
    public void setLastDate(LocalDateTime lastDate) {
    	LocalDateTime last = lastDate.withHourOfDay(23);
    	last = last.withMinuteOfHour(59);
    	setRepeat(getRepeat(),last);
    }
    
    public String getRepeat() {
    	Recur recur = getRecur();
    	if (recur != null) {
    		String frequency = recur.getFrequency();
    		if (frequency == null || frequency.length()==0) {
    		    return NO_REPEAT;
    	    }  else {
    	    	int interval = recur.getInterval();
    			NumberList yearDayList = recur.getYearDayList();
    			NumberList weekNoList = recur.getWeekNoList();     	
    			WeekDayList weekDayList = recur.getDayList(); 
    			NumberList monthDayList = recur.getMonthDayList();
    			NumberList monthList =	recur.getMonthList();
    			NumberList hourList = recur.getHourList();
    			NumberList minuteList =	recur.getMinuteList();
    			NumberList secondList = recur.getSecondList();
    			NumberList setPosList =	recur.getSetPosList();
    			if (yearDayList != null && !yearDayList.isEmpty() ||
    				(weekNoList != null && !weekNoList.isEmpty() && !frequency.equals(Recur.DAILY))||	
    				weekNoList != null && !weekNoList.isEmpty() ||
    				monthDayList != null && !monthDayList.isEmpty() ||
    				monthList != null && !monthList.isEmpty() ||
    				hourList != null && !hourList.isEmpty() ||
    				minuteList != null && !minuteList.isEmpty() ||
    				secondList != null && !secondList.isEmpty() || 
    				setPosList != null && !setPosList.isEmpty() ||
    				interval > 2 ||
    				(interval == 2 && !frequency.equals(Recur.WEEKLY))) {
    				return NOT_IMPLEMENTED;
    			} else if (frequency.equals(Recur.DAILY)) {
    		 		if (weekDayList != null && !weekDayList.isEmpty() &&
    		 			weekDayList.contains(WeekDay.MO) &&
    		 			weekDayList.contains(WeekDay.TU) &&
    		 			weekDayList.contains(WeekDay.WE) &&
    		 			weekDayList.contains(WeekDay.TH) &&
    		 			weekDayList.contains(WeekDay.FR) &&
    		 			!weekDayList.contains(WeekDay.SA) &&
    		 			!weekDayList.contains(WeekDay.SU)) {
    		 			return WEEKDAYS;
    		 		} else if (weekDayList == null || weekDayList.isEmpty()) {
    		 			return DAILY;
    		 		} else {
    		 			return NOT_IMPLEMENTED;
    		 		}
    		 	} else if (frequency.equals(Recur.WEEKLY) && interval >= 0 || interval <= 2) {
    		 		if (interval < 2) {
    		 			return WEEKLY;
    		 		} else if (interval == 2){
    		 			return BIWEEKLY;
    		 		} else {
    		 			return NOT_IMPLEMENTED;
    		 		}
    			} else if (frequency.equals(Recur.MONTHLY)) {
    				return MONTHLY;
    			} else if (frequency.equals(Recur.YEARLY)) {
    				return YEARLY;
    			}  else {
    				return NOT_IMPLEMENTED;
    			}
    	    }
    	} 
    	return NO_REPEAT;
    }
    
    /**
     * Set repeat and last date (Until) in RRULE
     * @param repeat
     * @param lastDate
     */
    public void setRepeat(String repeat, LocalDateTime lastDate) {
        checkReadOnly();
        RRule rrule = ( RRule ) getProperty( Property.RRULE );
        if ( repeat == null || repeat.equals(NO_REPEAT)) {
        	if (rrule != null) {
                vevent.getProperties().remove( rrule );
        	}
        } else if (repeat.equals(NOT_IMPLEMENTED)) {//Don't set rules if it hasn't been implemented
        	return;
        }
        else {
        	String frequency = null;
        	if (repeat.equals(DAILY) || repeat.equals(WEEKDAYS)) {
        		frequency = Recur.DAILY;
        	} else if (repeat.equals(WEEKLY) || repeat.equals(BIWEEKLY)) {
        		frequency = Recur.WEEKLY;
        	} else if (repeat.equals(MONTHLY)) {
        		frequency = Recur.MONTHLY;
        	} else if (repeat.equals(YEARLY)) {
        		frequency = Recur.YEARLY;
        	} else {//this shouldn't occur
        		return;
        	}
        	
        	Recur recur = new Recur(frequency,null);
         	if (repeat.equals(WEEKDAYS)) {
        		recur.getDayList().add(WeekDay.MO);
        		recur.getDayList().add(WeekDay.TU);
        		recur.getDayList().add(WeekDay.WE);
        		recur.getDayList().add(WeekDay.TH);
        		recur.getDayList().add(WeekDay.FR);
        		recur.setInterval(1);
        	}
        	if (repeat.equals(BIWEEKLY)) {
        		recur.setInterval(2);
        	}
        	Date last = lastDate == null ? null :
        		        ICalUtil.toICalDate( lastDate , getTimeZone());
        	recur.setUntil(last);
            RRule newRrule = new RRule(recur);
            if (rrule == null ||
            	!rrule.equals(newRrule)) {
            	if ( rrule != null ) {
            		vevent.getProperties().remove( rrule );
            	}
            	addProperty( newRrule );
            }
        }
    }
}
