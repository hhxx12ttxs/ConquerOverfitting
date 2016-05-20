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

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.LocalDateTime;
import org.joda.time.ReadableInstant;

/**
 * Utility functions for converting between Joda Time and iCal4j time objects.
 * 
 * @author David Peterson
 */
public class ICalUtil {
    private static final long MILLIS_PER_SECOND = 1000;

    private static final long SECONDS_PER_MINUTE = 60;

    private static final long MINUTES_PER_HOUR = 60;

    private static final long HOURS_PER_DAY = 24;

    private static final long DAYS_PER_WEEK = 7;

    private static final long MILLIS_PER_MINUTE = SECONDS_PER_MINUTE * MILLIS_PER_SECOND;

    private static final long MILLIS_PER_HOUR = MINUTES_PER_HOUR * MILLIS_PER_MINUTE;

    private static final long MILLIS_PER_DAY = HOURS_PER_DAY * MILLIS_PER_HOUR;

    private static final long MILLIS_PER_WEEK = DAYS_PER_WEEK * MILLIS_PER_DAY;

    private static final String ETC_UTC = "Etc/UTC";

    private ICalUtil() {
    }

    public static boolean isICalDateTime( Date date ) {
        return date instanceof net.fortuna.ical4j.model.DateTime;
    }

    public static net.fortuna.ical4j.model.Date toICalDate( org.joda.time.ReadableInstant instant ) {
        if ( instant instanceof org.joda.time.DateTime )
            return toICalDateTime( ( org.joda.time.DateTime ) instant );
        else if ( instant instanceof org.joda.time.DateMidnight )
            return toICalDate( ( org.joda.time.DateMidnight ) instant );
        else if ( instant != null )
            return toICalDateTime( instant.toInstant().toDateTime() );

        return null;
    }

    private static net.fortuna.ical4j.model.DateTime toICalDateTime( org.joda.time.DateTime dateTime ) {
        if ( dateTime == null )
            return null;

        net.fortuna.ical4j.model.DateTime icalDateTime = new net.fortuna.ical4j.model.DateTime( dateTime
                .getMillis() );

        TimeZoneRegistry reg = TimeZoneRegistryFactory.getInstance().createRegistry();
        DateTimeZone zone = dateTime.getZone();
        if ( zone == null || isUtc( zone ) )
            icalDateTime.setUtc( true );
        else {
            TimeZone timeZone = reg.getTimeZone( zone.getID() );
            if ( timeZone != null )
                icalDateTime.setTimeZone( timeZone );
            else
                icalDateTime.setUtc( true );
        }

        return icalDateTime;
    }

    public static net.fortuna.ical4j.model.DateTime toICalDateTime( LocalDateTime localDateTime,
            DateTimeZone dateTimeZone ) {
        if ( localDateTime == null )
            return null;

        long millis = DateTimeUtils.getInstantMillis( localDateTime.toDateTime( dateTimeZone ) );
        net.fortuna.ical4j.model.DateTime icalDateTime = new net.fortuna.ical4j.model.DateTime( millis );

        if ( isUtc( dateTimeZone ) )
            icalDateTime.setUtc( true );
        else {
            TimeZone timeZone = toICalTimeZone( dateTimeZone );
            if ( timeZone != null )
                icalDateTime.setTimeZone( toICalTimeZone( dateTimeZone ) );
            else
                // Use UTC as the default.
                icalDateTime.setUtc( true );
        }

        return icalDateTime;
    }

    private static boolean isUtc( DateTimeZone dateTimeZone ) {
        return ( dateTimeZone != null && ETC_UTC.equals( dateTimeZone.getID() ) );
    }

    public static TimeZone toICalTimeZone( DateTimeZone dateTimeZone ) {
        if ( dateTimeZone == null )
            dateTimeZone = DateTimeZone.getDefault();

        TimeZoneRegistry reg = TimeZoneRegistryFactory.getInstance().createRegistry();

        return reg.getTimeZone( dateTimeZone.getID() );
    }

    /*
     * private static TimeZone createICalTimeZone(DateTimeZone dateTimeZone) {
     * PropertyList props = new PropertyList(); ComponentList comps = new
     * ComponentList();
     * 
     * Map observances = new java.util.HashMap();
     *  // Set the ID props.add(new TzId(dateTimeZone.getID()));
     *  // Add the initial standard zone, starting in 1800 for safety long
     * prevTransition = DateTimeUtils.getInstantMillis(new DateMidnight(1800, 1,
     * 1, DateTimeZone.UTC)); // Stop 10 years into the future, if it gets that
     * far. long cutoffInstant = DateTimeUtils.getInstantMillis(new
     * DateMidnight(DateTimeZone.UTC).plusYears(10)); long currentTransition;
     * 
     * int prevOffset = 0, currentOffset;
     * 
     * while ((currentTransition = dateTimeZone.nextTransition(prevTransition)) !=
     * prevTransition && currentTransition < cutoffInstant) { currentOffset =
     * addObservance(dateTimeZone, currentTransition, prevOffset, observances,
     * comps);
     * 
     * prevTransition = currentTransition; prevOffset = currentOffset; }
     * 
     * if (comps.size() == 0) // no transitions - add the current time/offset as
     * the default addObservance(dateTimeZone, prevTransition, prevOffset,
     * observances, comps);
     * 
     * 
     * VTimeZone vTimeZone = new VTimeZone(props, comps); return new
     * TimeZone(vTimeZone); }
     */

    /*
     * private static int addObservance(DateTimeZone dateTimeZone, long
     * currentTransition, int prevOffset, Map observances, ComponentList comps) {
     * int currentOffset; int stdOffset; currentOffset =
     * dateTimeZone.getOffset(currentTransition); stdOffset =
     * dateTimeZone.getStandardOffset(currentTransition);
     * 
     * UtcOffset offsetFrom = new UtcOffset(prevOffset); UtcOffset offsetTo =
     * new UtcOffset(currentOffset); String key = offsetFrom + ">" + offsetTo;
     * 
     * net.fortuna.ical4j.model.DateTime dtInstant = new
     * net.fortuna.ical4j.model.DateTime(currentTransition); Observance
     * observance = (Observance) observances.get(key);
     * 
     * if (observance == null) { if (currentOffset == stdOffset) observance =
     * new Standard(); else observance = new Daylight();
     * 
     * observance.getProperties().add(new TzOffsetFrom(offsetFrom));
     * observance.getProperties().add(new TzOffsetTo(offsetTo));
     * observance.getProperties().add(new DtStart(dtInstant));
     * observance.getProperties().add(new
     * TzName(dateTimeZone.getNameKey(currentTransition)));
     * 
     * observances.put(key, observance); comps.add(observance); }
     * 
     * RDate rDate = new RDate(); rDate.getDates().add(dtInstant);
     * observance.getProperties().add(rDate); return currentOffset; }
     */

    public static Date toICalDate( LocalDateTime localDateTime, DateTimeZone timeZone ) {
        if ( localDateTime == null )
            return null;

        if ( timeZone != null )
            return toICalDateTime( localDateTime, timeZone );

        return new Date( DateTimeUtils.getInstantMillis( localDateTime.toDateTime( DateTimeZone.UTC ) ) );
    }

    // private static org.joda.time.DateTime toJodaDateTime(Date date)
    // {
    // if (date == null)
    // return null;
    //
    // if (date instanceof net.fortuna.ical4j.model.DateTime)
    // return toJodaDateTime((net.fortuna.ical4j.model.DateTime) date);
    //
    // return toJodaDateMidnight(date).toDateTime();
    // }

    public static org.joda.time.ReadableInstant toJodaReadableInstant( net.fortuna.ical4j.model.Date date ) {
        if ( date instanceof net.fortuna.ical4j.model.DateTime )
            return toJodaDateTime( ( net.fortuna.ical4j.model.DateTime ) date );

        return toJodaDateMidnight( date );
    }

    private static org.joda.time.DateTime toJodaDateTime( net.fortuna.ical4j.model.DateTime dateTime ) {
        if ( dateTime == null )
            return null;

        DateTimeZone zone = getJodaDateTimeZone( dateTime );

        return new org.joda.time.DateTime( dateTime.getTime(), zone );
    }

    public static DateTimeZone getJodaDateTimeZone( Date date ) {
        if ( date instanceof net.fortuna.ical4j.model.DateTime )
            return getJodaDateTimeZone( ( net.fortuna.ical4j.model.DateTime ) date );

        return null;
    }

    public static DateTimeZone getJodaDateTimeZone( net.fortuna.ical4j.model.DateTime dateTime ) {
        TimeZone timeZone = dateTime.getTimeZone();
        DateTimeZone zone = null;

        if ( timeZone != null )
            zone = DateTimeZone.forTimeZone( timeZone );

        if ( zone == null )
            zone = DateTimeZone.UTC;

        return zone;
    }

    /**
     * Converts the specified date to an iCalendar-friendly date. This method
     * assumes that the desired iCalendar date is set to midnight, which means
     * converting it from whatever timezone the DateMidnight is in into UTC.
     * 
     * @param dateMidnight
     *            The date to convert
     * @return the iCalendar-friendly date.
     */
    private static Date toICalDate( DateMidnight dateMidnight ) {
        if ( dateMidnight == null )
            return null;

        // return new
        // Date(dateMidnight.withZoneRetainFields(DateTimeZone.UTC).getMillis());
        return new Date( dateMidnight.getMillis() );
    }

    /**
     * Returns a date midnight in the default timezone.
     * 
     * @param date
     *            The iCalendar date.
     * @return the DateMidnight instance
     */
    private static DateMidnight toJodaDateMidnight( Date date ) {
        if ( date == null )
            return null;

        // DateMidnight dateUtc = new DateMidnight(date.getTime(),
        // DateTimeZone.UTC);
        return new DateMidnight( date );
    }

    public static Dur toICalDur( Duration duration ) {
        if ( duration == null )
            return null;

        int days, hours, mins, secs;

        long time = duration.getMillis();
        time = time / MILLIS_PER_SECOND;
        secs = ( int ) ( time % SECONDS_PER_MINUTE );
        time = time / SECONDS_PER_MINUTE;
        mins = ( int ) ( time % MINUTES_PER_HOUR );
        time = time / MINUTES_PER_HOUR;
        hours = ( int ) ( time % HOURS_PER_DAY );
        time = time / HOURS_PER_DAY;
        days = ( int ) time;

        return new Dur( days, hours, mins, secs );
    }

    public static Duration toJodaDuration( Dur dur ) {
        if ( dur != null ) {
            long millis;

            if ( dur.getWeeks() > 0 ) {
                millis = dur.getWeeks() * MILLIS_PER_WEEK;
            } else {
                millis = dur.getDays() * MILLIS_PER_DAY + dur.getHours() * MILLIS_PER_HOUR + dur.getMinutes()
                        * MILLIS_PER_MINUTE + dur.getSeconds() * MILLIS_PER_SECOND;
            }

            return new Duration( millis );
        }
        return null;
    }

    public static org.joda.time.DateTime toJodaDateTime( ReadableInstant nextInstant ) {
        if ( nextInstant instanceof org.joda.time.DateTime )
            return ( org.joda.time.DateTime ) nextInstant;
        else if ( nextInstant instanceof org.joda.time.DateMidnight )
            return ( ( org.joda.time.DateMidnight ) nextInstant ).toDateTime();
        else if ( nextInstant != null )
            return nextInstant.toInstant().toDateTime();

        return null;
    }

    public static Instant toJodaInstant( Date date ) {
        if ( date != null )
            return new Instant( date );
        return null;
    }

    public static LocalDateTime toJodaLocalDateTime( Date date ) {
        return toJodaLocalDateTime( date, null );
    }

    public static LocalDateTime toJodaLocalDateTime( Date date, DateTimeZone targetTimeZone ) {
        if ( date instanceof net.fortuna.ical4j.model.DateTime )
            return toJodaLocalDateTime( ( net.fortuna.ical4j.model.DateTime ) date, targetTimeZone );

        return new LocalDateTime( date );
    }

    public static LocalDateTime toJodaLocalDateTime( net.fortuna.ical4j.model.DateTime dateTime ) {
        return toJodaLocalDateTime( dateTime, null );
    }

    public static LocalDateTime toJodaLocalDateTime( net.fortuna.ical4j.model.DateTime dateTime,
            DateTimeZone targetTimeZone ) {
        if ( targetTimeZone == null )
            targetTimeZone = getJodaDateTimeZone( dateTime );
        return new LocalDateTime( dateTime.getTime(), targetTimeZone );
    }
}

