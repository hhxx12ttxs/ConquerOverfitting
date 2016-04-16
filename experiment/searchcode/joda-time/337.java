/*
    Copyright (c) 1996-2011 Ariba, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

    $Id: //ariba/platform/util/core/ariba/util/formatter/DateFormatter.java#31 $
*/

package ariba.util.formatter;

import ariba.util.core.Assert;
import ariba.util.core.Constants;
import ariba.util.core.FastStringBuffer;
import ariba.util.core.Fmt;
import ariba.util.core.ResourceService;
import ariba.util.core.StringUtil;
import ariba.util.core.MultiKeyHashtable;
import ariba.util.core.ListUtil;
import ariba.util.i18n.LocaleSupport;
import ariba.util.log.Log;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.List;
import java.util.Date;

/**
    <code>DateFormatter</code> is a subclass of <code>Formatter</code> which
    is responsible for formatting, parsing, and comparing <code>Date</code>
    objects.

    @aribaapi documented
*/
public class DateFormatter extends Formatter
{
    /*-----------------------------------------------------------------------
        Constants
      -----------------------------------------------------------------------*/

    /**
        Our Java class name.

        @aribaapi private
    */
    public static final String ClassName = "ariba.util.formatter.DateFormatter";

    /**
        The string table for localized formats

        @aribaapi private
    */
    protected static final String FormatStringTable = "resource.date";

    /**
        The string table for localized strings

        @aribaapi private
    */
    protected static final String UnitsStringTable = "ariba.util.formatter";

    /**
        Localized resource key for the separator character used between date
        formats in the SystemFormats format list.

        @aribaapi private
    */
    protected static final String FormatsSepKey = "FormatsSeparator";

    /**
        Localized resource key for the list of date formats used when parsing
        date strings.

        @aribaapi private
    */
    protected static final String SystemFormatsKey = "SystemFormats";

    /*-----------------------------------------------------------------------
        Localized Date Formats
      -----------------------------------------------------------------------*/

    /**
        localized resource string tag for the default date format pattern.
        See resource.date.csv.
        @aribaapi documented
    */
    public static final String DefaultFormatKey = "DefaultFmt";

    /**
        Format key for unpadded concise date format strings. See resource.date.csv.
        @aribaapi documented
    */
    public static final String ConciseDateFormatKey       = "ConciseFmt";
    /**
        Format key for padded concise date format strings. See resource.date.csv.
        @aribaapi documented
    */
    public static final String PaddedConciseDateFormatKey = "PaddedConciseFmt";

    /**
        Format key for unpadded concise date/time format strings. See resource.date.csv.
        @aribaapi documented
    */
    public static final String ConciseDateTimeFormatKey =
        "ConciseDateTimeFmt";
    /**
        Format key for padded concise date/time format strings. See resource.date.csv.
        @aribaapi documented
    */
    public static final String PaddedConciseDateTimeFormatKey =
        "PaddedConciseDateTimeFmt";

    /**
        Format key for padded concise date/time format strings. See resource.date.csv.
        @aribaapi documented
    */
    public static final String PaddedDateTimeFormatKey =
        "PaddedDateTimeFmt";

    /**
        Format key for medium length date format strings (for printing). See resource.date.csv.
        @aribaapi documented
    */
    public static final String DayDateMonthYearFormatKey  =
        "DayDateMonthYearFmt";
    /**
        Format key for medium length date format strings (for printing). See resource.date.csv.
        @aribaapi documented
    */
    public static final String DateMonthYearFormatKey     =
        "DateMonthYearFmt";
    /**
        Format key for date format strings (for printing). See resource.date.csv.
        @aribaapi documented
    */
    public static final String FullDateMonthYearFormatKey =
        "FullDateMonthYearFmt";

    /**
        Format key for date format strings with time zone(for printing). See resource.date.csv.
        @aribaapi documented
    */
    public static final String FullDateMonthYearTZFormatKey =
        "FullDateMonthYearTZFmt";

    /**
        Format key for long date format strings.  See resource.date.csv.
        @aribaapi documented
    */
    public static final String LongDayDateMonthYearFormatKey =
        "LongDayDateMonthYearFmt";
    /**
        Format key for full date format strings.  See resource.date.csv.
        @aribaapi documented
    */
    public static final String FullDayDateMonthYearFormatKey =
        "FullDayDateMonthYearFmt";

    /**
        Format key for full date format strings with time zone.  See resource.date.csv.
        @aribaapi documented
    */
    public static final String FullDayDateMonthYearTZFormatKey =
        "FullDayDateMonthYearTZFmt";

    // tag for date format string for hours, minutes, and seconds
    private static final String ShortTimeFormatKey =
        "HourMinuteAMPM";

    // tag for date format string for hours, minutes, and seconds
    private static final String HoursMinutesSecondsFormatKey =
        "HoursMinutesSecondsFmt";

        // tag for date format string for a short day of the week string
    private static final String DayOfWeekFormatKey = "DayOfWeekFmt";

    private static final String MilliSecondKey = "MilliSecond";
    private static final String MilliSecondsKey =  "MilliSeconds";
    private static final String SecondKey = "Second";
    private static final String SecondsKey = "Seconds";
    private static final String MinuteKey = "Minute";
    private static final String MinutesKey = "Minutes";
    private static final String HourKey = "Hour";
    private static final String HoursKey = "Hours";
    private static final String DayKey = "Day";
    private static final String DaysKey = "Days";

    private static final String JavaFormatKey = "JavaFormatKey";

    public static final String CannotParseDateKey = "CannotParseDate";
    private static final String InvalidYearKey = "InvalidYear";

    /*-----------------------------------------------------------------------
        Non-Localized Date Formats
      -----------------------------------------------------------------------*/

    /*
        The date format strings below should not be localized.  They are used
        only for internal formatting for things like storage in a database,
        reporting, etc.  Users should never see dates in these formats.
    */

        // date format using a 24 hour clock used by reporting
    private static final String MilitaryTimeFormat = "HH:mm:ss";

        // standard format for displaying date in GMT
    private static final String GMTFormat = "d MMM yyyy HH:mm:ss 'GMT'";

        // date format string showing time (for use as a filename suffix)
    private static final String FileTimeFormat = "hh_mm_ssa";

        // date format string for p-card statements
    private static final String YearMonthDateFormat = "yyyyMMdd";

        // date format string in UTC (Coordinated Universal Time). One of the
        // uses of this format is in digital signature.
    private static final String UTCFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";

        // date format string for the first part of cXML date
    private static final String cXMLFormat = "yyyy-MM-dd'T'HH:mm:ss";

        // date format used by java.util.Date.toString
    private static final String JavaFormat = "EEE MMM dd HH:mm:ss zzz yyyy";

        // parseDate() rejects year < 100
    private static final int InvalidYearThreshold = 100;

        // Cache of date format patterns
    private static final MultiKeyHashtable Patterns = new MultiKeyHashtable(2);

    /*-----------------------------------------------------------------------
        Constructor
      -----------------------------------------------------------------------*/

    /**
        Creates a new <code>DateFormatter</code>.

        @aribaapi private
    */
    public DateFormatter ()
    {
    }


    /*-----------------------------------------------------------------------
        Static Fields
      -----------------------------------------------------------------------*/

    private static final DateFormatter factory = new DateFormatter();

    /*-----------------------------------------------------------------------
        Static Formatting
      -----------------------------------------------------------------------*/

    /**
        Returns a formatted string for the given <code>Date</code> in the
        default locale and time zone.  Uses a default date format string,
        e.g. "Wed, 2 Dec 98".  The exact format is locale-specific.

        @param  date the <code>Date</code> to format into a string
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String getStringValue (Date date)
    {
        return getStringValue(date, getDefaultLocale());
    }

    /**
        Returns a formatted string for the given <code>Date</code> in the
        given locale using the default time zone.  Uses a default date format
        string, e.g. "Wed, 2 Dec 98".  The exact format is locale-specific.
        The <code>locale</code> parameter must be non-null.

        @param  date   the <code>Date</code> to format into a string
        @param  locale the <code>Locale</code> to use for formatting
        @return        a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String getStringValue (Date date, Locale locale)
    {
        return getStringValue(date, locale, getDefaultTimeZone());
    }

    /**
        Returns a formatted string for the given <code>Date</code> in the
        default locale using the given <code>timeZone</code>.  Uses a default
        date format string, e.g. "Wed, 2 Dec 98".  The exact format is
        locale-specific.  The <code>timeZone</code> parameter must be
        non-null.

        @param  date     the <code>Date</code> to format into a string
        @param  timeZone the <code>TimeZone</code> to use for formatting
        @return          a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String getStringValue (Date date, TimeZone timeZone)
    {
        return getStringValue(date, getDefaultLocale(), timeZone);
    }

    /**
        Returns a formatted string for the given <code>Date</code> in the
        given locale and time zone.  Uses a default date format string,
        e.g. "Wed, 2 Dec 98".  The exact format is locale-specific.  The
        <code>locale</code> and <code>timeZone</code> parameters must be
        non-null.

        @param  date     the <code>Date</code> to format into a string
        @param  locale   the <code>Locale</code> to use for formatting
        @param  timeZone the <code>TimeZone</code> to use for formatting
        @return          a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String getStringValue (Date date, Locale locale, TimeZone timeZone)
    {
        String pattern = lookupLocalizedFormat(DefaultFormatKey, locale);
        return getStringValue(date, pattern, locale, timeZone);
    }

    /**
        Returns a formatted string for the given <code>Date</code> in the
        default locale and time zone.  Uses the date format pattern
        <code>pattern</code> as the template for how the date should be
        formatted.

        @param  date    the <code>Date</code> to format into a string
        @param  pattern the locale-specific date format pattern to use for formatting
        @return         a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String getStringValue (Date date, String pattern)
    {
        return getStringValue(date, pattern, (String)null);
    }

    /**
        Returns a formatted string for the given <code>Date</code> in the
        given locale using the default time zone.  Uses the date format
        pattern <code>pattern</code> as the template for how the date should
        be formatted.

        @param  date    the <code>Date</code> to format into a string
        @param  pattern the locale-specific date format pattern to use for formatting
        @param  locale  the <code>Locale</code> to use for formatting
        @return         a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String getStringValue (Date   date,
                                         String pattern,
                                         Locale locale)
    {
        return getStringValue(date, pattern, locale, getDefaultTimeZone());
    }

    /**
        Returns a formatted string for the given <code>Date</code> in the
        default locale.  Uses the date format pattern <code>pattern</code> as
        the template for how the date should be formatted.  Looks up a
        <code>TimeZone</code> based on the given <code>timeZone</code> string
        (e.g. "GMT", "PST", etc.) and uses it to format the date in the given
        time zone.

        @param  date     the <code>Date</code> to format into a string
        @param  pattern  the locale-specific date format pattern to use for formatting
        @param  tzString the identifier of the time zone to use for formatting
        @return          a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String getStringValue (Date   date,
                                         String pattern,
                                         String tzString)
    {
        boolean  empty = StringUtil.nullOrEmptyString(tzString);
        TimeZone tz = (empty ?
                       getDefaultTimeZone() :
                       TimeZone.getTimeZone(tzString));

        return getStringValue(date, pattern, getDefaultLocale(), tz);
    }

    /**
        Returns a formatted string for the given <code>Date</code> in the
        default locale.  Uses the date format pattern <code>pattern</code> as
        the template for how the date should be formatted.  Uses the given
        <code>timeZone</code> to format the date.

        @param  date     the <code>Date</code> to format into a string
        @param  pattern  the date format pattern to use for formatting
        @param  timeZone the <code>TimeZone</code> to use for formatting
        @return          a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String getStringValue (Date     date,
                                         String   pattern,
                                         TimeZone timeZone)
    {
        return getStringValue(date, pattern, getDefaultLocale(), timeZone);
    }

    static boolean calendarDate (Date date)
    {
        return (date instanceof ariba.util.core.Date) && ((ariba.util.core.Date)date).calendarDate();
    }

    /**
        Returns a formatted string for the given <code>Date</code> in the
        given locale.  Uses the date format pattern <code>pattern</code> as
        the template for how the date should be formatted.  Looks up a
        <code>TimeZone</code> based on the given <code>timeZone</code> string
        (e.g. "GMT", "PST", etc.) and uses it to format the date in the given
        time zone.  The <code>locale</code> parameter must be non-null.

        @param  date     the <code>Date</code> to format into a string
        @param  pattern  the date format pattern to use for formatting
        @param  locale   the <code>Locale</code> to use for formatting
        @param  timeZone the <code>TimeZone</code> to use for formatting
        @return          a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String getStringValue (Date     date,
                                         String   pattern,
                                         Locale   locale,
                                         TimeZone timeZone)
    {

            // short circuit
        if (date == null) {
            return "";
        }
        // get a date format instance for the given pattern and locale
        Assert.that(locale != null, "invalid null Locale");
        SimpleDateFormat fmt = null;
        try
        {
            fmt = acquireSimpleDateFormat(locale,pattern, calendarDate(date));
            // honor time zone for non-calendar dates
            if (!calendarDate(date)) {
                Assert.that(timeZone != null, "invalid null TimeZone");
                fmt.setTimeZone(timeZone);
            }
            return fmt.format(date);
        }
        finally
        {
            releaseSimpleDateFormat(fmt,locale,pattern, calendarDate(date));
        }
    }

    public String getDateString (Date date, Locale locale, TimeZone timeZone)
    {
        String pattern = lookupLocalizedFormat(DefaultFormatKey, locale);
        return getDateString(date, pattern, locale, timeZone);
    }

    public String getDateString (Date date,
                                 String   pattern,
                                 Locale   locale,
                                 TimeZone timeZone)
    {
        return getStringValue(date, pattern, locale, timeZone);
    }

    /**
        Returns a formatted string for the given <code>Date</code> in the
        given locale.  Uses the formats key <code>formatsKey</code> to look up
        the template for how the date should be formatted.  Looks up a
        <code>TimeZone</code> based on the given <code>timeZone</code> string
        (e.g. "GMT", "PST", etc.) and uses it to format the date in the given
        time zone.

        @param  date     the <code>Date</code> to format into a string
        @param  formatsKey  key to the date format pattern, must not be null.
        @param  locale   the <code>Locale</code> to use for formatting, must not be null.
        @param  timeZone the <code>TimeZone</code> to use for formatting, must not be null.
        @return          a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String getStringValueUsingFormats (Date     date,
                                                     String   formatsKey,
                                                     Locale   locale,
                                                     TimeZone timeZone)
    {
        String pattern = lookupLocalizedFormat(formatsKey, locale);
        return getStringValue(date, pattern,  locale,  timeZone);
    }

    /**
        Formats the given <code>millis</code> as "D days, H hours, M
        minutes".  If <code>millis</code> is less than the number of
        milliseconds in a minute, then the format is "S seconds".  If it's
        less than the number of millis in a second, then we use "M
        milliseconds".

        @aribaapi private
    */
    public static String formatMillisAsDuration (long millis)
    {
        String result;
        if (millis < ariba.util.core.Date.MillisPerMinute) {
            int seconds = (int)(millis/ariba.util.core.Date.MillisPerSecond);
            if (seconds == 0) {
                result = toUnits(millis,
                                 ((millis == 1) ?
                                  MilliSecondKey :
                                  MilliSecondsKey));
            }
            else {
                result = toUnits(seconds,
                                 (seconds == 1) ? SecondKey : SecondsKey);
            }
        }
        else {
            FastStringBuffer fsb = new FastStringBuffer();
            int days = (int)(millis / ariba.util.core.Date.MillisPerDay);
            millis -= days * ariba.util.core.Date.MillisPerDay;
            int hours = (int)(millis / ariba.util.core.Date.MillisPerHour);
            millis -= hours * ariba.util.core.Date.MillisPerHour;
            int minutes = (int)(millis / ariba.util.core.Date.MillisPerMinute);
            if (days > 0) {
                fsb.append(toUnits(days,
                                   (days == 1) ? DayKey : DaysKey));
                if (hours > 0 || minutes > 0) {
                    fsb.append(", ");
                }
            }
            if (hours > 0) {
                fsb.append(toUnits(hours,
                                   (hours == 1) ? HourKey : HoursKey));
                if (minutes > 0) {
                    fsb.append(", ");
                }
            }
            if (minutes > 0) {
                fsb.append(toUnits(minutes,
                                   (minutes == 1) ? MinuteKey : MinutesKey));
            }
            result = fsb.toString();
        }
        return result;
    }

    private static String toUnits (long N, String units)
    {
        return Fmt.S("%s %s",
                     Constants.getLong(N),
                     ResourceService.getString(UnitsStringTable, units));
    }


    /*-----------------------------------------------------------------------
        Localized Date Formatting
      -----------------------------------------------------------------------*/

    /**
        Returns a string representation of the date portion of the given
        <code>Date</code> in a concise, unpadded format, e.g. "1/12/1999".
        The exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toConciseDateString (Date date)
    {
        return toConciseDateString(date, getDefaultLocale());
    }
    /**
        Returns a string representation of the date portion of the given
        <code>Date</code> in a concise, unpadded format, e.g. "1/12/1999".
        The exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @param  locale the <code>Locale</code> to use for formatting
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toConciseDateString (Date date, Locale locale)
    {
        return toConciseDateString(date, locale, getDefaultTimeZone());
    }

    /**
        Returns a string representation of the date portion of the given
        <code>Date</code> in a concise, unpadded format, e.g. "1/12/1999".
        The exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @param  locale the <code>Locale</code> to use for formatting
        @param tz the <code>TimeZone,</code> to use for formatting
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toConciseDateString (Date date, Locale locale, TimeZone tz)
    {
        String pattern = lookupLocalizedFormat(ConciseDateFormatKey, locale);
        return getStringValue(date, pattern, locale, tz);
    }

    /**
        Returns a string representation of the date portion of the given
        <code>Date</code> in a concise, padded format, e.g. "01/12/1999".  The
        exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toPaddedConciseDateString (Date date)
    {
        return toPaddedConciseDateString(date, getDefaultLocale());
    }
    /**
        Returns a string representation of the date portion of the given
        <code>Date</code> in a concise, padded format, e.g. "01/12/1999".  The
        exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @param  locale the <code>Locale</code> to use for formatting
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toPaddedConciseDateString (Date date, Locale locale)
    {
        return toPaddedConciseDateString(date, locale, getDefaultTimeZone());
    }

    /**
        Returns a string representation of the date portion of the given
        <code>Date</code> in a concise, padded format, e.g. "01/12/1999".  The
        exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @param  locale the <code>Locale</code> to use for formatting
        @param tz the <code>TimeZone,</code> to use for formatting
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toPaddedConciseDateString (Date date, Locale locale, TimeZone tz)
    {
        String pattern = lookupLocalizedFormat(PaddedConciseDateFormatKey, locale);
        return getStringValue(date, pattern, locale, tz);
    }

    /**
        Returns a string representation of the date and time for the given
        <code>Date</code> in a concise, unpadded format, e.g.  "1/12/1999 3:14
        PM".  The exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toConciseDateTimeString (Date date)
    {
        return toConciseDateTimeString(date, getDefaultLocale());
    }

    /**
        Returns a string representation of the date and time for the given
        <code>Date</code> in a concise, unpadded format, e.g.  "1/12/1999 3:14
        PM".  The exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @param  locale the <code>Locale</code> to use for formatting
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toConciseDateTimeString (Date date, Locale locale)
    {
        String pattern = lookupLocalizedFormat(ConciseDateTimeFormatKey, locale);
        return getStringValue(date, pattern, locale);
    }

    /**
        Returns a string representation of the date and time for the given
        <code>Date</code> in a concise, unpadded format, e.g.  "1/12/1999 3:14
        PM".  The exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @param  locale the <code>Locale</code> to use for formatting
        @param  timeZone the <code>TimeZone,</code> to use for formatting
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toConciseDateTimeString (Date date, Locale locale,
                                                  TimeZone timeZone)
    {
        String pattern = lookupLocalizedFormat(ConciseDateTimeFormatKey, locale);
        return getStringValue(date, pattern, locale, timeZone);
    }

    /**
        Returns a string representation of the date and time for the given
        <code>Date</code> in a concise, padded format, e.g.  "01/12/1999 03:14
        PM".  The exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toPaddedConciseDateTimeString (Date date)
    {
        return toPaddedConciseDateTimeString(date, getDefaultLocale());
    }

    /**
        Returns a string representation of the date and time for the given
        <code>Date</code> in a concise, padded format, e.g.  "01/12/1999 03:14
        PM".  The exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @param  locale the <code>Locale</code> to use for formatting
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toPaddedConciseDateTimeString (Date date, Locale locale)
    {
        String pattern = lookupLocalizedFormat(PaddedConciseDateTimeFormatKey, locale);
        return getStringValue(date, pattern, locale);
    }

    /**
        Returns a string representation of the date and time for the given
        <code>Date</code> in a concise, padded format, e.g.  "01/12/1999 03:14
        PM".  The exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @param  locale the <code>Locale</code> to use for formatting
        @param  timeZone the <code>TimeZone,</code> to use for formatting
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toPaddedConciseDateTimeString (Date date, Locale locale,
                                                        TimeZone timeZone)
    {
        String pattern = lookupLocalizedFormat(PaddedConciseDateTimeFormatKey, locale);
        return getStringValue(date, pattern, locale, timeZone);

    }


    /**
        Returns a string representation of the date and time for the given
        <code>DateTime</code> in a padded format, e.g.  "01/12/1999 03:14:34
        PM".  The exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toPaddedDateTimeString (Date date)
    {
        return toPaddedDateTimeString(date, getDefaultLocale());
    }

    /**
        Returns a string representation of the date and time for the given
        <code>DateTime</code> in a padded format, e.g.  "01/12/1999 03:14:34
        PM".  The exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @param  locale the <code>Locale</code> to use for formatting
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toPaddedDateTimeString (Date date, Locale locale)
    {
        String pattern = lookupLocalizedFormat(PaddedDateTimeFormatKey, locale);
        return getStringValue(date, pattern, locale);
    }

    /**
        Returns a string representation of the date and time for the given
        <code>DateTime</code> in a padded format, e.g.  "01/12/1999 03:14:34
        PM".  The exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @param  locale the <code>Locale</code> to use for formatting
        @param  timeZone the <code>TimeZone,</code> to use for formatting
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toPaddedDateTimeString (Date date, Locale locale,
                                                        TimeZone timeZone)
    {
        String pattern = lookupLocalizedFormat(PaddedDateTimeFormatKey, locale);
        return getStringValue(date, pattern, locale, timeZone);

    }

    /**
        Returns a string representation of the date portion of the given
        <code>Date</code> which includes an abbreviated day of week, day of
        month, month, and year, e.g. "Mon, 02 Jun, 1999".  The exact format is
        locale-specific.

        @param  date the <code>Date</code> to format
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toDayDateMonthYearString (Date date)
    {
        return toDayDateMonthYearString(date, getDefaultLocale());
    }

    /**
        Returns a string representation of the date portion of the given
        <code>Date</code> which includes an abbreviated day of week, day of
        month, month, and year, e.g. "Mon, 02 Jun, 1999".  The exact format is
        locale-specific.

        @param  date the <code>Date</code> to format
        @param  locale the <code>Locale</code> to use for formatting
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toDayDateMonthYearString (Date date, Locale locale)
    {
        String pattern = lookupLocalizedFormat(DayDateMonthYearFormatKey, locale);
        return getStringValue(date, pattern, locale);
    }

    /**
     *  Returns a string representation of the date portion of the given
        <code>Date</code> which includes an abbreviated day of week, day of
        month, month, and year, e.g. "Mon, 02 Jun, 1999".  The exact format is
        locale-specific.
     * @param date     date the <code>Date</code> to format
     * @param locale   locale the <code>Locale</code> to use for formatting
     * @param tz   Time zone.
     * @return     a string representation of the <code>Date</code>
     */
    public static String toDayDateMonthYearString (Date date, Locale locale, TimeZone tz)
    {
        String pattern = lookupLocalizedFormat(DayDateMonthYearFormatKey, locale);
        return getStringValue(date, pattern, locale, tz);
    }

    /**
        Returns a string representation of the date portion of the given
        <code>Date</code> which includes the full day of week, month, day of
        month and year, e.g. "Monday, January 31, 1999".  The exact format is
        locale-specific.

        @param  date the <code>Date</code> to format
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toLongDayDateMonthYearString (Date date)
    {
        return toLongDayDateMonthYearString(date, getDefaultLocale());
    }

    /**
        Returns a string representation of the date portion of the given
        <code>Date</code> which includes the full day of week, month, day of
        month and year, e.g. "Monday, January 31, 1999".  The exact format is
        locale-specific.

        @param  date the <code>Date</code> to format
        @param  locale the <code>Locale</code> to use for formatting
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toLongDayDateMonthYearString (Date date, Locale locale)
    {
        String pattern = lookupLocalizedFormat(LongDayDateMonthYearFormatKey, locale);
        return getStringValue(date, pattern, locale);
    }

    /**
        Returns a string representation of the given <code>Date</code>
        (including time) which includes the full day of week, month, day of
        month, year and time e.g. "Monday, January 31, 1999 at 3:15 PM".  The
        exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toFullDayDateMonthYearString (Date date)
    {
        return toFullDayDateMonthYearString(date, getDefaultLocale());
    }

    /**
        Returns a string representation of the given <code>Date</code>
        (including time) which includes the full day of week, month, day of
        month, year and time e.g. "Monday, January 31, 1999 at 3:15 PM".  The
        exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @param  locale the <code>Locale</code> to use for formatting
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toFullDayDateMonthYearString (Date date, Locale locale)
    {
        return toFullDayDateMonthYearString(date, locale, getDefaultTimeZone());
    }

    /**
        Returns a string representation of the given <code>Date</code>
        (including time) which includes the full day of week, month, day of
        month, year and time e.g. "Monday, January 31, 1999 at 3:15 PM".  The
        exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @param  locale the <code>Locale</code> to use for formatting
        @param  tz the <code>TimeZone</code> to use for formatting
        @return      a string representation of the <code>Date</code>
    */
    public static String toFullDayDateMonthYearString (Date date,
                                                Locale locale, TimeZone tz)
    {
        String pattern = lookupLocalizedFormat(FullDayDateMonthYearFormatKey, locale);
        return getStringValue(date, pattern, locale, tz);
    }

    /**
        Returns a string representation of the given <code>Date</code>
        (including time) which includes the full day of week, month, day of
        month, year and time e.g. "Monday, January 31, 1999 at 3:15 PM,
        Pacific Daylight Time".  The exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @param  locale the <code>Locale</code> to use for formatting
        @param  tz the <code>TimeZone</code> to use for formatting
        @return      a string representation of the <code>Date</code>
    */
    public static String toFullDayDateMonthYearTZString (Date date,
                                                Locale locale, TimeZone tz)
    {
        String pattern = lookupLocalizedFormat(FullDayDateMonthYearTZFormatKey, locale);
        return getStringValue(date, pattern, locale, tz);
    }

    /**
        Returns a string representation of the date portion of the given
        <code>Date</code> which includes the full month, day of month and
        year, e.g. "January 31, 1999".  The exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toDateMonthYearString (Date date)
    {
        return toDateMonthYearString(date, getDefaultLocale());
    }

    /**
        Returns a string representation of the date portion of the given
        <code>Date</code> which includes the full month, day of month and
        year, e.g. "January 31, 1999".  The exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @param  locale the <code>Locale</code> to use for formatting
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toDateMonthYearString (Date date, Locale locale)
    {
        return toDateMonthYearString(date, locale, getDefaultTimeZone());
    }

    public static String toDateMonthYearString (Date date, Locale locale,
                                                TimeZone timezone)
    {
        String pattern = lookupLocalizedFormat(DateMonthYearFormatKey, locale);
        return getStringValue(date, pattern, locale, timezone);
    }

    /**
        Returns a string representation of the given <code>Date</code> which
        includes the full month, day of month, year and time, e.g. "January
        31, 1999 at 3:15 PM".  The exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toFullDateMonthYearString (Date date)
    {
        return toFullDateMonthYearString(date, getDefaultLocale());
    }

    /**
        Returns a string representation of the given <code>Date</code> which
        includes the full month, day of month, year and time, e.g. "January
        31, 1999 at 3:15 PM".  The exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @param  locale the <code>Locale</code> to use for formatting
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toFullDateMonthYearString (Date date, Locale locale)
    {
        String pattern = lookupLocalizedFormat(FullDateMonthYearFormatKey, locale);
        return getStringValue(date, pattern, locale);
    }

    /**
        Returns a string representation of the given <code>Date</code> which
        includes the full month, day of month, year, time and time zone, e.g. "January
        31, 1999 at 3:15 PM Pacific Daylight Time".  The exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @param  locale the <code>Locale</code> to use for formatting
        @param  tz the <code>TimeZone</code> to use for formatting
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toFullDateMonthYearTZString (Date date,
                                                      Locale locale,
                                                      TimeZone tz)
    {
        String pattern = lookupLocalizedFormat(FullDateMonthYearTZFormatKey, locale);
        return getStringValue(date, pattern, locale, tz);
    }

    /**
        Returns a string representation of the time portion of the given
        <code>Date</code> which includes hours, minutes and seconds, e.g.
        "3:01:42".  The exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toHourMinSecString (Date date)
    {
        return toHourMinSecString(date, getDefaultLocale());
    }

    /**
        Returns a string representation of the time portion of the given
        <code>Date</code> which includes hours, minutes and seconds, e.g.
        "3:01:42".  The exact format is locale-specific.

        @param  date the <code>Date</code> to format
        @param  locale the <code>Locale</code> to use for formatting
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toHourMinSecString (Date date, Locale locale)
    {
        String pattern = lookupLocalizedFormat(HoursMinutesSecondsFormatKey, locale);
        return getStringValue(date, pattern, locale);
    }

    /**
        Returns an abbreviated string representation of the day of the week
        represented by the given <code>Date</code>, e.g. "Mon", "Tue", etc.

        @param  date the <code>Date</code> to format
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toDayOfWeekString (Date date)
    {
        return toDayOfWeekString(date, getDefaultLocale());
    }

    /**
        Returns an abbreviated string representation of the day of the week
        represented by the given <code>Date</code>, e.g. "Mon", "Tue", etc.

        @param  date the <code>Date</code> to format
        @param  locale the <code>Locale</code> to use for formatting
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toDayOfWeekString (Date date, Locale locale)
    {
        String pattern = lookupLocalizedFormat(DayOfWeekFormatKey, locale);
        return getStringValue(date, pattern, locale);
    }


    /*-----------------------------------------------------------------------
        Non-Localized Date Formatting
      -----------------------------------------------------------------------*/

    /**
        Returns a string representing the given <code>Date</code> in a
        standard GMT format, e.g. "12 Aug 1995 02:30:00 GMT".  This method
        should be used in place of the deprecated <code>toGMTString</code>.
        This format is not locale-specific.

        @param  date the <code>Date</code> to format
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toGMT (Date date)
    {
        return getStringValue(date, GMTFormat, "GMT");
    }

    /**
        Returns a string representation of the given <code>Date</code> in a
        compact format with year, month, and day of month run together, e.g.
        "19990521".  This format is not locale-specific.

        @param  date the <code>Date</code> to format
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toYearMonthDate (Date date)
    {
        return getStringValue(date, YearMonthDateFormat);
    }

    /**
        Returns a string representation of the time portion of the given
        <code>Date</code> in military time format, e.g. "21:05:35".  This
        format is not locale-specific.

        @param  date the <code>Date</code> to format
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toMilitaryTimeString (Date date)
    {
        return getStringValue(date, MilitaryTimeFormat);
    }

    /**
        @aribaapi private
    */
    public static String toFileTimeString (Date date)
    {
        return getStringValue(date, FileTimeFormat);
    }

    /**
        @aribaapi private
    */
    public static String toJavaFormat (Date date)
    {
            // Originally, this code calls the other toJavaFormat
            // (below) passing in the date and default locale. But
            // this causes an infinite loop (stack overflow) problem
            // leading to D55672 and D58777. Specifically, it tries to
            // invoke lookupLocalizedString, thus going through the
            // Resource service, which calls the Log manager to log a
            // message. If the Log manager is not initialized at that
            // time, it will try to the localize the date. The
            // Date.toString method calls toJavaFormat leading to an
            // infinite loop.
            //
            // Apparently, the localization group made this change to
            // fix a certain defect. Checked with them, and they will
            // be fixing their defect using another way. So restoring
            // the implementation to calling getStringValue. Still
            // leave the other toJavaFormat method alone for
            // compability reason of the util package.
        return getStringValue(date, JavaFormat);
    }

    /**
        @aribaapi private
    */
    public static String toJavaFormat (Date date, Locale locale)
    {
        // "Java Format" is NOT locale independet.
        if (locale.equals(Locale.US)) {
            return getStringValue(date, JavaFormat, locale);
        }
        String timeString = null;
        String format = lookupLocalizedFormat(JavaFormatKey, locale, false);
        // ResourceService#getLocalizedString returns "key" string as format
        // if specified key is not found.
        if ((!StringUtil.nullOrEmptyString(format)) &&
            (!format.equalsIgnoreCase(JavaFormatKey))) {
            timeString = getStringValue(date, format);
        }
        else {
            timeString = getStringValue(date, JavaFormat, locale);
        }
        return timeString;
    }

    /**
        Returns a string representing the given <code>Date</code> in the
        Coordinated Universal Time (UTC) as specified in
        http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#dateTime,
        e.g. "2003-08-30T03:01:39Z". This format is not locale-specific.
        @param  date the <code>Date</code> to format
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String toUTC (Date date)
    {
        return getStringValue(date, UTCFormat, getDefaultLocale(),
                              TimeZone.getTimeZone("GMT"));
    }

    /*-----------------------------------------------------------------------
        CXML Date Formatting
      -----------------------------------------------------------------------*/
    /**
        Returns a string representing the given <code>Date</code> in a
        standard cXML format, e.g. "1999-03-12T18:39:09-08:00".  The last
        six digits represent difference between local time and GMT time.
        This format is not locale-specific.

        @param  date the <code>Date</code> to format
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String tocXML (Date date)
    {
        return tocXML(date, null);
    }

    /**
        Returns a string representing the given <code>Date</code> in a
        standard cXML format, e.g. "1999-03-12T18:39:09-08:00".  The last
        six digits represent difference between local time and GMT time.
        This format is not locale-specific.

        @param  date the <code>Date</code> to format
        @param timezone the <code>TimeZone</code> to used. If null,
        DateFormatter.getDefaultTimeZone() will be used.
        @return      a string representation of the <code>Date</code>
        @aribaapi documented
    */
    public static String tocXML (Date date, TimeZone timezone)
    {
        TimeZone tz = (timezone == null ?
                       getDefaultTimeZone() :
                       timezone);

        String strDate = getStringValue(date, cXMLFormat, getDefaultLocale(), tz);

        // append the GMT difference
        boolean isNegative = false;
        int     gmtDiff    = ariba.util.core.Date.timezoneOffset(date, tz);
        if (gmtDiff < 0) {
            gmtDiff = -gmtDiff;
            isNegative = true;
        }
        int gmtHrDiff  = gmtDiff/60;
        int gmtMinDiff = gmtDiff % 60;

            // 6.1 bugfix changelist #65202
        strDate = Fmt.S("%s%s%02s:%02s", strDate,
                        isNegative ? "+" : "-",
                        Constants.getInteger(gmtHrDiff),
                        Constants.getInteger(gmtMinDiff));
        return strDate;
    }

    /*-----------------------------------------------------------------------
        Format key mapping with Sun's formats
      -----------------------------------------------------------------------*/

    /**
     * Translates the Sun's constant defined in {@link DateFormat} into a localized
     * resource that is a valid Date format matching Sun's associated format.
     *
     * Sun has defined 4 constants (SHORT, MEDIUM, LONG and FULL) and has associated
     * to them a time and a date format. This method will return the date format that
     * is the closest to Sun's format. In many cases the format will be identical.
     *
     * <i>Warning</i>: For now, only SHORT and MEDIUM are implemented. You will get
     * an assert for the others.
     *
     * @param style date format as defined by Sun
     * @param locale locale to use to look up the date format
     * @return a valid date format matching the Sun's constant
     * @aribaapi ariba
     */
    public static String getTimePatternEquivalentToSunFormat (int style, Locale locale)
    {
        switch (style) {
        case DateFormat.SHORT:
            return lookupLocalizedFormat(ShortTimeFormatKey, locale);
        case DateFormat.MEDIUM:
            return lookupLocalizedFormat(HoursMinutesSecondsFormatKey, locale);
        default:
            Assert.fail(
                "Invalid argument: %s is either not supported or not a valid value",
                new Object[] {Constants.getInteger(style)});
            return null;
        }
    }

    /**
     * Translates the Sun's constant defined in {@link DateFormat} into a localized
     * resource that is a valid Date format matching Sun's associated format.
     *
     * Sun has defined 4 constants (SHORT, MEDIUM, LONG and FULL) and has associated
     * to them a time and a date format. This method will return the date format that
     * is the closest to Sun's format. In many cases the format will be identical.
     *
     * <i>Warning</i>: For now, only SHORT and LONG are implemented. You will get
     * an assert for the others.
     *
     * @param style date format as defined by Sun
     * @param locale locale to use to look up the date format
     * @return a valid date format matching the Sun's constant
     * @aribaapi ariba
     */
    public static String getDatePatternEquivalentToSunFormat (int style, Locale locale)
    {
        switch (style) {
        case DateFormat.SHORT:
            return lookupLocalizedFormat(ConciseDateFormatKey, locale);
        case DateFormat.LONG:
            return lookupLocalizedFormat(DateMonthYearFormatKey,locale);
        default:
            Assert.fail(
                "Invalid argument: %s is either not supported or not a valid value",
                new Object[] {Constants.getInteger(style)});
            return null;
        }
    }

    /**
     * Translates the Sun's constant defined in {@link DateFormat} into a localized
     * resource that is a valid Date format matching Sun's associated format.
     *
     * Sun has defined 4 constants (SHORT, MEDIUM, LONG and FULL) and has associated
     * to them a time and a date format. This method will return the date format that
     * is the closest to Sun's format. In many cases the format will be identical.
     *
     * <i>Warning</i>: For now, only SHORT is implemented. You will get
     * an assert for the others.
     *
     * @param style date format as defined by Sun
     * @param locale locale to use to look up the date format
     * @return a valid date format matching the Sun's constant
     * @aribaapi ariba
     */
    public static String getDateTimePatternEquivalentToSunFormat (int style,
                                                                    Locale locale)
    {
        switch (style) {
        case DateFormat.SHORT:
            return lookupLocalizedFormat(ConciseDateTimeFormatKey, locale);
        default:
            Assert.fail(
                "Invalid argument: %s is either not supported or not a valid value",
                new Object[] {Constants.getInteger(style)});
            return null;
        }
    }

    /*-----------------------------------------------------------------------
        Static Parsing
      -----------------------------------------------------------------------*/
    /**
        Tries to parse the given string as a <code>Date</code> in the default
        locale and time zone. This method parses the string using a list of
        default formats specified by the SystemFormats and DefaultFmt entries in
        the resource.date.csv file. The actual format to use is the first format in
        the list where the string can be successfully parsed. It is important to realize
        that the actual format used may not be what the caller intends to use. Consequently,
        the parsed Date object may not be the value as expected by the caller. If the caller
        intends to use a specific format, it is recommended that {@link #parseDate(java.lang.String, java.lang.String)}
        or {@link #parseDateUsingFormats(java.lang.String, java.util.Locale, java.lang.String)} be used instead.

        @param string the string to parse as a <code>Date</code>
        @return       a <code>Date</code> derived from the string
        @exception    ParseException if the string can't be parsed as a
                      <code>Date</code>
        @aribaapi documented
        @see #parseDate(java.lang.String, java.lang.String)
        @see #parseDateUsingFormats(java.lang.String, java.util.Locale, java.lang.String)
    */
    public static ariba.util.core.Date parseDate (String string)
      throws ParseException
    {
        return parseDate(string, getDefaultLocale(), getDefaultTimeZone(), false);
    }

    /**
        Tries to parse the given string as a <code>Date</code> in the default
        locale and time zone. This method parses the string using a list of
        default formats specified by the SystemFormats and DefaultFmt entries in
        the resource.date.csv file. The actual format to use is the first format in
        the list where the string can be successfully parsed. It is important to realize
        that the actual format used may not be what the caller intends to use. Consequently,
        the parsed Date object may not be the value as expected by the caller. If the caller
        intends to use a specific format, it is recommended that {@link #parseDate(String, String, boolean)} or
        {@link #parseDateUsingFormats(java.lang.String, java.util.Locale, java.lang.String, boolean)}  be used instead.

        @param string       the string to parse as a <code>Date</code>
        @param calendarDate if true, a calendar date will be created
        @return             a <code>Date</code> derived from the string
        @exception          ParseException if the string can't be parsed as a
                            <code>Date</code>
        @see #parseDate(String, String, boolean)
        @see #parseDateUsingFormats(java.lang.String, java.util.Locale, java.lang.String, boolean)
        @aribaapi documented
    */
    public static ariba.util.core.Date parseDate (String string, boolean calendarDate)
      throws ParseException
    {
        return parseDate(string, getDefaultLocale(), getDefaultTimeZone(), calendarDate);
    }

    /**
        Tries to parse the given string as a <code>Date</code> in the given
        locale.  This method parses the string using a list of
        default formats specified by the SystemFormats and DefaultFmt entries in
        the resource.date.csv file. The actual format to use is the first format in
        the list where the string can be successfully parsed. It is important to realize
        that the actual format used may not be what the caller intends to use. Consequently,
        the parsed Date object may not be the value as expected by the caller. If the caller
        intends to use a specific format, it is recommended that
        {@link #parseDate(String, String, java.util.Locale)} or
        {@link #parseDateUsingFormats(String,java.util.Locale,String)}
        be used instead.

        @param     string the string to parse as a <code>Date</code>
        @param     locale the <code>Locale</code> to use for parsing, must not be <b>null</b>
        @return           a <code>Date</code> derived from the string
        @exception        ParseException if the string can't be parsed as a
                          <code>Date</code>
        @aribaapi documented
        @see #parseDate(String,String,java.util.Locale)
        @see #parseDateUsingFormats(String,java.util.Locale,String)
    */
    public static ariba.util.core.Date parseDate (String string, Locale locale)
      throws ParseException
    {
        return parseDate(string, locale, false);
    }

    /**
        Tries to parse the given string as a <code>Date</code> in the given
        locale using the default time zone. This method parses the string using a list of
        default formats specified by the SystemFormats and DefaultFmt entries in
        the resource.date.csv file. The actual format to use is the first format in
        the list where the string can be successfully parsed. It is important to realize
        that the actual format used may not be what the caller intends to use. Consequently,
        the parsed Date object may not be the value as expected by the caller. If the caller
        intends to use a specific format, it is recommended that
        {@link #parseDate(String, String, java.util.Locale, boolean)} or
        {@link #parseDateUsingFormats(String,java.util.Locale,String,boolean)}
        be used instead.

        @param string       the string to parse as a <code>Date</code>
        @param locale       the <code>Locale</code> to use for parsing, must not be <b>null</b>
        @param calendarDate if true, a calendar date will be created
        @return             a <code>Date</code> derived from the string
        @exception          ParseException if the string can't be parsed as a
                            <code>Date</code>
        @see #parseDate(String, String, java.util.Locale, boolean)
        @see #parseDateUsingFormats(String,java.util.Locale,String,boolean)
        @aribaapi documented
    */
    public static ariba.util.core.Date parseDate (String string, Locale locale, boolean calendarDate)
      throws ParseException
    {
        return parseDate(string, locale, getDefaultTimeZone(), calendarDate);
    }

    /**
        Tries to parse the given string as a <code>Date</code> in the given
        locale and time zone using a list of default formats. This list is
        specified by the SystemFormats and DefaultFmt entries in
        the resource.date.csv file. The actual format to use is the first format in
        the list where the string can be successfully parsed. It is important to realize
        that the actual format used may not be what the caller intends to use. Consequently,
        the parsed Date object may not be the value as expected by the caller. If the caller
        intends to use a specific format, it is recommended that
        {@link #parseDate(String,String,java.util.Locale,java.util.TimeZone,boolean)} or
        {@link #parseDateUsingFormats(String,java.util.Locale,String,java.util.TimeZone,boolean)}
        be used instead.

        @param string       the string to parse as a <code>Date</code>
        @param locale       the <code>Locale</code> to use for parsing, must not be <b>null</b>.
        @param timeZone     the <code>TimeZone</code> to use for parsing, must not be <b>null</b>.
        @param calendarDate if true, a calendar date will be created
        @return             a <code>Date</code> derived from the string
        @exception          ParseException if the string can't be parsed as a
                            <code>Date</code>
        @see #parseDate(String,String,java.util.Locale,java.util.TimeZone,boolean)
        @see #parseDateUsingFormats(String,java.util.Locale,String,java.util.TimeZone,boolean)
        boolean calendarDate)
        @aribaapi documented
    */
    public static ariba.util.core.Date parseDate (
        String   string,
        Locale   locale,
        TimeZone timeZone,
        boolean  calendarDate)
      throws ParseException
    {
        Assert.that(locale != null, "invalid null Locale");

            // try the localized list of system date formats
        ariba.util.core.Date result =
            parseDateUsingDefaultFormats(string, locale, timeZone, calendarDate);
        if (result != null) {
            return result;
        }

        throw makeParseException(CannotParseDateKey, string, 0);
    }

    /**
        Tries to parse the given string as a date in the given locale using
        the default time zone.  Uses the given <code>formatsKey</code> to look
        up a string which is the list of date formats to use to try and parse
        the string as a date.

        @param string       the string to parse as a <code>Date</code>
        @param locale       the <code>Locale</code> to use for parsing, must be non-null.
        @param formatsKey   the date format to use for parsing
        @return             a <code>Date</code> derived from the string, <b>null</b> if the sting cannot be parsed.
        @aribaapi documented
    */
    public static ariba.util.core.Date parseDateUsingFormats (
        String string,
        Locale locale,
        String formatsKey)
    {
        return parseDateUsingFormats(string, locale, formatsKey, false);
    }

    /**
        Tries to parse the given string as a date in the given locale using
        the default time zone.  Uses the given <code>formatsKey</code> to look
        up a string which is the list of date formats to use to try and parse
        the string as a date.

        @param string       the string to parse as a <code>Date</code>
        @param locale       the <code>Locale</code> to use for parsing, must be non-null.
        @param formatsKey   the date format to use for parsing
        @param calendarDate if true, a calendar date will be created
        @return             a <code>Date</code> derived from the string, <b>null</b> if the sting cannot be parsed.

        @aribaapi documented
    */
    public static ariba.util.core.Date parseDateUsingFormats (
        String   string,
        Locale   locale,
        String   formatsKey,
        boolean  calendarDate)
    {
        return parseDateUsingFormats(string, locale, formatsKey,
                                     getDefaultTimeZone(), calendarDate);
    }

    /**
        Tries to parse the given string as a date in the given locale and time
        zone.  Uses the given <code>formatsKey</code> to look up a string
        which is the list of date formats to use to try and parse the string
        as a date.

        @param string       the 
