/**
 * Copyright 2011 FeedDreamwork SIG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.feeddreamwork;

import java.text.*;
import java.util.*;

public final class DateUtils {
	private static final String RFC2822_PATTERN = "EEE, dd MMM yyyy HH:mm:ss Z";
	private static final String ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";
	private static final String FALLBACK_PATTERN = "yyyy-MM-dd HH:mm";
	private static final String ONLYDATE_PATTERN = "yyyy-MM-dd";
	private static final String ONLYDATE2_PATTERN = "dd/MM/yyyy";
	private static final String PRINTDATE_PATTERN = "yyyy-MM-dd HH:mm:ss'.'SSS";
	private static final String CLASSICDATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	private static final String DATETIME2_PATTERN = "yyyy/MM/dd'('HH:mm:ss')'";
	private static final String DATETIME3_PATTERN = "yyyy-MM-dd '('HH:mm')'";
	private static final String DATETIME4_PATTERN = "M/d/yyyy h:mm";
	private static final String DATETIME5_PATTERN = "yyyy?MM?dd?HH:mm";
	private static final String [] patterns = {
		RFC2822_PATTERN, 
		ISO8601_PATTERN,
		DATETIME2_PATTERN,
		DATETIME3_PATTERN,
		DATETIME5_PATTERN,
		CLASSICDATETIME_PATTERN,
		FALLBACK_PATTERN,
		PRINTDATE_PATTERN,
		ONLYDATE_PATTERN,
		ONLYDATE2_PATTERN,
	};

	public static String formatDate(String pattern, Date source) {
		Utils.throwIfNullOrEmpty(pattern);
		Utils.throwIfNull(source);
		return getDateFormat(pattern).format(source);
	}

	public static Date parseDate(String pattern, String source)
			throws ParseException {
		Utils.throwIfNullOrEmpty(pattern);
		Utils.throwIfNullOrEmpty(source);
		try {
			return getDateFormat(pattern).parse(source);
		} catch (ParseException e) {
			return getDateFormat(FALLBACK_PATTERN).parse(source);
		}
	}
	
	public static Date parseDate(String source) {
		Utils.throwIfNullOrEmpty(source);
		try {
			if (source.contains("PM") || source.contains("AM"))
			{
				Date ans = getDateFormat(DATETIME4_PATTERN).parse(source.replaceAll("(A|P)M", ""));
				if (source.contains("PM") && !source.contains("12:00"))
					ans = new Date(ans.getTime() + 43200000);
				if (source.contains("12:00AM"))
					ans = new Date(ans.getTime() - 43200000);
				return ans;
			}
		} catch (Exception e) {}
		for (String pattern : patterns)
			try {
				return getDateFormat(pattern).parse(source);
			} catch (Exception e) {}
		return new Date(0);
	}

	public static String formatDateAsRFC2822(Date source) {
		return DateUtils.formatDate(RFC2822_PATTERN, source);
	}

	public static String formatDateAsISO8601(Date source) {
		String result = DateUtils.formatDate(ISO8601_PATTERN, source);
		return result.substring(0, result.length() - 2) + ":"
				+ result.substring(result.length() - 2);
	}

	public static String formatDateAsSimpleDateTime(Date source) {
		return DateUtils.formatDate(PRINTDATE_PATTERN, source);
	}

	public static Date parseRFC2822Date(String source) throws ParseException {
		return DateUtils.parseDate(RFC2822_PATTERN, source);
	}

	public static Date parseISO8601Date(String source) throws ParseException {
		Utils.throwIfNullOrEmpty(source);
		source = source.replace("Z", "+00:00");
		char sign = source.charAt(source.length() - 3);
		if (sign == ':')
			source = source.substring(0, source.length() - 3)
					+ source.substring(source.length() - 2);
		if (sign == '+' || sign == '-')
			source += "00";
		int pos = source.lastIndexOf('.');
		if (pos != -1)
			source = source.substring(0, pos) + source.substring(pos + 4);
		return DateUtils.parseDate(ISO8601_PATTERN, source);
	}

	public static Date parseOnlyDate(String source) throws ParseException {
		return DateUtils.parseDate(ONLYDATE_PATTERN, source);
	}

	private static DateFormat getDateFormat(String pattern) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);
		dateFormat.setTimeZone(TimeZone
				.getTimeZone(ApplicationProperty.DEFAULT_TIME_ZONE));
		return dateFormat;
	}

	private DateUtils() {
	}
}

