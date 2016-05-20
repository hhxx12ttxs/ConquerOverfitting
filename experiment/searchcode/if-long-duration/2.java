/*
 * Copyright (c) 2006, 2007 Andy Armstrong, Kelsey Grant and other contributors.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * The names of contributors may not
 *       be used to endorse or promote products derived from this software without
 *       specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.andya.confluence.utils;

import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.InvalidDurationException;

/**
 * A number that represents a duration in seconds.
 */
public class Duration extends Number {
	private static final long serialVersionUID = -91792353858202089L;

	private final long duration;

	public Duration(long duration) {
		this.duration = duration;
	}

	/**
	 * Creates a duration if the string is in the correct format, else returns
	 * null.
	 */
	public static Duration parseDuration(String durationString) {
		if (!isDuration(durationString))
			return null;
		durationString = durationString.trim();
		try {
			long duration = DateUtils.getDuration(durationString);
			return new Duration(duration);
		} catch (InvalidDurationException e) {
			return null;
		}
	}

	/**
	 * Returns true if the specified string is a duration in the Confluence
	 * format.
	 */
	public static boolean isDuration(String durationString) {
		// todo: this should be part of DateUtils
		durationString = durationString.trim();
		int durationLength = durationString.length();
		if (durationLength == 0)
			return false;
		char lastChar = durationString.charAt(durationLength - 1);
		return "mhdw".indexOf(String.valueOf(lastChar)) >= 0;
	}

	/** Returns the string representation of this duration (e.g. -1h 30m). */
	public String getDurationString() {
		return DateUtils.getDurationStringWithNegative(longValue());
	}

	public double doubleValue() {
		return duration;
	}

	public float floatValue() {
		return duration;
	}

	public int intValue() {
		return (int) duration;
	}

	public long longValue() {
		return duration;
	}

	public boolean equals(Object object) {
		if (object instanceof Duration)
			return longValue() == ((Duration) object).longValue();
		return false;
	}

	public int hashCode() {
		return intValue();
	}

	public String toString() {
		return getDurationString();
	}
}

