 /* Copyright 2011 University of Arkansas for Medical Sciences
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
package edu.uams.dbmi.util.iso8601;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.quantity.Duration;
import javax.measure.unit.Unit;

public class Iso8601TimeParser {
	
	static final String regex = "^T?((([0-1]\\d|2[0-3])(([0-5]\\d)(([0-5]\\d))?)?([\\.,]\\d+)?)|((24)((00)((00))?)?)([\\.,]0+)?)" +
		"(Z|([+-])(0[1-9]|1[0-2])(([0-5]\\d))?)?";

	static final String regex_extended = "^T?((([0-1]\\d|2[0-3])(:([0-5]\\d)(:([0-5]\\d))?)?([\\.,]\\d+)?)|((24)(:(00)(:(00))?)?)([\\.,]0+)?)" +
		"(Z|([+-])(0[1-9]|1[0-2])(:([0-5]\\d))?)?";

	
	static final int HOUR_GROUP = 3;
	static final int MIN_GROUP = 5;
	static final int SEC_GROUP = 7;
	static final int FR_GROUP = 8;
	static final int HR24_GROUP = 10;
	static final int MI24_GROUP = 12;
	static final int SE24_GROUP = 14;
	static final int FR24_GROUP = 15;
	static final int Z_GROUP = 16;
	static final int TZSIGN_GROUP = 17;
	static final int TZHR_GROUP = 18;
	static final int TZMI_GROUP = 20;
	
	Pattern p = Pattern.compile(regex);
	Pattern p_exp = Pattern.compile(regex_extended);
	
	boolean isExtended;
	boolean isExtendedConsistent;
	
	public Iso8601TimeParser() {
		p = Pattern.compile(regex);
		p_exp = Pattern.compile(regex_extended);
	}
	
	public Iso8601Time parse(String s) throws Iso8601TimeParseException {
		Iso8601Time t = null;
		isExtended = s.contains(":");
		Matcher m = (isExtended) ? p_exp.matcher(s) : p.matcher(s);
		if (m.matches()) {
			/*
			 * Group 3 is hour
			 * Group 5 is minute
			 * Group 7 is second
			 * Group 8 is fraction
			 * 
			 * If time is 24:00 or 24:00:00
			 * 		Group 10 is hour
			 * 		Group 11 is minute
			 * 		Group 13 is second
			 * 
			 * Group 14 is "Z" if UTC
			 * Group 15 is + or -
			 * Group 16 is time zone hour
			 * Group 18 is time zone minute
			 * 
			 */
			int hr_group, mi_group, se_group, fr_group;
			if (m.group(HOUR_GROUP) != null) {
				hr_group = HOUR_GROUP;
				mi_group = MIN_GROUP;
				se_group = SEC_GROUP;
				fr_group = FR_GROUP;
			} else {
				hr_group = HR24_GROUP;
				mi_group = MI24_GROUP;
				se_group = SE24_GROUP;
				fr_group = FR24_GROUP;
			}
			String frTxt = m.group(fr_group);
			frTxt = (frTxt == null) ? null : frTxt.replace(',', '.');
			String seTxt = m.group(se_group);
			boolean isUnit = (frTxt == null) || 
								(frTxt != null && seTxt != null);
			int hr = Integer.parseInt(m.group(hr_group));
			String miTxt = m.group(mi_group);
			
			IsoTimeBuilder tb;
			if (isUnit) {
				tb = new IsoUnitTimeBuilder(hr);
				if (seTxt != null) {
					((IsoUnitTimeBuilder)tb).setSecond(
							Integer.parseInt(seTxt));
				}
				if (frTxt != null) {
					setSubsecondAndUnit((IsoUnitTimeBuilder)tb, frTxt);
				}
			} else {
				tb = new IsoFractionalTimeBuilder(hr);
				((IsoFractionalTimeBuilder)tb).setFraction(
						Double.parseDouble(frTxt));
			}

			if (miTxt != null) {
				tb.setMinute(Integer.parseInt(m.group(mi_group)));
			}

			if (m.group(Z_GROUP) != null && m.group(Z_GROUP).equals("Z")) {
				tb.setIsUtc(true);
			} else if (m.group(TZHR_GROUP) != null) {
				int tzHr = Integer.parseInt(m.group(TZHR_GROUP));
				if (m.group(TZSIGN_GROUP).equals("-")) {
					tzHr = -tzHr;
				}
				tb.setTimeZoneOffsetHour(tzHr);
				if (m.group(TZMI_GROUP) != null) {
					tb.setTimeZoneOffsetMinute(
							Integer.parseInt(m.group(TZMI_GROUP)));
				}
			}
			/*
			 * If the time string has just hour or just hour and timezone 
			 *  hour offset, then it's consistent with extended, because
			 *  it has no need for : delimiter.
			 */
			isExtendedConsistent = isExtended || 
				(tb.min == null && m.group(TZMI_GROUP) == null);
			if (isUnit) {
				t = new Iso8601UnitTime((IsoUnitTimeBuilder)tb);
			} else {
				t = new Iso8601FractionalTime((IsoFractionalTimeBuilder)tb);
			}
		} else {
			throw new Iso8601TimeParseException("Illegal 8601 time: " + s);
		}
		
		return t;
	}
	
	protected void setSubsecondAndUnit(IsoUnitTimeBuilder tb, String frTxt) {
		String frDotTxt = frTxt.replace(',', '.');
		BigDecimal bd = new BigDecimal(frDotTxt);
		tb.setSubsecond(bd.unscaledValue().intValue());
		long unitDivisor = BigDecimal.TEN.pow(bd.scale()).longValue();
		Unit<Duration> u = TimeUnit.SECOND.divide(unitDivisor);
		if (u==null) { System.err.println("Setting null unit"); }
		tb.setUnit(u);
	}
	
	public boolean isExtended() {
		return isExtended;
	}
	
	public boolean isExtendedConsistent() {
		return isExtendedConsistent;
	}
}

