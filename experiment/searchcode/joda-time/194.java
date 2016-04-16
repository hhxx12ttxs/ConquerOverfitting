/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package ilarkesto.core.time;

import ilarkesto.core.base.Utl;

import java.io.Serializable;

public class TimePeriod implements Comparable<TimePeriod>, Serializable {

	public static final TimePeriod ZERO = new TimePeriod(0);

	private long millis;

	public TimePeriod(long millis) {
		this.millis = Math.abs(millis);
	}

	public TimePeriod(String s) {
		int idx = s.indexOf(':');
		if (idx >= 0) {
			millis = 0;
			int i = 0;
			while (idx >= 0) {
				String num = s.substring(0, idx);
				long l = Long.parseLong(num);
				switch (i) {
					case 0:
						millis += l * Tm.HOUR;
						break;
					case 1:
						millis += l * Tm.MINUTE;
						break;
					case 2:
						millis += l * Tm.SECOND;
						break;
					default:
						throw new IllegalArgumentException(s);
				}
				i++;
				s = s.substring(idx + 1);
				idx = s.indexOf(':');
			}
			long l = Long.parseLong(s);
			switch (i) {
				case 0:
					millis += l * Tm.HOUR;
					break;
				case 1:
					millis += l * Tm.MINUTE;
					break;
				case 2:
					millis += l * Tm.SECOND;
					break;
				default:
					millis += l;
			}
		} else {
			this.millis = Long.parseLong(s);
		}
	}

	public TimePeriod() {
		this(0);
	}

	// ---

	public TimePeriod addDays(int days) {
		return new TimePeriod(millis + Tm.DAY);
	}

	public TimePeriod multiplyBy(int factor) {
		return new TimePeriod(millis * factor);
	}

	public TimePeriod getPeriodTo(Time other) {
		return new TimePeriod(other.toMillis() - toMillis());
	}

	public TimePeriod abs() {
		return millis < 0 ? new TimePeriod(-millis) : this;
	}

	public final long toMillis() {
		return millis;
	}

	public final long toSeconds() {
		return millis / Tm.SECOND;
	}

	public final long toMinutes() {
		return millis / Tm.MINUTE;
	}

	public final long toHours() {
		return millis / Tm.HOUR;
	}

	public final int toDays() {
		return (int) (millis / Tm.DAY);
	}

	public final int toWeeks() {
		return (int) (millis / Tm.WEEK);
	}

	public final int toMonths() {
		return (int) (millis / Tm.MONTH);
	}

	public final int toYears() {
		return (int) (millis / Tm.YEAR);
	}

	public final boolean isNegative() {
		return millis < 0;
	}

	public final boolean isPositive() {
		return millis > 0;
	}

	public String toShortestString() {
		return toShortestString(Utl.getLanguage());
	}

	public final String toShortestString(String language) {
		TmLocalizer loc = Tm.getLocalizer(language);
		StringBuilder sb = new StringBuilder();
		long m = millis >= 0 ? millis : -millis;
		if (m >= (Tm.MONTH * 2)) {
			int i = toMonths();
			sb.append(i);
			sb.append(" ").append(loc.months(i));
		} else if (m >= (Tm.WEEK * 2)) {
			int i = toWeeks();
			sb.append(i);
			sb.append(" ").append(loc.weeks(i));
		} else if (m >= Tm.DAY) {
			int i = toDays();
			sb.append(i);
			sb.append(" ").append(loc.days(i));
		} else if (m >= ((Tm.HOUR * 2) - (Tm.MINUTE - 20))) {
			long l = toHours();
			sb.append(l);
			sb.append(" ").append(loc.hours(l));
		} else if (m >= Tm.MINUTE) {
			long l = toMinutes();
			sb.append(l);
			sb.append(" ").append(loc.minutes(l));
		} else if (m >= Tm.SECOND) {
			long l = toSeconds();
			sb.append(l);
			sb.append(" ").append(loc.seconds(l));
		} else {
			sb.append(m);
			sb.append(" ").append(loc.millis(m));
		}
		return sb.toString();
	}

	public final String toHoursAndMinutes() {
		long hours = toHours();
		long remainingMillis = millis - (hours * 3600000);
		long minutes = remainingMillis / 60000;
		return hours + (minutes > 9 ? ":" : ":0") + minutes;
	}

	@Override
	public final int compareTo(TimePeriod o) {
		if (millis == o.millis) return 0;
		return millis > o.millis ? 1 : -1;
	}

	@Override
	public final int hashCode() {
		return (int) millis;
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof TimePeriod)) return false;
		return millis == ((TimePeriod) obj).millis;
	}

	@Override
	public final String toString() {
		return String.valueOf(millis);
	}

	// --- static ---

	public static TimePeriod seconds(int seconds) {
		return new TimePeriod(seconds * 1000);
	}

	public static TimePeriod minutes(int minutes) {
		return seconds(minutes * 60);
	}

	public static TimePeriod hours(int hours) {
		return minutes(hours * 60);
	}

	public static TimePeriod days(int days) {
		return hours(days * 24);
	}

	public static TimePeriod weeks(int weeks) {
		return days(weeks * 7);
	}

}

