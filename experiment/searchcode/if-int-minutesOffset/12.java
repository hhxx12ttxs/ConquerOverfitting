/**
 * 
 */
package org.sourceforge.ah.android.utilities.Formatters;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author Aston Hamilton
 * 
 */
public class DateFormatter {
    public static String getISODateString(final Date date) {
	final DateTime dt = new DateTime(date);
	return DateFormatter.getISODateString(dt);
    }

    public static String getISODateString(final DateTime dt) {
	final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
	final String str = fmt.print(dt);

	return str;
    }

    public static String getShortDateTime(final DateTime date) {
	final String format;
	if (DateTime.now().year().get() != date.year().get())
	    format = "MMM d, yyyy h:mma";
	else if (DateTime.now().monthOfYear().get() != date.monthOfYear().get())
	    format = "MMM d h:mma";
	else if (DateTime.now().dayOfMonth().get() == date.dayOfMonth().get())
	    format = "h:mma";
	else
	    format = "MMM d h:mma";

	return DateTimeFormat.forPattern(format).print(date);
    }

    public static String getLongDateTime(final DateTime date) {
	final String format = "MMM d, yyyy h:mma";
	return DateTimeFormat.forPattern(format).print(date);
    }

    public static String getLongDate(final DateTime date) {
	final String format = "MMM d, yyyy";
	return DateTimeFormat.forPattern(format).print(date);
    }

    public static long getUnixSecondsFromDateTime(final DateTime dateTime) {
	return dateTime.getMillis() / 1000l;
    }

    public static DateTime getDateTimeFromUnixSeconds(final long seconds) {
	return new DateTime(seconds * 1000l);
    }

    public static DateTime getDateTimeFromISOString(String dateString) {
	DateTime dateTime = null;

	final int dateStringLength = dateString.length();

	String timezoneOffset = null;

	char checkChar = dateString.charAt(dateStringLength - 1);
	if (checkChar == 'z' || checkChar == 'Z')
	    dateString = dateString.substring(0, dateStringLength - 1);
	else {
	    checkChar = dateString.charAt(dateStringLength - 6);
	    if (checkChar == '-' || checkChar == '+') {
		timezoneOffset = dateString.substring(dateStringLength - 6);
		dateString = dateString.substring(0, dateStringLength - 6);
	    } else {
		checkChar = dateString.charAt(dateStringLength - 5);
		if (checkChar == '-' || checkChar == '+') {
		    timezoneOffset = dateString.substring(dateStringLength - 5);
		    dateString = dateString.substring(0, dateStringLength - 5);
		}
	    }
	}

	try {
	    dateTime = ISODateTimeFormat.dateHourMinuteSecondMillis()
		    .parseDateTime(dateString);
	} catch (final IllegalArgumentException e) {
	    dateTime = ISODateTimeFormat.dateHourMinuteSecond().parseDateTime(
		    dateString);
	}

	if (checkChar == '+') {
	    final int timezoneOffsetLength = timezoneOffset.length();
	    final String hoursOffset = timezoneOffset.substring(1, 3);
	    final String minutesOffset = timezoneOffset
		    .substring(timezoneOffsetLength - 2);

	    dateTime = dateTime.minusHours(Integer.parseInt(hoursOffset));
	    dateTime = dateTime.minusMinutes(Integer.parseInt(minutesOffset));
	} else if (checkChar == '-') {
	    final int timezoneOffsetLength = timezoneOffset.length();
	    final String hoursOffset = timezoneOffset.substring(1, 3);
	    final String minutesOffset = timezoneOffset
		    .substring(timezoneOffsetLength - 2);

	    dateTime = dateTime.plusHours(Integer.parseInt(hoursOffset));
	    dateTime = dateTime.plusMinutes(Integer.parseInt(minutesOffset));
	}

	dateTime = dateTime.withZoneRetainFields(DateTimeZone.UTC).withZone(
		DateTimeZone.getDefault());

	return dateTime;
    }
}

