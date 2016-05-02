/*
 *      Copyright 2010 Battams, Derek
 *       
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 */
package com.google.code.sagetvaddons.sagealert.server.globals;

import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

/**
 * <p>A simple extension of java.util.Date that adds a format() method to format the date with the given DateFormat pattern</p>
 * <p>An instance of this object is available via the global object <b>$date</b></p>
 * 
 * @author dbattams
 * @version $Id: Date.java 1024 2010-09-19 04:49:19Z derek@battams.ca $
 */
public class Date extends java.util.Date {
	static private final Logger LOG = Logger.getLogger(Date.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor; sets date to current time
	 */
	public Date() {
		super();
	}

	/**
	 * Constructor; sets object to represent given timestamp
	 * @param date The Java timestamp representing the date and time this object represents
	 */
	public Date(long date) {
		super(date);
		// TODO Auto-generated constructor stub
	}

	/**
	 * <p>Return the date as a string formatted using the given SimpleDateFormat pattern string</p>
	 * <p>If the format string is invalid then this method simply returns the given format string with no substitutions</p>
	 * @param fmt The format for the string representation of this date
	 * @return The date formatted as specified by the given format string
	 * @see java.text.SimpleDateFormat
	 */
	public String format(String fmt) {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(fmt);
			return formatter.format(this);			
		} catch(IllegalArgumentException e) {
			LOG.error("Invalid format string! [" + fmt + "]");
			return fmt;
		}
	}	
}

