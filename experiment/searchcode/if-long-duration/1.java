/*
 * Duration.java
 * Created on Sep 10, 2004 1:03:21 PM
 * By Thijs
 *
 * (c) Copyright 2004, SEMANTICA B.V. All rights reserved.
 *
 * This software is the proprietary information of SEMANTICA B.V.
 *
 */
package com.suijten.bordermaker;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;


public class Duration extends AbstractDuration {
	private long duration = 0;
	private ResourceBundle resourceBundle;
	
	public Duration(long duration, Locale locale) {
		this.duration = duration;
		try {
			if(locale != null) {
				this.resourceBundle = ResourceBundle.getBundle(this.getClass().getName(), locale);
			} else {
				this.resourceBundle = ResourceBundle.getBundle(this.getClass().getName());
			}
		} catch (Exception e){}
		calculate();
	}
	
	public Duration(long duration) {
		this(duration, null);
	}
	
	protected long getDuration() {
		return duration;
	}
	
	protected String getLabel(String key, int value) {
		String m = null;
		if(resourceBundle == null) {
			if(key.equals(KEYS.MILLISECONDS.PLURAL)) {
				m = "{0} milliseconds";
			} else if(key.equals(KEYS.MILLISECONDS.SINGULAR)) {
				m = "1 millisecond";
			} else if(key.equals(KEYS.MILLISECONDS.ZERO)) {
				m = "0 milliseconds";
			} else if(key.equals(KEYS.SECONDS.PLURAL)) {
				m = "{0} seconds";
			} else if(key.equals(KEYS.SECONDS.SINGULAR)) {
				m = "1 second";
			} else if(key.equals(KEYS.SECONDS.ZERO)) {
				m = "0 seconds";
			} else if(key.equals(KEYS.MINUTES.PLURAL)) {
				m = "{0} minutes";
			} else if(key.equals(KEYS.MINUTES.SINGULAR)) {
				m = "1 minute";
			} else if(key.equals(KEYS.MINUTES.ZERO)) {
				m = "0 minutes";
			} else if(key.equals(KEYS.HOURS.PLURAL)) {
				m = "{0} hours";
			} else if(key.equals(KEYS.HOURS.SINGULAR)) {
				m = "1 hour";
			} else if(key.equals(KEYS.HOURS.ZERO)) {
				m = "0 hours";
			} else if(key.equals(KEYS.DAYS.PLURAL)) {
				m = "{0} days";
			} else if(key.equals(KEYS.DAYS.SINGULAR)) {
				m = "1 day";
			} else if(key.equals(KEYS.DAYS.ZERO)) {
				m = "0 days";
			} else if(key.equals(KEYS.WEEKS.PLURAL)) {
				m = "{0} weeks";
			} else if(key.equals(KEYS.WEEKS.SINGULAR)) {
				m = "1 week";
			} else if(key.equals(KEYS.WEEKS.ZERO)) {
				m = "0 weeks";
			}
		} else {
			try {
				m = resourceBundle.getString(key);
			} catch (Exception e) {
				return "[" + key + "]"; 
			}
		}
		if(key.indexOf("plural") != -1) {
			return MessageFormat.format(m, new Object[]{String.valueOf(value)});
		} else {
			return m;
		}
	}
}

