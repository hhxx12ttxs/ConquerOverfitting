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
package com.atlassian.confluence.extra.calendar;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import com.atlassian.confluence.util.GeneralUtil;

/**
 * Provides utilities for Calendar classes.
 */
public final class CalendarUtils {
    public CalendarUtils() {
    }

    /**
     * Returns the date of the first day of the week the given date occurs in.
     * 
     * @param date
     *            The date.
     * @param firstDay
     *            The first day of the week.
     * @return the date of the first day.
     */
    public static LocalDate getFirstDayOfWeek( LocalDate date, int firstDay ) {
        int daysPerWeek = date.dayOfWeek().getMaximumValue();
        Period shift = Period.days( ( date.getDayOfWeek() + daysPerWeek - firstDay ) % daysPerWeek );
        return date.minus( shift );
    }

    /**
     * Returns the date of the last day of the week the given date occurs in.
     * This is the day before the first day of the next week.
     * 
     * @param date
     *            The date.
     * @param firstDay
     *            The first day of the week.
     * @return The date of the last day of the week.
     */
    public static LocalDate getLastDayOfWeek( LocalDate date, int firstDay ) {
        int daysPerWeek = date.dayOfWeek().getMaximumValue();
        Period shift = Period.days( daysPerWeek - 1 - ( date.getDayOfWeek() + daysPerWeek - firstDay )
                % daysPerWeek );
        return date.plus( shift );
    }

    public static LocalDate getFirstWeekOfYear( int year, int firstDay ) {
        LocalDate yearStart = new LocalDate( year, 1, 1 );
        final int daysPerWeek = yearStart.dayOfWeek().getMaximumValue();

        int diff = ( yearStart.getDayOfWeek() + daysPerWeek - firstDay ) % daysPerWeek;
        if ( diff > ( daysPerWeek / 2 ) )
            yearStart = yearStart.plus( Period.days( daysPerWeek - diff ) );
        else
            yearStart = yearStart.minus( Period.days( diff ) );

        return yearStart;
    }

    public static LocalDate getFirstWeekOfYear( LocalDate date, int firstDay ) {
        LocalDate weekStart = getFirstDayOfWeek( date, firstDay );
        LocalDate weekEnd = getLastDayOfWeek( date, firstDay );

        LocalDate yearStart;
        // Check if we're in the crossover week...
        if ( weekStart.getYear() != weekEnd.getYear() && weekEnd.getDayOfMonth() < 4 ) {
            yearStart = getFirstWeekOfYear( weekStart.getYear(), firstDay );
        } else {
            yearStart = getFirstWeekOfYear( weekEnd.getYear(), firstDay );
        }
        return yearStart;
    }

    public static int getWeekOfYear( LocalDate date, int firstDay ) {
        LocalDate yearStart = getFirstWeekOfYear( date, firstDay );
        return new Period( yearStart, date, PeriodType.weeks() ).getWeeks() + 1;
    }

    public static int getMaxWeekOfYear( LocalDate date, int firstDay ) {
        LocalDate yearStart = getFirstWeekOfYear( date, firstDay );
        return new Period( yearStart, new LocalDate( date.getYear(), 12, 31 ), PeriodType.weeks() ).getWeeks() + 1;
    }

    /**
     * Converts any new-line characters into &gt;br/&lt; tags.
     * 
     * @param value
     *            The value to convert
     * @return The converted value
     */
    public static String htmlLineEncode( String value ) {
        if ( value == null )
            return null;

        return GeneralUtil.htmlEncode( value ).replaceAll( "\r?\n", "<br/>" );
    }
}
